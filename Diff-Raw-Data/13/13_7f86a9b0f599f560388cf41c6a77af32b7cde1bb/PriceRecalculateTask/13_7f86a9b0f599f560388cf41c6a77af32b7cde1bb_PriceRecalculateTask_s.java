 package com.geNAZt.RegionShop.Data.Tasks;
 
 import com.geNAZt.RegionShop.Config.ConfigManager;
 import com.geNAZt.RegionShop.Config.Sub.Item;
 import com.geNAZt.RegionShop.Config.Sub.ServerShop;
 import com.geNAZt.RegionShop.Database.Database;
 import com.geNAZt.RegionShop.Database.Table.CustomerSign;
 import com.geNAZt.RegionShop.Database.Table.Items;
 import com.geNAZt.RegionShop.RegionShopPlugin;
 import com.geNAZt.RegionShop.Util.ItemName;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.Sign;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.scheduler.BukkitRunnable;
 
 /**
  * Created for YEAHWH.AT
  * User: geNAZt (fabian.fassbender42@googlemail.com)
  * Date: 16.06.13
  */
 public class PriceRecalculateTask extends BukkitRunnable {
     @Override
     public void run() {
         for (ServerShop shop : ConfigManager.servershop.ServerShops) {
             for (Item item : shop.Items) {
                 Items itemInShop = Database.getServer().find(Items.class).
                         setUseCache(false).
                         setReadOnly(false).
                         setUseQueryCache(false).
                         where().
                         eq("meta.id.itemID", item.itemID).
                         eq("meta.id.dataValue", item.dataValue).
                         eq("itemStorage.regions.region", shop.Region).
                         findUnique();
 
                 if (itemInShop == null) continue;
 
                 Integer sold = (itemInShop.getSold() * 4) * 60;
                 Integer bought = (itemInShop.getBought() * 4) * 60;
 
                 Float sellPriceDiff = (float) sold / item.maxItemRecalc;
                 Float buyPriceDiff;
 
                 if (bought > 0) {
                     buyPriceDiff = (float) item.maxItemRecalc / bought;
                 } else {
                     buyPriceDiff = 2.0F;
                 }
 
                 if (sellPriceDiff > 1.0) {
                     //Preis geht rauf
                     if (sellPriceDiff > item.limitSellPriceFactor) {
                         sellPriceDiff = item.limitSellPriceFactor;
                     }
                 } else {
                     //Preis geht runter
                     if (sellPriceDiff < item.limitSellPriceUnderFactor) {
                         sellPriceDiff = item.limitSellPriceUnderFactor;
                     }
                 }
 
                 if (buyPriceDiff > 1.0) {
                     //Abgabe geht rauf
                     buyPriceDiff = buyPriceDiff * item.limitBuyPriceFactor;
                 } else {
                     //Abgabe geht runter
                     if (buyPriceDiff < item.limitBuyPriceUnderFactor) {
                         buyPriceDiff = item.limitBuyPriceUnderFactor;
                     }
                 }
 
                System.out.println(sellPriceDiff);

                 Float newSellPrice = item.sell;
                newSellPrice *= 1 + sellPriceDiff;
                 newSellPrice = Math.round(newSellPrice * 100) / 100.0F;
 
                 Float newBuyPrice = item.buy;
                newBuyPrice *= 1 + buyPriceDiff;
                 newBuyPrice = Math.round(newBuyPrice * 100) / 100.0F;
 
                itemInShop.setBuy((itemInShop.getBuy() - newBuyPrice) / 100);
                itemInShop.setSell((itemInShop.getSell() - newSellPrice) / 100);
                 itemInShop.setCurrentAmount(99999);
                 itemInShop.setBought(0);
                 itemInShop.setSold(0);
 
                 Database.getServer().update(itemInShop);
 
                 //Check if Item has a Sign
                 final CustomerSign customerSign = Database.getServer().find(CustomerSign.class).
                         where().
                         conjunction().
                         eq("item", itemInShop).
                         endJunction().
                         findUnique();
 
                 final Items items = itemInShop;
 
                 if (customerSign != null) {
                     RegionShopPlugin.getInstance().getServer().getScheduler().scheduleSyncDelayedTask(RegionShopPlugin.getInstance(), new Runnable() {
                         @Override
                         public void run() {
                             Block block = RegionShopPlugin.getInstance().getServer().getWorld(customerSign.getRegion().getWorld()).getBlockAt(customerSign.getX(), customerSign.getY(), customerSign.getZ());
                             if (block.getType().equals(Material.SIGN_POST) || block.getType().equals(Material.WALL_SIGN)) {
                                 Sign sign = (Sign) block.getState();
 
                                 //Get the nice name
                                 ItemStack itemStack = com.geNAZt.RegionShop.Database.Model.Item.fromDBItem(items);
                                 String itemName = ItemName.getDataName(itemStack) + itemStack.getType().toString();
                                 if (itemStack.getItemMeta().hasDisplayName()) {
                                     itemName = "(" + itemStack.getItemMeta().getDisplayName() + ")";
                                 }
 
                                 for (Integer line = 0; line < 4; line++) {
                                     sign.setLine(line, ConfigManager.language.Sign_Customer_SignText.get(line).
                                             replace("%id", items.getId().toString()).
                                             replace("%itemname", ItemName.nicer(itemName)).
                                             replace("%amount", items.getUnitAmount().toString()).
                                             replace("%sell", items.getSell().toString()).
                                             replace("%buy", items.getBuy().toString()));
                                 }
 
                                 sign.update();
                             }
                         }
                     });
                 }
             }
         }
     }
 }
 
