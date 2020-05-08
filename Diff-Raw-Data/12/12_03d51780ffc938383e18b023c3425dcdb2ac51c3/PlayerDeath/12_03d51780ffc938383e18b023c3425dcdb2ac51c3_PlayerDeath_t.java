 package com.undeadscythes.udsplugin.eventhandlers;
 
 import com.undeadscythes.udsplugin.*;
 import com.undeadscythes.udsplugin.utilities.*;
 import java.util.*;
 import org.bukkit.*;
 import org.bukkit.event.*;
 import org.bukkit.event.entity.*;
 import org.bukkit.inventory.*;
 import org.bukkit.inventory.meta.*;
 
 /**
  * Fired when a player dies.
  * @author UndeadScythes
  */
 public class PlayerDeath extends ListenerWrapper implements Listener {
     @EventHandler
     public final void onEvent(final PlayerDeathEvent event) {
         final SaveablePlayer victim = PlayerUtils.getOnlinePlayer(event.getEntity().getName());
         final String victimName = victim.getName();
         event.setDeathMessage(event.getDeathMessage().replace(victimName, victim.getNick()));
         if(victim.hasPermission(Perm.BACK_ON_DEATH)) {
             victim.setBackPoint(victim.getLocation());
         }
         if(victim.getKiller() != null) {
             final SaveablePlayer killer = PlayerUtils.getOnlinePlayer(victim.getKiller().getName());
             if(killer != null) {
                 pvp(killer, victim);
             }
         }
         dropItems(victim);
         PlayerUtils.saveInventory(victim);
         event.getDrops().clear();
         event.setNewTotalExp(9 * victim.getTotalExp() / 10);
         event.setDroppedExp(victim.getTotalExp() / 10);
     }
     
     private void clanKill(final SaveablePlayer killer, final SaveablePlayer victim) {
         final Clan victimClan = PlayerUtils.getPlayer(victim.getName()).getClan();
         final Clan killerClan = PlayerUtils.getPlayer(killer.getName()).getClan();
         if(!killerClan.getName().equals(victimClan.getName())) {
             killerClan.newKill();
         }
         victimClan.newDeath();
     }
     
     private void pvp(final SaveablePlayer killer, final SaveablePlayer victim) {
         if(victim.getBounty() > 0) {
                     bountyKill(killer, victim);
         }
         if(killer.isInClan() && victim.isInClan()) {
             clanKill(killer, victim);
         }
         dropHead(victim);
        killer.addKill();
     }
    
     private void dropItems(final SaveablePlayer victim) {
         final Random rng = new Random();
         if(victim.getKills() > 24 && rng.nextDouble() < 0.99) {
             dropItem(victim);
         }
         if(victim.getKills() > 14 && rng.nextDouble() < 0.9) {
             dropItem(victim);
         }
         if(victim.getKills() > 9 && rng.nextDouble() < 0.5) {
             dropItem(victim);
         }
         if(victim.getKills() > 4 && rng.nextDouble() < 0.2) {
             dropItem(victim);
         }
         if(victim.getKills() > 2 && rng.nextDouble() < 0.1) {
             dropItem(victim);
         }
         if(victim.getKills() > 0 && rng.nextDouble() < 0.05) {
             dropItem(victim);
         }
         if(rng.nextDouble() < 0.01) {
             dropItem(victim);
         }
     }
         
     private void dropItem(final SaveablePlayer victim) {
         final Random rng = new Random();
         ItemStack drop = victim.getInventory().getItem(rng.nextInt(36));
         if(drop != null) {
             victim.getWorld().dropItemNaturally(victim.getLocation(), drop.clone());
             drop.setType(Material.AIR);
         }
     }
     
     private void dropHead(final SaveablePlayer victim) {
         final Random rng = new Random();
         if(rng.nextDouble() < UDSPlugin.getConfigDouble(ConfigRef.SKULL) || victim.hasPermission(Perm.HEADDROP)) {
             final ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short)SkullType.PLAYER.ordinal());
             final SkullMeta meta = (SkullMeta)skull.getItemMeta();
             meta.setOwner(victim.getName());
             meta.setDisplayName(victim.getName() + "'s head");
             skull.setItemMeta(meta);
             victim.getWorld().dropItemNaturally(victim.getLocation(), skull);
         }
     }
 
     private void bountyKill(final SaveablePlayer killer, final SaveablePlayer victim) {
         final int total = victim.getBounty();
         UDSPlugin.sendBroadcast(killer.getNick() + " collected the " + total + " " + UDSPlugin.getConfigString(ConfigRef.CURRENCY) + " bounty on " + victim.getNick() + ".");
         killer.credit(total);
         victim.setBounty(0);
     }
 }
