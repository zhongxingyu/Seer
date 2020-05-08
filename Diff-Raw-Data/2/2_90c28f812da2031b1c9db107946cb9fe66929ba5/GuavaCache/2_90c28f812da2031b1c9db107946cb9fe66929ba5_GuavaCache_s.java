 package org.stephen.hashmap;
 
 import com.google.common.cache.CacheBuilder;
 import com.google.common.cache.LoadingCache;
 
 import java.beans.PropertyDescriptor;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.TimeUnit;
 
 public class GuavaCache {
    private LoadingCache<String, PropertyDescriptor> propertyCache;
 
     public GuavaCache () {
         this.propertyCache = CacheBuilder.newBuilder ()
                                          .maximumSize (100)
                                          .expireAfterAccess (100, TimeUnit.SECONDS)
                                          .build (new PropertyCacheLoader ());
     }
 
     public PropertyDescriptor get (final String key) throws ExecutionException {
         return propertyCache.get (key);
     }
 }
 
