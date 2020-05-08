 package net.serubin.serubans.commands;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import net.serubin.serubans.SeruBans;
 import net.serubin.serubans.util.ArgProcessing;
 import net.serubin.serubans.util.HashMaps;
 import net.serubin.serubans.util.MySqlDatabase;
 
 public class UpdateCommand implements CommandExecutor {
 
     private SeruBans plugin;
 
     public UpdateCommand(SeruBans plugin) {
         this.plugin = plugin;
     }
 
     public boolean onCommand(CommandSender sender, Command cmd,
             String commandLabel, String[] args) {
         if (commandLabel.equalsIgnoreCase("bupdate")) {
             if (sender.hasPermission(SeruBans.UPDATEPERM) || sender.isOp()
                     || (!(sender instanceof Player))) {
                 if (args.length == 0) {
                     return false;
                 } else if (args.length > 2) {
 
                 }
                 int bId;
                 try {
                     bId = Integer.parseInt(args[0]);
                 } catch (NumberFormatException ex) {
                     // Item was not an int, do nothing
                     sender.sendMessage(ChatColor.RED + "Id must be a number!");
                     return true;
                 }
                 if (!HashMaps.checkId(bId)) {
                     sender.sendMessage(ChatColor.RED + Integer.toString(bId)
                             + " is not a valid ban id.");
                     return false;
                 }
                 StringBuilder reasonRaw = new StringBuilder();
                 String reason;
                 // combine args into a string
                 for (String s : args) {
                     reasonRaw.append(" " + s);
                 }
                reason = reasonRaw.toString().replace(args[0], "");
 
                 MySqlDatabase.updateReason(bId, reason);
                 sender.sendMessage(ChatColor.GREEN + "Reason for Id " + bId
                         + " changed to '" + reason + "'");
                 plugin.printInfo(sender.getName()
                         + " updated reason of ban number "
                         + Integer.toString(bId) + " to " + reason);
                 return true;
             } else {
             sender.sendMessage(ChatColor.RED + "You do not have permission!");
             return true;
             }
         }
         return false;
     }
 
 }
