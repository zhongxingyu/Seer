 package com.theminequest.MineQuest.Frontend.Command;
 
 import java.io.File;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.logging.Level;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import com.theminequest.MineQuest.I18NMessage;
 import com.theminequest.MineQuest.MineQuest;
 import com.theminequest.MineQuest.Backend.BackendFailedException;
 import com.theminequest.MineQuest.Backend.GroupBackend;
 import com.theminequest.MineQuest.Backend.QuestAvailability;
 import com.theminequest.MineQuest.Backend.QuestBackend;
 import com.theminequest.MineQuest.Backend.BackendFailedException.BackendReason;
 import com.theminequest.MineQuest.Group.Group;
 import com.theminequest.MineQuest.Group.GroupException;
 import com.theminequest.MineQuest.Quest.Quest;
 import com.theminequest.MineQuest.Quest.QuestDescription;
 import com.theminequest.MineQuest.Utils.ChatUtils;
 import com.theminequest.MineQuest.Utils.PropertiesFile;
 
 public class QuestCommandFrontend extends CommandFrontend {
 
 	/*
 	 * TODO list:
 	 * discard quest function?
 	 */
 
 	public QuestCommandFrontend(){
 		super("quest");
 	}
 
 	public Boolean accept(Player p, String[] args) {
 		if (args.length!=1){
 			p.sendMessage(I18NMessage.Cmd_INVALIDARGS.getDescription());
 			return false;
 		}
 		try {
 			QuestBackend.acceptQuest(p, args[0]);
 			return true;
 		} catch (BackendFailedException e) {
 			if (e.getReason()==BackendReason.NOTHAVEQUEST)
 				p.sendMessage(I18NMessage.Cmd_Quest_NOTHAVEQUEST.getDescription());
 			else {
 				e.printStackTrace();
 				p.sendMessage(I18NMessage.Cmd_SQLException.getDescription());
 			}
 			return false;
 		}
 	}
 
 	public Boolean accepted(Player p, String[] args) {
 		if (args.length!=0){
 			p.sendMessage(I18NMessage.Cmd_INVALIDARGS.getDescription());
 			return false;
 		}
 		List<String> quests;
 		try {
 			quests = QuestBackend.getQuests(QuestAvailability.ACCEPTED, p);
 		} catch (SQLException e) {
 			e.printStackTrace();
 			p.sendMessage(I18NMessage.Cmd_SQLException.getDescription());
 			return false;
 		}
 
 		List<String> message = new ArrayList<String>();
 		message.add(ChatUtils.formatHeader(I18NMessage.Cmd_Quest_ACCEPTED.getDescription()));
 		for (String q : quests){
 			message.add(ChatColor.AQUA + q);
 		}
 
 		for (String m : message)
 			p.sendMessage(m);
 		return true;
 	}
 
 	public Boolean available(Player p, String[] args){
 		if (args.length!=0){
 			p.sendMessage(I18NMessage.Cmd_INVALIDARGS.getDescription());
 			return false;
 		}
 		List<String> quests;
 		try {
 			quests = QuestBackend.getQuests(QuestAvailability.AVAILABLE, p);
 		} catch (SQLException e) {
 			e.printStackTrace();
 			p.sendMessage(I18NMessage.Cmd_SQLException.getDescription());
 			return false;
 		}
 
 		List<String> message = new ArrayList<String>();
 		message.add(ChatUtils.formatHeader(I18NMessage.Cmd_Quest_AVAILABLE.getDescription()));
 		for (String q : quests){
 			message.add(ChatColor.AQUA + q);
 		}
 
 		for (String m : message)
 			p.sendMessage(m);
 		return true;
 	}
 
 	public Boolean abandon(Player p, String[] args) {
 		if (args.length!=0){
 			p.sendMessage(I18NMessage.Cmd_INVALIDARGS.getDescription());
 			return false;
 		}
 		if (GroupBackend.teamID(p)==-1){
 			p.sendMessage(ChatColor.RED + I18NMessage.Cmd_NOPARTY.getDescription());
 			return false;
 		}
 		Group g = GroupBackend.getCurrentGroup(p);
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
 		if (GroupBackend.teamID(p)==-1){
 			p.sendMessage(ChatColor.RED + I18NMessage.Cmd_NOPARTY.getDescription());
 			return false;
 		}
 		Group g = GroupBackend.getCurrentGroup(p);
 		if (g.getQuest()==null){
 			p.sendMessage(ChatColor.RED + I18NMessage.Cmd_Quest_NOACTIVE.getDescription());
 			return false;
 		}
 		p.sendMessage(ChatUtils.formatHeader("Active: " + g.getQuest().getName()));
 		p.sendMessage(g.getQuest().details.toString());
 		return true;
 	}
 
 	public Boolean enter(Player p, String[] args) {
 		if (args.length!=0){
 			p.sendMessage(I18NMessage.Cmd_INVALIDARGS.getDescription());
 			return false;
 		}
 		if (GroupBackend.teamID(p)==-1){
 			p.sendMessage(ChatColor.RED + I18NMessage.Cmd_NOPARTY.getDescription());
 			return false;
 		}
 		Group g = GroupBackend.getCurrentGroup(p);
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
 		if (g.isInQuest()){
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
 		if (GroupBackend.teamID(p)==-1){
 			p.sendMessage(ChatColor.RED + I18NMessage.Cmd_NOPARTY.getDescription());
 			return false;
 		}
 		Group g = GroupBackend.getCurrentGroup(p);
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
 		if (!g.isInQuest()){
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
 		QuestDescription qd = QuestBackend.getQuestDesc(args[0]);
 		if (qd==null){
 			p.sendMessage(I18NMessage.Cmd_NOSUCHQUEST.getDescription());
 			return false;
 		}
		p.sendMessage(qd.toString());
 		return true;
 	}
 
 	public Boolean start(final Player p, final String[] args) {
 		if (args.length!=1){
 			p.sendMessage(I18NMessage.Cmd_INVALIDARGS.getDescription());
 			return false;
 		}
 		if (GroupBackend.teamID(p)==-1){
 			p.sendMessage(ChatColor.RED + I18NMessage.Cmd_NOPARTY.getDescription());
 			return false;
 		}
 		final Group g = GroupBackend.getCurrentGroup(p);
 		if (!g.getLeader().equals(p)){
 			p.sendMessage(ChatColor.RED + I18NMessage.Cmd_NOTLEADER.getDescription());
 			return false;
 		}
 		if (g.getQuest()!=null){
 			p.sendMessage(ChatColor.RED + I18NMessage.Cmd_Quest_ALREADYACTIVE.getDescription());
 			return false;
 		}
 
 		List<String> quests;
 		try {
 			quests = QuestBackend.getQuests(QuestAvailability.ACCEPTED, p);
 		} catch (SQLException e) {
 			e.printStackTrace();
 			p.sendMessage(I18NMessage.Cmd_SQLException.getDescription());
 			return false;
 		}
 
 		if (!quests.contains(args[0])){
 			p.sendMessage(I18NMessage.Cmd_Quest_NOTHAVEQUEST.getDescription());
 			return false;
 		}
 		// TEST new multithreading
 		new Thread(new Runnable(){
 
 			@Override
 			public void run() {
 				p.sendMessage("Starting up...");
 				try {
 					g.startQuest(args[0]);
 				} catch (GroupException e) {
 					e.printStackTrace();
 					p.sendMessage(ChatColor.GRAY + "ERR: " + e.getMessage());
 					return;
 				}
 				p.sendMessage("Quest has been started!");
 			}
 
 		}).start();
 		return true;
 	}
 
 	public Boolean help(Player p, String[] args) {
 
 		List<String> messages = new ArrayList<String>();
 		boolean inGroup = false;
 		boolean isLeader = false;
 		boolean inQuest = false;
 		Quest active = null;
 		if (GroupBackend.teamID(p)!=-1){
 			Group g = GroupBackend.getCurrentGroup(p);
 			inGroup = true;
 			isLeader = g.getLeader().equals(p);
 			inQuest = g.isInQuest();
 			active = g.getQuest();
 		}
 
 		/*
 		 * accept <name>
 		 * accepted
 		 * available
 		 * 
 		 * abandon
 		 * active
 		 * enter
 		 * exit
 		 * start <name>
 		 */
 		messages.add(ChatUtils.formatHeader(I18NMessage.Cmd_Quest_HELP.getDescription()));
 		messages.add(ChatUtils.formatHelp("quest accept <name>", I18NMessage.Cmd_Quest_HELPACCEPT.getDescription()));
 		messages.add(ChatUtils.formatHelp("quest accepted", I18NMessage.Cmd_Quest_HELPACCEPTED.getDescription()));
 		messages.add(ChatUtils.formatHelp("quest available", I18NMessage.Cmd_Quest_HELPAVAILABLE.getDescription()));
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
 			if (active!=null && !inQuest && isLeader && active.isInstanced())
 				messages.add(ChatUtils.formatHelp("quest enter", I18NMessage.Cmd_Quest_HELPENTER.getDescription()));
 			else if (active!=null && inQuest && isLeader && active.isInstanced() && active.isFinished()!=null )
 				messages.add(ChatUtils.formatHelp("quest exit", I18NMessage.Cmd_Quest_HELPEXIT.getDescription()));
 			else if (active!=null && inQuest && isLeader && active.isInstanced() )
 				messages.add(ChatColor.GRAY + "[quest exit] " + I18NMessage.Cmd_Quest_EXITUNFINISHED.getDescription());
 			else if (active!=null && !active.isInstanced())
 				messages.add(ChatColor.GRAY + "[quest enter/exit] " + I18NMessage.Cmd_Quest_MAINWORLD.getDescription());
 			else if (active!=null && !isLeader)
 				messages.add(ChatColor.GRAY + "[quest enter/exit] " + I18NMessage.Cmd_NOTLEADER.getDescription());
 			else
 				messages.add(ChatColor.GRAY + "[quest enter/exit] " + I18NMessage.Cmd_Quest_NOACTIVE.getDescription());
 			if (active == null && isLeader)
 				messages.add(ChatUtils.formatHelp("quest start <name>", I18NMessage.Cmd_Quest_HELPSTART.getDescription()));
 			else if (active == null)
 				messages.add(ChatColor.GRAY + "[quest start] " + I18NMessage.Cmd_NOTLEADER.getDescription());
 			else
 				messages.add(ChatColor.GRAY + "[quest start] " + I18NMessage.Cmd_Quest_ALREADYACTIVE.getDescription());
 		} else
 			messages.add(ChatColor.AQUA + I18NMessage.Cmd_Quest_JOINPARTY.getDescription());
 
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
