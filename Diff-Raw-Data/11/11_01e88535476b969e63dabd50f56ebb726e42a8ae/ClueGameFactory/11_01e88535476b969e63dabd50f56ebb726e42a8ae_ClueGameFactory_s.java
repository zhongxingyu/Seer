 package main;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import service.ClueSessionService;
 
 /**
  * @author matt
  * 
  */
public class ClueGameFactory {
 	private static final Map<String, ClueSessionService> GAMES = new HashMap<String, ClueSessionService>();
 
 	public static ClueSessionService getGameForUser(final String name) {
 		ClueSessionService game = GAMES.get(name);
 		if (game == null) {
 			game = new ClueSessionService();
 			GAMES.put(name, game);
 		}
 		return game;
 	}
 
 	public static void resetGameForUser(final String name) {
 		GAMES.put(name, null);
 	}
 
 	private ClueGameFactory() {
 	}
 }
