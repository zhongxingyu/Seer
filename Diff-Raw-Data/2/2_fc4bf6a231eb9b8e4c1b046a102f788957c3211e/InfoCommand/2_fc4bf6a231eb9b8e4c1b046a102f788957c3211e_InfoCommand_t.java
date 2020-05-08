 package com.wolvencraft.prison.mines.cmd;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Level;
 
 import org.bukkit.ChatColor;
 import org.bukkit.material.MaterialData;
 
 import com.wolvencraft.prison.mines.PrisonMine;
 import com.wolvencraft.prison.mines.mine.Mine;
 import com.wolvencraft.prison.mines.settings.Language;
 import com.wolvencraft.prison.mines.util.DrawingTools;
 import com.wolvencraft.prison.mines.util.Message;
 import com.wolvencraft.prison.mines.util.Util;
 import com.wolvencraft.prison.mines.util.constants.BlacklistState;
 import com.wolvencraft.prison.mines.util.constants.Protection;
 import com.wolvencraft.prison.mines.util.data.Blacklist;
 import com.wolvencraft.prison.mines.util.flags.BaseFlag;
 
 public class InfoCommand  implements BaseCommand {
 	
 	@Override
 	public boolean run(String[] args) {
 		Mine curMine = null;
 		Language language = PrisonMine.getLanguage();
 		
 		if(args.length == 1) {
 			curMine = PrisonMine.getCurMine();
 			if(curMine == null) {
 				getHelp();
 				return true;
 			}
 		}
 		else curMine = Mine.get(args[1]);
 		
 		if(args.length > 2) {
 			Message.sendFormattedError(language.ERROR_ARGUMENTS);
 			return false;
 		}
 		
 		if(curMine == null) {
 			Message.sendFormattedError(language.ERROR_MINENAME.replaceAll("<ID>", args[1]));
 			return false;
 		}
 		
 		Mine parentMine = curMine.getSuperParent();
 		if(Util.hasPermission("prison.mine.info.*")) {
 			
 			List<String> text = new ArrayList<String>();
 			text.add("");
 			
 			// Title screen
 			try {
 				String line = "";
 				if(!curMine.getName().equals(curMine.getId()) && !curMine.getName().equals("")) line = "[ \"" + ChatColor.GREEN + curMine.getName() + ChatColor.WHITE + "\" (" + ChatColor.RED + ChatColor.BOLD + curMine.getId() + ChatColor.WHITE + ") ]";
 				else line = "[ " + ChatColor.RED + ChatColor.BOLD + curMine.getId() + ChatColor.WHITE + " ]";
 				
 				int padding = (45 - DrawingTools.getTrueLength(line)) / 2;
 				
 				for(int i = 0; i < padding; i++) { line = DrawingTools.LineHorizontal + line + DrawingTools.LineHorizontal; }
 				
 				text.add(DrawingTools.CornerTopLeft + line + DrawingTools.CornerTopRight);
 			} catch (Exception e) { Message.log(Level.SEVERE, "An error occurred while displaying the title"); }
 			
 			// Protection and exceptions
 			try {
 				String line = "[ ";
 				
 				if(curMine.getProtection().contains(Protection.BLOCK_BREAK)) {
 					if(curMine.getBreakBlacklist().getState().equals(BlacklistState.WHITELIST)) line += ChatColor.YELLOW + "Break" + ChatColor.WHITE;
 					else line += ChatColor.GREEN + "Break" + ChatColor.WHITE;
 				} else line += ChatColor.RED + "Break" + ChatColor.WHITE;
 				
 				line += " | ";
 				
 				if(curMine.getProtection().contains(Protection.PVP)) {
 					line += ChatColor.GREEN + "PvP" + ChatColor.WHITE;
 				} else line += ChatColor.RED + "PvP" + ChatColor.WHITE;
 
 				line += " | ";
 				
 				if(curMine.getProtection().contains(Protection.BLOCK_PLACE)) {
 					if(curMine.getPlaceBlacklist().getState().equals(BlacklistState.WHITELIST)) line += ChatColor.YELLOW + "Place" + ChatColor.WHITE;
 					else line += ChatColor.GREEN + "Place" + ChatColor.WHITE;
 				} else line += ChatColor.RED + "Place" + ChatColor.WHITE;
 				
 				line += " ]      ";
 				
 				BlacklistState blState = curMine.getBlacklist().getState();
 				if(!blState.equals(BlacklistState.DISABLED)) line += "[ " + ChatColor.GREEN + "Exc." + ChatColor.WHITE + " ]";
 				else line += "[ " + ChatColor.RED + "Blacklist" + ChatColor.WHITE + " ]";
 
 				int padding = (60 - DrawingTools.getTrueLength(line)) / 2;
 				for(int i = 0; i < padding; i++) { line = DrawingTools.WhiteSpace + line + DrawingTools.WhiteSpace; }
 				
 				text.add(line);
 				
 			} catch (Exception e) { Message.log(Level.SEVERE, "An error occurred while displaying the protection information"); }
 			
 			// Triggers
 			try {				
 				String line = "";
 				
 				boolean automaticReset = parentMine.getAutomaticReset();
 				boolean compositionReset = curMine.getCompositionReset();
 								
 				if(automaticReset) line += "[ " + ChatColor.GREEN + Util.secondsToTime(parentMine.getResetsInSafe()) + ChatColor.WHITE + " / " + ChatColor.RED + Util.secondsToTime(parentMine.getResetPeriodSafe()) + ChatColor.WHITE + " ]";
 				if(automaticReset && compositionReset) line += "     ";
				if(compositionReset) line += "[ " + ChatColor.GREEN + Util.formatDecimal(curMine.getCurrentPercent()) + ChatColor.WHITE + " / " + ChatColor.RED + curMine.getRequiredPercent() + "%" + ChatColor.WHITE + " ]";
 				
 				int padding = (60 - DrawingTools.getTrueLength(line)) / 2;
 				for(int i = 0; i < padding; i++) { line = DrawingTools.WhiteSpace + line + DrawingTools.WhiteSpace; }
 				
 				text.add(line);
 					
 			} catch (Exception e) { Message.log(Level.SEVERE, "An error occurred while displaying the trigger information"); }
 			
 			// Parent
 			try {
 				if(curMine.hasParent()) {
 					String line = "Extends " + ChatColor.RED + curMine.getParent();
 					int padding = (60 - DrawingTools.getTrueLength(line)) / 2;
 					for(int i = 0; i < padding; i++) { line = DrawingTools.WhiteSpace + line + DrawingTools.WhiteSpace; }
 					text.add(line);
 				}
 			} catch (Exception e) { Message.log(Level.SEVERE, "An error occurred while displaying the protection information"); }
 			
 			// Warnings
 			try {
 				if(parentMine.hasWarnings()) {
 					String line = ChatColor.YELLOW + "   Warnings: " + ChatColor.WHITE;
 					List<Integer> warnings = parentMine.getLocalWarningTimes();
 					line += Util.secondsToTime(warnings.get(0));
 					if(warnings.size() > 1) {
 						for(int i = 1; i < warnings.size(); i++) {
 							line += ", " + Util.secondsToTime(warnings.get(i));
 						}
 					}
 					text.add(line);
 				}
 			} catch (Exception e) { Message.log(Level.SEVERE, "An error occurred while displaying the warning information"); }
 			
 			// Children
 			try {
 				List<Mine> children = curMine.getChildren();
 				if(children.size() != 0) {
 					String line = "";
 					line = ChatColor.YELLOW + "   Children:" + ChatColor.WHITE;
 					line += " " + children.get(0).getId();
 					if(children.size() > 1) {
 						for(int i = 1; i < children.size(); i++) {
 							line += ", " + children.get(i).getId();
 						}
 					}
 					text.add(line);
 				}
 			} catch (Exception e) { Message.log(Level.SEVERE, "An error occurred while displaying the children"); }
 			
 			// Flags
 			try {
 				List<BaseFlag> flags = curMine.getAllFlags();
 				if(flags.size() != 0) {
 					text.add(ChatColor.YELLOW + "   Flags:" + ChatColor.WHITE);
 					for(BaseFlag flag : flags) {
 						String str = flag.getName();
 						if(!flag.getOption().equals("")) str += " (" + flag.getOption() + ")";
 						text.add("        " + str);
 					}
 				}
 			} catch (Exception e) { Message.log(Level.SEVERE, "An error occurred while displaying the flags"); }
 			
 			// Cooldown
 			try {
 				if(curMine.getCooldown()) {
 					text.add(ChatColor.YELLOW + "   Cooldown: " + ChatColor.GREEN + Util.secondsToTime(curMine.getCooldownEndsIn()) + ChatColor.WHITE + " / " + ChatColor.RED + Util.secondsToTime(curMine.getCooldownPeriod()));
 				}
 			} catch (Exception e) { Message.log(Level.SEVERE, "An error occurred while displaying the flags"); }
 			
 			// Mine composition
 			try {
 				text.add(ChatColor.YELLOW + "   Composition:" + ChatColor.WHITE);
 				List<String> finalList = curMine.getBlocksSorted();
 				for(int i = 0; i < (finalList.size() - 1); i += 2) {
 					int spaces = 10;
 					String line = finalList.get(i);
 					if(line.length() > 20) spaces -= (line.length() - 20);
 					else if(line.length() < 20) spaces += (20 - line.length());
 					
 					line = "        " + line;
 					for(int j = 0; j < spaces; j++) line += " ";
 					line += finalList.get(i + 1);
 					text.add(line);
 				}
 				if(finalList.size() % 2 != 0) text.add("        " + finalList.get(finalList.size() - 1));
 			} catch (Exception e) { Message.log(Level.SEVERE, "An error occurred while displaying the mine composition"); }
 			
 			// Blacklist composition
 			try {
 				Blacklist blacklist = curMine.getBlacklist();
 				List<MaterialData> blocks = blacklist.getBlocks();
 				
 				if(!blacklist.getState().equals(BlacklistState.DISABLED) && !blocks.isEmpty()) {
 					if(blacklist.getState().equals(BlacklistState.BLACKLIST)) text.add(ChatColor.YELLOW + "   Reset exemptions (blacklist): ");
 					else if(blacklist.getState().equals(BlacklistState.WHITELIST)) text.add(ChatColor.YELLOW + "   Reset exemptions (whitelist): ");
 					for(MaterialData block : blocks) {
 						String[] parts = {block.getItemTypeId() + "", block.getData() + ""};
 						String metadata = Util.parseMetadata(parts, true);
 						String itemtype = block.getItemType().toString().toLowerCase().replace("_", " ");
 						if(metadata.equals("")) text.add("        " + itemtype);
 						else text.add("        " + metadata + " " + itemtype);
 					}
 				}
 			} catch (Exception e) { Message.log(Level.SEVERE, "An error occurred while displaying the blacklist status"); }
 			
 			// Break protection
 			try {
 				Blacklist blacklist = curMine.getBreakBlacklist();
 				List<MaterialData> blocks = blacklist.getBlocks();
 				
 				if(!blacklist.getState().equals(BlacklistState.DISABLED) && !blocks.isEmpty()) {
 					if(blacklist.getState().equals(BlacklistState.BLACKLIST)) text.add(ChatColor.YELLOW + "   Block breaking protection (blacklist): ");
 					else if(blacklist.getState().equals(BlacklistState.WHITELIST)) text.add(ChatColor.YELLOW + "   Block breaking protection (whitelist): ");
 					for(MaterialData block : blocks) {
 						String[] parts = {block.getItemTypeId() + "", block.getData() + ""};
 						String metadata = Util.parseMetadata(parts, true);
 						String itemtype = block.getItemType().toString().toLowerCase().replace("_", " ");
 						if(metadata.equals("")) text.add("        " + itemtype);
 						else text.add("        " + metadata + " " + itemtype);
 					}
 				}
 			} catch (Exception e) { Message.log(Level.SEVERE, "An error occurred while displaying the break protection status"); }
 			
 			// Place protection
 			try {
 				Blacklist blacklist = curMine.getPlaceBlacklist();
 				List<MaterialData> blocks = blacklist.getBlocks();
 				
 				if(!blacklist.getState().equals(BlacklistState.DISABLED) && !blocks.isEmpty()) {
 					if(blacklist.getState().equals(BlacklistState.BLACKLIST)) text.add(ChatColor.YELLOW + "   Block breaking protection (blacklist): ");
 					else if(blacklist.getState().equals(BlacklistState.WHITELIST)) text.add(ChatColor.YELLOW + "   Block breaking protection (whitelist): ");
 					for(MaterialData block : blocks) {
 						String[] parts = {block.getItemTypeId() + "", block.getData() + ""};
 						String metadata = Util.parseMetadata(parts, true);
 						String itemtype = block.getItemType().toString().toLowerCase().replace("_", " ");
 						if(metadata.equals("")) text.add("        " + itemtype);
 						else text.add("        " + metadata + " " + itemtype);
 					}
 				}
 			} catch (Exception e) { Message.log(Level.SEVERE, "An error occurred while displaying the place protection status"); }
 						
 			for(String line : text) {
 				Message.send(line);
 			}
 			
 			return true;
 			
 		} else if(Util.hasPermission("prison.mine.info.time")) {
 			String displayString = "---==[ " + ChatColor.GREEN + ChatColor.BOLD + curMine.getName() + ChatColor.WHITE + " ]==---";
 			for(int i = 0; i < 25 - (curMine.getName().length() / 2); i++) displayString = " " + displayString;
 			Message.send(displayString);
 			Message.send("");
 			
 			if(parentMine.getAutomaticReset())
 				Message.send("    Resets every ->  " + ChatColor.GREEN + Util.secondsToTime(parentMine.getResetPeriodSafe()) + "    " + ChatColor.GOLD + Util.secondsToTime(parentMine.getResetsInSafe()) + ChatColor.WHITE + "  <- Next Reset");
 			else Message.send("   Mine has to be reset manually");
 			
 		} else {
 			Message.sendFormattedError(language.ERROR_ACCESS);
 			return false;
 		}
 		return false;
 	}
 	
 	@Override
 	public void getHelp() {
 		Message.formatHelp("info", "<name>", "Shows the basic mine information", "prison.mine.info.time");
 		if(Util.hasPermission("prison.mine.info.*")) Message.formatMessage("Displays information about mine composition and reset times");
 	}
 	
 	@Override
 	public void getHelpLine() { Message.formatHelp("info", "<name>", "Shows the basic mine information", "prison.mine.info.time"); }
 }
