 package org.mosaic.config.impl;
 
 import com.google.common.collect.ArrayListMultimap;
 import com.google.common.collect.Maps;
 import com.google.common.collect.Multimap;
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.nio.file.attribute.BasicFileAttributes;
 import java.util.Collections;
 import java.util.Map;
 import java.util.Properties;
 import java.util.concurrent.ConcurrentHashMap;
 import javax.annotation.Nonnull;
 import javax.annotation.Nullable;
 import javax.annotation.PostConstruct;
 import javax.annotation.PreDestroy;
 import org.mosaic.filewatch.WatchEvent;
 import org.mosaic.filewatch.WatchRoot;
 import org.mosaic.filewatch.annotation.FileWatcher;
 import org.mosaic.lifecycle.MethodEndpoint;
 import org.mosaic.lifecycle.annotation.*;
 import org.mosaic.util.collect.HashMapEx;
 import org.mosaic.util.collect.MapEx;
 import org.mosaic.util.convert.ConversionService;
 import org.mosaic.util.reflection.MethodHandle;
 import org.mosaic.util.reflection.MethodParameter;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import static com.google.common.collect.Multimaps.synchronizedMultimap;
 
 /**
  * @author arik
  */
 @Bean
 public class ConfigurationManager
 {
     private static final Logger LOG = LoggerFactory.getLogger( ConfigurationManager.class );
 
     @Nonnull
     private final Map<String, Properties> configurations = new ConcurrentHashMap<>( 50 );
 
     @Nonnull
     private ConversionService conversionService;
 
     @Nullable
     private Multimap<String, MethodEndpoint> endpoints;
 
     @ServiceRef
     public void setConversionService( @Nonnull ConversionService conversionService )
     {
         this.conversionService = conversionService;
     }
 
     @PostConstruct
     public void init()
     {
         this.endpoints = synchronizedMultimap( ArrayListMultimap.<String, MethodEndpoint>create() );
     }
 
     @PreDestroy
     public void destory()
     {
         this.endpoints = null;
     }
 
     @MethodEndpointBind(Configurable.class)
     public void addConfigurable( @Nonnull MethodEndpoint endpoint )
     {
         Multimap<String, MethodEndpoint> endpoints = this.endpoints;
         if( endpoints != null )
         {
             Configurable ann = ( Configurable ) endpoint.getType();
             endpoints.put( ann.value(), endpoint );
 
             Properties properties = this.configurations.get( ann.value() );
             notifyConfigurable( properties != null ? properties : new Properties(), endpoint );
         }
     }
 
     @MethodEndpointUnbind(Configurable.class)
     public void removeConfigurable( @Nonnull MethodEndpoint endpoint )
     {
         Multimap<String, MethodEndpoint> endpoints = this.endpoints;
         if( endpoints != null )
         {
             Configurable ann = ( Configurable ) endpoint.getType();
             endpoints.remove( ann.value(), endpoint );
         }
     }
 
     @FileWatcher(root = WatchRoot.ETC,
                 pattern = "*.properties",
                  event = { WatchEvent.FILE_ADDED, WatchEvent.FILE_MODIFIED })
     public void onFileModified( @Nonnull Path file, @Nonnull BasicFileAttributes attrs ) throws IOException
     {
         // load configuration data
         final Properties properties = new Properties();
         properties.load( new ByteArrayInputStream( Files.readAllBytes( file ) ) );
 
         // store (possibly replace previous) configuration in memory
         String fileName = file.getFileName().toString();
         String configurationName = fileName.substring( 0, fileName.length() - ".properties".length() );
         this.configurations.put( configurationName, properties );
 
         // notify interested endpoints
         Multimap<String, MethodEndpoint> endpoints = this.endpoints;
         if( endpoints != null )
         {
             for( MethodEndpoint endpoint : endpoints.get( configurationName ) )
             {
                 notifyConfigurable( properties, endpoint );
             }
         }
     }
 
     @FileWatcher(root = WatchRoot.ETC,
                  pattern = "**/*.properties",
                  event = WatchEvent.FILE_DELETED)
     public void onFileDeleted( @Nonnull Path file ) throws IOException
     {
         // store (possibly replace previous) configuration in memory
         String fileName = file.getFileName().toString();
         String configurationName = fileName.substring( 0, fileName.length() - ".properties".length() );
         this.configurations.remove( configurationName );
 
         // notify interested endpoints
         Multimap<String, MethodEndpoint> endpoints = this.endpoints;
         if( endpoints != null )
         {
             final Properties emptyProperties = new Properties();
             for( MethodEndpoint endpoint : endpoints.get( configurationName ) )
             {
                 notifyConfigurable( emptyProperties, endpoint );
             }
         }
     }
 
     private void notifyConfigurable( final Properties properties, MethodEndpoint endpoint )
     {
         try
         {
             endpoint.createInvoker( new MethodHandle.ParameterResolver()
             {
                 @Nullable
                 @Override
                 public Object resolve( @Nonnull MethodParameter parameter,
                                        @Nonnull MapEx<String, Object> resolveContext )
                 {
                     if( parameter.isMapEx() )
                     {
                         return new HashMapEx<>( Maps.fromProperties( properties ), conversionService );
                     }
                     else if( parameter.isMap() )
                     {
                         return Maps.fromProperties( properties );
                     }
                     else if( parameter.isProperties() )
                     {
                         return properties;
                     }
                     else
                     {
                         throw new MethodHandle.UnresolvableArgumentException( "unsupported @Configurable parameter type '" + parameter.getType() + "'", parameter );
                     }
                 }
             } ).resolve( Collections.<String, Object>emptyMap() ).invoke();
         }
         catch( Exception e )
         {
             LOG.error( "Could not provide configuration to @Configurable '{}': {}", endpoint, e.getMessage(), e );
         }
     }
 }
