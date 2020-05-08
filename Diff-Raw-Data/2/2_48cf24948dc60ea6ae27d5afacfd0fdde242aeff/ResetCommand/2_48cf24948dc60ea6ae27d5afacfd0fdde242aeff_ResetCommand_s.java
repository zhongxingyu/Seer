 package com.wolvencraft.prison.mines.cmd;
 
 import com.wolvencraft.prison.mines.CommandManager;
 import com.wolvencraft.prison.mines.MineCommand;
 import com.wolvencraft.prison.mines.PrisonMine;
 import com.wolvencraft.prison.mines.mine.Mine;
 import com.wolvencraft.prison.mines.util.GeneratorUtil;
 import com.wolvencraft.prison.mines.util.Message;
 import com.wolvencraft.prison.mines.util.Util;
 
 public class ResetCommand implements BaseCommand
 {	
 	public boolean run(String[] args) {
 		
 		Mine curMine;
 		if(args.length == 1) curMine = PrisonMine.getCurMine();
 		else if(args[1].equalsIgnoreCase("all")) {
 			boolean success = true;
 			for(Mine mine : PrisonMine.getMines()) {
 				if(!MineCommand.RESET.run(mine.getName())) success = false;
 			}
 			return success;
 		}
 		else curMine = Mine.get(args[1]);
 		
 		if(curMine == null) {
 			if(args.length == 1) getHelp();
 			return false;
 		}
 		
 		Message.debug("Resetting mine: " + curMine.getId());
 		boolean automatic;
 		if(CommandManager.getSender() == null) {
 			automatic = true;
 			Message.debug("Automatic reset!");
 		}
 		else {
 			automatic = false;
 			Message.debug("Manual reset!");
 		}
 		
 		if(!automatic) {
 			if(!Util.hasPermission("mcprison.mine.reset.manual." + curMine.getId()) && !Util.hasPermission("mcprison.mine.reset.manual")) {
 				Message.sendError(PrisonMine.getLanguage().ERROR_ACCESS);
 				return false;
 			}
 			
 			if(curMine.getCooldown() && curMine.getCooldownEndsIn() > 0 && !Util.hasPermission("mcprison.mine.bypass.cooldown")) {
 				Message.sendError(Util.parseVars(PrisonMine.getLanguage().RESET_COOLDOWN, curMine));
 				return false;
 			}
 		}
 		
 		String forcedGenerator = "";
 		if(args.length == 3) forcedGenerator = args[2];
 		
 		String generator = curMine.getGenerator();
 		if(forcedGenerator.equals("")) generator = curMine.getGenerator();
 		
 		if(curMine.getCooldown()) curMine.resetCooldown();
 		
 		if(!(curMine.reset(generator))) return false;
 		
 		String broadcastMessage;
 		if(automatic) {
 			for(Mine childMine : curMine.getChildren()) { MineCommand.RESET.run(childMine.getId()); }
 			
 			if(curMine.getResetsIn() <= 0)
 				broadcastMessage = PrisonMine.getLanguage().RESET_TIMED;
 			else if(curMine.getPercent() <= curMine.getCompositionPercent())
 				broadcastMessage = PrisonMine.getLanguage().RESET_COMPOSITION;
 			else
 				broadcastMessage = PrisonMine.getLanguage().RESET_AUTOMATIC;
 		}
 		else broadcastMessage = PrisonMine.getLanguage().RESET_MANUAL;
 		
 		if(curMine.getParent() == null) {
 			broadcastMessage = Util.parseVars(broadcastMessage, curMine);
 			
 			if(!curMine.getSilent()) Message.broadcast(broadcastMessage);
 			else if(!automatic) Message.sendSuccess(broadcastMessage);
 		}
 		return true;
 	}
 	
 	public void getHelp() {
 		Message.formatHeader(20, "Reset");
 		Message.formatHelp("reset", "<name> [generator]", "Resets the mine manually");
 		Message.formatMessage("Resets the mine according to the generation rules");
 		Message.formatMessage("The following generators are supported: ");
 		Message.formatMessage(GeneratorUtil.list());
 		return;
 	}
 	
 	public void getHelpLine() { Message.formatHelp("reset", "<name> [generator]", "Resets the mine manually", "mcprison.mine.reset.manual"); }
 }
