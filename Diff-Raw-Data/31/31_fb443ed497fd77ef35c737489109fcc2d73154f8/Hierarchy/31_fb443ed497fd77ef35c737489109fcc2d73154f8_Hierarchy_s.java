 package fivecardstud;
 import java.util.ArrayList;
 import java.util.List;
 
 public class Hierarchy {
     
     public Hierarchy() {
 }
 
     Card getHighCard(List<Card> cards){
         Card highCard;
         highCard = cards.get(0);
         for (Card card: cards) {
             if (card.value > highCard.value) highCard = card;
         }
         return highCard;
     }
  
    List checkFlush(List<Card> cards){
        String suitTemp;
        suitTemp = cardsBySuit.get(0);
        cardsTemp.add(cards.get(0));
        skipIter = true;
        int i = 0;
        for (String suit: cardsBySuit) {
            // skip first iteration
            if (skipIter == true) {
                skipIter = false;
                continue;
            }
            i++;
            if (suitTemp.equals(suit)) {
                suitTemp = suit;
                cardsTemp.add(cards.get(i));
             }
         }
        return cardsTemp;
     }
     
    boolean checkStrait(){
         skipIter = true;
         valueTemp = cardsByValue.get(0);
         for (int value: cardsByValue) {
             // skip first iteration
             if (skipIter == true) {
                 skipIter = false;
                 continue;
             }
             valueTemp+=1;
             if (valueTemp == value) {
                 valueTemp = value;
             } else {
                 return false;
             }
         }
         return true;
     }
     
     
     
     // rank hands from 0 being the worst and up to the best
    public int evaluate(){
 
         return this.rank;
     }
 }
