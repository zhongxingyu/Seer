 package org.jtheque.osgi.server;
 
 import org.jtheque.utils.SystemProperty;
 import org.jtheque.utils.collections.CollectionUtils;
 import org.jtheque.utils.io.FileUtils;
 
 import org.apache.felix.framework.Felix;
 import org.osgi.framework.Bundle;
 import org.osgi.framework.BundleException;
 import org.osgi.framework.Version;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.File;
 import java.io.FilenameFilter;
 import java.util.Arrays;
 import java.util.Map;
 
 /*
  * Copyright JTheque (Baptiste Wicht)
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 /**
  * A Felix OSGi server implementation.
  *
  * @author Baptiste Wicht
  */
 public final class FelixServer implements OSGiServer {
     private static final File BUNDLES_DIR = new File(SystemProperty.USER_DIR.get(), "bundles");
     private static final File CACHE_DIR = new File(SystemProperty.USER_DIR.get(), "cache");
 
     private final Map<String, Bundle> bundles = CollectionUtils.newHashMap(50);
 
     private Felix felix;
 
     @Override
     public void start() {
         getLogger().debug("Starting Felix Server");
 
         emptyFelixCache();
 
         startFelixServer();
 
         autoDeploy();
 
         getLogger().info("Felix started with {} cached bundles", bundles.size());
     }
 
 
     /**
      * Empty the cache.
      */
     private void emptyFelixCache() {
         long startTime = System.currentTimeMillis();
 
         for (File f : CACHE_DIR.listFiles()) {
             FileUtils.delete(f);
         }
 
         getLogger().debug("Cache cleaned in {} ms", System.currentTimeMillis() - startTime);
     }
 
     /**
      * Start the Felix server.
      */
     private void startFelixServer() {
         long startTime = System.currentTimeMillis();
 
         Map<String, Object> configMap = CollectionUtils.newHashMap(2);
 
         configMap.put("felix.cache.bufsize", "16384");
         configMap.put("org.osgi.framework.storage", CACHE_DIR.getAbsolutePath());
         //configMap.put("org.osgi.framework.bootdelegation", "org.netbeans.lib.profiler, org.netbeans.lib.profiler.*");
 
         try {
             felix = new Felix(configMap);
             felix.start();
         } catch (Exception e) {
             getLogger().error("Unable to start Felix due to {}", e.getMessage());
             getLogger().error(e.getMessage(), e);
         }
 
         for (Bundle bundle : felix.getBundleContext().getBundles()) {
             bundles.put(bundle.getSymbolicName(), bundle);
         }
 
         getLogger().debug("Felix Server started in {} ms", System.currentTimeMillis() - startTime);
     }
 
     /**
      * Auto deploy the bundles in the "bundles" folder of the user dir.
      */
     private void autoDeploy() {
         long startTime = System.currentTimeMillis();
 
         getLogger().info("Auto deploy start");
 
         for (File f : BUNDLES_DIR.listFiles(new JarFileFilter())) {
             String name = f.getName();
 
             getLogger().info("auto-deploy {} file" + f.getName());
 
             installIfNecessary(name.substring(0, name.indexOf('-')), f.getAbsolutePath());
         }
 
         getLogger().info("Auto deploy ends in {} ms", System.currentTimeMillis() - startTime);
     }
 
     @Override
     public void stop() {
         try {
             stopBundles();
 
             felix.stop();
             felix.waitForStop(1000);
 
             getLogger().info("Felix stopped");
         } catch (BundleException e) {
             getLogger().error("Unable to stop Felix due to {}", e.getMessage());
             getLogger().error(e.getMessage(), e);
         } catch (InterruptedException e) {
             getLogger().error("Unable to stop Felix due to {}", e.getMessage());
             getLogger().error(e.getMessage(), e);
         }
     }
 
     /**
      * Stop all the bundles other than the felix framework system bundle.
      */
     private void stopBundles() {
         try {
             for (Bundle bundle : bundles.values()) {
                 if (!"org.apache.felix.framework".equals(bundle.getSymbolicName())) {
                     bundle.stop();
                 }
             }
         } catch (BundleException e) {
             LoggerFactory.getLogger(getClass()).error("Cannot stop System Bundle");
             LoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
         }
     }
 
     @Override
     public Bundle[] getBundles() {
         return felix.getBundleContext().getBundles();
     }
 
     @Override
     public void installBundle(String path) {
         try {
             String url = path.startsWith("file:") ? path : "file:" + path;
 
             Bundle bundle = felix.getBundleContext().installBundle(url);
 
             bundles.put(bundle.getSymbolicName(), bundle);
 
            getLogger().info("Installed bundle {}", bundle.getSymbolicName());
         } catch (BundleException e) {
             getLogger().error("Unable to install bundle at path {} due to {}", path, e.getMessage());
             getLogger().error(e.getMessage(), e);
         }
     }
 
     @Override
     public void startBundle(String bundleName) {
         Bundle bundle = getBundle(bundleName);
 
         if (bundle != null) {
             try {
                 getLogger().debug("Start bundle {}", bundle.getSymbolicName());
 
                 bundle.start();
 
                 getLogger().debug("Started bundle {}", bundle.getSymbolicName());
             } catch (BundleException e) {
                 getLogger().error("Unable to start bundle ({}) due to {}", bundleName, e.getMessage());
                 getLogger().error(e.getMessage(), e);
 
                 debug();
             }
         } else {
             getLogger().error("Unable to start bundle ({}) because it's not installed", bundleName);
         }
     }
 
     @Override
     public void stopBundle(String bundleName) {
         Bundle bundle = getBundle(bundleName);
 
         if (bundle != null) {
             try {
                 getLogger().debug("Stop bundle {}", bundle.getSymbolicName());
 
                 bundle.stop();
 
                 getLogger().info("Stopped bundle {}", bundle.getSymbolicName());
             } catch (BundleException e) {
                 getLogger().error("Unable to stop bundle ({}) due to {}", bundleName, e.getMessage());
                 getLogger().error(e.getMessage(), e);
 
                 debug();
             }
         } else {
             getLogger().error("Unable to stop bundle ({}) because it's not installed", bundleName);
         }
     }
 
     @Override
     public void uninstallBundle(String bundleName) {
         Bundle bundle = getBundle(bundleName);
 
         if (bundle != null) {
             try {
                 getLogger().debug("Uninstall bundle {}", bundle.getSymbolicName());
 
                 bundle.uninstall();
 
                 getLogger().info("Uninstalled bundle {}", bundle.getSymbolicName());
             } catch (BundleException e) {
                 getLogger().error("Unable to uninstall bundle ({}) due to {}", bundleName, e.getMessage());
                 getLogger().error(e.getMessage(), e);
             }
         } else {
             getLogger().error("Unable to uninstall bundle ({}) because it's not installed", bundleName);
         }
     }
 
     @Override
     public Version getVersion(String bundleName) {
         return getVersion(getBundle(bundleName));
     }
 
     @Override
     public Version getVersion(Bundle bundle) {
         if (bundle != null) {
             return bundle.getVersion();
         }
 
         return null;
     }
 
     /**
      * Debug the server. It seems print all the installed bundles with informations about them.
      */
     private void debug() {
         getLogger().debug("Installed bundles : ");
 
         for (Bundle bundle : getBundles()) {
             getLogger().debug("{} ({}) : {}", new Object[]{bundle.getSymbolicName(), getVersion(bundle).toString(), getState(bundle)});
             getLogger().debug("Exported packages : {}", bundle.getHeaders().get("Export-Package"));
             getLogger().debug("Imported packages : {}", bundle.getHeaders().get("Import-Package"));
             getLogger().debug("Registered services: {}", Arrays.toString(bundle.getRegisteredServices()));
         }
     }
 
     @Override
     public BundleState getState(String bundle) {
         if (!isInstalled(bundle)) {
             return BundleState.NOT_INSTALLED;
         }
 
         return getState(getBundle(bundle));
     }
 
     @Override
     public BundleState getState(Bundle bundle) {
         int state = bundle.getState();
 
         switch (state) {
             case 1:
                 return BundleState.UNINSTALLED;
             case 2:
                 return BundleState.INSTALLED;
             case 4:
                 return BundleState.RESOLVED;
             case 8:
                 return BundleState.STARTING;
             case 16:
                 return BundleState.STOPPING;
             case 32:
                 return BundleState.ACTIVE;
             default:
                 getLogger().error("Undefined bundle state bundle : {}, state : {}", bundle.getSymbolicName(), state);
                 return null;
         }
     }
 
     /**
      * Install the module with the given name if it's not installed.
      *
      * @param name The module name.
      * @param path The path to the module file.
      */
     private void installIfNecessary(String name, String path) {
         if (!isInstalled(name)) {
             installBundle(path);
         }
     }
 
     @Override
     public boolean isInstalled(String bundleName) {
         return getBundle(bundleName) != null;
     }
 
     @Override
     public Bundle getBundle(String bundleName) {
         return bundles.get(bundleName);
     }
 
     /**
      * Return the logger of the server.
      *
      * @return The logger of the server.
      */
     private Logger getLogger() {
         return LoggerFactory.getLogger(getClass());
     }
 
     /**
      * A file name filter to keep only JAR files.
      *
      * @author Baptiste Wicht
      */
     private static final class JarFileFilter implements FilenameFilter {
         @Override
         public boolean accept(File dir, String name) {
             return name.endsWith(".jar");
         }
     }
 }
