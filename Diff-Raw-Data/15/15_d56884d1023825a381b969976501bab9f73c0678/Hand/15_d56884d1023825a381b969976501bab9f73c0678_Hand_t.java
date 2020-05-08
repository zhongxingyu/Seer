 /**
  * 
  */
 package com.rachum.amir.skyhiking;
 
 import java.util.Collection;
import java.util.Collections;
 import java.util.LinkedList;
 
 /**
  * @author Rachum
  *
  */
 public class Hand {
     private final Deck deck;
     private final Collection<Card> cards = new LinkedList<Card>();
     
 	public Hand(final Deck deck) {
 		this.deck = deck;
 	}
     
 	public void draw(final int numOfCards) {
 		cards.addAll(deck.draw(numOfCards));
 	}
     
 	public void discard(final Card card) {
 		if (!cards.contains(card)) {
			throw new IllegalStateException(card.toString() + 
											" is not in hand.");
 		}
         cards.remove(card);
         deck.discard(card);
 	}
     
 	public void discard(final Collection<Card> cards) {
         for (final Card card : cards) {
         	this.discard(card);
         }
 	}
     
 	public Collection<Card> getCards() {
 		return cards;
 	}
     
     public boolean contains(final Card card) {
         return this.cards.contains(card);
     }
     
 	public boolean contains(final Collection<Card> cards) {
		for (Card card : cards) {
			int inHand = Collections.frequency(this.cards, card);
			int inList = Collections.frequency(cards, card);
			if (inList > inHand) {
				return false;
			}
		}
		return true;
 	}
 
 }
