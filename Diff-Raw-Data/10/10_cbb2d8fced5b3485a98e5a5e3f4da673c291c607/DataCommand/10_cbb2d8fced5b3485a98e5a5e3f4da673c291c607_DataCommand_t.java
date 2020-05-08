 package com.wolvencraft.prison.mines.cmd;
 
 import com.wolvencraft.prison.mines.CommandManager;
 import com.wolvencraft.prison.mines.PrisonMine;
 import com.wolvencraft.prison.mines.settings.MineData;
 import com.wolvencraft.prison.mines.settings.SignData;
 import com.wolvencraft.prison.mines.util.Message;
 
 public class DataCommand implements BaseCommand {
 	public boolean run(String[] args) {
 		
 		if(args.length == 1) {
 			getHelp();
 			return true;
 		}
 		
 		if(args.length != 2) {
 			Message.sendError(PrisonMine.getLanguage().ERROR_ARGUMENTS);
 			return false;
 		}
 		
 		if(args[1].equalsIgnoreCase("save")) {
 			MineData.saveAll();
 			SignData.saveAll();
 			Message.sendSuccess("Mine and sign data saved to disc");
 			return true;
 		}
 		else if(args[1].equalsIgnoreCase("load")) {
 			CommandManager.getPlugin().reloadConfig();
			CommandManager.getPlugin().reloadSettings();
 			CommandManager.getPlugin().reloadLanguageData();
			CommandManager.getPlugin().reloadLanguage();
 			PrisonMine.setMines(MineData.loadAll());
 			PrisonMine.setSigns(SignData.loadAll());
 			Message.sendSuccess("Mine and sign data loaded from disc");
 			return true;
 		}
 		else {
 			Message.sendError(PrisonMine.getLanguage().ERROR_COMMAND);
 			return false;
 		}
 	}
 	
 	public void getHelp() {
 		Message.formatHeader(20, "Data");
 		Message.formatHelp("data", "save", "Saves the mine data to file");
 		Message.formatHelp("data", "load", "Loads the mine data from file");
 	}
 	
 	public void getHelpLine() { Message.formatHelp("data", "", "Shows information on data manipulation", "mcprison.mine.admin"); }
 }
