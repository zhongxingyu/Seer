 package de.htwg.wzzrd.model.tableComponent.impl;
 
 import java.io.Serializable;
 
 import de.htwg.wzzrd.model.tableComponent.CardInterface;
 
 public class Card implements Comparable<Card>, Serializable, CardInterface {
     private static final long serialVersionUID = 1L;
     private int nation;
     private int value;
 
     public Card(int n, int v) {
         this.nation = n;
         this.value = v;
     }
 
     @Override
     public int getNation() {
         return this.nation;
     }
 
     @Override
     public int getValue() {
         return this.value;
     }
 
     @Override
     public String toString() {
         return CardNames.getName(this);
     }
 
     @Override
     public boolean equals(Object o) {
         if (!(o instanceof Card)) {
             return false;
         } else {
             Card c = (Card) o;
             return c.nation == this.nation && c.value == this.value;
         }
     }
 
     /**
      * Returns true if the specified card is higher than this card.
      * 
      * @param card
      * @param trump
      * @return
      */
     @Override
     public boolean isLessThan(CardInterface card, int trump) {
         boolean retval;
         if (this.value == 0) {
             // fool will never get the trick if it wasn't the first card played
             if (card.getValue() == 0) {
                 retval = false;
             } else {
                 retval = true;
             }
         } else if (this.value == CardNames.getCardsPerNation() - 1) {
             // wizard on table, no other trick possible
             retval = false;
         } else if (card.getValue() == CardNames.getCardsPerNation() - 1) {
             // first wizard gets the trick
             retval = true;
         } else if (card.getNation() == trump) {
            if (this.nation != trump || card.getValue() > this.value) {
                 // no trump on table or new trump higher
                 retval = true;
             } else {
                 // trump was lower than existing trump
                 retval = false;
             }
         } else if (card.getNation() == this.nation) {
             if (card.getValue() > this.value) {
                 // new card is higher than previous
                 retval = true;
             } else {
                 // new card is lower or equal to the previous
                 retval = false;
             }
         } else {
             retval = false;
         }
         return retval;
     }
 
     @Override
     public int hashCode() {
         return (String.format("%d:%d", nation, value)).hashCode();
     }
 
     @Override
     public int compareTo(Card card) {
         return ((Integer) this.value).compareTo(card.value);
     }
 }
