 package net.nabaal.majiir.realtimerender;
 
 import org.bukkit.Chunk;
 import org.bukkit.Location;
 import org.bukkit.entity.Player;
 import org.bukkit.event.world.ChunkUnloadEvent;
 
 public class DiscoverTerrainTask implements Runnable {
 
 	private final RealtimeRender plugin;
 	private final double threshold;
 	
 	private int step;
 	private int last = 0;
 	
 	public DiscoverTerrainTask(RealtimeRender plugin, double threshold) {
 		this(plugin, threshold, 0);
 	}
 	
 	public DiscoverTerrainTask(RealtimeRender plugin, double threshold, int start) {
 		this.plugin = plugin;
 		this.threshold = threshold;
 		this.step = start;
 		this.last = start;
 	}
 	
 	@Override
 	public void run() {
 		aggressiveUnload();
 		Coordinate c;
 		do {
			step++;
 			if (isBeyondThreshold()) {
 				step = 0;
 			}
 			c = getCurrentPosition();
 		} while (!tryLoadChunk(c.getX(), c.getY()));
 		last = step;		
 	}
 	
 	private boolean isBeyondThreshold() {
 		return (step > (last * threshold));
 	}
 	
 	private Coordinate getCurrentPosition() {
 		int x;
 		int z;
 		
 		int n = (int) Math.floor((Math.sqrt(step) + 1) / 2);
 		int l = (2 * n) - 1;
 		int d = step - (l * l);
 		int k = (n > 0) ? (d / (2 * n)) : 0;
 		int f = d - (k * (2 * n));
 		
 		switch (k) {
 		case 0:
 			x = n;
 			z = (1 - n) + f;
 			break;
 		case 1:
 			x = (n - 1) - f;
 			z = n;
 			break;
 		case 2:
 			x = -n;
 			z = (n - 1) - f;
 			break;
 		case 3:
 			x = (1 - n) + f;
 			z = -n;
 			break;
 		default:
 			throw new IllegalStateException("Something went wrong!");
 		}
 		
 		return new Coordinate(x, z, Coordinate.LEVEL_CHUNK);
 	}
 	
 	private boolean tryLoadChunk(int x, int z) {
 		return this.plugin.getWorld().loadChunk(x, z, false);
 	}
 	
 	private void aggressiveUnload() {
 		for (Chunk chunk : this.plugin.getWorld().getLoadedChunks()) {
 			if (!isChunkInUse(chunk)) {
 				ChunkUnloadEvent event = new ChunkUnloadEvent(chunk);
 				plugin.getServer().getPluginManager().callEvent(event);
 				if (!event.isCancelled()) {
 					chunk.unload(true, false);
 				}
 			}
 		}
 	}
 	
 	private boolean isChunkInUse(Chunk chunk) {
 		int x = chunk.getX();
 		int z = chunk.getZ();
 		for (Player player : this.plugin.getWorld().getPlayers()) {
 			Location loc = player.getLocation();
 			if ((Math.abs(loc.getBlockX() - (x << 4)) <= 128) && (Math.abs(loc.getBlockZ() - (z << 4)) <= 128)) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 }
