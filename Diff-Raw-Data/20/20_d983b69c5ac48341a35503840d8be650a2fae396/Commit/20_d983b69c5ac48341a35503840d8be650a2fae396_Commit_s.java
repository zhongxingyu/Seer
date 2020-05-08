 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 
 
 public class Commit implements Iterable<Integer>{
 	
 	private Log log;
 	
 	private boolean abort;
 	/**
 	 * key = txn client is waiting on
 	 */
 	private Set<Integer> waitTXN;
 	/**
 	 * key = client that this commit is waiting on
 	 */
 	private Set<Integer> waitAddr;
 	/**
 	 * addr -> txnID
 	 */
 	private Map<Integer, Integer> txnIDLookup;
 	
 	private int seqNum;
 	
 	public Commit(int client, Log log, Map<Integer, Boolean> txnLog, int seqNum){
 		this.seqNum = seqNum;
 		this.log = log;
 		this.abort = false;
 		this.waitTXN = new HashSet<Integer>();
 		this.waitAddr = new HashSet<Integer>();
 		this.txnIDLookup = new HashMap<Integer, Integer>();
 		
 		for(MasterFile f : log){
 			Update u = log.getInitialVersion(f);
 			int addr = u.source % RIONode.NUM_NODES;
			if(f.getState() == File.INV || (txnLog.containsKey(u.source) && !txnLog.get(u.source)) || addr == -1 && log.hasReads(f)){
 				//Dep transaction aborted -> this one must also abort
 				this.abort = true;
 				return;
 			}else if(u.source == -1){
 				//Shouldn't ever happen
 			}else if(u.source > 0){
 				//this client didn't get its initial version from the server
 				if(u.version == f.getVersion() && f.getLastCommitter() != u.source){
 					//two nodes wrote the same version, and the one you don't depend on committed first
 					//but the node you depend on hasn't aborted yet/attempted to commit
 					this.abort = true;
 				}else if(!txnLog.containsKey(u.source)){
 					//if there are reads or writes that depend on an uncommitted and unaborted transaction
 					this.waitTXN.add(u.source);
 					this.waitAddr.add(addr);
 					this.txnIDLookup.put(addr, u.source);
 				}else if(u.version < f.getVersion() && log.hasWrites(f)){
 					//the version you wrote to is old
 					this.abort = true;
 				}
 			}else if(u.source == TransactionLayer.MASTER_NODE && u.version < f.getVersion() && log.hasWrites(f)){
 				//two nodes wrote same version from the master, but the other one committed first
 				this.abort = true;
 			}
 		}
 	}
 	
 	public boolean isDepOn(int txnID){
 		return this.waitTXN.contains(txnID);
 	}
 	
 	public boolean isWaitingFor(int addr){
 		return this.waitAddr.contains(addr);
 	}
 	
 	public int getDepTXNID(int addr){
 		return this.txnIDLookup.get(addr);
 	}
 	
 	public Log getLog(){
 		return this.log;
 	}
 	
 	public Iterator<Integer> iterator(){
 		return this.waitAddr.iterator();
 	}
 	
 	public void remove(int txnID){
 		this.waitTXN.remove(txnID);
 		this.waitAddr.remove(txnID % RIONode.NUM_NODES);
 	}
 	
 	public boolean isWaiting(){
 		return !this.waitAddr.isEmpty();
 	}
 	
 	public boolean abort(){
 		return this.abort;
 	}
 	
 	public int getSeqNum(){
 		return this.seqNum;
 	}
 }
