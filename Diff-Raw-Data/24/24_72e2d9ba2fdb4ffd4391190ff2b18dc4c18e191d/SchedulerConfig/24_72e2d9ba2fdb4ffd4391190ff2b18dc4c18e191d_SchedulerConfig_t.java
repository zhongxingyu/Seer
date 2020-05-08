 package com.payneteasy.mysql.scheduler;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.util.StringUtils;
 
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Properties;
 
 public class SchedulerConfig {
     
     private static final Logger LOG = LoggerFactory.getLogger(SchedulerConfig.class);
     
     private static Map<Config, String> theMap ;
     
     public enum Config { 
         
        MAX_THREADS("15")
         , SLEEP_MS("20000")
         , URL("jdbc:mysql://localhost:3306/sched?logger=Slf4JLogger")
         , USERNAME("java_sched")
        , PASSWORD("123java_sched123")
        , WAIT_SHUTDOWN_SECONDS("900")
        ;
         
         Config(String aValue) {
             theValue = aValue;
         }
         
         
         private String theValue;
     }
 
     
     static {
 
         Properties fileProperties = new Properties();
 
         final String config_file = System.getProperty("CONFIG_FILE");
         if(StringUtils.hasText(config_file)) {
             LOG.info("Loading config file {}...", config_file);
             try {
                 FileReader in = new FileReader(config_file);
                 try {
                     fileProperties.load(in);
                 } catch (IOException e) {
                     in.close();
                 }
             } catch (IOException e) {
                 LOG.error("Error reading properties from "+config_file, e);
                 System.exit(1);
             }
         }
         
         LOG.info("Config values:");
         theMap = new HashMap<Config, String>();        
         for (Config config : Config.values()) {
             String value = System.getProperty(config.name());
             if(!StringUtils.hasText(value)) {
                 value = fileProperties.getProperty(config.name(), config.theValue);
             }
 
             LOG.info("    {} = {}", config, config==Config.PASSWORD ? "xxxx" : value);
             theMap.put(config, value);
         }
     }
     
     public static String getConfig(Config aConfig) {
         return theMap.get(aConfig);
     }
 
     public static int getIntConfig(Config aConfig) {
         return Integer.parseInt(theMap.get(aConfig));
     }
     
     
 }
