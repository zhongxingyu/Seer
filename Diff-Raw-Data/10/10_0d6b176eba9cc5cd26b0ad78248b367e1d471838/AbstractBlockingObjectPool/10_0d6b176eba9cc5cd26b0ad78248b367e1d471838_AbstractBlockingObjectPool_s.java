 /*
  * Copyright 2010 the original author or authors.
  * Copyright 2009 Paxxis Technology LLC
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.paxxis.cornerstone.common;
 
 import java.util.ArrayDeque;
 import java.util.ArrayList;
 import java.util.Deque;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.log4j.Logger;
 
 
 /**
  * 
  * @author Matthew Pflueger
  */
 public abstract class AbstractBlockingObjectPool<T> extends CornerstoneConfigurable {
 
 	private static final Logger logger = Logger.getLogger(AbstractBlockingObjectPool.class);
 
 	public static class PoolEntry<T> {
 		private T object;
         private long timestamp;
 
 		public PoolEntry(T object) {
 			this.object = object;
 			stamp();
 		}
 
 		public T getObject() {
 			return object;
 		}
 
         public long getTimestamp() {
             return this.timestamp;
         }
 
         public void stamp() {
             this.timestamp = System.currentTimeMillis();
         }
         
         public void shutdown() {
 			this.object = null;
 		}
 
 		public void onReturn() {            
 		}
 
 	}
 
 	private class WaitingBorrower {
 		public DataLatch latch;
 		public Object borrower;
 
 		public WaitingBorrower(DataLatch latch, Object borrower) {
 			this.latch = latch;
 			this.borrower = borrower;
 		}
 	}
 
 	// free pool
 	private List<PoolEntry<T>> freePool = new ArrayList<PoolEntry<T>>();
 
 	// assigned entries
 	private Map<PoolEntry<T>, Object> activePool = new HashMap<PoolEntry<T>, Object>();
 
 	// abandoned entries
 	private Map<PoolEntry<T>, Object> abandonedPool = new HashMap<PoolEntry<T>, Object>();
 
 	// borrowers waiting for instances to free up
 	private Deque<WaitingBorrower> borrowersInWaiting = new ArrayDeque<WaitingBorrower>();
 
 	// the semaphore for protecting the pools
 	private Object semaphore = new Object();
 
 	private boolean shutdown = false;
 
 	/** the initial pool size */
 	private int poolSize = 1;
 	
 	/** the minimum pool size */
 	private int minPoolSize = -1;
 	
 	/** the maximum pool size */
 	private int maxPoolSize = -1;
 	
 	private long borrowTimeout = 600000;
 
 	private BlockingThreadPoolExecutor executor = null;
 
 	public void setExecutor(BlockingThreadPoolExecutor executor) {
 		this.executor = executor;
 	}
 
     public void setPoolSize(int size) {
         poolSize = size;
     }
 
     public int getPoolSize() {
         return poolSize;
     }
     
     public void setMinPoolSize(int size) {
         minPoolSize = size;
     }
 
     public int getMinPoolSize() {
         return minPoolSize;
     }
     
     public void setMaxPoolSize(int size) {
         maxPoolSize = size;
     }
 
     public int getMaxPoolSize() {
         return maxPoolSize;
     }
     
 	public void setBorrowTimeout(long borrowTimeout) {
 		this.borrowTimeout = borrowTimeout;
 	}
 
 	public boolean isEmpty() {
 		synchronized (semaphore) {
 			return freePool.isEmpty() && activePool.isEmpty();
 		}
 	}
 
 	/**
 	 *
 	 * @param borrower
 	 * @return
 	 */
 	@SuppressWarnings("unchecked")
 	public <P extends PoolEntry<T>> P borrow(Object borrower) {
 		if (borrower == null) {
 			//don't allow null borrowers as we look to make sure we have a borrower on return (for now)
 			throw new NullPointerException("Null borrower");
 		}
 
 		P entry = null;
 		DataLatch latch = null;
 
 		synchronized (semaphore) {
 			if (this.shutdown && borrower != this) {
 				throw new RuntimeException("Object pool is shutdown");
 			}
 
 			if (!freePool.isEmpty()) {
 				// there's a free entry, give it to this borrower.
 				entry = (P) freePool.remove(0);
 				activePool.put(entry, borrower);
 			} else {
 			    // if we haven't reached the upper limit then we create a new one
 			    if ((freePool.size() + activePool.size()) < maxPoolSize) {
 			        entry = (P)createPoolEntry();
 			        activePool.put(entry, borrower);
 			    }
 			}
 
 			if (entry == null) {
 				// the borrower will have to wait until an instance
 				// is returned by another borrower
 				latch = new DataLatch();
 				borrowersInWaiting.add(new WaitingBorrower(latch, borrower));
 			}
 		}
 
 		if (entry == null) {
 	        // We don't want to waitForObject inside the synchronized block, so these are outside
 	        entry = (P) latch.waitForObject(this.borrowTimeout);
 	        if (latch.hasTimedout()) {
 	            throw new TimeoutException("Timeout of " + this.borrowTimeout + " reached waiting for pool entry");
 	        }
 		}
 
         entry.stamp();
 		return validatePoolEntry(entry);
 	}
 
 	/**
 	 * Hook to validate the pool entry before returning it to borrower
 	 * @param entry
 	 * @return a valid pool entry
 	 */
 	protected <P extends PoolEntry<T>> P validatePoolEntry(P entry) {
 		return entry;
 	}
 
 	/**
 	 * Nicely shutdown the pool, may throw a TimeoutException
 	 */
 	public void shutdown() {
 		synchronized (semaphore) {
 			this.shutdown = true;
 		}
 		for (int i = 0; i < poolSize; i++) {
 			borrow(this).shutdown();
 		}
 		synchronized (semaphore) {
 			activePool.clear();
 		}
 	}
 
 	/**
 	 * Forcibly shutdown the pool if a nice shutdown fails due to a timeout
 	 */
 	@Override
 	public void destroy() {
 		try {
 			this.shutdown();
 		} catch (TimeoutException te) {
 			logger.error("Timeout during destroy, forcing shutdown");
 			synchronized (semaphore) {
 				Iterator<PoolEntry<T>> i = activePool.keySet().iterator();
 				while (i.hasNext()) {
 					try {
 						i.next().shutdown();
 					} catch (Exception e) {
 						//don't care...
 					}
 				}
 				activePool.clear();
 			}
 		}
 
 		super.destroy();
 	}
 
 	//FIXME the borrower is not really needed here - this is just to keep the api the same...
 	public <P extends PoolEntry<T>> void returnInstance(P entry, Object borrower) {
 		returnInstance(entry);
 	}
 
 	/**
 	 * Return a borrowed pool entry
 	 * 
 	 * @param entry the entry to return
 	 */
 	public <P extends PoolEntry<T>> void returnInstance(P entry) {
 		WaitingBorrower waiting = null;
 
 		synchronized (semaphore) {
 		    entry.stamp();
 			entry.onReturn();
 			// take this one off the active pool
 			Object borrower = activePool.remove(entry);
			if (borrower == null) {
				logger.error("Unknown entry returned to pool: " + entry);
				return;
			}
 
 			boolean abandoned = abandonedPool.containsKey(entry);	    
 			if (abandoned) {
 				abandonedPool.remove(entry);
 				return;
 			}
 
 			while (!borrowersInWaiting.isEmpty()) {
 				// give this one to the next borrower
 				waiting = borrowersInWaiting.removeFirst();
 				if (waiting.latch.isSettable()){
 					waiting.latch.setObject(entry);
 					activePool.put(entry, waiting.borrower);
 					return;
 				}
 			}		
 
 			// put it back into the free pool
 			freePool.add(entry);
 		}
 
 
 	}
 
 	public void initialize() {
 		super.initialize();
 		
 		Runnable r = new Runnable() {
 			public void run() {
 				synchronized (semaphore) {
 					// copy everything in the activePool into the abandonedPool so that when the instance is returned it
 					// doesn't get put back in the free pool
 					abandonedPool.putAll(activePool);
 
 					freePool.clear();
 					activePool.clear();
 					shutdown = false;
 
 					for (int i = 0; i < poolSize; i++) {
 						freePool.add(createPoolEntry());
 					}
 				}
 			}
 		};
 
 		if (minPoolSize == -1) {
 		    minPoolSize = poolSize;
 		}
 		
         if (maxPoolSize == -1) {
             maxPoolSize = poolSize;
         }
 
         if (executor == null) {
 			r.run();
 		} else {
 			executor.submit(r);
 		}
 	}
 
 	protected abstract <P extends PoolEntry<T>> P createPoolEntry();
 
 
 	protected Object getSemaphore() {
 		return this.semaphore;
 	}
 
 	protected List<PoolEntry<T>> getFreePool() {
 		return this.freePool;
 	}
 
 	protected Map<PoolEntry<T>, Object> getActivePool() {
 		return this.activePool;
 	}
 }
