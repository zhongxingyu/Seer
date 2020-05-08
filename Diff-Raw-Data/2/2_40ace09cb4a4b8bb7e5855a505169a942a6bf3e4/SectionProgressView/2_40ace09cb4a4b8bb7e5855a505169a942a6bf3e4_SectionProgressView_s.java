 package com.quizz.core.widgets;
 
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.Rect;
 import android.graphics.drawable.NinePatchDrawable;
 import android.util.AttributeSet;
 import android.widget.ImageView;
 
 public class SectionProgressView extends ImageView {
 
 	private int mProgressRes = 0;
 	private NinePatchDrawable mProgressDrawable;
 	private float mProgressValue = 0;
 
 	private int mPaddingTop = 0;
 	private int mPaddingLeft = 0;
 	private int mPaddingRight = 0;
 	private int mPaddingBottom = 0;
 
 	private Rect mProgressBounds = new Rect();
 
 	public SectionProgressView(Context context, AttributeSet attrs, int defStyle) {
 		super(context, attrs, defStyle);
 	}
 
 	public SectionProgressView(Context context, AttributeSet attrs) {
 		super(context, attrs);
 	}
 
 	public SectionProgressView(Context context) {
 		super(context);
 	}
 
 	public void setProgressRes(int res) {
 		mProgressRes = res;
 		if (res > 0) {
 			mProgressDrawable = (NinePatchDrawable) getContext().getResources()
 					.getDrawable(mProgressRes);
 		}
 	}
 
 	public void setProgressValue(float value) {
 		mProgressValue = value;
 	}
 
 	public void setPaddingProgress(int left, int top, int right, int bottom) {
 		mPaddingLeft = left;
 		mPaddingTop = top;
 		mPaddingRight = right;
 		mPaddingBottom = bottom;
 	}
 
 	@Override
 	protected void onDraw(Canvas canvas) {
 		super.onDraw(canvas);
 
		if (mProgressBounds != null) {
 			mProgressBounds.left = mPaddingLeft;
 			mProgressBounds.top = mPaddingTop;
 			mProgressBounds.right = (int) ((getWidth() - mPaddingRight)
 					* mProgressValue / 100f);
 			mProgressBounds.bottom = getHeight() - mPaddingBottom;
 
 			mProgressDrawable.setBounds(mProgressBounds);
 			mProgressDrawable.draw(canvas);
 		}
 	}
 }
