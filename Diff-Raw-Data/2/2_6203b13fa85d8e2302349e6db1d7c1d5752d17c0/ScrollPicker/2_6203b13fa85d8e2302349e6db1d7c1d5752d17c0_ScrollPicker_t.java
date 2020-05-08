 package com.hyperactivity.android_app.core;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.view.MotionEvent;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 import com.hyperactivity.android_app.Constants;
 import com.hyperactivity.android_app.R;
 
 public class ScrollPicker extends SurfaceView implements SurfaceHolder.Callback {
     private ScrollPickerThread thread;          //The thread that actually draws the animation
     private float xTouch;
     private float yTouch;
     private float xOffset;
     private float yOffset;
     private float scrollSpeed;
 
     public ScrollPicker(Context context, AttributeSet attrs) {
         super(context, attrs);
 
         // register our interest in hearing about changes to our surface
         SurfaceHolder holder = getHolder();
         holder.addCallback(this);
 
         // create thread only; it's started in surfaceCreated()
         thread = new ScrollPickerThread(holder, context);
 
         scrollSpeed = (float) getResources().getInteger(R.integer.scroll_speed);
     }
 
     /**
      * Standard window-focus override. Notice focus lost so we can pause on
      * focus lost. e.g. user switches to take a call.
      */
     @Override
     public void onWindowFocusChanged(boolean hasWindowFocus) {
         if (!hasWindowFocus) {
             thread.pause();
        } else {
            thread.unpause();
         }
     }
 
     @Override
     public boolean onTouchEvent(MotionEvent event) {
         if (event.getAction() == MotionEvent.ACTION_DOWN) {
             xTouch = event.getX();
             yTouch = event.getY();
             thread.onTouchDown(xTouch, yTouch);
         } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
             float deltaX = -(xTouch - event.getX());
             float deltaY = -(yTouch - event.getY());
 
             //offset should be cleared on scroll direction changes.
             if ((xOffset < 0 && deltaX > 0) || (xOffset > 0 && deltaX < 0)) {
                 xOffset = 0;
             }
             if ((yOffset < 0 && deltaY > 0) || (yOffset > 0 && deltaY < 0)) {
                 yOffset = 0;
             }
 
             xOffset += deltaX;
             yOffset += deltaY;
 
             thread.onTouchMove(deltaX * scrollSpeed, deltaY * scrollSpeed);
 
             xTouch = event.getX();
             yTouch = event.getY();
         } else if (event.getAction() == MotionEvent.ACTION_UP) {
             thread.onTouchUp(event.getX(), event.getY());
 
             xOffset = 0;
             yOffset = 0;
             xTouch = 0;
             yTouch = 0;
         }
         return true;
     }
 
     /*
      * Callback invoked when the Surface has been created and is ready to be
      * used.
      */
     public void surfaceCreated(SurfaceHolder holder) {
         // start the thread here so that we don't busy-wait in run()
         // waiting for the surface to be created
         thread.setRunning(true);
         thread.start();
     }
 
     /* Callback invoked when the surface dimensions change. */
     public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
         thread.setCanvasSize(width, height);
     }
 
     /*
      * Callback invoked when the Surface has been destroyed and must no longer
      * be touched. WARNING: after this method returns, the Surface/Canvas must
      * never be touched again!
      */
     public void surfaceDestroyed(SurfaceHolder holder) {
         // we have to tell thread to shut down & wait for it to finish, or else
         // it might touch the Surface after we return and explode
         boolean retry = true;
         thread.setRunning(false);
         while (retry) {
             try {
                 thread.join();
                 retry = false;
             } catch (InterruptedException e) {
                 Log.e(Constants.Log.TAG, e.getMessage());
             }
         }
     }
 
     public ScrollPickerThread getThread() {
         return thread;
     }
 
     public ScrollPickerItemManager getItemManager() {
         return thread.getItemManager();
     }
 
     public void reset() {
         getItemManager().reset();
 
         //TODO: show loading spinner in place instead.
     }
 
     public class ScrollPickerThread extends Thread {
         //State-tracking constants
         public static final int STATE_READY = 1;
         public static final int STATE_RUNNING = 2;
         public static final int STATE_PAUSE = 3;
         private int state;                      //The state of the renderer. READY, RUNNING or PAUSE
         private boolean run = false;            //Indicate whether the surface has been created & is ready to draw
         private SurfaceHolder surfaceHolder;    //Handle to the surface manager object we interact with
         private long lastTime;                  //Used to figure out elapsed time between frames
         private Context context;                //Handle to the application context, used to e.g. fetch Drawables.
         private float canvasWidth = 1;          //width of the drawable area, will be updated by function below.
         private float canvasHeight = 1;         //height of the drawable area, will be update by function below.
         private ScrollPickerItemManager itemManager;
 
         public ScrollPickerThread(SurfaceHolder surfaceHolder, Context context) {
             this.surfaceHolder = surfaceHolder;
             this.context = context;
             this.itemManager = new ScrollPickerItemManager(canvasWidth, canvasHeight, context.getResources().getInteger(R.integer.scroll_picker_categories_size)/100f);
         }
 
         /**
          * logic goes here.
          */
         private void doUpdate(float delta) {
             if (state == STATE_READY) {
                 //The thread should not start until setCanvasSize have been called.
                 if(canvasWidth != 1 && canvasHeight != 1) {
                     setState(STATE_RUNNING);
                 }
             } else if (state == STATE_RUNNING) {
                 itemManager.doUpdate(delta);
             }
         }
 
         /**
          * draw all the graphics
          */
         private void doDraw(Canvas canvas) {
             if (state == STATE_RUNNING) {
                 //TODO: do me
 
 
                  canvas.drawColor(context.getResources().getColor(R.color.background));
 
                 itemManager.doDraw(canvas);
             }
         }
 
         /**
          * Dump state to the provided Bundle. Typically called when the
          * Activity is being suspended.
          *
          * @return Bundle with this view's state
          */
         public Bundle saveState(Bundle map) {
             synchronized (surfaceHolder) {
                 if (map != null) {
                     //save values to map
                 }
             }
             return map;
         }
 
         /**
          * Restores state from the indicated Bundle. Typically called when
          * the Activity is being restored after having been previously
          * destroyed.
          *
          * @param savedState Bundle containing the state
          */
         public synchronized void restoreState(Bundle savedState) {
             synchronized (surfaceHolder) {
                 setState(STATE_PAUSE);
 
                 //restore values from savedState.
             }
         }
 
         /**
          * Resumes from a pause.
          */
         public void unpause() {
             // Move the real time clock up to now
             synchronized (surfaceHolder) {
                 lastTime = System.currentTimeMillis();
             }
 
             if (state == STATE_PAUSE) {
                 setState(STATE_RUNNING);
             }
         }
 
         public void onTouchDown(float x, float y) {
         }
 
         public void onTouchUp(float x, float y) {
         }
 
         public void onTouchMove(float dx, float dy) {
             itemManager.move(dx);
         }
 
         @Override
         public void run() {
             long now;
             float delta;
 
             while (run) {
                 Canvas c = null;
                 try {
                     c = surfaceHolder.lockCanvas(null);
                     synchronized (surfaceHolder) {
                         now = System.currentTimeMillis();
                         delta = (now - lastTime) / 1000.0f;
                         lastTime = now;
 
                         if (state == STATE_RUNNING || state == STATE_READY) {
                             doUpdate(delta);
                         }
 
                         doDraw(c);
                     }
                 } catch (Exception e) {
                     Log.e(Constants.Log.TAG, "exception", e);
                 } finally {
                     // do this in a finally so that if an exception is thrown
                     // during the above, we don't leave the Surface in an
                     // inconsistent state
                     if (c != null) {
                         surfaceHolder.unlockCanvasAndPost(c);
                     }
                 }
             }
         }
 
         /**
          * Pauses the physics doUpdate & animation.
          */
         public void pause() {
             synchronized (surfaceHolder) {
                 if (state == STATE_RUNNING) {
                     setState(STATE_PAUSE);
                 }
             }
         }
 
         public void setState(int mode) {
             this.state = mode;
         }
 
         /**
          * Used to signal the thread whether it should be running or not.
          * Passing true allows the thread to run; passing false will shut it
          * down if it's already running. Calling start() after this was most
          * recently called with false will result in an immediate shutdown.
          *
          * @param run true to run, false to shut down
          */
         public void setRunning(boolean run) {
             this.run = run;
         }
 
         /**
          * Callback invoked when the surface dimensions change.
          */
         public void setCanvasSize(float width, float height) {
             // synchronized to make sure these all change atomically
             synchronized (surfaceHolder) {
 
                 canvasWidth = width;
                 canvasHeight = height;
 
                 float canvasRatio = canvasHeight / canvasWidth;
 
                 Log.i(this.getClass().getName(), "Canvas width: " + canvasWidth + " height: " + canvasHeight + " ratio: " + canvasRatio);
 
                 if(itemManager != null) {
                     itemManager.onCanvasChanged(canvasWidth, canvasHeight);
                 }
             }
         }
 
         public boolean isRunning() {
             return state == STATE_RUNNING;
         }
 
         public ScrollPickerItemManager getItemManager() {
             return itemManager;
         }
     }
 }
 
