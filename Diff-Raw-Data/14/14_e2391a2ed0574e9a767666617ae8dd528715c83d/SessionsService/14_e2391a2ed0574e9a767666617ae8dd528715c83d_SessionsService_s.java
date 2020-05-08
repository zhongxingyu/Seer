 /*
  * Copyright 2012 Danylo Vashchilenko
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 package org.key2gym.business;
 
 import org.key2gym.business.api.BusinessException;
 import org.key2gym.business.api.SessionListener;
 import org.key2gym.business.api.ValidationException;
 import org.key2gym.persistence.Administrator;
 import org.key2gym.persistence.Session;
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
 public class SessionsService {
 
     protected SessionsService() {
         entityManager = StorageService.getInstance().getEntityManager();
         attemptsLeft = 5;
         strings = ResourceBundle.getBundle("org/key2gym/business/resources/Strings");
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
 
         for (SessionListener listener : listeners) {
             listener.sessionOpened();
         }
 
         logger.info(administrator.getFullName() + " (" + administrator.getId() + ") logged in.");
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
            throw new BusinessException(strings.getString("CurrentSessionWasOpennedBySameAdministrator"));
         }
 
         raisedAdministrator = administrator;
 
         for (SessionListener listener : listeners) {
             listener.sessionChanged();
         }
 
         logger.info(administrator.getFullName() + " (" + administrator.getId() + ") swaped previous administrator.");
     }
 
     public Boolean hasRaisedAdministrator() {
         return raisedAdministrator != null;
     }
 
     public void dropRaisedAdministrator() {
         if (raisedAdministrator == null) {
            throw new IllegalStateException(strings.getString("NoRasiedPermissionsLevel"));
         }
 
         raisedAdministrator = null;
 
         for (SessionListener listener : listeners) {
             listener.sessionChanged();
         }
 
         logger.info(session.getAdministrator().getFullName() + " (" + session.getAdministrator().getId() + ") swaped back.");
     }
 
     /**
      * Closes the current session.
      *
      * @throws IllegalStateException if the transaction is not active or there
      * isn't any open sessions
      */
     public void closeSession() {
         if (!StorageService.getInstance().isTransactionActive()) {
             throw new IllegalStateException("The transcation has to be active.");
         }
 
         if (session == null) {
             throw new IllegalStateException("There isn't any open session.");
         }
 
         raisedAdministrator = null;
 
         session.setDatetimeEnd(new Date());
         session = null;
 
         StorageService.getInstance().getEntityManager().flush();
 
         for (SessionListener listener : listeners) {
             listener.sessionClosed();
         }
 
         logger.info("The administrator logged out.");
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
                throw new BusinessException(strings.getString("LimitOfFailedAttemtsReached"));
             }
         }
 
         if (!StorageService.getInstance().isTransactionActive()) {
            throw new IllegalStateException(strings.getString("TransactionNotActive"));
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
            throw new ValidationException(MessageFormat.format(strings.getString("UsernameAndPasswordCombinationInvalid"), attemptsLeft));
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
      * Gets the current session.
      * 
      * @return the session 
      */
     Session getSession() {
         return session;
     }
     
     /**
      * Sets the current session.
      * 
      * @param session the new session
      */
     void setSession(Session session) {
          this.session = session;
      }
     
     /*
      * Storage
      */
     private EntityManager entityManager;
     private ResourceBundle strings;
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
     
     /**
      * Singleton instance.
      */
     private static SessionsService instance;
     
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
 }
