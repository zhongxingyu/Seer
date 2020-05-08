 package org.vtlabs.rtpproxy.client;
 
 import org.vtlabs.rtpproxy.config.RTPProxyClientConfigException;
 import org.vtlabs.rtpproxy.config.RTPProxyClientConfig;
 import java.io.IOException;
 import java.net.InetSocketAddress;
 import java.util.List;
 import java.util.concurrent.ScheduledThreadPoolExecutor;
 import org.vtlabs.rtpproxy.command.CommandTimeoutManager;
 import org.vtlabs.rtpproxy.callback.CallbackHandler;
 import org.vtlabs.rtpproxy.command.Command;
 import org.vtlabs.rtpproxy.command.DestroyCommand;
 import org.vtlabs.rtpproxy.command.UpdateCommand;
 import org.vtlabs.rtpproxy.scheduler.RTPProxyScheduler;
 import org.vtlabs.rtpproxy.scheduler.RTPProxySchedulerFactory;
 import org.vtlabs.rtpproxy.udp.DatagramListener;
 import org.vtlabs.rtpproxy.udp.DatagramService;
 
 /**
  * RTPProxy client API facade class.
  *
  * @author Marcos Hack <marcosh@voicetechnology.com.br>
  */
 public class RTPProxyClient {
 
     protected CommandTimeoutManager commandTimeout;
     protected DatagramService udpService;
     protected CallbackHandler callbackHandler;
     protected ScheduledThreadPoolExecutor executor;
     protected RTPProxyClientConfig config;
     protected RTPProxyScheduler scheduler;
     protected boolean isTerminated;
 
     public RTPProxyClient(RTPProxyClientConfig config)
             throws IOException, RTPProxyClientConfigException {
         this.config = config;
         executor = createThreadPoolExecutor(config.getPoolSize());
         commandTimeout = createCommandTimeoutManager(executor, config.getCommandTimeout());
         callbackHandler = createCallbackHandler(commandTimeout);
         udpService = createDatagraService(config.getBindPort(), callbackHandler);
         scheduler = createScheduler(config.getSchedulerName(), config.getServerList());
     }
 
     /**
      * Terminate RTPProxyClient instance and release all resources. All
      * subsequent call to client methods will throw an exception
      * RTPProxyClientTerminatedException.
      */
     public void terminate() throws RTPProxyClientTerminatedException,
             IOException {
         synchronized (this) {
             if (!isTerminated) {
                 udpService.stop();
                 commandTimeout.terminate();
                 executor.shutdownNow();
             } else {
                 throw new RTPProxyClientTerminatedException(
                         "RTPProxyClient instance is already terminated.");
             }
         }
     }
 
     public boolean isTerminated() {
         return isTerminated;
     }
 
     /**
      * Asynchronously create a new session containing the Callee media address.
      * This method doesn't pre-fill caller's address so RTPProxy server will
      * wait for caller's RTP packets before forwarding callee RTP packets to
      * him.
      * 
      * To create the Caller media address of an existing session use the method
      * updateSession().
      *
      * @param String to be used as session ID.
      * @param Application data object, will be passed as argument in the
      *        callback method, it isn't used internally.
      * @param Listener that will receive the callback events for this command.
      *        (see {@link RTPProxyClientListener} for more information about
      *        callback methods).
      */
     public void createSession(String sessionID, Object appData,
             RTPProxyClientListener listener) throws NoServerAvailableException {
 
         createSession(sessionID, null, appData, listener);
     }
 
      /**
      * Asynchronously create a new session containing the Callee media address
      * pre-filling caller's address with the given 'callerAddress'. That way the
      * RTPProxy servers doesn't need to wait caller's RTP packets to arrive
      * before start forwarding the callee's RTP packets to him.
      *
      * To create the Caller media address of an existing session use the method
      * updateSession().
      *
      * @param String to be used as session ID.
      * @param Caller's media address to be pre-filled in the new session
      * @param Application data object, will be passed as argument in the
      *        callback method, it isn't used internally.
      * @param Listener that will receive the callback events for this command.
      *        (see {@link RTPProxyClientListener} for more information about
      *        callback methods).
      */
     public void createSession(String sessionID, InetSocketAddress callerAddress,
             Object appData, RTPProxyClientListener listener)
             throws NoServerAvailableException {
 
         checkState();
 
         UpdateCommand updateCmd = new UpdateCommand(callbackHandler);
         updateCmd.setSessionID(sessionID);
         updateCmd.setCallbackListener(listener);
         updateCmd.setListener(callbackHandler);
        updateCmd.setAppData(appData);
 
         // The fromtag and totag doesn't matter since they match that used in
         // the updateSession() method below to link the callee and caller
         // session in the RTPProxy.
         updateCmd.setFromTag("fromtag");
 
         if (callerAddress != null) {
             updateCmd.setAddress(callerAddress);
         }
 
         // Get a server to handle the request and set it to the update command
         RTPProxyServer server = scheduler.getNextServer();
         updateCmd.setServer(server);
 
         sendCommand(updateCmd, server.getAddress());
     }
 
 
     /**
      * Asynchronously update an existing session to create the Caller media
      * address. This method doesn't pre-fill callee's address so RTPProxy server
      * will wait for callee's RTP packets before start forwarding caller's RTP
      * packets to him.
      *
      * @param Session to be updated.
      * @param Application data object, will be passed as argument in the
      *        callback method, it isn't used internally.
      * @param Listener that will receive the callback events for this command.
      *        (see {@link RTPProxyClientListener} for more information about
      *        callback methods).
      */
     public void updateSession(RTPProxySession session, Object appData,
             RTPProxyClientListener listener) throws NoServerAvailableException {
         updateSession(session, null, appData, listener);
     }
 
     /**
      * Asynchronously update an existing session to create the Caller media
      * address pre-filling callee's address with 'calleeAddress'. That way the
      * RTPProxy servers doesn't need to wait callee's RTP packets to arrive
      * before start forwarding caller's RTP packets to him.
      *
      * @param session to be updated
      * @param Callee's media address to be pre-filled
      * @param Application data object, will be passed as argument in the
      *        callback method, it isn't used internally.
      * @param Listener that will receive the callback events for this command.
      *        (see {@link RTPProxyClientListener} for more information about
      *        callback methods).
      */
     public void updateSession(RTPProxySession session,
             InetSocketAddress calleeAddress,
             Object appData, RTPProxyClientListener listener)
             throws NoServerAvailableException {
 
         checkState();
 
         UpdateCommand updateCmd = new UpdateCommand(session, callbackHandler);
         updateCmd.setCallbackListener(listener);
         updateCmd.setServer(session.getServer());
        updateCmd.setAppData(appData);
 
         // The fromtag and totag doesn't matter since they match that used in
         // the createSession() method above to link the callee and caller
         // session in the RTPProxy.
         updateCmd.setFromTag("totag");
         updateCmd.setToTag("fromtag");
 
         if (calleeAddress != null) {
             updateCmd.setAddress(calleeAddress);
         }
 
         sendCommand(updateCmd, session.getServer().getAddress());
     }
 
     /**
      * Asynchronously destroy the given session releasing all resources in the
      * RTPProxy server.
      *
      * @param Session to be destroyed.
      * @param Application data object, will be passed as argument in the
      *        callback method, it isn't used internally.
      * @param Listener that will receive the callback events for this command.
      *        (see {@link RTPProxyClientListener} for more information about
      *        callback methods).
      */
     public void destroySession(RTPProxySession session, Object appData,
             RTPProxyClientListener listener) throws NoServerAvailableException {
 
         checkState();
 
         DestroyCommand destroyCmd = new DestroyCommand(session, callbackHandler);
         destroyCmd.setCallbackListener(listener);
 
         // The fromtag and totag doesn't matter since they match that used in
         // the createSession() and updateSession() methods.
         destroyCmd.setFromTag("fromtag");
         destroyCmd.setToTag("totag");
 
         sendCommand(destroyCmd, session.getServer().getAddress());
     }
 
     /**
      * Add the command to the {@link CommandTimeoutManager} and send it to the
      * given server address using the {@link DatagramService}.
      */
     protected void sendCommand(Command command, InetSocketAddress serverAddr) {
         commandTimeout.addPendingCommand(command);
         udpService.send(command.getMessage(), serverAddr);
     }
 
 
     /**
      * Get RTPProxyClient configuration.
      *
      * @return Client configuration.
      */
     public RTPProxyClientConfig getConfig() {
         return config;
     }
 
     /**
      * Check if the RTPProxyClient instance is able to provide the service.
      * Basically it checks if the instance was terminated.
      *
      * @throws RTPProxyClientTerminatedException.
      */
     protected void checkState() {
         if (isTerminated) {
             throw new RTPProxyClientTerminatedException(
                     "RTPProxyClient instance is already terminated.");
         }
     }
 
     /**
      * Factory method to create a {@link CommandTimeoutManager}.
      *
      * @return
      */
     protected CommandTimeoutManager createCommandTimeoutManager(
             ScheduledThreadPoolExecutor executor, long commandTimeout) {
         return new CommandTimeoutManager(executor, commandTimeout);
     }
 
     /**
      * Factory method to create a {@link DatagramService}.
      *
      * @return
      */
     protected DatagramService createDatagraService(int bindPort,
             DatagramListener listener) throws IOException {
         return new DatagramService(bindPort, listener);
     }
 
     /**
      * Factory method to create a {@link RTPClientResponseHandler}.
      *
      * @return
      */
     protected CallbackHandler createCallbackHandler(
             CommandTimeoutManager commandManager) {
         return new CallbackHandler(commandManager);
     }
 
     /**
      * Factory method to create a {@link ScheduledThreadPoolExecutor}.
      *
      * @return
      */
     protected ScheduledThreadPoolExecutor createThreadPoolExecutor(
             int poolSize) {
         return new ScheduledThreadPoolExecutor(poolSize);
     }
 
     /**
      * Factory method to create a {@link RTPProxyScheduler}.
      * 
      * @return
      */
     protected RTPProxyScheduler createScheduler(String schedulerName,
             List<RTPProxyServer> servers) throws RTPProxyClientConfigException {
         return RTPProxySchedulerFactory.createScheduler(schedulerName, servers);
     }
 }
