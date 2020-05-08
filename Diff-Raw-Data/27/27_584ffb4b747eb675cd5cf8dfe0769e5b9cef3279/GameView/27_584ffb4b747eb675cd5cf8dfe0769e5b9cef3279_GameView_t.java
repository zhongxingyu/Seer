 package com.android.games.l9.view;
 
 import android.content.Context;
 import android.util.AttributeSet;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 import android.widget.Toast;
 
 public class GameView extends SurfaceView implements SurfaceHolder.Callback {
 	private GameViewThread gameThread;
 	private Context context;
 	
 	public GameView(Context context, AttributeSet attrs) {
 		super(context, attrs);
		
 		this.context = context;
 		SurfaceHolder holder = getHolder();
 		holder.addCallback(this);
		holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
 		gameThread = new GameViewThread(holder, context, new GameMessageHandler(context));
 	}
 	
 	public GameViewThread getGameThread() {
 		return this.gameThread;
 	}
 
	public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) {
 		// TODO Auto-generated method stub
 		
 	}
 
	public void surfaceCreated(SurfaceHolder surfaceHolder) {
 		gameThread.setRunning(true);
		gameThread.start();				
 	}
 
 	public void surfaceDestroyed(SurfaceHolder arg0) {
 		// TODO Auto-generated method stub
 	}
 	
 }
