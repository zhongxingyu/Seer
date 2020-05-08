 package com.sun.identity.admin.model;
 
 import java.io.Serializable;
 
 public class BankingResource extends Resource implements Serializable {
     public static final BankingResource ALL_ACCOUNTS;
 
     static {
         ALL_ACCOUNTS = new BankingResource();
         ALL_ACCOUNTS.setName("*");
     }
 
     private ViewSubject owner = null;
 
     public BankingResource deepClone() {
         BankingResource br = new BankingResource();
         br.setName(getName());
 
         return br;
     }
 
     public ViewSubject getOwner() {
         return owner;
     }
 
    @Override
    public String toString() {
        if (owner != null) {
            return getTitle() + " (" + owner.getTitle() + ")";
        }
        return super.toString();
    }

     public void setOwner(ViewSubject owner) {
         this.owner = owner;
     }
 }
