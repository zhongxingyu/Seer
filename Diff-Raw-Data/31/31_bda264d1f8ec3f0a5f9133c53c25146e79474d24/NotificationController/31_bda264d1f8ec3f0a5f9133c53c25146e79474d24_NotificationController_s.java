 /*
  * Funambol is a mobile platform developed by Funambol, Inc.
  * Copyright (C) 2011 Funambol, Inc.
  * 
  * This program is free software; you can redistribute it and/or modify it under
  * the terms of the GNU Affero General Public License version 3 as published by
  * the Free Software Foundation with the addition of the following permission
  * added to Section 15 as permitted in Section 7(a): FOR ANY PART OF THE COVERED
  * WORK IN WHICH THE COPYRIGHT IS OWNED BY FUNAMBOL, FUNAMBOL DISCLAIMS THE
  * WARRANTY OF NON INFRINGEMENT  OF THIRD PARTY RIGHTS.
  *
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
  * details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with this program; if not, see http://www.gnu.org/licenses or write to
  * the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
  * MA 02110-1301 USA.
  * 
  * You can contact Funambol, Inc. headquarters at 643 Bair Island Road, Suite
  * 305, Redwood City, CA 94063, USA, or at email address info@funambol.com.
  *
  * The interactive user interfaces in modified source and object code versions
  * of this program must display Appropriate Legal Notices, as required under
  * Section 5 of the GNU Affero General Public License version 3.
  *
  * In accordance with Section 7(b) of the GNU Affero General Public License
  * version 3, these Appropriate Legal Notices must retain the display of the
  * "Powered by Funambol" logo. If the display of the logo is not reasonably
  * feasible for technical reasons, the Appropriate Legal Notices must display
  * the words "Powered by Funambol".
  */
 
 package com.funambol.client.controller;
 
 import com.funambol.client.localization.Localization;
 import com.funambol.client.ui.DisplayManager;
import com.funambol.client.ui.Screen;
 
 /**
  * A controller for a "background" notification, i.e. a notification not
  * linked with the graphic interface of the application.
  * This class is just a controller. Refer to DisplayManager
  * implementation in order to manage everything.
  */
 public class NotificationController {
 
     /** TAG to be displayed into log messages*/
     private static final String TAG_LOG = "NotificationController";
     
     private static final int NOTIFICATION_ID_SERVER_FULL = 10;
     private static final int NOTIFICATION_ID_CLIENT_FULL = 11;
 
     /** Last notification shown (used by automatic tests) */
     private NotificationData lastNotification = null;
     
     //--- Local instance fields fed by the constructor
     private Localization localization;
     private DisplayManager displayManager;
 
     /**
      * Public constructor
     * @param displayManager the DisplayManager object to be used.
      * @param controller
      */
     public NotificationController(DisplayManager displayManager, Controller controller) {
         this.displayManager = displayManager;
         this.localization = controller.getLocalization();
     }
     
     /**
      * Display a notification when server has no more available storage for media upload
      */
    public void showNotificationServerFull(Screen screen){
         //creates notification data
         //TODO find a way to put android class to call
         NotificationData notificationData = NotificationData.Factory.create(
                 NOTIFICATION_ID_SERVER_FULL,
                 NotificationData.SEVERITY_WARNING,
                 localization.getLanguage("notification_online_quota_full_server_ticker"),
                 localization.getLanguage("notification_online_quota_full_server_title"),
                 localization.getLanguage("notification_online_quota_full_server_message"),
                 null);
        showNotification(screen, notificationData);
     }
     
     /**
      * Display a notification when server has no more available storage for media upload
      */
    public void showNotificationClientFull(Screen screen){
         //creates notification data
         //TODO find a way to put android class to call
         NotificationData notificationData = NotificationData.Factory.create(
                 NOTIFICATION_ID_CLIENT_FULL,
                 NotificationData.SEVERITY_WARNING,
                 localization.getLanguage("notification_storage_full_device_ticker"),
                 localization.getLanguage("notification_storage_full_device_title"),
                 localization.getLanguage("notification_storage_full_device_message"),
                 null);
        showNotification(screen, notificationData);
     }
     
    private void showNotification(Screen screen, NotificationData notificationData) {
         lastNotification = notificationData;
        displayManager.showNotification(screen, notificationData);
     }
     
     /**
      * Returns the last notification shown.
      * 
      * @return a NotificationData instance or null if no notification was shown since
      *         the controller was created
      * 
      */
     public NotificationData getLastNotification() {
         return lastNotification;
     }
 }
