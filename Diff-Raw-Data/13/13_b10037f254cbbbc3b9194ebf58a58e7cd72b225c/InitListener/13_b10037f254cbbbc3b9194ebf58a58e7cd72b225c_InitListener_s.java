 package com.atlassian.plugin.refimpl;
 
 import javax.servlet.ServletContextListener;
 import javax.servlet.ServletContextEvent;
 import java.util.Properties;
 import java.io.IOException;
 
 import org.apache.log4j.PropertyConfigurator;
 import org.apache.log4j.Logger;
 
 /**
  * Initializes app
  */
 public class InitListener implements ServletContextListener {
 
     public InitListener() {
     }
 
     public void contextInitialized(ServletContextEvent sce) {
         initializeLogger();
 
         Logger.getLogger(InitListener.class).info("Logging initialized.");
         ContainerManager.setInstance(new ContainerManager(sce.getServletContext()));
         ContainerManager mgr = ContainerManager.getInstance();
         mgr.getPluginManager().getPlugins();
     }
 
     public void contextDestroyed(ServletContextEvent sce) {
         ContainerManager.setInstance(null);
     }
 
     private void initializeLogger()
      {
        Properties logProperties = new Properties();
 
        try
        {
          logProperties.load(getClass().getResourceAsStream("/log4j.properties"));
          PropertyConfigurator.configure(logProperties);
 
        }
        catch(IOException e)
        {
          throw new RuntimeException("Unable to load logging property", e);
        }
      }
 
 }
