 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.artivisi.training.domain;
 
 import java.util.ArrayList;
 import java.util.List;
 import javax.persistence.CollectionTable;
 import javax.persistence.Column;
 import javax.persistence.ElementCollection;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 import javax.persistence.Table;
 import javax.validation.constraints.NotNull;
 import javax.validation.constraints.Size;
 import org.hibernate.validator.constraints.NotEmpty;
 
 /**
  *
  * @author endy
  */
 @Entity @Table(name="t_user")
 public class User {
     @Id @GeneratedValue
     private Integer id;
     
     @NotNull
     @NotEmpty
     @Column(nullable=false,unique=true)
     private String username;
     
     @NotEmpty
     @Size(min=4, max=255)
     private String password;
     
     @ElementCollection
     @CollectionTable(
             name="t_user_email", 
             joinColumns=@JoinColumn(name="id_user")
     )
     @Column(name="email")
     private List<String> daftarEmail 
             = new ArrayList<String>();
 
     
     @ElementCollection
     @CollectionTable(
             name="t_user_phone", 
             joinColumns=@JoinColumn(name="id_user")
     )
     private List<Phone> daftarTelepon 
             = new ArrayList<Phone>();
     
    @NotNull
     @ManyToOne
     @JoinColumn(name="id_role")
     private Role role;
     
     public Integer getId() {
         return id;
     }
 
     public void setId(Integer id) {
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
 
     public List<String> getDaftarEmail() {
         return daftarEmail;
     }
 
     public void setDaftarEmail(List<String> daftarEmail) {
         this.daftarEmail = daftarEmail;
     }
 
     public List<Phone> getDaftarTelepon() {
         return daftarTelepon;
     }
 
     public void setDaftarTelepon(List<Phone> daftarTelepon) {
         this.daftarTelepon = daftarTelepon;
     }
 
     public Role getRole() {
         return role;
     }
 
     public void setRole(Role role) {
         this.role = role;
     }
     
     
 }
