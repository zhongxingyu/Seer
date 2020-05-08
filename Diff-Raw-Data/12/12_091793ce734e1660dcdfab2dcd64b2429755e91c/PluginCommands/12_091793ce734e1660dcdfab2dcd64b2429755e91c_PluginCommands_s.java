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
 
 import at.co.hohl.mcutils.chat.Chat;
 import at.co.hohl.myresidence.InvalidResidenceListener;
 import at.co.hohl.myresidence.MyResidence;
 import at.co.hohl.myresidence.Nation;
 import at.co.hohl.myresidence.storage.Session;
 import at.co.hohl.myresidence.storage.persistent.Residence;
 import com.sk89q.minecraft.util.commands.Command;
 import com.sk89q.minecraft.util.commands.CommandContext;
 import com.sk89q.minecraft.util.commands.CommandPermissions;
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 
 import java.util.List;
 
 /**
  * Commands to handle the plugin.
  *
  * @author Michael Hohl
  */
 public class PluginCommands {
   @Command(
           aliases = {"reload"},
           desc = "Reloads the MyResidence plugin",
           max = 0
   )
   @CommandPermissions({"myresidence.admin"})
   public static void reload(final CommandContext args,
                             final MyResidence plugin,
                             final Nation nation,
                             final Player player,
                             final Session session) {
 
     plugin.getServer().reload();
     player.sendMessage(ChatColor.LIGHT_PURPLE + "Configuration reloaded!");
 
   }
 
   @Command(
           aliases = {"version", "ver"},
           desc = "Returns MyResidence version",
           max = 0
   )
   public static void version(final CommandContext args,
                              final MyResidence plugin,
                              final Nation nation,
                              final Player player,
                              final Session session) {
 
     player.sendMessage(ChatColor.GOLD +
             String.format("%s version %s",
                     plugin.getDescription().getName(),
                     plugin.getDescription().getVersion()));
 
     player.sendMessage(ChatColor.GOLD +
             plugin.getDescription().getWebsite());
 
   }
 
   @Command(
           aliases = {"debug"},
           desc = "Sends you more detailed debug information",
           max = 0
   )
   public static void debug(final CommandContext args,
                            final MyResidence plugin,
                            final Nation nation,
                            final Player player,
                            final Session session) {
 
     if (!session.isDebugger()) {
       session.setDebugger(true);
       Chat.sendMessage(player, "&dYou will now receive more detailed debug information!");
     } else {
       session.setDebugger(false);
       Chat.sendMessage(player, "&dYou will not receive debug information anymore!");
     }
 
   }
 
   @Command(
           aliases = {"searchconflicts"},
           desc = "Search the MyResidence database for conflicts",
           max = 0
   )
   @CommandPermissions("myresidence.admin")
   public static void searchConflicts(final CommandContext args,
                                      final MyResidence plugin,
                                      final Nation nation,
                                      final Player player,
                                      final Session session) {
 
    Chat.sendMessage(player, "&2MyResidence is checking the database in background. Expect lags!");
     Chat.sendMessage(player, "&2You'll get notified when interaction is needed.");
 
     nation.searchInvalidResidences(new InvalidResidenceListener() {
       public void invalidResidencesFound(final List<Residence> invalidResidences) {
         Chat.sendMessage(player, "&4Invalid residences found!");
 
         // Notify user about need confirmation.
         Chat.sendMessage(player, "&dDo you really want to remove &5{0}&d residences?", invalidResidences.size());
         Chat.sendMessage(player, "&dUse &5/task confirm&d to confirm this task!");
 
         session.setTaskActivator(Session.Activator.CONFIRM_COMMAND);
         session.setTask(new Runnable() {
           public void run() {
             for (Residence residenceToRemove : invalidResidences) {
               nation.remove(residenceToRemove);
             }
           }
         });
       }
     });
 
 
   }
 
 }
