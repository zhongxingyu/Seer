 package models;
 
 import play.db.ebean.Model;
 
 import javax.persistence.*;
 import java.util.List;
 import java.util.UUID;
 
 
 /**
  *
  * @author Saad Ahmed
  */
 
 @Entity
 @Table(name="subscriptions")
 public class Subscription extends Model {
 
     @Id
     public String id;
 
     @OneToOne (cascade = CascadeType.ALL)
     public Company company;
 
     public String edition;
 
     public String status;
 
     @OneToMany (cascade = CascadeType.ALL)
     public List<SubscriptionItem> items;
 
     @OneToMany (cascade = CascadeType.ALL)
     public List<User> users;
 
 
     public Subscription(User creator, Company company, String edition) {
         this.id = UUID.randomUUID().toString();
         this.company = company;
         this.edition = edition;
         this.status = Status.FREE_TRIAL.toString().toUpperCase();
         this.users.add(creator);
     }
 
 
     public static Finder<String, Subscription> find() {
         return new Finder<String, Subscription>(String.class, Subscription.class);
     }
 
     public static String create(User creator, Company company, String edition) {
         Subscription subscription = new Subscription(creator, company, edition);
         subscription.save();
         return subscription.id;
     }
 
     public static void addItem(String id, SubscriptionItem item) {
         Subscription subscription = Subscription.find().byId(id);
 
         if (subscription != null) {
             subscription.items.add(item);
             subscription.update();
         }
     }
 
     public static void changeEdition(String id, String edition) {
         Subscription subscription = Subscription.find().ref(id);
         subscription.edition = edition;
         subscription.update();
     }
 
     public static void changeStatus(String id, String status) {
         Subscription subscription = Subscription.find().ref(id);
         subscription.status = status.toUpperCase();
         subscription.update();
     }
 
     public static void addUser(String id, User user) {
         Subscription subscription = Subscription.find().byId(id);
 
         if (subscription != null) {
             subscription.users.add(user);
             subscription.update();
         }
     }
 
     public static void removeUser(String id, User user) {
         Subscription subscription = Subscription.find().byId(id);
 
         if (subscription != null) {
             subscription.users.remove(user);
             subscription.update();
         }
     }
 
 
     public enum Status {FREE_TRIAL, ACTIVE, FREE_TRIAL_EXPIRED, SUSPENDED, CANCELLED}
 }
 
