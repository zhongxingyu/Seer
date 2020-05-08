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
 
 import org.logicprobe.LogicMail.conf.MailSettings;
 import org.logicprobe.LogicMail.mail.ConnectionHandlerRequest;
 import org.logicprobe.LogicMail.mail.FolderTreeItem;
 import org.logicprobe.LogicMail.mail.MailStoreRequest;
 import org.logicprobe.LogicMail.mail.MailStoreRequestCallback;
 import org.logicprobe.LogicMail.mail.MessageToken;
 import org.logicprobe.LogicMail.mail.NetworkMailStore;
 import org.logicprobe.LogicMail.message.FolderMessage;
 import org.logicprobe.LogicMail.message.MessageFlags;
 import org.logicprobe.LogicMail.message.MimeMessagePart;
 import org.logicprobe.LogicMail.util.AtomicBoolean;
 
 /**
  * Handles folder-oriented requests for the mail store services layer.
  * <p>
  * While most of the application outside of the actual protocol level is
  * written to be as generic as possible, coded based on features instead of
  * specific protocols, this simply is not practical in this case. There are
  * very significant differences between how the various protocols handle their
  * mail folders, making a generic approach to folder refresh completely
  * impractical. As such, protocol specific subclasses are provided to implement
  * this behavior.
  * </p>
  */
 abstract class FolderRequestHandler {
     protected final NetworkMailStoreServices mailStoreServices;
     protected final NetworkMailStore mailStore;
     protected final FolderMessageCache folderMessageCache;
     protected final FolderTreeItem folderTreeItem;
     
     /** Flag to track whether a folder refresh is currently in progress. */
     protected final AtomicBoolean refreshInProgress = new AtomicBoolean();
     
     /**
      * Flag to track whether the current refresh-in-progress is deliberate
      * or automated, so the request objects can be configured appropriately.
      */
     private boolean refreshInProgressDeliberate = true;
     
     /**
      * Collection of <code>Runnable</code> tasks to execute following a
      * refresh operation.
      */
     private final Vector postRefreshTasks = new Vector();
     
     /** Indicates that the initial refresh has completed. */
     protected volatile boolean initialRefreshComplete;
     
     /** Indicates that cached messages have been loaded. */
     private boolean cacheLoaded;
     
     /**
      * Set of messages that have been loaded from the cache, but no longer
      * exist on the server.
      */
     protected final Hashtable orphanedMessageSet = new Hashtable();
     
     /**
      * Set if the mail store is disconnected, to indicate that local state
      * should be cleared prior to the next refresh request.
      */
     protected volatile boolean cleanPriorToUse;
     
     public FolderRequestHandler(
             NetworkMailStoreServices mailStoreServices,
             NetworkMailStore mailStore,
             FolderMessageCache folderMessageCache,
             FolderTreeItem folderTreeItem) {
         
         this.mailStoreServices = mailStoreServices;
         this.mailStore = mailStore;
         this.folderMessageCache = folderMessageCache;
         this.folderTreeItem = folderTreeItem;
     }
 
     public FolderTreeItem getFolder() {
         return folderTreeItem;
     }
 
     public void handleConnect() {
         synchronized(postRefreshTasks) {
             prepareForUse();
         }
     }
     
     public void cleanBeforeNextUse() {
         synchronized(postRefreshTasks) {
             cleanPriorToUse = true;
         }
     }
     
     protected void prepareForUse() {
         if(cleanPriorToUse) {
             refreshInProgressDeliberate = true;
             initialRefreshComplete = false;
             orphanedMessageSet.clear();
             cleanPriorToUse = false;
         }
     }
     
     /**
      * This method is called if the underlying protocol implementation detects
      * conditions that invalidate the current folder state information.
      * It is a likely situation that this method will be invoked during the
      * first request of a folder refresh operation.
      */
     public void requestFolderRefreshRequired() {
         if(!refreshInProgress.get()) {
             cleanBeforeNextUse();
         }
     }
     
     public void requestFolderRefresh(boolean deliberate) {
         requestFolderRefresh(deliberate, null);
     }
     
     public void requestFolderRefresh(boolean deliberate, Runnable postRefreshTask) {
         if(refreshInProgress.compareAndSet(false, true)) {
             prepareForUse();
             this.refreshInProgressDeliberate = deliberate;
             
             if(postRefreshTask != null) {
                 invokeAfterRefresh(postRefreshTask, true);
             }
             
             beginFolderRefreshOperation();
         }
         else {
             if(postRefreshTask != null) {
                 invokeAfterRefresh(postRefreshTask, true);
             }
         }
     }
     
     /**
      * Subclasses should implement this method to to begin the folder refresh
      * operation.  Upon completion of the operation, regardless of outcome,
      * {@link #endFolderRefreshOperation()} must be called.
      */
     protected abstract void beginFolderRefreshOperation();
     
     /**
      * This method should be called upon the completion of a folder refresh.
      * The final cleanup here has no difference in handling depending on
      * success or failure.  This is due to the lack of any follow-up requests.
      * In both cases we commit the cache, mark the refresh as complete, and
      * notify the listeners.
      * 
      * @param success true, if the operation completed successfully
      */
     protected void endFolderRefreshOperation(final boolean success) {
         // These final operations are all placed on the folder request thread
         // queue, to make sure that they happen after the completion of any
         // other refresh-related code.
         mailStoreServices.invokeLater(new Runnable() { public void run() {
             // Clear the set of loaded messages that have not yet been reconciled,
             // which should only be non-empty if this method was called due to an
             // error.
             orphanedMessageSet.clear();
 
             // Commit the folder message cache
             folderMessageCache.commit();
             
             // Clear the flag indicating that a refresh operation is in progress
             refreshInProgress.set(false);
             
             // Notify listeners of the end of the operation
             mailStoreServices.fireFolderMessagesAvailable(folderTreeItem, null, false, false);
 
             // Enqueue any folder-related requests that arrived during the refresh
             invokePostRefreshTasks(success);
         }});
     }
     
     /**
      * Loads cached folder messages, and populates the <code>orphanedMessageSet</code>
      * with them.  If the cache has not already been loaded, then corresponding
      * events are fired to notify listeners of the messages.  The load order is
      * determined by the global message display order setting.
      */
     protected void loadCachedFolderMessages() {
         boolean dispOrder = MailSettings.getInstance().getGlobalConfig().getDispOrder();
         FolderMessage[] messages = folderMessageCache.getFolderMessages(folderTreeItem);
         if(messages.length > 0) {
             // Add all the messages that have been loaded from the
             // cache.  Server-side messages will be removed from the
             // set later on.
             for(int i=0; i<messages.length; i++) {
                 orphanedMessageSet.put(messages[i].getMessageToken().getMessageUid(), messages[i]);
             }
             
             // If the cached messages have already been loaded, then we can
             // skip notifying mail store listeners.  However, we still have to
             // add them to the orphan set, as seen above.
             if(!cacheLoaded) {
                 if(dispOrder) {
                     for(int i=0; i<messages.length; i+=5) {
                         int endIndex = Math.min(i + 5, messages.length);
                         FolderMessage[] subset = new FolderMessage[endIndex - i];
                         for(int j=0; j<subset.length; j++) {
                             subset[j] = messages[i + j];
                         }
                         mailStoreServices.fireFolderMessagesAvailable(folderTreeItem, subset, false, false);
                     }
                 }
                 else {
                     for(int i=messages.length-1; i >= 0; i-=5) {
                         int startIndex = Math.max(i - 4, 0);
                         FolderMessage[] subset = new FolderMessage[i - startIndex + 1];
                         for(int j=0; j<subset.length; j++) {
                             subset[j] = messages[i - j];
                         }
                         mailStoreServices.fireFolderMessagesAvailable(folderTreeItem, subset, false, false);
                     }
                 }
                 cacheLoaded = true;
             }
         }
     }
 
     /**
      * Removes any messages stored in the <code>orphanedMessageSet</code> from
      * the cache, clears the set, and then fires the necessary events to have
      * them expunged by any listeners.
      */
     protected void removeOrphanedMessages() {
         Enumeration e = orphanedMessageSet.elements();
         MessageToken[] orphanedTokens = new MessageToken[orphanedMessageSet.size()];
         int index = 0;
         while(e.hasMoreElements()) {
             FolderMessage message = (FolderMessage)e.nextElement();
             folderMessageCache.removeFolderMessage(folderTreeItem, message);
             orphanedTokens[index++] = message.getMessageToken();
         }
         orphanedMessageSet.clear();
         mailStoreServices.fireFolderExpunged(folderTreeItem, orphanedTokens, new MessageToken[0]);
     }
     
     public void requestMoreFolderMessages(MessageToken firstToken, int increment) {
         processMailStoreRequest(mailStore.createFolderMessagesRangeRequest(folderTreeItem, firstToken, increment)
                 .setRequestCallback(new MailStoreRequestCallback() {
                     public void mailStoreRequestComplete(MailStoreRequest request) { }
                     public void mailStoreRequestFailed(MailStoreRequest request, Throwable exception, boolean isFinal) {
                         // Commit and notify even in cases of failure, to ensure that
                         // post-request operations can occur.
                         folderMessageCache.commit();
                         mailStoreServices.fireFolderMessagesAvailable(folderTreeItem, null, false);
                     }
                 }));
     }
     
     /**
      * Checks if is initial refresh is complete.
      * This flag should be checked prior to any operations that may depend on
      * valid knowledge of server-side message state.
      *
      * @return true, if is the initial refresh is complete
      */
     public boolean isInitialRefreshComplete() {
         return initialRefreshComplete;
     }
     
     void handleFolderMessageFlagsAvailable(FolderMessage[] messages) {
         if(messages != null) {
             Vector updatedMessages = new Vector(messages.length);
             for(int i=0; i<messages.length; i++) {
                 if(folderMessageCache.updateFolderMessage(folderTreeItem, messages[i])) {
                     updatedMessages.addElement(messages[i]);
                 }
             }
             messages = new FolderMessage[updatedMessages.size()];
             updatedMessages.copyInto(messages);
 
         }
         else {
             folderMessageCache.commit();
         }
         mailStoreServices.fireFolderMessagesAvailable(folderTreeItem, messages, true);
     }
     
     void handleFolderMessagesAvailable(FolderMessage[] messages) {
         if(messages != null) {
             for(int i=0; i<messages.length; i++) {
                 
                 // Manually set the "recent" flag on the message if it is marked
                 // as unseen on the server. This is necessary because the
                 // "recent" flag is an unreliable indicator of message newness.
                 // Since this method should only be called when messages are
                 // newly loaded from the server, it shouldn't cause redundant
                 // new message notifications.
                 if(!messages[i].isSeen()) { messages[i].setRecent(true); }
                 
                 folderMessageCache.addFolderMessage(folderTreeItem, messages[i]);
             }
         }
         else {
             folderMessageCache.commit();
         }
         mailStoreServices.fireFolderMessagesAvailable(folderTreeItem, messages, false);
     }
     
     void handleFolderExpunged(int[] indices, MessageToken[] updatedTokens) {
         FolderMessage[] messages = folderMessageCache.getFolderMessages(folderTreeItem);
         for(int i=0; i<messages.length; i++) {
             if(messages[i].isDeleted()) {
                 folderMessageCache.removeFolderMessage(folderTreeItem, messages[i]);
             }
         }
         folderMessageCache.commit();
         mailStoreServices.fireFolderExpunged(folderTreeItem, indices, updatedTokens);
     }
     
     void handleFolderExpunged(MessageToken[] expungedTokens, MessageToken[] updatedTokens) {
         for(int i=0; i<expungedTokens.length; i++) {
             FolderMessage message = folderMessageCache.getFolderMessage(folderTreeItem, expungedTokens[i]);
             if(message != null) {
                 folderMessageCache.removeFolderMessage(folderTreeItem, message);
             }
         }
         folderMessageCache.commit();
         mailStoreServices.fireFolderExpunged(folderTreeItem, expungedTokens, updatedTokens);
     }
     
     /**
      * This method is called at the end of any refresh operation, whether it
      * succeeds or fails.
      * 
      * @param success true, if the refresh operation was successful
      */
     private void invokePostRefreshTasks(boolean success) {
         Vector tasksToRun = null;
         
         synchronized(postRefreshTasks) {
             int size = postRefreshTasks.size();
             if(size > 0) {
                 tasksToRun = new Vector(size);
                 for(int i=0; i<size; i++) {
                     Object[] element = (Object[])postRefreshTasks.elementAt(i);
                     // Add the task if the refresh was successful, or if the
                     // task does not depend on a successful refresh.
                     if(success || !((Boolean)element[1]).booleanValue()) {
                         tasksToRun.addElement((Runnable)element[0]);
                     }
                 }
                 postRefreshTasks.removeAllElements();
             }
         }
         
         if(tasksToRun != null) {
             int size = tasksToRun.size();
             for(int i=0; i<size; i++) {
                 mailStoreServices.invokeLater((Runnable)tasksToRun.elementAt(i));
             }
         }
     }
     
     /**
      * Invoke the provided <code>Runnable</code> after the refresh for this
      * handler's folder has completed, successfully or unsuccessfully.
      * <p>
      * If the refresh has not yet occurred, and <code>triggerRefresh</code>
      * is set, this method will trigger it. With <code>triggerRefresh</code>
      * set, it is also assumed that the task depends on the successful
      * completion of a refresh. As such, if the triggered or in-progress
      * refresh fails, the task will not be executed.
      * </p>
      * <p>
      * If the refresh has already completed, the <code>Runnable</code> will be
      * executed immediately.
      * </p>
      *
      * @param runnable the runnable to execute following a refresh
      * @param triggerRefresh true, if a refresh should be triggered
      */
     public void invokeAfterRefresh(Runnable runnable, boolean triggerRefresh) {
         boolean runImmediately = false;
         
         synchronized(postRefreshTasks) {
             if(refreshInProgress.get()) {
                 // A refresh is currently in progress, so add this task to the
                 // list of post-refresh tasks
                 postRefreshTasks.addElement(new Object[] { runnable, new Boolean(triggerRefresh) });
             }
             else {
                 // A refresh is not in progress
                 if(postRefreshTasks.size() > 0) {
                     // The refresh completed, but the post-refresh tasks have
                     // not yet been executed.  This means we should add our
                     // task to the end of that list.
                     postRefreshTasks.addElement(new Object[] { runnable, new Boolean(triggerRefresh) });
                 }
                 else if((!initialRefreshComplete || cleanPriorToUse) && triggerRefresh) {
                     // The refresh has not yet begun, but is necessary.  This
                     // means we should add our task to the list, and trigger
                     // the refresh operation
                     postRefreshTasks.addElement(new Object[] { runnable, new Boolean(triggerRefresh) });
                     requestFolderRefresh(refreshInProgressDeliberate);
                 }
                 else {
                     // We are okay to execute this task normally
                     runImmediately = true;
                 }
             }
         }
         
         if(runImmediately) {
             runnable.run();
         }
     }
 
     public void handleMessageAvailable(final MessageToken messageToken, final MimeMessagePart messageStructure) {
         invokeAfterRefresh(new Runnable() {
             public void run() {
                 FolderMessage message = folderMessageCache.getFolderMessage(folderTreeItem, messageToken);
                 if(message == null) { return; }
                 
                 boolean updated = false;
                 MessageFlags messageFlags = message.getFlags();
                 if(!messageFlags.isSeen()) {
                     messageFlags.setSeen(true);
                     messageFlags.setRecent(false);
                     if(mailStore.hasFlags()) {
                         processMailStoreRequest(mailStore.createMessageFlagChangeRequest(messageToken, new MessageFlags(MessageFlags.Flag.SEEN), true));
                     }
                     updated = true;
                 }
 
                 if(messageStructure != null) {
                     message.setStructure(messageStructure);
                     updated = true;
                 }
                 
                 if(updated) {
                     folderMessageCache.updateFolderMessage(folderTreeItem, message);
                     folderMessageCache.commit();
                 }
             }
         }, false);
      }
 
     void setFolderMessageSeen(MessageToken messageToken) {
         setFolderMessageSeenImpl(messageToken, false);
     }    
     
     void setFolderMessageSeenCacheOnly(MessageToken messageToken) {
         setFolderMessageSeenImpl(messageToken, true);
     }
     
     private void setFolderMessageSeenImpl(final MessageToken messageToken, final boolean cacheOnly) {
         invokeAfterRefresh(new Runnable() {
             public void run() {
                 if(messageToken == null) { return; }
                 FolderMessage message = folderMessageCache.getFolderMessage(folderTreeItem, messageToken);
                 if(message == null) { return; }
                 MessageFlags messageFlags = message.getFlags();
                 if(!messageFlags.isSeen() || messageFlags.isRecent()) {
                     boolean updateInMailStore = !cacheOnly && mailStore.hasFlags() && !messageFlags.isSeen();
                     
                     messageFlags.setSeen(true);
                     messageFlags.setRecent(false);
                     folderMessageCache.updateFolderMessage(folderTreeItem, message);
                     folderMessageCache.commit();
                     
                     if(updateInMailStore) {
                        mailStore.requestMessageSeen(messageToken);
                     }
                 }
             }
         }, false);
     }
 
     MimeMessagePart getCachedMessageStructure(MessageToken messageToken) {
         // TODO Consider blocking if a refresh is in progress
         FolderMessage message = folderMessageCache.getFolderMessage(folderTreeItem, messageToken);
         if(message != null) {
             return message.getStructure();
         }
         else {
             return null;
         }
     }
     
     protected void processMailStoreRequest(MailStoreRequest request) {
         ((ConnectionHandlerRequest)request).setDeliberate(refreshInProgressDeliberate);
         mailStore.processRequest(request);
     }
 
     /**
      * Callback that should be used for the final operation of the folder
      * refresh process. It will set the <code>initialRefreshComplete</code>
      * flag prior to cleanup.
      */
     protected MailStoreRequestCallback finalFetchCallback = new FolderRefreshRequestCallback() {
         public void mailStoreRequestComplete(MailStoreRequest request) {
             initialRefreshComplete = true;
             endFolderRefreshOperation(true);
         }
     };
     
     /**
      * Standard callback used by all requests that are part of the folder
      * refresh process.
      */
     protected abstract class FolderRefreshRequestCallback implements MailStoreRequestCallback {
         public void mailStoreRequestFailed(MailStoreRequest request, Throwable exception, boolean isFinal) {
             // All folder refresh request failures are handled by cleanly
             // ending the refresh process.
             endFolderRefreshOperation(false);
         }
     }
 }
