 /*
  * Copyright 2004-2012 ICEsoft Technologies Canada Corp.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the
  * License. You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an "AS
  * IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
  * express or implied. See the License for the specific language
  * governing permissions and limitations under the License.
  *
  */
 package org.icepush;
 
 import org.icepush.http.Request;
 import org.icepush.http.Response;
 import org.icepush.http.ResponseHandler;
 import org.icepush.http.Server;
 import org.icepush.http.standard.FixedXMLContentHandler;
 import org.icepush.http.standard.ResponseHandlerServer;
 import org.icepush.servlet.BrowserDispatcher;
 import org.icepush.util.Slot;
 
 import java.io.IOException;
 import java.io.Writer;
 import java.util.*;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.ConcurrentLinkedQueue;
 import java.util.concurrent.LinkedBlockingQueue;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 public class BlockingConnectionServer extends TimerTask implements Server, NotificationBroadcaster.Receiver {
     private static final Logger log = Logger.getLogger(BlockingConnectionServer.class.getName());
     private static final String[] STRINGS = new String[0];
     //Define here to avoid classloading problems after application exit
     private static final ResponseHandler NoopResponse = new NoopResponseHandler();
     private final ResponseHandler CloseResponseDup = new CloseConnectionResponseHandler("duplicate") {
         public void respond(Response response) throws Exception {
             super.respond(response);
             //revert timeout to previous value, duplicate requests can extend excessively the calculated delay
             //the duplicate requests occur during page reload or navigating away from the page and then returning back
             //before the server decides to sever the connection
             revertConnectionRecreationTimeout();
         }
     };
     private final ResponseHandler CloseResponseDown = new CloseConnectionResponseHandler("shutdown");
     private final Server AfterShutdown = new ResponseHandlerServer(CloseResponseDown);
     private static final TimerTask NOOPTask = new TimerTask() {
         public void run() {
         }
     };
     private static final NotificationBroadcaster.Confirmation NOOPConfirmation = new NotificationBroadcaster.Confirmation() {
         public void handlingConfirmed(String[] pushIds) {
         }
     };
 
     private final BlockingQueue pendingRequest = new LinkedBlockingQueue(1);
     private final Slot heartbeatInterval;
     private final PushGroupManager pushGroupManager;
     private long responseTimeoutTime;
     private Server activeServer;
     private ConcurrentLinkedQueue notifiedPushIDs = new ConcurrentLinkedQueue();
     private List<String> participatingPushIDs = Collections.emptyList();
     private TimerTask confirmationFailed = NOOPTask;
     private NotificationBroadcaster.Confirmation confirmation = NOOPConfirmation;
     private Timer monitoringScheduler;
 
     private String lastWindow = "";
     private String[] lastNotifications = new String[]{};
     private String notifyBackURI;
     private long connectionRecreationTimeout;
     private long responseTimestamp = System.currentTimeMillis();
     private long requestTimestamp = System.currentTimeMillis();
     private long backupConnectionRecreationTimeout;
 
     public BlockingConnectionServer(final PushGroupManager pushGroupManager, final Timer monitoringScheduler, Slot heartbeat, final boolean terminateBlockingConnectionOnShutdown, Configuration configuration) {
         this.heartbeatInterval = heartbeat;
         this.pushGroupManager = pushGroupManager;
        this.connectionRecreationTimeout = configuration.getAttributeAsLong("connectionRecreationTimeout", 300);
         this.monitoringScheduler = monitoringScheduler;
         //add monitor
         monitoringScheduler.scheduleAtFixedRate(this, 0, 1000);
         this.pushGroupManager.addNotificationReceiver(this);
 
         //define blocking server
         activeServer = new RunningServer(pushGroupManager, terminateBlockingConnectionOnShutdown);
     }
 
     public void service(final Request request) throws Exception {
         activeServer.service(request);
     }
 
     public void shutdown() {
         cancel();
         pushGroupManager.deleteNotificationReceiver(this);
         activeServer.shutdown();
     }
 
     public void run() {
         if ((System.currentTimeMillis() > responseTimeoutTime) && (!pendingRequest.isEmpty())) {
             respondIfPendingRequest(NoopResponse);
         }
     }
 
     private boolean sendNotifications(String[] ids) {
         //stop sending notifications if pushID are not used anymore by the browser
         List pushIDs = new ArrayList(Arrays.asList(ids));
         pushIDs.retainAll(participatingPushIDs);
         boolean anyNotifications = !pushIDs.isEmpty();
 
         if (anyNotifications) {
             notifiedPushIDs.addAll(pushIDs);
             resetTimeout();
             respondIfNotificationsAvailable();
         }
 
         return anyNotifications;
     }
 
     private void resendLastNotifications() {
         sendNotifications(lastNotifications);
     }
 
     private synchronized void respondIfNotificationsAvailable() {
         if (!notifiedPushIDs.isEmpty()) {
             //save notifications, maybe they will need to be resent when blocking connection switches to another window 
             lastNotifications = (String[]) new HashSet(notifiedPushIDs).toArray(STRINGS);
             respondIfPendingRequest(new NotificationHandler(lastNotifications) {
                 public void writeTo(Writer writer) throws IOException {
                     super.writeTo(writer);
 
                     if (log.isLoggable(Level.FINE)) {
                         log.fine("Sending notifications for " + notifiedPushIDs + ".");
                     }
                     pushGroupManager.clearPendingNotifications(participatingPushIDs);
                     notifiedPushIDs.removeAll(Arrays.asList(lastNotifications));
                 }
             });
         }
     }
 
     private void resetTimeout() {
         responseTimeoutTime = System.currentTimeMillis() + heartbeatInterval.getLongValue();
     }
 
     private void respondIfPendingRequest(ResponseHandler handler) {
         Request previousRequest = (Request) pendingRequest.poll();
         if (previousRequest != null) {
             try {
                 recordResponseTime();
                 previousRequest.respondWith(handler);
             } catch (Exception e) {
                 throw new RuntimeException(e);
             }
         }
     }
 
     private static class NoopResponseHandler extends FixedXMLContentHandler {
         public void writeTo(Writer writer) throws IOException {
             writer.write("<noop/>");
             if (log.isLoggable(Level.FINEST)) {
                 log.finest("Sending NoOp.");
             }
         }
     }
 
     private class CloseConnectionResponseHandler implements ResponseHandler {
         private String reason = "undefined";
 
         public CloseConnectionResponseHandler(String reason) {
             this.reason = reason;
         }
 
         public void respond(Response response) throws Exception {
             //let the bridge know that this blocking connection should not be re-initialized
             response.setHeader("X-Connection", "close");
             response.setHeader("X-Connection-reason", reason);
             response.setHeader("Content-Length", 0);
             if (log.isLoggable(Level.FINEST)) {
                 log.finest("Close current blocking connection.");
             }
         }
     }
 
     private class NotificationHandler extends FixedXMLContentHandler {
         private String[] pushIDs;
 
         private NotificationHandler(String[] pushIDs) {
             this.pushIDs = pushIDs;
         }
 
         public void writeTo(Writer writer) throws IOException {
             writer.write("<notified-pushids>");
             for (int i = 0; i < pushIDs.length; i++) {
                 String id = pushIDs[i];
                 if (i > 0) {
                     writer.write(' ');
                 }
                 writer.write(id);
             }
             writer.write("</notified-pushids>");
         }
     }
 
     public void receive(String[] pushIds, final NotificationBroadcaster.Confirmation confirmation) {
         this.confirmation = confirmation;
         this.confirmationFailed = new TimerTask() {
             public void run() {
                 confirmation.handlingConfirmed(STRINGS);
                 if (notifyBackURI != null && !"".equals(notifyBackURI)) {
                     pushGroupManager.park(participatingPushIDs.toArray(STRINGS), notifyBackURI);
                 }
             }
         };
         //in case 500ms are gone confirm handling anyway and then park the last notified pushIDs
         if (log.isLoggable(Level.FINE)) {
             log.fine("Cloud push in " + (connectionRecreationTimeout * 2));
         }
         monitoringScheduler.schedule(confirmationFailed, connectionRecreationTimeout * 2);
 
         sendNotifications(pushIds);
     }
 
     private class RunningServer implements Server {
         private final PushGroupManager pushGroupManager;
         private final boolean terminateBlockingConnectionOnShutdown;
 
         public RunningServer(PushGroupManager pushGroupManager, boolean terminateBlockingConnectionOnShutdown) {
             this.pushGroupManager = pushGroupManager;
             this.terminateBlockingConnectionOnShutdown = terminateBlockingConnectionOnShutdown;
         }
 
         public void service(final Request request) throws Exception {
             resetTimeout();
             adjustConnectionRecreationTimeout(request);
 
             respondIfPendingRequest(CloseResponseDup);
 
             //resend notifications if the window owning the blocking connection has changed
             String currentWindow = request.getHeader("ice.push.window");
             currentWindow = currentWindow == null ? "" : currentWindow;
             boolean resend = !lastWindow.equals(currentWindow);
             lastWindow = currentWindow;
 
             pendingRequest.put(request);
             try {
                 participatingPushIDs = Arrays.asList(request.getParameterAsStrings("ice.pushid"));
                 notifyBackURI = request.getHeader("ice.notifyBack");
 
                 confirmationFailed.cancel();
                 confirmation.handlingConfirmed(participatingPushIDs.toArray(STRINGS));
 
                 if (null != notifyBackURI)  {
                     pushGroupManager.pruneParkedIDs(notifyBackURI, 
                             participatingPushIDs);
                 }
 
                 if (!sendNotifications(pushGroupManager.getPendingNotifications())) {
                     if (resend) {
                         resendLastNotifications();
                     } else {
                         respondIfNotificationsAvailable();
                     }
                 }
             } catch (RuntimeException e) {
                 log.fine("Request does not contain pushIDs.");
                 respondIfPendingRequest(NoopResponse);
             }
         }
 
         public void shutdown() {
             //avoid creating new blocking connections after shutdown
             activeServer = AfterShutdown;
             respondIfPendingRequest(terminateBlockingConnectionOnShutdown ? CloseResponseDown : NoopResponse);
         }
     }
 
     private void adjustConnectionRecreationTimeout(Request request) {
         backupConnectionRecreationTimeout = connectionRecreationTimeout;
         long now = System.currentTimeMillis();
         long elapsed = now - requestTimestamp;
         requestTimestamp = now;
         long currentResponseDelay = requestTimestamp - responseTimestamp;
         //adaptive timeout -- see algorithm described in PUSH-164
         long responseDelay = currentResponseDelay;
         responseDelay = Math.max(responseDelay, 
                 (connectionRecreationTimeout * 4) / 5);
         responseDelay = Math.min(responseDelay, 
                 (connectionRecreationTimeout * 3) / 2);
         responseDelay = Math.max(responseDelay, 500);
         
         connectionRecreationTimeout = 
                     (responseDelay + (connectionRecreationTimeout * 4)) / 5;
 
         if (log.isLoggable(Level.FINE)) {
             String browserID = BrowserDispatcher
                     .getBrowserIDFromCookie(request);
             if (null == browserID)  {
                 browserID = "undefined";
             }
             participatingPushIDs = Arrays.asList(
                     request.getParameterAsStrings("ice.pushid"));
             notifyBackURI = request.getHeader("ice.notifyBack");
             log.fine(
                 "ICEpush metric:" +
                 " IP: " + request.getRemoteAddr() + 
                 " pushIds: " + participatingPushIDs + 
                 " Cloud Push ID: " + notifyBackURI + 
                 " Browser: " + browserID + 
                 " last request: " + elapsed +
                 " Latency: " + currentResponseDelay +
                 " connectionRecreationTimeout: " + connectionRecreationTimeout );
         }
     }
 
     private void revertConnectionRecreationTimeout() {
         connectionRecreationTimeout = backupConnectionRecreationTimeout;
     }
 
     private void recordResponseTime() {
         responseTimestamp = System.currentTimeMillis();
     }
 }
