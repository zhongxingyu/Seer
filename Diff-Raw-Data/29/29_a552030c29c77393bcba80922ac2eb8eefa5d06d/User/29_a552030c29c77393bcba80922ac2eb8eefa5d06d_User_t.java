 package com.team.xslides.domain;
 
 import java.util.Set;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.OneToMany;
 import javax.persistence.Table;
 
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

 @Entity
 @Table(name = "Users")
 public class User {
     @Id
     @Column(name = "Id")
     @GeneratedValue(strategy = GenerationType.AUTO)
     private Integer id;
 
     @Column(name = "Displayname", length = 40)
     private String displayname;
     
     @Column(name = "Email", nullable = false, unique = true, length = 60)
     private String email;
 
     @Column(name = "Password", nullable = false, length = 40)
     private String password;
     
     @Column(name = "Confirmed")
     private Boolean confirmed;
     
     @Column(name = "Admin")
     private Boolean admin;
     
    @OneToMany(mappedBy = "author", fetch = FetchType.LAZY)
    @Cascade(value = CascadeType.ALL)
     private Set<Presentation> presentations;
 
     public Integer getId() {
         return id;
     }
 
     public void setId(Integer id) {
         this.id = id;
     }
 
     public String getDisplayname() {
         return displayname;
     }
 
     public void setDisplayname(String displayname) {
         this.displayname = displayname;
     }
 
     public String getEmail() {
         return email;
     }
 
     public void setEmail(String email) {
         this.email = email;
     }
 
     public String getPassword() {
         return password;
     }
 
     public void setPassword(String password) {
         this.password = password;
     }
 
     public Boolean getConfirmed() {
         return confirmed;
     }
 
     public void setConfirmed(Boolean confirmed) {
         this.confirmed = confirmed;
     }
 
     public Boolean getAdmin() {
         return admin;
     }
 
     public void setAdmin(Boolean admin) {
         this.admin = admin;
     }
 
     public Set<Presentation> getPresentations() {
         return presentations;
     }
 
     public void setPresentations(Set<Presentation> presentations) {
         this.presentations = presentations;
     }
 }
