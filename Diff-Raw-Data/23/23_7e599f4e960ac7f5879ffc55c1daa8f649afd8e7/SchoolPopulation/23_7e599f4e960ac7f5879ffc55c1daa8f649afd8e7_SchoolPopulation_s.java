 package models.statistics.populations;
 
 import java.util.List;
 import java.util.ArrayList;
 
 import com.avaje.ebean.Ebean;
 
 import models.dbentities.UserModel;
 import models.dbentities.ClassGroup;
 import models.dbentities.SchoolModel;
 
 /**
  * Represents a groups of User from the same school.
  * @author Felix Van der Jeugt
  */
 public class SchoolPopulation extends Population {
 
     private SchoolModel school;
 
     /** Creates a new population from the given school. */
     public SchoolPopulation(SchoolModel school) {
         this.school = school;
     }
 
     @Override public PopulationType populationType() {
         return PopulationType.SCHOOL;
     }
 
     @Override public String id() {
         return "" + school.id;
     }
 
     @Override public String describe() {
         return school.name + " (" + school.address + ")";
     }
 
     @Override public List<UserModel> getUsers() {
         List<ClassGroup> classes = Ebean.find(ClassGroup.class)
             .where().eq("schoolid", school.id).findList();
        if(classes == null || classes.size() > 0) {
             return new ArrayList<UserModel>();
         }
         List<UserModel> pupils = new ArrayList<UserModel>();
         for(ClassGroup c : classes) {
             pupils.addAll((new ClassPopulation(c)).getUsers());
         }
         return pupils;
     }
 
     /** @see models.statistics.populations.Population.Factory */
     public static class Factory implements Population.Factory {
         @Override public Population create(String identifier) 
                 throws PopulationFactoryException {
             try {
                 SchoolModel sm = Ebean.find(SchoolModel.class, identifier);
                 return new SchoolPopulation(sm);
             } catch(Exception e) {
                 throw new PopulationFactoryException(e);
             }
         }
     }
 
 }
