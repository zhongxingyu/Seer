 package net.serubin.serubans.commands;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import net.serubin.serubans.SeruBans;
 import net.serubin.serubans.util.ArgProcessing;
 import net.serubin.serubans.util.CheckPlayer;
 import net.serubin.serubans.util.HashMaps;
 import net.serubin.serubans.util.MySqlDatabase;
 
 public class TempBanCommand implements CommandExecutor {
 
     private String tempBanMessage;
     private String globalTempBanMessage;
     private SeruBans plugin;
 
     public TempBanCommand(String tempBanMessage, String globalTempBanMessage,
             String name, SeruBans plugin) {
         this.tempBanMessage = tempBanMessage;
         this.globalTempBanMessage = globalTempBanMessage;
         this.plugin = plugin;
     }
 
     public boolean onCommand(CommandSender sender, Command cmd,
             String commandLabel, String[] args) {
         String reason;
         Player victim;
         String mod;
         int display = 0;
         if (commandLabel.equalsIgnoreCase("tempban")) {
             if (sender.hasPermission(SeruBans.TEMPBANPERM) || sender.isOp()
                     || (!(sender instanceof Player))) {
                 if (args.length == 0) {
                     return false;
                 } else if (args.length > 3) {
                     reason = ArgProcessing.reasonArgsTB(args);
                 } else {
                     reason = "undefined";
                 }
 
                 mod = sender.getName();
                 victim = plugin.getServer().getPlayer(args[0]);
 
                 if (victim != null) {
                     // adds player to db
                     CheckPlayer.checkPlayer(victim, sender);
                     if (!HashMaps.keyIsInBannedPlayers(victim.getName())) {
                         long length = ArgProcessing.parseTimeSpec(args[1],
                                 args[2]);
                         plugin.printDebug(Long.toString(length));
                         if (length == 0)
                             return false;
                         length = System.currentTimeMillis()/1000 + length;
                        MySqlDatabase.addBan(victim.getName(), 2, length, mod,
                                 reason, display);
                         // kicks and broadcasts message
 
                         String date = ArgProcessing.getStringDate(length);
                         SeruBans.printServer(ArgProcessing
                                 .GlobalTempBanMessage(globalTempBanMessage,
                                         reason, mod, victim.getName(), date));
                         plugin.log.info(mod + " banned " + victim.getName()
                                 + " for " + reason);
                         victim.kickPlayer(ArgProcessing.GetColor(ArgProcessing
                                 .PlayerTempBanMessage(tempBanMessage, reason,
                                         mod, date)));
                         return true;
                     } else {
                         // TODO fix spelling
                         sender.sendMessage(ChatColor.GOLD
                                 + victim.getName()
                                 + ChatColor.RED
                                 + " This player is banned and on your server, Please inform your admin imidiatly!");
                         return true;
                     }
                 } else {
                     // broadcasts message
                     CheckPlayer.checkPlayerOffline(args[0], sender);
                     if (!HashMaps.keyIsInBannedPlayers(args[0])) {
                         long length = ArgProcessing.parseTimeSpec(args[1],
                                 args[2]);
                         if (length == 0)
                             return false;
                         length = System.currentTimeMillis() / 1000 + length;
 
                         MySqlDatabase.addBan(args[0], SeruBans.TEMPBAN, length,
                                 mod, reason, display);
                         SeruBans.printServer(ArgProcessing.GlobalMessage(
                                 globalTempBanMessage, reason, mod, args[0]));
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
             }
         }
         return false;
     }
 
 }
