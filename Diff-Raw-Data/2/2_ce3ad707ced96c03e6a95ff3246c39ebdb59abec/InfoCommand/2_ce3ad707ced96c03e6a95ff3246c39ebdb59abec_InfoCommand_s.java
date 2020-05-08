 package com.wolvencraft.prison.mines.cmd;
 
 import java.util.List;
 
 import org.bukkit.ChatColor;
 import org.bukkit.material.MaterialData;
 
 import com.wolvencraft.prison.mines.PrisonMine;
 import com.wolvencraft.prison.mines.mine.Mine;
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
 		
		if(args.length > 3) {
 			Message.sendError(language.ERROR_ARGUMENTS);
 			return false;
 		}
 		
 		if(curMine == null) {
 			Message.sendError(language.ERROR_MINENAME.replaceAll("<ID>", args[1]));
 			return false;
 		}
 		
 		Mine parentMine = curMine.getSuperParent();
 		if(Util.hasPermission("mcprison.mine.info.*")) {
 			
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
 			
 			// Timer. Not displayed if it is disabled
 			if(parentMine.getAutomatic())
 				Message.send("    Resets every ->  " + ChatColor.GREEN + Util.parseSeconds(parentMine.getResetPeriodSafe()) + "    " + ChatColor.GOLD + Util.parseSeconds(parentMine.getResetsInSafe()) + ChatColor.WHITE + "  <- Next Reset");
 			
 			// Generator & parent mine
 			str = "    Generator: " + ChatColor.GOLD + curMine.getGenerator();
 			String parentName;
 			if(curMine.getParent() == null)
 				parentName = "none";
 			else parentName = curMine.getParent();
 			for(int i = 0; i < (25 - parentName.length()); i++) str += " ";
 			str += ChatColor.WHITE + "Linked to: " + ChatColor.GOLD + parentName;
 			Message.send(str);
 			
 			List<Mine> children = curMine.getChildren();
 			if(children.size() != 0) {
 				str = "    Children:" + ChatColor.GOLD;
 				for(Mine mine : children) { str += " " + mine.getName(); }
 				Message.send(str);
 			}
 			
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
 			
 		} else if(Util.hasPermission("mcprison.mine.info.time")) {
 			String displayString = "---==[ " + ChatColor.GREEN + ChatColor.BOLD + curMine.getName() + ChatColor.WHITE + " ]==---";
 			for(int i = 0; i < 25 - (curMine.getName().length() / 2); i++) displayString = " " + displayString;
 			Message.send(displayString);
 			Message.send("");
 			
 			if(parentMine.getAutomatic())
 				Message.send("    Resets every ->  " + ChatColor.GREEN + Util.parseSeconds(parentMine.getResetPeriodSafe()) + "    " + ChatColor.GOLD + Util.parseSeconds(parentMine.getResetsInSafe()) + ChatColor.WHITE + "  <- Next Reset");
 			else Message.send("   Mine has to be reset automatically");
 			
 		} else {
 			Message.sendError(language.ERROR_ACCESS);
 			return false;
 		}
 		return false;
 	}
 	
 	public void getHelp() {
 		Message.formatHeader(20, "Information");
 		Message.formatHelp("info", "<name>", "Returns the information about a mine", "mcprison.mine.info.all");
 		return;
 	}
 	
 	public void getHelpLine() {
 		Message.formatHelp("info", "<name>", "Shows the basic mine information", "mcprison.mine.info.time");
 		if(Util.hasPermission("mcprison.mine.info.*")) Message.formatMessage("Displays information about mine composition and reset times");
 	}
 }
