 package com.ath.bukkit.safespawn;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 
 import org.bukkit.Location;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockPlaceEvent;
 import org.bukkit.event.entity.CreatureSpawnEvent;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityExplodeEvent;
 import org.bukkit.event.inventory.InventoryOpenEvent;
 import org.bukkit.event.player.AsyncPlayerChatEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerMoveEvent;
 import org.bukkit.event.player.PlayerPortalEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.event.player.PlayerTeleportEvent;
 import org.bukkit.event.world.ChunkLoadEvent;
 import org.bukkit.event.world.ChunkUnloadEvent;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.ath.bukkit.safespawn.cmd.CastCmd;
 import com.ath.bukkit.safespawn.cmd.LinesReaderCmd;
 import com.ath.bukkit.safespawn.cmd.NickCmd;
 import com.ath.bukkit.safespawn.cmd.SpawnCmd;
 import com.ath.bukkit.safespawn.data.BlockData;
 import com.ath.bukkit.safespawn.data.BlockStore;
 import com.ath.bukkit.safespawn.data.Persisted;
 import com.ath.bukkit.safespawn.data.PlayerData;
 import com.ath.bukkit.safespawn.data.PlayerStore;
 import com.ath.bukkit.safespawn.data.SimpleKeyVal;
 import com.ath.bukkit.safespawn.data.Task;
 import com.ath.bukkit.safespawn.event.BlockEventHandler;
 import com.ath.bukkit.safespawn.event.ChunkEventHandler;
 import com.ath.bukkit.safespawn.event.EntityEventHandler;
 import com.ath.bukkit.safespawn.event.PlayerEventHandler;
 import com.ath.bukkit.safespawn.event.ZoneEventHandler;
 import com.avaje.ebeaninternal.api.SpiEbeanServer;
 import com.avaje.ebeaninternal.server.ddl.DdlGenerator;
 
 public class SafeSpawn extends JavaPlugin {
 
 	private static SafeSpawn self;
 	private ZoneManager zoneManager;
 	private PlayerManager playerManager;
 	private Location spawnLocation;
 	private WorldsManager worldsManager;
 
 	private PlayerStore playerStore;
 	private BlockStore blockStore;
 
 	private TaskManager taskman;
 
 	public static final SafeSpawn instance() {
 		return self;
 	}
 
 	@Override
 	public void saveConfig() {
 		super.saveConfig();
 		Log.line( "saveConfig" );
 	}
 
 	@Override
 	public void saveDefaultConfig() {
 		super.saveDefaultConfig();
 		Log.line( "saveDefaultConfig" );
 	}
 
 	@Override
 	public void onLoad() {
 		super.onLoad();
 		Log.init( this );
 	}
 
 	@Override
 	public void onEnable() {
 		try {
 			SafeSpawn.self = this;
 
 			try {
 				setupDatabase();
 				dbTestExample();
 			} catch ( Exception e ) {
 				Log.error( e );
 			}
 
 			zoneManager = new ZoneManager( this );
 			playerManager = new PlayerManager( this );
 			worldsManager = new WorldsManager( this );
 
 			playerStore = new PlayerStore( getDatabase() );
 			blockStore = new BlockStore( getDatabase() );
 		} catch ( Exception e ) {
 			Log.error( e );
 		}
 
 
 		try {
 			initializeConfig();
 			initializeEvents();
 			initializeCommands();
 			playerManager.initialize();
 
			//getBlockStore().primeTheCache();
 
 			taskman = new TaskManager();
 
 			// tasks
 			getTaskman().addRepeatingTask( new Task() {
 				@Override
 				public void run() {
 					getBlockStore().syncAll();
 				}
 			} );
 			
 			GarbageCollection.init( getTaskman() );
 		} catch ( Exception e ) {
 			Log.error( e );
 		}
 	}
 
 	@Override
 	public void onDisable() {
 		super.onDisable();
 		Log.line( "onDisable" );
 
 		try {
 			getTaskman().shutdown();
 			taskman = null;
 
 			// blockStore.syncAll();
 			blockStore = null;
 			playerStore = null;
 			worldsManager = null;
 			playerManager = null;
 			zoneManager = null;
 		} catch ( Exception e ) {
 			Log.error( e );
 		}
 	}
 
 	private void initializeConfig() {
 		this.saveDefaultConfig(); // this does not overwrite if config.yml already exists, bad naming...
 
 		worldsManager.initialize();
 		getZoneManager().initializeFromConfig();
 		getZoneManager().logAllZones();
 
 		spawnLocation = CfgHelper.readLocationFromConfig( getConfig().getConfigurationSection( Const.CFG_spawnpoint ) );
 	}
 
 	private void initializeEvents() {
 		getServer().getPluginManager().registerEvents( new Listener() {
 			@EventHandler
 			public void creatureSpawn( CreatureSpawnEvent event ) {
 				ZoneEventHandler.onCreatureSpawnEvent( SafeSpawn.this, event );
 			}
 
 			@EventHandler
 			public void entityDamagedByEntity( EntityDamageByEntityEvent event ) {
 				PlayerEventHandler.onEntityDamagedByEntity( SafeSpawn.this, event );
 			}
 
 			@EventHandler
 			public void playerJoin( PlayerJoinEvent event ) {
 				PlayerEventHandler.onPlayerJoin( SafeSpawn.this, event );
 				GarbageCollection.onPlayerJoinEvent( SafeSpawn.this, event );
 			}
 
 			@EventHandler
 			public void playerLeave( PlayerQuitEvent event ) {
 				PlayerEventHandler.onPlayerLeave( SafeSpawn.this, event );
 				GarbageCollection.onPlayerQuitEvent( SafeSpawn.this, event );
 			}
 
 			@EventHandler
 			public void playerInteractEvent( PlayerInteractEvent event ) {
 				PlayerEventHandler.onPlayerInteractEvent( SafeSpawn.this, event );
 			}
 
 			@EventHandler
 			public void playerMoveEvent( PlayerMoveEvent event ) {
 				GarbageCollection.onPlayerMoveEvent( SafeSpawn.this, event );
 			}
 
 			@EventHandler
 			public void playerTeleportEvent( PlayerTeleportEvent event ) {
 				GarbageCollection.onPlayerMoveEvent( SafeSpawn.this, event );
 			}
 
 			@EventHandler
 			public void playerPortalEvent( PlayerPortalEvent event ) {
 				GarbageCollection.onPlayerMoveEvent( SafeSpawn.this, event );
 			}
 
 			@EventHandler
 			public void playerChatEvent( AsyncPlayerChatEvent event ) {
 				PlayerEventHandler.onAsyncPlayerChatEvent( SafeSpawn.this, event );
 			}
 
 			@EventHandler
 			public void onInventoryOpenEvent( InventoryOpenEvent event ) {
 				PlayerEventHandler.onInventoryOpenEvent( SafeSpawn.this, event );
 			}
 
 			@EventHandler
 			public void blockBreakEvent( BlockBreakEvent event ) {
 				BlockEventHandler.onBlockBreakEvent( SafeSpawn.this, event );
 			}
 
 			// @EventHandler
 			// public void blockFadeEvent( BlockFadeEvent event ) {
 			// BlockEventHandler.onBlockFadeEvent( SafeSpawn.this, event );
 			// }
 
 			@EventHandler
 			public void blockPlaceEvent( BlockPlaceEvent event ) {
 				BlockEventHandler.onBlockPlaceEvent( SafeSpawn.this, event );
 			}
 
 			@EventHandler
 			public void chunkLoadEvent( ChunkLoadEvent event ) {
 				ChunkEventHandler.onChunkLoadEvent( SafeSpawn.this, event );
 			}
 
 			@EventHandler
 			public void chunkUnloadEvent( ChunkUnloadEvent event ) {
 				ChunkEventHandler.onChunkUnloadEvent( SafeSpawn.this, event );
 			}
 
 			@EventHandler
 			public void entityExplodeEvent( EntityExplodeEvent event ) {
 				EntityEventHandler.onEntityExplode( SafeSpawn.this, event );
 			}
 
 			// @EventHandler
 			// public void blockEvent( BlockEvent event ) {
 			// BlockEventHandler.onBlockEvent( SafeSpawn.this, event );
 			// }
 
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
 		getCommand( Const.CMD_cast ).setExecutor( new CastCmd( this ) );
 		getCommand( Const.CMD_casthelp ).setExecutor( new LinesReaderCmd( this, Const.MSG_casthelp ) );
 		getCommand( Const.CMD_nick ).setExecutor( new NickCmd( this ) );
 	}
 
 	public PlayerStore getPlayerStore() {
 		return playerStore;
 	}
 
 	public BlockStore getBlockStore() {
 		return blockStore;
 	}
 
 	public Location getSpawnLocation() {
 		return spawnLocation;
 	}
 
 	public ZoneManager getZoneManager() {
 		return zoneManager;
 	}
 
 	public PlayerManager getPlayerManager() {
 		return playerManager;
 	}
 
 	public WorldsManager getWorldsManager() {
 		return worldsManager;
 	}
 
 	private void setupDatabase() {
 		try {
 			for ( Class<?> clazz : getDatabaseClasses() ) {
 				try {
 					getDatabase().find( clazz ).findRowCount();
 				} catch ( Exception e ) {
 					initializePersistedClass( clazz );
 				}
 			}
 		} catch ( Exception e ) {
 			Log.error( e );
 		}
 	}
 
 	private void initializePersistedClass( Class<?> clazz ) {
 		try {
 			Object o = clazz.newInstance();
 			if ( o instanceof Persisted ) {
 				Persisted persisted = (Persisted) o;
 				try {
 					Log.line( "Initializing: " + clazz.getName() );
 					SpiEbeanServer db = (SpiEbeanServer) getDatabase();
 					DdlGenerator gen = db.getDdlGenerator();
 					for ( String query : persisted.getSchema() ) {
 						Log.line( gen.generateCreateDdl() );
 						gen.runScript( true, query );
 					}
 				} catch ( Exception e3 ) {
 					// mute
 				}
 			} else {
 				Log.line( "All Database Objects must implement Pesisted" );
 			}
 		} catch ( Exception e2 ) {
 			Log.error( e2 );
 		}
 	}
 
 	@Override
 	public List<Class<?>> getDatabaseClasses() {
 		List<Class<?>> out = new ArrayList<Class<?>>();
 		out.add( SimpleKeyVal.class );
 		out.add( PlayerData.class );
 		out.add( BlockData.class );
 		return out;
 	}
 
 	public void dbTestExample() {
 		try {
 			Set<SimpleKeyVal> old = getDatabase().find( SimpleKeyVal.class ).where().ieq( "key", "test key" ).findSet();
 			if ( old != null ) {
 				getDatabase().delete( old );
 			}
 
 			SimpleKeyVal testOrig = new SimpleKeyVal();
 			testOrig.setKey( "test key" );
 			testOrig.setValue( "test value" );
 			Log.line( "Test: " + testOrig.getId() + ", " + testOrig.getKey() + " = " + testOrig.getValue() );
 			getDatabase().save( testOrig );
 
 			SimpleKeyVal test = getDatabase().find( SimpleKeyVal.class ).where().ieq( "key", "test key" ).findUnique();
 			Log.line( "Test: " + test.getId() + ", " + test.getKey() + " = " + test.getValue() );
 
 			getDatabase().delete( test );
 		} catch ( Exception e ) {
 			Log.error( e );
 		}
 	}
 
 	public TaskManager getTaskman() {
 		return taskman;
 	}
 
 }
