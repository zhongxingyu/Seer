 package com.tehbeard.beardstat.commands;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 
 import com.tehbeard.beardstat.BeardStat;
 import com.tehbeard.beardstat.BeardStatRuntimeException;
 import com.tehbeard.beardstat.dataproviders.IStatDataProvider;
 import com.tehbeard.beardstat.containers.EntityStatBlob;
 import com.tehbeard.beardstat.containers.IStat;
 import com.tehbeard.beardstat.manager.OnlineTimeManager;
 import com.tehbeard.beardstat.manager.EntityStatManager;
 import com.tehbeard.beardstat.containers.StatVector;
 import com.tehbeard.beardstat.utils.LanguagePack;
 
 /**
  * /played - Show users playtime /played name - show player of name
  * 
  * @author James
  * 
  */
 public class playedCommand extends BeardStatCommand {
 
     public playedCommand(EntityStatManager playerStatManager, BeardStat plugin) {
         super(playerStatManager, plugin);
     }
 
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
             blob = this.playerStatManager.getBlobByNameType(selectedPlayer.getName(), IStatDataProvider.PLAYER_TYPE).getValue();
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
 
     public String getPlayedString(int seconds) {
 
         if (seconds > 0) {
             return playerStatManager.formatStat("playedfor", seconds);
         }
 
         return LanguagePack.getMsg("command.played.zero");
     }
 }
