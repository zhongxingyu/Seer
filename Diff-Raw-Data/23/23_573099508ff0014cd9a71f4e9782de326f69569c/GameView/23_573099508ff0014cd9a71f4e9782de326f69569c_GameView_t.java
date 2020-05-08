 package com.slauson.asteroid_dasher.game;
 
 import com.slauson.asteroid_dasher.status.Options;
 
 import android.content.Context;
 import android.graphics.Canvas;
 import android.util.AttributeSet;
 import android.view.MotionEvent;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 
 /**
  * Main game view
  * 
  * Adapted from here: http://android-coding.blogspot.com/2012/01/create-surfaceview-game-step-by-step.html
  * 
  * @author Josh Slauson
  *
  */
 public class GameView extends SurfaceView implements SurfaceHolder.Callback {
 
 	private Game game;
 	
 	/** Holder for surface **/
 	private SurfaceHolder surfaceHolder;
 	
 	/** Canvas height modification factor (used for instructions) **/
 	private float canvasHeightFactor = 1;
 	/** Canvas height offset factor (used for instructions) **/
 	private float canvasHeightOffsetFactor = 0;
 	
 	public GameView(Context context) {
 		super(context);
 	}
 	
 	public GameView(Context context, AttributeSet attrs) {
 		super(context, attrs);
 		// TODO Auto-generated constructor stub
 	}
 	
 	public GameView(Context context, AttributeSet attrs, int defStyle) {
 		super(context, attrs, defStyle);
 	}
 
 	public void surfaceChanged(SurfaceHolder holder, int format, int width,
 			int height) {
 		// ignore for now
 	}
 
 	public void surfaceCreated(SurfaceHolder holder) {
 		
 		// initialize game
 		if (!game.isInitialized()) {
 			game.init(getWidth(), (int)(getHeight()*canvasHeightFactor));
 		} else {
 			// draw once if game is paused
 			if (Game.gameStatus == Game.STATUS_PAUSED) {
 				draw();
 			}
 		}
 	}
 
 	public void surfaceDestroyed(SurfaceHolder holder) {
 		// ignore for now
 	}
 	
 	/**
 	 * Sets up surface view
 	 */
 	public void onResume() {
 		
 		surfaceHolder = getHolder();
 		getHolder().addCallback(this);
 		
 	}
 	
 	@Override
 	public boolean onTouchEvent(MotionEvent event) {
 		
 		// only move when touch controls are being used and when ship in normal or invulnerability status
 		if (Options.controlType != Options.CONTROL_TOUCH) {
 			return false;
 		}
 		
 		game.touchEvent(event, -getHeight()*canvasHeightOffsetFactor);
 		return true;
 	}
 	
 	/**
 	 * Draw on surface view
 	 */
	public boolean draw() {
 		
 		Canvas canvas = null;
 
 		try {
 			canvas = surfaceHolder.lockCanvas();
 			
			// wait till canvas is non null
			// not the best solution, but sometimes the canvas isn't ready yet
			if (canvas == null) {
				return false;
			}
			
 			synchronized (surfaceHolder) {
 				
 				// translate canvas if we have a height offset
 				if (canvasHeightOffsetFactor > 0) {
 					canvas.save();
 					canvas.translate(0, getHeight()*canvasHeightOffsetFactor);
 				}
 				
 				game.draw(canvas);
 				
 				// restore canvas if we have a height offset
 				if (canvasHeightOffsetFactor > 0) {
 					canvas.restore();
 				}
 			}
 		} finally {
 			if (canvas != null) {
 				surfaceHolder.unlockCanvasAndPost(canvas);
 			}
 		}
		
		return true;
 	}
 	
 	/**
 	 * Sets game associated with this view
 	 * @param game game to associate with
 	 */
 	public void setGame(Game game) {
 		this.game = game;
 	}
 
 	/**
 	 * Sets canvas height characteristics
 	 * @param canvasHeightFactor height factor
 	 * @param canvasHeightOffset height top offset
 	 */
 	public void setHeight(float canvasHeightFactor, float canvasHeightOffset) {
 		this.canvasHeightFactor = canvasHeightFactor;
 		this.canvasHeightOffsetFactor = canvasHeightOffset;
 	}
 }
