 package svm.persistence.hibernate;
 
 import org.hibernate.HibernateException;
 import org.hibernate.Session;
 import org.hibernate.SessionFactory;
 import org.hibernate.cfg.Configuration;
 import svm.persistence.abstraction.exceptions.ExistingTransactionException;
 import svm.persistence.abstraction.exceptions.NoSessionFoundException;
 import svm.persistence.abstraction.exceptions.NoTransactionException;
 import svm.persistence.abstraction.model.IEntity;
 
 import java.util.HashMap;
 import java.util.Random;
 
 /**
  * Projectteam: Team C
  * Date: 18.10.12
  * Manage the Sessions in the System.
  */
 public class HibernateUtil {
     private static final SessionFactory ourSessionFactory;
 
     private static HashMap<Integer, Session> sessions = new HashMap<Integer, Session>();
 
     static {
         try {
             ourSessionFactory = new Configuration().configure().buildSessionFactory();
         } catch (Throwable ex) {
             throw new ExceptionInInitializerError(ex);
         }
     }
 
     /**
      * Returns Session for given SessionID
      *
      * @param sessionId Id of Hibernate Session
      * @return Session
      * @throws NoSessionFoundException No Session found for given ID
      * @throws HibernateException
      */
     public static Session getSession(Integer sessionId) throws HibernateException, NoSessionFoundException {
         if (sessions.containsKey(sessionId)) {
             return sessions.get(sessionId);
         } else {
             throw new NoSessionFoundException();
         }
     }
 
     /**
      * Closes Session for given Id
      *
      * @param sessionId Session ID
      * @throws NoSessionFoundException No Session found for this ID
      */
     public static void closeSession(Integer sessionId) throws NoSessionFoundException {
         Session s = getSession(sessionId);
         s.close();
         sessions.remove(sessionId);
     }
 
     /**
      * Generates a new Session for returned ID
      *
      * @return new Session ID
      */
     public static Integer generateSessionId() {
         Integer id = new Random().nextInt();
         if (!sessions.containsKey(id)) {
             Session s = ourSessionFactory.openSession();
             sessions.put(id, s);
             return id;
         } else {
             return generateSessionId();
         }
     }
 
     /**
      * Reattach Object to a Session
      *
      * @param sessionId SessionID
      * @param entity    Entity
      */
     public static void reattachObjectToSession(Integer sessionId, IEntity entity) throws NoSessionFoundException {
         Session s = getSession(sessionId);
         //s.merge(entity);
         // TODO Check Reattachment
         if (entity.getId() > 0) {
 
             s.update(entity);
            // TODO Eventuell wider rein aber
            // s.refresh(entity);
 
         }
     }
 
     /**
      * Start a Transaction for given Session ID
      *
      * @param sessionId Session ID
      * @throws NoSessionFoundException      No Session found for this ID
      * @throws ExistingTransactionException Existing Transaction found for this ID
      */
     public static void startTransaction(Integer sessionId) throws NoSessionFoundException, ExistingTransactionException {
         Session s = getSession(sessionId);
         if (!s.getTransaction().isActive()) {
             getSession(sessionId).beginTransaction();
         } else {
             throw new ExistingTransactionException();
         }
     }
 
     /**
      * Commit Session Transaction for given Id
      *
      * @param sessionId Session ID
      * @throws NoSessionFoundException No Session found for this Id
      * @throws NoTransactionException  No Transaction found for this Session
      */
     public static void commitTransaction(Integer sessionId) throws NoSessionFoundException, NoTransactionException {
         Session s = getSession(sessionId);
         if (s.getTransaction().isActive()) {
             getSession(sessionId).getTransaction().commit();
         } else {
             throw new NoTransactionException();
         }
     }
 
     /**
      * Abort Session Transaction for given Id
      *
      * @param sessionId Session ID
      * @throws NoSessionFoundException No Session found for this Id
      * @throws NoTransactionException  No Transaction found for this Session
      */
     public static void abortTransaction(Integer sessionId) throws NoSessionFoundException, NoTransactionException {
         Session s = getSession(sessionId);
         if (s.getTransaction().isActive()) {
             getSession(sessionId).getTransaction().rollback();
         } else {
             throw new NoTransactionException();
         }
     }
 
     /**
      * Returns true if Session exists for given Id
      *
      * @param sessionId Session Id
      * @return Session exists
      */
     public static boolean hasSession(Integer sessionId) {
         return sessions.containsKey(sessionId);
     }
 
     /**
      * Returns true if Session Transaction exists for given Id
      *
      * @param sessionId Session Id
      * @return Session exists
      * @throws NoSessionFoundException No Session found for this Id
      */
     public static boolean hasTransaction(Integer sessionId) throws NoSessionFoundException {
         return getSession(sessionId).getTransaction().isActive();
     }
 }
