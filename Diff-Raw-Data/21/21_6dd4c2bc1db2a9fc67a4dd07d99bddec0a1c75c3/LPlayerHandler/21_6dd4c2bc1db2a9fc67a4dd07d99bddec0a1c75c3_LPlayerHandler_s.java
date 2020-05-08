 /*
 This file is part of Legends.
 
     Legends is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     Legends is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with Legends.  If not, see <http://www.gnu.org/licenses/>.
 */
 package com.github.legendsdev.legends.library.lplayer;
 
 import com.github.legendsdev.legends.library.BasicHandler;
 import com.github.legendsdev.legends.library.Configuration;
 import com.github.legendsdev.legends.library.database.DataManager;
 import com.github.legendsdev.legends.library.lclass.LClassHandler;
 import com.github.legendsdev.legends.library.race.RaceHandler;
 
 import java.util.logging.Logger;
 
 /**
  * @author B2OJustin
  */
 public class LPlayerHandler extends BasicHandler<LPlayer> {
     private static Logger logger = Logger.getLogger(LPlayerHandler.class.getName());
     private static LPlayerHandler instance = new LPlayerHandler();
 
     public static LPlayerHandler getInstance() {
         return instance;
     }
 
     public LPlayer load(String id) {
         LPlayer lPlayer = super.get(id);
 
         // Attempt load from cache
         if(lPlayer == null) {
             lPlayer = LPlayerCache.getPlayer(id);
            if(lPlayer != null) register(id, lPlayer);
            logger.info("Loaded player '" + id + "' from cache");
         }
 
         // Attempt load from database
         if(lPlayer == null) {
             lPlayer = DataManager.getDataSource().loadLPlayer(id);
            if(lPlayer != null) register(id, lPlayer);
            logger.info("Loaded player data for '" + id + "' from database");
         }
 
         // Create new player data
         if(lPlayer == null) {
             Configuration config = Configuration.getInstance();
             lPlayer = new LPlayer(
                     RaceHandler.getInstance().load(config.getDefaultRace()),
                     LClassHandler.getInstance().load(config.getDefaultPrimaryClass()),
                     LClassHandler.getInstance().load(config.getDefaultSecondaryClass()
             ));
             super.register(id, lPlayer);
             logger.info("Created new player data for '" + id + "'");
             DataManager.getDataSource().saveLPlayer(lPlayer);
         }
 
         return lPlayer;
     }
 
     public LPlayerHandler remove(String id, boolean cache) {
         if(cache) {
             LPlayer lPlayer = get(id);
             LPlayerCache.cachePlayer(id, lPlayer);
         }
         super.remove(id);
         return this;
     }
 }
