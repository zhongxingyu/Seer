 package fr.cg95.cvq.dao.hibernate;
 
 import org.apache.log4j.Logger;
 import org.hibernate.HibernateException;
 import org.hibernate.Session;
 import org.hibernate.SessionFactory;
 import org.hibernate.Transaction;
 
 /**
  * Basic Hibernate helper class, handles SessionFactory, Session and
  * Transaction.
  * <p>
  * Uses a static initializer for the initial SessionFactory creation and holds
  * Session and Transactions in thread local variables. All exceptions are
  * wrapped in an unchecked InfrastructureException.
  * 
  * @author christian@hibernate.org
  * @author Benoit Orihuela (bor@zenexity.fr)
  */
 public class HibernateUtil {
 
     private static Logger logger = Logger.getLogger(HibernateUtil.class);
 
     private static final ThreadLocal<SessionFactory> threadSessionFactory = new ThreadLocal<SessionFactory>();
     private static final ThreadLocal<Session> threadSession = new ThreadLocal<Session>();
     private static final ThreadLocal<Transaction> threadTransaction = new ThreadLocal<Transaction>();
 
     /**
      * Returns the SessionFactory used for this static class.
      * 
      * @return SessionFactory
      */
     public static SessionFactory getSessionFactory() {
         return threadSessionFactory.get();        
     }
 
     public static void setSessionFactory(SessionFactory sessionFactory) {
         threadSessionFactory.set(sessionFactory);
     }
 
     /**
      * Retrieves the current Session local to the thread.
      * <p/>
      * If no Session is open, opens a new Session for the running thread.
      * 
      * @return Session
      */
     public static Session getSession() {
         Session s = threadSession.get();
         if (s == null) {
             s = getSessionFactory().openSession();
             threadSession.set(s);
         }
         return s;
     }
 
     /**
      * Closes the Session local to the thread.
      */
     public static void closeSession() {
         final Session s = threadSession.get();
         threadSession.set(null);
         if (s != null && s.isOpen()) {
             s.close();
         }
     }
 
     /**
      * Start a new database transaction.
      */
     public static void beginTransaction() {
         Transaction tx = threadTransaction.get();
         if (tx == null) {
             logger.debug("Starting new database transaction in this thread.");
             tx = getSession().beginTransaction();
             threadTransaction.set(tx);
         }
     }
 
     /**
      * Commit the database transaction.
      */
     public static void commitTransaction() {
         final Transaction tx = threadTransaction.get();
         try {
             if (tx != null && !tx.wasCommitted() && !tx.wasRolledBack()) {
                 logger.debug("Committing database transaction of this thread.");
                 tx.commit();
             }
             threadTransaction.set(null);
         } catch (final HibernateException ex) {
             rollbackTransaction();
             throw ex;
         }
     }
 
     /**
      * Rollback the database transaction.
      */
     public static void rollbackTransaction() {
         final Transaction tx = threadTransaction.get();
         try {
             threadTransaction.set(null);
             if (tx != null && !tx.wasCommitted() && !tx.wasRolledBack()) {
                 logger.debug("Trying to rollback database transaction of this thread.");
                 tx.rollback();
             }
         } catch (final HibernateException ex) {
             throw ex;
         } finally {
             closeSession();
         }
     }
 }
