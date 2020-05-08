 package me.everdras.wordfilter;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.Scanner;
 
 import me.everdras.core.command.ArgumentCountException;
 import me.everdras.core.command.ECommand;
 import me.everdras.core.log.MCLogger;
 import me.everdras.wordfilter.command.CommandHandler;
 import me.everdras.wordfilter.listener.ChatListener;
 import me.everdras.wordfilter.permission.Permissions;
 import me.everdras.wordfilter.util.Config;
 import me.everdras.wordfilter.util.PhraseList;
 import me.everdras.wordfilter.util.WordList;
 import me.everdras.wordfilter.util.WordMap;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class WordFilterPlugin extends JavaPlugin {
 	private static final String 
 	ROOT_DIR = "plugins" + File.separator + "WordFilter" + File.separator, 
 			WHITELIST_PATH = ROOT_DIR + "whitelist.txt", 
 			FUZZYLIST_PATH = ROOT_DIR + "fuzzylist.txt",
 			BLACKLIST_PATH = ROOT_DIR + "blacklist.txt", 
 			CONFIG_PATH = ROOT_DIR + "config.txt", 
 			PHRASELIST_PATH = ROOT_DIR + "phrase list.txt", 
 			REPLACEMENTMAP_PATH = ROOT_DIR + "replacement map.txt";
 
 	public static final MCLogger log = new MCLogger("WordFilter: ");
 
 	private WordList black, fuzzy, white;
 	private PhraseList replacements;
 	private Config config;
 	private WordMap replacementMap;
 
 	@Override
     public void onEnable() {
 	try {
 	    checkFiles();
 	} catch (IOException e) {
 	    log.logSevere("Error preparing files: " + e.getMessage());
 	}
 
 	black = new WordList(BLACKLIST_PATH);
 	white = new WordList(WHITELIST_PATH);
 	fuzzy = new WordList(FUZZYLIST_PATH);
 	replacementMap = new WordMap(REPLACEMENTMAP_PATH);
 	replacements = new PhraseList(PHRASELIST_PATH);
 
 	Permissions.registerPermissions(this.getServer().getPluginManager());
 	config = new Config(CONFIG_PATH);
 	
 	log.setDebugging(config.isDebugging());
 	WordList.setSimThresh(config.getSimilarityThreshold());
 	
 	hookListeners();
     }
 
 	public PhraseList getReplacements() {
 		return replacements;
 	}
 
 	@Override
 	public void onDisable() {
 		try {
 			white.persistList();
 			black.persistList();
 			fuzzy.persistList();
 			config.persist();
 			replacements.persistList();
 			replacementMap.persist();
 		} catch (IOException e) {
 			log.logSevere("Error persisting one or more files: "
 					+ e.getMessage());
 		}
 	}
 
 	public WordMap getReplacementMap() {
 		return replacementMap;
 	}
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command command,
 			String label, String[] args) {
 
 		ECommand cmd = new ECommand(label, args);
 		CommandHandler handler = new CommandHandler(this, sender);
 		String helpMessage = null;
 
 		try {
 			WordFilterPlugin.log.logAssert(cmd.get(0).equalsIgnoreCase("wf"),
 					"The first argument in the command was not wf.");
 
			helpMessage = "/wf (add | remove | list | map | config)";
 			switch (cmd.get(1)) {
 			case "add":
 				helpMessage = "/wf add <listname> <word>";
 				handler.addWordToList(cmd.get(2), cmd.get(3));
 				break;
 
 			case "remove":
 			case "rm":
 				helpMessage = "/wf remove <listname> <word>";
 				handler.removeWordFromList(cmd.get(2), cmd.get(3));
 				break;
 
 			case "list":
 			case "ls":
 				helpMessage = "/wf list <listname>";
 				handler.listWordsInList(cmd.get(2));
 				break;
 
 			case "config":
 				helpMessage = "/wf config <key> <new value>";
 				handler.mutateConfigValue(cmd.get(2), cmd.get(3));
 				break;
 
 			case "map":
 				helpMessage = "/wf map <word> (optional: replacement)";
 				if (!cmd.hasArgAtIndex(3))
 					handler.clearMapping(cmd.get(2));
				else
				    handler.addMapping(cmd.get(2), cmd.get(3));
				break;
 
 			default:
 				sender.sendMessage(ChatColor.RED
 						+ "Invalid parameter. Acceptable command parameters are:");
 				sender.sendMessage(ChatColor.AQUA + helpMessage);
 			}
 
 		} catch (ArgumentCountException e) {
 			sender.sendMessage(ChatColor.RED
 					+ "Invalid parameter. Acceptable command parameters are:");
 			sender.sendMessage(ChatColor.AQUA + helpMessage);
 		}
 
 		return true;
 	}
 
 	public WordList getBlack() {
 		return black;
 	}
 
 	public WordList getFuzzy() {
 		return fuzzy;
 	}
 
 	public WordList getWhite() {
 		return white;
 	}
 
 	public Config getTextConfig() {
 		return config;
 	}
 
 	private void checkFiles() throws IOException {
 		File whitelist, blacklist, fuzzylist, root, cfg, phrases, map;
 		whitelist = new File(WHITELIST_PATH);
 		fuzzylist = new File(FUZZYLIST_PATH);
 		blacklist = new File(BLACKLIST_PATH);
 		root = new File(ROOT_DIR);
 		cfg = new File(CONFIG_PATH);
 		phrases = new File(PHRASELIST_PATH);
 		map = new File(REPLACEMENTMAP_PATH);
 
 		if (!root.exists())
 			root.mkdir();
 
 		if (!whitelist.exists())
 			whitelist.createNewFile();
 
 		if (!fuzzylist.exists())
 			fuzzylist.createNewFile();
 
 		if (!blacklist.exists())
 			blacklist.createNewFile();
 
 		if (!cfg.exists()) {
 			cfg.createNewFile();
 		}
 
 		if (!phrases.exists())
 			phrases.createNewFile();
 		
 		if(!map.exists())
 			map.createNewFile();
 	}
 
 	private void hookListeners() {
 		this.getServer().getPluginManager()
 				.registerEvents(new ChatListener(this), this);
 	}
 }
