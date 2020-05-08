 package org.icepush;
 
 import java.util.HashSet;
 import java.util.Set;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 public class Group {
     private static final Logger LOGGER = Logger.getLogger(Group.class.getName());
 
     private final long groupTimeout;
     private final LocalPushGroupManager localPushGroupManager;
     private final String name;
     private final Set<String> pushIdList = new HashSet<String>();
 
     private long lastAccess = System.currentTimeMillis();
 
     protected Group(
         final String name, final String firstPushId, final long groupTimeout,
         final LocalPushGroupManager localPushGroupManager) {
 
         this.name = name;
         this.groupTimeout = groupTimeout;
         this.localPushGroupManager = localPushGroupManager;
         addPushID(firstPushId);
         if (LOGGER.isLoggable(Level.FINE)) {
             LOGGER.log(Level.FINE, "Push Group '" + this.name + "' created.");
         }
     }
 
     public String getName() {
         return name;
     }
 
     void addPushID(final String pushId) {
         pushIdList.add(pushId);
     }
 
     void discardIfExpired() {
         //expire group
         if (lastAccess + groupTimeout < System.currentTimeMillis()) {
             if (LOGGER.isLoggable(Level.FINE)) {
                 LOGGER.log(Level.FINE, "Push Group '" + name + "' expired.");
             }
             localPushGroupManager.removeGroup(name);
             localPushGroupManager.removePendingNotifications(pushIdList);
             for (String id : pushIdList) {
                 PushID pushID = localPushGroupManager.getPushID(id);
                 if (pushID != null) {
                     pushID.removeFromGroup(name);
                 }
             }
         }
     }
 
     String[] getPushIDs() {
         return pushIdList.toArray(new String[pushIdList.size()]);
     }
 
     public void removePushID(final String pushId) {
         pushIdList.remove(pushId);
         if (pushIdList.isEmpty()) {
             if (LOGGER.isLoggable(Level.FINE)) {
                 LOGGER.log(
                     Level.FINE, "Disposed Push Group '" + name + "' since it no longer contains any PushIDs.");
             }
             localPushGroupManager.removeGroup(name);
         }
     }
 
     private void touch() {
         touch(System.currentTimeMillis());
     }
 
     private void touch(final Long timestamp) {
         lastAccess = timestamp;
     }
 
    void touchIfMatching(final Set<String> pushIDSet) {
        for (final String _pushID : pushIDSet) {
            if (pushIdList.contains(_pushID)) {
                 touch();
                 localPushGroupManager.groupTouched(name, lastAccess);
                 //no need to touchIfMatching again
                 //return right away without checking the expiration
                 return;
             }
         }
     }
 }
