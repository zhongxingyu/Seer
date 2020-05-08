 ﻿/*
  * This file is part of HelpPlus. HelpPlus is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free
  * Software Foundation, either version 3 of the License, or (at your option) any later version. HelpPlus is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details. You should have received
  * a copy of the GNU General Public License along with HelpPlus. If not, see <http://www.gnu.org/licenses/>.
  */
 package sk.tomsik68.helpplus;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.command.PluginCommand;
 import org.bukkit.command.PluginCommandYamlParser;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.avaje.ebean.Transaction;
 
 import sk.tomsik68.permsguru.EPermissions;
 
 /**
  * HelpPlus for Bukkit
  * 
  * @author Tomsik68
  */
 public class HelpPlus extends JavaPlugin {
 	public static int commandsPerPage = 7;
 	private ChatColor def = ChatColor.BLUE;
 	private ChatColor def1 = ChatColor.GOLD;
 	private ChatColor def2 = ChatColor.GREEN;
 	private boolean commandsLoaded = false, showPlugin = true;
 	private EPermissions perms;
 	private FileConfiguration config;
 
 	public HelpPlus() {
 		super();
 
 	}
 
 	@Override
 	public void onEnable() {
 		// Register our events
 		PluginDescriptionFile pdfFile = this.getDescription();
 		System.out.println("Enabling " + pdfFile.getName() + " version " + pdfFile.getVersion());
 		getCommand("help").setExecutor(this);
 		getCommand("hp").setExecutor(this);
 		getCommand("h+").setExecutor(this);
 		System.out.println("[HelpPlus] Checking DB...");
 		try {
 			getDatabase().find(CommandInfo.class).findRowCount();
 			System.out.println("Help+ table exists!");
 		} catch (Exception e) {
 			System.out.println("Help+ table doesn't exist. Creating one...");
 			installDDL();
 			System.out.println("Help+ table created.");
 		}
 		config = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "config.yml"));
 		if (new File(getDataFolder(), "config.yml").exists()) {
 			perms = EPermissions.valueOf(config.getString("perms"));
 			commandsPerPage = config.getInt("cmds-on-page");
 			def = ChatColor.valueOf(config.getString("colors.a").toUpperCase());
 			def1 = ChatColor.valueOf(config.getString("colors.b").toUpperCase());
 			def2 = ChatColor.valueOf(config.getString("colors.c").toUpperCase());
 			showPlugin = config.getBoolean("show.plugin");
 			System.out.println("[HelpPlus] Found config file. Loading overrriden commands...");
 			try {
 				HashSet<String> overridenCommands = new HashSet<String>(config.getConfigurationSection("commands").getKeys(false));
 				for (String name : overridenCommands) {
 					ConfigurationSection cs = config.getConfigurationSection("commands." + name);
 					CommandInfo ci = new CommandInfo(name, cs.getString("usage"), cs.getString("description"), null, cs.getString("permission"),
 							cs.getString("plugin", "<unknown>"));
 					addCommand(ci);
 				}
 				System.out.println("[HelpPlus] Overriden commands loaded.");
 			} catch (NullPointerException n) {
 				System.out.println("[HelpPlus] No overriden commands were found.");
 			}
 		} else {
 			System.out.println("[HelpPlus] Config file not found. Creating a new one...");
 			getDataFolder().mkdir();
 			try {
 				new File(getDataFolder(), "config.yml").createNewFile();
 				config = new YamlConfiguration();
 				config.set("perms", "Server");
 				config.set("cmds-on-page", 7);
 				config.set("colors.a", ChatColor.BLUE.name());
 				config.set("colors.b", ChatColor.GOLD.name());
 				config.set("colors.c", ChatColor.GREEN.name());
 				config.set("show.plugin", true);
 				config.save(new File(getDataFolder(), "config.yml"));
 				perms = EPermissions.None;
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 		System.out.println("[HelpPlus] Finally enabled. :)");
 
 	}
 
 	@Override
 	public List<Class<?>> getDatabaseClasses() {
 		List<Class<?>> list = new ArrayList<Class<?>>();
 		list.add(CommandInfo.class);
 		return list;
 	}
 
 	@Override
 	public void onDisable() {
 
 		// EXAMPLE: Custom code, here we just output some info so we can check all is well
 		System.out.println(this.getDescription().getName() + " is disabled!");
 	}
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command command, String label, String[] cmd) {
 		if (!commandsLoaded) {
 			for (Plugin plug : getServer().getPluginManager().getPlugins()) {
 				List<Command> plugComms = PluginCommandYamlParser.parse(plug);
 				for (Command c : plugComms) {
 					CommandInfo ci = new CommandInfo((PluginCommand) c);
 					addCommand(ci);
 				}
 			}
 			for (CommandInfo ci : getAllCommands()) {
 				config.set("commands." + ci.getName() + ".description", ci.getDescription());
 				config.set("commands." + ci.getName() + ".usage", ci.getUsage());
 				config.set("commands." + ci.getName() + ".aliases", ci.getAliases());
 				config.set("commands." + ci.getName() + ".permission", ci.getPermission());
 				config.set("commands." + ci.getName() + ".plugin", ci.getPlugin());
 			}
 			try {
 				config.save(new File(getDataFolder(), "config.yml"));
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 			commandsLoaded = true;
 		}
 		List<CommandInfo> commands = getAllCommands();
 		int commandsOnPage = 0;
 		if (cmd.length == 0) {
 			sender.sendMessage(def1 + "[HelpPlus] Available Commands Page 1 of " + Math.round(commands.size() / HelpPlus.commandsPerPage));
 			for (int i = 0; commandsOnPage != HelpPlus.commandsPerPage && i < commands.size(); i++) {
 				if (isCommandDisplayed(sender, commands.get(i))) {
 					CommandInfo ci = commands.get(i);
 					commandsOnPage++;
 					sender.sendMessage(def + " /" + ci.getName() + " - " + def2 + ci.getDescription());
 				}
 			}
 		} else if (cmd.length == 1) {
 			try {
 				final int page = Integer.parseInt(cmd[0]);
 				sender.sendMessage(def1 + "[HelpPlus] Available Commands Page " + page + " of " + Math.round(commands.size() / HelpPlus.commandsPerPage));
 				int i = page * commandsPerPage;
 				final int displayCommands = i + HelpPlus.commandsPerPage;
 				List<CommandInfo> coms = new ArrayList<CommandInfo>(commands);
 				for (; i < displayCommands;) {
 					CommandInfo ci = coms.get(i);
 					// no more commands to display
 					if (ci == null) {
 						return true;
 					}
 					if (isCommandDisplayed(sender, ci)) {
 						sender.sendMessage(def + " /" + ci.getName() + " - " + def2 + ci.getDescription());
 						i++;
 					}
 				}
 			} catch (Exception e) {
 				String cmdName = cmd[0];
 				if (cmdName.startsWith("/"))
 					cmdName.replaceFirst("/", "");
 				CommandInfo comm = getCommandInfo(cmdName);
 				// fix for / in command name
 				if (comm == null)
 					comm = getCommandInfo(cmdName);
 				if (comm != null && comm.getName().equalsIgnoreCase(cmdName) && isCommandDisplayed(sender, comm)) {
 					sender.sendMessage(def + "Command: " + def2 + "/" + comm.getName());
 					sender.sendMessage(def + "Description: " + def2 + comm.getDescription());
 					sender.sendMessage(def + "Usage: " + def2 + comm.getUsage());
 					sender.sendMessage(def + "Permission needed: " + def2 + comm.getPermission());
 					if (showPlugin)
 						sender.sendMessage(def + "Plugin: " + def2 + comm.getPlugin());
 					sender.sendMessage(def + "Aliases: " + def2 + comm.getAliases());
 					return true;
 				}
				//check if we have plugin called cmdName
 				if (getServer().getPluginManager().getPlugin(cmdName) != null) {
 					List<CommandInfo> plugComms = getAllCommands(cmdName);
 					int i = 0;
 					for (CommandInfo ci : plugComms) {
 						if (isCommandDisplayed(sender, ci)) {
 							sender.sendMessage(def + " /" + ci.getName() + " - " + def2 + ci.getDescription());
 							i++;
 						}
 						if (i == commandsPerPage)
 							break;
 					}
 
 				}
 				sender.sendMessage(ChatColor.RED + "[HelpPlus] Command/Plugin not found.");
 			}
 		} else if (cmd.length == 2) {
 			String pluginName = cmd[0];
 			Integer page = 0;
 			try {
 				page = Integer.parseInt(cmd[1]);
 			} catch (Exception e) {
 				sender.sendMessage("[HelpPlus] Number expected, " + cmd[1] + " given.");
 			}
 			if (getServer().getPluginManager().getPlugin(pluginName) != null) {
 				List<CommandInfo> plugComms = getAllCommands("plugin = " + pluginName);
 				sender.sendMessage(def1 + "[HelpPlus] Available Commands of " + pluginName + " Page " + page + " of " + Math.round(plugComms.size() / HelpPlus.commandsPerPage));
 				int i = page * commandsPerPage;
 				final int displayCommands = i + HelpPlus.commandsPerPage;
 				for (; i < displayCommands;) {
 					CommandInfo ci = plugComms.get(i);
 					if (isCommandDisplayed(sender, ci)) {
 						sender.sendMessage(def + " /" + ci.getName() + " - " + def2 + ci.getDescription());
 						i++;
 					}
 				}
 
 			} else {
 				sender.sendMessage(ChatColor.RED + "[HelpPlus] Plugin " + cmd[0] + " not found");
 			}
 		} else {
 			sender.sendMessage(ChatColor.RED + "/help [page | command]");
 			sender.sendMessage(ChatColor.RED + "/hp [page | command]");
 		}
 		return true;
 	}
 
 	private boolean isCommandDisplayed(CommandSender sender, CommandInfo ci) {
 		if (ci.getPermission() == null || ci.getPermission().equalsIgnoreCase("null"))
 			return true;
 		return !(sender instanceof Player) || perms.has((Player) sender, ci.getPermission());
 	}
 
 	public CommandInfo getCommandInfo(final String name) {
 		return getDatabase().find(CommandInfo.class).where().ieq("name", name).findUnique();
 	}
 
 	public boolean commandExists(String name) {
 		return getDatabase().find(CommandInfo.class).where().ieq("name", name).findList().isEmpty();
 	}
 
 	public void addCommand(final CommandInfo ci) {
 		Transaction transaction = getDatabase().beginTransaction();
 		
 		if (getDatabase().find(CommandInfo.class).where().ieq("name", ci.name) != null) {
 			List<CommandInfo> matchingStuff = getDatabase().find(CommandInfo.class).where().ieq("name", ci.getName()).findList();
 			for (CommandInfo com : matchingStuff) {
 				// update neccessary fields
 				if (com.getDescription() != null)
 					ci.setDescription(com.getDescription());
 				if (com.getUsage() != null)
 					ci.setUsage(com.getUsage());
 				if (com.getPermission() != null)
 					ci.setPermission(com.getPermission());
 				if (com.getPlugin() != null)
 					ci.setPlugin(com.getPlugin());
 				if (com.getAliases() != null)
 					ci.setAliases(com.getAliases());
 				getDatabase().delete(com);
 			}
 		}
 		getDatabase().insert(ci, transaction);
 		transaction.commit();
 		transaction.end();
 	}
 
 	public int getCommandsCount() {
 		return getDatabase().find(CommandInfo.class).findRowCount();
 	}
 
 	public List<CommandInfo> getAllCommands() {
 		return getDatabase().find(CommandInfo.class).orderBy("name").findList();
 	}
 
 	public List<CommandInfo> getAllCommands(final String plugin) {
 		return getDatabase().find(CommandInfo.class).where().ieq("plugin", plugin).orderBy("name").findList();
 	}
 }
