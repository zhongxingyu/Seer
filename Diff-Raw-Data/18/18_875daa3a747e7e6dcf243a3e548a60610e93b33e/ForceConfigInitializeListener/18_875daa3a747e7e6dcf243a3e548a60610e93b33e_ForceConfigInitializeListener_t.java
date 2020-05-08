 package gov.usgs.cida.n52.wps.config;
 
 import gov.usgs.cida.watersmart.common.ContextConstants;
 import gov.usgs.cida.watersmart.common.JNDISingleton;
 import java.io.IOException;
 import javax.servlet.ServletContextEvent;
 import javax.servlet.ServletContextListener;
 import org.apache.xmlbeans.XmlException;
 import org.n52.wps.commons.WPSConfig;
 
 /**
  * Web application lifecycle listener.
  *
  * @author Jordan Walker <jiwalker@usgs.gov>
  */
 public class ForceConfigInitializeListener implements ServletContextListener {
 
     @Override
     public void contextInitialized(ServletContextEvent sce) {
         String defaultLocation = WPSConfig.getConfigPath();
         String configLocation = JNDISingleton.getInstance().getProperty(ContextConstants.WPS_CONFIG_LOCATION, defaultLocation);
         System.out.println(configLocation);
         try {
             WPSConfig.forceInitialization(configLocation);
         }
         catch (XmlException ex) {
             System.out.println(ex);
             // I think this will get caught later
         }
         catch (IOException ex) {
             System.out.println(ex);
             // yell at me if I'm wrong
         }
     }
 
     @Override
     public void contextDestroyed(ServletContextEvent sce) {
         // do nothing
     }
 }
