 package io.github.LilParker.RadioPlugin;
 
import java.util.HashMap;
 
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class RadioPlugin extends JavaPlugin {
 	
 	@Override
 	public void onEnable () {
 		
 		RadioPluginCommandExecuter executer = new RadioPluginCommandExecuter(this);
 		
 		getCommand("getfreq").setExecutor(executer);
 		getCommand("setfreq").setExecutor(executer);
 		getCommand("radio").setExecutor(executer);
 		getCommand("setekey").setExecutor(executer);
 		getCommand("getekey").setExecutor(executer);
 	
 	}
 
 	@Override	
 	public void onDisable () {
		getLogger().info(RadioPluginCommandExecuter.eKey.toString());
 		getConfig().createSection("eKey", RadioPluginCommandExecuter.eKey);
		String test = ((HashMap<String, String>)getConfig().getConfigurationSection("eKey")).toString();
		getLogger().info(test);
 		saveConfig();
 	}
 }
