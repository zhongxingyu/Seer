 package main;
 import java.io.*;
 import java.util.*;
 import java.util.Map.Entry;
 
 /**
  * This class implements a worker that is spawned every time a transaction
  * starts at a server Currently uses SS2PL, 2PC Assuming no network packet
  * failures!
  */
 
 public class Worker implements Runnable {
 
     private Server server;
     private CommunicationQ queue;
     private TransactionContext txn;
     private HashMap<String, String> readSet;
     private HashMap<Integer, HashMap<String, String> > writeSet;
     private TransactionId txnId;
 
     // local locks held
     private HashSet<String> readLocked;
     private HashSet<String> writeLocked;
 
     // for coordinator
     private HashSet<ServerAddress> cohorts;
 
     // thread state
     private boolean done;
 
     // for reconfiguration
     private ArrayList<Integer> partitions;
 
     public Worker(Server server, CommunicationQ queue) {
         this.server = server;
         this.queue = queue;
         txn = null;
         //willCommit = false;
         cohorts = new HashSet<ServerAddress>();
         done = false;
 
 	this.writeLocked = new HashSet<String>();
 	this.readLocked = new HashSet<String>();
 	this.cohorts = new HashSet<ServerAddress>();
 	
 	this.readSet = new HashMap<String, String>();
 	this.writeSet = new HashMap<Integer, HashMap<String, String> >();
 	
 	this.partitions = new ArrayList<Integer>();
     }
 
     public void startTransaction(RPCRequest rpcReq) {
         TransactionContext txnContext = new TransactionContext(rpcReq.tid, (HashMap<String, Object>) rpcReq.args);
 
 	//System.out.println("Start transaction " + rpcReq.tid.getTID());
 	txnId = rpcReq.tid;
 
         if (txn == null) {
             txn = txnContext;
         }
 
         TransactionId tid = rpcReq.tid;
 
         Iterator<String> write_set_it = txnContext.write_set.keySet()
                 .iterator();
         Iterator<String> read_set_it = txnContext.read_set.keySet().iterator();
 
         while (write_set_it.hasNext()) {
             String key = (String) write_set_it.next();
             int partNum = this.server.hashKey(key);
 	    ServerAddress sendSA = this.server.getPartitionTable().getServer(partNum);
 
             // if coordinator
             if (this.server.getAddress().equals(tid.getServerAddress())) {
 		partitions.add(new Integer(partNum));
 	        if (!sendSA.equals(this.server.getAddress())) {
 		    cohorts.add(sendSA);
 		}
             }
 
             if (sendSA.equals(this.server.getAddress())) {
                 this.server.lockW(key, tid);
                 writeLocked.add(key);
 		Integer part = new Integer(partNum);
 		HashMap<String, String> temp = writeSet.get(part);
 		if (temp == null) {
 		    temp = new HashMap<String, String>();
 		}
 		temp.put(key, (String) txnContext.write_set.get(key));
 		writeSet.put(part, temp);
 	    }
 	}
 
         while (read_set_it.hasNext()) {
             String key = (String) read_set_it.next();
             int partNum = this.server.hashKey(key);
 	    ServerAddress sendSA = this.server.getPartitionTable().getServer(partNum);
 	    
             // if coordinator
             if (this.server.getAddress().equals(tid.getServerAddress())) {
 		partitions.add(new Integer(partNum));
 		if (!sendSA.equals(this.server.getAddress())) {
 		    cohorts.add(sendSA);
 		}
             }
 
             if (sendSA.equals(this.server.getAddress())) {
                 this.server.lockR(key, tid);
                 String value = this.server.get(partNum, key);
 		if (value != null) {
 		    readSet.put(key, value);
 		} else {
 		    readSet.put(key, "");
 		}
 		readLocked.add(key);
             }
         }
 
         // TODO: this can definitely be optimized
         if (this.server.getAddress().equals(tid.getServerAddress())) {
             // need to make requests to get all locks from necessary servers
             // go ahead and sends txnContext to all
 
             Iterator<ServerAddress> it = cohorts.iterator();
             HashSet<ServerAddress> waitServers = new HashSet<ServerAddress>();
             ServerAddress thisSA = this.server.getAddress();
 
             while (it.hasNext()) {
                 ServerAddress sa = (ServerAddress) it.next();
                 HashMap<String, Object> args = txnContext.toJSONObject();
                 waitServers.add(sa); 
 
                 // TODO: what if packets are dropped?
                 RPCRequest newReq = new RPCRequest("start", thisSA, tid, args);
                 RPC.send(sa, "start", "001", newReq.toJSONObject());
             }
 
 	    // receive replies from all of the cohorts, reply to client
 
 	    HashSet<ServerAddress> receivedSA = new HashSet<ServerAddress>();
 
             while (!waitServers.isEmpty()) {
                 Object obj = queue.get();
 
                 if (obj.equals("")) {
                     //Thread.sleep(50);
                 	continue;
                 }
 
                 RPCRequest receivedReq = (RPCRequest) obj;
 		if (receivedReq.method.equals("abort-reply")) {
		    //System.out.println("Received abort in startTxn for tid " + rpcReq.tid.getTID());		    
 		    this.cohorts.remove(receivedReq.replyAddress);
 		    this.cohorts = receivedSA;
 		    this.abort(rpcReq);
 		    return ;
 		} else if (receivedReq.method.equals("start-reply")) {
 		    HashMap<String, String> rset = (HashMap<String, String>) ((HashMap<String, Object>) receivedReq.args).get("Read Set");
 		    //System.out.println(receivedReq.method + " -> Read set is " + rset);
 		    Iterator<Entry<String, String>> rit = rset.entrySet().iterator();
 		    while (rit.hasNext()) {
 			Map.Entry<String, String> kv = rit.next();
 			readSet.put(kv.getKey(), kv.getValue());
 		    }
 		    receivedSA.add(receivedReq.replyAddress);
 		}
                 waitServers.remove(receivedReq.replyAddress);
             }
 
 	    // reply to client
 	    HashMap<String, Object> args = new HashMap<String, Object>();
 	    args.put("State", true);
             RPCRequest newReq = new RPCRequest("start-done", thisSA, tid, args);
 	    RPC.send(rpcReq.replyAddress, "start-done", "001", newReq.toJSONObject());
 	    
         } else {
             // reply to original server with read-set information
             ServerAddress thisSA = this.server.getAddress();
             HashMap<String, Object> args = new HashMap<String, Object>();
             args.put("State", "OK");
             args.put("Read Set", readSet);
             RPCRequest newReq = new RPCRequest("start-reply", thisSA, tid, args);
 
             RPC.send(rpcReq.replyAddress, "start-reply", "001", newReq.toJSONObject());
             readSet.clear();
         }
 
     }
 
     // TODO: write to log?
     public void abort(RPCRequest rpcReq) {
         // abort the transaction, release all locks held by the txn
     	//System.out.println("ABORTING");
         Iterator<String> it1 = writeLocked.iterator();
         while (it1.hasNext()) {
             this.server.unlockW((String) it1.next(), rpcReq.tid);
         }
 
         Iterator<String> it2 = readLocked.iterator();
         while (it2.hasNext()) {
             this.server.unlockR((String) it2.next(), rpcReq.tid);
         }
 
         // TODO: reply to replyAddress, exit
         if (this.server.getAddress().equals(rpcReq.tid.getServerAddress())) {
             // sends "abort" to all servers
             HashSet<ServerAddress> waitServers = new HashSet<ServerAddress>();
 	    ServerAddress thisSA = this.server.getAddress();
 
 	    //System.out.println("Server " + thisSA + " cohorts: " + cohorts);
 
             Iterator<ServerAddress> it = cohorts.iterator();
             while (it.hasNext()) {
 
                 HashMap<String, Object> args = new HashMap<String, Object>();
                 RPCRequest newReq = new RPCRequest("abort", thisSA, rpcReq.tid, args);
 
                 ServerAddress sentServer = (ServerAddress) it.next();
                 RPC.send(sentServer, "abort", "001", newReq.toJSONObject());
                 waitServers.add(sentServer);
             }
 
             while (!waitServers.isEmpty()) {
                 Object obj = queue.get();
 
                 if (obj.equals("")) {
                     //Thread.sleep(50);
 		    continue;
                 }
 
                 RPCRequest req = (RPCRequest) obj;
 		if (req.method.equals("abort-reply")) {
 		    //System.out.println("Tid " + rpcReq.tid.getTID() + " abort received");
 		    waitServers.remove(req.replyAddress);
 		}
             }
 
 	    // reply to client
 	    HashMap<String, Object> args = new HashMap<String, Object>();
 	    args.put("State", true);
             RPCRequest newReq = new RPCRequest("abort-done", thisSA, rpcReq.tid, args);
 	    RPC.send(rpcReq.replyAddress, "abort-done", "001", newReq.toJSONObject());
 	    this.done = true;
 
         } else {
             // sends "ack" back to original server
             HashMap<String, Object> args = new HashMap<String, Object>();
             ServerAddress thisSA = this.server.getAddress();
             args.put("State", true);
 
 	    System.out.println("Server " + thisSA + " abort received for tid " + rpcReq.tid.getTID());
 
             RPCRequest newReq = new RPCRequest("abort-reply", thisSA, rpcReq.tid, args);
             RPC.send(rpcReq.replyAddress, "abort-reply", "001", newReq.toJSONObject());
 	    this.done = true;
         }
     }
 
     // TODO: in the future, when there are machine failures/other failures, should
     // have the option to reply "abort" instead of "commit-prepare-done"
     public void commitPrepare(RPCRequest rpcReq) {
 	ServerAddress thisSA = this.server.getAddress();
 	HashMap<String, Object> args = new HashMap<String, Object>();
 	RPCRequest newReq = new RPCRequest("commit-prepare-done", thisSA, rpcReq.tid, args);
 	RPC.send(rpcReq.replyAddress, "commit-prepare-done", "001", newReq.toJSONObject());
     }
 
     // TODO: write to log?
     public void commit(RPCRequest rpcReq) {
 	//System.out.println("Ready to commit");
 	
         // commit the transaction, release all locks held by the txn
         // write everything from write set to data store
         if (this.server.getAddress().equals(rpcReq.tid.getServerAddress())) {
             // sends "commit-prepare" to all servers
 	    ServerAddress thisSA = this.server.getAddress();
 
 	    // Phase 1 - commit prepare
             HashSet<ServerAddress> waitServers = new HashSet<ServerAddress>();
             Iterator<ServerAddress> c_it = cohorts.iterator();
             while (c_it.hasNext()) {
 
                 HashMap<String, Object> args = new HashMap<String, Object>();
                 RPCRequest newReq = new RPCRequest("commit-prepare", thisSA, rpcReq.tid, args);
 
                 ServerAddress sentServer = (ServerAddress) c_it.next();
                 RPC.send(sentServer, "commit-prepare", "001", newReq.toJSONObject());
                 waitServers.add(sentServer);
             }
 
             while (!waitServers.isEmpty()) {
                 Object obj = queue.get();
 
                 if (obj.equals("")) {
                     //Thread.sleep(50);
 		    continue;
                 }
 
                 RPCRequest req = (RPCRequest) obj;
		if (req.method.equals("abort-reply")) {
 		    this.abort(rpcReq);
 		    return ;
 		} else if (req.method.equals("commit-prepare-done")) {
 		    waitServers.remove(req.replyAddress);
 		}
             }
 
 	    // If it has reached this point, the transaction has to commit
 	    // Phase 2 - actual commit
 
 	    Iterator it = writeSet.entrySet().iterator();
 	    while (it.hasNext()) {
 		Map.Entry kv = (Map.Entry) it.next();
 		Integer partition = (Integer) kv.getKey();
 		Iterator map_it = ((HashMap<String, String>) kv.getValue()).entrySet().iterator();
 		while (map_it.hasNext()) {
 		    Map.Entry pair = (Map.Entry) map_it.next();
 		    this.server.put(partition, (String) pair.getKey(), (String) pair.getValue());
 		}
 	    }
 
 	    // release all locks
 	    Iterator it1 = writeLocked.iterator();
 	    while (it1.hasNext()) {
 		this.server.unlockW((String) it1.next(), rpcReq.tid);
 	    }
 
 	    Iterator it2 = readLocked.iterator();
 	    while (it2.hasNext()) {
 		this.server.unlockR((String) it2.next(), rpcReq.tid);
 	    }
 
             c_it = cohorts.iterator();
             while (c_it.hasNext()) {
 
                 HashMap<String, Object> args = new HashMap<String, Object>();
                 RPCRequest newReq = new RPCRequest("commit", thisSA, rpcReq.tid, args);
 
                 ServerAddress sentServer = (ServerAddress) c_it.next();
                 RPC.send(sentServer, "commit", "001", newReq.toJSONObject());
                 waitServers.add(sentServer);
             }
 
             while (!waitServers.isEmpty()) {
                 Object obj = queue.get();
 
                 if (obj.equals("")) {
                     //Thread.sleep(50);
 		    continue;
                 }
 
                 RPCRequest req = (RPCRequest) obj;
 		if (req.method.equals("commit-accept")) {
 		    waitServers.remove(req.replyAddress);
 		}
             }
 
 	    // TODO: figure out best way to increment the AF table
 	    if (partitions.size() > 1) {
 		for (int j = 0; j < partitions.size() - 1; j++) {
 		    for (int k = j+1; k < partitions.size(); k++) {
 			this.server.getAF().increment(partitions.get(j), partitions.get(k));
 		    }
 		}
 	    }
 
 	    // reply to client
 	    HashMap<String, Object> args = new HashMap<String, Object>();
 	    args.put("State", true);
 	    args.put("Read Set", readSet);
             RPCRequest newReq = new RPCRequest("commit-done", thisSA, rpcReq.tid, args);
 	    RPC.send(rpcReq.replyAddress, "commit-done", "001", newReq.toJSONObject());
 	    this.done = true;
 
         } else {
 
 	    Iterator it = writeSet.entrySet().iterator();
 	    while (it.hasNext()) {
 		Map.Entry kv = (Map.Entry) it.next();
 		Integer partition = (Integer) kv.getKey();
 		Iterator map_it = ((HashMap<String, String>) kv.getValue()).entrySet().iterator();
 		while (map_it.hasNext()) {
 		    Map.Entry pair = (Map.Entry) map_it.next();
 		    this.server.put(partition, (String) pair.getKey(), (String) pair.getValue());
 		}
 	    }
 
 	    // release all locks
 	    Iterator it1 = writeLocked.iterator();
 	    while (it1.hasNext()) {
 		this.server.unlockW((String) it1.next(), rpcReq.tid);
 	    }
 
 	    Iterator it2 = readLocked.iterator();
 	    while (it2.hasNext()) {
 		this.server.unlockR((String) it2.next(), rpcReq.tid);
 	    }
 
             // sends "ack" back to original server
             HashMap<String, Object> args = new HashMap<String, Object>();
             ServerAddress thisSA = this.server.getAddress();
 
             args.put("State", true);
 	    //args.put("Read Set", read_set);
             RPCRequest newReq = new RPCRequest("commit-accept", thisSA, rpcReq.tid, args);
 
             RPC.send(rpcReq.replyAddress, "commit-accept", "001", newReq.toJSONObject());
 	    this.done = true;
         }
     }
 
     public void receive(RPCRequest req) {
         // received reads from another machine, update readSet
         HashMap<String, String> rcvdSet = (HashMap<String, String>) req.args;
         Iterator<Entry<String, String>> it = rcvdSet.entrySet().iterator();
         while (it.hasNext()) {
             Map.Entry<String, String> kv = it.next();
             this.readSet.put((String) kv.getKey(), (String) kv.getValue());
         }
     }
     
 
     /**
      * Called when a Chandy-Misra-Haas message is received. Determines if a deadlock
      * exists or if not, sends messages to other resources it's waiting for.
      * @param req
      */
     public void cmhDeadlockReceiveMessage(RPCRequest req){
     	CMHProcessor cmhProcessor = new CMHProcessor(req.tid);
     	System.out.println(req.args);
     	HashMap<String, Object> args = (HashMap<String, Object>) req.args;
     	int initiator = ((Long) args.get("initiator")).intValue();
     	int to = ((Long) args.get("to")).intValue();
     	int from = ((Long) args.get("from")).intValue();
     	if (initiator == to){
 	    System.out.println("Deadlock detected");
     	} else {
 	    // continue to send messages
 	    cmhProcessor.propagateMessage(initiator, req.tid, server.getWFG(req.tid));
     	}
     }
     
     public void processcmhMessage(RPCRequest rpcReq){
         if (this.server.getAddress().equals(rpcReq.tid.getServerAddress())) {
 	    //ServerAddress thisSA = this.server.getAddress();
 	    cmhDeadlockReceiveMessage(rpcReq);
         }
     }
 
     public void run() {
 
         while (!done) {
             Object obj = queue.get();
 
             if (obj.equals("")) {
 		continue;
             }
 
             RPCRequest rpcReq = (RPCRequest) obj;
 	    //System.out.println("rpcReq: " + rpcReq.method);
 
             if (rpcReq.method.equals("start")) {
                 this.startTransaction(rpcReq);
             } else if (rpcReq.method.equals("abort")) {
                 this.abort(rpcReq);
             } else if (rpcReq.method.equals("commit-prepare")) {
                 this.commitPrepare(rpcReq);
             } else if (rpcReq.method.equals("commit")) {
 		this.commit(rpcReq);
 	    } else if (rpcReq.method.equals("deadlock")){
             	this.processcmhMessage(rpcReq);
             }
         }
 
 	this.server.threadDone(txnId);
 
     }
 
 }
