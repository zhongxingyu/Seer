 import java.io.File;
 import java.lang.reflect.Method;
 import java.util.Iterator;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Set;
 import java.util.TreeMap;
 import edu.washington.cs.cse490h.lib.Callback;
 import edu.washington.cs.cse490h.lib.PersistentStorageReader;
 import edu.washington.cs.cse490h.lib.PersistentStorageWriter;
 
 public class DFSServer extends DFSComponent {
 
   public class Transaction {
     TransactionId id;
     Map<DFSFilename,Checkout> checkouts;
     VersionRange committableRange;
     
     public Transaction(TransactionId id) {
       this.id = id;
       this.checkouts = new HashMap<DFSFilename,Checkout>();
     }
   }
 
   public class Checkout {
     Transaction transaction;
     FileVersion version;
     boolean isOwner;
     PersistentStorageCache.CacheEntry entry;
 
     public Checkout(Transaction t, FileVersion version, boolean isOwner) {
       this(t, version, owner, null);
     }
 
     public Checkout(Transaction t,
                     FileVersion version, 
                     boolean isOwner,
                     PersistentStorageCache.CacheEntry entry) {
       this.transaction = t;
       this.version = version;
       this.isOwner = isOwner;
       this.entry = entry;
     }
 
     public void commit(int commitTime) {
       client = PersistentStorageCache.kNotOwned;
       version.commit(commitTime);
     }
     
     public void delete() {
       if (version.isCommitted())
         throw new IllegalStateException("Cannot delete a committed version");
       
       if (entry == null)
         return;
 
       cache.forget(entry.getKey());
     }
 
   /**
    * Tracker class to track state related to transactions.
    */
   public class FileState { 
     public VersionedString liveVersions;
     public TreeMap<FileVersion,Checkout> checkouts;
     
     public FileState(DFSFilename file, PersistentStorageCache cache) {
       entry = cache.get(file);
       if (entry == null || entry.isInvalid())
         throw new IllegalArgumentException("No such cache entry!");
 
       liveVersions = new VersionedString(entry.getVersion(), entry.getData());
       checkouts = new TreeMap<FileVersion,Checkout>();
     }
 
     /**
      * Reduce the given commit window to bound the times when the associated
      * commit may proceed.
      *
      * @param checkoutVersion Checkout to ensure is in the version bound
      * @param range Version time bound.
      */
     public void reduceCommittableVersionRange(FileVersion checkoutVersion,
                                               VersionRange range) {
       VersionRange.Bound upperBound = null;
       VersionRange.Bound lowerBound = null;
       NavigableMap<FileVersion,Checkout> entry = checkouts.floorEntry(checkoutVersion);
       if (entry == null)
         throw new IllegalArgumentException("No such checkout!");
 
       NavigableMap<FileVersion,Checkout> nextEntry = checkouts.higherEntry(entry);
 
       while (nextEntry != null) {
         if (nextEntry.getKey().getRevision() == 0) {
           if (entry.getKey().isOwned) {
             range.collapse();
             return;
           }
 
           upperBound = new VersionRange.Bound(nextEntry.getKey().getCommitTime(),
                                               nextEntry.getValue());
           break;
         }
       }
 
       nextEntry = checkouts.lowerEntry(entry);
       while (nextEntry != null) {
         if (nextEntry.getKey().getRevision() == 0) {
           lowerBound = new VersionRange.Bound(nextEntry.getKey().getCommitTime(),
                                               nextEntry.getValue());
           break;
         }
       }
       
       range.reduce(lowerBound, upperBound);
     }
 
     /**
      * Handles a commit on the file. 
      */
       //go through list of syncdatamessages (from syncDataMessagesByTxID to deduce the state of files.
       //use a hashmap, dude, put(DFSFilename, int version (or FileVersion))
       //this determines where to record a commit relative to other commits
       //ex: for the given transaction, file A is at version 3
       //B is at 1
       //C is at 0
       //but someone else wrote A and committed before this commit. (Check the cache) A is now 4.
       //Commit of B must be placed in between 3, 4, according to Android's drawings
     public Errors commit(FileVersion ver, int commitTime, String data) {
       Checkout commit = checkouts.remove(ver);
       if (commit == null)
         return Errors.VersionNotFound;
 
       if (checkouts.containsKey(commit.version.nextVersion())) {
         checkouts.put(commit.version, commit);
         throw new IllegalStateException("Can't commit; next version already committed!");
       }
 
       checkouts.add(commit);
     }
 
     public Errors cancel(FileVersion ver) {
       Checkout c = checkouts.remove(ver);
       if (c == null) 
         return Errors.NoTransaction;
     }
   }
 
 
   // Temp filename used by the server to preserve atomic writes.
   public static final String tempFileName = ".temp";
 
   // Map of all files that have ever had a live transaction since server start.
   private HashMap<DFSFilename, FileState> files;
 
   // Map of all the live transactions.
  private HashMap<Integer, TransactionMessage> transactions;
   
   private HashMap<DFSFilename, OwnershipRequestWrapper> transferOwnerRequests;
   private HashMap<DFSFilename, LinkedList<SyncRequestWrapper>> readRequests;
   private HashMap<DFSFilename, Set<Integer>> readCheckOuts;
 
   // Server's master cache. The backing store for all data.
   private PersistentStorageCache cache;
 
   /**
    * Contains all state needed by the server to handle ongoing SyncRequests.
    */
   public static class SyncRequestWrapper {
     int originator;
     SyncRequestMessage request;
 
     public SyncRequestWrapper(int originator, SyncRequestMessage request) {
       this.originator = originator;
       this.request = request;
     }
   }
 
 
   /**
    * Contains all state needed by the server to handle ownership transfer requests.
    */
   public static class OwnershipRequestWrapper extends SyncRequestWrapper {
     int desiredOwner;
     Set<Integer> outstandingReaders;
 
     public OwnershipRequestWrapper(int originator, SyncRequestMessage request,
                                    int desiredOwner) {
       super(originator, request);
       this.desiredOwner = desiredOwner;
     }
   }
 
 
   /**
    * Starts the DFSServer. This method will trigger a cleanup of the filesystem
    * layout by instantiating a PersistentStorageCache.
    *
    * TODO(andrew): Handle any dirty files.
    */
   public DFSServer(DFSNode parent) {
     super(parent);
 
     System.err.println("Starting the DFSServer...");
 
     readRequests = new HashMap<DFSFilename, LinkedList<SyncRequestWrapper>>();
     transferOwnerRequests = new HashMap<DFSFilename, OwnershipRequestWrapper>();
     readCheckOuts = new HashMap<DFSFilename, Set<Integer>>();
 
     cache = new PersistentStorageCache(parent, this, true);
   }
 
 
   /**
    * General message handler.
    *
    * @param from Sender of the message
    * @param msg The message
    */
   @Override
   public void onReceive(Integer from, DFSMessage msg) {
     System.err.println("Server (addr " + this.addr + ") received: " +
                        msg.toString());
 
     // Demux on the message type and dispatch accordingly.
     switch (msg.getMessageType()) {
     case Transaction:
       handleTransaction(from, (TransactionMessage) msg);
       break;
     case SyncRequest:
       handleSyncRequest(from, (SyncRequestMessage) msg);
       break;
     case SyncData:
       handleSyncData(from, (SyncDataMessage) msg);
       break;
     case AckData:
       handleAckData(from, (AckDataMessage) msg);
       break;
     case Response:
       handleResponse(from, (ResponseMessage) msg);
       break;
     default:
     }
   }
 
   /**
    * Handles Transaction messages. These deal with starting, aborting, and committing
    * transactions.
    *
    *
    * @param from Sender of the message
    * @param msg The message
    */
   private void handleTransaction(int from, TransactionMessage msg) {
     // Clients don't ever confirm anything. We have the authority around here...
     if (msg.isSet(TransactionMessage.CONFIRM))
       return;
 
     if (msg.isSet(TransactionMessage.START)) {
       startTransaction(msg.getTxId());
     } else if (msg.isSet(TransactionMessage.ABORT)) {
       abortTransaction(msg.getTxId());
     } else if (msg.isSet(TransactionMessage.COMMIT)) {
       commitTransaction(msg.getTxId());
     }
 
     // Nothing to do, drop packet on floor.
   }
 
   /**
    * Handles AckData messages. An AckData can trigger the following response:
    *  - An attempt to continue a transfer-of-ownership. The client may have the file
    *    checked out for read, in which case that client is removed from the list of
    *    outstanding clients on the request. The request is completed if no more
    *    clients have yet to respond.
    *
    * @param from Sender of the message
    * @param msg The message
    */
   private void handleAckData(Integer from, AckDataMessage msg) {
     DFSFilename filename;
     try {
       filename = new DFSFilename(msg.getFileName());
     } catch (IllegalArgumentException e) {
       return;
     }
 
     PersistentStorageCache.CacheEntry entry = cache.get(filename);
     if (entry.getState() == PersistentStorageCache.CacheState.INVALID) {
       System.err.println("WARN: Server got AckData for an invalid cache entry...");
       return;
     }
 
     OwnershipRequestWrapper req = transferOwnerRequests.get(filename);
     if (req == null) {
       System.err.println("WARN: Server got AckData for an unknown ownership transfer...");
       return;
     }
 
     if (req != null &&
         req.outstandingReaders != null &&
         req.outstandingReaders.contains(from) &&
         req.desiredOwner != from) {
       req.outstandingReaders.remove(from);
       if (req.outstandingReaders.size() == 0)
         sendOwnershipToClient(req);
     }
 
     if (req.desiredOwner == from) {
       cache.set(filename,
                 entry.getVersion(),
                 entry.getData(),
                 from,
                 entry.exists());
       transferOwnerRequests.remove(filename);
     }
   }
 
 
   /**
    * Handles SyncData messages. These arrive in the following cases:
    *  - A client is trying to read a file that is owned by someone else
    *  - A client is trying to take ownership of a file.
    *
    * If the file is owned by someone else, the SyncRequest is effectively
    * forwarded to the owner. Any read occurs at the time the request
    * is processed by the owner. If the request is a transfer-of-ownership
    * request, all clients that have a copy in their cache will be
    * notified before the request is satisfied.
    *
    * @param from Sender of the message
    * @param msg The message
    */
   private void handleSyncData(Integer from, SyncDataMessage msg) {
     DFSFilename filename;
     try {
       filename = new DFSFilename(msg.getFileName());
     } catch (IllegalArgumentException e) {
       return;
     }
 
     PersistentStorageCache.CacheEntry entry = cache.get(filename);
     if (entry.getOwner() == from &&
         (entry.getOwner() == msg.getOwner() ||
          (msg.getOwner() == PersistentStorageCache.kNotOwned &&
           msg.getFlags().isSet(SyncFlags.TransferOwnership)))) {
       if (entry.getVersion().compareTo(msg.getVersion()) < 0) {
         cache.set(entry.getKey(),
                   msg.getVersion(),
                   msg.getData(),
                   entry.getOwner(),
                   entry.exists());
       }
 
       if (msg.getFlags().isSet(SyncFlags.TransferOwnership)) {
         transferOwnerToServer(filename);
         OwnershipRequestWrapper rq = transferOwnerRequests.get(filename);
         if (rq != null) {
           if (invalidateOwnership(rq))
             sendOwnershipToClient(rq);
         }
       }
 
       AckDataMessage response = new AckDataMessage(filename.toString(),
                                                    entry.getVersion());
       System.err.println("Server (addr " + this.addr + ") sending to " +
                          from + ": " + response.toString());
       RIOSend(from, Protocol.DATA, response.pack());
     }
   }
 
 
   /**
    * Handle a ResponseMessage. The server receives this message in the
    * following cases:
    *  - The server sent a SyncData message to inform the client of an
    *    ownership transfer. The client encountered an error while
    *    processing the message.
    *  - The server sent a SyncRequest message to a client to request an
    *    ownership transfer. The client refused the message or encountered
    *    an error.
    *
    * In all cases, the client is considered at fault and loses any data
    * or ongoing request.
    *
    * @param from Sender of the message
    * @param msg The message
    */
   private void handleResponse(Integer from, ResponseMessage msg) {
     DFSFilename filename;
     try {
       filename = new DFSFilename(msg.getFileName());
     } catch (IllegalArgumentException e) {
       return;
     }
 
     {
       transferOwnerToServer(filename);
       OwnershipRequestWrapper req = transferOwnerRequests.get(filename);
       if (req != null) {
         System.err.println("Error on ORW: " + msg.getCode());
         if (req.desiredOwner == from) {
           // sent message was a SyncDataMessage
           transferOwnerRequests.remove(filename);
         }
         sendOwnershipToClient(req);
       } // Other case is we went a SyncRequestMessage; handled below.
     }
 
     {
       List<SyncRequestWrapper> req = readRequests.get(filename);
       if (req != null) {
         System.err.println("Finalizing sync requests: " + req);
         // Invalidate from's ownership of the file, complete sync requests.
         transferOwnerToServer(filename);
         satisfySyncRequests(filename, req);
         readRequests.remove(filename);
       }
     }
   }
 
 
   /**
    * Responds positively (i.e. with a SyncData request) to all given
    * SyncRequests, using the cache to fill out fields.
    *
    * @param file File name to respond for.
    * @param reqs Requests to respond to.
    */
   private void satisfySyncRequests(DFSFilename file,
                                    List<SyncRequestWrapper> reqs) {
     SyncDataMessage msg = buildSyncDataMessage(file);
     Flags f = new Flags(msg.getFlags());
     for (SyncRequestWrapper req : reqs) {
       msg.getFlags().setFrom(f);
       copyAppropriateSyncRequestFlags(msg, req.request);
       RIOSend(req.originator, Protocol.DATA, msg.pack());
     }
   }
 
 
   /** Handles a SyncRequestMessage. The following actions can be taken:
    *   - If the message has Create set, responds immediately as appropriate.
    *   - If the message is a transfer-ownership request, responds immediately
    *     if possible, else sends an appropriate transfer-ownership request to
    *     the current owner.
    *   - Else the message is a standard sync request. Adds the sender to the
    *     set of readers on the requested file. Responds with a SyncData message
    *     if the file is not currently owned, else forwards the request to the
    *     current owner to request the latest copy.
    *
    * @param from address of requester
    * @param msg the message itself
    */
   private void handleSyncRequest(Integer from, SyncRequestMessage msg) {
     DFSFilename filename;
     try {
       filename = new DFSFilename(msg.getFileName());
     } catch (IllegalArgumentException e) {
       return;
     }
 
     Flags f = msg.getFlags();
 
     // Create is a special case; handle separately.
     if (f.isSet(SyncFlags.Create)) {
       Errors result = createFile(from, filename, msg);
       DFSMessage reply = null;
 
       switch (result) {
       case Success:
       case Delayed:
         break;
       default:
         reply = new ResponseMessage(filename.toString(), result.getId());
         break;
       }
 
       if (reply != null) {
         System.err.println("Server (addr " + this.addr + ") sending to " +
                            from + ": " + reply.toString());
         RIOSend(from, Protocol.DATA, reply.pack());
       }
 
       return;
     }
 
     PersistentStorageCache.CacheEntry entry = cache.get(filename);
     if (entry.getState() == PersistentStorageCache.CacheState.INVALID) {
       RIOSend(from, Protocol.DATA, new ResponseMessage(filename.toString(),
                                                        Errors.FileDoesNotExist.getId()).pack());
       return;
     }
 
     if (f.isSet(SyncFlags.TransferOwnership)) {
       if (transferOwnerRequests.containsKey(filename) &&
           transferOwnerRequests.get(filename).desiredOwner == from)
         return; // drop dupe request.
 
       if (!transferOwnership(filename, msg, from)) {
         ResponseMessage response = new ResponseMessage(filename.toString(),
                                                        Errors.OwnershipConflict.getId());
         System.err.println("Server (addr " + this.addr + ") sending to " +
                            from + ": " + response.toString());
         RIOSend(from, Protocol.DATA, response.pack());
       }
 
       return;
     }
 
     if (entry.getOwner() != PersistentStorageCache.kNotOwned) {
       SyncRequestWrapper req = new SyncRequestWrapper(entry.getVersion().getOwner(),
                                                       msg);
       enqueueSyncRequest(filename, req);
       return;
     }
 
     checkOutForRead(from, filename);
     SyncDataMessage sdMsg = buildSyncDataMessage(filename);
     copyAppropriateSyncRequestFlags(sdMsg, msg);
 
     System.err.println("Server (addr " + this.addr + ") sending to " +
                          from + ": " + sdMsg.toString());
     RIOSend(from, Protocol.DATA, sdMsg.pack());
   }
 
 
   /**
    * Build a SyncData message from the current contents in the cache.
    *
    * @param f File to build the SyncDataMessage for.
    */
   private SyncDataMessage buildSyncDataMessage(DFSFilename f) {
     PersistentStorageCache.CacheEntry entry = cache.get(f);
     return new SyncDataMessage(f.toString(),
                                entry.getVersion(),
                                new Flags(),
                                entry.getOwner(),
                                entry.exists(),
                                entry.getData());
   }
 
 
   /**
    * Set any flags on msg as appropriate for the given request. Does not
    * currently clear any flags.
    *
    * Currenty this really only sets ReadOnly if it is present in req.
    *
    * @param msg Response to set flags on
    * @param req Request.
    */
   private void copyAppropriateSyncRequestFlags(SyncDataMessage msg,
                                                SyncRequestMessage req) {
     Flags f = req.getFlags();
 
     if (f.isSet(SyncFlags.ReadOnly))
       msg.getFlags().set(SyncFlags.ReadOnly);
   }
 
 
   /**
    * Helper method to add the given client to the list of clients that
    * have the given file checked out.
    *
    * @param client Client to add
    * @param filename File.
    */
   private void checkOutForRead(Integer client, DFSFilename filename) {
     //add to readCheckOuts
     Set<Integer> s = readCheckOuts.get(filename);
     if (s == null) {
       s = new HashSet<Integer>();
       readCheckOuts.put(filename, s);
     }
 
     s.add(client);
   }
 
 
   /**
    * Helper method to remove the given client from the list of clients that
    * have the given file checked out.
    *
    * @param client Client to add
    * @param filename File.
    */
   private void unCheckOutForRead(Integer client, String filename) {
     if (readCheckOuts.get(filename) == null)
       return;
 
     readCheckOuts.get(filename).remove(client);
   }
 
   /**
    * Starts a transaction with the given transaction ID, or returns
    * an error code.
    */
   private void startTransaction(TransactionId id) {
     Errors error = Errors.Unknown;
 
     try {
       if (transactions.containsKey(id)) {
         error = Errors.DuplicateTransaction;
         return;
       }
 
       transactions.put(id, new Transaction(id));
       error = Errors.Success;
 
     } finally {
       RIOSend(id.getClient(), 
               Protocol.DATA, 
               new ResponseMessage("", error));
     }
   }
 
   private void abortTransaction(TransactionId id) {
     Errors error = doAbortTransaction(id);
     
     if (error != null)
       RIOSend(id.getClient(),
               Protocol.DATA,
               new ResponseMessage("", error));
   }
 
   private void doAbortTransaction(TransactionId id) {
     if (!transactions.containsKey(id)) {
       return Errors.NoTransaction;
     }
 
     Transaction t = transactions.remove(id);
     if (t == null)
       return Errors.NoTransaction;
 
     // Step 1. Delete all checked-out versions in the transaction.
     for (DFSFilename f : t.checkouts) {
       FileState state = files.get(f);
       if (state == null)
         return Errors.Unknown;
 
       state.cancel(t.checkouts.get(f).version);
     }
 
     // Step 2. Adjust any commit windows as needed.
 
     return Errors.Success;
   }
 
   private void commitTransaction(TransactionId id) {
     Errors error = Errors.Unknown;
     
     try {
       if (!transactions.containsKey(id)) {
         error = Errors.NoTransaction;
         return;
       }
 
       Transaction t = transactions.get(id);
 
       // Step 1. Ensure txn can be committed.
       VersionRange validCommitTimes = t.getValidCommitTimes();
       if (validCommitTimes == null) {
         abortTransaction(id);
         error = null;
         return;
       }
 
       // Step 2. Pick commit time
       // TODO(andrew): If we were really being picky, we'd do some type
       // of in-use histogram and pick the commit time least-likely to cause
       // a conflict. For now, we pick the latest finite time and hope for
       // the best!
       int commitTime;
       if (validCommitTimes.getEnd().time == VersionRange.kInfinite) {
         if (validCommitTimes.getStart().time == VersionRange.kInfinite)
           commitTime = nextValidCommitTime;
         else
           commitTime = validCommitTimes.getStart().time;
       } else {
         commitTime = validCommitTimes.getEnd().time - 1;
       }
 
       // Step 3. Log intent to commit.
       log.startCommit(id, commitTime);
 
       // Step 4. Commit all files, send invalidation requests
       for (DFSFilename f : t.checkouts.keySet()) {
         Checkout c = t.checkouts.get(f);
         FileState state = files.get(f);
         if (state == null) {
           doAbortTransaction(id);
           return;
         }
 
         state.commit(c.version, commitTime)
         if (!state.isLatestVersion(c.version))
           // TODO Fix once we refactor invalidateOwnership!
           ;
       }
 
       // Step 5. Log commit completion
       log.finishCommit(id, commitTime);
       
       // Step 6. Remove transaction
       transactions.remove(id);
       error = Errors.Success;
     } finally {
       if (error != null)
         RIOSend(id.getClient(),
                 Protocol.DATA,
                 new ResponseMessage("", error));
     }
   }
 
 
   /**
    * Creates the given file in the cache, if it is currently unowned and
    * legal to do so. If not, it returns an appropriate error code from the
    * Errors enum, which should be transmitted to the requesting client.
    * If it is currently owned, initiates a transfer-of-ownership to the
    * server.
    */
   private Errors createFile(int requestor, DFSFilename file,
                             SyncRequestMessage msg) {
     System.err.println("createFile");
     if (cache.getState(file) != PersistentStorageCache.CacheState.INVALID) {
       System.err.println("Not invalid");
       if (cache.get(file).getOwner() != PersistentStorageCache.kNotOwned) {
         System.err.println("Queueing ORW");
         transferOwnership(file, msg, requestor);
         return Errors.Delayed;
       }
     }
 
     return handleCreateOnServerOwnedFile(requestor, file, msg);
   }
 
 
   /**
    * Creates a file in the cache. The file must be owned by the server or
    * it is an error to call this method. If the client requests ownership
    * of the file, initiates a transfer-of-ownership of the file to the
    * client (notifies all readers).
    *
    * @param requester Id of the client that requested the file creation.
    * @param file File name to create.
    * @param msg SyncRequestMessage that prompted the create.
    */
   private Errors handleCreateOnServerOwnedFile(int requestor, DFSFilename file,
                                                SyncRequestMessage msg) {
     if (cache.getState(file) != PersistentStorageCache.CacheState.INVALID &&
         cache.get(file).exists())
       return Errors.FileAlreadyExists;
 
     cache.create(file);
 
     if (msg.getFlags().isSet(SyncFlags.TransferOwnership)) {
       transferOwnership(file, msg, requestor);
       return Errors.Success;
     }
 
     SyncDataMessage sdMsg = buildSyncDataMessage(file);
     copyAppropriateSyncRequestFlags(sdMsg, msg);
     RIOSend(requestor, Protocol.DATA, sdMsg.pack());
     return Errors.Success;
   }
 
 
   /**
    * Adds a SyncRequest to the queue of ongoing sync requests. Sends a
    * SyncRequestMessage to the owner of the file, if no message is in-flight.
    *
    * @param filename Name of the file to enqueue the request for.
    * @param req SyncRequest to enqueue.
    */
   private void enqueueSyncRequest(DFSFilename filename, SyncRequestWrapper req) {
     PersistentStorageCache.CacheEntry entry = cache.get(filename);
 
     // TODO(andrew): This maybe shouldn't be here...needs looking.
     if (entry.getOwner() == PersistentStorageCache.kNotOwned)
       throw new IllegalStateException("Shouldn't be doing this with a not-owned entry");
 
     LinkedList<SyncRequestWrapper> reqs = readRequests.get(filename);
     if (reqs == null) {
       reqs = new LinkedList<SyncRequestWrapper>();
       readRequests.put(filename, reqs);
     }
 
     SyncRequestMessage srMsg = new SyncRequestMessage(filename.toString(),
                                                       entry.getVersion(),
                                                       new Flags());
     if (reqs.size() == 0) {
       System.err.println("Server (addr " + this.addr + ") sending to " +
                          entry.getOwner() + ": " + srMsg.toString());
       RIOSend(entry.getOwner(), Protocol.DATA, srMsg.pack());
     }
 
     reqs.add(req);
   }
 
 
   /**
    * Sends the appropriate SyncRequest + TransferOwnership to the client that is
    * needed to satisfy the given transfer-of-ownership request.
    *
    * @param request Transfer-of-Ownership request that needs to be satisfied.
    */
   private boolean stealFromOwner(OwnershipRequestWrapper request) {
     System.err.println("Steal ownership");
     Flags f = new Flags();
     f.set(SyncFlags.TransferOwnership);
     SyncRequestMessage req = new SyncRequestMessage(request.request.getFileName(),
                                                     request.request.getVersion(),
                                                     f);
     System.err.println("Server (addr " + this.addr + ") sending to " +
                        request.desiredOwner + ": " + req.toString());
     RIOSend(request.desiredOwner, Protocol.DATA, req.pack(), newDeliveryCBInstance());
     return true;
   }
 
 
   /**
    * Set the given transfer-of-ownership request as _the_ ongoing transfer-of-
    * ownership for the given file. There can be only one ongoing transfer at a
    * time.
    *
    * @return true if the request was successfully recorded, false if another
    *      request was currently ongoing.
    */
   private boolean queueOwnershipRequestWrapper(DFSFilename file,
                                                OwnershipRequestWrapper wrap) {
     OwnershipRequestWrapper req = transferOwnerRequests.get(file);
     if (req != null)
       return false;
 
     System.err.println("Insert ORW: " + file + " -> " + wrap);
     transferOwnerRequests.put(file, wrap);
 
     return true;
   }
 
 
   /**
    * Initiate a transfer-of-ownership. You should call this function any time
    * you are initializing a transfer of ownership; it will cause the correct
    * sequence of callbacks to be called.
    *
    * Do not call this to transfer ownership to the server; this is the one
    * exception. Instead, see transferOwnerToServer, though this method does
    * not send appropriate network messages.
    *
    * @param file File to operate on.
    * @param msg Message that caused the transfer of ownership
    * @param newOwner Desired owner of the file.
    * @return true if the transfer was underway, false if an existing transfer
    *     was active for that file.
    */
   private boolean transferOwnership(DFSFilename file, 
                                     SyncRequestMessage msg,
                                     int newOwner) {
     OwnershipRequestWrapper req = new OwnershipRequestWrapper(newOwner, msg,
                                                               newOwner);
     if (!queueOwnershipRequestWrapper(file, req))
       return false;
 
     PersistentStorageCache.CacheEntry entry = cache.get(file);
     System.err.println("Do xfer ownership on " + entry);
 
     if (cache.get(file).getOwner() == PersistentStorageCache.kNotOwned) {
       System.err.println("Not owned");
       if (invalidateOwnership(req))
         sendOwnershipToClient(req);
     } else if (!stealFromOwner(req)) {
       return false;
     }
 
     return true;
   }
 
 
   /**
    * Send a SyncData message to the client with TransferOwnership
    * set. The transfer will be fully completed when an AckData
    * message is received from the client.
    *
    * @param req Ownership request to finish.
    */
   private void sendOwnershipToClient(OwnershipRequestWrapper req) {
     DFSFilename file = new DFSFilename(req.request.getFileName());
     PersistentStorageCache.CacheEntry entry = cache.get(file);
     if (req.desiredOwner == PersistentStorageCache.kNotOwned) {
       transferOwnerToServer(file);
       return;
     }
 
     cache.set(entry.getKey(), entry.getVersion(), entry.getData(),
               req.desiredOwner, entry.exists());
 
     SyncDataMessage msg = buildSyncDataMessage(file);
     msg.getFlags().set(SyncFlags.TransferOwnership);
     RIOSend(req.desiredOwner, Protocol.DATA, msg.pack());
     System.err.println("Sending SyncData, ORW = " + transferOwnerRequests.get(file));
   }
 
 
   /**
    * Transfer ownership to the server. Does not send any network messages;
    * simply marks the transfer in the cache and returns.
    *
    * @param filename File to transfer.
    */
   private void transferOwnerToServer(DFSFilename filename) {
     PersistentStorageCache.CacheEntry entry = cache.get(filename);
     if (entry.getState().equals(PersistentStorageCache.CacheState.INVALID))
       throw new IllegalStateException("Should not get here; server hasn't heard of " + filename);
 
     cache.set(filename,
               entry.getVersion(),
               entry.getData(),
               PersistentStorageCache.kNotOwned,
               entry.exists());
   }
 
 
   /**
    * Sends required messages to clients so that they know the ownership has been
    * changed on a file.
    *
    * @param req transfer-of-ownership request.
    * @return true if no invalidation messages needed to be sent, and the transfer
    *      can proceed; false otherwise.
    */
   private boolean invalidateOwnership(OwnershipRequestWrapper req) {
     DFSFilename file = new DFSFilename(req.request.getFileName());
     PersistentStorageCache.CacheEntry entry = cache.get(file);
     Set<Integer> checkouts = readCheckOuts.get(file);
     if (checkouts != null) {
       HashSet<Integer> outstandingReaders = new HashSet<Integer>(checkouts);
       outstandingReaders.remove(req.desiredOwner);
       Iterator<Integer> it = checkouts.iterator();
 
       while (it.hasNext()) {
         int reader = it.next();
         if (reader == req.desiredOwner)
           continue;
 
         SyncDataMessage sdMsg = new SyncDataMessage(file.toString(),
                                                     entry.getVersion(),
                                                     new Flags(), entry.getOwner(),
                                                     entry.exists(), null);
         System.err.println("Server (addr " + this.addr + ") sending to " +
                            reader + ": " + sdMsg.toString());
         RIOSend(reader, Protocol.DATA, sdMsg.pack());
       }
 
       if (outstandingReaders.size() != 0)
         req.outstandingReaders = outstandingReaders;
 
       return outstandingReaders.size() == 0;
     }
 
     return true;
   }
 
 
   /**
    * TODO
    */
   private Callback newDeliveryCBInstance() {
     try {
       String[] args = new String[]{ "java.lang.Integer", "java.lang.Exception" };
       Method m = Callback.getMethod("networkDeliveryCallback", this, args);
       return new Callback(m, this, new Object[]{ null, null });
     } catch (NoSuchMethodException nsme) {
       assert(false) : "Should never get here.";
       nsme.printStackTrace();
       return null;
     } catch (ClassNotFoundException cnfe) {
       assert(false) : "Should never get here.";
       cnfe.printStackTrace();
       return null;
     }
   }
 
 
   /**
    * TODO
    */
   public void networkDeliveryCallback(Integer seqNum, Exception e) {
     // TODO bitches
   }
 
 }
