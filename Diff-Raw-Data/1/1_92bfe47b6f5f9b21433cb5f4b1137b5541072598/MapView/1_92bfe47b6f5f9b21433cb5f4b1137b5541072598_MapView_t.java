 package org.dyndns.pawitp.salayatrammap.map;
 
 import org.dyndns.pawitp.salayatrammap.R;
 
 import android.content.Context;
 import android.database.Cursor;
 import android.graphics.Canvas;
 import android.graphics.Matrix;
 import android.graphics.Paint;
 import android.graphics.Rect;
 import android.graphics.drawable.Drawable;
 import android.graphics.drawable.NinePatchDrawable;
 import android.os.Build;
 import android.os.Bundle;
 import android.os.Parcelable;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.view.GestureDetector;
 import android.view.MotionEvent;
 import android.view.ScaleGestureDetector;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ImageView;
 import android.widget.Scroller;
 
 public class MapView extends ImageView implements OnClickListener {
 	
 	private static final String TAG = "MapView";
 	
 	private static final float DEFAULT_ZOOM = 0.8F;
 	private static final float MAX_ZOOM = 1.2F;
 	private static final float TRACKBALL_FACTOR = 10F;
 	private static final int SEARCH_LIMIT = 50; // Limit for searching nearest stop, see TramDbHelper for more info
 	
 	// for drawing stop info
 	private static final float TEXT_SIZE = 15F;
 	private static final float LINE_SPACING = 5F;
 	private static final float TEXT_Y_SHIFT = 12F;
 	private static final int NO_STOP_INFO = -1;
 	
 	private static final String KEY_MATRIX = "matrix";
 	private static final String KEY_STOP_INFO = "stop_info";
 	
 	private static Drawable mDrawable; // So that the (huge) map doesn't have to be reloaded every time. Not so elegant though.
 	
 	private boolean mRestored = false;
 	private boolean mLayout = false; // Prevent duplicate zoom/scale if onLayout is called more than once
 	private float[] mTmpValues = new float[9];
 	private Matrix mMatrix = new Matrix(); // for using in manipulate, don't create a new matrix everytime to reduce GC
 	private Scroller mScroller = new Scroller(getContext());
 	private Zoomer mZoomer = new Zoomer();
 	private ScaleGestureDetector mScaleGestureDetector; // Cannot be instantiated here in order to support pre-froyo
 	private GestureDetector mGestureDetector; // Instantiated later because pre-froyo needs a different constructor
 	private TramDbHelper mDbHelper = new TramDbHelper(getContext());
 	private StopInfo mStopInfo = new StopInfo();
 	
 	// for drawing stop info
 	private Paint mTextPaint = new Paint();
 	private Rect mRectTh = new Rect();
 	private Rect mRectEn = new Rect();
 	private NinePatchDrawable mDrawableStopInfoBg = (NinePatchDrawable) getResources().getDrawable(R.drawable.bubble);
 	private float mDensity = getContext().getResources().getDisplayMetrics().density;
 	private float mLineSpacing = LINE_SPACING * mDensity;
 	
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
 		
 		mDbHelper.open();
 		
 		// Load drawable
 		if (mDrawable == null) { // this is static and will survive reloads
 			mDrawable = getResources().getDrawable(R.drawable.map);
 		}
 		setImageDrawable(mDrawable);
 		
 		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
 			mGestureDetector = new GestureDetector(getContext(), mGestureDetectorListener, null, true);
 			
 			mScaleGestureDetector = new ScaleGestureDetector(getContext(), new ScaleGestureDetector.SimpleOnScaleGestureListener() {
 
 				@Override
 				public boolean onScale(ScaleGestureDetector detector) {
 					mMatrix.set(getImageMatrix());
 					
 					mMatrix.getValues(mTmpValues);
 					if ((mTmpValues[Matrix.MSCALE_X] == MAX_ZOOM && detector.getScaleFactor() > 1) ||
 						(mTmpValues[Matrix.MSCALE_X] == findFullscreenScale() && detector.getScaleFactor() < 1) ) {
 						return true;
 					}
 					
 					mMatrix.postScale(detector.getScaleFactor(), detector.getScaleFactor(), detector.getFocusX(), detector.getFocusY());
 					checkZoom(mMatrix);
 					checkEdges(mMatrix);
 					setImageMatrix(mMatrix);
 					return true;
 				}
 				
 			});
 		}
 		else {
 			mGestureDetector = new GestureDetector(getContext(), mGestureDetectorListener);
 		}
 		
 		// For drawing stop info
 		mTextPaint.setTextSize(TEXT_SIZE * mDensity);
 		mTextPaint.setAntiAlias(true);
 	}
 	
 	@Override
 	protected void onRestoreInstanceState(Parcelable state) {
 		super.onRestoreInstanceState(BaseSavedState.EMPTY_STATE); // stupid check
 		
 		Bundle bundle = (Bundle) state;
 		mTmpValues = bundle.getFloatArray(KEY_MATRIX);
 		
 		int stopId = bundle.getInt(KEY_STOP_INFO);
 		if (stopId != NO_STOP_INFO) {
 			Cursor cursor = mDbHelper.getStopInfo(stopId);
 			mStopInfo.readCursor(cursor);
			cursor.close();
 		}
 		
 		// Check zoom, edges later when widths and heights are initialized (onLayout)
 		
 		mRestored = true;
 	}
 
 	@Override
 	protected Parcelable onSaveInstanceState() {
 		super.onSaveInstanceState(); // stupid check
 		
 		Bundle state = new Bundle();
 		getImageMatrix().getValues(mTmpValues);
 		state.putFloatArray(KEY_MATRIX, mTmpValues);
 		
 		if (mStopInfo.enabled) {
 			state.putInt(KEY_STOP_INFO, mStopInfo.id);
 		}
 		else {
 			state.putInt(KEY_STOP_INFO, NO_STOP_INFO);
 		}
 		
 		return state;
 	}
 
 	public void showStopInfo(int stopId) {
 		Cursor cursor = mDbHelper.getStopInfo(stopId);
 		mStopInfo.readCursor(cursor);
 		
 		getImageMatrix().getValues(mTmpValues);
 		
 		int x = Math.round(mTmpValues[Matrix.MTRANS_X]);
 		int y = Math.round(mTmpValues[Matrix.MTRANS_Y]);
 		int fx = Math.round(mStopInfo.x * mTmpValues[Matrix.MSCALE_X]) - getWidth() / 2;
 		int fy = Math.round(mStopInfo.y * mTmpValues[Matrix.MSCALE_Y]) - getHeight() / 2;
 		int dx = -(x + fx);
 		int dy = -(y + fy);
 		
 		//Log.v(TAG, String.format("scaleX: %f scaleY: %f", mTmpValues[Matrix.MSCALE_X], mTmpValues[Matrix.MSCALE_Y]));
 		//Log.v(TAG, String.format("x: %d y: %d dx: %d dy: %d fx: %d fy :%d", x, y, dx, dy, fx, fy));
 		
 		mScroller.startScroll(x, y, dx, dy);
 		invalidate();
 	}
 	
 	public boolean isShowingStopInfo() {
 		return mStopInfo.enabled;
 	}
 	
 	public void hideStopInfo() {
 		mStopInfo.enabled = false;
 		invalidate();
 	}
 	
 	@Override
 	protected void onLayout(boolean changed, int left, int top, int right,
 			int bottom) {
 		super.onLayout(changed, left, top, right, bottom);
 		
 		// Zoom the map out by default and center it
 		// Cannot be done in the constructor because the size of the view is not known yet
 		if (!mLayout) {
 			if (!mRestored) {
 				mMatrix.set(getImageMatrix());
 				float scale = findFullscreenScale();
 				mMatrix.setScale(scale, scale);
 				
 				float width = -(getDrawable().getIntrinsicWidth() * scale - getWidth()) / 2;
 				float height = -(getDrawable().getIntrinsicHeight() * scale - getHeight()) / 2;
 				mMatrix.postTranslate(width, height);
 				
 				setImageMatrix(mMatrix);
 			}
 			else {
 				mMatrix.set(getImageMatrix());
 				mMatrix.setValues(mTmpValues);
 				checkZoom(mMatrix);
 				checkEdges(mMatrix);
 				setImageMatrix(mMatrix);
 			}
 			
 			mLayout = true;
 		}
 	}
 
 	@Override
 	public boolean onTouchEvent(MotionEvent event) {
 		boolean ret = false;
 		ret = mGestureDetector.onTouchEvent(event); // feed it multi-touch event as well so it knows what to ignore
 		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
 			ret |= mScaleGestureDetector.onTouchEvent(event);
 		}
 		return ret;
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
 		mMatrix.set(getImageMatrix());
 		
 		mMatrix.getValues(mTmpValues);
 		
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
 			mMatrix.set(getImageMatrix());
 			
 			mMatrix.getValues(mTmpValues);
 			
 			int currX = mScroller.getCurrX();
 			int currY = mScroller.getCurrY();
 			if ((currX == mScroller.getFinalX() && currX != mScroller.getStartX()) || // Prevent awkward scrolling along the edge
 				(currY == mScroller.getFinalY() && currY != mScroller.getStartY())) {
 				mScroller.abortAnimation();
 			}
 			else {
 				mMatrix.postTranslate(-mTmpValues[Matrix.MTRANS_X] + currX,
 									 -mTmpValues[Matrix.MTRANS_Y] + currY);
 				
 				checkEdges(mMatrix);
 				setImageMatrix(mMatrix);
 			}
 			invalidate();
 		}
 		
 		if (mZoomer.compute()) {
 			mMatrix.set(getImageMatrix());
 			
 			mMatrix.getValues(mTmpValues);
 			
 			float scale = mZoomer.getCurrScale() / mTmpValues[Matrix.MSCALE_X];
 			mMatrix.postScale(scale, scale, mZoomer.getPivotX(), mZoomer.getPivotY());
 			
 			checkEdges(mMatrix);
 			setImageMatrix(mMatrix);
 			
 			invalidate();
 		}
 	}
 	
 	@Override
 	protected void onDraw(Canvas canvas) {
 		super.onDraw(canvas);
 		
 		// Draw stop info
 		if (mStopInfo.enabled) {
 			getImageMatrix().getValues(mTmpValues);
 			
 			float textX = mStopInfo.x * mTmpValues[Matrix.MSCALE_X] + mTmpValues[Matrix.MTRANS_X];
 			float textY = mStopInfo.y * mTmpValues[Matrix.MSCALE_Y] + mTmpValues[Matrix.MTRANS_Y] - TEXT_Y_SHIFT * mTmpValues[Matrix.MSCALE_Y];
 			
 			String textTh = mStopInfo.name_th;
 			String textEn = mStopInfo.name_en;
 			float hWidthTh = mTextPaint.measureText(textTh) / 2;
 			float hWidthEn = mTextPaint.measureText(textEn) / 2;
 			mTextPaint.getTextBounds(textTh, 0, textTh.length(), mRectTh);
 			mTextPaint.getTextBounds(textEn, 0, textEn.length(), mRectEn);
 			
 			int rectThTop = mRectTh.top; // we'll want this later
 			
 			Rect rectBg = mRectTh; // re-use object
 			rectBg.left = mRectTh.left; // same for both
 			rectBg.right = Math.max(mRectTh.right, mRectEn.right);
 			rectBg.top = Math.round(mRectTh.top + mRectEn.top - mLineSpacing);
 			rectBg.bottom = mRectTh.bottom;
 			
 			rectBg.offset(Math.round(textX - rectBg.width() / 2), Math.round(textY));
 			
 			// Draw text background
 			Rect padding = mRectEn; // re-use object
 			mDrawableStopInfoBg.getPadding(padding);
 			rectBg.top -= padding.top + padding.bottom;
 			rectBg.left -= padding.left;
 			rectBg.right += padding.right;
 			mDrawableStopInfoBg.setBounds(rectBg);
 			mDrawableStopInfoBg.draw(canvas);
 			
 			// Draw text
 			canvas.drawText(textEn, textX - hWidthEn, textY - padding.bottom + rectThTop - mLineSpacing, mTextPaint);
 			canvas.drawText(textTh, textX - hWidthTh, textY - padding.bottom, mTextPaint);
 		}
 	}
 	
 	GestureDetector.SimpleOnGestureListener mGestureDetectorListener = new GestureDetector.SimpleOnGestureListener() {
 
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
 			mMatrix.set(getImageMatrix());
 			mMatrix.postTranslate(-distanceX, -distanceY);
 			
 			checkEdges(mMatrix);
 			
 			setImageMatrix(mMatrix);
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
 		
 		@Override
 		public void onLongPress(MotionEvent e) {
 			onSingleTapConfirmed(e);
 		}
 
 		@Override
 		public boolean onSingleTapConfirmed(MotionEvent e) {
 			invalidate();
 			
 			getImageMatrix().getValues(mTmpValues);
 			int imageX = (int)((e.getX() - mTmpValues[Matrix.MTRANS_X]) / mTmpValues[Matrix.MSCALE_X]);
 			int imageY = (int)((e.getY() - mTmpValues[Matrix.MTRANS_Y]) / mTmpValues[Matrix.MSCALE_Y]);
 			
 			Log.v(TAG, "Tap/Long press: x: " + imageX + " y: " + imageY);
 			
 			Cursor cursor = mDbHelper.findNearestStop(imageX, imageY, (int) (SEARCH_LIMIT / mTmpValues[Matrix.MSCALE_X]));
 			try {
 				if (cursor.getCount() > 0) {
 					mStopInfo.readCursor(cursor);
 					
 					Log.v(TAG, "Stop: " + mStopInfo.id + " d: " + cursor.getString(cursor.getColumnIndex("d")));
 					
 					return true;
 				}
 				else {
 					mStopInfo.enabled = false;
 					
 					return false;
 				}
 			}
 			finally {
 				cursor.close();
 			}
 		}
 		
 	};
 	
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
 
 	private static class StopInfo {
 		public boolean enabled = false;
 		public int id;
 		public String name_th;
 		public String name_en;
 		public int x;
 		public int y;
 		
 		public void readCursor(Cursor cursor) {
 			enabled = true;
 			id = cursor.getInt(cursor.getColumnIndex(TramDbHelper.KEY_ROWID));
 			name_th = cursor.getString(cursor.getColumnIndex(TramDbHelper.KEY_NAME_TH));
 			name_en = cursor.getString(cursor.getColumnIndex(TramDbHelper.KEY_NAME_EN));
 			x = cursor.getInt(cursor.getColumnIndex(TramDbHelper.KEY_X));
 			y = cursor.getInt(cursor.getColumnIndex(TramDbHelper.KEY_Y));
 		}
 	}
 	
 }
