 /**
  * 
  */
 package com.rachum.amir.skyhiking;
 
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Logger;
 
 import com.rachum.amir.util.range.Range;
 
 /**
  * @author Rachum
  *
  */
 public class Deck {
     private final List<Card> cards;
     private final List<Card> discard;
     private final Logger logger = Logger.getLogger(this.getClass().getName());
     
 	public Deck() {
 		cards = new LinkedList<Card>();
 		discard = new LinkedList<Card>();
 		for (final int i : new Range(4)) {
 			cards.add(Card.WILD);
 		}
 		for (final int i : new Range(18)) {
 			cards.add(Card.RED);
 			cards.add(Card.GREEN);
 			cards.add(Card.PURPLE);
 			cards.add(Card.YELLOW);
 		}
 		Collections.shuffle(cards);
 	}
 	
 	public void discard(final Card card) {
         discard.add(card);
 	}
     
 	public Collection<Card> draw(final int numOfCards) {
         final Collection<Card> cardsDrawn = new LinkedList<Card>();
         for (final int i : new Range(numOfCards)) {
             if (cards.isEmpty()) {
                 cards.addAll(discard);
                 discard.clear();
         		Collections.shuffle(cards);
                 assert(cards.size() + discard.size() == 76);
             }
            if (cards.isEmpty()) {
            	return cardsDrawn;
            }
             assert(!cards.isEmpty());
         	cardsDrawn.add(cards.remove(0));
 		}
         logger.info("Current deck: " + this);
         return cardsDrawn;
 	}
     
 	@Override
 	public String toString() {
         final Map<Card, Integer> display = new HashMap<Card, Integer>();
 		for (final Card card : Card.values()) {
 			final int count = Collections.frequency(cards, card);
 			display.put(card, count);
 		}
         return display.toString();
 	}
 }
