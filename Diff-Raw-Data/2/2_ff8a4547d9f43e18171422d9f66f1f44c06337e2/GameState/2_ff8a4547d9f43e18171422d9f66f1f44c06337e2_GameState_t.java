 /*******************************************************************************
  * CS544 Computer Networks Spring 2013
  * 5/26/2013 - GameState.java
  * Group Members
  * o Jennifer Lautenschlager
  * o Constantine Lazarakis
  * o Carol Greco
  * o Duc Anh Nguyen
  * 
  * Purpose: Most of the 'interesting stuff' that a game does is implemented in
  * the dynamic GameState. It handles adding and removing players, and provides
  * convenience methods for sending the 6xx response codes that alert clients
  * as to changes in the game state.
  ******************************************************************************/
 package drexel.edu.blackjack.server.game;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import drexel.edu.blackjack.cards.DealerShoeInterface;
 import drexel.edu.blackjack.cards.Hand;
 import drexel.edu.blackjack.cards.Hand.COMPARISON_RESULT;
 import drexel.edu.blackjack.cards.SimpleDealerShoe;
 import drexel.edu.blackjack.db.user.UserMetadata;
 import drexel.edu.blackjack.server.BlackjackProtocol;
 import drexel.edu.blackjack.server.ResponseCode;
 
 /**
  * Used to pass game state information from
  * the server to client, to show what bets
  * are made, what cards are dealt, what users
  * are joining and leaving, etc. Unlike
  * {@link drexel.edu.blackjack.db.game.GameMetadata},
  * the GameState is volatile and will change
  * often as the game progresses.
  * <P>
  * See Section 2.15 of the protocol design
  * for details.
  * 
  * @author Jennifer
  */
 public class GameState {
 
 	// And a logger for errors
 	//private final static Logger LOGGER = BlackjackLogger.createLogger(GameState.class.getName()); 
 
 	/**
 	 * Used for the status of players in the currently represented
 	 * state of the game.
 	 */
 	public enum STATUS {
 		/**
 		 * The user is an active participant. They have either
 		 * already made a bet, or else the server is expecting
 		 * them to make one currently.
 		 */
 		ACTIVE,
 		/**
 		 * The user is in the session, but as an observer.
 		 * Typically what happens is that, unless the user
 		 * is the first one into the game session, they are
 		 * joining when a game round is in progress. They
 		 * therefore must wait, as an observer of the game,
 		 * until bets are called for again.
 		 */
 		OBSERVER
 	}
 
 	/**
 	 * Used in the GAMESTAGE line of a GAMESTATUS response to indicate
 	 * that the game has started.
 	 */
 	public static final String STARTED_KEYWORD		= "STARTED";
 	/**
 	 * Used in the GAMESTAGE line of a GAMESTATUS response to indicate
 	 * that the game has not started.
 	 */
 	public static final String NOT_STARTED_KEYWORD	= "NOT_STARTED";
 	
 	/**
 	 * Used in the HAND line of a GAMESTATUS response to represent
 	 * the dealer's hand. Also used in certain game state messages,
 	 * such as the 
 	 * {@link drexel.edu.blackjack.server.ResponseCode.CODE#PLAYER_ACTION} or
 	 * {@link drexel.edu.blackjack.server.ResponseCode.CODE#CARDS_DEALT}
 	 * responses, to indicate that an action applies to the dealer.
 	 */
 	public static final String DEALER_USERNAME		= "dealer";
 	/**
 	 * Used in the BET or HAND lines of a GAMESTATUS response when
 	 * the username is for some reason (typically an error of some
 	 * sort) not known. Also used in the game state messages for
 	 * this purpose.
 	 */
 	public static final String UNKNOWN_USERNAME		= "(unknown)";
 	
 	/**
 	 * Used in
 	 * {@link drexel.edu.blackjack.server.ResponseCode.CODE#PLAYER_ACTION} 
 	 * responses to indicate that the cards were shuffled.
 	 */
 	public static final String SHUFFLED_KEYWORD		= "SHUFFLED";
 	/**
 	 * Used in
 	 * {@link drexel.edu.blackjack.server.ResponseCode.CODE#PLAYER_ACTION} 
 	 * responses to indicate that the user specified chose to stand
 	 * on their turn.
 	 */
 	public static final String STAND_KEYWORD		= "STAND";
 	/**
 	 * Used in
 	 * {@link drexel.edu.blackjack.server.ResponseCode.CODE#PLAYER_ACTION} 
 	 * responses to indicate that the user specified chose to hit
 	 * on their turn.
 	 */
 	public static final String HIT_KEYWORD			= "HIT";
 	/**
 	 * Used in
 	 * {@link drexel.edu.blackjack.server.ResponseCode.CODE#PLAYER_ACTION} 
 	 * responses to indicate that the user specified went bust
 	 * on their turn.
 	 */
 	public static final String BUST_KEYWORD			= "BUST";
 	/**
 	 * Used in
 	 * {@link drexel.edu.blackjack.server.ResponseCode.CODE#PLAYER_ACTION} 
 	 * responses to indicate that the user specified got a blackjack
 	 * on their turn.
 	 */
 	public static final String BLACKJACK_KEYWORD	= "BLACKJACK";
 	/**
 	 * Used in
 	 * {@link drexel.edu.blackjack.server.ResponseCode.CODE#GAME_OUTCOME}
 	 * responses to indicate that he player won. 
 	 */
 	public static final String WON_KEYWORD			= "WON";
 	/**
 	 * Used in
 	 * {@link drexel.edu.blackjack.server.ResponseCode.CODE#GAME_OUTCOME}
 	 * responses to indicate that he player lost. 
 	 */
 	public static final String LOST_KEYWORD			= "LOST";
 	/**
 	 * Used in
 	 * {@link drexel.edu.blackjack.server.ResponseCode.CODE#GAME_OUTCOME}
 	 * responses to indicate that he player tied. 
 	 */
 	public static final String TIED_KEYWORD			= "TIED";
 	
 	
 	// An ordered list of players involved in the game. Gameplay will
 	// occur in this order, amongst the ACTIVE users. 
 	private List<User> players 			= null;
 	
 	// The dealer's hand
 	private Hand dealerHand				= null;
 	
 	// Every game has an identifier
 	private String gameId				= null;
 	
 	// Needs to be a dealer shoe, with cards, and track the number of decks used
 	private int numberOfDecks			= 1;
 	private DealerShoeInterface	shoe	= null;
 
 	
 	/*********************************************************************
 	 * Constructor goes here
 	 ********************************************************************/
 	
 	/**
 	 * Construct a game state object for the given game
 	 * ID.
 	 * 
 	 * @param gameId A game's unique identifier to be used in
 	 * constructed messages
 	 * @param numberOfDecks how many decks to initialize the
 	 * dealer shoe with
 	 */
 	public GameState( String gameId, int numberOfDecks ) {
 		this.gameId = gameId;
 		this.numberOfDecks = numberOfDecks;
 		players = Collections.synchronizedList(new ArrayList<User>());
 	}
 
 	
 	/*********************************************************************
 	 * Notification methods. These are used to alert others in the game
 	 * of some sort of player action.
 	 ********************************************************************/
 
 	/**
 	 * Sets the dealer hand silently. No notifications are given
 	 * 
 	 * @param hand Hand that the dealer has
 	 */
 	private void setDealerHand( Hand hand ) {
 		this.dealerHand = hand;
 	}
 
 	/**
 	 * Gets the dealer hand silently.
 	 * 
 	 * @param hand Hand that the dealer has
 	 */
 	public Hand getDealerHand() {
 		return dealerHand;
 	}
 	
 	/**
 	 * Needs to notify all players in the game of the result of the 
 	 * game outcome for a particular player.
 	 * <P>
 	 * This is a {@link drexel.edu.blackjack.server.ResponseCode.CODE#GAME_OUTCOME}
 	 * code, the first parameter is the session ID, the second is the username,
 	 * and the third is one of WON, LOST, or TIED
 	 * @return True if sent successfully, false otherwise
 	 */
 	public boolean notifyAllOfGameOutcome(User player, COMPARISON_RESULT outcome) {
 		boolean success = false;
 		
 		if( player != null ) {
 			// Create the response code: gameid username result bet
			StringBuilder str = new StringBuilder( getStringForGameAndUser( player ) );
 			str.append( " " );
 			if( outcome == COMPARISON_RESULT.WIN ) {
 				str.append( WON_KEYWORD );
 			} else if (outcome == COMPARISON_RESULT.LOSE ) {
 				str.append( LOST_KEYWORD );
 			} else {
 				str.append( TIED_KEYWORD );
 			}
 			str.append( " " );
 			str.append( player.getBet() );
 			ResponseCode code = new ResponseCode( ResponseCode.CODE.GAME_OUTCOME, str.toString() );
 			
 			// Then send it to all the players
 			success = notifyOtherPlayers( code, null );		
 		}
 		
 		return success;
 	}
 	
 	/**
 	 * Needs to notify all players in the game that the indicated player has been
 	 * requested to make a gameplay. Also marks the player from this was requested
 	 * of that fact.
 	 * <P>
 	 * This is a {@link drexel.edu.blackjack.server.ResponseCode.CODE#REQUEST_FOR_GAME_ACTION}
 	 * code, the first parameter is the session ID, the second is the username
 	 * @return True if sent successfully, false otherwise
 	 */
 	public boolean notifyAllOfGameplayNeeded(User player) {
 		
 		boolean success = false;
 		
 		if( player != null ) {
 			// Create the response code: gameid username
 			StringBuilder str = new StringBuilder( getStringForGameAndUser( player ) );
 			ResponseCode code = new ResponseCode( ResponseCode.CODE.REQUEST_FOR_GAME_ACTION, str.toString() );
 			
 			// Mark in the player who it's being requested of that they are expected to make a gameplay
 			player.setNeedsToMakeAPlay( true );
 			
 			// Then send it to all the players
 			success = notifyOtherPlayers( code, null );		
 		}
 		
 		return success;
 	}
 
 
 	/**
 	 * Needs to notify all players in the game that the dealer hit a blackjack.
 	 * <P>
 	 * This is a {@link drexel.edu.blackjack.server.ResponseCode.CODE#PLAYER_ACTION}
 	 * code, the first parameter is the session ID, the second is the dealer name,
 	 * and the action word is BLACKJACK
 	 * @return True if sent successfully, false otherwise
 	 */
 	public boolean notifyPlayersOfDealerBlackjack() {		
 		return this.notifyOthersOfGameAction(null, BLACKJACK_KEYWORD);
 	}
 
 	/**
 	 * Needs to set on this object the dealer's had, and also send out a 
 	 * response code indicating that the dealer has been dealt new cards.
 	 * <P>
 	 * This is a {@link drexel.edu.blackjack.server.ResponseCode.CODE#CARDS_DEALT}
 	 * code, the first parameter is the session ID, the second is the dealer name,
 	 * and remaining parameters are the new cards dealt. Facedown cards are not
 	 * presented in the message.
 	 * @param hand Hand that the dealer has
 	 * @return True if sent successfully, false otherwise
 	 */
 	public boolean setDealerHandAndNotify(Hand hand) {
 		
 		// First set the hand
 		setDealerHand(hand);
 		
 		// Create the response code: gameid username <hand as seen by other users>
 		StringBuilder str = new StringBuilder( getStringForGameAndUser( null ) );
 		str.append( " " );
 		str.append( dealerHand.toString(false) );	// False because players can't see dealer facedown cards
 		ResponseCode code = new ResponseCode( ResponseCode.CODE.CARDS_DEALT, str.toString() );
 
 		// Then send it to all the players
 		return notifyOtherPlayers( code, null );		
 	}
 
 	/**
 	 * Used when a hand is updated by flipping some facedown card
 	 * to a faceup card. Need to send out a response code with the
 	 * latest hand.
 	 * <P>
 	 * This is a {@link drexel.edu.blackjack.server.ResponseCode.CODE#UPDATED_HAND}
 	 * code, the first parameter is the session ID, the second is the username,
 	 * and remaining parameters are the updated cards. 
 	 * 
 	 * @param user
 	 * @param hand
 	 */
 	public boolean notifyOthersOfUpdatedHand(User user, Hand hand) {
 		boolean success = false;
 		
 		if( hand != null ) {
 			
 			// Create the response code: gameid username <hand as seen by other users>
 			StringBuilder str = new StringBuilder( getStringForGameAndUser( user) );
 			str.append( " " );
 			str.append( hand.toString(false) );	// False because it's not for the player whose hand it is
 			ResponseCode code = new ResponseCode( ResponseCode.CODE.UPDATED_HAND, str.toString() );
 
 			// Then send it to all the players except this user
 			success = notifyOtherPlayers( code, user );		
 		}
 		
 		return success;
 	}
 
 	/**
 	 * Needs to send out a response code indicating that the user
 	 * in question has been dealt new cards.
 	 * <P>
 	 * This is a {@link drexel.edu.blackjack.server.ResponseCode.CODE#CARDS_DEALT}
 	 * code, the first parameter is the session ID, the second is the username,
 	 * and remaining parameters are the new cards dealt. When the message is
 	 * sent to the player whose hand it is, they can see facedown cards. Otherwise,
 	 * facedown cards are not revealed
 	 * @param user User whose hand information should be sent
 	 * @return True if sent successfully, false otherwise
 	 */
 	public boolean notifyAllOfNewCards(User user) {
 		
 		boolean success = false;
 		
 		if( user != null && user.getHand() != null ) {
 			
 			Hand hand = user.getHand();
 			
 			// Create the response code: gameid username <hand as seen by other users>
 			StringBuilder str = new StringBuilder( getStringForGameAndUser( user) );
 			str.append( " " );
 			str.append( hand.toString(false) );	// False because it's not for the player whose hand it is
 			ResponseCode code = new ResponseCode( ResponseCode.CODE.CARDS_DEALT, str.toString() );
 
 			// Then send it to all the players except this user
 			success = notifyOtherPlayers( code, user );		
 
 			// A new code is used to notify this user, as they get to
 			// see all the card values
 			str = new StringBuilder( getStringForGameAndUser( user ) );
 			str.append( " " );
 			str.append( hand.toString(true) );	// True because it's for the playe whose hand it is
 			code = new ResponseCode( ResponseCode.CODE.CARDS_DEALT, str.toString() );
 			
 			// Then send it to the player in question
 			success = success && user.sendMessage( code );
 		}
 		
 		return success;
 	}
 
 	/**
 	 * Need to send out messages to all players and notify them about
 	 * the dealer shoe being shuffled.
      * <P>
 	 * This is a {@link drexel.edu.blackjack.server.ResponseCode.CODE#PLAYER_ACTION}
 	 * code, specifying that the performer of the action is the dealer 
 	 * and the action itself is a SHUFFLE.
 	 *
 	 * @return True if notifications were sent successfully, false
 	 * if there were problems
 	 */
 	public boolean notifyAllOfShuffle() {
 		
 		boolean success = false;
 		
 		// Create the response code: gameid dealer_username SHUFFLED_KEYWORD
 		StringBuilder str = new StringBuilder( getStringForGameAndUser( null ) );
 		str.append( " " );
 		str.append( SHUFFLED_KEYWORD );
 		ResponseCode code = new ResponseCode( ResponseCode.CODE.PLAYER_ACTION, str.toString() );
 
 		// Then send it to all the players
 		success = notifyOtherPlayers( code, null );		
 
 		return success;
 	}
 
 	
 	/**
 	 * Need to send out messages to the remaining players and notify them about
 	 * a player leaving the game.
      * <P>
 	 * This is a {@link drexel.edu.blackjack.server.ResponseCode.CODE#PLAYER_LEFT}
 	 * code, and the parameters are the gameId followed by the username. 
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
      * <P>
 	 * This is a {@link drexel.edu.blackjack.server.ResponseCode.CODE#PLAYER_JOINED}
 	 * code, and the parameters are the gameId followed by the username. 
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
 	 * how a bet was placed.
      * <P>
 	 * This is a {@link drexel.edu.blackjack.server.ResponseCode.CODE#PLAYER_BET}
 	 * code, and the parameters are the gameId followed by the username
 	 * followed by the bet amount. 
 	 * 
 	 * @param player Who placed the bet
 	 * @param bet The amount bet
 	 * @return True if it was successfully broadcast, else false
 	 */
 	public boolean notifyOthersOfBetPlaced( User player, int bet ) {
 		
 		boolean success = false;
 		
 		// It's all bad if they don't specify who joined
 		if( player != null ) {
 			// Create the response code: gameid username bet
 			StringBuilder str = new StringBuilder( getStringForGameAndUser( player ) );
 			str.append( " " );
 			str.append( bet );
 			ResponseCode code = new ResponseCode( ResponseCode.CODE.PLAYER_BET, str.toString() );
 
 			// Then send it to all the players except the one who generated
 			success = notifyOtherPlayers( code, player );		
 		}
 
 		return success;
 	}	
 
 	/**
 	 * Need to send out messages to the other players (if any) about
 	 * how a game action was made on the part of a player
      * <P>
 	 * This is a {@link drexel.edu.blackjack.server.ResponseCode.CODE#PLAYER_ACTION}
 	 * code, and the parameters are the gameId followed by the username
 	 * followed by the action taken
 	 * 
 	 * @param player Who performed the action (or null if it's the dealer)
 	 * @param action What the action was
 	 * @return True if it was successfully broadcast, else false
 	 */
 	public boolean notifyOthersOfGameAction( User player, String action ) {
 		
 		// Create the response code: gameid username action
 		StringBuilder str = new StringBuilder( getStringForGameAndUser( player ) );
 		str.append( " " );
 		str.append( action );
 		ResponseCode code = new ResponseCode( ResponseCode.CODE.PLAYER_ACTION, str.toString() );
 
 		// Then send it to all the players except the one who generated
 		return notifyOtherPlayers( code, player );		
 	}	
 
 	/**
 	 * Send a response code update to all other players except for
 	 * the one passed in (who might be null)
 	 * 
 	 * @param code What to send
 	 * @param player Who not to send it to. Set to null if it should
 	 * go to everyone
 	 * @return True if successfully sent, false otherwise
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
 	 * @param player The 'user' part of the 'GameAndUser', or NULL if
 	 * the user is the dealer
 	 * @return The string of the gameid and username, unless something
 	 * went wrong and then null
 	 */
 	private String getStringForGameAndUser( User player) {
 		// Start by creating a response code: gameId username
 		StringBuilder str = new StringBuilder( gameId );
 		str.append( " " );
 		UserMetadata metadata = (player == null ? null : player.getUserMetadata());
 		if( player == null ) {
 			str.append( DEALER_USERNAME );
 		} else if( metadata == null || metadata.getUsername() == null ) {
 			str.append( UNKNOWN_USERNAME );
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
 	 * added players are entered in the OBSERVER state, and their
 	 * bets and hands and need for gameplay are all reset
 	 * 
 	 * @param player Who to add
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
 				player.resetForNextRound();
 				
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
 	 * as it was already deducted from their account. Also need
 	 * to reset some variables on them, for when they play again
 	 * 
 	 * @param player Who to remove
 	 */
 	synchronized public boolean removePlayer( User player ) {
 		
 		// Assume that we'll fail
 		boolean status = false;
 		
 		if( player != null ) {		
 			// Remove them from the list of players
 			status = players.remove( player );
 			if( status ) {
 				player.resetForNextRound();
 			}
 		}
 		
 		// Return the status
 		return status;
 	}
 	
 	/** 
 	 * Returns the number of players, without regard to their
 	 * status as observer or active.
 	 * 
 	 * @return Total number of players in game
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
 						// Remove them from the list of game players
 						removePlayer(user);
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
 	
 
 	/**
 	 * Looks through the list of active players in the session
 	 * for one who needs to play their hand still. Return a
 	 * reference to them. If there are none that match that
 	 * criteria, return null
 	 * 
 	 * @return Either a player who still needs to hit/stand,
 	 * or else null if none that match that criteria are left
 	 */
 	public User getNextPlayerToPlay() {
 		
 		User nextPlayer = null;
 		
 		// Look for any who are active and need to pla
 		User[] players = this.getCopyOfPlayers();
 		for( User player : players ) {
 			if( player.getStatus() != null && player.getStatus().equals(STATUS.ACTIVE) ) {
 				// Okay, they're active. But do they need to make their game play?
 				if( !player.getFinishedGamePlayThisRound() ) {
 					nextPlayer = player;
 				}
 			}
 		}
 		
 		return nextPlayer;
 	}
 
 	
 	/*********************************************************************
 	 * These have to do with the game shoe, and dealing cards, and all
 	 * of that.
 	 ********************************************************************/
 
 	/**
 	 * Checks to see if the cards need to be shuffled. Rules for
 	 * this include: if you just started, of course you need to
 	 * shuffle. Otherwise you shuffle if less than 50% of the cards
 	 * are remaining.
 	 * 
 	 * @return True if a shuffle is needed as per these rules, false
 	 * otherwise
 	 */
 	public boolean needToShuffle() {
 		
 		boolean needToReshuffle = false;
 		
 		if( shoe == null ) {
 			needToReshuffle = true;
 		} else if( shoe.getPercentageOfDealtCards() > 0.5 ) {
 			needToReshuffle = true;
 		}
 		
 		return needToReshuffle;
 	}
 	
 	/**
 	 * Get the dealer shoe
 	 * return the dealer shoe, guaranteed non-null
 	 */
 	public DealerShoeInterface getDealerShoe() {
 		if (shoe == null) {
 			shoe = new SimpleDealerShoe( numberOfDecks );
 		}
 		return shoe;
 	}
 	
 	/**
 	 * Perform the action of reshuffling. Notify players about
 	 * the cards being shuffled.
 	 */
 	public void shuffle() {
 		shoe = getDealerShoe();
 		shoe.shuffle();
 		notifyAllOfShuffle();
 	}
 	
 	/*********************************************************************
 	 * These deal with altering the game state in response to activity
 	 * in the game
 	 ********************************************************************/
 
 	
 	/**
 	 * Indicates to the game state that a new round
 	 * is being started. A few things are done here:
      * <P>
 	 * <ol>
 	 * <li>All players are set to ACTIVE status
 	 * <li>Bets and hands are cleared out
 	 * <li>Requests for bids are made of all players
 	 * </ol>
 	 */
 	synchronized public void startNewRound() {
 		
 		// Need to have valid lists
 		if( players != null ) {
 			// Sets up all players active
 			makeAllPlayersActive();
 			
 			// Sets up all players active
 			resetAllPlayersForNewRound();
 			
 			// Reset the dealer's hand
 			setDealerHand( null );
 			
 			// Request bids from all players
 			for( User player : players ) {
 				ResponseCode code = new ResponseCode( ResponseCode.CODE.REQUEST_FOR_BET );
 				player.setProtocolState( BlackjackProtocol.STATE.IN_SESSION_AWAITING_BETS );
 				player.sendMessage(code);
 			}
 		}
 	}
 
 	
 	/*********************************************************************
 	 * Private methods go here
 	 ********************************************************************/
 
 	
 	/**
 	 * Walk through the players, and mark them all as ACTIVE.
      * <P>
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
      * <P>
 	 * TODO: I'm worried about this method and synchronization
 	 */
 	private void resetAllPlayersForNewRound() {
 
 		if( players != null ) {
 			for( User player : players ) {
 				player.resetForNextRound();
 			}
 		}
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
 	 * A game is supposed to be STARTED or NOT_STARTED. Not sure
 	 * what decides it. Maybe if its thread is running?
      * <P>
 	 * TODO: The game states probably don't make sense.
 	 * It should probably be BETTING or PLAYING or 
 	 * WAITING
 	 * 
 	 * @return Either "STARTED" or "NOT_STARTED"
 	 */
 	public String getGameStage() {
 		// TODO: Sort this out. Maybe it should be more 
 		// like "taking bets" or "playing hand" or something....
 		return STARTED_KEYWORD;
 	}
 
 }
