 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.HashMap;
 import edu.washington.cs.cse490h.lib.Callback;
 
 public class DFSNode extends RIONode {
 
   // Paxos Boostrap Configuration vars
   static int paxosNetworkSize = -1;
   static int paxosBootstrapNode = -1;
 
   DFSServer server;
   DFSClient client;
   DFSClientAPI clientAPI;
   PaxosNode paxos;
 
   @Override
   public void start() {
     server = new DFSServer(this);
     client = new DFSClient(this, -1);
     server.setClientCb(client.getServerTransactionCb());
     clientAPI = new DFSClientAPI(client);
     startPaxos();
   }
 
   public void startPaxos() {
     if (paxosNetworkSize == -1) {
       System.err.println("Cannot start Paxos: Not bootstrapped. Please bootstrap Paxos.");
       return;
     }
 
     ArrayList<Integer> l = new ArrayList<Integer>(1);
     l.add(paxosBootstrapNode);
     paxos = new PaxosNode(paxosNetworkSize,
                           this,
                           l,
                           createPaxosCb(),
                           createLeaderElectionCb(),
                           createPreProposalCb());
   }
 
   public void paxosInit(int networkSize) {
     if (paxosNetworkSize != -1) {
       System.err.println("Paxos has already been bootstrapped!");
       return;
     }
 
     paxosNetworkSize = networkSize;
     paxosBootstrapNode = addr;
     startPaxos();
   }
 
   /**
    * Transaction Callback.
    *
    * @param id
    *            The Transaction ID.
    * @param status
    *            Status of the transaction identified by id.
    * @param err
    *            Error associated with the transaction, or null
    *            if no error could be identified in particular.
    */
   public void transactionCallback(Exception err,
                                   TransactionId id,
                                   TransactionStatus status) {
     System.err.println("Transaction " + id + " (intended to be " + status + "): " + err);
   }
 
   /**
    * Test user callback.
    *
    * @param txId
    *            Transaction Id associated with the command.
    *
    * @param file
    *            The name of the file targeted by the command.
    *
    * @param e
    *            Exception that occurred, or null if none did.
    *
    * @param data
    *            Data/textual portion of command response.
    */
   public void dataCallback(Exception e,
                            TransactionId txId,
                            DFSFilename file,
                            String data) {
     System.err.print("Data callback for " + file + " (in transaction " + txId + "):" );
     if (e != null) {
       System.err.println("Error:");
       e.printStackTrace();
     } else {
       System.err.println("Found file contents:");
       System.err.println(data);
     }
   }
 
 
   public void nonDataCallback(Exception e,
                               TransactionId txId,
                               DFSFilename file) {
     System.err.println("Results of command on " + file + " for txId " + txId + ":");
     System.err.println(e);
   }
 
   public void paxosCallback(RequestNumber rq, Proposal p, ProposalStatus s) {
     if (s.getId() == ProposalStatus.Accepted.getId()) {
       server.onProposalAccepted(p);
     } else if (s.getId() == ProposalStatus.Failed.getId() &&
                p.getValue() instanceof TransactionProposal &&
                ((TransactionProposal) p.getValue()).getTransaction().getId().getClientId() == addr) {
       client.onTransactionFailed();
     }
 
     System.err.println("paxos callback! " + p + " has been " + s);
   }
 
   /**
    * Routes commands from the user or a file to the proper client method.
    *
    * @param command
    *            String-representation of a DFS command.
    */
   @Override
   public void onCommand(String command) {
     String[] pieces = command.split("\\s");
     String action = pieces[0];
 
     if (action.equals("paxosinit")) {
       if (pieces.length < 2) {
         System.err.println("Specify network size.");
         return;
       }
 
       try {
         int networkSize = Integer.parseInt(pieces[1]);
         paxosInit(networkSize);
       } catch (NumberFormatException e) {
         System.err.println("Invalid number for paxos network size: " + pieces[1]);
       }
       return;
     }
 
     DFSFilename filename;
     try {
       filename = new DFSFilename(pieces[1]);
     } catch (IllegalArgumentException e) {
       System.err.println(e);
       return;
     }
 
     int serverId = filename.getOwningServer();
     if (action.equals("txstart")) {
       clientAPI.startTransaction(createTransactionCb());
       return;
     } else if (action.equals("txcommit")) {
       clientAPI.commitTransaction(createTransactionCb());
       return;
     } else if (action.equals("txabort")) {
       clientAPI.abortTransaction(createTransactionCb());
       return;
     }
 
     String contents = "";
     for (int i = 2; i < pieces.length; i++) {
       contents += pieces[i] + " ";
     }
     contents = contents.trim();
 
     // Reoute the command to the appropriate client method.
     if (action.equals("create")) {
       clientAPI.create(filename, createNonDataCb());
     } else if (action.equals("get")) {
       clientAPI.get(filename, createDataCb());
     } else if (action.equals("put")) {
       clientAPI.put(filename, contents, createNonDataCb());
     } else if (action.equals("append")) {
       clientAPI.append(filename, contents, createNonDataCb());
     } else if (action.equals("delete")) {
       clientAPI.delete(filename, createNonDataCb());
     } else {
       System.err.println("Invalid command");
     }
   }
 
   protected Callback createPaxosCb() {
     try {
       Method method;
       String[] paramTypes = { "RequestNumber", "Proposal", "ProposalStatus" };
       method = Callback.getMethod("paxosCallback", this, paramTypes);
       return new Callback(method, this, null);
     } catch (NoSuchMethodException nsme) {
       assert(false): "Should never get here.";
       nsme.printStackTrace();
       System.exit(10);
       return null;
     } catch (ClassNotFoundException cnfe) {
       assert(false): "Should never get here.";
       cnfe.printStackTrace();
       System.exit(10);
       return null;
     }
   }
 
   protected Callback createPreProposalCb() {
     try {
       Method method;
       String[] paramTypes = { "Proposal", "MutableReference" };
       method = Callback.getMethod("onPreProposalReceive", server, paramTypes);
       return new Callback(method, server, null);
     } catch (NoSuchMethodException nsme) {
       assert(false): "Should never get here.";
       nsme.printStackTrace();
       System.exit(10);
       return null;
     } catch (ClassNotFoundException cnfe) {
       assert(false): "Should never get here.";
       cnfe.printStackTrace();
       System.exit(10);
       return null;
     }
   }
 
   protected Callback createTransactionCb() {
     try {
       Method method;
       String[] paramTypes = { "java.lang.Exception", "TransactionId", "TransactionStatus" };
       method = Callback.getMethod("transactionCallback", this, paramTypes);
       return new Callback(method, this, null);
     } catch (NoSuchMethodException nsme) {
       assert(false): "Should never get here.";
       nsme.printStackTrace();
       System.exit(10);
       return null;
     } catch (ClassNotFoundException cnfe) {
       assert(false): "Should never get here.";
       cnfe.printStackTrace();
       System.exit(10);
       return null;
     }
   }
 
   protected Callback createDataCb() {
     try {
       Method method;
       String[] paramTypes = { "java.lang.Exception",
                               "TransactionId",
                               "DFSFilename",
                               "java.lang.String" };
       method = Callback.getMethod("dataCallback", this, paramTypes);
       return new Callback(method, this, null);
     } catch (NoSuchMethodException nsme) {
       assert(false): "Should never get here.";
       nsme.printStackTrace();
       System.exit(10);
       return null;
     } catch (ClassNotFoundException cnfe) {
       assert(false): "Should never get here.";
       cnfe.printStackTrace();
       System.exit(10);
       return null;
     }
   }
 
   protected Callback createNonDataCb() {
     try {
       Method method;
       String[] paramTypes = { "java.lang.Exception",
                               "TransactionId",
                               "DFSFilename" };
       method = Callback.getMethod("nonDataCallback", this, paramTypes);
       return new Callback(method, this, null);
     } catch (NoSuchMethodException nsme) {
       assert(false): "Should never get here.";
       nsme.printStackTrace();
       System.exit(10);
       return null;
     } catch (ClassNotFoundException cnfe) {
       assert(false): "Should never get here.";
       cnfe.printStackTrace();
       System.exit(10);
       return null;
     }
   }
 
 
   /**
    * Boilerplate to instantiate a callback for designated proposer changes.
    *
    * @return the appropriate Callback object.
    */
   protected Callback createLeaderElectionCb() {
     Method method;
     Callback cb;
     try {
       String[] paramTypes = { "java.lang.Integer", "java.lang.Integer" };
       method = Callback.getMethod("leaderElectionCb", this, paramTypes);
       return new Callback(method, this, null);
     } catch (Exception e) {
       assert(false): "Should never get here.";
       System.err.println("When trying to instantiate designated proposer " +
                          "change cb:");
       e.printStackTrace();
       System.exit(10);
       return null;
     }
   }
 
 
   /**
    * Callback for designated proposer changes.
    *
    * @param proposalNumber
    *            Proposal number associated with the election.
    *
    * @param newDesignatedProposer
    *            The id of the new designated proposer.
    */
   public void leaderElectionCb(Integer proposalNumber,
                                Integer newDesignatedProposer) {
     client.setNewLeader(newDesignatedProposer);
   }
 
 
   /**
    * DFSNode receive method.
    *
    * Called whenever a message is received from the RIO layer. Routes the
    * message to either our server logic or one of our client sessions based
    * on the file name given in the message.
    */
   @Override
   public void onRIOReceive(Integer from, int protocol, byte[] msg) {
     DFSMessage unpacked = DFSMessage.unpack(msg);
 
     // Route the message to the client or server, depending on owning server
     // of target file.
 
     boolean isForServer = false;
 
     if (unpacked instanceof PaxosMessage) {
       paxos.onReceive(from, unpacked);
       return;
     }
 
     if ((unpacked instanceof TransactionMessage)) {
       isForServer = !((TransactionMessage) unpacked).getFlags().isSet(TransactionFlags.Confirm);
     } else {
      isForServer = (unpacked instanceof SyncRequestMessage);
     }
 
     System.err.print("DFSNode:" + addr + ": got pkt for ");
     if (isForServer) {
       server.onReceive(from, unpacked);
     } else {
       clientAPI.client.onReceive(from, unpacked);
     }
   }
 
 
   @Override
   public String packetBytesToString(byte[] bytes) {
     RIOPacket packet = RIOPacket.unpack(bytes);
     if (packet == null)
       return "<corrupt RIOPacket; could not be unpacked>";
     if (packet.getProtocol() != Protocol.DATA)
       return "<non-data packet>";
     DFSMessage unpacked = DFSMessage.unpack(packet.getPayload());
     if (unpacked == null)
       return "<corrupt DFSMessage; could not be unpacked>";
     else
       return unpacked.toString();
   }
 }
