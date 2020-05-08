 package com.martinbrook.tesseractuhc.command;
 
 import org.bukkit.ChatColor;
 
 import com.martinbrook.tesseractuhc.MatchPhase;
 import com.martinbrook.tesseractuhc.TesseractUHC;
 import com.martinbrook.tesseractuhc.UhcPlayer;
 import com.martinbrook.tesseractuhc.UhcSpectator;
 import com.martinbrook.tesseractuhc.UhcTeam;
 
 public class TeamCommand extends UhcCommandExecutor {
 
 	public TeamCommand(TesseractUHC plugin) {
 		super(plugin);
 	}
 
 	@Override
 	protected String runAsAdmin(UhcSpectator sender, String[] args) {
 		return ERROR_COLOR + "Admins cannot join the match. Please use /deop first.";
 	}
 	
 	@Override
 	protected String runAsSpectator(UhcSpectator sender, String[] args) {
 		return this.runAsPlayer(sender.getPlayer(), args);
 	}
 
 	@Override
 	protected String runAsPlayer(UhcPlayer sender, String[] args) {
 		if (sender.isParticipant())
 			return ERROR_COLOR + "You have already joined this match. Please /leave before creating a new team.";
 		
 		if (match.getMatchPhase() != MatchPhase.PRE_MATCH)
 			return ERROR_COLOR + "The match is already underway. You cannot create a team.";
 		
 		if (match.isFFA())
 			return ERROR_COLOR + "This is a FFA match. There are no teams.";
 		
 		if (!match.roomForAnotherTeam())
 			return ERROR_COLOR + "There are no more team slots left.";
 		
 		if (args.length < 1)
 			return ERROR_COLOR + "Syntax: /team identifier [full name]";
 		
 		String identifier = args[0].toLowerCase();
 		String name = "";
 		
 		if (args.length < 2)
 			name = identifier;
 		else {
 			for (int i = 1; i < args.length; i++) name += args[i] + " ";
 			name = name.substring(0,name.length()-1);
 		}
 		for(UhcTeam team : match.getTeams()){
			if(team.getIdentifier().equals(identifier))
 				return ERROR_COLOR +"You can't use an identifier that another team has used.";
			if(team.getName().equals(name))
 				return ERROR_COLOR +"You can't use a name that another team is using.";
 		}
 			
 		if (!match.addTeam(identifier, name))
 			return ERROR_COLOR + "Could not add a new team. Use /join to join an existing team.";
 		
 		if (!match.addParticipant(sender, identifier))
 			return ERROR_COLOR + "An error occurred. The team has been created but you could not be joined to it.";
 		
		
 		match.broadcast(ChatColor.GOLD + "Team " + ChatColor.AQUA + ChatColor.ITALIC + name + ChatColor.RESET 
 				+ ChatColor.GOLD + " was created by " + sender.getDisplayName() + ".\n"
 				+ "To join, type " + ChatColor.AQUA + ChatColor.ITALIC + "/join " + identifier);
 		
 		return OK_COLOR + "You have created the team: " + name;
 		
 
 	}
 
 }
