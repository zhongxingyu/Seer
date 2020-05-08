 package com.java.phondeux.team;
 
 import java.sql.SQLException;
 import java.util.ArrayList;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 public class TeamCommand implements CommandExecutor {
 	private final Team plugin;
 	
 	public TeamCommand(Team plugin) {
 		this.plugin = plugin;
 	}
 	
 	@Override
 	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
 		// Team commands
 		//      create		- create a team
 		//		disband		- disband a team
 		//		invite		- invite a player to a team (admin or mod only)
 		//		deinvite	- de-invite a player
 		//		description - set team description
 		//		join		- join a team
 		//		leave		- leave a team
 		//		kick		- kick a player from a team
 		//		open		- toggle team open enrollment
 		//		close		- toggle team open enrollment
 		//		promote		- team member -> mod -> owner
 		//		demote		- mod -> team member
 		//		chat		- toggle all chat to be team-only
 		//					  /tc will also be used to team chat for convenience
 		//		help		- a list of the commands and how to use them
 		Player player = (Player)sender;
 		Integer pID = plugin.th.playerGetID(player.getName());
 		Integer pStatus = 0, pTeamID = 0;
 		try {
 			pStatus = plugin.th.playerGetStatus(pID);
 			pTeamID = plugin.th.playerGetTeam(pID);
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		if (args.length > 0) {
 			if (args[0].matches("create")) {
 				if (args.length < 2) {
 					player.sendMessage("No team specified.");
 					return true;
 				}
 				if (pStatus != 0) {
 					player.sendMessage("You are already on a team.");
 					return true;
 				}
 				if (args[1].length() > 8) {
 					player.sendMessage("Team names are limited to 8 characters.");
 					return true;
 				}
 				if (!(args[1].matches("\\w+"))) {
 					player.sendMessage("Team names may be made up of only a-z, A-Z, or 0-9");
 					return true;
 				}
 				if (plugin.th.teamExists(args[1])) {
 					player.sendMessage("A team with that name already exists.");
 					return true;
 				}
 				
 				try {
 					int teamid = plugin.th.teamCreate(args[1]);
 					plugin.th.playerSetTeam(pID, teamid);
 					plugin.th.playerSetStatus(pID, 3);
 					plugin.eh.CreateEvent().TeamCreate(pID, teamid);
					player.setDisplayName(args[1] + " " + player.getName());
 				} catch (SQLException e) {
 					player.sendMessage("Database error.");
 					e.printStackTrace();
 				}
 				
 				return true;
 			}
 			if (args[0].matches("disband")) {
 				if (pStatus != 3) {
 					player.sendMessage("Either you aren't on a team, or you aren't the owner.");
 					return true;
 				}
 				try {
 					plugin.eh.CreateEvent().TeamDisband(pID, pTeamID);
 					plugin.th.teamDelete(pTeamID);
 					ArrayList<String> members = plugin.th.playersGetNameOnTeam(pTeamID);
 					for (String m : members) {
 						plugin.th.playerSetStatus(plugin.th.playerGetID(m), 0);
 						plugin.th.playerSetTeam(plugin.th.playerGetID(m), 0);
 						plugin.getServer().getPlayer(m).setDisplayName(plugin.getServer().getPlayer(m).getPlayerListName());
 					}
 				} catch (SQLException e) {
 					player.sendMessage("Database error.");
 					e.printStackTrace();
 				}
 				return true;
 			}
 			if (args[0].matches("invite")) {
 				if (args.length < 2) {
 					player.sendMessage("No player specified.");
 					return true;
 				}
 				if (pStatus != 3 && pStatus != 2) {
 					player.sendMessage("Either you aren't on a team, or you aren't a mod/owner.");
 					return true;
 				}
 				Integer playerid = plugin.th.playerGetID(args[1]);
 				if (playerid == null) {
 					player.sendMessage("The player " + args[1] + " doesn't exist.");
 					return true;
 				}
 				try {
 					plugin.eh.CreateEvent().PlayerInvite(playerid, pTeamID, pID);
 				} catch (SQLException e) {
 					player.sendMessage("Database error.");
 					e.printStackTrace();
 				}
 				return true;
 			}
 			if (args[0].matches("deinvite")) {
 				if (args.length < 2) {
 					player.sendMessage("No player specified.");
 					return true;
 				}
 				if (pStatus != 3 && pStatus != 2) {
 					player.sendMessage("Either you aren't on a team, or you aren't a mod/owner.");
 					return true;
 				}
 				Integer playerid = plugin.th.playerGetID(args[1]);
 				if (playerid == null) {
 					player.sendMessage("The player " + args[1] + " doesn't exist.");
 					return true;
 				}
 				try {
 					if (!plugin.th.playerIsInvited(playerid, pTeamID)) {
 						player.sendMessage("The player " + args[1] + " isn't invited.");
 						return true;
 					}
 					plugin.eh.CreateEvent().PlayerDeinvite(playerid, pTeamID, pID);
 				} catch (SQLException e) {
 					player.sendMessage("Database error.");
 					e.printStackTrace();
 				}
 				return true;
 			}
 			if (args[0].matches("setmotd")) {
 				if (args.length < 2) {
 					player.sendMessage("No motd specified.");
 					return true;
 				}
 				if (pStatus != 3 && pStatus != 2) {
 					player.sendMessage("Either you aren't on a team, or you aren't a mod/owner.");
 					return true;
 				}
 				String motd = "";
 				for (int i = 1; i < args.length; i++) {
 					motd += args[i];
 					if (i != args.length - 1) motd += " ";
 				}
 				try {
 					plugin.eh.CreateEvent().TeamMotd(pID, pTeamID, motd);
 				} catch (SQLException e) {
 					player.sendMessage("Database error.");
 					e.printStackTrace();
 				}
 				return true;
 			}
 			if (args[0].matches("description")) {
 				if (args.length < 2) {
 					player.sendMessage("No description specified.");
 					return true;
 				}
 				if (pStatus != 3 && pStatus != 2) {
 					player.sendMessage("Either you aren't on a team, or you aren't the owner.");
 					return true;
 				}
 				String descr = "";
 				for (int i = 1; i < args.length; i++) {
 					descr += args[i];
 					if (i != args.length - 1) descr += " ";
 				}
 				try {
 					plugin.th.teamSetDescription(pTeamID, descr);
 				} catch (SQLException e) {
 					player.sendMessage("Database error.");
 					e.printStackTrace();
 				}
 				return true;
 			}
 			if (args[0].matches("join")) {
 				if (args.length < 2) {
 					player.sendMessage("No team specified.");
 					return true;
 				}
 				if (pStatus != 0) {
 					player.sendMessage("You're already on a team.");
 					return true;
 				}
 				Integer teamid = plugin.th.teamGetID(args[1]);
 				if (!plugin.th.teamExists(teamid)) {
 					player.sendMessage("The team " + args[1] + " doesn't exist.");
 					return true;
 				}
 				String teamname = plugin.th.teamGetName(teamid);
 				try {
 					if (plugin.th.teamGetStatus(teamid) == 1 && !plugin.th.playerIsInvited(pID, teamid)) {
 						player.sendMessage(teamname + " is closed.");
 						return true;
 					}
 					plugin.th.playerSetStatus(pID, 1);
 					plugin.th.playerSetTeam(pID, teamid);
 					plugin.eh.CreateEvent().PlayerJoin(pID, teamid);
 					player.setDisplayName(teamname + " " + player.getName());
 				} catch (SQLException e) {
 					player.sendMessage("Database error.");
 					e.printStackTrace();
 				}
 				return true;
 			}
 			if (args[0].matches("leave")) {
 				if (pStatus == 0) {
 					player.sendMessage("Either you aren't on a team, or you aren't the owner.");
 					return true;
 				}
 				String teamname = plugin.th.teamGetName(pTeamID);
 				if (pStatus == 3) {
 					player.sendMessage("You own " + teamname + ", you must disband it.");
 					return true;
 				}
 				try {
 					plugin.eh.CreateEvent().PlayerLeave(pID, pTeamID);
 					plugin.th.playerSetStatus(pID, 0);
 					plugin.th.playerSetTeam(pID, 0);
 
 					player.setDisplayName(player.getPlayerListName());
 				} catch (SQLException e) {
 					player.sendMessage("Database error.");
 					e.printStackTrace();
 				}
 				return true;
 			}
 			if (args[0].matches("kick")) {
 				if (args.length < 2) {
 					player.sendMessage("No player specified.");
 					return true;
 				}
 				if (pStatus != 3 && pStatus != 2) {
 					player.sendMessage("Either you aren't on a team, or you aren't a mod/owner.");
 					return true;
 				}
 				Integer playerid = plugin.th.playerGetID(args[1]);
 				if (playerid == null) {
 					player.sendMessage("The player " + args[1] + " doesn't exist.");
 					return true;
 				}
 				try {
 					if (plugin.th.playerGetTeam(playerid) != pTeamID) {
 						player.sendMessage("The player " + args[1] + " isn't on your team.");
 						return true;
 					}
 					if (plugin.th.playerGetStatus(playerid) >= pStatus) {
 						player.sendMessage("You can't kick " + args[1] + ".");
 					}
 					plugin.eh.CreateEvent().PlayerKicked(playerid, pTeamID, pID);
 					plugin.th.playerSetStatus(playerid, 0);
 					plugin.th.playerSetTeam(playerid, 0);
 				} catch (SQLException e) {
 					player.sendMessage("Database error.");
 					e.printStackTrace();
 				}
 				return true;
 			}
 			if (args[0].matches("open")) {
 				if (pStatus != 3) {
 					player.sendMessage("Either you aren't on a team, or you aren't the owner.");
 					return true;
 				}
 				try {
 					if (plugin.th.teamGetStatus(pTeamID) == 0) {
 						player.sendMessage("Your team is already open.");
 						return true;
 					}
 					plugin.th.teamSetStatus(pTeamID, 0);
 					plugin.eh.CreateEvent().TeamOpen(pID, pTeamID);
 					player.sendMessage("Your team is now open.");
 				} catch (SQLException e) {
 					player.sendMessage("Database error.");
 					e.printStackTrace();
 				}
 				return true;
 			}
 			if (args[0].matches("close")) {
 				if (pStatus != 3) {
 					player.sendMessage("Either you aren't on a team, or you aren't the owner.");
 					return true;
 				}
 				try {
 					if (plugin.th.teamGetStatus(pTeamID) == 1) {
 						player.sendMessage("Your team is already closed.");
 						return true;
 					}
 					plugin.th.teamSetStatus(pTeamID, 1);
 					plugin.eh.CreateEvent().TeamClose(pID, pTeamID);
 					player.sendMessage("Your team is now closed.");
 				} catch (SQLException e) {
 					player.sendMessage("Database error.");
 					e.printStackTrace();
 				}
 				return true;
 			}
 			if (args[0].matches("promote")) {
 				return true;
 			}
 			if (args[0].matches("demote")) {
 				return true;
 			}
 			if (args[0].matches("chat")) {
 				return true;
 			}
 			if (args[0].matches("who")) {
 				try {
 					ArrayList<String> tmp = plugin.th.teamGetList();
 					for (String s : tmp) {
 						String msg = s + ": ";
 						int teamid = plugin.th.teamGetID(s);
 						ArrayList<String> tmp2 = plugin.th.playersGetNameOnTeam(teamid);
 						for (String s2 : tmp2) {
 							msg += s2 + ", ";
 						}
 						msg = msg.substring(0, msg.length() - 2);
 						player.sendMessage(msg);
 					}
 					if (tmp.size() == 0) {
 						player.sendMessage("No teams");
 					}
 				} catch (SQLException e) {
 					e.printStackTrace();
 				}
 				return true;
 			}
 		} else {
 			// Return a simple two/three column list of commands and how to get a full list
 			//    ie /team help #
 			player.sendMessage("Usage: /team [command]");
 			player.sendMessage(ChatColor.RED + "create    disband    kick");
 			player.sendMessage(ChatColor.RED + "invite    open       close");
 			player.sendMessage(ChatColor.RED + "deinvite  promote    demote    setmotd");
 			player.sendMessage(ChatColor.RED + "join      leave      chat      who");
 			player.sendMessage(ChatColor.RED + "help - " + ChatColor.WHITE + "for details on each");
 			return true;
 		}
 		return true;
 	}
 }
