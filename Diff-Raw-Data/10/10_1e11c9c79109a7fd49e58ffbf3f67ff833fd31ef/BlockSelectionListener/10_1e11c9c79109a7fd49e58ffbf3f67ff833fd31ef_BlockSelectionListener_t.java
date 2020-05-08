 /**
  *
  * @author Indivisible0
  */
 package com.github.indiv0.chestempty;
 
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.Chest;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerInteractEvent;
 
 public class BlockSelectionListener implements Listener {
 
     public static ChestEmpty plugin;
 
     public BlockSelectionListener(ChestEmpty instance) {
         plugin = instance;
     }
 
     // Create a method to handle/interact with clicking events.
     @EventHandler
     public void onChestHit(PlayerInteractEvent event) {
        if (event.getAction() != org.bukkit.event.block.Action.LEFT_CLICK_BLOCK)
            return;
 
         // Makes sure the player is currently in selection mode.
        if (!plugin.isSelecting(event.getPlayer().getDisplayName()))
            return;
 
         Block clickedBlock = event.getClickedBlock();
 
         // Makes sure that the clicked block is a chest.
         try {
             if (clickedBlock.getType() != Material.CHEST)
                 return;
         } catch (NullPointerException ex) {
             return;
         }
 
         Chest chest = (Chest) clickedBlock.getState();
         Player player = event.getPlayer();
 
         // Checks to make sure that the chest has items.
         if (chest.getBlockInventory().getSize() == 0) {
             player.sendMessage("This chest is empty. It cannot be cleared.");
             return;
         }
 
         // Creates a backup of the items, then deletes them.
         plugin.addChestInventoryBackup(chest, player);
         chest.getInventory().clear();
         chest.update();
 
         player.sendMessage("Chest successfully emptied.");

        event.setCancelled(true);
     }
 }
