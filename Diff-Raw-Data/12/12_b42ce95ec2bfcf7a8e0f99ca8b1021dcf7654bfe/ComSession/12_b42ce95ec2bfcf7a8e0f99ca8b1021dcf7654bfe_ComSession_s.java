 /*******************************************************************************
  * Forwarding on Gates Simulator/Emulator - Hierarchical Routing Management
  * Copyright (c) 2012, Integrated Communication Systems Group, TU Ilmenau.
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html.
  ******************************************************************************/
 package de.tuilmenau.ics.fog.routing.hierarchical.management;
 
 import java.io.Serializable;
 import java.util.LinkedList;
 
 import de.tuilmenau.ics.fog.application.util.Session;
 import de.tuilmenau.ics.fog.facade.Connection;
 import de.tuilmenau.ics.fog.facade.Description;
 import de.tuilmenau.ics.fog.facade.NetworkException;
 import de.tuilmenau.ics.fog.facade.RequirementsException;
 import de.tuilmenau.ics.fog.facade.RoutingException;
 import de.tuilmenau.ics.fog.packets.hierarchical.clustering.InformClusterLeft;
 import de.tuilmenau.ics.fog.packets.hierarchical.clustering.RequestClusterMembership;
 import de.tuilmenau.ics.fog.packets.hierarchical.AnnouncePhysicalEndPoint;
 import de.tuilmenau.ics.fog.packets.hierarchical.MultiplexHeader;
 import de.tuilmenau.ics.fog.packets.hierarchical.SignalingMessageHrm;
 import de.tuilmenau.ics.fog.routing.Route;
 import de.tuilmenau.ics.fog.routing.RouteSegment;
 import de.tuilmenau.ics.fog.routing.RouteSegmentAddress;
 import de.tuilmenau.ics.fog.routing.hierarchical.HRMConfig;
 import de.tuilmenau.ics.fog.routing.hierarchical.HRMController;
 import de.tuilmenau.ics.fog.routing.naming.hierarchical.L2Address;
 import de.tuilmenau.ics.fog.ui.Logging;
 
 /**
  * This class is used to manage a Session of a connection between two physical nodes.
  * Such a communication session is able to handle several inferior communication channels,
  * which handle again the communication between two control entities of the HRM infrastructure.
  * 
  * There exist a 1:1 relation between a FoG connection and a ComSession instance.
  * 
  */
 public class ComSession extends Session
 {
 
 	/**
 	 * Stores the L2Address of the peer - this reference is used within getPeerL2Address() of ComChannel
 	 */
 	private L2Address mPeerL2Address = null;
 
 	/**
 	 * Stores a reference to the HRMController application.
 	 */
 	private HRMController mHRMController = null;
 	
 	/**
 	 * Stores the registered ComChannel objects
 	 */
 	private LinkedList<ComChannel> mRegisteredComChannels = new LinkedList<ComChannel>();
 
 	/**
 	 * Stores the unregistered ComChannel objects
 	 */
 	private LinkedList<ComChannel> mUnregisteredComChannels = new LinkedList<ComChannel>();
 
 	/**
 	 * Stores a reference to the parent FoG connection
 	 */
 	private Connection mParentConnection = null;
 	
 	/**
 	 * Stores if this session is a local loopback session
 	 */
 	private boolean mLocalLoopback = false;
 	
 	/**
 	 * Stores if this an outgoing or incoming connection
 	 */
 	private boolean mIncomingConnection = false;
 
 	/**
 	 * Stores the route to the peer.
 	 */
 	private Route mRouteToPeer;
 	
 	/**
 	 * 
 	 * @param pHRMController is the HRMController instance this connection end point is associated to
 	 * @param pIncomingConnection indicates whether the connection is incoming or outgoing
 	 * @param pLevel the hierarchy level of this session
 	 * @param pComChannelMuxer the communication multiplexer to use
 	 * 
 	 */
 	public ComSession(HRMController pHRMController, boolean pIncomingConnection)
 	{
 		// call the Session constructor
 		super(false /* event handler not in an own tread */, Logging.getInstance(), null);
 		
 		// store a reference to the HRMController application
 		mHRMController = pHRMController;
 		
 		// store the connection direction
 		mIncomingConnection = pIncomingConnection;
 		
 		// register at the HRMController as incoming or outgoing session
 		if (mIncomingConnection){
 		    mHRMController.registerIncomingSession(this);
 		}else{
 		    mHRMController.registerOutgoingSession(this);
 		}
 			
 		if (mIncomingConnection){
 			Logging.log(this, "SERVER SESSION CREATED");
 		}else{
 			Logging.log(this, "CLIENT SESSION CREATED");
 		}
 	}
 	
 	/**
 	 * Factory function: creates a local loopback session
 	 * 
 	 * @param pHRMController the HRMController instance
 	 * 
 	 * @return the create comm. session
 	 */
 	static public ComSession createLoopback(HRMController pHRMController)
 	{
 		ComSession tResult = new ComSession(pHRMController, false);
 		
 		// mark as local loopback session
 		tResult.mLocalLoopback = true;
 		
 		// activate the loopback connection
 		tResult.startConnection(pHRMController.getNodeL2Address(), null);
 		
 		return tResult;
 	}
 	
 	/**
 	 * Sends a packet to along the connection 
 	 * 
 	 * @param pData is the data that should be sent
 	 * @return true if success, otherwise false
 	 */
 	public boolean write(Serializable pData)
 	{
 		boolean tResult = false;
 		
 		if (pData instanceof MultiplexHeader){
 			MultiplexHeader tMultiplexHeader = (MultiplexHeader)pData;
 			
 			if (HRMConfig.DebugOutput.GUI_SHOW_MULTIPLEX_PACKETS){
 				Logging.log(this, "SENDING MULTIPLEX HEADER: " + tMultiplexHeader  + ", payload=" + tMultiplexHeader.getPayload());
 			}
 			
 			if(tMultiplexHeader.getPayload() instanceof SignalingMessageHrm){
 				SignalingMessageHrm tSignalingHRMPacket = (SignalingMessageHrm)tMultiplexHeader.getPayload();
 				
 				// add source route entry
 				tSignalingHRMPacket.addSourceRoute("[S]: " + this.toString());
 			}
 		}
 		
 		if(!mLocalLoopback){
 			if(mParentConnection != null && mParentConnection.isConnected()) {
 				try	{
 					if(HRMConfig.DebugOutput.SHOW_SENT_SESSION_PACKETS){
 						Logging.log(this, "SENDING PACKET: " + pData.getClass().getSimpleName());
 					}
 	
 					mParentConnection.write(pData);
 					tResult = true;
 				} catch (NetworkException tExc) {
 					Logging.err(this, "Unable to send " + pData + " because write operation failed", tExc);
 				}
 			} else {
 				Logging.err(this, "Unable to send " + pData + " because of invalid connection: " + mParentConnection);
 			}
 		}else{
 			if(HRMConfig.DebugOutput.SHOW_SENT_SESSION_PACKETS){
 				Logging.log(this, "SENDING local (per loopback) PACKET: " + pData.getClass().getSimpleName());
 			}
 			receiveData(pData);
 			tResult = true;
 		}
 		
 		return tResult;
 	}
 
 	/**
 	 * Determines the route to the peer (its central FN)
 	 *  
 	 * @return the route to the central FN of the peer
 	 */
 	public Route getRouteToPeer()
 	{
 		return mRouteToPeer;
 	}
 
 	/**
 	 * EVENT: the route to the peer was announced, triggered by ourself if a AnnouncePhysicalEndPoint packet was received, we react by:
 	 * 		1.) store the route to the peer (to its central FN)
 	 * 
 	 * @param pRouteToPeer the route to the peer
 	 */
 	private void eventRouteToPeerAvailable(Route pRouteToPeer)
 	{
 		Logging.log(this, "Setting route to peer " + getPeerL2Address() + " as " + pRouteToPeer);
 		if(mRouteToPeer == null){
 			Logging.log(this, "OLD ROUTE TO PEER WAS INVALID");
 		}
 		
 		/**
 		 * Inform the HRS about the complete route to the peer
 		 */
 		Logging.log(this, "      ..registering route to peer: " + pRouteToPeer);
 		mHRMController.registerLinkL2(mPeerL2Address, pRouteToPeer);
 		
 		mRouteToPeer = pRouteToPeer;
 	}
 	
 	/**
 	 * Sets new L2Address for peer (central FN)
 	 * 
 	 * @param pL2Address the new L2Address
 	 */
 	private void setPeerL2Address(L2Address pL2Address)
 	{
 		mPeerL2Address = pL2Address;
 	}
 	
 	/**
 	 * Returns the L2Address of the peer (central FN)
 	 * 
 	 * @return the peer L2Address (central FN)
 	 */
 	public L2Address getPeerL2Address()
 	{
 		return mPeerL2Address;
 	}
 	
 	/**
 	 * Returns a reference to the local HRMController application
 	 * 
 	 * @return reference to the HRMController application
 	 */
 	public HRMController getHRMController()
 	{
 		return mHRMController;
 	}
 	
 	/**
 	 * Registers a communication channel
 	 * 
 	 * @param pComChannel the communication channel, which should be registered
 	 */
 	public void registerComChannel(ComChannel pComChannel)
 	{
 		Logging.log(this, "Registering communication channel: " + pComChannel);
 		
 		synchronized (mRegisteredComChannels) {
 			mRegisteredComChannels.add(pComChannel);			
 		}
 	}
 	
 	/**
 	 * Registers a communication channel
 	 * 
 	 * @param pComChannel the communication channel, which should be registered
 	 */
 	public void unregisterComChannel(ComChannel pComChannel)
 	{
 		Logging.log(this, "Unregistering communication channel: " + pComChannel);
 		
 		boolean tLastChannelClosed = false; //needed because of mutex usage below
 		synchronized (mRegisteredComChannels) {
 			mRegisteredComChannels.remove(pComChannel);
 			
 			if (mRegisteredComChannels.size() == 0){
 				Logging.log(this, "    ..last inferior comm. channel was unregistered");
 
 				tLastChannelClosed = true;
 			}
 		}
 		
 		synchronized (mUnregisteredComChannels) {
 			mUnregisteredComChannels.add(pComChannel);
 		}
 		
 		if(tLastChannelClosed){
 			/**
 			 * Trigger the event "all channels lost"
 			 */
 			eventAllChannelsClosed();
 		}		
 	}
 	
 	/**
 	 * Returns all registered communication channels
 	 * 
 	 * @return the list of known communication channels
 	 */
 	@SuppressWarnings("unchecked")
 	public LinkedList<ComChannel> getAllComChannels()
 	{
 		LinkedList<ComChannel> tResult = new LinkedList<ComChannel>();
 		
 		synchronized (mRegisteredComChannels) {
 			tResult = (LinkedList<ComChannel>) mRegisteredComChannels.clone();
 		}
 		
 		return tResult;
 	}
 
 	/**
 	 * Searches for a registered communication channel which is identified by its local clusterID
 	 * 
 	 * @param pDestinationClusterName the destination ClusterName
 	 * 
 	 * @return the found comm. channel or null
 	 */
 	private ComChannel getComChannel(ClusterName pDestinationClusterName, ClusterName pSourceClusterName)
 	{
 		ComChannel tResult = null;
 		
 		LinkedList<ComChannel> tComChannels = getAllComChannels();
 		for (ComChannel tComChannel : tComChannels){
 			if((tComChannel.getParent().getClusterID().equals(pDestinationClusterName.getClusterID())) && 
 			   (tComChannel.getParent().getHierarchyLevel().equals(pDestinationClusterName.getHierarchyLevel())) &&
 			   (tComChannel.getRemoteClusterName().getClusterID().equals(pSourceClusterName.getClusterID())) && 
 			   (tComChannel.getRemoteClusterName().getHierarchyLevel().equals(pSourceClusterName.getHierarchyLevel()))) {
 				tResult = tComChannel;
 				break;
 			}
 		}
 		
 		return tResult;
 	}
 	
 	/**
 	 * Searches for an unregistered communication channel which is identified by its local clusterID
 	 * 
 	 * @param pDestinationClusterName the destination ClusterName
 	 * 
 	 * @return the found comm. channel or null
 	 */
 	private ComChannel getDeletedComChannel(ClusterName pDestinationClusterName, ClusterName pSourceClusterName)
 	{
 		ComChannel tResult = null;
 		
 		synchronized (mUnregisteredComChannels) {
 			for (ComChannel tComChannel : mUnregisteredComChannels){
 				if((tComChannel.getParent().getClusterID().equals(pDestinationClusterName.getClusterID())) && 
 				   (tComChannel.getParent().getHierarchyLevel().equals(pDestinationClusterName.getHierarchyLevel())) &&
 				   (tComChannel.getRemoteClusterName().getClusterID().equals(pSourceClusterName.getClusterID())) && 
 				   (tComChannel.getRemoteClusterName().getHierarchyLevel().equals(pSourceClusterName.getHierarchyLevel()))) {
 					tResult = tComChannel;
 					break;
 				}
 			}
 		}
 		
 		return tResult;
 	}
 
 	/**
 	 * This function gets called when the physical end point at remote side is locally known
 	 */
 	private void eventSessionAvailable()
 	{
 		Logging.log(this, "EVENT: session is available now");
 	}
 
 	/**
 	 * EVENT: all inferior channels were closed
 	 */
 	private void eventAllChannelsClosed()
 	{
 		Logging.log(this, "EVENT: all channels are closed");
 		
 		if(!mLocalLoopback){
 			if (HRMConfig.Hierarchy.AUTO_CLEANUP_FOR_CONNECTIONS){
 				Logging.log(this, "\n\n\n########### Closing the parent connection(destination=" + mPeerL2Address + ", requirements=" + mParentConnection.getRequirements() + ")");
 				
 				// register at the HRMController as incoming or outgoing session
 				if (mIncomingConnection){
 				    mHRMController.unregisterIncomingSession(this);
 				}else{
 				    mHRMController.unregisterOutgoingSession(this);
 				}
 
 				//stop the session (closes the connection)
 				stop();
 			}
 		}
 	}
 
 	/**
 	 * EVENT: MultiplexHeader
 	 * This function handles a multiplex-header of received packets by delivering the packet payload as signaling packet to the correct comm. channel.
 	 * 
 	 * @param pMultiplexHeader the multiplex-header
 	 */
 	private void eventMultiplexPacket(MultiplexHeader pMultiplexHeader)
 	{
 		/**
 		 * Get the target from the Multiplex-Header
 		 */
 		ClusterName tDestination = pMultiplexHeader.getReceiverClusterName();
 
 		/**
 		 * Get the source from the Multiplex-Header
 		 */
 		ClusterName tSource = pMultiplexHeader.getSenderClusterName();
 
 		if (HRMConfig.DebugOutput.GUI_SHOW_MULTIPLEX_PACKETS){
 			Logging.log(this, "RECEIVING MULTIPLEX HEADER with destination: " + tDestination  + ", payload=" + pMultiplexHeader.getPayload());
 		}
 		
 		/**
 		 * Iterate over all communication channels and find the correct channel towards the destination
 		 */
 		ComChannel tDestinationComChannel = getComChannel(tDestination, tSource);
 		if (HRMConfig.DebugOutput.GUI_SHOW_MULTIPLEX_PACKETS){
 			Logging.log(this, "       ..found communication channel: " + tDestinationComChannel);
 		}
 
 		/**
 		 * Get the payload
 		 */
 		SignalingMessageHrm tPayload = pMultiplexHeader.getPayload();
 
 		/**
 		 * Forward the payload to the correct communication channel
 		 */
 		if (tDestinationComChannel != null){
 			if (HRMConfig.DebugOutput.GUI_SHOW_MULTIPLEX_PACKETS){
 				Logging.log(this, "       ..delivering received payload: " + tPayload);
 			}
 
 			// finally, forward the payload
 			tDestinationComChannel.receivePacket(tPayload);
 		} else {
 			ComChannel tDeletedComChannel = getDeletedComChannel(tDestination, tSource);
 			if (tDeletedComChannel != null){
 				Logging.warn(this, "Due to already deleted communication channel, dropping packet: " + pMultiplexHeader + ", old comm. channel is: " + tDeletedComChannel);
 			}else{
 				if (mHRMController.isGUIFormerCoordiantorID(tDestination.getGUICoordinatorID())){
 					Logging.warn(this, "Due to already deleted coordinator, dropping packet: " + pMultiplexHeader + ", old coordinator had ID: " + tDestination.getGUICoordinatorID());
 				}else{
 					String tKnownChannels = "";
 					for (ComChannel tComChannel: getAllComChannels()){
 						tKnownChannels += "\n      .." + tComChannel.toString() + " [Peer=" + tComChannel.getRemoteClusterName() + "]";
 					}
 					throw new RuntimeException("\n" + this + " >> is unable to find the communication channel\n   ..packet destination: " + tDestination + "\n   ..packet source: " + tSource + " @" + pMultiplexHeader.getPayload().getSenderName() + "\n   ..known communication channels are: " + tKnownChannels + "\n   ..known deleted channels are: " + mUnregisteredComChannels + "\n   ..dropped packet payload: " + pMultiplexHeader.getPayload());
 				}
 			}
 		}
 	}
 
 	/**
 	 * EVENT: RequestClusterMembership
 	 * 
 	 * @param pRequestClusterMembershipPacket the request packet
 	 */
 	private void eventRequestClusterMembership(RequestClusterMembership pRequestClusterMembershipPacket)
 	{
 		Logging.log(this, "EVENT: RequestClusterMembership: " + pRequestClusterMembershipPacket);
 		
 		/**
 		 * Is the requester located at a higher hierarchy level? ==> a coordinator is addressed, which should be member of the remote Cluster object
 		 */ 
 		if (pRequestClusterMembershipPacket.getRequestingCluster().getHierarchyLevel().isHigherLevel()){
 			int tTargetCoordinatorID = pRequestClusterMembershipPacket.getDestination().getCoordinatorID();
 
 			// check the coordinator ID
 			if (tTargetCoordinatorID > 0){
 				
 				/**
 				 * Search for the coordinator and inform him about the cluster membership request
 				 */
 				Coordinator tCoordinator = mHRMController.getCoordinatorByID(tTargetCoordinatorID);
 				
 				// is the parent a coordinator or a cluster?
 				if (tCoordinator != null){
 					tCoordinator.eventClusterMembershipRequest(pRequestClusterMembershipPacket.getRequestingCluster(), this);
 				}else{
 					Logging.warn(this, "receiveData() couldn't find the target coordinator for the incoming RequestClusterMembership packet: " + pRequestClusterMembershipPacket + ", coordinator has gone in the meanwhile?");
 					denyClusterMembershipRequest(pRequestClusterMembershipPacket.getRequestingCluster(), pRequestClusterMembershipPacket.getDestination());
 				}
 			}else{
 				Logging.err(this, "Detected an invalid coordinator ID in the cluster membrship request: " + pRequestClusterMembershipPacket);
 			}
 		}else{// the requester is located at base hierarchy level -> a new ClusterMember object has to be created, which should be member of the remote Cluster object
 			/**
 			 * Create ClusterName for the signaled cluster
 			 */
 			ClusterName tSignaledClusterName = new ClusterName(mHRMController, pRequestClusterMembershipPacket.getDestination().getHierarchyLevel(), pRequestClusterMembershipPacket.getDestination().getClusterID(), -1);
 
 			/**
 			 * Create new cluster member object
 			 */
 			Logging.log(this, "    ..creating new local cluster member for: " + tSignaledClusterName); 
 			ClusterMember tClusterMember = ClusterMember.create(mHRMController, tSignaledClusterName, null);
 
 			/**
 			 * Trigger: "cluster membership request" within the new ClusterMember object
 			 */
 			tClusterMember.eventClusterMembershipRequest(pRequestClusterMembershipPacket.getRequestingCluster(), this);
 		}
 	}
 
 	/**
 	 * EVENT: AnnouncePhysicalNeighborhood
 	 * 
 	 * @param pAnnouncePhysicalNeighborhood the packet
 	 */
 	private void eventAnnouncePhysicalEndPoint(AnnouncePhysicalEndPoint pAnnouncePhysicalNeighborhood)
 	{
 		// get the L2Address of the peer
 		setPeerL2Address(pAnnouncePhysicalNeighborhood.getSenderCentralAddress());
 
 		// get the L2Address of the peer, which should be used as routing target
 		L2Address tSenderAddress = pAnnouncePhysicalNeighborhood.getSenderAddress();
 		
 		if (tSenderAddress != null){
 			/**
 			 * Determine the route to the known FN from the peer
 			 */
 			Route tRouteToPeer = null;
 			// search a route form the central FN to the intermediate FN between the central FN and the bus
 			try {
 				tRouteToPeer = mHRMController.getHRS().getRoute(tSenderAddress, new Description(), getHRMController().getNode().getIdentity());
 			} catch (RoutingException tExc) {
 				Logging.err(this, "Unable to find route to ", tExc);
 			} catch (RequirementsException tExc) {
 				Logging.err(this, "Unable to fulfill requirements ", tExc);
 			}
 			
 			if(tRouteToPeer != null){
 				/**
 				 * Enlarge the found route by an L2Address in order to have a route which ends at the central FN of the peer node, but avoid trailing duplicates 
 				 */
 				boolean tPeerL2AddressAlreadyKnown = false;
 				// get the last entry of the known route to the peer
 				RouteSegment tRouteToPeerLastEntry = tRouteToPeer.getLast();
 				// is the last entry an address?
 				if (tRouteToPeerLastEntry instanceof RouteSegmentAddress){
 					RouteSegmentAddress tRouteToPeerLastAddress = (RouteSegmentAddress)tRouteToPeerLastEntry;
 					// is the last entry an L2Address? 
 					if(tRouteToPeerLastAddress.getAddress() instanceof L2Address){
 						// get the L2Address from the last entry
 						L2Address tLastAddress = (L2Address)tRouteToPeerLastAddress.getAddress();
 						// is the found L2Address the same like what we would add in the next step?
 						if(tLastAddress.equals(mPeerL2Address)){
 							tPeerL2AddressAlreadyKnown = true;
 						}
 					}
 				}
 				// should we add the peer L2Address as last entry in the route towards the peer?
 				if(!tPeerL2AddressAlreadyKnown){
 					Logging.log(this, ">>> Old route to peer was: " + tRouteToPeer);
 					// add the peer L2Address as last entry in the route to the peer node
 					tRouteToPeer.add(new RouteSegmentAddress(mPeerL2Address));
 					Logging.log(this, ">>> New route to peer is: " + tRouteToPeer);
 					
 					eventRouteToPeerAvailable(tRouteToPeer);
 				}else{
 					Logging.log(this, ">>> Old route to peer: " + tRouteToPeer + " includes already the entry " + mPeerL2Address + " as last entry");
 				}
 			}else{
 				Logging.warn(this, "Couldn't determine the route to the peer: " + tSenderAddress);
 			}
 		}
 		
 		/**
 		 * Send an answer packet
 		 */
 		if (!pAnnouncePhysicalNeighborhood.isAnswer()){
 			/**
 			 * get the name of the central FN
 			 */
 			L2Address tCentralFNL2Address = mHRMController.getHRS().getCentralFNL2Address();
 
 			/**
 			 *  determine the FN between the local central FN and the bus towards the physical neighbor node and tell this the neighbor 
 			 */
 			L2Address tFirstFNL2Address = (tSenderAddress != null ? mHRMController.getL2AddressOfFirstFNTowardsNeighbor(tSenderAddress) : null);
 
 			/**
 			 * Send AnnouncePhysicalNeighborhood to the neighbor
 			 */
 			if (tFirstFNL2Address == null){
 				Logging.warn(this, "handleAnnouncePhysicalEndPoint() wasn't able to determine the first FN towards: " + tSenderAddress);
 			}
 			// create a map between the central FN and the search FN
 			AnnouncePhysicalEndPoint tAnnouncePhysicalNeighborhoodAnswer = new AnnouncePhysicalEndPoint(tCentralFNL2Address, tFirstFNL2Address, AnnouncePhysicalEndPoint.ANSWER_PACKET);
 			// tell the neighbor about the FN
 			if (HRMConfig.DebugOutput.GUI_SHOW_TOPOLOGY_DETECTION){
 				Logging.log(this, "     ..sending ANNOUNCE PHYSICAL NEIGHBORHOOD ANSWER " + tAnnouncePhysicalNeighborhoodAnswer);
 			}
 			write(tAnnouncePhysicalNeighborhoodAnswer);
 		}
 		
 		/**
 		 * TRIGGER: session is available now
 		 */
 		eventSessionAvailable();		
 	}
 
 	/**
 	 * Deny a ClusterMembership request by sending 
 	 * @param pDestination the destination
 	 * @param pSource the source
 	 */
	private void denyClusterMembershipRequest(ClusterName pDestination, ClusterName pSource)
 	{
 		Logging.log(this, "Denying RequestClusterMembership from: " + pDestination);
 		
 		/**
 		 * Create "InformClusterLeft" packet
 		 */
 		InformClusterLeft tInformClusterLeft = new InformClusterLeft(mHRMController.getNodeName(), null, null, null);
 	    
 		// add source route entry
 		tInformClusterLeft.addSourceRoute("[S]: " + this.toString());
 		
 		/**
 		 * Create "MultiplexHeader"
 		 */
 		ClusterName tSignaledSourceClusterName = new ClusterName(mHRMController, new HierarchyLevel(this, pSource.getHierarchyLevel().getValue() + 1 /* we answer for a CoordinatorAsClusterMember instance which is always one level higher than its parent Coordinator instance*/), pSource.getClusterID(), pSource.getCoordinatorID());
 		MultiplexHeader tMultiplexPacket = new MultiplexHeader(tSignaledSourceClusterName, pDestination, tInformClusterLeft);
 
 		/**
 		 * Send the final packet
 		 */
 		Logging.log(this, "       ..sending cluster left: " + tMultiplexPacket + ", payload=" + tMultiplexPacket.getPayload());
 		write(tMultiplexPacket);
 	}
 
 	/**
 	 * Processes incoming packet data and forward it to the right ComChannel
 	 * 
 	 * @param pData the packet payload
 	 */	
 	@Override
 	public boolean receiveData(Object pData)
 	{
 		if(pData == null){
 			Logging.err(this, "Received invalid data");
 			return true;
 		}
 		
 		if(HRMConfig.DebugOutput.SHOW_RECEIVED_SESSION_PACKETS){
 			Logging.log(this, "RECEIVED PACKET: " + pData.getClass().getSimpleName());
 		}
 		
 		/**
 		 * AnnouncePhysicalNeighborhood:
 		 * 			ComSession ==> ComSession
 		 */
 		if(pData instanceof AnnouncePhysicalEndPoint) {
 			// get the packet
 			AnnouncePhysicalEndPoint tAnnouncePhysicalNeighborhood = (AnnouncePhysicalEndPoint)pData;
 
 			if (HRMConfig.DebugOutput.GUI_SHOW_TOPOLOGY_DETECTION){
 				Logging.log(this, "ANNOUNCE PHYSICAL NEIGHBORHOOD received: " + tAnnouncePhysicalNeighborhood);
 			}
 			
 			eventAnnouncePhysicalEndPoint(tAnnouncePhysicalNeighborhood);
 
 			return true;
 		} 
 		
 		/**
 		 * RequestClusterMembership:
 		 * 			L0: a node (HRMController) ==> other node (HRMController)
 		 *  		L1+: Cluster at level n ==> Coordinator at level (n-1)
 		 */
 		if(pData instanceof RequestClusterMembership) {
 			RequestClusterMembership tRequestClusterMembershipPacket = (RequestClusterMembership)pData;
 
 			if (HRMConfig.DebugOutput.GUI_SHOW_TOPOLOGY_DETECTION){
 				Logging.log(this, "REQUEST_CLUSTER_MEMBERSHIP-received from \"" + tRequestClusterMembershipPacket.getRequestingCluster());
 			}
 
 			eventRequestClusterMembership(tRequestClusterMembershipPacket);
 			
 			return true;
 		}
 
 		/**
 		 * MultiplexHeader:
 		 * 			ComChannel ==> ComChannel 
 		 */
 		if (pData instanceof MultiplexHeader) {
 			MultiplexHeader tMultiplexPacket = (MultiplexHeader) pData;
 			
 			if (HRMConfig.DebugOutput.GUI_SHOW_MULTIPLEX_PACKETS){
 				Logging.log(this, "MULTIPLEX PACKET received: " + tMultiplexPacket);
 			}
 			
 			/**
 			 * SignalingMessageHRM
 			 */
 			if(tMultiplexPacket.getPayload() instanceof SignalingMessageHrm){
 				SignalingMessageHrm tSignalingHRMPacket = tMultiplexPacket.getPayload();
 				
 				// add source route entry
 				tSignalingHRMPacket.addSourceRoute("[R]: " + this.toString());
 			}
 
 			eventMultiplexPacket(tMultiplexPacket);
 
 			return true;
 		}
 
 		Logging.warn(this, ">>>>>>>>>>>>> Found unsupported packet: " + pData);
 		return true;
 	}
 
 	/**
 	 * Starts the superior connection by the following steps:
 	 *     1.) call "start()" from the superior class
 	 *     2.) store the L2Address of the peer
 	 *     3.) store the route to the peer
 	 *     4.) announce the local physical end point to the peer
 	 *     
 	 * @param pTargetL2Address the L2Address of the target, which should be used as routing target
 	 * @param pConnection the superior connection
 	 */
 	public synchronized void startConnection(L2Address pTargetL2Address, Connection pConnection)
 	{
 		Logging.log(this, "\n\n###### STARTING connection for target: " + pTargetL2Address);
 		
 		if(!mLocalLoopback){
 			if(pConnection != null){
 				// store the connection
 				mParentConnection = pConnection;
 				
 				/**
 				 * Calls "start()" of the superior class
 				 */
 				start(mParentConnection);
 			}else{
 				Logging.err(this, "Given connection is null");
 			}
 		}
 
 		// do we know the target L2Address?
 		if (pTargetL2Address != null){
 			/**
 			 * Update the peer L2Address
 			 */
 			setPeerL2Address(pTargetL2Address);
 			
 //TODO: remove the following? it stores a route to the first FN towards the peer -> but has bad side-effects on the ARG quality, and it isn't needed for a working HRM infrastructure			
 //			/**
 //			 * Find and set the route to peer within the session object
 //			 */
 //			Route tRouteToNeighborFN = null;
 //			// get a route to the neighbor node (the destination of the desired connection)
 //			try {
 //				tRouteToNeighborFN = mHRMController.getHRS().getRoute(pTargetL2Address, new Description(), mHRMController.getNode().getIdentity());
 //			} catch (RoutingException tExc) {
 //				Logging.err(mHRMController, "Unable to find route to " + pTargetL2Address, tExc);
 //			} catch (RequirementsException tExc) {
 //				Logging.err(mHRMController, "Unable to find route to " + pTargetL2Address + " with requirements no requirents, Huh!", tExc);
 //			}
 //			// have we found a route to the neighbor?
 //			if(tRouteToNeighborFN != null) {
 //				/**
 //				 * Complete the found route to a route which ends at the first FN of the peer node towards its central FN
 //				 */
 //				tRouteToNeighborFN.add(new RouteSegmentAddress(mPeerL2Address));
 //				if(mHierarchyLevel.isBaseLevel()) {
 //					Logging.log(this, "      ..registering route to peer first FN: " + tRouteToNeighborFN);
 //					mHRMController.addRouteToDirectNeighbor(mPeerL2Address, tRouteToNeighborFN);
 //				}
 //			}
 		}else{
 			Logging.trace(this, "startConnection() doesn't know the target L2Address, will send the local L2Address to the peer");
 		}
 
 		/**
 		 * announce physical end point
 		 */
 		L2Address tFirstFNL2Address = (pTargetL2Address != null ? mHRMController.getL2AddressOfFirstFNTowardsNeighbor(pTargetL2Address) : null);
 		// HINT: if tFirstFNL2Address is null we send a blind announce to inform the peer about our L2Address
 		// get the name of the central FN
 		L2Address tCentralFNL2Address = mHRMController.getHRS().getCentralFNL2Address();
 		// create a map between the central FN and the search FN
 		AnnouncePhysicalEndPoint tAnnouncePhysicalEndPoint = new AnnouncePhysicalEndPoint(tCentralFNL2Address, tFirstFNL2Address, AnnouncePhysicalEndPoint.INIT_PACKET);
 		// tell the neighbor about the FN
 		Logging.log(mHRMController, "     ..sending ANNOUNCE PHYSICAL NEIGHBORHOOD");
 		if(write(tAnnouncePhysicalEndPoint)){
 			Logging.log(mHRMController, "     ..sent ANNOUNCE PHYSICAL NEIGHBORHOOD: " + tAnnouncePhysicalEndPoint);	
 		}else{
 			Logging.err(mHRMController, "     ..unable to send ANNOUNCE PHYSICAL NEIGHBORHOOD: " + tAnnouncePhysicalEndPoint);	
 		}
 	}
 
 	public synchronized void stopConnection()
 	{
 		/**
 		 * close all comm. channels
 		 */
 		while(mRegisteredComChannels.size() > 0)
 		{
 			mRegisteredComChannels.getLast().closeChannel();
 		}
 		
 		/**
 		 * Session::close() will be automatically called by the last closeChannel() call
 		 */
 	}
 	
 	/**
 	 * Returns true if this session is running.
 	 * 
 	 * @return true or false
 	 */
 	public boolean isRunning()
 	{
 		if(mParentConnection != null){
 			if(mParentConnection.isConnected()){
 				return true;
 			}
 		}
 		
 		return false;
 	}
 	
 	/**
 	 * Handles error events for the parent connection
 	 * 
 	 * @param pExc the exception which caused the error
 	 */
 	@Override
 	public void error(Exception pExc)
 	{
 		throw new RuntimeException("Error occurred, stack trace is: " + pExc.toString());
 	}
 
 	/**
 	 * Descriptive string about this object
 	 * 
 	 * @return the descriptive string
 	 */
 	public String toString()
 	{
 		if(getPeerL2Address() != null ) {
 			return getClass().getSimpleName() + "@" + mHRMController.getNodeGUIName() + (mLocalLoopback ? "@LOOP" : "") + "(Peer=" + getPeerL2Address() + (isRunning() ? ", RUNNING" : "") + ")";
 		} else {
 			return getClass().getSimpleName() + "@" + mHRMController.getNodeGUIName() + (mLocalLoopback ? "@LOOP" : "");
 		}
 		 
 	}
 }
