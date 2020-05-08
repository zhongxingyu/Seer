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
 
 import java.rmi.RemoteException;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 
 import de.tuilmenau.ics.fog.application.Application;
 import de.tuilmenau.ics.fog.application.Service;
 import de.tuilmenau.ics.fog.facade.Binding;
 import de.tuilmenau.ics.fog.facade.Connection;
 import de.tuilmenau.ics.fog.facade.Description;
 import de.tuilmenau.ics.fog.facade.Host;
 import de.tuilmenau.ics.fog.facade.IServerCallback;
 import de.tuilmenau.ics.fog.facade.Identity;
 import de.tuilmenau.ics.fog.facade.Name;
 import de.tuilmenau.ics.fog.facade.Namespace;
 import de.tuilmenau.ics.fog.facade.NetworkException;
 import de.tuilmenau.ics.fog.facade.RequirementsException;
 import de.tuilmenau.ics.fog.facade.RoutingException;
 import de.tuilmenau.ics.fog.facade.Signature;
 import de.tuilmenau.ics.fog.facade.properties.Property;
 import de.tuilmenau.ics.fog.facade.properties.PropertyException;
 import de.tuilmenau.ics.fog.packets.hierarchical.DiscoveryEntry;
 import de.tuilmenau.ics.fog.packets.hierarchical.RouteRequest;
 import de.tuilmenau.ics.fog.routing.Route;
 import de.tuilmenau.ics.fog.routing.RouteSegmentPath;
 import de.tuilmenau.ics.fog.routing.RoutingServiceLink;
 import de.tuilmenau.ics.fog.routing.hierarchical.clustering.*;
 import de.tuilmenau.ics.fog.routing.hierarchical.coordination.*;
 import de.tuilmenau.ics.fog.routing.hierarchical.properties.*;
 import de.tuilmenau.ics.fog.routing.hierarchical.properties.ClusterParticipationProperty.NestedParticipation;
 import de.tuilmenau.ics.fog.routing.naming.hierarchical.HRMID;
 import de.tuilmenau.ics.fog.routing.naming.hierarchical.HRMName;
 import de.tuilmenau.ics.fog.topology.Node;
 import de.tuilmenau.ics.fog.transfer.gates.GateID;
 import de.tuilmenau.ics.fog.ui.Logging;
 import de.tuilmenau.ics.fog.util.Logger;
 import de.tuilmenau.ics.fog.util.SimpleName;
 import de.tuilmenau.ics.fog.util.Tuple;
 
 /**
  * This is the main HRM controller. It provides functions that are necessary to build up the hierarchical structure - every node contains such an object
  */
 public class HRMController extends Application implements IServerCallback
 {
 	private boolean HRM_CONTROLLER_DEBUGGING = false;
 	
 	private SimpleName mName = null;
 	/**
 	 * Reference to physical node.
 	 */
 	private Node mPhysicalNode; //TV
 	private HierarchicalRoutingService mHRS = null;
 	private RoutableClusterGraph<IRoutableClusterGraphNode, RoutableClusterGraphLink> mRoutableClusterGraph = new RoutableClusterGraph<IRoutableClusterGraphNode, RoutableClusterGraphLink>();
 	private boolean mIsEdgeRouter;
 	private HashMap<Integer, ICluster> mLevelToCluster = new HashMap<Integer, ICluster>();
 	private HashMap<ICluster, Cluster> mIntermediateMapping = new HashMap<ICluster, Cluster>();
 //	private HashMap<Long, RouteRequest> mSessionToRequest = null;
 	private HashMap<Integer, CoordinatorCEPMultiplexer> mMuxOnLevel;
 	private LinkedList<LinkedList<Coordinator>> mRegisteredCoordinators;
 	private LinkedList<HRMSignature> mApprovedSignatures;
 	private HRMIdentity mIdentity;
 	private LinkedList<HRMID> mIdentifications = new LinkedList<HRMID>();
 	
 	/**
 	 * The global name space which is used to identify the HRM instances on neighbor nodes. //TV
 	 */
 	private final static Namespace ROUTING_NAMESPACE = new Namespace("routing");
 	
 	private int mConnectionCounter = 0;
 	
 	/**
 	 * @param pHost is the hosts that runs the coordinator
 	 * @param pParentLogger is the logger that is used for log output
 	 * @param pIdentity is the identity of the hosts that runs the coordinator
 	 * @param pNode is the node running the coordinator
 	 * @param pHRS is the hierarchical routing service that should be used
 	 */
 	public HRMController(Host pHost, Logger pParentLogger, Identity pIdentity, Node pNode, HierarchicalRoutingService pHRS)
 	{
 		super(pHost, pParentLogger, pIdentity);
 		mName = new SimpleName(ROUTING_NAMESPACE, null);
 		mHost = pHost;
 		mPhysicalNode = pNode;
 		getLogger().log(this, "created");
 		Binding serverSocket=null;
 		try {
 			serverSocket = getHost().bind(null, mName, getDescription(), getIdentity());
 			Service service = new Service(false, this);
 			service.start(serverSocket);
 		} catch (NetworkException tExc) {
 			Logging.err(this, "Unable to bind to hosts application interface", tExc);
 		}
 		mHRS = pHRS;
 		mApprovedSignatures = new LinkedList<HRMSignature>();
 	}
 
 	/**
 	 * This method is inherited from the class application and is called by the ServerFN object once a new connection setup request is required to be established.
 	 */
 	@Override
 	public void newConnection(Connection pConnection)
 	{
 		//long tClusterID = 0;
 		CoordinatorCEP tConnection = null;
 		
 		ClusterParticipationProperty tJoin = null;
 		Description tRequirements = pConnection.getRequirements();
 		for(Property tProperty : tRequirements) {
 			if(tProperty instanceof ClusterParticipationProperty) {
 				tJoin = (ClusterParticipationProperty)tProperty;
 			}
 		}
 		
 		try {
 			tJoin = (ClusterParticipationProperty) tRequirements.get(ClusterParticipationProperty.class);
 		} catch (ClassCastException tExc) {
 			mLogger.err(this, "Unable to find the information which cluster should be attached.", tExc);
 		}
 					
 		for(NestedParticipation tParticipate : tJoin.getNestedParticipations()) {
 			CoordinatorCEPDemultiplexed tCEP = null;
 			boolean tClusterFound = false;
 			ICluster tFoundCluster = null;
 			for(Cluster tCluster : getRoutingTargetClusters())
 			{
 				if(tCluster.equals(ClusterDummy.compare(tJoin.getTargetClusterID(), 0, tJoin.getLevel())) || tJoin.getTargetToken() != 0 && tCluster.equals(ClusterDummy.compare(tJoin.getTargetClusterID(), tJoin.getTargetToken(), tJoin.getLevel() )))	{
 					if(tConnection == null) {
 						tConnection = new CoordinatorCEP(mLogger, this, true, tJoin.getLevel(), tCluster.getMultiplexer());
 					}
 					
 					tCEP = new CoordinatorCEPDemultiplexed(mLogger, this, tCluster);
 					((Cluster)tCluster).getMultiplexer().addMultiplexedConnection(tCEP, tConnection);
 					if(tJoin.getLevel() > 0) {
 						((Cluster)tCluster).getMultiplexer().registerDemultiplex(tParticipate.getSourceClusterID(), tJoin.getTargetClusterID(), tCEP);
 					} else {
 						if(tParticipate.isInterASCluster()) {
 							tCEP.setEdgeCEP();
 							mIsEdgeRouter = true;
 						}
 					}
 					tCluster.addParticipatingCEP(tCEP);
 					tClusterFound = true;
 					tFoundCluster = tCluster;
 				}
 			}
 			if(!tClusterFound)
 			{
 				Cluster tCluster = new Cluster(new Long(tJoin.getTargetClusterID()), tJoin.getLevel(), this, mLogger);
 				if(tParticipate.isInterASCluster()) {
 					tCluster.setInterASCluster();
 					setSourceIntermediateCluster(tCluster, tCluster);
 				}
 				setSourceIntermediateCluster(tCluster, tCluster);
 				if(tConnection == null) {
 					tConnection = new CoordinatorCEP(mLogger, this, true, tJoin.getLevel(), tCluster.getMultiplexer());
 				}
 
 				if(tJoin.getLevel() > 0) {
 					for(ICluster tVirtualNode : getRoutingTargetClusters()) {
 						if(tVirtualNode.getHierarchyLevel() == tJoin.getLevel() - 1) {
 							tCluster.setPriority(tVirtualNode.getBullyPriority());
 						}
 					}
 				}
 				tCEP = new CoordinatorCEPDemultiplexed(mLogger, this, tCluster);
 				if(tJoin.getLevel() > 0) {
 					((Cluster)tCluster).getMultiplexer().registerDemultiplex(tParticipate.getSourceClusterID(), tJoin.getTargetClusterID(), tCEP);
 				} else {
 					if(tParticipate.isInterASCluster()) {
 						tCEP.setEdgeCEP();
 						mIsEdgeRouter = true;
 					}
 				}
 				tCluster.getMultiplexer().addMultiplexedConnection(tCEP, tConnection);
 				tCluster.addParticipatingCEP(tCEP);
 				tCluster.setAnnouncedCEP(tCEP);
 				tCEP.addAnnouncedCluster(tCluster, tCluster);
 				addCluster(tCluster);
 				tFoundCluster = tCluster;
 			}
 			tFoundCluster.getMultiplexer().addMultiplexedConnection(tCEP, tConnection);
 			for(ICluster tNegotiatingCluster : getRoutingTargetClusters()) {
 				if(tNegotiatingCluster.equals(ClusterDummy.compare(tParticipate.getSourceClusterID(), tParticipate.getSourceToken(), (tJoin.getLevel() - 1 > 0 ? tJoin.getLevel() - 1 : 0 )))) {
 					tCEP.setRemoteCluster(getCluster(ClusterDummy.compare(tParticipate.getSourceClusterID(), tParticipate.getSourceToken(), (tJoin.getLevel() - 1 > 0 ? tJoin.getLevel() - 1 : 0 ))));
 				}
 			}
 			if(tCEP.getRemoteCluster() == null && tJoin.getLevel() > 0) {
 				HashMap<ICluster, ClusterDummy> tNewlyCreatedClusters = new HashMap<ICluster, ClusterDummy>(); 
 				NeighborCluster tAttachedCluster = new NeighborCluster(tParticipate.getSourceClusterID(), tParticipate.getSourceName(), tParticipate.getSourceAddress(), tParticipate.getSourceToken(), tJoin.getLevel() -1, this);
 				tAttachedCluster.setPriority(tParticipate.getSenderPriority());
 				if(tAttachedCluster.getCoordinatorName() != null) {
 					try {
 						getHRS().registerNode(tAttachedCluster.getCoordinatorName(), tAttachedCluster.getCoordinatorsAddress());
 					} catch (RemoteException tExc) {
 						Logging.err(this, "Unable to fulfill requirements", tExc);
 					}
 				}
 				tNewlyCreatedClusters.put(tAttachedCluster, tParticipate.getPredecessor());
 				mLogger.log(this, "as joining cluster");
 				for(ICluster tCandidate : getRoutingTargetClusters()) {
 					if(tCandidate instanceof Cluster && tCandidate.getHierarchyLevel() == tAttachedCluster.getHierarchyLevel()) {
 						setSourceIntermediateCluster(tAttachedCluster, (Cluster)tCandidate);
 					}
 				}
 				if(getSourceIntermediate(tAttachedCluster) == null) {
 					mLogger.err(this, "No source intermediate cluster for" + tAttachedCluster.getClusterDescription() + " found");
 				}
 				
 				Logging.log(this, "Created " + tAttachedCluster);
 				
 				tCEP.setRemoteCluster(tAttachedCluster);
 				tAttachedCluster.addAnnouncedCEP(tCEP);
 				addCluster(tAttachedCluster);
 				if(tParticipate.getNeighbors() != null && !tParticipate.getNeighbors().isEmpty()) {
 					Logging.log(this, "Working on neighbors " + tParticipate.getNeighbors());
 					for(DiscoveryEntry tEntry : tParticipate.getNeighbors()) {
 						ICluster tCluster = null;
 						if(tEntry.getRoutingVectors()!= null) {
 							for(RoutingServiceLinkVector tVector : tEntry.getRoutingVectors())
 							getHRS().registerRoute(tVector.getSource(), tVector.getDestination(), tVector.getPath());
 						}
 						if(!getRoutingTargetClusters().contains(ClusterDummy.compare(tEntry.getClusterID(), tEntry.getToken(), tEntry.getLevel()))) {
 							tCluster = new NeighborCluster(tEntry.getClusterID(), tEntry.getCoordinatorName(), tEntry.getCoordinatorRoutingAddress(),  tEntry.getToken(), tEntry.getLevel(), this);
 							tCluster.setPriority(tEntry.getPriority());
 							if(tEntry.isInterASCluster()) {
 								tCluster.setInterASCluster();
 							}
 							try {
 								getHRS().registerNode(tCluster.getCoordinatorName(), tCluster.getCoordinatorsAddress());
 							} catch (RemoteException tExc) {
 								Logging.err(this, "Unable to fulfill requirements", tExc);
 							}
 							
 							
 							
 							if(tEntry.isInterASCluster()) tCluster.setInterASCluster();
 							tNewlyCreatedClusters.put(tCluster, tEntry.getPredecessor());
 							for(ICluster tCandidate : getRoutingTargetClusters()) {
 								if(tCandidate instanceof Cluster && tCluster.getHierarchyLevel() == tCandidate.getHierarchyLevel()) {
 									setSourceIntermediateCluster(tCluster, (Cluster)tCandidate);
 									mLogger.log(this, "as joining neighbor");
 								}
 							}
 							if(getSourceIntermediate(tAttachedCluster) == null) {
 								mLogger.err(this, "No source intermediate cluster for" + tCluster.getClusterDescription() + " found");
 							}
 //							((NeighborCluster)tCluster).setClusterHopsOnOpposite(tEntry.getClusterHops(), tCEP);
 							((NeighborCluster)tCluster).addAnnouncedCEP(tCEP);
 							Logging.log(this, "Created " +tCluster);
 						} else {
 							for(ICluster tPossibleCandidate : getRoutingTargetClusters()) {
 								if(tPossibleCandidate.equals(ClusterDummy.compare(tEntry.getClusterID(), tEntry.getToken(), tEntry.getLevel()))) {
 									tCluster = tPossibleCandidate;
 								}
 							}
 						}
 						getRoutableClusterGraph().storeLink(tAttachedCluster, tCluster, new RoutableClusterGraphLink(RoutableClusterGraphLink.LinkType.LOGICAL_LINK));
 					}
 					for(ICluster tCluster : tAttachedCluster.getNeighbors()) {
 						if(getSourceIntermediate(tCluster) != null) {
 							setSourceIntermediateCluster(tAttachedCluster, getSourceIntermediate(tCluster));
 						}
 					}
 				} else {
 					Logging.warn(this, "Adding cluster that contains no neighbors");
 				}
 				for(ICluster tEveluateNegotiator : tNewlyCreatedClusters.keySet()) {
 					tCEP.addAnnouncedCluster(tEveluateNegotiator, getCluster(tNewlyCreatedClusters.get(tEveluateNegotiator)));
 				}
 			} else {
 				mLogger.trace(this, "remote cluster was set earlier");
 			}
 			if(tCEP.getRemoteCluster() == null) {
 				mLogger.err(this, "Unable to set remote cluster");
 				tCEP.setRemoteCluster(ClusterDummy.compare(tParticipate.getSourceClusterID(), tParticipate.getSourceToken(), tParticipate.getLevel()));
 			}
 			tCEP.setPeerPriority(tParticipate.getSenderPriority());
 			mLogger.log(this, "Got request to open a new connection with reference cluster " + tFoundCluster);
 		}
 		
 		tConnection.start(pConnection);
 	}
 	
 	@Override
 	public boolean openAck(LinkedList<Signature> pAuths, Description pDescription, Name pTargetName)
 	{
 		return true;
 	}
 	
 	public String toString()
 	{
 		return "HRM controller@" + getPhysicalNode();
 	}
 	
 //	/**
 //	 * This function returns the last cluster is known as covered by another coordinator
 //	 * 
 //	 * @param pSourceCluster as cluster from which an uncovered node is propagated
 //	 * @param pTargetCluster as target cluster to which the first uncovered node has to be found
 //	 * @return first uncovered node - that node is the "outgoing interface of the cluster"
 //	 */
 //	public IVirtualNode getLastUncovered(IVirtualNode pSourceCluster, IVirtualNode pTargetCluster)
 //	{	
 //		if(pSourceCluster == null || pTargetCluster == null) {
 //			((ICluster)pSourceCluster).getHRMController().getLogger().log("You did not provide clusters for path search: " + pSourceCluster + " to " + pTargetCluster);
 //			return null;
 //		}
 //		RoutableClusterGraph<IVirtualNode, ClusterLink> tMap = ((ICluster)pSourceCluster).getHRMController().getRoutableClusterGraph();
 //		List<ClusterLink> tClusterConnection = tMap.getRoute(pSourceCluster, pTargetCluster);
 //		IVirtualNode tPredecessor=pSourceCluster;
 //		for(ClusterLink tLink: tClusterConnection) {
 //			if( ((ICluster)getRoutableClusterGraph().getDest(tLink)).getNegotiatorCEP() != null && ((ICluster)getRoutableClusterGraph().getDest(tLink)).getNegotiatorCEP().knowsCoordinator()) {
 //				return tPredecessor;
 //			} else if(((ICluster)getRoutableClusterGraph().getDest(tLink)).getNegotiatorCEP() != null) {
 //				tPredecessor = ((ICluster)getRoutableClusterGraph().getDest(tLink));
 //			}
 //		}
 //		return pTargetCluster;
 //	}
 //	
 	/**
 	 * 
 	 * @param pSourceCluster source cluster
 	 * @param pTargetCluster specify the target cluster to which the path has to be checked for separation through another coordinator
 	 * @param pCEPsToEvaluate list of connection end points that have to be chosen to the target
 	 * @return true if the path contains a node that is covered by another coordinator
 	 */
 	public boolean checkPathToTargetContainsCovered(IRoutableClusterGraphNode pSourceCluster, IRoutableClusterGraphNode pTargetCluster, LinkedList<CoordinatorCEPDemultiplexed> pCEPsToEvaluate)
 	{
 		if(pSourceCluster == null || pTargetCluster == null) {
 			Logging.log(this, "checking cluster route between null and null");
 			return false;
 		}
 		RoutableClusterGraph<IRoutableClusterGraphNode, RoutableClusterGraphLink> tMap = ((ICluster)pSourceCluster).getHRMController().getRoutableClusterGraph();
 		List<RoutableClusterGraphLink> tClusterConnection = tMap.getRoute(pSourceCluster, pTargetCluster);
 		String tCheckedClusters = new String();
 		boolean isCovered = false;
 		for(RoutableClusterGraphLink tConnection : tClusterConnection) {
 			Collection<IRoutableClusterGraphNode> tNodes = tMap.getGraphForGUI().getIncidentVertices(tConnection);
 			for(IRoutableClusterGraphNode tNode : tNodes) {
 				if(tNode instanceof ICluster) {
 					CoordinatorCEPDemultiplexed tCEPLookingFor = null;
 					for(CoordinatorCEPDemultiplexed tCEP : pCEPsToEvaluate) {
 						if(tCEP.getRemoteCluster().equals(tNode)) {
 							tCEPLookingFor = tCEP;
 						}
 					}
 					tCheckedClusters += tNode + " knows coordinator " + (tCEPLookingFor != null ? tCEPLookingFor.knowsCoordinator() : "UNKNOWN" ) + "\n";
 					if(tCEPLookingFor != null && tCEPLookingFor.knowsCoordinator()) {
 						isCovered = isCovered || true;
 					}
 				}
 			}
 		}
 		Logging.log(this, "Checked clusterroute from " + pSourceCluster + " to clusters " + tCheckedClusters);
 		return isCovered;
 	}
 	
 	/**
 	 * 
 	 * @param pCluster cluster identification
 	 * @return local object that holds meta information about the specified entity
 	 */
 	public ICluster getCluster(ICluster pCluster)
 	{
 		for(Cluster tCluster : getRoutingTargetClusters()) {
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
 		List<RoutableClusterGraphLink> tClusterRoute = null;
 		int tDistance = 0;
 		if(getSourceIntermediate(pCluster) == null || pCluster == null) {
 			mLogger.log(this, "source cluster for " + (pCluster instanceof NeighborCluster ? ((NeighborCluster)pCluster).getClusterDescription() : pCluster.toString() ) + " is " + getSourceIntermediate(pCluster));
 		}
 		ICluster tIntermediate = getSourceIntermediate(pCluster);
 		tClusterRoute = getRoutableClusterGraph().getRoute(tIntermediate, pCluster);
 		if(tClusterRoute != null && !tClusterRoute.isEmpty()) {
 			for(RoutableClusterGraphLink tConnection : tClusterRoute) {
 				if(tConnection.getLinkType() == RoutableClusterGraphLink.LinkType.LOGICAL_LINK) {
 					tDistance++;
 				}
 			}
 		} else {
 			Logging.log(this, "No cluster route available");
 			tClusterRoute = getRoutableClusterGraph().getRoute(tIntermediate, pCluster);
 		}
 		return tDistance;
 	}
 
 	/**
 	 * 
 	 * @param pParticipationProperty is the object that describes in which cluster this node wishes to participate
 	 * @return @return the description that will be put into the packet
 	 */
 	public Description getConnectDescription(ClusterParticipationProperty pParticipationProperty)
 	{
 		mLogger.log(this, "Creating a cluster participation property for level " + pParticipationProperty.getLevel());
 		Description tDescription = new Description();
 		//try {
 		tDescription.set(new ContactDestinationApplication(null, HRMController.ROUTING_NAMESPACE));
 		//} catch (PropertyException tExc) {
 		//	mLogger.err(this, "Unable to fulfill requirements given by ContactDestinationProperty", tExc);
 		//}
 
 		try {
 			tDescription.add(pParticipationProperty);
 		} catch (PropertyException tExc) {
 			mLogger.err(this, "Unable to match property that wants us to participate in a cluster", tExc);
 		}
 		return tDescription;
 	}
 	
 	/**
 	 * This method has to be invoked once a new neighbor node is spotted (/hierarchy level 0).
 	 * It causes the addition to the intermediate cluster that is associated to the interface the note was spotted at.
 	 * 
 	 * @param pName is the name of the entity a connection will be established to
 	 * @param pLevel is the level at which a connection is added
 	 * @param pToClusterID is the identity of the cluster a connection will be added to
 	 * @param pConnectionToOtherAS says whether the connection leads to another autonomous system
 	 */
 	public void addConnection(Name pName, int pLevel, Long pToClusterID, boolean pConnectionToOtherAS)
 	{
 		if(pConnectionToOtherAS) {
 			Logging.log(this, "Trigger");
 		}
 		
 		CoordinatorCEP tCEP = null;
 		ICluster tFoundCluster = null;
 		CoordinatorCEPDemultiplexed tDemux = null;
 		
 		boolean tClusterFound = false;
		for(ICluster tCluster : getRoutingTargetClusters())
 		{
 			if(tCluster.getClusterID().equals(pToClusterID)) {
 				tCEP = new CoordinatorCEP(mLogger, this, false, pLevel, tCluster.getMultiplexer());
 				Route tRoute = null;
 				try {
 					tRoute = getHRS().getRoute(getPhysicalNode().getCentralFN(), pName, new Description(), getPhysicalNode().getIdentity());
 				} catch (RoutingException tExc) {
 					mLogger.err(this, "Unable to resolve route to " + pName, tExc);
 				} catch (RequirementsException tExc) {
 					mLogger.err(this, "Unable to fulfill requirements for a route to " + pName, tExc);
 				}
 				tCEP.setRouteToPeer(tRoute);
 				tDemux = new CoordinatorCEPDemultiplexed(mLogger, this, tCluster);
				((Cluster)tCluster).getMultiplexer().addMultiplexedConnection(tDemux, tCEP);
 				
 				tCluster.addParticipatingCEP(tDemux);
 				tFoundCluster = tCluster;
 				tClusterFound = true;
 			}
 		}
 		if(!tClusterFound)
 		{
 			Cluster tCluster = new Cluster(new Long(pToClusterID), pLevel, this, mLogger);
 			setSourceIntermediateCluster(tCluster, tCluster);
 			addCluster(tCluster);
 			tCEP = new CoordinatorCEP(mLogger, this, false, pLevel, tCluster.getMultiplexer());
 			tDemux = new CoordinatorCEPDemultiplexed(mLogger, this, tCluster);
			((Cluster)tCluster).getMultiplexer().addMultiplexedConnection(tDemux, tCEP);
 			
 			tCluster.addParticipatingCEP(tDemux);
 			tFoundCluster = tCluster;
 		}
 		final ClusterParticipationProperty tProperty = new ClusterParticipationProperty(pToClusterID, pLevel, 0);
 		NestedParticipation tParticipate = tProperty.new NestedParticipation(pToClusterID, 0);
 		tProperty.addNestedparticipation(tParticipate);
 		
 		if(pConnectionToOtherAS) {
 			tFoundCluster.setInterASCluster();
 			mIsEdgeRouter = true;
 			tDemux.setEdgeCEP();
 			tParticipate.setInterASCluster();	
 		}
 		tParticipate.setSourceClusterID(pToClusterID);
 		
 		final Name tName = pName;
 		final CoordinatorCEP tConnectionCEP = tCEP;
 		final CoordinatorCEPDemultiplexed tDemultiplexed = tDemux;
 		final ICluster tClusterToAdd = tFoundCluster;
 		
 		Thread tThread = new Thread() {
 			public void run()
 			{
 				Connection tConn = null;
 				try {
 					tConn = mHost.connectBlock(tName, getConnectDescription(tProperty), getPhysicalNode().getIdentity());
 				} catch (NetworkException tExc) {
 					Logging.err(this, "Unable to connecto to " + tName, tExc);
 				}
 				if(tConn != null) {
 					mLogger.log(this, "Sending source routing service address " + tConnectionCEP.getSourceRoutingServiceAddress() + " for connection number " + (++mConnectionCounter));
 					tConnectionCEP.start(tConn);
 					
 					HRMName tMyAddress = tConnectionCEP.getSourceRoutingServiceAddress();
 
 					Route tRoute = null;
 					try {
 						tRoute = getHRS().getRoute(getPhysicalNode().getCentralFN(), tName, new Description(), getPhysicalNode().getIdentity());
 					} catch (RoutingException tExc) {
 						getLogger().err(this, "Unable to find route to " + tName, tExc);
 					} catch (RequirementsException tExc) {
 						getLogger().err(this, "Unable to find route to " + tName + " with requirements no requirents, Huh!", tExc);
 					}
 					
 					HRMName tMyFirstNodeInDirection = null;
 					if(tRoute != null) {
 						RouteSegmentPath tPath = (RouteSegmentPath) tRoute.getFirst();
 						GateID tID= tPath.getFirst();
 						
 						Collection<RoutingServiceLink> tLinkCollection = getHRS().getLocalRoutingMap().getOutEdges(tMyAddress);
 						RoutingServiceLink tOutEdge = null;
 						
 						for(RoutingServiceLink tLink : tLinkCollection) {
 							if(tLink.equals(tID)) {
 								tOutEdge = tLink;
 							}
 						}
 						
 						tMyFirstNodeInDirection = getHRS().getLocalRoutingMap().getDest(tOutEdge);
 						tConnectionCEP.setRouteToPeer(tRoute);
 					}
 					
 					Tuple<HRMName, HRMName> tTuple = new Tuple<HRMName, HRMName>(tMyAddress, tMyFirstNodeInDirection);
 					tConnectionCEP.write(tTuple);
 					tDemultiplexed.setRemoteCluster(tClusterToAdd);
 				}
 			}
 		};
 		tThread.start();
 	}
 	
 //	/**
 //	 * 
 //	 * @deprecated This function is for the old infrastructure in which BGP and HRM was mixed quite unorthodox
 //	 * @param pNamespace is the namespace that defines which routing identity is wished
 //	 * @return name of the central FN according to the routing service that was specified
 //	 */
 //	public Name getCentralFNAddress(Namespace pNamespace)
 //	{
 //		if(getPhysicalNode().getRoutingService() instanceof RoutingServiceMultiplexer) {
 //			for(NameMappingService tNMS : ((RoutingServiceMultiplexer)getPhysicalNode().getRoutingService()).getNameMappingServices()) {
 //				NameMappingEntry[] tEntries;
 //				try {
 //					tEntries = tNMS.getAddresses(getPhysicalNode().getCentralFN().getName());
 //					if(tEntries != null) {
 //						for(NameMappingEntry tEntry : tEntries) {
 //							if( ((Name)tEntry.getAddress()).getNamespace().equals(pNamespace)) {
 //								return (Name)tEntry.getAddress();
 //							}
 //						}
 //					}
 //				} catch (RemoteException tExc) {
 //					mLogger.err(this, "Unable to determine name for " + getPhysicalNode().getName(), tExc);
 //				}
 //				
 //			}
 //		} else {
 //			return getHRS().getSourceIdentification();
 //		}
 //		return null;
 //	}
 //	
 	
 	@Override
 	protected void started() {
 		;
 	}
 	
 	@Override
 	public void exit() {
 	}
 
 	@Override
 	public boolean isRunning() {
 		return true;
 	}
 	
 	/**
 	 * 
 	 * @return hierarchical routing service of this entity
 	 */
 	public HierarchicalRoutingService getHRS()
 	{
 		return mHRS;
 	}
 	
 	/**
 	 * @return the physical node running this coordinator
 	 */
 	public Node getPhysicalNode() //TV
 	{
 		return mPhysicalNode;
 	}
 	
 	/**
 	 * 
 	 * @param pCluster is the cluster to be added to the local cluster map
 	 */
 	public synchronized void addCluster(ICluster pCluster)
 	{
 		if(!mRoutableClusterGraph.contains(pCluster)) {
 			mRoutableClusterGraph.add(pCluster);
 		}
 	}
 	
 	/**
 	 * Calculates the clusters which are known to the local routing database (graph)
 	 * 
 	 * @return list of all known clusters from the local routing database (graph)
 	 */
 	public synchronized LinkedList<Cluster> getRoutingTargetClusters()
 	{
 		LinkedList<Cluster> tResult = new LinkedList<Cluster>();
 
 		if (HRM_CONTROLLER_DEBUGGING) {
 			Logging.log(this, "Amount of found routing targets: " + mRoutableClusterGraph.getVertices().size());
 		}
 		int j = -1;
 		for(IRoutableClusterGraphNode tRoutableGraphNode : mRoutableClusterGraph.getVertices()) {
 			if (tRoutableGraphNode instanceof Cluster) {
 				Cluster tCluster = (Cluster)tRoutableGraphNode;
 				j++;
 			
 				if (HRM_CONTROLLER_DEBUGGING) {
 					Logging.log(this, "Returning routing target cluster " + j + ": " + tRoutableGraphNode.toString());
 				}
 				
 				tResult.add(tCluster);
 			}
 		}
 		
 		return tResult;
 	}
 	
 	/**
 	 * Calculates the clusters which are known to the local routing database (graph)
 	 * 
 	 * @return list of all known clusters from the local routing database (graph)
 	 */
 	public synchronized LinkedList<ICluster> getRoutingTargets()
 	{
 		LinkedList<ICluster> tResult = new LinkedList<ICluster>();
 
 		if (HRM_CONTROLLER_DEBUGGING) {
 			Logging.log(this, "Amount of found routing targets: " + mRoutableClusterGraph.getVertices().size());
 		}
 		int j = -1;
 		for(IRoutableClusterGraphNode tRoutableGraphNode : mRoutableClusterGraph.getVertices()) {
 			ICluster tCluster = (ICluster)tRoutableGraphNode;
 			j++;
 		
 			if (HRM_CONTROLLER_DEBUGGING) {
 				Logging.log(this, "Returning routing target " + j + ": " + tRoutableGraphNode.toString());
 			}
 			
 			tResult.add(tCluster);
 		}
 		
 		return tResult;
 	}
 
 	/**
 	 * 
 	 * @return cluster map that is actually the graph that represents the network
 	 */
 	public RoutableClusterGraph<IRoutableClusterGraphNode, RoutableClusterGraphLink> getRoutableClusterGraph()
 	{
 		return mRoutableClusterGraph;
 	}
 	
 //	/**
 //	 * 
 //	 * @param pName name that should be registered for the address given as parameter two
 //	 * @param pAddress address that should be registered for the name given as parameter one
 //	 */
 //	public void registerNode(Name pName, HRMName pAddress)
 //	{
 //		try {
 //			getHRS().registerNode(pName, pAddress);
 //		} catch (RemoteException tExc) {
 //			mLogger.err(this, "Unable to fulfill requirements", tExc);
 //		}
 //	}
 	
 	/**
 	 * 
 	 * @param pLevel as level at which a a coordinator will be set
 	 * @param pCluster is the cluster that has set a coordinator
 	 */
 	public void setClusterWithCoordinator(int pLevel, ICluster pCluster)
 	{
 		mLogger.log(this, "Setting " + pCluster + " as cluster that has a connection to a coordinator at level " + pLevel);
 		mLevelToCluster.put(Integer.valueOf(pLevel), pCluster);
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
 			mLogger.err(this, "Setting " + pIntermediate + " as source intermediate for " + pCluster);
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
 	public LinkedList<Coordinator> getCoordinator(int pHierarchyLevel)
 	{
 		if(mRegisteredCoordinators.size() < pHierarchyLevel) {
 			return null;
 		} else {
 			return mRegisteredCoordinators.get(pHierarchyLevel);
 		}
 	}
 	
 	/**
 	 * Registers a coordinator for a defined hierarchy level.
 	 * 
 	 * @param pCoordinator the coordinator for a defined cluster
 	 * @param pHierarchyLevel the hierarchy level at which the coordinator is located
 	 */
 	public void registerCoordinator(Coordinator pCoordinator, int pHierarchyLevel)
 	{
 		// make sure we have a valid linked list object
 		if(mRegisteredCoordinators == null) {
 			mRegisteredCoordinators = new LinkedList<LinkedList<Coordinator>>();
 		}
 		
 		if(mRegisteredCoordinators.size() <= pHierarchyLevel) {
 			for(int i = mRegisteredCoordinators.size() - 1; i <= pHierarchyLevel ; i++) {
 				mRegisteredCoordinators.add(new LinkedList<Coordinator>());
 			}
 		}
 		
 		// store the new coordinator
 		if (mRegisteredCoordinators.get(pHierarchyLevel).size() > 0)
 		{
 			getLogger().log("#### Got more than one coordinator at level " + pHierarchyLevel + ", already known: " + mRegisteredCoordinators.get(pHierarchyLevel).get(0) + ", new one: " + pCoordinator);
 		}
 		mRegisteredCoordinators.get(pHierarchyLevel).add(pCoordinator);
 	}
 	
 	/**
 	 * 
 	 * @return list of all signatures that were already approved
 	 */
 	public LinkedList<HRMSignature> getApprovedSignatures()
 	{
 		return mApprovedSignatures;
 	}
 	
 	/**
 	 * 
 	 * @param pSignature is a signature that validates a FIB entry.
 	 */
 	public void addApprovedSignature(HRMSignature pSignature)
 	{
 		if(mApprovedSignatures == null) {
 			mApprovedSignatures = new LinkedList<HRMSignature>();
 		}
 		if(!mApprovedSignatures.contains(pSignature)) {
 			mApprovedSignatures.add(pSignature);
 		}
 	}
 	
 //	/**
 //	 * 
 //	 * @param pSignature is the signature of a coordinator that is not longer supposed to be authorized for the creation of FIB entries
 //	 */
 //	public void deleteApprovedSignature(Signature pSignature)
 //	{
 //		mApprovedSignatures.removeFirstOccurrence(pSignature);
 //	}
 	
 	/**
 	 * 
 	 * @param pIdentity is the identity that is supposed to be used for signing FIB entries
 	 */
 	public void setIdentity(HRMIdentity pIdentity)
 	{
 		mIdentity = pIdentity;
 	}
 
 	public HRMIdentity getIdentity()
 	{
 		return mIdentity;
 	}
 	
 	/**
 	 * 
 	 * @param pIdentification is one more identification the physical node may have because it can be either coordinator of different hierarchical levels or attached to different clusters
 	 */
 	public void addIdentification(HRMID pIdentification)
 	{
 		if(!mIdentifications.contains(pIdentification)) {
 			mIdentifications.add(pIdentification);
 		}
 	}
 	
 	/**
 	 * 
 	 * @param pIdentification is one HRMID that is checked against the identifications of the node owning the coordinator object
 	 * @return
 	 */
 	public boolean containsIdentification(HRMID pIdentification)
 	{
 		return mIdentifications.contains(pIdentification);
 	}
 	
 	/**
 	 * 
 	 * @param pLevel is the level at which a search for clusters is done
 	 * @return all virtual nodes that appear at the specified hierarchical level
 	 */
 	public LinkedList<IRoutableClusterGraphNode> getClusters(int pLevel)
 	{
 		LinkedList<IRoutableClusterGraphNode> tClusters = new LinkedList<IRoutableClusterGraphNode>();
 		for(IRoutableClusterGraphNode tNode : getRoutableClusterGraph().getVertices()) {
 			if(tNode instanceof ICluster && ((ICluster) tNode).getHierarchyLevel() == pLevel) {
 				tClusters.add((ICluster) tNode);
 			}
 		}
 		return tClusters;
 	}
 	
 //	/**
 //	 * In case the first hop is either required or not allowed, the following function finds candidates that allow routing from this
 //	 * entity to the next hop. 
 //	 * @param pSource 
 //	 * @param pLimitation contains restrictions regarding the allowed clusters, however the clusters have to be addressed by the given HRMID
 //	 * @return a list of allowed cluster
 //	 */
 //	private LinkedList<ICluster> getAllowedCluster(ICluster pSource, AddressLimitationProperty pLimitation)
 //	{
 //		LinkedList<ICluster> tPossibleClusters = new LinkedList<ICluster>();
 //		LinkedList<ICluster> tCandidates = new LinkedList<ICluster>();
 //		tCandidates.add(pSource);
 //		for(ICluster tCandidate : pSource.getNeighbors()) {
 //			if(tCandidate instanceof Cluster) {
 //				tCandidates.add(tCandidate);
 //			}
 //		}
 //		
 //		for(ICluster tCandidate : tCandidates) {
 //			for(HierarchyLevelLimitationEntry tEntry : pLimitation.getEntries()) {
 //				HRMID tClustersAddress = tCandidate.getHrmID();
 //				HRMID tEntryAddress = null;
 //				if(tEntry.getAddress() instanceof HRMID) {
 //					tEntryAddress =  (HRMID) tEntry.getAddress();
 //				} else {
 //					continue;
 //				}
 //				
 //				int tLowestDifference = tClustersAddress.getDescendingDifference(tEntryAddress);
 //				HRMID tComparison = new HRMID(0);
 //				for(int i = HRMConfig.Hierarchy.HEIGHT -1; i >= tLowestDifference; i--) {
 //					BigInteger tEntryLevelAddress = tEntryAddress.getLevelAddress(i);
 //					if(tEntryLevelAddress.equals(BigInteger.valueOf(0))) {
 //						tComparison.setLevelAddress(i, BigInteger.valueOf(0));
 //						for(int j = 0; j >=0 ; j--) {
 //							tComparison.setLevelAddress(j, BigInteger.valueOf(0));
 //						}
 //					} else {
 //						tComparison.setLevelAddress(i, tClustersAddress.getLevelAddress(i));
 //					}
 //				}
 //				if(tComparison.getAddress() != BigInteger.ZERO) {
 //					if(pLimitation.getType().equals(AddressLimitationProperty.LIST_TYPE.OBSTRUCTIVE)) {
 //						/*
 //						 * If it is obstructive, the entries tell which clusters are not allowed to be used
 //						 */
 //						if(!tComparison.equals(tEntry.getAddress())) {
 //							tPossibleClusters.add(tCandidate);
 //						} else {
 //							if(tPossibleClusters.contains(tCandidate)) {
 //								tPossibleClusters.remove(tCandidate);
 //							}
 //						}
 //					} else if(pLimitation.getType().equals(AddressLimitationProperty.LIST_TYPE.RESTRICTIVE)) {
 //						/*
 //						 * If it is restrictive we allow the cluster to be taken if it appears in the list of possible HRMIDs
 //						 */
 //						if(tComparison.equals(tEntry.getAddress())) {
 //							tPossibleClusters.add(tCandidate);
 //						}
 //					}
 //				}
 //			}
 //		}
 //		
 //		return tPossibleClusters;
 //	}
 //	
 //	/**
 //	 * Query route to a target that has to be specified in the object RouteRequest
 //	 * 
 //	 * @param pRequest contains the target of the request and restrictions regarding the route that has to be chosen
 //	 */
 //	public void queryRoute(RouteRequest pRequest)
 //	{
 //		try {
 //			HRMID tForwardingHRMID = getHRS().getForwardingHRMID( (HRMID) pRequest.getTarget());
 ////			ICluster tForwardingCluster = getCluster(getHRS().getFIBEntry(tForwardingHRMID).getNextCluster());
 //			
 //			LinkedList<ICluster> tAllowedClusters = null;//getAllowedCluster(tForwardingCluster, tLimitation);
 //			LinkedList<RouteRequest> tResults = new LinkedList<RouteRequest>();
 //			
 //			for(ICluster tCandidate : tAllowedClusters) {
 //				if(tCandidate.getCoordinatorCEP() != null) {
 //					if(pRequest.getRoute() == null) {
 //						//Route tRoute = new Route();
 //						//tRoute.add(tCandidate.getCoordinatorCEP().getRouteToPeer());
 //						pRequest.addRoutingVector(new RoutingServiceLinkVector(tCandidate.getCoordinatorCEP().getRouteToPeer(), tCandidate.getCoordinatorCEP().getSourceName(), tCandidate.getCoordinatorCEP().getPeerName()));
 //					}
 //					tCandidate.getCoordinatorCEP().sendPacket(pRequest);
 //					synchronized(pRequest) {
 //						if(!pRequest.isAnswer()) {
 //							pRequest.wait();
 //						}
 //					}
 //					if(pRequest.getResult().equals(RouteRequest.ResultType.SUCCESS)) {
 //						break;
 //					} else {
 //						tResults.add(pRequest);
 //						pRequest = pRequest.clone();
 //					}
 //				} else {
 //					((Cluster)tCandidate).getClusterManager().handleRouteRequest(pRequest, tCandidate);
 //				}
 //			}
 //		} catch (RemoteException tExc) {
 //			Logging.err(this, "Error when trying to determine region limited route", tExc);
 //		} catch (InterruptedException tExc) {
 //			Logging.err(this, "Error occured when waiting for route request", tExc);
 //		}
 //	}
 //	
 //	/**
 //	 * Find an appropriate route request in order to put together the pieces.
 //	 * 
 //	 * @param pSession is the identification of the session that has to be found
 //	 * @return
 //	 */
 //	public RouteRequest getRouteRequest(Integer pSession)
 //	{
 //		return mSessionToRequest.get(pSession);
 //	}
 	
 	/**
 	 * Find out whether this object is an edge router
 	 * 
 	 * @return true if the node is a router to another autonomous system
 	 */
 	public boolean isEdgeRouter()
 	{
 		return mIsEdgeRouter;
 	}
 	
 	/**
 	 * 
 	 * @param pLevel is the level at which a multiplexer to other clusters is installed and that has to be returned
 	 * @return
 	 */
 	public CoordinatorCEPMultiplexer getMultiplexerOnLevel(int pLevel)
 	{
 		if(mMuxOnLevel == null) {
 			mMuxOnLevel = new HashMap<Integer, CoordinatorCEPMultiplexer>();
 		}
 		if(!mMuxOnLevel.containsKey(pLevel)) {
 			CoordinatorCEPMultiplexer tMux = new CoordinatorCEPMultiplexer(this);
 			mMuxOnLevel.put(pLevel, tMux);
 			Logging.log(this, "Created new Multiplexer " + tMux + " for cluster managers on level " + pLevel);
 		}
 		return mMuxOnLevel.get(pLevel);
 	}
 }
