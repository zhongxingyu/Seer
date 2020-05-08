 package drexel.edu.blackjack.server.game;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.logging.Logger;
 
 import drexel.edu.blackjack.db.game.FlatfileGameManager;
 import drexel.edu.blackjack.db.game.GameManagerInterface;
 import drexel.edu.blackjack.db.game.GameMetadata;
 import drexel.edu.blackjack.util.BlackjackLogger;
 
 /**
  * The active game coordinator handles the threads that
  * are involved in playing games that are active on the
  * server.
  * 
  * @author Jennifer
  *
  */
 public class ActiveGameCoordinator {
 
 	/******************************************************************************
 	 * Class variables go here
 	 *****************************************************************************/
 
 	// For the singleton pattern
 	private static ActiveGameCoordinator coordinator = null;
 	
 	// Need something to map games to the game-playing threads.
 	private Map<Game,GamePlayingThread> gameToThreadMap = null;
 	
 	// Need something else to map game IDs to the games.
 	private Map<String,Game> idToGameMap = null;
 	
 	// And a logger for errors
 	private final static Logger LOGGER = BlackjackLogger.createLogger(ActiveGameCoordinator.class.getName());
 	
 	/******************************************************************************
 	 * Constructor goes here
 	 *****************************************************************************/
 	private ActiveGameCoordinator() {
 		loadGames();
 	}
 
 	/******************************************************************************
 	 * Public methods go here
 	 *****************************************************************************/
 
 	/**
 	 * Following the game singleton pattern, we only have one of
 	 * these ever instantiated.
 	 * @return
 	 */
 	public static ActiveGameCoordinator getDefaultActiveGameCoordinator() {
 		if( coordinator == null ) {
 			coordinator = new ActiveGameCoordinator();
 		}
 		return coordinator;
 	}
 	
 	/**
 	 * Returns all the games that are currently loaded.
 	 * 
 	 * @return All the games currently loaded
 	 */
 	public Set<Game> getAllGames() {
 		if( this.gameToThreadMap == null ) {
 			loadGames();
 		}
 		
 		return gameToThreadMap.keySet();
 	}
 	
 	/**
 	 * Given an ID, return the active game corresponding to the
 	 * ID. If no game is actively loaded, return null.
 	 * 
 	 * @param id An identifier for the game
 	 * @return Either the game itself or, if it's not loaded,
 	 * a null
 	 */
 	public Game getGame( String id ) {
 		
 		// This shouldn't happen
		if( gameToThreadMap == null ) {
 			loadGames();
 		}
 				
		GamePlayingThread thread = gameToThreadMap.get( id );
		if( thread != null ) {
			return thread.getGame();
 		}
 		
 		// If we get here, we weren't playing the game anywhere, just return null
 		return null;
 	}
 
 	/**
 	 * Add a player to the game with the indicated sessionName.
 	 * 
 	 * If the sessionName doesn't correspond to any gamemetadata,
 	 * this is a problem.
 	 * 
 	 * Otherwise, look to see if a game is already active. If so,
 	 * make sure that it has room. If not, it's a problem. If it
 	 * does, add the player
 	 * 
 	 * If there is no active game, create one, add the user, and
 	 * start it.
 	 * 
 	 * @param sessionName Should correspond to some identifier in gamemetadata
 	 * @param user The user to add
 	 * @return IF the game was successfully joined, return that game object.
 	 * IF it wasn't, return a null
 	 */
 	public Game addPlayer(String sessionName, User user) {
 		
 		if( idToGameMap == null ) {
 			LOGGER.severe( "The idToGameMap in the ActiveGameController is null. It shouldn't be." );
 			return null;
 		}
 		
 		// Look up the game
 		Game game = idToGameMap.get(sessionName);
 		if( game == null ) {
 			LOGGER.severe( "No game corresponded to the request id of '" + sessionName + "'." );
 			return null;
 		}
 		
 		// Make sure there is room to add the player
 		if( game.stillHasRoom() ) {
 			game.addPlayer( user );
 			return game;
 		} else {
			LOGGER.severe( "Game has no more room. Race condtion?" );
 			return null;
 		}
 	}
 
 	/******************************************************************************
 	 * Private methods go here
 	 *****************************************************************************/
 
 	/**
 	 * Loads the game metadata from the game manager interface.
 	 * Create a game for each game metadata object. Since they
 	 * initially aren't running, they all point to null threads.
 	 */
 	private void loadGames() {
 		GameManagerInterface gameManager = FlatfileGameManager.getDefaultGameManager();
 		if( gameManager == null ) {
 			LOGGER.severe( "Could not load the games in our game manager, the game manager was null." );
 		} else {
 			
 			// Initialize our maps
 			gameToThreadMap = new HashMap<Game,GamePlayingThread>();
 			idToGameMap = new HashMap<String,Game>();
 
 			// Can only add things from non-null game metadata list
 			List<GameMetadata> gameMetadatas = gameManager.getGames();
 			if( gameMetadatas != null ) {
 				for( GameMetadata metadata : gameMetadatas  ) {
 					Game game = new Game( metadata );
 					gameToThreadMap.put( game, null );
 					idToGameMap.put( game.getId(), game );
 				}
 			}
 		}
 	}
 }
