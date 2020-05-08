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
 import at.co.hohl.myresidence.exceptions.MyResidenceException;
 import at.co.hohl.myresidence.exceptions.NoTownSelectedException;
 import at.co.hohl.myresidence.exceptions.NotEnoughMoneyException;
 import at.co.hohl.myresidence.exceptions.PermissionsDeniedException;
 import at.co.hohl.myresidence.storage.Session;
 import at.co.hohl.myresidence.storage.persistent.Town;
 import at.co.hohl.myresidence.storage.persistent.TownChunk;
 import com.nijikokun.register.payment.Method;
 import com.sk89q.minecraft.util.commands.Command;
 import com.sk89q.minecraft.util.commands.CommandContext;
 import com.sk89q.minecraft.util.commands.CommandPermissions;
 import com.sk89q.worldedit.commands.InsufficientArgumentsException;
 import org.bukkit.ChatColor;
 import org.bukkit.Chunk;
 import org.bukkit.entity.Player;
 
 /**
  * Command for managing towns.
  *
  * @author Michael Hohl
  */
 public class TownCommands {
     @Command(
             aliases = {"found", "create"},
             usage = "<name>",
             desc = "Creates a new town",
             min = 1,
             flags = "w"
     )
     @CommandPermissions({"myresidence.town.found"})
     public static void create(CommandContext args, MyResidence plugin, Player player, Session session) {
         Town town = new Town();
         town.setName(args.getJoinedStrings(0));
         town.setMajorId(plugin.getPlayer(player.getName()).getId());
         plugin.getDatabase().save(town);
 
         player.sendMessage(ChatColor.DARK_GREEN + "Town '" + args.getJoinedStrings(0) + "' created!");
     }
 
     @Command(
             aliases = {"select", "sel", "s"},
             usage = "<name>",
             desc = "Selects a town",
             min = 1
     )
     public static void select(CommandContext args, MyResidence plugin, Player player, Session session) {
         Town townToSelect = plugin.getTown(args.getJoinedStrings(0));
         session.setSelectedTown(townToSelect);
 
         player.sendMessage(ChatColor.DARK_GREEN + "Town '" + townToSelect.getName() + "' selected!");
     }
 
     @Command(
             aliases = {"addchunk", "chunk", "c"},
             desc = "Adds your current chunk to the selected town",
             max = 0
     )
     @CommandPermissions({"myresidence.town.major.expand"})
     public static void addChunk(CommandContext args, MyResidence plugin, Player player, Session session)
             throws MyResidenceException {
         // Get selections.
         final Town selectedTown = session.getSelectedTown();
         final Chunk playerChunk = player.getLocation().getBlock().getChunk();
 
         // Check if player is major.
         if (!session.hasMajorRights(selectedTown)) {
             throw new PermissionsDeniedException("You are not the major of the selected town!");
         }
 
         // Check if already reserved?
        if (plugin.getTown(player.getLocation()) != null) {
            throw new MyResidenceException("This chunk is already bought by another town!");
         }
 
         // Check money.
         double chunkCost = plugin.getConfiguration(player.getWorld()).getChunkCost();
         double townMoney = selectedTown.getMoney();
         if (townMoney < chunkCost) {
             throw new NotEnoughMoneyException("The town has not enough money!");
         } else {
             selectedTown.setMoney(townMoney - chunkCost);
         }
 
         // Reserve new chunk.
         TownChunk townChunk = new TownChunk(selectedTown, playerChunk);
         plugin.getDatabase().save(townChunk);
 
         player.sendMessage(ChatColor.DARK_GREEN + "Town bought chunk for " + ChatColor.GREEN +
                 plugin.format(chunkCost) + ChatColor.DARK_GREEN + ".");
     }
 
     @Command(
             aliases = {"addselection", "selection"},
            desc = "Adds your current chunk to the selected town",
             max = 0
     )
     @CommandPermissions({"myresidence.town.major.expand"})
     public static void addSelection(CommandContext args, MyResidence plugin, Player player, Session session) {
 
     }
 
     @Command(
             aliases = {"info", "i"},
             desc = "Returns information about the selected town",
             max = 0
     )
     @CommandPermissions({"myresidence.town.info"})
     public static void info(CommandContext args, MyResidence plugin, Player player, Session session) {
     }
 
     @Command(
             aliases = {"list"},
             desc = "List available towns",
             max = 0,
             flags = "mi"
     )
     @CommandPermissions({"myresidence.town.list"})
     public static void list(CommandContext args, MyResidence plugin, Player player, Session session) {
     }
 
     @Command(
             aliases = {"money"},
             desc = "Balance of town account",
             max = 0
     )
     public static void money(CommandContext args, MyResidence plugin, Player player, Session session)
             throws PermissionsDeniedException, NoTownSelectedException {
         Town selectedTown = session.getSelectedTown();
 
         // Check if player is major.
         if (!session.hasMajorRights(selectedTown)) {
             throw new PermissionsDeniedException("You are not the major of the selected town!");
         }
 
         player.sendMessage(ChatColor.DARK_GREEN + "Town Account Balance: " +
                 ChatColor.GREEN + plugin.format(selectedTown.getMoney()));
     }
 
     @Command(
             aliases = {"pay"},
             usage = "<account> <amount>",
             desc = "Pays money to an economy account",
             min = 2,
             max = 2
     )
     @CommandPermissions({"myresidence.town.major.pay"})
     public static void pay(CommandContext args, MyResidence plugin, Player player, Session session)
             throws MyResidenceException, InsufficientArgumentsException {
         Town selectedTown = session.getSelectedTown();
         Method payment = plugin.getMethods().getMethod();
         Method.MethodAccount account = payment.getAccount(args.getString(0));
         double amount = args.getDouble(1);
 
         // Check if player is major.
         if (!session.hasMajorRights(selectedTown)) {
             throw new PermissionsDeniedException("You are not the major of the selected town!");
         }
 
         // Does account exist?
         if (account == null) {
             throw new MyResidenceException(ChatColor.RED + "Account " + ChatColor.DARK_RED + args.getString(0) +
                     ChatColor.RED + " does not exist!");
         }
 
         // Is passed amount of money greater than 0?
         if (amount <= 0.0) {
             throw new InsufficientArgumentsException("You can not send an amount smaller than 0!");
         }
 
         selectedTown.subtractMoney(amount);
         account.add(amount);
         plugin.getDatabase().save(selectedTown);
 
         player.sendMessage(String.format("%s%s%s send to %s%s%s!",
                 ChatColor.GREEN, args.getString(0), ChatColor.DARK_GREEN,
                 ChatColor.GREEN, payment.format(amount), ChatColor.DARK_GREEN));
     }
 
     @Command(
             aliases = {"grant"},
             usage = "<amount>",
             desc = "Grants money to town account",
             min = 1,
             max = 1
     )
     @CommandPermissions({"myresidence.town.major.grant"})
     public static void grant(CommandContext args, MyResidence plugin, Player player, Session session)
             throws MyResidenceException, InsufficientArgumentsException {
         Town selectedTown = session.getSelectedTown();
         Method payment = plugin.getMethods().getMethod();
         Method.MethodAccount account = payment.getAccount(player.getName());
         double amount = args.getDouble(0);
 
         // Check if player is major.
         if (!session.hasMajorRights(selectedTown)) {
             throw new PermissionsDeniedException("You are not the major of the selected town!");
         }
 
         // Does account exist?
         if (account == null) {
             throw new MyResidenceException("You do not own a bank account!");
         }
 
         // Has enough money?
         if (!account.hasEnough(amount)) {
             throw new InsufficientArgumentsException("You can not grant more money than you own!");
         }
 
         selectedTown.addMoney(amount);
         account.subtract(amount);
         plugin.getDatabase().save(selectedTown);
 
         player.sendMessage(String.format("%s%s%s send to town account!",
                 ChatColor.GREEN, payment.format(amount), ChatColor.DARK_GREEN));
     }
 }
