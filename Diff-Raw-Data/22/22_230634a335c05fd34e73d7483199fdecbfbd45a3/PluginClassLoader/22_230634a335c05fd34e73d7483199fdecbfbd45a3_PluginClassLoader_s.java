 /**
  *  Copyright (C) 2009 Progress Software, Inc. All rights reserved.
  *  http://fusesource.com
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *         http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  */
 package org.fusesource.meshkeeper.distribution;
 
 import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.util.Arrays;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Properties;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.fusesource.meshkeeper.classloader.ClassLoaderServer;
 import org.fusesource.meshkeeper.control.ControlService;
 import org.fusesource.meshkeeper.distribution.event.EventClient;
 import org.fusesource.meshkeeper.distribution.registry.RegistryClient;
 import org.fusesource.meshkeeper.distribution.remoting.RemotingClient;
 import org.fusesource.meshkeeper.distribution.repository.RepositoryClient;
 import org.fusesource.meshkeeper.util.internal.FileSupport;
 import org.fusesource.meshkeeper.util.internal.IOSupport;
 
 /**
  * PluginClassLoader
  * <p>
  * Description:
  * </p>
  * 
  * @author cmacnaug
  * @version 1.0
  */
 public class PluginClassLoader extends URLClassLoader {
 
     private static final Log LOG = LogFactory.getLog(PluginClassLoader.class);
 
     private static final HashSet<String> SPI_PACKAGES = new HashSet<String>();
     private static final HashSet<String> PARENT_FIRST = new HashSet<String>();
     private static final String DEFAULT_PLUGIN_VERSION = getDefaultPluginVersion();
 
     private static final PluginClassLoader DEFAULT_PLUGIN_CLASSLOADER = new PluginClassLoader(Thread.currentThread().getContextClassLoader());
     private static final boolean USE_PARENT_FIRST = false;
     private static PluginResolver PLUGIN_RESOLVER;
     private static final HashMap<String, List<File>> RESOLVED_PLUGINS = new HashMap<String, List<File>>();
 
     static {
         SPI_PACKAGES.add(DefaultDistributor.class.getPackage().getName());
         SPI_PACKAGES.add(RemotingClient.class.getPackage().getName());
         SPI_PACKAGES.add(RepositoryClient.class.getPackage().getName());
         SPI_PACKAGES.add(RegistryClient.class.getPackage().getName());
         SPI_PACKAGES.add(EventClient.class.getPackage().getName());
         SPI_PACKAGES.add(ControlService.class.getPackage().getName());
         SPI_PACKAGES.add(ClassLoaderServer.class.getPackage().getName());
 
         PARENT_FIRST.add(LogFactory.class.getPackage().getName());
         PARENT_FIRST.add("org.apache.log4j");
         PARENT_FIRST.add("org.apache.maven");
         PARENT_FIRST.add("org.springframework");
         PARENT_FIRST.add("javax.jms");
     }
 
     //Keep track of files already added to the classpath:
     private final HashSet<String> resolvedFiles = new HashSet<String>();
 
     /**
      * @return Returns the default plugin classloader.
      */
     public static final PluginClassLoader getDefaultPluginLoader() {
         return DEFAULT_PLUGIN_CLASSLOADER;
     }
 
     /**
      * If the current context class loader is a plugin class loader it is
      * returned otherwise the default plugin classloader is returned
      * 
      * @return
      */
     public static final PluginClassLoader getContextPluginLoader() {
         ClassLoader cl = Thread.currentThread().getContextClassLoader();
         if (cl instanceof PluginClassLoader) {
             return (PluginClassLoader) cl;
         } else {
             return getDefaultPluginLoader();
         }
     }
 
     PluginClassLoader(ClassLoader parent) {
         super(new URL[] {}, parent);
         //        delegate = new PluginCL(this.getParent());
     }
 
     @Override
     public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
         Class<?> c = findLoadedClass(name);
         if (c == null) {
 
             int pDelim = name.lastIndexOf(".");
             if (USE_PARENT_FIRST || (pDelim > 0 && SPI_PACKAGES.contains(name.substring(0, pDelim)))) {
                 c = super.loadClass(name, resolve);
             } else {
                 for (String prefix : PARENT_FIRST) {
                     if (name.startsWith(prefix)) {
                         try {
                             c = super.loadClass(name, resolve);
                         } catch (ClassNotFoundException cnfe) {
                         }
                     }
                 }
 
                 if (c == null) {
                     try {
                         c = findClass(name);
                     } catch (ClassNotFoundException cnfe) {
                         c = super.loadClass(name, resolve);
                     }
                 }
             }
         }
 
         if (c == null) {
             throw new ClassNotFoundException(name);
         }
 
         if (resolve) {
             resolveClass(c);
         }
         return c;
     }
 
     @Override
     public Class<?> findClass(String name) throws ClassNotFoundException {
 
         Class<?> c = super.findClass(name);
         if (LOG.isDebugEnabled()) {
             LOG.debug("PluginCL-" + this.hashCode() + " Found class: " + name);
             //" from: " + c.getProtectionDomain().getCodeSource().getLocation());
         }
         return c;
     }
 
     @Override
     public URL getResource(String name) {
         URL url = findResource(name);
 
         // if local search failed, delegate to parent
         if (url == null) {
             url = getParent().getResource(name);
         }
         return url;
     }
 
     @Override
     public Enumeration<URL> getResources(String name) throws IOException {
         Enumeration<URL> results = null;
         try {
             results = super.findResources(name);
         } catch (IOException ioe) {
         }
 
         // if local search failed, delegate to parent
         if (results == null || !results.hasMoreElements()) {
             results = getParent().getResources(name);
         }
         return results;
     }
 
     /**
      * Finds the resource with the specified name on the URL search path.
      * 
      * @param name
      *            the name of the resource
      * @return a <code>URL</code> for the resource, or <code>null</code> if the
      *         resource could not be found.
      */
     @Override
     public URL findResource(final String name) {
         URL url = null;
         if (name.equals("log4j.properties")) {
             url = getParent().getResource(name);
             if (url != null) {
                 return url;
             }
         }
         url = super.findResource(name);
         if (LOG.isDebugEnabled()) {
 
             if (url == null) {
                 LOG.debug("Couldn't find resource " + name + " in path: " + Arrays.toString(super.getURLs()));
             } else {
                 LOG.debug("Looking for resource: " + name + " found: " + url);
             }
         }
 
         return url;
     }
 
     public void addUrl(URL url) {
         super.addURL(url);
     }
 
     /**
      * @param key
      */
     public Class<?> loadPlugin(String path, String key) throws IOException, ClassNotFoundException {
 
         String uri = path + key;
 
         try {
             //Possible that the plugin properties are already on the classpath
             //try loading there first: 
             Properties properties = loadProperties(this, uri);
             if (properties == null) {
                 loadPlugin(key);
                 properties = loadProperties(this, uri);
             }
 
             if (properties == null) {
                 throw new IOException("Could not find factory properties: " + uri);
             }
 
             String className = properties.getProperty("class");
             if (className == null) {
                 throw new IOException("Expected property is missing: class");
             }
 
             // The properties file can point us to the artifact to use...
             String mavenArtifact = properties.getProperty("maven.artifact");
             if (mavenArtifact != null) {
                 loadArtifact(mavenArtifact);
             }
 
             //See if we can load it (either from system or the recently
             //loaded plugin classloader:
             return loadClass(className);
 
         } catch (IOException ioe) {
             throw ioe;
         } catch (ClassNotFoundException cnfe) {
             throw cnfe;
         } catch (Exception e) {
             IOException ioe = new IOException();
             ioe.initCause(e);
             throw ioe;
         }
 
     }
 
     private static Properties loadProperties(ClassLoader cl, String uri) throws IOException {
         InputStream in = cl.getResourceAsStream(uri);
         if (in == null) {
             return null;
         }
 
         // lets load the file
         BufferedInputStream reader = new BufferedInputStream(in);
         try {
             Properties properties = new Properties();
             properties.load(reader);
             return properties;
         } finally {
             try {
                 reader.close();
             } catch (Exception e) {
             }
         }
     }
 
    @SuppressWarnings("deprecation")
     public void loadArtifact(String artifactId) throws IOException, Exception {
         List<File> resolved = null;
         synchronized (RESOLVED_PLUGINS) {
             if (!RESOLVED_PLUGINS.containsKey(artifactId)) {
                 LOG.info("Resolving plugin: " + artifactId);
                 resolved = getPluginResolver().resolvePlugin(artifactId);
                 RESOLVED_PLUGINS.put(artifactId, resolved);
                 LOG.info("Resolved plugin: " + artifactId);
             } else {
                 resolved = RESOLVED_PLUGINS.get(artifactId);
             }
         }
 
         for (File f : resolved) {
             if (resolvedFiles.add(f.getCanonicalPath())) {
                 LOG.debug("Adding plugin dependency: " + f.getCanonicalPath());
                 addUrl(f.toURL());
             }
         }
 
     }
 
     private void loadPlugin(String key) throws IOException, Exception {
         // The plugin version can be configured via a system prop
         String version = System.getProperty(PluginResolver.KEY_PLUGIN_VERSION_PREFIX + key, DEFAULT_PLUGIN_VERSION);
         loadArtifact(PluginResolver.PROJECT_GROUP_ID + ":meshkeeper-" + key + "-plugin:" + version);
     }
 
     private static String getDefaultPluginVersion() {
         String rc = System.getProperty(PluginResolver.KEY_DEFAULT_PLUGINS_VERSION);
         if (rc != null) {
             return rc;
         }
 
         return getModuleVersion();
     }
 
     static public String getModuleVersion() {
         String pomProps = "META-INF/maven/" + PluginResolver.PROJECT_GROUP_ID + "/" + PluginResolver.PROJECT_ARTIFACT_ID + "/pom.properties";
 
         final String DEFAULT_VERSION = "LATEST";
         try {
             Properties p = loadProperties(PluginClassLoader.class.getClassLoader(), pomProps);
             if (p != null) {
                 return p.getProperty("version", DEFAULT_VERSION);
             }
         } catch (Exception e) {
         }
         LOG.warn("Unable to locate '" + pomProps + "' to determine plugin versions using default: " + DEFAULT_VERSION);
         return DEFAULT_VERSION;
     }
 
    @SuppressWarnings("deprecation")
     public synchronized PluginResolver getPluginResolver() {
         if (PLUGIN_RESOLVER == null) {
             PluginClassLoader loader = this;
             try {
                 // Extract the jar to temp file...
                 InputStream is = PluginClassLoader.class.getClassLoader().getResourceAsStream("meshkeeper-mop-resolver.jar");
                 File tempJar = File.createTempFile("meshkeeper-mop-resolver", ".jar");
                 tempJar.deleteOnExit();
                 try {
                     FileSupport.write(is, tempJar);
                 } finally {
                     IOSupport.close(is);
                 }
                 loader.addUrl(tempJar.toURL());
                 PLUGIN_RESOLVER = (PluginResolver) loader.loadClass("org.fusesource.meshkeeper.distribution.MopPluginResolver").newInstance();
             } catch (Throwable thrown) {
                 LOG.error("Error loading plugin resolver:" + thrown.getMessage(), thrown);
                 throw new RuntimeException(thrown);
             }
             PLUGIN_RESOLVER.setDefaultPluginVersion(getDefaultPluginVersion());
         }
         return PLUGIN_RESOLVER;
     }
 
 }
