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
 				if (pStatus != 0) {
 					player.sendMessage("You are already on a team.");
 					return true;
 				}
 				if (args[1].length() > 8) {
 					player.sendMessage("Team names are limited to 8 characters.");
 					return true;
 				}
				if (!(args[1].matches("\\w"))) {
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
 					player.sendMessage("Team " + args[1] + " created successfully!");
 					plugin.eh.CreateEvent().TeamCreate(pID, teamid);
 				} catch (SQLException e) {
 					player.sendMessage("Database error.");
 					e.printStackTrace();
 				}
 				
 				return true;
 			}
 			if (args[0].matches("disband")) {
 				if (pStatus != 3) {
 					player.sendMessage("Either you aren't on a team, or you are not the owner.");
 					return true;
 				}
 				try {
 					plugin.th.teamDelete(pTeamID);
 					ArrayList<String> members = plugin.th.playersGetNameOnTeam(pTeamID);
 					for (String m : members) {
 						plugin.th.playerSetStatus(plugin.th.playerGetID(m), 0);
 						plugin.th.playerSetTeam(plugin.th.playerGetID(m), 0);
 						if (plugin.getServer().getPlayer(m) != null) {
 							plugin.getServer().getPlayer(m).sendMessage("Your team has been disbanded.");
 						}
 					}
 					plugin.th.playerSetStatus(pID, 0);
 					plugin.eh.CreateEvent().TeamDisband(pID, pTeamID);
 				} catch (SQLException e) {
 					player.sendMessage("Database error.");
 					e.printStackTrace();
 				}
 				return true;
 			}
 			if (args[0].matches("invite")) {
 				return true;
 			}
 			if (args[0].matches("deinvite")) {
 				return true;
 			}
 			if (args[0].matches("description")) {
 				return true;
 			}
 			if (args[0].matches("join")) {
 				if (pStatus != 0) {
 					player.sendMessage("You're already on a team.");
 					return true;
 				}
 				Integer teamid = plugin.th.teamGetID(args[1]);
 				if (!plugin.th.teamExists(teamid)) {
 					player.sendMessage("The team " + args[1] + " doesn't exist.");
 					return true;
 				}
 				try {
 					if (plugin.th.teamGetStatus(teamid) == 1) {
 						player.sendMessage(args[1] + " is closed.");
 						return true;
 					}
 					plugin.th.playerSetStatus(pID, 1);
 					plugin.th.playerSetTeam(pID, teamid);
 					plugin.eh.CreateEvent().PlayerJoin(pID, teamid);
 					player.sendMessage("You've joined " + args[1] + ".");
 				} catch (SQLException e) {
 					player.sendMessage("Database error.");
 					e.printStackTrace();
 				}
 				return true;
 			}
 			if (args[0].matches("leave")) {
 				if (pStatus == 0) {
 					player.sendMessage("You aren't on a team.");
 					return true;
 				}
 				if (pStatus == 3) {
 					player.sendMessage("You own your team, you must disband it.");
 					return true;
 				}
 				try {
 					plugin.th.playerSetStatus(pID, 0);
 					plugin.th.playerSetTeam(pID, 0);
 					plugin.eh.CreateEvent().PlayerLeave(pID, pTeamID);
 					player.sendMessage("You've left your team.");
 				} catch (SQLException e) {
 					player.sendMessage("Database error.");
 					e.printStackTrace();
 				}
 				return true;
 			}
 			if (args[0].matches("kick")) {
 				return true;
 			}
 			if (args[0].matches("open")) {
 				if (pStatus != 3) {
 					player.sendMessage("Either you aren't on a team, or you are not the owner.");
 					return true;
 				}
 				try {
 					if (plugin.th.teamGetStatus(pTeamID) == 0) {
 						player.sendMessage("Your team is already open.");
 						return true;
 					}
 					plugin.th.teamSetStatus(pTeamID, 0);
 					plugin.eh.CreateEvent().TeamOpen(pID, pTeamID);
 				} catch (SQLException e) {
 					player.sendMessage("Database error.");
 					e.printStackTrace();
 				}
 				return true;
 			}
 			if (args[0].matches("close")) {
 				if (pStatus != 3) {
 					player.sendMessage("Either you aren't on a team, or you are not the owner.");
 					return true;
 				}
 				try {
 					if (plugin.th.teamGetStatus(pTeamID) == 1) {
 						player.sendMessage("Your team is already closed.");
 						return true;
 					}
 					plugin.th.teamSetStatus(pTeamID, 1);
 					plugin.eh.CreateEvent().TeamClose(pID, pTeamID);
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
 			if (args[0].matches("help")) {
 				try {
 					ArrayList<String> tmp = plugin.th.teamGetList();
 					for (String s : tmp) {
 						String msg = s + ": ";
 						int teamid = plugin.th.teamGetID(s);
 						ArrayList<String> tmp2 = plugin.th.playersGetNameOnTeam(teamid);
 						for (String s2 : tmp2) {
 							msg += s2 + ", ";
 						}
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
 			player.sendMessage("Using /team");
 			player.sendMessage(ChatColor.RED + "/team create" + ChatColor.WHITE + " - Creates a team");
 			return true;
 		}
 		return true;
 	}
 }
