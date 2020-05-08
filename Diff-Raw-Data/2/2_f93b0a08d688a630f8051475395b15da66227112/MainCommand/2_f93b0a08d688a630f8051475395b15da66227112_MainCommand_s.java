 package com.censoredsoftware.demigods.command;
 
 import com.censoredsoftware.demigods.Demigods;
 import com.censoredsoftware.demigods.ability.Ability;
 import com.censoredsoftware.demigods.data.DataManager;
 import com.censoredsoftware.demigods.deity.Deity;
 import com.censoredsoftware.demigods.helper.ListedCommand;
 import com.censoredsoftware.demigods.language.Translation;
 import com.censoredsoftware.demigods.player.DCharacter;
 import com.censoredsoftware.demigods.player.DPlayer;
 import com.censoredsoftware.demigods.util.Admins;
 import com.censoredsoftware.demigods.util.Unicodes;
 import com.google.common.collect.Sets;
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.command.ConsoleCommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.PluginManager;
 
 import java.util.ArrayList;
 import java.util.Set;
 
 public class MainCommand extends ListedCommand
 {
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
 				PluginManager pluginManager = Demigods.plugin.getServer().getPluginManager();
 				pluginManager.disablePlugin(Demigods.plugin);
 				pluginManager.enablePlugin(Demigods.plugin);
 				sender.sendMessage(ChatColor.GREEN + Demigods.language.getText(Translation.Text.RELOAD_COMPLETE));
 				return true;
 			}
 		}
 
 		// No console below this point
 		if(sender instanceof ConsoleCommandSender) return Demigods.message.noConsole((ConsoleCommandSender) sender);
 
 		// Define Player
 		Player player = (Player) sender;
 
 		// Check args and pass onto dg_extended() if need be
 		if(args.length > 0)
 		{
 			dg_extended(player, args);
 			return true;
 		}
 
 		// Check Permissions
 		if(!player.hasPermission("demigods.basic")) return Demigods.message.noPermission(player);
 
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
 
 		Demigods.message.tagged(sender, "Documentation");
 		for(String alliance : Deity.Util.getLoadedDeityAlliances())
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
 		String option1 = null, option2 = null, option3 = null, option4 = null;
 		if(args.length >= 2) option1 = args[1];
 		if(args.length >= 3) option2 = args[2];
 		if(args.length >= 4) option3 = args[3];
 		if(args.length >= 5) option4 = args[4];
 
 		// Check Permissions
 		if(!player.hasPermission("demigods.basic")) return Demigods.message.noPermission(player);
 
 		if(category.equalsIgnoreCase("admin"))
 		{
 			dg_admin(player, option1, option2, option3, option4);
 		}
 		else if(category.equalsIgnoreCase("commands"))
 		{
 			Demigods.message.tagged(player, "Command Directory");
 			player.sendMessage(ChatColor.GRAY + " There's nothing here..."); // TODO
 		}
 		else if(category.equalsIgnoreCase("info"))
 		{
 			if(option1 == null)
 			{
 				Demigods.message.tagged(player, "Information Directory");
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
 				Demigods.message.tagged(player, "About the Plugin");
 				player.sendMessage(ChatColor.WHITE + " Not to be confused with other RPG plugins that focus on skills and classes alone, " + ChatColor.GREEN + "Demigods" + ChatColor.WHITE + " adds culture and conflict that will keep players coming back even after they've maxed out their levels and found all of the diamonds in a 50km radius.");
 				player.sendMessage(" ");
 				player.sendMessage(ChatColor.GREEN + " Demigods" + ChatColor.WHITE + " is unique in its system of rewarding players for both adventuring (tributes) and conquering (PvP) with a wide array of fun and useful skills.");
 				player.sendMessage(" ");
 				player.sendMessage(ChatColor.WHITE + " Re-enact mythological battles and rise from a mere player to a full-fledged Olympian as you form new Alliances with mythical groups and battle to the bitter end.");
 				player.sendMessage(" ");
 				player.sendMessage(ChatColor.GRAY + " Developed by: " + ChatColor.GREEN + "_Alex" + ChatColor.GRAY + " and " + ChatColor.GREEN + "HmmmQuestionMark");
				player.sendMessage(ChatColor.GRAY + " Website: " + ChatColor.YELLOW + "demigodsrpg.com/");
 				player.sendMessage(ChatColor.GRAY + " Source: " + ChatColor.YELLOW + "github.com/CensoredSoftware/Minecraft-Demigods");
 			}
 			else if(option1.equalsIgnoreCase("characters"))
 			{
 				Demigods.message.tagged(player, "Characters");
 				player.sendMessage(ChatColor.GRAY + " This is some info about Characters.");
 			}
 			else if(option1.equalsIgnoreCase("shrine"))
 			{
 				Demigods.message.tagged(player, "Shrines");
 				player.sendMessage(ChatColor.GRAY + " This is some info about Shrines.");
 			}
 			else if(option1.equalsIgnoreCase("tribute"))
 			{
 				Demigods.message.tagged(player, "Tributes");
 				player.sendMessage(ChatColor.GRAY + " This is some info about Tributes.");
 			}
 			else if(option1.equalsIgnoreCase("player"))
 			{
 				Demigods.message.tagged(player, "Players");
 				player.sendMessage(ChatColor.GRAY + " This is some info about Players.");
 			}
 			else if(option1.equalsIgnoreCase("pvp"))
 			{
 				Demigods.message.tagged(player, "PVP");
 				player.sendMessage(ChatColor.GRAY + " This is some info about PVP.");
 			}
 			else if(option1.equalsIgnoreCase("stats"))
 			{
 				Demigods.message.tagged(player, "Stats");
 				player.sendMessage(ChatColor.GRAY + " Read some server-wide stats for Demigods.");
 			}
 			else if(option1.equalsIgnoreCase("rankings"))
 			{
 				Demigods.message.tagged(player, "Rankings");
 				player.sendMessage(ChatColor.GRAY + " This is some ranking info about Demigods.");
 			}
 		}
 
 		for(String alliance : Deity.Util.getLoadedDeityAlliances())
 		{
 			if(!player.hasPermission("demigods." + alliance.toLowerCase())) continue;
 			if(category.equalsIgnoreCase(alliance))
 			{
 				if(args.length < 2)
 				{
 					Demigods.message.tagged(player, alliance + " Directory");
 					for(Deity deity : Deity.Util.getAllDeitiesInAlliance(alliance))
 						player.sendMessage(ChatColor.GRAY + " /dg " + alliance.toLowerCase() + " " + deity.getName().toLowerCase());
 				}
 				else
 				{
 					for(final Deity deity : Deity.Util.getAllDeitiesInAlliance(alliance))
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
 												StringBuilder details = new StringBuilder(ChatColor.GRAY + "   " + Unicodes.rightwardArrow() + " ");
 												if(ability.getCommand() != null) details.append(ChatColor.GREEN + "/").append(ability.getCommand().toLowerCase()).append(ChatColor.WHITE).append(": ");
 												details.append(ChatColor.WHITE).append(detail);
 												add(details.toString());
 											}
 										}
 										add(" ");
 									}
 								})
 								{
 									player.sendMessage(toPrint);
 								}
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
 
 	private static boolean dg_admin(Player player, String option1, String option2, String option3, String option4)
 	{
 		Player toEdit;
 		DCharacter character;
 		int amount;
 
 		if(!player.hasPermission("demigods.admin")) return Demigods.message.noPermission(player);
 
 		if(option1 == null)
 		{
 			Demigods.message.tagged(player, "Admin Directory");
 			player.sendMessage(ChatColor.GRAY + " /dg admin wand");
 			player.sendMessage(ChatColor.GRAY + " /dg admin debug");
 			player.sendMessage(ChatColor.GRAY + " /dg admin check <p> <char>");
 			player.sendMessage(ChatColor.GRAY + " /dg admin remove [player|character] <name>");
 			player.sendMessage(ChatColor.GRAY + " /dg admin set [maxfavor|favor|devotion|ascensions] <p> <amt>");
 			player.sendMessage(ChatColor.GRAY + " /dg admin add [maxfavor|favor|devotion|ascensions] <p> <amt>");
 			player.sendMessage(ChatColor.GRAY + " /dg admin sub [maxfavor|favor|devotion|ascensions] <p> <amt>");
 			player.sendMessage(ChatColor.GRAY + " /dg admin reload");
 			player.sendMessage(ChatColor.DARK_RED + " /dg admin clear data yesdoitforsurepermanently");
 		}
 
 		if(option1 != null)
 		{
 			if(option1.equalsIgnoreCase("clear") && option2 != null && option2.equalsIgnoreCase("data") && option3 != null && option3.equalsIgnoreCase("yesdoitforsurepermanently"))
 			{
 				player.sendMessage(ChatColor.RED + Demigods.language.getText(Translation.Text.ADMIN_CLEAR_DATA_STARTING));
 				DataManager.flushData();
 				player.sendMessage(ChatColor.GREEN + Demigods.language.getText(Translation.Text.ADMIN_CLEAR_DATA_FINISHED));
 				return true;
 			}
 			else if(option1.equalsIgnoreCase("wand"))
 			{
 				if(!Admins.wandEnabled(player))
 				{
 					DataManager.saveTemp(player.getName(), "temp_admin_wand", true);
 					player.sendMessage(ChatColor.RED + "Your admin wand has been enabled for " + Material.getMaterial(Demigods.config.getSettingInt("admin.wand_tool")));
 				}
 				else if(Admins.wandEnabled(player))
 				{
 					DataManager.removeTemp(player.getName(), "temp_admin_wand");
 					player.sendMessage(ChatColor.RED + "You have disabled your admin wand.");
 				}
 				return true;
 			}
 			else if(option1.equalsIgnoreCase("debug"))
 			{
 				if(!DataManager.hasKeyTemp(player.getName(), "temp_admin_debug") || !Boolean.parseBoolean(DataManager.getValueTemp(player.getName(), "temp_admin_debug").toString()))
 				{
 					DataManager.saveTemp(player.getName(), "temp_admin_debug", true);
 					player.sendMessage(ChatColor.RED + "You have enabled debugging.");
 				}
 				else if(DataManager.hasKeyTemp(player.getName(), "temp_admin_debug") && Boolean.parseBoolean(DataManager.getValueTemp(player.getName(), "temp_admin_debug").toString()))
 				{
 					DataManager.removeTemp(player.getName(), "temp_admin_debug");
 					player.sendMessage(ChatColor.RED + "You have disabled debugging.");
 				}
 			}
 			else if(option1.equalsIgnoreCase("check"))
 			{
 				if(option2 == null)
 				{
 					player.sendMessage(ChatColor.RED + "You need to specify a player.");
 					player.sendMessage("/dg admin check <p>");
 					return true;
 				}
 
 				// Define variables
 				Player toCheck = Bukkit.getPlayer(option2);
 
 				if(option3 == null)
 				{
 					Demigods.message.tagged(player, ChatColor.RED + toCheck.getName() + " Player Check");
 					player.sendMessage(" Characters:");
 
 					final Set<DCharacter> chars = DPlayer.Util.getPlayer(toCheck).getCharacters();
 
 					for(DCharacter checkingChar : chars)
 					{
 						player.sendMessage(ChatColor.GRAY + "   (#: " + checkingChar.getId() + ") Name: " + checkingChar.getName() + " / Deity: " + checkingChar.getDeity());
 					}
 				}
 				else
 				{
 					// TODO: Display specific character information when called for.
 				}
 			}
 			else if(option1.equalsIgnoreCase("remove"))
 			{
 				if(option2 == null || option3 == null)
 				{
 					player.sendMessage(ChatColor.RED + "You need to be more specific with what you want to remove.");
 					return true;
 				}
 				else
 				{
 					if(option2.equalsIgnoreCase("player"))
 					{
 						// TODO: Full player data removal
 					}
 					else if(option2.equalsIgnoreCase("character"))
 					{
 						DCharacter removing = DCharacter.Util.getCharacterByName(option3);
 						String removingName = removing.getName();
 
 						// Remove the data
 						removing.remove();
 
 						player.sendMessage(ChatColor.RED + "Character \"" + removingName + "\" removed.");
 					}
 				}
 			}
 			else if(option1.equalsIgnoreCase("set"))
 			{
 				if(option2 == null || option3 == null)
 				{
 					player.sendMessage(ChatColor.RED + "You need to specify a player and amount.");
 					return true;
 				}
 				else
 				{
 					// Define variables
 					toEdit = Bukkit.getPlayer(option3);
 					character = DPlayer.Util.getPlayer(toEdit).getCurrent();
 					amount = Integer.parseInt(option4);
 				}
 
 				if(option2.equalsIgnoreCase("maxfavor"))
 				{
 					// Set the favor
 					character.getMeta().setMaxFavor(amount);
 
 					player.sendMessage(ChatColor.GREEN + "Max favor set to " + amount + " for " + toEdit.getName() + "'s current character.");
 
 					// Tell who was edited
 					toEdit.sendMessage(ChatColor.GREEN + "Your current character's max favor has been set to " + amount + ".");
 					toEdit.sendMessage(ChatColor.GRAY + "" + ChatColor.ITALIC + "This was performed by " + player.getName() + ".");
 					return true;
 				}
 				else if(option2.equalsIgnoreCase("favor"))
 				{
 					// Set the favor
 					character.getMeta().setFavor(amount);
 
 					player.sendMessage(ChatColor.GREEN + "Favor set to " + amount + " for " + toEdit.getName() + "'s current character.");
 
 					// Tell who was edited
 					toEdit.sendMessage(ChatColor.GREEN + "Your current character's favor has been set to " + amount + ".");
 					toEdit.sendMessage(ChatColor.GRAY + "" + ChatColor.ITALIC + "This was performed by " + player.getName() + ".");
 					return true;
 				}
 				else if(option2.equalsIgnoreCase("ascensions"))
 				{
 					// Set the ascensions
 					character.getMeta().setAscensions(amount);
 
 					player.sendMessage(ChatColor.GREEN + "Ascensions set to " + amount + " for " + toEdit.getName() + "'s current character.");
 
 					// Tell who was edited
 					toEdit.sendMessage(ChatColor.GREEN + "Your current character's Ascensions have been set to " + amount + ".");
 					toEdit.sendMessage(ChatColor.GRAY + "" + ChatColor.ITALIC + "This was performed by " + player.getName() + ".");
 					return true;
 				}
 			}
 			else if(option1.equalsIgnoreCase("add"))
 			{
 				if(option2 == null)
 				{
 					player.sendMessage(ChatColor.RED + "You need to be more specific.");
 					return true;
 				}
 				else if(option3 == null)
 				{
 					player.sendMessage(ChatColor.RED + "You must select a player and amount.");
 					return true;
 				}
 				else
 				{
 					// Define variables
 					toEdit = Bukkit.getPlayer(option3);
 					character = DPlayer.Util.getPlayer(toEdit).getCurrent();
 					amount = Integer.parseInt(option4);
 				}
 
 				if(option2.equalsIgnoreCase("maxfavor"))
 				{
 					// Set the favor
 					character.getMeta().setMaxFavor(character.getMeta().getMaxFavor() + amount);
 
 					player.sendMessage(ChatColor.GREEN + "" + amount + " added to " + toEdit.getName() + "'s current character's max favor.");
 
 					// Tell who was edited
 					toEdit.sendMessage(ChatColor.GREEN + "Your current character's max favor has been increased by " + amount + ".");
 					toEdit.sendMessage(ChatColor.GRAY + "" + ChatColor.ITALIC + "This was performed by " + player.getName() + ".");
 					return true;
 				}
 				else if(option2.equalsIgnoreCase("favor"))
 				{
 					// Set the favor
 					character.getMeta().addFavor(amount);
 
 					player.sendMessage(ChatColor.GREEN + "" + amount + " favor added to " + toEdit.getName() + "'s current character.");
 
 					// Tell who was edited
 					toEdit.sendMessage(ChatColor.GREEN + "Your current character has been given " + amount + " favor.");
 					toEdit.sendMessage(ChatColor.GRAY + "" + ChatColor.ITALIC + "This was performed by " + player.getName() + ".");
 					return true;
 				}
 				else if(option2.equalsIgnoreCase("ascensions"))
 				{
 					// Set the ascensions
 					character.getMeta().addAscensions(amount);
 
 					player.sendMessage(ChatColor.GREEN + "" + amount + " Ascension(s) added to " + toEdit.getName() + "'s current character.");
 
 					// Tell who was edited
 					toEdit.sendMessage(ChatColor.GREEN + "Your current character has been given " + amount + " Ascensions.");
 					toEdit.sendMessage(ChatColor.GRAY + "" + ChatColor.ITALIC + "This was performed by " + player.getName() + ".");
 					return true;
 				}
 			}
 			else if(option1.equalsIgnoreCase("sub"))
 			{
 				if(option2 == null)
 				{
 					player.sendMessage(ChatColor.RED + "You need to be more specific.");
 					return true;
 				}
 				else if(option3 == null)
 				{
 					player.sendMessage(ChatColor.RED + "You must select a player and amount.");
 					return true;
 				}
 				else
 				{
 					// Define variables
 					toEdit = Bukkit.getPlayer(option3);
 					character = DPlayer.Util.getPlayer(toEdit).getCurrent();
 					amount = Integer.parseInt(option4);
 				}
 
 				if(option2.equalsIgnoreCase("maxfavor"))
 				{
 					// Set the favor
 					character.getMeta().subtractFavor(amount);
 
 					player.sendMessage(ChatColor.GREEN + "" + amount + " removed from " + toEdit.getName() + "'s current character's max favor.");
 
 					// Tell who was edited
 					toEdit.sendMessage(ChatColor.RED + "Your current character's max favor has been reduced by " + amount + ".");
 					toEdit.sendMessage(ChatColor.GRAY + "" + ChatColor.ITALIC + "This was performed by " + player.getName() + ".");
 					return true;
 				}
 				if(option2.equalsIgnoreCase("favor"))
 				{
 					// Set the favor
 					character.getMeta().subtractFavor(amount);
 
 					player.sendMessage(ChatColor.GREEN + "" + amount + " favor removed from " + toEdit.getName() + "'s current character.");
 
 					// Tell who was edited
 					toEdit.sendMessage(ChatColor.RED + "Your current character has had " + amount + " favor removed.");
 					toEdit.sendMessage(ChatColor.GRAY + "" + ChatColor.ITALIC + "This was performed by " + player.getName() + ".");
 					return true;
 				}
 				else if(option2.equalsIgnoreCase("ascensions"))
 				{
 					// Set the ascensions
 					character.getMeta().subtractAscensions(amount);
 
 					player.sendMessage(ChatColor.GREEN + "" + amount + " Ascension(s) removed from " + toEdit.getName() + "'s current character.");
 
 					// Tell who was edited
 					toEdit.sendMessage(ChatColor.RED + "Your current character has had " + amount + " Ascension(s) removed.");
 					toEdit.sendMessage(ChatColor.GRAY + "" + ChatColor.ITALIC + "This was performed by " + player.getName() + ".");
 					return true;
 				}
 			}
 			else
 			{
 				player.sendMessage(ChatColor.RED + "Invalid category selected.");
 				player.sendMessage("/dg admin [set|add|sub] [maxfavor|favor|devotion|ascensions] <p> <amt>");
 				return true;
 			}
 		}
 		return true;
 	}
 }
