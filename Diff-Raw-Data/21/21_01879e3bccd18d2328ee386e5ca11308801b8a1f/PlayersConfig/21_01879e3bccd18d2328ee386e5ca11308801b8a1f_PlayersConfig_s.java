 package team.bukkitserverforbukkitpeople.util;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.logging.Level;
 
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 
 import team.bukkitserverforbukkitpeople.Main;
 
 public class PlayersConfig {
 
	public  Main plugin;
	public  File playersFile;
	public  FileConfiguration playersConfig;
 	
 	public PlayersConfig(Main plugin) {
 		this.plugin = plugin;
 	}
 	
 	
	public  void reloadPlayers() {
 	    if (playersFile == null) {
 	    	playersFile = new File(plugin.getDataFolder(), "players.yml");
 	    }
 	    playersConfig = YamlConfiguration.loadConfiguration(playersFile);
 	}
 
 	
	public  FileConfiguration getPlayers() {
 	    if (playersConfig == null) {
 	        this.reloadPlayers();
 	    }
 	    return playersConfig;
 	}
 	
	public  void savePlayers() {
	    if (playersConfig == null || playersConfig == null) {
 	    return;
 	    }
 	    try {
 	        getPlayers().save(playersFile);
 	    } catch (IOException ex) {
 	        plugin.getLogger().log(Level.SEVERE, "Could not save config to " + playersConfig, ex);
 	    }
 	}
 	
 	
}
