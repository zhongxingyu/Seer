 package com.ifmo.optiks;
 
 import java.io.*;
 import java.util.Properties;
 
 /**
  * Author: Sergey Fedorov (serezhka@xakep.ru)
  * Date: 01.04.12
  */
 
 public class OptiksProps {
 
    public static final String PROPS_FILE = "com/ifmo/optiks/Optiks.properties";

    private static final OptiksProps props = new OptiksProps();
 
     private static Properties properties;
 
     public static String getProperty(final String key) {
         init();
         if (properties == null) {
             return null;
         }
         return properties.getProperty(key);
     }
 
    public static String getProperty(final Keys key) {
        return getProperty(key.name);
    }

     private static void init() {
         if (properties == null) {
             try {
                final Reader reader = new BufferedReader(
                        new InputStreamReader(props.getClass().getClassLoader().getResourceAsStream(PROPS_FILE)));
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
 
        public final String name;
 
         Keys(final String name) {
             this.name = name;
         }
 
         @Override
         public String toString() {
             return name;
         }

        public static Keys getByName(final String name) {
            for (final Keys key : Keys.values()) {
                if (name.equalsIgnoreCase(key.name)) {
                    return key;
                }
            }
            return null;
        }
     }
 }
