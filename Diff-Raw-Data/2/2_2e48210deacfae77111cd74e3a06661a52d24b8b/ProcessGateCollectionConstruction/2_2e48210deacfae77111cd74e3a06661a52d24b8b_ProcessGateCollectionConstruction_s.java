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
 package de.tuilmenau.ics.fog.transfer.manager;
 
 import java.util.LinkedList;
 
 import de.tuilmenau.ics.fog.Config;
 import de.tuilmenau.ics.fog.IEvent;
 import de.tuilmenau.ics.fog.exceptions.CreationException;
 import de.tuilmenau.ics.fog.facade.Binding;
 import de.tuilmenau.ics.fog.facade.Connection;
 import de.tuilmenau.ics.fog.facade.Description;
 import de.tuilmenau.ics.fog.facade.Identity;
 import de.tuilmenau.ics.fog.facade.Name;
 import de.tuilmenau.ics.fog.facade.NetworkException;
 import de.tuilmenau.ics.fog.facade.properties.PropertyException;
 import de.tuilmenau.ics.fog.packets.Packet;
 import de.tuilmenau.ics.fog.packets.PleaseOpenConnection;
 import de.tuilmenau.ics.fog.routing.Route;
 import de.tuilmenau.ics.fog.transfer.ForwardingElement;
 import de.tuilmenau.ics.fog.transfer.ForwardingNode;
 import de.tuilmenau.ics.fog.transfer.forwardingNodes.ClientFN;
 import de.tuilmenau.ics.fog.transfer.forwardingNodes.GateContainer;
 import de.tuilmenau.ics.fog.transfer.gates.AbstractGate;
 import de.tuilmenau.ics.fog.transfer.gates.GateID;
 import de.tuilmenau.ics.fog.transfer.gates.HorizontalGate;
 import de.tuilmenau.ics.fog.transfer.gates.roles.Horizontal;
 import de.tuilmenau.ics.fog.transfer.gates.roles.IFunctionDescriptor;
 import de.tuilmenau.ics.fog.transfer.gates.roles.Transparent;
 import de.tuilmenau.ics.fog.transfer.manager.path.SocketPathCreator;
 import de.tuilmenau.ics.fog.transfer.manager.path.SocketPathParam;
 import de.tuilmenau.ics.fog.ui.Viewable;
 
 
 /**
  * 
  */
 public class ProcessGateCollectionConstruction extends ProcessConstruction
 {
 	private static final double MAX_NUMBER_RETRIES_SIGNALING = 4.0d;
 	
 	
 	public ProcessGateCollectionConstruction(ForwardingNode pBase, ForwardingNode pOutgoing, Description pRequirements, Identity pOwner)
 	{
 		super(pBase, pOutgoing, pRequirements, pOwner);
 	}
 	
 	/**
 	 * Constructs a {@link ClientFN} to connect it later.
 	 * 
 	 * @see de.tuilmenau.ics.fog.transfer.manager.ProcessSocketConstruction#create()
 	 */
 	@Override
 	public void start() throws NetworkException
 	{
 		if((mPeer == null) || (getBase() == null)) {
 			throw new NetworkException(this, "Invalid parameters for creating gates for requirements " +mDescription +".");
 		}
 		
 		mLogger.log(this, "Creating gates at " +getBase() +" to " +mPeer +" for requirements " +mDescription);
 		
 		super.start();
 	}
 	
 	public void signal(boolean calculateDescrForRemoteSystem, Route route) throws PropertyException
 	{
 		// Calculate description for request to remote system.
 		// Functional requirements can differ (initiator <> responder).
 		// Make copy here, if someone changes the given one later on.
 		Description tRequestDescription = getDescription();
 		if(tRequestDescription != null) {
 			if(calculateDescrForRemoteSystem) {
 				tRequestDescription = tRequestDescription.calculateDescrForRemoteSystem();
 			}
 		} else {
 			// avoid null pointer; use empty description
 			tRequestDescription = new Description();
 		}
 
 		Identity requester = getOwner();
 		PleaseOpenConnection tPleaseOpenConnection = new PleaseOpenConnection(null, this, tRequestDescription, calculateDescrForRemoteSystem);
 		Packet packet = new Packet(route, tPleaseOpenConnection);
 		
 		// sign it with the credentials of the process owner
 		getBase().getEntity().getAuthenticationService().sign(packet, requester);
 		
 		signal(packet);
 	}
 	
 	private void signal(Packet packet)
 	{
 		// re-send cached message?
 		if(packet == null) {
 			packet = mCachedSignalingMessage;
 		} else {
 			// make copy to protect message from changes
 			mCachedSignalingMessage = packet.clone();
 		}
 		
 		if(packet != null) {
 			// send signaling message
 			getBase().handlePacket(packet, null);
 			
 			// event for re-send signaling message
 			IEvent event = new IEvent() {
 				@Override
 				public void fire()
 				{
 					if(!isFinished()) {
 						if(!isOperational()) {
							getLogger().warn(this, "Timeout for re-sending the signaling message.");
 							
 							signal(null);
 						}
 					}
 				}
 			};
 			
 			// do it after sending the message. If we are not executed in the event loop, we would risk to be executed immediately otherwise.
 			getTimeBase().scheduleIn(Config.PROCESS_STD_TIMEOUT_SEC / (MAX_NUMBER_RETRIES_SIGNALING +1.0d), event);
 		} else {
 			getLogger().err(this, "Try to re-send signaling message, but no message in cache.");
 		}
 	}
 	
 	public void disableHorizontal()
 	{
 		mCreateHorizontal = false;
 	}
 	
 	/**
 	 * Constructs the missing gates and forwarding nodes really needed to
 	 * connect to the socket ({@link ClientFN}).
 	 * 
 	 * @param pDescription The description with (differing) requirements.
 	 * 
 	 * @param pReturnRouteFromLocalBaseFN The route starting at local base FN
 	 * and ending at remote base FN, may be needed for some gates.
 	 * 
 	 * @throws NetworkException on errors
 	 */
 	public void recreatePath(Description pDescription, Route pReturnRouteFromLocalBaseFN) throws NetworkException
 	{
 		/*
 		 * Check whether rebuild is necessary.
 		 */
 		boolean rebuild = (getClientEnteringGate() == null);
 		
 		if(!rebuild) {
 			rebuild = !mDescription.equals(pDescription);
 		}
 		
 		//TODO Use the given Routes to remotes base FN and up to his client.
 		
 		if(rebuild) {
 			// Use the new descriptions for new path.
 			if(pDescription != null) {
 				mDescription = pDescription;
 			}
 			
 			LinkedList<AbstractGate> tOldPath = new LinkedList<AbstractGate>();
 			tOldPath.addAll(mGatesFromBaseToSocket);
 			tOldPath.addAll(mGatesFromSocketToBase);
 			LinkedList<SocketPathParam> paramList = new LinkedList<SocketPathParam>();
 			
 			/*
 			 * De-construct old path.
 			 * 
 			 * TODO De-construction should be done differentiated to reuse gates.
 			 */
 			
 			for(AbstractGate tGate : tOldPath) {
 				if(tGate != null) {
 					
 					GateID tGateID = tGate.getGateID();
 					// Do not remove base FN nor outgoing FN but all other FNs.
 					boolean tRemoveTargetFN = (tGate.getNextNode() != mPeer) && (tGate.getNextNode() != getBase());
 					
 					SocketPathParam tPathParam = new SocketPathParam(tGateID, true, null, tRemoveTargetFN);
 					paramList.addLast(tPathParam);
 				}
 			}
 			
 			/*
 			 * Collect functional requirements and create parameters.
 			 */
 			
 			LinkedList<SocketPathParam> tListUp = new LinkedList<SocketPathParam>();
 			LinkedList<SocketPathParam> tListDown = new LinkedList<SocketPathParam>();
 			
 			IFunctionDescriptor tFuncUp = null;
 			IFunctionDescriptor tFuncDown = null;
 			SocketPathParam pathParamUp = null;
 			SocketPathParam pathParamDown = null;
 			try {
 				mIntermediateDescription = getBase().getEntity().getController().deriveGatesFromRequirements(getBase(), mDescription, tListUp, tListDown);
 			}
 			catch(NetworkException tExc) {
 				throw new NetworkException("Can not derive gates from requirements " +mDescription +".", tExc);
 			}
 			
 			/*
 			 * Create parameters for gates connecting to client FN.
 			 */
 			if(mCreateHorizontal) {
 				tFuncUp = Transparent.PURE_FORWARDING;
 				tFuncDown = Horizontal.TUNNEL;
 
 				pathParamUp = new SocketPathParam(/*GateID*/null, mPeer, tFuncUp, null);
 				pathParamDown = new SocketPathParam(/*GateID*/null, /*FN_Name*/null, tFuncDown, null);
 				pathParamUp.setLocalPartnerParam(pathParamDown);
 				pathParamDown.setLocalPartnerParam(pathParamUp);
 				tListUp.addLast(pathParamUp);
 				tListDown.addFirst(pathParamDown);
 			} else {
 				pathParamUp = tListUp.removeLast();
 				pathParamDown = tListDown.removeFirst();
 				
 				// make copy with different target/source
 				SocketPathParam pathParamUpNew = new SocketPathParam(pathParamUp.getGateID(), mPeer, pathParamUp.getFunctionDescriptor(), pathParamUp);
 				SocketPathParam pathParamDownNew = new SocketPathParam(pathParamDown.getGateID(), mPeer, pathParamDown.getFunctionDescriptor(), pathParamDown);
 
 				pathParamUpNew.setLocalPartnerParam(pathParamDownNew);
 				pathParamDownNew.setLocalPartnerParam(pathParamUpNew);
 
 				tListUp.addLast(pathParamUpNew);
 				tListDown.addFirst(pathParamDownNew);
 			}
 			
 			/*
 			 * Put both lists together.
 			 */
 			
 			paramList.addAll(tListUp);
 			paramList.addAll(tListDown);
 			
 			/*
 			 * Run path-creation.
 			 */
 			
 			try {
 				paramList = SocketPathCreator.createPath(getBase(), paramList, tOldPath, getOwner());
 			} catch (CreationException ce) {
 				mLogger.err(this, "Exception during path creation.", ce);
 				// All gates and FNs in path will be removed.
 				// -> Tidy up the members!
 				mGatesFromBaseToSocket.clear();
 				mGatesFromSocketToBase.clear();
 				// Throw error to caller.
 				throw new NetworkException("Connection failed.");
 			}
 			
 			if(paramList != null && !paramList.isEmpty()) {
 				
 				/*
 				 * Update gate lists (between base FN and client FN).
 				 */
 				
 				mGatesFromBaseToSocket.clear(); // measure of precaution
 				mGatesFromSocketToBase.clear(); // measure of precaution
 				
 				LinkedList<AbstractGate> tTargetList = mGatesFromBaseToSocket;
 				for(SocketPathParam param : paramList) {
 					if(param != null) {
 						AbstractGate tGate = param.getGate();
 						if(tGate != null) {
 							tTargetList.addLast(tGate);
 							
 							// do we have to switch to down direction?
 							ForwardingElement fe = tGate.getNextNode();
 							if(fe == mPeer) {
 								tTargetList = mGatesFromSocketToBase;
 							}
 						}
 					}
 				}
 				
 				/*
 				 * Initialize gates as pairs from base FN to client FN.
 				 */
 				
 				tTargetList = new LinkedList<AbstractGate>();
 				tTargetList.addAll(mGatesFromBaseToSocket);
 				tTargetList.addAll(mGatesFromSocketToBase);
 				
 				while(!tTargetList.isEmpty()) {
 					AbstractGate tGate = null;
 					// Initialize downgoing gate first.
 					tGate = tTargetList.pollLast();
 					if(tGate != null) {
 						tGate.initialise();
 					}
 					// Initialize upgoing gate second.
 					tGate = tTargetList.pollFirst();
 					if(tGate != null) {
 						tGate.initialise();
 					}
 				}
 				
 			}
 		}
 		
 		// start terminate timer
 		restartTimer();
 	}
 	
 	public Description getIntermediateDescr() throws NetworkException
 	{
 		// was a description in cache?
 		// if not, try to calculate one from given requirement
 		if(mIntermediateDescription == null) {
 			LinkedList<SocketPathParam> tListUp = new LinkedList<SocketPathParam>();
 			LinkedList<SocketPathParam> tListDown = new LinkedList<SocketPathParam>();
 			
 			mIntermediateDescription = getBase().getEntity().getController().deriveGatesFromRequirements(getBase(), mDescription, tListUp, tListDown);
 		}
 		
 		if(mIntermediateDescription.isEmpty() && mDescription != null) {
 			if(Config.Connection.DONT_USE_INTERMEDIATE_DESCRIPTION) {
 				mLogger.info(this, "No intermediate description was generated, using description provided by application");
 				return mDescription;
 			}
 		}else
 			mLogger.log(this, "Determined intermediate description: " + mIntermediateDescription);
 		
 		return mIntermediateDescription;
 	}
 	
 	/**
 	 * Called to set a new route between peers without new route on stacks
 	 */
 	public void updateRoute(Route pNewRouteToPeer, Identity pPeerIdentity)
 	{
 		updateRoute(pNewRouteToPeer, getPeerRouteUp(), getPeerRoutingName(), pPeerIdentity);
 	}
 	
 	@Override
 	public void updateRoute(Route pRouteToPeer, Route pRouteInternalToPeer, Name pPeerRoutingName, Identity pPeerIdentity)
 	{
 		mPeerRoutingName = pPeerRoutingName;
 		mPeerIdentity = pPeerIdentity;
 		
 		if(pRouteInternalToPeer != null) {
 			mPeerRouteUp = pRouteInternalToPeer.clone();
 		} else {
 			mPeerRouteUp = new Route();
 		}
 		
 		Route tCompleteRoute = getCompleteRoute(pRouteToPeer, pRouteInternalToPeer);
 			
 		AbstractGate tPeerLeavingGate = getClientLeavingGate();
 		if(tPeerLeavingGate != null && tPeerLeavingGate instanceof HorizontalGate) {
 			((HorizontalGate) tPeerLeavingGate).setRoute(tCompleteRoute);
 		}
 		
 		restartTimer();
 		
 		// process is fine now
 		setState(ProcessState.OPERATING);
 		
 		// remove cached message
 		mCachedSignalingMessage = null;
 		
 		if(Config.Connection.TERMINATE_WHEN_IDLE) {
 			activateIdleTimeout();
 		}
 		
 		if(Config.Connection.SEND_KEEP_ALIVE_MESSAGES_WHEN_IDLE) {
 			activateKeepAlive();
 		}
 	}
 	
 	public Name getPeerRoutingName()
 	{
 		return mPeerRoutingName;
 	}
 	
 	/**
 	 * @return Route from peer base FN through its gates
 	 */
 	public Route getPeerRouteUp()
 	{
 		return mPeerRouteUp;
 	}
 	
 	@Override
 	public boolean isChangableBy(Identity changer)
 	{
 		boolean allowed = super.isChangableBy(changer);
 		
 		if(!allowed) {
 			if(mPeerIdentity != null) {
 				allowed = mPeerIdentity.equals(changer);
 			} else {
 				allowed = true;
 			}
 		}
 		
 		return allowed;
 	}
 	
 	@Override
 	protected void finished()
 	{
 		/* *****************************************************************
 		 * Special treatment for client entering and client leaving gate.
 		 * 
 		 * Necessary because of the possibility that these gates have
 		 * already been removed / unregistered by closing peer (ClientFN).
 		 * Also the name of the local peer (ClientFN) might be unavailable
 		 * via routingservice due to unregistration by close()-methode.
 		 ******************************************************************/
 		
 		AbstractGate tClientEnteringGate = mGatesFromBaseToSocket.pollLast();
 		AbstractGate tClientLeavingGate = mGatesFromSocketToBase.pollFirst();
 		
 		// Origin FN of client entering gate.
 		ForwardingNode tClientEnteringGateOriginFN = getBase();
 		// Target FN of client leaving gate.
 		ForwardingNode tClientLeavingGateTargetFN = getBase();
 		
 		if(tClientEnteringGate != null && tClientLeavingGate != null) {
 			
 			// Fetch origin FN of client entering gate.
 			if(!mGatesFromBaseToSocket.isEmpty()) {
 				AbstractGate tGate = mGatesFromBaseToSocket.peekLast();
 				if(tGate != null) {
 					ForwardingElement tFE = tGate.getNextNode();
 					if(tFE != null && tFE instanceof ForwardingNode) {
 						tClientEnteringGateOriginFN = (ForwardingNode) tFE;
 					}
 				}
 			}
 			
 			// Remove client entering gate.
 			if(tClientEnteringGate.getReferenceCounter() > 0 && tClientEnteringGate.getGateID() != null) {
 				tClientEnteringGate.shutdown();
 				if(tClientEnteringGate.getReferenceCounter() < 1) {
 					tClientEnteringGateOriginFN.unregisterGate(tClientEnteringGate);
 				}
 			}
 			
 			// Fetch target FN of client leaving gate.
 			if(tClientLeavingGate.getNextNode() != null) {
 				ForwardingElement tFE = tClientLeavingGate.getNextNode();
 				if(tFE instanceof ForwardingNode) {
 					tClientLeavingGateTargetFN = (ForwardingNode) tFE;
 				}
 			}
 			
 			// Remove client leaving gate (if not already done).
 			if(mPeer != null && tClientLeavingGate.getReferenceCounter() > 0 && tClientLeavingGate.getGateID() != null) {
 				tClientLeavingGate.shutdown();
 				if(tClientLeavingGate.getReferenceCounter() < 1) {
 					mPeer.unregisterGate(tClientLeavingGate);
 				}
 			}
 		}
 		
 		/* *****************************************************************
 		 * De-construct old paths.
 		 ******************************************************************/
 		
 		LinkedList<AbstractGate> tOldPath = new LinkedList<AbstractGate>();
 		LinkedList<SocketPathParam> tParamList = new LinkedList<SocketPathParam>();
 		
 		if(mGatesFromBaseToSocket != null && !mGatesFromBaseToSocket.isEmpty()) {
 			// First round: upgoing path
 			// Using all gates of both paths for tOldPath to prevent shutdown
 			// and unregistration of gates used multiple times (or even in
 			// downgoing path).
 			
 			tOldPath.addAll(mGatesFromBaseToSocket);
 			tOldPath.addAll(mGatesFromSocketToBase);
 			
 			for(AbstractGate tGate : mGatesFromBaseToSocket) {
 				if(tGate != null) {
 					
 					// Do not remove local base FN nor local client FN
 					// but try to remove all FNs in between.
 					boolean tRemoveTargetFN = (tGate.getNextNode() != mPeer && tGate.getNextNode() != getBase());
 					
 					tParamList.addLast(new SocketPathParam(tGate.getGateID(), /*removeGate*/true, null, tRemoveTargetFN));
 				}
 			}
 			
 			try {
 				SocketPathCreator.createPath(getBase(), tParamList, tOldPath, getOwner());
 			} catch (CreationException e) {
 				mLogger.err(this, "Error de-constructing upgoing socket path. " + e.getMessage());
 			}
 			
 			tParamList.clear();
 			tOldPath.clear();
 			mGatesFromBaseToSocket.clear();
 		}
 		
 		if(mGatesFromSocketToBase != null && !mGatesFromSocketToBase.isEmpty()) {
 			// Second round: downgoing path
 			// Using all gates of downgoing path for tOldPath to prevent
 			// shutdown and unregistration of gates used multiple times.
 			
 			tOldPath.addAll(mGatesFromSocketToBase);
 			
 			for(AbstractGate tGate : mGatesFromSocketToBase) {
 				if(tGate != null) {
 					
 					// Do not remove local base FN nor local client FN
 					// but try to remove all FNs in between.
 					boolean tRemoveTargetFN = (tGate.getNextNode() != mPeer && tGate.getNextNode() != getBase());
 					
 					tParamList.addLast(new SocketPathParam(tGate.getGateID(), /*removeGate*/true, null, tRemoveTargetFN));
 				}
 			}
 			
 			try {
 				SocketPathCreator.createPath(tClientLeavingGateTargetFN, tParamList, tOldPath, getOwner());
 			} catch (CreationException e) {
 				mLogger.err(this, "Error de-constructing downgoing socket path. " + e.getMessage());
 			}
 			
 			//tParamList.clear();
 			//tOldPath.clear();
 			mGatesFromSocketToBase.clear();
 		}
 		
 		/* *****************************************************************
 		 * Check whether target FN of client leaving gate or origin FN of
 		 * client entering gate are linkless, no Socket, no ServerSocket and
 		 * not base FN. -> Close! 
 		 ******************************************************************/
 		
 		if(
 				tClientEnteringGateOriginFN != null &&
 				tClientEnteringGateOriginFN != getBase() &&
 				tClientEnteringGateOriginFN instanceof GateContainer &&
 				!(tClientEnteringGateOriginFN instanceof Connection) &&
 				!(tClientEnteringGateOriginFN instanceof Binding) &&
 				!tClientEnteringGateOriginFN.getIterator(null).hasNext()
 		) {
 			// Close origin FN of client entering gate.
 			((GateContainer) tClientEnteringGateOriginFN).close(); //TODO Synchronize?
 		}
 		if(
 				tClientLeavingGateTargetFN != null &&
 				tClientLeavingGateTargetFN != tClientEnteringGateOriginFN &&
 				tClientLeavingGateTargetFN != getBase() &&
 				tClientLeavingGateTargetFN instanceof GateContainer &&
 				!(tClientLeavingGateTargetFN instanceof Connection) &&
 				!(tClientLeavingGateTargetFN instanceof Binding) &&
 				!tClientLeavingGateTargetFN.getIterator(null).hasNext()
 		) {
 			// Close target FN of client leaving gate.
 			((GateContainer) tClientLeavingGateTargetFN).close(); //TODO Synchronize?
 		}
 		
 		/* *****************************************************************
 		 * Inform waiting apps.
 		 ******************************************************************/
 		super.finished();
 	}
 	
 	/**
 	 * @return The route from base FN to {@link Connection} FN.
 	 */
 	public Route getRouteUpToClient()
 	{
 		// Fetch the route from local base FN to local client FN.
 		Route tSendersRouteUpToHisClient = new Route();
 		
 		if(mGatesFromBaseToSocket != null) {
 			for(AbstractGate tGate : mGatesFromBaseToSocket) {
 				if(tGate != null && tGate.getGateID() != null) {
 					tSendersRouteUpToHisClient.addLast(tGate.getGateID());
 				}
 			}
 		}
 
 		return tSendersRouteUpToHisClient;
 	}
 	
 	/**
 	 * @return The route in path from local client (without HorizontalGate) to remote client.
 	 */
 	private Route getCompleteRoute(Route pReturnRouteFromBase, Route pSendersRouteUpToHisClient)
 	{
 		Route tCompleteRoute = new Route();
 		
 		if(mGatesFromSocketToBase != null) {
 			// Do not use first gate because it is HorizontalGate himself.
 			for(int i = 1; i < mGatesFromSocketToBase.size(); ++i) {
 				tCompleteRoute.addLast(mGatesFromSocketToBase.get(i).getGateID());
 			}
 		}
 		if(pReturnRouteFromBase != null && !pReturnRouteFromBase.isEmpty()) {
 			tCompleteRoute.addLast(pReturnRouteFromBase.clone());
 		}
 		if(pSendersRouteUpToHisClient != null && !pSendersRouteUpToHisClient.isEmpty()) {
 			tCompleteRoute.addLast(pSendersRouteUpToHisClient.clone());
 		}
 
 		return tCompleteRoute;
 	}
 	
 	
 	/* *************************************************************************
 	 * Members
 	 **************************************************************************/
 	
 	/**
 	 * Description used for the route request for the route between both
 	 * local gates for a connection.
 	 */
 	@Viewable("Intermediate description")
 	private Description mIntermediateDescription = null;
 	
 	private boolean mCreateHorizontal = true;
 	
 	@Viewable("Peer routing name")
 	private Name mPeerRoutingName;
 	
 	@Viewable("Peer route up")
 	private Route mPeerRouteUp;
 	
 	@Viewable("Peer identity")
 	private Identity mPeerIdentity;
 
 	private Packet mCachedSignalingMessage;
 }
