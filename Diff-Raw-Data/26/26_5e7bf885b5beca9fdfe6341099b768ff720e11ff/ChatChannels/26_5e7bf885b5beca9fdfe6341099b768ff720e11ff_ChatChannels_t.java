 package net.mayateck.ChatChannels;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.logging.Level;
 
 import net.mayateck.ChatChannels.CommandHandler;
 import org.bukkit.ChatColor;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.event.Listener;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class ChatChannels extends JavaPlugin implements Listener{
 	Plugin plugin = this;
 	public static String head = ChatColor.DARK_GRAY+"["+ChatColor.YELLOW+"TeamUp"+ChatColor.DARK_GRAY+"] "+ChatColor.RESET;
 	private FileConfiguration players = null;
 	private static File playersFile = null;
 	
 	@Override
 	public void onEnable(){
		this.getLogger().info("#======# ChatChannels by Wehttam664 #======#");
 		this.getLogger().info("Initializing...");	
 			getCommand("channel").setExecutor(new CommandHandler(this));
 			getCommand("local").setExecutor(new CommandHandler(this));
 			getCommand("global").setExecutor(new CommandHandler(this));
 			getCommand("chatchannel").setExecutor(new CommandHandler(this));
 			new ChatHandler(this);
 		this.getLogger().info("Requesting disk response...");
 			this.saveDefaultConfig();
			this.saveDefaultPlayersList();
 		this.getLogger().info("Ready! Current version is "+plugin.getDescription().getVersion());
 		// Eventually I'll check if the current version up-to-date.
		this.getLogger().info("#==========================================#");
 	}
 	
	public void reloadPlayersList() {
 		if (playersFile == null) {
 			playersFile = new File(plugin.getDataFolder(), "players.yml");
 		}
 	    players = YamlConfiguration.loadConfiguration(playersFile);
 	    InputStream vendorConfigStream = plugin.getResource("players.yml");
 	    if (vendorConfigStream != null) {
 	        YamlConfiguration vendorConfig = YamlConfiguration.loadConfiguration(vendorConfigStream);
 	        players.setDefaults(vendorConfig);
 	    }
 	}
 	
	public FileConfiguration getPlayersList() {
 		if (players == null) {
	    	this.reloadPlayersList();
 		}
 		return players;
 	}
 	
	public void savePlayersList() {
 	    if (players == null || playersFile == null) {
 	    	return;
 	    }
 	    try {
	        getPlayersList().save(playersFile);
 	    } catch (IOException ex) {
 	        plugin.getLogger().log(Level.SEVERE, "Could not save config to " + playersFile, ex);
 	    }
 	}
 	
	public void saveDefaultPlayersList() {
 	    if (playersFile == null) {
 	        playersFile = new File(plugin.getDataFolder(), "players.yml");
 	    }
 	    if (!playersFile.exists()) {
 	         plugin.saveResource("players.yml", false);
 	    }
 	}
 }
