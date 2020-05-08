 package com.wolvencraft.prison;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.bukkit.Bukkit;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.configuration.serialization.ConfigurationSerialization;
 import org.bukkit.entity.Player;
 
 import com.sk89q.worldedit.bukkit.WorldEditPlugin;
 import com.wolvencraft.prison.events.LoginListener;
 import com.wolvencraft.prison.events.WandListener;
 import com.wolvencraft.prison.hooks.PrisonPlugin;
 import com.wolvencraft.prison.hooks.TimedTask;
 import com.wolvencraft.prison.region.PrisonRegion;
 import com.wolvencraft.prison.region.PrisonSelection;
 import com.wolvencraft.prison.settings.Language;
 import com.wolvencraft.prison.settings.Settings;
 import com.wolvencraft.prison.util.Message;
 
 public class PrisonSuite extends PrisonPlugin {
 	
 	private static WorldEditPlugin worldEditPlugin = null;
 	
 	private static CommandManager commandManager;
 	
 	private static List<PrisonPlugin> plugins;
 	private static List<TimedTask> tasks;
 	private static List<PrisonSelection> selections;
 	
 	private static Settings settings;
 	private FileConfiguration languageData = null;
 	private File languageDataFile = null;
 	private static Language language;
 	
 	public void onEnable() {
 		getConfig().options().copyDefaults(true);
 		saveConfig();
 		settings = new Settings(this);
 		
 		getLanguageData().options().copyDefaults(true);
 		saveLanguageData();
 		language = new Language(this);
 		Message.debug("1. Loaded plugin configuration");
 		
 		worldEditPlugin = (WorldEditPlugin) this.getServer().getPluginManager().getPlugin("WorldEdit");
 		if(worldEditPlugin != null) Message.log("WorldEdit found, using it for region selection");
 		Message.debug("2. Checked for WorldEdit");
 		
 		plugins = new ArrayList<PrisonPlugin>();
 		tasks = new ArrayList<TimedTask>();
 		selections = new ArrayList<PrisonSelection>();
 		
 		plugins.add(this);
 		
 		Message.debug("3. Accepting plugins...");
 		
 		ConfigurationSerialization.registerClass(PrisonRegion.class, "PrisonRegion");
 		Message.debug("4. Registered serializable classes");
 
 		commandManager = new CommandManager(this);
 		getCommand("prison").setExecutor(commandManager);
 		getCommand("ps").setExecutor(commandManager);
 		Message.debug("5. Started up the CommandManager");
 		
 		new LoginListener(this);
 		new WandListener(this);
 		Message.debug("6. Loaded event listeners");
 		
 		Message.log("PrisonCore started");
 		
 		Message.debug("7. Starting up the timer...");
 		
 		long checkEvery = settings.TICKRATE;
 		Bukkit.getScheduler().scheduleAsyncRepeatingTask(this, new Runnable() {
 			public void run() {
 				for(TimedTask task : tasks) {
 					task.run();
 				}
 			}
 		}, 0L, checkEvery);
 	}
 	
 	public void onDisable() {
 		
 	}
 	
 	public void reloadLanguageData() {
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
 
 	public void saveLanguageData() {
 		if (languageData == null || languageDataFile == null) return;
 		try { languageData.save(languageDataFile); }
 		catch (IOException ex) { Message.log("Could not save config to " + languageDataFile); }
 	}
 	
 	public static PrisonSuite addPlugin(PrisonPlugin plugin) {
 		plugins.add(plugin);
 		Message.log(plugin.getName() + " registered");
		return (PrisonSuite) Bukkit.getPluginManager().getPlugin("PrisonCore");
 	}
 	
 	public static boolean hasSelection(Player player) {
 		for(PrisonSelection sel : selections) {
 			if(sel.getPlayer().equals(player)) return true;
 		}
 		return false;
 	}
 	
 	public static PrisonSelection getSelection(Player player) {
 		PrisonSelection selection = null;
 		Message.debug(selections.size() + " selections found");
 		for(PrisonSelection sel : selections) {
 			if(sel.getPlayer().getName().equals(player.getName())) selection = sel;
 		}
 		if(selection == null) selection = addSelection(new PrisonSelection(player));
 		return selection;
 	}
 	
 	public static PrisonSelection addSelection(PrisonSelection selection) {
 		selections.add(selection);
 		return selection;
 	}
 
 	public static void addTask(TimedTask task) {
 		tasks.add(task);
 		Message.log(task.getName() + " registered");
 	}
 	
 	public static WorldEditPlugin getWorldEditPlugin() 	{ return worldEditPlugin; }
 	public static Settings getSettings() 				{ return settings; }
 	public static Language getLanguage() 				{ return language; }
 	public static List<PrisonPlugin> getPlugins() 		{ return plugins; }
 }
