 package com.geNAZt.RegionShop.Listener;
 
 import com.geNAZt.RegionShop.Config.ConfigManager;
 import com.geNAZt.RegionShop.Config.Sub.ServerShop;
 import com.geNAZt.RegionShop.Database.Database;
 import com.geNAZt.RegionShop.Database.Model.Item;
 import com.geNAZt.RegionShop.Database.Table.Chest;
 import com.geNAZt.RegionShop.Database.Table.CustomerSign;
 import com.geNAZt.RegionShop.Util.NMS;
 import org.bukkit.Location;
 import org.bukkit.entity.Entity;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.world.ChunkLoadEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.util.Vector;
 
 import java.util.List;
 
 /**
  * @author geNAZt (fabian.fassbender42@googlemail.com)
  * @date Last changed: 23.10.13 12:54
  */
 public class ShowcaseReloadOnChunkLoad implements Listener {
     @EventHandler
     public void onChunkLoad(ChunkLoadEvent event) {
         //Get all DB Entries which could have a Showcase in this World
         final List<CustomerSign> customerSigns = Database.getServer().find(CustomerSign.class).where().eq("region.world", event.getWorld().getName()).findList();
         final List<Chest> chests = Database.getServer().find(Chest.class).where().eq("world", event.getWorld().getName()).findList();
 
         //Check if a CustomerSign is above
         for (final CustomerSign customerSign : customerSigns) {
             //Load the ServerShop, only CustomerSigns with ServerShop can have Showcases
             ServerShop serverShop = ConfigManager.servershop.getServerShopByRegion(customerSign.getRegion().getRegion());
             if (serverShop == null) continue;
 
             //Check if Sign is in this Chunk
             if(!(
                    (event.getChunk().getX() * 16 < customerSign.getX() && (event.getChunk().getX() * 16) + 16 > customerSign.getX()) &&
                    (event.getChunk().getZ() * 16 < customerSign.getZ() && (event.getChunk().getZ() * 16) + 16 > customerSign.getZ())
             )) {
                 continue;
             }
 
             //Loop through all Entities located in the World
             for (final Entity ent : event.getChunk().getEntities()) {
                 //Get the location of this Entity
                 Location entLocation = ent.getLocation();
                 if (entLocation.getBlockZ() == customerSign.getZ() && entLocation.getBlockY() == customerSign.getY() - 1 && entLocation.getBlockX() == customerSign.getX()) {
                     ent.remove();
                 }
             }
 
             //Check if a new Entity needs to be dropped
             if (serverShop.Showcase) {
                 //Get the ItemStack out of the Database
                 ItemStack itemStack = Item.fromDBItem(customerSign.getItem());
 
                 //Drop the Item
                 org.bukkit.entity.Item droppedItem = event.getWorld().dropItem(new Location(event.getWorld(), (double) customerSign.getX() + 0.5, (double) customerSign.getY() - 0.8, (double) customerSign.getZ() + 0.5), itemStack);
                 droppedItem.setVelocity(new Vector(0, 0.1, 0));
                 droppedItem.setPickupDelay(Integer.MAX_VALUE);
                 NMS.safeGuard(droppedItem);
             }
         }
 
         //Check if a Chest is underneath
         for(final Chest chest : chests) {
             //Check if Chest is in this Chunk
             if(!(
                    (event.getChunk().getX() * 16 < chest.getChestX() && (event.getChunk().getX() * 16) + 16 > chest.getChestX()) &&
                    (event.getChunk().getZ() * 16 < chest.getChestZ() && (event.getChunk().getZ() * 16) + 16 > chest.getChestZ())
             )) {
                 continue;
             }
 
             //Loop through all Entities located in the World
             for (final Entity ent : event.getChunk().getEntities()) {
                 //Get the location of this Entity
                 Location entLocation = ent.getLocation();
                 if (entLocation.getBlockZ() == chest.getChestZ() && entLocation.getBlockY() == chest.getChestY() + 1 && entLocation.getBlockX() == chest.getChestX()) {
                     ent.remove();
                 }
             }
 
 
             //Get the ItemStack out of the Database
             ItemStack itemStack = Item.fromDBItem(chest.getItemStorage().getItems().iterator().next());
 
             //Drop the Item
             org.bukkit.entity.Item droppedItem = event.getWorld().dropItem(new Location(event.getWorld(), (double) chest.getChestX() + 0.5, (double) chest.getChestY() + 1.2, (double) chest.getChestZ() + 0.5), itemStack);
             droppedItem.setVelocity(new Vector(0, 0.1, 0));
             droppedItem.setPickupDelay(Integer.MAX_VALUE);
             NMS.safeGuard(droppedItem);
         }
     }
 }
