 /*******************************************************************************
  * Copyright (C) 2014 John Casey.
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  ******************************************************************************/
 package org.commonjava.shelflife.store.flat;
 
 import static org.apache.commons.io.IOUtils.closeQuietly;
 
 import java.io.File;
 import java.io.FileFilter;
 import java.io.FileInputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeSet;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.TimeUnit;
 
 import javax.annotation.PostConstruct;
 import javax.annotation.PreDestroy;
 import javax.enterprise.context.ApplicationScoped;
 import javax.inject.Inject;
 
 import org.commonjava.cdi.util.weft.ExecutorConfig;
 import org.commonjava.cdi.util.weft.ScheduledExecutor;
 import org.commonjava.cdi.util.weft.StoppableRunnable;
 import org.commonjava.shelflife.ExpirationManagerException;
 import org.commonjava.shelflife.inject.Shelflife;
 import org.commonjava.shelflife.model.Expiration;
 import org.commonjava.shelflife.store.ExpirationBlockStore;
 import org.commonjava.web.json.ser.JsonSerializer;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.gson.reflect.TypeToken;
 
 @ApplicationScoped
 public class FlatBlockStore
     implements ExpirationBlockStore
 {
 
     private static final String ENCODING = "UTF-8";
 
     private static final String PREFIX = "expiration-block-";
 
     private static final String SUFFIX = ".json";
 
     private final Logger logger = LoggerFactory.getLogger( getClass() );
 
     @Inject
     @Shelflife
     private JsonSerializer serializer;
 
     @Inject
     private FlatBlockStoreConfiguration config;
 
     @Inject
     @ExecutorConfig( daemon = true, named = "shelflife-flushcache", threads = 1, priority = 6 )
     @ScheduledExecutor
     private ScheduledExecutorService executor;
 
     private final Map<String, Set<Expiration>> blocks = new ConcurrentHashMap<>();
 
     public FlatBlockStore()
     {
     }
 
     public FlatBlockStore( final FlatBlockStoreConfiguration config, final JsonSerializer serializer, final ScheduledExecutorService executor )
     {
         this.config = config;
         this.serializer = serializer;
         this.executor = executor;
         startCacheManager();
     }
 
     @PostConstruct
     public void startCacheManager()
     {
         executor.scheduleAtFixedRate( new CacheFlusher(), config.getCacheFlushMillis(), config.getCacheFlushMillis(), TimeUnit.MILLISECONDS );
     }
 
     @Override
     public void writeBlocks( final Map<String, Set<Expiration>> currentBlocks )
         throws ExpirationManagerException
     {
         for ( final Map.Entry<String, Set<Expiration>> entry : currentBlocks.entrySet() )
         {
             writeBlock( entry.getKey(), entry.getValue() );
         }
     }
 
     @Override
     public void addToBlock( final String key, final Expiration expiration )
         throws ExpirationManagerException
     {
         Set<Expiration> block = getBlock( key );
         if ( block == null )
         {
             block = new TreeSet<Expiration>();
         }
 
         synchronized ( block )
         {
             if ( block.add( expiration ) )
             {
                 cacheBlock( key, block );
             }
         }
     }
 
     @Override
     public Set<Expiration> getBlock( final String key )
         throws ExpirationManagerException
     {
         Set<Expiration> block = blocks.get( key );
         if ( block == null )
         {
             final File f = getBlockFile( key );
             if ( f.exists() )
             {
                 FileInputStream stream = null;
                 try
                 {
                     stream = new FileInputStream( f );
 
                     final TypeToken<List<Expiration>> token = new TypeToken<List<Expiration>>()
                     {
                     };
 
                     logger.info( "Loading expiration block from: {}", f );
                     final List<Expiration> listing = serializer.fromStream( stream, ENCODING, token );
                     block = new TreeSet<Expiration>( listing );
                 }
                 catch ( final IOException e )
                 {
                     throw new ExpirationManagerException( "Failed to read block from: {}. Reason: {}", e, f, e.getMessage() );
                 }
                 finally
                 {
                     closeQuietly( stream );
                 }
             }
         }
 
         return block;
     }
 
     @Override
     public void removeFromBlock( final String key, final Expiration expiration )
         throws ExpirationManagerException
     {
         final Set<Expiration> block = getBlock( key );
         if ( block != null )
         {
             synchronized ( block )
             {
                 if ( block.remove( expiration ) )
                 {
                     if ( block.isEmpty() )
                     {
                         removeBlocks( key );
                     }
                     else
                     {
                         cacheBlock( key, block );
                     }
                 }
             }
         }
     }
 
     private void cacheBlock( final String key, final Set<Expiration> block )
     {
         blocks.put( key, block );
     }
 
     @Override
     @PreDestroy
     public synchronized void flushCaches()
         throws ExpirationManagerException
     {
         final Set<String> keys = new HashSet<String>( blocks.keySet() );
         for ( final String key : keys )
         {
             final Set<Expiration> block = blocks.get( key );
             writeBlock( key, block );
             blocks.remove( key );
         }
     }
 
     private void writeBlock( final String key, final Set<Expiration> block )
         throws ExpirationManagerException
     {
         final File f = getBlockFile( key );
         final File d = f.getParentFile();
         if ( d != null && !d.isDirectory() )
         {
             d.mkdirs();
         }
 
         final List<Expiration> sortedBlock = new ArrayList<Expiration>( block );
         Collections.sort( sortedBlock );
 
         FileWriter writer = null;
         try
         {
             writer = new FileWriter( f );
             logger.info( "Writing expiration block to: {}", f );
             final String json = serializer.toString( sortedBlock );
 
             writer.write( json );
         }
         catch ( final IOException e )
         {
             throw new ExpirationManagerException( "Failed to write expiration block to: {}. Reason: {}", e, f, e.getMessage() );
         }
         finally
         {
             closeQuietly( writer );
         }
     }
 
     @Override
     public void removeBlocks( final Set<String> currentKeys )
         throws ExpirationManagerException
     {
         for ( final String key : currentKeys )
         {
             blocks.remove( key );
             final File f = getBlockFile( key );
             if ( f.exists() )
             {
                 logger.info( "Deleting expiration block at: {}", f );
                 f.delete();
             }
         }
     }
 
     @Override
     public void removeBlocks( final String... currentKeys )
         throws ExpirationManagerException
     {
         for ( final String key : currentKeys )
         {
             blocks.remove( key );
             final File f = getBlockFile( key );
             if ( f.exists() )
             {
                 logger.info( "Deleting expiration block at: {}", f );
                 f.delete();
             }
         }
     }
 
     protected File getBlockFile( final String key )
     {
         final String fname = PREFIX + key + SUFFIX;
 
         config.getStorageDirectory()
               .mkdirs();
 
         return new File( config.getStorageDirectory(), fname );
     }
 
     private final class CacheFlusher
         extends StoppableRunnable
     {
         @Override
         protected void doExecute()
         {
             try
             {
                 logger.debug( "Flushing block caches to disk." );
                 flushCaches();
             }
             catch ( final ExpirationManagerException e )
             {
                logger.error( "Failed to write cached blocks to disk: {}", e, e.getMessage() );
             }
         }
 
     }
 
     @Override
     public List<String> listKeysInOrder()
     {
         final File dir = config.getStorageDirectory();
         if ( dir.isDirectory() )
         {
             final File[] blockFiles = dir.listFiles( new FileFilter()
             {
                 @Override
                 public boolean accept( final File file )
                 {
                     return isBlockFile( file );
                 }
             } );
 
             if ( blockFiles != null && blockFiles.length > 0 )
             {
                 final List<String> keys = new ArrayList<String>();
                 for ( final File file : blockFiles )
                 {
                     keys.add( getKey( file ) );
                 }
 
                 Collections.sort( keys );
 
                 return keys;
             }
         }
 
         return null;
     }
 
     protected boolean isBlockFile( final File file )
     {
         final String name = file.getName();
         return name.startsWith( PREFIX ) && name.endsWith( SUFFIX );
     }
 
     protected String getKey( final File file )
     {
         return file.getName()
                    .substring( PREFIX.length(), file.getName()
                                                     .length() - SUFFIX.length() );
     }
 }
