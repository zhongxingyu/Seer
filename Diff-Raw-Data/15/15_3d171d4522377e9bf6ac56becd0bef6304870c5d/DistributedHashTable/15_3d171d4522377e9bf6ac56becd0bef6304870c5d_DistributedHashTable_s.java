 package distributed.hash.table;
 
 import java.io.IOException;
 import java.rmi.Naming;
 import java.rmi.RemoteException;
 import java.rmi.server.UnicastRemoteObject;
 import java.util.*;
 import java.util.Map.Entry;
 import javax.swing.SwingWorker;
 
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
     private Hashtable<String, ArrayList<String>> localCache;
     private Hashtable<String, ArrayList<String>> dirtyInsertCache; // stores inserted data that is not populated on replication machines
     private Hashtable<String, ArrayList<String>> dirtyDeleteCache; // stores deleted data that is not populated on replication machines
     private List<ReplicationStorage> replications;
     private LinkedHashMap<Integer, String> successorTable; // <id, address>
     private Map.Entry<Integer, String> backupSuccessor; // the back up server if a live server failed
     private int myId;
     private String myAddress;
     private int sCount;
     private int joinServerId;
     private PersistentStorageManager persistentManager;
 
 
     /** 
      * Constructor
      */
     public DistributedHashTable(int id, int port, int serverCount, LinkedHashMap<Integer, String> successors) throws java.rmi.RemoteException {
     	this(id, "" + port, serverCount, successors);
     }
     
     /** 
      * Constructor
      */
     public DistributedHashTable(int id, String address, int serverCount, LinkedHashMap<Integer, String> successors) throws java.rmi.RemoteException {
         super(); 
         this.localCache = new Hashtable<String, ArrayList<String>>();
         this.dirtyInsertCache =  new Hashtable<String, ArrayList<String>>();
         this.dirtyDeleteCache =  new Hashtable<String, ArrayList<String>>();
         replications = new ArrayList<ReplicationStorage>();
         this.successorTable = new LinkedHashMap<Integer, String>();
 
         this.myId = id;
         this.myAddress = address;
         this.sCount = serverCount;
        this.persistentManager = new PersistentStorageManager(600);
         PersistentStorage persistentStorage = new PersistentStorage();
         
         handleMessage("DHT server id: " + this.myId + " is created.");
 		int i = 1;
 		// <id, address> address can be name:port. For now assumption is address is just port
 		for (Map.Entry<Integer, String> successor : successors.entrySet()) {
 			if(i++ <= MaxServersToConnect){
 				this.successorTable.put(successor.getKey(), successor.getValue());
 				replications.add(new ReplicationStorage(successor.getKey(), "", Integer.parseInt(successor.getValue())));
 				handleMessage(" successor " + successor.getKey()  + " on port " + successor.getValue() + " is added.");
 			}
 			else{
 				handleMessage(" backup successor " + successor.getKey()  + " on port " + successor.getValue() + " is added.");
 				this.backupSuccessor = successor;
 			}
 		}
 		
 		// sync replications with master server
 		for (ReplicationStorage rep: replications){
 			String localCacheBackupName = this.myId + "_" + rep.getId() + "_localCache.bkp";
             java.net.URL path = ClassLoader.getSystemResource(localCacheBackupName);    
 			if (null != path) {
 			    try {
                     rep.setLocalCache(persistentStorage.load(localCacheBackupName));
                     handleMessage("preformed restore with " + localCacheBackupName);
                 } catch (IOException e) {
                     e.printStackTrace();
                 } catch (ClassNotFoundException e) {
                     e.printStackTrace();
                 }
 			}
 			
 			// register data structures for persistent storage
 			this.persistentManager.register(localCacheBackupName, rep.getLocalCache());
 //			this.persistentManager.register(this.myId + "_" + rep.getId() + "_dirtyInsertCache.bkp", rep.getDirtyInsertCache());
 //			this.persistentManager.register(this.myId + "_" + rep.getId() + "_dirtyDeleteCache.bkp", rep.getDirtyDeleteCache());      
 
 			syncReplicationstorage(rep);
 		}
 		// register data structures for persistent storage	
         String localCacheBackupName = this.myId + "_" + this.myId + "_localCache.bkp";
         java.net.URL path = ClassLoader.getSystemResource(localCacheBackupName);    
         if (null != path) {
             try {
                 localCache = persistentStorage.load(localCacheBackupName);
                 handleMessage("preformed restore with " + localCacheBackupName);
             } catch (IOException e) {
                 e.printStackTrace();
             } catch (ClassNotFoundException e) {
                 e.printStackTrace();
             }
         }
 		this.persistentManager.register(localCacheBackupName, localCache);
 //		this.persistentManager.register(this.myId + "_" + this.myId + "_dirtyInsertCache.bkp", dirtyInsertCache);	
 //		this.persistentManager.register(this.myId + "_" + this.myId + "_dirtyDeleteCache.bkp", dirtyDeleteCache);
 		this.persistentManager.start();
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
                 if(insertToCache(this.localCache, word, newValue)){
                     // this.cache.put(word, values);
                     handleMessage(req, "insert: machine " + this.myId + " - request " + req.getRequestId() + " from machine " + req.getMachineId() + " with <" + word + " , " + req.getValue() + ">  is inserted.\n +++++++ Update Repliction", true);
                 	// update repository servers
                 	doInsertReplication(req, word, false);                    	
                 }
                 else
                 	handleMessage(req, "insert: machine " + this.myId + " - request " + req.getRequestId() + " from machine " + req.getMachineId() + " with <" + word + " , " + req.getValue() + ">  already exists.", true);
             }
             else{
             	// go to the next machine for insert
                 try {                	
                 	Map.Entry<Integer, String> nextMachine = getNextMachine(serverId, word);
                 	if(FailureDetectorThread.isServerAlive(nextMachine.getKey())){
                 		// go to next machine, it is either the master server or the last successor
                 		// create a new request for every word and send the request to the last server
                 		doInsertNextMachine (req, nextMachine, word, newValue);
                 	}
                 	else{
                 		if(nextMachine.getKey() == serverId){
     	                	// master server is off line, updates the replication machines
     	            		handleMessage(req, "insert: machine " + this.myId + " - server " + serverId + " is failed. Insert replications.", true);
     	            		doInsertReplication(req, word, true);
     	            	}
                 		else{
                 			nextMachine = getNextLiveMachine();
                         	// create a new request for every word and send the request to the last server
                 			doInsertNextMachine (req, nextMachine, word, newValue);
                 		}
                 	}
                 }  catch(Exception e) {
                     handleMessage(req, "Error-insert: machine " + this.myId + " - dhtNextMachine: " +  e.getMessage(), true);
                 }
             }
     	}
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
 			handleMessage(req, "insertReplication: machine " + this.myId + " - drop package: request " + req.getRequestId() + ". Request visited all servers.", true);
 			// we are the the master server, so both replications are not updated, 
 			// mark the insert as a dirty to be synced with replication servers when they join the ring 
 			if(serverId == this.myId){
 				handleMessage(req, "insertReplication: machine " + this.myId + " - Mark dirty insert : request " + req.getRequestId() + ".", true);
 				insertToCache(this.dirtyInsertCache, key, value);
 			}
 			handleMessage(req, "-------------------", true);
 			return; 
 		}
 
     	synchronized (replications) {
     		ReplicationStorage rep = getReplicationStorage(serverId);
     		if(rep != null && serverId == rep.id){
     			// local server contains the replication
     			handleMessage(req, "insertReplication: machine " + this.myId + " - request " + req.getRequestId() + " from machine " + req.getMachineId() + " with <" + key + " , " + req.getValue() + ">  is inserted.", true);
     			if(req.isDirty())
     				handleMessage(req, "insertReplication: machine " + this.myId + " - request " + req.getRequestId() + " Mark dirty insert replication", true);
     			handleMessage(req, "+++++++", true);
     			req.incrementUpdateCount();
     			rep.insert(key, value, req.isDirty());
     			// drop the package, if both replication servers are updated
     			if(req.getUpdateCount() == MaxServersToConnect){
     				handleMessage(req, "insertReplication: machine " + this.myId + " - drop package: request " + req.getRequestId() + ". Both replication servers are updated.\n-------------------", true);
     				return;
     			}
         	}
 		}
     	
     	// send the update replication to the next machine
     	Map.Entry<Integer, String> repMachine = getNextLiveMachine();
     	req.updateProbe(this.myId);
 		try {
         	handleMessage(req, "insertReplication: machine " + this.myId + " - " + req.printRequest() + " routting to machine address " + repMachine.getValue(), true);
 			IDistributedHashTable dhtNextMachine = (IDistributedHashTable) 
 					Naming.lookup("rmi://localhost:" + repMachine.getValue() + "/DistributedHashTable");
 	        dhtNextMachine.insertReplication(req);
 		} catch (Exception e) {
 			handleMessage(req, "Error-insertReplication: machine " + this.myId + " " +  e.getMessage(), true);
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
     	Object value = null;
     	// this machine is suppose to contain the key
     	if(serverId == this.myId){
             synchronized(this.localCache) {
                 if(this.localCache.containsKey(key)){
                     value = this.localCache.get(key);
                     handleMessage(req, "lookup: machine " + this.myId + " - value of " + req.printRequest() + " is " + value, true);
                 }
                 else{
                     handleMessage(req, "lookup: machine " + this.myId + " - value of " + req.printRequest() + " not found.", true);
                 }			
             }
         }
         else{ 
         	// go to the next machine for lookup
             try {
             	Map.Entry<Integer, String> nextMachine = getNextMachine(serverId, key);
             	if(FailureDetectorThread.isServerAlive(nextMachine.getKey())){
             		// go to next machine, it is either the master server or the last successor
             		value = doLookupNextMachine(req, nextMachine);
             	}
             	else{
             		// master or last successor is failed, check if local server contains the repository?
             		if(this.successorTable.keySet().contains(serverId)){
             			// it contains repository storage, return the value
                         value = getReplicationStorage(serverId).getValue(key);
                         handleMessage(req, "lookup: machine " + this.myId + " - value of " + req.printRequest() + " server " + serverId + " is failed", true);
                         handleMessage(req, "lookup: machine " + this.myId + " - value of " + req.printRequest() + " in replication is " + value, true);
             		}
             		else{
 	            		// go to next live machine, it knows how to handle the request
 	            		nextMachine = getNextLiveMachine();
 	            		value =  doLookupNextMachine(req, nextMachine);
             		}
             	}
             }catch(Exception e) {
                 handleMessage(req, "Error-lookup: machine " + this.myId + " - dhtNextMachine: " +  e.getMessage(), true);
             }
         }
         return value;
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
                     handleMessage(req, "lookup trace: machine " + this.myId + " - value of " + req.printRequest() + " is found", true);
                     return 1;
                 }
                 else{
                     handleMessage(req, "lookup trace: machine " + this.myId + " - value of " + req.printRequest() + " not found.", true);               
                     return 0;
                 }           
             }
         }
         else{
             try {
             	Map.Entry<Integer, String> nextMachine = getNextMachine(serverId, key);
             	IDistributedHashTable dhtNextMachine = (IDistributedHashTable) 
                 Naming.lookup("rmi://localhost:"+ nextMachine.getValue() +"/DistributedHashTable");
                 handleMessage(req, "lookup trace: machine " + this.myId + " - value of " + req.printRequest() + " routed to machine address " + nextMachine.getValue(), true);
                 return 1 + dhtNextMachine.lookupTrace(req);
             }catch(Exception e) {
                 handleMessage(req, "Error-lookup trace: machine " + this.myId + " - dhtNextMachine: " +  e.getMessage(), true);
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
                 if(deleteFromChace(this.localCache, word, value)){
         			handleMessage(req, "delete: machine " + this.myId + " - request " + req.getRequestId() + " from machine " + req.getMachineId() + " with <" + word + ", "+ req.getValue() + ">  is deleted.\n+++++++ Update Replication.", true);
                 	// update repository servers
                 	doDeleteReplication(req, word, value, false);
                 }
                 else
                 	handleMessage(req, "delete: machine " + this.myId + " - request " + req.getRequestId() + " from machine " + req.getMachineId() + " with <" + word + ", "+ req.getValue() + ">  not found.", true);
 	        }
 	        else{
 	        	// go to the next machine for lookup
 	            try {
 	            	Map.Entry<Integer, String> nextMachine = getNextMachine(serverId, word);
 	            	if(FailureDetectorThread.isServerAlive(nextMachine.getKey())){
 	            		// go to next machine, it is either the master server or the last successor
             			doDeleteNextMachine (req, nextMachine, word, value);
 	            	}
 	            	else
 	            	{
 	            		if(nextMachine.getKey() == serverId){
 		                	// master server is off line, updates the replication machines
 		            		handleMessage(req, "lookup: machine " + this.myId + " - server " + serverId + " is failed. Delete replications.", true);
 		                	doDeleteReplication(req, word, value, true);
 		            	}
 	            		else{
 	            			nextMachine = getNextLiveMachine();
 	                    	// create a new request for every word and send the request to the last server
 	            			doDeleteNextMachine (req, nextMachine, word, value);
 	            		}
 	            	}
 	            } catch(Exception e) {
 	            	e.printStackTrace();
 	                handleMessage(req, "Error-delete: machine " + this.myId + " - dhtNextMachine: " +  e.getMessage(), true);
 	            }
 	        }
     	}
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
 			handleMessage(req, "deleteReplication: machine " + this.myId + " - drop package: request " + req.getRequestId() + ". Request visited all servers.", true);
 			// we are the the master server, so both replications are not updated, 
 			// mark the delete as a dirty to be synced with replication servers when they join the ring 
 			if(serverId == this.myId){
 				handleMessage(req, "deleteReplication: machine " + this.myId + " - Mark dirty delete : request " + req.getRequestId(), true);
 				insertToCache(this.dirtyDeleteCache, key, value);
 			}
 			handleMessage(req, "-------------------", true);
 			return; 
 		}
 
     	synchronized (replications) {
     		ReplicationStorage rep = getReplicationStorage(serverId);
     		if(rep != null && serverId == rep.id){
     			req.incrementUpdateCount();
     			if(rep.remove(key, value, req.isDirty())){
     				handleMessage(req, "deleteReplication: machine " + this.myId + " - request " + req.getRequestId() + " from machine " + req.getMachineId() + " with <" + key + ", " + req.getValue() + ">  is deleted from replication server.", true);
     				if(req.isDirty())
     					handleMessage(req, "deleteReplication: machine " + this.myId + " - request " + req.getRequestId() + " from machine " + req.getMachineId() + " Mark dirty delete replication.", true);
         			handleMessage(req, "+++++++", true);    				
     			}
     			else
     				handleMessage(req, "deleteReplication: machine " + this.myId + " - request " + req.getRequestId() + " from machine " + req.getMachineId() + " with <" + key + ", " + req.getValue()  + ">  not found.", true);
     			
     			if(req.getUpdateCount() == MaxServersToConnect){
     				handleMessage(req, "deleteReplication: machine " + this.myId + " - drop package: request " + req.getRequestId() + ". Both replication servers are updated.\n-------------------", true);
     				return;
     			}
         	}
 		}
     	
     	// replication machine is not find in local, send it to the next live server
     	Map.Entry<Integer, String> repMachine = getNextLiveMachine();
     	req.updateProbe(this.myId);
 		try {
         	handleMessage(req, "deleteReplication: machine " + this.myId + " - " + req.printRequest() + " routting to machine address " + repMachine.getValue(), true);
 			IDistributedHashTable dhtNextMachine = (IDistributedHashTable) 
 					Naming.lookup("rmi://localhost:"+ repMachine.getValue() +"/DistributedHashTable");
 	        dhtNextMachine.deleteReplication(req);
 		} catch (Exception e) {
 			handleMessage(req, "Error-deleteReplication: machine " + this.myId + " " +  e.getMessage(), true);
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
         handleMessage(purgeRep, purgeRep.getMessage(), true);
         return res;
     }
     
     /** 
      * purge replication cache
      */
     public boolean purgeReplication(IReplicationQueryRequest req) throws RemoteException{
     	boolean res = false;
     	// drop the package, if the request is visited all servers
 		if(req.getProbe().contains(this.myId)){
 			handleMessage(req, "purgeReplication: machine " + this.myId + " - drop package: request " + req.getRequestId() + ". Request visited all servers.\n-------------------", true);
 			// we are the the master server, so both replications are not updated,
 			handleMessage(req, "purgeReplication: machine " + this.myId + " - replication machines might be offline: request " + req.getRequestId() + ".", true);
 			return false; 
 		}
     	synchronized (replications) {
         	for(ReplicationStorage rep: replications){
         		if(req.getMachineId()  == rep.id){
         			req.incrementUpdateCount();
         			rep.clear();
     				handleMessage(req, "purgeReplication: machine " + this.myId + " - request " + req.getRequestId() + ". purge repositroy " + req.getMachineId(), true);
         			if(req.getUpdateCount() == MaxServersToConnect){
         				handleMessage(req, "purgeReplication: machine " + this.myId + " - drop package: request " + req.getRequestId() + ". Both replication servers are updated.\n-------------------", true);
         				return true;
         			}
         		}
         	}
 		}
     	
     	Map.Entry<Integer, String> repMachine = getNextLiveMachine();
     	req.updateProbe(this.myId);
     	try {
         	handleMessage(req, "purgeReplication: machine " + this.myId + " - request " + req.getRequestId() + " from machine "+ req.getMachineId() + " routting to machine address " + repMachine.getValue(), true);
 			IDistributedHashTable dhtNextMachine = (IDistributedHashTable) 
 					Naming.lookup("rmi://localhost:"+ repMachine.getValue() +"/DistributedHashTable");
 			res = dhtNextMachine.purgeReplication(req);
 		} catch (Exception e) {
 			handleMessage(req, "Error-purgeReplication: machine " + this.myId + " " +  e.getMessage(), true);
 		}
     	return res;
     }
 
     /** 
      * return number of keys store in the local hash table
      */
     public int count(){
         synchronized(this.localCache) {
             int n = this.localCache.size();
             handleMessage("count: machine " + this.myId + " is " + n);
             return n;
         }
     }
 
     
 	/** 
      * a failed server join back the ring
      */	
 	public void onServerJoin(ServerJoinEvent e){
 		handleMessage("DistributedHashTable - onServerJoin: server " +  e.getServerId() + " joind");
 		joinServerId = e.getServerId();
 		// skip if back up successor comes back online
 		if(joinServerId == this.backupSuccessor.getKey())
 			return;
 		
 		// run the update on a worker thread
 		try {
 			SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
 				@Override
 				protected Void doInBackground() throws Exception {
 					String onlineServerAddress =  successorTable.get(joinServerId);
 					ReplicationStorage repServer = getReplicationStorage(joinServerId);				
 									
 					// make sure the other replication machine is online, if it is off online, 
 					// make the update dirty to be synced later with other replication in case master failed.
 		        	Map.Entry<Integer, String> nextLiveMachine = getNextLiveMachine();
 					IDistributedHashTable dhtNextLiveMachine = (IDistributedHashTable) 
 							Naming.lookup("rmi://localhost:"+ nextLiveMachine.getValue() +"/DistributedHashTable");
 					Map.Entry<Integer, String> nextRepMachineAddress = dhtNextLiveMachine.getRepHostAddress(myId, myId);
 					boolean isDirty = true;
 					if(nextRepMachineAddress != null){
 						handleMessage("onServerJoin: machine " + myId + " - other replication server " + nextRepMachineAddress.getKey() + " is online");
 						isDirty = false;
 					}
 					else
 						handleMessage("onServerJoin: machine " + myId + " - other replication storage of server " + repServer.id + " is offline");
 					
 					IDistributedHashTable dhtJointMachine = (IDistributedHashTable) 
 							Naming.lookup("rmi://localhost:"+ onlineServerAddress +"/DistributedHashTable");
 					
 			        // update dirty insert cache on the server that joins to the ring
 			        if(repServer.getDirtyInsertCache().size() > 0 &&
 			        		dhtJointMachine.syncDirtyInsertCache(myId , repServer.getDirtyInsertCache(), isDirty)){
 			        	// delete dirty inserts as master server gets the updates and the other replication is online
 			        	if(!isDirty)
 			        		repServer.clearDirtyInsert();
 			        }
 			        // update dirty delete cache on the server that joins to the ring 
 			        if(repServer.getDirtyDeleteCache().size() > 0 &&
 			        		dhtJointMachine.syncDirtyDeleteCache(myId, repServer.getDirtyDeleteCache(), isDirty)){
 			        	// delete dirty deletes as master server gets the updates and the other replication is online
 			        	if(!isDirty)
 			        		repServer.clearDirtyDelete();
 			        }
 					return null;
 				}
 			};
 			worker.execute();
 		} catch (Exception e1) {
 			handleMessage("Error-onServerJoin: machine " + this.myId + " - server" + joinServerId + " failed to update dirty updates " + e1.getMessage()); 
 		}
 	}
 
 
 	/** 
      * update dirty insert cache when this server joins back to the ring
      */	
 	public boolean syncDirtyInsertCache(final int senderId, final Hashtable<String, ArrayList<String>> dirtyInserts, final boolean isDirty) throws RemoteException {		
 		SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {            
 			@Override
 			protected Boolean doInBackground() throws Exception {
 				Iterator<Entry<String, ArrayList<String>>> it = dirtyInserts.entrySet().iterator();
 				while (it.hasNext()) {
 					Entry<String, ArrayList<String>> entry = it.next();
 					for(String value: entry.getValue()){
 						handleMessage("updateDirtyInsertCache: machine " + myId + " - from server " + senderId + " insert dirty entry <" + entry.getKey() + ", " + entry.getValue() + ">.");
 						insertToCache(localCache, entry.getKey(), value);
 						if(isDirty)
 							insertToCache(dirtyInsertCache, entry.getKey(), value);
 					}
 				}
 				return true;
 			}
         };
         worker.execute();
         return true;
 	}
 
 
 	/** 
      * update dirty delete cache when this server joins back to the ring
      */ 
 	public boolean syncDirtyDeleteCache(final int senderId, final Hashtable<String, ArrayList<String>> dirtyDeletes, final boolean isDirty) throws RemoteException {
 		SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {            
 			@Override
 			protected Boolean doInBackground() throws Exception {
 				Iterator<Entry<String, ArrayList<String>>> it = dirtyDeletes.entrySet().iterator();
 				while (it.hasNext()) {
 					Entry<String, ArrayList<String>> entry = it.next();
 					for(String value: entry.getValue()){
 						handleMessage("updateDirtyDeleteCache: machine " + myId + " - from server " + senderId + " delete dirty entry <" + entry.getKey() + ", " + entry.getValue() + ">."); 
 						deleteFromChace(localCache, entry.getKey(), value);
 						if(isDirty)
 							insertToCache(dirtyDeleteCache, entry.getKey(), value);
 					}
 				}
 				return true;
 			}
         };
         worker.execute();
         return true;
 	}
 
     
 	/** 
      * returns dirty delete cache. This function should be called on join a server
      * this functions check if both replication servers are running, then cleans the dirty delete cache
      */ 
     public Hashtable<String, ArrayList<String>> getDirtyDeleteCache(final int newJoindServerId){
 		Hashtable<String, ArrayList<String>> copy = deepCopy(this.dirtyDeleteCache);
 		SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {            
 			@Override
 			protected Void doInBackground() throws Exception {
 	        	Map.Entry<Integer, String> nextLiveMachine = getNextLiveMachine();
 				List<Integer> repHosts = null;
 				try {
 					IDistributedHashTable dhtLivMachine = (IDistributedHashTable) 
 						Naming.lookup("rmi://localhost:"+ nextLiveMachine.getValue() +"/DistributedHashTable");
 					repHosts = dhtLivMachine.getReplicationServers(myId);
 				} catch (Exception e) { 
 					handleMessage("Error-getDirtyInsertCache: machine " + myId + " " + e.getMessage());
 				}
 				if(repHosts != null && (repHosts.size() == MaxServersToConnect ||
 						(repHosts.size() == (MaxServersToConnect) && !repHosts.contains(newJoindServerId))))
 				{
 					// there is a condition that the server asked for the dirty inserts, has a delay to join
 					// the ring, so repHosts checks if the number of host in case the requester server does not 
 					// respond yet
 					dirtyDeleteCache.clear();
 				}
 				else
 					handleMessage("getDirtyInsertCache: machine " + myId + " a replication server is offline keep DirtyInsertCache.");
 				return null;
 			}
 		};
 		worker.execute();
 		return copy;
 	}
 
 	/** 
      * returns dirty insert cache. This function should be called on join a server
      * this functions check if both replication servers are running, then cleans the dirty insert cache
      */ 
     public Hashtable<String, ArrayList<String>> getDirtyInsertCache(final int newJoindServerId){
 		Hashtable<String, ArrayList<String>> copy = deepCopy(this.dirtyInsertCache);
 		SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {            
 			@Override
 			protected Void doInBackground() throws Exception {
 	        	Map.Entry<Integer, String> nextLiveMachine = getNextLiveMachine();
 				List<Integer> repHosts = null;
 				try {
 					IDistributedHashTable dhtLivMachine = (IDistributedHashTable) 
 						Naming.lookup("rmi://localhost:"+ nextLiveMachine.getValue() +"/DistributedHashTable");
 					repHosts = dhtLivMachine.getReplicationServers(myId);
 				} catch (Exception e) { 
 					handleMessage("Error-getDirtyInsertCache: machine " + myId + " " + e.getMessage());
 				}
 				if(repHosts != null && (repHosts.size() == MaxServersToConnect ||
 						(repHosts.size() == (MaxServersToConnect - 1) && !repHosts.contains(newJoindServerId))))
 				{
 					// there is a condition that the server asked for the dirty inserts, has a delay to join
 					// the ring, so repHosts checks if the number of host in case the requester server does not 
 					// respond yet
 					handleMessage("getDirtyInsertCache: machine " + myId + " both replication servers are runinng, clean DirtyInsertCache.");
 					dirtyDeleteCache.clear();
 				}
 				else
 					handleMessage("getDirtyInsertCache: machine " + myId + " a replication server is offline keep DirtyInsertCache.");
 				return null;
 			}
 		};
 		worker.execute();
 		return copy;
 	}
 
 	/** 
      * returns if this server contains a replication server of request id and sends the query to the next live machine. 
      * the request would go to the ring till it reaches the requester
      */ 
 	public List<Integer> getReplicationServers(int serverId) throws RemoteException{
 		// token has visited all servers in the ring, don't need to go more
 		List<Integer> res = new ArrayList<Integer>();
 		if(serverId == this.myId)
 			return res;
 		
 		ReplicationStorage rep = getReplicationStorage(serverId);
 		if(rep != null && rep.id == serverId)
 			res.add(this.myId);
 		
     	Map.Entry<Integer, String> nextLiveMachine = getNextLiveMachine();
 		try {
 			IDistributedHashTable dhtNextMachine = (IDistributedHashTable) 
 				Naming.lookup("rmi://localhost:"+ nextLiveMachine.getValue() +"/DistributedHashTable");
 			res.addAll(dhtNextMachine.getReplicationServers(serverId));
 		} catch (Exception e) {
 			handleMessage("Error-getReplicationCount: machine " + myId + e.getMessage());
 		}
 		return res; 
 	}
 	
 	/** 
      * returns host address of a replication server
      * return null if no host is found
      */ 
 	public Map.Entry<Integer, String> getRepHostAddress(int requestorId ,int serverId) throws RemoteException{
 		// token has visited all servers in the ring, don't need to go more
 		if(requestorId == this.myId)
 			return null;
 		
 		ReplicationStorage rep = getReplicationStorage(serverId);
 		if(rep != null && rep.id == serverId)
 			return new AbstractMap.SimpleEntry<Integer, String>(this.myId, this.myAddress);
 		
 		Map.Entry<Integer, String> nextLivMachine = getNextLiveMachine();
 		try {
 			IDistributedHashTable dhtNextMachine = (IDistributedHashTable) 
 				Naming.lookup("rmi://localhost:"+ nextLivMachine.getValue() +"/DistributedHashTable");
 			return dhtNextMachine.getRepHostAddress(requestorId, serverId);
 		} catch (Exception e) {
 			handleMessage("Error-getRepHostAddress: machine " + myId + e.getMessage());
 		}
 		return null; 
 	}
 
 	/** 
      * get dirty deletes of a replication server hosted in this machine
      */
 	public Hashtable<String, ArrayList<String>> getDirtyDeleteRepCache(int serverId) throws RemoteException {
 		Hashtable<String, ArrayList<String>> res = new Hashtable<String, ArrayList<String>>();
 		ReplicationStorage rep = getReplicationStorage(serverId);
 		if(rep!= null && rep.id == serverId)
 			res = rep.getDirtyDeleteCache();
 		
 		return res;
 	}
 
 	/** 
      * get dirty inserts of a replication server hosted in this machine
      */
 	public Hashtable<String, ArrayList<String>> getDirtyInsertRepCache(int serverId) throws RemoteException {
 		Hashtable<String, ArrayList<String>> res = new Hashtable<String, ArrayList<String>>();
 		for(ReplicationStorage rep: this.replications){
 			if(rep.id == serverId){
 				res = rep.getDirtyInsertCache();
 			}
 		}
 		return res;	
 	}
 	
 	/** 
      * create an lookup query request and send the request to next machine servers
      */
     private Object doLookupNextMachine(IQueryRequest req, Map.Entry<Integer, String> nextMachine) throws RemoteException{
     	Object value = null;
         handleMessage(req, "lookup: machine " + this.myId + " - value of " + req.printRequest() + " routed to machine address " + nextMachine.getValue(), true);
 		try {
 			IDistributedHashTable dhtNextMachine = (IDistributedHashTable) 
 					Naming.lookup("rmi://localhost:"+ nextMachine.getValue() +"/DistributedHashTable");
 			value = dhtNextMachine.lookup(req);
 		} catch (Exception e) {
             handleMessage(req, "Error-doLookupNextMachine: machine " + this.myId + " - dhtNextMachine: " +  e.getMessage(), true);
 		}
 		return value;
     }
 
     /** 
      * insert an entry to the cache
      */	
     private boolean insertToCache(Hashtable<String, ArrayList<String>> cache, String key, String value){
     	synchronized (cache) {
     		ArrayList<String> values = cache.get(key);
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
      * delete an entry from the cache
      */
     private boolean deleteFromChace(Hashtable<String, ArrayList<String>> cache, String key, String value){
     	synchronized (cache) {
     		if(cache.containsKey(key)){
     			ArrayList<String> values = cache.get(key);
                 if(values.contains(value)){
                 	values.remove(value);
                 	if(values.size() == 0)
                 		cache.remove(key);
                 	return true;
                 }
             }
     	}
     	return false;
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
      * get next live machine from successor table or backup successor
      */
     private Map.Entry<Integer, String>  getNextLiveMachine(){
     	for (Map.Entry<Integer, String> successor : this.successorTable.entrySet()) {
 			// check if it is alive
 			if(FailureDetectorThread.isServerAlive(successor.getKey())){
 				return successor;
 			}
     	}
     	return this.backupSuccessor;
     }
     
     /** 
      * find the next machine from successor table for a key
      * if next machine is not found, return the last server in the successor table if it is a live machine
      * otherwise ask the FD module for the back up server
      */
 	private Map.Entry<Integer, String> getNextMachine(int serverId,  String key){
 		Map.Entry<Integer, String> nextMachine = null;
 		for (Map.Entry<Integer, String> successor : this.successorTable.entrySet()) {
 			int id = successor.getKey();
 			if(serverId == id)
 				return successor;
 			nextMachine = successor;
 		}
 		return nextMachine;
 	}
   
 	/** 
      * find and return a replication storage if it is located in this machine
      */
 	private ReplicationStorage getReplicationStorage(int serverId){
 		for(ReplicationStorage rep: this.replications){
 			if(serverId == rep.id)
 				return rep;
 		}
 		return null;
 	}
 	
     /** 
      * output a message message 
      */
     private void handleMessage(String msg){
         try{
             if(DebugMode){
             	utils.Output.println(msg);
             }
         }catch(Exception e){ }
     }
     
     /** 
      * append message to the request
      */
     private void handleMessage(IQueryRequest req, String msg, boolean output){
         try{
             if(DebugMode){
             	if(output)
             		handleMessage(msg);
             	if(req != null)
             		req.appendMessage(msg);
             }
             	
         }catch(Exception e){ }
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
      * when this server backs to the ring, it will sync the replication servers with the master if it is online
      * or with the other replication server 
      */ 
     private void syncReplicationstorage(final ReplicationStorage rep){
     	SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
 			@Override
 			protected Void doInBackground() throws Exception {
 				// is master online
 		    	if(FailureDetectorThread.isServerAlive(rep.id)){
 		    		
 					// make sure the other replication machine is online, if it is off online, 
 					// make the update dirty to be synced later with other replication in case master failed.
 		        	Map.Entry<Integer, String> nextLiveMachine = getNextLiveMachine();
 					IDistributedHashTable dhtNextLiveMachine = (IDistributedHashTable) 
 							Naming.lookup("rmi://localhost:"+ nextLiveMachine.getValue() +"/DistributedHashTable");
 					Map.Entry<Integer, String> nextRepMachineAddress = dhtNextLiveMachine.getRepHostAddress(myId, rep.id);
 
 					boolean isDirty = true;
 					if(nextRepMachineAddress != null){
 						handleMessage("syncReplicationstorage: machine " + myId + " - other replication storage of " + rep.id  + " is " + nextRepMachineAddress.getKey() + " is online");
 						isDirty = false;
 					}
 					else
 						handleMessage("syncReplicationstorage: machine " + myId + " - other replication storage of " + rep.id + " is offline");
 					
 					IDistributedHashTable dhtMasterMachine = (IDistributedHashTable) 
 							Naming.lookup("rmi://localhost:" + rep.port + "/DistributedHashTable");
 
 			        // update dirty insert cache on the server that joins to the ring
 					Hashtable<String, ArrayList<String>> masterDirtyInsertCache = dhtMasterMachine.getDirtyInsertCache(myId);
 					Iterator<Entry<String, ArrayList<String>>> it = masterDirtyInsertCache.entrySet().iterator();
 					while (it.hasNext()) {
 						Entry<String, ArrayList<String>> entry = it.next();
 						for(String value: entry.getValue()){
 							handleMessage("syncReplicationstorage: machine " + myId + " - from server " + rep.id + " insert dirty entry <" + entry.getKey() + ", " + entry.getValue() + ">.");
 							rep.insert(entry.getKey(), value, isDirty);
 						}
 					}
 					
 					Hashtable<String, ArrayList<String>> masterDirtyDeleteCache = dhtMasterMachine.getDirtyDeleteCache(myId);
 					it = masterDirtyDeleteCache.entrySet().iterator();
 					while (it.hasNext()) {
 						Entry<String, ArrayList<String>> entry = it.next();
 						for(String value: entry.getValue()){
 							handleMessage("syncReplicationstorage: machine " + myId + " - from server " + rep.id + " delete dirty entry <" + entry.getKey() + ", " + entry.getValue() + ">.");
 							rep.remove(entry.getKey(), value, isDirty);
 						}
 					}
 		        }
 		    	else{
 		    		// master server is off-line, sync with the other replication machine if it is online
 		        	Map.Entry<Integer, String> nextLiveMachine = getNextLiveMachine();
 					IDistributedHashTable dhtNextliveMachine = (IDistributedHashTable) 
 						Naming.lookup("rmi://localhost:" + nextLiveMachine.getValue() + "/DistributedHashTable");
 					Map.Entry<Integer, String> nextRepMachineAddress = dhtNextliveMachine.getRepHostAddress(myId, rep.id);
 					if(nextRepMachineAddress != null){
 				        // update dirty insert cache on the server that joins to the ring
 						IDistributedHashTable dhtRepMachine = (IDistributedHashTable) 
 							Naming.lookup("rmi://localhost:" + nextRepMachineAddress.getValue() + "/DistributedHashTable");
 						
 						Hashtable<String, ArrayList<String>> repDirtyInsertCache = dhtRepMachine.getDirtyInsertRepCache(rep.id);
 						Iterator<Entry<String, ArrayList<String>>> it = repDirtyInsertCache.entrySet().iterator();
 						while (it.hasNext()) {
 							Entry<String, ArrayList<String>> entry = it.next();
 							for(String value: entry.getValue()){
 								handleMessage("syncReplicationstorage: machine " + myId + " - from server " + rep.id + " insert dirty entry <" + entry.getKey() + ", " + entry.getValue() + ">.");
 								// make the insert dirty, in case the other replication machine fails 
 								// the dirty insert can be synced with master 
 								rep.insert(entry.getKey(), value, true);
 							}
 						}
 						
 						Hashtable<String, ArrayList<String>> repDirtyDeleteCache = dhtRepMachine.getDirtyDeleteRepCache(rep.id);
 						it = repDirtyDeleteCache.entrySet().iterator();
 						while (it.hasNext()) {
 							Entry<String, ArrayList<String>> entry = it.next();
 							for(String value: entry.getValue()){
 								handleMessage("syncReplicationstorage: machine " + myId + " - from server " + rep.id + " delete dirty entry <" + entry.getKey() + ", " + entry.getValue() + ">.");
 								// make the delete dirty, in case the other replication machine fails 
 								// the dirty delete can be synced with master 
 								rep.remove(entry.getKey(), value, true);
 							}
 						}
 					}
 		    	}
 		    	return null;
 			}
         };
         worker.execute();
     }	
 	
 	/** 
      * create an insert query request and send the request to next machine servers
      */
     private void doInsertNextMachine (IInsertDeleteRequest req, Map.Entry<Integer, String> nextMachine, 
     		String key, String value) throws RemoteException{
 		try{
 	    	IInsertDeleteRequest reqNextMachine = new InsertDeleteRequest(req.getRequestId(), req.getMachineId() , key, value);
 	    	handleMessage(req, "insert: machine " + this.myId + " - " + reqNextMachine.printRequest() + " routting to machine address " + nextMachine.getValue(), true);
 	    	if(DebugMode)
 	    		UnicastRemoteObject.exportObject(reqNextMachine);
 	        IDistributedHashTable dhtNextMachine = (IDistributedHashTable) 
 	        		Naming.lookup("rmi://localhost:"+ nextMachine.getValue() +"/DistributedHashTable");
 	        dhtNextMachine.insert(reqNextMachine);
 	        // update message received by next machine as original request is not sent to the next machine
 	        handleMessage(req, reqNextMachine.getMessage(), false);
 		} catch(Exception e) {
             handleMessage(req, "Error-doInsertNextMachine: machine " + this.myId + " - " +  e.getMessage(), true);
         }
     }
     
 	/** 
      * create a delete query request and send the request to next machine servers
      */
     private void doDeleteNextMachine (IInsertDeleteRequest req, Map.Entry<Integer, String> nextMachine, 
     		String key, String value) throws RemoteException{
 		try{
     		IInsertDeleteRequest reqNextMachine = new InsertDeleteRequest(req.getRequestId(), req.getMachineId() , key, value);
             handleMessage(req, "delete: machine " + this.myId + " - with <" + key + ", "+ req.getKey() + ">  routed to machine address " + nextMachine.getValue(), true);
             if(DebugMode)
             	UnicastRemoteObject.exportObject(reqNextMachine);
         	IDistributedHashTable dhtNextMachine = (IDistributedHashTable)
             	Naming.lookup("rmi://localhost:"+ nextMachine.getValue() +"/DistributedHashTable");
             dhtNextMachine.delete(reqNextMachine);
          // update message received by next machine as original request is not sent to the next machine
             handleMessage(req, reqNextMachine.getMessage(), false);
 		} catch(Exception e) {
             handleMessage(req, "Error-doInsertNextMachine: machine " + this.myId + " - " +  e.getMessage(), true);
         }
     }
     
     /** 
      * create an insert replication request and send the request to update replication servers
      */
     private void doInsertReplication(IInsertDeleteRequest req, String key, boolean dirtyInsert) throws RemoteException{
     	IInsertDeleteReplicationRequest insRepReq = new InsertDeleteReplicationRequest(req.getRequestId(), req.getMachineId(), key, req.getValue(), dirtyInsert);
     	if(DebugMode)
     		UnicastRemoteObject.exportObject(insRepReq);
     	insertReplication(insRepReq);
         handleMessage(req, insRepReq.getMessage(), false);
     }
     
     /** 
      * create an delete query replication request and send the request to update replication servers
      */
     private void doDeleteReplication(IQueryRequest req, String key, String value, boolean dirtyDelete) throws RemoteException {
     	IInsertDeleteReplicationRequest delRepReq = new InsertDeleteReplicationRequest(req.getRequestId(), req.getMachineId(), key, value, dirtyDelete);
     	if(DebugMode)
     		UnicastRemoteObject.exportObject(delRepReq);
     	deleteReplication(delRepReq);
         handleMessage(req, delRepReq.getMessage(), false);
 	}
 
 	
 	/** 
      * creates a deep copy of cache
      */
 	@SuppressWarnings("unchecked")
     private Hashtable<String, ArrayList<String>> deepCopy(Hashtable<String, ArrayList<String>> original){
 		
 		Hashtable<String, ArrayList<String>> copy = new Hashtable<String, ArrayList<String>>(original.size());
 		synchronized (original){
 			for(Map.Entry<String, ArrayList<String>> entry : original.entrySet()) {
 		        copy.put(entry.getKey(), (ArrayList<String>)entry.getValue().clone());
 		    }
 		}
 		return copy;
 	}
 }
