 package com.irrelevantknight.hiddenchat;
 
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public final class HiddenChat extends JavaPlugin {

 	@Override
     public void onEnable() {
 		getServer().getPluginManager().registerEvents(new ServerChatPlayerListener(this), this);
 		PluginDescriptionFile pdfFile = this.getDescription();
 		getLogger().info(pdfFile.getName() + " version " + pdfFile.getVersion() + " has been enabled.");
     }
  
     @Override
     public void onDisable() {
 		PluginDescriptionFile pdfFile = this.getDescription();
 		getLogger().info(pdfFile.getName() + " has been disabled.");
     }
     
 }
