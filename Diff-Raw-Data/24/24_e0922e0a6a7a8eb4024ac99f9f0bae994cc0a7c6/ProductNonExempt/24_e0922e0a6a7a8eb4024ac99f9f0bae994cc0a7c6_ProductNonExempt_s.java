 /***********************************************************
  * Author:      Mark Johnson
  * Date:        10/07/12
  * Modified:    10/21/12
  * 
  * Compilation: javac ProductNonExempt.java
  * Execution:   not applicable
  *
  * Defines a type for non-exempt products.
  **********************************************************/
 
 package salestax;
 
public class ProductNonExempt extends Product{
    private static final double basicSalesTax = 10.0;
    private static final double importDuty = 5.0;
     private final boolean imported;
     private final String name;
     private final double unitPrice;
     private final int quantity;
     
     public ProductNonExempt(int quantity, boolean imported, String name, double unitPrice) {
         if (quantity < 1) throw new java.lang.IllegalArgumentException();
         if (name.isEmpty()) throw new java.lang.IllegalArgumentException();
         if (unitPrice < 0) throw new java.lang.IllegalArgumentException();
         this.quantity = quantity;
         this.imported = imported;
         this.name = name;
         this.unitPrice = unitPrice;
     }
 
     public String name() {
         return this.name;
     }
 
     public double price() {
         return this.unitPrice + this.tax();
     }
 
     public int quantity() {
         return this.quantity;
     }
 
     public boolean isImported() {
         return imported;
     }
 
     public double tax() {
         if (imported) {
            return taxhelper(this.unitPrice * (importDuty + basicSalesTax) / 100);
         } else {
            return taxhelper(this.unitPrice * (basicSalesTax / 100));
         }
     }
 
     private double taxhelper(double rawTax) {
         return (Math.ceil(rawTax * 20.0) / 20);
     }
 
     public String toString() {
         return ("" + this.quantity() + " " + this.name() + " : " + this.price());
     }
 }
