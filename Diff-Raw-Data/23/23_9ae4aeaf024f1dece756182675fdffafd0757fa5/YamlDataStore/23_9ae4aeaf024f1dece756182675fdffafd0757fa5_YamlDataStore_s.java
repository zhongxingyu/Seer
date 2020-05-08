 /* Copyright 2013 Kevin Seiden. All rights reserved.
 
  This works is licensed under the Creative Commons Attribution-NonCommercial 3.0
 
  You are Free to:
     to Share: to copy, distribute and transmit the work
     to Remix: to adapt the work
 
  Under the following conditions:
     Attribution: You must attribute the work in the manner specified by the author (but not in any way that suggests that they endorse you or your use of the work).
     Non-commercial: You may not use this work for commercial purposes.
 
  With the understanding that:
     Waiver: Any of the above conditions can be waived if you get permission from the copyright holder.
     Public Domain: Where the work or any of its elements is in the public domain under applicable law, that status is in no way affected by the license.
     Other Rights: In no way are any of the following rights affected by the license:
         Your fair dealing or fair use rights, or other applicable copyright exceptions and limitations;
         The author's moral rights;
         Rights other persons may have either in the work itself or in how the work is used, such as publicity or privacy rights.
 
  Notice: For any reuse or distribution, you must make clear to others the license terms of this work. The best way to do this is with a link to this web page.
  http://creativecommons.org/licenses/by-nc/3.0/
  */
 
 package io.github.alshain01.flags.data;
 
 import io.github.alshain01.flags.*;
 import io.github.alshain01.flags.System;
 import io.github.alshain01.flags.area.Area;
 import io.github.alshain01.flags.area.Default;
 import io.github.alshain01.flags.area.Subdivision;
 import io.github.alshain01.flags.area.World;
 import io.github.alshain01.flags.economy.EPurchaseType;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import me.ryanhamshire.GriefPrevention.GriefPrevention;
 
 import org.bukkit.Bukkit;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.bekvon.bukkit.residence.Residence;
 
 /**
  * Class for managing YAML Database Storage
  */
 public final class YamlDataStore implements DataStore {
 	private static CustomYML data;
 	private static CustomYML def;
 	private static CustomYML world;
 	private static CustomYML bundle;
 	private static CustomYML price;
 
     /*
      * Constructor
      */
 	public YamlDataStore(JavaPlugin plugin) {
 		def = new CustomYML(plugin, "default.yml");
 		world = new CustomYML(plugin, "world.yml");
 		data = new CustomYML(plugin, "data.yml");
 		bundle = new CustomYML(plugin, "bundle.yml");
 		price = new CustomYML(plugin, "price.yml");
 		price.saveDefaultConfig();
 		bundle.saveDefaultConfig();
 	}
 
     /*
      * Interface Methods
      */
     @Override
     public boolean create(JavaPlugin plugin) {
         // Don't change the version here, not needed (will change in update)
         if (!exists(plugin)) {
             writeVersion(new DBVersion(1, 0, 0));
         }
         return true;
     }
 
     @Override
     public boolean reload() {
         data.reload();
         def.reload();
         world.reload();
         bundle.reload();
         price.reload();
         return true;
     }
 
     @Override
     public DBVersion readVersion() {
         final String path = "Default.Database.Version";
         final FileConfiguration cYml = getYml(path).getConfig();
         if (!cYml.isSet("Default.Database.Version")) {
             return new DBVersion(0, 0, 0);
         }
         final String[] ver = cYml.getString("Default.Database.Version").split("\\.");
         return new DBVersion(Integer.valueOf(ver[0]), Integer.valueOf(ver[1]),
                 Integer.valueOf(ver[2]));
     }
 
     @Override
     public DataStoreType getType() {
         return DataStoreType.YAML;
     }
 
 
     @Override
     public void update(JavaPlugin plugin) {
         final DBVersion ver = readVersion();
         if (ver.major <= 1 && ver.minor <= 2 && ver.build < 2) {
             CustomYML cYml = getYml("data");
             ConfigurationSection cSec;
             System system = System.getActive();
             if(cYml.getConfig().isConfigurationSection(System.getActive().toString() + "Data")) {
                 if (system == System.GRIEF_PREVENTION || System.getActive() == System.RESIDENCE) {
                     cSec = cYml.getConfig().getConfigurationSection(System.getActive().toString() + "Data");
 
                     Flags.debug("Updating World Names " + cSec.getCurrentPath());
                     final Set<String> keys = cSec.getKeys(true);
                     for (final String k : keys) {
 
                         if (k.contains("Value") || k.contains("Message")
                                 || k.contains("Trust") || k.contains("InheritParent")) {
 
                             final String id = k.split("\\.")[0];
                             String world;
 
                             if (System.getActive() == System.GRIEF_PREVENTION) {
                                 world = GriefPrevention.instance.dataStore
                                         .getClaim(Long.valueOf(id))
                                         .getGreaterBoundaryCorner().getWorld()
                                         .getName();
                             } else {
                                 world = Residence.getResidenceManager()
                                         .getByName(id).getWorld();
                             }
 
                             cSec.set(world + "." + k, cSec.get(k));
                         }
                         cYml.saveConfig();
                     }
                     // Remove the old
                     for (final String k : keys) {
                         if (k.split("\\.").length == 1
                                 && Bukkit.getWorld(k.split("\\.")[0]) == null) {
                             cSec.set(k, null);
                             cYml.saveConfig();
                         }
                     }
 
                 }
 
                 //Convert values to boolean instead of string
                 final String[] fileArray = {"data", "default", "world"};
                 for(final String s : fileArray) {
                     cYml = getYml(s);
                     Flags.debug("Updating Boolean " + cYml.getConfig().getCurrentPath());
                     Set<String> keys = cYml.getConfig().getKeys(true);
                     for (final String k : keys) {
                         if (k.contains("Value") || k.contains("InheritParent")) {
                             cYml.getConfig().set(k,	Boolean.valueOf(cYml.getConfig().getString(k)));
                             cYml.saveConfig();
                         }
                     }
                 }
 
                 //Remove "Data" from the root heading.
                 Flags.debug(System.getActive().toString() + "Data");
                 cYml = getYml("data");
                 cSec = getYml("data").getConfig().getConfigurationSection(System.getActive().toString() + "Data");
                 getYml("data").getConfig().createSection(System.getActive().toString());
                 ConfigurationSection newCSec = getYml("data").getConfig().getConfigurationSection(System.getActive().toString());
                 Flags.debug("Moving " + cSec.getCurrentPath() + " to " + newCSec.getCurrentPath());
                 Set<String> keys = cSec.getKeys(true);
                 for (final String k : keys) {
                     if (k.contains("Value") || k.contains("Message")
                             || k.contains("Trust") || k.contains("InheritParent")) {
                         Flags.debug("Moving " + k);
                         newCSec.set(k , cSec.get(k));
                         cSec.set(k, null);
                         cYml.saveConfig();
                     }
                 }
                 cYml.getConfig().set(io.github.alshain01.flags.System.getActive().toString() + "Data", null);
                 cYml.saveConfig();
             }
             writeVersion(new DBVersion(1, 2, 2));
         }
     }
 
     @Override
     public final Set<String> readBundles() {
         return getYml("Bundle").getConfig().getConfigurationSection("Bundle").getKeys(false);
     }
 
     @Override
 	public final Set<Flag> readBundle(String bundle) {
 		final HashSet<Flag> flags = new HashSet<Flag>();
 		final List<?> list = getYml("Bundle").getConfig().getList("Bundle." + bundle, new ArrayList<String>());
 		
 		for (final Object o : list) {
 			if (Flags.getRegistrar().isFlag((String) o)) {
 				flags.add(Flags.getRegistrar().getFlag((String) o));
 			}
 		}
 		return flags;
 	}
 
     @Override
     public final void writeBundle(String name, Set<Flag> flags) {
         final String path = "Bundle." + name;
         final CustomYML cYml = getYml(path);
         if (flags == null || flags.size() == 0) {
             deleteBundle(name);
             return;
         }
 
         final List<String> list = new ArrayList<String>();
         for (final Flag f : flags) {
             list.add(f.getName());
         }
 
         cYml.getConfig().set(path, list);
         cYml.saveConfig();
     }
 
 	@Override
 	public Boolean readFlag(Area area, Flag flag) {
 		final String path = getAreaPath(area) + "." + flag.getName() + ".Value";
 
 		final FileConfiguration cYml = getYml(path).getConfig();
 		return cYml.isSet(path) ? cYml.getBoolean(path) : null;
 	}
 
     @Override
     public void writeFlag(Area area, Flag flag, Boolean value) {
         final String path = getAreaPath(area) + "." + flag.getName() + ".Value";
         final CustomYML cYml = getYml(path);
 
         if (value == null) {
             cYml.getConfig().set(path, null);
         } else {
             cYml.getConfig().set(path, value);
         }
         cYml.saveConfig();
     }
 
 	@Override
 	public String readMessage(Area area, Flag flag) {
 		final String path = getAreaPath(area) + "." + flag.getName() + ".Message";
 		return getYml(path).getConfig().getString(path);
 	}
 
     @Override
     public void writeMessage(Area area, Flag flag, String message) {
         final String path = getAreaPath(area) + "." + flag.getName()
                 + ".Message";
         final CustomYML cYml = getYml(path);
         cYml.getConfig().set(path, message);
         cYml.saveConfig();
     }
 
 	@Override
 	public double readPrice(Flag flag, EPurchaseType type) {
 		final String path = "Price." + type.toString() + "." + flag.getName();
 		return getYml(path).getConfig().isSet(path) ? getYml(path).getConfig().getDouble(path) : 0;
 	}
 
     @Override
     public void writePrice(Flag flag, EPurchaseType type, double price) {
         final String path = "Price." + type.toString() + "." + flag.getName();
         final CustomYML cYml = getYml(path);
         cYml.getConfig().set(path, price);
         cYml.saveConfig();
     }
 
 	@Override
 	public Set<String> readTrust(Area area, Flag flag) {
 		final String path = getAreaPath(area) + "." + flag.getName() + ".Trust";
 		final List<?> setData = getYml(path).getConfig().getList(path, new ArrayList<String>());
 		final Set<String> stringData = new HashSet<String>();
 		
 		for (final Object o : setData) {
 			stringData.add((String) o);
 		}
 		return stringData;
 	}
 
     @Override
     public Set<String> readPlayerTrust(Area area, Flag flag) {
         final String path = getAreaPath(area) + "." + flag.getName() + ".Trust";
         final List<?> setData = getYml(path).getConfig().getList(path, new ArrayList<String>());
         final Set<String> stringData = new HashSet<String>();
 
         for (final Object o : setData) {
             if(!((String)o).contains(".")) {
                 stringData.add((String) o);
             }
         }
         return stringData;
     }
 
     @Override
     public Set<String> readPermissionTrust(Area area, Flag flag) {
         final String path = getAreaPath(area) + "." + flag.getName() + ".Trust";
         final List<?> setData = getYml(path).getConfig().getList(path, new ArrayList<String>());
         final Set<String> stringData = new HashSet<String>();
 
         for (final Object o : setData) {
             if(((String)o).contains(".")) {
                 stringData.add((String) o);
             }
         }
         return stringData;
     }
 
     @Override
     public void writeTrust(Area area, Flag flag, Set<String> players) {
         final String path = getAreaPath(area) + "." + flag.getName() + ".Trust";
         final CustomYML cYml = getYml(path);
 
         final List<String> list = new ArrayList<String>();
         for (final String s : players) {
             list.add(s);
         }
 
         cYml.getConfig().set(path, list);
         cYml.saveConfig();
     }
 
     @Override
     public boolean readInheritance(Area area) {
         if (!(area instanceof Subdivision) || !((Subdivision)area).isSubdivision()) {
             return true;
         }
 
         final String path = area.getSystemType().toString() + "." + area.getWorld().getName() + "." + area.getSystemID() + "."	+ ((Subdivision) area).getSystemSubID() + ".InheritParent";
 
         final FileConfiguration cYml = getYml(path).getConfig();
         return !cYml.isSet(path) || cYml.getBoolean(path);
     }
 
     @Override
     public void writeInheritance(Area area, boolean value) {
         if (!(area instanceof Subdivision) || !((Subdivision) area).isSubdivision()) {
             return;
         }
 
         final String path = area.getSystemType().toString() + "." + area.getWorld().getName() + "." + area.getSystemID() + "."	+ ((Subdivision) area).getSystemSubID() + ".InheritParent";
 
         final CustomYML cYml = getYml(path);
 
         cYml.getConfig().set(path, value);
         cYml.saveConfig();
     }
 
 	@Override
 	public void remove(Area area) {
		final String path = getAreaPath(area);
		getYml(path).getConfig().set(path, null);
 	}
 
     /*
      * Used in SQL Import/Export
      */
     protected Set<String> getAreaIDs() {
         ConfigurationSection cSec = getYml("data").getConfig().getConfigurationSection(System.getActive().toString() + "Data");
         if(cSec == null) { return new HashSet<String>(); }
         return cSec.getKeys(false);
     }
 
     protected Set<String> getAreaSubIDs(String id) {
         ConfigurationSection cSec = getYml("data").getConfig().getConfigurationSection(System.getActive().toString() + "Data." + id);
         Set<String> subIDs = new HashSet<String>();
         if(cSec == null) { return subIDs; }
 
         for(String i : cSec.getKeys(true)) {
             String[] nodes = i.split("\\.");
             if(nodes.length == 4) { // All keys for the parent should have 3 nodes.
                 subIDs.add(nodes[0]);
             }
         }
         return subIDs;
     }
 
     protected Set<String> readKeys() {
         return data.getConfig().getKeys(true);
     }
 
     protected Boolean getBoolean(String dataLocation) {
         if(data.getConfig().isBoolean(dataLocation)) {
             return data.getConfig().getBoolean(dataLocation);
         }
         return null;
     }
 
     protected String getString(String dataLocation) {
         return data.getConfig().getString(dataLocation);
     }
 
     protected List<?> getList(String dataLocation) {
         return data.getConfig().getList(dataLocation);
     }
 
     /*
      * Private
      */
     private void writeVersion(DBVersion version) {
         final String path = "Default.Database.Version";
         final CustomYML cYml = getYml(path);
         cYml.getConfig().set("Default.Database.Version",
                 version.major + "." + version.minor + "." + version.build);
         cYml.saveConfig();
     }
 
     private void deleteBundle(String name) {
         final String path = "Bundle." + name;
         final CustomYML cYml = getYml(path);
         cYml.getConfig().set("Bundle." + name, null);
         cYml.saveConfig();
     }
 
     private boolean exists(JavaPlugin plugin) {
         final File fileObject = new File(plugin.getDataFolder(), "default.yml");
         return fileObject.exists();
     }
 
     private String getAreaPath(Area area) {
         String path = area.getSystemType().toString() + "." + area.getWorld().getName();
 
         if (!(area instanceof World || area instanceof Default)) {
             path += "." + area.getSystemID();
         }
 
         if (area instanceof Subdivision && !readInheritance(area)) {
             path += "." + ((Subdivision) area).getSystemSubID();
         }
 
         return path;
     }
 
     private CustomYML getYml(String path) {
         final String[] pathList = path.split("\\.");
 
         if (pathList[0].equalsIgnoreCase("world")) {
             return world;
         } else if (pathList[0].equalsIgnoreCase("default")) {
             return def;
         } else if (pathList[0].equalsIgnoreCase("bundle")) {
             return bundle;
         } else if (pathList[0].equalsIgnoreCase("price")) {
             return price;
         } else {
             return data;
         }
     }
 }
