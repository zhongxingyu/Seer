 /**
  * DropHelpers.java
  * Purpose: Handles all entity dropping for the plugin.
  * 
  * @version 1.2.0 11/5/12
  * @author Scott Woodward
  */
 package com.gmail.scottmwoodward.headhunter.helpers;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.craftbukkit.v1_4_6.inventory.CraftItemStack;
 import org.bukkit.entity.Item;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.meta.SkullMeta;
 
 
 public class DropHelper {
 
     /**
      * Called when the plugin is enabled upon server
      * startup. Registers all events and commands for
      * the plugin
      * 
      * @param head is the type of head to be dropped
      * @param loc is the location to drop the head
      * @param name is the player name to be put on a human skull (null if not a human skull)
      * @param world is the world for the drop to occur in
      */
     public static void drop(HeadType head, Location loc, String name, World world){
         if(shouldDrop(head)){
        	ItemStack itemStack = new ItemStack(Material.SKULL_ITEM, 1, (short) head.getValue());
         	CraftItemStack item = CraftItemStack.asCraftCopy(itemStack);
             Item drop = world.dropItemNaturally(loc,item);
             if(name != null){
                 drop.setItemStack(setSkin(new ItemStack(Material.SKULL_ITEM, 1, (byte) 3), name));
             }
         }
     }
 
     private static boolean shouldDrop(HeadType head){
         double fraction = Math.random();
         double chance = 0;
         if(head.getValue()==0 && ConfigHelper.getBoolean("Skeleton")){
             chance = ConfigHelper.getDouble("Skeleton");
         }else if(head.getValue()==1 && ConfigHelper.getBoolean("WitherSkeleton")){
             chance = ConfigHelper.getDouble("WitherSkeleton");
         }else if(head.getValue()==2 && ConfigHelper.getBoolean("Zombie")){
             chance = ConfigHelper.getDouble("Zombie");
         }else if(head.getValue()==3 && ConfigHelper.getBoolean("Player")){
             chance = ConfigHelper.getDouble("Player");
         }else if(head.getValue()==4 && ConfigHelper.getBoolean("Creeper")){
             chance = ConfigHelper.getDouble("Creeper");
         }
         if(chance >= 100){
             return true;
         }else if(chance <= 0){
             return false;
         }else{
             return ((fraction*100) <= chance );
         }
     }
     private static ItemStack setSkin(ItemStack item, String nick) {
         SkullMeta meta = (SkullMeta) item.getItemMeta();
         meta.setOwner(nick);
         item.setItemMeta(meta);
         return item;
     }
 }
