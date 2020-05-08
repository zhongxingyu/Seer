 package CIAPI.Java.cachestuff;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.joda.time.DateTime;
 import org.joda.time.Period;
 
 /**
  * Default implementation of a Cache.  Backed by a hashmap
  * @author justin nelson
  *
  * @param <TKey>
  * @param <TValue>
  */
 public class DefaultCache<TKey, TValue> implements Cache<TKey, TValue> {
 
 	private Period maxAge;
 
 	private Map<TKey, CacheItem> storage;
 
 	/**
 	 * Creates an empty cache
 	 */
 	public DefaultCache(long maxAgeL){
 		storage = new HashMap<TKey, CacheItem>();
 		maxAge = new Period(maxAgeL);
 	}
 	
 	@Override
 	public TValue get(TKey key) {
 		CacheItem obj = storage.get(key);
 		if (obj == null)
 			return null;
 		if (obj.isExpired()) {
 			delete(key);
 			return null;
 		}
 		return obj.data;
 	}
 
 	@Override
 	public TValue put(TKey key, TValue value) {
 		CacheItem oldItem = storage.put(key, new CacheItem(value, new DateTime()));
 		return oldItem == null ? null : oldItem.data;
 	}
 
 	@Override
 	public void clean() {
		for (Entry<TKey, CacheItem> entry : storage.entrySet()) {
 			if (entry.getValue().isExpired())
				delete(entry.getKey());
 		}
 	}
 
 	@Override
 	public int entryCount() {
 		return storage.size();
 	}
 
 	@Override
 	public TValue delete(TKey key) {
 		return storage.remove(key).data;
 	}
 	
 	@Override
 	public Period maxAge() {
 		return maxAge;
 	}
 
 	class CacheItem {
 		private TValue data;
 		private DateTime timeEntered;
 
 		private CacheItem(TValue data, DateTime timeCreated) {
 			this.data = data;
 			this.timeEntered = timeCreated;
 		}
 
 		private boolean isExpired() {
 			return timeEntered.plus(maxAge).isBeforeNow();
 		}
 	}
 }
