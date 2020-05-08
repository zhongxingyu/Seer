 package com.minecarts.barrenschat.cache;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import org.bukkit.entity.Player;
 
 //Caches who is ignoring whom
 
 public class CacheIgnore extends CacheBase{
     private static HashMap<String, List<String>> ignoreList = new HashMap<String, List<String>>();
     public static List<String> getIgnoreList(Player player) {
        return ignoreList.get(player);
     }
     public static boolean isIgnoring(Player player, Player ignore) {
         if (!ignoreList.containsKey(player.getName())) {
             ignoreList.put(player.getName(), plugin.dbHelper.getIgnoreList(player)); //Add it to the DB
         }
         return ignoreList.get(player.getName()).contains(ignore.getName());
     }
     public static void addIgnore(Player player, Player ignore) {
         verifyPlayerIsInCache(player);
         ignoreList.get(player.getName()).add(ignore.getName());
     }
     public static void removeIgnore(Player player, Player ignore) {
         verifyPlayerIsInCache(player);
         ignoreList.get(player.getName()).remove(ignore.getName());
     }
     public static void loadListFromDB(Player player){
         ignoreList.put(player.getName(), plugin.dbHelper.getIgnoreList(player));
     }
     private static void verifyPlayerIsInCache(Player player){
         if (ignoreList.get(player.getName()) == null) {
             ignoreList.put(player.getName(), new ArrayList<String>());
         }
     }
 }
