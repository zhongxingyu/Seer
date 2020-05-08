 /*
  * CDDL HEADER START
  *
  * The contents of this file are subject to the terms of the
  * Common Development and Distribution License, Version 1.0 only
  * (the "License").  You may not use this file except in compliance
  * with the License.
  *
  * You can obtain a copy of the license at license/ESCIDOC.LICENSE
  * or http://www.escidoc.de/license.
  * See the License for the specific language governing permissions
  * and limitations under the License.
  *
  * When distributing Covered Code, include this CDDL HEADER in each
  * file and include the License file at license/ESCIDOC.LICENSE.
  * If applicable, add the following below this CDDL HEADER, with the
  * fields enclosed by brackets "[]" replaced with your own identifying
  * information: Portions Copyright [yyyy] [name of copyright owner]
  *
  * CDDL HEADER END
  */
 
 /*
  * Copyright 2006-2008 Fachinformationszentrum Karlsruhe Gesellschaft
  * fuer wissenschaftlich-technische Information mbH and Max-Planck-
  * Gesellschaft zur Foerderung der Wissenschaft e.V.  
  * All rights reserved.  Use is subject to license terms.
  */
 package de.escidoc.core.test.common.resources;
 
 import de.escidoc.core.test.common.logger.AppLogger;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 import org.springframework.core.io.Resource;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Properties;
 
 /**
  * @author MSC
  * 
  */
 public class PropertiesProvider {
 
     private static final AppLogger LOG = new AppLogger(
         PropertiesProvider.class.getName());
 
     public static final String ESCIDOC_SERVER_NAME = "server.name";
 
     public static final String ESCIDOC_SERVER_PORT = "server.port";
 
     public static final String FEDORA_URL = "fedora.url";
 
     public static final String FEDORA_USER = "fedora.user";
 
     public static final String FEDORA_PASSWORD = "fedora.passwd";
 
    public static final String TESTDATA_URL = "testdata.url";
 
     public static final String DIGILIB_SCALER_URL = "digilib.scaler";
 
     public static final String PERFORMANCE_DB_DRIVER_CLASSNAME =
         "escidoc.performance.db.driverClassName";
 
     public static final String PERFORMANCE_DB_URL =
         "escidoc.performance.db.url";
 
     public static final String PERFORMANCE_DB_USERNAME =
         "escidoc.performance.db.username";
 
     public static final String PERFORMANCE_DB_PASSWORD =
         "escidoc.performance.db.password";
 
     private Properties properties = null;
 
     private final List<String> files;
 
     /**
      * @throws Exception
      *             Thrown if init of properties failed.
      */
     public PropertiesProvider() throws Exception {
 
         this.files = new LinkedList<String>();
         addFile("escidoc.properties");
         String currentUser = System.getProperties().getProperty("user.name");
         if (currentUser != null) {
             addFile(currentUser + ".properties");
         }
         addFile("test.properties");
         addFile("load-test.properties");
         init();
     }
 
     /**
      * Returns the property with the given name or null if property was not
      * found.
      * 
      * @param name
      *            The name of the Property.
      * @return Value of the given Property as String.
      */
     public String getProperty(final String name) {
 
         return properties.getProperty(name);
     }
 
     /**
      * Returns the property with the given name or the second parameter as
      * default value if property was not found.
      * 
      * @param name
      *            The name of the Property.
      * @param defaultValue
      *            The default vaule if property isn't given.
      * @return Value of the given Property as String.
      */
     public String getProperty(final String name, final String defaultValue) {
 
         return properties.getProperty(name, defaultValue);
     }
 
     /**
      * 
      * @throws Exception
      *             Thrown if init of properties failed.
      */
     public synchronized void init() throws Exception {
 
         Properties result = new Properties();
         Iterator<String> fileIter = files.iterator();
         while (fileIter.hasNext()) {
             String next = fileIter.next();
             try {
                 Properties prop = loadProperties(next);
                 result.putAll(prop);
             }
             catch (Exception e) {
                 LOG.debug(e);
             }
         }
         this.properties = result;
     }
 
     /**
      * Get an InputStream for the given file.
      * 
      * @param filename
      *            The name of the file.
      * @return The InputStream or null if the file could not be located.
      * @throws IOException
      *             If access to the specified file fails.
      */
     private synchronized InputStream getInputStream(final String filename)
         throws IOException {
         final ApplicationContext applicationContext =
             new ClassPathXmlApplicationContext(new String[] {});
         final Resource[] resource =
             applicationContext.getResources("classpath*:**/" + filename);
         if (resource.length == 0) {
             throw new FileNotFoundException("Unable to find file '" + filename
                 + "' in classpath.");
         }
         return resource[0].getInputStream();
     }
 
     /**
      * Loads the Properties from the possible files. First loads properties from
      * the file escidoc-core.properties.default. Afterwards tries to load
      * specific properties from the file escidoc.properties and merges them with
      * the default properties. If any key is included in default and specific
      * properties, the value of the specific property will overwrite the default
      * property.
      * 
      * @param file
      *            The name of the properties file.
      * @return The properties
      * @throws Exception
      *             If the loading of the default properties (file
      *             escidoc-core.properties.default) fails.
      */
     private synchronized Properties loadProperties(final String file)
         throws Exception {
         Properties result = new Properties();
         InputStream propertiesStream = getInputStream(file);
         result.load(propertiesStream);
         return result;
     }
 
     /**
      * Add a properties file to the list of properties.
      * 
      * @param name
      *            Name of properties file.
      */
     public synchronized void addFile(final String name) {
 
         this.files.add(name);
     }
 }
