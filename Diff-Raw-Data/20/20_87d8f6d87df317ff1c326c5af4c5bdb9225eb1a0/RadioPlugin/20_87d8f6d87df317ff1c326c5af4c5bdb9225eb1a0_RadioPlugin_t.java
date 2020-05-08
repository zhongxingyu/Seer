 package io.github.LilParker.RadioPlugin;
 
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class RadioPlugin extends JavaPlugin {
 	
 	public void onEnable () {
 		
		RadioPluginCommandExecuter executer = new RadioPluginCommandExecuter(this);
		getCommand("getfreq").setExecutor(executer);
		getCommand("setfreq").setExecutor(executer);
 	}
 }
