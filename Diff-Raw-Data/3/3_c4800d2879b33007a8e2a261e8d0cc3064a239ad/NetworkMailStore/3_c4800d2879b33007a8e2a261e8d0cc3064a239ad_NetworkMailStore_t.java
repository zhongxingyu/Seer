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
 
 package org.logicprobe.LogicMail.mail;
 
 import net.rim.device.api.system.UnsupportedOperationException;
 
 import org.logicprobe.LogicMail.conf.AccountConfig;
 import org.logicprobe.LogicMail.message.MessageFlags;
 import org.logicprobe.LogicMail.message.MimeMessagePart;
 
 public class NetworkMailStore extends AbstractMailStore {
 	private IncomingMailClient client;
 	private IncomingMailConnectionHandler connectionHandler;
 	private AccountConfig accountConfig;
 	public NetworkMailStore(AccountConfig accountConfig) {
 		super();
 		this.client = MailClientFactory.createMailClient(accountConfig);
 		this.accountConfig = accountConfig;
 		this.connectionHandler = new IncomingMailConnectionHandler(this, client);
 		this.connectionHandler.start();
 	}
 
     /**
 	 * Gets the account configuration associated with this network mail store.
 	 * 
 	 * @return Account configuration.
 	 */
 	public AccountConfig getAccountConfig() {
 		return this.accountConfig;
 	}
 	
 	public void shutdown(boolean wait) {
 		connectionHandler.shutdown(wait);
 	}
 
 	/**
 	 * Restarts the mail connection handler thread.
 	 */
 	public void restart() {
 		if(!connectionHandler.isRunning()) {
 			connectionHandler.start();
 		}
 	}
 	
 	public boolean isLocal() {
 		return false;
 	}
 
 	public boolean hasFolders() {
 		return client.hasFolders();
 	}
 
 	public boolean hasMessageParts() {
 		return client.hasMessageParts();
 	}
 	
 	public boolean hasFlags() {
 		return client.hasFlags();
 	}
 	
 	public boolean hasAppend() {
 		return client.hasAppend();
 	}
 
 	public boolean hasCopy() {
 		return client.hasCopy();
 	}
 	
 	public boolean hasUndelete() {
 		return client.hasUndelete();
 	}
 
 	public boolean hasExpunge() {
 	    return client.hasExpunge();
 	}
 	
 	/**
 	 * Returns whether the mail store supports retrieval of a full folder
 	 * message index-to-UID map.
 	 *
 	 * @return True if index-to-UID map retrieval is supported, false otherwise
 	 * @see #requestFolderMessageIndexMap(FolderTreeItem, MailStoreRequestCallback)
 	 */
 	public boolean hasFolderMessageIndexMap() {
 	    return client.hasFolderMessageIndexMap();
 	}
 	
 	/**
 	 * Returns whether the mail store has a locked folder view while connected.
 	 * If this method returns true, then an explicit refresh during an existing
 	 * connection will not return new data.
 	 * @return True if folder contents are locked while connected, false otherwise.
 	 */
 	public boolean hasLockedFolders() {
 	    return client.hasLockedFolders();
 	}
 	
 	public boolean isConnected() {
 		return client.isConnected();
 	}
 
 	/**
 	 * Gets the inbox folder, if available.
 	 */
 	public FolderTreeItem getInboxFolder() {
 	    return client.getInboxFolder();
 	}
 	
     /**
      * Requests that the mail store disconnect from the mail server.
      * <p>
      * Unlike the <code>shutdown(boolean)</code> method, this does not cause
      * the connection handler thread to terminate.  As such, any subsequent
      * request may cause it to reconnect.
      * </p>
      */
     public void requestDisconnect() {
         processRequest(new NetworkDisconnectRequest(this, NetworkDisconnectRequest.REQUEST_DISCONNECT));
     }
 
     /**
      * Creates a request to instruct the mail client to enable or disable its
      * idle mode.  This request is useful at the beginning and end of a batch
      * operation, to prevent the mail client from entering and exiting idle
      * mode between requests.
      *
      * @param idleEnabled whether or not the idle mode should be enabled
      */
     public NetworkClientIdleModeRequest createClientIdleModeRequest(boolean idleEnabled) {
         NetworkClientIdleModeRequest request = new NetworkClientIdleModeRequest(this, idleEnabled);
         return request;
     }
     
 	public FolderTreeRequest createFolderTreeRequest() {
 	    NetworkFolderTreeRequest request = new NetworkFolderTreeRequest(this);
 		return request;
 	}
 
 	public FolderExpungeRequest createFolderExpungeRequest(FolderTreeItem folder) {
 	    NetworkFolderExpungeRequest request = new NetworkFolderExpungeRequest(this, folder);
         return request;
 	}
 	
 	public FolderStatusRequest createFolderStatusRequest(FolderTreeItem[] folders) {
 	    NetworkFolderStatusRequest request = new NetworkFolderStatusRequest(this, folders);
         return request;
 	}
 
 	public FolderMessagesRequest createFolderMessagesRangeRequest(FolderTreeItem folder, MessageToken firstToken, int increment) {
 	    if(firstToken == null || increment <= 0) {
 	        throw new IllegalArgumentException();
 	    }
 		
 	    NetworkFolderMessagesRequest request = new NetworkFolderMessagesRequest(this, folder, firstToken, increment);
 	    return request;
 	}
 
 	public FolderMessagesRequest createFolderMessagesSetRequest(FolderTreeItem folder, MessageToken[] messageTokens, boolean flagsOnly) {
         NetworkFolderMessagesRequest request = new NetworkFolderMessagesRequest(this, folder, messageTokens, flagsOnly);
         return request;
 	}
 	
 	public FolderMessagesRequest createFolderMessagesSetByIndexRequest(FolderTreeItem folder, int[] messageIndices) {
         NetworkFolderMessagesRequest request = new NetworkFolderMessagesRequest(this, folder, messageIndices);
         return request;
 	}
 	
 	public FolderMessagesRequest createFolderMessagesRecentRequest(FolderTreeItem folder, boolean flagsOnly) {
         NetworkFolderMessagesRequest request = new NetworkFolderMessagesRequest(this, folder, flagsOnly);
         return request;
 	}
 	
 	/**
 	 * Requests the message UID-to-index map for a particular folder.
      * <p>
      * Successful completion is indicated by a call to
      * {@link FolderListener#folderMessageIndexMapAvailable(FolderMessageIndexMapEvent)}.
      * </p>
      * 
      * @param folder The folder to request a message listing for.
 	 */
 	public NetworkFolderMessageIndexMapRequest requestFolderMessageIndexMap(FolderTreeItem folder) {
         NetworkFolderMessageIndexMapRequest request = new NetworkFolderMessageIndexMapRequest(this, folder);
         return request;
 	}
 	
 	public MessageRequest createMessageRequest(MessageToken messageToken, boolean useLimits) {
 		NetworkMessageRequest request = new NetworkMessageRequest(this, messageToken, useLimits);
         return request;
 	}
 
 	public MessageRequest createMessagePartsRequest(MessageToken messageToken, MimeMessagePart[] messageParts) {
 		NetworkMessageRequest request = new NetworkMessageRequest(this, messageToken, messageParts);
         return request;
 	}
 	
 	public MessageFlagChangeRequest createMessageFlagChangeRequest(
 	        MessageToken messageToken,
 	        MessageFlags messageFlags,
 	        boolean addOrRemove) {
 
 	    if(messageFlags.isDeleted()) {
 	        if(!addOrRemove && !client.hasUndelete()) {
 	            throw new UnsupportedOperationException();
 	        }
 	    }
 	    else if(!this.hasFlags()) {
 	        throw new UnsupportedOperationException();
 	    }
 
 	    NetworkMessageFlagChangeRequest request = new NetworkMessageFlagChangeRequest(this, messageToken, messageFlags, addOrRemove);
 	    return request;
 	}
 	
 	public MessageAppendRequest createMessageAppendRequest(FolderTreeItem folder, String rawMessage, MessageFlags initialFlags) {
 		if(!this.hasAppend()) {
 			throw new UnsupportedOperationException();
 		}
 		NetworkMessageAppendRequest request = new NetworkMessageAppendRequest(this, folder, rawMessage, initialFlags);
 		return request;
 	}
 	
 	public MessageCopyRequest createMessageCopyRequest(MessageToken messageToken, FolderTreeItem destinationFolder) {
 	    if(!this.hasCopy()) {
 	        throw new UnsupportedOperationException();
 	    }
 	    NetworkMessageCopyRequest request = new NetworkMessageCopyRequest(this, messageToken, destinationFolder);
 	    return request;
 	}
 	
     public void processRequest(MailStoreRequest request) {
         if(request instanceof NetworkMailStoreRequest
                 && request instanceof ConnectionHandlerRequest) {
             connectionHandler.addRequest((ConnectionHandlerRequest)request);
         }
         else {
             throw new IllegalArgumentException();
         }
     }
     
     IncomingMailConnectionHandler getConnectionHandler() {
         return connectionHandler;
     }
 }
