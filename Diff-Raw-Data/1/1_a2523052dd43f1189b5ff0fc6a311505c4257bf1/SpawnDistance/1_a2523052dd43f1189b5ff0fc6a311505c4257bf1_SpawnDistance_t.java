 /*
  * Copyright (C) 2013 Dabo Ross <http://www.daboross.net/>
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
 package net.daboross.bukkitdev.spawndistance;
 
 import org.bukkit.ChatColor;
 import org.bukkit.World;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Listener;
 import org.bukkit.plugin.java.JavaPlugin;
 
 /**
  *
  * @author daboross
  */
 public class SpawnDistance extends JavaPlugin implements Listener {
 
     @Override
     public void onEnable() {
     }
 
     @Override
     public void onDisable() {
     }
 
     @Override
     public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
         if (cmd.getName().equalsIgnoreCase("spawndistance")) {
             if (!(sender instanceof Player)) {
                 sender.sendMessage("You must be a player to execute this command.");
             }
             Player player = (Player) sender;
             World world = player.getWorld();
             double distance = player.getLocation().distance(world.getSpawnLocation());
             distance = ((int) (distance * 10)) / 10.0;
             sender.sendMessage(ChatColor.GREEN + "You are " + ChatColor.RED + distance + ChatColor.GREEN + " blocks away from spawn.");
         } else {
             sender.sendMessage("SpawnDistance doesn't know about the command /" + cmd.getName());
         }
         return true;
     }
 }
