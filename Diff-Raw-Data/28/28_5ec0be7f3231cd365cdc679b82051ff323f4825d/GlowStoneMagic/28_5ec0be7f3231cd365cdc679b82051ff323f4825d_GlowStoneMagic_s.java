 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package lightmagic.magics;
 
 import bluenova.fairytailcraft.plugin.MagePluginEvent;
 import org.bukkit.Material;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.event.Event.Result;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.inventory.ItemStack;
 
 /**
  *
  * @author Sven
  */
 public class GlowStoneMagic extends MagePluginEvent {
 
     @Override
     public boolean callPlayerInteractEvent(PlayerInteractEvent event) {
         if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
             ItemStack item = event.getItem();
             if(item == null) {
                item = new CraftItemStack(Material.GLOWSTONE, 1);
                 event.getPlayer().setItemInHand(item);
                Result useItemInHand = event.useItemInHand();
                //event.setUseItemInHand(useItemInHand);
                event.getPlayer().setItemInHand(null);
                 return true;
             }
         }
         return false;
     }
 }
