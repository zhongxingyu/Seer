 package com.thezomg.zomgctf;
 
 import org.bukkit.ChatColor;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.block.BlockPlaceEvent;
 import org.bukkit.event.entity.EntityDeathEvent;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 
 public class CTFPlayerListener implements Listener {
     private ZomgCTF plugin;
 
     public CTFPlayerListener(ZomgCTF plugin) {
         this.plugin = plugin;
     }
 
     @EventHandler(priority = EventPriority.HIGHEST)
     public void onBlockPlace(BlockPlaceEvent event) {
         if (plugin.flag_set.containsKey(event.getPlayer().getName().toLowerCase())) {
             Team t = plugin.tm.getTeam(plugin.flag_set.get(event.getPlayer().getName().toLowerCase()));
             if (t == null) {
                 // Something went rather wrong here.
                 event.getPlayer().sendMessage(ChatColor.RED + "An error occurred while setting the flag, see the console for more details.");
                 return;
             }
             t.setFlagHome(event.getBlock().getLocation());
             plugin.flag_set.remove(event.getPlayer().getName().toLowerCase());
             event.getPlayer().sendMessage(ChatColor.GREEN + "Successfully set the " + t.getName() + " flag location.");
         } else {
             for (Team t : plugin.tm.getTeams()) {
                 if (event.getBlock().equals(t.getBlockHome())) {
                     event.setCancelled(true);
                     break;
                 }
             }
         }
     }
     
     @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
     public void onPlayerDeath(PlayerDeathEvent event) {
         Player killer = event.getEntity().getKiller();
         Player player = event.getEntity();
         
         // If there is a killer, check to see if they are part of the match
         if (killer != null) {
             Team kt = plugin.tm.getPlayerTeam(killer.getName());
             Team pt = plugin.tm.getPlayerTeam(player.getName());
             
             // If both players have a team, give the killer and their team some points.
            if (kt != null && pt != null) {
                 kt.kill(killer.getName());
             }
         }
         
         // Check to see if the player is a carrier and if they are drop the flag.
         Team pt = plugin.tm.getTeamFromCarrier(player.getName());
         if (pt != null) {
             pt.dropflag();
         }
     }
     
     @EventHandler(priority = EventPriority.HIGHEST)
     public void onInteract(PlayerInteractEvent event) {
         String playerName = event.getPlayer().getName();
         if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
             Block clicked = event.getClickedBlock();
             Team pt = plugin.tm.getPlayerTeam(playerName);
             if (pt != null) {
                 if (clicked.equals(pt.getBlockHome())) {
                     //Clicking their own teams flag.
                     Team ft = plugin.tm.getTeamFromCarrier(playerName);
                     if (ft == null) {
                         // Not a carrier, so stop processing.
                         return;
                     }
                     
                     if (!pt.isFlagHome()) {
                         // Flag is not home, return (this shouldn't happen, but contingencies).
                         return;
                     }
 
                     pt.capture();
                     plugin.getServer().broadcastMessage(ChatColor.BLUE + ft.getCarrier().getDisplayName() +
                             ChatColor.BLUE + " captured the " + ft.getChatColour() + ft.getName() +
                             ChatColor.BLUE + " flag");
                     ft.respawnFlag();
                 }
                 else {
                     // They are not trying to cap, check if they are trying to take another team's flag.
                     for (Team t : plugin.tm.getTeams()) {
                         if (t.equals(pt))
                             continue;
                         if (clicked.equals(t.getBlockHome()) && t.getCarrier() == null) {
                             t.setCarrier(event.getPlayer());
                             plugin.getServer().broadcastMessage(ChatColor.BLUE + event.getPlayer().getDisplayName() +
                                 ChatColor.BLUE + " has taken the " + t.getChatColour() + t.getName() +
                                 ChatColor.BLUE + " flag");
                             break;
                         }
                     }
                 }
             }
         }
     }
 }
