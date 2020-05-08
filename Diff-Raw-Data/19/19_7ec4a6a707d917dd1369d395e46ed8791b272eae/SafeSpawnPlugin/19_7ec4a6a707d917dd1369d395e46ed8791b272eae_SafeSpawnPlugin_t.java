 package com.ath.bukkit.safespawn;
 
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.bukkit.Location;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockPlaceEvent;
 import org.bukkit.event.entity.CreatureSpawnEvent;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.ath.bukkit.safespawn.cmd.LinesReaderCmd;
 import com.ath.bukkit.safespawn.cmd.SpawnCmd;
 import com.ath.bukkit.safespawn.data.PlayerDao;
 import com.ath.bukkit.safespawn.event.BlockEventHandler;
 import com.ath.bukkit.safespawn.event.PlayerEventHandler;
 import com.ath.bukkit.safespawn.event.ZoneEventHandler;
 
 public class SafeSpawnPlugin extends JavaPlugin {
 
 	private static SafeSpawnPlugin self;
 	private static Logger logger;
 	private ZoneManager zoneManager;
 	private PlayerManager playerManager;
 	private Location spawnLocation;
 	private PlayerDao dao;
 	private WorldsManager worldsManager;
 
 	public static final SafeSpawnPlugin instance() {
 		return self;
 	}
 
 	@Override
 	public void onLoad() {
 		SafeSpawnPlugin.self = this;
 		super.onLoad();
 
 		logger = getLogger();
 		dao = new PlayerDao( this );
 		zoneManager = new ZoneManager( this );
 		playerManager = new PlayerManager( this );
 		worldsManager = new WorldsManager( this );
 	}
 
 	@Override
 	public void onEnable() {
 		try {
 			initializeConfig();
 			initializeEvents();
 			initializeCommands();
 			playerManager.initialize();
 		} catch ( Exception e ) {
 			logger.log( Level.SEVERE, e.getMessage(), e );
 		}
 	}
 
 	private void initializeConfig() {
 		this.saveDefaultConfig(); // this does not overwrite if config.yml already exists, bad naming...
 
 		// FIXME: if this fails, the plugin is useless
 		// the plugin should shut its services down
 		// maybe just disable the server?
 
 		worldsManager.initialize();
 		getZoneManager().initializeFromConfig();
 		getZoneManager().logAllZones();
 
 		spawnLocation = CfgHelper.readLocationFromConfig( getConfig().getConfigurationSection( Const.CFG_spawnpoint ) );
 	}
 
 	private void initializeEvents() {
 		getServer().getPluginManager().registerEvents( new Listener() {
 			@EventHandler
 			public void creatureSpawn( CreatureSpawnEvent event ) {
 				ZoneEventHandler.onCreatureSpawnEvent( SafeSpawnPlugin.this, event );
 			}
 
 			@EventHandler
 			public void entityDamagedByEntity( EntityDamageByEntityEvent event ) {
 				PlayerEventHandler.onEntityDamagedByEntity( SafeSpawnPlugin.this, event );
 			}
 
 			@EventHandler
 			public void playerJoin( PlayerJoinEvent event ) {
 				PlayerEventHandler.onPlayerJoin( SafeSpawnPlugin.this, event );
 			}
 
 			@EventHandler
 			public void playerLeave( PlayerQuitEvent event ) {
 				PlayerEventHandler.onPlayerLeave( SafeSpawnPlugin.this, event );
 			}
 
 			@EventHandler
 			public void playerInteractEvent( PlayerInteractEvent event ) {
 				PlayerEventHandler.onPlayerInteractEvent( SafeSpawnPlugin.this, event );
 			}
			
 			@EventHandler
 			public void blockBreakEvent( BlockBreakEvent event ) {
 				BlockEventHandler.onBlockBreakEvent( SafeSpawnPlugin.this, event );
 			}
 
 			@EventHandler
 			public void blockPlaceEvent( BlockPlaceEvent event ) {
 				BlockEventHandler.onBlockPlaceEvent( SafeSpawnPlugin.this, event );
 			}
 
 			// @EventHandler
 			// public void signChangeEvent( SignChangeEvent event ) {
 			// BlockEventHandler.onSignChangeEvent( SafeSpawnPlugin.this, event );
 			// }
 
 		}, this );
 	}
 
 	private void initializeCommands() {
 		getCommand( Const.CMD_rules ).setExecutor( new LinesReaderCmd( this, Const.MSG_rules ) );
 		getCommand( Const.CMD_gamedesc ).setExecutor( new LinesReaderCmd( this, Const.MSG_gamedesc ) );
 		getCommand( Const.CMD_spawn ).setExecutor( new SpawnCmd( this ) );
 	}
 
 	public PlayerDao getPlayerDao() {
 		return dao;
 	}
 
 	public Location getSpawnLocation() {
 		return spawnLocation;
 	}
 
 	public ZoneManager getZoneManager() {
 		return zoneManager;
 	}
 
 	public static void logError( Exception e ) {
 		logger.log( Level.SEVERE, e.getMessage() );
 		for ( StackTraceElement el : e.getStackTrace() ) {
 			logger.log( Level.SEVERE, el.getFileName() + ":" + el.getLineNumber() );
 		}
 	}
 
 	public static void logLine() {
 		logLine( null );
 	}
 
 	public static void logLine( String msg ) {
 		Throwable t = new Throwable();
 		StackTraceElement el = t.getStackTrace()[1];
 		if ( msg == null || msg.isEmpty() ) {
 			logger.info( String.format( "%s: %s", el.getFileName(), el.getLineNumber() ) );
 		} else {
 			logger.info( String.format( "%s: %s: %s", msg, el.getFileName(), el.getLineNumber() ) );
 		}
 	}
 
 	public PlayerManager getPlayerManager() {
 		return playerManager;
 	}
 
 	public WorldsManager getWorldsManager() {
 		return worldsManager;
 	}
 }
