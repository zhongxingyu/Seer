 package com.wolvencraft.prison.mines.cmd;
 
 import java.util.List;
 
 import org.bukkit.ChatColor;
 import org.bukkit.material.MaterialData;
 
 import com.wolvencraft.prison.mines.PrisonMine;
 import com.wolvencraft.prison.mines.mine.Mine;
 import com.wolvencraft.prison.mines.mine.MineFlag;
 import com.wolvencraft.prison.mines.mine.Protection;
 import com.wolvencraft.prison.mines.settings.Language;
 import com.wolvencraft.prison.mines.util.Message;
 import com.wolvencraft.prison.mines.util.Util;
 
 public class InfoCommand  implements BaseCommand {
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
 			
 			Message.send("");
 			String displayString = "---==[ " + ChatColor.GREEN + ChatColor.BOLD + curMine.getName() + ChatColor.WHITE + " ]==---";
 			for(int i = 0; i < 25 - (curMine.getName().length() / 2); i++) displayString = " " + displayString;
 			Message.send(displayString);
 			Message.send("");
 			
 			// Block & PVP protection
 			String str = "    [ ";
 			if(curMine.getProtection().contains(Protection.BLOCK_BREAK)) {
 				if(curMine.getBreakBlacklist().getWhitelist()) str += ChatColor.YELLOW;
 				else str += ChatColor.GREEN;
 			}
 			else str += ChatColor.RED;
 			str += "Block Breaking" + ChatColor.WHITE + " ]     [ ";
 			if(curMine.getProtection().contains(Protection.PVP)) str += ChatColor.GREEN;
 			else str += ChatColor.RED;
 			str += "PVP" + ChatColor.WHITE + " ]    [ ";
 			if(curMine.getProtection().contains(Protection.BLOCK_PLACE)) {
 				if(curMine.getPlaceBlacklist().getWhitelist()) str += ChatColor.YELLOW;
 				else str += ChatColor.GREEN;
 			}
 			else str += ChatColor.RED;
 			str += "Block Placement" + ChatColor.WHITE + " ]";
 			Message.send(str);
 			Message.send("");
 			
 			// Timer and Composition triggers information
 			boolean automaticReset = parentMine.getAutomaticReset();
 			boolean compositionReset = curMine.getCompositionReset();
 			
 			if(automaticReset || compositionReset) {
 				String fillerString = "";
 				
 				if(automaticReset) {
 					if(!compositionReset) fillerString += "               ";
 					fillerString += "       [ " + ChatColor.GREEN + Util.parseSeconds(parentMine.getResetsInSafe()) + ChatColor.WHITE + " | " + ChatColor.RED + Util.parseSeconds(parentMine.getResetPeriodSafe()) + ChatColor.WHITE + " ]";
 				} else {
 					fillerString += "               ";
 				}
 				
 				if(compositionReset) {
 					fillerString += "       [ " + ChatColor.GREEN + curMine.getCurrentPercent() + "%" + ChatColor.WHITE + " | " + ChatColor.RED + curMine.getRequiredPercent() + "%" + ChatColor.WHITE + " ]";
 				}
 				
 				Message.send(fillerString);
 				Message.send("");
 			}
 			
 			if(parentMine.getWarned() && !parentMine.getWarningTimes().isEmpty()) {
 				String fillerString = "";
 				for(Integer warning : parentMine.getWarningTimes()) {
 					if(!fillerString.equals("")) fillerString += ",";
 					fillerString += " " + Util.parseSeconds(warning);
 				}
 				fillerString = ChatColor.YELLOW + "   Warnings: " + ChatColor.WHITE + fillerString;
 				Message.send(fillerString);
 				Message.send("");
 			}
 			
 			// Children and flags
 			List<Mine> children = curMine.getChildren();
 			if(children.size() != 0) {
 				str = ChatColor.YELLOW + "   Children:" + ChatColor.WHITE;
 				str += " " + children.get(0);
 				if(children.size() > 1) {
 					for(int i = 1; i < children.size(); i++) {
						str += ", " + children.get(i).getId();
 					}
 				}
 				Message.send(str);
 				Message.send("");
 			}
 			
 			List<MineFlag> flags = curMine.getFlags();
 			if(flags.size() != 0) {
 				str = ChatColor.YELLOW + "   Flags:" + ChatColor.WHITE;
 				str += " " + flags.get(0);
 				if(flags.size() > 1) {
 					for(int i = 1; i < flags.size(); i++) {
 						str +=  ", " + flags.get(i);
 					}
 				}
 				Message.send(str);
 				Message.send("");
 			}
 			
 			// Generator & parent mine
 			str = ChatColor.YELLOW + "   Composition:" + ChatColor.WHITE;
 			String parentName;
 			if(curMine.getParent() == null)
 				parentName = "none";
 			else parentName = curMine.getParent();
 			for(int i = 0; i < (25 - parentName.length()); i++) str += " ";
 			str += ChatColor.WHITE + "Linked to: " + ChatColor.GOLD + parentName;
 			Message.send(str);
 			
 			// Mine composition
 			List<String> finalList = curMine.getBlocksSorted();
 			for(int i = 0; i < (finalList.size() - 1); i += 2) {
 				int spaces = 10;
 				String line = finalList.get(i);
 				if(line.length() > 25) spaces -= (line.length() - 25);
 				else if(line.length() < 25) spaces += (25 - line.length());
 				
 				str = "        " + line;
 				for(int j = 0; j < spaces; j++) str += " ";
 				str += finalList.get(i + 1);
 				Message.send(str);
 			}
 			if(finalList.size() % 2 != 0) Message.send("        " + finalList.get(finalList.size() - 1));
 			
 			Message.send(" ");
 			boolean enabled = curMine.getBlacklist().getEnabled();
 			boolean whitelist = curMine.getBlacklist().getWhitelist();
 			List<MaterialData> blocks = curMine.getBlacklist().getBlocks();
 			
 			str = "                 [ ";
 			if(enabled) str += ChatColor.GREEN;
 			else str += ChatColor.RED;
 			str += "Blacklist" + ChatColor.WHITE + " ]       [ ";
 			if(whitelist) str += ChatColor.GREEN;
 			else str += ChatColor.RED;
 			str += "Whitelist" + ChatColor.WHITE + " ]";
 			Message.send(str);
 			if(!blocks.isEmpty()) {
 				Message.send(ChatColor.BLUE + "    Blacklist Composition: ");
 				for(MaterialData block : blocks) {
 					String[] parts = {block.getItemTypeId() + "", block.getData() + ""};
 					Message.send("        - " + Util.parseMetadata(parts, true) + " " + block.getItemType().toString().toLowerCase().replace("_", " "));
 				}
 			}
 			Message.send(" ");
 			
 			return true;
 			
 		} else if(Util.hasPermission("prison.mine.info.time")) {
 			String displayString = "---==[ " + ChatColor.GREEN + ChatColor.BOLD + curMine.getName() + ChatColor.WHITE + " ]==---";
 			for(int i = 0; i < 25 - (curMine.getName().length() / 2); i++) displayString = " " + displayString;
 			Message.send(displayString);
 			Message.send("");
 			
 			if(parentMine.getAutomaticReset())
 				Message.send("    Resets every ->  " + ChatColor.GREEN + Util.parseSeconds(parentMine.getResetPeriodSafe()) + "    " + ChatColor.GOLD + Util.parseSeconds(parentMine.getResetsInSafe()) + ChatColor.WHITE + "  <- Next Reset");
 			else Message.send("   Mine has to be reset manually");
 			
 		} else {
 			Message.sendFormattedError(language.ERROR_ACCESS);
 			return false;
 		}
 		return false;
 	}
 	
 	public void getHelp() {
 		Message.formatHelp("info", "<name>", "Shows the basic mine information", "prison.mine.info.time");
 		if(Util.hasPermission("prison.mine.info.*")) Message.formatMessage("Displays information about mine composition and reset times");
 	}
 	
 	public void getHelpLine() { Message.formatHelp("info", "<name>", "Shows the basic mine information", "prison.mine.info.time"); }
 }
