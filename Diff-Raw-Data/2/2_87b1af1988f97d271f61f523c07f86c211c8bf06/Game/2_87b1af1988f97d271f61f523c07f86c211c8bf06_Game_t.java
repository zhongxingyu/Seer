 package models;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import play.Logger;
 
 public class Game {
 	private final static String[] CHARS = { "chara0", "chara1", "chara2",
 			"chara3", "chara4", "chara5", "chara6", "chara7" };
 
 	private final static Map<String, Integer[]> POSITIONS = new HashMap<String, Integer[]>();
 	static{
 		POSITIONS.put( "chara0", new Integer [] {32,32});
 		POSITIONS.put( "chara1", new Integer [] {64,32});
 		POSITIONS.put( "chara2", new Integer [] {32,64});
 		POSITIONS.put( "chara3", new Integer [] {64,64});
 		POSITIONS.put( "chara4", new Integer [] {32,48});
 		POSITIONS.put( "chara5", new Integer [] {48,32});
 		POSITIONS.put( "chara6", new Integer [] {48,48});
 		POSITIONS.put( "chara7", new Integer [] {0,0});
 	}
 	private Map<String, String> playerMapping =  new HashMap<String,String>();
 	private List<Client> players = new ArrayList<Client>();
 	private int charCounter = 0;
 
 	public Game() {
 		Logger.info("starting new Game!");
 	}
 
 	public String getNextPlayerFor(String username, String ip) {
 		if(playerMapping.containsKey(ip)){
 			return playerMapping.get(ip);
 		}
 		if (!playerMapping.containsKey(username) && charCounter < CHARS.length) {
 			String player = CHARS[charCounter];
 			Logger.info("Player " + username + " uses Char " + player);
 			charCounter++;
 			players.add(new Client(player, username, POSITIONS.get(player)));
			playerMapping.put(ip, player);
 			return player;
 		}
 		return null;
 	}
 
 	public String getPlayerName(String ip){
 		return playerMapping.get(ip);
 	}
 	
 	public List<Client> getPlayers() {
 		return players;
 	}
 }
