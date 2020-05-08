 package com.wolvencraft.prison.mines.cmd;
 
 import java.util.List;
 
 import org.bukkit.ChatColor;
 
 import com.wolvencraft.prison.mines.PrisonMine;
 import com.wolvencraft.prison.mines.mine.Mine;
 import com.wolvencraft.prison.mines.util.Message;
 import com.wolvencraft.prison.mines.util.Util;
 
 
 public class WarningCommand  implements BaseCommand {
 	public boolean run(String[] args) {
 		
 		if(args.length == 1) {
 			getHelp();
 			return true;
 		}
 
 		Mine curMine = PrisonMine.getCurMine();
 		if(curMine == null) {
 			Message.sendFormattedError(PrisonMine.getLanguage().ERROR_MINENOTSELECTED);
 			return false;
 		}
 		
 		if(args[1].equalsIgnoreCase("toggle")) {
 			if(args.length != 2) {
 				Message.sendFormattedError(PrisonMine.getLanguage().ERROR_ARGUMENTS);
 				return false;
 			}
 			
 			if(curMine.getWarned()) {
 				curMine.setWarned(false);
 				Message.sendFormattedMine("Reset warnings are " + ChatColor.RED + "off");
 			}
 			else {
 				curMine.setWarned(true);
 				Message.sendFormattedMine("Reset warnings are " + ChatColor.GREEN + "on");
 			}
 		} else if(args[1].equalsIgnoreCase("add") || args[1].equalsIgnoreCase("+")) {
 			if(args.length != 3) {
 				Message.sendFormattedError(PrisonMine.getLanguage().ERROR_ARGUMENTS);
 				return false;
 			}
 			
 			int time = Util.parseTime(args[2]);
 			if(time <= 0) {
 				Message.sendFormattedError("Invalid time provided");
 				return false;
 			}
 			if(time > curMine.getResetPeriod()) {
 				Message.sendFormattedError("Time cannot be set to a value greater then the reset time");
 				return false;
 			}
 			
 			List<Integer> warnList = curMine.getWarningTimes();
 			warnList.add(time);
 			String parsedTime = Util.parseSeconds(time);
 			Message.sendFormattedMine("Mine will now send warnings " + ChatColor.GOLD + parsedTime + ChatColor.WHITE + " minute(s) before the reset");
 		} else if(args[2].equalsIgnoreCase("remove") || args[2].equalsIgnoreCase("-")) {
			if(args.length != 4) {
 				Message.sendFormattedError(PrisonMine.getLanguage().ERROR_ARGUMENTS);
 				return false;
 			}
 			
 			int time = Util.parseTime(args[3]);
 			if(time <= 0) {
 				Message.sendFormattedError("Invalid time provided");
 				return false;
 			}
 			
 			List<Integer> warnList = curMine.getWarningTimes();
 			int index = warnList.indexOf(time);
 			if(index == -1) {
 				Message.sendFormattedError("'" + curMine.getId() + "' does not send a warning " + ChatColor.GOLD + Util.parseSeconds(time) + ChatColor.WHITE + " minute(s) before the reset");
 				return false;
 			}
 			
 			warnList.remove(index);
 			Message.sendFormattedMine("Mine will no longer send a warning " + ChatColor.GOLD + Util.parseSeconds(time) + ChatColor.WHITE + " minute(s) before the reset");
 		}
 		else {
 			Message.sendFormattedError(PrisonMine.getLanguage().ERROR_COMMAND);
 			return false;
 		}
 		
 		return curMine.saveFile();
 	}
 
 	public void getHelp() {
 		Message.formatHeader(20, "Timer");
 		Message.formatHelp("warning", "toggle", "Toggles reset warnings on and off");
 		Message.formatHelp("warning", "add <time>", "Adds a warning at time specified");
 		Message.formatHelp("warning", "remove <time>", "Adds a warning at time specified");
 		return;
 	}
 	
 	public void getHelpLine() { Message.formatHelp("warning", "", "Shows reset warning options", "prison.mine.edit"); }
 }
