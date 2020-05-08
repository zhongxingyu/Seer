 package com.wolvencraft.prison.mines.cmd;
 
 import java.text.DecimalFormat;
 import java.util.List;
 
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.entity.Player;
 import org.bukkit.material.MaterialData;
 
 import com.wolvencraft.prison.PrisonSuite;
 import com.wolvencraft.prison.hooks.TimedTask;
 import com.wolvencraft.prison.mines.CommandManager;
 import com.wolvencraft.prison.mines.PrisonMine;
 import com.wolvencraft.prison.mines.mine.Mine;
 import com.wolvencraft.prison.mines.settings.Language;
 import com.wolvencraft.prison.mines.settings.MineData;
 import com.wolvencraft.prison.mines.util.Message;
 import com.wolvencraft.prison.mines.util.Util;
 import com.wolvencraft.prison.mines.util.data.MineBlock;
 
 public class EditCommand  implements BaseCommand {
 	
 	@Override
 	public boolean run(String[] args) {
 		
 		Language language = PrisonMine.getLanguage();
 		Mine curMine = PrisonMine.getCurMine();
 		if(curMine == null
 				&& !args[0].equalsIgnoreCase("edit")
 				&& !args[0].equalsIgnoreCase("delete")) {
 			Message.sendFormattedError(language.ERROR_MINENOTSELECTED);
 			return false;
 		}
 		
 		if(args[0].equalsIgnoreCase("edit")) {
 			if(args.length == 1) {
 				if(PrisonMine.getCurMine() != null) {
 					Message.sendFormattedSuccess(language.MINE_DESELECTED);
 					PrisonMine.setCurMine(null);
 					return true;
 				} else { getHelp(); return true; }
 			} else if(args.length == 2) {
 				if(args[1].equalsIgnoreCase("help")) { getHelp(); return true; }
 				
 				curMine = Mine.get(args[1]);
 				if(curMine == null) { Message.sendFormattedError(language.ERROR_MINENAME.replace("<ID>", args[1])); return false; }
 				PrisonMine.setCurMine(curMine);
 				Message.sendFormattedSuccess(language.MINE_SELECTED);
 				return true;
 			} else { Message.sendFormattedError(language.ERROR_ARGUMENTS); return false; }
 		} else if(args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("+")) {
 			if(args.length == 1) { getHelp(); return true; }
 			if(args.length > 3) { Message.sendFormattedError(language.ERROR_ARGUMENTS); return false; }
 			
 			List<MineBlock> localBlocks = curMine.getLocalBlocks();
 			if(localBlocks.size() == 0) curMine.addBlock(new MaterialData(Material.AIR), 1);
 			
 			MaterialData block = Util.getBlock(args[1]);
 			MineBlock air = curMine.getBlock(new MaterialData(Material.AIR));
 			
 			if(block == null) { Message.sendFormattedError(language.ERROR_NOSUCHBLOCK.replaceAll("<BLOCK>", args[1])); return false; }
 			if(block.equals(air.getBlock())) { Message.sendFormattedError(language.ERROR_FUCKIGNNOOB); return false; }
 
 			double percent, percentAvailable = air.getChance();
 			
 			if(args.length == 3) {
 				if(Util.isNumeric(args[2])) percent = Double.parseDouble(args[2]);
 				else {
 					try { percent = Double.parseDouble(args[2].replace("%", "")); }
 					catch(NumberFormatException nfe) { Message.sendFormattedError(language.ERROR_ARGUMENTS); return false; }
 				}
 				percent = percent / 100;
 			}
 			else percent = percentAvailable;
 			
 			if(percent <= 0) { Message.sendFormattedError(language.ERROR_ARGUMENTS); return false; }
 			DecimalFormat dFormat = new DecimalFormat("#.########");
 			percent = Double.valueOf(dFormat.format(percent));
 			
 			if((percentAvailable - percent) < 0) { Message.sendFormattedError("Invalid percentage. Use /mine info " + curMine.getId() + " to review the percentages"); return false; }
 			else percentAvailable -= percent;
 			
 			air.setChance(percentAvailable);
 			MineBlock index = curMine.getBlock(block);
 			
 			if(index == null) curMine.addBlock(block, percent);
 			else index.setChance(index.getChance() + percent);
 			
 			Message.sendFormattedMine(Util.round(percent) + " of " + block.getItemType().toString().toLowerCase().replace("_", " ") + " added to the mine");
 			Message.sendFormattedMine("Reset the mine for the changes to take effect");
 			
 			return curMine.saveFile();
 		} else if(args[0].equalsIgnoreCase("remove") || args[0].equalsIgnoreCase("-")) {
 			if(args.length == 1) { getHelp(); return true; }
 			if(args.length > 3) { Message.sendFormattedError(language.ERROR_ARGUMENTS); return false; }
 			
 			MineBlock blockData = curMine.getBlock(Util.getBlock(args[1]));
 			if(blockData == null) { Message.sendFormattedError("There is no " + ChatColor.RED + args[1] + ChatColor.WHITE + " in mine '" + curMine.getId() + "'"); return false; }
 
 			MineBlock air = curMine.getBlock(new MaterialData(Material.AIR));
 			if(blockData.getBlock().equals(air.getBlock())) { Message.sendFormattedError(language.ERROR_FUCKIGNNOOB); return false; }
 			
 			double percent = 0;
 			
 			if(args.length == 3) {
 				if(Util.isNumeric(args[2])) percent = Double.parseDouble(args[2]);
 				else {
 					try { percent = Double.parseDouble(args[2].replace("%", "")); }
 					catch(NumberFormatException nfe) { Message.sendFormattedError(language.ERROR_ARGUMENTS); return false; }
 				}
 				percent = percent / 100;
 				if(percent > blockData.getChance()) percent = blockData.getChance();
 				air.setChance(air.getChance() + percent);
 				blockData.setChance(blockData.getChance() - percent);
 				Message.sendFormattedMine(Util.round(percent) + " of " + args[1] + " was successfully removed from the mine");
 			}
 			else {
 				air.setChance(air.getChance() + blockData.getChance());
 				curMine.removeBlock(blockData);
 				Message.sendFormattedMine(args[1] + " was successfully removed from the mine");
 			}
 			
 			return curMine.saveFile();
 		} else if(args[0].equalsIgnoreCase("name")) {
 			if(args.length == 1) { getHelp(); return true; }
 			if(args.length < 2) { Message.sendFormattedError(language.ERROR_ARGUMENTS); return false; }
 			
 			String name = args[1];
 			for(int i = 2; i < args.length; i++) name = name + " " + args[i];
 			curMine.setName(name);
 			Message.sendFormattedMine("Mine now has a display name '" + ChatColor.GOLD + name + ChatColor.WHITE + "'");
 			return curMine.saveFile();
 		} else if(args[0].equalsIgnoreCase("cooldown")) {
 			if(args.length > 2) { Message.sendFormattedError(language.ERROR_ARGUMENTS); return false; }
 			
 			if(args.length == 1) {
 				if(curMine.getCooldown()) {
 					curMine.setCooldownEnabled(false);
 					Message.sendFormattedMine("Reset cooldown " + ChatColor.RED + "disabled");
 				}
 				else {
 					curMine.setCooldownEnabled(true);
 					Message.sendFormattedMine("Reset cooldown " + ChatColor.GREEN + "enabled");
 				}
 			} else if(args.length == 2) {
 				int seconds = Util.parseTime(args[1]);
 				if(seconds == -1) { Message.sendFormattedError(language.ERROR_ARGUMENTS); return false; }
 				curMine.setCooldownPeriod(seconds);
 				Message.sendFormattedMine("Reset cooldown set to " + ChatColor.GREEN + Util.parseSeconds(seconds));
 			} else { Message.sendFormattedError(language.ERROR_ARGUMENTS); return false; }
 			
 			return curMine.saveFile();
 		} else if(args[0].equalsIgnoreCase("setparent") || args[0].equalsIgnoreCase("link")) {
 			if(args.length > 2) { Message.sendFormattedError(language.ERROR_ARGUMENTS); return false; }
 			
 			if(args.length == 1) {
 				if(curMine.getParent() != null) {
 					Message.sendFormattedMine("Mine is no longer linked to " + ChatColor.RED + curMine.getParent());
 					curMine.setParent(null);
 				} else { getHelp(); return true; }
 			} else {
 				Mine parentMine = Mine.get(args[1]);
 				if(parentMine == null) { Message.sendFormattedError(language.ERROR_MINENAME); return false; }
 				if(parentMine.getId().equalsIgnoreCase(curMine.getId())) { Message.sendFormattedError("The mine cannot be a parent of itself"); return false; }
 				if(parentMine.hasParent() && parentMine.getSuperParent().getId().equalsIgnoreCase(curMine.getId())) { Message.sendFormattedError("Looping structure detected!"); return false; }
 				
 				curMine.setParent(args[1]);
 				Message.sendFormattedMine("Mine is now linked to " + ChatColor.GREEN + args[1]);
 			}			
 			return curMine.saveFile();
 		} else if(args[0].equalsIgnoreCase("setwarp")) {
 			if(args.length != 1) { Message.sendFormattedError(language.ERROR_ARGUMENTS); return false; }
 			curMine.setTpPoint(((Player) CommandManager.getSender()).getLocation());
 			Message.sendFormattedMine("Mine tp point is set to your current location");
 			return curMine.saveFile();
 		} else if(args[0].equalsIgnoreCase("delete") || args[0].equalsIgnoreCase("del")) {
 			if(args.length == 1 && curMine == null) { getHelp(); return true; }
 			if(args.length > 2) { Message.sendFormattedError(language.ERROR_ARGUMENTS); return false; }
 			
 			if(args.length != 1) {
 				curMine = Mine.get(args[1]);
 				if(curMine == null) { Message.sendFormattedError(language.ERROR_MINENAME); return false; }
 			}
 			
 			for(Mine child : curMine.getChildren()) { child.setParent(null); }
 			for(TimedTask task : PrisonSuite.getLocalTasks()) {
 				if(task.getName().endsWith(curMine.getId())) task.cancel();
 			}
 			
 			PrisonMine.removeMine(curMine);
 			Message.sendFormattedMine("Mine successfully deleted");
 			PrisonMine.setCurMine(null);
 			curMine.deleteFile();
 			MineData.saveAll();
 			return true;
 		} else {
 			Message.sendFormattedError(language.ERROR_COMMAND);
 			return false;
 		}
 	}
 	
 	@Override
 	public void getHelp() {
 		Message.formatHeader(20, "Editing");
 		Message.formatHelp("edit", "<id>", "Selects a mine to edit its properties");
 		Message.formatHelp("edit", "", "Deselects the current mine");
 		Message.formatHelp("+", "<block> [percentage]", "Adds a block type to the mine");
 		Message.formatHelp("-", "<block> [percentage]", "Removes the block from the mine");
 		Message.formatHelp("name", "<name>", "Sets a display name for a mine. Spaces allowed");
 		Message.formatHelp("cooldown", "", "Toggles the reset cooldown");
 		Message.formatHelp("cooldown <time>", "", "Sets the cooldown time");
 		Message.formatHelp("setparent", "<id>", "Links the timers of two mines");
 		Message.formatHelp("setwarp", "", "Sets the teleportation point for the mine");
 		Message.formatHelp("delete", "[id]", "Deletes all the mine data");
 		return;
 	}
 	
 	@Override
 	public void getHelpLine() { Message.formatHelp("edit help", "", "Shows a help page on mine atribute editing", "prison.mine.edit"); }
 }
