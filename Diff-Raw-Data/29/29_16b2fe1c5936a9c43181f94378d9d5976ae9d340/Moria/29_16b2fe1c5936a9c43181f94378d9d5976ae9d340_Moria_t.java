 package no.feide.mellon;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.rmi.RemoteException;
 import java.util.logging.Logger;
 import java.util.HashMap;
 import javax.xml.rpc.Stub;
 import no.feide.moria.service.*;
 
 /**
  * Represents the interface to Moria, hiding the internals of the
  * Mellon-Moria communication.
  * @author Cato Olsen
  */
 public class Moria {
     
     /** Used for logging. */
     private static Logger log = Logger.getLogger(Moria.class.toString());
     
     /** Singleton self. */
     private static Moria me;
     
     /** The JAX-RPC Moria interface proxy stub. */
     private Stub stub;
     
     
     /**
      * Private constructor. Will prepare SSL and JAX-RPC stub.
      */
     private Moria() {
         log.finer("Moria()");
         
         // Fix some properties.
	String s = System.getProperty("no.feide.mellon.trustStore");
	log.config("no.feide.mellon.trustStore="+s);
        System.setProperty("javax.net.ssl.trustStore", s);
	s = System.getProperty("no.feide.mellon.trustStorePassword");
	log.config("no.feide.mellon.trustStorePassword="+s);
        System.setProperty("javax.net.ssl.trustStorePassword", s);
         
         // Create service proxy and set credentials.
         stub = (Stub)(new FeideAuthentication_Impl().getAuthenticationIFPort());
	s = System.getProperty("no.feide.mellon.serviceUsername");
	log.config("no.feide.mellon.serviceUsername="+s);
        stub._setProperty(javax.xml.rpc.Stub.USERNAME_PROPERTY, s);
	s = System.getProperty("no.feide.mellon.servicePassword");
	log.config("no.feide.mellon.servicePassword="+s);
        stub._setProperty(javax.xml.rpc.Stub.PASSWORD_PROPERTY, s);
 	AuthenticationIF service = (AuthenticationIF)stub;
     }
 
     
     /**
      * Get an instance of Moria.
      * @return An instance of the Moria interface.
      * @throws MoriaException If the singleton object couldn't be constructed.
      */
     public static Moria getInstance()
     throws MoriaException {
         log.finer("getInstance()");
         
         // Return singleton, creating it if necessary.
         if (me == null)
             me = new Moria();
         return me;
     }
     
     
     /**
      * Wrapper for same JAX-RPC stub of same name.
      * @param attributes The names of requested attributes, to be returned
      *                   later throught <code>getAttributes</code>.
      * @param prefix The prefix, used to build the <code>verifySession</code>
      *               return value.
      * @param postfix The postfix, used to build the
      *                <code>verifySession</code> return value.
      * @return A Moria session descriptor.
      * @throws MoriaException If a RemoteException is caught.
      */
     public String requestSession(String[] attributes, String prefix, String postfix) 
     throws MoriaException {
         log.finer("requestSession(String[], String, String)");
         
 	AuthenticationIF service = (AuthenticationIF)stub;
         try {
             return service.requestSession(attributes, prefix, postfix);
         } catch (RemoteException e) {
             log.severe("RemoteException caught and re-thrown as MoriaException");
             throw new MoriaException("RemoteException caught", e);
         }
     }
     
     
     /**
      * Wrapper for same JAX-RPC stub of same name.
      * @param id
      * @return
      * @throws MoriaException If a RemoteException is caught.
      */
     public HashMap getAttributes(String id)
     throws MoriaException {
         log.finer("getAttributes(String)");
         
 	AuthenticationIF service = (AuthenticationIF)stub;
         try {
             return service.getAttributes(id);
         } catch (RemoteException e) {
             log.severe("RemoteException caught and re-thrown as MoriaException");
             throw new MoriaException("RemoteException caught", e);
         }
     }
 
 
     /**
      * Returns the address of the service used.
      * @return The service's endpoint address.
      */
     public String getServiceAddress() {
 	log.finer("getServiceAddress()");
 
 	return (String)stub._getProperty(Stub.ENDPOINT_ADDRESS_PROPERTY);
     }
     
     
     /**
      * A nasty hack to get a session authenticated. Will disappear without
      * notice.
      * @param id Session ID of unauthenticated session.
      * @param username
      * @param password
      * @return The URL combined of prefix, session ID and postfix. Session ID is
      *         that of an authenticated session.
      * @deprecated
      */
     public String authenticateUser(String id, String username, String password)
     throws MoriaException {
         log.finer("requestUserAuthentication(String, String, String)");
         
         AuthenticationIF service = (AuthenticationIF)stub;
         try {
             return service.requestUserAuthentication(id, username, password);
         } catch (RemoteException e) {
             log.severe("RemoteException caught and re-thrown as MoriaException");
             throw new MoriaException("RemoteException caught", e);
         }
     }
     
 
     
 }
