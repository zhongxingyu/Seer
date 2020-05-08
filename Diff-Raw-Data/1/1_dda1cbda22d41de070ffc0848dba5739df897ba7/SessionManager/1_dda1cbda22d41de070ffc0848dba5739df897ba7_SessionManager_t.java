 package com.vdweem.jplanningpoker.session;
 
 import java.util.Collection;
 import java.util.LinkedHashMap;
 import java.util.Map;
 
 import org.apache.struts2.ServletActionContext;
 
 import com.vdweem.jplanningpoker.actions.AdminAction;
 import com.vdweem.jplanningpoker.actions.ChangenameAction;
 
 /**
  * com.vdweem.jplanningpoker.session.SessionManager
  *
  * Keeps track of all active users.
  * @author       Niels
  */
 public class SessionManager {
     public static Map<String, Session> sessions = new LinkedHashMap<String, Session>();
     private static ThreadLocal<Session> currentSession = new ThreadLocal<Session>();
 
     /**
      * Is called for each request by the SessionInterceptor.
      * Marks the session of the current request as active and cleans all inactive sessions.
      */
     public static void init() {
         // Sychronized because cleanInactives uses a foreach loop on sessions. Disallow concurrent modifications.
         synchronized(sessions) {
             cleanInactives();
 
             // The admin user can't poker, its session is therefore not useful.
             if (AdminAction.isAdmin()) return;
 
             // Create a new session if there isn't a session yet, otherwise put the existing session in the currentSession.
             String id = ServletActionContext.getRequest().getSession().getId();
             if (sessions.get(id) == null) {
                 sessions.put(id, new Session());
                 ChangenameAction.changeNameFromCookie();
             }
             currentSession.set(sessions.get(id));
         }
     }
 
     /**
      * Gives a list of all active sessions.
      * @return
      */
     public static Collection<Session> getSessions() {
         return sessions.values();
     }
 
     /**
      * Removes all sessions which are currently marked inactive.
      */
     private static void cleanInactives() {
         synchronized (sessions) {
             for (String id : sessions.keySet())
                 if (!sessions.get(id).isActive())
                     sessions.remove(id);
         }
     }
 
     /**
      * Returns the session of the current user.
      * @return
      */
     public static Session getSession() {
         init();
        if (currentSession.get() == null) return new Session();
         return currentSession.get().touch();
     }
 
 }
