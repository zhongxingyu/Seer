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
 package uk.co.tfd.sm.memory.ehcache;
 
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.lang.management.ManagementFactory;
 import java.lang.ref.WeakReference;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.management.MBeanServer;
 
 import net.sf.ehcache.CacheManager;
 import net.sf.ehcache.management.ManagementService;
 
 import org.apache.commons.io.IOUtils;
import org.apache.felix.scr.annotations.Activate;
 import org.apache.felix.scr.annotations.Component;
 import org.apache.felix.scr.annotations.Deactivate;
 import org.apache.felix.scr.annotations.Modified;
 import org.apache.felix.scr.annotations.Property;
 import org.apache.felix.scr.annotations.Service;
 import org.sakaiproject.nakamura.api.memory.Cache;
 import org.sakaiproject.nakamura.api.memory.CacheManagerService;
 import org.sakaiproject.nakamura.api.memory.CacheScope;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * The <code>CacheManagerServiceImpl</code>
  */
@Component(immediate = true, metatype=true)
@Service(value=CacheManagerService.class)
 public class CacheManagerServiceImpl implements CacheManagerService {
 
   public static final String DEFAULT_CACHE_CONFIG = "sling/ehcacheConfig.xml";
 
   @Property( value = DEFAULT_CACHE_CONFIG)
   public static final String CACHE_CONFIG = "cache-config";
 
  @Property(value = "The Sakai Foundation")
   static final String SERVICE_VENDOR = "service.vendor";
 
   @Property(value = "Cache Manager Service Implementation")
   static final String SERVICE_DESCRIPTION = "service.description";
   
   @SuppressWarnings("unused")
   @Property()
   public static final String BIND_ADDRESS = "bind-address";
   
   @SuppressWarnings("unused")
   @Property(value="sling/ehcache/data")
   public static final String CACHE_STORE = "cache-store";
 
   private static final String CONFIG_PATH = "uk/co/tfd/sm/memory/ehcache/ehcacheConfig.xml";
 
   private static final Logger LOGGER = LoggerFactory.getLogger(CacheManagerServiceImpl.class);
   private CacheManager cacheManager;
   private Map<String, Cache<?>> caches = new HashMap<String, Cache<?>>();
   private ThreadLocalCacheMap requestCacheMapHolder = new ThreadLocalCacheMap();
   private ThreadLocalCacheMap threadCacheMapHolder = new ThreadLocalCacheMap();
 
   public CacheManagerServiceImpl() throws IOException {
   }
   
  @Activate
   protected void activate(Map<String, Object> properties) throws FileNotFoundException, IOException {
 	  String config = toString(properties.get(CACHE_CONFIG), DEFAULT_CACHE_CONFIG);
 	  File configFile = new File(config);
 	  if ( configFile.exists() ) {
 		  LOGGER.info("Configuring Cache from {} ",configFile.getAbsolutePath());
 		  InputStream in = null;
 		  try {
 			  in = processConfig(new FileInputStream(configFile), properties);
 			  cacheManager = new CacheManager(in);
 		  } finally {
 			  if ( in != null ) {
 				  in.close();
 			  }
 		  }
 	  } else {
 		    LOGGER.info("Configuring Cache from Classpath Default {} ", CONFIG_PATH);
 		    InputStream in = processConfig(this.getClass().getClassLoader().getResourceAsStream(CONFIG_PATH), properties);
 		    if ( in == null ) {
 		    	throw new IOException("Unable to open config at classpath location "+CONFIG_PATH);
 		    }
 		    cacheManager = new CacheManager(in);
 		    in.close();		  
 	  }
 
 	  final WeakReference<CacheManagerServiceImpl> ref = new WeakReference<CacheManagerServiceImpl>(this);
     /*
      * Add in a shutdown hook, for safety
      */
     Runtime.getRuntime().addShutdownHook(new Thread() {
       /*
        * (non-Javadoc)
        *
        * @see java.lang.Thread#run()
        */
       @Override
       public void run() {
         try {
         	CacheManagerServiceImpl cm = ref.get();
         	if ( cm != null ) {
         		cm.deactivate();
         	}
         } catch (Throwable t) {
         	LOGGER.debug(t.getMessage(),t);
         }
       }
     });
 
     // register the cache manager with JMX
     MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
     ManagementService.registerMBeans(cacheManager, mBeanServer, true, true,
         true, true);
 
   }
 
   private String toString(Object object, String defaultValue) {
 	  if ( object == null ) {
 		  return defaultValue;
 	  }
 	return String.valueOf(object);
   }
 
   private InputStream processConfig(InputStream in,
 		Map<String, Object> properties) throws IOException {
 	  if ( in == null ) {
 		  return null;
 	  }
 	  StringBuilder config = new StringBuilder(IOUtils.toString(in, "UTF-8"));
 	  in.close();
 	  int pos = 0;
 	  for(;;) {
 		  int start = config.indexOf("${",pos);
 		  if ( start < 0 ) {
 			  break;
 		  }
 		  int end = config.indexOf("}", start);
 		  if ( end < 0 ) {
 				throw new IllegalArgumentException(
 						"Config file malformed, unterminated variable "
 								+ config.substring(start,
 										Math.min(start + 10, config.length())));
 		  }
 		  String key = config.substring(start+2, end);
 		  if ( properties.containsKey(key)) {
 			  String replacement = (String) properties.get(key);
 			  config.replace(start, end+1, replacement);
 			  pos = start + replacement.length();
 		  } else {
 			  throw new IllegalArgumentException("Missing replacement property "+key);
 		  }
 	  }
 	  return new ByteArrayInputStream(config.toString().getBytes("UTF-8"));
 	  
   }
 
 /**
    * perform a shutdown
    */
   @Deactivate
   public void deactivate() {
 	  if ( cacheManager != null ) {
 		  cacheManager.shutdown();
 		  cacheManager = null;
 	  }
   }
 
   /**
    * {@inheritDoc}
    *
    * @see org.sakaiproject.nakamura.api.memory.CacheManagerService#getCache(java.lang.String)
    */
   public <V> Cache<V> getCache(String name, CacheScope scope) {
     switch (scope) {
     case INSTANCE:
       return getInstanceCache(name);
     case CLUSTERINVALIDATED:
       return getInstanceCache(name);
     case CLUSTERREPLICATED:
       return getInstanceCache(name);
     case REQUEST:
       return getRequestCache(name);
     case THREAD:
       return getThreadCache(name);
     default:
       return getInstanceCache(name);
     }
   }
 
   /**
    * Generate a cache bound to the thread.
    *
    * @param name
    * @return
    */
   @SuppressWarnings("unchecked")
   private <V> Cache<V> getThreadCache(String name) {
     Map<String, Cache<?>> threadCacheMap = threadCacheMapHolder.get();
     Cache<V> threadCache = (Cache<V>) threadCacheMap.get(name);
     if (threadCache == null) {
       threadCache = new MapCacheImpl<V>();
       threadCacheMap.put(name, threadCache);
     }
     return threadCache;
   }
 
   /**
    * Generate a cache bound to the request
    *
    * @param name
    * @return
    */
   @SuppressWarnings("unchecked")
   private <V> Cache<V> getRequestCache(String name) {
     Map<String, Cache<?>> requestCacheMap = requestCacheMapHolder.get();
     Cache<V> requestCache = (Cache<V>) requestCacheMap.get(name);
     if (requestCache == null) {
       requestCache = new MapCacheImpl<V>();
       requestCacheMap.put(name, requestCache);
     }
     return requestCache;
   }
 
   /**
    * @param name
    * @return
    */
   @SuppressWarnings("unchecked")
   private <V> Cache<V> getInstanceCache(String name) {
     if (name == null) {
       return new CacheImpl<V>(cacheManager, null);
     } else {
       Cache<V> c = (Cache<V>) caches.get(name);
       if (c == null) {
         c = new CacheImpl<V>(cacheManager, name);
         caches.put(name, c);
       }
       return c;
     }
   }
 
   /**
    * {@inheritDoc}
    *
    * @see org.sakaiproject.nakamura.api.memory.CacheManagerService#unbind(org.sakaiproject.nakamura.api.memory.CacheScope)
    */
   public void unbind(CacheScope scope) {
     switch (scope) {
     case REQUEST:
       unbindRequest();
       break;
     case THREAD:
       unbindThread();
       break;
     }
   }
 
   /**
    *
    */
   private void unbindThread() {
     Map<String, Cache<?>> threadCache = threadCacheMapHolder.get();
     for (Cache<?> cache : threadCache.values()) {
       cache.clear();
     }
     threadCacheMapHolder.remove();
   }
 
   /**
    *
    */
   private void unbindRequest() {
     Map<String, Cache<?>> requestCache = requestCacheMapHolder.get();
     for (Cache<?> cache : requestCache.values()) {
       cache.clear();
     }
     requestCacheMapHolder.remove();
   }
 
 }
