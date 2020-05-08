 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package census.business;
 
 import census.business.api.BusinessException;
 import census.business.api.SessionListener;
 import census.business.api.ValidationException;
 import census.persistence.Administrator;
 import census.persistence.Session;
 import java.io.UnsupportedEncodingException;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.text.MessageFormat;
 import java.util.*;
 import javax.persistence.EntityManager;
 import javax.persistence.NoResultException;
 import org.apache.log4j.Logger;
 import org.joda.time.DateMidnight;
 import org.joda.time.DateTime;
 
 /**
  *
  * @author Danylo Vashchilenko
  */
 public class SessionsService extends Observable {
 
     protected SessionsService() {
         entityManager = StorageService.getInstance().getEntityManager();
         attemptsLeft = 5;
         bundle = ResourceBundle.getBundle("census.business.resources.Strings");
         listeners = new HashSet<>();
     }
 
     /**
      * Opens a session for an administrator by its username and password. This
      * method will try to find and join an already existing session open today
      * by this administrator. The password should be passed here from the user
      * without any changes.
      *
      * @param username the administrator's username
      * @param password the administrator's password
      * @throws NullPointerException if any of the arguments is null
      * @throws ValidationException if the username and the password combination
      * is invalid
      * @throws IllegalStateException if the transaction is not active
      */
     public void openSession(String username, char[] password) throws ValidationException, BusinessException {
 
         Administrator administrator = authenticateAdministrator(username, password);
 
         /*
          * If the same session already exists, just joins it,
          */
         List<Session> sessions = entityManager.createNamedQuery("Session.findByAdministratorAndDateTimeBeginRange") //NOI18N
                 .setParameter("rangeBegin", new DateMidnight().toDate()) //NOI18N
                 .setParameter("rangeEnd", new DateMidnight().plusDays(1).toDate()) //NOI18N
                 .setParameter("administrator", administrator) //NOI18N
                 .getResultList();
 
         if (sessions.isEmpty()) {
            session = new Session();
             session.setAdministrator(administrator);
             session.setDatetimeBegin(new Date());
             session.setDatetimeEnd(null);
 
             entityManager.persist(session);
         } else {
             session = sessions.get(0);
             session.setDatetimeEnd(null);
         }
 
         entityManager.flush();
 
         setChanged();
         notifyObservers();
 
         for (SessionListener listener : listeners) {
             listener.sessionOpened();
         }
 
         logger.info("Current session is now associated with "
                 + administrator.getFullName() + " (" + administrator.getId() + ")");
     }
 
     /**
      * Gets whether there is an open session.
      *
      * @return true, if there is an open session
      */
     public Boolean hasOpenSession() {
         return session != null;
     }
 
     public Short getTopmostAdministratorId() {
         if (raisedAdministrator == null) {
             return session.getAdministrator().getId();
         } else {
             return raisedAdministrator.getId();
         }
     }
 
     public void raiseAdministrator(String username, char[] password) throws ValidationException, BusinessException {
 
         Administrator administrator = authenticateAdministrator(username, password);
 
         if (session.getAdministrator().equals(administrator)) {
             throw new BusinessException(bundle.getString("CurrentSessionWasOpennedBySameAdministrator"));
         }
 
         raisedAdministrator = administrator;
 
         setChanged();
         notifyObservers();
 
         for (SessionListener listener : listeners) {
             listener.sessionUpdated();
         }
 
         logger.info("Current session is now associated with "
                 + administrator.getFullName() + " (" + administrator.getId() + ")");
     }
 
     public Boolean hasRaisedAdministrator() {
         return raisedAdministrator != null;
     }
 
     public void dropRaisedAdministrator() {
         if (raisedAdministrator == null) {
             throw new IllegalStateException(bundle.getString("NoRasiedPermissionsLevel"));
         }
 
         raisedAdministrator = null;
 
         setChanged();
         notifyObservers();
 
         for (SessionListener listener : listeners) {
             listener.sessionUpdated();
         }
 
         logger.info("Current session is now associated with "
                 + session.getAdministrator().getFullName() + " (" + session.getAdministrator().getId() + ")");
     }
 
     /**
      * Closes the current session.
      *
      * @throws IllegalStateException if the transaction is not active or there
      * isn't any open sessions
      */
     public void closeSession() {
         if (!StorageService.getInstance().isTransactionActive()) {
             throw new IllegalStateException(bundle.getString("TransactionNotActive"));
         }
 
         if (session == null) {
             throw new IllegalStateException(bundle.getString("NoOpenSessions"));
         }
 
         raisedAdministrator = null;
 
         session.setDatetimeEnd(new Date());
         session = null;
 
         StorageService.getInstance().getEntityManager().flush();
 
         setChanged();
         notifyObservers();
 
         for (SessionListener listener : listeners) {
             listener.sessionClosed();
         }
 
         logger.info("Current session is now not associtead with any administrator.");
     }
 
     /**
      * Gets the current administrator's permissions level. If the permissions
      * level was raised, the raised permissions level will be returned.
      *
      * @throws IllegalStateException if there isn't any open sessions
      * @return the number representing the administrator's permissions level.
      */
     public Short getPermissionsLevel() {
         if (session == null) {
             throw new IllegalStateException("There isn't any open sessions.");
         }
         if (raisedAdministrator != null) {
             return raisedAdministrator.getPermissionsLevel();
         }
         return session.getAdministrator().getPermissionsLevel();
     }
 
     /**
      * Tries to authenticate the administrator. This method is to be used by
      * publicly available methods of this class.
      *
      * @param username the username
      * @param password the password
      * @return the administrator
      * @throws ValidationException if the combination is invalid
      * @throws BusinessException if can not log in now
      */
     private Administrator authenticateAdministrator(String username, char[] password) throws ValidationException, BusinessException {
         if (username == null) {
             throw new NullPointerException("The username is null."); //NOI18N
         }
 
         if (password == null) {
             throw new NullPointerException("The password is null."); //NOI18N
         }
 
         if (attemptsLeft != ATTEMPTS_LIMIT) {
             if (recoverAttemptsTime.isBeforeNow()) {
                 attemptsLeft = ATTEMPTS_LIMIT;
                 recoverAttemptsTime = null;
             }
 
             if (attemptsLeft == 0) {
                 throw new BusinessException(bundle.getString("LimitOfFailedAttemtsReached"));
             }
         }
 
         if (!StorageService.getInstance().isTransactionActive()) {
             throw new IllegalStateException(bundle.getString("TransactionNotActive"));
         }
 
         /*
          * Hashes the password with SHA-256
          */
         MessageDigest md;
         try {
             md = MessageDigest.getInstance("SHA-256"); //NOI18N
         } catch (NoSuchAlgorithmException ex) {
             throw new RuntimeException(ex);
         }
         try {
             md.update(String.valueOf(password).getBytes("UTF-8")); //NOI18N
         } catch (UnsupportedEncodingException ex) {
             throw new RuntimeException(ex);
         }
 
         byte byteData[] = md.digest();
 
         /*
          * Converts the bytes to a hex string
          */
         StringBuilder sb = new StringBuilder();
         for (int i = 0; i < byteData.length; i++) {
             sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
         }
         String passwordString = sb.toString();
 
         Administrator administrator;
         try {
             administrator = (Administrator) entityManager.createNamedQuery("Administrator.findByUsernameAndPassword") //NOI18N
                     .setParameter("username", username) //NOI18N
                     .setParameter("password", passwordString) //NOI18N
                     .getSingleResult();
         } catch (NoResultException ex) {
             if (attemptsLeft == ATTEMPTS_LIMIT) {
                 recoverAttemptsTime = new DateTime().plusMinutes(1);
             }
             attemptsLeft--;
             throw new ValidationException(MessageFormat.format(bundle.getString("UsernameAndPasswordCombinationInvalid"), attemptsLeft));
         }
 
         return administrator;
     }
 
     /**
      * Adds the listener. It's fine to add the same listener several times. The
      * order of listeners is not defined.
      *
      * @param sessionListener the listener to add
      */
     public void addListener(SessionListener sessionListener) {
         listeners.add(sessionListener);
     }
 
     /**
      * Removes the listener. It's fine to remove non-existing listeners.
      *
      * @param sessionListener
      */
     public void removeListener(SessionListener sessionListener) {
         listeners.remove(sessionListener);
     }
 
     /**
      * Gets an instance of this class.
      *
      * @return an instance of this class
      */
     public static SessionsService getInstance() {
         if (instance == null) {
             instance = new SessionsService();
         }
         return instance;
     }
     /*
      * Storage
      */
     private EntityManager entityManager;
     private ResourceBundle bundle;
     private DateTime recoverAttemptsTime;
     private Short attemptsLeft;
     private static final short ATTEMPTS_LIMIT = 5;
     private Session session;
     private Administrator raisedAdministrator;
     private Set<SessionListener> listeners;
     private static final Logger logger = Logger.getLogger(SessionsService.class.getName());
     /*
      * Permissions levels
      */
     public static final Short PL_ALL = 1;
     public static final Short PL_EXTENDED = 3;
     public static final Short PL_BASIC = 5;
     /*
      * Singleton instance
      */
     private static SessionsService instance;
 }
