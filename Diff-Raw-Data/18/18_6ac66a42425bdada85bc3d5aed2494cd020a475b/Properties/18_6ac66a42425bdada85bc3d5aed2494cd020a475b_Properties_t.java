 package com.herocraftonline.dev.heroes.util;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.entity.CreatureType;
 import org.bukkit.entity.Player;
 
 public class Properties {
 
     // Debug Mode //
     public boolean debug;
 
     // Leveling//
     public double power;
     public int maxExp;
     public int maxLevel;
     public int[] levels;
     public double expLoss;
     public boolean levelsViaExpLoss = false;
     public boolean masteryLoss = false;
     
     // Experience//
     public double partyBonus = 0;
     public boolean resetExpOnClassChange = true;
     public int blockTrackingDuration;
     public int maxTrackedBlocks;
     public double playerKillingExp = 0;
     public boolean noSpawnCamp = false;
     public int spawnCampRadius;
     public Map<CreatureType, Double> creatureKillingExp = new HashMap<CreatureType, Double>();
     public Map<Material, Double> miningExp = new HashMap<Material, Double>();
     public Map<Material, Double> loggingExp = new HashMap<Material, Double>();
     public Map<Material, Double> craftingExp = new HashMap<Material, Double>();
     public Map<String, String> skillInfo = new HashMap<String, String>();
     public Map<Player, Location> playerDeaths = new HashMap<Player, Location>();
 
     // Default//
     public String defClass;
     public int defLevel;
     public boolean resetOnDeath;
     public int globalCooldown = 0;
     public int pvpLevelRange = 50;
 
     // Properties//
     public boolean iConomy;
     public ChatColor cColor;
     public String prefix;
     public int swapCost;
     public boolean firstSwitchFree;
     public boolean swapMasteryCost;
     public boolean damageSystem;
 
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
 
     // Worlds
     public Set<String> disabledWorlds = new HashSet<String>();
 
     // Stupid Hats...
     public boolean allowHats;
 
     // Prefix ClassName
     public boolean prefixClassName;
 
     /**
      * Generate experience for the level ArrayList<Integer>
      */
     public void calcExp() {
         levels = new int[maxLevel + 1];
 
         double A = maxExp * Math.pow(maxLevel - 1, -(power + 1));
         for (int i = 0; i < maxLevel; i++) {
             levels[i] = (int) (A * Math.pow(i, power + 1));
         }
         levels[maxLevel - 1] = maxExp;
         levels[maxLevel] = (int) (A * Math.pow(maxLevel, power + 1));
     }
 
     public int getExperience(int level) {
        if (level >= levels.length) {
             return levels[levels.length - 1];
         } else if (level < 1) {
             return levels[0];
         }
         
         return levels[level - 1];
     }
 
     /**
      * Convert the given Exp into the correct Level.
      * 
      * @param exp
      * @return
      */
     public int getLevel(double exp) {
         for (int i = maxLevel - 1; i >= 0; i--) {
             if (exp >= levels[i]) {
                 return i + 1;
             }
         }
         return -1;
     }
 }
