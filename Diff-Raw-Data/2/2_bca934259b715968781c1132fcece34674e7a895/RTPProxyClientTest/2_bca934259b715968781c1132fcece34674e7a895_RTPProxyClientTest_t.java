 package org.vtlabs.rtpproxy.test.integration;
 
 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 import java.util.UUID;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.vtlabs.rtpproxy.client.NoServerAvailableException;
 import org.vtlabs.rtpproxy.client.RTPProxyClient;
 import org.vtlabs.rtpproxy.client.RTPProxyClientConfig;
 import org.vtlabs.rtpproxy.client.RTPProxyClientConfigurator;
 import org.vtlabs.rtpproxy.client.RTPProxyClientListener;
 import org.vtlabs.rtpproxy.client.RTPProxySession;
 import static org.junit.Assert.*;
 
 /**
  *
  * @author mhack
  */
 public class RTPProxyClientTest implements RTPProxyClientListener {
 
     private static Logger log;
     private RTPProxyClient client;
     private RTPProxyClientConfig config;
     private RTPProxySession session;
     private boolean wasCreateTimeout;
     private boolean wasUpdateTimeout;
     private boolean wasCreateFailed;
     private boolean wasUpdateFailed;
 
     @BeforeClass
     public static void initClass() {
         String configFile = "conf/log4j.properties";
         System.setProperty("log4j.configuration", "file:" + configFile);
         log = LoggerFactory.getLogger(RTPProxyClientTest.class);
     }
 
     /**
     * Test the creation of a session using a RTPProxy server bound to
      * localhost:22222.
      *
      * @throws java.lang.Exception
      */
     @Test
     public void createSession() throws Exception {
         config = RTPProxyClientConfigurator.load("127.0.0.1:22222");
         client = new RTPProxyClient(config);
         String sessionID = UUID.randomUUID().toString();
         Object appData = new Object();
 
         log.info("Creating new session. SessionID = " + sessionID);
         client.createSession(sessionID, appData, this);
 
         synchronized (this) {
             // We'll be notified when some callback method was called.
             wait();
         }
 
         client.terminate();
 
         assertNotNull("Session wasn't created", session);
 
         assertNotNull("Callee media address wasn't created",
                 session.getCalleeMediaAddress());
 
         assertNotNull("Caller media address wasn't created",
                 session.getCallerMediaAddress());
     }
 
     /**
      * Test the creation timeout callback trying to create a session using an
      * server that isn't running (remember to don't start rtpproxy in this
      * port ;)
      *
      * @throws java.lang.Exception
      */
     @Test
     public void createSessionTimeout() throws Exception {
         config = RTPProxyClientConfigurator.load("127.0.0.1:22200");
         client = new RTPProxyClient(config);
         String sessionID = UUID.randomUUID().toString();
         Object appData = new Object();
 
         log.info("Creating new session. SessionID = " + sessionID);
         client.createSession(sessionID, appData, this);
 
         synchronized (this) {
             // We'll be notified when some callback method was called.
             wait();
         }
 
         client.terminate();
 
         assertTrue("Listener didn't receive timeout callback",
                 wasCreateTimeout);
     }
 
     //
     // Callback methods implemented from RTPProxyClientListener
     //
     public void sessionCreated(RTPProxySession session, Object appData) {
         log.debug("Session created: " + session);
         this.session = session;
 
         try {
             log.info("Updating session " + session);
             client.updateSession(session, appData, this);
 
         } catch (NoServerAvailableException noServerEx) {
             log.error("No server available on updating session.", noServerEx);
         }
     }
 
     public void sessionUpdated(RTPProxySession session, Object appData) {
         log.debug("Session creation completed: " + session);
         wakeup();
     }
 
     public void createSessionTimeout(String sessionID, Object appData) {
         log.debug("Session creation timeout: " + sessionID);
         wasCreateTimeout = true;
         wakeup();
     }
 
     public void updateSessionTimeout(RTPProxySession session, Object appData) {
         log.debug("Session update timeout: " + session);
         wasUpdateTimeout = true;
         wakeup();
     }
 
     public void createSessionFailed(String sessionID, Object appData,
             Throwable t) {
         log.debug("Session creation failed: " + sessionID, t);
         wasCreateFailed = true;
         wakeup();
     }
 
     public void updateSessionFailed(RTPProxySession session, Object appData,
             Throwable t) {
         log.debug("Session update failed: " + session, t);
         wasUpdateFailed = true;
         wakeup();
     }
 
     private void wakeup() {
         synchronized (this) {
             notify();
         }
     }
 }
