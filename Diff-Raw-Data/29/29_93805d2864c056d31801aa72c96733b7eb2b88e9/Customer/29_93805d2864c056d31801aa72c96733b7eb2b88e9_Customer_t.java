 package com.mycompany.booking.core;
 
 import java.io.Serializable;
 import javax.persistence.Entity;
 import javax.persistence.Transient;
 
 /**
  * A Customer
  * @author hajo
  */
 @Entity
 public class Customer extends AbstractEntity implements Serializable{
    
     @Transient
     private Cart cart = new Cart();
     private String fname;
     private String lname;
     private String pNumber;
     private String email;
     private String username;
     private String password;
 
     public Customer() {
     }
 
    public Customer(String fname, String lname, String username, String password,
            String pNumber, String email) {
 
         //this.address = address;
         this.fname = fname;
         this.lname = lname;
         this.pNumber = pNumber;
         this.email = email;
         this.username=username;
         this.password=password;
 
     }
     
     public Customer(Long id, String fname,
             String lname, String pNumber,String email) {
         super(id);
        // this.address = address;
         this.fname = fname;
         this.lname = lname;
         this.pNumber = pNumber;
         this.email = email;
     }
 
     public void addTicketToCart(Ticket ticket) {
         cart.add(ticket);
     }
 
     public void removeTicketFromCart(Ticket ticket) {
         cart.remove(ticket);
     }
 
     public void emptyCart() {
         cart = new Cart();
     }
 
    /* public Address getAddress() {
         return address;
     }*/
 
     public Cart getCart() {
         return cart;
     }
 
     public String getEmail() {
         return email;
     }
     
     public String getPass() {
         return password;
     }
 
     public String getUserName() {
         return username;
     }
 
 
     public String getFname() {
         return fname;
     }
 
     public String getLname() {
         return lname;
     }
     
     public String getPnumber(){
         return pNumber;
     }
 
     @Override
     public String toString() {
         return "Customer{" + "id=" + getId() + ", fname=" + fname + ", lname=" + lname + ", pNumber=" + pNumber + ", email=" + email + '}';
     }
 
     public void setId(Long id) {
         this.id = id;
     }
 }
 
