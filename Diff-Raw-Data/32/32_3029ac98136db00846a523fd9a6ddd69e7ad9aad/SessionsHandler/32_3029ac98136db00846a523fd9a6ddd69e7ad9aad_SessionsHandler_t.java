 /*
  * Copyright (c) 2010 StockPlay development team
  * All rights reserved.
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  *
  */
 package com.kapti.backend.security;
 
 import com.kapti.data.Role;
 import com.kapti.data.User;
 import com.kapti.data.persistence.StockPlayDAOFactory;
import com.kapti.exceptions.ServiceException;
 import com.kapti.exceptions.StockPlayException;
 import java.io.IOException;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Properties;
 import java.util.Timer;
 import java.util.TimerTask;
 import org.apache.log4j.Logger;
 
 // TODO: documentatie
 /**
  *
  * @author Thijs
  */
 public class SessionsHandler {
     //
     // Member data
     //
 
     private static final Logger logger = Logger.getLogger(SessionsHandler.class);
     private final static long SESSIONS_CLEANUP_TIMEOUT = 1000 * 60 * 10;
     private Map<String, Session> sessions = new HashMap<String, Session>();
     private Map<String, SecurityRoleField> securityroles;
     private Map<User.Role, Role> roles;
     private static SessionsHandler instance = new SessionsHandler();
     private Timer timer;
 
     //
     // Construction
     //
     public static SessionsHandler getInstance() {
         return instance;
     }
 
     private static enum SecurityRoleField {
 
         NONE,
         LOGGEDIN,
         USER_REMOVE,
         SECURITY_CREATE,
         SECURITY_MODIFY,
         SECURITY_REMOVE,
         SECURITY_UPDATE,
         TRANSACTION_ADMIN,
         POINTS_ADMIN,
         BACKEND_ADMIN,
         DATABASE_ADMIN,
         SCRAPER_ADMIN
     }
 
     private SessionsHandler() {
 
         timer = new Timer();
         timer.scheduleAtFixedRate(new CleanupSessionsTask(sessions), SESSIONS_CLEANUP_TIMEOUT, SESSIONS_CLEANUP_TIMEOUT);
 
 
 
         try {
             roles = new HashMap<User.Role, Role>();
             Collection<Role> rolesFromDB = StockPlayDAOFactory.getDAO().getRolesDAO().findAll();
 
             for (Role r : rolesFromDB) {
                 roles.put(User.Role.fromId(r.getId()), r);
             }
 
 
         } catch (StockPlayException ex) {
             logger.error("Unable to retrieve roles from database", ex);
         }
 
 
         //we halen de security-informatie op (welke types gebruikers mogen wat doen?
         try {
             securityroles = new HashMap<String, SecurityRoleField>();
 
             java.util.Properties secProperties = new Properties();
             secProperties.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("security.properties"));
 
             for (Entry<Object, Object> entry : secProperties.entrySet()) {
 
                 String methodName = (String) entry.getKey();
                 SecurityRoleField field = SecurityRoleField.valueOf((String) entry.getValue());
                 securityroles.put(methodName, field);
             }
 
         } catch (IOException e) {
             logger.fatal("Unable to load resource security.properties", e);
         }
     }
 
     public void registerSession(String sessionid, User user) {
         sessions.put(sessionid, new Session(sessionid, user));
     }
 
     public User getUser(String sessionid) {
         if (sessions.containsKey(sessionid)) {
             return sessions.get(sessionid).getUser();
         } else {
             return null;
         }
     }
 
     public Role getRole(String sessionid) {
         if (sessionid != null && getUser(sessionid) != null) {
             return roles.get(getUser(sessionid).getRole());
         } else {
             return null;
         }
     }
 
     //
     // Methods
     //
     public boolean containsDefinition(String methodName) {
         return securityroles.containsKey(methodName);
     }
 
    public boolean verifyRequest(String sessionid, String methodName) throws StockPlayException {
        if (sessionid != null) {
            if (! sessions.containsKey(sessionid))
                throw new ServiceException(ServiceException.Type.SESSION_CORRUPT);
            
             Session s = sessions.get(sessionid);
             s.recordActivity();
 
             User u = s.getUser();
             Role role = roles.get(u.getRole());
 
             switch (securityroles.get(methodName)) {
                 case BACKEND_ADMIN:
                     return role.isBackendAdmin();
                 case DATABASE_ADMIN:
                     return role.isDatabaseAdmin();
                 case TRANSACTION_ADMIN:
                     return role.isTransactionAdmin();
                 case POINTS_ADMIN:
                     return role.isPointsAdmin();
                 case SCRAPER_ADMIN:
                     return role.isScraperAdmin();
                 case SECURITY_CREATE:
                     return role.isSecurityCreate();
                 case SECURITY_MODIFY:
                     return role.isSecurityModify();
                 case SECURITY_REMOVE:
                     return role.isSecurityRemove();
                 case SECURITY_UPDATE:
                     return role.isSecurityUpdate();
                 case USER_REMOVE:
                     return role.isUserRemove();
                 case NONE:
                 case LOGGEDIN:
                     return true;
                 default:
                     return false;
 
             }
         } else {
             switch (securityroles.get(methodName)) {
                 case NONE:
                     return true;
                 default:
                     return false;
             }
         }
     }
 
     private class CleanupSessionsTask extends TimerTask {
 
         Map<String, Session> sessions;
 
         public CleanupSessionsTask(Map<String, Session> sessions) {
             this.sessions = sessions;
         }
 
         @Override
         public void run() {
             Iterator<Entry<String, Session>> it = sessions.entrySet().iterator();
 
             while (it.hasNext()) {
                 Entry<String, Session> session = it.next();
 
                 if (session.getValue().sessionTimedOut()) {
                     it.remove();
                 }
             }
 
         }
     }
 }
