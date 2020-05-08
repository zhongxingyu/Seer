 /*******************************************************************************
  * CS544 Computer Networks Spring 2013
  * 5/26/2013 - Hand.java
  * Group Members
  * o Jennifer Lautenschlager
  * o Constantine Lazarakis
  * o Carol Greco
  * o Duc Anh Nguyen
  * 
  * Purpose: Represents the cards in a player's (or the dealer's) hand, some of
  * which may explicitly be facedown and not revealed to other players
  ******************************************************************************/
 package drexel.edu.blackjack.cards;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import drexel.edu.blackjack.server.game.User;
 
 /**
  * A user's hand with all cards dealt from dealer
  * @author DAN
  *
  */
 
 public class Hand {
 	
 	/**
 	 * Used to report the results of comparing this
 	 * hand to a dealer hand, which is passed in.
 	 * It'll either be a win, a lose, or a tie
 	 */
 	public enum COMPARISON_RESULT {
 		
 		/**
 		 * Two ways to win:
 		 * <OL>
 		 * <LI>The dealer hand busted and this hand did not.
 		 * <LI>Neither hand busted, and this hand has more points.
 		 * </OL>
 		 */
 		WIN,
 		/**
 		 * Two ways to lose:
 		 * <OL>
 		 * <LI>This hand busted, and the dealer did not
 		 * <LI>Neither hand busted, and this hand has more points
 		 * </OL>
 		 */
 		LOSE,
 		/**
 		 * Only way to tie is if neither hand busted and they have
 		 * the same points.
 		 */
 		TIE
 	}
 	
 	private final ArrayList<DealtCard> cards = new ArrayList<DealtCard>();
 	private final HashSet<Integer> points = new HashSet<Integer>();
 	private final User user;
 	
 	/**
 	 * Construct an empty hand for a user
 	 * @param user The user
 	 */
 	public Hand(User user) {
 		this.user = user;
 		points.add(0);
 	}
 	
 	/**
 	 * get a card from dealer, it must be faceup or facedown using DealtCard class
 	 * this method also interprets the card and calculate all possible values 
 	 * there are many other simpler ways, but this is more generic
 	 * @param card
 	 */
 	public void receiveCard(DealtCard card) {
 		
 		cards.add(card);
 		
 		ArrayList<Integer> temp = new ArrayList<Integer>();
 		
 		for (int v: card.getValues()) {
 			// calculate all ways to interpret the card value(s) (Ace)
 			ArrayList<Integer> spoints = new ArrayList<Integer>(points);
 			for (int i=0; i<spoints.size(); i++) {
 				spoints.set(i, spoints.get(i) + v);
 			}
 			temp.addAll(spoints);
 		}
 		
 		points.clear();
 		points.addAll(temp);
 	}
 	
 	/**
 	 * Get the number of cards in the hand
 	 * 
 	 * @return Number of cards in the hand
 	 */
 	public int getTotalNumberOfCards() {
 		return (cards == null ? 0 : cards.size() );
 	}
 	
 	/**
 	 * Get all possible point values that the hand could
 	 * have. There are multiple ones as some cards (like
 	 * Ace) have multiple point values.
 	 * @return all possibly interpreted values of hand
 	 */
 	public List<Integer> getPossibleValues() {
 		return new ArrayList<Integer>(points);
 	}
 	
 	/**
 	 * Get a list of the faceup cards in the hand
 	 * @return List of faceup cards
 	 */
 	public List<DealtCard> getFaceupCards() {
 		ArrayList<DealtCard> temp = new ArrayList<DealtCard>();
 		for (DealtCard c:cards) {
 			if (c.isFaceUp()) {
 				temp.add(c);
 			}
 		}
 		return temp;
 	}
 	
 	/**
 	 * Get a list of facedown cards in the deck
 	 * @return List of facedown cards
 	 */
 	public List<DealtCard> getFacedownCards() {
 		ArrayList<DealtCard> temp = new ArrayList<DealtCard>();
 		for (DealtCard c:cards) {
 			if (!c.isFaceUp()) {
 				temp.add(c);
 			}
 		}
 		return temp;
 	}
 	
 	/**
 	 * Determines if the hand is busted
 	 * @return true if the hand is definitely busted
 	 */
 	public boolean getIsBusted() {
 		for (int i: points) {
 			if (i<=21) {
 				// not busted if any way to calculate points <= 21
 				return false;
 			}
 		}
 		return true;
 	}
 	
 	/**
 	 * In the modern game, a blackjack refers to any hand of an ace
 	 *  plus a ten or face card, regardless of suits or colours
 	 *  http://www.casino.org/games/blackjack/history.php
 	 * @return True if it's a blackjack, false otherwise
 	 */
 	public boolean getIsBlackJack() {
 		// a black jack if there are only two cards, and possible value is 21
 		if (cards.size() != 2) return false;
 		for (int i:points) {
 			if (i == 21) return true;
 		}
 		return false;
 	}
 	
 	@Deprecated
 	/**
 	 * <b>UI:</b> This needs to display a string representation of the cards
 	 * in the hand, as presented to the person whose hand it is.
 	 * The distinction is in the facedown cards. If displaying
 	 * for this person, the facedown cards are shown as if faceup,
 	 * that is, with the card.toString() value. Here, the facedown
 	 * card should be listed first in order, then the faceup cards
 	 * in order. The cards should be space-delimited. An example
 	 * of a valid response might be:
 	 * <p>
 	 * <b>UI:</b> 2C 3S 5D
 	 * <p>
 	 * <b>UI:</b> Here (though it is not indicated) the 2C was the facedown
 	 * card, and the 3S and 5D were the two facedown cards, in order.
 	 * 
 	 * @return
 	 */
 	public String toStringIfThisPlayer() {
 		StringBuilder b = new StringBuilder();
 		String separator = ""; 
 		for (Card c:this.getFacedownCards()) {
 			b.append(separator);
 			b.append(c.toString());
 			separator = " ";
 		}
 		for (Card c:this.getFaceupCards()) {
 			b.append(separator);
 			b.append(c.toString());
 			separator = " ";
 		}
 		return b.toString();
 	}
 	
 	@Deprecated
 	/**
 	 * <b>UI:</b> This is very similar to the toStringIfThisPlayer() method,
 	 * with one important distinction: instead of showing the
 	 * card.toString() values of facedown cards, an "X" should
 	 * be used.
 	 * 
 	 * <b>UI:</b> Continuing the example of the previous method, calling
 	 * this method on the same hand would return:
 	 * 
 	 * <b>UI:</b> X 3S 5D
 	 * 
 	 * @return
 	 */
 	public String toStringIfNotThisPlayer() {
 		StringBuilder b = new StringBuilder();
 		String separator = ""; 
 		for (Card c:this.getFacedownCards()) {
 			b.append(separator);
 			b.append("X");
 			separator = " ";
 		}
 		for (Card c:this.getFaceupCards()) {
 			b.append(separator);
 			b.append(c.toString());
 			separator = " ";
 		}
 		return b.toString();
 	}
 	
 	/**
 	 * <b>UI:</b> String representation of card
 	 * @param callingUser Who the representation is being 
 	 * constructed for
 	 * @return A string representation of cards in the hand
 	 * @see #toStringIfNotThisPlayer()
 	 * @see #toStringIfThisPlayer()
 	 */
 	public String toString(User callingUser) {
 		StringBuilder b = new StringBuilder();
 		String separator = ""; 
 		for (Card c:this.getFacedownCards()) {
 			b.append(separator);
 			if (callingUser == user) 
 				b.append(c.toString());
 			else
 				b.append("X");
 			separator = " ";
 		}
 		for (Card c:this.getFaceupCards()) {
 			b.append(separator);
 			b.append(c.toString());
 			separator = " ";
 		}
 		return b.toString();
 	}
 
 	
 	/**
 	 * <b>UI:</b> Construct a string representing the cards in this hand
 	 * @param isOwner True if this is being constructed for the
 	 * 'owner' of the hand, who can see facedown card values,
 	 * or false otherwise
 	 * @return String representation of the hand
 	 * @see #toStringIfNotThisPlayer()
 	 * @see #toStringIfThisPlayer()
 	 */
 	public String toString(boolean isOwner) {
 		StringBuilder b = new StringBuilder();
 		String separator = ""; 
 		for (Card c:this.getFacedownCards()) {
 			b.append(separator);
 			if (isOwner) 
 				b.append(c.toString());
 			else
 				b.append("X");
 			separator = " ";
 		}
 		for (Card c:this.getFaceupCards()) {
 			b.append(separator);
 			b.append(c.toString());
 			separator = " ";
 		}
 		return b.toString();
 	}
 	
 	/**
 	 * <b>UI:</b> Based on the game's rule, find out if dealer's hand should
 	 * hit or stand next
 	 * @param rules: rules of the game to parse. If there is one rule that has the form:
 	 * Dealer must hit soft 16 to stand
 	 * than 16 will be the point for dealer to stand.
 	 * Otherwise, just pass <b>null</b>, default stand point is 17.
 	 */
 	public boolean getDealerShouldHit(ArrayList<String> rules) {
 		int pointsToStand = 17;
 		Pattern pattern = Pattern.compile(".*soft\\s(\\d{2})\\sto\\sstand.*");
 		if (rules != null) {
 			for (String s:rules) {
 				Matcher matcher = pattern.matcher(s);
 				if (matcher.find()) {
 					try {
 						pointsToStand = Integer.parseInt(matcher.group(1));
 						break; // found the rule
 					} catch (Exception e) {
 						// doing nothing, let 17 be the point to stand
 					}
 				}
 			}
 		}
 		for (int p:this.getPossibleValues()) {
 			if (p >= pointsToStand) {
 				return false;
 			}
 		}
		return true;
 	}
 	
 	
 	/**
 	 * Take this hand, presumed to be a player hand, and
 	 * compare it to the dealer's hand. Report what the
 	 * result it. See the enum for the rules.
 	 * @param dealerHand Shouldn't be null
 	 * @return The result from this hand's perspective
 	 */
 	public COMPARISON_RESULT compareToDealerHand( Hand dealerHand ) {
 		
 		// This is just plain bad, but assume this hand wins I guess
 		if( dealerHand == null ) {
 			return COMPARISON_RESULT.WIN;
 		}
 		
 		// If you busted, you don't win, doesn't matter what the dealer has
 		if( getIsBusted() ) {
 			return COMPARISON_RESULT.LOSE;
 		}
 		
 		// If the player didn't bust and the dealer did, it's a win
 		if( dealerHand.getIsBusted() ) {
 			return COMPARISON_RESULT.WIN;
 		}
 		
 		// No one busted. Therefore, it comes down to point comparisons
 		// So what's the best possible value for this hand?
 		Integer thisHandValue = null;
 		if( getPossibleValues() != null ) {
 			for( Integer value : getPossibleValues() ) {
 				if( thisHandValue == null || (value > thisHandValue && value <= 21) ) {
 					thisHandValue = value;
 				}
 			}
 		}
 		
 		// And what about the dealer?
 		Integer dealerHandValue = null;
 		if( dealerHand.getPossibleValues() != null ) {
 			for( Integer value : dealerHand.getPossibleValues() ) {
 				if( dealerHandValue == null || (value > dealerHandValue && value <= 21 ) ) {
 					dealerHandValue = value;
 				}
 			}
 		}
 		
 		// Really, they should be null. And because they shouldn't be if they are? I'm going
 		// to give them a value of 1.
 		if( thisHandValue == null ) {
 			thisHandValue = 1;
 		}
 		if( dealerHandValue == null ) {
 			dealerHandValue = 1;
 		}
 		
 		// Otherwise we compare
 		if( thisHandValue > dealerHandValue ) {
 			return COMPARISON_RESULT.WIN;
 		} else if( thisHandValue < dealerHandValue ) {
 			return COMPARISON_RESULT.LOSE;
 		}
 		
 		// If we got here, must've tied
 		return COMPARISON_RESULT.TIE;
 	}
 }
