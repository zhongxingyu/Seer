 package com.tapdancingmonk.payload.dao;
 
 import com.google.appengine.api.datastore.KeyFactory;
 import com.google.inject.Inject;
 import com.tapdancingmonk.payload.model.Product;

 import javax.jdo.JDOObjectNotFoundException;
 import javax.jdo.PersistenceManager;
 import javax.jdo.PersistenceManagerFactory;
 
 /**
  *
  * @author Igor Minar
  */
 public class JdoProductDao implements ProductDao {
 
     private final PersistenceManagerFactory pmf;
 
 
     @Inject
     public JdoProductDao(PersistenceManagerFactory pmf) {
         this.pmf = pmf;
     }
 
 
     public Product save(Product product) {
         PersistenceManager pm = pmf.getPersistenceManager();
         try {
             return pm.makePersistent(product);
         } finally {
             pm.close();
         }
     }
 
 
     public Product find(String id) {
         PersistenceManager pm = pmf.getPersistenceManager();
         try {
             return pm.getObjectById(Product.class, KeyFactory.stringToKey(id));
         } catch (JDOObjectNotFoundException ex) {
             throw new EntityNotFoundException(
                        String.format("entity with id %s doesn't exist", id), ex);
         } finally {
             pm.close();
         }
     }
     
 
     public void delete(Product product) {
         PersistenceManager pm = pmf.getPersistenceManager();
         try {
             pm.makePersistent(product);
             pm.deletePersistent(product);
         } finally {
             pm.close();
         }
     }
 }
