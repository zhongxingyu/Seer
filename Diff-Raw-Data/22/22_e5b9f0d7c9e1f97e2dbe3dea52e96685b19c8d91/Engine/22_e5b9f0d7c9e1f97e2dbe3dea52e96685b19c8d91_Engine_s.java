 package com.chromabits.ugaacm.WarpDrive.control;
 
 import android.os.SystemClock;
 import android.util.Log;
 
import com.chromabits.ugaacm.WarpDrive.render.Color;
 import com.chromabits.ugaacm.WarpDrive.render.DrawQueue;
 import com.chromabits.ugaacm.WarpDrive.render.DrawQueueManager;
 import com.chromabits.ugaacm.WarpDrive.render.Gl2SurfaceView;
 import com.chromabits.ugaacm.WarpDrive.render.GlRenderer;
import com.chromabits.ugaacm.WarpDrive.render.Vertex;
import com.chromabits.ugaacm.WarpDrive.render.primitives.Rectangle;
import com.chromabits.ugaacm.Warped.LoadingScreen;
import com.chromabits.ugaacm.Warped.MainMenuScreen;
 
 /**
  * Main Engine Thread
  *
  * - Contains main game loop
  * - Directs how and when functions are processed in the game
  *
  * Created by Eduardo Trujillo <ed@chromabits.com> on 10/29/13.
  */
 public class Engine implements Runnable {
 
     private Game game;
 
     private GlRenderer renderer;
     private Gl2SurfaceView view;
     private DrawQueueManager queues;
 
     private boolean finishing;
     private boolean isFirstRun;
     private boolean isPaused;
     private Object pauseLock;
 
     // Timing
     private long lastTime;
 
     public Engine(Game game, GlRenderer renderer, Gl2SurfaceView view){
         this.game = game;
        game.setScreen(new MainMenuScreen(game));
         this.renderer = renderer;
         this.view = view;
 
         queues = new DrawQueueManager(renderer);
 
         finishing = false;
         isFirstRun = true;
         isPaused = false;
         pauseLock = new Object();
 
         lastTime = SystemClock.uptimeMillis();
 
     }
 
     @Override
     public void run() {
         lastTime = SystemClock.uptimeMillis();
         while(!finishing){
             // First run stuff
             if(isFirstRun){
                 firstRun();
             }else{
                 renderer.waitForDraw();
             }
 
             // Calculate time delta
             final long time = SystemClock.uptimeMillis();
             final long timeDelta = time - lastTime;
             long finalDelta = timeDelta;
 
             if(timeDelta > 12){
 
                 float secondsDelta = (time - lastTime) * 0.001f;
                 if (secondsDelta > 0.1f) {
                     // TODO: Use this variable in the game logic for animations and time based stuff
                     secondsDelta = 0.1f;
                 }
                 lastTime = time;
 
                 DrawQueue curQueue = queues.getWorkingQueue();
 
                 // <<<<< BEGIN GAME CODE >>>>>
 
 
                 game.getCurrentScreen().updateLogic();
                 game.getCurrentScreen().addElements(curQueue);
 
                 // <<<<< END GAME CODE >>>>>
 
                 // Swap draw queues
                 queues.swap();
                 view.requestRender();
 
                 // Calculate final delta
                 final long endTime = SystemClock.uptimeMillis();
                 finalDelta = endTime - time;
             }
 
             // Sleep if logic finished early
             if(finalDelta < 16){
                 try{
                     Thread.sleep(16 - finalDelta);
                 }
                 catch(InterruptedException ex){
                     // Not a big problem
                     ex.printStackTrace();
                 }
             }
 
             // Detect if the engine is pausing
             synchronized (pauseLock){
                 if(isPaused){
                     // While the engine paused, wait until a signal
                     while(isPaused){
                         try {
                             Log.i("WarpDrive","Engine waiting to resume");
                             pauseLock.wait();
                         } catch (InterruptedException e) {
                             // Not a big problem if it is interrupted
                             Log.w("WarpDrive","Engine interrupted");
                             e.printStackTrace();
                         }
                     }
                     Log.i("WarpDrive","Engine resuming");
                 }
             }
         }
     }
 
     private void firstRun(){
         // <<<<< TEST CODE >>>>>
         // PUT ANYTHING YOU NEED TO TEST HERE
 
 
 
         //drawQueue.addObject(r1);
         //drawQueue.addObject(r2);
         //drawQueue.addObject(r3);
         //drawQueue.addObject(r4);
 
         // <<<<< END TEST CODE >>>>>
 
         isFirstRun = false;
     }
 
     public void onResume(){
         // Resume engine
         synchronized (pauseLock){
             isPaused = false;
             pauseLock.notifyAll();
         }
 
         // Resume gl view
         view.onResume();
     }
 
     public void onPause(){
         synchronized (pauseLock){
             isPaused = true;
         }
 
         // Pause gl view
         renderer.onPause();
         view.onPause();
     }
 
     public void onStopping(){
         synchronized (pauseLock){
             finishing = true;
             isPaused = false;
             pauseLock.notifyAll();
         }
     }
 }
