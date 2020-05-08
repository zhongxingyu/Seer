 /*
  * #%L
  * Bitmagasin integrationstest
  * 
  * $Id$
  * $HeadURL$
  * %%
  * Copyright (C) 2010 The State and University Library, The Royal Library and The State Archives, Denmark
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
 import org.bitrepository.integration.IntegrationComponentFactory;
 
 /**
  * Method for launching the ReferencePillar. 
  * It just loads the configurations and uses them to create the PillarSettings needed for starting the ReferencePillar.
  */
 public class ReferencePillarLauncher {
     private static final String DEFAULT_COLLECTION_ID = "bitrepository-devel";
    private static final String DEFAULT_PATH_TO_SETTINGS = "src/test/resources/settings/xml";
 
     /**
      * @param args <ol>
      * <li> The path to the directory containing the settings. See {@link XMLFileSettingsLoader} for details.</li>
      * <li> The collection ID to load the settings for.</li>
      * </ol>
      */
     public static void main(String[] args) throws Exception {
         String collectionId;
         String pathToSettings;
         if(args.length >= 2) {
             collectionId = args[0];
             pathToSettings = args[1];
         } else {
             collectionId = DEFAULT_COLLECTION_ID;
             pathToSettings = DEFAULT_PATH_TO_SETTINGS;
         }
         
         SettingsProvider settingsLoader = new SettingsProvider(
                 new XMLFileSettingsLoader(pathToSettings));
         try {
             Settings settings = settingsLoader.getSettings(collectionId);
             IntegrationComponentFactory.getInstance().getPillar(settings);
         } catch (Exception e) {
             e.printStackTrace();
             System.exit(0);
         }
     }
 }
