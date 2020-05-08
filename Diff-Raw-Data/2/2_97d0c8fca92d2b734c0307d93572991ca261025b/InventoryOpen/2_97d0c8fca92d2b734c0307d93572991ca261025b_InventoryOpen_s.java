 package com.undeadscythes.udsplugin.eventhandlers;
 
 import com.undeadscythes.udsplugin.Color;
 import com.undeadscythes.udsplugin.*;
 import org.bukkit.*;
 import org.bukkit.block.Chest;
 import org.bukkit.block.*;
 import org.bukkit.block.Sign;
 import org.bukkit.entity.*;
 import org.bukkit.event.*;
 import org.bukkit.event.inventory.*;
 import org.bukkit.inventory.*;
 import org.bukkit.material.*;
 
 /**
  * Fired when a player opens an inventory window.
  * If the inventory is protected and the player does not have access and does
  * not have Perm.BYPASS the action is stopped. If the inventory is a shop then
  * the player is put in 'shopping mode'. If the inventory is the players pack
  * then nothing happens.
  * @author UndeadScythes
  */
 public class InventoryOpen extends ListenerWrapper implements Listener {
     private SaveablePlayer player;
 
     @EventHandler
     public final void onEvent(final InventoryOpenEvent event) {
         final InventoryHolder holder = event.getInventory().getHolder();
         player = UDSPlugin.getOnlinePlayers().get(event.getPlayer().getName());
         if(holder instanceof DoubleChest) {
             if(isShop(((DoubleChest)holder).getLeftSide()) || isShop(((DoubleChest)holder).getRightSide())) {
                 startShopping();
             } else {
                 event.setCancelled(isProtected(((DoubleChest)holder).getLeftSide()) || isProtected(((DoubleChest)holder).getRightSide()));
             }
         } else if(holder instanceof Chest) {
             if(isShop(holder)) {
                 startShopping();
             } else {
                 event.setCancelled(isProtected(holder));
             }
        } else if(!(holder instanceof Player || holder instanceof EnderChest)) {
             event.setCancelled(isProtected(holder));
         }
     }
 
     private void startShopping() {
         player.setShopping(true);
         player.getShoppingList().clear();
     }
 
     private boolean isShop(final InventoryHolder holder) {
         final BlockState block = ((BlockState)holder).getBlock().getRelative(BlockFace.UP).getState();
         if(block.getType().equals(Material.WALL_SIGN)) {
             return isShopSign(((Sign)block).getLines());
         } else {
             return false;
         }
     }
 
     private boolean isProtected(final InventoryHolder holder) {
         if(!player.canBuildHere(((BlockState)holder).getBlock().getLocation())) {
             if(player.hasPermission(Perm.BYPASS)) {
                 player.sendMessage(Color.MESSAGE + "Protection bypassed.");
             } else {
                 player.sendMessage(Color.ERROR + "You do not have access to this block.");
                 return true;
             }
         }
         return false;
     }
 }
