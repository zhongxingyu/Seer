 package com.vesalaakso.rbb.model;
 
 import org.newdawn.slick.util.Log;
 
 import com.vesalaakso.rbb.RubberBandBall;
 
 /**
  * A class to store the game status, such as points and other general stuff that
  * is really important for the game itself to have any meaning whatsoever.
  * 
  * @author Vesa Laakso
  */
 public class GameStatus {
 
 	/** How many tries has a player made for maps, indexed by map level */
 	private int[] triesPerMap = new int[RubberBandBall.LEVEL_COUNT];
 
 	/**
 	 * How many times did the player have to shoot the player to be able to
 	 * complete different maps. Indexed by map level.
 	 */
 	private int[] scoresPerMap = new int[RubberBandBall.LEVEL_COUNT];
 
 	/** The current map level */
 	private int currentLevel;
 
 	/** Times the player has shot the ball in the current map */
 	private int currentShotCount;
 
 	/**
 	 * Constructs a new instance for this class.
 	 */
 	public GameStatus() {
 		reset();
 	}
 
 	/** Resets what needs to be reset when map is completed. */
 	private void reset() {
 		currentLevel = 0;
 		currentShotCount = 0;
 	}
 
 	/**
 	 * Gets the amount of tries the player has made in the given map.
 	 * 
 	 * @param map
 	 *            the map to query for tries
 	 * @return the times player has had to start the map over
 	 */
 	public int getTryCount(TileMap map) {
 		// Validate the level
 		int level = map.getLevel();
 		if (level <= 0 || level > RubberBandBall.LEVEL_COUNT) {
 			Log.warn("Weird map given for GameStatus#getTryCount()");
 			return 0;
 		}
 
 		int tries = triesPerMap[level - 1];
 		return tries;
 	}
 
 	/**
 	 * Adds one try to the amount of tries the player has made in the given map.
 	 * 
 	 * @param map
 	 *            the map to add try count for
 	 */
 	public void increaseTryCount(TileMap map) {
 		// Validate the level
 		int level = map.getLevel();
 		if (level <= 0 || level > RubberBandBall.LEVEL_COUNT) {
 			Log.warn("Weird map given for GameStatus#increaseTryCount()");
 			return;
 		}
 
 		triesPerMap[level - 1]++;
 	}
 
 	/**
 	 * Gets the lowest amount of shots the player has made to complete the given
 	 * map or 0 if the player has not yet passed the map.
 	 * 
 	 * @param map
 	 *            the map to query for score
 	 * @return the best score for a map or 0 if the map has not yet been played
 	 */
 	public int getScore(TileMap map) {
 		// Validate the level
 		int level = map.getLevel();
 		if (level <= 0 || level > RubberBandBall.LEVEL_COUNT) {
 			Log.warn("Weird map given for GameStatus#getScore()");
 			return 0;
 		}
 
 		int score = scoresPerMap[level - 1];
 		return score;
 	}
 
 	/**
 	 * Gets the amount of times the player has been launched in the current map.
 	 * 
 	 * @return amount of times the player has been launched in the current map
 	 */
 	public int getCurrentShotCount() {
 		return currentShotCount;
 	}
 
 	/**
 	 * Adds one to the times the player has been shot in the current map.
 	 */
 	public void increaseCurrentShotCount() {
 		currentShotCount++;
 	}
 
 	/**
 	 * Gets the current try count in the current map
 	 * 
 	 * @return the current try count in the current map
 	 */
 	public int getCurrentTryCount() {
 		// Validate the level
 		if (currentLevel <= 0) {
 			return 0;
 		}
 
		return triesPerMap[currentLevel - 1];
 	}
 
 	/**
 	 * Called when a map is completed, this updates the stored scores of maps.
 	 */
 	public void onMapCompleted() {
 		// Only allow this to happen if we're on a map at all.
 		if (currentLevel <= 0) {
 			return;
 		}
 
 		// Even a completion is a try
 		triesPerMap[currentLevel - 1]++;
 
 		int oldScore = scoresPerMap[currentLevel - 1];
 		if (oldScore > currentShotCount) {
 			// We did better than last time!
 			scoresPerMap[currentLevel - 1] = currentShotCount;
 		}
 
 		currentShotCount = 0;
 	}
 
 	/**
 	 * Called when a map is failed, this updates only the try count for the
 	 * current map.
 	 */
 	public void onMapFailed() {
 		// Only allow this to happen if we're on a map at all.
 		if (currentLevel <= 0) {
 			return;
 		}
 
 		triesPerMap[currentLevel - 1]++;
 		currentShotCount = 0;
 	}
 
 	/**
 	 * Called when a map is changed from one to another.
 	 * 
 	 * @param oldMap
 	 *            the map to change from, <code>null</code> if there was no map
 	 *            before the current one.
 	 * @param newMap
 	 *            the map to change to, <code>null</code> if there is no map to
 	 *            change to (i.e. we're moving to menu)
 	 */
 	public void onMapChange(TileMap oldMap, TileMap newMap) {
 		reset();
 
 		if (newMap != null) {
 			currentLevel = newMap.getLevel();
 		}
 	}
 
 	@Override
 	public String toString() {
 		// Construct a string for tries per map
 		StringBuilder sb = new StringBuilder("[");
 		for (int i = 0; i < triesPerMap.length; i++) {
 			sb.append(triesPerMap[i]);
 			if (i < triesPerMap.length - 1) {
 				sb.append(',');
 			}
 		}
 		sb.append(']');
 		String triesPerMapStr = sb.toString();
 
 		// Construct a string for scores per map
 		sb = new StringBuilder("[");
 		for (int i = 0; i < scoresPerMap.length; i++) {
 			sb.append(scoresPerMap[i]);
 			if (i < scoresPerMap.length - 1) {
 				sb.append(',');
 			}
 		}
 		sb.append(']');
 		String scoresPerMapStr = sb.toString();
 
 		// Construct the real string
 		String str =
 			"GameStatus=" + "triesPerMap:" + triesPerMapStr + ",scoresPerMap:"
 					+ scoresPerMapStr + ",currentLevel:" + currentLevel
 					+ ",currentShotCount:" + currentShotCount;
 
 		return str;
 	}
 }
