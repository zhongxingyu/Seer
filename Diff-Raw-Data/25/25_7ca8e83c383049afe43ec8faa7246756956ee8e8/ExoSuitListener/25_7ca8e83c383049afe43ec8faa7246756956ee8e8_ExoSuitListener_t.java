 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package net.celestiusmc.exosuit;
 
 import java.util.HashSet;
 import java.util.Set;
 import org.bukkit.Material;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.player.PlayerKickEvent;
 import org.bukkit.event.player.PlayerMoveEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
 import org.bukkit.util.Vector;
 
 /**
  * ExoSuit listener.
  */
 public class ExoSuitListener implements Listener {
     private Set<Player> jumping = new HashSet<Player>();
 
     @EventHandler
     public void onPlayerMove(PlayerMoveEvent event) {
         Player player = event.getPlayer();
 
         if (event.getTo().getY() <= event.getFrom().getY()) {
             jumping.remove(player);
         }
 
        ItemStack boots = event.getPlayer().getInventory().getBoots();
         if ((event.getTo().getY() > event.getFrom().getY())
                 && player.hasPermission("exosuit.helmet")
                && boots != null
                && boots.getType().equals(Material.DIAMOND_BOOTS)) {
             double factor = 8.0;
 
             if (!jumping.contains(player) && (factor != 0.0D)) {
                 Vector newDirection = player.getVelocity();
                 newDirection.multiply(-factor);
                 player.setVelocity(newDirection);
 
                 jumping.add(player);
             }
         }
     }
 
     @EventHandler
     public void onPlayerQuit(PlayerQuitEvent event) {
         jumping.remove(event.getPlayer());
     }
 
     @EventHandler
     public void onPlayerKick(PlayerKickEvent event) {
         jumping.remove(event.getPlayer());
     }
 
     @EventHandler
     public void onEntityDamage(EntityDamageEvent event) {
         Entity entity = event.getEntity();
 
         if (!(entity instanceof Player)) {
             return;
         }
 
         Player player = (Player) entity;
 
         //Helmet check
        ItemStack helmet = player.getInventory().getHelmet();
         if (player.hasPermission("exosuit.helmet")
                 && event.getCause().equals(EntityDamageEvent.DamageCause.DROWNING)
                && helmet != null
                && helmet.getType().equals(Material.DIAMOND_HELMET)) {
             event.setDamage(0);
         }
 
         //Chestplate check
        ItemStack chestplate = player.getInventory().getChestplate();
         if (player.hasPermission("exosuit.chestplate")
                 && (event.getCause().equals(EntityDamageEvent.DamageCause.FIRE)
                 || event.getCause().equals(EntityDamageEvent.DamageCause.FIRE_TICK))
                && chestplate != null
                && chestplate.getType().equals(Material.DIAMOND_CHESTPLATE)) {
             event.setDamage(0);
         }
 
         //Leggings check
        ItemStack leggings = player.getInventory().getLeggings();
         if (player.hasPermission("exosuit.leggings")
                 && event.getCause().equals(EntityDamageEvent.DamageCause.FALL)
                && leggings != null
                && leggings.getType().equals(Material.DIAMOND_LEGGINGS)) {
             event.setDamage(0);
         }
 
     }
 
 }
