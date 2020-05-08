 package com.group7.dragonwars.ui;
 
 import android.content.Context;
 import android.graphics.Canvas;
import android.graphics.SurfaceHolder;
 
 
 public class DrawingThread extends Thread {
     private boolean run;
     private Canvas canvas;
     private SurfaceHolder surfaceholder;
     private Context context;
     private GameView gview;
 
     public DrawingThread(final SurfaceHolder sholder,
                          final Context ctx, final GameView gv) {
         surfaceholder = sholder;
         context = ctx;
         run = false;
         gview = gv;
     }
 
     public void setRunning(final boolean newrun) {
         run = newrun;
     }
 
     @Override
     public void run() {
         super.run();
 
         while (run) {
             canvas = surfaceholder.lockCanvas();
 
             if (canvas != null) {
                 gview.doDraw(canvas);
                 surfaceholder.unlockCanvasAndPost(canvas);
             }
         }
     }
 
 }
