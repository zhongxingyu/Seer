 /*
  * MyResidence, Bukkit plugin for managing your towns and residences
  * Copyright (C) 2011, Michael Hohl
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package at.co.hohl.myresidence.commands;
 
 import at.co.hohl.myresidence.MyResidence;
 import at.co.hohl.myresidence.Nation;
 import at.co.hohl.myresidence.exceptions.PlayerNotFoundException;
 import at.co.hohl.myresidence.storage.Session;
 import at.co.hohl.myresidence.storage.persistent.Inhabitant;
 import at.co.hohl.myresidence.storage.persistent.Residence;
 import at.co.hohl.myresidence.storage.persistent.Town;
 import com.avaje.ebean.ExpressionList;
 import com.sk89q.minecraft.util.commands.Command;
 import com.sk89q.minecraft.util.commands.CommandContext;
 import com.sk89q.worldedit.commands.InsufficientArgumentsException;
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 
 import java.util.List;
 
 /**
  * Commands for listing and searching the residences.
  *
  * @author Michael Hohl
  */
 public class ResidenceListCommands {
   /**
    * Maximum number of lines per page.
    */
   private static final int LINES_PER_PAGE = 7;
 
   @Command(
           aliases = {"forsale", "sale", "sell"},
           usage = "[page]",
           desc = "Lists the residences for sale",
           flags = "t",
           max = 1
   )
   public static void forSale(final CommandContext args,
                              final MyResidence plugin,
                              final Nation nation,
                              final Player player,
                              final Session session) throws InsufficientArgumentsException {
 
     ExpressionList expressionList = nation.getDatabase().find(Residence.class).where();
 
     // Cheapest residence at the top.
     expressionList.orderBy("price ASC");
 
     // Only residences for sale.
     expressionList.eq("forSale", true);
 
     if (args.hasFlag('t')) {
       Town currentTown = nation.getTown(player.getLocation());
       if (currentTown == null) {
         throw new InsufficientArgumentsException(
                 "You are not inside a town! You could only use -t inside towns.");
       } else {
         expressionList.eq("townId", currentTown.getId());
       }
     }
 
     // Find and display exact page.
     int page = args.getInteger(0, 1);
     displayResults("Residences (Oldest)", expressionList, page, plugin, nation, player, !args.hasFlag('t'), true,
             true);
 
   }
 
   @Command(
           aliases = {"own", "my"},
           usage = "[page]",
           desc = "Lists all residences you own",
           flags = "t",
           max = 2
   )
   public static void own(final CommandContext args,
                          final MyResidence plugin,
                          final Nation nation,
                          final Player player,
                          final Session session) throws InsufficientArgumentsException {
 
     ExpressionList expressionList = nation.getDatabase().find(Residence.class).where();
 
     // Sort by name.
     expressionList.orderBy("name ASC");
 
     // Only residences i own.
     expressionList.eq("ownerId", session.getPlayerId());
 
     if (args.hasFlag('t')) {
       Town currentTown = nation.getTown(player.getLocation());
       if (currentTown == null) {
         throw new InsufficientArgumentsException(
                 "You are not inside a town! You could only use -t inside towns.");
       } else {
         expressionList.eq("townId", currentTown.getId());
       }
     }
 
     // Find and display exact page.
     int page = args.getInteger(0, 1);
     displayResults("Residences (Owner: " + player.getDisplayName() + ")", expressionList, page,
             plugin, nation, player, !args.hasFlag('t'), false, false);
 
   }
 
   @Command(
           aliases = {"player"},
           usage = "<PLAYER> [page]",
           desc = "Lists all residences a player owns",
           flags = "t",
           min = 1,
           max = 2
   )
   public static void player(final CommandContext args,
                             final MyResidence plugin,
                             final Nation nation,
                             final Player player,
                             final Session session) throws InsufficientArgumentsException, PlayerNotFoundException {
 
     ExpressionList expressionList = nation.getDatabase().find(Residence.class).where();
 
     // Sort by name.
     expressionList.orderBy("name ASC");
 
     // Only residences i own.
     Inhabitant inhabitant = nation.getInhabitant(args.getString(0));
     if (inhabitant == null) {
       throw new PlayerNotFoundException();
     }
    expressionList.eq("ownerId", inhabitant);
 
     if (args.hasFlag('t')) {
       Town currentTown = nation.getTown(player.getLocation());
       if (currentTown == null) {
         throw new InsufficientArgumentsException(
                 "You are not inside a town! You could only use -t inside towns.");
       } else {
         expressionList.eq("townId", currentTown.getId());
       }
     }
 
     // Find and display exact page.
     int page = args.getInteger(1, 1);
     displayResults("Residences (Owner: " + inhabitant + ")", expressionList, page,
             plugin, nation, player, !args.hasFlag('t'), false, false);
 
   }
 
   @Command(
           aliases = {"alphabetic", "abc"},
           usage = "[page]",
           desc = "Lists all residences in alphabetic order",
           flags = "t",
           max = 1
   )
   public static void alphabetic(final CommandContext args,
                                 final MyResidence plugin,
                                 final Nation nation,
                                 final Player player,
                                 final Session session) throws InsufficientArgumentsException {
 
     ExpressionList expressionList = nation.getDatabase().find(Residence.class).where();
 
     // Sort by name.
     expressionList.orderBy("name ASC");
 
     if (args.hasFlag('t')) {
       Town currentTown = nation.getTown(player.getLocation());
       if (currentTown == null) {
         throw new InsufficientArgumentsException(
                 "You are not inside a town! You could only use -t inside towns.");
       } else {
         expressionList.eq("townId", currentTown.getId());
       }
     }
 
     // Find and display exact page.
     int page = args.getInteger(0, 1);
     displayResults("Residences (Alphabetic Order)", expressionList, page, plugin, nation, player,
             !args.hasFlag('t'), true, false);
 
   }
 
   @Command(
           aliases = {"expensive"},
           usage = "[page]",
           desc = "Lists the most expensive residences",
           flags = "to",
           max = 1
   )
   public static void expensive(final CommandContext args,
                                final MyResidence plugin,
                                final Nation nation,
                                final Player player,
                                final Session session) throws InsufficientArgumentsException {
 
     ExpressionList expressionList = nation.getDatabase().find(Residence.class).where();
 
     // Sort by name.
     expressionList.orderBy("value DESC");
 
     if (args.hasFlag('t')) {
       Town currentTown = nation.getTown(player.getLocation());
       if (currentTown == null) {
         throw new InsufficientArgumentsException(
                 "You are not inside a town! You could only use -t inside towns.");
       } else {
         expressionList.eq("townId", currentTown.getId());
       }
     }
 
     if (args.hasFlag('o')) {
       expressionList.eq("ownerId", session.getPlayerId());
     }
 
     // Find and display exact page.
     int page = args.getInteger(0, 1);
     displayResults("Residences (Most Expensive)", expressionList, page, plugin, nation, player,
             !args.hasFlag('t'), !args.hasFlag('o'), false);
 
   }
 
   // Displays the search results.
 
   private static void displayResults(final String searchTitle,
                                      final ExpressionList expressionList,
                                      final int page,
                                      final MyResidence plugin,
                                      final Nation nation,
                                      final Player player,
                                      boolean showTown,
                                      boolean showOwner,
                                      boolean showPrice)
           throws InsufficientArgumentsException {
 
     int rows = expressionList.findRowCount();
     int index = (page - 1) * 7 + 1;
     if (rows == 0) {
       throw new InsufficientArgumentsException("No search results found!");
     }
     if (index > rows || index < 1) {
       throw new InsufficientArgumentsException("Invalid page number!");
     }
     expressionList.setMaxRows(LINES_PER_PAGE);
     expressionList.setFirstRow((page - 1) * LINES_PER_PAGE);
 
     // Get towns.
     List<Residence> residences = expressionList.findList();
 
     // Send results to player.
     player.sendMessage(String.format("%s= = = %s [Page %s/%s] = = =",
             ChatColor.LIGHT_PURPLE, searchTitle, page, rows / LINES_PER_PAGE + 1));
 
     for (Residence residence : residences) {
       StringBuilder line = new StringBuilder();
       line.append(ChatColor.GRAY);
       line.append(index++);
       line.append(". ");
       if (showTown) {
         line.append(Town.toString(nation.getTown(residence.getTownId())));
         line.append("->");
       }
       line.append(ChatColor.WHITE);
       line.append(residence.getName());
       line.append(ChatColor.GRAY);
       line.append(' ');
       if (showOwner) {
         line.append('(');
         line.append(nation.getInhabitant(residence.getOwnerId()));
         line.append(") ");
       }
       line.append("[");
       if (showPrice) {
         line.append("Price: ");
         line.append(plugin.format(residence.getPrice()));
       } else {
         line.append("Value: ");
         line.append(plugin.format(residence.getValue()));
       }
       line.append("]");
 
       player.sendMessage(line.toString());
     }
   }
 }
