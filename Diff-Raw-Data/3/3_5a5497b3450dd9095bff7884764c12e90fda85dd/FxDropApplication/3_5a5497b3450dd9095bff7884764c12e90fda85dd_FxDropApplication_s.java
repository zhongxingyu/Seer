 /***************************************************************
  *  This file is part of the [fleXive](R) framework.
  *
  *  Copyright (c) 1999-2010
  *  UCS - unique computing solutions gmbh (http://www.ucs.at)
  *  All rights reserved
  *
  *  The [fleXive](R) project is free software; you can redistribute
  *  it and/or modify it under the terms of the GNU Lesser General Public
  *  License version 2.1 or higher as published by the Free Software Foundation.
  *
  *  The GNU Lesser General Public License can be found at
  *  http://www.gnu.org/licenses/lgpl.html.
  *  A copy is found in the textfile LGPL.txt and important notices to the
  *  license from the author are found in LICENSE.txt distributed with
  *  these libraries.
  *
  *  This library is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  For further information about UCS - unique computing solutions gmbh,
  *  please see the company website: http://www.ucs.at
  *
  *  For further information about [fleXive](R), please see the
  *  project website: http://www.flexive.org
  *
  *
  *  This copyright notice MUST APPEAR in all copies of the file!
  ***************************************************************/
 package com.flexive.shared;
 
 import com.google.common.collect.Maps;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.Serializable;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.List;
 import java.util.Map;
 import java.util.jar.JarEntry;
 import java.util.jar.JarInputStream;
 
 import static com.flexive.shared.FxSharedUtils.checkParameterEmpty;
 
 /**
  * Information about a "drop application" deployed as part of the flexive EAR.
  * <p/>
  * <p>
  * Some properties can be configured in a file called <code>flexive-application.properties</code> in the root
  * folder of the application shared JAR file:
  * </p>
  * <code><pre>
  * # Application name
  * name=hello-flexive
  * displayName=Hello-World Application
  *
  * # Context root (must match the path specified in application.xml, used for the backend start page)
  * contextRoot=war
  * </pre></code>
  *
  * @author Daniel Lichtenberger (daniel.lichtenberger@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  * @version $Rev$
  * @since 3.0.2
  */
 public class FxDropApplication implements Serializable {
     private static final long serialVersionUID = 4947330707321634617L;
     private static final Log LOG = LogFactory.getLog(FxDropApplication.class);
 
     private final String name;
     private final String displayName;
     private final String contextRoot;
     private final String resourceURL;
     private final boolean isJarProtocol;
 
     /**
      * Create a new application descriptor.
      *
      * @param name              the unique name of the application
      * @param contextRoot       the context root of the web application
      * @param displayName       a human-readable name of the application
      * @param resourceJarURL    the URL of the JAR file containing the resources of the drop application
      * @deprecated              pass the actual URL as the last argument for better compatibility
      */
     public FxDropApplication(String name, String contextRoot, String displayName, String resourceJarURL) {
         checkParameterEmpty(name, "name");
         checkParameterEmpty(displayName, "displayName");
         this.name = name;
         this.contextRoot = contextRoot;
         this.displayName = displayName;
         this.resourceURL = resourceJarURL;
         this.isJarProtocol = true;
     }
 
     /**
      * Create a new application descriptor.
      *
      * @param name              the unique name of the application
      * @param contextRoot       the context root of the web application
      * @param displayName       a human-readable name of the application
      * @param resourceURL       the URL that was used for loading flexive-application.properties
      * @since 3.1
      */
     public FxDropApplication(String name, String contextRoot, String displayName, URL resourceURL) {
         checkParameterEmpty(name, "name");
         checkParameterEmpty(displayName, "displayName");
         this.name = name;
         this.contextRoot = contextRoot;
         this.displayName = displayName;
 
         if (resourceURL == null) {
             this.resourceURL = null;
             this.isJarProtocol = false;
         } else {
             // set base url
             String path = resourceURL.getPath().replace("/" + FxSharedUtils.FLEXIVE_DROP_PROPERTIES, "");
             if (path.endsWith("!")) {
                 // strip JAR content separator
                 path = StringUtils.chop(path);
             }
             this.resourceURL = path;
            this.isJarProtocol = resourceURL.getPath().indexOf('!') != -1;
         }
     }
 
     /**
      * Create a new application descriptor. The application name will also be used for the
      * display name and the context root path.
      *
      * @param name      the unique name of the application
      */
     public FxDropApplication(String name) {
         this(name, name, name, (URL) null);
     }
 
     /**
      * Return the unique name of the application. It should not contain spaces or
      * non-alphanumeric characters (excluding "-" and "_").
      *
      * @return the unique name of the application
      */
     public String getName() {
         return name;
     }
 
     /**
      * Return the context root of the web application. This defaults to the <code>name</code>,
      * but can be customized in <code>flexive-application.properties</code>.
      * <p>
      * When an application uses <p>flexive-application.properties</p> but does not specify a
      * contextRoot, it is assumed that the application does not provide a web context.
      * </p>
      *
      * @return the context root of the web application
      */
     public String getContextRoot() {
         return contextRoot;
     }
 
     /**
      * Returns true when the drop application has a web context available (i.e. a context root has been set).
      *
      * @return  true when the drop application has a web context available (i.e. a context root has been set).
      * @since 3.0.3
      */
     public boolean isWebContextAvailable() {
         return contextRoot != null;
     }
 
     /**
      * Returns a human-readable name of the application. This defaults to the <code>name</code>,
      * but can be customized in <code>flexive-application.properties</code>.
      *
      * @return a human-readable name of the application
      */
     public String getDisplayName() {
         return displayName;
     }
 
     /**
      * Returns the URL of the JAR file containing the resources of the drop application, usually
      * the "shared" module (but it could be any JAR file).
      *
      * @return the URL of the JAR file containing the resources of the drop application
      */
     public String getResourceJarURL() {
         return resourceURL;
     }
 
     /**
      * Returns a stream to the JAR file containing the resources of the drop applications.
      *
      * @return a stream to the JAR file containing the resources of the drop applications.
      * @throws IOException if the JAR file stream could not be opened
      * @deprecated  use {@link #loadTextResources(String)} for better compatibility, since drop applications may also
      * be served directly from a directory (e.g. in Maven test suites).
      */
     public JarInputStream getResourceJarStream() throws IOException {
         if (!isJarProtocol) {
             throw new IllegalArgumentException("Cannot create a JarInputStream for a file URL.");
         }
         return getJarStream();
     }
 
     private JarInputStream getJarStream() throws IOException {
         try {
             return resourceURL != null
                     ? new JarInputStream(new URL(resourceURL).openStream())
                     : null;
         } catch (MalformedURLException e) {
             //try again using JBoss v5 vfszip ...
             try {
                 return new JarInputStream(new URL("vfszip:" + resourceURL).openStream());
             } catch (MalformedURLException e2) {
                 throw new IllegalArgumentException("Cannot create JAR stream for URL " + resourceURL);
             }
         }
     }
 
     /**
      * Load text resources packaged with the drop.
      *
      * @param pathPrefix    a path prefix (e.g. "scripts/")
      * @return              a map of filename (relative to the drop application package) -> file contents
      * @throws java.io.IOException  on I/O errors
      * @since 3.1
      */
     public Map<String, String> loadTextResources(String pathPrefix) throws IOException {
         if (pathPrefix == null) {
             pathPrefix = "";
         } else if (pathPrefix.startsWith("/")) {
             pathPrefix = pathPrefix.substring(1);
         }
         if (isJarProtocol) {
             // read directly from jar file
             
             final Map<String, String> result = Maps.newHashMap();
             final JarInputStream stream = getJarStream();
             try {
                 JarEntry entry;
                 while ((entry = stream.getNextJarEntry()) != null) {
 
                     if (!entry.isDirectory() && entry.getName().startsWith(pathPrefix)) {
                         try {
                             result.put(
                                     entry.getName(),
                                     FxSharedUtils.readFromJarEntry(stream, entry)
                             );
                         } catch (Exception e) {
                             if (LOG.isTraceEnabled()) {
                                 LOG.trace("Failed to read text JAR entry " + entry.getName() + ": " + e.getMessage(), e);
                             }
                             // ignore, continue reading
                         }
 
                     }
                 }
                 return result;
             } finally {
                 FxSharedUtils.close(stream);
             }
         } else {
             // read from filesystem
 
             final List<File> files = FxFileUtils.listRecursive(new File(resourceURL + File.separator + pathPrefix));
             final Map<String, String> result = Maps.newHashMapWithExpectedSize(files.size());
             for (File file : files) {
                 try {
                     result.put(
                             StringUtils.replace(file.getPath(), resourceURL + File.separator, ""),
                             FxSharedUtils.loadFile(file)
                     );
                 } catch (Exception e) {
                     if (LOG.isTraceEnabled()) {
                         LOG.trace("Failed to read text file " + file.getPath() + ": " + e.getMessage(), e);
                     }
                 }
             }
             return result;
         }
     }
 }
