 package com.titankingdoms.nodinchan.titanchat.util.displayname;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.logging.Level;
 
 import org.bukkit.OfflinePlayer;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 
 import com.titankingdoms.nodinchan.titanchat.TitanChat;
 
 /*     Copyright (C) 2012  Nodin Chan <nodinchan@live.com>
  * 
  *     This program is free software: you can redistribute it and/or modify
  *     it under the terms of the GNU General Public License as published by
  *     the Free Software Foundation, either version 3 of the License, or
  *     (at your option) any later version.
  * 
  *     This program is distributed in the hope that it will be useful,
  *     but WITHOUT ANY WARRANTY; without even the implied warranty of
  *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *     GNU General Public License for more details.
  * 
  *     You should have received a copy of the GNU General Public License
  *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 /**
  * DisplayName - Manages the display names of Players
  * 
  * @author NodinChan
  *
  */
 public class DisplayName {
 	
 	private final TitanChat plugin;
 	
 	private File nickFile;
 	private FileConfiguration nickConfig;
 	
 	private final Map<String, OfflinePlayer> players;
 	
 	public DisplayName() {
 		this.plugin = TitanChat.getInstance();
 		this.players = new HashMap<String, OfflinePlayer>();
 	}
 	
 	/**
 	 * Gets an OfflinePlayer from its display name
 	 * 
 	 * @param name The display name of the OfflinePlayer
 	 * 
 	 * @return The OfflinePlayer if found, else null
 	 */
 	public OfflinePlayer fromDisplayName(String name) {
 		return players.get(name);
 	}
 	
 	/**
 	 * Gets the display name config
 	 * 
 	 * @return The config of display names
 	 */
 	private FileConfiguration getNickConfig() {
 		if (nickConfig == null)
 			reloadNickConfig();
 		
 		return nickConfig;
 	}
 	
 	/**
 	 * Gets the display name of the Player by name
 	 * 
 	 * @param name The name of the Player
 	 * 
 	 * @return The display name of the player if found, otherwise the name of the Player
 	 */
 	public String getDisplayName(String name) {
		String displayname = getNickConfig().getString("name");
 		return (displayname != null) ? displayname : name;
 	}
 	
 	/**
 	 * Gets the display name of the Player
 	 * 
 	 * @param player The Player
 	 * 
 	 * @return The display name of the player if found, otherwise the name of the Player
 	 */
 	public String getDisplayName(Player player) {
 		return getDisplayName(player.getName());
 	}
 	
 	public void load() {
 		ConfigurationSection section = getNickConfig().getConfigurationSection("");
 		players.clear();
 		
 		if (section != null) {
 			for (String name : section.getKeys(false))
 				players.put(getNickConfig().getString(name), plugin.getOfflinePlayer(name));
 		}
 	}
 	
 	/**
 	 * Reloads the display name config
 	 */
 	private void reloadNickConfig() {
 		if (nickFile == null)
 			nickFile = new File(plugin.getDataFolder(), "nick.yml");
 		
 		nickConfig = YamlConfiguration.loadConfiguration(nickFile);
 	}
 	
 	/**
 	 * Saves the display name config
 	 */
 	private void saveNickConfig() {
 		if (nickFile == null || nickConfig == null)
 			return;
 		
 		try { nickConfig.save(nickFile); } catch (IOException e) { plugin.log(Level.SEVERE, "Could not save config to " + nickFile); }
 	}
 	
 	public void setDisplayName(String name, String displayname) {
 		getNickConfig().set(name, displayname);
 		saveNickConfig();
 		players.put(displayname, plugin.getOfflinePlayer(name));
 	}
 	
 	/**
 	 * Sets the display name of the Player
 	 * 
 	 * @param player The Player
 	 * 
 	 * @param displayname The name to be displayed
 	 */
 	public void setDisplayName(Player player, String displayname) {
 		player.setDisplayName(displayname);
 		setDisplayName(player.getName(), displayname);
 	}
 	
 	/**
 	 * Unsets the display name of the Player
 	 * 
 	 * @param name The name of the Player
 	 */
 	public void unsetDisplayName(String name) {
 		getNickConfig().set(name, null);
 		saveNickConfig();
 	}
 	
 	/**
 	 * Unsets the display name of the Player
 	 * 
 	 * @param player The Player
 	 */
 	public void unsetDisplayName(Player player) {
 		player.setDisplayName(player.getName());
 		unsetDisplayName(player.getName());
 	}
 }
