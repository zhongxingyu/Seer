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
 package org.logicprobe.LogicMail.model;
 
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.util.Hashtable;
 
 import org.logicprobe.LogicMail.conf.AccountConfig;
 import org.logicprobe.LogicMail.mail.AbstractMailSender;
 import org.logicprobe.LogicMail.mail.AbstractMailStore;
 import org.logicprobe.LogicMail.mail.FolderTreeItem;
 import org.logicprobe.LogicMail.mail.MailSenderListener;
 import org.logicprobe.LogicMail.mail.MessageSentEvent;
 import org.logicprobe.LogicMail.mail.MessageToken;
 import org.logicprobe.LogicMail.message.Message;
import org.logicprobe.LogicMail.message.MessageContent;
 import org.logicprobe.LogicMail.message.MessageEnvelope;
 import org.logicprobe.LogicMail.message.MessageFlags;
 import org.logicprobe.LogicMail.util.StringParser;
 
 public class OutboxMailboxNode extends MailboxNode {
 	private int lastMessageId = 0;
 	private Hashtable mailSenderTable = new Hashtable();
 	private Hashtable outboundMessageMap = new Hashtable();
 	
     private MailSenderListener mailSenderListener = new MailSenderListener() {
         public void messageSent(MessageSentEvent e) {
             mailSender_MessageSent(e);
         }
     };
     
 	OutboxMailboxNode(FolderTreeItem folderTreeItem) {
 		super(folderTreeItem, false, MailboxNode.TYPE_OUTBOX);
 	}
 
     void addMessage(MessageNode message) {
     	if(message instanceof OutgoingMessageNode) {
     		addOutgoingMessageImpl((OutgoingMessageNode)message);
     	}
     	super.addMessage(message);
     }
     
     void addMessages(MessageNode[] messages) {
     	for(int i=0; i<messages.length; i++) {
         	if(messages[i] instanceof OutgoingMessageNode) {
         		addOutgoingMessageImpl((OutgoingMessageNode)messages[i]);
         	}
     	}
     	super.addMessages(messages);
     }
     
 	/**
      * Gets the unseen message count for this folder.
      * For the outbox, this is a special case.  Instead of returning
      * the number of unseen messages, we return the total number of
      * messages.  This is because the outbox is transitory in nature,
      * and any messages contained within it are ones the user should
      * know about.
      * 
      * @return Unseen message count.
      */
     public int getUnseenMessageCount() {
     	return this.getMessageCount();
     }
     
     protected void fireMailboxStatusChanged(int type, MessageNode[] affectedMessages) {
     	super.fireMailboxStatusChanged(type, affectedMessages);
     	if(type == MailboxNodeEvent.TYPE_NEW_MESSAGES) {
     		for(int i=0; i<affectedMessages.length; i++) {
     			if(affectedMessages[i] instanceof OutgoingMessageNode) {
     				handleNewMessage((OutgoingMessageNode)affectedMessages[i]);
     			}
     		}
     	}
     }
 
     /**
      * For an outgoing message, this method runs before it is added
      * to the mailbox.  It fixes the ID and makes sure we have a
      * listener subscribed to its mail sender.
      * 
      * @param message The outgoing message.
      */
     private void addOutgoingMessageImpl(OutgoingMessageNode message) {
     	message.setId(lastMessageId++);
 		AbstractMailSender mailSender = message.getMailSender();
 		if(!mailSenderTable.containsKey(mailSender)) {
 			mailSender.addMailSenderListener(mailSenderListener);
 			mailSenderTable.put(mailSender, new Integer(1));
 		}
 		else {
 			int count = ((Integer)mailSenderTable.get(mailSender)).intValue();
 			mailSenderTable.put(mailSender, new Integer(count++));
 		}
 		
 		// Create and set a dummy message token
 		OutboxMessageToken messageToken =
 			new OutboxMessageToken(getFolderTreeItem(), message.getId());
 		message.setMessageToken(messageToken);
     }
 
     /**
 	 * For an outgoing message, this method runs after everyone else
 	 * has been notified of the message being added to the mailbox.
 	 * Its purpose is to request that the message be sent using its
 	 * mail sender.
 	 * 
 	 * @param outgoingMessageNode the outgoing message node
 	 */
 	private void handleNewMessage(OutgoingMessageNode outgoingMessageNode) {
 		// Build the envelope object
 		MessageEnvelope envelope = new MessageEnvelope();
 		envelope.date = outgoingMessageNode.getDate();
 		envelope.subject = outgoingMessageNode.getSubject();
 		envelope.from = StringParser.toStringArray(outgoingMessageNode.getFrom());
 		envelope.sender = StringParser.toStringArray(outgoingMessageNode.getSender());
 		envelope.replyTo = StringParser.toStringArray(outgoingMessageNode.getReplyTo());
 		envelope.to = StringParser.toStringArray(outgoingMessageNode.getTo());
 		envelope.cc = StringParser.toStringArray(outgoingMessageNode.getCc());
 		envelope.bcc = StringParser.toStringArray(outgoingMessageNode.getBcc());
 		envelope.inReplyTo = outgoingMessageNode.getInReplyTo();
 		envelope.messageId = outgoingMessageNode.getMessageId();
 		
 		// Create a protocol-compatible message object
 		Message message = new Message(outgoingMessageNode.getMessageStructure());

		// Populate the message content
		MessageContent[] content = outgoingMessageNode.getAllMessageContent();
		for(int i=0; i<content.length; i++) {
			message.putContent(content[i].getMessagePart(), content[i]);
		}
 		
 		// Update the outbound map and request the message to be sent
 		outboundMessageMap.put(message, outgoingMessageNode);
 		outgoingMessageNode.getMailSender().requestSendMessage(envelope, message);
 	}
 	
	private void mailSender_MessageSent(MessageSentEvent e) {
     	// Find out whether we know about this message
     	if(outboundMessageMap.get(e.getMessage()) instanceof OutgoingMessageNode) {
     		OutgoingMessageNode outgoingMessageNode = (OutgoingMessageNode)outboundMessageMap.get(e.getMessage());
     		outboundMessageMap.remove(e.getMessage());
     		
     		// Store to the Sent folder
     		AccountConfig sendingAccountConfig = outgoingMessageNode.getSendingAccount().getAccountConfig();
     		
     		// Append to the Sent message folder, if available
     		MailboxNode sentMailbox = sendingAccountConfig.getSentMailbox();
     		if(sentMailbox != null && sentMailbox.hasAppend()) {
     			MessageFlags initialFlags = new MessageFlags();
     			initialFlags.setSeen(true);
     			sentMailbox.appendRawMessage(e.getMessageSource(), initialFlags);
     		}
 
     		// Update replied-to message flags
     		MessageNode replyToMessageNode = outgoingMessageNode.getReplyToMessageNode();
     		if(replyToMessageNode != null) {
     			AbstractMailStore sendingMailStore = outgoingMessageNode.getSendingAccount().getMailStore();
     			if(sendingMailStore.hasFlags()) {
     				sendingMailStore.requestMessageAnswered(
     						replyToMessageNode.getMessageToken(),
     						MessageNode.createMessageFlags(replyToMessageNode.getFlags()));
     			}
     		}
     		
     		// Remove from this folder
     		removeMessage(outgoingMessageNode);
     	}
     }
     
     /**
      * Special message token for outbox messages.
      * This is necessary because Outbox messages do not
      * normally exist within any real mail store unless
      * they fail to transmit.
      *
      */
     private static class OutboxMessageToken implements MessageToken {
 		private String folderPath;
 		private int messageId;
 		
     	public OutboxMessageToken(FolderTreeItem folderTreeItem, int messageId) {
     		this.folderPath = folderTreeItem.getPath();
     		this.messageId = messageId;
     	}
     	
     	public int getMessageId() {
     		return messageId;
     	}
     	
 		public boolean containedWithin(FolderTreeItem folderTreeItem) {
 			return folderTreeItem.getPath().equals(folderPath);
 		}
 
 		public long getUniqueId() {
 			// Empty because this special token is not intended to be serialized
 			return 0;
 		}
 
 		public void serialize(DataOutputStream output) throws IOException {
 			// Empty because this special token is not intended to be serialized
 		}
     	
 		public void deserialize(DataInputStream input) throws IOException {
 			// Empty because this special token is not intended to be serialized
 		}
     }
 }
