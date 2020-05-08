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
 
 package org.logicprobe.LogicMail.mail.imap;
 
 import java.io.IOException;
 import java.util.Enumeration;
 import java.util.Hashtable;
 import java.util.Vector;
 
 import org.logicprobe.LogicMail.conf.AccountConfig;
 import org.logicprobe.LogicMail.conf.ConnectionConfig;
 import org.logicprobe.LogicMail.conf.GlobalConfig;
 import org.logicprobe.LogicMail.conf.ImapConfig;
 import org.logicprobe.LogicMail.conf.MailSettings;
 import org.logicprobe.LogicMail.conf.MailSettingsEvent;
 import org.logicprobe.LogicMail.conf.MailSettingsListener;
 import org.logicprobe.LogicMail.mail.FolderMessageCallback;
 import org.logicprobe.LogicMail.mail.FolderTreeItem;
 import org.logicprobe.LogicMail.mail.IncomingMailClient;
 import org.logicprobe.LogicMail.mail.MailException;
 import org.logicprobe.LogicMail.mail.MailProgressHandler;
 import org.logicprobe.LogicMail.mail.MessageToken;
 import org.logicprobe.LogicMail.mail.imap.ImapProtocol.FetchEnvelopeResponse;
 import org.logicprobe.LogicMail.message.FolderMessage;
 import org.logicprobe.LogicMail.message.Message;
 import org.logicprobe.LogicMail.message.MimeMessageContent;
 import org.logicprobe.LogicMail.message.MimeMessageContentFactory;
 import org.logicprobe.LogicMail.message.MessageFlags;
 import org.logicprobe.LogicMail.message.MimeMessagePart;
 import org.logicprobe.LogicMail.message.MimeMessagePartFactory;
 import org.logicprobe.LogicMail.message.MultiPart;
 import org.logicprobe.LogicMail.message.UnsupportedContentException;
 import org.logicprobe.LogicMail.message.UnsupportedPart;
 import org.logicprobe.LogicMail.util.Connection;
 import org.logicprobe.LogicMail.util.DataStore;
 import org.logicprobe.LogicMail.util.DataStoreFactory;
 
 /**
  * 
  * Implements the IMAP client
  * 
  */
 public class ImapClient implements IncomingMailClient {
     private ImapConfig accountConfig;
     private Connection connection;
     private ImapProtocol imapProtocol;
     private String username;
     private String password;
     private boolean openStarted;
     private boolean configChanged;
 
     /**
      * Table of supported server capabilities
      */
     private Hashtable capabilities;
     
     /**
      * Delimiter between folder names in the hierarchy
      */
     private String folderDelim = "";
 
     /**
      * Personal namespace from the IMAP server
      */
     private ImapProtocol.Namespace nsPersonal;
     
     /**
      * Active mailbox path, so most commands do not need
      * to take a mailbox path parameter.  This makes it easier
      * to provide a common front-end that still works for
      * protocols that do not support a mailbox hierarchy.
      */
     private FolderTreeItem activeMailbox = null;
 
     /**
      * Special INBOX mailbox reference.
      */
     private FolderTreeItem inboxMailbox = null;
     
     /**
      * Seen mailboxes, used to track whether fetching new
      * messages should be based on a limited range, or
      * the UIDNEXT parameter.
      */
     private Hashtable seenMailboxes = new Hashtable();
     
     /**
      * Known mailboxes, used to track protocol-specific
      * information on mailboxes that is not exposed
      * to other classes.
      */
     private Hashtable knownMailboxes = new Hashtable();
 
     private static String strINBOX = "INBOX";
     
     public ImapClient(GlobalConfig globalConfig, ImapConfig accountConfig) {
         this.accountConfig = accountConfig;
         connection = new Connection(
                 accountConfig.getServerName(),
                 accountConfig.getServerPort(),
                 accountConfig.getServerSecurity() == ConnectionConfig.SECURITY_SSL,
                 accountConfig.getDeviceSide());
         imapProtocol = new ImapProtocol(connection);
         username = accountConfig.getServerUser();
         password = accountConfig.getServerPass();
         openStarted = false;
         configChanged = false;
         MailSettings.getInstance().addMailSettingsListener(mailSettingsListener);
     }
 
     private MailSettingsListener mailSettingsListener = new MailSettingsListener() {
 		public void mailSettingsSaved(MailSettingsEvent e) {
 			mailSettings_MailSettingsSaved(e);
 		}
     };
     
     private void mailSettings_MailSettingsSaved(MailSettingsEvent e) {
 		if(MailSettings.getInstance().containsAccountConfig(accountConfig)) {
 			// Refresh authentication information from the configuration
 	        username = accountConfig.getServerUser();
 	        password = accountConfig.getServerPass();
 	        
 	        if(!isConnected()) {
 	        	// Rebuild the connection to include new settings
 	            connection = new Connection(
 	                    accountConfig.getServerName(),
 	                    accountConfig.getServerPort(),
 	                    accountConfig.getServerSecurity() == ConnectionConfig.SECURITY_SSL,
 	                    accountConfig.getDeviceSide());
 	            imapProtocol = new ImapProtocol(connection);
 	        }
 	        else {
 		        // Set a flag to make sure we rebuild the Connection object
 		        // the next time we close the connection.
 		        configChanged = true;
 	        }
 		}
 		else {
 			// We have been deleted, so unregister to make sure we
 			// no longer affect the system and can be garbage collected.
 			MailSettings.getInstance().removeMailSettingsListener(mailSettingsListener);
 		}
     }
     
     /* (non-Javadoc)
      * @see org.logicprobe.LogicMail.mail.MailClient#open()
      */
     public boolean open() throws IOException, MailException {
         try {
             if(!openStarted) {
                 connection.open();
                 activeMailbox = null;
                 
                 // Swallow the initial "* OK" line from the server
                 connection.receive();
 
                 // Find out server capabilities
                 capabilities = imapProtocol.executeCapability();
                 
                 openStarted = true;
             }
             
             // TLS initialization
             int serverSecurity = accountConfig.getServerSecurity();
             if((serverSecurity == ConnectionConfig.SECURITY_TLS_IF_AVAILABLE && capabilities.containsKey("STARTTLS"))
             		|| (serverSecurity == ConnectionConfig.SECURITY_TLS)) {
             	imapProtocol.executeStartTLS();
             	connection.startTLS();
             }
             
             // Authenticate with the server
             if(!imapProtocol.executeLogin(username, password)) {
                 return false;
             }
 
             // Get the namespaces, if supported
             if(capabilities.containsKey("NAMESPACE")) {
                 ImapProtocol.NamespaceResponse nsResponse = imapProtocol.executeNamespace();
 
                 if(nsResponse.personal != null &&
                    nsResponse.personal.length > 0 &&
                    nsResponse.personal[0] != null &&
                    nsResponse.personal[0].delimiter != null &&
                    nsResponse.personal[0].prefix != null) {
                     // We got a valid personal namespace, so proceed
                     nsPersonal = nsResponse.personal[0];
                     folderDelim = nsPersonal.delimiter;
                 }
             }
             // We could not get valid personal namespace information,
             // so the folder delimiter will be acquired differently.
             if(nsPersonal == null) {
                 // Discover folder delimiter
                 Vector resp = imapProtocol.executeList("", "", null);
                 if(resp.size() > 0) {
                     folderDelim = ((ImapProtocol.ListResponse)resp.elementAt(0)).delim;
                 }
             }
             
             openStarted = false;
         } catch (MailException exp) {
             close();
             String msg = exp.getMessage();
             if(msg.startsWith("NO")) {
                 msg = msg.substring(msg.indexOf(' ')+1);
             }
             throw new MailException(msg);
         }
         return true;
     }
     
     /* (non-Javadoc)
      * @see org.logicprobe.LogicMail.mail.MailClient#close()
      */
     public void close() throws IOException, MailException {
         if(connection.isConnected()) {
             // Not closing to avoid expunging deleted messages
             //if(activeMailbox != null && !activeMailbox.equals("")) {
             //    imapProtocol.executeClose();
             //}
             try {
                 imapProtocol.executeLogout();
             } catch (Exception exp) { }
         }
         activeMailbox = null;
         connection.close();
         
         if(configChanged) {
         	// Rebuild the connection to include new settings
             connection = new Connection(
                     accountConfig.getServerName(),
                     accountConfig.getServerPort(),
                     accountConfig.getServerSecurity() == ConnectionConfig.SECURITY_SSL,
                     accountConfig.getDeviceSide());
             imapProtocol = new ImapProtocol(connection);
         	configChanged = false;
         }
     }
 
     /* (non-Javadoc)
      * @see org.logicprobe.LogicMail.mail.MailClient#isConnected()
      */
     public boolean isConnected() {
         return connection.isConnected();
     }
 
     /* (non-Javadoc)
      * @see org.logicprobe.LogicMail.mail.IncomingMailClient#getAcctConfig()
      */
     public AccountConfig getAcctConfig() {
         return accountConfig;
     }
 
     /* (non-Javadoc)
      * @see org.logicprobe.LogicMail.mail.MailClient#getConnectionConfig()
      */
     public ConnectionConfig getConnectionConfig() {
 		return getAcctConfig();
 	}
 
     /* (non-Javadoc)
      * @see org.logicprobe.LogicMail.mail.MailClient#getUsername()
      */
     public String getUsername() {
         return username;
     }
 
     /* (non-Javadoc)
      * @see org.logicprobe.LogicMail.mail.MailClient#setUsername(java.lang.String)
      */
     public void setUsername(String username) {
         this.username = username;
     }
 
     /* (non-Javadoc)
      * @see org.logicprobe.LogicMail.mail.MailClient#getPassword()
      */
     public String getPassword() {
         return password;
     }
 
     /* (non-Javadoc)
      * @see org.logicprobe.LogicMail.mail.MailClient#setPassword(java.lang.String)
      */
     public void setPassword(String password) {
         this.password = password;
     }
 
     /* (non-Javadoc)
      * @see org.logicprobe.LogicMail.mail.IncomingMailClient#hasFolders()
      */
     public boolean hasFolders() {
         return true;
     }
 
     /* (non-Javadoc)
      * @see org.logicprobe.LogicMail.mail.IncomingMailClient#hasUndelete()
      */
     public boolean hasUndelete() {
         return true;
     }
 
     /* (non-Javadoc)
      * @see org.logicprobe.LogicMail.mail.IncomingMailClient#hasAppend()
      */
     public boolean hasAppend() {
     	return true;
     }
     
     /* (non-Javadoc)
      * @see org.logicprobe.LogicMail.mail.IncomingMailClient#hasCopy()
      */
     public boolean hasCopy() {
     	return true;
     }
     
     /* (non-Javadoc)
      * @see org.logicprobe.LogicMail.mail.IncomingMailClient#hasIdle()
      */
     public boolean hasIdle() {
 		return true;
 	}
     
     /* (non-Javadoc)
      * @see org.logicprobe.LogicMail.mail.IncomingMailClient#getFolderTree(org.logicprobe.LogicMail.mail.MailProgressHandler)
      */
     public FolderTreeItem getFolderTree(MailProgressHandler progressHandler) throws IOException, MailException {
         FolderTreeItem rootItem = new FolderTreeItem("", "", folderDelim);
         
         boolean childrenExtension = capabilities.containsKey("CHILDREN");
         
         // Special logic to handle a user-specified folder prefix
         String folderPrefix = accountConfig.getFolderPrefix();
         if(folderPrefix != null && folderPrefix.length() > 0) {
             FolderTreeItem fakeRootItem = new FolderTreeItem("", folderPrefix, folderDelim);
             getFolderTreeImpl(fakeRootItem, 0, childrenExtension, progressHandler);
             
             // Since we have no way to find the inbox with a hard-coded prefix,
             // assume it to be there and then do a STATUS to verify.
             // If the STATUS fails, a MailException will be thrown, which we can
             // safely ignore.  Otherwise, create a folder item and add it.
             try {
                 imapProtocol.executeStatus(new String[] { strINBOX }, null);
                 FolderTreeItem inboxItem = new FolderTreeItem(rootItem, strINBOX, strINBOX, folderDelim, true, true);
                 rootItem.addChild(inboxItem);
             } catch (MailException exp) { }
             
             if(fakeRootItem.hasChildren()) {
                 FolderTreeItem[] children = fakeRootItem.children();
                 for(int i=0; i<children.length; i++) {
                     if(children[i].getName().trim().length() > 0) {
                         rootItem.addChild(children[i]);
                     }
                 }
             }
         }
         else {
             getFolderTreeImpl(rootItem, 0, childrenExtension, progressHandler);
         }
 
         // Find and save the INBOX folder
     	FolderTreeItem inbox = findInboxFolder(rootItem);
     	if(inbox != null) {
         	setInboxFolder(inbox);
     	}
     	
         return rootItem;
     }
 
     private void getFolderTreeImpl(FolderTreeItem baseFolder, int depth, boolean childrenExtension, MailProgressHandler progressHandler) throws IOException, MailException {
         Vector respList;
         if(depth == 0) {
             respList = imapProtocol.executeList(baseFolder.getPath(), "%", progressHandler);
         }
         else {
             respList = imapProtocol.executeList(baseFolder.getPath() + baseFolder.getDelim(), "%", progressHandler);
         }
 
         int size = respList.size();
         for(int i=0;i<size;++i) {
             ImapProtocol.ListResponse resp = (ImapProtocol.ListResponse)respList.elementAt(i);
             FolderTreeItem childItem = getFolderItem(baseFolder, resp.name, resp.canSelect);
             baseFolder.addChild(childItem);
             if(resp.hasChildren || (!resp.noInferiors && !childrenExtension)) {
                 // The folder has children, so lets go and list them
                 if(depth+1 < accountConfig.getMaxFolderDepth()) {
                     getFolderTreeImpl(childItem, depth+1, childrenExtension, progressHandler);
                 }
             }
             else if(depth == 0 &&
                     nsPersonal != null &&
                     (resp.name + nsPersonal.delimiter).equals(nsPersonal.prefix) &&
                     accountConfig.getMaxFolderDepth() > 1) {
                 // The folder claims to have no children, but it is a root
                 // folder that matches the personal namespace prefix, so
                 // look for children anyways.
                 getFolderTreeImpl(childItem, depth+1, childrenExtension, progressHandler);
             }
         }
     }
 
     /* (non-Javadoc)
      * @see org.logicprobe.LogicMail.mail.IncomingMailClient#refreshFolderStatus(org.logicprobe.LogicMail.mail.FolderTreeItem[])
      */
     public void refreshFolderStatus(FolderTreeItem[] folders, MailProgressHandler progressHandler) throws IOException, MailException {
         int i;
     	
         // Construct an array of mailbox paths to match the folder vector
         Vector mboxPaths = new Vector();
         Hashtable mboxMap = new Hashtable();
         for(i=0; i<folders.length; i++) {
             FolderTreeItem item = folders[i];
             if(item.isSelectable()) {
                 mboxPaths.addElement(item.getPath());
                 mboxMap.put(item.getPath(), item);
             }
         }
         String[] mboxPathsArray = new String[mboxPaths.size()];
         mboxPaths.copyInto(mboxPathsArray);
         
         // Execute the STATUS command on the folders
         ImapProtocol.StatusResponse[] response = imapProtocol.executeStatus(mboxPathsArray, progressHandler);
         
         // Iterate through the results and update the FolderTreeItem objects
         for(i=0; i<mboxPathsArray.length; i++) {
             FolderTreeItem item = (FolderTreeItem)mboxMap.get(mboxPathsArray[i]);
             item.setMsgCount(response[i].exists);
             item.setUnseenCount(response[i].unseen);
         }
     }
     
     /**
      * Recursively search a folder tree for the INBOX folder.
      * 
      * @param mailbox Starting folder
      * @return INBOX folder
      */
     private FolderTreeItem findInboxFolder(FolderTreeItem mailbox) {
     	if(mailbox.getName().equals(strINBOX)) {
     		return mailbox;
     	}
     	else if(mailbox.hasChildren()) {
     		FolderTreeItem[] children = mailbox.children();
     		for(int i=0; i<children.length; i++) {
     			FolderTreeItem result = findInboxFolder(children[i]);
     			if(result != null) {
     				return result;
     			}
     		}
     	}
     	return null;
     }
     
     /**
      * Gets the INBOX folder.
      * If no INBOX folder is available, this method will attempt
      * to load a persisted folder item that may have been associated
      * with the account configuration.
      * 
      * @see org.logicprobe.LogicMail.mail.IncomingMailClient#getInboxFolder()
      */
     public FolderTreeItem getInboxFolder() {
     	if(this.inboxMailbox == null) {
         	DataStore dataStore = DataStoreFactory.getConnectionCacheStore();
 	    	Object loadedObject = dataStore.getNamedObject(Long.toString(accountConfig.getUniqueId()) + "_INBOX");
         	if(loadedObject instanceof FolderTreeItem) {
         		this.inboxMailbox = (FolderTreeItem)loadedObject;
         	}
     	}
     	return this.inboxMailbox;
     }
 
     /**
      * Sets the INBOX folder.
      * This method also persists the INBOX folder in association
      * with the account configuration.
      * 
      * @param mailbox the new INBOX folder
      */
     private void setInboxFolder(FolderTreeItem mailbox) {
     	if(this.inboxMailbox != mailbox) {
     		mailbox = new FolderTreeItem(mailbox);
 	    	DataStore dataStore = DataStoreFactory.getConnectionCacheStore();
 	    	dataStore.putNamedObject(Long.toString(accountConfig.getUniqueId()) + "_INBOX", mailbox);
 	    	dataStore.save();
 	    	this.inboxMailbox = mailbox;
     	}
     }
     
     /* (non-Javadoc)
      * @see org.logicprobe.LogicMail.mail.IncomingMailClient#getActiveFolder()
      */
     public FolderTreeItem getActiveFolder() {
         return activeMailbox;
     }
 
     /* (non-Javadoc)
      * @see org.logicprobe.LogicMail.mail.IncomingMailClient#setActiveFolder(org.logicprobe.LogicMail.mail.FolderTreeItem)
      */
     public void setActiveFolder(FolderTreeItem mailbox) throws IOException, MailException {
     	// Shortcut out if a folder change is not necessary
     	if(activeMailbox != null && activeMailbox.getPath().equals(mailbox.getPath())) {
     		return;
     	}
 
     	// change active mailbox
         ImapProtocol.SelectResponse response = imapProtocol.executeSelect(mailbox.getPath());
 
         this.activeMailbox = mailbox;
         activeMailbox.setMsgCount(response.exists);
         knownMailboxes.put(activeMailbox, response);
         
         // ideally, this should parse out the message counts
         // and populate the appropriate fields of the activeMailbox FolderItem
     }
     
     /* (non-Javadoc)
      * @see org.logicprobe.LogicMail.mail.IncomingMailClient#setActiveFolder(org.logicprobe.LogicMail.mail.MessageToken)
      */
     public void setActiveFolder(MessageToken messageToken) throws IOException, MailException {
     	ImapMessageToken imapMessageToken = (ImapMessageToken)messageToken;
     	String folderPath = imapMessageToken.getFolderPath();
     	
     	// Shortcut out if a folder change is not necessary
     	if(activeMailbox != null && activeMailbox.getPath().equals(imapMessageToken.getFolderPath())) {
     		return;
     	}
     	
         // change active mailbox
         ImapProtocol.SelectResponse response = imapProtocol.executeSelect(folderPath);
 
         FolderTreeItem mailbox = null;
         Enumeration e = knownMailboxes.keys();
         while(e.hasMoreElements()) {
         	FolderTreeItem currentMailbox = (FolderTreeItem)e.nextElement();
         	if(currentMailbox.getPath().equals(folderPath)) {
         		mailbox = currentMailbox;
         		break;
         	}
         }
         if(mailbox == null) {
         	int p = folderPath.lastIndexOf(folderDelim.charAt(0));
         	if(p != -1 && p < folderPath.length() - 1) {
         		mailbox = new FolderTreeItem(folderPath.substring(p + 1), folderPath, folderDelim);
         	}
         	else {
         		mailbox = new FolderTreeItem("", folderPath, folderDelim);
         	}
         }
         
         this.activeMailbox = mailbox;
         activeMailbox.setMsgCount(response.exists);
         knownMailboxes.put(activeMailbox, response);
     }
     
     /* (non-Javadoc)
      * @see org.logicprobe.LogicMail.mail.IncomingMailClient#getFolderMessages(int, int, org.logicprobe.LogicMail.mail.FolderMessageCallback, org.logicprobe.LogicMail.mail.MailProgressHandler)
      */
     public void getFolderMessages(int firstIndex, int lastIndex, FolderMessageCallback callback, MailProgressHandler progressHandler) throws IOException, MailException {
     	getFolderMessages(firstIndex, lastIndex, false, callback, progressHandler);
     }
     
     private class ImapFetchEnvelopeCallback implements ImapProtocol.FetchEnvelopeCallback {
         private FolderMessageCallback callback;
         public ImapFetchEnvelopeCallback(FolderMessageCallback callback) {
             this.callback = callback;
         }
         public void responseAvailable(FetchEnvelopeResponse response) {
             if(response != null) {
                 callback.folderMessageUpdate(ImapClient.this.prepareFolderMessagesEnvelope(response));
             }
             else {
                 callback.folderMessageUpdate(null);
             }
         }
     }
     
     private void getFolderMessages(int firstIndex, int lastIndex, boolean flagsOnly, FolderMessageCallback callback, MailProgressHandler progressHandler) throws IOException, MailException {
     	// Sanity check
     	if(activeMailbox == null) {
     		throw new MailException("Mailbox not selected");
     	}
     	
         // Make sure we do not FETCH an empty folder
         if(firstIndex > lastIndex) {
             callback.folderMessageUpdate(null);
             return;
         }
         
         if(flagsOnly) {
             ImapProtocol.FetchFlagsResponse[] flagsResponse =
                 imapProtocol.executeFetchFlags(firstIndex, lastIndex, progressHandler);
             FolderMessage[] result = prepareFolderMessagesFlags(flagsResponse);
             for(int i=0; i<result.length; i++) {
                 callback.folderMessageUpdate(result[i]);
             }
             callback.folderMessageUpdate(null);
         }
         else {
             imapProtocol.executeFetchEnvelope(firstIndex, lastIndex, new ImapFetchEnvelopeCallback(callback), progressHandler);
         }
     }
 
 	/* (non-Javadoc)
 	 * @see org.logicprobe.LogicMail.mail.IncomingMailClient#getFolderMessages(org.logicprobe.LogicMail.mail.MessageToken[], org.logicprobe.LogicMail.mail.FolderMessageCallback, org.logicprobe.LogicMail.mail.MailProgressHandler)
 	 */
 	public void getFolderMessages(MessageToken[] messageTokens, FolderMessageCallback callback, MailProgressHandler progressHandler)
 			throws IOException, MailException {
     	// Sanity check
     	if(activeMailbox == null) {
     		throw new MailException("Mailbox not selected");
     	}
 
     	int[] uids = new int[messageTokens.length];
     	for(int i=0; i<messageTokens.length; i++) {
     		uids[i] = ((ImapMessageToken)messageTokens[i]).getImapMessageUid();
     	}
     	
         imapProtocol.executeFetchEnvelopeUid(uids, new ImapFetchEnvelopeCallback(callback), progressHandler);
 	}
 
     private class ImapNewFetchEnvelopeCallback implements ImapProtocol.FetchEnvelopeCallback {
         private FolderMessageCallback callback;
         private FolderMessage lastFolderMessage;
         public ImapNewFetchEnvelopeCallback(FolderMessageCallback callback) {
             this.callback = callback;
         }
         public void responseAvailable(FetchEnvelopeResponse response) {
             if(response != null) {
                 FolderMessage folderMessage = ImapClient.this.prepareFolderMessagesEnvelope(response);
                 callback.folderMessageUpdate(folderMessage);
                 lastFolderMessage = folderMessage;
             }
             else {
                 if(lastFolderMessage != null) {
                     int uidNext = lastFolderMessage.getUid() + 1;
                     ((ImapProtocol.SelectResponse)knownMailboxes.get(activeMailbox)).uidNext = uidNext;
                 }
                 callback.folderMessageUpdate(null);
             }
         }
     }
     
     /* (non-Javadoc)
      * @see org.logicprobe.LogicMail.mail.IncomingMailClient#getNewFolderMessages(boolean, org.logicprobe.LogicMail.mail.FolderMessageCallback, org.logicprobe.LogicMail.mail.MailProgressHandler)
      */
     public void getNewFolderMessages(boolean flagsOnly, FolderMessageCallback callback, MailProgressHandler progressHandler) throws IOException, MailException {
     	// Sanity check
     	if(activeMailbox == null) {
     		throw new MailException("Mailbox not selected");
     	}
     	
     	if(!seenMailboxes.containsKey(activeMailbox)) {
 	    	int count = MailSettings.getInstance().getGlobalConfig().getRetMsgCount();
 			int msgCount = activeMailbox.getMsgCount();
 	        int firstIndex = Math.max(1, msgCount - count);
 	    	getFolderMessages(firstIndex, activeMailbox.getMsgCount(), flagsOnly, callback, progressHandler);
 	    	seenMailboxes.put(activeMailbox, new Object());
     	}
     	else {
             FolderMessage[] result;
     		int uidNext = ((ImapProtocol.SelectResponse)knownMailboxes.get(activeMailbox)).uidNext;
     		if(flagsOnly) {
 	    		ImapProtocol.FetchFlagsResponse[] flagsResponse =
 	    			imapProtocol.executeFetchFlagsUid(uidNext, progressHandler);
 	    		result = prepareFolderMessagesFlags(flagsResponse);
 	            if(result.length > 0) {
 	                uidNext = result[result.length-1].getUid() + 1;
 	                ((ImapProtocol.SelectResponse)knownMailboxes.get(activeMailbox)).uidNext = uidNext;
 	            }
 	            for(int i=0; i<result.length; i++) {
 	                callback.folderMessageUpdate(result[i]);
 	            }
     		}
     		else {
     		    imapProtocol.executeFetchEnvelopeUid(uidNext, new ImapNewFetchEnvelopeCallback(callback), progressHandler);
     		}
     	}
     }
 
     private FolderMessage prepareFolderMessagesEnvelope(ImapProtocol.FetchEnvelopeResponse response) {
         FolderMessage folderMessage = new FolderMessage(
                 new ImapMessageToken(activeMailbox.getPath(), response.uid),
                 response.envelope,
                 response.index,
                 response.uid);
         folderMessage.setSeen(response.flags.seen);
         folderMessage.setAnswered(response.flags.answered);
         folderMessage.setDeleted(response.flags.deleted);
         folderMessage.setRecent(response.flags.recent);
         folderMessage.setFlagged(response.flags.flagged);
         folderMessage.setDraft(response.flags.draft);
         folderMessage.setJunk(response.flags.junk);
         folderMessage.setStructure(createMessagePartTree(response.structure));
         return folderMessage;
     }
     
     private FolderMessage[] prepareFolderMessagesFlags(ImapProtocol.FetchFlagsResponse[] response) {
         FolderMessage[] folderMessages = new FolderMessage[response.length];
         for(int i=0;i<response.length;i++) {
             folderMessages[i] = new FolderMessage(
             		new ImapMessageToken(activeMailbox.getPath(), response[i].uid),
             		null,
             		response[i].index,
             		response[i].uid);
             folderMessages[i].setSeen(response[i].flags.seen);
             folderMessages[i].setAnswered(response[i].flags.answered);
             folderMessages[i].setDeleted(response[i].flags.deleted);
             folderMessages[i].setRecent(response[i].flags.recent);
             folderMessages[i].setFlagged(response[i].flags.flagged);
             folderMessages[i].setDraft(response[i].flags.draft);
             folderMessages[i].setJunk(response[i].flags.junk);
         }
     	return folderMessages;
     }
     
     /* (non-Javadoc)
      * @see org.logicprobe.LogicMail.mail.IncomingMailClient#getMessage(org.logicprobe.LogicMail.mail.MessageToken, org.logicprobe.LogicMail.mail.MailProgressHandler)
      */
     public Message getMessage(MessageToken messageToken, MailProgressHandler progressHandler) throws IOException, MailException {
     	ImapMessageToken imapMessageToken = (ImapMessageToken)messageToken;
     	if(!imapMessageToken.getFolderPath().equalsIgnoreCase(activeMailbox.getPath())) {
     		throw new MailException("Invalid mailbox for message");
     	}
     	
         ImapParser.MessageSection structure = getMessageStructure(imapMessageToken.getImapMessageUid());
         Hashtable contentMap = new Hashtable();
         MimeMessagePart rootPart =
             getMessagePart(contentMap, imapMessageToken.getImapMessageUid(),
                            structure, accountConfig.getMaxMessageSize(),
                            progressHandler);
         Message msg = new Message(rootPart);
         Enumeration e = contentMap.keys();
         while(e.hasMoreElements()) {
         	MimeMessagePart part = (MimeMessagePart)e.nextElement();
         	msg.putContent(part, (MimeMessageContent)contentMap.get(part));
         }
         return msg;
     }
 
     public MimeMessageContent getMessagePart(MessageToken messageToken, MimeMessagePart mimeMessagePart, MailProgressHandler progressHandler) throws IOException, MailException {
     	ImapMessageToken imapMessageToken = (ImapMessageToken)messageToken;
     	if(!imapMessageToken.getFolderPath().equalsIgnoreCase(activeMailbox.getPath())) {
     		throw new MailException("Invalid mailbox for message");
     	}
 
     	// Get the relevant data from the MessagePart object
     	String partAddress = mimeMessagePart.getTag();
     	String mimeType = mimeMessagePart.getMimeType();
     	String mimeSubtype = mimeMessagePart.getMimeSubtype();
     	
     	// Make sure we can actually get this part
     	if(partAddress == null || mimeType == null || mimeSubtype == null) { return null; }
     	if(mimeType.equalsIgnoreCase("multipart")) { return null; }
     	if(!(messageToken instanceof ImapMessageToken)) { return null; }
 
     	
     	String data = getMessageBody(imapMessageToken.getImapMessageUid(), partAddress, progressHandler);
     	MimeMessageContent content;
     	try {
 			content = MimeMessageContentFactory.createContent(mimeMessagePart, data);
 		} catch (UnsupportedContentException e) {
 			content = null;
 		}
     	return content;
 	}
 
     private MimeMessagePart getMessagePart(
     		Hashtable contentMap,
     		int uid,
             ImapParser.MessageSection structure,
             int maxSize,
             MailProgressHandler progressHandler)
         throws IOException, MailException
     {
         MimeMessagePart part;
         if(MimeMessagePartFactory.isMimeMessagePartSupported(structure.type, structure.subtype)) {
             String data;
             if(structure.type.equalsIgnoreCase("multipart"))
                 data = null;
             else {
                 if(structure.size < maxSize) {
                     data = getMessageBody(uid, structure.address, progressHandler);
                     maxSize -= structure.size;
                 }
                 else {
                     // We hit the size limit, so stop processing
                     return null;
                 }
             }
             part = MimeMessagePartFactory.createMimeMessagePart(
             		structure.type,
             		structure.subtype,
             		structure.name,
             		structure.encoding,
             		structure.charset,
             		structure.disposition,
             		structure.contentId,
             		structure.size,
             		structure.address);
             try {
 				contentMap.put(part, MimeMessageContentFactory.createContent(part, data));
 			} catch (UnsupportedContentException e) {
 				System.err.println("UnsupportedContentException: " + e.getMessage());
 			}
         }
         else if(structure.address.equals("1")) {
         	// If this was the root part, and still could not be loaded,
         	// insert a placeholder to avoid having to return null.
         	part = new UnsupportedPart(structure.type, structure.subtype);
         }
         else {	
             part = null;
         }
 
         if((part instanceof MultiPart)&&(structure.subsections != null)&&(structure.subsections.length > 0)) {
             for(int i=0;i<structure.subsections.length;i++) {
                 MimeMessagePart subPart = getMessagePart(contentMap, uid, structure.subsections[i], maxSize, progressHandler);
                 if(subPart != null) {
                     ((MultiPart)part).addPart(subPart);
                 }
             }
         }
         return part;
     }
 
     private MimeMessagePart createMessagePartTree(ImapParser.MessageSection structure) {
     	if(structure == null) { return null; }
         MimeMessagePart part = MimeMessagePartFactory.createMimeMessagePart(
         		structure.type,
         		structure.subtype,
         		structure.name,
         		structure.encoding,
         		structure.charset,
         		structure.disposition,
         		structure.contentId,
         		structure.size,
         		structure.address);
 
         if((part instanceof MultiPart)&&(structure.subsections != null)&&(structure.subsections.length > 0)) {
         	MultiPart multiPart = (MultiPart)part;
             for(int i=0; i<structure.subsections.length; i++) {
                 MimeMessagePart subPart = createMessagePartTree(structure.subsections[i]);
                 if(subPart != null) {
                 	multiPart.addPart(subPart);
                 }
             }
         }
         return part;
     }
     
     /**
      * Returns the message structure tree independent of message content.
      * This tree is used to build the final message tree.
      */
     private ImapParser.MessageSection getMessageStructure(int uid) throws IOException, MailException {
         if(activeMailbox.equals("")) {
             throw new MailException("Mailbox not selected");
         }
 
         return imapProtocol.executeFetchBodystructure(uid);
     }
 
     /**
      * Create a new folder item, doing the relevant parsing on the path.
      * @param parent Parent folder
      * @param folderPath Folder path string
      * @param canSelect Whether the folder is selectable
      * @return Folder item object
      */
     private FolderTreeItem getFolderItem(FolderTreeItem parent, String folderPath, boolean canSelect) throws IOException, MailException {
         int pos = 0;
         int i = 0;
         while((i = folderPath.indexOf(folderDelim, i)) != -1) {
             if(i != -1) { pos = i+1; i++; }
         }
         String decodedName = ImapParser.parseFolderName(folderPath.substring(pos));
         FolderTreeItem item = new FolderTreeItem(parent, decodedName, folderPath, folderDelim, canSelect, canSelect);
         item.setMsgCount(0);
         return item;
     }
 
     private String getMessageBody(int uid, String address, MailProgressHandler progressHandler) throws IOException, MailException {
         if(activeMailbox.equals("")) {
             throw new MailException("Mailbox not selected");
         }
         return imapProtocol.executeFetchBody(uid, address, progressHandler);
     }
     
     /* (non-Javadoc)
      * @see org.logicprobe.LogicMail.mail.IncomingMailClient#deleteMessage(org.logicprobe.LogicMail.mail.MessageToken, org.logicprobe.LogicMail.message.MessageFlags)
      */
     public void deleteMessage(MessageToken messageToken, MessageFlags messageFlags) throws IOException, MailException {
     	ImapMessageToken imapMessageToken = (ImapMessageToken)messageToken;
     	if(!imapMessageToken.getFolderPath().equalsIgnoreCase(activeMailbox.getPath())) {
     		throw new MailException("Invalid mailbox for message");
     	}
     	
         ImapProtocol.MessageFlags updatedFlags =
             imapProtocol.executeStore(imapMessageToken.getImapMessageUid(), true, new String[] { "\\Deleted" });
         refreshMessageFlags(updatedFlags, messageFlags);
     }
 
 
     /* (non-Javadoc)
      * @see org.logicprobe.LogicMail.mail.IncomingMailClient#undeleteMessage(org.logicprobe.LogicMail.mail.MessageToken, org.logicprobe.LogicMail.message.MessageFlags)
      */
     public void undeleteMessage(MessageToken messageToken, MessageFlags messageFlags) throws IOException, MailException {
     	ImapMessageToken imapMessageToken = (ImapMessageToken)messageToken;
     	if(!imapMessageToken.getFolderPath().equalsIgnoreCase(activeMailbox.getPath())) {
     		throw new MailException("Invalid mailbox for message");
     	}
     	
         ImapProtocol.MessageFlags updatedFlags =
             imapProtocol.executeStore(imapMessageToken.getImapMessageUid(), false, new String[] { "\\Deleted" });
         refreshMessageFlags(updatedFlags, messageFlags);
     }
     
     /**
      * Sets the flags on a message so the server knows it was answered.
      *
      * @throws IOException on I/O errors
      * @throws MailException on protocol errors
      */
     public void messageAnswered(MessageToken messageToken, MessageFlags messageFlags) throws IOException, MailException {
     	ImapMessageToken imapMessageToken = (ImapMessageToken)messageToken;
     	if(!imapMessageToken.getFolderPath().equalsIgnoreCase(activeMailbox.getPath())) {
     		throw new MailException("Invalid mailbox for message");
     	}
     	
         ImapProtocol.MessageFlags updatedFlags =
             imapProtocol.executeStore(imapMessageToken.getImapMessageUid(), true, new String[] { "\\Answered" });
         refreshMessageFlags(updatedFlags, messageFlags);
     }
     
     private static void refreshMessageFlags(ImapProtocol.MessageFlags updatedFlags, MessageFlags messageFlags) {
         if(updatedFlags != null) {
         	messageFlags.setAnswered(updatedFlags.answered);
         	messageFlags.setDeleted(updatedFlags.deleted);
             messageFlags.setDraft(updatedFlags.draft);
            messageFlags.setFlagged(updatedFlags.draft);
             messageFlags.setRecent(updatedFlags.recent);
             messageFlags.setSeen(updatedFlags.seen);
         }
     }
     
     /* (non-Javadoc)
      * @see org.logicprobe.LogicMail.mail.IncomingMailClient#appendMessage(org.logicprobe.LogicMail.mail.FolderTreeItem, java.lang.String, org.logicprobe.LogicMail.message.MessageFlags)
      */
     public void appendMessage(FolderTreeItem folder, String rawMessage, MessageFlags initialFlags) throws IOException, MailException {
         ImapProtocol.MessageFlags flags = new ImapProtocol.MessageFlags();
         flags.seen = initialFlags.isSeen();
         flags.answered = initialFlags.isAnswered();
         flags.flagged = initialFlags.isFlagged();
         flags.deleted = initialFlags.isDeleted();
         flags.draft = initialFlags.isDraft();
         flags.recent = initialFlags.isRecent();
         flags.junk = initialFlags.isJunk();
 
         imapProtocol.executeAppend(folder.getPath(), rawMessage, flags);
     }
 
     /* (non-Javadoc)
      * @see org.logicprobe.LogicMail.mail.IncomingMailClient#copyMessage(org.logicprobe.LogicMail.mail.MessageToken, org.logicprobe.LogicMail.mail.FolderTreeItem)
      */
     public void copyMessage(MessageToken messageToken, FolderTreeItem destinationFolder) throws IOException, MailException {
     	ImapMessageToken imapMessageToken = (ImapMessageToken)messageToken;
     	if(!imapMessageToken.getFolderPath().equalsIgnoreCase(activeMailbox.getPath())) {
     		throw new MailException("Invalid mailbox for message");
     	}
     	
     	imapProtocol.executeCopy(imapMessageToken.getImapMessageUid(), destinationFolder.getPath());
     }
     
 	/* (non-Javadoc)
 	 * @see org.logicprobe.LogicMail.mail.IncomingMailClient#noop()
 	 */
 	public boolean noop() throws IOException, MailException {
 		boolean result = imapProtocol.executeNoop();
 		return result;
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.logicprobe.LogicMail.mail.IncomingMailClient#idleModeBegin()
 	 */
 	public void idleModeBegin() throws IOException, MailException {
 		imapProtocol.executeIdle();
 	}
 
 	/* (non-Javadoc)
 	 * @see org.logicprobe.LogicMail.mail.IncomingMailClient#idleModeEnd()
 	 */
 	public void idleModeEnd() throws IOException, MailException {
 		imapProtocol.executeIdleDone();
 	}
 
 	/* (non-Javadoc)
 	 * @see org.logicprobe.LogicMail.mail.IncomingMailClient#idleModePoll()
 	 */
 	public boolean idleModePoll() throws IOException, MailException {
 		return imapProtocol.executeIdlePoll();
 	}
 }
