 /*
  * Copyright (C) 2011 halvors <halvors@skymiastudios.com>
  * Copyright (C) 2011 speeddemon92 <speeddemon92@gmail.com>
  *
  * This file is part of Lupi.
  *
  * Lupi is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * Lupi is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with Lupi.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.halvors.lupi.commands;
 
 import java.util.List;
 
 import org.bukkit.ChatColor;
 import org.bukkit.World;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Wolf;
 
 import com.halvors.lupi.Lupi;
 import com.halvors.lupi.util.ConfigManager;
 import com.halvors.lupi.util.WorldConfig;
 import com.halvors.lupi.wolf.SelectedWolfManager;
 import com.halvors.lupi.wolf.WolfManager;
 import com.halvors.lupi.wolf.WolfTable;
 
 /**
  * Represents a CommandExecutor
  * 
  * @author halvors
  */
 public class LupiCommandExecutor implements CommandExecutor {
     private Lupi plugin;
 
     private final ConfigManager configManager;;
 
     public LupiCommandExecutor(final Lupi plugin) {
         this.plugin = plugin;
         this.configManager = plugin.getConfigManager();
     }
 
     public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
         if (sender instanceof Player) {
             Player player = (Player) sender;
 
             if (args.length == 0) {
                 if (plugin.hasPermissions(player, "Wolf.wolf.list")) {
                     showPlayerWolves(player);
                 }
             } else {
                 String subCommand = args[0];
 
                 if (subCommand.equalsIgnoreCase("help")) {
                     if (plugin.hasPermissions(player, "Wolf.help")) {
                         showHelp(player, label);
 
                            return true;
                        }
                    } else if (subCommand.equalsIgnoreCase("list")) {
                        if (plugin.hasPermissions(player, "Wolf.list")) {
                            showWolves(player);
                            
                            return true;
                        }
                    } else if (subCommand.equalsIgnoreCase("name")) {
                        if (plugin.hasPermissions(player, "Wolf.wolf.name")) {
                            if (SelectedWolfManager.hasSelectedWolf(player)) {
                                Wolf wolf = SelectedWolfManager.getSelectedWolf(player);
 
                                if (WolfManager.hasWolf(wolf)) {
                                    com.halvors.lupi.wolf.Wolf wolf1 = WolfManager.getWolf(wolf);
 
                                    player.sendMessage("This wolf's name is " + ChatColor.YELLOW + wolf1.getName() + ChatColor.WHITE + ".");
                                }
                            }
 
                            return true;
                        }
                    } else if (subCommand.equalsIgnoreCase("setname")) {
                        if (plugin.hasPermissions(player, "Wolf.wolf.setname")) {
                            Wolf wolf = null;
                            String owner = player.getName();
                            String name = null;
                            String grammar = "";
 
                            if (args.length == 2) {
                                if (SelectedWolfManager.hasSelectedWolf(player)) {
                                    wolf = SelectedWolfManager.getSelectedWolf(player);
                                    name = args[1];
                                    grammar = "this";
                                } else {
                                    player.sendMessage(ChatColor.RED + "No wolf selected.");
                                     
                                    return true;
                                }
                            } else if (args.length == 3){
                                String oldName = args[1];
                                
                                if (WolfManager.hasWolf(oldName, owner)) {
                                    wolf = WolfManager.getWolf(oldName, owner).getEntity();
                                    name = args[2];
                                    grammar = "that";
                                } else {
                                    player.sendMessage(ChatColor.RED + "That wolf doesn't exists.");
                                     
                                    return true;
                                }
                            } else {
                                player.sendMessage(ChatColor.RED + "Too many arguments.");
                                 
                                return true;
                            }
 
                            if (WolfManager.hasWolf(wolf) && wolf != null && name != null) {
                                com.halvors.lupi.wolf.Wolf wolf1 = WolfManager.getWolf(wolf);
                                wolf1.setName(name);
                                
                                player.sendMessage("The name of " + grammar + " wolf has been set to " + ChatColor.YELLOW + name + ChatColor.WHITE + ".");
                            }
                            
                            return true;
                        }
                    } else if (subCommand.equalsIgnoreCase("call")) {
                        if (plugin.hasPermissions(player, "Wolf.wolf.call")) {
                         if (args.length == 2) {
                             String name = args[1];
                             String owner = player.getName();
 
                             if (WolfManager.hasWolf(name, owner)) {
                                 Wolf wolf = WolfManager.getWolf(name, owner).getEntity();
                                 wolf.teleport(player);
 
                                 player.sendMessage(ChatColor.GREEN + "Your wolf is on its way.");
                             }
                            }
                             
                            return true;
                        }
                    } else if (subCommand.equalsIgnoreCase("stop")) {
                        if (plugin.hasPermissions(player, "Wolf.wolf.stop")) {
                            Wolf wolf = null;
                            String owner = player.getName();
 
                            if (args.length <= 1) {
                                if (SelectedWolfManager.hasSelectedWolf(player)) {
                                    wolf = SelectedWolfManager.getSelectedWolf(player);
                                } else {
                                    player.sendMessage(ChatColor.RED + "No wolf selected.");
                                }
                            } else {
                                String name = args[1];
 
                                if (WolfManager.hasWolf(name, owner)) {
                                    wolf = WolfManager.getWolf(name, owner).getEntity();
                                } else {
                                   player.sendMessage(ChatColor.RED + "That wolf doesn't exists.");
                                }
                            }
 
                            if (WolfManager.hasWolf(wolf) && wolf != null) {
                                com.halvors.lupi.wolf.Wolf wolf1 = WolfManager.getWolf(wolf);
 
                                wolf.setTarget(null);
 
                                player.sendMessage(ChatColor.YELLOW + wolf1.getName() + ChatColor.WHITE + " has stopped attacking.");
                            }
 
                            return true;
                        }
                    } else if (subCommand.equalsIgnoreCase("give")) {
                        if (plugin.hasPermissions(player, "Wolf.wolf.give")) {
                            Wolf wolf = null;
                            String owner = player.getName();
                            Player receiver = null;
                            World world = player.getWorld();
                            WorldConfig worldConfig = configManager.getWorldConfig(world);
                            
                            if (args.length == 2) {
                                if (SelectedWolfManager.hasSelectedWolf(player)) {
                                    wolf = SelectedWolfManager.getSelectedWolf(player);
                                    receiver = plugin.getServer().getPlayer(args[1]);
                                } else {
                                    player.sendMessage(ChatColor.RED + "No wolf selected.");
                                     
                                    return true;
                                }
                            } else if (args.length == 3) {
                                String name = args[1];
 
                                if (WolfManager.hasWolf(name, owner)) {
                                    wolf = WolfManager.getWolf(name, owner).getEntity();
                                    receiver = plugin.getServer().getPlayer(args[2]);
                                } else {
                                    player.sendMessage(ChatColor.RED + "That wolf doesn't exists.");
                                     
                                    return true;
                                }
                            } else {
                                player.sendMessage(ChatColor.RED + "Too many arguments.");
                                 
                                return true;
                            }
 
                            if (WolfManager.hasWolf(wolf) && wolf != null && receiver != null) {
                                com.halvors.lupi.wolf.Wolf wolf1 = WolfManager.getWolf(wolf);
                                String name = wolf1.getName();
                                String to = receiver.getName();
                                int limit = worldConfig.wolfLimit;
                                
                         	   if (limit > 0) {
                                    List<WolfTable> wts = WolfManager.getWolfTables(player);
                                    
                                    if (limit <= wts.size()) {
                                        player.sendMessage("You can't give " + ChatColor.YELLOW + name + ChatColor.WHITE + " to " + ChatColor.YELLOW + to + ChatColor.WHITE + " because he has reached the limit, limit is " + ChatColor.YELLOW + Integer.toString(limit) + ChatColor.WHITE + " wolves.");
                                        receiver.sendMessage(owner + " tried to give you " + ChatColor.YELLOW + name + ChatColor.WHITE + " but you can't receive the wolf because the limit is " + ChatColor.YELLOW + Integer.toString(limit)  + ChatColor.WHITE + " wolves.");
                                        
                                        return true;
                                    }
                                }
                                
                                wolf1.setOwner(receiver);
                                wolf.teleport(receiver);
                                
                                player.sendMessage(ChatColor.YELLOW + name + ChatColor.WHITE + " was given to " + ChatColor.YELLOW + to + ChatColor.WHITE + ".");
                                receiver.sendMessage("You have received " + ChatColor.YELLOW + name + ChatColor.WHITE + " from " + ChatColor.YELLOW + owner + ChatColor.WHITE + ".");
                            }
 
                            return true;
                        }
                    } else if (subCommand.equalsIgnoreCase("release")) {
                        if (plugin.hasPermissions(player, "Wolf.wolf.release")) {
                            Wolf wolf = null;
                            String owner = player.getName();
 
                            if (args.length == 1) {
                                if (SelectedWolfManager.hasSelectedWolf(player)) {
                                    wolf = SelectedWolfManager.getSelectedWolf(player);
                                } else {
                                    player.sendMessage(ChatColor.RED + "No wolf selected.");
                                     
                                    return true;
                                }
                            } else if (args.length == 2){
                                String name = args[1];
 
                                if (WolfManager.hasWolf(name, owner)) {
                                    wolf = WolfManager.getWolf(name, owner).getEntity();
                                } else {
                                    player.sendMessage(ChatColor.RED + "That wolf doesn't exists.");
                                     
                                    return true;
                                }
                            } else {
                                player.sendMessage(ChatColor.RED + "Too many arguments.");
                                 
                                return true;
                            }
 
 
                            if (WolfManager.hasWolf(wolf) && wolf != null) {
                                com.halvors.lupi.wolf.Wolf wolf1 = WolfManager.getWolf(wolf);
 
                                player.sendMessage(ChatColor.YELLOW + wolf1.getName() + ChatColor.WHITE + " has been released.");
                                 
                                WolfManager.releaseWolf(wolf);
                            }
 
                            return true;
                        }
                    } else {
                         if (plugin.hasPermissions(player, "Wolf.help")) {
                             showHelp(player, label);
 
                             return true;
                         }
                     }
                 }
             } else {
                 sender.sendMessage("Sorry but these commands are for in-game players only.");
             }
         
         return true;
     }
 
     private void showPlayerWolves(Player player) {
         List<com.halvors.lupi.wolf.Wolf> wolves = WolfManager.getWolves(player);
         
         player.sendMessage(ChatColor.GREEN + plugin.getName() + ChatColor.GREEN + " (" + ChatColor.WHITE + plugin.getVersion() + ChatColor.GREEN + ")");
         
         if (!wolves.isEmpty()) {
             for (com.halvors.lupi.wolf.Wolf wolf : wolves) {
                 player.sendMessage(ChatColor.YELLOW + wolf.getName());
             }
         } else {
             player.sendMessage(ChatColor.RED + "You have no wolves.");
         }
     }
 
     private void showWolves(Player player) {
         List<com.halvors.lupi.wolf.Wolf> wolves = WolfManager.getWolves(player.getWorld());
         
         player.sendMessage(ChatColor.GREEN + plugin.getName() + ChatColor.GREEN + " (" + ChatColor.WHITE + plugin.getVersion() + ChatColor.GREEN + ")");
         
         if (!wolves.isEmpty()) {
             for (com.halvors.lupi.wolf.Wolf wolf : wolves) {
                player.sendMessage(ChatColor.YELLOW + wolf.getName() + ChatColor.WHITE + " - " + wolf.getOwner());
             }
         } else {
             player.sendMessage(ChatColor.RED + "There are no tamed wolves.");
         }
     }
     
     private void showHelp(Player player, String label) {
         String command = "/" + label + " ";
         
         player.sendMessage(ChatColor.GREEN + plugin.getName() + ChatColor.GREEN + " (" + ChatColor.WHITE + plugin.getVersion() + ChatColor.GREEN + ")");
         player.sendMessage(ChatColor.RED + "[]" + ChatColor.WHITE + " Required, " + ChatColor.GREEN + "<>" + ChatColor.WHITE + " Optional.");
 
         if (plugin.hasPermissions(player, "Wolf.help")) {
             player.sendMessage(command + "help" + ChatColor.YELLOW + " - Show help.");
         }
         
         if (plugin.hasPermissions(player, "Wolf.list")) {
             player.sendMessage(command + "list" + ChatColor.YELLOW + " - Show a list of tamed wolves.");
         }
         
         if (plugin.hasPermissions(player, "Wolf.wolf.name")) {
             player.sendMessage(command + "name " + ChatColor.YELLOW + " - Show your wolf's name.");
         }
         
         if (plugin.hasPermissions(player, "Wolf.wolf.setname")) {
             player.sendMessage(command + "setname " + ChatColor.GREEN + "<" + ChatColor.WHITE + "name" + ChatColor.GREEN + ">" + ChatColor.YELLOW + " - Set your wolf's name.");
         }
         
         if (plugin.hasPermissions(player, "Wolf.wolf.call")) {
             player.sendMessage(command + "call " + ChatColor.GREEN + "<" + ChatColor.WHITE + "name"  + ChatColor.GREEN + ">" + ChatColor.YELLOW + " - Call your wolf.");
         }
         
         if (plugin.hasPermissions(player, "Wolf.wolf.stop")) {
             player.sendMessage(command + "stop " + ChatColor.GREEN + "<" + ChatColor.WHITE + "name"  + ChatColor.GREEN + ">" + ChatColor.YELLOW + " - Stop your wolf from attacking.");
         }
         
         if (plugin.hasPermissions(player, "Wolf.wolf.give")) {
             player.sendMessage(command + "give " + ChatColor.GREEN + "<" + ChatColor.WHITE + "name" + ChatColor.GREEN + "> [" + ChatColor.WHITE + "player" + ChatColor.GREEN + "]" + ChatColor.YELLOW + " - Release your wolf.");
         }
         
         if (plugin.hasPermissions(player, "Wolf.wolf.release")) {
             player.sendMessage(command + "release " + ChatColor.GREEN + "<" + ChatColor.WHITE + "name" + ChatColor.GREEN + ">" + ChatColor.YELLOW + " - Release your wolf.");
         }
     }
 }
