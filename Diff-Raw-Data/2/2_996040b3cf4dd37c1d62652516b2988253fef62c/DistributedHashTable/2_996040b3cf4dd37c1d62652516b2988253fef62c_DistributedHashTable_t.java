 package distributed.hash.table;
 
 import java.rmi.Naming;
 import java.rmi.RemoteException;
 import java.rmi.server.UnicastRemoteObject;
 import java.util.*;
 
 import failure.detector.FailureDetectorThread;
 import failure.detector.ServerJoinEvent;
 import failure.detector.ServerJoinListener;
 
 public class DistributedHashTable extends java.rmi.server.UnicastRemoteObject implements IDistributedHashTable, ServerJoinListener{
 	
 	private static final List<String> STOP_WORDS = new ArrayList<String>(Arrays.asList("a","able","about","across","after","all","almost","also","am","among","an","and",
 			"any","are","as","at","be","because","been","but","by","can","cannot","could","dear","did","do","does","either","else",
 			"ever","every","for","from","get","got","had","has","have","he","her","hers","him","his","how","however","i","if","in",
 			"into","is","it","its","just","least","let","like","likely","may","me","might","most","must","my","neither","no","nor",
 			"not","of","off","often","on","only","or","other","our","own","rather","said","say","says","she","should","since","so",
 			"some","than","that","the","their","them","then","there","these","they","this","tis","to","too","twas","us","wants","was",
 			"we","were","what","when","where","which","while","who","whom","why","will","with","would","yet","you","your"));
 	public static boolean DebugMode;
 	
     private static final long serialVersionUID = 1L;
 	private static final int MaxServersToConnect = 2;
     private Hashtable<String, List<String>> localCache;
     private Hashtable<String, List<String>> dirtyInsertCache; // stores inserted data that is not populated on replication machines
     private Hashtable<String, List<String>> dirtyDeleteCache; // stores deleted data that is not populated on replication machines
     private List<ReplicationStorage> replications;
     private LinkedHashMap<Integer, String> successorTable; // <id, address>
 	Map.Entry<Integer, String> backupSuccessor; // the back up server if a live server failed
     private int myId;
     private int sCount;
 
     /** 
      * Constructor
      */
     public DistributedHashTable(int id, int serverCount, LinkedHashMap<Integer, String> successors) throws java.rmi.RemoteException {
         super(); 
         this.localCache = new Hashtable<String, List<String>>();
         this.dirtyInsertCache =  new Hashtable<String, List<String>>();
         this.dirtyDeleteCache =  new Hashtable<String, List<String>>();
         replications = new ArrayList<ReplicationStorage>();
         this.successorTable = new LinkedHashMap<Integer, String>();
 
         this.myId = id;
         this.sCount = serverCount;
         
         utils.Output.println("DHT server id: " + this.myId + " is created.");
 		int i = 1;
 		// <id, address> address can be name:port. For now assumption is address is just port
 		for (Map.Entry<Integer, String> successor : successors.entrySet()) {
 			if(i++ <= MaxServersToConnect){
 				this.successorTable.put(successor.getKey(), successor.getValue());
 				replications.add(new ReplicationStorage(successor.getKey(), "", Integer.parseInt(successor.getValue())));
 				utils.Output.println(" successor " + successor.getKey()  + " on port " + successor.getValue() + " is added.");
 			}
 			else{
 				utils.Output.println(" backup successor " + successor.getKey()  + " on port " + successor.getValue() + " is added.");
 				this.backupSuccessor = successor;
 			}
 		}
     }
     
     /** 
      * insert an entity on the local hash table if it in the range of this machine,
      * or send the request to the next server that key belongs to if it is not in this server.
      * if next server can not be located, send it to the last server
      */
     public void insert(IInsertDeleteRequest req) throws RemoteException{
     	for(String word: splitWithStopWords(req.getKey().toLowerCase())){
     		word = word.trim();
     		int serverId = getServer(word);
         	String newValue = (String)req.getValue();
         	// this machine is suppose to store the key
             if(serverId == this.myId){
             	// update local copy
                 if(insert(this.localCache, word, newValue)){
                     // this.cache.put(word, values);
                     handleMessage(req, "insert: machine " + this.myId + " - request " + req.getRequestId() + " from machine " + req.getMachineId() + " with <" + word + " , " + req.getValue() + ">  is inserted.\n +++++++ Update Repliction\n");
                 	// update repository servers
                 	doInsertReplication(req, word, false);                    	
                 }
                 else
                 	handleMessage(req, "insert: machine " + this.myId + " - request " + req.getRequestId() + " from machine " + req.getMachineId() + " with <" + word + " , " + req.getValue() + ">  already exists.\n");
             }
             else{
             	// go to the next machine for insert
                 try {                	
                 	Map.Entry<Integer, String> nextMachine = getNextMachine(serverId, word);
 	                if(nextMachine.getKey() == serverId && !FailureDetectorThread.isAlive(serverId)){
 	                	// server is off line, updates the replication machines
 	            		handleMessage(req, "insert: machine " + this.myId + " - server " + serverId + " is failed. Insert replications.\n");
 	            		doInsertReplication(req, word, true);
 	            	}
                 	else{
                     	// create a new request for every single word and send the request to the last server
                 		IInsertDeleteRequest reqNextMachine = new InsertDeleteRequest(req.getRequestId(), req.getMachineId() , word, newValue);
 	                	handleMessage(req, "insert: machine " + this.myId + " - " + reqNextMachine.printRequest() + " routting to machine address " + nextMachine.getValue() + "\n");
 	                	if(DebugMode)
 	                		UnicastRemoteObject.exportObject(reqNextMachine);
 	                    IDistributedHashTable dhtNextMachine = (IDistributedHashTable) 
 	                    		Naming.lookup("rmi://localhost:"+ nextMachine.getValue() +"/DistributedHashTable");
 	                    dhtNextMachine.insert(reqNextMachine);
 	                    // update message received by next machine as original request is not sent to the next machine
 	                    handleMessage(req, reqNextMachine.getMessage());
                 	}
                 }  catch(Exception e) {
                     handleMessage(req, "Error-insert: machine " + this.myId + " - dhtNextMachine: " +  e.getMessage());
                 }
             }
     	}
     }
     
     private boolean insert(Hashtable<String, List<String>> cache, String key, String value){
     	synchronized (cache) {
         	List<String> values = cache.get(key);
             if(values == null){
             	values = new ArrayList<String>();
             	cache.put(key, values);
             }
             if(!values.contains(value)){
             	values.add(value);
             	return true;
             }
 		}
     	return false;
     }
 
     /** 
      * create an insert replication request and send the request to update replication servers
      */
     private void doInsertReplication(IInsertDeleteRequest req, String key, boolean dirtyInsert) throws RemoteException{
     	IInsertDeleteReplicationRequest insRepReq = new InsertDeleteReplicationRequest(req.getRequestId(), req.getMachineId(), key, req.getValue(), dirtyInsert);
     	if(DebugMode)
     		UnicastRemoteObject.exportObject(insRepReq);
     	insertReplication(insRepReq);
         handleMessage(req, insRepReq.getMessage());
     }
 
     /** 
      * insert an entity on the replication hash table if it in the range of this machine,
      * and then send the request to the next server.
      * The request is dropped if it visited all servers in the ring, or both replication servers are updated
      */
 	public void insertReplication(IInsertDeleteReplicationRequest req) throws RemoteException {
 		String key = req.getKey().trim();
 		String value = (String)req.getValue();
     	int serverId = getServer(key);
 		// drop the package, if the request is visited all servers 
 		if(req.getProbe().contains(this.myId)){
 			handleMessage(req, "insertReplication: machine " + this.myId + " - drop package: request " + req.getRequestId() + ". Request visited all servers.\n");
 			// we are the the master server, so both replications are not updated, 
 			// mark the insert as a dirty to be synched with replication servers when they join the ring 
 			if(serverId == this.myId){
 				handleMessage(req, "insertReplication: machine " + this.myId + " - Mark dirty insert : request " + req.getRequestId() + ".\n");
 				insert(this.dirtyInsertCache, key, value);
 			}
 			handleMessage(req, "-------------------\n");
 			return; 
 		}
 
     	synchronized (replications) {
         	for(ReplicationStorage rep: replications){
         		if(serverId == rep.id){
         			// local server contains the replication
         			handleMessage(req, "insertReplication: machine " + this.myId + " - request " + req.getRequestId() + " from machine " + req.getMachineId() + " with <" + key + " , " + req.getValue() + ">  is inserted.\n+++++++\n");
         			req.incrementUpdateCount();
         			rep.insert(key, value, req.isDirty());
         			// drop the package, if both replication servers are updated
         			if(req.getUpdateCount() == MaxServersToConnect){
         				handleMessage(req, "insertReplication: machine " + this.myId + " - drop package: request " + req.getRequestId() + ". Both replication servers are updated.\n-------------------\n");
         				return;
         			}
         			break;
         		}
         	}
 		}
     	
     	// send the update replication to the next machine
     	String repAddres = getNextLiveAddress();
     	req.updateProbe(this.myId);
 		try {
         	handleMessage(req, "insertReplication: machine " + this.myId + " - " + req.printRequest() + " routting to machine address " + repAddres + "\n");
 			IDistributedHashTable dhtNextMachine = (IDistributedHashTable) 
 					Naming.lookup("rmi://localhost:"+ repAddres +"/DistributedHashTable");
 	        dhtNextMachine.insertReplication(req);
 		} catch (Exception e) {
 			handleMessage(req, "Error-insertReplication: machine " + this.myId + " " +  e.getMessage() + "\n");
 		}
     }
 
     /** 
      * insert an entity on the local hash table if it in the range of this machine,
      * or send the request to the next server that key belongs to if it is not in this server.
      * if next server can not be located, send it to the last server
      */
     public Object lookup(IQueryRequest req) throws RemoteException{
     	String key = req.getKey().trim().toLowerCase();
     	int serverId = getServer(key);
     	// this machine is suppose to contain the key
     	if(serverId == this.myId){
             synchronized(this.localCache) {
                 if(this.localCache.containsKey(key)){
                     Object value = this.localCache.get(key);
                     handleMessage(req, "lookup: machine " + this.myId + " - value of " + req.printRequest() + " is " + value);
                     return value;
                 }
                 else{
                     handleMessage(req, "lookup: machine " + this.myId + " - value of " + req.printRequest() + " not found.");				
                     return null;
                 }			
             }
         }
         else{ 
         	// go to the next machine for lookup
             try {
             	Map.Entry<Integer, String> nextMachine = getNextMachine(serverId, key);
             	// server that contains the key is off line then lookup in replication servers
             	if((nextMachine.getKey() == serverId && !FailureDetectorThread.isAlive(serverId))){
             		handleMessage(req, "lookup: machine " + this.myId + " - server " + serverId + " is failed. Lookup replications.\n");
             		return doLookupReplication(req, key);
             	}
             	else{
             		// could not locate the machine that contains the key, go to the last server
 	                handleMessage(req, "lookup: machine " + this.myId + " - value of " + req.printRequest() + " routed to machine address " + nextMachine.getValue() + "\n");
 	                IDistributedHashTable dhtNextMachine = (IDistributedHashTable) 
 	                Naming.lookup("rmi://localhost:"+ nextMachine.getValue() +"/DistributedHashTable");
 	                return dhtNextMachine.lookup(req);
             	}
             }catch(Exception e) {
                 handleMessage(req, "Error-lookup: machine " + this.myId + " - dhtNextMachine: " +  e.getMessage());
             }
         }
         return null;
     }
     
     /** 
      * create an lookup query replication request and send the request to replication servers
      */
     private Object doLookupReplication(IQueryRequest req, String key) throws RemoteException{
     	IReplicationQueryRequest queryRepReq = new ReplicationQueryRequest(req.getRequestId(), req.getMachineId(), key);
     	if(DebugMode)
     		UnicastRemoteObject.exportObject(queryRepReq);
     	Object value = lookupReplication(queryRepReq);
         handleMessage(req, queryRepReq.getMessage());
         return value;
     }
     
     /** 
      * lookup a query request on replication servers and send the request to next live machine if local does not contain the replication
      */
     public Object lookupReplication(IReplicationQueryRequest req) throws RemoteException{
     	String key = req.getKey().trim();
     	int serverId = getServer(key);
     	synchronized (replications) {
         	for(ReplicationStorage rep: replications){
         		if(serverId == rep.id){
                     Object value = rep.getValue(key);
         			handleMessage(req, "doLookupReplication: machine " + this.myId + " - request " + req.getRequestId() + " from machine " + req.getMachineId() + " with <" + key + ">  is located on replication server. value is " + value + "\n");
         			return value;
         		}
         	}
 		}
     	
     	// replication machine is not find in local, send it to the next live server
     	String repAddres = getNextLiveAddress();
 		try {
         	handleMessage(req, "doLookupReplication: machine " + this.myId + " - " + req.printRequest() + " routting to machine address " + repAddres + "\n");
 			IDistributedHashTable dhtNextMachine = (IDistributedHashTable) 
 					Naming.lookup("rmi://localhost:"+ repAddres +"/DistributedHashTable");
 	        return dhtNextMachine.lookupReplication(req);
 		} catch (Exception e) {
 			handleMessage(req, "Error-doLookupReplication: machine " + this.myId + " " +  e.getMessage());
 		}
 		// we should not get to this point
 		handleMessage(req, "Error-doLookupReplication: machine " + this.myId + " - request " + req.getRequestId() + " from machine " + req.getMachineId() + " with <" + key + ">  is not located on any replication machines\n");
 		return null;
     }
 
     /** 
      * lookup and trace an entity on the local hash table if it in the range of this machine,
      * or send the request to the next server that key belongs to if it is not in this server.
      * if next server can not be located, send it to the last server
      */
     public int lookupTrace(IQueryRequest req) throws RemoteException{
     	String key = req.getKey().trim().toLowerCase();
     	int serverId = getServer(key);
     	if(serverId  == this.myId){
             synchronized(this.localCache) {
                 if(this.localCache.containsKey(key)){
                     handleMessage(req, "lookup trace: machine " + this.myId + " - value of " + req.printRequest() + " is found");
                     return 1;
                 }
                 else{
                     handleMessage(req, "lookup trace: machine " + this.myId + " - value of " + req.printRequest() + " not found.");               
                     return 0;
                 }           
             }
         }
         else{
             try {
             	Map.Entry<Integer, String> nextMachine = getNextMachine(serverId, key);
             	IDistributedHashTable dhtNextMachine = (IDistributedHashTable) 
                 Naming.lookup("rmi://localhost:"+ nextMachine.getValue() +"/DistributedHashTable");
                 handleMessage(req, "lookup trace: machine " + this.myId + " - value of " + req.printRequest() + " routed to machine address " + nextMachine.getValue() + "\n");
                 return 1 + dhtNextMachine.lookupTrace(req);
             }catch(Exception e) {
                 handleMessage(req, "Error-lookup trace: machine " + this.myId + " - dhtNextMachine: " +  e.getMessage());
             }
         }
         return 0;
     }
 
     /** 
      * delete an entity on the local hash table if it in the range of this machine,
      * or send the request to the next server that key belongs to if it is not in this server.
      * if next server can not be located, send it to the last server
      */
     public void delete(IInsertDeleteRequest req) throws RemoteException{
     	String value = (String) req.getValue();
     	for(String word: splitWithStopWords(req.getKey().toLowerCase())){
     		word = word.trim();
     		int serverId = getServer(word);
 	    	if(serverId  == this.myId){
 	            synchronized(this.localCache) {
 	                if(this.localCache.containsKey(word)){
 	                    List<String> values = this.localCache.get(word);
 	                    if(values.contains(value))
 	                    {
 	            			handleMessage(req, "delete: machine " + this.myId + " - request " + req.getRequestId() + " from machine " + req.getMachineId() + " with <" + word + ", "+ req.getValue() + ">  is deleted.\n+++++++ Update Replication.\n");
 	                    	values.remove(value);
 	                    	if(values.size() == 0)
 	                    		this.localCache.remove(word);
 	                    	// update repository servers
 		                	doDeleteReplication(req, word, value, false);
 	                    }
 	                    else
 	                    	handleMessage(req, "delete: machine " + this.myId + " - request " + req.getRequestId() + " from machine " + req.getMachineId() + " with <" + word + ", "+ req.getValue() + ">  not found.\n");
 	                }
 	                else
                     	handleMessage(req, "delete: machine " + this.myId + " - request " + req.getRequestId() + " from machine " + req.getMachineId() + " with <" + word + ", "+ req.getValue() + ">  not found.\n");	            
 	            }
 	        }
 	        else{
 	        	// go to the next machine for lookup
 	            try {
 	            	Map.Entry<Integer, String> nextMachine = getNextMachine(serverId, word);
 	            	// server that contains the key is off line then lookup in replication servers
 	            	if((nextMachine.getKey() == serverId && !FailureDetectorThread.isAlive(serverId))){
 	            		handleMessage(req, "lookup: machine " + this.myId + " - server " + serverId + " is failed. Delete replications.\n");
 	                	doDeleteReplication(req, word, value, true);
 	            	}
 	            	else{
 	            		// create a new request for every single word and send the request to the last server
                 		IInsertDeleteRequest reqNextMachine = new InsertDeleteRequest(req.getRequestId(), req.getMachineId() , word, req.getValue());
 		                handleMessage(req, "delete: machine " + this.myId + " - with <" + word + ", "+ req.getKey() + ">  routed to machine address " + nextMachine.getValue() + "\n");
 		                if(DebugMode)
 		                	UnicastRemoteObject.exportObject(reqNextMachine);
 		            	IDistributedHashTable dhtNextMachine = (IDistributedHashTable)
 		                	Naming.lookup("rmi://localhost:"+ nextMachine.getValue() +"/DistributedHashTable");
 		                dhtNextMachine.delete(reqNextMachine);
 		             // update message received by next machine as original request is not sent to the next machine
 	                    handleMessage(req, reqNextMachine.getMessage());
 	            	}
 	            } catch(Exception e) {
 	                handleMessage(req, "Error-delete: machine " + this.myId + " - dhtNextMachine: " +  e.getMessage());
 	            }
 	        }
     	}
     }
 
     /** 
      * create an delete query replication request and send the request to update replication servers
      */
     private void doDeleteReplication(IQueryRequest req, String key, String value, boolean dirtyDelete) throws RemoteException {
     	IInsertDeleteReplicationRequest delRepReq = new InsertDeleteReplicationRequest(req.getRequestId(), req.getMachineId(), key, value, dirtyDelete);
     	if(DebugMode)
     		UnicastRemoteObject.exportObject(delRepReq);
     	deleteReplication(delRepReq);
         handleMessage(req, delRepReq.getMessage());
 	}
 
     /** 
      * delete a query request on replication servers and send the request to next live machine
      */
     public void deleteReplication(IInsertDeleteReplicationRequest req) throws RemoteException{
     	String key = req.getKey().trim();
     	int serverId = getServer(key);
     	String value = (String)req.getValue();
     	// drop the package, if the request is visited all servers
 		if(req.getProbe().contains(this.myId)){
 			handleMessage(req, "deleteReplication: machine " + this.myId + " - drop package: request " + req.getRequestId() + ". Request visited all servers.\n");
 			// we are the the master server, so both replications are not updated, 
 			// mark the delete as a dirty to be synched with replication servers when they join the ring 
 			if(serverId == this.myId){
 				handleMessage(req, "deleteReplication: machine " + this.myId + " - Mark dirty delete : request " + req.getRequestId() + ".\n");
 				insert(this.dirtyDeleteCache, key, value);
 			}
 			handleMessage(req, "-------------------\n");
 			return; 
 		}
 
     	synchronized (replications) {
         	for(ReplicationStorage rep: replications){
         		if(serverId == rep.id){
         			req.incrementUpdateCount();
         			if(rep.remove(key, value, req.isDirty()))
         				handleMessage(req, "deleteReplication: machine " + this.myId + " - request " + req.getRequestId() + " from machine " + req.getMachineId() + " with <" + key + ", " + req.getValue() + ">  is deleted from replication server.\n+++++++\n");
         			else
         				handleMessage(req, "deleteReplication: machine " + this.myId + " - request " + req.getRequestId() + " from machine " + req.getMachineId() + " with <" + key + ", " + req.getValue()  + ">  not found.\n");
         			
         			if(req.getUpdateCount() == MaxServersToConnect){
         				handleMessage(req, "deleteReplication: machine " + this.myId + " - drop package: request " + req.getRequestId() + ". Both replication servers are updated.\n-------------------\n");
         				return;
         			}
         		}
         	}
 		}
     	
     	// replication machine is not find in local, send it to the next live server
     	String repAddres = getNextLiveAddress();
     	req.updateProbe(this.myId);
 		try {
         	handleMessage(req, "deleteReplication: machine " + this.myId + " - " + req.printRequest() + " routting to machine address " + repAddres + "\n");
 			IDistributedHashTable dhtNextMachine = (IDistributedHashTable) 
 					Naming.lookup("rmi://localhost:"+ repAddres +"/DistributedHashTable");
 	        dhtNextMachine.deleteReplication(req);
 		} catch (Exception e) {
 			handleMessage(req, "Error-deleteReplication: machine " + this.myId + " " +  e.getMessage());
 		}
     }
 
 	/** 
      * purge local hash table
      */
     public boolean purge() throws RemoteException{
         synchronized(this.localCache) {
             this.localCache.clear();
         }
         IReplicationQueryRequest purgeRep = new ReplicationQueryRequest(1, this.myId, null);
         if(DebugMode)
         	UnicastRemoteObject.exportObject(purgeRep);
         boolean res = purgeReplication(purgeRep);
         handleMessage(purgeRep, purgeRep.getMessage());
         return res;
     }
     
     /** 
      * purge replication cache
      */
     public boolean purgeReplication(IReplicationQueryRequest req) throws RemoteException{
     	boolean res = false;
     	// drop the package, if the request is visited all servers
 		if(req.getProbe().contains(this.myId)){
 			handleMessage(req, "purgeReplication: machine " + this.myId + " - drop package: request " + req.getRequestId() + ". Request visited all servers.\n-------------------\n");
 			// we are the the master server, so both replications are not updated,
 			handleMessage(req, "purgeReplication: machine " + this.myId + " - replication machines might be offline: request " + req.getRequestId() + ".\n");
 			return false; 
 		}
     	synchronized (replications) {
         	for(ReplicationStorage rep: replications){
         		if(req.getMachineId()  == rep.id){
         			req.incrementUpdateCount();
         			rep.clear();
     				handleMessage(req, "purgeReplication: machine " + this.myId + " - request " + req.getRequestId() + ". purge repositroy " + req.getMachineId() + "\n" );
         			if(req.getUpdateCount() == MaxServersToConnect){
         				handleMessage(req, "purgeReplication: machine " + this.myId + " - drop package: request " + req.getRequestId() + ". Both replication servers are updated.\n-------------------\n");
         				return true;
         			}
         		}
         	}
 		}
     	
     	String repAddres = getNextLiveAddress();
     	req.updateProbe(this.myId);
     	try {
         	handleMessage(req, "purgeReplication: machine " + this.myId + " - request " + req.getRequestId() + " from machine "+ req.getMachineId() + " routting to machine address " + repAddres + "\n");
 			IDistributedHashTable dhtNextMachine = (IDistributedHashTable) 
 					Naming.lookup("rmi://localhost:"+ repAddres +"/DistributedHashTable");
 			res = dhtNextMachine.purgeReplication(req);
 		} catch (Exception e) {
 			handleMessage(req, "Error-purgeReplication: machine " + this.myId + " " +  e.getMessage());
 		}
     	return res;
     }
 
     /** 
      * return number of keys store in the local hash table
      */
     public int count(){
         synchronized(this.localCache) {
             int n = this.localCache.size();
             System.out.println("count: machine " + this.myId + " is " + n +"\n");
             return n;
         }
     }
 
     /** 
      * append message to the request
      */
     private void handleMessage(IQueryRequest req, String msg){
         try{
             if(DebugMode){
             	utils.Output.println(msg);
             	req.appendMessage(msg);
             }
             	
         }catch(Exception e){ }
     }
     
     /** 
      * calculate the hash code of the key and return the server associated to the key
      */   
     private int getServer(String key){
     	int hash = key.hashCode();
     	int server =(hash % this.sCount) + 1;
    	return ( server <= 0 ) ? server + this.sCount : server;
     }
     
     /** 
      * get next live machine from successor table
      */
     private String getNextLiveAddress(){
     	for (Map.Entry<Integer, String> peer : this.successorTable.entrySet()) {
 			int id = peer.getKey();
 			// check if it is alive
 			if(FailureDetectorThread.isAlive(id)){
 				return peer.getValue();
 			}
     	}
     	return this.backupSuccessor.getValue();
     }
     
     /** 
      * find the next machine from successor table for a key
      * if next machine is not found, return the last server in the successor table if it is a live machine
      * otherwise ask the FD module for the back up server
      */
 	private Map.Entry<Integer, String> getNextMachine(int serverId,  String key){
 		Map.Entry<Integer, String> nextMachine = null;
 		for (Map.Entry<Integer, String> peer : this.successorTable.entrySet()) {
 			int id = peer.getKey();
 			// check if it is alive
 			if(serverId == id)
 				return peer;
 			nextMachine = peer;
 		} 
 		// send the request to the last machine in successor  if it is alive
 		if(nextMachine != null && FailureDetectorThread.isAlive(nextMachine.getKey()))
 			return nextMachine;
 		// all successors are off line, then send the request to the backup successor.
 		return this.backupSuccessor;
 		
 	}
 	
 	/** 
      * split the string and remove stop words
      */
 	private String[] splitWithStopWords(String s){
 		List<String> res = new ArrayList<String>();
 		for(String word: s.split(" "))
 		{
 			if(STOP_WORDS.contains(word) == false){
 				res.add(word);
 			}
 		}
 		return res.toArray(new String[res.size()]);
 	}
 	
 	/** 
      * a failed server join back the ring
      */	
 	public void onServerJoin(ServerJoinEvent e){
 		utils.Output.println("DistributedHashTable - onServerJoin: server " +  e.getServerId() + " joind");
 	}
 }
