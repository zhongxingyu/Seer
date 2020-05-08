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
 
 import net.rim.device.api.i18n.ResourceBundle;
 import net.rim.device.api.ui.Screen;
 import net.rim.device.api.ui.UiApplication;
 import net.rim.device.api.ui.component.Dialog;
 import net.rim.device.api.ui.component.Status;
 
 import org.logicprobe.LogicMail.LogicMailResource;
 import org.logicprobe.LogicMail.mail.MailConnectionListener;
 import org.logicprobe.LogicMail.mail.MailConnectionLoginEvent;
 import org.logicprobe.LogicMail.mail.MailConnectionManager;
 import org.logicprobe.LogicMail.mail.MailConnectionStateEvent;
 import org.logicprobe.LogicMail.mail.MailConnectionStatusEvent;
 import org.logicprobe.LogicMail.model.MailManager;
 import org.logicprobe.LogicMail.model.MailRootNode;
 import org.logicprobe.LogicMail.model.MailboxNode;
 import org.logicprobe.LogicMail.model.MessageNode;
 import org.logicprobe.LogicMail.model.NetworkAccountNode;
 import org.logicprobe.LogicMail.util.EventObjectRunnable;
 
 /**
  * Controller class for screen creation.
  * This class is intended to hide all screen creation logic
  * from the rest of the UI, and provide a simple interface
  * in terms of viewing data model objects.
  */
 public final class NavigationController {
 	private static ResourceBundle resources = ResourceBundle.getBundle(LogicMailResource.BUNDLE_ID, LogicMailResource.BUNDLE_NAME);
 	private ScreenFactory screenFactory;
 	private MailRootNode mailRootNode;
 	
 	private UiApplication uiApplication;
 	private StandardScreen mailHomeScreen;
 	
 	private MessageActions messageActions;
 	
 	public NavigationController(UiApplication uiApplication) {
 		this.uiApplication = uiApplication;
 		this.screenFactory = ScreenFactory.getInstance();
 		this.mailRootNode = MailManager.getInstance().getMailRootNode();
 		this.messageActions = new MessageActions(this);
 		
 		MailConnectionManager.getInstance().addMailConnectionListener(new MailConnectionListener() {
 			public void mailConnectionStateChanged(MailConnectionStateEvent e) { }
 			public void mailConnectionStatus(MailConnectionStatusEvent e) {
 				handleMailConnectionStatus(e);
 			}
 			public void mailConnectionError(MailConnectionStatusEvent e) {
 				handleMailConnectionError(e);
 			}
 			public void mailConnectionLogin(MailConnectionLoginEvent e) {
 				handleMailConnectionLogin(e);
 			}
 		});
 	}
 
 	public synchronized void displayMailHome() {
 		if(mailHomeScreen == null) {
 			mailHomeScreen = screenFactory.getMailHomeScreen(this, mailRootNode);
 		}
 		uiApplication.pushScreen(mailHomeScreen);
 	}
 	
 	public synchronized void displayAccountConfigurationWizard() {
 		UiApplication.getUiApplication().invokeLater(new Runnable() {
 			public void run() {
 				// Start the new account configuration wizard
 				AccountConfigWizard wizard = new AccountConfigWizard();
 				wizard.start();
 			}
 		});
 	}
 	
 	public synchronized void displayMailbox(MailboxNode mailboxNode) {
 		StandardScreen screen = screenFactory.getMailboxScreen(this, mailboxNode);
         ScreenFactory.getInstance().attachScreenTransition(screen, ScreenFactory.TRANSITION_SLIDE);
 		uiApplication.pushScreen(screen);
 	}
 	
 	public synchronized void displayMessage(MessageNode messageNode) {
 		StandardScreen screen = screenFactory.getMessageScreen(this, messageNode);
         ScreenFactory.getInstance().attachScreenTransition(screen, ScreenFactory.TRANSITION_SLIDE);
 		uiApplication.pushScreen(screen);
 	}
 	
 	public synchronized void displayComposition(NetworkAccountNode accountNode) {
 		StandardScreen screen = screenFactory.getCompositionScreen(this, accountNode);
         ScreenFactory.getInstance().attachScreenTransition(screen, ScreenFactory.TRANSITION_ZOOM);
 		uiApplication.pushScreen(screen);
 	}
 
 	public synchronized void displayComposition(NetworkAccountNode accountNode, MessageNode messageNode) {
 		StandardScreen screen = screenFactory.getCompositionScreen(
 				this,
 				accountNode,
 				messageNode);
         ScreenFactory.getInstance().attachScreenTransition(screen, ScreenFactory.TRANSITION_ZOOM);
 		uiApplication.pushScreen(screen);
 	}
 
 	public void displayComposition(NetworkAccountNode accountNode, String address) {
 		StandardScreen screen = screenFactory.getCompositionScreen(this, accountNode, address);
         ScreenFactory.getInstance().attachScreenTransition(screen, ScreenFactory.TRANSITION_ZOOM);
 		uiApplication.pushScreen(screen);
 	}
 	
 	public synchronized void displayCompositionReply(NetworkAccountNode accountNode, MessageNode messageNode, boolean replyAll) {
 		StandardScreen screen = screenFactory.getCompositionReplyScreen(
 				this,
 				accountNode,
 				messageNode,
 				replyAll);
         ScreenFactory.getInstance().attachScreenTransition(screen, ScreenFactory.TRANSITION_ZOOM);
 		uiApplication.pushScreen(screen);
 	}
 
 	public synchronized void displayCompositionForward(NetworkAccountNode accountNode, MessageNode messageNode) {
 		StandardScreen screen = screenFactory.getCompositionForwardScreen(
 				this,
 				accountNode,
 				messageNode);
         ScreenFactory.getInstance().attachScreenTransition(screen, ScreenFactory.TRANSITION_ZOOM);
 		uiApplication.pushScreen(screen);
 	}
 	
 	/**
 	 * Gets the delegate for handling actions on message nodes.
 	 * 
 	 * @return the message actions delegate instance
 	 */
 	public MessageActions getMessageActions() {
 	    return this.messageActions;
 	}
 	
 	/**
 	 * Gets the current status text.
 	 * 
 	 * @return the current status text
 	 */
 	public String getCurrentStatus() {
 		return statusRunnable.getCurrentStatus();
 	}
 	
 	private void handleMailConnectionStatus(MailConnectionStatusEvent e) {
 	    statusRunnable.updateStatus(e);
 	}
 
 	private final MailConnectionStatusRunnable statusRunnable = new MailConnectionStatusRunnable();
 	
 	private static class MailConnectionStatusRunnable implements Runnable {
         private final Object lockObj = new Object();
 	    private MailConnectionStatusEvent event;
 	    private boolean onQueue;
 	    
 	    public void updateStatus(MailConnectionStatusEvent event) {
 	        synchronized(lockObj) {
 	            this.event = event;
 	            if(!onQueue) {
 	                UiApplication.getUiApplication().invokeLater(this);
 	            }
 	        }
         }
 	    
 	    public String getCurrentStatus() {
 	        String currentStatus;
 	        synchronized(lockObj) {
 	            if(event != null) {
 	                currentStatus = event.getMessage();
 	            }
 	            else {
 	                currentStatus = "";
 	            }
 	        }
 	        return currentStatus;
 	    }
 	    
 	    public void run() {
 	        synchronized(lockObj) {
 	            if(event != null) {
 	                Screen activeScreen =
 	                    UiApplication.getUiApplication().getActiveScreen();
 	                if(activeScreen instanceof StandardScreen) {
 	                    StandardScreen screen = (StandardScreen)activeScreen;
 	                    screen.updateStatus(event.getMessage());
 	                }
 	            }
 	            onQueue = false;
 	        }
 	    }
 	}
 	
 	private void handleMailConnectionError(MailConnectionStatusEvent e) {
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
 
 	private void handleMailConnectionLogin(MailConnectionLoginEvent e) {
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
