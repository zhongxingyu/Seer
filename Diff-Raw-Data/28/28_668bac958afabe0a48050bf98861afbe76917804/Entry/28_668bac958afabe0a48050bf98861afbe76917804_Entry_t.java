 package com.oppian.oikos.model;
 
 import java.io.Serializable;
import java.math.BigDecimal;
 import java.util.Date;
 
 import com.j256.ormlite.field.DatabaseField;
 import com.j256.ormlite.table.DatabaseTable;
 
 @DatabaseTable
 public class Entry implements Serializable {
 
     private static final long  serialVersionUID = -731479195234492944L;
 
     public static final String DATE_FIELD_NAME  = "entryDate";
 
     @DatabaseField(generatedId = true)
     private Integer            id;
 
     @DatabaseField(canBeNull = false)
     private int                amount;
 
     @DatabaseField
     private String             description;
 
     @DatabaseField(canBeNull = false, columnName = DATE_FIELD_NAME, index = true)
     private Date               entryDate;
 
     @DatabaseField(canBeNull = false, foreign = true)
     private Account            account;
 
     public Entry() {
 
     }
 
     public Entry(Account account, int amount, String description) {
         this.account = account;
         this.amount = amount;
         this.description = description;
         this.entryDate = new Date();
     }
 
     @Override
     public String toString() {
         return "Entry [account=" + account + ", amount=" + amount + ", description=" + description + ", entryDate="
                 + entryDate + ", id=" + id + "]";
     }
 
     public Integer getId() {
         return id;
     }
 
     public void setId(Integer id) {
         this.id = id;
     }
 
     public int getAmount() {
         return amount;
     }
    
    public BigDecimal getAmountBigDecimal() {
        return BigDecimal.valueOf(amount, 2);
    }
 
     public void setAmount(int amount) {
         this.amount = amount;
     }
 
     public String getDescription() {
         return description;
     }
 
     public void setDescription(String description) {
         this.description = description;
     }
 
     public Date getEntryDate() {
         return entryDate;
     }
 
     public void setEntryDate(Date entryDate) {
         this.entryDate = entryDate;
     }
 
     public Account getAccount() {
         return account;
     }
 
     public void setAccount(Account account) {
         this.account = account;
     }
 }
