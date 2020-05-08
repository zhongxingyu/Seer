 package com.github.kpacha.jkata.pokerhand;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class PokerHand {
 
     private List<PokerCard> hand;
 
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
 	return "High Card : " + findHigherCard().getCardValue();
     }
 
     private PokerCard findHigherCard() {
 	PokerCard higherCard = hand.get(0);
 	for (PokerCard currentCard : hand) {
 	    if (higherCard.compareTo(currentCard) < 0) {
 		higherCard = currentCard;
 	    }
 	}
 	return higherCard;
     }
 }
