 /*
  * Copyright (C) 2013 mewin<mewin001@hotmail.de>
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
 
 package de.mewin.killRewards;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.logging.Level;
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.yaml.snakeyaml.Yaml;
 
 /**
  *
  * @author mewin<mewin001@hotmail.de>
  */
 public class KillRewardsPlugin extends JavaPlugin
 {
     private KillListener listener;
     private HashMap<String, Integer> sprees;
     private HashMap<Integer, Reward> rewards;
     
     @Override
     public void onEnable()
     {
         sprees = new HashMap<String, Integer>();
         rewards = new HashMap<Integer, Reward>();
         listener = new KillListener(this);
         loadRewards();
         getServer().getPluginManager().registerEvents(listener, this);
     }
 
     @Override
     public void onDisable()
     {
         
     }
     
     public void addSpree(String player)
     {
         int spree = 1;
         if (sprees.containsKey(player))
         {
             spree = sprees.get(player) + 1;
         }
         sprees.put(player, spree);
         
         if (rewards.containsKey(spree))
         {
             Player pl = getServer().getPlayer(player);
            pl.sendMessage(ChatColor.GREEN + "You received " + rewards.get(spree).getName());
            rewards.get(spree).give(pl);
         }
     }
     
     public void resetSpree(String player)
     {
         sprees.remove(player);
     }
     
     public int getSpree(String player)
     {
         if (sprees.containsKey(player))
         {
             return sprees.get(player);
         }
         else
         {
             return 0;
         }
     }
     
     private void loadRewards()
     {
         File rewardsFile = new File(getDataFolder(), "rewards.yml");
         FileInputStream str = null;
         try
         {
             Yaml yaml = new Yaml();
             str = new FileInputStream(rewardsFile);
             HashMap<String, Object> map = (HashMap) yaml.load(str);
             ArrayList<HashMap> list = (ArrayList) map.get("rewards");
             for (HashMap rMap : list)
             {
                 Reward reward = Reward.rewardFromYaml(rMap);
                 if (reward != null)
                 {
                     rewards.put(reward.getKills(), reward);
                 }
             }
             str.close();
         }
         catch(Exception ex)
         {
             getLogger().log(Level.SEVERE, "Could not load rewards: ", ex);
         }
         finally
         {
             if (str != null)
             {
                 try
                 {
                     str.close();
                 }
                 catch(Exception ex)
                 {}
             }
         }
         
         getLogger().log(Level.INFO, "{0} rewards loaded.", rewards.size());
     }
 }
