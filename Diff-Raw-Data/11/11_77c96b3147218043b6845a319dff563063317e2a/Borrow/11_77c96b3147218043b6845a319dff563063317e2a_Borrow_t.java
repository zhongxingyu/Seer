 package cz.muni.fi.pv168;
 
 import java.util.Calendar;
 
 /**
  * Created by IntelliJ IDEA.
  * User: fivekeyem, janinko
  * Date: 3/14/11
  */
 public class Borrow {
 
     private int id;
     private CD cd;
     private Customer customer;
     private boolean active;
     private Calendar from;
     private Calendar to;
 
 
     public Borrow() {
         this.id = 0;
         this.cd = null;
         this.customer = null;
         this.active = false;
         this.from = null;
         this.to = null;
     }
 
     public Borrow(int id) {
         this.id = id;
         this.cd = null;
         this.customer = null;
         this.active = false;
         this.from = null;
         this.to = null;
     }
 
     public Borrow(int id, CD cd, Customer customer, boolean active, Calendar from, Calendar to) {
         this.id = id;
         this.cd = cd;
         this.customer = customer;
         this.active = active;
         this.from = from;
         this.to = to;
     }
 
     public int getId() {
         return id;
     }
 
     public void setId(int id) {
         this.id = id;
     }
 
     public CD getCd() {
         return cd;
     }
 
     public void setCd(CD cd) {
         this.cd = cd;
     }
 
     public Customer getCustomer() {
         return customer;
     }
 
     public void setCustomer(Customer customer) {
         this.customer = customer;
     }
 
     public boolean isActive() {
         return active;
     }
 
     public void setActive(boolean active) {
         this.active = active;
     }
 
 
     public Calendar getFrom() {
         return from;
     }
 
     public void setFrom(Calendar from) {
         this.from = from;
     }
 
     public Calendar getTo() {
         return to;
     }
 
     public void setTo(Calendar to) {
         this.to = to;
     }
 
     @Override
     public boolean equals(Object o){
         if(! (o instanceof Borrow))
             return false;
 
         Borrow oo = (Borrow) o;
 
         if(this.getId() != oo.getId())
             return false;
        if(!this.getCustomer().equals(oo.getCustomer()))
             return false;
        if(!(this.getCd().equals(oo.getCd())))
             return false;
        if(!(this.getFrom().equals(oo.getFrom())))
             return false;
        if(!(this.getFrom().equals(oo.getFrom())))
             return false;
 
         return true;
     }
 }
