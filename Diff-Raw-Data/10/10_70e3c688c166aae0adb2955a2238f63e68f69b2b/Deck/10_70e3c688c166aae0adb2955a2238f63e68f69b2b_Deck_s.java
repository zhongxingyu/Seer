 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package blackjack;
 
 /**
  *
  * @author jeekstro
  */
 import java.util.*;
 
 public class Deck {
     final static int DECK_SIZE = 52;
    private int cards_left;
     private ArrayList<Card> deck;
     
     Random generator = new Random();
     
     public Deck() {
        cards_left = DECK_SIZE;
        ArrayList<Card> deck = new ArrayList<Card>();
         for (Card.Suit suit : Card.Suit.values()) {
             for (Card.Rank rank : Card.Rank.values()) {
                 deck.add(new Card(suit, rank));
             }
         }
        cards_left = deck.size();
         System.out.println(""+deck.size());
     }
     
     @Override
     public String toString() {
        return ""+cards_left;
     }
     
     public Card Deal() {
         System.out.println("Deck size before dealing was: "+deck.size());
         
         // deal random number between 0 and deck.size()
         
         int derp = generator.nextInt(deck.size());
         
         System.out.println("Random generated number was: "+derp);
         
         // return a Card to the using appliance.
         
         return deck.get(derp);
     }
     /*
     public String toString() {
         return "Herp derp";
     }
     * 
     */
 }
