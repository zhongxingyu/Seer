 package com.TeamNovus.Supernaturals.Commands.Common;
 
 import java.util.ArrayList;
 import java.util.LinkedList;
 
 import org.apache.commons.lang.StringUtils;
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import com.TeamNovus.Supernaturals.Permission;
 import com.TeamNovus.Supernaturals.SNClasses;
 import com.TeamNovus.Supernaturals.SNPlayers;
 import com.TeamNovus.Supernaturals.Classes.Human;
 import com.TeamNovus.Supernaturals.Commands.BaseCommand;
 import com.TeamNovus.Supernaturals.Commands.CommandManager;
 import com.TeamNovus.Supernaturals.Player.SNClass;
 import com.TeamNovus.Supernaturals.Player.SNPlayer;
 import com.TeamNovus.Supernaturals.Player.Class.Ability;
 import com.TeamNovus.Supernaturals.Player.Class.Power;
 import com.TeamNovus.Supernaturals.Util.StringUtil;
 
 public class PluginCommands {
 
 	@BaseCommand(aliases = { "tutorial", "tut" }, desc = "Learn all about Supernaturals!", permission = Permission.COMMAND_TUTORIAL, usage = "[Page]", min = 0, max = 1)
 	public void onTutorialCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {		
 		int page = args.length >= 1 && StringUtil.isInteger(args[0]) ? Math.abs(Integer.valueOf(args[0])) : 1;
 		
 		switch (Math.abs(page)) {
 		case 0:
 		case 1:
 			sender.sendMessage(CommandManager.getExtra() + "___________________.[ " + CommandManager.getHighlight() + "Tutorial (1/4)" + CommandManager.getExtra() + " ].___________________");
 			sender.sendMessage(CommandManager.getLight() + "Welcome to Supernaturals!");
 			sender.sendMessage(CommandManager.getLight() + "First, try joining a class! /sn evolve <Class>");
 			sender.sendMessage(CommandManager.getLight() + "To see all the classes you can join type /sn classes");
 			sender.sendMessage(CommandManager.getLight() + "Now that you have joined a class you can devolve by typing /sn devolve!");
 			sender.sendMessage(CommandManager.getLight() + "To continue type /sn tutorial 2");
 			sender.sendMessage(CommandManager.getExtra() + "---------------------------------------------------");
 			break;
 			
 		case 2:
 			sender.sendMessage(CommandManager.getExtra() + "___________________.[ " + CommandManager.getHighlight() + "Tutorial (2/4)" + CommandManager.getExtra() + " ].___________________");
 			sender.sendMessage(CommandManager.getLight() + "When you are in a class you gain Experience from different sources!");
 			sender.sendMessage(CommandManager.getLight() + "This experience can be seen in /sn level.  This also tells you how much to level up!");
 			sender.sendMessage(CommandManager.getLight() + "Leveling up lets you unlock new powers, abilities and specs!");
 			sender.sendMessage(CommandManager.getLight() + "To see the unlockable perks type: /sn powers, /sn abilities and /sn specs");
 			sender.sendMessage(CommandManager.getLight() + "To continue type /sn tutorial 3");		
 			sender.sendMessage(CommandManager.getExtra() + "---------------------------------------------------");
 			break;
 			
 		case 3:
 			sender.sendMessage(CommandManager.getExtra() + "___________________.[ " + CommandManager.getHighlight() + "Tutorial (3/4)" + CommandManager.getExtra() + " ].___________________");
 			sender.sendMessage(CommandManager.getLight() + "When you are in a class you gain Experience from different sources!");
 			sender.sendMessage(CommandManager.getLight() + "Different classes have different and unique powers!");
 			sender.sendMessage(CommandManager.getLight() + "To cast a power you left-click with a crafted wand!  To switch powers, right-click!");
 			sender.sendMessage(CommandManager.getLight() + "Abilities are passive and are active at all times.");
 			sender.sendMessage(CommandManager.getLight() + "To continue type /sn tutorial 4");		
 			sender.sendMessage(CommandManager.getExtra() + "---------------------------------------------------");
 			break;
 
 		case 4:
			sender.sendMessage(CommandManager.getExtra() + "___________________.[ " + CommandManager.getHighlight() + "Tutorial (3/4)" + CommandManager.getExtra() + " ].___________________");
 			sender.sendMessage(CommandManager.getLight() + "You can craft different wands with different aptitudes!");
 			sender.sendMessage(CommandManager.getLight() + "    | _ R _ |    | G G G |    | D D D |");
 			sender.sendMessage(CommandManager.getLight() + "    | _ S _ |    | _ B _ |    | G B G |");
 			sender.sendMessage(CommandManager.getLight() + "    | _ S _ |    | _ B _ |    | G B G |");
 			sender.sendMessage(CommandManager.getLight() + "S = Stick, R = Redstone, B = Blaze Rod");
 			sender.sendMessage(CommandManager.getLight() + "G = Gold Ingot, D = Diamond, _ = Nothing ;)");
 			sender.sendMessage(CommandManager.getLight() + "Thats it!  We hope you enjoy using Supernaturals!");
 			sender.sendMessage(CommandManager.getExtra() + "---------------------------------------------------");
 			break;
 
 		default:
 			sender.sendMessage(ChatColor.RED + "The specified page was not found!");
 			break;
 		}
 	}
 	
 	@BaseCommand(aliases = { "online" }, desc = "View information on online players.", permission = Permission.COMMAND_ONLINE)
 	public void onOnlineCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
 		
 		ArrayList<SNClass> classes = new ArrayList<SNClass>();
 
 		for(SNPlayer player : SNPlayers.i.getOnlinePlayers()) {
 			if(!(classes.contains(player.getPlayerClass()))) {
 				classes.add(player.getPlayerClass());
 			}
 		}
 
 		ArrayList<String> lines = new ArrayList<String>();
 
 		for(SNClass playerClass : classes) {
 			ArrayList<String> names = new ArrayList<String>();
 
 			for(SNPlayer player : SNPlayers.i.getPlayersInClass(playerClass)) {
 				if(player.isOnline()) {
 					names.add(player.getName());
 				}
 			}
 
 			lines.add(playerClass.getColor() + playerClass.getName() + ": " + StringUtils.join(names, ", "));
 
 		}
 
 		if(lines.size() == 0) {
 			sender.sendMessage(CommandManager.getError() + "There are no players online!");
 			return;
 		}
 		
 		sender.sendMessage(CommandManager.getExtra() + "__________________.[ " + CommandManager.getHighlight() + "Online Players" + CommandManager.getExtra() + " ].__________________");
 		
 		for(String line : lines) {
 			sender.sendMessage(line);
 		}
 		
 		sender.sendMessage(CommandManager.getExtra() + "---------------------------------------------------");
 	}
 	
 	@BaseCommand(aliases = { "kd" }, desc = "View your kill-death ratio.", permission = Permission.COMMAND_KD, console = false)
 	public void onKillDeathCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
 		SNPlayer player = SNPlayers.i.get((Player) sender);
 		
 		sender.sendMessage(CommandManager.getExtra() + "__________________.[ " + CommandManager.getHighlight() + "Player KD" + CommandManager.getExtra() + " ].__________________");
 		
 		sender.sendMessage(CommandManager.getDark() + "Kills: " + CommandManager.getLight() + player.getKills());
 		sender.sendMessage(CommandManager.getDark() + "Deaths: " + CommandManager.getLight() + player.getDeaths());
 		sender.sendMessage(CommandManager.getDark() + "Ratio: " + CommandManager.getLight() + (player.getDeaths() > 0 ? 1.0 * player.getKills() / player.getDeaths() : player.getKills()));
 		
 		sender.sendMessage(CommandManager.getExtra() + "---------------------------------------------------");
 	}
 	
 	@BaseCommand(aliases = { "top" }, desc = "View the top players on the server!", permission = Permission.COMMAND_TOP)
 	public void onTopCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {		
 		sender.sendMessage(CommandManager.getExtra() + "__________________.[ " + CommandManager.getHighlight() + "Top Players" + CommandManager.getExtra() + " ].__________________");
 		
 		LinkedList<SNPlayer> maxPlayers = new LinkedList<SNPlayer>();
 		
 		int limit = 10;
 		for (int i = 0; i < limit; i++) {
 			SNPlayer maxPlayer = null;
 			for(SNPlayer player : SNPlayers.i.getAllPlayers()) {
 				if(maxPlayers.contains(player))
 					continue;
 				
 				if(maxPlayer == null) {
 					maxPlayer = player;
 				} else {
 					if(player.getKD() > maxPlayer.getKD()) {
 						maxPlayer = player;
 					}
 				}
 			}
 			
 			if(maxPlayer != null)
 				maxPlayers.add(maxPlayer);
 		}
 		
 		for (int i = 0; i < maxPlayers.size(); i++) {
 			SNPlayer player = maxPlayers.get(i);
 			
 			sender.sendMessage(CommandManager.getDark() + "" + (i + 1) + ". " + CommandManager.getLight() + player.getName() + CommandManager.getExtra() + " - " + CommandManager.getDark() + "Level: " + CommandManager.getLight() + player.getLevel() + ", " + CommandManager.getDark() + "KD: " + CommandManager.getLight() + player.getKD());		
 		}
 		
 		sender.sendMessage(CommandManager.getExtra() + "---------------------------------------------------");
 	}
 	
 	@BaseCommand(aliases = { "info", "stats" }, desc = "View information on online players.", usage = "[Player]", permission = Permission.COMMAND_INFO, max = 1)
 	public void onInfoCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
 		if(args.length == 0 && Permission.has(Permission.COMMAND_INFO, sender)) {
 			if(!(sender instanceof Player)) {
 				sender.sendMessage(ChatColor.RED + "This command cannot be ran from the console!");
 				return;
 			}
 
 			SNPlayer player = SNPlayers.i.get((Player) sender);
 
 			sender.sendMessage(CommandManager.getExtra() + "___________________.[ " + CommandManager.getHighlight() + "Player Info" + CommandManager.getExtra() + " ].___________________");
 			
 			sender.sendMessage(CommandManager.getDark() + "Name: " + CommandManager.getLight() + player.getName());
 			sender.sendMessage(CommandManager.getDark() + "Class: " + CommandManager.getLight() + player.getPlayerClass().getColor() + player.getPlayerClass().getName());
 			sender.sendMessage(CommandManager.getDark() + "Level: " + CommandManager.getLight() + player.getLevel() + "/" + player.getPlayerClass().getMaxLevel());
 			
 			player.sendMessage(CommandManager.getDark() + "Experience: "
 								+ ChatColor.RED + "[" + StringUtil.createBar(50, ChatColor.GOLD, ChatColor.GRAY, (player.getExperience() - player.getTotalExperienceFor(player.getLevel() - 1)), (player.getTotalExperienceFor(player.getLevel()) - player.getTotalExperienceFor(player.getLevel() - 1))) + ChatColor.RED + "]"
 								+ " (" + (player.getExperience() - player.getTotalExperienceFor(player.getLevel() - 1)) + "/" + (player.getTotalExperienceFor(player.getLevel()) - player.getTotalExperienceFor(player.getLevel() - 1)) + ")");
 			
 			sender.sendMessage(CommandManager.getDark() + "Health: "
 								+ ChatColor.RED + "[" + StringUtil.createBar(50, ChatColor.DARK_RED, ChatColor.GRAY, player.getHealth(), player.getMaxHealth()) + ChatColor.RED + "]"
 								+ " (" + player.getHealth() + "/" + player.getMaxHealth() + ")");
 			
 			sender.sendMessage(CommandManager.getDark() + "Mana: " 
 					 			+ ChatColor.RED + "[" + StringUtil.createBar(50, ChatColor.DARK_BLUE, ChatColor.GRAY, player.getMana(), player.getMaxMana()) + ChatColor.RED + "]"
 								+ " (" + player.getMana() + "/" + player.getMaxMana() + ")");
 						
 			sender.sendMessage(CommandManager.getDark() + "Hunger: " 
 								+ ChatColor.RED + "[" + StringUtil.createBar(50, ChatColor.DARK_GREEN, ChatColor.GRAY, player.getFoodLevel(), player.getMaxFoodLevel()) + ChatColor.RED + "]"
 								+ " (" + player.getFoodLevel() + "/" + player.getMaxFoodLevel() + ")");
 			
 			sender.sendMessage(CommandManager.getDark() + "Speed: " + ChatColor.RESET + player.getSpeed() + "/0.2");
 
 			sender.sendMessage(CommandManager.getExtra() + "---------------------------------------------------");
 		} else if(args.length == 1 && Permission.has(Permission.COMMAND_INFO_OTHERS, sender)) {
 			SNPlayer player = SNPlayers.i.getPlayer(args[0]);
 
 			if(player == null) {
 				sender.sendMessage(ChatColor.RED + "The specified player could not be found!");
 				return;
 			}
 
 			sender.sendMessage(CommandManager.getExtra() + "___________________.[ " + CommandManager.getHighlight() + "Player Info" + CommandManager.getExtra() + " ].___________________");
 			
 			sender.sendMessage(CommandManager.getDark() + "Name: " + ChatColor.RESET + player.getName());
 			sender.sendMessage(CommandManager.getDark() + "Class: " + ChatColor.RESET + player.getPlayerClass().getColor() + player.getPlayerClass().getName());
 			sender.sendMessage(CommandManager.getDark() + "Level: " + ChatColor.RESET + player.getLevel() + "/" + player.getPlayerClass().getMaxLevel());
 			
 			player.sendMessage(CommandManager.getDark() + "Experience: "
 								+ ChatColor.RED + "[" + StringUtil.createBar(50, ChatColor.GOLD, ChatColor.GRAY, (player.getExperience() - player.getTotalExperienceFor(player.getLevel() - 1)), (player.getTotalExperienceFor(player.getLevel()) - player.getTotalExperienceFor(player.getLevel() - 1))) + ChatColor.RED + "]"
 								+ " (" + (player.getExperience() - player.getTotalExperienceFor(player.getLevel() - 1)) + "/" + (player.getTotalExperienceFor(player.getLevel()) - player.getTotalExperienceFor(player.getLevel() - 1)) + ")");
 			
 			sender.sendMessage(CommandManager.getDark() + "Health: "
 								+ ChatColor.RED + "[" + StringUtil.createBar(50, ChatColor.DARK_RED, ChatColor.GRAY, player.getHealth(), player.getMaxHealth()) + ChatColor.RED + "]"
 								+ " (" + player.getHealth() + "/" + player.getMaxHealth() + ")");
 			
 			sender.sendMessage(CommandManager.getDark() + "Mana: " 
 					 			+ ChatColor.RED + "[" + StringUtil.createBar(50, ChatColor.DARK_BLUE, ChatColor.GRAY, player.getMana(), player.getMaxMana()) + ChatColor.RED + "]"
 								+ " (" + player.getMana() + "/" + player.getMaxMana() + ")");
 						
 			sender.sendMessage(CommandManager.getDark() + "Hunger: " 
 								+ ChatColor.RED + "[" + StringUtil.createBar(50, ChatColor.DARK_GREEN, ChatColor.GRAY, player.getFoodLevel(), player.getMaxFoodLevel()) + ChatColor.RED + "]"
 								+ " (" + player.getFoodLevel() + "/" + player.getMaxFoodLevel() + ")");
 			
 			sender.sendMessage(CommandManager.getDark() + "Speed: " + ChatColor.RESET + player.getSpeed() + "/0.2");
 			
 			sender.sendMessage(CommandManager.getExtra() + "---------------------------------------------------");
 		} else {
 			sender.sendMessage(ChatColor.RED + "You do not have permission for this command!");
 		}
 	}
 
 	@BaseCommand(aliases = { "speed" }, desc = "View your current speed.", permission = Permission.COMMAND_SPEED, console = false)
 	public void onSpeedCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
 		SNPlayer player = SNPlayers.i.get((Player) sender);
 
 		sender.sendMessage(CommandManager.getExtra() + "___________________.[ " + CommandManager.getHighlight() + "Player Speed" + CommandManager.getExtra() + " ].___________________");
 		sender.sendMessage(CommandManager.getDark() + "Speed: " + ChatColor.RESET + player.getSpeed() + "/0.2");
 		sender.sendMessage(CommandManager.getExtra() + "---------------------------------------------------");
 
 	}
 
 	@BaseCommand(aliases = { "mana" }, desc = "View your current mana.", permission = Permission.COMMAND_MANA, console = false)
 	public void onManaCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
 		SNPlayer player = SNPlayers.i.get((Player) sender);
 
 		sender.sendMessage(CommandManager.getExtra() + "___________________.[ " + CommandManager.getHighlight() + "Player Mana" + CommandManager.getExtra() + " ].___________________");
 		sender.sendMessage(CommandManager.getDark() + "Mana: " 
 	 			+ ChatColor.RED + "[" + StringUtil.createBar(50, ChatColor.DARK_BLUE, ChatColor.GRAY, player.getMana(), player.getMaxMana()) + ChatColor.RED + "]"
 				+ " (" + player.getMana() + "/" + player.getMaxMana() + ")");
 		sender.sendMessage(CommandManager.getExtra() + "---------------------------------------------------");
 	}
 
 	@BaseCommand(aliases = { "health" }, desc = "View your current health.", permission = Permission.COMMAND_HEALTH, console = false)
 	public void onHealthCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
 		SNPlayer player = SNPlayers.i.get((Player) sender);
 
 		sender.sendMessage(CommandManager.getExtra() + "___________________.[ " + CommandManager.getHighlight() + "Player Health" + CommandManager.getExtra() + " ].___________________");		
 		sender.sendMessage(CommandManager.getDark() + "Health: "
 				+ ChatColor.RED + "[" + StringUtil.createBar(50, ChatColor.DARK_RED, ChatColor.GRAY, player.getHealth(), player.getMaxHealth()) + ChatColor.RED + "]"
 				+ " (" + player.getHealth() + "/" + player.getMaxHealth() + ")");
 		sender.sendMessage(CommandManager.getExtra() + "---------------------------------------------------");
 	}
 
 	@BaseCommand(aliases = { "hunger", "food" }, desc = "View your current health.", permission = Permission.COMMAND_HUNGER, console = false)
 	public void onHungerCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
 		SNPlayer player = SNPlayers.i.get((Player) sender);
 
 		sender.sendMessage(CommandManager.getExtra() + "___________________.[ " + CommandManager.getHighlight() + "Player Hunger" + CommandManager.getExtra() + " ].___________________");		
 		sender.sendMessage(CommandManager.getDark() + "Hunger: " 
 				+ ChatColor.RED + "[" + StringUtil.createBar(50, ChatColor.DARK_GREEN, ChatColor.GRAY, player.getFoodLevel(), player.getMaxFoodLevel()) + ChatColor.RED + "]"
 				+ " (" + player.getFoodLevel() + "/" + player.getMaxFoodLevel() + ")");
 		sender.sendMessage(CommandManager.getExtra() + "---------------------------------------------------");
 	}
 
 	@BaseCommand(aliases = { "level" }, desc = "View your current level.", permission = Permission.COMMAND_LEVEL, console = false)
 	public void onLevelCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
 		SNPlayer player = SNPlayers.i.get((Player) sender);
 
 		sender.sendMessage(CommandManager.getExtra() + "___________________.[ " + CommandManager.getHighlight() + "Player Level" + CommandManager.getExtra() + " ].___________________");		
 		sender.sendMessage(CommandManager.getDark() + "Level: " + ChatColor.RESET + player.getLevel() + "/" + player.getPlayerClass().getMaxLevel());
 		player.sendMessage(CommandManager.getDark() + "Experience: "
 							+ ChatColor.RED + "[" + StringUtil.createBar(50, ChatColor.GOLD, ChatColor.GRAY, (player.getExperience() - player.getTotalExperienceFor(player.getLevel() - 1)), (player.getTotalExperienceFor(player.getLevel()) - player.getTotalExperienceFor(player.getLevel() - 1))) + ChatColor.RED + "]"
 							+ " (" + (player.getExperience() - player.getTotalExperienceFor(player.getLevel() - 1)) + "/" + (player.getTotalExperienceFor(player.getLevel()) - player.getTotalExperienceFor(player.getLevel() - 1)) + ")");
 		sender.sendMessage(CommandManager.getExtra() + "---------------------------------------------------");
 	}
 
 	@BaseCommand(aliases = { "powers" }, desc = "View powers for a class!", usage = "[Class]", permission = Permission.COMMAND_POWERS, min = 0, max = 1)
 	public void onPowersCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
 		if(!(sender instanceof Player) && args.length == 0) {
 			sender.sendMessage(ChatColor.RED + "This command cannot be ran from the console!");
 			return;
 		}
 		
 		SNPlayer player = SNPlayers.i.get((Player) sender);
 
 		sender.sendMessage(CommandManager.getExtra() + "___________________.[ " + CommandManager.getHighlight() + "Class Powers" + CommandManager.getExtra() + " ].___________________");		
 		
 		SNClass targetClass = player.getPlayerClass();
 		
 		if(args.length == 1) {
 			targetClass = SNClasses.i.getBestClass(args[0]);
 		}
 		
 		if(targetClass == null) {
 			sender.sendMessage(ChatColor.RED + "The specified class was not found!");
 			return;
 		}
 		
 		boolean containsContent = false;
 		
 		for (int i = 1; i <= targetClass.getMaxLevel(); i++) {
 			if(!(targetClass.hasChangedFrom(i - 1, i))) {
 				continue;
 			}
 			
 			if(targetClass.getUniquePowers(i).size() > 0) {
 				if(!containsContent) {
 					containsContent = true;
 
 					sender.sendMessage(CommandManager.getDark() + "Class: " + targetClass.getColor() + targetClass.getName());
 				}
 				
 				for(Power power : targetClass.getUniquePowers(i)) {
 					if(player.getPowers().contains(power)) {
 						sender.sendMessage(ChatColor.GREEN + "Level " + i + CommandManager.getExtra() + " - " + CommandManager.getDark() + power.getName() + ": " + CommandManager.getLight() + power.getDesc());
 					} else {
 						sender.sendMessage(ChatColor.RED + "Level " + i + CommandManager.getExtra() + " - " + CommandManager.getDark() + power.getName() + ": " + CommandManager.getLight() + power.getDesc());
 					}
 				}
 			}
 		}
 		
 		if(!(containsContent)) {
 			sender.sendMessage(ChatColor.RED + "This class does not have any powers!");
 		}
 	}
 	
 	@BaseCommand(aliases = { "abilities" }, desc = "View abilities for a class!", usage = "[Class]", permission = Permission.COMMAND_ABILITIES, min = 0, max = 1)
 	public void onAbilitiesCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
 		if(!(Permission.has(Permission.COMMAND_ABILITIES, sender))) {
 			sender.sendMessage(ChatColor.RED + "You do not have permission for this command!");
 			return;
 		}
 
 		if(!(sender instanceof Player) && args.length == 0) {
 			sender.sendMessage(ChatColor.RED + "This command cannot be ran from the console!");
 			return;
 		}
 		
 		SNPlayer player = SNPlayers.i.get((Player) sender);
 
 		sender.sendMessage(CommandManager.getExtra() + "___________________.[ " + CommandManager.getHighlight() + "Class Abilities" + CommandManager.getExtra() + " ].___________________");		
 		
 		SNClass targetClass = player.getPlayerClass();
 		
 		if(args.length == 1) {
 			targetClass = SNClasses.i.getBestClass(args[0]);
 		}
 		
 		if(targetClass == null) {
 			sender.sendMessage(ChatColor.RED + "The specified class was not found!");
 			return;
 		}
 		
 		boolean containsContent = false;
 		
 		for (int i = 1; i <= targetClass.getMaxLevel(); i++) {
 			if(!(targetClass.hasChangedFrom(i - 1, i))) {
 				continue;
 			}
 			
 			if(targetClass.getUniqueAbilities(i).size() > 0) {
 				if(!containsContent) {
 					containsContent = true;
 
 					sender.sendMessage(CommandManager.getDark() + "Class: " + targetClass.getColor() + targetClass.getName());
 				}
 				
 				for(Ability ability : targetClass.getUniqueAbilities(i)) {
 					if(player.getAbilities().contains(ability)) {
 						sender.sendMessage(ChatColor.GREEN + "Level " + i + CommandManager.getExtra() + " - " + CommandManager.getDark() + ability.getName() + ": " + CommandManager.getLight() + ability.getDesc());
 					} else {
 						sender.sendMessage(ChatColor.RED + "Level " + i + CommandManager.getExtra() + " - " + CommandManager.getDark() + ability.getName() + ": " + CommandManager.getLight() + ability.getDesc());
 					}
 				}
 			}
 		}
 		
 		if(!(containsContent)) {
 			sender.sendMessage(ChatColor.RED + "This class does not have any abilities!");
 		}
 	}
 	
 	@BaseCommand(aliases = { "specs" }, desc = "View specifications for a class!", usage = "[Class]", permission = Permission.COMMAND_SPECS, console = false, min = 0, max = 1)
 	public void onSpecsCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
 		SNPlayer player = SNPlayers.i.get((Player) sender);
 
 		sender.sendMessage(CommandManager.getExtra() + "___________________.[ " + CommandManager.getHighlight() + "Class Specs" + CommandManager.getExtra() + " ].___________________");		
 		
 		SNClass targetClass = player.getPlayerClass();
 		
 		if(args.length == 1) {
 			targetClass = SNClasses.i.getBestClass(args[0]);
 		}
 		
 		if(targetClass == null) {
 			sender.sendMessage(ChatColor.RED + "The specified class was not found!");
 			return;
 		}
 		
 		boolean containsContent = false;
 		
 		for (int i = 1; i <= targetClass.getMaxLevel(); i++) {
 			if(!(targetClass.hasChangedFrom(i - 1, i))) {
 				continue;
 			}
 			
 			if(!containsContent) {
 				containsContent = true;
 
 				sender.sendMessage(CommandManager.getDark() + "Class: " + targetClass.getColor() + targetClass.getName());
 			}
 			
 			if(player.getLevel() >= i && player.getPlayerClass().equals(targetClass)) {
 				if(targetClass.getMaxHealth(i) != targetClass.getMaxHealth(i - 1)) {
 					sender.sendMessage(ChatColor.GREEN + "Level " + i + CommandManager.getExtra() + " - " + CommandManager.getDark() + "Health: " + CommandManager.getLight() + targetClass.getMaxHealth(i));
 				}
 				
 				if(targetClass.getMaxMana(i) != targetClass.getMaxMana(i - 1)) {
 					sender.sendMessage(ChatColor.GREEN + "Level " + i + CommandManager.getExtra() + " - " + CommandManager.getDark() + "Mana: " + CommandManager.getLight() + targetClass.getMaxMana(i));
 				}
 				
 				if(targetClass.getMaxFoodLevel(i) != targetClass.getMaxFoodLevel(i - 1)) {
 					sender.sendMessage(ChatColor.GREEN + "Level " + i + CommandManager.getExtra() + " - " + CommandManager.getDark() + "Hunger: " + CommandManager.getLight() + targetClass.getMaxFoodLevel(i));
 				}
 				
 				if(targetClass.getSpeed(i) != targetClass.getSpeed(i - 1)) {
 					sender.sendMessage(ChatColor.GREEN + "Level " + i + CommandManager.getExtra() + " - " + CommandManager.getDark() + "Speed: " + CommandManager.getLight() + targetClass.getSpeed(i));
 				}
 
 
 			} else {
 				if(targetClass.getMaxHealth(i) != targetClass.getMaxHealth(i - 1)) {
 					sender.sendMessage(ChatColor.RED + "Level " + i + CommandManager.getExtra() + " - " + CommandManager.getDark() + "Health: " + CommandManager.getLight() + targetClass.getMaxHealth(i));
 				}
 				
 				if(targetClass.getMaxMana(i) != targetClass.getMaxMana(i - 1)) {
 					sender.sendMessage(ChatColor.RED + "Level " + i + CommandManager.getExtra() + " - " + CommandManager.getDark() + "Mana: " + CommandManager.getLight() + targetClass.getMaxMana(i));
 				}
 				
 				if(targetClass.getMaxFoodLevel(i) != targetClass.getMaxFoodLevel(i - 1)) {
 					sender.sendMessage(ChatColor.RED + "Level " + i + CommandManager.getExtra() + " - " + CommandManager.getDark() + "Hunger: " + CommandManager.getLight() + targetClass.getMaxFoodLevel(i));
 				}
 				
 				if(targetClass.getSpeed(i) != targetClass.getSpeed(i - 1)) {
 					sender.sendMessage(ChatColor.RED + "Level " + i + CommandManager.getExtra() + " - " + CommandManager.getDark() + "Speed: " + CommandManager.getLight() + targetClass.getSpeed(i));
 				}
 			}
 		}
 		
 		if(!(containsContent)) {
 			sender.sendMessage(ChatColor.RED + "This class does not have any specs!");
 		}
 	}
 	
 	@BaseCommand(aliases = { "classes" }, desc = "View available classes.", permission = Permission.COMMAND_CLASSES, console = false)
 	public void onClassesCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
 		sender.sendMessage(CommandManager.getExtra() + "__________________.[ " + CommandManager.getHighlight() + "Player Classes" + CommandManager.getExtra() + " ].__________________");		
 		
 		SNPlayer player = SNPlayers.i.get((Player) sender);
 
 		if(player.getPlayerClass().getParentClass() != null)
 			sender.sendMessage(CommandManager.getDark() + "Parent Class: " + ChatColor.RESET + player.getPlayerClass().getParentClass().getColor() + player.getPlayerClass().getParentClass().getName());
 
 		ArrayList<String> joinableClasses = new ArrayList<String>();
 
 		for(SNClass c : player.getJoinableClasses()) {
 			joinableClasses.add(c.getColor() + c.getName());
 		}
 
 		if(joinableClasses.size() > 0) {
 			if(joinableClasses.size() == 1) {
 				sender.sendMessage(CommandManager.getDark() + "Unlocked Class: " + ChatColor.RESET + StringUtils.join(joinableClasses, ", "));
 			} else {
 				sender.sendMessage(CommandManager.getDark() + "Unlocked Classes: " + ChatColor.RESET + StringUtils.join(joinableClasses, ", "));
 			}
 		}
 
 
 		ArrayList<String> unjoinableClasses = new ArrayList<String>();
 
 		for(SNClass c : player.getPlayerClass().getAllJoinableClasses()) {
 			if(!(player.getJoinableClasses().contains(c)))
 				unjoinableClasses.add(c.getColor() + c.getName());
 		}
 
 		if(unjoinableClasses.size() > 0) {
 			if(unjoinableClasses.size() == 1) {
 				sender.sendMessage(CommandManager.getDark() + "Locked Class: " + ChatColor.RESET + StringUtils.join(unjoinableClasses, ", "));
 			} else {
 				sender.sendMessage(CommandManager.getDark() + "Locked Classes: " + ChatColor.RESET + StringUtils.join(unjoinableClasses, ", "));
 			}
 		}
 		
 		sender.sendMessage(CommandManager.getExtra() + "---------------------------------------------------");
 	}
 
 	@BaseCommand(aliases = { "evolve", "convert" }, desc = "Evolve to another class.", usage = "<Class>", permission = Permission.COMMAND_EVOLVE, console = false, min = 1, max = 1)
 	public void onEvoloveCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
 		SNPlayer player = SNPlayers.i.get((Player) sender);
 		SNClass targetClass = SNClasses.i.getBestClass(args[0]);
 
 		if(targetClass == null) {
 			sender.sendMessage(ChatColor.RED + "The specified class was not found!");
 			return;
 		}
 
 		if(player.getPlayerClass().equals(targetClass)) {
 			sender.sendMessage(ChatColor.RED + "You are already in this class!");
 			return;
 		}
 
 		if(!(player.getJoinableClasses().contains(targetClass))) {
 			if(player.getPlayerClass().getAllJoinableClasses().contains(targetClass)) {
 				sender.sendMessage(ChatColor.RED + "You are not a high enough level to evolve into this class!");
 			} else {
 				sender.sendMessage(ChatColor.RED + "You cannot evolve into this class!");
 			}
 			return;
 		}
 
 		player.setPlayerClass(targetClass, true);
 		player.setExperience(0);
 
 		if(StringUtil.startsWithVowel(targetClass.getName())) {
 			player.sendMessage(ChatColor.GREEN + "You are now an " + targetClass.getColor() + targetClass.getName());
 			Bukkit.getServer().broadcastMessage(ChatColor.YELLOW + player.getName() + ChatColor.GREEN + " is now an " + targetClass.getColor() + targetClass.getName());
 		} else {
 			player.sendMessage(ChatColor.GREEN + "You are now a " + targetClass.getColor() + targetClass.getName());
 			Bukkit.getServer().broadcastMessage(ChatColor.YELLOW + player.getName() + ChatColor.GREEN + " is now a " + targetClass.getColor() + targetClass.getName());
 		}
 	}
 
 	@BaseCommand(aliases = { "devolve" }, desc = "Devolve to your parent race.", permission = Permission.COMMAND_DEVOLVE, console = false)
 	public void onDevolveCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
 		SNPlayer player = SNPlayers.i.get((Player) sender);
 
 		SNClass lastClass = player.getPlayerClass();
 		SNClass targetClass = player.getPlayerClass().getParentClass();
 
 		if(targetClass == null) {
 			sender.sendMessage(ChatColor.RED + "You cannot devolve any further!");
 			return;
 		}
 		
 		int level = targetClass.getLevelRequiredForClass(lastClass);
 		int amount = 5;
 
 		player.setPlayerClass(targetClass, false);
 		player.setLevel(level - amount >= 1 ? level - amount : 1);
 
 		if(StringUtil.startsWithVowel(targetClass.getName())) {
 			player.sendMessage(ChatColor.GREEN + "You are now an " + targetClass.getColor() + targetClass.getName());
 			Bukkit.getServer().broadcastMessage(ChatColor.YELLOW + player.getName() + ChatColor.GREEN + " is now an " + targetClass.getColor() + targetClass.getName());
 		} else {
 			player.sendMessage(ChatColor.GREEN + "You are now a " + targetClass.getColor() + targetClass.getName());
 			Bukkit.getServer().broadcastMessage(ChatColor.YELLOW + player.getName() + ChatColor.GREEN + " is now a " + targetClass.getColor() + targetClass.getName());
 		}
 	}
 
 	@BaseCommand(aliases = { "verbose" }, desc = "Toggle verbose messages.", permission = Permission.COMMAND_VERBOSE, console = false)
 	public void onVerboseCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
 		SNPlayer player = SNPlayers.i.get((Player) sender);
 
 		player.setVerbose(!(player.isVerbose()));
 
 		sender.sendMessage(ChatColor.GREEN + "Verbose messaging is now " + (player.isVerbose() ? ChatColor.GREEN : ChatColor.RED) + (player.isVerbose() ? "enabled" : "disabled") + ChatColor.GREEN + "!");
 	}
 	
 	@BaseCommand(aliases = { "gui" }, desc = "Toggle the gui.", permission = Permission.COMMAND_GUI, console = false)
 	public void onGuiCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
 		SNPlayer player = SNPlayers.i.get((Player) sender);
 
 		player.setUsingGUI(!(player.isUsingGUI()));
 		player.updateGUI();
 
 		sender.sendMessage(ChatColor.GREEN + "The GUI is now " + (player.isUsingGUI() ? ChatColor.GREEN : ChatColor.RED) + (player.isUsingGUI() ? "enabled" : "disabled") + ChatColor.GREEN + "!");
 	}
 
 	
 	@BaseCommand(aliases = { "reset" }, desc = "Reset all your data. DANGEROUS!", permission = Permission.COMMAND_RESET, console = false)
 	public void onResetCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
 		SNPlayer player = SNPlayers.i.get((Player) sender);
 
 		SNClass targetClass = new Human();
 
 		player.setPlayerClass(targetClass, true);
 		player.setExperience(0);
 
 		if(StringUtil.startsWithVowel(targetClass.getName())) {
 			player.sendMessage(ChatColor.GREEN + "You are now an " + targetClass.getColor() + targetClass.getName());
 			Bukkit.getServer().broadcastMessage(ChatColor.YELLOW + player.getName() + ChatColor.GREEN + " is now an " + targetClass.getColor() + targetClass.getName());
 		} else {
 			player.sendMessage(ChatColor.GREEN + "You are now a " + targetClass.getColor() + targetClass.getName());
 			Bukkit.getServer().broadcastMessage(ChatColor.YELLOW + player.getName() + ChatColor.GREEN + " is now a " + targetClass.getColor() + targetClass.getName());
 		}
 	}
 
 }
