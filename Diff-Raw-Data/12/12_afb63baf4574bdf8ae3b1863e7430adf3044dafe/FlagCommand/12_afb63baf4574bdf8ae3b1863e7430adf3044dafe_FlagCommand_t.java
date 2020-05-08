 package com.wolvencraft.prison.mines.cmd;
 
 import com.wolvencraft.prison.mines.PrisonMine;
 import com.wolvencraft.prison.mines.flags.BaseFlag;
 import com.wolvencraft.prison.mines.flags.FlagHandler;
 import com.wolvencraft.prison.mines.mine.Mine;
 import com.wolvencraft.prison.mines.settings.Language;
 import com.wolvencraft.prison.mines.util.Message;
 
 public class FlagCommand implements BaseCommand {
 
 	@Override
 	public boolean run(String[] args) {
 		Language language = PrisonMine.getLanguage();
 		if(args.length < 3 || !FlagHandler.exists(args[1])) {
 			Message.sendError(language.ERROR_ARGUMENTS);
 			return false;
 		}
 		
 		Mine curMine = PrisonMine.getCurMine();
 		if(curMine == null) {
 			Message.sendError(language.ERROR_MINENOTSELECTED);
 			return false;
 		}
 		
 		BaseFlag flag = FlagHandler.get(args[1]).dispatch();
 		if(flag == null) return false;
 		if(!flag.checkValue(args[2])) {
 			String[] validValues = flag.getValidValues();
 			String valueString = validValues[0];
 			for(String temp : validValues) {
 				valueString += ", " + temp;
 			}
 			Message.sendError("This flag value is not valid. Valid values: " + valueString);
 		}
 		if(!curMine.hasFlag(flag)) { curMine.addFlag(flag); }
 		curMine.getFlag(args[1]).setParam(args[2]);
 		Message.sendCustom(curMine.getId(), "A new flag has been added to the mine: " + flag.getName() + ":" + flag.getParam().toString());
 		return curMine.saveFile();
 	}
 
 	@Override
 	public void getHelp() {
 		Message.formatHeader(20, "Flags");
 		Message.formatHelp("flag", "<flag> <value>", "Adds a flag value to the mine");
 		FlagHandler[] validFlags = FlagHandler.values();
 		String flagString = validFlags[0].getAlias();
 		for(int i = 1; i < validFlags.length; i++) {
 			flagString += ", " + validFlags[i].getAlias();
 		}
 		Message.send("Available flags: "+ flagString);
 	}
 
 	@Override
 	public void getHelpLine() {
		if(PrisonMine.getInstance().getVersion() > 1.2) Message.formatHelp("flag", "", "Shows the help page on mine flags", "prison.mine.edit");
 	}
 }
