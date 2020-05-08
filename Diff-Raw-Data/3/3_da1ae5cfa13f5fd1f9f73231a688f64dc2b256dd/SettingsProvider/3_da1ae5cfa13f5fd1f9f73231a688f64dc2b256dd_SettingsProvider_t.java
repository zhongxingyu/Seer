 /*
  * #%L
  * Bitrepository Protocol
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
 package org.bitrepository.common.settings;
 
 import org.bitrepository.protocol.messagebus.destination.DestinationHelper;
 import org.bitrepository.settings.collectionsettings.CollectionSettings;
 import org.bitrepository.settings.referencesettings.ReferenceSettings;
 
 /**
  * Used for accessing <code>Settings</code> objects. A <code>SettingsLoader</code> needs to be provides on 
  * instantiation for loading stored settings.
  */
 public class SettingsProvider {
     /** The loader to use for accessing stored settings*/
     private final SettingsLoader settingsReader;
     /** The loaded settings */
     private Settings settings;
     
     /**
      * Creates a <code>SettingsProvider</code> which will use the provided <code>SettingsLoader</code> for loading the 
      * settings.
      * @param settingsReader Use for loading the settings.
      */
     public SettingsProvider(SettingsLoader settingsReader) {
         this.settingsReader = settingsReader;
     }
     
     /**
      * Gets the settings, if no settings has been loaded into memory, will load the settings from disk
      * @return The settings 
      */
     public synchronized Settings getSettings(String componentID) {
         if(settings == null) {
             reloadSettings(componentID);
         }
         return settings;
     }
     
     /**
     * Will load the settings from disk. Will overwrite any settings already in the provider.
     */
     public synchronized void reloadSettings(String componentID) {
     	CollectionSettings collectionSettings = settingsReader.loadSettings(CollectionSettings.class);
     	ReferenceSettings referenceSettings = settingsReader.loadSettings(ReferenceSettings.class);
 
         String receiverDestinationIDFactoryClass = null;
         if (referenceSettings.getGeneralSettings() != null) {
            receiverDestinationIDFactoryClass =
                referenceSettings.getGeneralSettings().getReceiverDestinationIDFactoryClass();
         }
 
         DestinationHelper dh = new DestinationHelper(
                 componentID,
                 receiverDestinationIDFactoryClass,
                 collectionSettings.getProtocolSettings().getCollectionDestination());
         settings = new Settings(componentID, dh.getReceiverDestinationID(), collectionSettings, referenceSettings);
     }
 
     /**
      * Will load the reference settings from disk.
      * @return The loaded referenceSettings
      */
     public synchronized ReferenceSettings loadReferenceSettings() {
         return settingsReader.loadSettings(ReferenceSettings.class);
     }
 }
