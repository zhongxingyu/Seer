 package ru.terra.server.security;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import ru.terra.server.config.Config;
 import ru.terra.server.constants.ConfigConstants;
 import ru.terra.server.db.entity.AbstractUser;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.UUID;
 
 /**
  * User: Vadim Korostelev
  * Date: 09.09.13
  * Time: 15:42
  */
 public class SessionsHolder {
     private static SessionsHolder instance = new SessionsHolder();
     private ArrayList<SessionInfo> sessions = new ArrayList<>();
     private Long sessionTTL = Long.parseLong(Config.getConfig().getValue(ConfigConstants.SESSION_TTL, ConfigConstants.SESSION_TTL_DEFAULT));//600 sec = 10m default ttl
     private Logger logger = LoggerFactory.getLogger(this.getClass());
 
     private SessionsHolder() {
 
     }
 
     public static SessionsHolder getInstance() {
         return instance;
     }
 
     public synchronized boolean addUser(String session, AbstractUser user) {
         sessions.add(new SessionInfo(session, user));
         logger.info("Added session " + session + " for user " + user.getName());
         return true;
     }
 
     public synchronized SessionInfo getSession(String session) {
         SessionInfo ret = null;
         for (SessionInfo si : sessions)
             if (si.getSessionid().equals(session)) {
                 si.setSessionAccessDate(new Date());
                 return si;
             }
         return ret;
     }
 
     public synchronized void removeUser(String session) {
         logger.info("Removing session " + session);
         int i = -1;
         for (int j = 0; j < sessions.size(); j++)
             if (sessions.get(j).getSessionid().equals(session)) {
                 i = j;
                 break;
             }
         if (i > -1)
             sessions.remove(i);
     }
 
     public synchronized boolean isSessionExipred(String session) {
         return new Date().getTime() - getSession(session).getSessionAccessDate().getTime() > sessionTTL;
     }
 
     public synchronized ArrayList<String> getSessions() {
         ArrayList<String> ret = new ArrayList<>();
         for (SessionInfo si : sessions)
             ret.add(si.getSessionid());
         return ret;
     }
 
     public synchronized String registerUserSession(AbstractUser u) {
         String sessionId = getSession(u.getId());
         if (sessionId == null) {
             sessionId = UUID.randomUUID().toString();
             addUser(sessionId, u);
         } else {
             logger.info("Reusing session " + sessionId + " for user " + u.getName());
         }
         return sessionId;
     }
 
     public synchronized void checkExpirations() {
         ArrayList<String> expiredSessions = new ArrayList<>();
         for (String session : getSessions()) {
             if (isSessionExipred(session))
                 expiredSessions.add(session);
         }
         for (String es : expiredSessions) {
             removeUser(es);
         }
     }
 
     public synchronized String getSession(Integer uid) {
        if (uid != null)
            for (SessionInfo si : sessions)
                if (si.getUser() != null && uid.equals(si.getUser().getId())) {
                    si.setSessionAccessDate(new Date());
                    return si.getSessionid();
                }
         return null;
     }
 }
