 package drexel.edu.blackjack.server;
 
 import java.util.StringTokenizer;
 import java.util.regex.Pattern;
 
 /**
  * This class is used to generate or interpret response
  * codes. It's mostly a set of convenience functions, though
  * by encapsulating the functionality we can change codes
  * in just one place if we get them wrong.
  * 
  * @author Jennifer
  */
 public class ResponseCode {
 
 	/***************************************************************
 	 * Code definitions go here
 	 **************************************************************/
 	
 	/**
 	 * This lists all the response codes, with the enumeration value they are
 	 * known by, the code (as a 3-digit number), and the default message.
 	 * 
 	 * NOTE THAT IF THE DEFAULT MESSAGE IS NULL THE CONSTRUCTOR WHERE YOU
 	 * JUST PASS IN A CODE ENUMERATION CANNOT BE USED.
 	 * 
 	 * This is becuse there's a parameter expected, and then optional
 	 * text. So either: a) use the constructor where you specify the
 	 * code AND the overridden text, which you can use to specify
 	 * the parameter(s); or,b) use a convenience function to generate
 	 * the response code.
 	 */
 	public enum CODE {
 		
 		CAPABILITIES_FOLLOW ( 101, "Capabilities list follows." ),
 		GAMES_FOLLOW ( 102, null ),
 		VERSION ( 103, null ),
 		ACCOUNT_BALANCE ( 104, null ),
 		NO_GAMES_HOSTED ( 105, "There are no games hosted on this server." ),
 		// TODO: Shouldn't these next to be in the 'client error' range of codes
 		TIMEOUT_EXCEEDED_WHILE_BETTING ( 106, "Client idle too long while server was expecting a BET." ),
 		TIMEOUT_EXCEEDED_WHILE_PLAYING ( 107, "Client idle too long while server was expecting HIT or STAND." ),
 		
 		SUCCESSFULLY_AUTHENTICATED( 200, "The username and password were correct. Welcome to the game!" ),
 		SUCCESSFULLY_QUIT( 201, "Come back soon!" ),
 		SUCCESSFULLY_JOINED_SESSION( 211, "Successfully joined game session." ),
 		SUCCESSFULLY_BET( 220, "Bet successfully placed. Good luck!" ),
 		SUCCESSFULLY_LEFT_SESSION_NOT_MIDPLAY( 221, "Successfully left the game session. No bet was forfeited." ),
 		SUCCESSFULLY_LEFT_SESSION_FORFEIT_BET( 222, null ),
 		SUCCESSFULLY_STAND( 223, "Okay, you stand. No more cards will be dealt." ),
 
 		WAITING_FOR_PASSWORD( 300, "User acknowledged. Send PASSWORD." ),
 		// TODO: Shouldn't this be in the 200 block? Where SUCCESSFULLY_STAND is?
 		SUCCESSFULLY_HIT( 320, null ),
 
 		INTERNAL_ERROR ( 400, "An internal error occured in the server." ),
 		NEED_TO_BE_AUTHENTICATED( 401, "The client must authenticate before using this command." ),
 		INVALID_LOGIN_CREDENTIALS( 402, "The username and password are incorrect." ),
 		NOT_EXPECTING_PASSWORD( 403, "Server was not expected to receive a PASSWORD command just now." ),
 		NOT_EXPECTING_USERNAME( 404, "Server was not expected to receive a USERNAME command just now." ),
 		JOIN_SESSION_DOES_NOT_EXIST( 410, "Tried to join a non-existent game session." ),
 		JOIN_SESSION_AT_MAX_PLAYERS( 411, "Cannot join a session at the maximum number of players." ),
 		JOIN_SESSION_TOO_POOR( 412, "Cannot join the session as bank account is too low." ),
 		USER_NOT_IN_GAME_ERROR( 413, "That comman can't be used if not in a game session." ),
 		INVALID_BET_NOT_EXPECTED( 420, "The server was not expecting a BET command now. Be patient." ),
 		INVALID_BET_OUTSIDE_RANGE( 421, "That bet amount is either below the minimum or above the maximum allowed." ),
 		INVALID_BET_TOO_POOR( 422, "That bet amount is more than the user's account balance." ),
 		USER_BUSTED( 423, null ),
 		NOT_EXPECTING_HIT( 424, "The server was not expecting a HIT command now. Be patient." ),
 		NOT_EXPECTING_STAND( 425, "The server was not expecting a STAND command now. Be patient." ),
 
 		UNKNOWN_COMMAND( 500, "That command is unknown to the server." ),
 		UNSUPPORTED_COMMAND( 501, "That command is not supported on this server." ),
		SYNTAX_ERROR( 502, "That command had a syntax error." )
		
 		INFORMATIVE_MESSAGE( 600, null ),
 		USER_RESPONSE_NEEDED_MESSAGE( 601, null );
 
 		// 3-digit response code
 		private final int code;
 		// A somewhat human-understandable explanation
 		private final String message;
 		// Simple constructor
 		CODE( int code, String message ) {
 			this.code = code;
 			this.message = message;
 		}
 		
 		public int getCode() {
 			return code;
 		}
 		
 		public String getCodeAsString() {
 			return Integer.valueOf(code).toString();
 		}
 		
 		public String getMessage() {
 			return message;
 		}
 
 	}
 
 	/***************************************************************
 	 * Local variables go here
 	 **************************************************************/
 	
 	// The code is the 3-digit number, represented as an integer
 	private Integer code = null;
 	
 	// And here is the optional text -- sometimes it's parameters, 
 	// sometimes it's just extra text, it depends on the error code
 	private String text = null;
 	
 	/***************************************************************
 	 * Static variables here
 	 **************************************************************/
 
 	// This defines a regular expression for validating a response is valid
 	// (e.g., it begins with a 3-digit number)
 	private static Pattern validResponsePattern;
 	
 	/***************************************************************
 	 * Constructors!
 	 **************************************************************/
 
 	/**
 	 * Constructs an empty response code. This is
 	 * purposefully private so others don't use it.
 	 */
 	private ResponseCode() {
 	}
 	
 	/**
 	 * Constructs a type of response code with the
 	 * default message. If the code doesn't have a
 	 * default message, then an exception is thrown.
 	 * 
 	 * @param code Must be non-null, and must be a code
 	 * that has a default message.
 	 * @throws IllegalArgumentException If the code is null
 	 * OR if the code requires a message to be specified
 	 */
 	public ResponseCode( CODE code ) {
 
 		// Make sure the argument is correct
 		if( code == null ) {
 			throw new IllegalArgumentException( "The code parameter cannot be null." );
 		}
 		if( code.getMessage() == null ) {
 			throw new IllegalArgumentException( "The code used requires you to use " +
 						"the constructor where you specify a message, for the parameter" );
 		}
 		
 		this.code = code.getCode();
 		this.text = code.getMessage();
 	}
 	
 	/**
 	 * Constructs a type of response code, overriding
 	 * the message. If you don't want to include the
 	 * default message, and don't want to include any
 	 * message, pass in a null
 	 * 
 	 * @param code A valid code
 	 * @param message The message to include after it.
 	 * Could be null if you don't want a message
 	 */
 	public ResponseCode( CODE code, String message ) {
 
 		if( code == null ) {
 			throw new IllegalArgumentException( "The code parameter cannot be null." );
 		}
 		
 		this.code = code.getCode();
 		this.text = message;
 	}
 	
 	/***************************************************************
 	 * Public methods for moving between strings and ResponseCode
 	 * objects go here
 	 **************************************************************/
 	
 	/**
 	 * Given an instantiated response code, generate a string
 	 * that represents this. Mostly this is concatenating the
 	 * code and the text.
 	 * 
 	 * @return A valid response code string that the server could
 	 * send to the client
 	 */
 	@Override
 	public String toString() {
 		
 		// Best practice to use a string builder for appending
 		StringBuilder str = new StringBuilder();
 		
 		// If the code isn't set, that's an internal error right there
 		if( code == null ) {
 			str.append( CODE.INTERNAL_ERROR.toString() );
 		} else {
 			// Otherwise just start with the code
 			str.append( code.toString() );
 		}
 		
 		// Any non-null text is added
 		if( text != null ) {
 			str.append( " " );
 			str.append( text );
 		}
 		
 		return str.toString();
 	}
 	
 	/**
 	 * Given a string -- for example, one that a client received --
 	 * instantiate a ResponseCode for the string. If there is a
 	 * syntax error, which basically means that the string did
 	 * not start with a 3-digit code, then a null is returned.
 	 * 
 	 * @param str The string, which should be something like
 	 * "500 something went wrong!" 
 	 * 
 	 * @return If the string started with a 3-digit integer then
 	 * return a ResponseCode object, parsing that and any text
 	 * out from the string. Otherwise, return null.
 	 */
 	public static ResponseCode getCodeFromString( String str ) {
 		
 		// Make sure the pattern has been generated for recognizing valid response strings
 		if( validResponsePattern == null ) {
 			validResponsePattern = Pattern.compile("^\\d{3}.*");
 		}
 
 		// Nulls are bad, as are strings not matching our valid response pattern
 		if( str == null || !validResponsePattern.matcher(str).matches() ) {
 			return null;
 		}
 		
 		// Otherwise parse out the text to associate with the response code
 		ResponseCode code = new ResponseCode();
 		// This just says "and everything after the third character, unless there's nothing
 		// there, in which case just set it to a null
 		code.setText( str.length() > 3 ? str.substring(3) : null );
 
 		// And the code, which we have to interpret as an integer
 		String numberAsString = str.substring(0, 3); 	// 3 because 3-digit code
 		try {
 			code.setCode( Integer.parseInt(numberAsString) );
 		} catch( NumberFormatException e ) {
 			// This is odd. We validated that it was a 3-digit number so this
 			// should technically never happen
 		}
 		
 		return code;
 	}
 	
 	/*************************************************************************************
 	 * Some convenience methods for creating response codes
 	 ************************************************************************************/
 	
 	/**
 	 * Generate a response with the appropriate code for
 	 * reporting the balance, with the account balance
 	 * (as an integer) following.
 	 * 
 	 * @param balance The user's account balance
 	 * @return A proper response code, for the given balance
 	 */
 	public static ResponseCode createAccountBalanceResponseCode( int balance ) {
 		return new ResponseCode( CODE.ACCOUNT_BALANCE, "" + balance );
 	}
 
 	/*************************************************************************************
 	 * Some convenience methods for interpreting response codes passed back as string
 	 ************************************************************************************/
 	
 	/**
 	 * Given a string that represents the response code, create an actual ResponseCode
 	 * object from it.
 	 * 
 	 * @param responseString A string like one would receive from a BlackjackCommand.processCommand()
 	 * method call
 	 * @return The ResponseCode object that can be constructed from it
 	 */
 	public static ResponseCode createResponseCodeFromString( String responseString ) {
 		if( responseString == null ) {
 			return null;
 		}
 		
 		ResponseCode responseCode = new ResponseCode();
 		
 		// The first 'token' is the code, which would be an integer
 		StringTokenizer strtok = new StringTokenizer(responseString);
 		String codeAsString = null;
 		if( strtok.hasMoreTokens() ) {
 			codeAsString = strtok.nextToken();
 			try {
 				Integer codeAsInteger = Integer.parseInt(codeAsString);
 				responseCode.setCode( codeAsInteger );
 			} catch( NumberFormatException e ) {
 				// TODO: Add an error reported
 			}
 		}
 		
 		// The rest of the string after this first token is the text
 		if( codeAsString != null ) {
 			responseCode.setText( responseString.substring(codeAsString.length()).trim() );
 		}
 		
 		return responseCode;
 	}
 
 	/*************************************************************************************
 	 * Autogenerated getters and setters.
 	 ************************************************************************************/
 
 	/**
 	 * @return the code
 	 */
 	public Integer getCode() {
 		return code;
 	}
 
 	/**
 	 * @param code the code to set
 	 */
 	public void setCode(Integer code) {
 		this.code = code;
 	}
 
 	/**
 	 * @return the text
 	 */
 	public String getText() {
 		return text;
 	}
 
 	/**
 	 * @param text the text to set
 	 */
 	public void setText(String text) {
 		this.text = text;
 	}
 	
 	/*************************************************************************************
 	 * Smarter getters
 	 ************************************************************************************/
 	
 	/**
 	 * The text of the response code might just be optional text, but in some
 	 * cases the first 'word' should be interpreted as a parameter to the response.
 	 * Return this first word, if present. (Note that if the response code doesn't
 	 * have parameters, this method doesn't really make sense to use.)
 	 * 
 	 * @return The first parameter of the response string, or null if there is
 	 * nothing that could be interpreted as a parameter
 	 */
 	public String getFirstParameterAsString() {
 		
 		String parameter = null;
 		
 		String text = getText();
 		// Only can parse out a parameter if there's some text
 		if( text != null ) {
 			StringTokenizer strtok = new StringTokenizer(text);
 			if( strtok.hasMoreTokens() ) {
 				parameter = strtok.nextToken();
 			}
 		}
 		
 		return parameter;
 	}
 
 	/**
 	 * The text of the response code might just be optional text, but in some
 	 * cases the first 'word' should be interpreted as a parameter to the response.
 	 * Moreover sometimes this word should be a number. Try to find a number that
 	 * could be returned
 	 * 
 	 * @return The first parameter of the response string interpreted as an integer.
 	 * If there is no first parameter, or it's not a number, returns null.
 	 */
 	public Integer getFirstParameterAsInteger() {
 		
 		Integer parameter = null;
 
 		String parameterAsString = getFirstParameterAsString();
 		if( parameterAsString != null ) {
 			try {
 				parameter = Integer.parseInt( parameterAsString );
 			} catch( NumberFormatException e ) {
 				// It's okay to silently fail here
 			}
 		}
 		
 		return parameter;
 	}
 }
