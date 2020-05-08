 /*
  * This file is part of Lore.
  *
  * Copyright (c) 2013, AlmuraDev <http://www.almuradev.com/>
  * Lore is licensed under the Almura Development License.
  *
  * Lore is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * Lore is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License. If not,
  * see <http://www.gnu.org/licenses/> for the GNU General Public License.
  */
 package com.almuradev.lore;
 
 import org.bukkit.Bukkit;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.meta.BookMeta;
 
 public class LoreCommands implements CommandExecutor {
 	private final LorePlugin plugin;
 
 	public LoreCommands(LorePlugin plugin) {
 		this.plugin = plugin;
 	}
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
 		if (command.getName().equalsIgnoreCase("lore")) {
 			Player player = null;
 			Boolean isPlayer = false;
 
 			if (sender instanceof Player) {
 				isPlayer = true;
 				player = (Player) sender;
 			}
 
 			if (args.length > 0) {
 				switch (args[0].toLowerCase()) {
 					case "give":
						if (args.length > 2) {
 							if ((isPlayer && player.hasPermission("lore.command.give")) || !isPlayer) {
 								Player target = Bukkit.getPlayerExact(args[1]);
 								if (target != null) {
 									if (plugin.getConfiguration().verifyBook(args[2])) {
 										target.getInventory().addItem(plugin.getConfiguration().getBook(args[2]));
 									} else {
 										sender.sendMessage("Sorry, " + args[2] + " does not exist within Lore's book library.");
 										return true;
 									}
 									if (target.getName() != sender.getName()) {
 										sender.sendMessage("You've given " + args[1] + " a copy of " + args[2] + ".");
 									} else {
 										sender.sendMessage("You've given yourself a copy of " + args[2] + ".");
 									}
 								} else {
 									sender.sendMessage("Unable to give " + args[1] + " a copy of " + args[2] + ", is that player online?");
 								}
 							}
 							return true;
 						}
 						return false;
 
 					case "create":
						if (args.length > 2) {
 							if (isPlayer && player.hasPermission("lore.command.create")) {
 								if (!(player.getItemInHand().getItemMeta() instanceof BookMeta)) {
 									sender.sendMessage("Held item must be a signed book.");
 								} else if (plugin.getConfiguration().verifyBook(args[1])) {
 									sender.sendMessage(args[1] + " already exists in Lore's book library.");
 								} else {
 									BookMeta meta = (BookMeta) player.getItemInHand().getItemMeta();
 									if (meta.hasTitle() == false) {
 										sender.sendMessage("You must sign this book first before adding it to Lore's book library.");
 										return true;
 									}
 									plugin.getConfiguration().createBook(args[1], meta);
 									sender.sendMessage(args[1] + " has been added to Lore's book library.");
 								}
 							} else if (!isPlayer) {
 								sender.sendMessage("You must be logged in to perform that command!");
 							}
 							return true;
 						}
 						return false;
 
 					case "remove":
						if (args.length > 1) {
 							if (isPlayer && player.hasPermission("lore.command.remove")) {
 								if (plugin.getConfiguration().verifyBook(args[1])) {
 									plugin.getConfiguration().removeBook(args[1]);
 									sender.sendMessage(args[1] + " has been removed from Lore's book library.");
 								} else {
 									sender.sendMessage(args[1] + " does not exist in Lore's book library.");
 								}
 							} else if (!isPlayer) {
 								sender.sendMessage("You must be logged in to perform that command!");
 							}
 							return true;
 						}
 						return false;
 
 					case "join":
						if (args.length > 2) {
 							if (args[1].equalsIgnoreCase("add") && isPlayer && player.hasPermission("lore.command.join.add")) {
 								if (plugin.getConfiguration().addJoinBook(args[2])) {
 									sender.sendMessage(args[2] + " was added to join list.");
 								} else {
 									sender.sendMessage(args[2] + " is already on the join list.");
 								}
 							} else if (args[1].equalsIgnoreCase("remove") && isPlayer && player.hasPermission("lore.command.join.remove")) {
 								if (plugin.getConfiguration().removeJoinBook(args[2])) {
 									sender.sendMessage(args[2] + " was removed from the join list.");
 								} else {
 									sender.sendMessage(args[2] + " is not on the join list.");
 								}
 							} else if (!isPlayer) {
 								sender.sendMessage("You must be logged in to perform that command!");
 							}
 							return true;
 						}
 						return false;
 
 					case "respawn":
						if (args.length > 2) {
 							if (args[1].equalsIgnoreCase("add") && isPlayer && player.hasPermission("lore.command.respawn.add")) {
 								if (plugin.getConfiguration().addRespawnBook(args[2])) {
 									sender.sendMessage(args[2] + " was added to respawn list.");
 								} else {
 									sender.sendMessage(args[2] + " is already on the respawn list.");
 								}
 							} else if (args[1].equalsIgnoreCase("remove") && isPlayer && player.hasPermission("lore.command.respawn.remove")) {
 								if (plugin.getConfiguration().removeRespawnBook(args[2])) {
 									sender.sendMessage(args[2] + " was removed from the respawn list.");
 								} else {
 									sender.sendMessage(args[2] + " is not on the respawn list.");
 								}
 							} else if (!isPlayer) {
 								sender.sendMessage("You must be logged in to perform that command!");
 							}
 							return true;
 						}
 						return false;
 				}
 				return true;
 			}
 		}
 		return false;
 	}
 }
