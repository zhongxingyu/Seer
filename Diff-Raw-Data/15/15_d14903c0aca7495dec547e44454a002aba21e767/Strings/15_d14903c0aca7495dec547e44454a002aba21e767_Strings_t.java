 package net.tgxn.bukkit.backup.config;
 
 import java.io.*;
 import java.util.logging.Level;
 import net.tgxn.bukkit.backup.utils.LogUtils;
 import org.bukkit.configuration.InvalidConfigurationException;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 
 /**
  * String loader for the plugin, provides strings for each event.
  *
  * @author Domenic Horner (gamerx)
  */
 public class Strings {
     
     private File stringsFile;
     private FileConfiguration fileStringConfiguration;
     
     /**
      * Loads the strings configuration file.
      * If it does not exist, it creates it from defaults.
      * 
      * @param plugin The plugin this is for.
      */
     public Strings(File stringsFile) {
         this.stringsFile = stringsFile;
         
         checkAndCreate();
         loadStrings();
     }
     
     private void checkAndCreate() {
         // Check for the config file, have it created if needed.
         try {
             if (!stringsFile.exists()) {
                 createDefaultStrings();
             }
         } catch (NullPointerException npe) {
             LogUtils.exceptionLog(npe.getStackTrace(), "Error checking strings file.");
         } catch (SecurityException se) {
             LogUtils.exceptionLog(se.getStackTrace(), "Error checking strings file.");
         }
     }
     
     public void checkStringsVersion(String requiredVersion) {
     
         boolean needsUpdate = false;
         
         // Check configuration is loaded.
         if (fileStringConfiguration != null) {
 
             // Get the version information from the file.
             String stringVersion = fileStringConfiguration.getString("version", null);
 
             // Check we got a version from the config file.
             if (stringVersion == null) {
                 LogUtils.sendLog("Failed to get strings file verison.", Level.SEVERE, true);
                 needsUpdate = true;
            } else if (!stringVersion.equals(requiredVersion)) {
                 needsUpdate = true;
            }
             // After we have checked the versions, we have determined that we need to update.
             if (needsUpdate) {
                 LogUtils.sendLog(Level.SEVERE, this.getString("stringsupdate"));
             }
         }
     }
     
     private void loadStrings() {
         fileStringConfiguration = new YamlConfiguration();
         try {
             fileStringConfiguration.load(stringsFile);
         } catch (FileNotFoundException ex) {
             LogUtils.exceptionLog(ex.getStackTrace(), "Error loading strings file.");
         } catch (InvalidConfigurationException ice) {
             LogUtils.exceptionLog(ice.getStackTrace(), "Error loading strings file.");
         } catch (IOException ioe) {
             LogUtils.exceptionLog(ioe.getStackTrace(), "Error loading strings file.");
         }
     }
     
     private void createDefaultStrings() {
     
         if(stringsFile.exists())
             stringsFile.delete();
         
     /**
      * Load the properties file from the JAR and place it in the backup DIR.
      */
     
         BufferedReader bReader = null;
         BufferedWriter bWriter = null;
         String line;
 
         try {
 
             // Open a stream to the properties file in the jar, because we can only access over the class loader.
             bReader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/settings/strings.yml")));
             bWriter = new BufferedWriter(new FileWriter(stringsFile));
 
             // Copy the content to the configfile location.
             while ((line = bReader.readLine()) != null) {
                 bWriter.write(line);
                 bWriter.newLine();
             }
         } catch (IOException ioe) {
             LogUtils.exceptionLog(ioe.getStackTrace(), "Error opening streams.");
         }
         
         finally {
             try {
                 if (bReader != null) {
                     bReader.close();
                 }
                 if (bWriter != null) {
                     bWriter.close();
                 }
             } catch (IOException ioe) {
                 LogUtils.exceptionLog(ioe.getStackTrace(), "Error closing streams.");
             }
         }
     
 
     }
     
     public void doStringsUpdate() {
         loadStrings();
         
     }
     
     /**
      * Gets a value of the string property.
      * 
      * @param sname The identifier for the string.
      * @return The string from properties, with colors encoded.
      */
     public String getString(String property) {
         
         // Get string for this name.
         String string = fileStringConfiguration.getString(property);
         
         // If we cannot find a string for this, return default.
         if (string != null)
             return colorizeString(string);
         else
             return fileStringConfiguration.getString("stringnotfound") + property;
     }
     
     /**
      * Gets a value of the string property, and replaces options.
      * 
      * @param property The identifier for the string.
      * @param option The variable to replace %%ARG%% with.
      * @return The string from properties, with colors encoded, and text replaced.
      */
     public String getString(String property, String option) {
         
         // Get string for this name.
         String string = fileStringConfiguration.getString(property);
         
         // If we cannot find a string for this, return default.
         if (string != null)
             return colorizeString(string.replaceAll("%%ARG%%", option));
         else
             return fileStringConfiguration.getString("stringnotfound") + property;
     }
     
     /**
      * Encodes the color codes, and returns the encoded string.
      * If the parameter is blank or null, return blank.
      * 
      * @param toColour The string to encode.
      * @return The encoded string.
      */
     private String colorizeString(String toColor) {
         
         // Check we got passed a string.
         if (toColor != null)
             return toColor.replaceAll("&([0-9a-f])", "\u00A7$1");
         else
             return "";
     }
 }
