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
 
 import java.util.Vector;
 import net.rim.device.api.system.Application;
 import net.rim.device.api.system.KeypadListener;
 import net.rim.device.api.ui.Field;
 import net.rim.device.api.ui.Keypad;
 import net.rim.device.api.ui.Manager;
 import net.rim.device.api.ui.MenuItem;
 import net.rim.device.api.ui.component.Menu;
 import net.rim.device.api.ui.component.RichTextField;
 import net.rim.device.api.ui.component.SeparatorField;
 import net.rim.device.api.ui.component.NullField;
 import net.rim.device.api.ui.container.VerticalFieldManager;
 import net.rim.device.api.ui.UiApplication;
 import org.logicprobe.LogicMail.LogicMailResource;
 import org.logicprobe.LogicMail.conf.AccountConfig;
 import org.logicprobe.LogicMail.message.Message;
 import org.logicprobe.LogicMail.message.MessageEnvelope;
 import org.logicprobe.LogicMail.model.AccountNode;
 import org.logicprobe.LogicMail.model.MailManager;
 import org.logicprobe.LogicMail.model.MailboxNode;
 import org.logicprobe.LogicMail.model.MessageNode;
 import org.logicprobe.LogicMail.model.MessageNodeEvent;
 import org.logicprobe.LogicMail.model.MessageNodeListener;
 
 /**
  * Display an E-Mail message
  */
 public class MessageScreen extends BaseScreen {
 	private BorderedFieldManager addressFieldManager;
 	private BorderedFieldManager subjectFieldManager;
 	private VerticalFieldManager messageFieldManager;
 	
 	private AccountConfig accountConfig;
     private MessageNode messageNode;
     private MessageEnvelope envelope;
     private boolean isSentFolder;
     private boolean messageRendered;
     private ThrobberField throbberField;
     
     public MessageScreen(MessageNode messageNode)
     {
         super(Manager.VERTICAL_SCROLLBAR);
         this.messageNode = messageNode;
         this.accountConfig = messageNode.getParent().getParentAccount().getAccountConfig();
         this.envelope = messageNode.getFolderMessage().getEnvelope();
         
         // Determine if this screen is viewing a sent message
         int mailboxType = messageNode.getParent().getType();
         this.isSentFolder = (mailboxType == MailboxNode.TYPE_SENT) || (mailboxType == MailboxNode.TYPE_OUTBOX);
         
         // Create screen elements
         addressFieldManager = new BorderedFieldManager(
         		Manager.NO_HORIZONTAL_SCROLL
         		| Manager.NO_VERTICAL_SCROLL
         		| BorderedFieldManager.BOTTOM_BORDER_NONE);
         subjectFieldManager = new BorderedFieldManager(
         		Manager.NO_HORIZONTAL_SCROLL
         		| Manager.NO_VERTICAL_SCROLL
         		| BorderedFieldManager.BOTTOM_BORDER_LINE);
         messageFieldManager = new VerticalFieldManager();
         
         if(isSentFolder) {
             if(envelope.to != null && envelope.to.length > 0) {
             	addressFieldManager.add(new RichTextField(resources.getString(LogicMailResource.MESSAGEPROPERTIES_TO) + " " + envelope.to[0]));
                 if(envelope.to.length > 1) {
                     for(int i=1;i<envelope.to.length;i++) {
                         if(envelope.to[i] != null) {
                         	addressFieldManager.add(new RichTextField("    " + envelope.to[i]));
                         }
                     }
                 }
             }
         }
         else {
             if(envelope.from != null && envelope.from.length > 0) {
             	addressFieldManager.add(new RichTextField(resources.getString(LogicMailResource.MESSAGEPROPERTIES_FROM) + " " + envelope.from[0]));
                 if(envelope.from.length > 1) {
                     for(int i=1;i<envelope.from.length;i++) {
                         if(envelope.from[i] != null) {
                         	addressFieldManager.add(new RichTextField("      " + envelope.from[i]));
                         }
                     }
                 }
             }
         }
         if(envelope.subject != null) {
             subjectFieldManager.add(new RichTextField(resources.getString(LogicMailResource.MESSAGEPROPERTIES_SUBJECT) + " " + envelope.subject));
         }
 
         add(addressFieldManager);
         add(subjectFieldManager);
         add(messageFieldManager);
     }
     
     private MessageNodeListener messageNodeListener = new MessageNodeListener() {
 		public void messageStatusChanged(MessageNodeEvent e) {
 			messageNode_MessageStatusChanged(e);
 		}
     };
     
     protected void onDisplay() {
     	super.onDisplay();
     	messageNode.addMessageNodeListener(messageNodeListener);
     	if(messageNode.getMessage() == null) {
     		throbberField = new ThrobberField(this.getWidth() / 4, Field.FIELD_HCENTER);
     		add(throbberField);
     		messageNode.refreshMessage();
     	}
     	else if(!messageRendered) {
     		renderMessage();
     	}
     }
 
     protected void onUndisplay() {
     	messageNode.removeMessageNodeListener(messageNodeListener);
         synchronized(Application.getEventLock()) {
     		if(throbberField != null) {
     			this.delete(throbberField);
     			throbberField = null;
     		}
         }
     	super.onUndisplay();
     }
     
     private MenuItem propsItem = new MenuItem(resources.getString(LogicMailResource.MENUITEM_PROPERTIES), 100, 10) {
         public void run() {
         	MessagePropertiesDialog dialog = new MessagePropertiesDialog(messageNode);
         	dialog.doModal();
         }
     };
     private MenuItem replyItem = new MenuItem(resources.getString(LogicMailResource.MENUITEM_REPLY), 110, 10) {
         public void run() {
             if(messageNode.getMessage() != null) {
                 CompositionScreen screen =
                     new CompositionScreen(
                     		messageNode.getParent().getParentAccount(),
                     		messageNode,
                     		CompositionScreen.COMPOSE_REPLY);
                 UiApplication.getUiApplication().pushModalScreen(screen);
             }
         }
     };
     private MenuItem replyAllItem = new MenuItem(resources.getString(LogicMailResource.MENUITEM_REPLYTOALL), 115, 10) {
         public void run() {
             if(messageNode.getMessage() != null) {
                 CompositionScreen screen =
                     new CompositionScreen(
                     		messageNode.getParent().getParentAccount(),
                     		messageNode,
                     		CompositionScreen.COMPOSE_REPLY_ALL);
                 UiApplication.getUiApplication().pushModalScreen(screen);
             }
         }
     };
     private MenuItem forwardItem = new MenuItem(resources.getString(LogicMailResource.MENUITEM_FORWARD), 120, 10) {
         public void run() {
             if(messageNode.getMessage() != null) {
                 CompositionScreen screen =
                     new CompositionScreen(
                     		messageNode.getParent().getParentAccount(),
                     		messageNode,
                     		CompositionScreen.COMPOSE_FORWARD);
                 UiApplication.getUiApplication().pushModalScreen(screen);
             }
         }
     };
     private MenuItem copyToItem = new MenuItem(resources.getString(LogicMailResource.MENUITEM_COPY_TO), 125, 10) {
         public void run() {
             if(messageNode.getMessage() != null) {
             	AccountNode[] accountNodes = MailManager.getInstance().getMailRootNode().getAccounts();
             	MailboxSelectionDialog dialog = new MailboxSelectionDialog(
             			resources.getString(LogicMailResource.MESSAGE_SELECT_FOLDER_COPY_TO),
             			accountNodes);
             	dialog.setSelectedMailboxNode(messageNode.getParent());
             	dialog.addUnselectableNode(messageNode.getParent());
             	dialog.doModal();
             	
             	MailboxNode selectedMailbox = dialog.getSelectedMailboxNode();
             	if(selectedMailbox != null && selectedMailbox != messageNode.getParent()) {
             		selectedMailbox.appendMessage(messageNode);
             	}
             }
         }
     };
     private MenuItem moveToItem = new MenuItem(resources.getString(LogicMailResource.MENUITEM_MOVE_TO), 130, 10) {
         public void run() {
             if(messageNode.getMessage() != null) {
             	AccountNode[] accountNodes = MailManager.getInstance().getMailRootNode().getAccounts();
             	MailboxSelectionDialog dialog = new MailboxSelectionDialog(
             			resources.getString(LogicMailResource.MESSAGE_SELECT_FOLDER_MOVE_TO),
             			accountNodes);
             	dialog.setSelectedMailboxNode(messageNode.getParent());
             	dialog.addUnselectableNode(messageNode.getParent());
             	dialog.doModal();
             	
             	MailboxNode selectedMailbox = dialog.getSelectedMailboxNode();
             	if(selectedMailbox != null && selectedMailbox != messageNode.getParent()) {
             		selectedMailbox.appendMessage(messageNode);
             		//TODO: Move To Folder should delete after append
             		//This should only be executed after the append was successful
             		//messageNode.deleteMessage();
             	}
             }
         }
     };
     private MenuItem compositionItem = new MenuItem(resources.getString(LogicMailResource.MENUITEM_COMPOSE_EMAIL), 150, 10) {
         public void run() {
             UiApplication.getUiApplication().pushScreen(new CompositionScreen(messageNode.getParent().getParentAccount()));
         }
     };
     private MenuItem closeItem = new MenuItem(resources.getString(LogicMailResource.MENUITEM_CLOSE), 200000, 10) {
         public void run() {
             onClose();
         }
     };
     protected void makeMenu(Menu menu, int instance) {
         menu.add(propsItem);
         menu.addSeparator();
         if(accountConfig != null && accountConfig.getOutgoingConfig() != null) {
             menu.add(replyItem);
             if(accountConfig.getIdentityConfig() != null) {
                 menu.add(replyAllItem);
             }
             menu.add(forwardItem);
             menu.add(compositionItem);
         }
         menu.add(copyToItem);
         menu.add(moveToItem);
         menu.addSeparator();
         menu.add(closeItem);
     }
 
     public boolean keyChar(char key,
                            int status,
                            int time)
     {
         boolean retval = false;
         switch(key) {
             case Keypad.KEY_ENTER:
             case Keypad.KEY_SPACE:
                 if(status == 0) {
                     scroll(Manager.DOWNWARD);
                     retval = true;
                 }
                 else if(status == KeypadListener.STATUS_ALT) {
                     scroll(Manager.UPWARD);
                     retval = true;
                 }
                 break;
         }
         return retval;
     }
     
     private void messageNode_MessageStatusChanged(MessageNodeEvent e) {
     	if(e.getType() == MessageNodeEvent.TYPE_LOADED) {
             synchronized(Application.getEventLock()) {
 	    		if(throbberField != null) {
 	    			this.delete(throbberField);
 	    			throbberField = null;
 	    		}
             }
     		renderMessage();
     	}
     }
 
     private void renderMessage() {
     	Message message = messageNode.getMessage();
     	if(message != null) {
 			// Prepare the UI elements
 			Vector messageFields;
 			if(message.getBody() != null) {
 				MessageRenderer messageRenderer = new MessageRenderer();
 				message.getBody().accept(messageRenderer);
 				messageFields = messageRenderer.getMessageFields();
 			}
 			else {
 				messageFields = new Vector();
 				messageFields.addElement(
 						new RichTextField(resources.getString(LogicMailResource.MESSAGE_NOTDISPLAYABLE)));
 			}
 			drawMessageFields(messageFields);
 			messageRendered = true;
     	}
     }
     
     private void drawMessageFields(Vector messageFields) {
         if(messageFields == null) {
             return;
         }
         synchronized(Application.getEventLock()) {
             int size = messageFields.size();
             for(int i=0;i<size;++i) {
                 if(messageFields.elementAt(i) != null) {
                     messageFieldManager.add((Field)messageFields.elementAt(i));
                 }
                 if(i != size-1) {
                	messageFieldManager.add(new SeparatorField(Field.FOCUSABLE));
                 }
             }
             messageFieldManager.add(new NullField(Field.FOCUSABLE));
         }
     }
 }
