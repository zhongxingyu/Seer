 package com.undeadscythes.udsplugin.eventhandlers;
 
 import com.undeadscythes.udsplugin.*;
 import org.bukkit.*;
 import org.bukkit.event.*;
 import org.bukkit.event.block.*;
 import org.bukkit.inventory.*;
 
 /**
  * Description
  * @author UndeadScythes
  */
 public class BlockBreak implements Listener {
     @EventHandler
     public void onEvent(BlockBreakEvent event) {
         SaveablePlayer player = UDSPlugin.getPlayers().get(event.getPlayer().getName());
         if(player.isJailed()) {
             event.setCancelled(true);
         } else if(!player.canBuildHere(event.getBlock().getLocation().add(UDSPlugin.HALF_BLOCK))) {
             event.setCancelled(true);
             player.sendMessage(Message.CANT_BUILD_HERE);
        } else if(event.getBlock().getType().equals(Material.WALL_SIGN) || event.getBlock().getType().equals(Material.SIGN_POST) && !player.isSneaking()) {
             event.setCancelled(true);
             player.sendMessage(Color.ERROR + "Sneak while punching if you want to break this block.");
         } else if(event.getBlock().getType().equals(Material.MOB_SPAWNER)) {
             player.getWorld().dropItemNaturally(event.getBlock().getLocation().add(UDSPlugin.HALF_BLOCK), new ItemStack(Material.MOB_SPAWNER));
             event.setExpToDrop(Config.SPAWNER_EXP);
         } else if(event.getBlock().getType().equals(Material.SNOW_BLOCK)) {
             for(Region arena : UDSPlugin.getArenas().values()) {
                 if(arena.getData().equals("spleef")) {
                     event.setCancelled(true);
                     event.getBlock().setType(Material.AIR);
                     player.setItemInHand(new ItemStack(player.getItemInHand()));
                     player.updateInventory();
                 }
             }
         }
     }
 }
