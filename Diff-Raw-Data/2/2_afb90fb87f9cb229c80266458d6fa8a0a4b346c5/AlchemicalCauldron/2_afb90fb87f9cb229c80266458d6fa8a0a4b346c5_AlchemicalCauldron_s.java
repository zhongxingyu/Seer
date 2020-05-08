 package com.github.Indiv0.AlchemicalCauldron;
 
 import java.io.File;
 import java.io.IOException;
 import java.text.DecimalFormat;
 import java.util.HashMap;
 import java.util.Set;
 import java.util.logging.Level;
 
 import org.bukkit.Material;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.mcstats.MetricsLite;
 
 public class AlchemicalCauldron extends JavaPlugin {
     public final EntityInteractListener entityInteractListener = new EntityInteractListener(this);
     
     private HashMap<Material, Double> inputMaterials = new HashMap<Material, Double>();
     private HashMap<Material, Double> outputMaterials = new HashMap<Material, Double>();
 
     public void onEnable() {
         // Retrieves an instance of the PluginManager.
         PluginManager pm = getServer().getPluginManager();
 
         // Registers the blockListener with the PluginManager.
         pm.registerEvents(this.entityInteractListener, this);
         
         loadConfig();
         
         //FileConfiguration probabilityConfig = loadConfig("config.yml");
         loadMaterials(getConfig(), getInputMaterials(), "inputs");
         loadMaterials(getConfig(), outputMaterials, "outputs");
         
         // Enable PluginMetrics.
         enableMetrics();
         
         // Prints a message to the server confirming successful initialization of the plugin.
         PluginDescriptionFile pdfFile = this.getDescription();
         getLogger().info(pdfFile.getName() + " " + pdfFile.getVersion() + " is enabled.");
     }
 
     private void enableMetrics()
     {
         try {
             MetricsLite metrics = new MetricsLite(this);
             metrics.start();
         } catch (IOException ex) {
            System.out.println("An error occured while appempting to connect to PluginMetrics.");
         }
     }
     
     private void loadMaterials(FileConfiguration fileConfiguration, HashMap<Material, Double> materials, String section)
     {
         // Defines the section of the configuration to be searched.
         ConfigurationSection configSection = fileConfiguration.getConfigurationSection(section);
         
         // If the configuration section does not exist, outputs a warning.
         if(configSection == null) {
             getLogger().log(Level.WARNING, "No keys/values have been defined for the section \"" + section + "\"");
             return;
         }
         
         // Gets all of the keys for the section.
         Set<String> keyList = configSection.getKeys(false);
         
         for(String materialID : keyList) {
             // Attempts to get the material represented by the key.
             Material material = Material.matchMaterial(materialID);
             
             // If the key is invalid, output as such.
             if (material == null || material == Material.AIR) {
                 getLogger().log(Level.WARNING, "AlCo config contains an invalid key: " + materialID);
             }
             else {
                 // Tries to lead the ratio value for that key.
                 double val = -1;
                 try {
                     val = Double.parseDouble((String) fileConfiguration.get(section + "." + materialID));
                 } catch(Exception ex) {
                     getLogger().log(Level.WARNING, "Config contains an invalid value for key: " + materialID);
                 }
                 
                 // Reduce the precision to 2 decimal places.
                 DecimalFormat form = new DecimalFormat();
                 form.setMaximumFractionDigits(2);
                 form.setMinimumFractionDigits(0);
                 val = Double.parseDouble(form.format(val));
                 
                 if (val < 0 || val > 1)
                     getLogger().log(Level.WARNING, "Config contains an invalid value for key: " + materialID);
                 
                 // Makes sure an item is not being added twice, then adds the material and its value to the cache.
                 if (!materials.containsKey(material))
                     materials.put(material, val);
                 else
                     getLogger().log(Level.WARNING, "Config contains the same material twice. Will not be added again.");
             }
         }        
     }
     
     private void loadConfig()
     {
         if(!(new File("plugins/AlchemicalCauldron/config.yml").exists())) {
             // Create some default configuration values.
             getConfig().addDefault("inputs.2", "0.01");
             getConfig().addDefault("inputs.cobblestone", "0.2");
             getConfig().addDefault("outputs.iron_ingot", "0.6");
             
             getConfig().options().copyDefaults(true);
         }
         
         saveConfig();
     }
 
     public HashMap<Material, Double> getInputMaterials() {
         return inputMaterials;
     }
 
     public HashMap<Material, Double> getOutputMaterials() {
         return outputMaterials;
     }
 }
