 package NewBlackjack.src;
 
 import java.util.Vector;
 
 
 public class Hand {
 	private Vector<Card> myCards = new Vector<Card>();
 	
 	public Hand() {
 
 	}
 	
 	public Hand(Card one) {
 		myCards.add(one);
 	}
 	
 	public Hand(Card one, Card two) {
 		myCards.add(one);
 		myCards.add(two);
 	}
 	
 	public int sizeOfHand() {
 		return myCards.size();
 	}
 	
 	public void addCard(Card a) {
 		myCards.add(a);
 	}
 	
 	public Card getCard(int num) {
 		return myCards.get(num);
 	}
 	
 	public void removeCard(int num) {
 		myCards.remove(num);
 	}
 	
 	public boolean hasAce() {
 		boolean ace = false;
 		
 		for (Card c : myCards) {
 			if (c.getNumber().equalsIgnoreCase("ace"))
 				ace = true;
 		}
 		
 		return ace;
 	}
 	
 	public int numberOfAces() {
 		int counter = 0;
 		
 		for (Card c : myCards) {
 			if (c.getNumber().equalsIgnoreCase("ace"))
 				counter++;
 		}
 		
 		return counter;
 	}
 	
 	public int getPoints() {
 		int points = 0;
 		
 		for (Card c : myCards) {
 			points += c.getFaceValue();
 		}
 		
 		if (points > 21 && (this.numberOfAces() >= 1))
 			points -= 10;
 		if (points > 21 && (this.numberOfAces() >= 2))
 			points -= 10;
 		if (points > 21 && (this.numberOfAces() >= 3))
 			points -= 10;
 		if (points > 21 && (this.numberOfAces() == 4))
 			points -= 10;
 		
 		return points;		
 	}
 	
	public int getRawPoints() {
		int points = 0;
		
		for (Card c : myCards) {
			points += c.getFaceValue();
		}
		
		return points;
	}
	
 	public void printHand() {
 		for (Card c : myCards) {
 			System.out.println(c.toString());
 		}
 	}
 }
