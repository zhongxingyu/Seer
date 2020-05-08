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
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Paint.Align;
 import android.graphics.Paint.Style;
 import android.graphics.PointF;
 import android.graphics.Rect;
 import android.graphics.RectF;
 import android.os.Handler;
 import android.util.Log;
 import android.util.SparseArray;
 import android.view.MotionEvent;
 import android.view.View;
 import android.widget.CheckBox;
 import android.widget.CompoundButton;
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
     private int mPossibleGestureFinger;
     private long mPossibleGestureFingerTime;
     private int mGestureFinger;
     private long mGestureMenuTime;
     private PointF mGestureFingerPos;
     private boolean mInstantMenu;
     private boolean mShowGestureMenu;
     private Gesture mActiveCategory;
     private PointF mActiveCategoryOrigin;
     private Gesture mSubSelection;
     private UI mUI;
     private final Handler mHandler = new Handler();
     private static final int mChordDelay = 1000 * 1000 * 200; // 200 ms in ns
     private static final int mFlashDelay = 1000 * 1000 * 400; // 400 ms in ns
     private static final int mGestureMenuDelay = 1000 * 1000 * 200; // 200 ms in ns
     private static final int mTrialDelay = 500; // 500 ms
     private static final int mBlockDelay = 1000; // 1 sec
     private static final int mOverlayButtonIndex = 16;
     private static final int mGestureButtonDist = 150;
     private static final int mGestureButtonSize = 75;
     
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
         mStudyCtl = new StudyController(mLog);
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
         mGestureFinger = -1;
         mPossibleGestureFinger = -1;
         mInstantMenu = false;
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
                     }
                 } else if (mUI == UI.GESTURE) {
                     if (mPossibleGestureFinger != -1 && now - mPossibleGestureFingerTime > mGestureMenuDelay && !mChanged) {
                         mGestureFinger = mPossibleGestureFinger;
                         mIgnoredFingers.add(mGestureFinger);
                         mPossibleGestureFinger = -1;
                         mShowGestureMenu = true;
                         mGestureMenuTime = now;
                         postInvalidate();
                     }
                 }
             }
         }, 25, 25);
         
         View studySetupLayout = mMainActivity.getLayoutInflater().inflate(R.layout.study_setup, null);
         final CheckBox studyCheckBox = (CheckBox) studySetupLayout.findViewById(R.id.study_mode_checkbox);
         final CheckBox gestureCheckBox = (CheckBox) studySetupLayout.findViewById(R.id.gesture_mode_checkbox);
         final CheckBox leftHandedCheckBox = (CheckBox) studySetupLayout.findViewById(R.id.left_handed_checkbox);
         final CheckBox permanentGridCheckBox = (CheckBox) studySetupLayout.findViewById(R.id.permanent_grid_checkbox);
         
         gestureCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
             @Override
             public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                 leftHandedCheckBox.setEnabled(!isChecked);
                 permanentGridCheckBox.setEnabled(!isChecked);
             }
         });
         
         final NumberPicker subjectIdPicker = (NumberPicker) studySetupLayout.findViewById(R.id.subject_id_picker);
         subjectIdPicker.setMinValue(0);
         subjectIdPicker.setMaxValue(99);
         subjectIdPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS); // Remove the virtual keyboard
         
         final NumberPicker setNumPicker = (NumberPicker) studySetupLayout.findViewById(R.id.set_num_picker);
         setNumPicker.setMinValue(1);
         setNumPicker.setMaxValue(mStudyCtl.getNumSets());
         setNumPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS); // Remove the virtual keyboard
         
         final NumberPicker blockNumPicker = (NumberPicker) studySetupLayout.findViewById(R.id.block_num_picker);
         blockNumPicker.setMinValue(1);
         blockNumPicker.setMaxValue(mStudyCtl.getNumBlocks(1));
         blockNumPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS); // Remove the virtual keyboard
         
         setNumPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
             @Override
             public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                 blockNumPicker.setMaxValue(mStudyCtl.getNumBlocks(newVal));
             }
         });
 
         new AlertDialog.Builder(mainActivity)
             .setMessage(R.string.dialog_study_mode)
             .setCancelable(false)
             .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int which) {
                     mStudyMode = studyCheckBox.isChecked();
                     
                     if (mStudyMode) {
                         mStudyCtl.setSetNum(setNumPicker.getValue());
                         mStudyCtl.setBlockNum(blockNumPicker.getValue());
                         mMainActivity.setTitle("Your targets will appear here.");
                         pauseStudy("Press OK when you are ready to begin.");
                     }
                     
                     mLog.setSubjectId(subjectIdPicker.getValue());
                     mUI = gestureCheckBox.isChecked() ? UI.GESTURE : UI.CHORD;
                     mLeftHanded = leftHandedCheckBox.isChecked();
                     mPermanentGrid = permanentGridCheckBox.isChecked();
                     DrawView.this.invalidate();
                 }
             })
             .setView(studySetupLayout)
             .show();
 
         mMainActivity.getActionBar().setIcon(R.drawable.trans);
     }
     
     public void pauseStudy(String message) {
         new AlertDialog.Builder(mMainActivity)
             .setMessage(message)
             .setCancelable(false)
             .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int which) {
                     Runnable waitStep = new Runnable() {
                         @Override
                         public void run() {
                             mStudyCtl.waitStep(false);
                             mMainActivity.setTitle(mStudyCtl.getPrompt());
                         }
                     };
                     
                     mStudyCtl.hideTargets();
                     waitStep.run();
                     mHandler.postDelayed(waitStep, mBlockDelay / 4);
                     mHandler.postDelayed(waitStep, mBlockDelay / 2);
                     mHandler.postDelayed(waitStep, mBlockDelay * 3 / 4);
                     mHandler.postDelayed(waitStep, mBlockDelay);
                 }
             })
             .show();
     }
     
     public void alert(String message) {
         new AlertDialog.Builder(mMainActivity)
             .setMessage(message)
             .setCancelable(false)
             .setPositiveButton(android.R.string.yes, null)
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
         
         mPaint.setColor(0xEEFFFFAA);
         canvas.drawRect(bounds, mPaint);
         
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
         } else if (!mPermanentGrid) {
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
         
         canvas.drawText(mThicknessName, bounds.left + mColWidth / 2,
                 bounds.top + mRowHeight / 2 + getTextHeight(mThicknessName, mPaint) / 2 - 30, mPaint);
         canvas.drawText(mColorName, bounds.left + mColWidth / 2,
                 bounds.top + mRowHeight / 2 + getTextHeight(mColorName, mPaint) / 2, mPaint);
         canvas.drawText(mToolName, bounds.left + mColWidth / 2,
                 bounds.top + mRowHeight / 2 + getTextHeight(mToolName, mPaint) / 2 + 30, mPaint);
         
         if (mShowGestureMenu) {
             PointF origin = mOrigins.get(mGestureFinger);
             Gesture gesture = mGestureDetector.recognize();
             Gesture mainGesture = Gesture.UNKNOWN;
             Gesture subGesture = Gesture.UNKNOWN;
             
             if (gesture != Gesture.UNKNOWN) {
                 switch (gesture) {
                     case UP: mainGesture = Gesture.UP; subGesture = Gesture.UP; break;
                     case UP_LEFT: mainGesture = Gesture.UP; subGesture = Gesture.LEFT; break;
                     case UP_RIGHT: mainGesture = Gesture.UP; subGesture = Gesture.RIGHT; break;
                     case UP_DOWN: mainGesture = Gesture.UP; subGesture = Gesture.DOWN; break;
                     
                     case LEFT: mainGesture = Gesture.LEFT; subGesture = Gesture.LEFT; break;
                     case LEFT_RIGHT: mainGesture = Gesture.LEFT; subGesture = Gesture.RIGHT; break;
                     case LEFT_UP: mainGesture = Gesture.LEFT; subGesture = Gesture.UP; break;
                     case LEFT_DOWN: mainGesture = Gesture.LEFT; subGesture = Gesture.DOWN; break;
                     
                     case RIGHT: mainGesture = Gesture.RIGHT; subGesture = Gesture.RIGHT; break;
                     case RIGHT_LEFT: mainGesture = Gesture.RIGHT; subGesture = Gesture.LEFT; break;
                     case RIGHT_UP: mainGesture = Gesture.RIGHT; subGesture = Gesture.UP; break;
                     case RIGHT_DOWN: mainGesture = Gesture.RIGHT; subGesture = Gesture.DOWN; break;
                     
                     case DOWN: mainGesture = Gesture.DOWN; subGesture = Gesture.DOWN; break;
                     case DOWN_LEFT: mainGesture = Gesture.DOWN; subGesture = Gesture.LEFT; break;
                     case DOWN_RIGHT: mainGesture = Gesture.DOWN; subGesture = Gesture.RIGHT; break;
                     case DOWN_UP: mainGesture = Gesture.DOWN; subGesture = Gesture.UP; break;
                     
                     default:
                         break;
                 }
             }
 
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
             } else if (mainGesture == mActiveCategory) {
                 mSubSelection = subGesture;
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
                     if (mInstantMenu && !getOverlayButtonBounds().contains(x, y) && !mShowGestureMenu) {
                         mInstantMenu = false;
                         mGestureFinger = id;
                         mGestureMenuTime = now;
                         mShowGestureMenu = true;
                         mIgnoredFingers.add(id);
                         mGestureFingerPos = new PointF(x, y);
                         mOrigins.put(id, mGestureFingerPos);
                         mGestureDetector.clear();
                     } else if (event.getPointerCount() == 1) {
                         mGestureDetector.clear();
                         
                         if (getOverlayButtonBounds().contains(x, y)) {
                             mIgnoredFingers.add(id);
                             mInstantMenu = true;
                         } else {
                             mPossibleGestureFinger = id;
                             mGestureFingerPos = new PointF(x, y);
                             mPossibleGestureFingerTime = now;
                         }
                     }
                     
                     if (mShowGestureMenu)
                         mIgnoredFingers.add(id);
                 }
                 
                 if (!mShowOverlay && !mShowGestureMenu) {
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
                    float x2 = event.getX(i);
                    float y2 = event.getY(i);
                     
                     if (fingerId == mGestureFinger) {
                        mGestureFingerPos = new PointF(x2, y2);
                        mGestureDetector.addPoint(x2, y2);
                     }
                     
                     if(mIgnoredFingers.contains(fingerId))
                         continue;
                     
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
                     boolean gestureSelection;
                     Gesture gesture = Gesture.UNKNOWN;
                     
                     if (mActiveCategory != Gesture.UNKNOWN && mSubSelection != Gesture.UNKNOWN) {
                         gestureSelection = false;
                         
                         switch (mActiveCategory) {
                             case UP:
                                 switch (mSubSelection) {
                                     case UP: gesture = Gesture.UP; break;
                                     case LEFT: gesture = Gesture.UP_LEFT; break;
                                     case RIGHT: gesture = Gesture.UP_RIGHT; break;
                                     case DOWN: gesture = Gesture.UP_DOWN; break;
                                     default: break;
                                 }
                                 break;
                                 
                             case LEFT:
                                 switch (mSubSelection) {
                                     case UP: gesture = Gesture.LEFT_UP; break;
                                     case LEFT: gesture = Gesture.LEFT; break;
                                     case RIGHT: gesture = Gesture.LEFT_RIGHT; break;
                                     case DOWN: gesture = Gesture.LEFT_DOWN; break;
                                     default: break;
                                 }
                                 break;
 
                             case RIGHT:
                                 switch (mSubSelection) {
                                     case UP: gesture = Gesture.RIGHT_UP; break;
                                     case LEFT: gesture = Gesture.RIGHT_LEFT; break;
                                     case RIGHT: gesture = Gesture.RIGHT; break;
                                     case DOWN: gesture = Gesture.RIGHT_DOWN; break;
                                     default: break;
                                 }
                                 break;
 
                             case DOWN:
                                 switch (mSubSelection) {
                                     case UP: gesture = Gesture.DOWN_UP; break;
                                     case LEFT: gesture = Gesture.DOWN_LEFT; break;
                                     case RIGHT: gesture = Gesture.DOWN_RIGHT; break;
                                     case DOWN: gesture = Gesture.DOWN; break;
                                     default: break;
                                 }
                                 break;
                                 
                             default:
                                 break;
                         }
                     } else {
                         gestureSelection = true;
                         gesture = mGestureDetector.recognize();
                     }
 
                     long menuOpenNs = now - mGestureMenuTime;
                     long menuOpenMs = menuOpenNs / 1000000;
                     
                     if (mStudyMode)
                         mStudyCtl.addUITime(menuOpenNs);
                     
                     if (mGestureSelections.containsKey(gesture)) {
                         if (gestureSelection)
                             mLog.event("Menu closed with gesture selection: " + menuOpenMs + " ms");
                         else
                             mLog.event("Menu closed with exact selection: " + menuOpenMs + " ms");
                         
                         changeSelection(mGestureSelections.get(gesture));
                     } else {
                         mLog.event("Menu closed without selection: " + menuOpenMs + " ms");
                     }
 
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
                         
                         if (mStudyMode) {
                             if (mOverlayStart > mStudyCtl.getTrialStart())
                                 mStudyCtl.addUITime(duration);
                             else
                                 mStudyCtl.addUITime(now - mStudyCtl.getTrialStart());
                         }
                     }
                 } else if (draw) {
                     if (event.getPointerCount() == 1 && mChanged) {
                         mStudyCtl.incrementTimesPainted();
                         mUndo = mNextUndo;
                     }
                     
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
             boolean gesture = mUI == UI.GESTURE;
             boolean wasLastTarget = mStudyCtl.isOnLastTarget();
             boolean wasLastTrial = mStudyCtl.isOnLastTrial();
             
             if (mUI == UI.CHORD && mShowOverlay && wasLastTarget) {
                 long now = System.nanoTime();
                 
                 if (mOverlayStart > mStudyCtl.getTrialStart())
                     mStudyCtl.addUITime(now - mOverlayStart);
                 else
                     mStudyCtl.addUITime(now - mStudyCtl.getTrialStart());
             }
             
             boolean correctSelection = mStudyCtl.handleSelected(selection.name, gesture);
             
             if (mStudyCtl.isFinished()) {
                 mMainActivity.getActionBar().setIcon(R.drawable.trans);
                 mMainActivity.setTitle(mStudyCtl.getPrompt());
                 
                 if (wasLastTarget)
                     alert("You are finished!\n\nThank you for participating!");
             } else if (correctSelection && wasLastTarget) {
                 // Clear screen and undo history
                 Canvas canvas = new Canvas(mBitmap);
                 canvas.drawRGB(0xFF, 0xFF, 0xFF);
                 mUndo = mBitmap.copy(mBitmap.getConfig(), true);
                 
                 // Forcibly unpost the command map overlay
                 mShowOverlay = false;
                 long duration = System.nanoTime() - mOverlayStart;
                 mLog.event("Overlay automatically hidden at end of trial after " + duration / 1000000 + " ms");
                 
                 mMainActivity.getActionBar().setIcon(R.drawable.check);
                 
                 if (wasLastTrial) {
                     mMainActivity.setTitle(mStudyCtl.getPrompt());
                     mStudyCtl.nextTrial();
                     pauseStudy("Press OK when you are ready to continue.");
                 } else {
                     Runnable waitStep = new Runnable() {
                         @Override
                         public void run() {
                             mStudyCtl.waitStep(true);
                             mMainActivity.setTitle(mStudyCtl.getPrompt());
                         }
                     };
                     
                     waitStep.run();
                     mHandler.postDelayed(waitStep, mTrialDelay / 4);
                     mHandler.postDelayed(waitStep, mTrialDelay / 2);
                     mHandler.postDelayed(waitStep, mTrialDelay * 3 / 4);
                     mHandler.postDelayed(waitStep, mTrialDelay);
     
                     mHandler.postDelayed(new Runnable() {
                         @Override
                         public void run() {
                             mMainActivity.getActionBar().setIcon(R.drawable.trans);
                         }
                     }, mTrialDelay);
                 }
             } else if (!correctSelection) {
                 mMainActivity.getActionBar().setIcon(R.drawable.x);
             } else {
                 mMainActivity.getActionBar().setIcon(R.drawable.trans);
                 mMainActivity.setTitle(mStudyCtl.getPrompt());
             }
         }
     }
 }
