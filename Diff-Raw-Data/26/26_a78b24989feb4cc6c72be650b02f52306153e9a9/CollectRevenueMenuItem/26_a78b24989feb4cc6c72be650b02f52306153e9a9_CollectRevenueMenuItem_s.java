 package com.dumptruckman.bartersigns.menu;
 
 import com.dumptruckman.bartersigns.actionmenu.SignActionMenuItem;
 import com.dumptruckman.bartersigns.BarterSignsPlugin;
 import com.dumptruckman.bartersigns.inventory.InventoryTools;
 import com.dumptruckman.bartersigns.locale.LanguagePath;
 import com.dumptruckman.bartersigns.sign.BarterSign;
 import org.bukkit.inventory.ItemStack;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  * @author dumptruckman
  */
 public class CollectRevenueMenuItem extends SignActionMenuItem {
 
     private BarterSignsPlugin plugin;
     private BarterSign barterSign;
 
     public CollectRevenueMenuItem(BarterSignsPlugin plugin, BarterSign barterSign) {
         super(plugin.lang.lang(LanguagePath.SIGN_REVENUE_COLLECT.getPath(), barterSign.getRevenueStackCount().toString(), barterSign.getRevenueStackCount() > 0 ? "s" : ""));
         this.plugin = plugin;
         this.barterSign = barterSign;
     }
 
     public void update() {
         String s = "";
         Integer stacks = barterSign.getRevenueStackCount();
         if (stacks > 0) s = "s";
         setLines(plugin.lang.lang(LanguagePath.SIGN_REVENUE_COLLECT.getPath(),
                 stacks.toString(), s));
     }
 
     public void run() {
         List<ItemStack> acceptItems = barterSign.getAcceptableItems();
         boolean hasRevenue = false;
         List<String> itemList = new ArrayList<String>();
         for (ItemStack acceptItem : acceptItems) {
             if (barterSign.getRevenue(acceptItem) > 0) {
                 if (!hasRevenue) hasRevenue = true;
                 HashMap<Integer, ItemStack> itemsLeftOver = null;
                 if (plugin.enforceMaxStackSize()) {
                     List<ItemStack> items = InventoryTools.getSeparatedItems(new ItemStack(
                             acceptItem.getType(),
                             barterSign.getRevenue(acceptItem),
                             acceptItem.getDurability()));
                     itemsLeftOver = player.getInventory().addItem(items.toArray(new ItemStack[items.size()]));
                 } else {
                     itemsLeftOver = player.getInventory().addItem(new ItemStack(
                             acceptItem.getType(),
                             barterSign.getRevenue(acceptItem),
                             acceptItem.getDurability()));
                 }
                 int amountLeftOver = 0;
                 for (Map.Entry<Integer, ItemStack> item : itemsLeftOver.entrySet()) {
                     amountLeftOver += item.getValue().getAmount();
                 }
                 itemList.add(plugin.itemToString(new ItemStack(
                         acceptItem.getType(),
                         barterSign.getRevenue(acceptItem) - amountLeftOver,
                         acceptItem.getDurability())));
                 barterSign.setRevenue(acceptItem, barterSign.getRevenue(acceptItem)
                         - (barterSign.getRevenue(acceptItem) - amountLeftOver));
                 if (amountLeftOver > 0) {
                     plugin.sendMessage(player, LanguagePath.SIGN_COLLECT_LEFTOVER.getPath());
                     break;
                 }
             }
         }
         if (!hasRevenue) {
             plugin.sendMessage(player, LanguagePath.SIGN_REVENUE_EMPTY.getPath());
         } else {
             String items = "";
             for (int i = 0; i < itemList.size(); i++) {
                 if (i != 0) {
                     items += ", ";
                 }
                 items += itemList.get(i);
             }
             plugin.sendMessage(player, LanguagePath.SIGN_REVENUE_COLLECTED.getPath(), items);
         }
     }
 }
