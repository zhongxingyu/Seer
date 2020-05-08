 package com.tehbeard.beardstat.commands;
 
 import java.util.Stack;
 import java.util.regex.PatternSyntaxException;
 
 import com.tehbeard.utils.commands.ArgumentPack;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import com.tehbeard.beardstat.BeardStat;
 import com.tehbeard.beardstat.BeardStatRuntimeException;
 import com.tehbeard.beardstat.dataproviders.IStatDataProvider;
 import com.tehbeard.beardstat.containers.EntityStatBlob;
 import com.tehbeard.beardstat.containers.IStat;
 import com.tehbeard.beardstat.manager.EntityStatManager;
 import com.tehbeard.beardstat.containers.StatVector;
 import com.tehbeard.beardstat.dataproviders.ProviderQuery;
 import com.tehbeard.beardstat.utils.LanguagePack;
 
 /**
  * Show stats for a player,
  * 
  * @author James
  * 
  */
 public class StatCommand extends BeardStatCommand {
 
     public StatCommand(EntityStatManager playerStatManager, BeardStat plugin) {
         super(playerStatManager, plugin);
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
 
             // not a player and no player picked? or -h flag? show the help message.
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
 
                 EntityStatBlob blob = this.playerStatManager.getBlob(new ProviderQuery(player, IStatDataProvider.PLAYER_TYPE,null,false));
                 if (blob == null) {
                     sender.sendMessage(LanguagePack.getMsg("command.error.noplayer", player));
                     return true;
                 }
                 StatVector vector = null;
                 try {
                     vector = blob.getStats(domain, world, category, statistic, true);
                 } catch (PatternSyntaxException ex) {
                     sender.sendMessage("Invalid stat entered");
                     return true;
                 }
 
                 if (vector.size() == 0) {
                     sender.sendMessage(LanguagePack.getMsg("command.error.nostat"));
                     return true;
                 }
                 sender.sendMessage(ChatColor.YELLOW + "=========");
                 if (vector.size() == 1) {
                     IStat iStat = vector.iterator().next();
 
                     sender.sendMessage(LanguagePack.getMsg("command.stat.stat",
                             playerStatManager.getLocalizedStatisticName(iStat.getStatistic()),
                             playerStatManager.formatStat(iStat.getStatistic(), iStat.getValue())));
                     return true;
                 }
                 if (vector.size() > 1) {
                     sender.sendMessage(LanguagePack.getMsg("command.stat.stat",
                             playerStatManager.getLocalizedStatisticName(vector.getStatistic()) + " total",
                             playerStatManager.formatStat(vector.getStatistic(), vector.getValue())));
                     // command.stat.stat.world
                     for(IStat iStat : vector) {
 
                         sender.sendMessage(LanguagePack.getMsg("command.stat.stat.world", iStat.getWorld(),
                                 playerStatManager.getLocalizedStatisticName(iStat.getStatistic()),
                                 playerStatManager.formatStat(iStat.getStatistic(), iStat.getValue())));
                         
                     }
                     return true;
 
                 }
 
             } else {
                //TODO - Swap to API call instead
                 Bukkit.dispatchCommand(sender, "statpage " + player + " default");
             }
 
         } catch (Exception e) {
             this.plugin.handleError(new BeardStatRuntimeException("/stats threw an error", e, true));
         }
 
         // TODO: FINISH UP, i think this means check all the options have been added back in?
 
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
