 package com.liongrid.gameengine;
 
 import com.liongrid.infectosaurus.GameActivity;
 import com.liongrid.infectosaurus.Infectosaurus;
 import com.liongrid.infectosaurus.Main;
 
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
 	RenderSystem renderSystem;
 	RenderingThread renderThread;
 
 	private Object updateLock;
 
 	private static final int MIN_REFRESH_TIME = 16; //ms, 16 gives 60fps
 	//ms, if the dt is lower than this, don't update this cycle
 	private static final int MIN_UPDATE_MS = 12; 
 	private static final float MAX_TIMESTEP = 0.1f; //seconds
 
 	private volatile boolean paused = true;
 
 
 
 	public GameThread() {
 		updateLock = new Object();
 		root = BaseObject.gamePointers.root;
 		setName("GameThread");
 	}
 
 	@Override
 	public void run() {
 		renderSystem = BaseObject.gamePointers.renderSystem;
 		renderThread = BaseObject.gamePointers.renderThread;
 		Log.d(Main.TAG, "Starting GameThread");
 		while(running){
 			//Make sure we don`t swap queues while renderer is rendering
 			renderThread.waitDrawingComplete();
 
 			waitWhilePaused();
 
 			//calculateDT();
 			currentTime = SystemClock.uptimeMillis();
 			dt = currentTime - lastTime;
 			if(lastTime == -1){
 				lastTime = SystemClock.uptimeMillis();
 				dt = 0; //Take no timestep if this is the first iteration
 			}		
 
 			long dtfinal = dt; 
 			if(dt > MIN_UPDATE_MS){
 				float dtsec = (currentTime - lastTime) * 0.001f;
 				//We never want to take too big timesteps!
 				if(dtsec > MAX_TIMESTEP){
 					dtsec = MAX_TIMESTEP;
 				}
 				lastTime = currentTime;
 
 				//Update all game logic
 				synchronized(updateLock){
 					root.update(dtsec, null);
 				}
 				//Sends the previously completed renderQueue 
 				//to the renderer, and gets a new empty one
 				renderSystem.swap(renderThread);
 
 				dtfinal = SystemClock.uptimeMillis() - currentTime;
 
 			}
 			//Take a small nap to get those touch events!
 			try {
 				Thread.sleep(Math.max(2,MIN_UPDATE_MS - dtfinal));
 			} catch (InterruptedException e) {
 				//Doesn`t matter
 			}
 		}
 	}
 
 	private void calculateDT() {
 
 	}
 
 	private synchronized void waitWhilePaused() {
 		while(paused){
 			try {
 				wait();
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	public void doTouchEvent(MotionEvent event) {
 	}
 
 	public void registerScreenTouch(MotionEvent event) {
 		synchronized(updateLock){
 			if(BaseObject.gamePointers.currentSaurus != null) return;
 			
 			Infectosaurus inf = new Infectosaurus();
 			BaseObject.gamePointers.currentSaurus = inf;
 			GameActivity.infectoPointers.gameObjectHandler.add(inf);
 
 
 			float y = (BaseObject.gamePointers.panel.getHeight() - event.getY()) / Camera.scale;
 			float x = event.getX() / Camera.scale;
 			BaseObject.gamePointers.currentSaurus.pos.set(x + Camera.pos.x, 
 					y + Camera.pos.y);
 		}
 	}
	
 
 	public void onPause() {
 		paused = true;
 	}
 
 	/**
 	 * Stops the thread!
 	 */
 	public void stopRunning(){
 		running = false;
 	}
 
 	public void onResume(){
 		paused = false;
 		synchronized(this){
 			notifyAll();
 		}
 	}
 }
