 package com.bitcup.configurator;
 
 import org.apache.commons.configuration.CompositeConfiguration;
 import org.apache.commons.configuration.ConfigurationException;
 import org.apache.commons.configuration.ConfigurationUtils;
 import org.apache.commons.configuration.PropertiesConfiguration;
 import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.File;
 import java.net.URL;
 import java.util.List;
 import java.util.concurrent.TimeUnit;
 
 /**
  * Loads configuration from a properties file on the classpath or in a context's config path.
  * <p/>
  * TODO: RuntimeException instead of ConfigException?
  * TODO: for spring, create separate class (SpringFileConfig)
  * User: omar
  */
 public class FileConfig {
 
     private static final Logger logger = LoggerFactory.getLogger(FileConfig.class);
 
     private static final int REFRESH_DELAY_IN_SECONDS = 15;
     private static final String SEPARATOR = ".";
 
     private CompositeConfiguration configuration = new CompositeConfiguration();
 
     /**
      * Loads config properties file
      *
      * @param filename name of the properties file to load
      */
     public FileConfig(String filename) {
         if (Context.getInstance().hasConfigPath()) {
             final String fn = Context.getInstance().getConfigPath() + File.separator + filename;
             URL fileUrl = ConfigurationUtils.locate(fn);
             if (fileUrl != null) {
                 try {
                     PropertiesConfiguration pc = new PropertiesConfiguration(fileUrl);
                     pc.setReloadingStrategy(getReloadingStrategy());
                     configuration.addConfiguration(pc);
                    logger.info("Using configPath-based config file " + fn);
                 } catch (ConfigurationException e) {
                    logger.error("Could not use configPath-based config file " + fn, e);
                 }
             }
         }
         if (Context.getInstance().hasHostName()) {
             final String fn = Context.getInstance().getHostName() + SEPARATOR + filename;
             try {
                 PropertiesConfiguration pc = new PropertiesConfiguration(fn);
                 pc.setReloadingStrategy(getReloadingStrategy());
                 configuration.addConfiguration(pc);
                 logger.info("Using hostname-based config file " + fn);
             } catch (ConfigurationException e) {
                logger.error("Could not use hostname-based config file " + fn, e);
             }
         }
         if (Context.getInstance().hasEnv()) {
             final String fn = Context.getInstance().getEnv() + SEPARATOR + filename;
             try {
                 PropertiesConfiguration pc = new PropertiesConfiguration(fn);
                 pc.setReloadingStrategy(getReloadingStrategy());
                 configuration.addConfiguration(pc);
                 logger.info("Using env-based config file " + fn);
             } catch (ConfigurationException e) {
                logger.error("Could not use env-based config file " + fn, e);
             }
         }
         try {
             PropertiesConfiguration pc = new PropertiesConfiguration(filename);
             pc.setReloadingStrategy(getReloadingStrategy());
             configuration.addConfiguration(pc);
            logger.info("Using config file " + filename);
         } catch (ConfigurationException e) {
             logger.error("Could not use config file " + filename, e);
         }
     }
 
     private FileChangedReloadingStrategy getReloadingStrategy() {
         final FileChangedReloadingStrategy reloadingStrategy = new FileChangedReloadingStrategy();
         reloadingStrategy.setRefreshDelay(TimeUnit.SECONDS.toMillis(REFRESH_DELAY_IN_SECONDS));
         return reloadingStrategy;
     }
 
     public String getString(String key) {
         return configuration.getString(key);
     }
 
     public String getString(String key, String defaultValue) {
         return configuration.getString(key, defaultValue);
     }
 
     public Boolean getBoolean(String key) {
         return configuration.getBoolean(key);
     }
 
     public Boolean getBoolean(String key, Boolean defaultValue) {
         return configuration.getBoolean(key, defaultValue);
     }
 
     public Integer getInt(String key) {
         return configuration.getInt(key);
     }
 
     public Integer getInt(String key, Integer defaultValue) {
         return configuration.getInt(key, defaultValue);
     }
 
     public Long getLong(String key) {
         return configuration.getLong(key);
     }
 
     public Long getLong(String key, Long defaultValue) {
         return configuration.getLong(key, defaultValue);
     }
 
     public List<Object> getList(String key) {
         return configuration.getList(key);
     }
 
     public List<Object> getList(String key, List<Object> defaultValue) {
         return configuration.getList(key, defaultValue);
     }
 }
