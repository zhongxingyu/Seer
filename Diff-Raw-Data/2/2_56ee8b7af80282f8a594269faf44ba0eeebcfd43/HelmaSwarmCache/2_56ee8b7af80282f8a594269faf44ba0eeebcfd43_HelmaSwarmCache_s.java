 /*
  * Helma License Notice
  *
  * The contents of this file are subject to the Helma License
  * Version 2.0 (the "License"). You may not use this file except in
  * compliance with the License. A copy of the License is available at
   * http://adele.helma.org/download/helma/license.txt
  *
  * Copyright 1998-2003 Helma Software. All Rights Reserved.
  *
  * $RCSfile$
  * $Author$
  * $Revision$
  * $Date$
  */
 
 package helma.objectmodel.swarm;
 
 import helma.objectmodel.ObjectCache;
 import helma.objectmodel.db.DbMapping;
 import helma.objectmodel.db.DbKey;
 import helma.objectmodel.db.Node;
 import helma.objectmodel.db.NodeChangeListener;
 import helma.framework.core.Application;
 import helma.util.CacheMap;
 
 import org.jgroups.blocks.*;
 import org.jgroups.*;
 
 import java.io.Serializable;
 import java.util.HashSet;
 import java.util.Properties;
 import java.util.List;
 import java.util.ArrayList;
 
 public class HelmaSwarmCache implements ObjectCache, NodeChangeListener {
 
     CacheMap cache;
 
     Application app;
     String cacheName;
     Connector connector;
 
     /**
      * Initialize the cache client from the properties of our
      * {@link helma.framework.core.Application Application} instance
      *
      * @param app the app instance
      */
     public void init(Application app) {
         this.app = app;
         app.getNodeManager().addNodeChangeListener(this);
         // Configure and Initialize the cache
         cache = new CacheMap();
         connector = new Connector();
     }
 
     /** 
      * Called to shut down the cache when the application terminates.
      */
     public void shutdown() {
         cache.shutdown();
         connector.stop();
     }
 
     /**
      * Called when a transaction is committed that has created, modified or 
      * deleted one or more nodes.
      */
     public void nodesChanged(List inserted, List updated, List deleted) {
         connector.sendNotification(inserted, updated, deleted);
     }
 
     /**
      * Called when the application's properties have been updated to let
      * the cache implementation update its settings.
      * @param props
      */
     public void updateProperties(Properties props) {
         cache.updateProperties(props);
     }
 
     /**
      * Returns true if the collection contains an element for the key.
      *
      * @param key the key that we are looking for
      */
     public boolean containsKey(Object key) {
         return cache.containsKey(key);
     }
 
     /**
      * Returns the number of keys in object array <code>keys</code> that
      * were not found in the Map.
      * Those keys that are contained in the Map are nulled out in the array.
      *
      * @param keys an array of key objects we are looking for
      * @see helma.objectmodel.ObjectCache#containsKey
      */
     public int containsKeys(Object[] keys) {
         return cache.containsKeys(keys);
     }
 
     /**
      * Gets the object associated with the specified key in the
      * hashtable.
      *
      * @param key the specified key
      * @return the element for the key or null if the key
      *         is not defined in the hash table.
      * @see helma.objectmodel.ObjectCache#put
      */
     public Object get(Object key) {
         return cache.get(key);
     }
 
     /**
      * Puts the specified element into the hashtable, using the specified
      * key.  The element may be retrieved by doing a get() with the same key.
      * The key and the element cannot be null.
      *
      * @param key   the specified key in the hashtable
      * @param value the specified element
      * @return the old value of the key, or null if it did not have one.
      * @throws NullPointerException If the value of the element
      *                              is equal to null.
      * @see helma.objectmodel.ObjectCache#get
      */
     public Object put(Object key, Object value) {
         return cache.put(key, value);
     }
 
     /**
      * Removes the element corresponding to the key. Does nothing if the
      * key is not present.
      *
      * @param key the key that needs to be removed
      * @return the value of key, or null if the key was not found.
      */
     public Object remove(Object key) {
         return cache.remove(key);
     }
 
     /**
      * Removes all items currently stored in the cache.
      *
      * @return true if the operation succeeded
      */
     public boolean clear() {
         return cache.clear();
     }
 
     /**
      * Return the number of objects currently stored in the cache.
      *
      * @return the number of cached items
      */
     public int size() {
         return cache.size();
     }
 
     /**
      * Return an array with all objects currently contained in the cache.
      */
     public Object[] getCachedObjects(){
         return cache.getCachedObjects();
     }
 
     
     class Connector implements NotificationBus.Consumer {
         
         // jgroups properties. copied from swarmcache
         final static String groupPropsPrefix =  "UDP(";
         // plus something like this created from app.properties:
         // mcast_addr=231.12.21.132;mcast_port=45566;ip_ttl=32;
         final static String groupPropsSuffix = 
             "mcast_send_buf_size=150000;mcast_recv_buf_size=80000):" +
             "PING(timeout=2000;num_initial_members=3):" +
             "MERGE2(min_interval=5000;max_interval=10000):" +
             "FD_SOCK:" +
             "VERIFY_SUSPECT(timeout=1500):" +
             "pbcast.NAKACK(gc_lag=50;retransmit_timeout=300,600,1200,2400,4800):" +
             "UNICAST(timeout=5000):" +
             "pbcast.STABLE(desired_avg_gossip=20000):" +
             "FRAG(frag_size=8096;down_thread=false;up_thread=false):" +
             "pbcast.GMS(join_timeout=5000;join_retry_timeout=2000;shun=false;print_local_addr=true)";
 
         private String mcast_ip = "224.0.0.132";
         private String mcast_port = "22023";
         private String ip_ttl = "32";
         private String bind_port = "48848";
         private String port_range = "1000";
 
         private NotificationBus bus;
         private Address address;
         
         public Connector() {
        	try {
                 String groupProps = getJGroupProperties();
                 bus = new NotificationBus("HelmaSwarm", groupProps);
                 bus.start();
                 bus.getChannel().setOpt(Channel.AUTO_RECONNECT, new Boolean(true));
                 bus.setConsumer(this);
                 address = bus.getLocalAddress();
                 app.logEvent("HelmaSwarm: Joined notification bus, local addres is "+address);
             } catch (Exception e) {
                 app.logError("HelmaSwarm: Error starting/joining notification bus", e);
                 e.printStackTrace();
             }
         }
 
         public void stop() {
             bus.stop();
         }
 
         public Serializable getCache() {
             return null;
         }
 
         public void sendNotification(List inserted, List updated, List deleted) {
             if (!inserted.isEmpty() || !updated.isEmpty() || !deleted.isEmpty()) {
                 HashSet keys = new HashSet();
                 HashSet types = new HashSet();
                 collectUpdates(inserted, keys, types);
                 collectUpdates(updated, keys, types);
                 collectUpdates(deleted, keys, types);
                 app.logEvent("HelmaSwarm: Sending invalidation for "+keys+", "+types);
                 InvalidationList list = new InvalidationList(address, keys, types);
                 bus.sendNotification(list);
             }
         }
 
         public void handleNotification(Serializable object) {
             InvalidationList list = (InvalidationList) object;
             if (address.equals(list.address)) {
                 app.logEvent("HelmaSwarm: got own message, returning");
                 return;
             }
             for (int i=0; i<list.types.length; i++) {
                 app.logEvent("HelmaSwarm: marking "+list.types[i]);
                 DbMapping dbm = app.getDbMapping(list.types[i]);
                 long now = System.currentTimeMillis();
                 if (dbm != null) {
                     dbm.setLastDataChange(now);
                 }
             }
             for (int i=0; i<list.keys.length; i++) {
                 app.logEvent("HelmaSwarm: invalidating "+list.keys[i]);
                 Node node = (Node) cache.remove(list.keys[i]);
                 if (node != null) {
                     node.setState(Node.INVALID);
                 }
             }
         }
 
         public void memberJoined(Address who) {
             app.logEvent("HelmaSwarm: A host has joined the cache notification bus: " + who + ".");
         }
 
         public void memberLeft(Address who) {
             app.logEvent("HelmaSwarm: A host has left the cache notification bus: " + who + ".");
         }
 
         public String getJGroupProperties() {
             StringBuffer b = new StringBuffer(groupPropsPrefix);
             b.append("mcast_addr=");
             b.append(app.getProperty("helmaswarm.multicast_ip", mcast_ip));
             b.append(";mcast_port=");
             b.append(app.getProperty("helmaswarm.multicast_port", mcast_port));
             b.append(";ip_ttl=");
             b.append(app.getProperty("helmaswarm.ip_ttl", ip_ttl));
             b.append(";bind_port=");
             b.append(app.getProperty("helmaswarm.bind_port", bind_port));
             b.append(";port_range=");
             b.append(app.getProperty("helmaswarm.port_range", port_range));
             String bind_addr = app.getProperty("helmaswarm.bind_addr");
             if (bind_addr != null) {
                 b.append(";bind_addr=");
                 b.append(bind_addr);
             }
             b.append(";");
             b.append(groupPropsSuffix);
             return b.toString();
         }
         
         private void collectUpdates(List list, HashSet keys, HashSet types) {
             for (int i=0; i<list.size(); i++) {
                 Node node = (Node) list.get(i);
                 keys.add(node.getKey());
                 DbMapping dbm = node.getDbMapping();
                 if (dbm != null) {
                     types.add(dbm.getTypeName());
                 }
             }
         }
     }
     
     static class InvalidationList implements Serializable {
         Address address;
         Object[] keys;
         String[] types;
         
         public InvalidationList(Address address, HashSet keys, HashSet types) {
             this.address = address;
             this.keys = keys.toArray();
             this.types = (String[]) types.toArray(new String[types.size()]);
         }
     }
 }
