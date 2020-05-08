 /**
  *  Asteroids - the classic game, a Java implementation
  *  Copyright (C) 2011 @author Max DeLiso <maxdeliso@gmail.com>
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package game;
 
 import game.GameThread.GameInputOperation;
 
 import java.awt.Graphics2D;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Random;
 import java.util.concurrent.LinkedBlockingDeque;
 
 /**
  * This class describes the entire game state.
  * All other classes who which to access any part of the
  * game state must use the provided interface.
  * The state includes all of the game objects,
  * the pending input operations, the score, 
  * the timers, and some other information.
  */
 public class GameState
 {
 	public enum State
 	{
 		START,
 		PLAYING,
 		HIGH_SCORE,
 		PAUSED;
 	}
 
 	public enum HighScoreState
 	{
 		GETTING_INITIALS,
 		DISPLAYING_TABLE
 	}
 
 	private State state;
 	private HighScoreState highScoreState;
 	private boolean isRunning;
 	private boolean isListening;
 	private boolean isWaiting;
 	private GameThread gameThread;
 	private GameFrame gameFrame;
 	private GameCanvas gameCanvas;
 	private GameTimer gameTimer;
 	private GameScore gameScore;
 	private int asteroidCount;
 	private int shipCount;
 	private int bulletCount;
 	private Point mousePosition;
 	private String initialsString;
 
 	private LinkedBlockingDeque < GameObject > gameObjectList;
 	private LinkedBlockingDeque < GameParticleEffect > particleEffectList;
 	private LinkedBlockingDeque < GameInputOperation > blockingInputQueue;
 	private Map < String, List < Integer >> highScores =
 		new HashMap < String, List < Integer >> ();
 
 	/**
 	 * The main constructor
 	 * @param gameThread a reference to the game thread for message passing
 	 */
 	public GameState (GameThread gameThread)
 	{
 		this.gameThread = gameThread;
 
 		gameObjectList = new LinkedBlockingDeque < GameObject > ();
 		blockingInputQueue = new LinkedBlockingDeque < GameInputOperation > ();
 		particleEffectList = new LinkedBlockingDeque < GameParticleEffect > ();
 
 		isRunning = false;
 		isListening = false;
 		isWaiting = false;
 		state = State.START;
 
 		GameLauncher.log ("initialized game state");
 	}
 
 	/**
 	 * @return the current state
 	 */
 	public State getState ()
 	{
 		return state;
 	}
 
 	/**
 	 * @return the high score state
 	 */
 	public HighScoreState getHighScoreState ()
 	{
 		return highScoreState;
 	}
 
 	/**
 	 * @return the current initials
 	 */
 	public String getInitialsString ()
 	{
 		return initialsString;
 	}
 
 	/**
 	 * @return whether we are waiting
 	 */
 	public boolean isWaiting ()
 	{
 		return isWaiting;
 	}
 
 	/**
 	 * @return whether we are listening
 	 */
 	public boolean isListening ()
 	{
 		return isListening;
 	}
 
 	/**
 	 * @return whether we are running
 	 */
 	public boolean isRunning ()
 	{
 		return isRunning;
 	}
 
 	/**
 	 * This function sets the wait state
 	 * @param isWaiting the wait state
 	 */
 	public void setWaiting (boolean isWaiting)
 	{
 		this.isWaiting = isWaiting;
 	}
 
 	/**
 	 * This function sets the run state
 	 * @param isRunning the run state
 	 */
 	public void setRunning (boolean isRunning)
 	{
 		this.isRunning = isRunning;
 	}
 
 	/**
 	 * This function gets called by the mouse listener to update the mouse position on the game canvas.
 	 * Whenever this function gets called, a mouse moved event gets sent to the operation queue
 	 * so that the new value is processed.
 	 * @param mp the new mouse position.
 	 */
 	public void updateMousePosition (Point mp)
 	{
 		mousePosition = mp;
 	}
 
 	/** 
 	 * This function resets the game state 
 	 * to the title screen
 	 */
 	public void resetGame ()
 	{
 		GameLauncher.log ("switching state to START");
 		state = State.START;
 		gameCanvas.showCursor ();
 	}
 
 	/** 
 	 * This function switches the game state
 	 * to the playing state and starts the game timer.
 	 */
 	public void startGame ()
 	{
 		int shipWidth;
 		GameLauncher.log ("switching state to PLAYING");
 		state = State.PLAYING;
 
 		gameFrame = gameThread.getFrame ();
 		gameCanvas = gameFrame.getCanvas ();
 		gameCanvas.hideCursor ();
 		gameTimer = new GameTimer (gameThread);
 		gameScore = new GameScore (gameThread);
 
 		gameObjectList.
 		add (new
 				GameShip (new
 						Point (GameCanvas.defaultDimension.width / 2 -
 								GameShip.PLAYER_SHIP_WIDTH / 2,
 								GameCanvas.defaultDimension.height / 2),
 								GameShip.GameShipType.PLAYER_SHIP, -Math.PI / 2));
 	}
 
 	/**
 	 * This function switches the game state to paused
 	 * and pauses the game timer.
 	 */
 	public void pauseGame ()
 	{
 		if (state == State.PLAYING)
 		{
 			GameLauncher.log ("switching state to PAUSED");
 
 			state = State.PAUSED;
 			gameCanvas.showCursor ();
 			gameTimer.pause ();
 		}
 	}
 
 	/**
 	 * This function switches the game state back to
 	 * playing from paused.
 	 */
 	public void resumeGame ()
 	{
 		if (state == State.PAUSED)
 		{
 			GameLauncher.log ("switching state to PLAYING");
 			state = State.PLAYING;
 			gameCanvas.hideCursor ();
 			gameTimer.resume ();
 		}
 		else
 		{
 			GameLauncher.logError ("called resume when not in the paused state");
 		}
 	}
 
 	/**
 	 * This function switches the game state back to
 	 * playing from paused.
 	 */
 	public void stopGame ()
 	{
 		gameCanvas.hideCursor ();
 		gameTimer.kill ();
 		gameObjectList.clear ();
 
 		if (gameScore.getScore () == 0)
 		{
 			highScores = gameThread.getGameDatabaseConnector ().getScores ();
 			gameThread.setHighScorePairs (generateHighScorePairs ());
 			state = State.HIGH_SCORE;
 			highScoreState = HighScoreState.DISPLAYING_TABLE;
 		}
 		else
 		{
 			GameLauncher.log ("switching state to HIGH_SCORE");
 			state = State.HIGH_SCORE;
 			highScoreState = HighScoreState.GETTING_INITIALS;
 			initialsString = "";
 		}
 	}
 
 	/**
 	 * This function destroys the game timer.
 	 */
 	public void killTimer ()
 	{
 		if (gameTimer != null)
 		{
 			gameTimer.kill ();
 		}
 	}
 
 	/**
 	 * @return the game timer
 	 */
 	public GameTimer getTimer ()
 	{
 		return gameTimer;
 	}
 
 	/** 
 	 * This function gets called by one of the game objects
 	 * and tells the main thread to add a new enemy to the object list
 	 */
 	public void spawnEnemy ()
 	{
 		GameLauncher.log ("Timer event SPAWN_ENEMIES");
 	}
 
 	/**
 	 * @return the game score
 	 */
 	public GameScore getScore ()
 	{
 		return gameScore;
 	}
 
 	/** 
 	 * This function gets called by one of the game objects
 	 * and tells the main thread to add a new asteroid to the object list
 	 */
 	public void spawnAsteroid ()
 	{
 		if (asteroidCount < GameLauncher.ASTEROID_CUTOFF_COUNT)
 		{
 			GameLauncher.logFrequent ("spawning asteroid");
 			gameObjectList.add (GameAsteroid.spawnAsteroid( GameAsteroid.ASTEROID_START_SIZE) );
 		}
 		else
 		{
 			GameLauncher.
 			logFrequent ("not spawning asteroid, there are enough already");
 		}
 	}
 
 	/** 
 	 * This function gets called by one of the game objects
 	 * and tells the main thread to add a new powerup to the object list
 	 */
 	public void spawnPowerup ()
 	{
 		GameLauncher.log ("Timer event SPAWN_POWERUP");
 	}
 
 	/**
 	 * This function pokes the player's ship and asks it to fire a
 	 * bullet, and then adds the returned bullet to the game
 	 * object list.
 	 */
 	public void fireBullet ()
 	{
 		if (bulletCount < GameLauncher.PLAYER_BULLET_CUTOFF_COUNT)
 		{
 			GameBullet newBullet =
 				((GameShip) gameObjectList.getFirst ()).fire ();
 
 			if (newBullet != null)
 			{
 				gameObjectList.add (newBullet);
 			}
 		}
 	}
 
 	/**
 	 * This function performs updates on all the game objects,
 	 * keeping their state consistent and making them interact in various ways.
 	 * The 'brains' of the game go here. All input is processed here and
 	 * other state information is handled.
 	 */
 	void updateGameState ()
 	{
 		processInputQueue ();
 
 		if (isRunning ())		/* the input queue might have switched the run state */
 		{
 			switch (getState ())
 			{
 			case START:
 
 				break;
 			case PLAYING:
 				updateGameObjects ();
 				createParticleEffects();
 				handleCollisions ();
 				break;
 			case PAUSED:
 				break;
 			case HIGH_SCORE:
 				break;
 			}
 		}
 	}
 
 	/**
 	 * This function updates all game objects
 	 * and counts to see how many of each of them are currently
 	 * allocated.
 	 */
 	private void updateGameObjects ()
 	{
 		int newShipCount = 0;
 		int newAsteroidCount = 0;
 		int newBulletCount = 0;
 		GameObject currentObject;
 
 		Iterator < GameObject > i = gameObjectList.iterator ();
 		while (i.hasNext ())
 		{
 			currentObject = i.next ();
 			if (currentObject.isValid)
 			{
 				currentObject.update ();
 				if (currentObject instanceof GameShip)
 					newShipCount++;
 				else if (currentObject instanceof GameAsteroid)
 					newAsteroidCount++;
 				else if (currentObject instanceof GameBullet)
 					newBulletCount++;
 			}
 			else
 			{
 				i.remove ();
 			}
 		}
 
 		Iterator < GameParticleEffect > j = particleEffectList.iterator ();
 		while (j.hasNext ())
 		{
 			currentObject = j.next ();
 			if (currentObject.isValid)
 			{
 				currentObject.update ();
 			}
 			else
 			{
 				j.remove ();
 			}
 		}
 
 		shipCount = newShipCount;
 		asteroidCount = newAsteroidCount;
 		bulletCount = newBulletCount;
 	}
 
 	private void createParticleEffects()
 	{
 		GameParticleEffect gpe = ((GameShip)gameObjectList.getFirst()).getParticleEffect();
 
 		if( gpe != null )
 		{
 			particleEffectList.add(gpe);
 		}
 	}
 
 	/**
 	 * This is the main collision detection loop. It runs in O(n^2) so it is
 	 * critical that the number of game objects remains relatively low.
 	 */
 	private void handleCollisions ()
 	{
 		GameLauncher.logFrequent ("Starting collision detection loop with " +
 				gameObjectList.size () + " game objects");
 		GameObject go_inside, go_outside;
 		int inside_index, outside_index;
 		int collisionCount = 0;
 		inside_index = 0;
 
 		for (Iterator < GameObject > it_outside = gameObjectList.iterator ();
 		it_outside.hasNext (); inside_index++)
 		{
 			go_outside = it_outside.next ();
 
 			outside_index = 0;
 			for (Iterator < GameObject > it_inside = gameObjectList.iterator ();
 			it_inside.hasNext () && outside_index < inside_index;
 			outside_index++)
 			{
 				go_inside = it_inside.next ();
 
 				collisionCount++;
 				if (go_inside.isTouching (go_outside))
 				{
 					GameLauncher.log ("COLLISION BETWEEN " + go_inside + " and " +
 							go_outside);
 
 					if (go_inside instanceof GameShip)
 					{
 						if (go_outside instanceof GameShip)
 						{
 							shipHitShip ((GameShip) go_inside,
 									(GameShip) go_outside);
 						}
 						else if (go_outside instanceof GameAsteroid)
 						{
 							shipHitAsteroid ((GameShip) go_inside,
 									(GameAsteroid) go_outside);
 						}
 						else if (go_outside instanceof GameBullet)
 						{
 							shipHitBullet ((GameShip) go_inside,
 									(GameBullet) go_outside);
 						}
 					}
 					else if (go_inside instanceof GameAsteroid)
 					{
 						if (go_outside instanceof GameShip)
 						{
 							shipHitAsteroid ((GameShip) go_outside,
 									(GameAsteroid) go_inside);
 						}
 						else if (go_outside instanceof GameAsteroid)
 						{
 							asteroidHitAsteroid ((GameAsteroid) go_inside,
 									(GameAsteroid) go_outside);
 						}
 						else if (go_outside instanceof GameBullet)
 						{
 							asteroidHitBullet ((GameAsteroid) go_inside,
 									(GameBullet) go_outside);
 						}
 					}
 					else if (go_inside instanceof GameBullet)
 					{
 						if (go_outside instanceof GameShip)
 						{
 							shipHitBullet ((GameShip) go_outside,
 									(GameBullet) go_inside);
 						}
 						else if (go_outside instanceof GameAsteroid)
 						{
 							asteroidHitBullet ((GameAsteroid) go_outside,
 									(GameBullet) go_inside);
 						}
 						else if (go_outside instanceof GameBullet)
 						{
 							bulletHitBullet ((GameBullet) go_inside,
 									(GameBullet) go_outside);
 						}
 					}
 
 				}
 				else
 				{
 					/* no collision */
 				}
 			}
 		}
 
 		GameLauncher.logFrequent ("Finished collision detection loop with " +
 				gameObjectList.size () +
 				" game objects, processed " + collisionCount +
 		" possible collisions");
 	}
 
 	/**
 	 * Called when a ship hits a ship
 	 * @param shipOne the first ship
 	 * @param shipTwo the second ship
 	 */
 	public void shipHitShip (GameShip shipOne, GameShip shipTwo)
 	{
 
 	}
 
 	/**
 	 * Called when a ship hits an asteroid
 	 * @param ship the ship
 	 * @param asteroid the asteroid
 	 */
 	public void shipHitAsteroid (GameShip ship, GameAsteroid asteroid)
 	{
 		if (gameObjectList.getFirst ().equals (ship))
 		{
 			stopGame ();
 		}
 	}
 
 	/**
 	 * Called when a ship hits a bullet
 	 * @param ship the ship 
 	 * @param bullet the bullet
 	 */
 	public void shipHitBullet (GameShip ship, GameBullet bullet)
 	{
 
 	}
 
 	/**
 	 * Called when an asteroid hits an asteroid
 	 * @param asteroidOne the first asteroid
 	 * @param asteroidTwo the second asteroid
 	 */
 	public void asteroidHitAsteroid (GameAsteroid asteroidOne,
 			GameAsteroid asteroidTwo)
 	{
 		GameAsteroid.collideAsteroids (asteroidOne, asteroidTwo);
 		//TODO: add particle FX
 	}
 
 	/**
 	 * Called when an asteroid hits a bullet
 	 * @param asteroid the asteroid
 	 * @param bullet the bullet
 	 */
 	public void asteroidHitBullet (GameAsteroid asteroid, GameBullet bullet)
 	{
 		Rectangle asteroidBounds = asteroid.getSize();
 		asteroid.isValid = false;
 		bullet.isValid = false;
 		GameAsteroid newAsteroids[] = asteroid.split ();
 
 		gameScore.splitAsteroid ();
 
 		if (newAsteroids != null)
 		{
 			GameLauncher.
 			logFrequent ("adding new asteroids to the game object list");
 			for (int ii = 0; ii < newAsteroids.length; ii++)
 				gameObjectList.add (newAsteroids[ii]);
 		}
 		
 		for( int i = 0; i < 10; i++ )
 		{
 			particleEffectList.add(
 					new GameParticleEffect(
 							asteroid.x+asteroidBounds.width/2, 
 							asteroid.y+asteroidBounds.height/2 ));
 
 		}
 	}
 
 	/**
 	 * Called when a bullet hits a bullet
 	 * @param bulletOne the first bullet
 	 * @param bulletTwo the second bullet
 	 */
 	public void bulletHitBullet (GameBullet bulletOne, GameBullet bulletTwo)
 	{
 
 	}
 
 	/**
 	 * Called to process a mouse rotation
 	 * @param gio the type of mouse rotation
 	 */
 	public void processMouseRotation (GameInputOperation gio)
 	{
 		switch (gio)
 		{
 		case MOUSE_MOVED_LEFT:
 
 			break;
 		case MOUSE_MOVED_RIGHT:
 
 			break;
 		case MOUSE_MOVED_UP:
 
 			break;
 		case MOUSE_MOVED_DOWN:
 
 			break;
 		default:
 			GameLauncher.
 			logError ("invalid input operation sent to processMouseRotation");
 			break;
 		}
 	}
 
 	/**
 	 * Iterate through the pending input operations
 	 * and process them according to the current game state.
 	 */
 	private void processInputQueue ()
 	{
 		gameFrame = gameThread.getFrame ();
 		GameInputOperation gio;
 		boolean processedItem;
 
 		while (blockingInputQueue.size () > 0)
 		{
 			gio = blockingInputQueue.getFirst ();
 			processedItem = false;
 			GameLauncher.logFrequent ("caught " + gio);
 
 			if (getState () == GameState.State.START && !processedItem)
 			{
 				switch (gio)
 				{
 				case MOUSE_CLICK_LEFT:
 				case MOUSE_CLICK_RIGHT:
 				case KEYBOARD_ENTER_DOWN:
 				case KEYBOARD_SPACE_DOWN:
 					processedItem = true;
 					startGame ();
 					break;
 				case KEYBOARD_F_DOWN:
 					processedItem = true;
 					gameFrame.toggleFullscreen ();
 					break;
 				case KEYBOARD_ESC_DOWN:
 					processedItem = true;
 					gameThread.postGameShutdown ();
 					break;
 				}
 			}
 			else if (getState () == GameState.State.PLAYING && !processedItem)
 			{
 				switch (gio)
 				{
 				case MOUSE_MOVED_LEFT:
 					processMouseRotation (GameInputOperation.MOUSE_MOVED_LEFT);
 					processedItem = true;
 					break;
 				case MOUSE_MOVED_RIGHT:
 					processMouseRotation (GameInputOperation.MOUSE_MOVED_RIGHT);
 					processedItem = true;
 					break;
 				case MOUSE_MOVED_UP:
 					processMouseRotation (GameInputOperation.MOUSE_MOVED_UP);
 					processedItem = true;
 					break;
 				case MOUSE_MOVED_DOWN:
 					processMouseRotation (GameInputOperation.MOUSE_MOVED_DOWN);
 					processedItem = true;
 					break;
 				case MOUSE_CLICK_LEFT:
 					processedItem = true;
 					fireBullet ();
 					break;
 				case MOUSE_CLICK_RIGHT:
 					processedItem = true;
 					break;
 				case KEYBOARD_W_DOWN:
 					processedItem = true;
 					((GameShip) gameObjectList.getFirst ()).forwardThruster ();
 					break;
 				case KEYBOARD_A_DOWN:
 					((GameShip) gameObjectList.getFirst ()).leftThruster ();
 					processedItem = true;
 					break;
 				case KEYBOARD_S_DOWN:
 					((GameShip) gameObjectList.getFirst ()).reverseThruster ();
 					processedItem = true;
 					break;
 				case KEYBOARD_D_DOWN:
 					((GameShip) gameObjectList.getFirst ()).rightThruster ();
 					processedItem = true;
 					break;
 				case KEYBOARD_W_UP:
 					((GameShip) gameObjectList.getFirst ()).killMainThruster ();
 					processedItem = true;
 					break;
 				case KEYBOARD_A_UP:
 					((GameShip) gameObjectList.getFirst ()).
 					killRotationalThruster ();
 					processedItem = true;
 					break;
 				case KEYBOARD_S_UP:
 					((GameShip) gameObjectList.getFirst ()).killMainThruster ();
 					processedItem = true;
 					break;
 				case KEYBOARD_D_UP:
 					((GameShip) gameObjectList.getFirst ()).
 					killRotationalThruster ();
 					processedItem = true;
 					break;
 				case KEYBOARD_SPACE_DOWN:
 					processedItem = true;
 					fireBullet ();
 					break;
 				case KEYBOARD_P_DOWN:
 					processedItem = true;
 					pauseGame ();
 					break;
 				case KEYBOARD_F_DOWN:
 					processedItem = true;
 					pauseGame ();
 					gameFrame.toggleFullscreen ();
 					break;
 				case KEYBOARD_ESC_DOWN:
 					processedItem = true;
 					gameThread.postGameShutdown ();
 					break;
 				}
 			}
 			else if (getState () == GameState.State.PAUSED && !processedItem)
 			{
 				switch (gio)
 				{
 				case KEYBOARD_P_DOWN:
 					processedItem = true;
 					resumeGame ();
 					break;
 				case KEYBOARD_F_DOWN:
 					processedItem = true;
 					gameFrame.toggleFullscreen ();
 					break;
 				case KEYBOARD_ESC_DOWN:
 					processedItem = true;
 					gameThread.postGameShutdown ();
 					break;
 				}
 			}
 			else if (getState () == GameState.State.HIGH_SCORE && !processedItem)
 			{
 				if (highScoreState == HighScoreState.GETTING_INITIALS)
 				{
 					switch (gio)
 					{
 					case MOUSE_CLICK_LEFT:
 					case MOUSE_CLICK_RIGHT:
 					case KEYBOARD_SPACE_DOWN:
 					case KEYBOARD_ENTER_DOWN:
 						if (initialsString.length () >= 3)
 						{
 							processedItem = true;
 							gameThread.getGameDatabaseConnector ().
 							insertNewScore (initialsString.substring (0, 3),
 									gameScore.getScore ());
 							highScores =
 								gameThread.getGameDatabaseConnector ().getScores ();
 							gameThread.
 							setHighScorePairs (generateHighScorePairs ());
 							highScoreState = HighScoreState.DISPLAYING_TABLE;
 							//      resetGame();
 						}
 						break;
 					case KEYBOARD_BACKSPACE_DOWN:
						if (initialsString.length () == 1)
 						{
 							initialsString = "";
 						}
 						else
 						{
 							initialsString =
 								initialsString.substring (0,
 										initialsString.length () -
 										1);
 						}
 						processedItem = true;
 						break;
 					case KEYBOARD_A_TYPED:
 						initialsString += 'A';
 						processedItem = true;
 						break;
 					case KEYBOARD_B_TYPED:
 						initialsString += 'B';
 						processedItem = true;
 						break;
 					case KEYBOARD_C_TYPED:
 						initialsString += 'C';
 						processedItem = true;
 						break;
 					case KEYBOARD_D_TYPED:
 						initialsString += 'D';
 						processedItem = true;
 						break;
 					case KEYBOARD_E_TYPED:
 						initialsString += 'E';
 						processedItem = true;
 						break;
 					case KEYBOARD_F_TYPED:
 						initialsString += 'F';
 						processedItem = true;
 						break;
 					case KEYBOARD_G_TYPED:
 						initialsString += 'G';
 						processedItem = true;
 						break;
 					case KEYBOARD_H_TYPED:
 						initialsString += 'H';
 						processedItem = true;
 						break;
 					case KEYBOARD_I_TYPED:
 						initialsString += 'I';
 						processedItem = true;
 						break;
 					case KEYBOARD_J_TYPED:
 						initialsString += 'J';
 						processedItem = true;
 						break;
 					case KEYBOARD_K_TYPED:
 						initialsString += 'K';
 						processedItem = true;
 						break;
 					case KEYBOARD_L_TYPED:
 						initialsString += 'L';
 						processedItem = true;
 						break;
 					case KEYBOARD_M_TYPED:
 						initialsString += 'M';
 						processedItem = true;
 						break;
 					case KEYBOARD_N_TYPED:
 						initialsString += 'N';
 						processedItem = true;
 						break;
 					case KEYBOARD_O_TYPED:
 						initialsString += 'O';
 						processedItem = true;
 						break;
 					case KEYBOARD_P_TYPED:
 						initialsString += 'P';
 						processedItem = true;
 						break;
 					case KEYBOARD_Q_TYPED:
 						initialsString += 'Q';
 						processedItem = true;
 						break;
 					case KEYBOARD_R_TYPED:
 						initialsString += 'R';
 						processedItem = true;
 						break;
 					case KEYBOARD_S_TYPED:
 						initialsString += 'S';
 						processedItem = true;
 						break;
 					case KEYBOARD_T_TYPED:
 						initialsString += 'T';
 						processedItem = true;
 						break;
 					case KEYBOARD_U_TYPED:
 						initialsString += 'U';
 						processedItem = true;
 						break;
 					case KEYBOARD_V_TYPED:
 						initialsString += 'V';
 						processedItem = true;
 						break;
 					case KEYBOARD_W_TYPED:
 						initialsString += 'W';
 						processedItem = true;
 						break;
 					case KEYBOARD_X_TYPED:
 						initialsString += 'X';
 						processedItem = true;
 						break;
 					case KEYBOARD_Y_TYPED:
 						initialsString += 'Y';
 						processedItem = true;
 						break;
 					case KEYBOARD_Z_TYPED:
 						initialsString += 'Z';
 						processedItem = true;
 						break;
 					case KEYBOARD_ESC_DOWN:
 						processedItem = true;
 						gameThread.postGameShutdown ();
 						break;
 					}
 				}
 				else if (highScoreState == HighScoreState.DISPLAYING_TABLE)
 				{
 					switch (gio)
 					{
 					case MOUSE_CLICK_LEFT:
 					case MOUSE_CLICK_RIGHT:
 					case KEYBOARD_SPACE_DOWN:
 						resetGame ();
 						break;
 					case KEYBOARD_F_DOWN:
 						processedItem = true;
 						pauseGame ();
 						gameFrame.toggleFullscreen ();
 						break;
 					case KEYBOARD_ESC_DOWN:
 						processedItem = true;
 						gameThread.postGameShutdown ();
 						break;
 					}
 				}
 			}
 			blockingInputQueue.remove ();
 		}
 	}
 
 	/**
 	 * This function adds a new input operation onto the tail of the input queue.
 	 * @param gio the game input operation which we want to enqueue into the blocking input queue
 	 */
 	public void enqueueInputOperation (GameInputOperation gio)
 	{
 		if (isListening ())
 		{
 			blockingInputQueue.add (gio);
 		}
 	}
 
 	/**
 	 * This function tells the input queue to accept new items.
 	 */
 	public synchronized void acceptInput ()
 	{
 		GameLauncher.log ("now accepting input");
 		isListening = true;
 	}
 
 	/**
 	 * This function tells the input queue to ignore any new items.
 	 */
 	public synchronized void ignoreInput ()
 	{
 		GameLauncher.log ("now ignoring input");
 		isListening = false;
 	}
 
 	/**
 	 * This function draws all the game objects
 	 * @param g the graphical context
 	 */
 	public void drawObjects (Graphics2D g)
 	{
 		Iterator < GameObject > i = gameObjectList.iterator ();
 		while (i.hasNext ())
 		{
 			i.next ().drawObject (g);
 		}
 
 		Iterator < GameParticleEffect > j = particleEffectList.iterator ();
 		while (j.hasNext ())
 		{
 			j.next ().drawObject (g);
 		}
 	}
 
 	/**
 	 * This function takes the simulated multimap, sorts it
 	 * and returns the result.
 	 * @return the sorted list
 	 */
 	private HighScorePair[] generateHighScorePairs ()
 	{
 		LinkedList < HighScorePair > highScorePairsList =
 			new LinkedList < HighScorePair > ();
 		HighScorePair[] highScorePairs = null;
 
 		if (highScores == null)
 		{
 			return null;
 		}
 
 		for (Iterator < Entry < String, List < Integer >>> ii =
 			highScores.entrySet ().iterator (); ii.hasNext ();)
 		{
 			Entry < String, List < Integer >> nextEntry = ii.next ();
 
 			for (Iterator < Integer > jj = nextEntry.getValue ().iterator ();
 			jj.hasNext ();)
 			{
 				highScorePairsList.
 				push (new HighScorePair (nextEntry.getKey (), jj.next ()));
 			}
 		}
 
 		highScorePairs = highScorePairsList.toArray (new HighScorePair[0]);
 
 		Arrays.sort (highScorePairs);
 		return highScorePairs;
 	}
 
 	/**
 	 * This internal class is used to help sort high score pairs.
 	 */
 	public class HighScorePair implements Comparable < Object >
 	{
 		private String initials;
 		private int score;
 
 		/**
 		 * The main constructor
 		 * @param initials the initials
 		 * @param score the score
 		 */
 		HighScorePair (String initials, int score)
 		{
 			this.initials = initials;
 			this.score = score;
 		}
 
 		/**
 		 * This function is required because we implemented the Comparable 
 		 * interface
 		 */
 		@Override public int compareTo (Object arg0)
 		{
 			if (!(arg0 instanceof HighScorePair))
 			{
 				return 1;
 			}
 			HighScorePair other = (HighScorePair) arg0;
 
 			if (other.getScore () == score)
 			{
 				return 0;
 			}
 			else if (other.getScore () > score)
 			{
 				return 1;
 			}
 			else
 			{
 				return -1;
 			}
 		}
 
 		/**
 		 * @return the score
 		 */
 		public int getScore ()
 		{
 			return score;
 		}
 
 		/**
 		 * @return the initials
 		 */
 		public String getInitials ()
 		{
 			return initials;
 		}
 	}
 }
