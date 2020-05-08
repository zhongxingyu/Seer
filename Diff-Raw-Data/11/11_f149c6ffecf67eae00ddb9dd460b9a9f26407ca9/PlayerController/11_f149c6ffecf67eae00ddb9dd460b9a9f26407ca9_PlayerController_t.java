 package controller;
 
 import java.util.ArrayList;
 import java.util.Collections;
 
 import view.GameView;
 
 import model.Player;
 import model.Word;
 
 public class PlayerController {
 	private ArrayList<Player> players = null;
 	private Player currentPlayer = null;
 	private static PlayerController instance;
 	
 	private PlayerController() {}
 	
 	public static PlayerController instance() {
 		if (instance == null) instance = new PlayerController();
 		return instance;
 	}
 	
 	public void setNumberOfPlayers(int numberOfPlayers) {
 		players = new ArrayList<Player>();
 		if (numberOfPlayers <= 0) return;
 		for (int i = 0; i < numberOfPlayers; i++) {
 			players.add(new Player("Player " + (i + 1)));
 		}
 		currentPlayer = players.get(0);
 	}
 	
 	public int getScoreForCurrentPlayer() {
 		if (currentPlayer == null) return -1;
 		return currentPlayer.getScore();
 	}
 	
 	
 	public String getNameOfCurrentPlayer() {
 		return currentPlayer.getName();
 	}
 	
 	/**
 	 * Changes the current player to the next one in line
 	 * @return true if round has ended
 	 */
 	public boolean nextPlayer() {
 		boolean newRound = false;
 		int index = players.indexOf(currentPlayer);
 		if (index >= players.size() - 1) {
 			index = 0;
 			newRound = true;
 		}
 		else index++;
 		
 		currentPlayer = players.get(index);
 		
 		return newRound;
 	}
 	
 	public ArrayList<Player> getPlayersSortedByScore() {
		ArrayList<Player> out = new ArrayList<Player>();
		for (Player player: players) {
			out.add(player);
		}
 		Collections.sort(out);
 		return out;
 	}
 	
 	public ArrayList<Player> getPlayers() {
 		return players;
 	}
 
 	public void addWordToCurrentPlayer(Word word) {
 		currentPlayer.incrementScore(word.getWordScore());
 		currentPlayer.addWord(word);
 	}
 }
