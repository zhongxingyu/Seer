 package br.ufmg.dcc.vod.ncrawler.master.processor.manager;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.Map;
 import java.util.concurrent.locks.Condition;
 import java.util.concurrent.locks.ReentrantLock;
 
 import com.google.common.annotations.VisibleForTesting;
 
 public class WorkerManagerImpl implements WorkerManager {
 
 	public enum WorkerState {IDLE, BUSY, SUSPECTED}
 
 	private Map<WorkerState, Collection<WorkerID>> stateMap;
 	private Map<String, WorkerID> allocMap;
 	private Map<WorkerID, String> inverseAllocMap;
 	private ReentrantLock lock;
 	private Condition waitCondition;
 	
 	public WorkerManagerImpl(Collection<WorkerID> workerIDs) {
 		this.stateMap = new HashMap<>();
 		
 		this.stateMap.put(WorkerState.IDLE, new LinkedList<WorkerID>());
 		this.stateMap.put(WorkerState.BUSY, new HashSet<WorkerID>());
 		this.stateMap.put(WorkerState.SUSPECTED, new HashSet<WorkerID>());
 		
 		this.stateMap.get(WorkerState.IDLE).addAll(workerIDs);
 		this.allocMap = new HashMap<>();
 		this.inverseAllocMap = new HashMap<>();
 		this.lock = new ReentrantLock();
 		this.waitCondition = this.lock.newCondition();
 	}
 	
 	@Override
 	public WorkerID allocateAvailableExecutor(String crawlID) 
 			throws InterruptedException {
 		try {
 			this.lock.lock();
 		
			while (this.allocMap.containsKey(crawlID))
 				return null;
 			
 			LinkedList<WorkerID> idle = (LinkedList<WorkerID>) 
 					this.stateMap.get(WorkerState.IDLE);
			if (idle.size() == 0)
 				this.waitCondition.await();
 			
 			//Can only reach here with at least one element
 			WorkerID workerID = idle.remove(0);
 			this.stateMap.get(WorkerState.BUSY).add(workerID);
 			this.allocMap.put(crawlID, workerID);
 			this.inverseAllocMap.put(workerID, crawlID);
 			return workerID;
 		} finally {
 			this.lock.unlock();
 		}
 	}
 
 	@Override
 	public boolean freeExecutor(String crawlID) {
 		try {
 			this.lock.lock();
 			WorkerID workerID = this.allocMap.remove(crawlID);
 			if (workerID != null) {
 				String remove = this.inverseAllocMap.remove(workerID);
				assert remove == crawlID;
 				
 				this.stateMap.get(WorkerState.BUSY).remove(workerID);
 				this.stateMap.get(WorkerState.IDLE).add(workerID);
 				this.waitCondition.signal();
 				return true;
 			} else {
 				return false;
 			}
 		} finally {
 			this.lock.unlock();
 		}
 	}
 
 	@Override
 	public void executorSuspected(WorkerID workerID) {
 		try {
 			this.lock.lock();
 			String crawlID = this.inverseAllocMap.remove(workerID);
 			if (crawlID != null) {
 				WorkerID remove = this.allocMap.remove(crawlID);
				assert remove == workerID;
 				this.stateMap.get(WorkerState.BUSY).remove(workerID);
 			} else {
 				this.stateMap.get(WorkerState.IDLE).remove(workerID);
 			}
 			
 			if (!this.stateMap.get(WorkerState.SUSPECTED).contains(workerID))
 				this.stateMap.get(WorkerState.SUSPECTED).add(workerID);
 		} finally {
 			this.lock.unlock();
 		}
 	}
 
 	@Override
 	public void markAvailable(WorkerID workerID) {
 		try {
 			this.lock.lock();
 			
 			if (this.stateMap.get(WorkerState.SUSPECTED).remove(workerID)) {
 				assert !this.inverseAllocMap.containsKey(workerID);
 			}
 			
 			if (this.inverseAllocMap.containsKey(workerID)) {
 				String crawlID = this.inverseAllocMap.remove(workerID);
 				
 				WorkerID removedID = this.allocMap.remove(crawlID);
 				assert removedID.equals(workerID);
 				boolean removedBusy = 
 						this.stateMap.get(WorkerState.BUSY).remove(workerID);
 				assert removedBusy;
 			} else {
 				this.stateMap.get(WorkerState.IDLE).remove(workerID);
 			}
 			
 			assert !this.allocMap.containsKey(workerID);
 			assert !this.stateMap.get(WorkerState.IDLE).contains(workerID);
 			assert !this.stateMap.get(WorkerState.BUSY).contains(workerID);
 			assert !this.stateMap.get(WorkerState.SUSPECTED).contains(workerID);
 			
 			this.stateMap.get(WorkerState.IDLE).add(workerID);
 			this.waitCondition.signal();
 		} finally {
 			this.lock.unlock();
 		}
 	}
 
 	@VisibleForTesting Collection<WorkerID> getByState(WorkerState state) {
 		return this.stateMap.get(state);
 	}
 }
