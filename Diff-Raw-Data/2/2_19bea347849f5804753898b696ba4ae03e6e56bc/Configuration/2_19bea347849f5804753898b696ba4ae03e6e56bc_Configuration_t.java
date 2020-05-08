 /************************************************************************
  * This file is part of CreativePlus.
  *
  * CreativePlus is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * ExamplePlugin is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with CreativePlus.  If not, see <http://www.gnu.org/licenses/>.
  ************************************************************************/
 
 package de.Lathanael.CP.CreativePlus;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import be.Balor.Tools.Configuration.File.ExtendedConfiguration;
 import be.Balor.bukkit.AdminCmd.ACPluginManager;
 
 /**
  * @authors Lathanael, Balor
  *
  */
 public class Configuration {
 
 	private ExtendedConfiguration pluginConfig;
 	private static Configuration instance = null;
 
 	public static Configuration getInstance() {
 		if (instance == null)
 			instance = new Configuration();
 		return instance;
 	}
 
 	/**
 	 * @param pluginInstance
 	 *            the pluginInstance to set
 	 */
 	public void setInstance(CreativePlus instance) {
 		String directory = instance.getDataFolder().getPath();
 		File file = createConfFile(directory, "config.yml");
 		pluginConfig = ExtendedConfiguration.loadConfiguration(file);
 		pluginConfig.add("CreativeWorlds", Arrays.asList("Creative1", "Creative2"));
 		pluginConfig.add("PlayersCanDropItems", false);
 		pluginConfig.add("BlockBreakBlacklist", Arrays.asList(7, 54, 63, 64, 68, 69, 71, 77, 93, 94, 96));
 		pluginConfig.add("BlockPlaceBlacklist", Arrays.asList(7, 8, 9, 10, 11));
 		try {
 			pluginConfig.save();
 		} catch(IOException exception) {
 			exception.printStackTrace();
 		}
 	}
 
 	/**
 	 * Get boolean from config
 	 *
 	 * @param path
 	 * @return
 	 */
 	public boolean getConfBoolean(String path) {
 		return pluginConfig.getBoolean(path, false);
 	}
 
 	/**
 	 * Get float parameter of config file.
 	 *
 	 * @param path
 	 * @return
 	 */
 	public Float getConfFloat(String path) {
 		return Float.parseFloat(pluginConfig.getString(path));
 	}
 
 	/**
 	 * Get Integer parameter from config.
 	 *
 	 * @param path
 	 * @return
 	 */
 	public Integer getConfInt(String path) {
 		return pluginConfig.getInt(path, 0);
 	}
 
 	/**
 	 * Get String parameter from config.
 	 *
 	 * @param path
 	 * @return
 	 */
 	public String getConfString(String path) {
 		return pluginConfig.getString(path, "");
 	}
 
 	/**
 	 * Get List<Integer> from config.
 	 *
 	 * @param path
 	 * @return
 	 */
 	public List<Integer> getConfIntList(String path) {
 		return pluginConfig.getIntList(path, new ArrayList<Integer>());
 	}
 
 	/**
 	 * Get List<String> from config.
 	 *
 	 * @param path
 	 * @return
 	 */
 	public List<String> getConfStringList(String path) {
 		return pluginConfig.getStringList(path, new ArrayList<String>());
 	}
 
 	/**
 	 * Sets the property of a configuration node
 	 *
 	 * @param path
 	 *             The node to be set
 	 * @param obj
 	 *             The value of the node
 	 */
 	public void setConfProperty(String path, Object obj) {
 		pluginConfig.set(path, obj);
 		try {
 			pluginConfig.save();
 		} catch(IOException exception) {
 			exception.printStackTrace();
 		}
 	}
 
 	/**
 	 *
 	 */
 	private File createConfFile(String dir, String name) {
 		File file = new File(dir + File.separator + name);
 		new File(dir).mkdir();
 		if (!file.exists()){
 			try {
 				file.createNewFile();
 			}
 			catch (IOException ex){
 				ACPluginManager.getPluginInstance("CreativePlus").getLogger()
					.info("Could not create file: " + name);
 			}
 		}
 		return file;
 	}
 }
