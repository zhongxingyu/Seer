 package com.ACStache.RangedWolves;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Set;
 
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Wolf;
 
 import com.garbagemule.MobArena.Arena;
 
 public class RWOwner
 {
     private static HashMap<Arena,HashMap<String,Set<Wolf>>> arenaMap = new HashMap<Arena,HashMap<String,Set<Wolf>>>();
     private static HashMap<String,Set<Wolf>> arenaWolfMap = new HashMap<String,Set<Wolf>>();
     private static HashMap<String,Set<Wolf>> tamedWolfMap = new HashMap<String,Set<Wolf>>();
     
     /**
      * method to add wolves from onTameEntity and onCreatureSpawn events
      * @param player the player who gets the wolf
      * @param wolf the wolf in question
      */
     public static void addWolf(String playerName, Wolf wolf)
     {
         if(tamedWolfMap.get(playerName) == null)
         {
             tamedWolfMap.put(playerName, new HashSet<Wolf>());
             if(!tamedWolfMap.get(playerName).contains(wolf))
             {
                 tamedWolfMap.get(playerName).add(wolf);
                 if(RWDebug.getDebug())
                     System.out.println("Wolf " + wolf.getEntityId() + " added to Player " + playerName);
             }
         }
         else
         {
             if(!tamedWolfMap.get(playerName).contains(wolf))
             {
                 tamedWolfMap.get(playerName).add(wolf);
                 if(RWDebug.getDebug())
                     System.out.println("Wolf " + wolf.getEntityId() + " added to Player " + playerName);
             }
         }
     }
     
     /**
      * method to add wolves spawned using Mob Arena
      * @param arena the arena the player is in
      * @param player the player who picked a pet class
      * @param wolf the wolf that got spawned
      */
     public static void addWolf(Arena arena, String playerName, Wolf wolf)
     {
         if(arenaMap.get(arena) == null) //arena not found
         {
             if(arenaWolfMap.get(playerName) == null) //player not found in the arena (always true on just making a new arena key)
             {
                 arenaWolfMap.put(playerName, new HashSet<Wolf>());
                 arenaMap.put(arena, arenaWolfMap);
                 if(!arenaWolfMap.get(playerName).contains(wolf))
                 {
                     arenaWolfMap.get(playerName).add(wolf);
                 }
             }
         }
         else
         {
             if(arenaWolfMap.get(playerName) == null) //player not found in the arena
             {
                 arenaWolfMap.put(playerName, new HashSet<Wolf>());
                 if(!arenaWolfMap.get(playerName).contains(wolf))
                 {
                     arenaWolfMap.get(playerName).add(wolf);
                 }
             }
             else
             {
                 if(!arenaWolfMap.get(playerName).contains(wolf))
                 {
                     arenaWolfMap.get(playerName).add(wolf);
                 }
             }
         }
     }
     
     /**
      * method to remove a wolf from a player's list of wolves if it dies
      * @param player the player who just lost a pet
      * @param wolf the wolf that just died
      */
     public static void removeWolf(String playerName, Wolf wolf)
     {
         if(tamedWolfMap.get(playerName).contains(wolf))
         {
             tamedWolfMap.get(playerName).remove(wolf);
             if(RWDebug.getDebug())
                 System.out.println("Wolf " + wolf.getEntityId() + " removed from Player " + playerName);
         }
     }
     
     /**
      * checks a wolf versus all wolves that have been attached to a player
      * @param wolf the wolf being checked
      * @return true or false
      */
     public static boolean checkArenaWolf(Wolf wolf)
     {
         for(Set<Wolf> set : arenaWolfMap.values()) //for each set of wolves
             if(set.contains(wolf)) //if the set contains the wolf
                 return true; 
         return false;
     }
     
     /**
      * checks a wolf versus all wolves that have been attached to a player
      * @param wolf the wolf being checked
      * @return true or false
      */
     public static boolean checkWorldWolf(Wolf wolf)
     {
         for(Set<Wolf> set : tamedWolfMap.values()) //for each set of wolves
             if(set.contains(wolf)) //if the set contains the wolf
                 return true; 
         return false;
     }
     
     /**
      * returns the list of pets of the player
      * @param player the owner of the pets
      * @return the list of pets of the player
      */
     public static Set<Wolf> getPets(Player player)
     {
         if(RWArenaChecker.isPlayerInArena(player)) //if player is in an arena match
             return arenaWolfMap.get(player.getName());
         else //if player is NOT in an arena match
             return tamedWolfMap.get(player.getName());
     }
     
     /**
      * clears all players/wolves in the arena
      * @param arena the arena that just ended
      */
     public static void clearWolves(Arena arena)
     {
        arenaMap.get(arena).clear();
     }
 }
