 package pokergame;
 
 import java.util.Collections;
 import java.util.Stack;
 
 public class Dealer {
 	Stack<Card> pack;
 	static Dealer instance;
 
 	private Dealer() {
 		newPack();
 
 	}
 
 	public void newPack() {
 		pack = new Stack<Card>();
 		String[] suits = new String[] { "hearts", "clubs", "diamonds", "spades" };
 		Integer[] values = new Integer[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12,
 				13};
 		for (String s : suits) {
 			for (Integer v : values) {
 				Card c = new Card(v, s);
 				pack.add(c);
 			}
 		}
 		shuffle();
 	}
 
 	public static Dealer getInstance() {
 		if (instance == null) {
 			instance = new Dealer();
 		}
 		return instance;
 	}
 
 	public void shuffle() {
 		Collections.shuffle(pack);
 	}
 
 	public Card dealACard() {
 		return pack.pop();
 	}
 
 	public Card[] dealFiveCards() {
 //		return new Card[] { pack.pop(), pack.pop(), pack.pop(), pack.pop(),
 	//			pack.pop() };
		return new Card[] {pack.pop(), pack.pop(), pack.pop(), pack.pop(),
 							pack.pop() };
 
 	}
 }
