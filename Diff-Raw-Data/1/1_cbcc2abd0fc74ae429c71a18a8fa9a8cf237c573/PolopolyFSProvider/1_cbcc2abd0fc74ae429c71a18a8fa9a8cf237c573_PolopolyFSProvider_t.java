 package com.polopoly.javarebel.fs;
 
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.ConcurrentHashMap;
 
 import com.polopoly.javarebel.cfg.Configuration;
 import com.polopoly.javarebel.cfg.Configuration.Item;
 import com.polopoly.javarebel.cfg.ConfigurationProvider;
 import com.polopoly.javarebel.cfg.ConfigurationProvider.Cfg;
 
 public class PolopolyFSProvider implements FSProvider {
 
     public static PolopolyFSProvider instance() {
         return Instance.instance;
     }
 
     private static class Instance {
         public static PolopolyFSProvider instance = new PolopolyFSProvider();
     }
 
     Map<String, FS> filterCache = new ConcurrentHashMap<String, FS>();
     Map<String, FS> contentCache = new ConcurrentHashMap<String, FS>();
     long lastChange = -1;
 
     public FS getContentFS(String externalid)
     {
         Configuration config = getConfiguration();
         if (config == null) {
             return null;
         }
         FS fs = contentCache.get(externalid);
         if (fs != null) {
             return fs;
         }
         List<Configuration.Item> item = config.getContentFiles(externalid);
         if (item == null) {
             return null;
         }
         fs = createFS(item);
         contentCache.put(externalid, fs);
         return fs;
     }
 
     public FS getFilterFS(String filtername)
     {
         Configuration config = getConfiguration();
         if (config == null) {
             return null;
         }
         FS fs = filterCache.get(filtername);
         if (fs != null) {
             return fs;
         }
         List<Configuration.Item> item = config.getFilterFiles(filtername);
         if (item == null) {
             return null;
         }
         fs = createFS(item);
         filterCache.put(filtername, fs);
         return fs;
     }
 
     private FS createFS(List<Item> item)
     {
         return new PolopolyFS(item);
     }
 
     private Configuration getConfiguration()
     {
         Cfg cfg = ConfigurationProvider.instance().getConfiguration();
         if (cfg.lastModified > lastChange) {
             filterCache.clear();
             contentCache.clear();
            lastChange = cfg.lastModified;
         }
         return cfg.configuration;
     }
 }
