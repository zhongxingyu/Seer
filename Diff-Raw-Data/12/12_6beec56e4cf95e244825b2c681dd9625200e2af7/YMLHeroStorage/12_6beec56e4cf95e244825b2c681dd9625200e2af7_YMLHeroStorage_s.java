 package com.herocraftonline.dev.heroes.persistence;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.logging.Level;
 
 import org.bukkit.Material;
 import org.bukkit.entity.Player;
 import org.bukkit.util.config.Configuration;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.classes.HeroClass;
 import com.herocraftonline.dev.heroes.command.CommandHandler;
 import com.herocraftonline.dev.heroes.hero.Hero;
 
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
         // Check if it already exists, if so we load the data.
         if (playerFile.exists()) {
             Configuration playerConfig = new Configuration(playerFile); // Setup the Configuration
             playerConfig.load(); // Load the Config File
 
             HeroClass playerClass = loadClass(player, playerConfig);
             if (playerClass == null) {
                 Heroes.log(Level.INFO, "Invalid class found for " + player.getName() + ". Resetting player.");
                 return createNewHero(player);
             }
 
             Hero playerHero = new Hero(plugin, player, playerClass);
             playerHero.setSecondClass(loadSecondaryClass(player, playerConfig));
             loadCooldowns(playerHero, playerConfig);
             loadExperience(playerHero, playerConfig);
             loadBinds(playerHero, playerConfig);
             loadSkillSettings(playerHero, playerConfig);
             playerHero.setMana(playerConfig.getInt("mana", 0));
             playerHero.setHealth(playerConfig.getDouble("health", playerClass.getBaseMaxHealth()));
             playerHero.setVerbose(playerConfig.getBoolean("verbose", true));
             playerHero.setSuppressedSkills(new HashSet<String>(playerConfig.getStringList("suppressed", null)));
 
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
         File playerFile = new File(playerFolder, name + ".yml");
         Configuration playerConfig = new Configuration(playerFile);
 
         playerConfig.setProperty("class", hero.getHeroClass().toString());
         if (hero.getSecondClass() != null) {
             playerConfig.setProperty("secondary-class", hero.getSecondClass().toString());
         }
         playerConfig.setProperty("verbose", hero.isVerbose());
         playerConfig.setProperty("suppressed", new ArrayList<String>(hero.getSuppressedSkills()));
         playerConfig.setProperty("mana", hero.getMana());
         playerConfig.removeProperty("itemrecovery");
         playerConfig.setProperty("health", hero.getHealth());
 
         saveSkillSettings(hero, playerConfig);
         saveCooldowns(hero, playerConfig);
         saveExperience(hero, playerConfig);
         saveBinds(hero, playerConfig);
 
         playerConfig.save();
         return true;
     }
 
     /**
      * Loads a hero's bindings
      * 
      * @param hero
      * @param config
      */
     private void loadBinds(Hero hero, Configuration config) {
         List<String> bindKeys = config.getKeys("binds");
         if (bindKeys != null && bindKeys.size() > 0) {
             for (String material : bindKeys) {
                 try {
                     Material item = Material.valueOf(material);
                     String bind = config.getString("binds." + material, "");
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
             } else if (!CommandHandler.hasPermission(player, "heroes.classes." + playerClass.getName().toLowerCase())) {
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
        HeroClass defaultClass = plugin.getClassManager().getDefaultClass();
 
         if (config.getString("secondary-class") != null) {
             playerClass = plugin.getClassManager().getClass(config.getString("secondary-class"));
 
            if (playerClass == null) {
                playerClass = defaultClass;
            } else if (!CommandHandler.hasPermission(player, "heroes.classes." + playerClass.getName().toLowerCase())) {
                playerClass = defaultClass;
            }
        } else {
            playerClass = defaultClass;
         }
         return playerClass;
     }
 
     /**
      * Loads the hero's saved cooldowns
      * 
      * @param hero
      * @param config
      */
     private void loadCooldowns(Hero hero, Configuration config) {
         HeroClass heroClass = hero.getHeroClass();
 
         String path = "cooldowns";
         List<String> storedCooldowns = config.getKeys(path);
         if (storedCooldowns != null) {
             long time = System.currentTimeMillis();
             for (String skillName : storedCooldowns) {
                 long cooldown = (long) config.getDouble(path + "." + skillName, 0);
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
      * @param config
      */
     private void loadExperience(Hero hero, Configuration config) {
         if (hero == null || hero.getClass() == null || config == null)
             return;
 
         String root = "experience";
         List<String> expList = config.getKeys(root);
         if (expList != null) {
             for (String className : expList) {
                 double exp = config.getDouble(root + "." + className, 0);
                 HeroClass heroClass = plugin.getClassManager().getClass(className);
                 if (heroClass == null || hero.getExperience(heroClass) != 0) {
                     continue;
                 }
 
                 hero.setExperience(heroClass, exp);
                 /*
                  * We shouldn't be needing to alter XP values when a player loads back
                  * this causes confusion/issues on how XP is determined.
                  * if (!heroClass.isPrimary() && exp > 0) {
                  * HeroClass parent = heroClass.getParent();
                  * hero.setExperience(parent, plugin.getConfigManager().getProperties().levels[parent.getMaxLevel()]);
                  * }
                  */
             }
         }
     }
 
     /**
      * Loads hero-specific Skill settings
      * 
      * @param hero
      * @param config
      */
     private void loadSkillSettings(Hero hero, Configuration config) {
         String path = "skill-settings";
 
         if (config.getKeys(path) != null) {
             for (String skill : config.getKeys(path)) {
                 if (config.getNode(path).getKeys(skill) != null) {
                     for (String node : config.getNode(path).getKeys(skill)) {
                         hero.setSkillSetting(skill, node, config.getNode(path).getNode(skill).getString(node));
                     }
                 }
             }
         }
     }
 
     private void saveBinds(Hero hero, Configuration config) {
         config.removeProperty("binds");
         Map<Material, String[]> binds = hero.getBinds();
         for (Material material : binds.keySet()) {
             String[] bindArgs = binds.get(material);
             StringBuilder bind = new StringBuilder();
             for (String arg : bindArgs) {
                 bind.append(arg).append(" ");
             }
             config.setProperty("binds." + material.toString(), bind.toString().substring(0, bind.toString().length() - 1));
         }
     }
 
     private void saveCooldowns(Hero hero, Configuration config) {
         String path = "cooldowns";
         long time = System.currentTimeMillis();
         Map<String, Long> cooldowns = hero.getCooldowns();
         for (Map.Entry<String, Long> entry : cooldowns.entrySet()) {
             String skillName = entry.getKey();
             long cooldown = entry.getValue();
             if (cooldown > time) {
                 System.out.println(path + "." + skillName);
                 config.setProperty(path + "." + skillName, cooldown);
             }
         }
     }
 
     private void saveExperience(Hero hero, Configuration config) {
         if (hero == null || hero.getClass() == null || config == null)
             return;
 
         String root = "experience";
         Map<String, Double> expMap = hero.getExperienceMap();
         for (Map.Entry<String, Double> entry : expMap.entrySet()) {
             config.setProperty(root + "." + entry.getKey(), (double) entry.getValue());
         }
     }
 
     private void saveSkillSettings(Hero hero, Configuration config) {
         String path = "skill-settings";
         for (Entry<String, Map<String, String>> entry : hero.getSkillSettings().entrySet()) {
             for (Entry<String, String> node : entry.getValue().entrySet()) {
                 config.setProperty(path + "." + entry.getKey() + "." + node.getKey(), node.getValue());
             }
         }
 
     }
 }
