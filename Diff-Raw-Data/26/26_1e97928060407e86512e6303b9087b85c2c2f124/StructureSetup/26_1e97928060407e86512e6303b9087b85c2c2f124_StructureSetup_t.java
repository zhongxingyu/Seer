 /*
  * Copyright (C) 2013 Admin
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
 package com.arix.data;
 
 import com.arix.Server;
 import com.arix.utils.Log;
 import java.io.File;
 import java.io.IOException;
 
 /**
  *
  * @author Arix
  */
 public class StructureSetup {
 
    public static final File DATA_FOLDER = new File("./data");
    public static final File PLUGIN_FOLDER = new File("./plugins");
    public static final File SAVE_FOLDER = new File("./data/players");
    public static final File CONFIG_FOLDER = new File("./data/configurations");
    public static final File SERVER_CONFIG = new File(CONFIG_FOLDER.getPath() + "\\" + "server.arx");
 
     private static final File[] dirList = {DATA_FOLDER, PLUGIN_FOLDER, SAVE_FOLDER, CONFIG_FOLDER};
 
     /**
      * Checks and validates if the file/folder structure is healthy.
      */
     public static void checkStructureIntegrity() {
 
         for (File f : dirList) {
             if (!f.exists()) {
                 try {
                     Log.debug("Creating: " + f.getName() + "(" + f.getPath() + ")");
                     f.mkdir();
 
                 } catch (Exception e) {
                     Log.error("Could not repair/create data structure.");
                 }
             }
         }
 
         if (!SERVER_CONFIG.exists()) {
             try {
                 SERVER_CONFIG.createNewFile();
                 ArxFile af = new ArxFile();
                 af.setString("name", "RSPS Server");
                 af.setInt("version", 1);
                 af.setBoolean("site", true);

                 ArxParser.writeArxFile(af.getData(), SERVER_CONFIG);
                 Log.debug("Created server configuration file.");
             } catch (IOException e) {
                 Log.error("Could not create Server Configuration File.\nReason: " + e.getMessage());
             }
         } else {

         }
 
     }
 }
