 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package me.xhawk87.Coinage.commands;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeMap;
 import me.xhawk87.Coinage.Coinage;
 import me.xhawk87.Coinage.Currency;
 import me.xhawk87.Coinage.Denomination;
 import org.bukkit.Location;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 
 /**
  *
  * @author XHawk87
  */
 public class SplitCoinsCommand extends CoinCommand {
 
     private Coinage plugin;
 
     public SplitCoinsCommand(Coinage plugin) {
         this.plugin = plugin;
     }
 
     @Override
     public String getHelpMessage(CommandSender sender) {
         return "/SplitCoins ([player]) [denomination=amount...]. Split the held coins into a specified number of smaller denominations. The denomination should be the id, not the display name or short name and the amount should be a whole number of coins. You can include as many denominations as you wish, so long as the total is less than or equal to the total value of the held coins.";
     }
 
     @Override
     public String getPermission() {
         return "coinage.commands.splitcoins";
     }
 
     @Override
     public boolean execute(CommandSender sender, String[] args) {
         if (args.length < 1) {
             return false;
         }
 
         int index = 0;
         String firstArg = args[index++];
         Player player;
         Map<Denomination, Integer> into = new TreeMap<>();
         if (firstArg.contains("=")) {
             if (sender instanceof Player) {
                 player = (Player) sender;
             } else {
                 sender.sendMessage("Console must specify a player");
                 return true;
             }
 
             index--; // The first arg is a denomination so check it again
         } else {
             if (args.length < 2) {
                 return false; // There must be at least 1 denomination
             }
             player = plugin.getServer().getPlayer(firstArg);
             if (player == null) {
                 sender.sendMessage("There is no player matching " + firstArg);
                 return true;
             }
         }
 
         ItemStack held = player.getItemInHand();
         if (held == null || held.getTypeId() == 0) {
             if (player != sender) {
                 sender.sendMessage(player.getDisplayName() + " must be holding a coin to split");
             }
             player.sendMessage("You must be holding a coin to split");
             return true;
         }
         Denomination heldCoin = plugin.getDenominationOfCoin(held);
         Currency currency = heldCoin.getCurrency();
         int heldAmount = held.getAmount();
         int heldValue = heldCoin.getValue() * heldAmount;
         int splitValue = 0;
        for (; index < args.length; index++) {
             String arg = args[index];
             String[] parts = arg.split("=");
             if (parts.length != 2) {
                 sender.sendMessage("Denominations to split into should be in form denomId=amount: " + arg);
                 return true;
             }
             String denomName = parts[0];
             String amountString = parts[1];
 
             Denomination denomination = currency.getDenominationByName(denomName);
             if (denomination == null) {
                 sender.sendMessage("There is no denomination of " + currency.toString() + " with id " + denomName);
                 return true;
             }
             int amount;
             try {
                 amount = Integer.parseInt(amountString);
                 if (amount < 1) {
                     sender.sendMessage("There must be at least one coin of each denomination: " + amountString);
                     return true;
                 }
             } catch (NumberFormatException ex) {
                 sender.sendMessage("The number of coins was not a valid number: " + amountString);
                 return true;
             }
 
             splitValue += denomination.getValue() * amount;
             if (into.put(denomination, amount) != null) {
                 sender.sendMessage("Duplicate denomination to split: " + denomName);
                 return true;
             }
         }
 
         if (splitValue > heldValue) {
             if (sender != player) {
                 sender.sendMessage(player.getDisplayName() + " cannot split their " + heldAmount + " x " + heldCoin.toString() + " as their value does not equal or exceed the split " + splitValue);
             }
             player.sendMessage("Your held coins only amount to " + heldValue + " " + currency.toString() + " but you need " + splitValue + " " + currency.toString() + " to make this split");
             return true;
         }
 
         // Remove the held coins
         player.getInventory().clear(player.getInventory().getHeldItemSlot());
         player.sendMessage("You hand over " + heldAmount + " x " + heldCoin.toString());
 
         // Give the split coins
         List<ItemStack> toAdd = new ArrayList<>();
         for (Map.Entry<Denomination, Integer> entry : into.entrySet()) {
             Denomination denomination = entry.getKey();
             int amount = entry.getValue();
             toAdd.add(denomination.create(amount));
             player.sendMessage("You receive " + amount + " x " + denomination.toString());
         }
         HashMap<Integer, ItemStack> toDrop = player.getInventory().addItem(toAdd.toArray(new ItemStack[toAdd.size()]));
         Location loc = player.getLocation();
         for (ItemStack drop : toDrop.values()) {
             loc.getWorld().dropItem(loc, drop);
         }
 
         // Give change
         int change = heldValue - splitValue;
         if (change > 0) {
             currency.give(player.getInventory(), change);
             player.sendMessage("You receive " + change + " in " + currency.toString());
         }
 
         return true;
     }
 }
