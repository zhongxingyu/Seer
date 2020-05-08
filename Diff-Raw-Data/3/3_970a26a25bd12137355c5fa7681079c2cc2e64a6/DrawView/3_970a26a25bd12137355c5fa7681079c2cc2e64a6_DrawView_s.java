 package usask.hci.fastdraw;
 
 import java.util.ArrayList;
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
 import android.graphics.BitmapFactory;
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
 import android.util.Pair;
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
     private ArrayList<Gesture> mButtonDirections;
     private HashMap<Gesture, String> mMainButtonNames;
     private PointF mGestureFlashLocation;
     private long mGestureFlashTime;
     private Selection mGestureFlashSelection;
     private UI mUI;
     private final Handler mHandler = new Handler();
     private static final int mChordDelay = 1000 * 1000 * 200; // 200 ms in ns
     private static final int mFlashDelay = 1000 * 1000 * 500; // 500 ms in ns
     private static final int mGestureMenuDelay = 1000 * 1000 * 200; // 200 ms in ns
     private static final int mTrialDelay = 500; // 500 ms
     private static final int mBlockDelay = 1000; // 1 sec
     private static final int mOverlayButtonIndex = 16;
     private static final int mGestureButtonDist = 150;
     private static final int mGestureButtonSize = 75;
     private static final int mGestureSubButtonSize = (int)(mGestureButtonSize * 0.70);
     
     private enum UI {
         CHORD, GESTURE
     }
     
     private enum Action {
         SAVE, CLEAR, UNDO
     }
     
     private enum Effect {
         NO_EFFECT, GLOWING, BLURRED, DASHED
     }
 
     private enum SelectionType {
         TOOL, COLOR, THICKNESS, ACTION, EFFECT
     }
     
     private class Selection {
         public Object object;
         public String name;
         public SelectionType type;
         public Bitmap icon;
         
         public Selection(Object object, String name, int iconResource, SelectionType type) {
             this.object = object;
             this.name = name;
             this.type = type;
             this.icon = BitmapFactory.decodeResource(getResources(), iconResource);
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
         mPaint.setAntiAlias(true);
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
             new Selection(new PaintTool(this), "Paintbrush", R.drawable.paintbrush, SelectionType.TOOL),
             new Selection(new LineTool(this), "Line", R.drawable.line, SelectionType.TOOL),
             new Selection(new CircleTool(this), "Circle", R.drawable.circle, SelectionType.TOOL),
             new Selection(new RectangleTool(this), "Rectangle", R.drawable.rectangle, SelectionType.TOOL),
             
             new Selection(Color.BLACK, "Black", R.drawable.black, SelectionType.COLOR),
             new Selection(Color.WHITE, "White", R.drawable.white, SelectionType.COLOR),
             new Selection(Color.RED, "Red", R.drawable.red, SelectionType.COLOR),
             new Selection(Color.BLUE, "Blue", R.drawable.blue, SelectionType.COLOR),
             
             new Selection(Effect.NO_EFFECT, "No Effect", R.drawable.no_effect, SelectionType.EFFECT),
             new Selection(Effect.GLOWING, "Glowing", R.drawable.glowing, SelectionType.EFFECT),
             new Selection(Effect.BLURRED, "Blurred", R.drawable.blurred, SelectionType.EFFECT),
             new Selection(Effect.DASHED, "Dashed", R.drawable.dashed, SelectionType.EFFECT),
 
             new Selection(1, "Fine", R.drawable.fine, SelectionType.THICKNESS),
             new Selection(6, "Thin", R.drawable.thin, SelectionType.THICKNESS),
             new Selection(16, "Medium", R.drawable.medium, SelectionType.THICKNESS),
             new Selection(50, "Wide", R.drawable.wide, SelectionType.THICKNESS),
             
             null, // The position of the command map button
             new Selection(Action.SAVE, "Save", R.drawable.save, SelectionType.ACTION),
             new Selection(Action.CLEAR, "Clear", R.drawable.clear, SelectionType.ACTION),
             new Selection(Action.UNDO, "Undo", R.drawable.undo, SelectionType.ACTION)
         };
         
         mGestureSelections = new HashMap<Gesture, Integer>();
         
         mGestureSelections.put(Gesture.UP, 0);          // Paintbrush
         mGestureSelections.put(Gesture.UP_RIGHT, 1);    // Line
         mGestureSelections.put(Gesture.UP_DOWN, 2);     // Circle
         mGestureSelections.put(Gesture.UP_LEFT, 3);     // Rectangle
 
         mGestureSelections.put(Gesture.LEFT, 4);        // Black
         mGestureSelections.put(Gesture.LEFT_RIGHT, 5);  // White
         mGestureSelections.put(Gesture.LEFT_UP, 6);     // Red
         mGestureSelections.put(Gesture.LEFT_DOWN, 7);   // Blue
         
         mGestureSelections.put(Gesture.RIGHT, 8);       // Plain
         mGestureSelections.put(Gesture.RIGHT_DOWN, 9);  // Glowing
         mGestureSelections.put(Gesture.RIGHT_LEFT, 10); // Blurred
         mGestureSelections.put(Gesture.RIGHT_UP, 11);   // Dashed
         
         mGestureSelections.put(Gesture.DOWN_LEFT, 12);  // Fine
         mGestureSelections.put(Gesture.DOWN_UP, 13);    // Thin
         mGestureSelections.put(Gesture.DOWN, 14);       // Medium
         mGestureSelections.put(Gesture.DOWN_RIGHT, 15); // Wide
         
         // Default to thin black paintbrush
         changeSelection(0, false);
         changeSelection(4, false);
         changeSelection(13, false);
         
         mButtonDirections = new ArrayList<Gesture>();
         mButtonDirections.add(Gesture.UP);
         mButtonDirections.add(Gesture.LEFT);
         mButtonDirections.add(Gesture.RIGHT);
         mButtonDirections.add(Gesture.DOWN);
         
         mMainButtonNames = new HashMap<Gesture, String>();
         mMainButtonNames.put(Gesture.UP, "Tools");
         mMainButtonNames.put(Gesture.LEFT, "Colors");
         mMainButtonNames.put(Gesture.RIGHT, "Effects");
         mMainButtonNames.put(Gesture.DOWN, "Widths");
         
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
                     if (now - mGestureFlashTime > mFlashDelay) {
                         mGestureFlashLocation = null;
                         postInvalidate();
                     }
                     
                     if (mPossibleGestureFinger != -1 && now - mPossibleGestureFingerTime > mGestureMenuDelay && !mChanged) {
                         mGestureFinger = mPossibleGestureFinger;
                         mIgnoredFingers.add(mGestureFinger);
                         mPossibleGestureFinger = -1;
                         mShowGestureMenu = true;
                         mGestureFlashLocation = null;
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
                 permanentGridCheckBox.setEnabled(!isChecked);
             }
         });
         
         final NumberPicker subjectIdPicker = (NumberPicker) studySetupLayout.findViewById(R.id.subject_id_picker);
         subjectIdPicker.setMinValue(0);
         subjectIdPicker.setMaxValue(99);
         subjectIdPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS); // Remove the virtual keyboard
         
         final NumberPicker blockNumPicker = (NumberPicker) studySetupLayout.findViewById(R.id.block_num_picker);
         blockNumPicker.setMinValue(1);
         blockNumPicker.setMaxValue(mStudyCtl.getNumBlocks());
         blockNumPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS); // Remove the virtual keyboard
 
         new AlertDialog.Builder(mainActivity)
             .setMessage(R.string.dialog_study_mode)
             .setCancelable(false)
             .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int which) {
                     mStudyMode = studyCheckBox.isChecked();
                     
                     if (mStudyMode) {
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
         
         if (mShowOverlay || mFlashTimes.size() > 0 || mInstantMenu)
             mPaint.setColor(0xEEEEEE99);
         else
             mPaint.setColor(0xEEFFFFAA);
         
         canvas.drawRect(bounds, mPaint);
         
         if (mUI == UI.CHORD && (mShowOverlay || mPermanentGrid || mFlashTimes.size() > 0)) {
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
                     if (mSelections[i] != null && mFlashTimes.get(i) == null) {
                         String name = mSelections[i].name;
                         int heightAdj = getTextHeight(name, mPaint) / 2;
                         float centerX = (x + 0.5f) * mColWidth;
                         float centerY = (y + 0.5f) * mRowHeight;
                         
                         Bitmap icon = mSelections[i].icon;
                         float iconWidth = icon.getWidth();
                         float iconHeight = icon.getHeight();
                         
                         canvas.drawBitmap(icon, centerX - iconWidth / 2, centerY - iconHeight * 3 / 4, mPaint);
                         canvas.drawText(name, centerX, centerY + iconHeight / 2 + heightAdj, mPaint);
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
                         
                         Bitmap icon = selection.icon;
                         float iconWidth = icon.getWidth();
                         float iconHeight = icon.getHeight();
                         float centerX = buttonBounds.left + 0.5f * mColWidth;
                         float centerY = buttonBounds.top + 0.5f * mRowHeight;
                         
                         mPaint.setColor(0xFF000000);
                         String name = selection.name;
                         int heightAdj = getTextHeight(name, mPaint) / 2;
                         
                         canvas.drawBitmap(icon, centerX - iconWidth / 2, centerY - iconHeight * 3 / 4, mPaint);
                         canvas.drawText(name, centerX, centerY + iconHeight / 2 + heightAdj, mPaint);
                     }
                 }
             }
         } else if (mUI == UI.GESTURE) {
             if (mGestureFlashLocation != null) {
                 mPaint.setTextSize(18);
                 drawGestureButton(canvas, mGestureFlashSelection.name, mGestureFlashSelection.icon, mGestureFlashLocation, mGestureSubButtonSize, mPaint, true, false);
                 mPaint.setTextSize(26);
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
             
             Pair<Gesture, Gesture> gestures = splitGesture(gesture);
             Gesture mainGesture = gestures.first;
             Gesture subGesture = gestures.second;
 
             for (Gesture buttonDirection : mButtonDirections) {
                 PointF position = mainButtonPosition(origin, buttonDirection);
                 
                 if (isInCircle(mGestureFingerPos, position, mGestureButtonSize)) {
                     mActiveCategoryOrigin = position;
                     mActiveCategory = buttonDirection;
                     break;
                 }
             }
             
             boolean greyoutInactive = mActiveCategory != Gesture.UNKNOWN;
             
             mPaint.setTextSize(22);
 
             for (Gesture buttonDirection : mButtonDirections) {
                 PointF position = mainButtonPosition(origin, buttonDirection);
                 boolean active = mActiveCategory == buttonDirection;
                 String name = mMainButtonNames.get(buttonDirection);
                 
                 drawGestureButton(canvas, name, null, position, mGestureButtonSize, mPaint, active, greyoutInactive);
             }
             
             mPaint.setTextSize(18);
             mSubSelection = Gesture.UNKNOWN;
 
             for (Gesture buttonDirection : mButtonDirections) {
                 PointF position = subButtonPosition(mActiveCategoryOrigin, buttonDirection);
                 
                 if (isInCircle(mGestureFingerPos, position, mGestureSubButtonSize)) {
                     mSubSelection = buttonDirection;
                     break;
                 }
             }
             
             if (mSubSelection == Gesture.UNKNOWN && mainGesture == mActiveCategory)
                 mSubSelection = subGesture;
             
             if (mActiveCategory != Gesture.UNKNOWN) {
                 for (Gesture buttonDirection : mButtonDirections) {
                     PointF position = subButtonPosition(mActiveCategoryOrigin, buttonDirection);
                     boolean active = mSubSelection == buttonDirection;
                     Selection selection = mSelections[mGestureSelections.get(combineGestures(mActiveCategory, buttonDirection))];
                     
                     drawGestureButton(canvas, selection.name, selection.icon, position, mGestureSubButtonSize, mPaint, active, false);
                 }
             }
             
             mPaint.setTextSize(26);
         }
     }
     
     private PointF mainButtonPosition(PointF origin, Gesture gesture) {
         switch (gesture) {
             case UP:
                 return new PointF(origin.x, origin.y - mGestureButtonDist);
                 
             case LEFT:
                 return new PointF(origin.x - mGestureButtonDist, origin.y);
                 
             case RIGHT:
                 return new PointF(origin.x + mGestureButtonDist, origin.y);
                 
             case DOWN:
                 return new PointF(origin.x, origin.y + mGestureButtonDist);
                 
             default:
                 throw new IllegalArgumentException("Main button gesture must be up, left, right, or down.");
         }
     }
     
     private PointF subButtonPosition(PointF subOrigin, Gesture gesture) {
         int dist = mGestureButtonSize + mGestureSubButtonSize;
         
         switch (gesture) {
             case UP:
                 return new PointF(subOrigin.x, subOrigin.y - dist);
                 
             case LEFT:
                 return new PointF(subOrigin.x - dist, subOrigin.y);
                 
             case RIGHT:
                 return new PointF(subOrigin.x + dist, subOrigin.y);
                 
             case DOWN:
                 return new PointF(subOrigin.x, subOrigin.y + dist);
                 
             default:
                 throw new IllegalArgumentException("Sub-button gesture must be up, left, right, or down.");
         }
     }
     
     private Gesture combineGestures(Gesture mainGesture, Gesture subGesture) {
         switch (mainGesture) {
         case UP:
             switch (subGesture) {
                 case UP: return Gesture.UP;
                 case LEFT: return Gesture.UP_LEFT;
                 case RIGHT: return Gesture.UP_RIGHT; 
                 case DOWN: return Gesture.UP_DOWN; 
                 default: break;
             }
             break;
             
         case LEFT:
             switch (subGesture) {
                 case UP: return Gesture.LEFT_UP;
                 case LEFT: return Gesture.LEFT;
                 case RIGHT: return Gesture.LEFT_RIGHT;
                 case DOWN: return Gesture.LEFT_DOWN;
                 default: break;
             }
             break;
     
         case RIGHT:
             switch (subGesture) {
                 case UP: return Gesture.RIGHT_UP;
                 case LEFT: return Gesture.RIGHT_LEFT;
                 case RIGHT: return Gesture.RIGHT;
                 case DOWN: return Gesture.RIGHT_DOWN;
                 default: break;
             }
             break;
     
         case DOWN:
             switch (subGesture) {
                 case UP: return Gesture.DOWN_UP;
                 case LEFT: return Gesture.DOWN_LEFT;
                 case RIGHT: return Gesture.DOWN_RIGHT;
                 case DOWN: return Gesture.DOWN;
                 default: break;
             }
             break;
             
         default:
             break;
         }
         
         return Gesture.UNKNOWN;
     }
     
     private Pair<Gesture, Gesture> splitGesture(Gesture gesture) {
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
         
         return new Pair<Gesture, Gesture>(mainGesture, subGesture);
     }
     
     private boolean isInCircle(PointF point, PointF center, float radius) {
         float dx = point.x - center.x;
         float dy = point.y - center.y;
         double distance = Math.sqrt(dx*dx + dy*dy);
         
         return distance < radius;
     }
     
     private void drawGestureButton(Canvas canvas, String text, Bitmap icon, PointF position, int size, Paint paint, boolean highlight, boolean greyout) {
         paint.getTextBounds(text, 0, text.length(), mTextBounds);
         
         if (highlight)
             mPaint.setColor(0xFFAAAAAA);
         else if (greyout)
             mPaint.setColor(0xFFDDDDDD);
         else
             mPaint.setColor(0xFFCCCCCC);
         
         canvas.drawCircle(position.x, position.y, size, paint);
 
         final float verticalShift;
         
         if (icon != null) {
             final int iconSize = (int)(size * 0.9);
             verticalShift = size * 0.3f;
             
             Bitmap scaledIcon = Bitmap.createScaledBitmap(icon, iconSize, iconSize, true);
             
             canvas.drawBitmap(scaledIcon, position.x - iconSize / 2, position.y - iconSize / 2 - verticalShift, mPaint);
         } else {
             verticalShift = 0;
         }
 
         if (greyout && !highlight) {
             mPaint.setColor(0xEE777777);
         } else {
             mPaint.setColor(0xEE000000);
             mPaint.setShadowLayer(2, 1, 1, 0x33000000);
         }
 
         canvas.drawText(text, position.x, position.y + mTextBounds.height() / 2 + verticalShift, mPaint);
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
                         mGestureFinger = id;
                         mGestureMenuTime = now;
                         mShowGestureMenu = true;
                         mGestureFlashLocation = null;
                         mIgnoredFingers.add(id);
                         mGestureFingerPos = new PointF(x, y);
                         mOrigins.put(id, mGestureFingerPos);
                         mGestureDetector.clear();
                     } else if (event.getPointerCount() == 1) {
                         mGestureDetector.clear();
                         
                         if (getOverlayButtonBounds().contains(x, y)) {
                             mIgnoredFingers.add(id);
                             mInstantMenu = true;
                             mFingerInside = id;
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
                     
                     if (fingerId == mGestureFinger && !mGestureFingerPos.equals(x2, y2)) {
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
 
                 PointF origin = mOrigins.get(id);
                 mOrigins.delete(id);
                 mFingers.remove(id);
 
                 if (id == mFingerInside) {
                     mInstantMenu = false;
                     mFingerInside = -1;
                 }
                 
                 if (id == mPossibleGestureFinger)
                     mPossibleGestureFinger = -1;
 
                 boolean draw = true;
                 
                 if (id == mGestureFinger) {
                     mGestureFinger = -1;
                     mShowGestureMenu = false;
                     boolean gestureSelection = false;
                     Gesture gesture = combineGestures(mActiveCategory, mSubSelection);
                     
                     if (gesture == Gesture.UNKNOWN) {
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
                         
                         Pair<Gesture, Gesture> gestures = splitGesture(gesture);
                         PointF subOrigin = mainButtonPosition(origin, gestures.first);
 
                         if (mGestureSelections.containsKey(gesture)) {
                             mGestureFlashLocation = subButtonPosition(subOrigin, gestures.second);
                             mGestureFlashTime = now;
                             mGestureFlashSelection = mSelections[mGestureSelections.get(gesture)];
                         }
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
             if (mUI == UI.CHORD) {
                 synchronized (mFlashTimes) {
                     mFlashTimes.put(selected, System.nanoTime());
                 }
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
                
             case EFFECT:
                 break;
         }
         
         if (fromUser && mStudyMode && !mStudyCtl.isFinished()) {
             boolean gesture = mUI == UI.GESTURE;
             boolean wasLastTarget = mStudyCtl.isOnLastTarget();
             boolean wasLastTrial = mStudyCtl.isOnLastTrial();
             boolean wasLastBlock = mStudyCtl.isOnLastBlock();
             
             if (mUI == UI.CHORD && mShowOverlay && wasLastTarget) {
                 long now = System.nanoTime();
                 
                 if (mOverlayStart > mStudyCtl.getTrialStart())
                     mStudyCtl.addUITime(now - mOverlayStart);
                 else
                     mStudyCtl.addUITime(now - mStudyCtl.getTrialStart());
             }
             
             boolean correctSelection = mStudyCtl.handleSelected(selection.name, gesture);
             
             if (correctSelection && wasLastTarget) {
                 // Clear screen and undo history
                 Canvas canvas = new Canvas(mBitmap);
                 canvas.drawRGB(0xFF, 0xFF, 0xFF);
                 mUndo = mBitmap.copy(mBitmap.getConfig(), true);
                 
                 // Forcibly unpost the command map overlay
                 if (mShowOverlay) {
                     mShowOverlay = false;
                     long duration = System.nanoTime() - mOverlayStart;
                     mLog.event("Overlay automatically hidden at end of trial after " + duration / 1000000 + " ms");
                 }
                 
                 mMainActivity.getActionBar().setIcon(R.drawable.check);
                 
                 if (wasLastTrial) {
                     if (wasLastBlock) {
                         mStudyCtl.finish();
                         mMainActivity.getActionBar().setIcon(R.drawable.trans);
                         mMainActivity.setTitle(mStudyCtl.getPrompt());
                         alert("You are finished!\n\nThank you for participating!");
                     } else {
                         mMainActivity.setTitle(mStudyCtl.getPrompt());
                         mStudyCtl.nextTrial();
                         pauseStudy("Press OK when you are ready to continue.");
                     }
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
