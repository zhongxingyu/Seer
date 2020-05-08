 public class ChangeMaker {
        
     private float initialMoney, money;
     private int quarters = 0, dimes = 0, nickels = 0, pennies = 0;
 
     public ChangeMaker(float change) {
         money = initialMoney = change;
         this.calculateChange();
     }
 
     private void calculateChange() {
         calculateQuarters();
         calculateDimes();
         calculateNickels();
         calculatePennies();
     }
 
     private void calculateQuarters() {
       if (money >= .25) {
            quarters++;
            money -= .25;
            this.calculateQuarters();
        }
        return;
     }
 
     private void calculateDimes() {
       if (money >= .10) {
            dimes++;
            money -= .10;
            this.calculateDimes();
        }
        return;
     }
 
     private void calculateNickels() {
       if (money >= .05) {
            nickels++;
            money -= .05;
            this.calculateNickels();
        }
        return;
     }
    
     private void calculatePennies() {
       pennies = (int) (money * 100.0); 
     }
 
     public int getQuarters() {
         return quarters;
     }
     
     public int getDimes() {
         return dimes;
     }
     
     public int getNickels() {
         return nickels;
     }
     
     public int getPennies() {
         return pennies;
     }
 
     //OVERRIDE - toString
 }
