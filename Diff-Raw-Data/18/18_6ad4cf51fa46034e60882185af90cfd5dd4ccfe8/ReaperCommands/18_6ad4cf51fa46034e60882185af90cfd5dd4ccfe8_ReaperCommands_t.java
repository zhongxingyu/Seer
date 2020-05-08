 /* This file is part of FearTheReaper.
 
     FearTheReaper is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     FearTheReaper is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with FearTheReaper.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.herocraftonline.fearthereaper.commands;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import com.herocraftonline.fearthereaper.FearTheReaper;
 import com.herocraftonline.fearthereaper.spawnpoint.Spawn;
 import com.herocraftonline.fearthereaper.spawnpoint.SpawnPoint;
 import com.herocraftonline.fearthereaper.utils.GraveyardUtils;
 
 public class ReaperCommands implements CommandExecutor {
     public final FearTheReaper plugin;
 
     public ReaperCommands(FearTheReaper instance) {
         this.plugin = instance;
     }
 
     public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
         if (args.length == 0) {
             return false;
         }
 
         if (args[0].equalsIgnoreCase("closest")) {
             closest(sender);
         } else if (args[0].equalsIgnoreCase("info")) {
             info(sender, args);
         } else if (args[0].equalsIgnoreCase("list")) {
             list(sender, args);
         } else if (args[0].equalsIgnoreCase("tp")) {
             teleport(sender, args);
         } else if (args[0].equalsIgnoreCase("message")) {
             message(sender, args);
         } else if (args[0].equalsIgnoreCase("reload")) {
             reload(sender);
         } else if (args[0].equalsIgnoreCase("group")) {
             group(sender, args);
         } else if (args[0].equalsIgnoreCase("add")) {
             add(sender, args);
         } else if (args[0].equalsIgnoreCase("delete")) {
             delete(sender, args);
         }
 
         return true;
     }
 
     private void closest(CommandSender sender) {
         if (!(sender instanceof Player)) {
             noConsole(sender);
             return;
         }
         if (!sender.hasPermission("graveyard.command.closest")) {
             noPermission(sender);
             return;
         }
         Player player = (Player) sender;
         Spawn point = SpawnPoint.getClosestAllowed(player);
         commandLine(sender);
 
         if (point != null) {
             sender.sendMessage(ChatColor.WHITE + "Closest spawn point is: " + ChatColor.RED + SpawnPoint.getClosestAllowed(player).getName());
         } else {
             sender.sendMessage(ChatColor.WHITE + "Could not find the closest spawn point to your location.");
         }
 
         commandLine(sender);
     }
 
     private void info(CommandSender sender, String[] args) {
         if (!sender.hasPermission("graveyard.command.info")) {
             noPermission(sender);
             return;
         }
         Spawn point;
         if (args.length > 1) {
             point = SpawnPoint.get(GraveyardUtils.makeString(args));
             if (point == null) {
                 commandLine(sender);
                 sender.sendMessage("CHatColor.WHITE + Could not find the spawn point: " + args[1]);
                 commandLine(sender);
                 return;
             }
         } else {
             if (!(sender instanceof Player)) {
                 noConsole(sender);
                 return;
             } else {
                 point = SpawnPoint.getClosestAllowed((Player) sender);
             }
             if (point == null) {
                 commandLine(sender);
                 sender.sendMessage("ChatColor.WHITE + Could not find the closest spawn point.");
                 commandLine(sender);
                 return;
             }
         }
         commandLine(sender);
         sender.sendMessage(ChatColor.GRAY + point.getName() + ": " + ChatColor.WHITE + Math.round(point.getX()) + ", " + Math.round(point.getY()) + ", " + Math.round(point.getZ()));
        sender.sendMessage(ChatColor.WHITE + "Respawn Message: " + GraveyardUtils.replaceColors(point.getSpawnMessage()));
         sender.sendMessage(ChatColor.WHITE + "Group: " + ChatColor.GREEN + point.getGroup());
         sender.sendMessage(ChatColor.WHITE + "Permission: " + ChatColor.GREEN + "'graveyard.spawn." + point.getGroup() + "'");
         commandLine(sender);
         return;
     }
 
     private void list(CommandSender sender, String[] args) {
         if (!sender.hasPermission("graveyard.command.list")) {
             noPermission(sender);
             return;
         }
         String world = null;
         if (args.length > 1) {
             world = args[1];
         }
         commandLine(sender);
         for (Spawn point : FearTheReaper.SpawnPointList.values()) {
             if (world != null && !point.getWorldName().equalsIgnoreCase(world)) {
                 continue;
             }
             sender.sendMessage(ChatColor.GRAY + point.getName() + ": " + ChatColor.WHITE + Math.round(point.getX()) + ", " + Math.round(point.getY()) + ", " + Math.round(point.getZ()) + " : " + point.getWorldName());
         }
         commandLine(sender);
         return;
     }
 
     private void teleport(CommandSender sender, String[] args) {
         if (!sender.hasPermission("graveyard.command.teleport")) {
             noPermission(sender);
             return;
         } else if (!(sender instanceof Player)) {
             noConsole(sender);
             return;
         }
         if (args.length == 1) {
             commandLine(sender);
             sender.sendMessage(ChatColor.GRAY + "/graveyard" + ChatColor.WHITE + " tp " + ChatColor.RED + "Name");
             sender.sendMessage(ChatColor.WHITE + "Teleports player to the specified spawn point.");
             commandLine(sender);
             return;
         }
         Player player = (Player) sender;
         String pointname = GraveyardUtils.makeString(args);
         if (SpawnPoint.exists(pointname)) {
             player.teleport(SpawnPoint.get(pointname).getLocation());
             player.sendMessage("Teleporting to: " + pointname);
         } else {
             player.sendMessage("Spawnpoint '" + pointname + "' does not exist.");
         }
     }
 
     private void message(CommandSender sender, String[] args) {
         if (!sender.hasPermission("graveyard.command.message")) {
             noPermission(sender);
             return;
         } else if (args.length == 1) {
             commandLine(sender);
             sender.sendMessage(ChatColor.GRAY + "/graveyard" + ChatColor.WHITE + " message " + ChatColor.RED + "Spawn Message");
             sender.sendMessage(ChatColor.WHITE + "Changes the respawn message of the closest spawn point.");
             commandLine(sender);
             return;
         }
         Player player = (Player) sender;
         String message = GraveyardUtils.makeString(args);
         Spawn closest = SpawnPoint.getClosestAllowed(player);
         if (closest == null) {
             commandLine(sender);
             sender.sendMessage(ChatColor.GRAY + "/graveyard" + ChatColor.WHITE + " message " + ChatColor.RED + "Spawn Message");
             sender.sendMessage(ChatColor.WHITE + "Changes the respawn message of the closest spawn point.");
             commandLine(sender);
             return;
         }
         closest.setSpawnMessage(message);
         player.sendMessage(ChatColor.GRAY + closest.getName() + ChatColor.WHITE + " respawn message set to " + ChatColor.GREEN + message);
         SpawnPoint.save(closest);
         return;
     }
 
     private void reload(CommandSender sender) {
         if (!sender.hasPermission("graveyard.command.reload")) {
             noPermission(sender);
             return;
         }
         commandLine(sender);
         FearTheReaper.SpawnPointList.clear();
         SpawnPoint.loadAllPoints();
         sender.sendMessage(ChatColor.WHITE + "All spawn points have been reloaded.");
         commandLine(sender);
         return;
     }
 
     private void group(CommandSender sender, String[] args) {
         if (!sender.hasPermission("graveyard.command.group")) {
             noPermission(sender);
             return;
         } else if (!(sender instanceof Player)) {
             noConsole(sender);
             return;
         }
         Player player = (Player) sender;
         if (args.length > 1) {
             Spawn closest = SpawnPoint.getClosestAllowed(player);
             closest.setGroup(args[1]);
             player.sendMessage(ChatColor.GRAY + closest.getName() + ChatColor.WHITE + " group set to " + ChatColor.GREEN + args[1]);
             player.sendMessage(ChatColor.WHITE + "Permission is now " + ChatColor.GREEN + "'graveyard.spawn." + args[1] + "'");
             SpawnPoint.save(closest);
             return;
         } else {
             commandLine(sender);
             sender.sendMessage(ChatColor.GRAY + "/graveyard" + ChatColor.WHITE + " message " + ChatColor.RED + "Spawn Message");
             sender.sendMessage(ChatColor.WHITE + "Changes the group of the closest spawn point.");
             commandLine(sender);
             return;
         }
     }
 
     private void add(CommandSender sender, String[] args) {
         if (!sender.hasPermission("graveyard.command.add")) {
             noPermission(sender);
             return;
         } else if (!(sender instanceof Player)) {
             noConsole(sender);
             return;
         }
         Player player = (Player) sender;
         if (args.length > 1) {
             String pointname = GraveyardUtils.makeString(args);
             if (SpawnPoint.exists(pointname)) {
                 player.sendMessage(ChatColor.WHITE + "A spawn point with that name already exists!");
                 return;
             }
             Spawn newpoint = new Spawn(pointname, player);
             SpawnPoint.save(newpoint);
             SpawnPoint.addSpawnPoint(newpoint);
             commandLine(sender);
             sender.sendMessage(ChatColor.DARK_GREEN + "Adding: " + ChatColor.GRAY + pointname);
             commandLine(sender);
             SpawnPoint.save(newpoint);
             return;
         } else {
             commandLine(sender);
             sender.sendMessage(ChatColor.GRAY + "/graveyard" + ChatColor.WHITE + " add " + ChatColor.RED + "Name");
             sender.sendMessage(ChatColor.WHITE + "Adds a new spawn point at current location.");
             commandLine(sender);
             return;
         }
     }
 
     private void delete(CommandSender sender, String[] args) {
         if (!sender.hasPermission("graveyard.command.add")) {
             noPermission(sender);
             return;
         } 
         if (args.length > 1) {
             String pointname = GraveyardUtils.makeString(args);
             if (SpawnPoint.deleteSpawnPoint(pointname)) {
                 commandLine(sender);
                 sender.sendMessage(ChatColor.DARK_GREEN + "Deleting: " + ChatColor.GRAY + pointname);
                 commandLine(sender);
             } else {
                 commandLine(sender);
                 sender.sendMessage(ChatColor.RED + "Error. Point does not exist.");
                 commandLine(sender);
             }
             return;
         } else {
             commandLine(sender);
             sender.sendMessage(ChatColor.GRAY + "/graveyard" + ChatColor.WHITE + " remove " + ChatColor.RED + "Name");
             sender.sendMessage(ChatColor.WHITE + "Permenently deletes specified spawn point.");
             commandLine(sender);
             return;
         }
     }
     private void commandLine(CommandSender sender) {
         sender.sendMessage(ChatColor.DARK_GREEN + "*--------------------------------------*");
     }
 
     private void noPermission(CommandSender sender) {
         commandLine(sender);
         sender.sendMessage(ChatColor.WHITE + "Sorry you do not have permission to use that command.");
         commandLine(sender);
     }
 
     private void noConsole(CommandSender sender) {
         commandLine(sender);
         sender.sendMessage(ChatColor.WHITE + "Sorry you cannot use that command from the console.");
         commandLine(sender);
     }
 }
