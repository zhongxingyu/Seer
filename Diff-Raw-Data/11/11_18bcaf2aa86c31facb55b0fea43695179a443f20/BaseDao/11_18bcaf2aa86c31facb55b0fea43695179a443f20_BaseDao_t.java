 package com.github.ddth.plommon.bo;
 
 import java.util.concurrent.ConcurrentMap;
 
 import org.jboss.netty.util.internal.ConcurrentHashMap;
 import org.springframework.jdbc.core.JdbcTemplate;
 
 import play.cache.Cache;
 import play.db.DB;
 
 /**
  * Base class for application DAOs.
  * 
  * <p>
  * Note: {@link BaseDao} utilizes Spring's {@link JdbcTemplate} to query data.
  * </p>
  * 
  * @author Thanh Nguyen <btnguyen2k@gmail.com>
  * @since 0.3.0
  */
 public class BaseDao {
 
     /*--------------------------------------------------------------------------------*/
     private static ConcurrentMap<String, JdbcTemplate> jdbcTemplates = new ConcurrentHashMap<String, JdbcTemplate>();
 
     /**
      * Gets {@link JdbcTemplate} instance for the "default" datasource.
      * 
      * @return
      */
     protected static JdbcTemplate jdbcTemplate() {
         return jdbcTemplate("default");
     }
 
     /**
      * Gets {@link JdbcTemplate} instance for a specified datasource.
      * 
      * @param datasourceName
      * @return
      */
     protected static JdbcTemplate jdbcTemplate(String datasourceName) {
         JdbcTemplate jdbcTemplate = jdbcTemplates.get(datasourceName);
         if (jdbcTemplate == null) {
            synchronized (jdbcTemplates) {
                // test again to make sure no other thread has created the
                // object
                jdbcTemplate = jdbcTemplates.get(datasourceName);
                if (jdbcTemplate == null) {
                    jdbcTemplate = new JdbcTemplate(DB.getDataSource(datasourceName));
                    jdbcTemplates.putIfAbsent(datasourceName, jdbcTemplate);
                }
            }
         }
         return jdbcTemplate;
     }
 
     /*--------------------------------------------------------------------------------*/
 
     /*--------------------------------------------------------------------------------*/
     /**
      * Removes an entry from cache.
      * 
      * @param key
      */
     protected static void removeFromCache(String key) {
         Cache.remove(key);
     }
 
     /**
      * Puts an entry to cache.
      * 
      * @param key
      * @param value
      */
     protected static void putToCache(String key, Object value) {
         putToCache(key, value, 0);
     }
 
     /**
      * Puts an entry to cache, with specific TTL.
      * 
      * @param key
      * @param value
      * @param ttl
      *            TTL in seconds
      */
     protected static void putToCache(String key, Object value, int ttl) {
         if (value != null) {
             if (ttl > 0) {
                 Cache.set(key, value, ttl);
             } else {
                 Cache.set(key, value);
             }
         }
     }
 
     /**
      * Gets an entry from cache.
      * 
      * @param key
      * @return
      */
     protected static Object getFromCache(String key) {
         return Cache.get(key);
     }
 
     /**
      * Gets an entry from cache.
      * 
      * Note: if the object from cache is not assignable to clazz,
      * <code>null</code> is returned.
      * 
      * @param key
      * @param clazz
      * @return
      */
     @SuppressWarnings("unchecked")
     protected static <T> T getFromCache(String key, Class<T> clazz) {
         Object obj = getFromCache(key);
         if (obj == null) {
             return null;
         }
         if (clazz.isAssignableFrom(obj.getClass())) {
             return (T) obj;
         }
         return null;
     }
 
     /*--------------------------------------------------------------------------------*/
 }
