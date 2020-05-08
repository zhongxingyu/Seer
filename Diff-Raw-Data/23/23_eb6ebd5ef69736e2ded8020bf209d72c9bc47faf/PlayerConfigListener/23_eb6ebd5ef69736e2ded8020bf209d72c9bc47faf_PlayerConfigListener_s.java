 package net.othercraft.steelsecurity.listeners;
 
 import java.io.IOException;
 
 import net.othercraft.steelsecurity.Main;
 import net.othercraft.steelsecurity.utils.PlayerConfigManager;
 import net.othercraft.steelsecurity.utils.SSCmdExe;
 
 import org.bukkit.GameMode;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.player.PlayerJoinEvent;
 
 
 public class PlayerConfigListener extends SSCmdExe {
 
 	public Main plugin;
 
 	public PlayerConfigListener(String name, Main instance) {
 		super("PlayerConfigListener", true);//true only if its a listener, false if it isnt
 		this.plugin = instance;
 	}
 	@EventHandler
	public void loadConfig(PlayerJoinEvent event){
 		try {
 			String playername = event.getPlayer().getName();
			if (PlayerConfigManager.getConfig(playername)==null){
				PlayerConfigManager.createConfig(playername);
 			}
			PlayerConfigManager.getConfig(playername).addDefault("GameMode", 0);
			PlayerConfigManager.getConfig(playername).options().copyDefaults(true);
			PlayerConfigManager.saveConfig(PlayerConfigManager.getConfig(playername), playername);
			event.getPlayer().setGameMode(GameMode.getByValue(PlayerConfigManager.getConfig(playername).getInt("GameMode")));
 		}
 		catch (Exception e){
 			try {
 				catchListenerException(e, event.getEventName());
 			} catch (IOException e1) {
 				e1.printStackTrace();
 			}
 		}
 	}
 
 
 }
