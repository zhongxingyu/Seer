 package com.undeadscythes.udsplugin.eventhandlers;
 
 import com.undeadscythes.udsplugin.*;
 import com.undeadscythes.udsplugin.utilities.*;
 import org.bukkit.*;
 import org.bukkit.event.*;
 import org.bukkit.event.inventory.*;
 import org.bukkit.inventory.*;
 
 /**
  * Fired when a player clicks in an inventory window.
  * If the player is in 'shopping mode' then we edit their shopping list
  * accordingly.
  * 
  * @author UndeadScythes
  */
 public class InventoryClick extends ListenerWrapper implements Listener {
     @SuppressWarnings("deprecation")
     @EventHandler
     public final void onEvent(final InventoryClickEvent event) {
         final SaveablePlayer shopper = PlayerUtils.getOnlinePlayer(event.getWhoClicked().getName());
         if(shopper.isShopping()) {
             final ItemStack item = event.getCursor();
             if(event.isShiftClick()) {
                 event.setResult(Event.Result.DENY);
             } else if(shopper.isBuying() && event.getSlot() == -999) {
                 event.setResult(Event.Result.DENY);
                 event.getInventory().addItem(item);
                 event.setCursor(new ItemStack(Material.AIR));
             } else if(item.getType().equals(Material.AIR)) {
                 if(event.getRawSlot() == event.getSlot()) {
                     shopper.setBuying(true);
                 } else {
                     shopper.setBuying(false);
                 }
             } else if(shopper.isBuying() && event.getRawSlot() != event.getSlot()) {
                 final Inventory cart = shopper.getShoppingList();
                 if(event.isLeftClick()) {
                     cart.addItem(item);
                 } else if(event.isRightClick()) {
                     final ItemStack oneItem = item.clone();
                     oneItem.setAmount(1);
                     cart.addItem(oneItem);
                 }
             } else if(!shopper.isBuying() && event.getRawSlot() == event.getSlot()) {
                 final Inventory cart = shopper.getShoppingList();
                 if(event.isLeftClick()) {
                     cart.removeItem(item);
                 } else if(event.isRightClick()) {
                     final ItemStack oneItem = item.clone();
                     oneItem.setAmount(1);
                     cart.removeItem(oneItem);
                 }
             }
         }
     }
 }
