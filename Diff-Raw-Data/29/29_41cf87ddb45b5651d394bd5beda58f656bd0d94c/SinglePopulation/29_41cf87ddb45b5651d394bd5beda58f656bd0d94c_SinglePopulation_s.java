 package models.statistics;
 
 import models.statistics.Population;
 
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
        return user.name + "(" + user.id + ")";
     }
 
     public List<User> getUsers() {
         List<User> list = new ArrayList<User>();
         list.add(user);
         return list;
     }
 
 }
