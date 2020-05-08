 package net.serubin.serubans.commands;
 
 import java.awt.Color;
 
 import net.serubin.serubans.SeruBans;
 import net.serubin.serubans.util.ArgProcessing;
 import net.serubin.serubans.util.CheckPlayer;
 import net.serubin.serubans.util.MySqlDatabase;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.Server;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 public class WarnCommand implements CommandExecutor {
 
     private String WarnMessage;
     private String name;
     private SeruBans plugin;
     private String WarnPlayerMessage;
 
     public WarnCommand(String WarnMessage, String WarnPlayerMessage,
             String name, SeruBans plugin) {
         this.WarnMessage = WarnMessage;
         this.WarnPlayerMessage = WarnPlayerMessage;
         this.name = name;
         this.plugin = plugin;
     }
 
     public boolean onCommand(CommandSender sender, Command cmd,
             String commandLabel, String[] args) {
         Player victim;
         String mod;
         String reason = "";
         int display = SeruBans.SHOW;
         boolean silent = false;
 
         if (commandLabel.equalsIgnoreCase("warn")) {
             if (sender.hasPermission(SeruBans.WARNPERM) || sender.isOp()
                     || (!(sender instanceof Player))) {
                 silent = false;
                 silent = false;
                 display = SeruBans.SHOW;
                 if (args[0].startsWith("-")) {
                     if (args[0].contains("s")) {
                         silent = true;
                     }
                     if (args[0].contains("h")) {
                         display = SeruBans.HIDE;
                     }
                     args = ArgProcessing.stripFirstArg(args);
                 }
                 if (args.length == 0) {
                     return false;
                 } else if (args.length > 1) {
                     reason = ArgProcessing.reasonArgs(args);
                 } else {
                     reason = "undefined";
                 }
                 mod = sender.getName();
                 victim = plugin.getServer().getPlayer(args[0]);
                 // processes Warn message
 
                 String line = "";
                 if (victim != null) {
                     // checks player for id in database
                     CheckPlayer.checkPlayer(victim, sender);
                     // adds ban to database
                     MySqlDatabase.addBan(victim.getName(), SeruBans.WARN, 0,
                             mod, reason, display);
                     // prints to players on server with perms
                     SeruBans.printServer(ArgProcessing.GlobalMessage(
                             WarnMessage, reason, mod, victim.getName()), silent);
                     // logs i t
                     plugin.log.info(mod + " warned " + victim.getName()
                             + " for " + reason);
                     // tells victim
                     victim.sendMessage(ArgProcessing.GetColor(ArgProcessing
                             .PlayerMessage(WarnPlayerMessage, reason, mod)));
                     // sends kicker ban id
                     sender.sendMessage(ChatColor.GOLD + "Ban Id: "
                             + ChatColor.YELLOW
                             + Integer.toString(MySqlDatabase.getLastBanId()));
 
                     // adds player to db
                     return true;
                 } else {
                     // checks player for id in database
                     CheckPlayer.checkPlayerOffline(args[0], sender);
                     // adds ban to database
                     MySqlDatabase.addBan(args[0], SeruBans.WARN, 0, mod,
                             reason, display);
                     // prints to players on server with perms
                     SeruBans.printServer(ArgProcessing.GlobalMessage(
                             WarnMessage, reason, mod, args[0]), silent);
                     // logs it
                     plugin.log.info(mod + " warned " + args[0] + " for "
                             + reason);
                     // sends kicker ban id
                     sender.sendMessage(ChatColor.GOLD + "Ban Id: "
                             + ChatColor.YELLOW
                             + Integer.toString(MySqlDatabase.getLastBanId()));
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
