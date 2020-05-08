 package com.hsun324.protection.commands;
 
 import com.hsun324.protection.Protection;
 import com.hsun324.protection.ProtectionSystem;
 import com.hsun324.protection.config.ProtectionConfiguration;
 import com.hsun324.protection.config.ProtectionConfiguration.ListMode;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 public class LoginCommandHandler
 	implements CommandExecutor
 {
 	private static final LoginCommandHandler instance = new LoginCommandHandler();
 
	public static LoginCommandHandler getInstance() {
 		return instance;
 	}
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command command, String alias, String[] arguments)
 	{
 		boolean isPlayer = sender instanceof Player;
 		Player player = null;
 		if (isPlayer)
 			player = (Player)sender;
 		if (arguments.length < 1)
 		{
 			outputUsage(sender);
 		}
 		else
 		{
 			ProtectionAction action = getAction(arguments[0]);
 			if (action == ProtectionAction.HELP)
 			{
 				outputUsage(sender);
 			}
 			else if (action == ProtectionAction.FUNCTION)
 			{
 				if ((arguments.length > 2) || (!isPlayer))
 				{
 					outputUsage(sender);
 				}
 				else if (player.hasPermission("protection.access"))
 				{
 					if (arguments.length == 1)
 					{
 						if (ProtectionSystem.isPlayerLoggedIn(player.getName()))
 							player.sendMessage("\u00A7e[LoginProtection] I'm sorry, but you seem to already be logged in.");
 						else
 							attemptLogin(player, arguments[0]);
 					}
 					else if (arguments.length == 2)
 					{
 						attemptSet(player, arguments[0], arguments[1]);
 					}
 				}
 				else
 				{
 					outputDisallowed(sender);
 				}
 			}
 			else if (action == ProtectionAction.LOGOFF)
 			{
 				if ((arguments.length > 2) || (!isPlayer))
 				{
 					outputUsage(sender);
 				}
 				else if (player.hasPermission("protection.access"))
 				{
 					if (ProtectionSystem.isPlayerLoggedIn(player.getName()))
 					{
 						ProtectionSystem.setPlayerLoggedIn(player.getName(), false);
 						player.sendMessage("\u00A7e[LoginProtection] Thank you for using Protection.");
 					}
 					else {
 						player.sendMessage("\u00A7e[LoginProtection] I'm sorry, but you don't seem to be logged in.");
 					}
 				}
 				else {
 					outputDisallowed(sender);
 				}
 			}
 			else if (action == ProtectionAction.OPTIN || action == ProtectionAction.OPTOUT)
 			{
 				if ((arguments.length > 2) || (!isPlayer))
 				{
 					outputUsage(sender);
 				}
 				else if (player.hasPermission("loginprotection.access"))
 				{
 					if (ProtectionConfiguration.playerControllable)
 					{
 						if (ProtectionSystem.isPlayerLoggedIn(player.getName()))
 						{
 							if(ProtectionConfiguration.listMode != ListMode.DISABLED)
 							{
 								if(action == ProtectionAction.OPTIN)
 								{
 									if(ProtectionConfiguration.listedPlayers.contains(player.getName().toLowerCase()))
 									{
 										player.sendMessage("\u00A7e[LoginProtection] I'm sorry, but you are already in the list.");
 									}
 									else
 									{
 										ProtectionConfiguration.listedPlayers.add(player.getName().toLowerCase());
 										ProtectionConfiguration.saveToFile();
 										player.sendMessage("\u00A7e[LoginProtection] You are now in the list.");
 										player.sendMessage("\u00A7e[LoginProtection] From now on you will " + (ProtectionConfiguration.listMode == ListMode.WHITELIST?"not":"") + " asked to login.");
 									}
 								}
 								else
 								{
 									if(ProtectionConfiguration.listedPlayers.contains(player.getName().toLowerCase()))
 									{
 										ProtectionConfiguration.listedPlayers.add(player.getName().toLowerCase());
 										ProtectionConfiguration.saveToFile();
 										player.sendMessage("\u00A7e[LoginProtection] You have been removed from the list.");
 										player.sendMessage("\u00A7e[LoginProtection] From now on you will " + (ProtectionConfiguration.listMode == ListMode.BLACKLIST?"not":"") + " asked to login.");
 									}
 									else
 									{
 										player.sendMessage("\u00A7e[LoginProtection] I'm sorry, but I cannot find you on the list.");
 									}
 								}
 							}
 							else
 							{
 								player.sendMessage("\u00A7e[LoginProtection] I'm sorry, but the list is not enabled. \u00A77Ask an administrator for assitance.");
 							}
 						}
 						else
 						{
 							player.sendMessage("\u00A7e[LoginProtection] I'm sorry, but you cannot use that while you are logged out.");
 						}
 					}
 					else
 					{
 						player.sendMessage("\u00A7e[LoginProtection] I'm sorry, but you are not allowed to opt-in and/or opt-out of this. \u00A77Ask an administrator for assitance.");
 					}
 				}
 				else {
 					outputDisallowed(sender);
 				}
 			}
 			else if (action == ProtectionAction.ADMIN)
 			{
 				if (arguments.length < 2)
 				{
 					outputAdminUsage(sender);
 				}
 				else if ((!isPlayer) || (player.hasPermission("loginprotection.admin")))
 				{
 					if ((isPlayer) && (!ProtectionSystem.isPlayerLoggedIn(player.getName())))
 					{
 						ProtectionSystem.warn(player, false);
 						return true;
 					}
 					if (arguments[1].equalsIgnoreCase("reload"))
 					{
 						ProtectionSystem.readFromFile();
 						ProtectionConfiguration.readFromFile();
 						if (Protection.getInstance().isEnabled())
 						{
 							if (isPlayer)
 								sender.sendMessage("\u00A7e[LoginProtection] Successfully reloaded configuration and password entries.");
 							else {
 								sender.sendMessage("[LoginProtection] Successfully reloaded configuration and password entries.");
 							}
 
 						}
 						else if (isPlayer)
 							sender.sendMessage("\u00A7c[LoginProtection] Failed to reload configuration and password entries.");
 						else {
 							sender.sendMessage("[LoginProtection] Failed to reload configuration and password entries.");
 						}
 						for (Player onlinePlayer : Protection.getInstance().getServer().getOnlinePlayers())
 						{
 							ProtectionSystem.createStatusIfNone(onlinePlayer);
 							ProtectionSystem.setPlayerLoggedIn(onlinePlayer.getName(), ProtectionConfiguration.getDefaultLoginStatus(onlinePlayer));
 							if (!ProtectionConfiguration.getDefaultLoginStatus(onlinePlayer))
 								onlinePlayer.sendMessage("\u00A7e[LoginProtection] You may need to login again. I am sincerely sorry for the inconvenience.");
 						}
 					}
 					else if (arguments[1].equalsIgnoreCase("list"))
 					{
 						if (arguments.length > 5)
 						{
 							outputAdminListUsage(sender);
 						}
 						else if (arguments.length == 2)
 						{
 							if (isPlayer)
 								sender.sendMessage("\u00A7e[LoginProtection] List Mode: \u00A76" + ProtectionConfiguration.listMode.name());
 							else
 								sender.sendMessage("[LoginProtection] List Mode: " + ProtectionConfiguration.listMode.name());
 							String playerList = "";
 							if (isPlayer)
 								playerList = playerList + "\u00A7e[LoginProtection] List Members: ";
 							else
 								playerList = playerList + "[LoginProtection] List Members: ";
 							int i = 0;
 							for (String listedPlayer : ProtectionConfiguration.listedPlayers)
 							{
 								if (++i > 1)
 									playerList = playerList + ", ";
 								if (isPlayer)
 									playerList = playerList + "\u00A76" + listedPlayer + "\u00A7f";
 								else
 									playerList = playerList + listedPlayer;
 							}
 							sender.sendMessage(playerList);
 						}
 						else if (arguments[2].equalsIgnoreCase("add"))
 						{
 							String trimmedPlayer = arguments[3].trim();
 							String addingPlayer = trimmedPlayer.toLowerCase();
 							if (ProtectionConfiguration.listedPlayers.contains(addingPlayer))
 							{
 								if (isPlayer)
 									sender.sendMessage("\u00A7e[LoginProtection] \u00A76" + trimmedPlayer + "\u00A7e is already in the list.");
 								else
 									sender.sendMessage("[LoginProtection] " + trimmedPlayer + " is already in the list.");
 							}
 							else
 							{
 								ProtectionConfiguration.listedPlayers.add(addingPlayer);
 								ProtectionConfiguration.saveToFile();
 								if (isPlayer)
 									sender.sendMessage("\u00A7e[LoginProtection] Successfully added \u00A76" + trimmedPlayer + "\u00A7e to the list.");
 								else
 									sender.sendMessage("[LoginProtection] Successfully added " + trimmedPlayer + " to the list.");
 							}
 						}
 						else if (arguments[2].equalsIgnoreCase("remove"))
 						{
 							String trimmedPlayer = arguments[3].trim();
 							String removingPlayer = trimmedPlayer.toLowerCase();
 							if (!ProtectionConfiguration.listedPlayers.contains(removingPlayer))
 							{
 								if (isPlayer)
 									sender.sendMessage("\u00A7e[LoginProtection] \u00A76" + trimmedPlayer + "\u00A7e is not in the list.");
 								else
 									sender.sendMessage("[LoginProtection] " + trimmedPlayer + " is not in the list.");
 							}
 							else
 							{
 								ProtectionConfiguration.listedPlayers.remove(removingPlayer);
 								ProtectionConfiguration.saveToFile();
 								if (isPlayer)
 									sender.sendMessage("\u00A7e[LoginProtection] Successfully removed \u00A76" + trimmedPlayer + "\u00A7e from the list.");
 								else
 									sender.sendMessage("[LoginProtection] Successfully removed " + trimmedPlayer + " from the list.");
 							}
 						}
 						else if (arguments[2].equalsIgnoreCase("mode"))
 						{
 							String settingMode = arguments[3].trim();
 							if (settingMode.equalsIgnoreCase("NONE"))
 							{
 								ProtectionConfiguration.listMode = ProtectionConfiguration.ListMode.DISABLED;
 								ProtectionConfiguration.saveToFile();
 								if (isPlayer)
 									sender.sendMessage("\u00A7e[LoginProtection] Successfully changed list mode to \u00A76DISABLED\u00A7e.");
 								else
 									sender.sendMessage("[LoginProtection] Successfully changed list mode to DISABLED.");
 							}
 							else if (settingMode.equalsIgnoreCase("WHITELIST"))
 							{
 								ProtectionConfiguration.listMode = ProtectionConfiguration.ListMode.WHITELIST;
 								ProtectionConfiguration.saveToFile();
 								if (isPlayer)
 									sender.sendMessage("\u00A7e[LoginProtection] Successfully changed list mode to \u00A76WHITELIST\u00A7e.");
 								else
 									sender.sendMessage("[LoginProtection] Successfully changed list mode to WHITELIST.");
 							}
 							else if (settingMode.equalsIgnoreCase("BLACKLIST"))
 							{
 								ProtectionConfiguration.listMode = ProtectionConfiguration.ListMode.BLACKLIST;
 								ProtectionConfiguration.saveToFile();
 								if (isPlayer)
 									sender.sendMessage("\u00A7e[LoginProtection] Successfully changed list mode to \u00A76BLACKLIST\u00A7e.");
 								else {
 									sender.sendMessage("[LoginProtection] Successfully changed list mode to BLACKLIST.");
 								}
 
 							}
 							else if (isPlayer) {
 								sender.sendMessage("\u00A7e[LoginProtection] I'm sorry, but you can only use the keywords: Whitelist, Blacklist, and None");
 							} else {
 								sender.sendMessage("[LoginProtection] I'm sorry, but you can only use the keywords: Whitelist, Blacklist, and None");
 							}
 						}
 						else
 						{
 							outputAdminListUsage(sender);
 						}
 					}
 					else
 					{
 						outputAdminUsage(sender);
 					}
 				}
 				else
 				{
 					outputDisallowed(sender);
 				}
 			}
 		}
 		return true;
 	}
 
 	private void attemptLogin(Player player, String password)
 	{
 		String playerName = player.getName();
 		if (validPassword(playerName, password.trim()))
 		{
 			ProtectionSystem.setPlayerLoggedIn(playerName, true);
 			if (ProtectionSystem.getPlayerPasswordHash(playerName).equals(""))
 			{
 				ProtectionSystem.setPlayerPassword(playerName, password);
 				player.sendMessage("\u00A7e[LoginProtection] Thank you for logging in and setting your password.");
 				ProtectionSystem.saveToFile();
 			}
 			else {
 				player.sendMessage("\u00A7e[LoginProtection] Thank you for logging in.");
 			}ProtectionSystem.setPlayerAttempts(player, 0);
 		}
 		else
 		{
 			player.sendMessage("\u00A7e[LoginProtection] I'm sorry, but that doesn't seem to be the correct password.");
 			ProtectionSystem.incrementPlayerAttempts(player);
 		}
 	}
 
 	private void attemptSet(Player player, String oldPassword, String newPassword)
 	{
 		String playerName = player.getName();
 		if (validPassword(playerName, oldPassword.trim()))
 		{
 			ProtectionSystem.setPlayerPassword(playerName, newPassword);
 			if (ProtectionSystem.isPlayerLoggedIn(playerName))
 			{
 				ProtectionSystem.setPlayerLoggedIn(playerName, true);
 				player.sendMessage("\u00A7e[LoginProtection] Thank you for setting your password.");
 			}
 			else {
 				player.sendMessage("\u00A7e[LoginProtection] Thank you for setting your password and logging in.");
 			}ProtectionSystem.saveToFile();
 			ProtectionSystem.setPlayerAttempts(player, 0);
 		}
 		else
 		{
 			player.sendMessage("\u00A7e[LoginProtection] I'm sorry, but that doesn't seem to be the correct password.");
 			ProtectionSystem.incrementPlayerAttempts(player);
 		}
 	}
 
 	private boolean validPassword(String playerName, String password)
 	{
 		String currentPasswordHash = ProtectionSystem.getPlayerPasswordHash(playerName);
 		return (currentPasswordHash.equals("")) || (currentPasswordHash.equalsIgnoreCase(ProtectionSystem.getHash(password)));
 	}
 
 	private void outputUsage(CommandSender sender)
 	{
 		outputUsage(sender, true);
 	}
 
 	private void outputUsage(CommandSender sender, boolean showMessage)
 	{
 		if ((sender instanceof Player))
 		{
 			Player player = (Player)sender;
 			sender.sendMessage("\u00A7a/protection help\u00A7f - Show help");
 			if (player.hasPermission("protection.access"))
 			{
 				sender.sendMessage("\u00A7a/protection [Password]\u00A7f - Attempt to login");
 				sender.sendMessage("\u00A7a/protection [Old Password] [New Password]\u00A7f - Set your password");
 				sender.sendMessage("\u00A7a/protection logoff\u00A7f - Logoff this account");
 			}
 			if (player.hasPermission("protection.admin"))
 				sender.sendMessage("\u00A7cAdmin: \u00A7a/protection admin\u00A7f - Administration commands");
 			if (showMessage)
 				sender.sendMessage("\u00A77As always, passwords are encrypted and not even the server owner can decrypt them.");
 		}
 		else
 		{
 			sender.sendMessage("/protection help - Show help");
 			sender.sendMessage("Admin: /protection admin - Administration commands");
 		}
 	}
 
 	private void outputAdminUsage(CommandSender sender)
 	{
 		outputUsage(sender, false);
 		if ((sender instanceof Player))
 		{
 			if (((Player)sender).hasPermission("protection.admin"))
 			{
 				sender.sendMessage("\u00A7cAdmin: \u00A7a/protection admin reload\u00A7f - Reload data");
 				sender.sendMessage("\u00A7cAdmin: \u00A7a/protection admin list\u00A7f - Modify the player list");
 			}
 		}
 		else
 		{
 			sender.sendMessage("Admin: /protection admin reload - Reload data");
 			sender.sendMessage("Admin: /protection admin list - Modify the player list");
 		}
 	}
 
 	private void outputAdminListUsage(CommandSender sender)
 	{
 		outputAdminUsage(sender);
 		if ((sender instanceof Player))
 		{
 			if (((Player)sender).hasPermission("protection.admin"))
 			{
 				sender.sendMessage("\u00A7cAdmin: \u00A7a/protection admin list add [Player]\u00A7f - Add a player to the list");
 				sender.sendMessage("\u00A7cAdmin: \u00A7a/protection admin list remove [Player]\u00A7f - Remove a player from the list");
 				sender.sendMessage("\u00A7cAdmin: \u00A7a/protection admin list mode [Whitelist|Blacklist|None]\u00A7f - Set the list's mode");
 			}
 		}
 		else
 		{
 			sender.sendMessage("Admin: /protection admin list add [Player] - Add a player to the list");
 			sender.sendMessage("Admin: /protection admin list remove [Player] - Remove a player from the list");
 			sender.sendMessage("Admin: /protection admin list mode [Whitelist|Blacklist|None] - Set the list's mode");
 		}
 	}
 
 	private void outputDisallowed(CommandSender sender)
 	{
 		sender.sendMessage("\u00A7cI'm sorry, but I can't let you use this.");
 	}
 
 	private ProtectionAction getAction(String argument)
 	{
 		if (argument.equalsIgnoreCase("help"))
 			return ProtectionAction.HELP;
 		if (argument.equalsIgnoreCase("admin"))
 			return ProtectionAction.ADMIN;
 		if (argument.equalsIgnoreCase("optin"))
 			return ProtectionAction.OPTIN;
 		if (argument.equalsIgnoreCase("optout"))
 			return ProtectionAction.OPTOUT;
 		if ((argument.equalsIgnoreCase("logoff")) || (argument.equalsIgnoreCase("logout")))
 			return ProtectionAction.LOGOFF;
 		return ProtectionAction.FUNCTION;
 	}
 
 	private static enum ProtectionAction
 	{
 		ADMIN, 
 		FUNCTION, 
 		HELP,
 		OPTIN,
 		OPTOUT,
 		LOGOFF;
 	}
 }
