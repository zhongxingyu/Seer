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
 
 import java.util.Enumeration;
 import java.util.Vector;
 import java.util.Hashtable;
 
 import com.funambol.client.source.AppSyncSource;
 import com.funambol.client.source.ExternalAppManager;
 import com.funambol.client.ui.HomeScreen;
 import com.funambol.client.ui.UISyncSource;
 import com.funambol.client.ui.Bitmap;
 import com.funambol.client.ui.DisplayManager;
 import com.funambol.syncml.spds.SyncStatus;
 import com.funambol.sync.SyncException;
 import com.funambol.sync.SyncListener;
 import com.funambol.sync.SyncSource;
 import com.funambol.util.Log;
 import com.funambol.util.StringUtil;
 import com.funambol.platform.NetworkStatus;
 
 /**
  * This class represents the controller for the home screen. Since the
  * HomeScreen is a screen where synchronizations can be performed, the
  * class extends the SynchronizationController. On top of this the class adds
  * the ability of handling the home screen.
  */
 public class HomeScreenController extends SynchronizationController {
 
     private static final String TAG_LOG = "HomeScreenController";
 
     protected HomeScreen         homeScreen;
 
     protected Vector             items = null;
 
     private Hashtable            pushRequestQueue = new Hashtable();
 
     private int                  selectedIndex = -1;
 
     private boolean              updateAvailableSources = false;
 
     private boolean              syncAllButtonAdded = false;
     
     /**
      *  This flag is to switch off the storage limit warning after
      *  it is displayed once. The warning must be displayed also more
      *  than once if an individual-source sync is fired, but not for
      *  multiple-source sync, scheduled sync and push sync.
      *  See US7498.
      */
     protected boolean dontDisplayStorageLimitWarning = false;
     /**
      *  This flag is to switch off the server quota warning after
      *  it is displayed once. The warning must be displayed also more
      *  than once if an individual-source sync is fired, but not for
      *  multiple-source sync, scheduled sync and push sync.
      *  See US7499.
      */
     protected boolean dontDisplayServerQuotaWarning = false;
     private boolean homeScreenRegisteredAndInForeground = false;
 
     private class ContinueSyncAction implements Runnable {
         private AppSyncSource appSource;
         
         public ContinueSyncAction() {
             this.appSource = null;
         }
 
         public ContinueSyncAction(AppSyncSource appSource) {
             this.appSource = appSource;
         }
         
         public void run() {
             if (appSource == null)
                 syncAllSources(MANUAL);
             else
                 syncSource(MANUAL, appSource);
         }
     }
 
     public HomeScreenController(Controller controller, HomeScreen homeScreen,NetworkStatus networkStatus) {
         super(controller, homeScreen,networkStatus);
         this.controller = controller;
         this.homeScreen = homeScreen;
         forceUpdateAvailableSources();
     }
 
     // TODO FIXME This is here until we need the ProxyHomeScreenController only
     public HomeScreenController() {
     }
 
     public HomeScreen getHomeScreen() {
         return homeScreen;
     }
 
     public void setHomeScreen(HomeScreen homeScreen) {
         if (this.homeScreen != homeScreen) {
             syncAllButtonAdded = false;
         }
         this.homeScreen = homeScreen;
         // If required, we shall add the sync all button
         addSyncAllButtonIfRequired();
         super.setScreen(homeScreen);
     }
 
     public void updateAvailableSources() {
         if (Log.isLoggable(Log.TRACE)) {
             Log.trace(TAG_LOG, "updateAvailableSources");
         }
         updateAvailableSources = true;
     }
 
     public boolean syncStarted(Vector sources) {
         if (Log.isLoggable(Log.TRACE)) {
             Log.trace(TAG_LOG, "syncStarted");
         }
         boolean res = super.syncStarted(sources);
         lockHomeScreen(sources);
         AppSyncSource appSource = (AppSyncSource)sources.elementAt(0);
         changeSyncLabelsOnSync(appSource);
         attachToSource(appSource);
         return res;
     }
 
     public void attachToRunningSync(AppSyncSource appSource) {
         if (Log.isLoggable(Log.DEBUG)) {
             Log.debug(TAG_LOG, "Attaching to running sync for " + appSource.getName());
         }
         if(homeScreen.isLocked()) {
             if (Log.isLoggable(Log.DEBUG)) {
                 Log.debug(TAG_LOG, "Cannot attach to running sync, home screen is locked");
             }
             return;
         }
         // First of all select the source to attach
         setSelected(appSource.getUiSourceIndex(), false);
         
         Vector sources = new Vector();
         sources.addElement(appSource);
         
         lockHomeScreen(sources);
         changeSyncLabelsOnSync(appSource);
         attachToSource(appSource);
     }
 
     public void endSync(Vector sources, boolean hadErrors) {
         super.endSync(sources, hadErrors);
     }
     
     protected void displayStorageLimitWarning(Vector localStorageFullSources) {
         logSyncSourceErrors(localStorageFullSources);
         if (isInForeground()) {
             if (!dontDisplayStorageLimitWarning) {         
                 String message = localization.getLanguage("message_storage_limit");
                 controller.getDialogController().showOkDialog(homeScreen, message);
                 dontDisplayStorageLimitWarning = true; // Once is enough
             }
         } else {
             super.displayStorageLimitWarning(localStorageFullSources);
         }
     }
     
     protected void displayServerQuotaWarning(Vector serverQuotaFullSources) {
         logSyncSourceErrors(serverQuotaFullSources);
 
         // if we had at least one device full error, we must choose how show
         // these errors to the user, according to US7498 and US7499
         if (isInForeground()) {
             if (!dontDisplayServerQuotaWarning) {
                 StringBuffer sourceNames = new StringBuffer(""); 
                 for(int i=0; i<serverQuotaFullSources.size(); i++) {
                     AppSyncSource appSource = (AppSyncSource)serverQuotaFullSources.elementAt(i);
                     if (sourceNames.length() > 0) {
                         sourceNames.append(",");
                     }
                     sourceNames.append(appSource.getName().toLowerCase());
                 }
                 String msg = localization.getLanguage("dialog_server_full");
                 msg = StringUtil.replaceAll(msg, "__source__", sourceNames.toString());
                 controller.getDialogController().showOkDialog(homeScreen, msg);
             }
         
         //error in sync when activity is in background 
         } else {
             super.displayServerQuotaWarning(serverQuotaFullSources);
         }
     }
     
     public void syncEnded() {
         if (Log.isLoggable(Log.TRACE)) {
             Log.trace(TAG_LOG, "sync ended");
         }
         super.syncEnded();
         
         for(int i=0;i<items.size();++i) {
             AppSyncSource appSource = (AppSyncSource)items.elementAt(i);
         
             // To make sure the UI is properly updated, we force a sync
             // termination for each source
             SyncSource    source    = appSource.getSyncSource();
             if (source != null) {
                 SyncListener  listener  = source.getListener();
                 SyncStatus report = new SyncStatus(source.getName());
                 report.setStatusCode(SyncListener.CANCELLED);
                 SyncException se = new SyncException(SyncException.CANCELLED, "Sync cancelled");
                 report.setSyncException(se);
                 if (listener != null) {
                     listener.endSession(report);
                 }
             }
         }
 
         changeSyncLabelsOnSyncEnded();
         unlockHomeScreen();
         setSelected(getFirstActiveItemIndex(), false);
         
         // If there are pending syncs, we start serving them
         synchronized(pushRequestQueue) {
             if (pushRequestQueue.size() > 0) {
                 Vector sources = new Vector(pushRequestQueue.size());
                 Enumeration keys = pushRequestQueue.keys();
                 while(keys.hasMoreElements()) {
                     sources.addElement(keys.nextElement());
                 }
                 pushRequestQueue.clear();
                 synchronize(com.funambol.client.controller.SynchronizationController.PUSH, sources);
             }
         }
     }
 
     public void redraw() {
 
         // We may need to update the list of
         // visible items as the server may have sent its capabilities
         if (updateAvailableSources) {
             forceUpdateAvailableSources();
         }
         if (homeScreen != null) {
             homeScreen.redraw();
         }
     }
 
     public Vector getVisibleItems() {
         return items;
     }
 
     public void buttonSelected(int index) {
         if (Log.isLoggable(Log.TRACE)) {
             Log.trace(TAG_LOG, "Button selected " + index);
         }
         AppSyncSource source = (AppSyncSource) items.elementAt(index);
         if (source.getConfig().getEnabled()) {
             setSelected(index, true);
         }
     }
 
     public void buttonPressed(int index) {
         if (Log.isLoggable(Log.TRACE)) {
             Log.trace(TAG_LOG, "Button pressed " + index);
         }
         
         AppSyncSource source = (AppSyncSource) items.elementAt(index);
         if (source.isWorking() && source.getConfig().getEnabled() && source.getConfig().getAllowed()) {
             long profileExpireDate = configuration.getProfileExpireDate();
             if (profileExpireDate != -1 
                 && profileExpireDate < System.currentTimeMillis() 
                 || configuration.getProfileNetworkUsageWarning())
             {
                 ContinueSyncAction csa = new ContinueSyncAction(source); 
                 NetworkUsageWarningController nuwc = new NetworkUsageWarningController(screen, controller, csa);
                 nuwc.askUserNetworkUsageConfirmation();
             } else {
                 syncSource(MANUAL, source);
             }
         } else {
             Log.error(TAG_LOG, "The user pressed a source disabled, this is an error in the code");
         }
     }
 
     public void selectFirstAvailable() {
         if (Log.isLoggable(Log.TRACE)) {
             Log.trace(TAG_LOG, "Select first source available");
         }
         setSelected(getFirstActiveItemIndex(), false);
     }
 
     public void sourceStarted(AppSyncSource appSource) {
         super.sourceStarted(appSource);
 
         // this selects the appSource and disable any previously selected one
         setSelected(appSource.getUiSourceIndex(), false);
     }
 
     /**
      * This method enques a sync request coming from a push notification. This
      * method can be used when there is a sync running and a new request comes
      * in. The request is enqueued and server as soon as the current sync
      * terminates.
      *
      * @param sources the sources to be enqueued
      */
     public void enquePushSyncRequest(Vector sources) {
         synchronized(pushRequestQueue) {
             for(int i=0;i<sources.size();++i) {
                 AppSyncSource source = (AppSyncSource)sources.elementAt(i);
                 pushRequestQueue.put(source, source);
             }
         }
     }
 
     /**
      * This method enques a sync request coming from a push notification. This
      * method can be used when there is a sync running and a new request comes
      * in. The request is enqueued and server as soon as the current sync
      * terminates.
      */
     public void enquePushSyncRequest() {
         synchronized(pushRequestQueue) {
             for(int i=0;i<items.size();++i) {
                 AppSyncSource appSource = (AppSyncSource)items.elementAt(i);
                 if (appSource.getConfig().getEnabled() && appSource.isWorking()) {
                     pushRequestQueue.put(appSource, appSource);
                 }
             }
         }
     }
 
     protected void lockHomeScreen(Vector sources) {
 
         if (homeScreen == null) {
             return;
         }
         if (customization.syncAllOnMainScreenRequired()) {
             // disable the sync all button (if it does not have the cancel sync
             // role during a sync)
             if (!customization.syncAllActsAsCancelSync()) {
                 homeScreen.setSyncAllEnabled(false);
             }
         }
 
         for(int j=0;j<items.size();++j) {
             AppSyncSource appSource = (AppSyncSource) items.elementAt(j);
             
             // Source is not allowed and must be skipped
             if (!appSource.getConfig().getAllowed())
                 continue;
             
             // If this source is in sources then we shall enable it,
             // otherwise we must disable it
             boolean enable = false;
             for(int i=0;i<sources.size();++i) {
                 AppSyncSource appSource2 = (AppSyncSource)sources.elementAt(i);
                 if (appSource2.getId() == appSource.getId()) {
                     enable = true;
                     break;
                 }
             }
             UISyncSource uiSource = appSource.getUISyncSource();
             uiSource.setEnabled(enable);
         }
         redraw();
         homeScreen.lock();
     }
 
     public void updateEnabledSources() {
 
         if (Log.isLoggable(Log.TRACE)) {
             Log.trace(TAG_LOG, "updateEnabledSources");
         }
 
         // If a sync is in progress, then we don't change the sources status,
         // otherwise we would corrupt the UI. On sync termination, the home
         // screen will get refreshed
         if (isSynchronizing()  || (homeScreen != null && homeScreen.isLocked())) {
             return;
         }
 
         Enumeration sources = items.elements();
         boolean atLeastOneEnabled = false;
         while (sources.hasMoreElements()) {
             AppSyncSource appSource = (AppSyncSource)sources.nextElement();
             UISyncSourceController sourceController = appSource.getUISyncSourceController();
 
             if (sourceController != null) {
                 if (appSource.getConfig().getActive()) {
                     if (!appSource.isEnabled() || !appSource.isWorking() || !appSource.getConfig().getAllowed()) {
                         sourceController.disable();
                         UISyncSource uiSource = appSource.getUISyncSource();
                         // If this is the selected source, then we shall move the
                         // selection to the first available
                         if (uiSource != null && uiSource.isSelected()) {
                             setSelected(getFirstActiveItemIndex(), false);
                         }
                     } else {
                         sourceController.enable();
                         atLeastOneEnabled = true;
                     }
                 }
             }
         }
         // If there are no sources enabled, then we disable the sync all button
         if (homeScreen != null) {
             homeScreen.setSyncAllEnabled(atLeastOneEnabled);
         }
         if (!atLeastOneEnabled) {
             // We must "deselect" all items because all are disabled
             for(int i=0;i<items.size();++i) {
                 setSelected(i, false);
             }
         }
 
         redraw();
     }
 
     protected void syncSource(String syncType, AppSyncSource appSource) {        
         Vector sources = new Vector();
         sources.addElement(appSource);        
         synchronize(syncType, sources);
     }
     
     public void syncMenuSelected() {
         if (selectedIndex != -1) {
             AppSyncSource appSource = (AppSyncSource)items.elementAt(selectedIndex);
             
             long profileExpireDate = configuration.getProfileExpireDate();
             if (profileExpireDate != -1 
                 && profileExpireDate < System.currentTimeMillis() 
                 || configuration.getProfileNetworkUsageWarning())
             {
                 ContinueSyncAction csa = new ContinueSyncAction(appSource); 
                 NetworkUsageWarningController nuwc = new NetworkUsageWarningController(screen, controller, csa);
                 nuwc.askUserNetworkUsageConfirmation();
             } else {
                 syncSource(MANUAL, appSource);
             }
         }
     }
 
     public void syncAllPressed() {       
         if (Log.isLoggable(Log.TRACE)) {
             Log.trace(TAG_LOG, "Sync All Button pressed");
         }
 
         // If a sync is in progress, then this is a cancel sync request
         if (isSynchronizing() && customization.syncAllActsAsCancelSync()) {
             if (!doCancel) {
                 cancelSync();
             } else {
                 if (Log.isLoggable(Log.INFO)) {
                     Log.info(TAG_LOG, "Cancelling already in progress");
                 }
             }
         } else {
             long profileExpireDate = configuration.getProfileExpireDate();
             if (profileExpireDate != -1 
                 && profileExpireDate < System.currentTimeMillis() 
                 || configuration.getProfileNetworkUsageWarning())
             {
                 ContinueSyncAction csa = new ContinueSyncAction(); 
                 NetworkUsageWarningController nuwc = 
                     new NetworkUsageWarningController(screen, controller, csa);
                 nuwc.askUserNetworkUsageConfirmation();
             } else {
                 syncAllSources(MANUAL);
             }
         }
     }
 
     public void aloneSourcePressed() {
         if (Log.isLoggable(Log.TRACE)) {
             Log.trace(TAG_LOG, "Alone Source Button pressed");
         }
 
         // If a sync is in progress, then this is a cancel sync request
         if (isSynchronizing()) {
             if (!doCancel) {
                 cancelSync();
             } else {
                 if (Log.isLoggable(Log.INFO)) {
                     Log.info(TAG_LOG, "Cancelling already in progress");
                 }
             }
         } else {
             AppSyncSource appSource = (AppSyncSource)items.elementAt(0);
             if (appSource.isWorking() && appSource.getConfig().getEnabled() && appSource.getConfig().getAllowed()) {
                 long profileExpireDate = configuration.getProfileExpireDate();
                 if (profileExpireDate != -1 
                     && profileExpireDate < System.currentTimeMillis() 
                     || configuration.getProfileNetworkUsageWarning())
                 {
                     ContinueSyncAction csa = new ContinueSyncAction(appSource); 
                     NetworkUsageWarningController nuwc = new NetworkUsageWarningController(screen, controller, csa);
                     nuwc.askUserNetworkUsageConfirmation();
                 } else {
                     syncSource(MANUAL, appSource);
                 }
             }
         }
     }
 
 
     public void syncAllSources(String syncType) {
         if (Log.isLoggable(Log.INFO)) {
             Log.info(TAG_LOG, "syncAllSources");
         }
 
         Vector sources = new Vector();        
         for(int i=0;i<items.size();++i) {
             AppSyncSource appSource = (AppSyncSource)items.elementAt(i);
             if (appSource.getConfig().getEnabled() && appSource.isWorking() && appSource.getConfig().getAllowed()) {
                 sources.addElement(appSource);
             }
         }
         
         synchronize(syncType, sources);
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
         try {
             new ProfileUpdateHelper(controller).updateProfile();  
             
             Vector newSyncSources = getAllowedSources();            
             if (syncSources.size() > 1) {
                 syncSources = newSyncSources;
             }
             
             syncEnded();
         } catch (Exception e) {
             Log.error(TAG_LOG, "Config sync failed ", e);            
             syncEnded();            
             SyncException se = new SyncException(SyncException.CLIENT_ERROR, e.toString());
             sourceFailed((AppSyncSource)syncSources.elementAt(0), se);            
             return;
         }
         
         // For manual sync, always show alert message for storage/server
         // quota limit. For other sync modes, doesn't display message if
         // the previous sync ended with the same error.
         if (MANUAL.equals(syncType)) {
             dontDisplayStorageLimitWarning = false;
             dontDisplayServerQuotaWarning = false;
         } else {
             for(int i = 0 ; i < syncSources.size(); ++i) {
                 AppSyncSource appSource = (AppSyncSource)syncSources.elementAt(i);
                     
                 switch (appSource.getConfig().getLastSyncStatus()) {
                 case SyncListener.LOCAL_CLIENT_FULL_ERROR:
                     // If for at least one source the storage limit warning has
                     // already been shown, no warning should be displayed again
                     dontDisplayStorageLimitWarning = true;
                     break;
                 case SyncListener.SERVER_FULL_ERROR:
                     // If for at least one source the server full quota warning has
                     // already been shown, no warning should be displayed again
                     dontDisplayServerQuotaWarning = true;
                     break;
                 }
             }
         }
         
         super.synchronize(syncType, syncSources);
     }
     
     private Vector getAllowedSources() {
         Enumeration sources = appSyncSourceManager.getRegisteredSources();
         Vector allowedSources = new Vector();
         while (sources.hasMoreElements()) {
             AppSyncSource appSource = (AppSyncSource)sources.nextElement();
             
             if (appSource.getConfig().getAllowed()) {
                allowedSources.addElement(appSource);
             }
         }
         
         return allowedSources;
     }
 
     public void cancelMenuSelected() {
         cancelSync();
     }
 
     public void updateMenuSelected() {
         controller.promptUpdate();
     }
 
     public void quitMenuSelected() {
         controller.toBackground();
     }
 
     public boolean isUpdate() {
         return controller.isUpdate();
     }
 
     public void exit() {
         Controller globalController = getController();
         DisplayManager dm = globalController.getDisplayManager();
         dm.askYesNoQuestion(homeScreen, "Are you sure you want to exit?",
                             new ExitAction(), null, 0);
     }
 
     private class ExitAction implements Runnable {
 
         public ExitAction() {
         }
 
         public void run() {
             if (Log.isLoggable(Log.TRACE)) {
                 Log.trace(TAG_LOG, "Exiting application");
             }
         }
     }
 
     public void showConfigurationScreen() {
         Controller globalController = getController();
         // If a sync is running, we wait for its termination before opening the
         // settings screen
         if (isSynchronizing()) {
             showSyncInProgressMessage();
         } else {
             globalController.showScreen(homeScreen, Controller.CONFIGURATION_SCREEN_ID);
         }
     }
 
     public void showAboutScreen() {
         Controller globalController = getController();
         globalController.showScreen(homeScreen, Controller.ABOUT_SCREEN_ID);
     }
 
     public void showAccountScreen() {
         Controller globalController = getController();
         globalController.showScreen(homeScreen, Controller.ACCOUNT_SCREEN_ID);
     }
 
     public void gotoMenuSelected() {
         if (selectedIndex != -1) {
             AppSyncSource source = (AppSyncSource)items.elementAt(selectedIndex);
 
             ExternalAppManager manager = source.getAppManager();
             if (manager != null) {
                 try {
                     manager.launch(source, null);
                 } catch (Exception e) {
                     // TODO FIXME: show a toast?
                     Log.error(TAG_LOG, "Cannot launch external app manager, because: " + e);
                 }
             } else {
                 Log.error(TAG_LOG, "No external manager associated to source: " + source.getName());
             }
         }
     }
     
     /**
      * Returns true when the associated screen is in foreground (visible
      * to the user and with focus)
      */
     public boolean isInForeground() {
         //first of all, if an HomeScreen is not associated with the controller
         //it's impossible that the screen is in foreground
         if (null == homeScreen) {
             return false;
         }
         
         //then, check for internal flag
         return homeScreenRegisteredAndInForeground ;
     }
     
     /**
      * Sets foreground status of the screen
      */
     public void setForegroundStatus(boolean newValue) {
         homeScreenRegisteredAndInForeground = newValue;
     }
     
 
     protected void unlockHomeScreen() {
         if (homeScreen == null) {
             return;
         }
         if (customization.syncAllOnMainScreenRequired()) {
             // enable the sync all button
             if (!customization.syncAllActsAsCancelSync()) {
                 homeScreen.setSyncAllEnabled(true);
             }
         }
 
         for(int j=0;j<items.size();++j) {
             AppSyncSource appSource = (AppSyncSource) items.elementAt(j);
                        
             // If this source is in sources then we shall enable it,
             // otherwise we must disable it
             UISyncSourceController uiSourceController = appSource.getUISyncSourceController();
             if (appSource.isWorking() && appSource.isEnabled() && appSource.getConfig().getAllowed()) {
                 uiSourceController.enable();
             } else {
                 uiSourceController.disable();
             }
             // If a UI Source is in the syncing state force it to stop
             if(uiSourceController.isSyncing()) {
                 uiSourceController.resetStatus();
             }
         }
         redraw();
         homeScreen.unlock();
     }
 
     protected void showSyncInProgressMessage() {
         // If the home screen is not displayed, we cannot show any warning and
         // just ignore this event
         Controller globalController = getController();
         if (homeScreen != null) {
             DisplayManager dm = globalController.getDisplayManager();
             String msg = localization.getLanguage("message_sync_running_wait");
             dm.showMessage(homeScreen, msg);
         }
     }
 
     private int getFirstActiveItemIndex() {
         int size = items.size();
         
         for (int i=0; i<size; i++) {
             AppSyncSource source = (AppSyncSource) items.elementAt(i);
             if (source.isEnabled() && source.isWorking()) {
                 return i;
             }
         }   
         return 0;
     }
 
     private void addSyncAllButtonIfRequired() {
         if (!syncAllButtonAdded && homeScreen != null) {
             if (customization.syncAllOnMainScreenRequired()) {
 
                 Bitmap img = customization.getSyncAllIcon();
                 Bitmap bg = customization.getSyncAllBackground();
                 Bitmap bgSel = customization.getSyncAllHighlightedBackground();
 
                 homeScreen.addSyncAllButton(localization.getLanguage("home_sync_all"),
                             img, bg, bgSel);
             }
             syncAllButtonAdded = true;
         }
     }
 
     private void computeVisibleItems() {
 
         items = new Vector();
 
         //Set the SyncAll Item if required
         addSyncAllButtonIfRequired();
 
         int realSize = controller.computeNumberOfVisibleSources();
         if (realSize == 0) {
             // There are no available sources, nothing to do
             return;
         }
         if (Log.isLoggable(Log.DEBUG)) {
             Log.debug(TAG_LOG, "Number of visible sources: " + realSize);
         }
         items.setSize(realSize);
 
         // Now recompute the ui position for all available sources
         int sourcesOrder[] = customization.getSourcesOrder();
         int uiOrder = 0;
         for (int i=0;i<sourcesOrder.length;++i) {
             int sourceId = sourcesOrder[i];
             // If this is a working source, then set its UI position
             AppSyncSource source = appSyncSourceManager.getSource(sourceId);
             if (controller.isVisible(source)) {
                 if (Log.isLoggable(Log.DEBUG)) {
                     Log.debug(TAG_LOG, "Setting source " + source.getName() + " at position: " + uiOrder);
                 }
                 source.setUiSourceIndex(uiOrder++);
             }
         }
 
         // Add an item for each registered source that has to fit into the home
         // screen. So far the only one we shall discard is the ConfigSyncSource
         Enumeration sources = appSyncSourceManager.getRegisteredSources();
         while (sources.hasMoreElements()) {
             AppSyncSource appSource = (AppSyncSource)sources.nextElement();
             if (controller.isVisible(appSource)) {
                 // Set the sources in the appropriate order
                 int index = appSource.getUiSourceIndex();
                 if (Log.isLoggable(Log.DEBUG)) {
                     Log.debug(TAG_LOG, "Setting source at index: " + index);
                 }
                 items.setElementAt(appSource, index);
             }
         }
     }
 
     private void setSelected(int index, boolean fromUi) {
 
         // First of all remove selection from the current selected item
         if ((selectedIndex != index) &&
             (selectedIndex != -1) &&
             (selectedIndex < items.size())) {
             
             AppSyncSource oldAppSource = (AppSyncSource)items.elementAt(selectedIndex);
             UISyncSourceController sourceController = oldAppSource.getUISyncSourceController();
             if (sourceController != null) {
                 sourceController.setSelected(false, fromUi);
             }
         }
         
         AppSyncSource appSource = (AppSyncSource)items.elementAt(index);
         if (!appSource.isEnabled() || !appSource.isWorking()) {
             // Invalid selection, the source cannot be selected
             return;
         }
 
         selectedIndex = index;
         UISyncSourceController sourceController = appSource.getUISyncSourceController();
         if (sourceController != null) {
             sourceController.setSelected(true, fromUi);
         } else {
             Log.error(TAG_LOG, "Found a source without controller associated");
         }
     }
 
 
     protected void changeSyncLabelsOnSync(AppSyncSource appSource) {
         if (homeScreen == null) {
             return;
         }
 
         if (customization.syncAllOnMainScreenRequired()) {
             if (customization.syncAllActsAsCancelSync()) {
                 homeScreen.setSyncAllText(localization.getLanguage("menu_cancel_sync"));
             } else {
                 homeScreen.setSyncAllText(localization.getLanguage("status_sync"));
             }
         }
         homeScreen.setSyncMenuText(localization.getLanguage("menu_cancel_sync"));
     }
 
     protected void attachToSource(AppSyncSource appSource) {
         // Force the source to start syncing, even though we have not received
         // any event from the SyncEngine. This has two nice effects:
         // 1) the source is marked immediately as syncing
         // 2) the buttons and the sync status are always aligned, even if the
         // sync takes time to start
         UISyncSourceController sourceController = appSource.getUISyncSourceController();
         if (sourceController != null) {
             sourceController.attachToSession();
         }
     }
 
     protected void changeSyncLabelsOnSyncEnded() {
         if (homeScreen == null) {
             return;
         }
         if (customization.syncAllOnMainScreenRequired()) {
             homeScreen.setSyncAllText(localization.getLanguage("home_sync_all"));
         }
         homeScreen.setSyncMenuText(localization.getLanguage("menu_sync"));
     }
 
     private void forceUpdateAvailableSources() {
         if (Log.isLoggable(Log.TRACE)) {
             Log.trace(TAG_LOG, "forceUpdateAvailableSources");
         }
         // Compute the set of items to be displayed
         computeVisibleItems();
  
         if (homeScreen != null) {
             homeScreen.updateVisibleItems();
         }
         setSelected(getFirstActiveItemIndex(), false);
         updateAvailableSources = false;
     }
     
     /**
      * Logs sync sources where server full quota or storage limit error happened 
      * @param storageLimitOrserverQuotaFullSources
      */
     protected void logSyncSourceErrors(Vector storageLimitOrserverQuotaFullSources) {
         for(int i=0; i<storageLimitOrserverQuotaFullSources.size(); i++) {
             AppSyncSource appSource = (AppSyncSource)storageLimitOrserverQuotaFullSources.elementAt(i);
             switch (appSource.getConfig().getLastSyncStatus()) {
             case SyncListener.LOCAL_CLIENT_FULL_ERROR:
                 Log.error(TAG_LOG, "Storage limit reached for source " + appSource.getName());
                 break;
             case SyncListener.SERVER_FULL_ERROR:
                 Log.error(TAG_LOG, "Server quota full for source " + appSource.getName());
                 break;
             }
         }
     }
 }
