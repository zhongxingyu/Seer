 /*
  * Funambol is a mobile platform developed by Funambol, Inc.
  * Copyright (C) 2009 Funambol, Inc.
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
 
 import java.util.Enumeration;
 import java.util.Date;
 
 import com.funambol.client.customization.Customization;
 import com.funambol.client.source.AppSyncSource;
 import com.funambol.client.source.AppSyncSourceConfig;
 import com.funambol.client.source.AppSyncSourceManager;
 import com.funambol.client.ui.UISyncSource;
 import com.funambol.client.ui.Bitmap;
 import com.funambol.client.localization.Localization;
 import com.funambol.util.Log;
 import com.funambol.sync.SyncItem;
 import com.funambol.sync.SyncSource;
 import com.funambol.sync.SyncListener;
 import com.funambol.sync.SyncReport;
 import com.funambol.syncml.protocol.DevInf;
 
 
 public class UISyncSourceController implements SyncListener {
 
     private static final String TAG_LOG = "UISyncSourceController";
 
     private Localization         localization = null;
     private Customization        customization = null;
     private AppSyncSourceManager appSyncSourceManager = null;
     private Controller           controller = null;
     private UISyncSource         uiSource   = null;
     private AppSyncSource        appSource  = null;
 
     private int              totalSent;
     private int              totalSending;
     private int              totalReceived;
     private int              totalReceiving;
 
     private long             currentSendingItemSize = 0;
     private long             currentReceivingItemSize = 0;
 
     private Bitmap           statusIcon = null;
     private Bitmap           statusSelectedIcon = null;
     private Bitmap           okIcon = null;
     private Bitmap           errorIcon = null;
 
     private SyncingAnimation animation = null;
     private SyncReport       lastSyncReport = null;
 
     private boolean          cancelling = false;
     private boolean          syncing    = false;
 
     private long             syncStartedTimestamp = 0;
 
     // The progress is computed as follow: 50% for the connecting/sending phase
     // and 50% for the receiving/mapping phase
     // Connecting and mapping counts for one item
     private int              currentStep = 0;
 
     public UISyncSourceController(Customization customization, Localization localization,
                                   AppSyncSourceManager appSyncSourceManager,
                                   Controller controller, AppSyncSource appSource)
     {
         init(customization, localization, appSyncSourceManager, controller, appSource);
     }
 
     public UISyncSourceController() {
     }
 
     public void init(Customization customization, Localization localization,
                      AppSyncSourceManager appSyncSourceManager,
                      Controller controller, AppSyncSource appSource)
     {
         this.customization = customization;
         this.localization  = localization;
         this.appSyncSourceManager = appSyncSourceManager;
         this.controller = controller;
         this.appSource = appSource;
 
         okIcon = customization.getOkIcon();
         errorIcon = customization.getErrorIcon();
         statusSelectedIcon = customization.getStatusSelectedIcon();
 
         // Create the animation object (depends on the customization, so this
         // can only be invoked after the customization has been set)
         animation = new SourceSyncingAnimation();
     }
 
     public void setUISyncSource(UISyncSource uiSource) {
         this.uiSource = uiSource;
         if (uiSource != null) {
             String lastStatus;
 
             if (!appSource.isWorking()) {
                 lastStatus = localization.getLanguage("home_not_available");
                 uiSource.setEnabled(false);
             } else if (!appSource.getConfig().getEnabled()) {
                 lastStatus = localization.getLanguage("home_disabled");
                 uiSource.setEnabled(false);
             } else if (!appSource.getConfig().getAllowed()) {
                 lastStatus = localization.getLanguage("home_not_allowed");
                 uiSource.setEnabled(true);
                uiSource.setAllowed(false);
             } else {
                 int status = appSource.getConfig().getLastSyncStatus();
                 if (status == SyncListener.COMPRESSED_RESPONSE_ERROR) {
                     return;
                 }
                 lastStatus = getLastSyncStatus(status, null);
                 statusIcon   = getLastSyncIcon(status);
                 if (statusIcon != null) {
                     uiSource.setStatusIcon(statusIcon);
                 }
                 uiSource.setEnabled(true);
             }
             uiSource.setStatusString(lastStatus);
             uiSource.redraw();
         }
     }
 
     public boolean isSyncing() {
         return syncing;
     }
 
     public void disableStatusAnimation() {
         if(animation != null) {
             animation.stopAnimation();
         }
         animation = null;
     }
 
     public void enableStatusAnimation() {
         if(animation == null) {
             animation = new SourceSyncingAnimation();
         }
     }
 
     /*
      * (non-Javadoc)
      * @see com.funambol.util.SyncListener#endConnecting(int)
      */
     public void endConnecting(int action) {
     }
 
     public void refreshStatus() {
         AppSyncSourceConfig appConfig = appSource.getConfig();
         int status = appConfig.getLastSyncStatus();
         if(uiSource != null) {
             String lastStatus = getLastSyncStatus(status, lastSyncReport);
             uiSource.setStatusString(lastStatus);
         }
     }
 
 
     /*
      * (non-Javadoc)
      * @see com.funambol.util.SyncListener#endFinalizing()
      */
     public void endFinalizing() {
         if (uiSource != null) {
             if (!cancelling) {
                 uiSource.setStatusString(localization.getLanguage("status_mapping_done"));
                 uiSource.redraw();
             }
         }
     }
 
     /*
      * (non-Javadoc)
      * @see com.funambol.util.SyncListener#endReceiving()
      */
     public void endReceiving() {
     }
 
     /*
      * (non-Javadoc)
      * @see com.funambol.util.SyncListener#endSending()
      */
     public void endSending() {
     }
 
     /**
      * Disable the source to receive input events
      */
     public void disable() {
         if (uiSource != null) {
             String status;
             if (!appSource.isWorking()) {
                 status = localization.getLanguage("home_not_available");
             } else {
                 status = localization.getLanguage("home_disabled");
             }
             uiSource.setStatusString(status);
             Bitmap sourceIcon = customization.getSourceDisabledIcon(appSource.getId());
             if (sourceIcon != null) {
                 uiSource.setIcon(sourceIcon);
             }
             uiSource.setStatusIcon(null);
             uiSource.setEnabled(false);
             uiSource.setAllowed(appSource.getConfig().getAllowed());
             uiSource.redraw();
         }
     }
 
     /**
      * Enable the source to receive input events
      */
     public void enable() {
         if (uiSource != null) {
             AppSyncSource appSource = uiSource.getSource();
             int status = appSource.getConfig().getLastSyncStatus();
             if (status==SyncListener.COMPRESSED_RESPONSE_ERROR) {
                 return;
             }
 
             String statusMsg;
             if (appSource.getConfig().getAllowed()) {
                 statusMsg = getLastSyncStatus(status, null);
             } else {
                 statusMsg = localization.getLanguage("home_not_allowed");
             }
             uiSource.setStatusString(statusMsg);
             Bitmap sourceIcon = customization.getSourceIcon(appSource.getId());
             if (sourceIcon != null) {
                 uiSource.setIcon(sourceIcon);
             }
             statusIcon = getLastSyncIcon(status);
             if (statusIcon != null) {
                 uiSource.setStatusIcon(statusIcon);
             }
             uiSource.setEnabled(true);
             uiSource.setAllowed(appSource.getConfig().getAllowed());
             uiSource.redraw();
         }
     }
 
     /*
      * (non-Javadoc)
      * @see com.funambol.util.SyncListener#endSession(SyncReport)
      */
     public void endSession(SyncReport report) {
 
         if (!syncing) {
             return;
         }
 
         if (Log.isLoggable(Log.TRACE)) {
             Log.trace(TAG_LOG, "endSession");
         }
 
         lastSyncReport = report;
         int status = report.getStatusCode();
 
         if (Log.isLoggable(Log.INFO)) {
             Log.info(TAG_LOG, report.toString());
         }
 
         // Stop any animation in progress
         if (animation != null) {
             animation.stopAnimation();
         }
         
         //The following condition is made to trap the compression error when a 
         //wap compression error occur. 
         //Notice that this change introduce a dependency on the class SyncEngine 
         //and it can happen that the status is not correctly update the http 
         //compression is disabled.
         if (status==SyncListener.COMPRESSED_RESPONSE_ERROR) {
             //This error is the result for a problem reading the compressed 
             //stream. In this case the sync client retries to send
             //an uncompressed request
             Log.error(TAG_LOG, "Compressed Header Error");
             return;
         }
 
         // set the status into the app source
         appSource.getConfig().setLastSyncStatus(status);
         appSource.getConfig().setLastSyncTimestamp(syncStartedTimestamp);
         appSource.getConfig().commit();
 
         // This source sync is over, set the proper status
         if (uiSource != null) {
             String statusMsg = getLastSyncStatus(status, report);
             statusIcon   = getLastSyncIcon(status);
             uiSource.setStatusString(statusMsg);
             if (statusIcon != null) {
                 uiSource.setStatusIcon(statusIcon);
             }
             uiSource.syncEnded();
         }
        
         if (uiSource != null) {
             uiSource.redraw();
         }
         cancelling = false;
         syncing = false;
 
         // Reset the overall status
         resetInternalStatus();
     }
     
     /**
      * Resets the current status
      */
     public void resetStatus() {
 
         // Stop any animation in progress
         if (animation != null) {
             animation.stopAnimation();
         }
         int status = appSource.getConfig().getLastSyncStatus();
         String lastStatus = getLastSyncStatus(status, null);
         statusIcon = getLastSyncIcon(status);
         if (uiSource != null) {
             uiSource.setStatusIcon(statusIcon);
             uiSource.setStatusString(lastStatus);
             uiSource.syncEnded();
             uiSource.redraw();
         }
 
         cancelling = false;
         syncing = false;
     }
 
     public void setSelected(boolean value, boolean fromUi) {
         if (uiSource != null && !cancelling) {
             // Sets the proper icon (if the source is enabled)
             if (appSource.getConfig().getEnabled()) {
                 if (customization.showSyncIconOnSelection()) {
                     if (value) {
                         uiSource.setStatusIcon(statusSelectedIcon);
                     } else {
                         uiSource.setStatusIcon(statusIcon);
                     }
                 } else {
                     uiSource.setStatusIcon(statusIcon);
                 }
             }
             uiSource.setSelection(value, fromUi);
             uiSource.redraw();
         }
     }
 
     /*
      * (non-Javadoc)
      * @see com.funambol.util.SyncListener#endSyncing()
      */
     public void endSyncing() {
         // We force the progress bar to the end so that also empty syncs move
         // the bar
         if (uiSource != null) {
             updateCurrentProgress(100);
         }
     }
 
     /*
      * (non-Javadoc)
      */
     public void itemAddSendingEnded(String key, String parent) {
     }
 
     public void itemAddSendingStarted(String key, String parent, long size) {
         startSending(key, size);
     }
 
     public void itemAddSendingProgress(String key, String parent, long size) {
         sentProgress(key, size);
     }
 
     public void itemAddReceivingEnded(String key, String parent) {
     }
 
     public void itemAddReceivingStarted(String key, String parent, long size) {
         startReceiving(key, size);
     }
 
     public void itemAddReceivingProgress(String key, String parent, long size) {
         receivedProgress(key, size);
     }
 
     /*
      * (non-Javadoc)
      * @see com.funambol.util.SyncListener#itemDeleteSent(java.lang.Object)
      */
     public void itemDeleteSent(SyncItem syncItem) {
         startSending(syncItem.getKey(), 0);
     }
 
     /*
      * (non-Javadoc)
      * @see com.funambol.util.SyncListener#itemDeleted(java.lang.Object)
      */
     public void itemDeleted(SyncItem item) {
         startReceiving(item.getKey(), 0);
     }
 
     /*
      * (non-Javadoc)
      * @see com.funambol.util.SyncListener#itemReplaceSent(java.lang.Object)
      */
     public void itemReplaceSendingStarted(String key, String parent, long size) {
         startSending(key, size);
     }
 
     public void itemReplaceSendingEnded(String key, String parent) {
         currentStep++;
         updateCurrentProgress();
     }
 
     public void itemReplaceSendingProgress(String key, String parent, long size) {
         sentProgress(key, size);
     }
 
     /*
      * (non-Javadoc)
      * @see com.funambol.util.SyncListener#itemReplaceReceivingStarted(java.lang.Object)
      */
     public void itemReplaceReceivingStarted(String key, String parent, long size) {
         startReceiving(key, size);
     }
 
     public void itemReplaceReceivingEnded(String key, String parent) {
         currentStep++;
         updateCurrentProgress();
     }
 
     public void itemReplaceReceivingProgress(String key, String parent, long size) {
         receivedProgress(key, size);
     }
 
     /*
      * (non-Javadoc)
      * @see com.funambol.util.SyncListener#startConnecting()
      */
     public void startConnecting() {
         if (uiSource != null) {
             if (animation != null && !animation.isRunning()) {
                 animation.startAnimation();
             }
             if (!cancelling) {
                 uiSource.setStatusString(localization.getLanguage("status_connecting"));
                 uiSource.redraw();
             }
         }
     }
 
     /*
      * (non-Javadoc)
      * @see com.funambol.util.SyncListener#startFinalizing()
      */
     public void startFinalizing() {
         if (uiSource != null) {
             if (!cancelling) {
                 uiSource.setStatusString(localization.getLanguage("status_mapping"));
                 uiSource.redraw();
             }
         }
     }
 
     /*
      * (non-Javadoc)
      * @see com.funambol.util.SyncListener#startReceiving(int)
      */
     public void startReceiving(int numItems) {
         if (totalReceiving == ITEMS_NUMBER_UNKNOWN) {
             totalReceiving = numItems;
             currentStep = 0;
         }
     }
 
     /*
      * (non-Javadoc)
      * @see com.funambol.util.SyncListener#startSending(int, int, int)
      */
     public void startSending(int numNewItems, int numUpdItems, int numDelItems) {
         totalSending = numNewItems + numUpdItems + numDelItems;
         currentStep++;
     }
 
     /*
      * (non-Javadoc)
      * @see com.funambol.util.SyncListener#startSession()
      */
     public void startSession() {
         if (Log.isLoggable(Log.DEBUG)) {
             Log.debug(TAG_LOG, "startSession");
         }
         resetInternalStatus();
         // It is possible that this method gets invoked more than once. This is
         // the case because it is invoked by SyncManager but also by the
         // HomeScreenController.
         if (uiSource != null && !syncing) {
             uiSource.syncStarted();
         }
         syncStartedTimestamp = new Date().getTime();
         syncing = true;
     }
 
     public void attachToSession() {
         if (Log.isLoggable(Log.INFO)) {
             Log.info(TAG_LOG, "Attaching to session");
         }
         syncing = true;
         if (uiSource != null) {
             uiSource.syncStarted();
             String text = localization.getLanguage("status_connecting");
             uiSource.setStatusString(text);
             if (animation != null && !animation.isRunning()) {
                 animation.startAnimation();
             }
         }
     }
 
     /*
      * (non-Javadoc)
      * @see com.funambol.util.SyncListener#startSyncing(int, Object)
      */
     public boolean startSyncing(int mode, Object devInf) {
         // If the server sends its capabilities, we must decode them and update
         // the configuration accordingly
         if (devInf != null) {
             if (Log.isLoggable(Log.INFO)) {
                 Log.info(TAG_LOG, "Server sent its capabilities");
             }
             if (devInf instanceof DevInf) {
                 controller.reapplyServerCaps((DevInf)devInf);
             }
         }
 
         return true;
     }
 
     public void startCancelling() {
         if (uiSource != null) {
             uiSource.setStatusString(localization.getLanguage("status_cancelling"));
             uiSource.redraw();
         }
         cancelling = true;
     }
 
     /*
      * (non-Javadoc)
      * @see com.funambol.util.SyncListener#syncStarted(int)
      */
     public void syncStarted(int arg0) {
     }
 
     public void removingAllData() {
         if (uiSource != null) {
             if (animation != null && !animation.isRunning()) {
                 animation.startAnimation();
             }
             uiSource.setStatusString(localization.getLanguage("status_recover"));
             uiSource.redraw();
         }
     }
 
     public void itemRemoved(int current, int size) {
         StringBuffer sb = new StringBuffer(localization.getLanguage("status_removing_item"));
         sb.append(" ").append(current);
         sb.append("/").append(size);
         if (Log.isLoggable(Log.TRACE)) {
             Log.trace(TAG_LOG, "notifyRemoved " + sb.toString());
         }
         currentStep++;
         if (uiSource != null) {
             if (!cancelling) {
                 uiSource.setStatusString(sb.toString());
                 uiSource.redraw();
             }
             updateCurrentProgress();
         }
     }
     
     public Controller getController() {
         return controller;
     }
 
     public SyncReport getLastSyncReport() {
         return lastSyncReport;
     }
 
     public void setAnimationIcons(Bitmap[] icons) {
         if (animation != null) {
             animation.setAnimationIcons(icons);
         }
     }
 
     private void sentProgress(String key, long size) {
         
         StringBuffer sb = new StringBuffer(localization.getLanguage("status_sending_item"));
         sb.append(" ").append(totalSent);
 
         if (totalSending > 0) {
             sb.append("/").append(totalSending);
         }
 
         // This is a LO
         // Compute the percentage of what we have sent so far
         if(currentSendingItemSize > 0) {
             long perc = (size * 100) / currentSendingItemSize;
             if (perc > 100) {
                 perc = 100;
             }
             sb.append(" (").append(perc).append("%)");
         }
         if (uiSource != null) {
             if (!cancelling) {
                 uiSource.setStatusString(sb.toString());
                 uiSource.redraw();
             }
         }
     }
 
     private void receivedProgress(String key, long size) {
 
         StringBuffer sb = new StringBuffer(localization.getLanguage("status_receiving_item"));
         sb.append(" ").append(totalReceived);
 
         if (totalReceiving > 0) {
             sb.append("/").append(totalReceiving);
         }
 
         // This is a LO
         // Compute the percentage of what we have sent so far
         if(currentReceivingItemSize > 0) {
             long perc = (size * 100) / currentReceivingItemSize;
             if (perc > 100) {
                 perc = 100;
             }
             sb.append(" (").append(perc).append("%)");
         }
         if (uiSource != null) {
             if (!cancelling) {
                 uiSource.setStatusString(sb.toString());
                 uiSource.redraw();
             }
         }
     }
 
     private void startSending(String key, long size) {
         totalSent++;
         currentSendingItemSize = size;
 
         StringBuffer sb = new StringBuffer(localization.getLanguage("status_sending_item"));
         sb.append(" ").append(totalSent);
 
         if (totalSending > 0) {
             sb.append("/").append(totalSending);
         }
 
         if (uiSource != null) {
             if (!cancelling) {
                 uiSource.setStatusString(sb.toString());
                 uiSource.redraw();
             }
         }
     }
 
     private void startReceiving(String key, long size) {
         totalReceived++;
         currentReceivingItemSize = size;
 
         StringBuffer sb = new StringBuffer(localization.getLanguage("status_receiving_item"));
         sb.append(" ").append(totalReceived);
 
         if (totalReceiving > 0) {
             sb.append("/").append(totalReceiving);
         }
 
         if (uiSource != null) {
             if (!cancelling) {
                 uiSource.setStatusString(sb.toString());
                 uiSource.redraw();
             }
         }
     }
 
     private String createLastSyncedString(long anchor) {
     
         StringBuffer sb = new StringBuffer();
         long now = System.currentTimeMillis();
         long aday = 24 * 60 * 60 * 1000;
         long yesterday = now - aday;
 
         String todayDate = localization.getDate(now);
         String yesterdayDate = localization.getDate(yesterday);
         String anchorDate = localization.getDate(anchor);
 
         if (anchorDate.equals(todayDate)) {
             sb.append(localization.getLanguage("word_today"));
         } else if (anchorDate.equals(yesterdayDate)) {
             sb.append(localization.getLanguage("word_yesterday"));
         } else {
             sb.append(anchorDate);
         }
     
         sb.append(" ").append(localization.getLanguage("word_at")).append(" ");
     
         String time = localization.getTime(anchor);
         sb.append(time);
     
         return sb.toString();
     }
 
     private void abortSlow() {
     }
 
     private String getListOfSourceNames(Enumeration sourceNameList) {
         StringBuffer sourceNames = new StringBuffer();
 
         int x = 0;
         AppSyncSource appSource = (AppSyncSource)sourceNameList.nextElement();
 
         while(appSource != null) {
 
             String name = appSource.getName();
             appSource = (AppSyncSource)sourceNameList.nextElement();
 
             if (x > 0) {
                 sourceNames.append(", ");
                 if (appSource == null) {
                     sourceNames.append(localization.getLanguage("dialog_and").toLowerCase());
                 }
             }
 
             sourceNames.append(name);
         }
 
         return sourceNames.toString();
     }
 
     private String getLastSyncStatus(int status, SyncReport report) {
 
         if (!appSource.getConfig().getAllowed()) {
             return localization.getLanguage("home_not_allowed");
         }
 
         String res;
         switch (status) {
             case SyncListener.SUCCESS:
             {
                 SyncSource source = appSource.getSyncSource();
                 long lastSyncTS;
                 if (source != null) {
                     lastSyncTS = appSource.getConfig().getLastSyncTimestamp();
                 } else {
                     lastSyncTS = 0;
                 }
 
                 if (lastSyncTS > 0) {
                     res = localization.getLanguage("home_last_sync") + " "
                           + createLastSyncedString(lastSyncTS);
                 } else {
                     res = localization.getLanguage("home_unsynchronized");
                 }
                 break;
             }
             case SyncListener.INVALID_CREDENTIALS:
                 res = localization.getLanguage("status_invalid_credentials");
                 break;
             case SyncListener.FORBIDDEN_ERROR:
                 res = localization.getLanguage("status_forbidden_error");
                 break;
             case SyncListener.READ_SERVER_RESPONSE_ERROR:
             case SyncListener.WRITE_SERVER_REQUEST_ERROR:
             case SyncListener.CONN_NOT_FOUND:
                 if (report != null && (report.getReceivedItemsCount() > 0 || report.getSentItemsCount() > 0)) {
                     res = localization.getLanguage("status_partial_failure");
                 } else {
                     res = localization.getLanguage("status_network_error");
                 }
                 break;
             case SyncListener.CONNECTION_BLOCKED_BY_USER:
                 res = localization.getLanguage("status_connection_blocked");
                 break;
             case SyncListener.CANCELLED:
                 res = localization.getLanguage("status_cancelled");
                 break;
             case SyncListener.SERVER_FULL_ERROR:
                 res = localization.getLanguage("status_quota_exceeded");
                 break;
             case SyncListener.LOCAL_CLIENT_FULL_ERROR:
                 res = localization.getLanguage("status_no_space_on_device");
                 break;
             case SyncListener.NOT_SUPPORTED:
                 res = localization.getLanguage("status_not_supported");
                 break;
             case SyncListener.SD_CARD_UNAVAILABLE:
                 res = localization.getLanguage("status_sd_card_unavailable");
                 break;
             case SyncListener.PAYMENT_REQUIRED:
                 res = localization.getLanguage("status_payment_required");
                 break;
             default:
                 if (report != null && (report.getReceivedItemsCount() > 0 || report.getSentItemsCount() > 0)) {
                     res = localization.getLanguage("status_partial_failure");
                 } else {
                     res = localization.getLanguage("status_complete_failure");
                 }
                 break;
         }
         if (Log.isLoggable(Log.DEBUG)) {
             Log.debug(TAG_LOG, "getLastSyncStatus " + res);
         }
         return res;
     }
 
     private Bitmap getLastSyncIcon(int status) {
         Bitmap res;
         if (status == SyncListener.SUCCESS) {
             long lastSyncTS = appSource.getConfig().getLastSyncTimestamp();
             if (lastSyncTS > 0) {
                 res = okIcon;
             } else {
                 res = null;
             }
         } else {
             res = errorIcon;
         }
         return res;
     }
 
     protected void updateCurrentProgress() {
 
         if (uiSource != null) {
             // Update the total if new info is available
             int progress = 0;
 
             if (totalSending > 0 &&
                 totalReceiving == ITEMS_NUMBER_UNKNOWN)
             {
                 // We are in the sending phase
                 progress = (currentStep * 50) / totalSending;
                 if (progress > 50) {
                     progress = 50;
                 }
             }
 
             if (totalReceiving > 0) {
                 // We are in the receiving phase
                 progress = 50 + (currentStep * 50) / totalReceiving;
                 if (progress > 100) {
                     progress = 100;
                 }
             }
 
             // Update the uiSource
             uiSource.setProgress(progress);
         }
     }
 
     /**
      * This method forces the progress bar to a given value. Must be used
      * carefully to avoid the bar to move forward and backward.
      */
     protected void updateCurrentProgress(int percentage) {
         if (uiSource != null) {
             uiSource.setProgress(percentage);
         }
     }
 
     private class SourceSyncingAnimation extends SyncingAnimation {
 
         public SourceSyncingAnimation() {
             super(customization.getStatusIconsForAnimation());
         }
 
         protected void showBitmap(Bitmap bitmap) {
             uiSource.setStatusIcon(bitmap);
             uiSource.redraw();
         }
     }
 
     private void resetInternalStatus() {
         totalReceiving = ITEMS_NUMBER_UNKNOWN;
         totalReceived = 0;
         totalSent = 0;
         totalSending = 0;
         cancelling = false;
         // Reset the current step
         currentStep = 0;
         updateCurrentProgress();
     }
 
 }
 
