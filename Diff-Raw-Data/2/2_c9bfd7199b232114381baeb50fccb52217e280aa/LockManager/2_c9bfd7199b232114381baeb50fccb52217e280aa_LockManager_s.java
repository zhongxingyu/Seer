 
 package nyu.ads.conctrl.site;
 
 import java.util.*;
 import nyu.ads.conctrl.entity.*;
 import nyu.ads.conctrl.site.entity.*;
 
 /**
  * Class LockManager
  * 
  * @author Yaxing Chen (16929794)
  */
 public class LockManager {
 	
 	/**
 	 * lock Table
 	 * "resource"=>ArrayList<LockEnty>
 	 * @see LockEnty
 	 */
 	private HashMap<String, ArrayList<LockEnty>> locks; 
 
 	/**
 	 * replicated resources that are locked from reading when site recover
 	 * delete when certain resource is committed written
 	 * locked as -1
 	 */
 	private HashMap<String, Integer> recoverLocks; 
 	
 	public LockManager() {
 		locks = new HashMap<String, ArrayList<LockEnty>>(); 
 		recoverLocks = new HashMap<String, Integer>();
 	}
 	
 	/**
 	 * generates conflicting information and return
 	 * @param String res
 	 * @param int transacId
 	 * @return
 	 */
 	private String conflictRespGen(String res, int transacId) {
 		StringBuilder buffer = new StringBuilder();
 		buffer.append(InstrCode.EXE_RESP + " 0 ");
 		ArrayList<LockEnty> lockInfo = locks.get(res);
 		buffer.append("{");
 		int counter = 0;
 		for(; counter < lockInfo.size();) {
 			LockEnty lock = lockInfo.get(counter);
 			buffer.append(lock.transacId);
 			counter ++;
 			if(counter < lockInfo.size()) {
 				for(; counter < lockInfo.size();) {
 					if(lock.transacId == lockInfo.get(counter).transacId) {
 						counter ++;
 					}
 					else {
 						buffer.append(",");
 						break;
 					}
 				}
 			}
 		}
 		buffer.append("} ");
 		buffer.append(transacId);
 		return buffer.toString();
 	}
 	
 	/**
 	 * first time lock a resource,
 	 * add resource and lock information into lock table
 	 * @param int transacId
 	 * @param String res
 	 * @param boolean isExclusive
 	 * @return void
 	 */
 	private void newLock(int transacId, String res, LockType lockType) {
 		if(locks.containsKey(res)) {
 			ArrayList<LockEnty> lockInfo = locks.get(res);
 			int count = 0;
 			for(LockEnty lock : lockInfo) {
 				if(lock.transacId == transacId && lock.type == lockType) {
 					return;
 				}
 				else if(lock.transacId > transacId) {
 					lockInfo.add(count, new LockEnty(transacId, lockType));
 					return;
 				}
 				count ++;
 			}
 			if(count >= lockInfo.size()) {
 				lockInfo.add(new LockEnty(transacId, lockType));
 			}
 		}
 		else {
 			ArrayList<LockEnty> lockInfo = new ArrayList<LockEnty>();
 			lockInfo.add(new LockEnty(transacId, lockType));
 			locks.put(res, lockInfo);
 		}
 		return;
 	}
 	
 	/**
 	 * request a lock on res for transaction with transacId
 	 * @param transacId
 	 * @param res
 	 * @return String NULL: lock retrieved; "{T1, T2} T3": T3 is conflict with T1, T2 who hold lock
 	 */
 	public String lock(int transacId, String res, LockType requestLockType) {
 		//if there're locks on this resource
 		if(locks.containsKey(res)) {
 			
 			ArrayList<LockEnty> lockInfo = locks.get(res);
 			LockType curLockType = findCurrentLockType(res);
 			
 			if(isLockedByT(res, transacId)) {
 				if(curLockType == LockType.READ && lockInfo.size() == 1 || curLockType == LockType.WRITE) {
 					newLock(transacId, res, requestLockType);
 					return null;
 				}
 				else if(lockInfo.size() > 1 && requestLockType == LockType.READ) {
 					return null;
 				}
 				else {
 					return conflictRespGen(res, transacId);
 				}
 			}
 			else if(curLockType == LockType.WRITE || requestLockType == LockType.WRITE) {
 				return conflictRespGen(res, transacId);
 			}
 			else {
 				newLock(transacId, res, requestLockType);
 				return null;
 			}
 		}
 		//if recover lock
 		else if(recoverLocks.containsKey(res)) {
 			if(requestLockType == LockType.WRITE) {
 				newLock(transacId, res, requestLockType);
 				recoverLocks.remove(res);
 				return null;
 			}
 			else {
 				return InstrCode.EXE_RESP + " 0";
 			}
 		}
 		else {
 			newLock(transacId, res, requestLockType);
 			return null;
 		}
 	}
 	
 	/**
 	 * judge whether a resource is locked by a certain transaction
 	 * @param String res   resource name
 	 * @param int transacId transaction id
 	 * @return boolean
 	 */
 	private boolean isLockedByT(String res, int transacId) {
 		ArrayList<LockEnty> lockInfo = locks.get(res);
 		for(LockEnty curLock : lockInfo) {
 			if(curLock.transacId == transacId) {
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	/**
 	 * traverse lock list for current resource (lock exist):
 	 * if there are multiple read locks from different transactions, then current lock type is READ
 	 * if there is one write lock, then current lock type is write 
 	 * @return LockType
 	 */
 	private LockType findCurrentLockType(String res) {
 		ArrayList<LockEnty> lockInfo = locks.get(res); 
 		for(LockEnty lock : lockInfo) {
 			if(lock.type == LockType.WRITE) {
 				return LockType.WRITE;
 			}
 		}
 		return LockType.READ;
 	}
 	
 	/**
 	 * unlock all resources locked by a transaction T
 	 * @param int transacId
 	 */
 	public void unlockTransac(int transacId) {
 		Iterator<Map.Entry<String, ArrayList<LockEnty>>> locksIt = locks.entrySet().iterator();
 		
 		while(locksIt.hasNext()) {
 			Map.Entry<String, ArrayList<LockEnty>> entry = (Map.Entry<String, ArrayList<LockEnty>>)locksIt.next();
 			ArrayList<LockEnty> lockInfo = entry.getValue();
 			int counter = 0;
 			for(;counter < lockInfo.size(); counter ++) {
 				LockEnty lock = lockInfo.get(counter);
 				if(lock.transacId == transacId) {
 					lockInfo.remove(counter);
					counter = 0;
 					if(lockInfo.isEmpty()) {
 						locks.remove(entry.getKey());
 						break;
 					}
 				}
 			}
 			if(locks.isEmpty()) {
 				break;
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
 		recoverLocks.clear();
 	}
 	
 
 	
 	/**
 	 * test main
 	 */
 	public static void main(String[] args) {
 		LockManager lm = new LockManager();
 		//System.out.println(lm.lock(2, "X1", LockType.WRITE));
 		System.out.println(lm.lock(2, "X1", LockType.READ));
 		System.out.println(lm.lock(0, "X1", LockType.READ));
 		System.out.println(lm.lock(1, "X1", LockType.WRITE));
 		lm.testOutputLocks();
 		lm.unlockTransac(0);
 		lm.testOutputLocks();
 		lm.unlockTransac(2);
 		lm.testOutputLocks();
 	}
 	
 	/**
 	 * test output
 	 */
 	private void testOutputLocks() {
 		System.out.println("locks:{");
 		Set<Map.Entry<String, ArrayList<LockEnty>>> entries = locks.entrySet();
 		for(Map.Entry<String, ArrayList<LockEnty>> entry : entries) {
 			System.out.print(entry.getKey() + ": { ");
 			ArrayList<LockEnty> lockInfo = entry.getValue();
 			for(LockEnty lock : lockInfo) {
 				System.out.print(lock.transacId + "=" + lock.type + ",");
 			}
 			System.out.println("}");
 		}
 		System.out.println("}");
 	}
 }
