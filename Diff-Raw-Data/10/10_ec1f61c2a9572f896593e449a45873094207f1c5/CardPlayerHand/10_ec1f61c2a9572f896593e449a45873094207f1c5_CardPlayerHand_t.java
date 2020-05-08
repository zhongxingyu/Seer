 package fr.eurecom.util;
 
 import java.util.Collections;
 import java.util.LinkedList;
 import java.util.List;
 import android.graphics.Point;
 import android.view.View;
 import fr.eurecom.cardify.Game;
 
 public class CardPlayerHand {
 
 	public List<Card> cardStack;
 	public List<Card> cardPublic;
 	public Game game;
 	
 	public CardPlayerHand(Game game){
 		this.game = game;
 		cardStack = new LinkedList<Card>();
 		cardPublic = new LinkedList<Card>();
 	}
 	
 	public void dealInitialCards(List<Card> cards){
 		cardStack = cards;
 		Collections.sort(cardStack, new CardComparator(CardSortingRule.S_H_D_C_ACE_HIGH));
 		stackCards();
 		for (Card c : cardStack){
 			game.addView(c);
 			c.setOwner(this);
 		}
 	}
 	
 	public void addToStack(Card card){
 		cardStack.remove(card);
 		int pos = 0;
 		for (Card c : cardStack){
 			if (c.getX() >= card.getX()) break;
 			pos++;
 		}
 		cardStack.add(pos, card);
 		stackCards();
 	}
 	
 	public void addToPublic(Card card){
 		if (cardStack.remove(card)){
 			cardPublic.add(card);
 			//game.getClient().publishPutCardInPublicZone(card);
 		}
 		stackCards();
 	}
 	
	public void blindAddToPublic(char suit, int face){
		Card card = new Card(this.game, suit, face);
 		cardPublic.add(card);
 		cardStack.remove(card);
 		cardPublic.add(card);
 	}
 	
 	public void stackCards(){
 		if (cardStack.isEmpty()) return;
 		Point displaySize = game.getDisplaySize();
 		
 		int maximumStackWidth = displaySize.x - 10; //5px free on each side of stack
 		double pixelsBetweenCards = cardStack.size() != 1 ? Math.min(Card.width/2, (maximumStackWidth - Card.width)/(cardStack.size() - 1)) : 0;
 		int y = displaySize.y - Card.height;
 		int x = 0;
 		
 		if (pixelsBetweenCards <= Card.width/2) {
 			x = (int) Math.round(((displaySize.x - ((cardStack.size() - 1)*pixelsBetweenCards + Card.width))/2));
 		} else {
 			x = 5;
 		}
 		
 		for (Card card : cardStack) {
 			card.setX(x);
 			card.setY(y);
 			x += Math.round(pixelsBetweenCards);
 			queueBringToFront(card);
 		}
 		applyBringToFront(cardStack.get(0));
 	}
 	
 	public boolean inStackZone(float x, float y){
 		Point displaySize = game.getDisplaySize();
 		if (x < 0 || x > displaySize.x) return false;
 		if (y < displaySize.y - 1.75*Card.height || y > displaySize.y) return false;
 		return true;
 	}
 	
 	public void takeCard(Card card) {
 		
 	}
 	
 	public void dropCard(Card card){
 		if(inStackZone(card.getX(), card.getY())) {
 			addToStack(card);
 		} else {
 			addToPublic(card);
 		}
 	}
 	
 	public void moveCard(Card card) {
 		
 	}
 	
 	private void queueBringToFront(View v){
 		v.bringToFront();
 	}
 	
 	private void applyBringToFront(View view){
 		view.requestLayout();
 		view.invalidate();
 	}
 	
 	public void sortCards(CardSortingRule sorting) {
 		Collections.sort(cardStack, new CardComparator(sorting));
 		stackCards();
 	}
 	
 }
