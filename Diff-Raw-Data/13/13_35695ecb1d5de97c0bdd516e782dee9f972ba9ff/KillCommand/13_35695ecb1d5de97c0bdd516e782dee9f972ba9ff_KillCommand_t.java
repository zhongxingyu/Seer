 package com.martinbrook.tesseractuhc.command;
 
import org.bukkit.command.ConsoleCommandSender;
 import org.bukkit.entity.Player;
 
 import com.martinbrook.tesseractuhc.TesseractUHC;
 import com.martinbrook.tesseractuhc.UhcSpectator;
 
 public class KillCommand extends UhcCommandExecutor {
 
 	public KillCommand(TesseractUHC plugin) {
 		super(plugin);
 	}
 
 	@Override
 	protected String runAsAdmin(UhcSpectator sender, String[] args) {
		return run(args);
	}
	
	@Override
	protected String runAsConsole(ConsoleCommandSender sender, String[] args) {
		return run(args);
	}
	
	protected String run(String[] args) {
 		
 		if (args.length < 1) return ERROR_COLOR + "Please specify the player to kill";
 		
 		Player p = server.getPlayer(args[0]);
 		
 		if (p == null || !p.isOnline())
 			return ERROR_COLOR + "Player " + args[0] + " not found online.";
 
 		p.setHealth(0);
 		match.broadcast(DECISION_COLOR + p.getDisplayName() + " has been killed");
 		
 		return null;
 	}
 

 }
