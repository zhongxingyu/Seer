 package com.worksnet.system;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.Properties;
 
 /**
  * @author Max Levicky
  *         Date: 25.02.13
  *         Time: 23:57
  */
 public class Conf {
     final protected static Properties settings = new Properties();
     protected static String confPath;
 
     static {
         confPath = System.getProperty("user.home") + "/worksnetConf/";
         if (!new File(confPath).isDirectory()) {
             throw new Error("Can't find configuration path. Put configs in $HOME/worksnetConf/main.properties");
         }
     }
 
     public static String getConfPath() throws FileNotFoundException {
         return confPath;
     }
 
     public static String get(String name) {
         if (settings.isEmpty()) {
             init();
         }
         String result = settings.getProperty(name);
         if (result == null) {
             throw new Error("Property " + name + " not found");
         }
         return result;
     }
 
     public static boolean isDev() {
         try {
             return Boolean.parseBoolean(get("isDev"));
         } catch (ConfFileNotFound e) {
             return false;
         }
     }
 
     protected static void init() {
         try {
             synchronized (settings) {
                 if (settings.isEmpty()) {
                     settings.load(getMainConfFile());
                 }
             }
         } catch (IOException e) {
             throw new Error("Property file not found");
         }
     }
 
     protected static FileInputStream getMainConfFile() throws FileNotFoundException {
         try {
             return new FileInputStream(getConfPath() + "main.properties");
         } catch (FileNotFoundException e) {
            Log.getLogger(Conf.class).error("Can't open config file.", e);
             throw new ConfFileNotFound();
         }
     }
 
     protected static class ConfFileNotFound extends Error {
     }
 
 }
