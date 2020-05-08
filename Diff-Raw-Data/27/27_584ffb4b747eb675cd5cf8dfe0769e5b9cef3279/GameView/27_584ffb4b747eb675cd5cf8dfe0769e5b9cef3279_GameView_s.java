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
 		gameThread = new GameViewThread(holder, context, new GameMessageHandler(context));
 	}
 	
 	public GameViewThread getGameThread() {
 		return this.gameThread;
 	}
 
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
 		// TODO Auto-generated method stub
 		
 	}
 
	public void surfaceCreated(SurfaceHolder arg0) {
		Toast toast = Toast.makeText(context, "surface created", Toast.LENGTH_SHORT);
		toast.show();
 		gameThread.setRunning(true);
		/*gameThread.start();*/				
 	}
 
 	public void surfaceDestroyed(SurfaceHolder arg0) {
 		// TODO Auto-generated method stub
 	}
 	
 }
