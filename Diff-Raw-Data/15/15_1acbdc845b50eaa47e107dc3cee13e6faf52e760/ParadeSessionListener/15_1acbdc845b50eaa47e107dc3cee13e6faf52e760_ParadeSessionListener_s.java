 package org.makumba.parade.listeners;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.servlet.http.HttpSession;
 import javax.servlet.http.HttpSessionEvent;
 import javax.servlet.http.HttpSessionListener;
 
 /**
  * This listener triggers an event each time a HTTP session is created or destroyed
  * 
  * TODO actually implement it
  * 
  * @author Manuel Gay
  * 
  */
 public class ParadeSessionListener implements HttpSessionListener {
 
     private static Map<String, HttpSession> activeSessions = new HashMap<String, HttpSession>();
 
     public void sessionCreated(HttpSessionEvent e) {
         activeSessions.put(e.getSession().getId(), e.getSession());
 
         // System.out.println("session created");
         // System.out.println("id: "+e.getSession().getId());
         // System.out.println("user attribute: "+e.getSession().getAttribute("org.makumba.parade.user"));
 
         // TODO create a custom event, send it to dbservlet
 
     }
 
     public void sessionDestroyed(HttpSessionEvent e) {
         activeSessions.remove(e.getSession().getId());
 
         // System.out.println("session destroyed");
         // System.out.println("id: "+e.getSession().getId());
         // System.out.println("user attribute: "+e.getSession().getAttribute("org.makumba.parade.user"));
 
         // TODO create a custom event, send it to dbservlet
 
     }
 
     public static List<HttpSession> getActiveSessions() {
         List<HttpSession> sessions = new LinkedList<HttpSession>();
         Iterator<String> it = activeSessions.keySet().iterator();
         while (it.hasNext()) {
             HttpSession s = activeSessions.get(it.next());
             try {
                 s.getAttribute("org.makumba.parade.user"); // check if the session is still valid
                 sessions.add(s);
             } catch (java.lang.IllegalStateException e) {
                 // this session is invalidated, we need to remove it
                 activeSessions.remove(s.getId());
             }
 
         }
         return sessions;
     }
 
     public static List<String[]> getActiveSessionUsers() {
         List<String[]> onlineUsers = new LinkedList<String[]>();
 
        // hashset for filtering expired sessions
        Set<String[]> online = new HashSet<String[]>();
 
         Iterator<String> it = activeSessions.keySet().iterator();
         while (it.hasNext()) {
             HttpSession s = activeSessions.get(it.next());
             try {
                 String login = (String) s.getAttribute("user_login");
                 String nickName = (String) s.getAttribute("user_nickname");
                 if (login != null && login.length() > 0) {
                    online.add(new String[] { login, nickName });
                 }
             } catch (java.lang.IllegalStateException e) {
                 // this session is invalidated, we need to remove it
                 activeSessions.remove(s.getId());
             }
         }
 
        Iterator<String[]> it2 = online.iterator();
        while (it2.hasNext()) {
            onlineUsers.add(it2.next());
         }
 
         return onlineUsers;
     }
 }
