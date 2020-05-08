 package com.codisimus.plugins.pvpreward;
 
 import com.codisimus.plugins.pvpreward.Rewarder.RewardType;
 import java.io.*;
 import java.util.*;
 import net.milkbowl.vault.economy.Economy;
 import net.milkbowl.vault.permission.Permission;
 import org.bukkit.Server;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.RegisteredServiceProvider;
 import org.bukkit.plugin.java.JavaPlugin;
 
 /**
  * Loads Plugin and manages Data/Permissions
  *
  * @author Codisimus
  */
 public class PvPReward extends JavaPlugin {
     public static Server server;
     public static Permission permission;
     public static String karmaName;
     public static String outlawName;
     public static int cooldownTime;
     public static boolean negative;
     private static PluginManager pm;
     private static Properties p;
     private static HashMap<String, Record> records = new HashMap<String, Record>();
     private static String dataFolder;
 
     /**
      * Clears all graves that exist when this Plugin is disabled
      *
      */
     @Override
     public void onDisable () {
         if (!PvPRewardListener.digGraves)
             return;
 
         for (Record record: records.values())
             //Reset the Sign to AIR if there is one
             if (record.signLocation != null)
                 record.signLocation.getBlock().setTypeId(0);
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
         
         //Load Config settings
         loadSettings();
         
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
         
         //Load Records Datap.load(new FileInputStream(file));
         loadData();
         
         //Register Events
         pm.registerEvents(new PvPRewardListener(), this);
         
         //Register the command found in the plugin.yml
         PvPRewardCommand.command = (String)this.getDescription().getCommands().keySet().toArray()[0];
         getCommand(PvPRewardCommand.command).setExecutor(new PvPRewardCommand());
         
         System.out.println("PvPReward "+this.getDescription().getVersion()+" is enabled!");
         
         //Start cooldown Thread if there is one
         if (cooldownTime != 0)
             cooldown();
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
             
             PvPRewardMessages.setDeadedMsg(loadValue("KilledMessage"));
             PvPRewardMessages.setKillerMsg(loadValue("KillerMessage"));
             PvPRewardMessages.setDeadedNotEnoughMoneyMsg(loadValue("KilledNotEnoughMoney"));
             PvPRewardMessages.setKillerNotEnoughMoneyMsg(loadValue("KillerNotEnoughMoney"));
             PvPRewardMessages.setOutLawBroadcast(loadValue("OutlawBroadcast"));
             PvPRewardMessages.setNoLongerOutLawBroadcast(loadValue("NoLongerOutlawBroadcast"));
             PvPRewardMessages.setKarmaDecreasedMsg(loadValue("KarmaDecreased"));
             PvPRewardMessages.setKarmaIncreasedMsg(loadValue("KarmaIncreased"));
             PvPRewardMessages.setKarmaNoChangeMsg(loadValue("KarmaNoChange"));
 
             String tollType = loadValue("DeathTollType");
             if (tollType.equalsIgnoreCase("none")) {
                 Rewarder.tollAsPercent = false;
                 Rewarder.tollAmount = 0;
             }
             else {
                 PvPRewardMessages.setDeathTollMsg(loadValue("DeathTollMessage"));
                 Rewarder.tollAmount = Double.parseDouble(loadValue("DeathToll"));
                 PvPRewardListener.disableTollForPvP = Boolean.parseBoolean(loadValue("DisableTollForPvP"));
 
                 if (tollType.equalsIgnoreCase("percent")) 
                     Rewarder.tollAsPercent = true;
                 else if (tollType.equalsIgnoreCase("flatrate")) {
                     Rewarder.tollAsPercent = false;
                     Rewarder.tollAmount = Double.parseDouble(loadValue("DeathToll"));
                 } 
             }
             
             PvPRewardListener.disableTollForPvP = Boolean.parseBoolean(loadValue("DisableTollForPvP"));
             PvPRewardListener.digGraves = Boolean.parseBoolean(loadValue("DigGraves"));
 
             PvPRewardMessages.setDenyTeleMsg(loadValue("DenyTeleMessage"));
             PvPRewardListener.denyTele = Boolean.parseBoolean(loadValue("DenyTele"));
             PvPRewardListener.telePenalty = Integer.parseInt(loadValue("TelePenalty"));
             PvPRewardListener.penalizeLoggers = Boolean.parseBoolean(loadValue("PenalizeLoggers"));
 
             Record.combatTimeOut = Integer.parseInt(loadValue("CombatTime")) * 1000;
             Record.graveTimeOut = Integer.parseInt(loadValue("GraveTime")) * 1000;
             PvPRewardMessages.setGraveRobMsg(loadValue("GraveRobMessage"));
 
             Record.outlawTag = PvPRewardMessages.format(loadValue("OutlawTag"));
             karmaName = loadValue("KarmaName");
             outlawName = loadValue("OutlawName");
             cooldownTime = Integer.parseInt(loadValue("CooldownTime")) * 20;
 
            Rewarder.rewardType = RewardType.valueOf(loadValue("RewardType").toUpperCase().replace(' ', '_'));
             Rewarder.percent = Integer.parseInt(loadValue("Percent"));
             
             Rewarder.amount = Double.parseDouble(loadValue("Amount"));
             Record.outlawLevel = (int)Rewarder.amount;
             
             Rewarder.hi = Integer.parseInt(loadValue("High"));
             Rewarder.lo = Integer.parseInt(loadValue("Low"));
 
             Rewarder.threshold = Integer.parseInt(loadValue("KarmaThreshold"));
             Rewarder.modifier = Integer.parseInt(loadValue("OutlawModifier")) / 100;
             Rewarder.max = Integer.parseInt(loadValue("ModifierMax")) / 100;
             Rewarder.whole = Boolean.parseBoolean(loadValue("WholeNumbers"));
 
             negative = Boolean.parseBoolean(loadValue("Negative"));
             
             Rewarder.tollDisabledIn = new LinkedList<String>
                     (Arrays.asList(loadValue("DisableDeathTollInWorlds").split(", ")));
             PvPRewardListener.rewardDisabledIn = new LinkedList<String>
                     (Arrays.asList(loadValue("DisableRewardInWorlds").split(", ")));
             
             Record.outlawGroup = loadValue("OutlawGroup");
             Record.removeGroup = Boolean.parseBoolean(loadValue("RemoveFromCurrentGroup"));
             
             fis.close();
         }
         catch (Exception missingProp) {
             System.err.println("Failed to load PvPReward "+this.getDescription().getVersion());
             missingProp.printStackTrace();
         }
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
             System.err.println("[PvPReward] Missing value for "+key+" in config file");
             System.err.println("[PvPReward] Please regenerate config file");
         }
 
         return p.getProperty(key);
     }
 
     /**
      * Returns false if either Player does not have proper permission
      * killer will be checked for "pvpreward.getreward"
      * deaded will be checked for "pvpreward.givereward"
      * 
      * @param killer The Player who would receive the reward
      * @param deaded The Player who the reward would be taken from
      * @return true both Players have proper permission
      */
     public static boolean hasPermisson(Player killer, Player deaded) {
         return permission.has(killer, "pvpreward.getreward") && permission.has(deaded, "pvpreward.givereward");
     }
     
     /**
      * Returns true the given Player has the given Permission node
      * 
      * @param player The Player who would receive the reward
      * @param node The String of the node
      * @return true if either Player does not have proper permission
      */
     public static boolean hasPermisson(Player player, String node) {
         return permission.has(player, "pvpreward."+node);
     }
     
     /**
      * Subtracts 1 karma from all online Players' Records
      * This is repeated on an interval of the assigned cooldownTime
      */
     public void cooldown() {
     	server.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
             @Override
     	    public void run() {
                 for (Record record: records.values()) {
                     //Check if the Player is online
                     Player player = server.getPlayer(record.name);
                     if (player != null)
                         record.decrementKarma(player);
                 }
 
                 save();
     	    }
     	}, 0L, new Long(cooldownTime));
     }
 
     /**
      * Reads save file to load PvPReward data
      * Saving is turned off if an error occurs
      */
     private static void loadData() {
         try {
             File file = new File(dataFolder+"/pvpreward.records");
             if (!file.exists()) {
                 File old = new File(dataFolder+"/pvpreward.save");
                 if (old.exists())
                     old.renameTo(file);
                 else
                     return;
             }
             
             BufferedReader bReader = new BufferedReader(new FileReader(file));
             String line = bReader.readLine();
             while (line != null) {
                 try {
                     String[] split = line.split(";");
 
                     String player = split[0];
                     int kills = Integer.parseInt(split[1]);
                     int deaths = Integer.parseInt(split[2]);
                     int karma = Integer.parseInt(split[3]);
 
                     Record record = new Record(player, kills, deaths, karma);
                     records.put(player, record);
 
                     if (split.length == 5)
                         record.group = split[4];
 
                     line = bReader.readLine();
                 }
                 catch (Exception corruptedData) {
                     /* Do not load line */
                 }
             }
             
             bReader.close();
         }
         catch (Exception loadFailed) {
             System.out.println("[PvPReward] Load failed");
             loadFailed.printStackTrace();
         }
     }
 
     /**
      * Writes data to save file
      * Old file is overwritten
      */
     public static void save() {
         try {
             File file = new File(dataFolder+"/pvpreward.records");
             if (!file.exists())
                 file.createNewFile();
             
             BufferedWriter bWriter = new BufferedWriter(new FileWriter(dataFolder+"/pvpreward.records"));
             for (Record record: records.values()) {
                 //Write data in the format "name;kills;deaths;karma(;group)"
                 bWriter.write(record.name.concat(";"));
                 bWriter.write(record.kills+";");
                 bWriter.write(record.deaths+";");
                 bWriter.write(String.valueOf(record.karma));
                 
                 if (record.group != null)
                     bWriter.write(";"+record.group);
                 
                 //Write each Record on a new line
                 bWriter.newLine();
             }
 
             bWriter.close();
         }
         catch (Exception saveFailed) {
             System.err.println("[PvPReward] Save Failed!");
             saveFailed.printStackTrace();
         }
     }
 
     /**
      * Returns the Record for the given Player
      * A new Record is created if one is not found
      * 
      * @param player The name of the Player
      * @return The Record of the Player
      */
     public static Record getRecord(String player) {
         for (Record record: records.values())
             if (record.name.equals(player))
                 return record;
 
         //Create a new Record
         Record newRecord = new Record(player);
         records.put(player, newRecord);
         return newRecord;
     }
 
     /**
      * Returns the Record for the given Player
      * 
      * @param player The name of the Player
      * @return The Record of the Player
      */
     public static Record findRecord(String player) {
         for (Record record: records.values())
             if (record.name.equals(player))
                 return record;
 
         //Return null because the Player does not have a Record
         return null;
     }
 
     /**
      * Returns a LinkedList of records in sorted order
      * Records are sorted in order of rank (highest KDR first)
      * 
      * @return The sorted LinkedList of records
      */
     public static LinkedList<Record> getRecords() {
         LinkedList<Record> recordList = new LinkedList(records.values());
         Collections.sort(recordList);
         return recordList;
     }
 }
