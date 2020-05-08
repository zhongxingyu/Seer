 package com.ath.bukkit.safespawn;
 
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import org.bukkit.Chunk;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerMoveEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 
 import com.ath.bukkit.safespawn.data.BlockStore;
 import com.ath.bukkit.safespawn.data.ChunkBlocksLoader;
 import com.ath.bukkit.safespawn.data.Task;
 import com.google.common.collect.Sets;
 
 public class GarbageCollection {
 
 	private static Map<String, AtomicInteger> chunkPlayerCount = new ConcurrentHashMap<String, AtomicInteger>();
 
 	public static void init( TaskManager taskmgr ) {
 		taskmgr.addRepeatingTask( new Task() {
 			@Override
 			public void run() {
 				try {
 					// find all active chunks
 					Set<String> activeChunks = Sets.newHashSet();
 					for ( World w : SafeSpawn.instance().getServer().getWorlds() ) {
 						for ( Player p : SafeSpawn.instance().getServer().getWorld( w.getUID() ).getPlayers() ) {
 							Chunk pChunk = p.getLocation().getChunk();
 							int cx = pChunk.getX();
 							int cz = pChunk.getZ();
 							for ( int x = ( cx - 1 ); x <= ( cx + 1 ); x++ ) {
 								for ( int z = ( cz - 1 ); z <= ( cz + 1 ); z++ ) {
									activeChunks.add( BlockStore.toChunkHash( w.getName(), x, z ) );
 								}
 							}
 						}
 					}
 
 					// now clear cache for chunks that are cached but are not found as active above.
 					Set<String> chunkKeys = new HashSet<String>( SafeSpawn.instance().getBlockStore().getCachedChunks() );
 					for ( String chunkHash : chunkKeys ) {
 						if ( activeChunks.contains( chunkHash ) ) {
 							continue;
 						}
 
 						boolean hasPlayers = false;
						Chunk chunk = BlockStore.toChunk( chunkHash );
						for ( Entity e : chunk.getEntities() ) {
 							if ( e instanceof Player ) {
 								hasPlayers = true;
 								break;
 							}
 						}
 
 						if ( !hasPlayers ) {
 							SafeSpawn.instance().getBlockStore().removeFromCache( chunkHash );
							getCounter( chunk ).set( 0 ); // no players, make sure its zero'd
 						}
 					}
 				} catch ( Exception e ) {
 					Log.error( e );
 				}
 			}
 		} );
 	}
 
 	private static AtomicInteger getCounter( Chunk chunk ) {
 		String hash = BlockStore.toHash( chunk );
 		AtomicInteger i = chunkPlayerCount.get( hash );
 		if ( i == null ) {
 			i = new AtomicInteger();
 			chunkPlayerCount.put( hash, i );
 		}
 		return i;
 	}
 
 	private static void inc( Chunk chunk ) {
 		int changed = getCounter( chunk ).incrementAndGet();
 		if ( changed == 1 ) {
 			onPlayerEnteredNewChunk( SafeSpawn.instance(), chunk );
 		}
 	}
 
 	// don't automate cleanup of chunks, we'll let a scheduled task take care of that
 	private static void dec( Chunk chunk ) {
 		getCounter( chunk ).decrementAndGet();
 	}
 
 	public static void onPlayerPortalEvent( SafeSpawn plugin, PlayerMoveEvent event ) {
 		onPlayerMoveEvent( plugin, event );
 	}
 
 	public static void onPlayerTeleportEvent( SafeSpawn plugin, PlayerMoveEvent event ) {
 		onPlayerMoveEvent( plugin, event );
 	}
 
 	public static void onPlayerMoveEvent( SafeSpawn plugin, PlayerMoveEvent event ) {
 		try {
 			Location from = event.getFrom();
 			Location to = event.getTo();
 			if ( from.getChunk() != to.getChunk() ) { // chunk change
 				dec( from.getChunk() );
 				inc( to.getChunk() );
 			}
 		} catch ( Exception e ) {
 			Log.error( e );
 		}
 	}
 
 	public static void onPlayerQuitEvent( SafeSpawn plugin, PlayerQuitEvent event ) {
 		try {
 			Location from = event.getPlayer().getLocation();
 			dec( from.getChunk() );
 		} catch ( Exception e ) {
 			Log.error( e );
 		}
 	}
 
 	public static void onPlayerJoinEvent( SafeSpawn plugin, PlayerJoinEvent event ) {
 		try {
 			Location to = event.getPlayer().getLocation();
 			inc( to.getChunk() );
 		} catch ( Exception e ) {
 			Log.error( e );
 		}
 	}
 
 	private static void onPlayerEnteredNewChunk( SafeSpawn plugin, Chunk chunk ) {
 		try {
 			Log.line( "Player entered: %s %sx%s", chunk.getWorld().getName(), chunk.getX(), chunk.getZ() );
 			plugin.getTaskman().addNonRepeatingTask( new ChunkBlocksLoader( chunk ) );
 		} catch ( Exception e ) {
 			Log.error( e );
 		}
 	}
 }
