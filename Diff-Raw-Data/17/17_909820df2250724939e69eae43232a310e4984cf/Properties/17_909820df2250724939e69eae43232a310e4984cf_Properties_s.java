 package com.herocraftonline.dev.heroes.util;
 
 import java.util.HashMap;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.entity.CreatureType;
 
 public class Properties {
 
     // Debug Mode //
     public boolean debug;
 
     // Persistence //
     public String host;
     public String port;
     public String database;
     public String username;
     public String password;
     public String method;
 
     // Leveling//
     public double power;
     public int baseExp;
     public int maxExp;
     public int maxLevel;
     public int[] levels;
     public int classSwitchLevel;
     // Experience//
     public int playerKillingExp = 0;
     public HashMap<CreatureType, Integer> creatureKillingExp = new HashMap<CreatureType, Integer>();
     public HashMap<Material, Integer> miningExp = new HashMap<Material, Integer>();
     public HashMap<Material, Integer> loggingExp = new HashMap<Material, Integer>();
 
     // Skills //
 
     public enum Skills {
         BLACKJACK,
         BLADEGRASP,
         HARMTOUCH,
         LAYHANDS,
         SUMMON,
         TAME,
         TRACK,
         JUMP
     }
 
    public HashMap<String, Integer> skillInfo;
     // Default//
     public String defClass;
     public int defLevel;
     // Properties//
     public boolean iConomy;
     public ChatColor cColor;
     public String prefix;
     public int swapcost;
 
     /**
      * Generate experience for the level ArrayList<Integer>
      */
     public void calcExp() {
         levels = new int[maxLevel];
         double A = 2 * (maxExp - baseExp) * Math.pow(maxLevel, (power * -1));
         for (int n = 0; n < maxLevel; n++) {
             levels[n] = (int) (A / 2 * Math.pow(n, power) + baseExp);
         }
     }
 
     /**
      * Convert the given Exp into the correct Level.
      * @param exp
      * @return
      */
     public int getLevel(Integer exp) {
         int n = 0;
         for (Integer i : levels) {
             if (!(exp >= i)) {
                 return n;
             }
             n++;
         }
         return -1;
     }
 
     public Skills[] getSkills() {
         return Skills.values();
     }
 }
