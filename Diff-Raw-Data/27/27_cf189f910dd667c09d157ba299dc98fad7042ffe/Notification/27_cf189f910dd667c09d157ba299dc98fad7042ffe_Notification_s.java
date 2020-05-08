 package fr.univnantes.atal.web.trashnao.model;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.List;
 import javax.jdo.annotations.IdGeneratorStrategy;
 import javax.jdo.annotations.PersistenceCapable;
 import javax.jdo.annotations.Persistent;
 import javax.jdo.annotations.PrimaryKey;
 
 @PersistenceCapable
 public class Notification {
 
     @PrimaryKey
     @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
     private Address address;
     @Persistent
     private List<NotificationTransport> onYellowDay;
     @Persistent
     private List<NotificationTransport> onBlueDay;
 
     public Notification() {
         onBlueDay = new ArrayList<>();
         onYellowDay = new ArrayList<>();
     }
 
     public Address getAddress() {
         return address;
     }
 
     public void setAddress(Address address) {
         this.address = address;
     }
 
     public void addNotificationOnBlueDay(NotificationTransport transport) {
         if (!onBlueDay.contains(transport)) {
             onBlueDay.add(transport);
         }
     }
 
     public void addNotificationOnYellowDay(NotificationTransport transport) {
         if (!onYellowDay.contains(transport)) {
             onYellowDay.add(transport);
         }
     }
     
     public void removeNotificationOnBlueDay(NotificationTransport transport) {
         onBlueDay.remove(transport);
     }
 
     public void removeNotificationOnYellowDay(NotificationTransport transport) {
         onYellowDay.remove(transport);
     }
     
     public Collection<NotificationTransport> getNotificationsOnBlueDay() {
         return Collections.unmodifiableCollection(onBlueDay);
     }
     
     public Collection<NotificationTransport> getNotificationsOnYellowDay() {
         return Collections.unmodifiableCollection(onYellowDay);
     }
 }
