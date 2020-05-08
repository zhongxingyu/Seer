 package models.statistics;
 
import java.util.List;

 import models.statistics.Population;
import models.dbentities.ClassGroup;
import models.user.User;
 
 /**
  * A Population of one class.
  * @author Felix Van der Jeugt
  */
 public class ClassPopulation implements Population {
 
     private ClassGroup classGroup;
 
     public ClassPopulation() {}
 
     public ClassPopulation(ClassGroup classGroup) {
         this.classGroup = classGroup;
     }
 
     public String describe() {
        return classGroup.name + " of the " + classGroup.schoolid;
     }
 
     public List<User> getUsers() {
         // TODO #72
         return null;
     }
 
 }
