 /*
  * #%L
  * Bitrepository Alarm Service
  * 
  * $Id$
  * $HeadURL$
  * %%
  * Copyright (C) 2010 - 2012 The State and University Library, The Royal Library and The State Archives, Denmark
  * %%
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as 
  * published by the Free Software Foundation, either version 2.1 of the 
  * License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Lesser Public License for more details.
  * 
  * You should have received a copy of the GNU General Lesser Public 
  * License along with this program.  If not, see
  * <http://www.gnu.org/licenses/lgpl-2.1.html>.
  * #L%
  */
 package org.bitrepository.alarm;
 
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.Properties;
 
 import org.bitrepository.common.settings.Settings;
 import org.bitrepository.common.settings.SettingsProvider;
 import org.bitrepository.common.settings.XMLFileSettingsLoader;
 
 public final class AlarmStoreFactory {
 	/** The instance of the alarmStore */
     private static AlarmStore alarmStore;
     /** Member to hold the directory path of where the configuration files are located. */
     private static String confDir; 
     /** Member to hold the name of the file which persists alarms, once read from properties */
     private static String alarmStoreFile;
     /** Member to hold the name of the file which holds the components private key and certificate */
     private static String privateKeyFile;
     /** Property key to tell where to locate the path and filename to the private key file. */
     private static final String PRIVATE_KEY_FILE = "org.bitrepository.alarm-service.privateKeyFile";
     /** The properties file holding implementation specifics for the alarm service. */
     private static final String CONFIGFILE = "alarmservice.properties"; 
     /** Property key for the property holding the path the file persisting alarms */
    private static final String ALARMSTOREFILE = "org.bitrepository.alarm-service.alarmstorefile";
     /** Default collection settings identifier (used to build the path the collection and referencesettings */
     private static final String DEFAULT_COLLECTION_ID = "bitrepository-devel";
     
     private AlarmStoreFactory() {
     	//Empty constructor
     }
     
     /**
      * Set the configuration directory. 
      * Should only be run at initialization time. 
      */
     public static synchronized void init(String configurationDir) {
     	confDir = configurationDir;
     }
     
     /**
      *	Factory method to get a singleton instance of BasicClient
      *	@return The BasicClient instance or a null in case of trouble.  
      */
     public static synchronized AlarmStore getAlarmStore() {
         if(alarmStore == null) {
         	if(confDir == null) {
         		throw new IllegalStateException("No configuration dir has been set!");
         	}
         	SettingsProvider settingsLoader = new SettingsProvider(new XMLFileSettingsLoader(confDir));
             Settings settings = settingsLoader.getSettings(DEFAULT_COLLECTION_ID);	 
             try {
             	loadProperties();
             	alarmStore = new AlarmStore(settings, alarmStoreFile, privateKeyFile);
             } catch (Exception e) {
                 throw new RuntimeException(e);
             }
         }
         return alarmStore;
     } 
     
     private static void loadProperties() throws IOException {
     	Properties properties = new Properties();
     	String propertiesFile = confDir + "/" + CONFIGFILE;
     	BufferedReader propertiesReader = new BufferedReader(new FileReader(propertiesFile));
     	properties.load(propertiesReader);
 
     	alarmStoreFile = properties.getProperty(ALARMSTOREFILE);
     	privateKeyFile = properties.getProperty(PRIVATE_KEY_FILE);
     }
     
 }
