 /*******************************************************
  * Author:      Mark Johnson
  * Date:        10/07/12
  * Modified:    10/21/12
  *
  * Compilation: javac ProductNonExempt.java
  * Execution: not applicable
  *
  * Defines a type for exempt products.
  ******************************************************/
 
 package salestax;
 
 public class ProductExempt extends Product {
     private static final double BASIC_SALES_TAX = 0.0;
     private static final double IMPORT_DUTY = 5.0;
     private final boolean imported;
     private final String name;
     private final double unitPrice;
     private final int quantity;
 
     public ProductExempt(int quantity, boolean imported, String name, double unitPrice) {
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
         return this.imported;
     }
 
     public double tax() {
         if (imported) {
            return taxhelper(this.unitPrice * (IMPORTDUTY + BASIC_SALES_TAX) / 100);
         } else {
             return 0;
         }
     }
 
     private double taxhelper(double rawTax) {
         return (Math.ceil(rawTax * 20) / 20);
     }
 
     public String toString() {
         return ("" + this.quantity() + " " + this.name() + " : " + this.price());
     }
 }
