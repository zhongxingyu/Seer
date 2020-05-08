 package org.g_okuyama.capture;
 
 import android.view.View;
 
 import android.content.Context;
 import android.content.res.Resources;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Paint;
 import android.graphics.PorterDuff.Mode;
 import android.view.MotionEvent;
 
 public class OverlayView extends View {
     private static final boolean DEBUG = false;
     
     Context mContext;
     CameraPreview mPreview;
     
     Paint mPaint;
     
     private Bitmap  mBitmap;
     private Canvas  mCanvas;
     private Paint   mBitmapPaint;
     
     private Bitmap mFocus;
     int mWidth;
     int mHeight;
     
     int mSizeX;
     int mSizeY;
     int mBaseX;
     int mBaseY;
 
     public OverlayView(CameraPreview preview, Context context) {
         super(context);
         mContext = context;
         mPreview = preview;
         
         mBitmapPaint = new Paint(Paint.DITHER_FLAG);
         
         //tH[JXɕ\ACR
         Resources r = getResources();
         mFocus = BitmapFactory.decodeResource(r, R.drawable.focus_prev);
         mWidth = mFocus.getWidth();
         mHeight = mFocus.getHeight();
 
         mSizeX = getWidth();
         mSizeY = getHeight();
         mBaseX = getLeft();
         mBaseY = getTop();
     }
 
     public boolean onTouchEvent(MotionEvent event){
         //B[h̓tH[JXăACR\Ȃ
         if(((ContShooting)mContext).isMask()){
             mPreview.doAutoFocus();
             return false;
         }
                 
         switch (event.getAction()) {
             case MotionEvent.ACTION_DOWN:
                 //^񒆂ɕ\
                 float x = mBaseX + (mSizeX/2) - (mWidth/2);
                 float y = mBaseY + (mSizeY/2) - (mHeight/2);
                 mCanvas.drawBitmap(mFocus, x, y, null);
                 invalidate();
                 break;
             case MotionEvent.ACTION_MOVE:
                 invalidate();
                 break;
             case MotionEvent.ACTION_UP:
                 invalidate();
                 break;
         }
         
         mPreview.doAutoFocus();
         
         return false;
     }
 
     @Override
     protected void onSizeChanged(int w, int h, int oldw, int oldh) {
         super.onSizeChanged(w, h, oldw, oldh);
         mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
         mCanvas = new Canvas(mBitmap);
         mSizeX = w;
         mSizeY = h;
     }
     
     @Override
     protected void onDraw(Canvas canvas){
         canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
     }
 
     void clearCanvas(){
         mCanvas.drawColor(0,Mode.CLEAR);
         invalidate();
     }
     
     void displayFocus(){
         if(((ContShooting)mContext).isMask()){
             return;
         }
         
         //^񒆂ɕ\
         float x = mBaseX + (mSizeX/2) - (mWidth/2);
         float y = mBaseY + (mSizeY/2) - (mHeight/2);
         mCanvas.drawBitmap(mFocus, x, y, null);
         invalidate();
     }
 }
