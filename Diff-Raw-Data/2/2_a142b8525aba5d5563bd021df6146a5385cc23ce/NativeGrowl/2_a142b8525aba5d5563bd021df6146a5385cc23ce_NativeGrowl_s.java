 /*
  * Copyright (C) 2009 the original author or authors.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.sonatype.grrrowl.impl;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.sonatype.grrrowl.Growl;
 import org.sonatype.grrrowl.impl.hawtjni.Foundation;
 import org.sonatype.grrrowl.impl.hawtjni.ID;
 import org.sonatype.grrrowl.impl.hawtjni.NSConstants;
 import org.sonatype.grrrowl.impl.hawtjni.Selector;
 
 /**
  * Native (via <a href="https://jna.dev.java.net/">JNA</a>) implementation of {@link Growl}.
  *
  * This code comes from <a href="http://www.jetbrains.org/display/IJOS/Home">Intellij IDEA Community Edition</a>
  * and has been massaged slightly.
  * 
  * @author spleaner
  * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
  * @since 1.0
  */
 public class NativeGrowl
     implements Growl, NSConstants
 {
     private static final String GROWL_APPLICATION_REGISTRATION_NOTIFICATION = "GrowlApplicationRegistrationNotification";
 
     private static final String GROWL_APP_NAME = "ApplicationName";
 
     private static final String GROWL_APP_ICON = "ApplicationIcon";
 
     private static final String GROWL_DEFAULT_NOTIFICATIONS = "DefaultNotifications";
 
     private static final String GROWL_ALL_NOTIFICATIONS = "AllNotifications";
 
     private static final String GROWL_NOTIFICATION_NAME = "NotificationName";
 
     private static final String GROWL_NOTIFICATION_TITLE = "NotificationTitle";
 
     private static final String GROWL_NOTIFICATION_DESCRIPTION = "NotificationDescription";
 
     private static final String GROWL_NOTIFICATION = "GrowlNotification";
 
     private static final Logger log = LoggerFactory.getLogger(NativeGrowl.class);
 
     private String appName;
 
     private String[] allNotifications;
 
     private String[] enabledNotifications;
 
     public NativeGrowl(final String appName) {
         assert appName != null;
         this.appName = appName;
         Foundation.load();
     }
 
     public boolean isGrowlRunning() {
         // FIXME: Need to either drop the native impl muck, or figure out how to properly implement stuff 
         return true;
     }
 
     public void register() {
         final ID autoReleasePool = createAutoReleasePool();
         final ID applicationIcon = getApplicationIcon();
         final ID defaultNotifications = fillArray(this.enabledNotifications);
         final ID allNotifications = fillArray(this.allNotifications);
         final ID userDict = createDict(new String[]{
                 GROWL_APP_NAME, GROWL_APP_ICON, GROWL_DEFAULT_NOTIFICATIONS, GROWL_ALL_NOTIFICATIONS
             },
             new Object[]{
                 appName, applicationIcon, defaultNotifications, allNotifications
             });
 
         final ID center = invoke(NSDISTRIBUTED_NOTIFICATION_CENTER, "defaultCenter");
         final ID notificationName = Foundation.cfString(GROWL_APPLICATION_REGISTRATION_NOTIFICATION);
         invoke(center, "postNotificationName:object:userInfo:deliverImmediately:", notificationName, null, userDict, true);
         invoke(autoReleasePool, "release");
     }
 
     public void notifyGrowlOf(final String notification, final String title, final String description) {
         log.debug("Notifying growl of: {} {} {}", new Object[] {notification, title, description});
 
         final ID autoReleasePool = createAutoReleasePool();
         final ID dict = createDict(new String[]{
                 GROWL_NOTIFICATION_NAME, GROWL_NOTIFICATION_TITLE, GROWL_NOTIFICATION_DESCRIPTION, GROWL_APP_NAME
             },
             new Object[]{
                 notification, title, description, appName
             });
 
         final ID center = invoke(NSDISTRIBUTED_NOTIFICATION_CENTER, "defaultCenter");
         final ID notificationName = Foundation.cfString(GROWL_NOTIFICATION);
 
         invoke(center, "postNotificationName:object:userInfo:deliverImmediately:", notificationName, null, dict, true);
         invoke(autoReleasePool, "release");
     }
 
     public void setAllowedNotifications(final String... notifications) {
         assert notifications != null;
         this.allNotifications = notifications;
     }
 
     public void setEnabledNotifications(final String... notifications) {
         assert notifications != null;
         this.enabledNotifications = notifications;
     }
 
     private static ID createAutoReleasePool() {
         return invoke(NSAUTORELEASE_POOL, "new");
     }
 
     private static ID fillArray(final Object[] a) {
         final ID result = invoke(NSMUTABLE_ARRAY, "array");
         for (Object s : a) {
             invoke(result, "addObject:", convertType(s));
         }
         return result;
     }
 
     private static ID createDict(final String[] keys, final Object[] values) {
         assert keys != null;
         assert values != null;
        return invoke(NSDICTIONARY, "dictionaryWithObjects:forKeys:", fillArray(keys), fillArray(values));
     }
 
 
     private static ID convertType(final Object o) {
         if (o instanceof ID) {
             return ((ID) o);
         }
         else if (o instanceof String) {
             return Foundation.cfString((String) o);
         }
         else {
             throw new IllegalArgumentException("Unsupported type: " + o.getClass());
         }
     }
     
     //
     // TODO: Allow the icon to be given
     //
 
     private static ID getApplicationIcon() {
         final ID sharedApp = invoke(NSAPPLICATION, "sharedApplication");
         final ID nsImage = invoke(sharedApp, "applicationIconImage");
         return invoke(nsImage, "TIFFRepresentation");
     }
 
     private static ID invoke(final String className, final String selector) {
         return invoke(Foundation.getClass(className), selector);
     }
     private static ID invoke(final ID id, final String selector) {
         return invoke(id, Foundation.createSelector(selector));
     }    
     private static ID invoke(final ID id, final Selector selector) {
         assert id != null;
         assert selector != null;
         return Foundation.invoke(id, selector);
     }
 
     private ID invoke(ID id, String selector, ID arg1, ID arg2, ID arg3, boolean arg4) {
         assert id != null;
         assert selector != null;
         return Foundation.invoke(id, Foundation.createSelector(selector), arg1, arg2, arg3, arg4);
     }
     private static ID invoke(ID id, String selector, ID arg1) {
         assert id != null;
         assert selector != null;
         return Foundation.invoke(id, Foundation.createSelector(selector), arg1);
     }
     private static ID invoke(String id, String selector, ID arg1, ID arg2) {
         assert id != null;
         assert selector != null;
         return Foundation.invoke(Foundation.getClass(id), Foundation.createSelector(selector), arg1, arg2);
     }
 }
