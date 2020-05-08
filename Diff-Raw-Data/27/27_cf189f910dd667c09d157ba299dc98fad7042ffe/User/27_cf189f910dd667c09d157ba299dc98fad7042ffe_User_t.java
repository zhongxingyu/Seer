 package fr.univnantes.atal.web.trashnao.model;
 
import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import javax.jdo.annotations.IdGeneratorStrategy;
 import javax.jdo.annotations.PersistenceCapable;
 import javax.jdo.annotations.Persistent;
 import javax.jdo.annotations.PrimaryKey;
 
 @PersistenceCapable
 public class User {
 
     @PrimaryKey
     @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
     private String googleId;
     @Persistent
     private String email;
     @Persistent
     private List<Notification> notifications;
 
     public User(String googleId, String email) {
         this.googleId = googleId;
         this.email = email;
        notifications = new ArrayList<>(); 
   }
 
     public String getEmail() {
         return email;
     }
 
     public String getGoogleId() {
         return googleId;
     }
     
     public void addNotification(Notification notification) {
         notifications.add(notification);
     }
     
     public void removeNotification(int index) {
         notifications.remove(index);
     }
     
     public List<Notification> getNotifications() {
         return Collections.unmodifiableList(notifications);
     }
 }
