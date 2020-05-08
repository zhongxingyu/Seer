 package models.statistics;
 
 import java.util.List;
 
 /**
  * Represents a group of Users, joined together to be summarized as a whole.
  * @author Felix Van der Jeugt
  */
 public interface Population {
 
     /**
      * Describes this population as a user readabl string. For instance: "Class
      * A of the Royal Institution".
      */
     public String describe();
 
     /**
      * Returns the list of users in this population.
      */
     public List<User> getUsers();
 
 }
