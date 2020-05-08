 /**
  * Copyright (C) 2003 FEIDE
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
  */
 
 package no.feide.moria;
 
 import java.security.Principal;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Collections;
 import java.util.Map;
 import java.util.Vector;
 import java.util.Enumeration;
 import java.util.logging.Logger;
 import org.doomdark.uuid.UUIDGenerator;
 import no.feide.moria.authorization.WebService;
 import no.feide.moria.stats.StatsStore;
 
 public class SessionStore {
     
     /** Used for logging. */
     private static Logger log = Logger.getLogger(SessionStore.class.toString());
 
     /** Statistics */
     private StatsStore stats = StatsStore.getInstance();
 
     /** Static pointer to singleton object. */
     static private SessionStore me;
     
     /** Contains all active session objects. Key is current session ID. */
     Map sessions = null;
     
     
     
     /**
      * Constructor. Kicks off the session store maintenance thread, to handle
      * session timeouts. Will read the Moria property file named in the
      * system property <code>no.feide.moria.config.file</code>, or
      * <code>/moria.properties</code> if the property is not set.
      * @throws SessionException If the properties file doesn't include the
      *                          properties
      *                          <code>no.feide.moria.SessionStoreInitMapSize</code>
      *                          or
      *                          code>no.feide.moria.SessionStoreMapLoadFactor</code>.
      *                          Also thrown if a
      *                          <code>FileNotFoundException</code> or
      *                          <code>IOException</code> is caught while
      *                          reading the property file.
      */
     private SessionStore()
     throws SessionException {
         log.finer("SessionStore()");
 
         try {
             int initialSize = new Integer(Configuration.getProperty("no.feide.moria.SessionStoreInitMapSize")).intValue();
             float loadFactor = new Float(Configuration.getProperty("no.feide.moria.SessionStoreMapLoadFactor")).floatValue();
             sessions = Collections.synchronizedMap(new HashMap(initialSize, loadFactor));
             log.config("Session register initialized. Initial size="+initialSize+" loadFactor="+loadFactor);
         } catch (ConfigurationException e) {
             log.severe("ConfigurationException caught and re-thrown as SessionException");
             throw new SessionException("ConfigurationException caught and re-thrown as SessionException");
         }
     }
     
     
     /** 
      * Returns a pointer to the SessionStore singleton object.
      * @return SessionStore
      * @throws SessionException If an error occurs creating the singleton
      *                          instance.
      */
     public static SessionStore getInstance() 
     throws SessionException {
         log.finer("getInstance()");
         
         if (me == null) 
             me = new SessionStore();
         return me;
     }
     
     
     /**
      * Generates a unique session ID.
      * @param sessions The list of sessions.
      * @return A new session ID.
      * @throws SessionException If an unique (that is, non-existing) ID
      *                          cannot be generated in 20 tries.
      */
     protected String generateSessionID()
     throws SessionException {
         log.finer("generateSessionID()");
 
         // Try 20 times to generate a unique session ID, then give up.
         String generated = null;
         int count = 0;
         do {
             if (count++ == 20) {
                 log.severe("Unable to create unique session ID");
                 throw new SessionException("Unable to create unique session ID");
             }          
             generated = UUIDGenerator.getInstance().generateRandomBasedUUID().toString();
         } while (sessions.containsKey(generated));
 
         return generated;
     }    
 
 
     /**
      * Creates a new session, with an attribute request.
      * @param attributes The list of requested attribute names, possibly
      *                   <code>null</code>.
      * @param prefix The prefix, used to build the <code>verifySession</code>
      *               return value. May be <code>null</code>.
      * @param postfix The postfix, used to build the
      *                <code>verifySession</code> return value.
      *                May be <code>null</code>.
      * @param client Identifies the client service initiating the session.
      *               May be <code>null</code>.
      * @return A new session.
      */
     public Session createSession(String[] attributes, String prefix, String postfix, Principal client, WebService ws)
     throws SessionException {
         log.finer("createSession(String[], String, String, Principal)");
 
         String sessionID = generateSessionID();
         Session session = new Session(sessionID, attributes, prefix, postfix, client, ws);
         synchronized (sessions) {
             sessions.put(sessionID, session);
         }
 
         return session;
     }
 
 
     /**
      * Returns the session for a given session ID.
      * @param sessionID Current ID for the Moria session.
      * @throws SessionException If the session wasn't found.
      */
     public Session getSession(String sessionID) 
     throws SessionException {
         log.finer("getSession(String)");
         if (sessionID == null || !sessions.containsKey(sessionID)) {
             log.fine("No such session: "+sessionID);
             throw new NoSuchSessionException("No such session: "+sessionID);
         } else
             return (Session)sessions.get(sessionID);
     }
 
   
     /**
      * Removes a Moria session.
      * @param session The session.
      */     
     public synchronized void deleteSession(Session session) {
         log.finer("deleteSession(Session)");
         
         synchronized (sessions) {
             sessions.remove(session.getID());
         }
     } 
     
     
     /**
      * Confirms a Moria session, that is, gives it a new second-level session
      * ID after a successful authentication.
      * @param sessionID The old session ID.
      * @throws SessionExcepion If the old session ID cannot be found.
      */
     public void renameSession(Session session)
     throws SessionException {
         log.finer("renameSession(String)");
         
         // Generate a new session ID.
         String oldID = session.getID();
 
         synchronized (sessions) {
             session.setID(generateSessionID());
             
             // Remove old session.
             sessions.remove(oldID);
             sessions.put(session.getID(), session);
         }
 
         log.fine("Session renamed to "+session.getID());
     }
 
     
     protected void checkTimeout(int timeout, int authTimeoutSec, int timeoutSso) {
 
         Vector invalidatedSessions = new Vector();
         Date start = new Date();
 
         log.fine("Number of sessions: "+sessions.size());
 
         // Find all timedout sessions.
         synchronized (sessions) {
             for (Iterator iterator = sessions.keySet().iterator(); iterator.hasNext();) {
                 String key = (String) iterator.next();
                 Session session = (Session) sessions.get(key);
                 double now = new Date().getTime();
                 String wsName = session.getWebService().getId();
 
                 if (session.isAuthenticated()) {
                    
                     /* Look for timed out SSO sessions. */
                     if (session.isLocked() && 
                         !session.isValid(now-timeoutSso)) {
                             log.info("Invalidating SSO session (timeout): "+session.getID());
                             stats.sessionTimeout(wsName, "SSO");
                             invalidatedSessions.add(session);
                     }
 
                     /* Web service to slow to fetch user attributes */
                    else if (!session.isLocked() && !session.isValid(now-authTimeoutSec)) {
                            log.info("Invalidating authenticated session (Mellon timeout): "+session.getID());
                             stats.sessionTimeout(wsName, "AUTH");
                             invalidatedSessions.add(session);
                     }
                 }
 
                 /* Time out due to missing login info from user */
                 else {
                     if (!session.isValid(now-timeout)) {
                        log.info("Invalidating session (user timeout): "+session.getID());
                         stats.sessionTimeout(wsName, "USER");
                         invalidatedSessions.add(session);
                     }
                 }
             }
 
             // Invalidate sessions
             for (Enumeration enum = invalidatedSessions.elements(); enum.hasMoreElements(); ) {
                 deleteSession((Session)enum.nextElement());
             }
         }
         if (invalidatedSessions.size() > 0) 
             log.info(invalidatedSessions.size()+" of "+(invalidatedSessions.size()+sessions.size())+" sessions invalidated in "+(new Date().getTime()-start.getTime())+ " ms.");
 
     }
 
 }
