 /*
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 package org.apache.sshd.server.session;
 
 import java.io.IOException;
 import java.security.KeyPair;
 import java.util.List;
 import java.util.concurrent.Future;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.TimeUnit;
 
 import org.apache.mina.core.session.IoSession;
 import org.apache.sshd.common.*;
 import org.apache.sshd.common.service.ConnectionServiceProvider;
 import org.apache.sshd.common.service.ServiceProvider;
 import org.apache.sshd.common.service.ServiceProviderFactory;
 import org.apache.sshd.common.session.AbstractSession;
 import org.apache.sshd.common.util.Buffer;
 import org.apache.sshd.server.ServerFactoryManager;
 
 /**
  *
  * TODO: handle key re-exchange
  *          key re-exchange should be performed after each gigabyte of transferred data
  *          or one hour time connection (see RFC4253, section 9)
  *
  * TODO: better use of SSH_MSG_DISCONNECT and disconnect error codes
  *
  * TODO Add javadoc
  *
  * @author <a href="mailto:dev@mina.apache.org">Apache MINA SSHD Project</a>
  */
 public class ServerSession extends AbstractSession<ServiceProvider> {
 
     private Future serviceRequestTimerFuture;
     private Future idleTimerFuture;
     private int serviceRequestTimeout = 10 * 60 * 1000; // 10 minutes in milliseconds
     private int idleTimeout = 10 * 60 * 1000; // 10 minutes in milliseconds
 
     public ServerSession(FactoryManager server, IoSession ioSession) throws Exception {
         super(server, ioSession);
         serviceRequestTimeout = getIntProperty(ServerFactoryManager.AUTH_TIMEOUT, serviceRequestTimeout);
         idleTimeout = getIntProperty(ServerFactoryManager.IDLE_TIMEOUT, idleTimeout); // XXX: Should this be a new timer?
         log.info("Session created from {}", ioSession.getRemoteAddress());
         sendServerIdentification();
         sendKexInit();
     }
 
     public String getNegociated(int index) {
         return negociated[index];
     }
 
     public KeyExchange getKex() {
         return kex;
     }
 
     public byte [] getSessionId() {
       return sessionId;
     }
 
     public ServerFactoryManager getServerFactoryManager() {
         return (ServerFactoryManager) factoryManager;
     }
 
     public ScheduledExecutorService getScheduledExecutorService() {
         return getServerFactoryManager().getScheduledExecutorService();
     }
 
     protected void handleMessage(Buffer buffer) throws Exception {
         SshConstants.Message cmd = buffer.getCommand();
         log.debug("Received packet {}", cmd);
         switch (cmd) {
             case SSH_MSG_DISCONNECT: {
                 int code = buffer.getInt();
                 String msg = buffer.getString();
                 log.debug("Received SSH_MSG_DISCONNECT (reason={}, msg={})", code, msg);
                 close(true);
                 break;
             }
             case SSH_MSG_UNIMPLEMENTED: {
                 int code = buffer.getInt();
                 log.debug("Received SSH_MSG_UNIMPLEMENTED #{}", code);
                 break;
             }
             case SSH_MSG_DEBUG: {
                 boolean display = buffer.getBoolean();
                 String msg = buffer.getString();
                 log.debug("Received SSH_MSG_DEBUG (display={}) '{}'", display, msg);
                 break;
             }
             case SSH_MSG_IGNORE:
                 log.debug("Received SSH_MSG_IGNORE");
                 break;
             default:
                 switch (getState()) {
                     case ReceiveKexInit:
                         if (cmd != SshConstants.Message.SSH_MSG_KEXINIT) {
                             log.warn("Ignoring command " + cmd + " while waiting for " + SshConstants.Message.SSH_MSG_KEXINIT);
                             break;
                         }
                         log.debug("Received SSH_MSG_KEXINIT");
                         receiveKexInit(buffer);
                         negociate();
                         kex = NamedFactory.Utils.create(factoryManager.getKeyExchangeFactories(), negociated[SshConstants.PROPOSAL_KEX_ALGS]);
                         kex.init(this, serverVersion.getBytes(), clientVersion.getBytes(), I_S, I_C);
                         setState(State.Kex);
                         break;
                     case Kex:
                         buffer.rpos(buffer.rpos() - 1);
                         if (kex.next(buffer)) {
                             sendNewKeys();
                             setState(State.ReceiveNewKeys);
                         }
                         break;
                     case ReceiveNewKeys:
                         if (cmd != SshConstants.Message.SSH_MSG_NEWKEYS) {
                             disconnect(SshConstants.SSH2_DISCONNECT_PROTOCOL_ERROR, "Protocol error: expected packet " + SshConstants.Message.SSH_MSG_NEWKEYS + ", got " + cmd);
                             return;
                         }
                         log.debug("Received SSH_MSG_NEWKEYS");
                         receiveNewKeys(true);
                         scheduleServiceRequestTimer();
                         setState(State.WaitForServiceRequest);
                         break;
                     case WaitForServiceRequest:
                         if (cmd != SshConstants.Message.SSH_MSG_SERVICE_REQUEST) {
                             log.debug("Expecting a {}, but received {}", SshConstants.Message.SSH_MSG_SERVICE_REQUEST, cmd);
                             notImplemented();
                         } else {
                             unscheduleServiceRequestTimer();
                             String serviceName = buffer.getString();
                             log.debug("Received SSH_MSG_SERVICE_REQUEST '{}'", serviceName);
                             ServiceProvider serviceProvider = createService(serviceName, false, null);
                             if (serviceProvider == null) {
                                 log.debug("Service {} rejected", serviceName);
                                 disconnect(SshConstants.SSH2_DISCONNECT_SERVICE_NOT_AVAILABLE, "Bad service request: " + serviceName);
                                 break;
                             }
                             log.debug("Accepted service {}", serviceName);
                             Buffer response = createBuffer(SshConstants.Message.SSH_MSG_SERVICE_ACCEPT, 0);
                             response.putString(serviceName);
                             writePacket(response);
                             // install the service _after_ the accept message has been sent - not yet authed.
                             startService(serviceProvider, false, null);
                             setState(State.Running);
                         }
                         break;
                     case Running:
                         unscheduleIdleTimer();
                         running(cmd, buffer);
                         scheduleIdleTimer();
                         break;
                     default:
                         throw new IllegalStateException("Unsupported state: " + getState());
                 }
         }
     }
 
     public ServiceProvider createService(String serviceName, boolean authenticated, String username) {
         List<ServiceProviderFactory> serviceProviderFactories = getServerFactoryManager().getServiceProviderFactories();
         ServiceProviderFactory serviceProviderFactory = ServiceProviderFactory.find(serviceProviderFactories, serviceName);
         if (serviceProviderFactory == null) {
             log.debug("Service {} not found", serviceName);
             return null; // not found
         }
         ServiceProvider serviceProvider = serviceProviderFactory.create(this, lock, authenticated, username);
         // serviceProvider will be null if factory.create chooses to reject it.
         log.debug("Factory for service {} created provider {}", serviceName, serviceProvider);
         return serviceProvider;
     }
 
     public void startService(ServiceProvider serviceProvider, boolean authenticated, String username) {
         this.currentService = serviceProvider;
         this.authed = authenticated;
         this.username = username;
         serviceProvider.start();
     }
 
     private void running(SshConstants.Message cmd, Buffer buffer) throws Exception {
         switch (cmd) {
             case SSH_MSG_KEXINIT:
                 receiveKexInit(buffer);
                 sendKexInit();
                 negociate();
                 kex = NamedFactory.Utils.create(factoryManager.getKeyExchangeFactories(), negociated[SshConstants.PROPOSAL_KEX_ALGS]);
                 kex.init(this, serverVersion.getBytes(), clientVersion.getBytes(), I_S, I_C);
                 break;
             case SSH_MSG_KEXDH_INIT:
                 buffer.rpos(buffer.rpos() - 1);
                 if (kex.next(buffer)) {
                     sendNewKeys();
                 }
                 break;
             case SSH_MSG_NEWKEYS:
                 receiveNewKeys(true);
                 break;
             default:
                 this.currentService.process(cmd, buffer);
         }
     }
 
     private void scheduleIdleTimer() {
         if (idleTimeout < 1) {
             // A timeout less than one means there is no timeout.
             return;
         }
         Runnable idleTimerTask = new Runnable() {
             public void run() {
                 try {
                     processIdleTimer();
                 } catch (IOException e) {
                     // Ignore
                 }
             }
         };
         idleTimerFuture = getScheduledExecutorService().schedule(idleTimerTask, idleTimeout, TimeUnit.MILLISECONDS);
     }
 
     private void unscheduleIdleTimer() {
         if (idleTimerFuture != null) {
             idleTimerFuture.cancel(false);
             idleTimerFuture = null;
         }
     }
 
     private void processIdleTimer() throws IOException {
         disconnect(SshConstants.SSH2_DISCONNECT_PROTOCOL_ERROR, "User idle has timed out after " + idleTimeout + "ms.");
     }
 
     private void sendServerIdentification() {
         if (getFactoryManager().getProperties() != null && getFactoryManager().getProperties().get(ServerFactoryManager.SERVER_IDENTIFICATION) != null) {
             serverVersion = "SSH-2.0-" + getFactoryManager().getProperties().get(ServerFactoryManager.SERVER_IDENTIFICATION);
         } else {
             serverVersion = "SSH-2.0-" + getFactoryManager().getVersion();
         }
         sendIdentification(serverVersion);
     }
 
     private void sendKexInit() throws IOException {
         serverProposal = createProposal(factoryManager.getKeyPairProvider().getKeyTypes());
         I_S = sendKexInit(serverProposal);
     }
 
     protected boolean readIdentification(Buffer buffer) throws IOException {
         clientVersion = doReadIdentification(buffer);
         if (clientVersion == null) {
             return false;
         }
         log.debug("Client version string: {}", clientVersion);
         if (!clientVersion.startsWith("SSH-2.0-")) {
             throw new SshException(SshConstants.SSH2_DISCONNECT_PROTOCOL_VERSION_NOT_SUPPORTED,
                                    "Unsupported protocol version: " + clientVersion);
         }
         return true;
     }
 
     private void receiveKexInit(Buffer buffer) throws IOException {
         clientProposal = new String[SshConstants.PROPOSAL_MAX];
         I_C = receiveKexInit(buffer, clientProposal);
     }
 
     public KeyPair getHostKey() {
         return factoryManager.getKeyPairProvider().loadKey(negociated[SshConstants.PROPOSAL_SERVER_HOST_KEY_ALGS]);
     }
 
     private void scheduleServiceRequestTimer() {
         Runnable authTimerTask = new Runnable() {
             public void run() {
                 try {
                     disconnect(SshConstants.SSH2_DISCONNECT_PROTOCOL_ERROR, "time out");
                 } catch (IOException e) {
                     // Ignore
                 }
             }
         };
         serviceRequestTimerFuture = getScheduledExecutorService().schedule(authTimerTask, serviceRequestTimeout, TimeUnit.MILLISECONDS);
     }
 
     private void unscheduleServiceRequestTimer() {
         if (serviceRequestTimerFuture != null) {
             serviceRequestTimerFuture.cancel(false);
             serviceRequestTimerFuture = null;
         }
     }
 
     public String initAgentForward() throws IOException {
         return ((ConnectionServiceProvider)currentService).initAgentForward();
     }
 
     public String createX11Display(boolean singleConnection, String authenticationProtocol, String authenticationCookie, int screen) throws IOException {
         return findService(ConnectionServiceProvider.class).createX11Display(singleConnection, authenticationProtocol, authenticationCookie, screen);
     }
 
 	/**
 	 * Returns the session id.
 	 * 
 	 * @return The session id.
 	 */
 	public long getId() {
 		return ioSession.getId();
 	}
 }
