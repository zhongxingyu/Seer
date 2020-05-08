 package gov.usgs.cida.coastalhazards.jpa;
 
 import gov.usgs.cida.coastalhazards.model.Activity;
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 
 /**
  *
  * @author Jordan Walker <jiwalker@usgs.gov>
  */
 public class ActivityManager {
 
     @PersistenceContext
     private EntityManager em;
             
     public ActivityManager() {
         em = JPAHelper.getEntityManagerFactory().createEntityManager();
     }
 
     public String hit(Activity activity) {
         String result = "{\"success\": false}";
         try {
             em.getTransaction().begin();
             em.persist(activity);
             em.getTransaction().commit();
             result = "{\"success\": true}";
         } catch (Exception ex) {
             em.getTransaction().rollback();
         }
         return result;
     }
 }
