 package com.slauson.dasher.other;
 
 import com.slauson.dasher.R;
 import com.slauson.dasher.game.Accelerometer;
 import com.slauson.dasher.game.Game;
 import com.slauson.dasher.game.GameThread;
 import com.slauson.dasher.game.GameView;
 import com.slauson.dasher.status.Configuration;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.KeyEvent;
 
 /**
  * Abstract class used for updating activity from game.
  * 
  * Used by GameActivity and InstructionsMenu.
  * 
  * @author Josh Slauson
  *
  */
 public abstract class GameBaseActivity extends Activity implements GameThreadCallback {
 
 	/** Game **/
 	protected Game game;
 
 	/** Game view **/
 	protected GameView gameView;
 
 	/** Game thread **/
 	protected GameThread gameThread;
 	
 	/** Accelerometer **/
 	protected Accelerometer accelerometer;
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		System.out.println("GameBaseActivity::onCreate()");
 		
 		game = new Game(this);
 		accelerometer = new Accelerometer(this);
 	}
 	
 	@Override
 	protected void onResume() {
 		super.onResume();
 		gameView.onResume();
 		
 		System.out.println("GameBaseActivity::onResume()");
 		
 		// Create and start background Thread
 		gameThread = new GameThread(this);
 		gameThread.setRunning(true);
 		gameThread.start();
 		
 		if (Configuration.controlType == Configuration.CONTROL_ACCELEROMETER) {
 			accelerometer.registerListener();
 		}
 	}
 	
 	@Override
 	protected void onPause() {
 		super.onPause();
 		
 		System.out.println("GameBaseActivity::onPause()");
 		
 		// Kill the background thread
 		boolean retry = true;
 		gameThread.setRunning(false);
 		
 		while (retry) {
 			try {
 				gameThread.join();
 				retry = false;
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		}
 		
 		if (Configuration.controlType == Configuration.CONTROL_ACCELEROMETER) {
 			accelerometer.unregisterListener();
 		}
 	}
 	
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 
 		// only move when keyboard controls are being used and when ship in normal or invulnerability status
 		if (Configuration.controlType != Configuration.CONTROL_KEYBOARD) {
 			return false;
 		}
 
 		game.keyDown(keyCode);
 		return true;
 	}
 	
 	@Override
 	public boolean onKeyUp(int keyCode, KeyEvent event) {
 		
 		// only move player ship when its in normal or invulnerability status
 		if (Configuration.controlType != Configuration.CONTROL_KEYBOARD) {
 			return false;
 		}
 		
 		game.keyUp(keyCode);
 		
 		return true;
 	}
 
 	/**
 	 * Updates game state and draws everything to surface view
 	 */
 	public void update() {
 		if (game.isInitialized()) {
 			game.updateStates();
 			gameView.draw();
 		}
 	}
 	
 	/**
 	 * Updates accelerometer
 	 * @param tx horizontal orientation
 	 * @param ty vertical orientation
 	 */
 	public void updateAccelerometer(float tx, float ty) {
 		
 		// only move when accelerometer controls are being used or when ship in normal or invulnerability status
 		if (Configuration.controlType != Configuration.CONTROL_ACCELEROMETER) {
 			return;
 		}
 		
 		game.updateAccelerometer(tx, ty);
 	}
 
 	/**
 	 * Used when game is over so we can quit properly
 	 */
 	public abstract void gameOver();
 }
