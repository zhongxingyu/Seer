 package card;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 
 public class Deck {
 
 	private final List<Card> cards;
 
 	protected Deck(List<Card> cards) {
 		this.cards = cards;
 	}
 
 	public static Deck init32Cards() {
 		List<Card> cards = new ArrayList<Card>();
 		for (Color color : Color.values()) {
 			for (Value value : Value.values()) {
				if (value.ordinal() > Value._6.ordinal()) {
					cards.add(Card.of(value, color));
 				}
 			}
 		}
 		return new Deck(cards);
 	}
 
 	public static Deck init52Cards() {
 		List<Card> cards = new ArrayList<Card>();
 		for (Color color : Color.values()) {
 			for (Value value : Value.values()) {
 				cards.add(Card.of(value, color));
 			}
 		}
 		return new Deck(cards);
 	}
 
 	public int size() {
 		return cards.size();
 	}
 
 	public void shuffle() {
 		Collections.shuffle(cards);
 	}
 
 	public Card distribute() throws NoMoreCardException {
 		if (cards.isEmpty()) {
 			throw new NoMoreCardException();
 		}
 		return cards.remove(0);
 	}
 
 	protected List<Card> getCards() {
 		return cards;
 	}
 }
