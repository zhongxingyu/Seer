 /*
  * $Id: $
  *
  * Copyright (c) 2009 Fujitsu Denmark
  * All rights reserved.
  */
 package dk.fujitsu.issuecheck;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Properties;
 
 /**
  * @author Claus Brøndby Reimer (dencbr) / Fujitsu Denmark a|s
  * @version $Revision: $ $Date: $
  */
 public class Config {
     private static final Properties PROPERTIES = new Properties();
     public static Map<String, Object> INSTANCES = new HashMap<String, Object>();
 
     static {
         InputStream config;
 
         config = Config.class.getClassLoader().getResourceAsStream("commit-check-config.properties");
         try {
             PROPERTIES.load(config);
         } catch (IOException x) {
             throw new RuntimeException("unable to load commit-check-config.properties, " + x.getMessage(), x);
         }
     }
 
     public static <T> T getInstance(String type) {
         T object;
 
         object = (T) INSTANCES.get(type);
         if (object == null) {
             try {
                 object = (T) Class.forName(PROPERTIES.getProperty(type + ".implementation")).newInstance();
                 INSTANCES.put(type, object);
             } catch (Throwable x) {
                 throw new RuntimeException("unable to create scm implementation of " + Config.get(type + ".implementation") + ", " + x.getMessage(), x);
             }
         }
 
         return object;
     }
 
 
     public static String get(String key) {
         return PROPERTIES.getProperty(key);
     }
 
     public static void set(String key, String value) {
         PROPERTIES.setProperty(key, value);
     }
 
     public static void exit(int value) {
         System.exit(value);
     }
 
     public static void out(String message) {
         System.out.println(message);
     }
 }
