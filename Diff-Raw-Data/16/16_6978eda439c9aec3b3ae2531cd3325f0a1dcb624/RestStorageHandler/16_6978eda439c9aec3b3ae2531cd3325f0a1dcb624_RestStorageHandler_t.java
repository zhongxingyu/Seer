 /*
  * Copyright 2013 Tim Roes <tim.roes@inovex.de>.
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
 package de.inovex.andsync.manager;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 import java.util.concurrent.ScheduledFuture;
 import com.mongodb.BasicDBObject;
 import java.util.Map;
 import java.util.Set;
 import android.util.Log;
 import com.mongodb.BasicDBList;
 import de.inovex.andsync.cache.Cache;
 import com.mongodb.DBObject;
 import com.mongodb.DBRef;
 import de.inovex.andsync.AndSyncApplication;
 import de.inovex.andsync.cache.CacheDocument;
 import de.inovex.andsync.rest.RestClient;
 import de.inovex.andsync.rest.RestClient.RestResponse;
 import de.inovex.andsync.rest.RestException;
 import de.inovex.andsync.util.Base64;
 import de.inovex.andsync.util.BsonConverter;
 import de.inovex.andsync.util.TimeUtil;
 import de.inovex.jmom.FieldList;
 import de.inovex.jmom.Storage;
 import java.net.HttpURLConnection;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentLinkedQueue;
 import java.util.concurrent.Executors;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.TimeUnit;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.apache.lucene.util.NamedThreadFactory;
 import org.bson.types.BasicBSONList;
 import org.bson.types.ObjectId;
 import static de.inovex.andsync.Constants.*;
 
 /**
  *
  * @author Tim Roes <tim.roes@inovex.de>
  */
 class RestStorageHandler implements Storage.DBHandler {
 
 	private RepeatingRestClient mRest;
 	private CallCollector mCallCollector;
 	private Cache mCache;
 	private SharedPreferences mPrefs;
 
 	public RestStorageHandler(RestClient restClient, Cache cache) {
 		assert restClient != null && cache != null;
 		this.mCallCollector = new CallCollector();
 		this.mRest = new RepeatingRestClient(restClient);
 		this.mCache = cache;
 		this.mPrefs = AndSyncApplication.getAppContext().getSharedPreferences(
 				RestStorageHandler.class.getName(), Context.MODE_PRIVATE);
 		untransmitted();
 	}
 	
 	/**
 	 * Transmit all untransmitted changes or newly created objects in another {@link Thread}
 	 * to the server.
 	 */
 	private synchronized void untransmitted() {
 		
 		new Thread(new Runnable() {	
 			public void run() {
 			
 				// Get a list of all untransmitted objects
 				Collection<CacheDocument> untransmitted = mCache.getUntransmitted();
 				for(CacheDocument doc : untransmitted) {
 					// Decide what to do with the object depending on its transmitted state
 					switch(doc.getState()) {
 						case DELETED:
 							deleteObject(doc.getCollection(), (ObjectId)doc.getDBObject().get(MONGO_ID));
 							break;
 						case NEVER_TRANSMITTED:
 							newObject(doc.getCollection(), doc.getDBObject());
 							break;
 						case UPDATE_NOT_TRANSMITTED:
 							updateObject(doc.getCollection(), doc.getDBObject());
 							break;
 					}
 				}
 				
 			}
 		}).start();
 		
 	}
 
 	/**
 	 * Updates the change of an object to the server via a POST call. The call will be stored in
 	 * the {@link #mCallCollector} so that multiple POST calls can be collected and bundled.
 	 * 
 	 * The call collector will make sure to mark the object as transmitted as soon as it has been.
 	 * 
 	 * This method will return immediately, before the object has been transmitted to the server.
 	 * 
 	 * @param collection
 	 * @param dbo 
 	 */
 	private void updateObject(final String collection, final DBObject dbo) {
 		mCallCollector.postCall(collection, dbo);
 	}
 
 	/**
 	 * Transfer an object to the server, that has just been created on that device and never
 	 * has been transfered to the server.
 	 * 
 	 * This will add a PUT call to the {@link #mCallCollector call collector}, so multiple PUT calls
 	 * can be collected and bundled. The object should have been put to cache (marked as never transmitted)
 	 * before this method is called. The call collector will take care of marking the object as
 	 * transmitted as soon as it has been.
 	 * 
 	 * This method will return immediately, before the object has been transmitted to the server.
 	 * 
 	 * @param collection The collection of that object.
 	 * @param dbo The object to transfer.
 	 */
 	private void newObject(final String collection, final DBObject dbo) {
 		mCallCollector.putCall(collection, dbo);
 	}
 	
 	/**
 	 * Send the server a notification to delete an object. This will add a DELETE call to the 
 	 * {@link #mCallCollector call collector}, so multiple DELETE calls can be collected and bundled.
 	 * The call collector is responsible for finally deleting the object from cache once it has been
 	 * transmitted to server.
 	 * 
 	 * @param collection The collection of that object.
 	 * @param id The {@link ObjectId id} of the object to delete.
 	 */
 	private void deleteObject(final String collection, final ObjectId id) {
 		mCallCollector.deleteCall(collection, id);
 	}
 
 	/**
 	 * Saves an {@link DBObject} into the specified collection. This method will detect if the 
 	 * object has never been transmitted to the server (it doesn't contain an {@link ObjectId}) or
 	 * if it has already been transfered. The object will be stored into the cache (marked as either
 	 * never transmitted or updated) and then either PUT to server or POSTed to server via the
 	 * REST interface.
 	 * 
 	 * This method need to take care of the caching of the object (and not the 
 	 * {@link CacheStorageHandler#onSave(java.lang.String, com.mongodb.DBObject) method) since 
 	 * this is the only place where the difference between UPDATE and CREATE of an object can be
 	 * detected.
 	 * 
 	 * @param collection The name of the collection for that object.
 	 * @param dbo The object to save.
 	 */
 	@Override
 	public void onSave(final String collection, final DBObject dbo) {
 		if (dbo.containsField(MONGO_ID)) {
 			// Object already has an id, update object
 			// Put the object also in cache (see CacheStorageHandler.onSave() for explanation)
 			mCache.putUpdated(collection, dbo);
 			updateObject(collection, dbo);
 		} else {
 			// Object is new in storage
 			// Create an id for the DBObject.
 			dbo.put(MONGO_ID, ObjectId.get());
 			// Put the object also in cache (see CacheStorageHandler.onSave() for explanation)
 			mCache.put(collection, dbo);
 			newObject(collection, dbo);
 		}
 	}
 	
 	public Collection<DBObject> onGet(final String collection, final FieldList fl) {
 		
 		// Get timestamp of last fetch (or 0 if never fetched before)
 		long lastFetched = mPrefs.getLong(collection, 0);
 		
 		RestResponse deletionRes = mRest.get(REST_META_PATH, collection, REST_META_DELETION_PATH);
 		
 		boolean refetchAll;
 		try {
 			refetchAll = deletionRes == null || Long.valueOf(new String(deletionRes.data)) > lastFetched;
 		} catch(Exception ex) {
 			refetchAll = true;
 		}
 			
 		RestResponse response = null;
 		
 		// Either fetch all objects (if objects got deleted, so we can check what to delete form cache later)
 		if(refetchAll) {
 			response = mRest.get(REST_OBJECT_PATH, collection);
 		} else {
 			response = mRest.get(REST_OBJECT_PATH, collection, REST_MTIME_PATH, String.valueOf(lastFetched));
 		}
 
 		try {
 			long lastModification = Long.valueOf(response.headers.get(HTTP_MODIFIED_HEADER).get(0));
 			// Store timestamp of the last modification (taken from http header) as last fetch,
 			// so next call will only get objects from that 
 			mPrefs.edit().putLong(collection, lastModification).commit();
 		} catch(Exception ex) {
 			Log.w(LOG_TAG, String.format("Could not save last modification time from server. "
 					+ "Client will fetch these objects from server again with the next call. "
 					+ "[Caused by: %s]", ex.getMessage()));
 		}
 		
 		final List<DBObject> objects = (response.data == null || response.code == HttpURLConnection.HTTP_NO_CONTENT)
 				? new ArrayList<DBObject>(0) : BsonConverter.fromBsonList(response.data);
 		
 		long beginCacheUpdate = TimeUtil.getTimestamp();
 
 		// Save all retrieved documents in cache
 		mCache.putTransmitted(collection, objects);
 		
 		if(refetchAll) {
 			// Delete all objects from cache, that doesn't exist on the server anymore
 			// -> Haven't been updated in this session (so update timestamp is older than beginCacheUpdate
 			// Only do this when we fetched all objects, otherwise we would delete here a lot of 
 			// objects that just hasn't been transfered in this call from the server, but still exists.
 			mCache.deleted(collection, beginCacheUpdate);
 		}
 
 		return objects;
 
 	}
 
 	public DBObject onGetById(String collection, ObjectId id) {
 		return mCallCollector.callForId(collection, id);
 	}
 
 	public DBRef onCreateRef(String collection, DBObject dbo) {
 		return new DBRef(null, collection, dbo.get("_id"));
 	}
 
 	public DBObject onFetchRef(DBRef dbref) {
 
 		if (dbref == null || dbref.getId() == null) {
 			return null;
 		}
 
 		DBObject dbobj = mCallCollector.callForId(dbref.getRef(), (ObjectId) dbref.getId());
 		mCache.putTransmitted(dbref.getRef(), dbobj);
 		return dbobj;
 
 	}
 
 	public void onDelete(String collection, ObjectId oi) {
 		deleteObject(collection, oi);
 	}
 
 	private interface RestCall {
 		RestResponse doCall() throws RestException;
 	}
 
 	/**
 	 * Keeps retrying REST calls to the server if connection fails.
 	 */
 	private class RepeatingRestClient extends RestClient {
 
 		/**
 		 * The wrapped {@link RestClient} to use for communication.
 		 */
 		private RestClient mWrapped;
 		/**
 		 * The maximum delay in milliseconds after trying a call again.
 		 */
 		private final long MAX_RETRY_TIME = 60000;
 		private final int MAX_RETRIES = 10;
 		
 		public RepeatingRestClient(RestClient wrappedClient) {
 			assert mWrapped != null;
 			mWrapped = wrappedClient;
 		}
 
 		@Override
 		public RestResponse get(final String... path) {
 			return call(new RestCall() {
 				public RestResponse doCall() throws RestException {
 					return mWrapped.get(path);
 				}
 			});
 		}
 
 		@Override
 		public RestResponse delete(final String... path) {
 			return call(new RestCall() {
 				public RestResponse doCall() throws RestException {
 					return mWrapped.delete(path);
 				}
 			});
 		}
 
 		@Override
 		public RestResponse put(final byte[] data, final String... path) {
 			return call(new RestCall() {
 				public RestResponse doCall() throws RestException {
 					return mWrapped.put(data, path);
 				}
 			});
 		}
 
 		@Override
 		public RestResponse post(final byte[] data, final String... path) {
 			return call(new RestCall() {
 				public RestResponse doCall() throws RestException {
 					return mWrapped.post(data, path);
 				}
 			});
 		}
 
 		/**
 		 * Retry the given call until it succeeds, or the maximum limit of tries has been exceeded.
 		 * Double the delay every time a call fails, until the {@link #MAX_RETRY_TIME maximum
 		 * time limit} has been reached.
 		 * 
 		 * @param call The call to repeat.
 		 * @return The response from that call.
 		 */
 		private RestResponse call(RestCall call) {
 			RestResponse response = null;
 			long wait = 2000;
 			int retry = 0;
 			do {
 				try {
 					response = call.doCall();
 				} catch (RestException ex) {
 					Log.d(LOG_TAG, String.format("Could not make REST call. Trying again in %d ms. "
 							+ "[Caused by %s]", wait, ex.getCause().getMessage()));
 					try {
 						Thread.sleep(wait);
 						wait = Math.min(wait * 2, MAX_RETRY_TIME);
 					} catch (InterruptedException ex1) {
 						Logger.getLogger(RestStorageHandler.class.getName()).log(Level.SEVERE, null, ex1);
 					}
 				}
 				retry++;
 			} while ((response == null || response.code < 200 || response.code >= 300) && retry < MAX_RETRIES);
 			return response;
 		}
 	}
 
 	/**
 	 * Collects different calls to the REST interface and try to minimize the required HTTP connections,
 	 * by bunching calls together.
 	 */
 	private class CallCollector {
 
 		/**
 		 * Limit of calls that get cached for one collection before sending a REST request.
 		 */
 		private final static int CALL_COLLECT_LIMIT = 100;
 		
 		/**
 		 * Time limit after which a REST call will be made (even when less than {@link #CALL_COLLECT_LIMIT}
 		 * calls are pending.
 		 */
 		private final static int CALL_COLLECT_TIME_LIMIT = 3;
 		
 		/**
 		 * A list of {@link ObjectId} that are waiting to be fetched from server. Separated by their
 		 * collection.
 		 */
 		private Map<String, Set<ObjectId>> mIdCalls = new ConcurrentHashMap<String, Set<ObjectId>>();
 		
 		/**
 		 * A map of all collections and their pending PUT calls.
 		 */
 		private Map<String, Set<DBObject>> mPutCalls = new ConcurrentHashMap<String, Set<DBObject>>();
 		
 		/**
 		 * A map of all collections and their pending POST calls.
 		 */
 		private Map<String, Set<DBObject>> mPostCalls = new ConcurrentHashMap<String, Set<DBObject>>();
 		
 		private Map<String, Set<ObjectId>> mDeleteCalls = new ConcurrentHashMap<String, Set<ObjectId>>();
 		
 		/**
 		 * The {@link DBObject DBObjects} returned from the server for each {@link ObjectId}.
 		 */
 		private Map<ObjectId, DBObject> mResults = new ConcurrentHashMap<ObjectId, DBObject>();
 	
 		/**
 		 * The waiting locks for each call made. Each call holds a list of locks for all pending calls 
 		 * to this {@link ObjectId}.
 		 */
 		private Map<ObjectId, Collection<Object>> mIdLocks = Collections.synchronizedMap(new HashMap<ObjectId, Collection<Object>>());
 	
 		/**
 		 * This is a map containing a lock for each collection. This is used when the call for a collection
 		 * is made and when modifying the {@link #mIdCalls pending calls} for that collection.
 		 * So the pending call ids aren't modified while reading from the list to fetch them via REST.
 		 */
 		private Map<String, Object> mCollectionLocks = Collections.synchronizedMap(new HashMap<String, Object>());
 	
 		/**
 		 * This lock is used when modifying the periodic check for pending calls.
 		 * @see #mScheduled
 		 * @see #mCheckForPendingCalls
 		 * @see #mExecutor
 		 */
 		private final Object mSchedulerLock = new Object();
 	
 		/**
 		 * This lock is used when modifying the {@link #mIdLocks id locks}.
 		 */
 		private final Object mLockCreationLock = new Object();
 	
 		/**
 		 * This lock is used when modifying the {@link #mResults results list}.
 		 */
 		private final Object mResultsLock = new Object();
 		private ScheduledFuture<?> mScheduled;
 		private final ScheduledExecutorService mExecutor = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("Periodic AndSync Thread "));
 		private final Runnable mCheckForPendingCalls = new Runnable() {
 
 			public void run() {
 				
 				// Check for pending put calls.
 				for(String collection : mPutCalls.keySet()) {
 					if(numPendingPutCalls(collection) > 0) {
 						doPutCalls(collection);
 					}
 				}
 				
 				// Check for pending post calls.
 				for(String collection : mPostCalls.keySet()) {
 					if(numPendingPostCalls(collection) > 0) {
 						doPostCalls(collection);
 					}
 				}
 				
 				// Check if any pending calls exist. If they do, do REST calls.
 				for (String collection : mIdCalls.keySet()) {
 					if (numPendingIdCalls(collection) > 0) {
 						doGetByIdCalls(collection);
 					}
 				}
 				
 				// Check for pending deleted calls.
 				for(String collection : mDeleteCalls.keySet()) {
 					if(numPendingDeleteCalls(collection) > 0) {
 						doDeleteCalls(collection);
 					}
 				}
 
 				// Check if any calls are left over, if so reschedule the periodic check, if not
 				// don't do any further periodic check.
 				synchronized (mSchedulerLock) {
 					if (hasPendingCalls()) {
 						mScheduled = mExecutor.schedule(mCheckForPendingCalls, CALL_COLLECT_TIME_LIMIT,
 								TimeUnit.SECONDS);
 					} else {
 						mScheduled = null;
 					}
 				}
 
 			}
 		};
 
 		/**
 		 * Checks whether any pending call exists. This will check all type of pending calls and
 		 * return true, if any call (no matter what type) is pending.
 		 * 
 		 * @return Whether any pending call exists.
 		 */
 		private boolean hasPendingCalls() {
 			for (Set<ObjectId> pending : mIdCalls.values()) {
 				if (pending.size() > 0) return true;
 			}
 			for(Set<DBObject> pending : mPostCalls.values()) {
 				if(pending.size() > 0) return true;
 			}
 			for(Set<DBObject> pending : mPutCalls.values()) {
 				if(pending.size() > 0) return true;
 			}
 			for(Set<ObjectId> pending : mDeleteCalls.values()) {
 				if(pending.size() > 0) return true;
 			}
 			return false;
 		}
 
 		private int numPendingIdCalls(String collection) {
 			Set<ObjectId> calls = mIdCalls.get(collection);
 			return calls != null ? mIdCalls.get(collection).size() : 0;
 		}
 		
 		private int numPendingPutCalls(String collection) {
 			Set<DBObject> calls = mPutCalls.get(collection);
 			return calls != null ? calls.size() : 0;
 		}
 		
 		private int numPendingPostCalls(String collection) {
 			Set<DBObject> calls = mPostCalls.get(collection);
 			return calls != null ? calls.size() : 0;
 		}
 		
 		private int numPendingDeleteCalls(String collection) {
 			Set<?> calls = mDeleteCalls.get(collection);
 			return calls != null ? calls.size() : 0;
 		}
 		
 		private <T> void savePendingCall(Map<String,Set<T>> pendingCache, String collection, T object) {
 			synchronized(getCollectionLock(collection)) {
 				Set<T> pending = pendingCache.get(collection);
 				if(pending == null) {
 					pending = Collections.newSetFromMap(new ConcurrentHashMap<T, Boolean>());
 					pendingCache.put(collection, pending);
 				}
 				pending.add(object);
 			}
 		}
 		
 		public void putCall(final String collection, final DBObject dbo) {
 			
 			new Thread(new Runnable() {
 
 				public void run() {
 					savePendingCall(mPutCalls, collection, dbo);
 
 					if(numPendingPutCalls(collection) >= CALL_COLLECT_LIMIT) {
 						doPutCalls(collection);
 					}
 
 					scheduleCall();
 				}
 				
 			}).start();
 			
 		}
 		
 		public void postCall(final String collection, final DBObject dbo) {
 			
 			new Thread(new Runnable() {
 
 				public void run() {
 					savePendingCall(mPostCalls, collection, dbo);
 					
 					if(numPendingPostCalls(collection) >= CALL_COLLECT_LIMIT) {
 						doPostCalls(collection);			
 					}
 					
 					scheduleCall();
 					
 				}
 				
 			}).start();
 			
 		}
 		
 		public void deleteCall(final String collection, final ObjectId id) {
 			
 			new Thread(new Runnable() {
 
 				public void run() {
 					savePendingCall(mDeleteCalls, collection, id);
 					
 					if(numPendingDeleteCalls(collection) >= CALL_COLLECT_LIMIT) {
 						doDeleteCalls(collection);
 					}
 					
 					scheduleCall();
 				}
 
 			}).start();
 			
 		}
 
 		public DBObject callForId(String collection, ObjectId id) {
 
 			// Add the call to the queue
 			savePendingCall(mIdCalls, collection, id);
 
 			scheduleCall();
 
 			Object lockForId = newLock(id);
 			synchronized (lockForId) {
 				// Call if we have enough requests collected
 				if (numPendingIdCalls(collection) >= CALL_COLLECT_LIMIT) {
 					doGetByIdCalls(collection);
 				}
 
 				while (!mResults.containsKey(id)) {
 					try {
 						lockForId.wait(10000);
 					} catch (InterruptedException ex) {
 						// Do nothing
 						//Log.w("ANDSYNC", String.format("Thread got interrupted %s", Thread.currentThread().getId()));
 					}
 				}
 
 				boolean removeObject = false;
 				synchronized (mLockCreationLock) {
 					mIdLocks.get(id).remove(lockForId);
 					if (mIdLocks.get(id) != null && mIdLocks.get(id).isEmpty()) {
 						mIdLocks.remove(id);
 						removeObject = true;
 					}
 					synchronized (mResultsLock) {
 						DBObject dbo = (removeObject) ? mResults.remove(id) : mResults.get(id);
 						return dbo;
 					}
 				}
 
 			}
 
 		}
 		
 		private void scheduleCall() {
 			synchronized (mSchedulerLock) {
 				if (mScheduled == null) {
 					mScheduled = mExecutor.schedule(mCheckForPendingCalls,
 							CALL_COLLECT_TIME_LIMIT, TimeUnit.SECONDS);
 				} else {
 					if (mScheduled.cancel(true)) {
 						mScheduled = mExecutor.schedule(mCheckForPendingCalls, CALL_COLLECT_TIME_LIMIT, TimeUnit.SECONDS);
 					}
 				}
 			}
 		}
 
 		private synchronized Object getCollectionLock(String collection) {
 			Object lock = mCollectionLocks.get(collection);
 			if (lock == null) {
 				lock = new Object();
 				mCollectionLocks.put(collection, lock);
 			}
 			return lock;
 		}
 
 		private Object newLock(ObjectId id) {
 			synchronized (mLockCreationLock) {
 				Collection<Object> locksForId = mIdLocks.get(id);
 				if (locksForId == null) {
 					locksForId = new ConcurrentLinkedQueue<Object>();
 					mIdLocks.put(id, locksForId);
 				}
 				Object newLock = new Object();
 				locksForId.add(newLock);
 				return newLock;
 			}
 		}
 		
 		/**
 		 * Executes all pending PUT calls for the specified collection in a new {@link Thread}.
 		 * The new Thread will synchronize on the collection lock so that adding calls to the collection
 		 * is blocked.
 		 * 
 		 * @param collection The collection name.
 		 */
 		private void doPutCalls(final String collection) {
 			
 			new Thread(new Runnable() {
 
 				public void run() {
 					
 					synchronized(getCollectionLock(collection)) {
 					
 						Set<DBObject> putCalls = mPutCalls.get(collection);
 						
 						if(putCalls == null || putCalls.size() <= 0) return;
 						
 						BasicBSONList bsonList = new BasicBSONList();
 						for(DBObject call : putCalls) {
 							bsonList.add(call);
 						}
 						
 						if(mRest.put(BsonConverter.bsonObjectAsBytes(bsonList), REST_OBJECT_PATH, collection) != null) {
 							for(DBObject call : putCalls) {
 								mCache.putTransmitted(collection, call);
 							}
 							mCache.commit();
 							putCalls.clear();
 						}
 								
 					}
 				}
 				
 			}).start();
 	
 		}
 		
 		private void doPostCalls(final String collection) {
 			
 			new Thread(new Runnable() {
 
 				public void run() {
 					synchronized(getCollectionLock(collection)) {
 						
 						Set<DBObject> postCalls = mPostCalls.get(collection);
 						
 						if(postCalls == null || postCalls.size() <= 0) return;
 						
 						BasicBSONList bsonList = new BasicBSONList();
 						for(DBObject call : postCalls) {
 							bsonList.add(call);
 						}
 						
 						if(mRest.post(BsonConverter.bsonObjectAsBytes(bsonList), REST_OBJECT_PATH, collection) != null) {
 							for(DBObject call : postCalls) {
 								mCache.putTransmitted(collection, call);
 							}
 							mCache.commit();
 							postCalls.clear();
 						}
 						
 					}
 				}
 				
 			}).start();
 
 		}

 		private void doDeleteCalls(final String collection) {
 			new Thread(new Runnable() {
 
 				public void run() {
 					synchronized(getCollectionLock(collection)) {
 						Set<ObjectId> deletions = mDeleteCalls.get(collection);
 						
 						if(deletions == null || deletions.size() <= 0) return;
 						
 						for(ObjectId id : deletions) {
 							if(mRest.delete(REST_OBJECT_PATH, collection, id.toString()) != null) {
 								mCache.deleted(collection, id);
 							}
 						}
 						
 						deletions.clear();
 					}
 				}
 				
 			}).start();
 		}		
 		
 		private void doGetByIdCalls(final String collection) {
 			
 			Runnable r = new Runnable() {
 
 				public void run() {
 		
 					synchronized(getCollectionLock(collection)) {
 					
 						Set<ObjectId> requestedIds = mIdCalls.get(collection);
 
 						// Build list of all ids to fetch
 						BasicDBList keyList = new BasicDBList();
 						for (ObjectId id : requestedIds) {
 							keyList.add(id);
 						}
 
 						RestResponse response = mRest.get(REST_OBJECT_PATH, collection,
 								Base64.encode(BsonConverter.toByteArray(keyList), 0));
 
 						if (response.code != 200) {
 							Log.w(LOG_TAG, String.format("Server returned error code %d.", response.code));
 							return;
 						}
 
 						BasicDBObject objects = (BasicDBObject) BsonConverter.fromBson(response.data);
 
 						if (objects == null) {
 							Log.w(LOG_TAG, "Received object list from server wasn't a valid BSON object.");
 							return;
 						}
 
 						for (Object o : objects.values()) {
 
 							if (!(o instanceof DBObject)) {
 								Log.w(LOG_TAG, "Received object from server wasn't a DBObject.");
 								continue;
 							}
 
 							DBObject dbo = (DBObject) o;
 							ObjectId id = (ObjectId) dbo.get(MONGO_ID);
 
 							synchronized (mResultsLock) {
 								mResults.put(id, dbo);
 							}
 							requestedIds.remove(id);
 
 							Collection<Object> locksForId = mIdLocks.get(id);
 							if (locksForId != null) {
 								for (Object lock : locksForId) {
 									synchronized (lock) {
 										lock.notifyAll();
 									}
 								}
 							}
 
 						}
 					}
 				}
 				
 			};
 			
 			new Thread(r).start();
 			
 		}
 	}
 }
