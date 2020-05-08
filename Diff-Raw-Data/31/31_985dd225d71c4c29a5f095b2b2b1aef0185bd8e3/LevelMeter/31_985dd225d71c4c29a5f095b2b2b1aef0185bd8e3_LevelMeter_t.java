 package com.example.audiolevelmeter;
 
 import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.util.AttributeSet;
import android.util.Log;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 
 final class LevelMeter extends LinearLayout {
     private static final String TAG = LevelMeter.class.getSimpleName();
 
    static final int NUM_ELEMENTS = 20;
     private final Segment[] segment = new Segment[NUM_ELEMENTS];
 
     private volatile int mLevel = 0;
 
     public LevelMeter(Context context, AttributeSet attrs) {
         super(context, attrs);
 
         LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT);
         layoutParams.setMargins(1, 0, 1, 0);
 
         // Creates level meter segments.
         for (int i = 0; i < NUM_ELEMENTS; i++) {
             segment[i] = new Segment(context, i);
             addView(segment[i], layoutParams);
         }
     }
 
    private static final int LEVEL0_SHIFT = 30;	// -30db = level 0
 
     void setLevel(int amplitude) {
 
         // Normalize the level to 0 to 1.0
         double normalizedLevel = amplitude / ((double) Short.MAX_VALUE);
         double db = 20.0 * Math.log10(normalizedLevel);
         int level = (int) ((db + LEVEL0_SHIFT) / (LEVEL0_SHIFT / NUM_ELEMENTS));
 
         // Valid range is 0 to NUM_ELEMENTS (NUM_ELEMENTS + 1 levels).
         if (level < 0) {
             level = 0;
         } else if (level > NUM_ELEMENTS) {
             level = NUM_ELEMENTS;
         }
 
        Log.d(TAG, "amp=" + amplitude + " level=" + level);

         mLevel = level;
 
         // For in case called from non-UI thread, instead of invalidate()
        //postInvalidate();
        for (Segment seg : segment) {
            seg.postInvalidate();
        }
     }
 
    private static final int WIDTH  = 9;
     private static final int HEIGHT = 20;
 
     private static final int ON_COLOR  = Color.GREEN;
     private static final int OFF_COLOR = Color.DKGRAY;
 
     // One segment of level meter
     private class Segment extends ImageView {
        private final int mLevelIndex;
 
         public Segment(Context context, int index) {
             super(context);
 
            mLevelIndex = index;
             setMinimumWidth(WIDTH);
             setMinimumHeight(HEIGHT);
         }
 
         @Override
         protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
             setMeasuredDimension(getSuggestedMinimumWidth(),
                                  getSuggestedMinimumHeight());
         }
 
         @Override
         protected void onDraw(Canvas canvas) {
            if (mLevelIndex + 1 <= mLevel) {
                 canvas.drawColor(ON_COLOR);
             } else {
                 canvas.drawColor(OFF_COLOR);
             }
         }
     }
 }
