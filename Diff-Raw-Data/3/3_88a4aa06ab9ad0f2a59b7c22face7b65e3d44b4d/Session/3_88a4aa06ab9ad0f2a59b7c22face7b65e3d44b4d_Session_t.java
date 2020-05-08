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
 import java.util.Vector;
 import java.util.logging.Logger;
 
 import no.feide.moria.authorization.AuthorizationData;
 import no.feide.moria.authorization.WebService;
 import no.feide.moria.stats.StatsStore;
 
 public class Session {
     
     /** Used for logging. */
     private static Logger log = Logger.getLogger(Session.class.toString());
     
     /** Holds the prefix value sent by the resource. */
     private String urlPrefix;
     
     /** Holds the postfix value sent by the resource. */
     private String urlPostfix;
        
     /** The object's current unique session ID. */
     private String sessionID;
     
     /** The attributes requested for this session. */
     private String[] request;
 
     /** The attributes requested for this session that are not allowed
      * with SSO. */
     private String[] noneSsoAttributes;
 
     /** Number of failed logins (wrong username/password). */
     private int failedLogins = 0;
 
     /** The user for this session, set after a successful authentication. */
     private User user;
 
     /** The identity of the client service requesting this session. */
     private Principal client;
 
     /** Timestamp - for invalidating session after time out. */
     private long timestamp = new Date().getTime();
     
     /** The web service that has requested this session. */
     private WebService webService = null;
 
     /** The name of the security level for the requested attributes. */
     private String attributesSecLevel;
     
     /** 
      * Is session locked? It can be unlocked by performing a
      * successful authentication. When user attributes are retrieved
      * once, the session is locked again. It is not possible to fetch
      * user attriibutes from the session when the session is
      * locked. */ 
     private boolean locked = true;
 
     /**
      * Indicates that the session is ready to accept authentication
      * attempts or not. Only after a user has requested a login page
      * we should accept authentication attempts.
      */
     private boolean authenticationInitiated = false;
 
     /**
      * True if session can be used in SSO.
      */
     private boolean allowSso = false;
 
     /**
      * Cached attributes. Only those that allow SSO.
      */ 
     private HashMap cachedAttributes;
 
 
     /**
      * Protected constructor, only to be used by
      * <code>SessionStore<code>. The session URL is set to the
      * authentication URL read from global properties
      * (<code>no.feide.moria.LoginURL</code>).
      * @param sessionID The session's ID.
      * @param attributes The attributes requested for this session.
      *                   <code>null</code> allowed.
      * @param urlPrefix The prefix, a value stored in the session and used to
      *               build the <code>SessionStore.verifySession</code> return
      *               value. May be <code>null</code>.
      * @param urlPostfix The postfix, a value stored in the session and used to
      *                build the <code>SessionStore.verifySession</code> return
      *                value. May be <code>null</code>.
      * @param client The client service identifier.
      */
      Session(String sessionID, String[] attributes, String urlPrefix, String urlPostfix, Principal client, WebService webService) {
         log.finer("Session(String, String[], String)");
         
         this.webService = webService;
         this.attributesSecLevel = webService.secLevelNameForAttributes(attributes);
         this.sessionID = sessionID;
         this.request = attributes;
         this.urlPrefix = urlPrefix;
         this.urlPostfix = urlPostfix;
         this.client = client;
         user = null;
 
         /* SSO */
         Vector ssoAttributes = AuthorizationData.getInstance().getSsoAttributes();
         Vector noneSsoAttributes = new Vector();
 
         for (int i = 0; i < attributes.length; i++) {
             if (!ssoAttributes.contains(attributes[i])) {
                 noneSsoAttributes.add(attributes[i]);
             }
         }
         
         this.noneSsoAttributes = (String[]) noneSsoAttributes.toArray(new String[noneSsoAttributes.size()]);
         this.allowSso = (webService.allowSsoForAttributes(attributes));
         log.fine("Allow SSO: "+this.allowSso+" ID: "+this.sessionID);
     }
 
 
     /**
      * Authenticates a user through the backend. The session gets a new ID
      * and the session URL is set to the session URL from the Moria
      * configuration (<code>no.feide.moria.SessionURL</code>). If the number
      * of failed logins for this session exceeds the maximum number
      * (<code>no.feide.moria.MaxFailedLogins</code>) the session is removed.
      * The caller should possibly verify the session ID after each failed
      * attempt to check if the session is still alive.<br>
      * The default number of failed logins is 3.
      * @param c User's credentials.
      * @return <code>true</code> if the user was authenticated, otherwise
      *         <code>false</code>.
      * @throws BackendException If an error occurs when accessing the backend.
      * @throws SessionException If an error occurs when accessing the
      *                          session store.
      */
     public boolean authenticateUser(Credentials c)
     throws BackendException, SessionException {
         log.finer("authenticateUser(Credentials)");
 
         timestamp = new Date().getTime();
                 
         // Authenticate user.
         user = User.getInstance();
         if (user.authenticate(c)) {
             // Update session ID and URL.
             SessionStore.getInstance().renameSession(this);
             log.fine("Session authenticated");
             locked = false;
             return true;
         }
         
         // Authentication failed. Check if the session should be invalidated.
         log.fine("Authentication failed");
         user = null;
         failedLogins++;
         try {
             Integer maxFailures = Integer.decode(Configuration.getProperty("no.feide.moria.MaxFailedLogins", "3"));
             if (failedLogins == maxFailures.intValue()) {
                 // Remove ourselves from the session store.
                 log.fine("Invalidating session: "+sessionID);
                StatsStore stats = StatsStore.getInstance();
				stats.decStatsCounter(getWebService().getId(), "activeSessions");
				stats.incStatsCounter(getWebService().getId(), "authLimitExeeded");
                 SessionStore.getInstance().deleteSession(this);
 		log.info("Max number of authN attempts ("+maxFailures+") reached");
             }
         } catch (NumberFormatException e) {
             log.severe("NumberFormatException caught and re-thrown as SessionException");
             throw new SessionException("NumberFormatException caught", e);
         } catch (ConfigurationException e) {
             log.severe("ConfigurationException caught and re-thrown as SessionException");
             throw new SessionException("ConfigurationException caught", e);
         }
         return false;
     }
     
     
     /**
      * Checks if the session has been through authentication.
      * @return <code>true</code> if successfully authenticated, otherwise
      *         <code>false</code>.
      */
     public boolean isAuthenticated() {
         log.finer("isAuthenticated()");
         
         // The user variable is only set after a successful authentication.
         return (user != null);
     }
        
        
 
     /**
      * Returns the concatenated prefix/id/postfix string.
      * @return The URL to the authentication service, if the session has yet to
      *         be authenticated. The session ID is appended to this URL as the
      *         parameter "id". If the session is authenticated, the concatenated
      *         string <code>[urlPrefix][id][urlPostfix]</code> is returned,
      *         where <code>[urlPrefix]</code> and <code>[urlPostfix]</code> are
      *         the parameter strings given to the constructor.
      * @throws ConfigurationException If unable to resolve the property
      *                                <code>no.feide.moria.LoginURL</code>.
      */
     public String getRedirectURL()
     throws ConfigurationException {
         
         String retval = "";
         if (user == null) {
             retval = Configuration.getProperty("no.feide.moria.LoginURL")+"?id="+sessionID;
         } else {
             if (urlPrefix != null)
                 retval = retval + urlPrefix;
             retval = retval + sessionID;
             if (urlPostfix != null)
                 retval = retval + urlPostfix;
         }
         return retval;
     }
 
     
     /**
      * Retrieves user attributes from the backend. Requires a previous
      * successful authentication against the backend.
      * @param attributes User element attribute names.
      * @return The requested user attributes. May be an empty set if no
      *         attributes have been requested.
      * @throws SessionException If a BackendException is caught, or if
      *                          the user has yet to be authenticated.
      */
     public HashMap getAttributes()
     throws SessionException {
         log.finer("getAttributes()");
         
         if (locked) {
             log.severe("Cannot get attributes when session is locked.");
             throw new SessionException("Session is locked.");
         }
 
         // Check for authentication.
         if (user == null) {
             log.warning("User attribute request without previous authentication");
             throw new SessionException("User attribute request without previous authentication");
         }
         
         // If no attributes have been requested, return an empty array.
         if ( (request == null) ||
              (request.length == 0) )
             return new HashMap();
         
         // Look up through backend.
         try {
             
             /* Double LDAP-lookup to simplify separation of attributes that are allowed to be cached and not. 
             	In case of bottleneck, rewrite to do one fetch.
             */
             if (cachedAttributes == null) {
                 Vector ssoAttributes = AuthorizationData.getInstance().getSsoAttributes();
                 cachedAttributes = user.lookup((String[]) ssoAttributes.toArray(new String[ssoAttributes.size()]));
             }
 
             HashMap noCachedAttributes = new HashMap();
             if (noneSsoAttributes.length > 0) {
                 noCachedAttributes = user.lookup(noneSsoAttributes);
             }
 
             HashMap result = genAttrResult(cachedAttributes, noCachedAttributes, request);
             prepareForSSO();
             return result;
 
         } catch (BackendException e) {
             log.severe("BackendException caught and re-thrown as SessionException");
             throw new SessionException(e);
         }
     }
     
 
     /**
      * Merge cached and nocached attributes together to one result HashMap.
      */
     private HashMap genAttrResult(HashMap cache, HashMap noCache, String[] request) {
         HashMap result = new HashMap();
 
         for (int i = 0; i < request.length; i++) {
 
             if (cache.containsKey(request[i])) 
                 result.put(request[i], cache.get(request[i]));
             
             
             else if (noCache.containsKey(request[i])) 
                 result.put(request[i], noCache.get(request[i]));
             
 
             else
                 log.warning("Failed to fetch attribute "+request[i]+".");
         }
 
         return result;
     }
 
 
     /**
      * Reset all variables/pointers that are not used with SSO.
      */ 
     private void prepareForSSO() throws BackendException {
         StatsStore stats = StatsStore.getInstance();
 
         stats.decStatsCounter(webService.getId(), "activeSessions");
         stats.increaseCounter("sessionsSSOActive");
 
         locked = true;
         user.close();
         webService = null;
         urlPrefix = null;
         urlPostfix = null;
         request = null;
         noneSsoAttributes = null;
         failedLogins = 0;
         client = null;
         attributesSecLevel = null;
         authenticationInitiated = false;
         allowSso = false;
     }
 
     
     /**
      * Updates the session's current ID. Note that this does not update the
      * <code>SessionStore</code> internal name for this session, which may
      * cause inconsistency if used incorrectly.
      * @param sessionID The new session ID.
      */
      void setID(String sessionID) {
         log.finer("setID(String)");
             
         this.sessionID = sessionID;
     }
     
     
     /**
      * Returns the session's current ID.
      * @return Current session ID.
      */
     public String getID() {
         log.finer("getID()");
         
         return sessionID;
     }
 
     
     /**
      * Returns the identity of the client requesting this session.
      * @return The identity of the client service.
      */
     public Principal getClientPrincipal() {
 	log.finer("getClientPrincipal()");
 
 	return client;
     }
 
 
     /**
      * Returns true if sessions has not timed out. 
      * @param validUntil Milliseconds since epoc
      */
      boolean isValidAt(double time, double lifetime) {
         return timestamp + lifetime > time;
     }
 
     /**
      * Specify the web service that uses this session.
      * @param webService The web service object, from authorization
      * database.
      */
     public void setWebService(WebService webService) {
         this.webService = webService;
     }
 
 
     /**
      * Return the web service that uses this session.
      */
     public WebService getWebService() {
         return webService;
     }
 
     
     /**
      * Return the name of the security level for the requested attributes.
      */
     public String getAttributesSecLevel() {
         return attributesSecLevel;
     }
 
     
     /** 
      * Return true if the session can be used with SSO.
      */
     public boolean getAllowSso() {
         return allowSso;
     }
 
 
     /** 
      * Prohibit the session from using SSO.
      */
     public void setDenySso() {
         allowSso = false;
     }
 
 
     /**
      * Return true if session is locked and cannot be used to retreive
      * attributes.
      */
     public boolean isLocked() {
         return locked;
     }
 
     /**
      * Unlock session. 
      * @param user The user attribute that is assosiated with a
      * session.
      */
     public void unlock(User user) {
         this.user = user;
         locked = false;
     }
 
     /**
      * Return the user object that is authenticated.
      */
     public User getUser() {
         return user;
     }
 
     /**
      * Flag that the login page is retrieved and that the session now
      * accepts authentication attempts.
      */
     public void initiateAuthentication() {
         authenticationInitiated = true;
     }
 
     /**
      * Return true if the session is open for authentication attempts.
      */
     public boolean authenticationInitiated() {
         return authenticationInitiated;
     }
 
     /**
      * Return the attributes that are cached in the session. Used to
      * transfer the attributes from one session to another.
      */
     public HashMap getCachedAttributes() {
         return cachedAttributes;
     }
 
     /**
      * Set the cached attributes. This method is used when
      * transferring a set of attributes from another session.
      * @param attrs The cached attributes, transferred from another
      * session.
      */
     public void setCachedAttributes(HashMap attrs) {
         cachedAttributes = attrs;
     }
     
 }
