 package com.geNAZt.RegionShop.Interface.Sign.Commands;
 
 import com.geNAZt.RegionShop.Config.ConfigManager;
 import com.geNAZt.RegionShop.Core.Add;
 import com.geNAZt.RegionShop.Core.Equip;
 import com.geNAZt.RegionShop.Data.Storage.InRegion;
 import com.geNAZt.RegionShop.Database.Database;
 import com.geNAZt.RegionShop.Database.Model.Item;
 import com.geNAZt.RegionShop.Database.Table.CustomerSign;
 import com.geNAZt.RegionShop.Database.Table.Items;
 import com.geNAZt.RegionShop.Database.Table.Region;
 import com.geNAZt.RegionShop.Interface.Sign.Command;
 import com.geNAZt.RegionShop.Interface.Sign.SignCommand;
 import com.geNAZt.RegionShop.Util.ChestFinder;
 import com.geNAZt.RegionShop.Util.ItemName;
 import com.geNAZt.RegionShop.Util.NMS;
 import org.bukkit.Location;
 import org.bukkit.block.Block;
 import org.bukkit.block.Chest;
 import org.bukkit.block.DoubleChest;
 import org.bukkit.event.block.SignChangeEvent;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.InventoryHolder;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.util.Vector;
 import sun.misc.Regexp;
 
 import java.util.ListIterator;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 
 /**
  * Created for YEAHWH.AT
  * User: geNAZt (fabian.fassbender42@googlemail.com)
  * Date: 10.06.13
  */
 public class Shop implements SignCommand {
    private static Pattern pattern = Pattern.compile(".*([0-9.]+):([0-9.]+).*");
     private static Pattern intPattern = Pattern.compile("([0-9]+)");
 
     @Command(command="thisisimpossibletowriteonasign", permission="rs.sign.shop")
     public static void shop(SignChangeEvent event) {
         //Check if Chest is near
         Inventory chest = ChestFinder.findChest(event.getBlock());
         Block chestBlock = ChestFinder.findChestBlock(event.getBlock());
 
         if(chest == null) {
             event.getPlayer().sendMessage(ConfigManager.main.Chat_prefix + ConfigManager.language.Sign_Shop_NoChest);
             event.getBlock().breakNaturally();
             return;
         }
 
         //Get all lines and try to parse the amount
         String[] lines = event.getLines();
         Integer amount;
 
         try {
             amount = Integer.parseInt(lines[1]);
         } catch(NumberFormatException e) {
             event.getPlayer().sendMessage(ConfigManager.main.Chat_prefix + ConfigManager.language.Sign_Shop_InvalidAmount);
             event.getBlock().breakNaturally();
             return;
         }
 
         //Parse the third line (<sell>:<buy>)
         Matcher matcher = pattern.matcher(lines[2]);
         Float buy = 0.0F, sell = 0.0F;
         if(matcher.find()) {
             try {
                 sell = Float.parseFloat(matcher.group(1));
                 buy = Float.parseFloat(matcher.group(2));
             } catch(NumberFormatException e) {
                 event.getPlayer().sendMessage(ConfigManager.main.Chat_prefix + ConfigManager.language.Sign_Shop_InvalidBuySell);
                 event.getBlock().breakNaturally();
                 return;
             }
         } else {
             event.getPlayer().sendMessage(ConfigManager.main.Chat_prefix + ConfigManager.language.Sign_Shop_NoValidBuySellLine);
             event.getBlock().breakNaturally();
             return;
         }
 
         //Check if Chest has items in it
         Boolean hasItem = false;
         ListIterator<ItemStack> itemStackListIterator = chest.iterator();
 
         while(itemStackListIterator.hasNext()) {
             ItemStack itemStack = itemStackListIterator.next();
             if(itemStack == null) continue;
             hasItem = true;
             break;
         }
 
         if(!hasItem) {
             event.getPlayer().sendMessage(ConfigManager.main.Chat_prefix + ConfigManager.language.Sign_Shop_ChestIsEmpty);
             event.getBlock().breakNaturally();
             return;
         }
 
         //Store the Chest
         com.geNAZt.RegionShop.Database.Model.Chest.store(event.getPlayer(), event.getBlock(), chestBlock, event.getPlayer().getWorld());
         com.geNAZt.RegionShop.Database.Table.Chest chest1 = com.geNAZt.RegionShop.Database.Model.Chest.get(chestBlock, event.getPlayer().getWorld(), false);
 
         //Store the Items
         itemStackListIterator = chest.iterator();
         Integer firstID = null;
         Byte firstData = null;
         Integer itemAmount = 0;
         ItemStack firstItemStack = null;
         while(itemStackListIterator.hasNext()) {
             ItemStack itemStack = itemStackListIterator.next();
 
             if(itemStack == null) continue;
 
             if(firstID == null) {
                 firstID = itemStack.getTypeId();
                 firstData = itemStack.getData().getData();
                 firstItemStack = itemStack;
 
                 if(Add.add(itemStack, event.getPlayer(), chest1, sell, buy, amount) == -1) {
                     event.getBlock().breakNaturally();
                     return;
                 }
 
                 itemAmount += itemStack.getAmount();
 
                 continue;
             }
 
             if(itemStack.getTypeId() == firstID && itemStack.getData().getData() == firstData) {
                 itemAmount += itemStack.getAmount();
             }
         }
 
         Items item = chest1.getItemStorage().getItems().iterator().next();
         item.setCurrentAmount(itemAmount);
         Database.getServer().update(item);
 
         item.getItemStorage().setItemAmount(itemAmount);
         Database.getServer().update(item.getItemStorage());
 
         //Create an itemdrop over the chest
         ItemStack itemStack = firstItemStack.clone();
         itemStack.setAmount(1);
         org.bukkit.entity.Item droppedItem = event.getPlayer().getWorld().dropItem(new Location(event.getPlayer().getWorld(), (double) chest1.getChestX() + 0.5, (double)chest1.getChestY() + 1.2, (double)chest1.getChestZ() + 0.5), itemStack);
         droppedItem.setVelocity(new Vector(0, 0.1, 0));
         NMS.safeGuard(droppedItem);
 
         //Change the Sign
         //Get the nice name
         String itemName = ItemName.getDataName(firstItemStack) + firstItemStack.getType().toString();
         if (firstItemStack.getItemMeta().hasDisplayName()) {
             itemName = "(" + firstItemStack.getItemMeta().getDisplayName() + ")";
         }
 
         for(Integer line = 0; line < 4; line++) {
             event.setLine(line, ConfigManager.language.Sign_Shop_SignText.get(line).
                     replace("%player",  event.getPlayer().getName()).
                     replace("%itemname", ItemName.nicer(itemName)).
                     replace("%amount", amount.toString()).
                     replace("%sell", sell.toString()).
                     replace("%buy", buy.toString()));
         }
     }
 
     public static boolean checkForShop(SignChangeEvent event) {
         Matcher matcher = pattern.matcher(event.getLine(2));
         Matcher matcher1 = intPattern.matcher(event.getLine(1));
 
         return (event.getLine(0).equals("") || //If first line is blank thats ok
                 event.getLine(0).toLowerCase().contains(event.getPlayer().getName().toLowerCase())) && //If first line contains the owner name thats ok
                 matcher1.matches() && //The second line must be a Integer
                 matcher.matches(); //The third line mus match the Regex
     }
 }
