 /*
  * Funambol is a mobile platform developed by Funambol, Inc.
  * Copyright (C) 2008 Funambol, Inc.
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
 
 import java.util.Vector;
 import java.util.Enumeration;
 
 import com.funambol.client.controller.DialogController;
 import com.funambol.client.configuration.Configuration;
 import com.funambol.client.engine.SyncEngine;
 import com.funambol.client.engine.Poller;
 import com.funambol.client.engine.SyncEngineListener;
 import com.funambol.client.engine.AppSyncRequest;
 import com.funambol.client.source.AppSyncSource;
 import com.funambol.client.source.AppSyncSourceConfig;
 import com.funambol.client.source.AppSyncSourceManager;
 import com.funambol.client.customization.Customization;
 import com.funambol.client.localization.Localization;
 import com.funambol.client.ui.Screen;
 import com.funambol.client.push.SyncScheduler;
 import com.funambol.syncml.protocol.SyncML;
 import com.funambol.sync.SyncException;
 import com.funambol.sync.SyncSource;
 import com.funambol.sync.SourceConfig;
 import com.funambol.sync.SyncListener;
 import com.funambol.util.Log;
 import com.funambol.platform.NetworkStatus;
 import com.funambol.sapisync.source.JSONSyncSource;
 
 /**
  * This class provides a basic controller that can be used by any other
  * controller that needs synchronization support. This controller is just
  * a building block, it does not control any UI component. But it shall be
  * extended by controllers that need synchronization capabilities (e.g.
  * HomeScreenController and AccountScreenController).
  */
 public class SynchronizationController extends BasicSynchronizationController
         implements SyncEngineListener {
 
     private static final String TAG_LOG = "SynchronizationController";
 
     public static final int REFRESH_FROM_SERVER = 0;
     public static final int REFRESH_TO_SERVER   = 1;
 
     protected Controller controller;
     
     protected Customization customization;
 
    protected AppSyncSourceManager appSyncSourceManager;

     protected Localization localization;
 
     protected SyncEngine engine;
 
     protected boolean    doCancel        = false;
 
     protected AppSyncSource currentSource = null;
 
     protected boolean    showTCPAlert;
 
     protected boolean    logConnectivityError;
 
     private SyncScheduler  syncScheduler;
 
     private int            scheduledAttempt = 0;
 
     private Poller         retryPoller = null;
 
     private final AppSyncRequest appSyncRequestArr[] = new AppSyncRequest[1];
     private RequestHandler reqHandler;
 
     private int            RETRY_POLL_TIME = 1;
 
     SynchronizationController() {
         super(null,null,null,null);
     }
 
     SynchronizationController(Controller controller, Screen screen, NetworkStatus networkStatus) {
 
         super(controller, controller.getConfiguration(), controller.getAppSyncSourceManager(), screen);
         if (Log.isLoggable(Log.TRACE)) {
             Log.trace(TAG_LOG, "Initializing synchronization controller");
         }
 
         this.networkStatus = networkStatus;
         
         localization = controller.getLocalization();
         customization = controller.getCustomization();
 
         initSyncScheduler();
     }
 
     /**
      * TODO: Remove once the com.funambol.client.controller package integration is finished
      */
     SynchronizationController(Controller controller, Customization customization,
             Configuration configuration, Localization localization,
             AppSyncSourceManager appSyncSourceManager, Screen screen,
             NetworkStatus networkStatus) {
         
         super(controller, configuration, appSyncSourceManager, screen);
 
         if (Log.isLoggable(Log.TRACE)) {
             Log.trace(TAG_LOG, "Initializing synchronization controller");
         }
 
         this.networkStatus = networkStatus;
 
         this.localization = localization;
         this.customization = customization;
 
         initSyncScheduler();
     }
     
     protected void initSyncScheduler() {
         engine = createSyncEngine();
         syncScheduler = new SyncScheduler(engine);
         // The request handler is a daemon serving external requests
         reqHandler = new RequestHandler();
         reqHandler.start();
     }
 
 
     /**
      * Returns true iff a synchronization is in progress
      */
     public boolean isSynchronizing() {
         return engine.isSynchronizing();
     }
 
     /**
      * Returns the sync source currently being synchronized. If a sync is not
      * in progress, then null is returned. Please note that this method is not
      * completely equivalent to isSynchronizing. At the beginning of a sync,
      * isSynchronizing returns true, but getCurrentSource may return null until
      * the source is prepared for the synchronization.
      */
     public AppSyncSource getCurrentSource() {
         return engine.getCurrentSource();
     }
 
     /**
      * @return the current <code>SyncEngine</code> instance
      */
     public SyncEngine getSyncEngine() {
         return engine;
     }
 
     /**
      * Try to cancel the current sync. This works for cooperative sources that
      * check the synchronizationController status.
      */
     public void cancelSync() {
         if (Log.isLoggable(Log.TRACE)) {
             Log.trace(TAG_LOG, "Cancelling sync " + isSynchronizing() + " currentSource=" + currentSource);
         }
         setCancel(true);
 
         if (isSynchronizing() && currentSource != null) {
             UISyncSourceController uiSourceController = currentSource.getUISyncSourceController();
             if (uiSourceController != null) {
                 uiSourceController.startCancelling();
             }
             engine.cancelSync();
         }
     }
 
     /**
      * Perform a refresh for a set of sources and a given direction. The method
      * gets blocked until the sync terminates.
      *
      * @param mask the set of sources to sync
      * @param direction the refresh direction
      */
     public void refresh(int mask, int direction) {
 
         Enumeration sources = appSyncSourceManager.getEnabledAndWorkingSources();
         Vector syncSources = new Vector();
         while(sources.hasMoreElements()) {
             AppSyncSource appSource = (AppSyncSource)sources.nextElement();
             if ((appSource.getId() & mask) != 0) {
                 syncSources.addElement(appSource);
             }
         }
         refreshSources(syncSources, direction);
     }
 
     public synchronized void refreshSources(Vector syncSources, int direction) {
 
         if (isSynchronizing()) {
             return;
         }
         // A refresh is always a manual sync, force the sync type here
         forceSynchronization(MANUAL, syncSources, true, direction, 0, false);
     }
 
     public void syncEnded() {
 
         displayEndOfSyncWarnings();
        
         // TODO FIXME MARCO
         /*
         if(customization.enableUpdaterManager()){
             UpdaterManager upm = UpdaterManager.getInstance();
             upm.setController(controller);
             upm.check();
         }
         */
         
     }
 
     /**
      * Triggers a synchronization for the given syncSources. The caller can
      * specify its type (manual, scheduled, push) to change the error handling
      * behavior
      *
      * @param syncType the caller type (SYNC_TYPE_MANUAL, SYNC_TYPE_SCHEDULED)
      * @param syncSources is a vector of AppSyncSource to be synchronized
      *
      */
     public synchronized void synchronize(String syncType, Vector syncSources) {
         synchronize(syncType, syncSources, 0);
     }
 
     /**
      * Schedules a synchronization for the given syncSources. The sync is
      * scheduled in "delay" milliseconds from now. The caller can
      * specify its type (manual, scheduled, push) to change the error handling
      * behavior
      *
      * @param syncType the caller type (SYNC_TYPE_MANUAL, SYNC_TYPE_SCHEDULED)
      * @param syncSources is a vector of AppSyncSource to be synced
      * @param delay the interval at which the sync shall be performed (relative
      *              to now)
      *
      */
     public synchronized void synchronize(String syncType, Vector syncSources, int delay) {
         synchronize(syncType, syncSources, delay, false);
     }
 
     /**
      * Schedules a synchronization for the given syncSources. The sync is
      * scheduled in "delay" milliseconds from now. The caller can
      * specify its type (manual, scheduled, push) to change the error handling
      * behavior.
      * The caller can also specify it the sync request is generated outside of
      * the application. In such a case the handling is special and the
      * synchronization is actually performed by the Sync Client process. This
      * calls notifies the SyncClient to schedule a sync at the given interval.
      * This is useful when syncs are triggered on external events, such as
      * modification of PIM (c2s push).
      *
      * @param syncType the caller type (SYNC_TYPE_MANUAL, SYNC_TYPE_SCHEDULED)
      * @param syncSources is a vector of AppSyncSource to be synced
      * @param delay the interval at which the sync shall be performed (relative
      *              to now)
      * @param fromOutside specifies if the request is generated outside of the
      * application
      */
     public synchronized void synchronize(String syncType, Vector syncSources,
                                          int delay, boolean fromOutside) {
 
         if (Log.isLoggable(Log.INFO)) {
             Log.info(TAG_LOG, "synchronize " + syncType);
         }
 
         if (isSynchronizing()) {
             if (Log.isLoggable(Log.INFO)) {
                 Log.info(TAG_LOG, "A sync is already in progress");
             }
             return;
         }
         forceSynchronization(syncType, syncSources, false, 0, delay, fromOutside);
     }
 
     protected synchronized void forceSynchronization(String syncType, Vector syncSources,
                                                      boolean refresh, int direction,
                                                      int delay, boolean fromOutside)
     {
         // Search if at least one of the selected sources has a warning on the
         // first sync
         Vector sourcesWithQuestion = new Vector();
         syncSources = applyBandwidthSaver(syncSources, sourcesWithQuestion, syncType);
 
         // We cannot ask the question if there is no app visible 
         if (screen == null && sourcesWithQuestion.size() > 0) {
             AppSyncSource[] dialogDependentSources = new AppSyncSource[sourcesWithQuestion.size()];
             sourcesWithQuestion.copyInto(dialogDependentSources);
         } else {
             if (sourcesWithQuestion.isEmpty()) {
                 if (Log.isLoggable(Log.DEBUG)) {
                     Log.debug(TAG_LOG, "Continue sync without prompts");
                 }
                 //No dialog is prompted for any sources: the sync can begin
                 continueSynchronizationAfterBandwithSaverDialog(syncType, syncSources, 
                         refresh, direction, delay, fromOutside, false);
             } else {
                 if (Log.isLoggable(Log.DEBUG)) {
                     Log.debug(TAG_LOG, "Continue sync displaying bandwith prompt");
                 }
                 DialogController dialControll = controller.getDialogController();
                 dialControll.showNoWIFIAvailableDialog(screen,syncType,
                                                        sourcesWithQuestion, refresh,
                                                        direction, delay,
                                                        fromOutside);
 
                 //The sync request is started when the user has finished to reply
                 //all the first sync request dialogs (the last sync request dialog)
                 //(calling the continueSynchronizationAfterDialogCheck method)
             }
         }
     }
 
 
     protected void continueSyncAfterNetworkUsage(String syncType, Vector syncSources,
                                        int delay, boolean fromOutside)
     {
         // TODO FIXME: this method is currently not used
     }
 
     protected synchronized void
     continueSynchronizationAfterBandwithSaverDialog(String syncType,
                                                     Vector syncSources,
                                                     boolean refresh,
                                                     int direction,
                                                     int delay,
                                                     boolean fromOutside,
                                                     boolean continueSyncFromDialog)
     {
         // If no sources left, we simply return and do not update/change
         // anything
         if (syncSources.isEmpty()) {
             syncEnded();
             return;
         }
 
         // We register as listeners for the sync
         engine.setListener(this);
 
         int sourceSyncType = 0;
         AppSyncRequest appSyncRequest = new AppSyncRequest(null, delay);
         Enumeration sources = syncSources.elements();
         while(sources.hasMoreElements()) {
             AppSyncSource appSource = (AppSyncSource) sources.nextElement();
             SyncSource source = appSource.getSyncSource();
 
             if (refresh) {
                 int syncMode = appSource.prepareRefresh(direction);
                 source.getConfig().setSyncMode(syncMode);
             } else {
                 sourceSyncType = appSource.getConfig().getSyncMode();
                 // If this source has no config set, then we cannot force a sync
                 // mode, but this is a logical error
                 if (source.getConfig() != null) {
                     source.getConfig().setSyncMode(sourceSyncType);
                 } else {
                     Log.error(TAG_LOG, "Source has no config, cannot set sync mode");
                 }
             }
 
             // Clear any pending sync information here, because we are about to
             // start a sync
             AppSyncSourceConfig sourceConfig = appSource.getConfig();
             sourceConfig.setPendingSync("", -1);
             configuration.save();
             // Add the request for this synchronization
             appSyncRequest.addRequestContent(appSource);
         }
 
         if (fromOutside) {
             synchronized(appSyncRequestArr) {
                 appSyncRequestArr[0] = appSyncRequest;
                 appSyncRequestArr.notify();
             }
         } else {
             syncScheduler.addRequest(appSyncRequest);
         }
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
 
     protected void showMessage(String msg) {
         controller.getDisplayManager().showMessage(screen, msg);
     }
 
     public void noCredentials() {
         showMessage(localization.getLanguage("message_login_required"));
     }
 
     public void noSources() {
         showMessage(localization.getLanguage("message_nothing_to_sync"));
     }
 
     public void noConnection() {
         showMessage(localization.getLanguage("message_radio_off"));
     }
 
     public void noSignal() {
         showMessage(localization.getLanguage("message_no_signal"));
     }
 
     public void setCancel(boolean value) {
         doCancel = value;
     }
 
     /**
      * Check if the current sync should be cancelled
      * 
      * @return
      */
     public boolean isCancelled() {
         return doCancel;
     }
 
     public void beginSync() {
         clearErrors();
         setCancel(false);
     }
 
 
     public boolean syncStarted(Vector sources) {
         if (Log.isLoggable(Log.TRACE)) {
             Log.trace(TAG_LOG, "syncStarted");
         }
         if (customization.checkForUpdates()) {
             boolean isRequired = controller.checkForUpdate();
             if (isRequired) {
                 return false;
             }
         }
 
         return true;
 
     }
 
     public void endSync(Vector sources, boolean hadErrors) {
         if (Log.isLoggable(Log.DEBUG)) {
             Log.debug(TAG_LOG, "endSync reached");
         }
         
         setCancel(false);
 
         // Disable the retry poller if not null
         if(retryPoller != null) {
             retryPoller.disable();
             retryPoller = null;
         }
 
         // If we had a CONNECTION BLOCKED BY THE USER error (user does not allow
         // any network configuration) then we show an error because the user
         // had already interacted with the app for this sync
         if (hadErrors && showTCPAlert) {
             controller.toForeground();
             if (Log.isLoggable(Log.DEBUG)) {
                 Log.debug(TAG_LOG, "showing tcp settings alert!");
             }
             showMessage(localization.getLanguage("message_enter_TCP_settings"));
         }
 
         // Re-checks
         checkSourcesForStorageOrQuotaFullErrors(sources);
 
         // We reset these errors because this sync is over (if we are retrying,
         // we must consider the new one with no errors)
         logConnectivityError = false;
         showTCPAlert = false;
     }
 
     public void sourceStarted(AppSyncSource appSource) {
         if (Log.isLoggable(Log.TRACE)) {
             Log.trace(TAG_LOG, "sourceStarted " + appSource.getName());
         }
         currentSource = appSource;
         UISyncSourceController sourceController = appSource.getUISyncSourceController();
         if (sourceController != null) {
             sourceController.setSelected(true, false);
         }
         
         if (currentSource.getSyncSource().getConfig().getSyncMode() == SyncML.ALERT_CODE_REFRESH_FROM_SERVER) {
             refreshClientData(appSource, sourceController);
         }
         if (Log.isLoggable(Log.TRACE)) {
             Log.trace(TAG_LOG, "sourceStarted " + currentSource);
         }
     }
 
     public void sourceEnded(AppSyncSource appSource) {
         if (Log.isLoggable(Log.TRACE)) {
             Log.trace(TAG_LOG, "sourceEnded " + appSource.getName());
         }
         currentSource = null;
 
         // Set synced source
         appSource.getConfig().setSynced(true);
         
         saveSourceConfig(appSource);
         
         UISyncSourceController sourceController = appSource.getUISyncSourceController();
         if (sourceController != null) {
             sourceController.setSelected(false, false);
         }
     }
 
     public void sourceFailed(AppSyncSource appSource, SyncException e) {
 
         if (Log.isLoggable(Log.TRACE)) {
             Log.trace(TAG_LOG, "sourceFailed");
         }
 
         int code = e.getCode();
         if (   code == SyncException.READ_SERVER_RESPONSE_ERROR
             || code == SyncException.WRITE_SERVER_REQUEST_ERROR
             || code == SyncException.CONN_NOT_FOUND) {
 
             logConnectivityError = true;
         } else if (code == SyncException.CONNECTION_BLOCKED_BY_USER) {
             showTCPAlert = true;
         }
     }
 
     public String getRemoteUri(AppSyncSource appSource) {
         SourceConfig config = appSource.getSyncSource().getConfig();
         return config.getRemoteUri();
     }
 
     public void serverOperationFailed() {
         showMessage(localization.getLanguage("message_not_send_to_server"));
     }
 
     public Controller getController() {
         return controller;
     }
 
     public void clearErrors() {
         showTCPAlert = false;
         logConnectivityError = false;
     }
 
     protected void setScreen(Screen screen) {
         this.screen = screen;
     }
 
     protected SyncEngine createSyncEngine() {
         return new SyncEngine(customization, configuration, appSyncSourceManager, null);
     }
 
     private void saveSourceConfig(AppSyncSource appSource) {
         appSource.getConfig().saveSourceSyncConfig();
         appSource.getConfig().commit();
     }
 
     private boolean retry(Vector sources) {
 
         boolean willRetry = false;
 
         if (retryPoller != null) {
             retryPoller.disable();
         }
 
         if (scheduledAttempt < 3) {
             scheduledAttempt++;
             Log.error(TAG_LOG, "Scheduled sync: Connection attempt failed. " + "Try again in "
                     + RETRY_POLL_TIME + " minutes");
 
             retryPoller = new Poller(this, RETRY_POLL_TIME, true, false);
             retryPoller.start();
             willRetry = true;
         } else {
             retryPoller = null;
             scheduledAttempt = 0;
         }
         return willRetry;
     }
 
     protected BasicController getBasicController() {
         return getController();
     }
 
     private class RequestHandler extends Thread {
 
         private boolean stop = false;
 
         public RequestHandler() {
         }
 
         public void run() {
             if (Log.isLoggable(Log.INFO)) {
                 Log.info(TAG_LOG, "Starting request handler");
             }
             while (!stop) {
                 try {
                     synchronized (appSyncRequestArr) {
                         appSyncRequestArr.wait();
                         syncScheduler.addRequest(appSyncRequestArr[0]);
                     }
                 } catch (Exception e) {
                     // All handled exceptions are trapped below, this is just a
                     // safety net for runtime exception because we don't want
                     // this thread to die.
                     Log.error(TAG_LOG, "Exception while performing a programmed sync " + e.toString());
                 }
             }
         }
     }
 
     private void refreshClientData(AppSyncSource appSource, UISyncSourceController controller) {
         // TODO FIXME: MARCO (delete items and notify the UI)
         /*
         if (appSource.getSyncSource() instanceof BBPIMSyncSource) {
             try {
                 BBPIMSyncSource bpss = (BBPIMSyncSource) appSource.getSyncSource();
 
                 PIMItemHelper pih = bpss.getHelper();
 
                 Enumeration items = pih.getItemsList();
 
                 //Notify the ui that items are being deleted from the client
                 controller.removingAllData();
 
                 //count the PIMList elements
                 int size = 0;
                 while (items.hasMoreElements()) {
                     PIMItem item = (PIMItem) items.nextElement();
                     size++;
                 }
 
                 //remove the PIMList elements
                 items = pih.getItemsList();
                 int i = 0;
                 while (items.hasMoreElements()) {
                     if (doCancel) {
                         // The sync has not started yet, so we must synthetize a
                         // report here
                         SyncReport report = new SyncReport(bpss);
                         report.setSyncStatus(SyncListener.CANCELLED);
                         controller.endSession(report);
                         
                         //Reset anchors if a cancel action was performed
                         //after at least 1 item was cancelled
                         if (i>0) {
                             bpss.getConfig().setLastAnchor(0);
                             bpss.getConfig().setLastAnchor(0);
                             bpss.resetTrackingData();
                         }
 
                         return;
                     }
                     i++;
                     PIMItem item = (PIMItem) items.nextElement();
                     pih.deleteItem(item);
 
                     //Notify the UI
                     controller.itemRemoved(i, size);
                 }
             } catch (PIMException ex) {
                 Log.error(TAG_LOG, "[refreshClientData]Cannot delete device item" + ex);
             }
         }
         */
     }
 
     private String getDataTag(SyncSource src) {
         String dataTag = null;
         if (src instanceof JSONSyncSource) {
             JSONSyncSource jsonSyncSource = (JSONSyncSource)src;
             dataTag = jsonSyncSource.getDataTag();
         }
         if (dataTag == null) {
             // This is the default value
             dataTag = src.getConfig().getRemoteUri() + "s";
         }
         return dataTag;
     }
 }
