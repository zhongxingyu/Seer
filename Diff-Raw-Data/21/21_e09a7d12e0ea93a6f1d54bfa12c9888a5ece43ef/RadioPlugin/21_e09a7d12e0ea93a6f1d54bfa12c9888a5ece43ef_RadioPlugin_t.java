 package io.github.LilParker.RadioPlugin;
 
import java.util.Map;
import java.util.Map.Entry;
 
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class RadioPlugin extends JavaPlugin {
 	
 	@Override
 	public void onEnable () {
 		
		Map<String, Object> configmap = getConfig().getConfigurationSection("playerFreqs").getValues(false);
		for(Entry entry : configmap.entrySet()){
			Double val = (Double)entry.getValue();
			RadioPluginCommandExecuter.playerFreqs.put((String) entry.getKey(), val.floatValue());
		}
		
		Map<String, Object> eKeymap = getConfig().getConfigurationSection("eKey").getValues(false);
		for(Entry entry : eKeymap.entrySet()){
			RadioPluginCommandExecuter.eKey.put((String) entry.getKey(), (String) entry.getValue());
		}
		
 		RadioPluginCommandExecuter executer = new RadioPluginCommandExecuter(this);
 		
 		getCommand("getfreq").setExecutor(executer);
 		getCommand("setfreq").setExecutor(executer);
 		getCommand("radio").setExecutor(executer);
 		getCommand("setekey").setExecutor(executer);
 		getCommand("getekey").setExecutor(executer);
 	
 	}
 
 	@Override	
 	public void onDisable () {
		getConfig().createSection("playerFreqs", RadioPluginCommandExecuter.playerFreqs);
 		getConfig().createSection("eKey", RadioPluginCommandExecuter.eKey);
 		saveConfig();
 	}
 }
