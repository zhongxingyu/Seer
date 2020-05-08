 package de.fgtech.pomo4ka.AuthMe.DataSource;
 
 import java.util.HashMap;
 import java.util.Map;
 
 public class DataCache implements DataSource {
 
     private HashMap<String,String> cache;
     private DataSource source;
     private boolean caching = false;
 
     public DataCache(DataSource source, boolean caching) {
         this.source = source;
         this.caching = caching;
         if(caching) {
             this.cache = source.loadAllAuths();
         } else {
             this.cache = null;
         }
     }
 
     @Override
     public HashMap<String, String> loadAllAuths() {
         if(caching) {
             cache = source.loadAllAuths();
             return cache;
         } else {
             return source.loadAllAuths();
         }
     }
 
     @Override
     public boolean saveAuth(String playername, String hash,
             Map<String, String> customInformation) {
         playername = playername.toLowerCase();
         if(source.saveAuth(playername, hash, customInformation)) {
             if(caching) cache.put(playername, hash);
             return true;
         }
         return false;
     }
 
     @Override
     public boolean updateAuth(String playername, String hash) {
         playername = playername.toLowerCase();
         if(source.updateAuth(playername, hash)) {
             if(caching) {
                 cache.put(playername, hash);
             }
             return true;
         }
         return false;
     }
 
     @Override
     public boolean removeAuth(String playername) {
         playername = playername.toLowerCase();
         if(source.removeAuth(playername)) {
             if(caching) {
                 cache.remove(playername);
             }
             return true;
         }
         return false;
     }
 
     @Override
     public String loadHash(String playername) {
         playername = playername.toLowerCase();
         if(caching) {
             return cache.get(playername);
         } else {
             return source.loadHash(playername);
         }
     }
 
     @Override
     public boolean isPlayerRegistered(String playername) {
         playername = playername.toLowerCase();
         if(caching) {
             return cache.containsKey(playername);
         } else {
             return source.isPlayerRegistered(playername);
         }
     }
 
     @Override
     public int getRegisteredPlayerAmount() {
         if(caching) {
             return cache.size();
         } else {
             return source.getRegisteredPlayerAmount();
         }
     }

 }
