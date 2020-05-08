 package com.censoredsoftware.demigods.command;
 
 import java.util.ArrayList;
 import java.util.Set;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.command.ConsoleCommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.PluginManager;
 
 import com.censoredsoftware.demigods.Demigods;
 import com.censoredsoftware.demigods.ability.Ability;
 import com.censoredsoftware.demigods.data.DataManager;
 import com.censoredsoftware.demigods.deity.Deity;
 import com.censoredsoftware.demigods.helper.WrappedCommand;
 import com.censoredsoftware.demigods.language.Symbol;
 import com.censoredsoftware.demigods.language.Translation;
 import com.censoredsoftware.demigods.player.DCharacter;
 import com.censoredsoftware.demigods.player.DPlayer;
 import com.censoredsoftware.demigods.util.*;
 import com.google.common.collect.Sets;
 
 public class MainCommand extends WrappedCommand
 {
 	public MainCommand()
 	{
 		super(false);
 	}
 
 	@Override
 	public Set<String> getCommands()
 	{
 		return Sets.newHashSet("demigods", "deity");
 	}
 
 	@Override
 	public boolean processCommand(CommandSender sender, Command command, String[] args)
 	{
 		// Commands able to be run by the console
 		if(command.getName().equals("demigods"))
 		{
 			if(args.length == 2 && args[0].equalsIgnoreCase("admin") && args[1].equalsIgnoreCase("reload"))
 			{
 				PluginManager pluginManager = Demigods.PLUGIN.getServer().getPluginManager();
 				pluginManager.disablePlugin(Demigods.PLUGIN);
 				pluginManager.enablePlugin(Demigods.PLUGIN);
 				sender.sendMessage(ChatColor.GREEN + Demigods.LANGUAGE.getText(Translation.Text.RELOAD_COMPLETE));
 				return true;
 			}
 		}
 
 		// No console below this point
 		if(sender instanceof ConsoleCommandSender) return Messages.noConsole((ConsoleCommandSender) sender);
 
 		// Define Player
 		Player player = (Player) sender;
 
 		// Check args and pass onto appropriate method
 		if(args.length > 0 && args[0].equalsIgnoreCase("admin"))
 		{
 			dg_admin(player, args);
 			return true;
 		}
 		else if(args.length > 0)
 		{
 			dg_extended(player, args);
 			return true;
 		}
 
 		// Check Permissions
 		if(!player.hasPermission("demigods.basic")) return Messages.noPermission(player);
 
 		if(command.getName().equals("deity") && DPlayer.Util.getPlayer(player).getCurrent() != null && DPlayer.Util.getPlayer(player).getCurrent().isUsable())
 		{
 			Deity deity = DPlayer.Util.getPlayer(player).getCurrent().getDeity();
 			player.chat("/dg " + deity.getAlliance().toLowerCase() + " " + deity.getName().toLowerCase());
 			return true;
 		}
 		else if(command.getName().equals("deity"))
 		{
 			player.sendMessage(ChatColor.RED + "This command requires you to have a character.");
 			return true;
 		}
 
 		Messages.tagged(sender, "Documentation");
 		for(String alliance : Deity.Util.getLoadedPlayableDeityAlliances())
 		{
 			if(!sender.hasPermission("demigods." + alliance.toLowerCase())) continue;
 			sender.sendMessage(ChatColor.GRAY + " /dg " + alliance.toLowerCase());
 		}
 		sender.sendMessage(ChatColor.GRAY + " /dg info");
 		sender.sendMessage(ChatColor.GRAY + " /dg commands");
 		if(player.hasPermission("demigods.admin")) sender.sendMessage(ChatColor.RED + " /dg admin");
 		sender.sendMessage(" ");
 		sender.sendMessage(ChatColor.WHITE + " Use " + ChatColor.YELLOW + "/check" + ChatColor.WHITE + " to see your player information.");
 		return true;
 	}
 
 	private static boolean dg_extended(Player player, String[] args)
 	{
 		// Define args
 		String category = args[0];
 		String option1 = null;
 		if(args.length >= 2) option1 = args[1];
 
 		// Check Permissions
 		if(!player.hasPermission("demigods.basic")) return Messages.noPermission(player);
 
 		if(category.equalsIgnoreCase("commands"))
 		{
 			Messages.tagged(player, "Command Directory");
 			player.sendMessage(ChatColor.GRAY + " There's nothing here..."); // TODO
 		}
 		else if(category.equalsIgnoreCase("info"))
 		{
 			if(option1 == null)
 			{
 				Messages.tagged(player, "Information Directory");
 				player.sendMessage(ChatColor.GRAY + " /dg info characters");
 				player.sendMessage(ChatColor.GRAY + " /dg info shrines");
 				player.sendMessage(ChatColor.GRAY + " /dg info tributes");
 				player.sendMessage(ChatColor.GRAY + " /dg info players");
 				player.sendMessage(ChatColor.GRAY + " /dg info pvp");
 				player.sendMessage(ChatColor.GRAY + " /dg info stats");
 				player.sendMessage(ChatColor.GRAY + " /dg info rankings");
 				player.sendMessage(ChatColor.GRAY + " /dg info demigods");
 			}
 			else if(option1.equalsIgnoreCase("demigods"))
 			{
 				Messages.tagged(player, "About the Plugin");
 				player.sendMessage(ChatColor.WHITE + " Not to be confused with other RPG plugins that focus on skills and classes alone, " + ChatColor.GREEN + "Demigods" + ChatColor.WHITE + " adds culture and conflict that will keep players coming back even after they've maxed out their levels and found all of the diamonds in a 50km radius.");
 				player.sendMessage(" ");
 				player.sendMessage(ChatColor.GREEN + " Demigods" + ChatColor.WHITE + " is unique in its system of rewarding players for both adventuring (tributes) and conquering (PvP) with a wide array of fun and useful skills.");
 				player.sendMessage(" ");
 				player.sendMessage(ChatColor.WHITE + " Re-enact mythological battles and rise from a mere player to a full-fledged Olympian as you form new Alliances with mythical groups and battle to the bitter end.");
 				player.sendMessage(" ");
 				player.sendMessage(ChatColor.GRAY + " Developed by: " + ChatColor.GREEN + "_Alex" + ChatColor.GRAY + " and " + ChatColor.GREEN + "HmmmQuestionMark");
 				player.sendMessage(ChatColor.GRAY + " Website: " + ChatColor.YELLOW + "demigodsrpg.com");
 				player.sendMessage(ChatColor.GRAY + " Source: " + ChatColor.YELLOW + "github.com/CensoredSoftware/Minecraft-Demigods");
 			}
 			else if(option1.equalsIgnoreCase("characters"))
 			{
 				Messages.tagged(player, "Characters");
 				player.sendMessage(ChatColor.GRAY + " This is some info about Characters.");
 			}
 			else if(option1.equalsIgnoreCase("shrine"))
 			{
 				Messages.tagged(player, "Shrines");
 				player.sendMessage(ChatColor.GRAY + " This is some info about Shrines.");
 			}
 			else if(option1.equalsIgnoreCase("tribute"))
 			{
 				Messages.tagged(player, "Tributes");
 				player.sendMessage(ChatColor.GRAY + " This is some info about Tributes.");
 			}
 			else if(option1.equalsIgnoreCase("player"))
 			{
 				Messages.tagged(player, "Players");
 				player.sendMessage(ChatColor.GRAY + " This is some info about Players.");
 			}
 			else if(option1.equalsIgnoreCase("pvp"))
 			{
 				Messages.tagged(player, "PVP");
 				player.sendMessage(ChatColor.GRAY + " This is some info about PVP.");
 			}
 			else if(option1.equalsIgnoreCase("stats"))
 			{
 				Messages.tagged(player, "Stats");
 				player.sendMessage(ChatColor.GRAY + " Read some server-wide stats for Demigods.");
 			}
 			else if(option1.equalsIgnoreCase("rankings"))
 			{
 				Messages.tagged(player, "Rankings");
 				player.sendMessage(ChatColor.GRAY + " This is some ranking info about Demigods.");
 			}
 		}
 
 		for(String alliance : Deity.Util.getLoadedMajorPlayableDeityAlliancesWithPerms(player))
 		{
 			if(!player.hasPermission("demigods." + alliance.toLowerCase())) continue;
 			if(category.equalsIgnoreCase(alliance))
 			{
 				if(args.length < 2)
 				{
 					Messages.tagged(player, alliance + " Directory");
 					for(Deity deity : Deity.Util.getLoadedPlayableDeitiesInAlliance(alliance))
 						player.sendMessage(ChatColor.GRAY + " /dg " + alliance.toLowerCase() + " " + deity.getColor() + deity.getName().toLowerCase());
 				}
 				else
 				{
 					for(final Deity deity : Deity.Util.getLoadedMajorPlayableDeitiesInAllianceWithPerms(alliance, player))
 					{
 						assert option1 != null;
 						if(option1.equalsIgnoreCase(deity.getName()))
 						{
 							try
 							{
 								for(String toPrint : new ArrayList<String>()
 								{
 									{
 										addAll(deity.getLore());
 										for(Ability ability : deity.getAbilities())
 										{
 											for(String detail : ability.getDetails())
 											{
 												StringBuilder details = new StringBuilder(ChatColor.GRAY + "   " + Symbol.RIGHTWARD_ARROW + " ");
 												if(ability.getCommand() != null) details.append(ChatColor.GREEN + "/").append(ability.getCommand().toLowerCase()).append(ChatColor.WHITE).append(": ");
 												details.append(ChatColor.WHITE).append(detail);
 												add(details.toString());
 												if(ability.getCost() > 0) add("      " + ChatColor.GRAY + "-" + ChatColor.WHITE + " Favor cost: " + ChatColor.RED + ability.getCost());
 											}
 										}
 										add(" ");
 									}
 								})
 									player.sendMessage(toPrint);
 								return true;
 							}
 							catch(Exception e)
 							{
 								e.printStackTrace();
 								return true;
 							}
 						}
 					}
 					player.sendMessage(ChatColor.DARK_RED + " No such deity, please try again.");
 					return false;
 				}
 			}
 		}
 		return true;
 	}
 
 	private static boolean dg_admin(Player sender, String[] args)
 	{
 		if(!sender.hasPermission("demigods.admin")) return Messages.noPermission(sender);
 
 		// Display main admin options menu
 		if(args.length < 2)
 		{
 			Messages.tagged(sender, "Admin Directory");
 			sender.sendMessage(ChatColor.GRAY + " /dg admin wand");
 			sender.sendMessage(ChatColor.GRAY + " /dg admin debug");
 			for(AdminCommands command : AdminCommands.values())
 			{
 				sender.sendMessage(ChatColor.GRAY + " " + command.getCommand().getName());
 			}
 			sender.sendMessage(ChatColor.DARK_RED + " /dg admin clear data yesdoitforsurepermanently");
 			return true;
 		}
 
 		// Handle automatic commands
 		for(AdminCommands command : AdminCommands.values())
 		{
 			if(args[1].equalsIgnoreCase(command.getCommand().getRootCommand())) return command.getCommand().doCommand(sender, args);
 		}
 
 		// Handle manual commands
 		if(args[1].equalsIgnoreCase("wand"))
 		{
 			if(!Admins.wandEnabled(sender))
 			{
 				DataManager.saveTemp(sender.getName(), "temp_admin_wand", true);
 				sender.sendMessage(ChatColor.RED + "Your admin wand has been enabled for " + Material.getMaterial(Configs.getSettingInt("admin.wand_tool")));
 			}
 			else if(Admins.wandEnabled(sender))
 			{
 				DataManager.removeTemp(sender.getName(), "temp_admin_wand");
 				sender.sendMessage(ChatColor.RED + "You have disabled your admin wand.");
 			}
 			return true;
 		}
 		else if(args[1].equalsIgnoreCase("debug"))
 		{
 			if(!DataManager.hasKeyTemp(sender.getName(), "temp_admin_debug") || !Boolean.parseBoolean(DataManager.getValueTemp(sender.getName(), "temp_admin_debug").toString()))
 			{
 				DataManager.saveTemp(sender.getName(), "temp_admin_debug", true);
 				sender.sendMessage(ChatColor.RED + "You have enabled debugging.");
 			}
 			else if(DataManager.hasKeyTemp(sender.getName(), "temp_admin_debug") && Boolean.parseBoolean(DataManager.getValueTemp(sender.getName(), "temp_admin_debug").toString()))
 			{
 				DataManager.removeTemp(sender.getName(), "temp_admin_debug");
 				sender.sendMessage(ChatColor.RED + "You have disabled debugging.");
 			}
 		}
 		else if(args[1].equalsIgnoreCase("clear") && args[1].equalsIgnoreCase("data") && args[2].equalsIgnoreCase("yesdoitforsurepermanently"))
 		{
 			sender.sendMessage(ChatColor.RED + Demigods.LANGUAGE.getText(Translation.Text.ADMIN_CLEAR_DATA_STARTING));
 			DataManager.flushData();
 			sender.sendMessage(ChatColor.GREEN + Demigods.LANGUAGE.getText(Translation.Text.ADMIN_CLEAR_DATA_FINISHED));
 			return true;
 		}
 
 		return true;
 	}
 
 	static class doCheck implements AdminCommand
 	{
 		@Override
 		public String getName()
 		{
 			return "/dg admin check [player|character] <name>";
 		}
 
 		@Override
 		public String getRootCommand()
 		{
 			return "check";
 		}
 
 		@Override
 		public boolean doCommand(Player sender, String[] args)
 		{
 			if(args.length < 4)
 			{
 				// Not enough parameters, return
 				sender.sendMessage(ChatColor.RED + "You didn't specify enough parameters.");
 				return true;
 			}
 
 			if(args[2].equalsIgnoreCase("player"))
 			{
 				// Define the player
 				DPlayer player = DPlayer.Util.getPlayer(Bukkit.getOfflinePlayer(args[3]));
 
 				// Display the information
 				if(player != null)
 				{
 					Messages.tagged(sender, "Player Information");
 					sender.sendMessage(ChatColor.GRAY + " " + Symbol.RIGHTWARD_ARROW + " " + ChatColor.RESET + "Name: " + player.getPlayerName());
 					sender.sendMessage(ChatColor.GRAY + " " + Symbol.RIGHTWARD_ARROW + " " + ChatColor.RESET + "Last Login: " + Times.getTimeTagged(player.getLastLoginTime(), true) + " ago");
 					sender.sendMessage(ChatColor.GRAY + " " + Symbol.RIGHTWARD_ARROW + " " + ChatColor.RESET + "Last Logout: " + Times.getTimeTagged(player.getLastLogoutTime(), true) + " ago");
 					sender.sendMessage(ChatColor.GRAY + " " + Symbol.RIGHTWARD_ARROW + " " + ChatColor.RESET + "Can PvP? " + (player.canPvp() ? ChatColor.GREEN : ChatColor.RED) + Strings.beautify("" + player.canPvp()));
 					if(player.hasCurrent()) sender.sendMessage(ChatColor.GRAY + " " + Symbol.RIGHTWARD_ARROW + " " + ChatColor.RESET + "Current Character: " + player.getCurrent().getDeity().getColor() + player.getCurrent().getName());
 					if(player.getCharacters() != null && !player.getCharacters().isEmpty())
 					{
 						sender.sendMessage(ChatColor.GRAY + " " + Symbol.RIGHTWARD_ARROW + " " + ChatColor.RESET + "Characters:");
 						for(DCharacter character : player.getCharacters())
							sender.sendMessage(ChatColor.GRAY + "   - " + character.getDeity().getColor() + character.getName() + ChatColor.RESET + " (" + character.getDeity().getColor() + character.getDeity().getName() + ChatColor.RESET + ")");
 					}
 				}
 				else
 				{
 					sender.sendMessage(ChatColor.RED + "No player found with that name.");
 				}
 			}
 			else if(args[2].equalsIgnoreCase("character"))
 			{
 				// Define the character
 				DCharacter character = DCharacter.Util.getCharacterByName(args[3]);
 
 				// Display the information
 				if(character != null)
 				{
 					Messages.tagged(sender, "Character Information");
 					sender.sendMessage(ChatColor.GRAY + " " + Symbol.RIGHTWARD_ARROW + " " + ChatColor.RESET + "Name: " + character.getDeity().getColor() + character.getName());
 					sender.sendMessage(ChatColor.GRAY + " " + Symbol.RIGHTWARD_ARROW + " " + ChatColor.RESET + "Deity: " + character.getDeity().getColor() + character.getDeity().getName());
 					sender.sendMessage(ChatColor.GRAY + " " + Symbol.RIGHTWARD_ARROW + " " + ChatColor.RESET + "Alliance: " + ChatColor.WHITE + character.getAlliance());
 					sender.sendMessage(ChatColor.GRAY + " " + Symbol.RIGHTWARD_ARROW + " " + ChatColor.RESET + "Favor: " + Strings.getColor(character.getMeta().getFavor(), character.getMeta().getMaxFavor()) + character.getMeta().getFavor() + ChatColor.WHITE + " (of " + ChatColor.GREEN + character.getMeta().getMaxFavor() + ChatColor.WHITE + ")");
 					sender.sendMessage(ChatColor.GRAY + " " + Symbol.RIGHTWARD_ARROW + " " + ChatColor.RESET + "Ascensions: " + ChatColor.GREEN + character.getMeta().getAscensions());
 					sender.sendMessage(ChatColor.GRAY + " " + Symbol.RIGHTWARD_ARROW + " " + ChatColor.RESET + "Available Skill Points: " + ChatColor.GREEN + character.getMeta().getSkillPoints());
 					sender.sendMessage(ChatColor.GRAY + " " + Symbol.RIGHTWARD_ARROW + " " + ChatColor.RESET + "Kills: " + ChatColor.GREEN + character.getKillCount() + ChatColor.WHITE + " / Deaths: " + ChatColor.RED + character.getDeathCount());
 					sender.sendMessage(ChatColor.GRAY + " " + Symbol.RIGHTWARD_ARROW + " " + ChatColor.RESET + "Owner: " + ChatColor.WHITE + character.getPlayer() + " (" + (character.getOfflinePlayer().isOnline() ? ChatColor.GREEN + "online" : ChatColor.RED + "offline") + ChatColor.WHITE + ")");
 					sender.sendMessage(ChatColor.GRAY + " " + Symbol.RIGHTWARD_ARROW + " " + ChatColor.RESET + "Active? " + (character.isActive() ? ChatColor.GREEN : ChatColor.RED) + Strings.beautify("" + character.isActive()));
 					sender.sendMessage(ChatColor.GRAY + " " + Symbol.RIGHTWARD_ARROW + " " + ChatColor.RESET + "Usable? " + (character.isUsable() ? ChatColor.GREEN : ChatColor.RED) + Strings.beautify("" + character.isUsable()));
 					sender.sendMessage(" ");
 				}
 				else
 				{
 					sender.sendMessage(ChatColor.RED + "No character found with that name.");
 				}
 			}
 
 			return true;
 		}
 	}
 
 	static class doRemove implements AdminCommand
 	{
 		@Override
 		public String getName()
 		{
 			return "/dg admin remove [player|character] <name>";
 		}
 
 		@Override
 		public String getRootCommand()
 		{
 			return "remove";
 		}
 
 		@Override
 		public boolean doCommand(Player sender, String[] args)
 		{
 			if(args.length < 4)
 			{
 				// Not enough parameters, return
 				sender.sendMessage(ChatColor.RED + "You didn't specify enough parameters.");
 				return true;
 			}
 
 			if(args[2].equalsIgnoreCase("player"))
 			{
 				// Define the player
 				DPlayer player = DPlayer.Util.getPlayer(Bukkit.getOfflinePlayer(args[3]));
 
 				// Remove their data if not null
 				if(player != null)
 				{
 					// TODO: Implement a method to remove DPlayers and call it here.
 				}
 				else
 				{
 					sender.sendMessage(ChatColor.RED + "No player found with that name.");
 				}
 			}
 			else if(args[2].equalsIgnoreCase("character"))
 			{
 				// Define the character
 				DCharacter character = DCharacter.Util.getCharacterByName(args[3]);
 
 				// Remove their data if not null
 				if(character != null)
 				{
 					// Kick the player first if they're online
 					if(character.getOfflinePlayer().isOnline() && DPlayer.Util.getPlayer(character.getOfflinePlayer()).getCurrent().getName().equalsIgnoreCase(character.getName()))
 					{
 						character.getOfflinePlayer().getPlayer().kickPlayer(ChatColor.RED + "Your active character has been deleted.");
 					}
 
 					// Remove them
 					character.remove();
 
 					// Send success message
 					sender.sendMessage(ChatColor.RED + character.getName() + " has been removed successfully!");
 				}
 				else
 				{
 					sender.sendMessage(ChatColor.RED + "No player found with that name.");
 				}
 			}
 
 			return true;
 		}
 	}
 
 	static class doSet implements AdminCommand
 	{
 		@Override
 		public String getName()
 		{
 			return "/dg admin set [fav|maxfav|sp] <character> <amt>";
 		}
 
 		@Override
 		public String getRootCommand()
 		{
 			return "set";
 		}
 
 		@Override
 		public boolean doCommand(Player sender, String[] args)
 		{
 			if(args.length < 5)
 			{
 				// Not enough parameters, return
 				sender.sendMessage(ChatColor.RED + "You didn't specify enough parameters.");
 				return true;
 			}
 
 			DCharacter character = DCharacter.Util.getCharacterByName(args[3]);
 			int amount = Integer.parseInt(args[4]);
 			String updatedValue = null;
 
 			if(character != null)
 			{
 				Player owner = character.getOfflinePlayer().getPlayer();
 
 				if(args[2].equalsIgnoreCase("fav"))
 				{
 					// Update the amount
 					character.getMeta().setFavor(amount);
 
 					// Set what was updated
 					updatedValue = "favor";
 				}
 				else if(args[2].equalsIgnoreCase("maxfav"))
 				{
 					// Update the amount
 					character.getMeta().setMaxFavor(amount);
 
 					// Set what was updated
 					updatedValue = "max favor";
 				}
 				else if(args[2].equalsIgnoreCase("sp"))
 				{
 					// Update the amount
 					character.getMeta().setSkillPoints(amount);
 
 					// Set what was updated
 					updatedValue = "skill points";
 				}
 				else
 				{
 					// Nothing was edited
 					owner.sendMessage(ChatColor.RED + "Invalid value to update specified.");
 				}
 
 				// Message the administrator to confirm
 				sender.sendMessage(ChatColor.GREEN + Strings.beautify(updatedValue) + " updated for " + character.getName() + ".");
 
 				// Message the edited player
 				if(character.getOfflinePlayer().isOnline())
 				{
 					owner.sendMessage(ChatColor.GREEN + "Your character's (" + character.getName() + ") " + updatedValue + " has been set to " + amount + ".");
 					owner.sendMessage(ChatColor.GRAY + "" + ChatColor.ITALIC + "This was performed by " + sender.getName() + ".");
 				}
 			}
 			else
 			{
 				sender.sendMessage(ChatColor.RED + "No character could be found.");
 			}
 
 			return true;
 		}
 	}
 
 	static class doAdd implements AdminCommand
 	{
 		@Override
 		public String getName()
 		{
 			return "/dg admin add [fav|maxfav|sp] <character> <amt>";
 		}
 
 		@Override
 		public String getRootCommand()
 		{
 			return "add";
 		}
 
 		@Override
 		public boolean doCommand(Player sender, String[] args)
 		{
 			if(args.length < 5)
 			{
 				// Not enough parameters, return
 				sender.sendMessage(ChatColor.RED + "You didn't specify enough parameters.");
 				return true;
 			}
 
 			DCharacter character = DCharacter.Util.getCharacterByName(args[3]);
 			int amount = Integer.parseInt(args[4]);
 			String updatedValue = null;
 
 			if(character != null)
 			{
 				Player owner = character.getOfflinePlayer().getPlayer();
 
 				if(args[2].equalsIgnoreCase("fav"))
 				{
 					// Update the amount
 					character.getMeta().addFavor(amount);
 
 					// Set what was updated
 					updatedValue = "favor";
 				}
 				else if(args[2].equalsIgnoreCase("maxfav"))
 				{
 					// Update the amount
 					character.getMeta().addMaxFavor(amount);
 
 					// Set what was updated
 					updatedValue = "max favor";
 				}
 				else if(args[2].equalsIgnoreCase("sp"))
 				{
 					// Update the amount
 					character.getMeta().addSkillPoints(amount);
 
 					// Set what was updated
 					updatedValue = "skill points";
 				}
 				else
 				{
 					// Nothing was edited
 					owner.sendMessage(ChatColor.RED + "Invalid value to update specified.");
 				}
 
 				// Message the administrator to confirm
 				sender.sendMessage(ChatColor.GREEN + Strings.beautify(updatedValue) + " updated for " + character.getName() + ".");
 
 				// Message them
 				if(character.getOfflinePlayer().isOnline())
 				{
 					owner.sendMessage(ChatColor.GREEN + "Your character's (" + character.getName() + ") " + updatedValue + " has been increased to " + amount + ".");
 					owner.sendMessage(ChatColor.GRAY + "" + ChatColor.ITALIC + "This was performed by " + sender.getName() + ".");
 				}
 			}
 			else
 			{
 				sender.sendMessage(ChatColor.RED + "No character could be found.");
 			}
 
 			return true;
 		}
 	}
 
 	static class doSub implements AdminCommand
 	{
 		@Override
 		public String getName()
 		{
 			return "/dg admin sub [fav|maxfav|sp] <character> <amt>";
 		}
 
 		@Override
 		public String getRootCommand()
 		{
 			return "sub";
 		}
 
 		@Override
 		public boolean doCommand(Player sender, String[] args)
 		{
 			if(args.length < 5)
 			{
 				// Not enough parameters, return
 				sender.sendMessage(ChatColor.RED + "You didn't specify enough parameters.");
 				return true;
 			}
 
 			DCharacter character = DCharacter.Util.getCharacterByName(args[3]);
 			int amount = Integer.parseInt(args[4]);
 			String updatedValue = null;
 
 			if(character != null)
 			{
 				Player owner = character.getOfflinePlayer().getPlayer();
 
 				if(args[2].equalsIgnoreCase("fav"))
 				{
 					// Update the amount
 					character.getMeta().subtractFavor(amount);
 
 					// Set what was updated
 					updatedValue = "favor";
 				}
 				else if(args[2].equalsIgnoreCase("maxfav"))
 				{
 					// Update the amount
 					character.getMeta().subtractMaxFavor(amount);
 
 					// Set what was updated
 					updatedValue = "max favor";
 				}
 				else if(args[2].equalsIgnoreCase("sp"))
 				{
 					// Update the amount
 					character.getMeta().subtractSkillPoints(amount);
 
 					// Set what was updated
 					updatedValue = "skill points";
 				}
 				else
 				{
 					// Nothing was edited
 					owner.sendMessage(ChatColor.RED + "Invalid value to update specified.");
 				}
 
 				// Message the administrator to confirm
 				sender.sendMessage(ChatColor.GREEN + Strings.beautify(updatedValue) + " updated for " + character.getName() + ".");
 
 				// Message them
 				if(character.getOfflinePlayer().isOnline())
 				{
 					owner.sendMessage(ChatColor.GREEN + "Your character's (" + character.getName() + ") " + updatedValue + " has been decreased to " + amount + ".");
 					owner.sendMessage(ChatColor.GRAY + "" + ChatColor.ITALIC + "This was performed by " + sender.getName() + ".");
 				}
 			}
 			else
 			{
 				sender.sendMessage(ChatColor.RED + "No character could be found.");
 			}
 
 			return true;
 		}
 	}
 
 	public enum AdminCommands
 	{
 		CHECK(new doCheck()), REMOVE(new doRemove()), SET(new doSet()), ADD(new doAdd()), SUBTRACT(new doSub());
 
 		private AdminCommand command;
 
 		private AdminCommands(AdminCommand command)
 		{
 			this.command = command;
 		}
 
 		public AdminCommand getCommand()
 		{
 			return this.command;
 		}
 	}
 
 	interface AdminCommand
 	{
 		public String getName();
 
 		public String getRootCommand();
 
 		public boolean doCommand(Player sender, String[] args);
 	}
 }
