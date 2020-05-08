 /*
  * Copyright (C) 2011 MineStar.de 
  * 
  * This file is part of DirectorPlugin.
  * 
  * DirectorPlugin is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, version 3 of the License.
  * 
  * DirectorPlugin is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with DirectorPlugin.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package de.minestar.director;
 
 import java.io.File;
 
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.Event.Type;
 import org.bukkit.event.block.BlockListener;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import de.minestar.director.database.DatabaseHandler;
 import de.minestar.director.listener.BlockChangeListener;
 
 public class Main extends JavaPlugin {
 
     private DatabaseHandler dbHandler;
 
     public static void printToConsole(String msg) {
         System.out.println("[ DirectorsPlugin ] : " + msg);
     }
 
     @Override
     public void onDisable() {
         dbHandler = null;
 
     }
 
     @Override
     public void onEnable() {
 
         // when we don't have a connection to the database, the whole plugin
         // can't work
         if (!initDatabase()) {
             printToConsole("------------------------------------------");
             printToConsole("- COULD NOT CONNECT TO DIRECTOR DATABASE -");
             printToConsole("------------------------------------------");
             return;
         }
 
         // Register event listener
         PluginManager pm = getServer().getPluginManager();
         BlockListener bListener = new BlockChangeListener(dbHandler);
         pm.registerEvent(Type.BLOCK_BREAK, bListener, Priority.Normal, this);
         pm.registerEvent(Type.BLOCK_PLACE, bListener, Priority.Normal, this);
 
         printToConsole(getDescription().getVersion() + " is enabled!");
     }
 
     private boolean initDatabase() {
 
         try {
 
             File f = new File("plugins/DirectorsPlugin/");
             f.mkdirs();
             f = new File(f.getAbsolutePath() + "/sqlconfig.yml");
             FileConfiguration sqlConfig = getConfig();
 
             if (!f.exists()) {
                 printToConsole("Can't find sql configuration!");
                 printToConsole("Create an empty configuration file at plugins/DirectorsPlugin/sqlconfig.yml");
                 f.createNewFile();
                 sqlConfig.set("host", "host");
                 sqlConfig.set("port", "port");
                 sqlConfig.set("database", "database");
                 sqlConfig.set("username", "userName");
                 sqlConfig.set("password", "passwort");
                 sqlConfig.save(f);
                 return false;
             }
            sqlConfig.load(f);
             dbHandler = new DatabaseHandler(sqlConfig.getString("host"), sqlConfig.getInt("port"), sqlConfig.getString("database"), sqlConfig.getString("username"), sqlConfig.getString("password"));
         } catch (Exception e) {
             e.printStackTrace();
             return false;
         }
 
         return true;
     }
 }
