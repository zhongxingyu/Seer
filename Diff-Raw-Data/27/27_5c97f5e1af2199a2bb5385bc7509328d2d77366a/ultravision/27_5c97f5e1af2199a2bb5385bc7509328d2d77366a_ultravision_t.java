 /*  ============================================================================
  *
  *      U L T R A V I S I O N  ---  P l a y e r   S u p e r v i s i o n
  *
  *       This Bukkit Plugin provides functionality for every security,
  *              as well as broadcasting and logging purposes on your MC-Server.
  *
  *                              by prosicraft  ,   (c) 2013
  *
  *          Update 12.7.2013
  *
  *  ============================================================================
  */
 package com.prosicraft.ultravision;
 
 import com.prosicraft.ultravision.JMessage.JMessage;
 import com.prosicraft.ultravision.base.UVChatListener;
 import com.prosicraft.ultravision.base.UVClickAuth;
 import com.prosicraft.ultravision.base.UltraVisionAPI;
 import com.prosicraft.ultravision.chat.MCChatListener;
 import com.prosicraft.ultravision.chat.UVServer;
 import com.prosicraft.ultravision.commands.*;
 import com.prosicraft.ultravision.global.globalEngine;
 import com.prosicraft.ultravision.local.localEngine;
 import com.prosicraft.ultravision.util.*;
 import java.io.File;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Locale;
 import java.util.Map;
 import java.util.ResourceBundle;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 //**********************************************************************************************
 /**
  * UltraVision Main Class
  *
  * @author prosicraft
  */
 public class ultravision extends JavaPlugin
 {
 
 	private MConfiguration config		= null;     // Ultravision Configuration
 	private PluginDescriptionFile fPDesc	= null;     // Plugin description file
 	private uvPlayerListener playerListener	= null;     // The Main Listener
 	private uvblocklistener blockListener	= null;     // Block listener for auth
 	public uvCoreListener coreListener	= null;     // Listener for UVBridges
 	private UVServer uvserver		= null;     // The Mineconnect server
 	private MCChatListener uvchatlistener	= null;     // Mineconnect listener
 	private JMessage jmsg			= null;     // JMessage Template
 	private MAuthorizer auth		= null;     // Authorizer Template
 	private UVClickAuth clickauth		= null;     // ClickAuth Template
 	private UltraVisionAPI api		= null;     // Ultravision API Instance
 	private boolean global			= false;    // use global api ?
 	private boolean useMineconnect		= false;    // Mineconnect Template
 	private boolean useJMessage		= false;    // Join Message Template
 	private boolean useAuthorizer		= true;     // Authorizer / Login Template
 	private boolean useClickAuthorizer	= false;    // Clickauthorizer / Optional login template
 	private boolean useCommandLog		= true;     // Command logging
 	public boolean useUltraChat		= true;     // Use UltraChat Plugin if found
 	private boolean showNotRegWarning	= true;     // Show "NotRegistered" Warning on join
 	public boolean allowNotRegActions	= true;     // Allow Player Interaction when not registered
 	public boolean showWelcomeMessage	= true;     // Show powered by Message on login
 	public boolean showMessagesNotLoggedIn	= true;     // Show messages, if not logged in
 	public boolean disableIngameOp		= true;	    // Disable ingame op command
 	public UVBRIDGE[] bridges		= new UVBRIDGE[ 5 ];     // Bridge to UltraChat
 
 	//**********************************************************************************************
 	/**
 	 * OnEnable will start the plugin and everything else
 	 */
 	@Override
 	public void onEnable()
 	{
 
 		// Print the boot information
 		printStartupInfo();
 
 		// Try to load the configuration file
 		try
 		{
 			initConfig();
 		}
 		catch( Exception ex )
 		{
 			MLog.e( "FATAL: " + ex.toString() );
 			ex.printStackTrace( System.out );
 			return;
 		}
 
 		// Load and initialize used Templates
 		loadTemplateSelection();
 		initAuthorizer();
 		initClickAuthorizer();
 		initJMessage();
 
 
 		// Now start the Engine
 		MLog.d( "Starting engine..." );
 
 		if( !global )
 		{
 			api = new localEngine( this.getDataFolder().getAbsolutePath() );
 			MLog.i( "Using Local Engine. Version: " + UltraVisionAPI.version );
 
			if( useAuthorizer )
			{
				final MResult tr;
				if( ( tr = api.registerAuthorizer( auth ) ) == MResult.RES_SUCCESS )
					MLog.i( "Authorizer hooked into Engine." );
				else
					MLog.e( "Authorizer can't hook into Engine: " + tr.toString() );
			}
 		}
 		else
 		{
 			api = new globalEngine();
 			MLog.i( "Using global Engine. Version: " + UltraVisionAPI.version );
 			MLog.w( "Global Engine isn't supported yet." );
 
			if( useAuthorizer )
			{
				final MResult tr;
				if( ( tr = api.registerAuthorizer( auth ) ) == MResult.RES_SUCCESS )
					MLog.i( "Authorizer hooked into Engine." );
				else
					MLog.e( "Authorizer can't hook into Engine: " + tr.toString() );
			}
 		}
 
 		// Check if everything went well. If so apply API to Jmessage and other templates
 		if( api != null )
 		{
 			if( jmsg != null )
 				jmsg.setAPI( api );
 
 			for( Player p : getServer().getOnlinePlayers() )
 				api.playerLogin( p );
 		}
 
 
 		// Hook the several Events into our listeners
 		initEvents();
 		playerListener.initUV( api );
 
 		// Start the Mineconnect Chat Server
 		if( !useMineconnect )
 			MLog.i( "Mineconnect is disabled in configuration." );
 		else
 		{
 			MLog.d( "Starting Mineconnect server..." );
 
 			uvchatlistener = new MCChatListener()
 			{
 				@Override
 				public void onMessage( String msg )
 				{
 					getServer().broadcastMessage( "MConnect (test): " + msg );
 				}
 
 				@Override
 				public void onLogin( String username )
 				{
 					getServer().broadcastMessage( username + " joined the UV Server!" );
 				}
 
 				@Override
 				public void onLeave( String username )
 				{
 					getServer().broadcastMessage( username + " left the UV Server!" );
 				}
 			};
 			uvserver = new UVServer( getServer().getIp(), auth, 10 );
 			uvserver.registerListener( uvchatlistener );
 			uvserver.start();
 
 			MLog.i( "Started Mineconnect server." );
 		}
 
 		// In case of Reload: Do Rejoin everybody
 		rejoin();
 
 	}
 
 	//**********************************************************************************************
 	/**
 	 * onDisable disables the plugin and shuts down everything
 	 */
 	@Override
 	public void onDisable()
 	{
 		MLog.i( "Ultravision is being shutdown. (Updating Config...)" );
 
 		if( auth != null )
 			auth.save();
 		if( clickauth != null )
 			clickauth.saveToFile();
 
 		config.setDefault( "auth.showMessagesNotLoggedIn", true );
 		config.setDefault( "ultravision.showWarnedMessages", true );
 
 		if( jmsg != null )
 			jmsg.save( config );
 
 		config.set( "general.savestats", config.getBoolean( "general.savestats", true ) );
 
 		MLog.i( "Request engine shutdown..." );
 
 		if( api != null )
 		{
 			for( Player p : getServer().getOnlinePlayers() )
 			{
 				api.playerLeave( p );
 			}
 
 			final MResult r;
 			if( ( r = api.flush() ) == MResult.RES_SUCCESS )
 				MLog.i( "Shut down engine (" + ( ( global ) ? "global" : "local" ) + ")" );
 			else
 				MLog.e( "Can't shut down engine (" + ( ( global ) ? "global" : "local" ) + "): " + r.toString() );
 		}
 
 		config.save();
 		playerListener = null;
 		fPDesc = null;
 		config = null;
 
 		if( uvserver != null && uvserver.isAlive() )
 		{
 			uvserver.shutdown();
 			uvserver = null;
 			MLog.i( "Server stopped successfullly." );
 		}
 		else
 			MLog.i( "Server already shut down or disabled." );
 	}
 
 	//**********************************************************************************************
 	/**
 	 * Prints Startup / Boot information about the plugin into the console
 	 */
 	private void printStartupInfo ()
 	{
 		// Startup info
 		fPDesc = this.getDescription();
 		try
 		{
 			Locale currentLocale = new Locale( "en", "US" );
 			MLog.i( "Ultravision is starting (Version " + fPDesc.getVersion() + " #b" + MCrypt.prependZeros( ResourceBundle.getBundle( "com.prosicraft.ultravision.version", currentLocale ).getString( "BUILD" ) ) + ") ..." );
 		}
 		catch( java.util.MissingResourceException ex )
 		{
 			MLog.i( "Ultravision is starting (Version " + fPDesc.getVersion() + " #b???) ..." );
 		}
 	}
 
 	//**********************************************************************************************
 	/**
 	 * Load the template selection from the configuration
 	 */
 	private void loadTemplateSelection()
 	{
 		config.set( "general.useGlobalAPI", ( global = config.getBoolean( "useGlobalAPI", false ) ) );
 		config.set( "general.useAuthorizer", ( useAuthorizer = config.getBoolean( "general.useAuthorizer", true ) ) );
 		config.set( "general.useClickAuthorizer", ( useClickAuthorizer = config.getBoolean( "general.useClickAuthorizer", false ) ) );
 		config.set( "general.useJMessage", ( useJMessage = config.getBoolean( "general.useJMessage", false ) ) );
 		config.set( "general.useCommandLog", ( useCommandLog = config.getBoolean( "general.useCommandLog", true ) ) );
 		config.set( "general.useUltraChat", ( useUltraChat = config.getBoolean( "general.useUltraChat", true ) ) );
 		config.set( "general.showWelcomeMessage", ( showWelcomeMessage = config.getBoolean( "general.showWelcomeMessage", true ) ) );
 		config.set( "general.logFileLimitKByte", ( MConst._LIMIT_A = config.getInt( "general.logFileLimitKByte", MConst._LIMIT_A ) ) );
 		config.set( "general.showNotRegWarning", ( showNotRegWarning = config.getBoolean( "general.showNotRegWarning", true ) ) );
 		config.set( "general.showMessagesNotLoggedIn", ( showMessagesNotLoggedIn = config.getBoolean( "general.showMessagesNotLoggedIn", true ) ) );
 		config.set( "general.debug", ( MConst._DEBUG_ENABLED = config.getBoolean( "general.debug", false ) ) );
 		config.set( "general.allowNotRegActions", ( allowNotRegActions = config.getBoolean( "general.allowNotRegActions", true ) ) );
 		config.set( "general.disableIngameOp", ( disableIngameOp = config.getBoolean( "general.disableIngameOp", true ) ) );
 
 		// Initialize Bridges
 		if( useUltraChat )
 		{
 			addBridge( new UVBRIDGE( this, "UltraChat" ) );
 		}
 	}
 
 	//**********************************************************************************************
 	/**
 	 * Add a new UVBRIDGE, e.g. from an extern Plugin
 	 * @param bridge the bridge instance to be added
 	 * @return true if everything went well, false if not
 	 */
 	public boolean addBridge( UVBRIDGE bridge )
 	{
 		boolean assigned = false;
 		for( int n = 0; n < bridges.length; n++ )
 		{
 			if( bridges[n] == null )
 			{
 				bridges[n] = bridge;
 				assigned = true;
 			}
 		}
 		return assigned;
 	}
 
 	//**********************************************************************************************
 	/**
 	 * Initialize MAuthorizer if used
 	 */
 	private void initAuthorizer()
 	{
 		if( !useAuthorizer )
 		{
 			MLog.i( "Authorizer not used!" );
 			return;
 		}
 
 		MLog.d( "Loading Authorizer now..." );
 
 		File authFile = new File( this.getDataFolder(), "authdb.dat" );
 		if( !authFile.exists() )
 		{
 			try
 			{
 				authFile.createNewFile();
 				MLog.i( "Created unexisting authentication file at " + authFile.getAbsolutePath() );
 			}
 			catch( IOException ioex )
 			{
 				MLog.e( "Can't create unexisting authentication file at " + authFile.getAbsolutePath() );
 				ioex.printStackTrace( System.out );
 			}
 		}
 
 		// Initialize Authorizer
 		( auth = new MAuthorizer( authFile.getAbsolutePath() ) ).a();
 
 		// Initialize Login Attempt counter
 
 	}
 
 	//**********************************************************************************************
 	/**
 	 * Initialize ClickAuthorizer if used
 	 */
 	private void initClickAuthorizer()
 	{
 		if( !useClickAuthorizer )
 		{
 			MLog.i( "Clickauthorizer is not used." );
 			return;
 		}
 
 		clickauth = new UVClickAuth( api, this, config.getBoolean( "auth.showMessagesNotLoggedIn", true ) );
 		clickauth.init();
 	}
 
 	//**********************************************************************************************
 	/**
 	 * Initialize JMessage if used
 	 */
 	private void initJMessage()
 	{
 		if( jmsg != null || !useJMessage )
 		{
 			MLog.i( "JMessage is disabled in configuration file." );
 			return;
 		}
 
 		( jmsg = new JMessage( config, api )
 		{
 			@Override
 			public void broadcast( String txt )
 			{
 				ownBroadcast( txt );
 			}
 		} ).init( this, auth, clickauth );
 		jmsg.save( config );
 	}
 
 	//**********************************************************************************************
 	/**
 	 * Register Events and assign templates
 	 */
 	private void initEvents()
 	{
 		playerListener	= new uvPlayerListener( this );
 		blockListener	= new uvblocklistener( this );
 		coreListener	= new uvCoreListener( this );
 
 		PluginManager pm = this.getServer().getPluginManager();
 
 		pm.registerEvents( playerListener, this );
 		pm.registerEvents( blockListener, this );
 		pm.registerEvents( coreListener, this );
 		pm.registerEvents( new UVChatListener( this ), this );
 	}
 
 	//**********************************************************************************************
 	/**
 	 * Initialize Main-Configuration file of ultravision plugin
 	 */
 	private void initConfig()
 	{
 		if( !this.getDataFolder().exists() && !getDataFolder().mkdirs() )
 			MLog.e( "Can't create missing configuration Folder for UltraVision" );
 
 		File cf = new File( this.getDataFolder(), "config.yml" );
 
 		if( !cf.exists() )
 		{
 
 			try
 			{
 				MLog.w( "Configuration File doesn't exist. Trying to recreate it..." );
 				if( !cf.createNewFile() || !cf.exists() )
 				{
 					MLog.e( "Placement of Plugin might be wrong or has no Permissions to access configuration file." );
 				}
 			}
 			catch( IOException iex )
 			{
 				MLog.e( "Can't create unexisting configuration file" );
 			}
 		}
 
 		config = new MConfiguration( YamlConfiguration.loadConfiguration( cf ), cf );
 
 		// Initialize DataTable
 		Map<String, MConfiguration.DataType> dt = new HashMap<>();
 
 		dt.put( "general.useGlobalAPI", MConfiguration.DataType.DATATYPE_BOOLEAN );
 		dt.put( "general.useAuthorizer", MConfiguration.DataType.DATATYPE_BOOLEAN );
 		dt.put( "general.useClickAuthorizer", MConfiguration.DataType.DATATYPE_BOOLEAN );
 		dt.put( "general.useJMessage", MConfiguration.DataType.DATATYPE_BOOLEAN );
 		dt.put( "general.useCommandLog", MConfiguration.DataType.DATATYPE_BOOLEAN );
 		dt.put( "general.useCHack", MConfiguration.DataType.DATATYPE_BOOLEAN );
 		dt.put( "general.useUltraChat", MConfiguration.DataType.DATATYPE_BOOLEAN );
 		dt.put( "general.showWelcomeMessage", MConfiguration.DataType.DATATYPE_BOOLEAN );
 		dt.put( "general.logFileLimitKByte", MConfiguration.DataType.DATATYPE_INTEGER );
 		dt.put( "general.showNotRegWarning", MConfiguration.DataType.DATATYPE_BOOLEAN );
 		dt.put( "general.allowNotRegActions", MConfiguration.DataType.DATATYPE_BOOLEAN );
 		dt.put( "general.showMesssagesNotLoggedIn", MConfiguration.DataType.DATATYPE_BOOLEAN );
 		dt.put( "general.debug", MConfiguration.DataType.DATATYPE_BOOLEAN );
 		dt.put( "general.savestats", MConfiguration.DataType.DATATYPE_BOOLEAN );
 
 		config.setDataTypeTable( dt );
 
 		config.load();
 	}
 
 	//**********************************************************************************************
 	/**
 	 * Clears whole configuration file.
 	 */
 	public boolean clearConfig()
 	{
 		try
 		{
 			config.clear();
 			config = null;
 
 			File cfg = new File( this.getDataFolder(), "config.yml" );
 
 			if( cfg.exists() && cfg.isFile() )
 			{
 				cfg.delete();
 			}
 
 			if( !cfg.createNewFile() )
 			{
 				throw new Exception( "Couldn't create new configuration File." );
 			}
 
 			if( !cfg.exists() )
 			{
 				throw new Exception( "Didn't create configuration File properly." );
 			}
 
 			config = new MConfiguration( YamlConfiguration.loadConfiguration( cfg ), cfg );
 			config.load();
 			config.set( "general.savestats", true );
 			config.save();
 
 			return true;
 		}
 		catch( Exception ex )
 		{
 			MLog.e( "Failed to reload Ultravision: " + ex.getMessage() );
 			return false;
 		}
 	}
 
 	//**********************************************************************************************
 	/**
 	 * When a players tries to join the game...
 	 */
 	public boolean playerJoin( Player p )
 	{
 
 		if( p == null )
 			return true;    // not false, otherwise it would crash
 
 		api.playerLogin( p );
 		if( api.isBanned( p.getName() ) )
 			return false;
 
 		config.save();
 		MLog.d( "Player joined successfully: " + p.getName() );
 
 		if( uvserver != null )
 			uvserver.sendMessage( p.getName() + " joined the server via Minecraft." );
 
 		return true;
 
 	}
 
 	//**********************************************************************************************
 	/**
 	 * When a player says goodbye
 	 */
 	public boolean playerLeave( Player p )
 	{
 		if( p != null )
 		{
 
 			api.playerLeave( p );
 
 			if( useAuthorizer && auth.loggedIn( p ) )
 				auth.logout( p );
 
 			if( uvserver != null )
 				uvserver.sendMessage( p.getName() + " left the channel via Minecraft." );
 
 		}
 		return false;
 	}
 
 	//**********************************************************************************************
 	/**
 	 * player chat
 	 */
 	public void playerChat( String playername, String msg )
 	{
 	}
 
 	//**********************************************************************************************
 	/**
 	 * Walk through all players after reload
 	 */
 	private boolean rejoin()
 	{
 
 		// Get all Players and hook into count thread
 		//    ONLY IF SOME STUPID KIDS JOINED BEFORE SERVER IS READY >:|
 		Player[] oPlayer = getServer().getOnlinePlayers();
 		if( oPlayer.length > 0 )
 		{
 			for( int i = 0; i < oPlayer.length; i++ )
 			{
 				playerJoin( oPlayer[i] );
 			}
 		}
 
 		return true;
 	}
 
 	//**********************************************************************************************
 	/**
 	 * Handle ultravision Login Command (called by PreCommandListener)
 	 *
 	 * @return true if password was correct or player is not registered or auth is not initialized
 	 */
 	public boolean doLoginCommand( Player player, String password )
 	{
 		if( auth == null )
 			return true;
 
 		if( player != null && password.length() > 0 )
 		{
 			if( !auth.isRegistered( player ) )
 			{
 				player.sendMessage( ChatColor.GOLD + "You're not registered in the login system." );
 				return true;
 			}
 			if( auth.loggedIn( player ) )
 			{
 				player.sendMessage( ChatColor.RED + "You're already logged in." );
 				return true;
 			}
 
 			if( password.contains( " " ) )
 			{
 				player.kickPlayer( MLog.real( ChatColor.DARK_GRAY + "[UltraVision] " + ChatColor.RED + "Wrong password!" ) );
 				return true;
 			}
 			if( auth.login( player, password ) == MResult.RES_NOACCESS )
 			{
 				player.kickPlayer( MLog.real( ChatColor.DARK_GRAY + "[UltraVision] " + ChatColor.RED + "Wrong password!" ) );
 				return true;
 			}
 			else
 			{
 				player.sendMessage( ChatColor.GREEN + "Logged in successfully." );
 				return true;
 			}
 		}
 		return false;
 	}
 
 	//**********************************************************************************************
 	/**
 	 * Handle ultravision Commands
 	 */
 	@Override
 	public boolean onCommand( CommandSender sender, Command cmd, String commandLabel, String[] args )
 	{
 
 		if( ( sender instanceof Player ) )
 		{
 
 			Player p = ( Player ) sender;
 
 			if( auth != null && ( !auth.loggedIn( p ) && !cmd.getLabel().equalsIgnoreCase( "uvlogin" ) ) )
 			{
 				p.sendMessage( ChatColor.RED + "You are not logged in." );
 				return true;
 			}
 			else if( clickauth != null && ( !clickauth.isLoggedIn( p.getName() ) && !cmd.getLabel().equalsIgnoreCase( "uvlogin" ) ) )
 			{
 				p.sendMessage( ChatColor.RED + "You are not logged in." );
 				return true;
 			}
 
 			// Faked Ban
 			if( cmd.getName().equalsIgnoreCase( "uvfban" ) )
 			{
 				p.sendMessage( ChatColor.GREEN + "Ban." );
 				return true;
 			}
 
 			if( cmd.getName().equalsIgnoreCase( "ultravision" ) )
 			{
 				if( args.length == 1 && args[0].equalsIgnoreCase( "reload" ) )
 				{
 					config = null;
 					initConfig();
 					loadTemplateSelection();
 					p.sendMessage( ChatColor.GREEN + "Reloaded Config" );
 					return true;
 				}
 
 				p.sendMessage( ChatColor.DARK_GRAY + "=== " + ChatColor.DARK_AQUA + "Server running " + ChatColor.AQUA + "ULTRAVISION" + ChatColor.GRAY + " version " + ChatColor.AQUA + fPDesc.getVersion() + ChatColor.DARK_GRAY + " ===" );
 
 				p.sendMessage( ChatColor.GOLD + "This Bukkit Plugin provides functionality for every security, as well as frondemd and logging purposes on your MC-Server." );
 
 				String coms = "";
 				for( String lecom : fPDesc.getCommands().keySet() )
 					coms += lecom + ChatColor.DARK_GRAY + ", " + ChatColor.GRAY;
 				coms = coms.substring( 0, coms.length() - 2 );
 				p.sendMessage( ChatColor.DARK_GRAY + "Commands: " + ChatColor.GRAY + coms );
 				return true;
 			}
 
 			if( cmd.getName().equalsIgnoreCase( "jmessage" ) )
 			{
 				if( args.length == 0 || ( !args[0].equalsIgnoreCase( "reload" ) && !args[0].equalsIgnoreCase( "assign" ) && !args[0].equalsIgnoreCase( "preview" ) ) )
 				{
 					p.sendMessage( ChatColor.RED + "Command not recognized or too few arguments." );
 					return false;
 				}
 				if( jmsg == null )
 				{
 					p.sendMessage( ChatColor.RED + "JMessage is disabled." );
 					return true;
 				}
 				if( args[0].equalsIgnoreCase( "reload" ) )
 				{
 					jmsg.load( config );
 					p.sendMessage( ChatColor.GREEN + "Reloaded JMessage config." );
 					return true;
 				}
 				else if( args[0].equalsIgnoreCase( "preview" ) )
 				{
 					jmsg.doJoinTest( p );
 					jmsg.doLeaveTest( p );
 					return true;
 				}
 				else
 				{
 					if( args.length < 3 )
 					{
 						p.sendMessage( ChatColor.RED + "Too few arguments." );
 						return false;
 					}
 					String thetxt = "";
 					for( int n = 2; n < ( args.length ); n++ )
 						thetxt += args[n] + " ";
 					jmsg.assignIndividual( args[1], thetxt.trim() );
 					p.sendMessage( ChatColor.GREEN + "Assigned join message to '" + args[1] + "' successfully." );
 					return true;
 				}
 			}
 
 			if( cmd.getLabel().equalsIgnoreCase( "uvver" ) )
 			{
 				//chack.join(p);
 				p.sendMessage( "Executed." );
 				return true;
 			}
 
 			if( cmd.getLabel().equalsIgnoreCase( "uvregister" ) )
 			{
 				if( auth.isRegistered( p ) )
 				{
 					p.sendMessage( ChatColor.GOLD + "You're already registered in the login system." );
 					return true;
 				}
 				if( args.length != 1 )
 				{
 					p.sendMessage( ChatColor.RED + "Please specify a password." );
 					return true;
 				}
 				if( args[0].equalsIgnoreCase( "password" ) || args[0].equalsIgnoreCase( "passwort" ) || args[0].equalsIgnoreCase( p.getName() ) )
 				{
 					p.sendMessage( ChatColor.RED + "This password is too simple!" );
 					return true;
 				}
 				MResult res;
 				if( ( res = auth.register( p, args[0] ) ) == MResult.RES_SUCCESS )
 				{
 					p.sendMessage( ChatColor.GREEN + "Registered successfully in login system as " + p.getName() + "." );
 					p.sendMessage( ChatColor.GREEN + "Login with " + ChatColor.GOLD + "/login YourPassword" + ChatColor.GREEN + "." );
 				}
 				else
 					MLog.e( "Couldn't register new player in login system (player=" + p.getName() + "): " + String.valueOf( res ) );
 				return true;
 			}
 
 			/*if ( cmd.getLabel().equalsIgnoreCase("uvaddmac") ) {
 			 if ( chack == null ) {
 			 p.sendMessage(ChatColor.RED + "CrashHack is not used on this server.");
 			 return true;
 			 }
 			 if ( args.length < 1 ) {
 			 p.sendMessage(ChatColor.RED + "Too few arguments.");
 			 return false;
 			 }
 			 if ( chack.hasMac(args[0]) ) {
 			 p.sendMessage(ChatColor.RED + "Player " + ChatColor.GRAY + args[0] + ChatColor.RED + " already registered. Use /clearmac to clear.");
 			 return true;
 			 }
 			 for ( Player tp : getServer().getOnlinePlayers() ) {
 			 if ( tp.getName().equalsIgnoreCase(args[0]) ) {
 			 chack.addMac(tp);
 			 p.sendMessage (ChatColor.GREEN + "Registered Mac address of player " + ChatColor.GRAY + tp.getName() + ChatColor.GREEN + ".");
 			 chack.save(config); config.save();
 			 return true;
 			 }
 			 }
 			 p.sendMessage(ChatColor.RED + "Player " + ChatColor.GRAY + args[0] + ChatColor.RED + " not found or offline.");
 			 return true;
 			 }
 
 			 if ( cmd.getLabel().equalsIgnoreCase("uvclearmac") ) {
 			 if ( chack == null ) {
 			 p.sendMessage(ChatColor.RED + "CrashHack is not used on this server.");
 			 return true;
 			 }
 			 if ( args.length < 1 ) {
 			 p.sendMessage(ChatColor.RED + "Too few arguments.");
 			 return false;
 			 }
 			 if ( !chack.hasMac(args[0]) ) {
 			 p.sendMessage(ChatColor.RED + "Player " + ChatColor.GRAY + args[0] + ChatColor.RED + " not registered in CrashHack.");
 			 return true;
 			 }
 			 chack.clearMac(args[0]);
 			 p.sendMessage(ChatColor.GREEN + "Mac of player " + ChatColor.GRAY + args[0] + ChatColor.GREEN + " has been unregistered.");
 			 return true;
 			 }*/
 
 			if( cmd.getLabel().equalsIgnoreCase( "uvclickregister" ) )
 			{
 				if( clickauth == null )
 				{
 					p.sendMessage( ChatColor.RED + "The UV-ClickAuth system is not used." );
 					return true;
 				}
 				if( clickauth.isRegistered( p.getName() ) )
 				{
 					p.sendMessage( ChatColor.GOLD + "You're already registered in the UV-ClickAuth System." );
 					return true;
 				}
 				if( clickauth.toggleRegistering( p ) )
 				{
 					p.sendMessage( ChatColor.AQUA + "--- Registering for UV-ChatAuth ---" );
 					p.sendMessage( ChatColor.GRAY + " Place Blocks with distance from each other." );
 					p.sendMessage( ChatColor.GRAY + " Remember the location of the Blocks, relatively." );
 					p.sendMessage( ChatColor.GRAY + " Finish by typing " + ChatColor.AQUA + "/caregister" + ChatColor.GRAY + " again." );
 				}
 				else
 				{
 					p.sendMessage( ChatColor.AQUA + "--- Finished UV-ClickAuth Registering ---" );
 					p.sendMessage( ChatColor.GRAY + " Please Login now." );
 				}
 				return true;
 			}
 			else if( cmd.getLabel().equalsIgnoreCase( "uvclickunregister" ) )
 			{
 				if( clickauth == null )
 				{
 					p.sendMessage( ChatColor.RED + "The UV-ClickAuth system is not used." );
 					return true;
 				}
 				if( args.length < 1 )
 				{
 					p.sendMessage( ChatColor.RED + "Too few arguments" );
 					return false;
 				}
 				String thePlayer = args[0];
 				if( !clickauth.isRegistered( p.getName() ) )
 				{
 					p.sendMessage( ChatColor.GOLD + thePlayer + " is not registered in ClickAuth System." );
 					return true;
 				}
 				clickauth.unRegister( thePlayer );
 				clickauth.saveToFile();
 				p.sendMessage( ChatColor.GREEN + thePlayer + " has been unregistered." );
 				for( int i = 0; i < getServer().getOnlinePlayers().length; i++ )
 				{
 					if( getServer().getOnlinePlayers()[i].getName().equalsIgnoreCase( thePlayer ) )
 					{
 						getServer().getOnlinePlayers()[i].sendMessage( ChatColor.GOLD + "You have been unregistered from ClickAuth." );
 						break;
 					}
 				}
 				return true;
 			}
 
 			if( cmd.getName().equalsIgnoreCase( "uvclear" ) )
 			{
 				p.sendMessage( ChatColor.GOLD + "Trying to cleanup Configuration..." );
 				if( !this.clearConfig() )
 				{
 					p.sendMessage( ChatColor.RED + "Failed to cleanup Configuration..." );
 				}
 				else
 				{
 					p.sendMessage( ChatColor.GREEN + "Cleanup Configuration was successful." );
 				}
 				return true;
 			}
 
 			if( cmd.getLabel().equalsIgnoreCase( "uvaunregister" ) )
 			{
 				if( args.length != 1 )
 				{
 					p.sendMessage( ChatColor.RED + "Too few arguments." );
 					return true;
 				}
 				MResult res;
 				if( ( res = auth.unregister( args[0], getServer().getPlayer( args[0] ) ) ) == MResult.RES_SUCCESS )
 					p.sendMessage( ChatColor.GREEN + "Unregistered player " + args[0] + " successfully." );
 				else
 					p.sendMessage( ChatColor.RED + "Couldn't unregister player " + args[0] + ": " + String.valueOf( res ) );
 				return true;
 			}
 
 			if( cmd.getName().equalsIgnoreCase( "uvkick" ) )
 			{
 				if( ( new kickCommand( this, args ) ).run( p ) == commandResult.RES_SUCCESS )
 					return true;
 			}
 			else if( cmd.getName().equalsIgnoreCase( "uvconfig" ) )
 			{
 				if( ( new configCommand( this, args ) ).run( p ) == commandResult.RES_SUCCESS )
 					return true;
 			}
 			else if( cmd.getName().equalsIgnoreCase( "uvbackendkick" ) )
 			{
 				if( ( new backendkickCommand( this, args ) ).run( p ) == commandResult.RES_SUCCESS )
 					return true;
 			}
 			else if( cmd.getName().equalsIgnoreCase( "uvwarn" ) )
 			{
 				if( ( new warnCommand( this, args ) ).run( p ) == commandResult.RES_SUCCESS )
 					return true;
 			}
 			else if( cmd.getName().equalsIgnoreCase( "uvtempwarn" ) )
 			{
 				if( ( new warnCommand( this, args ) ).run( p ) == commandResult.RES_SUCCESS )
 					return true;
 			}
 			else if( cmd.getName().equalsIgnoreCase( "uvunwarn" ) )
 			{
 				if( ( new unwarnCommand( this, args ) ).run( p ) == commandResult.RES_SUCCESS )
 					return true;
 			}
 			else if( cmd.getName().equalsIgnoreCase( "uvpraise" ) )
 			{
 				if( ( new praiseCommand( this, args ) ).run( p ) == commandResult.RES_SUCCESS )
 					return true;
 			}
 			else if( cmd.getName().equalsIgnoreCase( "uvunpraise" ) )
 			{
 				if( ( new unpraiseCommand( this, args ) ).run( p ) == commandResult.RES_SUCCESS )
 					return true;
 			}
 			else if( cmd.getName().equalsIgnoreCase( "uvban" ) )
 			{
 				if( ( new banCommand( this, args ) ).run( p ) == commandResult.RES_SUCCESS )
 					return true;
 			}
 			else if( cmd.getName().equalsIgnoreCase( "uvunban" ) )
 			{
 				if( ( new unbanCommand( this, args ) ).run( p ) == commandResult.RES_SUCCESS )
 					return true;
 			}
 			else if( cmd.getName().equalsIgnoreCase( "uvtempban" ) )
 			{
 				if( ( new tempbanCommand( this, args ) ).run( p ) == commandResult.RES_SUCCESS )
 					return true;
 			}
 			else if( cmd.getName().equalsIgnoreCase( "uvgc" ) )
 			{
 				if( ( new gcCommand( this, args ) ).run( p ) == commandResult.RES_SUCCESS )
 					return true;
 			}
 			else if( cmd.getName().equalsIgnoreCase( "uvnote" ) )
 			{
 				if( ( new noteCommand( this, args ) ).run( p ) == commandResult.RES_SUCCESS )
 					return true;
 			}
 			else if( cmd.getName().equalsIgnoreCase( "uvdelnote" ) )
 			{
 				if( ( new delnoteCommand( this, args ) ).run( p ) == commandResult.RES_SUCCESS )
 					return true;
 			}
 			else if( cmd.getName().equalsIgnoreCase( "uvaddfriend" ) )
 			{
 				if( ( new addfriendCommand( this, args ) ).run( p ) == commandResult.RES_SUCCESS )
 					return true;
 			}
 			else if( cmd.getName().equalsIgnoreCase( "uvacceptfriend" ) )
 			{
 				if( ( new accfriendCommand( this, args ) ).run( p ) == commandResult.RES_SUCCESS )
 					return true;
 			}
 			else if( cmd.getName().equalsIgnoreCase( "uvdelfriend" ) )
 			{
 				if( ( new delfriendCommand( this, args ) ).run( p ) == commandResult.RES_SUCCESS )
 					return true;
 			}
 			else if( cmd.getName().equalsIgnoreCase( "uvstat" ) )
 			{
 				if( ( new statCommand( this, args ) ).run( p ) == commandResult.RES_SUCCESS )
 					return true;
 			}
 			else if( cmd.getName().equalsIgnoreCase( "uvingamelog" ) )
 			{
 				if( ( new ingamelogCommand( this, args ) ).run( p ) == commandResult.RES_SUCCESS )
 					return true;
 			}
 			else if( cmd.getName().equalsIgnoreCase( "uvsay" ) )
 			{
 				if( ( new sayCommand( this, args ) ).run( p ) == commandResult.RES_SUCCESS )
 					return true;
 			}
 			else if( cmd.getName().equalsIgnoreCase( "uvspamsay" ) )
 			{
 				if( ( new spamsayCommand( this, args ) ).run( p ) == commandResult.RES_SUCCESS )
 					return true;
 			}
 
 		}
 		else
 		{    // Running command from console
 
 			if( cmd.getName().equalsIgnoreCase( "ultravision" ) )
 			{
 				MLog.i( "Running UltraVision " + fPDesc.getVersion() + "." );
 			}
 
 			if( cmd.getName().equalsIgnoreCase( "uvkick" ) )
 			{
 				if( ( new kickCommand( this, args ) ).consoleRun( sender ) == commandResult.RES_SUCCESS )
 					return true;
 			}
 			else if( cmd.getName().equalsIgnoreCase( "uvbackendkick" ) )
 			{
 				if( ( new backendkickCommand( this, args ) ).consoleRun( sender ) == commandResult.RES_SUCCESS )
 					return true;
 			}
 			else if( cmd.getName().equalsIgnoreCase( "uvsay" ) )
 			{
 				if( ( new sayCommand( this, args ) ).consoleRun( sender ) == commandResult.RES_SUCCESS )
 					return true;
 			}
 			else if( cmd.getName().equalsIgnoreCase( "uvspamsay" ) )
 			{
 				if( ( new spamsayCommand( this, args ) ).consoleRun( sender ) == commandResult.RES_SUCCESS )
 					return true;
 			}
 
 		}
 		return false;
 	}
 
 	//**********************************************************************************************
 	//                      G E T    I N S T A N C E S
 	public MAuthorizer getAuthorizer()
 	{
 		return auth;
 	}
 
 	public UVClickAuth getClickAuth()
 	{
 		return clickauth;
 	}
 
 	public UltraVisionAPI getAPI()
 	{
 		return api;
 	}
 
 	public JMessage getMessager()
 	{
 		return jmsg;
 	}
 
 	public MConfiguration getMConfig()
 	{
 		return config;
 	}
 
 	public boolean showNotRegWarning()
 	{
 		return showNotRegWarning;
 	}
 
 	public boolean useCommandLog()
 	{
 		return useCommandLog;
 	}
 
 	//**********************************************************************************************
 	/**
 	 * Better Broadcast, that won't crash or show the message twice
 	 */
 	public int ownBroadcast( String message )
 	{
 		Player[] ps = getServer().getOnlinePlayers();
 		int cnt = 0;
 		for( Player p : ps )
 		{
 			p.sendMessage( message );
 			cnt++;
 		}
 		return cnt + 1;
 	}
 
 	//**********************************************************************************************
 	/**
 	 * Get Private properties
 	 */
 	public boolean IsUsingAuthorizer()
 	{
 		return useAuthorizer;
 	}
 }
