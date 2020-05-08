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
 	
 	@Override
 	public void onEnable(){
 		debugOn = true;
 		getLogger().info("onEnable was invoked in BukkitPluginMD! Most likely BukkitPluginMD was enabled.");
 		onlinePlayers = Bukkit.getOnlinePlayers();
 		maxPlayers = Bukkit.getMaxPlayers();
 		playerList = Bukkit.getOnlinePlayers();
 		getCommand("MDcommand").setExecutor(new BukkitPluginMDCommandExecutor(this));
 		getCommand("MDcommand2").setExecutor(new BukkitPluginMDCommandExecutor(this));
 		getCommand("MDslap").setExecutor(new BukkitPluginMDCommandExecutor(this));
 		getCommand("mdtp").setExecutor(new BukkitPluginMDTPHandler(this));
		getCommand("mdmd").setExecutor(new BukkitPluginMDCommandExecutor(this));
 		settings.setup(this);
 		
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
