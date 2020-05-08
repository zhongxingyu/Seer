 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package me.zilo.DTplugin.Utility;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 
 public class DeathData 
 {
     private final ItemStack[] itemInv;
     private final ItemStack[] armInv;
     private final Location location;
     
     public DeathData(Location loc, ItemStack[] item, ItemStack[] arm)
     {
         location = loc;
         itemInv = item;
         armInv = arm;
     }
     
     public void dropItem()
     {
         for (ItemStack item : itemInv)
         {
             if (item != null && item.getType() != Material.AIR)
             {
                 location.getWorld().dropItem(location, item);
             }
         }
         
         for (ItemStack item : armInv)
         {
             if (item != null && item.getType() != Material.AIR)
             {
                 location.getWorld().dropItem(location, item);
             }
         }
     }
     
     public void giveKeepItemToPlayer(final Player player)
     {
         player.getInventory().setContents(itemInv);
         player.getInventory().setArmorContents(armInv);
     }
 }
