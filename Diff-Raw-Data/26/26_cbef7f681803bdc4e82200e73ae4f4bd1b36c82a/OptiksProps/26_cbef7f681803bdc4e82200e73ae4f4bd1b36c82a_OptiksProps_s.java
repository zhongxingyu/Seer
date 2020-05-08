 package com.ifmo.optiks;
 
 import java.io.*;
 import java.util.Properties;
 
 /**
  * Author: Sergey Fedorov (serezhka@xakep.ru)
  * Date: 01.04.12
  */
 
 public class OptiksProps {
 
    public static final String PROPS_FILE = "Optiks.properties";
 
     private static Properties properties;
 
     public static String getProperty(final String key) {
         init();
         if (properties == null) {
             return null;
         }
         return properties.getProperty(key);
     }
 
     private static void init() {
         if (properties == null) {
             try {
                final Reader reader = new BufferedReader(new FileReader(PROPS_FILE));
                 properties = new Properties();
                 properties.load(reader);
             } catch (FileNotFoundException e) {
                 /* Do nothing. Log */
             } catch (IOException e) {
                 /* Do nothing. Log */
             }
         }
     }
 
     public enum Keys {
 
         SERVER_ADDRESS("server_address"),
         GET_LEVEL_BOLET("get_level_bolet");
 
        private final String name;
 
         Keys(final String name) {
             this.name = name;
         }
 
         @Override
         public String toString() {
             return name;
         }
     }
 }
