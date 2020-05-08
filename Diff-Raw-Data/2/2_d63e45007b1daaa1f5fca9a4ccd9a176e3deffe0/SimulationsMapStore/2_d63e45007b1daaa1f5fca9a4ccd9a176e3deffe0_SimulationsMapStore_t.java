 /**
  * Pleiades
  * Copyright (C) 2011 - 2012
  * Computational Intelligence Research Group (CIRG@UP)
  * Department of Computer Science
  * University of Pretoria
  * South Africa
  */
 package net.pleiades.database;
 
 import com.hazelcast.core.MapStore;
 import com.mongodb.BasicDBObject;
 import com.mongodb.DB;
 import com.mongodb.DBCollection;
 import com.mongodb.DBCursor;
 import com.mongodb.DBObject;
 import com.mongodb.Mongo;
 import com.mongodb.WriteConcern;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.util.Collection;
 import java.util.LinkedHashSet;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Set;
 import java.util.concurrent.ConcurrentHashMap;
 import net.pleiades.persistence.PersistentSimulationsMapObject;
 import net.pleiades.simulations.CilibSimulation;
 import net.pleiades.simulations.Simulation;
 
 /**
  *
  * @author bennie
  */
 public class SimulationsMapStore implements MapStore<String, Simulation> {
     private static final String configFile = "pleiades.conf"; //fix this if you can
     private DBCollection jobs;
 
     public SimulationsMapStore() {
         if (!connect()) {
             System.out.println(">ERROR: Unable to connect to persistent store. Contact administrator.");
             System.exit(1);
         }
         System.out.println(">[Connected to jobs map store]");
     }
 
     private boolean connect() {
         Properties properties = loadConfiguration();
         
         Mongo mongo;
         String storeAddress = properties.getProperty("persistent_store_address");
         int storePort = Integer.valueOf(properties.getProperty("persistent_store_port"));
         String pass = properties.getProperty("persistent_store_password");
         String user = properties.getProperty("persistent_store_user");
         boolean auth = false;
         
         try {
             mongo = new Mongo(storeAddress, storePort);
             mongo.setWriteConcern(WriteConcern.SAFE);
             DB db = mongo.getDB("Pleiades");
             auth = db.authenticate(user, pass.toCharArray());
             
             jobs = db.getCollection(properties.getProperty("simulations_map"));
             jobs.setObjectClass(PersistentSimulationsMapObject.class);
         } catch (Exception e) {
             return false;
         }
         
         return auth;
     }
 
     @Override
     public void store(String k, Simulation v) {
         DBObject o = new PersistentSimulationsMapObject(v);
         BasicDBObject query = new BasicDBObject();
 
         query.put("id", o.get("id"));
         
         if (jobs.find(query).toArray().isEmpty()) {
             jobs.insert(o);
         } else {
             jobs.findAndModify(query, o);
         }
     }
 
     @Override
     public void storeAll(Map<String, Simulation> map) {
         for (String k : map.keySet()) {
             store(k, map.get(k));
         }
     }
 
     @Override
     public void delete(String k) {
         BasicDBObject query = new BasicDBObject();
         query.put("id", k);
         
         jobs.remove(query);
     }
 
     @Override
     public void deleteAll(Collection<String> clctn) {
         for (String k : clctn) {
             delete(k);
         }
     }
 
     @Override
     public Simulation load(String k) {
         BasicDBObject query = new BasicDBObject();
         query.put("id", k);
 
         DBObject load = jobs.findOne(query);
 
         if (load == null) {
             return null;
         }
         
         Simulation s = new CilibSimulation((PersistentSimulationsMapObject) load);
         
         return s;
     }
 
     @Override
     public Map<String, Simulation> loadAll(Collection<String> clctn) {
         Map<String, Simulation> sims = new ConcurrentHashMap<String, Simulation>();
 
         for (String k : clctn) {
             sims.put(k, load(k));
         }
 
         return sims;
     }
 
     @Override
     public Set<String> loadAllKeys() {
         Set<String> keys = new LinkedHashSet<String>();
         BasicDBObject query = new BasicDBObject();
        BasicDBObject sort = new BasicDBObject("id", 1);
         
         DBCursor cursor = jobs.find(query).sort(sort);
         
         while (cursor.hasNext()) {
             keys.add((String) cursor.next().get("id"));
         }
         
         return keys;
     }
     
     private static Properties loadConfiguration() {
         Properties p = new Properties();
         
         try {
             p.load(new FileInputStream(configFile));
         } catch (IOException e) {
             throw new Error(">ERROR: Unable to load configuration file " + configFile);
         }
         
         return p;
     }
 }
