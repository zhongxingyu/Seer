 package org.mosaic.runner.watcher;
 
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.util.*;
 import org.osgi.framework.*;
 import org.osgi.framework.wiring.BundleRevisions;
 import org.osgi.framework.wiring.FrameworkWiring;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * @author arik
  */
 public class BundlesWatcher implements Runnable {
 
     private static final Logger LOG = LoggerFactory.getLogger( BundlesWatcher.class );
 
     private static final int DEFAULT_SCAN_INTERVAL = 1000;
 
     private final BundleContext bundleContext;
 
     private final Path ignoreFlagFile;
 
     private final long scanInterval;
 
     private final Collection<WatchedResourceProvider> watchedResourceProviders = new LinkedList<>();
 
    private final Set<Long> unresolvedBundles = new HashSet<>();
 
     private boolean stop;
 
     public BundlesWatcher( BundleContext bundleContext, Path ignoreFlagFile, Path... directories ) {
         this.bundleContext = bundleContext;
         this.ignoreFlagFile = ignoreFlagFile;
         this.scanInterval = Long.getLong( "bundleScanInterval", DEFAULT_SCAN_INTERVAL );
         for( Path directory : directories ) {
             this.watchedResourceProviders.add( new JarLinksWatchedResourceProvider( bundleContext, directory ) );
             this.watchedResourceProviders.add( new JarsWatchedResourceProvider( bundleContext, directory ) );
         }
     }
 
     public void start() {
         this.stop = false;
         Thread thread = new Thread( this, "BundlesWatcher" );
         thread.setDaemon( true );
         thread.start();
     }
 
     public void scan() {
 
         // if the ignore file flag exists, it means the user wants us to avoid checking updates until he removes it
         if( Files.exists( this.ignoreFlagFile ) ) {
             return;
         }
 
         // no ignore file - continue with scanning
         for( WatchedResourceProvider watchedResourceProvider : this.watchedResourceProviders ) {
             for( WatchedResource watchedResource : watchedResourceProvider.getWatchedResources() ) {
                 watchedResource.checkForUpdates();
             }
         }
 
         // build the bundle dependency graph of bundles that need to be refreshed
         Collection<Bundle> bundlesToRefresh = getBundlesToRefresh();
 
         // if we can refresh, do it
         if( bundlesToRefresh != null ) {
 
             Bundle systemBundle = this.bundleContext.getBundle( 0 );
             FrameworkWiring frameworkWiring = systemBundle.adapt( FrameworkWiring.class );
             frameworkWiring.refreshBundles( bundlesToRefresh, new FrameworkListener() {
                 @Override
                 public void frameworkEvent( FrameworkEvent event ) {
                     // attempt to start un-started bundles
                     startBundles( bundleContext.getBundles() );
                 }
             } );
 
         } else {
 
             // attempt to start un-started bundles
             startBundles( this.bundleContext.getBundles() );
 
         }
     }
 
     private Collection<Bundle> getBundlesToRefresh() {
         Bundle[] bundles = this.bundleContext.getBundles();
         if( bundles == null ) {
             return Collections.emptyList();
         }
 
         Collection<Bundle> bundlesToRefresh = null;
         for( Bundle bundle : bundles ) {
             BundleRevisions revisions = bundle.adapt( BundleRevisions.class );
             if( revisions != null && revisions.getRevisions().size() > 1 ) {
                 bundlesToRefresh = addBundle( bundlesToRefresh, bundle );
             } else if( bundle.getState() == Bundle.UNINSTALLED ) {
                 bundlesToRefresh = addBundle( bundlesToRefresh, bundle );
             }
         }
         return bundlesToRefresh;
     }
 
     private Collection<Bundle> addBundle( Collection<Bundle> bundles, Bundle bundle ) {
         if( bundles == null ) {
             bundles = new LinkedList<>();
         }
         bundles.add( bundle );
         return bundles;
     }
 
     private void startBundles( Bundle[] bundles ) {
         for( Bundle bundle : bundles ) {
             if( bundle.getState() == Bundle.INSTALLED || bundle.getState() == Bundle.RESOLVED ) {
                 try {
                     bundle.start();
                    this.unresolvedBundles.remove( bundle.getBundleId() );
                 } catch( BundleException e ) {
                     // we basically ignore this because something is still missing for it - it will be started
                     // in a future cycle when the missing dependency is added to the server
                    if( !this.unresolvedBundles.contains( bundle.getBundleId() ) ) {
                        this.unresolvedBundles.add( bundle.getBundleId() );
                         LOG.warn( "Could not start bundle '{}-{}[{}]': {}",
                                   new Object[] {
                                           bundle.getSymbolicName(),
                                           bundle.getVersion(),
                                           bundle.getBundleId(),
                                           e.getMessage()
                                   } );
                     }
                 }
             }
         }
     }
 
     @Override
     public void run() {
         while( !this.stop ) {
 
             // sleep for a while
             try {
                 Thread.sleep( this.scanInterval );
             } catch( InterruptedException e ) {
                 break;
             }
 
             // scan all watched resources
             scan();
 
         }
     }
 }
