 package com.alta189.minemail;
 
 import java.io.File;
 import java.util.logging.Logger;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.alta189.minemail.addons.PermissionsHandler;
 import com.alta189.minemail.command.CommandHandler;
 import com.alta189.sqllitelib.sqlCore;
 
 public class MineMail extends JavaPlugin {
 	
 	//Declare all the basic objects\\
 	public final Logger log = Logger.getLogger("Minecraft");
 	public String version = "1.0";
 	public File pFolder = new File("plugins/MineMail");
 	public String logPrefix = "[MineMail] ";
 	
 	//Declare all of the Handlers\\
 	public sqlCore dbManage;
 	public PermissionsHandler PermManager = new PermissionsHandler(this);
 	public CommandHandler command = new CommandHandler(this);
	public MailServer mmServer = new MailServer(this);
 	
 	//Declare any other variables\\
 	public Boolean ScheduledWipe = false;
 	public int DelayWipeTime = 60; //Time in seconds to delay the wipe
 	
 	@Override
 	public void onDisable() {
 		// TODO Auto-generated method stub
 		
 	}
 	
 	@Override
 	public void onEnable() {
 		// TODO Auto-generated method stub
 		
 	}
 	
 	//Command Executer\\
 	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
 		Player player = (Player) sender;
 		if (commandLabel.equalsIgnoreCase("mail")) {
 			 if (args.length >= 1) {
 				 	if (args[0].equalsIgnoreCase("read") && player != null) {
 				 		this.command.read(player, cmd, commandLabel, args);
 				 	} else if (args[0].equalsIgnoreCase("write") && player != null) {
 				 		if (args.length >= 3) {
 				 			this.command.write(player, cmd, commandLabel, args);
 				 		} else {
 				 			player.sendMessage(ChatColor.RED + "/mm write <player name> <message>"); 
 				 		}
 				 	} else if (args[0].equalsIgnoreCase("help") && player != null) {
 				 		this.command.help(player, cmd, commandLabel, args);
 				 	} else if (args[0].equalsIgnoreCase("admin") && player != null) {
 				 		this.command.admin(player, cmd, commandLabel, args);
 				 	} else if (args[0].equalsIgnoreCase("wipe") && player != null) {
 				 		this.command.wipe(player, cmd, commandLabel, args);
 				 	} else if (args[0].equalsIgnoreCase("reload") && player != null) {
 				 		this.command.reload(player, cmd, commandLabel, args);
 				 	}
 			 }
 		}
 		return false;
 	}
 	
 	public Boolean isAdmin(Player player, String type) { //Handles Permissions\OP access to commands
 		if (type.contains("/")) {
 			for (String subType : type.split("/")) {
 				if (player.isOp() || PermManager.hasPermissions(player, "admin") || PermManager.hasPermissions(player, subType)) {
 					return true;
 				}
 			}
 		} else {
 			if (player.isOp() || PermManager.hasPermissions(player, "admin") || PermManager.hasPermissions(player, type)) {
 				return true;
 			}
 		}
 		
 		return false;
 	}
 	
 	public void createPluginFolder() { //This will create the plugin folder so that we can store stuff in it!
 		if (!this.pFolder.exists()) {
 			pFolder.mkdir();
 		}
 	}
 
 	public Player getPlayer(String playername) { //This will get the player from his/her name
 		Player player = null;
 		
 		for (Player checkPlayer : this.getServer().getOnlinePlayers()) { 
 			//for each player in online player do this 
 			if (checkPlayer.getName().equalsIgnoreCase(playername)) {
 				player = checkPlayer;
 			}
 		}
 		
 		return player;
 	}
 	
 	public void notifyReceiver(String playername) { //This is an easy way to notify the player when he gets a message
 		Player receiver = this.getPlayer(playername);
 		if (receiver != null) {
 			receiver.sendMessage(ChatColor.GREEN + "MineMail - You got a message");
 		}
 	}
 }
