 package com.tehbeard.BeardStat.commands;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 
 import com.tehbeard.BeardStat.BeardStat;
 import com.tehbeard.BeardStat.BeardStatRuntimeException;
 import com.tehbeard.BeardStat.containers.EntityStatBlob;
 import com.tehbeard.BeardStat.containers.IStat;
 import com.tehbeard.BeardStat.containers.OnlineTimeManager;
 import com.tehbeard.BeardStat.containers.PlayerStatManager;
 import com.tehbeard.BeardStat.containers.StatVector;
 import com.tehbeard.BeardStat.utils.LanguagePack;
 import com.tehbeard.BeardStat.utils.StatisticMetadata;
 
 /**
  * /played - Show users playtime /played name - show player of name
  * 
  * @author James
  * 
  */
 public class playedCommand extends BeardStatCommand {
 
     public playedCommand(PlayerStatManager playerStatManager, BeardStat plugin) {
         super(playerStatManager, plugin);
     }
 
    private PlayerStatManager playerStatManager;
    private BeardStat         plugin;

     @Override
     public boolean onCommand(CommandSender sender, Command command, String cmdLabel, String[] args) {
         try {
 
             int seconds = 0;
             EntityStatBlob blob;
 
             // If sender is a player, default to them
             OfflinePlayer selectedPlayer = (sender instanceof OfflinePlayer) ? (OfflinePlayer) sender : null;
 
             // We got an argument, use that player instead
             if ((args.length == 1) && sender.hasPermission(BeardStat.PERM_COMMAND_PLAYED_OTHER)) {
                 selectedPlayer = Bukkit.getOfflinePlayer(args[0]).hasPlayedBefore() ? Bukkit.getOfflinePlayer(args[0])
                         : null;
             }
 
             // failed to get a player, send error and finish
             if (selectedPlayer == null) {
                 sender.sendMessage(ChatColor.RED + LanguagePack.getMsg("command.error.noconsole.noargs"));
                 return true;
             }
 
             // Grab player blob and format out stat
             // TODO: async this
             blob = this.playerStatManager.getPlayerBlob(selectedPlayer.getName());
             if (blob == null) {
                 sender.sendMessage(ChatColor.RED + LanguagePack.getMsg("command.error.noplayer", args[0]));
                 return true;
             }
             StatVector vector = blob.getStats(BeardStat.DEFAULT_DOMAIN, "*", "stats", "playedfor");
             seconds = vector.getValue();
 
             seconds += OnlineTimeManager.getRecord(selectedPlayer.getName()).sessionTime();
             sender.sendMessage(getPlayedString(seconds) + " total");
 
             for (IStat stat : vector) {
                 sender.sendMessage(LanguagePack.getMsg("command.stat.stat", stat.getWorld(),
                         getPlayedString(stat.getValue())));
             }
         } catch (Exception e) {
             this.plugin.handleError(new BeardStatRuntimeException("An error occured running /played", e, true));
         }
 
         return true;
     }
 
     public static String getPlayedString(int seconds) {
 
         if (seconds > 0) {
             return StatisticMetadata.formatStat("playedfor", seconds);
         }
 
         return LanguagePack.getMsg("command.played.zero");
     }
 }
