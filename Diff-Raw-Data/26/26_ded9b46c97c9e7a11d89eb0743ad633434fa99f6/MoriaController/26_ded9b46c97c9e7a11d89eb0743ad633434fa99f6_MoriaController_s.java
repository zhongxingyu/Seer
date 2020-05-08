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
 
 import no.feide.moria.authorization.AuthorizationManager;
 import no.feide.moria.authorization.UnknownServicePrincipalException;
 import no.feide.moria.authorization.UnknownAttributeException;
 import no.feide.moria.configuration.ConfigurationManager;
 import no.feide.moria.configuration.ConfigurationManagerException;
 import no.feide.moria.store.InvalidTicketException;
 import no.feide.moria.store.MoriaAuthnAttempt;
 import no.feide.moria.store.MoriaStore;
 import no.feide.moria.store.MoriaStoreFactory;
 import no.feide.moria.store.MoriaStoreException;
 import no.feide.moria.log.AccessLogger;
 import no.feide.moria.log.AccessStatusType;
 import no.feide.moria.log.MessageLogger;
 
 import javax.servlet.ServletContext;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Properties;
 
 /**
  * @author Lars Preben S. Arnesen &lt;lars.preben.arnesen@conduct.no&gt;
  * @version $Revision$
  */
 public class MoriaController {
 
     /**
      * The single instance of the data store
      */
     private static MoriaStore store;
 
     /**
      * The single instance of the configuration manager
      */
     private static ConfigurationManager configManager;
 
     /**
      * The single instance of the authorization manager
      */
     private static AuthorizationManager authzManager;
 
     /**
      * The single instance of the directory manager
      */
     private static AuthorizationManager directoryManager;
 
     /**
      * Flag set to true if the controller has been initialized
      */
     private static Boolean isInitialized = new Boolean(false);
 
     /**
      * Flag set to true if the controller and all modules are ready
      */
     private static boolean ready = false;
 
     /**
      * Flag set to true if the authorization manager is ready
      */
     private static boolean amReady = false;
 
     /**
      * Flag set to true if the directory manager is ready
      */
     private static boolean dmReady = false;
 
     /**
      * Flag set to true if the store manager is ready
      */
     private static boolean smReady = false;
 
     /**
      * Flag set to true if the web module is ready
      */
     private static boolean webReady = false;
 
     /**
      * The servlet context for the servlets using the controller
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
 
     synchronized static void init() {
         synchronized (isInitialized) {
             // TODO: Implemented just to get the current code running
             if (isInitialized.booleanValue()) {
                 return;
             }
             isInitialized = new Boolean(true);
 
 
             try {
                 store = MoriaStoreFactory.createMoriaStore();
             } catch (MoriaStoreException e) {
                 // TODO: Log and throw exception
             }
 
             // TODO: Should use value specified on the command line, in startup servlet or something
             // like that
             if (System.getProperty("no.feide.moria.configuration.base") == null)
                 System.setProperty("no.feide.moria.configuration.base", MoriaController.class.getResource("/cm-test-valid.properties").getPath());
             if (System.getProperty("no.feide.moria.store.nodeid") == null)
                 System.setProperty("no.feide.moria.store.nodeid", "no1");
 
             /* Logging */
             accessLogger = new AccessLogger();
             messageLogger = new MessageLogger(MoriaController.class);
 
 
             /* Authorization manager */
             authzManager = new AuthorizationManager();
 
             /* Configuration manager */
             try {
                 configManager = new ConfigurationManager();
             } catch (ConfigurationManagerException e) {
                 //TODO: Handle exeption properly, should probably throw new
                 // IllegalInputException
                 System.out.println("ConfigurationManagerException caught.");
                 e.printStackTrace();
             }
         }
     }
 
     /**
      * Shut down the controller. All ready status fields are set to false;
      */
     synchronized static void stop() {
         synchronized (isInitialized) {
            if (isInitialized.booleanValue()) {
                 authzManager = null;
                 amReady = false;
                 configManager.stop();
                 configManager = null;
                 directoryManager = null;
                 dmReady = false;
                 store = null;
                 smReady = false;
                 servletContext = null;
                 ready = false;
                 isInitialized = new Boolean(false);
             }
         }
     }
 
     /* For Login Servlet */
 
     /**
      * @param loginTicket
      * @param ssoTicket
      * @return
      * @throws UnknownTicketException
      */
     public static String attemptSingleSignOn(final String loginTicket, final String ssoTicket)
             throws UnknownTicketException, InoperableStateException, IllegalInputException {
 
         // If the login ticket is invalid throw exception
         // if (!validateLoginTicket(loginTicket))
         //   throw new UnknownTicketException("Single Sign-On failed for ticket: " + loginTicket);
         // TODO: Implement
         return null;
     }
 
     /**
      * @param loginTicket
      * @param ssoTicket
      * @param userId
      * @param password
      * @return
      * @throws UnknownTicketException
      */
     public static boolean attemptLogin(final String loginTicket, final String ssoTicket, final String userId,
                                        final String password)
             throws UnknownTicketException, InoperableStateException, IllegalInputException {
         // TODO: Implement
         return false;
     }
 
     /* For Web Service */
 
     /**
      * @param attributes
      * @param returnURLPrefix
      * @param returnURLPostfix
      * @param forceInteractiveAuthentication
      * @return
      * @throws AuthorizationException
      * @throws IllegalInputException
      */
     public static String initiateAuthentication(final String[] attributes, final String returnURLPrefix,
                                                 final String returnURLPostfix, final boolean forceInteractiveAuthentication, final String servicePrincipal)
             throws AuthorizationException, IllegalInputException, InoperableStateException {
 
         if (!ready) {
            throw new IllegalStateException("Controller not initialized");
         }
 
         /* Validate parameters */
         if (servicePrincipal == null || servicePrincipal.equals("")) {
             throw new IllegalInputException("servicePrincipal cannot be null or an empty string.");
         }
         if (attributes == null) {
             throw new IllegalInputException("Attributes cannot be null.");
         }
         if (returnURLPrefix == null || returnURLPrefix.equals("")) {
             throw new IllegalInputException("URLPrefix cannot be null or an empty string.");
         }
         if (returnURLPostfix == null) {
             throw new IllegalInputException("URLPostfix cannot be null.");
         }
 
         /* Authorization */
         try {
             if (!authzManager.allowAccessTo(servicePrincipal, attributes)) {
                 accessLogger.logService(AccessStatusType.ATTRIBUTES_ACCESS_DENIED, servicePrincipal, null, null);
                 messageLogger.logInfo("Service '" + servicePrincipal
                         + "' tried to access '" + attributes + "', but have only access to '" + authzManager.getAttributes(servicePrincipal)); // TODO: Finish
                 throw new AuthorizationException("Access to the requested attributes is denied.");
             }
             if (!authzManager.allowOperations(servicePrincipal, new String[]{"InteractiveAuth"})) {
                 throw new AuthorizationException("Access to the requested operations is denied.");
             }
         } catch (UnknownServicePrincipalException e) {
             // TODO: Log event (Message log)
             throw new AuthorizationException("Authorization failed for: " + servicePrincipal);
         }
 
         /* URL validation */
         if (returnURLPrefix == null || returnURLPrefix.equals("")) {
             throw new IllegalInputException("URLPrefix cannot be null or an empty string.");
         }
         if (returnURLPostfix == null) {
             throw new IllegalInputException("URLPostfix cannot be null.");
         }
         if (!(isLegalURL(returnURLPrefix + "FakeMoriaID" + "urlPostfix"))) {
             throw new IllegalInputException("URLPrefix and URLPostfix combined does not make a valid URL.");
         }
 
         /* Create authentication attempt */
         try {
             return store.createAuthnAttempt(attributes, returnURLPrefix, returnURLPostfix, forceInteractiveAuthentication, servicePrincipal);
         } catch (MoriaStoreException e) {
             throw new InoperableStateException("Moria is unavailable, store is down");
         }
     }
 
     /**
      * @param serviceTicket
      * @param servicePrincipal
      * @return Map containing user attributes in strings or string arrays
      * @throws IllegalInputException
      */
     public static Map getUserAttributes(final String serviceTicket, final String servicePrincipal)
             throws IllegalInputException, UnknownTicketException, InoperableStateException {
         // TODO: Implement
         if (!ready) {
            throw new IllegalStateException("Controller not initialized");
         }
         return null;
     }
 
     /**
      * @param attributes
      * @param userId
      * @param password
      * @param servicePrincipal
      * @return Map containing user attributes in strings or string arrays
      * @throws AuthorizationException
      * @throws IllegalInputException
      */
     public static Map directNonInteractiveAuthentication(final String[] attributes, final String userId,
                                                          final String password, final String servicePrincipal)
             throws AuthorizationException, IllegalInputException, InoperableStateException {
         // TODO: Implement
         if (!ready) {
            throw new IllegalStateException("Controller not initialized");
         }
         return null;
     }
 
     /**
      * @param attributes
      * @param proxyTicket
      * @param servicePrincipal
      * @return Map containing user attributes in strings or string arrays
      * @throws AuthorizationException
      * @throws IllegalInputException
      */
     public static Map proxyAuthentication(final String[] attributes, final String proxyTicket, final String servicePrincipal)
             throws AuthorizationException, IllegalInputException, InoperableStateException, UnknownTicketException {
         // TODO: Implement
         if (!ready) {
            throw new IllegalStateException("Controller not initialized");
         }
         return null;
     }
 
     /**
      * @param ticketGrantingTicket
      * @param proxyServicePrincipal
      * @param servicePrincipal
      * @return
      * @throws AuthorizationException
      * @throws IllegalInputException
      */
     public static String getProxyTicket(final String ticketGrantingTicket, final String proxyServicePrincipal,
                                         final String servicePrincipal)
             throws AuthorizationException, IllegalInputException, InoperableStateException, UnknownTicketException {
         // TODO: Implement
         if (!ready) {
            throw new IllegalStateException("Controller not initialized");
         }
         return null;
     }
 
     /**
      * @param username
      * @param servicePrincipal
      * @return
      * @throws AuthorizationException
      * @throws IllegalInputException
      */
     public static boolean verifyUserExistence(final String username, final String servicePrincipal)
             throws AuthorizationException, IllegalInputException, InoperableStateException {
         // TOOD: Implement
         if (!ready) {
            throw new IllegalStateException("Controller not initialized");
         }
         return false;
     }
 
     /* For Configuration Manager */
 
     /**
      * @param properties
      * @param module
      */
     synchronized public static void setConfig(final String module, final Properties properties) {
         //init();
         if (module.equals(ConfigurationManager.MODULE_AM)) {
             if (authzManager != null) {
                 authzManager.setConfig(properties);
                 amReady = true;
             }
         } else if (module.equals(ConfigurationManager.MODULE_DM)) {
             if (directoryManager != null) {
                 directoryManager.setConfig(properties);
                 dmReady = true;
             }
         } else if (module.equals(ConfigurationManager.MODULE_SM)) {
             if (store != null) {
                 store.setConfig(properties);
                 smReady = true;
             }
         } else if (module.equals(ConfigurationManager.MODULE_WEB)) {
             if (servletContext != null) {
                 servletContext.setAttribute("config", properties);
                 webReady = true;
             } else {
                 // TODO: Log event
                 // MessageLogger.logCritical("Servlet context not set. Config cannot be updated.");
             }
         }
 
         /* If all modules are ready, the controller is ready */
         if (isInitialized.booleanValue() && amReady && dmReady && smReady && webReady) {
             ready = true;
         }
 
     }
 
     /**
      * Start the controller. The controller is supposed to be started from a servlet. The supplied
      * ServletContext can be used to transfer config from the configuration manager to the servlets.
      *
      * @param sc the servletContext from the caller
      */
     public static void initController(ServletContext sc) {
         servletContext = sc;
         init();
         // TODO: Finish implementation, the init() method will change
     }
 
     /**
      * Validate URL. Uses blacklist to indicate whether the URL should be accepted or not.
      *
      * @param url the URL to validate
      * @return true if the URL is valid, else false
      */
     static boolean isLegalURL(String url) {
         // TODO: Implement a more complete URL validator
 
         if (url == null || url.equals("")) {
             throw new IllegalArgumentException("url must be a non-empty string.");
         }
 
         String[] illegal = new String[]{"\n", "\r"};
 
         /* Protocol */
         if (url.indexOf("http://") != 0 && url.indexOf("https://") != 0) return false;
 
         /* Illegal characters */
         for (int i = 0; i < illegal.length; i++) {
             if (url.indexOf(illegal[i]) != -1) {
                 System.out.println("Contains: " + illegal[i]);
                 return false;
             }
         }
 
         return true;
     }
 
     /**
      * Returns the service properties for an authentication attempt.
      *
      * @param loginTicketId
      * @return a HashMap with service properties
      * @throws UnknownTicketException if the ticket does not point to a authentication attempt
      */
     public static HashMap getServiceProperties(String loginTicketId)
             throws UnknownTicketException, InoperableStateException, IllegalInputException {
 
         if (loginTicketId == null || loginTicketId.equals("")) {
             throw new IllegalInputException("loginTicketId must be a non-empty string");
         }
 
         if (!ready) {
            throw new IllegalStateException("Controller not initialized");
         }
         /* Validate arguments */
         if (loginTicketId == null || loginTicketId.equals("")) {
             throw new IllegalArgumentException("loginTicketId must be a non-empty string, was: " + loginTicketId);
         }
 
         MoriaAuthnAttempt authnAttempt;
         try {
             authnAttempt = store.getAuthnAttempt(loginTicketId, true);
         } catch (InvalidTicketException e) {
             // TODO: Log
             throw new UnknownTicketException("Ticket does not exist");
         } catch (MoriaStoreException e) {
             // TODO: Log
             throw new InoperableStateException("Moria is unavailable, store is down");
         }
 
         try {
             return authzManager.getServiceProperties(authnAttempt.getServicePrincipal());
         } catch (UnknownServicePrincipalException e) {
             // TODO: Log loginTicketId points to a non-existing service (unlikely to happen)
             throw new UnknownTicketException("Ticket is no longer connected to a service.");
         }
     }
 
     /**
      * Get the seclevel for an authentication attempt.
      *
      * @param loginTicketId the ticket associated with
      * @return int describing the security level for the requested attributes in the authentication attempt
      * @throws UnknownTicketException if the ticket does is invalid
      */
     public static int getSecLevel(String loginTicketId)
             throws UnknownTicketException, InoperableStateException, AuthorizationException {
         if (!ready) {
            throw new IllegalStateException("Controller not initialized");
         }
         /* Validate argument */
         if (loginTicketId == null || loginTicketId.equals("")) {
             throw new IllegalArgumentException("loginTicketId must be a non-empty string, was: " + loginTicketId);
         }
 
         MoriaAuthnAttempt authnAttempt;
         try {
             authnAttempt = store.getAuthnAttempt(loginTicketId, true);
         } catch (InvalidTicketException e) {
             // TODO: Log
             throw new UnknownTicketException("Ticket does not exist.");
         } catch (MoriaStoreException e) {
             // TODO: Log
             throw new InoperableStateException("Moria is unavailable, store is down");
         }
 
         try {
             return authzManager.getSecLevel(authnAttempt.getServicePrincipal(), authnAttempt.getRequestedAttributes());
         } catch (UnknownServicePrincipalException e) {
             // TODO: Log loginticket points to a non-existing service (unlikely to happen)
             throw new UnknownTicketException("Ticket is no longer connected to a service.");
         } catch (UnknownAttributeException e) {
             // TODO: Log, should never happen since getSeclevel is used on login page, attrs allready authorized
             throw new AuthorizationException("Attribute does not exist");
         }
     }
 }
