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
     private final transient static Log log = LogFactory.getLog(CSSLookAndFeel.class);
     private static final String PROPERTIES_LOCATION_START = "WEB-INF/" + CSSLookAndFeel.class.getPackage().getName();
     private static final String PROPERTIES_LOCATION_END = ".properties";
 
     public CSSLookAndFeel() throws IOException {
         super(loadProperties());
     }
 
     private static Properties loadProperties() throws IOException {
         // default properties
         StringBuffer propertiesLocation = new StringBuffer(PROPERTIES_LOCATION_START);
         propertiesLocation.append(PROPERTIES_LOCATION_END);
         // browser dependent properties
        String browserType = SessionManager.getSession().getUserAgent().getBrowserType().getShortName();
         StringBuffer browserPropertiesLocation = new StringBuffer(PROPERTIES_LOCATION_START);
         browserPropertiesLocation.append(".");
        browserPropertiesLocation.append(browserType);
         browserPropertiesLocation.append(PROPERTIES_LOCATION_END);
 
         Properties properties = new Properties();
         InputStream in;
         try {
             in = SessionManager.getSession().getServletContext().getResourceAsStream(propertiesLocation.toString());
             properties.load(in);
            in.close();
         } catch (Exception e) {
             final String error = "Unable to open " + propertiesLocation.toString() + " due to "+e+".\nPlease check deployment!";
             log.fatal(error);
             throw new IOException(error);
         }
         try {
             in = SessionManager.getSession().getServletContext().getResourceAsStream(browserPropertiesLocation.toString());
            if (in != null) { 
                // file is there, so we should be able to load it.
                properties.load(in);
                in.close();
            } 
            /* else file is not there, we are not overriding any property.
             * So we don't need to do anything.
             */
         } catch (Exception e) {
            final String error = "Unable to open " + browserPropertiesLocation.toString() + " due to "+e+".\nPlease check deployment!";
            log.error(error);
            throw new IOException(error);
         }
         return properties;
     }
 }
 
 
