 /**
  * This file, PlayerDetails.java, is part of MineQuest:
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
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.LinkedHashMap;
 import java.util.List;
 
 import org.bukkit.Bukkit;
 import org.bukkit.entity.Player;
 import com.theminequest.MineQuest.MineQuest;
 import com.theminequest.MineQuest.Quest.Quest;
 import com.theminequest.MineQuest.Utils.PropertiesFile;
 import com.theminequest.MineQuest.AbilityAPI.Ability;
 import com.theminequest.MineQuest.Backend.TeamBackend;
 import com.theminequest.MineQuest.BukkitEvents.PlayerExperienceEvent;
 import com.theminequest.MineQuest.BukkitEvents.PlayerLevelEvent;
 import com.theminequest.MineQuest.BukkitEvents.PlayerManaEvent;
 
 /**
  * Extra details about the Player
  * 
  * @author MineQuest
  * 
  */
 public class PlayerDetails {
 
 	private long quest;
 	private long team;
 	private Player player;
 	private boolean abilitiesEnabled;
 	// >_>
 	public LinkedHashMap<Ability,Long> abilitiesCoolDown;
 	// end >_>
 	private long teamwaiting;
 	private int waitingid;
 	
 	// player properties
 	private long mana;
 	private int level;
 	private long exp;
 	private int classid;
 
 	public PlayerDetails(Player p) throws SQLException {
 		reload();
 		abilitiesCoolDown = new LinkedHashMap<Ability,Long>();
 		// check for player existence in DB.
 		// if player does not, add.
 		ResultSet playerresults = MineQuest.sqlstorage.querySQL("Players/retrievePlayer", p.getName());
 		if (playerresults==null || !playerresults.first()){
 			// this means that the player does not exist; add them.
 			MineQuest.log("[Player] Player not found in SQL; creating: " + p.getName());
 			MineQuest.sqlstorage.querySQL("Players/addPlayer",p.getName());
 			level = 1;
 			exp = 0;
 		}else{
 			level = playerresults.getInt("LEVEL");
 			classid = playerresults.getInt("C_ID");
 			exp = playerresults.getLong("EXP");
 		}
 		// give the player almost full mana (3/4 full)
 		mana = (3/4)*(PlayerManager.BASE_MANA*level);
 		// and feel happeh.
 	}
 	
 	protected synchronized void reload() {
 		quest = -1;
 		team = -1;
 		teamwaiting = -1;
 		waitingid = -1;
 		abilitiesEnabled = false;
 	}
 	
 	public synchronized void invitePlayer(String string, long teamid, boolean yes){
 		if (team!=-1)
 			throw new IllegalArgumentException("Player is already on a team!");
 		if (teamwaiting!=-1)
 			throw new IllegalArgumentException("Player has an invite pending. Try again in a few seconds.");
 		if (yes){
 			team = teamid;
 			player.sendMessage("[TeamManager] Joined the team!");
 			return;
 		}
 		player.sendMessage("[TeamManager] " + string + " has sent" +
 				"you an invite to a team! To accept, type /accept. You have 30 seconds.");
 		teamwaiting = teamid;
 		waitingid = Bukkit.getScheduler().scheduleSyncDelayedTask(MineQuest.activePlugin, 
 				new Runnable(){
 					@Override
 					public void run() {
 						teamwaiting = -1;
 					}
 		}, 600);
 	}
 	
 	public synchronized void acceptInvite(){
 		Bukkit.getScheduler().cancelTask(waitingid);
 		team = teamwaiting;
 		teamwaiting = -1;
 		MineQuest.teamManager.getTeam(team).add(player);
 		player.sendMessage("[TeamManager] Joined the team!");
 	}
 	
 	public long getQuest(){
 		return quest;
 	}
 	
 	public void setQuest(long q){
 		quest = q;
 	}
 	
 	public long getTeam(){
 		return team;
 	}
 	
 	public void setTeam(long t){
 		team = t;
 	}
 	
 	public void save(){
 		MineQuest.sqlstorage.querySQL("Players/modPlayer_class",String.valueOf(classid),player.getName());
 		MineQuest.sqlstorage.querySQL("Players/modPlayer_exp",String.valueOf(level),player.getName());
 		MineQuest.sqlstorage.querySQL("Players/modPlayer_lvl",String.valueOf(exp),player.getName());
 	}
 	
 	public int getLevel(){
 		return level;
 	}
 	
 	public long getExperience(){
 		return exp;
 	}
 	
 	public int getClassId(){
 		return classid;
 	}
 	
 	public long getMana(){
 		return mana;
 	}
 	
 	/*
 	 * A user should be able to toggle ability use on/off
 	 * with a command, like /ability on/off?
 	 */
 	public boolean getAbilitiesEnabled(){
 		return abilitiesEnabled;
 	}
 	
 	public void setAbilitiesEnabled(boolean b){
 		abilitiesEnabled = b;
 	}
 	
 	public void levelUp(){
 		level+=1;
 		PlayerLevelEvent event = new PlayerLevelEvent(player);
 		Bukkit.getPluginManager().callEvent(event);
 		exp = (PlayerManager.BASE_EXP*level)-exp;
 	}
 	
 	public void modifyExperienceBy(int e){
 		exp+=e;
 		PlayerExperienceEvent event = new PlayerExperienceEvent(player, e);
 		Bukkit.getPluginManager().callEvent(event);
 		if (exp>=(PlayerManager.BASE_EXP*getLevel()))
 			levelUp();
 	}
 	
 	public void modifyManaBy(int m){
 		long manatoadd = m;
 		if (mana==PlayerManager.BASE_MANA*level)
 			return;
 		else if (m+mana>(PlayerManager.BASE_MANA*level))
 			manatoadd = (PlayerManager.BASE_MANA*level)-(m+mana);
 		mana+=manatoadd;
 		PlayerManaEvent event = new PlayerManaEvent(player,m);
 		Bukkit.getPluginManager().callEvent(event);
 		if (event.isCancelled())
 			mana-=manatoadd;
 	}
 
 }
