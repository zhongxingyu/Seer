 /*
  * LabPlugin
  * Copyright (C) 2013 beaucoralk <http://www.liveatblocks.fr> and contributors
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
  */
 package com.beaucoralk.bukkit.labplugin.commands;
 
 import com.beaucoralk.bukkit.labplugin.LabPlugin;
 import com.beaucoralk.bukkit.labplugin.utils.VariablesUtils;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.command.BlockCommandSender;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.plugin.java.JavaPlugin;
 
 /**
  * This is the classe who are instancied for /cpaddblock command.
  * /cpenchant, it's a command who add a block in x, y, z location, or in zone
  * bounded by min and max of this coordinates.
  *
  * @author beaucoralk
  */
 public class CpAddBlock extends JavaPlugin implements CommandExecutor {
     private LabPlugin plugin;
     public static final String usageMessage = "/cpaddblock <x>[:xmax] <y>[:ymax] <z>:[:zmax] <blockID>:[data]";
     
     public CpAddBlock(LabPlugin _plugin) {
         plugin = _plugin;
     }
     
     @Override
     public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
         if (!(sender instanceof BlockCommandSender)) {
             sender.sendMessage(ChatColor.RED + "'/cpplayers' is only for command block.");
             return true;
         }
         
        if (args.length == 4) {
             sender.sendMessage(ChatColor.RED + "'/cpenchant' command must have 4 arguments.");
             sender.sendMessage(ChatColor.RED + "Usage: " + usageMessage);
             return true;
         }
         
          //Définition du block visé par la commande
         String[] _material_data = args[3].split(":");
         Material _material = Material.matchMaterial(_material_data[0]);
         if (_material == null) {
             sender.sendMessage(ChatColor.RED + "Block '" + _material_data[0] + "' not found.");
             sender.sendMessage(ChatColor.RED + "Usage: " + usageMessage);
             return true;
         }
         Byte _data = 0;
         if (_material_data.length == 2) {
             _data = Byte.valueOf(_material_data[1]);
         }
         
         //Définition des coordonnées
         String[] x = args[0].split(":");
         String[] y = args[1].split(":");
         String[] z = args[2].split(":");
         Integer xmin, xmax = null, ymin, ymax = null, zmin, zmax = null;
         xmin = VariablesUtils.getInteger(x[0]);
         if (x.length == 2) {
             xmax = VariablesUtils.getInteger(x[1]);
         }
         ymin = VariablesUtils.getInteger(y[0]);
         if (y.length == 2) {
             ymax = VariablesUtils.getInteger(y[1]);
         }
         zmin = VariablesUtils.getInteger(z[0]);
         if (z.length == 2) {
             zmax = VariablesUtils.getInteger(z[1]);
         }
         if (xmin == null || ymin == null || zmin == null) {
             sender.sendMessage(ChatColor.RED + "Coords zone '" + args[0] + "," + args[1] + "," + args[2] + "' not correct.");
             sender.sendMessage(ChatColor.RED + "Usage: " + usageMessage);
             return true;
         }
         
         //Récupération des info du command block qui utilise la commande
         BlockCommandSender _commandSender = (BlockCommandSender) sender;
         Location _location = _commandSender.getBlock().getLocation();
         World _world = _location.getWorld();
         
         //Si on a xmax, ymax et zmax on rempli une zone du block
         if (xmax != null && ymax != null && zmax != null) {
             for (Integer i = xmin; i <= xmax; i++) {
                 for (Integer j = ymin; j <= ymax; j++) {
                     for (Integer k = zmin; k <= zmax; k++) {
                        Block blockWillChange = _world.getBlockAt(xmin, ymin, zmin);
                         blockWillChange.setTypeIdAndData(_material.getId(), _data, true);
                     }
                 }
             }
             Block blockWillChange = _world.getBlockAt(xmin, ymin, zmin);
             blockWillChange.setType(_material);
         }
         //Sinon c'est on ne prend pas une zone, mais juste une coordonné
         else {
             Block blockWillChange = _world.getBlockAt(xmin, ymin, zmin);
             blockWillChange.setTypeIdAndData(_material.getId(), _data, true);
         }
         
         return true;
     }
 }
