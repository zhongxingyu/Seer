 package com.simulator.ccn;
 
 import arjuna.JavaSim.Simulation.*;
 import arjuna.JavaSim.Distributions.*;
 
 import java.io.BufferedWriter;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.Writer;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.log4j.Logger;
 
 import com.simulator.controller.SimulationController;
 
 import com.simulator.distributions.PacketDistributions;
 import com.simulator.enums.PacketsType;
 import com.simulator.enums.SimulationTypes;
 import com.simulator.enums.SupressionTypes;
 import com.simulator.packets.Packets;
 import com.simulator.topology.Grid;
 import com.simulator.trace.ReadTraceFile;
 
 import arjuna.JavaSim.Simulation.SimulationException;
 
 /* This class is a SimulationProcess, hence, it will get scheduled in JavaSim Scheduler. The main function is that when it is 
  * scheduled to be picked, then the CCNRouter object selected will process a packet from its queue. 
  * */
 public class CCNRouter extends SimulationProcess {
 	
 	static final Logger log = Logger.getLogger(CCNRouter.class);
 
 	//private Writer fs1 = null;
 	private ExponentialStream STime;
 	private boolean working;
 	private Packets currentPacket;
 	private CCNQueue packetsQ;
 	private static double procDelay;
 	private static double pitTimeOut;
 	/**
 	 * Pending Interest Table is a map of Interest Id and as list of Integers representing routerId's of interested nodes.
 	 */
 	Map<InterestEntry, List<PITEntry>> pit = null;
 	/**
 	 * forwarding table is a map of <DataPacketId,NodeId>
 	 */
 	Map<Integer,FIBEntry> forwardingTable = null;
 	/**
 	 * Routers Unique Id
 	 */
 	private int routerId = 0;
 	/**
 	 * Routers localCache. Its own Cache
 	 */
 	private CCNCache localCache = null;
 	/**
 	 * Cache to store the data objects it received from other routers.
 	 */
 	private CCNCache globalCache = null;
 	
 	/**
 	 * Counter to distinguish various log entries of this router.
 	 */
 	private int logCounter = 0;
 	
 	/**
 	 * List of interest packets served. Before serving an Interest packet we check with this list first to make sure 
 	 * we haven't served it already.
 	 * TODO Implementing this list in a very naive way for now, need to think of more efficient way of handling this
 	 */
 	
 	private List<InterestEntry> interestsServed = null;
 	
 	public CCNRouter (int id) {
 		
 		STime = new ExponentialStream(8);
 		working = false;
 		currentPacket = null;
 		packetsQ = new CCNQueue(id);
 		pit = new HashMap<InterestEntry,List<PITEntry>>();
 		forwardingTable = new HashMap<Integer, FIBEntry>(200);
 		interestsServed = new ArrayList<InterestEntry>();
 		setRouterId(id);
 		
 		/* The localCache will always be of unlimited size */
 		localCache = new CCNCache(id, 100, true);
 		
 		/* The globalCache will be based on the value passed in "ccn.properties" file. The size will be effective in case the cache
 		 * is of a limited size */
 		globalCache = new CCNCache(id, SimulationController.getCacheSize(), SimulationController.cacheType());	
 	}
 	
 	/* The CCNRouter is a SimulationProcess, and is scheduled by JavaSim Scheduler. The following method is called when 
 	 * the JavaSim scheduler selects a CCNRouter object from its queue. The CCNRouter removes a packet from its queue, 
 	 * and processes it accordingly. 
 	 * */
 	public void run () {
 		
 		for (;;) {
 			
 			working = true;
 
 			while (!packetsQ.isEmpty())	{
 				
 				CurrentTime();
 
 				int ctr= getLogCounter();
 				
 				log.info("Start Processing of Router:"+getRouterId()+" Iteration:"+ctr+"\n");
 				log.info(toString());
 				
 				currentPacket = (Packets) packetsQ.remove();
 				log.info("Processing  packet "+currentPacket.toString());
 				
 				/* Records the time at which the packet was removed from the queue */
 				Packets.dumpStatistics(currentPacket, "DEQUEUE");
 				
 				/* 
 				 * "Hold(getProcDelay())" represents the processing delay for a packet before the result of that
 				 *  processing is reflected in the methods "interestpackethandler()" and "datapackethandler()"
 				 */
 				
 				try	{
 					Hold(getProcDelay());
 				} 
 				catch (SimulationException e) {}
 				catch (RestartException e) {}
 				
 				/* Records the time at which the packet has been processed */
 				Packets.dumpStatistics(currentPacket, "PROCSED");	
 				
 				/* The packet will be processed according to its packet type */
 				if(currentPacket.getPacketType() == PacketsType.PACKET_TYPE_INTEREST)
 					interestPacketsHandler(currentPacket);
				else if (currentPacket.getPacketType() == PacketsType.PACKET_TYPE_DATA)
 					dataPacketsHandler(currentPacket);
 				
 				log.info(toString());
 				log.info("Ending Processing of Router:"+getRouterId()+" Iteration:"+ctr+"#########################################################\n");
 				
 				CurrentTime();
 			}
 
 			working = false;
 
 			/* In case the CCNRouter objects queue is empty, then we suspend the CCNRouter using "Cancel()". When it will have 
 			 * packets in its queue, it will be activated and placed in the scheduler again. 
 			 * */
 			try	{
 				Cancel();
 			}
 			catch (RestartException e) {}
 		}
 	}
 	/**
 	 * This is the handler  data packets in a router. It first checks if there is an entry in Pit table
 	 * if it doesn't find any it just ignores the data packet. else it runs through the the linked list
 	 * returned by Pit table and sends the data packet to those routers. If the router id is my own I just ignore it.
 	 * After that it deletes the pit entry.
 	 * @param curPackets the data packet
 	 */
 	public void dataPacketsHandler(Packets curPacket) {
 		
 		/* The following code was used for debugging */
 		/*
 		if (getRouterId() == 2) {
 			
 			try {
 				
 				Writer fs1 = new BufferedWriter(new FileWriter("dump/debugging.txt",true));
 				fs1.write("Start of Data Handler function \nTime: " + CCNRouter.CurrentTime() + "\n");
 				fs1.write("Processing Interest packet #: " + curPacket.getRefPacketId() + "\n");
 				fs1.write("Processing Data packet #: " + curPacket.getPacketId() + "\n");
 				fs1.write("Forwarding Table Entry @ Node 2 for ObjectID " + curPacket.getPacketId() + " is: " + getForwardingTableEntry(curPacket.getPacketId()) + "\n" + "\n");
 				fs1.write("Is it in the Global Cache or not: " + getGlobalCache().isPresentInCache(curPacket) + "\n");
 				fs1.write("Is it in the Local Cache or not: " + getLocalCache().isPresentInCache(curPacket) + "\n");
 				fs1.close();
 			}
 			catch (IOException e){}		
 		}
 		*/
 		
 		InterestEntry interest = new InterestEntry (curPacket.getRefPacketId(), curPacket.getSegmentId());
 		
 		/* I got this data packet so setting its locality to false */
 		curPacket.setLocality(false);
 		log.info("In Data packet handler");
 		List<PITEntry> pitEntry = pit.get(interest); 
 		
 		/* I havn't seen this packet so discard it */
 		if(pitEntry == null) {
 			
 			log.info("No entry in pit table ignoring");
 			curPacket.finished(SupressionTypes.SUPRESSION_NO_PIT);
 			return;
 		}
 		
 		/* If the current node is not present in the PIT table list dump the packet as live packet and set the hop counts 
 		 * to zero.
 		 * */
 		if(!(pitEntry.contains(-1))) {
 			//Packets.dumpStatistics(curPacket);
 			//curPacket.setNoOfHops(0);
 		}
 		
 		/* Check if there are PIT entries that need to be expelled because the interest packets do not need to be sent there anymore.
 		 * To reduce processing time, we will only check PIT entries associated with the current objectID in question at this node.
 		 * This way, we will not have to traverse over the entire HashMap to update all the PIT Entries corresponding to the PIT table of this node
 		 * */
 		
 		Iterator<PITEntry> stalledPITEntries = pitEntry.iterator();		
 		while(stalledPITEntries.hasNext()) {
 			
 			PITEntry rid = stalledPITEntries.next();
 			
 			try {
 				
 				/* Used for debugging purposes 
 				 *	Writer fs1 = new BufferedWriter(new FileWriter("dump/PITExpiration.txt",true));
 					fs1.write("AT Router: " + this.getRouterId() + "\n");
 					fs1.write("Start of Data Handler function \nTime: " + CCNRouter.CurrentTime() + "\n");
 					fs1.write("Processing Interest packet #: " + curPacket.getRefPacketId() + "\n");
 					fs1.write("Processing Data packet #: " + curPacket.getPacketId() + "\n");
 					fs1.write("PIT Entry: Outgoing Interface is " + rid.getoutgoingInterface() + "\n");
 					fs1.write("PIT Entry: Entry was created at " + rid.getCreatedAtTime() + "\n");	
 				 * */
 				
 				
 				
 				/* We are traversing and removing the expired entries from PIT table. The pitTimeOut is a parameterized value taken from
 				 * "ccn.properties" file
 				 * */
 				if ((SimulationProcess.CurrentTime() - rid.getCreatedAtTime()) >= pitTimeOut) {
 					stalledPITEntries.remove();
 					/* Used for debugging purposes 
 					 *	fs1.write("\nEntry Removed at " + SimulationProcess.CurrentTime() + "\n"); 
 					 */										
 				}
 				
 				/* Used for debugging purposes
 				 *  fs1.write("\n\n\n");				
 					fs1.close();
 				 */				
 			}				
 			catch (Exception e){}
 		}
 		
 		/* The following code is used to flood data packets over all the interfaces in PIT entry for this object */
 		Iterator<PITEntry> itr = pitEntry.iterator();		
 		while(itr.hasNext()) {
 			
 			PITEntry rid = itr.next();
 			
 			/* We shouldn't flood the interest packet to same node where it came from */
 			if(rid.getoutgoingInterface() != getRouterId()) {
 				
 				Packets clonePac = (Packets) curPacket.clone();
 				sendPacket(clonePac, rid.getoutgoingInterface());
 			}
 		}		
 		
 		/* Now remove the entry */ 
 		pit.remove(interest);			
 		
 		/* The following swapping before entering into the Global cache is because of a more logical entry relevant to "(PCKSTATUS:CRTDPRD)" 
 		 * in the trace file 
 		 * */
 		int tempCurr = curPacket.getCurNode(); 
 		int tempPreHop = curPacket.getPrevHop();
 		
 		curPacket.setCurNode(-1); 
 		curPacket.setPrevHop(-1);
 		
 		/* add the data packet into my global cache */
 		log.info("Adding to global cache");	
 		
 		/* Cache is a Map <Integer, Packet> object, hence, we do not have to check for duplicate values */
 		getGlobalCache().addToCache((Packets)curPacket.clone());		
 		
 		curPacket.setCurNode(tempCurr); 
 		curPacket.setPrevHop(tempPreHop);
 		
 		/* adding entry to forwarding table */
 		log.info("Adding a entry on forwarding table");
 		
 		/* 1. We check whether the FIBEntry is null or not
 		 * 2. If the FIB entry is not null, then we check whether the current FIB entry is already the shortest path, in which case, we will 
 		 * not update the FIB entry
 		 * 3. However, if FIB entry is null, then we will add the new FIB entry
 		 * */
 		if (forwardingTable.get(curPacket.getPacketId()) != null) {
 			
 			try {
 				
 				/* Used for debugging purposes
 				*	Writer fs1 = new BufferedWriter(new FileWriter("dump/FIBEntryHopcountVerfication.txt",true));
 					fs1.write("AT Router: " + this.getRouterId() + "\n");
 					fs1.write("Current Time: " + CCNRouter.CurrentTime() + "\n");
 					fs1.write("Processing Interest packet #: " + curPacket.getRefPacketId() + "\n");
 					fs1.write("Processing Data packet #: " + curPacket.getPacketId() + "\n");
 					
 					fs1.write("Current number of hops in FIB: " + getForwardingTableEntry(curPacket.getPacketId()).getHops() + "\n");
 					fs1.write("Current number of hops in Packet: " + curPacket.getNoOfHops() + "\n");
 				 */
 				
 				
 				/* The following code will print to the file whenever a FIB entry is changed because of hop count */
 				if (getForwardingTableEntry(curPacket.getPacketId()).getHops() > curPacket.getNoOfHops()) {
 					
 					/* The following four lines of code are part of the core code performing the entry into the FIB in case there is a change of 
 					 * hop count 
 					 * */
 					FIBEntry temp = getForwardingTableEntry(curPacket.getPacketId());
 					temp.setDestinationNode(curPacket.getPrevHop());
 					temp.setHops(curPacket.getNoOfHops());
 					
 					getForwardingTable().put(curPacket.getPacketId(), temp);
 					
 					/* Used for debugging purposes
 					 * 	fs1.write("FIB updated: " + curPacket.getNoOfHops() + "\n");
 						fs1.write("New number of hops in FIB: " + getForwardingTableEntry(curPacket.getPacketId()).getHops() + "\n");
 					 */
 										
 				}
 				
 				/* Used for debugging purposes
 				 * 	fs1.write("\n\n");
 					fs1.close();
 				 */
 				
 			}
 			catch (Exception e) {}
 		}
 		
 		/* If there is no FIB entry for the object, then we create a new FIB entry for it */
 		else 
 			getForwardingTable().put(curPacket.getPacketId(), new FIBEntry (curPacket.getPrevHop(), curPacket.getNoOfHops()));			
 				
 		/* The following code was used for debugging */
 		/*
 		if (this.getRouterId() == 2) {
 			try {
 				
 				System.out.println("Inside");
 				Writer fs1 = new BufferedWriter(new FileWriter("dump/debugging.txt",true));
 				fs1.write("End of Data Hander function \nTime: " + SimulationProcess.CurrentTime() + "\n");
 				fs1.write("Forwarding Table Entry @ Node 2 for ObjectID " + curPacket.getPacketId() + " is: " + getForwardingTableEntry(curPacket.getPacketId()) + "\n" + "\n");
 				fs1.write("Is it in the Global Cache or not: " + getGlobalCache().isPresentInCache(curPacket) + "\n");
 				fs1.write("Is it in the Local Cache or not: " + getLocalCache().isPresentInCache(curPacket) + "\n" + "\n" + "\n" + "\n");
 				fs1.close();
 			}
 			catch (IOException e){}	
 		}
 		*/	
 	}
 	/**
 	 * Interest Packet handler for the machine. It first searches in Machine caches. If it fails then adds it to PIT. 
 	 * Then looks in Forwarding Table
 	 * if successful forwards the Interest packet to that node. If it fails then floods all the neighboring nodes. 
 	 * @param curPacket
 	 */
 	public void interestPacketsHandler(Packets curPacket) {
 		
 		InterestEntry interest = new InterestEntry (curPacket.getPacketId(), curPacket.getSegmentId());
 		
 		/* The following code is to suppress the interest packets that have already been served */
 		if (isInterestServed(interest)) {
 			
 			curPacket.finished(SupressionTypes.SUPRESSION_ALREADY_SERVED);
 			log.info("Already served interest packet "+ curPacket.getPacketId() + " with segment ID " + curPacket.getSegmentId());
 			return;
 		}
 		
 		log.info("Machine Interest packet handler"+curPacket.toString());
 		
 		/* Add the interest packet into the list of served interest packets. It will assist in achieving the step above, which
 		 * is to suppress interest packets already served 
 		 * */
 		addToInterestServed(interest);
 		
 		Boolean newInPit = false;
 		
 		/* The following code is called when the interest is satisfied from: (a) the Local Cache, which means, the object originally 
 		 * resides on this CCNRouter; or (b) the Global Cache also known as the content store of CCN */
 		
 		InterestEntry dataObject = new InterestEntry (curPacket.getRefPacketId(), curPacket.getSegmentId());		
 		Packets data_packet = getDataPacketfromCache(dataObject);
 		
 		if(data_packet != null) {
 			
 			log.info("Sending data packet to nodeId:"+Integer.toString(curPacket.getPrevHop()));
 			
 			/* Changing the reference of the data packet to the interest packet, after sendPacket should change back to -1 */
 			data_packet.setRefPacketId(curPacket.getSourcePacketId());
 			data_packet.setSegmentId(curPacket.getSegmentId());
 			
 			/* Create a data packet in reply of the interest packet, and place it in the trace file */
 			Packets.dumpStatistics(data_packet, "CRTDPRD");			
 			
 			sendPacket((Packets) data_packet.clone(), curPacket.getPrevHop());
 			
 			data_packet.setRefPacketId(-1);
 			data_packet.setSegmentId(0);
 			curPacket.finished(SupressionTypes.SUPRESSION_SENT_DATA_PACKET);
 			return;
 		}
 		
 		log.info("Inserting into pit table");
 		List<PITEntry> pitEntry = pit.get(interest);
 		
 		/* I havn't seen this packet so I need to create a new PIT entry for this objectID */
 		if(pitEntry == null) {
 			log.info("New entry in pit table");
 			pitEntry = new ArrayList<PITEntry>();
 			newInPit=true;
 		}		
 		
 		/* 
 		 * This step is to avoid duplicate entries in the PITEntry list. If node on previous hop has been already present in the 
 		 * PITEntry list then there is no reason to enter a duplicate entry 
 		 *  */
 		
 		/* TODO: I am confused about the parameter for the contains method. Shouldn't it be a PITEntry object? */
 		if(!pitEntry.contains(curPacket.getPrevHop())) {
 						
 			pitEntry.add(new PITEntry (curPacket.getPrevHop(), SimulationProcess.CurrentTime()));
 			pit.put(interest, pitEntry);
 		}
 		
 		log.info("Current Pit table-> "+pit);
 		
 		/* If we have a FIB match, then we will not flood the packet. We will simply help it along the FIB entry. If not then
 		 * we will flood the packet 
 		 * */
 		FIBEntry rid = getForwardingTableEntry(curPacket.getRefPacketId());
 		if(rid != null) {
 			 /* Questions for Mahesh: Why do we want to destroy this packet ? Why are we not cloning Packet before transmitting it? */
 			log.info("Forwarding table hit sending to"+ rid.getDestinationNode());
 			sendPacket((Packets)curPacket.clone(), rid.getDestinationNode());
 			curPacket.finished(SupressionTypes.SUPPRESS_FLOODING_FIB_HIT);			
 		}
 		/* oh god !! the flooding devil */
 		else {
 			floodInterestPacket(curPacket);			
 		}	
 	}
 	/**
 	 * gets the forwardingTableEntry for this packetid.
 	 * @param packetId
 	 * @return
 	 */
 	private FIBEntry getForwardingTableEntry(Integer packetId) {
 		
 		return forwardingTable.get(packetId);
 	}
 	/**
 	 * Searches local cache and then global cache to find the the packet and if successful returns it else returns null;
 	 * @param packetId Id if the data packet
 	 * @return data packet
 	 */
 	private Packets getDataPacketfromCache(InterestEntry dataObject) {
 		
 		InterestEntry localDataObject = new InterestEntry (dataObject.getInterestID(), 0);
 		
 		Packets packet = localCache.get(localDataObject);
 	
 		if(packet != null) {
 			
 			log.info("Hit in local cache "+packet.toString());
 			return packet;
 		}
 		
 		packet = globalCache.get(dataObject);
 
 		if(packet != null) {
 			
 			log.info("Hit in Global cache "+packet.toString());
 			return packet;
 		}
 		
 		log.info("cache miss ");
 		return null;		
 	}
 	
 	/**
 	 * Floods packet on the all the interfaces of this router, except on the node from which this packet came from.
 	 * @param curPacket Packet to be flooded.
 	 */
 	public void floodInterestPacket(Packets curPacket) {
 		
 		LinkedHashSet<HashMap<Integer, Integer>> adjList= Grid.getAdjacencyList(getRouterId());
 		Iterator<HashMap<Integer,Integer>> itr = adjList.iterator();
 		log.info("Flooding the packet to {");
 
 		/* Getting the previous node of the packet so as not to flood to same node */ 
 		int srcNode = curPacket.getPrevHop(); 
 		
 		/* The following code is used to send packets to all the neighbors of a node */
 		while(itr.hasNext()) {
 			
 			HashMap<Integer,Integer> adjNode = (HashMap<Integer, Integer>) itr.next();
 			Integer nodeId = adjNode.keySet().iterator().next();
 			
 			 /* We shouldn't flood the interest packet to same node where it came from */
 			if(nodeId != srcNode) {
 				
 				Packets pacToadd = (Packets)curPacket.clone();
 				sendPacket(pacToadd, nodeId);
 			}
 		}
 		log.info("}");
 	}	
 	
 	/**
 	 * This function puts the packet in the destination Router's queue. But makes necessary changes to the packet before putting it.
 	 * It creates a new SimulationProcess TransmitPackets and adds transmission delay to it.
 	 */
 	public void sendPacket(Packets curPacket,final Integer nodeId) {
 		
 		if(nodeId != -1) {
 			
 			/* setting the source id as my Id before flooding it to my good neighbors */
 			curPacket.setPrevHop(getRouterId()); 
 			curPacket.incrHops();
 			
 			TransmitPackets trans = new TransmitPackets(curPacket, nodeId);
 			
 			try {
 				trans.ActivateDelay(TransmitPackets.getTransDelay());
 			} 
 			catch (SimulationException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} 
 			catch (RestartException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		
 			log.info("Sending packet to nodeId:"+nodeId);
 		}
 		else {
 			
 			curPacket.finished(SupressionTypes.SUPRESSION_DEST_NODE);
 			log.info("Packet Destined to my node,so suppressing");
 		}
 	}
 	
 	@Override
 	/* I am not sure if this function is even needed. Moreover, the two functions following it seem unnecessary */
 	public void Activate() {
 		
 		if (!getPacketsQ().isEmpty() && !Processing()) {
 			
 			try {				
 				super.Activate();
 			}
 			catch (SimulationException e) {
 				log.info("Exception "+ e.toString());
 			}
 			catch (RestartException e) {
 				log.info("Exception "+ e.toString());
 			}
 		}
 		else
 			log.info("Not activating "+getPacketsQ().isEmpty()+Processing());
 	}	
 	
 	public boolean Processing () {
 		return working;
 	}
 
 	public double ServiceTime () {
 		try
 		{
 			return STime.getNumber();
 		}
 		catch (IOException e)
 		{
 			return 0.0;
 		}
 	}
 
 	@Override
 	public String toString() {
 		
 		String str;
 		str = "CCNRouter\n{ Id:"+getRouterId()+ " \n"+ getPacketsQ().toString()+"\n PIT:"+getPIT().toString()+"\n ForwardingTable"+
 		getForwardingTable().toString()+"\n interestServedTable"+interestsServed.toString()+"\nGlobalCache:"+getGlobalCache().toString()+"\nLocalCache:"+getLocalCache().toString()+"}\n";
 		return str;
 	}
 	
 	public CCNQueue getPacketsQ() {
 		return packetsQ;
 	}
 	
 	public void setPacketsQ(CCNQueue packetsQ) {
 		packetsQ = this.packetsQ;
 	}
 	
 	public int getRouterId() {
 		return routerId;
 	}
 	
 	public void setRouterId(int routerId) {
 		this.routerId = routerId;
 	}
 	
 	public boolean isPresentInPit(Integer packetId) {
 		
 		if(pit.containsKey(packetId))
 			return true;
 		else
 			return false;
 	}
 	
 	public boolean isPresentInQueue(Packets packet) {
 		
 	    if(packetsQ.contains(packet))
 	    	return true;
 	    else
 	    	return false;
 	}
 	
 	public Map<InterestEntry, List<PITEntry>> getPIT() {
 		return pit;
 	}
 	
 	public void setPIT(Map<InterestEntry, List<PITEntry>> tempPit) {
 		pit = tempPit;
 	}
 	
 	public CCNCache getLocalCache() {
 		return localCache;
 	}
 	
 	public void setLocalCache(CCNCache localCache) {
 		this.localCache = localCache;
 	}
 	
 	public CCNCache getGlobalCache() {
 		return globalCache;
 	}
 	
 	public void setGlobalCache(CCNCache globalCache) {
 		this.globalCache = globalCache;
 	}
 
 	public Map<Integer, FIBEntry> getForwardingTable() {
 		return forwardingTable;
 	}
 
 	public void setForwardingTable(Map<Integer, FIBEntry> forwardingTable) {
 		this.forwardingTable = forwardingTable;
 	}
 
 	/**
 	 * Searches the interestServed list.
 	 * @param id
 	 * @return
 	 */
 	public boolean isInterestServed(InterestEntry temp) {		
 		return interestsServed.contains(temp);
 	}
 	/**
 	 * Adds to the interestServed Table.
 	 * @param id
 	 */
 
 	public void addToInterestServed(InterestEntry temp) {
 		this.interestsServed.add(temp);
 	}
 
 	public int getLogCounter() {
 		
 		logCounter=logCounter+1;
 		return logCounter;
 	}
 
 	public void setLogCounter(int logCounter) {
 		this.logCounter = logCounter;
 	}
 
 	public static double getProcDelay() {
 		return procDelay;
 	}
 
 	public static void setProcDelay(double procDelay) {
 		CCNRouter.procDelay = procDelay;
 	}
 	
 	public static double getPitTimeOut (){
 		return pitTimeOut;
 	}
 
 	public static void setPitTimeOut (double tempTimeOut) {
 		CCNRouter.pitTimeOut = tempTimeOut;
 	}
 };
