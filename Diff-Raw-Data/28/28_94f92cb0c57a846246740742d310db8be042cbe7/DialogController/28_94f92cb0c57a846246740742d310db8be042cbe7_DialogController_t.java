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
 
 import com.funambol.client.localization.Localization;
 import com.funambol.client.ui.DisplayManager;
 import com.funambol.client.ui.Screen;
 import com.funambol.client.source.AppSyncSource;
 import com.funambol.client.source.AppSyncSourceManager;
 
 import com.funambol.util.Log;
 
 /**
  * Control dialog alert flow on the client using the client's controller and
  * DisplayManager. This class is just a controller. Refer to DisplayManager
  * implementation in order to manage the alert diplaying logic.
  */
 public class DialogController {
 
     /** TAG to be displayed into log messages*/
     private static final String TAG_LOG = "DialogController";
 
     //--- Local instance fields fed by the constructor
     private AppSyncSourceManager appSyncSourceManager;
     private Controller controller;
     protected DisplayManager displayManager;
     protected Localization localization;
 
     /**
      * Public constructor
      * @param displayManager the DisplayManager object to be used.
      * @param controller
      */
     public DialogController(DisplayManager displayManager, Controller controller) {
         this.displayManager = displayManager;
         this.controller = controller;
         this.localization = controller.getLocalization();
         this.appSyncSourceManager = controller.getAppSyncSourceManager();
     }
 
     /**
      * Show and OK Dialog with a message and a single button to quit.
      */
     public void showOkDialog(Screen screen, String message) {
         DialogOption options[] = new DialogOption[1];
         options[0] = new DialogOption(displayManager, screen, message, 0);
 
         displayManager.promptSelection(
                 screen,
                 message,
                 options,
                 0,
                 DisplayManager.GENERIC_DIALOG_ID);
  
     }
 
     /**
      * Use the localization field to build the reset direction alert dialog.
      * This dialog builds ResetDirectionDialogOption objects to refer to the
      * dialog choices.
      * @param screen the dialog alert owner Screen
      */
     public void showRefreshDirectionDialog(Screen screen) {
        DialogOption[] opt = new DialogOption[3];
 
         opt[0] = createResetDirectionDialogOption(screen, 
                                                   localization.getLanguage("dialog_refresh_from"),
                                                   SynchronizationController.REFRESH_FROM_SERVER);
         
         opt[1] = createResetDirectionDialogOption(screen,
                                                   localization.getLanguage("dialog_refresh_to"),
                                                   SynchronizationController.REFRESH_TO_SERVER);
 
         opt[2] = createResetDirectionDialogOption(screen, 
                                                   localization.getLanguage("dialog_cancel"),
                                                   -1);
 
         displayManager.promptSelection(screen, localization.getLanguage("dialog_refresh_which") + "\n" 
                                        + localization.getLanguage("dialog_refresh_warn2"), opt, -1,
                                        displayManager.REFRESH_DIRECTION_DIALOG_ID);
     }
 
      /**
      * Use the localization field to build the wifi not available alert dialog.
      * This dialog builds WIFINotAvailableDialogOption objects to refer to the
      * dialog choices.
      * @param screen the dialog alert owner Screen
      */
     public void showNoWIFIAvailableDialog(Screen screen,
                                           String syncType,
                                           Vector filteredSources,
                                           boolean refresh,
                                           int direction,
                                           int delay,
                                           boolean fromOutside)
     {
         DialogOption[] opt = new DialogOption[2];
 
         opt[0] = new WIFINotAvailableDialogOption(
                 displayManager,
                 screen,
                 localization.getLanguage("dialog_continue"),
                 0,
                 syncType,
                 filteredSources,
                 refresh,
                 direction,
                 delay,
                 fromOutside);
 
         opt[1] = new WIFINotAvailableDialogOption(
                 displayManager,
                 screen,
                 localization.getLanguage("dialog_cancel"),
                 -1,
                 syncType,
                 filteredSources,
                 refresh,
                 direction,
                 delay,
                 fromOutside);
 
         displayManager.promptSelection(screen, localization.getLanguage("dialog_no_wifi_availabale"),
                 opt, 0, displayManager.NO_WIFI_AVAILABLE_ID);
     }
 
     /**
      * Dialog option related to the wifi not available.
      */
     protected class WIFINotAvailableDialogOption extends DialogOption {
         private String syncType;
         private Vector filteredSources;
         private boolean refresh;
         private int direction;
         private int delay;
         private  boolean fromOutside;
 
 
         public WIFINotAvailableDialogOption(DisplayManager displayManager, Screen screen,
                                             String description, int value,
                                             String syncType,
                                             Vector filteredSources,
                                             boolean refresh,
                                             int direction,
                                             int delay,
                                             boolean fromOutside) {
             super(displayManager, screen, description, value);
             this.filteredSources = filteredSources;
             this.refresh = refresh;
             this.direction = direction;
             this.delay = delay;
             this.fromOutside = fromOutside;
             this.syncType = syncType;
         }
 
         /**
          * Triggered in threaded like logic when the user selects an option.
          */
         public void run() {
             displayManager.dismissSelectionDialog(displayManager.NO_WIFI_AVAILABLE_ID);
             HomeScreenController hsCont = controller.getHomeScreenController();
 
             if (getValue() == 0) {
                 if(!hsCont.isSynchronizing()) {
                     hsCont.changeSyncLabelsOnSyncEnded();
                 }
 
                 hsCont.continueSynchronizationAfterBandwithSaverDialog(syncType, filteredSources,
                                                                        refresh, direction,
                                                                        delay, fromOutside, true);
             } else {
                 hsCont.continueSynchronizationAfterBandwithSaverDialog(syncType, new Vector(),
                                                                        refresh, direction,
                                                                        delay, fromOutside, false);
             }
         }
     }
 
     /**
      * Use the localization field to build the reset type alert dialog that use
      * the DisplayManager client implementation to ask the user whose sources
      * must be refreshed. This dialog builds ResetTypeDialogOption objects to
      * refer to the dialog choiches. Actual client implementation requires this
      * alert to be diaplayed after the showRefreshDirectionDialog call in order
      * to have full information abaout the direction.
      * @param screen the dialog alert owner Screen
      * @param int the refresh direction for the selected sources.
      */
     public void showRefreshTypeDialog(Screen screen, int direction) {
         ResetTypeDialogOption[] options = null;
         // Count the number of enabled and refreshable sources
         Vector opts = new Vector();
 
         int numEnabledSources = -1;
         Enumeration enabledSources = appSyncSourceManager.getEnabledAndWorkingSources();
 
         int allId = 0;
         while(enabledSources.hasMoreElements()) {
             AppSyncSource source = (AppSyncSource)enabledSources.nextElement();
             if (source.isRefreshSupported(direction) && source.isVisible() &&
                 source.getConfig().getActive() && source.isWorking()) {
 
                 if (Log.isLoggable(Log.DEBUG)) {
                     Log.debug(TAG_LOG, "Source: " + source.getName() + " direction " + direction +
                                        " supported " + source.isRefreshSupported(direction));
                 }
                 opts.addElement(new ResetTypeDialogOption(displayManager, screen, source.getName(),
                                                           source.getId(), direction));
                 numEnabledSources++;
                 allId |= source.getId();
             }
         }
 
         if ((numEnabledSources + 1) > 1) {
             opts.addElement(new ResetTypeDialogOption(
                     displayManager,
                     screen,
                    localization.getLanguage("type_all-enabled"),
                     allId,
                     direction));
         }
 
         opts.addElement(new ResetTypeDialogOption(
                     displayManager,
                     screen,
                     localization.getLanguage("dialog_cancel"),
                     0,
                     direction));
 
         options = new ResetTypeDialogOption[opts.size()];
 
         opts.copyInto(options);
 
         displayManager.promptSelection(screen, localization.getLanguage("dialog_refresh")
                 + " "
                 + (direction == SynchronizationController.REFRESH_FROM_SERVER ? localization
                         .getLanguage("dialog_refresh_from").toLowerCase() : localization
                         .getLanguage("dialog_refresh_to")).toLowerCase(), options, -1,
                         displayManager.REFRESH_TYPE_DIALOG_ID);
     }
 
     protected ResetDirectionDialogOption createResetDirectionDialogOption(Screen screen, String label, int value) {
         return new ResetDirectionDialogOption(displayManager, screen, label, value);
     }
 
     /**
      * Dialog option related to the refresh direction to be used.
      */
     protected class ResetDirectionDialogOption extends DialogOption {
 
         public ResetDirectionDialogOption(DisplayManager dm, Screen screen, String description, int value) {
             super(dm, screen, description, value);
         }
 
         /**
          * Triggered in threaded like logic when the user selects an option.
          */
         public void run() {
             //Dismiss the currect dialog
             displayManager.dismissSelectionDialog(displayManager.REFRESH_DIRECTION_DIALOG_ID);
             //if the user selected a direction the refresh type dialog is shown
             //with the message related to that sync direction
            if (getValue() != -1) {
                 showRefreshTypeDialog(screen, value);
             }
         }
     }
 
     /**
      * Container for the reset type dialog option
      */
     protected class ResetTypeDialogOption extends DialogOption {
         int direction;
         public ResetTypeDialogOption(DisplayManager displayManager, Screen screen, String description,
                                      int value, int direction) {
             super(displayManager, screen, description, value);
             this.direction = direction;
         }
 
         /**
          * Accessor method to get the sync direction value
          * @return int the sync direction
          */
         public int getDirection() {
             return this.direction;
         }
 
         /**
          * Triggered in threaded like logic when the user selects an option.
          */
         public void run() {
             if (Log.isLoggable(Log.DEBUG)) {
                 Log.debug(TAG_LOG, "TYPE - Selected: " + this.getDescription() + " - code " +
                                    this.getValue() + " - direction " + getDirection());
             }
 
             //dismiss the progress dialog
             displayManager.dismissSelectionDialog(displayManager.REFRESH_TYPE_DIALOG_ID);
             if (!this.getDescription().equals(localization.getLanguage("dialog_cancel"))) {
                 //User selected the sync sources to be refreshed
                 try {
                    if (screen != null) {
                        displayManager.hideScreen(screen);
                    }
                     //starts the sync
                     controller.getHomeScreenController().refresh(value, direction);
                     controller.getHomeScreenController().redraw();
                 } catch (Exception ex) {
                     Log.error("Exception accessing home screen: " + ex);
                 }
             } else {
                 //User selected the cancel option
                 controller.getHomeScreenController().redraw();
             }
         }
     }
 
     /**
      * Helper function to prompt the user for a yes/no answer
      */
     public void askYesNoQuestion(Screen screen, String question, boolean defaultYes, Runnable yesAction,
                                  Runnable noAction) {
 
         DialogOption options[] = new DialogOption[2];
         options[0] = new RunnableDialogOption(displayManager,screen,localization.getLanguage("dialog_yes"),1,yesAction);
         options[1] = new RunnableDialogOption(displayManager,screen,localization.getLanguage("dialog_no"),0,noAction);
         displayManager.promptSelection(screen,question,options,(defaultYes ? 1 : 0),DisplayManager.GENERIC_DIALOG_ID);
     }
 
     /**
      * Helper function to prompt the user for an accept/deny answer
      */
     public void askAcceptDenyQuestion(Screen screen, String question, boolean defaultYes, Runnable yesAction,
                                       Runnable noAction) {
 
         DialogOption options[] = new DialogOption[2];
         options[0] = new RunnableDialogOption(displayManager,screen,localization.getLanguage("dialog_accept"),
                                               1,yesAction);
         options[1] = new RunnableDialogOption(displayManager,screen,localization.getLanguage("dialog_deny"),0,noAction);
         displayManager.promptSelection(screen,question,options,(defaultYes ? 1 : 0),DisplayManager.GENERIC_DIALOG_ID);
     }
 
     public void promptSelection(Screen screen, String question, DialogOption options[], int defaultOption) {
         displayManager.promptSelection(screen, question, options, defaultOption, DisplayManager.GENERIC_DIALOG_ID);
     }
 
     private class RunnableDialogOption extends DialogOption {
         private Runnable runnable;
         public RunnableDialogOption(DisplayManager dm, Screen screen, String label, int value, Runnable runnable) {
             super(dm, screen, label, value);
             this.runnable = runnable;
         }
 
         public void onClick() {
             if (runnable != null) {
                 runnable.run();
             }
         }
     }
 }
