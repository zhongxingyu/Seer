 /*******************************************************************************
  * CS544 Computer Networks Spring 2013
  * 5/26/2013 - BlackjackProtocol.java
  * Group Members
  * o Jennifer Lautenschlager
  * o Constantine Lazarakis
  * o Carol Greco
  * o Duc Anh Nguyen
  * 
  * Purposes: This is the object that represents the protocol state of the
  * server-client connection. One protocol object exists for every client
  * connection. It holds stateful information such as which (DFA) state the
  * protocol is in, as well as attributes associated with the state, such as
  * which logged-in user is associated with it. Finally, it is the 'router'
  * for input from the client, passing it off to the appropriate implemented
  * command using the Command pattern.
  ******************************************************************************/
 package drexel.edu.blackjack.server;
 
 import drexel.edu.blackjack.server.commands.BlackjackCommand;
 import drexel.edu.blackjack.server.commands.CommandMetadata;
 import drexel.edu.blackjack.server.game.User;
 import drexel.edu.blackjack.util.BlackjackLogger;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 import java.util.logging.Logger;
 
 /**
  * <b>STATEFUL:</b> Our protocol class is where we keep 
  * track of the protocol state, along with any stateful
  * variables. These variables include, for example, which 
  * user is authenticated in a particular instantiation of
  * the protocol. The user is initially null until they
  * authenticate. There are other local variables too, such
  * as the amount bet, or the number of incorrect login
  * attempts made.
  * 
  * All state information is kept associated with the
  * protocol object. When a command is called on the
  * protocol option, the command itself is stateless.
  * It just queries the protocol object it's passed for
  * the state. In this way, one instantiation of the
  * command option can handle an arbitrary number of
  * different protocols' commands.
  * 
  * Pretty much everything in this class has to do with
  * our requirement for being stateful.
  */
 public class BlackjackProtocol {
 
 	/*************************************************************
 	 * These static variables are saved across all protocol
 	 * classes that are instantiated.
 	 ************************************************************/
 	
 	// Only want to initialize it once
 	private static boolean isInitialized					= false;
 	
 	// Keep track of incorrect login attempts
 	private int incorrectLogins = 0;
 	
 	// This file should be in the classpath
 	private static String COMMAND_FILENAME					= "commands.txt";
 	
 	// We keep a map of command classes to the command words that invoke them. We'll hash
 	// on an all lowercase version of the command word, since our server is case-insensitive
 	private static Map<String,BlackjackCommand> commands	= new HashMap<String,BlackjackCommand>();
 	
 	// This is the command to invoke if we can't find a valid command
 	private static BlackjackCommand unknownCommand = null;
 
 	// And our logger
 	private final static Logger LOGGER = BlackjackLogger.createLogger(BlackjackProtocol.class.getName()); 
 
 	/*************************************************************
 	 * The enumeration of protocol states
 	 ************************************************************/
 	public enum STATE {
 		
 		/**
 		 * The client connected but hasn't supplied a username yet
 		 */
 		WAITING_FOR_USERNAME,
 		
 		/**
 		 * The client connected and has given a username, but needs to give a password
 		 * username variable holds the username
 		 */
 		WAITING_FOR_PASSWORD,
 		
 		/**
 		 * The client has authenticated but is not in a session
 		 * In this and all states below, the user variable should be set
 		 */
 		NOT_IN_SESSION,	
 		
 		/**
 		 * The user has joined a session, but there's a game in progress and they
 		 * aren't playing, they have to wait until the next round of betting
 		 */
 		IN_SESSION_AS_OBSERVER,
 		
 		/**
 		 * The server has requested that the client give its bet
 		 */
 		IN_SESSION_AWAITING_BETS,
 		
 		/**
 		 * Bets have been made and cards are being dealt and other
 		 * players may be taking their turn. The server is not waiting 
 		 * for any client input. The bet variable should be set to the 
 		 * value of the bet
 		 */
 		IN_SESSION_BEFORE_YOUR_TURN,
 		
 		/**
 		 * This is after the cards are dealt, and the dealer doesn't have
 		 * a blackjack. Now waiting for client input as to whether
 		 * they HIT or STAND. The bet variable should be set to the value of the bet.
 		 */
 		IN_SESSION_AND_YOUR_TURN,
 		
 		/**
 		 * In this case, the dealer has a blackjack. Too bad for the
 		 * client! The thread has to do some bookkeeping and is not
 		 * expecting client input. The bet value should be set to 
 		 * what the client bet.
 		 */
 		IN_SESSION_DEALER_BLACKJACK,
 		
 		/**
 		 * The dealer didn't have blackjack, and the client already 
 		 * played out their hand. It's just waiting for others to 
 		 * finish playing now. The bet value should be set to what
 		 * the client bet.
 		 */
 		IN_SESSION_AFTER_YOUR_TURN,
 		
 		/**
 		 * Now everyone is done playing and the server is processing
 		 * the results. The bet value should be set to what the client bet.
 		 */
 		IN_SESSION_SERVER_PROCESSING,
 				
 		/**
 		 * This is, like it says, the state of being disconnected. Not much
 		 * exciting happens in this state.
 		 */
 		DISCONNECTED
 
 	}
 
 	/*************************************************************
 	 * Each instantiation of the protocol have these local
 	 * variables.
 	 ************************************************************/
 	
 	// This is null until we're authenticated, then it points to the user associated
 	// with this instance of the protocol
 	private User user = null;
 	
 	// This is the state that the protocol connection is in.
 	private STATE state = null;
 	
 	// If the client is in the state where it's waiting for a password,
 	// the username attribute is set to what they said as a username.
 	private String username = null;
 	
 	// If the client has made a bet that hasn't been processed, this 
 	// will reflect the amount bet.
 	private Integer bet = null;
 	
 	// Pointer to the server thread, for sending stuff
 	// CONCURRENT: Since there are multiple protocol instantiations
 	// they will correspond to multiple server threads, which handle
 	// the socket traffic
 	private BlackjackServerThread thread = null;
 	
 	
 	/******************************************************************
 	 * Need to track their last command received, so they can be
 	 * timed out
 	 *****************************************************************/
 	
 	// This attribute is the last time the client did any sort of
 	// command, even if it was pure garbage. It's system time in
 	// milliseconds
 	private Long lastCommand = null;
 	
 	/*************************************************************
 	 * Constructor goes  here
 	 ************************************************************/
 	
 	/**
 	 * We initialize the protocol with the list of commands
 	 * that it knows how to handle, if that hasn't been done
 	 * already. Since everyone can share that list, it's done
 	 * as static variables.
 	 * 
 	 * @param thread The thread that handles the connection
 	 * for this protocol state
 	 */
 	public BlackjackProtocol( BlackjackServerThread thread ) {
 		
 		// Only initialize it once
 		if( !isInitialized ) {
 			LOGGER.info( "About to initialize the commands" );
 			isInitialized = initializeCommands();
 			if( !isInitialized ) {
 				LOGGER.severe( "Could not initialize config file for the protocol. This is probably bad..." );
 			}
 		}
 		
 		// When we start up, we're waiting for the username, that's the first state
 		state = STATE.WAITING_FOR_USERNAME;
 		
 		// Keep a pointer to the thread
 		this.thread = thread;
 		
 		// And we start the timer
 		setLastCommand( System.currentTimeMillis() );
 	}
 
 	/*************************************************************
 	 * Public methods go here
 	 ************************************************************/
 	
 	/**
 	 * Return a list of all the valid commands that the protocol
 	 * can process. The CAPABILITIES command in particular will
 	 * be interested in this.
 	 * 
 	 * @return A set of all commands that are valid in at least
 	 * one state.
 	 */
 	public Set<BlackjackCommand> getAllValidCommands() {
 		
 		if( commands == null ) {
 			return null;
 		}
 		
 		// Not sure if I can cast i like that....
 		return new HashSet<BlackjackCommand>( commands.values() );
 	}
 	
 	/*************************************************************
 	 * Private methods go here
 	 ************************************************************/
 
 	/**
 	 * Instead of being hard-coded, the list of commands is in a 
 	 * config file. The file contains full-path class names. So
 	 * this method reads the file, and uses reflection to
 	 * instantiate those classes.
 	 * 
 	 * @return True if successfully initialized, else false
 	 */
 	private boolean initializeCommands() {
 		
 		// The file of commands should be on the classpath
 		InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream( COMMAND_FILENAME );
 		BufferedReader reader = new BufferedReader( new InputStreamReader(inputStream) );
 		
 		try {
 			// Keep reading lines in the file to process
 			String line = reader.readLine();
 			while( line != null ) {
 				
 				// Process anyline that isn't blank or starting with '#'
 				if( line.trim().length() > 0 && !line.startsWith("#") ) {
 					
 					// This should have a class name that we need to instantiate
 					String classname = line.trim();
 
 					try {
 						// This uses java reflection to get the class object offf the name
 						Class<?> c = Class.forName( classname );
 						
 						// Make sure class is something that extends BlackjackComand
 						if( !BlackjackCommand.class.isAssignableFrom(c) ) {
 							LOGGER.warning( c.getCanonicalName() + " did not subclass BlackjackCommand and couldn't be used." );
 							LOGGER.warning( "Will continue, but that command won't work on the server." );
 						} else {
 						
 							Object obj = c.newInstance();						
 							BlackjackCommand command = (BlackjackCommand)obj;
 							
 							// Now we hash it using its command word -- unless it's a null, which
 							// means it's the special "we don't know what to do" command.
 							if( command.getCommandWord() == null ) {
 								// Of course, if we already had something designated with a null, well, that's bad
 								if( unknownCommand != null ) {
 									LOGGER.warning( "Had two specified command define themselves as the 'unknown' command." );
 									LOGGER.warning( "First one was: " + unknownCommand.getClass().getName() );
 									LOGGER.warning( "Second one was: " + command.getClass().getName() );
 									LOGGER.warning( "Will ignore the second one and use the first." );
 								} else {
 									// But if we didn't have something designated with a null, we do now
 									unknownCommand = command;
 								}
 							} else {
 								
 								// Remember that it's case-insensitive, so convert to all lowercase
 								String commandWord = command.getCommandWord().toLowerCase();
 								
 								// IF we already hashed it, that's bad
 								if( commands.get( commandWord ) != null ) {
 									BlackjackCommand originalCommand = commands.get( commandWord );
 									LOGGER.warning( "Had two specified command define their command word as: " + commandWord );
 									LOGGER.warning( "First one was: " + originalCommand.getClass().getName() );
 									LOGGER.warning( "Second one was: " + command.getClass().getName() );
 									LOGGER.warning( "Will ignore the second one and use the first." );
 								} else {
 									// At this point, it's unique in its command word, so put it in the map
 									commands.put( commandWord, command );
 								}
 							}
 						}
 					} catch (InstantiationException e) {
 						LOGGER.warning( "Could not instantiate class " + classname );
 						LOGGER.warning( "Will continue, but that command won't work on the server." );
 						e.printStackTrace();
 					} catch (IllegalAccessException e) {
 						LOGGER.warning( "Could not access class " + classname );
 						LOGGER.warning( "Will continue, but that command won't work on the server." );
 						e.printStackTrace();
 					} catch (ClassNotFoundException e) {
 						LOGGER.warning( "Could not find class " + classname );
 						LOGGER.warning( "Will continue, but that command won't work on the server." );
 						e.printStackTrace();
 					}
 				}
 				
 				// Read the next line
 				line = reader.readLine();
 			}
 		} catch (IOException e ) {
 			LOGGER.severe( "Had a problem reading the command file. The server probably won't work." );
 			e.printStackTrace();
 			return false;
 		}
 		
 		// Recap what was read in
 		LOGGER.info( "We do " + (unknownCommand == null ? "NOT " : "") + "have an unknown command set." );
 		LOGGER.info( "We have " + commands.size() + " commands loaded successfully." );
 		// Assume if you got this far, that it's all good
 		return true;
 	}
 
 	/*************************************************************
 	 * Protected methods go here
 	 ************************************************************/
 
 	/**
 	 * This is how we handle messages. This method is called whenever
 	 * a single-line command is received at the server. This
 	 * method is responsible for deserializing the command information
 	 * into a {@link CommandMetadata} object. It then uses a map
 	 * that matches command words, to the Command class that 
 	 * implements handling the command. Assuming it finds one,
 	 * it hands off the command metadata, and this protocol
 	 * state, to the object to process. Otherwise, it uses a
 	 * special UnknownCommand class to handle what to do when
 	 * a command that isn't recognized is received.
 	 * 
 	 * @param inputLine The message as received from the client
 	 * @return The message that should be sent back to the client
 	 */
 	protected String processInput(String inputLine) {
 		// This basically parses out the command word, and the parameters
 		CommandMetadata metadata = new CommandMetadata(inputLine);
 
 		// We'll keep whatever command we decide to use here
 		BlackjackCommand command = null;
 
 		// We use the command word to look up the command to use
 		String commandWord = metadata.getCommandWord();
 		if( commandWord == null ) {
 			LOGGER.info( "Command word was null. That's pretty odd." );
 		} else {
 			// Make it lowercase as we're case-insensitive, and hashed them in lowercase
 			commandWord = commandWord.toLowerCase();
 			command = commands.get( commandWord );
 		}
 		
 		// If we can't find it, we have to use our unknown command. Hope it's not null!
 		if( command == null ) {
 			command = unknownCommand;
 		}
 		
 		// We are going to use this response if we had a null command, at this point
 		String response = "Received an unknown command, but had no command handler defined for it.";
 		if( command != null ) {
 			response = command.processCommand( this, metadata );
 		}
 		
 		// Update the timer
 		setLastCommand( System.currentTimeMillis() );
 		
 		// Whatever we have, we return
 		return response;
 	}
 
 	/**
 	 * Get a map of command words, to the Command classes that
 	 * implement responding to them.
 	 * 
 	 * @return A map of command words to their implementing
 	 * command class
 	 */
 	public static Map<String, BlackjackCommand> getCommands() {
 		return commands;
 	}
 
 	/**
 	 * Set a map of command words, to the Command classes that
 	 * implement responding to them.
 	 * 
 	 * @param commands the commands to set
 	 */
 	public static void setCommands(Map<String, BlackjackCommand> commands) {
 		BlackjackProtocol.commands = commands;
 	}
 
 	/**
 	 * Get the user, which is a stateful variable associated with
 	 * an instantiated protocol session. This refers to a user who
 	 * has successfully authenticated themselves. 
 	 * 
 	 * @return The authenticated user, or null if there is no
 	 * properly authenticated user
 	 */
 	public User getUser() {
 		return user;
 	}
 
 	/**
 	 * Set the user, which is a stateful variable associated with
 	 * an instantiated protocol session. This refers to a user who
 	 * has successfully authenticated themselves. 
 	 * @param user The authenticated user, or null if there is no
 	 * properly authenticated user
 	 */
 	public void setUser(User user) {
 		// Don't just set them here, insert a reference to their server thread
 		this.user = user;
 		user.setBlackjackServerThread( thread );
 	}
 
 	/**
 	 * Get the state of the protocol
 	 * @return the state
 	 */
 	public STATE getState() {
 		return state;
 	}
 
 	/**
 	 * Set the state of the protocol
 	 * @param state the state to set
 	 */
 	public void setState(STATE state) {
 		this.state = state;
 	}
 
 	/**
 	 * Get the username that is associated with this protocol
 	 * state. It is stored on the protocol after the USERNAME
 	 * command has specified a username, but before the PASSWORD
 	 * command has specified the password so that the authentication
 	 * can be attempted. It should be set in the {@link STATE#WAITING_FOR_PASSWORD}
 	 * state.
 	 * 
 	 * @return the username
 	 */
 	public String getUsername() {
 		return username;
 	}
 
 	/**
 	 * Set the username that is associated with this protocol
 	 * state. It is stored on the protocol after the USERNAME
 	 * command has specified a username, but before the PASSWORD
 	 * command has specified the password so that the authentication
 	 * can be attempted.
 	 */
 	public void setUsername(String username) {
 		this.username = username;
 	}
 
 	/**
 	 * Set the bet that is associated with this protocol
 	 * state. It is stored on the protocol after the BET
 	 * command has specified a value, so that if the
 	 * user wins they can have the appropriate amount
 	 * credited to their account. It should be set
 	 * in the {@link STATE#IN_SESSION_BEFORE_YOUR_TURN},
 	 * {@link STATE#IN_SESSION_AFTER_YOUR_TURN},
 	 * {@link STATE#IN_SESSION_AND_YOUR_TURN},
 	 * {@link STATE#IN_SESSION_DEALER_BLACKJACK},
 	 * and {@link STATE#IN_SESSION_SERVER_PROCESSING}
 	 * states.
 	 * 
 	 * @return the bet
 	 */
 	public Integer getBet() {
 		return bet;
 	}
 
 	/**
 	 * Get the bet that is associated with this protocol
 	 * state. It is stored on the protocol after the BET
 	 * command has specified a value, so that if the
 	 * user wins they can have the appropriate amount
 	 * credited to their account.
 	 * 
 	 * @param bet the bet to set
 	 */
 	public void setBet(Integer bet) {
 		this.bet = bet;
 	}
 
 	/**
 	 * Get the timestamp (expressed in system time as
 	 * number of milliseconds since 1970) of when the 
 	 * last command was received through this protocol.
 	 * It is used so that the idle disconnect daemon
 	 * can detect connections that have been idle for
 	 * too long.
 	 * 
 	 * @return the lastCommand Number of milliseconds since
 	 * 1970 since the last command (successful or not)
 	 * was received
 	 */
 	public Long getLastCommand() {
 		return lastCommand;
 	}
 
 	/**
 	 * Set the timestamp (expressed in system time as
 	 * number of milliseconds since 1970) of when the 
 	 * last command was received through this protocol.
 	 * It is used so that the idle disconnect daemon
 	 * can detect connections that have been idle for
 	 * too long.
 	 * 
 	 * @param lastCommand the lastCommand Number of milliseconds since
 	 * 1970 since the last command (successful or not)
 	 * was received
 	 */
 	public void setLastCommand(Long lastCommand) {
 		this.lastCommand = lastCommand;
 	}
 
 	/**
 	 * A count is kept of incorrect login attempts, as
 	 * a state variable. If it exceeds the MAX_INVALID_LOGINS, 
 	 * the session is ended. This state variable is used
 	 * to responding to incorrect PASSWORD messages, in the
 	 * {@link STATE#WAITING_FOR_PASSWORD} state.
 	 */
 	public void incrementIncorrectLogins() {
 		incorrectLogins++;
 	}
 
 	/**
 	 * Get the number of incorrect logins recorded
 	 * at this point in time.
 	 * 
 	 * @return Number of incorrect logins
 	 */
 	public int getIncorrectLogins() {
 		return incorrectLogins;
 	}
 }
