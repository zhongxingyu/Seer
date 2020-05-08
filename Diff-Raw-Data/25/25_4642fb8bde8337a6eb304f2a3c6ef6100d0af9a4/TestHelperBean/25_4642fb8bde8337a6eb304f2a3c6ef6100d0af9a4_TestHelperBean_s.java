 package vio.test;
 
 import javax.ejb.Stateless;
 import javax.ejb.LocalBean;
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import javax.persistence.Query;
 
 /**
  * @author moroz
  */
 @Stateless
 @LocalBean
 public class TestHelperBean {
 
     @PersistenceContext
     EntityManager em;
     
     /**
      * This method is mainly for testing purposes,
      *   because arquillian not support PersistenceContext injection in jboss-containers
      * 
      * @param <T> expected class
      * @param query native query string
      * @return query result
      */
    public <T> T executeNativeQueryWithSingleResult(String query) {
         Query nativeQuery = em.createNativeQuery(query);
         return (T) nativeQuery.getSingleResult();
     }
     
     public int executeNativeQueryWithNoResult(String query) {
         Query nativeQuery = em.createNativeQuery(query);
         return nativeQuery.executeUpdate();
     }
     
 
 }
