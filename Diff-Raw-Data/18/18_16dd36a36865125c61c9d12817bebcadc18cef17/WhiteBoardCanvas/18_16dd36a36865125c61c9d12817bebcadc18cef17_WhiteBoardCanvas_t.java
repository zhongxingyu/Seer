 package com.livejournal.karino2.whiteboardcast;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.DashPathEffect;
 import android.graphics.Paint;
 import android.graphics.Path;
 import android.graphics.Rect;
 import android.graphics.RectF;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.view.MotionEvent;
 import android.view.View;
 
 /**
  * Created by karino on 6/26/13.
  */
 public class WhiteBoardCanvas extends View implements FrameRetrieval {
 
     private Bitmap mBitmap;
     private Canvas mCanvas;
     private Path mPath;
     private Paint mBitmapPaint;
     private Paint       mPaint;
     private Paint mCursorPaint;
     private Rect invalRegion;
 
     public WhiteBoardCanvas(Context context, AttributeSet attrs) {
         super(context, attrs);
         mPath = new Path();
         mBitmapPaint = new Paint(Paint.DITHER_FLAG);
 
         mPaint = new Paint();
         mPaint.setAntiAlias(true);
         mPaint.setDither(true);
         mPaint.setColor(0xFF000000);
         mPaint.setStyle(Paint.Style.STROKE);
         mPaint.setStrokeJoin(Paint.Join.ROUND);
         mPaint.setStrokeCap(Paint.Cap.ROUND);
         mPaint.setStrokeWidth(12);
         invalRegion = new Rect(0, 0, 0, 0);
 
         mCursorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
         mCursorPaint.setStyle(Paint.Style.STROKE);
         mCursorPaint.setPathEffect(new DashPathEffect(new float[]{5, 2}, 0));
     }
 
     int mWidth;
     int mHeight;
     int mX1, mX2, mY1, mY2;
     protected void onSizeChanged(int w, int h, int oldw, int oldh) {
         super.onSizeChanged(w, h, oldw, oldh);
         mCenterX = ((float)w)/2F;
         mCenterY = ((float)h)/2F;
         mWidth = w;
         mHeight = h;
         mX1 = CROSS_SIZE*2;
         mY1 = CROSS_SIZE*2;
         mX2 = mWidth-(CROSS_SIZE*2);
         mY2 = mHeight-(CROSS_SIZE*2);
         resetCanvas(w, h);
     }
 
     public void resetCanvas() {
         resetCanvas(mBitmap.getWidth(), mBitmap.getHeight());
         invalidate();
     }
     public void resetCanvas(int w, int h) {
         mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
         mBitmap.eraseColor(Color.WHITE);
         mCanvas = new Canvas(mBitmap);
     }
 
     protected void onDraw(Canvas canvas) {
         canvas.drawColor(0xFFFFFFFF);
 
         synchronized(mBitmap) {
             canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
         }
 
         canvas.drawPath(mPath, mPaint);
         canvas.drawOval(mBrushCursorRegion, mCursorPaint);
 
     }
 
     private final int CROSS_SIZE = 20;
     private void drawCross(Canvas canvas, float x, float y) {
         canvas.drawLine(x-CROSS_SIZE, y, x+CROSS_SIZE, y, mCursorPaint);
         canvas.drawLine(x, y-CROSS_SIZE, x, y+CROSS_SIZE, mCursorPaint);
     }
 
     private float mCenterX, mCenterY;
 
     private float mX, mY;
     private static final float TOUCH_TOLERANCE = 4;
 
     private static final float CURSOR_SIZE=10;
     RectF mBrushCursorRegion = new RectF(0f, 0f, 0f, 0f);
 
     private void setBrushCursorPos(float x, float y)
     {
         mBrushCursorRegion = new RectF(x-CURSOR_SIZE/2, y-CURSOR_SIZE/2,
                 x+CURSOR_SIZE/2, y+CURSOR_SIZE/2);
 
     }
 
 
     boolean mDownHandled = false;
     private RectF invalF = new RectF();
     private Rect tmpInval = new Rect();
 
     public boolean onTouchEvent(MotionEvent event) {
         float x = event.getX();
         float y = event.getY();
         setBrushCursorPos(x, y);
         switch (event.getAction()) {
             case MotionEvent.ACTION_DOWN:
                 mDownHandled = true;
                 mPath.reset();
                 mPath.moveTo(x, y);
                 mX = x;
                 mY = y;
                 invalidate();
                 break;
             case MotionEvent.ACTION_MOVE:
                 if(!mDownHandled)
                     break;
                 float dx = Math.abs(x - mX);
                 float dy = Math.abs(y - mY);
                 if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
                     mPath.quadTo(mX, mY, (x + mX)/2, (y + mY)/2);
                     mX = x;
                     mY = y;
                     updateInvalRegion();
                     mCanvas.drawPath(mPath, mPaint);
                 }
                 invalidate();
                 break;
             case MotionEvent.ACTION_UP:
                 if(!mDownHandled)
                     break;
                 mDownHandled = false;
                 mPath.lineTo(mX, mY);
                 updateInvalRegion();
                 mCanvas.drawPath(mPath, mPaint);
                 mPath.reset();
                 invalidate();
                 break;
         }
         return true;
 
     }
 
     private void updateInvalRegion() {
         mPath.computeBounds(invalF, false);
         invalF.roundOut(tmpInval);
        widen(tmpInval);
         invalRegion.union(tmpInval);
     }
 
    static final int BRUSH_WIDTH = 6;
    private void widen(Rect tmpInval) {
        int newLeft = Math.max(0, tmpInval.left-BRUSH_WIDTH);
        int newTop = Math.max(0, tmpInval.top - BRUSH_WIDTH);
        int newRight = Math.min(mWidth, tmpInval.right+BRUSH_WIDTH);
        int newBottom = Math.min(mHeight, tmpInval.bottom+BRUSH_WIDTH);
        tmpInval.set(newLeft, newTop, newRight, newBottom);
    }

     public Bitmap getBitmap() { return mBitmap;}
 
     @Override
     public void pullUpdateRegion(int[] pixelBufs, Rect inval) {
         synchronized(mBitmap) {
             inval.set(invalRegion);
             int stride = mBitmap.getWidth();
             int offset = inval.left+inval.top*stride;
             Log.d("WBCast", "mWidth=" + mWidth + ", mHeight=" + mHeight);
             mBitmap.getPixels(pixelBufs, offset, stride,  inval.left, inval.top, inval.width(), inval.height());
 //            mBitmap.getPixels(pixelBufs, 0, mWidth,  0, 0, mWidth, mHeight);
 
             invalRegion.set(0, 0, 0, 0);
         }
     }
 }
