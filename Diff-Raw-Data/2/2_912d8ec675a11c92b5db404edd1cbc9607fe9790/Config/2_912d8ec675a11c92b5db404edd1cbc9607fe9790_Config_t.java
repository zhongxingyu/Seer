 /**
  * Copyright (c) 2011-2013 Robert Maupin
  * 
  * This software is provided 'as-is', without any express or implied
  * warranty. In no event will the authors be held liable for any damages
  * arising from the use of this software.
  * 
  * Permission is granted to anyone to use this software for any purpose,
  * including commercial applications, and to alter it and redistribute it
  * freely, subject to the following restrictions:
  * 
  *    1. The origin of this software must not be misrepresented; you must not
  *    claim that you wrote the original software. If you use this software
  *    in a product, an acknowledgment in the product documentation would be
  *    appreciated but is not required.
  * 
  *    2. Altered source versions must be plainly marked as such, and must not be
  *    misrepresented as being the original software.
  * 
  *    3. This notice may not be removed or altered from any source
  *    distribution.
  */
 package org.csdgn.fxm;
 
 import java.io.File;
 import java.io.FileReader;
 import java.util.Properties;
 import java.util.UUID;
 
 /**
  * @author Chase
  */
 public class Config {
 	public static String FOLDER_DATABASE = null;
 	public static String FOLDER_USER = null;
 	public static String FOLDER_CHARACTER = null;
 	public static String FOLDER_WORLD = null;
 	public static String FILE_WELCOME = null;
 	public static UUID START_ROOM_UUID = null;
 
 	public static void loadConfiguration() {
 		Properties properties = new Properties();
 		
 		try {
 			properties.load(new FileReader("config.properties"));
 		} catch(Exception e) {
 			System.out.println("Warning: Failed to load configuration file, using defaults.");
 		}
 
 		FOLDER_DATABASE = properties.getProperty("folder.db", "db/").trim();
 		FOLDER_USER = FOLDER_DATABASE
 				+ properties.getProperty("folder.user", "user/").trim();
 		FOLDER_CHARACTER = FOLDER_DATABASE
 				+ properties.getProperty("folder.character", "characters/")
 						.trim();
 		FOLDER_WORLD = FOLDER_DATABASE
 				+ properties.getProperty("folder.world", "world/").trim();
		FILE_WELCOME = FOLDER_DATABASE
 				+ properties.getProperty("file.welcome", "welcome").trim();
 		
 		START_ROOM_UUID = UUID.fromString(properties.getProperty("start.uuid",
 				"00000000-0000-0000-0000-000000000000").trim());
 	}
 	
 	public static void createMissingFolders() {
 		new File(FOLDER_DATABASE).mkdir();
 		new File(FOLDER_USER).mkdir();
 		new File(FOLDER_CHARACTER).mkdir();
 		new File(FOLDER_WORLD).mkdir();
 	}
 
 }
