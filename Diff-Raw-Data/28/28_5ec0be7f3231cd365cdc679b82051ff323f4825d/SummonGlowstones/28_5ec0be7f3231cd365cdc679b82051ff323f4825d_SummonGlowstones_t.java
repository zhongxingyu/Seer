 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package lightmagic.magics;
 
 import bluenova.fairytailcraft.plugin.MagePluginEvent;
 import org.bukkit.Material;
 import org.bukkit.craftbukkit.inventory.CraftItemStack;
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
                item = new CraftItemStack(Material.GLOWSTONE, 64);
                 event.getPlayer().setItemInHand(item);
                 return true;
             }
         }
         return false;
     }
 }
