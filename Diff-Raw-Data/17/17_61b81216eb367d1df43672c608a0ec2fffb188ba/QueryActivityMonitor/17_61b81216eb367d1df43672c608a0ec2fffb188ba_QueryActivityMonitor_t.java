 package org.sciflex.plugins.synapse.esper.mediators.monitors;
 
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentMap;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import org.sciflex.plugins.synapse.esper.core.util.UUIDGenerator;
 import org.sciflex.plugins.synapse.esper.core.util.QueryObject;
 import org.sciflex.plugins.synapse.esper.mediators.helpers.EPLStatementHelper;
 import org.sciflex.plugins.synapse.esper.ui.Browsable;
 
 /**
  * Provides necessary capability to monitor a Query Activity. The Query Activity
  * monitor works based on a {@link EPLStatementHelper} type object.
  * Each Query Activity monitor will have at most one such associated
  * {@link EPLStatementHelper} type object.
  */
 public class QueryActivityMonitor implements Browsable, QueryActivity {
 
     /**
      * Log associated with the Query Activity Monitor.
      */
     private static final Log log = LogFactory.getLog(QueryActivityMonitor.class);
 
     /**
      * The {@link EPLStatementHelper} whom the Query Activity Monitor will assit.
      */
     private EPLStatementHelper owner = null;
 
     /**
      * Unique Identifier of this object.
      */
     private String uid = null;
 
     /**
      * Unique Identifier of this object's parent.
      */
     private String parentUID = null;
 
     /**
      * Stores a Map of query activity records.
      */
     private ConcurrentMap<String, QueryObject> queryActivityMap = null;
 
     /**
      * The current query object.
      */
     private QueryObject current = null;
 
     /**
      * Object used to lock updates
      */
     private Object updateLock = new Object();
 
     /**
      * Constructor accepting the Owner of the EPL.
      *
      * @param owner The Owner of the EPL. The owner cannot be null.
      *              The owner is of type {@link EPLStatementHelper}.
      */
     public QueryActivityMonitor(EPLStatementHelper owner) {
         this.owner = owner;
     }
 
     /**
      * Notifies that the EPL Query has been used.
      *
      * @param epl EPL query.
      */
     public void notify(String epl) {
         if (epl == null) {
             log.error("EPL statement is null");
             return;
         }
         synchronized(updateLock) {
             String hash = getHash(epl);
             if (queryActivityMap == null) {
                 queryActivityMap = new ConcurrentHashMap<String, QueryObject>();
                 current = new EPLQueryObject(epl);
                 queryActivityMap.put(hash, current);
             } else if (!(hash.equals(getHash(current.getQuery())))) {
                 QueryObject qo = queryActivityMap.get(hash);
                 if (qo != null) {
                     current = qo;
                 } else {
                     current = new EPLQueryObject(epl);
                     queryActivityMap.put(hash, current);
                 }
             }
         }
     }
 
     /**
      * Increments the times the EPL Query has been used.
      */
     public void incrementCount() {
         if (current != null) {
             // The incrementing of current is already supporting
             // concurrency, and further synchronization is not
             // needed.
             current.incrementCount();
         } else {
             log.error("No Query Object Associated.");
         }
     }
 
     /**
      * Removes the EPL Query.
      *
      * @param epl EPL query.
      */
     public void remove(String epl) {
         if (queryActivityMap == null) {
             return;
         }
         synchronized(updateLock) {
             String hash = getHash(epl);
             QueryObject qo = queryActivityMap.get(hash);
             if (qo != current) {
                 queryActivityMap.remove(hash);
             }
         }
     }
 
     /**
      * Resets counters
      */
     public void reset() {
         if (queryActivityMap == null) {
             return;
         }
         synchronized(updateLock) {
             Iterator it = queryActivityMap.keySet().iterator();
             while (it.hasNext()) {
                 Object key = it.next();
                 QueryObject qo = queryActivityMap.get(key);
                 if (qo != null) {
                     qo.reset();
                 } else {
                     log.error("Invalid Query Object");
                 }
             }
         }
     }
 
     /**
      * Contains an implementation of the {@link QueryObject}
      * interface. This type of object is capable of storing
      * a query and the number of times it was invoked.
      */
     private class EPLQueryObject implements QueryObject {
 
         /**
          * The EPL Query.
          */
         private String query = null;
 
         /**
          * Number of times the query was used.
          */
         private AtomicInteger count = new AtomicInteger();
 
         /**
          * Constructor accepting EPL Query.
          *
          * @param query The EPL Query.
          */
         public EPLQueryObject(String query) {
             this.query = query;
         }
 
         /**
          * Retrieves the EPL Query.
          *
          * @return The EPL Query.
          */
         public String getQuery() {
             return query;
         }
 
         /**
          * Retrieves the number of times the query was used.
          *
          * @return number of times the query was used.
          */
         public int getCount() {
             return count.get();
         }
 
         /**
          * Increments the number of times the query was used by one.
          */ 
         public synchronized void incrementCount() {
             count.incrementAndGet();
         }
 
         /**
          * Resets the counter.
          */
         public synchronized void reset() {
             count.set(0);
         }
     }
 
     /**
      * Method to obtain a hash of an input String.
      *
      * @param  input Input string
      * @return MD5 Hash of input.
      */
     private String getHash(String input) {
         try {
             MessageDigest md = MessageDigest.getInstance("MD5");
             md.update(input.getBytes());
 
             byte[] digest = md.digest();
             if (digest == null) {
                 log.error("An error occured while acquiring Message Digest");
                 return null;
             }
             StringBuffer sbuf = new StringBuffer();
 
             for (int i = 0; i < digest.length; i++) {
                 String hex = Integer.toHexString(0xFF & digest[i]);
 
                 if (hex.length() < 2) {
                     sbuf.append("0");
                 }
                 sbuf.append(hex);
             }
             return sbuf.toString();
         } catch (NoSuchAlgorithmException e) {
             log.error("An error occured while acquiring Message Digest " + e.getMessage());
         }
         return null;
     }
 
     /**
      * Browse the resource and return a representation object.
      *
     * @return the query activity map, of type {@link Map}, 
      * or null if nothing can be returned.
      */
     public Object browse() {
         if (owner == null) {
             return null;
         }
         return (Map<String, QueryObject>)queryActivityMap;
     }
 
     /**
      * Sets the Unique Identifier of the resources parent.
      *
      * @param uid sets Unique Identifier for the resource's parent.
      */
     public void setParentUID(String uid) {
         parentUID = uid;
     }
 
     /**
      * Gets the Unique Identifier of the resource's parent.
      *
      * @return The Unique Identifier for the resource's parent.
      */
     public String getParentUID() {
         return parentUID;
     }
 
     /**
      * Returns the Unique Identifier of the resource.
      *
      * @return the Unique Identifier of the resource.
      */
     public String getUID() {
         if (uid == null) {
             uid = UUIDGenerator.createUUID();
         }
         return uid;
     }
 
     /**
      * Returns the Type of the resource.
      *
      * @return the Type of the resource.
      */
     public String getType() {
         return QueryActivityMonitor.class.getName();
     }
 }
