 package com.edsdev.jconvert.util;
 
 import java.io.FileOutputStream;
 import java.util.Properties;
 
 /**
  * "Static" class that represents the settings of the application that could be configured by the user.
  * 
  * @author Ed S Created on Sep 19, 2007 4:27:45 PM
  */
 public class JConvertSettingsProperties {
     private static Properties props = null;
 
     private static final Logger log = Logger.getInstance(JConvertSettingsProperties.class);
 
     private static final String FILE_NAME = "jconvert_settings.properties";
 
     public static final String APP_WIDTH = "ApplicationWidth";
 
     public static final String APP_HEIGHT = "ApplicationHeight";
 
     public static final String APP_X = "ApplicationX";
 
     public static final String APP_Y = "ApplicationY";
 
     public static final String HIDDEN_TABS = "HiddenTabs";
 
     public static final String LAST_TAB = "LastTab";
 
     /** Static initializer - lets do this once */
     static {
         try {
             props = ResourceManager.loadProperties(FILE_NAME);
         } catch (Exception e) {
            log.error("Failed to load the jconvert settings. - " + FILE_NAME, e);
             props = new Properties();
         }
     }
 
     private static String getFilePath() {
         String jarPath = ResourceManager.getJarPath();
         return jarPath + FILE_NAME;
     }
 
     public static void persist() {
         try {
             FileOutputStream fos = new FileOutputStream(getFilePath());
             props.store(fos, "Jconvert Settings File");
         } catch (Exception e) {
             log.error("Failed to save jconvert settings.", e);
         }
     }
 
     private JConvertSettingsProperties() {
         //not public
     }
 
     public static String getProp(String propName) {
         return props.getProperty(propName);
     }
 
     public static String getAppWidth() {
         return props.getProperty(APP_WIDTH);
     }
 
     public static void setAppWidth(String val) {
         props.setProperty(APP_WIDTH, val);
     }
 
     public static String getAppHeight() {
         return props.getProperty(APP_HEIGHT);
     }
 
     public static void setAppHeight(String val) {
         props.setProperty(APP_HEIGHT, val);
     }
 
     public static String getAppX() {
         return props.getProperty(APP_X);
     }
 
     public static void setAppX(String val) {
         props.setProperty(APP_X, val);
     }
 
     public static String getAppY() {
         return props.getProperty(APP_Y);
     }
 
     public static void setAppY(String val) {
         props.setProperty(APP_Y, val);
     }
 
     public static String getHiddenTabs() {
         return props.getProperty(HIDDEN_TABS);
     }
 
     public static void setHiddenTabs(String val) {
         props.setProperty(HIDDEN_TABS, val);
     }
 
     public static String getLastTab() {
         return props.getProperty(LAST_TAB);
     }
 
     public static void setLastTab(String val) {
         props.setProperty(LAST_TAB, val);
     }
 }
