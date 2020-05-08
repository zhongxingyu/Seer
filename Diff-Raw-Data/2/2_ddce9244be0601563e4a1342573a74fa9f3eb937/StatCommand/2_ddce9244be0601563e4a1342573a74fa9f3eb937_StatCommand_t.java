 package com.tehbeard.BeardStat.commands;
 
 import java.util.Iterator;
 import java.util.Stack;
 
 import me.tehbeard.utils.commands.ArgumentPack;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import com.tehbeard.BeardStat.BeardStat;
 import com.tehbeard.BeardStat.BeardStatRuntimeException;
 import com.tehbeard.BeardStat.containers.EntityStatBlob;
 import com.tehbeard.BeardStat.containers.IStat;
 import com.tehbeard.BeardStat.containers.PlayerStatManager;
 import com.tehbeard.BeardStat.containers.StatVector;
 import com.tehbeard.BeardStat.utils.LanguagePack;
 import com.tehbeard.BeardStat.utils.StatisticMetadata;
 
 /**
  * Show stats for a player,
  * 
  * @author James
  * 
  */
 public class StatCommand implements CommandExecutor {
 
     private PlayerStatManager playerStatManager;
 
     public StatCommand(PlayerStatManager playerStatManager) {
         this.playerStatManager = playerStatManager;
     }
 
     @Override
     public boolean onCommand(CommandSender sender, Command command, String cmdLabel, String[] args) {
         try {
             ArgumentPack arguments = new ArgumentPack(new String[] { "i", "h" }, new String[] { "p", "s" }, args);
 
             String player = null;
             // Use another player
             if (sender.hasPermission(BeardStat.PERM_COMMAND_STAT_OTHER)) {
                 player = arguments.getOption("p");
             }
 
             // Else use this player
             if ((player == null) && (sender instanceof Player)) {
                 player = ((Player) sender).getName();
             }
 
             // not a player and no player picked? show the help message
             if ((player == null) || arguments.getFlag("h")) {
                 sendHelpMessage(sender);
                 return true;
             }
 
             if (arguments.getFlag("i")) {
                 sender.sendMessage("Interactive mode has been removed at this time.");
                 return true;
             }
 
             if (arguments.getOption("s") != null) {
 
                 Stack<String> stat = new Stack<String>();
                 for (String s : arguments.getOption("s").split("\\:\\:")) {
                     stat.add(s);
                 }
 
                 String statistic = !stat.isEmpty() ? stat.pop() : null;
                 String category = !stat.isEmpty() ? stat.pop() : null;
                 String world = !stat.isEmpty() ? stat.pop() : ".*";
                 String domain = !stat.isEmpty() ? stat.pop() : ".*";
 
                 EntityStatBlob blob = this.playerStatManager.findPlayerBlob(player);
                 if (blob == null) {
                     sender.sendMessage(LanguagePack.getMsg("command.error.noplayer", player));
                     return true;
                 }
 
                 StatVector vector = blob.getStats(domain, world, category, statistic, true);
 
                 if (vector.size() == 0) {
                     sender.sendMessage(LanguagePack.getMsg("command.error.nostat"));
                     return true;
                 }
                 if (vector.size() == 1) {
                     IStat iStat = vector.iterator().next();
 
                     sender.sendMessage(LanguagePack.getMsg("command.stat.stat",
                             StatisticMetadata.localizedName(iStat.getStatistic()),
                             StatisticMetadata.formatStat(iStat.getStatistic(), iStat.getValue())));
                     return true;
                 }
                 if (vector.size() > 1) {
                     // command.stat.stat.world
                     Iterator<IStat> it = vector.iterator();
                     while (it.hasNext()) {
                         IStat iStat = it.next();
 
                         sender.sendMessage(LanguagePack.getMsg("command.stat.stat.world", iStat.getWorld(),
                                 StatisticMetadata.localizedName(iStat.getStatistic()),
                                 StatisticMetadata.formatStat(iStat.getStatistic(), iStat.getValue())));
                         return true;
                     }
 
                 }
 
             } else {
                 Bukkit.dispatchCommand(sender, "statpage " + player + " default");
             }
 
         } catch (Exception e) {
             BeardStat.handleError(new BeardStatRuntimeException("/stats threw an error", e, true));
         }
 
         // TODO: FINISH UP
 
         return true;
     }
 
     public static void sendHelpMessage(CommandSender sender) {
         sender.sendMessage(ChatColor.BLUE + "Stats Help page");
         sender.sendMessage(ChatColor.BLUE + "/stats:" + ChatColor.GOLD + " Default display of your stats");
         sender.sendMessage(ChatColor.BLUE + "/stats [flags]:");
         sender.sendMessage(ChatColor.BLUE + "-h :" + ChatColor.GOLD + " This page");
         // sender.sendMessage(ChatColor.BLUE + "-i :" + ChatColor.GOLD +
         // " Interactive stats menu");
         sender.sendMessage(ChatColor.BLUE + "-p [player]:" + ChatColor.GOLD + " view [player]'s stats");
         sender.sendMessage(ChatColor.BLUE + "-s [stat] :" + ChatColor.GOLD
                + " view this stat (format category::statistic)");
         sender.sendMessage(ChatColor.BLUE + "/statpage :" + ChatColor.GOLD + " list available stat pages");
         sender.sendMessage(ChatColor.BLUE + "/statpage [user] page :" + ChatColor.GOLD + " show a specific stat page");
         if (sender.hasPermission("command.laston")) {
             sender.sendMessage(ChatColor.BLUE + "/laston [user] :" + ChatColor.GOLD
                     + " show when you [or user] was last on");
         }
         if (sender.hasPermission("command.laston")) {
             sender.sendMessage(ChatColor.BLUE + "/firston [user] :" + ChatColor.GOLD
                     + " show when you [or user] was first on");
         }
         if (sender.hasPermission("command.played")) {
             sender.sendMessage(ChatColor.BLUE + "/played [user] :" + ChatColor.GOLD
                     + " shows how long you [or user] have played");
         }
 
     }
 
     /*
      * public static void SendPlayerStats(CommandSender sender, EntityStatBlob
      * blob) { if (blob != null &&
      * blob.getStat(BeardStat.DEFAULT_DOMAIN,BeardStat.GLOBAL_WORLD,"stats",
      * "playedfor").getValue() != 0) { sender.sendMessage(ChatColor.GOLD + "-= "
      * + blob.getName() + "'s Stats =-");
      * 
      * long seconds =
      * blob.getStat(BeardStat.DEFAULT_DOMAIN,BeardStat.GLOBAL_WORLD,"stats",
      * "playedfor").getValue(); if (sender instanceof Player) { seconds +=
      * BeardStat.self().getStatManager().getSessionTime(((Player)
      * sender).getName()); }
      * sender.sendMessage(playedCommand.GetPlayedString(seconds));
      * 
      * Bukkit.dispatchCommand(sender, "statpage " + blob.getName() +
      * " default"); } else { sender.sendMessage(ChatColor.RED +
      * "Player not found."); } }
      */
 }
