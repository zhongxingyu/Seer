 package me.lilfade.faded;
 
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class Faded extends JavaPlugin{
	
 	@Override
 	public void onEnable() {
 	//	PluginDescriptionFile pdfFile = this.getDescription();
 	//	this.logger.info(pdfFile.getName() + " Version " + pdfFile.getVersion() + " Has been Enabled!");		
 		PluginManager manager = this.getServer().getPluginManager();
 		manager.registerEvents(new FadedPlayerListener(),  this);
 	}
 	
 	@Override
 	public void onDisable() {
 	//	PluginDescriptionFile pdfFile = this.getDescription();
 	//	this.logger.info(pdfFile.getName() + " Has Been Disabled");
 	}
 }
