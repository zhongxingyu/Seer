 package com.wolvencraft.prison.mines.cmd;
 
 import java.util.List;
 
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import com.wolvencraft.prison.PrisonSuite;
 import com.wolvencraft.prison.mines.CommandManager;
 import com.wolvencraft.prison.mines.PrisonMine;
 import com.wolvencraft.prison.mines.mine.Mine;
 import com.wolvencraft.prison.mines.settings.MineData;
 import com.wolvencraft.prison.mines.settings.SignData;
 import com.wolvencraft.prison.mines.upgrade.ImportData;
 import com.wolvencraft.prison.mines.util.Message;
 import com.wolvencraft.prison.region.PrisonSelection;
 
 public class DebugCommand implements BaseCommand {
 
 	@Override
 	public boolean run(String[] args) {
 		if(args.length == 1 && !args[0].equalsIgnoreCase("debug") && !args[0].equalsIgnoreCase("import")) {
 			Message.sendFormattedError(PrisonMine.getLanguage().ERROR_COMMAND);
 			return false;
 		}
 
 		CommandSender player = CommandManager.getSender();
 		
 		Message.debug("Checks passed, parsing the command");
 		
 		if(args[0].equalsIgnoreCase("debug")) {
 			getHelp();
 			return true;
 		} else if(args[0].equalsIgnoreCase("import")) {
 			List<Mine> newMines = ImportData.loadAll();
 			if(newMines == null) {
 				Message.sendFormatted("DEBUG", "Import folder not found", false);
 				return false;
 			}
 			for(Mine mine : newMines) { PrisonMine.addMine(mine); }
 			Message.sendFormatted("DEBUG", "Mines imported into the system. Check the server log for more info", false);
 			return true;
 		} else if(args[0].equalsIgnoreCase("setregion")) {
 			Mine curMine = Mine.get(args[1]);
 			PrisonSelection sel = PrisonSuite.getSelection((Player) player);
 			curMine.setRegion(sel);
 			Message.sendFormatted("DEBUG", "Region set", false);
 			return curMine.saveFile();
 		} else if(args[0].equalsIgnoreCase("tp")) {
 			Mine curMine = Mine.get(args[1]);
 			((Player) player).teleport(curMine.getRegion().getMaximum());
 			Message.sendFormatted("DEBUG", "Teleported to: " + curMine.getId(), false);
 			return true;
 		} else if(args[0].equalsIgnoreCase("unload")) {
 			PrisonMine.removeMine(Mine.get(args[1]));
 			Message.sendFormatted("DEBUG", "Unloaded " + args[1] + " from memory", false);
 			return true;
		} else if(args[0].equalsIgnoreCase("saveall")) {
 			MineData.saveAll();
 			SignData.saveAll();
 			Message.sendFormatted("DEBUG", "Mine and sign data saved to disk", false);
 			return true;
 		} else {
 			Message.sendFormattedError(PrisonMine.getLanguage().ERROR_COMMAND);
 			return false;
 		}
 	}
 
 	@Override
 	public void getHelp() { 
 		Message.formatHeader(20, "Debug");
 		Message.formatHelp("import", "", "Imports MR and MRL files into the system");
 		Message.formatHelp("setregion", "<id>", "Sets the reset region of a mine to the one specified");
 		Message.formatHelp("tp", "<id>", "Teleports the sender to the specified mine");
 		Message.formatHelp("unload", "<id>", "Unloads the mine from the memory. The mine will be loaded back on server restart");
		Message.formatHelp("saveall", "", "Forces the plugin to save all data to the file");
 	}
 
 	@Override
 	public void getHelpLine() { }
 
 }
