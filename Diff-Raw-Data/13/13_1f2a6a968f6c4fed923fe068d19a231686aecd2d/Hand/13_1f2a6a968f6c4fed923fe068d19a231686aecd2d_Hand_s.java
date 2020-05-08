 package org.kullgren.robot;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Set;
 
 public class Hand {
     public Hand() {
         myCards = new ArrayList<Card>();
         selectedCards = new HashSet<Integer>();
     }
 
     public int numCards() {
         return size;
     }
 
     public Card getCard(int position) {
         if (position >= size) {
             return null;
         }
         return myCards.get(position);
     }
 
     public void addCard(Card card) {
         myCards.add(card);
         size++;
     }
 
     public void selectCard(int position) {
         if (!selectedCards.contains(position)) {
             selectedCards.add(position);
         } else {
             selectedCards.remove(position);
         }
     }
 
     public boolean isCardSelected(int position) {
         return selectedCards.contains(position);
     }
 
     public void moveSelectedCardsLeft() {
         if (selectedCards.size() < 1) {
             return;
         }
 
         Iterator<Integer> it = selectedCards.iterator();
         int c1 = it.next();
 
         if (c1 < myCards.size() && c1 > 0) {
             int c2 = c1 - 1;
             swapCards(c1, c2);
         }
         moveInvalidCardsToEndOfList();
     }
 
     public void moveSelectedCardsRight() {
         if (selectedCards.size() < 1) {
             return;
         }
 
         Iterator<Integer> it = selectedCards.iterator();
         int c1 = it.next();
 
         if (c1 < myCards.size() - 1) {
             int c2 = c1 + 1;
             swapCards(c1, c2);
         }
         moveInvalidCardsToEndOfList();
     }
 
     public void invalidateCard() {
         if (selectedCards.size() < 1) {
             return;
         }
 
         Iterator<Integer> it = selectedCards.iterator();
 
         for (int i = 0; i < selectedCards.size(); i++) {
             int cardNo = it.next();
             myCards.get(cardNo).toggleInvalid();
         }
 
         moveInvalidCardsToEndOfList();
         clearAllSelections();
     }
 
     public boolean isCardInvalid(int i) {
         return myCards.get(i).isCardInvalid();
     }
 
     public void generateRandomCards(int numberOfCards) {
         size = numberOfCards;
         Card card1 = new Card(Movement.Forward, 19);
         Card card2 = new Card(Movement.TurnLeft, 19);
         Card card3 = new Card(Movement.TurnRight, 19);
         Card card4 = new Card(Movement.Wait, 19);
         Card card5 = new Card(Movement.TurnLeft, 19);
         myCards.add(card1);
         myCards.add(card2);
         myCards.add(card3);
         myCards.add(card4);
         myCards.add(card5);
     }
 
     private void moveInvalidCardsToEndOfList() {
         for (int i = 0; i < size; i++) {
             if (myCards.get(i).isCardInvalid()) {
                 int newPosition = i + 1;
                 while (newPosition < size
                         && myCards.get(newPosition).isCardInvalid()) {
                     newPosition++;
                 }
                 if (newPosition < size) {
                     swapCards(i, newPosition);
                 }
             }
         }
     }
 
     private void clearAllSelections() {
         selectedCards.clear();
     }
 
     private void swapCards(int cardNo1, int cardNo2) {
         Card card1 = myCards.get(cardNo1);
         Card card2 = myCards.get(cardNo2);
         myCards.set(cardNo1, card2);
         myCards.set(cardNo2, card1);
     }
 
     public void execute(Board board) {
         for (int i = 0; i < size; i++) {
            board.moveRobot(myCards.get(i).cardType);
         }
     }
     
     private ArrayList<Card> myCards;
     private Set<Integer> selectedCards;
     private int size;
 }
