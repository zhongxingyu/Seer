 /*
  * Copyright (C) 2009 The J2AB Project
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package javax.microedition.lcdui;
 
 import javax.microedition.midlet.MIDlet;
 
 
 import com.orange.memoplayer.Widget;
 import com.orange.memoplayer.WidgetUpdate;
 
 import android.app.Activity;
 import android.app.Service;
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.MotionEvent;
 import android.view.SurfaceView;
 import android.view.View;
 
 public abstract class Canvas extends Displayable {
     public static final String TAG = "MyCanvas";
 
     public static final int FIRE = KeyEvent.KEYCODE_DPAD_CENTER;
     public static final int GAME_A = KeyEvent.KEYCODE_MENU;
     public static final int GAME_B = KeyEvent.KEYCODE_BACK;
     public static final int GAME_C = KeyEvent.KEYCODE_CAMERA;
     public static final int GAME_D = KeyEvent.KEYCODE_DEL;
 
     public static final int LEFT = KeyEvent.KEYCODE_DPAD_LEFT;
     public static final int RIGHT = KeyEvent.KEYCODE_DPAD_RIGHT;
     public static final int UP = KeyEvent.KEYCODE_DPAD_UP;
     public static final int DOWN = KeyEvent.KEYCODE_DPAD_DOWN;
 
     public static final int KEY_NUM0 = KeyEvent.KEYCODE_0;
     public static final int KEY_NUM1 = KeyEvent.KEYCODE_1;
     public static final int KEY_NUM2 = KeyEvent.KEYCODE_2;
     public static final int KEY_NUM3 = KeyEvent.KEYCODE_3;
     public static final int KEY_NUM4 = KeyEvent.KEYCODE_4;
     public static final int KEY_NUM5 = KeyEvent.KEYCODE_5;
     public static final int KEY_NUM6 = KeyEvent.KEYCODE_6;
     public static final int KEY_NUM7 = KeyEvent.KEYCODE_7;
     public static final int KEY_NUM8 = KeyEvent.KEYCODE_8;
     public static final int KEY_NUM9 = KeyEvent.KEYCODE_9;
     public static final int KEY_POUND = KeyEvent.KEYCODE_POUND;
     public static final int KEY_STAR = KeyEvent.KEYCODE_STAR;
 
     private interface CanvasBackend {
         boolean isShown ();
         void setSufaceView (SurfaceView sf);
         void removeSurfaveView ();
         void repaint ();
         void repaint (int x, int y, int w, int h);
         void serviceRepaints ();
         int getHeight();
         int getWidth();
     }
 
     
     private CanvasBackend canvasBackend;
     
     protected Canvas() {}
 
     protected void hideNotify () {}
 
     protected void showNotify () {}
 
     protected void sizeChanged (int w, int h) {}
 
     protected void keyPressed (int keyCode) {}
 
     protected void keyReleased (int keyCode) {}
     
     protected void keyRepeated(int keyCode) {}
 
     protected abstract void paint (javax.microedition.lcdui.Graphics g);
 
     protected void pointerPressed (int x, int y) {}
 
     protected void pointerReleased (int x, int y) {}
 
     protected void pointerDragged (int x, int y) {}
 
     public void setFullScreenMode (boolean fullScreen) {
         Log.i (TAG, "setFullScreenMode: " + fullScreen);
         /*
          * if (fullScreen) {
          * MIDlet.DEFAULT_MIDLET.getActivity().requestWindowFeature
          * (Window.FEATURE_NO_TITLE);
          * MIDlet.DEFAULT_MIDLET.getActivity().getWindow().setFlags(
          * WindowManager.LayoutParams.FLAG_FULLSCREEN,
          * WindowManager.LayoutParams.FLAG_FULLSCREEN); } else {
          * MIDlet.DEFAULT_MIDLET.getActivity().getWindow().setFlags(
          * WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN,
          * WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN); }
          */
     }
 
     public void show (Display d) {
         Log.i("Canvas","show: register canvas.");
         Context c = d.getMIDlet ().getContext ();
         if (c instanceof Activity) {
             ViewBackend cv = new ViewBackend (d.getMIDlet ());
             view = cv;
             canvasBackend = cv;
         } else if (c instanceof Service) {
             canvasBackend = new WidgetBackend (d.getMIDlet (), this);
             view = null;
         }
         super.show (d);
     }
     
     public void hide (Display d) {
         canvasBackend = null;
         super.hide (d);
     }
     
     // Called only by AndroidVideoControl
     public void setSurfaceView (SurfaceView surfaceView) {
         if (canvasBackend != null) {
             canvasBackend.setSufaceView (surfaceView);
         }
     }
     
     // Called only by AndroidVideoControl
     public void removeSurfaceView () {
         if (canvasBackend != null) {
             canvasBackend.removeSurfaveView ();
         }
     }
     
     public int getGameAction (int keyCode) {
         return keyCode;
     }
 
     public int getKeyCode (int gameAction) {
         return gameAction;
     }
 
     public int getWidth () {
         if (canvasBackend != null) {
             return canvasBackend.getWidth();
         }
         return 0;
     }
 
     public int getHeight () {
         if (canvasBackend != null) {
             return canvasBackend.getHeight();
         }
         return 0;
     }
 
     public void repaint (int x, int y, int w, int h) {
         // Out of bound values cause Android to ignore
         // postInvalidate which locks serviceRepaints !
         int W = getWidth (), H = getHeight ();
         int x2 = x + w;
         int y2 = y + h;
         if (x < 0) x = 0;
         if (y < 0) y = 0;
         if (x2 > W) x2 = W;
         if (y2 > H) y2 = H;
         if (canvasBackend != null && x2 > x && y2 > y) {
             canvasBackend.repaint (x, y, x2, y2);
         }
     }
 
     public void repaint () {
         if (canvasBackend != null) {
             canvasBackend.repaint ();
         }
     }
 
     public void serviceRepaints () {
         if (canvasBackend != null) {
             canvasBackend.serviceRepaints ();
         }
     }
 
     protected boolean isShown () {
         if (canvasBackend != null) {
             return canvasBackend.isShown ();
         }
         return false;
     }
 
     public boolean hasPointerEvents () {
         return true;
     }
     
     public void fakeKeyPressed (int keyCode) {
         keyPressed (keyCode);
     }
     
     public void fakeKeyReleased (int keyCode) {
         keyReleased (keyCode);
     }
     
     
     
     /*
      * Android canvas implementation
      */
 
     //@SuppressWarnings("deprecation")
     private class ViewBackend extends View implements CanvasBackend {
         
         private Bitmap buffer;
         private javax.microedition.lcdui.Graphics graphics;
         private SurfaceView mSurfaceView;
         private Object serviceRepaintLock = new Object ();
         private boolean needsRepaint = true;
         private boolean sizeInitialized = false;
         
         public ViewBackend (MIDlet m) {
             super (m.getContext ());
             setFocusable (true);
             if (mSurfaceView != null) {
                 //addView (mSurfaceView);
             }
         }
         
         protected void onWindowVisibilityChanged (int visibility) {
             Log.i (TAG, "onWindowVisibilityChanged: " + visibility);
             switch (visibility) {
             case GONE:
             case INVISIBLE:
                 Canvas.this.hideNotify ();
                 break;
             case VISIBLE:
                 // Only call shownNotify once the size of the view is known !
                 if (sizeInitialized) {
                     Canvas.this.showNotify ();
                 }
                 requestFocus ();
             }
         }
 
         protected void onDraw (android.graphics.Canvas canvas) {
             //MCP: Need to keep a buffer, just because Android purges the Canvas buffer "sometimes"
             //     on key press or other random events !
             if (buffer == null) {
                 buffer = Bitmap.createBitmap (canvas.getWidth (), canvas.getHeight (), Bitmap.Config.ARGB_8888);
                 graphics = new javax.microedition.lcdui.Graphics (buffer);
                 needsRepaint = true;
                // FIX: after displaying native text box, display thread could be stopped
                // call to showNotify will start rendering thread to avoid "black screen" issue
                Canvas.this.showNotify();
             }
             synchronized (serviceRepaintLock) {
                 if (needsRepaint) {
                     Canvas.this.paint (graphics);
                     needsRepaint = false;
                     // Wakeup serviceRepaint on end of paint
                     serviceRepaintLock.notify ();
                 }
             }
             canvas.drawBitmap (buffer, 0, 0, null);
         }
 
         protected void onSizeChanged (int w, int h, int oldw, int oldh) {
             Log.i (TAG, "onSizeChanged: " + w + "x" + h);
             buffer = null;
             if (sizeInitialized) {
                 Canvas.this.sizeChanged (w, h);
             } else {
                 // On first view display, call showNotify, now that we know the view size !
                 sizeInitialized = true;
                 Canvas.this.showNotify ();
             }
         }
         
         public boolean onKeyUp (int keyCode, KeyEvent event) {
             Log.i (TAG, "onKeyUp: "+keyCode);
             Canvas.this.keyReleased (keyCode);
             return true;
         }
         
         public boolean onKeyDown (int keyCode, KeyEvent event) {
             Log.i (TAG, "onKeyDown: " +keyCode);
             if (event.getRepeatCount () == 0) {
                 Canvas.this.keyPressed (keyCode);
             } else {
                 Canvas.this.keyRepeated (keyCode);
             }
             return true;
         }
 
         public boolean onTouchEvent (MotionEvent event) {
             int x = Math.round (event.getX ());
             int y = Math.round (event.getY ());
             switch (event.getAction ()) {
             case MotionEvent.ACTION_DOWN:
                 Canvas.this.pointerPressed (x, y);
                 break;
             case MotionEvent.ACTION_UP:
                 Canvas.this.pointerReleased (x, y);
                 break;
             case MotionEvent.ACTION_MOVE:
                 Canvas.this.pointerDragged (x, y);
                 break;
             }
             return true;
         }
 
         public void removeSurfaveView () {
             if (mSurfaceView != null) {
                 //removeView (mSurfaceView);
                 mSurfaceView = null;
             }
             
         }
         
         public void setSufaceView (SurfaceView sf) {
             mSurfaceView = sf;
             //addView (sf);            
         }
 
         public void repaint () {
             needsRepaint = true;
             postInvalidate ();
         }
 
         public void repaint (int x, int y, int x2, int y2) {
             needsRepaint = true;
             postInvalidate (x, y, x2, y2);
         }
 
         public void serviceRepaints () {
             synchronized (serviceRepaintLock) {
                 if (needsRepaint) {
                     try {
                         serviceRepaintLock.wait ();
                     } catch (InterruptedException e) {}
                 }
             }
         }
     }
     
     private class WidgetBackend implements CanvasBackend {
         private Bitmap mBuffer;
         private javax.microedition.lcdui.Graphics mGraphics;
         private int mWidth, mHeight;
         private boolean mRepaint;
         private MIDlet mMidlet;
         
         public WidgetBackend (MIDlet m, Canvas c) {
             float density = m.getContext().getResources().getDisplayMetrics().density;
             mWidth = (int) ((Widget.width * density));
             mHeight = (int) ((Widget.height * density));
             mMidlet = m;
             Log.i(TAG, "WidgetBackend: Starting... ("+mWidth+"x"+mHeight+"/"+density+")");
             mMidlet.post(new Runnable() {
                 public void run() {
                     Canvas.this.showNotify();
                 }
             });
         }
         
         private void render () {
             //Log.i(TAG, "WidgetBackend: render ");
             if (mBuffer == null) {
                 mBuffer = Bitmap.createBitmap (mWidth, mHeight, Bitmap.Config.ARGB_8888);
                 mGraphics = new javax.microedition.lcdui.Graphics (mBuffer);
                 mMidlet.invokeAndWait(new Runnable() {
                     public void run() {
                         ((WidgetUpdate)mMidlet.getContext()).setImageBuffer(mBuffer);
                     }
                 });
                 mRepaint = true;
             }
             if (mRepaint && mMidlet != null) {
                 Canvas.this.paint (mGraphics);
             }
         }
         
         public boolean isShown () {
             return true;
         }
 
         public void removeSurfaveView () {
         }
 
         public void setSufaceView (SurfaceView sf) {
         }
 
         public void repaint () {
             //Log.i(TAG, "WidgetBackend: Repaint");
             mRepaint = true;
         }
 
         public void repaint (int x, int y, int w, int h) {
             //Log.i(TAG, "WidgetBackend: Repaint");
             mRepaint = true;
         }
 
         public void serviceRepaints () {
             //Log.i(TAG, "WidgetBackend: ServiceRepaints");
             if (mRepaint) {
                 render();
                 mRepaint = false;
             }
         }
 
         public int getHeight() {
         	return mHeight;
         }
         
         public int getWidth() {
         	return mWidth;
         }
     }
 }
