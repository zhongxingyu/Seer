 /*
  * #%L
  * Bitrepository Integration
  * 
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
 package org.bitrepository.pillar;
 
 import org.bitrepository.common.settings.Settings;
 import org.bitrepository.common.settings.SettingsProvider;
 import org.bitrepository.common.settings.XMLFileSettingsLoader;
 import org.bitrepository.pillar.checksumpillar.ChecksumPillar;
 import org.bitrepository.pillar.checksumpillar.cache.FilebasedChecksumStore;
 import org.bitrepository.pillar.referencepillar.ReferencePillar;
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
import org.bitrepository.settings.referencesettings.ReferenceSettings;
 
 /**
  * Component factory for this module.
  */
 public final class PillarComponentFactory {
     /** The singleton instance. */
     private static PillarComponentFactory instance;
 
     /**
      * Instantiation of this singleton.
      *
      * @return The singleton instance of this factory class.
      */
     public static synchronized PillarComponentFactory getInstance() {
         // ensure singleton.
         if(instance == null) {
             instance = new PillarComponentFactory();
         }
         return instance;
     }
 
     /**
      * Private constructor for initialization of the singleton.
      */
     private PillarComponentFactory() {
     }
 
     /**
      * Method for retrieving a reference pillar.
      * @param pathToSettings The path to the directory containing the settings. See {@link XMLFileSettingsLoader} for details.
      * @param pathToKeyFile The path to the private key file with the certificates for communication.
      * @param pillarID The pillars componentID.
      * @return The reference requested pillar.
      */
    public ReferencePillar createReferencePillar(String pathToSettings, String pathToKeyFile, String pillarID) {
         Settings settings = loadSettings(pillarID, pathToSettings);
         SecurityManager securityManager = loadSecurityManager(pathToKeyFile, settings);
 
         MessageBus messageBus = ProtocolComponentFactory.getInstance().getMessageBus(settings, securityManager);
 
         return new ReferencePillar(messageBus, settings);
     }
     
     /**
      * Method for retrieving a checksum pillar.
      * @param pathToSettings The path to the directory containing the settings. See {@link XMLFileSettingsLoader} for details.</li>
      * @param pathToKeyFile The path to the private key file with the certificates for communication.</li>
      * @param pillarID The pillars componentID.</li>
      * @return The reference requested checksum pillar.
      */
     public ChecksumPillar createChecksumPillar(
             String pathToSettings, String pathToKeyFile, String pillarID) {
         Settings settings = loadSettings(pillarID, pathToSettings);
         SecurityManager securityManager = loadSecurityManager(pathToKeyFile, settings);
 
         MessageBus messageBus = ProtocolComponentFactory.getInstance().getMessageBus(settings, securityManager);
         
         return new ChecksumPillar(messageBus, settings, new FilebasedChecksumStore(settings));
     }
 
     /** The default path for the settings in the development.*/
     private static final String DEFAULT_PATH_TO_SETTINGS = "conf";
     /** The default path for the settings in the development.*/
     private static final String DEFAULT_PATH_TO_KEY_FILE = "conf/client.pem";
 
     /**
      * Method for retrieving the settings for the launcher.
      * @param pathToSettings The path to the settings. If it is null or empty, then the default path is used.
      * @return The settings.
      */
     private static Settings loadSettings(String pillarID, String pathToSettings) {
         SettingsProvider settingsLoader;
         if(pathToSettings == null || pathToSettings.isEmpty()) {
             settingsLoader = new SettingsProvider(new XMLFileSettingsLoader(DEFAULT_PATH_TO_SETTINGS));
         } else {
             settingsLoader = new SettingsProvider(new XMLFileSettingsLoader(pathToSettings));
         }
        
        if(pillarID == null) {
            ReferenceSettings referenceSettings = settingsLoader.loadReferenceSettings();
            pillarID = referenceSettings.getPillarSettings().getPillarID();
        }
 
         return settingsLoader.getSettings(pillarID);
     }
 
     /**
      * Instantiates the security manager based on the settings and the path to the key file.
      * @param pathToPrivateKeyFile The path to the key file.
      * @param settings The settings.
      * @return The security manager.
      */
     private static BasicSecurityManager loadSecurityManager(String pathToPrivateKeyFile, Settings settings) {
         String privateKeyFile;
         if(pathToPrivateKeyFile == null || pathToPrivateKeyFile.isEmpty()) {
             privateKeyFile = DEFAULT_PATH_TO_KEY_FILE;
         } else {
             privateKeyFile = pathToPrivateKeyFile;
         }
 
         PermissionStore permissionStore = new PermissionStore();
         MessageAuthenticator authenticator = new BasicMessageAuthenticator(permissionStore);
         MessageSigner signer = new BasicMessageSigner();
         OperationAuthorizor authorizer = new BasicOperationAuthorizor(permissionStore);
         return new BasicSecurityManager(settings.getCollectionSettings(), privateKeyFile,
                 authenticator, signer, authorizer, permissionStore,
                 settings.getComponentID());
     }
 }
