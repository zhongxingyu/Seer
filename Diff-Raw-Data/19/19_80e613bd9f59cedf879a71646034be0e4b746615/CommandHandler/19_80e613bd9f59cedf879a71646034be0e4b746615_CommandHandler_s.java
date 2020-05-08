 package me.everdras.wordfilter.command;
 
 import java.io.PrintStream;
 import java.util.ArrayList;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 
 import me.everdras.core.command.CommandSenderWrapper;
 import me.everdras.wordfilter.WordFilterPlugin;
 import me.everdras.wordfilter.permission.Permissions;
 import me.everdras.wordfilter.util.Config;
 import me.everdras.wordfilter.util.FilterMode;
 import me.everdras.wordfilter.util.WordList;
 import me.everdras.wordfilter.util.WordMap;
 import static me.everdras.core.chat.ChatUtil.*;
 
 public class CommandHandler {
 	private CommandSenderWrapper senderWrapper;
 	private WordList black, fuzzy, white;
 	private Config config;
 	private WordMap repMap;
 
 	public CommandHandler(WordFilterPlugin p, CommandSender sender) {
 		senderWrapper = new CommandSenderWrapper(sender);
 
 		this.black = p.getBlack();
 		this.white = p.getWhite();
 		this.fuzzy = p.getFuzzy();
 		this.config = p.getTextConfig();
 		this.repMap = p.getReplacementMap();
 		
 	}
 
 	public void addWordToList(String list, String word) {
 		if (!senderWrapper.hasExternalPermissions(Permissions.MUTATE_LISTS
 				.toString())) {
 			senderWrapper.notifyInsufPermissions();
 			return;
 		}
 
 		switch (list.toLowerCase()) {
 		case "fuzzy":
 			fuzzy.addWord(word);
 			senderWrapper.sendMessage(SUCC + "Word added.");
 			break;
 
 		case "white":
 			white.addWord(word);
 			senderWrapper.sendMessage(SUCC + "Word added.");
 			break;
 
 		case "black":
 			black.addWord(word);
 			senderWrapper.sendMessage(SUCC + "Word added.");
 			break;
 
 		default:
 			senderWrapper.sendMessage(ERR
 					+ "Invalid list. Valid lists are: white, black, fuzzy.");
 		}
 	}
 
 	public void removeWordFromList(String list, String word) {
 		if (!senderWrapper.hasExternalPermissions(Permissions.MUTATE_LISTS
 				.toString())) {
 			senderWrapper.notifyInsufPermissions();
 			return;
 		}
 
 		switch (list.toLowerCase()) {
 		case "fuzzy":
 			fuzzy.removeWord(word);
 			senderWrapper.sendMessage(SUCC + "Word removed.");
 			break;
 
 		case "white":
 			white.removeWord(word);
 			senderWrapper.sendMessage(SUCC + "Word removed.");
 			break;
 
 		case "black":
 			black.removeWord(word);
 			senderWrapper.sendMessage(SUCC + "Word removed.");
 			break;
 
 		default:
 			senderWrapper.sendMessage(ERR
					+ "Invalid list. Valid lists are: white, black, fuzzy.");
 		}
 	}
 
 	public void listWordsInList(String list) {
 		if (!senderWrapper.hasExternalPermissions(Permissions.MUTATE_LISTS
 				.toString())) {
 			senderWrapper.notifyInsufPermissions();
 			return;
 		}
 
 		WordList selected;
 		switch (list.toLowerCase()) {
 		case "fuzzy":
 			selected = fuzzy;
 			break;
 
 		case "white":
 			selected = white;
 			break;
 
 		case "black":
 			selected = black;
 			break;
 
 		default:
 			senderWrapper.sendMessage(ERR
 					+ "Invalid list. Valid lists are: white, black, fuzzy.");
 			return;
 		}
 
 		senderWrapper.sendMessage(INFO
 				+ "The selected list contains these words:");
 		int count = 0;
 		String tempMsg = "";
 		for (String word : selected.getWords()) {
 			if (count < 5) {
 				tempMsg += word;
 				tempMsg += " ";
 				count++;
 			} else {
 				count = 0;
 				senderWrapper.sendMessage(tempMsg);
 				tempMsg = "";
 			}
 		}
 		if (tempMsg.length() != 0)
 			senderWrapper.sendMessage(tempMsg);
 
 	}
 
 	public void mutateConfigValue(String key, String value) {
 		if (!senderWrapper.hasExternalPermissions(Permissions.MUTATE_CONFIG
 				.toString())) {
 			senderWrapper.notifyInsufPermissions();
 			return;
 		}
 
 		try {
 			switch (key.toLowerCase()) {
 			case "similaritythreshold":
 				config.setSimilarityThreshold(Double.parseDouble(value));
 				senderWrapper.sendMessage(SUCC + "Config updated.");
 				senderWrapper.sendMessage(INFO
 						+ "Reload the plugin to apply changes.");
 				break;
 
 			case "debugging":
 				config.setDebugging(Boolean.parseBoolean(value));
 				WordFilterPlugin.log.setDebugging(Boolean.parseBoolean(value));
 				senderWrapper.sendMessage(SUCC + "Config updated.");
 				break;
 
 			case "filtermode":
 				config.setFilterMode(FilterMode.parseMode(value));
 				senderWrapper.sendMessage(SUCC + "Config updated.");
 				senderWrapper.sendMessage(INFO
 						+ "Reload the plugin to apply changes.");
 				break;
 
 			case "violationmessage":
 				senderWrapper
 						.sendMessage(INFO
 								+ "The violation message can only be set via the text config file for now.");
 				break;
 			default:
 				senderWrapper
 						.sendMessage("Invalid key. Valid keys are: similarityThreshold, debugging, filterMode, violationMessage.");
 			}
 		} catch (Exception e) {
 			senderWrapper.sendMessage(ERR + "Error: invalid value \"" + value
 					+ "\" for key \"" + key + "\": " + e.getMessage());
 		}
 	}
 
 	public void addMapping(String string, String string2) {
 		if(!senderWrapper.hasExternalPermissions(Permissions.MUTATE_LISTS.toString())) {
 			senderWrapper.notifyInsufPermissions();
 			return;
 		}
 		
 		repMap.addMapping(string, string2);
 		senderWrapper.sendMessage(SUCC + "Successfully mapped " + string + " to " + string2 + ".");
 	}
 
 	public void clearMapping(String string) {
 		if(!senderWrapper.hasExternalPermissions(Permissions.MUTATE_LISTS.toString())) {
 			senderWrapper.notifyInsufPermissions();
 			return;
 		}
 		
 		repMap.clearMapping(string);
 		senderWrapper.sendMessage(SUCC + "Successfully cleared the mapping for " + string + ".");
 	}
 	
 	public void listMappings() {
 		if(!senderWrapper.hasExternalPermissions(Permissions.MUTATE_LISTS.toString())) {
 			senderWrapper.notifyInsufPermissions();
 			return;
 		}
 		
 		senderWrapper.sendMessage(INFO + "Existing mappings:");
 		
 		ArrayList<String> maps = repMap.toStringList();
 		
 		for(String s : maps) {
 			senderWrapper.sendMessage(ChatColor.YELLOW + s);
 		}
 	}
 }
