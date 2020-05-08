 package herbstJennrichLehmannRitter.engine.model;
 
 import herbstJennrichLehmannRitter.engine.enums.CardType;
 
import java.util.Collection;

 
 /**
  * 
  * Interface, für die Karten
  *
  */
 public interface Deck {
 	
 	public Collection<Card> getAllCards();
 	
 	public void discardCard(Card card);
 	public void discardAllCards();
 	public void discardAllCardsByType(CardType cardType);
 	
 	public boolean pickCard();
 	public boolean pickCards(int numberOfCards);
 	public boolean pickNumberOfCardsWithType(int numberOfCards, CardType cardType);
 	public boolean pickCardFromDeckStackOrCemeteryDeckWithCostAbout(int cost);
 	
 	public void exchangeCardsWithHandDeck(Deck deck);
 	
 	public void shuffle();
 	
	public int getHandDeckSize();
 
 }
