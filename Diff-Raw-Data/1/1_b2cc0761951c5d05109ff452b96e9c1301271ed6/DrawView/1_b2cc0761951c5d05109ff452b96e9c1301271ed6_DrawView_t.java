 package usask.hci.fastdraw;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Set;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import usask.hci.fastdraw.GestureDetector.Gesture;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.SharedPreferences;
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Paint.Align;
 import android.graphics.Paint.Style;
 import android.graphics.PointF;
 import android.graphics.Rect;
 import android.graphics.RectF;
 import android.util.Log;
 import android.util.SparseArray;
 import android.view.MotionEvent;
 import android.view.View;
 import android.widget.CheckBox;
 import android.widget.LinearLayout;
 import android.widget.NumberPicker;
 
 public class DrawView extends View {
 	private MainActivity mMainActivity;
 	private StudyLogger mLog;
 	private StudyController mStudyCtl;
 	private boolean mStudyMode;
 	private Bitmap mBitmap;
     private Paint mBitmapPaint;
 	private final int mCols = 4;
 	private final int mRows = 5;
 	private float mColWidth;
 	private float mRowHeight;
 	private boolean mShowOverlay;
 	private long mOverlayStart;
 	private Paint mPaint;
 	private int mSelected;
 	private long mPressedInsideTime;
 	private int mFingerInside;
     private boolean mCheckOverlay;
     private Set<Integer> mFingers;
     private Set<Integer> mIgnoredFingers;
     private Selection[] mSelections;
     private HashMap<Gesture, Integer> mGestureSelections;
 	private Tool mTool;
     private int mColor;
     private int mThickness;
     private String mToolName;
     private String mColorName;
     private String mThicknessName;
     private Bitmap mUndo;
     private Bitmap mNextUndo;
     private boolean mLeftHanded;
     private final float mThreshold = 10; // pixel distance before tool registers
     private SparseArray<PointF> mOrigins;
     private boolean mPermanentGrid;
     private Rect mTextBounds;
     private SparseArray<Long> mFlashTimes;
     private SparseArray<Long> mRecentTouches;
     private boolean mChanged;
 	private GestureDetector mGestureDetector;
 	private Gesture mGesture;
 	private int mPossibleGestureFinger;
 	private long mPossibleGestureFingerTime;
 	private int mGestureFinger;
 	private PointF mGestureFingerPos;
 	private boolean mShowGestureMenu;
 	private Gesture mActiveCategory;
 	private PointF mActiveCategoryOrigin;
 	private Gesture mSubSelection;
 	private UI mUI;
     private final int mChordDelay = 1000 * 1000 * 200; // 200ms in ns
 	private final int mFlashDelay = 1000 * 1000 * 400; // 400ms in ns
     private final int mGestureMenuDelay = 1000 * 1000 * 200; // 200ms in ns
 	private final int mOverlayButtonIndex = 16;
 	private final int mGestureButtonDist = 150;
 	private final int mGestureButtonSize = 75;
 	
 	private enum UI {
 		CHORD, GESTURE
 	}
     
     private enum Action {
     	SAVE, CLEAR, UNDO
     }
 
     private enum SelectionType {
     	TOOL, COLOR, THICKNESS, ACTION
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
 
     public DrawView(Context mainActivity) {
         super(mainActivity);
         
         if (!(mainActivity instanceof MainActivity)) {
         	Log.e("DrawView", "DrawView was not given the MainActivity");
         	return;
         }
 
         mUI = UI.CHORD;
         mMainActivity = (MainActivity) mainActivity;
         mStudyMode = false;
         mLog = new StudyLogger(mainActivity);
         mBitmapPaint = new Paint(Paint.DITHER_FLAG);
         mPaint = new Paint();
         mPaint.setTextSize(26);
         mPaint.setTextAlign(Align.CENTER);
         mSelected = -1;
         mFingerInside = -1;
         mCheckOverlay = true;
         mFingers = new HashSet<Integer>();
         mIgnoredFingers = new HashSet<Integer>();
         mLeftHanded = false;
         mPermanentGrid = false;
         mOrigins = new SparseArray<PointF>();
         mTextBounds = new Rect();
         mFlashTimes = new SparseArray<Long>();
         mRecentTouches = new SparseArray<Long>();
         mChanged = false;
         mGestureDetector = new GestureDetector();
         mGesture = Gesture.UNKNOWN;
         mGestureFinger = -1;
         mPossibleGestureFinger = -1;
         mShowGestureMenu = false;
         mActiveCategory = Gesture.UNKNOWN;
         mActiveCategoryOrigin = new PointF();
 		mSubSelection = Gesture.UNKNOWN;
         
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
         	new Selection(Action.SAVE, "Save", SelectionType.ACTION),
         	new Selection(Action.CLEAR, "Clear", SelectionType.ACTION),
         	new Selection(Action.UNDO, "Undo", SelectionType.ACTION)
         };
         
         mGestureSelections = new HashMap<Gesture, Integer>();
         
         mGestureSelections.put(Gesture.UP, 0);          // Paintbrush
         mGestureSelections.put(Gesture.UP_RIGHT, 1);    // Line
         mGestureSelections.put(Gesture.UP_DOWN, 2);     // Circle
         mGestureSelections.put(Gesture.UP_LEFT, 3);     // Rectangle
 
         mGestureSelections.put(Gesture.LEFT, 4);        // Black
         mGestureSelections.put(Gesture.LEFT_UP, 5);     // Red
         mGestureSelections.put(Gesture.LEFT_RIGHT, 6);  // Green
         mGestureSelections.put(Gesture.LEFT_DOWN, 7);   // Blue
         
         mGestureSelections.put(Gesture.RIGHT, 8);       // White
         mGestureSelections.put(Gesture.RIGHT_DOWN, 9);  // Yellow
         mGestureSelections.put(Gesture.RIGHT_LEFT, 10); // Cyan
         mGestureSelections.put(Gesture.RIGHT_UP, 11);   // Magenta
         
         mGestureSelections.put(Gesture.DOWN_LEFT, 12);  // Fine
         mGestureSelections.put(Gesture.DOWN_UP, 13);    // Thin
         mGestureSelections.put(Gesture.DOWN, 14);       // Normal
         mGestureSelections.put(Gesture.DOWN_RIGHT, 15); // Wide
         
         // Default to thin black paintbrush
         changeSelection(0, false);
         changeSelection(4, false);
         changeSelection(13, false);
         
         Timer timer = new Timer();
         timer.schedule(new TimerTask() {
         	@Override
         	public void run() {
         		long now = System.nanoTime();
         		
         		if (mUI == UI.CHORD) {
 	        		synchronized (mFlashTimes) {
 		        		for (int i = 0; i < mFlashTimes.size(); i++) {
 		        			long time = mFlashTimes.valueAt(i);
 			        		if (now - time > mFlashDelay) {
 			        			mFlashTimes.removeAt(i);
 			        			postInvalidate();
 			        		}
 		        		}
 	        		}
 	        		
 	        		if (mFingerInside != -1 && now - mPressedInsideTime > mChordDelay && mCheckOverlay && !mShowOverlay) {
 	        			mOverlayStart = now;
 	        			mShowOverlay = true;
 	        			mLog.event("Overlay shown");
 	        			mTool.clearFingers();
 	        			postInvalidate();
 	        			
 	        			if (mStudyMode)
 	        				mStudyCtl.handleOverlayShown();
 	        		}
         		} else if (mUI == UI.GESTURE) {
         			if (mPossibleGestureFinger != -1 && now - mPossibleGestureFingerTime > mGestureMenuDelay && !mChanged) {
         				mGestureFinger = mPossibleGestureFinger;
         				mIgnoredFingers.add(mGestureFinger);
         				mPossibleGestureFinger = -1;
         				mShowGestureMenu = true;
         				postInvalidate();
         			}
         		}
         	}
         }, 25, 25);
 
         final CheckBox studyCheckBox = new CheckBox(mainActivity);
         studyCheckBox.setText("Run in study mode");
         studyCheckBox.setChecked(true);
 
         final CheckBox gestureCheckBox = new CheckBox(mainActivity);
         gestureCheckBox.setText("Use the gesture interface");
         
         final NumberPicker subjectIdPicker = new NumberPicker(mainActivity);
         subjectIdPicker.setMinValue(0);
         subjectIdPicker.setMaxValue(999);
         subjectIdPicker.setWrapSelectorWheel(false);
         
         // Prevent the keyboard from popping up.
         subjectIdPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
         
         LinearLayout studySetupView = new LinearLayout(mainActivity);
         studySetupView.setOrientation(LinearLayout.VERTICAL);
         studySetupView.addView(studyCheckBox);
         studySetupView.addView(gestureCheckBox);
         studySetupView.addView(subjectIdPicker);
         
         new AlertDialog.Builder(mainActivity)
         	.setMessage(R.string.dialog_study_mode)
         	.setCancelable(false)
         	.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
         		public void onClick(DialogInterface dialog, int which) {
         			mStudyMode = studyCheckBox.isChecked();
         			
         			if (mStudyMode) {
         				mStudyCtl = new StudyController(mLog);
         				mMainActivity.setTitle(mStudyCtl.getPrompt());
         			}
         			
         			mLog.setSubjectId(subjectIdPicker.getValue());
         			mUI = gestureCheckBox.isChecked() ? UI.GESTURE : UI.CHORD;
         			DrawView.this.invalidate();
         		}
         	})
         	.setView(studySetupView)
         	.show();
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
 	
 	private RectF getButtonBounds(int index) {
 		int y = index / mCols;
 		int x = index % mCols;
 		
 		if (mLeftHanded)
 			x = mCols - x - 1;
 		
 		float top = mRowHeight * y;
 		float bottom = top + mRowHeight;
 		float left = mColWidth * x;
 		float right = left + mColWidth;
 		
 		return new RectF(left, top, right, bottom);
 	}
 	
 	private RectF getOverlayButtonBounds() {
 		return getButtonBounds(mOverlayButtonIndex);
 	}
 
     @Override
     protected void onDraw(Canvas canvas) {
     	RectF bounds = getOverlayButtonBounds();
     	
         canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
         mTool.draw(canvas);
         
         if (mShowOverlay)
         	canvas.drawARGB(0xAA, 0xFF, 0xFF, 0xFF);
         
         if (mUI == UI.CHORD) {
 	    	mPaint.setColor(0x88FFFF00);
 	    	canvas.drawRect(bounds, mPaint);
         }
         
         if (mShowOverlay || (mPermanentGrid && mUI == UI.CHORD)) {
         	mPaint.setColor(0x44666666);
 
         	for (int i = 0; i < mRows; i++) {
         		float top = i * mRowHeight;    		
         		canvas.drawLine(0, top, mColWidth * mCols, top, mPaint);
         	}
         	for (int i = 0; i < mCols; i++) {
         		float left = i * mColWidth;    		
         		canvas.drawLine(left, 0, left, mRowHeight * mRows, mPaint);
         	}
         }
         
         if (mShowOverlay) {
     		mPaint.setColor(0xFF000000);
         	for (int y = 0; y < mRows; y++) {
         		for (int x = 0; x < mCols; x++) {
         			int realX = x;
         			if (mLeftHanded)
         				realX = mCols - x - 1;
         			
         			int i = y * mCols + realX;
         			if (mSelections[i] != null) {
         				String name = mSelections[i].name;
         				int heightAdj = getTextHeight(name, mPaint) / 2;
         				canvas.drawText(name, (x + 0.5f) * mColWidth, (y + 0.5f) * mRowHeight + heightAdj, mPaint);
         			}
         		}
         	}
         } else if (!mPermanentGrid && mUI == UI.CHORD) {
     		mPaint.setColor(0x44666666);
         	canvas.drawLine(bounds.left, bounds.top, bounds.right, bounds.top, mPaint);
         	
         	if (mLeftHanded)
         		canvas.drawLine(bounds.left, bounds.top, bounds.left, bounds.bottom, mPaint);
         	else
         		canvas.drawLine(bounds.right, bounds.top, bounds.right, bounds.bottom, mPaint);
         }
         
         if (mUI == UI.CHORD) { 
 	        synchronized (mFlashTimes) {
 	        	for (int i = 0; i < mFlashTimes.size(); i++) {
 	        		int selectionNum = mFlashTimes.keyAt(i);
 	    	        Selection selection = mSelections[selectionNum];
 	    	        if (selection != null) {
 	    	        	RectF buttonBounds = getButtonBounds(selectionNum);
 	    	        	
 	    	        	mPaint.setColor(0xBBF5F5F5);
 	    	        	canvas.drawRect(buttonBounds, mPaint);
 	    	        	
 	    	        	mPaint.setColor(0x44666666);
 	    	        	mPaint.setStyle(Style.STROKE);
 	    	        	canvas.drawRect(buttonBounds, mPaint);
 	    	        	mPaint.setStyle(Style.FILL);
 	    	        	
 	    	    		mPaint.setColor(0xFF000000);
 	    				String name = selection.name;
 	    				int heightAdj = getTextHeight(name, mPaint) / 2;
 	    				canvas.drawText(name, buttonBounds.left + 0.5f * mColWidth, buttonBounds.top + 0.5f * mRowHeight + heightAdj, mPaint);
 	    	        }
 	            }
 			}
         }
         
 		mPaint.setColor(0xFF666666);
 		
 		if (mUI == UI.CHORD) {
 	        canvas.drawText(mThicknessName, bounds.left + mColWidth / 2,
 	        		bounds.top + mRowHeight / 2 + getTextHeight(mThicknessName, mPaint) / 2 - 30, mPaint);
 	        canvas.drawText(mColorName, bounds.left + mColWidth / 2,
 	        		bounds.top + mRowHeight / 2 + getTextHeight(mColorName, mPaint) / 2, mPaint);
 	        canvas.drawText(mToolName, bounds.left + mColWidth / 2,
 	        		bounds.top + mRowHeight / 2 + getTextHeight(mToolName, mPaint) / 2 + 30, mPaint);
 		} else if (mUI == UI.GESTURE) {
 			String tools = mThicknessName + " " + mColorName + " " + mToolName;
 			int padding = 10;
 			mPaint.setColor(0xBBAAAAAA);
 			canvas.drawRect(0, getHeight() - getTextHeight(tools, mPaint) - padding * 2, getTextWidth(tools, mPaint) + padding * 2, getHeight(), mPaint);
 			mPaint.setColor(0xFF444444);
 	        canvas.drawText(tools, getTextWidth(tools, mPaint) / 2 + padding, getHeight() - padding, mPaint);
 		}
         
         if (mShowGestureMenu) {
         	PointF origin = mOrigins.get(mGestureFinger);
 
         	if (isInCircle(mGestureFingerPos, origin.x, origin.y - mGestureButtonDist, mGestureButtonSize)) {
         		mActiveCategoryOrigin.x = origin.x;
         		mActiveCategoryOrigin.y = origin.y - mGestureButtonDist;
         		mActiveCategory = Gesture.UP;
         	} else if (isInCircle(mGestureFingerPos, origin.x - mGestureButtonDist, origin.y, mGestureButtonSize)) {
         		mActiveCategoryOrigin.x = origin.x - mGestureButtonDist;
         		mActiveCategoryOrigin.y = origin.y;
         		mActiveCategory = Gesture.LEFT;
         	} else if (isInCircle(mGestureFingerPos, origin.x + mGestureButtonDist, origin.y, mGestureButtonSize)) {
         		mActiveCategoryOrigin.x = origin.x + mGestureButtonDist;
         		mActiveCategoryOrigin.y = origin.y;
         		mActiveCategory = Gesture.RIGHT;
         	} else if (isInCircle(mGestureFingerPos, origin.x, origin.y + mGestureButtonDist, mGestureButtonSize)) {
         		mActiveCategoryOrigin.x = origin.x;
         		mActiveCategoryOrigin.y = origin.y + mGestureButtonDist;
         		mActiveCategory = Gesture.DOWN;
         	}
         	
         	boolean greyout = mActiveCategory != Gesture.UNKNOWN;
         	
         	mPaint.setTextSize(22);
         	int size = mGestureButtonSize;
         	
         	drawGestureButton(canvas, "Tools", origin.x, origin.y - mGestureButtonDist, size, mPaint, mActiveCategory == Gesture.UP, greyout);
         	drawGestureButton(canvas, "Colors", origin.x - mGestureButtonDist, origin.y, size, mPaint, mActiveCategory == Gesture.LEFT, greyout);
         	drawGestureButton(canvas, "Colors", origin.x + mGestureButtonDist, origin.y, size, mPaint, mActiveCategory == Gesture.RIGHT, greyout);
         	drawGestureButton(canvas, "Widths", origin.x, origin.y + mGestureButtonDist, size, mPaint, mActiveCategory == Gesture.DOWN, greyout);
         	
         	mPaint.setTextSize(18);
         	int subSize = (int)(size * 0.70);
         	int subDist = size + subSize;
         	float subOriginX = mActiveCategoryOrigin.x;
         	float subOriginY = mActiveCategoryOrigin.y;
 
         	if (isInCircle(mGestureFingerPos, subOriginX, subOriginY - subDist, subSize)) {
         		mSubSelection = Gesture.UP;
         	} else if (isInCircle(mGestureFingerPos, subOriginX - subDist, subOriginY, subSize)) {
         		mSubSelection = Gesture.LEFT;
         	} else if (isInCircle(mGestureFingerPos, subOriginX + subDist, subOriginY, subSize)) {
         		mSubSelection = Gesture.RIGHT;
         	} else if (isInCircle(mGestureFingerPos, subOriginX, subOriginY + subDist, subSize)) {
         		mSubSelection = Gesture.DOWN;
         	} else {
         		mSubSelection = Gesture.UNKNOWN;
         	}
         	
         	switch (mActiveCategory) {
 	        	case UP:
 	            	drawGestureButton(canvas, "Paintbrush", subOriginX, subOriginY - subDist, subSize, mPaint, mSubSelection == Gesture.UP, false);
 	            	drawGestureButton(canvas, "Rectangle", subOriginX - subDist, subOriginY, subSize, mPaint, mSubSelection == Gesture.LEFT, false);
 	            	drawGestureButton(canvas, "Line", subOriginX + subDist, subOriginY, subSize, mPaint, mSubSelection == Gesture.RIGHT, false);
 	            	drawGestureButton(canvas, "Circle", subOriginX, subOriginY + subDist, subSize, mPaint, mSubSelection == Gesture.DOWN, false);
 	        		break;
 	        		
 	        	case LEFT:
 	            	drawGestureButton(canvas, "Red", subOriginX, subOriginY - subDist, subSize, mPaint, mSubSelection == Gesture.UP, false);
 	            	drawGestureButton(canvas, "Black", subOriginX - subDist, subOriginY, subSize, mPaint, mSubSelection == Gesture.LEFT, false);
 	            	drawGestureButton(canvas, "Green", subOriginX + subDist, subOriginY, subSize, mPaint, mSubSelection == Gesture.RIGHT, false);
 	            	drawGestureButton(canvas, "Blue", subOriginX, subOriginY + subDist, subSize, mPaint, mSubSelection == Gesture.DOWN, false);
 	        		break;
 	        		
 	        	case RIGHT:
 	            	drawGestureButton(canvas, "Magenta", subOriginX, subOriginY - subDist, subSize, mPaint, mSubSelection == Gesture.UP, false);
 	            	drawGestureButton(canvas, "Cyan", subOriginX - subDist, subOriginY, subSize, mPaint, mSubSelection == Gesture.LEFT, false);
 	            	drawGestureButton(canvas, "White", subOriginX + subDist, subOriginY, subSize, mPaint, mSubSelection == Gesture.RIGHT, false);
 	            	drawGestureButton(canvas, "Yellow", subOriginX, subOriginY + subDist, subSize, mPaint, mSubSelection == Gesture.DOWN, false);
 	        		break;
 	        		
 	        	case DOWN:
 	            	drawGestureButton(canvas, "Thin", subOriginX, subOriginY - subDist, subSize, mPaint, mSubSelection == Gesture.UP, false);
 	            	drawGestureButton(canvas, "Fine", subOriginX - subDist, subOriginY, subSize, mPaint, mSubSelection == Gesture.LEFT, false);
 	            	drawGestureButton(canvas, "Wide", subOriginX + subDist, subOriginY, subSize, mPaint, mSubSelection == Gesture.RIGHT, false);
 	            	drawGestureButton(canvas, "Normal", subOriginX, subOriginY + subDist, subSize, mPaint, mSubSelection == Gesture.DOWN, false);
 	        		break;
 	        		
 				default:
 					break;
         	}
         	
         	mPaint.setTextSize(26);
         }
     }
     
     private boolean isInCircle(PointF point, float cx, float cy, float radius) {
     	float dx = point.x - cx;
     	float dy = point.y - cy;
     	double distance = Math.sqrt(dx*dx + dy*dy);
     	
     	return distance < radius;
     }
     
     private void drawGestureButton(Canvas canvas, String text, float x, float y, int size, Paint paint, boolean highlight, boolean greyout) {
 		paint.getTextBounds(text, 0, text.length(), mTextBounds);
 		
 		if (highlight)
 			mPaint.setColor(0xFFAAAAAA);
 		else if (greyout)
 			mPaint.setColor(0xFFDDDDDD);
 		else
 			mPaint.setColor(0xFFCCCCCC);
 		
 		canvas.drawCircle(x, y, size, paint);
 
 		if (greyout && !highlight) {
 			mPaint.setColor(0xEE777777);
 		} else {
 			mPaint.setColor(0xEE000000);
 			mPaint.setShadowLayer(2, 1, 1, 0x33000000);
 		}
 			
     	canvas.drawText(text, x, y + mTextBounds.height() / 2, mPaint);
     	mPaint.setShadowLayer(0, 0, 0, 0);
     }
     
     private int getTextHeight(String text, Paint paint) {
 		mPaint.getTextBounds(text, 0, text.length(), mTextBounds);
 		return mTextBounds.height();
     }
     
     private int getTextWidth(String text, Paint paint) {
 		mPaint.getTextBounds(text, 0, text.length(), mTextBounds);
 		return mTextBounds.width();
     }
     
     @Override
     public boolean onTouchEvent(MotionEvent event) {
     	int index = event.getActionIndex();
         float x = event.getX(index);
         float y = event.getY(index);
         int id = event.getPointerId(index);
     	long now = System.nanoTime();
 
         switch (event.getActionMasked()) {
             case MotionEvent.ACTION_DOWN:
             case MotionEvent.ACTION_POINTER_DOWN:
             	mLog.event("Touch down: " + id);
             	
             	mFingers.add(id);
             	
             	if (mUI == UI.CHORD) { 
 	            	if (event.getPointerCount() == 1)
 	            		mCheckOverlay = true;
 	            	
 	            	if (getOverlayButtonBounds().contains(x, y)) {
 	            		mFingerInside = id;
 	            		mPressedInsideTime = now;
 	            		mIgnoredFingers.add(mFingerInside);
 	            	} else {
 	            		int col = (int) (x / mColWidth);
 	            		int row = (int) (y / mRowHeight);
 	            		
 	            		if (mLeftHanded)
 	            			col = mCols - col - 1;
 	            		
 	            		mSelected = row * mCols + col;
 	                	mRecentTouches.put(mSelected, now);
 	            	}
 	            	
 	            	for (int i = 0; i < mRecentTouches.size(); i++) {
 	            		int selection = mRecentTouches.keyAt(i);
 	            		long time = mRecentTouches.valueAt(i);
 	            		
 	            		if ((now - time < mChordDelay && now - mPressedInsideTime < mChordDelay) || mShowOverlay) {
 	            			changeSelection(selection);
 	            			mCheckOverlay = false;
 	            			mRecentTouches.removeAt(i);
 	            			i--;
 	            		} else if (now - time > mChordDelay) {
 	            			mRecentTouches.removeAt(i);
 	            			i--;
 	            		}
 	            	}
             	} else if (mUI == UI.GESTURE) {
             		if (event.getPointerCount() == 1) {
                     	mGestureDetector.clear();
                     	mPossibleGestureFinger = id;
                     	mGestureFingerPos = new PointF(x, y);
                     	mPossibleGestureFingerTime = now;
             		} else {
             			mPossibleGestureFinger = -1;
             			mGestureFinger = -1;
             			mShowGestureMenu = false;
            			mActiveCategory = Gesture.UNKNOWN;
             		}
             	}
             	
             	if (!mShowOverlay) {
 	            	mOrigins.put(id, new PointF(x, y));
 	            	mTool.touchStart(id, x, y);
             	}
                 break;
                 
             case MotionEvent.ACTION_MOVE:
             	if (mShowOverlay)
             		break;
             	
             	int count = event.getPointerCount();
             	
             	for (int i = 0; i < count; i++) {
             		int fingerId = event.getPointerId(i);
             		
             		if (fingerId == mGestureFinger) {
             			mGestureFingerPos = new PointF(x, y);
                     	mGestureDetector.addPoint(x, y);
             		}
             		
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
                     	
                         if (!mChanged) {
                         	mChanged = true;
                         	mNextUndo = mBitmap.copy(mBitmap.getConfig(), true);
                         }
                     }
             	}
             	
                 break;
                 
             case MotionEvent.ACTION_UP:
             case MotionEvent.ACTION_POINTER_UP:
             	mLog.event("Touch up: " + id);
 
             	mOrigins.delete(id);
                 mFingers.remove(id);
 
             	if (id == mFingerInside)
             		mFingerInside = -1;
             	
             	if (id == mPossibleGestureFinger)
             		mPossibleGestureFinger = -1;
 
         		boolean draw = true;
         		
             	if (id == mGestureFinger) {
             		mGestureFinger = -1;
             		mShowGestureMenu = false;
             		
             		if (mActiveCategory != Gesture.UNKNOWN && mSubSelection != Gesture.UNKNOWN) {
             			switch (mActiveCategory) {
             				case UP:
             					switch (mSubSelection) {
             						case UP: mGesture = Gesture.UP; break;
             						case LEFT: mGesture = Gesture.UP_LEFT; break;
             						case RIGHT: mGesture = Gesture.UP_RIGHT; break;
             						case DOWN: mGesture = Gesture.UP_DOWN; break;
             						default: break;
             					}
             					break;
             					
             				case LEFT:
             					switch (mSubSelection) {
             						case UP: mGesture = Gesture.LEFT_UP; break;
             						case LEFT: mGesture = Gesture.LEFT; break;
             						case RIGHT: mGesture = Gesture.LEFT_RIGHT; break;
             						case DOWN: mGesture = Gesture.LEFT_DOWN; break;
             						default: break;
             					}
             					break;
 
             				case RIGHT:
             					switch (mSubSelection) {
             						case UP: mGesture = Gesture.RIGHT_UP; break;
             						case LEFT: mGesture = Gesture.RIGHT_LEFT; break;
             						case RIGHT: mGesture = Gesture.RIGHT; break;
             						case DOWN: mGesture = Gesture.RIGHT_DOWN; break;
             						default: break;
             					}
             					break;
 
             				case DOWN:
             					switch (mSubSelection) {
             						case UP: mGesture = Gesture.DOWN_UP; break;
             						case LEFT: mGesture = Gesture.DOWN_LEFT; break;
             						case RIGHT: mGesture = Gesture.DOWN_RIGHT; break;
             						case DOWN: mGesture = Gesture.DOWN; break;
             						default: break;
             					}
             					break;
             					
             				default:
             					break;
             			}
             		} else {
                 		mGesture = mGestureDetector.recognize();
             		}
 
             		if (mGestureSelections.containsKey(mGesture))
             			changeSelection(mGestureSelections.get(mGesture));
 
         			mActiveCategory = Gesture.UNKNOWN;
             		draw = false;
             	}
         		
             	if (mIgnoredFingers.contains(id)) {
             		mIgnoredFingers.remove(id);
             		draw = false;
             	}
             	
             	if (mShowOverlay) {
             		if (event.getPointerCount() == 1) {
             			mShowOverlay = false;
             			long duration = now - mOverlayStart;
             			mLog.event("Overlay hidden after " + duration / 1000000 + " ms");
             		}
             	} else if (draw) {
                     if (event.getPointerCount() == 1 && mChanged)
                     	mUndo = mNextUndo;
                     
             		mTool.touchStop(id, x, y, new Canvas(mBitmap));
             	}
 
             	if (event.getPointerCount() == 1)
             		mChanged = false;
         		
                 break;
         }
 
         invalidate();
         return true;
     }
     
     private void changeSelection(int selected) {
     	changeSelection(selected, true);
     }
     
     private void changeSelection(int selected, boolean fromUser) {
 		if (mTool != null)
 			mTool.clearFingers();
 		
 		for (int id : mFingers) {
 			mIgnoredFingers.add(id);
 		}
 		
 		mOrigins.clear();
 		
     	Selection selection = mSelections[selected];
     	
     	if (selection == null)
     		return;
 		
 		if (fromUser) {
 			synchronized (mFlashTimes) {
 				mFlashTimes.put(selected, System.nanoTime());
 			}
 			
 			invalidate();
 		}
     	
     	switch (selection.type) {
     		case TOOL:
     			if (fromUser)
     				mLog.event("Tool selected: " + selection.name);
     			
     			mTool = (Tool) selection.object;
     			mToolName = selection.name;
     			break;
     			
     		case COLOR:
     			if (fromUser)
     				mLog.event("Color selected: " + selection.name);
     			
     			mColor = (Integer) selection.object;
     			mColorName = selection.name;
     			break;
     			
     		case THICKNESS:
     			if (fromUser)
     				mLog.event("Thickness selected: " + selection.name);
     			
     			mThickness = (Integer) selection.object;
     			mThicknessName = selection.name;
     			break;
     			
     		case ACTION:
     			if (fromUser)
     				mLog.event("Action selected: " + selection.name);
     			
     			switch ((Action) selection.object) {
 					case SAVE:
 						break;
 				
     				case CLEAR:
     					mUndo = mBitmap.copy(mBitmap.getConfig(), true);
     			        Canvas canvas = new Canvas(mBitmap);
     			        canvas.drawRGB(0xFF, 0xFF, 0xFF);
     					break;
     					
     				case UNDO:
     					if (mUndo != null) {
         					Bitmap temp = mBitmap;
         					mBitmap = mUndo;
         					mUndo = temp;
         				}
     					break;
     			}
     			break;
     	}
     	
     	if (fromUser && mStudyMode) {
 	    	mStudyCtl.handleSelected(selection.name);
 	    	mMainActivity.setTitle(mStudyCtl.getPrompt());
     	}
     }
 
 	public void loadPreferences(SharedPreferences sharedPreferences) {
 		mLeftHanded = sharedPreferences.getBoolean("pref_left_handed", false);
 		mPermanentGrid = sharedPreferences.getBoolean("pref_permanent_grid", false);
 		invalidate();
 	}
 }
