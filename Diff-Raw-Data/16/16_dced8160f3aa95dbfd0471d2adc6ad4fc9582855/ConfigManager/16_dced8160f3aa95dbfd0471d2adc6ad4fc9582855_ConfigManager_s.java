 package com.herocraftonline.dev.heroes.util;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.logging.Level;
 
 import org.bukkit.Material;
 import org.bukkit.configuration.Configuration;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.CreatureType;
 import org.bukkit.entity.Player;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.classes.HeroClassManager;
 import com.herocraftonline.dev.heroes.hero.Hero;
 import com.herocraftonline.dev.heroes.skill.SkillConfigManager;
 
 public class ConfigManager {
 
     protected final Heroes plugin;
     // Files
     protected static File classConfigFolder;
     protected static File expConfigFile;
     protected static File damageConfigFile;
     protected static File recipesConfigFile;
 
     //Configurations
     private static Configuration damageConfig;
     private static Configuration expConfig;
     private static Configuration recipeConfig;
 
     public ConfigManager(Heroes plugin) {
         this.plugin = plugin;
         File dataFolder = plugin.getDataFolder();
         classConfigFolder = new File(dataFolder + File.separator + "classes");
         expConfigFile = new File(dataFolder, "experience.yml");
         damageConfigFile = new File(dataFolder, "damages.yml");
         recipesConfigFile = new File(dataFolder, "recipes.yml");
     }
 
     public void load() throws Exception {
         checkForConfig(expConfigFile);
         checkForConfig(damageConfigFile);
         checkForConfig(recipesConfigFile);
         if (!classConfigFolder.exists()) {
             classConfigFolder.mkdirs();
             checkForConfig(new File(classConfigFolder, "citizen.yml"));
             checkForConfig(new File(classConfigFolder, "rogue.yml"));
             checkForConfig(new File(classConfigFolder, "cleric.yml"));
             checkForConfig(new File(classConfigFolder, "mage.yml"));
             checkForConfig(new File(classConfigFolder, "warrior.yml"));
         }
         plugin.setSkillConfigs(new SkillConfigManager(plugin));
         plugin.getSkillConfigs().load();
     }
 
     public void loadManagers() {
         damageConfig = YamlConfiguration.loadConfiguration(damageConfigFile);
         InputStream defConfigStream = plugin.getResource("defaults" + File.separator + "damages.yml");
         if (defConfigStream != null) {
             YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
             damageConfig.setDefaults(defConfig);
         }
         plugin.getDamageManager().load(damageConfig);
 
         expConfig = YamlConfiguration.loadConfiguration(expConfigFile);
         defConfigStream = plugin.getResource("defaults" + File.separator + "experience.yml");
         if (defConfigStream != null) {
             YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
             expConfig.setDefaults(defConfig);
         }
         loadExperience();
 
         recipeConfig = YamlConfiguration.loadConfiguration(recipesConfigFile);
         loadRecipes();
 
         HeroClassManager heroClassManager = new HeroClassManager(plugin);
         heroClassManager.loadClasses(classConfigFolder);
         plugin.setClassManager(heroClassManager);
     }
 
     public boolean reload() {
         try {
             final Player[] players = plugin.getServer().getOnlinePlayers();
             for (Player player : players) {
                 plugin.getHeroManager().saveHero(player);
                 Hero hero = plugin.getHeroManager().getHero(player);
                 hero.clearEffects();
             }
             plugin.getSkillConfigs().reload();
             damageConfig = null;
             expConfig = null;
             recipeConfig = null;
             plugin.setClassManager(null);
             loadManagers();
             for (Player player : players) {
                 Hero hero = plugin.getHeroManager().getHero(player);
                 plugin.getHeroManager().performSkillChecks(hero);
             }
         } catch (Exception e) {
             e.printStackTrace();
             Heroes.log(Level.SEVERE, "Critical error encountered while reloading. Disabling...");
             plugin.getServer().getPluginManager().disablePlugin(plugin);
             return false;
         }
         Heroes.log(Level.INFO, "Reloaded Configuration");
         return true;
     }
 
     public void checkForConfig(File config) {
         if (!config.exists()) {
             try {
                 Heroes.log(Level.WARNING, "File " + config.getName() + " not found - generating defaults.");
                 config.getParentFile().mkdir();
                 config.createNewFile();
                 OutputStream output = new FileOutputStream(config, false);
                 InputStream input = ConfigManager.class.getResourceAsStream("/defaults/" + config.getName());
                 byte[] buf = new byte[8192];
                 while (true) {
                     int length = input.read(buf);
                     if (length < 0) {
                         break;
                     }
                     output.write(buf, 0, length);
                 }
                 input.close();
                 output.close();
             } catch (Exception e) {
                 e.printStackTrace();
             }
         }
     }
 
     private void loadRecipes() {
         if (!Heroes.useSpout()) {
             RecipeGroup rg = new RecipeGroup("default", 1);
             rg.setAllRecipes(true);
             Heroes.properties.recipes.put("default", rg);
             return;
         }
 
         Set<String> recipes = recipeConfig.getKeys(false);
         if (recipes.isEmpty()) {
             Heroes.log(Level.WARNING, "No recipes found!");
         }
         for (String key : recipes) {
             int level = recipeConfig.getInt(key + ".level", 1);
             RecipeGroup rg = new RecipeGroup(key, level);
             // Load in the allowed items for this RecipeGroup
             List<String> items = recipeConfig.getStringList(key + ".items");
             if (items != null && !items.isEmpty()) {
                 for (String i : items) {
                     String[] vals = i.split(":");
                     if (vals[0].equalsIgnoreCase("*") || vals[0].equalsIgnoreCase("all")) {
                         rg.setAllRecipes(true);
                         break;
                     }
                     try {
                         Material mat = Material.getMaterial(Integer.valueOf(vals[0]));
                         short subType = 0;
                        if (vals.length > 1)
                             subType = Short.valueOf(vals[1]);

                         rg.put(new ItemData(mat, subType), true);
                     } catch (NumberFormatException e) {
                         Heroes.log(Level.SEVERE, "Invalid item ID in recipe group" + key);
                         continue;
                     }
                 }
             }
             List<String> deniedItems = recipeConfig.getStringList(key + ".denied-items");
             if (deniedItems != null && !deniedItems.isEmpty()) {
                 for (String i : deniedItems) {
                    String[] vals = i.split(",");
                     try {
                         Material mat = Material.getMaterial(Integer.valueOf(vals[0]));
                         short subType = 0;
                        if (vals.length > 1)
                             subType = Short.valueOf(vals[1]);

                         rg.put(new ItemData(mat, subType), false);
                     } catch (NumberFormatException e) {
                         Heroes.log(Level.SEVERE, "Invalid item ID in recipe group" + key);
                         continue;
                     }
                 }
             }
             Heroes.properties.recipes.put(key.toLowerCase(), rg);
         }
     }
 
     private void loadExperience() {
         ConfigurationSection section = expConfig.getConfigurationSection("killing");
         if (section == null) {
             Heroes.log(Level.WARNING, "No Experience Section Killing defined!");
             return;
         }
         Set<String> keys = section.getKeys(false);
         if (keys != null && !keys.isEmpty()) {
             for (String item : keys) {
                 try {
                     double exp = section.getDouble(item, 0);
                     if (item.equals("player")) {
                         Heroes.properties.playerKillingExp = exp;
                     } else {
                         CreatureType type = CreatureType.valueOf(item.toUpperCase());
                         Heroes.properties.creatureKillingExp.put(type, exp);
                     }
                 } catch (IllegalArgumentException e) {
                     Heroes.log(Level.WARNING, "Invalid creature type (" + item + ") found in experience.yml.");
                 }
             }
         } else {
             Heroes.log(Level.WARNING, "No Experience Section Killing defined!");
         }
 
         Heroes.properties.miningExp = loadMaterialExperience(expConfig.getConfigurationSection("mining"));
         Heroes.properties.farmingExp = loadMaterialExperience(expConfig.getConfigurationSection("farming"));
         Heroes.properties.loggingExp = loadMaterialExperience(expConfig.getConfigurationSection("logging"));
         Heroes.properties.craftingExp = loadMaterialExperience(expConfig.getConfigurationSection("crafting"));
         Heroes.properties.buildingExp = loadMaterialExperience(expConfig.getConfigurationSection("building"));
         Heroes.properties.fishingExp = expConfig.getDouble("fishing", 0);
     }
 
     private Map<Material, Double> loadMaterialExperience(ConfigurationSection section) {
         Map<Material, Double> expMap = new HashMap<Material, Double>();
         if (section != null) {
             Set<String> keys = section.getKeys(false);
             for (String item : keys) {
                 double exp = section.getDouble(item, 0);
                 Material type = Material.matchMaterial(item);
 
                 if (type != null) {
                     expMap.put(type, exp);
                 } else {
                     Heroes.log(Level.WARNING, "Invalid material type (" + item + ") found in experience.yml.");
                 }
             }
         }
         return expMap;
     }
 }
