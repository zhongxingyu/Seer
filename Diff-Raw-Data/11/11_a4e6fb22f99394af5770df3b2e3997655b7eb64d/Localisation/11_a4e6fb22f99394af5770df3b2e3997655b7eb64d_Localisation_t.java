 package com.gmail.jameshealey1994.simplepvptoggle.localisation;
 
 import com.gmail.jameshealey1994.simplepvptoggle.SimplePVPToggle;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 import java.util.HashMap;
 import java.util.logging.Level;
 import org.bukkit.ChatColor;
 
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 
 /**
  * Class to help with localisation and custom messages.
  *
  * @author JamesHealey94 <jameshealey1994.gmail.com>
  */
 public class Localisation {
 
     /**
      * The default localisation filename.
      */
     public static final String DEFAULT_FILENAME = "localisation.yml";
     
     /**
      * Plugin associated with the localisation.
      */
     private final SimplePVPToggle plugin;
 
     /**
      * Initialises plugin variable and localisations.
      *
      * @param plugin    plugin used for config file, used for filename
      */
     public Localisation(SimplePVPToggle plugin) {
         if (plugin == null) {
             throw new IllegalArgumentException("plugin cannot be null");
         }
 //        if (!plugin.isInitialized()) {
 //            throw new IllegalArgumentException("plugin must be initialized");
 //        }
         this.plugin = plugin;
 
 //        if (plugin.getDataFolder() == null) {
 //            throw new IllegalStateException();
 //        }
         
 //        getConfigFile(plugin);        
 //        this.localisations = getLocalisation();
     }
 
     /**
      * Gets the corresponding localisation to the key passed.
      *
      * @param key   key used to obtain localisation value
      * @return      value obtained, or error message if no valid value is found
      */
     public String get(LocalisationEntry key) {
         final HashMap<String, Object> localisations = getLocalisations();
 
         if (localisations.containsKey(key.getName())) {
             // TODO - Check if String conversion is needed
             if (localisations.get(key.getName()) instanceof String) {
                 return addColor((String) localisations.get(key.getName()));
             } else {
                 plugin.getLogger().log(Level.WARNING, "[{0}] Value should be a String: {1}", new Object[]{plugin.getName(), localisations.get(key.getName())});
                 return "ERROR:Value should be a String: '" + key.getName() + "'";
             }
         } else {
             plugin.getLogger().log(Level.WARNING, "[{0}] Missing localisation: {1}", new Object[]{plugin.getName(), key});
             return "ERROR:Missing localisation: '" + key.getName() + "'";
         }
     }
 
     /**
      * Adds colours to the passed string.
      *
      * @param string    string to add colours to
      * @return          passed string with colours added
      */
     public static String addColor(String string) {
         return ChatColor.translateAlternateColorCodes('&', string);
     }
     
     /**
      * Returns either a custom or default HashMap of localisations.
      *
      * @return  custom HashMap of localisations if one has been provided, else a
      *          default is returned
      */
     private HashMap<String, Object> getLocalisations() {
 //
 //        // Look for defaults in the jar
 //        final InputStream defConfigStream = plugin.getResource(filename);
 //        if (defConfigStream != null) {
 //            final YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
 //            fileConfiguration.setDefaults(defConfig);
 //        } else {
 //            createDefaultConfig(configFile);
 //        }
         return (HashMap<String, Object>) getConfig().getValues(true);
     }
     
     /**
      * Returns the FileConfiguration of getFile().
      * 
      * @return the FileConfiguration of getFile()
      */
     public FileConfiguration getConfig() {
         return YamlConfiguration.loadConfiguration(getFile());
     }
     
     /**
      * Returns the file containing localisation values.
      * 
      * @return  the file containing localisation values
      */
     private File getFile() {
        if (!(new File(plugin.getDataFolder(), getFilename()).exists())) {
             createDefaultConfig();   
         }
         return new File(plugin.getDataFolder(), getFilename());
     }
     
     /**
      * Fills the file specified in the config with default localisation values.
      */
     private void createDefaultConfig() {
         final String title = plugin.getName() + " localisation configuration";
 
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(plugin.getDataFolder(), getFilename()), true), "UTF-8"))) {
             writer.write("# " + title + "\n");
             for (LocalisationEntry entry : LocalisationEntry.values()) {
                 writer.write(entry.toString());
             }
         } catch (IOException ex) {
             plugin.getLogger().log(Level.SEVERE, "[{0}] IOException when creating new localisation file : {1}!", new Object[]{plugin.getName(), title});
             plugin.getLogger().severe(ex.getMessage());
         }
     }
     
     /**
      * Returns the filename as specified in config.yml.
      * 
      * @return  filename as specified in config.yml
      */
     public final String getFilename() {
         return plugin.getConfig().getString("localisation filename", DEFAULT_FILENAME);
     }
     
 //    /**
 //     * Name of the configFile.
 //     */
 //    private String filename;
 
 //    /**
 //     * The file containing Localisations.
 //     */
 //    private File configFile;
 
 //    /**
 //     * The configuration of the configFile.
 //     */
 //    private FileConfiguration fileConfiguration;
 
 //    /**
 //     * The values of the localisation.
 //     */
 //    private HashMap<String, Object> localisations;   
     
 //    /**
 //     * Returns all localisation entries and their default values as a HashMap.
 //     *
 //     * @return all localisation entries and their default values as a HashMap
 //     */
 //    private HashMap<String, Object> getDefaultLocalisation() {
 //        final HashMap<String, Object> defaultLocalisation = new HashMap<>();
 //        for (LocalisationEntry entry : LocalisationEntry.values()) {
 //            defaultLocalisation.put(entry.getName(), entry.getDefaultValue());
 //        }
 //        return defaultLocalisation;
 //    }
 
 //    public void reloadConfig() {
 //        fileConfiguration = YamlConfiguration.loadConfiguration(getFile());
 //
 //        // Look for defaults in the jar
 //        final InputStream defConfigStream = plugin.getResource(getFilename());
 //        if (defConfigStream != null) {
 //            final YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
 //            fileConfiguration.setDefaults(defConfig);
 //        } else {
 //            createDefaultConfig();
 //        }
 //    }
     
 //    public void saveConfig() {
 //        if (/*fileConfiguration != null && */getFile() != null) {
 //            try {
 //                getConfig().save(getFile());
 //            } catch (IOException ex) {
 //                plugin.getLogger().log(Level.SEVERE, "Could not save config to " + getFile(), ex);
 //            }
 //        }
 //    }
 //
 //    /**
 //     * Creates or overwrites the localisation file with default values.
 //     */
 //    public void saveDefaultConfig() {
 //        if (!getFile().exists()) {
 //            plugin.saveResource(getFilename(), false);
 //        }
 //    }
 }
