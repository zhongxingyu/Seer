 package com.github.kpacha.jkata.pokerhand;
 
 import java.util.HashMap;
 import java.util.Map;
 
 public class PokerCard implements Comparable<PokerCard> {
 
     private String card;
     private Map<Character, Integer> specialCards = new HashMap<Character, Integer>(
 	    4) {
 	{
	    put('A', 14);
	    put('K', 13);
	    put('Q', 12);
	    put('J', 11);
	    put('T', 10);
 	}
     };
 
     public PokerCard(String card) {
 	this.card = card;
     }
 
     public int getNumericValue() {
 	if (specialCards.containsKey(card.charAt(0)))
 	    return specialCards.get(card.charAt(0));
 	return Integer.parseInt(card.substring(0, 1));
     }
 
     public int compareTo(PokerCard card) {
 	return getNumericValue() - card.getNumericValue();
     }
 
     public String getCardValue() {
 	return card.substring(0, 1);
     }
 }
