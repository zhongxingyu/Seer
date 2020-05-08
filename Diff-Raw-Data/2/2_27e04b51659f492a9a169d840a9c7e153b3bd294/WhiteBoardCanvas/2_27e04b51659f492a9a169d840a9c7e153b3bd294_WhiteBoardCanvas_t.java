 package com.livejournal.karino2.whiteboardcast;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.DashPathEffect;
 import android.graphics.Paint;
 import android.graphics.Path;
 import android.graphics.PorterDuff;
 import android.graphics.PorterDuffXfermode;
 import android.graphics.Rect;
 import android.graphics.RectF;
 import android.os.Handler;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.MotionEvent;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ListPopupWindow;
 import android.widget.PopupMenu;
 import android.widget.PopupWindow;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Created by karino on 6/26/13.
  */
 public class WhiteBoardCanvas extends View implements FrameRetrieval, PageScrollAnimator.Animatee , UndoList.Undoable {
 
     // penCanvasBmp + BGBmp = viewBmp.
     Bitmap viewBmp;
     Bitmap penCanvasBmp;
 
     private Canvas penCanvas;
     Canvas viewCanvas;
     Bitmap cursorBackupBmp;
     int[] cursorBackupPixels;
     Canvas cursorBackupCanvas;
 
 
     Canvas committedCanvas;
     private Path mPath;
     private Paint mBitmapPaint;
     private Paint       mPaint;
     private Paint undoRedoPaint;
     private Paint mCursorPaint;
     private Rect invalRegion;
 
     BoardList boardList;
 
     FloatingOverlay overlay;
 
     static final int DEFAULT_PEN_WIDTH = 6;
     static final int ERASER_WIDTH = 60;
 
     private boolean isAnimating = false;
 
     int penCursorWidth, penCursorHeight;
 
     private List<File> slides = new ArrayList<File>(); // null object.
 
 
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
 
         undoRedoPaint = new Paint();
         undoRedoPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST));
 
         mCursorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
         mCursorPaint.setStyle(Paint.Style.STROKE);
         mCursorPaint.setPathEffect(new DashPathEffect(new float[]{5, 2}, 0));
 
         boardList = new BoardList(this);
         penCursorWidth = (ERASER_WIDTH+2*CURSOR_MARGIN);
         penCursorHeight = (ERASER_WIDTH+2*CURSOR_MARGIN);
         cursorBackupPixels = new int[penCursorWidth*penCursorHeight];
         cursorBackupBmp = Bitmap.createBitmap(penCursorWidth, penCursorHeight, Bitmap.Config.ARGB_8888);
         cursorBackupBmp.eraseColor(Color.TRANSPARENT);
         cursorBackupCanvas = new Canvas(cursorBackupBmp);
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
        if( w <= mWidth)
            return; // when activity changing, some Android version call this when orientation changed (for other Activity!). just ignore.
         mWidth = w;
         mHeight = h;
         mX1 = CROSS_SIZE*2;
         mY1 = CROSS_SIZE*2;
         mX2 = mWidth-(CROSS_SIZE*2);
         mY2 = mHeight-(CROSS_SIZE*2);
         resetCanvas(w, h);
         overlay.onSize(w, h);
     }
 
     public void resetCanvas(int w, int h) {
         penCanvasBmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
         penCanvasBmp.eraseColor(Color.TRANSPARENT);
         viewBmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
         viewBmp.eraseColor(Color.WHITE);
         viewCanvas = new Canvas(viewBmp);
 
         boardList.setSize(w, h);
         getCommittedBitmap().eraseColor(Color.TRANSPARENT);
         penCanvas = new Canvas(penCanvasBmp);
         committedCanvas = new Canvas(getCommittedBitmap());
     }
 
 
 
     protected void onDraw(Canvas canvas) {
         canvas.drawColor(0xFFFFFFFF);
 
         if(isAnimating) {
             synchronized(viewBmp) {
                 canvas.drawBitmap(viewBmp, 0, 0, mBitmapPaint);
             }
             return;
         }
 
         // drawToViewBmp();
         canvas.drawBitmap(viewBmp, 0, 0, mBitmapPaint);
 
 
         overlay.onDraw(canvas);
 
         drawFps(canvas);
 
     }
 
 
     private void drawToViewBmp(Rect region) {
         drawToViewBmp(region, penCanvasBmp);
 
     }
 
     private void drawToViewBmp(Rect region, Bitmap fg) {
         synchronized(viewBmp) {
             viewCanvas.drawBitmap(getCurrentBackground(), region, region, mBitmapPaint);
             viewCanvas.drawBitmap(fg, region, region, mBitmapPaint);
         }
     }
 
     private void drawToViewBmp() {
         Rect whole = new Rect(0, 0, mWidth, mHeight);
         drawToViewBmp(whole);
     }
 
     Paint fpsPaint = new Paint();
     private void drawFps(Canvas canvas) {
         if(!showFpsBar)
             return;
         drawFpsBar(canvas, encoderFpsCounter.cycleFps(), 5, 0xFF0000FF);
         drawFpsBar(canvas, paintFpsCounter.cycleFps(), 5+12, 0xFFFF0000);
     }
 
     private void drawFpsBar(Canvas canvas, int fps, int py, int fgColor) {
         if(fps == -1)
             return;
         fpsPaint.setColor(fgColor);
         final int px = 25;
         int m = 5;
         canvas.drawRect( new Rect( px, py, px + fps*m, py + 8), fpsPaint);
 
         fpsPaint.setColor(0xFFFFFF00);
         for (int i=0; i<=fps/10; i++)
         {
             canvas.drawRect( new Rect( px + i*m*10, py, px + i*m*10 + 2, py + 8), fpsPaint);
         }
     }
 
     private final int CROSS_SIZE = 20;
     private void drawCross(Canvas canvas, float x, float y) {
         canvas.drawLine(x-CROSS_SIZE, y, x+CROSS_SIZE, y, mCursorPaint);
         canvas.drawLine(x, y-CROSS_SIZE, x, y+CROSS_SIZE, mCursorPaint);
     }
 
     private float mX, mY;
     private static final float TOUCH_TOLERANCE = 4;
 
     RectF mBrushCursorRegion = new RectF(0f, 0f, 0f, 0f);
     Rect lastBrushCursorRegion = new Rect(0, 0, 0, 0);
 
     boolean isRectValid(Rect region) {
         return region.width() != 0;
     }
 
     boolean isRectFValid(RectF region) {
         return region.width() >= 0.1;
     }
 
     static final int CURSOR_MARGIN = 2;
 
     void backupCursorRegion(RectF region) {
         region.roundOut(lastBrushCursorRegion);
         widen(lastBrushCursorRegion, CURSOR_MARGIN);
         fitIntoBrushSize(lastBrushCursorRegion);
         if(lastBrushCursorRegion.width() <=0 || lastBrushCursorRegion.height() <= 0) {
             makeRegionInvalid(lastBrushCursorRegion);
             return;
         }
         penCanvasBmp.getPixels(cursorBackupPixels, 0, penCursorWidth,
                 lastBrushCursorRegion.left, lastBrushCursorRegion.top,
                 lastBrushCursorRegion.width(), lastBrushCursorRegion.height());
         /*
         Rect dest = new Rect(0, 0, lastBrushCursorRegion.width(), lastBrushCursorRegion.height());
         // TODO: too slow?
         cursorBackupBmp.eraseColor(Color.TRANSPARENT);
         cursorBackupCanvas.drawBitmap(penCanvasBmp, lastBrushCursorRegion, dest, null);
         */
     }
 
     private void fitIntoBrushSize(Rect lastBrushCursorRegion) {
         fitInsideScreen(lastBrushCursorRegion);
         lastBrushCursorRegion.intersect(lastBrushCursorRegion.left, lastBrushCursorRegion.top,
                 lastBrushCursorRegion.left + penCursorWidth, lastBrushCursorRegion.top + penCursorHeight);
 
         /*
         lastBrushCursorRegion.set(lastBrushCursorRegion.left, lastBrushCursorRegion.top,
                 lastBrushCursorRegion.left+Math.min(lastBrushCursorRegion.width(), penCursorWidth),
                 lastBrushCursorRegion.top+Math.min(lastBrushCursorRegion.height(), penCursorHeight));
                 */
     }
 
     private void revertBrushDrawnRegionIfNecessary() {
         if(!isRectValid(lastBrushCursorRegion))
             return;
         /*
         Rect tmp = new Rect(0, 0, lastBrushCursorRegion.width(), lastBrushCursorRegion.height());
         penCanvas.drawBitmap(cursorBackupBmp, tmp, lastBrushCursorRegion, null);
         */
         penCanvasBmp.setPixels(cursorBackupPixels, 0, penCursorWidth,
                 lastBrushCursorRegion.left, lastBrushCursorRegion.top,
                 lastBrushCursorRegion.width(), lastBrushCursorRegion.height());
 
         drawToViewBmp(lastBrushCursorRegion);
 
         invalViewBmpRegion(lastBrushCursorRegion);
 
         makeRegionInvalid(lastBrushCursorRegion);
     }
 
     private void invalViewBmpRegionF(RectF regionF) {
         Rect region = new Rect();
         regionF.roundOut(region);
         invalViewBmpRegion(region);
     }
 
     void fitInsideScreen(Rect region) {
         region.intersect(0, 0, mWidth, mHeight);
     }
 
     private void invalViewBmpRegion(Rect region) {
         invalRegionForEncoder(region);
         invalidate(region.left, region.top, region.right, region.bottom);
     }
 
     private void invalRegionForEncoder(Rect region) {
         invalRegion.union(region);
         fitInsideScreen(invalRegion);
     }
 
 
     void drawBrushCursorIfNecessary() {
         revertBrushDrawnRegionIfNecessary();
 
         if(isRectFValid(mBrushCursorRegion)) {
             backupCursorRegion(mBrushCursorRegion);
             penCanvas.drawOval(mBrushCursorRegion, mCursorPaint);
 
             Rect region = new Rect();
             mBrushCursorRegion.roundOut(region);
             drawToViewBmp(region);
 
             invalViewBmpRegionF(mBrushCursorRegion);
         }
     }
 
     private float getCursorSize() {
         return (float)penWidth;
     }
 
     private void setBrushCursorPos(float x, float y)
     {
         mBrushCursorRegion.set(x-getCursorSize()/2, y-getCursorSize()/2,
                 x+getCursorSize()/2, y+getCursorSize()/2);
 
     }
 
     void makeRegionInvalid(Rect region)
     {
         region.set(0, 0, 0, 0);
     }
     void makeRegionInvalidF(RectF region) {
         region.set(0f, 0f, 0f, 0f);
     }
     
     private void eraseBrushCursor() {
         makeRegionInvalidF(mBrushCursorRegion);
         revertBrushDrawnRegionIfNecessary();
     }
 
 
     boolean mDownHandled = false;
     private RectF invalF = new RectF();
     private Rect tmpInval = new Rect();
 
     private boolean overTolerance(float x, float y) {
         float dx = Math.abs(x - mX);
         float dy = Math.abs(y - mY);
         return (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE);
     }
 
     @Override
     public boolean onHoverEvent(MotionEvent event) {
         if(isAnimating)
             return super.onHoverEvent(event);
         if(event.getAction() == MotionEvent.ACTION_HOVER_EXIT) {
             eraseBrushCursor();
         }else {
             float x = event.getX();
             float y = event.getY();
             drawBrush(x, y);
         }
         return super.onHoverEvent(event);
     }
 
     private void drawBrush(float x, float y) {
         setBrushCursorPos(x, y);
         drawBrushCursorIfNecessary();
     }
 
     public boolean onTouchEvent(MotionEvent event) {
         if(isAnimating)
             return true;
 
         float x = event.getX();
         float y = event.getY();
         setBrushCursorPos(x, y);
         revertBrushDrawnRegionIfNecessary();
         onTouchWithoutCursor(event, x, y);
         drawBrushCursorIfNecessary();
         return true;
 
     }
 
     private void onTouchWithoutCursor(MotionEvent event, float x, float y) {
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
                 paintFpsCounter.push(System.currentTimeMillis());
                 if(overlay.onTouchMove(x, y)) {
                     invalidate();
                     break;
                 }
                 if(!mDownHandled)
                     break;
                 if (overTolerance(x, y)) {
                     int historySize = event.getHistorySize();
                     if(historySize == 0) {
                         mPath.quadTo(mX, mY, (x + mX)/2, (y + mY)/2);
                         mX = x;
                         mY = y;
                     }else {
                         for(int i = 0; i < event.getHistorySize(); i++) {
                             float hx = event.getHistoricalX(i);
                             float hy = event.getHistoricalY(i);
                             if(overTolerance(hx, hy)) {
                                 mPath.quadTo(mX, mY, (hx + mX)/2, (hy + mY)/2);
                                 mX = hx;
                                 mY = hy;
                             }
                         }
                     }
                     penCanvas.drawPath(mPath, mPaint);
                     drawToViewBmp(pathBound());
                     updateInvalRegionForEncoder();
                 }
                 // no tolerance
                 /*
                 mPath.quadTo(mX, mY, (x + mX)/2, (y + mY)/2);
                 mX = x;
                 mY = y;
                 updateInvalRegionForEncoder();
                 penCanvas.drawPath(mPath, mPaint);
                 */
                 // no tolerance done.
 
                 invalidate(pathBound());
                 break;
             case MotionEvent.ACTION_UP:
                 eraseBrushCursor();
                 if(overlay.onTouchUp(x, y)) {
                     invalidate();
                     break;
                 }
                 if(!mDownHandled)
                     break;
                 mDownHandled = false;
                 mPath.lineTo(mX, mY);
 
                 boolean canUndoBefore = getUndoList().canUndo();
 
                 Rect region = pathBound();
                 Bitmap undo = Bitmap.createBitmap(getCommittedBitmap(), region.left, region.top, region.width(), region.height() );
                 committedCanvas.drawPath(mPath, mPaint);
                 Bitmap redo = Bitmap.createBitmap(getCommittedBitmap(), region.left, region.top, region.width(), region.height());
 
                 pushUndoCommand(region, undo, redo);
 
                 invalRegionForEncoder(region);
                 penCanvas.drawPath(mPath, mPaint);
                 mPath.reset();
                 if(getUndoList().canUndo() != canUndoBefore) {
                     overlay.changeUndoStatus();
                 }
                 invalidate();
                 break;
         }
     }
 
     private void pushUndoCommand(Rect region, Bitmap undo, Bitmap redo) {
         getUndoList().pushBitmapUndoCommand(region.left, region.top, undo, redo);
     }
 
 
     private Rect pathBound() {
         mPath.computeBounds(invalF, false);
         invalF.roundOut(tmpInval);
         widenPenWidth(tmpInval);
         fitInsideScreen(tmpInval);
         return tmpInval;
     }
 
 
     private void updateInvalRegionForEncoder() {
         invalRegionForEncoder(pathBound());
     }
 
     private void widenPenWidth(Rect tmpInval) {
         int penWidth = (int)getCursorSize();
         widen(tmpInval, penWidth);
     }
 
     private void widen(Rect tmpInval, int width) {
         int newLeft = Math.max(0, tmpInval.left- width);
         int newTop = Math.max(0, tmpInval.top - width);
         int newRight = Math.min(mWidth, tmpInval.right+ width);
         int newBottom = Math.min(mHeight, tmpInval.bottom+ width);
         tmpInval.set(newLeft, newTop, newRight, newBottom);
     }
 
     public Bitmap getBitmap() { return viewBmp;}
 
 
     public boolean canUndo() {
         return getUndoList().canUndo();
     }
 
     public void undo() {
         getUndoList().undo();
     }
 
     public void redo() {
         getUndoList().redo();
     }
 
     private void overwriteByBmp(Bitmap target, Bitmap bmp, Rect region) {
         int[] buf = new int[region.width()*region.height()];
         bmp.getPixels(buf, 0, region.width(), region.left, region.top, region.width(), region.height());
         target.setPixels(buf, 0, region.width(), region.left, region.top, region.width(), region.height());
     }
 
     @Override
     public void invalCommitedBitmap(Rect undoInval) {
         drawToViewBmp(undoInval, getCommittedBitmap());
         overwriteByBmp(penCanvasBmp, getCommittedBitmap(), undoInval);
         invalRegionForEncoder(undoInval);
         invalidate(undoInval);
     }
 
     @Override
     public void changeUndoStatus() {
         overlay.changeUndoStatus();
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
         boolean canUndoBefore = getUndoList().canUndo();
         Bitmap undo = Bitmap.createBitmap(getCommittedBitmap(), 0, 0, viewBmp.getWidth(), viewBmp.getHeight() );
 
         getCommittedBitmap().eraseColor(Color.TRANSPARENT);
         penCanvasBmp.eraseColor(Color.TRANSPARENT);
         afterChangeBGImage();
 
         Bitmap redo = Bitmap.createBitmap(getCommittedBitmap(), 0, 0, viewBmp.getWidth(), viewBmp.getHeight() );
         getUndoList().pushBitmapUndoCommand(0, 0, undo, redo);
         if(getUndoList().canUndo() != canUndoBefore) {
             overlay.changeUndoStatus();
         }
     }
 
     public void invalWholeRegionForEncoder() {
         invalRegion.set(0, 0, viewBmp.getWidth(), viewBmp.getHeight());
     }
 
     private int penWidth = DEFAULT_PEN_WIDTH;
 
     private void setPenWidth(int width) {
         mPaint.setStrokeWidth(width);
         penWidth = width;
     }
 
     public void setPenOrEraser(int penIndex) {
         mPaint.setXfermode(null);
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
                 mPaint.setXfermode(new PorterDuffXfermode(
                         PorterDuff.Mode.CLEAR));
                 // mPaint.setColor(Color.WHITE);
                 setPenWidth(ERASER_WIDTH);
                 break;
         }
     }
 
     public void changeRecStatus() {
         overlay.changeRecStatus();
         invalidate();
     }
 
     public void changeSlidesStatus() {
         overlay.changeSlidesStatus();
         invalidate();
     }
 
     public boolean canRedo() {
         return getUndoList().canRedo();
     }
 
     FpsCounter paintFpsCounter = new FpsCounter(3);
     FpsCounter encoderFpsCounter = new FpsCounter(12);
     private boolean showFpsBar = false;
 
     public void enableDebug(boolean enabled) {
         showFpsBar = enabled;
     }
 
     public EncoderTask.FpsListener getEncoderFpsCounter() {
         return new EncoderTask.FpsListener() {
             @Override
             public void push(long currentFrameMill) {
                 encoderFpsCounter.push(currentFrameMill);
             }
         };
     }
 
     private Board getCurrentBoard() {
         return boardList.getCurrent();
     }
 
     @Override
     public Bitmap getCommittedBitmap() {
         return getCurrentBoard().getBoardBmp();
     }
 
 
     private Bitmap getCurrentBackground() {
         return getCurrentBoard().getBackgroundBmp();
     }
 
     private UndoList getUndoList() {
         return getCurrentBoard().getUndoList();
     }
 
     private void afterChangeBoard() {
         synchronized (viewBmp) {
             invalWholeRegionForEncoder();
             viewCanvas.drawBitmap(getCurrentBackground(), 0, 0, mBitmapPaint);
             viewCanvas.drawBitmap(getCommittedBitmap(), 0, 0, mBitmapPaint);
         }
         // TODO: slow.
         penCanvasBmp = getCommittedBitmap().copy(Bitmap.Config.ARGB_8888, true);
         penCanvas = new Canvas(penCanvasBmp);
         committedCanvas = new Canvas(getCommittedBitmap());
 
         overlay.changeUndoStatus();
 
         invalidate();
     }
 
     boolean pageUp() {
         isAnimating = false;
         if(boardList.pagePrev()) {
             afterChangeBoard();
             return true;
         }
         return false;
     }
 
     void pageDown() {
         isAnimating = false;
         boardList.pageNext();
        afterChangeBoard();
     }
 
     void updateScreenUIThread() {
         invalWholeRegionForEncoder();
         invalidate();
     }
 
     @Override
     public void start() {
         isAnimating = true;
     }
 
     Handler handler = new Handler();
     Runnable updateScreenRunnable = new Runnable() {
         @Override
         public void run() {
             updateScreenUIThread();
         }
     };
     @Override
     public void updateScreen() {
         handler.post(updateScreenRunnable);
     }
 
     @Override
     public void done(PageScrollAnimator.Direction dir) {
         if(dir == PageScrollAnimator.Direction.Next) {
             handler.post(new Runnable() {
                 @Override
                 public void run() {
                     pageDown();
                 }
             });
         } else {
             handler.post(new Runnable() {
                 @Override
                 public void run() {
                     pageUp();
                 }
             });
         }
     }
 
     public boolean beginPagePrev(PageScrollAnimator animator) {
         if(!boardList.hasPrevPage())
             return false;
         animator.start(boardList.createPrevSynthesizedBmp(), getCurrentBoard().createSynthesizedTempBmp(), viewBmp, PageScrollAnimator.Direction.Prev);
         return true;
     }
 
     public void beginPageNext(PageScrollAnimator animator) {
         animator.start(getCurrentBoard().createSynthesizedTempBmp(), boardList.createNextSynthesizedBmp(),  viewBmp, PageScrollAnimator.Direction.Next);
     }
 
     public void newPresentation() {
         boardList = new BoardList(this);
         if(popup != null) {
             popup.dismiss();
         }
         popup = null;
         popupShown = false;
         resetCanvas(mWidth, mHeight);
         afterChangeBoard();
     }
 
     public int getStoredWidth() {
         return mWidth;
     }
 
     public int getStoredHeight() {
         return mHeight;
     }
 
     class InsertBGUndoRedoCommand implements UndoList.UndoCommand {
         Board.BackgroundImage prev;
         Board.BackgroundImage cur;
         InsertBGUndoRedoCommand(Board.BackgroundImage cur, Board.BackgroundImage prev) {
             this.prev = prev;
             this.cur = cur;
         }
 
         @Override
         public void undo(UndoList.Undoable undoTarget) {
             getCurrentBoard().setBackground(prev);
             afterChangeBGImage();
         }
 
         @Override
         public void redo(UndoList.Undoable undoTarget) {
             getCurrentBoard().setBackground(cur);
             afterChangeBGImage();
         }
 
         @Override
         public int getByteSize() {
             return 0;
         }
     }
 
 
     public void insertNewBGFile(File file) {
         Board.BackgroundImage newBackground = new Board.BackgroundImage(file);
         Board.BackgroundImage prev = getCurrentBoard().setBackground(newBackground);
         prev.discardBitmap();
         newBackground.discardBitmap();
         getUndoList().pushUndoCommand(new InsertBGUndoRedoCommand(newBackground, prev));
         overlay.changeUndoStatus();
         afterChangeBGImage();
     }
 
     private void afterChangeBGImage() {
         drawToViewBmp();
         invalWholeRegionForEncoder();
         invalidate();
     }
 
     public void setSlides(List<File> slides) {
         this.slides = slides;
         overlay.changeSlidesStatus();
     }
 
 
     FileImageAdapter slideAdapter;
     ListPopupWindow popup;
     ListPopupWindow getSlideWindow() {
         if(popup == null) {
             popup = new ListPopupWindow(getContext());
             popup.setAnchorView(this);
             popup.setHorizontalOffset(mWidth - mWidth / 6);
             popup.setVerticalOffset(-mHeight);
             slideAdapter = new FileImageAdapter(LayoutInflater.from(getContext()), slides, mWidth / 6, mHeight / 6);
             popup.setAdapter(slideAdapter);
             popup.setWidth(mWidth/6);
             popup.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                 @Override
                 public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                     File slide = slideAdapter.reverseLookUp(id);
                     insertNewBGFile(slide);
                     hideSlideWindow();
                 }
             });
         }
         return popup;
     }
 
 
     boolean popupShown = false;
 
     public void toggleShowSlides() throws IOException {
         if(popupShown) {
             hideSlideWindow();
         }
         else {
             showSlideWindow();
         }
     }
 
     private void showSlideWindow() {
         popupShown = true;
         getSlideWindow().show();
     }
 
     private void hideSlideWindow() {
         popupShown = false;
         getSlideWindow().dismiss();
     }
 
 
 
 }
