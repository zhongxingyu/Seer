 package me.kevin.uhc.listeners;
 
 import me.kevin.uhc.UltraHardcore;
 import org.bukkit.Material;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.*;
 import org.bukkit.event.player.*;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.meta.SkullMeta;
 import org.bukkit.scoreboard.Score;
 import org.bukkit.scoreboard.Team;
 
 import java.util.HashMap;
 
 public class PlayerListener implements Listener {
     UltraHardcore main;
     HashMap<OfflinePlayer, OfflinePlayer> damageCounter = new HashMap<OfflinePlayer, OfflinePlayer>();
     public PlayerListener(UltraHardcore main) {
         this.main = main;
     }
 
     @EventHandler
     public void onPlayerJoin(PlayerJoinEvent event) {
         event.getPlayer().setScoreboard(main.scoreboard);
         if (main.ingame) {
             if (main.getTeamByPlayer(event.getPlayer()) == null) {
                 for (Player player : main.getServer().getOnlinePlayers()) {
                     if (player != event.getPlayer()) {
                         player.hidePlayer(event.getPlayer());
                     }
                 }
             }
         }
     }
 
     @EventHandler
     public void onPlayerDeath(PlayerDeathEvent event) {
         if (main.ingame) {
             Team team = main.getTeamByPlayer(event.getEntity());
             if (team != null) {
                 team.removePlayer(event.getEntity());
             }
             OfflinePlayer oPlayer = damageCounter.get(event.getEntity());
             if (oPlayer != null && oPlayer.isOnline()) {
                 Team point = main.getTeamByPlayer(oPlayer.getPlayer());
                 if (team != null) {
                     Score score = main.score.getScore(main.getServer().getOfflinePlayer(point.getName()));
                     score.setScore(score.getScore() + 1);
                 }
             }
            if (oPlayer != null) {
                ItemStack head = new ItemStack(Material.SKULL_ITEM, 1, (short)3);
                SkullMeta meta = (SkullMeta)head.getItemMeta();
                meta.setOwner(event.getEntity().getName());
                head.setItemMeta(meta);
                event.getDrops().add(head);
            }
             main.setSpectator(event.getEntity());
         }
     }
 
     @EventHandler
     public void onPlayerInteract(PlayerInteractEvent event) {
         if (main.ingame) {
             if (main.getTeamByPlayer(event.getPlayer()) == null) {
                 event.setCancelled(true);
             }
         } else {
             event.setCancelled(true);
         }
     }
 
     @EventHandler
     public void onPlayerPickupItem(PlayerPickupItemEvent event) {
         if (main.getTeamByPlayer(event.getPlayer()) == null) {
             event.setCancelled(true);
         }
     }
 
     @EventHandler
     public void onPlayerDropItem(PlayerDropItemEvent event) {
         if (main.getTeamByPlayer(event.getPlayer()) == null) {
             event.setCancelled(true);
         }
     }
 
     @EventHandler
     public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
         if (main.ingame) {
             if (main.getTeamByPlayer(event.getPlayer()) == null) {
                 event.setCancelled(true);
             }
         } else {
             event.setCancelled(true);
         }
     }
 }
