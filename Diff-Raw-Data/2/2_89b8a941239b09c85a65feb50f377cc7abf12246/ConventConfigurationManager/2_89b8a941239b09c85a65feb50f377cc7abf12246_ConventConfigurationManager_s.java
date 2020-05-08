 /*
  * Copyright (c) 2013. ToppleTheNun
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
  * documentation files (the "Software"), to deal in the Software without restriction,
  * including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
  * and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so,
  * subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in all copies or substantial portions of
  * the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
  * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
  * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
  * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
  */
 
 package com.conventnunnery.libraries.config;
 
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.plugin.Plugin;
 
 import java.io.File;
 import java.io.IOException;
 
 public class ConventConfigurationManager {
 
 	private final Plugin plugin;
 
 	/**
 	 * Instantiates a new version of the configuration manager for a plugin
 	 *
 	 * @param plugin Plugin that is using the manager
 	 */
 	public ConventConfigurationManager(Plugin plugin) {
 		this.plugin = plugin;
 	}
 
 	public void unpackConfigurationFiles(String... configurationFiles) {
 		unpackConfigurationFiles(configurationFiles, false);
 	}
 
 	public void unpackConfigurationFiles(String[] configurationFiles, boolean overwrite) {
 		for (String s : configurationFiles) {
 			YamlConfiguration yc = YamlConfiguration.loadConfiguration(plugin.getResource(s));
 			try {
 				File f = new File(plugin.getDataFolder(), s);
 				if (!f.exists()) {
 					yc.save(new File(plugin.getDataFolder(), s));
 					continue;
 				}
 				if (overwrite) {
 					yc.save(new File(plugin.getDataFolder(), s));
 				}
 			} catch (IOException e) {
 				plugin.getLogger().warning("Could not unpack " + s);
 			}
 		}
 	}
 
 	public ConventConfigurationGroup getConventConfigurationGroup(File directory) throws IllegalArgumentException {
 		if (directory == null) {
			throw new IllegalArgumentException(directory.getPath() + " cannot be null");
 		}
 		if (!directory.exists() && !directory.getParentFile().mkdirs()) {
 			throw new IllegalArgumentException(directory.getPath() + " does not exist and cannot be made");
 		}
 		if (!directory.isDirectory()) {
 			throw new IllegalArgumentException(directory.getPath() + " must be a directory");
 		}
 		ConventConfigurationGroup ccg = new ConventConfigurationGroup();
 		for (File file : directory.listFiles()) {
 			ConventConfiguration c = getConventConfiguration(file);
 			if (c != null) {
 				ccg.addConventConfiguration(c);
 			}
 		}
 		return ccg;
 	}
 
 	public ConventConfiguration getConventConfiguration(File file) throws IllegalArgumentException {
 		if (file == null) {
 			throw new IllegalArgumentException("File cannot be null");
 		}
 
 		ConventConfiguration c = null;
 		if (file.getName().endsWith(".yml")) {
 			if (!file.exists()) {
 				new ConventYamlConfiguration(plugin, file).saveDefaults(plugin.getResource(file.getName()));
 			}
 			c = new ConventYamlConfiguration(plugin, file);
 		}
 		return c;
 	}
 
 }
