 package com.github.omwah.SDFEconomy.location;
 
 import java.util.Queue;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentLinkedQueue;
 import java.util.concurrent.Semaphore;
 import org.bukkit.Location;
 import org.bukkit.Server;
 
 /**
  * Adds in the functionality for an external event to add locations for 
  * players based on additional information.
  *
  * The class queues destinations per player and uses them in order.
  */
 public abstract class SetDestinationLocationTranslator extends PlayerLocationTranslator {
     
     private final ConcurrentHashMap<String, ConcurrentLinkedQueue<Location>> destinations;
     
     public SetDestinationLocationTranslator(Server server) {
         super(server);
 
         this.destinations = new ConcurrentHashMap<String, ConcurrentLinkedQueue<Location>>();
     }
     
     /*
      * Adds a destination to use for a named player for subsequent
      * transactions 
      */
     public void addDestination(String playerName, Location destLocation) {
         destinations.putIfAbsent(playerName, new ConcurrentLinkedQueue<Location>());
 
         destinations.get(playerName).add(destLocation);
     }
     
     /*
      * Removes a destination and associate locks
      */    
     public void removeDestination(String playerName, Location destLocation) {
         Queue locations = destinations.get(playerName);
         locations.remove(destLocation);

         if (locations.isEmpty()) {
             destinations.remove(playerName);
         }
     }
     
     /*
      * Retrieves a set location if present, otherwise returns what
      * the super class computes.
      */
     @Override
     public String getLocationName(String playerName) {
         if(destinations.containsKey(playerName)) {
             return getLocationName(destinations.get(playerName).peek());
         } else {
             return super.getLocationName(playerName);
         }
     }
     
     public abstract String getLocationName(Location location);
 }
