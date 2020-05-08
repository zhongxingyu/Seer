 package com.additt.custom_views;
 
 import java.util.ArrayList;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Matrix;
 import android.graphics.Paint;
 import android.graphics.PointF;
 import android.graphics.Paint.Style;
 import android.graphics.Rect;
 import android.graphics.RectF;
 import android.util.AttributeSet;
 import android.view.Display;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.View.MeasureSpec;
 import android.view.WindowManager;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 
 /**
  * This is a custom view for displaying a bitmap and a moveable rectangle over it.
  * It makes it easy to crop images.
  * @author benrudolph
  *
  */
 public class CropView extends ImageView {
 	private Paint mPaint;
 	
 	/* Top left and bottom right corner of crop box */
 	
 	private RectF mBoundingBox;
 	private RectF mCropBox;
 	
 	/* The types of crop drags */
 	private enum DragState {
 		RESIZE,
 		MOVE, 
 		NONE
 	}
 	
 	/* Quadrant the drag started in */
 	private enum DragQuadrant {
 		TOP_LEFT,
 		TOP_RIGHT,
 		BOTTOM_LEFT,
 		BOTTOM_RIGHT,
 	}
 	private DragState mDragState;
 	private DragQuadrant mDragQuadrant;
 
 
 	private static final int MARGIN = 20;
 	private static final int ALPHA = 160;
 	private static final int RADIUS = 20;
 	private static final int EDGE_THRESHOLD = 30;
 	private static final float MIN_LENGTH_PERCENT = .2f;
 	
 	
 	private Float mRatio;
 	
 	/* Length of crop square */
 	private float mLength;
 	
 	/* Min length of crop box */
 	private float mMinLength;
 	
 	
 	private Bitmap mCurrBitmap;
 	
 	private boolean mFirstRender;
 	public enum Direction {
 		X,
 		Y
 	}
 	
 	/* The list of 4 rects that provide the background shadow */
 	private ArrayList<Rect> mShadowRects;
 	
 	public CropView(Context context, AttributeSet attrs) {
 		super(context, attrs);
 		
 		mPaint = new Paint();
 		mCurrBitmap = null;
		
 
 		mBoundingBox = new RectF();
 		mCropBox = new RectF();
 
 		mShadowRects = new ArrayList<Rect>();
 		
 		for (int i = 0; i < 4; i++) {
 			mShadowRects.add(new Rect());
 		}
 		
 		
 		mFirstRender = true;
 	
 		mDragState = DragState.NONE;
 		mDragQuadrant = DragQuadrant.TOP_LEFT;
 		
 		mMinLength = 0;
 		
 		mRatio = null;
 		 
 		WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
 		Display display = wm.getDefaultDisplay();
 		
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
 		int desiredWidth = mCurrBitmap != null && mBoundingBox != null ? (int) (mBoundingBox.width() + 2*MARGIN) : 0;
 	    int desiredHeight = mCurrBitmap != null && mBoundingBox != null ? (int) (mBoundingBox.height() + 2*MARGIN) : 0;
 
 	    int widthMode = MeasureSpec.getMode(widthMeasureSpec);
 	    int widthSize = MeasureSpec.getSize(widthMeasureSpec);
 	    int heightMode = MeasureSpec.getMode(heightMeasureSpec);
 	    int heightSize = MeasureSpec.getSize(heightMeasureSpec);
 
 	    int width;
 	    int height;
 
 	    //Measure Width
 	    if (widthMode == MeasureSpec.EXACTLY) {
 	        //Must be this size
 	        width = widthSize;
 	    } else if (widthMode == MeasureSpec.AT_MOST) {
 	        //Can't be bigger than...
 	        width = Math.min(desiredWidth, widthSize);
 	    } else {
 	        //Be whatever you want
 	        width = desiredWidth;
 	    }
 
 	    //Measure Height
 	    if (heightMode == MeasureSpec.EXACTLY) {
 	        //Must be this size
 	        height = heightSize;
 	    } else if (heightMode == MeasureSpec.AT_MOST) {
 	        //Can't be bigger than...
 	        height = Math.min(desiredHeight, heightSize);
 	    } else {
 	        height = desiredHeight;
 	    }
 	    
 	    setMeasuredDimension(width, height);
 	}
 
 	protected void onDraw(Canvas canvas) {
 		
 		if (mCurrBitmap != null) {
 			
 			if (mFirstRender) {
				int bBoxWidth = canvas.getWidth() - 2*MARGIN;
				int bBoxHeight = (int) ((canvas.getHeight() / 2) - 2 * MARGIN);
 				
 				float bBoxAspectRatio = (float) bBoxWidth / bBoxHeight;
 				float adAspectRatio = (float) mCurrBitmap.getWidth() / mCurrBitmap.getHeight();
 				
 				// Width in relation to height is larger
 				if (adAspectRatio > bBoxAspectRatio) {
 					
 					// Ratio to match the bitmap width of the screen
 					mRatio = ((float) bBoxWidth / mCurrBitmap.getWidth());
 					bBoxHeight = (int) (mRatio * mCurrBitmap.getHeight());
 				} else {
 					// Ratio to match the bitmap with the height of the screen
 					mRatio = ((float) bBoxHeight / mCurrBitmap.getHeight());
 					bBoxWidth = (int) (mRatio * mCurrBitmap.getWidth());
 				}
 				
 				mBoundingBox.set(MARGIN, MARGIN, MARGIN + bBoxWidth, MARGIN + bBoxHeight);
 
 				mLength = mBoundingBox.width() > mBoundingBox.height() ? mBoundingBox.height() : mBoundingBox.width();
 				mMinLength = mLength * MIN_LENGTH_PERCENT;
 				
 				mCropBox.set(mBoundingBox.centerX() - mLength / 2,
 							 mBoundingBox.centerY() - mLength / 2, 
 							 mBoundingBox.centerX() + mLength / 2,
 							 mBoundingBox.centerY() + mLength / 2);
 				
 				
 	
 				mFirstRender = false;
 				requestLayout();
 
 			}
 			
 			canvas.drawARGB(50, 255, 255, 255);
 			mPaint.setColor(Color.BLACK);
 			mPaint.setStyle(Style.STROKE);
 			mPaint.setStrokeWidth(3.0f);
 			
 			canvas.drawBitmap(mCurrBitmap, null, mBoundingBox, null);
 			
 			canvas.drawRect(mCropBox, mPaint);
 			
 			mPaint.setStyle(Style.FILL);
 			mPaint.setAlpha(ALPHA);
 			
 			/* Draw shadows */
 			canvas.drawRect(mBoundingBox.left, mBoundingBox.top, mCropBox.left, mBoundingBox.bottom, mPaint);
 			canvas.drawRect(mCropBox.left, mBoundingBox.top, mCropBox.right, mCropBox.top, mPaint);
 			canvas.drawRect(mCropBox.right, mBoundingBox.top, mBoundingBox.right, mBoundingBox.bottom, mPaint);
 			canvas.drawRect(mCropBox.left, mCropBox.bottom, mCropBox.right, mBoundingBox.bottom, mPaint);
 			/* End drawing shadows */
 			
 			mPaint.setARGB(255, 190, 190, 190);
 			/* Draw circle handles */
 			canvas.drawCircle(mCropBox.left, mCropBox.top,  RADIUS, mPaint);
 			canvas.drawCircle(mCropBox.right, mCropBox.top,RADIUS, mPaint);
 			canvas.drawCircle(mCropBox.left, mCropBox.bottom, RADIUS, mPaint);
 			canvas.drawCircle(mCropBox.right, mCropBox.bottom, RADIUS, mPaint);
 			/* End handles */
 
 
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
 			return mCropBox.left + delta > mBoundingBox.left && mCropBox.left + delta < mBoundingBox.right &&
 					mCropBox.right + delta > mBoundingBox.left && mCropBox.right + delta < mBoundingBox.right;						
 		case Y:
 			return mCropBox.top + delta > mBoundingBox.top && mCropBox.top + delta < mBoundingBox.bottom &&
 					mCropBox.bottom + delta >mBoundingBox.top && mCropBox.bottom + delta < mBoundingBox.bottom;				
 		default:
 			return false;
 		}
 		
 	}
 	
 	/**
 	 * If possible, moves rect delta distance from current position
 	 * @param deltaX distance to move in X direction
 	 * @param deltaY distance to move in Y direction
 	 */
 	private void moveCropBox(float deltaX, float deltaY) {
 		
 		if (isValidMove(Direction.X, deltaX)) {
 			mCropBox.offset(deltaX, 0);
 		}
 		if (isValidMove(Direction.Y, deltaY)) {
 			mCropBox.offset(0, deltaY);
 		}
 			
 	
 	}
 	/**
 	 * Returns cropped bitmap based on crop box
 	 * @return
 	 */
 	public Bitmap crop() {
 		int x = (int) ((mCropBox.left - MARGIN) / mRatio);
 		int y = (int) ((mCropBox.top - MARGIN) / mRatio);
 		int length = (int) (mCropBox.width() / mRatio);
 		
 		/* Take care of rounding errors */
 		length = x + length > mCurrBitmap.getWidth() ? mCurrBitmap.getWidth() - x : length;
 		length = y + length > mCurrBitmap.getHeight() ? mCurrBitmap.getHeight() - y : length;
 
 		return Bitmap.createBitmap(mCurrBitmap, x, y, length, length);
 	}
 	
 	/**
 	 * Determines if the resize of the crop box is valid. Does it go outside the picture? Is it too small?
 	 * @param delta The amount we are going to resize the box
 	 * @return
 	 */
 	private boolean isValidResize(float delta) {
 		if ((int) Math.floor(mCropBox.left + delta) > (int) Math.floor(mCropBox.right - mMinLength) && delta > 0) {
 			return false;
 		} else if (mCropBox.left + delta < mBoundingBox.left) {
 			return false;
 		} else if (mCropBox.right - delta > mBoundingBox.right) {
 			return false;
 		} else if (mCropBox.top + delta < mBoundingBox.top) {
 			return false;
 		} else if (mCropBox.bottom - delta > mBoundingBox.bottom) {
 			return false;
 		}
 		
 		return true;
 		
 		
 	} 
 	
 	/**
 	 * This will resize the crop box uniformly. Takes the largest delta and then moves the box inward
 	 * or outward depending on a variety of factors. The logic is a bit verbose but works logically to
 	 * the user.
 	 * @param deltaX
 	 * @param deltaY
 	 */
 	private void resizeCropBox(float deltaX, float deltaY) {		
 		
 		Direction dominantDirection = Math.abs(deltaX) > Math.abs(deltaY) ? Direction.X : Direction.Y;
 		float delta = Math.abs(deltaX) > Math.abs(deltaY) ? deltaX : deltaY;
 		
 		switch (mDragQuadrant) {
 		case TOP_LEFT:
 			break;
 		case TOP_RIGHT:
 			if (dominantDirection == Direction.X) {
 				delta = -delta;
 			}
 			break;
 		case BOTTOM_LEFT:
 			if (dominantDirection == Direction.Y) {
 				delta = -delta;
 			}
 			break;
 		case BOTTOM_RIGHT:
 			delta = -delta;
 			break;
 		}
 		
 		if (isValidResize(delta)) {
 			mCropBox.inset(delta, delta);
 		}
 	}
 
 	/**
 	 * Determines if a point x,y is near the edge of the cropBox by drawing 2 rectangles.
 	 * If it's inside the outer and outside the inner than we are near the edge.
 	 * @param x
 	 * @param y
 	 * @return
 	 */
 	private boolean isNearEdge(float x, float y) {
 		RectF outer = new RectF(mCropBox);
 		RectF inner = new RectF(mCropBox);
 		
 		outer.inset(-EDGE_THRESHOLD, -EDGE_THRESHOLD);
 		inner.inset(EDGE_THRESHOLD, EDGE_THRESHOLD);
 		
 		return outer.contains(x, y) && !inner.contains(x, y);
 	}
 	
 	/**
 	 * Determines what quadrant the drag started in.
 	 * @param x
 	 * @param y
 	 */
 	private void setDragQuadrant(float x, float y) {
 		float centerX = mCropBox.centerX();
 		float centerY = mCropBox.centerY();
 		if (x < centerX && y < centerY) {
 			mDragQuadrant = DragQuadrant.TOP_LEFT;
 		} else if (x > centerX && y < centerY) {
 			mDragQuadrant = DragQuadrant.TOP_RIGHT;
 		} else if (x < centerX && y > centerY){
 			mDragQuadrant = DragQuadrant.BOTTOM_LEFT;
 		} else if (x > centerX && y > centerY) {
 			mDragQuadrant = DragQuadrant.BOTTOM_RIGHT;
 		}
 	}
 	
 	/**
 	 * Based on the action event and x,y values we determine the drag state.
 	 * @param x
 	 * @param y
 	 * @param action
 	 */
 	public void setDragState(float x, float y, int action) {
 		
 		if (action == MotionEvent.ACTION_DOWN && isNearEdge(x,y)) {
 			mDragState = DragState.RESIZE;
 			setDragQuadrant(x, y);
 		} else if (action == MotionEvent.ACTION_DOWN) {
 			mDragState = DragState.MOVE;
 		} else if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
 			mDragState = DragState.NONE;
 		}
 	}
 
 	/**
 	 * Either move the cropbox or resize it.
 	 * @param deltaX
 	 * @param deltaY
 	 */
 	public void update(float deltaX, float deltaY) {
 		switch (mDragState) {
 		case RESIZE:
 			resizeCropBox(deltaX, deltaY);
 			break;
 		case MOVE:
 			moveCropBox(deltaX, deltaY);
 			break;
 		}
 		
 	}
 
 	public void rotate() {
 		Matrix m = new Matrix();
 		m.postRotate(90);
 		Bitmap rotated = Bitmap.createBitmap(mCurrBitmap, 0, 0, 
                 mCurrBitmap.getWidth(), mCurrBitmap.getHeight(), 
                 m, true);
 		
 		setBitmap(rotated);
 		
 	}
 	
 	
 	
 }
