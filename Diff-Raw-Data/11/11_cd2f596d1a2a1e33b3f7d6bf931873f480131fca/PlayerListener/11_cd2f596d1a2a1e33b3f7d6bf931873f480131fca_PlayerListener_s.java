 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.mahn42.anhalter42.creator;
 
 import com.mahn42.framework.BlockPosition;
 import com.mahn42.framework.Framework;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerMoveEvent;
 
 /**
  *
  * @author andre
  */
 public class PlayerListener  implements Listener {
 
     static Block lLastBlock;
     
     @EventHandler
     public void playerMove(PlayerMoveEvent aEvent) {
         Player lPlayer = aEvent.getPlayer();
         World lWorld = lPlayer.getWorld();
         if (Framework.plugin.isDebugSet("c_target")) {
             Block lTargetBlock = lPlayer.getTargetBlock(null, 100);
             if (lTargetBlock != null && !lTargetBlock.equals(lLastBlock)) {
                BlockPosition lPos = CreatorPlugin.plugin.getMarker(lWorld, "a");
                if (lPos != null) {
                     BlockPosition lTargetPos = new BlockPosition(lTargetBlock.getLocation());
                    lTargetPos.add(-lPos.x, -lPos.y, -lPos.z);
                     lPlayer.sendMessage("Block " + lTargetBlock.getType() + lTargetPos);
                 }
                 lLastBlock = lTargetBlock;
             }
         }
     }
     
 }
