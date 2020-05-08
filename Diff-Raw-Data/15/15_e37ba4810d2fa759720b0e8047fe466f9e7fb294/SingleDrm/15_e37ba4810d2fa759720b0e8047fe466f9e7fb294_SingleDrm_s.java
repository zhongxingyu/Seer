/**
  * Copyright 2013 Mat Booth <mbooth@apache.org>
  */
 package com.gpm.calameo;
 
 public class SingleDrm {
   private String ID;
   private String SubscriptionID;
   private String SubscriberLogin;
   private Publication Book;
   private String Extras;
 
   public SingleDrm() {
   }
 
   public String getId() {
     return ID;
   }
 
   public String getSubscriptionId() {
     return SubscriptionID;
   }
 
   public String getSubscriberLogin() {
     return SubscriberLogin;
   }
 
   public Publication getBook() {
     return Book;
   }
 
   public String getExtras() {
     return Extras;
   }
 
   @Override
   public String toString() {
     return String.format("{ ID: %s, Subscription ID: %s, Subscriber Login: %s, Book: %s, Extras: %s }", getId(),
         getSubscriptionId(), getSubscriberLogin(), getBook(), getExtras());
   }
 }
