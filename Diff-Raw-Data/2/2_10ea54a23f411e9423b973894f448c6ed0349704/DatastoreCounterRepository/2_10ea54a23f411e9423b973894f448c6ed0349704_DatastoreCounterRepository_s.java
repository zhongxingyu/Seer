 package com.threewks.thundr.persist;
 
 import java.util.ArrayList;
 import java.util.ConcurrentModificationException;
 import java.util.List;
 import java.util.Random;
 
 import com.google.appengine.api.datastore.DatastoreService;
 import com.google.appengine.api.datastore.Entity;
 import com.google.appengine.api.datastore.EntityNotFoundException;
 import com.google.appengine.api.datastore.Key;
 import com.google.appengine.api.datastore.KeyFactory;
 import com.google.appengine.api.datastore.Query;
 import com.google.appengine.api.datastore.Query.FilterOperator;
 import com.google.appengine.api.datastore.Transaction;
 import com.google.appengine.api.memcache.Expiration;
 import com.google.appengine.api.memcache.MemcacheService;
 import com.google.appengine.api.memcache.MemcacheService.SetPolicy;
 import com.threewks.thundr.logger.Logger;
 
 /**
  * This is based on this article here: https://developers.google.com/appengine/articles/sharding_counters
  * 
  * Effectively, sharding is a technique where data is split across several entities to overcome datastore write limitations, and the
  * data of interest is aggregated by a read query (which is super quick).
  */
 public class DatastoreCounterRepository implements CounterRepository {
 
 	private static final Random generator = new Random();
 	private static final long InitialShards = 1;
 	private static final String KindNameShardCount = "ShardedCounter";
 	private static final String KindNameCount = "Count";
 	/**
 	 * The property to store the count in each shard.
 	 */
 	private static final String PropertyCount = "count";
 	/**
 	 * Counters which are associated with another entity store the reference in this property.
 	 */
 	private static final String PropertyAssociation = "assocation";
 
 	private MemcacheService memcacheService;
 	private DatastoreService datastoreService;
 
 	public DatastoreCounterRepository(MemcacheService cacheService, DatastoreService datastoreService) {
 		super();
 		this.memcacheService = cacheService;
 		this.datastoreService = datastoreService;
 	}
 
 	@Override
 	public long getCount(String counterType, String association) {
 		String memcacheKey = memcacheKey(counterType, association);
 		Long value = readCache(memcacheKey);
 		if (value != null) {
 			return value;
 		}
 		long sum = sumFromQuery(counterType, association);
 		storeCache(memcacheKey, sum);
 		return sum;
 	}
 
 	@Override
 	public long getCount(String counterType) {
 		return getCount(counterType, null);
 	}
 
 	@Override
 	public long incrementCount(String counterType) {
 		return incrementCount(counterType, null);
 	}
 
 	@Override
 	public long incrementCount(String counterType, String association) {
 		return incrementCount(counterType, association, 1);
 	}
 
 	@Override
 	public long incrementCountBy(String counterType, String association, long by) {
 		return incrementCount(counterType, association, by);
 	}
 
 	@Override
 	public long decrementCount(String counterType) {
 		return decrementCount(counterType, null);
 	}
 
 	@Override
 	public long decrementCount(String counterType, String association) {
 		return incrementCount(counterType, association, -1);
 	}
 
 	@Override
 	public long decrementCountBy(String counterType, String association, long by) {
 		return incrementCount(counterType, association, -1 * by);
 	}
 
 	/**
 	 * Finds a random shard for the given counter and increments it by the specified amount.
 	 * This method grows the number of shards as needed.
 	 * Shard count is doubled when the number of concurrent accesses is equal to the number of shards.
 	 * 
 	 * @param counterType
 	 * @param association
 	 * @param by
 	 * @return
 	 */
 	protected long incrementCount(String counterType, String association, long by) {
 		String memcacheKey = memcacheKey(counterType, association);
 		// track concurrent writes - if this exceeds the number of shards, increment the shards
 		String concurrentIncrementKey = memcacheKey + "Concurrent";
 		long numShards = shardCount(counterType, association);
 		try {
 			long currentAccesses = memcacheService.increment(concurrentIncrementKey, 1, 0L);
 			if (currentAccesses > numShards) {
 				// double the number of shards if there are at least that many concurrent writes happening
 				numShards = addShardsToShardedCounter(numShards, counterType, association);
 			}
 
 			// Choose the shard randomly from the available shards.
			long shardNum = generator.nextInt((int) numShards);
 
 			String counterKind = kindName(counterType);
 			String name = association == null ? Long.toString(shardNum) : String.format("%s_%s", association, shardNum);
 			Key shardKey = createKey(counterKind, name);
 			incrementCounter(shardKey, association, by, by);
 			memcacheService.increment(memcacheKey, by);
 			return getCount(counterType, association);
 		} finally {
 			memcacheService.increment(concurrentIncrementKey, -1, 0L);
 		}
 	}
 
 	/**
 	 * Increase the number of shards for a given sharded counter. Will never
 	 * decrease the number of shards.
 	 * 
 	 * @param shardsToAdd
 	 *            Number of new shards to build and store
 	 * @param association
 	 */
 	protected long addShardsToShardedCounter(long shardsToAdd, String counterType, String association) {
 		long shardCount = InitialShards;
 		String memcacheKey = shardCountMemcacheKey(counterType, association);
 		Key counterKey = createKey(KindNameShardCount, memcacheKey);
 		try {
 			Entity counter = datastoreService.get(counterKey);
 			shardCount = (Long) counter.getProperty(PropertyCount);
 		} catch (EntityNotFoundException ignore) {
 		}
 		long newShardCount = shardCount + shardsToAdd;
 		incrementCounter(counterKey, association, shardsToAdd, newShardCount);
 		return storeCache(KindNameShardCount, newShardCount);
 	}
 
 	/**
 	 * Get the number of shards in this counter.
 	 * 
 	 * @param association
 	 * 
 	 * @return shard count
 	 */
 	long shardCount(String counterType, String association) {
 		String memcacheKey = shardCountMemcacheKey(counterType, association);
 		Long shardCount = readCache(memcacheKey);
 		if (shardCount != null) {
 			return shardCount;
 		}
 
 		try {
 			Key counterKey = createKey(KindNameShardCount, memcacheKey);
 			Entity counter = datastoreService.get(counterKey);
 			shardCount = (Long) counter.getProperty(PropertyCount);
 		} catch (EntityNotFoundException ignore) {
 			shardCount = InitialShards;
 		}
 		storeCache(memcacheKey, shardCount);
 		return shardCount;
 	}
 
 	Long readCache(String memcacheKey) {
 		return (Long) memcacheService.get(memcacheKey);
 	}
 
 	long storeCache(String memcacheKey, long value) {
 		memcacheService.put(memcacheKey, value, Expiration.byDeltaSeconds(60), SetPolicy.ADD_ONLY_IF_NOT_PRESENT);
 		return value;
 	}
 
 	long sumFromQuery(String counterType, String association) {
 		String kind = kindName(counterType);
 		long sum = 0;
 		Query query = new Query(kind);
 		if (association != null) {
 			query.addFilter(PropertyAssociation, FilterOperator.EQUAL, association);
 		}
 		for (Entity shard : datastoreService.prepare(query).asIterable()) {
 			sum += (Long) shard.getProperty(PropertyCount);
 		}
 		return sum;
 	}
 
 	/**
 	 * Increment datastore property value inside a transaction. If the entity with
 	 * the provided key does not exist, instead create an entity with the supplied
 	 * initial property value.
 	 * 
 	 * @param key
 	 *            the entity key to update or create
 	 * @param prop
 	 *            the property name to be incremented
 	 * @param increment
 	 *            the amount by which to increment
 	 * @param initialValue
 	 *            the value to use if the entity does not exist
 	 */
 	private void incrementCounter(Key key, String association, long increment, long initialValue) {
 		try {
 			Transaction tx = datastoreService.beginTransaction();
 			Entity thing;
 			long value;
 			try {
 				thing = datastoreService.get(tx, key);
 				value = (Long) thing.getProperty(PropertyCount) + increment;
 			} catch (EntityNotFoundException e) {
 				thing = new Entity(key);
 				if (association != null) {
 					thing.setProperty(PropertyAssociation, association);
 				}
 				value = initialValue;
 			}
 			thing.setUnindexedProperty(PropertyCount, value);
 			datastoreService.put(tx, thing);
 			tx.commit();
 		} catch (ConcurrentModificationException e) {
 			Logger.warn("Failed to update counter in datastore %s %s - relying on cached results to rectify count over time", key.getName(), association);
 		}
 	}
 
 	protected Key createKey(String kind, String name) {
 		return KeyFactory.createKey(kind, name);
 	}
 
 	String memcacheKey(String counterType, String association) {
 		String kindName = kindName(counterType);
 		return association != null ? String.format("%s_%s", kindName, association) : kindName;
 	}
 
 	String kindName(String counterType) {
 		return counterType + KindNameCount;
 	}
 
 	String shardCountMemcacheKey(String counterType, String association) {
 		String kindName = kindName(counterType) + "Shards";
 		return association != null ? String.format("%s_%s", kindName, association) : kindName;
 	}
 
 	/**
 	 * Sets the counter to a specific value. The set method is more susceptible to race conditions, as it involves
 	 * altering the value across all shards. This should be used infrequently and cautiously. The implementation currently deletes all existing shards and creates a new one.
 	 * This will result in shard count growth being required on heavily used counters.
 	 * 
 	 * @param counterType
 	 * @param association
 	 * @param value
 	 * @return
 	 */
 	@Override
 	public long setCount(String counterType, String association, long value) {
 		// delete all shards, and create a new one
 		String kind = kindName(counterType);
 		String cacheKey = memcacheKey(counterType, association);
 		Query query = new Query(kind);
 		if (association != null) {
 			query.addFilter(PropertyAssociation, FilterOperator.EQUAL, association);
 		}
 		query.setKeysOnly();
 		List<Key> shardKeys = new ArrayList<Key>();
 		for (Entity shard : datastoreService.prepare(query).asIterable()) {
 			shardKeys.add(shard.getKey());
 		}
 		datastoreService.delete(shardKeys.toArray(new Key[0]));
 		memcacheService.delete(cacheKey);
 		long currentValue = incrementCount(counterType, association, value);
 		memcacheService.put(cacheKey, value);
 		return currentValue;
 	}
 }
