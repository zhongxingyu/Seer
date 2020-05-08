 /*-
  * Copyright (c) 2006, Derek Konigsberg
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
 
 import net.rim.device.api.i18n.ResourceBundle;
 import net.rim.device.api.ui.MenuItem;
 import net.rim.device.api.ui.Screen;
 import net.rim.device.api.ui.UiApplication;
 import net.rim.device.api.ui.component.Dialog;
 import net.rim.device.api.ui.component.LabelField;
 import net.rim.device.api.ui.component.Menu;
 import net.rim.device.api.ui.component.Status;
 import net.rim.device.api.ui.container.MainScreen;
 import org.logicprobe.LogicMail.LogicMailResource;
 import org.logicprobe.LogicMail.mail.MailConnectionManager;
 import org.logicprobe.LogicMail.mail.MailConnectionListener;
 import org.logicprobe.LogicMail.mail.MailConnectionLoginEvent;
 import org.logicprobe.LogicMail.mail.MailConnectionStateEvent;
 import org.logicprobe.LogicMail.mail.MailConnectionStatusEvent;
 import org.logicprobe.LogicMail.model.AccountNode;
 import org.logicprobe.LogicMail.model.MailManager;
 import org.logicprobe.LogicMail.util.EventObjectRunnable;
 
 /**
  * This class is the base for all screens in LogicMail.
  * Its purpose is to provide uniform menu and event
  * handler interfaces across the application.
  */
 public abstract class BaseScreen extends MainScreen {
 	protected static ResourceBundle resources = ResourceBundle.getBundle(LogicMailResource.BUNDLE_ID, LogicMailResource.BUNDLE_NAME);
 	private LabelField statusLabel;
 	private boolean isExposed = false;
 	
 	private final static int MENU_CONTEXT = 0x10000;
 	private final static int MENU_MAIN = 0x40000000;
 	
     public BaseScreen() {
         super();
 		statusLabel = new LabelField();
 		setStatus(null);
     }
 
     public BaseScreen(String title) {
         super();
         // Create screen elements
        HeaderField headerField = new HeaderField("LogicMail - " + title);
         setTitle(headerField);
 		statusLabel = new LabelField();
 		setStatus(null);
     }
     
     private MailConnectionListener mailConnectionListener = new MailConnectionListener() {
 		public void mailConnectionStateChanged(MailConnectionStateEvent e) { }
 		public void mailConnectionStatus(MailConnectionStatusEvent e) {
 			if(isExposed) {
 				mailConnectionListener_MailConnectionStatus(e);
 			}
 		}
 		public void mailConnectionError(MailConnectionStatusEvent e) {
 			if(isExposed) {
 				mailConnectionListener_MailConnectionError(e);
 			}
 		}
 		public void mailConnectionLogin(MailConnectionLoginEvent e) {
 			if(isExposed) {
 				mailConnectionListener_MailConnectionLogin(e);
 			}
 		}
     };
 
     protected void onDisplay() {
     	super.onDisplay();
     	isExposed = true;
     	MailConnectionManager.getInstance().addMailConnectionListener(mailConnectionListener);
     }
     
     protected void onUndisplay() {
     	isExposed = false;
     	MailConnectionManager.getInstance().removeMailConnectionListener(mailConnectionListener);
     	super.onUndisplay();
     }
     
     protected void onExposed() {
     	super.onExposed();
     	isExposed = true;
     }
     
     protected void onObscured() {
     	super.onObscured();
     	isExposed = false;
     }
     
     public boolean onMenu(int instance) {
 		if (instance == MENU_MAIN) {
 			// Main menu button pressed, display menu
 			return super.onMenu(instance);
 		}
 		else if (instance == MENU_CONTEXT) {
 			// Trackball click, call override method
 			if(!onClick()) {
 				return super.onMenu(instance);
 			}
 			else {
 				return false;
 			}
 		}
 		else {
 			// Trackwheel click, display menu
 			return super.onMenu(instance);
 		}
 	}
     
     /**
      * Invoked when the user clicks the trackball on
      * devices that have a separate menu button.
      * 
      * @return True if the click was handled, false to fall
      *         through and display the context menu.
      */
     protected boolean onClick() {
     	return false;
     }
     
     // Create menu items
     private MenuItem configItem = new MenuItem(resources.getString(LogicMailResource.MENUITEM_CONFIGURATION), 10020, 10) {
         public void run() {
             showConfigScreen();
         }
     };
     private MenuItem aboutItem = new MenuItem(resources.getString(LogicMailResource.MENUITEM_ABOUT), 10050, 10) {
         public void run() {
             // Show the about dialog
         	AboutDialog dialog = new AboutDialog();
         	dialog.doModal();
         }
     };
     private MenuItem closeItem = new MenuItem(resources.getString(LogicMailResource.MENUITEM_CLOSE), 200000, 10) {
         public void run() {
         	// TODO: Deal with closing/hiding while still running
         	
             onClose();
         }
     };
     private MenuItem exitItem = new MenuItem(resources.getString(LogicMailResource.MENUITEM_EXIT), 200001, 10) {
         public void run() {
         	tryShutdownApplication();
         }
     };
 
     protected void tryShutdownApplication() {
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
                
                 System.exit(0);
             }
         }
         else {
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
     
     protected void makeMenu(Menu menu, int instance) {
         menu.addSeparator();
         menu.add(configItem);
         menu.add(aboutItem);
         menu.add(closeItem);
         menu.add(exitItem);
     }
 
     /**
      * Disable save-prompting dialog since it
      * isn't applicable to most BaseScreen
      * implementations
      */
     protected boolean onSavePrompt() {
         return true;
     }
     
 	/**
 	 * Invoked when there is a change in status from
 	 * the mail connection.
 	 *
 	 * @param e Status event data
 	 */
     protected void mailConnectionListener_MailConnectionStatus(MailConnectionStatusEvent e) {
 		UiApplication.getUiApplication().invokeLater(new EventObjectRunnable(e) {
 			public void run() {
 		    	String message = ((MailConnectionStatusEvent)getEvent()).getMessage();
 		    	if(message != null) {
 		    		statusLabel.setText(message);
 		    		setStatus(statusLabel);
 		    	}
 		    	else {
 		    		statusLabel.setText("");
 		    		setStatus(null);
 		    	}
 			}
 		});
     }
     
 	/**
 	 * Invoked when an error occurs with the mail connection.
 	 *
 	 * @param e Error event data
 	 */
     protected void mailConnectionListener_MailConnectionError(MailConnectionStatusEvent e) {
 		UiApplication.getUiApplication().invokeLater(new EventObjectRunnable(e) {
 			public void run() {
 				String message = ((MailConnectionStatusEvent)getEvent()).getMessage();
 				if(message == null) { message = resources.getString(LogicMailResource.ERROR_UNKNOWN); }
 	            try {
 	                Screen activeScreen =
 	                        UiApplication.getUiApplication().getActiveScreen();
 	                if(activeScreen instanceof Status) {
 	                    UiApplication.getUiApplication().popScreen(activeScreen);
 	                }
 	            } catch (Exception e) { }
 	            Status.show(message, 5000);
 			}
 		});
     }
     
 	/**
 	 * Invoked when the mail connection needs login
 	 * information to be provided by the user interface.
 	 * 
 	 * @param e Login event data
 	 */
     protected void mailConnectionListener_MailConnectionLogin(MailConnectionLoginEvent e) {
 		UiApplication.getUiApplication().invokeAndWait(new EventObjectRunnable(e) {
 			public void run() {
 				MailConnectionLoginEvent e = (MailConnectionLoginEvent)getEvent();
 				LoginDialog dialog = new LoginDialog(e.getUsername(), e.getPassword());
 				if(dialog.doModal() == Dialog.OK) {
 					e.setUsername(dialog.getUsername());
 					e.setPassword(dialog.getPassword());
 				}
 				else {
 					e.setCanceled(true);
 				}
 			}
 		});
     }
 }
