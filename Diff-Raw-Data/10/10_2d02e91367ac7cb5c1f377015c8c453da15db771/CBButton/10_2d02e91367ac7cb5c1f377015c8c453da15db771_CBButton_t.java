 /**
  * @Title: CBButton.java
  * @Package: com.android.cb.view
  * @Author: Raiden Awkward<raiden.ht@gmail.com>
  * @Date: 2012-3-29
  */
 package com.android.cb.view;
 
 import android.content.Context;
 import android.graphics.drawable.Drawable;
 import android.util.AttributeSet;
 import android.view.MotionEvent;
 import android.widget.Button;
 
 /**
  * @author raiden
  *
  * @Description button with image, text and status of 'on' and 'of'
  */
 public class CBButton extends Button {
 
 	public final static int STATUS_UNCERTAIN = 0;
 	public final static int STATUS_OFF = 1;
 	public final static int STATUS_ON = 2;
 
 	private int mStatus = STATUS_UNCERTAIN;
 	Drawable mDrawableOn = null;
 	Drawable mDrawableOff = null;
 	Drawable mDrawableCurrent = null;
 
 	public CBButton(Context context, AttributeSet attrs, int defStyle) {
 		super(context, attrs, defStyle);
 		initButton();
 	}
 
 	public CBButton(Context context, AttributeSet attrs) {
 		super(context, attrs);
 		initButton();
 	}
 
 	public CBButton(Context context) {
 		super(context);
 		initButton();
 	}
 
 	public CBButton(Context context, Drawable drawableOn, Drawable drawableOff) {
 		super(context);
 
 		initButton();
 		setDrawableOn(drawableOn);
 		setDrawableOff(drawableOff);
 		turnOff();
 	}
 
 	public CBButton(Context context, int drawableResourceOn, int drawableResourceOff) {
 		super(context);
 
 		initButton();
 		setDrawableOn(drawableResourceOn);
 		setDrawableOff(drawableResourceOff);
 		turnOff();
 	}
 
 	private void initButton() {
 
 	}
 
 	public void turnOn() {
 		mDrawableCurrent = mDrawableOn;
 
 		if (mDrawableCurrent != null) {
 			mStatus = STATUS_ON;
 			this.setBackgroundDrawable(mDrawableCurrent);
 		}
 	}
 
 	public void turnOff() {
 		mDrawableCurrent = mDrawableOff;
 
 		if (mDrawableCurrent != null) {
 			mStatus = STATUS_OFF;
 			this.setBackgroundDrawable(mDrawableCurrent);
 		}
 	}
 
 	public int getStatus() {
 		return mStatus;
 	}
 
 	public Drawable getDrawableOn() {
 		return mDrawableOn;
 	}
 
 	public void setDrawableOn(Drawable drawable) {
 		this.mDrawableOn = drawable;
 	}
 
 	public void setDrawableOn(int id) {
 		this.mDrawableOn = getResources().getDrawable(id);
 	}
 
 	public Drawable getDrawableOff() {
 		return mDrawableOff;
 	}
 
 	public void setDrawableOff(Drawable drawable) {
 		this.mDrawableOff = drawable;
 	}
 
 	public void setDrawableOff(int id) {
 		this.mDrawableOff = getResources().getDrawable(id);
 	}
 
 	@Override
 	public boolean onTouchEvent(MotionEvent event) {
 		switch (event.getAction()) {
 		case MotionEvent.ACTION_DOWN:
 			if (mDrawableOn != null)
 				this.setBackgroundDrawable(mDrawableOn);
 			break;
 		case MotionEvent.ACTION_MOVE:
 			if (mDrawableCurrent != null && mDrawableOn != null) {
 				if (event.getX() < 0 || event.getX() > this.getWidth()
 						|| event.getY() < 0 || event.getY() > this.getHeight())
 					this.setBackgroundDrawable(mDrawableCurrent);
 				else
 					this.setBackgroundDrawable(mDrawableOn);
 			}
 			break;
 		case MotionEvent.ACTION_OUTSIDE:
 		case MotionEvent.ACTION_UP:
 			this.setBackgroundDrawable(mDrawableCurrent);
 			break;
		default:
			this.setBackgroundDrawable(mDrawableCurrent);
			break;
 		}
 		return super.onTouchEvent(event);
 	}
 
 }
