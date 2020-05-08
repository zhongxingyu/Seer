 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.vtlabs.rtpproxy.client;
 
 import java.io.IOException;
 import java.net.InetSocketAddress;
 import java.util.concurrent.ScheduledThreadPoolExecutor;
 import org.junit.Before;
 import org.junit.Test;
 import org.vtlabs.rtpproxy.command.Command;
 import org.vtlabs.rtpproxy.config.RTPProxyClientConfig;
 import org.vtlabs.rtpproxy.config.RTPProxyClientConfigException;
 import org.vtlabs.rtpproxy.config.RTPProxyClientConfigurator;
 import org.vtlabs.rtpproxy.command.CommandTimeoutManager;
 import org.vtlabs.rtpproxy.command.DestroyCommand;
 import org.vtlabs.rtpproxy.command.UpdateCommand;
 import org.vtlabs.rtpproxy.mock.client.RTPProxyClientListenerMOCK;
 import org.vtlabs.rtpproxy.mock.command.CommandTimeoutManagerMOCK;
 import org.vtlabs.rtpproxy.mock.udp.DatagramServiceMOCK;
 import org.vtlabs.rtpproxy.udp.DatagramListener;
 import org.vtlabs.rtpproxy.udp.DatagramService;
 import static org.junit.Assert.*;
 
 /**
  *
  * @author mhack
  */
 public class RTPProxyClientTest {
 
     RTPProxyClientMOCK client;
     private RTPProxyClientConfig clientConfig;
     private RTPProxyClientListener listener;
 
     @Before
     public void setUp() throws Exception {
         clientConfig = RTPProxyClientConfigurator.load("127.0.0.1:222");
         client = new RTPProxyClientMOCK(clientConfig);
         listener = new RTPProxyClientListenerMOCK();
     }
 
     @Test
     public void createSession() throws Exception {
         String sessionID = "create_session_id";
         Object appData = new Object();
 
         client.createSession(sessionID, appData, listener);
 
         // TODO [marcoshack] assert command attributes
        UpdateCommand command = (UpdateCommand)assertCommons();
     }
 
     @Test
     public void updateSession() throws Exception {
         String sessionID = "update_session_id";
         Object appData = new Object();
         RTPProxyServer server = new RTPProxyServer();
         server.setAddress(new InetSocketAddress("127.0.0.1", 22222));
         RTPProxySession session = new RTPProxySession();
         session.setSessionID(sessionID);
         session.setServer(server);
 
         client.updateSession(session, appData, listener);
 
         // TODO [marcoshack] assert command attributes
        UpdateCommand command = (UpdateCommand)assertCommons();
     }
 
     @Test
     public void destroySession() throws Exception {
         String sessionID = "destroy_session_id";
         Object appData = new Object();
         RTPProxyServer server = new RTPProxyServer();
         server.setAddress(new InetSocketAddress("127.0.0.1", 22222));
         RTPProxySession session = new RTPProxySession();
         session.setSessionID(sessionID);
         session.setServer(server);
 
         client.destroySession(session, appData, listener);
 
         // TODO [marcoshack] assert command attributes
        DestroyCommand command = (DestroyCommand)assertCommons();
     }
 
     /**
      * Commons asserts for RTPProxyClient tests.
      *
      * @return Command created by the test method.
      */
    protected Command assertCommons() {
         CommandTimeoutManagerMOCK timeout = client.getCommandTimeoutManager();
         Command command = timeout.pendingCommand;
 
         // Asserts
         assertNotNull("Command wasn't added to timeout manager",
                 command);
 
         DatagramServiceMOCK udpService = client.getDatagramService();
         assertNotNull("Invalid sent message", udpService.sentMessage);
 
         assertEquals("Invalid callback listener", listener,
                 command.getCallbackListener());
 
         return command;
     }
 
     /**
      * RTPProxyClient MOCK class used in these tests.
      */
     protected class RTPProxyClientMOCK extends RTPProxyClient {
 
         public RTPProxyClientMOCK(RTPProxyClientConfig config)
                 throws IOException, RTPProxyClientConfigException {
             super(config);
         }
 
         public DatagramServiceMOCK getDatagramService() {
             return (DatagramServiceMOCK) udpService;
         }
 
         public CommandTimeoutManagerMOCK getCommandTimeoutManager() {
             return (CommandTimeoutManagerMOCK) commandTimeout;
         }
 
         @Override
         protected CommandTimeoutManager createCommandTimeoutManager(
                 ScheduledThreadPoolExecutor executor, long commandTimeout) {
 
             return new CommandTimeoutManagerMOCK(executor, commandTimeout);
         }
 
         @Override
         protected DatagramService createDatagraService(
                 int bindPort, DatagramListener listener) throws IOException {
 
             return new DatagramServiceMOCK(bindPort, listener);
         }
 
         @Override
         protected ScheduledThreadPoolExecutor createThreadPoolExecutor(
                 int poolSize) {
             return null;
         }
     }
 }
