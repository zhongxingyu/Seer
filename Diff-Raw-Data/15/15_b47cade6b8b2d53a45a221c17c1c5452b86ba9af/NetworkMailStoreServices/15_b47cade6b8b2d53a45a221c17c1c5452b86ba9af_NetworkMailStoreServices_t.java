 /*-
  * Copyright (c) 2010, Derek Konigsberg
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
 
 import java.util.Enumeration;
 import java.util.Hashtable;
 import java.util.Vector;
 
 import net.rim.device.api.util.ToIntHashtable;
 
 import org.logicprobe.LogicMail.conf.AccountConfig;
 import org.logicprobe.LogicMail.conf.ImapConfig;
 import org.logicprobe.LogicMail.mail.FolderTreeItem;
 import org.logicprobe.LogicMail.mail.MailStoreRequestCallback;
 import org.logicprobe.LogicMail.mail.MessageToken;
 import org.logicprobe.LogicMail.mail.NetworkMailStore;
 import org.logicprobe.LogicMail.message.FolderMessage;
 import org.logicprobe.LogicMail.message.MimeMessageContent;
 import org.logicprobe.LogicMail.message.MimeMessagePart;
 import org.logicprobe.LogicMail.message.MimeMessagePartTransformer;
 
 public class NetworkMailStoreServices extends MailStoreServices {
     private final NetworkMailStore mailStore;
     private final FolderMessageCache folderMessageCache;
     private final MessageContentFileManager contentFileManager;
     
     /** Map of FolderTreeItem objects to corresponding request handlers */
     private final Hashtable folderRequestHandlerMap = new Hashtable();
     
     public NetworkMailStoreServices(NetworkMailStore mailStore, FolderMessageCache folderMessageCache) {
         super(mailStore);
         this.mailStore = mailStore;
         this.folderMessageCache = folderMessageCache;
         this.contentFileManager = MessageContentFileManager.getInstance();
     }
 
     AccountConfig getAccountConfig() {
         return mailStore.getAccountConfig();
     }
 
     public void disconnect() {
         mailStore.requestDisconnect();
     }
     
     public void restart() {
         mailStore.restart();
     }
 
     public void setConnected(boolean connected) {
         // Only handle disconnects
         if(connected) { return; }
         
         synchronized(folderRequestHandlerMap) {
             Enumeration e = folderRequestHandlerMap.elements();
             while(e.hasMoreElements()) {
                 FolderRequestHandler handler = (FolderRequestHandler)e.nextElement();
                 handler.handleDisconnect();
             }
         }
     }
     
     public void requestFolderRefresh(final FolderTreeItem folderTreeItem) {
         FolderRequestHandler handler = getFolderRequestHandler(folderTreeItem);
         handler.requestFolderRefresh();
     }
     
     public void requestMoreFolderMessages(FolderTreeItem folderTreeItem, MessageToken firstToken) {
         FolderRequestHandler handler = getFolderRequestHandler(folderTreeItem);
         int increment = mailStore.getAccountConfig().getFolderMessageIncrement();
         handler.requestMoreFolderMessages(firstToken, increment);
     }
     
     public void requestMessageSeen(MessageToken messageToken) {
         FolderRequestHandler handler = getFolderRequestHandler(messageToken);
         handler.setFolderMessageSeen(messageToken);
     }
     
     public void removeSavedData(final FolderTreeItem[] folderTreeItems) {
         for(int i=0; i<folderTreeItems.length; i++) {
             folderMessageCache.removeFolder(folderTreeItems[i]);
         }
         folderMessageCache.commit();
         
         (new Thread() { public void run() {
             for(int i=0; i<folderTreeItems.length; i++) {
                 contentFileManager.removeFolder(folderTreeItems[i]);
             }
         }}).start();
         super.removeSavedData(folderTreeItems);
     }
     
     protected void handleFolderMessagesAvailable(FolderTreeItem folder, FolderMessage[] messages, boolean flagsOnly) {
         FolderRequestHandler handler = getFolderRequestHandler(folder);
         
         if(flagsOnly) {
             handler.handleFolderMessageFlagsAvailable(messages);
         }
         else {
             handler.handleFolderMessagesAvailable(messages);
         }
     }
     
     protected void handleFolderMessageIndexMapAvailable(FolderTreeItem folder, ToIntHashtable uidIndexMap) {
         FolderRequestHandler handler = getFolderRequestHandler(folder);
         
         handler.handleFolderMessageIndexMapAvailable(uidIndexMap);
     }
 
     protected void handleFolderExpunged(FolderTreeItem folder, int[] indices) {
         FolderRequestHandler handler = getFolderRequestHandler(folder);
         
         handler.handleFolderExpunged(indices);
     }
 
     /**
      * Gets the folder request handler for the provided folder object.
      * If a handler does not exist, then one is created.
      *
      * @param folder the folder to get a request handler for
      * @return the folder request handler
      */
     private FolderRequestHandler getFolderRequestHandler(FolderTreeItem folder) {
         FolderRequestHandler handler;
         synchronized(folderRequestHandlerMap) {
             handler = (FolderRequestHandler)folderRequestHandlerMap.get(folder);
             if(handler == null) {
                 handler = new FolderRequestHandler(this, mailStore, folderMessageCache, folder);
                 folderRequestHandlerMap.put(folder, handler);
             }
         }
         return handler;
     }
     
     /**
      * Gets the folder request handler for the folder that contains the provided
      * message token.  If no such folder handler exists, then <code>null</code>
      * is returned.
      *
      * @param messageToken the message token to find a folder handler for
      * @return the folder request handler
      */
     private FolderRequestHandler getFolderRequestHandler(MessageToken messageToken) {
         FolderRequestHandler handler = null;
         synchronized(folderRequestHandlerMap) {
             Enumeration en = folderRequestHandlerMap.keys();
             while(en.hasMoreElements()) {
                 FolderTreeItem folder = (FolderTreeItem)en.nextElement();
                 if(messageToken.containedWithin(folder)) {
                     handler = (FolderRequestHandler)folderRequestHandlerMap.get(folder);
                     break;
                 }
             }
         }
         return handler;
     }
 
     public boolean hasCachedMessageContent(FolderTreeItem folder, MessageToken messageToken) {
         return contentFileManager.messageContentExists(folder, messageToken);
     }
     
     public boolean requestMessageRefresh(final MessageToken messageToken) {
         FolderRequestHandler handler = getFolderRequestHandler(messageToken);
         if(handler == null) { return false; }
         
         // Get the message folder and structure from the folder request handler
         final boolean cacheOnly = !handler.isInitialRefreshComplete();
         final MimeMessagePart structure = handler.getCachedMessageStructure(messageToken);
         final FolderTreeItem folder = handler.getFolder();
 
         // This condition should not be possible, if the initial refresh
         // completed successfully, but we'll check anyways just to be safe.
         if(mailStore.hasMessageParts() && structure == null) { return false; }
         
         // Start a thread for the remaining logic, which includes file I/O
         (new Thread() { public void run() {
             if(mailStore.hasMessageParts()) {
                 requestMessageRefreshParts(folder, messageToken, structure, cacheOnly);
             }
             else {
                 requestMessageRefreshWhole(folder, messageToken, structure, cacheOnly);
             }
         }}).start();
         return true;
     }
     
     private void requestMessageRefreshParts(
             final FolderTreeItem folder,
             final MessageToken messageToken,
             final MimeMessagePart structure,
             final boolean cacheOnly) {
         
         // Determine which parts are displayable
         MimeMessagePart[] displayableParts = MimeMessagePartTransformer.getDisplayableParts(structure);
         
         // Load displayable parts from cache
         Hashtable loadedPartSet = loadMessagePartsFromCache(folder, messageToken, displayableParts);
    
         // If there are parts we couldn't load from the cache, then see if
         // we should load them.
         if(loadedPartSet.size() < displayableParts.length && !cacheOnly) {
             // Determine the max size to fetch
             int maxSize = getMaxSizeToFetch();
             
             // Filter the displayable parts list based on the size
             Vector partsToFetch = new Vector();
             int sizeTotal = 0;
             for(int i=0; i<displayableParts.length; i++) {
                 sizeTotal += displayableParts[i].getSize();
                 if(sizeTotal <= maxSize) {
                     if(!loadedPartSet.containsKey(displayableParts[i])) {
                         partsToFetch.addElement(displayableParts[i]);
                     }
                 }
                 else {
                     break;
                 }
             }
             
             // Request the remaining parts from the server
             if(partsToFetch.size() > 0) {
                 MimeMessagePart[] partsArray = new MimeMessagePart[partsToFetch.size()];
                 partsToFetch.copyInto(partsArray);
                 mailStore.requestMessageParts(messageToken, partsArray, new MailStoreRequestCallback() {
                     public void mailStoreRequestComplete() { }
                     public void mailStoreRequestFailed(Throwable exception) {
                         messageRefreshFailed(messageToken);
                     }
                 });
             }
             else {
                 messageRefreshComplete(messageToken);
             }
         }
         else {
             // If everything was loaded from the cache, notify that the load
             // operation is complete.
             messageRefreshComplete(messageToken);
         }
     }
     
     private Hashtable loadMessagePartsFromCache(final FolderTreeItem folder, final MessageToken messageToken, MimeMessagePart[] partsToLoad) {
         Hashtable loadedPartSet = new Hashtable(partsToLoad.length);
    
         if(contentFileManager.messageContentExists(folder, messageToken)) {
             MimeMessageContent[] loadedContent =
                 contentFileManager.getMessageContent(folder, messageToken, partsToLoad);
             
             for(int i=0; i<loadedContent.length; i++) {
                 MimeMessagePart loadedPart = loadedContent[i].getMessagePart();
                 loadedPartSet.put(loadedPart, Boolean.TRUE);
             }
             
             // Notify listeners that content was loaded
             if(loadedContent.length > 0) {
                 fireMessageContentAvailable(messageToken, loadedContent);
             }
         }
         return loadedPartSet;
     }
     
     private int getMaxSizeToFetch() {
         int maxSize;
         AccountConfig accountConfig = mailStore.getAccountConfig();
         if(accountConfig instanceof ImapConfig) {
             maxSize = ((ImapConfig)accountConfig).getMaxMessageSize();
         }
         else {
             maxSize = Integer.MAX_VALUE;
         }
         return maxSize;
     }
 
     private void requestMessageRefreshWhole(
             final FolderTreeItem folder,
             final MessageToken messageToken,
             final MimeMessagePart structure,
             final boolean cacheOnly) {
         
         //TODO: Implement more robust POP behavior
         if(structure != null && contentFileManager.messageContentExists(folder, messageToken)) {
             // If the store does not support parts, then take a simplified
             // approach that assumes the cache can only contain complete
             // data for a message.
             
             // Determine which parts are displayable
             MimeMessagePart[] displayableParts = MimeMessagePartTransformer.getDisplayableParts(structure);
             MimeMessageContent[] loadedContent =
                 contentFileManager.getMessageContent(folder, messageToken, displayableParts);
             
             // Notify listeners that content was loaded
             if(loadedContent.length > 0) {
                 fireMessageContentAvailable(messageToken, loadedContent);
                 messageRefreshComplete(messageToken);
             }
         }
         else if(!cacheOnly) {
             mailStore.requestMessage(messageToken, new MailStoreRequestCallback() {
                 public void mailStoreRequestComplete() { }
                 public void mailStoreRequestFailed(Throwable exception) {
                     messageRefreshFailed(messageToken);
                 }
             });
         }
         else {
             messageRefreshComplete(messageToken);
         }
     }
 
     private void messageRefreshComplete(MessageToken messageToken) {
         fireMessageContentAvailable(messageToken, null);
         requestMessageSeen(messageToken);
     }
     
     private void messageRefreshFailed(MessageToken messageToken) {
         requestMessageSeen(messageToken);
     }
     
     public void requestMessageParts(final MessageToken messageToken, final MimeMessagePart[] messageParts) {
         FolderRequestHandler handler = getFolderRequestHandler(messageToken);
         final FolderTreeItem folder = handler.getFolder();
 
         // Start a thread for the remaining logic, which includes file I/O
         (new Thread() { public void run() {
             // Load any parts that may be in the cache
             Hashtable loadedPartSet = loadMessagePartsFromCache(folder, messageToken, messageParts);
             
             // Build a list of any non-cached parts that need to be fetched
             Vector partsToFetch = new Vector();
             for(int i=0; i<messageParts.length; i++) {
                 if(!loadedPartSet.containsKey(messageParts[i])) {
                     partsToFetch.addElement(messageParts[i]);
                 }
             }
             
             // Request the remaining parts from the server
             if(partsToFetch.size() > 0) {
                 MimeMessagePart[] partsArray = new MimeMessagePart[partsToFetch.size()];
                 partsToFetch.copyInto(partsArray);
                 mailStore.requestMessageParts(messageToken, partsArray);
             }
         }}).start();
     }
     
     protected void handleMessageAvailable(
             MessageToken messageToken,
             MimeMessagePart messageStructure,
             MimeMessageContent[] messageContent,
             String messageSource) {
         
         // Update the status and structure in the folder cache for this message
         FolderRequestHandler handler = getFolderRequestHandler(messageToken);
         handler.handleMessageAvailable(messageToken, messageStructure);
         
         // Update the message content cache
         contentFileManager.putMessageContent(handler.getFolder(), messageToken, messageContent);
         
         fireMessageAvailable(messageToken, messageStructure, messageContent, messageSource);
     }
     
     protected void handleMessageContentAvailable(
             MessageToken messageToken,
             MimeMessageContent[] messageContent) {
         
         // Update the status in the folder cache for this message
         FolderRequestHandler handler = getFolderRequestHandler(messageToken);
         handler.setFolderMessageSeen(messageToken);
         
         // Update the message content cache
         contentFileManager.putMessageContent(handler.getFolder(), messageToken, messageContent);
         
         fireMessageContentAvailable(messageToken, messageContent);
         
         // Since message refreshes are currently done such that this handler
         // will be called once for all requested content, it is safe to assume
         // that the operation is complete here.
         messageRefreshComplete(messageToken);
     }
 }
