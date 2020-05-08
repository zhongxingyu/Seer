 package uk.co.quartzcraft.kingdoms;
 
 import java.sql.Connection;
 import java.util.Arrays;
 import java.util.logging.Logger;
 
 import com.sun.tools.javac.util.List;
 
 import org.bukkit.Bukkit;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import uk.co.quartzcraft.core.QuartzCore;
 import uk.co.quartzcraft.core.chat.ChatPhrase;
 import uk.co.quartzcraft.core.database.MySQL;
 import uk.co.quartzcraft.kingdoms.command.*;
 import uk.co.quartzcraft.kingdoms.listeners.*; 
 
 public class QuartzKingdoms extends JavaPlugin {
 	
 	public Plugin plugin = this.plugin;
 	
 	public static final Logger log = Logger.getLogger("Minecraft");
 	
 	public static String releaseVersion = QuartzCore.displayReleaseVersion();
 
 	public static Connection DBKing = null;
 
 	public static MySQL MySQLking = null;
 	
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
 		
 		log.info("[QC]Running plugin configuration");
 		this.saveDefaultConfig();
 		
 		String SQLKingHost = this.getConfig().getString("database.kingdoms.host");
 		String SQLKingDatabase = this.getConfig().getString("database.kingdoms.database");
 		String SQLKingUser = this.getConfig().getString("database.kingdoms.username");
 		String SQLKingPassword = this.getConfig().getString("database.kingdoms.password");
 		MySQLking = new MySQL(plugin, SQLKingHost, "3306", SQLKingDatabase, SQLKingUser, SQLKingPassword);
 		
 		//Phrases
 		log.info("[QK][STARTUP]Creating Phrases");
		ChatPhrase.addPhrase("kingdom_name_single_word", "&cA kingdoms name may only be a single word!");
 		ChatPhrase.addPhrase("created_kingdom_yes", "&aSuccessfully created kingdom: ");
 		ChatPhrase.addPhrase("created_kingdom_no", "&cFailed to create kingdom: ");
 		ChatPhrase.addPhrase("deleted_kingdom_yes", "&aSuccessfully deleted kingdom: ");
 		ChatPhrase.addPhrase("deleted_kingdom_no", "&cFailed to delete kingdom: ");
 		ChatPhrase.addPhrase("specify_kingdom_name", "&cPlease specify a name!");
 		ChatPhrase.addPhrase("kingdomname_already_used", "&cAnother kingdom is using that name! &aConsider using a different name and overtaking that kingdom!");
 		ChatPhrase.addPhrase("info_kingdom", "&bInfo on Kingdom: ");
 		ChatPhrase.addPhrase("chunk_claimed_for_kingdom_yes", "&aChunk successfully claimed for Kingdom: ");
 		ChatPhrase.addPhrase("chunk_claimed_for_kingdom_not", "&aChunk not successfully claimed for Kingdom: ");
 		
 		//Database
 		//logger.info("[STARTUP]Connecting to Database");
 		DBKing = MySQLking.openConnection();
 		
 		//Listeners
 		log.info("[QK][STARTUP]Registering Listeners");
 		//getServer().getPluginManager().registerEvents(new BlockListener(), this);
 		new BlockListener(this);
 		
 		//Commands
 		log.info("[QK][STARTUP]Registering Commands");
 		getCommand("kingdom").setExecutor(new CommandKingdom());
 		CommandKingdom.addCommand(Arrays.asList("info"), new KingdomInfoSubCommand());
 		CommandKingdom.addCommand(Arrays.asList("create"), new KingdomCreateSubCommand());
 		CommandKingdom.addCommand(Arrays.asList("delete"), new KingdomDeleteSubCommand());
 		CommandKingdom.addCommand(Arrays.asList("promote"), new KingdomPromoteSubCommand());
 		CommandKingdom.addCommand(Arrays.asList("claim"), new KingdomClaimSubCommand());
 	   	
         //Startup notice
 		log.info("[QK]The QuartzKingdoms Plugin has been enabled!");
 		log.info("[QK]Compiled using QuartzCore version " + releaseVersion);
 	}
 
 }
