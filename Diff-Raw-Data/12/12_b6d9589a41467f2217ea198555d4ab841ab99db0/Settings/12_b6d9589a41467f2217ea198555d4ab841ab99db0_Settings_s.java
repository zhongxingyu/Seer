 /*
  * Copyright (C) 2012 MineStar.de 
  * 
  * This file is part of FifthElement.
  * 
  * FifthElement is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, version 3 of the License.
  * 
  * FifthElement is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with FifthElement.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package de.minestar.FifthElement.core;
 import java.io.File;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.World;
 import org.bukkit.configuration.ConfigurationSection;
 
 import de.minestar.core.MinestarCore;
 import de.minestar.core.units.MinestarGroup;
 import de.minestar.minestarlibrary.config.MinestarConfig;
 import de.minestar.minestarlibrary.utils.ConsoleUtils;
 
 public class Settings {
 
     /* VALUES */
     private static Map<MinestarGroup, Integer> maxPrivateWarps;
     private static Map<MinestarGroup, Integer> maxPublicWarps;
 
     private static int pageSize;
 
     private static int minWarpnameSize;
     private static int maxWarpnameSize;
 
     private static ChatColor warpListPublic;
     private static ChatColor warpListPrivate;
     private static ChatColor warpListOwned;
 
     private static int backPositionLimit;
 
     private static World resourceWorld;
     private static Set<String> forbiddenWarpWorlds;
 
     /* USED FOR SETTING */
     private static MinestarConfig config;
     private static File configFile;
 
     private Settings() {
 
     }
 
     public static boolean init(File dataFolder, String pluginName, String pluginVersion) {
         configFile = new File(dataFolder, "config.yml");
         try {
             // LOAD EXISTING CONFIG FILE
             if (configFile.exists())
                 config = new MinestarConfig(configFile, pluginName, pluginVersion);
             // CREATE A DEFAUL ONE
             else
                 config = MinestarConfig.copyDefault(Settings.class.getResourceAsStream("/config.yml"), configFile);
 
             loadValues();
             return true;
 
         } catch (Exception e) {
             ConsoleUtils.printException(e, Core.NAME, "Can't load the settings from " + configFile);
             return false;
         }
     }
 
     private static void loadValues() {
 
         loadMaxWarps();
 
         pageSize = config.getInt("common.pageSize");
 
         minWarpnameSize = config.getInt("warp.minNameLength");
         maxWarpnameSize = config.getInt("warp.maxNameLength");
 
         warpListOwned = ChatColor.getByChar(config.getString("warpList.ownedWarp"));
         warpListPublic = ChatColor.getByChar(config.getString("warpList.publicWarp"));
         warpListPrivate = ChatColor.getByChar(config.getString("warpList.privateWarp"));
 
         backPositionLimit = config.getInt("back.limit");
 
         loadResourceWorldSettings();
 
     }
     private static void loadResourceWorldSettings() {
         String resurceWorldName = config.getString("resourceWorld.world");
         if (resurceWorldName == null)
             ConsoleUtils.printInfo(Core.NAME, "No Ressource World found. Warps can created everywhere, Mines are disabled!");
         else {
             resourceWorld = Bukkit.getWorld(resurceWorldName);
             if (resourceWorld == null)
                 ConsoleUtils.printWarning(Core.NAME, "Can't find the resource world '" + resurceWorldName + "'. Player cannot create or use their mines");
             else
                 ConsoleUtils.printInfo(Core.NAME, "Resource world is: " + resurceWorldName);
         }
 
         List<?> list = config.getList("resourceWorld.noWarpsWorlds");
         if (list != null) {
            forbiddenWarpWorlds = new HashSet<String>(list.size());
             for (Object o : list)
                 forbiddenWarpWorlds.add(o.toString().toLowerCase());
 
             ConsoleUtils.printInfo(Core.NAME, "Worlds disallowing warps: " + forbiddenWarpWorlds.toString());
         } else
             ConsoleUtils.printInfo(Core.NAME, "All worlds allow warps");
 
     }
     private static void loadMaxWarps() {
         // GET THE SUB SECTION
         ConfigurationSection section = config.getConfigurationSection("warpCounter.private");
 
         // READ THE COUNTER FOR MAX ALLOWED PRIVATE WARPS
         maxPrivateWarps = new HashMap<MinestarGroup, Integer>();
         MinestarGroup group;
         int count;
         for (Entry<String, Object> configEntry : section.getValues(true).entrySet()) {
             group = MinestarGroup.getGroup(configEntry.getKey());
             count = Integer.parseInt(configEntry.getValue().toString());
             maxPrivateWarps.put(group, count);
         }
 
         // READ THE COUNTER FOR MAX ALLOWED PUBLIC WARPS
         section = config.getConfigurationSection("warpCounter.public");
         maxPublicWarps = new HashMap<MinestarGroup, Integer>();
         for (Entry<String, Object> configEntry : section.getValues(true).entrySet()) {
             group = MinestarGroup.getGroup(configEntry.getKey());
             count = Integer.parseInt(configEntry.getValue().toString());
             maxPublicWarps.put(group, count);
         }
     }
 
     public static Integer getMaxPrivateWarps(String playerName) {
         return getMaxPrivateWarps(MinestarCore.getPlayer(playerName).getMinestarGroup());
     }
 
     public static Integer getMaxPrivateWarps(MinestarGroup group) {
         return maxPrivateWarps.get(group);
     }
 
     public static Integer getMaxPublicWarps(String playerName) {
         return getMaxPublicWarps(MinestarCore.getPlayer(playerName).getMinestarGroup());
     }
 
     public static Integer getMaxPublicWarps(MinestarGroup group) {
         return maxPublicWarps.get(group);
     }
 
     public static int getPageSize() {
         return pageSize;
     }
 
     public static int getMinWarpnameSize() {
         return minWarpnameSize;
     }
 
     public static int getMaxWarpnameSize() {
         return maxWarpnameSize;
     }
 
     public static ChatColor getWarpListOwned() {
         return warpListOwned;
     }
 
     public static ChatColor getWarpListPrivate() {
         return warpListPrivate;
     }
 
     public static ChatColor getWarpListPublic() {
         return warpListPublic;
     }
 
     public static int getBackPositionLimit() {
         return backPositionLimit;
     }
 
     public static World getResourceWorld() {
         return resourceWorld;
     }
 
     public static Set<String> getForbiddenWarpWorlds() {
         return forbiddenWarpWorlds;
     }
 }
