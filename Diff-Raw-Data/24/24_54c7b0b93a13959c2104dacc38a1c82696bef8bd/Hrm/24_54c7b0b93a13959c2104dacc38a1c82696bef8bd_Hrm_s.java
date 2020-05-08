 package de.hswt.hrm.common;
 
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.nio.file.Paths;
 
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.lang.SystemUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Holds application methods like "init" or "shutdown".
  */
 public final class Hrm {
     
     private static final Logger LOG = LoggerFactory.getLogger(Hrm.class);
     
     private Hrm() { }
     
     /**
      * Initializes the application. (E. g. load configuration file)
      */
     public static void init() {
         // Try to find the current config
         Path configPath = getConfigPath().resolve("hrm.properties");
         if (!Files.exists(configPath)) {
             try {
                 // Create a default one
                 FileOutputStream targetFile = new FileOutputStream(configPath.toFile());
                 InputStream configFile = BundleUtil.getStreamForFile("de.hswt.hrm.common", 
                         "resources/hrm.properties");
                 IOUtils.copy(configFile, targetFile);
             }
             catch (IOException e) {
                LOG.error("Could create default configuration file from bundle.", e);
             }
             
         }
         
         Config cfg = Config.getInstance();
         try {
             cfg.load(configPath);
         }
         catch (IOException e) {
             LOG.error("Could not load configuration at '" + configPath.toString()+ "'.", e);
         }
     }
     
     /**
      * @return The correct home directory of the current user.
      */
     private static Path getConfigPath() {
         // We have to do this as the java standard way seems to be broken
         // for Windows
         if (SystemUtils.IS_OS_WINDOWS) {
             String path = System.getenv("HOMEDRIVE") + System.getenv("HOMEPATH");
             Path userHome = Paths.get(path);
             return userHome.resolve("AppData\\hrm");
         }
         else {
             // This is a linux way, maybe this should be changed to also reflect
             // Mac OSX style
             Path userHome = SystemUtils.getUserHome().toPath();
             return userHome.resolve(".config/hrm");
         }
     }
     
 }
