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
  
    boolean checkFlush(List<Card> cards){
        Card temp;
        temp = cards.get(0);
        for (Card card: cards) {
            if (!card.suit.equals(temp.suit)) {
                return false;
             }
         }
        return true;
     }
     
    boolean checkStrait(List<Card> cards){
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
    public int evaluate(List<Card> cards){
 
         return this.rank;
     }
 }
