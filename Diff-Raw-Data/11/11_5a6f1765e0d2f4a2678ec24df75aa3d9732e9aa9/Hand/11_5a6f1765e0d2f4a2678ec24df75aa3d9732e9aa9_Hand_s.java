 package game;
 
 import game.enumeration.Suit;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 
 public class Hand {
 	private transient ArrayList<Card> hand = new ArrayList<Card>(10);
 	private int numberOfCard = 0;
 	private Suit suit = null;
 	
 	public Hand(Deck deck) throws Exception {
 		for(int i = 0; i < 10; ++i) {
 			if(deck.isEmpty()) {
 				throw new Exception("The deck is empty.");
 			}
 			else {
 				this.hand.add(deck.takeCard());
 				++this.numberOfCard;
 			}
 		}
 	}
 	
 	public Suit getGameSuit() {
 		return this.suit;
 	}
 	
 	public boolean setGameSuit(Suit suit) {
 		if(this.suit ==  Suit.BLACK || this.suit == Suit.COLOR)
 			return false;
 		
 		this.suit = suit;
 		return true;
 	}
 	
 	public Card[] getCards() {
		if(this.hand == null)
 			return null;
		else
			return (Card[])this.hand.toArray();
 	}
 	
 	public void setCards(ArrayList<Card> cards) {
 		this.hand = (ArrayList<Card>) cards.clone();
 	}
 	
 	public boolean playCard(Card card) {
 		if(this.hand != null && this.hand.contains(card)) {
 			this.hand.remove(card);
 			--this.numberOfCard;
 			return true;
 		}
 		
 		return false;
 	}
 	
 	public ArrayList<Card> getPlayableCard() {
 		if(this.suit !=  null && this.hand != null) {
 			ArrayList<Card> ret = new ArrayList<Card>();
 			Iterator<Card> itr = this.hand.iterator(); 
 			
 			while(itr.hasNext()) {
 				Card card = itr.next();
 				if(card.getSuit().equals(this.suit)) {
 					ret.add(card);
 				}
 			}
 			
 			if(ret.size() != 0) {
 				return ret;
 			}		
 		}
 		
 		return this.hand;
 	}
 	
 	public int getNumberOfCard() {
 		return this.numberOfCard;
 	}
 	
 	public String toString() {
 		
 		String hand = "";
 	
 		for( Card card : this.getCards() ) {
 			hand += card.toString() + "\n";
 		}
 		
 		return hand;
 		
 	}
 }
