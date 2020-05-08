 /*
  * Copyright (C) 2012 MineStar.de 
  * 
  * This file is part of Contao2.
  * 
  * Contao2 is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, version 3 of the License.
  * 
  * Contao2 is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with Contao2.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package de.minestar.contao2.commands.user;
 
 import java.util.List;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.command.ConsoleCommandSender;
 import org.bukkit.entity.Player;
 
 import de.minestar.contao2.core.Core;
 import de.minestar.contao2.manager.DatabaseManager;
 import de.minestar.contao2.manager.StatisticManager;
 import de.minestar.contao2.units.MCUser;
 import de.minestar.contao2.units.MCWarning;
 import de.minestar.contao2.units.PlayerWarnings;
 import de.minestar.contao2.units.Statistic;
 import de.minestar.core.MinestarCore;
 import de.minestar.core.units.MinestarPlayer;
 import de.minestar.minestarlibrary.commands.AbstractExtendedCommand;
 import de.minestar.minestarlibrary.utils.ChatUtils;
 import de.minestar.minestarlibrary.utils.ConsoleUtils;
 
 public class cmdStatus extends AbstractExtendedCommand {
 
     private DatabaseManager databaseManager;
     private StatisticManager statisticManager;
 
     public cmdStatus(String syntax, String arguments, String node, DatabaseManager databaseManager, StatisticManager statisticManager) {
         super(Core.NAME, syntax, arguments, node);
         this.description = "Userstatus abfragen";
         this.databaseManager = databaseManager;
         this.statisticManager = statisticManager;
     }
 
     @Override
     public void execute(String[] args, Player player) {
         if (args.length == 0)
             getStatus(player.getName(), player);
         else if (checkSpecialPermission(player, "contao.rights.status"))
             getStatus(args[0], player);
     }
     @Override
     public void execute(String[] args, ConsoleCommandSender console) {
         if (args.length == 0)
             ConsoleUtils.printError(pluginName, "You don't have any stats...");
         else
             getStatus(args[0], console);
     }
 
     private void getStatus(String targetName, CommandSender sender) {
 
         MCUser user = databaseManager.getIngameData(targetName);
         if (user == null) {
            ChatUtils.writeError(sender, pluginName, "Der Spieler '" + targetName + "' befindet sich nicht in der Datenbank!");
             return;
         }
 
         ChatUtils.writeColoredMessage(sender, pluginName, ChatColor.GOLD, "Informationen ueber " + targetName);
 
         // We have checked whether the player exists in the database, so there
         // is no null pointer check necessary!
         printGroup(sender, targetName, user.getContaoID());
         printAccountDates(sender, targetName);
         printWarnings(sender, targetName);
         printStatistics(sender, targetName);
        printXReason(sender, targetName);
     }
 
     private void printGroup(CommandSender caller, String playerName, int contaoID) {
         String group = databaseManager.getContaoGroup(contaoID);
 
         if (group == null)
             ChatUtils.writeError(caller, "Wurde bisher keiner Gruppe zugewiesen.");
         else {
             if (group.equalsIgnoreCase("vip"))
                 group = "Free";
             ChatUtils.writeColoredMessage(caller, ChatColor.BLUE, "Contao Gruppe: '" + group + "'.");
         }
     }
 
     private void printAccountDates(CommandSender sender, String playerName) {
         String[] dates = databaseManager.getAccountDates(playerName);
         ChatUtils.writeColoredMessage(sender, ChatColor.BLUE, "Seit dem " + dates[0] + " auf dem Server.");
         // is probe member
         if (dates[1] != null)
             ChatUtils.writeColoredMessage(sender, ChatColor.BLUE, "Probeuser bis zum " + dates[1]);
         // is pay user
         else if (!dates[2].equalsIgnoreCase("11.11.1111"))
             ChatUtils.writeColoredMessage(sender, ChatColor.BLUE, "Payuser bis zum " + dates[2]);
     }
 
     private void printWarnings(CommandSender sender, String playerName) {
 
         PlayerWarnings warnings = databaseManager.getsManager().getWarnings(playerName);
         // user has no warnings or have in the same session warnings lost
         if (warnings == null || warnings.isEmpty())
             ChatUtils.writeColoredMessage(sender, ChatColor.BLUE, "Keine Verwarnungen. Guter Junge :)");
         // user has warnings -> list them
         else {
             List<MCWarning> warnList = warnings.getWarnings();
             ChatUtils.writeColoredMessage(sender, ChatColor.RED, "Wurde " + warnList.size() + "x verwarnt!");
             for (MCWarning warning : warnList)
                 ChatUtils.writeColoredMessage(sender, ChatColor.RED, warning.toString());
         }
     }
 
     private void printStatistics(CommandSender sender, String playerName) {
         Statistic stats = statisticManager.getPlayersStatistic(playerName);
 
         if (stats == null)
             ChatUtils.writeColoredMessage(sender, ChatColor.RED, "Hat keine Statistiken!");
         else {
             ChatUtils.writeColoredMessage(sender, ChatColor.BLUE, "Bloecke zerstoert: " + stats.getTotalBreak());
             ChatUtils.writeColoredMessage(sender, ChatColor.BLUE, "Bloecke gesetzt  : " + stats.getTotalPlaced());
         }
     }
 
     private void printXReason(CommandSender sender, String targetName) {
 
         MinestarPlayer mPlayer = MinestarCore.getPlayer(targetName);
         String xReason = mPlayer.getString("contao.xreason");
         if (xReason != null) {
             ChatUtils.writeInfo(sender, "X-User Grund: " + xReason);
             ChatUtils.writeInfo(sender, "X-User Admin: " + mPlayer.getString("contao.xadmin"));
         }
     }
 }
