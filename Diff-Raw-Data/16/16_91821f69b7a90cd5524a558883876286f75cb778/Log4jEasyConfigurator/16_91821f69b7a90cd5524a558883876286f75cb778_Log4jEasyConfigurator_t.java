 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package de.cismet.jpresso.core.log4j.config;
 
 import de.cismet.jpresso.core.serviceprovider.JPressoFileManager;
 import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.FileInputStream;
import java.io.IOException;
 import java.util.Properties;
 
 /**
  *
  * @author srichter
  */
 public class Log4jEasyConfigurator {
 
     public static void configLog4j() {
         final String home = System.getProperty("user.home");
         final File logConf = new File(home, JPressoFileManager.LOG_CONFIG);
         final Properties p = new Properties();
         if (logConf.isFile()) {
            FileInputStream fis = null;
             try {
                fis = new FileInputStream(logConf);
                p.load(new BufferedInputStream(fis));
                 System.out.println("Loading log4J configuration from file.");
             } catch (Exception ex) {
                 //ignore
            } finally {
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (IOException ex) {
                        System.out.println(ex);
                    }
                }
             }
         }
         if (p.isEmpty()) {
             p.put("log4j.appender.Remote", "org.apache.log4j.net.SocketAppender");
             p.put("log4j.appender.Remote.remoteHost", "localhost");
             p.put("log4j.appender.Remote.port", "4445");
             p.put("log4j.appender.Remote.locationInfo", "true");
             final File debugIndicator = new File(home, File.separator + JPressoFileManager.DEBUG_INDICATOR);
             if (debugIndicator.isFile()) {
                 p.put("log4j.rootLogger", "DEBUG,Remote");
             } else {
                 p.put("log4j.rootLogger", "INFO,Remote");
             }
             System.out.println("Using standard log4J configuration with " + p.getProperty("log4j.rootLogger"));
         }
         try {
             org.apache.log4j.PropertyConfigurator.configure(p);
         } catch (Exception ex) {
             System.err.println("Can not connect to local log4j host! Will try again later.");
         }
     }
 }
