 /*
  * #%L
  * bitrepository-access-client
  * *
  * $Id$
  * $HeadURL$
  * %%
  * Copyright (C) 2010 - 2011 The State and University Library, The Royal Library and The State Archives, Denmark
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
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.Properties;
 import org.bitrepository.alarm.handling.handlers.AlarmStorer;
 import org.bitrepository.alarm.store.AlarmServiceDAO;
 import org.bitrepository.alarm.store.AlarmStore;
 import org.bitrepository.common.settings.Settings;
 import org.bitrepository.common.settings.XMLFileSettingsLoader;
 import org.bitrepository.protocol.ProtocolComponentFactory;
 import org.bitrepository.protocol.messagebus.MessageBus;
 import org.bitrepository.protocol.security.BasicMessageAuthenticator;
 import org.bitrepository.protocol.security.BasicMessageSigner;
 import org.bitrepository.protocol.security.BasicOperationAuthorizor;
 import org.bitrepository.protocol.security.BasicSecurityManager;
 import org.bitrepository.protocol.security.MessageAuthenticator;
 import org.bitrepository.protocol.security.MessageSigner;
 import org.bitrepository.protocol.security.OperationAuthorizor;
 import org.bitrepository.protocol.security.PermissionStore;
 import org.bitrepository.protocol.security.SecurityManager;
 import org.bitrepository.service.ServiceSettingsProvider;
 import org.bitrepository.service.contributor.ContributorMediator;
 import org.bitrepository.service.contributor.SimpleContributorMediator;
 import org.bitrepository.settings.referencesettings.ServiceType;
 
 /**
  * Class for launching an alarm service.
  */
 public class AlarmServiceFactory {
     /** The alarm service. 
      * @see #getAlarmService().*/
     private static AlarmService alarmService;
     /** The path to the directory containing the configuration files.*/
     private static String configurationDir;
     /** The path to the private key file.*/
     private static String privateKeyFile;
     
     /** The properties file holding implementation specifics for the alarm service. */
     private static final String CONFIGFILE = "alarmservice.properties";
     /** Property key to tell where to locate the path and filename to the private key file. */
     private static final String PRIVATE_KEY_FILE = "org.bitrepository.alarm-service.privateKeyFile";
         
     /**
      * Private constructor as the class is meant to be used in a static way.
      */
     private AlarmServiceFactory() { }
     
     /**
      * Initialize the factory with configuration. 
      * @param confDir String containing the path to the AlarmService's configuration directory
      */
     public static synchronized void init(String confDir) {
         configurationDir = confDir;
     }
     
     /**
      * Factory method to retrieve AlarmService  
      * @return The AlarmService.
      */
     public static synchronized AlarmService getAlarmService() {
         if(alarmService == null) {
             MessageAuthenticator authenticator;
             MessageSigner signer;
             OperationAuthorizor authorizer;
             PermissionStore permissionStore;
             SecurityManager securityManager;
             ServiceSettingsProvider settingsLoader =
                     new ServiceSettingsProvider(new XMLFileSettingsLoader(configurationDir), ServiceType.ALARM_SERVICE);
 
             Settings settings = settingsLoader.getSettings();
             try {
                 loadProperties();
                 permissionStore = new PermissionStore();
                 authenticator = new BasicMessageAuthenticator(permissionStore);
                 signer = new BasicMessageSigner();
                 authorizer = new BasicOperationAuthorizor(permissionStore);
                 securityManager = new BasicSecurityManager(settings.getCollectionSettings(), privateKeyFile, 
                         authenticator, signer, authorizer, permissionStore, 
                        settings.getReferenceSettings().getAlarmServiceSettings().getID());
                 
                 MessageBus messageBus = ProtocolComponentFactory.getInstance().getMessageBus(settings, 
                         securityManager);
                 ContributorMediator contributorMediator = new SimpleContributorMediator(messageBus, settings, null);
                 
                 AlarmStore store = new AlarmServiceDAO(settings);
                 alarmService = new BasicAlarmService(messageBus, settings, store, contributorMediator);
                 
                 // Add the default handler for putting the alarms into the database.
                 alarmService.addHandler(new AlarmStorer(store));
             } catch (IOException e) {
                 throw new RuntimeException(e);
             }
         }
         
         return alarmService;
     }
     
     /**
      * Loads the properties.
      * @throws IOException If any input/output issues occurs.
      */
     private static void loadProperties() throws IOException {
         Properties properties = new Properties();
         File propertiesFile = new File(configurationDir, CONFIGFILE);
         BufferedReader propertiesReader = new BufferedReader(new FileReader(propertiesFile));
         properties.load(propertiesReader);
         privateKeyFile = properties.getProperty(PRIVATE_KEY_FILE);
     }
 }
