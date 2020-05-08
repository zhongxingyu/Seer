 import java.util.Set;
 import java.util.TreeSet;
 import java.util.Collection;
 import java.net.InetSocketAddress;
 
 import net.spy.memcached.MemcachedClient;
 import net.spy.memcached.internal.OperationFuture;
 import org.apache.shiro.cache.Cache;
 import org.apache.shiro.cache.CacheException;
 
 
 public class MemCachedCache<K,V> implements Cache<K,V> {
 
     private final MemcachedClient memcachedClient;
     private final String shiroCacheName;
     private final int expiry = 3600;
     private final int keysExpiry = 3*24*3600;
    private Set<String> keys;
 
    private Set<String> getKeys() {
        Set<String> retValue = (Set<String>) memcachedClient.get(shiroCacheName);
         System.out.format("\nretrieved the set %s from memcache under key '%s'\n", retValue, shiroCacheName);
         return retValue;
     }
 
    private void addToKeys(String key) throws Exception {
         System.out.format("asked to add key: %s to the list of keys", key);
         this.keys = getKeys();
         if (this.keys==null)
            this.keys = new TreeSet<String>();
         this.keys.add(key);
         OperationFuture<Boolean> result = memcachedClient.set(shiroCacheName, keysExpiry, keys);
         System.out.format("\n %s to add to memcache, under key %s the structure: %s", (result.get()?"managed":"did not manage"), shiroCacheName, keys);
     }
 
     public MemCachedCache(MemcachedClient memcachedClient, String shiroCacheName) {
         this.memcachedClient = memcachedClient;
         this.shiroCacheName  = shiroCacheName;
         keys = getKeys();
     }
 
     public void clear() {
         throw new UnsupportedOperationException();
     }
 
     public V get(K key) {
         return (V) memcachedClient.get(memCacheKey(key));
     }
 
     public Set<K> keys() {
         this.keys = getKeys();
         return (Set<K>) keys;
     }
 
     private String memCacheKey(K shiroKey) {
         return shiroCacheName+String.valueOf(shiroKey.hashCode());
     }
 
     // according to specs: returns the previous value associated with the given key or null if there was no previous value 
     public V put(K key, V value) {
         long t0 = System.currentTimeMillis();
         V prevValue = (V) memcachedClient.get(memCacheKey(key));
         long t1 = System.currentTimeMillis();
         System.out.format("got the previous object in %d millis", t1 - t0);
         OperationFuture<Boolean> result = memcachedClient.set(memCacheKey(key), expiry, value);
         long t2 = System.currentTimeMillis();
         System.out.format("set the object in %d milliseconds", t2 - t1);
         try {
            addToKeys(memCacheKey(key));
         } catch (Exception e) {
             throw new CacheException(e.getMessage());
         }
         return prevValue;
     }
 
     public V remove(K key) {
         throw new UnsupportedOperationException();
     }
 
     public int size() {
         throw new UnsupportedOperationException();
     }
 
     public Collection<V> values() {
         throw new UnsupportedOperationException();
     }
 
 }
