 package se.chalmers.dryleafsoftware.androidrally.model.cards;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 /**
  * A deck with cards that cards are drawn from and put back into.
  */
 public class Deck {
 	private List<Card> cards;
 	
 	/**
 	 * Creates a new card deck with different move and turn cards.
 	 */
 	public Deck() {
 		cards = new ArrayList<Card>();
 		
 		for (int i = 10; i <= 60; i+=10) {
 			cards.add(new Turn(i, TurnType.UTURN));
 		}
 		for (int i = 70; i <= 410; i+=20) {
 			cards.add(new Turn(i, TurnType.LEFT));
 		}
 		for (int i = 80; i <= 420; i+=20) {
 			cards.add(new Turn(i, TurnType.RIGHT));
 		}
 		for (int i = 430; i <= 480; i+=10) {
 			cards.add(new Move(i, -1));
 		}
 		for (int i = 490; i <= 660; i+=10) {
 			cards.add(new Move(i, 1));
 		}
 		for (int i = 670; i <= 780; i+=10) {
 			cards.add(new Move(i, 2));
 		}
 		for (int i = 790; i <= 840; i+=10) {
 			cards.add(new Move(i, 3));
 		}
 		
 		shuffleDeck();
 	}
 	
 	/**
 	 * Shuffle the deck.
 	 */
 	public void shuffleDeck() {
 		Collections.shuffle(cards);
 	}
 	
 	/**
 	 * Take and remove a card from the deck.
 	 * @return a drawn card from the deck
 	 */
 	public Card drawCard() {
 		return cards.remove(0);
 	}
 	
 	/**
 	 * Put cards back to the deck.
 	 * @param returnedCards the card to be returned
 	 */
 	public void returnCards(List<Card> returnedCards) {
 		this.cards.addAll(returnedCards);
 	}
 
 	public List<Card> getCards() {
 		return cards;
 	}
 	
 	/**
 	 * Get a specific card from deck.
 	 * @param prio the priority of the wanted card
 	 * @return the wanted card. Null if its not in the deck.
 	 */
 	public Card getCard(int prio) {
 		for (Card card : cards) {
 			if (card.getPriority() == prio) {
 				cards.remove(card);
				System.out.println("Deck returning " + card.getPriority() + ", (" + prio + ")");
 				return card;
 			}
 		}
 		return null;
 	}
 }
