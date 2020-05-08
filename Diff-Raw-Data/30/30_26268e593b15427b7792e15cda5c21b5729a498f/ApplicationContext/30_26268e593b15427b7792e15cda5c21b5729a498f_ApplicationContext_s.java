 package org.debox.photo.server;
 
 import java.sql.SQLException;
 import org.debox.photo.dao.ConfigurationDao;
 import org.debox.photo.model.Configuration;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * @author Corentin Guy <corentin.guy@debox.fr>
  */
 public class ApplicationContext {
     
     private static final Logger logger = LoggerFactory.getLogger(ApplicationContext.class);
 
     protected static ConfigurationDao configurationDao = new ConfigurationDao();
     protected static ApplicationContext instance = new ApplicationContext();
     protected static Configuration configuration;
 
     protected ApplicationContext() {
         // Nothing to do
     }
 
     public static ApplicationContext getInstance() {
         return instance;
     }
 
     public Configuration saveConfiguration(Configuration configuration) throws SQLException {
         configurationDao.save(configuration);
         return configuration;
     }
 
     public Configuration getConfiguration() {
         if (configuration == null) {
             try {
                 configuration = configurationDao.get();
             } catch (SQLException ex) {
                 logger.error("Unable to load configuration from database", ex);
             }
         }
         return configuration;
     }
 }
