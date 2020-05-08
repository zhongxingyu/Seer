 package org.apache.archiva.redback.components.cache.ehcache;
 
 /*
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  *  http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 
 import net.sf.ehcache.Cache;
 import net.sf.ehcache.CacheManager;
 import net.sf.ehcache.Element;
 import net.sf.ehcache.Status;
 import net.sf.ehcache.config.CacheConfiguration;
 import net.sf.ehcache.config.Configuration;
 import net.sf.ehcache.config.DiskStoreConfiguration;
 import net.sf.ehcache.config.MemoryUnit;
 import net.sf.ehcache.store.MemoryStoreEvictionPolicy;
 import org.apache.archiva.redback.components.cache.CacheStatistics;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import javax.annotation.PostConstruct;
 import javax.annotation.PreDestroy;
 
 /**
  * EhcacheCache
  * configuration document available <a href="http://www.ehcache.org/documentation/configuration/index">EhcacheUserGuide</a>
  *
  * @author <a href="mailto:joakim@erdfelt.com">Joakim Erdfelt</a>
  */
 public class EhcacheCache<V, T>
     implements org.apache.archiva.redback.components.cache.Cache<V, T>
 {
 
     private Logger log = LoggerFactory.getLogger( getClass() );
 
     class Stats
         implements CacheStatistics
     {
         public void clear()
         {
             // TODO not supported anymore
             //ehcache.getStatistics().clearStatistics();
         }
 
         public double getCacheHitRate()
         {
             double hits = getCacheHits();
             double miss = getCacheMiss();
 
             if ( ( hits == 0 ) && ( hits == 0 ) )
             {
                 return 0.0;
             }
 
             return hits / ( hits + miss );
         }
 
         public long getCacheHits()
         {
             return ehcache.getStatistics().cacheHitCount();//.getCacheHits();
         }
 
         public long getCacheMiss()
         {
             return ehcache.getStatistics().cacheMissCount();// .getCacheMisses();
         }
 
         public long getSize()
         {
             //
             return ehcache.getStatistics().getSize();
         }
 
         public long getInMemorySize()
         {
            return ehcache.getStatistics().getLocalHeapSize();
             //return ehcache.calculateInMemorySize();
         }
     }
 
     /**
      * how often to run the disk store expiry thread. A large number of 120 seconds plus is recommended
      */
     private long diskExpiryThreadIntervalSeconds = 600;
 
     /**
      * Whether to persist the cache to disk between JVM restarts.
      */
     private boolean diskPersistent = true;
 
     /**
      * Location on disk for the ehcache store.
      */
     private String diskStorePath = System.getProperty( "java.io.tmpdir" ) + "/ehcache";
 
     private boolean eternal = false;
 
     private int maxElementsInMemory = 0;
 
     private String memoryEvictionPolicy = "LRU";
 
     private String name = "cache";
 
     /**
      * Flag indicating when to use the disk store.
      */
     private boolean overflowToDisk = false;
 
     private int timeToIdleSeconds = 600;
 
     private int timeToLiveSeconds = 300;
 
     /**
      * @since 2.0
      */
     private boolean overflowToOffHeap = false;
 
     /**
      * @since 2.0
      */
     private long maxBytesLocalHeap;
 
     /**
      * @since 2.0
      */
     private long maxBytesLocalOffHeap;
 
     private boolean failOnDuplicateCache = false;
 
     /**
      * @since 2.1
      */
     private int maxElementsOnDisk;
 
     /**
      * @since 2.1
      */
     //private String persistenceStrategy = PersistenceConfiguration.Strategy.LOCALTEMPSWAP.name();
 
     /**
      * @since 2.1
      */
     //private boolean synchronousWrites = false;
 
     private boolean statisticsEnabled = true;
 
     private CacheManager cacheManager = null;//CacheManager.getInstance();
 
     private net.sf.ehcache.Cache ehcache;
 
     private Stats stats;
 
     public void clear()
     {
         ehcache.removeAll();
         stats.clear();
     }
 
     @PostConstruct
     public void initialize()
     {
         stats = new Stats();
 
         boolean cacheManagerExists = CacheManager.getCacheManager( getName() ) != null;
 
         if ( cacheManagerExists )
         {
             if ( failOnDuplicateCache )
             {
                 throw new RuntimeException( "A previous cacheManager with name [" + getName() + "] exists." );
             }
             else
             {
                 log.warn( "skip duplicate cache {}", getName() );
                 cacheManager = CacheManager.getCacheManager( getName() );
             }
         }
         else
         {
             this.cacheManager = new CacheManager( new Configuration().name( getName() ).diskStore(
                 new DiskStoreConfiguration().path( getDiskStorePath() ) ) );
         }
 
         boolean cacheExists = cacheManager.cacheExists( getName() );
 
         if ( cacheExists )
         {
             if ( failOnDuplicateCache )
             {
                 throw new RuntimeException( "A previous cache with name [" + getName() + "] exists." );
             }
             else
             {
                 log.warn( "skip duplicate cache " + getName() );
                 ehcache = cacheManager.getCache( getName() );
             }
         }
 
         if ( !cacheExists )
         {
             CacheConfiguration cacheConfiguration =
                 new CacheConfiguration().name( getName() ).memoryStoreEvictionPolicy(
                     getMemoryStoreEvictionPolicy() ).eternal( isEternal() ).timeToLiveSeconds(
                     getTimeToLiveSeconds() ).timeToIdleSeconds(
                     getTimeToIdleSeconds() ).diskExpiryThreadIntervalSeconds(
                     getDiskExpiryThreadIntervalSeconds() ).overflowToOffHeap(
                     isOverflowToOffHeap() ).maxEntriesLocalDisk( getMaxElementsOnDisk() ).diskPersistent(
                     isDiskPersistent() ).overflowToDisk( overflowToDisk );
 
             if ( getMaxElementsInMemory() > 0 )
             {
                 cacheConfiguration = cacheConfiguration.maxEntriesLocalHeap( getMaxElementsInMemory() );
             }
 
             if ( getMaxBytesLocalHeap() > 0 )
             {
                 cacheConfiguration = cacheConfiguration.maxBytesLocalHeap( getMaxBytesLocalHeap(), MemoryUnit.BYTES );
             }
             if ( getMaxBytesLocalOffHeap() > 0 )
             {
                 cacheConfiguration =
                     cacheConfiguration.maxBytesLocalOffHeap( getMaxBytesLocalOffHeap(), MemoryUnit.BYTES );
             }
 
             ehcache = new Cache( cacheConfiguration );
 
             cacheManager.addCache( ehcache );
             // TODO not supported anymore?
             //ehcache.setStatisticsEnabled( statisticsEnabled );
         }
     }
 
     @PreDestroy
     public void dispose()
     {
         if ( cacheManager.getStatus().equals( Status.STATUS_ALIVE ) )
         {
             log.info( "Disposing cache: {}", ehcache );
             if ( this.ehcache != null )
             {
                 cacheManager.removeCache( this.ehcache.getName() );
                 ehcache = null;
             }
         }
         else
         {
             log.debug( "Not disposing cache, because cacheManager is not alive: {}", ehcache );
         }
     }
 
     public T get( V key )
     {
         if ( key == null )
         {
             return null;
         }
 
         Element elem = ehcache.get( key );
         if ( elem == null )
         {
             return null;
         }
         return (T) elem.getObjectValue();
     }
 
     public long getDiskExpiryThreadIntervalSeconds()
     {
         return diskExpiryThreadIntervalSeconds;
     }
 
     public String getDiskStorePath()
     {
         return diskStorePath;
     }
 
     public int getMaxElementsInMemory()
     {
         return maxElementsInMemory;
     }
 
     public String getMemoryEvictionPolicy()
     {
         return memoryEvictionPolicy;
     }
 
     public MemoryStoreEvictionPolicy getMemoryStoreEvictionPolicy()
     {
         return MemoryStoreEvictionPolicy.fromString( memoryEvictionPolicy );
     }
 
     public String getName()
     {
         return name;
     }
 
     public CacheStatistics getStatistics()
     {
         return stats;
     }
 
     public int getTimeToIdleSeconds()
     {
         return timeToIdleSeconds;
     }
 
     public int getTimeToLiveSeconds()
     {
         return timeToLiveSeconds;
     }
 
     public boolean hasKey( V key )
     {
         return ehcache.isKeyInCache( key );
     }
 
     public boolean isDiskPersistent()
     {
         return diskPersistent;
     }
 
     public boolean isEternal()
     {
         return eternal;
     }
 
     public boolean isOverflowToDisk()
     {
         return overflowToDisk;
     }
 
     public void register( V key, T value )
     {
         ehcache.put( new Element( key, value ) );
     }
 
     public T put( V key, T value )
     {
         // Multiple steps done to satisfy Cache API requirement for Previous object return.
         Element elem = null;
         Object previous = null;
         elem = ehcache.get( key );
         if ( elem != null )
         {
             previous = elem.getObjectValue();
         }
         elem = new Element( key, value );
         ehcache.put( elem );
         return (T) previous;
     }
 
     public T remove( V key )
     {
         Element elem = null;
         Object previous = null;
         elem = ehcache.get( key );
         if ( elem != null )
         {
             previous = elem.getObjectValue();
             ehcache.remove( key );
         }
 
         return (T) previous;
     }
 
     public void setDiskExpiryThreadIntervalSeconds( long diskExpiryThreadIntervalSeconds )
     {
         this.diskExpiryThreadIntervalSeconds = diskExpiryThreadIntervalSeconds;
     }
 
     public void setDiskPersistent( boolean diskPersistent )
     {
         this.diskPersistent = diskPersistent;
     }
 
     public void setDiskStorePath( String diskStorePath )
     {
         this.diskStorePath = diskStorePath;
     }
 
     public void setEternal( boolean eternal )
     {
         this.eternal = eternal;
     }
 
 
     public void setMaxElementsInMemory( int maxElementsInMemory )
     {
         this.maxElementsInMemory = maxElementsInMemory;
         if ( this.ehcache != null )
         {
             this.ehcache.getCacheConfiguration().setMaxElementsInMemory( this.maxElementsInMemory );
             this.ehcache.getCacheConfiguration().setMaxEntriesLocalHeap( this.maxElementsInMemory );
 
         }
     }
 
     public void setMemoryEvictionPolicy( String memoryEvictionPolicy )
     {
         this.memoryEvictionPolicy = memoryEvictionPolicy;
     }
 
     public void setName( String name )
     {
         this.name = name;
     }
 
     public void setOverflowToDisk( boolean overflowToDisk )
     {
         this.overflowToDisk = overflowToDisk;
     }
 
     public void setTimeToIdleSeconds( int timeToIdleSeconds )
     {
         if ( this.ehcache != null )
         {
             this.ehcache.getCacheConfiguration().setTimeToIdleSeconds( timeToIdleSeconds );
         }
         this.timeToIdleSeconds = timeToIdleSeconds;
     }
 
     public void setTimeToLiveSeconds( int timeToLiveSeconds )
     {
         if ( this.ehcache != null )
         {
             this.ehcache.getCacheConfiguration().setTimeToLiveSeconds( timeToIdleSeconds );
         }
         this.timeToLiveSeconds = timeToLiveSeconds;
     }
 
     public boolean isStatisticsEnabled()
     {
         return statisticsEnabled;
     }
 
     public void setStatisticsEnabled( boolean statisticsEnabled )
     {
         this.statisticsEnabled = statisticsEnabled;
     }
 
     public boolean isFailOnDuplicateCache()
     {
         return failOnDuplicateCache;
     }
 
     public void setFailOnDuplicateCache( boolean failOnDuplicateCache )
     {
         this.failOnDuplicateCache = failOnDuplicateCache;
     }
 
     public boolean isOverflowToOffHeap()
     {
         return overflowToOffHeap;
     }
 
     public void setOverflowToOffHeap( boolean overflowToOffHeap )
     {
         this.overflowToOffHeap = overflowToOffHeap;
     }
 
     public long getMaxBytesLocalHeap()
     {
         return maxBytesLocalHeap;
     }
 
     public void setMaxBytesLocalHeap( long maxBytesLocalHeap )
     {
         this.maxBytesLocalHeap = maxBytesLocalHeap;
     }
 
     public long getMaxBytesLocalOffHeap()
     {
         return maxBytesLocalOffHeap;
     }
 
     public void setMaxBytesLocalOffHeap( long maxBytesLocalOffHeap )
     {
         this.maxBytesLocalOffHeap = maxBytesLocalOffHeap;
     }
 
     public int getMaxElementsOnDisk()
     {
         return maxElementsOnDisk;
     }
 
     public void setMaxElementsOnDisk( int maxElementsOnDisk )
     {
         this.maxElementsOnDisk = maxElementsOnDisk;
         if ( this.ehcache != null )
         {
             this.ehcache.getCacheConfiguration().setMaxElementsOnDisk( this.maxElementsOnDisk );
             this.ehcache.getCacheConfiguration().maxEntriesLocalDisk( this.maxElementsOnDisk );
         }
     }
 
     /*public String getPersistenceStrategy()
     {
         return persistenceStrategy;
     }
 
     public void setPersistenceStrategy( String persistenceStrategy )
     {
         this.persistenceStrategy = persistenceStrategy;
     }
 
     public boolean isSynchronousWrites()
     {
         return synchronousWrites;
     }
 
     public void setSynchronousWrites( boolean synchronousWrites )
     {
         this.synchronousWrites = synchronousWrites;
     }*/
 }
