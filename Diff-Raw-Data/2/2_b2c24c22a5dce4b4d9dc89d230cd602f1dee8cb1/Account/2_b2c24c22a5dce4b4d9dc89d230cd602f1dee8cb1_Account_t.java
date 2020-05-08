 package com.financial.pyramid.domain;
 
 import com.financial.pyramid.domain.type.TransactionType;
 import org.hibernate.annotations.Cache;
 import org.hibernate.annotations.CacheConcurrencyStrategy;
 import org.hibernate.annotations.OnDelete;
 import org.hibernate.annotations.OnDeleteAction;
 
 import javax.persistence.*;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Set;
 
 /**
  * User: Danil
  * Date: 25.08.13
  * Time: 0:46
  */
 @Entity
 @Table(name = "account")
 @Cacheable
 @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
 public class Account extends AbstractEntity implements Serializable {
 
     @Basic
     @Column(name = "balance")
     private Long balance = 0L;
 
     @Column(name="date_activated", nullable = true)
     private Date dateActivated;
 
     @Column(name="date_expired", nullable = true)
     private Date dateExpired;
 
     @Column(name="earnings_sum", nullable = false)
     private Double earningsSum;
 
     @Column(name="locked", nullable = false)
     private boolean locked;
 
     @OneToMany(fetch = FetchType.LAZY, mappedBy="account", cascade = CascadeType.ALL)
     @OrderBy("created")
     private List<Transaction> transactions = new ArrayList<Transaction>();
 
     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if (!(o instanceof Account)) return false;
         Account account = (Account) o;
         if (earningsSum != null ? !earningsSum.equals(account.earningsSum) : account.earningsSum != null) return false;
         if (dateExpired != null ? !dateExpired.equals(account.dateExpired) : account.dateExpired != null) return false;
         if (dateActivated != null ? !dateActivated.equals(account.dateActivated) : account.dateActivated != null) return false;
         return true;
     }
 
     @Override
     public int hashCode() {
         int result = earningsSum != null ? earningsSum.hashCode() : 0;
         result = 31 * result + (dateExpired != null ? dateExpired.hashCode() : 0);
         result = 31 * result + (dateActivated != null ? dateActivated.hashCode() : 0);
         return result;
     }
 
     @Transient
     public void writeOFF(Long count) {
         this.balance -= count;
         Transaction transaction = new Transaction();
         transaction.setAccount(this);
         transaction.setType(TransactionType.OUT);
         transaction.setCount(count);
         transaction.setBalance(this.balance);
         transactions.add(transaction);
     }
 
     @Transient
     public void writeIN(Long count) {
         this.balance += count;
         Transaction transaction = new Transaction();
         transaction.setAccount(this);
         transaction.setType(TransactionType.IN);
         transaction.setCount(count);
         transaction.setBalance(this.balance);
         transactions.add(transaction);
     }
 
     public Long getBalance() {
         return balance;
     }
 
     public List<Transaction> getTransactions() {
         return transactions;
     }
 
     public Date getDateActivated() {
         return dateActivated;
     }
 
     public void setDateActivated(Date dateActivated) {
         this.dateActivated = dateActivated;
     }
 
     public Date getDateExpired() {
         return dateExpired;
     }
 
     public void setDateExpired(Date dateExpired) {
         this.dateExpired = dateExpired;
     }
 
     public boolean isLocked() {
        return this.locked;
     }
 
     public void setLocked(boolean locked) {
         this.locked = locked;
     }
 
     public Double getEarningsSum() {
         return earningsSum;
     }
 
     public void setEarningsSum(Double earningsSum) {
         this.earningsSum = earningsSum;
     }
 }
