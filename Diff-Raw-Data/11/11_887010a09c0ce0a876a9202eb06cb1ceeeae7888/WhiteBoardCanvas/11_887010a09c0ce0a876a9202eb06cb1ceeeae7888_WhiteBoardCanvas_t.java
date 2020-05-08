 package com.livejournal.karino2.whiteboardcast;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.DashPathEffect;
 import android.graphics.Paint;
 import android.graphics.Path;
 import android.graphics.PorterDuff;
 import android.graphics.PorterDuffXfermode;
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
 
     Bitmap viewBmp;
     Bitmap commitedBmp;
     private Canvas mCanvas;
     Canvas commitedCanvas;
     private Path mPath;
     private Paint mBitmapPaint;
     private Paint       mPaint;
     private Paint mCursorPaint;
     private Rect invalRegion;
 
     FloatingOverlay overlay;
 
     static final int DEFAULT_PEN_WIDTH = 6;
     UndoList undoList = new UndoList();
 
     public WhiteBoardCanvas(Context context, AttributeSet attrs) {
         super(context, attrs);
         mPath = new Path();
         mBitmapPaint = new Paint(Paint.DITHER_FLAG);
 
         overlay = new FloatingOverlay((WhiteBoardCastActivity)context, 0);
 
         mPaint = new Paint();
         mPaint.setColor(Color.DKGRAY);
         mPaint.setAntiAlias(true);
         mPaint.setDither(true);
         mPaint.setStyle(Paint.Style.STROKE);
         mPaint.setStrokeJoin(Paint.Join.ROUND);
         mPaint.setStrokeCap(Paint.Cap.ROUND);
         mPaint.setStrokeWidth(DEFAULT_PEN_WIDTH);
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
        // align for livbpx.
        // Log.d("WBCast", "before, w,h=" + w+ "," + h);
        w = (w+15) & ~15;
        h = (h+15) & ~15;
        // Log.d("WBCast", "after, w,h=" + w+ "," + h);
         mCenterX = ((float)w)/2F;
         mCenterY = ((float)h)/2F;
         mWidth = w;
         mHeight = h;
         mX1 = CROSS_SIZE*2;
         mY1 = CROSS_SIZE*2;
         mX2 = mWidth-(CROSS_SIZE*2);
         mY2 = mHeight-(CROSS_SIZE*2);
         resetCanvas(w, h);
         overlay.onSize(w, h);
     }
 
     public void resetCanvas() {
         resetCanvas(viewBmp.getWidth(), viewBmp.getHeight());
         invalidate();
     }
     public void resetCanvas(int w, int h) {
         viewBmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
         viewBmp.eraseColor(Color.WHITE);
         commitedBmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
         commitedBmp.eraseColor(Color.WHITE);
         mCanvas = new Canvas(viewBmp);
         commitedCanvas = new Canvas(commitedBmp);
     }
 
     protected void onDraw(Canvas canvas) {
         canvas.drawColor(0xFFFFFFFF);
 
         synchronized(viewBmp) {
             canvas.drawBitmap(viewBmp, 0, 0, mBitmapPaint);
         }
 
         // canvas.drawPath(mPath, mPaint);
 
         // should write to viewBmp. but later.
         canvas.drawOval(mBrushCursorRegion, mCursorPaint);
 
         overlay.onDraw(canvas);
 
     }
 
     private final int CROSS_SIZE = 20;
     private void drawCross(Canvas canvas, float x, float y) {
         canvas.drawLine(x-CROSS_SIZE, y, x+CROSS_SIZE, y, mCursorPaint);
         canvas.drawLine(x, y-CROSS_SIZE, x, y+CROSS_SIZE, mCursorPaint);
     }
 
     private float mCenterX, mCenterY;
 
     private float mX, mY;
     private static final float TOUCH_TOLERANCE = 4;
 
     RectF mBrushCursorRegion = new RectF(0f, 0f, 0f, 0f);
 
     private float getCursorSize() {
         return (float)penWidth;
     }
 
     private void setBrushCursorPos(float x, float y)
     {
         mBrushCursorRegion = new RectF(x-getCursorSize()/2, y-getCursorSize()/2,
                 x+getCursorSize()/2, y+getCursorSize()/2);
 
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
                 if(overlay.onTouchDown(x, y)) {
                     invalidate();
                     break;
                 }
                 mDownHandled = true;
                 mPath.reset();
                 mPath.moveTo(x, y);
                 mX = x;
                 mY = y;
                 invalidate();
                 break;
             case MotionEvent.ACTION_MOVE:
                 if(overlay.onTouchMove(x, y)) {
                     invalidate();
                     break;
                 }
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
                 if(overlay.onTouchUp(x, y)) {
                     invalidate();
                     break;
                 }
                 if(!mDownHandled)
                     break;
                 mDownHandled = false;
                 mPath.lineTo(mX, mY);
 
                 boolean canUndoBefore = undoList.canUndo();
 
                 Rect region = pathBound();
                 Bitmap undo = Bitmap.createBitmap(commitedBmp, region.left, region.top, region.width(), region.height() );
                 commitedCanvas.drawPath(mPath, mPaint);
                 Bitmap redo = Bitmap.createBitmap(commitedBmp, region.left, region.top, region.width(), region.height());
                 undoList.pushUndoCommand(region.left, region.top, undo, redo);
                 invalRegion.union(region);
                 mCanvas.drawPath(mPath, mPaint);
                 mPath.reset();
                 if(undoList.canUndo() != canUndoBefore) {
                     overlay.changeUndoStatus();
                 }
                 invalidate();
                 break;
         }
         return true;
 
     }
 
 
 
     private Rect pathBound() {
         mPath.computeBounds(invalF, false);
         invalF.roundOut(tmpInval);
         widen(tmpInval);
         return tmpInval;
     }
 
 
     private void updateInvalRegion() {
         invalRegion.union(pathBound());
     }
 
     static final int BRUSH_WIDTH = 6;
     private void widen(Rect tmpInval) {
         int newLeft = Math.max(0, tmpInval.left-BRUSH_WIDTH);
         int newTop = Math.max(0, tmpInval.top - BRUSH_WIDTH);
         int newRight = Math.min(mWidth, tmpInval.right+BRUSH_WIDTH);
         int newBottom = Math.min(mHeight, tmpInval.bottom+BRUSH_WIDTH);
         tmpInval.set(newLeft, newTop, newRight, newBottom);
     }
 
     public Bitmap getBitmap() { return viewBmp;}
 
     public void setWholeAreaInvalidate() {
         invalRegion.set(0, 0, mWidth, mHeight);
     }
 
     public boolean canUndo() {
         return undoList.canUndo();
     }
 
     public void undo() {
         Rect undoInval = undoList.undo(commitedCanvas, mPaint);
         afterUndoRedo(undoInval);
     }
 
     public void redo() {
         Rect undoInval = undoList.redo(commitedCanvas, mPaint);
         afterUndoRedo(undoInval);
     }
 
     private void afterUndoRedo(Rect undoInval) {
         synchronized (viewBmp) {
             mCanvas.drawBitmap(commitedBmp, undoInval, undoInval, mPaint);
             invalRegion.union(undoInval);
         }
         invalidate(undoInval);
     }
 
 
     @Override
     public void pullUpdateRegion(int[] pixelBufs, Rect inval) {
         synchronized(viewBmp) {
             inval.set(invalRegion);
             int stride = viewBmp.getWidth();
             int offset = inval.left+inval.top*stride;
             viewBmp.getPixels(pixelBufs, offset, stride,  inval.left, inval.top, inval.width(), inval.height());
 
             invalRegion.set(0, 0, 0, 0);
         }
     }
 
     public void clearCanvas() {
         boolean canUndoBefore = undoList.canUndo();
         Bitmap undo = Bitmap.createBitmap(commitedBmp, 0, 0, viewBmp.getWidth(), viewBmp.getHeight() );
 
         commitedBmp.eraseColor(Color.WHITE);
         viewBmp.eraseColor(Color.WHITE);
         invalRegion.set(0, 0, viewBmp.getWidth(), viewBmp.getHeight());
 
         Bitmap redo = Bitmap.createBitmap(commitedBmp, 0, 0, viewBmp.getWidth(), viewBmp.getHeight() );
         undoList.pushUndoCommand(0, 0, undo, redo);
         if(undoList.canUndo() != canUndoBefore) {
             overlay.changeUndoStatus();
         }
     }
 
     private int penWidth = DEFAULT_PEN_WIDTH;
 
     private void setPenWidth(int width) {
         mPaint.setStrokeWidth(width);
         penWidth = width;
     }
 
     public void setPenOrEraser(int penIndex) {
         setPenWidth(DEFAULT_PEN_WIDTH);
 
         switch(penIndex) {
             case FloatingOverlay.PEN_INDEX_BLACK:
                 mPaint.setColor(Color.DKGRAY);
                 break;
             case FloatingOverlay.PEN_INDEX_BLUE:
                 mPaint.setColor(Color.BLUE);
                 break;
             case FloatingOverlay.PEN_INDEX_RED:
                 mPaint.setColor(Color.RED);
                 break;
             case FloatingOverlay.PEN_INDEX_GREEN:
                 mPaint.setColor(Color.GREEN);
                 break;
             case FloatingOverlay.PEN_INDEX_ERASER:
                 mPaint.setColor(Color.WHITE);
                 setPenWidth(40);
                 break;
         }
     }
 
     public void changeRecStatus(WhiteBoardCastActivity.RecordStatus recStats) {
         overlay.changeRecStatus();
         invalidate();
     }
 
     public boolean canRedo() {
         return undoList.canRedo();
     }
 
 }
