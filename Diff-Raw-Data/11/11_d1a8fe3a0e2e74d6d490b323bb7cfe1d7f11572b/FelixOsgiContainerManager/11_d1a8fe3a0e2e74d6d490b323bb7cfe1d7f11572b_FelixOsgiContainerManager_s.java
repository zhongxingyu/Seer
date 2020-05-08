 package com.atlassian.plugin.osgi.container.felix;
 
 import com.atlassian.plugin.osgi.container.OsgiContainerException;
 import com.atlassian.plugin.osgi.container.OsgiContainerManager;
 import com.atlassian.plugin.osgi.container.PackageScannerConfiguration;
 import com.atlassian.plugin.osgi.hostcomponents.HostComponentProvider;
 import com.atlassian.plugin.osgi.hostcomponents.HostComponentRegistration;
 import com.atlassian.plugin.osgi.hostcomponents.impl.DefaultComponentRegistrar;
 import com.atlassian.plugin.osgi.util.OsgiHeaderUtil;
 import com.atlassian.plugin.util.ClassLoaderUtils;
 import com.atlassian.plugin.event.PluginEventManager;
 import com.atlassian.plugin.event.events.PluginFrameworkShutdownEvent;
 import com.atlassian.plugin.event.events.PluginFrameworkStartedEvent;
 import com.atlassian.plugin.event.events.PluginFrameworkStartingEvent;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.lang.Validate;
 import org.apache.felix.framework.Felix;
 import org.apache.felix.framework.cache.BundleCache;
 import org.apache.felix.framework.util.FelixConstants;
 import org.apache.felix.framework.util.StringMap;
 import org.osgi.framework.*;
 import static org.twdata.pkgscanner.PackageScanner.jars;
 import static org.twdata.pkgscanner.PackageScanner.packages;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.FilenameFilter;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.*;
 import java.util.jar.JarFile;
 
 /**
  * Felix implementation of the OSGi container manager
  */
 public class FelixOsgiContainerManager implements OsgiContainerManager
 {
     private static final Log log = LogFactory.getLog(FelixOsgiContainerManager.class);
     private BundleRegistration registration = null;
     private Felix felix = null;
     private boolean felixRunning = false;
     private File cacheDirectory;
     private boolean disableMultipleBundleVersions = true;
 
     private final URL frameworkBundlesUrl;
     private PackageScannerConfiguration packageScannerConfig;
     public static final String OSGI_FRAMEWORK_BUNDLES_ZIP = "osgi-framework-bundles.zip";
     private File frameworkBundlesDir;
     private HostComponentProvider hostComponentProvider;
 
 
     /**
      * Constructs the container manager using the framework bundles zip file located in this library
      * @param frameworkBundlesDir The directory to unzip the framework bundles into.
      * @param packageScannerConfig The configuration for package scanning
      * @param provider The host component provider.  May be null.
      * @param eventManager The plugin event manager to register for init and shutdown events
      */
     public FelixOsgiContainerManager(File frameworkBundlesDir, PackageScannerConfiguration packageScannerConfig,
                                      HostComponentProvider provider, PluginEventManager eventManager)
     {
         this(ClassLoaderUtils.getResource(OSGI_FRAMEWORK_BUNDLES_ZIP, FelixOsgiContainerManager.class), frameworkBundlesDir,
                 packageScannerConfig, provider, eventManager);
     }
 
     /**
      * Constructs the container manager
      * @param frameworkBundlesZip The location of the zip file containing framework bundles
      * @param frameworkBundlesDir The directory to unzip the framework bundles into.
      * @param packageScannerConfig The configuration for package scanning
      * @param provider The host component provider.  May be null.
      * @param eventManager The plugin event manager to register for init and shutdown events
      */
     public FelixOsgiContainerManager(URL frameworkBundlesZip, File frameworkBundlesDir,
                                      PackageScannerConfiguration packageScannerConfig, HostComponentProvider provider,
                                      PluginEventManager eventManager)
     {
         Validate.notNull(frameworkBundlesZip, "The framework bundles zip is required");
         Validate.notNull(frameworkBundlesDir, "The framework bundles directory must not be null");
         Validate.notNull(packageScannerConfig, "The package scanner configuration must not be null");
         Validate.notNull(eventManager, "The plugin event manager is required");
 
         this.frameworkBundlesUrl = frameworkBundlesZip;
         this.packageScannerConfig = packageScannerConfig;
         this.frameworkBundlesDir = frameworkBundlesDir;
         this.hostComponentProvider = provider;
         eventManager.register(this);
     }
 
     public void setDisableMultipleBundleVersions(boolean val)
     {
         this.disableMultipleBundleVersions = val;
     }
 
     public void channel(PluginFrameworkStartingEvent event)
     {
         start();
     }
 
     public void channel(PluginFrameworkShutdownEvent event)
     {
         stop();
     }
 
 
     public void start() throws OsgiContainerException
     {
         if (isRunning())
             return;
 
         initialiseCacheDirectory();
 
         DefaultComponentRegistrar registrar = collectHostComponents(hostComponentProvider);
         // Create a case-insensitive configuration property map.
         final Map configMap = new StringMap(false);
         // Configure the Felix instance to be embedded.
         configMap.put(FelixConstants.EMBEDDED_EXECUTION_PROP, "true");
         // Add the bundle provided service interface package and the core OSGi
         // packages to be exported from the class path via the system bundle.
         configMap.put(Constants.FRAMEWORK_SYSTEMPACKAGES, OsgiHeaderUtil.determineExports(registrar.getRegistry(), packageScannerConfig));
 
         // Explicitly specify the directory to use for caching bundles.
         configMap.put(BundleCache.CACHE_PROFILE_DIR_PROP, cacheDirectory.getAbsolutePath());
 
         FelixLoggerBridge bridge = new FelixLoggerBridge(log);
         configMap.put(FelixConstants.LOG_LEVEL_PROP, String.valueOf(bridge.getLogLevel()));
 
         try
         {
             // Create host activator;
             registration = new BundleRegistration(frameworkBundlesUrl, frameworkBundlesDir, registrar);
             final List<BundleActivator> list = new ArrayList<BundleActivator>();
             list.add(registration);
 
             // Now create an instance of the framework with
             // our configuration properties and activator.
             felix = new Felix(bridge, configMap, list);
 
             // Now start Felix instance.  Starting in a different thread to explicity set daemon status
             Thread t = new Thread() {
                 @Override
                 public void run() {
                     try
                     {
 
                         felix.start();
                         felixRunning = true;
                     } catch (BundleException e)
                     {
                         throw new OsgiContainerException("Unable to start felix", e);
                     }
                 }
             };
             t.setDaemon(true);
             t.start();
 
             // Give it 10 seconds
             t.join(10 * 60 * 1000);
 
 
 
         }
         catch (Exception ex)
         {
             throw new OsgiContainerException("Unable to start OSGi container", ex);
         }
     }
 
     public void stop() throws OsgiContainerException
     {
         try
         {
             if (felixRunning)
                 felix.stop();
             
             felixRunning = false;
             felix = null;
         } catch (BundleException e)
         {
             throw new OsgiContainerException("Unable to stop OSGi container", e);
         }
     }
 
     public Bundle[] getBundles()
     {
         return registration.getBundles();
     }
 
     public ServiceReference[] getRegisteredServices()
     {
         return felix.getRegisteredServices();
     }
 
     public Bundle installBundle(File file) throws OsgiContainerException
     {
         try
         {
             return registration.install(file, disableMultipleBundleVersions);
         } catch (BundleException e)
         {
             throw new OsgiContainerException("Unable to install bundle", e);
         }
     }
 
     DefaultComponentRegistrar collectHostComponents(HostComponentProvider provider)
     {
         DefaultComponentRegistrar registrar = new DefaultComponentRegistrar();
         if (provider != null)
             provider.provide(registrar);
         return registrar;
     }
 
     void initialiseCacheDirectory() throws OsgiContainerException
     {
         try
         {
             cacheDirectory = new File(File.createTempFile("foo", "bar").getParentFile(), "felix");
             if (cacheDirectory.exists())
                 FileUtils.deleteDirectory(cacheDirectory);
 
             cacheDirectory.mkdir();
             cacheDirectory.deleteOnExit();
         } catch (IOException e)
         {
             throw new OsgiContainerException("Cannot create cache directory", e);
         }
     }
 
     public boolean isRunning()
     {
         return felixRunning;
     }
 
     public List<HostComponentRegistration> getHostComponentRegistrations()
     {
         return registration.getHostComponentRegistrations();
     }
 
     /**
      * Manages framwork-level framework bundles and host components registration, and individual plugin bundle
      * installation and removal.
      */
     static class BundleRegistration implements BundleActivator, BundleListener
     {
         private BundleContext bundleContext;
         private DefaultComponentRegistrar registrar;
         private List<ServiceRegistration> hostServicesReferences;
         private List<HostComponentRegistration> hostComponentRegistrations;
         private URL frameworkBundlesUrl;
         private File frameworkBundlesDir;
 
         public BundleRegistration(URL frameworkBundlesUrl, File frameworkBundlesDir, DefaultComponentRegistrar registrar)
         {
             this.registrar = registrar;
             this.frameworkBundlesUrl = frameworkBundlesUrl;
             this.frameworkBundlesDir = frameworkBundlesDir;
         }
 
         public void start(BundleContext context) throws Exception {
             this.bundleContext = context;
             context.addBundleListener(this);
 
             loadHostComponents(registrar);
             extractAndInstallFrameworkBundles();
         }
 
         public void stop(BundleContext ctx) throws Exception {
         }
 
         public void bundleChanged(BundleEvent evt) {
             switch (evt.getType()) {
                 case BundleEvent.INSTALLED:
                    log.warn("Installed bundle " + evt.getBundle().getSymbolicName() + " ("+evt.getBundle().getBundleId()+")");
                     break;
                 case BundleEvent.STARTED:
                    log.warn("Started bundle " + evt.getBundle().getSymbolicName() + " ("+evt.getBundle().getBundleId()+")");
                     break;
                 case BundleEvent.STOPPED:
                    log.warn("Stopped bundle " + evt.getBundle().getSymbolicName() + " ("+evt.getBundle().getBundleId()+")");
                     break;
                 case BundleEvent.UNINSTALLED:
                    log.warn("Uninstalled bundle " + evt.getBundle().getSymbolicName() + " ("+evt.getBundle().getBundleId()+")");
                     break;
             }
         }
 
         public Bundle install(File path, boolean uninstallOtherVersions) throws BundleException
         {
             Bundle bundle;
 
             if (uninstallOtherVersions)
             {
                 try
                 {
                     JarFile jar = new JarFile(path);
                     String name = jar.getManifest().getMainAttributes().getValue(Constants.BUNDLE_SYMBOLICNAME);
                     for (Bundle oldBundle : bundleContext.getBundles())
                     {
                         if (name.equals(oldBundle.getSymbolicName()))
                         {
                             log.info("Uninstalling existing version "+oldBundle.getHeaders().get(Constants.BUNDLE_VERSION));
                             oldBundle.uninstall();
                         }
                     }
 
                 } catch (IOException e)
                 {
                     throw new BundleException("Invalid bundle format", e);
                 }
             }
             try
             {
                 bundle = bundleContext.installBundle(path.toURL().toString());
             } catch (MalformedURLException e)
             {
                 throw new BundleException("Invalid path: "+path);
             }
             return bundle;
         }
 
         public Bundle[] getBundles()
         {
             return bundleContext.getBundles();
         }
 
         public List<HostComponentRegistration> getHostComponentRegistrations()
         {
             return hostComponentRegistrations;
         }
 
         private void loadHostComponents(DefaultComponentRegistrar registrar)
         {
             // Unregister any existing host components
             if (hostServicesReferences != null) {
                 for (ServiceRegistration reg : hostServicesReferences)
                     reg.unregister();
             }
 
             // Register host components as OSGi services
             hostServicesReferences = registrar.writeRegistry(bundleContext);
             hostComponentRegistrations = registrar.getRegistry();
         }
 
         private void extractAndInstallFrameworkBundles() throws IOException, BundleException
         {
             List<Bundle> bundles = new ArrayList<Bundle>();
             com.atlassian.plugin.util.FileUtils.conditionallyExtractZipFile(frameworkBundlesUrl, frameworkBundlesDir);
             for (File bundleFile : frameworkBundlesDir.listFiles(new FilenameFilter() {
                     public boolean accept(File file, String s) {
                         return s.endsWith(".jar"); 
                     }
                 }))
             {
                 bundles.add(install(bundleFile, false));
             }
 
             for (Bundle bundle : bundles)
             {
                 bundle.start();
             }
         }
     }
 
 }
