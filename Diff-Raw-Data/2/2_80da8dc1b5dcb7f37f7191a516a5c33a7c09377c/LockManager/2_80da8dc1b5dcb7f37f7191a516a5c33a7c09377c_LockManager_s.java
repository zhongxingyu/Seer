 package node;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import node.Lock.Type;
 
 public class LockManager {
 
 	private final Map<Integer, Lock> locks = new HashMap<Integer, Lock>();
 	private final Map<String, List<Integer>> tLocks = new HashMap<String, List<Integer>>();
 	
 	private void lock(String transactionId, int start, int length, Type type) {
 		for( int i = start; i < start+length; i++ ) {
 			locks.put(i, new Lock(transactionId, i, type));
			if( tLocks.containsKey(transactionId) )
 				tLocks.put(transactionId, new ArrayList<Integer>());
 			tLocks.get(transactionId).add(i);
 		}
 	}
 	
 	private boolean isLocked(String transactionId, int start, int length, Type type) {
 		for( int i = start; i < start+length; i++ ) {
 			Lock lock = locks.get( i );
 			if( lock != null && !lock.getOwnerTransactionId().equals(transactionId) && ( type == Type.EXCLUSIVE || lock.getType() == Type.EXCLUSIVE ) )
 				return true;
 		}
 		return false;
 	}
 	
 	public synchronized void unlock(String transactionId, int start, int length) {
 		for( int i = start; i < start+length; i++ ) {
 			Lock lock = locks.get(i);
 			
 			if( lock.getOwnerTransactionId().equals(transactionId) ) {
 				locks.remove(i);
 				locks.remove(transactionId);
 			}
 		}
 	}
 	
 	public synchronized void unlock(String transactionId) {
 		List<Integer> list = tLocks.get(transactionId);
 		for (Integer i : list) {
 			unlock(transactionId, i, 1);
 		}
 		
 	}
 
 	public synchronized boolean tryLock(String transactionId, List<ReadItem> items) {
 		for (ReadItem readItem : items) {
 			if( isLocked(transactionId, readItem.getAddress(), readItem.getLength(), Type.SHARED) )
 				return false;
 		}
 		for (ReadItem readItem : items) {
 			lock(transactionId, readItem.getAddress(), readItem.getLength(), Type.SHARED);
 		}
 		return true;
 	}
 
 	public synchronized void releaseLocks(String transactionId, List<ReadItem> items) {
 		for (ReadItem readItem : items) {
 			unlock(transactionId, readItem.getAddress(), readItem.getLength());
 		}
 	}
 
 	public synchronized boolean tryLockToWrite(String transactionId, List<WriteItem> writes) {
 		for (WriteItem writeItem : writes) {
 			if( isLocked(transactionId, writeItem.getAddress(), writeItem.getData().length, Type.EXCLUSIVE) )
 				return false;
 		}
 		for (WriteItem writeItem : writes) {
 			lock(transactionId, writeItem.getAddress(), writeItem.getData().length, Type.EXCLUSIVE);
 		}
 		return true;
 	}
 
 	public synchronized void releaseWriteLocks(String transactionId, List<WriteItem> writes) {
 		for (WriteItem writeItem : writes) {
 			unlock(transactionId, writeItem.getAddress(), writeItem.getData().length);
 		}
 	}
 }
