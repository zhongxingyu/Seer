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
 package iddb.runtime.cache.impl;
 
 import org.apache.commons.lang.StringUtils;
 
 import iddb.core.cache.Cache;
 import iddb.core.cache.UnavailableCacheException;
 
 import java.net.InetSocketAddress;
 import java.util.Properties;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.Future;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.TimeoutException;
 
 import net.spy.memcached.MemcachedClient;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class CacheImpl implements Cache {
 
 	private static final Logger log = LoggerFactory.getLogger(CacheImpl.class);
 	
 	private MemcachedClient client;
 	private Integer expiration = 3600;
 	private String namespace;
 	private String prefix;
 	private String instanceName;
     
 	public CacheImpl() throws UnavailableCacheException {
 		Properties props = new Properties();
 		try {
 			props.load(getClass().getClassLoader().getResourceAsStream("memcache.properties"));
 			client = new MemcachedClient(new InetSocketAddress(props.getProperty("host"), Integer.parseInt(props.getProperty("port"))));
 			if (props.containsKey("expiration")) expiration = Integer.parseInt(props.getProperty("expiration"));
 			if (props.containsKey("prefix")) prefix = props.getProperty("prefix");
 			else prefix = "ipdb";
 		} catch (Exception e) {
 			log.error("Unable to load cache properties [{}]", e.getMessage());
 			throw new UnavailableCacheException();
 		}	
 		setNamespace("default");
 		log.debug("Initialized memcache instance.");
 	}
 	
     private String getCachePrefix() {
        return StringUtils.StringUtils(Long.toString(System.currentTimeMillis() / 1000), 5)
     }
 
 	public String getNamespace() {
 		return this.instanceName;
 	}
 
 	public void setNamespace(String namespace) {
 		this.namespace = namespace;
        this.instanceName = this.prefix + "-" + getCachePrefix() + "-" + namespace
 	}
 
 	/* (non-Javadoc)
 	 * @see iddb.core.cache.Cache#clear()
 	 */
 	@Override
 	public void clear() {
        # clear is not supported. lets create a new namespace instead
		setNamespace(this.namespace)
 	}
 
 	/* (non-Javadoc)
 	 * @see iddb.core.cache.Cache#get(java.lang.String)
 	 */
 	@Override
 	public Object get(String key) {
 		synchronized (key) {
 			Object obj = null;
 			Future<Object> asGet = client.asyncGet(getNamespace() + "-" + key);
 			try {
 				obj = asGet.get(10, TimeUnit.SECONDS);
 			} catch (TimeoutException e) {
 				asGet.cancel(false);
 				log.error(e.getMessage());
 			} catch (InterruptedException e) {
 				log.error(e.getMessage());
 			} catch (ExecutionException e) {
 				log.error(e.getMessage());
 			}
 			return obj;
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see iddb.core.cache.Cache#put(java.lang.String, java.lang.Object)
 	 */
 	@Override
 	public void put(String key, Object value) {
 		if (client == null) return;
 		client.set(getNamespace() + "-" + key, expiration, value);
 	}
 
 	/* (non-Javadoc)
 	 * @see iddb.core.cache.Cache#put(java.lang.String, java.lang.Object, java.lang.Integer)
 	 */
 	@Override
 	public void put(String key, Object value, Integer expire) {
 		if (client == null) return;
 		client.set(getNamespace() + "-" + key, expire, value);
 	}
 
 }
