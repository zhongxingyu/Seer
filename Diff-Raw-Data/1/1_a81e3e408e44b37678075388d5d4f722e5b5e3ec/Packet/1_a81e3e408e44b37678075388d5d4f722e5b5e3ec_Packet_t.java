 /*******************************************************************************
  * Forwarding on Gates Simulator/Emulator
  * Copyright (C) 2012, Integrated Communication Systems Group, TU Ilmenau.
  * 
  * This program and the accompanying materials are dual-licensed under either
  * the terms of the Eclipse Public License v1.0 as published by the Eclipse
  * Foundation
  *  
  *   or (per the licensee's choosing)
  *  
  * under the terms of the GNU General Public License version 2 as published
  * by the Free Software Foundation.
  ******************************************************************************/
 package de.tuilmenau.ics.fog.packets;
 
 import java.io.Serializable;
 import java.rmi.RemoteException;
 import java.util.LinkedList;
 
 import de.tuilmenau.ics.fog.Config;
 import de.tuilmenau.ics.fog.facade.Description;
 import de.tuilmenau.ics.fog.facade.Signature;
 import de.tuilmenau.ics.fog.packets.statistics.IPacketStatistics;
 import de.tuilmenau.ics.fog.routing.Route;
 import de.tuilmenau.ics.fog.routing.RouteSegment;
 import de.tuilmenau.ics.fog.routing.RouteSegmentDescription;
 import de.tuilmenau.ics.fog.topology.NeighborInformation;
 import de.tuilmenau.ics.fog.topology.NetworkInterface;
 import de.tuilmenau.ics.fog.topology.Simulation;
 import de.tuilmenau.ics.fog.transfer.ForwardingElement;
 import de.tuilmenau.ics.fog.transfer.ForwardingNode;
 import de.tuilmenau.ics.fog.transfer.gates.AbstractGate;
 import de.tuilmenau.ics.fog.transfer.gates.GateID;
 import de.tuilmenau.ics.fog.transfer.gates.headers.ProtocolHeader;
 import de.tuilmenau.ics.fog.ui.Logging;
 import de.tuilmenau.ics.fog.ui.Statistic;
 import de.tuilmenau.ics.fog.util.Size;
 
 
 public class Packet implements Serializable
 {
 	private static final long serialVersionUID = -4342798823284871078L;
 	private static final int PACKET_MAX_CHANGE_COUNTER = 100;
 	
 	public Packet(Serializable data)
 	{
 		setRoute(new Route());
 		mPayload = data;
 		mReturnRoute = new Route();
 		mId = getNewId();
 	}
 
 	public Packet(Route gateids, Serializable data)
 	{
 		setRoute(gateids);
 		mPayload = data;
 		mReturnRoute = new Route();
 		mId = getNewId();
 	}
 
 	public Packet(Route gateids, Route gateidsReturnRoute, Serializable data)
 	{
 		setRoute(gateids);
 		mPayload = data;
 		mReturnRoute = gateidsReturnRoute;
 		mId = getNewId();
 	}
 
 	private Packet(Route gateids, Route gateidsReturnRoute, Serializable data, Long pId)
 	{
 		setRoute(gateids);
 		mPayload = data;
 		mReturnRoute = gateidsReturnRoute;
 		mId = pId;
 	}
 
 	public Packet(Serializable data, Packet pPredecessor)
 	{
 		setRoute(new Route());
 		mPayload = data;
 		mReturnRoute = new Route();
 		mId = getNewId();
 		mPredecessorId = pPredecessor.getId();
 	}
 
 	public Packet(Route gateids, Serializable data, Packet pPredecessor)
 	{
 		setRoute(gateids);
 		mPayload = data;
 		mReturnRoute = new Route();
 		mId = getNewId();
 		mPredecessorId = pPredecessor.getId();
 	}
 
 	public Packet(Route gateids, Route gateidsReturnRoute, Serializable data, Packet pPredecessor)
 	{
 		setRoute(gateids);
 		mPayload = data;
 		mReturnRoute = gateidsReturnRoute;
 		mId = getNewId();
 		mPredecessorId = pPredecessor.getId();
 	}
 	
 	/**
 	 * @deprecated Just for internal use and for debugging purposes! In reality, a packet does not have an ID! 
 	 * @return ID of packet
 	 */
 	public long getId()
 	{
 		return mId;
 	}
 	
 	public boolean change()
 	{
 		mChangeCounter--;
 		
 		return (mChangeCounter > 0);
 	}
 	
 	public void setNewId()
 	{
 		mPredecessorId = mId;
 		mId = getNewId();
 	}
 	
 	/**
 	 * @return New ID for packet
 	 */
 	private synchronized static long getNewId()
 	{
 		IPacketIDManager IDManager = PacketIDManager.getSimulationPacketIDManager();
 		long Id = 0;
 		try {
 			Id = IDManager.getID();
 		} catch (RemoteException rExc) {
 			Logging.err(Packet.class, "No managagement of packet IDs available", rExc);
 			Id = lastUsedId++;
 		} 
 		return Id;
 	}
 	
 	public Route getRoute()
 	{
 		return mRoute;
 	}
 	
 	public void setRoute(Route pNewRoute)
 	{
 		if (mRoute == null) {
 			if (pNewRoute != null) {
 				mInitRouteLength = pNewRoute.size();
 			} else {
 				mInitRouteLength = 0;
 			}
 		}
 		
 		if(pNewRoute != null) mRoute = pNewRoute;
 		else mRoute = new Route();
 	}
 
 	public void addGateIDFront(GateID id)
 	{
 		mRoute.addFirst(id);
 	}
 
 	public void addGateIDFront(RouteSegment segment)
 	{
 		mRoute.addFirst(segment);
 	}
 
 	public void addGateIDFront(Route route)
 	{
 		// make a deep copy and add it to this route
 		for(int i = route.size() -1; i >= 0; i--) {
 			RouteSegment seg = route.get(i);
 			
 			if(seg != null) {
 				mRoute.addFirst(seg.clone());
 			}
 			// else: ignore useless segment
 		}
 	}
 
 	public GateID fetchNextGateID()
 	{
 		GateID res = mRoute.getFirst(true);
 		
 		if(res != null) {
 			mGatesPassed++;
 		}
 		
 		return res;
 	}
 
 	public Description fetchNextDescription() {
 		
 		RouteSegmentDescription tNextRouteDescription = mRoute.getFirstDescription(true);
 		
 		if (tNextRouteDescription != null)
 			return tNextRouteDescription.getDescription();
 		else
 			return null;
 	}
 
 	public void addReturnRoute(GateID id)
 	{
 		if(mReturnRoute != null) {
 			mReturnRoute.addFirst(id);
 		}
 	}
 
 	public void activateTraceRouting()
 	{
 		mTraceRouting = true;
 	}
 	
 	public boolean isTraceRouting()
 	{
 		return mTraceRouting;
 	}
 
 	public boolean traceBackwardRoute()
 	{
 		return !isReturnRouteBroken();
 	}
 
 	/**
 	 * Backward route trace was not possible
 	 * => invalidate return route
 	 */
 	public void returnRouteBroken()
 	{
 		mReturnRoute = null;
 	}
 
 	public boolean isReturnRouteBroken()
 	{
 		return (mReturnRoute == null);
 	}
 
 	/**
 	 * @return backward route or null, if route not valid
 	 */
 	public Route getReturnRoute()
 	{
 		return mReturnRoute;
 	}
 
 	/**
 	 * Creates a packet addressed for the sender of the original packet
 	 * 
 	 * @param pPayload
 	 *            payload for answer packet
 	 * @return packet OR null if error occured
 	 */
 	public Packet createReversePacket(Serializable pPayload)
 	{
 		if (isReturnRouteBroken()) {
 			// TODO extend it with Hop-by-Hop routing!
 			return null;
 		} else {
 			return new Packet(getReturnRoute(),	pPayload);
 		}
 	}
 
 	public Serializable getData()
 	{
 		return mPayload;
 	}
 	
 	/**
 	 * @param data The serializable Payload to set.
 	 */
 	public void setData(Serializable data)
 	{
 		mPayload = data;
 	}
 	
 	public boolean isSignalling()
 	{
 		return (mPayload instanceof Signalling);
 	}
 	
 	/**
 	 * @return true in case a forwarding node should add its signature to the packet
 	 */
 	public boolean pleaseAuthenticate()
 	{
 		return Config.Logging.AUTHENTICATE_PACKETS;
 	}
 	
 	/**
 	 * @return true in case at least one signature is present
 	 */
 	public boolean isAuthenticated()
 	{
 		if(mAuthentications != null) {
 			return (mAuthentications.size() > 0);
 		} else {
 			return false;
 		}
 	}
 	
 	/**
 	 * Adds a signature to list of authentication information.
 	 * Skip signatures where successor would insert same signature like predecessor.
 	 * 
 	 * @param pAuthentication signature supposed to be added
 	 */
 	public void addAuthentication(Signature pAuthentication)
 	{
 		if(pAuthentication != null) {
 			if(mAuthentications == null) mAuthentications = new LinkedList<Signature>();
 			
 			if(mAuthentications.isEmpty()) {
 				mAuthentications.add(pAuthentication);
 			}
 			else if(!(mAuthentications.getLast().equals(pAuthentication))) {
 				mAuthentications.addLast(pAuthentication);
 			}
 		}
 	}
 	
 	/**
 	 * @return First signature, which is most probably the signature of the sender; null if no signature is present 
 	 */
 	public Signature getSenderAuthentication()
 	{
 		if(mAuthentications != null) {
 			return mAuthentications.getFirst();
 		} else {
 			return null;
 		}
 	}
 	
 	/**
 	 * @return All signatures of the packet; null if no signatures are present
 	 */
 	public LinkedList<Signature> getAuthentications()
 	{
 		return mAuthentications;
 	}
 	
 	/**
 	 * Not included in FoG specification. Simulator specific!
 	 * Invisible packets are used to handle special GUI related tasks
 	 * in the simulator.
 	 * 
 	 * @return If this packet is an invisible one and should forwarded without changes and without listing it to the statistics
 	 */
 	public boolean isInvisible()
 	{
 		return (mPayload instanceof Invisible);
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public Packet clone()
 	{
 		Route clonedgateidlist = new Route(mRoute);
 		Route clonedgateidlistReturn = null;
 
 		if (mReturnRoute != null)
 			clonedgateidlistReturn = new Route(mReturnRoute);
 		
 		Packet tClone = new Packet(clonedgateidlist, clonedgateidlistReturn, mPayload, mId);
 		
 		if(mAuthentications != null) {
 			tClone.mAuthentications = (LinkedList<Signature>) mAuthentications.clone();
 		}
 		
 		tClone.setLowerLayers((LinkedList<String>) mLowerLayers.clone());
 		tClone.setSourceNode(mSourceNode);
 		tClone.setTargetNode(mTargetNode);
 		tClone.mPredecessorId = mPredecessorId;
 		tClone.mGatesPassed = mGatesPassed;
 		tClone.mInitRouteLength = mInitRouteLength;
 		tClone.mChangeCounter = mChangeCounter;
		tClone.mTraceRouting = mTraceRouting;
 		
 		return tClone;
 	}
 
 	/**
 	 * TODO rewrite for boosting performance
 	 */
 	@Override
 	public String toString()
 	{
 		String tAuth = " " +authenticationListToString(mAuthentications);
 		String tReverseList = null;
 		
 		if(!traceBackwardRoute()) tReverseList = "";
 		else tReverseList = mReturnRoute.toString();
 		
 		if(!isAuthenticated()) tAuth = "";
 		
 		return "(" + mRoute +tReverseList +", '" + mPayload + "'" +tAuth +")";
 	}
 	
 	private static String authenticationListToString(LinkedList<Signature> list)
 	{
 		StringBuffer tRes = new StringBuffer(128);
 		
 		tRes.append("{");
 		if(list != null) {
 			if(list.size() > 0) {
 				for(int i = 0; i < list.size(); i++) {
 					tRes.append(list.get(i));
 					
 					if(i < list.size() - 1) {
 						tRes.append(",");
 					}
 				}
 			}
 		} else {
 			tRes.append("null");
 		}
 		tRes.append("}");
 
 		return tRes.toString();
 	}
 	
 	public void setSourceNode(String pSourceNode)
 	{
 		mSourceNode = pSourceNode;
 	}
 
 	public void setTargetNode(String pTargetNode)
 	{
 		mTargetNode = pTargetNode;
 	}
 
 	/**
 	 * For internal use in the node, only. The lower layer information
 	 * are needed for setting up new DownGates.
 	 * 
 	 * @param pLowerLayerInfos Network interface and parameters from which the packet was received
 	 */
 	public void setReceivingLowerLayer(NetworkInterface pReceivingInterface, NeighborInformation pFrom)
 	{
 		mReceivingInterface = pReceivingInterface;
 		mFrom = pFrom;
 	}
 	
 	public NetworkInterface getReceivingInterface()
 	{
 		return mReceivingInterface;
 	}
 	
 	public NeighborInformation getReceivedFrom()
 	{
 		return mFrom;
 	}
 	
 	public void addBus(String bus)
 	{
 		if(Config.Connection.LOG_PACKET_STATIONS){
 			Logging.log(this, "Packet " + getId() + " passes: " + bus);
 		}
 		mLowerLayers.add(bus);
 	}
 	
 	public LinkedList<String> getBus()
 	{
 		return mLowerLayers;
 	}
 
 	private void setLowerLayers(LinkedList<String> clone)
 	{
 		mLowerLayers = clone;
 	}
 
 	/**
 	 * For internal use in the node, only. The route is recorded when a packet
 	 * is descending the gates. Is needed in case of bus errors. 
 	 */
 	public void addToDownRoute(GateID pGateID)
 	{
 		if (mDownRoute == null) mDownRoute = new Route();
 		mDownRoute.addLast(pGateID);
 	}
 
 	public void clearDownRoute()
 	{
 		mDownRoute = null;
 	}
 	
 	public Route getDownRoute()
 	{
 		return mDownRoute;
 	}	
 
 	public void logStats(Simulation pSim)
 	{
 		if(Config.Logging.WRITE_PACKET_STATISTIC) {
 			if(mPayload instanceof LoggableElement) {
 				if(!((LoggableElement)mPayload).logMe()) {
 					pSim.getLogger().trace(this, "Not logging me");
 					return;
 				}
 			}
 			if(mAuthentications != null) {
 				if(!mAuthentications.isEmpty()) logStats(pSim, mAuthentications.getLast());
 			} else {
 				logStats(pSim, null);
 			}
 		}
 	}
 	
 	public void logStats(Simulation pSim, Object pLastHopName)
 	{
 		if(Config.Logging.WRITE_PACKET_STATISTIC) {
 			if(mPayload instanceof LoggableElement)
 			{
 				if(!((LoggableElement)mPayload).logMe()) {
 					pSim.getLogger().trace(this, "Not logging me");
 					return;
 				}
 			}
 			LinkedList<String> tColumnList = new LinkedList<String>();
 			tColumnList.add(mId.toString());
 			if (mPredecessorId != null) {
 				tColumnList.add(mPredecessorId.toString());
 			} else {
 				tColumnList.add("");
 			}
 			if (mPayload != null) {
 				tColumnList.add(mPayload.getClass().getSimpleName());
 			} else {
 				tColumnList.add("");
 			}
 			if (mSourceNode != null) {
 				tColumnList.add(mSourceNode.toString());
 			}
 			else if (getData() instanceof ExperimentAgent && ((ExperimentAgent)getData()).getSourceNode() != null) {
 				tColumnList.add(((ExperimentAgent)getData()).getSourceNode());
 			} else {
 				tColumnList.add("");
 			}
 			if (mTargetNode != null) {
 				tColumnList.add(mTargetNode.toString());
 			}
 			else if (getData() instanceof ExperimentAgent && ((ExperimentAgent)getData()).getDestNode() != null) {
 					tColumnList.add(((ExperimentAgent)getData()).getDestNode());
 			} else {
 				tColumnList.add("");
 			}
 			if (mAuthentications != null) { // Hops
 				// mAuthentications includes source which is no hop, so subtract 1
 				tColumnList.add(Integer.toString(mAuthentications.size() - 1));
 			} else {
 				tColumnList.add("");
 			}
 			tColumnList.add(mGatesPassed.toString());
 			tColumnList.add(mInitRouteLength.toString());
 			if (pLastHopName != null) {
 				tColumnList.add(pLastHopName.toString());
 			} else {
 				tColumnList.add("");
 			}
 			// additional payload specific columns
 			if (mPayload instanceof IPacketStatistics) {
 				pSim.getLogger().debug(this, "getting statistics from this packet");
 				tColumnList.addAll(((IPacketStatistics) mPayload).getStats());
 				// TODO: evtl. auf null testen
 			}
 	
 			try {
 //				if (mPayload instanceof Reroute) {
 //					Logging.log("Logstats", tColumnList.toString());
 //					StackTraceElement[] plah = Thread.currentThread().getStackTrace();
 //					for (StackTraceElement tElem : plah) {
 //						Logging.log("Logstats", tElem.toString());
 //					}
 //				}
 				Statistic.getInstance(pSim, Packet.class).log(tColumnList);
 			} catch(Exception e) {
 				pSim.getLogger().err(this, "Can not write statistic log.", e);
 			}
 		}
 	}
 	
 	public int getSerialisedSize()
 	{
 		/*
 		 * Packet size in byte:
 		 * Length of header      = 2
 		 * Flags                 = 1 (Return route, signaling (4 options), auth.)
 		 * Modification counter  = 1
 		 * Length pay load       = 2
 		 * Route                 = dynamic
 		 * Payload               = dynamic
 		 * Auth                  = dynamic
 		 * Return route          = dynamic
 		 */
 		int tResult = 6;
 		
 		if(mRoute != null) tResult += mRoute.getSerialisedSize();
 		
 		if(mPayload instanceof ProtocolHeader) {
 			tResult += ((ProtocolHeader) mPayload).getSerialisedSize();
 		} else {
 			tResult += Size.sizeOf(mPayload);
 		}
 
 		tResult += Size.sizeOf(mAuthentications);
 		if(mReturnRoute != null) tResult += mReturnRoute.getSerialisedSize();
 		
 		return tResult;		
 	}
 	
 	/**
 	 * helper method telling a contained experiment about the next steps.
 	 */
 	private void registerForwarding(ForwardingElement elem, int type)
 	{
 		if (getData() instanceof ExperimentAgent) {
 			// the packet contains an experiment -> check whether we need to carry it out
 			ExperimentAgent tExp = (ExperimentAgent)getData(); 
 			if (tExp.atType(type)) {
 				tExp.nextStep(elem, this);
 			}
 		}					
 	}
 	
 	/**
 	 * The packet is about to be forwarded by the handler given in the parameter.
 	 * 
 	 * @param handler The forwarding node which is about to forward the packet.
 	 */
 	public void forwarded(ForwardingNode handler)
 	{
 		if(Config.Connection.LOG_PACKET_STATIONS){
 			Logging.log(this, "Packet " + getId() + " passes: " + handler);
 		}
 		registerForwarding(handler, ExperimentAgent.FN_NODE);
 	}
 	
 	/**
 	 * The packet is about to be forwarded by the handler given in the parameter.
 	 * 
 	 * @param handler The gate which is about to forward the packet.
 	 */
 	public void forwarded(AbstractGate handler)
 	{
 		if(Config.Connection.LOG_PACKET_STATIONS){
 			Logging.log(this, "Packet " + getId() + " passes: " + handler);
 		}
 		registerForwarding(handler, ExperimentAgent.GATE);
 	}
 	
 	/**
 	 * The packet was dropped in a forwarding node. This method is called
 	 * by the network entity which made the decision to drop a packet.
 	 * 
 	 * @param handler The node that dropped the packet.
 	 */
 	public void dropped(ForwardingNode handler)
 	{
 		if (getData() instanceof ExperimentAgent) {
 			((ExperimentAgent)getData()).finish(handler, this);
 		}
 		this.logStats(handler.getEntity().getNode().getAS().getSimulation(), handler.getEntity()); // log statistics of this packet as it finished its way through the network
 	}
 	
 	/**
 	 * The packet was dropped by a gate. This method is called by a gate that
 	 * decides to drop a packet.
 	 * 
 	 * @param handler The gate that dropped the packet.
 	 */
 	public void dropped(AbstractGate handler)
 	{
 		// TODO how do we get the correspond node for a gate?
 		Logging.warn(this, ">>> ### PACKET DROPPED ### <<<");
 	}
 	
 	/**
 	 * The packet reached it's final destination. This method normally
 	 * should only be called by ClientFN or forwarding node with similar
 	 * objectives.
 	 * 
 	 * @param target The node which is the final target of this packet.
 	 */
 	public void finished(ForwardingNode target)
 	{
 		if (getData() instanceof ExperimentAgent) {
 			((ExperimentAgent)getData()).finish(target, this);
 		}
 		this.logStats(target.getEntity().getNode().getAS().getSimulation(), target.getEntity()); // log statistics of this packet as it finished its way through the network
 	}
 
 	/**
 	 * A packet was detected as being dropped. This method is called by the infrastructure
 	 * if a packet was dropped somewhere along the path outside the network's control (e.g. by
 	 * a lossy bus). Instead of giving a hint where the packet was dropped, the system can only give
 	 * an indication who detected the dropping.
 	 * 
 	 * @param detector The node that detected the dropping of the packet.
 	 */
 	public void droppingDetected(Object detector, Simulation simulation)
 	{
 		if (getData() instanceof ExperimentAgent) {
 			((ExperimentAgent)getData()).finish(null, this);
 		}
 		this.logStats(simulation); // log statistics of this packet as it finished its way through the network		
 	}
 	
 	private Route mRoute = null;
 	private int mChangeCounter = PACKET_MAX_CHANGE_COUNTER;
 	private Route mReturnRoute;
 	private boolean mTraceRouting = false;
 	private Serializable mPayload;
 	private LinkedList<Signature> mAuthentications; // lacy creation
 	private LinkedList<String> mLowerLayers = new LinkedList<String>();
 
 	// for error handling inside of a node
 	private transient NetworkInterface mReceivingInterface = null;
 	private transient NeighborInformation mFrom = null;
 	private transient Route mDownRoute = null;
 
 	//
 	// Statistics:
 	//   Just for debugging and GUI use. In reality, packets do not transport these values.
 	//
 	private static long lastUsedId = 0;
 	private Long mId;
 	private Long mPredecessorId = null;
 	private String mSourceNode = null;
 	private String mTargetNode = null;
 	private Integer mGatesPassed = 0;
 	private Integer mInitRouteLength; // Length of the very first route assigned to packet
 }
