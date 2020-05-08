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
 
 package no.feide.moria.service;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 
 import java.net.URL;
 import java.security.Principal;
 import java.rmi.RemoteException;
 import java.net.MalformedURLException;
 
 import java.util.Date;
 import java.util.Timer;
 import java.util.HashMap;
 import java.util.logging.Logger;
 
 import javax.xml.rpc.ServiceException;
 import javax.xml.rpc.server.ServiceLifecycle;
 import javax.xml.rpc.server.ServletEndpointContext;
 
 import no.feide.moria.Configuration;
 import no.feide.moria.ConfigurationException;
 import no.feide.moria.Session;
 import no.feide.moria.SessionStore;
 import no.feide.moria.SessionException;
 import no.feide.moria.BackendException;
 import no.feide.moria.Credentials;
 import no.feide.moria.authorization.WebService;
 import no.feide.moria.authorization.AuthorizationData;
 import no.feide.moria.authorization.AuthorizationTask;
 
 
 public class AuthenticationImpl
 implements AuthenticationIF, ServiceLifecycle {
 
     /** Used for logging. */
     private static Logger log = Logger.getLogger(AuthenticationImpl.class.toString());
 
     /** Used to retrieve the client identity. */
     private ServletEndpointContext ctx;
     
     /** Session store. */
     private SessionStore sessionStore;
       
     /** Timer for updating the web service authorization module. */
     private Timer authTimer = new Timer(true);
 
     
     /**
      * Service endpoint destructor. Some basic housekeeping.
      */
     public void destroy() {
 	log.finer("destroy()");
 
         authTimer.cancel();
 	log = null;
 	ctx = null;
     }
 
 
     /**
      * Service endpoint initialization. Will read the <code>Properties</code>
      * file found in the location given by the system property
      * <code>no.feide.moria.config.file</code>. If the property is not
      * set, the default filename is <code>/moria.properties</code>.
      * @param context The servlet context, used to find the user (client service)
      *                identity in later methods.
      * @throws ServiceException If a FileNotFoundException or IOException id
      *                          caught when reading the properties file. Also
      *                          thrown if the system property
      *                          <code>no.feide.moria.AuthorizationTimerDelay</code>
      *                          is not set, or if there is a problem getting
      *                          the session store instance.
      */
     public void init(Object context) 
     throws ServiceException {
 	log.finer("init(Object)");
 
 	ctx = (ServletEndpointContext)context;
        
         try {
             
             sessionStore = SessionStore.getInstance();
             
             int authDelay = new Integer(Configuration.getProperty("no.feide.moria.AuthorizationTimerDelay")).intValue()*1000; // Seconds to milliseconds
             log.config("Starting authorization update service with delay= "+authDelay+"ms");
             authTimer.scheduleAtFixedRate(new AuthorizationTask(), new Date(), authDelay);
             
             /* Sleep a short while. If not the authorization data will not
                be updated in time to authorize the first authentication request. */
             try { 
                 int initialSleep = new Integer(Configuration.getProperty("no.feide.moria.AuthorizationTimerInitThreadSleep")).intValue(); 
                 log.config("Sleep "+initialSleep+" seconds while reading auth. config.");
                 Thread.sleep(initialSleep*1000); 
             } 
             catch (InterruptedException e) { 
                 /* We didn't get any sleep. Don't care. If this is the
                  * case, the first web service authorization request will
                  * end in an exception. After that everything will be all
                  * right.
                  */
             }
             
         } catch (ConfigurationException e) {
             log.severe("ConfigurationException caught and re-thrown as ServiceException");
             throw new ServiceException("ConfigurationException caught", e);
         } catch (SessionException e) {
             log.severe("SessionException caught and re-thrown as ServiceException");
             throw new ServiceException("SessionException caught", e);
         }
 
     }
 
 
     /**
      * A simple wrapper for 
      * <code>requestSession(String[], String, String, true)</code>. That is,
      * the web service configuration is used to determine if SSO should be
      * used or not.
      * @param attributes The requested user attributes, to be returned from
      *                   <code>verifySession()</code> once authentication is
      *                   complete. <code>null</code> value allowed.
      * @param prefix The prefix, used to build the <code>verifySession</code>
      *               return value. May be <code>null</code>.
      * @param postfix The postfix, used to build the
      *                <code>verifySession</code> return value. May be
      *                <code>null</code>.
      * @return An URL to the authentication service.
      * @throws RemoteException If a SessionException or a
      *                         BackendException is caught. Also thrown if the
      *                         prefix/postfix doesn't combine into a valid
      *                         URL, or if the 
      */
     public String requestSession(String[] attributes, String prefix, String postfix)
     throws RemoteException {
         log.finer("requestSession(String[], String, String)");
 
 	return requestSession(attributes, prefix, postfix, false);
     }
 
 
     /**
      * Request a new Moria session, with the option to turn off SSO even though
      * the web service authorization config would allow it.
      * @param attributes The requested user attributes, to be returned from
      *                   <code>verifySession()</code> once authentication is
      *                   complete. <code>null</code> value allowed.
      * @param prefix The prefix, used to build the <code>verifySession</code>
      *               return value. May be <code>null</code>.
      * @param postfix The postfix, used to build the
      *                <code>verifySession</code> return value. May be
      *                <code>null</code>.
      * @param denySSO If <code>true</code> SSO is disabled even though the
      *                web service config may allow SSO. If <code>false</code>,
      *                the config is used to determine if the web service can
      *                use SSO.
      * @return An URL to the authentication service.
      * @throws RemoteException If a SessionException or a
      *                         BackendException is caught. Also thrown if the
      *                         prefix/postfix doesn't combine into a valid
      *                         URL, or if a <code>ConfigurationException<code>
      *                         is caught.
      */
     public String requestSession(String[] attributes, String prefix, String postfix, boolean denySso)
     throws RemoteException {
         log.finer("requestSession(String[], String, String, boolean)");
         
         /* Check if prefix and postfix, together with a possible
          * session ID, is a valid URL. */
         String simulatedURL = prefix+"MORIAID"+postfix;
         try {
             new URL(simulatedURL);
         } catch (MalformedURLException e) {
             log.severe("Malformed URL: "+simulatedURL);
             throw new RemoteException("Malformed URL: "+simulatedURL);
         }
 
         /* Look up service authorization data. */
         Principal p = ctx.getUserPrincipal();
         String serviceName = null;
         if (p != null)
             serviceName = p.getName();
 	log.fine("Client service requesting session: "+serviceName);
         
         WebService ws = AuthorizationData.getInstance().getWebService(serviceName);
         if (ws == null) {
             log.warning("Unauthorized service access: "+serviceName);
             throw new RemoteException("Web Service not authorized for use with Moria");
         } else if (!ws.allowAccessToAttributes(attributes)) {
             log.warning("Attribute request from service "+serviceName+" refused");
             throw new RemoteException("Access to one or more attributes prohibited");
         }
 
         try {
             Session session = sessionStore.createSession(attributes, prefix, postfix, p, ws);      
             
             /* Turn of SSO if required by web service. */
            if (!denySso) 
                 session.denySso();
 
             return session.getRedirectURL();
         } catch (SessionException e) {
             log.severe("SessionException caught and re-thrown as RemoteException");
             throw new RemoteException("SessionException caught", e);
         } catch (ConfigurationException e) {
             log.severe("ConfigurationException caught and re-thrown as RemoteException");
             throw new RemoteException("ConfigurationException caught", e);
         }
         
     }
 
 
     /* Return the previously requested user attributes.
      * @param The session ID.
      * @return The previously requested user attributes.
      * @throws RemoteException If a SessionException or a
      *                         BackendException is caught. Also thrown if
      *                         the current client's identity (as found in
      *                         the context) is different from the identity
      *                         of the client service originally requesting
      *                         the session.
      */
     public HashMap getAttributes(String id)
     throws RemoteException {
         log.finer("getAttributes(String)");
 
 	try {
 
 	    /* Look up session and check the client identity. */
             Session session = sessionStore.getSession(id);
 
 
             String serviceName = null;
             if (ctx.getUserPrincipal() != null)
                 serviceName = ctx.getUserPrincipal().getName();
             
             if (!session.getWebService().getId().equals(serviceName)) {
                 log.severe("WebService ("+serviceName+") tried to get unauthorized access to auth session.");
                 throw new RemoteException("Access denied");
             }
 
 
             if (session.isLocked()) {
                 log.warning("Service ("+serviceName+") tried to access locked session: "+session.getID());
                 throw new RemoteException("No such session.");
             }
 
 	    assertPrincipals(ctx.getUserPrincipal(), session.getClientPrincipal());
 
 	    /* Return attributes. */
             HashMap result = session.getAttributes();
             return result;
 
         } catch (SessionException e) {
             log.severe("SessionException caught, and re-thrown as RemoteException");
             throw new RemoteException("SessionException caught", e);
         }
     }
 
 
     /**
      * Utility method, used to check if a given client service principal
      * matches the principal stored in the session. <code>null</code> values
      * are allowed.
      * @param pCurrent The current client principal.
      * @param pStored The principal stored in the session.
      * @throws SessionException If there's a problem getting the session from
      *                          the session sessionStore.
      * @throws RemoteException If the client principals didn't match.
      */
     private static void assertPrincipals(Principal pCurrent, Principal pStored)
     throws SessionException, RemoteException {
 	log.finer("assertPrincipals(Principal, String)");
 
 	if (pCurrent == null) {
 	    if (pStored == null)
 		return;
 	}
 	else if (pCurrent.toString().equals(pStored.toString()))
 	    return;
 
         log.severe("Client service identity mismatch; "+pCurrent+" != "+pStored);
 	throw new RemoteException("Client service identity mismatch");
     }
 
 }
