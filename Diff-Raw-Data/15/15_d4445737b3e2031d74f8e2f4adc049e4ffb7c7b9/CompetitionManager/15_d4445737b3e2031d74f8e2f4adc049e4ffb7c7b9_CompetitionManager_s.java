 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package Domaene;
 
 import Persistence.PersistenceManager;
 import Persistence.Competition;
 import java.util.Date;
 import java.util.List;
 /**
  *
  * @author rafa
  */
 public class CompetitionManager {
 
     public Competition getCompetitinById(int id) {  
         PersistenceManager persistenceManager = new PersistenceManager();
         return (Persistence.Competition) persistenceManager.getObjectById(Persistence.Competition.class, id);
     }   
 
     public List<Object> getCompetitionsByName(String name) {
         String hqlQuerie = "FROM Competition cp WHERE (cp.name LIKE '"+name+"')";
         PersistenceManager persistenceManager = new PersistenceManager();
         return persistenceManager.getObjectsByHQLQuery(hqlQuerie);
     }
 
     public List<Object> getCompetitionByDepartmentId(int departmentId) {
        String hqlQuerie = "FROM Competition cp WHERE (cp.department_id = '3')";
         PersistenceManager persistenceManager = new PersistenceManager();
         return persistenceManager.getObjectsByHQLQuery(hqlQuerie);
     }
 
     public Competition getCompetitionByDate(Date date) {
         throw new UnsupportedOperationException("Not yet implemented");
     }
 
     public List<Object> getAllCompetitionsBetweenPeriod(Date startdate, Date enddate) {
         throw new UnsupportedOperationException("Not yet implemented");
     }
 
     public List<Object> getAllCompetitionsByDate(Date startdate) {
         throw new UnsupportedOperationException("Not yet implemented");
     }
 
     public List<Object> getAllCompetitions() {
         throw new UnsupportedOperationException("Not yet implemented");
     }
     
 }
