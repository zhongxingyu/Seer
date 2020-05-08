 /* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
  * This code is licensed under the GPL 2.0 license, availible at the root
  * application directory.
  */
 package org.vfny.geoserver.global;
 
 import org.springframework.beans.BeansException;
 import org.springframework.beans.factory.BeanInitializationException;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.ApplicationContextAware;
 import org.springframework.web.context.WebApplicationContext;
 import org.vfny.geoserver.global.xml.XMLConfigReader;
 import java.io.File;
 import javax.servlet.ServletContext;
 
 
 /**
  * The application configuratoin facade.
  *
  * @author Justin Deoliveira, The Open Planning Project, jdeolive@openplans.org
  *
  */
 public class Config implements ApplicationContextAware {
     WebApplicationContext context;
     XMLConfigReader reader;
 
     public XMLConfigReader getXMLReader() throws ConfigurationException {
         return reader;
     }
 
     public File dataDirectory() {
         ServletContext sc = this.context.getServletContext();
 
         return GeoserverDataDirectory.getGeoserverDataDirectory();
     }
 
     public void setApplicationContext(ApplicationContext context)
         throws BeansException {
         this.context = (WebApplicationContext) context;
 
         // if the server admin did not set it up otherwise, force X/Y axis ordering
         // This one is a good place because we need to initialize this property
         // before any other opeation can trigger the initialization of the CRS subsystem
         if (System.getProperty("org.geotools.referencing.forceXY") == null) {
             System.setProperty("org.geotools.referencing.forceXY", "true");
         }
 
         //HACK: java.util.prefs are awful.  See
         //http://www.allaboutbalance.com/disableprefs.  When the site comes
         //back up we should implement their better way of fixing the problem.
         System.setProperty("java.util.prefs.syncInterval", "5000000");
 
         ServletContext sc = this.context.getServletContext();
 
         try {
             GeoserverDataDirectory.init(this.context);
             reader = new XMLConfigReader(dataDirectory(), sc);
         } catch (ConfigurationException e) {
             String msg = "Error creating xml config reader";
             throw new BeanInitializationException(msg, e);
         }
     }
 
     public WebApplicationContext getApplictionContext() {
         return context;
     }
 }
