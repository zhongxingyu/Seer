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
 import net.sf.ehcache.store.MemoryStoreEvictionPolicy;
 import org.apache.archiva.redback.components.cache.CacheStatistics;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import javax.annotation.PostConstruct;
 
 /**
  * EhcacheCache 
 * configuration document  available <a href="http://ehcache.sourceforge.net/EhcacheUserGuide.html>EhcacheUserGuide</a>
  *  
  * @author <a href="mailto:joakim@erdfelt.com">Joakim Erdfelt</a>
  *
  * 
  */
 public class EhcacheCache
     implements org.apache.archiva.redback.components.cache.Cache
 {
     
     private Logger log = LoggerFactory.getLogger( getClass() );    
     
     class Stats
         implements CacheStatistics
     {
         public void clear()
         {
             ehcache.clearStatistics();
         }
 
         public double getCacheHitRate()
         {
             double hits = getCacheHits();
             double miss = getCacheMiss();
 
             if ( ( hits == 0 ) && ( hits == 0 ) )
             {
                 return 0.0;
             }
 
             return (double) hits / (double) ( hits + miss );
         }
 
         public long getCacheHits()
         {
             return ehcache.getStatistics().getCacheHits();
         }
 
         public long getCacheMiss()
         {
             return ehcache.getStatistics().getCacheMisses();
         }
 
         public long getSize()
         {
             return ehcache.getMemoryStoreSize() + ehcache.getDiskStoreSize();
         }
 
     }
 
     /**
      * how often to run the disk store expiry thread. A large number of 120 seconds plus is recommended
      * 
      */
     private long diskExpiryThreadIntervalSeconds = 600;
 
     /**
      * Whether to persist the cache to disk between JVM restarts.
      * 
      */
     private boolean diskPersistent = true;
 
     /**
      * Location on disk for the ehcache store.
      * 
      */
     private String diskStorePath = System.getProperty( "java.io.tmpdir" ) + "/ehcache";
 
     /**
      *
      */
     private boolean eternal = false;
 
     /**
      *
      */
     private int maxElementsInMemory = 1000;
 
     /**
      *
      */
     private String memoryEvictionPolicy = "LRU";
 
     /**
      *
      */
     private String name = "cache";
 
     /**
      * Flag indicating when to use the disk store.
      * 
      */
     private boolean overflowToDisk = false;
 
     /**
      *
      */
     private int timeToIdleSeconds = 600;
 
     /**
      *
      */
     private int timeToLiveSeconds = 300;
     
     /**
      *
      */    
     private boolean failOnDuplicateCache = false;
 
     private boolean statisticsEnabled = true;
 
     private CacheManager cacheManager = CacheManager.getInstance();
 
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
 
         if (!cacheExists)
         {
             ehcache = new Cache( getName(), getMaxElementsInMemory(), getMemoryStoreEvictionPolicy(), isOverflowToDisk(),
                                  getDiskStorePath(), isEternal(), getTimeToLiveSeconds(), getTimeToIdleSeconds(),
                                  isDiskPersistent(), getDiskExpiryThreadIntervalSeconds(), null );
 
 
             cacheManager.addCache( ehcache );
             ehcache.setStatisticsEnabled( statisticsEnabled );
         }
     }    
 
     public void dispose()
     {
         if ( cacheManager.getStatus().equals( Status.STATUS_ALIVE ) )
         {
             log.info( "Disposing cache: " + ehcache );
             if ( this.ehcache != null )
             {
                 cacheManager.removeCache( this.ehcache.getName() );
                 ehcache = null;
             }
         }
         else
         {
             log.debug( "Not disposing cache, because cacheManager is not alive: " + ehcache );
         }
     }
 
     public Object get( Object key )
     {
         if (key == null)
         {
             return null;
         }
         Element elem = ehcache.get( key );
         if ( elem == null )
         {
             return null;
         }
         return elem.getObjectValue();
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
 
     public boolean hasKey( Object key )
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
 
     public void register( Object key, Object value )
     {
         ehcache.put( new Element( key, value ) );
     }
     
     public Object put( Object key, Object value )
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
         return previous;
     }
 
     public Object remove( Object key )
     {
         Element elem = null;
         Object previous = null;
         elem = ehcache.get( key );
         if ( elem != null )
         {
             previous = elem.getObjectValue();
             ehcache.remove( key );
         }
 
         return previous;
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
         this.timeToIdleSeconds = timeToIdleSeconds;
     }
 
     public void setTimeToLiveSeconds( int timeToLiveSeconds )
     {
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
 }
