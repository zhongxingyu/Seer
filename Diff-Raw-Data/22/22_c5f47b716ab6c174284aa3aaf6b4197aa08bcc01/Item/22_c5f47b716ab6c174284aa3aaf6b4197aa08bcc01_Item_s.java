 package com.mustangexchange.polymeal;
 
 import java.math.BigDecimal;
 
 /**
  * Created by Blake on 9/4/13.
  */
 public class Item
 {
     private String name = null;
     private BigDecimal price = null;
     private String description;
     private boolean isValid;
     private boolean ounces = false;
     public Item(String name, BigDecimal price, String description, boolean isValid)
     {
         this.name = name;
         this.price = price;
         this.description = description;
         this.isValid = isValid;
     }
 
     public Item(Item item, BigDecimal price) {
         this.name = item.getName();
         this.price = price;
         this.description = item.getDescription();
         this.isValid = item.getValid();
     }
 
     public Item(){}
 
     public void setName(String title)
     {
         this.name = title;
     }
 
     public String getName()
     {
         return this.name;
     }
 
     public BigDecimal getPrice()
     {
         if(price != null)
         {
            return price;
         }
         else
         {
             return new BigDecimal("0.00");
         }
     }
 
     public String getPriceString()
     {
         if(price!=null)
         {
            return "$"+price;
         }
         else
         {
             return "";
         }
     }
 
     public void setPrice(BigDecimal price)
     {
         this.price = price;
     }
 
     public String getDescription()
     {
         return description;
     }
 
     public void setDescription(String description)
     {
         this.description = description;
     }
 
     public void setValid(boolean valid)
     {
         isValid = valid;
     }
 
     public boolean getValid()
     {
         return isValid;
     }
 
     public void setOunces(boolean ounces)
     {
         this.ounces = ounces;
     }
 
     public boolean getOunces()
     {
         return ounces;
     }
 }
