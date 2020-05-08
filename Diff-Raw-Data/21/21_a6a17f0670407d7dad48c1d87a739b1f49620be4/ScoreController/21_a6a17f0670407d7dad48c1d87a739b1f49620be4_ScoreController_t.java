 package controller;
 
 import java.util.ArrayList;
 
 import model.Player;
 import model.Round;
 
 public class ScoreController {
 	private ArrayList<Round> rounds;
 	
 	private static ScoreController instance;
 	
 	public static ScoreController getInstance() {
 		if (instance == null)
 instance = new ScoreController();
 		return instance;
 	}
 	
	private ScoreController() {
 		rounds = new ArrayList<Round>();
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
