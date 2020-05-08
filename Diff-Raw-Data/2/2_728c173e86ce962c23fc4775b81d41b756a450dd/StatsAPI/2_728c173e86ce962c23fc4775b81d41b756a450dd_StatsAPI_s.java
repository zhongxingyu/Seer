 package btwmods;
 
 import java.util.ArrayDeque;
 import java.util.concurrent.ConcurrentLinkedQueue;
 
 import net.minecraft.server.MinecraftServer;
 import net.minecraft.src.ChunkCoordIntPair;
 import net.minecraft.src.NextTickListEntry;
 import net.minecraft.src.World;
 import btwmods.events.EventDispatcher;
 import btwmods.events.EventDispatcherFactory;
 import btwmods.events.IAPIListener;
 import btwmods.measure.Average;
 import btwmods.measure.Measurements;
 import btwmods.server.IStatsListener;
 import btwmods.server.StatsEvent;
 import btwmods.server.Tick;
 
 public class StatsAPI {
 
 	private static Measurements measurements = new Measurements<Tick>();
 	
 	private static volatile StatsProcessor statsProcessor = null;
 	
 	private static EventDispatcher listeners = EventDispatcherFactory.create(new Class[] { IStatsListener.class });
 	private static ConcurrentLinkedQueue<QueuedTickStats> statsQueue = new ConcurrentLinkedQueue<QueuedTickStats>();
 	
 	private StatsAPI() {}
 
 	public static void addListener(IAPIListener listener) {
 		if (listener instanceof IStatsListener) {
 			listeners.queuedAddListener(listener, IStatsListener.class);
 
 			if (statsProcessor == null) {
 				Thread thread = new Thread(statsProcessor = new StatsProcessor());
				thread.setName("ServerAPI StatsListeners");
 				thread.start();
 			}
 		}
 		else {
 			listeners.addListener(listener);
 		}
 	}
 
 	public static void removeListener(IAPIListener listener) {
 		if (listener instanceof IStatsListener) {
 			listeners.queuedRemoveListener(listener, IStatsListener.class);
 		}
 		else {
 			listeners.removeListener(listener);
 		}
 	}
 
 	public static void startTick(MinecraftServer server, int tickCounter) {
 		// Process any failures that may be queued from the last tick.
 		ModLoader.processFailureQueue();
 	}
 
 	public static void endTick(MinecraftServer server, int tickCounter) {
 		QueuedTickStats stats = new QueuedTickStats();
 		
 		stats.tickCounter = tickCounter;
 		stats.tickTime = server.tickTimeArray[tickCounter % 100];
 		stats.sentPacketCount = server.sentPacketCountArray[tickCounter % 100];
 		stats.sentPacketSize = server.sentPacketSizeArray[tickCounter % 100];
 		stats.receivedPacketCount = server.receivedPacketCountArray[tickCounter % 100];
 		stats.receivedPacketSize = server.receivedPacketSizeArray[tickCounter % 100];
 		
 		stats.worldTickTimes = new long[server.timeOfLastDimensionTick.length];
 		for (int i = 0; i < stats.worldTickTimes.length; i++) {
 			stats.worldTickTimes[i] = server.timeOfLastDimensionTick[i][tickCounter % 100];
 		}
 		
 		// Save measurements and clear it for the next round.
 		stats.measurements = measurements.startNew();
 		
 		statsQueue.add(stats);
 	}
 	
 	/**
 	 * Begin a measurement.
 	 */
 	public static void begin(Tick.Type type) {
 		measurements.begin(new Tick(type));
 	}
 	
 	/**
 	 * Begin a measurement for a specific world.
 	 */
 	public static void begin(Tick.Type type, World world) {
 		measurements.begin(new Tick(type, world));
 	}
 
 	/**
 	 * Begin a measurement for a specific entity tick in a world world.
 	 */
 	public static void begin(Tick.Type type, World world, NextTickListEntry entityTick) {
 		measurements.begin(new Tick(type, world, entityTick));
 	}
 	
 	/**
 	 * End a measurement.
 	 */
 	public static void end() {
 		measurements.end();
 	}
 	
 	private static class QueuedTickStats {
 		public int tickCounter;
 		public long tickTime;
 		public long sentPacketCount;
 		public long sentPacketSize;
 		public long receivedPacketCount;
 		public long receivedPacketSize;
 		public long[] worldTickTimes;
 		ArrayDeque<Tick> measurements;
 	}
 
 	public static class StatsProcessor implements Runnable {
 		
 		public int tickCounter;
 		private final ServerStats serverStats = new ServerStats();
 		private final WorldStats[] worldStats;
 		
 		public static class ServerStats {
 			public final Average tickTime = new Average();
 			public final Average sentPacketCount = new Average();
 			public final Average sentPacketSize = new Average();
 			public final Average receivedPacketCount = new Average();
 			public final Average receivedPacketSize = new Average();
 		}
 		
 		public static class WorldStats {
 			public final Average worldTickTime = new Average();
 			public final Average mobSpawning = new Average();
 			public final Average blockTick = new Average();
 			public final Average tickBlocksAndAmbiance = new Average();
 			public final Average tickBlocksAndAmbianceSuper = new Average();
 			public final Average entities = new Average();
 			public final Average timeSync = new Average();
 			public final Average buildActiveChunkSet = new Average();
 			public final Average checkPlayerLight = new Average();
 		}
 		
 		public StatsProcessor() {
 			// Initialize per-world stats.
 			worldStats = new WorldStats[MinecraftServer.getServer().worldServers.length];
 			for (int i = 0; i < worldStats.length; i++) {
 				worldStats[i] = new WorldStats();
 			}
 		}
 
 		@Override
 		public void run() {
 			while (statsProcessor == this) {
 				
 				// Stop if the thread if there are no listeners.
 				if (listeners.isEmpty(IStatsListener.class)) {
 					statsProcessor = null;
 				}
 				else {
 					
 					// Process all the queued tick stats.
 					QueuedTickStats stats;
 					while ((stats = statsQueue.poll()) != null) {
 						tickCounter = stats.tickCounter;
 						
 						serverStats.tickTime.record(stats.tickTime);
 						serverStats.sentPacketCount.record(stats.sentPacketCount);
 						serverStats.sentPacketSize.record(stats.sentPacketSize);
 						serverStats.receivedPacketCount.record(stats.receivedPacketCount);
 						serverStats.receivedPacketSize.record(stats.receivedPacketSize);
 						
 						for (int i = 0; i < worldStats.length; i++) {
 							worldStats[i].worldTickTime.record(stats.worldTickTimes[i]);
 
 							// Reset the measurement entries to 0
 							worldStats[i].mobSpawning.resetCurrent();
 							worldStats[i].blockTick.resetCurrent();
 							worldStats[i].tickBlocksAndAmbiance.resetCurrent();
 							worldStats[i].tickBlocksAndAmbianceSuper.resetCurrent();
 							worldStats[i].entities.resetCurrent();
 							worldStats[i].timeSync.resetCurrent();
 							worldStats[i].buildActiveChunkSet.resetCurrent();
 							worldStats[i].checkPlayerLight.resetCurrent();
 						}
 						
 						// Add the time taken by each measurement type.
 						Tick tick;
 						while ((tick = stats.measurements.poll()) != null) {
 							//ChunkCoordIntPair.chunkXZ2Int(par0, par1);
 							
 							switch (tick.identifier) {
 								
 								case mobSpawning:
 									worldStats[tick.worldIndex].mobSpawning.incrementCurrent(tick.getTime());
 									break;
 									
 								case tickBlocksAndAmbiance:
 									worldStats[tick.worldIndex].tickBlocksAndAmbiance.incrementCurrent(tick.getTime());
 									break;
 									
 								case tickBlocksAndAmbianceSuper:
 									worldStats[tick.worldIndex].tickBlocksAndAmbianceSuper.incrementCurrent(tick.getTime());
 									break;
 									
 								case blockTick:
 									worldStats[tick.worldIndex].blockTick.incrementCurrent(tick.getTime());
 									break;
 									
 								case entities:
 									worldStats[tick.worldIndex].entities.incrementCurrent(tick.getTime());
 									break;
 									
 								case timeSync:
 									worldStats[tick.worldIndex].timeSync.incrementCurrent(tick.getTime());
 									break;
 									
 								case buildActiveChunkSet:
 									worldStats[tick.worldIndex].buildActiveChunkSet.incrementCurrent(tick.getTime());
 									break;
 									
 								case checkPlayerLight:
 									worldStats[tick.worldIndex].checkPlayerLight.incrementCurrent(tick.getTime());
 									break;
 							}
 						}
 					}
 					
 					// Run all the listeners.
 					StatsEvent event = new StatsEvent(MinecraftServer.getServer(), tickCounter, serverStats, worldStats);
 					((IStatsListener)listeners).statsAction(event);
 
 					try {
 						Thread.sleep(50L);
 					} catch (InterruptedException e) {
 						
 					}
 				}
 			}
 		}
 	}
 }
