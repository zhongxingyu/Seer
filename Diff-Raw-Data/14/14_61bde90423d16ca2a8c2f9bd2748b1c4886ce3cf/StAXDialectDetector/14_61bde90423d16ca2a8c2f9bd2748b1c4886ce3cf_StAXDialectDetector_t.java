 /*
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements. See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership. The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License. You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied. See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 
 package org.apache.axiom.util.stax.dialect;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.jar.Attributes;
 import java.util.jar.Manifest;
 
 import javax.xml.stream.XMLInputFactory;
 import javax.xml.stream.XMLOutputFactory;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 /**
  * Detects StAX dialects and normalizes factories for a given StAX implementation.
  * <p>
  * Note that this class internally maintains a cache of detected dialects. The overhead caused by
  * invocations of methods in this class is thus small.
  */
 public class StAXDialectDetector {
     private static final Log log = LogFactory.getLog(StAXDialectDetector.class);
     
     private static final Attributes.Name IMPLEMENTATION_TITLE =
             new Attributes.Name("Implementation-Title");
     
     private static final Attributes.Name IMPLEMENTATION_VENDOR =
         new Attributes.Name("Implementation-Vendor");
 
     private static final Attributes.Name IMPLEMENTATION_VERSION =
             new Attributes.Name("Implementation-Version");
     
     private static final Map/*<URL,StAXDialect>*/ dialectByUrl =
             Collections.synchronizedMap(new HashMap());
 
     private StAXDialectDetector() {}
     
     /**
      * Get the URL corresponding to the root folder of the classpath entry from which a given
      * resource is loaded. This URL can be used to load other resources from the same classpath
      * entry (JAR file or directory).
      * 
      * @return the root URL or <code>null</code> if the resource can't be found or if it is not
      *         possible to determine the root URL
      */
     private static URL getRootUrlForResource(ClassLoader classLoader, String resource) {
         URL url = classLoader.getResource(resource);
         if (url == null) {
             return null;
         }
         String file = url.getFile();
         if (file.endsWith(resource)) {
             try {
                 return new URL(url.getProtocol(), url.getHost(), url.getPort(),
                         file.substring(0, file.length()-resource.length()));
             } catch (MalformedURLException ex) {
                 return null;
             }
         } else {
             return null;
         }
     }
     
     /**
      * Detect the dialect of a given {@link XMLInputFactory} and normalize it.
      * 
      * @param factory the factory to normalize
      * @return the normalized factory
      * 
      * @see StAXDialect#normalize(XMLInputFactory)
      */
     public static XMLInputFactory normalize(XMLInputFactory factory) {
         return getDialect(factory.getClass()).normalize(factory);
     }
     
     /**
      * Detect the dialect of a given {@link XMLOutputFactory} and normalize it.
      * 
      * @param factory the factory to normalize
      * @return the normalized factory
      * 
      * @see StAXDialect#normalize(XMLOutputFactory)
      */
     public static XMLOutputFactory normalize(XMLOutputFactory factory) {
         return getDialect(factory.getClass()).normalize(factory);
     }
     
     /**
      * Detect the dialect of a given StAX implementation.
      * 
      * @param implementationClass
      *            any class that is part of the StAX implementation; typically this should be a
      *            {@link XMLInputFactory}, {@link XMLOutputFactory},
      *            {@link javax.xml.stream.XMLStreamReader} or
      *            {@link javax.xml.stream.XMLStreamWriter} implementation
      * @return the detected dialect
      */
     public static StAXDialect getDialect(Class implementationClass) {
         URL rootUrl = getRootUrlForResource(implementationClass.getClassLoader(),
                 implementationClass.getName().replace('.', '/') + ".class");
         if (rootUrl == null) {
             log.warn("Unable to determine location of StAX implementation containing class "
                     + implementationClass.getName() + "; using default dialect");
             return UnknownStAXDialect.INSTANCE;
         } else {
             return getDialect(rootUrl);
         }
     }
 
     private static StAXDialect getDialect(URL rootUrl) {
         StAXDialect dialect = (StAXDialect)dialectByUrl.get(rootUrl);
         if (dialect != null) {
             return dialect;
         } else {
             dialect = detectDialect(rootUrl);
             dialectByUrl.put(rootUrl, dialect);
             return dialect;
         }
     }
     
     private static StAXDialect detectDialect(URL rootUrl) {
         Manifest manifest;
         try {
             URL metaInfUrl = new URL(rootUrl, "META-INF/MANIFEST.MF");
             InputStream is = metaInfUrl.openStream();
             try {
                 manifest = new Manifest(is);
             } finally {
                 is.close();
             }
         } catch (IOException ex) {
             log.warn("Unable to load manifest for StAX implementation at " + rootUrl);
             return UnknownStAXDialect.INSTANCE;
         }
         Attributes attrs = manifest.getMainAttributes();
         String title = attrs.getValue(IMPLEMENTATION_TITLE);
         String vendor = attrs.getValue(IMPLEMENTATION_VENDOR);
         String version = attrs.getValue(IMPLEMENTATION_VERSION);
         if (log.isDebugEnabled()) {
             log.debug("StAX implementation at " + rootUrl + " is:\n" +
                     "  Title:   " + title + "\n" +
                     "  Vendor:  " + vendor + "\n" +
                     "  Version: " + version);
         }
         // For the moment, the dialect detection is quite simple, but in the future we will probably
         // have to differentiate by version number
        if(vendor != null) {
            if (vendor.toLowerCase().indexOf("woodstox") != -1) {
                return WoodstoxDialect.INSTANCE;
            } else if (title.indexOf("SJSXP") != -1) {
                return SJSXPDialect.INSTANCE;
            } 
         }
        return UnknownStAXDialect.INSTANCE;
     }
 }
