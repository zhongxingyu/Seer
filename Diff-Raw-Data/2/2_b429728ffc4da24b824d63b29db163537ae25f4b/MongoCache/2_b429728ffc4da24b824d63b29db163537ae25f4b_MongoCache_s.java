 package com.realistiq.cache;
 
 import com.mongodb.*;
 
 import java.io.IOException;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.List;
 
 public class MongoCache {
 
 
     private Mongo _mongo;
     private DB _db;
     private DBCollection _coll;
 
     public MongoCache(String addresses,String database,String collection){
 
         List<ServerAddress> addr = new ArrayList<ServerAddress>();
         String[] hosts = addresses.split("\\n");
 
         for(int i=0; i < hosts.length; i++){
             try {
                 addr.add(new ServerAddress(hosts[i]));
             } catch (UnknownHostException e) {
                 e.printStackTrace();
             }
         }
 
         _mongo = new Mongo(addr);
         _db = _mongo.getDB(database);
         _coll = _db.getCollection(collection);
 
         //create the indexes
         _coll.ensureIndex(new BasicDBObject("key",1));
         _coll.ensureIndex(new BasicDBObject("expires",1));
         _coll.ensureIndex(new BasicDBObject("tags",1));
 
     }
 
     /**
      * Save a new cache object( overwrite if teh same key already exists)
      * @param key
      * @param value
      * @param lifespan  Number of seconds. 0 = no end
      * @param tags
      */
     public void put(String key,String value,int lifespan,List tags){
 
         Long created = System.currentTimeMillis();
         Long life = new Long(0);
 
         if(lifespan > 0){
            life = lifespan*100 + created;
         }
 
         BasicDBObject obj = new BasicDBObject();
         obj.put("key",key.toLowerCase());
         obj.put("value",value);
         obj.put("expires",life);
         obj.put("tags", tags);
 
         _coll.insert(obj);
 
     }
 
     /**
      * Save a new cache object( overwrite if teh same key already exists)
      * @param key
      * @param value
      */
     public void put(String key,String value){
         put(key,value,0,new ArrayList());
     }
 
     /**
      * Save a new cache object( overwrite if teh same key already exists)
      * @param key
      * @param value
      * @param lifespan  Number of seconds. 0 = no end
      */
     public void put(String key,String value,int lifespan){
         put(key,value,lifespan,new ArrayList());
     }
 
 
     /**
      * Get a value from cache
      * @param key
      * @return value
      * @throws IOException
      */
     public String get(String key) throws IOException{
 
         //expired items must be removed
         clearExpired();
 
         //get the item
         BasicDBObject q = new BasicDBObject();
         q.put("key",key.toLowerCase());
         DBObject obj = _coll.findOne(q);
 
        if(obj == null){
             throw new IOException("Key [" + key + "] does not exists in this cache.");
         }
 
         String value = (String)obj.get("value");
 
        return value;
 
     }
 
     /**
      * Remove a key from the cache db
      * @param key
      */
     public void remove(String key){
         _coll.remove(new BasicDBObject("key",key));
     }
 
     /**
      * Drop the entire cache collection
      */
     public void flush(){
         _coll.drop();
     }
 
     /**
      * Get the Mongo driver instance
      * @return Mongo
      */
     public Mongo get_mongo() {
         return _mongo;
     }
 
     /**
      * Get the actual used database instance
      * @return DB
      */
     public DB get_db() {
         return _db;
     }
 
     /**
      * Get collection instance used by this cache
      * @return  DBCollection
      */
     public DBCollection get_coll() {
         return _coll;
     }
 
     /**
      * Remove all the expired keys
      */
     public void clearExpired(){
 
         Long now = System.currentTimeMillis();
         BasicDBObject q = new BasicDBObject("expires", new BasicDBObject("$lt",now).append("$gt",0));
 
         // remove the match
         _coll.remove(q);
 
     }
 
     /**
      * remove the keys that match the passed criteria in the tags array
      * @param tags
      */
     public void clearTags(String tags){
         String[] tgs = tags.split(",");
         for(String t : tgs){
             _coll.remove(new BasicDBObject("tags",t));
         }
 
     }
 
     /**
      * Return true if the key exists in cache
      * @param key
      * @return
      */
     public boolean exists(String key){
        //clean
         clearExpired();
         //get the item
         DBCursor obj = _coll.find(new BasicDBObject("key",key.toLowerCase()), new BasicDBObject("key",1));
 
        if(obj.size() == 0){
             return false;
         }
 
         return true;
     }
 }
