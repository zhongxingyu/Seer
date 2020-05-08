 import java.util.ArrayList;
 import java.util.Collections;
 
 public class Deck {
 
     public class DeckEmptyException extends IndexOutOfBoundsException {
         public DeckEmptyException() {
             super("Deck is out of cards, reshuffle");
         }
     }
 
     private ArrayList<Card> m_cards = new ArrayList<Card>();
     private int m_iterator = 0;
 
 	public Deck() {
         makeDeck();
 	}
 
     public Card getNext() {
         if(m_iterator == m_cards.size() - 1)
             throw new DeckEmptyException();
 
         return m_cards.get(m_iterator++);
     }
 
     public void shuffle() {
         Collections.shuffle(m_cards);
     }
 
 	private void makeDeck() {
         for (Card.Suit suit : Card.Suit.values())
            for (int i = 0; i < 14; ++i)
                 m_cards.add(new Card(i, suit));
 	}
 
 }
