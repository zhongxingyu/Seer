 package com.socialcomputing.wps.server.persistence.hibernate;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 import javax.persistence.Table;
 
 @Entity
 @Table(name = "users")
 public class Users {
 
     @Id
     @Column(name = "id")
    @GeneratedValue(strategy=GenerationType.AUTO)
     int id;
 
     @Column(name = "username", columnDefinition = "varchar(255)")
     String username;
     
     @Column(name = "password", columnDefinition = "varchar(255)")
     String password;
     
     @Column(name = "firstname", columnDefinition = "varchar(255)")
     String firstname;
     
     @Column(name = "lastname", columnDefinition = "varchar(255)")
     String lastname;
     
     @Column(name = "email", columnDefinition = "varchar(255)")
     String email;
 
     @ManyToOne(cascade = CascadeType.ALL)
     @JoinColumn(name="account_id")
     private Accounts account;
     
     public Users() {
         
     }
     
     public int getId() {
         return id;
     }
 
     public void setId(int id) {
         this.id = id;
     }
 
     public String getUsername() {
         return username;
     }
 
     public void setUsername(String username) {
         this.username = username;
     }
 
     public String getPassword() {
         return password;
     }
 
     public void setPassword(String password) {
         this.password = password;
     }
 
     public String getFirstname() {
         return firstname;
     }
 
     public void setFirstname(String firstname) {
         this.firstname = firstname;
     }
 
     public String getLastname() {
         return lastname;
     }
 
     public void setLastname(String lastname) {
         this.lastname = lastname;
     }
 
     public String getEmail() {
         return email;
     }
 
     public void setEmail(String email) {
         this.email = email;
     }
     
     public Accounts getUserAccount() {
         return this.account;
     }
     
 }
