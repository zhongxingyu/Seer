 package com.theminequest.bukkit.frontend.cmd;
 
 import static com.theminequest.common.util.I18NMessage._;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import com.theminequest.api.ManagerException;
 import com.theminequest.api.Managers;
 import com.theminequest.api.group.Group;
 import com.theminequest.api.group.GroupException;
 import com.theminequest.api.platform.MQPlayer;
 import com.theminequest.api.util.ChatUtils;
 
 public class PartyCommandFrontend extends CommandFrontend {
 	
 	public static final String IINPARTY = ChatColor.YELLOW + _("You're already in a party!");
 	public static final String IOUTPARTY = ChatColor.YELLOW + _("You're not in a party!");
 	public static final String INOINVITE = ChatColor.YELLOW + _("You haven't been invited to a party!");
 	public static final String IACCEPT = ChatColor.GREEN + _("Accepted invite and joined the party!");
 	public static final String IDISCARD = ChatColor.YELLOW + _("Discarded party invite.");
 	public static final String ICREATE = ChatColor.GREEN + _("Created a new party!");
 	public static final String INOTLEADER = ChatColor.YELLOW + _("You're not the leader of the party!");
 	public static final String ITARGETINPARTY = ChatColor.YELLOW + _("%n% is already in the party!");
 	public static final String ITARGETOUTPARTY = ChatColor.YELLOW + _("%n% isn't in the party!");
 	public static final String ITARGETINVITED = ChatColor.GOLD + _("Invited %n%.");
 	public static final String IKICKED = ChatColor.YELLOW + _("Kicked %n% from the party.");
 	public static final String ITARGETKICKED = ChatColor.RED + _("You've been kicked by %n% from the party.");
 	public static final String ILEAVE = ChatColor.YELLOW + _("Left the party.");
 	public static final String ILIST = _("Players in the Party");
 	public static final String IPROMOTE = ChatColor.YELLOW + _("Promoted %n% to leader!");
 	
 	public PartyCommandFrontend() {
 		super("party");
 	}
 	
 	public void accept(Player pl, String[] args) {
 		MQPlayer p = Managers.getPlatform().toPlayer(pl);
 		if (Managers.getGroupManager().indexOf(p) != -1) {
 			p.sendMessage(PartyCommandFrontend.IINPARTY);
 			return;
 		}
 		if (!Managers.getGroupManager().hasInvite(p)) {
 			p.sendMessage(PartyCommandFrontend.INOINVITE);
 			return;
 		}
 		try {
 			Managers.getGroupManager().acceptInvite(p);
 			p.sendMessage(PartyCommandFrontend.IACCEPT);
 		} catch (ManagerException e) {
 			throw new RuntimeException(e); // toss this to CommandFrontend
 		}
 	}
 	
 	public void create(Player pl, String[] args) {
 		MQPlayer p = Managers.getPlatform().toPlayer(pl);
 
 		if (Managers.getGroupManager().indexOf(p) != -1) {
 			p.sendMessage(PartyCommandFrontend.IINPARTY);
 			return;
 		}
 		if (Managers.getGroupManager().hasInvite(p))
 			p.sendMessage(PartyCommandFrontend.IDISCARD);
 		Managers.getGroupManager().createNewGroup(p);
 		p.sendMessage(PartyCommandFrontend.ICREATE);
 	}
 	
 	public void invite(Player pl, String[] args) {
 		MQPlayer p = Managers.getPlatform().toPlayer(pl);
 
 		if (Managers.getGroupManager().indexOf(p) == -1) {
 			p.sendMessage(PartyCommandFrontend.IOUTPARTY);
 			return;
 		}
 		if (args.length != 1) {
 			p.sendMessage(CommandFrontend.IINVALID);
 			return;
 		}
 		Group g = Managers.getGroupManager().get(p);
 		if (!g.getLeader().equals(p)) {
 			p.sendMessage(PartyCommandFrontend.INOTLEADER);
 			return;
 		}
 		
 		Player mate = Bukkit.getPlayerExact(args[0]);
 		if (mate == null) {
 			p.sendMessage(CommandFrontend.INOPLAYER);
 			return;
 		}
 		MQPlayer mqmate = Managers.getPlatform().toPlayer(mate);
 		
 		if (Managers.getGroupManager().indexOf(mqmate) != -1) {
 			p.sendMessage(PartyCommandFrontend.ITARGETINPARTY);
 			return;
 		}
 		try {
 			Managers.getGroupManager().invite(mqmate, g);
 			p.sendMessage(PartyCommandFrontend.ITARGETINVITED);
 		} catch (ManagerException e) {
 			throw new RuntimeException(e); // throw to CommandFrontend
 		}
 	}
 	
 	public void kick(Player pl, String[] args) {
 		MQPlayer p = Managers.getPlatform().toPlayer(pl);
 		
 		if (Managers.getGroupManager().indexOf(p) == -1) {
 			p.sendMessage(PartyCommandFrontend.IOUTPARTY);
 			return;
 		}
 		if (args.length != 1) {
 			p.sendMessage(CommandFrontend.IINVALID);
 			return;
 		}
 		Group g = Managers.getGroupManager().get(p);
 		if (!g.getLeader().equals(p)) {
 			p.sendMessage(PartyCommandFrontend.INOTLEADER);
 			return;
 		}
 		Player mate = Bukkit.getPlayerExact(args[0]);
 		
 		MQPlayer mqmate = Managers.getPlatform().toPlayer(mate);
 		if (!g.contains(mqmate)) {
 			p.sendMessage(PartyCommandFrontend.ITARGETOUTPARTY);
 			return;
 		}
 		
 		try {
 			g.remove(mqmate);
 			pl.sendMessage(PartyCommandFrontend.IKICKED.replace("%n%", mate.getDisplayName()));
 			mate.sendMessage(PartyCommandFrontend.ITARGETKICKED.replace("%n%", p.getDisplayName()));
 		} catch (GroupException e) {
 			throw new RuntimeException(e); // toss to CommandFrontend
 		}
 		
 	}
 	
 	public void leave(Player pl, String[] args) {
 		
 		MQPlayer p = Managers.getPlatform().toPlayer(pl);
 		if (Managers.getGroupManager().indexOf(p) == -1) {
 			p.sendMessage(PartyCommandFrontend.IOUTPARTY);
 			return;
 		}
 		if (args.length != 0) {
 			p.sendMessage(CommandFrontend.IINVALID);
 			return;
 		}
 		Group g = Managers.getGroupManager().get(p);
 		boolean leader = g.getLeader().equals(p);
 		
 		try {
 			if (leader && (g.getMembers().size() != 1))
 				g.setLeader(g.getMembers().get(1));
 			g.remove(p);
 			p.sendMessage(PartyCommandFrontend.ILEAVE);
 		} catch (GroupException e) {
 			throw new RuntimeException(e); // toss to CommandFrontend
 		}
 		
 	}
 	
 	public void list(Player pl, String[] args) {
 		MQPlayer p = Managers.getPlatform().toPlayer(pl);
 		
 		if (Managers.getGroupManager().indexOf(p) == -1) {
 			p.sendMessage(PartyCommandFrontend.IOUTPARTY);
 			return;
 		}
 		if (args.length != 0) {
 			p.sendMessage(CommandFrontend.IINVALID);
 			return;
 		}
 		Group g = Managers.getGroupManager().get(p);
 		List<MQPlayer> members = g.getMembers();
 		List<String> messages = new ArrayList<String>();
 		messages.add(ChatUtils.formatHeader(PartyCommandFrontend.ILIST) + " " + members.size() + "/" + g.getCapacity());
 		for (MQPlayer m : members) {
 			Player bPlayer = Bukkit.getPlayerExact(m.getName());
 			if (g.getLeader().equals(m))
 				messages.add(ChatColor.RED + bPlayer.getName() + ChatColor.GRAY + " : Lvl " + bPlayer.getLevel() + ", Health " + m.getHealth() + "/" + bPlayer.getMaxHealth());
 			else
 				messages.add(ChatColor.AQUA + bPlayer.getName() + ChatColor.GRAY + " : Lvl " + bPlayer.getLevel() + ", Health " + bPlayer.getHealth() + "/" + bPlayer.getMaxHealth());
 		}
 		
 		for (String m : messages)
 			p.sendMessage(m);
 	}
 	
 	public void promote(Player pl, String[] args) {
 		MQPlayer p = Managers.getPlatform().toPlayer(pl);
 		
 		if (Managers.getGroupManager().indexOf(p) == -1) {
 			p.sendMessage(PartyCommandFrontend.IOUTPARTY);
 			return;
 		}
 		if (args.length != 1) {
 			p.sendMessage(CommandFrontend.IINVALID);
 			return;
 		}
 		Group g = Managers.getGroupManager().get(p);
 		if (!g.getLeader().equals(p)) {
 			p.sendMessage(PartyCommandFrontend.INOTLEADER);
 			return;
 		}
 		Player mate = Bukkit.getPlayerExact(args[0]);
 		
 		MQPlayer mqmate = Managers.getPlatform().toPlayer(mate);
 		if (!g.contains(mqmate)) {
 			p.sendMessage(PartyCommandFrontend.ITARGETOUTPARTY);
 			return;
 		}
 		
 		try {
 			g.setLeader(mqmate);
 			for (MQPlayer member : g.getMembers())
 				member.sendMessage(PartyCommandFrontend.IPROMOTE);
 		} catch (GroupException e) {
 			throw new RuntimeException(e); // throw to CommandFrontend
 		}
 	}
 	
 	@Override
 	public void help(CommandSender sender, String[] args) {
 		Player pl = (Player) sender;
 		MQPlayer p = Managers.getPlatform().toPlayer(pl);
 		/*
 		 * accept
 		 * create
 		 * 
 		 * info
 		 * invite
 		 * kick
 		 * leave
 		 * list
 		 * promote
 		 */
 		
 		List<String> messages = new ArrayList<String>();
 		Group g = null;
 		boolean invite = Managers.getGroupManager().hasInvite(p);
 		boolean isLeader = false;
 		if (Managers.getGroupManager().indexOf(p) != -1) {
 			g = Managers.getGroupManager().get(p);
 			isLeader = g.getLeader().equals(p);
 		}
 		
 		messages.add(ChatUtils.formatHeader(_("Party Help Menu")));
 		if (g == null) {
 			if (invite)
 				messages.add(ChatUtils.formatHelp("party accept", _("Accept invite to party.")));
 			else
 				messages.add(ChatColor.GRAY + PartyCommandFrontend.INOINVITE);
 			messages.add(ChatUtils.formatHelp("party create", _("Create a party.")));
			messages.add(ChatColor.AQUA + "PHELPHINT");
 		} else {
 			if (isLeader) {
 				messages.add(ChatUtils.formatHelp("party invite <name>", _("Invite someone to your party.")));
 				messages.add(ChatUtils.formatHelp("party kick <name>", _("Kick someone from your party.")));
 			} else {
 				messages.add(ChatColor.GRAY + "[party invite] " + PartyCommandFrontend.INOTLEADER);
 				messages.add(ChatColor.GRAY + "[party kick] " + PartyCommandFrontend.INOTLEADER);
 			}
 			messages.add(ChatUtils.formatHelp("party leave", _("Leave the party.")));
 			messages.add(ChatUtils.formatHelp("party list", _("List all players in the party.")));
 			if (isLeader)
 				messages.add(ChatUtils.formatHelp("party promote <name>", _("Promote someone in your party to leader.")));
 			else
 				messages.add(ChatColor.GRAY + "[party promote] " + PartyCommandFrontend.INOTLEADER);
 		}
 		
 		for (String s : messages)
 			p.sendMessage(s);
 	}
 	
 	@Override
 	public boolean allowConsole() {
 		return false;
 	}
 	
 	@Override
 	public void noOptionSpecified(CommandSender sender, String[] args) {
 		help(sender, args);
 	}
 	
 }
