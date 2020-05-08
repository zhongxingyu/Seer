 package com.me.tft_02.assassin.util;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.bukkit.Location;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 
 import com.me.tft_02.assassin.Assassin;
 
 public class Misc {
 
     public static boolean isNPCEntity(Entity entity) {
         return (entity == null || entity.hasMetadata("NPC"));
     }
 
     public static int getSystemTime() {
         return (int) System.currentTimeMillis() / 1000;
     }
 
     /**
      * Determine if two locations are near each other.
      *
      * @param first       The first location
      * @param second      The second location
      * @param maxDistance The max distance apart
      * @return true if the distance between <code>first</code> and <code>second</code> is less than <code>maxDistance</code>, false otherwise
      */
     public static boolean isNear(Location first, Location second, double maxDistance) {
         if (!first.getWorld().equals(second.getWorld())) {
             return false;
         }
 
         return first.distanceSquared(second) < (maxDistance * maxDistance);
 
     }
 
     public static String getStringTimeLeft(Player player) {
         long time = PlayerData.getActiveTimeLeft(player);
         int hours = (int) time / 3600;
         int remainder = (int) time - hours * 3600;
         int mins = remainder / 60;
         remainder = remainder - mins * 60;
         int secs = remainder;
         if (mins == 0 && hours == 0) {
             return secs + "s";
         }
         if (hours == 0) {
             return mins + "m " + secs + "s";
         }
         else {
             return hours + "h " + mins + "m " + secs + "s";
         }
     }
 
     public static <K, V extends Comparable<? super V>> List<Entry<K, V>> entriesSortedByValues(Map<K, V> map) {
 
         List<Entry<K, V>> sortedEntries = new ArrayList<Entry<K, V>>(map.entrySet());
 
         Collections.sort(sortedEntries, new Comparator<Entry<K, V>>() {
             @Override
             public int compare(Entry<K, V> e1, Entry<K, V> e2) {
                 return e2.getValue().compareTo(e1.getValue());
             }
         });
 
         return sortedEntries;
     }
     /**
      * Attempts to match any player names with the given name, and returns a list of all possibly matches.
      * <p/>
      * This list is not sorted in any particular order.
      * If an exact match is found, the returned list will only contain a single result.
      *
      * @param partialName Name to match
      * @return List of all possible names
      */
     public static List<String> matchPlayer(String partialName) {
         List<String> matchedPlayers = new ArrayList<String>();
 
         for (OfflinePlayer offlinePlayer : Assassin.p.getServer().getOfflinePlayers()) {
             String iterPlayerName = offlinePlayer.getName();
 
             if (partialName.equalsIgnoreCase(iterPlayerName)) {
                 // Exact match
                 matchedPlayers.clear();
                 matchedPlayers.add(iterPlayerName);
                 break;
             }
             if (iterPlayerName.toLowerCase().contains(partialName.toLowerCase())) {
                 // Partial match
                 matchedPlayers.add(iterPlayerName);
             }
         }
 
         return matchedPlayers;
     }
 
     /**
      * Get a matched player name if one was found in the database.
      *
      * @param partialName Name to match
      * @return Matched name or {@code partialName} if no match was found
      */
     public static String getMatchedPlayerName(String partialName) {
         List<String> matches = matchPlayer(partialName);
 
         if (matches.size() == 1) {
             partialName = matches.get(0);
         }
         return partialName;
     }
 }
