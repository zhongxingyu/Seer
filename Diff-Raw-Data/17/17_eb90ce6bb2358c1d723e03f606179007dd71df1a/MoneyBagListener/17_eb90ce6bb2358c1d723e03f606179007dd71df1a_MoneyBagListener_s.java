 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package me.xhawk87.Coinage.listeners;
 
 import me.xhawk87.Coinage.Coinage;
 import me.xhawk87.Coinage.moneybags.MoneyBag;
 import org.bukkit.event.Event;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockPlaceEvent;
 import org.bukkit.event.inventory.CraftItemEvent;
 import org.bukkit.event.inventory.InventoryClickEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.scheduler.BukkitRunnable;
 
 /**
  *
  * @author XHawk87
  */
 public class MoneyBagListener implements Listener {
 
     private Coinage plugin;
 
     public void registerEvents(Coinage plugin) {
         this.plugin = plugin;
         plugin.getServer().getPluginManager().registerEvents(this, plugin);
     }
 
     @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
     public void onOpenMoneyBag(BlockPlaceEvent event) {
         MoneyBag moneybag = plugin.getMoneyBag(event.getItemInHand());
         if (moneybag != null) {
             event.getPlayer().openInventory(moneybag.getInventory());
             event.setCancelled(true);
         }
     }
 
     @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
     public void onOpenMoneyBag(PlayerInteractEvent event) {
         if (event.hasItem()) {
             switch (event.getAction()) {
                 case RIGHT_CLICK_AIR:
                 case RIGHT_CLICK_BLOCK:
                     MoneyBag moneybag = plugin.getMoneyBag(event.getItem());
                     if (moneybag != null) {
                         event.getPlayer().openInventory(moneybag.getInventory());
                         event.setUseItemInHand(Event.Result.DENY);
                     }
             }
         }
     }
 
     @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
     public void onUpdateMoneyBag(InventoryClickEvent event) {
         if (event.getInventory().getHolder() instanceof MoneyBag) {
             final MoneyBag moneybag = (MoneyBag) event.getInventory().getHolder();
             final Inventory out = event.getView().getBottomInventory();
             new BukkitRunnable() {
                 @Override
                 public void run() {
                     moneybag.checkCoins(out);
                 }
             }.runTask(plugin);
         }
     }
 
     @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
     public void noCraftingWithMoneyBags(CraftItemEvent event) {
         for (ItemStack item : event.getInventory().getMatrix()) {
            if (item == null) {
                 continue;
             }
 
             if (plugin.getMoneyBag(item) != null) {
                 event.setCancelled(true);
                 return;
             }
         }
     }
 }
