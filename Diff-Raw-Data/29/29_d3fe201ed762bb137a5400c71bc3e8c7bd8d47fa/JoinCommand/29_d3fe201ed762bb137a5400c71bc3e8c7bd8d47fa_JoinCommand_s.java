 package com.martinbrook.tesseractuhc.command;
 
 import com.martinbrook.tesseractuhc.MatchPhase;
 import com.martinbrook.tesseractuhc.TesseractUHC;
 import com.martinbrook.tesseractuhc.UhcPlayer;
 import com.martinbrook.tesseractuhc.UhcSpectator;
 import com.martinbrook.tesseractuhc.UhcTeam;
 
 public class JoinCommand extends UhcCommandExecutor {
 
 	public JoinCommand(TesseractUHC plugin) {
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
 		if (match.getMatchPhase() != MatchPhase.PRE_MATCH)
 			return ERROR_COLOR + "The match is already underway. You cannot join.";
 		
 		if (match.isFFA()) {
 			if (match.addSoloParticipant(sender)) {
 				return OK_COLOR + "You have joined the match";
 			} else {
 				return ERROR_COLOR + "Unable to join";
 			}
 		}
 		
 		UhcTeam teamToJoin = null;
 				
 		if(args.length > 0) teamToJoin = match.getTeam(args[0]);
 			
 		if (teamToJoin == null) {
 			String response = ERROR_COLOR + "Please specify a team to join. Available teams:\n";
 			for (UhcTeam t : match.getTeams()) {
 				response += t.getIdentifier() + ": " + t.getName() + "\n";
 			}
 			return response;
 		}
 		
 		if (match.addParticipant(sender, teamToJoin.getIdentifier())){
			match.broadcastTeam(OK_COLOR +sender.getName() + "has joined "+teamToJoin.getName(), teamToJoin);
 			match.adminBroadcast(OK_COLOR +sender.getName() + "has joined "+teamToJoin.getName());
			return OK_COLOR + "You are now a member of " + teamToJoin.getName();
 		}else
 			return ERROR_COLOR + "Unable to join team. Are you already on a team?";
 	}
 
 	
 }
