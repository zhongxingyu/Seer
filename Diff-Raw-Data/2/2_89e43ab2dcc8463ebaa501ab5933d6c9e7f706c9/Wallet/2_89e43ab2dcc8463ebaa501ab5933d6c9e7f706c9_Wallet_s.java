 package game.misc;
 
 public class Wallet {
   private int money;
 
   public Wallet(int start) {
     assert start >= 0;
 
     this.money = start;
   }
 
   public void addMoney(int value) {
     assert value >= 0;
 
     money += value;
   }
 
   public boolean takeMoney(int value) {
     assert value >= 0;
 
    if (value >= money) {
       money -= value;
       return true;
     } else {
       return false;
     }
   }
 
   public int getMoney() {
     return money;
   }
 }
