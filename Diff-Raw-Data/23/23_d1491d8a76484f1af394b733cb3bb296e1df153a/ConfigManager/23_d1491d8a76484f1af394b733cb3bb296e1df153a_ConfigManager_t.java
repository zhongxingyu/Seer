 package com.herocraftonline.dev.heroes.util;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Level;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.entity.CreatureType;
 import org.bukkit.util.config.Configuration;
 import org.bukkit.util.config.ConfigurationNode;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.classes.HeroClassManager;
 import com.herocraftonline.dev.heroes.command.BaseCommand;
 import com.herocraftonline.dev.heroes.skill.Skill;
 
 public class ConfigManager {
 
     protected Heroes plugin;
     protected File primaryConfigFile;
     protected File classConfigFile;
     protected File expConfigFile;
     protected File skillConfigFile;
     protected File damageConfigFile;
     protected Properties properties = new Properties();
 
     public ConfigManager(Heroes plugin) {
         this.plugin = plugin;
         this.primaryConfigFile = new File(plugin.getDataFolder(), "config.yml");
         this.classConfigFile = new File(plugin.getDataFolder(), "classes.yml");
         this.expConfigFile = new File(plugin.getDataFolder(), "experience.yml");
         this.skillConfigFile = new File(plugin.getDataFolder(), "skills.yml");
         this.damageConfigFile = new File(plugin.getDataFolder(), "damages.yml");
     }
 
     public Properties getProperties() {
         return properties;
     }
 
     public void load() throws Exception {
         checkForConfig(primaryConfigFile);
         checkForConfig(classConfigFile);
         checkForConfig(expConfigFile);
         checkForConfig(damageConfigFile);
 
         Configuration primaryConfig = new Configuration(primaryConfigFile);
         primaryConfig.load();
         loadLevelConfig(primaryConfig);
         loadDefaultConfig(primaryConfig);
         loadProperties(primaryConfig);
 
         Configuration damageConfig = new Configuration(damageConfigFile);
         damageConfig.load();
         plugin.getDamageManager().load(damageConfig);
 
         Configuration expConfig = new Configuration(expConfigFile);
         expConfig.load();
         loadExperience(expConfig);
 
         Configuration skillConfig = new Configuration(skillConfigFile);
         skillConfig.load();
         generateSkills(skillConfig);
 
         HeroClassManager heroClassManager = new HeroClassManager(plugin);
         heroClassManager.loadClasses(classConfigFile);
         plugin.setClassManager(heroClassManager);
     }
 
     public void reload() {
         try {
             load();
         } catch (Exception e) {
             e.printStackTrace();
             plugin.log(Level.SEVERE, "Critical error encountered while loading. Disabling...");
             plugin.getServer().getPluginManager().disablePlugin(plugin);
             return;
         }
         plugin.log(Level.INFO, "Reloaded Configuration");
     }
 
     private void addNodeToConfig(Configuration config, ConfigurationNode node, String path) {
         for (String key : node.getKeys(null)) {
             config.setProperty(path + "." + key, node.getProperty(key));
         }
     }
 
     private void checkForConfig(File config) {
         if (!config.exists()) {
             try {
                 plugin.log(Level.WARNING, "File " + config.getName() + " not found - generating defaults.");
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
 
     private void generateSkills(Configuration config) {
         for (BaseCommand baseCommand : plugin.getCommandManager().getCommands()) {
             if (baseCommand instanceof Skill) {
                 Skill skill = (Skill) baseCommand;
                 ConfigurationNode node = config.getNode(skill.getName());
                 if (node == null) {
                     addNodeToConfig(config, skill.getDefaultConfig(), skill.getName());
                 } else {
                     ConfigurationNode defaultNode = skill.getDefaultConfig();
                     mergeNodeToConfig(config, defaultNode, skill.getName());
                 }
             }
 
         }
         config.save();
         loadSkills(config);
     }
 
     private void loadDefaultConfig(Configuration config) {
         String root = "default.";
         properties.defClass = config.getString(root + "class");
         properties.defLevel = config.getInt(root + "level", 1);
     }
 
     private void loadExperience(Configuration config) {
         List<String> keys = config.getKeys("killing");
         if (keys != null) {
             for (String item : keys) {
                 try {
                     double exp = config.getDouble("killing." + item, 0);
                     if (item.equals("player")) {
                         properties.playerKillingExp = exp;
                     } else {
                         CreatureType type = CreatureType.valueOf(item.toUpperCase());
                         properties.creatureKillingExp.put(type, exp);
                     }
                 } catch (IllegalArgumentException e) {
                     plugin.log(Level.WARNING, "Invalid creature type (" + item + ") found in experience.yml.");
                 }
             }
         }
 
         properties.miningExp = loadMaterialExperience(config, "mining");
         properties.loggingExp = loadMaterialExperience(config, "logging");
         properties.craftingExp = loadMaterialExperience(config, "crafting");
     }
 
     private void loadLevelConfig(Configuration config) {
         String root = "leveling.";
         properties.power = config.getDouble(root + "power", 1.03);
         properties.maxExp = config.getInt(root + "maxExperience", 90000);
         properties.maxLevel = config.getInt(root + "maxLevel", 20);
         properties.partyBonus = config.getDouble(root + "partyBonus", 0.20);
         properties.expLoss = config.getDouble(root + "expLoss", 0.05);
         properties.blockTrackingDuration = config.getInt(root + "block-tracking-duration", 10 * 60 * 1000);
         properties.maxTrackedBlocks = config.getInt(root + "max-tracked-blocks", 1000);
         properties.resetExpOnClassChange = config.getBoolean(root + "resetExpOnClassChange", true);
         properties.swapMasteryCost = config.getBoolean(root + "swapMasteryCost", false);
         properties.calcExp();
     }
 
     private Map<Material, Double> loadMaterialExperience(ConfigurationNode config, String path) {
         Map<Material, Double> expMap = new HashMap<Material, Double>();
         List<String> keys = config.getKeys(path);
         if (keys != null) {
             for (String item : keys) {
                 double exp = config.getDouble(path + "." + item, 0);
                 Material type = Material.matchMaterial(item);
 
                 if (type != null) {
                     expMap.put(type, exp);
                 } else {
                     plugin.log(Level.WARNING, "Invalid material type (" + item + ") found in experience.yml.");
                 }
             }
         }
         return expMap;
     }
 
     private void loadProperties(Configuration config) {
         String root = "properties.";
         properties.iConomy = config.getBoolean(root + "iConomy", false);
         properties.cColor = ChatColor.valueOf(config.getString(root + "color", "WHITE"));
         properties.swapCost = config.getInt(root + "swapcost", 0);
         properties.debug = config.getBoolean(root + "debug", false);
         properties.damageSystem = config.getBoolean(root + "useDamageSystem", false);
     }
 
     private void loadSkills(Configuration config) {
         config.load();
         for (BaseCommand baseCommand : plugin.getCommandManager().getCommands()) {
             if (baseCommand instanceof Skill) {
                 Skill skill = (Skill) baseCommand;
                 ConfigurationNode node = config.getNode(skill.getName());
                 if (node != null) {
                     // System.out.println(skill.getName());
                     // print(node.getAll(), "  ");
                     skill.setConfig(node);
                 } else {
                     skill.setConfig(Configuration.getEmptyNode());
                 }
                 skill.init();
             }
         }
     }
 
     private void mergeNodeToConfig(Configuration config, ConfigurationNode node, String path) {
         List<String> keys = node.getKeys(null);
         if (keys != null) {
             for (String key : keys) {
                 Object value = config.getProperty(path + "." + key);
                 if (value == null) {
                     config.setProperty(path + "." + key, node.getProperty(key));
                 }
             }
         }
     }
 
     @SuppressWarnings({ "unchecked", "unused" })
     private void print(Map<String, Object> map, String indent) {
         for (Map.Entry<String, Object> entry : map.entrySet()) {
             if (entry.getValue() instanceof Map) {
                 System.out.println(indent + entry.getKey());
                 print((Map<String, Object>) entry.getValue(), indent + "  ");
             } else {
                 System.out.println(indent + entry.getKey() + ": " + entry.getValue());
             }
         }
     }
 }
