 /*
  * The MIT License
  *
  * Copyright 2013 Manuel Gauto.
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 package org.mgenterprises.java.bukkit.gmcfps.Core.Configuration;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.mgenterprises.java.bukkit.gmcfps.Core.GameManagement.Game;
 import org.mgenterprises.java.bukkit.gmcfps.Core.Teams.Team;
 
 /**
  *
  * @author Manuel Gauto
  */
 public class ConfigurationManager {
 
     private File dataDirectory;
     private JavaPlugin plugin;
     
     public ConfigurationManager(JavaPlugin plugin) {
         this.dataDirectory = plugin.getDataFolder();
         this.plugin = plugin;
     }
 
     public ArrayList<File> getGameConfigurationFiles() {
         ArrayList<File> results = new ArrayList<File>();
         File[] files = dataDirectory.listFiles();
 
         for (File file : files) {
             if (file.isFile()) {
                 results.add(file);
             }
         }
         return results;
     }
 
     public Game processGameConfigurationFile(File file) {
         FileConfiguration gameConfig = YamlConfiguration.loadConfiguration(file);
 
         String name = gameConfig.getString("Name");
         boolean isFreeForAll = gameConfig.getBoolean("Freeforall");
         int scoreCap = gameConfig.getInt("ScoreCap");
         int maxSize = gameConfig.getInt("MaxSize");
         List<String> teamNames = gameConfig.getStringList("Teams");
         Game game = new Game(plugin, name);
         game.setMaxSize(maxSize);
         game.setScoreCap(scoreCap);
         game.getFPSCore().getTeamManager().setFreeForAll(isFreeForAll);
         for(String tname : teamNames){
             game.getFPSCore().getTeamManager().registerTeam(new Team(tname));
         }
         return game;
     }
     
     public void saveGameConfig(Game game){
         FileConfiguration gameConfig = new YamlConfiguration();
         gameConfig.set("Name", game.getName());
         gameConfig.set("Freeforall", game.getFPSCore().getTeamManager().isFreeForAll());
         gameConfig.set("ScoreCap", game.getScoreCap());
         gameConfig.set("MaxSize", game.getMaxSize());
         gameConfig.set("Teams", game.getFPSCore().getTeamManager().getAllTeams());
         try {
            gameConfig.save(dataDirectory+"/"+game.getName()+".yml");
         } catch (IOException ex) {
             Logger.getLogger(ConfigurationManager.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 }
