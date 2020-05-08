 package controller;
 
 import java.util.ArrayList;
 import java.util.Random;
 
 import model.Game;
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
 	
 	private Player[][] distribute(ArrayList<Player> remaining) {
 		ArrayList<Player> teamA = new ArrayList<Player>();
 		ArrayList<Player> teamB = new ArrayList<Player>();
 		return distribute(remaining, teamA, teamB);
 	}
 	
 	private Player[][] distribute(ArrayList<Player> remaining,
 			ArrayList<Player> teamA, ArrayList<Player> teamB) {
 		if (remaining.size() == 0) {
 			Player[][] current = new Player[][] {teamA.toArray(new Player[0]), teamB.toArray(new Player[0])};
 			return current;
 		} else
 			return distribute(remaining, teamA, teamB);
 	}
 	
 	public int getCountRounds() {
 		return rounds.size() > 0 ? 1 : rounds.size();
 	}
 	
 	public Game getCurrentGame() {
 		return getLatest().getPlaying();
 	}
 	
 	public Player[] getCurrentTeamA() {
 		return getLatest().getTeamA();
 	}
 	
 	public Player[] getCurrentTeamB() {
 		return getLatest().getTeamB();
 	}
 	
 	public Round getLatest() {
 		if (rounds.size() > 0) return rounds.get(rounds.size() - 1);
 		else
			return null;
 	}
 	
 	public String getLeader() {
 		Player highest = PlayerController.getInstance().getPlayers().get(0);
 		for (Player candidate : PlayerController.getInstance().getPlayers())
 			if (candidate.getScore() > highest.getScore()) highest = candidate;
 		return highest.getName();
 	}
 	
 	public void newGame() {
 		// TODO: Make this work
 		// Random games
 		// Random (fair teams)
 		// rounds.add(new Round(g));
 		GameController c = GameController.getInstance();
 		Random r = new Random();
 		Game g = c.getGames().get(r.nextInt(c.getGames().size()));
 		// Add to team A
 		// Add to team B until > A
 		// repeat
 	}
 	
 	public void setTeamAWin(boolean b) {
 		if (rounds.size() > 0) {
 			Round last = rounds.get(rounds.size() -1);
 			if (b) last.setTeamAWin();
 			else
 				last.setTeamBWin();
 		}
 	}
 }
