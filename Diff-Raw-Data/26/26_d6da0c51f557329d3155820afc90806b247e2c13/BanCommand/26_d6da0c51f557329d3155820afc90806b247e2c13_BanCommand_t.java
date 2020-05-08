 package net.serubin.serubans.commands;
 
 import net.serubin.serubans.SeruBans;
 import net.serubin.serubans.util.ArgProcessing;
 import net.serubin.serubans.util.CheckPlayer;
 import net.serubin.serubans.util.HashMaps;
 import net.serubin.serubans.util.MySqlDatabase;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.Server;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 public class BanCommand implements CommandExecutor {
 
     private String BanMessage;
     private String GlobalBanMessage;
     private String name;
     private SeruBans plugin;
 
     public BanCommand(String BanMessage, String GlobalBanMessage, String name,
             SeruBans plugin) {
         this.BanMessage = BanMessage;
         this.GlobalBanMessage = GlobalBanMessage;
         this.name = name;
         this.plugin = plugin;
     }
 
     public boolean onCommand(CommandSender sender, Command cmd,
             String commandLabel, String[] args) {
         Player victim;
         String mod;
         String reason;
         int display = 0;
         if (commandLabel.equalsIgnoreCase("ban")) {
             if (sender.hasPermission(SeruBans.BANPERM) || sender.isOp()
                     || (!(sender instanceof Player))) {
                 if (args.length == 0) {
                     return false;
                 } else if (args.length > 1) {
                     reason = ArgProcessing.reasonArgs(args);
                 } else {
                     reason = "undefined";
                 }
 
                 mod = sender.getName();
                 victim = plugin.getServer().getPlayer(args[0]);
 
                 String line = "";
                 if (victim != null) {
                     // adds player to db
                     CheckPlayer.checkPlayer(victim, sender);
                     if (!HashMaps.keyIsInBannedPlayers(victim.getName())) {
                         MySqlDatabase.addBan(victim.getName(), SeruBans.BAN, 0,
                                 mod, reason, SeruBans.SHOW);
                         // kicks and broadcasts message
                         SeruBans.printServer(ArgProcessing.GlobalMessage(
                                 GlobalBanMessage, reason, mod, victim.getName()));
                         plugin.log.info(mod + " banned " + victim.getName()
                                 + " for " + reason);
                         victim.kickPlayer(ArgProcessing.GetColor(ArgProcessing
                                 .PlayerMessage(BanMessage, reason, mod)));
                         return true;
                     } else {
                         sender.sendMessage(ChatColor.GOLD
                                 + victim.getName()
                                 + ChatColor.RED
                                 + " is already banned! Also, This player is banned and on your server... Might want to look into that.");
                         return true;
                     }
                 } else {
                     // broadcasts message
                     CheckPlayer.checkPlayerOffline(args[0], sender);
                     if (!HashMaps.keyIsInBannedPlayers(args[0])) {
                        MySqlDatabase.addBan(args[0], SeruBans.BAN, 0, mod, reason, display);
                         SeruBans.printServer(ArgProcessing.GlobalMessage(
                                 GlobalBanMessage, reason, mod, args[0]));
                         plugin.log.info(mod + " banned " + args[0] + " for "
                                 + reason);
                         return true;
                     } else {
                         sender.sendMessage(ChatColor.GOLD + args[0]
                                 + ChatColor.RED + " is already banned!");
                         return true;
                     }
 
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
