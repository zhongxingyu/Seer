 package com.github.mineguild.MineguildAdmin;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.plugin.PluginDescriptionFile;
 
 public class Main extends org.bukkit.plugin.java.JavaPlugin {
 	
 
 	
 //Calling MGACommandExecutor as Executor
 private MGACommandExecutor CMDEXE;
 @Override
 //Enabling Plugin
 public void onEnable(){
 	//Getting Plugin Description
 	PluginDescriptionFile pdffile = this.getDescription();
 	
 	//Interpreting CMDEXE
 	CMDEXE = new MGACommandExecutor (null);
 	
 	//Sending commands to MGACommandExecutor
 	getCommand("gm").setExecutor(CMDEXE);
 	getCommand("heal").setExecutor(CMDEXE);
 	getCommand("check").setExecutor(CMDEXE);
 	getCommand("feed").setExecutor(CMDEXE);
 	getCommand("spawn").setExecutor(CMDEXE);
 	getCommand("setspawn").setExecutor(CMDEXE);
 	
 	//Send message to console
 	getLogger().info("MineguildAdmin V" + pdffile.getVersion() + " has been enabled!");
 }
 //Disabling Plugin
 public void onDisable(){
 	//Getting Plugin description file
 	PluginDescriptionFile pdffile = this.getDescription();
 	getLogger().info("MineguildAdmin V" + pdffile.getVersion() + " has been disabled!");
 }

 
 
 public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
 	if(cmd.getName().equalsIgnoreCase("mga")){
 		//If the args are length 0 or the args[0] isnt equal "version" it will return false
 		if (args.length == 0 || !args[0].equalsIgnoreCase("version")){
 			return false;
 		}
 		if (args[0].equalsIgnoreCase("version")) {
			PluginDescriptionFile pdffile = this.getDescription();
			String v = pdffile.getVersion();
			//Show version to sender and return true if the value of args[0] is equal to "version"
			sender.sendMessage(ChatColor.AQUA + "[MGA]" + ChatColor.WHITE + "MineguildAdmin" + ChatColor.BLUE + "V" + v);
			return true;
 		}
 		return false;
 	}
 return false;
 }
 }
