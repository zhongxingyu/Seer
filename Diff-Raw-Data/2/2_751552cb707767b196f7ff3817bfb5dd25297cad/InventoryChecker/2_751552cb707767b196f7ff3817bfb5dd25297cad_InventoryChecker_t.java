 package com.herocraftonline.dev.heroes.inventory;
 
 import org.bukkit.Bukkit;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.PlayerInventory;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.classes.HeroClass;
 import com.herocraftonline.dev.heroes.classes.HeroClass.WeaponItems;
 import com.herocraftonline.dev.heroes.persistence.Hero;
 import com.herocraftonline.dev.heroes.util.MaterialUtil;
 import com.herocraftonline.dev.heroes.util.Messaging;
 
 public class InventoryChecker {
 
     private Heroes plugin;
     private boolean allowHats = false;
 
     public InventoryChecker(Heroes plugin) {
         this.plugin = plugin;
         this.allowHats = plugin.getConfigManager().getProperties().allowHats;
     }
 
     public void checkInventory(Player p) {
         PlayerInventory inv = p.getInventory();
         Hero h = plugin.getHeroManager().getHero(p);
         HeroClass hc = h.getHeroClass();
         int removedCount = 0;
         int count = 0;
         String item;
         if (inv.getHelmet() != null && inv.getHelmet().getTypeId() != 0 && !allowHats) {
             item = inv.getHelmet().getType().toString();
             if (!hc.getAllowedArmor().contains(item) && !hc.getAllowedArmor().contains("*")) {
                 if (moveItem(p, -1, inv.getHelmet())) {
                     removedCount++;
                 }
                 inv.setHelmet(null);
                 count++;
             }
         }
         if (inv.getChestplate() != null && inv.getChestplate().getTypeId() != 0) {
             item = inv.getChestplate().getType().toString();
             if (!hc.getAllowedArmor().contains(item) && !hc.getAllowedArmor().contains("*")) {
                 if (moveItem(p, -1, inv.getChestplate())) {
                     removedCount++;
                 }
                 inv.setChestplate(null);
                 count++;
             }
         }
         if (inv.getLeggings() != null && inv.getLeggings().getTypeId() != 0) {
             item = inv.getLeggings().getType().toString();
             if (!hc.getAllowedArmor().contains(item) && !hc.getAllowedArmor().contains("*")) {
                 if (moveItem(p, -1, inv.getLeggings())) {
                     removedCount++;
                 }
                 inv.setLeggings(null);
                 count++;
             }
         }
         if (inv.getBoots() != null && inv.getBoots().getTypeId() != 0) {
             item = inv.getBoots().getType().toString();
             if (!hc.getAllowedArmor().contains(item) && !hc.getAllowedArmor().contains("*")) {
                 if (moveItem(p, -1, inv.getBoots())) {
                     removedCount++;
                 }
                 inv.setBoots(null);
                 count++;
             }
         }
         for (int i = 0; i < 9; i++) {
             ItemStack itemStack = inv.getItem(i);
             String itemType = itemStack.getType().toString();
 
             // Perform a check to see if what we have is a Weapon.
             if (!itemType.equalsIgnoreCase("BOW")) {
                 try {
                     WeaponItems.valueOf(itemType.substring(itemType.indexOf("_") + 1, itemType.length()));
                 } catch (IllegalArgumentException e1) {
                     continue;
                 }
             }
 
            if (!hc.getAllowedWeapons().contains(itemType) && !hc.getAllowedWeapons().contains("*")) {
                 if (moveItem(p, i, itemStack)) {
                     removedCount++;
                 }
                 count++;
             }
         }
         // If items were removed from the Players inventory then we need to alert them of such event.
         if (removedCount > 0) {
             Messaging.send(p, "$1 have been removed from your inventory due to class restrictions.", removedCount + " Items");
             Messaging.send(p, "Please make space in your inventory then type '$1'", "/heroes recoveritems");
         }
         // If any items were removed or moved in the inventory then we need to make sure the Client is in Sync.
         if (count > 0) {
             syncInventory(p);
         }
     }
 
     /**
      * Check the given Players inventory for any Armor or Weapons which are restricted.
      *
      * @param name
      */
     public void checkInventory(String name) {
         Player player = Bukkit.getServer().getPlayer(name);
         if (player != null) {
             checkInventory(player);
         }
     }
 
     /**
      * Grab the first empty INVENTORY SLOT, skips the Hotbar.
      *
      * @param p
      * @return
      */
     public int firstEmpty(Player p) {
         ItemStack[] inventory = p.getInventory().getContents();
         for (int i = 9; i < inventory.length; i++) {
             if (inventory[i] == null) {
                 return i;
             }
         }
         return -1;
     }
 
     /**
      * Move the selected Item to an available slot, if a slot does not exist then we remove it from the inventory.
      *
      * @param p
      * @param slot
      * @param item
      * @return
      */
     public boolean moveItem(Player p, int slot, ItemStack item) {
         PlayerInventory inv = p.getInventory();
         Hero h = plugin.getHeroManager().getHero(p);
         int empty = firstEmpty(p);
         if (empty == -1) {
             h.addRecoveryItem(item);
             if (slot != -1) {
                 inv.setItem(slot, null);
             }
             return true;
         } else {
             inv.setItem(empty, item);
             if (slot != -1) {
                 inv.setItem(slot, null);
             }
             Messaging.send(p, "You are not trained to use a $1.", MaterialUtil.getFriendlyName(item.getType()));
             return false;
         }
     }
 
     /**
      * Synchronize the Clients Inventory with the Server. This is dealt during a scheduler so it happens after ANY
      * changes are made.
      * Synchronizing during changes often results in the client losing Sync.
      *
      * @param player
      */
     public void syncInventory(final Player player) {
         plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
 
             @SuppressWarnings("deprecation")
             public void run() {
                 player.updateInventory();
             }
         });
     }
 }
