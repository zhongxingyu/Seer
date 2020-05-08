 package models.statistics;
 
import java.util.List;
import java.util.ArrayList;

 import models.statistics.Population;
import models.user.User;
 
 /**
  * Quite a stupid Population, actually. Represents a single user.
  * @author Felix Van der Jeugt
  */
 public class SinglePopulation implements Population {
 
     private User user;
 
     public SinglePopulation() {}
 
     public SinglePopulation(User user) {
         this.user = user;
     }
 
     public String describe() {
        return user.getData().name + "(" + user.getID() + ")";
     }
 
     public List<User> getUsers() {
         List<User> list = new ArrayList<User>();
         list.add(user);
         return list;
     }
 
 }
