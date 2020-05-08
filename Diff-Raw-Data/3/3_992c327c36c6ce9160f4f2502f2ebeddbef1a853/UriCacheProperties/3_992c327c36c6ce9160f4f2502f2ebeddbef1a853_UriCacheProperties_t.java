 package org.hackystat.utilities.uricache;
 
 import java.util.Properties;
 import java.util.logging.Level;
 
 /**
  * Provides configuration properties for the UriCache library.
  * 
  * @author Pavel Senin.
  * 
  */
 public class UriCacheProperties {
 
   /** Cache default region name. */
   private String uriCaheRegionName = "UriCache";
 
   /** Cache elements idle time. 24 hours default time. */
   private Long maxIdleTime = 86400L;
 
   /** Cache default capacity. */
   private Long maxCacheCapacity = 50000L;
 
   /** JCS cache configuration properties. */
   private Properties cacheProperties = null;
 
   /** JCS DC plug-in storage path. */
   private String dcStoragePath = System.getProperties().getProperty("java.io.tmpdir");
 
   /** UriCache logging level */
   private Level loggerLevel = Level.OFF;
 
   /**
    * Creates new instance and sets default properties.
    */
   public UriCacheProperties() {
     cacheProperties = setupProperties();
   }
 
   /**
    * Sets maximum idle time for this cache items.
    * 
    * @param idleTime the item idle time.
    */
   public void setMaxIdleTime(Long idleTime) {
     this.maxIdleTime = idleTime;
   }
 
   /**
    * Sets maximum capacity for this cache.
    * 
    * @param maxCacheCapacity maximal cache capacity.
    */
   public void setMaxMemoryCacpacity(Long maxCacheCapacity) {
     this.maxCacheCapacity = maxCacheCapacity;
   }
 
   /**
    * Sets the path to the cache storage folder.
    * 
    * @param storagePath the cache storage folder.
    */
   public void setCacheStoragePath(String storagePath) {
     this.dcStoragePath = storagePath;
     this.cacheProperties.setProperty("jcs.auxiliary.indexedDiskCache.attributes.DiskPath",
         this.dcStoragePath);
   }
 
   /**
    * Reports the path to the cache storage folder.
    * 
    * @return path to the cache storage folder.
    */
   public String getCacheStoragePath() {
     return this.dcStoragePath;
   }
 
   /**
    * Constructs configuration properties for the cache instance.
    * 
    * @return cache properties.
    */
   private Properties setupProperties() {
 
     Properties prop = new Properties();
     //
     // this is JCS required part - configuring default cache properties
     //
     prop.setProperty("jcs.default", "indexedDiskCache");
     prop.setProperty("jcs.default.cacheattributes",
         "org.apache.jcs.engine.CompositeCacheAttributes");
     prop.setProperty("jcs.default.cacheattributes.MaxObjects", this.maxCacheCapacity.toString());
     prop.setProperty("jcs.default.cacheattributes.MemoryCacheName",
         "org.apache.jcs.engine.memory.lru.LRUMemoryCache");
     //
     // ------- UriCache region cache attributes ---------
     //
     prop.setProperty("jcs.region." + this.uriCaheRegionName, "indexedDiskCache");
     prop.setProperty("jcs.region." + this.uriCaheRegionName + ".cacheattributes",
         "org.apache.jcs.engine.CompositeCacheAttributes");
     prop.setProperty("jcs.region." + this.uriCaheRegionName + ".cacheattributes.MaxObjects",
         this.maxCacheCapacity.toString());
     prop.setProperty("jcs.region." + this.uriCaheRegionName + ".cacheattributes.MemoryCacheName",
         "org.apache.jcs.engine.memory.lru.LRUMemoryCache");
     prop.setProperty("jcs.region." + this.uriCaheRegionName
         + ".cacheattributes.ShrinkerIntervalSeconds", "300");
     prop.setProperty("jcs.region." + this.uriCaheRegionName + ".cacheattributes.UseMemoryShrinker",
         "true");
     prop.setProperty("jcs.region." + this.uriCaheRegionName + ".cacheattributes.UseDisk", "true");
     prop
        .setProperty("jcs.region." + this.uriCaheRegionName 
            + ".cacheattributes.UseRemote", "false");
     prop.setProperty("jcs.region." + this.uriCaheRegionName + ".cacheattributes.UseLateral",
         "false");
     prop.setProperty("jcs.region." + this.uriCaheRegionName + ".cacheattributes.MaxSpoolPerRun",
         "500");
     //
     // -------- elements attributes --------
     //
     prop.setProperty("jcs.region." + this.uriCaheRegionName + ".elementattributes",
         "org.apache.jcs.engine.ElementAttributes");
     prop.setProperty("jcs.region." + this.uriCaheRegionName + ".elementattributes.IsEternal",
         "false");
     prop.setProperty("jcs.region." + this.uriCaheRegionName + ".elementattributes.IsLateral",
         "false");
     prop.setProperty("jcs.region." + this.uriCaheRegionName + ".elementattributes.MaxLifeSeconds",
         this.maxIdleTime.toString());
     prop.setProperty("jcs.region." + this.uriCaheRegionName + ".elementattributes.IdleTime",
         this.maxIdleTime.toString());
     prop.setProperty("jcs.region." + this.uriCaheRegionName + ".elementattributes.isSpool", "true");
     //
     // -------- disk cache elements attributes --------
     //
     prop.setProperty("jcs.auxiliary.indexedDiskCache",
         "org.apache.jcs.auxiliary.disk.indexed.IndexedDiskCacheFactory");
     prop.setProperty("jcs.auxiliary.indexedDiskCache.attributes",
         "org.apache.jcs.auxiliary.disk.indexed.IndexedDiskCacheAttributes");
     prop.setProperty("jcs.auxiliary.indexedDiskCache.attributes.MaxPurgatorySize", "300000");
     prop.setProperty("jcs.auxiliary.indexedDiskCache.attributes.MaxKeySize", "500000");
     prop.setProperty("jcs.auxiliary.indexedDiskCache.attributes.MaxRecycleBinSize", "50000");
     prop.setProperty("jcs.auxiliary.indexedDiskCache.attributes.OptimizeAtRemoveCount", "300000");
     prop.setProperty("jcs.auxiliary.indexedDiskCache.attributes.EventQueueType", "SINGLE");
     prop.setProperty("jcs.auxiliary.indexedDiskCache.attributes.EventQueuePoolName",
         "disk_cache_event_queue");
     //
     // --------- disk cache tricks
     //
     prop.setProperty("thread_pool.default.boundarySize", "2000");
     prop.setProperty("thread_pool.default.maximumPoolSize", "150");
     prop.setProperty("thread_pool.default.minimumPoolSize", "4");
     prop.setProperty("thread_pool.default.keepAliveTime", "350000");
     prop.setProperty("thread_pool.default.whenBlockedPolicy", "RUN");
     prop.setProperty("thread_pool.default.startUpSize", "4");
     prop.setProperty("thread_pool.disk_cache_event_queue.useBoundary", "false");
     prop.setProperty("thread_pool.disk_cache_event_queue.minimumPoolSize", "2");
     prop.setProperty("thread_pool.disk_cache_event_queue.keepAliveTime", "3500");
     prop.setProperty("thread_pool.disk_cache_event_queue.startUpSize", "10");
 
     return prop;
 
   }
 
   /**
    * Reports full set of configuration properties.
    * 
    * @return configuration properties.
    */
   public Properties getProperties() {
     return this.cacheProperties;
   }
 
   /**
    * Reports the cache name associated with this properties object.
    * 
    * @return this cache region name.
    */
   public String getCacheRegionName() {
     return this.uriCaheRegionName;
   }
 
   /**
    * Sets the cache region name.
    * 
    * @param cacheName name to set.
    */
   public void setCacheRegionName(String cacheName) {
     this.uriCaheRegionName = cacheName;
   }
 
   /**
    * Reports the logger level set in the properties.
    * 
    * @return logger level.
    */
   public Level getLoggerLevel() {
     return this.loggerLevel;
   }
 
   /**
    * Set the logger level.
    * 
    * @param level logger level to set.
    */
   public void setLoggerLevel(Level level) {
     this.loggerLevel = level;
   }
 
 }
