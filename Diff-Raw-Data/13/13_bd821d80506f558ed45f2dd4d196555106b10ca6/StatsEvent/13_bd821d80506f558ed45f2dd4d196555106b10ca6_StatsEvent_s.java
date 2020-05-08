 package btwmods.server.events;
 import java.util.EventObject;
 
 import btwmods.ServerAPI.StatsProcessor;
 import net.minecraft.server.MinecraftServer;
 
 public class StatsEvent extends EventObject {
 	
 	private StatsProcessor processor;
 	private long[] tickTimeArray = null;
 	private long[] sentPacketCountArray = null;
 	private long[] sentPacketSizeArray = null;
 	private long[] receivedPacketCountArray = null;
 	private long[] receivedPacketSizeArray = null;
 	private long[][] worldTickTimeArray = null;
 	
 	public final int tickCounter;
 	public final double averageTickTime;
 	public final double averageSentPacketCount;
 	public final double averageSentPacketSize;
 	public final double averageReceivedPacketCount;
 	public final double averageReceivedPacketSize;
 
 	public long[] getLast100TickTimes() {
 		if (tickTimeArray == null)
 			tickTimeArray = processor.tickTimeArray.clone();
 			
 		return tickTimeArray;
 	}
 	
 	public long[] getLast100SentPacketCounts() {
 		if (sentPacketCountArray == null)
 			sentPacketCountArray = processor.sentPacketCountArray.clone();
 			
 		return sentPacketCountArray;
 	}
 	
 	public long[] getLast100SentPacketSizes() {
 		if (sentPacketSizeArray == null)
 			sentPacketSizeArray = processor.sentPacketSizeArray.clone();
 			
 		return sentPacketSizeArray;
 	}
 	
 	public long[] getLast100ReceivedPacketCounts() {
 		if (receivedPacketCountArray == null)
 			receivedPacketCountArray = processor.receivedPacketCountArray.clone();
 			
 		return receivedPacketCountArray;
 	}
 	
 	public long[] getLast100ReceivedPacketSizes() {
 		if (receivedPacketSizeArray == null)
 			receivedPacketSizeArray = processor.receivedPacketSizeArray.clone();
 			
 		return receivedPacketSizeArray;
 	}
 	
 	
 	public long[][] getLast100WorldTickTimes() {
 		if (worldTickTimeArray == null) {
 			worldTickTimeArray = new long[processor.worldTickTimeArray.length][];
 			for (int i = 0; i < worldTickTimeArray.length; i++) {
 				worldTickTimeArray[i] = processor.worldTickTimeArray[i].clone();
 			}
 		}
 		return worldTickTimeArray;
 	}
 	
 	public StatsEvent(MinecraftServer server, StatsProcessor processor) {
 		super(server);
 		this.processor = processor;
 		
 		tickCounter = processor.tickCounter;
 		averageTickTime = (double)processor.tickTimeTotal / (double)Math.min(100, tickCounter);
 		averageSentPacketCount = (double)processor.sentPacketCountTotal / (double)Math.min(100, tickCounter);
 		averageSentPacketSize = (double)processor.sentPacketSizeTotal / (double)Math.min(100, tickCounter);
 		averageReceivedPacketCount = (double)processor.receivedPacketCountTotal / (double)Math.min(100, tickCounter);
 		averageReceivedPacketSize = (double)processor.receivedPacketSizeTotal / (double)Math.min(100, tickCounter);
 	}
 }
