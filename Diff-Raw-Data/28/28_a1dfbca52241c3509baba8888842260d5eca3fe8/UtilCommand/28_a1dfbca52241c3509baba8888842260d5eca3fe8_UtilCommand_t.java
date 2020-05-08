 package com.wolvencraft.prison.mines.cmd;
 
 import com.wolvencraft.prison.mines.PrisonMine;
 import com.wolvencraft.prison.mines.settings.MineData;
 import com.wolvencraft.prison.mines.settings.SignData;
 import com.wolvencraft.prison.mines.util.Message;
 
 public class UtilCommand implements BaseCommand {
 	
 	@Override
 	public boolean run(String[] args) {
 		
 		if(args[0].equalsIgnoreCase("reload")) {
 			PrisonMine.getInstance().reloadConfig();
 			PrisonMine.getInstance().reloadSettings();
 			PrisonMine.getInstance().reloadLanguageData();
 			PrisonMine.getInstance().reloadLanguage();
 			PrisonMine.setMines(MineData.loadAll());
 			PrisonMine.setSigns(SignData.loadAll());
 			Message.sendFormattedSuccess("Data loaded from disk successfully", false);
 			return true;
		} else if(args[0].equalsIgnoreCase("saveall")) {
			MineData.saveAll();
			SignData.saveAll();
			Message.sendFormattedSuccess("Data saved to disk successfully", false);
			return true;
 		} else {
 			Message.sendFormattedError(PrisonMine.getLanguage().ERROR_COMMAND);
 			return false;
 		}
 	}
 	
 	@Override
 	public void getHelp() {}
 	
 	@Override
 	public void getHelpLine() { Message.formatHelp("reload", "", "Reloads all data from file", "prison.mine.admin"); }
 }
