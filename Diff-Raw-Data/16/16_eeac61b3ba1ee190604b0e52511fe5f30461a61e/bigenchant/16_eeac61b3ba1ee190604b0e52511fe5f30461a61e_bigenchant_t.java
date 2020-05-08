 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package blacksmithmagic.magics.enchant;
 
 import blacksmithmagic.*;
 import bluenova.fairytailcraft.plugin.MagePluginEvent;
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.enchantments.Enchantment;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.inventory.ItemStack;
 
 /**
  *
  * @author Sven
  */
 public class bigenchant extends MagePluginEvent {
 
     @Override
     public boolean callPlayerInteractEvent(PlayerInteractEvent event, Integer level) {
         if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
             ItemStack item = event.getItem();
             if (item != null) {
                 int minLevel = 22;
                 if (BlacksmithMagic.activeBEnchants.containsKey(event.getPlayer())) {
                     Enchantment ench = BlacksmithMagic.activeBEnchants.get(event.getPlayer());
                     if (level > ench.getMaxLevel()) {
                         level = ench.getMaxLevel();
                     }
                    minLevel = minLevel + ((level-1) * 7);
                     int playerLevel = BlacksmithMagic.manager.getPlayerConfig(event.getPlayer()).getLevel();
                     if (playerLevel >= minLevel) {
 
                         if (ench.canEnchantItem(item)) {
                             item.addEnchantment(ench, level);
                             event.getPlayer().setItemInHand(item);
                             event.getPlayer().sendMessage(ChatColor.GREEN + "Item in Hand Enchanted!");
                             event.getPlayer().sendMessage("Mana deceesed!");
                         } else {
                             event.getPlayer().sendMessage(ChatColor.RED + "Can't Enchant Item in Hand with Active Enchantment!");
                         }
 
                     } else {
                         event.getPlayer().sendMessage(ChatColor.RED + "Minimumlevel for Spelllevel " + level + " is " + minLevel + "!");
                     }
                 } else {
                     event.getPlayer().sendMessage(ChatColor.RED + "You have to set an Enchantment first!");
                 }
             }
         } else {
             event.getPlayer().sendMessage(ChatColor.RED + "Can't Enchant Item in Hand with Active Enchantment!");
         }
         return false;
     }
 }
