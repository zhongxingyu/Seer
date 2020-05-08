 /*
     This file is part of SudoAlias.
 
     SudoAlias is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     SudoAlias is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with SudoAlias.  If not, see <http://www.gnu.org/licenses/>.
 */
 package com.mike724.sudoalias;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.plugin.PluginDescriptionFile;
 
 public class SudoAliasCommandExecutor implements CommandExecutor {
 
     @Override
     public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
         if (cmd.getName().equalsIgnoreCase("sudoalias")) {
             if (args.length != 0) {
                 if (args.length == 1) {
                     if (args[0].equalsIgnoreCase("reload") && sender.hasPermission("SudoAlias.reload")) {
                         SudoAlias.getInstance().reload();
                         sender.sendMessage(ChatColor.GREEN + "SudoAlias configuration reloaded!");
                         return true;
                     }
                 }
             } else {
                 PluginDescriptionFile pdf = SudoAlias.getInstance().getDescription();
                 sender.sendMessage(ChatColor.AQUA + "SudoAlias " + ChatColor.YELLOW + "v" + pdf.getVersion() + ChatColor.AQUA + " by " + ChatColor.YELLOW + pdf.getAuthors().get(0) + ChatColor.AQUA + " is " + ChatColor.GREEN + "running");
                 return true;
             }
         }
         return false;
     }
 
 }
