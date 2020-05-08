 package com.flexymind.alpha.customviews;
 
 import android.content.Context;
 import android.graphics.Picture;
 import android.util.AttributeSet;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.ViewConfiguration;
 import android.view.ViewGroup;
 import android.widget.FrameLayout;
 import com.flexymind.alpha.player.Note;
 
 import java.util.ArrayList;
 
 import static com.flexymind.alpha.customviews.PictureStorage.blackKeyNotPressed;
 import static com.flexymind.alpha.customviews.PictureStorage.whiteKeyNotPressed;
 
 public class PianoKeyboard extends Board implements View.OnTouchListener {
 
     private final int    COUNT_OF_WHITE_KEYS     =   8;
     private final int    START_ID_FOR_KEY_VIEWS  = 100;
     private final int[]  BLACK_KEY_POSITIONS     =  {1, 2, 4, 5, 6};
     //TODO fill this array automatic
 
     private View parent;
 
     private final ArrayList[] recentTouchedViewsIndex = new ArrayList[10];
 
     private final ArrayList[] downTouchedViewsIndex = new ArrayList[10];
 
     private final ArrayList<View> moveOutsideEnabledViews = new ArrayList<View>();
 
     private int mTouchSlop = 24;
 
     private boolean isOnLayout = false;
 
 
 
     public PianoKeyboard(Context context, AttributeSet attrs) {
 
         super(context, attrs);
 
         parent = getRootView();
         parent.setOnTouchListener(this);
         mTouchSlop = ViewConfiguration.get(getContext())
                 .getScaledTouchSlop();
     }
 
     @Override
     public void onLayout(boolean changed, int l,
                          int t, int r, int b) {
 
         super.onLayout(changed, l, t, r, b);
 
         if (!isOnLayout) {
 
             addWhiteKeys();
             addBlackKeys();
             isOnLayout = true;
         }
     }
 
     private void addWhiteKeys() {
 
         int id = START_ID_FOR_KEY_VIEWS;
         final boolean whiteKey = true;
 
         LayoutParams params = new LayoutParams(
                 LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
 
         params.addRule(ALIGN_LEFT);
 
         addKey( whiteKeyNotPressed
                 , getWhiteKeyHeight()
                 , getWhiteKeyWidth()
                 , params
                 , id++
                 , Note.C
                 , whiteKey  );
 
         for (int i = 0; i < COUNT_OF_WHITE_KEYS - 1; i++) {
             addKey( whiteKeyNotPressed
                     , getWhiteKeyHeight()
                     , getWhiteKeyWidth()
                     , paramsWithRightOf(id - 1)
                     , id++
                     , Note.getNotesForWhiteKeys()[i+1]
                     , whiteKey);
         }
     }
 
     private LayoutParams paramsWithRightOf(int id) {
 
         LayoutParams params = new LayoutParams
                 (LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
         params.addRule(RIGHT_OF, id);
 
         return  params;
     }
 
     private void addBlackKeys() {
 
         int id = START_ID_FOR_KEY_VIEWS + COUNT_OF_WHITE_KEYS;
         final boolean whiteKey = false;
 
         int j = 0;
         for (int whiteKeyNumber: BLACK_KEY_POSITIONS) {
             addKey( blackKeyNotPressed
                     , getBlackKeyHeight()
                     , getBlackKeyWidth()
                     , paramsWithMargin(whiteKeyNumber * getWhiteKeyWidth()
                     - getBlackKeyWidth() / 2)
                     , ++id
                     , Note.getNotesForBlackKeys()[j++]
                     , whiteKey);
         }
     }
 
     private LayoutParams paramsWithMargin(int margin) {
         LayoutParams params = new LayoutParams
                 (LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
         params.leftMargin = margin;
 
         return params;
     }
 
     private void addKey(Picture picture, int keyH, int keyW,
                         LayoutParams params, int id, Note note
             , boolean isWhiteKey ) {
 
         PianoKey key = new PianoKey( getContext()
                 , keyH
                 , keyW
                 , picture
                 , note
                 , isWhiteKey);
         key.setId(id);
         key.setOnTouchListener(this);
         addView(key, params);
     }
 
     private int getWhiteKeyWidth() {
         return this.width / COUNT_OF_WHITE_KEYS;
     }
 
     private int getWhiteKeyHeight() {
         return  this.height;
     }
 
     private int getBlackKeyWidth() {
         return  getWhiteKeyWidth() / 2;
     }
 
     private int getBlackKeyHeight() {
         return getWhiteKeyHeight() / 2;
     }
 
     private void dealEvent(final int actionPointerIndex,
                            final MotionEvent event, final View eventView,
                            final int actionResolved) {
 
         int rawX, rawY;
         final int location[] = { 0, 0 };
         eventView.getLocationOnScreen(location);
 
         rawX = (int) event.getX(actionPointerIndex) + location[0];
         rawY = (int) event.getY(actionPointerIndex) + location[1];
 
         final int actionPointerID = event.getPointerId(actionPointerIndex);
 
 
         ArrayList<View> hoverViews = getTouchedViews(rawX, rawY);
 

         if (actionResolved == MotionEvent.ACTION_DOWN) {
             downTouchedViewsIndex[actionPointerID] = (ArrayList<View>) hoverViews
                     .clone();
         }
 
         if (downTouchedViewsIndex[actionPointerID] != null) {
 
             final ArrayList<View> tempViews = (ArrayList<View>) hoverViews
                     .clone();
             tempViews.removeAll(downTouchedViewsIndex[actionPointerID]);
             hoverViews.removeAll(tempViews);
         }
 
         if (recentTouchedViewsIndex[actionPointerID] != null) {
 
             final ArrayList<View> recentTouchedViews = recentTouchedViewsIndex[actionPointerID];
 
             final ArrayList<View> shouldTouchViews = (ArrayList<View>) hoverViews
                     .clone();
             if ( !shouldTouchViews.containsAll(recentTouchedViews) ) {
 
                 shouldTouchViews.removeAll(recentTouchedViews);
                 shouldTouchViews.addAll(recentTouchedViews);
 
                 final ArrayList<View> outsideTouchedViews = (ArrayList<View>) shouldTouchViews
                         .clone();
                 outsideTouchedViews.removeAll(hoverViews);
             }
 
             recentTouchedViewsIndex[actionPointerID] = hoverViews;
             hoverViews = shouldTouchViews;
         } else {
 
             recentTouchedViewsIndex[actionPointerID] = hoverViews;
         }
 
         if (actionResolved == MotionEvent.ACTION_UP) {
 
             recentTouchedViewsIndex[actionPointerID] = null;
             downTouchedViewsIndex[actionPointerID] = null;
         }
 
         for (final View view : hoverViews) {
 
             int x, y;
             view.getLocationOnScreen(location);
             x = rawX - location[0];
             y = rawY - location[1];
 
 
             if (recentTouchedViewsIndex[actionPointerID] != null) {
                 if (pointInView(x, y, mTouchSlop, view.getWidth(),
                         view.getHeight())) {
 
 
                     if (!recentTouchedViewsIndex[actionPointerID]
                             .contains(view)) {
                         recentTouchedViewsIndex[actionPointerID].add(view);
                     }
                 } else if (moveOutsideEnabledViews.contains(view)) {
 
                     recentTouchedViewsIndex[actionPointerID].add(view);
                 }
             }
             final MotionEvent me = MotionEvent.obtain(event.getDownTime(),
                     event.getEventTime(), actionResolved, rawX, rawY,
                     event.getPressure(actionPointerIndex),
                     event.getPressure(actionPointerIndex),
                     event.getMetaState(), event.getXPrecision(),
                     event.getYPrecision(), event.getDeviceId(),
                     event.getEdgeFlags());
             me.setLocation(x, y);
 
             if (!me.equals(event)) {
 
                if (event.getY() < 0) {
 
                     me.setAction(MotionEvent.ACTION_UP);
                     eventView.onTouchEvent(me);
                 }
                else
                     view.onTouchEvent(me);
             }
 
 
             if (actionResolved == MotionEvent.ACTION_MOVE) {
             }
         }
     }
 
     private ArrayList<View> getChildViews(final View view) {
 
         final ArrayList<View> views = new ArrayList<View>();
 
         if (view instanceof ViewGroup) {
 
             final ViewGroup v = ((ViewGroup) view);
 
             if (v.getChildCount() > 0) {
 
                 for (int i = 0; i < v.getChildCount(); i++) {
 
                     views.add(v.getChildAt(i));
                 }
             }
         }
 
         return views;
 
     }
 
     private ArrayList<View> getTouchedViews(final int x, final int y) {
 
         final ArrayList<View> touchedViews = new ArrayList<View>();
         final ArrayList<View> possibleViews = new ArrayList<View>();
 
         if (parent instanceof ViewGroup) {
             possibleViews.add(parent);
             for (int i = 0; i < possibleViews.size(); i++) {
                 final View view = possibleViews.get(i);
 
                 final int location[] = { 0, 0 };
                 view.getLocationOnScreen(location);
 
                 if (((view.getHeight() + location[1] >= y)
                         & (view.getWidth() + location[0] >= x)
                         & (view.getLeft() <= x) & (view.getTop() <= y))
                         || view instanceof FrameLayout) {
 
                     touchedViews.add(view);
                     possibleViews.addAll(getChildViews(view));
                 }
             }
         }
 
         if ( touchedViews.size() > 0) {
 
             PianoKey lastKey =  (PianoKey)touchedViews.get(touchedViews.size() - 1);
 
             if(touchedViews.size() > 2 && !lastKey.isWhite()) {
 
                 touchedViews.remove(1);
             }
         }
         return touchedViews;
     }
 
     @Override
     public boolean onTouch(final View v, final MotionEvent event) {
 
         // index of the pointer which starts this Event
         final int actionPointerIndex = event.getActionIndex();
         // resolve the action as a basic type (up, down or move)
         int actionResolved = event.getAction() & MotionEvent.ACTION_MASK;
         if (actionResolved < 7 && actionResolved > 4) {
 
             actionResolved = actionResolved - 5;
         }
 
         if (actionResolved == MotionEvent.ACTION_MOVE) {
 
             for (int ptrIndex = 0; ptrIndex < event.getPointerCount(); ptrIndex++) {
                 // only one event for all move events.
                 dealEvent(ptrIndex, event, v, actionResolved);
             }
 
         } else {
 
             dealEvent(actionPointerIndex, event, v, actionResolved);
         }

         return true;
     }
 
     private boolean pointInView(final float localX, final float localY,
                                 final float slop, final float width, final float height) {
         return localX >= -slop && localY >= -slop && localX < ((width) + slop)
                 && localY < ((height) + slop);
     }
 
 }
