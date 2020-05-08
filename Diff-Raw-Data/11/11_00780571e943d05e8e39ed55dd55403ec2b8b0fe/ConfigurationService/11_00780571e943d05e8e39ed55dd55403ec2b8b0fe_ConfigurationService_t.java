 /**
  * LICENSING
  * 
  * This software is copyright by sunkid <sunkid@iminurnetz.com> and is
  * distributed under a dual license:
  * 
  * Non-Commercial Use:
  *    This program is free software: you can redistribute it and/or modify
  *    it under the terms of the GNU General Public License as published by
  *    the Free Software Foundation, either version 3 of the License, or
  *    (at your option) any later version.
  *
  *    This program is distributed in the hope that it will be useful,
  *    but WITHOUT ANY WARRANTY; without even the implied warranty of
  *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *    GNU General Public License for more details.
  *
  *    You should have received a copy of the GNU General Public License
  *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
  * 
  * Commercial Use:
  *    Please contact sunkid@iminurnetz.com
  */
 package com.iminurnetz.bukkit.plugin.util;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.URL;
 import java.util.logging.Level;
 
 import org.bukkit.util.config.Configuration;
 import org.bukkit.util.config.ConfigurationNode;
 
 import com.iminurnetz.bukkit.plugin.BukkitPlugin;
 
 public class ConfigurationService {
 
 	private static final String CONFIG_FILE = "config.yml";
 	
 	private final BukkitPlugin plugin;
 	private final String lastChangedInVersion;
 
 	// global settings
 	public static final String SETTINGS_TAG = "settings";
 	public static final String USER_SETTINGS_TAG = SETTINGS_TAG + ".user";
 		
 	public static final boolean ALLOW_OPS = true;
 	public static final boolean IS_ENABLED = false;
 	public static final boolean DEBUG = false;
 
 	public ConfigurationService(BukkitPlugin plugin, String lastChangedInVersion) {
 		this.plugin = plugin;
 		this.lastChangedInVersion = lastChangedInVersion;
 		load();
 	}
 
 	/**
 	 * @return the plugin
 	 */
 	public BukkitPlugin getPlugin() {
 		return plugin;
 	}
 
 	/**
 	 * @return the lastChangedInVersion
 	 */
 	public String getLastChangedInVersion() {
 		return lastChangedInVersion;
 	}
 
 	protected void load() {
 		File dir = getPlugin().getDataFolder();
 		
 		Configuration config = getPlugin().getConfiguration();
 		
 		// we are relying on a value that is always set
 		ConfigurationNode settings = config.getNode(SETTINGS_TAG);
 		
 		if (settings == null) {
 			dir.mkdirs();
 			generateDefaultConfig(dir);
 		} else // check if there was an update to the config file/parameters
 		if (!config.getString(SETTINGS_TAG + ".version", "").equals(getLastChangedInVersion())) {
 			getPlugin().log(Level.WARNING, "Your configuration file is outdated, please read config-new.yml");
 			URL shipped = getClass().getResource(CONFIG_FILE);
 			byte[] buf = new byte[1024];
 			int len;
 			try {
 				InputStream in = shipped.openStream();
 				OutputStream out = new FileOutputStream(new File(dir, "config-new.yml"));
 				while ((len = in.read(buf)) > 0) {
 					out.write(buf, 0, len);
 				}
 				in.close();
 				out.close();
 			} catch (IOException e) {
 				getPlugin().getLogger().log(Level.SEVERE, "Cannot generate config-new.file file", e);
 			}
 		}
 	}
 
 	private void generateDefaultConfig(File dir) {
 		// use the default configuration file shipped with the jar
 		URL defCon = getClass().getResource("/" + CONFIG_FILE);
 		byte[] buf = new byte[1024];
 		int len;
 		try {
 			InputStream in = defCon.openStream();
 			OutputStream out = new FileOutputStream(new File(getPlugin().getDataFolder(), CONFIG_FILE));
 			while ((len = in.read(buf)) > 0) {
 				out.write(buf, 0, len);
 			}
 			in.close();
 			out.close();
 			
 		} catch (IOException e) {
 			plugin.getLogger().log(Level.SEVERE, "Cannot generate config file", e);
 		}	
 		getPlugin().getConfiguration().load();
 	}
 
 	public boolean isEnabled() {
 		return getSettingsAsBoolean("enabled", IS_ENABLED);
 	}
 
 	public boolean isDebug() {
 		return getUserSettingsAsBoolean("debug", DEBUG);
 	}
 	
 	public boolean getSettingsAsBoolean(String setting) {
 		return getSettingsAsBoolean(setting, false);
 	}
 	
 	public boolean getSettingsAsBoolean(String setting, boolean defaultBool) {
		return getPlugin().getConfiguration().getBoolean(SETTINGS_TAG + "." + setting, defaultBool);
 	}
 
 	public boolean getUserSettingsAsBoolean(String setting) {
 		return getUserSettingsAsBoolean(setting, false);
 	}
 	
 	public boolean getUserSettingsAsBoolean(String setting, boolean defaultBool) {
		return getPlugin().getConfiguration().getBoolean(USER_SETTINGS_TAG + "." + setting, defaultBool);
 	}
 
 	public int getSettingsAsInt(String setting) {
 		return getSettingsAsInt(setting, 0);
 	}
 	
 	public int getSettingsAsInt(String setting, int defaultInt) {
		return getPlugin().getConfiguration().getInt(SETTINGS_TAG + "." + setting, defaultInt);
 	}
 
 	public int getUserSettingsAsInt(String setting) {
 		return getUserSettingsAsInt(setting, 0);
 	}
 	
 	public int getUserSettingsAsInt(String setting, int defaultInt) {
		return getPlugin().getConfiguration().getInt(USER_SETTINGS_TAG + "." + setting, defaultInt);
 	}
 
 	public String getSettings(String setting) {
 		return getSettings(setting, "");
 	}
 
 	public String getSettings(String setting, String defaultString) {
 		return getPlugin().getConfiguration().getString(SETTINGS_TAG + "." + setting, defaultString);
 	}
 }
