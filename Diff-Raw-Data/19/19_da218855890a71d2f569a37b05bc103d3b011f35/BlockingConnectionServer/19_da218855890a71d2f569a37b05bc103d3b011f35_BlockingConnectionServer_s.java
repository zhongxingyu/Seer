 /*
  * Copyright 2004-2013 ICEsoft Technologies Canada Corp.
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
 
 import javax.servlet.http.Cookie;
 import java.io.*;
 import java.util.*;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.ConcurrentLinkedQueue;
 import java.util.concurrent.LinkedBlockingQueue;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 public class BlockingConnectionServer extends TimerTask implements Server, NotificationBroadcaster.Receiver {
     private static final Logger log = Logger.getLogger(BlockingConnectionServer.class.getName());
     private static final String BrowserIDCookieName = "ice.push.browser";
     private static final String[] STRINGS = new String[0];
     //Define here to avoid classloading problems after application exit
     private static final ResponseHandler ShutdownNoopResponse = new NoopResponseHandler("shutdown");
     private static final ResponseHandler TimeoutNoopResponse = new NoopResponseHandler("response timeout");
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
 
     private final BlockingQueue pendingRequest = new LinkedBlockingQueue(1);
     private final Slot heartbeatInterval;
     // This is either a LocalPushGroupManager or a DynamicPushGroupManager
     private final PushGroupManager pushGroupManager;
     private String browserID;
     private long responseTimeoutTime;
     private Server activeServer;
     private ConcurrentLinkedQueue notifiedPushIDs = new ConcurrentLinkedQueue();
     private List<String> participatingPushIDs = Collections.emptyList();
     private Timer monitoringScheduler;
 
     private String lastWindow = "";
     private String[] lastNotifications = new String[]{};
     private NotifyBackURI notifyBackURI;
     private long defaultConnectionRecreationTimeout;
     private long responseTimestamp = System.currentTimeMillis();
     private long requestTimestamp = System.currentTimeMillis();
     private long backOffDelay = 0;
 
     public BlockingConnectionServer(final PushGroupManager pushGroupManager, final Timer monitoringScheduler, Slot heartbeat, final boolean terminateBlockingConnectionOnShutdown, Configuration configuration) {
         this.heartbeatInterval = heartbeat;
         this.pushGroupManager = pushGroupManager;
         this.defaultConnectionRecreationTimeout = configuration.getAttributeAsLong("connectionRecreationTimeout", 5000);
         this.monitoringScheduler = monitoringScheduler;
         //add monitor
         this.monitoringScheduler.scheduleAtFixedRate(this, 0, 1000);
         this.pushGroupManager.addBlockingConnectionServer(this);
         this.pushGroupManager.addNotificationReceiver(this);
 
         //define blocking server
         activeServer = new RunningServer(pushGroupManager, terminateBlockingConnectionOnShutdown);
     }
 
     public synchronized void backOff(final String browserID, final long delay) {
         if (this.browserID != null && this.browserID.equals(browserID) && delay > 0) {
             backOffDelay = delay;
             respondIfBackOffRequested();
         }
     }
 
     public void service(final Request request) throws Exception {
         activeServer.service(request);
     }
 
     public void shutdown() {
         cancel();
         pushGroupManager.deleteNotificationReceiver(this);
         pushGroupManager.removeBlockingConnectionServer(this);
         activeServer.shutdown();
     }
 
     public void run() {
         try {
             if ((System.currentTimeMillis() > responseTimeoutTime) && (!pendingRequest.isEmpty())) {
                 respondIfPendingRequest(TimeoutNoopResponse);
             }
         } catch (Exception exception) {
             if (log.isLoggable(Level.WARNING)) {
                 log.log(Level.WARNING, "Exception caught on " + this.getClass().getName() + " TimerTask.", exception);
             }
         }
     }
 
     private boolean sendNotifications(final String[] ids) {
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
 
     private synchronized boolean respondIfBackOffRequested() {
         boolean result = false;
         if (backOffDelay > 0) {
             if (result = respondIfPendingRequest(new BackOffResponseHandler(backOffDelay))) {
                 backOffDelay = 0;
             }
         }
         return result;
     }
 
     private synchronized void respondIfNotificationsAvailable() {
         if (!notifiedPushIDs.isEmpty()) {
             //save notifications, maybe they will need to be resent when blocking connection switches to another window
             lastNotifications = (String[]) new HashSet(notifiedPushIDs).toArray(STRINGS);
             respondIfPendingRequest(new NotificationHandler(lastNotifications) {
                 public void writeTo(Writer writer) throws IOException {
                     super.writeTo(writer);
 
                     if (log.isLoggable(Level.FINE)) {
                         log.log(
                             Level.FINE,
                             "Push Notifications available for PushIDs '" + notifiedPushIDs + "', trying to respond.");
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
 
     private boolean respondIfPendingRequest(ResponseHandler handler) {
         Request previousRequest = (Request) pendingRequest.poll();
         if (previousRequest != null) {
             if (log.isLoggable(Level.FINE)) {
                 log.log(Level.FINE, "Pending request for PushIDs '" + participatingPushIDs + "', trying to respond.");
             }
             try {
                 recordResponseTime();
                 previousRequest.respondWith(handler);
                 return true;
             } catch (Exception e) {
                 throw new RuntimeException(e);
             }
         }
         return false;
     }
 
     private static class ServerErrorResponseHandler extends FixedXMLContentHandler {
         private String message;
 
         private ServerErrorResponseHandler(Throwable exception) {
             this.message = exception.getMessage();
         }
 
         public void respond(Response response) throws Exception {
             response.setStatus(503);
             super.respond(response);
         }
 
         public void writeTo(Writer writer) throws IOException {
             writer.write("<server-error message=\"");
             writer.write(message);
             writer.write("\"/>");
             if (log.isLoggable(Level.FINE)) {
                 log.log(Level.FINE, "Sending server-error - " + message);
             }
         }
     }
 
     private static class NoopResponseHandler extends FixedXMLContentHandler {
         private final String reason;
 
         private NoopResponseHandler(final String reason) {
             this.reason = reason;
         }
 
         @Override
         public void respond(final Response response)
         throws Exception {
             response.setHeader("X-Connection-reason", reason);
             super.respond(response);
         }
 
         public void writeTo(Writer writer) throws IOException {
             writer.write("<noop/>");
             if (log.isLoggable(Level.FINE)) {
                 log.log(Level.FINE, "Sending NoOp.");
             }
         }
     }
 
     private static class BackOffResponseHandler extends FixedXMLContentHandler {
         private long delay;
 
         private BackOffResponseHandler(long delay) {
             this.delay = delay;
         }
 
         public void writeTo(Writer writer) throws IOException {
             writer.write("<back-off delay=\"" + delay + "\"/>");
             if (log.isLoggable(Level.FINE)) {
                 log.log(Level.FINE, "Sending back-off - " + delay + "ms.");
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
             if (log.isLoggable(Level.FINE)) {
                 log.log(Level.FINE, "Close current blocking connection.");
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
             if (log.isLoggable(Level.FINE)) {
                 log.log(Level.FINE, "Sending Notified PushIDs '" + Arrays.toString(pushIDs) + "'.");
             }
         }
     }
 
     public void receive(final String[] pushIDs) {
         List<String> pushIDList = new ArrayList<String>(Arrays.asList(pushIDs));
         pushIDList.retainAll(participatingPushIDs);
         sendNotifications(pushIDs);
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
             try {
                 participatingPushIDs = Arrays.asList(request.getParameterAsStrings("ice.pushid"));
                 adjustConnectionRecreationTimeout(request);
 
                 respondIfPendingRequest(CloseResponseDup);
                 if (browserID == null) {
                     browserID = getBrowserIDFromCookie(request);
                 }
                 long sequenceNumber;
                 try {
                     sequenceNumber = request.getHeaderAsLong("ice.push.sequence");
                 } catch (final RuntimeException exception) {
                     sequenceNumber = 0;
                 }
                 pushGroupManager.recordListen(participatingPushIDs, sequenceNumber);
                 //resend notifications if the window owning the blocking connection has changed
                 String currentWindow = request.getHeader("ice.push.window");
                 currentWindow = currentWindow == null ? "" : currentWindow;
                 boolean resend = !lastWindow.equals(currentWindow);
                 lastWindow = currentWindow;
 
                 pendingRequest.put(request);
                 setNotifyBackURI(request);
                 pushGroupManager.scan(participatingPushIDs.toArray(STRINGS));
                 pushGroupManager.cancelConfirmationTimeout(participatingPushIDs);
                 pushGroupManager.cancelExpiryTimeout(participatingPushIDs);
                 pushGroupManager.startExpiryTimeout(participatingPushIDs, notifyBackURI);
                 if (null != notifyBackURI)  {
                     pushGroupManager.pruneParkedIDs(notifyBackURI, 
                             participatingPushIDs);
                 }
                 if (!respondIfBackOffRequested()) {
                     if (!sendNotifications(pushGroupManager.getPendingNotifications())) {
                         if (resend) {
                             resendLastNotifications();
                         } else {
                             respondIfNotificationsAvailable();
                         }
                     }
                 }
             } catch (Throwable t) {
                 log.log(Level.WARNING, "Failed to respond to request", t);
                 respondIfPendingRequest(new ServerErrorResponseHandler(t));
             }
         }
 
         public void shutdown() {
             //avoid creating new blocking connections after shutdown
             activeServer = AfterShutdown;
             respondIfPendingRequest(terminateBlockingConnectionOnShutdown ? CloseResponseDown : ShutdownNoopResponse);
         }
     }
 
     private void adjustConnectionRecreationTimeout(Request request) {
         for (final String pushIDString : participatingPushIDs) {
             PushID pushID = pushGroupManager.getPushID(pushIDString);
             if (pushID != null) {
                 if (pushID.getStatus().getConnectionRecreationTimeout() == -1) {
                     pushID.getStatus().setConnectionRecreationTimeout(defaultConnectionRecreationTimeout);
                 }
                 pushID.getStatus().backUpConnectionRecreationTimeout();
             }
         }
         long now = System.currentTimeMillis();
         long elapsed = now - requestTimestamp;
         requestTimestamp = now;
         long currentResponseDelay = requestTimestamp - responseTimestamp;
         //adaptive timeout -- see algorithm described in PUSH-164
         long responseDelay = currentResponseDelay;
         for (final String pushIDString : participatingPushIDs) {
             PushID pushID = pushGroupManager.getPushID(pushIDString);
             if (pushID != null) {
                 responseDelay = Math.max(responseDelay, (pushID.getStatus().getConnectionRecreationTimeout() * 4) / 5);
                 responseDelay = Math.min(responseDelay, (pushID.getStatus().getConnectionRecreationTimeout() * 3) / 2);
                 responseDelay = Math.max(responseDelay, 500);
                 pushID.getStatus().setConnectionRecreationTimeout(
                     (responseDelay + (pushID.getStatus().getConnectionRecreationTimeout() * 4)) / 5);
             }
         }
 
         if (log.isLoggable(Level.FINE)) {
             String browserID = getBrowserIDFromCookie(request);
             if (null == browserID)  {
                 browserID = "undefined";
             }
             participatingPushIDs = Arrays.asList(
                     request.getParameterAsStrings("ice.pushid"));
             setNotifyBackURI(request);
             log.log(
                 Level.FINE,
                 "ICEpush metric:" +
                     " IP: " + request.getRemoteAddr() +
                     " pushIds: " + participatingPushIDs +
                     " Cloud Push ID: " + notifyBackURI +
                     " Browser: " + browserID +
                     " last request: " + elapsed +
                     " Latency: " + currentResponseDelay);
         }
     }
 
     private static String getBrowserIDFromCookie(final Request request) {
         String browserID = request.getHeader(BrowserIDCookieName);
         if (null != browserID)  {
             return browserID;
         }
         Cookie[] cookies = request.getCookies();
         if (cookies != null) {
             for (Cookie cookie : cookies) {
                 if (BrowserIDCookieName.equals(cookie.getName())) {
                     return cookie.getValue();
                 }
             }
         }
         return null;
     }
 
     private void revertConnectionRecreationTimeout() {
         for (final String pushIDString : participatingPushIDs) {
             PushID pushID = pushGroupManager.getPushID(pushIDString);
             if (pushID != null) {
                 pushID.getStatus().revertConnectionRecreationTimeout();
             }
         }
     }
 
     private void recordResponseTime() {
         responseTimestamp = System.currentTimeMillis();
     }
 
     private void setNotifyBackURI(final Request request) {
         String notifyBack = request.getHeader("ice.notifyBack");
         if (notifyBack != null && notifyBack.trim().length() != 0) {
            log.log(Level.FINE, "Found new NotifyBackURI on Request: '" + notifyBack + "'.");
            this.notifyBackURI = new NotifyBackURI(notifyBack);
             pushGroupManager.setNotifyBackURI(participatingPushIDs, this.notifyBackURI, true);
         } else {
             //If notifyBackURI was set on the server side with no corresponding request from the client
             //then we should query the PushGroupManager for the uri and set it here.
             Iterator<String> idIterator = participatingPushIDs.iterator();
             while (idIterator.hasNext()) {
                 NotifyBackURI notifyBackURI = pushGroupManager.getNotifyBackURI(idIterator.next());
                 if (notifyBackURI != null) {
                     log.log(Level.FINE, "Found NotifyBackURI on PushGroupManager: '" + notifyBackURI + "'.");
                     this.notifyBackURI = notifyBackURI;
                     break;
                 }
             }
         }
     }
 }
