 package com.albaniliu.chuangxindemo;
 
 import java.io.File;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.Environment;
 import android.os.Handler;
 import android.support.v4.view.ViewPager;
 import android.view.Menu;
 import android.view.View;
 import android.view.animation.Animation;
 import android.view.animation.TranslateAnimation;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 import com.albaniliu.chuangxindemo.widget.LargePicGallery;
 import com.albaniliu.chuangxindemo.widget.LargePicGallery.SingleTapListner;
 import com.albaniliu.chuangxindemo.widget.ViewPagerAdapter;
 
 public class ImageShow extends Activity {
 	private String mPath = Environment.getExternalStorageDirectory() + "/liangdemo1";
 	private File mTestFolder = new File(mPath);
 	private ViewPagerAdapter mAdapter;
 	private LinearLayout mFlowBar;
 	private TextView mFooter;
 	private LargePicGallery mPager;
 	private Handler mHanler = new Handler();
 	
 	private SingleTapListner mListener = new SingleTapListner() {
 		@Override
 		public void onSingleTapUp() {
 			toggleFlowBar();
 		}
 	};
 	
 	private Runnable mToggleRunnable = new Runnable() {
         @Override
         public void run() {
             toggleFlowBar();
         }
     };
 
 	private void toggleFlowBar() {
 	    int delta = mFlowBar.getHeight();
 	    mHanler.removeCallbacks(mToggleRunnable);
 
 	    if (mFlowBar.getVisibility() == View.INVISIBLE
                 || mFlowBar.getVisibility() == View.GONE) {
             mFlowBar.setVisibility(View.VISIBLE);
             mFooter.setVisibility(View.VISIBLE);
             Animation anim = new TranslateAnimation(0, 0, -delta, 0);
             anim.setDuration(300);
             mFlowBar.startAnimation(anim);
             Animation animDown = new TranslateAnimation(0, 0, delta, 0);
             animDown.setDuration(300);
            mFooter.startAnimation(animDown);
             mHanler.postDelayed(mToggleRunnable, 5000);
         } else {
             mFlowBar.setVisibility(View.INVISIBLE);
             mFooter.setVisibility(View.INVISIBLE);
             Animation anim = new TranslateAnimation(0, 0, 0, -delta);
             anim.setDuration(300);
             mFlowBar.startAnimation(anim);
             
             Animation animDown = new TranslateAnimation(0, 0, 0, delta);
             animDown.setDuration(300);
             mFooter.startAnimation(animDown);
         }
 	}
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.largepic);
 		
 		mAdapter = new ViewPagerAdapter(mTestFolder, mListener);
 		mPager = (LargePicGallery) findViewById(R.id.photo_flow);
 		mPager.setOffscreenPageLimit(1);
 		mPager.setPageMargin(20);
 		mPager.setHorizontalFadingEdgeEnabled(true);
 		mPager.setAdapter(mAdapter);
 		mPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
 			@Override
 			public void onPageSelected(int position) {
 				mFooter.setText(mAdapter.getName(position));
 			}
 		});
 		mFlowBar = (LinearLayout) findViewById(R.id.flow_bar);
 		
 		mFooter = (TextView) findViewById(R.id.footer_bar);
 		mFooter.setText(mAdapter.getName(0));
 		
 		mHanler.postDelayed(mToggleRunnable, 5000);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		// getMenuInflater().inflate(R.menu.activity_image_show, menu);
 		return true;
 	}
 
 	public void onBackClick(View view) {
 		onBackPressed();
 	}
 	
 	public void onSlideClick(View view) {
 		Intent intent = new Intent();
 		intent.setClass(getApplicationContext(), SlideShowActivity.class);
 		startActivity(intent);
 	}
 }
