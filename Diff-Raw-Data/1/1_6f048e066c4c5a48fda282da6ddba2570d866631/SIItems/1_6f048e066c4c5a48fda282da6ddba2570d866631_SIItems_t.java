 package haveric.stackableItems;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.bukkit.Material;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.configuration.InvalidConfigurationException;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 
 public final class SIItems {
 
     //           world.player/group  item    num
     private static Map<String, Map<String, Integer>> itemsMap;
     //                 item        groups
     private static Map<String, ArrayList<String>> itemGroups;
 
     private static StackableItems plugin;
 
     private static FileConfiguration configItems;
     private static File configItemsFile;
 
     private static FileConfiguration configGroups;
     private static File configGroupsFile;
 
     private static FileConfiguration itemsConfig;
     private static File itemsFile;
 
     private static String cfgMin = "MIN";
     private static String cfgMax = "MAX";
     private static String allWorlds = "allWorlds";
 
     public static final int ITEM_DEFAULT = -1;
     public static final int ITEM_INFINITE = -2;
     public static final int ITEM_DEFAULT_MAX = 64;
     public static final int ITEM_NEW_MAX = 127;
 
     private SIItems() { } // Private constructor for utility class
 
     public static void init(StackableItems si) {
         plugin = si;
 
         configGroupsFile = new File(plugin.getDataFolder() + "/groups.yml");
         configGroups = YamlConfiguration.loadConfiguration(configGroupsFile);
         if (configGroupsFile.length() == 0) {
             Config.saveConfig(configGroups, configGroupsFile);
         }
 
         itemsFile = new File(plugin.getDataFolder() + "/items.yml");
         itemsConfig = YamlConfiguration.loadConfiguration(itemsFile);
         setupItemsFile();
 
         reload();
     }
 
     private static void setupItemsFile() {
         itemsConfig.addDefault("allWorlds.default." + cfgMin, ITEM_DEFAULT);
         itemsConfig.addDefault("allWorlds.default." + cfgMax, ITEM_DEFAULT);
         itemsConfig.addDefault("allWorlds.testPlayer." + cfgMin, ITEM_DEFAULT);
         itemsConfig.addDefault("allWorlds.testPlayer." + cfgMax, ITEM_DEFAULT);
         itemsConfig.addDefault("allWorlds.testGroup." + cfgMin, ITEM_DEFAULT);
         itemsConfig.addDefault("allWorlds.testGroup." + cfgMax, ITEM_DEFAULT);
         itemsConfig.addDefault("allWorlds.chest." + cfgMin, ITEM_DEFAULT);
         itemsConfig.addDefault("allWorlds.chest." + cfgMax, ITEM_DEFAULT);
 
         itemsConfig.addDefault("testWorld.default." + cfgMin, ITEM_DEFAULT);
         itemsConfig.addDefault("testWorld.default." + cfgMax, ITEM_DEFAULT);
 
         itemsConfig.options().copyDefaults(true);
         Config.saveConfig(itemsConfig, itemsFile);
     }
 
     public static void reload() {
         itemsMap = new HashMap<String, Map<String, Integer>>();
         itemGroups = new HashMap<String, ArrayList<String>>();
 
         try {
             configGroups.load(configGroupsFile);
         } catch (FileNotFoundException e) {
             plugin.log.warning("groups.yml missing. Creating a new one");
             Config.saveConfig(configGroups, configGroupsFile);
         } catch (IOException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         } catch (InvalidConfigurationException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
 
        itemsConfig = YamlConfiguration.loadConfiguration(itemsFile);
         loadItemsFile();
 
         loadItemGroups();
     }
 
     private static void loadItemsFile() {
         for (String key : itemsConfig.getKeys(false)) {
             Set<String> categories = itemsConfig.getConfigurationSection(key).getKeys(false);
 
             for (String category : categories) {
                 String catEntry = key + "." + category;
                 ConfigurationSection catSection = itemsConfig.getConfigurationSection(catEntry);
                 Set<String> items = catSection.getKeys(false);
 
                 if (!itemsMap.containsKey(catEntry)) {
                     itemsMap.put(catEntry, new HashMap<String, Integer>());
                 }
                 Map<String, Integer> itemsCat = itemsMap.get(catEntry);
 
                 for (String item : items) {
                     Object temp = catSection.get(item);
 
                     if (temp instanceof String) {
                         if (temp.equals("unlimited") || temp.equals("infinite") || temp.equals("infinity")) {
                             itemsCat.put(item.toUpperCase(), ITEM_INFINITE);
                         }
                     } else if (temp instanceof Integer) {
                         itemsCat.put(item.toUpperCase(), (Integer) temp);
                     }
                 }
             }
         }
     }
 
     private static void loadItemGroups() {
         List<String> saveList = new ArrayList<String>();
         for (String key : configGroups.getKeys(false)) {
             List<String> items = configGroups.getStringList(key);
             int size = items.size();
 
             if (size == 0) {
                 saveList.add(key);
             } else {
                 saveList.add(key);
 
                 for (int i = 0; i < size; i++) {
                     String item = items.get(i).toUpperCase();
                     if (!itemGroups.containsKey(item)) {
                         itemGroups.put(item, new ArrayList<String>());
                     }
                     itemGroups.get(item).addAll(saveList);
                 }
                 saveList.clear();
             }
         }
     }
 
     public static int getItemMax(Player player, Material mat, short dur, String inventoryType) {
         String world = player.getWorld().getName();
         int max = ITEM_DEFAULT;
 
         // Force air to keep default value
         if (mat != Material.AIR) {
 
             // Check player
             String playerName = player.getName();
             max = getMax(world + "." + playerName, mat, dur);
 
             if (max == ITEM_DEFAULT) {
                 max = getMax(allWorlds + "." + playerName, mat, dur);
             }
 
             // Check groups
             if (max == ITEM_DEFAULT && Perms.canStackInGroup(player)) {
                 String group = Perms.getPrimaryGroup(player);
                 if (group != null) {
                     max = getMax(world + "." + group, mat, dur);
                     if (max == ITEM_DEFAULT) {
                         max = getMax(allWorlds + "." + group, mat, dur);
                     }
                 }
             }
 
             // Check inventory types
             if (max == ITEM_DEFAULT) {
                 max = getMax(world + "." + inventoryType, mat, dur);
             }
             if (max == ITEM_DEFAULT) {
                 max = getMax(allWorlds + "." + inventoryType, mat, dur);
             }
 
             // Check default
             if (max == ITEM_DEFAULT) {
                 max = getMax(world + "." + "default", mat, dur);
             }
             if (max == ITEM_DEFAULT) {
                 max = getMax(allWorlds + "." + "default", mat, dur);
             }
 
             // Handle invalid max
             if (max <= ITEM_DEFAULT && max != ITEM_INFINITE) {
                 // Invalid max, count as default
                 max = ITEM_DEFAULT;
             }
         }
         return max;
     }
 
     public static int getMax(String itemString, Material mat, short dur) {
         if (dur == ITEM_DEFAULT) {
             return getMaxFromMap(itemString, mat);
         }
 
         return getMaxFromMap(itemString, mat, dur);
     }
 /*
     public static void setDefaultMax(Material mat, short dur, int newAmount) {
         setMax("defaultItems", mat, dur, newAmount);
     }
 
     public static void setMax(String playerOrGroup, Material mat, short dur, int newAmount) {
         configItemsFile = new File(plugin.getDataFolder() + "/" + playerOrGroup + ".yml");
         configItems = YamlConfiguration.loadConfiguration(configItemsFile);
 
         String name;
         if (dur == ITEM_DEFAULT) {
             name = mat.name();
         } else {
             name = mat.name() + " " + dur;
         }
 
         configItems.set(name, newAmount);
         if (!itemsMap.containsKey(playerOrGroup)) {
             itemsMap.put(playerOrGroup, new HashMap<String, Integer>());
         }
         itemsMap.get(playerOrGroup).put(name, newAmount);
 
         try {
             configItems.save(configItemsFile);
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 */
     private static int getMaxFromMap(String itemString, Material mat, short dur) {
         //plugin.log.info("Get Max 1: " + itemString + ", " + mat.name() + ", " + dur);
         int max = ITEM_DEFAULT;
 
         List<String> groups = null;
         String matName = mat.name().toUpperCase();
         int matId = mat.getId();
         if (itemGroups.containsKey(matName + " " + dur)) {
             groups = itemGroups.get(matName + " " + dur);
         } else if (itemGroups.containsKey(matId + " " + dur)) {
             groups = itemGroups.get(matId + " " + dur);
         } else if (itemGroups.containsKey(matName)) {
             groups = itemGroups.get(matName);
         } else if (itemGroups.containsKey("" + matId)) {
             groups = itemGroups.get("" + matId);
         }
 
         if (itemsMap.containsKey(itemString)) {
             Map<String, Integer> subMap = itemsMap.get(itemString);
 
             if (groups != null) {
                 int groupSize = groups.size();
                 for (int i = 0; i < groupSize; i++) {
                     String key = groups.get(i).toUpperCase();
                     if (subMap.containsKey(key)) {
                         max = subMap.get(key);
                     }
                 }
             }
 
             if (max == ITEM_DEFAULT) {
                 // check for material and durability
                 if (subMap.containsKey(matName + " " + dur)) {
                     max = subMap.get(matName + " " + dur);
                 // check for item id and durability
                 } else if (subMap.containsKey(matId + " " + dur)) {
                     max = subMap.get(matId + " " + dur);
                 // material name with no durability
                 } else if (subMap.containsKey(matName)) {
                     max = subMap.get(matName);
                 // item id with no durability
                 } else if (subMap.containsKey("" + matId)) {
                     max = subMap.get("" + matId);
                 // no individual item set, use the max and min values
                 } else {
                     int defaultMax = mat.getMaxStackSize();
                     if (subMap.containsKey(cfgMin)) {
                         int temp = subMap.get(cfgMin);
                         if (temp > defaultMax && temp > ITEM_DEFAULT) {
                             max = temp;
                         }
                     }
                     if (subMap.containsKey(cfgMax)) {
                         int temp = subMap.get(cfgMax);
                         if (temp < defaultMax && temp > ITEM_DEFAULT) {
                             max = temp;
                         }
                     }
                 }
             }
 
             // TODO: implement workaround to allow larger stacks after player leaving and logging back in.
             if (max > SIItems.ITEM_NEW_MAX) {
                 max = SIItems.ITEM_NEW_MAX;
             }
         }
 
         return max;
     }
 
     private static int getMaxFromMap(String file, Material mat) {
         plugin.log.info("Get Max 2");
         int max = ITEM_DEFAULT;
 
         List<String> groups = null;
 
         String matName = mat.name().toUpperCase();
         int matId = mat.getId();
         if (itemGroups.containsKey(matName)) {
             groups = itemGroups.get(matName);
         } else if (itemGroups.containsKey("" + matId)) {
             groups = itemGroups.get("" + matId);
         }
 
         if (itemsMap.containsKey(file)) {
             Map<String, Integer> subMap = itemsMap.get(file);
 
             if (groups != null) {
                 int groupSize = groups.size();
                 for (int i = 0; i < groupSize; i++) {
                     String key = groups.get(i).toUpperCase();
                     if (subMap.containsKey(key)) {
                         max = subMap.get(key);
                     }
                 }
             }
 
             if (max == ITEM_DEFAULT) {
                 // material name with no durability
                 if (subMap.containsKey(matName)) {
                     max = subMap.get(matName);
                 // item id with no durability
                 } else if (subMap.containsKey("" + matId)) {
                     max = subMap.get("" + matId);
                  // no individual item set, use the max and min values
                 } else {
                     int defaultMax = mat.getMaxStackSize();
                     if (subMap.containsKey(cfgMin)) {
                         int temp = subMap.get(cfgMin);
                         if (temp > defaultMax && temp > ITEM_DEFAULT) {
                             max = temp;
                         }
                     }
                     if (subMap.containsKey(cfgMax)) {
                         int temp = subMap.get(cfgMax);
                         if (temp < defaultMax && temp > ITEM_DEFAULT) {
                             max = temp;
                         }
                     }
                 }
             }
 
             // TODO: implement workaround to allow larger stacks after player leaving and logging back in.
             if (max > SIItems.ITEM_NEW_MAX) {
                 max = SIItems.ITEM_NEW_MAX;
             }
         }
 
         return max;
     }
 }
