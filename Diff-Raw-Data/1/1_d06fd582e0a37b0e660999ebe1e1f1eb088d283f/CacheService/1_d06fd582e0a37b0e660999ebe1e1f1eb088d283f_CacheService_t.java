 /*
  *  Copyright 2010 mathieuancelin.
  * 
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  * 
  *       http://www.apache.org/licenses/LICENSE-2.0
  * 
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  *  under the License.
  */
 package cx.ath.mancel01.webframework.cache;
 
 import cx.ath.mancel01.webframework.WebFramework;
 import java.io.NotSerializableException;
 import java.io.Serializable;
 import net.sf.ehcache.Cache;
 import net.sf.ehcache.CacheManager;
 import net.sf.ehcache.Element;
 import net.sf.ehcache.config.CacheConfiguration;
 import net.sf.ehcache.store.MemoryStoreEvictionPolicy;
 
 /**
  *
  * @author mathieuancelin
  */
 public class CacheService {
 
     // see : http://ehcache.org/documentation/samples.html
     // see : http://ehcache.org/documentation/distributed_caching_with_terracotta.html
     // see : http://code.google.com/p/spymemcached/
     
     private static CacheService INSTANCE;
     private CacheManager cacheManager;
     private Cache cache;
 
     private CacheService() {
         this.cacheManager = CacheManager.create();
         Cache webFrameworkCache = new Cache(
              new CacheConfiguration("web-framework", 10000)
                .memoryStoreEvictionPolicy(MemoryStoreEvictionPolicy.LRU)
                .overflowToDisk(false)
                .eternal(false)
                .timeToLiveSeconds(120)
                .timeToIdleSeconds(120)
                .diskPersistent(false)
                .diskExpiryThreadIntervalSeconds(120));
         this.cacheManager.addCache(webFrameworkCache);
         this.cache = cacheManager.getCache("web-framework");
         // TODO : if prod, search for terracota distributed cache
         /**Configuration configuration = new Configuration()
             .terracotta(new GlobalTerracottaConfiguration().url("localhost:9510"))
             .defaultCache(new CacheConfiguration("defaultCache", 100))
             .cache(new CacheConfiguration("example", 100)
             .timeToIdleSeconds(5)
             .timeToLiveSeconds(120)
             .terracotta(new TerracottaConfiguration()));
         this.cacheManager = new CacheManager(configuration);**/
     }
 
     public static CacheService getInstance() {
         if (INSTANCE == null) {
             INSTANCE = new CacheService();
         }
         return INSTANCE;
     }
 
     public void clear() {
         cache.removeAll();
     }
 
     public Object get(String key) {
         Element e = cache.get(key);
         return (e == null) ? null : e.getValue();
     }
 
     public synchronized long incr(String key, int by) {
         Element e = cache.get(key);
         if (e == null) {
             return -1;
         }
         long newValue = ((Number) e.getValue()).longValue() + by;
         Element newE = new Element(key, newValue, e.getExpirationTime());
         cache.put(newE);
         return newValue;
     }
 
     public synchronized long decr(String key, int by) {
         Element e = cache.get(key);
         if (e == null) {
             return -1;
         }
         long newValue = ((Number) e.getValue()).longValue() - by;
         Element newE = new Element(key, newValue, e.getExpirationTime());
         cache.put(newE);
         return newValue;
     }
 
     public boolean add(String key, Object value, int expiration) {
         checkSerializable(value);
         try {
             checkSerializable(value);
             if (cache.get(key) != null) {
                 return false;
             }
             Element element = new Element(key, value);
             element.setTimeToLive(expiration);
             cache.put(element);
             return true;
         } catch (Exception e) {
             return false;
         }
     }
 
     public boolean delete(String key) {
         try {
             cache.remove(key);
             return true;
         } catch (Exception e) {
             WebFramework.logger.error(e.toString());
             return false;
         }
     }
 
     public boolean replace(String key, Object value, int expiration) {
         checkSerializable(value);
         try {
             checkSerializable(value);
             if (cache.get(key) == null) {
                 return false;
             }
             Element element = new Element(key, value);
             element.setTimeToLive(expiration);
             cache.put(element);
             return true;
         } catch (Exception e) {
             WebFramework.logger.error(e.toString());
             return false;
         }
     }
 
     public boolean set(String key, Object value, int expiration) {
         checkSerializable(value);
         try {
             Element element = new Element(key, value);
             element.setTimeToLive(expiration);
             cache.put(element);
             return true;
         } catch (Exception e) {
             WebFramework.logger.error(e.toString());
             return false;
         }
     }
 
     static void checkSerializable(Object value) {
         if (value != null && !(value instanceof Serializable)) {
             throw new RuntimeException("Can't cache a non-serializable value of type " + value.getClass().getName(), new NotSerializableException(value.getClass().getName()));
         }
     }
 }
