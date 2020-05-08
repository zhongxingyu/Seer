 package com.martinbrook.tesseractuhc.command;
 
 import org.bukkit.command.ConsoleCommandSender;
 
 import com.martinbrook.tesseractuhc.TesseractUHC;
 import com.martinbrook.tesseractuhc.UhcSpectator;
 import com.martinbrook.tesseractuhc.util.MatchUtils;
 
 public class PvpCommand extends UhcCommandExecutor {
 
 	public PvpCommand(TesseractUHC tesseractUHC) {
 		super(tesseractUHC);
 	}
 
 
 
 	@Override
 	protected String runAsAdmin(UhcSpectator sender, String[] args) {
 		return run(args);
 	}
 	
 	@Override
 	protected String runAsConsole(ConsoleCommandSender sender, String[] args) {
 		return run(args);
 	}
 	
 	protected String run(String[] args){
 		if(args.length<1){
 			return ERROR_COLOR + "Syntax: /pvp [on | off]";
 		}
		
		Boolean value = MatchUtils.stringToBoolean(args[0]);
		if (value == null)
			return ERROR_COLOR + "Syntax: /pvp [on | off]";
		
		match.setPVP(value);
		return OK_COLOR +"PVP is now " + (value? "on" : "off");
 		
 	
 	}
 
 }
