 package com.github.kpacha.jkata.pokerhand;
 
 import java.util.ArrayList;
import java.util.LinkedList;
 import java.util.List;
 
 import com.github.kpacha.jkata.pokerhand.hand.HigherCard;
 import com.github.kpacha.jkata.pokerhand.hand.Pair;
import com.github.kpacha.jkata.pokerhand.hand.ThreeOfAKind;
 import com.github.kpacha.jkata.pokerhand.hand.TwoPairs;
 
 public class PokerHand {
 
     private List<PokerCard> hand;
    private static List<AbstractPokerHandArchetype> archetypes = new LinkedList<AbstractPokerHandArchetype>() {
 	{
	    add(new ThreeOfAKind());
 	    add(new TwoPairs());
 	    add(new Pair());
 	    add(new HigherCard());
 	}
     };
 
     public PokerHand(String card1, String card2, String card3, String card4,
 	    String card5) {
 	hand = new ArrayList<PokerCard>(5);
 	hand.add(new PokerCard(card1));
 	hand.add(new PokerCard(card2));
 	hand.add(new PokerCard(card3));
 	hand.add(new PokerCard(card4));
 	hand.add(new PokerCard(card5));
     }
 
     public String findHand() {
 	String result = null;
 	for (AbstractPokerHandArchetype handArchetype : archetypes) {
 	    handArchetype.processHand(this);
 	    if (handArchetype.match()) {
 		result = handArchetype.getName() + " : "
 			+ handArchetype.getHandDescription();
 		break;
 	    }
 	}
 	return result;
     }
 
     public List<PokerCard> getCards() {
 	return hand;
     }
 }
