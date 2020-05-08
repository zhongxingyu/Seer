 /*******************************************************************************
  * Forwarding on Gates Simulator/Emulator - Hierarchical Routing Management
  * Copyright (c) 2012, Integrated Communication Systems Group, TU Ilmenau.
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html.
  ******************************************************************************/
 package de.tuilmenau.ics.fog.routing.hierarchical;
 
 import java.net.UnknownHostException;
 import java.rmi.RemoteException;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Observer;
 
 import de.tuilmenau.ics.fog.IEvent;
 import de.tuilmenau.ics.fog.application.Application;
 import de.tuilmenau.ics.fog.application.Service;
 import de.tuilmenau.ics.fog.facade.Binding;
 import de.tuilmenau.ics.fog.facade.Connection;
 import de.tuilmenau.ics.fog.facade.Description;
 import de.tuilmenau.ics.fog.facade.IServerCallback;
 import de.tuilmenau.ics.fog.facade.Name;
 import de.tuilmenau.ics.fog.facade.Namespace;
 import de.tuilmenau.ics.fog.facade.NetworkException;
 import de.tuilmenau.ics.fog.facade.RequirementsException;
 import de.tuilmenau.ics.fog.facade.RoutingException;
 import de.tuilmenau.ics.fog.facade.Signature;
 import de.tuilmenau.ics.fog.facade.properties.CommunicationTypeProperty;
 import de.tuilmenau.ics.fog.packets.NeighborRoutingInformation;
 import de.tuilmenau.ics.fog.packets.hierarchical.DiscoveryEntry;
 import de.tuilmenau.ics.fog.routing.Route;
 import de.tuilmenau.ics.fog.routing.RouteSegmentPath;
 import de.tuilmenau.ics.fog.routing.RoutingServiceLink;
 import de.tuilmenau.ics.fog.routing.hierarchical.election.BullyPriority;
 import de.tuilmenau.ics.fog.routing.hierarchical.management.*;
 import de.tuilmenau.ics.fog.routing.hierarchical.properties.*;
 import de.tuilmenau.ics.fog.routing.hierarchical.properties.ClusterParticipationProperty.NestedParticipation;
 import de.tuilmenau.ics.fog.routing.naming.HierarchicalNameMappingService;
 import de.tuilmenau.ics.fog.routing.naming.NameMappingEntry;
 import de.tuilmenau.ics.fog.routing.naming.hierarchical.HRMID;
 import de.tuilmenau.ics.fog.routing.naming.hierarchical.HRMName;
 import de.tuilmenau.ics.fog.routing.naming.hierarchical.L2Address;
 import de.tuilmenau.ics.fog.topology.AutonomousSystem;
 import de.tuilmenau.ics.fog.topology.ILowerLayer;
 import de.tuilmenau.ics.fog.topology.NetworkInterface;
 import de.tuilmenau.ics.fog.topology.Node;
 import de.tuilmenau.ics.fog.transfer.ForwardingElement;
 import de.tuilmenau.ics.fog.transfer.TransferPlaneObserver.NamingLevel;
 import de.tuilmenau.ics.fog.transfer.forwardingNodes.GateContainer;
 import de.tuilmenau.ics.fog.transfer.gates.AbstractGate;
 import de.tuilmenau.ics.fog.transfer.gates.DirectDownGate;
 import de.tuilmenau.ics.fog.transfer.gates.GateID;
 import de.tuilmenau.ics.fog.ui.Logging;
 import de.tuilmenau.ics.fog.util.SimpleName;
 
 /**
  * This is the main HRM controller. It provides functions that are necessary to build up the hierarchical structure - every node contains such an object
  */
 public class HRMController extends Application implements IServerCallback, IEvent
 {
 	/**
 	 * The global name space which is used to identify the HRM instances on nodes.
 	 */
 	public final static Namespace ROUTING_NAMESPACE = new Namespace("routing");
 
 	/**
 	 * Stores the GUI observable, which is used to notify possible GUIs about changes within this HRMController instance.
 	 */
 	private HRMControllerObservable mGUIInformer = null;
 	
 	/**
 	 * The name under which the HRMController application is registered on the local node.
 	 */
 	private SimpleName mApplicationName = null;
 	
 	/**
 	 * Reference to physical node.
 	 */
 	private Node mNode;
 	
 	/**
 	 * Stores a reference to the local autonomous system instance.
 	 */
 	private AutonomousSystem mAS = null;
 	
 	/**
 	 * Stores the registered HRMIDs.
 	 * This is used within the GUI and during "share phase".
 	 */
 	private LinkedList<HRMID> mRegisteredOwnHRMIDs = new LinkedList<HRMID>();
 
 	/**
 	 * Stores a database about all registered coordinators.
 	 * For example, this list is used for the GUI.
 	 */
 	private LinkedList<Coordinator> mKnownCoordinators = new LinkedList<Coordinator>();
 
 	/**
 	 * Stores a database about all registered clusters.
 	 * For example, this list is used for the GUI.
 	 */
 	private LinkedList<Cluster> mKnownClusters = new LinkedList<Cluster>();
 
 	/**
 	 * Stores a reference to the local instance of the hierarchical routing service.
 	 */
 	private HRMRoutingService mHierarchicalRoutingService = null;
 	
 	/**
 	 * Stores if the application was already started.
 	 */
 	private boolean mApplicationStarted = false;
 	
 	/**
 	 * Stores a database including all HRMControllers of this physical simulation machine
 	 */
 	private static LinkedList<HRMController> mRegisteredHRMControllers = new LinkedList<HRMController>();
 	
 	/**
 	 * Stores an abstract routing graph (ARG), which provides an abstract overview about logical links between clusters/coordinator.
 	 */
 	private AbstractRoutingGraph<AbstractRoutingGraphNode, AbstractRoutingGraphLink> mAbstractRoutingGraph = new AbstractRoutingGraph<AbstractRoutingGraphNode, AbstractRoutingGraphLink>();
 	
 	/**
 	 * Count the outgoing connections
 	 */
 	private int mCounterOutgoingConnections = 0;
 
 	private HashMap<Integer, ICluster> mLevelToCluster = new HashMap<Integer, ICluster>();
 	private HashMap<ICluster, Cluster> mIntermediateMapping = new HashMap<ICluster, Cluster>();
 	private HashMap<Integer, ComChannelMuxer> mMuxOnLevel;
 	private LinkedList<LinkedList<Coordinator>> mRegisteredCoordinators = new LinkedList<LinkedList<Coordinator>>();
 	
 	/**
 	 * @param pAS the autonomous system at which this HRMController is instantiated
 	 * @param pNode the node on which this controller was started
 	 * @param pHRS is the hierarchical routing service that should be used
 	 */
 	public HRMController(AutonomousSystem pAS, Node pNode, HRMRoutingService pHierarchicalRoutingService)
 	{
 		// initialize the application context
 		super(pNode.getHost(), null, pNode.getIdentity());
 
 		// define the local name "routing://"
 		mApplicationName = new SimpleName(ROUTING_NAMESPACE, null);
 
 		// the observable, e.g., it is used to delegate update notifications to the GUI
 		mGUIInformer = new HRMControllerObservable(this);
 		
 		// reference to the physical node
 		mNode = pNode;
 		
 		// reference to the AutonomousSystem object 
 		mAS = pAS;
 		
 		/**
 		 * Create communication service
 		 */
 		// bind the HRMController application to a local socket
 		Binding tServerSocket=null;
 		// enable simple datagram based communication
 		Description tServiceReq = getDescription();
 		tServiceReq.set(CommunicationTypeProperty.DATAGRAM);
 		try {
 			tServerSocket = getHost().bind(null, mApplicationName, tServiceReq, getIdentity());
 		} catch (NetworkException tExc) {
 			Logging.err(this, "Unable to bind to hosts application interface", tExc);
 		}
 		// create and start the socket service
 		Service tService = new Service(false, this);
 		tService.start(tServerSocket);
 		
 		// store the reference to the local instance of hierarchical routing service
 		mHierarchicalRoutingService = pHierarchicalRoutingService;
 		
 		// set the Bully priority 
 		BullyPriority.configureNode(pNode);
 
 		
 		// fire the first "report/share phase" trigger
 		reportAndShare();
 		
 		Logging.log(this, "CREATED");
 		
 		// start the application
 		start();
 	}
 
 	/**
 	 * Returns the local instance of the hierarchical routing service
 	 * 
 	 * @return hierarchical routing service of this entity
 	 */
 	public HRMRoutingService getHRS()
 	{
 		return mHierarchicalRoutingService;
 	}
 	
 	/**
 	 * Returns the local physical node object.
 	 * 
 	 * @return the physical node running this coordinator
 	 */
 	public Node getNode()
 	{
 		return mNode;
 	}
 	
 	/**
 	 * Return the actual GUI name description of the physical node;
      * However, this function should only be used for debug outputs, e.g., GUI outputs.
      * 
 	 * @return the GUI name
 	 */
 	public String getNodeGUIName()
 	{
 		return mNode.getName();
 	}	
 
 	/**
 	 * Notifies the GUI about essential updates within the HRM system
 	 */
 	private void notifyGUI(Object pArgument)
 	{
 		if (HRMConfig.DebugOutput.GUI_SHOW_NOTIFICATIONS){
 			Logging.log(this, "Got notification with argument " + pArgument);
 		}
 		
 		//TODO: add a time period here to avoid multiple GUI updates in short time periods
 		
 		mGUIInformer.notifyObservers(pArgument);
 	}
 
 	/**
 	 * Registers a GUI for being notified about HRMController internal changes. 
 	 */
 	public void registerGUI(Observer pGUI)
 	{
 		if (HRMConfig.DebugOutput.GUI_SHOW_NOTIFICATIONS){
 			Logging.log(this, "Registering GUI " + pGUI);
 		}
 		mGUIInformer.addObserver(pGUI);
 	}
 	
 	/**
 	 * Unregisters a GUI for being notified about HRMController internal changes. 
 	 */
 	public void unregisterGUI(Observer pGUI)
 	{
 		if (HRMConfig.DebugOutput.GUI_SHOW_NOTIFICATIONS){
 			Logging.log(this, "Unregistering GUI " + pGUI);
 		}
 		mGUIInformer.deleteObserver(pGUI);
 	}
 
 	/**
 	 * Registers a coordinator at the local database.
 	 * 
 	 * @param pCoordinator the coordinator for a defined cluster
 	 */
 	public void registerCoordinator(Coordinator pCoordinator)
 	{
 		int tLevel = pCoordinator.getHierarchyLevel().getValue() - 1; //TODO: die Hierarchieebenen im Koordinator richtig verwalten 
 		
 		Logging.log(this, "Registering coordinator " + pCoordinator + " at level " + tLevel);
 
 		synchronized (mRegisteredCoordinators) {
 			if(mRegisteredCoordinators.size() <= tLevel) {
 				for(int i = mRegisteredCoordinators.size() - 1; i <= tLevel ; i++) {
 					mRegisteredCoordinators.add(new LinkedList<Coordinator>());
 				}
 			}
 			
 			if (mRegisteredCoordinators.get(tLevel).size() > 0){
 				Logging.log(this, "#### Got more than one coordinator at level " + tLevel + ", already known (0): " + mRegisteredCoordinators.get(tLevel).get(0) + ", new one: " + pCoordinator);
 			}
 		
 			// store the new coordinator
 			mRegisteredCoordinators.get(tLevel).add(pCoordinator);
 		}
 		
 		// register a route to the coordinator as addressable target
 		getHRS().addHRMRoute(RoutingEntry.createLocalhostEntry(pCoordinator.getHRMID()));
 		
 		//TODO: remove this
 		registerNodeARG(pCoordinator);
 
 		synchronized (mKnownCoordinators) {
 			// register as known coordinator
 			mKnownCoordinators.add(pCoordinator);
 		}
 		
 		// update GUI: image for node object 
 		//TODO: check and be aware of topology dynamics
 		getNode().setDecorationParameter("L"+ tLevel);
 		
 		// it's time to update the GUI
 		notifyGUI(pCoordinator);
 	}
 	
 	/**
 	 * Unregisters a coordinator from the internal database.
 	 * 
 	 * @param pCoordinator the coordinator which should be unregistered
 	 */
 	public void unregisterCoordinator(Coordinator pCoordinator)
 	{
 		Logging.log(this, "Unegistering coordinator " + pCoordinator);
 
 		synchronized (mKnownCoordinators) {
 			// unregister from list of known coordinators
 			mKnownCoordinators.remove(pCoordinator);
 		}
 
 		// it's time to update the GUI
 		notifyGUI(pCoordinator);
 	}
 	
 	/**
 	 * Updates the registered HRMID for a defined coordinator.
 	 * 
 	 * @param pCluster the cluster whose HRMID is updated
 	 */
 	@SuppressWarnings("unused")
 	public void updateCoordinatorAddress(Coordinator pCoordinator)
 	{
 		HRMID tHRMID = pCoordinator.getHRMID();
 		
 		Logging.log(this, "Updating address to " + tHRMID.toString() + " for coordinator " + pCoordinator);
 		
 		synchronized(mRegisteredOwnHRMIDs){
 			if ((!mRegisteredOwnHRMIDs.contains(tHRMID)) || (!HRMConfig.DebugOutput.GUI_AVOID_HRMID_DUPLICATES)){
 				
 				if (HRMConfig.DebugOutput.GUI_HRMID_UPDATES){
 					Logging.log(this, "Updating the HRMID to " + tHRMID.toString() + " for " + pCoordinator);
 				}
 	
 				/**
 				 * Update the local address DB with the given HRMID
 				 */
 				// register the new HRMID
 				mRegisteredOwnHRMIDs.add(tHRMID);
 	
 				/**
 				 * Register a local loopback route for the new address 
 				 */
 				// register a route to the cluster member as addressable target
 				getHRS().addHRMRoute(RoutingEntry.createLocalhostEntry(tHRMID));
 	
 				/**
 				 * Update the GUI
 				 */
 				// filter relative HRMIDs
 				if ((!tHRMID.isRelativeAddress()) || (HRMConfig.DebugOutput.GUI_SHOW_RELATIVE_ADDRESSES)){
 					// update node label within GUI
 					String tOldDeco = (getNode().getDecorationValue() != null ? getNode().getDecorationValue().toString() : "");
 					getNode().setDecorationValue(tOldDeco + ", " + tHRMID.toString());
 				}
 				// it's time to update the GUI
 				notifyGUI(pCoordinator);
 			}else{
 				Logging. warn(this, "Skipping HRMID duplicate, additional registration is triggered by " + pCoordinator);
 			}
 		}			
 	}
 
 	/**
 	 * Returns a list of known coordinators.
 	 * 
 	 * @return the list of known coordinators
 	 */
 	@SuppressWarnings("unchecked")
 	public LinkedList<Coordinator> getAllCoordinators()
 	{
 		LinkedList<Coordinator> tResult;
 		
 		synchronized (mKnownCoordinators) {
 			tResult = (LinkedList<Coordinator>) mKnownCoordinators.clone();
 		}
 		
 		return tResult;
 	}
 	
 	/**
 	 * Registers a cluster at the local database.
 	 * 
 	 * @param pCluster the cluster which should be registered
 	 */
 	public void registerCluster(Cluster pCluster)
 	{
 		int tLevel = pCluster.getHierarchyLevel().getValue();
 
 		Logging.log(this, "Registering cluster " + pCluster + " at level " + tLevel);
 
 		synchronized (mKnownClusters) {
 			// register as known cluster
 			mKnownClusters.add(pCluster);
 		}
 		
 		// it's time to update the GUI
 		notifyGUI(pCluster);
 	}
 	
 	/**
 	 * Unregisters a cluster from the local database.
 	 * 
 	 * @param pCluster the cluster which should be unregistered.
 	 */
 	public void unregisterCluster(Cluster pCluster)
 	{
 		Logging.log(this, "Unegistering coordinator " + pCluster);
 
 		synchronized (mKnownClusters) {
 			// unregister from list of known clusters
 			mKnownClusters.remove(pCluster);
 		}
 		
 		// it's time to update the GUI
 		notifyGUI(pCluster);
 	}
 	
 	/**
 	 * Updates the registered HRMID for a defined cluster.
 	 * 
 	 * @param pCluster the cluster whose HRMID is updated
 	 */
 	@SuppressWarnings("unused")
 	public void updateClusterAddress(Cluster pCluster)
 	{
 		HRMID tHRMID = pCluster.getHRMID();
 
 		Logging.log(this, "Updating address to " + tHRMID.toString() + " for cluster " + pCluster);
 
 		// process this only if we are at base hierarchy level, otherwise we will receive the same update from 
 		// the corresponding coordinator instance
 		if (pCluster.getHierarchyLevel().isBaseLevel()){
 			synchronized(mRegisteredOwnHRMIDs){
 				if ((!mRegisteredOwnHRMIDs.contains(tHRMID)) || (!HRMConfig.DebugOutput.GUI_AVOID_HRMID_DUPLICATES)){
 					
 					/**
 					 * Update the local address DB with the given HRMID
 					 */
 					if (HRMConfig.DebugOutput.GUI_HRMID_UPDATES){
 						Logging.log(this, "Updating the HRMID to " + pCluster.getHRMID().toString() + " for " + pCluster);
 					}
 					
 					// register the new HRMID
 					mRegisteredOwnHRMIDs.add(tHRMID);
 					
 					/**
 					 * Register a local loopback route for the new address 
 					 */
 					// register a route to the cluster member as addressable target
 					getHRS().addHRMRoute(RoutingEntry.createLocalhostEntry(tHRMID));
 	
 					/**
 					 * We are at base hierarchy level! Thus, the new HRMID is an address for this physical node and has to be
 					 * registered in the DNS as address for the name of this node. 
 					 */
 					// register the HRMID in the hierarchical DNS for the local router
 					HierarchicalNameMappingService<HRMID> tNMS = null;
 					try {
 						tNMS = (HierarchicalNameMappingService) HierarchicalNameMappingService.getGlobalNameMappingService();
 					} catch (RuntimeException tExc) {
 						HierarchicalNameMappingService.createGlobalNameMappingService(getNode().getAS().getSimulation());
 					}				
 					// get the local router's human readable name (= DNS name)
 					Name tLocalRouterName = getNodeName();				
 					// register HRMID for the given DNS name
 					tNMS.registerName(tLocalRouterName, tHRMID, NamingLevel.NAMES);				
 					// give some debug output about the current DNS state
 					String tString = new String();
 					for(NameMappingEntry<HRMID> tEntry : tNMS.getAddresses(tLocalRouterName)) {
 						if (!tString.isEmpty()){
 							tString += ", ";
 						}
 						tString += tEntry;
 					}
 					Logging.log(this, "HRM router " + tLocalRouterName + " is now known under: " + tString);
 					
 					/**
 					 * Update the GUI
 					 */
 					// filter relative HRMIDs
 					if ((!tHRMID.isRelativeAddress()) || (HRMConfig.DebugOutput.GUI_SHOW_RELATIVE_ADDRESSES)){
 						// update node label within GUI
 						String tOldDeco = (getNode().getDecorationValue() != null ? getNode().getDecorationValue().toString() : "");
 						getNode().setDecorationValue(tOldDeco + ", " + tHRMID.toString());
 					}
 					// it's time to update the GUI
 					notifyGUI(pCluster);
 				}else{
 					Logging.warn(this, "Skipping HRMID duplicate, additional registration is triggered by " + pCluster);
 				}
 			}
 		}else{
 			// we are at a higher hierarchy level and don't need the HRMID update because we got the same from the corresponding coordinator instance
 			Logging.warn(this, "Skipping HRMID registration " + tHRMID.toString() + " for " + pCluster);
 		}
 	}
 
 	/**
 	 * Returns a list of known clusters.
 	 * 
 	 * @return the list of known clusters
 	 */
 	public LinkedList<Cluster> getAllClusters()
 	{
 		LinkedList<Cluster> tResult = null;
 		
 		synchronized (mKnownClusters) {
 			tResult = (LinkedList<Cluster>) mKnownClusters.clone();
 		}
 		
 		return tResult;
 	}
 
 	/**
 	 * Returns the list of registered own HRMIDs which can be used to address the physical node on which this instance is running.
 	 *  
 	 * @return the list of HRMIDs
 	 */
 	@SuppressWarnings("unchecked")
 	public LinkedList<HRMID> getOwnHRMIDs()
 	{
 		LinkedList<HRMID> tResult = null;
 		
 		synchronized(mRegisteredOwnHRMIDs){
 			tResult = (LinkedList<HRMID>) mRegisteredOwnHRMIDs.clone();
 		}
 		
 		return tResult;
 	}
 	
 	/**
 	 * Adds an entry to the routing table of the local HRS instance.
 	 * In opposite to addHRMRoute() from the HierarchicalRoutingService class, this function additionally updates the GUI.
 	 * If the L2 address of the next hop is defined, the HRS will update the HRMID-to-L2ADDRESS mapping.
 	 * 
 	 * @param pRoutingEntry the new routing entry
 	 */
 	public void addHRMRoute(RoutingEntry pRoutingEntry)
 	{
 		// inform the HRS about the new route
 		if(getHRS().addHRMRoute(pRoutingEntry)){
 			// it's time to update the GUI
 			notifyGUI(this);
 		}
 	}
 
 	/**
 	 * Adds a route to the local HRM routing table.
 	 * This function doesn't send GUI update notifications. For this purpose, the HRMController instance has to be used.
 	 * 
 	 * @param pNeighborL2Address the L2Address of the direct neighbor
 	 * @param pRoute the route to the direct neighbor
 	 */
 	public void addRouteToDirectNeighbor(L2Address pNeighborL2Address, Route pRoute)
 	{
 		// inform the HRS about the new route
 		if(getHRS().addRouteToDirectNeighbor(pNeighborL2Address, pRoute)){
 			// it's time to update the GUI
 			notifyGUI(this);
 		}
 	}
 
 	/**
 	 * Reacts on a detected new physical neighbor. A new connection to this neighbor is created.
 	 * However, pNeighborL2Address doesn't correspond to the neighbor's central FN!.
 	 * 
 	 * @param pNeighborL2Address the L2 address of the detected physical neighbor
 	 */
 	public void detectedPhysicalNeighborNode(L2Address pNeighborL2Address)
 	{
 		L2Address tThisHostL2Address = getHRS().getL2AddressFor(mNode.getCentralFN());
 
 		Logging.info(this, "NODE " + tThisHostL2Address + " FOUND DIRECT NEIGHBOR: " + pNeighborL2Address);
 		
 		// determine the route to the neighbor
 		List<RoutingServiceLink> tGateIDsToNeighbor = getHRS().getGateIDsToL2Address(pNeighborL2Address);
 		Logging.log(this, "    ..determined route from " + tThisHostL2Address + " to " + pNeighborL2Address + ": " + tGateIDsToNeighbor);
 		if(tGateIDsToNeighbor != null) {
 			// determine the transparent gate towards the neighbor
 			AbstractGate tOutgoingTransparentGate = null;
 			try {
 				tOutgoingTransparentGate = mNode.getCentralFN().getGate(tGateIDsToNeighbor.get(0).getID());
 			} catch (IndexOutOfBoundsException tExc) {
 				Logging.err(this, "registerLink() couldn't determine the outgoing gate for a connection from " + tThisHostL2Address + " to " + pNeighborL2Address + ", determined route is: " + tGateIDsToNeighbor, tExc);
 			}
 			if(tOutgoingTransparentGate != null) {
 				// determine the first next node behind the outgoing transparent gate
 				ForwardingElement tFirstNextNode = tOutgoingTransparentGate.getNextNode();
 				
 				// get the gate container from the first next node
 				GateContainer tContainer = (GateContainer) tFirstNextNode;
 				
 				// get the DirectDownGate ID
 				RoutingServiceLink tDownGateLink = tGateIDsToNeighbor.get(1); 
 				
 				if (tDownGateLink != null){
 					GateID tDownGateGateID = tDownGateLink.getID();
 				
 					// hash the name of the bus where the outgoing gate belongs to in order to create a temporary identification of the cluster
 					Long tClusterID = Long.valueOf(0L);
 					// get the DirectDownGate to the neighbor node
 					DirectDownGate tDirectDownGate = (DirectDownGate) tContainer.getGate(tDownGateGateID);
 					if (tDirectDownGate != null){
 						NetworkInterface tNetworkInterface = tDirectDownGate.getLowerLayer();
 						if (tNetworkInterface != null){
 							ILowerLayer tLowerLayer = tNetworkInterface.getBus();
 							if (tLowerLayer != null){
 								String tBusName = null;
 								try {
 									tBusName = tLowerLayer.getName();
 								} catch (RemoteException tExc) {
 									Logging.err(this, "registerLink() wasn't able to determine a hash value of the bus (" + tNetworkInterface.getBus() + "), Bus Name is invalid", tExc);
 									tBusName = null;
 								}
 								if (tBusName != null){
 									tClusterID = Long.valueOf(tBusName.hashCode());
 								}else{
 									Logging.err(this, "registerLink() wasn't able to determine a hash value of the bus (" + tNetworkInterface.getBus() + "), Bus Name is invalid");
 								}
 							}else{
 								Logging.err(this, "registerLink() wasn't able to determine a hash value of the bus (" + tNetworkInterface.getBus() + "), ILowerLayer is invalid");
 							}
 						}else{
 							Logging.err(this, "registerLink() wasn't able to determine a hash value of the bus to " + pNeighborL2Address + ", NetworkInterface is invalid");
 						}									
 					}else{
 						Logging.err(this, "registerLink() wasn't able to determine a hash value of the bus " + pNeighborL2Address + ", DirectDownGate is invalid");
 					}
 
 					/**
 					 * Open a connection to the neighbor
 					 */
 				    Logging.log(this, "    ..opening connection to " + pNeighborL2Address);
 				    connectToNeighborNode(pNeighborL2Address, HierarchyLevel.createBaseLevel(), tClusterID);
 				}else{
 					Logging.err(this, "registerLink() hasn't found the DirectDownGate in the route from " + tThisHostL2Address + " to " + pNeighborL2Address + ", route is: " + tGateIDsToNeighbor);
 				}
 					
 			}else{
 				Logging.err(this, "registerLink() couldn't determine the outgoing gate of the route from " + tThisHostL2Address + " to " + pNeighborL2Address + ", route is: " + tGateIDsToNeighbor);
 			}
 			
 		}else{
 			Logging.err(this, "registerLink() couldn't determine a route from " + tThisHostL2Address + " to " + pNeighborL2Address);
 		}
 	}
 	
 	/**
 	 * Determines a reference to the current AutonomousSystem instance.
 	 * 
 	 * @return the desired reference
 	 */
 	public AutonomousSystem getAS()
 	{
 		return mAS;
 	}
 	
 	/**
 	 * Determines the current simulation time
 	 * 
 	 * @return the simulation time
 	 */
 	public double getSimulationTime()
 	{
 		return mAS.getTimeBase().now();
 	}
 	
 	/* (non-Javadoc)
 	 * @see de.tuilmenau.ics.fog.IEvent#fire()
 	 */
 	@Override
 	public void fire()
 	{
 		reportAndShare();
 	}
 
 	/**
 	 * Triggers the "report phase" / "share phase" of all known coordinators
 	 */
 	private void reportAndShare()
 	{	
 		if (HRMConfig.DebugOutput.GUI_SHOW_TIMING_ROUTE_DISTRIBUTION){
 			Logging.log(this, "REPORT AND SHARE TRIGGER received");
 		}
 
 		/**
 		 * report phase
 		 */
 		for (Coordinator tCoordinator : mKnownCoordinators) {
 			tCoordinator.reportPhase();
 		}
 		
 		/**
 		 * share phase
 		 */
 		for (Coordinator tCoordinator : mKnownCoordinators) {
 			tCoordinator.sharePhase();
 		}
 		
 		/**
 		 * register next trigger
 		 */
 		mAS.getTimeBase().scheduleIn(HRMConfig.Routing.GRANULARITY_SHARE_PHASE, this);
 	}
 	
 	/**
 	 * Calculate the time period between "share phases" 
 	 *  
 	 * @param pHierarchyLevel the hierarchy level 
 	 * @return the calculated time period
 	 */
 	public double getPeriodSharePhase(int pHierarchyLevelValue)
 	{
 		return (double) 2 * HRMConfig.Routing.GRANULARITY_SHARE_PHASE * pHierarchyLevelValue; //TODO: use an exponential time distribution here
 	}
 	
 	/**
 	 * Calculate the time period between "share phases" 
 	 *  
 	 * @param pHierarchyLevel the hierarchy level 
 	 * @return the calculated time period
 	 */
 	public double getPeriodReportPhase(HierarchyLevel pHierarchyLevel)
 	{
 		return (double) HRMConfig.Routing.GRANULARITY_SHARE_PHASE * (pHierarchyLevel.getValue() - 1); //TODO: use an exponential time distribution here
 	}
 	
 	/**
 	 * This method is derived from IServerCallback. It is called by the ServerFN in order to acquire the acknowledgment from the HRMController about the incoming connection
 	 * 
 	 * @param pAuths the authentications of the requesting sender
 	 * @param pRequirements the requirements for the incoming connection
 	 * @param pTargetName the registered name of the addressed target service
 	 * @return true of false
 	 */
 	@Override
 	public boolean openAck(LinkedList<Signature> pAuths, Description pRequirements, Name pTargetName)
 	{
 		if (HRMConfig.DebugOutput.GUI_SHOW_TOPOLOGY_DETECTION){
 			Logging.log(this, "Incoming request for acknowledging the connection:");
 			Logging.log(this, "    ..source: " + pAuths);
 			Logging.log(this, "    ..destination: " + pTargetName);
 			Logging.log(this, "    ..requirements: " + pRequirements);
 		}
 		
 		return true;
 	}
 	
 	/**
 	 * Helper function to get the local machine's host name.
 	 * The output of this function is useful for distributed simulations if coordinators/clusters with the name might coexist on different machines.
 	 * 
 	 * @return the host name
 	 */
 	public static String getHostName()
 	{
 		String tResult = null;
 		
 		try{	
 			tResult = java.net.InetAddress.getLocalHost().getHostName();
 		} catch (UnknownHostException tExc) {
 			Logging.err(null, "Unable to determine the local host name", tExc);
 		}
 		
 		return tResult;
 	}
 	
 	/**
 	 * Determines the L2Address of the first FN towards a neighbor. This corresponds to the FN, located between the central FN and the bus to the neighbor node.
 	 * 
 	 * @param pNeighborName the name of the neighbor
 	 * @return the L2Address of the search FN
 	 */
 	public L2Address getL2AddressOfFirstFNTowardsNeighbor(Name pNeighborName)
 	{
 		L2Address tResult = null;
 
 		Route tRoute = null;
 		// get the name of the central FN
 		L2Address tCentralFNL2Address = getHRS().getCentralFNL2Address();
 		// get a route to the neighbor node (the destination of the desired connection)
 		try {
 			tRoute = getHRS().getRoute(pNeighborName, new Description(), getNode().getIdentity());
 		} catch (RoutingException tExc) {
 			Logging.err(this, "getL2AddressOfFirstFNTowardsNeighbor() is unable to find route to " + pNeighborName, tExc);
 		} catch (RequirementsException tExc) {
 			Logging.err(this, "getL2AddressOfFirstFNTowardsNeighbor() is unable to find route to " + pNeighborName + " with requirements no requirents, Huh!", tExc);
 		}
 		// have we found a route to the neighbor?
 		if(tRoute != null) {
 			// get the first route part, which corresponds to the link between the central FN and the searched first FN towards the neighbor 
 			RouteSegmentPath tPath = (RouteSegmentPath) tRoute.getFirst();
 			// get the gate ID of the link
 			GateID tGateID= tPath.getFirst();						
 			// get all outgoing links from the central FN
 			Collection<RoutingServiceLink> tOutgoingLinksFromCentralFN = getHRS().getOutgoingLinks(tCentralFNL2Address);
 			
 			RoutingServiceLink tLinkBetweenCentralFNAndFirstNodeTowardsNeighbor = null;
 
 			// iterate over all outgoing links and search for the link from the central FN to the FN, which comes first when routing towards the neighbor
 			for(RoutingServiceLink tLink : tOutgoingLinksFromCentralFN) {
 				// compare the GateIDs
 				if(tLink.equals(tGateID)) {
 					// found!
 					tLinkBetweenCentralFNAndFirstNodeTowardsNeighbor = tLink;
 				}
 			}
 			// determine the searched FN, which comes first when routing towards the neighbor
 			HRMName tFirstNodeBeforeBusToNeighbor = getHRS().getLinkDestination(tLinkBetweenCentralFNAndFirstNodeTowardsNeighbor);
 			if (tFirstNodeBeforeBusToNeighbor instanceof L2Address){
 				// get the L2 address
 				tResult = (L2Address)tFirstNodeBeforeBusToNeighbor;
 			}else{
 				Logging.err(this,  "getL2AddressOfFirstFNTowardsNeighbor() found a first FN (" + tFirstNodeBeforeBusToNeighbor + ") towards the neighbor " + pNeighborName + " but it has the wrong class type");
 			}
 		}
 		
 		return tResult;
 	}
 
 	/**
 	 * This function gets called if the HRMController appl. was started
 	 */
 	@Override
 	protected void started() 
 	{
 		mApplicationStarted = true;
 		
 		// register in the global HRMController database
 		synchronized (mRegisteredHRMControllers) {
 			mRegisteredHRMControllers.add(this);
 		}
 	}
 	
 	/**
 	 * This function gets called if the HRMController appl. should exit/terminate right now
 	 */
 	@Override
 	public void exit() 
 	{
 		Logging.warn(this, "Exit the HRMController application isn't supported");
 	}
 
 	/**
 	 * Return if the HRMController application is running
 	 * 
 	 * @return true if the HRMController application is running, otherwise false
 	 */
 	@Override
 	public boolean isRunning() 
 	{
 		return mApplicationStarted;
 	}
 
 	/**
 	 * Returns the list of known HRMController instances for this physical simulation machine
 	 *  
 	 * @return the list of HRMController references
 	 */
 	@SuppressWarnings("unchecked")
 	public static LinkedList<HRMController> getALLHRMControllers()
 	{
 		LinkedList<HRMController> tResult = null;
 		
 		synchronized (mRegisteredHRMControllers) {
 			tResult = (LinkedList<HRMController>) mRegisteredHRMControllers.clone();
 		}
 		
 		return tResult;
 	}
 	
 	/**
 	 * Creates a Description, which directs a connection to another HRMController instance
 
 	 * @return the new description
 	 */
 	public Description createHRMControllerDestinationDescription()
 	{
 		Description tResult = new Description();
 		
 		if (HRMConfig.DebugOutput.GUI_SHOW_TOPOLOGY_DETECTION){
 			Logging.log(this, "Creating a HRMController destination description");
 		}
 
 		tResult.set(new DestinationApplicationProperty(HRMController.ROUTING_NAMESPACE, null, null));
 		
 		return tResult;
 	}
 
 	/**
 	 * Registers a cluster/coordinator to the locally stored abstract routing graph (ARG)
 	 *  
 	 * @param pNode the node (cluster/coordinator) which should be stored in the ARG
 	 */
 	public synchronized void registerNodeARG(ICluster pNode)
 	{
 		if (HRMConfig.DebugOutput.GUI_SHOW_TOPOLOGY_DETECTION){
 			Logging.log(this, "REGISTERING NODE ADDRESS (ARG): " + pNode );
 		}
 
 		synchronized (mAbstractRoutingGraph) {
 			if(!mAbstractRoutingGraph.contains(pNode)) {
 				mAbstractRoutingGraph.add(pNode);
 				if (HRMConfig.DebugOutput.GUI_SHOW_TOPOLOGY_DETECTION){
 					Logging.log(this, "     ..added to ARG");
 				}
 			}else{
 				if (HRMConfig.DebugOutput.GUI_SHOW_TOPOLOGY_DETECTION){
 					Logging.log(this, "     ..node for ARG already known: " + pNode);
 				}
 			}
 		}
 	}
 
 	/**
 	 * Registers a logical link between clusters/coordinators to the locally stored abstract routing graph (ARG)
 	 * 
 	 * @param pFrom the starting point of the link
 	 * @param pTo the ending point of the link
 	 * @param pLink the link between the two nodes
 	 */
 	public void registerLinkARG(ICluster pFrom, ICluster pTo, AbstractRoutingGraphLink pLink)
 	{
 		if (HRMConfig.DebugOutput.GUI_SHOW_TOPOLOGY_DETECTION){
 			Logging.log(this, "REGISTERING LINK (ARG):  source=" + pFrom + " ## dest.=" + pTo + " ## link=" + pLink);
 		}
 
 		synchronized (mAbstractRoutingGraph) {
 			mAbstractRoutingGraph.storeLink(pFrom, pTo, pLink);
 		}
 	}
 
 	/**
 	 * Determines a route in the locally stored abstract routing graph (ARG).
 	 * 
 	 * @param pSource the source of the desired route
 	 * @param pDestination the destination of the desired route
 	 * 
 	 * @return the determined route, null if no route could be found
 	 */
 	public List<AbstractRoutingGraphLink> getRouteARG(AbstractRoutingGraphNode pSource, AbstractRoutingGraphNode pDestination)
 	{
 		List<AbstractRoutingGraphLink> tResult = null;
 		
 		if (HRMConfig.DebugOutput.GUI_SHOW_ROUTING){
 			Logging.log(this, "GET ROUTE (ARG) from \"" + pSource + "\" to \"" + pDestination +"\"");
 		}
 
 		synchronized (mAbstractRoutingGraph) {
 			tResult = mAbstractRoutingGraph.getRoute(pSource, pDestination);
 		}
 
 		if (HRMConfig.DebugOutput.GUI_SHOW_ROUTING){
 			Logging.log(this, "        ..result: " + tResult);
 		}
 		
 		return tResult;
 	}
 
 	/**
 	 * Determines the other end of a link and one known link end
 	 * 
 	 * @param pKnownEnd the known end of the link
 	 * @param pLink the link
 	 * 
 	 * @return the other end of the link
 	 */
 	public AbstractRoutingGraphNode getOtherEndOfLinkARG(AbstractRoutingGraphNode pKnownEnd, AbstractRoutingGraphLink pLink)
 	{
 		AbstractRoutingGraphNode tResult = null;
 		
 		if (HRMConfig.DebugOutput.GUI_SHOW_ROUTING){
 			Logging.log(this, "GET OTHER END (ARG) of link \"" + pKnownEnd + "\" connected at \"" + pKnownEnd +"\"");
 		}
 
 		synchronized (mAbstractRoutingGraph) {
 			tResult = (ICluster) mAbstractRoutingGraph.getOtherEndOfLink(pKnownEnd, pLink);
 		}
 		
 		if (HRMConfig.DebugOutput.GUI_SHOW_ROUTING){
 			Logging.log(this, "        ..result: " + tResult);
 		}
 
 		return tResult;
 	}
 
 	/**
 	 * Determines all known neighbors of a cluster/coordinator, which are stored in the local abstract routing graph (ARG).
 	 * 
 	 * @param pRoot the root node in the ARG
 	 * 
 	 * @return a collection of found neighbor nodes
 	 */
 	public Collection<AbstractRoutingGraphNode> getNeighborsARG(AbstractRoutingGraphNode pRoot)
 	{
 		Collection<AbstractRoutingGraphNode> tResult = null;
 		
 		if (HRMConfig.DebugOutput.GUI_SHOW_ROUTING){
 			Logging.log(this, "GET NEIGHBORS (ARG) from \"" + pRoot + "\"");
 		}
 
 		synchronized (mAbstractRoutingGraph) {
 			tResult = mAbstractRoutingGraph.getNeighbors(pRoot);
 		}
 		
 		if (HRMConfig.DebugOutput.GUI_SHOW_ROUTING){
 			Logging.log(this, "      ..result: " + tResult.size() + " entries:");				
 			int i = 0;
 			for (AbstractRoutingGraphNode tName : tResult){
 				Logging.log(this, "      ..[" + i + "]: " + tName);
 				i++;
 			}			
 		}
 
 		return tResult;
 	}
 
 	/**
 	 * Determines all vertices ordered by their distance from a given root vertex
 	 * 
 	 * @param pRootVertex the root vertex from where the distances are calculated
 	 * 
 	 * @return a list of found vertices
 	 */
 	public List<AbstractRoutingGraphNode> getVerticesInOrderRadiusARG(AbstractRoutingGraphNode pRootVertex)
 	{
 		List<AbstractRoutingGraphNode> tResult = null;
 		
 		if (HRMConfig.DebugOutput.GUI_SHOW_ROUTING){
 			Logging.log(this, "GET VERTICES ORDER RADIUS (ARG) from \"" + pRootVertex + "\"");
 		}
 
 		synchronized (mAbstractRoutingGraph) {
 			tResult = mAbstractRoutingGraph.getVerticesInOrderRadius(pRootVertex);
 		}
 		
 		if (HRMConfig.DebugOutput.GUI_SHOW_ROUTING){
 			Logging.log(this, "      ..result: " + tResult.size() + " entries:");				
 			int i = 0;
 			for (AbstractRoutingGraphNode tName : tResult){
 				Logging.log(this, "      ..[" + i + "]: " + tName);
 				i++;
 			}			
 		}
 
 		return tResult;
 	}
 
 	/**
 	 * Checks if two nodes in the locally stored abstract routing graph are linked.
 	 * 
 	 * @param pFirst first node
 	 * @param pSecond second node
 	 * 
 	 * @return true or false
 	 */
 	public boolean isLinkedARG(ClusterName pFirst, ClusterName pSecond)
 	{
 		boolean tResult = false;
 		
 		if (HRMConfig.DebugOutput.GUI_SHOW_ROUTING){
 			Logging.log(this, "IS LINK (ARG) from \"" + pFirst + "\" to \"" + pSecond +"\"");
 		}
 
 		synchronized (mAbstractRoutingGraph) {
 			tResult = mAbstractRoutingGraph.isLinked(pFirst, pSecond);
 		}
 		
 		if (HRMConfig.DebugOutput.GUI_SHOW_ROUTING){
 			Logging.log(this, "      ..result: " + tResult);				
 		}
 
 		return tResult;
 	}
 
 	/**
 	 * Returns the ARG for the GraphViewer.
 	 * (only for GUI!)
 	 * 
 	 * @return the ARG
 	 */
 	public AbstractRoutingGraph<AbstractRoutingGraphNode, AbstractRoutingGraphLink> getARGForGraphViewer()
 	{
 		AbstractRoutingGraph<AbstractRoutingGraphNode, AbstractRoutingGraphLink> tResult = null;
 		
 		synchronized (mAbstractRoutingGraph) {
 			tResult = mAbstractRoutingGraph; //TODO: use a new clone() method here
 		}
 		
 		return tResult;
 	}
 
 	
 	
 	
 	
 	
 	
 	/** 
 	 * This method is derived from IServerCallback and is called for incoming connection requests by the HRMController application's ServerFN.
 	 * 
 	 * @param pConnection the incoming connection
 	 */
 	@Override
 	public void newConnection(Connection pConnection)
 	{
 		Logging.log(this, "INCOMING CONNECTION " + pConnection.toString() + " with requirements: " + pConnection.getRequirements());
 
 		// get the connection requirements
 		Description tConReqs = pConnection.getRequirements();
 
 		/**
 		 * Check if the new connection is a probe-routing connection
 		 */
 		boolean tProbeRoutingConnection = false;
 		ProbeRoutingProperty tPropProbeRouting = (ProbeRoutingProperty) tConReqs.get(ProbeRoutingProperty.class);
 		if (tPropProbeRouting != null){
 			// get the recorded route from the property
 			LinkedList<HRMID> tRecordedHRMIDs = tPropProbeRouting.getRecordedHops();
 			
 			Logging.log(this, "       ..detected a probe-routing connection(source=" + tPropProbeRouting.getSourceDescription() + " with " + tRecordedHRMIDs.size() + " recorded hops");
 
 			// print the recorded route
 			int i = 0;
 			for(HRMID tHRMID : tRecordedHRMIDs){
 				Logging.log(this, "            [" + i + "]: " + tHRMID);
 				i++;
 			}
 
 			// mark the incoming connection as probe-packet connection
 			tProbeRoutingConnection = true;
 		}
 
 		// do we have a probe-packet connection?
 		if (!tProbeRoutingConnection){
 			//long tClusterID = 0;
 			ComSession tConnectionSession = null;
 			
 			ClusterParticipationProperty tJoin = (ClusterParticipationProperty) tConReqs.get(ClusterParticipationProperty.class);
 			if(tJoin != null) {
 				Logging.log(this, "Found ClusterParticipationProperty " + tJoin);
 			}
 			
 			Logging.log(this, "Nested participations: " + tJoin.getNestedParticipations());
 			
			for(NestedParticipation tParticipation : tJoin.getNestedParticipations()) {
 				Logging.log(this, "Iterate over nested participations");
 	
 				ComChannel tCEP = null;
 				boolean tClusterFound = false;
 				ICluster tFoundCluster = null;
 				
 				LinkedList<Cluster> tKnownClusters = getAllClusters();
 				
 				Logging.log(this, "Searching for target cluster among " + tKnownClusters.size() + " known clusters:");
 				
 				int i = 0;
 				for(Cluster tCluster : tKnownClusters)
 				{
 					Logging.log(this, "       ..[" + i + "]: " + tCluster);
 					
 					ClusterName tJoinClusterName = new ClusterName(tJoin.getTargetToken(), tJoin.getTargetClusterID(), tJoin.getHierarchyLevel());
 					ClusterName tJoinClusterNameTok0 = new ClusterName(0, tJoin.getTargetClusterID(), tJoin.getHierarchyLevel());
 					
 					if(tCluster.equals(tJoinClusterNameTok0) || tJoin.getTargetToken() != 0 && tCluster.equals(tJoinClusterName))	{
 						Logging.log(this, "Cluster found: " + tCluster);
 						
 						if(tConnectionSession == null) {
 							tConnectionSession = new ComSession(this, true, tJoin.getHierarchyLevel(), tCluster.getMultiplexer());
 						}
 						
 						tCEP = new ComChannel(this, tCluster);
 						((Cluster)tCluster).getMultiplexer().mapChannelToSession(tCEP, tConnectionSession);
 						if(tJoin.getHierarchyLevel().isHigherLevel()) {
							((Cluster)tCluster).getMultiplexer().registerDemultiplex(tParticipation.getSourceClusterID(), tJoin.getTargetClusterID(), tCEP);
 						}
 						tClusterFound = true;
 						tFoundCluster = tCluster;
 					}
 					i++;
 				}
 				
 				if(!tClusterFound)
 				{
 					Logging.log(this, "Cluster not found");
 	
 					Cluster tCluster = new Cluster(this, new Long(tJoin.getTargetClusterID()), tJoin.getHierarchyLevel());
 					setSourceIntermediateCluster(tCluster, tCluster);
 					if(tConnectionSession == null) {
 						tConnectionSession = new ComSession(this, true, tJoin.getHierarchyLevel(), tCluster.getMultiplexer());
 					}
 	
 					if(tJoin.getHierarchyLevel().isHigherLevel()) {
 						for(Cluster tCluster2 : getAllClusters()) {
 							if(tCluster2.getHierarchyLevel().getValue() == tJoin.getHierarchyLevel().getValue() - 1) {
 								tCluster.setPriority(tCluster2.getPriority());
 							}
 						}
 					}
 					tCEP = new ComChannel(this, tCluster);
 					if(tJoin.getHierarchyLevel().isHigherLevel()) {
						((Cluster)tCluster).getMultiplexer().registerDemultiplex(tParticipation.getSourceClusterID(), tJoin.getTargetClusterID(), tCEP);
 					}
 					tCluster.getMultiplexer().mapChannelToSession(tCEP, tConnectionSession);
 					registerNodeARG(tCluster);
 					tFoundCluster = tCluster;
 				}
 				tFoundCluster.getMultiplexer().mapChannelToSession(tCEP, tConnectionSession);
 				for(ICluster tNegotiatingCluster : getAllClusters()) {
					ClusterName tNegClusterName = new ClusterName(tParticipation.getSourceToken(), tParticipation.getSourceClusterID(), new HierarchyLevel(this, tJoin.getHierarchyLevel().getValue() - 1 > HRMConfig.Hierarchy.BASE_LEVEL ? tJoin.getHierarchyLevel().getValue() - 1 : 0 ));
 					if(tNegotiatingCluster.equals(tNegClusterName)) {
 						tCEP.setRemoteClusterName(tNegClusterName);
 					}
 				}
 				if(tCEP.getRemoteClusterName() == null && tJoin.getHierarchyLevel().isHigherLevel()) {
 					HashMap<ICluster, ClusterName> tNewlyCreatedClusters = new HashMap<ICluster, ClusterName>(); 
					NeighborCluster tAttachedCluster = new NeighborCluster(tParticipation.getSourceClusterID(), tParticipation.getSourceName(), tParticipation.getSourceAddress(), tParticipation.getSourceToken(), new HierarchyLevel(this, tJoin.getHierarchyLevel().getValue() - 1), this);
					tAttachedCluster.setPriority(tParticipation.getSenderPriority());
 					if(tAttachedCluster.getCoordinatorName() != null) {
 						try {
 							getHRS().mapFoGNameToL2Address(tAttachedCluster.getCoordinatorName(), tAttachedCluster.getCoordinatorsAddress());
 						} catch (RemoteException tExc) {
 							Logging.err(this, "Unable to fulfill requirements", tExc);
 						}
 					}
					tNewlyCreatedClusters.put(tAttachedCluster, tParticipation.getPredecessor());
 					Logging.log(this, "as joining cluster");
 					for(Cluster tCluster : getAllClusters()) {
 						if(tCluster.getHierarchyLevel().equals(tAttachedCluster.getHierarchyLevel())) {
 							setSourceIntermediateCluster(tAttachedCluster, tCluster);
 						}
 					}
 					if(getSourceIntermediate(tAttachedCluster) == null) {
 						Logging.err(this, "No source intermediate cluster for" + tAttachedCluster.getClusterDescription() + " found");
 					}
 					
 					Logging.log(this, "Created " + tAttachedCluster);
 					
 					tCEP.setRemoteClusterName(new ClusterName(tAttachedCluster.getToken(), tAttachedCluster.getClusterID(), tAttachedCluster.getHierarchyLevel()));
 					tAttachedCluster.addAnnouncedCEP(tCEP);
 					registerNodeARG(tAttachedCluster);
					if(tParticipation.getNeighbors() != null && !tParticipation.getNeighbors().isEmpty()) {
						Logging.log(this, "Working on neighbors " + tParticipation.getNeighbors());
						for(DiscoveryEntry tEntry : tParticipation.getNeighbors()) {
 							
 							/**
 							 * Create a ClusterName object from this entry
 							 */
 							ClusterName tEntryClusterName = new ClusterName(tEntry.getToken(), tEntry.getClusterID(), tEntry.getLevel());
 							
 							
 							/**
 							 * Search if the cluster is already locally known
 							 */
 							ICluster tCluster = null;
 							for(Cluster tKnownCluster : getAllClusters()) {
 								if(tKnownCluster.equals(tEntryClusterName)) {
 									tCluster = tKnownCluster;
 								}
 							}
 
 							if(tEntry.getRoutingVectors()!= null) {
 								for(RoutingServiceLinkVector tVector : tEntry.getRoutingVectors())
 								getHRS().registerRoute(tVector.getSource(), tVector.getDestination(), tVector.getPath());
 							}
 							
 							
 							// was the cluster already known?
 							if(tCluster == null) {
 								tCluster = new NeighborCluster(tEntry.getClusterID(), tEntry.getCoordinatorName(), tEntry.getCoordinatorRoutingAddress(),  tEntry.getToken(), tEntry.getLevel(), this);
 								tCluster.setPriority(tEntry.getPriority());
 								try {
 									getHRS().mapFoGNameToL2Address(tCluster.getCoordinatorName(), (L2Address)((NeighborCluster)tCluster).getCoordinatorsAddress());
 								} catch (RemoteException tExc) {
 									Logging.err(this, "Unable to fulfill requirements", tExc);
 								}
 								
 								tNewlyCreatedClusters.put(tCluster, tEntry.getPredecessor());
 								for(Cluster tCluster1 : getAllClusters()) {
 									if(tCluster1.getHierarchyLevel() == tCluster.getHierarchyLevel()) {
 										setSourceIntermediateCluster(tCluster, tCluster1);
 										Logging.log(this, "as joining neighbor");
 									}
 								}
 								if(getSourceIntermediate(tAttachedCluster) == null) {
 									Logging.err(this, "No source intermediate cluster for" + tCluster.getClusterDescription() + " found");
 								}
 	//							((NeighborCluster)tCluster).setClusterHopsOnOpposite(tEntry.getClusterHops(), tCEP);
 								((NeighborCluster)tCluster).addAnnouncedCEP(tCEP);
 								Logging.log(this, "Created " +tCluster);
 							} 
 							
 							// register the link to the local ARG
 							registerLinkARG(tAttachedCluster, tCluster, new AbstractRoutingGraphLink(AbstractRoutingGraphLink.LinkType.LOGICAL_LINK));
 						}
 						for(ICluster tCluster : tAttachedCluster.getNeighbors()) {
 							if(getSourceIntermediate(tCluster) != null) {
 								setSourceIntermediateCluster(tAttachedCluster, getSourceIntermediate(tCluster));
 							}
 						}
 					} else {
 						Logging.warn(this, "Adding cluster that contains no neighbors");
 					}
 				} else {
 					Logging.trace(this, "remote cluster was set earlier");
 				}
 				if(tCEP.getRemoteClusterName() == null) {
 					Logging.err(this, "Unable to set remote cluster");
					ClusterName tRemoteClusterName = new ClusterName(tParticipation.getSourceToken(), tParticipation.getSourceClusterID(), tParticipation.getLevel());
 							
 					tCEP.setRemoteClusterName(tRemoteClusterName);
 				}
				tCEP.setPeerPriority(tParticipation.getSenderPriority());
 				Logging.log(this, "Got request to open a new connection with reference cluster " + tFoundCluster);
 			}
 			
 			tConnectionSession.start(pConnection);
 		}else{
 			// probe-packet connection
 		}
 	}
 	
 	/**
 	 * 
 	 * @param pCluster cluster identification
 	 * @return local object that holds meta information about the specified entity
 	 */
 	public Cluster getCluster(ICluster pCluster)
 	{
 		for(Cluster tCluster : getAllClusters()) {
 			if (tCluster.equals(pCluster)) {
 				return tCluster;
 			}
 		}
 		return null;
 	}
 	
 	/**
 	 * 
 	 * @param pCluster cluster to which the distance has to be computed
 	 * @return number of clusters to target
 	 */
 	public int getClusterDistance(ICluster pCluster)
 	{
 		List<AbstractRoutingGraphLink> tClusterRoute = null;
 		int tDistance = 0;
 		if(getSourceIntermediate(pCluster) == null || pCluster == null) {
 			Logging.log(this, "source cluster for " + (pCluster instanceof NeighborCluster ? ((NeighborCluster)pCluster).getClusterDescription() : pCluster.toString() ) + " is " + getSourceIntermediate(pCluster));
 		}
 		ICluster tIntermediate = getSourceIntermediate(pCluster);
 		tClusterRoute = getRouteARG(tIntermediate, pCluster);
 		if(tClusterRoute != null && !tClusterRoute.isEmpty()) {
 			for(AbstractRoutingGraphLink tConnection : tClusterRoute) {
 				if(tConnection.getLinkType() == AbstractRoutingGraphLink.LinkType.LOGICAL_LINK) {
 					tDistance++;
 				}
 			}
 		} else {
 			//Logging.log(this, "No cluster route available");
 			tClusterRoute = getRouteARG(tIntermediate, pCluster);
 		}
 		return tDistance;
 	}
 
 	/**
 	 * This method is called in case a neighbor node is detected.
 	 * 
 	 * @param pName is the name of the entity a connection will be established to
 	 * @param pLevel is the level at which a connection is added
 	 * @param pToClusterID is the identity of the cluster a connection will be added to
 	 */
 	private void connectToNeighborNode(Name pName, HierarchyLevel pLevel, Long pToClusterID)
 	{
 		Logging.log(this, "ADDING CONNECTION to " + pName + "(ClusterID=" + pToClusterID + ", hierarchy level=" + pLevel.getValue() + ")");
 
 		ComSession tSession = null;
 		ICluster tFoundCluster = null;
 		ComChannel tCEP = null;
 		
 		boolean tClusterFound = false;
 		for(Cluster tCluster : getAllClusters())
 		{
 			if(tCluster.getClusterID().equals(pToClusterID)) {
 				tSession = new ComSession(this, false, pLevel, tCluster.getMultiplexer());
 				Route tRoute = null;
 				try {
 					tRoute = getHRS().getRoute(pName, new Description(), getNode().getIdentity());
 				} catch (RoutingException tExc) {
 					Logging.err(this, "Unable to resolve route to " + pName, tExc);
 				} catch (RequirementsException tExc) {
 					Logging.err(this, "Unable to fulfill requirements for a route to " + pName, tExc);
 				}
 				tSession.setRouteToPeer(tRoute);
 				tCEP = new ComChannel(this, tCluster);
 				tCluster.getMultiplexer().mapChannelToSession(tCEP, tSession);
 				tFoundCluster = tCluster;
 				tClusterFound = true;
 			}
 		}
 		if(!tClusterFound)
 		{
 			Logging.log(this, "Cluster is new, creating objects...");
 			Cluster tCluster = new Cluster(this, new Long(pToClusterID), pLevel);
 			setSourceIntermediateCluster(tCluster, tCluster);
 			registerNodeARG(tCluster);
 			tSession = new ComSession(this, false, pLevel, tCluster.getMultiplexer());
 			tCEP = new ComChannel(this, tCluster);
 			tCluster.getMultiplexer().mapChannelToSession(tCEP, tSession);
 			tFoundCluster = tCluster;
 		}
 		final ClusterParticipationProperty tClusterParticipationProperty = new ClusterParticipationProperty(pToClusterID, pLevel, 0);
 		NestedParticipation tParticipate = tClusterParticipationProperty.new NestedParticipation(pToClusterID, 0);
 		tClusterParticipationProperty.addNestedparticipation(tParticipate);
 		
 		tParticipate.setSourceClusterID(pToClusterID);
 		
 		final Name tNeighborName = pName;
 		final ComSession tFSession = tSession;
 		final ComChannel tDemultiplexed = tCEP;
 		final ICluster tClusterToAdd = tFoundCluster;
 		final HRMController tHRMController = this;
 		
 		Thread tThread = new Thread() {
 			public void run()
 			{
 				/**
 				 * Connect to the neighbor node
 				 */
 				Connection tConn = null;
 				Description tConReq = createHRMControllerDestinationDescription();
 				tConReq.set(tClusterParticipationProperty);
 				
 				try {
 					Logging.log(tHRMController, "\n\n\nOUTGOING CONNECTION to " + tNeighborName + " with requirements: " + tConReq);
 					tConn = getHost().connectBlock(tNeighborName, tConReq, getNode().getIdentity());
 				} catch (NetworkException tExc) {
 					Logging.err(tHRMController, "Unable to connecto to " + tNeighborName, tExc);
 				}
 				if(tConn != null) {
 					Logging.log(tHRMController, "     ..starting CONNECTION " + mCounterOutgoingConnections);
 					tFSession.start(tConn);					
 
 					/**
 					 * Determine the FN between the local central FN and the bus towards the physical neighbor node and tell this the neighbor (destination of this connection)
 					 */
 					L2Address tFirstFNL2Address = getL2AddressOfFirstFNTowardsNeighbor(tNeighborName);
 					
 					/**
 					 * Send NeighborRoutingInformation to the neighbor
 					 */
 					if (tFirstFNL2Address != null){
 						// get the name of the central FN
 						L2Address tCentralFNL2Address = getHRS().getCentralFNL2Address();
 						// create a map between the central FN and the search FN
 						NeighborRoutingInformation tNeighborRoutingInformation = new NeighborRoutingInformation(tCentralFNL2Address, tFirstFNL2Address, NeighborRoutingInformation.INIT_PACKET);
 						// tell the neighbor about the FN
 						Logging.log(tHRMController, "     ..send NEIGHBOR ROUTING INFO " + tNeighborRoutingInformation);
 						tFSession.write(tNeighborRoutingInformation);
 					}
 
 					/**
 					 * Find and set the route to the peer within the session object
 					 */
 					Route tRouteToNeighborFN = null;
 					// get a route to the neighbor node (the destination of the desired connection)
 					try {
 						tRouteToNeighborFN = getHRS().getRoute(tNeighborName, new Description(), getNode().getIdentity());
 					} catch (RoutingException tExc) {
 						Logging.err(tHRMController, "Unable to find route to " + tNeighborName, tExc);
 					} catch (RequirementsException tExc) {
 						Logging.err(tHRMController, "Unable to find route to " + tNeighborName + " with requirements no requirents, Huh!", tExc);
 					}
 					// have we found a route to the neighbor?
 					if(tRouteToNeighborFN != null) {
 						tFSession.setRouteToPeer(tRouteToNeighborFN);
 					}
 					
 					tDemultiplexed.setRemoteClusterName(new ClusterName(tClusterToAdd.getToken(), tClusterToAdd.getClusterID(), tClusterToAdd.getHierarchyLevel()));
 				}
 			}
 		};
 		tThread.start();
 	}
 
 	/**
 	 * 
 	 * @param pLevel as level at which a a coordinator will be set
 	 * @param pCluster is the cluster that has set a coordinator
 	 */
 	public void setClusterWithCoordinator(HierarchyLevel pLevel, ICluster pCluster)
 	{
 		Logging.log(this, "Setting " + pCluster + " as cluster that has a connection to a coordinator at level " + pLevel.getValue());
 		mLevelToCluster.put(Integer.valueOf(pLevel.getValue()), pCluster);
 	}
 	
 	/**
 	 * 
 	 * @param pLevel level at which a cluster with a coordinator should be provided
 	 * @return cluster that contains a reference or a connection to a coordinator
 	 */
 	public ICluster getClusterWithCoordinatorOnLevel(int pLevel)
 	{
 		return (mLevelToCluster.containsKey(pLevel) ? mLevelToCluster.get(pLevel) : null );
 	}
 	
 	/**
 	 * 
 	 * @param pCluster is the cluster for which an intermediate cluster is saved as entity that is physically connected
 	 * @param pIntermediate is the cluster that acts as cluster that is intermediately connected to the node
 	 */
 	public void setSourceIntermediateCluster(ICluster pCluster, Cluster pIntermediate)
 	{
 		if(pIntermediate == null) {
 			Logging.err(this, "Setting " + pIntermediate + " as source intermediate for " + pCluster);
 		}
 		mIntermediateMapping.put(pCluster, pIntermediate);
 	}
 	
 	/**
 	 * 
 	 * @param pCluster for which an intermediate cluster is searched
 	 * @return intermediate cluster that is directly connected to the node
 	 */
 	public Cluster getSourceIntermediate(ICluster pCluster)
 	{
 		if(mIntermediateMapping.containsKey(pCluster)) {
 			
 			return mIntermediateMapping.get(pCluster);
 		} else {
 			return null;
 		}
 	}
 	
 	/**
 	 * Determines the coordinator for a given hierarchy level.
 	 * 
 	 * @param pHierarchyLevel level for which all cluster managers should be provided
 	 * @return list of managers at the level
 	 */
 	public LinkedList<Coordinator> getCoordinator(HierarchyLevel pHierarchyLevel)
 	{
 		LinkedList<Coordinator> tResult = null;
 		
 		// is the given hierarchy level valid?
 		if (!pHierarchyLevel.isUndefined()){
 			synchronized (mRegisteredCoordinators) {
 				// check of we know the search coordinator
 				if(mRegisteredCoordinators.size() - 1 < pHierarchyLevel.getValue()) {
 					// we don't know a valid coordinator
 				} else {
 					// we have found the searched coordinator
 					tResult = mRegisteredCoordinators.get(pHierarchyLevel.getValue());
 				}
 			}
 		}else{
 			Logging.warn(this, "Cannot determine coordinator on an undefined hierachy level, return null");
 		}
 		
 		return tResult;
 	}
 
 	/**
 	 * 
 	 * @param pLevel is the level at which a search for clusters is done
 	 * @return all virtual nodes that appear at the specified hierarchical level
 	 */
 //	public LinkedList<IRoutableClusterGraphTargetName> getClusters(int pLevel)
 //	{
 //		LinkedList<IRoutableClusterGraphTargetName> tClusters = new LinkedList<IRoutableClusterGraphTargetName>();
 //		for(IRoutableClusterGraphTargetName tNode : getRoutableClusterGraph().getVertices()) {
 //			if(tNode instanceof ICluster && ((ICluster) tNode).getHierarchyLevel().getValue() == pLevel) {
 //				tClusters.add((ICluster) tNode);
 //			}
 //		}
 //		return tClusters;
 //	}
 //	
 
 	/**
 	 * 
 	 * @param pLevel is the level at which a multiplexer to other clusters is installed and that has to be returned
 	 * @return
 	 */
 	public ComChannelMuxer getCoordinatorMultiplexerOnLevel(Coordinator pCoordinator)
 	{
 		int tLevel = pCoordinator.getHierarchyLevel().getValue() + 1;
 		
 		if(mMuxOnLevel == null) {
 			mMuxOnLevel = new HashMap<Integer, ComChannelMuxer>();
 		}
 		
 		if(!mMuxOnLevel.containsKey(tLevel)) {
 			ComChannelMuxer tMux = new ComChannelMuxer(pCoordinator, this);
 			mMuxOnLevel.put(tLevel, tMux);
 			Logging.log(this, "Created new communication multiplexer " + tMux + " for coordinators on level " + tLevel);
 		}
 		
 		return mMuxOnLevel.get(tLevel);
 	}
 	
 	
 	
 	
 	/**
 	 * Creates a descriptive string about this object
 	 * 
 	 * @return the descriptive string
 	 */
 	public String toString()
 	{
 		return "HRM controller@" + getNode();
 	}
 
 	/**
 	 * Determine the name of the central FN of this node
 	 * 
 	 * @return the name of the central FN
 	 */
 	public Name getNodeName()
 	{
 		return mNode.getCentralFN().getName();
 	}
 }
