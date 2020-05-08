 /**
  *    Copyright 2013 Christian Hilmersson
  *    
  *    This file is part of config-bootstrapper (https://github.com/chilmers/config-bootstrapper)
  *    
  *    config-bootstrapper is free software; you can redistribute it and/or modify
  *    it under the terms of version 2.1 of the GNU Lesser General Public
  *    License as published by the Free Software Foundation.
  *    
  *    config-bootstrapper is distributed in the hope that it will be useful,
  *    but WITHOUT ANY WARRANTY; without even the implied warranty of
  *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *    GNU Lesser General Public License for more details.
  *    
  *    You should have received a copy of the GNU Lesser General Public
  *    License along with config-bootstrapper; if not, write to the
  *    Free Software Foundation, Inc., 59 Temple Place, Suite 330,
  *    Boston, MA 02111-1307  USA
  */
 package com.chilmers.configbootstrapper;
 
 import java.io.FileInputStream;
 import java.io.InputStream;
 import java.util.PropertyResourceBundle;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 
 public class ConfigHelper {
 
     private static final Logger log = Logger.getLogger(ConfigHelper.class);
     
     /**
      * Used for reading the application from it's location after initialization is done
     * using the default application config location key.
      * See {@link ConfigServletContextListener#DEFAULT_CONFIG_LOCATION_PROPERTY_KEY}
      * @return A property resource bundle holding the current configuration.
      */
     public static PropertyResourceBundle readApplicationConfiguration() {
         return readApplicationConfiguration(null);
     }
     
     /**
      * Used for reading the application from it's location after initialization is done.
      * @param propertyKey the name of the property for the application config location or
      * null for the default key.
      * Can be null in most cases. If it is blank or null the default key will be used. 
      * See {@link ConfigServletContextListener#DEFAULT_CONFIG_LOCATION_PROPERTY_KEY}
      * @return A property resource bundle holding the current configuration.
      */
     public static PropertyResourceBundle readApplicationConfiguration(String propertyKey) {
         if (StringUtils.isBlank(propertyKey)) {
             propertyKey = ConfigServletContextListener.DEFAULT_CONFIG_LOCATION_PROPERTY_KEY;
         }
         String applicationConfigLocation = System.getProperty(propertyKey);
         InputStream is = null;
         try {
             if (applicationConfigLocation.startsWith("classpath:")) {
                 applicationConfigLocation = applicationConfigLocation.replaceFirst("classpath:", "");
                 is = Thread.currentThread().getContextClassLoader().getResourceAsStream(applicationConfigLocation); 
             } else {
                 is = new FileInputStream(applicationConfigLocation);    
             }
             return new PropertyResourceBundle(is);
             
         } catch (Exception e) {
             log.error("There was a problem reading the application configuration at location: " 
                     + applicationConfigLocation +"\n"
                     + "Exception:" + e.getClass().toString() + "\n"
                     + "Message:" + e.getMessage());
         } finally {
             try {
                 is.close();
             } catch (Exception e) {
                 log.error("WARNING! Exception while trying to close configuration file.\n"
                         + "Exception:" + e.getClass().toString() + "\n"
                         + "Message:" + e.getMessage());
             }
         }
         return null;        
     }
     
 }
