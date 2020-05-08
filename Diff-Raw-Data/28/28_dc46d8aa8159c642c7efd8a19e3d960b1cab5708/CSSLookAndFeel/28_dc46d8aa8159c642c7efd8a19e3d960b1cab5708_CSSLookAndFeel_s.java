 /*
  * $Id$
  * Copyright 2000,2005 wingS development team.
  *
  * This file is part of wingS (http://www.j-wings.org).
  *
  * wingS is free software; you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License
  * as published by the Free Software Foundation; either version 2.1
  * of the License, or (at your option) any later version.
  *
  * Please see COPYING for the complete licence.
  */
 package org.wings.plaf.css;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.wings.session.SessionManager;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Properties;
 
 public class CSSLookAndFeel
         extends org.wings.plaf.LookAndFeel {
     private static final String WEB_INF = "WEB-INF/";
     private final transient static Log log = LogFactory.getLog(CSSLookAndFeel.class);
     private static final String PROPERTIES_FILENAME_START = CSSLookAndFeel.class.getPackage().getName();
     private static final String PROPERTIES_FILENAME_END = ".properties";
     private static final String PROPERTIES_CLASSPATH = PROPERTIES_FILENAME_START.replace('.','/').concat("/");
 
     public CSSLookAndFeel() throws IOException {
         super(loadProperties());
     }
 
    private static Properties loadProperties() throws IOException{
         // default properties
         StringBuffer propertiesFilename = new StringBuffer(PROPERTIES_FILENAME_START);
         propertiesFilename.append(PROPERTIES_FILENAME_END);
         // browser dependent properties
         String browserType = SessionManager.getSession().getUserAgent().getBrowserType().getShortName();
         StringBuffer browserPropertiesFilename = new StringBuffer(PROPERTIES_FILENAME_START);
         browserPropertiesFilename.append(".");
         browserPropertiesFilename.append(browserType);
         browserPropertiesFilename.append(PROPERTIES_FILENAME_END);
 
        Properties properties;
         properties = loadProperties(propertiesFilename);
        // catch IOExceptions b/c these files are totally optional
         try {
             properties.putAll(loadProperties(browserPropertiesFilename));
         } catch (IOException e) {
            log.info("the (optional) browser properties files did not get loaded.\n If this is ok (default), ignore the log messages above.");
         }
         return properties;
     }
 
     /**
      * Loads properties from a file. First looks in the classPath for the
      * default file. Throws an exception if that is not found. Then looks 
      * for an overriding file in the webapp container. If this is not found,
      * log this info.
      * @param propertiesFilename the name of the properties file.
      * @throws IOException if default (in classPath) is not found.
      * @return The Properties loaded
      */
    private static Properties loadProperties(StringBuffer propertiesFilename) throws IOException{
         Properties properties;
         InputStream in;
         IOException finalException = null;
         // first load defaults from classpath, and if it fails, throw Exception
         final String classPath = PROPERTIES_CLASSPATH + propertiesFilename.toString();
        try {
            properties = loadPropertiesFromClasspath(classPath);
        } catch (IOException e) {
            properties = new Properties();
            finalException = e;
        }
         // now load from webapp folder, log if fails.
         String webappUrl = WEB_INF + propertiesFilename.toString();
         properties.putAll(loadPropertiesFromContainer(webappUrl));
        // throw delayed exception
        if (finalException != null) {
            throw finalException;
        }
         return properties;
     }
 
     /**
      * Loads a file from the webapp's dir into a properties file.
      * @param webappUrl the file's url
      * @return The Properties loaded
      */
     private static Properties loadPropertiesFromContainer(String webappUrl) {
         Properties properties = new Properties();
         InputStream in;
         try {
             in = SessionManager.getSession().getServletContext().getResourceAsStream(webappUrl);
             properties.load(in);
             in.close();
         } catch (Exception e) {
             final String error = "Unable to open " + webappUrl + " due to "+e+".\nIt seems you didn't provide a custom config file.";
             log.warn(error);
         } 
         return properties;
     }
 
     /**
      * Loads a file from the webapp's classpath into a properties file.
      * @param classPath the file's classpath
      * @return The Properties loaded
      * @throws IOException
      */
     private static Properties loadPropertiesFromClasspath(final String classPath) throws IOException {
         Properties properties = new Properties();
         InputStream in;
         try {
             in = CSSLookAndFeel.class.getClassLoader().getResourceAsStream(classPath);
             properties.load(in);
             in.close();
         } catch (Exception e) {
             final String error = "Unable to open " + classPath + " from classPath due to "+e+".\nPlease check deployment!";
             log.warn(error);
             throw new IOException(error);
         }
         return properties;
     }
 }
 
 
