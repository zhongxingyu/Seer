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
 import java.util.ArrayList;
 import java.util.List;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.command.BlockCommandSender;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.java.JavaPlugin;
 
 /**
  * This is the classe who are instancied for /cpplayers commands.
  * /cpplayers, it's a command that will count the numbers of players into a
  * region and active the repeater who are just at position of the command block
  * who launch this command with -1 to y position if the <expression> is true.
  * @author beaucoralk
  */
 public class CpPlayers extends JavaPlugin implements CommandExecutor {
     private LabPlugin plugin;
     public static final String usageMessage = "/cpplayers <xmin:xmax> <ymin:ymax> <zmin:zmax> <expression> <nbr>";
     public static final String ZERO = "0";
     public static final String UN = "1";
     
     public CpPlayers(LabPlugin _plugin) {
         plugin = _plugin;
     }
 
     @Override
     public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
         if (!(sender instanceof BlockCommandSender)) {
             sender.sendMessage(ChatColor.RED + "'/cpplayers' is only for command block.");
             return true;
         }
         
         if (args.length < 5) {
             sender.sendMessage(ChatColor.RED + "'/cpplayers' command must have 5 argument.");
             sender.sendMessage(ChatColor.RED + "Usage: " + usageMessage);
             return true;
         }
         
         BlockCommandSender _commandSender = (BlockCommandSender) sender;
         Location _location = _commandSender.getBlock().getLocation();
         Block _blockDown = _location.getWorld().getBlockAt(_location.getBlockX(), _location.getBlockY() - 1, _location.getBlockZ());

         // Récupération des bornes
         String[] x = args[0].split(":");
         String[] y = args[1].split(":");
         String[] z = args[2].split(":");
         Integer xmin = VariablesUtils.getInteger(x[0]);
         Integer xmax = VariablesUtils.getInteger(x[1]);
         Integer ymin = VariablesUtils.getInteger(y[0]);
         Integer ymax = VariablesUtils.getInteger(y[1]);
         Integer zmin = VariablesUtils.getInteger(z[0]);
         Integer zmax = VariablesUtils.getInteger(z[1]);
         if (xmin == null || xmax == null || ymin == null || ymax == null || zmin == null || zmax == null) {
             sender.sendMessage(ChatColor.RED + "Coords zone '" + args[0] + "," + args[1] + "," + args[2] + "' not correct.");
             sender.sendMessage(ChatColor.RED + "Usage: " + usageMessage);
             return true;
         }
         
         
         // Récupération de la liste des joueurs demandés
         List<Player> _listAllPlayers = new ArrayList<>(_location.getWorld().getEntitiesByClass(Player.class));
         Integer _nbrPlayers = 0;
         for (Player _player : _listAllPlayers) {
             if (_player.getLocation().getX() <= xmax && _player.getLocation().getX() >= xmin &&
                 _player.getLocation().getY() <= ymax && _player.getLocation().getY() >= ymin &&
                 _player.getLocation().getZ() <= zmax && _player.getLocation().getZ() >= zmin) {
                 _nbrPlayers++;
             }
         }
             
         // Récupération de l'expression
         String _expression = args[3];
         
         // Récupération du resultat attendu
         Integer _resultat = VariablesUtils.getInteger(args[4]);
         if (_resultat == null) {
             sender.sendMessage(ChatColor.RED + "Aim result '" + args[4] + "' not correct, only number accepted.");
             sender.sendMessage(ChatColor.RED + "Usage: " + usageMessage);
             return true;
         }
         
         // Calcul de l'expression
         if (_expression.trim().equals("=")) {
             if (_nbrPlayers.equals(_resultat)) {
                 if (_blockDown.getType().equals(Material.DIODE_BLOCK_OFF) || _blockDown.getType().equals(Material.DIODE_BLOCK_ON) || _blockDown.getType().equals(Material.DIODE)) {
                     _blockDown.setType(Material.DIODE_BLOCK_ON);
                 }
                 return true;
             }
         } else if (_expression.equals("!=")) {
             if (!_nbrPlayers.equals(_resultat)) {
                 if (_blockDown.getType().equals(Material.DIODE_BLOCK_OFF) || _blockDown.getType().equals(Material.DIODE_BLOCK_ON) || _blockDown.getType().equals(Material.DIODE)) {
                     _blockDown.setType(Material.DIODE_BLOCK_ON);
                 }
                 return true;
             }
         } else if (_expression.equals("<")) {
             if (_nbrPlayers.compareTo(_resultat) < 0) {
                 if (_blockDown.getType().equals(Material.DIODE_BLOCK_OFF) || _blockDown.getType().equals(Material.DIODE_BLOCK_ON) || _blockDown.getType().equals(Material.DIODE)) {
                     _blockDown.setType(Material.DIODE_BLOCK_ON);
                 }
                 return true;
             }
         } else if (_expression.equals(">")) {
             if (_nbrPlayers.compareTo(_resultat) > 0) {
                 if (_blockDown.getType().equals(Material.DIODE_BLOCK_OFF) || _blockDown.getType().equals(Material.DIODE_BLOCK_ON) || _blockDown.getType().equals(Material.DIODE)) {
                     _blockDown.setType(Material.DIODE_BLOCK_ON);
                 }
                 return true;
             }
         } else if (_expression.equals("<=")) {
             if (_nbrPlayers.compareTo(_resultat) <= 0) {
                 if (_blockDown.getType().equals(Material.DIODE_BLOCK_OFF) || _blockDown.getType().equals(Material.DIODE_BLOCK_ON) || _blockDown.getType().equals(Material.DIODE)) {
                     _blockDown.setType(Material.DIODE_BLOCK_ON);
                 }
                 return true;
             }
         } else if (_expression.equals(">=")) {
             if (_nbrPlayers.compareTo(_resultat) >= 0) {
                 if (_blockDown.getType().equals(Material.DIODE_BLOCK_OFF) || _blockDown.getType().equals(Material.DIODE_BLOCK_ON) || _blockDown.getType().equals(Material.DIODE)) {
                     _blockDown.setType(Material.DIODE_BLOCK_ON);
                 }
                 return true;
             }
         }
         
         return true;
     }
 }
