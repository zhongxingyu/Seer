 package drexel.edu.blackjack.server.game;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.logging.Logger;
 
 import drexel.edu.blackjack.db.user.UserMetadata;
 import drexel.edu.blackjack.server.BlackjackProtocol;
 import drexel.edu.blackjack.server.ResponseCode;
 import drexel.edu.blackjack.util.BlackjackLogger;
 
 /**
  * Used to pass game state information from
  * the server to client, to show what bets
  * are made, what cards are dealt, what users
  * are joining and leaving, etc.
  * 
  * See Section 2.15 of the protocol design
  * for details.
  * 
  * @author Jennifer
  *
  */
 public class GameState {
 
 	// And a logger for errors
 	private final static Logger LOGGER = BlackjackLogger.createLogger(GameState.class.getName()); 
 
 	protected enum STATUS {
 		ACTIVE,		// The user is an active participant
 		OBSERVER	// The user is in the session, but as an observer
 	}
 	
 	// These are for the various gamestages
 	public static final String STARTED_KEYWORD		= "STARTED";
 	public static final String NOT_STARTED_KEYWORD	= "NOT_STARTED";
 	
 	// An ordered list of players involved in the game. Gameplay will
 	// occur in this order, amongst the ACTIVE users. 
 	private List<User> players 		= null;
 	
 	// The current player in terms of play
 	private User currentPlayer		= null;
 	
 	// Every game has an identifier
 	private String gameId			= null;
 
 	
 	/*********************************************************************
 	 * Constructor goes here
 	 ********************************************************************/
 	
 	/**
 	 * Construct a game state object for the given game
 	 * ID.
 	 * 
 	 * @param gameId A game's unique identifier to be used in
 	 * constructed messages
 	 */
 	public GameState( String gameId ) {
 		this.gameId = gameId;
 		players = Collections.synchronizedList(new ArrayList<User>());
 	}
 
 	
 	/*********************************************************************
 	 * Notification methods. These are used to alert others in the game
 	 * of some sort of player action.
 	 ********************************************************************/
 
 	
 	/**
 	 * Need to send out messages to the remaining players (those who are
 	 * not GONE status, or the player themselves) and notify them about
 	 * a player leaving the game.
 	 * 
 	 * This is a ResponseCode.CODE.PLAYER_LEFT code, and the parameters
 	 * are the gameId followed by the userId 
 	 *
 	 * @param departedPlayer Who left
 	 * @return True if notifications were sent successfully, false
 	 * if there were problems
 	 */
 	public boolean notifyOthersOfDepartedPlayer(User departedPlayer) {
 		
 		boolean success = false;
 		
 		// It's all bad if they don't specify who joined
 		if( departedPlayer != null ) {
 			// Create the response code
 			ResponseCode code = new ResponseCode( ResponseCode.CODE.PLAYER_LEFT, getStringForGameAndUser(departedPlayer) );
 
 			// Then send it to all the players except the one who generated
 			success = notifyOtherPlayers( code, departedPlayer );		
 		}
 
 		return success;
 	}
 	
 	/**
 	 * Need to send out messages to the other players (if any) about
 	 * how some new player has joined the session.
 	 * 
 	 * @param newPlayer Who just joined
 	 * @return True if notifications were sent successfully, false
 	 * if there were problems
 	 */
 	public boolean notifyOthersOfJoinedPlayer( User newPlayer) {
 		
 		boolean success = false;
 		
 		// It's all bad if they don't specify who joined
 		if( newPlayer != null ) {
 			// Create the response code: gameid username
 			ResponseCode code = new ResponseCode( ResponseCode.CODE.PLAYER_JOINED, getStringForGameAndUser(newPlayer) );
 
 			// Then send it to all the players except the one who generated
 			success = notifyOtherPlayers( code, newPlayer );		
 		}
 
 		return success;
 	}	
 	
 	/**
 	 * Need to send out messages to the other players (if any) about
 	 * how a bet was placed
 	 * 
 	 * @param newPlayer Who just joined
 	 */
 	public boolean notifyOthersOfBetPlaced( User player, int bet ) {
 		
 		boolean success = false;
 		
 		// It's all bad if they don't specify who joined
 		if( player != null ) {
 			// Create the response code: gameid username bet
 			StringBuilder str = new StringBuilder( getStringForGameAndUser( player ) );
 			str.append( " " );
 			str.append( bet );
			ResponseCode code = new ResponseCode( ResponseCode.CODE.PLAYER_JOINED, getStringForGameAndUser(player) );
 
 			// Then send it to all the players except the one who generated
 			success = notifyOtherPlayers( code, player );		
 		}
 
 		return success;
 	}	
 
 	/**
 	 * Send a response code update to all other players except for
 	 * the one passed in (who might be null)
 	 * 
 	 * @param code What to send
 	 * @param player Who not to send it to
 	 * @return True if successfully sent, false othrewise
 	 */
 	private boolean notifyOtherPlayers(ResponseCode code, User player) {
 		
 		boolean success = true;
 		
 		// They can't be null, that's bad
 		if( players == null ) {
 			success = false;
 		} else {
 			
 			// And then send it to all the remaining players. We make a
 			// copy of them in a synchronized block to avoid deadlocking
 			User[] copy = getCopyOfPlayers();
 			
 			// If copy is non-null, we can send our messages
 			if( copy != null ) {
 				for( int i = 0; i < copy.length; i++ ) {
 					User user = ((User)copy[i]);
 					if( user != null && !user.hasSameUsername(player)) {
 						success = success && user.sendMessage( code );
 					}
 				}
 			}
 		}
 		
 		return success;
 	}
 
 
 	/**
 	 * A common pattern for these response codes starts with putting
 	 * the gameid and username up front. So make a convenient method
 	 * that computes this
 	 * 
 	 * @param player The 'user' part of the 'GameAndUser'
 	 * @return The string of the gameid and username, unless something
 	 * went wrong and then null
 	 */
 	private String getStringForGameAndUser( User player) {
 		// Start by creating a response code: gameId username
 		StringBuilder str = new StringBuilder( gameId );
 		str.append( " " );
 		UserMetadata metadata = (player == null ? null : player.getUserMetadata());
 		if( metadata == null || metadata.getUsername() == null ) {
 			str.append( "(unknown)" );
 		} else {
 			str.append( metadata.getUsername() );
 		}
 		return str.toString();
 	}
 	
 	/*********************************************************************
 	 * These deal with managing the players in the game session
 	 ********************************************************************/
 	
 	
 	/**
 	 * Adds a player to the tracking within the game state. Newly
 	 * added players are entered in the OBSERVER state, unless...
 	 * 
 	 * Note that it's a possible the player is already IN the list
 	 * of players, but in the GONE state. In this case, they should
 	 * be in the RETURNED state.
 	 */
 	synchronized public boolean addPlayer( User player ) {
 
 		// Assume that we'll fail
 		boolean status = false;
 		
 		if( player != null ) {		
 			// Add them to the list of players
 			status = players.add( player );
 			
 			// If that worked, note that they are an observer
 			if( status ) {
 				// Set the user's status to OBSERVER
 				player.setStatus( STATUS.OBSERVER );
 				
 				// If they're the first person in the game, though, it needs to be started
 				if( players.size() == 1 ) {
 					ActiveGameCoordinator.getDefaultActiveGameCoordinator().startGame( this.gameId );
 				}
 			}
 		}
 		
 		// Return the status
 		return status;
 	}
 
 	/**
 	 * Removes a player to the tracking within the game state.
 	 * If they've placed a bet, it'll be automatically forfeited
 	 * as it was already deducted from their account
 	 */
 	synchronized public boolean removePlayer( User player ) {
 		
 		// Assume that we'll fail
 		boolean status = false;
 		
 		if( player != null ) {		
 			// Remove them from the list of players
 			status = players.remove( player );
 		}
 		
 		// Return the status
 		return status;
 	}
 	
 	/**
 	 * Return a pointer to the current player
 	 * 
 	 * @return Who the current player is. May be null.
 	 */
 	synchronized public User getCurrentPlayer() {
 		return currentPlayer;
 	}
 
 	/** 
 	 * Returns the number of players, without regard to their
 	 * status as observer or active
 	 */
 	synchronized public int getNumberOfPlayers() {
 		return (players == null ? 0 : players.size() );
 	}
 	
 	/**
 	 * Looks at all the players who are active. If there are any
 	 * who don't have their bet set, they need to be idle-bumped
 	 */
 	public void removeActivePlayersWithNoBet() {
 		
 		// Grab the users
 		User[] users = getCopyOfPlayers();
 		
 		// If array is non-null, we can look and see if any users haven't bet
 		if( users != null ) {
 			for( int i = 0; i < users.length; i++ ) {
 				User user = ((User)users[i]);
 				
 				// If the user is active...
 				if( user != null && user.getStatus() != null && user.getStatus().equals(STATUS.ACTIVE) ) {
 					
 					// And they placed no bet
 					if( !user.hasSpecifiedBet() ) {
 						// Force them to idle timeout
 						user.forceTimeoutWhileBetting();
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * Cycles through the players. If it finds a player
 	 * who is active who does not have a bet value
 	 * set on the connection protocol, you have
 	 * outstanding bets that are being waited for
 	 * 
 	 * @return True if there is some active player
 	 * who has no bet specified
 	 */
 	public boolean arePlayersWithOutstandingBets() {
 	
 		User[] users = getCopyOfPlayers();
 		for( User user : users ) {
 			// If the user is active
 			if( user.getStatus() != null && user.getStatus().equals(STATUS.ACTIVE) ) {
 				// Return true if they don't have a bet set
 				if( !user.hasSpecifiedBet() ) {
 					return true;
 				}
 			}
 		}
 		
 		// If we got this far, we're okay
 		return false;
 	}
 	
 
 	
 	/*********************************************************************
 	 * These deal with altering the game state in response to activity
 	 * in the game
 	 ********************************************************************/
 
 	
 	/**
 	 * Indicates to the game state that a new round
 	 * is being started. A few things are done here:
 	 * 
 	 * <ol>
 	 * <li>All players are set to ACTIVE status
 	 * <li>The currentPlayer is set to null
 	 * <li>Requests for bids are made of all players
 	 * </ol>
 	 */
 	synchronized public void startNewRound() {
 		
 		// Need to have valid lists
 		if( players != null ) {
 			// Sets up all players active
 			makeAllPlayersActive();
 			
 			// Sets up all players active
 			removeAllPlayerBets();
 			
 			// Reset the starting player
 			currentPlayer = null;
 			
 			// Request bids from all players
 			for( User player : players ) {
 				ResponseCode code = new ResponseCode( ResponseCode.CODE.REQUEST_FOR_BET );
 				player.setProtocolState( BlackjackProtocol.STATE.IN_SESSION_AWAITING_BETS );
 				player.sendMessage(code);
 			}
 		}
 	}
 
 	/**
 	 * Advances the currentPlayer pointer to the next player
 	 * in the list with ACTIVE status. If there was no 
 	 * currentPlayer, it is the first User in the list
 	 * with active status. If there are no more subsequent
 	 * players with an ACTIVE status, it is set to null.
 	 */
 	synchronized public void advanceCurrentPlayer() {
 		
 		if( players == null ) {
 			// If there aren't players, the current player is null
 			currentPlayer = null;
 		} else if( currentPlayer == null ) {
 			// Otherwise we try to find the first active player (may be null!)
 			currentPlayer = getFirstActivePlayerFrom( 0 );
 		} else {
 			// Or else we find the current player's index
 			int index = players.indexOf( currentPlayer );
 			
 			// And look for someone active beyond them (may be null!)
 			if( index == -1 ) {
 				LOGGER.severe( "Could not advance current player as we could not find them in the list." );
 			} else {
 				currentPlayer = getFirstActivePlayerFrom( index+1 );
 			}
 		}
 	}
 	
 	
 	/*********************************************************************
 	 * Private methods go here
 	 ********************************************************************/
 
 	
 	/**
 	 * Walk through the players, and mark them all as ACTIVE.
 	 * 
 	 * TODO: I'm worried about this method and synchronization
 	 */
 	private void makeAllPlayersActive() {
 
 		if( players != null ) {
 			for( User player : players ) {
 				player.setStatus( STATUS.ACTIVE );
 			}
 		}
 	}
 
 	/**
 	 * Walk through the players, find their protocol object,
 	 * and set the bet value stored on it to null. This signifies
 	 * starting a new round of play, where the bet no longer
 	 * needs to be stored.
 	 * 
 	 * TODO: I'm worried about this method and synchronization
 	 */
 	private void removeAllPlayerBets() {
 
 		if( players != null ) {
 			for( User player : players ) {
 				player.clearBet();
 			}
 		}
 	}
 
 	/**
 	 * Given a starting index, look through the statuses list for
 	 * the first status on or after that value that is ACTIVE,
 	 * and then return the corresponding USER. If there are no
 	 * more ACTIVE statuses in the list at or after that index,
 	 * then return null
 	 * 
 	 * TODO: I'm worried about this method and synchronization
 	 * 
 	 * @param startingIndex The index from which to start looking for
 	 * ACTIVE status players
 	 * @return The first encountered player with an ACTIVE
 	 * status, or null if none are found
 	 */
 	private User getFirstActivePlayerFrom(int startingIndex ) {
 		
 		// Can't start at a negative index
 		if( startingIndex < 0 ) {
 			startingIndex = 0;
 		}
 		
 		// No players? Must be a null active one
 		if( players == null ) {
 			return null;
 		}
 		
 		// Cycle through
 		for( User player : players ) {
 			STATUS status = player.getStatus();
 			if( status != null && status.equals(STATUS.ACTIVE ) ) {
 				return player;
 			}
 		}
 		
 		// If we got here, we found nothing and need to return null
 		return null;
 	}
 	
 	/**
 	 * Get a copy of all the players, in an array list, while
 	 * synchronized. That way we can add and remove players to
 	 * our less and not run into concurrency issues
 	 * 
 	 * @return A list of the players
 	 */
 	synchronized public User[] getCopyOfPlayers() {
 
 		User[] users = null;
 		if( players != null ) {
 			Object[] copy = players.toArray();
 			users = new User[copy.length];
 			for( int i = 0; i < copy.length; i++ ) {
 				users[i] = (User)copy[i];
 			}
 		}
 		return users;
 	}
 
 	/**
 	 * A game is supposed to be STARTED or NOT_STARTED. Not sre
 	 * what decides it. Maybe if its thread is running?
 	 * 
 	 * @return Either "STARTED" or "NOT_STARTED"
 	 */
 	public String getGameStage() {
 		// TODO: Sort this out. Maybe it should be more 
 		// like "taking bets" or "playing hand" or something....
 		return STARTED_KEYWORD;
 	}
 
 
 }
