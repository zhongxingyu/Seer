 package btwmods;
 
 import java.util.HashSet;
 import java.util.concurrent.ConcurrentLinkedQueue;
 
 import net.minecraft.server.MinecraftServer;
 import btwmods.server.events.StatsEvent;
 import btwmods.server.listeners.IStatsListener;
 
 public class ServerAPI {
 	
 	private static volatile StatsProcessor statsProcessor = null;
 	private static ConcurrentLinkedQueue<QueuedTickStats> statsQueue = new ConcurrentLinkedQueue<ServerAPI.QueuedTickStats>();
 	
 	private static HashSet<IStatsListener> statsListeners = new HashSet<IStatsListener>();
 	
 	private ServerAPI() {}
 
 	public static void addListener(IAPIListener listener) {
 		if (listener instanceof IStatsListener) {
 			synchronized (statsListeners) {
 				statsListeners.add((IStatsListener) listener);
 				
 				if (statsProcessor == null) {
 					Thread thread = new Thread(statsProcessor = new StatsProcessor());
 					thread.setName("ServerAPI StatsListeners");
 					thread.start();
 				}
 			}
 		}
 	}
 
 	public static void removeListener(IAPIListener listener) {
 		if (listener instanceof IStatsListener) {
 			synchronized (statsListeners) {
 				statsListeners.remove((IStatsListener)listener);
 			}
 		}
 	}
 
 	public static void startTick(MinecraftServer server, int tickCounter) {
 		
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
 		
 		statsQueue.add(stats);
 	}
 	
 	private static class QueuedTickStats {
 		public int tickCounter;
 		public long tickTime;
 		public long sentPacketCount;
 		public long sentPacketSize;
 		public long receivedPacketCount;
 		public long receivedPacketSize;
 		public long[] worldTickTimes;
 	}
 
 	public static class StatsProcessor implements Runnable {
 
 		public int tickCounter;
 		
 		public long[] tickTimeArray = new long[100];
 		public long[] sentPacketCountArray = new long[100];
 		public long[] sentPacketSizeArray = new long[100];
 		public long[] receivedPacketCountArray = new long[100];
 		public long[] receivedPacketSizeArray = new long[100];
 		public long[][] worldTickTimeArray;
 
 		public long tickTimeTotal;
 		public long sentPacketCountTotal;
 		public long sentPacketSizeTotal;
 		public long receivedPacketCountTotal;
 		public long receivedPacketSizeTotal;
 		public long[] worldTickTimeTotals;
 		
 		public StatsProcessor() {
 			worldTickTimeTotals = new long[MinecraftServer.getServer().worldServers.length];
 			worldTickTimeArray = new long[worldTickTimeTotals.length][100];
 		}
 
 		@Override
 		public void run() {
 			synchronized (statsListeners) {
 				while (!statsListeners.isEmpty()) {
 					
 					// Process all the queued tick stats.
 					QueuedTickStats stats;
 					while ((stats = statsQueue.poll()) != null) {
 						tickCounter = stats.tickCounter;
 						
 						// Remove the stats we are replacing from the totals, if we have looped through the last 100 arrays at least once.
 						if (tickCounter >= 100) {
 							tickTimeTotal -= tickTimeArray[tickCounter % 100];
 							sentPacketCountTotal -= sentPacketCountArray[tickCounter % 100];
 							sentPacketSizeTotal -= sentPacketSizeArray[tickCounter % 100];
 							receivedPacketCountTotal -= receivedPacketCountArray[tickCounter % 100];
 							receivedPacketSizeTotal -= receivedPacketSizeArray[tickCounter % 100];
 							for (int i = 0; i < worldTickTimeTotals.length; i++) {
 								worldTickTimeTotals[i] -= worldTickTimeArray[i][tickCounter % 100];
 							}
 						}
 						
 						// Increment the total with the new stats, and also add them to the last 100 arrays.
 						tickTimeTotal += tickTimeArray[tickCounter % 100] = stats.tickTime;
 						sentPacketCountTotal += sentPacketCountArray[tickCounter % 100] = stats.sentPacketCount;
 						sentPacketSizeTotal += sentPacketSizeArray[tickCounter % 100] = stats.sentPacketSize;
 						receivedPacketCountTotal += receivedPacketCountArray[tickCounter % 100] = stats.receivedPacketCount;
 						receivedPacketSizeTotal += receivedPacketSizeArray[tickCounter % 100] = stats.receivedPacketSize;
 						for (int i = 0; i < worldTickTimeTotals.length; i++) {
 							worldTickTimeTotals[i] += stats.worldTickTimes[i];
 						}
 					}
 					
 					// Run all the listeners.
 					StatsEvent event = new StatsEvent(MinecraftServer.getServer(), this);
 					for (IStatsListener listener : statsListeners)
 						try {
 							listener.statsAction(event);
 						} catch (Throwable t) {
 							ModLoader.reportListenerFailure(t, listener);
 						}
				}
 
				try {
					Thread.sleep(50L);
				} catch (InterruptedException e) {
					
 				}
 				
 				statsProcessor = null;
 			}
 		}
 	}
 }
