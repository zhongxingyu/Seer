 package org.mosaic.filewatch.impl.manager;
 
 import com.google.common.io.ByteStreams;
 import com.google.common.io.Flushables;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.URL;
 import java.nio.charset.Charset;
 import java.nio.file.Path;
 import java.nio.file.PathMatcher;
 import java.nio.file.Paths;
 import java.nio.file.attribute.BasicFileAttributes;
 import java.util.*;
 import java.util.concurrent.atomic.AtomicBoolean;
 import javax.annotation.Nonnull;
 import javax.annotation.Nullable;
 import org.mosaic.Server;
 import org.mosaic.filewatch.WatchEvent;
 import org.mosaic.lifecycle.impl.util.BundleUtils;
 import org.osgi.framework.*;
 import org.osgi.framework.wiring.FrameworkWiring;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.util.PropertyPlaceholderHelper;
 
 import static java.nio.file.Files.*;
 import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
 import static java.nio.file.StandardOpenOption.WRITE;
 
 /**
  * @author arik
  */
 public class BundlesManager extends AbstractFileWatcherAdapter implements PropertyPlaceholderHelper.PlaceholderResolver
 {
     private static final Logger LOG = LoggerFactory.getLogger( BundlesManager.class );
 
     public static final PropertyPlaceholderHelper PROPERTY_PLACEHOLDER_HELPER = new PropertyPlaceholderHelper( "${", "}", ":", false );
 
     private static class BundleCollection
     {
         private long readTime;
 
         @Nonnull
         private Set<Path> targets = Collections.emptySet();
 
         private BundleCollection( long readTime )
         {
             this.readTime = readTime;
         }
     }
 
     @Nonnull
     private final BundleContext bundleContext;
 
     @Nonnull
     private final Server server;
 
     @Nonnull
     private final Map<Path, BundleCollection> bundleCollections = new HashMap<>();
 
     public BundlesManager( @Nonnull BundleContext bundleContext, @Nonnull Server server )
     {
         this.bundleContext = bundleContext;
         this.server = server;
     }
 
     @Nonnull
     public synchronized Bundle installModule( @Nonnull URL url ) throws IOException, BundleException
     {
         // download URL to a temporary file
         Path tempFile = createTempFile( "mosaic.deploy.module", ".jar" );
         try( InputStream in = url.openStream();
              OutputStream out = newOutputStream( tempFile, TRUNCATE_EXISTING, WRITE ) )
         {
             LOG.info( "Reading module from '{}'...", url );
             ByteStreams.copy( in, out );
             Flushables.flush( out, false );
         }
         return installBundle( tempFile );
     }
 
     @Override
     public String resolvePlaceholder( String placeholderName )
     {
         return this.bundleContext.getProperty( placeholderName );
     }
 
     @Override
     protected boolean matchesEvent( @Nonnull WatchEvent event )
     {
         switch( event )
         {
             case SCAN_STARTING:
             case DIR_ENTER:
             case FILE_ADDED:
             case FILE_MODIFIED:
             case FILE_DELETED:
             case DIR_EXIT:
             case SCAN_FINISHED:
                 return true;
             default:
                 return false;
         }
     }
 
     @Override
     protected boolean matchesSvnDir()
     {
         return false;
     }
 
     @Nonnull
     @Override
     protected Path getRoot()
     {
         return this.server.getLib();
     }
 
     @Nullable
     @Override
     protected PathMatcher getPathMatcher()
     {
         return null;
     }
 
     @Override
     protected synchronized void notify( @Nonnull ScanContext context,
                                         @Nonnull WatchEvent event,
                                         @Nullable Path path,
                                         @Nullable BasicFileAttributes attrs )
     {
         if( event == WatchEvent.SCAN_STARTING )
         {
             context.getAttributes().put( "bundlesToStart", new LinkedList<>() );
             context.getAttributes().put( "bundlesWereUninstalled", false );
         }
         else if( event == WatchEvent.FILE_DELETED )
         {
             if( path == null || attrs == null )
             {
                 throw new NullPointerException( "Path and file attributes must not be null when event is DELETE" );
             }
 
             String lowerCasedFileName = path.getFileName().toString().toLowerCase();
             if( lowerCasedFileName.endsWith( ".jar" ) )
             {
                 handleJarFileDeleted( context, path );
             }
             else if( lowerCasedFileName.endsWith( ".jars" ) || lowerCasedFileName.endsWith( ".maven" ) )
             {
                 handleJarCollectionFileDeleted( context, path );
             }
         }
         else if( event == WatchEvent.FILE_ADDED || event == WatchEvent.FILE_MODIFIED )
         {
             if( path == null || attrs == null )
             {
                 throw new NullPointerException( "Path and file attributes must not be null when event is ADD or MODIFIED" );
             }
 
             String lowerCasedFileName = path.getFileName().toString().toLowerCase();
             if( lowerCasedFileName.endsWith( ".jar" ) )
             {
                 handleJarFileAddedOrModified( context, path );
             }
             else if( lowerCasedFileName.endsWith( ".jars" ) || lowerCasedFileName.endsWith( ".maven" ) )
             {
                 handleJarCollectionFileAddedOrModified( context, path, attrs );
             }
         }
         else if( event == WatchEvent.SCAN_FINISHED )
         {
             handleScanFinished( context );
         }
     }
 
     private void handleJarFileDeleted( ScanContext context, Path path )
     {
         Bundle bundle = this.bundleContext.getBundle( path.toString() );
         if( bundle != null )
         {
             LOG.debug( "Deleted JAR file detected at: {} (uninstalling bundle '{}')", path, BundleUtils.toString( bundle ) );
             if( uninstallBundle( bundle ) )
             {
                 context.getAttributes().put( "bundlesWereUninstalled", true );
             }
         }
     }
 
     private void handleJarCollectionFileDeleted( ScanContext context, Path path )
     {
         BundleCollection cache = this.bundleCollections.remove( path );
         if( cache != null )
         {
             for( Path jar : cache.targets )
             {
                 boolean referenced = false;
                 for( BundleCollection validCache : this.bundleCollections.values() )
                 {
                     if( validCache.targets.contains( jar ) )
                     {
                         // jar is still referenced from any other jar collection - do not uninstall it
                         referenced = true;
                         break;
                     }
                 }
 
                 if( !referenced )
                 {
                     // file not referenced from any other jar-collection file (*.jarS or *.maven)
                     handleJarFileDeleted( context, jar );
                 }
             }
         }
     }
 
     private void handleJarFileAddedOrModified( @Nonnull ScanContext context, @Nonnull Path path )
     {
        @SuppressWarnings("unchecked")
         List<Bundle> bundlesToStart = context.getAttributes().require( "bundlesToStart", List.class );
 
         // check whether we need to install a new bundle or update an existing bundle
         Bundle bundle = this.bundleContext.getBundle( path.toString() );
         if( bundle == null )
         {
             bundle = installBundleNoError( path );
             if( bundle != null )
             {
                 // installed successfully - remember it for later so we will try to start it
                 bundlesToStart.add( bundle );
             }
         }
         else
         {
             // log the change, stop the bundle, update it, and remember it for start later on
             LOG.debug( "Updated JAR file detected at '{}' (updating bundle '{}')", path, BundleUtils.toString( bundle ) );
             if( stopAndUpdateBundle( bundle, path ) )
             {
                 bundlesToStart.add( bundle );
             }
         }
     }
 
     private void handleJarCollectionFileAddedOrModified( @Nonnull ScanContext context,
                                                          @Nonnull Path path,
                                                          @Nonnull BasicFileAttributes attrs )
     {
         // obtain list of installed bundles specified in this file from previous runs
         Set<Path> oldTargets = Collections.emptySet();
         BundleCollection cache = this.bundleCollections.get( path );
         if( cache != null )
         {
             oldTargets = cache.targets;
         }
 
         // obtain an up-to-date list of bundles this file specifies to install
         Set<Path> updatedTargets = getJarFiles( path, attrs );
 
         // uninstall any bundles present in the file on previous runs, but were removed from the file now
         for( Path oldTarget : oldTargets )
         {
             if( !updatedTargets.contains( oldTarget ) )
             {
                 Bundle oldBundle = this.bundleContext.getBundle( oldTarget.toString() );
                 if( oldBundle != null )
                 {
                     LOG.debug( "JAR file '{}' has been removed from '{}' (uninstalling bundle '{}')", oldTarget, path, BundleUtils.toString( oldBundle ) );
                     if( uninstallBundle( oldBundle ) )
                     {
                         context.getAttributes().put( "bundlesWereUninstalled", true );
                     }
                 }
             }
         }
     }
 
     private void handleScanFinished( ScanContext context )
     {
        @SuppressWarnings("unchecked")
         final List<Bundle> bundlesToStart = context.getAttributes().require( "bundlesToStart", List.class );
 
         // check bundles installed from jars/maven files
         for( BundleCollection bundleCollection : this.bundleCollections.values() )
         {
             for( Path target : bundleCollection.targets )
             {
                 if( exists( target ) )
                 {
                     Bundle bundle = this.bundleContext.getBundle( target.toString() );
                     if( bundle == null )
                     {
                         // file exists but there's no corresponding bundle (maybe removed in the past, and now restored)
                         bundle = installBundleNoError( target );
                         if( bundle != null )
                         {
                             bundlesToStart.add( bundle );
                         }
                     }
                     else
                     {
                         long fileLastMod;
                         try
                         {
                             BasicFileAttributes attrs = readAttributes( target, BasicFileAttributes.class );
                             fileLastMod = attrs.lastModifiedTime().toMillis();
                         }
                         catch( IOException ignore )
                         {
                             LOG.warn( "Cannot read file attributes of {}", target );
                             continue;
                         }
 
                         if( fileLastMod > bundle.getLastModified() && System.currentTimeMillis() - 2000 > fileLastMod )
                         {
                             if( stopAndUpdateBundle( bundle, target ) )
                             {
                                 bundlesToStart.add( bundle );
                             }
                         }
                     }
                 }
                 else
                 {
                     Bundle bundle = this.bundleContext.getBundle( target.toString() );
                     if( bundle != null )
                     {
                         if( uninstallBundle( bundle ) )
                         {
                             context.getAttributes().put( "bundlesWereUninstalled", true );
                         }
                     }
                 }
             }
         }
 
         final Boolean bundlesWereUninstalled = context.getAttributes().require( "bundlesWereUninstalled", Boolean.class );
 
         // if any bundle was uninstalled or we have any bundle to start, we should perform a package refresh now
         if( bundlesWereUninstalled || !bundlesToStart.isEmpty() )
         {
             // refresh packages so bundles are re-wired
             final AtomicBoolean refreshed = new AtomicBoolean( false );
             this.bundleContext.getBundle( 0 ).adapt( FrameworkWiring.class ).refreshBundles( null, new FrameworkListener()
             {
                 @Override
                 public void frameworkEvent( FrameworkEvent event )
                 {
                     if( !bundlesToStart.isEmpty() )
                     {
                         LOG.debug( "Refreshed OSGi packages before starting {} new/updated bundles", bundlesToStart.size() );
                     }
                     else if( bundlesWereUninstalled )
                     {
                         LOG.debug( "Refreshed OSGi packages (some bundles were uninstalled)" );
                     }
                     refreshed.set( true );
                 }
             } );
 
             // wait until refresh operation is completed
             while( true )
             {
                 if( refreshed.get() )
                 {
                     // start any bundles that were installed or updated
                     for( Bundle bundle : bundlesToStart )
                     {
                         startBundle( bundle );
                     }
                     break;
                 }
                 else
                 {
                     try
                     {
                         Thread.sleep( 100 );
                     }
                     catch( InterruptedException e )
                     {
                         break;
                     }
                 }
             }
         }
     }
 
     @Nullable
     private Bundle installBundleNoError( @Nonnull Path file )
     {
         try
         {
             return installBundle( file );
         }
         catch( Exception e )
         {
             LOG.error( "Could not install bundle from '{}': {}", file, e.getMessage(), e );
             return null;
         }
     }
 
     @Nonnull
     private Bundle installBundle( Path file ) throws BundleException, IOException
     {
         return this.bundleContext.installBundle( file.toString(), newInputStream( file ) );
     }
 
     private boolean uninstallBundle( @Nonnull Bundle bundle )
     {
         try
         {
             bundle.uninstall();
             return true;
         }
         catch( BundleException e )
         {
             LOG.error( "Could not uninstall bundle '{}' (its update location was removed): {}", BundleUtils.toString( bundle ), e.getMessage(), e );
             return false;
         }
     }
 
     private boolean stopAndUpdateBundle( @Nonnull Bundle bundle, @Nonnull Path file )
     {
         try
         {
             bundle.stop();
             bundle.update( newInputStream( file ) );
             return true;
         }
         catch( Exception e )
         {
             LOG.error( "Could not update bundle '{}' from '{}': {}", BundleUtils.toString( bundle ), file, e.getMessage(), e );
             return false;
         }
     }
 
     private void startBundle( @Nonnull Bundle bundle )
     {
         try
         {
             bundle.start();
         }
         catch( BundleException e )
         {
            switch( e.getType() )
            {
                case BundleException.MANIFEST_ERROR:
                case BundleException.DUPLICATE_BUNDLE_ERROR:
                case BundleException.RESOLVE_ERROR:
                case BundleException.START_TRANSIENT_ERROR:
                case BundleException.SECURITY_ERROR:
                    LOG.error( "Could not START bundle '{}': {}", BundleUtils.toString( bundle ), e.getMessage() );
                    break;

                default:
                    LOG.error( "Could not START bundle '{}': {}", BundleUtils.toString( bundle ), e.getMessage(), e );
                    break;
            }
             try
             {
                 bundle.stop();
             }
             catch( BundleException e1 )
             {
                 LOG.error( "Could not STOP bundle '{}' due to failure to START: {}", BundleUtils.toString( bundle ), e1.getMessage(), e1 );
             }
         }
     }
 
     @Nonnull
     private Set<Path> getJarFiles( @Nonnull Path path, @Nonnull BasicFileAttributes attrs )
     {
         long fileModificationTime = attrs.lastModifiedTime().toMillis();
 
         // check if we have the list of targets for this file already cached - and if that cache is not stale
         BundleCollection cache = this.bundleCollections.get( path );
         if( cache != null )
         {
             if( cache.readTime >= fileModificationTime )
             {
                 // cache is not stale - return the cache
                 return cache.targets;
             }
             // cache is stale - we will reload it
         }
         else
         {
             // no cache - create a new cache entry and proceed to read the target locations
             cache = new BundleCollection( fileModificationTime );
             this.bundleCollections.put( path, cache );
         }
 
         // is this a maven file?
         boolean maven = path.getFileName().toString().toLowerCase().endsWith( ".maven" );
 
         // read the locations of target files for this file
         Set<Path> files = new LinkedHashSet<>();
         try
         {
             for( String line : readAllLines( path, Charset.forName( "UTF-8" ) ) )
             {
                 String trimmedLine = line.trim();
                 if( !trimmedLine.isEmpty() && !trimmedLine.startsWith( "#" ) )
                 {
                     Path target;
                     if( maven )
                     {
                         String[] tokens = line.split( ":" );
                         if( tokens.length != 3 )
                         {
                             LOG.warn( "Illegal Maven artifact ID found in file '{}': {}", path, line );
                             continue;
                         }
 
                         target = Paths.get( this.bundleContext.getProperty( "user.home" ),
                                             ".m2",
                                             "repository",
                                             tokens[ 0 ].replace( '.', '/' ),               // group ID
                                             tokens[ 1 ],                                   // artifact ID
                                             tokens[ 2 ],                                   // version
                                             tokens[ 1 ] + "-" + tokens[ 2 ] + ".jar"       // ${artifactId}-${version}.jar
                         );
                     }
                     else
                     {
                         target = Paths.get( PROPERTY_PLACEHOLDER_HELPER.replacePlaceholders( trimmedLine, this ) );
                         if( !target.isAbsolute() )
                         {
                             target = path.resolveSibling( target );
                         }
                         target = target.normalize().toAbsolutePath();
                     }
                     files.add( target );
                 }
             }
         }
         catch( Exception e )
         {
             LOG.warn( "Could not read target JARs from file '{}': {}", path, e.getMessage(), e );
         }
 
         cache.targets = files;
         cache.readTime = fileModificationTime;
         return cache.targets;
     }
 }
