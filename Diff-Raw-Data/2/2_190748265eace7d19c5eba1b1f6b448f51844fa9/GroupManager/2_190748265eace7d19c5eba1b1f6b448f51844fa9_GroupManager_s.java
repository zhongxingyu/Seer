 /**
  * This file, GroupManager.java, is part of MineQuest:
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
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Level;
 
 import org.bukkit.Bukkit;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerEvent;
 import org.bukkit.event.player.PlayerKickEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 
 import com.theminequest.MineQuest.ManagerException;
 import com.theminequest.MineQuest.ManagerException.ManagerReason;
 import com.theminequest.MineQuest.MineQuest;
 import com.theminequest.MineQuest.BukkitEvents.QuestCompleteEvent;
 import com.theminequest.MineQuest.BukkitEvents.TeamInviteEvent;
 import com.theminequest.MineQuest.Group.GroupException.GroupReason;
 import com.theminequest.MineQuest.Player.PlayerDetails;
 import com.theminequest.MineQuest.Quest.Quest;
 
 public class GroupManager implements Listener{
 
 	protected final int TEAM_MAX_CAPACITY;
 	protected final int SUPER_MAX_CAPACITY;
 	private Map<Long, Group> groups;
 	private Map<Player, Group> invitations;
 	private long groupid;
 
 	public GroupManager(){
 		MineQuest.log("[Team] Starting Manager...");
 		groups = Collections.synchronizedMap(new LinkedHashMap<Long,Group>());
 		invitations = Collections.synchronizedMap(new LinkedHashMap<Player,Group>());
 		groupid = 0;
 		TEAM_MAX_CAPACITY = MineQuest.configuration.groupConfig.getInt("team_max_capacity", 8);
 		SUPER_MAX_CAPACITY = MineQuest.configuration.groupConfig.getInt("super_max_capacity", 3);
 	}
 
 	public synchronized long createTeam(ArrayList<Player> p){
 		long id = groupid;
 		groupid++;
 		groups.put(id, new Team(groupid,p));
 		//for (Player player : p){
 		//	MineQuest.playerManager.getPlayerDetails(player).setTeam(id);
 		//}
 		return id;
 	}
 
 	public synchronized long createTeam(Player p){
 		ArrayList<Player> group = new ArrayList<Player>();
 		group.add(p);
 		return createTeam(group);
 	}
 	
 	public synchronized long createSuperTeam(ArrayList<Player> p){
 		// to implement
 		throw new RuntimeException(new ManagerException(ManagerReason.NOTIMPLEMENTED));
 	}
 	
 	public synchronized long createSuperTeam(Player p){
 		// to implement
 		throw new RuntimeException(new ManagerException(ManagerReason.NOTIMPLEMENTED));
 	}
 
 	public synchronized Group getGroup(long id){
 		return groups.get(id);
 	}
 
 	/**
 	 * Determine if a player is on a team.
 	 * @param p Player to check for.
 	 * @return Team ID, or -1 if not on team.
 	 */
 	public synchronized long indexOf(Player p){
 		for (long id : groups.keySet()){
 			Group t = groups.get(id);
 			if (t!=null && t.contains(p))
 				return id;
 		}
 		return -1;
 	}
 	
 	/**
 	 * Determine if a quest is being played by a team
 	 * @param q Quest
 	 * @return Team ID, or -1 if not on a team.
 	 */
 	public synchronized long indexOfQuest(Quest q){
 		for (long id : groups.keySet()){
 			Group t = groups.get(id);
 			if (t!=null && t.getQuest()!=null && t.getQuest().equals(q))
 				return id;
 		}
 		return -1;
 	}
 	
 	public synchronized void acceptPendingInvite(Player p) throws ManagerException, GroupException{
 		if (!invitations.containsKey(p))
 			throw new ManagerException(ManagerReason.INVALIDARGS);
 		invitations.get(p).add(p);
 		invitations.remove(p);
 	}
 	
 	public synchronized boolean hasInvite(Player p) {
 		return (invitations.containsKey(p));
 	}
 	
 	protected synchronized void invitePlayer(final Player p, Group g) throws GroupException {
 		if (invitations.containsKey(p))
 			throw new GroupException(GroupReason.ALREADYINTEAM);
 		invitations.put(p, g);
 		// TODO Call TeamInviteEvent (remember, 30 seconds to accept invite)
 		TeamInviteEvent event = new TeamInviteEvent(g.getLeader().getName(), p, g.getID());
 		Bukkit.getPluginManager().callEvent(event);
 		Bukkit.getScheduler().scheduleAsyncDelayedTask(MineQuest.activePlugin, new Runnable(){
 
 			@Override
 			public void run() {
 				disposeInvite(p);
 			}
 			
 		}, 600);
 	}
 	
 	private synchronized void disposeInvite(Player p) {
 		if (!invitations.containsKey(p)) // accepted; just return.
 			return;
 		invitations.remove(p);
 		// TODO Call TeamInviteExpiredEvent
 		p.sendMessage("Invite expired!"); // FIXME
 	}
 
 	/*
 	 * Only called by Team objects when everyone leaves the team.
 	 */
 	protected synchronized void removeEmptyTeam(long id){
 		groups.get(id).lockGroup();
 		groups.put(id, null);
 	}
 
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public synchronized void onPlayerQuit(PlayerQuitEvent e){
 		processEvent(e);
 	}
 
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public synchronized void onPlayerKick(PlayerKickEvent e){
 		processEvent(e);
 	}
 	
 	private synchronized void processEvent(PlayerEvent e){
 		//PlayerDetails p = MineQuest.playerManager.getPlayerDetails(e.getPlayer());
 		//if (p.getTeam()!=-1){
 		//	MineQuest.groupManager.getTeam(p.getTeam()).remove(e.getPlayer());
 		//}
 		long team = indexOf(e.getPlayer());
 		if (team!=-1){
 			try {
 				groups.get(team).remove(e.getPlayer());
 			} catch (GroupException e1) {
 				MineQuest.log(Level.SEVERE, "Failed to remove player from team: " + e1);
 				MineQuest.log(Level.WARNING, "Locking group and kicking all players...");
 				for (Player p : groups.get(team).getPlayers()){
 					p.sendMessage("[ERROR] Something went wrong and we have to disband your group. :(");
 					try {
 						groups.get(team).remove(p);
 					} catch (GroupException e2) {
 						// ignore
 					}
 					removeEmptyTeam(team);
 				}
 				
 			} catch (NullPointerException e1) {
 				// ...
 			}
 		}
 	}
 	
 	@EventHandler
 	public synchronized void onQuestCompleteEvent(QuestCompleteEvent e){
 		Quest q = MineQuest.questManager.getQuest(e.getQuestId());
 		if (!q.isInstanced()){
 			try {
 				e.getGroup().finishQuest();
 			} catch (GroupException e1) {
 				// TODO Auto-generated catch block
 				e1.printStackTrace();
 			}
 		}
 	}
 
 }
