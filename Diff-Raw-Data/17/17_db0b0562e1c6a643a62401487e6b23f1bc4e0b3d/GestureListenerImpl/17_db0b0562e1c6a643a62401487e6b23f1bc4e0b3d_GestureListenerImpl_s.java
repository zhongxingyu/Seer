 package com.shj00007.touch.ctrl;
 
 import android.app.Activity;
 import android.util.DisplayMetrics;
 import android.view.GestureDetector.OnGestureListener;
 import android.view.MotionEvent;
 import android.view.animation.Animation;
 import android.view.animation.Animation.AnimationListener;
 import android.view.animation.AnimationUtils;
 import android.widget.FrameLayout;
 import android.widget.LinearLayout;
 
 import com.shj00007.R;
 
 public class GestureListenerImpl implements OnGestureListener {
 
 	private LinearLayout mHomeLayout = null;
 	private DisplayMetrics dm = null;
 	private boolean ishomeopen = true;
 	private Animation open_layout_anim = null;
 	private Animation close_layout_anim;
 	private Activity mMainActivity = null;
 
 	private FrameLayout.LayoutParams mHomeParms = null;
 
 	public GestureListenerImpl(Activity pActivity, LinearLayout pLayout,
 			DisplayMetrics dm) {
 		this.mMainActivity = pActivity;
 		this.mHomeLayout = pLayout;
		
 		this.dm = dm;
 		this.mHomeParms = (FrameLayout.LayoutParams) mHomeLayout
 				.getLayoutParams();
 	}
 
 	@Override
 	public boolean onDown(MotionEvent e) {
 		// TODO Auto-generated method stub
 		if (e.getRawX() > dm.widthPixels * 0.7 && ishomeopen) {
			setAnimation();
 			mHomeLayout.startAnimation(open_layout_anim);
 			ishomeopen = false;
 		}
 		return false;
 	}
 
 	@Override
 	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
 			float velocityY) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public void onLongPress(MotionEvent e) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
 			float distanceY) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public void onShowPress(MotionEvent e) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public boolean onSingleTapUp(MotionEvent e) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	public void setAnimation() {
 		open_layout_anim = AnimationUtils.loadAnimation(mMainActivity,
 				R.anim.open_layout);
 		open_layout_anim.setAnimationListener(new AnimationListener() {
 
 			@Override
 			public void onAnimationStart(Animation animation) {
 				// TODO Auto-generated method stub
 
 			}
 
 			@Override
 			public void onAnimationRepeat(Animation animation) {
 				// TODO Auto-generated method stub
 
 			}
 
 			@Override
 			public void onAnimationEnd(Animation animation) {
 				// TODO Auto-generated method stub
 				mHomeParms.leftMargin = -(int) (dm.widthPixels * 0.3);
 				mHomeLayout.setLayoutParams(mHomeParms);
 				mHomeLayout.clearAnimation();
 			}
 		});
 		close_layout_anim = AnimationUtils.loadAnimation(mMainActivity,
 				R.anim.close_layout);
 		close_layout_anim.setAnimationListener(new AnimationListener() {
 
 			@Override
 			public void onAnimationStart(Animation animation) {
 				// TODO Auto-generated method stub
 
 			}
 
 			@Override
 			public void onAnimationRepeat(Animation animation) {
 				// TODO Auto-generated method stub
 
 			}
 
 			@Override
 			public void onAnimationEnd(Animation animation) {
 				// TODO Auto-generated method stub
 				mHomeParms.leftMargin = 0;
 				mHomeLayout.setLayoutParams(mHomeParms);
 				mHomeLayout.clearAnimation();
 			}
 		});
 	}
 
 }
