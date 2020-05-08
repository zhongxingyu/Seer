 package de.aidger.utils;
 
 import static de.aidger.utils.Translation._;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.text.MessageFormat;
 import java.util.Properties;
 
 import de.aidger.model.Runtime;
 
 /**
  * Gets and sets the settings
  *
  * @author aidGer Team
  */
 public final class Configuration {
 
     /**
      * The settings of the program.
      */
     private final Properties properties = new Properties();
 
     /**
      * The file name of the configuration.
      */
     private final String file;
 
     /**
      * Initializes this Configuration with a given path.
      *
      * @param path
      *            The path of the configuration.
      */
     public Configuration() {
         file = Runtime.getInstance().getConfigPath() + "settings.cfg";
 
         initialize();
     }
 
     /**
      * Creates the Settings file if it does not exist already, or reads the data
      * from it, if it does.
      *
      * @return True if the file was created/read successfully.
      */
     public boolean initialize() {
         /* Check if the configuration exists and create it if it does not */
     	File config = new File(file);
         if (!config.exists()) {
             createFile();
         } else {
             /* Read the settings from the file. */
             try {
                 FileInputStream inputStream = new FileInputStream(config);
                 properties.load(inputStream);
                 inputStream.close();
             } catch (Exception e) {
                 createFile();
                 return false;
             }
         }
         return true;
     }
 
     /**
      * Writes the default settings to the settings file.
      */
     private void createFile() {
         try {
             File outputFile = new File(file);
             FileOutputStream outputStream = new FileOutputStream(outputFile);
             properties.setProperty("name", "");
             properties.setProperty("pdf-viewer", "");
             properties.setProperty("language", "de");
             properties.setProperty("activities", "10");
             properties.setProperty("auto-open", "n");
             properties.setProperty("auto-save", "n");
             properties.setProperty("pessimistic-factor", "1.0");
             properties.setProperty("historic-factor", "1.0");
            properties.setProperty("anonymize-time", "365");
             properties.setProperty("debug", "false");
             properties.store(outputStream, "");
             outputStream.close();
         } catch (Exception e) {
             Logger.error(MessageFormat.format(
                 _("Could not create file \"{0}\"."), new Object[] { file }));
         }
     }
 
     /**
      * Gets the value of a property.
      *
      * @param option
      *            The property of which to get the value from
      * @return The value of the specified property.
      */
     public String get(String option) {
         return properties.getProperty(option);
     }
 
     /**
      * Sets the value of a property.
      *
      * @param option
      *            The property to change.
      * @param value
      *            The value to change the property to.
      */
     public void set(String option, String value) {
         properties.setProperty(option, value);
 
         try {
             File outputFile = new File(file);
             FileOutputStream outputStream = new FileOutputStream(outputFile);
             properties.store(outputStream, "");
             outputStream.close();
         } catch (Exception e) {
             createFile();
         }
     }
 }
