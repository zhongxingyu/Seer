 package fr.eurecom.util;
 
 import java.util.Collections;
 import java.util.LinkedList;
 import java.util.List;
 
 import fr.eurecom.cardify.Game;
 
 import android.content.Context;
 import android.graphics.Color;
 import android.graphics.ColorFilter;
 import android.graphics.LightingColorFilter;
 import android.widget.ImageView;
 
 public class CardDeck extends ImageView {
 	private static char[] suits = {'s', 'h', 'd', 'c'};
 	private static int[] faces = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13};
 	
 	private List<Card> cards;
 	private final ColorFilter highlightFilter = new LightingColorFilter(Color.DKGRAY, 1);
 	
 	public CardDeck(Context context){
 		super(context);
 		
 		this.cards = new LinkedList<Card>();
 		
 		for (char suit : suits){
 			for (int face : faces){
 				cards.add(new Card(suit, face, false));
 			}
 		}
 		
 		this.setImage(context);
 		
 		this.setX(Game.screenSize.x/4);
 		this.setY(Game.screenSize.y/4);
 		this.toggleEmpty();
 	}
 	
 	public CardDeck(Context context, List<Card> receivedCards) {
 		super(context);
 		
 		this.cards = receivedCards;
 		this.setImage(context);
 		
 		this.setX(Game.screenSize.x/4);
 		this.setY(Game.screenSize.y/4);
		this.toggleEmpty();
 	}
 	
 	private void setImage(Context context) {
 		this.setImageResource(context.getResources().getIdentifier("drawable/deck", null, context.getPackageName()));
 	}
 	
 	public Card pop(){
 		if (cards.isEmpty()) return null;
 		return cards.remove(0);
 	}
 
 	public Card peek(){
 		if (cards.isEmpty()) return null;
 		return cards.get(0);
 	}
 	
 	public void addCard(Card card) {
 		this.cards.add(0, card);
 		if (cards.size() == 1) {
 			toggleEmpty();
 		}
 	}
 	
 	public List<Card> draw(int n){
 		List<Card> temp = new LinkedList<Card>();
 		for (int i = 0; i < n; i++){
 			if (cards.isEmpty()) break;
 			temp.add(this.pop());
 		}
 		return temp;
 	}
 	
 	public void shuffle(){
 		Collections.shuffle(this.cards);
 	}
 	
 	public Card drawFromDeck() {
 		Card c = pop();
 		c.setTurned(true);
 		
 		if (cards.isEmpty()) {
 			toggleEmpty();
 		}
 		
 		return c;
 	}
 	
 	public void toggleHighlight(boolean highlight) {
 		if (highlight) {
 			this.setColorFilter(highlightFilter);
 		} else {
 			this.setColorFilter(null);
 		}
 	}
 	
 	public boolean toggleEmpty() {
 		if (cards.isEmpty()) {
 			this.setAlpha((float)0.2);
 			return true;
 		} else {
 			this.setAlpha((float)1.0);
 			return false;
 		}
 	}
 	
 	public List<Card> getCards() {
 		return cards;
 	}
 }
