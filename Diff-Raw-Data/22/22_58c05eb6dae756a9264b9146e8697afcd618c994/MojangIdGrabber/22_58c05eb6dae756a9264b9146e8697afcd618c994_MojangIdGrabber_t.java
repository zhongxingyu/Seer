 package com.censoredsoftware.demigods.engine.helper;
 
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Maps;
 import com.google.common.collect.Sets;
 import com.mojang.api.profiles.HttpProfileRepository;
 import com.mojang.api.profiles.Profile;
 import com.mojang.api.profiles.ProfileCriteria;
 import org.bukkit.OfflinePlayer;
 
 import java.util.Arrays;
 import java.util.Map;
 import java.util.Set;
 
 public class MojangIdGrabber {
     private static final String AGENT = "minecraft";
    private static HttpProfileRepository repository;
    private static Map<String, String> knownUUIDs;
    private static Set<String> fakePlayers;

    public static void load() {
        repository = new HttpProfileRepository();
        knownUUIDs = Maps.newHashMap();
        fakePlayers = Sets.newHashSet();
    }
 
     /**
      * This method requires an OfflinePlayer as an extra step to prevent just passing in random String names.
      *
      * @param player The offline player that we are checking the UUID of.
      * @return The Mojang UUID.
      */
     public static String getUUID(OfflinePlayer player) {
         // Get the player's name.
         String playerName = player.getName();
 
         // Check if we already know this Id, of if the name actually belongs to a player.
         if (knownUUIDs.containsKey(playerName)) return knownUUIDs.get(playerName);
         if (fakePlayers.contains(playerName)) return null;
 
         // Get the Id from Mojang.
         Profile profile = Iterables.getFirst(Arrays.asList(repository.findProfilesByCriteria(new ProfileCriteria(playerName, AGENT))), null);
         if (profile == null) {
             // Add the name to the fake players list.
             fakePlayers.add(playerName);
             return null;
         }
         String id = profile.getId();
 
         // Put the player in the known Ids map, and return the found Id.
         knownUUIDs.put(playerName, id);
         return id;
     }
 }
