 package com.exadel.borsch.managers;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 
 /**
  * @author Vlad
  */
@Service
 public final class ManagerFactory {
 
     @Autowired
     private UserManager userManager;
     @Autowired
     private OrderManager orderManager;
     @Autowired
     private PriceManager priceManager;
 
     public UserManager getUserManager() {
         return userManager;
     }
 
     public OrderManager getOrderManager() {
         return orderManager;
     }
 
     public PriceManager getPriceManager() {
         return priceManager;
     }
 }
