 package de.htwg.wzzrd.model;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class ServerPlayer extends Player {
     private static final long serialVersionUID = 1L;
     private transient List<Card> cards;
    private transient int[] cards_per_nation = new int[Settings.getNationcount()];
 
     private transient GameNetwork gnet;
 
     public ServerPlayer(String name, GameNetwork gnet) {
         super(name);
         this.gnet = gnet;
         cards = new ArrayList<Card>();
     }
 
     public void addCard(Card card) {
         cards.add(card);
         if (!Settings.getSpecialCards().contains(card.getValue())) {
            cards_per_nation[card.getNation()]++;
         }
     }
 
     public boolean removeCard(Card card) {
         if (cards.remove(card)) {
             if (!Settings.getSpecialCards().contains(card.getValue())) {
                cards_per_nation[card.getNation()]++;
             }
             return true;
         }
         return false;
     }
 
     public boolean hasCard(Card card) {
         return cards.contains(card);
     }
 
     public boolean hasNation(int nation) {
        return cards_per_nation[nation] != 0;
     }
 
     public GameNetwork getNet() {
         return gnet;
     }
 
     public List<Card> getCards() {
         return cards;
     }
 }
