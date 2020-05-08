 package io.github.md678685.BukkitPluginMD;
 
 import org.bukkit.Bukkit;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class BukkitPluginMD extends JavaPlugin {
 	
 	public int maxPlayers;
 	public boolean debugOn;
 	public BukkitPluginMDDebugger debugger = new BukkitPluginMDDebugger();
 	public Player[] onlinePlayers;
 	public Player[] playerList;
 	SettingsManager settings = SettingsManager.getInstance();
	BukkitPluginMD plugin = this;
 	
 	@Override
 	public void onEnable(){
 		debugOn = true;
 		getLogger().info("onEnable was invoked in BukkitPluginMD! Most likely BukkitPluginMD was enabled.");
 		onlinePlayers = Bukkit.getOnlinePlayers();
 		maxPlayers = Bukkit.getMaxPlayers();
 		playerList = Bukkit.getOnlinePlayers();
		getCommand("MDcommand").setExecutor(new BukkitPluginMDCommandExecutor(plugin));
		getCommand("MDcommand2").setExecutor(new BukkitPluginMDCommandExecutor(plugin));
		getCommand("MDslap").setExecutor(new BukkitPluginMDCommandExecutor(plugin));
		//getCommand("mdtp").setExecutor(new BukkitPluginMDTPHandler(plugin));
		getCommand("mdmd").setExecutor(new BukkitPluginMDCommandExecutor(plugin));
		settings.setup(plugin);
 		
 		if (debugOn == true) {
 			
 		}
 		//Insert code to run on plugin enabled
 	}
 	
 	@Override
 	public void onDisable(){
 		getLogger().info("onDisable was invoked in BukkitPluginMD! Most likely BukkitPluginMD was disabled.");
 		//Insert code to run on plugin disabled
 	}
 	
 	public void broadcastMsg(String message){
 		getServer().broadcastMessage(message);
 	}
 	
 }
