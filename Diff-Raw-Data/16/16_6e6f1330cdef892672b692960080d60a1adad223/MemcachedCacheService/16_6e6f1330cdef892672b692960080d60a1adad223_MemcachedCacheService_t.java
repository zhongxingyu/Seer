 package com.amee.base.cache;
 
 import net.spy.memcached.MemcachedClient;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import java.io.IOException;
 import java.net.InetSocketAddress;
 
 public class MemcachedCacheService implements CacheService {
 
     private final Log log = LogFactory.getLog(getClass());
 
     private MemcachedClient client;
 
     public MemcachedCacheService() {
         super();
         try {
             client = new MemcachedClient(new InetSocketAddress("localhost", 11211));
         } catch (IOException e) {
            log.error("CacheService() Caught IOException: " + e.getMessage(), e);
             throw new RuntimeException("CacheService() Caught IOException: " + e.getMessage());
         }
     }
 
     public void set(String region, String key, Object o) {
         client.set(region + "_" + key, 3600, o);
     }
 
     public Object get(String region, String key) {
         return client.get(region + "_" + key);
     }
 
     public void delete(String region, String key) {
         client.delete(region + "_" + key);
     }
 }
