 package com.puidokas.epuidokas;
 
 import org.bukkit.block.Block;
 import org.bukkit.event.block.BlockFromToEvent;
 import org.bukkit.event.block.BlockListener;
import org.bukkit.inventory.ItemStack;
 
 public class ObGenBlockerBlockListener extends BlockListener{
     
     public ObGenBlockerBlockListener(){ }
 
     public void onBlockFromTo(BlockFromToEvent event) {
         if (event.isCancelled() == true)
             return;
         int source = event.getBlock().getTypeId();
         Block block = event.getToBlock();
        if ((source == 10 || source == 11 || source == 0) && block.getTypeId() == 55) {
             block.setTypeId(0);
            block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(331,1));
         }
     }
 }
