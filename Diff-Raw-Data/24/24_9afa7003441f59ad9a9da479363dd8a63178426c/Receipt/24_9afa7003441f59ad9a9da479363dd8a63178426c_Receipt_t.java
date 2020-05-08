 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package sac.discountstrategy;
 
 import java.text.DecimalFormat;
 
 /**
  *
  * @author Stuart - HP AMD 10
  */
 public class Receipt {
     
     private String receiptItem;
     private double totalDue;
     private final static String RECEIPT_HEADING1
             = "Thank you for shopping at Lombardo's\n";
     //public Receipt(String customer, LineItem[] lineItems)
     
     //public void createReceipt(Customer cust, LineItem[] lineItems) {
     public void createReceipt(LineItem[] lineItems) {    
         System.out.println(RECEIPT_HEADING1);
         DecimalFormat dollars = new DecimalFormat("#00.00");
         for (int sub = 0; sub < lineItems.length; sub++ ) {
//            System.out.println(lineItems[sub].getItemId()
//                    + "\t" + lineItems[sub].getItemDescription()
//                    + "\n"
//                    + lineItems[sub].getQuanity()
//                    + "\t$" + dollars.format(lineItems[sub].getUnitPrice())
//                    + "\t$" + dollars.format(lineItems[sub].getOriginalRetailPriceTotal())
//                    + "\n\t\t$" + dollars.format(lineItems[sub].getDiscount())
//                    + "\t$" + dollars.format(lineItems[sub].getNetPrice()));
             System.out.println(lineItems[sub].getItemId()
                    + "\t" + lineItems[sub].getItemDescription());
            System.out.print(lineItems[sub].getQuanity());
            System.out.printf("\t%3.2f", lineItems[sub].getUnitPrice());
            System.out.printf("\t%3.2f", 
                    lineItems[sub].getOriginalRetailPriceTotal());
            System.out.printf("\n    discount\t%3.2f", lineItems[sub].getDiscount());
            System.out.printf("\t%3.2f", lineItems[sub].getNetPrice());
            System.out.println("");
                     
         }
     }
             
     
 }
