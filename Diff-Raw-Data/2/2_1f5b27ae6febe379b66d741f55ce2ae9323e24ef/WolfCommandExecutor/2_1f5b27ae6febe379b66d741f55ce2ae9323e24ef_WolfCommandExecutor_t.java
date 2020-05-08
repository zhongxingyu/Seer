 /*
  * Copyright (C) 2011 halvors <halvors@skymiastudios.com>
  * Copyright (C) 2011 speeddemon92 <speeddemon92@gmail.com>
  *
  * This file is part of Wolf.
  *
  * Wolf is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * Wolf is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with Wolf.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.halvors.wolf.command;
 
 import java.util.List;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Wolf;
 
 import com.halvors.wolf.WolfPlugin;
 import com.halvors.wolf.wolf.SelectedWolfManager;
 import com.halvors.wolf.wolf.WolfManager;
 import com.halvors.wolf.wolf.WolfTable;
 
 /**
  * Represents a CommandExecutor
  * 
  * @author halvors
  */
 public class WolfCommandExecutor implements CommandExecutor {
     private final WolfPlugin plugin;
 
 //    private final ConfigManager configManager;
     private final WolfManager wolfManager;
     private final SelectedWolfManager selectedWolfManager;
 
     public WolfCommandExecutor(final WolfPlugin plugin) {
         this.plugin = plugin;
 //        this.configManager = plugin.getConfigManager();
         this.wolfManager = plugin.getWolfManager();
         this.selectedWolfManager = plugin.getSelectedWolfManager();
     }
 
     public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
     	if (sender instanceof Player) {
     		Player player = (Player)sender; 
 
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
    				} else if (subCommand.equalsIgnoreCase("status")) {
    					if (plugin.hasPermissions(player, "Wolf.wolf.status")) {
    						if (args.length >= 2) {
    							String owner = args[1];
 
    							if (wolfManager.hasWolf(owner)) {
    								player = (Player) plugin.getServer().getPlayer(args[1]);
    							} else {
    								player.sendMessage(ChatColor.RED + "Player doesn't have a wolf.");
    							}
    						}
 
    						if (player != null) {
    							showPlayerWolves(player);
    						}
 
    						return true;
    					}
    				} else if (subCommand.equalsIgnoreCase("name")) {
    					if (plugin.hasPermissions(player, "Wolf.wolf.name")) {
    						String name = player.getName();
 
    						if (selectedWolfManager.hasSelectedWolf(name)) {
    							Wolf wolf = (Wolf) selectedWolfManager.getSelectedWolf(name);
 
    							if (wolfManager.hasWolf(wolf)) {
    								com.halvors.wolf.wolf.Wolf wolf1 = wolfManager.getWolf(wolf);
 
    								player.sendMessage("This is " + ChatColor.YELLOW + wolf1.getName() + ChatColor.WHITE + ".");
    							}
    						}
 
    						return true;
    					}
    				} else if (subCommand.equalsIgnoreCase("setname")) {
    					if (plugin.hasPermissions(player, "Wolf.wolf.setname")) {
    						Wolf wolf = null;
    						String owner = player.getName();
    						String name = null;
 
    						if (args.length == 2) {
    							if (selectedWolfManager.hasSelectedWolf(owner)) {
    								wolf = (Wolf) selectedWolfManager.getSelectedWolf(owner);
    								name = args[1];
    							} else {
    								player.sendMessage(ChatColor.RED + "No wolf selected.");
     								
    								return true;
    							}
    						} else if (args.length == 3){
    							String oldName = args[1];
    							
    							if (wolfManager.hasWolf(oldName, owner)) {
    								wolf = (Wolf) wolfManager.getWolf(oldName, owner).getEntity();
    								name = args[2];
    							} else {
    								player.sendMessage(ChatColor.RED + "Wolf doesn't exists.");
     								
    								return true;
    							}
    						} else {
    							player.sendMessage(ChatColor.RED + "Invalid number of arguments");
     							
    							return true;
    						}
 
    						if (wolfManager.hasWolf(wolf) && wolf != null && name != null) {
    							com.halvors.wolf.wolf.Wolf wolf1 = wolfManager.getWolf(wolf);
    							wolf1.setName(name);
    							
    							player.sendMessage("Wolf name set to " + ChatColor.YELLOW + name + ChatColor.WHITE + ".");
    						}
    						
    						return true;
    					}
    				} else if (subCommand.equalsIgnoreCase("call")) {
    					if (plugin.hasPermissions(player, "Wolf.wolf.call")) {
     					if (args.length == 2) {
     						String name = args[1];
     						String owner = player.getName();
 
     						if (wolfManager.hasWolf(name, owner)) {
     							Wolf wolf = (Wolf) wolfManager.getWolf(name, owner).getEntity();
     							wolf.teleport(player);
 
     							player.sendMessage(ChatColor.GREEN + "Your wolf is coming.");
     						}
    						}
     						
    						return true;
    					}
    				} else if (subCommand.equalsIgnoreCase("stop")) {
    					if (plugin.hasPermissions(player, "Wolf.wolf.stop")) {
    						Wolf wolf = null;
    						String owner = player.getName();
 
    						if (args.length <= 1) {
    							if (selectedWolfManager.hasSelectedWolf(owner)) {
    								wolf = (Wolf)selectedWolfManager.getSelectedWolf(owner);
    							} else {
    								player.sendMessage(ChatColor.RED + "No wolf selected.");
    							}
    						} else {
    							String name = args[1];
 
    							if (wolfManager.hasWolf(name, owner)) {
    								wolf = (Wolf) wolfManager.getWolf(name, owner).getEntity();
    							} else {
   								player.sendMessage(ChatColor.RED + "Wolf doesn't exists.");
    							}
    						}
 
    						if (wolfManager.hasWolf(wolf) && wolf != null) {
    							com.halvors.wolf.wolf.Wolf wolf1 = wolfManager.getWolf(wolf);
 
    							wolf.setTarget(null);
 
    							player.sendMessage(ChatColor.YELLOW + wolf1.getName() + ChatColor.WHITE + " has stopped attacking.");
    						}
 
    						return true;
    					}
     					/*
                 } else if (subCommand.equalsIgnoreCase("target")){
                     if (plugin.hasPermissions(player, "Wolf.wolf.target")) {
                         if (args.length == 2) {
                             Player target = plugin.getServer().getPlayer(args[1]);
 
                             if (target != null) {
 
                             } else {
                                 player.sendMessage(ChatColor.RED + "Target doesn't exist.");
                             }
                         }
 
                     return true;
                 }
  				 */
    				} else if (subCommand.equalsIgnoreCase("give")) {
    					if (plugin.hasPermissions(player, "Wolf.wolf.give")) {
    						Wolf wolf = null;
    						String owner = player.getName();
    						Player receiver = null;
 
    						if (args.length == 2) {
    							if (selectedWolfManager.hasSelectedWolf(owner)) {
    								wolf = (Wolf) selectedWolfManager.getSelectedWolf(owner);
    								receiver = (Player) plugin.getServer().getPlayer(args[1]);
    							} else {
    								player.sendMessage(ChatColor.RED + "No wolf selected.");
     								
    								return true;
    							}
    						} else if (args.length == 3) {
    							String name = args[1];
 
    							if (wolfManager.hasWolf(name, owner)) {
    								wolf = (Wolf) wolfManager.getWolf(name, owner).getEntity();
    								receiver = (Player) plugin.getServer().getPlayer(args[2]);
    							} else {
    								player.sendMessage(ChatColor.RED + "Wolf doesn't exists.");
     								
    								return true;
    							}
    						} else {
    							player.sendMessage(ChatColor.RED + "Correct Syntax is /wolf give <player> (wolf)");
     							
    							return true;
    						}
 
    						if (wolfManager.hasWolf(wolf) && wolf != null && receiver != null) {
    							com.halvors.wolf.wolf.Wolf wolf1 = wolfManager.getWolf(wolf);
    							String name = wolf1.getName();
    							String to = receiver.getName();
 
    							wolf1.setOwner(receiver);
    							wolf.teleport(receiver);
 
    							player.sendMessage(ChatColor.YELLOW + name + ChatColor.WHITE + " was given to " + ChatColor.YELLOW + to + ChatColor.WHITE + ".");
    							receiver.sendMessage("You got the wolf " + ChatColor.YELLOW + name + ChatColor.WHITE + " from " + ChatColor.YELLOW + owner + ChatColor.WHITE + ".");
    						}
 
    						return true;
    					}
    				} else if (subCommand.equalsIgnoreCase("release")) {
    					if (plugin.hasPermissions(player, "Wolf.wolf.elease")) {
    						Wolf wolf = null;
    						String owner = player.getName();
 
                            if (args.length == 1) {
                                if (selectedWolfManager.hasSelectedWolf(owner)) {
                                    wolf = (Wolf) selectedWolfManager.getSelectedWolf(owner);
                                } else {
                                    player.sendMessage(ChatColor.RED + "No wolf selected.");
                                     
                                    return true;
                                }
                            } else if (args.length == 2){
                                String name = args[1];
 
                                if (wolfManager.hasWolf(name, owner)) {
                                    wolf = (Wolf) wolfManager.getWolf(name, owner).getEntity();
                                } else {
                                    player.sendMessage(ChatColor.RED + "Wolf doesn't exists.");
                                     
                                    return true;
                                }
                            } else {
                                player.sendMessage(ChatColor.RED + "Too many arguments.");
                                 
                                return true;
                            }
 
 
                            if (wolfManager.hasWolf(wolf) && wolf != null) {
                                com.halvors.wolf.wolf.Wolf wolf1 = wolfManager.getWolf(wolf);
 
                                player.sendMessage(ChatColor.YELLOW + wolf1.getName() + ChatColor.WHITE + " has been released.");
                                 
                                wolfManager.releaseWolf(wolf);
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
         List<com.halvors.wolf.wolf.Wolf> wolves = wolfManager.getWolves(player);
         
         player.sendMessage(ChatColor.GREEN + plugin.getName() + ChatColor.GREEN + " (" + ChatColor.WHITE + plugin.getVersion() + ChatColor.GREEN + ")");
         
         if (!wolves.isEmpty()) {
             for (com.halvors.wolf.wolf.Wolf wolf1 : wolves) {
                 player.sendMessage(ChatColor.YELLOW + wolf1.getName());
             }
         } else {
             player.sendMessage(ChatColor.RED + "You have no wolves.");
         }
     }
 
     private void showWolves(Player player) {
         List<WolfTable> wolfTables = wolfManager.getWolfTables();
         
         player.sendMessage(ChatColor.GREEN + plugin.getName() + ChatColor.GREEN + " (" + ChatColor.WHITE + plugin.getVersion() + ChatColor.GREEN + ")");
         
         if (!wolfTables.isEmpty()) {
             for (WolfTable wolfTable : wolfTables) {
                 player.sendMessage(ChatColor.YELLOW + wolfTable.getName() + ChatColor.WHITE + " - " + wolfTable.getOwner());
             }
         } else {
             player.sendMessage(ChatColor.RED + "There is no tame wolves.");
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
         
         if (plugin.hasPermissions(player, "Wolf.wolf.status")) {
             player.sendMessage(command + "status" + ChatColor.YELLOW + " - Show your wolves.");
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
         
         if (plugin.hasPermissions(player, "Wolf.wolf.release")) {
             player.sendMessage(command + "release " + ChatColor.GREEN + "<" + ChatColor.WHITE + "name" + ChatColor.GREEN + ">" + ChatColor.YELLOW + " - Release your wolf.");
         }
     }
 }
