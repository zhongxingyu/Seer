 /*
  * Copyright (C) 2012 MineStar.de 
  * 
  * This file is part of MinestarCore.
  * 
  * MinestarCore is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, version 3 of the License.
  * 
  * MinestarCore is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with MinestarCore.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package de.minestar.core;
 
 import java.io.File;
 
 import org.anjocaido.groupmanager.GroupManager;
 import org.bukkit.Bukkit;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import de.minestar.core.listener.ConnectionListener;
 import de.minestar.core.manager.PlayerManager;
 import de.minestar.core.units.MinestarPlayer;
 import de.minestar.minestarlibrary.utils.ConsoleUtils;
 
 public class MinestarCore extends JavaPlugin {
 
     public static String pluginName = "MinestarCore";
     public static File dataFolder;
     public static GroupManager groupManager = null;
 
     /**
      * Manager
      */
     private static PlayerManager playerManager;
 
     /**
      * Listener
      */
     private ConnectionListener connectionListener;
 
     @Override
     public void onDisable() {
         // SAVE PLAYERS
         playerManager.savePlayers();
 
         // PRINT INFO
         ConsoleUtils.printInfo(pluginName, "Disabled v" + this.getDescription().getVersion() + "!");
     }
 
     @Override
     public void onEnable() {
         dataFolder = this.getDataFolder();
         dataFolder.mkdirs();
 
         // GET GROUPMANAGER
         this.getGroupManager();
 
        File playerFolder = new File(dataFolder, "playerdata");
         playerFolder.mkdir();
 
         // CREATE MANAGER, LISTENER, COMMANDS
         this.createManager();
         this.createListener();
 
         // REGISTER EVENTS
         this.registerEvents();
 
         // PRINT INFO
         ConsoleUtils.printInfo(pluginName, "Enabled v" + this.getDescription().getVersion() + "!");
     }
 
     private void getGroupManager() {
         Plugin gm = Bukkit.getServer().getPluginManager().getPlugin("GroupManager");
         if (gm != null && gm.isEnabled())
             MinestarCore.groupManager = (GroupManager) gm;
         else
             ConsoleUtils.printError(MinestarCore.pluginName, "Can't find GroupManager was not found!");
     }
 
     private void createManager() {
         playerManager = new PlayerManager();
     }
 
     private void createListener() {
         this.connectionListener = new ConnectionListener(playerManager);
     }
 
     private void registerEvents() {
         Bukkit.getPluginManager().registerEvents(this.connectionListener, this);
     }
 
     public static MinestarPlayer getPlayer(String playerName) {
         return playerManager.getPlayer(playerName);
     }
 
     public static MinestarPlayer getPlayer(Player player) {
         return MinestarCore.getPlayer(player.getName());
     }
 }
