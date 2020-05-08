 package cz.muni.fi.pv243.mymaps.dto;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import javax.validation.constraints.NotNull;
 import javax.validation.constraints.Size;
 
 public class User {
 
     private Long id;
     
     private Collection<View> views;
     
     @NotNull
     @Size(min = 1)
     private String name;
     
     @NotNull
     @Size(min = 1)
     private String nick;
     
     @NotNull
     @Size(min = 1)
     private String password;
     
     private String role;
 
     public Collection<View> getViews() {
        if(views == null){
            return new ArrayList<>();
        }
         return Collections.unmodifiableCollection(views);
     }
 
     public void setViews(Collection<View> views) {
         this.views = new ArrayList<>();
         this.views.addAll(views);
     }
 
     public Long getId() {
         return id;
     }
 
     public void setId(Long id) {
         this.id = id;
     }
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public String getNick() {
         return nick;
     }
 
     public void setNick(String nick) {
         this.nick = nick;
     }
 
     public String getPassword() {
         return password;
     }
 
     public void setPassword(String password) {
         this.password = password;
     }
 
     public String getRole() {
         return role;
     }
 
     public void setRole(String role) {
         this.role = role;
     }
 
     @Override
     public boolean equals(Object o) {
         if (this == o) {
             return true;
         }
         if (o == null || getClass() != o.getClass()) {
             return false;
         }
 
         User user = (User) o;
 
         if (id != null ? !id.equals(user.id) : user.id != null) {
             return false;
         }
 
         return true;
     }
 
     @Override
     public int hashCode() {
         return id != null ? id.hashCode() : 0;
     }
 }
