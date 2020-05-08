  /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package discountstategies;
 
 /**
  *
  * @author andrew
  */
 public class CashRegister {
     
    Receipt receipt = new Receipt();
     
     
     public void startNewSale(String customerNo) {
       receipt.startNewSale(customerNo);    
     }
     
     public void addProduct(String productId, int qty){
         receipt.addNewLineItem(productId, qty);
     }
     
     public void finalizeSale() {
         receipt.displayReceipt();
     }
     
 }
