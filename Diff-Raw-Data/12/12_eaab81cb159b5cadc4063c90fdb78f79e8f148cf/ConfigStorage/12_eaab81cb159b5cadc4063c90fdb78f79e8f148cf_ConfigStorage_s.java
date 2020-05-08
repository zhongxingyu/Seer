 package com.github.vmorev.amazon.utils;
 
 import org.codehaus.jackson.map.ObjectMapper;
 
 import java.io.IOException;
 import java.util.Map;
 
 /**
  * User: Valentin_Morev
  * Date: 13.01.13
  */
 public class ConfigStorage {
 
     public static <T> T loadObject(String configName, Class<T> clazz, boolean checkLocalAndTest) {
         T config;
         try {
             config = new ObjectMapper().readValue(ClassLoader.getSystemResource(configName), clazz);
         } catch (IOException e) {
             throw new RuntimeException(e);
         }
         T localConfig = null;
         if (checkLocalAndTest) {
             try {
                 localConfig = new ObjectMapper().readValue(ClassLoader.getSystemResource(configName.replace(".json", ".local.json")), clazz);
            } catch (IOException e) {
                 //just ignore
             }
             try {
                 localConfig = new ObjectMapper().readValue(ClassLoader.getSystemResource(configName.replace(".json", ".test.json")), clazz);
            } catch (IOException e) {
                 //just ignore
             }
             if (localConfig != null)
                 config = localConfig;
         }
         return config;
     }
 
     public static Map loadMap(String configName, boolean checkLocalAndTest) {
         Map config;
         try {
             config = new ObjectMapper().readValue(ClassLoader.getSystemResource(configName), Map.class);
         } catch (IOException e) {
             throw new RuntimeException(e);
         }
         if (checkLocalAndTest) {
             try {
                 Map localConfig = new ObjectMapper().readValue(ClassLoader.getSystemResource(configName.replace(".json", ".local.json")), Map.class);
                 config.putAll(localConfig);
             } catch (Throwable e) {
                 //just ignore
             }
             try {
                 Map localConfig = new ObjectMapper().readValue(ClassLoader.getSystemResource(configName.replace(".json", ".test.json")), Map.class);
                 config.putAll(localConfig);
             } catch (Throwable e) {
                 //just ignore
             }
         }
         return config;
     }
 
 }
