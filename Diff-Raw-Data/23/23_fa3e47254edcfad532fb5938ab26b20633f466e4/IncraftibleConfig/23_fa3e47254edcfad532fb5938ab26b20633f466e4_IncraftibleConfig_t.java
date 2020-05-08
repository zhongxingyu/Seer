 package com.quiptiq.incraftible;
 
 import static com.quiptiq.incraftible.PermissionsReference.PERMISSION_CRAFT_PREFIX;
 import static com.quiptiq.incraftible.message.FixedMessage.LOG_PREFIX;
 
 import java.io.*;
 import java.util.TreeSet;
 import java.util.jar.JarFile;
 import java.util.logging.Logger;
 
 import org.bukkit.Material;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.material.MaterialData;
 import org.bukkit.permissions.PermissionAttachmentInfo;
 
 import com.quiptiq.incraftible.message.Message;
 
 /**
  * Configuration for Incraftible.
  *
  * TODO: Refactor the permissions logic into a dedicated class.
  *
  * @author Taufiq Hoven
  */
 public class IncraftibleConfig {
     private static final Logger log = Logger.getLogger(Incraftible.DEFAULT_LOGGER);
 
     private static final String CONFIG_FILENAME = "config.yml";
 
     private static final String LINE_SEPARATOR = System.getProperty("line.separator");
 
     private static final String LOG_DEFAULT_CONFIG = LOG_PREFIX + "No config file found in %s, generating default.";
 
     private static final String LOG_WARN_FAILED_CREATING_CONFIG_DIRECTORY = LOG_PREFIX
             + "Couldn't create config directory %s";
 
     private static final String LOG_WARN_CLOSE_JAR_ENTRY = LOG_PREFIX + "Couldn't close jar entry reader";
 
     private static final String LOG_WARN_CLOSE_FILE_WRITER = LOG_PREFIX + "Couldn't close file writer";
 
     private static final String CONFIG_CRAFT_EVENT_RETURN_VALUE_NULL = "event.craft.returnvalue.null";
 
     private static final String CONFIG_CRAFT_DEFAULT = "craft.default";
 
     private static final PermissionsStrategy DEFAULT_PERMISSIONS_STRATEGY = PermissionsStrategy.ALL;
 
     private FileConfiguration config;
 
     private boolean eventReturnValueMadeNull = false;
 
     private final PermissionsReference incraftiblePerms = PermissionsReference.getInstance();
 
     private PermissionsStrategy strategy;
 
     /**
      * Creates a new IncraftibleConfig based on the specified Bukkit
      * configuration.
      *
      * @param newConfig
      *            Bukkit config on which properties are retrieved.
      */
     public IncraftibleConfig(Incraftible plugin, File pluginFile) {
         loadConfig(plugin, pluginFile);
     }
 
     /**
      * Loads configuration from the specified Bukkit configuration, discarding
      * any current settings.
      *
      * @param newConfig
      *            Bukkit config on which properties are retrieved.
      */
     public void loadConfig(Incraftible plugin, File pluginFile) {
         // Check for existing config file
         File configFile = new File(plugin.getDataFolder(), CONFIG_FILENAME);
         if (!configFile.exists()) {
             log.info(String.format(LOG_DEFAULT_CONFIG, configFile.getPath()));
             config = loadDefaultConfig(plugin, pluginFile, configFile);
         } else {
             plugin.reloadConfig();
             config = plugin.getConfig();
         }
         // Override configurable messages
         for (Message message : Message.values()) {
             String configuredMessage = config.getString(message.getConfigNode());
             if (configuredMessage != null) {
                 message.overrideMessage(configuredMessage);
             }
         }
         eventReturnValueMadeNull = config.getBoolean(CONFIG_CRAFT_EVENT_RETURN_VALUE_NULL, false);
         String defaultCraftPermissions = config.getString(CONFIG_CRAFT_DEFAULT,
                DEFAULT_PERMISSIONS_STRATEGY.getConfigString());
        log.info(LOG_PREFIX + "Using permission strategy:" + defaultCraftPermissions);
        strategy = PermissionsStrategy.strategyForConfig(defaultCraftPermissions);
         if (strategy == null) {
             log.warning("Invalid crafting default: " + defaultCraftPermissions);
             strategy = DEFAULT_PERMISSIONS_STRATEGY;
         }
     }
 
     /**
      * Loads the default configuration for the specified plugin, using the given
      * plugin file and writes if out to the specified config file.
      *
      * @param plugin
      *            Plugin to be configured.
      * @param pluginFile
      *            Jar file containing the plugin.
      * @param configFile
      *            File that will contain the config.
      * @return
      */
     private FileConfiguration loadDefaultConfig(Incraftible plugin, File pluginFile, File configFile) {
         // Use the default that is packaged in the Incraftible jar
         String nextLine = null;
         FileWriter writer = null;
         BufferedReader reader = null;
         if (configFile.getParent() != null) {
             // Create the parent directory if it doesn't exist already
             File parentDir = new File(configFile.getParent());
             if (!parentDir.exists() && !(parentDir.mkdirs())) {
                 log.warning(String.format(LOG_WARN_FAILED_CREATING_CONFIG_DIRECTORY, configFile.getPath()));
                 return null;
             }
         }
 
         // Read the default config file from the jar
         try {
             JarFile jar = new JarFile(pluginFile);
             reader = new BufferedReader(new InputStreamReader(jar.getInputStream(jar.getJarEntry(CONFIG_FILENAME))));
             writer = new FileWriter(configFile);
             while ((nextLine = reader.readLine()) != null) {
                 writer.write(nextLine);
                 writer.append(LINE_SEPARATOR);
             }
         } catch (IOException e) {
             e.printStackTrace();
             return null;
         } finally {
             if (reader != null) {
                 try {
                     reader.close();
                 } catch (IOException e) {
                     log.warning(LOG_WARN_CLOSE_JAR_ENTRY);
                 }
             }
             if (writer != null) {
                 try {
                     writer.close();
                 } catch (IOException e) {
                     log.warning(LOG_WARN_CLOSE_FILE_WRITER);
                 }
             }
         }
         plugin.reloadConfig();
         FileConfiguration config = plugin.getConfig();
         return config;
     }
 
     /**
      * @return Default method of granting craft permissions. Possible values
      *         are defined in {@link PermissionsStrategy}
      */
     public PermissionsStrategy getPermissionsStrategy() {
         return (strategy == null ? DEFAULT_PERMISSIONS_STRATEGY : strategy);
     }
 
     /**
      * Whether or not the specified item is allowed.
      *
      * @param item
      *            Item to be checked. If null, returns false.
      * @return True if the item is allowed, otherwise false.
      */
     public boolean isItemAllowed(Material item, ItemStack stack, Player player) {
         String permissionName;
         if (item == null) {
             return false;
         }
         if (incraftiblePerms.hasBasePermission(item)) {
             permissionName = incraftiblePerms.getBasePermissionName(item);
         } else if (incraftiblePerms.hasDataPermission(item)) {
             if (stack == null) {
                 log.warning(LOG_PREFIX + "Invalid stack " + item.toString());
                 return false;
             }
             MaterialData materialData = stack.getData();
             if (materialData == null) {
                 log.warning(LOG_PREFIX + "No material data for expected item material" + item.toString());
                 return false;
             }
             permissionName = incraftiblePerms.getDataPermissionName(item, materialData.getData());
             log.info("Checking permission value for " + permissionName);
             if (permissionName == null) {
                 log.warning(LOG_PREFIX + "No permission name stored for " + item.toString() + ":" + materialData.getData());
                 return false;
             }
         } else {
             log.warning(LOG_PREFIX + "No permission stored for material " + item.toString());
             return false;
         }
 
         // If the permission is not set, only allowed if the ALL permission strategy is in effect.
         if (!player.isPermissionSet(permissionName)) {
             return PermissionsStrategy.ALL.equals(strategy);
         } else {
             return player.hasPermission(permissionName);
         }
     }
 
     /**
      * Logs all incraftible permissions for the specified player. This does not
      * include default permissions.
      *
      * @param player
      *            Player for which permissions are logged.
      */
     public void logCraftPermissions(Player player) {
         if (player == null) {
             log.warning(LOG_PREFIX + "Can't log permissions for null player");
             return;
         }
         TreeSet<String> sortedLogEntries = new TreeSet<String>();
         for (PermissionAttachmentInfo info : player.getEffectivePermissions()) {
             String permissionName = info.getPermission();
             if (!permissionName.startsWith(PERMISSION_CRAFT_PREFIX)) {
                 continue;
             }
             sortedLogEntries.add(permissionName + ":" + info.getValue());
         }
         for (String logEntry : sortedLogEntries) {
             log.info(logEntry);
         }
     }
 
     /**
      * Whether or not a craft event return value should be set to null once
      * handled.
      *
      * @return True if a craft event return value should be set to null when it
      *         is handled, otherwise false.
      */
     public boolean isEventReturnValueMadeNull() {
         return eventReturnValueMadeNull;
     }
 }
