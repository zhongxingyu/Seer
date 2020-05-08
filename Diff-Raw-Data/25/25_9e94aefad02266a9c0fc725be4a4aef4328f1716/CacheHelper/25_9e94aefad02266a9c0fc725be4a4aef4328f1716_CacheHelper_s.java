 package com.amee.domain.cache;
 
 import com.amee.domain.PersistentObject;
 import net.sf.ehcache.CacheManager;
 import net.sf.ehcache.Ehcache;
 import net.sf.ehcache.Element;
 import net.sf.ehcache.constructs.blocking.BlockingCache;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import java.io.Serializable;
 import java.util.List;
 
 public class CacheHelper implements Serializable {
 
     private final Log log = LogFactory.getLog(getClass());
 
     private static CacheHelper instance = new CacheHelper();
     private CacheManager cacheManager;
 
     private CacheHelper() {
         super();
         cacheManager = CacheManager.getInstance();
     }
 
     public static CacheHelper getInstance() {
         return instance;
     }
 
     public Object getInternal() {
         return cacheManager;
     }
 
     public BlockingCache getBlockingCache(String cacheName) {
         Ehcache cache = getCacheManager().getEhcache(cacheName);
         if ((cache != null) && !(cache instanceof BlockingCache)) {
             synchronized (this) {
                 cache = getCacheManager().getEhcache(cacheName);
                 if ((cache != null) && !(cache instanceof BlockingCache)) {
                     BlockingCache newBlockingCache = new BlockingCache(cache);
                     getCacheManager().replaceCacheWithDecoratedCache(cache, newBlockingCache);
                 }
             }
         }
         return (BlockingCache) getCacheManager().getEhcache(cacheName);
     }
 
     public Object getCacheable(CacheableFactory factory) {
         Object o = null;
         String cacheName = factory.getCacheName();
         String key = factory.getKey();
         BlockingCache cache = getBlockingCache(cacheName);
         if (cache != null) {
             log.debug("getCacheable() - cache: " + cacheName + " key: " + key);
             String originalThreadName = Thread.currentThread().getName();
             try {
                 Element element = cache.get(key);
                 if (element == null) {
                     try {
                         // element is not cached - build it
                         o = factory.create();
                         cache.put(new Element(key, o));
                     } catch (final Throwable throwable) {
                         // must unlock the cache if the above fails
                         cache.put(new Element(key, null));
                         // TODO: what should we really be throwing here?
                         throw new RuntimeException(throwable);
                     }
                 } else {
                     o = element.getObjectValue();
                 }
             } finally {
                 Thread.currentThread().setName(originalThreadName);
             }
         } else {
             log.warn("getCacheable() - cache NOT found: " + cacheName);
             o = factory.create();
         }
         return o;
     }
 
     public void clearCache(CacheableFactory factory) {
         remove(factory.getCacheName(), factory.getKey());
     }
 
    // TODO: profile this method to see if it's cost effective
    public void clearCache(String cacheName, String scope) {
        BlockingCache cache = getBlockingCache(cacheName);
        if (cache != null) {
            log.debug("clearCache() - cache: " + cacheName + " scope: " + scope);
            for (Object o : cache.getKeys()) {
                String elementKey = (String) o;
                if (elementKey.startsWith("E_" + scope + "_")) {
                    log.debug("clearCache() removing: " + elementKey);
                    cache.remove(elementKey);
                }
            }
        }
     }
 
     public void clearCache(String cacheName) {
         BlockingCache cache = getBlockingCache(cacheName);
         if (cache != null) {
             log.debug("clearCache() - cache: " + cacheName);
             cache.removeAll();
             cache.getKeys();
         }
     }
 
     private String getCacheKey(String key, String scope) {
         return "S_" + scope + "_" + key;
     }
 
     public void remove(String cacheName, String key, String scope) {
         remove(cacheName, getCacheKey(key, scope));
     }
 
     public void remove(String cacheName, String key) {
         BlockingCache cache = getBlockingCache(cacheName);
         if (cache != null) {
             log.debug("remove() - cache: " + cacheName + " key: " + key);
             cache.remove(key);
         }
     }
 
     public CacheManager getCacheManager() {
         return cacheManager;
     }
 
     public void add(String cacheName, String scope, String key, Object o) {
         add(cacheName, getCacheKey(key, scope), o);
     }
 
     public void add(String cacheName, String key, Object o) {
         BlockingCache cache = getBlockingCache(cacheName);
         if (cache != null) {
             log.debug("add() - cache: " + cacheName + " key: " + key);
             cache.put(new Element(key, o));
         }
     }
 
     public List getKeys(String cacheName) {
         BlockingCache cache = getBlockingCache(cacheName);
         return cache.getKeys();
     }
 
     public Object get(String cacheName, String scope, String key) {
         return get(cacheName, getCacheKey(key, scope));
     }
 
     public Object get(String cacheName, Object key) {
         BlockingCache cache = getBlockingCache(cacheName);
         if ((cache != null) && cache.isKeyInCache(key)) {
             Element element = cache.get(key);
             if (element != null) {
                 return element.getValue();
             } else {
                 // unlock the blocking cache if cache.get fails
                 cache.put(new Element(key, null));
             }
         }
         return null;
     }
 
     public Object getAndBlock(String cacheName, String key, String scope) {
         return getAndBlock(cacheName, getCacheKey(key, scope));
     }
 
     public Object getAndBlock(String cacheName, Object key) {
         BlockingCache cache = getBlockingCache(cacheName);
         if ((cache != null) && cache.isKeyInCache(key)) {
             Element element = cache.get(key);
             if (element != null) {
                 return element.getValue();
             }
         }
         return null;
     }
 
     public Object getAndRemove(String cacheName, String key, String scope) {
         return getAndRemove(cacheName, getCacheKey(key, scope));
     }
 
     public Object getAndRemove(String cacheName, Object key) {
         BlockingCache cache = getBlockingCache(cacheName);
         if ((cache != null) && cache.isKeyInCache(key)) {
             Element element = cache.get(key);
             if (element != null) {
                 cache.remove(key);
                 return element.getValue();
             } else {
                 // unlock the blocking cache if cache.get fails
                 cache.put(new Element(key, null));
             }
         }
         return null;
     }
 
     public void add(String cacheName, PersistentObject persistentObject, String scope) {
         add(cacheName, getCacheKey(persistentObject.getUid(), scope), persistentObject);
     }
 
     public void add(String cacheName, PersistentObject persistentObject) {
         add(cacheName, persistentObject.getUid(), persistentObject);
     }
 
     public void remove(String cacheName, PersistentObject persistentObject, String scope) {
         remove(cacheName, getCacheKey(persistentObject.getUid(), scope));
     }
 
     public void remove(String cacheName, PersistentObject persistentObject) {
         remove(cacheName, persistentObject.getUid());
     }
 }
