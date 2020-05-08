 package com.github.mineguild.MineguildAdmin;
 
 
 public class Main extends org.bukkit.plugin.java.JavaPlugin {
 //Calling MGACommandExecutor as Executor
 private MGACommandExecutor myExecutor;
 @Override
 //Enabling Plugin
 public void onEnable(){
 myExecutor = new MGACommandExecutor (null);
 //Sending commands to MGACommandExecutor
getCommand("mga").setExecutor(myExecutor);
 //Send message to console
 getLogger().info("MineguildAdmin has been enabled!");
 }
 //Disabling Plugin
 public void onDisable(){
 	getLogger().info("MineguildAdmin has been disabled!");
 }
 }
