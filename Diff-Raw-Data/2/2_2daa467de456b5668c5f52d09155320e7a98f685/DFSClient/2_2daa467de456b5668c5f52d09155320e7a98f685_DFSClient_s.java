 import java.lang.reflect.Method;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Queue;
 import edu.washington.cs.cse490h.lib.Callback;
 import edu.washington.cs.cse490h.lib.PersistentStorageReader;
 import edu.washington.cs.cse490h.lib.PersistentStorageWriter;
 import edu.washington.cs.cse490h.lib.Utility;
 
 
 public class DFSClient extends DFSComponent {
   /**
    * Tracks all transient, online state associated with a file.
    */
   public class DFSFile {
     DFSFilename name;
     Queue<DFSCommands.Command> blockedCommands;
     Queue<Flags> pendingSyncRequests;
     Map<Flags,Boolean> completedSyncRequests;
     FileVersion dirtyWriteBackInFlight;
 
     public DFSFile(DFSFilename name) {
       this.name = name;
       this.blockedCommands = new LinkedList<DFSCommands.Command>();
       this.pendingSyncRequests = new LinkedList<Flags>();
       this.completedSyncRequests = new HashMap<Flags,Boolean>(3);
       this.dirtyWriteBackInFlight = null;
     }
   }
 
   /**
    * TODO
    */
   public class ProxiedProposalRequest {
     Transaction transaction;
 
     public ProxiedProposalRequest(Transaction t) {
       this.transaction = t;
     }
   }
 
   public class TransactionRequest {
     TransactionId id;
     TransactionFlags type;
     Callback cb;
     int seqno;
 
     public TransactionRequest(TransactionId id, TransactionFlags type, Callback cb) {
       this.id = id;
       this.type = type;
       this.cb = cb;
     }
 
     public void complete(ErrorCode err) {
       if (cb == null)
         return;
 
       try {
         TransactionStatus status;
         switch (type)  {
         case Start:
           status = TransactionStatus.Started;
           break;
         case Commit:
           status = TransactionStatus.Committed;
           break;
         default:
         case Abort:
           status = TransactionStatus.Aborted;
           break;
         }
 
         cb.setParams(new Object[]{new DFSExceptions.FileSystemException(err),
                                   id,
                                   (err != null ? status : TransactionStatus.Aborted)});
         cb.invoke();
       } catch (Exception e) {
         System.err.println("Error while invoking TransactionRequest user callback:");
         e.printStackTrace();
       }
     }
   }
 
   public class SyncIdentifier {
     DFSFilename file;
     Flags flags;
     boolean isDirtyWriteBack;
 
     public SyncIdentifier(DFSFilename file,
                           Flags flags,
                           boolean isDirtyWriteBack) {
       this.file = file;
       this.flags = flags;
       this.isDirtyWriteBack = isDirtyWriteBack;
     }
 
     @Override
     public boolean equals(Object other) {
       if (other == null ||
           !(other instanceof SyncIdentifier))
         return false;
 
       SyncIdentifier id = (SyncIdentifier) other;
       return file.equals(id.file) && flags.equals(id.flags);
     }
   }
 
   private PersistentStorageCache cache;
   private int serverAddr;
 
   // In-flight SyncRequests without delivery confirmations.
   private HashMap<Integer, SyncIdentifier> outstandingSyncRequests;
 
   // Requests that cannot proceed until transaction state changes. Also
   // includes, at the head of the queue, the transaction request that's
   // blocking progress.
   private Queue<Object> blockedRequests;
 
   // Stores all transient state of files.
   private HashMap<DFSFilename, DFSFile> files;
 
   private HashSet<DFSFilename> serverAckdDirtyFiles;
   private int numDirtyWritebacksInFlight;
   private int numSyncRequestsInFlight;
 
   // Current Transaction ID. null if no transaction open now.
   private TransactionId currentTxId;
 
   public static final int kDirtyWriteBackTimeout = 3;
   public static final int kRequestTimeout = 10;
   public static final int kTransactionId = 1;
 
   public DFSClient(DFSNode client, int serverAddr) {
     super(client);
     this.serverAddr = serverAddr;
 
     // Request state
     outstandingSyncRequests = new HashMap<Integer,SyncIdentifier>();
     blockedRequests = new LinkedList<Object>();
     serverAckdDirtyFiles = new HashSet<DFSFilename>();
 
     // File state
     files = new HashMap<DFSFilename,DFSFile>();
     cache = new PersistentStorageCache(client, this, false);
   }
 
   /**
    * Get or create the DFSFile object for name.
    */
   private DFSFile getFile(DFSFilename name) {
     DFSFile f = files.get(name);
     if (f == null) {
       f = new DFSFile(name);
       files.put(name, f);
     }
 
     return f;
   }
 
   /**
    * Request an appropriate cache update for the given command, where
    * "appropriate" means a SyncRequest with flags as defined by
    * cmd.getCacheSyncFlags().
    *
    * If a similar request has been just-previously issued, fails the
    * command.
    *
    * @return true if a SyncRequest was sent, false if the command
    *     was failed.
    */
   public boolean requestCacheUpdate(DFSCommands.Command cmd) {
     Flags flags = cmd.getCacheSyncFlags();
     DFSFile file = getFile(cmd.getFile());
 
     if (file.pendingSyncRequests.contains(flags)) {
       System.err.println("Not enqueuing SyncRequest for " + cmd + " because " +
                          "there is already a similar one in-flight!");
       return true;
     }
 
     System.err.println("NOTE: current tx = " + currentTxId);
     SyncRequestMessage msg =
       new SyncRequestMessage(cmd.getFile().toString(),
                              currentTxId,
                              new FileVersion(currentTxId, 0, 0),
                              flags);
 
     int seqno = RIOSend(cmd.getFile().getOwningServer(),
                         Protocol.DATA,
                         msg.pack());
     if (seqno < 0) {
       cmd.complete(ErrorCode.Unknown);
       return false;
     }
 
     numSyncRequestsInFlight++;
     file.pendingSyncRequests.offer(flags);
 
     outstandingSyncRequests.put(seqno, new SyncIdentifier(file.name,
                                                           flags,
                                                           false));
 
     return true;
   }
 
   /**
    * Perform a single command, if possible. This method first does
    * sanity checks on the local node environment (transaction has been
    * started, commit not in progress, etc). It then calls execute() on the
    * command object.  This method contains per-command logic that
    * determines if this can be done by examining the cache.
    *
    * If the command indicates that it requires permissions on the file
    * that are not currently held, the request will be queued in a per-file
    * queue. When permissions are received, the command will be retried.
    * If permissons are denied, the command is considered failed and will
    * terminated with ErrorCode.OwnershipConflict.
    *
    * @param command
    *            A DFSCommands.Command object wrapping the command.
    * @return the result of the command.
    */
   protected ErrorCode perform(DFSCommands.Command cmd) {
     DFSFilename fname = cmd.getFile();
 
     cmd.setTxId(currentTxId);
     ErrorCode e = cmd.execute(cache);
     if (e.isTransient()) {
       System.err.println("" + cmd + " gave transient error " + e + "; sync flags are " + cmd.getCacheSyncFlags().prettyPrint(SyncFlags.byWireId));
       // See if we've previously issued a similar SyncRequest...
       DFSFile f = getFile(fname);
       if (f.completedSyncRequests.containsKey(cmd.getCacheSyncFlags())) {
         System.err.println("Cache sync flags have already been tried -> " +
                            f.completedSyncRequests.get(cmd.getCacheSyncFlags()));
         e = ErrorCode.OwnershipConflict;
         cmd.complete(ErrorCode.OwnershipConflict);
       }
     } else {
       cmd.complete(e);
     }
 
     if (cache.get(cmd.getFile()).isDirty()) {
       Callback dirtyCb = getMaybeFlushDirtyEntryCb();
       dirtyCb.setParams(new Object[]{fname, cache.get(fname).getVersion()});
       parent.addTimeout(dirtyCb,
                         kDirtyWriteBackTimeout);
     }
 
     return e;
   }
 
 
   /**
    * If a command queue exists, enqueues the command at the end. If not,
    * tries to perform the command.
    */
   protected void performOrEnqueue(DFSCommands.Command cmd) {
     Queue<DFSCommands.Command> cmds = getFile(cmd.getFile()).blockedCommands;
 
     // Only perform command if there are no outstanding commands for
     // this file.
     if (!cmds.isEmpty()) {
       cmds.add(cmd);
       return;
     }
 
     ErrorCode e = perform(cmd);
     if (e.isTransient()) {
       if (requestCacheUpdate(cmd)) {
         System.err.println("Queuing command " + cmd);
         cmds.add(cmd);
       }
     }
   }
 
   /**
    * Issue a request to execute a command on the server.
    *
    * If there is an outstanding SyncRequest on the target file, the command is
    * queued until a response to the previous request is received.
    *
    * @param cmd Command to execute.
    */
   public void issueRequest(DFSCommands.Command cmd) {
     if (blockedRequests.size() > 0) {
       // We're waiting for a response from a txStart, so queue the request.
       blockedRequests.add(cmd);
       System.err.println("" + cmd + " took a number (" + blockedRequests.size() + ")");
       return;
     }
 
     if (currentTxId == null) {
       System.err.println("Client (addr " + this.addr + ") attempting to " +
                          "issue a request outside of a transaction: " +
                          cmd.toString());
       cmd.complete(ErrorCode.NoTransaction);
       return;
     }
 
     performOrEnqueue(cmd);
   }
 
   /**
    * DEPRECATED
    *
    * Flush one dirty cache entry with the given file name to the server. If
    * transferOwnership is true, assigns ownership of the entry to the server.
    * Returns true if a SyncData message is in-flight, or false if an error occurred
    * when sending the message.
    *
    * If a dirty cache entry was flushed, increments the global dirty writebacks
    * in-flight counter, and sets the appropriate FileVersion in the DFSFile object.
    *
    * @param fname
    *      File name to flush.
    * @param transferOwnership
    *      true to transfer ownership to the server.
    * @return
    *      true if the SyncData was sent.
    */
   protected boolean flushDirtyEntry(DFSFilename fname, boolean transferOwnership) {
     PersistentStorageCache.CacheEntry cachedFile = cache.get(fname);
     if (!cachedFile.isDirty())
       return false;
 
     if (cachedFile.getVersion().equals(getFile(fname).dirtyWriteBackInFlight))
       return true;
 
     Flags flags = new Flags();
     if (transferOwnership)
       flags.set(SyncFlags.TransferOwnership);
 
     SyncDataMessage sdMsg = new SyncDataMessage(fname.toString(),
                                                 currentTxId,
                                                 cachedFile.getVersion(),
                                                 flags,
                                                 cachedFile.getOwner(),
                                                 cachedFile.exists(),
                                                 cachedFile.getData());
     int seqno = RIOSend(serverAddr,
                         Protocol.DATA,
                         sdMsg.pack(),
                         getNetworkDeliveryCb());
     if (seqno >= 0) {
       numDirtyWritebacksInFlight++;
       getFile(fname).dirtyWriteBackInFlight = cachedFile.getVersion();
       return true;
     } else {
       return false;
     }
   }
 
   /**
    * Flush all dirty entries in the cache to the server. This method
    * iterates through all dirty entries in the cache and calls
    * flushDirtyEntry() on the entry. Returns the number of dirty entries
    * with in-flight SyncData messages. If flushDirtyEntry() returns false
    * (i.e. an error occurred while sending the SyncData message), this
    * method returns -(numSuccessfulFlushes + 1), which will always be
    * a negative integer.
    *
    * If you see a negative integer, the expression (-retVal - 1) gives
    * a positive integer representing the number of successfully-flushed
    * entries.
    *
    * This method fails fast; if one entry fails, it won't try to resend
    * any remaining entries.
    *
    * @param transferOwnership
    *      passed to flushDirtyEntry()
    * @return the number of successfully flushed entries, or if one fails,
    *      -(numSuccessfullyFlushed + 1).
    */
   protected int performTransactionCommit(boolean transferOwnership) {
     Transaction transaction = new Transaction(currentTxId);
     Iterator<DFSFilename> it = cache.dirtyIterator();
     while (it.hasNext()) {
       DFSFilename fname = it.next();
       PersistentStorageCache.CacheEntry cachedFile = cache.get(fname);
       // Only include the file if it's dirty and isn't already in flight.
       if (cachedFile.isDirty() &&
           !cachedFile.getVersion().equals(getFile(fname).dirtyWriteBackInFlight)) {
         transaction.addFile(cachedFile);
       }
     }
 
    Proposal p = new TransactionProposal(transaction);
     PaxosNode.propose(p);
     return transaction.files.size();
   }
 
   /**
    * Service as many requests in the blocked requests queue as possible.
    * If a TransactionRequest is encountered, performs the request if
    * possible.
    */
   protected void serviceBlockedRequests() {
     Iterator<Object> it = blockedRequests.iterator();
 
     while (it.hasNext()) {
       Object rq = it.next();
       if (rq instanceof DFSCommands.Command) {
         performOrEnqueue((DFSCommands.Command) rq);
       } else if (rq instanceof TransactionRequest) {
         if (outstandingSyncRequests.size() > 0) {
           System.err.println("Deferring transaction commit because SyncRequests " +
                              "are currently in-flight!");
           return;
         }
         sendTransactionRequest();
         return;
       } else {
         System.err.println("Dropping unknown blocked request " +
                            rq.toString());
       }
       it.remove();
     }
   }
 
   /**
    * Sends the transaction request at the head of blockedRequests.
    */
   protected void sendTransactionRequest() {
     if (!(blockedRequests.peek() instanceof TransactionRequest)) {
       System.err.println("Tried to send transaction request at the head of the " +
                          "blocked requests queue, but no transaction request " +
                          "was there....");
       System.exit(10);
     }
 
     TransactionRequest rq = (TransactionRequest) blockedRequests.peek();
     if (rq == null || !(rq instanceof TransactionRequest))
       return;
 
     if (rq.type.equals(TransactionFlags.Start)) {
       if (currentTxId != null) {
         blockedRequests.poll();
         rq.complete(ErrorCode.DuplicateTransaction);
         serviceBlockedRequests();
         return;
       }
     } else if (rq.type.equals(TransactionFlags.Commit)) {
       if (currentTxId == null) {
         blockedRequests.poll();
         rq.complete(ErrorCode.NoTransaction);
         serviceBlockedRequests();
         return;
       }
 
       int numFlushed = performTransactionCommit(true);
       System.err.println("Flushed " + numFlushed + " dirty entries to server...");
       if (numFlushed < 0) {
         rq.complete(ErrorCode.Unknown);
         blockedRequests.poll();
         return;
       } else if (numFlushed > 0) {
         return;
       }
     } else if (rq.type.equals(TransactionFlags.Abort)) {
       if (currentTxId == null) {
         blockedRequests.poll();
         rq.complete(ErrorCode.NoTransaction);
         serviceBlockedRequests();
         return;
       }
     } else {
       System.err.println("Unknown type of transaction request wound up in the " +
                          "blocked request queue...");
       blockedRequests.poll();
       rq.complete(ErrorCode.Unknown);
       serviceBlockedRequests();
       return;
     }
 
     Map<DFSFilename,FileVersion> changedFiles = null;
     if (rq.type.equals(TransactionFlags.Commit)) {
       changedFiles = new HashMap<DFSFilename,FileVersion>();
 
       for (DFSFilename f : serverAckdDirtyFiles)
         changedFiles.put(f, cache.get(f).getVersion());
     }
 
     Flags flags = new Flags();
     flags.set(rq.type);
     TransactionMessage msg = new TransactionMessage(rq.id,
                                                     flags,
                                                     changedFiles);
     int seqno = RIOSend(serverAddr,
                         Protocol.DATA,
                         msg.pack(),
                         getNetworkDeliveryCb());
     if (seqno < 0) {
       rq.complete(ErrorCode.Unknown);
       blockedRequests.poll();
       serviceBlockedRequests();
       return;
     }
 
     rq.seqno = seqno;
   }
 
   /**
    * Enqueues a TransactionRequest in blockedRequests. If no other blocked
    * requets are present, sends the transaction request.
    */
   protected void enqueueTransactionRequest(TransactionRequest rq) {
     blockedRequests.add(rq);
     if (blockedRequests.size() == 1 &&
         (!rq.type.equals(TransactionFlags.Commit) ||
          !cache.dirtyIterator().hasNext()))
       sendTransactionRequest();
   }
 
   protected void completeOutstandingTransactionRequest(ErrorCode err) {
     if (blockedRequests.size() == 0 ||
         !(blockedRequests.peek() instanceof TransactionRequest)) {
       System.err.println("Tried to complete an outstanding TransactionRequest, but " +
                          "no such request was ongoing...");
       System.exit(10);
     }
 
     TransactionRequest txReq = (TransactionRequest) blockedRequests.poll();
     invalidateAllFiles();
 
     if (err.equals(ErrorCode.Success)) {
       if (txReq.type.equals(TransactionFlags.Start))
         currentTxId = txReq.id;
       else if (txReq.type.equals(TransactionFlags.Abort) ||
                txReq.type.equals(TransactionFlags.Commit))
         currentTxId = null;
       else
         System.err.println("WARNING: completing transaction request of unknown type...");
     }
 
     txReq.complete(err);
     serviceBlockedRequests();
   }
 
 
   /**
    * Send a transaction start request to the server. If a transaction is already
    * started, and no other transaction requests are in blockedRequests, fails the
    * request with DuplicateTransaction.
    *
    * @param cb
    *      Callback to invoke when the request is complete.
    */
    public void txStart(Callback cb) {
      if (currentTxId != null && blockedRequests.size() == 0) {
        try {
          cb.setParams(new Object[]{ErrorCode.DuplicateTransaction});
          cb.invoke();
        } catch (Exception e) {
          System.err.println("Error while invoking StartTransaction user callback:");
          e.printStackTrace();
        }
        return;
      }
 
      TransactionRequest rq = new TransactionRequest(new TransactionId(this.addr,
                                                                       kTransactionId),
                                                     TransactionFlags.Start,
                                                     cb);
 
     enqueueTransactionRequest(rq);
   }
 
   /**
    * Send a transaction commit request to the server.
    */
   public void txCommit(Callback cb) {
     if (currentTxId == null && blockedRequests.size() == 0) {
       try {
         cb.setParams(new Object[]{ErrorCode.NoTransaction});
         cb.invoke();
        } catch (Exception e) {
          System.err.println("Error while invoking StartTransaction user callback:");
          e.printStackTrace();
        }
       return;
     }
 
      TransactionRequest rq = new TransactionRequest(new TransactionId(this.addr,
                                                                       kTransactionId),
 
                                                     TransactionFlags.Commit,
                                                     cb);
      enqueueTransactionRequest(rq);
   }
 
   /**
    * Commit the transaction request at the head of the blockedRequests queue.
    */
   public boolean doCommit() {
     if (!(blockedRequests.peek() instanceof TransactionRequest) ||
         !((TransactionRequest) blockedRequests.peek()).type.equals(TransactionFlags.Commit)) {
       System.err.println("Tried to commit a blocked request that wasn't a TxCommit!");
       System.exit(1);
     }
 
     // TODO(andrew): do we actually need this method...? Perhaps for Paxos...
     return true;
   }
 
   /**
    * Invalidates all files in the cache that have been changed by the client.
    * More directly, invalidates all files with a non-zero revision.
    * Call this method when a transaction terminates to reset the cache to
    * follow the committed version history. This is sort of like deleting a
    * branch in git.
    */
   public void invalidateAllFiles() {
     System.err.println("Invalidating files...");
     Iterator<DFSFilename> it = cache.iterator();
     while (it.hasNext()) {
       DFSFilename file = it.next();
       System.err.print("Invadidate " + file + " -> ");
       if (!cache.get(file).getState().equals(PersistentStorageCache.CacheState.INVALID))
         cache.invalidate(file, false);
       System.err.println(cache.get(file).getState());
     }
   }
 
   /**
    * Send a transaction abort request to the server.
    */
   public void txAbort(Callback cb) {
     if (currentTxId == null && blockedRequests.size() == 0) {
       try {
         cb.setParams(new Object[]{ErrorCode.NoTransaction});
         cb.invoke();
        } catch (Exception e) {
          System.err.println("Error while invoking StartTransaction user callback:");
          e.printStackTrace();
        }
 
       return;
     }
 
     TransactionRequest rq = new TransactionRequest(new TransactionId(this.addr,
                                                                      kTransactionId),
                                                    TransactionFlags.Abort,
                                                    cb);
     enqueueTransactionRequest(rq);
   }
 
   /** Delete the given outstanding sync request. Call this method when
    * a response to a SyncRequest has been received.
    *
    * @param file File associated with the SyncRequest
    * @param flags Flags associated with the SyncRequest
    */
   public void deleteOutstandingSyncRequest(DFSFilename file,
                                            Flags f) {
     numSyncRequestsInFlight--;
     SyncIdentifier id = new SyncIdentifier(file, f, false);
     for (Integer i : outstandingSyncRequests.keySet()) {
       if (outstandingSyncRequests.get(i).equals(id)) {
         outstandingSyncRequests.remove(i);
         return;
       }
     }
   }
 
 
   /**
    * Perform as many outstanding commands as possible on fname. Examines
    * the state given in DFSFile to determine if cache state has transitioned
    * for better or for worse; if state is not acceptable for the commmand,
    * fails it with an error code.
    *
    * @return true if no more outstanding commands exist in this file's
    *         command queue.
    */
   private boolean performOutstandingCommands(DFSFilename fname) {
     PersistentStorageCache.CacheEntry entry = cache.get(fname);
 
     Iterator<DFSCommands.Command> it = getFile(fname).blockedCommands.iterator();
 
     // Service all blocked commands, depending on whether we've been
     // granted ownership or read access.
     while (it.hasNext()) {
       DFSCommands.Command cmd = it.next();
 
       ErrorCode err = perform(cmd);
       if (err.isTransient()) {
         if (!requestCacheUpdate(cmd))
           cmd.complete(ErrorCode.Unknown);
         else
           return false;
       }
 
       it.remove();
     }
 
     return true;
   }
 
   private void handleSyncDataMessage(SyncDataMessage msg) {
     DFSFilename fname = msg.getDFSFileName();
     DFSFile file = getFile(fname);
 
     // Set the cache to the freshly-received copy of the file.
     System.err.println("Set " + fname + " ver " + msg.getVersion());
     cache.set(fname, msg.getVersion(), msg.getData(),
               msg.getOwner(), msg.exists());
 
     if (msg.getFlags().isSet(SyncFlags.TransferOwnership))
       cache.takeOwnership(fname, msg.getVersion());
 
     PersistentStorageCache.CacheEntry cachedItem = cache.get(fname);
 
     if (msg.getData() == null && msg.exists()) {
       // We're receiving an invalidation directive, so invalidate the
       // cache entry corresponding to the file.
       System.err.println("Client invalidation directive received!");
       cachedItem.setState(PersistentStorageCache.CacheState.INVALID);
       cachedItem.setOwner(msg.getOwner());
     } else {
       if (file.pendingSyncRequests.size() == 0) {
         System.err.println("Received a SyncData message, but no outstanding " +
                            "SyncRequests were found :(");
         return;
       }
 
       if (msg.getFlags().equals(file.pendingSyncRequests.peek())) {
         deleteOutstandingSyncRequest(file.name, msg.getFlags());
 
         file.completedSyncRequests.put(file.pendingSyncRequests.poll(), true);
         performOutstandingCommands(fname);
 
         if (numSyncRequestsInFlight == 0 && numDirtyWritebacksInFlight == 0)
           serviceBlockedRequests();
       } else {
         System.err.println("Received a SyncData message, but apparently it was " +
                            "out-of-order with respect to SyncRequests...");
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
     if (msg.getFileName().equals("")) {
       // Message was in response to a TransactionMessage
       if (blockedRequests.size() > 0 &&
           blockedRequests.peek() instanceof TransactionRequest &&
           ((TransactionRequest) blockedRequests.peek()).id.equals(msg.getTxId())) {
         ErrorCode code = ErrorMap.map.get(msg.getCode());
 
         completeOutstandingTransactionRequest(code);
       } else {
         System.err.println("Received a response message for an unknown transaction!");
       }
       return;
     }
 
     if (files.get(msg.getDFSFileName()) == null) {
       System.err.println("Recieved a ResponseMessage for an unknown file!");
       return;
     }
 
     // Message is for a file, try to find a SyncRequest to match it to.
     DFSFile file = getFile(msg.getDFSFileName());
 
     if (msg.getVersion().getRevision() > 0) {
       // If revision != 0, this is an error on a SyncData message we sent.
       numDirtyWritebacksInFlight--;
 
       if (msg.getVersion().equals(file.dirtyWriteBackInFlight)) {
         file.dirtyWriteBackInFlight = null;
       }
     } else if (file.pendingSyncRequests.size() > 0) {
       // Response was for a SyncRequest that we initiated...
       deleteOutstandingSyncRequest(file.name, file.pendingSyncRequests.peek());
       file.completedSyncRequests.put(file.pendingSyncRequests.poll(), false);
       performOutstandingCommands(msg.getDFSFileName());
     } else {
       System.err.println("Can't figure out what this ResponseMessage was for!");
       return;
     }
 
 
     if (numSyncRequestsInFlight == 0 &&
         numDirtyWritebacksInFlight == 0)
       serviceBlockedRequests();
   }
 
   /**
    * Handles a received AckDataMessage. Behavior varies based on what ack is
    * is perceived as being in response to:
    *   * If the client is waiting for acks in response to a transaction commit
    *     request, then the ack is treated as such.
    *   * Otherwise, [TODO]
    *
    * @param fname
    *            The name of the file associated with the AckDataMessage.
    */
   private void handleAckDataMessage(AckDataMessage msg) {
     if (!files.containsKey(msg.getDFSFileName())) {
       System.err.println("Got an ack data message, don't know what it's for...");
       return;
     }
 
     DFSFile file = getFile(msg.getDFSFileName());
     if (file.dirtyWriteBackInFlight.equals(msg.getVersion())) {
       file.dirtyWriteBackInFlight = null;
       cache.setClean(msg.getDFSFileName());
     }
 
     numDirtyWritebacksInFlight--;
     serverAckdDirtyFiles.add(msg.getDFSFileName());
 
     if (numDirtyWritebacksInFlight == 0) {
       if (!cache.dirtyIterator().hasNext())
         serviceBlockedRequests();
       else
         System.err.println("Not servicing blocked requests; some files are still dirty!");
     }
   }
 
 
   /**
    * Handles a received TransactionMessage. Behavior varies based on what flags
    * are set.
    *
    * @param msg
    *            The received message.
    */
   private void handleTransactionMessage(TransactionMessage msg) {
     Flags txFlags = msg.getFlags();
     if (txFlags.isSet(TransactionFlags.Confirm)) {
       // Server is confirming one of our requests; is it the right one?
       if (blockedRequests.size() == 0 ||
           !(blockedRequests.peek() instanceof TransactionRequest)) {
         System.err.println("Received a confirming transaaction message, but " +
                            "no transaction is active atm; dropping message...");
         return;
       }
 
       TransactionRequest txReq = (TransactionRequest) blockedRequests.peek();
 
       txFlags.clear(TransactionFlags.Confirm);
       if (!txFlags.isSet(txReq.type)) {
         System.err.println("Received a confirmation for a transaction message " +
                            "of type " + txFlags.prettyPrint(TransactionFlags.byWireId) +
                            ", but the last transaction request we sent was type " +
                            txReq.type);
         return;
       }
 
       if (txReq.type.equals(TransactionFlags.Commit))
         doCommit();
 
       completeOutstandingTransactionRequest(ErrorCode.Success);
     } else if (txFlags.isSet(TransactionFlags.Abort)) {
       completeOutstandingTransactionRequest(ErrorCode.OwnershipConflict);
     } else {
       System.err.println("Received a TransactionMessage that didn't make sense " +
                          "(flags were " +
                          txFlags.prettyPrint(TransactionFlags.byWireId) + ")");
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
 
     switch (msg.getMessageType()) {
     case Response:
       handleResponseMessage((ResponseMessage) msg);
       break;
     case SyncRequest:
       System.err.println("Unexpected SyncRequest!");
       break;
     case SyncData:
       handleSyncDataMessage((SyncDataMessage) msg);
       break;
     case Transaction:
       handleTransactionMessage((TransactionMessage) msg);
       break;
     case AckData:
       handleAckDataMessage((AckDataMessage) msg);
       break;
     default:
       System.err.println("Node " + this.addr + ": Error: Invalid message " +
                          "type received from server " + from);
       return;
     }
   }
 
   protected void failPacket(int seqno, ErrorCode err) {
     SyncIdentifier id = outstandingSyncRequests.remove(seqno);
     if (id == null) {
       // No SyncRequest was in-flight with this seqno; check if an outstanding
       // transaction request has this seqno...
       if (blockedRequests.size() == 0 ||
           !(blockedRequests.peek() instanceof TransactionRequest))
         return;
 
       TransactionRequest txReq = (TransactionRequest) blockedRequests.peek();
       if (txReq.seqno != seqno)
         return;
 
       // Outstanding transaction request timed out
       completeOutstandingTransactionRequest(err);
       return;
     }
 
     DFSFile file = getFile(id.file);
     if (!file.pendingSyncRequests.contains(id.flags)) {
       System.err.println("SyncRequest timed out, but can't find it!");
       return;
     }
 
     file.pendingSyncRequests.remove(id.flags);
     file.completedSyncRequests.put(id.flags, false);
     performOutstandingCommands(id.file);
   }
 
   // ----------------------------[ Callbacks ]----------------------------------
 
   /**
    * Stupid boilerplate code to instantiate a callback.
    *
    * @return a Calblack with no params set to the network delivery cb.
    */
   protected Callback getNetworkDeliveryCb() {
     Method method;
     Callback cb;
     try {
       String[] paramTypes = { "java.lang.Integer",
                               "NetworkExceptions$NetworkException" };
       method = Callback.getMethod("networkDeliveryCb", this, paramTypes);
       return new Callback(method, this, null);
     } catch (Exception e) {
       assert(false): "Should never get here.";
       System.err.println("When trying to instantiate network delivery cb:");
       e.printStackTrace();
       System.exit(10);
       return null;
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
     if (seqno < 0)
       return;
 
     if (e == null) {
       Callback rqTimeoutCb = getRequestTimeoutCb();
       rqTimeoutCb.setParams(new Object[]{seqno});
       parent.addTimeout(rqTimeoutCb, kRequestTimeout);
       return;
     }
 
     failPacket(seqno, ErrorCode.NetworkTimeout);
   }
 
   /**
    * Stupid boilerplate code to instantiate a callback.
    *
    * @return a Calblack with no params set to the request timeout cb.
    */
   protected Callback getRequestTimeoutCb() {
     Method method;
     Callback cb;
     try {
       String[] paramTypes = { "java.lang.Integer" };
       method = Callback.getMethod("requestTimeoutCb", this, paramTypes);
       return new Callback(method, this, null);
     } catch (Exception e) {
       assert(false): "Should never get here.";
       System.err.println("When trying to instantiate network delivery cb:");
       e.printStackTrace();
       System.exit(10);
       return null;
     }
   }
 
 
   /**
    * Callback for server response timeout.
    *
    * @param seqno
    *            Sequence number of the timed-out packet.
    *
    */
   public void requestTimeoutCb(Integer seqno) {
     failPacket(seqno, ErrorCode.NetworkTimeout);
   }
 
   /**
    * Stupid boilerplate code to instantiate a callback.
    *
    * @return a Calblack with no params set to the maybe-flush-dirty-entry cb.
    */
   protected Callback getMaybeFlushDirtyEntryCb() {
     Method method;
     Callback dirtyCb;
     try {
       String[] paramTypes = { "DFSFilename",
                               "FileVersion" };
       method = Callback.getMethod("maybeFlushDirtyEntry", this, paramTypes);
       return new Callback(method, this, null);
     } catch (Exception e) {
       assert(false): "Should never get here.";
       System.err.println("When trying to instantiate maybe-flush-dirty-entry cb:");
       e.printStackTrace();
       System.exit(10);
       return null;
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
