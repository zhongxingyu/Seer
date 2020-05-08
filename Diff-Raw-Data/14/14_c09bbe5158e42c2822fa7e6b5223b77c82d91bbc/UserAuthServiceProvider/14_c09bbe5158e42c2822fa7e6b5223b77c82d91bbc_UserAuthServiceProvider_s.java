 package org.apache.sshd.common.service;
 
 import org.apache.mina.core.session.IoSession;
 import org.apache.sshd.common.NamedFactory;
 import org.apache.sshd.common.SshConstants;
 import org.apache.sshd.common.SshException;
 import org.apache.sshd.common.session.AbstractSession;
 import org.apache.sshd.common.util.Buffer;
 import org.apache.sshd.server.HandshakingUserAuth;
 import org.apache.sshd.server.ServerFactoryManager;
 import org.apache.sshd.server.UserAuth;
 import org.apache.sshd.server.session.ServerSession;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.Future;
 import java.util.concurrent.TimeUnit;
 
 /**
  * Created with IntelliJ IDEA.
  * User: cagney
  * Date: 19/02/13
  * Time: 11:20 AM
  * To change this template use File | Settings | File Templates.
  */
 public class UserAuthServiceProvider extends UserAuthService<ServerSession> implements ServiceProvider {
 
     UserAuthServiceProvider(ServerSession session, Object sessionLock) {
         super(session, sessionLock);
         logger.debug("Accepting user authentication request");
         maxAuthRequests = session.getIntProperty(ServerFactoryManager.MAX_AUTH_REQUESTS, maxAuthRequests);
         welcomeBanner = session.getFactoryManager().getProperties().get(ServerFactoryManager.WELCOME_BANNER);
         authTimeout = session.getIntProperty(ServerFactoryManager.AUTH_TIMEOUT, authTimeout);
         userAuthFactories = new ArrayList<NamedFactory<UserAuth>>(session.getServerFactoryManager().getUserAuthFactories());
         logger.debug("Authorized authentication methods: {}", NamedFactory.Utils.getNames(userAuthFactories));
     }
 
     public void start() {
         // nothing to do, wait for the client
         scheduleAuthTimer();
     }
 
     protected final Logger logger = LoggerFactory.getLogger(this.getClass());
     private int nbAuthRequests;
     private int maxAuthRequests = 20;
     private HandshakingUserAuth currentAuth;
     private List<NamedFactory<UserAuth>> userAuthFactories;
     private String welcomeBanner = null;
     private Future authTimerFuture;
     private int authTimeout = 10 * 60 * 1000; // 10 minutes in milliseconds
     private String currentUsername;
     private String currentServiceName;
 
     public void process(SshConstants.Message cmd, Buffer request) throws Exception {
         logger.debug("Received " + cmd);
         Boolean authed = null;
 
         switch (cmd) {
             case SSH_MSG_USERAUTH_REQUEST:
                 if (nbAuthRequests++ > maxAuthRequests) {
                     throw new SshException(SshConstants.SSH2_DISCONNECT_PROTOCOL_ERROR, "Too may authentication failures");
                 }
                 currentUsername = request.getString();
                 currentServiceName = request.getString();
                 // currentAuth == null?
                 String method = request.getString();
 
                 if (logger.isDebugEnabled()) {
                     logger.debug("Authenticating user '{}' with method '{}' and service '{}'", new Object[] {currentUsername, method, currentServiceName});
                 }
                 NamedFactory<UserAuth> factory = NamedFactory.Utils.get(userAuthFactories, method);
                 if (factory != null) {
                     UserAuth auth = factory.create();
                     try {
                         authed = auth.auth(this.session, currentUsername, currentServiceName, request);
                         if (authed == null) {
                             // authentication is still ongoing
                             logger.debug("Authentication not finished");
                             currentAuth = (HandshakingUserAuth) auth;
                             return;
                         } else {
                             logger.debug(authed ? "Authentication succeeded" : "Authentication failed");
                         }
                     } catch (Exception e) {
                         // Continue
                         authed = false;
                         logger.debug("Authentication failed: {}", e.getMessage());
                     }
                 } else {
                     logger.debug("Unsupported authentication method '{}'", method);
                     if (welcomeBanner != null) {
                         Buffer response = session.createBuffer(SshConstants.Message.SSH_MSG_USERAUTH_BANNER, 0);
                         response.putString(welcomeBanner);
                         response.putString("\n");
                         session.writePacket(response);
                     }
                     authed = false;
                 }
                 break;
             default:
                 if (currentAuth == null || !currentAuth.handles(cmd)) {
                     session.disconnect(SshConstants.SSH2_DISCONNECT_PROTOCOL_ERROR, "Protocol error: expected packet " + SshConstants.Message.SSH_MSG_USERAUTH_REQUEST + ", got " + cmd);
                     return;
                 }
                 try {
                     authed = currentAuth.next(this.session, cmd, request);
                     if (authed == null) {
                         // authentication is still ongoing
                         logger.debug("Authentication still not finished");
                         return;
                     }
                 } catch (Exception e) {
                     // failed
                     authed = false;
                     logger.debug("Authentication next failed: {}", e.getMessage());
                 }
                 // success or fail, close the authenticator.
                 currentAuth.destroy();
                 currentAuth = null;
                 break;
         }
 
         // No more handshakes now - clean up if necessary
 
         // By now, authed is true or false ...
         if (authed) {
 
             if (session.getFactoryManager().getProperties() != null) {
                 String maxSessionCountAsString = session.getFactoryManager().getProperties().get(ServerFactoryManager.MAX_CONCURRENT_SESSIONS);
                 if (maxSessionCountAsString != null) {
                     int maxSessionCount = Integer.parseInt(maxSessionCountAsString);
                     int currentSessionCount = getActiveSessionCountForUser(currentServiceName);
                     if (currentSessionCount >= maxSessionCount) {
                         session.disconnect(SshConstants.SSH2_DISCONNECT_SERVICE_NOT_AVAILABLE, "Too many concurrent connections");
                         return;
                     }
                 }
             }
 
             ServiceProvider nextService = session.createService(currentServiceName,  true, currentUsername);
             if (nextService == null) {
                 // Is this response correct?
                 session.disconnect(SshConstants.SSH2_DISCONNECT_SERVICE_NOT_AVAILABLE, "Unknown service");
                 return;
             }
 
             unscheduleAuthTimer();
            logger.info("Session {}@{} authenticated", session.getUsername(), session.getIoSession().getRemoteAddress());
             Buffer response = session.createBuffer(SshConstants.Message.SSH_MSG_USERAUTH_SUCCESS, 0);
             session.writePacket(response);
             // start the service after the auth message has been sent
             session.startService(nextService, true, currentUsername);
         } else {
             Buffer response = session.createBuffer(SshConstants.Message.SSH_MSG_USERAUTH_FAILURE, 0);
             NamedFactory.Utils.remove(userAuthFactories, "none"); // 'none' MUST NOT be listed
             response.putString(NamedFactory.Utils.getNames(userAuthFactories));
             response.putByte((byte) 0);
             session.writePacket(response);
         }
     }
 
     public void close(boolean immediately) {
         unscheduleAuthTimer();
         session.closeIoSession(immediately);
     }
 
     private void scheduleAuthTimer() {
         Runnable authTimerTask = new Runnable() {
             public void run() {
                 try {
                     session.disconnect(SshConstants.SSH2_DISCONNECT_PROTOCOL_ERROR, "User authentication has timed out");
                 } catch (IOException e) {
                     // Ignore
                 }
             }
         };
         authTimerFuture = session.getScheduledExecutorService().schedule(authTimerTask, authTimeout, TimeUnit.MILLISECONDS);
     }
 
     private void unscheduleAuthTimer() {
         if (authTimerFuture != null) {
             authTimerFuture.cancel(false);
             authTimerFuture = null;
         }
     }
 
     /**
      * Retrieve the current number of sessions active for a given username.
      *
      * @param userName The name of the user
      * @return The current number of live <code>SshSession</code> objects associated with the user
      */
     protected int getActiveSessionCountForUser(String userName) {
         int totalCount = 0;
         for (IoSession is : session.getIoSession().getService().getManagedSessions().values()) {
             ServerSession ss = (ServerSession) AbstractSession.getSession(is, true);
             if (ss != null) {
                 if (ss.getUsername() != null && ss.getUsername().equals(userName)) {
                     totalCount++;
                 }
             }
         }
         return totalCount;
     }
 
 }
