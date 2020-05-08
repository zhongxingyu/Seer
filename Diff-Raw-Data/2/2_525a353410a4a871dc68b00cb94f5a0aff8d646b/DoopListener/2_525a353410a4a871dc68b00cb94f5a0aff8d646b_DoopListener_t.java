 package com.thevoxelbox.voxeldoop;
 
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.inventory.ItemStack;
 
 public class DoopListener implements Listener
 {
     private final VoxelDoop plugin;
 
     public DoopListener(final VoxelDoop plugin)
     {
         this.plugin = plugin;
     }
 
     /* Feature incorporated into onPlayerInteract() to allow for use with placing items such as redstone.
     @EventHandler
     public void onBlockPlace(BlockPlaceEvent event)
     {
         ItemStack placedItem = event.getItemInHand();
         if (placedItem != null)
         {
             if (placedItem.getAmount() == 32 && placedItem.getType().isBlock())
             {
                 placedItem.setAmount(64);
             }
         }
     }*/
 
     @EventHandler
     public void onPlayerInteract(PlayerInteractEvent event)
     {
         if (event.hasItem())
         {
             if(event.getAction() == Action.RIGHT_CLICK_BLOCK)
             {
                 ItemStack hand = event.getPlayer().getItemInHand();
                if(hand.getAmount() <= 32 && hand.getAmount() >= 2) hand.setAmount(64);
             }
             if (event.getClickedBlock() == null)
             {
                 this.plugin.getToolManager().onRangedUse(event.getPlayer(), event.getItem(), event.getAction());
             }
             else
             {
                 if (this.plugin.getToolManager().onUse(event.getPlayer(), event.getItem(), event.getAction(), event.getClickedBlock(), event.getBlockFace()))
                 {
                     event.setCancelled(true);
                 }
             }
         }
     }
 }
