 package com.github.kpacha.jkata.pokerhand.hand;
 
 import java.util.List;
 
 import com.github.kpacha.jkata.pokerhand.AbstractPokerHandArchetype;
 import com.github.kpacha.jkata.pokerhand.PokerCard;
 
 public class AbstractNumberOfAKind extends AbstractPokerHandArchetype {
 
     private PokerCard subject = null;
     protected int numberOfAppearences = 0;
 
     @Override
     public String getHandDescription() {
 	return subject.getCardValue();
     }
 
     @Override
     protected AbstractPokerHandArchetype processHand() {
 	subject = findNOfAKind(hand.getCards(), null, numberOfAppearences);
 	return this;
     }
 
     @Override
     public boolean match() {
 	return subject != null;
     }
 
     public int getNumericValue() {
 	return subject.getNumericValue();
     }
 
     @Override
     public int compareEqualRanked(AbstractPokerHandArchetype hand) {
 	return subject.getNumericValue()
 		- ((AbstractNumberOfAKind) hand).getNumericValue();
     }
 
     public PokerCard findNOfAKind(List<PokerCard> hand, PokerCard cardToAvoid,
 	    int expectedAppearences) {
 	String cardValueToAvoid = (cardToAvoid != null) ? cardToAvoid
 		.getCardValue() : "";
 	PokerCard result = null;
	int appearances = 0;
 	OUTERMOST: for (int i = 0; i < 4; i++) {
 	    if (hand.get(i).getCardValue().equals(cardValueToAvoid))
 		continue;
 	    for (int j = i + 1; j < 5; j++) {
 		if (hand.get(j).getCardValue().equals(cardValueToAvoid))
 		    continue;
 		if (hand.get(i).compareTo(hand.get(j)) == 0) {
 		    appearances++;
 		    if (appearances == expectedAppearences) {
 			result = hand.get(i);
 			break OUTERMOST;
 		    }
 		}
 	    }
 	}
 	return result;
     }
 
 }
