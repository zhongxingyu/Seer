 package drexel.edu.blackjack.client.screens;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.StringTokenizer;
 
 import drexel.edu.blackjack.client.BlackjackCLClient;
 import drexel.edu.blackjack.client.in.ClientInputFromServerThread;
 import drexel.edu.blackjack.client.out.ClientOutputToServerHelper;
 import drexel.edu.blackjack.server.ResponseCode;
 import drexel.edu.blackjack.server.game.Game;
 
 /**
  * This is the user interface that shows when the
  * user is waiting to join a session.
  * 
  * @author Jennifer
  */
 public class NotInSessionScreen extends AbstractScreen {
 	
 	/**********************************************************************
 	 * Private variables.
 	 *********************************************************************/
 
 	// This screen can be in one of these two states
 	private static final int MAIN_MENU	= 0;
 	private static final int JOIN_GAME	= 1;
 
 	// The different options for this menu
 	private static String JOIN_OPTION			= "J";
 	private static String VERSION_OPTION		= "V";
 	private static String CAPABILITIES_OPTION	= "C";
 	private static String QUIT_OPTION			= "Q";
 	private static String MENU_OPTION			= "?";
 	private static String ACCOUNT_OPTION		= "A";
 	private static String TOGGLE_MONITOR_OPTION	= "T";
 	private static String BACK_OPTION			= "back";
 	
 	// Which of the states the screen is in
 	private int state;
 	
 	// Once we read a list of games, need a way to map from their menu
 	// number, to their ID (which is needed for joining the session)
 	private Map<Integer,String> menuNumberToGameIdMap = new HashMap<Integer,String>();
 	
 	// When the user requests to join a game, keep track of it here. If the join
 	// request is successful the client will need to be informed which game
 	private String requestedGameId = null;
 	
 	// And keep a copy to itself for the singleton pattern
 	private static NotInSessionScreen notInSessionScreen = null;
 	
 	/**********************************************************************
 	 * Private constructor.
 	 *********************************************************************/
 	
 	private NotInSessionScreen( BlackjackCLClient client, ClientInputFromServerThread thread,
 			ClientOutputToServerHelper helper ) {
 		
 		// It starts in the ENTER_USERNAME state, but inactive
 		super(client, thread,helper);
 		setScreenType( AbstractScreen.SCREEN_TYPE.NOT_IN_SESSION_SCREEN );
 		state = MAIN_MENU;
 		setIsActive( false );
 		
 	}
 
 	/**********************************************************************
 	 * Public methods.
 	 *********************************************************************/
 	
 	@Override
 	public void processMessage(ResponseCode code) {
 
 		if( this.isActive ) {
 			// This is bad.
 			if( code == null ) {
 				reset();
 				return;
 			}
 			
 			else if( code.hasSameCode( ResponseCode.CODE.GAMES_FOLLOW ) ) {
 				displayGameList( code );
 				state = JOIN_GAME;
				displayMenu();
 			} else if( code.hasSameCode( ResponseCode.CODE.NO_GAMES_HOSTED ) ) {
 				System.out.println( "Unfortunately, no games are hosted on this server" );
 				state = MAIN_MENU;
				displayMenu();
 			} else if( code.hasSameCode( ResponseCode.CODE.JOIN_SESSION_AT_MAX_PLAYERS ) ) {
 				System.out.println( "The game you selected already has the maximum number of players." );
 				state = MAIN_MENU;
				displayMenu();
 			} else if( code.hasSameCode( ResponseCode.CODE.JOIN_SESSION_DOES_NOT_EXIST ) ) {
 				System.out.println( "That game no longer is hosted by the server. Sorry about that." );
 				state = MAIN_MENU;
				displayMenu();
 			} else if( code.hasSameCode( ResponseCode.CODE.JOIN_SESSION_TOO_POOR ) ) {
 				System.out.println( "You don't have enough money in your account to cover the minimum bet." );
 				state = MAIN_MENU;
				displayMenu();
 			} else if( code.hasSameCode( ResponseCode.CODE.SUCCESSFULLY_JOINED_SESSION ) ) {
 				// This needs to say that they successfully joined a game, and move them to the next screen
 				System.out.println( "Successfully joined the game." );
 				state = MAIN_MENU;	// Reset the internal state just in case....
 				client.setCurrentGameById( requestedGameId );
 				showNextScreen();	// Move to the next screen
 			} else {
 				super.handleResponseCode( code );
 			}
 		}
 
 	}
 
 	/**
 	 * Display the game list for the user to select from.
 	 * Save it out so that we can send the write 'JOINSESSION'
 	 * command later.
 	 */
 	private void displayGameList( ResponseCode code ) {
 
 		// Set the list of games in the client
 		Map<String,ClientSideGame> gameMap = generateGameMap(code);
 		client.setGameMap( gameMap );
 		
 		// Present to user, while tracking menu option number to game ID
 		menuNumberToGameIdMap.clear();
 		Integer menuNumber = 1;
 		for( ClientSideGame game : gameMap.values() ) {
 			System.out.println( menuNumber + ") " + game.toString() );
 			menuNumberToGameIdMap.put( menuNumber, game.getId() );
 			menuNumber++;
 		}
 	}
 
 	/**
 	 * This helper function can read what's in a response code and,
 	 * from it, create a bunch of ClientSideGame records to track
 	 * 
 	 * @param code The response code
 	 * @return The map of the games' IDs to the games
 	 */
 	private Map<String, ClientSideGame> generateGameMap(ResponseCode code) {
 
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
 						rules.clear();
 						
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
 
 	/**
 	 * Used to display whatever sort of command-line 'menu'
 	 * to the screen that is appropriate.
 	 */
 	public void displayMenu() {
 
 		if( this.isActive ) {
 			if( state == JOIN_GAME ) {
 				System.out.println( "Enter the number of the game you wish to join." );
 				System.out.println( "(Or you can type '" + BACK_OPTION + "' to go back to the previous menu.)" );
 				System.out.println( "***********************************************************" );
 			} else {
 				System.out.println( "***********************************************************" );
 				System.out.println( "                Looking for a Game Screen                  " );
 				System.out.println( "***********************************************************" );
 				System.out.println( "Please enter the letter or symbol of the option to perform:" );
 				System.out.println( JOIN_OPTION + ") Join a game" );
 				System.out.println( ACCOUNT_OPTION + ") Account balance request" );
 				System.out.println( QUIT_OPTION + ") Quit playing" );
 				System.out.println( VERSION_OPTION + ") See what version of the game is running (for debug purposes)" );
 				System.out.println( CAPABILITIES_OPTION + ") See what capabilities the game implements (for debug purposes)" );
 				System.out.println( TOGGLE_MONITOR_OPTION + ") Toggle the message monitor window (for debug purposes)" );
 				System.out.println( MENU_OPTION + ") Repeat this menu of options" );
 				System.out.println( "***********************************************************" );
 			}
 		}
 		
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
 		
 		if( notInSessionScreen == null ) {
 			notInSessionScreen = new NotInSessionScreen( client, thread, helper );
 		}
 		
 		return notInSessionScreen;
 		
 	}
 
 	@Override
 	public void reset() {
 
 		if( this.isActive ) {
 			// For us, resetting the screen involves showing the menu again
 			System.out.println( "Whoops, the user interface got confused. Let's try this again." );
 			state = MAIN_MENU;
 			displayMenu();
 		}
 		
 	}
 
 	/***********************************************************************************
 	 * These methods process user input
 	 **********************************************************************************/
 
 	@Override
 	public void handleUserInput(String str) {
 
 		if( this.isActive ) {
 			if( state == MAIN_MENU ) {
 
 				if( str == null ) {
 					reset();
 				} else if( str.trim().equals(CAPABILITIES_OPTION) ) {
 					sendCapabilitiesRequest();
 				} else if( str.trim().equals(JOIN_OPTION) ) {
 					sendListGamesRequest();
 				} else if( str.trim().equals(MENU_OPTION) ) {
 					displayMenu();
 				} else if( str.trim().equals(QUIT_OPTION) ) {
 					quit();
 				} else if( str.trim().equals(VERSION_OPTION) ) {
 					sendVersionRequest();
 				} else if( str.trim().equals(ACCOUNT_OPTION) ) {
 					sendAccountRequest();
 				} else if( str.trim().equals(TOGGLE_MONITOR_OPTION) ) {
 					toggleMessageMonitorFrame();
 				} else {
 					System.out.println( "Unrecognized user input: " + str );
 					displayMenu();
 				}
 				
 			} else if( state == JOIN_GAME ) {
 				
 				processJoinGameResponse( str );
 				
 			} else {
 				System.out.println( "Uh oh. The UI got into a weird state, resetting it for you." );
 				reset();
 			}
 		}
 
 	}
 
 	/**
 	 * This handles whatever the user typed in for joining a game.
 	 * It had better be a number that is in range!!!
 	 * 
 	 * @param str
 	 */
 	private void processJoinGameResponse(String str) {
 
 		Integer number = null;
 		if( str != null ) {
 			try {
 				number = Integer.valueOf(Integer.parseInt(str));
 			} catch( NumberFormatException e ) {
 				// Too bad, but nothing we need to report now
 			}
 		}
 		
 		// Make sure the number is in range
 		if( number != null && (number < 1 || number > menuNumberToGameIdMap.size() ) ) {
 			number = null;
 		}
 		
 		// Okay, did we get a valid number? 
 		if( number == null ) {
 			// Maybe they just wanted to go back to the previous menu?
 			if( str != null && str.equalsIgnoreCase(BACK_OPTION) ) {
 				System.out.println( "Returning you to the previous menu." );
 				state = MAIN_MENU;
 				displayMenu();
 			} else {				
 				// If not, force them to try again
 				System.out.println( "You need to enter a number from 1-" + menuNumberToGameIdMap.size() + "," );
 				System.out.println( "or '" + BACK_OPTION + "' to return to the previous menu. Try again." );
 				sendListGamesRequest();
 			}
 		} else {
 			// We got a valid number, so we need to join the game
 			String id = menuNumberToGameIdMap.get( Integer.valueOf(number) );
 			if( id == null ) {
 				System.out.println( "Whoops. Could not find the game to request due to an internal error!" );
 				reset();
 			} else {
 				sendJoinGameRequest( id );
 			}
 		}
 	}
 
 	/***********************************************************************************
 	 * Interacts with the server to handle various user requests
 	 **********************************************************************************/
 
 	private void quit() {
 		System.out.println( "One moment, informing the server of your departure..." );
 		helper.sendQuitRequest();
 	}
 
 	private void sendListGamesRequest() {
 		System.out.println( "One moment, fetching a list of games from the server..." );
 		System.out.println( "***********************************************************" );
 		System.out.println( "                  Viewing Games Screen                    " );
 		System.out.println( "***********************************************************" );
 		helper.sendListGamesRequest();
 	}
 
 	private void sendJoinGameRequest( String id ) {
 		System.out.println( "Alerting the server that you wish to join this game..." );
 		// Remember to track it internally
 		requestedGameId = id;
 		helper.sendJoinSessionRequest( id );
 	}	
 }
