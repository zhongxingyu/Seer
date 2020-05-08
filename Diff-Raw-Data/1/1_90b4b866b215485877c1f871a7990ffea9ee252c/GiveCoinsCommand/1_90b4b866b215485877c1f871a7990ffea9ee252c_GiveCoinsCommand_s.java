 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package me.xhawk87.Coinage.commands;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import me.xhawk87.Coinage.Coinage;
 import me.xhawk87.Coinage.Currency;
 import me.xhawk87.Coinage.Denomination;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.PlayerInventory;
 
 /**
  *
  * @author XHawk87
  */
 public class GiveCoinsCommand extends CoinCommand {
 
     private Coinage plugin;
 
     public GiveCoinsCommand(Coinage plugin) {
         this.plugin = plugin;
     }
 
     @Override
     public String getHelpMessage(CommandSender sender) {
         return "/GiveCoins [denomination] [amount] - Gives the specified number of coins in the default currency to the player issuing the command\n"
                 + "/GiveCoins [currency] [denomination] [amount] - Gives the specified number of coins of the given currency to the player issuing the command\n"
                 + "/GiveCoins [player] [denomination] [amount] - Gives the specified number of coins in the default currency to the given player\n"
                 + "/GiveCoins [player] [currency] [denomination] [amount] - Gives the specified number of coins of the given currency to the given player";
     }
 
     @Override
     public String getPermission() {
         return "coinage.commands.givecoins";
     }
 
     @Override
     public boolean execute(CommandSender sender, String[] args) {
         if (args.length < 2 || args.length > 4) {
             return false;
         }
 
         int index = 0;
         Player player = null;
         Currency currency;
         Denomination denomination;
         int totalAmount;
         if (args.length >= 3) {
             String firstArg = args[index++];
             if (args.length == 4) {
                 player = plugin.getServer().getPlayer(firstArg);
                 if (player == null) {
                     sender.sendMessage("There is no player matching " + firstArg);
                     return true;
                 }
                 String currencyName = args[index++];
                 currency = plugin.getCurrency(currencyName);
                 if (currency == null) {
                     sender.sendMessage("There is no currency with name " + currencyName);
                     return true;
                 }
             } else {
                 currency = plugin.getCurrency(firstArg);
                 if (currency == null) {
                     player = plugin.getServer().getPlayer(firstArg);
                     if (player == null) {
                         sender.sendMessage("There is no matching player or currency with name " + firstArg);
                         return true;
                     }
                 }
             }
         } else {
             currency = plugin.getDefaultCurrency();
         }
 
         if (player == null) {
             if (sender instanceof Player) {
                 player = (Player) sender;
             } else {
                 sender.sendMessage("The console must specify a player");
                 return true;
             }
         }
 
         String denomName = args[index++];
         denomination = currency.getDenominationByName(denomName);
         if (denomination == null) {
             sender.sendMessage(currency.toString() + " has no " + denomName + " denomination");
             return true;
         }
 
         String amountString = args[index++];
         try {
             totalAmount = Integer.parseInt(amountString);
             if (totalAmount < 1) {
                 sender.sendMessage("The amount must be a positive number");
                 return true;
             }
         } catch (NumberFormatException ex) {
             sender.sendMessage("The amount must be a valid number: " + amountString);
             return true;
         }
 
         PlayerInventory inv = player.getInventory();
         List<ItemStack> stacks = new ArrayList<>();
         int maxStackSize = Math.min(inv.getMaxStackSize(), denomination.getMaxStackSize());
         int amount = totalAmount;
         while (amount > maxStackSize) {
             ItemStack coins = denomination.create(maxStackSize);
             amount -= coins.getAmount();
             stacks.add(coins);
         }
         ItemStack coins = denomination.create(amount);
         stacks.add(coins);
 
         HashMap<Integer, ItemStack> drops = inv.addItem(stacks.toArray(new ItemStack[stacks.size()]));
         for (ItemStack toDrop : drops.values()) {
             player.getWorld().dropItem(player.getLocation(), toDrop);
         }
         sender.sendMessage("Given " + totalAmount + " " + denomination.toString() + " to " + player.getDisplayName());
         player.sendMessage("You have received " + totalAmount + " x " + denomination.toString());
         return true;
     }
 }
