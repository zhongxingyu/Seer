 package cs309.a1.shared;
 
 /**
  * 
  * This class will be used to represent constants in a common game. Each of the constants
  * below are classified by a section of the game in which they will be used.
  *
  */
 public class Constants {
 	
 	/* GUI */
 	/**
 	 * A constant to limit the number of characters for a players name
 	 */
 	public static final int NAME_MAX_CHARS = 10;
	public static final int CARD_IMAGE_SCALE_SMALL = 200;
	public static final int CARD_IMAGE_SCALE_MED = 400;
	public static final int CARD_IMAGE_SCALE_LARGE = 500;
 	
 	/* Game Constants */
 	/**
 	 * A constant to represent the Clubs suit
 	 */
 	public static final int SUIT_CLUBS = 0;
 	
 	/**
 	 * A constant to represent the Diamonds suit
 	 */
 	public static final int SUIT_DIAMONDS = 1;
 	
 	/**
 	 * A constant to represent the Hearts suit
 	 */
 	public static final int SUIT_HEARTS = 2;
 	
 	/**
 	 * A constant to represent the Spades suit
 	 */
 	public static final int SUIT_SPADES = 3;
 	
 	/**
 	 * A constant to represent the jokers
 	 */
 	public static final int SUIT_JOKER = 4;
 	
 	public static final String PLAYER_1 = "player1";
 	public static final String PLAYER_2 = "player2";
 	public static final String PLAYER_3 = "player3";
 	public static final String PLAYER_4 = "player4";
 	
 	public static final String SUIT = "suit";
 	public static final String VALUE = "value";
 	public static final String RESOURCE_ID = "resourceid";
 	public static final String ID = "id";
 	public static final String MESSAGE_TYPE = "messagetype";
 	public static final String TURN = "isturn";
 	public static final String PLAYER_NAME = "playername";
 
 	public static final int GET_PLAYER_NAME = "getPlayerName".hashCode();
 
 	// these are bluetooth message types that can be sent by the GameBoard
 	public static final int SETUP = 0;
 	public static final int IS_TURN = 1;
 	public static final int CARD_DRAWN = 2;
 	public static final int WINNER = 3;
 	public static final int LOSER = 4;
 	public static final int REFRESH = 5;
 	public static final int PAUSE = 6;
 	public static final int UNPAUSE = 7;
 	public static final int END_GAME = 8;
 
 	// these are bluetooth messages types that can be sent by Player
 	public static final int PLAY_CARD = 9;
 	public static final int DRAW_CARD = 10;
 	
 	/* Language Options */
 	/**
 	 * A string representing the US locale
 	 */
 	public static final String LANGUAGE_US = "US"; 
 	
 	/**
 	 * A string representing the German locale
 	 */
 	public static final String LANGUAGE_GERMAN = "GERMAN";
 	
 	/**
 	 * A string representing the France locale
 	 */
 	public static final String LANGUAGE_FRANCE = "FRANCE"; 
 	
 	/**
 	 * A string representing the Canada locale
 	 */
 	public static final String LANGUAGE_CANADA = "CANADA"; 
 	
 	/**
 	 * A string representing the UK locale
 	 */
 	public static final String LANGUAGE_UK = "UK";
 	
 	
 	/* Preferences Options  */
 	/**
 	 * The name of the shared preferences to be used when getting the object
 	 */
 	public static final String PREFERENCES = "PREFERENCES"; 
 	
 	/**
 	 * A constant representing the difficulty of the AI players in the game for the preferences
 	 */
 	public static final String DIFFICULTY_OF_COMPUTERS = "DIFFICULTY OF COMPUTERS"; 
 	
 	/**
 	 * A constant to represent a key in the shared preferences for the sound effects option
 	 */
 	public static final String SOUND_EFFECTS = "SOUND EFFECTS"; 
 	
 	/**
 	 * A constant to represent a key in the shared preferences for the speech volume option
 	 */
 	public static final String SPEECH_VOLUME = "SPEECH VOLUME"; 
 	
 	/**
 	 * A constant to represent a key in the shared preferences for the language/locale option
 	 */
 	public static final String LANGUAGE = "LANGUAGE"; 
 	
 	/**
 	 * A constant to represent a key in the shared preferences for the number of computers in the game option
 	 */
 	public static final String NUMBER_OF_COMPUTERS = "NUMBER OF COMPUTERS";
 	
 	/**
 	 * A constant to represent a key in the shared preferences for the game type
 	 */
 	public static final String GAME_TYPE="GAME TYPE";
 	
 	/**
 	 * A constant to represent a key in the shared preferences for the game type crazy eights
 	 */
 	public static final String CRAZY_EIGHTS="CRAZY EIGHTS";
 	
 	/* AI Constants */
 	/**
 	 * The time you wait in between computer turns
 	 */
 	public static final long COMPUTER_WAIT_TIME = 1500;
 }
