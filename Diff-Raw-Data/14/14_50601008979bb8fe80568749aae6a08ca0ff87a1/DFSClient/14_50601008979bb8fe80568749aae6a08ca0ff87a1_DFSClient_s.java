 import java.lang.reflect.Method;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 import edu.washington.cs.cse490h.lib.Callback;
 import edu.washington.cs.cse490h.lib.PersistentStorageReader;
 import edu.washington.cs.cse490h.lib.PersistentStorageWriter;
 import edu.washington.cs.cse490h.lib.Utility;
 
 
 public class DFSClient extends DFSComponent {
 
   private HashMap<Integer, DFSFilename> outstandingSyncRequests;
   private HashMap<Integer, DFSFilename> outstandingOwnershipRequests;
   private HashMap<DFSFilename, LinkedList<CommandWrapper>> outstandingCommands;
   private PersistentStorageCache cache;
   private int serverAddr;
 
 
   /**
    * Wrapper for saving command state while we wait for server response.
    */
   private static class CommandWrapper {
     public enum CommandType { Create, Get, Put, Append, Delete; }
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
     outstandingCommands = new HashMap<DFSFilename, LinkedList<CommandWrapper>>();
     cache = new PersistentStorageCache(client, this, true);
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
 
   public void cancelSyncRequests(DFSFilename file) {
     LinkedList<CommandWrapper> reqs = outstandingCommands.get(file);
     if (reqs == null)
       return;
 
     Iterator<CommandWrapper> it = reqs.iterator();
     while (it.hasNext()) {
       switch (it.next().type) {
       case Get:
         it.remove();
         break;
       }
     }
   }
 
   public void cancelOwnershipRequests(DFSFilename file) {
     LinkedList<CommandWrapper> reqs = outstandingCommands.get(file);
     if (reqs == null)
       return;
 
     Iterator<CommandWrapper> it = reqs.iterator();
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
         cancelSyncRequests(file);
     } else {
       file = outstandingOwnershipRequests.remove(seqno);
       if (file != null && e != null)
         cancelOwnershipRequests(file);
     }
   }
 
   /**
    * Store a blocked command to be executed upon response from the server.
    *
    * @param type
    *            Type of command.
    *
    * @param fname
    *            Name of file involved in command.
    *
    * @param cb
    *           Callback to be fired upon command success.
    */
   private void addOutstandingCommand(CommandWrapper.CommandType type,
                                      DFSFilename fname, Callback cb) {
     LinkedList<CommandWrapper> reqList = outstandingCommands.get(fname);
 
     if (reqList == null) {
       reqList = new LinkedList<CommandWrapper>();
       outstandingCommands.put(fname, reqList);
     }
     reqList.add(new CommandWrapper(type, fname, cb));
   }
 
 
   /**
    * Issue a request to execute a command on the server.
    *
    * @param msg
    *            Message to send to the server.
    *
    * @param userCompleteCb
    *            Callback to fire on success.
    */
   private void issueCommand(CommandWrapper.CommandType type, DFSMessage msg, Callback userCompleteCb) {
     // Create a network delivery callback.
     Method method;
     Callback cb;
     try {
       String[] paramTypes = { "java.lang.Integer",
                               "NetworkExceptions$NetworkException" };
       method = Callback.getMethod("networkDeliveryCb", this, paramTypes);
       cb = new Callback(method, this, new Object[]{ msg, serverAddr });
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
 
     // Pack and send the message.
     byte[] packedMsg = msg.pack();
     System.err.println("Client (addr " + this.addr + ") sending: " +
                        msg.toString());
     addOutstandingCommand(type, ((FileNameMessage) msg).getDFSFileName(), cb);
     int seqno = RIOSend(serverAddr, Protocol.DATA, packedMsg, cb);
     if (msg instanceof SyncRequestMessage &&
         ((SyncRequestMessage) msg).getFlags().isSet(SyncFlags.TransferOwnership))
       outstandingOwnershipRequests.put(seqno, new DFSFilename(((FileNameMessage) msg).getFileName()));
     else
       outstandingSyncRequests.put(seqno, new DFSFilename(((FileNameMessage) msg).getFileName()));
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
     SyncRequestMessage msg = new SyncRequestMessage(filename, 0, flags);
     issueCommand(CommandWrapper.CommandType.Create, msg, cb);
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
       SyncRequestMessage msg = new SyncRequestMessage(filename, 0, flags);
       issueCommand(CommandWrapper.CommandType.Get, msg, cb);
     } else {
       // File is cached, so we fire callback with its contents.
       // TODO: callback exception sig: cb(Exception e, String data);
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
     // First check if we have the file cached and read-only.
     PersistentStorageCache.CacheEntry cachedFile = cache.get(new DFSFilename(filename));
 
     switch (cachedFile.getState()) {
     case INVALID:
     case READ_ONLY:
       // If file isn't cached or is read-only, then try to get ownership.
       Flags flags = new Flags();
       flags.set(SyncFlags.TransferOwnership);
       SyncRequestMessage msg = new SyncRequestMessage(filename, 0, flags);
       issueCommand(CommandWrapper.CommandType.Put, msg, cb);
       break;
     case READ_WRITE:
       // We have ownership, so write new contents and fire callback.
       // TODO: callback exception sig: cb(Exception e, String data);
       cache.update(new DFSFilename(filename), contents);
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
     // First check if we have the file cached and read-only.
     PersistentStorageCache.CacheEntry cachedFile = cache.get(new DFSFilename(filename));
 
     switch (cachedFile.getState()) {
     case INVALID:
     case READ_ONLY:
       // If file isn't cached or is read-only, then try to get ownership.
       Flags flags = new Flags();
       flags.set(SyncFlags.TransferOwnership);
       SyncRequestMessage msg = new SyncRequestMessage(filename, 0, flags);
       issueCommand(CommandWrapper.CommandType.Append, msg, cb);
       break;
     case READ_WRITE:
       // We have ownership, so write new contents and fire callback.
       // TODO: callback exception sig: cb(Exception e, String data);
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
     // First check if we have the file cached and read-only.
     PersistentStorageCache.CacheEntry cachedFile = cache.get(new DFSFilename(filename));
 
     switch (cachedFile.getState()) {
     case INVALID:
     case READ_ONLY:
       // If file isn't cached or is read-only, then try to get ownership.
       Flags flags = new Flags();
       flags.set(SyncFlags.TransferOwnership);
       SyncRequestMessage msg = new SyncRequestMessage(filename, 0, flags);
       issueCommand(CommandWrapper.CommandType.Delete, msg, cb);
       break;
     case READ_WRITE:
       // We have ownership, so delete file.
       // TODO: callback exception sig: cb(Exception e, String data);
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
   public void onReceive(Integer from, FileNameMessage msg) {
     System.err.println("Client (addr " + this.addr + ") received: " +
                        msg.toString());
 
     SyncRequestMessage srMsg = (SyncRequestMessage) msg;
     Flags flags = srMsg.getFlags();
     DFSFilename fname = srMsg.getDFSFileName();
     PersistentStorageCache.CacheEntry cachedItem = cache.get(fname);
     FileNameMessage response = null;
     Flags responseFlags = new Flags();
 
     switch (msg.getMessageType()) {
     case SyncRequest:
       if (flags.isSet(SyncFlags.TransferOwnership)) {
         responseFlags.set(SyncFlags.TransferOwnership);
         cachedItem.setOwner(cache.kNotOwned);
         cachedItem.setState(PersistentStorageCache.CacheState.READ_ONLY);
       } else if (flags.isSet(SyncFlags.ReadOnly)) {
         responseFlags.set(SyncFlags.ReadOnly);
       } else {
         System.err.println("Node " + this.addr + ": Error: Invalid flags in " +
                            "message received from server " + from);
         return;
       }
       break;
     case SyncData:
       SyncDataMessage sdMsg = (SyncDataMessage) msg;
 
       if (sdMsg.getData() == null && sdMsg.exists()) {
         // We're receiving an invalidation directive.
         cachedItem.setState(PersistentStorageCache.CacheState.INVALID);
         cachedItem.setOwner(sdMsg.getOwner());
       } else {
         // We're receiving a response associated with an outstanding command,
         // so perform the actual command.
         // TODO: Handle errors!
         LinkedList<CommandWrapper> commandList = outstandingCommands.get(sdMsg.getDFSFileName());
         CommandWrapper command = commandList.remove();
         switch (command.type) {
         case Create:
           System.err.println("Got create msg!");
           cache.set(fname, 1, "", this.addr, true);
           // TODO: What to pass to cb?
          command.cb.setParams(new Object[]{msg.getFileName(), null});
           break;
         case Get:
           cache.set(fname, sdMsg.getVersion(), sdMsg.getData(),
                     sdMsg.getOwner(), true);
          command.cb.setParams(new Object[]{msg.getFileName(), null, sdMsg.getData()});
           break;
         case Put:
           cache.set(fname, sdMsg.getVersion() + 1, command.contents,
                     this.addr, true);
          command.cb.setParams(new Object[]{msg.getFileName(), null});
           break;
         case Append:
           cache.set(fname, sdMsg.getVersion() + 1,
                     sdMsg.getData() + command.contents, this.addr, true);
          command.cb.setParams(new Object[]{msg.getFileName(), null});
           break;
         case Delete:
           cache.delete(fname);
          command.cb.setParams(new Object[]{msg.getFileName(), null});
           break;
         }
 
         try {
           command.cb.invoke();
         } catch (Exception e) {
           System.err.println("An exception occurred while trying to call the " +
                              "callback for a request to " + srMsg.getFileName() +
                              ":");
           e.printStackTrace();
           return;
         }
       }
 
       // Need to ack iff owner != kNotOwned.
       if (sdMsg.getOwner() != cache.kNotOwned) {
         response = new AckDataMessage(msg.getFileName(), sdMsg.getVersion());
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
       response = new SyncDataMessage(msg.getFileName(),
                                      cachedItem.getVersion(),
                                      responseFlags,
                                      cachedItem.getOwner(),
                                      cachedItem.exists(),
                                      cachedItem.getData());
     }
 
     // TODO: Send the response, build a cb.
     byte[] packedResponse = response.pack();
     RIOSend(from, Protocol.DATA, packedResponse, null);
   }
 
 }
