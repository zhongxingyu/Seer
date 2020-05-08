 package com.infectosaurus;
 
 import android.os.SystemClock;
 import android.util.Log;
 import android.view.MotionEvent;
 
 /**
  * @author Lionleaf
  * This class is the main loop responsible for updating
  * game logic and generating a queue of objects for the 
  * rendering thread to render
  *
  */
 public class GameThread extends Thread { 
 	private ObjectHandler root;
  
 	private long dt;
 	private long currentTime;
 	private long lastTime = -1;
 	
 	private boolean running = true;
 	private final static String TAG = "GameThread";
 	RenderSystem renderSystem;
 	RenderingThread renderThread;
 	
 	private static final int MIN_REFRESH_TIME = 16; //ms, 16 gives 60fps
 	//ms, if the dt is lower than this, don't update this cycle
 	private static final int MIN_UPDATE_MS = 12; 
 	
 	private static final float MAX_TIMESTEP = 0.1f; //seconds
 	
     public GameThread() {
     	root = BaseObject.gamePointers.root;
     	setName("GameThread");
     }
  
     @Override
     public void run() {
     	renderSystem = BaseObject.gamePointers.renderSystem;
     	renderThread = BaseObject.gamePointers.renderThread;
     	Log.d(TAG, "Starting GameThread");
     	while(running){
     		//Make sure we don`t swap queues while renderer is rendering
     		renderThread.waitDrawingComplete();
     		
    		//Sends the previously completed renderQueue 
    		//to the renderer, and gets a new empty one
    		renderSystem.swap(renderThread);
     		
     		currentTime = SystemClock.uptimeMillis();
     		
     		dt = currentTime - lastTime;
     		   		
     		if(lastTime == -1){
     			lastTime = SystemClock.uptimeMillis();
     			dt = 0; //Take no timestep if this is the first iteration
     		}
     		  		
     		long dtfinal = dt; 
     		
    		//if(dt > MIN_UPDATE_MS){
     			float dtsec = (currentTime - lastTime) * 0.001f;
     			//We never want to take too big timesteps!
     			if(dtsec > MAX_TIMESTEP){
     				dtsec = MAX_TIMESTEP;
     			}
    			lastTime = currentTime;
     			
     			//Update all game logic
     			root.update(dtsec, null);
    			
     			dtfinal = SystemClock.uptimeMillis() - currentTime;
     			
    		//}
     		
    		/*if(dtfinal < MIN_REFRESH_TIME){
     			try {
					Thread.sleep(MIN_UPDATE_MS - dtfinal);
 				} catch (InterruptedException e) {
 					//Doesn`t matter
 				}
    		}*/
     	}
     }
 
 	public void doTouchEvent(MotionEvent event) {
 	}
 }
