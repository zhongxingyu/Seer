 package play.modules.multijpa;
 
import com.sun.javaws.exceptions.InvalidArgumentException;
 import play.Logger;
 import play.exceptions.JPAException;
 
 import javax.persistence.*;
 
 /**
  * Datastore for single database, not thread-safe.<br />
  * Instances of this class is created for each database and each thread.
  */
 public class Datastore {
 
     private EntityManagerFactory entityManagerFactory;
 
     private EntityManager entityManager = null;
 
     /**
      * If true, the database is rolled-back after each invocation.
      */
     boolean readonly = true;
     /**
      * If true, DatastoreRegistry automatically starts a JPA transaction for each invocation.
      */
     boolean autoTxs = true;
 
     boolean transactionBegan = false;
 
     public Datastore(EntityManagerFactory entityManagerFactory) {
         this.entityManagerFactory = entityManagerFactory;
     }
 
     /**
      * Retrive the entity manager
      * @return <code>null</code> if application.conf does not contain a database configuration for <code>databaseName</code>.
      */
     public EntityManager getEntityManager() {
         Logger.debug("Datastore automatically starting a transaction.");
         // TODO when "readonly" is set in the original JPAPlugin.
         beginTransaction(false);
         return entityManager;
     }
 
     /*
      * Tell to JPA do not commit the current transaction
      */
     public void setRollbackOnly() {
         entityManager.getTransaction().setRollbackOnly();
     }
 
     /**
      * initialize the JPA context and starts a JPA transaction
      *
      * @param readonly true for a readonly transaction
      */
     public void beginTransaction(boolean readonly) {
         if (transactionBegan) {
             return;
         }
         transactionBegan = true;
         entityManager = entityManagerFactory.createEntityManager();
         entityManager.setFlushMode(FlushModeType.COMMIT);
         entityManager.setProperty("org.hibernate.readOnly", readonly);
         if (autoTxs) {
             entityManager.getTransaction().begin();
         }
         this.readonly = readonly;
     }
 
     public boolean isTransactionEnded() {
         return entityManager == null;
     }
 
     public void endTransaction(boolean rollback) {
         if (isTransactionEnded()) {
             return;
         }
         
         EntityTransaction transaction = entityManager.getTransaction();
 
         try {
             if (!autoTxs || !transaction.isActive()) {
                 return;
             }
 
             if (readonly || rollback || transaction.getRollbackOnly()) {
                 transaction.rollback();
                 return;
             }
 
             try {
                 transaction.commit();
             } catch (Throwable e) {
                 for (int i = 0; i < 10; i++) {
                     if (e instanceof PersistenceException && e.getCause() != null) {
                         e = e.getCause();
                         break;
                     }
                     e = e.getCause();
                     if (e == null) {
                         break;
                     }
                 }
                 throw new JPAException("Cannot commit", e);
             }
         } finally {
             entityManager.close();
             entityManager = null;
             transactionBegan = false;
         }
     }
 
     public void clearContext() {
         entityManager.clear();
     }
 }
