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
 
 import org.logicprobe.LogicMail.LogicMailResource;
 import org.logicprobe.LogicMail.model.AccountNode;
 import org.logicprobe.LogicMail.model.MailManager;
 
 import net.rim.device.api.i18n.ResourceBundle;
 import net.rim.device.api.ui.Field;
 import net.rim.device.api.ui.MenuItem;
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
 	private HeaderField headerField;
 	private Field originalStatusField;
     
     private MenuItem configItem;
     private MenuItem aboutItem;
     private MenuItem closeItem;
     private MenuItem exitItem;
 	
 	protected ScreenProvider screenProvider;
 	
     /**
      * Instantiates a new standard screen.
      * 
      * @param navigationController the navigation controller
      * @param screenProvider the screen provider
      */
     public StandardScreen(NavigationController navigationController, ScreenProvider screenProvider) {
         super();
         if(navigationController == null || screenProvider == null) {
         	throw new IllegalArgumentException();
         }
         
         this.navigationController = navigationController;
         this.screenProvider = screenProvider;
         initialize();
     }
 
     /**
      * Initialize the screen elements.
      */
     private void initialize() {
         // Create screen elements
         if(screenProvider.getTitle() != null) {
 	        this.headerField = new HeaderField("LogicMail - " + screenProvider.getTitle());
 	        setTitle(headerField);
         }
         
         initMenuItems();
         screenProvider.setNavigationController(navigationController);
         screenProvider.initFields(this);
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
     * that makes sure <code>IllegalStateException</code>s don't appear
     * if the field had previously been added.
     * 
     * @param status the new status field
     */
    private void superSetStatusImpl(Field status) {
        if(status != null && status.getManager() != null) {
            status.getManager().delete(status);
        }
        super.setStatus(status);
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
      * @see net.rim.device.api.ui.Screen#onDisplay()
      */
     protected void onDisplay() {
     	super.onDisplay();
     	updateStatus(navigationController.getCurrentStatus());
     	NotificationHandler.getInstance().cancelNotification();
     	screenProvider.onDisplay();
     }
     
     /* (non-Javadoc)
      * @see net.rim.device.api.ui.Screen#onUndisplay()
      */
     protected void onUndisplay() {
     	screenProvider.onUndisplay();
    	superSetStatusImpl(originalStatusField);
 		statusBarField.setStatusText(null);
     	NotificationHandler.getInstance().cancelNotification();
     	super.onUndisplay();
     }
     
     /* (non-Javadoc)
      * @see net.rim.device.api.ui.Screen#onExposed()
      */
     protected void onExposed() {
     	super.onExposed();
     	updateStatus(navigationController.getCurrentStatus());
     }
     
     /* (non-Javadoc)
      * @see net.rim.device.api.ui.Screen#onObscured()
      */
     protected void onObscured() {
     	super.onObscured();
    	superSetStatusImpl(originalStatusField);
    		statusBarField.setStatusText(null);
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
 	    configItem = new MenuItem(resources, LogicMailResource.MENUITEM_CONFIGURATION, 10020, 10) {
 	        public void run() {
 	            showConfigScreen();
 	        }
 	    };
 	    aboutItem = new MenuItem(resources, LogicMailResource.MENUITEM_ABOUT, 10050, 10) {
 	        public void run() {
 	            // Show the about dialog
 	        	AboutDialog dialog = new AboutDialog();
 	        	dialog.doModal();
 	        }
 	    };
 	    closeItem = new MenuItem(resources, LogicMailResource.MENUITEM_CLOSE, 200000, 10) {
 	        public void run() {
 	        	// TODO: Deal with closing/hiding while still running
 	        	
 	            StandardScreen.super.onClose();
 	        }
 	    };
 	    exitItem = new MenuItem(resources, LogicMailResource.MENUITEM_EXIT, 200001, 10) {
 	        public void run() {
 	        	tryShutdownApplication();
 	        }
 	    };
     }
     
     public void tryShutdownApplication() {
     	// Get all accounts
     	AccountNode[] accounts = MailManager.getInstance().getMailRootNode().getAccounts();
     	
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
                 headerField.removeListeners();
                 NotificationHandler.getInstance().shutdown();
                 System.exit(0);
             }
         }
         else {
             headerField.removeListeners();
             NotificationHandler.getInstance().shutdown();
             System.exit(0);
         }
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
         menu.addSeparator();
         menu.add(configItem);
         menu.add(aboutItem);
         menu.add(closeItem);
         menu.add(exitItem);
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
     	boolean result = screenProvider.navigationClick(status, time);
     	if(!result) {
     		result = super.navigationClick(status, time);
     	}
     	return result;
     }
 
     /* (non-Javadoc)
      * @see net.rim.device.api.ui.Screen#keyChar(char, int, int)
      */
     protected boolean keyChar(char c, int status, int time) {
     	boolean result = screenProvider.keyChar(c, status, time);
     	if(!result) {
     		result = super.keyChar(c, status, time);
     	}
     	return result;
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
 }
