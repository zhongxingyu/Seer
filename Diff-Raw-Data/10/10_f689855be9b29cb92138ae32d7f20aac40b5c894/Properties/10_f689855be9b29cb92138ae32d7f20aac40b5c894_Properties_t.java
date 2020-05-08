 package com.herocraftonline.dev.heroes.util;
 
 import java.util.EnumMap;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.entity.CreatureType;
 import org.bukkit.entity.Player;
 
 import com.herocraftonline.dev.heroes.Heroes;
 
 public class Properties {
 
     // Leveling //
     public double power;
     public static int maxExp;
     public static int maxLevel;
     public static int[] levels;
     public double expLoss;
     public double pvpExpLossMultiplier = 0;
     public boolean levelsViaExpLoss = false;
     public boolean masteryLoss = false;
     public double partyBonus = 0;
     public double playerKillingExp = 0;
     public boolean noSpawnCamp = false;
     public int spawnCampRadius;
     public double spawnCampExpMult;
     public boolean resetOnDeath;
     public int pvpLevelRange = 50;
     public boolean orbExp;
     
     // Classes //
     public int swapCost;
     public int oldClassSwapCost;
     public boolean firstSwitchFree;
     public boolean swapMasteryCost;
     public boolean prefixClassName;
     public boolean resetExpOnClassChange = true;
     public boolean resetMasteryOnClassChange = false;
     public boolean resetProfMasteryOnClassChange = false;
     public boolean resetProfOnPrimaryChange = false;
     public boolean lockPathTillMaster = false;
     public boolean lockAtHighestTier = false;
     
     //Properties
     public boolean debug;
     public String storageType;
     public boolean iConomy;
     public int blockTrackingDuration;
     public int maxTrackedBlocks;
     public double foodHealPercent = .05;
     public int globalCooldown = 0;
     public double enchantXPMultiplier;
 
     // Bed Stuffs
     public boolean bedHeal;
     public int healInterval;
     public int healPercent;
 
     // Mana stuff
     public int manaRegenPercent;
     public int manaRegenInterval;
 
     // Map Stuffs
     public boolean mapUI;
     public byte mapID;
     public int mapPacketInterval;
 
     // Hats...
     public int hatsLevel;
     public boolean allowHats;
 
     // Worlds
     public Set<String> disabledWorlds = new HashSet<String>();
     
     public Map<CreatureType, Double> creatureKillingExp = new EnumMap<CreatureType, Double>(CreatureType.class);
     public Map<Material, Double> miningExp = new EnumMap<Material, Double>(Material.class);
     public Map<Material, Double> farmingExp = new EnumMap<Material, Double>(Material.class);
     public Map<Material, Double> loggingExp = new EnumMap<Material, Double>(Material.class);
     public Map<Material, Double> craftingExp = new EnumMap<Material, Double>(Material.class);
     public Map<String, String> skillInfo = new HashMap<String, String>();
     public Map<Player, Location> playerDeaths = new HashMap<Player, Location>();
     public Map<String, RecipeGroup> recipes = new HashMap<String, RecipeGroup>();
     public double fishingExp = 0;
     
     public void load(Heroes plugin) {
         FileConfiguration config = plugin.getConfig();
         config.options().copyDefaults(true);
         plugin.saveConfig();
         
         // Load in the data
         loadLevelConfig(config.getConfigurationSection("leveling"));
         loadClassConfig(config.getConfigurationSection("classes"));
         loadProperties(config.getConfigurationSection("properties"));
         loadManaConfig(config.getConfigurationSection("mana"));
         loadMapConfig(config.getConfigurationSection("mappartyui"));
         loadBedConfig(config.getConfigurationSection("bed"));
         loadWorldConfig(config.getConfigurationSection("worlds"));
         loadHatsConfig(config.getConfigurationSection("hats"));
     }
     
     private void loadBedConfig(ConfigurationSection section) {
         if (section == null)
             return;
         bedHeal = section.getBoolean("bedHeal", true);
         healInterval = section.getInt("healInterval", 30);
         healPercent = section.getInt("healPercent", 5);
     }
 
     private void loadHatsConfig(ConfigurationSection section) {
         hatsLevel = section.getInt("level", 1);
         allowHats = section.getBoolean("allowhatsplugin", false);
     }
 
     private void loadLevelConfig(ConfigurationSection section) {
         if (section == null)
             return;
         power = section.getDouble("power", 1.00);
         maxExp = section.getInt("maxExperience", 100000);
         maxLevel = section.getInt("maxLevel", 20);
         partyBonus = section.getDouble("partyBonus", 0.20);
         expLoss = section.getDouble("expLoss", 0.05);
         pvpExpLossMultiplier = section.getDouble("pvpExpLossMultiplier", 1.0);
         levelsViaExpLoss = section.getBoolean("levelsViaExpLoss", false);
         masteryLoss = section.getBoolean("mastery-loss", false);
         noSpawnCamp = section.getBoolean("noSpawnCamp", false);
         spawnCampRadius = section.getInt("spawnCampRadius", 7);
         spawnCampExpMult = section.getDouble("spawnCampExpMult", .5);
         resetOnDeath = section.getBoolean("resetOnDeath", false);
         pvpLevelRange = section.getInt("pvpLevelRange", 50);
         calcExp();
     }
     
     private void loadClassConfig(ConfigurationSection section) {
         if (section == null)
             return;
         prefixClassName = section.getBoolean("prefixClassName", false);
         resetExpOnClassChange = section.getBoolean("resetExpOnClassChange", true);
         resetMasteryOnClassChange = section.getBoolean("resetMasteryOnClassChange", false);
         resetProfMasteryOnClassChange = section.getBoolean("resetProfMasteryOnClassChange", false);
         resetProfOnPrimaryChange = section.getBoolean("resetProfOnPrimaryChange", false);
         lockPathTillMaster = section.getBoolean("lockPathTillMaster", false);
         lockAtHighestTier = section.getBoolean("lockAtHighestTier", false);
         swapCost = section.getInt("swapcost", 0);
         swapMasteryCost = section.getBoolean("swapMasteryCost", false);
         oldClassSwapCost = section.getInt("oldClassSwapCost", 0);
         firstSwitchFree = section.getBoolean("firstSwitchFree", true);
     }
 
     private void loadManaConfig(ConfigurationSection section) {
         if (section == null)
             return;
         manaRegenInterval = section.getInt("regenInterval", 5);
         manaRegenPercent = section.getInt("regenPercent", 5);
         // Out of bounds check
         if (manaRegenPercent > 100 || manaRegenPercent < 0) {
             manaRegenPercent = 5;
         }
     }
 
     private void loadMapConfig(ConfigurationSection section) {
         if (section == null)
             return;
         mapUI = section.getBoolean("enabled", false);
         mapID = (byte) section.getInt("id", 0);
         mapPacketInterval = section.getInt("packetinterval", 20);
     }
     
     private void loadProperties(ConfigurationSection section) {
         if (section == null)
             return;
         storageType = section.getString("storage-type");
         iConomy = section.getBoolean("iConomy", false);
         debug = section.getBoolean("debug", false);
        foodHealPercent = Util.toDouble(section.get("foodHealPercent", .05));
        globalCooldown = Util.toInt(section.get("globalCooldown", 1));
        blockTrackingDuration = Util.toInt(section.get("block-tracking-duration", 10 * 60 * 1000));
        maxTrackedBlocks = Util.toInt(section.get("max-tracked-blocks", 1000));
        enchantXPMultiplier = Util.toDouble(section.get("enchant-exp-mult", 1));
     }
 
     private void loadWorldConfig(ConfigurationSection section) {
         if (section == null)
             return;
         List<String> worlds = section.getStringList("disabledWorlds");
         disabledWorlds.addAll(worlds);
     }
     
     /**
      * Generate experience for the level ArrayList<Integer>
      */
     protected void calcExp() {
         levels = new int[maxLevel + 1];
 
         double A = maxExp * Math.pow(maxLevel - 1, -(power + 1));
         for (int i = 0; i < maxLevel; i++) {
             levels[i] = (int) (A * Math.pow(i, power + 1));
         }
         levels[maxLevel - 1] = maxExp;
         levels[maxLevel] = (int) (A * Math.pow(maxLevel + 1, power + 1));
     }
 
     public static int getExperience(int level) {
         if (level >= levels.length)
             return levels[levels.length - 1];
         else if (level < 1)
             return levels[0];
 
         return levels[level - 1];
     }
 
     /**
      * Convert the given Exp into the correct Level.
      * 
      * @param exp
      * @return
      */
     public static int getLevel(double exp) {
         for (int i = maxLevel - 1; i >= 0; i--) {
             if (exp >= levels[i])
                 return i + 1;
         }
         return -1;
     }
 }
