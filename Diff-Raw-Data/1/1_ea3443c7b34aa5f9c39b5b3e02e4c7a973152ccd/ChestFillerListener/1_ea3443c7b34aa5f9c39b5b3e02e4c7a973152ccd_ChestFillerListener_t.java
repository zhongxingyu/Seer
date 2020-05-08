 package com.precipicegames.zeryl.chestfiller;
 
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.Chest;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 
 /**
  *
  * @author Zeryl
  */
 public class ChestFillerListener implements Listener {
     
     private ChestFiller plugin = null;
     
     public ChestFillerListener(ChestFiller instance) {
         this.plugin = instance;
     }
 
     @EventHandler
     public void onPlayerInteract(PlayerInteractEvent event) {
         Block block = event.getClickedBlock();
         Player player = event.getPlayer();
 
         if ((block != null) && (block.getType() == Material.CHEST)) {
             if (plugin.items.containsKey(player)) {
 
                 String item = plugin.items.get(player);
                 int itemid = 0;
                 int damage = 0;
 
                 if (item.indexOf(":") > 0) {
                     itemid = Integer.parseInt(item.split(":")[0]);
                     damage = Integer.parseInt(item.split(":")[1]);
                 } else {
                     itemid = Integer.parseInt(item);
                 }
 
                 if (itemid == 0) {
                     player.sendMessage("You cannot fill chests with air, please specify a blocktype first");
                     return;
                 }
 
                 ItemStack is = new ItemStack(itemid, 64);
 
                 is.setDurability((short) damage);
 
                 Boolean found = false;
                 Block partner;
                 Block foundblock = null;
 
                 
                 while(!found) {
                     partner = player.getWorld().getBlockAt(block.getX() + 1, block.getY(), block.getZ());
 
                     if (partner.getType() == Material.CHEST) {
                         foundblock = partner;
                         found = true;
                     }
 
                     partner = player.getWorld().getBlockAt(block.getX() - 1, block.getY(), block.getZ());
 
                     if (partner.getType() == Material.CHEST) {
                         foundblock = partner;
                         found = true;
                     }
 
                     partner = player.getWorld().getBlockAt(block.getX(), block.getY(), block.getZ() + 1);
 
                     if (partner.getType() == Material.CHEST) {
                         foundblock = partner;
                         found = true;
                     }
 
                     partner = player.getWorld().getBlockAt(block.getX(), block.getY(), block.getZ() - 1);
 
                     if (partner.getType() == Material.CHEST) {
                         foundblock = partner;
                         found = true;
                     }
                    break;
                 }
                 
                 int i;
                 
                 Chest chest = (Chest) block.getState();
                 Inventory inventory = chest.getInventory();
                 Inventory inventory2 = null;
                 
                 if(found) {
                     Chest chest2 = (Chest) foundblock.getState();
                     inventory2 = chest2.getInventory();
                     
                 }
                 for(i = 0; i <= 26; i++) {
                     inventory.setItem(i, is);
                     if(found) {
                         inventory2.setItem(i, is);
                     }
                 }
             }
         }
     }
 }
