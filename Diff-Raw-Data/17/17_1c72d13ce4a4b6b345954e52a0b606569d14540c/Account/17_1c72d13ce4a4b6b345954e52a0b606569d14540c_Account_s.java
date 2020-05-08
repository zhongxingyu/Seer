 package com.tpg.tmjug.springdata.demo.jpa.entities;
 
 import javax.persistence.Entity;
 
 @Entity
 public class Account extends AbstractEntity {
 
     private long balance;
 
    public Account(final long balance) {
         this.balance = balance;
     }
 
     protected Account() {
     }
 
     public long getBalance() {
         return balance;
     }
 
 }
