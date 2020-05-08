 package com.app.getconnected.animations;
 
 import android.view.View;
 import android.view.ViewGroup.LayoutParams;
 import android.view.animation.AccelerateInterpolator;
 import android.view.animation.Animation;
 
 public class CollapseAnimation extends Animation implements Animation.AnimationListener {
 
 	private View view;
 	private static int ANIMATION_DURATION;
 	private int ToHeight;
 	private int LastHeight;
 
 	/**
 	 * Constructor
 	 * @param v
 	 * @param FromHeighth
 	 * @param ToHeight
 	 * @param Duration
 	 */
 	public CollapseAnimation(View v, int FromHeighth, int ToHeight, int Duration) {
 		
 		this.view = v;
 		LayoutParams lyp =  view.getLayoutParams();
 		ANIMATION_DURATION = 1;
 		this.ToHeight = lyp.height;
 		setDuration(ANIMATION_DURATION);
 		setRepeatCount(20);
 		setFillAfter(false);
 		setInterpolator(new AccelerateInterpolator());
 		setAnimationListener(this);
 	}
 
 	@Override
 	public void onAnimationEnd(Animation animation) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void onAnimationRepeat(Animation animation) {
 		// TODO Auto-generated method stub
 		LayoutParams lyp =  view.getLayoutParams();
 		lyp.height = lyp.height - ToHeight/20;
 		view.setLayoutParams(lyp);
 	}
 
 	@Override
 	public void onAnimationStart(Animation animation) {
 		// TODO Auto-generated method stub
 		LayoutParams lyp =  view.getLayoutParams();
 		LastHeight = lyp.height;
 	}
 
 }
