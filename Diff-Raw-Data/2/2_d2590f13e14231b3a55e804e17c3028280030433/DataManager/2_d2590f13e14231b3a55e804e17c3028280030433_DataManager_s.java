 package nyu.ads.conctrl.site;
 
 import java.util.*;
 
 import nyu.ads.conctrl.entity.*;
 import nyu.ads.conctrl.site.entity.*;
 /**
  * DataManager class
  * 
  * hold and manage all data on this site
  * perform data operations, after locks are retrieved by site
  * 
  * @author Yaxing Chen(N16929794)
  *
  */
 public class DataManager {
 
 	private HashMap<String, String> db;//stable storage, actual db on this server, resource=>value
 
 	private String[] uniqueRes; // used when recover, to lock 
 
 
 	/**
 	 * transaction log
 	 * logging all updating operations of each transaction.
 	 * transaction id is key
 	 * 
 	 * used before commit and when recovery
 	 * 
 	 * @see TransactionLogItemEnty
 	 */
 	private HashMap<Integer, ArrayList<TransactionLogItemEnty>> transactionLog; 
 
 	/**
 	 * snapshot queue
 	 * 
 	 * only keep most recent snapshot_qty snapshots
 	 */
 	private int snapshot_qty = 20;
 	private HashMap<String, ArrayList<SnapShotEnty>> snapshots; //resource=>snapshots
 												// snapshots: String[2] = (value, timestamp);
 
 	DataManager() {
 		this.db = new HashMap<String, String>();
 		this.transactionLog = new HashMap<Integer, ArrayList<TransactionLogItemEnty>>();
 		this.snapshots = new HashMap<String, ArrayList<SnapShotEnty>>();
 	}
 	
 	/**
 	 * get replicated resourses
 	 * @return
 	 */
 	public ArrayList<String> getReplicatedResource() {
 		ArrayList<String> res = new ArrayList<String>();
 		Set<Map.Entry<String, String>> entries = this.db.entrySet();
 		for(Map.Entry<String, String> entry : entries) {
 			String resource = entry.getKey();
 			int i = 0;
 			for(; i < this.uniqueRes.length; i ++) {
 				if(resource == this.uniqueRes[i]) {
 					continue;
 				}
 			}
			if(i == this.uniqueRes.length - 1) {
 				res.add(resource);
 			}
 		}
 		return res;
 	}
 
 	/**
 	 * write transaction log
 	 * @param transacId
 	 * @param resource
 	 * @param value
 	 */
 	private void logTransaction(int transacId, String resource, String value) {
 		ArrayList<TransactionLogItemEnty> loginfo = null;
 		if(!transactionLog.containsKey(transacId)) {
 			loginfo = new ArrayList<TransactionLogItemEnty>();
 			transactionLog.put(transacId, loginfo);
 		}
 		loginfo = transactionLog.get(transacId);
 		loginfo.add(new TransactionLogItemEnty(resource, value));
 	}
 
 	/**
 	 * add new resource when initiating site
 	 * @param resName
 	 * @param value
 	 */
 	public void newRes(String resName, String value) {
 		this.db.put(resName, value);
 	}
 
 	/**
 	 * define which resources are unique on this site
 	 * @param uniqueRes
 	 */
 	public void setUniqRes(String[] uniqueRes) {
 		this.uniqueRes = uniqueRes;
 	}
 
 	public String[] getUniqRes() {
 		return this.uniqueRes;
 	}
 
 	/**
 	 * write resource, write into log
 	 * @param resId
 	 * @return 
 	 */
 	public void write(int transacId, String res, String value) {
 		logTransaction(transacId, res, value);
 	}
 
 	/**
 	 * read resource, return read value
 	 * 2 situation (use R(T1, X1) as an example):
 	 * 1) T1 wrote X1 before, T1 holds write lock of X1, then read from log
 	 * 2) T1 never write X1 before, then read from db
 	 * @param transacId
 	 * @param res
 	 * @return String read value
 	 */
 	public String read(int transacId, String res) {
 		ArrayList<TransactionLogItemEnty> history = transactionLog.get(transacId); 
 		if(history != null && history.size() >= 1) {
 			for(int i = history.size() - 1; i >= 0; i --) {
 				if(history.get(i).resource.equals(res)) {
 					return history.get(i).value;
 				}
 			}
 		}
 		return db.get(res);
 	}
 
 	/**
 	 * read only read function
 	 * 
 	 * search for the snapshot list of the resource, return the latest snapshot's value
 	 * @param resourceName
 	 * @param timestamp
 	 * @return
 	 */
 	public String roRead(String resourceName, TimeStamp timestamp) {
 		ArrayList<SnapShotEnty> sList = this.snapshots.get(resourceName);
 		int counter = sList.size() - 1;
 		for(; counter >= 0; counter --) {
 			if(sList.get(counter).timestamp.compareTo(timestamp) <= 0) {
 				return sList.get(counter).value; 
 			}
 		}
 		return this.db.get(resourceName);
 	}
 
 	/**
 	 * abort transaction, 
 	 * clean write log page of this certain transaction
 	 * @param transacId
 	 */
 	public void abortT(int transacId) {
 		transactionLog.remove(transacId);
 	}
 	
 	/**
 	 * abort all uncommitted transactions
 	 * for site fail / recover
 	 */
 	public void abortAllTx() {
 		transactionLog.clear();
 	}
 
 	/**
 	 * commit transaction T
 	 * write log data into real db.
 	 * write commit Log
 	 * clear corresponding recovery locks, if exist
 	 * @param transacId
 	 * @return
 	 */
 	public boolean commitT(int transacId) {
 		ArrayList<TransactionLogItemEnty> writeLog = transactionLog.get(transacId);
 		if(writeLog == null || writeLog.size() == 0) {
 			return true;
 		}
 		for(TransactionLogItemEnty log : writeLog) {
 			db.put(log.resource, log.value);
 		}
 		transactionLog.remove(transactionLog);
 		return true;
 	}
 
 	/**
 	 * return all db resources, that is, committed values
 	 * @return String a structured String that can be parsed by TM
 	 */
 	public String dump() {
 		return db.toString();
 	}
 
 	/**
 	 * return designated resource's committed value
 	 * @param traget resource name
 	 * @return String a structured String that can be parsed by TM
 	 */
 	public String dump(String target) {
 		return target + "=" + db.get(target);
 	}
 
 	/**
 	 * prepare to commit a certain transaction
 	 * return true or false to TM
 	 * @param transacId
 	 * @return
 	 */
 	public boolean prepareCommitT(int transacId) {
 
 		return true;
 	}
 
 	/**
 	 * take a snapshot with current timestamp
 	 * @param timestamp
 	 */
 	public void snapshot() {
 		Set<Map.Entry<String, String>> entries = db.entrySet();
 		TimeStamp now = new TimeStamp();
 		for(Map.Entry<String, String> entry : entries) {
 			String resource = entry.getValue();
 			ArrayList<SnapShotEnty> list = snapshots.get(resource);
 			if(list == null) {
 				list = new ArrayList<SnapShotEnty>();
 				this.snapshots.put(resource, list);
 			}
 			list.add(snapshotGen(resource, now));
 			if(list.size() > snapshot_qty) {
 				list.remove(0);
 			}
 		}
 	}
 	
 	/**
 	 * take a snapshot with given timestamp
 	 * @param String(long) timestamp
 	 */
 	public void snapshot(String timestamp) {
 		Set<Map.Entry<String, String>> entries = db.entrySet();
 		TimeStamp now = new TimeStamp(Long.parseLong(timestamp));
 		for(Map.Entry<String, String> entry : entries) {
 			String resource = entry.getKey();
 			ArrayList<SnapShotEnty> list = snapshots.get(resource);
 			if(list == null) {
 				list = new ArrayList<SnapShotEnty>();
 				this.snapshots.put(entry.getKey(), list);
 			}
 			list.add(snapshotGen(resource, now));
 			if(list.size() > snapshot_qty) {
 				list.remove(0);
 			}
 		}
 	}
 
 	/**
 	 * 
 	 * @param String resource
 	 * @param String timestamp
 	 * @return String[] a snapshot of resource
 	 */
 	private SnapShotEnty snapshotGen(String resource, TimeStamp timestamp) {
 		return new SnapShotEnty(db.get(resource), timestamp);
 	} 
 
 	
 	//===========test funcs==================//
 	public static void main(String[] args) {
 		DataManager dm = new DataManager();
 		String[] uniq = new String[10];
 		int j = 0;
 		
 		//===========initialization testing======//
 		for(int i = 0; i < 10; i ++) {
 			dm.newRes("X" + i, Integer.toString(i));
 			if(i % 2 == 1) {
 				uniq[j ++] = Integer.toString(i);
 			}
 		}
 		dm.setUniqRes(uniq);
 		System.out.println(dm.dump());
 		for(String s : dm.uniqueRes) {
 			if(s != null && s.length() > 0) {
 				System.out.print(s + ",");
 			}
 		}
 		System.out.println();
 		
 		//========snapshots testing======//
 		String timestamp = Long.toString(new TimeStamp().getTime());
 		dm.snapshot(timestamp);
 		dm.printSnapshot();
 		
 		TimeStamp timestamp1 = new TimeStamp();//prepare a read-only transaction
 		
 		//======read, write, commit testing======//
 		dm.write(1, "X2", "6");
 		dm.write(1, "X3", "6");
 		dm.write(1, "X5", "6");
 		dm.write(2, "X6", "66");
 		System.out.println(dm.read(1, "X6"));
 		//expected: 6
 		System.out.println(dm.dump());
 		//expected: {X0=0, X1=1, X2=2, X3=3, X4=4, X5=5, X6=6, X7=7, X9=9, X8=8}
 		dm.commitT(1);
 		System.out.println(dm.dump());
 		//expected: {X0=0, X1=1, X2=6, X3=6, X4=4, X5=6, X6=6, X7=7, X9=9, X8=8}
 		dm.printLog();
 		//expected: 
 		//1:{X2=6, X3=6, X5=6, }
 		//2:{X6=66, }
 		dm.abortT(2);
 		dm.printLog();
 		//expected: 
 		//1:{X2=6, X3=6, X5=6, }
 		
 		
 		//========readonly testing======//
 		String timestamp2 = Long.toString(new TimeStamp().getTime());
 		dm.snapshot(timestamp2);
 		dm.printSnapshot();
 		System.out.println(dm.roRead("X5", timestamp1));
 		//expected: 5
 	}
 
 	/**
 	 * debug function dump transactionLog
 	 */
 	public void printLog() {
 		Set<Map.Entry<Integer, ArrayList<TransactionLogItemEnty>>> entries = this.transactionLog.entrySet();
 		for(Map.Entry<Integer, ArrayList<TransactionLogItemEnty>> entry : entries) {
 			System.out.print(entry.getKey() + ":{");
 			for(TransactionLogItemEnty log : entry.getValue()) {
 				System.out.print(log.resource + "=" + log.value + ", ");
 			}
 			System.out.println("}");
 		}
 	}
 	
 	/**
 	 * debug function dump snapshots
 	 */
 	public void printSnapshot() {		
 		Set<Map.Entry<String, ArrayList<SnapShotEnty>>> entries = this.snapshots.entrySet();
 		for(Map.Entry<String, ArrayList<SnapShotEnty>> entry : entries) {
 			System.out.print(entry.getKey() + ":{");
 			for(SnapShotEnty s : entry.getValue()) {
 				System.out.print(s.value + ":" + s.timestamp.toString() + ",");
 			}
 			System.out.println("}");
 		}
 	}
 }
