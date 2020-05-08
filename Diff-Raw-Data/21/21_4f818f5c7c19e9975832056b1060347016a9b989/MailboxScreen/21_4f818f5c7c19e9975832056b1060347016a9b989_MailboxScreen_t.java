 /*-
  * Copyright (c) 2008, Derek Konigsberg
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
 
 import java.util.Hashtable;
 import java.util.Vector;
 import net.rim.device.api.ui.Field;
 import net.rim.device.api.ui.Keypad;
 import net.rim.device.api.ui.Manager;
 import net.rim.device.api.ui.MenuItem;
 import net.rim.device.api.ui.component.Menu;
 import net.rim.device.api.ui.component.Dialog;
 import net.rim.device.api.ui.container.VerticalFieldManager;
 import net.rim.device.api.ui.UiApplication;
 import net.rim.device.api.util.Comparator;
 
 import org.logicprobe.LogicMail.LogicMailResource;
 import org.logicprobe.LogicMail.conf.AccountConfig;
 import org.logicprobe.LogicMail.conf.MailSettings;
 import org.logicprobe.LogicMail.model.AccountNode;
 import org.logicprobe.LogicMail.model.MailManager;
 import org.logicprobe.LogicMail.model.MailboxNode;
 import org.logicprobe.LogicMail.model.MailboxNodeEvent;
 import org.logicprobe.LogicMail.model.MailboxNodeListener;
 import org.logicprobe.LogicMail.model.MessageNode;
 import org.logicprobe.LogicMail.model.MessageNodeEvent;
 import org.logicprobe.LogicMail.model.MessageNodeListener;
 import org.logicprobe.LogicMail.util.EventObjectRunnable;
 
 /**
  * Display the active mailbox listing.
  * If the supplied folder matches the configured sent folder
  * for the provided account, then the display fields will be
  * adjusted accordingly.
  */
 public class MailboxScreen extends BaseScreen {
 	private MailboxNode mailboxNode;
     private Vector knownMessages;
     private Hashtable messageFieldMap;
     private boolean firstDisplay = true;
     private MailSettings mailSettings;
     private VerticalFieldManager messageFieldManager;
     
     /**
      * Initializes a new MailboxScreen to view the provided mailbox.
      * 
      * @param navigationController Controller for screen navigation
      * @param mailboxNode Mailbox node to view.
      */
     public MailboxScreen(NavigationController navigationController, MailboxNode mailboxNode) {
     	super(navigationController, mailboxNode.toString());
     	this.mailboxNode = mailboxNode;
     	this.knownMessages = new Vector();
     	this.messageFieldMap = new Hashtable();
     	mailSettings = MailSettings.getInstance();
     	
     	initFields();
     }
 
     /**
      * Gets the mailbox node being displayed by this screen.
      * 
      * @return The mailbox node
      */
     public MailboxNode getMailboxNode() {
     	return mailboxNode;
     }
     
     /** The mailbox node listener. */
     private MailboxNodeListener mailboxNodeListener = new MailboxNodeListener() {
 		public void mailboxStatusChanged(MailboxNodeEvent e) {
 			UiApplication.getUiApplication().invokeLater(new EventObjectRunnable(e) {
 				public void run() {
 					mailboxNode_MailboxStatusChanged((MailboxNodeEvent)getEvent());
 				}
 			});
 		}
     };
     
     /** The message node listener. */
     private MessageNodeListener messageNodeListener = new MessageNodeListener() {
 		public void messageStatusChanged(MessageNodeEvent e) {
 			UiApplication.getUiApplication().invokeLater(new EventObjectRunnable(e) {
 				public void run() {
 					messageNode_MessageStatusChanged((MessageNodeEvent)getEvent());
 				}
 			});
 		}
     };
 
     /* (non-Javadoc)
      * @see org.logicprobe.LogicMail.ui.BaseScreen#onDisplay()
      */
     protected void onDisplay() {
     	super.onDisplay();
         this.mailboxNode.addMailboxNodeListener(mailboxNodeListener);
         if(firstDisplay) {
             MessageNode[] initialMessages = this.mailboxNode.getMessages();
             for(int i=0; i<initialMessages.length; i++) {
             	knownMessages.addElement(initialMessages[i]);
             	if(isMessageDisplayable(initialMessages[i])) {
             		insertDisplayableMessage(initialMessages[i]);
             	}
             }
             
         	this.mailboxNode.refreshMessages();
         	firstDisplay = false;
         }
         int size = knownMessages.size();
         for(int i=0; i<size; i++) {
         	((MessageNode)knownMessages.elementAt(i)).addMessageNodeListener(messageNodeListener);
         }
         
         // TODO: Support message list changes between display pushing
         // TODO: Support updating when messages are deleted
     }
     
 	/* (non-Javadoc)
 	 * @see org.logicprobe.LogicMail.ui.BaseScreen#onUndisplay()
 	 */
 	protected void onUndisplay() {
         this.mailboxNode.removeMailboxNodeListener(mailboxNodeListener);
         int size = knownMessages.size();
         for(int i=0; i<size; i++) {
         	((MessageNode)knownMessages.elementAt(i)).removeMessageNodeListener(messageNodeListener);
         }
         
     	super.onUndisplay();
     }
     
 	/**
 	 * Initializes the fields.
 	 */
 	private void initFields() {
 		messageFieldManager = new VerticalFieldManager(Manager.USE_ALL_WIDTH | Manager.USE_ALL_HEIGHT);
         add(messageFieldManager);
 	}    
 
     /* (non-Javadoc)
      * @see org.logicprobe.LogicMail.ui.BaseScreen#onSavePrompt()
      */
     protected boolean onSavePrompt() {
         return true;
     }
 
     private MenuItem selectItem = new MenuItem(resources, LogicMailResource.MENUITEM_SELECT, 100, 10) {
         public void run() {
             openSelectedMessage();
         }
     };
     private MenuItem propertiesItem = new MenuItem(resources, LogicMailResource.MENUITEM_PROPERTIES, 105, 10) {
         public void run() {
             openSelectedMessageProperties();
         }
     };
     private MenuItem compositionItem = new MenuItem(resources, LogicMailResource.MENUITEM_COMPOSE_EMAIL, 120, 10) {
         public void run() {
         	getNavigationController().displayComposition(mailboxNode.getParentAccount());
         }
     };
     private MenuItem deleteItem = new MenuItem(resources, LogicMailResource.MENUITEM_DELETE, 130, 10) {
         public void run() {
         	deleteSelectedMessage();
         }
     };
     private MenuItem undeleteItem = new MenuItem(resources, LogicMailResource.MENUITEM_UNDELETE, 135, 10) {
         public void run() {
         	undeleteSelectedMessage();
         }
     };
 
     /* (non-Javadoc)
      * @see org.logicprobe.LogicMail.ui.BaseScreen#makeMenu(net.rim.device.api.ui.component.Menu, int)
      */
     protected void makeMenu(Menu menu, int instance) {
     	Field fieldWithFocus = messageFieldManager.getFieldWithFocus();
     	if(fieldWithFocus instanceof MailboxMessageField) {
     		menu.add(selectItem);
     		menu.add(propertiesItem);
     	}
         if(mailboxNode.getParentAccount().hasMailSender()) {
             menu.add(compositionItem);
         }
         if(fieldWithFocus instanceof MailboxMessageField) {
         	MessageNode messageNode = ((MailboxMessageField)fieldWithFocus).getMessageNode();
             if((messageNode.getFlags() & MessageNode.Flag.DELETED) != 0) {
                 if(mailboxNode.getParentAccount().hasUndelete()) {
                     menu.add(undeleteItem);
                 }
             }
             else {
                 menu.add(deleteItem);
             }
         }
         super.makeMenu(menu, instance);
     }
 
     /**
      * Handles mailbox status change events.
      * 
      * @param e Event data.
      */
     private void mailboxNode_MailboxStatusChanged(MailboxNodeEvent e) {
     	if(e.getType() == MailboxNodeEvent.TYPE_NEW_MESSAGES) {
     		MessageNode[] messageNodes = e.getAffectedMessages();
     		for(int i=0; i<messageNodes.length; i++) {
     			knownMessages.addElement(messageNodes[i]);
     			
     			if(isMessageDisplayable(messageNodes[i])) {
     				// Insert the message
 	    			insertDisplayableMessage(messageNodes[i]);
     			}
     			
     			if(isDisplayed()) {
     				messageNodes[i].addMessageNodeListener(messageNodeListener);
     			}
     		}
     	}
 	}
     
     /**
      * Determines whether a message should be displayed,
      * per the configuration.
      * 
      * @param messageNode Message to check.
      * 
      * @return True if it should be displayed, false otherwise.
      */
     private boolean isMessageDisplayable(MessageNode messageNode) {
     	if((messageNode.getFlags() & MessageNode.Flag.DELETED) != 0 &&
     	   mailSettings.getGlobalConfig().getHideDeletedMsg()) {
     		return false;
     	}
     	else {
     		return true;
     	}
     }
     
     /**
      * Insert a message into the list and associated data structures.
      * This will insert into the correct order, per the configuration.
      * 
      * @param messageNode Message to insert.
      */
     private void insertDisplayableMessage(MessageNode messageNode) {
     	// TODO: Make sorting by mailbox order vs message date an option
     	int selectedIndex = messageFieldManager.getFieldWithFocusIndex();
     	
 		if(messageFieldManager.getFieldCount() > 0) {
 			Comparator comparator = MessageNode.getComparator();
 			int index = messageFieldManager.getFieldCount();
 			
 			if(mailSettings.getGlobalConfig().getDispOrder()) {
 				// Ascending order
 				MessageNode lastMessage = getLastDisplayedMessage(index - 1);
 				while(lastMessage != null && index > 0 && comparator.compare(lastMessage, messageNode) >= 0) {
 					index--;
 					if(index > 0) { lastMessage = getLastDisplayedMessage(index - 1); }
 				}
 			}
 			else {
 				// Descending order
 				MessageNode lastMessage = getLastDisplayedMessage(index - 1);
 				while(lastMessage != null && index > 0 && comparator.compare(lastMessage, messageNode) <= 0) {
 					index--;
 					if(index > 0) { lastMessage = getLastDisplayedMessage(index - 1); }
 				}
 			}
 			MailboxMessageField mailboxMessageField =
 				new MailboxMessageField(mailboxNode, messageNode, Field.USE_ALL_WIDTH | Field.FOCUSABLE);
 			messageFieldMap.put(messageNode, mailboxMessageField);
 			messageFieldManager.insert(
 					mailboxMessageField,
 					index);
 			if(selectedIndex != -1) { messageFieldManager.getField(selectedIndex).setFocus(); }
 		}
 		else {
 			MailboxMessageField mailboxMessageField =
 				new MailboxMessageField(mailboxNode, messageNode, Field.USE_ALL_WIDTH | Field.FOCUSABLE);
 			messageFieldMap.put(messageNode, mailboxMessageField);
 			messageFieldManager.insert(
 					mailboxMessageField,
 					0);
 			if(selectedIndex != -1) { messageFieldManager.getField(selectedIndex).setFocus(); }
 		}
     }
 
     /**
      * Gets the last displayed message.
      * 
      * @param index the index
      * 
      * @return the last displayed message
      */
     private MessageNode getLastDisplayedMessage(int index) {
     	while(index >= 0) {
     		if(messageFieldManager.getField(index) instanceof MailboxMessageField) {
     			return ((MailboxMessageField)messageFieldManager.getField(index)).getMessageNode();
     		}
     		index--;
     	}
     	return null;
     }
     
     /**
      * Handles message status change events.
      * 
      * @param e Event data.
      */
 	private void messageNode_MessageStatusChanged(MessageNodeEvent e) {
 		if(e.getType() == MessageNodeEvent.TYPE_FLAGS) {
 			MessageNode messageNode = (MessageNode)e.getSource();
 			boolean currentlyDisplayed = messageFieldMap.containsKey(messageNode);
 			boolean displayable = isMessageDisplayable(messageNode);
 			
 			if(currentlyDisplayed && !displayable) {
 				// Remove from display
 				MailboxMessageField mailboxMessageField = (MailboxMessageField)messageFieldMap.get(messageNode);
 				messageFieldManager.delete(mailboxMessageField);
 				messageFieldMap.remove(messageNode);
 			}
 			else if(!currentlyDisplayed && displayable) {
 				// Add to display
 				insertDisplayableMessage(messageNode);
 			}
 			else if(currentlyDisplayed) {
 				// Just a visual flag update, so find and invalidate the item
 				MailboxMessageField mailboxMessageField = (MailboxMessageField)messageFieldMap.get(messageNode);
 				mailboxMessageField.invalidate();
 			}
 		}
 	}
 
     /**
      * Gets the selected message.
      * 
      * @return the selected message
      */
     private MessageNode getSelectedMessage() {
     	Field fieldWithFocus = messageFieldManager.getFieldWithFocus();
     	if(fieldWithFocus instanceof MailboxMessageField) {
     		return ((MailboxMessageField)fieldWithFocus).getMessageNode();
     	}
     	else {
     		return null;
     	}
     }
     
     /**
      * Opens the selected message.
      * 
      * @return True, if successful
      */
     private boolean openSelectedMessage()
     {
     	MessageNode messageNode = getSelectedMessage();
     	if(messageNode != null) {
     		if(mailboxNode.getType() == MailboxNode.TYPE_DRAFTS) {
     			openDraftMessage(messageNode);
     		}
     		else {
     			getNavigationController().displayMessage(messageNode);
     		}
     		return true;
     	}
     	else {
     		return false;
     	}
     }
 
     private boolean openDraftMessage(MessageNode messageNode) {
     	// Build a list of all the accounts that have this mailbox
     	// configured as their drafts folder.
     	Vector matchingAccounts = new Vector();
     	AccountNode[] accounts = MailManager.getInstance().getMailRootNode().getAccounts();

     	for(int i=0; i<accounts.length; i++) {
     		AccountConfig accountConfig = accounts[i].getAccountConfig();
     		if(accountConfig != null) {
     			if(accountConfig.getDraftMailbox() == mailboxNode) {
     				matchingAccounts.addElement(accounts[i]);
     			}
     		}
     	}
 
    	// If no matching accounts were found, then add all
    	// non-local accounts so we have something for the
    	// user to select from.
    	if(matchingAccounts.size() == 0) {
        	for(int i=0; i<accounts.length; i++) {
        		if(accounts[i].getAccountConfig() != null) {
    				matchingAccounts.addElement(accounts[i]);
        		}
        	}
    	}
    	
     	// Select the account node that matches this mailbox, prompting the
     	// user if necessary.
     	AccountNode account;
     	int size = matchingAccounts.size();
     	if(size > 1) {
     		AccountNode[] choices = new AccountNode[size];
     		matchingAccounts.copyInto(choices);
         	int result = Dialog.ask(
     			resources.getString(LogicMailResource.MAILBOX_DRAFT_MULTIPLE_ACCOUNTS),
     			choices, 0);
         	if(result != -1) {
         		account = choices[result];
         	}
         	else {
         		return false;
         	}
     	}
    	else if(size == 1) {
     		account = (AccountNode)matchingAccounts.elementAt(0);
     	}
    	else {
    		return false;
    	}
 
     	// Show the message composition screen
     	getNavigationController().displayComposition(account, messageNode);
 
 		return true;
     }
     
     /**
      * Open selected message properties.
      */
     private void openSelectedMessageProperties()
     {
     	MessageNode messageNode = getSelectedMessage();
     	if(messageNode != null) {
 	        MessagePropertiesScreen propertiesScreen =
 	        	new MessagePropertiesScreen(messageNode);
 	        UiApplication.getUiApplication().pushModalScreen(propertiesScreen);
     	}
     }
 
     /**
      * Delete selected message.
      * 
      * @return True, if successful
      */
     private boolean deleteSelectedMessage() {
     	MessageNode messageNode = getSelectedMessage();
     	if(messageNode != null) {
 	        if(Dialog.ask(Dialog.D_YES_NO, resources.getString(LogicMailResource.MAILBOX_DELETE_PROMPT)) == Dialog.YES) {
 	        	messageNode.deleteMessage();
 	        }
 	        return true;
     	}
     	else {
     		return false;
     	}
     }
     
     /**
      * Undelete selected message.
      */
     private void undeleteSelectedMessage() {
     	MessageNode messageNode = getSelectedMessage();
     	if(messageNode != null) {
     		messageNode.undeleteMessage();
     	}
     }
     
     /* (non-Javadoc)
      * @see org.logicprobe.LogicMail.ui.BaseScreen#onClick()
      */
     protected boolean onClick() {
     	return openSelectedMessage();
     }
     
     /* (non-Javadoc)
      * @see net.rim.device.api.ui.Screen#keyChar(char, int, int)
      */
     public boolean keyChar(char key, int status, int time) {
         boolean retval = false;
         switch(key) {
             case Keypad.KEY_ENTER:
                 retval = openSelectedMessage();
                 break;
             case Keypad.KEY_BACKSPACE:
             	retval = deleteSelectedMessage();
             	break;
         }
         return retval;
     }
 }
