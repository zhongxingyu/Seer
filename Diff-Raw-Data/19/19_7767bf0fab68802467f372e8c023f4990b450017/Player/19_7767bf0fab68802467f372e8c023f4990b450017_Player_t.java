 package server;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 public class Player {
 	
 	private int gameId;
 	private final int auth;
 	private int id;
 	private String name;
 
 	public Player(int pCount, int gId) {
 		this.auth = (int) Math.floor(Math.random()*1000000); //Random 6 digit number used as an auth for clients
 		this.setId(pCount);
 		this.gameId = gId;
 	}
 	
 	
 	
 	public static Player getPlayerById(int i){
 		//TODO
 		return null;
 	}
 
 
 	/**
 	 * @return the name
 	 */
 	public String getName() {
 		return name;
 	}
 
 
 	/**
 	 * @param name the name to set
 	 */
 	public void setName(String name) {
 		this.name = name;
 	}
 
 
 	/**
 	 * @return the auth
 	 */
 	public int getAuth() {
 		return auth;
 	}
 
 
 	/**
 	 * @return the id
 	 */
 	public int getId() {
 		return id;
 	}
 	
 	/**
 	 * @return the gameId
 	 */
 	public int getGameId() {
 		return gameId;
 	}
 
 
 	/**
 	 * @param id the id to set
 	 */
 	public void setId(int id) {
 		this.id = id;
 	}
 	
 	public JSONObject toJSON(){
 		try {
			return new JSONObject("{\"ID\":"+id+",\"AUTH\":"+auth+",\"GAMEID\":"+gameId+"}");
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 
 }
