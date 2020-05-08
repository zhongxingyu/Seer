 /*
  * Copyright (C) 2012 MineStar.de 
  * 
  * This file is part of CastAway.
  * 
  * CastAway is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, version 3 of the License.
  * 
  * CastAway is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with CastAway.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package de.minestar.castaway.core;
 
 import java.io.File;
 
 import org.bukkit.event.Listener;
 import org.bukkit.plugin.PluginManager;
 
 import de.minestar.castaway.command.cmdCastAway;
 import de.minestar.castaway.command.cmdCreateDungeon;
 import de.minestar.castaway.command.cmdDeleteDungeon;
 import de.minestar.castaway.command.cmdRespawn;
 import de.minestar.castaway.database.DatabaseManager;
 import de.minestar.castaway.listener.GameListener;
 import de.minestar.castaway.listener.RegisterListener;
 import de.minestar.castaway.manager.DungeonManager;
 import de.minestar.castaway.manager.GameManager;
 import de.minestar.castaway.manager.PlayerManager;
 import de.minestar.minestarlibrary.AbstractCore;
 import de.minestar.minestarlibrary.commands.CommandList;
 
 public class CastAwayCore extends AbstractCore {
 
     public static final String NAME = "CastAway";
 
     public static DatabaseManager databaseManager;
 
     public static DungeonManager dungeonManager;
     public static GameManager gameManager;
     public static PlayerManager playerManager;
 
     private Listener gameListener, registerListener;
 
     public CastAwayCore() {
         super(NAME);
     }
 
     @Override
     protected boolean loadingConfigs(File dataFolder) {
         return Settings.init(dataFolder, NAME, getDescription().getVersion());
     }
 
     @Override
     protected boolean createManager() {
 
         databaseManager = new DatabaseManager(NAME, new File(getDataFolder(), "sqlconfig.yml"));
         if (!databaseManager.hasConnection())
             return false;
 
         dungeonManager = new DungeonManager();
         gameManager = new GameManager();
         playerManager = new PlayerManager();
 
         dungeonManager.init();
         gameManager.init();
 
         return true;
     }
 
     @Override
     protected boolean createListener() {
         this.registerListener = new RegisterListener();
         this.gameListener = new GameListener();
         return true;
     }
 
     @Override
     protected boolean registerEvents(PluginManager pm) {
         pm.registerEvents(this.registerListener, this);
         pm.registerEvents(this.gameListener, this);
         return true;
     }
 
     @Override
     protected boolean createCommands() {
 
         // @formatter:off        
         this.cmdList = new CommandList(
                 new cmdRespawn("/respawn",      "",        "castaway.command.respawn"),
                 
                 new cmdCastAway("/castaway", "", "castaway.command",                 
                     new cmdCreateDungeon("create",      "<DungeonName>",        "castaway.command.createdungeon"),
                     new cmdDeleteDungeon("delete",      "<DungeonName>",        "castaway.command.deletedungeon")
                 )
         );
         return true;
         // @formatter:on
     }
     @Override
     protected boolean commonDisable() {
        databaseManager.closeConnection();
        return !databaseManager.hasConnection();
     }
 }
