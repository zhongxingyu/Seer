 /*
  * PrisonMine.java
  * 
  * PrisonMine
  * Copyright (C) 2013 bitWolfy <http://www.wolvencraft.com> and contributors
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
 import java.util.logging.Level;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.configuration.serialization.ConfigurationSerialization;
 
 import com.wolvencraft.prison.PrisonSuite;
 import com.wolvencraft.prison.hooks.PrisonPlugin;
 import com.wolvencraft.prison.hooks.TimedTask;
 import com.wolvencraft.prison.mines.events.*;
 import com.wolvencraft.prison.mines.mine.*;
 import com.wolvencraft.prison.mines.routines.AutomaticResetRoutine;
 import com.wolvencraft.prison.mines.settings.*;
 import com.wolvencraft.prison.mines.triggers.*;
 import com.wolvencraft.prison.mines.upgrade.*;
 import com.wolvencraft.prison.mines.util.DisplaySignTask;
 import com.wolvencraft.prison.mines.util.Message;
 import com.wolvencraft.prison.mines.util.data.*;
 import com.wolvencraft.prison.region.PrisonRegion;
 
 /**
  * <b>Main plugin class.</b><br />
  * Establishes connection with PrisonSuite and loads mines from memory
  * @author bitWolfy
  *
  */
 public class PrisonMine extends PrisonPlugin {
     private static PrisonMine instance;
     private static PrisonSuite prisonSuite;
     
     private static Settings settings;
     private static Language language;
     private FileConfiguration languageData = null;
     private File languageDataFile = null;
     
     private static List<Mine> mines;
     private static List<DisplaySign> signs;
 
     private static Map<CommandSender, Mine> curMines;
     
     private static TimedTask signTask;
     
     /**
      * <b>Default constructor</b><br />
      * Establishes the connection with PrisonSuite and loads plugin configuration
      */
     public PrisonMine() {
         instance = this;
         prisonSuite = PrisonSuite.addPlugin(this);
 
         getConfig().options().copyDefaults(true);
         saveConfig();
         settings = new Settings(this);
         
         getLanguageData().options().copyDefaults(true);
         saveLanguageData();
         language = new Language(this);
    }
    
    @Override
    public void onEnable() {
         Message.debug("+-----[ Starting up PrisonMine ]-----");
 
         Message.debug("+ Register serializable classes");
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
         
         Message.debug("+ Load mine and sign data from file");
         try { mines = MineData.loadAll(); }
         catch (Exception e) {
             Message.log(Level.SEVERE, "=== An error occurred while loading mine files ===");
             e.printStackTrace();
             Message.log(Level.SEVERE, "=== === === ===  End of error log  === === === ===");
         }
         
         try { signs = SignData.loadAll(); }
         catch (Exception e) {
             Message.log(Level.SEVERE, "=== An error occurred while loading sign files ===");
             e.printStackTrace();
             Message.log(Level.SEVERE, "=== === === ===  End of error log  === === === ===");
         }
         
         curMines = new HashMap<CommandSender, Mine>();
 
         Message.debug("+ Initialize Event Listeners");
         new BlockProtectionListener(this);
         new DisplaySignListener(this);
         new PlayerListener(this);
         new FlagListener(this);
 //      new RedstoneListener(this);
         
         Message.debug("+ Sending sign task to PrisonCore");
         signTask = new DisplaySignTask();
         PrisonSuite.addTask(signTask);
         
         Message.debug("+---[ End of report ]---");
         
         Message.log("PrisonMine started [ " + mines.size() + " mine(s) found ]");
         
         if(settings.RESET_ALL_MINES_ON_STARTUP) {
             Message.log("Resetting all mines, as defined in the configuration");
             for(Mine mine : mines) AutomaticResetRoutine.run(mine);
         }
     }
     
     @Override
     public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
         if(!command.getName().equalsIgnoreCase("mine")) return false;
 
         CommandManager.setSender(sender);
         
         if(args.length == 0) { CommandManager.HELP.run(""); return true; }
         
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
         
         for(Mine mine : mines) {
             if(!mine.getAutomaticReset()) continue;
             for(TimedTask task : PrisonSuite.getLocalTasks()) {
                 if(task.getName().endsWith(mine.getId())) task.cancel();
             }
         }
         
         signTask.cancel();
         
         Message.log("Plugin stopped");
     }
     
     public void reloadLanguageData() {
         String lang = settings.LANGUAGE;
         if(lang == null) lang = "english";
         lang = lang + ".yml";
         
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
     
     /**
      * Returns the plugin version
      */
     @Override
     public double getVersion() {
         return settings.PLUGIN_VERSION;
     }
     
     /**
      * Returns the plugin instance
      * @return PrisonMine instance
      */
     public static PrisonMine getInstance() {
         return instance;
     }
     
     /**
      * Returns a PrisonSuite instance
      * @return PrisonSuite instance
      */
     public static PrisonSuite getPrisonSuite() {
         return prisonSuite;
     }
     
     /**
      * Returns the plugin settings
      * @return Plugin settings
      */
     public static Settings getSettings() {
         return settings;
     }
     
     /**
      * Returns the plugin language strings
      * @return Language strings
      */
     public static Language getLanguage() {
         return language;
     }
     
     /**
      * Returns the selected mine for the specified CommandSender
      * @param sender Command sender
      * @return Selected mine, or <b>null</b> if there isn't one
      */
     public static Mine getCurMine(CommandSender sender) {
         return curMines.get(sender);
     }
     
     /**
      * Returns the selected mine for the current command sender.<br />
      * This method is only safe to use inside a command class.
      * @return Selected mine, or <b>null</b> if there isn't one
      */
     public static Mine getCurMine() {
         return getCurMine(CommandManager.getSender());
     }
     
     /**
      * Sets the active mine for the current CommandSender
      * @param mine Mine to mark as selected
      */
     public static void setCurMine(Mine mine) {
         setCurMine(CommandManager.getSender(), mine);
     }
     
     /**
      * Sets the active mine for the specified sender
      * @param sender Command sender
      * @param mine Mine to mark as the selected
      */
     public static void setCurMine(CommandSender sender, Mine mine) {
         if(curMines.get(sender) != null) curMines.remove(sender);
         if(mine != null) curMines.put(sender, mine);
     }
     
     /**
      * Reloads plugin settings from file
      */
     public void reloadSettings() {
         settings = null;
         settings = new Settings(this);
     }
     
     /**
      * Reloads plugin language strings from file
      */
     public void reloadLanguage() {
         language = null;
         language = new Language(this);
     }
     
     /**
      * Returns a list all existing mines
      * @return List of mines
      */
     public static List<Mine> getStaticMines() { 
         List<Mine> temp = new ArrayList<Mine>();
         for(Mine mine : mines) temp.add(mine);
         return temp;
     }
     
     /**
      * Dumps the existing mines and replaces them with the specified ones
      * @param newMines New mines
      */
     public static void setMines(List<Mine> newMines) {
         mines.clear();
         for(Mine mine : newMines) mines.add(mine);
     }
     
     /**
      * Adds a new mine to the list
      * @param mine Mine to add
      */
     public static void addMine(Mine mine) {
         mines.add(mine);
     }
     
     /**
      * Adds a list of mines
      * @param newMines Mines to add
      */
     public static void addMine(List<Mine> newMines) {
         for(Mine mine : newMines) mines.add(mine);
     }
     
     /**
      * Removes a mine from the list
      * @param mine Mine to remove
      */
     public static void removeMine (Mine mine) {
         mines.remove(mine);
     }
     
     /**
      * Returns the list of all existing display signs
      * @return List of existing signs
      */
     public static List<DisplaySign> getStaticSigns() { 
         List<DisplaySign> temp = new ArrayList<DisplaySign>();
         for(DisplaySign sign : signs) temp.add(sign);
         return temp;
     }
     
     /**
      * Dumps the existing signs and replaces them with the list
      * @param newSigns List of new signs
      */
     public static void setSigns(List<DisplaySign> newSigns) {
         signs.clear();
         for(DisplaySign sign : newSigns) signs.add(sign);
     }
     
     /**
      * Adds a sign to the 
      * @param sign Sign to add
      */
     public static void addSign(DisplaySign sign) {
         signs.add(sign);
     }
     
     /**
      * Adds a list of signs
      * @param newSigns Signs to add
      */
     public static void addSign(List<DisplaySign> newSigns) {
         for(DisplaySign sign : newSigns) signs.add(sign);
     }
     
     /**
      * Removes a sign from the list
      * @param sign Sign to remove
      */
     public static void removeSign (DisplaySign sign) {
         signs.remove(sign);
     }
 }
