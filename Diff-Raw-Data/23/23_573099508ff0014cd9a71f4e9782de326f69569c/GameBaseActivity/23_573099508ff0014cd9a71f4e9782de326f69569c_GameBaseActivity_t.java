 package com.slauson.asteroid_dasher.other;
 
 import com.slauson.asteroid_dasher.game.Accelerometer;
 import com.slauson.asteroid_dasher.game.Game;
 import com.slauson.asteroid_dasher.game.GameThread;
 import com.slauson.asteroid_dasher.game.GameView;
 import com.slauson.asteroid_dasher.status.Options;
 
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
 public abstract class GameBaseActivity extends Activity {
 
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
 		
 		toggleGameThread(true);
 		accelerometer = new Accelerometer(this);
 	}
 	
 	@Override
 	protected void onResume() {
 		super.onResume();
 		gameView.onResume();
 		
 		if (Options.controlType == Options.CONTROL_ACCELEROMETER) {
 			accelerometer.registerListener();
 		}
 	}
 	
 	@Override
 	protected void onPause() {
 		super.onPause();
 		
 		toggleGameThread(false);
 		
 		if (Options.controlType == Options.CONTROL_ACCELEROMETER) {
 			accelerometer.unregisterListener();
 		}
 	}
 	
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 
 		// only move when keyboard controls are being used and when ship in normal or invulnerability status
 		if (game != null && game.isInitialized() && Options.controlType == Options.CONTROL_KEYBOARD) {
 			game.keyDown(keyCode);
 		}
 
 		
 		return true;
 	}
 	
 	@Override
 	public boolean onKeyUp(int keyCode, KeyEvent event) {
 		
 		// only move player ship when keyboard controls are being used
 		if (game != null && game.isInitialized() && Options.controlType == Options.CONTROL_KEYBOARD) {
 			game.keyUp(keyCode);
 		}
 		
 		return true;
 	}
 	
 	/**
 	 * Updates game state and draws everything to surface view
 	 */
 	public void update() {
 		if (game != null && game.isInitialized()) {
			// only update states if canvas is ready
			if (gameView.draw()) {
				game.updateStates();
			}
 		}
 	}
 	
 	/**
 	 * Updates accelerometer
 	 * @param tx horizontal orientation
 	 * @param ty vertical orientation
 	 */
 	public void updateAccelerometer(float tx, float ty) {
 		
 		// only move when accelerometer controls are being used or when ship in normal or invulnerability status
 		if (Options.controlType != Options.CONTROL_ACCELEROMETER) {
 			return;
 		}
 		
 		if (game != null && game.isInitialized()) {
 			game.updateAccelerometer(tx, ty);
 		}
 	}
 	
 	/**
 	 * Toggles running of game thread
 	 * @param running true if game thread should be running
 	 */
 	protected void toggleGameThread(boolean running) {
 
 		// Create and start new background thread
 		if (running) {
 			gameThread = new GameThread(this);
 			gameThread.setRunning(true);
 			gameThread.start();
 		}
 		// otherwise kill the background thread
 		else {
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
 		}
 	}
 	
 	/**
 	 * Used to initialize any canvas related stuff
 	 */
 	public abstract void init();
 
 	/**
 	 * Used when game is over so we can quit properly
 	 */
 	public abstract void gameOver();
 }
