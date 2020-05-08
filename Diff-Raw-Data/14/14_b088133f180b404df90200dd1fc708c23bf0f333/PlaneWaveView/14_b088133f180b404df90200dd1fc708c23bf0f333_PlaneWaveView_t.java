 package com.thesky;
 
 import android.content.Context;
 import android.content.res.Resources;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.os.Handler;
 import android.os.Message;
 import android.util.AttributeSet;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 import android.widget.TextView;
 
 public class PlaneWaveView extends SurfaceView implements SurfaceHolder.Callback {
 
     private PlaneWaveThread planeWaveThread;
     private PlaneWaveThread planeWaveThread2;
    private volatile Context mContext;
 
 
    private volatile TextView mStatusText;
 
     public void surfaceCreated(SurfaceHolder surfaceHolder) {
         planeWaveThread.init_params(getWidth(), getHeight());
         planeWaveThread.setRunning(true);
         planeWaveThread.start();
 
         planeWaveThread2.init_params(getWidth(), getHeight());
         planeWaveThread2.setRunning(true);
         planeWaveThread2.start();
     }
 
     public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
         planeWaveThread.setSurfaceSize(width, height);
        planeWaveThread2.setSurfaceSize(width, height);
     }
 
     public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
         boolean retry = true;
         planeWaveThread.setRunning(false);
         planeWaveThread2.setRunning(false);
 
         while (retry) {
             try {
                 planeWaveThread.join();
                 retry = false;
             } catch (InterruptedException e) {
             }
         }
         while (retry) {
             try {
                 planeWaveThread2.join();
                 retry = false;
             } catch (InterruptedException e) {
             }
         }
 
 
     }
 
     public PlaneWaveView(Context context, AttributeSet attrs) {
         super(context, attrs);
 
         SurfaceHolder holder = getHolder();
         holder.addCallback(this);
 
         // create thread only; it's started in surfaceCreated()
         planeWaveThread = new PlaneWaveThread(holder, context, new Handler() {
             @Override
             public void handleMessage(Message m) {
             }
         });
 
         planeWaveThread2 = new PlaneWaveThread(holder, context, new Handler() {
             @Override
             public void handleMessage(Message m) {
             }
         });
 
 
         setFocusable(true); // make sure we get key events
     }
 
 
     class PlaneWaveThread extends Thread {
 
         public static final int STATE_READY = 1;
         public static final int STATE_RUNNING = 2;
 
         private SurfaceHolder mSurfaceHolder;
         private Bitmap image_background;
         private Bitmap image_overlay;
         private Handler mHandler;
 
         private int height = 1;
         private int width = 1;
 
         private boolean mRun = false;
 
         private Wave wave;
 
         public void init_params(int width, int height) {
             this.width = width;
             this.height = height;
             wave = new Wave(height, width);
             Bitmap source_image = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.sky);
             image_background = Bitmap.createScaledBitmap(source_image, width, height, true);
             image_overlay = Bitmap.createBitmap(image_background);
         }
 
         public PlaneWaveThread(SurfaceHolder surfaceHolder, Context context, Handler handler) {
             mSurfaceHolder = surfaceHolder;
             mHandler = handler;
             mContext = context;
             Resources res = context.getResources();
         }
 
         public void setSurfaceSize(int width, int height) {
             synchronized (mSurfaceHolder) {
                 this.width = width;
                 this.height = height;
             }
         }
 
         @Override
         public void run() {
             while (mRun) {
                 Canvas c = null;
                 try {
                     c = mSurfaceHolder.lockCanvas();
                     synchronized (mSurfaceHolder) {
                         doDraw(c);
                         try {
                             sleep(10);
                             wave.randomize_params();
                             wave.propagate();
                             image_overlay = Bitmap.createBitmap(image_background);
                             wave.stretch(image_background, image_overlay);
                         } catch (Exception e) {
                         }
                     }
                 } finally {
                     if (c != null) {
                         mSurfaceHolder.unlockCanvasAndPost(c);
                     }
                 }
             }
         }
 
         private void doDraw(Canvas c) {
             c.drawBitmap(image_overlay, 0, 0, null);
         }
 
         /* public void doStart() {
             synchronized (mSurfaceHolder) {
 
                 while (true) {
                     try {
                         sleep(10);
                         wave.randomize_params();
                         wave.propagate();
                         image_overlay = Bitmap.createBitmap(image_background);
                         wave.stretch(image_background, image_overlay);
                     } catch (InterruptedException e) {
                     }
 
                 }
 
 
             }
         }*/
 
         /**
          * Pauses the physics update & animation.
          */
         public void pause() {
             synchronized (mSurfaceHolder) {
                 // if (mMode == STATE_RUNNING) setState(STATE_PAUSE);
             }
         }
 
         public void setRunning(boolean b) {
             mRun = b;
         }
     }
 
 
     public PlaneWaveThread getPlaneWaveThread() {
         return planeWaveThread;
     }
 
     public void setTextView(TextView textView) {
         mStatusText = textView;
     }
 
 }
