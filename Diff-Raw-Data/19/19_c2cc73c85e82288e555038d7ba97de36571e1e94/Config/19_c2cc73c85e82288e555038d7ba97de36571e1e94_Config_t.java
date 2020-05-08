 package mccity.plugins.pvprealm;
 
 import me.galaran.bukkitutils.pvprealm.GUtils;
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.logging.Level;
 
 public class Config {
 
     private static File file;
 
     private static World pvpWorld;
 
     public static Location entryLoc;
     public static Location defaultReturnLoc;
     public static boolean deathExpLoss;
 
     public static void create(File configFile) throws IllegalWorldException {
         file = configFile;
         FileConfiguration fileConfig = YamlConfiguration.loadConfiguration(file);
 
         String pvpWorldName = fileConfig.getString("pvp-world");
         pvpWorld = Bukkit.getServer().getWorld(pvpWorldName);
         if (pvpWorld == null) throw new IllegalWorldException();
 
         deathExpLoss = fileConfig.getBoolean("death-heroes-exp-loss", false);
        ConfigurationSection entryLocSection = fileConfig.getConfigurationSection("entry-loc");
        if (entryLocSection != null) {
            entryLoc = GUtils.deserializeLocation(entryLocSection.getValues(false));
        }
        ConfigurationSection defaultReturnLocSection = fileConfig.getConfigurationSection("default-return-loc");
        if (defaultReturnLocSection != null) {
            defaultReturnLoc = GUtils.deserializeLocation(defaultReturnLocSection.getValues(false));
        }
         initDefaults();
     }
 
     private static void initDefaults() {
         if (entryLoc == null) {
             entryLoc = pvpWorld.getSpawnLocation();
         }
         if (defaultReturnLoc == null) {
             defaultReturnLoc = entryLoc;
             GUtils.log("Default return location not set, specify it with /pvprealmadmin setreturn", Level.WARNING);
         }
     }
 
     public static void save() {
         YamlConfiguration fileConfig = new YamlConfiguration();
 
         fileConfig.set("pvp-world", pvpWorld.getName());
         fileConfig.set("death-heroes-exp-loss", deathExpLoss);
        if (entryLoc != null) {
            fileConfig.set("entry-loc", GUtils.serializeLocation(entryLoc));
        }
        if (defaultReturnLoc != null) {
            fileConfig.set("default-return-loc", GUtils.serializeLocation(defaultReturnLoc));
        }
 
         try {
             fileConfig.save(file);
         } catch (IOException ex) {
             GUtils.log("Failed to save config file", Level.SEVERE);
             ex.printStackTrace();
         }
     }
 
     public static World getPvpWorld() {
         return pvpWorld;
     }
 }
