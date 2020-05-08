 package com.undeadscythes.udsplugin;
 
 import java.util.*;
 
 /**
  * A LinkedHashMap whose contents can be matched with partials.
  * @param <Object>
  * @author UndeadScythes
  */
 public class MatchableHashMap<Object> extends HashMap<String, Object> {
     /**
      * Find all matches for a given partial key.
      * @param partial Partial key to search the map with.
      * @return A list of objects corresponding to matches of the partial key.
      */
     public List<Object> getKeyMatches(final String partial) {
         final String lowPartial = partial.toLowerCase();
         final ArrayList<Object> matches = new ArrayList<Object>();
         for(Map.Entry<String, Object> entry : super.entrySet()) {
             if(entry.getKey().toLowerCase().contains(lowPartial)) {
                 matches.add(entry.getValue());
             }
         }
         return matches;
     }
 
     /**
      * Finds the first match for a partial key.
      * @param partial Partial key to search the map with.
      * @return The first match or <code>null</code> if there are no matches.
      */
     public Object matchKey(final String partial) {
         final String lowPartial = partial.toLowerCase();
         for(Map.Entry<String, Object> entry : super.entrySet()) {
             if(entry.getKey().toLowerCase().contains(lowPartial)) {
                 return entry.getValue();
             }
         }
         return null;
 
     }
 
     @Override
     public Object put(final String key, final Object object) {
         return super.put(key.toLowerCase(), object);
     }
 
     /**
      * Get an object from the map, using the lower case key.
      * @param key The key to search for.
      * @return The object to which the key relates.
      */
     public Object get(final String key) {
         return super.get(key.toLowerCase());
     }
 
     /**
      * Remove an object from the map, using the lower case key.
      * @param key The key to remove.
      * @return The object the key used to match or <code>null</code> if the key didn't exist.
      */
     public Object remove(final String key) {
         return super.remove(key.toLowerCase());
     }
 
     /**
      * Check if the map contains the key.
      * @param key Key to check.
      * @return <code>true</code> if the map contains the key, <code>false</code> otherwise.
      */
     public boolean containsKey(final String key) {
         return super.containsKey(key.toLowerCase());
     }
 
     /**
      * A shortcut to get a sorted list of the map values.
      * @param comp Comparator to define sort priorities.
      * @return Sorted array of objects.
      */
    public List<Object> getSortedValues(final Comparator<Object> comp) {
         final ArrayList<Object> values = new ArrayList<Object>(this.values());
         Collections.sort(values, comp);
         return values;
     }
 
     /**
      * Replace the key of a value in the map.
      * @param oldKey Old key.
      * @param newKey New key.
      * @param object Object to relate to new key.
      */
     public void replace(final String oldKey, final String newKey, final Object object) {
         remove(oldKey);
         put(newKey, object);
     }
 }
