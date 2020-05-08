 /*******************************************************************************
  * Copyright (c) 2008, 2010 VMware Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *   VMware Inc. - initial contribution
  *******************************************************************************/
 
 package org.eclipse.virgo.kernel.equinox.extensions.hooks;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.JarURLConnection;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.Enumeration;
 import java.util.jar.JarFile;
 
 import org.eclipse.osgi.baseadaptor.BaseData;
 import org.eclipse.osgi.baseadaptor.bundlefile.BundleEntry;
 import org.eclipse.osgi.baseadaptor.bundlefile.BundleFile;
 import org.eclipse.osgi.baseadaptor.hooks.BundleFileWrapperFactoryHook;
 
 /**
  * A {@link BundleFileWrapperFactoryHook} implementation that wraps {@link BundleFile BundleFiles} to ensure that all
  * returned resource {@link URL URLs} have a <code>file:</code> protocol, not a <code>bundleresource:</code>
  * protocol as is the Equinox default.
  * 
  * <strong>Concurrent Semantics</strong><br />
  * This class is <strong>thread-safe</strong>.
  * 
  */
 final class ExtendedBundleFileWrapperFactoryHook implements BundleFileWrapperFactoryHook {
 
     /**
      * {@inheritDoc}
      */
     public BundleFile wrapBundleFile(BundleFile bundleFile, Object content, BaseData data, boolean base) throws IOException {
         return new FileResourceEnforcingBundleFile(bundleFile);
     }
 
     /**
      * A concrete extension of {@link BundleFile} that ensures that all resource {@link URL URLs} returned have a
      * <code>file:</code> protocol, not a <code>bundleresource:</code> protocol as is the Equinox default.
      * <p>
      * <strong>Concurrent Semantics</strong><br />
      * As thread-safe as the encapsulated <code>BundleFile</code> instance.
      * 
      */
     private static class FileResourceEnforcingBundleFile extends BundleFile {
 
         private final BundleFile bundleFile;
 
         private FileResourceEnforcingBundleFile(BundleFile bundleFile) {
             this.bundleFile = bundleFile;
         }
 
         /**
          * {@inheritDoc}
          */
         @Override
         public void close() throws IOException {
             this.bundleFile.close();
 
         }
 
         /**
          * {@inheritDoc}
          */
         @Override
         public boolean containsDir(String dir) {
             return this.bundleFile.containsDir(dir);
         }
 
         /**
          * {@inheritDoc}
          */
         @Override
         public BundleEntry getEntry(String path) {
             BundleEntry entry = this.bundleFile.getEntry(path);
             return entry;
         }
 
         /**
          * {@inheritDoc}
          */
         @Override
         public Enumeration<String> getEntryPaths(String path) {
             Enumeration<String> paths = this.bundleFile.getEntryPaths(path);
             return paths;
         }
 
         /**
          * {@inheritDoc}
          */
         @Override
         public File getFile(String path, boolean nativeCode) {
             return this.bundleFile.getFile(path, nativeCode);
         }
 
         /**
          * {@inheritDoc}
          */
         @Override
         public void open() throws IOException {
             this.bundleFile.open();
         }
 
         /**
          * {@inheritDoc}
          */
         @Override
         public File getBaseFile() {
             return this.bundleFile.getBaseFile();
         }
 
         /**
          * Locates the resource in the BundleFile identified by the supplied path and, if found, returns a
          * <code>file</code> protocol URL for the resource.
          * 
          * @return a <code>file</code> protocol URL for the resource
          */
         @Override
         public URL getResourceURL(String path, long hostBundleID, int index) {
             return doGetResourceURL(path);
         }
 
         /**
          * Locates the resource in the BundleFile identified by the supplied path and, if found, returns a
          * <code>file</code> protocol URL for the resource.
          * 
          * @return a <code>file</code> protocol URL for the resource
          */
         @Override
         public URL getResourceURL(String path, long hostBundleID) {
             return doGetResourceURL(path);
         }
 
         /**
          * {@inheritDoc}
          */
         @Override
         public URL getResourceURL(String path, BaseData hostData, int index) {
             return doGetResourceURL(path);
         }
 
         private URL doGetResourceURL(String path) {
             BundleEntry entry = getEntry(path);
             if (entry != null) {
                 return getLocalURLForEntry(entry);
             } else {
                 return null;
             }
         }
 
         private URL getLocalURLForEntry(BundleEntry entry) {
             URL url = entry.getLocalURL();
             try {
                url.openConnection().setDefaultUseCaches(false);
                url.openConnection().setUseCaches(false);
             } catch (Exception e) {
                 
             }
            if (!"jar".equals(url.getProtocol()) || doesJarEntryReallyExist(url)) {
                 return url;
             } else {
                 return null;
             }
         }
 
        private boolean doesJarEntryReallyExist(URL url) {
             boolean entryExists = false;
             JarFile jarFile = null;
             try {
                URLConnection connection = url.openConnection();
                if (connection instanceof JarURLConnection) {
                     JarURLConnection jarURLConnection = (JarURLConnection) connection;
                     jarFile = jarURLConnection.getJarFile();
                     String entryName = jarURLConnection.getEntryName();
                     if (entryName != null && jarFile != null && jarFile.getEntry(entryName) != null) {
                     	entryExists = true;
                     }
                 }
             } catch (IOException ioe) {
                 entryExists = false;
             } finally {
             	if (jarFile != null) {
             		try {
             			jarFile.close();
             		} catch (IOException _) {
             		}
             	}
             }
             return entryExists;
         }
     }
 }
