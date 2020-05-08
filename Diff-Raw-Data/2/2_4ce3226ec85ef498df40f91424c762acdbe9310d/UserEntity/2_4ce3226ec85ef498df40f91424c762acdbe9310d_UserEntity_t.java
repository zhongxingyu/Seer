 package cz.muni.fi.pv243.mymaps.entities;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import org.hibernate.search.annotations.Field;
 import org.hibernate.search.annotations.IndexedEmbedded;
 
 /**
  *
  * @author Jiri Holusa
  */
 public class UserEntity extends AbstractEntity implements Serializable {
     private static final long serialVersionUID = 19567189L;
     
     @Field
     private String login;  
     
     @Field
     private String passwordHash; 
     
     @Field
     private String name;    
     
     @Field
     private String role;
     
     @IndexedEmbedded
     private Collection<ViewEntity> views;
 
     public String getLogin() {
         return login;
     }
 
     public void setLogin(String login) {
         this.login = login;
     }
 
     public String getPasswordHash() {
         return passwordHash;
     }
 
     public void setPasswordHash(String passwordHash) {
         this.passwordHash = passwordHash;
     }
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public String getRole() {
         return role;
     }
 
     public void setRole(String role) {
         this.role = role;
     }
 
     public Collection<ViewEntity> getViews() {
        return views;
     }
 
     public void setViews(Collection<ViewEntity> views) {
         this.views = new ArrayList<>();
         this.views.addAll(views);        
     }
 
     @Override
     public String toString() {
         return "UserEntity{" + "login=" + login + ", passwordHash=" + passwordHash + ", name=" + name + ", role=" + role + '}';
     }
 
 }
