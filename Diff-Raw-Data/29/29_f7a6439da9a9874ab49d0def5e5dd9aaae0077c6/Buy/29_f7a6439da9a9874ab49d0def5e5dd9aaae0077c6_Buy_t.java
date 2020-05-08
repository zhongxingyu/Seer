 package com.geNAZt.RegionShop.Core;
 
 import com.geNAZt.RegionShop.Config.ConfigManager;
 import com.geNAZt.RegionShop.Database.Database;
 import com.geNAZt.RegionShop.Database.ItemStorageHolder;
 import com.geNAZt.RegionShop.Database.Model.Item;
 import com.geNAZt.RegionShop.Database.Model.Transaction;
 import com.geNAZt.RegionShop.Database.Table.ItemStorage;
 import com.geNAZt.RegionShop.Database.Table.Items;
 import com.geNAZt.RegionShop.RegionShopPlugin;
 import com.geNAZt.RegionShop.Util.EssentialBridge;
 import com.geNAZt.RegionShop.Util.ItemName;
 import com.geNAZt.RegionShop.Util.VaultBridge;
 import net.milkbowl.vault.economy.Economy;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * Created for YEAHWH.AT
  * User: geNAZt (fabian.fassbender42@googlemail.com)
  * Date: 18.09.13
  */
 public class Buy {
     public static void buy(Items item, Player player, ItemStorageHolder region, Integer wishAmount) {
         if (item.getOwner().toLowerCase().equals(player.getName().toLowerCase())) {
             player.sendMessage(ConfigManager.main.Chat_prefix + ConfigManager.language.Buy_NotYourItems);
             return;
         }
 
         if (item.getSell() <= 0) {
             player.sendMessage(ConfigManager.main.Chat_prefix + ConfigManager.language.Buy_NoSell);
             return;
         }
 
         if (wishAmount > item.getCurrentAmount()) {
             player.sendMessage(ConfigManager.main.Chat_prefix + ConfigManager.language.Buy_NotEnoughItems);
             return;
         }
 
         if (wishAmount < 1) {
             wishAmount = 1;
         }
 
         Economy eco = VaultBridge.economy;
         Float price = (((float) wishAmount / (float) item.getUnitAmount()) * item.getSell());
 
         if (eco.has(player.getName(), price)) {
             ItemStack iStack = Item.fromDBItem(item);
             iStack.setAmount(wishAmount);
 
            System.out.println(iStack.getData().toString());

             HashMap<Integer, ItemStack> notFitItems = player.getInventory().addItem(iStack);
             if (!notFitItems.isEmpty()) {
                 for(Map.Entry<Integer, ItemStack> notFitItem : notFitItems.entrySet()) {
                     wishAmount -= notFitItem.getValue().getAmount();
                 }
             }
 
             player.updateInventory();
 
             price = (((float) wishAmount / (float) item.getUnitAmount()) * item.getSell());
 
             String niceItemName = ItemName.getDataName(iStack) + ItemName.nicer(iStack.getType().toString());
 
             item.setCurrentAmount(item.getCurrentAmount() - wishAmount);
 
             if(!item.getItemStorage().isServershop()) {
                 OfflinePlayer owner = RegionShopPlugin.getInstance().getServer().getOfflinePlayer(item.getOwner());
                 Player onOwner = null;
 
                 if(owner.isOnline()) {
                     onOwner = (Player) owner;
                 }
 
                 if (onOwner != null) {
                     onOwner.sendMessage(ConfigManager.main.Chat_prefix + ConfigManager.language.Buy_OwnerHint.
                             replace("%player", player.getDisplayName()).
                             replace("%amount", wishAmount.toString()).
                             replace("%item", niceItemName).
                             replace("%shop", region.getName()).
                             replace("%price", price.toString()));
                 }
 
                 eco.depositPlayer(item.getOwner(), price);
 
                 if(item.getCurrentAmount() == 0) {
                     if (onOwner != null) {
                         onOwner.sendMessage(ConfigManager.main.Chat_prefix + ConfigManager.language.Buy_OwnerHintEmptyShop.replace("%item", niceItemName).replace("%shop", region.getName()));
                     } else {
                         EssentialBridge.sendMail(ConfigManager.main.Chat_prefix, owner, ConfigManager.language.Buy_OwnerHintEmptyShop.replace("%item", niceItemName).replace("%shop", region.getName()));
                     }
                 }
 
                 Transaction.generateTransaction(owner, com.geNAZt.RegionShop.Database.Table.Transaction.TransactionType.SELL, region.getName(), player.getWorld().getName(), player.getName(), item.getMeta().getId().getItemID(), wishAmount, 0.0, item.getSell().doubleValue(), item.getUnitAmount());
             }
 
             eco.withdrawPlayer(player.getName(), price);
             player.sendMessage(ConfigManager.main.Chat_prefix + ConfigManager.language.Buy_PlayerHint.
                     replace("%player", player.getDisplayName()).
                     replace("%amount", wishAmount.toString()).
                     replace("%item", niceItemName).
                     replace("%shop", region.getName()).
                     replace("%price", price.toString()).
                     replace("%owner", item.getOwner()));
 
             item.setSold(item.getSold() + wishAmount);
 
             ItemStorage itemStorage = region.getItemStorage();
             itemStorage.setItemAmount(itemStorage.getItemAmount() - wishAmount);
 
             Database.getServer().update(itemStorage);
             Database.getServer().update(item);
 
             Transaction.generateTransaction(player, com.geNAZt.RegionShop.Database.Table.Transaction.TransactionType.BUY, region.getName(), player.getWorld().getName(), item.getOwner(), item.getMeta().getId().getItemID(), wishAmount, item.getSell().doubleValue(), 0.0, item.getUnitAmount());
         } else {
             player.sendMessage(ConfigManager.main.Chat_prefix + ConfigManager.language.Buy_NotEnoughMoney.replace("%price", price.toString()));
         }
     }
 }
