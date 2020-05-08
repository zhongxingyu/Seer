 package com.theminequest.MineQuest.Frontend.Command;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 
 import org.bukkit.ChatColor;
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
 			p.sendMessage(I18NMessage.Cmd_INVALIDARGS.getDescription());
 			return false;
 		}
 		Map<String, Date> quests = QuestStatisticUtils.getQuests(p.getName(),LogStatus.GIVEN);
 
 		List<String> message = new ArrayList<String>();
 		message.add(ChatUtils.formatHeader(I18NMessage.Cmd_Quest_ACCEPTED.getDescription()));
 		for (String q : quests.keySet()){
 			if (q.isEmpty())
 				continue;
 			QuestDetails qd = Managers.getQuestManager().getDetails(q);
 			if (qd!=null){
 				message.add(ChatColor.AQUA + q + " : " + ChatColor.GOLD + qd.getProperty(QuestDetails.QUEST_NAME));
 			} else {
 				message.add(ChatColor.GRAY + q + " : <unavailable>");
 			}
 		}
 
 		for (String m : message)
 			p.sendMessage(m);
 		return true;
 	}
 
 	public Boolean main(Player p, String[] args) {
 		if (args.length>1){
 			p.sendMessage(I18NMessage.Cmd_INVALIDARGS.getDescription());
 			return false;
 		}
 		if (args.length==0){
 			Map<String, Date> quests = QuestStatisticUtils.getQuests(p.getName(), LogStatus.ACTIVE);
 			List<String> message = new ArrayList<String>();
 			message.add(ChatUtils.formatHeader(I18NMessage.Cmd_Quest_MWACTIVE.getDescription()));
 			for (String q : quests.keySet()){
 				if (q.isEmpty())
 					continue;
 				Quest quest = Managers.getQuestManager().getMainWorldQuest(p.getName(), q);
 				message.add(ChatColor.LIGHT_PURPLE + q + " : " + ChatColor.GOLD + quest.getDetails().getProperty(QuestDetails.QUEST_NAME));
 			}
 			for (String m : message)
 				p.sendMessage(m);
 			return true;
 		} else {
 			String name = args[0];
			Quest q = Managers.getQuestManager().getMainWorldQuest(p.getName(),name);
			if (q != null) {
 				p.sendMessage(QuestUtils.getStatusString(q).split("\n"));
 				return true;
			} else {
 				p.sendMessage(I18NMessage.Cmd_NOSUCHQUEST.getDescription());
 				return true;
 			}
 		}
 	}
 
 	public Boolean drop(Player p, String[] args) {
 		if (args.length!=1){
 			p.sendMessage(I18NMessage.Cmd_INVALIDARGS.getDescription());
 			return false;
 		}
 		try {
 			QuestStatisticUtils.dropQuest(p.getName(), args[0]);
 		} catch (QSException e) {
 			p.sendMessage(I18NMessage.Cmd_Quest_NOTHAVEQUEST.getDescription());
 			return false;
 		}
 		return true;
 	}
 
 	public Boolean abandon(Player p, String[] args) {
 		if (args.length!=0){
 			p.sendMessage(I18NMessage.Cmd_INVALIDARGS.getDescription());
 			return false;
 		}
 		if (Managers.getQuestGroupManager().indexOf(p)==-1){
 			p.sendMessage(ChatColor.RED + I18NMessage.Cmd_NOPARTY.getDescription());
 			return false;
 		}
 		QuestGroup g = Managers.getQuestGroupManager().get(p);
 		if (!g.getLeader().equals(p)){
 			p.sendMessage(ChatColor.RED + I18NMessage.Cmd_NOTLEADER.getDescription());
 			return false;
 		}
 		if (g.getQuest()==null){
 			p.sendMessage(ChatColor.RED + I18NMessage.Cmd_Quest_NOACTIVE.getDescription());
 			return false;
 		}
 		if (g.getQuest().isFinished()!=null){
 			p.sendMessage(ChatColor.RED + I18NMessage.Cmd_Quest_ALREADYDONE.getDescription());
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
 			p.sendMessage(I18NMessage.Cmd_INVALIDARGS.getDescription());
 			return false;
 		}
 		if (Managers.getQuestGroupManager().indexOf(p)==-1){
 			p.sendMessage(ChatColor.RED + I18NMessage.Cmd_NOPARTY.getDescription());
 			return false;
 		}
 		QuestGroup g = Managers.getQuestGroupManager().get(p);
 		if (g.getQuest()==null){
 			p.sendMessage(ChatColor.RED + I18NMessage.Cmd_Quest_NOACTIVE.getDescription());
 			return false;
 		}
 		p.sendMessage(QuestUtils.getStatusString(g.getQuest()).split("\n"));
 		return true;
 	}
 
 	public Boolean enter(Player p, String[] args) {
 		if (args.length!=0){
 			p.sendMessage(I18NMessage.Cmd_INVALIDARGS.getDescription());
 			return false;
 		}
 		if (Managers.getQuestGroupManager().indexOf(p)==-1){
 			p.sendMessage(ChatColor.RED + I18NMessage.Cmd_NOPARTY.getDescription());
 			return false;
 		}
 		QuestGroup g = Managers.getQuestGroupManager().get(p);
 		if (g.getQuest()==null){
 			p.sendMessage(ChatColor.RED + I18NMessage.Cmd_Quest_NOACTIVE.getDescription());
 			return false;
 		}
 		if (!g.getQuest().isInstanced()){
 			p.sendMessage(ChatColor.RED + I18NMessage.Cmd_Quest_MAINWORLD.getDescription());
 			return false;
 		}
 		if (!g.getLeader().equals(p)){
 			p.sendMessage(ChatColor.RED + I18NMessage.Cmd_NOTLEADER.getDescription());
 			return false;
 		}
 		if (g.getQuestStatus()==QuestStatus.INQUEST){
 			p.sendMessage(ChatColor.RED + I18NMessage.Cmd_Quest_INQUEST.getDescription());
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
 			p.sendMessage(I18NMessage.Cmd_INVALIDARGS.getDescription());
 			return false;
 		}
 		if (Managers.getQuestGroupManager().indexOf(p)==-1){
 			p.sendMessage(ChatColor.RED + I18NMessage.Cmd_NOPARTY.getDescription());
 			return false;
 		}
 		QuestGroup g = Managers.getQuestGroupManager().get(p);
 		if (g.getQuest()==null){
 			p.sendMessage(ChatColor.RED + I18NMessage.Cmd_Quest_NOACTIVE.getDescription());
 			return false;
 		}
 		if (!g.getQuest().isInstanced()){
 			p.sendMessage(ChatColor.RED + I18NMessage.Cmd_Quest_MAINWORLD.getDescription());
 			return false;
 		}
 		if (!g.getLeader().equals(p)){
 			p.sendMessage(ChatColor.RED + I18NMessage.Cmd_NOTLEADER.getDescription());
 			return false;
 		}
 		if (g.getQuestStatus()!=QuestStatus.INQUEST){
 			p.sendMessage(ChatColor.RED + I18NMessage.Cmd_Quest_NOTINQUEST.getDescription());
 			return false;
 		}
 		if (g.getQuest().isFinished()==null){
 			p.sendMessage(ChatColor.RED + I18NMessage.Cmd_Quest_EXITUNFINISHED.getDescription());
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
 			p.sendMessage(I18NMessage.Cmd_INVALIDARGS.getDescription());
 			return false;
 		}
 		QuestDetails qd = Managers.getQuestManager().getDetails(args[0]);
 		if (qd==null){
 			p.sendMessage(I18NMessage.Cmd_NOSUCHQUEST.getDescription());
 			return false;
 		}
 		p.sendMessage(QuestDetailsUtils.getOverviewString(qd).split("\n"));
 		return true;
 	}
 
 	public Boolean reload(Player p, String[] args) {
 		if (args.length>1){
 			p.sendMessage(I18NMessage.Cmd_INVALIDARGS.getDescription());
 			return false;
 		}
 		if (args.length==0)
 			Managers.getQuestManager().reloadQuests();
 		else
 			Managers.getQuestManager().reloadQuest(args[0]);
 		return true;
 	}
 
 	public Boolean start(final Player p, final String[] args) {
 		if (args.length!=1){
 			p.sendMessage(I18NMessage.Cmd_INVALIDARGS.getDescription());
 			return false;
 		}
 
 		Map<String, Date> quests = QuestStatisticUtils.getQuests(p.getName(), LogStatus.GIVEN);
 
 		if (!quests.containsKey(args[0])){
 			p.sendMessage(I18NMessage.Cmd_Quest_NOTHAVEQUEST.getDescription());
 			return false;
 		}
 
 		final QuestDetails qd = Managers.getQuestManager().getDetails(args[0]);
 		if (qd==null){
 			p.sendMessage(I18NMessage.Cmd_Quest_UNAVAILABLE.getDescription());
 			return false;
 		}
 
 		if (Managers.getQuestGroupManager().indexOf(p)==-1){
 			Managers.getQuestGroupManager().createNewGroup(p);
 			p.sendMessage(ChatColor.YELLOW + I18NMessage.Cmd_Party_CREATE.getDescription());
 		}
 		final QuestGroup g = Managers.getQuestGroupManager().get(p);
 		if (!g.getLeader().equals(p)){
 			p.sendMessage(ChatColor.RED + I18NMessage.Cmd_NOTLEADER.getDescription());
 			return false;
 		}
 		if (g.getQuest()!=null){
 			p.sendMessage(ChatColor.RED + I18NMessage.Cmd_Quest_ALREADYACTIVE.getDescription());
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
 
 
 	public Boolean help(Player p, String[] args) {
 
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
 		messages.add(ChatUtils.formatHeader(I18NMessage.Cmd_Quest_HELP.getDescription()));
 		messages.add(ChatUtils.formatHelp("quest given", I18NMessage.Cmd_Quest_HELPGIVEN.getDescription()));
 		messages.add(ChatUtils.formatHelp("quest main [name]", I18NMessage.Cmd_Quest_HELPMAIN.getDescription()));
 		messages.add(ChatUtils.formatHelp("quest drop <name>", I18NMessage.Cmd_Quest_HELPDROP.getDescription()));
 		messages.add(ChatUtils.formatHelp("quest info <name>", I18NMessage.Cmd_Quest_HELPINFO.getDescription()));
 
 		if (inGroup){
 			if (active != null && isLeader && active.isFinished()==null)
 				messages.add(ChatUtils.formatHelp("quest abandon", I18NMessage.Cmd_Quest_HELPABANDON.getDescription()));
 			else if (active != null && isLeader && active.isFinished()!=null)
 				messages.add(ChatColor.GRAY + "[quest abandon] " + I18NMessage.Cmd_Quest_ALREADYDONE.getDescription());
 			else if (active != null)
 				messages.add(ChatColor.GRAY + "[quest abandon] " + I18NMessage.Cmd_NOTLEADER.getDescription());
 			else
 				messages.add(ChatColor.GRAY + "[quest abandon] " + I18NMessage.Cmd_Quest_NOACTIVE.getDescription());
 			if (active!=null)
 				messages.add(ChatUtils.formatHelp("quest active", I18NMessage.Cmd_Quest_HELPACTIVE.getDescription()));
 			else
 				messages.add(ChatColor.GRAY + "[quest active] " + I18NMessage.Cmd_Quest_NOACTIVE.getDescription());
 			if (!isLeader)
 				messages.add(ChatColor.GRAY + "[quest enter/exit] " + I18NMessage.Cmd_NOTLEADER.getDescription());
 			else{
 				switch(inQuest){
 				case NOTINQUEST:
 					messages.add(ChatUtils.formatHelp("quest enter", I18NMessage.Cmd_Quest_HELPENTER.getDescription()));
 					break;
 				case INQUEST:
 					if (active.isFinished()!=null)
 						messages.add(ChatUtils.formatHelp("quest exit", I18NMessage.Cmd_Quest_HELPEXIT.getDescription()));
 					else
 						messages.add(ChatColor.GRAY + "[quest exit] " + I18NMessage.Cmd_Quest_EXITUNFINISHED.getDescription());
 					break;
 				case MAINWORLDQUEST:
 					messages.add(ChatColor.GRAY + "[quest enter/exit] " + I18NMessage.Cmd_Quest_MAINWORLD.getDescription());
 					break;
 				default:
 					messages.add(ChatColor.GRAY + "[quest enter/exit] " + I18NMessage.Cmd_Quest_NOACTIVE.getDescription());
 					break;
 				}
 			}
 			if (active == null && isLeader)
 				messages.add(ChatUtils.formatHelp("quest start <name>", I18NMessage.Cmd_Quest_HELPSTART.getDescription()));
 			else if (active == null)
 				messages.add(ChatColor.GRAY + "[quest start] " + I18NMessage.Cmd_NOTLEADER.getDescription());
 			else
 				messages.add(ChatColor.GRAY + "[quest start] " + I18NMessage.Cmd_Quest_ALREADYACTIVE.getDescription());
 		} else {
 			messages.add(ChatUtils.formatHelp("quest start <name>", I18NMessage.Cmd_Quest_HELPSTARTNOPARTY.getDescription()));
 			messages.add(ChatColor.AQUA + I18NMessage.Cmd_Quest_JOINPARTY.getDescription());
 		}
 
 		for (String m : messages) {
 			p.sendMessage(m);
 		}
 
 		return true;
 	}
 
 	@Override
 	public boolean allowConsole() {
 		return false;
 	}
 
 }
