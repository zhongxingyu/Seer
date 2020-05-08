 package com.geNAZt.RegionShop.Data.Tasks;
 
 import com.geNAZt.RegionShop.Config.ConfigManager;
 import com.geNAZt.RegionShop.Database.Database;
 import com.geNAZt.RegionShop.Database.Model.Item;
 import com.geNAZt.RegionShop.Database.Table.Chest;
 import com.geNAZt.RegionShop.Database.Table.Items;
 import com.geNAZt.RegionShop.RegionShopPlugin;
 import com.geNAZt.RegionShop.Util.ItemName;
 import com.geNAZt.RegionShop.Util.NMS;
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.block.Sign;
 import org.bukkit.entity.Entity;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.scheduler.BukkitRunnable;
 import org.bukkit.util.Vector;
 
 import java.util.Iterator;
 import java.util.List;
 
 /**
  * Created for ME :D
  * User: geNAZt (fabian.fassbender42@googlemail.com)
  * Date: 04.10.13
  */
 public class DisplayItemOverChest extends BukkitRunnable {
     @Override
     public void run() {
         List<Chest> chestList  = Database.getServer().find(Chest.class).findList();
 
         for(final Chest chest : chestList) {
             boolean found = false;
             for (Entity ent : Bukkit.getWorld(chest.getWorld()).getEntities()) {
                 if(ent.getLocation().getBlockY() == chest.getChestY()+1 && ent.getLocation().getBlockX() == chest.getChestX() && ent.getLocation().getBlockZ() == chest.getChestZ()) {
                     found = true;
                 }
             }
 
             if(!found) {
                 Iterator itemsIterator = chest.getItemStorage().getItems().iterator();
                 if(!itemsIterator.hasNext()) {
                     RegionShopPlugin.getInstance().getLogger().warning("Found Chest without item. Maybe wrong deletion: " + chest.getId());
                     continue;
                 }
 
                 final Items items = chest.getItemStorage().getItems().iterator().next();
                 final ItemStack itemStack = Item.fromDBItem(items);
                 itemStack.setAmount(1);
 
                 Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(RegionShopPlugin.getInstance(), new BukkitRunnable() {
                     @Override
                     public void run() {
                         org.bukkit.entity.Item droppedItem = Bukkit.getWorld(chest.getWorld()).dropItem(new Location(Bukkit.getWorld(chest.getWorld()), (double) chest.getChestX() + 0.5, (double)chest.getChestY() + 1.2, (double)chest.getChestZ() + 0.5), itemStack);
                         droppedItem.setVelocity(new Vector(0, 0.1, 0));
                         NMS.safeGuard(droppedItem);
 
                        Sign sign = (Sign) Bukkit.getWorld(chest.getWorld()).getBlockAt(chest.getSignX(), chest.getSignY(), chest.getSignZ()).getState();
 
                         //Get the nice name
                         String itemName = ItemName.getDataName(itemStack) + itemStack.getType().toString();
                         if (itemStack.getItemMeta().hasDisplayName()) {
                             itemName += "(" + itemStack.getItemMeta().getDisplayName() + ")";
                         }
 
 
                         for(Integer line = 0; line < 4; line++) {
                             sign.setLine(line, ConfigManager.language.Sign_Shop_SignText.get(line).
                                     replace("%player",  chest.getOwners().iterator().next().getName()).
                                     replace("%itemname", ItemName.nicer(itemName)).
                                     replace("%amount", items.getUnitAmount().toString()).
                                     replace("%sell", items.getSell().toString()).
                                     replace("%buy", items.getBuy().toString()));
                         }
 
                         sign.update();
                     }
                 });
             }
         }
     }
 }
