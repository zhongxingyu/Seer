 package uk.co.thomasc.wordmaster;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.util.Date;
 
 import org.json.simple.JSONArray;
 import org.json.simple.JSONObject;
 import org.json.simple.parser.JSONParser;
 import org.json.simple.parser.ParseException;
 
 import uk.co.thomasc.wordmaster.objects.Game;
 import uk.co.thomasc.wordmaster.objects.Turn;
 import uk.co.thomasc.wordmaster.objects.User;
 
 public class ServerAPI {
 	
 	private static final String BASE_URL = "http://thomasc.co.uk/wm/";
 
 	/**
 	 * Calls the getMatches function of the server API. Retrieves
 	 * a list of all the games a player is involved in.
 	 * 
 	 * @param playerID – the Google+ ID of the player to retrieve games for
 	 * @param activityReference – a reference to the BaseGame activity, used to get avatars from Google+
 	 * @return an array of Game objects for each of the games the player is involved in
 	 */
 	public static Game[] getMatches(String playerID, BaseGame activityReference) {
 		JSONObject json = makeRequest("getMatches", playerID, null, null); 
 		boolean success = ((Boolean) json.get("success")).booleanValue();
 		if (success) {
 			JSONArray response = (JSONArray) json.get("response");
 			Game[] games = new Game[response.size()];
 			for (int i = 0; i < response.size(); i ++) {
 				JSONObject gameObject = (JSONObject) response.get(i);
 				String opponentID = gameObject.get("oppid").toString();
 				String gameID = gameObject.get("gameid").toString();
 				boolean needsWord = ((Boolean) gameObject.get("needword")).booleanValue();
 				int playerScore = ((Integer) gameObject.get("pscore")).intValue();
 				int opponentScore = ((Integer) gameObject.get("oscore")).intValue();
 				boolean playersTurn = ((Boolean) gameObject.get("turn")).booleanValue();
 				Game game = new Game(gameID, new User(playerID, activityReference), new User(opponentID, activityReference));
 				game.setPlayersTurn(playersTurn);
 				game.setNeedsWord(needsWord);
 				game.setScore(playerScore, opponentScore);
 				games[i] = game;
 			}
 			return games;
 		} else {
 			return null;
 		}
 	}
 	
 	/**
 	 * Calls the getTurns function on the server API. Returns an array
 	 * containing all the turns taken in the game.
 	 * 
 	 * @param gameID – the game ID of the game to retrieve turns from
 	 * @param activityReference – a reference to the BaseGame activity, used to get avatars from Google+
 	 * @return an array containing Turn objects for all turns taken in the game
 	 */
 	public static Turn[] getTurns(String gameID, BaseGame activityReference) {
 		JSONObject json = makeRequest("getTurns", gameID, null, null);
 		boolean success = ((Boolean) json.get("success")).booleanValue();
 		if (success) {
 			JSONArray response = (JSONArray) json.get("response");
 			Turn[] turns = new Turn[response.size()];
 			for (int i = 0; i < response.size(); i ++) {
 				JSONObject turnObject = (JSONObject) response.get(i);
 				String id = turnObject.get("turnid").toString();
 				String playerID = turnObject.get("playerid").toString();
 				String guess = turnObject.get("guess").toString();
 				long when = ((Long) turnObject.get("when")).longValue();
 				int correct = ((Integer) turnObject.get("correct")).intValue();
 				int displaced = ((Integer) turnObject.get("displaced")).intValue();
 				Turn turn = new Turn(id, new Date(when), new User(playerID, activityReference), guess, correct, displaced);
 				turns[i] = turn;
 			}
 			return turns;
 		} else {
 			return null;
 		}
 	}
 	
 	/**
 	 * Calls the getTurns function on the server API. Starting from a 'pivot'
 	 * turn, retrieves a given number of turns from before or after this point.
 	 * Returns an array containing the turns.
 	 * 
 	 * @param gameID – the game ID of the game to retrieve turns from
 	 * @param turnID – the turn ID of the 'pivot' turn
 	 * @param number – the number of turns to retrieve (negative for less recent, positive for more recent) 
 	 * @param activityReference – a reference to the BaseGame activity, used to get avatars from Google+
 	 * @return an array containing Turn objects for turns retrieved
 	 */
 	public static Turn[] getTurns(String gameID, String turnID, int number, BaseGame activityReference) {
 		JSONObject json = makeRequest("getTurns", gameID, turnID, Integer.toString(number));
 		boolean success = ((Boolean) json.get("success")).booleanValue();
 		if (success) {
 			JSONArray response = (JSONArray) json.get("response");
 			Turn[] turns = new Turn[response.size()];
 			for (int i = 0; i < response.size(); i ++) {
 				JSONObject turnObject = (JSONObject) response.get(i);
 				String id = turnObject.get("turnid").toString();
 				String playerID = turnObject.get("playerid").toString();
 				String guess = turnObject.get("guess").toString();
 				long when = ((Long) turnObject.get("when")).longValue();
 				int correct = ((Integer) turnObject.get("correct")).intValue();
 				int displaced = ((Integer) turnObject.get("displaced")).intValue();
 				Turn turn = new Turn(id, new Date(when), new User(playerID, activityReference), guess, correct, displaced);
 				turns[i] = turn;
 			}
 			return turns;
 		} else {
 			return null;
 		}
 	}
 	
 	/**
 	 * Calls the takeTurn function on the server API. Makes a guess for the player
 	 * and updates the state of the game on the server. Returns two booleans to
 	 * represent the outcome of the call.
 	 * 
 	 * @param playerID – the Google+ ID of the player taking the turn
 	 * @param gameID – the game ID of the game the turn is from
 	 * @param word – the guess the player has made
 	 * @return a 2-item array of booleans containing the server response: [success, validword]
 	 */
 	public static boolean[] takeTurn(String playerID, String gameID, String word) {
 		JSONObject json = makeRequest("takeTurn", playerID, gameID, word);
 		boolean success = ((Boolean) json.get("success")).booleanValue();
		JSONObject response = (JSONObject) json.get("response");
		boolean validWord = ((Boolean) response.get("validword")).booleanValue();
 		boolean[] result = {success, validWord};
 		return result;
 	}
 	
 	/**
 	 * Calls the createGame function of the server API. Creates a new game
 	 * involving the given players. If no opponent is specified, the player
 	 * is put into the automatch pool.
 	 * 
 	 * @param playerID – the Google+ ID of the user
 	 * @param opponentID – the Google+ ID of the opponent (null for automatch)
 	 * @param activityReference – reference to the BaseGame activity, used to get avatars from Google+
 	 * @return a Game object representing the game which was created
 	 */
 	public static Game createGame(String playerID, String opponentID, BaseGame activityReference) {
 		JSONObject json = makeRequest("createGame", playerID, opponentID, null);
 		boolean success = ((Boolean) json.get("success")).booleanValue();
 		if (success) {
 			JSONArray response = (JSONArray) json.get("response");
 			JSONObject gameObject = (JSONObject) response.get(0);
 			String gameID = gameObject.get("gameid").toString();
 			Game game = new Game(gameID, new User(playerID, activityReference), new User(opponentID, activityReference));
 			return game;
 		} else {
 			return null;
 		}
 	}
 	
 	/**
 	 * Calls the setWord function of the server API. If no word has been set
 	 * for the given player in the given game, their word is updated. Returns
 	 * two booleans representing the response from the server.
 	 * 
 	 * @param playerID – the Google+ ID of the player whose word is being set
 	 * @param gameID – the game ID of the game the player is involved in
 	 * @param word – the word the player wishes to use
 	 * @return 
 	 */
 	public static boolean[] setWord(String playerID, String gameID, String word) {
 		JSONObject json = makeRequest("setWord", playerID, gameID, word);
 		boolean success = ((Boolean) json.get("success")).booleanValue();
		JSONObject response = (JSONObject) json.get("response");
		boolean validWord = ((Boolean) response.get("validword")).booleanValue();
 		boolean[] result = {success, validWord};
 		return result;
 	}
 	
 	private static JSONObject makeRequest(String iface, String param1, String param2, String param3) {
 		String url = BASE_URL + iface + "/" + param1;
 		if (param2 != null && param2.length() > 0)
 			url += "/" + param2;
 		if (param3 != null && param3.length() > 0)
 			url += "/" + param3;
 		
 		String jsonText = "";
 		
 		try {
 			InputStream is = new URL(url).openStream();
 			BufferedReader reader = new BufferedReader(new InputStreamReader(is));
 			String inputLine;
 	        while ((inputLine = reader.readLine()) != null)
 	            jsonText += inputLine;
 	        is.close();
 	        
 	        JSONParser parser = new JSONParser();
 	        JSONObject jsonObject = (JSONObject) parser.parse(jsonText);
 	        return jsonObject;
 		} catch (IOException ex) {
 			return null;
 		} catch (ParseException ex) {
 			return null;
 		}
 	}
 	
 }
