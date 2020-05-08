 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package discountstategies;
 
 /**
  *
  * @author andrew
  */
 public class Startup {
 
     /**
      * @param args the command line arguments
      */
     public static void main(String[] args) {
        CashRegister cr = new CashRegister();
        
        cr.startNewSale("101");
        cr.addProduct("HW1002", 1);
       cr.addProduct("HW1003", 3);
       cr.addProduct("HW1004", 1);
       cr.finalizeSale();
     }
 }
