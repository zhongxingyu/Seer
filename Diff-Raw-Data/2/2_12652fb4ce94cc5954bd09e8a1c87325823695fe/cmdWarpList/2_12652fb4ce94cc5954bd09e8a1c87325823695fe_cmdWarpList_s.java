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
 
 package de.minestar.FifthElement.commands.warp;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 
 import de.minestar.FifthElement.core.Core;
 import de.minestar.FifthElement.core.Settings;
 import de.minestar.FifthElement.data.Warp;
 import de.minestar.FifthElement.data.filter.OwnerFilter;
 import de.minestar.FifthElement.data.filter.PrivateFilter;
 import de.minestar.FifthElement.data.filter.PublicFilter;
 import de.minestar.FifthElement.data.filter.UseFilter;
 import de.minestar.FifthElement.data.filter.WarpFilter;
 import de.minestar.FifthElement.statistics.warp.WarpListStat;
 import de.minestar.minestarlibrary.stats.StatisticHandler;
 import de.minestar.minestarlibrary.commands.AbstractExtendedCommand;
 import de.minestar.minestarlibrary.utils.PlayerUtils;
 
 public class cmdWarpList extends AbstractExtendedCommand {
 
     public cmdWarpList(String syntax, String arguments, String node) {
         super(Core.NAME, syntax, arguments, node);
     }
 
     @Override
     public void execute(String[] args, Player player) {
         List<WarpFilter> filterList = new ArrayList<WarpFilter>();
 
         int pageNumber = 1;
         filterList.add(new UseFilter(player));
 
         // APPLY FILTER
         if (args.length > 0) {
             for (int i = 0; i < args.length; ++i) {
                 String arg = args[i];
                 // PAGE NUMBER
                 if (arg.equalsIgnoreCase("-page")) {
                     // NEXT PARAMETER MUST EXIST
                     if (i < args.length - 1) {
                         try {
                             pageNumber = Integer.valueOf(args[++i]);
                             // NEGATIVE PAGE NUMBER
                             if (pageNumber <= 0) {
                                 PlayerUtils.sendError(player, pluginName, "Die Seitenzahl muss grer 0 sein!");
                                 return;
                             }
                         } catch (Exception e) {
                             // NOT A VALID NUMBER
                             PlayerUtils.sendError(player, pluginName, args[i] + " ist keine gltige Seitenzahl!");
                             return;
                         }
                     } else {
                         PlayerUtils.sendError(player, pluginName, "Es fehlt bei '-page' die Seitenzahl!");
                         return;
                     }
                 }
                 // DISPLAY OWN CREATED WARPS
                 else if (arg.equalsIgnoreCase("-created")) {
                     filterList.add(new OwnerFilter(player.getName()));
                 }
                 // DISPLAY USEABLE PRIVATE WARPS
                 else if (arg.equalsIgnoreCase("-private")) {
                     filterList.add(PrivateFilter.getInstance());
                 }
                 // DISPLAY PUBLIC WARPS
                 else if (arg.equalsIgnoreCase("-public")) {
                     filterList.add(PublicFilter.getInstance());
                 }
                 // DISPLAY WARPS FROM A SPECIFIC PLAYER WHICH THE COMMAND CALLER
                 // CAN USE
                 else if (arg.equalsIgnoreCase("-player")) {
                     String targetName = null;
                     // AFTER -player THERE MUST BE A PLAYER NAME
                     if (i < args.length - 1) {
                         targetName = PlayerUtils.getCorrectPlayerName(args[++i]);
                         // PLAYER NOT FOUND
                         if (targetName == null) {
                             PlayerUtils.sendError(player, pluginName, "Der Spieler '" + args[i] + "' wurde nicht gefunden!");
                             return;
                         }
 
                         filterList.add(new OwnerFilter(targetName));
                     } else {
                         PlayerUtils.sendError(player, pluginName, "Es fehlt bei '-player' der Name des Spielers!");
                         return;
                     }
                 }
             }
         }
 
         // GET WARPS
         List<Warp> results = Core.warpManager.filterWarps(filterList);
         // NO WARPS FOUND
         if (results.isEmpty()) {
             PlayerUtils.sendError(player, pluginName, "Keine Ergebnisse gefunden mit folgendem Filter:");
             PlayerUtils.sendError(player, pluginName, filterList.toString());
             return;
         }
 
         int resultSize = results.size();
 
         // GET THE SINGLE PAGE
         int pageSize = Settings.getPageSize();
         int fromIndex = pageSize * (pageNumber - 1);
         if (fromIndex >= results.size()) {
             PlayerUtils.sendError(player, pluginName, "Zu hohe Seitenzahl!");
             return;
         }
         int toIndex = fromIndex + pageSize;
         if (toIndex > results.size())
             toIndex = results.size();
 
        int maxNumber = (results.size() / Settings.getPageSize()) + 1;
 
         results = results.subList(fromIndex, toIndex);
         Collections.sort(results, PUBLIC_PRIVATE_SORT);
         displayList(results, player, pageNumber, maxNumber, filterList);
 
         // FIRE STATISTIC
         StatisticHandler.handleStatistic(new WarpListStat(player.getName(), resultSize, filterList));
     }
 
     // USED FOR SORTING PUBLIC AND PRIVATE WARPS
     private final static Comparator<Warp> PUBLIC_PRIVATE_SORT = new Comparator<Warp>() {
 
         @Override
         public int compare(Warp o1, Warp o2) {
             if (!o1.isPublic() && !o2.isPublic()) {
                 if (o1.getOwner().equals(o2.getOwner())) {
                     return o1.getName().compareTo(o2.getName());
                 } else
                     return o1.getOwner().compareTo(o2.getOwner());
             }
 
             if (!o1.isPublic() && o2.isPublic()) {
                 return -1;
             } else
                 return 1;
         }
     };
 
     private final static String SEPERATOR = ChatColor.WHITE + "----------------------------------------";
     private final static ChatColor NAME_COLOR = ChatColor.GREEN;
     private final static ChatColor VALUE_COLOR = ChatColor.GRAY;
 
     private void displayList(List<Warp> list, Player player, int pageNumber, int maxNumber, List<WarpFilter> filter) {
 
         // HEAD
         PlayerUtils.sendInfo(player, SEPERATOR);
         PlayerUtils.sendInfo(player, String.format("%s %s", NAME_COLOR + "Seite:", VALUE_COLOR + Integer.toString(pageNumber)) + "/" + Integer.toString(maxNumber));
         PlayerUtils.sendInfo(player, String.format("%s %s", NAME_COLOR + "Filter:", VALUE_COLOR + filter.toString()));
         PlayerUtils.sendInfo(player, SEPERATOR);
 
         // GET WARP INDEX TO START WITH
         int index = ((pageNumber - 1) * Settings.getPageSize()) + 1;
         if (index < 0)
             index = 1;
 
         // HEAD FOR PUBLIC WARPS
         if (!list.get(0).isPublic())
             PlayerUtils.sendInfo(player, String.format("%s %s", NAME_COLOR + "Private Warps", ""));
 
         boolean priv = false;
         ChatColor color = null;
         // DISPLAY WARPS
         for (Warp warp : list) {
 
             // SEPERATE PUBLIC AND PRIVATE WARPS
             if (!priv && warp.isPublic()) {
                 priv = true;
                 PlayerUtils.sendInfo(player, String.format("%s %s", NAME_COLOR + "ffentliche Warps", ""));
             }
             // COLORS FOR WARPS
 
             // PUBLIC WARPS
             if (warp.isPublic())
                 color = Settings.getWarpListPublic();
             // OWNED WARPS
             else if (warp.isOwner(player))
                 color = Settings.getWarpListOwned();
             // INVITED TO PRIVATE WARPS
             else
                 color = Settings.getWarpListPrivate();
 
             PlayerUtils.sendInfo(player, String.format("%s%s %s%s %s(%s%s)", NAME_COLOR + "#", VALUE_COLOR + Integer.toString(index++), color, warp.getName(), NAME_COLOR, VALUE_COLOR + warp.getOwner(), NAME_COLOR));
         }
     }
 }
