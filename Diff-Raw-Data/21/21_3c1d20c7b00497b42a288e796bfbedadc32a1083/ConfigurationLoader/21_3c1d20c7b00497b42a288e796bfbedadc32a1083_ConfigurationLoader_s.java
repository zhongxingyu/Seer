 /*
  *  ExperienceMod - Bukkit server plugin for modifying the experience system in Minecraft.
  *  Copyright (C) 2012 Kristian S. Stangeland
  *
  *  This program is free software; you can redistribute it and/or modify it under the terms of the 
  *  GNU General Public License as published by the Free Software Foundation; either version 2 of 
  *  the License, or (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
  *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
  *  See the GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License along with this program; 
  *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 
  *  02111-1307 USA
  */
 
 package com.comphenix.xp;
 
 import java.io.File;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.configuration.file.YamlConfiguration;
 
 import com.comphenix.xp.messages.ChannelProvider;
 import com.comphenix.xp.parser.text.ItemNameParser;
 import com.comphenix.xp.parser.text.ItemParser;
 import com.comphenix.xp.parser.text.MobParser;
 import com.comphenix.xp.rewards.RewardProvider;
 
 public class ConfigurationLoader {
 
 	// Cache of configurations
 	private Map<File, Configuration> configurationFiles;
 	
 	private File rootPath;
 	private Debugger logger;
 	private RewardProvider rewardProvider;
 	private ChannelProvider channelProvider;
 	
 	// Registry of action types
 	private ActionTypes actionTypes = ActionTypes.Default();
 	
 	private ItemNameParser nameParser = new ItemNameParser();
 	private ItemParser itemParser = new ItemParser(nameParser);
 	private MobParser mobParser = new MobParser();
 	
 	public ConfigurationLoader(File rootPath, Debugger logger, RewardProvider rewardProvider, ChannelProvider channelProvider) {
 		this.rootPath = rootPath;
 		this.logger = logger;
 		this.rewardProvider = rewardProvider;
 		this.channelProvider = channelProvider;
 		this.configurationFiles = new HashMap<File, Configuration>();
 	}
 	
 	public void clearCache() {
 		configurationFiles.clear();
 	}
 	
 	/**
 	 * Retrieves the parser responsible for parsing item queries.
 	 * @return The current item query parser.
 	 */
 	public ItemParser getItemParser() {
 		return itemParser;
 	}
 
 	/**
 	 * Sets the parser responsible for parsing item queries.
 	 * @param itemParser - the new item query parser.
 	 */
 	public void setItemParser(ItemParser itemParser) {
 		this.itemParser = itemParser;
 	}
 
 	/**
 	 * Retrieves the parser responsible for parsing mob queries.
 	 * @return The current mob query parser.
 	 */
 	public MobParser getMobParser() {
 		return mobParser;
 	}
 
 	/**
 	 * Sets the parser responsible for parsing mob queries.
	 * @param itemParser - the new mob query parser.
 	 */
 	public void setMobParser(MobParser mobParser) {
 		this.mobParser = mobParser;
 	}
 
 	/**
 	 * Retrieves the current name parser.
 	 * @return Current name parser.
 	 */
 	public ItemNameParser getNameParser() {
 		return nameParser;
 	}
 
 	/**
 	 * Sets the current name parser. Updates the item parser as well.
 	 * @param nameParser - new name parser.
 	 */
 	public void setNameParser(ItemNameParser nameParser) {
 		this.nameParser = nameParser;
 		this.itemParser = new ItemParser(nameParser);
 	}
 	
 	/**
 	 * Retrieves the current registered action types.
 	 * @return Registry of action types.
 	 */
 	public ActionTypes getActionTypes() {
 		return actionTypes;
 	}
 
 	/**
 	 * Sets the current registry of action types. This must be changed before configurations are loaded.
 	 * @param actionTypes - new action type registry.
 	 */
 	public void setActionTypes(ActionTypes actionTypes) {
 		this.actionTypes = actionTypes;
 	}
 
 	public Configuration getFromPath(String path) {
 		
 		File absolutePath = new File(rootPath, path);
 		
 		if (absolutePath.exists())
 			return loadFromFile(absolutePath);
 		else
 			return null;
 	}
 	
 	public Configuration getFromSection(ConfigurationSection data) {
 
 		Configuration config = new Configuration(logger, rewardProvider, channelProvider);
 		
 		config.setItemParser(itemParser);
 		config.setMobParser(mobParser);
 		config.setActionTypes(actionTypes);
 		config.loadFromConfig(data);
 		return config;
 	}
 	
 	private Configuration loadFromFile(File path) {
 		
 		if (!configurationFiles.containsKey(path)) {
 			
 			YamlConfiguration yaml = YamlConfiguration.loadConfiguration(path);
 			Configuration config = new Configuration(logger, rewardProvider, channelProvider);
 			
 			// Load from YAML
 			config.setItemParser(itemParser);
 			config.setMobParser(mobParser);
 			config.setActionTypes(actionTypes);
 			config.loadFromConfig(yaml);
 			
 			// Cache 
 			configurationFiles.put(path, config);
 			return config;
 			
 		} else {
 			
 			// Return previously computed value
 			return configurationFiles.get(path);
 		}
 	}
 }
