 package net.roguedraco.infobutton.player;
 
 //import java.io.File;
 //import java.io.IOException;
 //import java.util.logging.Level;
 
 //import org.bukkit.Bukkit;
 import org.bukkit.configuration.file.FileConfiguration;
 //import org.bukkit.configuration.file.YamlConfiguration;
 
 public class RDPlayer {
 
	//private String name;
 
 	//private File playerFile = null;
 	private FileConfiguration player = null;
 
 	public RDPlayer(String name) {
		//this.name = name;
 		//load();
 	}
 	
 	public boolean getBoolean(String path) {
 		return this.player.getBoolean(path,false);
 	}
 	
 	public String getString(String path) {
 		return this.player.getString(path,"");
 	}
 	
 	public int getInt(String path) {
 		return this.player.getInt(path,0);
 	}
 	
 	public double getDouble(String path) {
 		return this.player.getDouble(path,0.00);
 	}
 	
 	public void set(String key, Object val) {
 		this.player.set(key, val);
 	}
 
 //	public void save() {
 //		if (RDPlayers.saveData == true) {
 //			try {
 //				player.save(playerFile); // Save the file
 //			} catch (IOException ex) {
 //				Bukkit.getServer()
 //						.getLogger()
 //						.log(Level.SEVERE,
 //								"Could not save config to " + playerFile, ex);
 //			}
 //		}
 //	}
 //
 //	public void load() {
 //		playerFile = new File("plugins/JumpPorts/players", name + ".yml");
 //		player = YamlConfiguration.loadConfiguration(playerFile);
 //
 //	}
 }
