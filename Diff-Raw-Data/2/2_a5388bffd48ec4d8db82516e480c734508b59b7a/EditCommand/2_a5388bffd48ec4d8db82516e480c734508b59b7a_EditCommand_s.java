 package com.wolvencraft.prison.mines.cmd;
 
 import java.util.List;
 
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.material.MaterialData;
 
 import com.wolvencraft.prison.mines.PrisonMine;
 import com.wolvencraft.prison.mines.generation.BaseGenerator;
 import com.wolvencraft.prison.mines.mine.Mine;
 import com.wolvencraft.prison.mines.mine.MineBlock;
 import com.wolvencraft.prison.mines.settings.Language;
 import com.wolvencraft.prison.mines.settings.MineData;
 import com.wolvencraft.prison.mines.util.ExtensionLoader;
 import com.wolvencraft.prison.mines.util.Message;
 import com.wolvencraft.prison.mines.util.Util;
 
 public class EditCommand  implements BaseCommand {
 	public boolean run(String[] args) {
 		
 		if(args.length == 1
 				&& !args[0].equalsIgnoreCase("none")
 				&& !args[0].equalsIgnoreCase("delete")
 				&& !args[0].equalsIgnoreCase("generator")
 				&& !args[0].equalsIgnoreCase("silent")) {
 			getHelp();
 			return true;
 		}
 
 		Language language = PrisonMine.getLanguage();
 		Mine curMine = PrisonMine.getCurMine();
 		if(curMine == null
 				&& !args[0].equalsIgnoreCase("edit")
 				&& !args[0].equalsIgnoreCase("delete")
 				&& !args[0].equalsIgnoreCase("generator")) {
 			Message.sendError(language.ERROR_MINENOTSELECTED);
 			return false;
 		}
 		
 		if(args[0].equalsIgnoreCase("edit")) {
 			if(args.length != 2) {
 				Message.sendError(language.ERROR_ARGUMENTS);
 				return false;
 			}
 			
 			if(args[1].equalsIgnoreCase("none")) {
 				Message.sendSuccess(Util.parseVars(language.MINE_DESELECTED, curMine));
 				PrisonMine.setCurMine(null);
 				return true;
 			}
 			
 			curMine = Mine.get(args[1]);
 			if(curMine == null) {
 				Message.sendError(language.ERROR_MINENAME.replace("<ID>", args[1]));
 				return false;
 			}
 
 			PrisonMine.setCurMine(curMine);
 			Message.sendSuccess(Util.parseVars(language.MINE_SELECTED, curMine));
 			return true;
 		}
 		else if(args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("+")) {
 			if(args.length != 2 && args.length != 3) {
 				Message.sendError(language.ERROR_ARGUMENTS);
 				return false;
 			}
 			
 			List<MineBlock> blocks = curMine.getBlocks();
 			if(blocks.size() == 0) blocks.add(new MineBlock(new MaterialData(Material.AIR), 1));
 			
 			MaterialData block = Util.getBlock(args[1]);
 			MineBlock air = curMine.getBlock(new MaterialData(Material.AIR));
 			
 			if(block == null) {
 				Message.sendError(language.ERROR_NOSUCHBLOCK.replaceAll("<BLOCK>", args[1]));
 				return false;
 			}
 			if(block.equals(air.getBlock())) {
 				Message.sendError(language.ERROR_FUCKIGNNOOB);
 				return false;
 			}
 
 			double percent, percentAvailable = air.getChance();
 			
 			if(args.length == 3) {
 				if(Util.isNumeric(args[2])) percent = Double.parseDouble(args[2]);
 				else {
 					Message.debug("Argument is not numeric, attempting to parse");
 					try { percent = Double.parseDouble(args[2].replace("%", "")); }
 					catch(NumberFormatException nfe) {
 						Message.sendError(language.ERROR_ARGUMENTS);
 						return false;
 					}
 				}
 				
 				percent = percent / 100;
 				Message.debug("Chance value is " + percent);
 			}
 			else percent = percentAvailable;
 			
 			if(percent <= 0) {
 				Message.sendError(language.ERROR_ARGUMENTS);
 				return false;
 			}
 			
 			if((percentAvailable - percent) < 0) {
 				Message.sendError("Invalid percentage. Use /mine info " + curMine.getId() + " to review the percentages");
 				return false;
 			}
 			else percentAvailable -= percent;
 			air.setChance(percentAvailable);
 			
 			MineBlock index = curMine.getBlock(block);
 			
 			if(index == null) blocks.add(new MineBlock(block, percent));
 			else index.setChance(index.getChance() + percent);
 			
 			Message.sendCustom(curMine.getId(), Util.round(percent) + " of " + block.getItemType().toString().toLowerCase().replace("_", " ") + " added to the mine");
 			Message.sendCustom(curMine.getId(), "Reset the mine for the changes to take effect");
 			
 			return curMine.save();
 		}
 		else if(args[0].equalsIgnoreCase("remove") || args[0].equalsIgnoreCase("-")) {
 			if(args.length != 2 && args.length != 3) {
 				Message.sendError(language.ERROR_ARGUMENTS);
 				return false;
 			}
 			
 			MineBlock blockData = curMine.getBlock(Util.getBlock(args[1]));
 			if(blockData == null) {
 				Message.sendError("There is no " + ChatColor.RED + args[1] + ChatColor.WHITE + " in mine '" + curMine + "'");
 				return false;
 			}
 
 			MineBlock air = curMine.getBlock(new MaterialData(Material.AIR));
 			if(blockData.equals(air)) {
 				Message.sendError("This value is calculated automatically");
 				return false;
 			}
 			
 			double percent;
 			
 			if(args.length == 3) {
 				if(Util.isNumeric(args[2])) percent = Double.parseDouble(args[2]);
 				else {
 					Message.debug("Argument is not numeric, attempting to parse");
 					try {
 						percent = Double.parseDouble(args[2].replace("%", ""));
 					}
 					catch(NumberFormatException nfe) {
 						Message.sendError(language.ERROR_ARGUMENTS);
 						return false;
 					}
 				}
 				
 				percent = percent / 100;
 				Message.debug("Chance value is " + percent);
 				
 				if(percent > blockData.getChance()) percent = blockData.getChance();
 				
 				air.setChance(air.getChance() + percent);
 				blockData.setChance(blockData.getChance() - percent);
 				
 				Message.sendCustom(curMine.getId(), Util.round(percent) + " of " + args[1] + " was successfully removed from the mine");
 			}
 			else {
 				List<MineBlock> blocks = curMine.getBlocks();
 
 				air.setChance(air.getChance() + blockData.getChance());
 				blocks.remove(blockData);
 				
 				Message.sendCustom(curMine.getId(), args[1] + " was successfully removed from the mine");
 			}
 			
 			return curMine.save();
 		}
 		else if(args[0].equalsIgnoreCase("delete") || args[0].equalsIgnoreCase("del")) {
 			if(args.length > 2) {
 				Message.sendError(language.ERROR_ARGUMENTS);
 				return false;
 			}
 			
 			if(args.length == 1) {
 				curMine = PrisonMine.getCurMine();
 				if(curMine == null) {
 					Message.sendError(language.ERROR_MINENOTSELECTED);
 					return false;
 				}
 			}
 			else {
 				curMine = Mine.get(args[1]);
 				if(curMine == null) {
 					Message.sendError(language.ERROR_MINENAME);
 					return false;
 				}
 			}
 			
 			if(!ExtensionLoader.get(curMine.getGenerator()).remove(curMine)) return false;
 			
 			PrisonMine.removeMine(curMine);
 			PrisonMine.setCurMine(null);
 			Message.sendCustom(curMine.getId(), "Mine successfully deleted");
 			curMine.delete();
 			MineData.saveAll();
 			return true;
 		}
 		else if(args[0].equalsIgnoreCase("name")) {
 			if(args.length < 2) {
 				Message.sendError(language.ERROR_ARGUMENTS);
 				return false;
 			}
 			
 			String name = args[1];
 			for(int i = 2; i < args.length; i++) name = name + " " + args[i];
 			
 			curMine.setName(name);
 			Message.sendCustom(curMine.getId(), "Mine now has a display name '" + ChatColor.GOLD + name + ChatColor.WHITE + "'");
 			
 			return curMine.save();
 		}
 		else if(args[0].equalsIgnoreCase("silent")) {
 			if(args.length != 1) {
 				Message.sendError(language.ERROR_ARGUMENTS);
 				return false;
 			}
 			
 			if(curMine.getSilent()) {
 				curMine.setSilent(false);
 				Message.sendCustom(curMine.getId(), "Silent mode " + ChatColor.RED + "off");
 			}
 			else {
 				curMine.setSilent(true);
 				Message.sendCustom(curMine.getId(), "Silent mode " + ChatColor.GREEN + "on");
 			}
 			
 			return curMine.save();
 		}
 		else if(args[0].equalsIgnoreCase("cooldown")) {
 			if(args.length != 2) {
 				Message.sendError(language.ERROR_ARGUMENTS);
 				return false;
 			}
 			
 			if(args[1].equalsIgnoreCase("toggle")) {
 				if(curMine.getCooldown()) {
 					curMine.setCooldownEnabled(false);
 					Message.sendCustom(curMine.getId(), "Reset cooldown " + ChatColor.RED + "disabled");
 				}
 				else {
 					curMine.setCooldownEnabled(true);
 					Message.sendCustom(curMine.getId(), "Reset cooldown " + ChatColor.GREEN + "enabled");
 				}
 			}
 			else {
 				try {
 					int seconds = Util.parseTime(args[1]);
 					if(seconds == -1) {
 						Message.sendError(language.ERROR_ARGUMENTS);
 						return false;
 					}
 					curMine.setCooldownPeriod(seconds);
 					Message.sendCustom(curMine.getId(), "Reset cooldown set to " + ChatColor.GREEN + Util.parseSeconds(seconds));
 				}
 				catch (NumberFormatException nfe) {
 					Message.sendError(language.ERROR_ARGUMENTS);
 				}
 			}
 			
 			return curMine.save();
 		}
 		else if(args[0].equalsIgnoreCase("generator")) {
 			if(args.length == 1) {
 				getGenerators();
 				return false;
 			}
 			
 			if(args.length != 2) {
 				Message.sendError(language.ERROR_ARGUMENTS);
 				return false;
 			}
 			
 			if(!ExtensionLoader.get(args[1]).init(curMine)) return false;
 			curMine.setGenerator(args[1].toUpperCase());
 			
 			Message.sendCustom(curMine.getId(), "Mine generator has been set to " + ChatColor.GREEN + args[1].toUpperCase());
 
 			return curMine.save();
 		}
 		else if(args[0].equalsIgnoreCase("setparent") || args[0].equalsIgnoreCase("link")) {
 			if(args.length != 2) {
 				Message.sendError(language.ERROR_ARGUMENTS);
 				return false;
 			}
 			
 			if(args[1].equalsIgnoreCase("none")) {
 				Message.sendCustom(curMine.getId(), "Mine is no longer linked to " + ChatColor.RED + curMine.getParent());
 				curMine.setParent(null);
 				
 				return curMine.save();
 			}
 			
 			if(Mine.get(args[1]) == null) {
 				Message.sendError(language.ERROR_MINENAME);
 				return false;
 			}
 			
 			if(args[1].equalsIgnoreCase(curMine.getId())) {
 				Message.sendError("You cannot set mine's parent to itself, silly");
 				return false;
 			}
 			
 			if(Mine.get(args[1]).getParent() != null
 					&& Mine.get(args[1]).getParent().equalsIgnoreCase(curMine.getId())) {
 				Message.sendError("Infinite loop detected in timers!");
 				return false;
 			}
 			
 			curMine.setParent(args[1]);
 			Message.sendCustom(curMine.getId(), "Mine will is now linked to " + ChatColor.GREEN + args[1]);
 			
 			return curMine.save();
 		}
 		else {
 			Message.sendError(language.ERROR_COMMAND);
 			return false;
 		}
 	}
 	
 
 	public void getHelp() {
 		Message.formatHeader(20, "Editing");
 		Message.formatHelp("edit", "<id>", "Selects a mine to edit its properties");
 		Message.formatHelp("name", "<name>", "Sets a display name for a mine");
 		Message.formatHelp("+", "<block> [percentage]", "Adds a block type to the mine");
 		Message.formatHelp("-", "<block> [persentage]", "Removes the block from the mine");
 		Message.formatHelp("delete", "[id]", "Deletes all the mine data");
 		Message.formatHelp("silent", "", "Toggles the public notifications");
 		Message.formatHelp("setparent", "<id>", "Links the timers of two mines");
 		Message.formatHelp("generator", "<generator>", "Changes the active generator");
 		Message.formatMessage("The following generators are supported: ");
 		Message.formatMessage(ExtensionLoader.list());
 		Message.formatHelp("cooldown toggle", "", "Toggles the reset cooldown");
 		Message.formatHelp("cooldown <time>", "", "Sets the cooldown time");
 		return;
 	}
 	
 	public void getGenerators() {
 		Message.formatHelp("generator", "<generator>", "Changes the active generator for the mine");
 		Message.formatMessage("The following generators are available:");
 		for(BaseGenerator gen : PrisonMine.getGenerators())
 			Message.formatMessage(ChatColor.GOLD + gen.getName() + ChatColor.WHITE + ": " + gen.getDescription());
 		return;
 	}
 	
 	public void getHelpLine() { Message.formatHelp("edit", "", "Shows a help page on mine atribute editing", "prison.mine.edit"); }
 }
