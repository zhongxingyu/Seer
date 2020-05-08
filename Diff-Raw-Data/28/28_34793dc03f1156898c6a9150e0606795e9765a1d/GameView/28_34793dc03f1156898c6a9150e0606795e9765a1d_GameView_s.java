 package com.slauson.dasher.game;
 
 import com.slauson.dasher.status.Options;
 
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
 	
 	private float canvasHeightFactor = 1;
 	private float canvasHeightOffset = 0;
 	
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
 		game.init(getWidth(), (int)(getHeight()*canvasHeightFactor));
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
 		
 		game.touchEvent(event, -getHeight()*canvasHeightOffset);
 		return true;
 	}

 	/**
 	 * Draw on surface view
 	 */
 	public void draw() {
 		
 		Canvas canvas = null;
 
 		try {
 			canvas = surfaceHolder.lockCanvas();
 			
 			synchronized (surfaceHolder) {
 				
 				// translate canvas if we have a height offset
 				if (canvasHeightOffset > 0) {
 					canvas.translate(0, getHeight()*canvasHeightOffset);
 				}
 				
 				game.draw(canvas);
 				
 				// restore canvas if we have a height offset
 				if (canvasHeightOffset > 0) {
 					canvas.restore();
 				}
 			}
 		} finally {
 			if (canvas != null) {
 				surfaceHolder.unlockCanvasAndPost(canvas);
 			}
 		}
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
 		this.canvasHeightOffset = canvasHeightOffset;
 	}
 }
