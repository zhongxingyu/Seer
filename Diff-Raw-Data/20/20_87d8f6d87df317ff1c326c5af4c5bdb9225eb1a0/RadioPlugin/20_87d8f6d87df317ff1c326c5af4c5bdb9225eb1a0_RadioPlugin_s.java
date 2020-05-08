 package io.github.LilParker.RadioPlugin;
 
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class RadioPlugin extends JavaPlugin {
 	
 	public void onEnable () {
 		
		getCommand("radio getfreq").setExecutor(new RadioPluginCommandExecuter(this));
		getCommand("radio setfreq").setExecutor(new RadioPluginCommandExecuter(this));
 	}
 }
