 /*******************************************************************************
  * CS544 Computer Networks Spring 2013
  * 5/26/2013 - ClientSideGame.java
  * Group Members
  * o Jennifer Lautenschlager
  * o Constantine Lazarakis
  * o Carol Greco
  * o Duc Anh Nguyen
  * 
  * Purpose: This helper class is used to translate the raw protocol message
  * that is a response to the LISTGAMES command into an object-oriented view
  * of the games.
  ******************************************************************************/
 package drexel.edu.blackjack.client.screens.util;
 
 import java.util.ArrayList;
 
 import drexel.edu.blackjack.db.game.GameMetadata;
 
 /**
  * <b>UI:</b> This is an object-oriented view of what a game is,
  * as 'visualized' on the client side. It's related to the UI
  * in that a successful LISTGAMES response message is parsed out
  * into multiple of these ClientSideGame objects. That way, the
  * UI can present information to the end user in a more user-
  * friendly manner than just showing them the raw protocol
  * messages. 
  * 
  * @author Jennifer
  */
 public class ClientSideGame {
 
 	// Mostly this wraps the GameMetadata except....
 	private GameMetadata gameMetadata;
 	
 	// The description isn't stored in it
 	private String description;		// Should really be in the game metadata eventually
 
 	/******************************************************
 	 * Constructor goes here.
 	 *****************************************************/
 
 	/**
 	 * Constructor the game object. Because the game metadata
 	 * uses the builder method, we pass in all the information
 	 * that is related to metadata in the constructor.
 	 * 
 	 * @param id Game session ID, used in making JOINSESSION requests
 	 * @param numDecksAsString What the game states as the number of decks used
 	 * @param rules Any optional rules. There is no 'grammar' for the rules, they
 	 * are text strings suitable to showing an end user
 	 * @param minBetAsString What the advertised minimum bet is
 	 * @param maxBetAsString What the advertised maximum bet is
 	 * @param description A description in a sentence or phrase suitable
 	 * for presenting directly to an end user
 	 */
 	public ClientSideGame( String id, String numDecksAsString, ArrayList<String> rules,
 			String minBetAsString, String maxBetAsString, String description ) {
 
 		// Hopefully this gets moved to metadata
 		this.description = description;
 		
 		// Parse out the numbers
 		Integer numDecks = parseAsNumber( numDecksAsString );
 		Integer maxBet = parseAsNumber( maxBetAsString );
 		Integer minBet = parseAsNumber( minBetAsString );
 
 		// Create the metadata
 		GameMetadata.Builder b = new GameMetadata.Builder();
 		b.setId(id);
 		b.setMaxBet(maxBet);
 		b.setMinBet(minBet);
 		b.setNumDecks(numDecks);
		if( rules != null && rules.size() > 0 ) {
			b.setRules(rules);
		}
 		gameMetadata = b.build();
 	}
 
 	/**
 	 * Given a string, try to interpret it as an integer.
 	 * If it doesn't parse, fail silently and return null
 	 * 
 	 * @param str String representation
 	 * @return Integer representation, or null if it didn't parse
 	 */
 	private Integer parseAsNumber(String str ) {
 		Integer value = null;
 		
 		try {
 			value = Integer.parseInt(str);
 		} catch( NumberFormatException e ) {
 			// Fail silently
 		}
 		
 		return value;
 	}
 
 	/******************************************************
 	 * Getters just pass through to the 
 	 * metadata object.
 	 *****************************************************/
 
 	/**
 	 * Get the game session ID, suitable for using in a
 	 * JOINSESSIOn command.
 	 * 
 	 * @return The ID
 	 */
 	public String getId() {
 		return gameMetadata.getId();
 	}
 
 	/**
 	 * Get the advertised number of decks used.
 	 * 
 	 * @return Number of decks used.
 	 */
 	public Integer getNumDecks() {
 		return gameMetadata.getNumDecks();
 	}
 
 	/**
 	 * Get the advertised rules, as a list of strings
 	 * suitable for presenting to an end user.
 	 * 
 	 * @return A list of the rules
 	 */
 	public ArrayList<String> getRules() {
 		return gameMetadata.getRules();
 	}
 
 	/**
 	 * Get the advertised minimum bet
 	 * 
 	 * @return Minimum bet
 	 */
 	public Integer getMinBet() {
 		return gameMetadata.getMinBet();
 	}
 
 	/**
 	 * Get the advertised maximum bet
 	 * 
 	 * @return Maximum bet
 	 */
 	public Integer getMaxBet() {
 		return gameMetadata.getMaxBet();
 	}
 
 	/******************************************************
 	 * The whole reason we are wrapping it is to print
 	 * it the way we want to print it.
 	 *****************************************************/
 	
 	@Override
 	/**
 	 * <b>UI:</b> Given this object that represents a game, construct
 	 * a single-line text string that is suitable for presenting to
 	 * an end user, that describes the game.
 	 * 
 	 * @return Human-readable string
 	 */
 	public String toString() {
 		// Finally we can create a string that represents the game!
 		StringBuilder str = new StringBuilder();
 		
 		// Need description
 		str.append( description );
 		
 		// Some metadata on the same line
 		str.append( " [" );
 		str.append( getBetRestriction() );
 		str.append(", " );
 		if( getNumDecks() == null ) {
 			str.append( "with an unspecified number of decks used" );
 		} else {
 			str.append( "with " + getNumDecks() + " deck" + (getNumDecks() > 1 ? "s" : "" ) + " used" );
 		}
 		str.append( "]" );
 		
 		// Rules, if any, in a bullet list below
		if(  getRules() != null ) {
			for( String rule : getRules() ) {
				str.append( "\n    o " );
				str.append( rule );
			}
 		}
 		
 		return str.toString();
 	}
 
 	/**
 	 * <b>UI:</b> Get a text description of the bet range 
 	 * of this game in a manner that can be presented to
 	 * the end user.
 	 * 
 	 * @return A sentence that stands on its own describing
 	 * the restrictions for bets on this game, with no
 	 * ending punctuation
 	 */
 	public String getBetRestriction() {
 		
 		String betRestriction = "Bet restrictions unknown";
 		
 		if( getMinBet() == null && getMaxBet() == null ) {
 			betRestriction = "Unlimited bet range";
 		} else if( getMinBet() == null ) {
 			betRestriction = "Bets up to $" + getMaxBet();
 		} else if( getMaxBet() == null ) {
 			betRestriction = "Bets start at $" + getMinBet();
 		} else {
 			betRestriction = "Bets from $" + getMinBet() + " - $" + getMaxBet();
 		}
 		
 		return betRestriction;
 	}
 }
