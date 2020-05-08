 package me.Ryan6338.ColouredPlayerList;
 
 import java.util.logging.Logger;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public final class ColouredPlayerList extends JavaPlugin {
 	public Logger logger = Logger.getLogger("Minecraft");
 	public PlayerListener listener;
 	public PermsLookup perms = new PermsLookup(this);
	public int interval = getConfig().getInt("Update Interval");
 
 	//Performs on Disable
 	
 	@Override
 	public void onDisable() {
 		PluginDescriptionFile pdfFile = this.getDescription();
 		logger.info(pdfFile.getName() + " has been disabled!");
 	}
 
 	//Performs on Enable if the version is correct
 	
 	@Override
 	public void onEnable() {
 		this.saveDefaultConfig();
 		PluginManager pm = this.getServer().getPluginManager();
 		NameLoop();
 		
 		//Registers events in the PlayerListener class
 		
 		pm.registerEvents(new PlayerListener(this), this);
 	}
 	
 	//Logs in the console messages from the plugin
 	
 	public void Log(String text) {
 		logger.info("[ColouredPlayerList] " + text);
 	}
 	
 	public void setName(Player p) {
 		ChatColor c = perms.colour(p);
 		String pname = ChatColor.stripColor(p.getDisplayName());
 		
 		//Checks to see if the nickname is too long
 		
 		if (this.getConfig().getBoolean("Add Dots")) {
 			if (pname.length() > 12) {
 				pname = pname.substring(0, 12) + "..";
 			}
 		} else {
 			if (pname.length() > 14) {
 				pname = pname.substring(0, 14);
 			}
 		}
 		p.setPlayerListName(c + pname);
 	}
 	
 	public void Delay(final Player p) {
 		getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
 			@Override
 			public void run() {
 				setName(p);
 			}
 		}, 2L);
 	}
 	
 	public void NameLoop() {
 		getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
 			@Override
 			public void run() {
 				Player[] Players = getServer().getOnlinePlayers();
 				for(int i = 0; i<Players.length; i++) {
 					if(Players[i].getPlayerListName() != perms.colour(Players[i]) + Players[i].getDisplayName()) {
 						setName(Players[i]);
 					}
 				}
 			}
 		}, interval, interval);
 	}
 	
 	public boolean Dots() {
 		return (this.getConfig().getBoolean("Add Dots"));
 	}
 }
