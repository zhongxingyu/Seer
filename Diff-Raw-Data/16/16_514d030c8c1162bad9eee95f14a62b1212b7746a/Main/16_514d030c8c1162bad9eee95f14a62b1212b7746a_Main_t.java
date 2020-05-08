 package com.github.mineguild.MineguildAdmin;
 
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
 	getCommand("mga").setExecutor(CMDEXE);
 	getCommand("gm").setExecutor(CMDEXE);
 	
 	//Send message to console
 	getLogger().info("MineguildAdmin V" + pdffile.getVersion() + " has been enabled!");
 }
 //Disabling Plugin
 public void onDisable(){
	//Getting Plugin description file
	PluginDescriptionFile pdffile = this.getDescription();
 	getLogger().info("MineguildAdmin V" + pdffile.getVersion() + " has been disabled!");
 }
 }
