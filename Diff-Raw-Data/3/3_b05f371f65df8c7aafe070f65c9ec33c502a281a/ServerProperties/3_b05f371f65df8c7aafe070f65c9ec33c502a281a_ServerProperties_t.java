 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package com.github.etsai.kfsxtrackingserver;
 
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.Properties;
 
 /**
  * Stores properties for the tracking server
  * @author etsai
  */
 public class ServerProperties {
     private static Properties props;
     private static Properties defaults;
     
     public static final String propUdpPort= "udp.port";
     public static final String propHttpPort= "http.port";
     public static final String propPassword= "password";
    public static final String propStatsMsgTTL= "60000";
     public static final String propDbName= "db.name";
     public static final String propDbWritePeriod= "db.write.period";
     public static final String propSteamPollingPeriod= "steam.polling.period";
     public static final String propLogLevel= "log.level";
     
     public synchronized static Properties load(String filename) throws IOException {
         if (props == null) {
             props= new Properties();
             props.load(new FileReader(filename));
         }
         
         return props;
     }
     
     public synchronized static Properties getDefaults() {
         if (defaults == null) {
             defaults= new Properties();
             defaults.setProperty(propUdpPort, "6000");
             defaults.setProperty(propHttpPort, "8080");
             defaults.setProperty(propPassword, "server");
            defaults.setProperty(propStatsMsgTTL, "60000");
             defaults.setProperty(propDbName, "kfsxdb.sqlite");
             defaults.setProperty(propDbWritePeriod, "1800000");
             defaults.setProperty(propSteamPollingPeriod, "21600000");
             defaults.setProperty(propLogLevel, "INFO");
         }
         return defaults;
     }
 }
