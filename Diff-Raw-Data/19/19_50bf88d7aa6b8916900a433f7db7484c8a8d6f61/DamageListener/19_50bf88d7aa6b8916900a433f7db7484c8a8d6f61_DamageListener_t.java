 package net.erbros.PvPTime;
 
 import org.bukkit.entity.Player;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityListener;
 
 public class DamageListener extends EntityListener {
     private PvPTime plugin;
     
     DamageListener(PvPTime instance) {
     	plugin = instance;
     }
     
     @Override
     public void onEntityDamage(EntityDamageEvent event) {
 
             if(event instanceof EntityDamageByEntityEvent) {
                 EntityDamageByEntityEvent entEvent = (EntityDamageByEntityEvent)event;
 
                 // check if both are players. thanks LRFLEW :)
                 if(entEvent.getDamager() instanceof Player && entEvent.getEntity() instanceof Player) {
                     // Do we have a player with pvp override?
                 Player attacker = (Player) entEvent.getDamager();
                if((Boolean) plugin.getValue(plugin.pvpWorlds, attacker.getWorld().getName(), "overrideEnabled") == true) {
                     if(attacker.hasPermission("pvptime.override")) {
                             return;
                     }
                 }
 
                 if(isItPvPTime(attacker.getWorld().getName()) == false) {
                     event.setCancelled(true);
                 }
                 return;
             }
         }
     }
 
     public boolean isItPvPTime(String world) {
         long startTime = Long.parseLong(plugin.getValue(plugin.pvpWorlds, world, "startTime").toString());
         long endTime = Long.parseLong(plugin.getValue(plugin.pvpWorlds, world, "endTime").toString());
         if(startTime < endTime) {
             if( plugin.getServer().getWorld(world).getTime() > startTime && plugin.getServer().getWorld(world).getTime() < endTime) {
                     return true;
             } else {
                     return false;
             }
     } else {
             if(plugin.getServer().getWorld(world).getTime() > startTime || plugin.getServer().getWorld(world).getTime() < endTime) {
                     return true;
             } else {
                     return false;
             }
         }
     }
 }
