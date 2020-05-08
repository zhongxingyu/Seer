 package se.liu.ida.tdp024.account.data.impl.db.entity;
 
 import java.util.Date;
import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import se.liu.ida.tdp024.account.data.api.entity.Account;
 import se.liu.ida.tdp024.account.data.api.entity.Transaction;
 
@Entity
 public class TransactionDB implements Transaction {
 
     @Id
     @GeneratedValue(strategy = GenerationType.AUTO)
     private int id;
     
     private String transactionType;
     private String created;
     private Boolean status;
     private Account account;
     private int amount;
     
    public TransactionDB() {
    }
    
     public TransactionDB(String transactionType, int amount) {
         this.transactionType = transactionType;
         this.amount = amount;
         this.created = new Date().toString();
         this.status = false;
         this.account = null;
     }
     
     @Override
     public int getId() {
         return this.id;
     }
 
     @Override
     public String getType() {
         return this.transactionType;
     }
 
     @Override
     public int getAmount() {
         return this.amount;
     }
 
     @Override
     public String getDate() {
         return this.created;
     }
 
     @Override
     public Boolean getStatus() {
         return this.status;
     }
 
     @Override
     public Account getAccount(int id) {
         return this.account;
     }
 
     @Override
     public Boolean setStatus(Boolean status) {
         this.status = status;
         return this.status.equals(status);
     }
 
     @Override
     public Boolean setAccount(Account account) {
         this.account = account;
         return this.account == account;
     }
     
 }
