 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package lightmagic.magics;
 
 import bluenova.fairytailcraft.plugin.MagePluginEvent;
 import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
 import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.event.Event.Result;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.inventory.ItemStack;
 
 /**
  *
  * @author Sven
  */
 public class SummonGlowstones extends MagePluginEvent {
 
     @Override
     public boolean callPlayerInteractEvent(PlayerInteractEvent event) {
         if (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR ) {
             ItemStack item = event.getItem();
             if(item == null) {
                 event.getPlayer().setItemInHand(item);
                Block clickedBlock = event.getClickedBlock();
                BlockFace blockFace = event.getBlockFace();
                clickedBlock.getRelative(blockFace).setType(Material.TORCH);
                 return true;
             }
         }
         return false;
     }
 }
