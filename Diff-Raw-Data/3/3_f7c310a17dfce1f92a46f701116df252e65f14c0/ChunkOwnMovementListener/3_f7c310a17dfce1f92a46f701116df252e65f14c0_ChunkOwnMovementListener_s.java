 package com.codisimus.plugins.chunkown;
 
 import java.util.HashMap;
 import java.util.LinkedList;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerMoveEvent;
 
 /**
  * Regenerate Health/Hunger & Alarm System & Notify When in OwnedChunks
  * 
  * @author Codisimus
  */
 public class ChunkOwnMovementListener implements Listener {
     static long rate;
     private static LinkedList<Player> healing = new LinkedList<Player>();
     private static LinkedList<Player> feeding = new LinkedList<Player>();
     private static HashMap<Player, ChunkOwner> inChunk = new HashMap<Player, ChunkOwner>();
     
     @EventHandler (priority = EventPriority.MONITOR)
     public void onPlayerMove(PlayerMoveEvent event) {
         Player player = event.getPlayer();
         OwnedChunk chunk = ChunkOwn.findOwnedChunk(event.getTo().getBlock());
         if (chunk == null) {
             if (inChunk.containsKey(player))
                 playerLeftChunk(player);
         }
         else {
             ChunkOwner previous = inChunk.get(player);
             if (chunk.owner != previous) {
                 playerLeftChunk(player);
                 playerEnteredChunk(player, chunk);
             }
         }
     }
     
     protected static void playerLeftChunk(Player player) {
         String name = player.getName();
         
         ChunkOwner owner = inChunk.get(player);
        if (owner.alarm)
             owner.sendMessage(name+" left your owned property");
         
         ChunkOwner walker = ChunkOwn.findOwner(name);
         if (walker != null && walker.notify)
             walker.sendMessage("You have left property owned by "+owner.name);
         
         healing.remove(player);
         feeding.remove(player);
         inChunk.remove(player);
     }
     
     protected static void playerEnteredChunk(Player player, OwnedChunk chunk) {
         String name = player.getName();
         
         ChunkOwner owner = inChunk.get(player);
         if (owner.alarm)
             owner.sendMessage(name+" entered your owned property: "+chunk.toString());
         
         ChunkOwner walker = ChunkOwn.findOwner(name);
         if (walker != null && walker.notify)
             walker.sendMessage("You have entered property owned by "+owner.name);
         
         if (chunk.owner.heal)
             healing.add(player);
         if (chunk.owner.feed)
             feeding.add(player);
         inChunk.put(player, chunk.owner);
     }
     
     /**
      * Heals Players who are in healing Chunks
      * 
      */
     public static void scheduleHealer() {
         if (Econ.heal == -2)
             return;
         
         ChunkOwn.server.getScheduler().scheduleSyncRepeatingTask(ChunkOwn.plugin, new Runnable() {
             @Override
     	    public void run() {
                 for (Player player: healing) {
                     int health = player.getHealth();
                     health++;
                     if (health <= player.getMaxHealth())
                         player.setHealth(health);
                 }
     	    }
     	}, 0L, 20L * rate);
     }
     
     /**
      * Feeds Players who are in feeding Chunks
      * 
      */
     public static void scheduleFeeder() {
         if (Econ.feed == -2)
             return;
         
         ChunkOwn.server.getScheduler().scheduleSyncRepeatingTask(ChunkOwn.plugin, new Runnable() {
             @Override
     	    public void run() {
                 for (Player player: feeding) {
                     int hunger = player.getFoodLevel();
                     hunger++;
                     if (hunger <= 20)
                         player.setFoodLevel(hunger);
                 }
     	    }
     	}, 0L, 20L * rate);
     }
 }
