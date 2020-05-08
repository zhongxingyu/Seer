 package server;
 
 import game.GenGame;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 public class Game {
 	
 	
 	
 	public volatile boolean running = false;
 	public Player[] players = new Player[GameManager.MAX_PLAYERS];
 	private AtomicInteger pCount = new AtomicInteger(0);
 	private GenGame gameHook;
 	
 
 	public Game(GenGame game) {
 		for(int i=0;i<GameManager.MAX_PLAYERS;i++){
 			players[i]=null;
 		}
 		gameHook = game;
 	}
 	
 	public Game(Player players[]) throws PlayerException {
 		if(players.length<=this.players.length){
 			this.players = players;
 		}else{
 			throw new PlayerException("Player array too large");
 		}
 	}
 
 
 	/**
 	 * @return the players
 	 */
 	public Player getPlayerById(int id) {
 		for(int i = 0;i<players.length;i++){
 			if(players[i].getId() == id) return players[i];
 		}
 		return null;
 	}
 
 	/**
 	 * @param players the players to set
 	 */
 	public int addPlayer(int gId) {
 		if(pCount.get()<this.players.length){
 			this.players[pCount.get()] = new Player(pCount.get(),gId);
 			pCount.addAndGet(1);
 			return pCount.get()-1;
 		}else{
 			return -1;
 		}
 	}
 	
 	
 	class PlayerException extends Exception {
 		private static final long serialVersionUID = 0L;
 
 		public PlayerException(String msg){
 			super(msg);
 		}
 	}
 
 
 	public JSONObject connectPlayer(int gId) {
 		int me = addPlayer(gId);
 		while(pCount.get()!=GameManager.MAX_PLAYERS){
 			try {
 				Thread.sleep(100);
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		}
		return players[me].toJSON();
 	}
 
 	public JSONObject getStatus() {
 		return gameHook.getStatus();
 	}
 	
 	public AtomicInteger getNumPlayers(){
 		return pCount;
 	}
 	
 	public JSONObject doCommand(JSONObject command) throws JSONException{
 		int id = Integer.parseInt(command.get("ID").toString());
 		int auth = Integer.parseInt(command.get("AUTH").toString());
 		
 		if(getPlayerById(id).getAuth() == auth){
 			return gameHook.doCommand(command);
 		}else{
 			return new JSONObject("{\"ERROR\":4}"); //error 4 means id and auth don't match
 		}
 	}
 
 }
