 package com.hxdcml.sql;
 
 import java.util.AbstractMap;
 import java.util.ArrayList;
 import java.util.Set;
 
 /**
  * User: Souleiman Ayoub
  * Date: 12/30/12
  * Time: 6:02 PM
  */
 public class QueryMap {
     private SQLMap map;
 
     /**
      * Prepares the QueryMap by instantiating the map object.
      */
     public QueryMap() {
         map = new SQLMap();
     }
 
     /**
      * Allow the query to populate the QueryMap if necessary.
      *
      * @param query the AbstractMap that will contain necessary data to be added into the
      *              QueryMap.
      */
     public QueryMap(AbstractMap query) {
         map = new SQLMap();
         for (Object key : query.keySet()) {
             ArrayList values = (ArrayList) query.get(key);
             String value = (String) values.get(0);
             int var = Integer.parseInt(values.get(1).toString().replaceAll("\\.0", ""));
             put((String) key, new QueryNode(value, var));
         }
     }
 
     /**
      * The data inserted, Where Key will indicate what Search Parameter we are going to be
      * looking at, and the QueryNode will contain the information we are to compare it to by.
      *
      * @param key search type
      * @param value the comparison values which indicates exact or exhuastive search.
      */
     public void put(String key, QueryNode value) {
         map.put(key, value);
     }
 
     /**
      * Returns the Set of Key Values.
      *
      * @return a Set that contains Strings of Key Values.
      */
     public Set<String> keySet() {
         return map.keySet();
     }
 
     /**
     * A simple getter method,t hat will access a data in the map.
      *
      * @param key the String key that we want to access in the Map.
      * @return the QueryNode that represents the key value.
      */
     public QueryNode get(String key) {
         return (QueryNode) map.get(key);
     }
 }
