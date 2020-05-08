 package au.com.addstar.birthdaygift;
 /*
 * BirthdayGift
 * Copyright (C) 2013 add5tar <copyright at addstar dot com dot au>
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import org.apache.commons.lang.StringUtils;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import au.com.addstar.birthdaygift.BirthdayGift.BirthdayRecord;
 
 public class CommandBirthday implements CommandExecutor {
 	private BirthdayGift plugin;
 	
 	public CommandBirthday(BirthdayGift plugin) {
 		this.plugin = plugin;
 	}
 	
 	/*
 	 * Handle the /birthday command
 	 */
 	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
 		if (cmd.getName().equalsIgnoreCase("birthday")) {
 			if (!(sender instanceof Player)) {
 				sender.sendMessage("Sorry, only in game players can use this command");
 			} else {
 				if (!plugin.RequirePermission((Player) sender, "birthdaygift.use")) { return false; }
 
 				Player player = (Player) sender;
 				BirthdayRecord birthday = plugin.getPlayerRecord(sender.getName());
 				if (args.length == 0) {
 					// Display player's birthday
 					if ((birthday == null) || (birthday.birthdayDate == null)) {
 						player.sendMessage(ChatColor.RED + "You have not set your birthday yet.");
						if (plugin.USDateFormat) {
							player.sendMessage(ChatColor.YELLOW + "Usage: /birthday MM-DD-YYYY    " + ChatColor.WHITE + "(eg. /birthday 12-31-2001)");
						} else {
							player.sendMessage(ChatColor.YELLOW + "Usage: /birthday DD-MM-YYYY    " + ChatColor.WHITE + "(eg. /birthday 31-12-2001)");
						}
 					} else {
 						String mydate = new SimpleDateFormat("dd MMM yyyy").format(birthday.birthdayDate); 
 						player.sendMessage(ChatColor.YELLOW + "Your birthday is currently set to: " + ChatColor.GREEN + mydate);
 					}
 				} else {
 					// Set player's birthday	
 					Date bdate;
 					try {
 						String input = StringUtils.join(args, "-");
 						bdate = new SimpleDateFormat(plugin.InputDateFormat).parse(input);
 					} catch (ParseException e) {
 						player.sendMessage(ChatColor.RED + "Invalid birthday! Please use format: " + plugin.InputDateFormat.toUpperCase());
 						return true;
 					}
 					
 					if (birthday == null) {
 						// Player's birthday is not set
 						BirthdayRecord rec = new BirthdayRecord();
 						rec.birthdayDate = bdate;
 						if (plugin.IsPlayerBirthday(rec)) {
 							// Don't allow players to set the birthday to today
 							player.sendMessage(ChatColor.RED + "Sorry, you cannot set your birthday to today.");
 							return true;
 						} else {
 							// Set player's birthday
 							plugin.SetPlayerBirthday(player.getName(), bdate);
 							String mydate = new SimpleDateFormat("dd MMM yyyy").format(bdate); 
 							player.sendMessage(ChatColor.YELLOW + "Your birthday is now set to: " + ChatColor.GREEN + mydate);
 						}
 					} else {
 						// Don't allow players to change their birthday once it's set
 						player.sendMessage(ChatColor.RED + "Sorry, you cannot change your birthday");
 					}
 				}
 			}
 		}
 		return true;
 	}
 }
