 package btwmod.livemap;
 
 import java.io.File;
 import java.util.ArrayDeque;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Deque;
 import java.util.List;
 import java.util.Random;
 
 import net.minecraft.src.AnvilChunkLoader;
 import net.minecraft.src.Chunk;
 import net.minecraft.src.IChunkLoader;
 import net.minecraft.src.ICommandSender;
 import net.minecraft.src.RegionFile;
 import net.minecraft.src.RegionFileCache;
 import net.minecraft.src.WorldServer;
 
 import btwmods.IMod;
 import btwmods.ModLoader;
 import btwmods.io.Settings;
 import btwmods.server.ITickListener;
 import btwmods.server.TickEvent;
 
 public class RegionLoader implements ITickListener {
 	
 	public int regionChunksDequeLimit = 50;
 	public int regionChunksDequeTicks = 20;
 	public int regionChunkQueueThreshold = 150;
 	
 	private mod_LiveMap mod;
 	
 	private int lastReportedPercentage = 0;
 	private long lastReportedTime = 0;
 	private int processed = 0;
 	private int total = 0;
 	
 	private Deque<QueuedRegion> regionQueue = new ArrayDeque<QueuedRegion>();
 	
 	public RegionLoader(mod_LiveMap mod, Settings settings) {
 		this.mod = mod;
 		
 		regionChunksDequeLimit = settings.getInt("regionChunksDequeLimit", regionChunksDequeLimit);
 		regionChunksDequeTicks = settings.getInt("regionChunksDequeTicks", regionChunksDequeTicks);
 		regionChunkQueueThreshold = settings.getInt("regionChunkQueueThreshold", regionChunkQueueThreshold);
 	}
 	
 	private void reset(int total) {
 		lastReportedTime = System.currentTimeMillis();
 		lastReportedPercentage = processed = 0;
 		this.total = total;
 	}
 	
 	public int getQueueRemaining() {
 		return total - processed;
 	}
 	
 	public boolean queueRegion(int worldIndex, int x, int z) {
 		return queueRegion(worldIndex, x, z, null);
 	}
 	
 	public boolean queueRegion(int worldIndex, int x, int z, ICommandSender sender) {
 		if (total > 0) {
 			if (sender != null)
 				sender.sendChatToPlayer("Can't queue any more regions since there are still chunks queued.");
 		}
 		else {
 			IChunkLoader loader = mod.getChunkLoader(worldIndex);
 			if (loader != null && loader instanceof AnvilChunkLoader) {
 				
 				File location = mod.getAnvilSaveLocation(worldIndex);
 				if (location != null) {
 					if (new File(new File(location, "region"), "r." + x + "." + z + ".mca").isFile()) {
 						regionQueue.add(new QueuedRegion(worldIndex, location, x, z));
 						reset(32 * 32);
 						return true;
 					}
 					else if (sender != null) {
 						sender.sendChatToPlayer("Region file for " + x + "." + z + " does not exist.");
 					}
 				}
 				else if (sender != null) {
 					sender.sendChatToPlayer("The anvil save location could not be determined for the world.");
 				}
 			}
 			else if (sender != null) {
 				sender.sendChatToPlayer("The IChunkLoader could not be retrieved or is not an instance of AnvilChunkLoader.");
 			}
 		}
 		
 		return false;
 	}
 	
 	public int queueWorld(int worldIndex) {
 		return queueWorld(worldIndex, null);
 	}
 	
 	public int queueWorld(int worldIndex, ICommandSender sender) {
 		if (total > 0) {
 			if (sender != null)
 				sender.sendChatToPlayer("Can't queue any more regions since there are still chunks queued.");
 		}
 		else {
 			IChunkLoader loader = mod.getChunkLoader(worldIndex);
 			if (loader != null && loader instanceof AnvilChunkLoader) {
 				
 				File location = mod.getAnvilSaveLocation(worldIndex);
 				if (location != null) {
 					
 					File regionDir = new File(location, "region");
 					if (regionDir.isDirectory()) {
 						
 						File[] files = regionDir.listFiles();
 						if (files != null) {
 							
 							List<QueuedRegion> regions = new ArrayList<QueuedRegion>();
 							int count = 0;
 							
 							for (File file : files) {
 								if (file.isFile() && file.getName().matches("^r\\.[\\-0-9]+\\.[\\-0-9]+\\.mca$")) {
 									String[] split = file.getName().split("\\.");
 									regions.add(new QueuedRegion(worldIndex, location, Integer.parseInt(split[1]), Integer.parseInt(split[2])));
 									count++;
 								}
 							}
 							
 							Collections.shuffle(regions, new Random(System.nanoTime()));
 							for (QueuedRegion region : regions) {
 								regionQueue.add(region);
 							}
 							
 							reset(count * 32 * 32);
 							
 							return count;
 						}
 						else if (sender != null) {
 							sender.sendChatToPlayer("The files could not be listed for the 'region' directory in the world's anvil save location.");
 						}
 					}
 					else if (sender != null) {
 						sender.sendChatToPlayer("The anvil save location for the world does not contain a 'region' directory.");
 					}
 				}
 				else if (sender != null) {
 					sender.sendChatToPlayer("The anvil save location could not be determined for the world.");
 				}
 			}
 			else if (sender != null) {
 				sender.sendChatToPlayer("The IChunkLoader could not be retrieved or is not an instance of AnvilChunkLoader.");
 			}
 		}
 		
 		return -1;
 	}
 
 	@Override
 	public void onTick(TickEvent event) {
 		if (total > 0
 			&& event.getType() == TickEvent.TYPE.END
 			&& event.getTickCounter() % regionChunksDequeTicks == 0
 			&& mod.getChunkQueueCount() < regionChunkQueueThreshold) {
 			
 			if (processed >= total) {
 				reset(0);
 				
 				int queueCheck = regionQueue.size();
 				if (queueCheck > 0) {
					ModLoader.outputError(mod.getName() + "'s " + RegionLoader.class.getSimpleName() + " completed but had " + queueCheck + " remaining in the queue");
 					regionQueue.clear();
 				}
 			}
 			
 			QueuedRegion region;
 			int polled = 0;
 			while (++polled <= regionChunksDequeLimit && (region = regionQueue.pollLast()) != null) {
 				if (region instanceof QueuedRegionChunk) {
 					processed++;
 					
 					if (processed * 100 / total > lastReportedPercentage && lastReportedTime + 5000 < System.currentTimeMillis()) {
 						lastReportedPercentage = processed * 100 / total;
 						ModLoader.outputInfo(mod.getName() + " processed " + processed + " (" + lastReportedPercentage + "%) chunks.");
 						lastReportedTime = System.currentTimeMillis();
 					}
 					
 					QueuedRegionChunk regionChunk = (QueuedRegionChunk)region;
 					if (((WorldServer)regionChunk.world).theChunkProviderServer.chunkExists(regionChunk.chunkX, regionChunk.chunkZ)) {
 						System.out.println("Skipped loaded chunk " + regionChunk.chunkX + "." + regionChunk.chunkZ);
 					}
 					else {
 						try {
 							Chunk chunk = mod.getChunkLoader(regionChunk.worldIndex).loadChunk(regionChunk.world, regionChunk.chunkX, regionChunk.chunkZ);
 							
 							if (chunk == null) {
 								ModLoader.outputError(mod.getName() + " failed to get chunk input stream for chunk " + regionChunk.chunkX + "," + regionChunk.chunkZ + " in world " + regionChunk.worldIndex);
 							}
 							else {
 								// Remove any tick updates that were queued.
 								regionChunk.world.getPendingBlockUpdates(chunk, true);
 								
 								mod.queueChunk(chunk);
 							}
 							
 						} catch (Exception e) {
 							ModLoader.outputError(mod.getName() + " failed (" + e.getClass().getSimpleName() + ") to load chunk " + regionChunk.chunkX + "," + regionChunk.chunkZ + " for world " + regionChunk.worldIndex + ": " + e.getMessage());
 						}
 					}
 				}
 				else {
 					RegionFile regionFile = RegionFileCache.createOrLoadRegionFile(region.location, region.regionX << 5, region.regionZ << 5);
 					for (int x = 0; x < 32; x++) {
 						for (int z = 0; z < 32; z++) {
 							if (regionFile.isChunkSaved(x, z)) {
 								regionQueue.add(new QueuedRegionChunk(region, region.regionX << 5 | x, region.regionZ << 5 | z));
 							}
							else if (mod.debugMessages) {
								ModLoader.outputInfo(mod.getName() + " skipped empty chunk " + x + "," + z + " for region " + region.regionX + "." + region.regionZ);
 							}
 						}
 					}
 				}
 			}
 		}
 	}
 
 	@Override
 	public IMod getMod() {
 		return mod;
 	}
 }
