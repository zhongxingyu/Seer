 /*
  * Copyright (C) 2012 MineStar.de 
  * 
  * This file is part of FifthElement.
  * 
  * FifthElement is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, version 3 of the License.
  * 
  * FifthElement is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with FifthElement.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package de.minestar.FifthElement.commands.mine;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.entity.Player;
 
 import de.minestar.FifthElement.core.Core;
 import de.minestar.FifthElement.data.Mine;
 import de.minestar.FifthElement.statistics.mine.MineInfoStat;
 import de.minestar.minestarlibrary.stats.StatisticHandler;
 import de.minestar.minestarlibrary.commands.AbstractExtendedCommand;
 import de.minestar.minestarlibrary.utils.PlayerUtils;
 
 public class cmdMineInfo extends AbstractExtendedCommand {
 
     private static final String OTHER_MINE_INFO_PERMISSION = "fifthelement.command.othermineinfo";
 
     public cmdMineInfo(String syntax, String arguments, String node) {
         super(Core.NAME, syntax, arguments, node);
     }
 
     @Override
     public void execute(String[] args, Player player) {
         Mine mine = null;
         // INFORMATION ABOUT OWN MINE
         if (args.length == 0) {
             mine = Core.mineManager.getMine(player.getName());
             // HAS NO MINE YET
             if (mine == null) {
                 PlayerUtils.sendError(player, pluginName, "Du hast keine Mine!");
                 return;
             }
         }
         // INFORMATION ABOUT OTHER MINES
         else if (args.length == 1) {
             // CAN USE THE COMMAND
             if (checkSpecialPermission(player, OTHER_MINE_INFO_PERMISSION)) {
                 // GET CORRECT PLAYER NAME
                 String targetName = PlayerUtils.getCorrectPlayerName(args[0]);
                 // PLAYER NOT FOUND
                 if (targetName == null) {
                     PlayerUtils.sendError(player, pluginName, "Der Spieler '" + args[0] + "' wurde nicht gefunden!");
                     return;
                 }
                 mine = Core.mineManager.getMine(targetName);
                 // TARGET HAS NO MINE YET
                 if (mine == null) {
                     PlayerUtils.sendError(player, pluginName, "Der Spieler '" + targetName + "' hat keine Mine!");
                     return;
                 }
             }
         }
         // WRONG SYNTAX
         else {
             PlayerUtils.sendError(player, pluginName, getHelpMessage());
             return;
         }
 
         displayMineInformation(player, mine);
 
         // FIRE STATISTICS
         StatisticHandler.handleStatistic(new MineInfoStat(player.getName(), mine.getOwner()));
     }
 
     private final static String SEPERATOR = "----------------------------------------";
     private final static ChatColor NAME_COLOR = ChatColor.GREEN;
     private final static ChatColor VALUE_COLOR = ChatColor.GRAY;
 
     private void displayMineInformation(Player player, Mine mine) {
 
         PlayerUtils.sendInfo(player, SEPERATOR);
         PlayerUtils.sendInfo(player, String.format("%s %s", NAME_COLOR + "Mine von:", VALUE_COLOR + mine.getOwner()));
         PlayerUtils.sendInfo(player, SEPERATOR);
 
         Location loc = mine.getLocation();
        PlayerUtils.sendInfo(player, String.format(NAME_COLOR + "X:" + VALUE_COLOR + " %d " + NAME_COLOR + "Y:" + VALUE_COLOR + " %d " + NAME_COLOR + "Z: " + VALUE_COLOR + "%d " + NAME_COLOR + "Welt:" + VALUE_COLOR + " %s", loc.getBlockX(), loc.getBlockY(), loc.getBlockY(), loc.getWorld().getName()));
         if (player.getLocation().getWorld().equals(mine.getLocation().getWorld()))
             PlayerUtils.sendInfo(player, String.format("%s %s m", NAME_COLOR + "Entfernung:", VALUE_COLOR + Integer.toString((int) player.getLocation().distance(loc))));
     }
 }
