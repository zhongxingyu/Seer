 /*
  * TouchImageView.java
  * By: Michael Ortiz
  * Updated By: Patrick Lackemacher
  * -------------------
  * Extends Android ImageView to include pinch zooming and panning.
  */
 
 package com.example.touch;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.Matrix;
 import android.graphics.PointF;
 import android.util.AttributeSet;
 import android.view.MotionEvent;
 import android.view.ScaleGestureDetector;
 import android.view.View;
 import android.widget.ImageView;
 
 public class TouchImageView extends ImageView {
 
 	Matrix matrix = new Matrix();
 
 	// We can be in one of these 3 states
 	static final int NONE = 0;
 	static final int DRAG = 1;
 	static final int ZOOM = 2;
 	int mode = NONE;
 
 	// Remember some things for zooming
 	PointF last = new PointF();
 	PointF start = new PointF();
 	float minScale = 0.5f;
 	float maxScale = 4.0f;
 	float[] m;
 
 	float redundantXSpace, redundantYSpace;
 
 	float width, height;
 	static final int CLICK = 3;
 	float saveScale = 1f;
 	float right, bottom, origWidth, origHeight, bmWidth, bmHeight;
 
 	ScaleGestureDetector mScaleDetector;
 
 	Context context;
 
 	public TouchImageView(Context context) {
 		super(context);
 		sharedConstructing(context);
 	}
 
 	public TouchImageView(Context context, AttributeSet attrs) {
 		super(context, attrs);
 		sharedConstructing(context);
 	}
 	
 	private float clamp(float value, float min, float max) {
 		return Math.min(Math.max(min, value), max);
 	}
 
 	private boolean scaleChanged = false;
 	
 	private void sharedConstructing(Context context) {
 		super.setClickable(true);
 		this.context = context;
 		mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
 		m = new float[9];
 		setImageMatrix(matrix);
 		setScaleType(ScaleType.MATRIX);
 		
 		TouchImageView.this.setAlpha(128);
 
 		setOnTouchListener(new OnTouchListener() {
 			
 			private float alpha = 0.5f;
 			private float newAlpha = 0.5f;
 
 			@Override
 			public boolean onTouch(View v, MotionEvent event) {
 				scaleChanged = false;
 				mScaleDetector.onTouchEvent(event);
 
 				matrix.getValues(m);
 				float x = m[Matrix.MTRANS_X];
 				float y = m[Matrix.MTRANS_Y];
 				PointF curr = new PointF(event.getX(), event.getY());
 
 				switch (event.getAction()) {
 				case MotionEvent.ACTION_DOWN:
 					last.set(event.getX(), event.getY());
 					start.set(last);
 					mode = DRAG;
 					break;
 				case MotionEvent.ACTION_MOVE:
 					if (mode == DRAG && !scaleChanged) {
 						float startVal = (start.y / (float)(v.getHeight()));
 						float curVal = (event.getY() / (float)(v.getHeight()));
 						float diff = curVal - startVal;
 						newAlpha = alpha + diff;
 						newAlpha = clamp(newAlpha, 0.0f, 1.0f);
//						System.out.println("alpha=" + alpha + " newAlpha=" + newAlpha + " startVal=" + startVal + " curVal=" + curVal);
 						//clamp
 						TouchImageView.this.setAlpha((int)(clamp(newAlpha*255.0f, 0.0f, 255.0f)));
 					}
 					break;
 
 				case MotionEvent.ACTION_UP:
 					mode = NONE;
 					alpha = newAlpha;
 					int xDiff = (int) Math.abs(curr.x - start.x);
 					int yDiff = (int) Math.abs(curr.y - start.y);
 					if (xDiff < CLICK && yDiff < CLICK)
 						performClick();
 					break;
 
 				case MotionEvent.ACTION_POINTER_UP:
 					mode = NONE;
 					break;
 				}
 				setImageMatrix(matrix);
 				invalidate();
 				return true; // indicate event was handled
 			}
 
 		});
 	}
 
 	@Override
 	public void setImageBitmap(Bitmap bm) {
 		super.setImageBitmap(bm);
 		if (bm != null) {
 			bmWidth = bm.getWidth();
 			bmHeight = bm.getHeight();
 		}
 		// animate to 0.5f, 10fps
 		final int[] counter = {0};
 		TouchImageView.this.setAlpha(0);
 		postDelayed(new Runnable() {
 			@Override
 			public void run() {
 				TouchImageView.this.setAlpha(Math.min(12 * (counter[0] + 1), 128));
 				counter[0]++;
 				if (counter[0] < 10) {
 					postDelayed(this, 50);
 				}
 			}
 		}, 50);
 	}
 
 	public void setMaxZoom(float x) {
 		maxScale = x;
 	}
 	
 	private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
 		@Override
 		public boolean onScale(ScaleGestureDetector detector) {
 			float mScaleFactor = detector.getScaleFactor();
 			float origScale = saveScale;
 			saveScale *= mScaleFactor;
 			
 			if (saveScale > maxScale) {
 				saveScale = maxScale;
 				mScaleFactor = maxScale / origScale;
 			} else if (saveScale < minScale) {
 				saveScale = minScale;
 				mScaleFactor = minScale / origScale;
 			}
 			matrix.postScale(mScaleFactor, mScaleFactor, width / 2, height / 2);
 			scaleChanged = true;
 			return true;
 		}
 	}
 
 	@Override
 	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
 		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
 		width = MeasureSpec.getSize(widthMeasureSpec);
 		height = MeasureSpec.getSize(heightMeasureSpec);
 		// Fit to screen.
 		float scale;
 		float scaleX = (float) width / (float) bmWidth;
 		float scaleY = (float) height / (float) bmHeight;
 		scale = Math.min(scaleX, scaleY);
 		matrix.setScale(scale, scale);
 		setImageMatrix(matrix);
 		saveScale = 1f;
 
 		// Center the image
 		redundantYSpace = (float) height - (scale * (float) bmHeight);
 		redundantXSpace = (float) width - (scale * (float) bmWidth);
 		redundantYSpace /= (float) 2;
 		redundantXSpace /= (float) 2;
 
 		System.out.println("redundantXSpace: " + redundantXSpace + ", redundantYspace: " + redundantYSpace);
 		matrix.postTranslate(redundantXSpace, redundantYSpace);
 
 		origWidth = width - 2 * redundantXSpace;
 		origHeight = height - 2 * redundantYSpace;
 		right = width * saveScale - width - (2 * redundantXSpace * saveScale);
 		bottom = height * saveScale - height
 				- (2 * redundantYSpace * saveScale);
 		setImageMatrix(matrix);
 	}
 }
