 package com.pepsdev.timedlight;
 
 import android.content.Context;
 import android.content.res.Resources;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.media.MediaPlayer;
 import android.util.AttributeSet;
 import android.util.DisplayMetrics;
 import android.util.Log;
 import android.view.GestureDetector;
 import android.view.MotionEvent;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 import android.view.View;
 import android.view.WindowManager;
 
 import alt.android.os.CountDownTimer;
 
 import android.graphics.Rect;
 
 
 public class TimedLightView extends SurfaceView
     implements SurfaceHolder.Callback {
 
     private static final String TAG = "com.pepsdev.timedlight";
     private static final int SPT = 250; // seconds per tick
 
     public interface OnTiretteListener {
         public void tiretted();
         public void unTiretted();
     }
 
     public void setOnTiretteListener(OnTiretteListener l) {
         tiretteListener = l;
     }
 
     private SurfaceHolder mSurfaceHolder;
     private GestureDetector gestureDetector;
     private OnTiretteListener tiretteListener;
 
     private CountDownTimer mCountDown;
     public boolean mCoundDownStarted = false;
 
     private float mDensity;
 
     private boolean mSurfaceCreated = false;
 
     private Bitmap currentLamp;
     private Bitmap lamp_off;
     private Bitmap lamp_on;
     private Bitmap handle;
 	private int handleHeight;
 
     private static final int HANDLE_DURATION = 1000 * 120; // 2 mins
 
     /* Following constants are density dependants
      * This is why they are not final
      */
     private int HANDLE_POS_DEFAULT;
     private int HANDLE_POS_X;
     private int HANDLE_POS_MAX;
     private Rect touchBox;
 
     private int handlePos; // bottom of the handle
     private boolean listeningToScroll = false;
 
     public void draw() {
         if (!mSurfaceCreated)
             return;
         Canvas c = null;
         try {
             c = mSurfaceHolder.lockCanvas();
             doDraw(c);
         } finally {
             if (c != null) {
                 mSurfaceHolder.unlockCanvasAndPost(c);
             }
         }
     }
 
     public void tick(long ms) {
         setTiretteDuration(ms);
 
         if (handlePos <= HANDLE_POS_DEFAULT) {
             unTiretted();
         }
     }
 
     public void doDraw(Canvas c) {
         c.drawARGB(255, 255, 255, 255);
         c.drawBitmap(handle, HANDLE_POS_X, handlePos - handleHeight, null);
         c.drawBitmap(currentLamp, 0, 0, null);
     }
 
     public long getTiretteDuration() {
         long msPerPx = HANDLE_DURATION / (HANDLE_POS_MAX - HANDLE_POS_DEFAULT);
 
         return msPerPx * (handlePos - HANDLE_POS_DEFAULT);
     }
 
     private void setTiretteDuration(long ms) {
         double pxPerMs = (double)(HANDLE_POS_MAX - HANDLE_POS_DEFAULT) /
                          (double) HANDLE_DURATION;
 
         handlePos = HANDLE_POS_DEFAULT + (int)(ms * pxPerMs);
         draw();
     }
 
     public void lightItUp() {
         playClick();
         currentLamp = lamp_on;
         draw();
     }
 
     public void switchOff() {
         playClick();
         reset();
     }
 
     public void reset() {
         currentLamp = lamp_off;
         setTiretteDuration(0);
     }
 
     public void toggle() {
         if (currentLamp == lamp_on) {
             switchOff();
         } else {
             lightItUp();
         }
     }
 
     public TimedLightView(Context context, AttributeSet attrs, int defStyle) {
         super(context, attrs, defStyle);
         init();
     }
 
     public TimedLightView(Context context, AttributeSet attrs) {
         super(context, attrs);
         init();
     }
 
     public TimedLightView(Context context) {
         super(context);
         init();
     }
 
     private void init() {
         mSurfaceHolder = getHolder();
         mSurfaceHolder.addCallback(this);
 
         lamp_off = BitmapFactory.decodeResource(getContext().getResources(),
                 R.drawable.lamp);
         lamp_on = BitmapFactory.decodeResource(getContext().getResources(),
                 R.drawable.lamp_hl);
         currentLamp = lamp_off;
         handle = BitmapFactory.decodeResource(getContext().getResources(),
                 R.drawable.handle);
 
         handleHeight = handle.getHeight();
 
         final DisplayMetrics metrics = new DisplayMetrics();
         final WindowManager wMgr = (WindowManager)
             getContext().getSystemService(Context.WINDOW_SERVICE);
         wMgr.getDefaultDisplay().getMetrics(metrics);
         mDensity = metrics.density;
 
         HANDLE_POS_DEFAULT = (int)(205 * mDensity);
         handlePos = HANDLE_POS_DEFAULT;
        HANDLE_POS_X = (int)(120 * mDensity);
         HANDLE_POS_MAX = HANDLE_POS_DEFAULT + (int)(155 * mDensity);
 
         touchBox = new Rect((int)(HANDLE_POS_X - (80 * mDensity)),
                             (int)(HANDLE_POS_DEFAULT - (120 * mDensity)),
                             (int)(HANDLE_POS_X + (80 * mDensity)),
                             (int)(HANDLE_POS_DEFAULT + handleHeight));
 
         gestureDetector = new GestureDetector(
                 new GestureDetector.SimpleOnGestureListener () {
                     public boolean onScroll(MotionEvent e1, MotionEvent e2,
                         float distanceX, float distanceY) {
                         if (!listeningToScroll)  return false;
                         if (distanceY < 0 &&
                             handlePos < HANDLE_POS_MAX) {
                             handlePos = Math.min(handlePos - (int)distanceY,
                                                     HANDLE_POS_MAX);
                         } else if (distanceY > 0 &&
                                    handlePos > HANDLE_POS_DEFAULT) {
                             handlePos = Math.max(handlePos - (int)distanceY,
                                                  HANDLE_POS_DEFAULT);
                         }
 
                         draw();
                         return false;
                     }
         });
 
         setOnTouchListener(new View.OnTouchListener() {
                 public boolean onTouch(View v, MotionEvent event) {
                     gestureDetector.onTouchEvent(event);
 
                     final float x = event.getX();
                     final float y = event.getY();
                     if (touchBox.contains((int)x, (int)y)) {
                         listeningToScroll = true;
                         stopCountDown();
                     }
                     if (event.getAction() == MotionEvent.ACTION_UP) {
                         if (tiretteListener != null && listeningToScroll) {
                             if (handlePos > HANDLE_POS_DEFAULT) {
                                 tiretted();
                             } else if (currentLamp == lamp_on) {
                                 unTiretted();
                             }
                         }
                         listeningToScroll = false;
                     }
                     return true;
                 }
         });
     }
 
     public void tiretted() {
         stopCountDown();
         tiretteListener.tiretted();
         startCountDown((int)getTiretteDuration());
     }
 
     public void unTiretted() {
         stopCountDown();
         if (tiretteListener != null) {
             tiretteListener.unTiretted();
         }
         switchOff();
     }
 
     public void startCountDown(long timeout) {
         final long stopAt = System.currentTimeMillis() + timeout;
         mCoundDownStarted = true;
 
         mCountDown = new CountDownTimer(timeout, SPT) {
             @Override
             public void onTick(long ms) {
                 tick(ms);
             }
 
             @Override
             public void onFinish() {
             }
         }.start();
     }
 
     public void stopCountDown() {
         mCoundDownStarted = false;
         if (mCountDown != null) {
             mCountDown.cancel();
         }
     }
 
 	public boolean isCountDownStarted(){
 		return mCoundDownStarted;
 	}
 
 	public void setCountDownStarted(boolean mCoundDownStarted) {
 		mCoundDownStarted = mCoundDownStarted;
 	}
 
     @Override
     public void surfaceChanged(SurfaceHolder holder, int format, int width,
             int height) {
     }
 
     @Override
     public void surfaceCreated(SurfaceHolder holder) {
         mSurfaceCreated = true;
         draw();
     }
 
     @Override
     public void surfaceDestroyed(SurfaceHolder holder) {
         mSurfaceCreated = false;
     }
 
     private MediaPlayer mpClick = null;
 
     private void playClick() {
         if (mpClick == null) {
             mpClick = MediaPlayer.create(getContext(), R.raw.click);
             mpClick.start();
         } else {
             mpClick.stop();
             try {
                 mpClick.prepare();
                 mpClick.start();
             } catch(java.io.IOException e) {
                 Log.w("TimedLightView", "Warning player did not work");
                 Log.w("TimedLightView", e);
             }
         }
     }
 }
