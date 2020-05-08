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
 import at.co.hohl.myresidence.exceptions.NoTownSelectedException;
 import at.co.hohl.myresidence.exceptions.PermissionsDeniedException;
 import at.co.hohl.myresidence.storage.Session;
 import at.co.hohl.myresidence.storage.persistent.Town;
 import at.co.hohl.myresidence.storage.persistent.TownFlag;
 import com.sk89q.minecraft.util.commands.Command;
 import com.sk89q.minecraft.util.commands.CommandContext;
 import com.sk89q.minecraft.util.commands.CommandPermissions;
 import com.sk89q.util.StringUtil;
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 
 /**
  * Commands for managing the flags of towns.
  *
  * @author Michael Hohl
  */
 public class TownFlagCommands {
   @Command(
           aliases = {"set", "add"},
           usage = "<flag>",
           desc = "Sets the flag for the selected town",
           min = 1,
           max = 1
   )
   @CommandPermissions({"myresidence.town.flags"})
   public static void set(final CommandContext args,
                          final MyResidence plugin,
                          final Nation nation,
                          final Player player,
                          final Session session) throws NoTownSelectedException, PermissionsDeniedException {
     Town selectedTown = session.getSelectedTown();
 
     if (!session.hasMajorRights(selectedTown)) {
       throw new PermissionsDeniedException("You are not the major of this town!");
     }
 
     try {
      TownFlag.Type flag = TownFlag.Type.valueOf(args.getString(0).toUpperCase());
       nation.getFlagManager(selectedTown).setFlag(flag);
 
       player.sendMessage(ChatColor.DARK_GREEN + "Set flag " +
               ChatColor.GREEN + flag +
               ChatColor.DARK_GREEN + " to " +
               ChatColor.GREEN + selectedTown.getName() +
               ChatColor.DARK_GREEN + "!");
     } catch (IllegalArgumentException e) {
       player.sendMessage(ChatColor.RED + "There does not exist any flag with that name!");
     }
   }
 
   @Command(
           aliases = {"remove"},
           usage = "<flag>",
           desc = "Removes the flag for the selected residence",
           min = 1,
           max = 1
   )
   @CommandPermissions({"myresidence.town.flags"})
   public static void remove(final CommandContext args,
                             final MyResidence plugin,
                             final Nation nation,
                             final Player player,
                             final Session session) throws NoTownSelectedException, PermissionsDeniedException {
     Town selectedTown = session.getSelectedTown();
 
     if (!session.hasMajorRights(selectedTown)) {
       throw new PermissionsDeniedException("You are not the major of this town!");
     }
 
     try {
 
      TownFlag.Type flag = TownFlag.Type.valueOf(args.getString(0).toUpperCase());
       nation.getFlagManager(selectedTown).removeFlag(flag);
 
       player.sendMessage(ChatColor.DARK_GREEN + "Removed flag " +
               ChatColor.GREEN + flag +
               ChatColor.DARK_GREEN + " from " +
               ChatColor.GREEN + selectedTown.getName() +
               ChatColor.DARK_GREEN + "!");
     } catch (IllegalArgumentException e) {
       player.sendMessage(ChatColor.RED + "There does not exist any flag with that name!");
     }
   }
 
   @Command(
           aliases = {"list", "?"},
           desc = "Lists all available flags",
           max = 0
   )
   @CommandPermissions({"myresidence.town.flags"})
   public static void list(final CommandContext args,
                           final MyResidence plugin,
                           final Nation nation,
                           final Player player,
                           final Session session) {
 
     player.sendMessage(ChatColor.DARK_GREEN + "Available Flags: " +
             ChatColor.GREEN + StringUtil.joinString(TownFlag.Type.values(), ", ", 0));
   }
 }
