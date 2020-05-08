 package models;
  
 import javax.persistence.*;
 import java.util.List;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 
 import play.db.jpa.*;
  
 @Entity
 public class User extends Model {
  
     @Column(unique=true)
     public String openid;
     @Column(unique=true)
     public String name;
     
     // has many Weights
     @OneToMany(mappedBy="user")
     public List<Weight> weights;
     
     // each game has players 
     @ManyToMany(mappedBy="players")
     public List<Game> games;
 
     public User(String openid, String name) {
         this.openid = openid;
         this.name = name;
         this.games = new ArrayList<Game>();
     }
     
     // first recorded entry for this user
     public Weight getFirstWeight() {
         // sort weights by date, earliest first
         Collections.sort(weights, new Comparator<Weight>() {
                 @Override
                 public int compare(final Weight object1, final Weight object2) {
                     return object1.date.compareTo(object2.date);
                 }
             });
        
        // if this user doesn't have any data, return a 0 weight    
        if (weights.size() == 0) {
            return new Weight(this, 0);
        }
         return weights.get(0);
     }
     
     // last recorded entry for this user
     public Weight getLastWeight() {
         // sort weights by date, latest first
         Collections.sort(weights, new Comparator<Weight>() {
                 @Override
                 public int compare(final Weight object1, final Weight object2) {
                     return object2.date.compareTo(object1.date);
                 }
             });
        
        // if this user doesn't have any data, return a 0 weight    
        if (weights.size() == 0) {
            return new Weight(this, 0);
        }
         return weights.get(0);
     }
     
     // is this user in a game?
     public boolean inGame() {
         if (games.size() > 0) {
             return true;
         }
         return false;
     }
     
     // check that a User exists with a particular openid
     public static boolean exists(String openid) {
         // get user by openid
         List<User> users = find("byOpenid", openid).fetch();
         if (users.size() > 0) {
             return true;
         }
 
         return false;
     }
     
     public String toString() {
         return name;
     }
     
 }
