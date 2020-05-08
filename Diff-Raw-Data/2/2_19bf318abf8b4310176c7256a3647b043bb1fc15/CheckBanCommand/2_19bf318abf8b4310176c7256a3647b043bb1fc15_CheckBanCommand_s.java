 package net.serubin.serubans.commands;
 
 import net.serubin.serubans.SeruBans;
 import net.serubin.serubans.util.HashMaps;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 public class CheckBanCommand implements CommandExecutor {
 
     private SeruBans plugin;
 
     public CheckBanCommand(SeruBans plugin) {
         this.plugin = plugin;
     }
 
     public boolean onCommand(CommandSender sender, Command cmd,
             String commandLabel, String[] args) {
 
         if (commandLabel.equalsIgnoreCase("checkban")) {
             if (sender.hasPermission(SeruBans.CHECKBANPERM) || sender.isOp()
                     || (!(sender instanceof Player))) {
                String player = args[0]toLowerCase();
                 boolean isBanned = HashMaps.keyIsInBannedPlayers(player);
 
                 if (isBanned) {
                     int id = HashMaps.getBannedPlayers(player);
                     sender.sendMessage(ChatColor.RED + args[0] + " is banned.");
                     sender.sendMessage(ChatColor.RED + "Ban id: "
                             + ChatColor.YELLOW + id);
                     return true;
                 } else {
                     sender.sendMessage(ChatColor.GREEN + args[0]
                             + " is not banned.");
                     return true;
                 }
             } else {
                 sender.sendMessage(ChatColor.RED
                         + "You do not have permission!");
                 return true;
             }
         }
         return false;
     }
 
 }
