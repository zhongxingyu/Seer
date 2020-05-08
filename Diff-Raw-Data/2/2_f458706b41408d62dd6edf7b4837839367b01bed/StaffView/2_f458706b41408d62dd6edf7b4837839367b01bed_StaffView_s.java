 /*
  * Copyright 2010 Andrew Prunicki
  * 
  * This file is part of Twinkle.
  * 
  * Twinkle is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * Twinkle is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with Twinkle.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.prunicki.suzuki.twinkle.widget;
 
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Path;
 import android.graphics.Typeface;
 import android.graphics.Paint.Cap;
 import android.graphics.Paint.Style;
 import android.graphics.Path.Direction;
 import android.util.AttributeSet;
 import android.view.View;
 
 public abstract class StaffView extends View {
     private static final int STROKE_WIDTH = 1;
     private static final int RATIO = 3;
     private static final float MEASURE_RATIO = 2.75f;
     private static final String TIME_4 = "#";
     private static final String TREBLE_CLEF = "$";
     
    public static final String DOUBlE_WHOLE_NOTE = "0";
     public static final String WHOLE_NOTE = "1";
     public static final String HALF_NOTE = "2";
     public static final String QUARTER_NOTE = "3";
     public static final String EIGHTH_NOTE = "4";
     public static final String SIXTEENTH_NOTE = "5";
     public static final String EIGHTH_START_NOTE = "6";
     public static final String SIXTEENTH_START_NOTE = "7";
     public static final String EIGHTH_MIDDLE_NOTE = "8";
     public static final String SIXTEENTH_MIDDLE_NOTE = "9";
     public static final String EIGHTH_TO_SIXTEENTH_MIDDLE_NOTE = ":";
     public static final String SIXTEENTH_TO_EIGHTH_MIDDLE_NOTE = ";";
     public static final String EIGHTH_END_NOTE = "<";
     public static final String SIXTEENTH_END_NOTE = "=";
     
     public static final String EIGHTH_REST = "A";
     public static final String SIXTEENTH_REST = "B";
     
     private final float mDensity;
     private final float mStrokeWidth;
     private final float mPadding;
     
     private final Path mFillPath;
     private final Paint mFillPaint;
     private final Path mLinePath;
     private final Paint mLinePaint;
     private final Path mFontPath;
     protected final Paint mFontPaint;
     
     private final Typeface mTypeface;
     
     protected float[] mLineY;
     
     private int mLastWidth;
     private int mLastHeight;
     protected float mLineHeight;
     protected float mStartNoteX;
     
     public StaffView(Context context, AttributeSet attrs) {
         super(context, attrs);
         
         mLineY = new float[5];
         mTypeface = Typeface.createFromAsset(context.getAssets(), "staff.ttf");
         
         mDensity = context.getResources().getDisplayMetrics().density;
         mStrokeWidth = STROKE_WIDTH * mDensity;
         mPadding = 4 * mDensity;
         
         mFillPaint = new Paint();
         mFillPaint.setColor(Color.WHITE);
         mFillPaint.setStyle(Style.FILL);
         
         mLinePaint = new Paint();
         mLinePaint.setColor(Color.BLACK);
         mLinePaint.setStyle(Style.STROKE);
         mLinePaint.setStrokeWidth(mStrokeWidth);
         mLinePaint.setStrokeCap(Cap.SQUARE);
         
         mFontPaint = new Paint();
         mFontPaint.setTypeface(mTypeface);
         mFontPaint.setColor(Color.BLACK);
         mFontPaint.setStyle(Style.FILL_AND_STROKE);
         
         mFillPath = new Path();
         mLinePath = new Path();
         mFontPath = new Path();
     }
 
     @Override
     protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
         int minWidth = getSuggestedMinimumWidth();
         int minHeight = getSuggestedMinimumHeight();
         int defaultWidth = getDefaultSize(minWidth, widthMeasureSpec);
         int defaultHeight = getDefaultSize(minHeight, heightMeasureSpec);
         
         float width = defaultWidth;
         float height = defaultWidth / MEASURE_RATIO;
         if (height > defaultHeight) {
             width = defaultHeight * MEASURE_RATIO;
             height = defaultHeight;
         }
         
         setMeasuredDimension((int) width, (int) height);
     }
     
     @Override
     protected void onDraw(Canvas canvas) {
         int width = getWidth();
         int height = getHeight();
         if (mLastWidth != width || mLastHeight != height) {
             compute(width, height);
         }
         
         canvas.drawPath(mFillPath, mFillPaint);
         canvas.drawPath(mLinePath, mLinePaint);
         canvas.drawPath(mFontPath, mFontPaint);
         
         drawCustom(canvas);
         
         super.onDraw(canvas);
     }
 
     protected abstract void drawCustom(Canvas canvas);
 
     private void compute(int width, int height) {
         mLastWidth = width;
         mLastHeight = height;
         
         float padding = mPadding;
         Path fillPath = mFillPath;
         Path linePath = mLinePath;
         Path fontPath = mFontPath;
         Paint fontPaint = mFontPaint;
         float strokeWidth = mStrokeWidth;
         float[] lineYArray = mLineY;
         
         float paddedWidth = width - padding * 2;
         float paddedHeight = height - padding * 2;
         
         linePath.reset();
         fillPath.reset();
         fontPath.reset();
         
         fillPath.addRect(0, 0, width - strokeWidth, height - strokeWidth, Direction.CW);
         linePath.addRect(0, 0, width - strokeWidth, height - strokeWidth, Direction.CW);
         
         float startX = padding;
         float startY = padding;
         float drawWidth = paddedWidth;
         float drawHeight = paddedWidth / RATIO;
         if (drawHeight > paddedHeight) {
             drawWidth = paddedHeight * RATIO;
             drawHeight = paddedHeight;
             startX = (paddedWidth - drawWidth) / 2;
         } else {
             startY = (paddedHeight - drawHeight) / 2;
         }
         
         float staffHeight = (3 * drawHeight) / 5;
         float staffStartY = startY + ((drawHeight - staffHeight) / 2);
         
         float lineHeight = staffHeight / 4;
         float endX = startX + drawWidth;
         for (int i = 0; i < 5; i++) {
             float lineY = staffStartY + lineHeight * i;
             lineYArray[i] = lineY;
             traceLine(linePath, startX, lineY, endX, lineY);
         }
         
         float vertLineX = startX + strokeWidth;
         float vertLineEndY = staffStartY + staffHeight;
         traceLine(linePath, vertLineX, staffStartY, vertLineX, vertLineEndY);
         vertLineX = endX - 2 * strokeWidth;
         traceLine(linePath, vertLineX, staffStartY, vertLineX, vertLineEndY);
         traceLine(linePath, endX, staffStartY, endX, vertLineEndY);
         
         float clefX = startX + 4 * strokeWidth;
         float clefY = staffStartY + staffHeight + 2;
         
         Path tmpPath = new Path();
         float[] tmpFloatArray = new float[1];
         
         fontPaint.setTextSize(drawHeight);
         fontPaint.getTextPath(TREBLE_CLEF, 0, 1, clefX, clefY, tmpPath);
         fontPath.addPath(tmpPath);
         
         fontPaint.getTextWidths(TREBLE_CLEF, tmpFloatArray);
         float timeSigX = clefX + tmpFloatArray[0];
         
         fontPaint.setTextSize(lineHeight * 2);
         fontPaint.getTextPath(TIME_4, 0, 1, timeSigX, lineYArray[2], tmpPath);
         fontPath.addPath(tmpPath);
         fontPaint.getTextPath(TIME_4, 0, 1, timeSigX, lineYArray[4], tmpPath);
         fontPath.addPath(tmpPath);
         
         fontPaint.getTextWidths(TIME_4, tmpFloatArray);
         
         mLineHeight = staffHeight;
         mStartNoteX = timeSigX + tmpFloatArray[0];
     }
     
     private void traceLine(Path path, float startX, float startY, float endX, float endY) {
         path.moveTo(startX, startY);
         path.lineTo(endX, endY);
     }
 }
