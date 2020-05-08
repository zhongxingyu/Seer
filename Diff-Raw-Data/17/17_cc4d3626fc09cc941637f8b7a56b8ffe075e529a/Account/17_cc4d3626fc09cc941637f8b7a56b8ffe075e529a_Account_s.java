 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.team33.entities;
 
 import java.io.Serializable;
 import javax.persistence.*;
 import javax.validation.constraints.NotNull;
 import javax.validation.constraints.Size;
 import javax.xml.bind.annotation.XmlRootElement;
 
 /**
  *
 * @author LaFamiglia
  */
 @Entity
 @Table(name = "account")
 @XmlRootElement
 @NamedQueries({
     @NamedQuery(name = "Account.findAll", query = "SELECT a FROM Account a"),
     @NamedQuery(name = "Account.findById", query = "SELECT a FROM Account a WHERE a.id = :id"),
     @NamedQuery(name = "Account.findByName", query = "SELECT a FROM Account a WHERE a.name = :name"),
     @NamedQuery(name = "Account.findByPassword", query = "SELECT a FROM Account a WHERE a.password = :password"),
     @NamedQuery(name = "Account.findByActivated", query = "SELECT a FROM Account a WHERE a.activated = :activated")})
 public class Account implements Serializable {
     private static final long serialVersionUID = 1L;
     @Id
     @Basic(optional = false)
     @NotNull
     @Column(name = "id")
     private Integer id;
     @Basic(optional = false)
     @NotNull
     @Size(min = 1, max = 80)
     @Column(name = "name")
     private String name;
     @Size(max = 20)
     @Column(name = "password")
     private String password;
     @Column(name = "activated")
     private Boolean activated;
 
     public Account() {
     }
 
     public Account(Integer id) {
         this.id = id;
     }
 
     public Account(Integer id, String name) {
         this.id = id;
         this.name = name;
     }
 
     public Integer getId() {
         return id;
     }
 
     public void setId(Integer id) {
         this.id = id;
     }
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public String getPassword() {
         return password;
     }
 
     public void setPassword(String password) {
         this.password = password;
     }
 
     public Boolean getActivated() {
         return activated;
     }
 
     public void setActivated(Boolean activated) {
         this.activated = activated;
     }
 
     @Override
     public int hashCode() {
         int hash = 0;
         hash += (id != null ? id.hashCode() : 0);
         return hash;
     }
 
     @Override
     public boolean equals(Object object) {
         // TODO: Warning - this method won't work in the case the id fields are not set
         if (!(object instanceof Account)) {
             return false;
         }
         Account other = (Account) object;
         if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
             return false;
         }
         return true;
     }
 
     @Override
     public String toString() {
         return "com.team33.entities.Account[ id=" + id + " ]";
     }
     
 }
