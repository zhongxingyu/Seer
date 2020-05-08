 package se.liu.tdp024.entity;
 
 import java.io.Serializable;
import java.util.Date;
 import javax.persistence.*;
 
 @Entity
 public class SavedTransaction implements Serializable {
 
     private static final long serialVersionUID = 1L;
 
     @Id
     @GeneratedValue(strategy = GenerationType.AUTO)
     private long id;
 
     @Column(nullable = false)
     private long sender;
 
     @Column(nullable = false)
     private long reciever;
 
    @Temporal(TemporalType.TIMESTAMP)
    private Date datetime;
 
     private long amount;
     private boolean success;
 
    @PrePersist
    public void onCreate() {
        datetime = new Date();
    }

     /* Getters */
     public long getID() {
         return id;
     }
 
     public long getSender() {
         return sender;
     }
 
     public long getReciever() {
         return reciever;
     }
 
    public Date getDatetime() {
         // Vulnerability to return a reference to mutable object.
         // Return a clone instead
        return datetime == null ? null : (Date)datetime.clone();
     }
 
     public long getAmount() {
         return amount;
     }
 
     public boolean getSuccess() {
         return success;
     }
 
     /* Setters */
     public void setSender(long sender) {
         this.sender = sender;
     }
 
     public void setReciever(long reciever) {
         this.reciever = reciever;
     }
 
     public boolean setAmount(long amount) {
         if (amount > 0) {
             this.amount = amount;
             return true;
         }
         return false;
     }
 
     public void setSuccess(boolean success) {
         this.success = success;
     }
 
 }
