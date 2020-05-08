 package com.github.notyy.testing.priceCalculatorWithDI;
 
 import com.github.notyy.testing.common.DiscountDAO;
 import com.github.notyy.testing.common.MessageSender;
 
 public class PriceCalculator {
 
     private DiscountDAO discountDAO;
     private MessageSender messageSender;
 
     public PriceCalculator(DiscountDAO discountDAO, MessageSender messageSender) {
         this.discountDAO = discountDAO;
         this.messageSender = messageSender;
     }
 
     public double getPrice(int quantity, double itemPrice) {
         double basePrice = quantity * itemPrice;
        if(basePrice < 100) {
             throw new IllegalArgumentException("quantity must be >= 100");
         }
 
         double discountFactor = discountDAO.findDiscount(basePrice);
         double resultPrice = basePrice * discountFactor;
         messageSender.send("resultPrice:"+resultPrice);
         return resultPrice;
     }
 
 }
