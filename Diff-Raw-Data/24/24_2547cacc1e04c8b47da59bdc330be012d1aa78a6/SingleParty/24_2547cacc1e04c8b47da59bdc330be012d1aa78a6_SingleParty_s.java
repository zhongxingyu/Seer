 /*
  * This file is part of MineQuest, The ultimate MMORPG plugin!.
  * MineQuest is licensed under GNU General Public License v3.
  * Copyright (C) 2012 The MineQuest Team
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
  */
 package com.theminequest.bukkit.group;
 
 import java.util.LinkedList;
 import java.util.List;
 
 import com.theminequest.api.group.Group;
 import com.theminequest.api.group.GroupException;
 import com.theminequest.api.group.GroupException.GroupReason;
 import com.theminequest.api.platform.MQLocation;
 import com.theminequest.api.platform.MQPlayer;
 import com.theminequest.api.quest.Quest;
 import com.theminequest.api.quest.QuestDetails;
 import com.theminequest.api.statistic.QuestStatisticUtils;
 import com.theminequest.api.statistic.QuestStatisticUtils.QSException;
 
 public class SingleParty implements Group {
 	
 	private MQPlayer player;
 	private Quest activeQuest;
 	
 	public SingleParty(MQPlayer mqPlayer, Quest activeQuest) {
 		this.player = mqPlayer;
 		this.activeQuest = activeQuest;
 	}
 	
 	@Override
 	public long getID() {
 		return -1;
 	}
 	
 	@Override
 	public MQPlayer getLeader() {
 		return player;
 	}
 	
 	@Override
 	public void setLeader(MQPlayer p) throws GroupException {
 		throw new GroupException(GroupReason.MAINWORLDQUEST);
 	}
 	
 	@Override
 	public List<MQPlayer> getMembers() {
 		LinkedList<MQPlayer> tr = new LinkedList<MQPlayer>();
 		tr.addFirst(player);
 		return tr;
 	}
 	
 	@Override
 	public void add(MQPlayer p) throws GroupException {
 		throw new GroupException(GroupReason.MAINWORLDQUEST);
 	}
 	
 	@Override
 	public void remove(MQPlayer p) throws GroupException {
 		throw new GroupException(GroupReason.MAINWORLDQUEST);
 	}
 	
 	@Override
 	public boolean contains(MQPlayer p) {
 		if (p.equals(player))
 			return true;
 		return false;
 	}
 	
 	@Override
 	public int getCapacity() {
 		return 1;
 	}
 	
 	@Override
 	public void setCapacity(int capacity) throws GroupException {
 		throw new GroupException(GroupReason.MAINWORLDQUEST);
 	}
 	
 	@Override
 	public boolean isPVP() {
 		return false;
 	}
 	
 	@Override
 	public void setPVP(boolean on) {
 		// do nothing
 	}
 	
 	@Override
 	public void teleportPlayers(MQLocation l) {
 		player.teleport(l);
 	}
 	
 	@Override
 	public int compareTo(Group arg0) {
 		return player.getName().compareTo(arg0.getLeader().getName());
 	}
 	
 	@Override
 	public Quest getQuest() {
 		return activeQuest;
 	}
 	
 	@Override
 	public QuestStatus getQuestStatus() {
 		return QuestStatus.MAINWORLDQUEST;
 	}
 	
 	@Override
 	public void startQuest(QuestDetails d) throws GroupException {
 		throw new GroupException(GroupReason.MAINWORLDQUEST);
 	}
 	
 	@Override
 	public void abandonQuest() throws GroupException {
 		throw new GroupException(GroupReason.MAINWORLDQUEST);
 	}
 	
 	@Override
 	public void enterQuest() throws GroupException {
 		throw new GroupException(GroupReason.MAINWORLDQUEST);
 	}
 	
 	@Override
 	public void exitQuest() throws GroupException {
 		throw new GroupException(GroupReason.MAINWORLDQUEST);
 	}
 	
 	@Override
 	public void finishQuest() throws GroupException {
 		try {
			QuestStatisticUtils.completeQuest(player.getName(), (String) activeQuest.getDetails().getProperty(QuestDetails.QUEST_NAME));
 		} catch (QSException e) {
 			throw new GroupException(e);
 		}
 	}
 	
 }
