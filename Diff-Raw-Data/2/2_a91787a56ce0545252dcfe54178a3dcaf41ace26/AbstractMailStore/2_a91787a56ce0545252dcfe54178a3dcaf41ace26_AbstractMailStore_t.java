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
 
 import net.rim.device.api.util.ToIntHashtable;
 
 import org.logicprobe.LogicMail.message.FolderMessage;
 import org.logicprobe.LogicMail.message.MimeMessageContent;
 import org.logicprobe.LogicMail.message.MessageFlags;
 import org.logicprobe.LogicMail.message.MimeMessagePart;
 import org.logicprobe.LogicMail.util.EventListenerList;
 
 /**
  * Provides the base implementation for the asynchronous
  * mail store infrastructure.
  * 
  * <p>Most methods of this class that could talk to a server
  * are designed to return immediately.  The results should
  * be delivered later via an event sent to the appropriate
  * listener.  Since servers can give notifications independent
  * from specific requests, and since commands can fail,
  * clients should not expect an exact match between requests
  * and events.
  */
 public abstract class AbstractMailStore {
     private final EventListenerList listenerList = new EventListenerList();
 
     protected AbstractMailStore() {
     }
 
     /**
      * Shutdown the mail store.
      * <p>
      * Only relevant for non-local stores which have stateful connections.
      * </p>
      * 
      * @param wait If true, wait for all pending requests to finish.
      */
     public abstract void shutdown(boolean wait);
 
     /**
      * Returns whether the mail store is local or remote.
      * 
      * @return True if local, false if remote.
      */
     public abstract boolean isLocal();
 
     /**
      * Returns whether the mail store supports mail folders.
      *
      * @return True if folders supported, false otherwise
      */
     public abstract boolean hasFolders();
 
     /**
      * Returns whether the mail store supports retrieving message
      * parts individually.  If this is true, it should be expected
      * that {@link FolderMessage} objects used by this mail store
      * also have their {@link FolderMessage#getStructure()} field
      * populated.
      * 
      * @return True if individual message part retrieval is supported, false otherwise
      */
     public abstract boolean hasMessageParts();
 
     /**
      * Returns whether the mail store supports message flags.
      * 
      * @return True if message flags supported, false otherwise
      */
     public abstract boolean hasFlags();
 
     /**
      * Returns whether the mail store supports appending messages.
      * 
      * @return True if message appending supported, false otherwise
      */
     public abstract boolean hasAppend();
 
     /**
      * Returns whether the mail store supports server-side copying of messages between mailboxes.
      * 
      * @return True if message copying is supported
      */
     public abstract boolean hasCopy();
 
     /**
      * Returns whether the mail store supports undeletion of messages.
      *
      * @return True if undelete supported, false otherwise
      */
     public abstract boolean hasUndelete();
 
     /**
      * Returns whether the mail store supports expunging of deleted messages.
      * 
      * @return True if expunge supported, false otherwise
      */
     public abstract boolean hasExpunge();
 
     /**
      * Requests the regeneration of the mail folder tree.
      * 
      * <p>Successful completion is indicated by a call to
      * {@link MailStoreListener#folderTreeUpdated(FolderEvent)}.
      * 
      * <p>This can be an expensive operation, so it should
      * be called sparingly on non-local mail stores.
      * 
      * @param callback The callback to receive success or failure notifications about the request
      */
     public abstract void requestFolderTree(MailStoreRequestCallback callback);
 
     /**
      * Requests the regeneration of the mail folder tree.
      * 
      * <p>Successful completion is indicated by a call to
      * {@link MailStoreListener#folderTreeUpdated(FolderEvent)}.
      * 
      * <p>This can be an expensive operation, so it should
      * be called sparingly on non-local mail stores.
      */
     public void requestFolderTree() {
         requestFolderTree(null);
     }
 
     /**
      * Requests that a folder be expunged of deleted messages.
      * 
      * <p>Successful completion is indicated by a call to
      * {@link FolderListener#folderExpunged(FolderEvent)}.
      * 
      * @param folder The folder to expunge
      * @param callback The callback to receive success or failure notifications about the request
      */
     public abstract void requestFolderExpunge(FolderTreeItem folder, MailStoreRequestCallback callback);
 
     /**
      * Requests that a folder be expunged of deleted messages.
      * 
      * <p>Successful completion is indicated by a call to
      * {@link FolderListener#folderExpunged(FolderEvent)}.
      * 
      * @param folder The folder to expunge
      */
     public void requestFolderExpunge(FolderTreeItem folder) {
         requestFolderExpunge(folder, null);
     }
 
     /**
      * Requests the current message counts of a group of folders.
      * 
      * <p>Successful completion is indicated by calls to
      * {@link FolderListener#folderStatusChanged(FolderEvent)}.
      * 
      * @param folders The folder tree items to refresh.
      * @param callback The callback to receive success or failure notifications about the request
      */
     public abstract void requestFolderStatus(FolderTreeItem[] folders, MailStoreRequestCallback callback);
 
     /**
      * Requests the current message counts of a group of folders.
      * 
      * <p>Successful completion is indicated by calls to
      * {@link FolderListener#folderStatusChanged(FolderEvent)}.
      * 
      * @param folders The folder tree items to refresh.
      */
     public void requestFolderStatus(FolderTreeItem[] folders) {
         requestFolderStatus(folders, null);
     }
 
     /**
      * Requests the message listing from a particular folder.
      * 
      * <p>Successful completion is indicated by a call to
      * {@link FolderListener#folderMessagesAvailable(FolderMessagesEvent)}.
      * 
      * @param folder The folder to request a message listing for.
      * @param firstToken The token for the message that precedes the requested range.
      * @param increment The maximum size of the requested range.
      * @param callback The callback to receive success or failure notifications about the request.
      */
     public abstract void requestFolderMessagesRange(FolderTreeItem folder, MessageToken firstToken, int increment, MailStoreRequestCallback callback);
 
     /**
      * Requests the message listing from a particular folder.
      * 
      * <p>Successful completion is indicated by a call to
      * {@link FolderListener#folderMessagesAvailable(FolderMessagesEvent)}.
      * 
      * @param folder The folder to request a message listing for.
      * @param firstToken The token for the message that precedes the requested range.
      * @param increment The maximum size of the requested range.
      */
     public void requestFolderMessagesRange(FolderTreeItem folder, MessageToken firstToken, int increment) {
         requestFolderMessagesRange(folder, firstToken, increment, null);
     }
 
     /**
      * Requests the message listing from a particular folder.
      * All message tokens must refer to messages that exist within the provided
      * folder item, or the results may be unexpected.
      * 
      * <p>Successful completion is indicated by a call to
      * {@link FolderListener#folderMessagesAvailable(FolderMessagesEvent)}.
      * 
      * @param folder The folder to request a message listing for.
      * @param messageTokens The set of tokens for the messages to get headers for.
      * @param flagsOnly If true, only tokens and flags will be fetched
      * @param callback The callback to receive success or failure notifications about the request
      */
     public abstract void requestFolderMessagesSet(FolderTreeItem folder, MessageToken[] messageTokens, boolean flagsOnly, MailStoreRequestCallback callback);
 
     /**
      * Requests the message listing from a particular folder.
      * All message tokens must refer to messages that exist within the provided
      * folder item, or the results may be unexpected.
      * 
      * <p>Successful completion is indicated by a call to
      * {@link FolderListener#folderMessagesAvailable(FolderMessagesEvent)}.
      * 
      * @param folder The folder to request a message listing for.
      * @param messageTokens The set of tokens for the messages to get headers for.
      * @param flagsOnly If true, only tokens and flags will be fetched
      */
     public void requestFolderMessagesSet(FolderTreeItem folder, MessageToken[] messageTokens, boolean flagsOnly) {
         requestFolderMessagesSet(folder, messageTokens, flagsOnly, null);
     }
     
     /**
      * Requests the message listing from a particular folder.
      * All message tokens must refer to messages that exist within the provided
      * folder item, or the results may be unexpected.
      * 
      * <p>Successful completion is indicated by a call to
      * {@link FolderListener#folderMessagesAvailable(FolderMessagesEvent)}.
      * 
      * @param folder The folder to request a message listing for.
      * @param messageTokens The set of tokens for the messages to get headers for.
      * @param callback The callback to receive success or failure notifications about the request
      */
     public void requestFolderMessagesSet(FolderTreeItem folder, MessageToken[] messageTokens, MailStoreRequestCallback callback) {
        requestFolderMessagesSet(folder, messageTokens, false, callback);
     }
 
     /**
      * Requests the message listing from a particular folder.
      * All message tokens must refer to messages that exist within the provided
      * folder item, or the results may be unexpected.
      * 
      * <p>Successful completion is indicated by a call to
      * {@link FolderListener#folderMessagesAvailable(FolderMessagesEvent)}.
      * 
      * @param folder The folder to request a message listing for.
      * @param messageTokens The set of tokens for the messages to get headers for.
      */
     public void requestFolderMessagesSet(FolderTreeItem folder, MessageToken[] messageTokens) {
         requestFolderMessagesSet(folder, messageTokens, false, null);
     }
     
     /**
      * Requests the message listing from a particular folder.
      * 
      * <p>Successful completion is indicated by a call to
      * {@link FolderListener#folderMessagesAvailable(FolderMessagesEvent)}.
      * 
      * @param folder The folder to request a message listing for.
      * @param messageIndices The set of index values for the messages to get headers for.
      * @param callback The callback to receive success or failure notifications about the request
      */
     public abstract void requestFolderMessagesSet(FolderTreeItem folder, int[] messageIndices, MailStoreRequestCallback callback);
 
     /**
      * Requests the message listing from a particular folder.
      * 
      * <p>Successful completion is indicated by a call to
      * {@link FolderListener#folderMessagesAvailable(FolderMessagesEvent)}.
      * 
      * @param folder The folder to request a message listing for.
      * @param messageIndices The set of index values for the messages to get headers for.
      */
     public void requestFolderMessagesSet(FolderTreeItem folder, int[] messageIndices) {
         requestFolderMessagesSet(folder, messageIndices, null);
     }
 
     /**
      * Requests the recent message listing from a particular folder.
      * 
      * <p>
      * Successful completion is indicated by a call to
      * {@link FolderListener#folderMessagesAvailable(FolderMessagesEvent)}.
      * If <tt>flagsOnly</tt> is set to <b>true</b>, then the envelope and
      * structure fields of the returned <tt>FolderMessage</tt> objects
      * will be set to <b>null</b>.
      * </p>
      * 
      * @param folder The folder to request a message listing for.
      * @param flagsOnly If true, only tokens and flags will be fetched
      * @param callback The callback to receive success or failure notifications about the request
      */
     public abstract void requestFolderMessagesRecent(FolderTreeItem folder, boolean flagsOnly, MailStoreRequestCallback callback);
 
     /**
      * Requests the recent message listing from a particular folder.
      * 
      * <p>
      * Successful completion is indicated by a call to
      * {@link FolderListener#folderMessagesAvailable(FolderMessagesEvent)}.
      * If <tt>flagsOnly</tt> is set to <b>true</b>, then the envelope and
      * structure fields of the returned <tt>FolderMessage</tt> objects
      * will be set to <b>null</b>.
      * </p>
      * 
      * @param folder The folder to request a message listing for.
      * @param flagsOnly If true, only tokens and flags will be fetched
      */
     public void requestFolderMessagesRecent(FolderTreeItem folder, boolean flagsOnly) {
         requestFolderMessagesRecent(folder, flagsOnly, null);
     }
 
     /**
      * Requests the recent message listing from a particular folder.
      * 
      * <p>Successful completion is indicated by a call to
      * {@link FolderListener#folderMessagesAvailable(FolderMessagesEvent)}.
      * 
      * @param folder The folder to request a message listing for.
      * @param callback The callback to receive success or failure notifications about the request
      */
     public void requestFolderMessagesRecent(FolderTreeItem folder, MailStoreRequestCallback callback) {
         requestFolderMessagesRecent(folder, false, callback);
     }
 
     /**
      * Requests the recent message listing from a particular folder.
      * 
      * <p>Successful completion is indicated by a call to
      * {@link FolderListener#folderMessagesAvailable(FolderMessagesEvent)}.
      * 
      * @param folder The folder to request a message listing for.
      */
     public void requestFolderMessagesRecent(FolderTreeItem folder) {
         requestFolderMessagesRecent(folder, false, null);
     }
 
     /**
      * Requests a particular message to be loaded.
      * 
      * <p>Successful completion is indicated by a call to
      * {@link MessageListener#messageAvailable(MessageEvent)}.
      * 
      * @param messageToken The token used to identify the message
      * @param callback The callback to receive success or failure notifications about the request
      */
     public abstract void requestMessage(MessageToken messageToken, MailStoreRequestCallback callback);
 
     /**
      * Requests a particular message to be loaded.
      * 
      * <p>Successful completion is indicated by a call to
      * {@link MessageListener#messageAvailable(MessageEvent)}.
      * 
      * @param messageToken The token used to identify the message
      */
     public void requestMessage(MessageToken messageToken) {
         requestMessage(messageToken, null);
     }
 
     /**
      * Requests a particular message part to be loaded.
      * 
      * <p>Successful completion is indicated by a call to
      * {@link MessageListener#messageAvailable(MessageEvent)}.
      * 
      * <p>If <tt>hasMessageParts()</tt> returns <tt>False</tt>,
      * then this method should throw an
      * <tt>UnsupportedOperationException</tt>.
      * 
      * @param messageToken The token used to identify the message
      * @param messagePart The part of the message to load
      * @param callback The callback to receive success or failure notifications about the request
      */
     public abstract void requestMessageParts(MessageToken messageToken, MimeMessagePart[] messageParts, MailStoreRequestCallback callback);
 
     /**
      * Requests a particular message part to be loaded.
      * 
      * <p>Successful completion is indicated by a call to
      * {@link MessageListener#messageAvailable(MessageEvent)}.
      * 
      * <p>If <tt>hasMessageParts()</tt> returns <tt>False</tt>,
      * then this method should throw an
      * <tt>UnsupportedOperationException</tt>.
      * 
      * @param messageToken The token used to identify the message
      * @param messagePart The part of the message to load
      */
     public void requestMessageParts(MessageToken messageToken, MimeMessagePart[] messageParts) {
         requestMessageParts(messageToken, messageParts, null);
     }
 
     /**
      * Requests a particular message to be deleted.
      * 
      * <p>Successful completion is indicated by a call to
      * {@link MessageListener#messageDeleted(MessageEvent)}.
      * 
      * @param messageToken The token used to identify the message
      * @param messageFlags The flags currently associated with the message
      * @param callback The callback to receive success or failure notifications about the request
      */
     public abstract void requestMessageDelete(MessageToken messageToken, MessageFlags messageFlags, MailStoreRequestCallback callback);
 
     /**
      * Requests a particular message to be deleted.
      * 
      * <p>Successful completion is indicated by a call to
      * {@link MessageListener#messageDeleted(MessageEvent)}.
      * 
      * @param messageToken The token used to identify the message
      * @param messageFlags The flags currently associated with the message
      */
     public void requestMessageDelete(MessageToken messageToken, MessageFlags messageFlags) {
         requestMessageDelete(messageToken, messageFlags, null);
     }
 
     /**
      * Requests a particular message to be undeleted.
      * 
      * <p>Successful completion is indicated by a call to
      * {@link MessageListener#messageUndeleted(MessageEvent)}.
      * 
      * <p>If <tt>hasUndelete()</tt> returns <tt>False</tt>,
      * then this method should throw an
      * <tt>UnsupportedOperationException</tt>.
      * 
      * @param messageToken The token used to identify the message
      * @param messageFlags The flags currently associated with the message
      * @param callback The callback to receive success or failure notifications about the request
      */
     public abstract void requestMessageUndelete(MessageToken messageToken, MessageFlags messageFlags, MailStoreRequestCallback callback);
 
     /**
      * Requests a particular message to be undeleted.
      * 
      * <p>Successful completion is indicated by a call to
      * {@link MessageListener#messageUndeleted(MessageEvent)}.
      * 
      * <p>If <tt>hasUndelete()</tt> returns <tt>False</tt>,
      * then this method should throw an
      * <tt>UnsupportedOperationException</tt>.
      * 
      * @param messageToken The token used to identify the message
      * @param messageFlags The flags currently associated with the message
      */
     public void requestMessageUndelete(MessageToken messageToken, MessageFlags messageFlags) {
         requestMessageUndelete(messageToken, messageFlags, null);
     }
 
     /**
      * Requests a particular message to be marked as answered.
      * 
      * <p>Successful completion is indicated by a call to
      * {@link MessageListener#messageFlagsChanged(MessageEvent)}.
      * 
      * <p>If <tt>hasFlags()</tt> returns <tt>False</tt>,
      * then this method should throw an
      * <tt>UnsupportedOperationException</tt>.
      * 
      * @param messageToken The token used to identify the message
      * @param messageFlags The flags currently associated with the message
      * @param callback The callback to receive success or failure notifications about the request
      */
     public abstract void requestMessageAnswered(MessageToken messageToken, MessageFlags messageFlags, MailStoreRequestCallback callback);
 
     /**
      * Requests a particular message to be marked as answered.
      * 
      * <p>Successful completion is indicated by a call to
      * {@link MessageListener#messageFlagsChanged(MessageEvent)}.
      * 
      * <p>If <tt>hasFlags()</tt> returns <tt>False</tt>,
      * then this method should throw an
      * <tt>UnsupportedOperationException</tt>.
      * 
      * @param messageToken The token used to identify the message
      * @param messageFlags The flags currently associated with the message
      */
     public void requestMessageAnswered(MessageToken messageToken, MessageFlags messageFlags) {
         requestMessageAnswered(messageToken, messageFlags, null);
     }
 
     /**
      * Requests a particular message to be marked as forwarded.
      * 
      * <p>Successful completion is indicated by a call to
      * {@link MessageListener#messageFlagsChanged(MessageEvent)}.
      * 
      * <p>If <tt>hasFlags()</tt> returns <tt>False</tt>,
      * then this method should throw an
      * <tt>UnsupportedOperationException</tt>.
      * 
      * @param messageToken The token used to identify the message
      * @param messageFlags The flags currently associated with the message
      * @param callback The callback to receive success or failure notifications about the request
      */
     public abstract void requestMessageForwarded(MessageToken messageToken, MessageFlags messageFlags, MailStoreRequestCallback callback);
 
     /**
      * Requests a particular message to be marked as forwarded.
      * 
      * <p>Successful completion is indicated by a call to
      * {@link MessageListener#messageFlagsChanged(MessageEvent)}.
      * 
      * <p>If <tt>hasFlags()</tt> returns <tt>False</tt>,
      * then this method should throw an
      * <tt>UnsupportedOperationException</tt>.
      * 
      * @param messageToken The token used to identify the message
      * @param messageFlags The flags currently associated with the message
      */
     public void requestMessageForwarded(MessageToken messageToken, MessageFlags messageFlags) {
         requestMessageForwarded(messageToken, messageFlags, null);
     }
     
     /**
      * Requests a message to be appended to a folder.
      * 
      * <p>Unlike other methods, this method requires the raw source of a message
      * in order to add it.  This is because we are often trying to save an
      * exact message that was returned by an operation such as sending mail.
      * 
      * <p>Successful completion is indicated by a call to
      * {@link FolderListener#folderMessagesAvailable(FolderMessagesEvent)}.
      * 
      * <p>If <tt>hasAppend()</tt> returns <tt>False</tt>,
      * then this method should throw an
      * <tt>UnsupportedOperationException</tt>.
      * 
      * @param folder The folder to add the message to
      * @param rawMessage The raw source of the message to add
      * @param initialFlags The initial flags for the message
      * @param callback The callback to receive success or failure notifications about the request
      */
     public abstract void requestMessageAppend(FolderTreeItem folder, String rawMessage, MessageFlags initialFlags, MailStoreRequestCallback callback);
 
     /**
      * Requests a message to be appended to a folder.
      * 
      * <p>Unlike other methods, this method requires the raw source of a message
      * in order to add it.  This is because we are often trying to save an
      * exact message that was returned by an operation such as sending mail.
      * 
      * <p>Successful completion is indicated by a call to
      * {@link FolderListener#folderMessagesAvailable(FolderMessagesEvent)}.
      * 
      * <p>If <tt>hasAppend()</tt> returns <tt>False</tt>,
      * then this method should throw an
      * <tt>UnsupportedOperationException</tt>.
      * 
      * @param folder The folder to add the message to
      * @param rawMessage The raw source of the message to add
      * @param initialFlags The initial flags for the message
      */
     public void requestMessageAppend(FolderTreeItem folder, String rawMessage, MessageFlags initialFlags) {
         requestMessageAppend(folder, rawMessage, initialFlags, null);
     }
 
     /**
      * Requests a message to be copied into a folder on the server-side.
      * 
      * <p>
      * If <tt>hasCopy()</tt> returns <tt>false</tt>,
      * then this method should throw an
      * <tt>UnsupportedOperationException</tt>.
      * </p>
      * 
      * <p>
      * Notification of successful completion is not directly provided,
      * however the destination folder should contain the message if queried.
      * </p>
      * 
      * @param messageToken The token used to identify the message
      * @param destinationFolder The folder to copy the message into
      * @param callback The callback to receive success or failure notifications about the request
      */
     public abstract void requestMessageCopy(MessageToken messageToken, FolderTreeItem destinationFolder, MailStoreRequestCallback callback);
 
     /**
      * Requests a message to be copied into a folder on the server-side.
      * 
      * <p>
      * If <tt>hasCopy()</tt> returns <tt>false</tt>,
      * then this method should throw an
      * <tt>UnsupportedOperationException</tt>.
      * </p>
      * 
      * <p>
      * Notification of successful completion is not directly provided,
      * however the destination folder should contain the message if queried.
      * </p>
      * 
      * @param messageToken The token used to identify the message
      * @param destinationFolder The folder to copy the message into
      */
     public void requestMessageCopy(MessageToken messageToken, FolderTreeItem destinationFolder) {
         requestMessageCopy(messageToken, destinationFolder, null);
     }
 
     /**
      * Adds a <tt>MailStoreListener</tt> to the mail store.
      * 
      * @param l The <tt>MailStoreListener</tt> to be added.
      */
     public void addMailStoreListener(MailStoreListener l) {
         listenerList.add(MailStoreListener.class, l);
     }
 
     /**
      * Removes a <tt>MailStoreListener</tt> from the mail store.
      * @param l The <tt>MailStoreListener</tt> to be removed.
      */
     public void removeMailStoreListener(MailStoreListener l) {
         listenerList.remove(MailStoreListener.class, l);
     }
 
     /**
      * Returns an array of all <tt>MailStoreListener</tt>s
      * that have been added to this mail store.
      * 
      * @return All the <tt>MailStoreListener</tt>s that have been added,
      * or an empty array if no listeners have been added.
      */
     public MailStoreListener[] getMailStoreListeners() {
         return (MailStoreListener[])listenerList.getListeners(MailStoreListener.class);
     }
 
     /**
      * Adds a <tt>FolderListener</tt> to the mail store.
      * 
      * @param l The <tt>FolderListener</tt> to be added.
      */
     public void addFolderListener(FolderListener l) {
         listenerList.add(FolderListener.class, l);
     }
 
     /**
      * Removes a <tt>FolderListener</tt> from the mail store.
      * @param l The <tt>FolderListener</tt> to be removed.
      */
     public void removeFolderListener(FolderListener l) {
         listenerList.remove(FolderListener.class, l);
     }
 
     /**
      * Returns an array of all <tt>FolderListener</tt>s
      * that have been added to this mail store.
      * 
      * @return All the <tt>FolderListener</tt>s that have been added,
      * or an empty array if no listeners have been added.
      */
     public FolderListener[] getFolderListeners() {
         return (FolderListener[])listenerList.getListeners(FolderListener.class);
     }
 
     /**
      * Adds a <tt>MessageListener</tt> to the mail store.
      * 
      * @param l The <tt>MessageListener</tt> to be added.
      */
     public void addMessageListener(MessageListener l) {
         listenerList.add(MessageListener.class, l);
     }
 
     /**
      * Removes a <tt>MessageListener</tt> from the mail store.
      * 
      * @param l The <tt>MessageListener</tt> to be removed.
      */
     public void removeMessageListener(MessageListener l) {
         listenerList.remove(MessageListener.class, l);
     }
 
     /**
      * Returns an array of all <tt>MessageListener</tt>s
      * that have been added to this mail store.
      * 
      * @return All the <tt>MessageListener</tt>s that have been added,
      * or an empty array if no listeners have been added.
      */
     public MessageListener[] getMessageListeners() {
         return (MessageListener[])listenerList.getListeners(MessageListener.class);
     }
 
     /**
      * Notifies all registered <tt>MailStoreListener</tt>s that
      * the folder tree has been updated. 
      * 
      * @param root The root node of the updated folder tree
      */
     protected void fireFolderTreeUpdated(FolderTreeItem root) {
         Object[] listeners = listenerList.getListeners(MailStoreListener.class);
         FolderEvent e = null;
         for(int i=0; i<listeners.length; i++) {
             if(e == null) {
                 e = new FolderEvent(this, root);
             }
             ((MailStoreListener)listeners[i]).folderTreeUpdated(e);
         }
     }
 
     /**
      * Notifies all registered <tt>FolderListener</tt>s that
      * the status of a folder has changed.
      * 
      * @param root The root node of the updated folder tree
      */
     protected void fireFolderStatusChanged(FolderTreeItem root) {
         Object[] listeners = listenerList.getListeners(FolderListener.class);
         FolderEvent e = null;
         for(int i=0; i<listeners.length; i++) {
             if(e == null) {
                 e = new FolderEvent(this, root);
             }
             ((FolderListener)listeners[i]).folderStatusChanged(e);
         }
     }
 
     /**
      * Notifies all registered <tt>FolderListener</tt>s that
      * the message list for a folder has been loaded.
      * 
      * @param folder The folder which has available messages
      * @param messages The messages that are now available
      * @param flagsOnly True if the message data only includes flags
      */
     protected void fireFolderMessagesAvailable(FolderTreeItem folder, FolderMessage[] messages, boolean flagsOnly) {
         Object[] listeners = listenerList.getListeners(FolderListener.class);
         FolderMessagesEvent e = null;
         for(int i=0; i<listeners.length; i++) {
             if(e == null) {
                 e = new FolderMessagesEvent(this, folder, messages, flagsOnly);
             }
             ((FolderListener)listeners[i]).folderMessagesAvailable(e);
         }
     }
     
     /**
      * Notifies all registered <tt>FolderListener</tt>s that
      * the UID-to-message index map for a folder has been loaded.
      * 
      * @param folder The folder which has available messages
      * @param uidIndexMap The UID-to-index map for the folder's messages
      */
     protected void fireFolderMessageIndexMapAvailable(FolderTreeItem folder, ToIntHashtable uidIndexMap) {
         Object[] listeners = listenerList.getListeners(FolderListener.class);
         FolderMessageIndexMapEvent e = null;
         for(int i=0; i<listeners.length; i++) {
             if(e == null) {
                 e = new FolderMessageIndexMapEvent(this, folder, uidIndexMap);
             }
             ((FolderListener)listeners[i]).folderMessageIndexMapAvailable(e);
         }
     }
 
     /**
      * Notifies all registered <tt>FolderListener</tt>s that
      * the folder has been expunged.
      * 
      * @param indices an array of the indices of all expunged messages, or null if not provided
      * @param root The root node of the updated folder tree
      */
     protected void fireFolderExpunged(FolderTreeItem root, int[] indices) {
         Object[] listeners = listenerList.getListeners(FolderListener.class);
         FolderExpungedEvent e = null;
         for(int i=0; i<listeners.length; i++) {
             if(e == null) {
                 e = new FolderExpungedEvent(this, root, indices);
             }
             ((FolderListener)listeners[i]).folderExpunged(e);
         }
     }
 
     /**
      * Notifies all registered <tt>MessageListener</tt>s that
      * a message has been loaded.
      * 
      * @param messageToken The token identifying the message
      * @param messageStructure The message structure
      * @param messageContent The message content
      * @param messageSource The raw message source, if available
      */
     protected void fireMessageAvailable(MessageToken messageToken, MimeMessagePart messageStructure, MimeMessageContent[] messageContent, String messageSource) {
         Object[] listeners = listenerList.getListeners(MessageListener.class);
         MessageEvent e = null;
         for(int i=0; i<listeners.length; i++) {
             if(e == null) {
                 e = new MessageEvent(this, messageToken, messageStructure, messageContent, messageSource);
             }
             ((MessageListener)listeners[i]).messageAvailable(e);
         }
     }
 
     /**
      * Notifies all registered <tt>MessageListener</tt>s that
      * a message has been loaded.
      * 
      * @param messageToken The token identifying the message
      * @param messageContent The message content
      */
     protected void fireMessageContentAvailable(MessageToken messageToken, MimeMessageContent[] messageContent) {
         Object[] listeners = listenerList.getListeners(MessageListener.class);
         MessageEvent e = null;
         for(int i=0; i<listeners.length; i++) {
             if(e == null) {
                 e = new MessageEvent(this, messageToken, messageContent);
             }
             ((MessageListener)listeners[i]).messageAvailable(e);
         }
     }
 
     /**
      * Notifies all registered <tt>MessageListener</tt>s that
      * a message's flags have changed.
      * 
      * @param messageToken The token identifying the message
      * @param messageFlags The updated message flags
      */
     protected void fireMessageFlagsChanged(MessageToken messageToken, MessageFlags messageFlags) {
         Object[] listeners = listenerList.getListeners(MessageListener.class);
         MessageEvent e = null;
         for(int i=0; i<listeners.length; i++) {
             if(e == null) {
                 e = new MessageEvent(this, messageToken, messageFlags);
             }
             ((MessageListener)listeners[i]).messageFlagsChanged(e);
         }
     }
 
     /**
      * Notifies all registered <tt>MessageListener</tt>s that
      * a message has been deleted.
      * 
      * @param messageToken The token identifying the message
      * @param messageFlags The updated message flags
      */
     protected void fireMessageDeleted(MessageToken messageToken, MessageFlags messageFlags) {
         Object[] listeners = listenerList.getListeners(MessageListener.class);
         MessageEvent e = null;
         for(int i=0; i<listeners.length; i++) {
             if(e == null) {
                 e = new MessageEvent(this, messageToken, messageFlags);
             }
             ((MessageListener)listeners[i]).messageDeleted(e);
         }
     }
 
     /**
      * Notifies all registered <tt>MessageListener</tt>s that
      * a message has been undeleted.
      * 
      * @param messageToken The token identifying the message
      * @param messageFlags The updated message flags
      */
     protected void fireMessageUndeleted(MessageToken messageToken, MessageFlags messageFlags) {
         Object[] listeners = listenerList.getListeners(MessageListener.class);
         MessageEvent e = null;
         for(int i=0; i<listeners.length; i++) {
             if(e == null) {
                 e = new MessageEvent(this, messageToken, messageFlags);
             }
             ((MessageListener)listeners[i]).messageUndeleted(e);
         }
     }
 }
