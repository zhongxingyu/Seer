 /**
  * This file, QuestManager.java, is part of MineQuest:
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
 package com.theminequest.MineQuest.Quest;
 
 import java.io.File;
 import java.io.FileNotFoundException;
import java.io.FilenameFilter;
 import java.io.IOException;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.logging.Level;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockDamageEvent;
 import org.bukkit.event.block.BlockPlaceEvent;
 import org.bukkit.event.player.PlayerRespawnEvent;
 
 import com.theminequest.MineQuest.MineQuest;
 import com.theminequest.MineQuest.BukkitEvents.CompleteStatus;
 import com.theminequest.MineQuest.BukkitEvents.QuestAvailableEvent;
 import com.theminequest.MineQuest.BukkitEvents.QuestCompleteEvent;
 import com.theminequest.MineQuest.BukkitEvents.QuestStartedEvent;
 import com.theminequest.MineQuest.BukkitEvents.TaskCompleteEvent;
 import com.theminequest.MineQuest.Editable.Edit;
 import com.theminequest.MineQuest.Group.Group;
 import com.theminequest.MineQuest.Group.Team;
 import com.theminequest.MineQuest.Quest.Parser.AcceptTextHandler;
 import com.theminequest.MineQuest.Quest.Parser.AreaPreserveHandler;
 import com.theminequest.MineQuest.Quest.Parser.CancelTextHandler;
 import com.theminequest.MineQuest.Quest.Parser.DescriptionHandler;
 import com.theminequest.MineQuest.Quest.Parser.EditHandler;
 import com.theminequest.MineQuest.Quest.Parser.EditMessageHandler;
 import com.theminequest.MineQuest.Quest.Parser.FinishTextHandler;
 import com.theminequest.MineQuest.Quest.Parser.GroupLimitHandler;
 import com.theminequest.MineQuest.Quest.Parser.InstanceHandler;
 import com.theminequest.MineQuest.Quest.Parser.LoadWorldHandler;
 import com.theminequest.MineQuest.Quest.Parser.NameHandler;
 import com.theminequest.MineQuest.Quest.Parser.RepeatableHandler;
 import com.theminequest.MineQuest.Quest.Parser.ResetHandler;
 import com.theminequest.MineQuest.Quest.Parser.SpawnHandler;
 import com.theminequest.MineQuest.Quest.Parser.TargetHandler;
 import com.theminequest.MineQuest.Quest.Parser.TaskHandler;
 import com.theminequest.MineQuest.Quest.Parser.WorldHandler;
 
 
 public class QuestManager implements Listener {
 
 	protected final String locationofQuests;
 	private LinkedHashMap<Long,Quest> quests;
 	private List<QuestDescription> descriptions;
 	private long questid;
 	public final QuestParser parser;
 
 	public QuestManager(){
 		MineQuest.log("[Quest] Starting Manager...");
 		quests = new LinkedHashMap<Long,Quest>();
 		questid = 0;
 		locationofQuests = MineQuest.configuration.questConfig
 				.getString("questfolderlocation",
 						MineQuest.activePlugin.getDataFolder().getAbsolutePath()
 						+File.separator+"quests");
 		File f = new File(locationofQuests);
 		if (!f.exists() || !f.isDirectory()){
 			f.delete();
 			f.mkdirs();
 		}
 		parser = new QuestParser();
 		parser.addClassHandler("accepttext", AcceptTextHandler.class);
 		parser.addClassHandler("areapreserve", AreaPreserveHandler.class);
 		parser.addClassHandler("canceltext", CancelTextHandler.class);
 		parser.addClassHandler("description", DescriptionHandler.class);
 		parser.addClassHandler("edit", EditHandler.class);
 		parser.addClassHandler("editmessage", EditMessageHandler.class);
 		parser.addClassHandler("event", com.theminequest.MineQuest.Quest.Parser.EventHandler.class);
 		parser.addClassHandler("finishtext", FinishTextHandler.class);
 		parser.addClassHandler("grouplimit", GroupLimitHandler.class);
 		parser.addClassHandler("instance", InstanceHandler.class);
 		parser.addClassHandler("loadworld", LoadWorldHandler.class);
 		parser.addClassHandler("name", NameHandler.class);
 		parser.addClassHandler("repeatable", RepeatableHandler.class);
 		parser.addClassHandler("reset", ResetHandler.class);
 		parser.addClassHandler("spawn", SpawnHandler.class);
 		parser.addClassHandler("target", TargetHandler.class);
 		parser.addClassHandler("task", TaskHandler.class);
 		parser.addClassHandler("world", WorldHandler.class);
 	}
 	
 	public void reloadQuests(){
 		MineQuest.log("[Quest] Reload Triggered. Starting reload...");
 		descriptions = new ArrayList<QuestDescription>();
 		File file = new File(locationofQuests);
		for (File f : file.listFiles(new FilenameFilter(){

			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".quest");
			}
			
		})){
 			try {
 				QuestDescription d = new QuestDescription(f);
 				descriptions.add(d);
 				MineQuest.log("[Quest] Loaded " + d.questname+".");
 			} catch (FileNotFoundException e) {
 				e.printStackTrace();
				MineQuest.log(Level.SEVERE, "[Quest] Failed to load "+f.getName()+"!");
 			}
 		}
 	}
 	
 	public QuestDescription getQuest(String name){
 		for (QuestDescription d : descriptions)
 			if (d.questname.equals(name))
 				return d;
 		return null;
 	}
 
 	public long startQuest(String id){
 		QuestDescription d = getQuest(id);
 		if (d==null)
 			throw new IllegalArgumentException(new NullPointerException(id));
 		try {
 			d = d.getCopy();
 		} catch (IOException e) {
 			throw new RuntimeException(e);
 		}
 		quests.put(questid,new Quest(questid,d));
 		if (!quests.get(questid).details.loadworld)
 			quests.get(questid).startQuest();
 		long thisquestid = questid;
 		questid++;
 		return thisquestid;
 	}
 
 	public Quest getQuest(long currentquest) {
 		if (quests.containsKey(currentquest))
 			return quests.get(currentquest);
 		return null;
 	}
 
 	@EventHandler
 	public void taskCompletion(TaskCompleteEvent e){
 		getQuest(e.getQuestID()).onTaskCompletion(e);
 	}
 
 	@EventHandler
 	public void onQuestCompletion(QuestCompleteEvent e){
 		if (e.getResult()!=CompleteStatus.CANCELED){
 			Quest q = quests.get(e.getQuestId());
 			String questname = q.details.questname;
 			for (Player p : e.getGroup().getPlayers()){
 				p.sendMessage(ChatColor.GREEN + q.details.displayfinish);
 				MineQuest.sqlstorage.querySQL("Quests/completeQuest", p.getName(), questname);
 			}
 		}
 		//quests.put(e.getQuestId(), null);
 	}
 	
 	@EventHandler
 	public void onBlockPlaceEvent(BlockPlaceEvent e){
 		Player p = e.getPlayer();
 		if (MineQuest.groupManager.indexOf(p)==-1)
 			return;
 		Group g = MineQuest.groupManager.getGroup(MineQuest.groupManager.indexOf(p));
 		if (g.isInQuest()){
 			Quest q = g.getQuest();
 			// by default, I don't allow this to happen.
 			e.setCancelled(true);
 			for (Edit edit : q.details.editables.values()){
 				edit.onBlockPlace(e);
 				if (!e.isCancelled())
 					return;
 			}
 			e.getPlayer().sendMessage(ChatColor.YELLOW+"[!] " + q.getEditMessage());
 		}
 	}
 	
 	@EventHandler
 	public void onBlockDamageEvent(BlockDamageEvent e){
 		Player p = e.getPlayer();
 		if (MineQuest.groupManager.indexOf(p)==-1)
 			return;
 		Group g = MineQuest.groupManager.getGroup(MineQuest.groupManager.indexOf(p));
 		if (g.isInQuest()){
 			Quest q = g.getQuest();
 			// by default, I don't allow this to happen.
 			e.setCancelled(true);
 			for (Edit edit : q.details.editables.values()){
 				edit.onBlockDamage(e);
 				if (!e.isCancelled())
 					return;
 			}
 			e.getPlayer().sendMessage(ChatColor.YELLOW+"[!] " + q.getEditMessage());
 		}
 	}
 	
 	@EventHandler
 	public void onPlayerRespawnEvent(PlayerRespawnEvent e){
 		Player p = e.getPlayer();
 		if (MineQuest.groupManager.indexOf(p)==-1)
 			return;
 		Group g = MineQuest.groupManager.getGroup(MineQuest.groupManager.indexOf(p));
 		if (g.isInQuest())
 			e.setRespawnLocation(g.getQuest().getSpawnLocation());
 	}
 	
 	@EventHandler
 	public void onQuestAvailableEvent(QuestAvailableEvent e){
 		e.getPlayer().sendMessage(ChatColor.YELLOW + "[Quest] You have a new quest, " + e.getQuestAvailableName() + ", available!");
 	}
 
 }
