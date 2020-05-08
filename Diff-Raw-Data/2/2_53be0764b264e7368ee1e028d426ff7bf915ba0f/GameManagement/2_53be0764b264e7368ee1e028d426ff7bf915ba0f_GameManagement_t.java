 package hsma.ss2011.vsy;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import org.apache.http.client.ClientProtocolException;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 public class GameManagement {
 	private RequestHandler reqHandler;
 	private String nick;
 	private String token;
 	private String gameID;
 	private String[] buzzwords;
 	private int size;
 	private String error;
 	
 	public GameManagement(String server, int port, String nick) throws MalformedURLException {
 		this.reqHandler = new RequestHandler(new URL("http", server, port, "/"));
 		this.nick = nick;
 		this.token = null;
 		this.gameID = null;
 		this.size = -1;
 		this.buzzwords = null;
 		this.error = null;
 	}
 	
 	public String getError() {
 		return error;
 	}
 	
 	public String[] getBuzzwords() {
 		return buzzwords;
 	}
 	
 	public int getSize() {
 		return size;
 	}
 	
 	public String getGameID() {
 		return gameID;
 	}
 
 	public void setGameID(String gameID) {
 		this.gameID = gameID;
 	}
 
 	/**
 	 * Request the current game sessions from the server.
 	 * @return The current game sessions or null if no exist.
 	 * @throws IOException 
 	 * @throws JSONException 
 	 * @throws ClientProtocolException 
 	 */
 	public GameSession[] currentGames() throws ClientProtocolException, JSONException, IOException {
 		GameSession[] gameSessions = null;
 		JSONArray response = null;
 		
 		response = reqHandler.getRequest("CurrentGames");
 		gameSessions = (response.length()>0) ? new GameSession[response.length()] : null;
 		
 		// Convert the JSONArray to a GameSession Array
		for (int i=0; i < response.length(); i++) {
 			JSONObject item = response.getJSONObject(i);
 			JSONArray participants = item.getJSONArray("participants");
 			
 			GameSession entry = new GameSession();
 			entry.setCreated(item.getInt("created"));
 			entry.setId(item.getString("id"));
 			entry.setName(item.getString("name"));
 			entry.setSize(item.getInt("size"));
 			entry.setWinner(item.getString("winner"));
 			
 			// extract the playernames from the participants JSONArray
 			String[] players = new String[participants.length()];
 			for (int j=0; j< players.length; j++)
 				players[j] = participants.getString(j);
 			entry.setParticipants(players);
 			
 			
 			gameSessions[i] = entry;
 			item = null; // just to make sure, that nothing happens by accident
 		}
 		
 		return gameSessions;
 	}
 
 	/**
 	 * Create a game at the server
 	 * @param name name of the game
 	 * @param size 3 to 6 are possible
 	 * @throws JSONException
 	 * @throws ClientProtocolException
 	 * @throws IOException
 	 */
 	public void createGame(String name, int size) throws JSONException, ClientProtocolException, IOException {
 		JSONObject request = new JSONObject();
 		JSONObject response = null;
 		
 		// Construct the request
 		request.put("token", this.token);
 		request.put("size", size);
 		request.put("name", name);
 		
 		response = reqHandler.postRequest("CreateGame", request);
 		
 		if (response.getBoolean("success")) {
 			this.size = size; // so now that the game is open, we save the size
 			this.gameID = response.getString("id");
 			
 			// Extract the buzzwords from the response
 			JSONArray w = response.getJSONArray("words");
 			this.buzzwords = new String[w.length()];
 			for (int i=0; i<buzzwords.length; i++)
 				this.buzzwords[i] = w.getString(i);
 		} else {
 			this.error = response.getString("error");
 		}
 	}
 	
 	/**
 	 * Register the player at the server
 	 * @throws JSONException 
 	 * @throws IOException 
 	 * @throws ClientProtocolException 
 	 */
 	public void registerPlayer() throws ClientProtocolException, IOException, JSONException {
 		JSONObject request = new JSONObject();
 		JSONObject response = null;
 		
 		request.put("nickname", this.nick);
 		response = reqHandler.postRequest("RegisterPlayer", request);
 		
 		/* Check if it worked, set the token else return the error message
 		 */
 		if (response.getBoolean("success"))
 			this.token = response.getString("token");
 		else
 			this.error = response.getString("error");
 	}
 	
 	/**
 	 * Join a game session
 	 * @param gameId id of the game to join
 	 * @throws JSONException
 	 * @throws ClientProtocolException
 	 * @throws IOException
 	 */
 	public void joinGame(String gameId) throws JSONException, ClientProtocolException, IOException {
 		JSONObject request = new JSONObject();
 		JSONObject response = null;
 		
 		// request
 		request.put("token", this.token);
 		request.put("id", gameId);
 		response = this.reqHandler.postRequest("JoinGame", request);
 		
 		if (response.getBoolean("success")) {
 			this.size = response.getInt("size");
 			this.gameID = gameId;
 			
 			// extract buzzwords
 			JSONArray w = response.getJSONArray("words");
 			this.buzzwords = new String[w.length()];
 			for (int i=0; i<buzzwords.length; i++)
 				this.buzzwords[i] = w.getString(i);
 		} else {
 			this.error = response.getString("error");
 		}
 	}
 	
 	/**
 	 * Leave the current game
 	 * @throws ClientProtocolException
 	 * @throws IOException
 	 * @throws JSONException
 	 */
 	public void leaveGame() throws ClientProtocolException, IOException, JSONException {
 		JSONObject request = new JSONObject();
 		JSONObject response = null;
 		
 		request.put("token", this.token);
 		request.put("id", this.gameID);
 		response = reqHandler.postRequest("LeaveGame", request);
 		
 		if (response.getBoolean("success"))
 			this.gameID = null;
 		else
 			this.error = response.getString("error");
 	}
 	
 	/**
 	 * Tell the server which field was marked
 	 * @param field the # of the field
 	 * @throws ClientProtocolException
 	 * @throws IOException
 	 * @throws JSONException
 	 */
 	public void makeMove(int field) throws ClientProtocolException, IOException, JSONException {
 		JSONObject request = new JSONObject();
 		JSONObject response = null;
 		
 		request.put("token", this.token);
 		request.put("id", this.gameID);
 		request.put("field", field);
 		response = reqHandler.postRequest("MakeMove", request);
 		
 		if (!response.getBoolean("success"))
 			this.error = response.getString("error");
 	}
 	
 	/**
 	 * Check if someone has won this game
 	 * @return The name of the winner or null if there's no winner yet.
 	 * @throws ClientProtocolException
 	 * @throws IOException
 	 * @throws JSONException
 	 */
 	public String checkWinner() throws ClientProtocolException, IOException, JSONException {
 		JSONObject request = new JSONObject();
 		JSONObject response = null;
 		String winner = null;
 		
 		request.put("token", this.token);
 		request.put("id", this.gameID);
 		response = reqHandler.postRequest("CheckWinner", request);
 		
 		if (response.getBoolean("success"))
 			winner = response.getString("winner");
 		else
 			this.error = response.getString("error");
 		
 		return winner;
 	}
 }
