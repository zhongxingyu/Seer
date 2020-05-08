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
 
 import java.util.Hashtable;
 
 import org.logicprobe.LogicMail.conf.MailSettings;
 import org.logicprobe.LogicMail.conf.OutgoingConfig;
 import org.logicprobe.LogicMail.mail.AbstractMailSender;
 import org.logicprobe.LogicMail.mail.MailFactory;
 import org.logicprobe.LogicMail.mail.MessageToken;
 import org.logicprobe.LogicMail.mail.NetworkMailSender;
 import org.logicprobe.LogicMail.message.FolderMessage;
 import org.logicprobe.LogicMail.message.MimeMessageContent;
 import org.logicprobe.LogicMail.message.MimeMessagePart;
 import org.logicprobe.LogicMail.message.PersistableFolderMessage;
 import org.logicprobe.LogicMail.util.SerializationUtils;
 
 /**
  * Provides a MessageNode object with the necessary data to support
  * outgoing messages.  This keeps all the information involved
  * in sending a message in one place, and provides the ability
  * to serialize unsent messages.
  */
 public class OutgoingMessageNode extends MessageNode {
     private FolderMessage folderMessage;
 	private AccountNode sendingAccount;
 	private AbstractMailSender mailSender;
 	private AccountNode replyToAccount;
 	private MessageToken replyToToken;
 	private boolean sendAttempted;
 	private boolean sending;
     private Hashtable errorSet;
     private int replyType;
     
     public static final int REPLY_ANSWERED = 0;
     public static final int REPLY_FORWARDED = 1;
     
 	/**
 	 * Creates a new instance of OutgoingMessageNode.
 	 * 
 	 * @param The FolderMessage being represented.
      * @param sendingAccount The account the message is being sent from.
      * @param mailSender The mail sender to be used for sending the message.
      * @param replyToMessageNode The message this is a reply to, if applicable.
      * @param replyType Whether this message is answering or forwarding the
      *                  <code>replyToMessageNode</code>.
      */
     OutgoingMessageNode(
     		FolderMessage folderMessage,
     		AccountNode sendingAccount,
     		AbstractMailSender mailSender,
     		MessageNode replyToMessageNode,
     		int replyType) {
     	super(folderMessage);
     	this.folderMessage = folderMessage;
     	this.sendingAccount = sendingAccount;
     	this.mailSender = mailSender;
     	if(replyToMessageNode != null) {
         	this.replyToAccount = replyToMessageNode.getParent().getParentAccount();
         	this.replyToToken = replyToMessageNode.getMessageToken();
         	this.replyType = replyType;
     	}
     }
 
 	/**
 	 * Creates a new instance of OutgoingMessageNode.
 	 * 
 	 * @param The FolderMessage being represented.
      * @param sendingAccount The account the message is being sent from.
      * @param mailSender The mail sender to be used for sending the message.
      */
     OutgoingMessageNode(
     		FolderMessage folderMessage,
     		AccountNode sendingAccount,
     		AbstractMailSender mailSender) {
     	this(folderMessage, sendingAccount, mailSender, null, -1);
     }
     
     /**
      * Creates a new instance of OutgoingMessageNode, setting the contents from
      * a persistable container.
      *
      * @param persistable the persistable container
      */
     public OutgoingMessageNode(PersistableOutgoingMessageNode persistable) {
         super(getFolderMessageFromPersistable(persistable));
         setFromPersistable(persistable);
     }
 
     public boolean refreshMessage() {
         // Refresh is a non-applicable operation for outgoing messages,
         // since they should be completely loaded when the outbox
         // is loaded.
         return false;
     }
     
     public void deleteMessage() {
         if(this.getParent() instanceof OutboxMailboxNode) {
             OutboxMailboxNode parentMailbox = (OutboxMailboxNode)this.getParent();
             parentMailbox.removePersistedMessage(this);
         }
     }
     
     void setMessageStructure(MimeMessagePart messageStructure) {
         this.folderMessage.setStructure(messageStructure);
         super.setMessageStructure(messageStructure);
     }
     
     /**
      * Set the account that this message was sent from.
      * 
      * @param sendingAccount sending account
      */
     void setSendingAccount(AccountNode sendingAccount) {
         this.sendingAccount = sendingAccount;
     }
     
     /**
      * Get the account that this message was sent from.
      * 
      * @return sending account
      */
     public AccountNode getSendingAccount() {
 		return sendingAccount;
 	}
 
     /**
      * Set the mail sender that should be used to send this message.
      * 
      * @param mailSender mail sender
      */
 	public void setMailSender(AbstractMailSender mailSender) {
 		this.mailSender = mailSender;
 	}
 
 	/**
 	 * Get the mail sender that should be used to send this message.
 	 * 
 	 * @return mail sender
 	 */
 	public AbstractMailSender getMailSender() {
 		return mailSender;
 	}
 
 	/**
 	 * Sets the Reply-To message account.
 	 * 
 	 * @param replyToAccount the new Reply-To message account
 	 */
 	void setReplyToAccount(AccountNode replyToAccount) {
 	    this.replyToAccount = replyToAccount;
 	}
 	
 	/**
 	 * Gets the Reply-To message account.
 	 * 
 	 * @return the Reply-To message account
 	 */
 	public AccountNode getReplyToAccount() {
 	    return replyToAccount;
 	}
 	
     /**
      * Sets the Reply-To message token.
      * 
      * @param replyToToken the new Reply-To message token
      */
     void setReplyToToken(MessageToken replyToToken) {
         this.replyToToken = replyToToken;
     }
     
 	/**
 	 * Gets the Reply-To message token.
 	 * 
 	 * @return the Reply-To message token
 	 */
 	public MessageToken getReplyToToken() {
 	    return replyToToken;
 	}
 	
 	/**
 	 * Gets the reply type, either <code>REPLY_ANSWERED</code> or
 	 * <code>REPLY_FORWARDED</code>.
 	 *
 	 * @return the reply type, if applicable
 	 */
 	public int getReplyType() {
         return replyType;
     }
 	
 	/**
 	 * Set whether the sending of this message has been attempted.
 	 * 
 	 * @param sendAttempted true if the sending of this message has been attempted
 	 */
 	void setSendAttempted(boolean sendAttempted) {
 	    this.sendAttempted = sendAttempted;
 	}
 	
 	/**
 	 * Checks if the sending of this message has been attempted.
 	 * <p>
 	 * This flag should be set in all cases except for when the message is
 	 * first created by the composition screen.  It should be used to track
 	 * whether to automatically attempt sending the message, or to only send
 	 * in response to deliberate user action.
 	 * </p>
 	 * 
 	 * @return true, if is send was attempted
 	 */
 	public boolean isSendAttempted() {
         return sendAttempted;
     }
 
 	/**
 	 * Sets whether this message is currently in the process of being sent.
 	 * 
 	 * @param sending true, if sending is in progress
 	 */
 	void setSending(boolean sending) {
 	    this.sending = sending;
 	}
 	
 	/**
 	 * Checks if this message is currently in the process of being sent.
 	 * 
 	 * @return true, if sending is in progress
 	 */
     public boolean isSending() {
         return sending;
     }
     
 	/**
 	 * Send the message.
 	 * <p>
 	 * Message sending normally happens automatically when a message is added
 	 * to the outbox.  This method is intended to only be called for deliberate
 	 * send attempts as triggered by the user.
 	 * </p>
 	 */
     public void sendMessage() {
         MailboxNode parentMailbox = this.getParent();
         if(parentMailbox instanceof OutboxMailboxNode) {
             OutboxMailboxNode outbox = (OutboxMailboxNode)parentMailbox;
             outbox.sendMessage(this);
         }
     }
 
     void setToError(int toError) {
         addRecipientError(this.getTo()[toError]);
     }
     
     void setCcError(int ccError) {
         addRecipientError(this.getCc()[ccError]);
     }
     
     void setBccError(int bccError) {
         addRecipientError(this.getBcc()[bccError]);
     }
     
     public boolean hasRecipientError() {
         return errorSet != null;
     }
     
     public boolean hasRecipientError(Address recipient) {
         if(errorSet != null && errorSet.containsKey(recipient)) {
             return true;
         }
         else {
             return false;
         }
     }
     
     private void addRecipientError(Address recipient) {
         if(errorSet == null) {
             errorSet = new Hashtable();
         }
         errorSet.put(recipient, Boolean.TRUE);
     }
     
     /**
      * Gets a persistable container populated with the contents of this object.
      *
      * @return the persistable container
      */
     public PersistableOutgoingMessageNode getPersistable() {
         PersistableOutgoingMessageNode result = new PersistableOutgoingMessageNode();
         
         PersistableFolderMessage persistableFolderMessage = folderMessage.getPersistable();
         // Replace the token in the persistable folder message, since it can
         // be set after creating the message node object.
         MessageToken token = getMessageToken();
         persistableFolderMessage.setElement(PersistableFolderMessage.FIELD_MESSAGETOKEN, token.clone());
         
         result.setElement(PersistableOutgoingMessageNode.FIELD_FOLDERMESSAGE, persistableFolderMessage);
         
         MimeMessageContent[] content = this.getAllMessageContent();
         byte[][] contentArray = new byte[content.length][];
         for(int i=0; i<content.length; i++) {
             contentArray[i] = SerializationUtils.serializeClass(content[i]);
         }
         result.setElement(PersistableOutgoingMessageNode.FIELD_CONTENT_ARRAY, contentArray);
         
         if(sendingAccount instanceof NetworkAccountNode) {
             result.setElement(PersistableOutgoingMessageNode.FIELD_SENDING_ACCOUNT,
                     new Long(((NetworkAccountNode)sendingAccount).getUniqueId()));
         }
         
         if(mailSender instanceof NetworkMailSender) {
             NetworkMailSender networkMailSender = (NetworkMailSender)mailSender;
             if(networkMailSender.getOutgoingConfig() != null) {
                 result.setElement(PersistableOutgoingMessageNode.FIELD_MAIL_SENDER,
                         new Long(networkMailSender.getOutgoingConfig().getUniqueId()));
             }
         }
 
         if(replyToAccount instanceof NetworkAccountNode) {
             result.setElement(PersistableOutgoingMessageNode.FIELD_REPLYTO_ACCOUNT,
                     new Long(((NetworkAccountNode)replyToAccount).getUniqueId()));
         }
         
         if(replyToToken != null) {
             result.setElement(PersistableOutgoingMessageNode.FIELD_REPLYTO_TOKEN, replyToToken.clone());
         }
         result.setElement(PersistableOutgoingMessageNode.FIELD_REPLYTO_TOKEN, new Integer(replyType));
         
         return result;
     }
     
     private static FolderMessage getFolderMessageFromPersistable(PersistableOutgoingMessageNode persistable) {
         Object value = persistable.getElement(PersistableOutgoingMessageNode.FIELD_FOLDERMESSAGE);
         if(value instanceof PersistableFolderMessage) {
             return new FolderMessage((PersistableFolderMessage)value);
         }
         else {
             return new FolderMessage();
         }
     }
     
     private void setFromPersistable(PersistableOutgoingMessageNode persistable) {
         Object value;
         this.folderMessage = getFolderMessageFromPersistable(persistable);
         
         value = persistable.getElement(PersistableOutgoingMessageNode.FIELD_CONTENT_ARRAY);
         if(value instanceof byte[][]) {
             byte[][] contentArray = (byte[][])value;
             for(int i=0; i<contentArray.length; i++) {
                 MimeMessageContent content = (MimeMessageContent)SerializationUtils.deserializeClass(contentArray[i]);
                 if(content != null) {
                     this.putMessageContent(content);
                 }
             }
         }
         
         long sendingAccountId = -1;
         long mailSenderId = -1;
         long replyToAccountId = -1;
         
         value = persistable.getElement(PersistableOutgoingMessageNode.FIELD_SENDING_ACCOUNT);
         if(value instanceof Long) { sendingAccountId = ((Long)value).longValue(); }
 
         value = persistable.getElement(PersistableOutgoingMessageNode.FIELD_MAIL_SENDER);
         if(value instanceof Long) { mailSenderId = ((Long)value).longValue(); }
 
         value = persistable.getElement(PersistableOutgoingMessageNode.FIELD_REPLYTO_ACCOUNT);
         if(value instanceof Long) { replyToAccountId = ((Long)value).longValue(); }
         
         value = persistable.getElement(PersistableOutgoingMessageNode.FIELD_REPLYTO_TOKEN);
         if(value instanceof MessageToken) {
             this.replyToToken = ((MessageToken)value).clone();
         }
         
         value = persistable.getElement(PersistableOutgoingMessageNode.FIELD_REPLY_TYPE);
         if(value instanceof Integer) { this.replyType = ((Integer)value).intValue(); }
         
         // Resolve the accounts
         if(sendingAccountId != -1 || replyToAccountId != -1) {
             NetworkAccountNode[] accounts = MailManager.getInstance().getMailRootNode().getNetworkAccounts();
             for(int i=0; i<accounts.length; i++) {
                 long accountId = accounts[i].getUniqueId();
 
                 if(accountId == sendingAccountId) {
                     this.sendingAccount = accounts[i];
                 }
 
                 if(accountId == replyToAccountId) {
                     this.replyToAccount = accounts[i];
                 }
             }
         }
 
         // Resolve the mail sender
         if(mailSenderId != -1) {
             OutgoingConfig outgoingConfig = MailSettings.getInstance().getOutgoingConfigByUniqueId(mailSenderId);
             if(outgoingConfig != null) {
                 // This will return the existing instance if possible, or create a new one if necessary.
                 // Since this method should only be invoked after the application is fully initialized,
                 // an existing mail sender should always be returned.
                 AbstractMailSender mailSender = MailFactory.createMailSender(outgoingConfig);
                 this.mailSender = mailSender;
             }
         }
     }
     
 }
