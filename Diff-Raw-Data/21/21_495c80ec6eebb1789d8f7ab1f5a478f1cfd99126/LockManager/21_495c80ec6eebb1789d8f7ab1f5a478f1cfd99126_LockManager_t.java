 
 package nyu.ads.conctrl.site;
 
 import java.util.*;
 import nyu.ads.conctrl.entity.*;
 /**
  * Class LockManager
  * 
  * @author Yaxing Chen (16929794)
  */
 public class LockManager {
 	
 	public HashMap<String, ArrayList<String>> locks; // current locks: 
 										  //"resource"=>"transaction id:1/0 (1: lock exclusive, 0: shared)"
 	
 	//public HashMap<String, Integer> locks;	// current locks: "resource"=>"transaction id"
 	
 	public HashMap<String, Integer> recoverLocks; // replicated resources that are locked from reading when site recover
 												  // delete when certain resource is committed written
 												  // locked as -1
 	
 	public LockManager() {
 		locks = new HashMap<String, ArrayList<String>>(); 
 	}
 	
 	/**
 	 * request a lock on res for transaction with transacId
 	 * @param transacId
 	 * @param res
 	 * @return String NULL: lock retrieved; "conflict: T1, T2": T1 is conflict with T2, T2 holds the lock
 	 */
	public String lock(int transacId, String res, boolean isExclusive) {
 		if(locks.containsKey(res)) {
 			ArrayList<String> keyInfo = locks.get(res);
 			String[] tmp = keyInfo.get(0).split(":");
 			int lockType = Integer.parseInt(tmp[1]);
			if(lockType == 1 || isExclusive) {
				return InstrCode.EXE_RESP + " 0 " + locks.get(res).toString() + " " + transacId;
 			}
 			else {
				keyInfo.add(lockGen(transacId, isExclusive));
				return null;
 			}
 		}
 		else if(recoverLocks.containsKey(res)) {
 			return "";
 		}
		else {
			ArrayList<String> newLock = new ArrayList<String>();
			newLock.add(lockGen(transacId, isExclusive));
			locks.put(res, newLock);
 			return null;
 		}
 	}
 	
 	/**
 	 * unlock a certain resource
 	 * @param res resource name
 	 */
 	public void unlockRes(String res) {
 		locks.remove(res);
 	}
 	
 	/**
 	 * unlock all resources locked by a transaction T
 	 * @param int transacId
 	 */
 	public void unlockTransac(int transacId) {
 		Set<Map.Entry<String, Integer>> entries = locks.entrySet();
 		for(Map.Entry<String, Integer> entry : entries) {
 			int id = entry.getValue();
 			if(id == transacId) {
 				locks.remove(entry.getKey());
 			}
 		}
 	}
 	
 	/**
 	 * lock certain resource for reading
 	 * @param String res resource Name
 	 */
 	public void recoverLock(String res) {
 		recoverLocks.put(res, -1);
 	}
 	
 	/**
 	 * clear all lock information when site failed
 	 */
 	public void clearLocks() {
 		locks.clear();
 	}
 	
 	private String lockGen(int transacId, boolean isExclusive) {
		return Integer.toString(transacId) + ":" + (isExclusive ? 1 : 0);
 	}
 }
