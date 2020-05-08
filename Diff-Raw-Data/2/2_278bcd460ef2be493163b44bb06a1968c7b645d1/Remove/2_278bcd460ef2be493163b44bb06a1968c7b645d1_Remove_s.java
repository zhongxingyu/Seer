 package com.geNAZt.RegionShop.Interface.CLI.Commands;
 
 import com.geNAZt.RegionShop.Config.ConfigManager;
 import com.geNAZt.RegionShop.Database.Database;
 import com.geNAZt.RegionShop.Database.Model.Item;
 import com.geNAZt.RegionShop.Database.Model.Transaction;
 import com.geNAZt.RegionShop.Database.Table.Items;
 import com.geNAZt.RegionShop.Database.Table.Region;
 import com.geNAZt.RegionShop.Interface.CLI.CLICommand;
 import com.geNAZt.RegionShop.Interface.CLI.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * Created for YEAHWH.AT
  * User: geNAZt (fabian.fassbender42@googlemail.com)
  * Date: 06.06.13
  */
 public class Remove implements CLICommand {
     @Command(command="shop remove", arguments=1, helpKey="Command_Remove_HelpText", helpPage="owner", permission="rs.command.remove")
     public static void remove(CommandSender sender, String[] args) {
         //Check if sender is a player
         if(!(sender instanceof Player)) {
             sender.sendMessage(ConfigManager.main.Chat_prefix + ConfigManager.language.Command_OnlyForPlayers);
             return;
         }
 
         Player player = (Player) sender;
 
         Integer shopItemId = 0;
 
         try {
             shopItemId = Integer.parseInt(args[0]);
         } catch (NumberFormatException e) {
             player.sendMessage(ConfigManager.main.Chat_prefix + ConfigManager.language.Command_Remove_InvalidArguments);
             return;
         }
 
         Items item = Database.getServer().find(Items.class).
                     where().
                         eq("id", shopItemId).
                     findUnique();
 
         if(item != null) {
             if(!item.getOwner().equals(player.getName()) && !player.hasPermission("rs.bypass.remove")) {
                 player.sendMessage(ConfigManager.main.Chat_prefix + ConfigManager.language.Command_Remove_NotYourItem);
                 return;
             }
 
             ItemStack iStack = Item.fromDBItem(item);
             iStack.setAmount(item.getCurrentAmount());
 
             Region region = item.getItemStorage().getRegions().iterator().next();
 
             HashMap<Integer, ItemStack> notFitItems = player.getInventory().addItem(iStack);
             if (!notFitItems.isEmpty()) {
                 for(Map.Entry<Integer, ItemStack> notFitItem : notFitItems.entrySet()) {
                     item.setCurrentAmount(item.getCurrentAmount() - notFitItem.getValue().getAmount());
                 }
 
                 Database.getServer().update(item);
 
                 Transaction.generateTransaction(player, com.geNAZt.RegionShop.Database.Table.Transaction.TransactionType.REMOVE, region.getRegion(), region.getWorld(), item.getOwner(), iStack.getTypeId(), iStack.getAmount(), item.getSell().doubleValue(), item.getBuy().doubleValue(), item.getUnitAmount());
 
                 player.sendMessage(ConfigManager.main.Chat_prefix + ConfigManager.language.Command_Remove_NotAllItemsFit);
                 return;
             }
 
             Database.getServer().delete(item);
 
             Transaction.generateTransaction(player, com.geNAZt.RegionShop.Database.Table.Transaction.TransactionType.REMOVE, region.getRegion(), region.getWorld(), item.getOwner(), iStack.getTypeId(), iStack.getAmount(), item.getSell().doubleValue(), item.getBuy().doubleValue(), item.getUnitAmount());
             player.sendMessage(ConfigManager.main.Chat_prefix + ConfigManager.language.Command_Remove_Success);
         } else {
             player.sendMessage(ConfigManager.main.Chat_prefix + ConfigManager.language.Command_Remove_NotFound);
         }
     }
 }
