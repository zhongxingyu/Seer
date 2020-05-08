 package com.wolvencraft.prison.mines.cmd;
 
 import java.util.List;
 
 import org.bukkit.command.CommandSender;
 import org.bukkit.command.ConsoleCommandSender;
 import org.bukkit.entity.Player;
 
 import com.wolvencraft.prison.PrisonSuite;
import com.wolvencraft.prison.mines.CommandManager;
 import com.wolvencraft.prison.mines.PrisonMine;
 import com.wolvencraft.prison.mines.mine.Mine;
 import com.wolvencraft.prison.mines.upgrade.ImportData;
 import com.wolvencraft.prison.mines.util.Message;
 import com.wolvencraft.prison.region.PrisonSelection;
 
 public class DebugCommand implements BaseCommand {
 
 	@Override
 	public boolean run(String[] args) {
 		if(args.length == 1 && !args[0].equalsIgnoreCase("debug") && !args[0].equalsIgnoreCase("import")) {
 			Message.sendError(PrisonMine.getLanguage().ERROR_COMMAND);
 			return false;
 		}
 
 		CommandSender player = CommandManager.getSender();
 		
 		if(!(player instanceof ConsoleCommandSender) && !player.isOp()) {
 			Message.sendError(PrisonMine.getLanguage().ERROR_COMMAND);
 			return false;
 		}
 		
 		Message.debug("Checks passed, parsing the command");
 		
 		if(args[0].equalsIgnoreCase("debug")) {
 			getHelp();
 			return true;
 		} else if(args[0].equalsIgnoreCase("import")) {
 			List<Mine> newMines = ImportData.loadAll();
 			if(newMines == null) {
 				Message.sendCustom("DEBUG", "Import folder not found");
 				return false;
 			}
 			for(Mine mine : newMines) { PrisonMine.addMine(mine); }
 			Message.sendCustom("DEBUG", "Mines imported into the system. Check the server log for more info");
 			return true;
 		} else if(args[0].equalsIgnoreCase("setregion")) {
 			Mine curMine = Mine.get(args[1]);
 			PrisonSelection sel = PrisonSuite.getSelection((Player) player);
 			curMine.setRegion(sel);
 			Message.sendCustom("DEBUG", "Region set");
 			return curMine.saveFile();
 		} else if(args[0].equalsIgnoreCase("tp")) {
 			Mine curMine = Mine.get(args[1]);
 			((Player) player).teleport(curMine.getRegion().getMaximum());
 			Message.sendCustom("DEBUG", "Teleported to: " + curMine.getId());
 			return true;
 		} else if(args[0].equalsIgnoreCase("unload")) {
 			PrisonMine.removeMine(Mine.get(args[1]));
 			Message.sendCustom("DEBUG", "Unloaded " + args[1] + " from memory");
 			return true;
 		} else if(args[0].equalsIgnoreCase("setwarp")) {
 			Mine curMine = Mine.get(args[1]);
 			curMine.setTpPoint(((Player) CommandManager.getSender()).getLocation());
 			Message.sendCustom("DEBUG", "Mine tp point is set");
 			return true;
 		} else {
 			Message.sendError(PrisonMine.getLanguage().ERROR_COMMAND);
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
 		Message.formatHelp("setwarp", "<id>", "Sets the tp point for the mine");
 	}
 
 	@Override
 	public void getHelpLine() { }
 
 }
