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
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import org.bukkit.entity.Player;
 
 /**
  *
  * @author mewin<mewin001@hotmail.de>
  */
 public class MultiReward extends Reward
 {
 
     protected ArrayList<Reward> rewards;
     
     public MultiReward(int kills, String name, ArrayList<HashMap> list)
     {
         super(kills, name);
         
         rewards = new ArrayList<Reward>();
         
         for (HashMap map : list)
         {
             Reward reward = Reward.rewardFromYaml(map);
             if (reward != null)
             {
                 rewards.add(reward);
             }
         }
     }
     
     @Override
     public void give(Player player)
     {
         for (Reward reward : rewards)
         {
             reward.give(player);
         }
     }

 }
