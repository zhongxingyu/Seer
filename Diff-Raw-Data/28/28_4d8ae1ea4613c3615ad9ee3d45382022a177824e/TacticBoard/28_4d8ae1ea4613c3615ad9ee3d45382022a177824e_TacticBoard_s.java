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
 	
 	private Stack<Bitmap> mUndoStack = new Stack<Bitmap>();
 	private Stack<Path> mPathStack = new Stack<Path>();
 	
 	public static int maxUndos = 10;
 
 	private final Paint mPaint = new Paint();
 	private Path mPath = new Path();
 	private Canvas mCanvas;
 	private Bitmap mBitmap;
 	private float mLastX;
 	private float mLastY;
 	private float mCurveEndX;
 	private float mCurveEndY;
 
 	private final float RADIUS_TRIANGLE = 20;
 	private final float RADIUS_CIRCLE = 15;
 	private final float TEXT_SIZE = 30;
 	private final int LINE_NUMBER_COLOR = 0xFFFFFFFF;
 	private float mStartX = -1;
 	private float mStartY = -1;
 	private int mLineCount = 1;
 	
 	private final int SHORT_DASH_WIDTH = 3;
 	private final int SHORT_DASH_DOT = 5;
 	private final int SHORT_DASH_SPACE = 10;
 	private final int LONG_DASH_WIDTH = 5;
 	private final int LONG_DASH_DOT = 40;
 	private final int LONG_DASH_SPACE = 30;
 	
 	private static final int INVALIDATE_EXTRA_BORDER = 10;
 	private static final float TOUCH_TOLERANCE = 2;
 	private static final boolean RENDERING_ANTIALIAS = true;
 	private static final boolean DITHER_FLAG = true;
 
	private int mCertainColor = 0xFF000000;
	private float mStrokeWidth = 4.0f;
 
 	private Bitmap mBackGround;
 	private Bitmap mScaledBackGround;
 	
 	private boolean mMoving = false;
 	
 	public TacticBoard(Context context) {
 		super(context);
 		init(context);
 	}	
 	public TacticBoard(Context context, AttributeSet attrs) {
 		super(context, attrs);
 		init(context);
 	}
 	public TacticBoard(Context context, AttributeSet attrs, int defStyle) {
 		super(context, attrs, defStyle);
 		init(context);
 	}
 	
 	public void init(Context context) {
 		this.mContext = context;
 		setDefaultPaint();
 		mBackGround = BitmapFactory.decodeResource(getResources(),R.drawable.img_field);
 		mLineCount = 1;
 	}
 	
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
 		mPaint.setStrokeWidth(SHORT_DASH_WIDTH);
 		mPaint.setPathEffect(
 				new DashPathEffect(new float[] {SHORT_DASH_DOT, SHORT_DASH_SPACE}, 0));
 	}
 	
 	public void setLongDashPaint() {
		setDefaultPaint();
 		mPaint.setStrokeWidth(LONG_DASH_WIDTH);
 		mPaint.setPathEffect(
 				new DashPathEffect(new float[] {LONG_DASH_DOT, LONG_DASH_SPACE}, 0));
 	}
 
 	public void saveImageToUndoStack() {
 		if (mBitmap == null) return;
 		
 		if (mUndoStack.size() >= maxUndos) {
 			Bitmap i = (Bitmap) mUndoStack.get(0);
 			i.recycle();
 			mUndoStack.remove(i);
 		}
 
 		Bitmap bitmap = Bitmap.createBitmap(mBitmap.getWidth(),
 				mBitmap.getHeight(), Bitmap.Config.ARGB_4444);
 		Canvas canvas = new Canvas();
 		canvas.setBitmap(bitmap);
 		canvas.drawBitmap(mBitmap, 0, 0, mPaint);
 
 		mUndoStack.push(bitmap);
 	}
 
 	public void undo() {
 		Bitmap prev = null;
 		try {
 			prev = (Bitmap) mUndoStack.pop();
 			if (mLineCount > 0) mLineCount --;
 		} catch (Exception e) {
 			Log.e(TAG, "undo exception");
 			e.printStackTrace();
 		}
 
 		if (prev != null) {
 			mCanvas.drawBitmap(prev, 0, 0, mPaint);
 			invalidate();
 
 			prev.recycle();
 		}
 	}
 
 	public void drawBackGround(Canvas canvas) {
 		if (canvas != null) {
 			mScaledBackGround = Bitmap.createScaledBitmap(mBackGround, mViewWidth, mViewHeight, true);
 			canvas.drawBitmap (mScaledBackGround, 0, 0, null);
 			//canvas.drawColor(Color.WHITE);
 		}
 	}
 
 	public void updatePaintProperty(int color, int size) {
 		mPaint.setColor(color);
 		mPaint.setStrokeWidth(size);
 	}
 	
 	public void updatePaintColor(int color) {
 		mPaint.setColor(color);
 	}
 
 	public void updatePaintSize(int size) {
 		mPaint.setStrokeWidth(size);
 	}
 	
 	public void resetTacticBoard() {
 		if (mViewWidth > 0 && mViewHeight > 0) {
 			newImage(mViewWidth, mViewHeight);
 		}
 		mUndoStack.clear();
 		mLineCount = 1;
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
 
 	public void setMoving(boolean b) {
 		mMoving = b;
 	}
 	
 	public boolean onTouchEvent(MotionEvent event) {
 		if (mMoving == false) return false;
 		
 		int action = event.getAction();
 		Rect rect = null;
 		
 		switch (action) {
 		case MotionEvent.ACTION_DOWN:
 			saveImageToUndoStack();
 			
 			mStartX = event.getX();
 			mStartY = event.getY();
 			
 			rect = touchDown(event);
 			if (rect != null) invalidate(rect);
 			
 			return true;		
 			
 		case MotionEvent.ACTION_MOVE:
 			rect = touchMoveOrUp(event);
 			if (rect != null) invalidate(rect);	
 			
 			return true;
 		
 		case MotionEvent.ACTION_UP:
 			rect = touchMoveOrUp(event);
 			if (rect != null) invalidate(rect);
 			
 			mPathStack.push(mPath);
 			mPath = new Path();
 			//mPath.rewind();
 			
 			rect = drawArrow();
 			invalidate(rect);
 			
 			rect = drawLineNumber(mStartX, mStartY);
 			if (rect != null) invalidate(rect);
 			
 			return true;
 		}
 
 		return false;
 	}
 	
 	private Rect drawLineNumber(float startX, float startY) {
 		float x = startX;
 		float y = startY;
 		
 		String lineNumber = Integer.toString(mLineCount);
 		
 		// draw circle at the starting line point
 		//mPaint.setStyle(Paint.Style.FILL);
 		//mCanvas.drawCircle(x, y, RADIUS_CIRCLE, mPaint);
 		//mPaint.setStyle(Paint.Style.STROKE);
 
 	    Rect numberRect = new Rect();
 		Paint pNumber = new Paint();
 		pNumber.setTextSize(TEXT_SIZE);
 		pNumber.setColor(LINE_NUMBER_COLOR);
 	    pNumber.getTextBounds(lineNumber, 0, lineNumber.length(), numberRect);
 	    
 		mCanvas.drawText(lineNumber, x - numberRect.width()/2, y + numberRect.height()/2, pNumber);
 		
 		mLineCount ++;
 		
 		Rect invalidRect = new Rect();
 		invalidRect.set((int) (x - RADIUS_CIRCLE - INVALIDATE_EXTRA_BORDER), 
 						(int) (y - RADIUS_CIRCLE - INVALIDATE_EXTRA_BORDER),
 						(int) (x + RADIUS_CIRCLE + INVALIDATE_EXTRA_BORDER), 
 						(int) (y + RADIUS_CIRCLE + INVALIDATE_EXTRA_BORDER) );
 		
 		return invalidRect;
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
 		
 		float p1x = (float) centerX;
 		float p1y = (float) centerY - RADIUS_TRIANGLE;
 		
 		float p2x = (float) (centerX - RADIUS_TRIANGLE * Math.sqrt(3)/2);
 		float p2y = (float) (centerY + RADIUS_TRIANGLE/2);
 		
 		float p3x = (float) centerX;
 		float p3y = (float) (centerY + RADIUS_TRIANGLE/4);
 		
 		float p4x = (float) (centerX + RADIUS_TRIANGLE * Math.sqrt(3)/2);
 		float p4y = (float) (centerY + RADIUS_TRIANGLE/2);
 		
 		Path triangle = new Path();
 		triangle.moveTo(p1x, p1y);
 		triangle.lineTo(p2x, p2y);
 		triangle.lineTo(p3x, p3y);
 		triangle.lineTo(p4x, p4y);
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
 	
 	// unused API
 	public void resetAllPath() {
 		while(!mPathStack.isEmpty()){
 			Path p = (Path) mPathStack.pop();				
 			p.reset();
 		}
 	}
 	
 	// unused API
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
 	
 	// unused API
 	public void clearUndo() {
 		while (true) {
 			Bitmap prev = (Bitmap) mUndoStack.pop();
 			if (prev == null) return;
 			prev.recycle();
 		}
 	}
 	
 }
