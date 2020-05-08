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
 
 import org.bitrepository.alarm.handler.AlarmLogger;
 import org.bitrepository.common.settings.Settings;
 import org.bitrepository.common.settings.SettingsProvider;
 import org.bitrepository.common.settings.XMLFileSettingsLoader;
 
 /**
  * Class for launching an alarm service.
  */
 public class AlarmServiceLauncher {
     /** The default bitrepository collection id. Used if no arguments.*/
     private static final String DEFAULT_COLLECTION_ID = "bitrepository-devel";
     /** The default the settings directory. Used if no arguments.*/
    private static final String DEFAULT_PATH_TO_SETTINGS = "src/test/resources/settings/xml";
 
     /**
      * @param args 
      */
     public static void main(String[] args) {
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
             // Instantiate the AlarmService.
             AlarmService alarm = AlarmComponentFactory.getInstance().getAlarmService(settings);
             alarm.addHandler(new AlarmLogger(), settings.getAlarmDestination());
         } catch (Exception e) {
             e.printStackTrace();
             System.out.println();
             System.out.println("Use with two arguments: AlarmServiceLauncher 'pathToSettings' "
                     + "'bitrepository collection id'");
             System.exit(0);
         }
     }
 }
