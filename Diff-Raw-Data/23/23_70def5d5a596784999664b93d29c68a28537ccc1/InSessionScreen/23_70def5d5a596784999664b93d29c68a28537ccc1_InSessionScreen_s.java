 package drexel.edu.blackjack.client.screens;
 
 import java.util.Map;
 import java.util.logging.Logger;
 
 import drexel.edu.blackjack.client.BlackjackCLClient;
 import drexel.edu.blackjack.client.in.ClientInputFromServerThread;
 import drexel.edu.blackjack.client.out.ClientOutputToServerHelper;
 import drexel.edu.blackjack.client.screens.util.ClientSideGame;
 import drexel.edu.blackjack.client.screens.util.ClientSideGameStatus;
 import drexel.edu.blackjack.server.ResponseCode;
 import drexel.edu.blackjack.util.BlackjackLogger;
 
 /**
  * This is the user screen to display when someone is playing
  * the game. It should inform them of changes in game status,
  * prompt them when it's time to bet or make moves, etc.
  * 
  * @author Jennifer
  *
  */
 public class InSessionScreen extends AbstractScreen {
 
 	/**********************************************************************
 	 * Private variables.
 	 *********************************************************************/
 
 	// This screen can be in one of these three states
 	private static final int NEED_BET		= 0;
 	private static final int NEED_PLAY		= 1;
 	private static final int WATCHING_GAME	= 3;
 
 	// The different options for this menu, other than the general ones
 	// that are defined in the abstract superclass
 	private static String LEAVE_OPTION			= "L";
 	private static String INFO_OPTION			= "I";
 	
 	// Which of the states the screen is in
 	private Integer state;
 	
 	// Current bet information
 	private Integer acceptedBet = null;		// Server has verified this is your bet
 	private Integer requestedBet = null;	// This is what's being requested, but it might be denied
 	
 	// Current card hand 
 	private String cards = "NONE";
 	
 	// And keep a copy to itself for the singleton pattern
 	private static InSessionScreen inSessionScreen = null;
 	
 	// And a logger for errors
 	private final static Logger LOGGER = BlackjackLogger.createLogger(InSessionScreen.class.getName()); 
 	
 	/**********************************************************************
 	 * Private constructor and singleton access method
 	 *********************************************************************/
 	
 	
 	private InSessionScreen( BlackjackCLClient client, ClientInputFromServerThread thread,
 			ClientOutputToServerHelper helper ) {
 		
 		// It starts in the ENTER_USERNAME state, but inactive
 		super(client, thread,helper);
 		
 		setScreenType( AbstractScreen.SCREEN_TYPE.IN_SESSION_SCREEN );
 		state = WATCHING_GAME;
 		setIsActive( false );
 		
 	}
 
 	/**
 	 * This is used in the singleton pattern so that only
 	 * one not-in-session screen is instantiated at a time.
 	 * 
 	 * @param client Need a valid client
 	 * @param thread Need a valid and active client input thread
 	 * @param helper Need smoething for output
 	 * @return The not-in-session screen
 	 */
 	public static AbstractScreen getDefaultScreen( BlackjackCLClient client, 
 			ClientInputFromServerThread thread,
 			ClientOutputToServerHelper helper ) {
 		
 		if( inSessionScreen == null ) {
 			inSessionScreen = new InSessionScreen( client, thread, helper );
 		}
 		
 		return inSessionScreen;
 		
 	}
 
 
 	/**********************************************************************
 	 * Methods related to messages received from the server
 	 *********************************************************************/
 
 	
 	@Override
 	public void processMessage(ResponseCode code) {
 		if( this.isActive ) {
 			
 			synchronized( state ) {
 				// This is bad.
 				if( code == null ) {
 					reset();
 				} else if( code.hasSameCode( ResponseCode.CODE.INVALID_BET_OUTSIDE_RANGE ) ) {
 					updateStatus( "Your bet was outside the range allowed in the game." );
 					if( client.getCurrentGame() != null ) {
 						updateStatus( client.getCurrentGame().getBetRestriction() + "." );
 					}
 				} else if( code.hasSameCode( ResponseCode.CODE.INVALID_BET_TOO_POOR ) ) {
 					updateStatus( "Your account is not large enough for that bet." );
 				} else if( code.hasSameCode( ResponseCode.CODE.SUCCESSFULLY_BET ) ) {
 					updateStatus( "Your bet of $" + requestedBet + " has been deducted from your account." );
 					acceptedBet = requestedBet;
 					state = WATCHING_GAME;
 					displayMenu();
 				} else if( code.hasSameCode( ResponseCode.CODE.SUCCESSFULLY_HIT ) ) {
 					// TODO
 					updateStatus( "Need to implement the response to a successful hit." );
 					displayMenu();
 				} else if( code.hasSameCode( ResponseCode.CODE.SUCCESSFULLY_LEFT_SESSION_FORFEIT_BET) ) {
 					updateStatus( "You left the game mid-play, forfeiting $" + code.getFirstParameterAsString() + "." );
					state = WATCHING_GAME;	// Reset the internal state just in case....
 					showPreviousScreen( true );	// Move to the previous screen
 				} else if( code.hasSameCode( ResponseCode.CODE.SUCCESSFULLY_LEFT_SESSION_NOT_MIDPLAY ) ) {
 					updateStatus( "You left the game between hands, and no money was lost." );
					state = WATCHING_GAME;	// Reset the internal state just in case....
 					showPreviousScreen( true );	// Move to the previous screen
 				} else if( code.hasSameCode( ResponseCode.CODE.SUCCESSFULLY_STAND ) ) {
 					// TODO
 					updateStatus( "Need to implement the response to a successful stand." );
 					displayMenu();
 				} else if( code.hasSameCode( ResponseCode.CODE.TIMEOUT_EXCEEDED_WHILE_BETTING ) ) {
 					updateStatus( "You did not place a bet in time." );
 					updateStatus( "Removed from game but no money lost." );
					state = WATCHING_GAME;	// Reset the internal state just in case....
 					showPreviousScreen( true );	// Move to the previous screen
 				} else if( code.hasSameCode( ResponseCode.CODE.TIMEOUT_EXCEEDED_WHILE_PLAYING ) ) {
 					// TODO: Something better here
 					updateStatus( "You did not choose your play in time" );
 					updateStatus( "Removed from game and bet was lost." );
					state = WATCHING_GAME;	// Reset the internal state just in case....
 					showPreviousScreen( true );	// Move to the previous screen
 				} else if( code.hasSameCode( ResponseCode.CODE.USER_BUSTED ) ) {
 					updateStatus( "You BUSTED. That's over 21, and you have lost." );
 					state = WATCHING_GAME;
 					displayMenu();
 				} else if( code.hasSameCode( ResponseCode.CODE.REQUEST_FOR_BET ) ) {
 					state = NEED_BET;
 					displayMenu();
 				} else if( code.hasSameCode( ResponseCode.CODE.REQUEST_FOR_GAME_ACTION ) ) {
 					state = NEED_PLAY;
 					displayMenu();
 				} else if( code.hasSameCode( ResponseCode.CODE.GAME_STATUS ) ) {
 					displayGameStatus( code );
 					displayMenu();
 				} else if( code.hasSameCode( ResponseCode.CODE.GAMES_FOLLOW ) ) {
 					displayGameMetadata( code );
 				} else {
 					super.handleResponseCode( code );
 				}
 			}
 		}
 	}	
 	
 	/**
 	 * Received the LISTGAMES response, which has descriptions
 	 * of every single game. Need to find the one we're interested
 	 * in, and show a short line about it.
 	 * 
 	 * @param code The game response code
 	 */
 	private void displayGameMetadata(ResponseCode code) {
 
 		// We get a list of games, not just our own
 		Map<String,ClientSideGame> gameMap = generateGameMap(code);
 		
 		// Though we're only interested in our own
 		ClientSideGame ourGame = null;
 		if( client != null && client.getCurrentGame() != null ) {
 			ourGame = gameMap.get( client.getCurrentGame().getId() );
 		}
 		
 		if( ourGame == null ) {
 			LOGGER.warning( "Cannot find general info about the game to display." );
 		} else {
 			StringBuilder str = new StringBuilder( "Our game: " );
 			str.append( ourGame.toString() );
 			this.updateStatus( str.toString() );
 		}
 	}
 
 	/**
 	 * Received some game status from the system. Need to display
 	 * it to the user
 	 * 
 	 * @param code The game status
 	 */
 	private void displayGameStatus(ResponseCode code) {
 		
 		if( code != null ) {
 			ClientSideGameStatus gameStatus = new ClientSideGameStatus(code);
 			updateStatus( gameStatus.getSummaryStatus() );
 			for( String username : gameStatus.getUsernames() ) {
 				updateStatus( gameStatus.getStatusForUser(username) );
 			}
 		}
 	}
 
 	/**********************************************************************
 	 * General methods related to showing menus and getting responses.
 	 *********************************************************************/
 
 	
 	@Override
 	public void displayMenu() {
 		
 		if( this.isActive ) {
 			synchronized(state) {
 				if( state == NEED_BET ) {
 					System.out.println( "***********************************************************" );
 					System.out.println( "                 Making a Bet Screen                       " );
 					System.out.println( "***********************************************************" );
 					System.out.println( "How much would you like to bet?" );
 					if( client.getCurrentGame() != null ) {
 						System.out.println( "(" + client.getCurrentGame().getBetRestriction() + ".)" );
 					}
 					StringBuilder str = new StringBuilder( "Enter amount, '" );
 					str.append( ACCOUNT_OPTION );
 					str.append( "' for balance, '" );
 					str.append( LEAVE_OPTION );
 					str.append( "' to leave, '" );
 					str.append( INFO_OPTION );
 					str.append( "' for game info." );
 					System.out.println( str.toString() );
 					System.out.println( "***********************************************************" );
 				} else if( state == NEED_PLAY ) {
 					// TODO: Yeah
 					reset();
 				} else {
 					System.out.println( "***********************************************************" );
 					System.out.println( "                 Playing Blackjack Screen                  " );
 					System.out.println( "***********************************************************" );
 					System.out.println( playingBlackjackStatusLine() );
 					System.out.println( "***********************************************************" );
 					System.out.println( "Please enter the letter or symbol of the option to perform:" );
 					System.out.println( LEAVE_OPTION + ") Leave the game" );
 					System.out.println( ACCOUNT_OPTION + ") Account balance request" );
 					System.out.println( INFO_OPTION + ") Info about the game" );
 					System.out.println( VERSION_OPTION + ") See what version of the game is running (for debug purposes)" );
 					System.out.println( CAPABILITIES_OPTION + ") See what capabilities the game implements (for debug purposes)" );
 					System.out.println( TOGGLE_MONITOR_OPTION + ") Toggle the message monitor window (for debug purposes)" );
 					System.out.println( MENU_OPTION + ") Repeat this menu of options" );
 					System.out.println( "***********************************************************" );
 				}
 			}
 		}	
 	}
 	
 	/**
 	 * Generate a fixed-width line that reports the cards held, 
 	 * and the current bet placed.
 	 * 
 	 * @return Fixed length string of this
 	 */
 	private String playingBlackjackStatusLine() {
 		
 		// We might just have the word NONE for the cards, might have some real values
 		// We really don't want it to be null, though
 		if( cards == null ) {
 			cards = "NONE";
 		}
 		StringBuilder str = new StringBuilder( "* Cards: " );
 		str.append( cards );
 		
 		// Now, the length of the cards string, plus number of spaces, should equal 20
 		for( int i = 0; i < (20-cards.length()); i++ ) {
 			str.append( " " );
 		}
 		
 		// Come up with a string for the bet part
 		String bet = "Bet: " + (acceptedBet == null ? "NONE" : "$" + acceptedBet.toString() );
 		
 		// We need to fill 28 spaces to the end of the bet line
 		for( int i = 0; i < (28-bet.length()); i++ ) {
 			str.append( " " );
 		}
 		str.append( bet );
 		
 		// And finally end it off
 		str.append( " *" );
 		
 		return str.toString();
 	}
 
 	@Override
 	public void reset() {
 		if( this.isActive ) {
 			// For us, resetting the screen involves showing the menu again
 			updateStatus( "Whoops, the user interface got confused. Let's try this again." );
 			state = WATCHING_GAME;
 			displayMenu();
 		}
 	}
 
 
 	/***********************************************************************************
 	 * These methods process user input
 	 **********************************************************************************/
 
 
 	@Override
 	public void handleUserInput(String str) {
 		if( this.isActive ) {
 
 			synchronized( state ) {
 				
 				if( str == null ) {
 					reset();
 				} else if( str.trim().equalsIgnoreCase(CAPABILITIES_OPTION) ) {
 					sendCapabilitiesRequest();
 				} else if( str.trim().equalsIgnoreCase(TOGGLE_MONITOR_OPTION) ) {
 					toggleMessageMonitorFrame();
 				} else if( str.trim().equalsIgnoreCase(MENU_OPTION) ) {
 					displayMenu();
 				} else if( str.trim().equalsIgnoreCase(INFO_OPTION) ) {
 					sendGameStatusRequest();
 				} else if( str.trim().equalsIgnoreCase(VERSION_OPTION) ) {
 					sendVersionRequest();
 				} else if( str.trim().equalsIgnoreCase(ACCOUNT_OPTION) ) {
 					sendAccountRequest();
 				} else if( str.trim().equalsIgnoreCase(LEAVE_OPTION) ) {
 					sendLeaveGameRequest();
 				} else if( state == NEED_BET ) {
 					interpretUserBet( str );
 				} else if( state == NEED_PLAY ) {
 					// TODO: Yeah
 					reset();
 				} else {
 					updateStatus( "Unrecognized user input: " + str );
 					displayMenu();
 				}
 			}
 		}
 	}
 
 
 	/***********************************************************************************
 	 * Interacts with the server to handle various user requests
 	 **********************************************************************************/
 
 	
 	/**
 	 * Sends a request to leave the session. No prompting
 	 * of the user to confirm, sucks to be them.
 	 */
 	private void sendLeaveGameRequest() {
 		updateStatus( "Exiting you from the game now..." );
 		helper.sendLeaveSessionRequest();
 	}
 
 	/**
 	 * Upon being requested to enter a bet, the user entered
 	 * something. Hopefully it's a number...
 	 */
 	private void interpretUserBet( String str ) {
 		if( str == null ) {
 			reset();
 		} else {
 			// Try to interpret the amount as a number
 			Integer bet = null;
 			try {
 				bet = Integer.parseInt(str.trim());
 				updateStatus( "One moment, submitting your bet to the dealer..." );
 				requestedBet = bet;
 				helper.sendBetRequest( bet );
 			} catch( NumberFormatException e ) {
 				updateStatus( "You need to enter a number for the bet amount." );
 				displayMenu();
 			}
 		}
 	}
 
 	/**
 	 * Sends a request for game status. This is actually two requests:
 	 * LISTGAMES to get the static information (and when it's processed
 	 * here, it will just need to show the information for this one
 	 * game, not ALL of them) and GAMESTATUS for this game
 	 */
 	private void sendGameStatusRequest() {
 		updateStatus( "Requesting current game status from the server..." );
 		helper.sendListGamesRequest();
 		if( getSessionId() == null ) {
 			LOGGER.severe( "Cannot send a request for game status because the game ID wasn't recorded anywhere." );
 		} else {
 			helper.sendGameStatusRequest( getSessionId() );
 		}
 	}
 
 	
 	/***********************************************************************************
 	 * Odds and ends
 	 **********************************************************************************/
 	
 	/**
 	 * Gets the session ID to use in various commands. This should
 	 * be the game ID of whatever game is being played in this UI.
 	 * 
 	 * @return The session ID / session name / game ID
 	 */
 	private String getSessionId() {
 		
 		// Hopefully they remembered to set the game!
 		if( client != null && client.getCurrentGame() != null ) {
 			return client.getCurrentGame().getId();
 		}
 		
 		// IF we can't find it, return null
 		return null;
 	}
 	
 }
