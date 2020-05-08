 package org.sukrupa.app.admin.subscribers;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import org.hibernate.*;
 import org.sukrupa.platform.RequiredByFramework;
 
 
 @Entity
 public class Subscriber {
 
     @Id
     @GeneratedValue
     private int id;
 
     @Column(name = "SUBSCRIBERNAME")
     private String subscriberName;
 
     @Column(name = "SUBSCRIBEREMAIL")
     private String subscriberEmail;
 
     @RequiredByFramework
     public Subscriber(){
     }
 
     public Subscriber(String subscriberName, String subscriberEmail){
         this.subscriberName=subscriberName;
         this.subscriberEmail=subscriberEmail;
     }
 
     public Subscriber(int id, String subscriberName, String subscriberEmail){
         this(subscriberName, subscriberEmail);
         this.id = id;
     }
 
     public String getSubscriberName(){
         return this.subscriberName;
     }
 
     public String getSubscriberEmail(){
         return this.subscriberEmail;
     }
 
    public long getSubscriberId() {
         return this.id;
     }
 
     public int hashCode(){
         return subscriberName.hashCode();
     }
 }
