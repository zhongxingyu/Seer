 package com.martinbrook.tesseractuhc.command;
 
 import org.bukkit.entity.Player;
 
 import com.martinbrook.tesseractuhc.TesseractUHC;
 import com.martinbrook.tesseractuhc.UhcParticipant;
 import com.martinbrook.tesseractuhc.UhcSpectator;
 
 public class TpnextCommand extends UhcCommandExecutor {
 
 	public TpnextCommand(TesseractUHC plugin) {
 		super(plugin);
 	}
 
 	@Override
 	protected String runAsAdmin(UhcSpectator sender, String[] args) {
 		return runAsSpectator(sender,args);
 	}
 
 	@Override
 	protected String runAsSpectator(UhcSpectator sender, String[] args) {
		Player to = null;
 		int cycleSize = match.countParticipantsInMatch();
 		int attempts = 0;
 		
 		do {
 			UhcParticipant up = match.getParticipantByIndex(sender.nextCyclePoint(cycleSize));
			if (up == null) break;
 			to = server.getPlayerExact(up.getName());
 			attempts++;
 		} while ((to == null || !to.isOnline()) && attempts < cycleSize);
 		
 		if (to == null || !to.isOnline())
 			return ERROR_COLOR + "No player found";
 		
 		sender.teleport(to, OK_COLOR + "Teleported to " + to.getName());
 			
 		return null;
 
 	}
 
 }
