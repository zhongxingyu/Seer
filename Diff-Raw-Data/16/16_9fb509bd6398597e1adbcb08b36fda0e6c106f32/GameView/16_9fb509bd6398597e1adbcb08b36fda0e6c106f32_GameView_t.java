 package org.ggj2013;
 
 import android.content.Context;
 import android.graphics.Canvas;
import android.util.Log;
 import android.view.MotionEvent;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 
 public class GameView extends SurfaceView implements SurfaceHolder.Callback {
 
	private MainThread thread;
 
 	public GameView(Context context) {
 		super(context);
 		getHolder().addCallback(this);
 
 		// create the game loop thread
 		thread = new MainThread(getHolder(), this);
 
 		setFocusable(true);
 	}
 
 	@Override
 	public void surfaceChanged(SurfaceHolder holder, int format, int width,
 			int height) {
 	}
 
 	@Override
 	public void surfaceCreated(SurfaceHolder holder) {
		if (thread.getState() == Thread.State.TERMINATED) {
			thread = new MainThread(getHolder(), this);
		}

 		thread.setRunning(true);
 		thread.start();
 	}
 
 	@Override
 	public void surfaceDestroyed(SurfaceHolder holder) {
 		boolean retry = true;

 		while (retry) {
 			try {
				thread.setRunning(false);
 				thread.join();
 				retry = false;
 			} catch (InterruptedException e) {
				Log.e("GameView", "surface destruction failed", e);
 			}
 		}
 	}
 
 	@Override
 	public boolean onTouchEvent(MotionEvent event) {
 		return super.onTouchEvent(event);
 	}
 
 	@Override
 	protected void onDraw(Canvas canvas) {
 	}
 }
