 package kara.greenfoot.sokoban;
 
 import greenfoot.*;
 
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.List;
 
 /**
  * Saves the highscores to the Greenfoot server using UserInfo. This can be used
  * if published to the Greenfoot Gallery.
  * <p>
  * 
  * UserInfo is originally designed to have only one highscore but we need one
  * highscore for every level. To work around this limitation, we use the
  * available Strings to store the users moves for each level. The score is
  * simply incremented whenever someone adds a new highscore entry. Then we can
  * use the score to get the most recently added players and compare their moves
  * in each level.
  * <p>
  * 
  * For each level we store the moves in a single char. The UserInfo can store 5
  * Strings of 50 characters (at the moment). So we can save move information for
  * a maximum of 250 levels.
  * <p>
  * Note: Every User will only be stored once for every level, e.g. it is not
  * possible for one user to be on first and second place.
  * 
  * @author Marco Jakob (http://edu.makery.ch)
  */
 public class ServerHighscore extends HighscoreManager {
 	private UserInfo myUserInfo;
 	private int topScore;
 
 	/**
 	 * The highscore map with the level number as a key and the Highscore as a
 	 * value.
 	 */
 	private Map<Integer, Highscore> highscores;
 
 	/**
 	 * Constructor.
 	 */
 	public ServerHighscore() {
 		myUserInfo = UserInfo.getMyInfo();
 	}
 
 	/**
 	 * Returns true if the ServerHighscore is available (can at least read
 	 * highscore info).
 	 */
 	public static boolean isAvailable() {
 		return UserInfo.getTop(0) != null;
 	}
 
 	/**
 	 * Initially loads the highscore from the server.
 	 */
 	public void initHighscores() {
 		initHighscoresFromUserInfos();
 	}
 
 	/**
 	 * Returns it the manager is read only.
 	 */
 	public boolean isReadOnly() {
 		return myUserInfo == null;
 	}
 
 	/**
 	 * Returns the name of the current player. If the user is not logged in,
 	 * null is returned.
 	 */
 	public String getCurrentPlayerName() {
 		if (myUserInfo == null) {
 			return null;
 		}
 		return myUserInfo.getUserName();
 	}
 
 	/**
 	 * This is ignored since the user name is set through the login in UserInfo.
 	 */
 	public void setCurrentPlayerName(String currentPlayerName) {
 		// do nothing
 	}
 
 	/**
 	 * Returns the Highscore for the specified level. The returned Highscore is
 	 * a clone. To store a change in the highscore, the method setHighscore(...)
 	 * must be called.
 	 */
 	public Highscore getHighscoreForLevel(int levelNumber) {
 		if (highscores == null) {
 			return null;
 		}
 
 		Highscore h = highscores.get(levelNumber);
 		if (h == null) {
 			return new Highscore(levelNumber);
 		}
 		return h.clone();
 	}
 
 	/**
 	 * Sets the specified Highscore and stores it to the server.
 	 */
 	public void setHighscore(Highscore highscore) {
 		if (myUserInfo == null) {
 			// return if user is not logged in
 			return;
 		}
 
 		// Find the new moves of the current user in high score and store it
 		if (highscore.getFirstEntry().getName().equals(myUserInfo.getUserName())) {
 			Highscore h = getHighscoreForLevel(highscore.getLevelNumber());
 			h.addHighscoreEntry(highscore.getFirstEntry());
 			highscores.put(highscore.getLevelNumber(), h);
 
 			setToMyInfo(highscore.getLevelNumber(), highscore.getFirstEntry().getMoves());
 			// Set the new score and save
 			myUserInfo.setScore(newScore());
 			myUserInfo.store();
 			
 		} else if (highscore.getSecondEntry().getName().equals(myUserInfo.getUserName())) {
 			Highscore h = getHighscoreForLevel(highscore.getLevelNumber());
 			h.addHighscoreEntry(highscore.getSecondEntry());
 			highscores.put(highscore.getLevelNumber(), h);
 
 			setToMyInfo(highscore.getLevelNumber(), highscore.getSecondEntry().getMoves());
 			// Set the new score and save
 			myUserInfo.setScore(newScore());
 			myUserInfo.store();
 			
 		} else if (highscore.getThirdEntry().getName().equals(myUserInfo.getUserName())) {
 			Highscore h = getHighscoreForLevel(highscore.getLevelNumber());
 			h.addHighscoreEntry(highscore.getThirdEntry());
 			highscores.put(highscore.getLevelNumber(), h);
 
 			setToMyInfo(highscore.getLevelNumber(), highscore.getThirdEntry().getMoves());
 			// Set the new score and save
 			myUserInfo.setScore(newScore());
 			myUserInfo.store();
 		}
 
 		// other users can't be stored --> do nothing
 	}
 
 	/**
 	 * Gets the top UserInfos and initializes the level move map.
 	 */
 	@SuppressWarnings("unchecked")
 	private void initHighscoresFromUserInfos() {
 		highscores = new HashMap<Integer, Highscore>();
 
 		// Get the top UserInfos: The maximum would be 750 (3 places in 250
 		// levels), but it is very unlikely that there are 750 distinct players
 		// in the highscore of all 250 levels! So we take the last 200.
 		List<UserInfo> userInfos = UserInfo.getTop(200);
 
 		// Get the current top score
 		if (!userInfos.isEmpty()) {
 			topScore = userInfos.get(0).getScore();
 		}
 
 		for (UserInfo userInfo : userInfos) {
 			Map<Integer, Integer> userLevelMoves = retrieveLevelMoves(userInfo);
 			for (Map.Entry<Integer, Integer> levelMove : userLevelMoves.entrySet()) {
 				int level = levelMove.getKey();
 				int moves = levelMove.getValue();
 				Highscore h = highscores.get(level);
 				if (h == null) {
 					h = new Highscore(level);
 					highscores.put(level, h);
 				}
 				h.addHighscoreEntry(userInfo.getUserName(), moves);
 			}
 		}
 	}
 
 	/**
 	 * Retrieves the moves per level from the specified UserInfo. Only
 	 * entries with moves > 0 are added.
 	 * 
 	 * @param userInfo
 	 * @return A map with the level number as a key and the moves
 	 *         as a value.
 	 */
 	private Map<Integer, Integer> retrieveLevelMoves(UserInfo userInfo) {
 		Map<Integer, Integer> result = new HashMap<Integer, Integer>();
 		
 		// To transform legacy highscore entries
 		// TODO: Remove this
 		if (userInfo.getScore() < 61) {
 			result = legacyRetrieveHighscores(userInfo);
 
 			// if it's the current user --> save in the new format for later
 			if (userInfo.getUserName().equals(myUserInfo.getUserName())) {
 				for (Map.Entry<Integer, Integer> entry : result.entrySet()) {
 					setToMyInfo(entry.getKey(), entry.getValue());
 				}
 				// Set the new score and save
 				myUserInfo.setScore(newScore());
 				myUserInfo.store();
 			}
 			
 			return result;
 		}
 		
 		// process the 5 stored Strings (each containing max 50 levels)
 		for (int i = 0; i < 5; i++) {
 			String encodedMoves = userInfo.getString(i);
 			int[] decodedMoves = decode(encodedMoves);
 	
 			for (int j = 0; j < decodedMoves.length; j++) {
 				if (decodedMoves[j] > 0) {
 					int currentLevel = i * 50 + j + 1;
 					result.put(currentLevel, decodedMoves[j]);
 				}
 			}
 		}
 		
 		return result;
 	}
 	
 	/**
 	 * Sets the number of moves for the given level to the current users
 	 * UserInfo.
 	 * 
 	 * @param levelNumber
 	 *            level number between 1 and 250
 	 * @param moves
 	 *            number of moves, between 1 and 65535. 0 moves means that there
 	 *            is no highscore for that level.
 	 */
 	private void setToMyInfo(int levelNumber, int moves) {
 		if (levelNumber < 1 || levelNumber > 250) {
 			return;
 		}
 
 		int userInfoIndex = (levelNumber - 1) / 50;
 		int charIndex = (levelNumber - 1) % 50;
 
 		// get the relevant user info string and decode it
 		int[] userInfoInts = decode(myUserInfo.getString(userInfoIndex));
 		// set the moves at the specified index
 		userInfoInts[charIndex] = (char) moves;
 
 		// encode again and set to UserInfo
 		myUserInfo.setString(userInfoIndex, encode(userInfoInts));
 	}
 
 	/**
 	 * Decodes the specified string into an array of exactly 50 integers. Each
 	 * integer is between 0 and 65535. 0 means that there is no move.
 	 * 
 	 * @param encoded
 	 *            The encoded String.
 	 * @return an array of 50 integers between 0 and 65535.
 	 */
 	private static int[] decode(String encoded) {
 		int[] dec = new int[50];
 		Arrays.fill(dec, 0);
 
 		for (int i = 0; i < encoded.length() && i < 50; i++) {
 			dec[i] = (int) encoded.charAt(i);
 		}
 		return dec;
 	}
 	
 	/**
 	 * Encodes the array of integers into a String with length 50. Each int is
 	 * saved as a single char and must therefore be between 0 and 65535.
 	 * 
 	 * @param numbers
 	 *            an array of integers (maximum 50) between 0 and 65535
 	 * @return the encoded String with length 50
 	 */
 	private static String encode(int[] numbers) {
 		StringBuffer buf = new StringBuffer();
 		if (numbers != null) {
 			for (int i = 0; i < numbers.length && i < 50; i++) {
 				int number = Math.min(65535, numbers[i]); // maximum of 65535
 				number = Math.max(0, number); // minimum of 0
 				buf.append((char) number);
 			}
 		}
 		while (buf.length() < 50) {
 			buf.append((char) 0);
 		}
 		return buf.toString();
 	}
 	
 	/**
 	 * Creates a new score for the current user. It takes the current highest
 	 * score and adds 1. This way we can always be shure that the top 750
 	 * (3*250) players are all possible players in any of the highscores.
 	 */
 	private int newScore() {
 		return topScore + 1;
 	}
 
 	/**
 	 * Handles legacy user info and saves the current user info in the new
 	 * format.
 	 * 
 	 * @param userInfo
 	 * @deprecated
 	 */
 	private Map<Integer, Integer> legacyRetrieveHighscores(UserInfo userInfo) {
 		Map<Integer, Integer> result = new HashMap<Integer, Integer>();
 		
 		int currentLevel = 1;
 	
 		// process stored integers
 		for (int i = 0; i < 10; i++) {
 			// decode the moves for next three levels
 			int encodedMoves = userInfo.getInt(i);
 			int[] decodedMoves = legacyDecodeInt(encodedMoves);
 	
 			for (int moves : decodedMoves) {
 				if (moves > 0) {
 					if (moves == 999) {
 						// change legacy moves of 999 to 9999
 						result.put(currentLevel, 9999);
 					} else {
 						result.put(currentLevel, moves);
 					}
 				}
 				currentLevel++;
 			}
 		}
 	
 		// process stored Strings
 		for (int i = 0; i < 5; i++) {
 			String encodedMoves = userInfo.getString(i);
 			int[] decodedMoves = legacyDecodeString(encodedMoves);
 	
 			for (int moves : decodedMoves) {
 				if (moves > 0) {
 					if (moves == 999) {
 						// change legacy moves of 999 to 9999
 						result.put(currentLevel, 9999);
 					} else {
 						result.put(currentLevel, moves);
 					}
 				}
 				currentLevel++;
 			}
 		}
 		
 		return result;
 	}
 
 	/**
 	 * Decodes the specified encoded integer into an array of exactly three
 	 * integers (each smaller or equal to 999).
 	 * @deprecated
 	 */
 	private int[] legacyDecodeInt(int encoded) {
 		int[] dec = new int[3];
 		dec[0] = encoded / 1000000 % 1000;
 		dec[1] = (encoded / 1000) % 1000;
 		dec[2] = encoded % 1000;
 		return dec;
 	}
 
 	/**
 	 * Decodes the specified string into an array of exactly 15 integers. Each
 	 * integer is smaller or equal to 999.
 	 * @deprecated
 	 */
 	private int[] legacyDecodeString(String encoded) {
 		int[] dec = new int[15];
 		for (int i = 0; i < encoded.length() / 3 && i < 15; i++) {
 			try {
 				dec[i] = Integer.parseInt(encoded.substring(i * 3, i * 3 + 3));
 			} catch (NumberFormatException e) {
 				// do nothing --> int in the array will have 0 als value
 			}
 		}
 		return dec;
 	}
 }
