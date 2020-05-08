 package uk.co.quartzcraft.kingdoms;
 
 import java.sql.Connection;
 import java.util.Arrays;
 import java.util.logging.Logger;
 
import com.sun.tools.javac.util.List;

 import org.bukkit.Bukkit;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import uk.co.quartzcraft.core.QuartzCore;
 import uk.co.quartzcraft.kingdoms.command.*;
 
 public class QuartzKingdoms extends JavaPlugin {
 	
 	public Plugin plugin = this.plugin;
 	
 	public static final Logger log = Logger.getLogger("Minecraft");
 	
 	public static String releaseVersion = QuartzCore.displayReleaseVersion();
 	//public static Connection DBCore = null;
 	//public static Connection DBKing = null;
 
 	//MySQL MySQLcore = new MySQL(plugin, "localhost", "3306", "Quartz", "root", "database1");
 	//MySQL MySQLking = new MySQL(plugin, "localhost", "3306", "Kingdoms", "root", "database1");
 	
 	@Override
 	public void onDisable() {
 		
 		//Database
 		//log.info("[SHUTDOWN]Terminating Connection to Database");
 		//DBKing = MySQL.closeConnection();
 				
     	//Shutdown notice
 		log.info("[QK]The QuartzKingdoms Plugin has been disabled!");
 	}
 	
 	@Override
 	public void onEnable() {
 		
 		//Database
 		//logger.info("[STARTUP]Connecting to Database");
 		//DBKing = MySQLking.openConnection();
 		//DBCore = MySQLcore.openConnection();
 		
 		//Listeners
 		log.info("[QK][STARTUP]Registering Listeners");
 		
 		//Commands
 		log.info("[QK][STARTUP]Registering Commands");
 		getCommand("kingdom").setExecutor(new CommandKingdom());
		CommandKingdom.addCommand(Arrays.asList("info"), new InfoSubCommand());
 	   	
         //Startup notice
 		log.info("[QK]The QuartzKingdoms Plugin has been enabled!");
 		log.info("[QK]Compiled using QuartzCore version " + releaseVersion);
 	}
 
 }
