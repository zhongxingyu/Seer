 package btwmods.stats;
 
 import java.util.Map;
 import java.util.concurrent.ConcurrentLinkedQueue;
 
 import net.minecraft.server.MinecraftServer;
 import net.minecraft.src.ChunkCoordIntPair;
 import btwmods.ModLoader;
 import btwmods.Stat;
 import btwmods.StatsAPI;
 import btwmods.events.EventDispatcher;
 import btwmods.measure.Average;
 import btwmods.measure.Measurement;
 import btwmods.network.NetworkType;
 import btwmods.stats.data.QueuedTickStats;
 import btwmods.stats.data.ServerStats;
 import btwmods.stats.data.WorldStats;
 import btwmods.stats.measurements.StatChunk;
 import btwmods.stats.measurements.StatPositionedClass;
 import btwmods.stats.measurements.StatNetwork;
 import btwmods.stats.measurements.StatNetworkPlayer;
 import btwmods.stats.measurements.StatWorld;
 import btwmods.stats.measurements.StatWorldValue;
 
 public class StatsProcessor implements Runnable {
 	
 	public static volatile StatsProcessor statsProcessor = null;
 	
 	public static boolean isRunning() {
 		return statsProcessor != null;
 	}
 	
 	public static StatsProcessor setStatsProcessor(StatsProcessor statsProcessor) {
 		StatsProcessor.statsProcessor = statsProcessor;
 		return statsProcessor;
 	}
 	
 	private ConcurrentLinkedQueue<QueuedTickStats> statsQueue;
 	private EventDispatcher listeners;
 	private boolean doingDetailedStats = StatsAPI.detailedMeasurementsEnabled;
 	private int tickCounter = 0;
 	private ServerStats serverStats = new ServerStats();
 	private WorldStats[] worldStats = null;
 	
 	public StatsProcessor(EventDispatcher listeners, ConcurrentLinkedQueue<QueuedTickStats> statsQueue) {
 		this.listeners = listeners;
 		this.statsQueue = statsQueue;
 	}
 	
 	@Override
 	public void run() {
 		try {
 			
 			while (statsProcessor == this) {
 				
 				// Stop if the thread if there are no listeners.
 				if (listeners.isEmpty(IStatsListener.class)) {
 					statsProcessor = null;
 				}
 				else {
 					
 					long threadStart = System.nanoTime();
 					
 					// Reset the detailed stats if the detailed measurements setting has changed.
 					if (doingDetailedStats != StatsAPI.detailedMeasurementsEnabled || worldStats == null) {
 						doingDetailedStats = StatsAPI.detailedMeasurementsEnabled;
 						
 						resetStats();
 					}
 					
 					// Process all the queued tick stats.
 					long polled = processQueue();
 					
 					serverStats.statsThreadTime.record(System.nanoTime() - threadStart);
 					serverStats.statsThreadQueueCount.record(polled);
 					
 					// Make sure the thread should still should be running.
 					if (statsProcessor == this) {
 						
 						// Run all the listeners.
 						StatsEvent event = new StatsEvent(MinecraftServer.getServer(), tickCounter, serverStats, worldStats);
 						((IStatsListener)listeners).onStats(event);
 
 						try {
 							Thread.sleep(40L);
 						} catch (InterruptedException e) {
 							
 						}
 					}
 				}
 			}
 		}
 		catch (Throwable e) {
 			ModLoader.outputError(e, "StatsAPI thread failed (" + e.getClass().getSimpleName() + "): " + e.getMessage());
 			
 			if (statsProcessor == this)
 				statsProcessor = null;
 		}
 	}
 	
 	private void resetStats() {
 		serverStats = new ServerStats();
 		
 		// Initialize per-world stats.
 		worldStats = new WorldStats[MinecraftServer.getServer().worldServers.length];
 		for (int i = 0; i < worldStats.length; i++) {
 			worldStats[i] = new WorldStats();
 		}
 	}
 	
 	/**
 	 * Process all the queued tick stats.
 	 * 
 	 * @return the number of queued tick stats that were processed.
 	 */
 	private long processQueue() {
 		long polled = 0;
 		QueuedTickStats stats;
 		while ((stats = statsQueue.poll()) != null) {
 			polled++;
 			tickCounter = stats.tickCounter;
 			
 			serverStats.lastTickEnd = Math.max(serverStats.lastTickEnd, stats.tickEnd);
 			serverStats.tickTime.record(stats.tickTime);
 			serverStats.players = stats.players;
 			serverStats.sentPacketCount.record(stats.sentPacketCount);
 			serverStats.sentPacketSize.record(stats.sentPacketSize);
 			serverStats.receivedPacketCount.record(stats.receivedPacketCount);
 			serverStats.receivedPacketSize.record(stats.receivedPacketSize);
 			
 			serverStats.bytesSent = stats.bytesSent;
 			serverStats.bytesReceived = stats.bytesReceived;
 			
 			serverStats.handlerInovcations = stats.handlerInvocations;
 
 			// Reset the current value of averages that are calculated from Measurements.
 			for (int i = 0; i < worldStats.length; i++) {
 				worldStats[i].resetCurrent();
 			}
 			
 			// Add the time taken by each measurement type.
 			Measurement measurement;
 			while ((measurement = stats.measurements.poll()) != null) {
 				if (measurement instanceof StatNetwork) {
 					StatNetwork statNetwork = (StatNetwork)measurement;
 					
 					if (statNetwork.identifier == NetworkType.RECEIVED)
 						serverStats.bytesReceivedFromPlayers += (long)statNetwork.size;
 					else
 						serverStats.bytesSentToPlayers += (long)statNetwork.size;
 					
 					if (measurement instanceof StatNetworkPlayer) {
 						// TODO: record player network usage.
 					}
 				}
 				
 				else if (measurement instanceof StatWorldValue) {
 					StatWorldValue statWorldValue = (StatWorldValue)measurement;
 					worldStats[statWorldValue.worldIndex].measurements.add(measurement);
 					worldStats[statWorldValue.worldIndex].measurementQueue.incrementCurrent(1);
 					worldStats[statWorldValue.worldIndex].averages.get(statWorldValue.identifier).incrementCurrent(statWorldValue.value);
 				}
 				
 				else if (measurement instanceof StatWorld) {
 					StatWorld statWorld = (StatWorld)measurement;
 				
 					worldStats[statWorld.worldIndex].measurements.add(measurement);
 					worldStats[statWorld.worldIndex].measurementQueue.incrementCurrent(1);
 					
 					if (statWorld.identifier == Stat.LOAD_CHUNK) {
 						worldStats[statWorld.worldIndex].averages.get(Stat.LOAD_CHUNK).incrementCurrent(1L);
 						worldStats[statWorld.worldIndex].averages.get(Stat.LOAD_CHUNK_TIME).incrementCurrent(statWorld.getTime());
 					}
 					else {
 						worldStats[statWorld.worldIndex].averages.get(statWorld.identifier).incrementCurrent(statWorld.getTime());
 						
 						if (statWorld instanceof StatChunk) {
 							StatChunk statChunk = (StatChunk)statWorld;
 							
 							// Increment the time for the chunk.
 							ChunkCoordIntPair coords = new ChunkCoordIntPair(statChunk.chunkX, statChunk.chunkZ);
 							Average chunkAverage = worldStats[statWorld.worldIndex].timeByChunk.get(coords);
							if (chunkAverage == null) {
 								worldStats[statWorld.worldIndex].timeByChunk.put(coords, chunkAverage = new Average());
								chunkAverage.resetCurrent();
							}
 							chunkAverage.incrementCurrent(statChunk.getTime());
 							
 							if (statWorld instanceof StatPositionedClass) {
 								StatPositionedClass statPositionedClass = (StatPositionedClass)statChunk;
 								
 								// Increment the time for the class.
 								Map<Class, Average> classAverages = worldStats[statWorld.worldIndex].timeByClass.get(statPositionedClass.identifier);
 								Average classAverage = classAverages.get(statPositionedClass.clazz);
								if (classAverage == null) {
 									classAverages.put(statPositionedClass.clazz, classAverage = new Average());
									classAverage.resetCurrent();
								}
 								classAverage.incrementCurrent(statPositionedClass.getTime());
 							}
 						}
 					}
 				}
 			}
 			
 			// Clean up the measurements just in case.
 			stats.measurements.clear();
 		}
 		
 		return polled;
 	}
 }
