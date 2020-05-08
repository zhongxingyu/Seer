 package com.codisimus.plugins.chestlock;
 
 import java.io.*;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.LinkedList;
 import java.util.Properties;
 import net.milkbowl.vault.economy.Economy;
 import net.milkbowl.vault.permission.Permission;
 import org.bukkit.Material;
 import org.bukkit.Server;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.RegisteredServiceProvider;
 import org.bukkit.plugin.java.JavaPlugin;
 
 /**
  * Loads Plugin and manages Data/Permissions
  * 
  * @author Codisimus
  */
 public class ChestLock extends JavaPlugin {
     private static Server server;
     static Permission permission;
     private static PluginManager pm;
     public static int own;
     public static int lock;
     public static int info;
     public static int admin;
     public static int disown;
     public static int adminDisown;
     public static int global;
     public static boolean explosionProtection;
     private Properties p;
     private static LinkedList<Safe> chests = new LinkedList<Safe>();
     private static LinkedList<Safe> furnaces = new LinkedList<Safe>();
     private static LinkedList<Safe> dispensers = new LinkedList<Safe>();
     public static LinkedList<LockedDoor> doors = new LinkedList<LockedDoor>();
     private static String dataFolder;
     private static int disownTime;
     public static Properties lastDaySeen = new Properties();
 
     @Override
     public void onDisable () {
     }
 
     /**
      * Calls methods to load this Plugin when it is enabled
      *
      */
     @Override
     public void onEnable () {
         server = getServer();
         pm = server.getPluginManager();
         
         File dir = this.getDataFolder();
         if (!dir.isDirectory())
             dir.mkdir();
         
         dataFolder = dir.getPath();
         
         dir = new File(dataFolder+"/Chests");
         if (!dir.isDirectory())
             dir.mkdir();
         
         dir = new File(dataFolder+"/Furnaces");
         if (!dir.isDirectory())
             dir.mkdir();
         
         dir = new File(dataFolder+"/Dispensers");
         if (!dir.isDirectory())
             dir.mkdir();
         
         dir = new File(dataFolder+"/Doors");
         if (!dir.isDirectory())
             dir.mkdir();
         
         //Load Config settings
         loadSettings();
         
         loadAll();
         
         //Find Permissions
         RegisteredServiceProvider<Permission> permissionProvider =
                 getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
         if (permissionProvider != null)
             permission = permissionProvider.getProvider();
         
         //Find Economy
         RegisteredServiceProvider<Economy> economyProvider =
                 getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
         if (economyProvider != null)
             Econ.economy = economyProvider.getProvider();
         
         //Register the command found in the plugin.yml
         ChestLockCommand.command = (String)this.getDescription().getCommands().keySet().toArray()[0];
         getCommand(ChestLockCommand.command).setExecutor(new ChestLockCommand());
         
         //Register Events
         pm.registerEvents(new ChestLockListener(), this);
         
         //Start the tickListener if there is an AutoDisownTimer
         if (disownTime > 0)
             tickListener();
         
         System.out.println("ChestLock "+this.getDescription().getVersion()+" is enabled!");
     }
     
     /**
      * Loads settings from the config.properties file
      * 
      */
     public void loadSettings() {
         try {
             //Copy the file from the jar if it is missing
             File file = new File(dataFolder+"/config.properties");
             if (!file.exists())
                 this.saveResource("config.properties", true);
             
             //Load config file
             p = new Properties();
             FileInputStream fis = new FileInputStream(file);
             p.load(fis);
             
             explosionProtection = Boolean.parseBoolean(loadValue("ExplosionProtection"));
             ChestLockListener.unlockToOpen = Boolean.parseBoolean(loadValue("UnlockToOpen"));
             
             ChestLockListener.ownPrice = Integer.parseInt(loadValue("CostToOwn"));
             ChestLockListener.lockPrice = Integer.parseInt(loadValue("CostToLock"));
             
             disownTime = Integer.parseInt(loadValue("AutoDisownTimer"));
             
             own = getID(loadValue("OwnTool"));
             lock = getID(loadValue("LockTool"));
             info = getID(loadValue("AdminInfoTool"));
             admin = getID(loadValue("AdminLockTool"));
             adminDisown = getID(loadValue("AdminDisownTool"));
             global = getID(loadValue("AdminGlobalKey"));
             disown = getID(loadValue("DisownTool"));
             
             ChestLockMessages.setPermissionMsg(loadValue("PermissionMessage"));
             ChestLockMessages.setLockMsg(loadValue("LockMessage"));
             ChestLockMessages.setLockedMsg(loadValue("LockedMessage"));
             ChestLockMessages.setUnlockMsg(loadValue("UnlockMessage"));
             ChestLockMessages.setKeySetMsg(loadValue("KeySetMessage"));
             ChestLockMessages.setInvalidKeyMsg(loadValue("InvalidKeyMessage"));
             ChestLockMessages.setUnlockableMsg(loadValue("UnlockableMessage"));
             ChestLockMessages.setLockableMsg(loadValue("LockableMessage"));
             ChestLockMessages.setDoNotOwnMsg(loadValue("DoNotOwnMessage"));
             ChestLockMessages.setOwnMsg(loadValue("OwnMessage"));
             ChestLockMessages.setDisownMsg(loadValue("DisownMessage"));
             ChestLockMessages.setLimitMsg(loadValue("limitMessage"));
             ChestLockMessages.setClearMsg(loadValue("ClearMessage"));
             ChestLockMessages.setInsufficientFundsMsg(loadValue("InsufficientFundsMessage"));
             
             fis.close();
         }
         catch (Exception missingProp) {
             System.err.println("Failed to load ChestLock "+this.getDescription().getVersion());
             missingProp.printStackTrace();
         }
     }
 
     /**
      * Converts the given String to the equal Material ID
      * 
      * @param type The given type (Material name or ID)
      * @return The Material ID
      */
     private int getID(String type) {
         //Return -1 for any Block type
         if (type.equalsIgnoreCase("any"))
             return -1;
         
         //Try to find the Material
         Material mat = Material.getMaterial(type.toUpperCase());
         if (mat != null)
             //Return the TypeID
             return mat.getId();
         
         //Iterate through the String to make sure each Character is a digit
         for (char c: type.toCharArray())
             if (!Character.isDigit(c)) {
                 System.err.println("[ChestLock] "+type+" is not a valid Block type");
                 return -2;
             }
         
         //Return the String as an int
         return Integer.parseInt(type);
     }
 
     /**
      * Loads the given key and prints an error if the key is missing
      *
      * @param key The key to be loaded
      * @return The String value of the loaded key
      */
     private String loadValue(String key) {
         //Print an error if the key is not found
         if (!p.containsKey(key)) {
             System.err.println("[ChestLock] Missing value for "+key+" in config file");
             System.err.println("[ChestLock] Please regenerate config file");
         }
         
         return p.getProperty(key);
     }
 
     /**
      * Returns boolean value of whether the given player has the specific permission
      * 
      * @param player The Player who is being checked for permission
      * @param node The String of the permission, ex. admin
      * @return true if the given player has the specific permission
      */
     public static boolean hasPermission(Player player, String node) {
         return permission.has(player, "chestlock."+node);
     }
     
     /**
      * Returns the Integer value that is the limit of items the given Player can own
      * Returns -1 if there is no limit
      * 
      * @param player The Player who is being checked for a limit
      * @param type The type of item (ex. chest or door)
      * @return The Integer value that is the limit of items the given Player can own
      */
     public static int getOwnLimit(Player player, String type) {
         //First check for no limit node
         if (hasPermission(player, "limit."+type+".-1"))
             return -1;
 
         //Start by checking a limit of 100 and work down to a limit of 0
         for (int i = 100; i >= 0; i--)
             if (hasPermission(player, "limit."+type+"."+i))
                 return i;
         
         //No limit if a limit node was not found
         return -1;
     }
     
     /*
      * Loads data for all Worlds
      * 
      */
     public static void loadAll() {
         for (World loadedWorld: server.getWorlds())
             loadData(loadedWorld);
         
         if (chests.isEmpty() && furnaces.isEmpty() && dispensers.isEmpty() && doors.isEmpty())
             for (World loadedWorld: server.getWorlds())
                 loadOld(loadedWorld);
     }
     
     /**
      * Reads save file to load ChestLock data
      * Loads only data for specific World if one is provided
      * Saving is turned off (or line is deleted) if an error occurs
      * 
      * @param world The World if one is provided
      */
     public static void loadData(World world) {
         BufferedReader bReader;
         String line;
         
         //Open save file for the Chest data of the given World
         File file = new File(dataFolder+"/Chests/"+world.getName()+".clc");
         if (file.exists()) {
             try {
                 bReader = new BufferedReader(new FileReader(file));
 
                 //Convert each line into data until all lines are read
                 while ((line = bReader.readLine()) != null) {
                     try {
                         String[] split = line.split(";");
 
                         String owner = split[0];
 
                         int x = Integer.parseInt(split[1]);
                         int y = Integer.parseInt(split[2]);
                         int z = Integer.parseInt(split[3]);
                         Block block = world.getBlockAt(x, y, z);
 
                         //Skip this data if the Block is not a Chest
                         if (block.getTypeId() != 54)
                             continue;
 
                         LinkedList<String> coOwners = new LinkedList<String>();
                         LinkedList<String> groups = new LinkedList<String>();
 
                         boolean lockable = Boolean.parseBoolean(split[4]);
                         coOwners.addAll(Arrays.asList(split[5].substring(1,
                                 split[5].length() - 1).split(", ")));
                         groups.addAll(Arrays.asList(split[6].substring(1,
                                 split[6].length() - 1).split(", ")));
 
                         chests.add(new Safe(owner, block, lockable, coOwners, groups));
                     }
                     catch (Exception corruptedData) {
                         /* Do not load this line */
                     }
                 }
 
                 bReader.close();
             }
             catch (Exception loadFailed) {
                 System.err.println("[ChestLock] Error occurred while loading data");
                 loadFailed.printStackTrace();
             }
         }
         
         //Open save file for the Furnace data of the given World
         file = new File(dataFolder+"/Furnaces/"+world.getName()+".clf");
         if (file.exists()) {
             try {
                 bReader = new BufferedReader(new FileReader(file));
 
                 //Convert each line into data until all lines are read
                 while ((line = bReader.readLine()) != null) {
                     try {
                         String[] split = line.split(";");
 
                         String owner = split[0];
 
                         int x = Integer.parseInt(split[1]);
                         int y = Integer.parseInt(split[2]);
                         int z = Integer.parseInt(split[3]);
                         Block block = world.getBlockAt(x, y, z);
 
                         //Skip this data if the Block is not a Furnace
                         int id = block.getTypeId();
                         if (id != 61 && id != 62)
                             continue;
 
                         LinkedList<String> coOwners = new LinkedList<String>();
                         LinkedList<String> groups = new LinkedList<String>();
 
                         boolean lockable = Boolean.parseBoolean(split[4]);
                         coOwners.addAll(Arrays.asList(split[5].substring(1,
                                 split[5].length() - 1).split(", ")));
                         groups.addAll(Arrays.asList(split[6].substring(1,
                                 split[6].length() - 1).split(", ")));
 
                         furnaces.add(new Safe(owner, block, lockable, coOwners, groups));
                     }
                     catch (Exception corruptedData) {
                         /* Do not load this line */
                     }
                 }
 
                 bReader.close();
             }
             catch (Exception loadFailed) {
                 System.err.println("[ChestLock] Error occurred while loading data");
                 loadFailed.printStackTrace();
             }
         }
         
         //Open save file for the Dispenser data of the given World
         file = new File(dataFolder+"/Dispensers/"+world.getName()+".cld");
         if (file.exists()) {
             try {
                 bReader = new BufferedReader(new FileReader(file));
 
                 //Convert each line into data until all lines are read
                 while ((line = bReader.readLine()) != null) {
                     try {
                         String[] split = line.split(";");
 
                         String owner = split[0];
 
                         int x = Integer.parseInt(split[1]);
                         int y = Integer.parseInt(split[2]);
                         int z = Integer.parseInt(split[3]);
                         Block block = world.getBlockAt(x, y, z);
 
                         //Skip this data if the Block is not a Dispenser
                         if (block.getTypeId() != 23)
                             continue;
 
                         LinkedList<String> coOwners = new LinkedList<String>();
                         LinkedList<String> groups = new LinkedList<String>();
 
                         boolean lockable = Boolean.parseBoolean(split[4]);
                         coOwners.addAll(Arrays.asList(split[5].substring(1,
                                 split[5].length() - 1).split(", ")));
                         groups.addAll(Arrays.asList(split[6].substring(1,
                                 split[6].length() - 1).split(", ")));
 
                         dispensers.add(new Safe(owner, block, lockable, coOwners, groups));
                     }
                     catch (Exception corruptedData) {
                         /* Do not load this line */
                     }
                 }
 
                 bReader.close();
             }
             catch (Exception loadFailed) {
                 System.err.println("[ChestLock] Error occurred while loading data");
                 loadFailed.printStackTrace();
             }
         }
         
         //Open save file for the Door data of the given World
         file = new File(dataFolder+"/Doors/"+world.getName()+".cldr");
         if (file.exists()) {
             try {
                 bReader = new BufferedReader(new FileReader(file));
 
                 //Convert each line into data until all lines are read
                 while ((line = bReader.readLine()) != null) {
                     try {
                         String[] split = line.split(";");
 
                         String owner = split[0];
 
                         int x = Integer.parseInt(split[1]);
                         int y = Integer.parseInt(split[2]);
                         int z = Integer.parseInt(split[3]);
                         Block block = world.getBlockAt(x, y, z);
 
                         //Skip this data if the Block is not a Door
                         switch (block.getType()) {
                             case WOOD_DOOR: break;
                             case WOODEN_DOOR: break;
                             case IRON_DOOR: break;
                             case IRON_DOOR_BLOCK: break;
                             default: continue;
                         }
 
                         doors.add(new LockedDoor(owner, block, Integer.parseInt(split[4])));
                     }
                     catch (Exception corruptedData) {
                         /* Do not load this line */
                     }
                 }
 
                 bReader.close();
             }
             catch (Exception loadFailed) {
                 System.err.println("[ChestLock] Error occurred while loading data");
                 loadFailed.printStackTrace();
             }
         }
     }
     
     /**
      * Reads save file to load ChestLock data
      * Loads only data for specific World if one is provided
      * Saving is turned off (or line is deleted) if an error occurs
      * 
      * @param world The World if one is provided
      */
     public static void loadOld(World world) {
         try {
             //Open save file in BufferedReader
             File file = new File(dataFolder.concat("/chestlock.save"));
             if (!file.exists())
                 return;
             BufferedReader bReader = new BufferedReader(new FileReader(file));
 
             //Convert each line into data until all lines are read
             String line;
             while ((line = bReader.readLine()) != null) {
                 String[] split = line.split(";");
                 
                 if (split[1].endsWith("~NETHER"))
                     split[1].replace("~NETHER", "");
                 
                 if (world.getName().equals(split[1])) {
                     String owner = split[0];
                     
                     int x = Integer.parseInt(split[2]);
                     int y = Integer.parseInt(split[3]);
                     int z = Integer.parseInt(split[4]);
                     Block block = world.getBlockAt(x, y, z);
                     
                     //Create a LockedDoor if the Block is a door
                     Material material = block.getType();
                     switch (material) {
                         case WOOD_DOOR: //Fall through
                         case WOODEN_DOOR: //Fall through
                         case IRON_DOOR: //Fall through
                        case IRON_DOOR_BLOCK:
                             doors.add(new LockedDoor(owner, block, Integer.parseInt(split[5])));
                             return;
                             
                         default: break;
                     }
                     
                     boolean lockable = true;
                     LinkedList<String> coOwners = new LinkedList<String>();
                     LinkedList<String> groups = new LinkedList<String>();
                     
                     if (split.length == 6) {
                         String coOwnerList = split[5];
                         if (coOwnerList.contains("unlockable"))
                             lockable = false;
                         else {
                             if (coOwnerList.startsWith("CoOwners:"))
                                 coOwnerList = coOwnerList.substring(9);
                             else
                                 coOwnerList = coOwnerList.replaceAll(",any,", ",");
                             
                             for (String coOwner: coOwnerList.split(","))
                                 if (coOwner.startsWith("player:"))
                                     coOwners.add(coOwner.substring(7));
                                 else if (coOwner.startsWith("group:"))
                                     coOwners.add(coOwner.substring(6));
                                 else
                                     coOwners.add(coOwner);
                         }
                     }
                     else {
                         lockable = Boolean.parseBoolean(split[5]);
                         coOwners.addAll(Arrays.asList(split[6].substring(1,
                                 split[6].length() - 1).split(", ")));
                         groups.addAll(Arrays.asList(split[7].substring(1,
                                 split[7].length() - 1).split(", ")));
                     }
                     
                     switch (material) {
                         case DISPENSER: dispensers.add(new Safe(owner, block, lockable, coOwners, groups)); break;
                         case CHEST: chests.add(new Safe(owner, block, lockable, coOwners, groups)); break;
                         case FURNACE: furnaces.add(new Safe(owner, block, lockable, coOwners, groups)); break;
                         case BURNING_FURNACE: furnaces.add(new Safe(owner, block, lockable, coOwners, groups)); break;
                         
                         default:
                             System.err.println("[ChestLock] Invalid blocktype for "+line);
                     }
                 }
             }
 
             bReader.close();
         }
         catch (Exception loadFailed) {
             System.err.println("[ChestLock] Load failed, saving turned off to prevent loss of data");
             loadFailed.printStackTrace();
         }
     }
     
     /**
      * Saves data for all Worlds
      * 
      */
     public static void saveAll() {
         for (World world: server.getWorlds())
             save(world);
     }
     
     /**
      * Saves data for the given World
      * Old files are overwritten
      */
     public static void save(World world) {
         BufferedWriter bWriter;
         File file;
         
         try {
             if (!chests.isEmpty()) {
                 file = new File(dataFolder+"/Chests/"+world.getName()+".clc");
                 if (!file.exists())
                     file.createNewFile();
                 
                 //Open save file for writing data
                 bWriter = new BufferedWriter(new FileWriter(file));
                 
                 //Write all Chest data to file that pertains to the given World
                 for (Safe safe: chests)
                     if (safe.block.getWorld().equals(world)) {
                         //Write data in format "owner;x;y;z;lockable;coOwner1,coOwner2,...;group1,group2,...;"
                         bWriter.write(safe.owner.concat(";"));
 
                         bWriter.write(safe.block.getX()+";");
                         bWriter.write(safe.block.getY()+";");
                         bWriter.write(safe.block.getZ()+";");
 
                         bWriter.write(safe.lockable+";");
 
                         bWriter.write(safe.coOwners.toString().concat(";"));
                         bWriter.write(safe.groups.toString().concat(";"));
 
                         //Write each Chest on its own line
                         bWriter.newLine();
                     }
                 
                 bWriter.close();
             }
             
             if (!furnaces.isEmpty()) {
                 file = new File(dataFolder+"/Furnaces/"+world.getName()+".clf");
                 if (!file.exists())
                     file.createNewFile();
                 
                 //Open save file for writing data
                 bWriter = new BufferedWriter(new FileWriter(file));
                 
                 //Write all Furnace data to file that pertains to the given World
                 for (Safe safe: furnaces)
                     if (safe.block.getWorld().equals(world)) {
                         //Write data in format "owner;x;y;z;lockable;coOwner1,coOwner2,...;group1,group2,...;"
                         bWriter.write(safe.owner.concat(";"));
 
                         bWriter.write(safe.block.getX()+";");
                         bWriter.write(safe.block.getY()+";");
                         bWriter.write(safe.block.getZ()+";");
 
                         bWriter.write(safe.lockable+";");
 
                         bWriter.write(safe.coOwners.toString().concat(";"));
                         bWriter.write(safe.groups.toString().concat(";"));
 
                         //Write each Furnace on its own line
                         bWriter.newLine();
                     }
                 
                 bWriter.close();
             }
             
             if (!dispensers.isEmpty()) {
                 file = new File(dataFolder+"/Dispensers/"+world.getName()+".cld");
                 if (!file.exists())
                     file.createNewFile();
                 
                 //Open save file for writing data
                 bWriter = new BufferedWriter(new FileWriter(file));
                 
                 //Write all Dispenser data to file that pertains to the given World
                 for (Safe safe: dispensers)
                     if (safe.block.getWorld().equals(world)) {
                         //Write data in format "owner;x;y;z;lockable;coOwner1,coOwner2,...;group1,group2,...;"
                         bWriter.write(safe.owner.concat(";"));
 
                         bWriter.write(safe.block.getX()+";");
                         bWriter.write(safe.block.getY()+";");
                         bWriter.write(safe.block.getZ()+";");
 
                         bWriter.write(safe.lockable+";");
 
                         bWriter.write(safe.coOwners.toString().concat(";"));
                         bWriter.write(safe.groups.toString().concat(";"));
 
                         //Write each Dispenser on its own line
                         bWriter.newLine();
                     }
                 
                 bWriter.close();
             }
             
             if (!doors.isEmpty()) {
                 file = new File(dataFolder+"/Doors/"+world.getName()+".cldr");
                 if (!file.exists())
                     file.createNewFile();
                 
                 //Open save file for writing data
                 bWriter = new BufferedWriter(new FileWriter(file));
                 
                 //Write all Door data to file that pertains to the given World
                 for (LockedDoor door: doors)
                     if (door.block.getWorld().equals(world)) {
                         //Write data in format "owner;x;y;z;key;"
                         bWriter.write(door.owner.concat(";"));
                         Block block = door.block;
                         bWriter.write(block.getX()+";");
                         bWriter.write(block.getY()+";");
                         bWriter.write(block.getZ()+";");
                         bWriter.write(door.key+";");
 
                         //Write each Door on its own line
                         bWriter.newLine();
                     }
                 
                 bWriter.close();
             }
         }
         catch (Exception saveFailed) {
             System.err.println("[ChestLock] Save failed!");
             saveFailed.printStackTrace();
         }
     }
     
     /**
      * Writes the Map of last seen data to the save file
      * Old file is over written
      */
     public static void saveLastSeen() {
         try {
             lastDaySeen.store(new FileOutputStream(dataFolder.concat("/lastseen.properties")), null);
         }
         catch (Exception ex) {
         }
     }
     
     /**
      * Returns the LinkedList of Chests owned by the given Player
      * 
      * @param player The given Player
      * @return The LinkedList of Chests owned by the given Player
      */
     public static LinkedList<Safe> getOwnedChests(String player) {
         LinkedList<Safe> ownedChests = new LinkedList<Safe>();
         
         //Iterate through all Chests and add the ones that the Player owns
         for (Safe safe: chests)
             if (safe.owner.equals(player))
                 ownedChests.add(safe);
         
         return ownedChests;
     }
     
     /**
      * Returns the LinkedList of Furnaces owned by the given Player
      * 
      * @param player The given Player
      * @return The LinkedList of Furnaces owned by the given Player
      */
     public static LinkedList<Safe> getOwnedFurnaces(String player) {
         LinkedList<Safe> ownedChests = new LinkedList<Safe>();
         
         //Iterate through all Furnaces and add the ones that the Player owns
         for (Safe safe: chests)
             if (safe.owner.equals(player))
                 ownedChests.add(safe);
         
         return ownedChests;
     }
     
     /**
      * Returns the LinkedList of Dispensers owned by the given Player
      * 
      * @param player The given Player
      * @return The LinkedList of Dispensers owned by the given Player
      */
     public static LinkedList<Safe> getOwnedDispensers(String player) {
         LinkedList<Safe> ownedChests = new LinkedList<Safe>();
         
         //Iterate through all Dispensers and add the ones that the Player owns
         for (Safe safe: chests)
             if (safe.owner.equals(player))
                 ownedChests.add(safe);
         
         return ownedChests;
     }
     
     /**
      * Returns the Safe of given Block
      * 
      * @param block The given Block
      * @return The Safe of given Block
      */
     public static Safe findSafe(Block block) {
         switch (block.getType()) {
             case DISPENSER:
                 //Iterate through all Dispensers to find the one for the Block
                 for (Safe safe: dispensers)
                     if (safe.block.equals(block))
                         return safe;
                 
                 //Return null because the block is not owned
                 return null;
                 
             case CHEST:
                 //Iterate through all Chests to find the one for the Block
                 for (Safe safe: chests)
                     if (safe.block.equals(block) || safe.isNeighbor(block))
                         return safe;
                 
                 //Return null because the block is not owned
                 return null;
                 
             case FURNACE:
                 //Iterate through all Furnaces to find the one for the Block
                 for (Safe safe: furnaces)
                     if (safe.block.equals(block))
                         return safe;
                 
                 //Return null because the block is not owned
                 return null;
                 
             case BURNING_FURNACE:
                 //Iterate through all Furnaces to find the one for the Block
                 for (Safe safe: furnaces)
                     if (safe.block.equals(block))
                         return safe;
                 
                 //Return null because the block is not owned
                 return null;
                 
             default: return null;
         }
     }
     
     /**
      * Adds the given Safe to the saved data
      * 
      * @param safe The given Safe
      */
     public static void addSafe(Safe safe) {
         switch (safe.block.getType()) {
             case DISPENSER: dispensers.add(safe); break;
             case CHEST: chests.add(safe); break;
             case FURNACE: furnaces.add(safe); break;
             case BURNING_FURNACE: furnaces.add(safe); break;
             default: return;
         }
         
         save(safe.block.getWorld());
     }
     
     /**
      * Removes the given Safe from the saved data
      * 
      * @param safe The given Safe
      */
     public static void removeSafe(Safe safe) {
         switch (safe.block.getType()) {
             case DISPENSER: dispensers.remove(safe); break;
             case CHEST: chests.remove(safe); break;
             case FURNACE: furnaces.remove(safe); break;
             case BURNING_FURNACE: furnaces.remove(safe); break;
             default: return;
         }
         
         save(safe.block.getWorld());
     }
     
     /**
      * Returns the LinkedList of LockedDoors owned by the given Player
      * 
      * @return The LinkedList of LockedDoors owned by the given Player
      */
     public static LinkedList<LockedDoor> getOwnedDoors(String player) {
         LinkedList<LockedDoor> ownedDoors = new LinkedList<LockedDoor>();
         
         //Iterate through all Doors and add the ones that the Player owns
         for (LockedDoor door: doors)
             if (door.owner.equals(player))
                 ownedDoors.add(door);
         
         return ownedDoors;
     }
     
     /**
      * Returns the LockedDoor of given Block
      * 
      * @param block The given Block
      * @return The LockedDoor of given Block
      */
     public static LockedDoor findDoor(Block block) {
         //Return null is the Block is not a Door
         switch (block.getType()) {
             case WOOD_DOOR: break;
             case WOODEN_DOOR: break;
             case IRON_DOOR: break;
             case IRON_DOOR_BLOCK: break;
             default: return null;
         }
         
         //Iterate through all Doors to find the one for the Block
         for (LockedDoor door: doors)
             if (door.block.equals(block) || door.isNeighbor(block))
                 return door;
         
         //Return null because the block is not owned
         return null;
     }
     
     /**
      * Returns the number of the current day in the AD time period
      * 
      * @return The number of the current day in the AD time period
      */
     public static int getDayAD() {
         Calendar calendar = Calendar.getInstance();
         int yearAD = calendar.get(Calendar.YEAR);
         int dayOfYear = calendar.get(Calendar.DAY_OF_YEAR);
         return (int)((yearAD - 1) * 365.4) + dayOfYear;
     }
     
     /**
      * Checks for Players who have not logged on within the given amount of time
      * These Players will have their Safes/Doors automatically disowned
      */
     public void tickListener() {
         //Repeat every day
     	server.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
             @Override
     	    public void run() {
                 int cutoffDay = getDayAD() - disownTime;
                 
                 for (String key: lastDaySeen.stringPropertyNames())
                     if (Integer.parseInt(lastDaySeen.getProperty(key)) < cutoffDay) {
                         System.out.println("[ChestLock] Disowning Chests that are owned by "+key);
                         clear(key);
                         lastDaySeen.remove(key);
                         saveLastSeen();
                     }
     	    }
     	}, 0L, 1728000L);
     }
     
     /**
      * Removes the LockedDoors/Safes that are owned by the given Player
      * 
      * @param player The name of the Player
      */
     public static void clear(String player) {
         //Iterate through all Chests
         for (Safe safe: chests)
             //Remove the Chest if it is owned by the Player
             if (safe.owner.equals(player))
                 chests.remove(safe);
         
         //Iterate through all Furnaces
         for (Safe safe: furnaces)
             //Remove the Furnace if it is owned by the Player
             if (safe.owner.equals(player))
                 chests.remove(safe);
         
         //Iterate through all Dispensers
         for (Safe safe: dispensers)
             //Remove the Dispenser if it is owned by the Player
             if (safe.owner.equals(player))
                 chests.remove(safe);
         
         //Iterate through all Doors
         for (LockedDoor door: doors)
             //Remove the Door if it is owned by the Player
             if (door.owner.equals(player))
                 doors.remove(door);
         
         saveAll();
     }
 }
