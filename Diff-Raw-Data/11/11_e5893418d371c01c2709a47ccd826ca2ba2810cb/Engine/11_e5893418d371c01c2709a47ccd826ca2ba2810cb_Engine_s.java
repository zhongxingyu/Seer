 package com.hifreshday.android.pge.engine;
 
 import android.graphics.Canvas;
 import android.util.Log;
 import android.view.MotionEvent;
 import android.view.SurfaceHolder;
 import com.hifreshday.android.pge.engine.handler.RunnableHandler;
 import com.hifreshday.android.pge.engine.options.EngineOptions;
 import com.hifreshday.android.pge.engine.options.EngineUtil;
 import com.hifreshday.android.pge.entity.scene.Scene;
 
 public class Engine {
 	
 	private static final String TAG = "Engine";
 	
 	private EngineOptions options;
 	private long lastTick = -1;
 	private boolean isRunning = false;
 	private EngineThread engineThread;
 
 	private Scene scene;
 	private SurfaceHolder surfaceHolder;
 	
 	private final RunnableHandler updateThreadRunnableHandler = new RunnableHandler();
 	
 
 	public Engine(final EngineOptions options){
 		this.options = options;
 		engineThread = new EngineThread();
 		engineThread.start();
 	}
 	
 	public void onTickUpdate() throws InterruptedException{
 		
 		if(isRunning){
 			final long pNanosecondsElapsed = EngineUtil.getNanosecondsElapsed(lastTick);
 			logicUpdate(pNanosecondsElapsed);
 		}else{
 			Thread.sleep(16);
 		}
 	}
 	
 	private void logicUpdate(long nanosecondsElapsed){
 		final float secondsElapsed = (float)nanosecondsElapsed / 1000000000;
 		this.lastTick += nanosecondsElapsed;
 		
 		updateUpdateHandler(secondsElapsed);	// handler update
 		onUpdateScene(secondsElapsed);			// view update
 		
 		draw(surfaceHolder, secondsElapsed);
 	}
 	
 	protected void onUpdateScene(float secondsElapsed) {
 		if(scene != null) {
 			scene.onUpdate(secondsElapsed);
 		}
 	}
 	
 	protected void updateUpdateHandler(float secondsElapsed) {
 		updateThreadRunnableHandler.onUpdate(secondsElapsed);
 	}
 
 	private void draw(SurfaceHolder surfaceHolder, float secondsElapsed){
 		Canvas c = null;
 		try {
 			c = surfaceHolder.lockCanvas();
 			synchronized (surfaceHolder) {
 				if (c != null) {
 					draw(c, secondsElapsed);
 				}
 			}
 		} finally {
 			if (c != null) {
 				surfaceHolder.unlockCanvasAndPost(c);
 			}
 		}
 	}
 	
 	private void draw(Canvas canvas, float pSecondsElapsed){
 		if(scene != null){
 			scene.onDraw(canvas);
 		}
 	}
 	
 	
 	public void onResume() {
 	}
 	
 	public void onPause() {
 		stop();
 	}
 	
 	public void onDestory() { 
 		interruptUpdateThread();
 	}
 	
 	public synchronized void start() {
 		Log.i(TAG, "engine start ... ");
 		if(!this.isRunning) {
 			this.lastTick = System.nanoTime();
 			this.isRunning = true;
 		}
 	}
 
 	public synchronized void stop() {
 		Log.i(TAG, "engine stop ... ");
 		if(this.isRunning) {
 			this.isRunning = false;
 		}
 	}
 	
 	private void interruptUpdateThread() {
 		this.engineThread.interrupt();
 	}
 	
 	private class EngineThread extends Thread {
 		
 		public EngineThread() {
 			super("EngineThread");
 		}
 		
 		@Override
 		public void run() {
 			Log.i(TAG, "engine thread start ... ");
 			while(true){
 				try {
 					Engine.this.onTickUpdate();
 				} catch (final InterruptedException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 	}
 	
 	public boolean onTouchEvent(MotionEvent event) {
 		return scene.onTouchEvent(event);
 	}
 
 	public Scene getScene() {
 		return scene;
 	}
 
 	public EngineOptions getOptions() {
 		return options;
 	}
 
 	public void setOptions(EngineOptions options) {
 		this.options = options;
 	}
 
 	public void onLoadInit(SurfaceHolder surfaceHolder, Scene scene) {
 		Log.i("MOLO_DEBUG", "surfaceholder create");
 		this.scene = scene;
 		this.surfaceHolder = surfaceHolder;
 	}
 	
 	public void runOnUpdateThread(final Runnable runnable) {
 		updateThreadRunnableHandler.postRunnable(runnable);
 	}
 }
