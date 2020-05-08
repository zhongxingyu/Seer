 package se.liu.ida.tdp024.account.data.impl.db.entity;
 
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import se.liu.ida.tdp024.account.data.api.entity.Account;
 
 @Entity
 public class AccountDB implements Account {
 
     @Id
     @GeneratedValue(strategy = GenerationType.AUTO)
     private int id;
     
     private String accountType;
     private String personKey;
     private String bankKey;
     private int holdings;
 
     public AccountDB() {
     }
     
    public AccountDB() {
    }
     
     public AccountDB(String accountType, String name, String bank) {
         this.accountType = accountType;
         this.personKey = name;
         this.bankKey = bank;
     }
     
     @Override
     public int getId() {
         return this.id;
     }
 
     @Override
     public String getPersonKey() {
         return this.personKey;
     }
 
     @Override
     public String getAccountType() {
         return this.accountType;
     }
 
     @Override
     public String getBankKey() {
        return this.bankKey;
     }
 
     @Override
     public Boolean debitAccount(int amount) {
         this.holdings -= amount;
         return true;
     }
 
     @Override
     public Boolean creditAccount(int amount) {
         this.holdings += amount;
         return true;
     }
 
     @Override
     public int getHoldings() {
         return holdings;
     }
     
 }
