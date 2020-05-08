 package com.bukkit.Magick.ARGAntiPirate;
 
 import java.io.File;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.plugin.PluginDescriptionFile;
 
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class ARGAntiPirate extends JavaPlugin {
 
 	private final ARGAntiPiratePlayerListener	playerListener	= new ARGAntiPiratePlayerListener(this);
 	private final ARGAntiPirateBlockListener	blockListener	= new ARGAntiPirateBlockListener(this);
 	static String								maindirectory	= "argantipirate/";
 	static File									ChestLogger		= new File(maindirectory + "Chest.log");
 	public static ARG_Rank						rankMachine		= new ARG_Rank();
 	public static ARG_ThiefProtect				chestMachine	= new ARG_ThiefProtect();
 	public ARGAntiPirate						plugin;
 
 	@Override
 	public void onEnable() {
 		PluginManager pm = getServer().getPluginManager();
 		pm.registerEvent(Event.Type.PLAYER_CHAT, this.playerListener, Priority.Low, this);
 		pm.registerEvent(Event.Type.PLAYER_LOGIN, this.playerListener, Priority.Low, this);
 		pm.registerEvent(Event.Type.BLOCK_PLACED, this.blockListener, Priority.Lowest, this);
 		pm.registerEvent(Event.Type.BLOCK_INTERACT, this.blockListener, Priority.Lowest, this);
 		pm.registerEvent(Event.Type.BLOCK_DAMAGED, this.blockListener, Priority.Lowest, this);
 		pm.registerEvent(Event.Type.BLOCK_BREAK, this.blockListener, Priority.Lowest, this);
 		PluginDescriptionFile pdfFile = this.getDescription();
 		System.out.println(pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled!");
 
 		chestMachine.Load();
		
 		rankMachine.Load();
 	}
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
 		String[] trimmedArgs = args;
 		String commandName = command.getName().toLowerCase();
 		Player p = (Player) sender;
 		int rank = rankMachine.getRank(p);
 		if (rank == 5) {
 			// admin commands
 			if (commandName.equals("adduser")) {
 				if (rankMachine.setRank(args) > -1) {
 					sender.sendMessage(trimmedArgs[0] + " has been given rank " + args[1]);
 					return true;
 				} else {
 					sender.sendMessage(ChatColor.RED + "Player already exists");
 					return true;
 				}
 			}
 		}
 		// user commands
 		if (rank >= 1) {
 			if (commandName.equals("unlock")) {
 				return chestMachine.unlockIt(p);
 			}
 		} else {
 			sender.sendMessage(ChatColor.RED + "You do not have permission to do that.");
 			return true;
 		}
 		return false;
 	}
 
 	@Override
 	public void onDisable() {
 		chestMachine.Save();
		
 		rankMachine.Save();
 		System.out.println("ARGAntiPirate Disabeled.");
 	}
 }
