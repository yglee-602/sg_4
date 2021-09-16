package com.example.sg_4;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.Preview;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import androidx.camera.lifecycle.ProcessCameraProvider;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    private final int COUNT=2;					//아이템 갯수
    private int mPrevPosition;					//이전에 선택되었던 포지션 값

    private ViewPager mPager;					//뷰 페이저
    private LinearLayout mPageMark;			    //현재 몇 페이지 인지 나타내는 뷰

    private static final int PERMISSIONS_REQUEST_CODE = 100;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.other);

        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindPreview(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                // No errors need to be handled for this Future.
                // This should never be reached.
            }
        }, ContextCompat.getMainExecutor(this));
        mPageMark = (LinearLayout)findViewById(R.id.page_mark);			//상단의 현재 페이지 나타내는 뷰

        mPager = (ViewPager)findViewById(R.id.pager);						//뷰 페이저
        mPager.setAdapter(new BkPagerAdapter(getApplicationContext()));//PagerAdapter로 설정
        mPager.setCurrentItem(COUNT);			//무한 스크롤 하기 위해서는 아이템을 3배로 가지고 있고 그 중 가운데 범위의 아이템만 보이게 한다
        mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {	//아이템이 변경되면, gallery나 listview의 onItemSelectedListener와 비슷
            @Override public void onPageSelected(int position) {
                if(position < COUNT)												//3배의 아이템중 앞쪽이면 뷰페이져의 포지션을 가운데 범위로 이동시킨다
                    mPager.setCurrentItem(position+COUNT, false);
                else if(position >= COUNT*2)										//3배의 아이템 중 뒤쪽이면 뷰페이져의 포지션을 가운데 범위로 이동시킨다
                    mPager.setCurrentItem(position - COUNT, false);
                else {																	//가운데 범위이면 상단의 페이지 표시를 변경한다
                    position -= COUNT;
                    //아이템이 선택이 되었으면
                    mPageMark.getChildAt(mPrevPosition).setBackgroundResource(R.drawable.page_not);	//이전 페이지에 해당하는 페이지 표시 이미지 변경
                    mPageMark.getChildAt(position).setBackgroundResource(R.drawable.page_select);		//현재 페이지에 해당하는 페이지 표시 이미지 변경
                    mPrevPosition = position;				//이전 포지션 값을 현재로 변경
                }
            }
            @Override public void onPageScrolled(int position, float positionOffest, int positionOffsetPixels) {}
            @Override public void onPageScrollStateChanged(int state) {}
        });

        initPageMark();	//현재 페이지 표시하는 뷰 초기화
    }

    void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {
        Preview preview = new Preview.Builder()
                .build();

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        Camera camera = cameraProvider.bindToLifecycle((LifecycleOwner)this, cameraSelector, preview);
    }

    //상단의 현재 페이지 표시하는 뷰 초기화
    private void initPageMark(){
        for(int i=0; i<COUNT; i++)
        {
            ImageView iv = new ImageView(getApplicationContext());	//페이지 표시 이미지 뷰 생성
            iv.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

            //첫 페이지 표시 이미지 이면 선택된 이미지로
            if(i==0)
                iv.setBackgroundResource(R.drawable.page_select);
            else	//나머지는 선택안된 이미지로
                iv.setBackgroundResource(R.drawable.page_not);

            //LinearLayout에 추가
            mPageMark.addView(iv);
        }
        mPrevPosition = 0;	//이전 포지션 값 초기화
    }

    //Pager Adapter 구현
    private class BkPagerAdapter extends PagerAdapter {
        private Context mContext;
        public BkPagerAdapter( Context con) { super(); mContext = con; }

        @Override public int getCount() { return COUNT * 3; }

        @NonNull
        @Override
        //뷰페이저에서 사용할 뷰객체 생성/등록
        //예제 앱에는 이미지 뷰 객체 생성(실제 앱에서는 다른 뷰 객체로 하고 뷰페이저에 add하는 방식이면 될듯)
        public Object instantiateItem(@NonNull ViewGroup pager, int position) {
            position %= COUNT;
            ImageView iv = new ImageView(getApplicationContext());	//페이지 표시 이미지 뷰 생성
            iv.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));


            if(position==0){
                iv.setBackgroundResource(R.drawable.main_shop);
            } else{
                iv.setBackgroundResource(R.drawable.main_exp);
            }

            ((ViewPager)pager).addView(iv, 0);		//뷰 페이저에 추가

            return iv;
        }

        //뷰 객체 삭제.
        @Override public void destroyItem(ViewGroup pager, int position, Object view) {
            ((ViewPager)pager).removeView((View)view);
        }

        // instantiateItem메소드에서 생성한 객체를 이용할 것인지
        @Override public boolean isViewFromObject(View view, Object obj) { return view == obj; }

        @Override public void finishUpdate(ViewGroup arg0) {}
        @Override public void restoreState(Parcelable arg0, ClassLoader arg1) {}
        @Override public Parcelable saveState() { return null; }
        @Override public void startUpdate(ViewGroup arg0) {}
    }

}