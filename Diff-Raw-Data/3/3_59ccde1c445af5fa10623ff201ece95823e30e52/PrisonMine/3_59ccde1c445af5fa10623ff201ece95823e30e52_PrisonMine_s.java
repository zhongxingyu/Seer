 /*
  * PrisonMine
  * Copyright (C) 2012 bitWolfy
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
 
 package com.wolvencraft.prison.mines;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.configuration.serialization.ConfigurationSerialization;
 
 import com.wolvencraft.prison.PrisonSuite;
 import com.wolvencraft.prison.hooks.PrisonPlugin;
 import com.wolvencraft.prison.mines.events.*;
 import com.wolvencraft.prison.mines.mine.*;
 import com.wolvencraft.prison.mines.routines.AutomaticResetRoutine;
 import com.wolvencraft.prison.mines.settings.*;
 import com.wolvencraft.prison.mines.triggers.*;
 import com.wolvencraft.prison.mines.upgrade.MRLMine;
 import com.wolvencraft.prison.mines.upgrade.MRMine;
 import com.wolvencraft.prison.mines.util.DisplaySignTask;
 import com.wolvencraft.prison.mines.util.Message;
 import com.wolvencraft.prison.mines.util.data.Blacklist;
 import com.wolvencraft.prison.mines.util.data.BlockSerializable;
 import com.wolvencraft.prison.mines.util.data.MineBlock;
 import com.wolvencraft.prison.mines.util.data.SimpleLoc;
 import com.wolvencraft.prison.region.PrisonRegion;
 
 public class PrisonMine extends PrisonPlugin {
 	private static PrisonSuite prisonSuite;
 	private static PrisonMine plugin;
 	
 	private static Settings settings;
 	private FileConfiguration languageData = null;
 	private File languageDataFile = null;
 	private static Language language;
 	
 	private static List<Mine> mines;
 	private static List<DisplaySign> signs;
 
 	private static Map<CommandSender, Mine> curMines;
 	
 	@Override
 	public void onEnable() {
 		prisonSuite = PrisonSuite.addPlugin(this);
 		plugin = this;
 		
		
 		getConfig().options().copyDefaults(true);
 		saveConfig();
 		settings = new Settings(this);
 		Message.debug("+-----[ Starting up PrisonMine ]-----");
		Message.debug("| ");
 		Message.debug("+ Established connection with PrisonCore");
 		
 		getLanguageData().options().copyDefaults(true);
 		saveLanguageData();
 		language = new Language(this);
 		Message.debug("+ Loaded plugin configuration");
 		
 		ConfigurationSerialization.registerClass(Mine.class, "pMine");
 		ConfigurationSerialization.registerClass(MineBlock.class, "MineBlock");
 		ConfigurationSerialization.registerClass(Blacklist.class, "Blacklist");
 		ConfigurationSerialization.registerClass(DisplaySign.class, "DisplaySign");
 		ConfigurationSerialization.registerClass(SimpleLoc.class, "SimpleLoc");
 		ConfigurationSerialization.registerClass(PrisonRegion.class, "PrisonRegion");
 		ConfigurationSerialization.registerClass(BaseTrigger.class, "BaseTrigger");
 		ConfigurationSerialization.registerClass(TimeTrigger.class, "TimeTrigger");
 		ConfigurationSerialization.registerClass(CompositionTrigger.class, "CompositionTrigger");
 		ConfigurationSerialization.registerClass(BlockSerializable.class, "BlockSerializable");
 		
 		ConfigurationSerialization.registerClass(MRMine.class, "MRMine");
 		ConfigurationSerialization.registerClass(MRLMine.class, "MRLMine");
 		Message.debug("+ Registered serializable classes");
 		
 		mines = MineData.loadAll();
 		signs = SignData.loadAll();
 		
 		curMines = new HashMap<CommandSender, Mine>();
 		Message.debug("+ Loaded data from file");
 
 		Message.debug("+ Initializing Event Listeners");
 		new BlockProtectionListener(this);
 		new DisplaySignListener(this);
 		new PlayerListener(this);
 		new FlagListener(this);
 		
 		Message.debug("+ Sending sign task to PrisonCore");
 		PrisonSuite.addTask(new DisplaySignTask());
 		
 		Message.debug("+---[ End of report ]---");
 		
 		Message.log("PrisonMine started [ " + mines.size() + " mine(s) found ]");
 		
 		if(settings.RESET_ALL_MINES_ON_STARTUP) {
 			Message.log("Resetting all mines, as defined in the configuration");
 			for(Mine mine : mines) {
 				AutomaticResetRoutine.run(mine);
 			}
 		}
 		
 	}
 	
 	@Override
 	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
 		if(!command.getName().equalsIgnoreCase("mine")) return false;
 
 		CommandManager.setSender(sender);
 		
 		if(args.length == 0) {
 			CommandManager.HELP.run("");
 			return true;
 		}
 		
 		for(CommandManager cmd : CommandManager.values()) {
 			if(cmd.isCommand(args[0])) {
 				boolean result = cmd.run(args);
 				CommandManager.resetSender();
 				return result;
 			}
 		}
 		
 		Message.sendFormattedError(PrisonMine.getLanguage().ERROR_COMMAND);
 		CommandManager.resetSender();
 		return false;
 	}
 	
 	@Override
 	public void onDisable() {
 		MineData.saveAll();
 		SignData.saveAll();
 		
 		Message.log("Plugin stopped");
 	}
 	
 	public void reloadLanguageData() {
 		String lang = settings.LANGUAGE;
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
 	
 	public static PrisonMine getInstance() 				{ return plugin; }
 	public static Settings getSettings()				{ return settings; }
 	public static Language getLanguage()				{ return language; }
 	public static PrisonSuite getPrisonSuite() 			{ return prisonSuite; }
 	public double getVersion()							{ return Double.parseDouble(this.getDescription().getVersion()); }
 	public static Mine getCurMine(CommandSender sender) { return curMines.get(sender); }
 	public static Mine getCurMine() 					{ return getCurMine(CommandManager.getSender()); }
 	public static void setCurMine(Mine mine) 			{ setCurMine(CommandManager.getSender(), mine); }
 	public void reloadSettings()						{ settings = null; settings = new Settings(this); }
 	public void reloadLanguage()						{ language = null; language = new Language(this); }
 	
 	public static List<Mine> getLocalMines() { 
 		List<Mine> temp = new ArrayList<Mine>();
 		for(Mine mine : mines) temp.add(mine);
 		return temp;
 	}
 	
 	public static void setMines(List<Mine> newMines) {
 		mines.clear();
 		for(Mine mine : newMines) mines.add(mine);
 	}
 	
 	public static void addMine(Mine mine) 				{ mines.add(mine); }
 	public static void addMine(List<Mine> newMines) 	{ for(Mine mine : newMines) mines.add(mine); }
 	public static void removeMine (Mine mine) 			{ mines.remove(mine); }
 	
 	public static List<DisplaySign> getLocalSigns() { 
 		List<DisplaySign> temp = new ArrayList<DisplaySign>();
 		for(DisplaySign sign : signs) temp.add(sign);
 		return temp;
 	}
 	
 	public static void setSigns(List<DisplaySign> newSigns) {
 		signs.clear();
 		for(DisplaySign sign : newSigns) signs.add(sign);
 	}
 	
 	public static void setCurMine(CommandSender sender, Mine mine) {
 		if(curMines.get(sender) != null) curMines.remove(sender);
 		if(mine != null) curMines.put(sender, mine);
 	}
 	
 	public static void addSign(DisplaySign sign) 				{ signs.add(sign); }
 	public static void addSign(List<DisplaySign> newSigns) 		{ for(DisplaySign sign : newSigns) signs.add(sign); }
 	public static void removeSign (DisplaySign sign) 			{ signs.remove(sign); }
 }
