 package org.SkyCraft.Coliseum;
 
 import java.util.HashSet;
 import java.util.Set;
 import java.util.logging.Logger;
 
 import org.SkyCraft.Coliseum.Arena.Arena;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class ColiseumPlugin extends JavaPlugin {
 
 	private ColiseumCommandExecutor executor;
	private Logger log = getLogger();
 	private Set<Arena> arenaSet = new HashSet<Arena>();
 	private Set<String> playerAlreadyJoined = new HashSet<String>();
 	//private ConfigHandler confHandler;
 	//TODO Need logout listener to remove and move player on logout
 	
 	
 	public void onEnable() {
 		getCommand("coliseum").setExecutor(executor = new ColiseumCommandExecutor(this, log));
 		//confHandler = new ConfigHandler(this);
 	}
 	
 	public Set<Arena> getArenaSet() {
 		return arenaSet;
 	}
 	
 	public boolean isPlayerJoined(String name) {
 		return playerAlreadyJoined.contains(name);
 	}
 	
 	public void joinPlayer(String name) {
 		playerAlreadyJoined.add(name);
 		return;
 	}
 	
 	public void leavePlayer(String name) {
 		playerAlreadyJoined.remove(name);
 		return;
 	}
 
 }
