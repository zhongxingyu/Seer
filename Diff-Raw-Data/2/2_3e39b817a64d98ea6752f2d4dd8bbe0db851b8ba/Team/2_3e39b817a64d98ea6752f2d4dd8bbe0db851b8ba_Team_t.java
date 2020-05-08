 /**
  * This file, Team.java, is part of MineQuest:
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
 package com.theminequest.MineQuest.Group;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.LinkedHashMap;
 import java.util.List;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.entity.Player;
 
 import com.theminequest.MineQuest.ManagerException;
 import com.theminequest.MineQuest.MineQuest;
 import com.theminequest.MineQuest.BukkitEvents.CompleteStatus;
 import com.theminequest.MineQuest.BukkitEvents.QuestStartedEvent;
 import com.theminequest.MineQuest.Group.GroupException.GroupReason;
 import com.theminequest.MineQuest.Quest.Quest;
 
 public class Team implements Group {
 
 	private long teamid;
 	private List<Player> players;
 	private LinkedHashMap<Player,Location> locations;
 	private int capacity;
 	private Quest quest;
 	private boolean inQuest;
 
 	protected Team(long id, ArrayList<Player> p){
 		if (p.size()<=0 || p.size()>MineQuest.groupManager.TEAM_MAX_CAPACITY)
 			throw new IllegalArgumentException(GroupReason.BADCAPACITY.name());
 		// ^ never should encounter this unless a third-party tries to, in which
 		// case they get what they deserve.
 		teamid = id;
 		players = Collections.synchronizedList(p);
 		locations = null;
 		quest = null;
 		inQuest = false;
 		capacity = MineQuest.groupManager.TEAM_MAX_CAPACITY;
 	}
 
 	@Override
 	public synchronized Player getLeader(){
 		return players.get(0);
 	}
 
 	@Override
 	public synchronized void setLeader(Player p) throws GroupException{
 		if (!contains(p))
 			throw new GroupException(GroupReason.NOTONTEAM);
 		players.remove(p);
 		players.add(0, p);
 	}
 
 	@Override
 	public synchronized List<Player> getPlayers(){
 		return players;
 	}
 	
 	/*
 	 * Mark team in a way such that nobody can get on the team anymore.
 	 * This should help Java trigger GC on this object.
 	 */
 	@Override
 	public void lockGroup(){
 		capacity = 0;
 	}
 
 	@Override
 	public synchronized void setCapacity(int c) throws GroupException{
 		if (c<=0 || c>players.size())
 			throw new GroupException(GroupReason.BADCAPACITY);
 		capacity = c;
 	}
 
 	@Override
 	public synchronized int getCapacity(){
 		return capacity;
 	}
 
 	@Override
 	public long getID(){
 		return teamid;
 	}
 
 	@Override
 	public synchronized boolean contains(Player p){
 		return players.contains(p);
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * @see com.theminequest.MineQuest.Group.Group#startQuest(com.theminequest.MineQuest.Quest.Quest)
 	 * let the backend handle checking valid quests, calling QuestManager, etc...
 	 */
 	@Override
 	public synchronized void startQuest(String q) throws GroupException {
 		if (quest!=null)
 			throw new GroupException(GroupReason.ALREADYONQUEST);
 		long id = MineQuest.questManager.startQuest(q);
 		quest = MineQuest.questManager.getQuest(id);
 		QuestStartedEvent event = new QuestStartedEvent(quest);
 		Bukkit.getPluginManager().callEvent(event);
 	}
 
 	@Override
 	public synchronized void abandonQuest() throws GroupException {
 		if (quest==null)
 			throw new GroupException(GroupReason.NOQUEST);
 		quest.finishQuest(CompleteStatus.CANCELED);
 		if (inQuest)
 			exitQuest();
		else if (quest!=null && quest.isInstanced()){
 			try {
 				quest.unloadQuest();
 			} catch (IOException e) {
 				throw new GroupException(e);
 			}
 		}
 		quest = null;
 	}
 	
 	/**
 	 * Get the quest the team is undertaking.
 	 * @return Quest the team is undertaking, or <code>null</code> if the team
 	 * is not on a quest.
 	 */
 	@Override
 	public synchronized Quest getQuest() {
 		return quest;
 	}
 
 	@Override
 	public synchronized void teleportPlayers(Location l) {
 		for (Player p : players){
 			p.teleport(l);
 		}
 	}
 
 	@Override
 	public synchronized void add(Player p) throws GroupException {
 		if (MineQuest.groupManager.indexOf(p)!=-1)
 			throw new GroupException(GroupReason.ALREADYINTEAM);
 		if (players.size()>=capacity)
 			throw new GroupException(GroupReason.OVERCAPACITY);
 		if (contains(p))
 			throw new GroupException(GroupReason.ALREADYINTEAM);
 		if (inQuest)
 			throw new GroupException(GroupReason.INSIDEQUEST);
 		//MineQuest.playerManager.getPlayerDetails(p).setTeam(teamid);
 		players.add(p);
 		// TODO add TeamPlayerJoinedEvent
 	}
 
 	@Override
 	public synchronized void remove(Player p) throws GroupException{
 		if (MineQuest.groupManager.indexOf(p)==-1)
 			throw new GroupException(GroupReason.NOTONTEAM);
 		if (!contains(p))
 			throw new GroupException(GroupReason.NOTONTEAM);
 		//MineQuest.playerManager.getPlayerDetails(p).setTeam(-1);
 		players.remove(p);
 		if (locations!=null){
 			moveBackToLocations(p);
 			locations.remove(p);
 		}
 		
 		if (players.size()<=0){
 			if (quest!=null){
 				abandonQuest();
 			}
 			MineQuest.groupManager.removeEmptyTeam(teamid);
 		}
 		// TODO add TeamPlayerQuitEvent
 	}
 
 	@Override
 	public synchronized void enterQuest() throws GroupException {
 		if (quest==null)
 			throw new GroupException(GroupReason.NOQUEST);
 		if (inQuest)
 			throw new GroupException(GroupReason.INSIDEQUEST);
 		if (!quest.isInstanced())
 			throw new GroupException(GroupReason.MAINWORLDQUEST);
 		recordCurrentLocations();
 		inQuest = true;
 		teleportPlayers(quest.getSpawnLocation());
 		quest.startQuest();
 	}
 
 	@Override
 	public synchronized void recordCurrentLocations() {
 		locations = new LinkedHashMap<Player,Location>();
 		for (Player p : players){
 			locations.put(p, p.getLocation());
 		}
 	}
 	
 	@Override
 	public synchronized void moveBackToLocations() throws GroupException{
 		for (Player p : players){
 			moveBackToLocations(p);
 		}
 		locations = null;
 	}
 	
 	@Override
 	public synchronized void moveBackToLocations(Player p) throws GroupException {
 		if (locations==null)
 			throw new GroupException(GroupReason.NOLOCATIONS);
 		p.teleport(locations.get(p));
 		locations.remove(p);
 	}
 
 	@Override
 	public synchronized void exitQuest() throws GroupException {
 		if (quest==null)
 			throw new GroupException(GroupReason.NOQUEST);
 		if (!inQuest)
 			throw new GroupException(GroupReason.NOTINSIDEQUEST);
 		if (!quest.isInstanced())
 			throw new GroupException(GroupReason.MAINWORLDQUEST);
 		if (quest.isFinished()==null)
 			throw new GroupException(GroupReason.UNFINISHEDQUEST);
 		moveBackToLocations();
 		inQuest = false;
 		try {
 			quest.unloadQuest();
 		} catch (IOException e) {
 			throw new GroupException(GroupReason.EXTERNALEXCEPTION,e);
 		}
 		quest = null;
 	}
 	
 	@Override
 	public synchronized void finishQuest() throws GroupException {
 		if (quest==null)
 			throw new GroupException(GroupReason.NOQUEST);
 		if (quest.isInstanced())
 			throw new GroupException(GroupReason.NOTMAINWORLDQUEST);
 		if (quest.isFinished()==null)
 			throw new GroupException(GroupReason.UNFINISHEDQUEST);
 		quest = null;
 	}
 
 	@Override
 	public synchronized boolean isInQuest() {
 		return inQuest;
 	}
 
 	// remember that only the leader should be able to invite
 	@Override
 	public void invite(Player p) throws GroupException {
 		MineQuest.groupManager.invitePlayer(p, this);
 	}
 
 	@Override
 	public int compareTo(Group arg0) {
 		return (int) (teamid-arg0.getID());
 	}
 
 }
