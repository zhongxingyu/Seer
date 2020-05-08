 /*
  * Licensed to the Sakai Foundation (SF) under one
  * or more contributor license agreements. See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership. The SF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License. You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied. See the License for the
  * specific language governing permissions and limitations under the License.
  */
 package org.sakaiproject.nakamura.lite;
 
 import org.sakaiproject.nakamura.api.lite.CacheHolder;
 import org.sakaiproject.nakamura.api.lite.StorageClientException;
 import org.sakaiproject.nakamura.lite.storage.StorageClient;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.util.Map;
 
 public class CachingManager {
 
     private static final Logger LOGGER = LoggerFactory.getLogger(CachingManager.class);
     private Map<String, CacheHolder> sharedCache;
     private StorageClient client;
     private int hit;
     private int miss;
     private long calls;
 
     public CachingManager(StorageClient client, Map<String, CacheHolder> sharedCache) {
         this.client = client;
         this.sharedCache = sharedCache;
     }
 
     protected Map<String, Object> getCached(String keySpace, String columnFamily, String key)
             throws StorageClientException {
        Map<String, Object> m;
         String cacheKey = getCacheKey(keySpace, columnFamily, key);
         if (sharedCache != null && sharedCache.containsKey(cacheKey)) {
             CacheHolder aclCacheHolder = sharedCache.get(cacheKey);
            m = aclCacheHolder.get();
            hit++;
        } else {
             m = client.get(keySpace, columnFamily, key);
             miss++;
             if (sharedCache != null) {
                 if (m != null) {
                     LOGGER.debug("Found Map {} {}", cacheKey, m);
                 }
                 sharedCache.put(cacheKey, new CacheHolder(m));
             }
         }
         calls++;
         if ((calls % 1000) == 0) {
             LOGGER.info("Cache Stats Hits {} Misses {}  hit% {}", new Object[] { hit, miss,
                     ((100 * hit) / (hit + miss)) });
         }
         return m;
     }
 
     private String getCacheKey(String keySpace, String columnFamily, String key) {
         return keySpace + ":" + columnFamily + ":" + key;
     }
 
     protected void removeFromCache(String keySpace, String columnFamily, String key) {
         if (sharedCache != null) {
             sharedCache.remove(getCacheKey(keySpace, columnFamily, key));
         }
     }
 
     protected void putCached(String keySpace, String columnFamily, String key,
             Map<String, Object> encodedProperties, boolean probablyNew) throws StorageClientException {
         removeFromCache(keySpace, columnFamily, key);
         LOGGER.debug("Updating {} with {}  ",key, encodedProperties);
         client.insert(keySpace, columnFamily, key, encodedProperties, probablyNew);
     }
 
 }
