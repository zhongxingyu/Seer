 /*-
  * Copyright (c) 2009, Derek Konigsberg
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met:
  *
  * 1. Redistributions of source code must retain the above copyright
  *    notice, this list of conditions and the following disclaimer.
  * 2. Redistributions in binary form must reproduce the above copyright
  *    notice, this list of conditions and the following disclaimer in the
  *    documentation and/or other materials provided with the distribution.
  * 3. Neither the name of the project nor the names of its
  *    contributors may be used to endorse or promote products derived
  *    from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
  * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
  * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
  * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
  * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
  * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
  * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
  * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
  * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
  * OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package org.logicprobe.LogicMail.ui;
 
 import org.logicprobe.LogicMail.AnalyticsDataCollector;
 import org.logicprobe.LogicMail.LogicMail;
 import org.logicprobe.LogicMail.LogicMailResource;
 import org.logicprobe.LogicMail.model.AccountNode;
 import org.logicprobe.LogicMail.model.MailManager;
 import org.logicprobe.LogicMail.model.NetworkAccountNode;
 
 import net.rim.device.api.i18n.ResourceBundle;
 import net.rim.device.api.ui.Field;
 import net.rim.device.api.ui.MenuItem;
 import net.rim.device.api.ui.Screen;
 import net.rim.device.api.ui.UiApplication;
 import net.rim.device.api.ui.component.Dialog;
 import net.rim.device.api.ui.component.Menu;
 import net.rim.device.api.ui.container.MainScreen;
 
 /**
  * Standard UI screen implementation.
  * This implementation is designed to separate RIM API inheritance
  * relationships from concrete UI screens through composition.
  * The concrete UI is implemented through a <tt>ScreenProvider</tt>
  * implementation.
  */
 public class StandardScreen extends MainScreen {
     protected static ResourceBundle resources = ResourceBundle.getBundle(LogicMailResource.BUNDLE_ID, LogicMailResource.BUNDLE_NAME);
     protected static StatusBarField statusBarField = new StatusBarField();
     private NavigationController navigationController;
     private Field titleField;
     private Field originalStatusField;
     private Field currentStatusField;
 
     private MenuItem configItem;
     private MenuItem aboutItem;
     private MenuItem closeItem;
     private MenuItem exitItem;
 
     protected final ScreenProvider screenProvider;
 
     /**
      * Instantiates a new standard screen.
      * 
      * @param navigationController the navigation controller
      * @param screenProvider the screen provider
      */
     public StandardScreen(NavigationController navigationController, ScreenProvider screenProvider) {
         super(screenProvider.getStyle());
         if(navigationController == null || screenProvider == null) {
             throw new IllegalArgumentException();
         }
 
         this.navigationController = navigationController;
         this.screenProvider = screenProvider;
         initialize();
     }
 
     NavigationController getNavigationController() {
         return navigationController;
     }
     
     /**
      * Initialize the screen elements.
      */
     private void initialize() {
         // Create screen elements
         if(screenProvider.getTitle() != null) {
             this.titleField = createTitleField();
             setTitle(titleField);
         }
 
         initMenuItems();
         screenProvider.setNavigationController(navigationController);
         screenProvider.initFields(this);
     }
     
     protected Field createTitleField() {
         return new HeaderField(
                 resources.getString(LogicMailResource.APPNAME)
                 + " - "
                 + screenProvider.getTitle());
     }
 
     public String getScreenName() {
         return screenProvider.getScreenName();
     }
     
     public String getScreenPath() {
         return screenProvider.getScreenPath();
     }
     
     /* (non-Javadoc)
      * @see net.rim.device.api.ui.container.MainScreen#setStatus(net.rim.device.api.ui.Field)
      */
     public void setStatus(Field status) {
         originalStatusField = status;
         superSetStatusImpl(status);
     }
 
     /**
      * Wrapper for internal calls to {@link MainScreen#setStatus(Field)}
      * that makes sure <code>IllegalStateException</code>s do not appear
      * if the field had previously been added, and that the field does
      * not get added if it is already the active status field.
      * 
      * @param status the new status field
      */
     private void superSetStatusImpl(Field status) {
         if(currentStatusField != status) {
             currentStatusField = status;
             if(status != null && status.getManager() != null) {
                 status.getManager().delete(status);
             }
             super.setStatus(status);
         }
     }
 
     /**
      * Update status text, showing or hiding the status bar as necessary.
      * 
      * @param statusText the status text
      */
     public void updateStatus(String statusText) {
         statusBarField.setStatusText(statusText);
         if(statusBarField.hasStatus()) {
             superSetStatusImpl(statusBarField);
         }
         else {
             superSetStatusImpl(originalStatusField);
         }
     }
 
     /* (non-Javadoc)
      * @see net.rim.device.api.ui.Screen#onUiEngineAttached(boolean)
      */
     protected void onUiEngineAttached(boolean attached) {
         if(attached) {
             super.onUiEngineAttached(true);
             updateStatus(navigationController.getCurrentStatus());
             NotificationHandler.getInstance().cancelNotification();
             screenProvider.onDisplay();
         }
         else {
             screenProvider.onUndisplay();
             superSetStatusImpl(originalStatusField);
             super.onUiEngineAttached(false);   
         }
     }
 
     /* (non-Javadoc)
      * @see net.rim.device.api.ui.Screen#onExposed()
      */
     protected void onExposed() {
         super.onExposed();
         updateStatus(navigationController.getCurrentStatus());
         NotificationHandler.getInstance().cancelNotification();
     }
 
     /* (non-Javadoc)
      * @see net.rim.device.api.ui.Screen#onObscured()
      */
     protected void onObscured() {
         super.onObscured();
         superSetStatusImpl(originalStatusField);
     }
 
     /* (non-Javadoc)
      * @see net.rim.device.api.ui.Screen#onClose()
      */
     public boolean onClose() {
         boolean result = screenProvider.onClose();
         if(result) {
             if(this.isDisplayed()) {
                 close();
             }
         }
         return result;
     }
 
     /* (non-Javadoc)
      * @see net.rim.device.api.ui.Field#onVisibilityChange(boolean)
      */
     protected void onVisibilityChange(boolean visible) {
         screenProvider.onVisibilityChange(visible);
     }
 
     private void initMenuItems() {
         configItem = new MenuItem(resources, LogicMailResource.MENUITEM_CONFIGURATION, 800000, 9000) {
             public void run() {
                 AnalyticsDataCollector.getInstance().onButtonClick(getScreenPath(), getScreenName(), "config");
                 showConfigScreen();
             }
         };
         aboutItem = new MenuItem(resources, LogicMailResource.MENUITEM_ABOUT, 800100, 9000) {
             public void run() {
                 AnalyticsDataCollector.getInstance().onButtonClick(getScreenPath(), getScreenName(), "about");
                 // Show the about dialog
                 AboutDialog dialog = new AboutDialog();
                 dialog.doModal();
             }
         };
         closeItem = new MenuItem(resources, LogicMailResource.MENUITEM_CLOSE, 60000000, 9000) {
             public void run() {
                 AnalyticsDataCollector.getInstance().onButtonClick(getScreenPath(), getScreenName(), "close");
                 StandardScreen.this.onClose();
             }
         };
         exitItem = new MenuItem(resources, LogicMailResource.MENUITEM_EXIT, 60000100, 9000) {
             public void run() {
                 AnalyticsDataCollector.getInstance().onButtonClick(getScreenPath(), getScreenName(), "exit");
                 tryShutdownApplication();
             }
         };
     }
 
     public void tryShutdownApplication() {
         // Get all accounts
         NetworkAccountNode[] accounts = MailManager.getInstance().getMailRootNode().getNetworkAccounts();
 
         // Find out of we still have an open connection
         boolean openConnection = false;
         for(int i=0; i<accounts.length; i++) {
             if(accounts[i].getStatus() == AccountNode.STATUS_ONLINE) {
                 openConnection = true;
                 break;
             }
         }
 
         if(openConnection) {
             if(Dialog.ask(Dialog.D_YES_NO, resources.getString(LogicMailResource.BASE_CLOSEANDEXIT)) == Dialog.YES) {
                 for(int i=0; i<accounts.length; i++) {
                     if(accounts[i].getStatus() == AccountNode.STATUS_ONLINE) {
                         accounts[i].requestDisconnect(true);
                     }
                 }
                 doShutdownProcess();
             }
         }
         else {
             doShutdownProcess();
         }
     }
 
     private void doShutdownProcess() {
         // Iterate through the screen stack, find the LogicMail home screen,
         // and tell it to save its state.  If other screens have persistable
         // state, then this needs to be refactored into a common ScreenProvider
         // method.
         Screen screen = this;
         while(screen != null) {
             if(screen instanceof StandardScreen) {
                 ScreenProvider provider = ((StandardScreen)screen).screenProvider;
                 if(provider instanceof MailHomeScreen) {
                     ((MailHomeScreen)provider).saveScreenMetadata();
                     break;
                 }
             }
             screen = screen.getScreenBelow();
         }
         
         cleanupTitleField(titleField);
         LogicMail.shutdownApplication();
     }
 
     protected void cleanupTitleField(Field titleField) {
         ((HeaderField)titleField).removeListeners();
     }
     
     /**
      * Shows the configuration screen.
      * Subclasses should override this method if they need to
      * refresh their view of the configuration after the screen
      * is closed.
      */
     protected void showConfigScreen() {
         UiApplication.getUiApplication().pushModalScreen(new ConfigScreen());
     }
 
     /* (non-Javadoc)
      * @see net.rim.device.api.ui.container.MainScreen#makeMenu(net.rim.device.api.ui.component.Menu, int)
      */
     protected void makeMenu(Menu menu, int instance) {
         screenProvider.makeMenu(menu, instance);
         if(instance == Menu.INSTANCE_DEFAULT) {
             menu.add(configItem);
             menu.add(aboutItem);
             menu.add(closeItem);
             menu.add(exitItem);
         }
     }
 
     /* (non-Javadoc)
      * @see net.rim.device.api.ui.container.MainScreen#onSavePrompt()
      */
     protected boolean onSavePrompt() {
         return screenProvider.onSavePrompt();
     }
 
     /* (non-Javadoc)
      * @see net.rim.device.api.ui.Screen#navigationClick(int, int)
      */
     protected boolean navigationClick(int status, int time) {
         return screenProvider.navigationClick(status, time);
     }
 
     /**
      * Provides a way for <code>ScreenProvider</code> implementations to call
      * {@link net.rim.device.api.ui.Screen#navigationClick(int, int)} if they
      * do not want to override its behavior.
      *
      * @see net.rim.device.api.ui.Screen#navigationClick(int, int)
      */
     boolean navigationClickDefault(int status, int time) {
         return super.navigationClick(status, time);
     }
     
     /* (non-Javadoc)
      * @see net.rim.device.api.ui.Screen#navigationUnclick(int, int)
      */
     protected boolean navigationUnclick(int status, int time) {
         return screenProvider.navigationUnclick(status, time);
     }
     
     /**
      * Provides a way for <code>ScreenProvider</code> implementations to call
      * {@link net.rim.device.api.ui.Screen#navigationUnclick(int, int)} if they
      * do not want to override its behavior.
      *
      * @see net.rim.device.api.ui.Screen#navigationUnclick(int, int)
      */
     boolean navigationUnclickDefault(int status, int time) {
         return super.navigationUnclick(status, time);
     }
 
     /* (non-Javadoc)
      * @see net.rim.device.api.ui.Screen#keyChar(char, int, int)
      */
     protected boolean keyChar(char c, int status, int time) {
         return screenProvider.keyChar(c, status, time);
     }
 
     /**
      * Provides a way for <code>ScreenProvider</code> implementations to call
      * {@link net.rim.device.api.ui.Screen#keyChar(char, int, int)} if they
      * do not want to override its behavior.
      *
      * @see net.rim.device.api.ui.Screen#keyChar(char, int, int)
      */
     boolean keyCharDefault(char c, int status, int time) {
         return super.keyChar(c, status, time);
     }
     
     /**
      * Gets the enabled state of a shortcut button.
      * Provided for subclasses that support shortcut buttons.
      * 
      * @param id the ID of the button
      * @return the enabled state
      */
     public boolean isShortcutEnabled(int id) {
         // Shortcuts not supported by the base screen class
         return false;
     }
 
     /**
      * Sets the enabled state of a shortcut button.
      * Provided for subclasses that support shortcut buttons.
      * 
      * @param id the ID of the button
      * @param enabled the enabled state
      */
     public void setShortcutEnabled(int id, boolean enabled) {
         // Shortcuts not supported by the base screen class
     }
     
     /**
      * Shows the virtual keyboard, if applicable to this device
      */
     public void showVirtualKeyboard() {
         // Virtual keyboard not supported by the base screen class
     }
 }
