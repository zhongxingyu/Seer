 package com.TeamNovus.Supernaturals.Commands;
 
 import java.util.ArrayList;
 
 import org.apache.commons.lang.StringUtils;
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import com.TeamNovus.Supernaturals.Permission;
 import com.TeamNovus.Supernaturals.SNPlayers;
 import com.TeamNovus.Supernaturals.Classes.Human;
 import com.TeamNovus.Supernaturals.Player.SNClass;
 import com.TeamNovus.Supernaturals.Player.SNPlayer;
 import com.TeamNovus.Supernaturals.Player.Class.Ability;
 import com.TeamNovus.Supernaturals.Util.SNClassUtil;
 import com.TeamNovus.Supernaturals.Util.StringUtil;
 
 public class PluginCommands {
 	/* Commands:
 	 * 	Default:
 	 *  /sn online
 	 *  /sn info [Player]
 	 *  /sn mana
 	 *  /sn speed
 	 *  /sn health
 	 *  /sn hunger
 	 *  /sn level
 	 *  /sn classes
 	 *  /sn inspect <Class>
 	 *  /sn evolve <Class>
 	 *  /sn devolve
 	 *  /sn reset
 	 *  
 	 *  Admin:
 	 *  /sn mana <Give/Take/Reset> <Player> <Mana>
 	 *  /sn exp <Give/Take/Reset> <Player> <Exp>
 	 *  /sn set <Player> <Class>
 	 *  
 	 */
 
 	@BaseCommand(aliases = { "online" }, description = "View information on online players.", usage = "")
 	public void onOnlineCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
 		if(!(Permission.has(Permission.COMMAND_ONLINE, sender))) {
 			sender.sendMessage(ChatColor.RED + "You do not have permission for this command!");
 			return;
 		}
 
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
 				names.add(player.getName());
 			}
 
 			lines.add(playerClass.getColor() + playerClass.getName() + ": " + StringUtils.join(names, ", "));
 
 		}
 
 		for(String line : lines) {
 			sender.sendMessage(line);
 		}
 	}
 
 	@BaseCommand(aliases = { "info" }, description = "View information on online players.", usage = "[Player]", min = 1, max = 2)
 	public void onInfoCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
 		if(args.length == 1 && Permission.has(Permission.COMMAND_INFO, sender)) {
 			if(!(sender instanceof Player)) {
 				sender.sendMessage(ChatColor.RED + "This command cannot be ran from the console!");
 			}
 
 			SNPlayer player = SNPlayers.i.get((Player) sender);
 
 			sender.sendMessage(ChatColor.DARK_RED + "<>-------------------------<>");
 
 			sender.sendMessage(ChatColor.GOLD + "  Name: " + ChatColor.RESET + player.getName());
 			sender.sendMessage(ChatColor.GOLD + "  Class: " + ChatColor.RESET + player.getPlayerClass().getColor() + player.getPlayerClass().getName());
 
 			if(player.getPlayerClass().getParentClass() != null)
 				sender.sendMessage(ChatColor.GOLD + "  Parent Class: " + ChatColor.RESET + player.getPlayerClass().getParentClass().getColor() + player.getPlayerClass().getParentClass().getName());
 
 			ArrayList<String> joinableClasses = new ArrayList<String>();
 
 			for(SNClass c : player.getJoinableClasses()) {
 				joinableClasses.add(c.getColor() + c.getName());
 			}
 
 			if(joinableClasses.size() > 0) {
 				if(joinableClasses.size() == 1) {
 					sender.sendMessage(ChatColor.GOLD + "  Unlocked Class: " + ChatColor.RESET + StringUtils.join(joinableClasses, ", "));
 				} else {
 					sender.sendMessage(ChatColor.GOLD + "  Unlocked Classes: " + ChatColor.RESET + StringUtils.join(joinableClasses, ", "));
 				}
 			}
 			
 			ArrayList<String> unjoinableClasses = new ArrayList<String>();
 			
 			for(SNClass c : player.getPlayerClass().getAllJoinableClasses()) {
 				if(!(player.getJoinableClasses().contains(c)))
 					unjoinableClasses.add(c.getColor() + c.getName());
 			}
 
 			if(unjoinableClasses.size() > 0) {
 				if(unjoinableClasses.size() == 1) {
 					sender.sendMessage(ChatColor.GOLD + "  Locked Class: " + ChatColor.RESET + StringUtils.join(unjoinableClasses, ", "));
 				} else {
 					sender.sendMessage(ChatColor.GOLD + "  Locked Classes: " + ChatColor.RESET + StringUtils.join(unjoinableClasses, ", "));
 				}
 			}
 
 			sender.sendMessage(ChatColor.GOLD + "  Speed: " + ChatColor.RESET + player.getSpeed());
 			sender.sendMessage(ChatColor.GOLD + "  Mana: " + ChatColor.RESET + player.getMana() + "/" + player.getMaxMana());
 			sender.sendMessage(ChatColor.GOLD + "  Health: " + ChatColor.RESET + player.getHealth() + "/" + player.getMaxHealth());
 			sender.sendMessage(ChatColor.GOLD + "  Food Level: " + ChatColor.RESET + player.getFoodLevel() + "/" + player.getMaxFoodLevel());
 
 			player.sendMessage(ChatColor.DARK_RED + "<>-------------------------<>");
 		} else if(args.length == 2 && Permission.has(Permission.COMMAND_INFO_OTHERS, sender)) {
 			boolean found = false;
 
 			for(SNPlayer player : SNPlayers.i.getOnlinePlayers()) {
 				if(player.getName().startsWith(args[1]) && !(found)) {
 					found = true;
 
 					sender.sendMessage(ChatColor.DARK_RED + "<>-------------------------<>");
 
 					sender.sendMessage(ChatColor.GOLD + "  Name: " + ChatColor.RESET + player.getName());
 					sender.sendMessage(ChatColor.GOLD + "  Class: " + ChatColor.RESET + player.getPlayerClass().getColor() + player.getPlayerClass().getName());
 
 					if(player.getPlayerClass().getParentClass() != null)
 						sender.sendMessage(ChatColor.GOLD + "  Parent Class: " + ChatColor.RESET + player.getPlayerClass().getParentClass().getColor() + player.getPlayerClass().getParentClass().getName());
 
 					ArrayList<String> joinableClasses = new ArrayList<String>();
 
 					for(SNClass c : player.getJoinableClasses()) {
 						joinableClasses.add(c.getColor() + c.getName());
 					}
 
 					if(joinableClasses.size() > 0) {
 						if(joinableClasses.size() == 1) {
 							sender.sendMessage(ChatColor.GOLD + "  Unlocked Class: " + ChatColor.RESET + StringUtils.join(joinableClasses, ", "));
 						} else {
 							sender.sendMessage(ChatColor.GOLD + "  Unlocked Classes: " + ChatColor.RESET + StringUtils.join(joinableClasses, ", "));
 						}
 					}
 					
 					ArrayList<String> unjoinableClasses = new ArrayList<String>();
 					
 					for(SNClass c : player.getPlayerClass().getAllJoinableClasses()) {
 						if(!(player.getJoinableClasses().contains(c)))
 							unjoinableClasses.add(c.getColor() + c.getName());
 					}
 
 					if(unjoinableClasses.size() > 0) {
 						if(unjoinableClasses.size() == 1) {
 							sender.sendMessage(ChatColor.GOLD + "  Locked Class: " + ChatColor.RESET + StringUtils.join(unjoinableClasses, ", "));
 						} else {
 							sender.sendMessage(ChatColor.GOLD + "  Locked Classes: " + ChatColor.RESET + StringUtils.join(unjoinableClasses, ", "));
 						}
 					}
 
 					sender.sendMessage(ChatColor.GOLD + "  Speed: " + ChatColor.RESET + player.getSpeed());
 					sender.sendMessage(ChatColor.GOLD + "  Mana: " + ChatColor.RESET + player.getMana() + "/" + player.getMaxMana());
 					sender.sendMessage(ChatColor.GOLD + "  Health: " + ChatColor.RESET + player.getHealth() + "/" + player.getMaxHealth());
 					sender.sendMessage(ChatColor.GOLD + "  Food Level: " + ChatColor.RESET + player.getFoodLevel() + "/" + player.getMaxFoodLevel());
 					sender.sendMessage(ChatColor.DARK_RED + "<>-------------------------<>");
 				}
 
 				if(!(found)) {
 					sender.sendMessage(ChatColor.RED + "The specified player could not be found!");
 				}
 			}
 		} else {
 			sender.sendMessage(ChatColor.RED + "You do not have permission for this command!");
 		}
 	}
 
 	@BaseCommand(aliases = { "speed" }, description = "View your current speed.", usage = "")
 	public void onSpeedCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
 		if(!(Permission.has(Permission.COMMAND_INFO, sender))) {
 			sender.sendMessage(ChatColor.RED + "You do not have permission for this command!");
 			return;
 		}
 
 		if(!(sender instanceof Player)) {
 			sender.sendMessage(ChatColor.RED + "This command cannot be ran from the console!");
 		}
 
 		SNPlayer player = SNPlayers.i.get((Player) sender);
 
 		sender.sendMessage(ChatColor.GOLD + "Speed: " + ChatColor.RESET + player.getSpeed());
 	}
 
 	@BaseCommand(aliases = { "mana" }, description = "View your current mana.", usage = "")
 	public void onManaCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
 		if(!(Permission.has(Permission.COMMAND_INFO, sender))) {
 			sender.sendMessage(ChatColor.RED + "You do not have permission for this command!");
 			return;
 		}
 
 		if(!(sender instanceof Player)) {
 			sender.sendMessage(ChatColor.RED + "This command cannot be ran from the console!");
 		}
 
 		SNPlayer player = SNPlayers.i.get((Player) sender);
 
 		sender.sendMessage(ChatColor.GOLD + "Mana: " + ChatColor.RESET + player.getMana() + "/" + player.getMaxMana());
 	}
 
 	@BaseCommand(aliases = { "health" }, description = "View your current health.", usage = "")
 	public void onHealthCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
 		if(!(Permission.has(Permission.COMMAND_INFO, sender))) {
 			sender.sendMessage(ChatColor.RED + "You do not have permission for this command!");
 			return;
 		}
 
 		if(!(sender instanceof Player)) {
 			sender.sendMessage(ChatColor.RED + "This command cannot be ran from the console!");
 		}
 
 		SNPlayer player = SNPlayers.i.get((Player) sender);
 
 		sender.sendMessage(ChatColor.GOLD + "Health: " + ChatColor.RESET + player.getHealth() + "/" + player.getMaxHealth());
 	}
 
 	@BaseCommand(aliases = { "hunger", "food" }, description = "View your current health.", usage = "")
 	public void onHungerCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
 		if(!(Permission.has(Permission.COMMAND_INFO, sender))) {
 			sender.sendMessage(ChatColor.RED + "You do not have permission for this command!");
 			return;
 		}
 
 		if(!(sender instanceof Player)) {
 			sender.sendMessage(ChatColor.RED + "This command cannot be ran from the console!");
 		}
 
 		SNPlayer player = SNPlayers.i.get((Player) sender);
 
 		sender.sendMessage(ChatColor.GOLD + "Hunger: " + ChatColor.RESET + player.getFoodLevel() + "/" + player.getMaxFoodLevel());
 	}
 
 	@BaseCommand(aliases = { "level" }, description = "View your current level.", usage = "")
 	public void onLevelCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
 		if(!(Permission.has(Permission.COMMAND_INFO, sender))) {
 			sender.sendMessage(ChatColor.RED + "You do not have permission for this command!");
 			return;
 		}
 
 		if(!(sender instanceof Player)) {
 			sender.sendMessage(ChatColor.RED + "This command cannot be ran from the console!");
 		}
 
 		SNPlayer player = SNPlayers.i.get((Player) sender);
 
 		sender.sendMessage(ChatColor.GOLD + "Level: " + ChatColor.RESET + player.getLevel() + "/" + player.getPlayerClass().getMaxLevel());
 		sender.sendMessage(ChatColor.GOLD + "Experience to Level Up: " + ChatColor.RESET + player.getExperienceTill(player.getLevel() + 1));
 	}
 
 	@BaseCommand(aliases = { "classes" }, description = "View available classes.", usage = "")
 	public void onClassesCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
 		if(!(Permission.has(Permission.COMMAND_INFO, sender))) {
 			sender.sendMessage(ChatColor.RED + "You do not have permission for this command!");
 			return;
 		}
 
 		if(!(sender instanceof Player)) {
 			sender.sendMessage(ChatColor.RED + "This command cannot be ran from the console!");
 		}
 
 		SNPlayer player = SNPlayers.i.get((Player) sender);
 
 		if(player.getPlayerClass().getParentClass() != null)
 			sender.sendMessage(ChatColor.GOLD + "Parent Class: " + ChatColor.RESET + player.getPlayerClass().getParentClass().getColor() + player.getPlayerClass().getParentClass().getName());
 
 		ArrayList<String> joinableClasses = new ArrayList<String>();
 
 		for(SNClass c : player.getJoinableClasses()) {
 			joinableClasses.add(c.getColor() + c.getName());
 		}
 
 		if(joinableClasses.size() > 0) {
 			if(joinableClasses.size() == 1) {
 				sender.sendMessage(ChatColor.GOLD + "Unlocked Class: " + ChatColor.RESET + StringUtils.join(joinableClasses, ", "));
 			} else {
 				sender.sendMessage(ChatColor.GOLD + "Unlocked Classes: " + ChatColor.RESET + StringUtils.join(joinableClasses, ", "));
 			}
 		}
 
 		
 		ArrayList<String> unjoinableClasses = new ArrayList<String>();
 		
 		for(SNClass c : player.getPlayerClass().getAllJoinableClasses()) {
 			if(!(player.getJoinableClasses().contains(c)))
 				unjoinableClasses.add(c.getColor() + c.getName());
 		}
 
 		if(unjoinableClasses.size() > 0) {
 			if(unjoinableClasses.size() == 1) {
 				sender.sendMessage(ChatColor.GOLD + "Locked Class: " + ChatColor.RESET + StringUtils.join(unjoinableClasses, ", "));
 			} else {
 				sender.sendMessage(ChatColor.GOLD + "Locked Classes: " + ChatColor.RESET + StringUtils.join(unjoinableClasses, ", "));
 			}
 		}
 	}
 
 	@BaseCommand(aliases = { "inspect" }, description = "View information about a specific class.", usage = "<Class>", min = 2, max = 2)
 	public void onInspectCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
 		if(!(Permission.has(Permission.COMMAND_INFO, sender))) {
 			sender.sendMessage(ChatColor.RED + "You do not have permission for this command!");
 			return;
 		}
 
 		SNClass targetClass = SNClassUtil.getBestClass(args[1], new Human());
 
 		if(targetClass == null) {
 			sender.sendMessage(ChatColor.RED + "The specified class was not found!");
 			return;
 		}
 
 		sender.sendMessage(ChatColor.DARK_RED + "<>-------------------------<>");
 
 		sender.sendMessage(ChatColor.GOLD + "  Name: " + ChatColor.RESET + targetClass.getColor() + targetClass.getName());
 		sender.sendMessage(ChatColor.GOLD + "  Max Level: " + ChatColor.RESET + targetClass.getMaxLevel());
 
 		if(targetClass.getParentClass() != null)
 			sender.sendMessage(ChatColor.GOLD + "  Parent Class: " + ChatColor.RESET + targetClass.getParentClass().getColor() + targetClass.getParentClass().getName());
 
 		for (int i = 0; i < targetClass.getMaxLevel() + 1; i++) {
			if(!(targetClass.hasChangedFrom(i - 1, i)) && i != 0) {
 				continue;
 			}
 
 			sender.sendMessage(ChatColor.GOLD + "  Level: " + ChatColor.RESET + i);
 
 			ArrayList<String> classes = new ArrayList<String>();
 
 			for(SNClass c : targetClass.getJoinableClasses(i)) {
 				if(!(targetClass.getJoinableClasses(i - 1).contains(c)))
 					classes.add(c.getColor() + c.getName());
 			}
 
 			if(classes.size() > 0) {
 				if(classes.size() == 1) {
 					sender.sendMessage(ChatColor.GOLD + "    Unlocked Class: " + ChatColor.RESET + StringUtils.join(classes, ", "));
 				} else {
 					sender.sendMessage(ChatColor.GOLD + "    Unlocked Classes: " + ChatColor.RESET + StringUtils.join(classes, ", "));
 				}
 			}
 
 			sender.sendMessage(ChatColor.GOLD + "    Speed: " + ChatColor.RESET + targetClass.getSpeed(i));
 			sender.sendMessage(ChatColor.GOLD + "    Max Mana: " + ChatColor.RESET + targetClass.getMaxMana(i));
 			sender.sendMessage(ChatColor.GOLD + "    Max Health: " + ChatColor.RESET + targetClass.getMaxHealth(i));
 			sender.sendMessage(ChatColor.GOLD + "    Max Food Level: " + ChatColor.RESET + targetClass.getMaxFoodLevel(i));
 
 			if(targetClass.getUniqueAbilities(i).size() > 0) {
 				sender.sendMessage(ChatColor.GOLD + "    Abilities: " + ChatColor.RESET);
 				for(Ability a : targetClass.getUniqueAbilities(i)) {
 					sender.sendMessage(ChatColor.GOLD + "      " + a.getName() + ": " + ChatColor.RESET + a.getDesc());
 				}
 			}
 		}
 
 		sender.sendMessage(ChatColor.DARK_RED + "<>-------------------------<>");
 	}
 
 	@BaseCommand(aliases = { "evolve", "convert" }, description = "Evolve to another class.", usage = "<Class>", min = 2, max = 2)
 	public void onEvoloveCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
 		if(!(sender instanceof Player)) {
 			sender.sendMessage(ChatColor.RED + "This command can only be ran as a player!");
 			return;
 		}
 
 		if(!(Permission.has(Permission.COMMAND_INFO, sender))) {
 			sender.sendMessage(ChatColor.RED + "You do not have permission for this command!");
 			return;
 		}
 
 		SNPlayer player = SNPlayers.i.get((Player) sender);
 		SNClass targetClass = SNClassUtil.getBestClass(args[1], new Human());
 
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
 
 		player.setPlayerClass(targetClass);
 		if(StringUtil.startsWithVowel(targetClass.getName())) {
 			player.sendMessage(ChatColor.GREEN + "You are now an " + targetClass.getColor() + targetClass.getName());
 			Bukkit.getServer().broadcastMessage(ChatColor.YELLOW + player.getName() + ChatColor.GREEN + " has is now an " + targetClass.getColor() + targetClass.getName());
 		} else {
 			player.sendMessage(ChatColor.GREEN + "You are now a " + targetClass.getColor() + targetClass.getName());
 			Bukkit.getServer().broadcastMessage(ChatColor.YELLOW + player.getName() + ChatColor.GREEN + " has is now a " + targetClass.getColor() + targetClass.getName());
 		}
 	}
 
 	@BaseCommand(aliases = { "devolve" }, description = "Devolve to your parent race.", usage = "")
 	public void onDevolveCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
 		if(!(Permission.has(Permission.COMMAND_INFO, sender))) {
 			sender.sendMessage(ChatColor.RED + "You do not have permission for this command!");
 			return;
 		}
 
 		if(!(sender instanceof Player)) {
 			sender.sendMessage(ChatColor.RED + "This command cannot be ran from the console!");
 		}
 
 		SNPlayer player = SNPlayers.i.get((Player) sender);
 
 		SNClass targetClass = player.getPlayerClass().getParentClass();
 		
 		if(targetClass == null) {
 			sender.sendMessage(ChatColor.RED + "You cannot devolve any further!");
 		}
 		
 		player.setPlayerClass(targetClass);
 		if(StringUtil.startsWithVowel(targetClass.getName())) {
 			player.sendMessage(ChatColor.GREEN + "You are now an " + targetClass.getColor() + targetClass.getName());
 			Bukkit.getServer().broadcastMessage(ChatColor.YELLOW + player.getName() + ChatColor.GREEN + " has is now an " + targetClass.getColor() + targetClass.getName());
 		} else {
 			player.sendMessage(ChatColor.GREEN + "You are now a " + targetClass.getColor() + targetClass.getName());
 			Bukkit.getServer().broadcastMessage(ChatColor.YELLOW + player.getName() + ChatColor.GREEN + " has is now a " + targetClass.getColor() + targetClass.getName());
 		}
 	}
 	
 	@BaseCommand(aliases = { "reset" }, description = "Reset all your data. DANGEROUS!", usage = "")
 	public void onResetCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
 		if(!(Permission.has(Permission.COMMAND_INFO, sender))) {
 			sender.sendMessage(ChatColor.RED + "You do not have permission for this command!");
 			return;
 		}
 
 		if(!(sender instanceof Player)) {
 			sender.sendMessage(ChatColor.RED + "This command cannot be ran from the console!");
 		}
 
 		SNPlayer player = SNPlayers.i.get((Player) sender);
 
 		SNClass targetClass = new Human();
 		
 		player.setPlayerClass(targetClass);
 		if(StringUtil.startsWithVowel(targetClass.getName())) {
 			player.sendMessage(ChatColor.GREEN + "You are now an " + targetClass.getColor() + targetClass.getName());
 			Bukkit.getServer().broadcastMessage(ChatColor.YELLOW + player.getName() + ChatColor.GREEN + " has is now an " + targetClass.getColor() + targetClass.getName());
 		} else {
 			player.sendMessage(ChatColor.GREEN + "You are now a " + targetClass.getColor() + targetClass.getName());
 			Bukkit.getServer().broadcastMessage(ChatColor.YELLOW + player.getName() + ChatColor.GREEN + " has is now a " + targetClass.getColor() + targetClass.getName());
 		}
 	}
 	
 }
