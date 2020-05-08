 package com.example.tb;
 
 import java.io.OutputStream;
 import java.util.Stack;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.DashPathEffect;
 import android.graphics.Matrix;
 import android.graphics.Paint;
 import android.graphics.Path;
 import android.graphics.Rect;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.view.MotionEvent;
 import android.view.View;
 
 public class TacticBoard extends View {
 	private boolean DEBUG = true;
 	private String TAG = "TacticBoard";
 	
 	private Context mContext;
 	private int mViewWidth;
 	private int mViewHeight;
 	
 	private Stack<Bitmap> mUndos = new Stack<Bitmap>();
 	private Stack<Path> mPathStack = new Stack<Path>();
 	
	public static int maxUndos = 11;
 
 	private final Paint mPaint = new Paint();
 	private Path mPath = new Path();
 	private Canvas mCanvas;
 	private Bitmap mBitmap;
 	private float mLastX;
 	private float mLastY;
 	private float mCurveEndX;
 	private float mCurveEndY;
 
 	private final float RADIUS_TRIANGLE = 20;
 	private static final int INVALIDATE_EXTRA_BORDER = 10;
 	private static final float TOUCH_TOLERANCE = 2;
 	private static final boolean RENDERING_ANTIALIAS = true;
 	private static final boolean DITHER_FLAG = true;
 
 	private int mCertainColor = 0xFF000000;
 	private float mStrokeWidth = 4.0f;
 
 	public void setDefaultPaint() {
 		Log.i(TAG, "Constructor Initialized");
 		mPaint.reset();
 		mPaint.setAntiAlias(RENDERING_ANTIALIAS);
 		mPaint.setColor(mCertainColor);
 		mPaint.setStyle(Paint.Style.STROKE);
 		mPaint.setStrokeJoin(Paint.Join.ROUND);
 		mPaint.setStrokeCap(Paint.Cap.ROUND);
 		mPaint.setStrokeWidth(mStrokeWidth);
 		mPaint.setDither(DITHER_FLAG);
 		
 		mLastX = -1;
 		mLastY = -1;
 	}
 	
 	public void setShortDashPaint() {
 		setDefaultPaint();
 		mPaint.setPathEffect(new DashPathEffect(new float[] {10, 10}, 0));
 	}
 	
 	public void setLongDashPaint() {
 		setDefaultPaint();
 		mPaint.setPathEffect(new DashPathEffect(new float[] {25, 25}, 0));
 	}
 	
 	public TacticBoard(Context context) {
 		super(context);
 		this.mContext = context;
 		setDefaultPaint();
 	}
 	
 	public TacticBoard(Context context, AttributeSet attrs) {
 		super(context, attrs);
 		this.mContext = context;
 		setDefaultPaint();
 	}
 
 	public TacticBoard(Context context, AttributeSet attrs, int defStyle) {
 		super(context, attrs, defStyle);
 		this.mContext = context;
 		setDefaultPaint();
 	}
 	
 	// unused API
 	public void clearUndo() {
 		while (true) {
 			Bitmap prev = (Bitmap) mUndos.pop();
 			if (prev == null) return;
 			prev.recycle();
 		}
 	}
 
 	public void saveImageToUndoStack() {
 		if (mBitmap == null) return;
 		
		while (mUndos.size() >= maxUndos) {
			Bitmap i = (Bitmap) mUndos.get(mUndos.size() - 1);
 			i.recycle();
 			mUndos.remove(i);
 		}
 
 		Bitmap bitmap = Bitmap.createBitmap(mBitmap.getWidth(),
 				mBitmap.getHeight(), Bitmap.Config.ARGB_4444);
 		Canvas canvas = new Canvas();
 		canvas.setBitmap(bitmap);
 		canvas.drawBitmap(mBitmap, 0, 0, mPaint);
 
 		mUndos.push(bitmap);
 	}
 
	//TODO: Bug for undo
 	public void undo() {
 		Bitmap prev = null;
 		try {
 			prev = (Bitmap) mUndos.pop();
 		} catch (Exception e) {
 			Log.e(TAG, "undo exception");
 			e.printStackTrace();
 		}
 
 		if (prev != null) {
 			drawBackGround(mCanvas);
 			mCanvas.drawBitmap(prev, 0, 0, mPaint);
 			invalidate();
 
 			prev.recycle();
 		}
 	}
 
 	public void drawBackGround(Canvas canvas) {
 		if (canvas != null) {
 			Bitmap bg = BitmapFactory.decodeResource(getResources(),R.drawable.img_field);
 			Bitmap scaledBg = Bitmap.createScaledBitmap(bg, mViewWidth, mViewHeight, true);
 			canvas.drawBitmap (scaledBg, 0, 0, null);
 			//canvas.drawColor(Color.WHITE);
 		}
 	}
 
 	public void updatePaintProperty(int color, int size) {
 		mPaint.setColor(color);
 		mPaint.setStrokeWidth(size);
 	}
 
 	public void resetTacticBoard() {
 		if (mViewWidth > 0 && mViewHeight > 0) {
 			newImage(mViewWidth, mViewHeight);
 		}
 		mUndos.clear();
 	}
 	
 	public void newImage(int width, int height) {
 		Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_4444);
 		Canvas canvas = new Canvas();
 		canvas.setBitmap(bitmap);
 
 		mBitmap = bitmap;
 		mCanvas = canvas;
 
 		drawBackGround(mCanvas);
 		invalidate();
 	}
 
 	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
 		this.mViewWidth = w;
 		this.mViewHeight = h;
 		if (mViewWidth > 0 && mViewHeight >0) 
 			newImage(mViewWidth, mViewHeight);
 	}
 	
 	protected void onDraw(Canvas canvas) {
 		super.onDraw(canvas);
 
 		if (mBitmap != null)
 			canvas.drawBitmap(mBitmap, 0, 0, null);
 	}
 
 	public boolean onTouchEvent(MotionEvent event) {
 		int action = event.getAction();
 		Rect rect = null;
 		
 		switch (action) {
 		case MotionEvent.ACTION_DOWN:
 			saveImageToUndoStack();
 			
 			rect = touchDown(event);
 			if (rect != null) invalidate(rect);
 			
 			return true;		
 			
 		case MotionEvent.ACTION_MOVE:
 			rect = touchMoveOrUp(event);
 			if (rect != null) invalidate(rect);	
 			
 			return true;
 		
 		case MotionEvent.ACTION_UP:
 			rect = touchMoveOrUp(event);
 			if (rect != null) {
 				invalidate(rect);
 			}
 			
 			mPathStack.push(mPath);
 			mPath = new Path();
 			//mPath.rewind();
 			
 			rect = drawArrow();
 			invalidate(rect);
 			
 			return true;
 		}
 
 		return false;
 	}
 	
 	private Rect drawArrow() {
 		float preX = mCurveEndX;
 		float preY = mCurveEndY;
 		float centerX = mLastX;
 		float centerY = mLastY;
 
 		float alpha = (float) Math.atan( (preX - centerX)/(preY - centerY) );
 		float alphaDegree = (float) Math.toDegrees(alpha);
 		
 		if (preY - centerY > 0) 
 			alphaDegree = - alphaDegree;
 		else 
 			alphaDegree = 180 - alphaDegree;
 		
 		float p1x = centerX;
 		float p1y = centerY - RADIUS_TRIANGLE;
 		
 		float p2x = (float) (centerX - RADIUS_TRIANGLE * Math.sqrt(3)/2);
 		float p2y = (float) (centerY + RADIUS_TRIANGLE/2);
 		
 		float p3x = (float) (centerX + RADIUS_TRIANGLE * Math.sqrt(3)/2);
 		float p3y = (float) (centerY + RADIUS_TRIANGLE/2);
 		
 		Path triangle = new Path();
 		triangle.moveTo(p1x, p1y);
 		triangle.lineTo(p2x, p2y);
 		triangle.lineTo(p3x, p3y);
 		triangle.lineTo(p1x, p1y);
 		
 		Matrix m = new Matrix();
 		m.setRotate(alphaDegree, centerX, centerY);
 		triangle.transform(m);
 		
 		Rect invalidRect = new Rect();
 		invalidRect.set( (int) (centerX - RADIUS_TRIANGLE - INVALIDATE_EXTRA_BORDER),
 				(int) (centerY - RADIUS_TRIANGLE - INVALIDATE_EXTRA_BORDER),
 				(int) (centerX + RADIUS_TRIANGLE + INVALIDATE_EXTRA_BORDER),
 				(int) (centerY + RADIUS_TRIANGLE + INVALIDATE_EXTRA_BORDER) );
 		
 
 		mPaint.setStyle(Paint.Style.FILL);
 		mCanvas.drawPath(triangle, mPaint);
 		mPaint.setStyle(Paint.Style.STROKE);
 		
 		return invalidRect;
 	}
 	
 	private Rect touchDown(MotionEvent event) {
 		float x = event.getX();
 		float y = event.getY();
 		
 		mLastX = mCurveEndX = x;
 		mLastY = mCurveEndY = y;
 		
 		mPath.moveTo(x, y);
 		mCanvas.drawPath(mPath, mPaint);
 		
 		final int border = INVALIDATE_EXTRA_BORDER;
 		Rect invalidRect = new Rect();
 		invalidRect.set((int) x - border, (int) y - border, (int) x + border, (int) y + border);
 		
 		return invalidRect;
 	}
 	
 	private Rect touchMoveOrUp(MotionEvent event) {
 		final float x = event.getX();
 		final float y = event.getY();
 		
 		final float dx = Math.abs(x - mLastX);
 		final float dy = Math.abs(y - mLastY);
 		
 		Rect invalidRect = new Rect();
 		if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
 			final int border = INVALIDATE_EXTRA_BORDER;
 			invalidRect.set((int) mCurveEndX - border, (int) mCurveEndY - border, 
 					(int) mCurveEndX + border, (int) mCurveEndY + border);
 			
 			float cX = mCurveEndX = (x + mLastX) / 2;
 			float cY = mCurveEndY = (y + mLastY) / 2;
 			
 			mPath.quadTo(mLastX, mLastY, cX, cY);
 			
 			invalidRect.union((int) mLastX - border, (int) mLastY - border, 
 					(int) mLastX + border, (int) mLastY + border);
 			invalidRect.union((int) cX - border, (int) cY - border, 
 					(int) cX + border, (int) cY + border);
 			
 			mLastX = x;
 			mLastY = y;
 			
 			mCanvas.drawPath(mPath, mPaint);	
 		}
 		return invalidRect;
 	}
 	
 	public void resetAllPath() {
 		while(!mPathStack.isEmpty()){
 			Path p = (Path) mPathStack.pop();				
 			p.reset();
 		}
 	}
 	
 	public boolean save(OutputStream outStream) {
 		try {
 			mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
 			invalidate();
 			return true;
 		} catch (Exception e) {
 			e.printStackTrace();
 			return false;
 		}
 	}
 	
 }
