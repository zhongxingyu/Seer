 package uk.ac.sussex.asegr3.tracker.client.location;
 
 import java.util.ArrayList;
 import java.util.SortedSet;
 import java.util.TreeSet;
 import java.util.concurrent.Executor;
 import java.util.concurrent.locks.Lock;
 import java.util.concurrent.locks.ReentrantReadWriteLock;
 
 import uk.ac.sussex.asegr3.tracker.client.dto.LocationDto;
 import uk.ac.sussex.asegr3.tracker.client.service.LocationUpdateListener;
 import uk.ac.sussex.asegr3.tracker.client.util.Logger;
 
 /**
  * This cache consumes locations and batches them up into accumulated lists. These lists are then forwarded
  * to a consumer in a single go when:
  * 
  *  <ul>
  *  <li>the consumer is ready to receive new updates. defined by {@link BatchLocationConsumer#isReady()}</li>
  *  <li>the size of the batch reaches that defined by {@link #defaultBatchSize}.</li>
  *  or
  *  <li>the time since the first message and the message being added is greater then the value
  *  defined by {@link #cacheFlushTime}. 
  *  </ul>
  *  
  *  To ensure that the cache does not fill up in the event of a consumer consistantly no being ready, there
  *  is a limit of how many location entries can be held. Once this is exceeded, the cache will drop off
  *  LRU items. Please note that callbacks to the consumer will be made synchronously from calls that this class
  *  receives from the {@link #addLocation(LocationDTO)} method. This does impose a possible limitation that
  *  if this stops receiving updates of locations then, it will also stop sending them, even if there are
  *  entries in the cache.
  *  
  *  All calls to dispatch a batch to a consumer will be done so on an OS managed thread.
  *  
  *  Please note that this class can be used in a multi thread environment
  * 
  * @author andrewhaines
  *
  */
 public class LocationCache implements LocationUpdateListener{
 
 	private final int cacheLimit;
 	private final int defaultBatchSize;
 	private final long cacheFlushTime;
 	private final BatchLocationConsumer consumer;
 	private final SortedSet<LocationDto> queue = new TreeSet<LocationDto>();
 	private int currentBatchVersion = 0;
 	private final Lock readLock;
 	private final Lock writeLock;
 	private final Logger logger;
 	private final Executor executor;
 	
 	public LocationCache(int cacheLimit, int defaultBatchSize, long cacheFlushTime, BatchLocationConsumer consumer, Logger logger, Executor executor) {
 		this.cacheLimit = cacheLimit;
 		this.defaultBatchSize = defaultBatchSize;
 		this.cacheFlushTime = cacheFlushTime;
 		this.consumer = consumer;
 		this.logger = logger;
 		this.executor = executor;
 		
 		ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
 		
 		this.readLock = readWriteLock.readLock();
 		this.writeLock = readWriteLock.writeLock();
 	}
 	
 	private void addLocationAndTryNotification(LocationDto locationDto){
 		
 		if (addLocation(locationDto)){
 			processQueue(queue);
 		}
 	}
 	
 	int getCacheSize(){
 		return queue.size();
 	}
 
 	private boolean addLocation(LocationDto locationDto) {
 		
 		boolean added;
 		writeLock.lock();
 		try{
 			if (!(added = queue.add(locationDto))){
 				logger.debug(LocationCache.class, "location: "+locationDto+" has already been added in this batch");
 			}
 		} finally{
 			writeLock.unlock();
 		}
 			
 		return added;
 	}
 	
 	public void clear(){
 		writeLock.lock();
 		try{
 			queue.clear();
 		} finally{
 			writeLock.unlock();
 		}
 	}
 
 	private void processQueue(SortedSet<LocationDto> queue) {
 		readLock.lock();
 		try{
 			if (queue.size() > cacheLimit){
 				// remove head of queue. As this gets called once for every location this is good enough
				readLock.unlock(); // upgrade lock to write
 				writeLock.lock();
 				try{
					if (queue.size() > cacheLimit){ // recheck in case another thread has altered the queue since we released the read lock
						logger.debug(LocationCache.class, "Trimming location cache as it is over the threshold of: "+cacheLimit);
						queue.remove(queue.first());
					}
 				}
 				finally{
					readLock.lock(); // downgrade to read lock
 					writeLock.unlock();
 				}
 			}
 			
 			if (consumer.isReady()){
 				if (queue.size() >= defaultBatchSize || tooMuchTimePastInQueue(queue)){
					readLock.unlock(); // upgrade to write lock
 					writeLock.lock();
 					try{
						if (queue.size() >= defaultBatchSize || tooMuchTimePastInQueue(queue)){ // re compute in case another thread has changed the state of the queue whilst upgrading
							logger.debug(LocationCache.class, "processing batch of size: "+queue.size());
							processBatch(queue);
						}
 					}
 					finally{
 						readLock.lock();
 						writeLock.unlock();
 					}
 				} else{
 					logger.debug(LocationCache.class, "Not notifying consumer as batch is not at its default size of: "+defaultBatchSize+" ("+queue.size()+") and time difference is not greater then "+cacheFlushTime);
 				}
 			} else{
 				logger.debug(LocationCache.class, "Consumer is not ready to recieve messages");
 			}
 		} finally {
 			readLock.unlock();
 		}
 	}
 
 	private boolean tooMuchTimePastInQueue(SortedSet<LocationDto> queue) {
 		LocationDto firstLocation = queue.first();
 		LocationDto lastLocation = queue.last();
 		
 		return (lastLocation.getTimestamp() - firstLocation.getTimestamp()) >= cacheFlushTime;
 	}
 
 	private void processBatch(SortedSet<LocationDto> queue) {
 		LocationBatch batch = new LocationBatch(new ArrayList<LocationDto>(queue), currentBatchVersion++);
 		BatchSendingTask worker = new BatchSendingTask(this, batch);
 		executor.execute(worker);
 	}
 
 	@Override
 	public void notifyNewLocation(LocationDto location) {
 		addLocationAndTryNotification(location);
 	}
 
 	private class BatchSendingTask implements Runnable{
 		private final LocationCache cache;
 		private final LocationBatch batch;
 		
 		private BatchSendingTask(LocationCache cache, LocationBatch batch){
 			this.cache = cache;
 			this.batch = batch;
 		}
 
 		@Override
 		public void run() {
 			writeLock.lock();
 			try{
 				boolean isSuccessful = consumer.processBatch(batch);
 				
 				if (isSuccessful){
 					cache.clear();
 				} else {
 					// add back onto queue
 					for (LocationDto location: batch.getLocations()){
 						cache.addLocation(location);
 					}
 				}
 			} finally{
 				writeLock.unlock();
 			}
 		}
 	}
 }
