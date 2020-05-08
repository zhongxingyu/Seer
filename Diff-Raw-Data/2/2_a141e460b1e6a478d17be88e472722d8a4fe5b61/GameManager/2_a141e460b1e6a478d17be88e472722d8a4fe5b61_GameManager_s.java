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
 package org.mgenterprises.java.bukkit.gmcfps.Core.GameManagement;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.HashMap;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.mgenterprises.java.bukkit.gmcfps.Core.Configuration.ConfigurationManager;
 import org.mgenterprises.java.bukkit.gmcfps.Core.Weapons.Weapon;
 
 /**
  *
  * @author Manuel Gauto
  */
 public class GameManager {
 
     HashMap<String, Game> games = new HashMap<String, Game>();
     private ConfigurationManager configManager;
     private JavaPlugin plugin;
     private ArrayList<Weapon> defaultWeapons = new ArrayList<Weapon>();
 
     public GameManager(JavaPlugin plugin) {
         configManager = new ConfigurationManager(plugin);
         this.plugin = plugin;
     }
 
     public void addDefaultWeapon(Weapon w) {
         this.defaultWeapons.add(w);
     }
 
     public JavaPlugin getPluginReference() {
         return this.plugin;
     }
 
     public Game getGameByName(String name) {
         return this.games.get(name);
     }
 
     public void registerGame(Game g) {
         Game game = g;
         for (Weapon w : defaultWeapons) {
             w.setWeaponManager(g.getFPSCore().getWeaponManager());
             game.getFPSCore().getWeaponManager().registerWeapon(w);
         }
         plugin.getServer().getPluginManager().registerEvents(g.getFPSCore().getCombatListener(), plugin);
         plugin.getServer().getPluginManager().registerEvents(g.getFPSCore().getWeaponListeners(), plugin);
         games.put(g.getName(), game);
     }
 
     public void saveAllGames() {
         for (Game game : games.values()) {
             configManager.saveGameConfig(game);
         }
     }
 
     public Game getPlayerGame(Player p) {
         for (Game g : this.games.values()) {
             if (g.getFPSCore().getTeamManager().isParticipating(p)) {
                 return g;
             }
         }
         return null;
     }
 
     public void loadAllGames() {
         ArrayList<File> gameFilesLoaded = configManager.getGameConfigurationFiles();
 
         for (File f : gameFilesLoaded) {
             Game g = configManager.processGameConfigurationFile(f);
            games.put(g.getName(), g);
         }
     }
 }
