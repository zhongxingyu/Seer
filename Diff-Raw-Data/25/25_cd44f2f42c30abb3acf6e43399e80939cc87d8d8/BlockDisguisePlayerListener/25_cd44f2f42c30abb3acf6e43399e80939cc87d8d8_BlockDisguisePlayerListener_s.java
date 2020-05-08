 package com.ne0nx3r0.blockdisguise.listeners;
 
 import com.ne0nx3r0.blockdisguise.BlockDisguise;
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Arrow;
 import org.bukkit.entity.Egg;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Fireball;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.SmallFireball;
 import org.bukkit.entity.Snowball;
 import org.bukkit.entity.ThrownPotion;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerKickEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 
 public class BlockDisguisePlayerListener implements Listener
 {
     private final BlockDisguise plugin;
 
     public BlockDisguisePlayerListener(BlockDisguise plugin)
     {
         this.plugin = plugin;
     }
     
     @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
     public void onPlayerJoin(PlayerJoinEvent e)
     {
         plugin.disguiseManager.hideDisguisedPlayersFrom(e.getPlayer());
         
         if(plugin.UPDATE_AVAILABLE
         && (e.getPlayer().hasPermission("BlockDisguise.admin")))
         {
             e.getPlayer().sendMessage(ChatColor.DARK_RED + "[BD] " 
                 + ChatColor.WHITE + "An update is available: "+plugin.UPDATE_NAME);
         }
     }
     
     @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
     public void onPlayerQuit(PlayerQuitEvent e)
     {
         plugin.disguiseManager.undisguise(e.getPlayer());
     }
     
     @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
     public void onEntityDamageByEntity(EntityDamageByEntityEvent e)
     {
         if(plugin.UNDISGUISE_ON_PVP)
         {
             Entity eAttacker = e.getDamager();
 
             if(!(eAttacker instanceof Player))
             {
                 if(eAttacker instanceof Arrow)
                 {
                     eAttacker = ((Arrow) eAttacker).getShooter();
                 }
                 else if(eAttacker instanceof ThrownPotion)
                 {
                     eAttacker = ((ThrownPotion) eAttacker).getShooter();
                 }
                 else if(eAttacker instanceof Snowball)
                 {
                     eAttacker = ((Snowball) eAttacker).getShooter();
                 }
                 else if(eAttacker instanceof Egg)
                 {
                     eAttacker = ((Egg) eAttacker).getShooter();
                 }
                 else if(eAttacker instanceof Fireball)
                 {
                     eAttacker = ((Fireball) eAttacker).getShooter();
                 }
                 else if(eAttacker instanceof SmallFireball)
                 {
                     eAttacker = ((SmallFireball) eAttacker).getShooter();
                 }
             }
 
             if(eAttacker instanceof Player)
             {
                 plugin.disguiseManager.undisguise((Player) eAttacker);
             }
         }
     }

     @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
     public void onPlayerKicked(PlayerKickEvent e)
     {
        if(e.getReason().equals(e.getPlayer().getName()+" was kicked for floating too long!"))
         {
            if(plugin.disguiseManager.isDisguisedPlayer(e.getPlayer().getLocation().subtract(0,1,0).getBlock()))
            {
                System.out.println("cancelling flying kick, player standing on BD block");
                e.setCancelled(true);
            }
         }
    }
 }
