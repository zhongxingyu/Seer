 package com.wolvencraft.prison.mines;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.List;
 
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.configuration.serialization.ConfigurationSerialization;
 
 import com.wolvencraft.prison.PrisonSuite;
 import com.wolvencraft.prison.hooks.PrisonPlugin;
 import com.wolvencraft.prison.mines.events.*;
 import com.wolvencraft.prison.mines.generation.BaseGenerator;
 import com.wolvencraft.prison.mines.mine.*;
 import com.wolvencraft.prison.mines.settings.*;
 import com.wolvencraft.prison.mines.util.GeneratorUtil;
 import com.wolvencraft.prison.mines.util.Message;
 import com.wolvencraft.prison.region.PrisonRegion;
 
 public class PrisonMine extends PrisonPlugin {
 	private static PrisonSuite prisonSuite;
 	private double version = 1.0;
 	
 	private static Settings settings;
 	private FileConfiguration languageData = null;
 	private File languageDataFile = null;
 	private static Language language;
 	
 	private CommandManager commandManager;
 	
 	private static List<Mine> mines;
 	private static List<DisplaySign> signs;
 	private static List<BaseGenerator> generators;
 	
 	public void onEnable() {
 		prisonSuite = PrisonSuite.addPlugin(this);
		Message.debug("1. Established connection with PrisonCore");
 
 		getConfig().options().copyDefaults(true);
 		saveConfig();
 		settings = new Settings(this);
 		
 		getLanguageData().options().copyDefaults(true);
 		saveLanguageData();
 		language = new Language(this);
 		Message.debug("2. Loaded plugin configuration");
 
 		commandManager = new CommandManager(this);
 		getCommand("mine").setExecutor(commandManager);
 		Message.debug("3. Started up the CommandManager");
 		
 		ConfigurationSerialization.registerClass(Mine.class, "Mine");
 		ConfigurationSerialization.registerClass(MineBlock.class, "MineBlock");
 		ConfigurationSerialization.registerClass(Blacklist.class, "Blacklist");
 		ConfigurationSerialization.registerClass(DisplaySign.class, "DisplaySign");
 		ConfigurationSerialization.registerClass(DataBlock.class, "DataBlock");
 		ConfigurationSerialization.registerClass(SimpleLoc.class, "SimpleLoc");
 		ConfigurationSerialization.registerClass(Protection.class, "Protection");
 		ConfigurationSerialization.registerClass(PrisonRegion.class, "PrisonRegion");
 		
 		Message.debug("4. Registered serializable classes");
 		
 		mines = MineData.loadAll();
 		signs = SignData.loadAll();
 		generators = GeneratorUtil.loadAll();
 		
 		Message.debug("5. Loaded data from file");
 		
 		new BlockBreakListener(this);
 		new BlockPlaceListener(this);
 		new BucketEmptyListener(this);
 		new BucketFillListener(this);
 		new SignClickListener(this);
 		new PVPListener(this);
 		new ButtonPressListener(this);
 		Message.debug("6. Started up event listeners");
 		
 		Message.log("PrisonMine started [ " + mines.size() + " mine(s) found ]");
 		
 		Message.debug("7. Sending a timed task to PrisonCore");
 		PrisonSuite.addTask(new MineTask(20));
 	}
 	
 	
 	public void onDisable()
 	{
 		MineData.saveAll();
 		SignData.saveAll();
 		
 		getServer().getScheduler().cancelTasks(this);
 		Message.log("[PrisonMine] Plugin stopped");
 	}
 	
 	public void reloadLanguageData() {
 		String lang = this.getConfig().getString("configuration.language");
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
 
 	public static List<Mine> getMines() 				{ return mines; }
 	public static List<DisplaySign> getSigns() 			{ return signs; }
 	public static List<BaseGenerator> getGenerators() 	{ return generators; }
 	public static Settings getSettings()				{ return settings; }
 	public static Language getLanguage()				{ return language; }
 	public static PrisonSuite getPrisonSuite() 			{ return prisonSuite; }
 	public double getVersion()							{ return version; }
 	
 	public static void setMines(List<Mine> newMines) {
 		mines.clear();
 		for(Mine mine : newMines) mines.add(mine);
 	}
 	
 	public static void setSigns(List<DisplaySign> newSigns) {
 		signs.clear();
 		for(DisplaySign sign : newSigns) signs.add(sign);
 	}
 }
