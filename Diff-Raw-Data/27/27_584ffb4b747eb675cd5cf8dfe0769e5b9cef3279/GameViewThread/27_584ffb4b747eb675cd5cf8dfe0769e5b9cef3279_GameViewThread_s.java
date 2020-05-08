 package com.android.games.l9.view;
 
 import android.content.Context;
 import android.graphics.Canvas;
 import android.os.Handler;
 import android.view.SurfaceHolder;
 import android.widget.Toast;
 
 public class GameViewThread extends Thread {
 	private SurfaceHolder surfaceHolder;
 	private Handler handler;
 	private Context context;
 	private boolean running = false;
 	private int mode;
 	
 	public GameViewThread(SurfaceHolder surfaceHolder, Context context, Handler handler) {
 		this.surfaceHolder = surfaceHolder;
 		this.context = context;
 		this.handler = handler;
 	}
 	
 	@Override
     public void run() {
 		Canvas canvas = null;
 		while(running) {
 			try {
				canvas = surfaceHolder.lockCanvas(null);
 				synchronized(surfaceHolder) {
 					testToast();					
 				}				
 			} finally {
				if (canvas != null) {
                     surfaceHolder.unlockCanvasAndPost(canvas);
                }
 			}
 		}		
 	}
 	
 	/**
 	 * Set game mode status.
 	 * @param mode
 	 */
 	public void setState(int mode) {		
 		synchronized (surfaceHolder) {
 			this.mode = mode;
 			switch (mode) {
 				case 1: testToast();
 						break;
 				default : running = false;
 			}
 		}
 	}
 	
 	/** Public methods **/
 	public void setRunning(boolean running) {
 		this.running = running;
 	}
 	
 	public void testToast() {
 		Toast toast = Toast.makeText(context, "test toast", Toast.LENGTH_SHORT);
 		toast.show();
 	}
 }
