 package com.ACStache.ArenaGodPlus.PluginListeners;
 
 import java.util.HashSet;
 
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerMoveEvent;
 
 import com.ACStache.ArenaGodPlus.AGPArenaChecker;
 import com.ACStache.ArenaGodPlus.AGPSetter;
 
 public class WarListener implements Listener
 {
    private HashSet<String> warPlayers;
 
     @EventHandler(ignoreCancelled = true)
     public void onPlayerMove(PlayerMoveEvent event)
     {
         Player player = event.getPlayer();
         if(!AGPArenaChecker.isPlayerInWarRegion(player) && !warPlayers.contains(player.getName()))
         {
             return;
         }
         else if(!AGPArenaChecker.isPlayerInWarRegion(player) && warPlayers.contains(player.getName()))
         {
             if(AGPSetter.isGod(player))
             {
                 warPlayers.remove(player.getName());
             }
             else
             {
                 AGPSetter.setGod(player);
                 warPlayers.remove(player.getName());
             }
         }
         else
         {
             if(AGPSetter.isGod(player))
             {
                 AGPSetter.setGod(player);
                 warPlayers.add(player.getName());
             }
             else
             {
                 return;
             }
         }
     }
 }
