 /*
  * Copyright (C) 2011 halvors <halvors@skymiastudios.com>
  * Copyright (C) 2011 speeddemon92 <speeddemon92@gmail.com>
  *
  * This file is part of Wolf.
  *
  * Wolf is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * Wolf is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with Wolf.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.halvors.Wolf;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.persistence.PersistenceException;
 
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.halvors.Wolf.command.WolfCommandExecutor;
 import com.halvors.Wolf.listeners.WolfEntityListener;
 import com.halvors.Wolf.listeners.WolfPlayerListener;
 import com.halvors.Wolf.listeners.WolfWorldListener;
 import com.halvors.Wolf.util.ConfigManager;
 import com.halvors.Wolf.wolf.SelectedWolfManager;
 import com.halvors.Wolf.wolf.WolfInventoryManager;
 import com.halvors.Wolf.wolf.WolfInventoryTable;
 import com.halvors.Wolf.wolf.WolfManager;
 import com.halvors.Wolf.wolf.WolfTable;
 import com.nijiko.permissions.PermissionHandler;
 import com.nijikokun.bukkit.Permissions.Permissions;
 
 public class Wolf extends JavaPlugin {
     public String name;
     public String version;
     
     private final Logger log = Logger.getLogger("Minecraft");
     
     private PluginManager pm;
     private PluginDescriptionFile pdfFile;
 
     private final ConfigManager configManager = new ConfigManager(this);
     private final WolfManager wolfManager = new WolfManager(this);
     private final WolfInventoryManager wolfInventoryManager = new WolfInventoryManager(this);
     private final SelectedWolfManager selectedWolfManager = new SelectedWolfManager(this);
     
     private final WolfEntityListener entityListener = new WolfEntityListener(this);
     private final WolfPlayerListener playerListener = new WolfPlayerListener(this);
     private final WolfWorldListener worldListener = new WolfWorldListener(this);
   
     
     public static PermissionHandler Permissions;
     
     private final HashMap<Player, Boolean> debugees = new HashMap<Player, Boolean>();
     
     @Override
     public void onEnable() {
         pm = this.getServer().getPluginManager();
         pdfFile = this.getDescription();
         
         // Load name and version from pdfFile
         name = pdfFile.getName();
         version = pdfFile.getVersion();
         
         // Load Configuration
         configManager.load();
         
         // Register our events Type.          
         pm.registerEvent(Event.Type.CREATURE_SPAWN, entityListener, Event.Priority.Normal, this);
         pm.registerEvent(Event.Type.ENTITY_DAMAGE, entityListener, Event.Priority.Normal, this);
         pm.registerEvent(Event.Type.ENTITY_DEATH, entityListener, Event.Priority.Normal, this);
 //      pm.registerEvent(Event.Type.ENTITY_TAME, entityListener, Event.Priority.Normal, this);
         pm.registerEvent(Event.Type.ENTITY_TARGET, entityListener, Event.Priority.Normal, this);
         
         pm.registerEvent(Event.Type.PLAYER_INTERACT, playerListener, Event.Priority.Normal, this);
         pm.registerEvent(Event.Type.PLAYER_INTERACT_ENTITY, playerListener, Event.Priority.Normal, this);
         
         pm.registerEvent(Event.Type.CHUNK_LOAD, worldListener, Event.Priority.Normal, this);
         pm.registerEvent(Event.Type.CHUNK_UNLOAD, worldListener, Event.Priority.Normal, this);
         pm.registerEvent(Event.Type.WORLD_LOAD, worldListener, Event.Priority.Normal, this);
         pm.registerEvent(Event.Type.WORLD_UNLOAD, worldListener, Event.Priority.Normal, this);
         
         // Register our commands
         this.getCommand("wolf").setExecutor(new WolfCommandExecutor(this));
         
         log(Level.INFO, "version " + version + " is enabled!");
         
         setupPermissions();
         setupDatabase();
     }
     
     @Override
     public void onDisable() {
         configManager.save();
         
         log(Level.INFO, "Plugin disabled!");
     }
     
     private void setupPermissions() {
         Plugin permissions = this.getServer().getPluginManager().getPlugin("Permissions");
 
         if (Permissions == null) {
             if (permissions != null) {
                 Permissions = ((Permissions) permissions).getHandler();
             } else {
                 log(Level.INFO, "Permission system not detected, defaulting to OP");
             }
         }
     }
     
     private void setupDatabase() {
         try {
             this.getDatabase().find(WolfTable.class).findRowCount();
         } catch (PersistenceException ex) {
             log(Level.INFO, "Installing database for " + getDescription().getName() + " due to first time usage");
             installDDL();
         }
     }
     
     @Override
     public List<Class<?>> getDatabaseClasses() {
         List<Class<?>> list = new ArrayList<Class<?>>();
         list.add(WolfTable.class);
         list.add(WolfInventoryTable.class);
         
         return list;
     }
     
     public boolean hasPermissions(Player player, String node) {
         if (Permissions != null) {
             return Permissions.has(player, node);
         } else {
             return player.isOp();
         }
     }
     
     public boolean isDebugging(final Player player) {
         if (debugees.containsKey(player)) {
             return debugees.get(player);
         } else {
             return false;
         }
     }
 
     public void setDebugging(final Player player, final boolean value) {
         debugees.put(player, value);
     }
     
     public void log(Level level, String msg) {
         this.log.log(level, "[" + name + "] " + msg);
     }
     
     public ConfigManager getConfigManager() {
         return configManager;
     }
     
     public WolfManager getWolfManager() {
         return wolfManager;
     }
     
     public WolfInventoryManager getWolfInventoryManager() {
         return wolfInventoryManager;
     }
     
     public SelectedWolfManager getSelectedWolfManager() {
         return selectedWolfManager;
     }
 }
