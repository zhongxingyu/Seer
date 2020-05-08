 package com.wolvencraft.prison.mines.cmd;
 
 import com.wolvencraft.prison.mines.CommandManager;
 import com.wolvencraft.prison.mines.PrisonMine;
 import com.wolvencraft.prison.mines.mine.Mine;
 import com.wolvencraft.prison.mines.util.ExtensionLoader;
 import com.wolvencraft.prison.mines.util.Message;
 import com.wolvencraft.prison.mines.util.Util;
 
 public class ResetCommand implements BaseCommand {	
 	public boolean run(String[] args) {
 		
 		Mine curMine = null;
 		String generator = "";
 		if(args.length == 1) {
 			getHelp();
 			return true;
 		} else if(args.length == 2) {
 			if(args[1].equalsIgnoreCase("all")) {
 				boolean success = true;
 				for(Mine mine : PrisonMine.getLocalMines()) {
 					if(!CommandManager.RESET.run(mine.getId())) success = false;
 				}
 				return success;
 			} else curMine = Mine.get(args[1]);
 		} else if(args.length == 3) {
 			curMine = Mine.get(args[1]);
 			generator = args[2];
 		} else {
 			Message.sendError(PrisonMine.getLanguage().ERROR_ARGUMENTS);
 			return false;
 		}
 				
 		if(curMine == null) {
 			Message.sendError(PrisonMine.getLanguage().ERROR_ARGUMENTS);
 			return false;
 		}
 		
 		boolean automatic;
 		if(CommandManager.getSender() == null) {
 			automatic = true;
 		} else {
 			automatic = false;
 			Message.debug("+---------------------------------------------");
 			Message.debug("| Mine " + curMine.getId() + " is resetting. Reset report:");
 			Message.debug("| Reset cause: MANUAL (command/sign)");
 		}
 		
		String broadcastMessage;
 		
 		if(automatic) {
 			
 			for(Mine childMine : curMine.getChildren()) {
 				Message.debug("+---------------------------------------------");
 				Message.debug("| Mine " + childMine.getId() + " is resetting. Reset report:");
 				Message.debug("| Reset cause: parent mine is resetting (" + curMine.getId() + ")");
 				CommandManager.RESET.run(childMine.getId());
 				Message.debug("| Reached the end of the report for " + childMine.getId());
 				Message.debug("+---------------------------------------------");
 			}
 			
 			if(curMine.getAutomaticReset() && curMine.getResetsIn() <= 0)
 				broadcastMessage = PrisonMine.getLanguage().RESET_TIMED;
 			else if(curMine.getCompositionReset() && curMine.getCurrentPercent() <= curMine.getRequiredPercent())
 				broadcastMessage = PrisonMine.getLanguage().RESET_COMPOSITION;
 			else
 				broadcastMessage = PrisonMine.getLanguage().RESET_AUTOMATIC;
 			
 			curMine.resetTimer();
 			
 		} else {
 			if(!Util.hasPermission("prison.mine.reset.manual." + curMine.getId()) && !Util.hasPermission("prison.mine.reset.manual")) {
 				Message.sendError(PrisonMine.getLanguage().ERROR_ACCESS);
 				Message.debug("| Insufficient permissions. Cancelling...");
 				Message.debug("| Reached the end of the report for " + curMine.getId());
 				Message.debug("+---------------------------------------------");
 				return false;
 			}
 			
 			if(curMine.getCooldown() && curMine.getCooldownEndsIn() > 0 && !Util.hasPermission("prison.mine.bypass.cooldown")) {
 				Message.sendError(Util.parseVars(PrisonMine.getLanguage().RESET_COOLDOWN, curMine));
 				Message.debug("| Cooldown is in effect. Checking for bypass...");
 				Message.debug("| Failed. Cancelling...");
 				Message.debug("| Reached the end of the report for " + curMine.getId());
 				Message.debug("+---------------------------------------------");
 				return false;
 			}
 			
 			if(curMine.getAutomaticReset() && PrisonMine.getSettings().MANUALTIMERRESET) {
 				Message.debug("| Resetting the timer (config)");
 				curMine.resetTimer();
 			}
 
 			broadcastMessage = PrisonMine.getLanguage().RESET_MANUAL;
 		}
 
 		if(generator.equals("")) generator = curMine.getGenerator();
 		
 		if(curMine.getCooldown()) curMine.resetCooldown();
 		
 		if(!(curMine.reset(generator))) return false;
 		
 		if(!automatic || curMine.getParent() == null) {
 			broadcastMessage = Util.parseVars(broadcastMessage, curMine);
 			
 			if(!curMine.getSilent()) Message.broadcast(broadcastMessage);
 			else if(!automatic) Message.sendSuccess(broadcastMessage);
 		}
 		
 		if(!automatic) {
 			Message.debug("| Reached the end of the report for " + curMine.getId());
 			Message.debug("+---------------------------------------------");
 		}
 		
 		return true;
 	}
 	
 	public void getHelp() {
 		Message.formatHeader(20, "Reset");
 		Message.formatHelp("reset", "<name> [generator]", "Resets the mine manually");
 		Message.formatMessage("Resets the mine according to the generation rules");
 		Message.formatMessage("The following generators are supported: ");
 		Message.formatMessage(ExtensionLoader.list());
 		return;
 	}
 	
 	public void getHelpLine() { Message.formatHelp("reset", "<name> [generator]", "Resets the mine manually", "prison.mine.reset.manual"); }
 }
