 package org.whitesource.teamcity.server;
 
 import com.thoughtworks.xstream.XStream;
 import jetbrains.buildServer.log.Loggers;
 import jetbrains.buildServer.serverSide.ServerPaths;
 import jetbrains.buildServer.serverSide.crypt.RSACipher;
 import jetbrains.buildServer.util.FileUtil;
 import jetbrains.buildServer.util.StringUtil;
import jetbrains.buildServer.util.XmlUtil;
 import org.jetbrains.annotations.NotNull;
 import org.whitesource.teamcity.common.WssUtils;
 
 import java.io.*;
 
 /**
  * Global configuration for the plugin.
  *
  * @author Edo.Shor
  */
 public class GlobalSettingsManager {
 
     /* --- Static members --- */
 
     private static final String LOG_COMPONENT = "GlobalConfig";
 
     private final static String CONFIG_FILE_NAME = "whitesource-config.xml";
 
     /* --- Members --- */
 
     private File configFile;
 
     private XStream xStream;
 
     private GlobalSettings globalSettings;
 
     /* --- Constructors --- */
 
     /**
      * Constructor
      *
      * @param serverPaths
      */
     public GlobalSettingsManager(@NotNull ServerPaths serverPaths) {
         xStream = new XStream();
         xStream.processAnnotations(GlobalSettings.class);
         xStream.setClassLoader(GlobalSettings.class.getClassLoader());
 
         configFile = new File(serverPaths.getConfigDir(), CONFIG_FILE_NAME);
         loadConfig();
     }
 
     /* --- Public methods --- */
 
     public void save() {
         FileOutputStream fos = null;
         try {
             fos = new FileOutputStream(configFile);
             xStream.toXML(globalSettings, fos);
         } catch (FileNotFoundException e) {
            Loggers.SERVER.error(WssUtils.logMsg(LOG_COMPONENT,"Failed to save config file " + configFile), e);
         } finally {
             FileUtil.close(fos);
         }
     }
 
     public boolean isProxy() {

        return globalSettings.getProxy() != null &&
                 !StringUtil.isEmptyOrSpaces(globalSettings.getProxy().getHost());
     }
 
     public String getHexEncodedPublicKey() {
         return RSACipher.getHexEncodedPublicKey();
     }
 
     /* --- Private methods --- */
 
    private void loadConfig(){
         if (configFile.exists()) {
             FileInputStream fis = null;
             try {
                 fis = new FileInputStream(configFile);
                 globalSettings = (GlobalSettings) xStream.fromXML(fis);
             } catch (IOException e) {
                Loggers.SERVER.error(WssUtils.logMsg(LOG_COMPONENT,"Failed to load config file " + configFile), e);
             } finally {
                 FileUtil.close(fis);
             }
         }
     }
 
     /* --- Getters / Setters --- */
 
     public GlobalSettings getGlobalSettings() {
         return globalSettings;
     }
 
     public void setGlobalSettings(GlobalSettings globalSettings) {
         this.globalSettings = globalSettings;
     }
 }
