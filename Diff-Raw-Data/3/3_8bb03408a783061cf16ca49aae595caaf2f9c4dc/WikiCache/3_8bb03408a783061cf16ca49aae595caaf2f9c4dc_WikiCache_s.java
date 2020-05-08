 /**
  * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, version 2.1, dated February 1999.
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the latest version of the GNU Lesser General
  * Public License as published by the Free Software Foundation;
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with this program (LICENSE.txt); if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
  */
 package org.jamwiki.utils;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.util.List;
 import net.sf.ehcache.Cache;
 import net.sf.ehcache.CacheException;
 import net.sf.ehcache.CacheManager;
 import net.sf.ehcache.Element;
 import net.sf.ehcache.config.CacheConfiguration;
 import net.sf.ehcache.config.Configuration;
 import net.sf.ehcache.config.DiskStoreConfiguration;
 import org.jamwiki.DataAccessException;
 import org.jamwiki.Environment;
 
 /**
  * Implement utility functions that interact with the cache and provide the
  * infrastructure for storing and retrieving items from the cache.
  */
 public class WikiCache {
 
 	private static final WikiLogger logger = WikiLogger.getLogger(WikiCache.class.getName());
 	private static CacheManager cacheManager = null;
 	// track whether this instance was instantiated from an ehcache.xml file or using configured properties.
 	private static final boolean USES_XML_CONFIG;
 	private static final String EHCACHE_XML_CONFIG_FILENAME = "ehcache.xml";
 
 	/** Directory for cache files. */
 	private static final String CACHE_DIR = "cache";
 
 	static {
 		boolean xmlConfig = false;
 		try {
 			Utilities.getClassLoaderFile(EHCACHE_XML_CONFIG_FILENAME);
 			logger.info("Initializing cache configuration from " + EHCACHE_XML_CONFIG_FILENAME + " file");
 			xmlConfig = true;
 		} catch (FileNotFoundException e) {
 			logger.info("No " + EHCACHE_XML_CONFIG_FILENAME + " file found, using default cache configuration");
 		}
 		USES_XML_CONFIG = xmlConfig;
 		WikiCache.initialize();
 	}
 
 	/**
 	 *
 	 */
 	private WikiCache() {
 	}
 
 	/**
 	 * Add an object to the cache.
 	 *
 	 * @param cacheName The name of the cache that the object is being added
 	 *  to.
 	 * @param key A String, Integer, or other object to use as the key for
 	 *  storing and retrieving this object from the cache.
 	 * @param value The object that is being stored in the cache.
 	 */
 	public static void addToCache(String cacheName, Object key, Object value) {
 		Cache cache = WikiCache.getCache(cacheName);
 		cache.put(new Element(key, value));
 	}
 
 	/**
 	 * Internal method used to retrieve a cache given the cache name.  If no
 	 * cache exists with the given name then a new cache will be created.
 	 *
 	 * @param cacheName The name of the cache to retrieve.
 	 * @return The existing cache with the given name, or a new cache if no
 	 *  existing cache exists.
 	 * @throws IllegalStateException if an attempt is made to retrieve a cache
 	 *  using XML configuration and the cache is not configured.
 	 */
 	private static Cache getCache(String cacheName) throws CacheException {
 		if (!WikiCache.cacheManager.cacheExists(cacheName)) {
 			if (USES_XML_CONFIG) {
 				// all caches should be configured from ehcache.xml
 				throw new IllegalStateException("No cache named " + cacheName + " is configured in the ehcache.xml file");
 			}
 			int maxSize = Environment.getIntValue(Environment.PROP_CACHE_INDIVIDUAL_SIZE);
 			int maxAge = Environment.getIntValue(Environment.PROP_CACHE_MAX_AGE);
 			int maxIdleAge = Environment.getIntValue(Environment.PROP_CACHE_MAX_IDLE_AGE);
 			Cache cache = new Cache(cacheName, maxSize, true, false, maxAge, maxIdleAge);
 			WikiCache.cacheManager.addCache(cache);
 		}
 		return WikiCache.cacheManager.getCache(cacheName);
 	}
 
 	/**
 	 * Initialize the cache, clearing any existing cache instances and loading
 	 * a new cache instance.
 	 */
 	public static void initialize() {
 		try {
 			if (WikiCache.cacheManager != null) {
 				if (USES_XML_CONFIG) {
 					WikiCache.cacheManager.removalAll();
 				}
 				WikiCache.cacheManager.shutdown();
 			}
 			File directory = new File(Environment.getValue(Environment.PROP_BASE_FILE_DIR), CACHE_DIR);
 			if (!directory.exists()) {
 				directory.mkdir();
 			}
 			if (USES_XML_CONFIG) {
 				WikiCache.cacheManager = CacheManager.create();
 			} else {
 				Configuration configuration = new Configuration();
 				CacheConfiguration defaultCacheConfiguration = new CacheConfiguration("jamwikiCache", Environment.getIntValue(Environment.PROP_CACHE_TOTAL_SIZE));
 				defaultCacheConfiguration.setDiskPersistent(false);
 				defaultCacheConfiguration.setEternal(false);
 				defaultCacheConfiguration.setOverflowToDisk(true);
 				configuration.addDefaultCache(defaultCacheConfiguration);
 				DiskStoreConfiguration diskStoreConfiguration = new DiskStoreConfiguration();
 //				diskStoreConfiguration.addExpiryThreadPool(new ThreadPoolConfiguration("", 5, 5));
 //				diskStoreConfiguration.addSpoolThreadPool(new ThreadPoolConfiguration("", 5, 5));
 				diskStoreConfiguration.setPath(directory.getPath());
 				configuration.addDiskStore(diskStoreConfiguration);
 				WikiCache.cacheManager = new CacheManager(configuration);
 			}
 		} catch (Exception e) {
 			logger.error("Failure while initializing cache", e);
 			throw new RuntimeException(e);
 		}
 		logger.info("Initializing cache 9");
 	}
 
 	public static void shutdown() {
 		if (WikiCache.cacheManager != null) {
 			WikiCache.cacheManager.shutdown();
 			WikiCache.cacheManager = null;
 		}
 	}
 
 	/**
 	 * Given two string values, generate a unique key value that can be used to
 	 * store and retrieve cache objects.
 	 *
 	 * @param value1 The first value to use in the key name.
 	 * @param value2 The second value to use in the key name.
 	 * @return The generated key value.
 	 */
 	public static String key(String value1, String value2) {
 		if (value1 == null && value2 == null) {
 			throw new IllegalArgumentException("WikiCache.key cannot be called with two null values");
 		}
 		if (value1 == null) {
 			value1 = "";
 		}
 		if (value2 == null) {
 			value2 = "";
 		}
 		return value1 + "/" + value2;
 	}
 
 	/**
 	 * Remove all values from the cache with the given name.
 	 *
 	 * @param cacheName The name of the cache from which objects are being
 	 *  removed.
 	 */
 	public static void removeAllFromCache(String cacheName) {
 		Cache cache = WikiCache.getCache(cacheName);
 		cache.removeAll();
 	}
 
 	/**
 	 * Remove a cache with the given name from the system, freeing any
 	 * resources used by that cache.
 	 *
 	 * @param cacheName The name of the cache being removed.
 	 */
 	public static void removeCache(String cacheName) {
 		WikiCache.cacheManager.removeCache(cacheName);
 	}
 
 	/**
 	 * Remove a value from the cache with the given key and name.
 	 *
 	 * @param cacheName The name of the cache from which the object is being
 	 *  removed.
 	 * @param key The key for the record that is being removed from the cache.
 	 */
 	public static void removeFromCache(String cacheName, Object key) {
 		Cache cache = WikiCache.getCache(cacheName);
 		cache.remove(key);
 	}
 
 	/**
 	 * Remove a key from the cache in a case-insensitive manner.  This method
 	 * is significantly slower than removeFromCache and should only be used when
 	 * the key values may not be exactly known.
 	 */
 	public static void removeFromCacheCaseInsensitive(String cacheName, String key) {
 		Cache cache = WikiCache.getCache(cacheName);
 		List cacheKeys = cache.getKeys();
 		for (Object cacheKey : cacheKeys) {
			if (cacheKey.toString().equalsIgnoreCase(key)) {
 				cache.remove(cacheKey);
 			}
 		}
 	}
 
 	/**
 	 * Retrieve a cached element from the cache.  This method will return
 	 * <code>null</code> if no matching element is cached, an element with
 	 * no value if a <code>null</code> value is cached, or an element with a
 	 * valid object value if such an element is cached.
 	 *
 	 * @param cacheName The name of the cache from which the object is being
 	 *  retrieved.
 	 * @param key The key for the record that is being retrieved from the
 	 *  cache.
 	 * @return A new <code>Element</code> object containing the key and cached
 	 *  object value.
 	 */
 	public static Element retrieveFromCache(String cacheName, Object key) throws DataAccessException {
 		Cache cache = null;
 		try {
 			cache = WikiCache.getCache(cacheName);
 		} catch (CacheException e) {
 			throw new DataAccessException("Failure while retrieving data from cache " + cacheName, e);
 		}
 		return cache.get(key);
 	}
 }
