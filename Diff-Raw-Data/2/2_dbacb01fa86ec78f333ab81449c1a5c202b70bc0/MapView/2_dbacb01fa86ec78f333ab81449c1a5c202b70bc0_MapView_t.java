 package org.dyndns.pawitp.salayatrammap.map;
 
 import android.content.Context;
 import android.graphics.Matrix;
 import android.os.Build;
 import android.os.Bundle;
 import android.os.Parcelable;
 import android.util.AttributeSet;
 import android.view.GestureDetector;
 import android.view.MotionEvent;
 import android.view.ScaleGestureDetector;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ImageView;
 import android.widget.Scroller;
 
 public class MapView extends ImageView implements OnClickListener {
 	
 	private static final float DEFAULT_ZOOM = 0.8F;
 	private static final float MAX_ZOOM = 1.2F;
 	private static final float TRACKBALL_FACTOR = 10F;
 	
 	private static final String KEY_MATRIX = "matrix";
 	
 	private boolean mRestored = false;
 	private float[] mTmpValues = new float[9];
 	private Scroller mScroller = new Scroller(getContext());
 	private Zoomer mZoomer = new Zoomer();
 	private ScaleGestureDetector mScaleGestureDetector; // Cannot be instantiated here in order to support pre-froyo
 	
 	public MapView(Context context) {
 		super(context);
 		init();
 	}
 
 	public MapView(Context context, AttributeSet attrs, int defStyle) {
 		super(context, attrs, defStyle);
 		init();
 	}
 
 	public MapView(Context context, AttributeSet attrs) {
 		super(context, attrs);
 		init();
 	}
 	
 	public void init() {
 		setFocusable(true);
 		requestFocus();
 		setClickable(true);
 		setOnClickListener(this);
 		
 		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
 			mScaleGestureDetector = new ScaleGestureDetector(getContext(), new ScaleGestureDetector.SimpleOnScaleGestureListener() {
 
 				@Override
 				public boolean onScale(ScaleGestureDetector detector) {
 					Matrix matrix = new Matrix(getImageMatrix());
 					matrix.postScale(detector.getScaleFactor(), detector.getScaleFactor(), detector.getFocusX(), detector.getFocusY());
 					checkZoom(matrix);
 					checkEdges(matrix);
 					setImageMatrix(matrix);
 					return true;
 				}
 				
 			});
 		}
 	}
 	
 	@Override
 	protected void onRestoreInstanceState(Parcelable state) {
 		super.onRestoreInstanceState(BaseSavedState.EMPTY_STATE); // stupid check
 		
 		Bundle bundle = (Bundle) state;
 		float[] values = bundle.getFloatArray(KEY_MATRIX);
 		
 		Matrix matrix = new Matrix();
 		matrix.setValues(values);
 		
 		checkZoom(matrix);
 		checkEdges(matrix);
 		setImageMatrix(matrix);
		
		mRestored = true;
 	}
 
 	@Override
 	protected Parcelable onSaveInstanceState() {
 		super.onSaveInstanceState(); // stupid check
 		
 		Bundle state = new Bundle();
 		getImageMatrix().getValues(mTmpValues);
 		state.putFloatArray(KEY_MATRIX, mTmpValues);
 		return state;
 	}
 
 	@Override
 	protected void onLayout(boolean changed, int left, int top, int right,
 			int bottom) {
 		super.onLayout(changed, left, top, right, bottom);
 		
 		// Zoom the map out by default and center it
 		// Cannot be done in the constructor because the size of the view is not known yet
 		if (!mRestored) {
 			Matrix matrix = new Matrix();
 			float scale = findFullscreenScale();
 			matrix.setScale(scale, scale);
 			
 			float width = -(getDrawable().getIntrinsicWidth() * scale - getWidth()) / 2;
 			float height = -(getDrawable().getIntrinsicHeight() * scale - getHeight()) / 2;
 			matrix.postTranslate(width, height);
 			
 			setImageMatrix(matrix);
 		}
 	}
 
 	@Override
 	public boolean onTouchEvent(MotionEvent event) {
 		if (event.getPointerCount() == 1) {
 			return mGestureDetector.onTouchEvent(event);
 		}
 		else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
 			return mScaleGestureDetector.onTouchEvent(event);
 		}
 		else {
 			return false;
 		}
 	}
 	
 	@Override
 	public boolean onTrackballEvent(MotionEvent event) {
 		if (event.getX() != 0 || event.getY() != 0) {
 			getImageMatrix().getValues(mTmpValues);
 			mScroller.startScroll((int) mTmpValues[Matrix.MTRANS_X], (int) mTmpValues[Matrix.MTRANS_Y],
 								  (int) (-event.getX() * TRACKBALL_FACTOR), (int) (-event.getY() * TRACKBALL_FACTOR));
 			invalidate();
 			return true;
 		}
 		else {
 			return super.onTrackballEvent(event);
 		}
 		
 	}
 	
 	@Override
 	public void onClick(View v) {
 		Matrix matrix = new Matrix(getImageMatrix());
 		
 		matrix.getValues(mTmpValues);
 		
 		float scale;
 		if (mTmpValues[Matrix.MSCALE_X] < DEFAULT_ZOOM - 0.01F /* floating point inaccuracy */) { // scale x == scale y
 			scale = DEFAULT_ZOOM;
 		}
 		else {
 			scale = findFullscreenScale();
 		}
 		
 		mZoomer.zoomTo(mTmpValues[Matrix.MSCALE_X], scale, getWidth() / 2, getHeight() / 2);
 		
 		invalidate();	
 	}
 
 	@Override
 	public void computeScroll() {		
 		if (mScroller.computeScrollOffset()) {
 			Matrix matrix = new Matrix(getImageMatrix());
 			
 			matrix.getValues(mTmpValues);
 			
 			matrix.postTranslate(-mTmpValues[Matrix.MTRANS_X] + mScroller.getCurrX(),
 								 -mTmpValues[Matrix.MTRANS_Y] + mScroller.getCurrY());
 			
 			checkEdges(matrix);
 			setImageMatrix(matrix);
 			
 			invalidate();
 		}
 		
 		if (mZoomer.compute()) {
 			Matrix matrix = new Matrix(getImageMatrix());
 			
 			matrix.getValues(mTmpValues);
 			
 			float scale = mZoomer.getCurrScale() / mTmpValues[Matrix.MSCALE_X];
 			matrix.postScale(scale, scale, mZoomer.getPivotX(), mZoomer.getPivotY());
 			
 			checkEdges(matrix);
 			setImageMatrix(matrix);
 			
 			invalidate();
 		}
 	}
 	
 	GestureDetector mGestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
 
 		@Override
 		public boolean onDoubleTap(MotionEvent e) {
 			Matrix matrix = new Matrix(getImageMatrix());
 			
 			matrix.getValues(mTmpValues);
 			
 			float scale;
 			if (mTmpValues[Matrix.MSCALE_X] < DEFAULT_ZOOM - 0.01F /* floating point inaccuracy */) { // scale x == scale y
 				scale = DEFAULT_ZOOM;
 			}
 			else {
 				scale = findFullscreenScale();
 			}
 			
 			mZoomer.zoomTo(mTmpValues[Matrix.MSCALE_X], scale, e.getX(), e.getY());
 			
 			invalidate();
 			
 			return true;
 		}
 
 		@Override
 		public boolean onScroll(MotionEvent e1, MotionEvent e2,
 				float distanceX, float distanceY) {
 			Matrix matrix = new Matrix(getImageMatrix());
 			matrix.postTranslate(-distanceX, -distanceY);
 			
 			checkEdges(matrix);
 			
 			setImageMatrix(matrix);
 			return true;
 		}
 
 		@Override
 		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
 				float velocityY) {
 			getImageMatrix().getValues(mTmpValues);
 			
 			int minWidth = (int) (-getDrawable().getIntrinsicWidth() * mTmpValues[Matrix.MSCALE_X]) + getWidth();
 			int minHeight = (int) (-getDrawable().getIntrinsicHeight() * mTmpValues[Matrix.MSCALE_Y]) + getHeight();
 			
 			mScroller.fling((int) mTmpValues[Matrix.MTRANS_X], (int) mTmpValues[Matrix.MTRANS_Y],
 							(int) velocityX, (int) velocityY, minWidth, 0, minHeight, 0);
 			
 			invalidate();
 			
 			return true;
 		}
 
 		@Override
 		public boolean onDown(MotionEvent e) {
 			if (!mScroller.isFinished()) { // Abort fling on user touch
 				mScroller.abortAnimation();
 			}
 			return true;
 		}
 		
 	});
 	
 	private float findFullscreenScale() {
 		// Find a scale such that the image fills the view
 		float scaleHeight = getHeight() / (float) getDrawable().getIntrinsicHeight();
 		float scaleWidth = getWidth() / (float) getDrawable().getIntrinsicWidth();
 		return Math.max(scaleHeight, scaleWidth);
 	}
 	
 	private void checkEdges(Matrix matrix) {
 		matrix.getValues(mTmpValues);
 		
 		if (mTmpValues[Matrix.MTRANS_X] > 0) {
 			matrix.postTranslate(-mTmpValues[Matrix.MTRANS_X], 0);
 		}
 		
 		float maxWidth = -getDrawable().getIntrinsicWidth() * mTmpValues[Matrix.MSCALE_X] + getWidth();
 		if (mTmpValues[Matrix.MTRANS_X] < maxWidth) {
 			matrix.postTranslate(maxWidth - mTmpValues[Matrix.MTRANS_X], 0);
 		}
 		
 		if (mTmpValues[Matrix.MTRANS_Y] > 0) {
 			matrix.postTranslate(0, -mTmpValues[Matrix.MTRANS_Y]);
 		}
 		
 		float maxHeight = -getDrawable().getIntrinsicHeight() * mTmpValues[Matrix.MSCALE_X] + getHeight();
 		if (mTmpValues[Matrix.MTRANS_Y] < maxHeight) {
 			matrix.postTranslate(0, maxHeight - mTmpValues[Matrix.MTRANS_Y]);
 		}
 	}
 	
 	private void checkZoom(Matrix matrix) {
 		matrix.getValues(mTmpValues);
 		
 		float minScale = findFullscreenScale();
 		
 		if (mTmpValues[Matrix.MSCALE_X] > MAX_ZOOM) {
 			float scale = MAX_ZOOM / mTmpValues[Matrix.MSCALE_X];
 			matrix.postScale(scale, scale);
 		}
 		else if (mTmpValues[Matrix.MSCALE_X] < minScale) {
 			float scale = minScale / mTmpValues[Matrix.MSCALE_X];
 			matrix.postScale(scale, scale);
 		}
 	}
 
 }
