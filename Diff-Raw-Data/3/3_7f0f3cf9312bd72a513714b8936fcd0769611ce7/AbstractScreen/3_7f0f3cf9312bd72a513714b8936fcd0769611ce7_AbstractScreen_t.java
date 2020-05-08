 /*******************************************************************************
  * CS544 Computer Networks Spring 2013
  * 5/26/2013 - AbstractScreen.java
  * Group Members
  * o Jennifer Lautenschlager
  * o Constantine Lazarakis
  * o Carol Greco
  * o Duc Anh Nguyen
  * 
  * Purpose: This is the abstract base clas for all client UI screens. It
  * specifies what methods all screens must implement, such as responding to
  * messages from the server and listening to input from the keyboard, as well
  * as provides some common functionality shared by all UI screens that is
  * related to sending and receiving messages with the server.
  ******************************************************************************/
 package drexel.edu.blackjack.client.screens;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.StringTokenizer;
 import java.util.logging.Logger;
 
 import drexel.edu.blackjack.client.BlackjackCLClient;
 import drexel.edu.blackjack.client.in.ClientInputFromServerThread;
 import drexel.edu.blackjack.client.in.MessagesFromServerListener;
 import drexel.edu.blackjack.client.out.ClientOutputToServerHelper;
 import drexel.edu.blackjack.client.screens.util.ClientSideGame;
 import drexel.edu.blackjack.server.ResponseCode;
 import drexel.edu.blackjack.server.game.Game;
 import drexel.edu.blackjack.server.game.GameState;
 import drexel.edu.blackjack.util.BlackjackLogger;
 
 /**
  * <b>UI:</b> A screen is something the presents some information
  * to the user, and expects them to do something in response.
  * If this was a graphical interface, it would probably be a window
  * the user could interact with. Since it's a command-line interface,
  * it is instead a menu-driven interface, with occasional prompts
  * for information (e.g., what game session to join, what amount 
  * to bet). Menus or prompts are displayed, and the user types in 
  * their response and hits enter to make a choice. The UI then 'translates'
  * the request, if needed, into protocol commands to issue to the
  * server. The user at the keyboard never has to type in raw protocol
  * commands, or even know that they exist. (Note: Some UI options 
  * don't involve the server, though, like the option to re-display 
  * the last menu.)
  * <p>
  * <b>UI:</b> The screen is also responsible for presenting any
  * relevant information from the server, passed as protocol messages,
  * to the user, as appropriate. (Inappropriate messages to show are
  * things like internal errors, which the user doesn't have to
  * know about.) The user at the keyboard never sees the raw response
  * code output from the server, though. Instead, the UI translates
  * it into a more user-friendly presentation format before 
  * displaying it.
  * <p>
  * <b>UI:</b> As this is an abstract class, it must be implemented
  * by another class to define a logical screen in the user interface.
  * Only one screen at a time is active, just as only one user
  * interface window in graphical client at a time has focus. There is
  * also the concept of showing the 'next' screen, and the 'previous'
  * screen, which is one way of having the UI move between screen.
  * Another way is simply specifying which screen should become active.
  * Inactive screens may receive input, from either the user at the
  * keyboard or the server, but so long as the screen is inactive it
  * should not do anything visible in the UI in response to such
  * messages.
  * <p>
  * <b>UI:</b> Some base functionality of interest to subclasses is 
  * included in this base abstract class. For example, in the protocol
  * definitions there are commands available at any state that 
  * generate either successful or error response. This abstract
  * provides default methods for generating such commands to send
  * to the server, or responding to input from the server, in a 
  * general fashion. Subclasses are free to make use of this 
  * functionality or extend it, as they see fit.
  * <P>
  * <b>UI:</b> In general, <b>everything</b> in this file corresponds
  * to the user interface in some fashion. All comments in this code
  * are therefore relevant to how the UI operates.
  * 
  * @author Jennifer
  */
 public abstract class AbstractScreen implements MessagesFromServerListener {
 
 	/*****************************************************************
 	 * Enum to define the type of screen
 	 ***************************************************************/
 	public enum SCREEN_TYPE {
 		
 		/**
 		 * The login screen corresponds, roughly, to the 
 		 * {@link drexel.edu.blackjack.server.BlackjackProtocol.STATE#WAITING_FOR_USERNAME}
 		 * and
 		 * {@link drexel.edu.blackjack.server.BlackjackProtocol.STATE#WAITING_FOR_PASSWORD}
 		 * protocol states.
 		 */
 		LOGIN_SCREEN,
 		/**
 		 * The not_in_session screen corresponds, roughly, to the 
 		 * {@link drexel.edu.blackjack.server.BlackjackProtocol.STATE#NOT_IN_SESSION}
 		 * protocol states.
 		 */
 		NOT_IN_SESSION_SCREEN,
 		/**
 		 * The in_session screen corresponds, roughly, to the 
 		 * {@link drexel.edu.blackjack.server.BlackjackProtocol.STATE#IN_SESSION_SERVER_PROCESSING},
 		 * {@link drexel.edu.blackjack.server.BlackjackProtocol.STATE#IN_SESSION_DEALER_BLACKJACK},
 		 * {@link drexel.edu.blackjack.server.BlackjackProtocol.STATE#IN_SESSION_BEFORE_YOUR_TURN},
 		 * {@link drexel.edu.blackjack.server.BlackjackProtocol.STATE#IN_SESSION_AWAITING_BETS},
 		 * {@link drexel.edu.blackjack.server.BlackjackProtocol.STATE#IN_SESSION_AS_OBSERVER},
 		 * {@link drexel.edu.blackjack.server.BlackjackProtocol.STATE#IN_SESSION_AND_YOUR_TURN}, and
 		 * {@link drexel.edu.blackjack.server.BlackjackProtocol.STATE#IN_SESSION_AFTER_YOUR_TURN} 
 		 * protocol states.
 		 */
 		IN_SESSION_SCREEN
 		
 	}
 
 	
 	/*****************************************************************
 	 * Local variables here
 	 ***************************************************************/
 
 
 	/**
 	 * Request to see the version of the server
 	 */
 	protected static String VERSION_OPTION			= "V";	
 	/**
 	 * Request to see the capabilities of the server
 	 */
 	protected static String CAPABILITIES_OPTION		= "C";	
 	/**
 	 * Request to quit the client entirely
 	 */
 	protected static String QUIT_OPTION				= "Q";	
 	/**
 	 * Request to repeat the last menu
 	 */
 	protected static String MENU_OPTION				= "?";	
 	/**
 	 * Request to view the user's bank account balance
 	 */
 	protected static String ACCOUNT_OPTION			= "A";	
 	/**
 	 * Request to toggle the frame that shows messages sent and received
 	 */
 	protected static String TOGGLE_MONITOR_OPTION	= "T";	
 	/**
 	 * Request to go back a menu
 	 */
 	protected static String BACK_OPTION				= "back";	
 
 	/**
 	 *  What type of screen is it
 	 */
 	private SCREEN_TYPE screenType = SCREEN_TYPE.LOGIN_SCREEN;	// We start with the login
 	
 	/**
 	 * Whether or not this particular screen is active. In general,
 	 * only one screen should be active at a time.
 	 */
 	protected boolean isActive;
 
 	/**
 	 * Once a bet has been accepted by the server, and a response as to this
 	 * received, it becomes an accepted bet. Obviously this only matters
 	 * if the person is playing a game.
 	 */
 	protected Integer acceptedBet = null;		// Server has verified this is your bet
 	
 	/**
 	 * This means that the user has requested bet, but that the server has
 	 * not agreed to it yet. Obviously this only matters if the person is
 	 * playing a game.
 	 */
 	protected Integer requestedBet = null;	// This is what's being requested, but it might be denied
 
 	/**
 	 * This is the player's current hand of cards. Obviously it only matters
 	 * if the person is playing a game currently.
 	 */
 	protected String cards = "NONE";
 	
 	/**
 	 * Keep a pointer to the thread that is associated with
 	 * this I/O for this screen. This is for messages received on the 
 	 * socket, not for keyboard input.
 	 */
 	protected ClientInputFromServerThread clientThread;
 	/**
 	 * Keep a pointer to the helper that is associated with
 	 * this I/O for this screen. This is for messages bound
 	 * to the server.
 	 */
 	protected ClientOutputToServerHelper helper;
 
 	/**
 	 * Reference to the main client
 	 */
 	protected BlackjackCLClient client;
 	
 	// The last username that was specified for logging in. If the user is authenticated,
 	// and the screen is something other than the LoginScreen, then this is the username
 	// of the currently logged in player. This is static so that all the screen's that
 	// subclass an abstract screen have access to the value.
 	private static String username = null;
 	
 	// Our logger
 	private final static Logger LOGGER = BlackjackLogger.createLogger(AbstractScreen.class .getName()); 
 	
 	
 	/*****************************************************************
 	 * Constructor goes here
 	 ***************************************************************/
 	
 	
 	/**
 	 * Create a default user interface screen.
 	 * 
 	 * @param client The client instance the screen is associated with
 	 * @param thread This thread is used to receive responses from the remote server
 	 * @param helper This class is used to send messages to the remote server
 	 */
 	public AbstractScreen( BlackjackCLClient client, ClientInputFromServerThread thread,
 			ClientOutputToServerHelper helper ) {
 		this.client = client;
 		this.clientThread = thread;
 		this.helper = helper;
 	}
 	
 	
 	/*****************************************************************
 	 * Abstract methods to be defined elsewhere
 	 ***************************************************************/
 	
 	
 	/**
 	 * Used to display whatever sort of command-line 'menu'
 	 * to the screen that is appropriate. Some common options
 	 * are listed in the static variables at the top of this
 	 * class, so that identical functionality will have identical
 	 * menu option letters across screens.
 	 */
 	public abstract void displayMenu();
 	
 	/**
 	 * Resets the 'screen' to its starting state. This is typically
 	 * done after some sort of error state (that is, error state of
 	 * the user interface, this is not protocol state) is reached.
 	 */
 	public abstract void reset();
 	
 	/**
 	 * Process a response code from the server. The screen should
 	 * make sure that they are active before responding visually
 	 * to any response code. Typically this is a set of if/elseif
 	 * statements (or a case statement) that uses the code value
 	 * for making the selection.
 	 */
 	public abstract void processMessage( ResponseCode code );
 
 	/**
 	 * Used to pass user input to the screen. Since input is handled
 	 * on a separate thread, a listener elsewhere is used to pass
 	 * the input to the user interface screen. The screen should
 	 * check that it is active before responding visually to any
 	 * response code. Typically this is a set of if/elseif
 	 * statements (or a case statement) that uses the string,
 	 * along with the state of the UI screen, to decide what to do.
 	 */
 	public abstract void handleUserInput( String str );
 
 	
 	/*****************************************************************
 	 * Some getters and setters, essentially, with at most a small
 	 * amount of necessary logic
 	 ***************************************************************/
 
 	
 	/**
 	 * @return the screenType
 	 */
 	public SCREEN_TYPE getScreenType() {
 		return screenType;
 	}
 
 	/**
 	 * @param screenType the screenType to set
 	 */
 	protected void setScreenType(SCREEN_TYPE screenType) {
 		this.screenType = screenType;
 	}	
 
 	/**
 	 * Sets the username, who may or may not have authenticated
 	 * okay. If we're in the LoginScreen, we don't know. Otherwise,
 	 * they must have.
 	 * 
 	 * @param username username
 	 */
 	protected void setUsername( String username ) {
 		AbstractScreen.username = username;
 	}
 	
 	/**
 	 * Gets the username, who may or may not have authenticated
 	 * okay. If we're in the LoginScreen, we don't know. Otherwise,
 	 * they must have.
 	 * 
 	 * @return username
 	 */
 	protected String getUsername() {
 		return username;
 	}
 
 	/**
 	 * Notifies the screen that it is now the 'active' screen,
 	 * and should start doing its I/O, if the value is true.
 	 * Otherwise it's notifying it that it's no longer active
 	 * and should stop doing I/O.
 	 * 
 	 * @param isActive Whether or not the screen should consider
 	 * itself active, and needing to process I/O received
 	 */
 	public void setIsActive( boolean isActive ) {
 		
 		// If you're activating yourself, set yourself up as the default listener
 		if( isActive ) {
 			clientThread.addListener( this );
 		} else { 
 			clientThread.removeListener( this );
 		}
 		
 		this.isActive = isActive;
 	}
 	
 	/**
 	 * Request that the user interface show the 'next screen', which is
 	 * based on what the currentScreen is. It defers the request to
 	 * the client.
 	 * 
 	 * @param displayMenu True if it should immediately show the menu
 	 */
 	public void showNextScreen( boolean displayMenu ) {
 		
 		if( client == null ) {
 			LOGGER.severe( "Cannot show the next user interface screen as we don't seem to have a client set." );
 		} else {
 			client.showNextScreen( displayMenu );
 		}
 	}
 	
 	/**
 	 * Request that the user interface show the 'previous screen', which is
 	 * based on what the currentScreen is. It defers the request to the
 	 * client.
 	 * 
 	 * @param displayMenu True if it should immediately show the menu
 	 */
 	public void showPreviousScreen( boolean displayMenu ) {
 		
 		if( client == null ) {
 			LOGGER.severe( "Cannot show the previous user interface screen as we don't seem to have a client set." );
 		} else {
 			client.showPreviousScreen( displayMenu );
 		}
 	}
 
 	
 	/*****************************************************************
 	 * These methods have to do with handling responses from the
 	 * server. Typically this would be overridden by the extending
 	 * class so they can add their own specific handling out of
 	 * methods, but defaults are provided here for many things.
 	 ***************************************************************/
 
 	
 	/**
 	 * Does the best it can to handle a response code that the
 	 * implementing screen did not handle. This might be because
 	 * it's a 'general error' or 'common message', or it might 
 	 * simply be something that was totally unexpected.
      * <P>
 	 * Handling the message might involve printing to the console
 	 * or it might just be handled internally in a silent manner.
 	 * Typically the user should be shielded as much as possible
 	 * from errors they cannot fix. Also, raw protocol messages
 	 * should not be presented to the user. They should be processed
 	 * and any information presented in a more visually-appealing
 	 * fashion.
      * <P>
 	 * If handling the code requires redisplaying the menu, do that
 	 * here. Typically this is only done if you reset, if you have
 	 * changed menus or screens, or if you have received a message 
 	 * that prints a lot to the screen, such that displaying the 
 	 * menu again would be helpful.
 	 * 
 	 * @param code What was received
 	 */
 	protected void handleResponseCode(ResponseCode code) {
 
 		// Only handle if it's active
 		if( isActive ) {
 			
 			if( code == null ) {
 				// Internal error or something
 				LOGGER.severe( "Received null code from the server and don't know what to do." );
 				reset();
 			} else if( code.isError() || code.isMalformed() ) {
 				// Internal error? Or syntax error? Just reset the screen
 				LOGGER.warning( "Received unhandled error code of '" + code.toString() + "'." );
 				reset();
 			} else if( code.isInformative() ) {
 				
 				if( code.hasSameCode(ResponseCode.CODE.VERSION ) ) {
 					displayVersion( code );
 				} else if( code.hasSameCode( ResponseCode.CODE.CAPABILITIES_FOLLOW ) ) {
 					displayCapabilities( code );
 					displayMenu();
 				} else if( code.hasSameCode( ResponseCode.CODE.ACCOUNT_BALANCE ) ) {
 					displayAccountBalance( code );
 				} else {
 					LOGGER.warning( "Received unhandled informative code of '" + code.toString() + "'." );
 				}
 				
 			} else if( code.isGameState() ) {
 				
 				if( code.hasSameCode(ResponseCode.CODE.PLAYER_JOINED ) ) {
 					displayPlayerMovement( code );
 				} else if( code.hasSameCode(ResponseCode.CODE.PLAYER_LEFT ) ) {
 					displayPlayerMovement( code );
 				} else if( code.hasSameCode(ResponseCode.CODE.PLAYER_BET ) ) {
 					displayPlayerBet( code );
 				} else if( code.hasSameCode(ResponseCode.CODE.CARDS_DEALT ) ) {
 					displayCardsDealt( code );
 				} else if( code.hasSameCode(ResponseCode.CODE.PLAYER_ACTION ) ) {
 					displayPlayerAction( code );
 				} else if( code.hasSameCode( ResponseCode.CODE.GAME_OUTCOME ) ) {
 					displayGameOutcome( code );
 				} else if( code.hasSameCode(ResponseCode.CODE.UPDATED_HAND ) ) {
 					displayUpdatedHand( code );
 				} else {
 					// TODO: Handle other game state codes
 					LOGGER.warning( "Received unhandled game state code of '" + code.toString() + "'." );
 				}
 				
 			} else if( code.isCommandComplete() ) {
 				
 				if( code.hasSameCode(ResponseCode.CODE.SUCCESSFULLY_QUIT ) ) {
 					quitTheGame();
 				} else {
 					LOGGER.warning( "Received unhandled command-complete code of '" + code.toString() + "'." );
 				}
 				
 			} else {
 				// TODO: Not sure what to do here
 				LOGGER.warning( "Received some other unhandled code of '" + code.toString() + "'." );
 			}
 		}
 	}
 
 	/**
 	 * This handles codes about players (or the dealer) performing
 	 * actions. In the blackjack protocol, the first parameter of
 	 * the response code is always the game ID. The second 
 	 * parameter is the username, or the special word 'dealer' if
 	 * the dealer performed the action. The third parameter is the action,
 	 * one of several predefined types in the protocol. This is all
 	 * as per the protocol definition.
 	 * 
 	 * @param code Hopefully of type ResponseCode.CODE.PLAYER_ACTION
 	 */
 	private void displayPlayerAction(ResponseCode code) {
 		
 		if( code != null  ) {
 
 			// Our parameters
 			List<String> params = code.getParameters();
 
 			// All player update messages that are presented to the user
 			// start the same, with the username involved in the action
 			StringBuilder str = createStringBuilderForUserUpdate(params);
 			str.append( " " );
 			
 			// What was the action? It's in parameter two (hopefully)
 			String action = null;
 			if( params != null && params.size() >= 3 ) {
 				action = params.get(2);
 			}
 			
 			// Decide what to display based on the action. The actions in the protocol
 			// are predefined. They can be found in the code in GameState class
 			if( action == null ) {
 				str.append( "performed an unknown action" );
 			} else if( action.equalsIgnoreCase( GameState.SHUFFLED_KEYWORD ) ) {
 				str.append( "shuffled all the cards in the shoe" );
 			} else if( action.equalsIgnoreCase( GameState.BUST_KEYWORD ) ) {
 				str.append( "just went bust!" );
 			} else if( action.equalsIgnoreCase( GameState.HIT_KEYWORD ) ) {
 				str.append( "decided to take another card" );
 			} else if( action.equalsIgnoreCase( GameState.STAND_KEYWORD ) ) {
 				str.append( "decided to stand" );
 			} else if( action.equalsIgnoreCase( GameState.BLACKJACK_KEYWORD ) ) {
 				str.append( "has a blackjack!" );
 			} else {
 				str.append( "performed an unknown action" );
 			}
 
 			// Grammar is important!
 			str.append( "." );
 			
 			// Display to the screen. Since this is a short 1-line message, treat it as
 			// a status update message
 			updateStatus( str.toString() );
 		}
 	}
 
 
 	/**
 	 * This handles codes about players, and whether they won or lost
 	 * the game, and if so by how much. In the blackjack protocol, the 
 	 * first parameter of the response code is always the game ID. The second 
 	 * parameter is the username. The third parameter is the action,
 	 * one of several predefined types in the protocol. This is all
 	 * as per the protocol definition.
 	 * 
 	 * @param code Hopefully of type ResponseCode.CODE.PLAYER_ACTION
 	 */
 	private void displayGameOutcome(ResponseCode code) {
 		
 		if( code != null  ) {
 
 			// Our parameters
 			List<String> params = code.getParameters();
 
 			// All player update messages that are presented to the user
 			// start the same, with the username involved in the action
 			StringBuilder str = createStringBuilderForUserUpdate(params);
 			str.append( " " );
 			
 			// Win, lose, tie?? It's in parameter two (hopefully)
 			String result = null;
 			if( params != null && params.size() >= 3 ) {
 				result = params.get(2);
 			}
 			
 			// Decide what to display based on the result
 			if( result == null ) {
 				str.append( "maybe won, maybe lost");
 			} else if( result.equalsIgnoreCase( GameState.WON_KEYWORD ) ) {
 				str.append( "won" );
 			} else if( result.equalsIgnoreCase( GameState.LOST_KEYWORD ) ) {
 				str.append( "lost" );
 			} else {
 				str.append( "tied and gets to keep" );
 			}
 			
 			// Bet amount is in parameter three (hopefully)
 			String bet = null;
 			if( params != null && params.size() >= 4 ) {
 				bet = params.get(3);
 			}
 
 			str.append( " their " );
 			if( bet == null ) {
 				str.append( "original" );
 			} else {
 				str.append( "$" );
 				str.append( bet );
 			}
 			
 			// Finish it off
 			str.append( " bet." );
 			
 			// Display to the screen. Since this is a short 1-line message, treat it as
 			// a status update message
 			updateStatus( str.toString() );
 		}
 	}
 
 
 	/**
 	 * This handles codes about players entering or leaving the
 	 * game. The first parameter is the game ID. The second parameter
 	 * is the username. This is as per the protocol definition.
 	 * 
 	 * @param code Hopefully of type ResponseCode.CODE.PLAYER_JOINED
 	 * or ResponseCode.CODE.PLAYER_LEFT
 	 */
 	private void displayPlayerMovement(ResponseCode code) {
 		
 		if( code != null  ) {
 			
 			// Our parameters
 			List<String> params = code.getParameters();
 
 			// All player update messages start the same
 			StringBuilder str = createStringBuilderForUserUpdate(params);
 			
 			// Have they left or joined?
 			str.append( " has " );
 			if( code.hasSameCode(ResponseCode.CODE.PLAYER_JOINED) ) {
 				str.append( "joined" );
 			} else if( code.hasSameCode(ResponseCode.CODE.PLAYER_LEFT) ) {
 				str.append( "left" );
 			} else { 
 				str.append( "performed an unknown action in " );
 			}
 			str.append( " the game." );
 			
 			// Display to the screen as a short status update
 			updateStatus( str.toString() );
 		}
 	}
 
 	/**
 	 * This handles codes about players placing a bet. The first 
 	 * parameter is the game ID. The second parameter is the username.
 	 * The third parameter is the bet amount. This is all as per
 	 * the protocol defintion.
 	 * 
 	 * @param code Hopefully of type ResponseCode.CODE.PLAYER_BET
 	 */
 	private void displayPlayerBet(ResponseCode code) {
 		
 		if( code != null  ) {
 
 			// Our parameters
 			List<String> params = code.getParameters();
 
 			// All player update messages start the same
 			StringBuilder str = createStringBuilderForUserUpdate(params);
 			
 			// How much did they bet?
 			str.append( " has placed a bet of " );
 			if( params == null || params.size() < 3 ) {
 				str.append( "an unknown amount" );
 			} else {
 				String amount = params.get(2);
 				str.append( "$" );
 				str.append( amount );
 			}
 			str.append( "." );
 			
 			// Display to the screen
 			updateStatus( str.toString() );
 		}
 	}
 
 	/**
 	 * This handles codes about cards dealt to someone. The first 
 	 * parameter is the game ID. The second parameter is the username.
 	 * The rest are the cards. This is all as per
 	 * the protocol defintion.
 	 * 
 	 * @param code Hopefully of type ResponseCode.CODE.CARDS_DEALT
 	 */
 	private void displayCardsDealt(ResponseCode code) {
 		
 		if( code != null  ) {
 
 			// Our parameters
 			List<String> params = code.getParameters();
 
 			// All player update messages start the same
 			StringBuilder str = createStringBuilderForUserUpdate(params);
 			
 			// And now add the cards, one by one
 			str.append( " has the following cards:" );
 			
 			// Try to line everything up at a string length of 40, at this point
 			while( str.length() < 40 ) {
 				str.append( " " );
 			}
 			
 			// Need to add the cards, one by one
 			if( params != null && params.size() >= 3 ) {
 				StringBuilder cardString = new StringBuilder();
 				for( int i = 2; i < params.size(); i++ ) {
 					cardString.append( " " );
 					cardString.append( params.get(i) );
 				}
 				str.append( cardString.toString() );
 				
 				// Also, if it's the current user, save their cards
 				// username comes in the second parameter
 				String userWithCards = params.get(1);
 				if( userWithCards != null && userWithCards.equals(username) ) {
 					cards = cardString.toString();
 				}
 			}
 			
 			// Display to the screen
 			updateStatus( str.toString() );
 		}
 	}
 
 
 	/**
 	 * This handles codes about cards being updated, which happens
 	 * when a facedown card is turned faceup. The first 
 	 * parameter is the game ID. The second parameter is the username.
 	 * The rest are the cards. This is all as per
 	 * the protocol defintion.
 	 * 
 	 * @param code Hopefully of type ResponseCode.CODE.UPDATED_HAND
 	 */
 	private void displayUpdatedHand(ResponseCode code) {
 		
 		if( code != null  ) {
 
 			// Our parameters
 			List<String> params = code.getParameters();
 
 			// All player update messages start the same
 			StringBuilder str = createStringBuilderForUserUpdate(params);
 			
 			// And now add the cards, one by one
 			str.append( "'s facedown card is revealed:" );
 			
 			// Try to line everything up at a string length of 40, at this point
 			while( str.length() < 40 ) {
 				str.append( " " );
 			}
 			
 			// Need to add the cards, one by one
 			if( params != null && params.size() >= 3 ) {
 				StringBuilder cardString = new StringBuilder();
 				for( int i = 2; i < params.size(); i++ ) {
 					cardString.append( " " );
 					cardString.append( params.get(i) );
 				}
 				str.append( cardString.toString() );
 				
 				// Also, if it's the current user, save their cards
 				// username comes in the second parameter
 				String userWithCards = params.get(1);
 				if( userWithCards != null && userWithCards.equals(username) ) {
 					cards = cardString.toString();
 				}
 			}
 			
 			// Display to the screen
 			updateStatus( str.toString() );
 		}
 	}
 
 
 	/**
 	 * Creates a string builder to be used for displaying messages about a
 	 * particular user, by starting it off with a line like: "<username>",
 	 * unless the username is the special DEALER_USERNAME, in which case it starts
 	 * off with "The dealer".
 	 * 
 	 * @param params The parameters. The second parameter should have the username. This
 	 * corresponds to the format of player update messages from the server
 	 * @return The stringbuilder, initialized to this lead-in phrase
 	 */
 	protected StringBuilder createStringBuilderForUserUpdate(List<String> params) {
 		// Start with their username
 		StringBuilder str = new StringBuilder();
 		if( params == null || params.size() < 2 ) {
 			str.append( GameState.UNKNOWN_USERNAME );
 		} else {
 			String username = params.get(1);
 			
 			if( username == null ) {
 				str.append( GameState.UNKNOWN_USERNAME );
 			} else if( username.equals( GameState.DEALER_USERNAME ) ) {
 				// Have to replace the whole thing
 				str = new StringBuilder( "The dealer" );
 			} else {
 				str.append( username );
 			}
 		}
 		return str;
 	}
 
 	/**
 	 * Print to the screen something about the capabilities. This is just
 	 * a debug capability to show off, in the UI, how capabilities can
 	 * vary from protocol state to protocol state. Normally, something
 	 * like this wouldn't be in the client, it's just for development.
 	 * 
 	 * @param code Hopefully of type CAPABILITIES_FOLLOW
 	 */
 	protected void displayCapabilities(ResponseCode code) {
 		
 		// Make sure this is a valid capabilities list first
 		if( code == null ||  
 				!code.hasSameCode( ResponseCode.CODE.CAPABILITIES_FOLLOW ) ) {
 			updateStatus( "Internal error, sorry. Can't display the capabilities list." );
 		} else {
 			updateStatus( "The server supports " + (code.getNumberOfLines()-1) + " protocol commands in this current state." );
 			updateStatus( "They are: " );
 			for( int i = 1; i < code.getNumberOfLines(); i++ ) {
 				updateStatus( i + ". " + code.getMultiline(i) );
 			}
 		}				
 	}
 
 	/**
 	 * This handles codes about players requesting their account balance.
 	 * The first parameter is the game ID. The second parameter is the 
 	 * username. The third parameter is the balance. This is all as per
 	 * the protocol definition.
 	 * 
 	 * @param code Hopefully of type ResponseCode.CODE.ACCOUNT_BALANCE
 	 */
 	protected void displayAccountBalance(ResponseCode code) {
 		
 		// Make sure this is a valid account balance response first
 		if( code == null ||  
 				!code.hasSameCode( ResponseCode.CODE.ACCOUNT_BALANCE ) ) {
 			updateStatus( "Internal error, sorry. Can't display the account balance." );
 		} else {
 			updateStatus( "Your account balance is $" + code.getFirstParameterAsString() + "." );
 		}
 				
 	}
 
 	/**
 	 * This handles codes about players requesting the server version.
 	 * The entirety of the text can be treated as the version string.
 	 * 
 	 * @param code Hopefully of type ResponseCode.CODE.VERSION
 	 */
 	protected void displayVersion(ResponseCode code) {
 		// Make sure this is a valid version response first
 		if( code == null ||  
 				!code.hasSameCode( ResponseCode.CODE.VERSION ) ) {
 			updateStatus( "Internal error, sorry. Can't display the version." );
 		} else {
 			updateStatus( "Server version " + code.getText().trim() );
 		}
 	}
 
 	
 	/**
 	 * Does whatever the UI needs to do in order to update the user 
 	 * as to a status, which can be displayed as a short text string.
 	 * In this implementation, it prints it out with a timestamp up
 	 * front.
 	 * 
 	 * @param str The staus to display
 	 */
 	protected void updateStatus(String str) {
 		SimpleDateFormat sdf = new SimpleDateFormat( "HH:mm:ss");
 		System.out.println( "[" + sdf.format( new Date() ) + "] " + str );
 	}
 
 	/**
 	 * Server acknowledges a quit, so we can cleanly exit the system.
 	 */
 	protected void quitTheGame() {
 		client.notifyOfShutdown();
 	}
 
 	
 	/***********************************************************************************
 	 * Interacts with the server as needed to handle various user requests
 	 **********************************************************************************/
 
 	
 	/**
 	 * Requests the version from the server, using a method
 	 * on the {@link drexel.edu.blackjack.client.out.ClientOutputToServerHelper}
 	 * class. Also prints a message to the screen so the user
 	 * knows what's going on.
 	 */
 	protected void sendVersionRequest() {
 		updateStatus( "One moment, fetching the version from the server..." );
 		helper.sendVersionRequest();
 	}
 
 	/**
 	 * Requests account balance from the server, using a method
 	 * on the {@link drexel.edu.blackjack.client.out.ClientOutputToServerHelper}
 	 * class. Also prints a message to the screen so the user
 	 * knows what's going on.
 	 */
 	protected void sendAccountRequest() {
 		updateStatus( "One moment, fetching your account balance from the server..." );
 		helper.sendAccountRequest();
 	}
 
 	/**
 	 * Requests the capabilities of the server, using a method
 	 * on the {@link drexel.edu.blackjack.client.out.ClientOutputToServerHelper}
 	 * class. Also prints a message to the screen so the user
 	 * knows what's going on.
 	 */
 	protected void sendCapabilitiesRequest() {
 		updateStatus( "One moment, fetching a list of capabilities from the server..." );
 		helper.sendCapabilitiesRequest();
 	}
 
 	/**
 	 * Either shows, or hides, the message monitor frame, which is a
 	 * GUI frame used for monitoring messages.
 	 */
 	protected void toggleMessageMonitorFrame() {
 		if( client != null ) {
 			client.toggleMessageFrame();
 		}
 	}
 
 	
 	/***********************************************************************************
 	 * This is what handles the interface that we are implementing. It takes input
 	 * and calls a method that causes the input to be processed.
 	 **********************************************************************************/
 
 	
 	@Override
 	public void receivedMessage(ResponseCode code) {
 		// Only ask an implementing class to process the message
 		// if the screen is active
 		if( isActive ) {
 			processMessage( code );
 		}
 	}
 
 	/**********************************************************************
 	 * These methods handle tracking the list of available games so that
 	 * they can be displayed to the user and, when the user picks one
 	 * to join, the proper request for joining it can be made.
 	 *********************************************************************/
 	
 	
 	/**
 	 * This helper function can read what's in a response code and,
 	 * from it, create a bunch of ClientSideGame records to track
 	 * 
 	 * @param code The response code
 	 * @return The map of the games' IDs to the games
 	 */
 	protected Map<String, ClientSideGame> generateGameMap(ResponseCode code) {
 
 		// This is what we will return
 		Map<String, ClientSideGame> map = new HashMap<String,ClientSideGame>();
 		
 		// We keep queueing up game description lines until we
 		// have reached the end for the game, then interpret them
 		String numDecks = null;
 		String minBet = null;
 		String maxBet = null;
 		String gameId = null;
 		String gameDescription = null;
 		ArrayList<String> rules = new ArrayList<String>();
 		
 		if( code != null && code.isMultilineCode() ) {
 			for( int i = 1; i < code.getNumberOfLines(); i++ ) {
 				String currentLine = code.getMultiline(i);
 				if( currentLine != null ) {
 					if( currentLine.startsWith( Game.RECORD_START_KEYWORD ) ) {
 
 						// Initialize all values
 						numDecks = minBet = maxBet = null;
 						gameId = gameDescription = null;
						// Create a new instance, otherwise created ClientSideGames will use the same instance
						rules = new ArrayList<String>();
 						
 						// Figure out the ID and description
 						// Use a tokenizer just because
 						StringTokenizer strtok = new StringTokenizer(currentLine);
 						if( strtok.hasMoreTokens() ) {
 							// First token is the word 'GAME', which we don't care about
 							strtok.nextToken();
 							if( strtok.hasMoreTokens() ) {
 								// This token, however, is the ID
 								gameId = strtok.nextToken();
 							}
 						}
 						
 						// Try to figure out what the description should be
 						int index = currentLine.indexOf( gameId, Game.RECORD_START_KEYWORD.length() );
 						gameDescription = currentLine.substring( index + gameId.length() ).trim();
 					} else if( currentLine.startsWith( Game.MAX_BET_ATTRIBUTE ) ) {
 						maxBet = currentLine.substring( Game.MAX_BET_ATTRIBUTE.length() ).trim();
 					} else if( currentLine.startsWith( Game.MIN_BET_ATTRIBUTE ) ) {
 						minBet = currentLine.substring( Game.MIN_BET_ATTRIBUTE.length() ).trim();
 					} else if( currentLine.startsWith( Game.NUM_DECKS_ATTRIBUTE ) ) {
 						numDecks = currentLine.substring( Game.NUM_DECKS_ATTRIBUTE.length() ).trim();
 					} else if( currentLine.startsWith( Game.RULE_KEYWORD ) ) {
 						rules.add( currentLine.substring( Game.RULE_KEYWORD.length() ).trim() );
 					} else if( currentLine.startsWith( Game.RECORD_END_KEYWORD ) ) {
 						// We need to create, and cache, a ClientSideGame
 						ClientSideGame game = new ClientSideGame( gameId, numDecks,
 								rules, minBet, maxBet, gameDescription );
 						map.put( gameId, game );
 					}
 				}
 			}
 		}
 		
 		return map;
 	}	
 }
