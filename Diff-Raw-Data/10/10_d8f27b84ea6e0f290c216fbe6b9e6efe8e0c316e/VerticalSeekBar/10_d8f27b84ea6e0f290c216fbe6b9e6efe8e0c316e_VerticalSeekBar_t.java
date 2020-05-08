 package org.quadcopter.controller.util;
 
 import android.content.Context;
 import android.graphics.Canvas;
 import android.util.AttributeSet;
 import android.view.MotionEvent;
 import android.widget.SeekBar;
 
 public class VerticalSeekBar extends SeekBar {
 	private OnSeekBarChangeListener mOnVerticalSeekBarChangeListerner;
 	
 	public void setOnVerticalSeekBarChangeListener(OnSeekBarChangeListener l) {
 		mOnVerticalSeekBarChangeListerner = l;
 		setOnSeekBarChangeListener(mOnVerticalSeekBarChangeListerner);
 	}
 
 	public VerticalSeekBar(Context context) {
 		super(context);
 	}
 
 	public VerticalSeekBar(Context context, AttributeSet attrs, int defStyle) {
 		super(context, attrs, defStyle);
 	}
 
 	public VerticalSeekBar(Context context, AttributeSet attrs) {
 		super(context, attrs);
 	}
 
 	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
 		super.onSizeChanged(h, w, oldh, oldw);
 	}
 
 	@Override
 	protected synchronized void onMeasure(int widthMeasureSpec,
 			int heightMeasureSpec) {
 		super.onMeasure(heightMeasureSpec, widthMeasureSpec);
 		setMeasuredDimension(getMeasuredHeight(), getMeasuredWidth());
 	}
 
 	protected void onDraw(Canvas c) {
 		c.rotate(-90);
 		c.translate(-getHeight(), 0);
 
 		super.onDraw(c);
 	}
	
	@Override
	public void setProgress(int p) {
		super.setProgress(p);
		onSizeChanged(getWidth(), getHeight(), 0, 0);
	}
 
 	private void trackTouchEvent(MotionEvent event) {
 		int i = 0;
 		i = getMax() - (int) (getMax() * event.getY() / getHeight());
		setProgress(i);		
 	}
 
 	@Override
 	public boolean onTouchEvent(MotionEvent event) {
 		if (!isEnabled()) {
 			return false;
 		}
 
 		switch (event.getAction()) {
 		case MotionEvent.ACTION_DOWN:
 			if (mOnVerticalSeekBarChangeListerner != null)
 				mOnVerticalSeekBarChangeListerner.onStartTrackingTouch(this);
 			trackTouchEvent(event);			
 			break;
 		case MotionEvent.ACTION_MOVE:
 			trackTouchEvent(event);
 			break;
 		case MotionEvent.ACTION_UP:
 			trackTouchEvent(event);
 			if (mOnVerticalSeekBarChangeListerner != null)
 				mOnVerticalSeekBarChangeListerner.onStopTrackingTouch(this);
 			break;
 
 		case MotionEvent.ACTION_CANCEL:
 			if (mOnVerticalSeekBarChangeListerner != null)
 				mOnVerticalSeekBarChangeListerner.onStopTrackingTouch(this);
 			break;
 		}
 		return true;
 	}
 }
