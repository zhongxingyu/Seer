 package player;
 import cards.*;
 
 public abstract class Player {
 	
 	private Pile hand;
 	private int chips;
 		
 	public Player(int buyin){
 		chips = buyin;
 	}
 	
 	public void addCard(Card card) {
 		hand.add(card);
 	}
 	
 	public Pile getHand() {
 		return hand;
 	}
 	
 	public int getStackSize() {
 		return chips;
 	}
 	
 	public void updateStack(int delta) {
 		chips += delta;
 	}
 	
 	//Returns the player's chosen action. 0 for fold, 1 for call and 2 for raise.
 	public abstract int getAction();
 
 }
