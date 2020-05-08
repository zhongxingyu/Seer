 package me.jophestus.JOPHWarn;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.command.ConsoleCommandSender;
 
 public class JOPHWarn extends JavaPlugin {
 	 public static JOPHWarn plugin;
 	Logger log = Logger.getLogger("Minecraft");
 	private FileConfiguration customConfig = null;
 	private File customConfigFile = null;
 	public final JOPHWarnListener Listener = new JOPHWarnListener(this);
 	public void reloadCustomConfig() {
 		if (customConfigFile == null) {
 			customConfigFile = new File(getDataFolder(), "warnings.yml");
 		}
 		customConfig = YamlConfiguration.loadConfiguration(customConfigFile);
 
 		// Look for defaults in the jar
 		InputStream defConfigStream = this.getResource("warnings.yml");
 		if (defConfigStream != null) {
 			YamlConfiguration defConfig = YamlConfiguration
 					.loadConfiguration(defConfigStream);
 			customConfig.setDefaults(defConfig);
 		}
 	}
 
 	public FileConfiguration getCustomConfig() {
 		if (customConfig == null) {
 			this.reloadCustomConfig();
 		}
 		return customConfig;
 	}
 
 	public void saveCustomConfig() {
 		if (customConfig == null || customConfigFile == null) {
 			return;
 		}
 		try {
 			getCustomConfig().save(customConfigFile);
 		} catch (IOException ex) {
 			this.getLogger().log(Level.SEVERE,
 					"Could not save config to " + customConfigFile, ex);
 		}
 	}
 
 	public void onEnable() {
 		SetupConfig();
 		loadConfiguration();
 		PluginManager pm = getServer().getPluginManager();
 	    pm.registerEvents(this.Listener, this);
 
 	}
 
 	private void SetupConfig() {

 		getConfig().options().copyDefaults(true);
 		saveDefaultConfig();
 	}
 
 	private void loadConfiguration() {
 
 		getCustomConfig().options().copyDefaults(true);
 		saveCustomConfig();
 	}
 
 	public void onDisable() {
 	}
 
 	public boolean onCommand(CommandSender sender, Command command,
 			String label, String[] args) {
 
 		if (command.getName().equalsIgnoreCase("warn")) {
 			
 			
 				
 				
 			
 			ConsoleCommandSender console = Bukkit.getServer()
 					.getConsoleSender();
 			int maxwarnings = this.getConfig().getInt("kickafter");
 			int maxwarningsBan = getConfig().getInt("banafter");
 			int custom1 = getConfig().getInt("custom1warns");
 			int custom2 = getConfig().getInt("custom2warns");
 			int custom3 = getConfig().getInt("custom3warns");
 			int custom4 = getConfig().getInt("custom4warns");
 			int custom5 = getConfig().getInt("custom5warns");
 			int custom6 = getConfig().getInt("custom6warns");
 
 			String custom1command = getConfig().getString("custom1command");
 			String custom2command = getConfig().getString("custom2command");
 			String custom3command = getConfig().getString("custom3command");
 			String custom4command = getConfig().getString("custom4command");
 			String custom5command = getConfig().getString("custom5command");
 			String custom6command = getConfig().getString("custom6command");
 			
 			if (args.length == 0 || args.length == 1) {
 				sender.sendMessage(ChatColor.RED
 						+ "[JOPHWarn] "
 						+ ChatColor.GREEN
 						+ "I'm sorry "
 						+ sender.getName()
 						+ ", You haven't provided enough args for this command.");
 
 				return false;
 			}
 			StringBuilder b = new StringBuilder();
 			for (int i = 1; i < args.length; i++) {
 				if (i != 1)
 					b.append(" ");
 				b.append(args[i]);
 			}
 			Player warnee = Bukkit.getServer().getPlayer(args[0]);
 			
 			List<String> warnings = getCustomConfig().getStringList(
 					args[0]);
 	if (warnee == null) {
 		OfflinePlayer offline = Bukkit.getServer()
 				.getOfflinePlayer(args[0]);
 				
 				warnings.add(b.toString() + " - By: " + sender.getName());
 				getCustomConfig().set(offline.getName() + "offline", warnings);
 				saveCustomConfig();
 				reloadCustomConfig();
 				sender.sendMessage(ChatColor.RED + "[JOPHWarn] " + ChatColor.GREEN + args[0] + " is not online. They will receive the warning when they logon");
 				warnings.clear();
 				return false;
 			}
 			
 		
 						
 			custom1command = custom1command.replace("%p", warnee.getName());
 			custom2command = custom2command.replace("%p", warnee.getName());
 			custom3command = custom3command.replace("%p", warnee.getName());
 			custom4command = custom4command.replace("%p", warnee.getName());
 			custom5command = custom5command.replace("%p", warnee.getName());
 			custom6command = custom6command.replace("%p", warnee.getName());
 
 			if (sender.hasPermission("JOPHWarn.warn")) {
 
 				sender.sendMessage(ChatColor.RED + "[JOPHWarn] "
 						+ ChatColor.GREEN + "Warning sent to "
 						+ warnee.getName());
 				warnee.sendMessage(ChatColor.BLACK
 						+ "+++++++++++++++++++++++++++++++++++++++");
 				warnee.sendMessage(ChatColor.RED + "You have been warned by: "
 						+ ChatColor.BLUE + sender.getName() + ChatColor.RED
 						+ " for:");
 				warnee.sendMessage(ChatColor.GREEN + b.toString());
 				warnee.sendMessage(ChatColor.BLACK
 						+ "+++++++++++++++++++++++++++++++++++++++");
 			
 				warnings.add(b.toString() + " - By: " + sender.getName());
 				getCustomConfig().set(warnee.getName(), warnings);
 				saveCustomConfig();
 				reloadCustomConfig();
 				int warningCount = warnings.size();
 				if (getConfig().getBoolean("enablekick", true)) {
 					if (warningCount == maxwarnings) {
 						warnee.kickPlayer(getConfig().getString("kickmessage"));
 						this.log.info(warnee.getName()
 								+ " reached the max warnings amount and was kicked");
 					}
 				}
 				if (getConfig().getBoolean("enableban", true)) {
 					if (warningCount == maxwarningsBan) {
 						Bukkit.getOfflinePlayer(args[0]).setBanned(true);
 						if (getServer().getPlayer(args[0]) != null) {
 							warnee.setBanned(true);
 							warnee.kickPlayer(getConfig().getString(
 									"banmessage"));
 							this.log.info(warnee.getName()
 									+ " reached the max warnings amount and was banned");
 						}
 
 					}
 				}
 				if (getConfig().getBoolean("enablecustom1", true)) {
 					if (warningCount == custom1) {
 
 						Bukkit.dispatchCommand(console, custom1command);
 					}
 				}
 				if (getConfig().getBoolean("enablecustom2", true)) {
 					if (warningCount == custom2) {
 
 						Bukkit.dispatchCommand(console, custom2command);
 					}
 				}
 				if (getConfig().getBoolean("enablecustom3", true)) {
 					if (warningCount == custom3) {
 
 						Bukkit.dispatchCommand(console, custom3command);
 					}
 				}
 				if (getConfig().getBoolean("enablecustom4", true)) {
 					if (warningCount == custom4) {
 
 						Bukkit.dispatchCommand(console, custom4command);
 					}
 				}
 				if (getConfig().getBoolean("enablecustom5", true)) {
 					if (warningCount == custom5) {
 
 						Bukkit.dispatchCommand(console, custom5command);
 					}
 				}
 				if (getConfig().getBoolean("enablecustom6", true)) {
 					if (warningCount == custom6) {
 
 						Bukkit.dispatchCommand(console, custom6command);
 					}
 				}
 
 				this.log.info("JOPHWarn:: " + sender.getName() + " warned "
 						+ warnee.getName() + " for:");
 				this.log.info("JOPHWarn:: " + b.toString());
 
 			} else {
 				sender.sendMessage(ChatColor.RED + "[JOPHWarn] "
 						+ ChatColor.GREEN + "I'm sorry " + sender.getName()
 						+ ", You can't do that.");
 			}
 
 		}
 
 		if (command.getName().equalsIgnoreCase("warnings")) {
 			if (args.length < 2){
 				sender.sendMessage(ChatColor.RED + "[JOPHWarn] "
 						+ ChatColor.GREEN + "You have not provided enough args :(");
 				return false;
 			}
 			if (sender.hasPermission("JOPHWarn.view")) {
 
 				if (args[0].equalsIgnoreCase("view")) {
 					reloadCustomConfig();
 					List<String> warns = getCustomConfig().getStringList(
 							args[1]);
 					sender.sendMessage(ChatColor.BLACK
 							+ "+++++++++++++++++++++++++++++++++++++++");
 					sender.sendMessage(ChatColor.GREEN + "Viewing " + args[1]
 							+ "'s Warnings");
 					for (String s : warns) {
 
 						sender.sendMessage(ChatColor.GOLD + s);
 
 					}
 					sender.sendMessage(ChatColor.BLACK
 							+ "+++++++++++++++++++++++++++++++++++++++");
 					return true;
 				}
 			}
 			if (sender.hasPermission("JOPHWarn.warnings.clearall")) {
 				if (args[0].equalsIgnoreCase("clear")) {
 					getCustomConfig().set(args[1], null);
 					saveCustomConfig();
 					reloadCustomConfig();
 					sender.sendMessage(ChatColor.RED + "[JOPHWarn]"
 							+ ChatColor.GREEN + " You have cleared " + args[1]
 							+ "'s warnings");
 					return true;
 
 				}
 			}
 		}
 
 		if (command.getName().equalsIgnoreCase("jophwarn")) {
 
 			
 					sender.sendMessage(ChatColor.RED + "[JOPHWarn]" + ChatColor.GREEN + " JOPHWarn, by JOPHESTUS. Version 1.6.2");
 							
 				}
 			
 		
 		return super.onCommand(sender, command, label, args);
 	}
 
 }
