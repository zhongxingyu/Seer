 package com.ajgames.endless_runner.controller;
 
 import org.jbox2d.dynamics.World;
 
 import android.app.Activity;
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.Paint;
 import android.util.Log;
 import android.view.MotionEvent;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 
 import com.ajgames.endless_runner.MainThread;
 import com.ajgames.endless_runner.callbacks.MainContactListener;
 import com.ajgames.endless_runner.model.Physics;
 import com.ajgames.endless_runner.model.Runner;
 import com.ajgames.endless_runner.view.IRenderer;
 import com.ajgames.endless_runner.view.RunningGameRenderer;
 
 public class GameEngine extends SurfaceView implements
 		SurfaceHolder.Callback
 {
 	private static final String TAG = GameEngine.class.getSimpleName();
 	
 	private String avgFps;
 	
 	private World world;
 	private Runner runner;
 	
 	private IRenderer renderer;
 
 	private PlatformController platformController;
 
 	private MainThread mainThread;
 	
 
 	public GameEngine( Context context )
 	{
 		super( context );
 		// adding the callback (this) to the surface holder to intercept events
 		this.getHolder().addCallback( this );
 
 		// make the GamePanel focusable so it can handle events
 		this.setFocusable( true );
 
 		this.world = new World( Physics.GRAVITY_VEC, Physics.DO_SLEEP );
 		this.world.setContactListener( new MainContactListener() );
 		
 		this.runner = new Runner( 30.0f, 0.0f, this.world );
 		
 		this.platformController = new PlatformController( this, this.runner, this.world );
 		
 		this.renderer = new RunningGameRenderer( this, this.platformController.platforms, this.runner );
 
 		this.mainThread = new MainThread( this.getHolder(), this );
 	}
 
 	public void update() 
 	{
 		this.world.step( 1.0f / 60.0f, 6, 2 );
 		this.platformController.update();
 		this.runner.update();
 		
 		if( this.runner.getY() > getHeight() )
 		{
 			//end game
			newGame();
 		}
 	} 
 
 	public void render( Canvas canvas )
 	{
 		this.renderer.render( canvas );
 		this.displayFps( canvas, avgFps );
 	}
 	
 	@Override
 	public void surfaceChanged( SurfaceHolder holder, int format, int width,
 			int height )
 	{
 
 	}
 
 	@Override
 	public void surfaceCreated( SurfaceHolder arg0 )
 	{
         if(!mainThread.isAlive()){
         	this.mainThread.setRunning( true );
         	this.mainThread.start();
         }
 	}
 
 	@Override
 	public void surfaceDestroyed( SurfaceHolder arg0 )
 	{
 		// tell the thread to shut down and wait for it to finish
 		// this is a clean shutdown
 		boolean retry = true;
 		while( retry )
 		{
 			try
 			{
 				mainThread.join();
 				retry = false;
 			} catch( InterruptedException e )
 			{
 				// try again shutting down the thread
 			}
 		}
 		Log.d( TAG, "Thread was shut down cleanly" );
 	}
 
 	@Override
 	public boolean onTouchEvent( MotionEvent event )
 	{
 		int x = (int) event.getX();
 		int y = (int) event.getY();
 
 		if( event.getAction() == MotionEvent.ACTION_DOWN )
 		{
 			this.runner.jump();
 			
 			//move the guy left, right, or stop based on where on the screen was clicked
 			//TODO this is not currently functional
 			this.runner.setDirection(x);
 
 		}
 		return true;
 	}
 	
 	public void stopGame(){
 
 		mainThread.setRunning( false );
 		( (Activity) getContext() ).finish();
 	}
 	
 	public void newGame(){
		mainThread.setRunning( false );
		( (Activity) getContext() ).finish();
 	}
 	public void pauseGame(){
 	}
 	public void resumeGame(){
 	}
 	
 	public void setAvgFps( String avgFps )
 	{
 		this.avgFps = avgFps;
 	}
 
 	private void displayFps( Canvas canvas, String fps )
 	{
 		if( canvas != null && fps != null )
 		{
 			Paint paint = new Paint();
 			paint.setARGB( 255, 255, 255, 255 );
 			canvas.drawText( fps, this.getWidth() - 50, 20, paint );
 		}
 	}
 	
 }
