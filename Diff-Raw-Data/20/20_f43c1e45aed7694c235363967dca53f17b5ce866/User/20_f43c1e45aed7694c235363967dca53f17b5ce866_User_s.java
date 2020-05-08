 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package ru.terra.terramarket.db.entity;
 
 import org.codehaus.jackson.annotate.JsonIgnore;
 import ru.terra.server.db.entity.AbstractUser;
 
 import javax.persistence.*;
 import javax.xml.bind.annotation.XmlRootElement;
 import javax.xml.bind.annotation.XmlTransient;
 import java.io.Serializable;
 import java.util.List;
 
 /**
  * @author terranz
  */
 @Entity
 @Table(name = "users", catalog = "terramarket", schema = "")
 @XmlRootElement
 @NamedQueries({
         @NamedQuery(name = "User.findAll", query = "SELECT u FROM User u"),
         @NamedQuery(name = "User.findById", query = "SELECT u FROM User u WHERE u.id = :id"),
         @NamedQuery(name = "User.findByName", query = "SELECT u FROM User u WHERE u.name = :name"),
         @NamedQuery(name = "User.findByLevel", query = "SELECT u FROM User u WHERE u.level = :level"),
         @NamedQuery(name = "User.findByLogin", query = "SELECT u FROM User u WHERE u.login = :login"),
         @NamedQuery(name = "User.findByLoginAndPass", query = "SELECT u FROM User u WHERE u.login = :login AND u.pass = :pass"),
         @NamedQuery(name = "User.findByPass", query = "SELECT u FROM User u WHERE u.pass = :pass")})
 public class User extends AbstractUser implements Serializable {
     private static final long serialVersionUID = 1L;
     @Basic(optional = false)
     @Column(name = "login", nullable = false, length = 128)
     private String login;
     @Basic(optional = false)
     @Column(name = "pass", nullable = false, length = 128)
     private String pass;
     @OneToMany(mappedBy = "userId")
     private List<Photo> photoList;
     @OneToMany(cascade = CascadeType.ALL, mappedBy = "user")
     private List<Waybill> waybillList;
     @OneToMany(cascade = CascadeType.ALL, mappedBy = "user")
     private List<Sells> sellsList;
 
     public List<Sells> getSellsList() {
         return sellsList;
     }
 
     public void setSellsList(List<Sells> sellsList) {
         this.sellsList = sellsList;
     }
 
     public User(Integer id, String name, int level, String login, String pass) {
         super(id, level, name);
         this.login = login;
         this.pass = pass;
     }
 
     public User() {
         super(null, 0, "");
     }
 
     public String getLogin() {
         return login;
     }
 
     public void setLogin(String login) {
         this.login = login;
     }
 
     public String getPass() {
         return pass;
     }
 
     public void setPass(String pass) {
         this.pass = pass;
     }
 
     @XmlTransient
     @JsonIgnore
     public List<Photo> getPhotoList() {
         return photoList;
     }
 
     public void setPhotoList(List<Photo> photoList) {
         this.photoList = photoList;
     }
 
     @XmlTransient
     @JsonIgnore
     public List<Waybill> getWaybillList() {
         return waybillList;
     }
 
     public void setWaybillList(List<Waybill> waybillList) {
         this.waybillList = waybillList;
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
         if (!(object instanceof User)) {
             return false;
         }
         User other = (User) object;
         if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
             return false;
         }
         return true;
     }
 
     @Override
     public String toString() {
         return "ru.terra.terramarket.db.entity.Users[ id=" + id + " ]";
     }
 
 }
