 package usask.hci.fastdraw;
 
 import java.util.HashSet;
 import java.util.Set;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Paint.Align;
 import android.graphics.PointF;
 import android.graphics.RectF;
 import android.util.SparseArray;
 import android.view.MotionEvent;
 import android.view.View;
 
 public class DrawView extends View {
 	private Bitmap mBitmap;
     private Paint mBitmapPaint;
 	private final int mCols = 4;
 	private final int mRows = 5;
 	private float mColWidth;
 	private float mRowHeight;
 	private boolean mShowCM;
 	private Paint mCMPaint;
 	private int mSelected;
 	private long mPressedOutsideTime;
 	private long mPressedInsideTime;
 	private boolean mSwitchTools;
 	private int mFingerInside;
     private boolean mCheckToolSwitch;
     private Set<Integer> mIgnoredFingers;
     private Selection[] mSelections;
 	private Tool mTool;
     private int mColor;
     private int mThickness;
     private String mToolName;
     private String mColorName;
     private String mThicknessName;
     private Bitmap mUndo;
     private boolean mLeftHanded;
     private final int mChordDelay = 1000 * 1000 * 200; // 200ms in ns
     private final float mThreshold = 10; // pixel distance before tool registers
     private SparseArray<PointF> mOrigins;
     private boolean mPermanentGrid;
     
     private enum Action {
     	CLEAR, UNDO
     }
 
     private enum SelectionType {
     	TOOL, COLOR, THICKNESS, ACTION, NOOP
     }
     
 	private class Selection {
 		public Object object;
 		public String name;
 		public SelectionType type;
 		
 		public Selection(Object object, String name, SelectionType type) {
 			this.object = object;
 			this.name = name;
 			this.type = type;
 		}
 	}
 
     public DrawView(Context c) {
         super(c);
 
         mBitmapPaint = new Paint(Paint.DITHER_FLAG);
         mCMPaint = new Paint();
         mCMPaint.setTextSize(26);
         mCMPaint.setTextAlign(Align.CENTER);
         mSelected = -1;
         mFingerInside = -1;
         mCheckToolSwitch = true;
         mIgnoredFingers = new HashSet<Integer>();
         mLeftHanded = false;
         mPermanentGrid = false;
         mOrigins = new SparseArray<PointF>();
         
         mSelections = new Selection[] {
         	new Selection(new PaintTool(this), "Paintbrush", SelectionType.TOOL),
         	new Selection(new LineTool(this), "Line", SelectionType.TOOL),
         	new Selection(new CircleTool(this), "Circle", SelectionType.TOOL),
         	new Selection(new RectangleTool(this), "Rectangle", SelectionType.TOOL),
         	
         	new Selection(Color.BLACK, "Black", SelectionType.COLOR),
         	new Selection(Color.RED, "Red", SelectionType.COLOR),
         	new Selection(Color.GREEN, "Green", SelectionType.COLOR),
         	new Selection(Color.BLUE, "Blue", SelectionType.COLOR),
 
         	new Selection(Color.WHITE, "White", SelectionType.COLOR),
         	new Selection(Color.YELLOW, "Yellow", SelectionType.COLOR),
         	new Selection(Color.CYAN, "Cyan", SelectionType.COLOR),
         	new Selection(Color.MAGENTA, "Magenta", SelectionType.COLOR),
 
         	new Selection(1, "Fine", SelectionType.THICKNESS),
         	new Selection(6, "Thin", SelectionType.THICKNESS),
         	new Selection(16, "Normal", SelectionType.THICKNESS),
         	new Selection(50, "Wide", SelectionType.THICKNESS),
         	
         	null, // The position of the command map button
         	new Selection(null, "Save", SelectionType.NOOP),
         	new Selection(Action.CLEAR, "Clear", SelectionType.ACTION),
         	new Selection(Action.UNDO, "Undo", SelectionType.ACTION)
         };
         
         // Default to thin black paintbrush
         changeSelection(0);
         changeSelection(4);
         changeSelection(13);
         
         Timer timer = new Timer();
         timer.schedule(new TimerTask() {
         	@Override
         	public void run() {
         		if (!mCheckToolSwitch)
         			return;
         		
         		long now = System.nanoTime();
         		
         		if (now - mPressedOutsideTime < mChordDelay && now - mPressedInsideTime < mChordDelay) {
         			mSwitchTools = true;
         			mCheckToolSwitch = false;
         		} else if (mFingerInside != -1 && now - mPressedInsideTime > mChordDelay) {
         			mShowCM = true;
         			mTool.clearFingers();
         			postInvalidate();
         		}
         	}
         }, 25, 25);
     }
     
     public int getColor() {
     	return mColor;
     }
     
     public int getThickness() {
     	return mThickness;
     }
 	
 	@Override
 	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
 		super.onSizeChanged(w, h, oldw, oldh);
         mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
         Canvas canvas = new Canvas(mBitmap);
         canvas.drawRGB(0xFF, 0xFF, 0xFF);
         mColWidth = (float)w / mCols;
         mRowHeight = (float)h / mRows;
 	}
 	
 	private RectF getCMButtonBounds() {
 		float top = mRowHeight * (mRows - 1);
 		float bottom = mRowHeight * mRows;
 		
 		if (mLeftHanded)
 			return new RectF(mColWidth * (mCols - 1), top, mColWidth * mCols, bottom);
 		else
 			return new RectF(0, top, mColWidth, bottom);
 	}
 
     @Override
     protected void onDraw(Canvas canvas) {
     	RectF bounds = getCMButtonBounds();
     	
         canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
         mTool.draw(canvas);
         
     	mCMPaint.setColor(0x88FFFF00);
     	canvas.drawRect(bounds, mCMPaint);
     	
         if (mShowCM || mPermanentGrid) {
         	mCMPaint.setColor(0x44888888);
 
         	for (int i = 0; i < mRows; i++) {
         		float top = i * mRowHeight;    		
         		canvas.drawLine(0, top, mColWidth * mCols, top, mCMPaint);
         	}
         	for (int i = 0; i < mCols; i++) {
         		float left = i * mColWidth;    		
         		canvas.drawLine(left, 0, left, mRowHeight * mRows, mCMPaint);
         	}
         }
         
         if (mShowCM) {
     		mCMPaint.setColor(0xFF888888);
         	for (int y = 0; y < mRows; y++) {
         		for (int x = 0; x < mCols; x++) {
         			int realX = x;
         			if (mLeftHanded)
         				realX = mCols - x - 1;
         			
         			int i = y * mCols + realX;
        			if (mSelections[i] != null)
        				canvas.drawText(mSelections[i].name, (x + 0.5f) * mColWidth, (y + 0.5f) * mRowHeight, mCMPaint);
         		}
         	}
         } else if (!mPermanentGrid) {
     		mCMPaint.setColor(0x44888888);
         	canvas.drawLine(bounds.left, bounds.top, bounds.right, bounds.top, mCMPaint);
         	
         	if (mLeftHanded)
         		canvas.drawLine(bounds.left, bounds.top, bounds.left, bounds.bottom, mCMPaint);
         	else
         		canvas.drawLine(bounds.right, bounds.top, bounds.right, bounds.bottom, mCMPaint);
         }
 
 		mCMPaint.setColor(0x88888888);
        canvas.drawText(mThicknessName, bounds.left + mColWidth / 2, bounds.top + mRowHeight / 2 - 30, mCMPaint);
        canvas.drawText(mColorName, bounds.left + mColWidth / 2, bounds.top + mRowHeight / 2, mCMPaint);
        canvas.drawText(mToolName, bounds.left + mColWidth / 2, bounds.top + mRowHeight / 2 + 30, mCMPaint);
     }
     
     @Override
     public boolean onTouchEvent(MotionEvent event) {
     	int index = event.getActionIndex();
         float x = event.getX(index);
         float y = event.getY(index);
         int id = event.getPointerId(index);
 
         switch (event.getActionMasked()) {
             case MotionEvent.ACTION_DOWN:
             case MotionEvent.ACTION_POINTER_DOWN:
             	if (event.getPointerCount() == 1)
             		mCheckToolSwitch = true;
             	
             	long now = System.nanoTime();
             	if (getCMButtonBounds().contains(x, y)) {
             		mFingerInside = id;
             		mPressedInsideTime = now;
             		mIgnoredFingers.add(mFingerInside);
             	} else {
             		int col = (int) (x / mColWidth);
             		int row = (int) (y / mRowHeight);
             		
             		if (mLeftHanded)
             			col = mCols - col - 1;
             		
             		mSelected = row * mCols + col;
             		
             		mPressedOutsideTime = now;
             	}
             	
             	if (mShowCM) {
             		if (event.getPointerCount() == 2) {
             			changeSelection(mSelected);
             			mSwitchTools = false;
             			mCheckToolSwitch = false;
             			mShowCM = false;
             			mFingerInside = -1;
             			
             			for (int i = 0; i < 2; i++)
             				mIgnoredFingers.add(event.getPointerId(i));
             		}
             		
             		break;
             	}
 
             	mOrigins.put(id, new PointF(x, y));
             	mTool.touchStart(id, x, y);
                 break;
                 
             case MotionEvent.ACTION_MOVE:
             	if (mShowCM)
             		break;
             	
             	int count = event.getPointerCount();
             	
             	for (int i = 0; i < count; i++) {
             		int fingerId = event.getPointerId(i);
             		
             		if(mIgnoredFingers.contains(fingerId))
             			continue;
             		
                     float x2 = event.getX(i);
                     float y2 = event.getY(i);
                     PointF origin = mOrigins.get(fingerId);
                     
                     if (origin != null) {
 	                    float dx = origin.x - x2;
 	                    float dy = origin.y - y2;
 	                    double dist = Math.sqrt(dx*dx + dy*dy);
 	                    
 	                    if (dist > mThreshold) {
 	                    	mOrigins.delete(fingerId);
 	                    	origin = null;
 	                    }
                     }
                     
                     if (origin == null) {
                     	mTool.touchMove(fingerId, x2, y2);
                     }
             	}
             	
                 break;
                 
             case MotionEvent.ACTION_UP:
             case MotionEvent.ACTION_POINTER_UP:
             	mOrigins.delete(id);
             	
         		boolean draw = true;
         		
             	if (id == mFingerInside)
             		mFingerInside = -1;
 
         		if (event.getPointerCount() == 1) {
         	    	if (mSwitchTools) {
         				changeSelection(mSelected);
         				mSwitchTools = false;
         				draw = false;
         	    	}
         		}
         		
             	if (mIgnoredFingers.contains(id)) {
             		mIgnoredFingers.remove(id);
             		draw = false;
             	}
             	
             	if (mShowCM) {
             		if (event.getPointerCount() == 1) {
             			mShowCM = false;
             		}
             	} else if (draw) {
             		mUndo = mBitmap.copy(mBitmap.getConfig(), true);
             		mTool.touchStop(id, x, y, new Canvas(mBitmap));
             	}
         		
                 break;
         }
 
         invalidate();
         return true;
     }
     
     private void changeSelection(int selected) {
 		if (mTool != null)
 			mTool.clearFingers();
 		
     	Selection selection = mSelections[selected];
     	
     	if (selection == null)
     		return;
     	
     	switch (selection.type) {
     		case TOOL:
     			mTool = (Tool) selection.object;
     			mToolName = selection.name;
     			break;
     			
     		case COLOR:
     			mColor = (Integer) selection.object;
     			mColorName = selection.name;
     			break;
     			
     		case THICKNESS:
     			mThickness = (Integer) selection.object;
     			mThicknessName = selection.name;
     			break;
     			
     		case ACTION:
     			switch ((Action) selection.object) {
     				case CLEAR:
     					mUndo = mBitmap.copy(mBitmap.getConfig(), true);
     			        Canvas canvas = new Canvas(mBitmap);
     			        canvas.drawRGB(0xFF, 0xFF, 0xFF);
     					break;
     					
     				case UNDO:
     					if (mUndo != null) {
         					Bitmap temp = mBitmap.copy(mBitmap.getConfig(), true);
         					mBitmap = mUndo;
         					mUndo = temp;
         				}
     					break;
     			}
     			break;
     			
     		default:
     			break;
     	}
     }
 
 	public void loadPreferences(SharedPreferences sharedPreferences) {
 		mLeftHanded = sharedPreferences.getBoolean("pref_left_handed", false);
 		mPermanentGrid = sharedPreferences.getBoolean("pref_permanent_grid", false);
 		invalidate();
 	}
 }
