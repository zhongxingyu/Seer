 /***************************************************************
  *  This file is part of the [fleXive](R) framework.
  *
  *  Copyright (c) 1999-2008
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
 
 import static com.flexive.shared.FxSharedUtils.checkParameterEmpty;
 
 import java.io.Serializable;
 import java.io.IOException;
 import java.util.jar.JarInputStream;
 import java.net.URL;
import java.net.MalformedURLException;
 
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
 
     private final String name;
     private final String displayName;
     private final String contextRoot;
     private final String resourceJarURL;
 
     /**
      * Create a new application descriptor.
      *
      * @param name              the unique name of the application
      * @param contextRoot       the context root of the web application
      * @param displayName       a human-readable name of the application
      * @param resourceJarURL    the URL of the JAR file containing the resources of the drop application
      */
     public FxDropApplication(String name, String contextRoot, String displayName, String resourceJarURL) {
         checkParameterEmpty(name, "name");
         checkParameterEmpty(displayName, "displayName");
         this.name = name;
         this.contextRoot = contextRoot;
         this.displayName = displayName;
         this.resourceJarURL = resourceJarURL;
     }
 
     /**
      * Create a new application descriptor. The application name will also be used for the
      * display name and the context root path.
      *
      * @param name      the unique name of the application
      */
     public FxDropApplication(String name) {
         this(name, name, name, null);
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
         return resourceJarURL;
     }
 
     /**
      * Returns a stream to the JAR file containing the resources of the drop applications.
      *
      * @return a stream to the JAR file containing the resources of the drop applications.
      * @throws IOException if the JAR file stream could not be opened
      */
     public JarInputStream getResourceJarStream() throws IOException {
        try {
            return resourceJarURL != null
                    ? new JarInputStream(new URL(resourceJarURL).openStream())
                    : null;
        } catch (MalformedURLException e) {
            //try again using JBoss v5 vfszip ...
            return new JarInputStream(new URL("vfszip:" + resourceJarURL).openStream());
        }
     }
 }
