 package controller;
 
 import java.util.ArrayList;
 
 import model.Game;
 import model.Player;
 
 public class PlayerController {
 	private ArrayList<Player> players;
 	public static PlayerController instance;
 	
 	public static PlayerController getInstance(){
 		if (instance == null)
 			instance = new PlayerController();
 		return instance;
 	}
 	
 	private PlayerController() {
 		players = new ArrayList<Player>();
 	}
 	
 	private void addDefaultPlayers(ArrayList<Game> games) {
 		String[] names = new String[] { "Ruben", "Marcus", "Tim", "Stan",
 				"Eloy", "Stefan", "David", "Rob" };
 		
 		addPlayers(names);
 	}
 	
 	public void addPlayer(Player p) {
 		players.add(p);
 	}
 	
 	private void addPlayers(String[] peopleNames) {
 		for (String name : peopleNames)
 			addPlayer(new Player(name, GameController.getInstance()
 					.getGames()));
 	}
 	
 	private Player findPlayer(String name) {
 		for (Player p : players)
 			if (p.getName().toLowerCase().equals(name.toLowerCase())) return p;
 		
 		return null;
 	}
 	
 	public int getCountActivePlayers() {
 		int active = 0;
 		for (Player player : players)
 			if (player.isActive())
 				active++;
 		return active;
 	}
 	
 	public int getCountPlayers() {
 		return players.size();
 	}
 	
 	// number of rounds played
 	public int getPlayed(String name) {
 		Player p = findPlayer(name);
 		if (p == null) return 0; // TODO: make this
 		return 1;
 	}
 	
 	public ArrayList<Player> getPlayers() {
 		return players;
 	}
 	
 	public int getRank(String name) {
 		Player p = findPlayer(name);
 		// TODO: return rank (place in list) of player
 		if (p == null) return 0;
 		return 1;
 	}
 	
	public boolean getWon(String name) {
 		Player p = findPlayer(name);
 		// TODO: Make this work
		if (p == null) return false;
		return true;
 	}
 	
 	public void setGames(ArrayList<Game> games) {
 		addDefaultPlayers(games);
 	}
 	
 }
