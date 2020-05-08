 package edu.uw.zookeeper.orchestra.common;
 
 import java.util.Map;
 import java.util.concurrent.ConcurrentMap;
 
 import com.google.common.base.Function;
 import com.google.common.collect.MapMaker;
 import com.google.common.util.concurrent.AsyncFunction;
 import edu.uw.zookeeper.common.AbstractPair;
 
 public class CachedLookup<K,V> extends AbstractPair<ConcurrentMap<K,V>, CachedFunction<K,V>> {
 
     public static class CacheFunction<K,V> implements Function<K,V> {
         
         public static <K,V> CacheFunction<K,V> create(Map<K,V> cache) {
             return new CacheFunction<K,V>(cache);
         }
         
         protected final Map<K,V> cache;
         
         public CacheFunction(Map<K,V> cache) {
             this.cache = cache;
         }
         
         public Map<K,V> get() {
             return cache;
         }
         
         @Override
         public V apply(K input) {
             return cache.get(input);
         }
     }
     
     public static <K,V> CachedLookup<K,V> create(
             AsyncFunction<? super K, V> async) {
         ConcurrentMap<K,V> cache = new MapMaker().makeMap();
         return create(
                 cache, 
                 SharedLookup.create(AddToCacheLookup.create(cache, async)));
     }
 
     public static <K,V> CachedLookup<K,V> create(
             ConcurrentMap<K,V> cache,
             AsyncFunction<? super K, V> async) {
         return new CachedLookup<K,V>(
                 cache, 
                CachedFunction.create(
                         CacheFunction.create(cache), 
                         async));
     }
     
     public CachedLookup(
             ConcurrentMap<K,V> cache,
             CachedFunction<K,V> lookup) {
         super(cache, lookup);
     }
     
     public ConcurrentMap<K,V> asCache() {
         return first;
     }
     
     public CachedFunction<K,V> asLookup() {
         return second;
     }
 }
