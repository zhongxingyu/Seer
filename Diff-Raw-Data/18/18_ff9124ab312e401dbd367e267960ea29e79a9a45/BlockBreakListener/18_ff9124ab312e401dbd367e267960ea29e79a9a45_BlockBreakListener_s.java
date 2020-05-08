 package com.github.mat8913.LegitMinecraft;
 
 import org.bukkit.event.Listener;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.block.BlockBreakEvent;
 
 public class BlockBreakListener implements Listener {
   //What tool you need to destroy these blocks
   private final int[] pick = {1, 4, 15, 16, 21, 22, 23, 24, 41, 42, 43, 44, 45, 48, 49, 52, 56, 57, 61, 62, 67, 69, 70, 71, 73, 74, 77, 82, 87, 89, 97, 98, 101, 108, 109, 112, 113, 114, 121, 129, 133};
   //private final int[] axe;
   //private final int[] shovel;
   //private final int[] shears;
 
 
   @EventHandler(priority = EventPriority.HIGH)
   public void blockBreak(BlockBreakEvent event) {
    //FIXME: Not very efficiant I would assume (but then again, Minecraft isn't very efficiant)
     for (int i = 0; i < this.pick.length; i++) {
       if (event.getBlock().getTypeId() == pick[i]) {
        event.setCancelled(true);
        event.getPlayer().sendMessage("You need a pickaxe to break this block");
       }
     }
 /*
     for (int i = 0; i < this.axe.length; i++) {
       if (event.getBlock().getTypeId() == axe[i]) {
         //Cancel event
         event.getPlayer().sendMessage("You need an axe to break this block.");
       }
     }
 
      for (int i = 0; i < this.axe.length; i++) {
       if (event.getBlock().getTypeId() == shovel[i]) {
         //Cancel event
         event.getPlayer().sendMessage("You need a shovel to break this block.");
       }
     }
 
     for (int i = 0; i < this.axe.length; i++) {
       if (event.getBlock().getTypeId() == shears[i]) {
         //Cancel event
         event.getPlayer().sendMessage("You need shears to break this block.");
       }
     }
 */
   }
 
 }
