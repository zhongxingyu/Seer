 package me.limebyte.battlenight.core.util;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.PlayerInventory;
 
 public class OldUtil {
 
     private static final ItemStack prev = createArrow("Previous Player");
     private static final ItemStack next = createArrow("Next Player");
 
     ////////////////////
     //     Items      //
     ////////////////////
 
     public static void clearInventory(Player player) {
         PlayerInventory inv = player.getInventory();
         inv.clear();
         inv.setArmorContents(new ItemStack[inv.getArmorContents().length]);
     }
 
     public static void equipArrows(Player player) {
         player.getInventory().setItem(0, prev);
         player.getInventory().setItem(8, next);
     }
 
     private static ItemStack createArrow(String action) {
         ItemStack stack = new ItemStack(Material.PAPER);
        stack.getItemMeta().setDisplayName(ChatColor.GREEN + action);
         return stack;
     }
 }
