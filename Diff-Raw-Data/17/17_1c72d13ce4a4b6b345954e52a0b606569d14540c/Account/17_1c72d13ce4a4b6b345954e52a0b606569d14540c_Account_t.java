 package com.tpg.tmjug.springdata.demo.jpa.entities;
 
 import javax.persistence.Entity;
 
 @Entity
 public class Account extends AbstractEntity {
 
     private long balance;
 
    public Account(Long id, Long balance) {
        super(id);
         this.balance = balance;
     }
 
     protected Account() {
     }
 
     public long getBalance() {
         return balance;
     }
 
 }
