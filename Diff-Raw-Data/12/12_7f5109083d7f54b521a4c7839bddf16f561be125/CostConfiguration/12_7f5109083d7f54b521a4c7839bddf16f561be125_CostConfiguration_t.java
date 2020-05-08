 /*
  * This file is part of Aqualock.
  *
  * Copyright (c) 2012, AlmuraDev <http://www.almuramc.com/>
  * Aqualock is licensed under the Almura Development License.
  *
  * Aqualock is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * As an exception, all classes which do not reference GPL licensed code
  * are hereby licensed under the GNU Lesser Public License, as described
  * in Almura Development License.
  *
  * Aqualock is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License,
  * the GNU Lesser Public License (for classes that fulfill the exception)
  * and the Almura Development License along with this program. If not, see
  * <http://www.gnu.org/licenses/> for the GNU General Public License and
  * the GNU Lesser Public License.
  */
 package com.almuramc.aqualock.bukkit.configuration;
 
 import java.io.File;
 import java.util.HashMap;
 import java.util.logging.Level;
 
 import com.almuramc.aqualock.bukkit.AqualockPlugin;
 import com.almuramc.aqualock.bukkit.node.CostNode;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Material;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 
 public class CostConfiguration {
 	private final FileConfiguration config;
 	private final HashMap<Material, CostNode> nodes = new HashMap<Material, CostNode>();
 	private double globalLock;
 	private double globalUnlock;
 	private double globalUse;
 
 	public CostConfiguration(File costYml) {
 		config = YamlConfiguration.loadConfiguration(costYml);
 	}
 
 	private final void construct() {
 		ConfigurationSection global = config.getConfigurationSection("global");
 		if (global == null) {
 			throw new IllegalStateException("Missing global section in your cost.yml.");
 		}
 		globalLock = global.getDouble("lock", 0.0);
 		globalUnlock = global.getDouble("unlock", 0.0);
 		globalUse = global.getDouble("use", 0.0);
 		//Now, we read in any material costs (if any)
 		ConfigurationSection material = config.getConfigurationSection("material");
 		if (material == null) {
 			return;
 		}
 		for (String key : material.getKeys(false)) {
 			if (!material.isConfigurationSection(key)) {
 				Bukkit.getLogger().log(Level.WARNING, AqualockPlugin.getPrefix() + " Found " + key + " in cost.yml but has no values specified! Skipping...");
 				continue;
 			}
 			Material keyed = Material.getMaterial(key.toUpperCase());
 			if (keyed == null) {
 				Bukkit.getLogger().log(Level.WARNING, AqualockPlugin.getPrefix() + " Found " + key + " in cost.yml but isn't a valid Minecraft material! Skipping...");
 				continue;
 			}
 			if (nodes.containsKey(keyed)) {
				Bukkit.getLogger().log(Level.WARNING, AqualockPlugin.getPrefix() + " Found " + key + " in cost.yml but is a duplicate! Overwriting...");
 			}
 			ConfigurationSection node = material.getConfigurationSection(key);
 			nodes.put(keyed, new CostNode(keyed, node.getDouble("lock", globalLock), node.getDouble("unlock", globalUnlock), node.getDouble("use", globalUse)));
 		}
 	}
 
 	public void reload() {
 		construct();
 	}
 
 	public double getLockCost(Material material) {
 		if (material == null) {
 			return globalLock;
 		}
 		CostNode node = nodes.get(material);
 		return node == null ? globalLock : node.getLock();
 	}
 
 	public double getUnlockCost(Material material) {
 		if (material == null) {
 			return globalUnlock;
 		}
 		CostNode node = nodes.get(material);
 		return node == null ? globalUnlock : node.getUnlock();
 	}
 
 	public double getUseCost(Material material) {
 		if (material == null) {
 			return globalUse;
 		}
 		CostNode node = nodes.get(material);
 		return node == null ? globalUse : node.getUse();
 	}
 }
