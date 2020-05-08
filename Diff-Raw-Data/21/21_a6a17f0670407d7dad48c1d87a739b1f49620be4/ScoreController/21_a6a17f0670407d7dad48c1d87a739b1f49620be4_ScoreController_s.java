 package controller;
 
 import java.util.ArrayList;
 
 import model.Player;
 import model.Round;
 
 public class ScoreController {
 	private ArrayList<Round> rounds;
	private ArrayList<Player> players;
 	
 	private static ScoreController instance;
 	
 	public static ScoreController getInstance() {
 		if (instance == null)
			instance = new ScoreController(PlayerController.getInstance().getPlayers());
 		return instance;
 	}
 	
	private ScoreController(ArrayList<Player> playerList) {
 		rounds = new ArrayList<Round>();
		players = playerList;
 	}
 	
 	public int getCountRounds() {
 		return rounds.size() + 1;
 	}
 	
 	public String getLeader() {
 		Player highest = players.get(0);
 		for (Player candidate : players)
 			if (candidate.getScore() > highest.getScore())
 				highest = candidate;
 		return highest.getName();
 	}
 	
 }
