 /*
  * This file is part of the aidGer project.
  *
  * Copyright (C) 2010-2011 The aidGer Team
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
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
      * The current version of the configuration file.
      */
     private static final int version = 2;
 
     /**
      * Initializes the Configuration.
      */
     public Configuration() {
         this("settings.cfg");
     }
 
     /**
      * Initializes the Configuration with a given filename.
      * 
      * @param filename
      *            The filename of the configuration.
      */
     public Configuration(String filename) {
         file = Runtime.getInstance().getConfigPath() + filename;
 
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
             }
         }
 
         /* Get the version of the config and migrate if necessary */
         String confVersion = get("config-version");
         if (confVersion == null) {
             migrate(0);
         } else if (Integer.parseInt(confVersion) < version) {
             migrate(Integer.parseInt(confVersion));
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
 
     /**
      * Remove an option from the config.
      * 
      * @param option
      *            The option to remove
      */
     public void remove(String option) {
         properties.remove(option);
 
         try {
             File outputFile = new File(file);
             FileOutputStream outputStream = new FileOutputStream(outputFile);
             properties.store(outputStream, "");
             outputStream.close();
         } catch (Exception e) {
             createFile();
         }
     }
 
     /**
      * Writes the default settings to the settings file.
      */
     private void createFile() {
         try {
             File outputFile = new File(file);
             FileOutputStream outputStream = new FileOutputStream(outputFile);
             migrate(0);
             properties.store(outputStream, "");
             outputStream.close();
         } catch (Exception e) {
             Logger.error(MessageFormat.format(
                 _("Could not create file \"{0}\"."), new Object[] { file }));
         }
     }
 
     /**
      * Migrate an old configuration file to the current version.
      * 
      * @param oldVersion
      *            The version of the config file
      */
     private void migrate(int oldVersion) {
         if (oldVersion < 1 && properties.getProperty("name") == null) {
             properties.setProperty("name", "");
             properties.setProperty("pdf-viewer", "");
             properties.setProperty("activities", "10");
             properties.setProperty("auto-open", "n");
             properties.setProperty("auto-save", "n");
             properties.setProperty("pessimistic-factor", "1.0");
             properties.setProperty("historic-factor", "1.0");
             properties.setProperty("anonymize-time", "365");
             properties.setProperty("tolerance", "0.0");
             properties.setProperty("calc-method", "1");
             properties.setProperty("rounding", "2");
             properties.setProperty("debug", "false");
         }
         if (oldVersion < 2) {
            properties.setProperty("database-uri", "jdbc:derby:"
                    + Runtime.getInstance().getConfigPath()
                    + "/database;create=true");
         }
 
         properties.setProperty("config-version", Integer.toString(version));
     }
 }
