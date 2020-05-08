 package com.herocraftonline.dev.heroes.persistence;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 import java.util.logging.Level;
 
 import org.bukkit.Material;
 import org.bukkit.configuration.Configuration;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.classes.HeroClass;
 import com.herocraftonline.dev.heroes.hero.Hero;
import com.herocraftonline.dev.heroes.util.Properties;
 import com.herocraftonline.dev.heroes.util.Util;
 
 public class YMLHeroStorage extends HeroStorage {
 
     private File playerFolder;
 
     public YMLHeroStorage(Heroes plugin) {
         super(plugin);
         playerFolder = new File(plugin.getDataFolder(), "players"); // Setup our Player Data Folder
         playerFolder.mkdirs(); // Create the folder if it doesn't exist.
     }
 
     @Override
     public Hero loadHero(Player player) {
         File playerFile = new File(playerFolder, player.getName() + ".yml"); // Setup our Players Data File.
         File pFolder = new File(playerFolder + File.separator + player.getName().toLowerCase().substring(0, 1));
         pFolder.mkdirs();
         File newPlayerFile = new File(pFolder, player.getName() + ".yml");
 
         if (newPlayerFile.exists())
             playerFile = newPlayerFile;
         else if (playerFile.exists()) {
             Util.moveFile(playerFile, newPlayerFile);
             playerFile = newPlayerFile;
         } else {
             playerFile = newPlayerFile;
         }
         // Check if it already exists, if so we load the data.
         if (playerFile.exists()) {
             Configuration playerConfig = YamlConfiguration.loadConfiguration(playerFile); // Setup the Configuration
 
             HeroClass playerClass = loadClass(player, playerConfig);
             if (playerClass == null) {
                 Heroes.log(Level.INFO, "Invalid class found for " + player.getName() + ". Resetting player.");
                 return createNewHero(player);
             }
             HeroClass secondClass = loadSecondaryClass(player, playerConfig);
             Hero playerHero = new Hero(plugin, player, playerClass, secondClass);
 
             loadCooldowns(playerHero, playerConfig.getConfigurationSection("cooldowns"));
             loadExperience(playerHero, playerConfig.getConfigurationSection("experience"));
             loadBinds(playerHero, playerConfig.getConfigurationSection("binds"));
             loadSkillSettings(playerHero, playerConfig.getConfigurationSection("skill-settings"));
             playerHero.setMana(playerConfig.getInt("mana", 0));
             playerHero.setHealth(playerConfig.getDouble("health", playerClass.getBaseMaxHealth()));
             playerHero.setVerbose(playerConfig.getBoolean("verbose", true));
             playerHero.setSuppressedSkills(new HashSet<String>(playerConfig.getStringList("suppressed")));
 
             Heroes.log(Level.INFO, "Loaded hero: " + player.getName() + " with EID: " + player.getEntityId());
             return playerHero;
         } else {
             // Create a New Hero with the Default Setup.
             Heroes.log(Level.INFO, "Created hero: " + player.getName());
             return createNewHero(player);
         }
     }
 
     @Override
     public boolean saveHero(Hero hero) {
         String name = hero.getPlayer().getName();
         File playerFile = new File(playerFolder + File.separator + name.substring(0, 1).toLowerCase(), name + ".yml");
         FileConfiguration playerConfig = new YamlConfiguration();
 
         playerConfig.set("class", hero.getHeroClass().toString());
         if (hero.getSecondClass() != null) {
             playerConfig.set("secondary-class", hero.getSecondClass().toString());
         }
         playerConfig.set("verbose", hero.isVerbose());
         playerConfig.set("suppressed", new ArrayList<String>(hero.getSuppressedSkills()));
         playerConfig.set("mana", hero.getMana());
         playerConfig.set("itemrecovery", null);
         playerConfig.set("health", hero.getHealth());
 
 
 
         saveSkillSettings(hero, playerConfig.createSection("skill-settings"));
         saveCooldowns(hero, playerConfig.createSection("cooldowns"));
         saveExperience(hero, playerConfig.createSection("experience"));
         saveBinds(hero, playerConfig.createSection("binds"));
 
         try {
             playerConfig.save(playerFile);
         } catch (IOException e) {
             e.printStackTrace();
             return false;
         }
         return true;
     }
 
     /**
      * Loads a hero's bindings
      * 
      * @param hero
      * @param section
      */
     private void loadBinds(Hero hero, ConfigurationSection section) {
         if (section == null)
             return;
 
         Set<String> bindKeys = section.getKeys(false);
         if (bindKeys != null && bindKeys.size() > 0) {
             for (String material : bindKeys) {
                 try {
                     Material item = Material.valueOf(material);
                     String bind = section.getString(material, "");
                     if (bind.length() > 0) {
                         hero.bind(item, bind.split(" "));
                     }
                 } catch (IllegalArgumentException e) {
                     this.plugin.debugLog(Level.WARNING, material + " isn't a valid Item to bind a Skill to.");
                     continue;
                 }
             }
         }
     }
 
     /**
      * Loads a players class, checks to make sure the class still exists and the player still has permission for the
      * class
      * 
      * @param player
      * @param config
      * @return
      */
     private HeroClass loadClass(Player player, Configuration config) {
         HeroClass playerClass = null;
         HeroClass defaultClass = plugin.getClassManager().getDefaultClass();
 
         if (config.getString("class") != null) {
             playerClass = plugin.getClassManager().getClass(config.getString("class"));
 
             if (playerClass == null) {
                 playerClass = defaultClass;
             } else if (!playerClass.isPrimary()) {
                 playerClass = defaultClass;
             }
         } else {
             playerClass = defaultClass;
         }
         return playerClass;
     }
 
     /**
      * Loads a players class, checks to make sure the class still exists and the player still has permission for the
      * class
      * 
      * @param player
      * @param config
      * @return
      */
     private HeroClass loadSecondaryClass(Player player, Configuration config) {
         HeroClass playerClass = null;
 
         if (config.getString("secondary-class") != null) {
             playerClass = plugin.getClassManager().getClass(config.getString("secondary-class"));
             if (!playerClass.isSecondary())
                 playerClass = null;
         }
         return playerClass;
     }
 
     /**
      * Loads the hero's saved cooldowns
      * 
      * @param hero
      * @param section
      */
     private void loadCooldowns(Hero hero, ConfigurationSection section) {
         if (section == null)
             return;
 
         HeroClass heroClass = hero.getHeroClass();
 
         Set<String> storedCooldowns = section.getKeys(false);
         if (storedCooldowns != null) {
             long time = System.currentTimeMillis();
             for (String skillName : storedCooldowns) {
                 long cooldown = (long) section.getDouble(skillName, 0);
                 if (heroClass.hasSkill(skillName) && cooldown > time) {
                     hero.setCooldown(skillName, cooldown);
                 }
             }
         }
     }
 
     /**
      * Loads the Hero's experience
      * 
      * @param hero
      * @param section
      */
     private void loadExperience(Hero hero, ConfigurationSection section) {
         if (hero == null || hero.getClass() == null || section == null)
             return;
 
         Set<String> expList = section.getKeys(false);
         if (expList != null) {
             for (String className : expList) {
                 double exp = section.getDouble(className, 0);
                 HeroClass heroClass = plugin.getClassManager().getClass(className);
                 if (heroClass == null || hero.getExperience(heroClass) != 0) {
                     continue;
                 }
                //Don't go above max exp
                if (exp > Properties.maxExp) {
                    exp = Properties.maxExp;
                }
                 hero.setExperience(heroClass, exp);
             }
         }
     }
 
     /**
      * Loads hero-specific Skill settings
      * TODO: Rewrite-me
      * @param hero
      * @param section
      */
     private void loadSkillSettings(Hero hero, ConfigurationSection section) {
         if (section == null || section.getKeys(false) == null)
             return;
         
         for (String skill : section.getKeys(false)) {
             if (section.isConfigurationSection(skill)) {
                 for (String node : section.getConfigurationSection(skill).getKeys(false)) {
                     hero.setSkillSetting(skill, node, section.getConfigurationSection(skill).getString(node));
                 }
             }
         }
     }
 
     private void saveBinds(Hero hero, ConfigurationSection section) {
         if (section == null)
             return;
 
         Map<Material, String[]> binds = hero.getBinds();
         for (Material material : binds.keySet()) {
             String[] bindArgs = binds.get(material);
             StringBuilder bind = new StringBuilder();
             for (String arg : bindArgs) {
                 bind.append(arg).append(" ");
             }
             section.set(material.toString(), bind.toString().substring(0, bind.toString().length() - 1));
         }
     }
 
     private void saveCooldowns(Hero hero, ConfigurationSection section) {
         if (section == null)
             return;
 
         long time = System.currentTimeMillis();
         Map<String, Long> cooldowns = hero.getCooldowns();
         for (Map.Entry<String, Long> entry : cooldowns.entrySet()) {
             String skillName = entry.getKey();
             long cooldown = entry.getValue();
             if (cooldown > time) {
                 section.set(skillName, cooldown);
             }
         }
     }
 
     private void saveExperience(Hero hero, ConfigurationSection section) {
         if (hero == null || hero.getClass() == null || section == null)
             return;
 
         Map<String, Double> expMap = hero.getExperienceMap();
         for (Map.Entry<String, Double> entry : expMap.entrySet()) {
             section.set(entry.getKey(), (double) entry.getValue());
         }
     }
 
     //TODO: this needs to be re-written to jut be a dump
     private void saveSkillSettings(Hero hero, ConfigurationSection config) {
         for (Entry<String, Map<String, String>> entry : hero.getSkillSettings().entrySet()) {
             for (Entry<String, String> node : entry.getValue().entrySet()) {
                 config.set(entry.getKey() + "." + node.getKey(), node.getValue());
             }
         }
     }
 }
