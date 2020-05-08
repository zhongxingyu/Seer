 package com.RoboMobo;
 
 import android.content.res.Resources;
 import android.graphics.*;
 import android.view.SurfaceHolder;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Роман
  * Date: 30.07.13
  * Time: 13:14
  * To change this template use File | Settings | File Templates.
  */
 public class ThreadUpdate extends Thread
 {
     public long prevTime;
     public boolean isRunning;
     public SurfaceHolder surfaceHolder;
     public float rotation = 0;
 
     public ThreadUpdate(SurfaceHolder surfaceHolder)
     {
         this.surfaceHolder = surfaceHolder;
 
         prevTime = System.currentTimeMillis();
         isRunning = true;
     }
 
     @Override
     public void run()
     {
         while (isRunning)
         {
             long now = System.currentTimeMillis();
             long elapsedTime = now - prevTime;
             if (elapsedTime > 30)
             {
                 RMR.Update();
 
                 try
                 {
                     RMR.c = surfaceHolder.lockCanvas(null);
                     synchronized (surfaceHolder)
                     {
                         if(RMR.c != null)
                         {
                             RMR.Draw();
                         }
                     }
                 }
                 finally
                 {
                     if (RMR.c != null)
                     {
                         surfaceHolder.unlockCanvasAndPost(RMR.c);
                     }
                 }
                 prevTime = now;
             }
         }
     }
 }
