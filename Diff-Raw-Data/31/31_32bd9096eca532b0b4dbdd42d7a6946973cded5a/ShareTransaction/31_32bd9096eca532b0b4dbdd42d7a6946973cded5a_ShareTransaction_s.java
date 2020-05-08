 package org.tomhume.webapps;
 
 import java.io.Serializable;
 import java.util.Date;
 import javax.persistence.*;
 
 /**
  *  Class encapsulating a single transaction of shares.
  * 
  * @author twhume
  */
 @Entity
 @Table(name="ShareTransaction") 
 public class ShareTransaction implements Serializable {
     private static final long serialVersionUID = 1L;
     @Id
     @GeneratedValue(strategy = GenerationType.AUTO)
     private long id;            /* Unique identifier for this transaction */
     private String company;    /* company in whom shares are being bought */
     private double amount;      /* # of shares bought, allowing for fractions
                                  * and negative if this was a sale */
     private int pricePaid;     /* Amount paid for these shares, in cents */
     @Temporal(javax.persistence.TemporalType.TIMESTAMP)
     private Date when;          /* Timestamp for when transaction was created */
 
     public static final String[] COMPANIES =
     {"Google" , "Apple" , "Oracle" , "Microsoft"};
 
     public ShareTransaction() {
         /* By default, transactions are presumed to be created "now" */
         when = new Date();
         
     }
 
     public Date getWhen() {
         return when;
     }
 
     public void setWhen(Date when) {
         this.when = when;
     }
     
     /**
      * Get the value of amount
      *
      * @return the value of amount
      */
     public double getAmount() {
         return amount;
     }
 
     /**
      * Set the value of amount
      *
      * @param amount new value of amount
      */
     public void setAmount(double amount) {
         this.amount = amount;
     }
 
     /**
      * Get the value of pricePaid
      *
      * @return the value of pricePaid
      */
     public long getPricePaid() {
         return pricePaid;
     }
 
     /**
      * Set the value of pricePaid
      *
      * @param pricePaid new value of pricePaid
      */
     public void setPricePaid(int pricePaid) {
         this.pricePaid = pricePaid;
     }
 
     public long getId() {
         return id;
     }
 
     public void setId(long id) {
         this.id = id;
     }
 
     /**
      * Get the value of company
      *
      * @return the value of company
      */
     public String getCompany() {
         return company;
     }
 
     /**
      * Set the value of company
      *
      * @param company new value of company
      */
     public void setCompany(String Company) {
         this.company = Company;
     }
 
     public String getTransactionType() {
        if (this.pricePaid<0) return "Sale";
         else return "Purchase";
     }
 
    public int getTransactionValue() {
        return Math.abs(this.pricePaid);
     }

     @Override
     public int hashCode() {
         int hash = 0;
         hash += (int) id;
         return hash;
     }
 
     @Override
     public boolean equals(Object object) {
         // TODO: Warning - this method won't work in the case the id fields are not set
         if (!(object instanceof ShareTransaction)) {
             return false;
         }
         ShareTransaction other = (ShareTransaction) object;
         if (this.id != other.id) {
             return false;
         }
         return true;
     }
 
     @Override
     public String toString() {
         return "org.tomhume.webapps.ShareTransaction[ id=" + id + " ]";
     }
     
 }
