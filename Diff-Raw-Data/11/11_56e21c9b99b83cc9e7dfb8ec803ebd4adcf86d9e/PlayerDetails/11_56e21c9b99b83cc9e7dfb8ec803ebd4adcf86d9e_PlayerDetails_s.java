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
 package com.theminequest.MQCoreRPG.Player;
 
 import java.io.Serializable;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.LinkedHashMap;
 import java.util.List;
 
 import org.bukkit.Bukkit;
 import org.bukkit.entity.Player;
 
 import com.theminequest.MQCoreRPG.MQCoreRPG;
 import com.theminequest.MQCoreRPG.API.Abilities.Ability;
 import com.theminequest.MQCoreRPG.Class.ClassDetails;
 import com.theminequest.MineQuest.MineQuest;
 import com.theminequest.MineQuest.Quest.Quest;
 import com.theminequest.MineQuest.Utils.PropertiesFile;
 import com.theminequest.MineQuest.Backend.GroupBackend;
 import com.theminequest.MineQuest.BukkitEvents.PlayerExperienceEvent;
 import com.theminequest.MineQuest.BukkitEvents.PlayerLevelEvent;
 import com.theminequest.MineQuest.BukkitEvents.PlayerManaEvent;
 
 /**
  * Extra details about the Player
  * 
  * @author MineQuest
  * 
  */
 public class PlayerDetails implements Serializable {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -2315094916617378789L;
 
 	private boolean abilitiesEnabled;
 	// >_>
 	public LinkedHashMap<Ability,Long> abilitiesCoolDown;
 	// end >_>
 
 	protected volatile boolean giveMana;
 
 	// player properties
 	private String name;
 	private long health;
 	private long mana;
 	private int level;
 	private long exp;
 	private String classid;
 
 	public PlayerDetails(Player p) {
 		name = p.getName();
 		abilitiesEnabled = false;
 		abilitiesCoolDown = new LinkedHashMap<Ability,Long>();
 		classid = "default";
 		level = 1;
 		exp = 0;
 		health = getMaxHealth();
 		mana = Math.round(getMaxMana()*0.75);
 		giveMana = true;
 		updateMinecraftView();
 	}
 
 	public Player getPlayer(){
 		return Bukkit.getPlayerExact(name);
 	}
 
 	public synchronized int getLevel(){
 		return level;
 	}
 
 	public synchronized void setLevel(int i) {
 		level = i;
 	}
 
 	public synchronized void levelUp(){
 		level+=1;
 		PlayerLevelEvent event = new PlayerLevelEvent(getPlayer());
 		Bukkit.getPluginManager().callEvent(event);
 		exp = exp-(MQCoreRPG.classManager.getClassDetail(classid).getBaseExp()*(level-1));
 		if (exp<0)
 			exp = 0;
 	}
 
 	public synchronized long getExperience(){
 		return exp;
 	}
 
 	public synchronized long getMaxExperience(){
 		return MQCoreRPG.classManager.getClassDetail(classid).getBaseExp()*level;
 	}
 
 	public synchronized void modifyExperienceBy(int e){
 		exp+=e;
 		PlayerExperienceEvent event = new PlayerExperienceEvent(getPlayer(), e);
 		Bukkit.getPluginManager().callEvent(event);
 		while (exp>=getMaxExperience())
 			levelUp();
 	}
 
 	public synchronized String getClassID(){
 		return classid;
 	}
 
 	public synchronized void setClassID(String classid){
 		this.classid = classid;
 	}
 
 	public synchronized long getMana(){
 		return mana;
 	}
 
 	public synchronized long getMaxMana(){
 		return MQCoreRPG.classManager.getClassDetail(classid).getBaseMana()*level;
 	}
 
 	public synchronized void modifyManaBy(int m){
 		long manatoadd = m;
 		if (mana==MQCoreRPG.classManager.getClassDetail(classid).getBaseMana()*level)
 			return;
 		else if (m+mana>(getMaxMana()))
 			manatoadd = (getMaxMana())-(m+mana);
 		mana+=manatoadd;
 		PlayerManaEvent event = new PlayerManaEvent(getPlayer(),m);
 		Bukkit.getPluginManager().callEvent(event);
 		if (event.isCancelled())
 			mana-=manatoadd;
 	}
 
 	public synchronized long getHealth(){
 		return health;
 	}
 
 	public synchronized long getMaxHealth(){
 		return MQCoreRPG.classManager.getClassDetail(classid).getBaseHealth()*level;
 	}
 
 	public synchronized void setHealth(long l){
 		this.health = l;
 	}
 
 	/*
 	 * A user should be able to toggle ability use on/off
 	 * with a command, like /ability on/off?
 	 */
 	public synchronized boolean getAbilitiesEnabled(){
 		return abilitiesEnabled;
 	}
 
 	public synchronized void setAbilitiesEnabled(boolean b){
 		abilitiesEnabled = b;
 	}
 
 	public synchronized void updateMinecraftView(){
 		if (!getPlayer().isDead()){
			getPlayer().setExp(getMinecraftExp(getExperience(),getLevel()));
 			getPlayer().setFoodLevel(getMinecraftMana(getMana()));
 			getPlayer().setHealth(getMinecraftHealth(getHealth()));
 		}
 	}
 
	public synchronized float getMinecraftExp(long exp, int level){
 		// (Math.pow(1.75[Level],2) + 5.00[Level]) + (3.5[Current Level] + 6.7)
 		double curlevel = (Math.pow(1.75*((double)level),2)+(5*((double)level)));
 		double exptonext = ((3.5*((double)level)+6.7));
 		double percentageiwant = ((double)exp)/getMaxExperience();
 		double soihave = (double) (exptonext*percentageiwant);
 		return Math.round(curlevel+soihave);
 	}
 
 	public synchronized int getMinecraftMana(long mana){
 		double percentage = ((double)mana)/getMaxMana();
 		return (int) Math.round((double)20*percentage);
 	}
 
 	public synchronized int getMinecraftHealth(long health){
 		double percentage = ((double)health)/getMaxHealth();
 		return (int) Math.round((double)20*percentage);
 	}
 
 	protected static void verifyObject(PlayerDetails d){
 
 	}
 
 }
