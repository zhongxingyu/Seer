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
 
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.util.Date;
 import java.util.Enumeration;
 import java.util.Hashtable;
 import java.util.Vector;
 
 import javax.microedition.io.Connector;
 import javax.microedition.io.file.FileConnection;
 
 import net.rim.device.api.i18n.DateFormat;
 import net.rim.device.api.system.Display;
 import net.rim.device.api.system.EventLogger;
 import net.rim.device.api.system.KeypadListener;
 import net.rim.device.api.ui.Color;
 import net.rim.device.api.ui.Field;
 import net.rim.device.api.ui.FieldChangeListener;
 import net.rim.device.api.ui.Font;
 import net.rim.device.api.ui.Graphics;
 import net.rim.device.api.ui.Keypad;
 import net.rim.device.api.ui.Manager;
 import net.rim.device.api.ui.MenuItem;
 import net.rim.device.api.ui.Screen;
 import net.rim.device.api.ui.UiApplication;
 import net.rim.device.api.ui.component.BasicEditField;
 import net.rim.device.api.ui.component.Dialog;
 import net.rim.device.api.ui.component.LabelField;
 import net.rim.device.api.ui.component.Menu;
 import net.rim.device.api.ui.component.RichTextField;
 import net.rim.device.api.ui.component.SeparatorField;
 import net.rim.device.api.ui.component.NullField;
 import net.rim.device.api.ui.component.Status;
 import net.rim.device.api.ui.container.VerticalFieldManager;
 
 import org.logicprobe.LogicMail.AppInfo;
 import org.logicprobe.LogicMail.LogicMailResource;
 import org.logicprobe.LogicMail.conf.MailSettings;
 import org.logicprobe.LogicMail.message.ContentPart;
 import org.logicprobe.LogicMail.message.MimeMessageContent;
 import org.logicprobe.LogicMail.message.MimeMessagePart;
 import org.logicprobe.LogicMail.message.MimeMessagePartTransformer;
 import org.logicprobe.LogicMail.model.AccountNode;
 import org.logicprobe.LogicMail.model.Address;
 import org.logicprobe.LogicMail.model.MailboxNode;
 import org.logicprobe.LogicMail.model.MessageNode;
 import org.logicprobe.LogicMail.model.MessageNodeEvent;
 import org.logicprobe.LogicMail.model.MessageNodeListener;
 import org.logicprobe.LogicMail.model.NetworkAccountNode;
 import org.logicprobe.LogicMail.model.OutgoingMessageNode;
 import org.logicprobe.LogicMail.util.UnicodeNormalizer;
 
 /**
  * Display an E-Mail message
  */
 public class MessageScreen extends AbstractScreenProvider {
     private VerticalFieldManager screenFieldManager;
     private BorderedFieldManager propertiesFieldManager;
 	private BorderedFieldManager headerFieldManager;
 	private LabelField attachmentsLabelField;
 	private BorderedFieldManager attachmentsFieldManager;
 	private VerticalFieldManager messageFieldManager;
     private MessageActions messageActions;
     
     private MenuItem saveAttachmentItem;
     private MenuItem compositionItem;
 	
     private MessageNode messageNode;
     private AccountNode parentAccount;
     private boolean isSentFolder;
     private boolean isOutgoingWithErrors;
     private boolean messageRendered;
     private Hashtable requestedContentSet = new Hashtable();
     private static DateFormat dateFormat = DateFormat.getInstance(DateFormat.DATETIME_DEFAULT);
     
     private UnicodeNormalizer unicodeNormalizer;
     
     public MessageScreen(MessageNode messageNode)
     {
         this.messageNode = messageNode;
         this.parentAccount = messageNode.getParent().getParentAccount();
         
         if(MailSettings.getInstance().getGlobalConfig().getUnicodeNormalization()) {
             unicodeNormalizer = UnicodeNormalizer.getInstance();
         }
 
         // Determine if this screen is viewing a sent message
         int mailboxType = messageNode.getParent().getType();
         this.isSentFolder = (mailboxType == MailboxNode.TYPE_SENT) || (mailboxType == MailboxNode.TYPE_OUTBOX);
         
         if(messageNode instanceof OutgoingMessageNode) {
             OutgoingMessageNode outgoingNode = (OutgoingMessageNode)messageNode;
             if(!outgoingNode.isSending()
                     && outgoingNode.isSendAttempted()
                     && outgoingNode.hasRecipientError()) {
                 isOutgoingWithErrors = true;
             }
         }
     }
     
     public long getStyle() {
         return Manager.NO_VERTICAL_SCROLL;
     }
     
     public void initFields(Screen screen) {
         super.initFields(screen);
         
         // Create screen elements
         screenFieldManager = new VerticalFieldManager(Manager.VERTICAL_SCROLL | Manager.VERTICAL_SCROLLBAR);
         
         FieldFactory fieldFactory = FieldFactory.getInstance();
         propertiesFieldManager = fieldFactory.getBorderedFieldManager(
                 Manager.NO_HORIZONTAL_SCROLL
                 | Manager.NO_VERTICAL_SCROLL
                 | BorderedFieldManager.BOTTOM_BORDER_NONE);
         headerFieldManager = fieldFactory.getBorderedFieldManager(
                 Manager.NO_HORIZONTAL_SCROLL
                 | Manager.NO_VERTICAL_SCROLL
                 | BorderedFieldManager.BOTTOM_BORDER_LINE);
         messageFieldManager = new VerticalFieldManager();
         messageFieldManager.add(new NullField(Field.FOCUSABLE));
         
         populatePropertiesFields(propertiesFieldManager);
         populateHeaderFields(headerFieldManager);
         
         screenFieldManager.add(propertiesFieldManager);
         screenFieldManager.add(headerFieldManager);
         screenFieldManager.add(messageFieldManager);
         screen.add(screenFieldManager);
         this.messageActions = navigationController.getMessageActions();
         initMenuItems();
     }
     
     private void populatePropertiesFields(Manager fieldManager) {
         MailboxNode mailboxNode = messageNode.getParent();
         if(mailboxNode != null) {
             if(mailboxNode.getType() != MailboxNode.TYPE_OUTBOX) {
                 String accountText = parentAccount.toString();
                 BasicEditField accountField = new BasicEditField(
                         resources.getString(LogicMailResource.MESSAGEPROPERTIES_ACCOUNT) + ' ',
                         accountText, accountText.length(), Field.FOCUSABLE);
                 accountField.setEditable(false);
                 fieldManager.add(accountField);
             }
             String mailboxText = mailboxNode.toString();
             BasicEditField mailboxField = new BasicEditField(
                     resources.getString(LogicMailResource.MESSAGEPROPERTIES_MAILBOX) + ' ',
                     mailboxText, mailboxText.length(), Field.FOCUSABLE);
             mailboxField.setEditable(false);
             fieldManager.add(mailboxField);
             fieldManager.add(new BlankSeparatorField(Font.getDefault().getHeight() / 4));
         }
         if(isSentFolder) {
             populateRecipientFields(fieldManager, LogicMailResource.MESSAGEPROPERTIES_FROM, messageNode.getFrom());
         }
         else {
             populateRecipientFields(fieldManager, LogicMailResource.MESSAGEPROPERTIES_TO, messageNode.getTo());
         }
         populateRecipientFields(fieldManager, LogicMailResource.MESSAGEPROPERTIES_CC, messageNode.getCc());
         populateRecipientFields(fieldManager, LogicMailResource.MESSAGEPROPERTIES_BCC, messageNode.getBcc());
         populateRecipientFields(fieldManager, LogicMailResource.MESSAGEPROPERTIES_REPLYTO, messageNode.getReplyTo());
     }
     
     private void populateHeaderFields(Manager fieldManager) {
         if(isSentFolder) {
             populateRecipientFields(fieldManager, LogicMailResource.MESSAGEPROPERTIES_TO, messageNode.getTo());
         }
         else {
             populateRecipientFields(fieldManager, LogicMailResource.MESSAGEPROPERTIES_FROM, messageNode.getFrom());
         }
         String subject = messageNode.getSubject();
         if(subject != null) {
             RichTextField subjectField = new RichTextField(normalize(subject));
             subjectField.setFont(subjectField.getFont().derive(Font.BOLD));
             fieldManager.add(subjectField);
         }
 
         Date date = messageNode.getDate();
         if(date != null) {
             RichTextField dateField = new RichTextField(dateFormat.formatLocal(date.getTime()));
             Font font = dateField.getFont();
             int height = font.getHeight() - 4;
             if(height > 0) {
                 font = font.derive(font.getStyle(), height);
             }
             dateField.setFont(font);
             fieldManager.add(dateField);
         }
     }
     
     private void populateRecipientFields(Manager fieldManager, int resourceKey, Address[] recipients) {
         if(recipients != null && recipients.length > 0) {
             String prefix = resources.getString(resourceKey) + ' ';
             for(int i=0;i<recipients.length;i++) {
                 if(isOutgoingWithErrors && ((OutgoingMessageNode)messageNode).hasRecipientError(recipients[i])) {
                     String text = normalize(recipients[i].toString());
                     BasicEditField field = new BasicEditField(prefix, text, text.length(), Field.FOCUSABLE) {
                         protected void paint(Graphics graphics) {
                             int originalColor = graphics.getColor();
                             graphics.setColor(Color.RED);
                             super.paint(graphics);
                             graphics.setColor(originalColor);
                         }
                     };
                     field.setEditable(false);
                     fieldManager.add(field);
                 }
                 else {
                     String text = recipients[i].getName();
                     if(text != null) {
                         text = normalize(text);
                     }
                     else {
                         text = recipients[i].getAddress();
                     }
                     int len = text.length();
                     if(len > 0) {
                         BasicEditField field = new BasicEditField(prefix, text, len, Field.FOCUSABLE);
                         field.setEditable(false);
                         fieldManager.add(field);
                     }
                 }
             }
         }
     }
     
     private MessageNodeListener messageNodeListener = new MessageNodeListener() {
 		public void messageStatusChanged(MessageNodeEvent e) {
 			messageNode_MessageStatusChanged(e);
 		}
     };
     
     private FieldChangeListener fieldChangeListener = new FieldChangeListener() {
 		public void fieldChanged(Field field, int context) {
 			message_FieldChanged(field, context);
 		}
     };
     
     /* (non-Javadoc)
      * @see org.logicprobe.LogicMail.ui.BaseScreen#onDisplay()
      */
     public void onDisplay() {
         padAndFocusScreen();
         
     	messageNode.addMessageNodeListener(messageNodeListener);
     	if(!messageNode.hasMessageContent()) {
     		if(!messageNode.refreshMessage()) {
     			renderMessage();
     		}
     	}
     	else if(!messageRendered) {
     		renderMessage();
     	}
     }
 
 	/* (non-Javadoc)
      * @see org.logicprobe.LogicMail.ui.BaseScreen#onUndisplay()
      */
     public void onUndisplay() {
     	messageNode.removeMessageNodeListener(messageNodeListener);
     }
     
     private void initMenuItems() {
 	    saveAttachmentItem = new MenuItem(resources, LogicMailResource.MENUITEM_SAVE_ATTACHMENT, 300050, 1005) {
 	        public void run() {
 	            Field field = attachmentsFieldManager.getFieldWithFocus();
 	            if(field instanceof AttachmentField) {
 	                saveAttachment(((AttachmentField)field).getMessagePart());
 	            }
 	        }
 	    };
 	    compositionItem = new MenuItem(resources, LogicMailResource.MENUITEM_COMPOSE_EMAIL, 400100, 2000) {
 	        public void run() {
 	            navigationController.displayComposition((NetworkAccountNode)parentAccount);
 	        }
 	    };
     }
     
     /* (non-Javadoc)
      * @see org.logicprobe.LogicMail.ui.BaseScreen#makeMenu(net.rim.device.api.ui.component.Menu, int)
      */
     public void makeMenu(Menu menu, int instance) {
    	if(messageFieldManager.getFieldWithFocus() == attachmentsFieldManager) {
     		if(attachmentsFieldManager.getFieldWithFocus() instanceof AttachmentField) {
                 menu.add(saveAttachmentItem);
     		}
     	}
     	
     	messageActions.makeMenu(menu, instance, messageNode, true);
     	
     	if(parentAccount instanceof NetworkAccountNode
     	        && ((NetworkAccountNode)parentAccount).hasMailSender()) {
             menu.add(compositionItem);
         }
     }
 
     /* (non-Javadoc)
      * @see org.logicprobe.LogicMail.ui.AbstractScreenProvider#navigationClick(int, int)
      */
     public boolean navigationClick(int status, int time) {
         if(attachmentsLabelField != null
                 && attachmentsLabelField.isFocus()
                 && attachmentsFieldManager != null
                 && attachmentsFieldManager.getFieldCount() > 0) {
             attachmentsFieldManager.setFocus();
             attachmentsFieldManager.getField(0).setFocus();
             return true;
         }
         else if(messageFieldManager.getFieldWithFocus() == attachmentsFieldManager) {
             saveAttachmentItem.run();
             return true;
         }
         else {
             return false;
         }
     }
     
     /* (non-Javadoc)
      * @see org.logicprobe.LogicMail.ui.AbstractScreenProvider#keyChar(char, int, int)
      */
     public boolean keyChar(
             char key,
             int status,
             int time) {
         boolean retval = false;
         switch(key) {
         case Keypad.KEY_ENTER:
             if(attachmentsLabelField.isFocus()
                     && attachmentsFieldManager != null
                     && attachmentsFieldManager.getFieldCount() > 0) {
                 attachmentsFieldManager.setFocus();
                 attachmentsFieldManager.getField(0).setFocus();
                 retval = true;
             }
             else if(messageFieldManager.getFieldWithFocus() == attachmentsFieldManager) {
                 saveAttachmentItem.run();
                 retval = true;
             }
             break;
         case Keypad.KEY_SPACE:
             if(status == 0) {
                 screen.scroll(Manager.DOWNWARD);
                 retval = true;
             }
             else if(status == KeypadListener.STATUS_ALT) {
                 screen.scroll(Manager.UPWARD);
                 retval = true;
             }
             break;
         }
         return retval;
     }
     
 	private void messageNode_MessageStatusChanged(MessageNodeEvent e) {
     	if(e.getType() == MessageNodeEvent.TYPE_CONTENT_LOADED) {
     		boolean contentSaved = false;
     		synchronized(requestedContentSet) {
 	    		if(requestedContentSet.size() > 0) {
 	    			Enumeration en = requestedContentSet.keys();
 	    			while(en.hasMoreElements()) {
 	    				ContentPart part = (ContentPart)en.nextElement();
 	    				MimeMessageContent content = (MimeMessageContent)messageNode.getMessageContent(part);
 	    				if(content != null) {
 	    					saveAttachmentInBackground(content, (String)requestedContentSet.get(part));
 	    					requestedContentSet.remove(part);
 	    					contentSaved = true;
 	    				}
 	    			}
 	    		}
     		}
     		if(!contentSaved) {
     			renderMessage();
     		}
     	}
     }
 
     private void renderMessage() {
 		Vector messageFields = new Vector();
     	
     	// Add fields to display the message body
     	MimeMessagePart[] displayableParts = MimeMessagePartTransformer.getDisplayableParts(messageNode.getMessageStructure());    	
     	for(int i=0; i<displayableParts.length; i++) {
     		MimeMessageContent content = messageNode.getMessageContent(displayableParts[i]);
     		if(content != null) {
     			Field field = MessageFieldFactory.createMessageField(messageNode, content);
     			messageFields.addElement(field);
     		}
     	}
     	
     	if(messageFields.size() == 0) {
 			messageFields.addElement(
 					new RichTextField(resources.getString(LogicMailResource.MESSAGE_NOTDISPLAYABLE)));
     	}
     	
     	// Add the attachments list at the bottom of the message
         MimeMessagePart[] attachmentParts = messageNode.getAttachmentParts();
         if(attachmentParts != null && attachmentParts.length > 0) {
             attachmentsFieldManager =
                 FieldFactory.getInstance().getBorderedFieldManager(
                         BorderedFieldManager.BOTTOM_BORDER_NORMAL
                         | BorderedFieldManager.OUTER_FILL_NONE);
             for(int i=0; i<attachmentParts.length; i++) {
                 attachmentsFieldManager.add(new AttachmentField(messageNode, (ContentPart)attachmentParts[i]));
             }
             messageFields.addElement(attachmentsFieldManager);
         }
         else {
             attachmentsFieldManager = null;
         }
     	
 		drawMessageFields(messageFields);
 		messageRendered = true;
     }
 
 	private void drawMessageFields(final Vector messageFields) {
         if(messageFields == null) {
             return;
         }
     	invokeLater(new Runnable() { public void run() {
             int size = messageFields.size();
         	messageFieldManager.deleteAll();
         	
             for(int i=0;i<size;++i) {
                 if(messageFields.elementAt(i) != null) {
                     messageFieldManager.add((Field)messageFields.elementAt(i));
                 }
                 if(i != size-1) {
                 	messageFieldManager.add(new SeparatorField());
                 }
             }
             messageFieldManager.add(new NullField(Field.FOCUSABLE));
             
             for(int i=0;i<size;++i) {
             	Field field = (Field)messageFields.elementAt(i);
             	if(field != null) {
             		field.setChangeListener(fieldChangeListener);
             	}
             }
             
             // Add the attachment indicator header
             if(attachmentsLabelField != null) {
                 headerFieldManager.delete(attachmentsLabelField);
                 attachmentsLabelField = null;
             }
             if(attachmentsFieldManager != null) {
                 int attachmentCount = attachmentsFieldManager.getFieldCount();
                 if(attachmentCount == 1) {
                     attachmentsLabelField = new LabelField(
                             "1 "
                             + resources.getString(LogicMailResource.MESSAGE_ATTACHMENT),
                             Field.FOCUSABLE);
                     headerFieldManager.add(attachmentsLabelField);
                 }
                 else if(attachmentCount > 1) {
                     attachmentsLabelField = new LabelField(
                             Integer.toString(attachmentCount) + ' '
                             + resources.getString(LogicMailResource.MESSAGE_ATTACHMENTS),
                             Field.FOCUSABLE);
                     headerFieldManager.add(attachmentsLabelField);
                 }
             }
             
             padAndFocusScreen();
         }});
     }
 
 	private void padAndFocusScreen() {
 	    // Check for the one case where we want the user to see all the message
 	    // properties fields upon opening the message
 	    if(isOutgoingWithErrors) { return; }
 	    
         // Determine how much padding is necessary at the bottom of the
         // field to ensure a correct initial scroll position
         int paddingSize = Display.getHeight()
             - headerFieldManager.getPreferredHeight()
             - messageFieldManager.getContentHeight();
         
         // Add a blank separator for padding, if necessary
         if(paddingSize > 0) {
             messageFieldManager.add(new BlankSeparatorField(paddingSize));
         }
         
         // Set focus and scroll position so the header field is at the
         // top of the screen
         messageFieldManager.setFocus();
         screenFieldManager.setVerticalScroll(headerFieldManager.getContentTop());
 	}
 	
 	/**
 	 * Save the message attachment.
 	 * 
 	 * @param contentPart Attachment part to save
 	 */
     private void saveAttachment(ContentPart contentPart) {
     	MimeMessageContent content = messageNode.getMessageContent(contentPart);
     	FileSaveDialog dialog = new FileSaveDialog(contentPart.getName());
 		if(dialog.doModal() != Dialog.CANCEL) {
 	    	if(content != null) {
 	    		// Content has been downloaded already, so just save it
 	    		saveAttachmentInBackground(content, dialog.getFileUrl());
 	    	}
 	    	else {
 	    		// Add the requested content to the expected set
 	    		synchronized(requestedContentSet) {
 	    			requestedContentSet.put(contentPart, dialog.getFileUrl());
 	    		}
 	    		
 	    		// Download content from server, then save it
 	    		messageNode.requestContentPart(contentPart);
 	    	}
 		}
 	}
 
     private void saveAttachmentInBackground(MimeMessageContent content, String fileUrl) {
         // Start the save thread
         (new SaveAttachmentThread(content, fileUrl)).start();
     }
     
     private static class SaveAttachmentThread extends Thread {
     	private MimeMessageContent content;
     	private String fileUrl;
     	
     	public SaveAttachmentThread(MimeMessageContent content, String fileUrl) {
     		this.content = content;
     		this.fileUrl = fileUrl;
     	}
 
     	public void run() {
     		boolean success = false;
 			byte[] rawData = content.getRawData();
 			if(rawData != null) {
 				try {
 					FileConnection fileConnection = (FileConnection)Connector.open(fileUrl);
 					if(fileConnection.exists()) {
 	                    final boolean[] overwriteExisting = new boolean[1];
 		                UiApplication.getUiApplication().invokeAndWait(new Runnable() {
 		                    public void run() {
 		                        int result = Dialog.ask(
 		                                Dialog.D_YES_NO,
 		                                resources.getString(LogicMailResource.MESSAGE_OVERWRITE_EXISTING_FILE),
 		                                Dialog.NO);
 		                        if(result == Dialog.YES) {
 		                            overwriteExisting[0] = true;
 		                        }
 		                    }
 		                });
 		                if(overwriteExisting[0]) {
 		                    fileConnection.delete();
 		                }
 		                else {
 		                    fileConnection.close();
 		                    fileConnection = null;
 		                }
 					}
 					if(fileConnection != null) {
 				        // Notify the user that an attachment is being saved
 				        UiApplication.getUiApplication().invokeLater(new Runnable() {
 				            public void run() {
 				                Status.show(resources.getString(LogicMailResource.MESSAGE_SAVING_ATTACHMENT));
 				            }
 				        });
     					fileConnection.create();
     					DataOutputStream outputStream = fileConnection.openDataOutputStream();
     					outputStream.write(rawData);
     					outputStream.close();
     					fileConnection.close();
 					}
 					success = true;
 				} catch (IOException e) {
 					EventLogger.logEvent(AppInfo.GUID, ("Unable to save: " + fileUrl).getBytes(), EventLogger.ERROR);
 					success = false;
 				}
 			}
 			else {
 				// No raw data to save
 				success = false;
 			}
 			
 			if(!success) {
 		        UiApplication.getUiApplication().invokeLater(new Runnable() {
 		            public void run() {
 	                    Status.show(resources.getString(LogicMailResource.MESSAGE_UNABLE_TO_SAVE_ATTACHMENT));
 		            }
 		        });
 			}
     	}
     }
 
 	/**
 	 * Invoked by a field when a property changes. 
 	 * @param field The field that changed.
 	 * @param context Information specifying the origin of the change
 	 * 
 	 * @see FieldChangeListener#fieldChanged(Field, int)
 	 */
     private void message_FieldChanged(Field field, int context) {
         if(field instanceof ActiveFieldManager) {
             if((context & ActiveFieldManager.ACTION_SEND_EMAIL) != 0) {
                 String address = ((ActiveFieldManager)field).getSelectedToken();
                 navigationController.displayComposition((NetworkAccountNode)parentAccount, address);
             }
         }
 	}
 
 	/**
      * Run the Unicode normalizer on the provide string,
      * only if normalization is enabled in the configuration.
      * If normalization is disabled, this method returns
      * the input unmodified.
      * 
      * @param input Input string
      * @return Normalized string
      */
     private String normalize(String input) {
         if(unicodeNormalizer == null) {
             return input;
         }
         else {
             return unicodeNormalizer.normalize(input);
         }
     }
 }
