 package com.alizarinarts.paintpaint;
 
 import android.app.Activity;
 
 import android.content.Context;
 
 import android.opengl.GLSurfaceView;
 
 
 import android.view.MotionEvent;
 
 /**
  * Custom GLSurfaceView to allow touch events.  Based on example from:
  * http://android-developers.blogspot.com/2009/04/introducing-glsurfaceview.html
  * 
  * @author <a href="mailto:david.e.shere@gmail.com">David Shere</a>
  */
 public class CanvasGLSurfaceView extends GLSurfaceView {
 
     CanvasRenderer mRenderer;
     Activity mActivity;
 
     public CanvasGLSurfaceView(Context context) {
         super(context);
         mActivity = (Activity) context;
         setEGLContextClientVersion(2);
         mRenderer = new CanvasRenderer(context);
         setRenderer(mRenderer);
 }
 
     public boolean onTouchEvent(final MotionEvent event) {
         boolean newEvent = false;
         switch (event.getAction()) {
             case MotionEvent.ACTION_DOWN:
             case MotionEvent.ACTION_POINTER_DOWN:
                 newEvent = true;
                 //lastTouchX = event.getX();
                 //lastTouchY = event.getY();
             //    return true;
             case MotionEvent.ACTION_MOVE:
                 final boolean newEventFinal = newEvent;
                final int[] pos = new int[2];
                getLocationOnScreen(pos);
                 final int i = event.getActionIndex();
                 // Add Runnable to queue, idea from https://github.com/MasDennis/Rajawali/issues/56
                //if ( Math.abs(lastTouchX - event.getX()) < 50 &&
                     //Math.abs(lastTouchY - event.getY()) < 50) {
                    queueEvent(new Runnable() {public void run() {
                        mRenderer.addCanvasDab((int)event.getX(i), (int)event.getY(i)-pos[1], event.getPressure(), newEventFinal);
                    }});
                 //   lastTouchX = event.getX();
                  //  lastTouchY = event.getY();
                //}
                 return true;
         }
 
         return false;
     }
 
     public CanvasRenderer getRenderer() {
         return mRenderer;
     }
 
 }
