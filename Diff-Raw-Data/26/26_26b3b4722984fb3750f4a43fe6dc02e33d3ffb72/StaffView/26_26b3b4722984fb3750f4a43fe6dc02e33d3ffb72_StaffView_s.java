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
 package com.prunicki.suzuki.twinkle;
 
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
 
 public class StaffView extends View {
     private static final int STROKE_WIDTH = 2;
     private static final int RATIO = 3;
     private static final String TIME_4 = "#";
     private static final String TREBLE_CLEF = "$";
     private static final String WHOLE_NOTE = "1";
     
     private final float mDensity;
     private final float mStrokeWidth;
     private final float mPadding;
     
    private final Path mLinePath;
    private final Paint mLinePaint;
     private final Path mFillPath;
     private final Paint mFillPaint;
     private final Path mFontPath;
     private final Paint mFontPaint;
     
     private final Typeface mTypeface;
     
     private int mNote;
     private float[] mLineY;
     
     private int mLastWidth;
     private int mLastHeight;
     private float mLineHeight;
     private float mStartNoteX;
     
     private float[] mTmpFloatArray;
     private Path mTmpPath;
     
     public StaffView(Context context, AttributeSet attrs) {
         super(context, attrs);
         
         mNote = -1;
         mLineY = new float[5];
         mTypeface = Typeface.createFromAsset(context.getAssets(), "staff.ttf");
         
         mDensity = context.getResources().getDisplayMetrics().density;
         mStrokeWidth = STROKE_WIDTH * mDensity;
         mPadding = 4 * mDensity;
         
         mLinePaint = new Paint();
         mLinePaint.setColor(Color.BLACK);
         mLinePaint.setStyle(Style.STROKE);
         mLinePaint.setStrokeWidth(mStrokeWidth);
         mLinePaint.setStrokeCap(Cap.SQUARE);
         
        mFillPaint = new Paint();
        mFillPaint.setColor(Color.WHITE);
        mFillPaint.setStyle(Style.FILL);
        
         mFontPaint = new Paint();
         mFontPaint.setTypeface(mTypeface);
         mFontPaint.setColor(Color.BLACK);
         mFontPaint.setStyle(Style.FILL_AND_STROKE);
        mLinePaint.setStrokeWidth(mStrokeWidth);
        mLinePaint.setStrokeCap(Cap.BUTT);
         
        mLinePath = new Path();
         mFillPath = new Path();
         mFontPath = new Path();
         
         mTmpPath = new Path();
         mTmpFloatArray = new float[1];
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
         
         if (mNote >= 0) {
             drawNote(canvas);
         }
         
         super.onDraw(canvas);
     }
 
     public void setNote(int note) {
         if (note != mNote) {
             mNote = note;
             postInvalidate();
         }
     }
     
     private void drawNote(Canvas canvas) {
         mFontPaint.setTextSize(mLineHeight);
         
         float[] lineYArray = mLineY;
         
         float y = lineYArray[4];
         switch (mNote) {
             case 0:
                 y = lineYArray[1];
                 break;
             case 1:
                 y = (lineYArray[1] + lineYArray[2]) / 2;
                 break;
             case 2:
                 y = lineYArray[2];
                 break;
             case 3:
                 y = (lineYArray[2] + lineYArray[3]) / 2;
                 break;
             case 4:
                 y = lineYArray[3];
                 break;
             case 5:
                 y = (lineYArray[3] + lineYArray[4]) / 2;
                 break;
             case 6:
                 y = lineYArray[4];
                 break;
         }
         
         canvas.drawText(WHOLE_NOTE, mStartNoteX, y, mFontPaint);
     }
 
     private void compute(int width, int height) {
         mLastWidth = width;
         mLastHeight = height;
         
         float padding = mPadding;
        Path linePath = mLinePath;
         Path fillPath = mFillPath;
         Path fontPath = mFontPath;
         Path tmpPath = mTmpPath;
         Paint fontPaint = mFontPaint;
         float strokeWidth = mStrokeWidth;
         float[] lineYArray = mLineY;
         float[] tmpFloatArray = mTmpFloatArray;
         
         float paddedWidth = width - padding * 2;
         float paddedHeight = height - padding * 2;
         
         linePath.reset();
         fillPath.reset();
         fontPath.reset();
         
         fillPath.addRect(0, 0, width, height, Direction.CW);
         linePath.addRect(0, 0, width, height, Direction.CW);
         
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
