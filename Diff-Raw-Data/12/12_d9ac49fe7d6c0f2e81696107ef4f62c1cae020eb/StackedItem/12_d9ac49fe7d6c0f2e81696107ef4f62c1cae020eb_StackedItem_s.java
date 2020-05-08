 package com.icbat.game.tradesong;
 
 public class StackedItem {
     private Item baseItem;
     /** Initializes with one item stacked. Add more if necessary */
     private int count = 1;
 
 
     public StackedItem(Item item) {
         baseItem = item;
     }
 
     public Item getBaseItem() {
         return baseItem;
     }
 
     public int getCount() {
         return count;
     }
 
     public void setCount(int count) {
         this.count = count;
     }
 
     /** Where the magic happens.
      *
      * @return true if add ends in appropriate params successfully
      * */
     public boolean add(int i) {
         int newTotal = count + i;
        if (newTotal <= baseItem.getMaxStack() && newTotal >= 0) {
             count = newTotal;
             return true;
         }
         else {
             return false;
         }
     }
 
     public boolean add() {
         return add(1);
     }
 
     public boolean remove() {
         return add(-1);
     }
 
     public boolean remove(int i) {
        return add(-1 * i);
     }
 }
