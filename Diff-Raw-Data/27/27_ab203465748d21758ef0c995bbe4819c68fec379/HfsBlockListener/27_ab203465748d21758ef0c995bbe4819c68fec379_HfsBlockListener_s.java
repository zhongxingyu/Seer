 /*
  * Copyright 2013 Alan Kristensen. All rights reserved.
  *
  *   This program is free software: you can redistribute it and/or modify
  *   it under the terms of the GNU General Public License as published by
  *   the Free Software Foundation, either version 3 of the License, or
  *   (at your option) any later version.
  *
  *   This program is distributed in the hope that it will be useful,
  *   but WITHOUT ANY WARRANTY; without even the implied warranty of
  *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *   GNU General Public License for more details.
  *
  *   You should have received a copy of the GNU General Public License
  *   along with this program.  See LICENSE in the jar file. If not, 
  *   see {http://www.gnu.org/licenses/}.
  */
 
 package ws.kristensen.HopperFilterSimplified;
 
 import org.bukkit.Location;
 import org.bukkit.block.Block;
 import org.bukkit.block.Chest;
 import org.bukkit.block.Hopper;
 import org.bukkit.entity.ItemFrame;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.hanging.HangingBreakEvent;
 import org.bukkit.event.hanging.HangingPlaceEvent;
 import org.bukkit.event.inventory.InventoryCloseEvent;
 import org.bukkit.event.inventory.InventoryMoveItemEvent;
 import org.bukkit.event.player.PlayerInteractEntityEvent;
 import org.bukkit.inventory.InventoryHolder;
 
 public class HfsBlockListener implements Listener {
     private final HopperFilterSimplified plugin;
     
     public HfsBlockListener(HopperFilterSimplified plugin) {
         if (plugin == null) {
             throw new IllegalArgumentException("Plugin cannot be null");
         }
         this.plugin = plugin;
     }
 
     /**
      * Event handler to watch whenever a player right clicks on an itemFrame.
      * This will remove the cache of a hopper the item frame is attached to.
      * 
      * @param event Item frame that was right clicked on
      */
     @EventHandler
     public void onPlayerInteractEntityEvent(PlayerInteractEntityEvent event) {
         if (event.getRightClicked() instanceof ItemFrame)  {
             //get the block it was attached to
             Location itemFrameLocation = event.getRightClicked().getLocation();
            Block blockItemFrameWasAttachedTo = itemFrameLocation.getBlock().getRelative(((ItemFrame)event.getRightClicked()).getFacing());
 
             //debug if requested
             if (plugin.debugLevel_get() > 0) plugin.getLogger().info("ItemFrame altered at (" + itemFrameLocation.toString() + ")");
 
            //an itemFrame was broken.  clear the cache of any hoppers around it.
            plugin.knownHoppersCache_ClearLocation(blockItemFrameWasAttachedTo.getLocation());
         }
     }
 
     /**
      * Event handler to watch whenever a chest is broken.
      * This will remove the cache of hoppers around the chest.
      * 
      * @param event Chest being broken
      */
     @EventHandler
     public void onBreakingEvent(BlockBreakEvent event) {
         if (event.getBlock() instanceof Chest) {
             Block chestBlock = event.getBlock();
 
             //debug if requested
             if (plugin.debugLevel_get() > 0) plugin.getLogger().info("Chest broken at (" + chestBlock.getLocation().toString() + ")");
 
             plugin.knownHoppersCache_ClearAroundLocation(chestBlock.getLocation());            
         }
     }
 
     /**
      * Event handler to watch whenever a chest is closed.
      * This will remove the cache of hoppers around the chest.
      *  
      * @param event Chest being closed
      */
     @EventHandler (ignoreCancelled=true)
     public void onInventoryCloseEvent(InventoryCloseEvent event) {
         InventoryHolder holder = event.getInventory().getHolder();
         if (holder instanceof Chest) {
             //debug if requested
             if (plugin.debugLevel_get() > 0) plugin.getLogger().info("Chest closed at (" + ((Chest)holder).getLocation().toString() + ")");
 
 
             //plugin.sendMessageInfo((CommandSender)event.getPlayer(), "Chest Closed");
             plugin.knownHoppersCache_ClearAroundLocation(((Chest) holder).getLocation());
         }
     }
     
     /**
      * Event handler to watch whenever an itemframe is placed.
      * This will remove the cache of the hopper it was placed on.
      * 
      * @param event HangingPlaceEvent containing data about the frame being placed.
      */
     @EventHandler(ignoreCancelled=true)
     public void onHangingPlaceEvent(HangingPlaceEvent event) {
         if (event.getEntity() instanceof ItemFrame) {
             if (event.getBlock() instanceof Hopper) {
                 //debug if requested
                 if (plugin.debugLevel_get() > 0) plugin.getLogger().info("ItemFrame placed at (" + event.getBlock().getLocation().toString() + ")");
 
                //an itemFrame was added. Clear the hopper it was placed on.
                plugin.knownHoppersCache_ClearLocation(event.getBlock().getLocation());                
             }
         }        
     }
     
     /**
      * Event handler to watch whenever an itemframe is broken.
      * This will remove the cache of the hoppers around it.
      * 
      * @param event HangingBreakEvent containing data about the frame being removed.
      */
     @EventHandler(ignoreCancelled=true)
     public void onHangingBreakingEvent(HangingBreakEvent event) {
         if (event.getEntity() instanceof ItemFrame) {
             //get the block it was attached to
             Location itemFrameLocation = event.getEntity().getLocation();
            Block blockItemFrameWasAttachedTo = event.getEntity().getWorld().getBlockAt(itemFrameLocation).getRelative(event.getEntity().getFacing());
 
             //debug if requested
             if (plugin.debugLevel_get() > 0) plugin.getLogger().info("ItemFrame broken at (" + itemFrameLocation.toString() + ")");
 
            //an itemFrame was broken.  clear the cache of any hoppers around it.
            plugin.knownHoppersCache_ClearLocation(blockItemFrameWasAttachedTo.getLocation());
         }
     }
     
     /** 
      * Event handler to watch whenever a container tries to move an item to another container.
      * Hoppers for example are the only thing that triggers this yet.
      * If the item being moved is not allowed in the destination container, then the event will be canceled to prevent the move.
      *
      *  @param event InventoryMoveItemEvent containing data about the event given by the system
      */
     @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled=true)
     public void inventoryMoveHandler(InventoryMoveItemEvent event) {
         //Get destination container
         InventoryHolder destHolder = event.getDestination().getHolder();
         
         //since we only care about hoppers, then ignore everything else
         if (destHolder instanceof Hopper) {
             //see if there is an itemFrame attached to this hopper.
             Block hopperBlock = ((Hopper) destHolder).getBlock();
 
             //check cache first
             String cache = plugin.knownHoppersCache_Get(hopperBlock);
 
             //get the eventItem for comparison
             String eventItemInformation = plugin.GetItemInformationForInventory(event.getItem(), false);
                         
             //debug if requested
             if (plugin.debugLevel_get() > 0) plugin.getLogger().info("Testing item (" + eventItemInformation + ") in hopper (" + hopperBlock.getLocation().toString() + ") against allowed:" + cache);
             
             //do the actual filtering
             if (cache.length() > 0)
                 if (!cache.contains(eventItemInformation))
                     //the item is not allowed in the hopper
                     event.setCancelled(true);
         }
     }
 }
