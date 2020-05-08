 package pl.tolvat.bukkit.yourlobby;
 
 import java.util.logging.Logger;
 
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class YourLobby extends JavaPlugin {
 	
 	/** Release Date of YourLobby. It's simple to understand it. */
 	private static int RELEASE_DATE = 1607201301;
 	
 	/** Versions - better not to change that. */
 	private static String BUKKIT_VERSION = "git-Bukkit-1.5.2-R1.0-27-gdc25312-b2814jnks (MC: 1.6.2)";
 	private static String PLUGIN_VERSION = "1.6.2";
 	
 	/** Logger instance */
 	private Logger logger = Logger.getLogger("Minecraft");
 	
 	/**
 	 * This function is executed when plugin is starting.
 	 */
 	public void onEnable()
 	{
		if(getServer().getVersion() != BUKKIT_VERSION){
 			log("WARNING", "YourLobby may not compatibile with your Craftbukkit version!");
 		}
 		
 		log("INFO", "Plugin has been started.");
 	}
 	
 	/**
 	 * This function is executed when plugin is stopping.
 	 */
 	public void onDisable()
 	{
 		log("INFO", "Plugin has been stopped.");
 	}
 	
 	/**
 	 * You can log something to console using this function.
 	 * @param String prefix
 	 * @param String message
 	 */
 	public void log(String prefix, String message)
 	{
 		logger.info("[" + prefix + "] [YourLobby for Craftbukkit " + PLUGIN_VERSION + "]: " + message);
 	}
 }
