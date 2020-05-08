 package com.digiflare.ces2013.ui;
 
 import android.content.Context;
 import android.support.v4.view.ViewPager;
 import android.util.AttributeSet;
 import android.view.MotionEvent;
 
 public class UninterceptableViewPager extends ViewPager {
 	
 	private boolean mDown;
 	private boolean mMoved;
 
 	public UninterceptableViewPager(Context context, AttributeSet attrs) {
 		super(context, attrs);
 	}
 
 	@Override
 	public boolean onInterceptTouchEvent(MotionEvent ev) {
 		//Log.i("===== on intercept =====", Integer.toString(ev.getActionMasked()));
 		
 		getParent().requestDisallowInterceptTouchEvent(true);
 		return true;
 	}
 	
 	@Override
 	public boolean onTouchEvent(MotionEvent ev) {
 		//Log.i("===== on touch =====", Integer.toString(ev.getActionMasked()));
 		
 		int action = ev.getActionMasked();
 		switch(action) {
 			case MotionEvent.ACTION_DOWN:
 				mDown = true;
 				break;
 			case MotionEvent.ACTION_MOVE:
 				mMoved = true;
 				break;
 			case MotionEvent.ACTION_UP:
 				if(mDown && !mMoved) {
					callOnClick();
 				}
 				mDown = false;
 				mMoved = false;
 				break;
 		}
 		return super.onTouchEvent(ev);
 	}
 }
