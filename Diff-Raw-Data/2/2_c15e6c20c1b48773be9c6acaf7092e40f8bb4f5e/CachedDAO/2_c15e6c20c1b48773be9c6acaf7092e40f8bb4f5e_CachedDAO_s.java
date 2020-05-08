 /**
  *   Copyright(c) 2010-2011 CodWar Soft
  * 
  *   This file is part of IPDB UrT.
  *
  *   IPDB UrT is free software: you can redistribute it and/or modify
  *   it under the terms of the GNU General Public License as published by
  *   the Free Software Foundation, either version 3 of the License, or
  *   (at your option) any later version.
  *
  *   This software is distributed in the hope that it will be useful,
  *   but WITHOUT ANY WARRANTY; without even the implied warranty of
  *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *   GNU General Public License for more details.
  *
  *   You should have received a copy of the GNU General Public License
  *   along with this software. If not, see <http://www.gnu.org/licenses/>.
  */
 package iddb.core.model.dao.cached;
 
 import iddb.core.cache.Cache;
 import iddb.core.cache.CacheFactory;
 import iddb.core.cache.UnavailableCacheException;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Properties;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public abstract class CachedDAO {
 
 	private static final Logger log = LoggerFactory.getLogger(CachedDAO.class);
 	
 	private Cache cacheImpl;
 	
 	protected Integer SEARCH_EXPIRE = 60;
 	
 	public CachedDAO(String namespace) {
 		initProperties();
 		createCache(namespace);
 	}
 
 	private void initProperties() {
 		Properties props = new Properties();
 		try {
 			props.load(getClass().getClassLoader().getResourceAsStream("memcache.properties"));
 			if (props.containsKey("list_expiration")) SEARCH_EXPIRE = Integer.parseInt(props.getProperty("list_expiration"));
 		} catch (Exception e) {
 			log.warn("Unable to load cache properties [{}]", e.getMessage());
 		}	
 	}
 	
 	@SuppressWarnings("unchecked")
 	protected Object getCachedList(String key, int[] count) {
 		if (cacheImpl == null) return null;
 		try {
 			Map<String, Object> map = (Map<String, Object>) cacheImpl.get(key);
 			if (map != null) {
 				if (log.isDebugEnabled()) log.debug(map.toString());
 				if (count != null) count[0] = (Integer) map.get("count");
 				return map.get("list");
 			}
 		} catch (Exception e) {
 			log.error(e.getMessage());
 		}
 		return null;
 	}
 	
 	protected void putCachedList(String key, Object list, int[] count) {
 		if (cacheImpl != null) {
 			Map<String, Object> map = new HashMap<String, Object>();
 			map.put("list", list);
 			if (count != null) map.put("count", count[0]);
 			else map.put("count", 0);
 			cacheImpl.put(key, map, SEARCH_EXPIRE);
 		}
 	}
 
 	protected void createCache(String namespace) {
 		try {
			this.cacheImpl = CacheFactory.getInstance().getCache("alias");
 		} catch (UnavailableCacheException e) {
 			this.cacheImpl = null;
 		}
 	}
 	
 	/**
 	 * Put object in cache
 	 * @param key
 	 * @param value
 	 */
 	protected void cachePut(String key, Object value) {
 		if (this.cacheImpl != null) this.cacheImpl.put(key, value); 
 	}
 
 	/**
 	 * Put object in cache with expiration time
 	 * @param key
 	 * @param value
 	 */
 	protected void cachePut(String key, Object value, Integer expire_in_seconds) {
 		if (this.cacheImpl != null) this.cacheImpl.put(key, value, expire_in_seconds * 60); 
 	}
 	
 	/**
 	 * Get object from cache
 	 * @param key
 	 * @return Object
 	 */
 	protected Object cacheGet(String key) {
 		if (this.cacheImpl == null) return null;
 		return this.cacheImpl.get(key);
 	}
 	
 	protected void cacheClear() {
 		if (this.cacheImpl != null) this.cacheImpl.clear();
 	}
 	
 }
