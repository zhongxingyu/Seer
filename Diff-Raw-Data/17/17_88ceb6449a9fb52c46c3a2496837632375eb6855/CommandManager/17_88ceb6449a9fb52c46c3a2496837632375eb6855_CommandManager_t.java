 package com.olympuspvp.squads;
 
 import java.util.*;
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
 
 public class CommandManager{
 
 	olySquads squad;
 	String squads = ChatColor.GOLD + "[" + ChatColor.YELLOW + "olySquads" + ChatColor.GOLD + "] " + ChatColor.GRAY;
 
 	protected CommandManager(olySquads squad){
 		this.squad = squad;
 	}
 
 	protected void squadCommand(CommandSender s, String[] args){
 		if(!(s instanceof Player)) return;
 		Player p = (Player) s;
 		if(args.length == 0) sendHelp(p);
 		else if(args[0].equalsIgnoreCase("create")) createSquadCommand(p, args);
 		else if(args[0].equalsIgnoreCase("delete")) deleteSquadCommand(p);
 		else if(args[0].equalsIgnoreCase("leave")) leaveSquadCommand(p);
 		else if(args[0].equalsIgnoreCase("invite")) invitePlayerCommand(p, args);
 		else if(args[0].equalsIgnoreCase("accept")) acceptInviteCommand(p);
 		else if(args[0].equalsIgnoreCase("sethome")) setHomeCommand(p);
 		else if(args[0].equalsIgnoreCase("setrally")) setRallyCommand(p);
 		else if(args[0].equalsIgnoreCase("rally")) rallyCommand(p);
 		else if(args[0].equalsIgnoreCase("home")) homeCommand(p);
 		else if(args[0].equalsIgnoreCase("playerinfo")) playerInfoCommand(p, args);
 		else if(args[0].equalsIgnoreCase("pinfo")) playerInfoCommand(p, args);
 		else if(args[0].equalsIgnoreCase("squadInfo")) squadInfoCommand(p, args);
 		else if(args[0].equalsIgnoreCase("sInfo")) squadInfoCommand(p, args);
 		else if(args[0].equalsIgnoreCase("list")) listCommand(p);
 		else if(args[0].equalsIgnoreCase("say") || args[0].equalsIgnoreCase("s")) squadcastCommand(p, args);
 		else if(args[0].equalsIgnoreCase("help")) sendHelp(p);
 		else sendHelp(p);
 	}
 
 	public void sendHelp(Player p){
 		p.sendMessage(squads + "Usage for olySquad's /squad command:");
 		p.sendMessage(ChatColor.GRAY + "/s create [name]" + ChatColor.DARK_GRAY + " - " + ChatColor.GRAY + "Creates a new Squad");
 		p.sendMessage(ChatColor.GRAY + "/s delete" + ChatColor.DARK_GRAY + " - " + ChatColor.GRAY + "Deletes your squad");
 		p.sendMessage(ChatColor.GRAY + "/s invite [player]" + ChatColor.DARK_GRAY + " - " + ChatColor.GRAY + "Invites a player to your squad");
 		p.sendMessage(ChatColor.GRAY + "/s accept" + ChatColor.DARK_GRAY + " - " + ChatColor.GRAY + "Accepts your active invite");
 		p.sendMessage(ChatColor.GRAY + "/s sethome" + ChatColor.DARK_GRAY + " - " + ChatColor.GRAY + "Sets the squad home to your location");
 		p.sendMessage(ChatColor.GRAY + "/s home" + ChatColor.DARK_GRAY + " - " + ChatColor.GRAY + "Teleports you to your squad's home");
 		p.sendMessage(ChatColor.GRAY + "/s setrally" + ChatColor.DARK_GRAY + " - " + ChatColor.GRAY + "Sets the squad rally to your location");
 		p.sendMessage(ChatColor.GRAY + "/s rally" + ChatColor.DARK_GRAY + " - " + ChatColor.GRAY + "Teleports you to your squad's rally");
 		p.sendMessage(ChatColor.GRAY + "/s [playerinfo/pinfo] [name]" + ChatColor.DARK_GRAY + " - " + ChatColor.GRAY + "Gives a player's squad");
 		p.sendMessage(ChatColor.GRAY + "/s [squadinfo/sinfo] [name]" + ChatColor.DARK_GRAY + " - " + ChatColor.GRAY + "Gives squad data");
 		p.sendMessage(ChatColor.GRAY + "/s [say/s] [message]" + ChatColor.DARK_GRAY + " - " + ChatColor.GRAY + "Sends a message to all members");
 	}
 
 	public boolean playerIsInSquad(Player p, boolean message){
 		boolean test = true;
 		if(squad.getPlayerSquad(p.getName()) == null) test = false;
 		if(!test && message) p.sendMessage(squads + "You have to be in a squad to do that.");
 		return test;
 	}
 
 	public boolean playerIsSquadOwner(Player p, boolean message){
 		Squad s = squad.getPlayerSquad(p.getName());
 		boolean test = s.getOwner().equals(p.getName());
 		if(!test) p.sendMessage(squads + "You have to be the owner of the squad to do that.");
 		return test;
 	}
 
 
 
 	/*  /s create [name]   */
 	protected void createSquadCommand(Player p, String[] args){
 		if(playerIsInSquad(p, false)){
 			p.sendMessage(squads + "You have to leave your current squad to do that.");
 			return;
 		}
 		if(args.length == 2){
 			String name = args[1];
 			if(name.length() > 10){
 				p.sendMessage(squads + "A squad name must be less than ten letters.");
 				return;
 			}if(squad.getConfig().getConfigurationSection("Squads") != null && squad.getConfig().getConfigurationSection("Squads").contains(name)){
 				p.sendMessage(squads + "That squad already exists.");
 				return;
 			}
 			Squad s = new Squad(name, p.getName(), new ArrayList<String>(), null, null, squad);
 			s.addMember(p.getName());
 			s.save();
 			squad.loadSquad(name, s);
 			squad.setPlayerSquad(p.getName(), name);
 			Bukkit.getServer().broadcastMessage(squads + ChatColor.DARK_GRAY + p.getName() + ChatColor.GRAY + " established " + ChatColor.DARK_GRAY + name);
 			p.sendMessage(squads + "Invite people to it using " + ChatColor.DARK_GRAY + "/s invite [name]");
 		}else p.sendMessage(squads + "Incorrect Usage. /s create [name]");
 	}
 
 	/*  /s delete      */
 	private List<String> deleteRequests = new ArrayList<String>();
 	protected void deleteSquadCommand(Player p){
 		if(!playerIsInSquad(p, true)) return;
 		if(!playerIsSquadOwner(p, true)) return;
 		if(!deleteRequests.contains(p.getName())){
 			p.sendMessage(squads + "This operation cannot be undone.");
 			p.sendMessage(squads + "Are you sure?");
 			p.sendMessage(squads + "Retype the command to confirm.");
 			deleteRequests.add(p.getName());
 		}else{
 			Squad s = squad.getPlayerSquad(p.getName());
 			s.squadcast("!", "The squad has been disbanded.", false);
 			s.delete();
 			deleteRequests.remove(p.getName());
 		}
 	}
 
 	private void leaveSquadCommand(Player p){
 		if(!playerIsInSquad(p, true)) return;
 		if(playerIsSquadOwner(p, false)){
 			p.sendMessage(squads + "You cannot leave the squad is you are its owner.");
 			p.sendMessage(squads + "You can delete it with " + ChatColor.DARK_GRAY + "/s delete");
 			return;
 		}Squad s = squad.getPlayerSquad(p.getName());
 		s.squadcast("olySquad", ChatColor.DARK_GRAY + p.getName() + ChatColor.GRAY + " has left the squad.", false);
 		s.removeMember(p.getName());
 		s.save();
 		squad.setPlayerSquad(p.getName(), null, true);
 	}
 
 	/*  /s invite [name]   */
 	private HashMap<String, String> invites = new HashMap<String, String>();
 	protected void invitePlayerCommand(Player p, String[] args){
 		if(!playerIsInSquad(p, true)) return;
 		if(!playerIsSquadOwner(p, true)) return;
 		if(args.length != 2){
 			p.sendMessage(squads + "Incorrect Usage. /s invite [name]");
 			return;
 		}List<Player> matches = Bukkit.matchPlayer(args[1]); 
 		if(matches.size() < 1){
 			p.sendMessage(squads + "Found multiple people with that in their name. Be more specific.");
 			return;
 		}if(matches.size() == 0){
 			p.sendMessage(squads + "Could not find that player.");
 			return;
 		}Player match = matches.get(0);
 		Squad s = squad.getPlayerSquad(p.getName());
 		match.sendMessage(squads + ChatColor.DARK_GRAY + p.getName() + ChatColor.GRAY + " has invited you to join " + ChatColor.DARK_GRAY + s.getName());
 		match.sendMessage(squads + ChatColor.DARK_GRAY + "/s accept" + ChatColor.GRAY + " to accept the invite.");
 		invites.put(match.getName(), s.getName());
 		s.squadcast("!", ChatColor.DARK_GRAY + match.getName() + ChatColor.GRAY + " has been invited.", false);
 	}
 
 	/*   /s accept   */
 	protected void acceptInviteCommand(Player p){
 		if(!invites.containsKey(p.getName())){
 			p.sendMessage(squads + "You do not have a pending request.");
 			return;
 		}if(playerIsInSquad(p,false)){
 			p.sendMessage(squads + "You must leave your current squad to join another.");
 			return;
 		}String squadName = invites.get(p.getName());
 		invites.remove(p.getName());
 		Squad s = squad.getSquad(squadName);
 		s.addMember(p.getName());
 		s.save();
 		squad.loadSquad(s.getName(), s);
 		squad.setPlayerSquad(p.getName(), squadName, true);
		s.squadcast("!", ChatColor.DARK_GRAY + p.getName() + ChatColor.GRAY + " has joined the squad.", false);
 	}
 
 	/*   /s sethome   */
 	protected void setHomeCommand(Player p){
 		if(!playerIsInSquad(p, true)) return;
 		if(!playerIsSquadOwner(p, true)) return;
 		Squad s = squad.getPlayerSquad(p.getName());
 		s.setHome(p.getLocation());
 		s.save();
 		s.squadcast("!", ChatColor.DARK_GRAY + p.getName() + ChatColor.GRAY + " set the squad home.", false);
 		s.squadcast("!", ChatColor.DARK_GRAY + "/s home" + ChatColor.GRAY + " to go there.", false);
 	}
 
 	/*   /s setrally  */
 	protected void setRallyCommand(Player p){
 		if(!playerIsInSquad(p, true)) return;
 		Squad s = squad.getPlayerSquad(p.getName());
 		s.setRally(p.getLocation());
 		s.save();
 		s.squadcast("!", ChatColor.DARK_GRAY + p.getName() + ChatColor.GRAY + " set the squad's rally point.", false);
 		s.squadcast("!", ChatColor.DARK_GRAY + "/s rally" + ChatColor.GRAY + " to go there.", false);
 	}
 
 	/*   /s home   */
 	protected void homeCommand(Player p){
 		if(!playerIsInSquad(p, true)) return;
 		Squad s = squad.getPlayerSquad(p.getName());
 		Location home = s.getHome();
 		if(home == null){
 			p.sendMessage(squads + "There is no home set.");
 			return;
 		}
		p.teleport(home, TeleportCause.COMMAND);
 		p.sendMessage(squads + "You have been teleported to your squads's home");
 	}
 
 	/*   /s home   */
 	protected void rallyCommand(Player p){
 		if(!playerIsInSquad(p, true)) return;
 		Squad s = squad.getPlayerSquad(p.getName());
 		Location rally = s.getRally();
 		if(rally == null){
 			p.sendMessage(squads + "There is no rally point set.");
 			return;
 		}
		p.teleport(rally, TeleportCause.COMMAND);
 		p.sendMessage(squads + "You have been teleported to your squad's rally point");
 	}
 
 	protected void playerInfoCommand(Player p, String[] args){
 		String name;
 		if(args.length != 2){
 			name = p.getName();
 		}else name = args[1];
 		Squad s = squad.getPlayerSquad(name);
 		if(s == null) p.sendMessage(squads + ChatColor.GRAY + args[1] + ChatColor.GRAY + " is not currently in a squad.");
 		else p.sendMessage(squads + ChatColor.GRAY + args[1] + ChatColor.GRAY + " is in the squad " + ChatColor.DARK_GRAY + s.getName());
 	}
 
 	protected void listCommand(Player p){
 		Object[] squadsCreated = squad.getConfig().getConfigurationSection("Squads").getKeys(false).toArray();
 		StringBuilder sb = new StringBuilder();
 		for(Object s : squadsCreated){
 			sb.append(ChatColor.GRAY + s.toString());
 			if(!s.equals(squadsCreated[squadsCreated.length - 1])) sb.append(ChatColor.DARK_GRAY + ", ");
 		}p.sendMessage(squads + "Squads created on " + ChatColor.GOLD + "OlympusPVP" + ChatColor.GRAY + ":");
 		p.sendMessage(sb.toString());
 	}
 
 	protected void squadInfoCommand(Player p, String[] args){
 		Squad s;
 		if(args.length != 2){
 			s = squad.getPlayerSquad(p.getName());
 		}else{
 			Set<String> squadsCreated = squad.getConfig().getConfigurationSection("Squads").getKeys(false);
 			if(!squadsCreated.contains(args[1])){
 				p.sendMessage(squads + "That squad does not exist.");
 				return;
 			}s = squad.getSquad(args[1]);
 		}
 		p.sendMessage(squads + "Information for " + ChatColor.DARK_GRAY + s.getName() + ChatColor.GRAY + ":");
 		p.sendMessage(squads + "Created by " + ChatColor.DARK_GRAY + s.getOwner());
 		Object[] members = s.getMembers().toArray();
 		StringBuilder sb = new StringBuilder();
 		for(Object str : members){
 			sb.append(ChatColor.DARK_GRAY + str.toString());
 			if(!str.equals(members[members.length - 1])) sb.append(ChatColor.GRAY + ", ");
 		}
 		p.sendMessage(squads + "Members: " + sb.toString());
 		if(s.contains(p.getName())){
 			Location home = s.getHome();
 			Location rally = s.getRally();
 			if(home != null) p.sendMessage(squads + ChatColor.DARK_GRAY + "Home: " + ChatColor.GRAY + home.getBlockX() + ", " + home.getBlockY() + ", " + home.getBlockZ() + " in " + home.getWorld().getName());
 			else p.sendMessage(squads + "No home has been set.");
 			if(rally != null)p.sendMessage(squads + ChatColor.DARK_GRAY + "Rally: " + ChatColor.GRAY + rally.getBlockX() + ", " + rally.getBlockY() + ", " + rally.getBlockZ() + " in " + rally.getWorld().getName());
 			else p.sendMessage(squads + "No rally point has been set.");
 		}
 	}
 
 	protected void squadcastCommand(Player p, String[] args){
 		if(!playerIsInSquad(p, true)) return;
 		if(args.length <= 1){
 			p.sendMessage(squads + "You must type a message.");
 			return;
 		}StringBuilder sb = new StringBuilder();
 		for(int i = 1; i < args.length; i++){
 			sb.append(args[i]);
 			sb.append(" ");
 		}Squad s = squad.getPlayerSquad(p.getName());
 		s.squadcast(p.getName(), sb.toString(), true);
 	}
 
 }
