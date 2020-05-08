 package it.chalmers.tendu.gamemodel;
 
 import it.chalmers.tendu.controllers.ModelController;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 public class LobbyModel {
 
 	public String hostMacAddress;
 	public Map<String, Integer> players;
 	public Map<Integer, Boolean> playerReady;
 	public int maxPlayers;
 
 	public LobbyModel(int maxPlayers) {
 		players = new HashMap<String, Integer>();
 		playerReady = new HashMap<Integer, Boolean>();
 		this.maxPlayers = maxPlayers;
 	}
 
 	public boolean isHost() {
 		if (Player.getInstance().getMac().equals(hostMacAddress)) {
 			return true;
 		}
 		return false;
 	}
 
 	public void addHost(String myMacAddress) {
 		this.hostMacAddress = myMacAddress;
 		addPlayer(myMacAddress);
 		
 	}
 
 	public void playerReady(String player, boolean ready) {
 		// TODO
 //		playerReady.put(player, ready);
 	}
 
 	public boolean allPlayersReady() {
 		boolean allReady = true;
 		for (Boolean rdy : playerReady.values()) {
 			if (!rdy)
 				allReady = false;
 		}
 		return allReady;
 	}
 
 	public void createGameSession() {
 		GameSession gameSession = new GameSession(players, hostMacAddress);
 		new ModelController(gameSession);
 	}
 
 	public Map<String, Integer> getLobbyMembers() {
 		return new HashMap<String, Integer>(players);
 	}
 
 	public void addPlayer(String macAddress) {
 		// connect mac id with player
 		players.put(macAddress, players.size());
 //		for (int i = 0; i < macAddress.size(); i++) {
 //			players.put(macAddress.get(i), (Integer) i);
 //		}
 	}
 	
 	public boolean isMaxPlayersConnected(){
 		return players.keySet().size() == maxPlayers;
 	}
 
 }
