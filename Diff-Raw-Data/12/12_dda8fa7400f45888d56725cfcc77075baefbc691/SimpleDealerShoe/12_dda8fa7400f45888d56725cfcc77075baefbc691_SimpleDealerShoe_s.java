 /*******************************************************************************
  * CS544 Computer Networks Spring 2013
  * 5/26/2013 - SimpleDealerShoe.java
  * Group Members
  * o Jennifer Lautenschlager
  * o Constantine Lazarakis
  * o Carol Greco
  * o Duc Anh Nguyen
  * 
  * Purpose: Implement functionality related to a dealer's shoe of cards, such
  * as shuffling and dealing cards from the top
  ******************************************************************************/
 package drexel.edu.blackjack.cards;
 
 import java.util.ArrayList;
 import java.util.Collections;
 
 /**
  * Implements a dealer's shoe that plays fair and square.
  * 
  * @author Duc
  */
 public class SimpleDealerShoe implements DealerShoeInterface {
 	
 	private final int numDecks;
 	private final ArrayList<Card> deck;
 	private final ArrayList<Card> bin;
 
 	/**
 	 * Constructs the shoe around a specified number of
 	 * decks. Cards start sequentially ordered unless
 	 * shuffled.
 	 * 
 	 * @param numDeck The number of decks
 	 */
 	public SimpleDealerShoe(int numDeck) {
 		numDecks = numDeck;
 		deck = new ArrayList<Card>();
 		bin = new ArrayList<Card>();
 		for (int i=0; i < numDeck; i++) {
 			for (Card.SUIT suit:Card.SUIT.values()) {
 				for (Card.RANK rank: Card.RANK.values()) {
 					deck.add(new Card(rank, suit));
 				}
 			}
 		}
 	}
 
 	@Override
 	public void shuffle() {
 		Collections.shuffle(deck);
 	}
 
 	@Override
 	public DealtCard dealTopCard() {
 		if (isEmpty()) return null;
 		Card c = deck.remove(0);
 		bin.add(c);
 		return new DealtCard(c);
 	}
 
 	@Override
 	public boolean isEmpty() {
 		return deck.size() == 0;
 	}
 
 	@Override
 	public int getNumberDecks() {
 		return numDecks;
 	}
 
 	@Override
 	public int getNumberOfDealtCards() {
 		return bin.size();
 	}
 
 	@Override
 	public float getPercentageOfDealtCards() {
		return bin.size()/(bin.size() + deck.size());
 	}
 
 	@Override
 	public int getTotalNumberOfCards() {
 		return deck.size();
 	}
 
 }
