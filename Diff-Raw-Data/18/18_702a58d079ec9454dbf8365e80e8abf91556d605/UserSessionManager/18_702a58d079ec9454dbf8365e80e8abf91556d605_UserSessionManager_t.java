 package org.gridlab.gridsphere.services.core.user;
 
 import org.gridlab.gridsphere.portlet.*;
 import org.gridlab.gridsphere.portlet.impl.SportletLog;
 import org.gridlab.gridsphere.portletcontainer.PortletSessionManager;
 
 import java.util.*;
 
 /**
  * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
  * @version $Id$
  */
 public class UserSessionManager implements PortletSessionListener {
 
     private static PortletSessionManager sessionManager = PortletSessionManager.getInstance();
     private static UserSessionManager instance  = new UserSessionManager();
     private PortletLog log = SportletLog.getInstance(UserSessionManager.class);
 
     private Map userSessions = new Hashtable();
 
     private UserSessionManager() {
     }
 
     public static UserSessionManager getInstance() {
         return instance;
     }
 
     public List getSessions(User user) {
         return (List)userSessions.get(user.getID());
     }
 
     public void addSession(User user, PortletSession session) {
         log.debug("Setting session for user " + user.getID());
         List sessions = null;
         if (userSessions.containsKey(user.getID())) {
             sessions = (List)userSessions.get(user.getID());
         } else {
             sessions = new ArrayList();
         }
         sessions.add(session);
         userSessions.put(user.getID(), sessions);
         sessionManager.addSessionListener(session.getId(), this);
     }
 
     public String getUserIdFromSession(PortletSession session) {
         Iterator it = userSessions.keySet().iterator();
         while (it.hasNext()) {
             String userId = (String)it.next();
             PortletSession s = (PortletSession)userSessions.get(userId);
             if (s.getId().equals(session.getId())) {
                 return userId;
             }
         }
         return null;
     }
 
     public List getUserIds() {
         List l = new ArrayList();
         Iterator it = userSessions.keySet().iterator();
         while (it.hasNext()) {
             l.add(it.next());
         }
         return l;
     }
 
     public List getSessions() {
         List l = new ArrayList();
         Iterator it = userSessions.values().iterator();
         while (it.hasNext()) {
             l.add(it.next());
         }
         return l;
     }
 
     public void login(PortletRequest request) {
         // so far do nothing
     }
 
     public void logout(PortletSession session) {
         log.debug("before logout");
         dumpSessions();
         if (userSessions.containsValue(session)) {
             Iterator it = userSessions.keySet().iterator();
             while (it.hasNext()) {
                 User u = (User)it.next();
                 PortletSession s = (PortletSession)userSessions.get(u.getID());
                 if (s.getId().equals(session.getId())) {
                     userSessions.remove(u.getID());
                     break;
                 }
             }
         }
         log.debug("after logout");
         dumpSessions();
     }
 
     public void removeSessions(User user) {
         log.error("Removing session for user " + user.getID());
         List userSessions = getSessions(user);
        if (userSessions != null) {
            Iterator it = userSessions.iterator();
            while (it.hasNext()) {
                PortletSession session = (PortletSession)it.next();
                if (session != null) {
                    try {
                        session.invalidate();
                    } catch (IllegalStateException e) {
                        log.debug("session " + session.getId() + " for user " + user.getID() + " has already been invalidated!");
                    }
                 }
             }
         }
         userSessions.remove(user.getID());
     }
 
     public void dumpSessions() {
         log.info("Session information:");
         log.info("# current sessions: " + userSessions.size());
         Set keySet = userSessions.keySet();
         Iterator it = keySet.iterator();
         while (it.hasNext()) {
             String u = (String)it.next();
             PortletSession s = (PortletSession)userSessions.get(u);
             log.info("User: " + u + " has session id: " + s.getId());
 
         }
     }
 }
