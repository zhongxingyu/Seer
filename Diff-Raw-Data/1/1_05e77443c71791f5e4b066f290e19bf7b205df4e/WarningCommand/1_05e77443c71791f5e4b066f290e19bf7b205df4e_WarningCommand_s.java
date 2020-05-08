 package com.wolvencraft.prison.mines.cmd;
 
 import org.bukkit.ChatColor;
 
 import com.wolvencraft.prison.mines.PrisonMine;
 import com.wolvencraft.prison.mines.mine.Mine;
 import com.wolvencraft.prison.mines.util.Message;
 import com.wolvencraft.prison.mines.util.Util;
 

 public class WarningCommand  implements BaseCommand {
 	
 	@Override
 	public boolean run(String[] args) {
 		
 		if(args.length == 1) { getHelp(); return true; }
 
 		Mine curMine = PrisonMine.getCurMine();
 		
 		if(args[1].equalsIgnoreCase("add") || args[1].equalsIgnoreCase("+")) {
 			if(args.length != 3) { Message.sendFormattedError(PrisonMine.getLanguage().ERROR_ARGUMENTS); return false; }
 			
 			int time = Util.parseTime(args[2]);
 			if(time <= 0) { Message.sendFormattedError("Invalid time provided"); return false; }
 			if(time > curMine.getResetPeriod()) { Message.sendFormattedError("Time cannot be set to a value greater then the reset time"); return false; }
 
 			String parsedTime = Util.parseSeconds(time);
 			if(curMine.hasWarningTime(time)) { Message.sendFormattedError("Mine already sends a warning at " + ChatColor.GOLD + parsedTime, false); return false; }
 			curMine.addWarningTime(time);
 			Message.sendFormattedMine("Mine will now send warnings at " + ChatColor.GOLD + parsedTime);
 		} else if(args[2].equalsIgnoreCase("remove") || args[2].equalsIgnoreCase("-")) {
 			if(args.length != 3) { Message.sendFormattedError(PrisonMine.getLanguage().ERROR_ARGUMENTS); return false; }
 			
 			int time = Util.parseTime(args[3]);
 			if(time <= 0) { Message.sendFormattedError("Invalid time provided"); return false; }
 			
 			String parsedTime = Util.parseSeconds(time);
 			if(!curMine.hasWarningTime(time)) { Message.sendFormattedError("Mine does not send a warning at " + ChatColor.GOLD + parsedTime, false); return false; }
 			curMine.removeWarningTime(time);
 			
 			Message.sendFormattedMine("Mine will no longer send a warning at " + ChatColor.GOLD + parsedTime);
 		}
 		else { Message.sendFormattedError(PrisonMine.getLanguage().ERROR_COMMAND); return false; }
 		
 		return curMine.saveFile();
 	}
 	
 	@Override
 	public void getHelp() {
 		Message.formatHeader(20, "Timer");
 		Message.formatHelp("warning", "add <time>", "Adds a warning at time specified");
 		Message.formatHelp("warning", "remove <time>", "Adds a warning at time specified");
 		return;
 	}
 	
 	@Override
 	public void getHelpLine() { Message.formatHelp("warning", "", "Shows reset warning options", "prison.mine.edit"); }
 }
