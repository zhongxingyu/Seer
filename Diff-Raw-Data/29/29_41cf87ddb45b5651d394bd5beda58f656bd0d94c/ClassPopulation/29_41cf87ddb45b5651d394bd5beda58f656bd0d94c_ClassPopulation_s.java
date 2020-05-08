 package models.statistics;
 
 import models.statistics.Population;
 
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
        // TODO How is a class described? (Issue #72)
        return "";
     }
 
     public List<User> getUsers() {
         // TODO #72
         return null;
     }
 
 }
