 package com.tp.utils;
 
 /**
  * User: ken.cui
  * Date: 13-3-22
  * Time: 上午10:49
  */
 public class UmsConfig {
     private static String jdbcConfig = "classpath:/application.properties";
     private static String develConfig = "classpath:/application.development.properties";
     private static String funConfig = "classpath:/application.functional.properties";
     private static String testConfig = "classpath:/application.test.properties";
 
     private static PropertiesLoader propertiesLoader;
 
     static {
         String contextProfiles = System.getProperty("spring.profiles.active");
        if (contextProfiles == null) {
             propertiesLoader = new PropertiesLoader(jdbcConfig);
        } else {
            if (contextProfiles.equals("development")) {
                propertiesLoader = new PropertiesLoader(jdbcConfig, develConfig);
            } else if (contextProfiles.equals("functional")) {
                propertiesLoader = new PropertiesLoader(jdbcConfig, funConfig);
            } else if (contextProfiles.equals("test")) {
                propertiesLoader = new PropertiesLoader(jdbcConfig, testConfig);
            } else {
                propertiesLoader = new PropertiesLoader(jdbcConfig);
            }
         }
     }
 
     public static String getProperty(String key) {
 
         return propertiesLoader.getProperty(key);
     }
 
     private UmsConfig() {
     }
 }
