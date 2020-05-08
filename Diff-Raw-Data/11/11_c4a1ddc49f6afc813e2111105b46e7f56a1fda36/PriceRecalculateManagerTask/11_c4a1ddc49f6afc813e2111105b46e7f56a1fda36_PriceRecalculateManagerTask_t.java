 package net.cubespace.RegionShop.Data.Tasks;
 
 import net.cubespace.RegionShop.Bukkit.Plugin;
 import net.cubespace.RegionShop.Config.ConfigManager;
 import net.cubespace.RegionShop.Config.Files.Sub.Item;
 import net.cubespace.RegionShop.Config.Files.Sub.ServerShop;
 import net.cubespace.RegionShop.Database.Database;
 import net.cubespace.RegionShop.Database.Table.ItemMeta;
 import net.cubespace.RegionShop.Database.Table.Items;
 import net.cubespace.RegionShop.Database.Table.Region;
 import net.cubespace.RegionShop.Util.Logger;
 import org.bukkit.scheduler.BukkitRunnable;
 
 import java.sql.SQLException;
 import java.util.ArrayList;
 
 /**
  * @author geNAZt (fabian.fassbender42@googlemail.com)
  */
 public class PriceRecalculateManagerTask extends BukkitRunnable {
     @Override
     public void run() {
         ArrayList<Item> items = new ArrayList<Item>();
 
         for (final ServerShop shop : ConfigManager.servershop.ServerShops) {
             Region region = null;
             try {
                 region = Database.getDAO(Region.class).queryBuilder().where().eq("region", shop.Region).queryForFirst();
             } catch (SQLException e) {
                 Logger.error("Could not get Region", e);
             }
 
             if(region == null) {
                 Logger.info("Region not found " + shop.Region);
                 continue;
             }
 
             for (Item item : shop.Items) {
                 Items itemInShop = null;
                 try {
                     ItemMeta itemMeta = Database.getDAO(ItemMeta.class).queryBuilder().
                             where().
                             eq("itemID", item.itemID).
                             and().
                             eq("dataValue", item.dataValue).queryForFirst();
 
                     if(itemMeta == null) {
                         Logger.info("ItemMeta not found " + item.itemID);
                         continue;
                     }
 
                     itemInShop = Database.getDAO(Items.class).queryBuilder().
                             where().
                             eq("itemstorage_id", region.getItemStorage().getId()).
                             and().
                             eq("itemmeta_id", itemMeta.getId()).queryForFirst();
                 } catch (SQLException e) {
                     Logger.error("Could not get Item", e);
                 }
 
                 if (itemInShop != null) {
                     item.databaseID = itemInShop.getId();
                     items.add(item);
                 }
             }
         }
 
         Logger.info("Recalc found " + items.size() + " Items which should be recalced");
 
         Integer amountThreads = ((Double) Math.ceil(items.size() / (double) 20)).intValue();
             ArrayList<Item> currentThread = new ArrayList<Item>();
             for(Item items1 : items) {
                 if (currentThread.size() > Math.ceil(items.size() / (double)amountThreads)) {
                     Plugin.getInstance().getServer().getScheduler().runTaskTimerAsynchronously(Plugin.getInstance(), new PriceRecalculateTask(new ArrayList<Item>(currentThread)), 5 * 20, 5 * 20);
                     currentThread = new ArrayList<Item>();
                 } else {
                     currentThread.add(items1);
                 }
             }
 
         Logger.info("Started " + amountThreads + " Recalc Threads");
     }
 }
