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
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.theminequest.MineQuest.Frontend.Command;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import com.theminequest.MineQuest.I18NMessage;
 import com.theminequest.MineQuest.API.Managers;
 import com.theminequest.MineQuest.API.Group.GroupException;
 import com.theminequest.MineQuest.API.Group.QuestGroup;
 import com.theminequest.MineQuest.API.Group.QuestGroup.QuestStatus;
 import com.theminequest.MineQuest.API.Quest.Quest;
 import com.theminequest.MineQuest.API.Quest.QuestDetails;
 import com.theminequest.MineQuest.API.Quest.QuestDetailsUtils;
 import com.theminequest.MineQuest.API.Quest.QuestUtils;
 import com.theminequest.MineQuest.API.Tracker.LogStatus;
 import com.theminequest.MineQuest.API.Tracker.QuestStatisticUtils;
 import com.theminequest.MineQuest.API.Tracker.QuestStatisticUtils.QSException;
 import com.theminequest.MineQuest.API.Utils.ChatUtils;
 
 public class QuestCommandFrontend extends CommandFrontend {
 	
 	public QuestCommandFrontend(){
 		super("quest");
 	}
 	
 	public Boolean given(Player p, String[] args) {
 		if (args.length!=0){
 			p.sendMessage(I18NMessage.Cmd_INVALIDARGS.getValue());
 			return false;
 		}
 		Map<String, Date> quests = QuestStatisticUtils.getQuests(p.getName(),LogStatus.GIVEN);
 		
 		List<String> message = new ArrayList<String>();
 		message.add(ChatUtils.formatHeader(I18NMessage.Cmd_Quest_GIVEN.getValue()));
 		for (String q : quests.keySet()){
 			if (q.isEmpty())
 				continue;
 			QuestDetails qd = Managers.getQuestManager().getDetails(q);
 			if (qd!=null){
 				message.add(ChatColor.AQUA + q + " : " + ChatColor.GOLD + qd.getProperty(QuestDetails.QUEST_DISPLAYNAME));
 			} else {
 				message.add(ChatColor.GRAY + q + " : <?>");
 			}
 		}
 		
		message.add(ChatUtils.formatHeader(I18NMessage.Cmd_Quest_GIVEN_INFO.getValue()));
 		
 		for (String m : message)
 			p.sendMessage(m);
		if (message.size()==1)
			p.sendMessage(I18NMessage.Cmd_NOQUESTS.getValue());
 		return true;
 	}
 	
 	public Boolean main(Player p, String[] args) {
 		if (args.length>1){
 			p.sendMessage(I18NMessage.Cmd_INVALIDARGS.getValue());
 			return false;
 		}
 		if (args.length==0){
 			Map<String, Date> quests = QuestStatisticUtils.getQuests(p.getName(), LogStatus.ACTIVE);
 			List<String> message = new ArrayList<String>();
 			message.add(ChatUtils.formatHeader(I18NMessage.Cmd_Quest_MAIN.getValue()));
 			for (String q : quests.keySet()){
 				if (q.isEmpty())
 					continue;
 				Quest quest = Managers.getQuestManager().getMainWorldQuest(p.getName(), q);
 				message.add(ChatColor.LIGHT_PURPLE + q + " : " + ChatColor.GOLD + quest.getDetails().getProperty(QuestDetails.QUEST_NAME));
 			}
 			
			message.add(ChatUtils.formatHeader(I18NMessage.Cmd_Quest_MAIN_INFO.getValue()));
 			
 			for (String m : message)
 				p.sendMessage(m);
			if (message.size()==1)
				p.sendMessage(I18NMessage.Cmd_NOQUESTS.getValue());
 			return true;
 		} else {
 			String name = args[0];
 			Quest q = Managers.getQuestManager().getMainWorldQuest(p.getName(),name);
 			if (q != null) {
 				p.sendMessage(QuestUtils.getStatusString(q).split(QuestDetailsUtils.CODE_NEWLINE_SEQ));
 				return true;
 			} else {
 				p.sendMessage(I18NMessage.Cmd_NOSUCHQUEST.getValue());
 				return true;
 			}
 		}
 	}
 	
 	public Boolean drop(Player p, String[] args) {
 		if (args.length!=1){
 			p.sendMessage(I18NMessage.Cmd_INVALIDARGS.getValue());
 			return false;
 		}
 		try {
 			QuestStatisticUtils.dropQuest(p.getName(), args[0]);
 			QuestDetails d = Managers.getQuestManager().getDetails(args[0]);
 			if (d==null || d.getProperty(QuestDetails.QUEST_ABORT)==null)
 				p.sendMessage(I18NMessage.Cmd_Quest_DROP + "");
 			else
 				p.sendMessage(ChatUtils.chatify((String) d.getProperty(QuestDetails.QUEST_ABORT)));
 		} catch (QSException e) {
 			p.sendMessage(I18NMessage.Cmd_Quest_NOTHAVEQUEST.getValue());
 			return false;
 		}
 		return true;
 	}
 	
 	public Boolean abandon(Player p, String[] args) {
 		if (args.length!=0){
 			p.sendMessage(I18NMessage.Cmd_INVALIDARGS.getValue());
 			return false;
 		}
 		if (Managers.getQuestGroupManager().indexOf(p)==-1){
 			p.sendMessage(ChatColor.RED + I18NMessage.Cmd_NOPARTY.getValue());
 			return false;
 		}
 		QuestGroup g = Managers.getQuestGroupManager().get(p);
 		if (!g.getLeader().equals(p)){
 			p.sendMessage(ChatColor.RED + I18NMessage.Cmd_NOTLEADER.getValue());
 			return false;
 		}
 		if (g.getQuest()==null){
 			p.sendMessage(ChatColor.RED + I18NMessage.Cmd_Quest_NOACTIVE.getValue());
 			return false;
 		}
 		if (g.getQuest().isFinished()!=null){
 			p.sendMessage(ChatColor.RED + I18NMessage.Cmd_Quest_ALREADYDONE.getValue());
 			return false;
 		}
 		try {
 			g.abandonQuest();
 			return true;
 		} catch (GroupException e) {
 			e.printStackTrace();
 			p.sendMessage(ChatColor.GRAY + "ERR: " + e.getMessage());
 			return false;
 		}
 	}
 	
 	public Boolean active(Player p, String[] args) {
 		if (args.length!=0){
 			p.sendMessage(I18NMessage.Cmd_INVALIDARGS.getValue());
 			return false;
 		}
 		if (Managers.getQuestGroupManager().indexOf(p)==-1){
 			p.sendMessage(ChatColor.RED + I18NMessage.Cmd_NOPARTY.getValue());
 			return false;
 		}
 		QuestGroup g = Managers.getQuestGroupManager().get(p);
 		if (g.getQuest()==null){
 			p.sendMessage(ChatColor.RED + I18NMessage.Cmd_Quest_NOACTIVE.getValue());
 			return false;
 		}
 		p.sendMessage(QuestUtils.getStatusString(g.getQuest()).split(QuestDetailsUtils.CODE_NEWLINE_SEQ));
 		return true;
 	}
 	
 	public Boolean enter(Player p, String[] args) {
 		if (args.length!=0){
 			p.sendMessage(I18NMessage.Cmd_INVALIDARGS.getValue());
 			return false;
 		}
 		if (Managers.getQuestGroupManager().indexOf(p)==-1){
 			p.sendMessage(ChatColor.RED + I18NMessage.Cmd_NOPARTY.getValue());
 			return false;
 		}
 		QuestGroup g = Managers.getQuestGroupManager().get(p);
 		if (g.getQuest()==null){
 			p.sendMessage(ChatColor.RED + I18NMessage.Cmd_Quest_NOACTIVE.getValue());
 			return false;
 		}
 		if (!g.getQuest().isInstanced()){
 			p.sendMessage(ChatColor.RED + I18NMessage.Cmd_Quest_MAINWORLD.getValue());
 			return false;
 		}
 		if (!g.getLeader().equals(p)){
 			p.sendMessage(ChatColor.RED + I18NMessage.Cmd_NOTLEADER.getValue());
 			return false;
 		}
 		if (g.getQuestStatus()==QuestStatus.INQUEST){
 			p.sendMessage(ChatColor.RED + I18NMessage.Cmd_Quest_INQUEST.getValue());
 			return false;
 		}
 		try {
 			g.enterQuest();
 			return true;
 		} catch (GroupException e) {
 			e.printStackTrace();
 			p.sendMessage(ChatColor.GRAY + "ERR: " + e.getMessage());
 			return false;
 		}
 	}
 	
 	public Boolean exit(Player p, String[] args) {
 		if (args.length!=0){
 			p.sendMessage(I18NMessage.Cmd_INVALIDARGS.getValue());
 			return false;
 		}
 		if (Managers.getQuestGroupManager().indexOf(p)==-1){
 			p.sendMessage(ChatColor.RED + I18NMessage.Cmd_NOPARTY.getValue());
 			return false;
 		}
 		QuestGroup g = Managers.getQuestGroupManager().get(p);
 		if (g.getQuest()==null){
 			p.sendMessage(ChatColor.RED + I18NMessage.Cmd_Quest_NOACTIVE.getValue());
 			return false;
 		}
 		if (!g.getQuest().isInstanced()){
 			p.sendMessage(ChatColor.RED + I18NMessage.Cmd_Quest_MAINWORLD.getValue());
 			return false;
 		}
 		if (!g.getLeader().equals(p)){
 			p.sendMessage(ChatColor.RED + I18NMessage.Cmd_NOTLEADER.getValue());
 			return false;
 		}
 		if (g.getQuestStatus()!=QuestStatus.INQUEST){
 			p.sendMessage(ChatColor.RED + I18NMessage.Cmd_Quest_NOTINQUEST.getValue());
 			return false;
 		}
 		if (g.getQuest().isFinished()==null){
 			p.sendMessage(ChatColor.RED + I18NMessage.Cmd_Quest_EXITUNFINISHED.getValue());
 			return false;
 		}
 		try {
 			g.exitQuest();
 			return true;
 		} catch (GroupException e) {
 			e.printStackTrace();
 			p.sendMessage(ChatColor.GRAY + "ERR: " + e.getMessage());
 			return false;
 		}
 	}
 	
 	public Boolean info(Player p, String[] args){
 		if (args.length!=1){
 			p.sendMessage(I18NMessage.Cmd_INVALIDARGS.getValue());
 			return false;
 		}
 		QuestDetails qd = Managers.getQuestManager().getDetails(args[0]);
 		if (qd==null){
 			p.sendMessage(I18NMessage.Cmd_NOSUCHQUEST.getValue());
 			return false;
 		}
 		p.sendMessage(QuestDetailsUtils.getOverviewString(qd).split(QuestDetailsUtils.CODE_NEWLINE_SEQ));
 		return true;
 	}
 	
 	public Boolean reload(Player p, String[] args) {
 		if (args.length>1){
 			p.sendMessage(I18NMessage.Cmd_INVALIDARGS.getValue());
 			return false;
 		}
 		if (args.length==0)
 			Managers.getQuestManager().reloadQuests();
 		else
 			Managers.getQuestManager().reloadQuest(args[0]);
 		p.sendMessage(I18NMessage.Cmd_Quest_RELOAD.getValue());
 		return true;
 	}
 	
 	public Boolean start(final Player p, final String[] args) {
 		if (args.length!=1){
 			p.sendMessage(I18NMessage.Cmd_INVALIDARGS.getValue());
 			return false;
 		}
 		
 		Map<String, Date> quests = QuestStatisticUtils.getQuests(p.getName(), LogStatus.GIVEN);
 		
 		if (!quests.containsKey(args[0])){
 			p.sendMessage(I18NMessage.Cmd_Quest_NOTHAVEQUEST.getValue());
 			return false;
 		}
 		
 		final QuestDetails qd = Managers.getQuestManager().getDetails(args[0]);
 		if (qd==null){
 			p.sendMessage(I18NMessage.Cmd_Quest_UNAVAILABLE.getValue());
 			return false;
 		}
 		
 		if (Managers.getQuestGroupManager().indexOf(p)==-1){
 			Managers.getQuestGroupManager().createNewGroup(p);
 			p.sendMessage(ChatColor.YELLOW + I18NMessage.Cmd_Party_CREATE.getValue());
 		}
 		final QuestGroup g = Managers.getQuestGroupManager().get(p);
 		if (!g.getLeader().equals(p)){
 			p.sendMessage(ChatColor.RED + I18NMessage.Cmd_NOTLEADER.getValue());
 			return false;
 		}
 		if (g.getQuest()!=null){
 			p.sendMessage(ChatColor.RED + I18NMessage.Cmd_Quest_ALREADYACTIVE.getValue());
 			return false;
 		}
 		new Thread(new Runnable(){
 			
 			@Override
 			public void run() {
 				p.sendMessage(ChatColor.YELLOW + "[Quest] Starting up. May take a few minutes.");
 				try {
 					g.startQuest(qd);
 				} catch (GroupException e) {
 					e.printStackTrace();
 					p.sendMessage(ChatColor.RED + "[Quest] Couldn't start your quest. :C");
 					return;
 				}
 				p.sendMessage(ChatColor.YELLOW + "[Quest] Quest has been started!");
 			}
 			
 		}).start();
 		
 		return true;
 	}
 	
 	
 	public void help(CommandSender sender, String[] args) {
 		
 		Player p = (Player)sender;
 		
 		List<String> messages = new ArrayList<String>();
 		boolean inGroup = false;
 		boolean isLeader = false;
 		QuestStatus inQuest = QuestStatus.NOQUEST;
 		Quest active = null;
 		if (Managers.getQuestGroupManager().indexOf(p)!=-1){
 			QuestGroup g = Managers.getQuestGroupManager().get(p);
 			inGroup = true;
 			isLeader = g.getLeader().equals(p);
 			inQuest = g.getQuestStatus();
 			active = g.getQuest();
 		}
 		
 		/*
 		 * OP: reload [name]
 		 * given
 		 * main [name]
 		 * drop <name>
 		 * info
 		 * 
 		 * abandon
 		 * active
 		 * enter
 		 * exit
 		 * start <name>
 		 */
 		if (p.hasPermission("minequest.command.quest.reload"))
 			messages.add(ChatUtils.formatHelp("quest reload [name]", "Reload quest into memory (or all)"));
 		messages.add(ChatUtils.formatHeader(I18NMessage.Cmd_Quest_HELP.getValue()));
 		messages.add(ChatUtils.formatHelp("quest given", I18NMessage.Cmd_Quest_HELPGIVEN.getValue()));
 		messages.add(ChatUtils.formatHelp("quest main [name]", I18NMessage.Cmd_Quest_HELPMAIN.getValue()));
 		messages.add(ChatUtils.formatHelp("quest drop <name>", I18NMessage.Cmd_Quest_HELPDROP.getValue()));
 		messages.add(ChatUtils.formatHelp("quest info <name>", I18NMessage.Cmd_Quest_HELPINFO.getValue()));
 		
 		if (inGroup){
 			if (active != null && isLeader && active.isFinished()==null)
 				messages.add(ChatUtils.formatHelp("quest abandon", I18NMessage.Cmd_Quest_HELPABANDON.getValue()));
 			else if (active != null && isLeader && active.isFinished()!=null)
 				messages.add(ChatColor.GRAY + "[quest abandon] " + I18NMessage.Cmd_Quest_ALREADYDONE.getValue());
 			else if (active != null)
 				messages.add(ChatColor.GRAY + "[quest abandon] " + I18NMessage.Cmd_NOTLEADER.getValue());
 			else
 				messages.add(ChatColor.GRAY + "[quest abandon] " + I18NMessage.Cmd_Quest_NOACTIVE.getValue());
 			if (active!=null)
 				messages.add(ChatUtils.formatHelp("quest active", I18NMessage.Cmd_Quest_HELPACTIVE.getValue()));
 			else
 				messages.add(ChatColor.GRAY + "[quest active] " + I18NMessage.Cmd_Quest_NOACTIVE.getValue());
 			if (!isLeader)
 				messages.add(ChatColor.GRAY + "[quest enter/exit] " + I18NMessage.Cmd_NOTLEADER.getValue());
 			else{
 				switch(inQuest){
 				case NOTINQUEST:
 					messages.add(ChatUtils.formatHelp("quest enter", I18NMessage.Cmd_Quest_HELPENTER.getValue()));
 					break;
 				case INQUEST:
 					if (active.isFinished()!=null)
 						messages.add(ChatUtils.formatHelp("quest exit", I18NMessage.Cmd_Quest_HELPEXIT.getValue()));
 					else
 						messages.add(ChatColor.GRAY + "[quest exit] " + I18NMessage.Cmd_Quest_EXITUNFINISHED.getValue());
 					break;
 				case MAINWORLDQUEST:
 					messages.add(ChatColor.GRAY + "[quest enter/exit] " + I18NMessage.Cmd_Quest_MAINWORLD.getValue());
 					break;
 				default:
 					messages.add(ChatColor.GRAY + "[quest enter/exit] " + I18NMessage.Cmd_Quest_NOACTIVE.getValue());
 					break;
 				}
 			}
 			if (active == null && isLeader)
 				messages.add(ChatUtils.formatHelp("quest start <name>", I18NMessage.Cmd_Quest_HELPSTART.getValue()));
 			else if (active == null)
 				messages.add(ChatColor.GRAY + "[quest start] " + I18NMessage.Cmd_NOTLEADER.getValue());
 			else
 				messages.add(ChatColor.GRAY + "[quest start] " + I18NMessage.Cmd_Quest_ALREADYACTIVE.getValue());
 		} else {
 			messages.add(ChatUtils.formatHelp("quest start <name>", I18NMessage.Cmd_Quest_HELPSTARTNOPARTY.getValue()));
 			messages.add(ChatColor.AQUA + I18NMessage.Cmd_Quest_JOINPARTY.getValue());
 		}
 		
 		for (String m : messages) {
 			p.sendMessage(m);
 		}
 	}
 	
 	@Override
 	public boolean allowConsole() {
 		return false;
 	}
 	
 	@Override
 	public void noOptionSpecified(CommandSender sender, String[] args) {
 		Player p = (Player)sender;
 		
 		/* Given Quests */
 		Map<String, Date> givenquests = QuestStatisticUtils.getQuests(p.getName(),LogStatus.GIVEN);
 		
 		List<String> givenmessage = new ArrayList<String>();
 		givenmessage.add(ChatUtils.formatHeader(I18NMessage.Cmd_Quest_GIVEN.getValue()));
 		for (String q : givenquests.keySet()){
 			if (q.isEmpty())
 				continue;
 			QuestDetails qd = Managers.getQuestManager().getDetails(q);
 			if (qd!=null){
 				givenmessage.add(ChatColor.AQUA + q + " : " + ChatColor.GOLD + qd.getProperty(QuestDetails.QUEST_DISPLAYNAME));
 			} else {
 				givenmessage.add(ChatColor.GRAY + q + " : <?>");
 			}
 		}
 		
 		for (String m : givenmessage)
 			p.sendMessage(m);
 		if (givenmessage.size()==1)
 			p.sendMessage(I18NMessage.Cmd_NOQUESTS.getValue());
 		
 		/* Main World Quests */
 		Map<String, Date> mqquests = QuestStatisticUtils.getQuests(p.getName(), LogStatus.ACTIVE);
 		List<String> mwmessage = new ArrayList<String>();
 		mwmessage.add(ChatUtils.formatHeader(I18NMessage.Cmd_Quest_MAIN.getValue()));
 		for (String q : mqquests.keySet()){
 			if (q.isEmpty())
 				continue;
 			Quest quest = Managers.getQuestManager().getMainWorldQuest(p.getName(), q);
 			mwmessage.add(ChatColor.LIGHT_PURPLE + q + " : " + ChatColor.GOLD + quest.getDetails().getProperty(QuestDetails.QUEST_NAME));
 		}
 		for (String m : mwmessage)
 			p.sendMessage(m);
 		if (mwmessage.size()==1)
 			p.sendMessage(I18NMessage.Cmd_NOQUESTS.getValue());
 		
 		p.sendMessage(ChatUtils.formatHeader("Help Options: /quest help"));
 	}
 	
 }
