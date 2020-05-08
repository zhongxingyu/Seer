 package com.example.custom_views;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.PointF;
 import android.graphics.Paint.Style;
 import android.graphics.Rect;
 import android.util.AttributeSet;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.LinearLayout;
 
 /**
  * This is a custom view for displaying a bitmap and a moveable rectangle over it.
  * It makes it easy to crop images.
  * @author benrudolph
  *
  */
 public class CropView extends View {
 	Paint mPaint;
 	
 	/* Top left and bottom right corner of crop box */
 	private PointF mTopLeft;
 	private PointF mBtmRight;
 	
 	private Rect mBoundingBox;
 	private static final int MARGIN = 20;
 	
 	private int mWidth;
 	private int mHeight;
 	
 	private float mRatio;
 	
 	/* Length of crop square */
 	private float mLength;
 	
 	private Bitmap mCurrBitmap;
 	
 	private boolean mFirstRender;
 	public enum Direction {
 		X,
 		Y
 	}
 	
 	public CropView(Context context, AttributeSet attrs) {
 		super(context, attrs);
 		mPaint = new Paint();
 		mCurrBitmap = null;
 		
 		mTopLeft = new PointF();
 		mBtmRight = new PointF();
 		
 		mBoundingBox = new Rect();
 		
 		mFirstRender = true;
 	
 	}
 	
 	/**
 	 * Sets the current bitmap to be cropped and sets the length of the crop box to
 	 * be the height of image.
 	 * @param bitmap New bitmap
 	 */
 	public void setBitmap(Bitmap bitmap) {
 		mCurrBitmap = bitmap;
 		mFirstRender = true;
 		invalidate();
 	}
 	
 	@Override
 	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
 		//super.onMeasure(width, height);
 		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		int height = mCurrBitmap == null ? 480 : mCurrBitmap.getHeight();
	    int width = MeasureSpec.getSize(widthMeasureSpec);
	    
		setMeasuredDimension(width, height);

 	}
 
 	protected void onDraw(Canvas canvas) {
 		
 		if (mCurrBitmap != null) {
 			
 			if (mFirstRender) {				
 				int bBoxWidth = canvas.getWidth() - 2*MARGIN;
 				mRatio = bBoxWidth / (float) mCurrBitmap.getWidth();
 				int bBoxHeight = (int) (mRatio * mCurrBitmap.getHeight());
 				
 				mBoundingBox.set(MARGIN, 0, MARGIN + bBoxWidth, bBoxHeight);
 
 				mLength = mBoundingBox.width() > mBoundingBox.height() ? mBoundingBox.height() : mBoundingBox.width();
 
 				mTopLeft.set(mBoundingBox.exactCenterX() - mLength / 2, mBoundingBox.exactCenterY() - mLength / 2);
 				mBtmRight.set(mBoundingBox.exactCenterX() + mLength / 2, mBoundingBox.exactCenterY() + mLength / 2);
 				
 				
 	
 				mFirstRender = false;
 			}
 			
 			canvas.drawARGB(50, 255, 255, 255);
 			mPaint.setColor(Color.BLACK);
 			mPaint.setStyle(Style.STROKE);
 			mPaint.setStrokeWidth(3.0f);
 			
 			canvas.drawBitmap(mCurrBitmap, null, mBoundingBox, null);
 			
 			canvas.drawRect(mTopLeft.x, mTopLeft.y, mBtmRight.x, mBtmRight.y, mPaint);
 		}
 		invalidate();
 		
 	}
 	
 	/**
 	 * Determines if the movement of the rectangle is valid given a direction and amount to move
 	 * @param dir The direction of movement
 	 * @param delta The distance the rect will move
 	 * @return
 	 */
 	private boolean isValidMove(Direction dir, float delta) {
 		switch (dir) {
 		case X:
 			return mTopLeft.x + delta > mBoundingBox.left && mTopLeft.x + delta < mBoundingBox.right &&
 					mBtmRight.x + delta > mBoundingBox.left && mBtmRight.x + delta < mBoundingBox.right;						
 		case Y:
 			return mTopLeft.y + delta > mBoundingBox.top && mTopLeft.y + delta < mBoundingBox.bottom &&
 					mBtmRight.y + delta >mBoundingBox.top && mBtmRight.y + delta < mBoundingBox.bottom;				
 		default:
 			return false;
 		}
 		
 	}
 	
 	/**
 	 * If possible, moves rect delta distance from current position
 	 * @param deltaX distance to move in X direction
 	 * @param deltaY distance to move in Y direction
 	 */
 	public void moveRect(float deltaX, float deltaY) {
 		
 		if (isValidMove(Direction.X, deltaX)) {
 			mTopLeft.x += deltaX;
 			mBtmRight.x += deltaX;
 		}
 		if (isValidMove(Direction.Y, deltaY)) {
 			mTopLeft.y += deltaY;
 			mBtmRight.y += deltaY;
 		}
 			
 	
 	}
 	/**
 	 * Returns cropped bitmap based on crop box
 	 * @return
 	 */
 	public Bitmap crop() {
		int x = (int) (mTopLeft.x / mRatio);
		int y = (int) (mTopLeft.y / mRatio);
		int length = (int) (mLength / mRatio);
		
		/* Take care of rounding errors */
		length = x + length > mCurrBitmap.getWidth() ? mCurrBitmap.getWidth() - x : length;
		length = y + length > mCurrBitmap.getHeight() ? mCurrBitmap.getHeight() - y : length;

		return Bitmap.createBitmap(mCurrBitmap, x, y, length, length);
 	}
 	
 }
