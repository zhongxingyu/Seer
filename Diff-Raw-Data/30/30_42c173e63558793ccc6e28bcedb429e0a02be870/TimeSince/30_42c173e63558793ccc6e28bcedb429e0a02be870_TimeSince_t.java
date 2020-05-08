 package blha303;
 
 import java.util.logging.Logger;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class TimeSince extends JavaPlugin implements Listener {
 	
 	private static final Logger log = Logger.getLogger("Minecraft");
 	boolean debug = false;
 
 	public void onEnable() {
 		getConfig().addDefault("lastPlayerLeftAt", 1357623885);
 		getConfig().addDefault("firstrun", true);
 		getConfig().addDefault("debug", false);
 		getConfig().options().copyDefaults(true);
 		saveConfig();
 		if (getConfig().getBoolean("debug")) debug = true;
 		getServer().getPluginManager().registerEvents(this, this);
 	}
 	
 	public void onDisable() {
 		saveConfig();
 	}
 	
 	@EventHandler
 	public void onPlayerJoin(PlayerJoinEvent event) {
 		if (debug) {
 			log.info("[TimeSince] DEBUG: " + getConfig().toString());
 			log.info("[TimeSince] DEBUG: " + getServer().getOnlinePlayers().length);
 		}
 		if ((getServer().getOnlinePlayers().length) == 1) {
 			if (getConfig().getBoolean("firstrun")) {
 				event.getPlayer().sendMessage("Thanks for installing TimeSince! This message will not be shown again.");
 				getConfig().set("firstrun", false);
 				return;
 			} else {
 				event.getPlayer().sendMessage(timesince());
 				return;
 			}
 		}
 	}
 	
 	@EventHandler
 	public void onPlayerLeave(PlayerQuitEvent event) {
 		if (debug) {
 			log.info("[TimeSince] DEBUG: " + getServer().getOnlinePlayers());
 			int time = safeLongToInt(System.currentTimeMillis()/1000);
 			int logout = getConfig().getInt("lastPlayerLeftAt");
 			log.info(logout + " " + time);
 		}
 		if (getServer().getOnlinePlayers().length == 1) {
 			getConfig().set("lastPlayerLeftAt", safeLongToInt(System.currentTimeMillis()/1000));
 			saveConfig();
 			log.info("[TimeSince] Time recorded.");
 			return;
 		}
 	}
 	
 	public boolean onCommand(CommandSender cs, Command cmnd, String string,
 			String[] args) {
 		cs.sendMessage(timesince());
 		
 		return true;
 	}
 	
 	// http://stackoverflow.com/questions/1590831/safely-casting-long-to-int-in-java
 	public static int safeLongToInt(long l) {
 	    if (l < Integer.MIN_VALUE || l > Integer.MAX_VALUE) {
 	        throw new IllegalArgumentException
 	            (l + " cannot be cast to int without changing its value.");
 	    }
 	    return (int) l;
 	}
 	
 	public String timesince() {
		String out = "";
 		int time = safeLongToInt(System.currentTimeMillis()/1000);
 		int logout = getConfig().getInt("lastPlayerLeftAt");
 		if (debug) log.info(logout + " " + time);
		int ts = ((logout - time) * -1);
		if (ts > 60) {
			if (ts > 60*60) {
				if (ts > 60*60*24) {
					if (ts > 60*60*24*30) {
						out = (ts/60/60/24/30 + " months ago");
					} else {
						out = (ts/60/60/24 + " days ago");
					}
				} else {
					out = (ts/60/60 + " hours ago");
				}
			} else {
				out = (ts/60 + " minutes ago");
			}
		} else {
			out = (ts + " seconds ago");
		}
		return "Someone was on this server " + out + "!";
 	}
 }
