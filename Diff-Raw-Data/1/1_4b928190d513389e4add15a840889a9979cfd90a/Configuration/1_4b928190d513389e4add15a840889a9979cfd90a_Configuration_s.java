 /************************************************************************
  * This file is part of FunCommands.
  *
  * FunCommands is free software: you can redistribute it and/or modify
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
  * along with FunCommands.  If not, see <http://www.gnu.org/licenses/>.
  ************************************************************************/
 
 package de.Lathanael.FunCommands;
 
 import java.io.File;
 import java.io.IOException;
 
 import be.Balor.Tools.ACLogger;
 import be.Balor.Tools.Configuration.ExtendedConfiguration;
 
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
 	public void setInstance(FunCommands instnace) {
 		String directory = instnace.getDataFolder().getPath();
 		File file = createConfFile(directory, "config.yml");
 		pluginConfig = new ExtendedConfiguration(file);
 		pluginConfig.load();
 		pluginConfig.addProperty("Slap.normalPower", 1.1);
 		pluginConfig.addProperty("Slap.normalHeight", 0);
 		pluginConfig.addProperty("Slap.hPower", 2);
 		pluginConfig.addProperty("Slap.hHeight", 0.5);
 		pluginConfig.addProperty("Slap.vPower", 3);
 		pluginConfig.addProperty("Slap.vHeight", 1);
 		pluginConfig.addProperty("Rocket.normalPower", 1.5);
 		pluginConfig.addProperty("Rocket.flagPower", 5);
 		pluginConfig.save();
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
 	 * Sets the property of a configuration node
 	 *
 	 * @param path
 	 *             The node to be set
 	 * @param obj
 	 *             The value of the node
 	 */
 	public void setConfProperty(String path, Object obj) {
 		pluginConfig.setProperty(path, obj);
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
 				ACLogger.info("[FunCommands] Could not create file: " + name);
 			}
 		}
 		return file;
 	}
 }
