 /*
  * Copyright (c) 2004 UNINETT FAS
  *
  * This program is free software; you can redistribute it and/or modify it
  * under the terms of the GNU General Public License as published by the Free
  * Software Foundation; either version 2 of the License, or (at your option)
  * any later version.
  *
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
  * more details.
  *
  * You should have received a copy of the GNU General Public License along with
  * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
  * Place - Suite 330, Boston, MA 02111-1307, USA.
  *
  * $Id$
  */
 
 package no.feide.moria.controller;
 
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Properties;
 
 import javax.servlet.ServletContext;
 
 import no.feide.moria.authorization.AuthorizationManager;
 import no.feide.moria.authorization.UnknownAttributeException;
 import no.feide.moria.authorization.UnknownServicePrincipalException;
 import no.feide.moria.configuration.ConfigurationManager;
 import no.feide.moria.configuration.ConfigurationManagerException;
 import no.feide.moria.directory.Credentials;
 import no.feide.moria.directory.DirectoryManager;
 import no.feide.moria.directory.backend.AuthenticationFailedException;
 import no.feide.moria.directory.backend.BackendException;
 import no.feide.moria.log.AccessLogger;
 import no.feide.moria.log.AccessStatusType;
 import no.feide.moria.log.MessageLogger;
 import no.feide.moria.store.InvalidTicketException;
 import no.feide.moria.store.MoriaAuthnAttempt;
 import no.feide.moria.store.MoriaStore;
 import no.feide.moria.store.MoriaTicketType;
 import no.feide.moria.store.MoriaStoreConfigurationException;
 import no.feide.moria.store.MoriaStoreException;
 import no.feide.moria.store.MoriaStoreFactory;
 import no.feide.moria.store.NonExistentTicketException;
 
 /**
  * Intermediator for the sub modules of Moria. The controller is the only entry
  * point for accessing Moria. Basically, all work is done by the authorization
  * module, the distributed store, the directory manager and the logger. The
 * controller must be initialized from the servlets that are using it. This can be
  * done by calling the <code>initController</code> method.
  * @author Lars Preben S. Arnesen &lt;lars.preben.arnesen@conduct.no&gt;
  * @version $Revision$
  * @see MoriaController#initController(javax.servlet.ServletContext)
  */
 public final class MoriaController {
     
     /**
      * Ticket type constant, indicating an SSO ticket, for use when returning a
      * HashMap of two tickets.
      * @see MoriaController#attemptLogin(java.lang.String, java.lang.String,
      *      java.lang.String, java.lang.String)
      * @see MoriaController#attemptSingleSignOn(java.lang.String,
      *      java.lang.String)
      */
     public static final String SSO_TICKET = "sso";
 
     
     /**
      * Ticket type constant, indicating a login ticket, for use when returning a
      * HashMap with multiple tickets.
      * @see MoriaController#attemptLogin(java.lang.String, java.lang.String,
      *      java.lang.String, java.lang.String)
      * @see MoriaController#attemptSingleSignOn(java.lang.String,
      *      java.lang.String)
      */
     public static final String SERVICE_TICKET = "service";
 
     /**
      * Operation type for local authentication.
      */
     private static final String DIRECT_AUTH_OPER = "DirectAuth";
 
     /**
      * Operation type for interactive authentication.
      */
     private static final String INTERACTIVE_AUTH_OPER = "InteractiveAuth";
 
     /**
      * Operation type for interactive authentication.
      */
     private static final String PROXY_AUTH_OPER = "ProxyAuth";
 
     /**
      * Operation type for verify user existence.
      */
     private static final String VERIFY_USER_EXISTENCE_OPER = "VerifyUserExistence";
 
     /**
      * Identifier for the TGT used in attribute requests.
      */
     static final String TGT_IDENTIFIER = "tgt";
 
     /**
      * Standard exception message for indication that the store is unavailable.
      */
     private static final String STORE_DOWN = "Moria is unavailable, the store is down.";
 
     /**
      * Standard exception message for indication that the controller is not
      * ready.
      */
     private static final String NOT_READY = "Moria is unavailable, the controller is not ready.";
 
     /**
      * Standard exception message for indication that ticket does not exist.
      */
     private static final String NONEXISTENT_TICKET = "Ticket does not exist.";
 
     /**
      * Standard log message for NonExistentTicketException.
      */
     private static final String CAUGHT_NONEXISTENT_TICKET = "NonExistentTicketException caught";
 
     /**
      * Standard log message for InvalidTicketException.
      */
     private static final String CAUGHT_INVALID_TICKET = "InvalidTicketException caught";
 
     /**
      * Standard log message for InvalidTicketException.
      */
     private static final String CAUGHT_STORE = "MoriaStoreException caught";
     
     /**
      * Log message for AuthorizationException.
      */
     private static final String CAUGHT_DENIED_USERORG = "AuthorizationException caught";
 
     /**
      * The single instance of the data store.
      */
     private static MoriaStore store;
 
     /**
      * The single instance of the configuration manager.
      */
     private static ConfigurationManager configManager;
 
     /**
      * The single instance of the authorization manager.
      */
     private static AuthorizationManager authzManager;
 
     /**
      * The single instance of the directory manager.
      */
     private static DirectoryManager directoryManager;
 
     /**
      * Flag set to true if the controller has been initialized.
      */
     private static Boolean isInitialized = new Boolean(false);
 
     /**
      * Flag set to true if the controller and all modules are ready.
      */
     private static boolean ready = false;
 
     /**
      * Flag set to true if the authorization manager is ready.
      */
     private static boolean amReady = false;
 
     /**
      * Flag set to true if the directory manager is ready.
      */
     private static boolean dmReady = false;
 
     /**
      * Flag set to true if the store manager is ready.
      */
     private static boolean smReady = false;
 
     /**
      * The servlet context for the servlets using the controller.
      */
     private static ServletContext servletContext;
 
     /**
      * Used for access logging.
      */
     private static AccessLogger accessLogger;
 
     /**
      * Used for message/error logging.
      */
     private static MessageLogger messageLogger;
 
 
     /**
      * Private constructor. Never to be used.
      */
     private MoriaController() {
 
     }
 
 
     /**
      * Initiates the controller. The initialization includes the initialization
      * of all sub modules.
      * @throws InoperableStateException
      *             If Moria is not ready for use.
      */
     static synchronized void init() throws InoperableStateException {
 
         synchronized (isInitialized) {
 
             /* Only run once */
             if (isInitialized.booleanValue()) { return; }
             isInitialized = new Boolean(true);
 
             /* Logger */
             messageLogger = new MessageLogger(MoriaController.class);
             accessLogger = new AccessLogger();
 
             /* Store */
             try {
                 store = MoriaStoreFactory.createMoriaStore();
             } catch (MoriaStoreException e) {
                 messageLogger.logCritical("Store failed to start", e);
                 throw new InoperableStateException("Moria cannot start, the store is unavailable.");
             }
 
             /* Authorization */
             authzManager = new AuthorizationManager();
 
             /* Directory */
             directoryManager = new DirectoryManager();
 
             /* Configuration manager */
             try {
                 configManager = new ConfigurationManager();
             } catch (ConfigurationManagerException e) {
                 messageLogger.logCritical("Moria cannot start, configuration failed.", e);
                 throw new InoperableStateException("Moria cannot start, configuration failed. " + e.getMessage());
             }
         }
     }
 
 
     /**
      * Shuts down the controller. All ready status fields are set to false.
      */
     static synchronized void stop() {
 
         synchronized (isInitialized) {
             if (ready) {
                 authzManager = null;
                 amReady = false;
                 configManager.stop();
                 configManager = null;
                 directoryManager.stop();
                 directoryManager = null;
                 dmReady = false;
                 store.stop();
                 store = null;
                 smReady = false;
                 servletContext = null;
                 ready = false;
                 isInitialized = new Boolean(false);
             }
         }
     }
 
 
     /**
      * Gets the total status of the controller. The method returns a HashMap with
      * Boolean values. The following elements are in the map: <ul>
      * <li>init: <code>true</code> if the <code>initController</code> method has
      * been called, else <code>false</code>.
      * <li>dm: <code>true</code> if the <code>DirectoryManager.setConfig</code>
      * method has been called, else <code>false</code>.
      * <li>sm: <code>true</code> if the <code>MoriaStore.setConfig</code> method
      * has been called, else <code>false</code>.
      * <li>am: <code>true</code> if the
      * <code>AuthorizationManager.setConfig</code> method has been called,
      * else <code>false</code>.
      * <li>moria: <code>true</code> all the above are true (the controller is
      * ready to use).</ul>
      * @return A <code>HashMap</code> with all status fields for the
      *         controller (<code>init</code>,<code>dm</code>,
      *         <code>sm</code>,<code>am</code> and <code>moria</code>).
      * @see MoriaController#initController(javax.servlet.ServletContext)
      * @see DirectoryManager#setConfig(java.util.Properties)
      * @see MoriaStore#setConfig(java.util.Properties)
      * @see AuthorizationManager#setConfig(java.util.Properties)
      */
     public static HashMap getStatus() {
 
         final HashMap totalStatus = new HashMap();
         totalStatus.put("init", isInitialized);
         totalStatus.put("dm", new Boolean(dmReady));
         totalStatus.put("sm", new Boolean(smReady));
         totalStatus.put("am", new Boolean(amReady));
         totalStatus.put("moria", new Boolean(ready));
 
         return totalStatus;
     }
 
 
     /**
      * Attempts single sign on (non-interactive) with an SSO ticket together with
      * the login ticket. If both tickets are valid and the requested attributes
      * are cached, a service ticket is returned and there is no need to perform
      * the regular interactive authentication.
      * @param loginTicketId
      *            The reference to the authentication attempt.
      * @param ssoTicketId
      *            The SSO ticket received from the users browser.
      * @return A service ticket.
      * @throws UnknownTicketException
      *             If either the login ticket or the SSO ticket is invalid or
      *             non-existing, the authetication attempt requires interactive
      *             authentication, or the SSO ticket does not point to a
      *             cached user data object with enough attributes.
      * @throws InoperableStateException
      *             If the controller is not ready.
      * @throws IllegalInputException
      *             If the <code>loginTicketId</code> and/or
      *             <code>ssoTicketId</code> is null or empty.
      */
     public static String attemptSingleSignOn(final String loginTicketId, final String ssoTicketId)
     throws UnknownTicketException, InoperableStateException,
     IllegalInputException {
 
         /* Check controller status */
         if (!ready) { throw new InoperableStateException(NOT_READY); }
 
         /* Validate arguments */
         if (loginTicketId == null || loginTicketId.equals("")) { throw new IllegalInputException("loginTicketId must be a non-empty string."); }
         if (ssoTicketId == null || ssoTicketId.equals("")) { throw new IllegalInputException("ssoTicketId must be a non-empty string."); }
 
         /* Get authentication attempt */
         final MoriaAuthnAttempt authnAttempt;
         try {
             authnAttempt = store.getAuthnAttempt(loginTicketId, true, null);
         } catch (InvalidTicketException e) {
             accessLogger.logUser(AccessStatusType.INVALID_LOGIN_TICKET, null, null, loginTicketId, null);
             messageLogger.logWarn(CAUGHT_INVALID_TICKET, loginTicketId, e);
             throw new UnknownTicketException(NONEXISTENT_TICKET);
         } catch (NonExistentTicketException e) {
             accessLogger.logUser(AccessStatusType.NONEXISTENT_LOGIN_TICKET, null, null, loginTicketId, null);
             messageLogger.logInfo(CAUGHT_NONEXISTENT_TICKET, loginTicketId, e);
             throw new UnknownTicketException(NONEXISTENT_TICKET);
         } catch (MoriaStoreException e) {
             messageLogger.logCritical(CAUGHT_STORE, loginTicketId, e);
             throw new InoperableStateException(STORE_DOWN);
         }
 
         /* Check if SSO is enabled in authentication attempt */
         if (authnAttempt.isForceInterativeAuthentication()) {
             messageLogger.logInfo("SSO authentication attempt denied by web service provider.");
             throw new UnknownTicketException("Authentication attempt requires interactive authentication.");
         }
 
         /* Service can only request cached attributes or SSO fails */
         final String[] requestedAttributes = authnAttempt.getRequestedAttributes();
         final HashSet cachedAttributes = authzManager.getCachableAttributes();
         for (int i = 0; i < requestedAttributes.length; i++) {
             if (!cachedAttributes.contains(requestedAttributes[i])) {
                 messageLogger.logDebug("SSO authentication failed, request for non-cached attributes.");
                 throw new UnknownTicketException("SSO ticket not sufficient, service requests uncached attributes.");
             }
         }
 
         /* Transfer attributes from cached user data to authentication attempt. */
         final String serviceTicket;
         try {
             /* Put transient attributes into authnattempt */
             store.setTransientAttributes(loginTicketId, ssoTicketId);
 
             /* set loginTicket userorg from SSO ticket */
             String userorg = store.getTicketUserorg(ssoTicketId, MoriaTicketType.SSO_TICKET); 
             store.setTicketUserorg(loginTicketId, MoriaTicketType.LOGIN_TICKET, userorg);
 
             /* Get service ticket */
             serviceTicket = store.createServiceTicket(loginTicketId);
         } catch (InvalidTicketException e) {
             accessLogger.logUser(AccessStatusType.INVALID_SSO_TICKET, null, null, ssoTicketId, null);
             messageLogger.logWarn(CAUGHT_INVALID_TICKET, ssoTicketId, e);
             throw new UnknownTicketException(NONEXISTENT_TICKET);
         } catch (NonExistentTicketException e) {
             accessLogger.logUser(AccessStatusType.NONEXISTENT_SSO_TICKET, null, null, ssoTicketId, null);
             messageLogger.logInfo(CAUGHT_NONEXISTENT_TICKET, ssoTicketId, e);
             throw new UnknownTicketException(NONEXISTENT_TICKET);
         } catch (MoriaStoreException e) {
             messageLogger.logCritical(CAUGHT_STORE, ssoTicketId, e);
             throw new InoperableStateException(STORE_DOWN);
         }
 
         accessLogger.logUser(AccessStatusType.SUCCESSFUL_SSO_AUTHENTICATION, authnAttempt.getServicePrincipal(), null, loginTicketId, serviceTicket);
         return serviceTicket;
     }
 
 
     /**
      * Performs interactive login attempt using tickets and credentials. The
      * authentication is performed by the directory service, using the supplied
      * username and password. All retrieved user data is cached in the
      * authentication attempt, identified by the <code>loginTicketId</code>.
      * A new cached userdata object is created and all cachable attributes are
      * stored in it. The existing SSO ticket is removed. After a successful
      * authentication a new service ticket, pointing to the same authentication
      * attempt, is created. A new SSO ticket is created, pointing to the cached
      * userdata object.
      * @param loginTicketId
      *            The ticket identifying the authentication attempt.
      * @param ssoTicketId
      *            The ticket identifying the existing cached user data object.
      * @param userId
      *            The user's userId.
      * @param password
      *            The user's password.
      * @param denySSO
      *            The user's SSO choice.
      * @return A HashMap with two tickets: login and SSO, indexed with
      *         <code>MoriaController.SSO_TICKET</code> and
      *         <code>MoiraController.LOGIN_TICKET</code>.
      * @throws UnknownTicketException
      *             If the login ticket is invalid or does not exist.
      * @throws InoperableStateException
      *             If the controller is not ready to be used, or the store
      *             cannot be accessed.
      * @throws IllegalInputException
      *             If any of <code>loginTicketId</code>,<code>userId</code>,
      *             or <code>password</code> are <code>null</code> or an
      *             empty string.
      * @throws AuthenticationException
      *             If the authentication failed due to wrong credentials.
      * @throws AuthorizationException
      *             If the user's organization is not allowed to use this service
      * @throws DirectoryUnavailableException
      *             If the directory of the user's home organization is
      *             unavailable.
      */
     public static Map attemptLogin(final String loginTicketId, final String ssoTicketId, final String userId, final String password, final boolean denySSO)
     throws UnknownTicketException, InoperableStateException,
     IllegalInputException, AuthenticationException,
     DirectoryUnavailableException, AuthorizationException {
 
         // Sanity checks.
         if (!ready)
             throw new InoperableStateException(NOT_READY);
         if (loginTicketId == null || loginTicketId.equals(""))
             throw new IllegalInputException("Login ticket ID must be a non-empty string");
         if (userId == null || userId.equals(""))
             throw new IllegalInputException("User ID must be a non-empty string");
         if (password == null || password.equals(""))
             throw new IllegalInputException("Password must be a non-empty string");
                 
         // Look up authentication attempt from the store.
         final MoriaAuthnAttempt authnAttempt;
         String userorg = null;
         try {
             String servicePrincipal = store.getTicketServicePrincipal(loginTicketId, MoriaTicketType.LOGIN_TICKET);
             userorg = getUserOrg(userId, password);
             // remember userorg for this ticket
             store.setTicketUserorg(loginTicketId, MoriaTicketType.LOGIN_TICKET, userorg);
             /* check userorg */
             if (!authzManager.allowUserorg(servicePrincipal, userorg)) {
                 throw new AuthorizationException("Access to the requested service is denied for " + userorg + ".");                
             }            
             authnAttempt = store.getAuthnAttempt(loginTicketId, true, null);
         } catch (NonExistentTicketException e) {
             accessLogger.logUser(AccessStatusType.NONEXISTENT_LOGIN_TICKET, null, userId, loginTicketId, null);
             messageLogger.logDebug(CAUGHT_NONEXISTENT_TICKET, loginTicketId, e);
             throw new UnknownTicketException(NONEXISTENT_TICKET);
         } catch (InvalidTicketException e) {
             accessLogger.logUser(AccessStatusType.INVALID_LOGIN_TICKET, null, userId, loginTicketId, null);
             messageLogger.logWarn(CAUGHT_INVALID_TICKET, loginTicketId, e);
             throw new UnknownTicketException(NONEXISTENT_TICKET);
         } catch (MoriaStoreException e) {
             messageLogger.logCritical(CAUGHT_STORE, loginTicketId, e);
             throw new InoperableStateException(STORE_DOWN);
         } catch (UnknownServicePrincipalException e) {
             // should not happen
             throw new AuthorizationException("Access to the requested service is denied for " + userorg + ".");
         }
         
         // TODO: Must be done for directNonInteractiveAuthentication. Should be
         // extracted to a method.
 
         // Parse requested attributes and extract special attributes.
         final String[] requestedAttributes = authnAttempt.getRequestedAttributes();
         final HashSet parsedRequestAttributes = new HashSet();
         boolean appendTGT = false;
         
         //TGT is only granted when users use SSO
         for (int i = 0; i < requestedAttributes.length; i++) {
             if (requestedAttributes[i].equals(TGT_IDENTIFIER) && !denySSO) {
                 appendTGT = true;
                 } else {
                     parsedRequestAttributes.add(requestedAttributes[i]);
                 }                            
         }
 
         // Resolve list of cached and requested attributes.
         final HashSet cachableAttributes = authzManager.getCachableAttributes();
         final HashSet retrieveAttributes = new HashSet(cachableAttributes);
         retrieveAttributes.addAll(parsedRequestAttributes);
 
         // Authentication.
         final HashMap fetchedAttributes;
         try {
             fetchedAttributes = directoryManager.authenticate(loginTicketId, new Credentials(userId, password), (String[]) retrieveAttributes.toArray(new String[retrieveAttributes.size()]));
         } catch (AuthenticationFailedException e) {
             accessLogger.logUser(AccessStatusType.BAD_USER_CREDENTIALS, null, userId, loginTicketId, null);
             messageLogger.logDebug("AuthenticationFailedException caught", loginTicketId, e);
             throw new AuthenticationException();
         } catch (BackendException e) {
             messageLogger.logWarn("BackendException caught", loginTicketId, e);
             throw new DirectoryUnavailableException();
         }
 
         // Remove any existing SSO ticket.
         if (ssoTicketId != null && !ssoTicketId.equals("")) {
             try {
                 store.removeSSOTicket(ssoTicketId);
             } catch (NonExistentTicketException e) {
                 // The ticket has probably already timed out.
                 messageLogger.logDebug("SSO ticket does not exist - may have timed out", ssoTicketId, e);
             } catch (MoriaStoreException e) {
                 // Unable to access the store.
                 messageLogger.logCritical(CAUGHT_STORE, ssoTicketId, e);
                 throw new InoperableStateException(STORE_DOWN);
             }
         }
 
         // Cache attributes and get tickets.
         final String serviceTicketId;
         final String newSSOTicketId;
         final HashMap cacheAttributes = new HashMap();
         final HashMap authnAttemptAttrs = new HashMap();
 
         final Iterator it = cachableAttributes.iterator();
         while (it.hasNext()) {
             final String attrName = (String) it.next();
             cacheAttributes.put(attrName, fetchedAttributes.get(attrName));
         }
 
         for (int i = 0; i < requestedAttributes.length; i++) {
             authnAttemptAttrs.put(requestedAttributes[i], fetchedAttributes.get(requestedAttributes[i]));
         }
 
         try {
             newSSOTicketId = store.cacheUserData(cacheAttributes, userorg);
             store.setTicketUserorg(newSSOTicketId, MoriaTicketType.SSO_TICKET, userorg);
             if (appendTGT) {
                 authnAttemptAttrs.put(TGT_IDENTIFIER, store.createTicketGrantingTicket(newSSOTicketId, authnAttempt.getServicePrincipal()));
             }
             store.setTransientAttributes(loginTicketId, authnAttemptAttrs);
             serviceTicketId = store.createServiceTicket(loginTicketId);
             store.setTicketUserorg(serviceTicketId, MoriaTicketType.SERVICE_TICKET, userorg);
 
         } catch (NonExistentTicketException e) {
             /* Should not happen due to previous validation in this method */
             messageLogger.logWarn(CAUGHT_NONEXISTENT_TICKET + ", should not happen (already validated)", loginTicketId, e);
             throw new UnknownTicketException(NONEXISTENT_TICKET);
         } catch (InvalidTicketException e) {
             messageLogger.logWarn(CAUGHT_INVALID_TICKET + ", should not happen (already validated)", loginTicketId, e);
             throw new UnknownTicketException(NONEXISTENT_TICKET);
         } catch (MoriaStoreException e) {
             messageLogger.logCritical(CAUGHT_STORE, ssoTicketId, e);
             throw new InoperableStateException(STORE_DOWN);
         }
 
         /* Return tickets */
         final HashMap tickets = new HashMap();
         tickets.put(SERVICE_TICKET, serviceTicketId);
         tickets.put(SSO_TICKET, newSSOTicketId);
 
         accessLogger.logUser(AccessStatusType.SUCCESSFUL_INTERACTIVE_AUTHENTICATION, authnAttempt.getServicePrincipal(), userId, loginTicketId, "Service: " + serviceTicketId + " SSO: " + ssoTicketId);
 
         return tickets;
     }
 
 
     /**
      * Initiates authentication through Moria. An authentication attempt is
      * created and the supplied argument is stored in it for later use. After a
      * successful authentication, the user is redirected back to a URL consisting
      * of the URL prefix and postfix, with the service ticket added in the
      * middle.
      * @param attributes
      *            The requested attributes. Cannot be <code>null</code>.
      * @param returnURLPrefix
      *            Prefix of the redirect URL, used to direct the user back to
      *            the web service. Cannot be <code>null</code> or an empty
      *            string.
      * @param returnURLPostfix
      *            Postfix of the redirect URL, used to direct the user back to
      *            the web service. Cannot be <code>null</code>.
      * @param forceInteractiveAuthentication
      *            If <code>true</code>, do not use SSO.
      * @param servicePrincipal
      *            The principal of the requesting service. Cannot be
      *            <code>null</code> or an empty string.
      * @return A login ticket ID.
      * @throws AuthorizationException
      *             If the service requests attributes it is not authorized to
      *             receive.
      * @throws IllegalInputException
      *             If <code>attributes</code> or <code>returnURLPostfix</code>
      *             is <code>null</code>, or <code>returnURLPrefix</code> or
      *             <code>servicePrincipal</code> is
      *             <code>null<code> or an empty string.
      * @throws InoperableStateException
      *             If the controller is not yet ready for use, or if the store cannot be accessed at this time.
      */
     public static String initiateAuthentication(final String[] attributes, final String returnURLPrefix, final String returnURLPostfix, final boolean forceInteractiveAuthentication, final String servicePrincipal)
     throws AuthorizationException, IllegalInputException,
     InoperableStateException {
 
         // Is the controller ready?
         if (!ready)
             throw new InoperableStateException("Controller is not ready");
 
         // Sanity checks.
         if (servicePrincipal == null || servicePrincipal.equals("")) {
             messageLogger.logCritical("Missing service principal - check service container configuration");
             throw new IllegalInputException("Service principal cannot be null or an empty string");
         }
         if (attributes == null)
             throw new IllegalInputException("Attributes cannot be null");
         if (returnURLPrefix == null || returnURLPrefix.equals(""))
             throw new IllegalInputException("URL prefix cannot be null or an empty string");
         if (returnURLPostfix == null)
             throw new IllegalInputException("URL postfix cannot be null");
 
         // Check authorization for this service. Supply null as userorg to delay this
         // test until later.
         authorizationCheck(servicePrincipal, attributes, INTERACTIVE_AUTH_OPER, null);
 
         // Validate the URL pre- and postfix.
         final String validationURL = returnURLPrefix + "FakeMoriaID" + "urlPostfix";
         if (!(isLegalURL(validationURL))) {
             accessLogger.logService(AccessStatusType.INITIATE_DENIED_INVALID_URL, servicePrincipal, null, null);
             messageLogger.logWarn("Service '" + servicePrincipal + "' tried to submit invalid URL '" + validationURL + "'");
             throw new IllegalInputException("Service supplied an invalid URL");
         }
 
         // Create authentication attempt.
         final String loginTicketId;
         try {
             loginTicketId = store.createAuthnAttempt(attributes, returnURLPrefix, returnURLPostfix, forceInteractiveAuthentication, servicePrincipal);
         } catch (MoriaStoreException e) {
             messageLogger.logCritical(CAUGHT_STORE, e);
             throw new InoperableStateException(STORE_DOWN);
         }
 
         // Log successful authentication initialization, and return the ticket ID.
         accessLogger.logService(AccessStatusType.SUCCESSFUL_AUTH_INIT, servicePrincipal, null, loginTicketId);
                 
         return loginTicketId;
     }
 
 
     /**
      * Performs an authorization validation of a service request. If no exception
      * is thrown, the authorization was successful.
      * @param servicePrincipal
      *            The principal for the service performing the request.
      * @param attributes
      *            The requested attributes.
      * @param operation
      *            The requested operation.
      * @param userorg
      * 		  The organization the user comes from, or null if unknown.
      * @throws AuthorizationException
      *             If the authorization failed.
      * @throws NullPointerException
      *             If servicePrincipal is null.
      * @throws IllegalArgumentException
      *             If servicePrincipal is an empty string or the operation 
      *             is wrong.
      */
     private static void authorizationCheck(final String servicePrincipal, final String[] attributes, final String operation, final String userorg)
     throws AuthorizationException {
 
         final AccessStatusType statusType;
 
         /* Validate arguments */
         if (servicePrincipal == null) {
             throw new NullPointerException("'servicePrincipal' cannot be null.");
         } else if (servicePrincipal == "") { throw new IllegalArgumentException("'servicePrincipal' cannot be an empty string."); }
 
         /* Set logging status type */
         if (operation == DIRECT_AUTH_OPER) {
             statusType = AccessStatusType.ACCESS_DENIED_DIRECT_AUTH;
         } else if (operation.equals(INTERACTIVE_AUTH_OPER)) {
             statusType = AccessStatusType.ACCESS_DENIED_INITIATE_AUTH;
         } else if (operation.equals(PROXY_AUTH_OPER)) {
             statusType = AccessStatusType.ACCESS_DENIED_PROXY_AUTH;
         } else if (operation.equals(VERIFY_USER_EXISTENCE_OPER)) {
             statusType = AccessStatusType.ACCESS_DENIED_VERIFY_USER_EXISTENCE;
         } else {
             throw new IllegalArgumentException("Wrong operation type: " + operation);
         }
 
         try {
             /* Operations */
             if (!authzManager.allowOperations(servicePrincipal, new String[] {operation})) {
                 accessLogger.logService(statusType, servicePrincipal, null, null);
                 messageLogger.logInfo("Service '" + servicePrincipal + "' tried to perform '" + operation + "', but can only do '" + authzManager.getOperations(servicePrincipal));
                 throw new AuthorizationException("Access to the requested operation is denied.");
             }
 
             /* Attributes */
             if (!authzManager.allowAccessTo(servicePrincipal, attributes)) {
                 accessLogger.logService(statusType, servicePrincipal, null, null);
                 messageLogger.logInfo("Service '" + servicePrincipal + "' tried to access '" + new HashSet(Arrays.asList(attributes)) + "', but have only access to '" + authzManager.getAttributes(servicePrincipal));
                 throw new AuthorizationException("Access to the requested attributes is denied.");
             }
             
             /* Userorg */
             if (userorg != null && !authzManager.allowUserorg(servicePrincipal, userorg)) {
                 accessLogger.logService(statusType, servicePrincipal, null, null);
                 //FIXME - this should be fixed to something more specific for the organization-check
                 messageLogger.logInfo("Service '" + servicePrincipal + "' tried to access '" + new HashSet(Arrays.asList(attributes)) + "', but have only access to '" + authzManager.getAttributes(servicePrincipal));
                 throw new AuthorizationException("Access to the requested service is denied for " + userorg + ".");                
             }
             
         } catch (UnknownServicePrincipalException e) {
             messageLogger.logWarn("UnknownServicePrincipalException caught during authorizationCheck, service probably not configured in authorization database.", e);
             throw new AuthorizationException("Authorization failed for: " + servicePrincipal);
         }
     }
 
 
     /**
      * Retrieves user attributes from an authentication attempt. The method
      * returns the user attributes stored in the authentication attempt, which
      * is referenced to by the service ticket. <br>
      * <br>
      * Note that this method can only be used once for each non-SSO
      * authentication attempt. For security reasons, Moria will not cache
      * attribute values longer than absolutely necessary.
      * @param serviceTicketId
      *            The ticket associated with the authentication attempt. Cannot
      *            be <code>null</code> or an empty string.
      * @param servicePrincipal
      *            The principal of the calling service. Cannot be
      *            <code>null</code> or an empty string.
      * @return A newly instantiated <code>Map</code> object containing the
      *         requested user attributes, if found. Entries have a
      *         <code>String</code> key and a <code>String[]</code> value.
      * @throws AuthorizationException
      *             If userorg isn't set for ticket, userorg is denied 
      *             access to the service or service principal is unknown.
      * @throws IllegalInputException
      *             If <code>serviceTicketId</code> or
      *             <code>servicePrincipal</code> is <code>null</code> or an
      *             empty string.
      * @throws UnknownTicketException
      *             If the service ticket does not exist in the store, or is
      *             invalid.
      * @throws InoperableStateException
      *             If Moria is not ready for use.
      * @throws
      */
     public static Map getUserAttributes(final String serviceTicketId, final String servicePrincipal)
     throws IllegalInputException, UnknownTicketException,
     InoperableStateException, AuthorizationException {
 
         // Sanity checks.
         if (!ready)
             throw new InoperableStateException(NOT_READY);
         if (serviceTicketId == null || serviceTicketId.equals(""))
             throw new IllegalInputException("Service ticket ID must be a non-empty string.");
         if (servicePrincipal == null || servicePrincipal.equals(""))
             throw new IllegalInputException("Service principal must be a non-empty string.");
 
         // Look up and return the requested attributes.
         HashMap filteredAttributes = new HashMap();
         try {
             String userorg = null;
             userorg = store.getTicketUserorg(serviceTicketId,
                                              MoriaTicketType.SERVICE_TICKET);
             if (userorg == null) {
                 throw new AuthorizationException("Userorg is not set for ticket");
             }
             /* check userorg */
             if (!authzManager.allowUserorg(servicePrincipal, userorg)) {
                 accessLogger.logService(AccessStatusType.ACCESS_DENIED_USERORG, servicePrincipal, serviceTicketId, null);
                 messageLogger.logWarn(CAUGHT_DENIED_USERORG + ", userorg (" + userorg + ") tried to access service (" + servicePrincipal + ")", serviceTicketId);
                 throw new AuthorizationException("Access to the requested service is denied for " + userorg + ".");                
             }
             
             // Get the originally requested attributes and all cached values.
             MoriaAuthnAttempt authenticationAttempt = store.getAuthnAttempt(serviceTicketId, false, servicePrincipal);
             String[] requestedAttributes = authenticationAttempt.getRequestedAttributes();
             final Map cachedAttributes = authenticationAttempt.getTransientAttributes();
 
             // Filter cached attributes; only those requested are to be
             // returned.
             for (int i = 0; i < requestedAttributes.length; i++)
                 if (cachedAttributes.containsKey(requestedAttributes[i]))
                     filteredAttributes.put(requestedAttributes[i], cachedAttributes.get(requestedAttributes[i]));
 
             // the service principal is unknown  
         } catch (UnknownServicePrincipalException e) {          
             accessLogger.logService(AccessStatusType.GET_USER_ATTRIBUTES_DENIED_INVALID_PRINCIPAL, servicePrincipal, serviceTicketId, null);
             messageLogger.logInfo("UnknownServicePrincipalException caught", e);
             throw new AuthorizationException("Unknown service principal: " + servicePrincipal);
         } catch (NonExistentTicketException e) {
 
             // Ticket did not exist in the store.
             accessLogger.logService(AccessStatusType.NONEXISTENT_SERVICE_TICKET, servicePrincipal, serviceTicketId, null);
             messageLogger.logWarn(CAUGHT_NONEXISTENT_TICKET + ", service (" + servicePrincipal + ") tried to fetch attributes to late", serviceTicketId, e);
             throw new UnknownTicketException(NONEXISTENT_TICKET);
 
         } catch (InvalidTicketException e) {
 
             // The ticket was found, but was invalid.
             accessLogger.logService(AccessStatusType.INVALID_SERVICE_TICKET, servicePrincipal, serviceTicketId, null);
             messageLogger.logWarn(CAUGHT_INVALID_TICKET + ", service (" + servicePrincipal + ") tried to fetch attributes to late", serviceTicketId, e);
             throw new UnknownTicketException(NONEXISTENT_TICKET);
 
         } catch (MoriaStoreException e) {
 
             // Unable to access the store.
             messageLogger.logCritical(CAUGHT_STORE, serviceTicketId, e);
             throw new InoperableStateException(STORE_DOWN);
 
         }
 
         // Return the filtered attributes only.
         accessLogger.logService(AccessStatusType.SUCCESSFUL_GET_ATTRIBUTES, servicePrincipal, serviceTicketId, null);
         return filteredAttributes;
     }
 
 
     /**
      * Performs a direct authentication without the use of tickets. The user is
      * authenticated directly against the backend, and the attributes retrieved
      * are returned to the caller.
      * @param requestedAttributes
      *            The requested attributes.
      * @param userId
      *            The user's username.
      * @param password
      *            The user's password.
      * @param servicePrincipal
      *            The principal of the calling service.
      * @return Map containing user attributes in strings or string arrays.
      * @throws AuthorizationException
      *             If the service is not allowed to perform this operation.
      * @throws IllegalInputException
      *             If <code>requestedAttributes</code> is null, or
      *             <code>userId</code> is null/empty, or <code>password</code>
      *             is null/empty, or <code>servicePrincipal</code> is
      *             null/empty.
      * @throws InoperableStateException
      *             If Moria is not ready for use.
      * @throws AuthenticationException
      *             If the authentication failed due to bad credentials.
      * @throws DirectoryUnavailableException
      *             If directory of the user's home organization is unavailable.
      */
     public static Map directNonInteractiveAuthentication(final String[] requestedAttributes, final String userId, final String password, final String servicePrincipal)
     throws AuthorizationException, IllegalInputException,
     InoperableStateException, AuthenticationException,
     DirectoryUnavailableException
     {
         
         /* Check controller state */
         if (!ready) { throw new InoperableStateException(NOT_READY); }
 
         /* Validate arguments */
         if (requestedAttributes == null) { throw new IllegalInputException("Attributes cannot be null"); }
         if (userId == null || userId.equals("")) { throw new IllegalInputException("UserId must be a non-empty string"); }
         if (password == null || password.equals("")) { throw new IllegalInputException("password must be a non-empty string"); }
         if (servicePrincipal == null || servicePrincipal.equals("")) { throw new IllegalInputException("servicePrincipal must be a non-empty string"); }
         
         //TODO doc and test later when the getUserOrg method is fixed
         String org = getUserOrg(userId, password);
 
         /* Authorize service */
         authorizationCheck(servicePrincipal, requestedAttributes, DIRECT_AUTH_OPER, org);
 
         /* Authenticate */
         final HashMap attributes;
         try {
             attributes = directoryManager.authenticate(null, new Credentials(userId, password), requestedAttributes);
         } catch (AuthenticationFailedException e) {
             accessLogger.logService(AccessStatusType.BAD_USER_CREDENTIALS, servicePrincipal, null, null);
             messageLogger.logInfo("AuthenticationFailedException caught", e);
             throw new AuthenticationException();
         } catch (BackendException e) {
             messageLogger.logWarn("Directory is unavailable. Tried to authenticate user: " + userId, e);
             throw new DirectoryUnavailableException();
         }
 
         accessLogger.logService(AccessStatusType.SUCCESSFUL_DIRECT_AUTHENTICATION, servicePrincipal, null, null);
         return attributes;
     }
 
 
     /**
      * Performs a ticket based proxy authentication. A proxy ticket and a set of
      * requested attributes are used to retrieve user data. Only cached userdata
      * can be retrieved.
      * @param requestedAttributes
      *            The requested attributes to retrieve.
      * @param proxyTicketId
      *            The proxy ticket connected with the cached user data.
      * @param servicePrincipal
      *            The principal of the requesting service.
      * @return Map containing user attributes with <code>String</code>
      *         (attribute name) as key and <code>String[]</code> (user
      *         attributes) as value.
      * @throws AuthorizationException
      *             If the service is not allowed to perform this operation.
      * @throws IllegalInputException
      *             If <code>requestedAttributes</code> is null, or
      *             <code>proxyTicketId</code> is null/empty, or
      *             <code>servicePrincipal</code> is null/empty.
      * @throws InoperableStateException
      *             If the controller is not ready to use.
      * @throws InvalidTicketException
      *             If userorg in not set in the proxy ticket.
      * @throws UnknownTicketException
      *             If the proxy ticket is invalid or does not exist.
      */
     public static Map proxyAuthentication(final String[] requestedAttributes, final String proxyTicketId, final String servicePrincipal)
     throws AuthorizationException, IllegalInputException,
     InoperableStateException, UnknownTicketException {
 
         /* Check controller state */
         if (!ready) { throw new InoperableStateException(NOT_READY); }
 
         /* Validate arguments */
         if (requestedAttributes == null) { throw new IllegalInputException("'requestedAttributes' cannot be null"); }
         if (proxyTicketId == null || proxyTicketId.equals("")) { throw new IllegalInputException("'proxyTicket' must be a non-empty string."); }
         if (servicePrincipal == null || servicePrincipal.equals("")) { throw new IllegalInputException("'servicePrincipal' must be a non-empty string."); }
 
         /* Check that attributes are cached */
         final HashMap result = new HashMap();
         final HashMap userData;
         final HashSet cachedAttributes = authzManager.getCachableAttributes();
         
         try {
             final String userorg = store.getTicketUserorg(proxyTicketId, MoriaTicketType.PROXY_TICKET); 
             if (userorg == null) throw new InvalidTicketException("Userorg is not set in ticket");
             authorizationCheck(servicePrincipal, requestedAttributes, 
                     PROXY_AUTH_OPER, userorg);
             userData = store.getUserData(proxyTicketId, servicePrincipal).getAttributes();
         } catch (InvalidTicketException e) {
             accessLogger.logService(AccessStatusType.INVALID_PROXY_TICKET, servicePrincipal, proxyTicketId, null);
             messageLogger.logWarn(CAUGHT_INVALID_TICKET, e);
             throw new UnknownTicketException(NONEXISTENT_TICKET);
         } catch (NonExistentTicketException e) {
             accessLogger.logService(AccessStatusType.NONEXISTENT_PROXY_TICKET, servicePrincipal, proxyTicketId, null);
             messageLogger.logDebug(CAUGHT_NONEXISTENT_TICKET, e);
             throw new UnknownTicketException(NONEXISTENT_TICKET);
         } catch (MoriaStoreException e) {
             messageLogger.logCritical(CAUGHT_STORE, e);
             throw new InoperableStateException(STORE_DOWN);
         }
 
         /* Get requested, cached attributes */
         for (int i = 0; i < requestedAttributes.length; i++) {
             final String attr = requestedAttributes[i];
             if (!cachedAttributes.contains(attr)) {
                 accessLogger.logService(AccessStatusType.PROXY_AUTH_DENIED_UNCACHED_ATTRIBUTES, servicePrincipal, proxyTicketId, null);
                 messageLogger.logInfo("Service (proxy authentication)'" + servicePrincipal + "' requested '" + new HashSet(Arrays.asList(requestedAttributes)) + "', but only the following are cached: '" + cachedAttributes);
                 throw new AuthorizationException("Requested attributes is not cached: '" + attr + "'");
             }
             result.put(attr, userData.get(attr));
         }
 
         accessLogger.logService(AccessStatusType.SUCCESSFUL_PROXY_AUTHENTICATION, servicePrincipal, proxyTicketId, null);
         return result;
     }
 
 
     /**
      * Generates a proxy ticket based on a TGT. A new proxy ticket is created,
      * referring to the same cached user data as the TGT does. The proxy ticket
      * will be owned by the target service, not the one that requested its
      * creation.
      * @param ticketGrantingTicket
      *            The TGT to generate a proxy ticket for.
      * @param proxyServicePrincipal
      *            The principal of the service that the proxy ticket is created
      *            for.
      * @param servicePrincipal
      *            The principal of the service requesting the ticket generation.
      * @return A <code>String</code> containing the proxy ticket.
      * @throws AuthorizationException
      *             If the requesting service is not allowed to perform the
      *             operation.
      * @throws IllegalInputException
      *             If <code>ticketGrantingTicket</code>,
      *             <code>proxyServicePrincipal</code> or
      *             <code>servicePrincipal</code> is null/empty.
      * @throws InoperableStateException
      *             If Moria is not ready for use.
      * @throws UnknownTicketException
      *             If the <code>ticketGrantingTicket</code> is invalid or does
      *             not exist, or <code>userorg</code> is not set in ticket.
      */
     public static String getProxyTicket(final String ticketGrantingTicket, final String proxyServicePrincipal, final String servicePrincipal)
     throws AuthorizationException, IllegalInputException,
     InoperableStateException, UnknownTicketException {
 
         /* Check controller state */
         if (!ready) { throw new InoperableStateException(NOT_READY); }
 
         /* Validate arguments */
         if (ticketGrantingTicket == null || ticketGrantingTicket.equals("")) { throw new IllegalInputException("'ticketGrantingTicket' must be a non-empty string."); }
         if (proxyServicePrincipal == null || proxyServicePrincipal.equals("")) { throw new IllegalInputException("'proxyServicePrincipal' must be a non-empty string."); }
         if (servicePrincipal == null || servicePrincipal.equals("")) { throw new IllegalInputException("'servicePrincipal' must be a non-empty string."); }
 
         /* Return proxyTicket */
         final String proxyTicketId;
         try {
             final String userorg = store.getTicketUserorg(ticketGrantingTicket, 
                                                           MoriaTicketType.TICKET_GRANTING_TICKET);
             if (userorg == null) throw new UnknownTicketException("Userorg is not set in ticket");
         
             /* Authorize creation of proxy ticket */
             authorizationCheck(servicePrincipal, new String[] {}, PROXY_AUTH_OPER, userorg);
             try {
                 if (!authzManager.getSubsystems(servicePrincipal).contains(proxyServicePrincipal)) {
                     accessLogger.logService(AccessStatusType.PROXY_TICKET_GENERATION_DENIED_UNAUTHORIZED, servicePrincipal, ticketGrantingTicket, null);
                     throw new AuthorizationException("Request for proxy ticket denied.");
                 }
             } catch (UnknownServicePrincipalException e) {
                 accessLogger.logService(AccessStatusType.PROXY_TICKET_GENERATION_DENIED_INVALID_PRINCIPAL, servicePrincipal, ticketGrantingTicket, null);
                 messageLogger.logInfo("UnknownServicePrincipalException caught", e);
                 throw new AuthorizationException("Unknown service principal: " + servicePrincipal);
             }
             proxyTicketId = store.createProxyTicket(ticketGrantingTicket, servicePrincipal, proxyServicePrincipal);
         } catch (InvalidTicketException e) {
             accessLogger.logService(AccessStatusType.INVALID_TGT, servicePrincipal, ticketGrantingTicket, null);
             messageLogger.logWarn(CAUGHT_INVALID_TICKET, e);
             throw new UnknownTicketException(NONEXISTENT_TICKET);
         } catch (NonExistentTicketException e) {
             accessLogger.logService(AccessStatusType.NONEXISTENT_TGT, servicePrincipal, ticketGrantingTicket, null);
             messageLogger.logInfo(CAUGHT_NONEXISTENT_TICKET, e);
             throw new UnknownTicketException(NONEXISTENT_TICKET);
         } catch (MoriaStoreException e) {
             messageLogger.logCritical(CAUGHT_STORE, e);
             throw new InoperableStateException(STORE_DOWN);
         }
 
         accessLogger.logService(AccessStatusType.SUCCESSFUL_GET_PROXY_TICKET, servicePrincipal, ticketGrantingTicket, proxyTicketId);
         return proxyTicketId;
     }
 
 
     /**
      * Verifies the existence of a user.
      * @param userId
      *            The username to verify.
      * @param servicePrincipal
      *            The principal of the requesting service.
      * @return true if the user exists, else false.
      * @throws AuthorizationException
      *             If the requesting service is not allowed to perform the
      *             operation.
      * @throws IllegalInputException
      *             If <code>userId</code> or <code>servicePrincipal</code>
      *             is null or empty.
      * @throws InoperableStateException
      *             If the controller is not ready to use.
      * @throws DirectoryUnavailableException
      *             If the directory for the user is not available.
      */
     public static boolean verifyUserExistence(final String userId, final String servicePrincipal)
     throws AuthorizationException, IllegalInputException,
     InoperableStateException, DirectoryUnavailableException {
 
         /* Check controller state */
         if (!ready) { throw new InoperableStateException(NOT_READY); }
 
         /* Validate arguments */
         if (userId == null || userId.equals("")) { throw new IllegalInputException("'userId' must be non-empty string."); }
         if (servicePrincipal == null || servicePrincipal.equals("")) { throw new IllegalInputException("'servicePrincipal' must be non-empty string."); }
 
         String org = null;
         if (userId.indexOf("@") != -1) {
             org = userId.substring(userId.indexOf("@") + 1, userId.length());
         }
         if (org == null) {
             throw new AuthorizationException("Userorg is unknown"); 
         }
 
         /* Authorization */
         authorizationCheck(servicePrincipal, new String[] {}, VERIFY_USER_EXISTENCE_OPER, org);
 
         /* Verify user (call DM) */
         final boolean userExistence;
         try {
             userExistence = directoryManager.userExists(null, userId);
         } catch (BackendException e) {
             messageLogger.logWarn("BackendException caught", e);
             throw new DirectoryUnavailableException();
         }
 
         /* Log */
         final String resultString;
         if (userExistence) {
             resultString = "was verified.";
         } else {
             resultString = "does not exist.";
         }
         accessLogger.logService(AccessStatusType.SUCCESSFUL_VERIFY_USER, servicePrincipal, null, null);
         messageLogger.logInfo("User verification (by " + servicePrincipal + "): " + userId + " " + resultString);
 
         return userExistence;
     }
 
 
     /**
      * Sets config for a module. A supplied configuration is transferred to the
      * correct module. When all modules have received their config, the
      * controller's status becomes ready.
      * @param module
      *            Name of the module to set config for.
      * @param properties
      *            The configuration to transfer to the module.
      * @see ConfigurationManager#MODULE_AM
      * @see ConfigurationManager#MODULE_DM
      * @see ConfigurationManager#MODULE_SM
      * @see ConfigurationManager#MODULE_WEB
      */
     public static synchronized void setConfig(final String module, final Properties properties) {
 
         if (module.equals(ConfigurationManager.MODULE_AM)) {
             if (authzManager != null) {
                 authzManager.setConfig(properties);
                 amReady = true;
                 messageLogger.logInfo("Config set for AM.");
             } else {
                 messageLogger.logCritical("Received authorization config before AM is initialized.");
             }
         } else if (module.equals(ConfigurationManager.MODULE_DM)) {
             if (directoryManager != null) {
                 directoryManager.setConfig(properties);
                 dmReady = true;
                 messageLogger.logInfo("Config set for DM.");
             } else {
                 messageLogger.logCritical("Received directory config before DM is initialized.");
             }
         } else if (module.equals(ConfigurationManager.MODULE_SM)) {
             if (store != null) {
                 try {
                     store.setConfig(properties);
                     smReady = true;
                     messageLogger.logInfo("Config set for SM.");
                 } catch (MoriaStoreConfigurationException msce) {
                     smReady = false;
                     messageLogger.logCritical("Unable to set config for SM", msce);
                 }
             } else {
                 messageLogger.logCritical("Received store config before SM is initialized.");
             }
         } else if (module.equals(ConfigurationManager.MODULE_WEB)) {
             if (servletContext != null) {
                 servletContext.setAttribute("no.feide.moria.web.config", properties);
                 messageLogger.logInfo("Config set for WEB.");
             } else {
                 messageLogger.logCritical("Received web config before the servlet context is available.");
             }
         }
 
         /* If all modules are ready, the controller is ready */
         if (isInitialized.booleanValue() && amReady && dmReady && smReady) {
             ready = true;
             messageLogger.logInfo("All config is set. Moria is READY for use.");
         }
     }
 
 
     /**
      * Starts the controller. The controller expects to be started from a web
      * application. The supplied ServletContext will be used to transfer config
      * from the configuration manager to the servlets.
      * @param sc
      *            The servletContext from the caller.
      * @throws InoperableStateException
      *             if Moria is not ready for use.
      */
     public static void initController(final ServletContext sc)
     throws InoperableStateException {
 
         if (isInitialized.booleanValue()) {
             messageLogger.logDebug("Controller has already been initialized");
             return;
         }
 
         /* Store servlet context for web module configuration. */
         servletContext = sc;
         init();
 
         messageLogger.logInfo("Controller initialized");
     }
 
 
     /**
      * Stops the controller.
      */
     public static void stopController() {
 
         if (!isInitialized.booleanValue()) {
             messageLogger = new MessageLogger(MoriaController.class);
             messageLogger.logInfo("Attempt to stop uninitialized controller, ignoring");
         } else {
             stop();
             messageLogger.logInfo("Controller stopped");
         }
     }
 
 
     /**
      * Validates a URL. Uses blacklist to indicate whether the URL should be
      * accepted or not.
      * @param url
      *            The URL to validate.
      * @return true if the URL is valid, else false.
      * @throws IllegalArgumentException if url is null or empty.
      */
     static boolean isLegalURL(final String url) {
 
         // TODO: Implement a more complete URL validator
 
         if (url == null || url.equals("")) { throw new IllegalArgumentException("'url' must be a non-empty string."); }
 
         final String[] illegal = new String[] {"\n", "\r"};
 
         /* Protocol */
         if (url.indexOf("http://") != 0 && url.indexOf("https://") != 0) { return false; }
 
         /* Illegal characters */
         for (int i = 0; i < illegal.length; i++) {
             if (url.indexOf(illegal[i]) != -1) {
                 messageLogger.logDebug("URL is invalid. Contains '" + illegal[i] + "'. " + url);
                 return false;
             }
         }
 
         return true;
     }
 
 
     /**
      * Returns the service configuration for the service that created the
      * authentication attempt.
      * @param loginTicketId
      *            The login ticket associated with the authentication attempt.
      *            Cannot be <code>null</code> or an empty string.
      * @return A HashMap containing service configuration.
      * @throws UnknownTicketException
      *             If the ticket does not exist in the store, if the ticket is
      *             invalid, or if the ticket does not correspond to a service.
      * @throws InoperableStateException
      *             If the controller or the store is not ready to use.
      * @throws IllegalInputException
      *             If <code>loginTicketId</code> is <code>null</code> or an
      *             empty string.
      */
     public static HashMap getServiceProperties(final String loginTicketId)
     throws UnknownTicketException, InoperableStateException,
     IllegalInputException {
 
         // Sanity checks.
         if (!ready)
             throw new InoperableStateException(NOT_READY);
         if (loginTicketId == null || loginTicketId.equals(""))
             throw new IllegalInputException("Login ticket ID must be a non-empty string");
 
         // Retrieve authentication attempt.
         final MoriaAuthnAttempt authnAttempt;
         try {
             authnAttempt = store.getAuthnAttempt(loginTicketId, true, null);
         } catch (NonExistentTicketException e) {
             accessLogger.logUser(AccessStatusType.NONEXISTENT_LOGIN_TICKET, null, null, loginTicketId, null);
             messageLogger.logDebug(CAUGHT_NONEXISTENT_TICKET, e);
             throw new UnknownTicketException(NONEXISTENT_TICKET);
         } catch (InvalidTicketException e) {
             accessLogger.logUser(AccessStatusType.INVALID_LOGIN_TICKET, null, null, loginTicketId, null);
             messageLogger.logWarn(CAUGHT_INVALID_TICKET, e);
             throw new UnknownTicketException(NONEXISTENT_TICKET);
         } catch (MoriaStoreException e) {
             messageLogger.logCritical(CAUGHT_STORE, e);
             throw new InoperableStateException(STORE_DOWN);
         }
 
         // Return service properties.
         try {
             return authzManager.getServiceProperties(authnAttempt.getServicePrincipal());
         } catch (UnknownServicePrincipalException e) {
             messageLogger.logWarn("Service '" + authnAttempt.getServicePrincipal() + "' is unknown", loginTicketId, e);
             throw new UnknownTicketException("Ticket '" + loginTicketId + "' does not correspond to a known service");
         }
     }
 
 
     /**
      * Gets the security level of an authentication attempt.
      * @param loginTicketId
      *            The ticket associated with the authentication attempt.
      * @return int describing the security level for the requested attributes in
      *         The authentication attempt.
      * @throws UnknownTicketException
      *             If the ticket does not exist, is invalid, or is not 
      *             associated with a service.
      * @throws InoperableStateException
      *             If Moria is not usable.
      * @throws IllegalArgumentException
      *             If loginTicketId is null or empty.
      */
     public static int getSecLevel(final String loginTicketId)
     throws UnknownTicketException, InoperableStateException {
 
         /* Check controller state */
         if (!ready) { throw new InoperableStateException(NOT_READY); }
 
         /* Validate argument */
         if (loginTicketId == null || loginTicketId.equals("")) { throw new IllegalArgumentException("'loginTicketId' must be a non-empty string, was: " + loginTicketId); }
 
         final MoriaAuthnAttempt authnAttempt;
         try {
             authnAttempt = store.getAuthnAttempt(loginTicketId, true, null);
         } catch (NonExistentTicketException e) {
             accessLogger.logUser(AccessStatusType.NONEXISTENT_LOGIN_TICKET, null, null, loginTicketId, null);
             messageLogger.logInfo(CAUGHT_NONEXISTENT_TICKET, e);
             throw new UnknownTicketException(NONEXISTENT_TICKET);
         } catch (InvalidTicketException e) {
             accessLogger.logUser(AccessStatusType.INVALID_LOGIN_TICKET, null, null, loginTicketId, null);
             throw new UnknownTicketException(NONEXISTENT_TICKET);
         } catch (MoriaStoreException e) {
             messageLogger.logCritical(CAUGHT_STORE, e);
             throw new InoperableStateException(STORE_DOWN);
         }
 
         try {
             return authzManager.getSecLevel(authnAttempt.getServicePrincipal(), authnAttempt.getRequestedAttributes());
         } catch (UnknownServicePrincipalException e) {
             messageLogger.logWarn("UnknownServicePrincipalException caught, has the service been removed from the authorization database?", e);
             throw new UnknownTicketException("Ticket is no longer connected to a service.");
         } catch (UnknownAttributeException e) {
             messageLogger.logWarn("UnknownAttributeException caught, has the attribute been removed from the authorization database?", e);
             throw new InoperableStateException("The authentication attempt is unusable.");
         }
     }
 
 
     /**
      * Invalidates an SSO ticket. After the invalidation, the ticket cannot be
      * used any more.
      * @param ssoTicketId
      *            The ticket to be invalidated.
      * @throws IllegalInputException
      *             If <code>ssoTicketId</code> is null or empty.
      * @throws InoperableStateException
      *             If Moria is not ready to use.
      */
     public static void invalidateSSOTicket(final String ssoTicketId)
     throws IllegalInputException, InoperableStateException {
 
         /* Check controller state */
         if (!ready) { throw new InoperableStateException(NOT_READY); }
 
         /* Validate argument */
         if (ssoTicketId == null || ssoTicketId.equals("")) { throw new IllegalInputException("'ssoTicketId' must be a non-empty string."); }
 
         try {
             store.removeSSOTicket(ssoTicketId);
         } catch (NonExistentTicketException e) {
             /* We don't care, it's just removal of a ticket */
             messageLogger.logDebug(CAUGHT_NONEXISTENT_TICKET + ", OK since we tried to remove");
         } catch (MoriaStoreException e) {
             messageLogger.logCritical(CAUGHT_STORE, e);
             throw new InoperableStateException(STORE_DOWN);
         }
 
         accessLogger.logUser(AccessStatusType.SSO_TICKET_INVALIDATED, null, null, ssoTicketId, null);
     }
 
 
     /**
      * Creates a redirect URL for redirecting user back to web service. The URL
      * is created by concatenating the URL prefix with the service ticket and
      * the URL postfix.
      * @param serviceTicketId
      *            The service ticket to generate redirect URL for.
      * @return A <code>String</code> containing the URL.
      * @throws InoperableStateException
      *             If Moria is not ready for use.
      * @throws IllegalInputException
      *             If <code>serviceTicketId</code> is null or empty.
      * @throws UnknownTicketException
      *             If the service ticket is invalid or does not exist.
      */
     public static String getRedirectURL(final String serviceTicketId)
     throws InoperableStateException, IllegalInputException,
     UnknownTicketException {
 
         /* Check controller state */
         if (!ready) { throw new InoperableStateException(NOT_READY); }
 
         /* Validate argument */
         if (serviceTicketId == null || serviceTicketId.equals("")) { throw new IllegalInputException("serviceTicketId must be a non-empty string."); }
 
         final MoriaAuthnAttempt authnAttempt;
         try {
             authnAttempt = store.getAuthnAttempt(serviceTicketId, true, null);
         } catch (InvalidTicketException e) {
             accessLogger.logUser(AccessStatusType.INVALID_SERVICE_TICKET, null, null, serviceTicketId, null);
             messageLogger.logWarn(CAUGHT_INVALID_TICKET, e);
             throw new UnknownTicketException(NONEXISTENT_TICKET);
         } catch (NonExistentTicketException e) {
             accessLogger.logUser(AccessStatusType.NONEXISTENT_SERVICE_TICKET, null, null, serviceTicketId, null);
             messageLogger.logInfo(CAUGHT_NONEXISTENT_TICKET, e);
             throw new UnknownTicketException(NONEXISTENT_TICKET);
         } catch (MoriaStoreException e) {
             messageLogger.logCritical(CAUGHT_STORE, e);
             throw new InoperableStateException(STORE_DOWN);
         }
 
         return authnAttempt.getReturnURLPrefix() + serviceTicketId + authnAttempt.getReturnURLPostfix();
     }
     
     /**
      * Gets the userorg from the directoryManager.
      * 
      * @param userId 
      * 				The userId of a user.
      * @param password
      * 				The password of a user.
      * @return A <code>String</code> containing the user's organization.
      * @throws AuthenticationException
      * 				If the user's organization is not found.
      */
     private static String getUserOrg(String userId, String password) throws AuthenticationException {
         String org = directoryManager.getRealm(userId);
         if (org == null) {
             throw new AuthenticationException(); 
         }
         return org;
     }
 }
