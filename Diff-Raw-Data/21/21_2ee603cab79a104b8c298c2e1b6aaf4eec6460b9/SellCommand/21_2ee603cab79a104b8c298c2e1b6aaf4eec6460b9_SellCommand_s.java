 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package dev.xyzcraft.net.xyzshop.commands;
 
 import dev.xyzcraft.net.xyzshop.XYZShop;
 import dev.xyzcraft.net.xyzshop.dataObject.MSItem;
 import dev.xyzcraft.net.xyzshop.dataObject.MSTransaction;
 import dev.xyzcraft.net.xyzshop.dataObject.MSTransactionType;
 import dev.xyzcraft.net.xyzshop.util.MacCommand;
 import dev.xyzcraft.net.xyzshop.util.MacCommandStatus;
import java.util.HashSet;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.PlayerInventory;
 
 /**
  *
  * @author Joey
  */
 public class SellCommand extends MacCommand{
     public SellCommand(XYZShop pl) {
          super(pl);
     }
     @Override
     public MacCommandStatus onCommand(Player cs, Command cmnd, String label, String[] args) {
         if (args.length < 1) {
             cs.sendMessage(ChatColor.RED + "Usage: " + ChatColor.DARK_RED + "/sell <itemID|hand|inv> [quantityOfItemID|quantityOfHand]");
             return MacCommandStatus.SUCESSFUL;
         }
         String sellType = args[0];
         PlayerInventory inv = cs.getInventory();
         if (sellType.equals("inv") || sellType.equals("all") || sellType.equals("inventory")) {
             ItemStack[] invItems = inv.getContents();
             float total = 0;
             for (ItemStack itemStack1 : invItems) {
                 if (itemStack1 == null) {
                     continue;
                 }
                 MSItem item = this.plugin.database.getItem(itemStack1.getTypeId(), itemStack1.getDurability());
                 if (item == null || item.itemSellPrice == 0) {
                     continue;
                 }
                 MSTransaction ts = new MSTransaction(itemStack1.getAmount(),item,MSTransactionType.SELL,cs);
                 if (ts.getPrice() < 0.01) {
                     continue;
                 }
                 if (plugin.shop.commit(ts)) {
                     total += ts.getPrice();
                 }
             }
             if (total == 0) {
                 cs.sendMessage(ChatColor.RED + "Nothing in your inventory could be sold.");
                 return MacCommandStatus.SUCESSFUL;
             }
             cs.sendMessage(ChatColor.GREEN + "$" + String.format("%.2f", total) + " added to your account!");
             return MacCommandStatus.SUCESSFUL;
         }
         else if (sellType.equals("hand") || sellType.equals("this")) {
             ItemStack hand = inv.getItemInHand();
             MSItem item = this.plugin.database.getItem(hand.getTypeId(), hand.getDurability());
             if (item == null) {
                 cs.sendMessage(ChatColor.RED + "This item cannot be sold.");
                 return MacCommandStatus.SUCESSFUL;
             }
             if (item.itemSellPrice == 0) {
                 cs.sendMessage(ChatColor.RED + "This item cannot be sold.");
                 return MacCommandStatus.SUCESSFUL;
             }
             MSTransaction ts = new MSTransaction(hand.getAmount(),item,MSTransactionType.SELL,cs);
             if (ts.getPrice() < 0.01) {
                cs.sendMessage(ChatColor.RED + "You need to have more of this item to sell it. Do /worth for info on this object");
                return MacCommandStatus.SUCESSFUL;
             }
             if (plugin.shop.commit(ts)) {
                 cs.sendMessage(ChatColor.GREEN + "$" + String.format("%.2f", ts.getPrice()) + " added to your account!");
             }
             else {
                cs.sendMessage(ChatColor.RED + "This item cannot be sold.");
             }
             return MacCommandStatus.SUCESSFUL;
         }
         else {
             return MacCommandStatus.UNSUPPORTED;
         }
         //return MacCommandStatus.SUCESSFUL;
     }
     
 }
