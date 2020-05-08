 package org.mosaic.launcher.osgi;
 
 import java.io.IOException;
 import java.lang.management.ManagementFactory;
 import java.net.URI;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.nio.file.Paths;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Properties;
 import java.util.StringTokenizer;
 import java.util.concurrent.Executors;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.TimeUnit;
 import javax.annotation.Nonnull;
 import javax.annotation.Nullable;
 import org.apache.felix.framework.Felix;
 import org.mosaic.launcher.MosaicInstance;
 import org.osgi.framework.Bundle;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.BundleException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import static java.nio.file.Files.*;
 import static java.nio.file.StandardOpenOption.READ;
 import static java.util.Arrays.asList;
 import static org.mosaic.launcher.util.SystemError.bootstrapError;
 
 /**
  * @author arik
  */
 public class BootBundlesWatcher implements Runnable
 {
     private static final Logger LOG = LoggerFactory.getLogger( BootBundlesWatcher.class );
 
     private static final String PATH_SEPARATOR = System.getProperty( "path.separator" );
 
     @Nonnull
     private final List<Bundle> bundles;
 
     @Nullable
     private ScheduledExecutorService executorService;
 
     public BootBundlesWatcher( @Nonnull MosaicInstance mosaic, @Nonnull Felix felix )
     {
         Properties properties = mosaic.getProperties();
 
         List<Bundle> bundles = new LinkedList<>();
         for( String name : asList( "api", "lifecycle", "config", "database", "event", "mail", "metrics", "security", "shell", "web" ) )
         {
             Path bundlePath = null;
 
             String location = properties.getProperty( "mosaic.boot." + name );
             if( location != null )
             {
                 bundlePath = mosaic.getHome().resolve( location ).normalize().toAbsolutePath();
                 verifyInstallableBundle( name, bundlePath );
             }
 
             String versionedFilename = name + "-" + mosaic.getVersion() + ".jar";
             if( bundlePath == null )
             {
                 StringTokenizer tokenizer = new StringTokenizer( ManagementFactory.getRuntimeMXBean().getClassPath(), PATH_SEPARATOR, false );
                 while( tokenizer.hasMoreTokens() )
                 {
                     String item = tokenizer.nextToken();
                     if( item.contains( "/" + name + "/target/classes" ) )
                     {
                         bundlePath = Paths.get( item ).getParent().resolve( versionedFilename );
                         verifyInstallableBundle( name, bundlePath );
                         break;
                     }
                    else if( name.endsWith( versionedFilename ) )
                     {
                         bundlePath = Paths.get( item );
                         verifyInstallableBundle( name, bundlePath );
                         break;
                     }
                 }
             }
             if( bundlePath == null )
             {
                 bundlePath = mosaic.getHome().resolve( "boot" ).resolve( versionedFilename ).normalize().toAbsolutePath();
                 verifyInstallableBundle( name, bundlePath );
             }
 
             try
             {
                 BundleContext bc = felix.getBundleContext();
                 Bundle bundle = bc.installBundle( bundlePath.toUri().toString(), newInputStream( bundlePath, READ ) );
                 bundle.start();
                 bundles.add( bundle );
             }
             catch( Exception e )
             {
                 throw bootstrapError( "Could not install boot bundle at '{}': {}", bundlePath, e.getMessage(), e );
             }
         }
         this.bundles = bundles;
 
         this.executorService = Executors.newSingleThreadScheduledExecutor();
         this.executorService.scheduleWithFixedDelay( this, 3, 1, TimeUnit.SECONDS );
     }
 
     public void stop() throws InterruptedException
     {
         ScheduledExecutorService service = this.executorService;
         if( service != null )
         {
             service.shutdown();
             service.awaitTermination( 30, TimeUnit.SECONDS );
             this.executorService = null;
         }
     }
 
     @Override
     public synchronized void run()
     {
         for( Bundle bundle : this.bundles )
         {
             // find the bundle file's modification time
             long bundleFileModTime;
             try
             {
                 URI bundleLocationUri = URI.create( bundle.getLocation() );
                 bundleFileModTime = Files.getLastModifiedTime( Paths.get( bundleLocationUri ) ).toMillis();
             }
             catch( IOException ignore )
             {
                 continue;
             }
 
             // check if it hasn't been modified in the last 2 seconds, and that it's newer than the bundle's modification time
             if( bundleFileModTime < System.currentTimeMillis() - 2000 && bundleFileModTime > bundle.getLastModified() )
             {
                 try
                 {
                     bundle.update();
                 }
                 catch( BundleException e )
                 {
                     LOG.error( "Bundle {}-{}[{}] has been updated ({}) but the reload failed: {}",
                                bundle.getSymbolicName(), bundle.getVersion(), bundle.getBundleId(),
                                bundle.getLocation(),
                                e.getMessage(), e );
                 }
             }
         }
     }
 
     private void verifyInstallableBundle( @Nonnull String name, @Nonnull Path file )
     {
         if( !exists( file ) )
         {
             throw bootstrapError( "Could not find bundle '{}' at '{}'", name, file );
         }
         else if( !isRegularFile( file ) )
         {
             throw bootstrapError( "Bundle at '{}' is not a file", file );
         }
         else if( !isReadable( file ) )
         {
             throw bootstrapError( "Bundle at '{}' is not readable", file );
         }
     }
 }
