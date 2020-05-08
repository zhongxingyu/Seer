 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package model;
 
 /**
  *
  * @author Neal
  */
 public class MenuItem {
     private String itemName;
     private double itemPrice;
     private String value;
     
     public MenuItem(String itemName, double itemPrice){
         this.itemName = itemName;
         this.itemPrice = itemPrice;
         this.value = itemName + "/" + itemPrice;
     }
 
     public String getItemName() {
         return itemName;
     }
 
     public void setItemName(String itemName) {
         this.itemName = itemName;
     }
 
     public double getItemPrice() {
         return itemPrice;
     }
 
     public void setItemPrice(double itemPrice) {
         this.itemPrice = itemPrice;
     }
     
     public String getValue() {
        return value;
     }
 
     public void setValue() {
         this.value = this.itemName + "/" + this.itemPrice;
     }
     
     @Override
     public int hashCode() {
         int hash = 5;
         hash = 23 * hash + (this.itemName != null ? this.itemName.hashCode() : 0);
         hash = 23 * hash + (int) (Double.doubleToLongBits(this.itemPrice) ^ (Double.doubleToLongBits(this.itemPrice) >>> 32));
         return hash;
     }
 
     @Override
     public boolean equals(Object obj) {
         if (obj == null) {
             return false;
         }
         if (getClass() != obj.getClass()) {
             return false;
         }
         final MenuItem other = (MenuItem) obj;
         return true;
     }
 }
