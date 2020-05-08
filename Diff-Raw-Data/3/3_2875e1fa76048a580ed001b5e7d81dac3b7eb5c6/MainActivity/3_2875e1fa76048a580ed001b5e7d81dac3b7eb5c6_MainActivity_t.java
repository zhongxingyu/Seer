 package com.example.win360style;
 
 import java.util.ArrayList;
 
 import android.content.Context;
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.view.PagerAdapter;
 import android.support.v4.view.ViewPager;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnTouchListener;
 import android.view.animation.Animation;
 import android.view.animation.TranslateAnimation;
 import android.widget.FrameLayout;
 import android.widget.ImageView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class MainActivity extends FragmentActivity {
 	private Context context=this;
 	private ViewPager myViewPager;
 	private FrameLayout mSettingFrameLayout;
 	private FrameLayout mMainFrameLayout;
 
 	private boolean mSlided = false;
 	private ImageView mSettingBtn;
 
 	private final static int TRANSLATE_ANIMATION_WIDTH = 140;
 	private final static int ANIMATION_DURATION_FAST = 450;
 	private final static int ANIMATION_DURATION_SLOW = 350;
 	private final static int MOVE_DISTANCE = 50;
 	
 	// Ļ
 	private int mWidth;
 	private float mPositionX;
 	private FrameLayout mMainFrameMaskLayout;
 	private ImageView mSettingNegativeBtn;
 	private PagerAdapter adapter;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		 initalView();
     }
 	private void initalView() {
 		myViewPager=(ViewPager)findViewById(R.id.viewPagerContent);
 //		LinearLayout lin = (LinearLayout)findViewById(R.id.pagerLinear);
 //		LinearLayout.LayoutParams lp =  new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT,LinearLayout.LayoutParams.FILL_PARENT);
 //		lp.gravity=Gravity.LEFT;
 //		lp.leftMargin=20;
 //		lin.setLayoutParams(lp);
 //		LayoutParams param = myViewPager.getLayoutParams();
 //	
 //		myViewPager.setLayoutParams(lp);
 		LayoutInflater inflater = LayoutInflater.from(context);
 		View view1 = myViewPager.inflate(context, R.layout.view1, null);
 		View view2 = myViewPager.inflate(context, R.layout.view2, null);
 		final ArrayList<View> listView = new ArrayList<View>();
 		listView.add(view1);
 		listView.add(view2);
 		adapter = new PagerAdapter(){
 
 			@Override
 			public int getCount() {
 				return listView.size();
 			}
 
 			@Override
 			public boolean isViewFromObject(View arg0, Object arg1) {
 				
 				return arg0==arg1;
 			}
 			 public void destroyItem(View container, int position, Object object) {
 				 ((ViewPager)container).removeView(listView.get(position));
 				 
 			 }
 			 public Object instantiateItem(View container, int position) {
 //				 LinearLayout.LayoutParams lp =  new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT,LinearLayout.LayoutParams.FILL_PARENT);
 //					lp.gravity=Gravity.LEFT;
 //					lp.leftMargin=30;
 //				 container.setLayoutParams(lp);
 				 ((ViewPager)container).addView(listView.get(position));
 
 				return   listView.get(position);
 
 			 }
 		};	
 		myViewPager.setAdapter(adapter);
 		final TextView pageNoTv = (TextView) findViewById(R.id.pageNo);
 		myViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {  
             @Override  
             public void onPageSelected(int arg0) {  
             	if(arg0==0){
             		pageNoTv.setText("1 / 2");
             	}else{
             		pageNoTv.setText("2 / 2");
             	}
    
             }  @Override  
             public void onPageScrolled(int arg0, float arg1, int arg2) {  
   
             }  
   
             @Override  
             public void onPageScrollStateChanged(int arg0) {  
   
             }  
         });  
             
             
 		mWidth = getResources().getDisplayMetrics().widthPixels;
 		mSettingFrameLayout = (FrameLayout) findViewById(R.id.setting);
 		
 		mMainFrameLayout = (FrameLayout) findViewById(R.id.main);
 		mMainFrameMaskLayout = (FrameLayout) findViewById(R.id.main_page_mask);
 //		mMainFrameLayout.setOnTouchListener(mOnTouchListener);
 		mSettingFrameLayout.setVisibility(View.GONE);
 		TranslateAnimation translate = new TranslateAnimation(0, TRANSLATE_ANIMATION_WIDTH, 0, 0);
 		translate.setDuration(ANIMATION_DURATION_FAST);
 		// ʱͣڽλ
 		translate.setFillAfter(true);
 		mSettingFrameLayout.startAnimation(translate);
 		mSettingFrameLayout.getAnimation().setAnimationListener(
 				new Animation.AnimationListener() {
 					
 					@Override
 					public void onAnimationStart(Animation anim) {						 
 					}
 
 					@Override
 					public void onAnimationRepeat(Animation animation) {
 
 					}
 
 					@Override
 					public void onAnimationEnd(Animation anima) {
 						mSettingFrameLayout.setVisibility(View.VISIBLE);
 						
 					}
 				});
 		
 		mSettingBtn = (ImageView) findViewById(R.id.setting_btn);
 		mSettingBtn.setOnClickListener(mOnClickListener);
 		
 
 		mSettingNegativeBtn = (ImageView) findViewById(R.id.setting_negative_btn);
 		mSettingNegativeBtn.setOnClickListener(mOnClickListener);
 		
 		
 		TextView tv = (TextView) findViewById(R.id.menu_net_udpate);
 		tv.setOnClickListener(new View.OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				Toast.makeText(context, "hello", Toast.LENGTH_LONG).show();
 			}
 		});
 	}
 	
 	
 	// ť
 		private OnClickListener mOnClickListener = new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				switch (v.getId()) {
 					case R.id.setting_btn :
 						if (mSlided) {
 							slideIn();
 						} else {
 							slideOut();
 						}
 						break;
 					case R.id.setting_negative_btn :
 						if (mSlided) {
 							slideIn();
 						} else {
 							slideOut();
 						}
 						break;
 				}
 			}
 		};
 		
 		// 
 		private OnTouchListener mOnTouchListener = new OnTouchListener() {
 			@Override
 			public boolean onTouch(View v, MotionEvent event) {
 				if (v.getId() == R.id.main_page_mask) {
 					int action = event.getAction();
 					switch (action) {
 						case MotionEvent.ACTION_DOWN :
 							mPositionX = event.getX();
 							break;
 						case MotionEvent.ACTION_MOVE :
 							final float currentX = event.getX();
 							// ߻
 							if (currentX - mPositionX <= -MOVE_DISTANCE && !mSlided) {
 								slideOut();
 							} else if (currentX - mPositionX >= MOVE_DISTANCE && mSlided) {
 								slideIn();
 							}
 							break;
 					}
 					return true;
 				} 
 				return false;
 			}
 		};
 		
 		
 
 		/**
 		 * 
 		 */
 		private void slideOut() {
 			TranslateAnimation animation = new TranslateAnimation(
 					0, -TRANSLATE_ANIMATION_WIDTH, 0, 0);
 			animation.setDuration(ANIMATION_DURATION_FAST);
 			animation.setFillAfter(true);
 			mMainFrameLayout.startAnimation(animation);			
 			mMainFrameLayout.getAnimation().setAnimationListener(
 					new Animation.AnimationListener() {
 						
 						@Override
 						public void onAnimationStart(Animation anim) {
 							TranslateAnimation translate = new TranslateAnimation(TRANSLATE_ANIMATION_WIDTH,
 									0, 0, 0);
 							translate.setDuration(ANIMATION_DURATION_SLOW);
 							translate.setFillAfter(true);
 							mSettingFrameLayout.startAnimation(translate);
 						}
 
 						@Override
 						public void onAnimationRepeat(Animation animation) {
 
 						}
 
 						@Override
 						public void onAnimationEnd(Animation anima) {
 							mSlided = true;
 //							RelativeLayout.LayoutParams lp=new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT); 
 //							lp.rightMargin = TRANSLATE_ANIMATION_WIDTH; 
 //							//lp.topMargin = top;
 //							int[] pos={mSettingBtn.getLeft(),mSettingBtn.getTop(),mSettingBtn.getRight(),mSettingBtn.getBottom()};
 //							mSettingBtn.setLayoutParams(lp);
 							/*final int left = mSettingBtn.getLeft();
 				            final int top = mSettingBtn.getTop();
 				            final int right = mSettingBtn.getRight();
 				            final int bottom = mSettingBtn.getBottom();
 				            mSettingBtn.layout(left, top , right, bottom);
 				            System.out.println("left: "+(left)+";top:"+ top+";right:"+ (right)+";bottom:"+  bottom);*/
 							
 							mMainFrameMaskLayout.setVisibility(View.VISIBLE);
 							mMainFrameMaskLayout.setOnTouchListener(mOnTouchListener);
 							mSettingBtn.setVisibility(View.GONE);
 						}
 					});
 		}
 
 		/**
 		 * 
 		 */
 		private void slideIn() {
 			TranslateAnimation translate = new TranslateAnimation(0, TRANSLATE_ANIMATION_WIDTH, 0, 0);
 			translate.setDuration(ANIMATION_DURATION_FAST);
 			// ʱͣڽλ
 			translate.setFillAfter(true);
 			mSettingFrameLayout.startAnimation(translate);
 			mSettingFrameLayout.getAnimation().setAnimationListener(
 					new Animation.AnimationListener() {
 
 						@Override
 						public void onAnimationStart(Animation animation) {
 							mSettingFrameLayout.setVisibility(View.GONE);
 							TranslateAnimation mainAnimation = new TranslateAnimation(
 									-TRANSLATE_ANIMATION_WIDTH, 0, 0, 0);
 							mainAnimation.setDuration(ANIMATION_DURATION_SLOW);
 							mainAnimation.setFillAfter(true);
 							mMainFrameLayout.startAnimation(mainAnimation);
 							
 						}
 
 						@Override
 						public void onAnimationRepeat(Animation animation) {
 
 						}
 
 						@Override
 						public void onAnimationEnd(Animation animation) {
 							mSlided = false;
 
 							mMainFrameMaskLayout.setVisibility(View.GONE);
 							mMainFrameMaskLayout.setOnTouchListener(null);
 							mSettingBtn.setVisibility(View.VISIBLE);
 							myViewPager.setFocusable(true);
 						}
 					});
 
 		}
 		
 		@Override
 		public boolean onKeyDown(int keyCode, KeyEvent event) {
 			if (keyCode == KeyEvent.KEYCODE_BACK && mSlided) {
 				slideIn();
 				return true;
 			}
 			return super.onKeyDown(keyCode, event);
 		}
 }
