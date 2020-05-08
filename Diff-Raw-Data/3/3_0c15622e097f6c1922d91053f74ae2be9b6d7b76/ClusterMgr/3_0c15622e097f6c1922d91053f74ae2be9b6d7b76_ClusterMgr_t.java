 package aether.cluster;
 
 import aether.conf.ConfigMgr;
 import aether.net.ControlMessage;
 import aether.net.Message;
 import aether.net.NetMgr;
 import java.io.IOException;
 import java.net.InetAddress;
 import java.net.SocketException;
 import java.net.SocketTimeoutException;
 import java.net.UnknownHostException;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.logging.ConsoleHandler;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.logging.SimpleFormatter;
 
 
 /**
  * Cluster manager manages the cluster related activities for this node. 
  * For example, joining the cluster, responding to discovery messages,
  * helping other nodes to join the cluster, updating the cluster member table
  * if someone leaves the cluster.
  * There can be only one cluster manager per node.
  * 
  * @author aniket oak
  */
 public class ClusterMgr implements Runnable {
     
     private static boolean isOne = false;
     private ClusterTable clusterTable;
     private int clusterPort;
     private NetMgr comm;
     private int nodeId;
     private int nodeIdCounter;
     private HashMap<Integer,ClusterTableRecord> tempRecs = new HashMap<>();
     private boolean dbg;
     private static final Logger log = 
             Logger.getLogger(ClusterMgr.class.getName());
     private int runLevel = 0;
     private static ClusterMgr instance;
     
     
     
     static {
         log.setLevel(Level.INFO);
         ConsoleHandler handler = new ConsoleHandler();
         handler.setFormatter(new SimpleFormatter());
         handler.setLevel(Level.ALL);
         log.addHandler(handler);
     }
     
     /**
      * The ClusterMgr is a singleton. This constructor should not be used to 
      * get an object. Use the method getInstance() instead
      * 
      * @throws UnsupportedOperationException if one clusterMgr is already
      *          running
      * @throws SocketException if clusterMgr fails to get hold of its socket
      */
     public ClusterMgr () throws UnsupportedOperationException, SocketException {
         
         if (isOne) {
             log.warning("Cluster manager is already running");
         } else {
             nodeIdCounter = 0;
             clusterTable = new ClusterTable();
             init();
             isOne = true;
             if (ConfigMgr.getIfDebug()) {
                 log.info("Setting log level to fine");
                 log.setLevel(Level.FINE);
             }
         }
     }
 
     
     
     /** Initialize the things clusterMgr will need
      * @throws SocketException 
      */
     private void init () throws SocketException {
         clusterPort = ConfigMgr.getClusterPort();
         comm = new NetMgr (clusterPort);
         comm.setTimeout(5000);
         log.fine("Initialized ClusterMgr");
     }
     
     
     
     /**
      * Get the instance of the ClusterMgr. This method should be used to get
      * the object of the clusterMgr instead of the constructor
      * @return
      * @throws UnsupportedOperationException
      * @throws SocketException 
      */
     public static ClusterMgr getInstance () throws 
             UnsupportedOperationException, SocketException {
         
         if (instance == null) {
            instance = new ClusterMgr();
            return instance;
         } else {
             return instance;
         }
     }
     
     
     /**
      * Print cluster table in user readable format.
      */
     private void printClusterView () {
         clusterTable.printTable();
     }
     
     
     
     
     /**
      * Broadcast the discovery message in the cluster for the purpose of joining
      * the cluster
      * @throws IOException 
      */
     private void broadcastDiscovery () throws IOException {
         
         log.fine("Broadcasting discovery message 'd'");
         InetAddress bAddr = NetMgr.getBroadcastAddr();
         
         if (bAddr == null) {
             log.severe("Unable to get broadcast addr");
         }
         
         ControlMessage discovery = new ControlMessage ('d', bAddr);
         comm.send(discovery);
         runLevel = 1;
         log.fine("Setting the run level to 1");
     }
     
     
     
     /**
      * Process the control message of subtype 'r' indicating reception of a 
      * response to discovery message
      * @param r     Discovery response message
      */
     private void processDiscoveryResponse (Message r) {
         /* Not implemented yet. Here we need to processes the discovery
          * response and initiate cluster joining mechanism.
          */
         if (runLevel > 1) {
             return;
         }
         log.fine("Processing the discovery response 'r'");
         
         int contactId = r.getSourceId();
         InetAddress contactIp = r.getSourceIp();
         
         log.log(Level.FINER, "Contact node IP: {0}, node Id: {1}", 
                 new Object[]{contactIp.toString(), contactId});
         
         /* Send the 'I-want-to-join' message to the contact node
          * Then wait for its response
          */
         ControlMessage d2 = new ControlMessage ('m', contactIp);
         try {
             log.fine("Sending membership request 'm'");
             comm.send(d2);
         } catch (IOException ex) {
             log.severe("Sending membership request failed");
             ex.printStackTrace();
         }
         
         runLevel = 2;
         log.fine("Setting the run level to 2");
     }
     
     
 
     
     /**
      * Process the control message of subtype 'j' indicating we are included
      * in the cluster.
      * @param j     Control message of subtype 'j'
      * @throws IllegalStateException when parsing the cluster table records 
      *      fails because either IP address could not be found or wrong message
      *      was parsed for the records. ClusterMgr cannot continue with bad
      *      cluster table.
      */
     private void processJoinMessage (Message j) throws IllegalStateException {
         
         if (runLevel > 2) {
             return;
         }
         
         log.fine("Processing join (admittance) message 'j'");
         
         ControlMessage join = (ControlMessage) j;
         nodeId = join.getDestId();
         ConfigMgr.setNodeId(nodeId);
         
         log.log(Level.FINE, "My node id:{0}", nodeId);
         
         /* Now we need to update our cluster table by parsing the data in
          * the payload of the control message.
          */
         ClusterTableRecord[] recs;
         try {
             recs = join.parseJControl();
             
         } catch (UnknownHostException ex) {
             /* Unknown host means parsing of the IP address failed. We could not
              * resolve the IP string to get the InetAddress. There is no way to
              * recover from this
              */
             log.severe("UnknownHostException while parsing IP String");
             throw new IllegalStateException();
             
         } catch (UnsupportedOperationException ex) {
             
             log.warning("Attempt to parse non-join message in wrong manner");
             return;
         }
         
         for (ClusterTableRecord rec:recs) {
             clusterTable.insertRecord(rec);
         }
         
         printClusterView();
         runLevel = 3;
         log.fine("Setting the run level to 3");
     }
     
     
     
     
     
     
     /**
      * Process the discovery message by some node.
      * @param d     Discovery message by the new node.
      */
     private void processDiscovery (Message d) {
         
         if (runLevel < 3) {
             return;
         }
         
         log.fine("Processing discovery message by new node 'd'");
         
         try {
             ControlMessage disc = (ControlMessage) d;
             InetAddress newNodeIp = disc.getSourceIp();
             
             log.log(Level.FINER, "New node IP: {0}", newNodeIp.toString());
             ControlMessage r = new ControlMessage('r', newNodeIp);
             log.fine("Sending discovery response 'r'");
             comm.send((Message)r);
             
         } catch (IOException ex) {
             log.warning("Could not send discovery response");
         }
     }
     
     
     
     
     
     
     
     /**
      * Process join request response from the other nodes in the cluster.
      * @param m Control message- reply to the join request from contact node
      * @return  true if reply is 'accept'
      */
     private boolean processJoinResponseMessage (Message m) {
         
         log.fine("Processing join request reply from the cluster 'a'");
         ControlMessage reply = (ControlMessage) m;
         
         if (reply == null || reply.getMessageSubtype() != 'a') {
             log.fine("Reply from cluster: Failed");
             return false;
         }
         if (reply.parseAControl().equalsIgnoreCase("accept")) {
             log.fine("Reply from cluster: Passed");
             return true;
         }
         log.fine("Reply from cluster: Failed");
         return false;
     }
     
     
     
     
     
     
     
     
     /**
      * Process the membership request from a new node and respond to it with
      * a join message if successful
      * @param d     Membership request message
      */
     private void processMembershipRequest (Message d) {
         
         if (runLevel < 3) {
             return;
         }
         
         log.fine("Processing membership request from the new node 'm'");
   
         ControlMessage mReq = (ControlMessage) d;
         // find total nodes in the cluster
         int numNodes = clusterTable.getNumRecords() - 1;
         int numAttempts = ConfigMgr.getNumJoinAttempts();
         
         boolean success = false;
         ClusterTableRecord tempNodeRec = null;
         Integer tempNodeId = null;
         
         
         try {
 
             while (numAttempts > 0 && success == false) {
 
                 
                 tempNodeId = ++nodeIdCounter;
                 log.log(Level.FINER, "Attempt {0}, Temp node id {1}", 
                         new Object[]{numAttempts, tempNodeId});
                 tempNodeRec = new ClusterTableRecord (
                         tempNodeId, mReq.getSourceIp());
                 
                 String payload = tempNodeRec.toDelimitedString();
 
                 log.fine("Sending membership proposal 'p'");
                 ControlMessage joinInfo = new ControlMessage('p',
                         NetMgr.getBroadcastAddr(), payload);
 
                 comm.send((Message) joinInfo);
 
                 int replyCount = 0;
                 LinkedList<Message> replyList = new LinkedList<>();
 
                 for (int i = 0; i < numNodes; i++) {
 
                     try {
                         Message reply = comm.receive();
                         if (processJoinResponseMessage(reply)) {
                             log.fine("Received positive reply from cluster");
                             replyList.add(reply);
                         }
                         
                     } catch (SocketTimeoutException soe) {
                         log.fine("Timeout waiting for reply");
                         break;
                         // nothing to do here. This means we missed a reply
                     }
                 }
 
                 if (replyList.size() == numNodes) {
                     success = true;
                 }
 
                 numAttempts--;
             }
 
             if (success) {
                 /* First we need to tell all the nodes that they can commit
                  * the new node in the cluster table
                  */
                 if (tempNodeRec != null && tempNodeId != null) {
                     
                     if (numNodes > 0) {
                         String load = tempNodeId.toString();
                         ControlMessage commit = new ControlMessage('c',
                                 NetMgr.getBroadcastAddr(), load);
 
                         log.fine("Sending commit message 'c'");
                         comm.send(commit);
                     }
                     
                     clusterTable.insertRecord(tempNodeRec);
                     printClusterView();
                 }
                 
                 
                 
                 /* Now we need to tell the new node that it has been admitted
                  * in the cluster
                  */
                 String joinPayload = prepareJoinMessagePayload();
                 ControlMessage joinMessage = new ControlMessage('j',
                         mReq.getSourceIp(), tempNodeId, joinPayload);
                 log.fine("Sending join message 'j' to the new node");
                 comm.send(joinMessage);
             }
             
         } catch (IOException ioe) {
             log.warning("Could not process membership request 'm'");
             ioe.printStackTrace();
         }
         
 
     }
     
     
     
     
     
     
     /**
      * Process the membership proposal from a node in the cluster
      * @param m     Membership proposal control message
      */
     private void processMembershipProposal (Message m) {
         
         if (runLevel < 3) {
             return;
         }
         
         log.fine("Processing membership proposal 'p'");
         ControlMessage p = (ControlMessage) m;
         String response;
         ClusterTableRecord tempRec = null;
         
         try {
             tempRec = p.parsePControl();
             if (tempRec == null) {
                 response = "deny";
             } else if (clusterTable.exists(tempRec.getNodeId())) {
                 response = "deny";
             } else {
                 response = "accept";
             }
             
         } catch (UnknownHostException e) {
             response = "deny";
         }
         
         log.log(Level.FINE, "Sending reply {0} 'a'", response);
         ControlMessage reply = new ControlMessage ('a', p.getSourceIp(),
                 response);
         
         try {
             comm.send(reply);
         } catch (IOException e) {
             // do nothing. The contact node will timeout and try again.
             log.warning("Sending membership reply 'a' failed");
             return;
         }
         
         if (tempRec != null && response.equalsIgnoreCase("accept")) {
             tempRecs.put(tempRec.getNodeId(), tempRec);
         }
         
     }
     
     
     
     
     
     /**
      * Process the membership commit message
      * @param m     Membership commit control message
      */
     private void processMembershipCommit (Message m) {
         
         if (runLevel < 3) {
             return;
         }
         
         log.fine("Processing membership commit 'c'");
         
         ControlMessage c = (ControlMessage) m;
         Integer newNodeId = c.parseCControl();
         
         ClusterTableRecord toInsert = tempRecs.get(newNodeId);
         if (toInsert == null) {
             // something went wrong
             log.warning("Could not find matching temp record for commit");
         } else {
             clusterTable.insertRecord(toInsert);
             tempRecs.remove(newNodeId);
             printClusterView();
         }
         
     }
     
     
     
     
 
     /**
      * This is a method which takes a message and calls an appropriate method
      * to handle that kind of message.
      * @param m     Message to be processed
      * @throws IllegalStateException when parsing of critical control message 
      *      fails
      */
     private void processMessage (Message m) throws IllegalStateException {
         
         log.fine("Processing control message");
         ControlMessage ctrl = (ControlMessage) m;
         char ctrlType = ctrl.getMessageSubtype();
         
         
         switch (ctrlType) {
             
             case 'r': /* discovery response */
                         processDiscoveryResponse(m);
                         break;
                 
             case 'j': /* cluster membership message */
                         processJoinMessage(m);
                         break;
                 
             case 'd': /* Someone wants to join the cluster */
                         processDiscovery(m);
                         break;
             
             case 'm': /* We need to act as a contact node for someone */
                         processMembershipRequest(m);
                         break;
             
             case 'a': /* This should not be called from here */
                         break;
             
             case 'p': /* A membership proposal was received */
                         processMembershipProposal(m);
                         break;
            
             case 'c': /* A membership commit */
                         processMembershipCommit(m);
                         break;
         }
     }
     
     
     
     
     
     
     
     
     
     
     
     
     
     
     
     
     
     /**
      * Prepare the payload for join message having all the records in the 
      * cluster table in character form.
      * @return  char array having all the records in cluster table
      */
     public String prepareJoinMessagePayload () {
         
         log.fine("Preparing to send the cluster table");
         ClusterTableRecord[] tableRecs = clusterTable.getAllRecords();
         String[] recStrings = new String[tableRecs.length];
         
         int i=0;
         for (ClusterTableRecord r: tableRecs) {
             recStrings[i++] = r.toDelimitedString();
         }
         
         String s = "";
         for (String recStr: recStrings) {
             s = recStr + ";" + s;
         }
         
         return s;
     }
     
     
     
     
     /**
      * Initialize the cluster table assuming we are the first one up in the
      * cluster.
      */
     private void initTable () {
         
         nodeId = ++nodeIdCounter;
         ClusterTableRecord myRecord = new ClusterTableRecord (nodeId, 
                 ConfigMgr.getLocalIp());
         clusterTable.insertRecord(myRecord);
         ConfigMgr.setNodeId(nodeId);
         log.fine("Initialized new cluster table");
         printClusterView();
         runLevel = 3;
         log.fine("Setting the run level to 3");
     }
     
     
     
   
     
     @Override
     public void run() {
         
         /* First thing we are supposed to do is to broadcast a discovery message
          * to the cluster.
          */
         
         log.info("Starting cluster manager");
         
         try {
             broadcastDiscovery();
             Message discoveryResponse = comm.receive();
             
             
             /* If we are here, that means someone is already up and running, or
              * there was another discovery message broadcasted. This will be 
              * complicated. Will need to think through the solution to implement
              * simultanious braodcasts. For now, just implement the solution 
              * where no simultanious discovery broadcast happen
              */
             
             
             processMessage (discoveryResponse);
             
         } catch (SocketTimeoutException to) {
             /* Looks like I am the first one up. Initialize the table.
              */
             initTable();
         } catch (IOException ex) {
             log.severe("ClusterMgr exiting as the cluster discovery failed");
             ex.printStackTrace();
             return;
         } catch (IllegalStateException badState) {
             log.severe("ClusterMgr exiting due to inconsistent state");
             return;
         }
         
         
         /* We are here means our table is initialized and we are up and running.
          * Now keep on listening to the socket for any incoming messages
          */
         
         while (true) {
             Message m;
             try {
                 m = comm.receive();
                 processMessage (m);
             } catch (SocketTimeoutException to) {
                 log.finer("Socket timeout");
                 // nothing needs to be done here
             } catch (IOException ex) {
                 log.severe("IOException while listening on socket");
                 ex.printStackTrace();
                 return;
             }
             
         }
     }
     
     
     
     
 }
