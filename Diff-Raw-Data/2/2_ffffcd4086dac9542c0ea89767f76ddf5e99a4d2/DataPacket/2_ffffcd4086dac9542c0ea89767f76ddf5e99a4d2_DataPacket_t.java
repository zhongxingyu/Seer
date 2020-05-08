 package com.simulator.packets;
 
 
 import org.apache.log4j.Logger;
 
 import arjuna.JavaSim.Simulation.Scheduler;
 
 import com.simulator.enums.DataPacketsApplTypes;
 import com.simulator.enums.SimulationTypes;
 
 
 /* *
 
  * This is class for DataPacket for CCN networks it extends Pakcets Class. It has constructors for GlobeTraffic Generator. 
  * NOTE:
  *  Important Note while adding elements to this class make sure you add only basic data types (i.e like int,char etc) , if you have to add Complex data types
  *  please make sure to edit clone() method of packets so that it works properly.
 **/
 public class DataPacket extends Packets implements Cloneable {
 	
 	private DataPacketsApplTypes applType;
 
 	static final Logger log = Logger.getLogger(DataPacket.class);
 	
 	private int popularity;
 	
 	/**
 	 * 
 	 * Constructor for DataPacket which parses a line from Doc.all from GlobeTraffic and sets the properties accordinly.
 	 * @param line line from docs.all file
 	 * @param nodeId node to which this packet is assigned.
 
 	 * Notes:
 	 * When a packet is created 
 	 * We get a unique Id from a static packet Id generator and assign it to PacketId. We also assign the same Id of sourceId since we
 	 * are creating the packet here.
 	 */
 
 	public DataPacket(Integer nodeId, int size) {
 		
 		setPacketId(getCurrenPacketId());
 		setSourcePacketId(getPacketId());
 		setPacketType(SimulationTypes.SIMULATION_PACKETS_DATA);
 		setPrevHop(-1);
 		setRefPacketId(-1);
 		setOriginNode(nodeId);
 		setSizeOfPacket(size);
 		setAlive(true);
 		setCauseOfSupr(SimulationTypes.SIMULATION_NOT_APPLICABLE);
 		log.info("node id = "+nodeId+" packet id ="+ getPacketId());
 	}
 	/**
 	 * Constructor for DataPacket which parses a line from Doc.all from GlobeTraffic and sets the properties accordinly.
 	 * @param line line from docs.all file
 	 * @param nodeId node to which this packet is assigned.
 	 */
 
 
 
 	public DataPacket(String line,int nodeId,int segId)
 	{
 		String [] words = line.split("\\s+");
 
 		setSegmentId (segId);
 
 		setPacketId(Integer.parseInt(words[0]));
 		setPopularity(Integer.parseInt(words[1]));
 		setApplType(DataPacketsApplTypes.values()[Integer.parseInt(words[3])]);
 		setSizeOfPacket(Integer.parseInt(words[2]));
 		setSourcePacketId(getPacketId());
 		setPacketType(SimulationTypes.SIMULATION_PACKETS_DATA);
 		setPrevHop(-1);
 		setRefPacketId(-1);
 		setOriginNode(nodeId);
 		setAlive(true);
 		setCauseOfSupr(SimulationTypes.SIMULATION_NOT_APPLICABLE);
 		log.info("node id = "+nodeId+" packet id ="+ getPacketId());
 
 		setSegmentId (segId);
 		//super(1,SimulationTypes.SIMULATION_PACKETS_DATA,2);
 	}
 	
 
 	@Override
 	public Object clone() {
 		
		DataPacket clonedPacket = (DataPacket) super.clone();
 		//clonedPacket.pathTravelled = new String(this.getPathTravelled());
 		return clonedPacket;
 	}
 
 	public DataPacketsApplTypes getApplType() {
 		return applType;
 	}
 
 	public void setApplType(DataPacketsApplTypes applType) {
 		this.applType = applType;
 	}
 
 	public int getPopularity() {
 		return popularity;
 	}
 
 	public void setPopularity(int popularity) {
 		this.popularity = popularity;
 	}
 		
 };
