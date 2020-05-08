 package uk.co.cynicode.CyniCord;
 
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.pircbotx.PircBotX;
 
 import uk.co.cynicode.CyniCord.DataGetters.IDataGetter;
 import uk.co.cynicode.CyniCord.DataGetters.JSONDataGetter;
 import uk.co.cynicode.CyniCord.DataGetters.MySQLDataGetter;
 import uk.co.cynicode.CyniCord.Listeners.PluginMessageListener;
 import uk.co.cynicode.CyniCord.IRCManager;
 
 import net.craftminecraft.bungee.bungeeyaml.pluginapi.ConfigurablePlugin;
 import net.md_5.bungee.api.plugin.Plugin;
 import net.md_5.bungee.api.ProxyServer;
 import net.md_5.bungee.api.config.ServerInfo;
 
 public class CyniCord extends ConfigurablePlugin {
 	
 	private static CyniCord self;
 	
 	private static Logger logger = null;
 	
 	private static boolean debug = false;
 	
 	//private static IDataGetter connection = null;
 	
 	public static ProxyServer proxy = null;
 	
 	public static Map<String, ServerInfo> servers = null;
 	
 	public static IRCManager PBot = null;
 	
 	public static void sendMessage( String channel, String player, String message ) {
 		PBot.sendMessage( channel, player, message );
 	}
 	
 	@Override
 	public void onEnable() {
 		
 		this.logger = getLogger();
 		this.saveDefaultConfig();
 		
 		if ( getConfig().getString( "CyniCord.other.debug" ).equalsIgnoreCase( "true" ) ) {
 			debug = true;
 			printInfo( "Debugging enabled..." );
 		} else {
 			printInfo( "Debugging disabled..." );
 		}
 		
 		//if ( getConfig().getString( "CyniCord.other.storage" ).equalsIgnoreCase( "mysql" ) ) {
 		//	connection = new MySQLDataGetter();
 		//} else {
 		//	connection = new JSONDataGetter();
 		//}
 		
 		//if ( connection.startConnection( this ) == false )
 		//	CyniCord.killPlugin();
 		
 		try {
 			PBot = new IRCManager( this/*, connection*/ );
 		} catch ( Exception e ) {
 			e.printStackTrace();
 		}
 		
 		proxy = ProxyServer.getInstance();
 		proxy.getPluginManager().registerListener( this, new PluginMessageListener( this ) );
 		
 		servers = proxy.getServers();
 		
 		self = this;
 		
 		logger.warning( "CyniCord has been activated..." );
 	}
 	
 	@Override
 	public void onDisable() {
 		
 		//connection.endConnection();
 		
 		printInfo( "Killing CyniCord..." );
 		
 		try {
			PBot.stop();
			//self.killPlugin();
 		} catch ( Exception e ) {
 			printSevere( "Uh oh... something went bang" );
 			e.printStackTrace();
 		}
 		
 		printInfo( "CyniCord has been shut down" );
 		
 	}
 
 	public static void killPlugin() {
 		
 		self.onDisable();
 		
 	}
 	
 	public static void printInfo( String output ) {
 		logger.info( output );
 	}
 	
 	public static void printWarning( String output ) {
 		logger.warning( output );
 	}
 	
 	public static void printSevere( String output ) {
 		logger.severe( output );
 	}
 	
 	public static void printDebug( String output ) {
 		if ( debug == true )
 			logger.log( Level.INFO, "[DEBUG] {0}", output);
 	}
 }
