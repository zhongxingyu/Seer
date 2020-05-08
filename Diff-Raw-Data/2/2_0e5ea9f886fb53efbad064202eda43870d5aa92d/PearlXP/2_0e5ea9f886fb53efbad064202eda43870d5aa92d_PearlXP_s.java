 /**
  * Rewrite of the original PearlXP created by Nebual of nebtown.info in March 2012.
  * 
  * Small plugin to enable the storage of experience points in an item.
  * 
  * rewrite by: Marex, Zonta.
  * 
  * Copyright (C) 2012 belongs to their respective owners
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
 
 package info.nebtown.PearlXP;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Logger;
 
 import org.bukkit.Material;
 import org.bukkit.configuration.ConfigurationSection;
 
 public class PearlXP extends org.bukkit.plugin.java.JavaPlugin {
 
 	/**
 	 * Maximum storage capacity of a item.
 	 */
 	public static final int MAX_STORAGE = 32767; // max of a short
 	
 	private static final Logger LOGGER = Logger.getLogger("Minecraft");
 	private static final String LOGGER_PREFIX = "[PearlXP]";
 
 	/****** Configuration options ******/
 
 	private int maxLevel;
 	private int itemId;
 	private String itemName;
 	private int imbuedItem;
 	private List<String> messages;
 
 	public enum MsgKeys { 
 
 		INVENTORY_FULL("inventory_full"),
 		INFO_XP("info_xp_content"),
 		INFO_XP_EMPTY("info_xp_empty"),
 		IMBUE_XP("imbue_xp"),
 		RESTORE_XP("restore_xp");
 
 		private String key;
 
 		MsgKeys(String key) {
 			this.key = key;
 		}
 
 		public String getKey() {
 			return key;
 		}
 	}
 
 	@Override
 	public void onEnable() {
 		loadConfig();
 		new PearlXPListener(this);
 
 		logInfo("Plugin loading complete. Plugin enabled.");
 	}
 
 	@Override
 	public void onDisable() {
 		logInfo("Plugin disabled.");
 	}
 
 	/**
 	 * Load the default configuration files and set the variables accordingly
 	 */
 	public void loadConfig() {
 		ConfigurationSection msgSection;
 		String itemName;
 
 		if (getConfig().getInt("configversion", 0) < 3) {
 			saveResource("config.yml", true);
 			logInfo("New config file created, you should check if your " +
 					"configurations are correct!");
 			reloadConfig();
 		}
 
 		setMaxLevel(getConfig().getInt("max_level"));
 		setItemId(getConfig().getInt("item_id"));
 
 		// take the default item name if no config exists
 		itemName = Material.getMaterial(this.getItemId()).toString();
 		setItemName(getConfig().getString("item_name", itemName.toLowerCase()));
 
 		// no change of appearance if this config doesn't exists
 		setImbuedItem(getConfig().getInt("imbued_appearance", this.getItemId()));
 
 		// Loading custom texts
 		msgSection = getConfig().getConfigurationSection("Messages");
 		messages = new ArrayList<String>();
 
 		if (msgSection != null) {
 			for (MsgKeys key : MsgKeys.values()) {
 				messages.add(msgSection.getString(key.getKey(), null));
 			}
 		}
 
 	}
 
 	/**
	 * Log information to the console with the "[Plugin name vX.x] " prefix
 	 * @param s text to print
 	 */
 	public void logInfo(String s) {
 		LOGGER.info(String.format("%s %s", LOGGER_PREFIX, s));
 	}
 
 	/**
 	 * @return the config message text
 	 */
 	public String getMessage(MsgKeys key) {
 		String msg = null;
 		int i = 0;
 
 		for (MsgKeys k : MsgKeys.values()) {
 			if (key == k) {
 				msg = messages.get(i);
 			}
 			i += 1;
 		}
 
 		return msg;
 	}
 
 	/**
 	 * @return the maxLevel
 	 */
 	public int getMaxLevel() {
 		return maxLevel;
 	}
 
 	/**
 	 * @return the itemId
 	 */
 	public int getItemId() {
 		return itemId;
 	}
 
 	/**
 	 * @return the itemName
 	 */
 	public String getItemName() {
 		return itemName;
 	}
 
 
 	/**
 	 * @return the imbuedItem
 	 */
 	public int getImbuedItem() {
 		return imbuedItem;
 	}
 
 	/**
 	 * @param maxLevel the maxLevel to set
 	 */
 	private void setMaxLevel(int maxLevel) {
 		// check if maxLevel fits in a short (2^15 - 1)
 		if (maxLevel > MAX_STORAGE) {
 			this.maxLevel = MAX_STORAGE;
 			logInfo("WARNING: maxLevel exceeds possible limits! Please modify your config file.");
 			logInfo("Setting maxLevel to " + maxLevel);
 		} else { 
 			this.maxLevel = maxLevel;
 		}
 	}
 
 	private void setItemId(int i) {
 		this.itemId = i;
 
 	}
 
 	/**
 	 * @param itemName the itemName to set
 	 */
 	private void setItemName(String itemName) {
 		this.itemName = itemName;
 	}
 
 	/**
 	 * @param imbuedItem the imbuedItem to set
 	 */
 	private void setImbuedItem(int imbuedItem) {
 		this.imbuedItem = imbuedItem;
 	}
 
 
 }
