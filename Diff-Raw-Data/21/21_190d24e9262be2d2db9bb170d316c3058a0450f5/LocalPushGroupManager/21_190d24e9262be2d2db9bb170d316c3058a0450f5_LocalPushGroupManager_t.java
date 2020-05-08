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
 
 import org.icepush.servlet.ServletContextConfiguration;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentMap;
 import java.util.concurrent.CopyOnWriteArraySet;
 import java.util.concurrent.LinkedBlockingQueue;
 import java.util.concurrent.atomic.AtomicInteger;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.servlet.ServletContext;
 
 public class LocalPushGroupManager extends AbstractPushGroupManager implements PushGroupManager {
     private static final Logger LOGGER = Logger.getLogger(LocalPushGroupManager.class.getName());
     private static final String[] STRINGS = new String[0];
     private static final int GROUP_SCANNING_TIME_RESOLUTION = 3000; // ms
     private static final OutOfBandNotifier NOOPOutOfBandNotifier = new OutOfBandNotifier() {
         public void broadcast(PushNotification notification, String[] uris) {
             System.out.println("message send " + notification + " to " + Arrays.asList(uris));
         }
 
         public void registerProvider(String protocol, NotificationProvider provider) {
         }
         public void trace(String message)  {
             if (LOGGER.isLoggable(Level.WARNING)) {
                 LOGGER.log(Level.WARNING, "NOOPOutOfBandNotifier discarding notification " + message);
             }
         }
     };
     private static final Runnable NOOP = new Runnable() {
         public void run() {
         }
     };
     private final Set<BlockingConnectionServer> blockingConnectionServerSet =
         new CopyOnWriteArraySet<BlockingConnectionServer>();
     private final ConcurrentMap<String, PushID> pushIDMap = new ConcurrentHashMap<String, PushID>();
     private final ConcurrentMap<String, Group> groupMap = new ConcurrentHashMap<String, Group>();
     private final HashSet<String> pendingNotifications = new HashSet();
     protected final HashMap<String, NotifyBackURI> parkedPushIDs = new HashMap();
     private final NotificationBroadcaster outboundNotifier = new LocalNotificationBroadcaster();
     private final Timer timer = new Timer("Notification queue consumer.", true);
     private final TimerTask queueConsumer;
     private final BlockingQueue<Runnable> queue;
     private final long groupTimeout;
     private final long pushIdTimeout;
     private final long cloudPushIdTimeout;
     protected final long minCloudPushInterval;
     private final ServletContext context;
     protected final Timer timeoutTimer = new Timer("Timeout timer", true);
 
     private long lastTouchScan = System.currentTimeMillis();
     private long lastExpiryScan = System.currentTimeMillis();
 
     public LocalPushGroupManager(final ServletContext servletContext) {
         this.context = servletContext;
         Configuration configuration = new ServletContextConfiguration("org.icepush", servletContext);
         this.groupTimeout = configuration.getAttributeAsLong("groupTimeout", 2 * 60 * 1000);
         this.pushIdTimeout = configuration.getAttributeAsLong("pushIdTimeout", 2 * 60 * 1000);
         this.cloudPushIdTimeout = configuration.getAttributeAsLong("cloudPushIdTimeout", 30 * 60 * 1000);
         this.minCloudPushInterval = configuration.getAttributeAsLong("minCloudPushInterval", 10 * 1000);
         int notificationQueueSize = configuration.getAttributeAsInteger("notificationQueueSize", 1000);
         this.queue = new LinkedBlockingQueue<Runnable>(notificationQueueSize);
         this.queueConsumer = new QueueConsumerTask();
         this.timer.schedule(queueConsumer, 0);
     }
 
     public void scan(final String[] confirmedPushIDs) {
         Set<String> pushIDs = new HashSet<String>();
         long now = System.currentTimeMillis();
         //accumulate pushIDs
         pushIDs.addAll(Arrays.asList(confirmedPushIDs));
         //avoid to scan/touch the groups on each notification
         if (lastTouchScan + GROUP_SCANNING_TIME_RESOLUTION < now) {
             try {
                 for (Group group : groupMap.values()) {
                     group.touchIfMatching(pushIDs);
                     group.discardIfExpired();
                 }
             } finally {
                 lastTouchScan = now;
                 lastExpiryScan = now;
             }
         }
     }
 
     public void addBlockingConnectionServer(final BlockingConnectionServer server) {
         blockingConnectionServerSet.add(server);
     }
 
     public void addMember(final String groupName, final String id) {
         PushID pushID = pushIDMap.get(id);
         if (pushID == null) {
             pushIDMap.put(id, newPushID(id, groupName));
         } else {
             pushID.addToGroup(groupName);
         }
         Group group = groupMap.get(groupName);
         if (group == null) {
             groupMap.put(groupName, new Group(groupName, id));
         } else {
             group.addPushID(id);
         }
         memberAdded(groupName, id);
         if (LOGGER.isLoggable(Level.FINE)) {
             LOGGER.log(Level.FINE, "Added PushID '" + id + "' to Push Group '" + groupName + "'.");
         }
     }
 
     public void addNotificationReceiver(final NotificationBroadcaster.Receiver observer) {
         outboundNotifier.addReceiver(observer);
     }
 
     public void backOff(final String browserID, final long delay) {
         for (final BlockingConnectionServer server : blockingConnectionServerSet) {
             server.backOff(browserID, delay);
         }
     }
 
     public void deleteNotificationReceiver(final NotificationBroadcaster.Receiver observer) {
         outboundNotifier.deleteReceiver(observer);
     }
 
     public void clearPendingNotifications(final List<String> pushIdList) {
         pendingNotifications.removeAll(pushIdList);
     }
 
     public String[] getPendingNotifications() {
         return pendingNotifications.toArray(STRINGS);
     }
 
     public Map<String, String[]> getGroupMap() {
         Map<String, String[]> groupMap = new HashMap<String, String[]>();
         for (Group group : new ArrayList<Group>(this.groupMap.values())) {
             groupMap.put(group.name, group.getPushIDs());
         }
         return groupMap;
     }
 
     public void push(final String groupName) {
         if (LOGGER.isLoggable(Level.FINE)) {
             LOGGER.log(Level.FINE, "Push Notification request for Push Group '" + groupName + "'.");
         }
         if (!queue.offer(new Notification(groupName))) {
             // Leave at INFO
             if (LOGGER.isLoggable(Level.INFO)) {
                 LOGGER.log(
                     Level.INFO,
                     "Push Notification request for Push Group '" + groupName + "' was dropped, " +
                         "queue maximum size reached.");
             }
         }
     }
 
     public void push(final String groupName, final PushConfiguration pushConfiguration) {
         if (LOGGER.isLoggable(Level.FINE)) {
             LOGGER.log(
                 Level.FINE,
                 "Push Notification request for Push Group '" + groupName + "' " +
                     "(Push configuration: '" + pushConfiguration + "').");
         }
         Notification notification;
         if (pushConfiguration.getAttributes().get("subject") != null) {
             notification = new OutOfBandNotification(groupName, pushConfiguration);
         } else {
             notification = new Notification(groupName, pushConfiguration);
         }
         //add this notification to a blocking queue
         queue.add(notification);
     }
 
     public void recordListen(final List<String> pushIDList, final int sequenceNumber) {
         for (final String pushIDString : pushIDList) {
             PushID pushID = pushIDMap.get(pushIDString);
             if (pushID != null) {
                 if (LOGGER.isLoggable(Level.FINE)) {
                     LOGGER.log(
                         Level.FINE,
                         "Record listen for PushID '" + pushIDString + "' (Sequence# " + sequenceNumber + ").");
                 }
                 pushID.setSequenceNumber(sequenceNumber);
             }
         }
     }
 
     public void removeBlockingConnectionServer(final BlockingConnectionServer server) {
         blockingConnectionServerSet.remove(server);
     }
 
     public void removeMember(final String groupName, final String pushId) {
         Group group = groupMap.get(groupName);
         if (group != null) {
             group.removePushID(pushId);
             PushID id = pushIDMap.get(pushId);
             if (id != null) {
                 id.removeFromGroup(groupName);
             }
             memberRemoved(groupName, pushId);
             if (LOGGER.isLoggable(Level.FINE)) {
                 LOGGER.log(Level.FINE, "Removed PushID '" + pushId + "' from Push Group '" + groupName + "'.");
             }
         }
     }
 
     public boolean setNotifyBackURI(
         final List<String> pushIDList, final NotifyBackURI notifyBackURI, final boolean broadcast) {
 
         boolean modified = false;
         for (final String pushIDString : pushIDList) {
             PushID pushID = pushIDMap.get(pushIDString);
             if (pushID != null) {
                 if (pushID.setNotifyBackURI(notifyBackURI)) {
                     modified = true;
                 }
             }
         }
         return modified;
     }
 
     public void park(final String pushId, final NotifyBackURI notifyBackURI) {
         parkedPushIDs.put(pushId, notifyBackURI);
     }
 
     public void pruneParkedIDs(final NotifyBackURI notifyBackURI, final List<String> listenedPushIds)  {
         for (String parkedID : parkedPushIDs.keySet())  {
             NotifyBackURI thisNotifyBack = parkedPushIDs.get(parkedID);
             if (thisNotifyBack.getURI().equals(notifyBackURI.getURI()) && !listenedPushIds.contains(parkedID))  {
                 if (LOGGER.isLoggable(Level.FINE)) {
                     LOGGER.log(
                         Level.FINE, "Removed unlistened parked PushID '" + parkedID + "' for '" + notifyBackURI + "'.");
                 }
                 parkedPushIDs.remove(parkedID);
             }
         }
     }
 
     public void shutdown() {
         queueConsumer.cancel();
         timer.cancel();
        timeoutTimer.cancel();
     }
 
     public void cancelConfirmationTimeout(final List<String> pushIDList) {
         for (final String pushIDString : pushIDList) {
             PushID pushID = pushIDMap.get(pushIDString);
             if (pushID != null) {
                 pushID.cancelConfirmationTimeout();
             }
         }
     }
 
     public void cancelExpiryTimeout(final List<String> pushIDList) {
         for (final String pushIDString : pushIDList) {
             PushID pushID = pushIDMap.get(pushIDString);
             if (pushID != null) {
                 pushID.cancelExpiryTimeout();
             }
         }
     }
 
     public void startConfirmationTimeout(
         final List<String> pushIDList, final NotifyBackURI notifyBackURI, final long timeout) {
 
         if (notifyBackURI != null) {
             long now = System.currentTimeMillis();
             if (notifyBackURI.getTimestamp() + minCloudPushInterval <= now + timeout) {
                 for (final String pushIDString : pushIDList) {
                     PushID pushID = pushIDMap.get(pushIDString);
                     if (pushID != null) {
                         pushID.startConfirmationTimeout(notifyBackURI, timeout, pushID.getSequenceNumber());
                     }
                 }
             } else {
                 if (LOGGER.isLoggable(Level.FINE)) {
                     LOGGER.log(
                         Level.FINE,
                         "Timeout is within the minimum Cloud Push interval for URI '" + notifyBackURI + "'. (" +
                             "timestamp: '" + notifyBackURI.getTimestamp() + "', " +
                             "minCloudPushInterval: '" + minCloudPushInterval + "', " +
                             "now: '" + now + "', " +
                             "timeout: '" + timeout + "'" +
                         ")");
                 }
             }
         }
     }
 
     public void startExpiryTimeout(final List<String> pushIDList, final NotifyBackURI notifyBackURI) {
         for (final String pushIDString : pushIDList) {
             PushID pushID = pushIDMap.get(pushIDString);
             if (pushID != null) {
                 pushID.startExpiryTimeout(notifyBackURI, pushID.getSequenceNumber());
             }
         }
     }
 
     protected Map<String, PushID> getPushIDMap() {
         return Collections.unmodifiableMap(pushIDMap);
     }
 
     protected HashMap<String, Integer> getPushIDSequenceNumberMap() {
         HashMap<String, Integer> pushIDSequenceNumberMap = new HashMap<String, Integer>();
         for (final PushID pushID : pushIDMap.values()) {
             pushIDSequenceNumberMap.put(pushID.getID(), pushID.getSequenceNumber());
         }
         return pushIDSequenceNumberMap;
     }
 
     private void scanForExpiry() {
         long now = System.currentTimeMillis();
         //avoid to scan/touch the groups on each notification
         if (lastExpiryScan + GROUP_SCANNING_TIME_RESOLUTION < now) {
             try {
                 for (Group group : groupMap.values()) {
                     group.discardIfExpired();
                 }
             } finally {
                 lastExpiryScan = now;
             }
         }
     }
 
     public void listeningPushIDs(final Map<String, Integer> pushIDSequenceNumberMap) {
         // Do nothing.
     }
 
 //    public void touchPushID(final String id, final Long timestamp) {
 //        PushID pushID = pushIDMap.get(id);
 //        if (pushID != null) {
 //            pushID.touch(timestamp);
 //        }
 //    }
 
     protected PushID newPushID(final String id, final String groupName) {
         PushID pushID = new PushID(id, groupName);
         pushID.startExpiryTimeout();
         return pushID;
     }
 
     protected OutOfBandNotifier getOutOfBandNotifier() {
         Object attribute = context.getAttribute(OutOfBandNotifier.class.getName());
         return attribute == null ? NOOPOutOfBandNotifier : (OutOfBandNotifier) attribute;
     }
 
     private class Group {
         private final Set<String> pushIdList = new HashSet<String>();
         private final String name;
         private long lastAccess = System.currentTimeMillis();
 
         private Group(final String name, final String firstPushId) {
             this.name = name;
             if (LOGGER.isLoggable(Level.FINE)) {
                 LOGGER.log(Level.FINE, "Push Group '" + this.name + "' created.");
             }
             addPushID(firstPushId);
         }
 
         private void addPushID(final String pushId) {
             pushIdList.add(pushId);
         }
 
         private void discardIfExpired() {
             //expire group
             if (lastAccess + groupTimeout < System.currentTimeMillis()) {
                 if (LOGGER.isLoggable(Level.FINE)) {
                     LOGGER.log(Level.FINE, "Push Group '" + name + "' expired.");
                 }
                 groupMap.remove(name);
                 pendingNotifications.removeAll(pushIdList);
                 for (String id : pushIdList) {
                     PushID pushID = pushIDMap.get(id);
                     if (pushID != null) {
                         pushID.removeFromGroup(name);
                     }
                 }
             }
         }
 
         private String[] getPushIDs() {
             return pushIdList.toArray(new String[pushIdList.size()]);
         }
 
         private void removePushID(final String pushId) {
             pushIdList.remove(pushId);
             if (pushIdList.isEmpty()) {
                 if (LOGGER.isLoggable(Level.FINE)) {
                     LOGGER.log(
                         Level.FINE, "Disposed Push Group '" + name + "' since it no longer contains any PushIDs.");
                 }
                 groupMap.remove(name);
             }
         }
 
         private void touch() {
             touch(System.currentTimeMillis());
         }
 
         private void touch(final Long timestamp) {
             lastAccess = timestamp;
         }
 
         private void touchIfMatching(final Collection pushIds) {
             Iterator i = pushIds.iterator();
             while (i.hasNext()) {
                 String pushId = (String) i.next();
                 if (pushIdList.contains(pushId)) {
                     touch();
                     groupTouched(name, lastAccess);
                     //no need to touchIfMatching again
                     //return right away without checking the expiration
                     return;
                 }
             }
         }
     }
 
     // These counters are only used by LOGGER
     protected static AtomicInteger globalConfirmationTimeoutCounter = new AtomicInteger(0);
     protected static AtomicInteger globalExpiryTimeoutCounter = new AtomicInteger(0);
 
     protected class PushID {
         private final String id;
         private final Set<String> groups = new HashSet<String>();
         private long lastAccess = System.currentTimeMillis();
         private NotifyBackURI notifyBackURI;
         private int sequenceNumber = -1;
         private int confirmationTimeoutSequenceNumber = -1;
         private int expiryTimeoutSequenceNumber = -1;
         protected TimerTask confirmationTimeout;
         private TimerTask expiryTimeout;
         protected PushConfiguration pushConfiguration;
 
         // These counters are only used by LOGGER
         protected AtomicInteger confirmationTimeoutCounter = new AtomicInteger(0);
         protected AtomicInteger expiryTimeoutCounter = new AtomicInteger(0);
 
         protected PushID(String id, String group) {
             this.id = id;
             addToGroup(group);
         }
 
         public void addToGroup(String group) {
             groups.add(group);
         }
 
         public void cancelConfirmationTimeout() {
             if (confirmationTimeout != null) {
                 if (LOGGER.isLoggable(Level.FINE)) {
                     LOGGER.log(
                         Level.FINE,
                         "Cancel confirmation timeout for PushID '" + id + "'.\r\n\r\n" +
                             "Confirmation Timeout Counter        : " +
                                 confirmationTimeoutCounter.decrementAndGet() + "\r\n" +
                             "Global Confirmation Timeout Counter : " +
                                 globalConfirmationTimeoutCounter.decrementAndGet() + "\r\n" +
                             "Expiry Timeout Counter              : " +
                                 expiryTimeoutCounter.get() + "\r\n" +
                             "Global Expiry Timeout Counter       : " +
                                 globalExpiryTimeoutCounter.get() + "\r\n");
                 }
                 confirmationTimeout.cancel();
                 confirmationTimeout = null;
                 confirmationTimeoutSequenceNumber = -1;
                 pushConfiguration = null;
             }
         }
 
         public void cancelExpiryTimeout() {
             if (expiryTimeout != null) {
                 if (LOGGER.isLoggable(Level.FINE)) {
                     LOGGER.log(
                         Level.FINE,
                         "Cancel expiry timeout for PushID '" + id + "'.\r\n\r\n" +
                             "Confirmation Timeout Counter        : " +
                                 confirmationTimeoutCounter.get() + "\r\n" +
                             "Global Confirmation Timeout Counter : " +
                                 globalConfirmationTimeoutCounter.get() + "\r\n" +
                             "Expiry Timeout Counter              : " +
                                 expiryTimeoutCounter.decrementAndGet() + "\r\n" +
                             "Global Expiry Timeout Counter       : " +
                                 globalExpiryTimeoutCounter.decrementAndGet() + "\r\n");
                 }
                 expiryTimeout.cancel();
                 expiryTimeout = null;
                 expiryTimeoutSequenceNumber = -1;
             }
         }
 
         public void discard() {
             if (!parkedPushIDs.containsKey(id)) {
                 if (LOGGER.isLoggable(Level.FINE)) {
                     LOGGER.log(Level.FINE, "PushID '" + id + "' discarded.");
                 }
                 pushIDMap.remove(id);
                 pendingNotifications.remove(id);
                 for (String groupName : groups) {
                     Group group = groupMap.get(groupName);
                     if (group != null) {
                         group.removePushID(id);
                     }
                 }
             }
         }
 
 //        public void discardIfExpired() {
 //            //expire pushId
 //            if (!parkedPushIDs.containsKey(id) && lastAccess + pushIdTimeout < System.currentTimeMillis()) {
 //                discard();
 //            }
 //        }
 
         public int getConfirmationTimeoutSequenceNumber() {
             return confirmationTimeoutSequenceNumber;
         }
 
         public int getExpiryTimeoutSequenceNumber() {
             return expiryTimeoutSequenceNumber;
         }
 
         public String getID() {
             return id;
         }
 
         public NotifyBackURI getNotifyBackURI() {
             return notifyBackURI;
         }
 
         public int getSequenceNumber() {
             return sequenceNumber;
         }
 
         public void removeFromGroup(String group) {
             groups.remove(group);
             if (groups.isEmpty()) {
                 if (LOGGER.isLoggable(Level.FINE)) {
                     LOGGER.log(
                         Level.FINE, "Disposed PushID '" + id + "' since it no longer belongs to any Push Group.");
                 }
                 pushIDMap.remove(id);
             }
         }
 
         public boolean setNotifyBackURI(final NotifyBackURI notifyBackURI) {
             boolean modified =
                 this.notifyBackURI == null ||
                 !this.notifyBackURI.getURI().equals(notifyBackURI.getURI());
             this.notifyBackURI = notifyBackURI;
             return modified;
         }
 
         public void setSequenceNumber(final int sequenceNumber) {
             this.sequenceNumber = sequenceNumber;
         }
 
         public void startConfirmationTimeout(
             final NotifyBackURI notifyBackURI, final long timeout, final int sequenceNumber) {
 
             if (pushConfiguration != null) {
                 if (confirmationTimeout == null) {
                     if (LOGGER.isLoggable(Level.FINE)) {
                         LOGGER.log(
                             Level.FINE,
                             "Start confirmation timeout for PushID '" + id + "' (" +
                                     "URI: '" + notifyBackURI + "', " +
                                     "timeout: '" + timeout + "', " +
                                     "sequence number: '" + sequenceNumber + "'" +
                             ").\r\n\r\n" +
                                 "Confirmation Timeout Counter        : " +
                                     confirmationTimeoutCounter.incrementAndGet() + "\r\n" +
                                 "Global Confirmation Timeout Counter : " +
                                     globalConfirmationTimeoutCounter.incrementAndGet() + "\r\n" +
                                 "Expiry Timeout Counter              : " +
                                     expiryTimeoutCounter.get() + "\r\n" +
                                 "Global Expiry Timeout Counter       : " +
                                     globalExpiryTimeoutCounter.get() + "\r\n");
                     }
                     confirmationTimeoutSequenceNumber = sequenceNumber;
                    try {
                        timeoutTimer.schedule(
                            confirmationTimeout = newConfirmationTimeout(notifyBackURI, timeout), timeout);
                    } catch (final IllegalStateException exception) {
                        // timeoutTimer was cancelled or its timer thread terminated.
                    }
                 } else {
                     if (LOGGER.isLoggable(Level.FINE)) {
                         LOGGER.log(
                             Level.FINE,
                             "Confirmation timeout already scheduled for PushID '" + id + "' " +
                                 "(URI: '" + notifyBackURI + "', timeout: '" + timeout + "').");
                     }
                 }
             }
         }
 
         public void startExpiryTimeout() {
             startExpiryTimeout(null, sequenceNumber);
         }
 
         public void startExpiryTimeout(final NotifyBackURI notifyBackURI, final int sequenceNumber) {
             if (expiryTimeout == null) {
                 if (LOGGER.isLoggable(Level.FINE)) {
                     LOGGER.log(
                         Level.FINE,
                         "Start expiry timeout for PushID '" + id + "' (" +
                                 "URI: '" + notifyBackURI + "', " +
                                 "timeout: '" + (notifyBackURI == null ? pushIdTimeout : cloudPushIdTimeout) + "', " +
                                 "sequence number: '" + sequenceNumber + "'" +
                         ").\r\n\r\n" +
                             "Confirmation Timeout Counter        : " +
                                 confirmationTimeoutCounter.get() + "\r\n" +
                             "Global Confirmation Timeout Counter : " +
                                 globalConfirmationTimeoutCounter.get() + "\r\n" +
                             "Expiry Timeout Counter              : " +
                                 expiryTimeoutCounter.incrementAndGet() + "\r\n" +
                             "Global Expiry Timeout Counter       : " +
                                 globalExpiryTimeoutCounter.incrementAndGet() + "\r\n");
                 }
                 expiryTimeoutSequenceNumber = sequenceNumber;
                try {
                    timeoutTimer.schedule(
                        expiryTimeout = newExpiryTimeout(notifyBackURI),
                        notifyBackURI == null ? pushIdTimeout : cloudPushIdTimeout);
                } catch (final IllegalStateException exception) {
                    // timeoutTimer was cancelled or its timer thread terminated.
                }
             } else {
                 if (LOGGER.isLoggable(Level.FINE)) {
                     LOGGER.log(
                         Level.FINE,
                         "Expiry timeout already scheduled for PushID '" + id + "' (" +
                             "URI: '" + notifyBackURI + "', " +
                             "timeout: '" + (notifyBackURI == null ? pushIdTimeout : cloudPushIdTimeout) + "'" +
                         ").");
                 }
             }
         }
 
 //        public void touch() {
 //            touch(System.currentTimeMillis());
 //        }
 //
 //        public void touch(final Long timestamp) {
 //            lastAccess = timestamp;
 //        }
 //
 //        public void touchIfMatching(Set pushIDs) {
 //            if (pushIDs.contains(id)) {
 //                touch();
 //                pushIDTouched(id, lastAccess);
 //            }
 //        }
         protected ConfirmationTimeout newConfirmationTimeout(final NotifyBackURI notifyBackURI, final long timeout) {
             return new ConfirmationTimeout(this, notifyBackURI, timeout);
         }
 
         protected ExpiryTimeout newExpiryTimeout(final NotifyBackURI notifyBackURI) {
             return new ExpiryTimeout(this, notifyBackURI);
         }
     }
 
     private class Notification implements Runnable {
         protected final String groupName;
         protected final Set<String> exemptPushIDSet = new HashSet<String>();
 
         public Notification(String groupName) {
             this.groupName = groupName;
         }
 
         public Notification(final String groupName, final PushConfiguration config) {
             this.groupName = groupName;
             Set pushIDSet = (Set)config.getAttributes().get("pushIDSet");
             if (pushIDSet != null) {
                 this.exemptPushIDSet.addAll(pushIDSet);
             }
         }
 
         public void run() {
             try {
                 Group group = groupMap.get(groupName);
                 if (group != null) {
                     if (LOGGER.isLoggable(Level.FINE)) {
                         LOGGER.log(Level.FINE, "Push Notification triggered for Push Group '" + groupName + "'.");
                     }
                     List<String> pushIDList = new ArrayList(Arrays.asList(group.getPushIDs()));
                     pushIDList.removeAll(exemptPushIDSet);
                     pendingNotifications.addAll(pushIDList);
                     outboundNotifier.broadcast(pushIDList.toArray(new String[pushIDList.size()]));
                     pushed(groupName);
                 }
             } finally {
                 scanForExpiry();
             }
         }
     }
 
     private class OutOfBandNotification extends Notification {
         private final PushConfiguration config;
 
         public OutOfBandNotification(String groupName, PushConfiguration config) {
             super(groupName, config);
             this.config = config;
         }
 
         public void run() {
             Group group = groupMap.get(groupName);
             String[] pushIDs = STRINGS;
             if (group != null) {
                 pushIDs = group.getPushIDs();
             }
             for (final String pushID : pushIDs) {
                 pushIDMap.get(pushID).pushConfiguration = config;
             }
             super.run();
         }
     }
 
     private class QueueConsumerTask extends TimerTask {
         private boolean running = true;
 
         public void run() {
             try {
                 //take tasks from the queue and execute them serially
                 while (running) {
                     try {
                         queue.take().run();
                     } catch (InterruptedException e) {
                         LOGGER.log(Level.FINE, "Notification queue draining interrupted.");
                     } catch (Throwable t)  {
                         LOGGER.log(Level.WARNING, "Notification queue encountered ", t);
                     }
                 }
             } catch (Exception exception) {
                 if (LOGGER.isLoggable(Level.WARNING)) {
                     LOGGER.log(
                         Level.WARNING, "Exception caught on " + this.getClass().getName() + " TimerTask.", exception);
                 }
             }
         }
 
         public boolean cancel() {
             running = false;
             queue.offer(NOOP);//add noop to unblock the queue
             return super.cancel();
         }
     }
 
     protected class ConfirmationTimeout
     extends TimerTask {
         protected final PushID pushID;
         protected final NotifyBackURI notifyBackURI;
         protected final long timeout;
 
         protected ConfirmationTimeout(final PushID pushID, final NotifyBackURI notifyBackURI, final long timeout) {
             this.pushID = pushID;
             this.notifyBackURI = notifyBackURI;
             this.timeout = timeout;
         }
 
         @Override
         public void run() {
             if (LOGGER.isLoggable(Level.FINE)) {
                 LOGGER.log(
                     Level.FINE,
                     "Confirmation timeout occurred for PushID '" + pushID.getID() + "' " +
                         "(URI: '" + notifyBackURI + "', timeout: '" + timeout + "').");
             }
             try {
                 if (notifyBackURI != null) {
                     park(pushID.getID(), notifyBackURI);
                 }
                 NotifyBackURI notifyBackURI = parkedPushIDs.get(pushID.getID());
                 if (notifyBackURI != null &&
                     notifyBackURI.getTimestamp() + minCloudPushInterval <=
                         System.currentTimeMillis()) {
 
                     if (LOGGER.isLoggable(Level.FINE)) {
                         LOGGER.log(Level.FINE, "Cloud Push dispatched for PushID '" + pushID.getID() + "'.");
                     }
                     notifyBackURI.touch();
                     getOutOfBandNotifier().broadcast(
                         (PushNotification)pushID.pushConfiguration,
                         new String[] {
                             notifyBackURI.getURI()
                         });
                 }
                 pushID.cancelConfirmationTimeout();
             } catch (Exception exception) {
                 if (LOGGER.isLoggable(Level.WARNING)) {
                     LOGGER.log(
                         Level.WARNING,
                         "Exception caught on confirmationTimeout TimerTask.",
                         exception);
                 }
             }
         }
     }
 
     protected class ExpiryTimeout
     extends TimerTask {
         protected final PushID pushID;
         protected final NotifyBackURI notifyBackURI;
 
         protected ExpiryTimeout(final PushID pushID, final NotifyBackURI notifyBackURI) {
             this.pushID = pushID;
             this.notifyBackURI = notifyBackURI;
         }
 
         @Override
         public void run() {
             if (LOGGER.isLoggable(Level.FINE)) {
                 LOGGER.log(
                     Level.FINE,
                     "Expiry timeout occurred for PushID '" + pushID.getID() + "' (" +
                         "URI: '" + notifyBackURI + "', " +
                         "timeout: '" +
                             (notifyBackURI == null ? pushIdTimeout : cloudPushIdTimeout) + "'" +
                     ").");
             }
             try {
                 pushID.discard();
                 pushID.cancelExpiryTimeout();
             } catch (Exception exception) {
                 if (LOGGER.isLoggable(Level.WARNING)) {
                     LOGGER.log(
                         Level.WARNING,
                         "Exception caught on expiryTimeout TimerTask.",
                         exception);
                 }
             }
         }
     }
 }
