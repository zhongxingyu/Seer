 package main;
 import java.io.*;
 import java.util.*;
 
 import com.thetransactioncompany.jsonrpc2.*;
 
 /**
  *  This class takes care of initiating partition transfers
  *  One server is the master that takes care of reconfiguring
  */
 
 public class PartitionUpdater implements Runnable {
 
     private Server server;
     private ServerAddress thisSA;
     private CommunicationQ queue;
     private ReconfigState reconfigState;
     private boolean isMaster;
 
     private HashMap<Integer, ServerAddress> servers;
     private PartitionTable table;
 
     public PartitionUpdater(Server server, CommunicationQ queue) {
         this.server = server;
         this.queue = queue;
         reconfigState = server.getReconfigState();
         this.isMaster = server.isMaster();
 	this.servers = server.getAllServers();
 	thisSA = server.getAddress();
 	table = server.getPartitionTable();
     }
 
     // run the reconfiguration algorithm
     // v.1: get the heaviest edge for now
     public HashMap<Integer, ServerAddress> configAlgo(HashMap<Pair, Integer> input) {
 	int max = 0;
 	Pair max_pair = null;
 	Iterator it = input.entrySet().iterator();
 
 	while (it.hasNext()) {
 	    Map.Entry kv = (Map.Entry) it.next();
 	    int temp = ((Integer) kv.getValue()).intValue();
 	    if (max < temp) {
 		max = temp;
 		max_pair = (Pair) kv.getKey();
 	    }
 	}
 
 	//System.out.println("Input: " + input + " --> " + max_pair);
 
 	HashMap<Integer, ServerAddress> ret = new HashMap<Integer, ServerAddress>();
 	if (max_pair != null) {
 
 	    Integer p1 = max_pair.p1;
 	    Integer p2 = max_pair.p2;
 
 	    ServerAddress s1 = table.psTable.get(p1);
 	    ServerAddress s2 = table.psTable.get(p2);
 
 	    if (!s1.equals(s2)) {
 		// look for a random partition from s1 to transfer to s2
 		ArrayList<Integer> l1 = table.spTable.get(s1);
 		Integer p3 = null;
 		for (int j = 0; j < l1.size(); j++) {
 		    if (!l1.get(j).equals(p1)) {
 			p3 = l1.get(j);
 			break;
 		    }
 		}
 
 		ret.put(p2, s1);
 		if (p3 != null) {
 		    // should always execute, we're assuming
 		    // # partitions > # servers
 		    ret.put(p3, s2);
 		}
 		return ret;
 	    }
 	}
 
 	return null;
     }
 
     public void changeLocalConfig(HashMap<Integer, ServerAddress> changes) {
 	this.server.setReconfigState(ReconfigState.CHANGE);
 	
 	System.out.println("Wait for all txns to stop");
 	
 	// Wait for all of the current transactions to commit/abort
 	while (this.server.getNumWorkers() > 0) {
 	    try {
 		Thread.sleep(50);
 	    } catch (InterruptedException e) {
 		System.err.println("Thread interrupted");
 	    }
 	}
 
 	System.out.println("Send/receive partitions");
 
 	// Send/receive all partitions
 	HashMap<Integer, ServerAddress> send = new HashMap<Integer, ServerAddress>();
 	HashMap<Integer, ServerAddress> receive = new HashMap<Integer, ServerAddress>();
 	Iterator it = changes.entrySet().iterator();
 	while (it.hasNext()) {
 	    Map.Entry entry = (Map.Entry) it.next();
 	    Integer partition = (Integer) entry.getKey();
 	    ServerAddress sa = (ServerAddress) entry.getValue();
 	    
 	    if (sa.equals(thisSA)) {
 		// needs to receive a partition
 		receive.put(partition, table.getServer(partition));
 	    } else if (table.getServer(partition).equals(thisSA)) {
 		// needs to send a partition
 		receive.put(partition, sa);
 	    }
 	}
 
 	while (!receive.isEmpty() && !send.isEmpty()) {
 	    
 	    if (!send.isEmpty()) {
 		Iterator send_it = send.entrySet().iterator();
 		while (send_it.hasNext()) {
 		    Map.Entry entry = (Map.Entry) send_it.next();
 		    Integer partition = (Integer) entry.getKey();
 		    ServerAddress sendSA = (ServerAddress) entry.getValue();
 		    HashMap<String, Object> args = new HashMap<String, Object>();
 		    args.put("Method", "Send");
 		    args.put("Partition", this.server.getPartitionData(partition.intValue()));
 		    args.put("Partition Number", partition);
 		    RPCRequest sendReq = new RPCRequest("reconfigure", thisSA, new TransactionId(thisSA, -1), args);
 		    RPC.send(sendSA, "reconfigure", "001", sendReq.toJSONObject());
 		}
 	    }
 
 	    while (!receive.isEmpty()) {
 		
 		Object obj = queue.get();
 		if (obj.equals("")) {
 		    continue;
 		}
 
 		RPCRequest receiveReq = (RPCRequest) obj;
 		HashMap<String, Object> args = (HashMap<String, Object>) receiveReq.args;
 		
 		if (args.get("Method").equals("Send")) {
 		    this.server.addPartitionData(((Long) args.get("Partition Number")).intValue(), (HashMap<String, String>) args.get("Partition"));
 		}
 		receive.remove((Integer) args.get("Partition Number"));
 	    }
 	}
 
 	// Change partition table
	System.out.println(this.server.getAddress());
	System.out.println("Before");
	System.out.println(this.server.getPartitionTable());
 	it = changes.entrySet().iterator();
 	while (it.hasNext()) {
 	    Map.Entry entry = (Map.Entry) it.next();
 	    this.server.getPartitionTable().addPartition(((Integer) entry.getKey()).intValue(), (ServerAddress) entry.getValue());
 	}
	System.out.println("After");
	System.out.println(this.server.getPartitionTable());
 
 	// Change server state
 	this.server.setReconfigState(ReconfigState.READY);
     }
 
     // master reconfiguration
     public void configureMaster() {
 
         // periodically contact all ther servers for AF information
 
 	while (true) {
 
 	    //System.out.println("Master: start ");
 	    
 	    Iterator it = servers.entrySet().iterator();
 	    HashSet<ServerAddress> waitAddresses = new HashSet<ServerAddress>();
 	    while (it.hasNext()) {
 		Map.Entry kv = (Map.Entry) it.next();
 		if (!kv.getValue().equals(thisSA)) {
 		    waitAddresses.add((ServerAddress) kv.getValue());
 		    HashMap<String, Object> args = new HashMap<String, Object>();
 		    args.put("Method", "getAF");
 		    RPCRequest req = new RPCRequest("reconfigure", thisSA, new TransactionId(thisSA, -1), args);
 		    RPC.send((ServerAddress) kv.getValue(), "reconfigure", "001", req.toJSONObject());
 		}
 	    }
 
 	    HashMap<Pair, Integer> newAFTable = new HashMap<Pair, Integer>();
 	    
 	    while (!waitAddresses.isEmpty()) {
 		Object obj = queue.get();
 		if (obj.equals("")) {
 		    continue;
 		}
 
 		RPCRequest reqIn = (RPCRequest) obj;
 		HashMap<String, Object> replyArgs = (HashMap<String, Object>) reqIn.args;
 		if (replyArgs.get("Method").equals("getAF-reply")) {
 		    Iterator ra_it = replyArgs.entrySet().iterator();
 		    while (ra_it.hasNext()) {
 			Map.Entry entry = (Map.Entry) ra_it.next();
 			if (!entry.getKey().equals("Method")) {
 			    HashMap<String, Object> value = (HashMap<String, Object>) entry.getValue();
 			    // aggregate the edge weights together
 			    Pair newPair = new Pair( new Integer(((Long) value.get("p1")).intValue()), new Integer(((Long) value.get("p2")).intValue()));
 			    Integer i = newAFTable.get(newPair);
 			    if (i == null) {
 				newAFTable.put(newPair, new Integer(((Long) value.get("af")).intValue()));
 			    } else {
 				newAFTable.put(newPair, new Integer(((Long) value.get("p1")).intValue()) + i);
 			    }
 			}
 		    }
 		    waitAddresses.remove(reqIn.replyAddress);
 		}
 	    }
 
 	    //System.out.println("Master: received all AF info ");
 	    
 	    HashMap<Integer, ServerAddress> newTable = this.configAlgo(newAFTable);
 
 	    if (newTable != null && !newTable.isEmpty()) {
 
 		HashMap<String, Object> args = new HashMap<String, Object>();
 		Iterator newTable_it = newTable.entrySet().iterator();
 		while (newTable_it.hasNext()) {
 		    Map.Entry kv = (Map.Entry) newTable_it.next();
 		    ServerAddress sa = (ServerAddress) kv.getValue();
 		    HashMap<String, Object> temp = new HashMap<String, Object>();
 		    temp.put("Number", sa.getServerNumber());
 		    temp.put("Name", sa.getServerName());
 		    temp.put("Port", sa.getServerPort());
 		    args.put(((Integer) kv.getKey()).toString(), temp);
 		}
 
 		args.put("Method", "changeConfig");
 		
 		// a new configuration is available, send to all of the servers
 		while (it.hasNext()) {
 		    Map.Entry kv = (Map.Entry) it.next();
 		    if (!kv.getValue().equals(thisSA)) {
 			waitAddresses.add((ServerAddress) kv.getValue());
 			RPCRequest req = new RPCRequest("reconfigure", thisSA, new TransactionId(thisSA, -1), args);
 			RPC.send((ServerAddress) kv.getValue(), "reconfigure", "001", req.toJSONObject());
 		    }
 		}
 
 		this.changeLocalConfig(newTable);
 
 		// TODO: should not start the next reconfiguration until everyone has reconfigured
 		
 		it = servers.entrySet().iterator();
 		waitAddresses.clear();
 		while (it.hasNext()) {
 		    Map.Entry entry = (Map.Entry) it.next();
 		    if (!thisSA.equals(entry.getValue())) {
 			waitAddresses.add((ServerAddress) entry.getValue());
 		    }
 		}
 
 		while (!waitAddresses.isEmpty()) {
 			
 		    Object obj = queue.get();
 		    if (obj.equals("")) {
 			continue;
 		    }
 
 		    RPCRequest doneReq = (RPCRequest) obj;
 		    if (doneReq.method.equals("changeConfig-done")) {
 			waitAddresses.remove(doneReq.replyAddress);
 		    }
 
 		}
 	    }
 
 	    try {
 		Thread.sleep(2000);
 	    } catch (InterruptedException e) {
 
 	    }
 	}
 
     }
 
     // worker reconfiguration
     public void configureWorker() {
 
         while (true) {
 
             Object obj = queue.get();
             if (obj.equals("")) {
                 continue;
             }
 
             RPCRequest reqIn = (RPCRequest) obj;
 	    HashMap<String, Object> args = (HashMap<String, Object>) reqIn.args;
 	    if (args.get("Method").equals("getAF")) {
 		//System.out.println("Worker: received AF request");
 		AFTable af = this.server.getAF();
 		HashMap<String, Object> reply_args = af.toJSONObject();
 		reply_args.put("Method", "getAF-reply");
 		RPCRequest retReq = new RPCRequest("reconfigure", thisSA, reqIn.tid, reply_args);
 		RPC.send(reqIn.replyAddress, "reconfigure", "001", retReq.toJSONObject());
 
 	    } else if (args.get("Method").equals("changeConfig")) {
 		// figure out the new configuration
 		args.remove("Method");
 
 		HashMap<Integer, ServerAddress> changes = new HashMap<Integer, ServerAddress>();
 		Iterator args_it = args.entrySet().iterator();
 		while (args_it.hasNext()) {
 		    Map.Entry entry = (Map.Entry) args_it.next();
 		    Integer partition = new Integer((String) entry.getKey());
 		    HashMap<String, Object> temp = (HashMap<String, Object>) entry.getValue();
 		    int number = ((Long) temp.get("Number")).intValue();
 		    String name = (String) temp.get("Name");
 		    int port = ((Long) temp.get("Port")).intValue();
 		    ServerAddress sa = new ServerAddress(number, name, port);
 		    changes.put(partition, sa);
 		}
 
 		this.changeLocalConfig(changes);
 		
 		// send response back to master
 		HashMap<String, Object> doneArgs = new HashMap<String, Object>();
 		doneArgs.put("Method", "changeConfig-done");
 		RPCRequest doneReq = new RPCRequest("reconfigure", thisSA, reqIn.tid, doneArgs);
 		RPC.send(reqIn.replyAddress, "reconfigure", "001", doneReq.toJSONObject());
 	    }
         }
     }
 
     public void run() {
 
 	if (this.isMaster) {
 	    this.configureMaster();
 	} else {
 	    this.configureWorker();
 	}
 	
     }
 
 }
