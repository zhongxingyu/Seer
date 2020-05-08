 package com.geNAZt.RegionShop.Listener;
 
 import com.geNAZt.RegionShop.Database.Database;
 import com.geNAZt.RegionShop.Database.Table.Chest;
 import com.geNAZt.RegionShop.Database.Table.CustomerSign;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerPickupItemEvent;
 
 /**
  * Created for ME :D
  * User: geNAZt (fabian.fassbender42@googlemail.com)
  * Date: 05.10.13
  */
 public class PretendDisplaysToPickup implements Listener {
     @EventHandler
     public void onPlayerPickupItem(PlayerPickupItemEvent event) {
         //Check if Item is a Display of a Chest
         Chest chest = Database.getServer().find(Chest.class).
             where().
                 eq("chestX", event.getItem().getLocation().getBlockX()).
                 eq("chestY", event.getItem().getLocation().getBlockY()-1).
                 eq("chestZ", event.getItem().getLocation().getBlockZ()).
             findUnique();
 
         if(chest != null) {
             event.setCancelled(true);
         }
 
         //Check if Item is a Display of a Chest
         CustomerSign customerSign = Database.getServer().find(CustomerSign.class).
                 where().
                     eq("x", event.getItem().getLocation().getBlockX()).
                     eq("y", event.getItem().getLocation().getBlockY()+1).
                     eq("y", event.getItem().getLocation().getBlockZ()).
                 findUnique();
 
        if(customerSign != null) {
             event.setCancelled(true);
         }
     }
 }
