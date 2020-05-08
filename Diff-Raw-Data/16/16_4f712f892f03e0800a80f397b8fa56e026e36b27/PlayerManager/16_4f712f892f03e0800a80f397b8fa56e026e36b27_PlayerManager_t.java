 /**
  * This file, PlayerManager.java, is part of MineQuest:
  * A full featured and customizable quest/mission system.
  * Copyright (C) 2012 The MineQuest Team
  * 
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
  **/
 package com.theminequest.MineQuest.Player;
 
 import java.sql.SQLException;
 import java.util.LinkedHashMap;
 
 import org.bukkit.Bukkit;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerKickEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 
 import com.theminequest.MineQuest.MineQuest;
 import com.theminequest.MineQuest.BukkitEvents.QuestAvailableEvent;
 import com.theminequest.MineQuest.BukkitEvents.QuestCompleteEvent;
 import com.theminequest.MineQuest.BukkitEvents.TeamInviteEvent;
 
 public class PlayerManager implements Listener {
 
 	public static final int BASE_EXP = 100;
 	public static final int BASE_MANA = 100;
 
 	private LinkedHashMap<Player,PlayerDetails> players;
 
 	public PlayerManager(){
 		MineQuest.log("[Player] Starting Manager...");
 		players = new LinkedHashMap<Player,PlayerDetails>();
 		// mana +1 every 5 seconds.
 		Bukkit.getScheduler().scheduleSyncRepeatingTask(MineQuest.activePlugin, new Runnable(){
 
 			@Override
 			public void run() {
 				for (Player p : Bukkit.getOnlinePlayers()){
 					MineQuest.playerManager.getPlayerDetails(p).modifyManaBy(1);
 				}
 			}
 
 		}, 300, 100);
 
 		Bukkit.getScheduler().scheduleSyncRepeatingTask(MineQuest.activePlugin, new Runnable(){
 
 			@Override
 			public void run() {
 				saveAll();
 				MineQuest.log("[Player] Routine Record Save Finished.");
 			}
 
 		}, 1200, 18000);
 	}
 
 	public void saveAll(){
 		for (PlayerDetails d : players.values())
 			d.save();
 	}
 
 	private void playerAcct(Player p){
 		if (!players.containsKey(p)){
 			try {
 				players.put(p,new PlayerDetails(p));
 			} catch (SQLException e) {
 				throw new RuntimeException(e);
 			}
 		} else {
 			players.get(p).reload();
 		}
 	}
 
 	public PlayerDetails getPlayerDetails(Player p){
 		playerAcct(p);
 		return players.get(p);
 	}
 
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onPlayerJoin(PlayerJoinEvent e){
 		MineQuest.log("[Player] Retrieving details for player " + e.getPlayer().getName());
 		playerAcct(e.getPlayer());
 	}
 
 	@EventHandler(priority = EventPriority.LOWEST)
 	public void onPlayerQuit(PlayerQuitEvent e){
 		MineQuest.log("[Player] Saving details for player " + e.getPlayer().getName());
 		getPlayerDetails(e.getPlayer()).save();
 	}
 
 	@EventHandler(priority = EventPriority.LOWEST)
 	public void onPlayerKick(PlayerKickEvent e){
 		MineQuest.log("[Player] Saving details for player " + e.getPlayer().getName());
 		getPlayerDetails(e.getPlayer()).save();
 	}
 	
 	@EventHandler
 	public void onQuestAvailableEvent(QuestAvailableEvent e){
 		MineQuest.log("[Player] New Quest Available for player " + e.getPlayer().getName());
		e.getPlayer().sendMessage("[Quest] You have a new quest, " + e.getQuestAvailableName() + ", available!");
 	}
 	
 	@EventHandler
 	public void onQuestCompleteEvent(QuestCompleteEvent e){
 		// TODO
 	}
 	
 	@EventHandler
 	public void onTeamInviteEvent(TeamInviteEvent e){
 		getPlayerDetails(e.getInvited()).invitePlayer(e.getInviterName(), e.getTeamId(), false);
 	}
 
 }
