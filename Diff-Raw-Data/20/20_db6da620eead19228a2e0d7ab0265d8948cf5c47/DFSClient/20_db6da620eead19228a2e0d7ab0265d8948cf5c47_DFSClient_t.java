 import java.lang.reflect.Method;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 import edu.washington.cs.cse490h.lib.Callback;
 import edu.washington.cs.cse490h.lib.PersistentStorageReader;
 import edu.washington.cs.cse490h.lib.PersistentStorageWriter;
 import edu.washington.cs.cse490h.lib.Utility;
 
 
 public class DFSClient extends DFSComponent {
 
   private HashMap<Integer, DFSFilename> outstandingSyncRequests;
   private HashMap<Integer, DFSFilename> outstandingOwnershipRequests;
   private PersistentStorageCache cache;
   private int serverAddr;
   // Blocked requests waiting for response on txStart.
   private LinkedList<RequestWrapper> blockedRequests;
   // Blocked commands associated with queued requests.
   private HashMap<DFSFilename, LinkedList<CommandWrapper>> blockedCommands;
   // True if we're waiting on a response to a txStart
   private boolean waitingForTxStartResponse;
   // True if we're currently in the middle of a transaction.
   private boolean hasOpenTransaction;
   private TransactionId currentTxId;
   // Callbacks associated with transaction requests.
   private Callback txStartCb;
   private Callback txCommitCb;
   private Callback txAbortCb;
   // Transaction commits or aborts that have been issued while we're waiting
   // for a txstart response.
   private LinkedList<RequestWrapper> blockedTxRequests;
   public static final int kDirtyWriteBack = 3;
 
   private enum CommandType {
     Create, Get, Put, Append, Delete, TxStart, TxCommit, TxAbort;
   }
 
 
   /**
    * Wrapper for saving reuqests while we wait for a txStart response.
    */
   private static class RequestWrapper {
     CommandType type;
     FileMessage msg;
     String contents;
     Callback cb;
 
     public RequestWrapper(CommandType type, FileMessage msg, String contents,
                           Callback cb) {
       this.type = type;
       this.msg = msg;
       this.contents = contents;
       this.cb = cb;
     }
   }
 
 
   /**
    * Wrapper for saving command state while we wait for server response.
    */
   private static class CommandWrapper {
     CommandType type;
     DFSFilename fname;
     String contents;
     Callback cb;
 
     public CommandWrapper(CommandType type, DFSFilename fname, Callback cb) {
       this(type, fname, null, cb);
     }
 
     public CommandWrapper(CommandType type, DFSFilename fname,
                           String contents, Callback cb) {
       this.type = type;
       this.fname = fname;
       this.contents = contents;
       this.cb = cb;
     }
   }
 
   public DFSClient(DFSNode client, int serverAddr) {
     super(client);
     outstandingSyncRequests = new HashMap<Integer, DFSFilename>();
     outstandingOwnershipRequests = new HashMap<Integer, DFSFilename>();
     blockedRequests = new LinkedList<RequestWrapper>();
     blockedCommands = new HashMap<DFSFilename, LinkedList<CommandWrapper>>();
     cache = new PersistentStorageCache(client, this, true);
     waitingForTxStartResponse = false;
     hasOpenTransaction = false;
     currentTxId = new TransactionId(this.addr, 0);
     blockedTxRequests = new LinkedList<RequestWrapper>();
     this.serverAddr = serverAddr;
   }
 
 
   /**
    * Callback for server response timeout.
    *
    * @param seqno
    *            Sequence number of the timed-out packet.
    *
    */
   public void requestTimeoutCb(Integer seqno) {
     System.err.println("Packet timeout, not doing nuffink now!");
   }
 
 
   /**
    * Remove SyncRequests associated with a given file.
    *
    * @param file
    *            The file of which to cancel SyncRequests.
    */
   private void cancelReadRequests(DFSFilename file) {
     LinkedList<CommandWrapper> cmds = blockedCommands.get(file);
     if (cmds == null)
       return;
 
     Iterator<CommandWrapper> it = cmds.iterator();
     while (it.hasNext()) {
       switch (it.next().type) {
       case Get:
         it.remove();
         break;
       }
     }
   }
 
 
   /**
    * Remove OwnershipRequests associated with a given file.
    *
    * @param file
    *            The file of which to cancel OwnershipRequests.
    */
   private void cancelOwnershipRequests(DFSFilename file) {
     LinkedList<CommandWrapper> cmds = blockedCommands.get(file);
     if (cmds == null)
       return;
 
     Iterator<CommandWrapper> it = cmds.iterator();
     while (it.hasNext()) {
       switch (it.next().type) {
       case Get:
         break;
       case Append:
       case Create:
       case Delete:
       case Put:
         it.remove();
         break;
       }
     }
   }
 
 
   /**
    * Callback for network layer.
    *
    * @param seqno
    *            Sequence number of finished packet.
    *
    * @param e
    *           Exception thrown by possible failure of packet.
    */
   public void networkDeliveryCb(Integer seqno, NetworkExceptions.NetworkException e) {
     DFSFilename file = outstandingSyncRequests.remove(seqno);
     if (file != null) {
       if (e != null)
         cancelReadRequests(file);
     } else {
       file = outstandingOwnershipRequests.remove(seqno);
       if (file != null && e != null)
         cancelOwnershipRequests(file);
     }
   }
 
 
   /**
    * Add a command blockedCommands, creating a new LinkedList if necessary.
    *
    * @param fname
    *            The name of the file associated with the command.
    *
    * @param cmd
    *            The command to be added to blockedCommands.
    */
   private void addBlockedCommand(DFSFilename fname, CommandWrapper cmd) {
     LinkedList<CommandWrapper> cmdList = blockedCommands.get(fname);
 
     if (cmdList == null) {
       cmdList = new LinkedList<CommandWrapper>();
       blockedCommands.put(fname, cmdList);
     }
     cmdList.add(cmd);
   }
 
 
   private void issueRequest(CommandType type, FileMessage msg,
                             Callback userCompleteCb) {
     issueRequest(type, msg, null, userCompleteCb);
   }
 
 
   /**
    * Issue a request to execute a command on the server.
    *
    * If there is an outstanding SyncRequest on the target file, the command is
    * queued until a response to the previous request is received.
    *
    * @param type
    *            Type of command to issue.
    *
    * @param msg
    *            Message to send to the server.
    *
    * @param contents
    *            For write commands (put and append), the contents to be written.
    *
    * @param userCompleteCb
    *            Callback to fire on success.
    */
   private void issueRequest(CommandType type, FileMessage msg, String contents,
                             Callback userCompleteCb) {
     DFSFilename fname = msg.getDFSFileName();
 
     // Create a network delivery callback.
     Method method;
     Callback cb;
     try {
       String[] paramTypes = { "java.lang.Integer",
                               "NetworkExceptions$NetworkException" };
       method = Callback.getMethod("networkDeliveryCb", this, paramTypes);
       cb = new Callback(method, this, null);
     } catch (IllegalArgumentException iae) {
       System.err.println(iae.getMessage());
       return;
     } catch (Exception e) {
       assert(false): "Should never get here.";
       e.printStackTrace();
       return;
     }
 
     if (waitingForTxStartResponse) {
       // We're waiting for a response from a txStart, so queue the request.
       blockedRequests.add(new RequestWrapper(type, msg, contents,
                                              userCompleteCb));
       return;
     } else if (!hasOpenTransaction) {
       System.err.println("Client (addr " + this.addr + ") attempting to " +
                          "issue a request outside of a transaction: " +
                          msg.toString());
       return;
     } else {
       LinkedList<CommandWrapper> cmds = blockedCommands.get(fname);
       // Only send a SyncRequest if there are no queued commands for this file.
       if (cmds == null || cmds.isEmpty()) {
         // Pack and send the message.
         System.err.println("Client (addr " + this.addr + ") sending: " +
                            msg.toString());
         System.err.println("Type=" + type +", cnts=" + contents);
         int seqno = RIOSend(serverAddr, Protocol.DATA, msg.pack(), cb);
 
         // NOTE(evan): I'm not sure what this is doing, so I'm not touching
         //             it for now.
         if (msg instanceof SyncRequestMessage) {
           // Add this SyncRequest to the appropriate data structures.
           if (((SyncRequestMessage) msg).getFlags().isSet(SyncFlags.TransferOwnership))
             outstandingOwnershipRequests.put(seqno, fname);
           else
             outstandingSyncRequests.put(seqno, fname);
         }
       }
 
       // Queue the command to be performed upon response from the server.
       addBlockedCommand(fname, new CommandWrapper(type, fname, contents,
                                                    userCompleteCb));
     }
   }
 
 
   /**
    * Send a transaction start request to the server.
    */
   public void txStart(Callback cb) {
     Flags txFlags = new Flags();
     txFlags.set(TransactionFlags.Start);
     TransactionId txId = new TransactionId(this.addr, 0);
     TransactionMessage txMsg = new TransactionMessage(txId, txFlags, null);
     System.err.println("Client (addr " + this.addr + ") sending transaction " +
                        "start request: " + txMsg.toString());
     int seqno = RIOSend(serverAddr, Protocol.DATA, txMsg.pack(), cb);
     waitingForTxStartResponse = true;
     txStartCb = cb;
   }
 
 
   /**
    * Send a transaction commit request to the server.
    */
   public void txCommit(Callback cb) {
    if (!hasOpenTransaction || !blockedCommands.isEmpty()) {
      // If there isn't a transaction in progress or there are blocked
      // commands, queue the commit.
       blockedRequests.add(new RequestWrapper(CommandType.TxCommit, null,
                                              null, cb));
     } else {
       Flags txFlags = new Flags();
       txFlags.set(TransactionFlags.Commit);
       TransactionId txId = new TransactionId(this.addr, 0);
       TransactionMessage txMsg = new TransactionMessage(txId, txFlags, null);
       System.err.println("Client (addr " + this.addr + ") sending transaction " +
                          "commit request: " + txMsg.toString());
       int seqno = RIOSend(serverAddr, Protocol.DATA, txMsg.pack(), cb);
       txCommitCb = cb;
     }
   }
 
 
   /**
    * Send a transaction abort request to the server.
    */
   public void txAbort(Callback cb) {
    if (!hasOpenTransaction || !blockedCommands.isEmpty()) {
      // If there isn't a transaction in progress or there are blocked
      // commands, queue the abort.
       blockedRequests.add(new RequestWrapper(CommandType.TxAbort, null,
                                              null, cb));
     } else {
       Flags txFlags = new Flags();
       txFlags.set(TransactionFlags.Abort);
       TransactionId txId = new TransactionId(this.addr, 0);
       TransactionMessage txMsg = new TransactionMessage(txId, txFlags, null);
       System.err.println("Client (addr " + this.addr + ") sending transaction " +
                          "abort request: " + txMsg.toString());
       int seqno = RIOSend(serverAddr, Protocol.DATA, txMsg.pack(), cb);
       txAbortCb = cb;
     }
   }
 
 
   /**
    * Send a create request to the server.
    *
    * @param filename
    *            Name of the file to create.
    *
    * @param cb
    *            Callback to fire on success.
    */
   public void create(String filename, Callback cb) {
     Flags flags = new Flags();
     flags.set(SyncFlags.Create);
     flags.set(SyncFlags.TransferOwnership);
     SyncRequestMessage msg =
       new SyncRequestMessage(filename,
                              currentTxId,
                              new FileVersion(currentTxId, 0, 0), flags);
     issueRequest(CommandType.Create, msg, cb);
   }
 
 
   /**
    * Send a get request to the server.
    *
    * @param filename
    *            Name of the file to get.
    *
    * @param cb
    *            Callback to fire on success.
    */
   public void get(String filename, Callback cb) {
     // First check if we have the file cached and read-only.
     PersistentStorageCache.CacheEntry cachedFile = cache.get(new DFSFilename(filename));
     PersistentStorageCache.CacheState state = cachedFile.getState();
 
     if (state == PersistentStorageCache.CacheState.INVALID ||
         (state == PersistentStorageCache.CacheState.READ_ONLY &&
          (cachedFile.getOwner() == cache.kNotOwned))) {
       // If file isn't cached or is owned by someone else, send sync
       // request to server.
       Flags flags = new Flags();
       flags.set(SyncFlags.ReadOnly);
       SyncRequestMessage msg =
         new SyncRequestMessage(filename, currentTxId, cachedFile.getVersion(), flags);
       issueRequest(CommandType.Get, msg, cb);
     } else {
       // File is cached, so we fire callback with its contents.
       cb.setParams(new Object[]{null, cachedFile.getData()});
       try {
         cb.invoke();
       } catch (Exception e) {
         System.err.println("An exception occurred while trying to call the " +
                            "callback for a get request to " + filename + ":");
         e.printStackTrace();
       }
     }
   }
 
 
   /**
    * Send a put request to the server.
    *
    * @param filename
    *            Name of the file to put.
    *
    * @param contents
    *            Contents to put in file.
    *
    * @param cb
    *            Callback to fire on success.
    */
   public void put(String filename, String contents, Callback cb) {
     DFSFilename dfsFname = new DFSFilename(filename);
     PersistentStorageCache.CacheEntry cachedFile = cache.get(dfsFname);
 
     switch (cachedFile.getState()) {
     case INVALID:
     case READ_ONLY:
       // If file isn't cached or is read-only, then try to get ownership.
       Flags flags = new Flags();
       flags.set(SyncFlags.TransferOwnership);
       SyncRequestMessage msg =
         new SyncRequestMessage(filename, currentTxId, cachedFile.getVersion(), flags);
       issueRequest(CommandType.Put, msg, contents, cb);
       break;
     case READ_WRITE:
       // We have ownership, so write new contents and fire callback.
       cache.update(dfsFname, contents);
       cb.setParams(new Object[]{null, cachedFile.getData()});
       try {
 	cb.invoke();
       } catch (Exception e) {
 	System.err.println("An exception occurred while trying to call the " +
 			   "callback for a put request to " + filename + ":");
 	e.printStackTrace();
       }
     }
   }
 
 
   /**
    * Send a append request to the server.
    *
    * @param filename
    *            Name of the file to append.
    *
    * @param contents
    *            Contents to append to file.
    *
    * @param cb
    *            Callback to fire on success.
    */
   public void append(String filename, String contents, Callback cb) {
     DFSFilename dfsFname = new DFSFilename(filename);
     PersistentStorageCache.CacheEntry cachedFile = cache.get(dfsFname);
 
     switch (cachedFile.getState()) {
     case INVALID:
     case READ_ONLY:
       // If file isn't cached or is read-only, then try to get ownership.
       Flags flags = new Flags();
       flags.set(SyncFlags.TransferOwnership);
       SyncRequestMessage msg =
         new SyncRequestMessage(filename, currentTxId, cachedFile.getVersion(), flags);
       issueRequest(CommandType.Append, msg, contents, cb);
       break;
     case READ_WRITE:
       // We have ownership, so write new contents and fire callback.
       cache.update(new DFSFilename(filename),
 		   cachedFile.getData() + contents);
       cb.setParams(new Object[]{null, null});
       try {
 	cb.invoke();
       } catch (Exception e) {
 	System.err.println("An exception occurred while trying to call the " +
 			   "callback for an append request to " + filename +
 			   ":");
 	e.printStackTrace();
       }
     }
   }
 
 
   /**
    * Send a delete request to the server.
    *
    * @param filename
    *            Name of the file to delete.
    *
    * @param cb
    *            Callback to fire on success.
    */
   public void delete(String filename, Callback cb) {
     DFSFilename dfsFname = new DFSFilename(filename);
     PersistentStorageCache.CacheEntry cachedFile = cache.get(dfsFname);
 
     switch (cachedFile.getState()) {
     case INVALID:
     case READ_ONLY:
       // If file isn't cached or is read-only, then try to get ownership.
       Flags flags = new Flags();
       flags.set(SyncFlags.TransferOwnership);
       SyncRequestMessage msg =
         new SyncRequestMessage(filename, currentTxId, cachedFile.getVersion(), flags);
       issueRequest(CommandType.Delete, msg, cb);
       break;
     case READ_WRITE:
       // We have ownership, so delete file.
       cache.delete(new DFSFilename(filename));
       cb.setParams(new Object[]{null, null});
       try {
 	cb.invoke();
       } catch (Exception e) {
 	System.err.println("An exception occurred while trying to call the " +
 			   "callback for an append request to " + filename +
 			   ":");
 	e.printStackTrace();
       }
     }
   }
 
 
   /**
    * Constructs a FileSystemException to wrap a ResponseMessage error code
    * in order to fire user callback.
    *
    * @param msg
    *            The ResponseMessage containing the error code.
    */
   private void handleResponseMessage(ResponseMessage msg) {
     Errors code = ErrorMap.map.get(msg.getCode());
     DFSExceptions.FileSystemException fse =
       new DFSExceptions.FileSystemException(code);
     LinkedList<CommandWrapper> commandList =
       blockedCommands.get(msg.getDFSFileName());
     CommandWrapper command = commandList.remove();
     command.cb.setParams(new Object[]{null,
                                       new DFSFilename(msg.getFileName()), "", fse});
 
     try {
       command.cb.invoke();
     } catch (Exception e) {
       System.err.println("An exception occurred while trying to call the " +
                          "callback for an FileSystemException associated with " +
                          msg.getFileName() + ":");
       e.printStackTrace();
       return;
     }
   }
 
 
   /**
    * Perform a single command.
    *
    * @param command
    *            A CommandWrapper encompassing the command to be performed.
    *
    * @param newContents
    *            Any new file contents to be written by the command.
    */
   private void performCommand(CommandWrapper command, String newContents) {
     PersistentStorageCache.CacheEntry cachedItem = cache.get(command.fname);
 
     Method method;
     Callback dirtyCb;
     try {
       String[] paramTypes = { "DFSFilename",
                               "FileVersion" };
       method = Callback.getMethod("maybeFlushDirtyEntry", this, paramTypes);
       dirtyCb = new Callback(method, this, null);
     } catch (IllegalArgumentException iae) {
       System.err.println(iae.getMessage());
       return;
     } catch (NoSuchMethodException nsme) {
       assert(false): "Should never get here.";
       nsme.printStackTrace();
       return;
     } catch (ClassNotFoundException cnfe) {
       assert(false): "Should never get here.";
       cnfe.printStackTrace();
       return;
     }
 
     switch (command.type) {
     case Create:
       System.err.println("Got create msg!");
       command.cb.setParams(new Object[]{null, command.fname, "", null});
       break;
     case Get:
       command.cb.setParams(new Object[]{null, command.fname, newContents, null});
       break;
     case Put:
       System.err.println("Going to put " + command.contents);
       cache.update(command.fname, command.contents);
       command.cb.setParams(new Object[]{null, command.fname, "", null});
       dirtyCb.setParams(new Object[]{command.fname, cache.get(command.fname).getVersion()});
       parent.addTimeout(dirtyCb, kDirtyWriteBack);
       break;
     case Append:
       cache.update(command.fname, cachedItem.getData() + command.contents);
       command.cb.setParams(new Object[]{null, command.fname, "", null});
       dirtyCb.setParams(new Object[]{command.fname, cache.get(command.fname).getVersion()});
       parent.addTimeout(dirtyCb, kDirtyWriteBack);
       break;
     case Delete:
       cache.delete(command.fname);
       command.cb.setParams(new Object[]{null, command.fname, "", null});
       break;
     }
 
     try {
       command.cb.invoke();
     } catch (Exception e) {
       System.err.println("An exception occurred while trying to call the " +
                          "callback for a request to " +
                          command.fname.getPath() + ":");
       e.printStackTrace();
       return;
     }
   }
 
 
   /**
    * Called when the client receives an arbitrary message.
    *
    * There are three cases, based on message type and context:
    *   * SyncRequest: We are a receiving a request from another client proxied
    *                  through a server.
    *   * SyncData: We are receiving a response from a command we'd previously
    *               issued.
    *   * SyncData: We are receiving an invalidation directive based on the
    *               ownership acquisition by a third party.
    *
    * @param from
    *            The integer ID of sending server.
    *
    * @param msg
    *            The message being received.
    */
   public void onReceive(Integer from, DFSMessage msg) {
     System.err.println("Client (addr " + this.addr + ") received: " +
                        msg.toString());
 
     // Extract the filename from the message, if applicable.
     DFSFilename fname = null;
     if (msg instanceof FileNameMessage)
       fname = ((FileNameMessage) msg).getDFSFileName();
 
     if (msg instanceof ResponseMessage) {
       handleResponseMessage((ResponseMessage) msg);
       return;
     } else if (msg instanceof SyncRequestMessage) {
       SyncRequestMessage srMsg = (SyncRequestMessage) msg;
       Flags flags = srMsg.getFlags();
       PersistentStorageCache.CacheEntry cachedItem = cache.get(fname);
       FileNameMessage response = null;
       Flags responseFlags = new Flags();
 
       switch (msg.getMessageType()) {
       case SyncRequest:
         if (flags.isSet(SyncFlags.TransferOwnership)) {
           // If I'm not the owner, send an error response.
           try {
             cache.relinquishOwnership(fname, cachedItem.getVersion());
             responseFlags.set(SyncFlags.TransferOwnership);
           } catch (IllegalStateException e) {
             response = new ResponseMessage(currentTxId,
                                            fname.toString(),
                                            cachedItem.getVersion(),
                                            Errors.OwnershipConflict.getId());
           }
         } else if (flags.isSet(SyncFlags.ReadOnly)) {
           responseFlags.set(SyncFlags.ReadOnly);
         } else {
           System.err.println("Node " + this.addr + ": Error: Invalid flags " +
                              "in message received from server " + from);
           return;
         }
         break;
       case SyncData:
         SyncDataMessage sdMsg = (SyncDataMessage) msg;
 
         // Set the cache to the freshly-received copy of the file.
         cache.set(fname, sdMsg.getVersion(), sdMsg.getData(),
                   sdMsg.getOwner(), sdMsg.exists());
 
         if (sdMsg.getData() == null && sdMsg.exists()) {
           // We're receiving an invalidation directive, so invalidate the
           // cache entry corresponding to the file.
           System.err.println("Client invalidation directive received!");
           cachedItem.setState(PersistentStorageCache.CacheState.INVALID);
           cachedItem.setOwner(sdMsg.getOwner());
         } else {
           System.err.println("Response associated with outstanding command " +
                              "received!");
 
           Iterator<CommandWrapper> it = blockedCommands.get(fname).iterator();
           boolean grantedOwnership = true;
           if (flags.isSet(SyncFlags.TransferOwnership))
             cache.takeOwnership(fname, sdMsg.getVersion());
           else
             grantedOwnership = false;
 
           // Service all blocked commands, depending on whether we've been
           // granted ownership or read access.
           while (it.hasNext()) {
             CommandWrapper cmd = it.next();
             if (grantedOwnership && cmd.type != CommandType.Get ||
                 !grantedOwnership && cmd.type == CommandType.Get) {
               performCommand(cmd, cmd.contents);
               it.remove();
             }
           }
 
           // Need to ack iff owner != kNotOwned.
           if (sdMsg.getOwner() != cache.kNotOwned)
             response = new AckDataMessage(fname.toString(), currentTxId, sdMsg.getVersion());
         }
         break;
       default:
         System.err.println("Node " + this.addr + ": Error: Invalid message " +
                            "type received from server " + from);
         return;
       }
 
       // If we didn't build an AckDataMessage response, respond with a
       // SyncDataMessage.
       if (response == null) {
         response = new SyncDataMessage(fname.toString(),
                                        currentTxId,
                                        cachedItem.getVersion(),
                                        responseFlags,
                                        cachedItem.getOwner(),
                                        cachedItem.exists(),
                                        cachedItem.getData());
       }
 
       RIOSend(from, Protocol.DATA, response.pack(), null);
     } else if (msg instanceof AckDataMessage) {
       AckDataMessage ackMsg = (AckDataMessage) msg;
 
       PersistentStorageCache.CacheEntry entry = cache.get(fname);
       if (entry.getState() != PersistentStorageCache.CacheState.INVALID &&
           entry.isDirty())
         cache.setClean(fname);
     } else if (msg instanceof TransactionMessage) {
       TransactionMessage txMsg = (TransactionMessage) msg;
       Flags txFlags = txMsg.getFlags();
       if (waitingForTxStartResponse && txFlags.isSet(TransactionFlags.Start) &&
           txFlags.isSet(TransactionFlags.Confirm)) {
         // A response to a txStart has arrived, so service any blocked requests.
         hasOpenTransaction = true;
         waitingForTxStartResponse = false;
 
        int numReqs = blockedRequests.size();
        for (int i = 0; i < numReqs; i++) {
          RequestWrapper req = blockedRequests.remove();
           if (req.type == CommandType.TxCommit) {
             txCommit(req.cb);
           } else if (req.type == CommandType.TxAbort) {
             txAbort(req.cb);
           } else {
             issueRequest(req.type, req.msg, req.contents, req.cb);
           }
         }
 
         // Fire the appropriate callback.
         txStartCb.setParams(new Object[]{TransactionStatus.OK, null, "", null});
 
         try {
           txStartCb.invoke();
         } catch (Exception e) {
           System.err.println("An exception occurred while trying to call the " +
                              "callback for a transaction abort request.");
           e.printStackTrace();
         }
       } else if (txFlags.isSet(TransactionFlags.Abort)) {
         // A txAbort has arrived, so abort the current transaction.
         System.err.println("Client (addr " + this.addr +
                            ") aborting transaction with ID #" + txMsg.getTxId());
         hasOpenTransaction = false;
 
         // Fire the appropriate callback.
         txAbortCb.setParams(new Object[]{TransactionStatus.OK, null, "", null});
 
         try {
           txAbortCb.invoke();
         } catch (Exception e) {
           System.err.println("An exception occurred while trying to call the " +
                              "callback for a transaction abort request.");
           e.printStackTrace();
         }
       } else if (txFlags.isSet(TransactionFlags.Confirm)) {
         System.err.println("Client (addr " + this.addr +
                            ") transaction with ID #" + txMsg.getTxId() +
                            " has completed!");
         hasOpenTransaction = false;
 
         // Fire the appropriate callback.
         txCommitCb.setParams(new Object[]{TransactionStatus.OK, null, "", null});
 
         try {
           txCommitCb.invoke();
         } catch (Exception e) {
           System.err.println("An exception occurred while trying to call the " +
                              "callback for a transaction abort request.");
           e.printStackTrace();
         }
       } else {
         System.err.println("Node " + this.addr + ": Error: Invalid transaction "
                            + "message type received from server " + from);
       }
     }
   }
 
 
   /**
    * Possibly flushes a dirty entry associated with a file.
    *
    * Effectively, we transfer ownership of a file back to the server if
    * the version has been the same for the duration of a timeout.
    *
    * NOTE: this has to be declared public or else the Callback logic won't
    * be able to call it, because of package permission problems.
    *
    * @param file
    *            The file of which to possibly flush a dirty entry.
    *
    * @param expectedVersion
    *            The expected version of the file.
    */
   public void maybeFlushDirtyEntry(DFSFilename file, FileVersion expectedVersion) {
     PersistentStorageCache.CacheEntry entry = cache.get(file);
     if (!entry.getVersion().equals(expectedVersion) ||
         !entry.isDirty())
       return;
 
     try {
       cache.relinquishOwnership(file, expectedVersion);
     } catch (IllegalStateException e) {
       return;
     }
 
     RIOSend(serverAddr, Protocol.DATA,
             new SyncDataMessage(file.toString(),
                                 currentTxId,
                                 entry.getVersion(),
                                 new Flags(SyncFlags.TransferOwnership),
                                 entry.getOwner(),
                                 entry.exists(),
                                 entry.getData()).pack());
   }
 
 }
