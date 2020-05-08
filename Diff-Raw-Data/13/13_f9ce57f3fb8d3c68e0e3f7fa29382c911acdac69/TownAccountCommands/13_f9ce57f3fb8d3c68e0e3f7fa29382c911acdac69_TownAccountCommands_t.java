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
 import at.co.hohl.myresidence.exceptions.MyResidenceException;
 import at.co.hohl.myresidence.exceptions.NoTownSelectedException;
 import at.co.hohl.myresidence.exceptions.PermissionsDeniedException;
 import at.co.hohl.myresidence.storage.Session;
 import at.co.hohl.myresidence.storage.persistent.Town;
 import com.nijikokun.register.payment.Method;
 import com.sk89q.minecraft.util.commands.Command;
 import com.sk89q.minecraft.util.commands.CommandContext;
 import com.sk89q.minecraft.util.commands.CommandPermissions;
 import com.sk89q.worldedit.commands.InsufficientArgumentsException;
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 
 /**
  * Commands for managing the bank account of the town.
  *
  * @author Michael Hohl
  */
 public class TownAccountCommands {
   @Command(
           aliases = {"balance"},
           desc = "Balance of town account",
           max = 0
   )
   @CommandPermissions({"myresidence.major"})
   public static void money(final CommandContext args,
                            final MyResidence plugin,
                            final Nation nation,
                            final Player player,
                            final Session session)
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
   @CommandPermissions({"myresidence.major"})
   public static void pay(final CommandContext args,
                          final MyResidence plugin,
                          final Nation nation,
                          final Player player,
                          final Session session)
           throws MyResidenceException, InsufficientArgumentsException {
     Town selectedTown = session.getSelectedTown();
     Method payment = plugin.getPaymentMethods().getMethod();
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
 
    // Has enough money on account?
    if (amount > selectedTown.getMoney()) {
      throw new InsufficientArgumentsException("Not enough money!");
    }

     selectedTown.subtractMoney(amount);
     account.add(amount);
     nation.save(selectedTown);
 
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
   @CommandPermissions({"myresidence.major"})
   public static void grant(final CommandContext args,
                            final MyResidence plugin,
                            final Nation nation,
                            final Player player,
                            final Session session)
           throws MyResidenceException, InsufficientArgumentsException {
     Town selectedTown = session.getSelectedTown();
     Method payment = plugin.getPaymentMethods().getMethod();
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
     nation.getDatabase().save(selectedTown);
 
     player.sendMessage(String.format("%s%s%s send to town account!",
             ChatColor.GREEN, payment.format(amount), ChatColor.DARK_GREEN));
   }
 }
