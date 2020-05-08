 package fr.cg95.cvq.dao.jpa;
 
 import javax.persistence.EntityManager;
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.EntityTransaction;
 import javax.persistence.PersistenceException;
 import javax.persistence.PersistenceUnitUtil;
 import javax.persistence.RollbackException;
 
 import org.apache.log4j.Logger;
 
 public class JpaUtil {
 
     private static Logger logger = Logger.getLogger(JpaUtil.class);
 
     private static final ThreadLocal<EntityManagerFactory> threadEntityManagerFactory = new InheritableThreadLocal<EntityManagerFactory>();
 
     private static final ThreadLocal<EntityManager> threadEntityManager = new ThreadLocal<EntityManager>();
 
     private static final ThreadLocal<EntityTransaction> threadEntityTransaction = new ThreadLocal<EntityTransaction>();
 
     public static void setEntityManagerFactory(EntityManagerFactory entityManagerFactory) {
         threadEntityManagerFactory.set(entityManagerFactory);
         threadEntityManager.set(entityManagerFactory.createEntityManager());
     }
 
     public static EntityManager getEntityManager() {
         EntityManager entityManager = threadEntityManager.get();
         if (entityManager == null) {
             entityManager = threadEntityManagerFactory.get().createEntityManager();
             threadEntityManager.set(entityManager);
             logger.debug("create new EntityManager : " + threadEntityManager.get());
         }
         return entityManager;
     }
 
     public static void beginTransaction() {
         EntityTransaction tx = threadEntityTransaction.get();
         if (tx == null) {
             // TODO why dont I make this on getEntityManager instead?
 //            getEntityManager().setFlushMode(FlushModeType.COMMIT);
             tx = getEntityManager().getTransaction();
             logger.debug("Creation of entityTransaction : " + tx);
             try {
                 tx.begin();
             } catch (IllegalStateException ise) {
                 // assert : active is not null => the transaction already begin
                 // => this case should never been throw..
                 logger.error("Amazing exception..");
                 // TODO: handle exception
             }
             threadEntityTransaction.set(tx);
         } else {
             logger.warn("Cannot begin an already opened transaction");
             // TODO
         }
     }
 
     public static void commitTransaction() {
         EntityTransaction tx = threadEntityTransaction.get();
         if (tx != null && tx.isActive()) {
             try {
                 tx.commit();
                 threadEntityTransaction.set(null);
             } catch (final RollbackException rbe) {
                 rollbackTransaction();
             } catch (IllegalStateException ise) {
                 logger.warn("Sorry can't stop non started transaction");
                 // TODO ? dont log and throw ?
             } finally {
 //                close();  // FIXME I think we should not close the entitymanager
             }
         } else {
             logger.warn("No transaction to commit");
             // TODO what ?
         }
     }
 
     /**
      * Rollback a transaction
      * 
      * @throws IllegalStateException
      *             - if isActive() is false
      * @throws PersistenceException
      *             - if an unexpected error condition is encountered
      */
     public static void rollbackTransaction() {
         EntityTransaction entityTransaction = threadEntityTransaction.get();
         if (entityTransaction != null) {
             entityTransaction.rollback();
             threadEntityTransaction.remove();
         }
     }
 
     /**
      * close the entityManager
      * 
      * @throws IllegalStateException
      */
     public static void close() {
         EntityManager entityManager = threadEntityManager.get();
         if (entityManager != null && entityManager.isOpen()) {
             threadEntityManager.get().close();
             threadEntityManager.set(null);
         }
 
         EntityTransaction tx = threadEntityTransaction.get();
         if (tx != null && tx.isActive()) {
             rollbackTransaction();
             throw new RuntimeException("EntityManager closed before transition commited");
         }
     }
 
     public static PersistenceUnitUtil getPersistenceUnitUtil(){
         // if threadEntityManagerFactory dont have an emf
         EntityManagerFactory emf = threadEntityManagerFactory.get();
         if (emf != null){
             return threadEntityManagerFactory.get().getPersistenceUnitUtil();
         }else{
             throw new RuntimeException("you can't use getPersistenceUnitUtil() when no EntityManagerFactory is set");
         }
     }
 
 }
