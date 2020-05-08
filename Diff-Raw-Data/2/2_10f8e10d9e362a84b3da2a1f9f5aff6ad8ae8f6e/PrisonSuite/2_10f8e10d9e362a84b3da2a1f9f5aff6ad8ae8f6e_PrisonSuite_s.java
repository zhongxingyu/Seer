 package com.wolvencraft.prison;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Level;
 
 import net.milkbowl.vault.economy.Economy;
 
 import org.bukkit.Bukkit;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.configuration.serialization.ConfigurationSerialization;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.RegisteredServiceProvider;
 
 import com.sk89q.worldedit.bukkit.WorldEditPlugin;
 import com.wolvencraft.prison.events.LoginListener;
 import com.wolvencraft.prison.events.WandListener;
 import com.wolvencraft.prison.hooks.PrisonPlugin;
 import com.wolvencraft.prison.hooks.TimedTask;
 import com.wolvencraft.prison.metrics.Statistics;
 import com.wolvencraft.prison.region.PrisonRegion;
 import com.wolvencraft.prison.region.PrisonSelection;
 import com.wolvencraft.prison.settings.Language;
 import com.wolvencraft.prison.settings.Settings;
 import com.wolvencraft.prison.util.Message;
 
 public class PrisonSuite extends PrisonPlugin {
 	private static PrisonSuite plugin;
 	
 	private static WorldEditPlugin worldEditPlugin = null;
 	private static Economy economy = null;
 	
 	private static List<PrisonPlugin> plugins;
 	private static List<TimedTask> tasks;
 	private static List<PrisonSelection> selections;
 	
 	private static Settings settings;
 	private FileConfiguration languageData = null;
 	private File languageDataFile = null;
 	private static Language language;
 	
 	public void onEnable() {
 		plugin = this;
 		
 		getConfig().options().copyDefaults(true);
 		saveConfig();
 		settings = new Settings(this);
 		
 		getLanguageData().options().copyDefaults(true);
 		saveLanguageData();
 		language = new Language(this);
 		Message.debug("1. Loaded plugin configuration");
 		
 		worldEditPlugin = (WorldEditPlugin) this.getServer().getPluginManager().getPlugin("WorldEdit");
 		if(worldEditPlugin != null) Message.log("WorldEdit found, using it for region selection");
 		else Message.debug("No WorldEdit found");
 		
 		if (getServer().getPluginManager().getPlugin("Vault") != null) {
 			RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
 			if(rsp != null) {
 				economy = rsp.getProvider();
 				Message.log("Vault found, using it for the economy");
 			}
         } else Message.debug("No Vault found");
 		Message.debug("2. Checked for WorldEdit and Vault");
 		
 		plugins = new ArrayList<PrisonPlugin>();
 		tasks = new ArrayList<TimedTask>();
 		selections = new ArrayList<PrisonSelection>();
 		
 		plugins.add(this);
 		
 		Message.debug("3. Accepting plugins...");
 		
 		ConfigurationSerialization.registerClass(PrisonRegion.class, "PrisonRegion");
 		Message.debug("4. Registered serializable classes");
 
 		Message.debug("5. Started up the CommandManager");
 		
 		new LoginListener(this);
 		new WandListener(this);
 		Message.debug("6. Loaded event listeners");
 		
 		Message.log("PrisonCore started");
 		
 		new Statistics(this);
 		Message.debug("7. Attempted to start up PluginMetrics.");
 		
 		Message.debug("8. Starting up the timer...");
 		
 		long checkEvery = settings.TICKRATE;
 		Bukkit.getScheduler().runTaskTimer(this, new Runnable() {
 			public void run() {
 				for(TimedTask task : getLocalTasks()) {
 					if(task.getExpired()) {
 						Message.debug("Task expired: " + task.getName());
 						tasks.remove(task);
 					} else { task.run(); }
 				}
 			}
 		}, 0L, checkEvery);
 	}
 	
 	@Override
 	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
 		CommandManager.setSender(sender);
 		if(!(command.getName().equalsIgnoreCase("prison"))) return false;
 		
 		if(args.length == 0) {
 			CommandManager.HELP.run("");
 			CommandManager.resetSender();
 			return true;
 		}
 		for(CommandManager cmd : CommandManager.values()) {
 			if(cmd.isCommand(args[0])) {
 				
 				String argString = "/prison";
 		        for (String arg : args) { argString = argString + " " + arg; }
 				Message.debug(sender.getName() + ": " + argString);
 				
 				boolean result = cmd.run(args);
 				CommandManager.resetSender();
 				return result;
 			}
 		}
 		
 		Message.sendError(PrisonSuite.getLanguage().ERROR_COMMAND);
 		CommandManager.resetSender();
 		return false;
 	}
 	
 	public void onDisable() {
 		getServer().getScheduler().cancelTasks(this);
 		Message.log("PrisonSuite stopped");
 	}
 	
 	private void reloadLanguageData() {
 		String lang = PrisonSuite.getSettings().LANGUAGE;
 		if(lang == null) lang = "english";
 		lang = lang + ".yml";
 		Message.log("Language file used: " + lang);
 		
 		if (languageDataFile == null) languageDataFile = new File(getDataFolder(), lang);
 		languageData = YamlConfiguration.loadConfiguration(languageDataFile);
 		
 		InputStream defConfigStream = getResource(lang);
 		if (defConfigStream != null) {
 			YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
 			languageData.setDefaults(defConfig);
 		}
 	}
 	
 	public FileConfiguration getLanguageData() {
 		if (languageData == null) reloadLanguageData();
 		return languageData;
 	}
 
 	private void saveLanguageData() {
 		if (languageData == null || languageDataFile == null) return;
 		try { languageData.save(languageDataFile); }
 		catch (IOException ex) { Message.log("Could not save config to " + languageDataFile); }
 	}
 	
 	public static PrisonSuite addPlugin(PrisonPlugin plugin) {
 		plugins.add(plugin);
 		Message.log(plugin.getName() + " registered");
 		return (PrisonSuite) Bukkit.getPluginManager().getPlugin("PrisonSuite");
 	}
 	
 	public static PrisonPlugin getPlugin(String plugin) {
 		if(plugins.indexOf(plugin) == -1) return null;
 		else return plugins.get(plugins.indexOf(plugin));
 	}
 	
 	public static boolean hasSelection(Player player) {
 		for(PrisonSelection sel : selections) { if(sel.getPlayerName().equals(player.getPlayerListName())) return true; }
 		return false;
 	}
 	
 	public static PrisonSelection getSelection(Player player) {
 		PrisonSelection selection = null;
 		Message.debug(selections.size() + " selections found");
 		for(PrisonSelection sel : selections) {
 			if(sel.getPlayerName().equals(player.getPlayerListName())) selection = sel;
 		}
 		if(selection == null) selection = addSelection(new PrisonSelection(player));
 		return selection;
 	}
 	
 	public static PrisonSelection addSelection(PrisonSelection selection) {
 		selections.add(selection);
 		return selection;
 	}
 
 	public static void addTask(TimedTask task) {
 		for(TimedTask testTask : tasks) {
 			if(testTask.getName().equalsIgnoreCase(task.getName())) {
				Message.log(Level.WARNING, "Attempted to add a duplicate task");
 				return;
 			}
 		}
 		tasks.add(task);
 		Message.debug("Task added: " + task.getName());
 	}
 	
 	public static List<TimedTask> getLocalTasks() {
 		List<TimedTask> temp = new ArrayList<TimedTask>();
 		for(TimedTask task : tasks) temp.add(task);
 		return temp;
 	}
 	
 	public static PrisonSuite getInstance()				{ return plugin; }
 	public static WorldEditPlugin getWorldEditPlugin() 	{ return worldEditPlugin; }
 	public static boolean usingWorldEdit() 				{ return (worldEditPlugin != null && worldEditPlugin.isEnabled()); }
 	public static Economy getEconomy() 					{ return economy; }
 	public static Settings getSettings() 				{ return settings; }
 	public static Language getLanguage() 				{ return language; }
 	public static List<PrisonPlugin> getPlugins() 		{ return plugins; }
 	public double getVersion()							{ return Double.parseDouble(this.getDescription().getVersion()); }
 }
