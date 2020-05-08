 package com.github.joakimpersson.tda367.model;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import com.github.joakimpersson.tda367.model.constants.Parameters;
 import com.github.joakimpersson.tda367.model.player.Player;
 import com.github.joakimpersson.tda367.model.utils.FileScanner;
 
 public class Highscore {
 
 	private String fileName;
 	private List<Score> playerList;
 	private final int maxSize;
 
 	/**
 	 * Create a new HighScore object responsible for managing the games score
 	 * objects
 	 */
 	public Highscore() {
 		playerList = new ArrayList<Score>();
 		fileName = "highScore.data";
 		maxSize = Parameters.INSTANCE.getHighscoreMaxSize();
 		loadList();
 	}
 
 	/**
 	 * Update the current HighScorelist
 	 * 
 	 * @param players
 	 *            A list of the players that have completed a game
 	 */
 	public void update(List<Player> players) {
 		for (Player p : players) {
 			Score score = new Score(p.getName(), p.getPoints());
 			playerList.add(score);
 		}
 
 		this.sortList();
 		this.trimeHighScoreList();
 		this.saveList();
 	}
 
 	/**
 	 * Sorts the list of score objects in descending order
 	 */
 	private void sortList() {
 
 		Collections.sort(playerList);
 		// Reverse the order, since it sorts ascending
 		Collections.reverse(playerList);
 
 	}
 
 	/**
 	 * Responsible for score objects from the list if they don't qualify under
 	 * the limit of Score objects
 	 */
 	private void trimeHighScoreList() {
 		int index = playerList.size() - 1;
 		while (playerList.size() > maxSize) {
 			playerList.remove(index);
 			index--;
 		}
 
 	}
 
 	private void saveList() {
 		FileScanner.writeOjbect(fileName, playerList);
 	}
 
 	@SuppressWarnings("unchecked")
 	private void loadList() {
 		Object object = FileScanner.readObject(fileName);
		if (object != null) {
			playerList = (List<Score>) object;
		}// otherwise it should overwrite itself
 	}
 
 	/**
 	 * Get the HighScore list
 	 * 
 	 * @return The list of score objects in descending order
 	 */
 	public List<Score> getList() {
 		this.loadList();
 		return playerList;
 	}
 
 	/**
 	 * Get the size of the HighScore list
 	 * 
 	 * @return The number of score objects in the list
 	 */
 	public int getSize() {
 		return this.playerList.size();
 	}
 
 	/**
 	 * Reset the HighScorelist and remove all previous records from the game
 	 */
 	public void reset() {
 		playerList.clear();
 		saveList();
 	}
 }
