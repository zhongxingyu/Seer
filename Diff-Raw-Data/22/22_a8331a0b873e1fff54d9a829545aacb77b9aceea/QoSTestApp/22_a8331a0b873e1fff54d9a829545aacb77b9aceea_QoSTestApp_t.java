 /*******************************************************************************
  * Forwarding on Gates Simulator/Emulator - Hierarchical Routing Management
  * Copyright (c) 2012, Integrated Communication Systems Group, TU Ilmenau.
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html.
  ******************************************************************************/
 package de.tuilmenau.ics.fog.app.routing;
 
 import java.rmi.RemoteException;
 import java.util.LinkedList;
 import java.util.Random;
 import java.util.HashMap;
 
 import de.tuilmenau.ics.fog.application.ThreadApplication;
 import de.tuilmenau.ics.fog.application.util.Session;
 import de.tuilmenau.ics.fog.facade.Connection;
 import de.tuilmenau.ics.fog.facade.Name;
 import de.tuilmenau.ics.fog.facade.NetworkException;
 import de.tuilmenau.ics.fog.packets.InvisibleMarker;
import de.tuilmenau.ics.fog.routing.hierarchical.HRMConfig;
 import de.tuilmenau.ics.fog.routing.hierarchical.HRMController;
 import de.tuilmenau.ics.fog.routing.hierarchical.IHRMApi;
 import de.tuilmenau.ics.fog.routing.hierarchical.properties.HRMRoutingProperty;
 import de.tuilmenau.ics.fog.routing.naming.HierarchicalNameMappingService;
 import de.tuilmenau.ics.fog.routing.naming.NameMappingEntry;
 import de.tuilmenau.ics.fog.routing.naming.NameMappingService;
 import de.tuilmenau.ics.fog.routing.naming.hierarchical.HRMID;
 import de.tuilmenau.ics.fog.topology.Node;
 import de.tuilmenau.ics.fog.ui.Logging;
 import de.tuilmenau.ics.fog.ui.Marker;
 import de.tuilmenau.ics.fog.ui.MarkerContainer;
 import de.tuilmenau.ics.fog.ui.eclipse.commands.hierarchical.ProbeRouting;
 import de.tuilmenau.ics.fog.util.SimpleName;
 		
 /**
  * This class is responsible for creating QoS-probe connections and also for their destruction.
  */
 public class QoSTestApp extends ThreadApplication
 {
 	/**
 	 * Stores a reference to the NMS instance.
 	 */
 	private NameMappingService mNMS = null;
 
 	/**
 	 * Stores the current node where the probe routing should start
 	 */
 	private Node mNode = null;
 
 	/**
 	 * Stores the name of the destination node
 	 */
 	private Name mDestinationNodeName = null;
 	
 	/**
 	 * Stores the last destination HRMID
 	 */
 	private HRMID mDestinationHRMID = null;
 	
 	/**
 	 * Stores the established connections
 	 */
 	private LinkedList<Connection> mConnections = new LinkedList<Connection>();
 	private HashMap<Connection, QoSTestAppSession> mConnectionSessions = new HashMap<Connection, QoSTestAppSession>();
 
 	/**
 	 * Stores the established connections with fulfilled QoS requirements
 	 */
 	private LinkedList<Connection> mConnectionsWithFulfilledQoS = new LinkedList<Connection>();
 
 	/**
 	 * Stores the established connections with feedback
 	 */
 	private LinkedList<Connection> mConnectionsWithFeedback = new LinkedList<Connection>();
 	
 	/**
 	 * Stores the marker per connection
 	 */
 	private HashMap<Connection, Marker> mMarkers = new HashMap<Connection, Marker>();
 	
 	/**
 	 * Stores the default max. desired delay in [ms]
 	 */
 	private int mDefaultDelay = 53;  // some random number above 0
 	
 	/**
 	 * Stores the default min. desired data rate in [kbit/s]
 	 */
 	private int mDefaultDataRate = 1000;
 	
 	/**
 	 * Stores if the QoSTestApp is still needed or is already exit  
 	 */
 	private boolean mQoSTestAppNeeded = true;
 	
 	/**
 	 * Stores if the QoSTestApp is running
 	 */
 	private boolean mQoSTestAppRunning = false;
 	
 	/**
 	 * The possible operations: increase/decrease connection amount
 	 */
 	private enum Operation{INC_CONN, DEC_CONN};
 	
 	/**
 	 * The pending operations
 	 */
 	private LinkedList<Operation> mOperations = new LinkedList<Operation>();
 	
 	// debugging?
 	private boolean DEBUG = false;
 	
 	/**
 	 * Constructor
 	 * 
 	 * @param pLocalNode the local node where this app. instance is running
 	 */
 	public QoSTestApp(Node pLocalNode)
 	{
 		super(pLocalNode, null);
 		mNode = pLocalNode;
 
 		/**
 		 * Get a reference to the naming-service
 		 */
 		try {
 			mNMS = HierarchicalNameMappingService.getGlobalNameMappingService(mNode.getAS().getSimulation());
 		} catch (RuntimeException tExc) {
 			mNMS = HierarchicalNameMappingService.createGlobalNameMappingService(mNode.getAS().getSimulation());
 		}
 	}
 
 	/**
 	 * Returns a reference to the local HRMApi
 	 * 
 	 * @return the HRMApi
 	 */
 	public IHRMApi getHRMApi()
 	{
 		IHRMApi tResult = HRMController.getHRMApi(mNode);
 		
 		return tResult;
 	}
 	
 	/**
 	 * Constructor
 	 * 
 	 * @param pLocalNode the local node where this app. instance is running
 	 * @param pDestinationNodeNameStr the name of the destination node as string
 	 */
 	public QoSTestApp(Node pLocalNode, String pDestinationNodeNameStr)
 	{
 		this(pLocalNode);
 		setDestination(pDestinationNodeNameStr);
 	}
 
 	/**
 	 * Sets a new destination node name
 	 * 
 	 * @param pDestinationNodeNamestr the new destination node name
 	 */
 	public void setDestination(String pDestinationNodeNameStr)
 	{
 		mDestinationNodeName = new SimpleName(Node.NAMESPACE_HOST, pDestinationNodeNameStr);
 	}
 	
 	/**
 	 * Determines the HRMIDs of the destination node
 	 * 
 	 * @return the list of HRMIDs
 	 */
 	private LinkedList<HRMID> getDestinationHRMIDsFromDNS()
 	{
 		LinkedList<HRMID> tResult = new LinkedList<HRMID>();
 		
 		/**
 		 * Get the HRMID of the destination node
 		 */
 		// send a HRM probe-packet to each registered address for the given target name
 		try {
 			for(NameMappingEntry<?> tNMSEntryForTarget : mNMS.getAddresses(mDestinationNodeName)) {
 				if(tNMSEntryForTarget.getAddress() instanceof HRMID) {
 					// get the HRMID of the target node
 					HRMID tTargetNodeHRMID = (HRMID)tNMSEntryForTarget.getAddress();
 					
 					if(DEBUG){
 						Logging.log(this, "Found in the NMS the HRMID " + tTargetNodeHRMID.toString() + " for node " + mDestinationNodeName);
 					}
 				}
 			}
 			
 			for(NameMappingEntry<?> tNMSEntryForTarget : mNMS.getAddresses(mDestinationNodeName)) {
 				if(tNMSEntryForTarget.getAddress() instanceof HRMID) {
 					// get the HRMID of the target node
 					HRMID tDestinationNodeHRMID = (HRMID)tNMSEntryForTarget.getAddress();
 					
 					// an entry in the result list
 					tResult.add(tDestinationNodeHRMID);
 				}
 			}
 		} catch (RemoteException tExc) {
 			Logging.err(this, "Unable to determine addresses for node " + mDestinationNodeName, tExc);
 		}
 
 		return tResult;
 	}
 	
 	/**
 	 * Returns the best HRMID of the destination node
 	 * 
 	 * @return the best HRMID of the destination node
 	 */
 	private HRMID getBestDestinationHRMIDFromDNS()
 	{
 		HRMID tResult = null;
 		
 		LinkedList<HRMID> tDestinationHRMIDs = getDestinationHRMIDsFromDNS();
 		if((tDestinationHRMIDs != null) && (!tDestinationHRMIDs.isEmpty())){
 			// use first HRMID as default value
 			tResult = tDestinationHRMIDs.getFirst();
 			
 			// search for a better choice
 			for(HRMID tHRMID : tDestinationHRMIDs){
 				IHRMApi tHRMApi = HRMController.getHRMApi(mNode);
 				if(tHRMApi != null){
 					if(!tHRMApi.isLocalCluster(tHRMID.getClusterAddress(0))){
 						tResult = tHRMID;
 						break;
 					}
 				}
 			}
 		}
 		
 		return tResult;
 	}
 	
 	/**
 	 * EVENT: increase connections
 	 */
 	public synchronized void eventIncreaseConnections()
 	{
 		if(DEBUG){
 			Logging.log(this, "EVENT: increase connections (currently: " + countConnections() + ")");
 		}
 		synchronized (mOperations) {
 			mOperations.add(Operation.INC_CONN);
 		}
 		notify();
 	}
 	
 	/**
 	 * EVENT: decrease connections
 	 */
 	public synchronized void eventDecreaseConnections()
 	{
 		if(DEBUG){
 			Logging.log(this, "EVENT: decrease connections (currently: " + countConnections() + ")");
 		}
 		synchronized (mOperations) {
 			mOperations.add(Operation.DEC_CONN);
 		}
 		notify();
 	}
 
 	/**
 	 * Returns the default delay
 	 * 
 	 * @return the default delay
 	 */
 	public int getDefaultDelay()
 	{
 		return mDefaultDelay;
 	}
 
 	/**
 	 * Sets a new default delay for future connections
 	 * 
 	 * @param pDefaultDelay the new default delay
 	 */
 	public void setDefaultDelay(int pDefaultDelay)
 	{
 		mDefaultDelay = pDefaultDelay;
 	}
 	
 	/**
 	 * Returns the default data rate
 	 * 
 	 * @return the default data rate
 	 */
 	public int getDefaultDataRate()
 	{
 		return mDefaultDataRate;
 	}
 
 	/**
 	 * Sets a new default data rate for future connections
 	 * 
 	 * @param pDefaultDataRate the new default data rate
 	 */
 	public void setDefaultDataRate(int pDefaultDataRate)
 	{
 		mDefaultDataRate = pDefaultDataRate;
 	}
 
 	/**
 	 * Sends a marker along an established connection
 	 * 
 	 * @param pConnection the connection, whose route should be marked
 	 */
 	private void sendMarker(Connection pConnection)
 	{
 		Random tRandom = new Random();
 		int tRed = (int)(tRandom.nextFloat() * 128) + 127;
 		int tGreen = (int)(tRandom.nextFloat() * 128) + 127;
 		int tBlue = (int)(tRandom.nextFloat() * 128) + 127;
 		if(DEBUG){
 			Logging.log("Creating marker with coloring (" + tRed + ", " + tGreen + ", " + tBlue + ")");
 		}
 
 		java.awt.Color tMarkerColor = new java.awt.Color(tRed, tGreen, tBlue);
 		String tMarkerText = "Marker " +new Random().nextInt();
 		InvisibleMarker.Operation tMarkerOperation = InvisibleMarker.Operation.ADD;
 		
 		Marker tMarker = new Marker(tMarkerText, tMarkerColor);
 		InvisibleMarker tMarkerPacketPayload = new InvisibleMarker(tMarker, tMarkerOperation);
 		
 		// store the marker for this connection in order to be able to remove the marker later
 		mMarkers.put(pConnection, tMarker);
 		
 		if(DEBUG){
 			//Logging.log(this, "Sending: " + tMarkerPacketPayload);
 		}
 
 		try {
 			pConnection.write(tMarkerPacketPayload);
 		} catch (NetworkException tExc) {
 			Logging.err(this, "sendMarker() wasn't able to send marker", tExc);
 		}
 	}
 	
 	/**
 	 * Returns the last destination HRMID
 	 * 
 	 * @return the last destination HRMID
 	 */
 	public HRMID getLastDestinationHRMID()
 	{
 		return mDestinationHRMID;
 	}
 	
 	/**
 	 * Adds another connection
 	 */
 	private void incConnections()
 	{
 		if(DEBUG){
 			Logging.log(this, "Increasing connections (currently: " + countConnections() + ")");
 		}
 		
 		HRMID tDestinationHRMID = getBestDestinationHRMIDFromDNS();
 		if(tDestinationHRMID != null){
 			mDestinationHRMID = tDestinationHRMID;
 			/**
 			 * Connect to the destination node
 			 */
 		    boolean tRetryConnection = false;
 		    boolean tRetriedConnection = false;
 		    int tAttemptNr = 1;
 		    Connection tConnection = null;
 		    do{
 		    	tRetryConnection = false;
 				tConnection = ProbeRouting.createProbeRoutingConnection(this, mNode, tDestinationHRMID, mDefaultDelay /* ms */, mDefaultDataRate /* kbit/s */, false);
				if((tConnection == null) && (tAttemptNr < HRMConfig.Hierarchy.CONNECTION_MAX_RETRIES)){
 					tAttemptNr++;
 					tRetryConnection = true;
 					tRetriedConnection = true;
 					Logging.warn(this, "Cannot connect to: " + tDestinationHRMID + ", connect attempt nr. " + tAttemptNr);
 				}
 		    }while((tRetryConnection) && (mQoSTestAppNeeded));
 			/**
 			 * Check if connect request was successful
 			 */
 			if(tConnection != null){
			    if(tRetriedConnection){
					Logging.warn(this, "Successfully recovered from connection problems towards: " + tDestinationHRMID + ", connect attempts: " + tAttemptNr);
			    }
			    
 				if(DEBUG){
 					Logging.log(this, "        ..found valid connection to " + tDestinationHRMID);
 				}
 				
 				synchronized (mConnections) {
 					mConnections.add(tConnection);
 				}
 
 				/**
 				 * Create the connection session
 				 */
 				QoSTestAppSession tConnectionSession = new QoSTestAppSession(this);
 				tConnectionSession.start(tConnection);
 				synchronized (mConnectionSessions) {
 					mConnectionSessions.put(tConnection, tConnectionSession);
 				}
 				
 				/**
 				 * Send some test data
 				 */
 //				for(int i = 0; i < 3; i++){
 //					try {
 //						//Logging.log(this, "      ..sending test data " + i);
 //						tConnection.write("TEST DATA " + Integer.toString(i));
 //					} catch (NetworkException tExc) {
 //						Logging.err(this, "Couldn't send test data", tExc);
 //					}
 //				}
 				
 				/**
 				 * Send connection marker
 				 */
 				sendMarker(tConnection);
 			}
 		}else{
 			Logging.err(this, "No destination HRMID found for: " + mDestinationNodeName);
 		}
 	}
 
 	/**
 	 * Removes the last connection
 	 */
 	private void decConnections()
 	{
 		if(DEBUG){
 			Logging.log(this, "Decreasing connections (currently: " + countConnections() + ")");
 		}
 
 		/**
 		 * get the last connection
 		 */
 		Connection tConnection = null;
 		synchronized (mConnections) {
 			if(countConnections() > 0){
 				tConnection = mConnections.removeLast();
 				if(DEBUG){
 					Logging.log(this, "  ..seleted for renoving the connection: " + tConnection);
 				}
 			}
 		}
 		
 		if(tConnection != null){
 			/**
 			 * Remove the marker
 			 */
 			synchronized(mMarkers){
 				Marker tMarker = mMarkers.get(tConnection);
 				if(tMarker != null){
 					mMarkers.remove(tConnection);
 					MarkerContainer.getInstance().removeMarker(tMarker);
 				}
 			}
 		
 			/**
 			 * Stop the connection session
 			 */
 			QoSTestAppSession tConnectionSession = null;
 			synchronized (mConnectionSessions) {
 				tConnectionSession = mConnectionSessions.remove(tConnection);
 			}
 			if(tConnectionSession != null){
 				tConnectionSession.stop();
 			}
 					
 			/**
 			 * Disconnect by closing the connection
 			 */
 			tConnection.close();
 			
 			/**
 			 * Remove as QoS connection
 			 */
 			synchronized (mConnectionsWithFulfilledQoS) {
 				if(mConnectionsWithFulfilledQoS.contains(tConnection)){
 					mConnectionsWithFulfilledQoS.remove(tConnection);
 				}
 			}
 			synchronized (mConnectionsWithFeedback) {
 				if(mConnectionsWithFeedback.contains(tConnection)){
 					mConnectionsWithFeedback.remove(tConnection);
 				}
 			}
 
 		}
 	}
 
 	/**
 	 * Counts the already established connections which fulfill the desired QoS requirements
 	 * 
 	 * @return the number of connections
 	 */
 	public int countConnectionsWithFulfilledQoS()
 	{
 		int tResult = 0;
 		
 		synchronized (mConnectionsWithFulfilledQoS) {
 			tResult = mConnectionsWithFulfilledQoS.size();
 		}
 		
 		return tResult;
 	}
 	
 	/**
 	 * Counts the already established connections which have already a feedback from the server to the client (this app)
 	 * 
 	 * @return the number of connections
 	 */
 	public int countConnectionsWithFeedback()
 	{
 		int tResult = 0;
 		
 		synchronized (mConnectionsWithFeedback) {
 			tResult = mConnectionsWithFeedback.size();
 		}
 		
 		return tResult;
 	}
 
 	/**
 	 * Counts the already established connections
 	 * 
 	 * @return the number of connections
 	 */
 	public int countConnections()
 	{
 		int tResult = 0;
 		
 		synchronized (mConnections) {
 			if(mConnections != null){
 				tResult = mConnections.size();
 			}
 		}
 
 		if(DEBUG){
 			//Logging.log(this, "Connections: " + tResult);
 		}
 		
 		return tResult;
 	}
 	
 	/**
 	 * Waits for the next wake-up signal (a new event was triggered)
 	 */
 	private synchronized void waitForNextEvent()
 	{
 		// suspend until next trigger
 		try {
 			wait();
 			//Logging.log(this, "WakeUp");
 		} catch (InterruptedException tExc) {
 			Logging.warn(this, "waitForNextEvent() got an interrupt", tExc);
 		}
 	}
 	
 	/**
 	 * The main loop
 	 */
 	/* (non-Javadoc)
 	 * @see de.tuilmenau.ics.fog.application.ThreadApplication#execute()
 	 */
 	@Override
 	protected void execute()
 	{
 		Thread.currentThread().setName(getClass().getSimpleName() + "@" + mNode);
 
 		/**
 		 * START
 		 */
 		Logging.log(this, "Main loop started");
 		mQoSTestAppRunning = true;
 
 		/**
 		 * MAIN LOOP
 		 */
 		while(mQoSTestAppNeeded){
 			Operation tNextOperation = null;
 			do{
 				/**
 				 * Get the next operation
 				 */
 				tNextOperation = null;
 				synchronized (mOperations) {
 					if(mOperations.size() > 0){
 						tNextOperation = mOperations.removeFirst();
 					}
 				}
 				
 				/**
 				 * Process the next operation
 				 */
 				if(tNextOperation != null){
 					if(DEBUG){
 						Logging.log(this, "Processing operation: " + tNextOperation);
 					}
 					switch(tNextOperation)
 					{
 						case INC_CONN:
 							incConnections();
 							break;
 						case DEC_CONN:
 							decConnections();
 							break;
 						default:
 							break;
 					}
 				}
 			}while(tNextOperation != null);				
 			
 			waitForNextEvent();
 		}
 
 		/**
 		 * Close all existing connections
 		 */
 		while(countConnections() > 0){
 			decConnections();
 		}
 
 		/**
 		 * END
 		 */
 		Logging.log(this, "Main loop finished");
 		mQoSTestAppRunning = false;
 	}
 
 	/**
 	 * Exit the QoS test app. right now
 	 */
 	/* (non-Javadoc)
 	 * @see de.tuilmenau.ics.fog.application.Application#exit()
 	 */
 	@Override
 	public synchronized void exit()
 	{
 		Logging.log(this, "exit() starting... (running: " + isRunning() + ")");
 		mQoSTestAppNeeded = false;
 		
 		// wakeup
 		if(isRunning()){
 			notifyAll();
 		}
 		
 		Logging.log(this, "..exit() finished");
 	}
 
 	/**
 	 * Returns if the QoS test app. is still running
 	 * 
 	 * @return true or false
 	 */
 	/* (non-Javadoc)
 	 * @see de.tuilmenau.ics.fog.application.Application#isRunning()
 	 */
 	@Override
 	public boolean isRunning()
 	{
 		return mQoSTestAppRunning;
 	}
 	
 	/**
 	 * Returns a descriptive string about this app.
 	 * 
 	 * @return the descriptive string
 	 */
 	@Override
 	public String toString()
 	{
 		return getClass().getSimpleName() +"@" + mNode;
 	}
 	
 	
 	private class QoSTestAppSession extends Session
 	{
 		private QoSTestApp mQoSTestApp = null;
 		
 		public QoSTestAppSession(QoSTestApp pQoSTestApp)
 		{
 			super(false, mHost.getLogger(), null);
 			mQoSTestApp = pQoSTestApp;
 		}
 
 		@Override
 		public boolean receiveData(Object pData) {
 			boolean tResult = false;
 			
 			// incoming UDP encapsulation data
 			if (pData instanceof HRMRoutingProperty){
 				HRMRoutingProperty tProbeRoutingProperty = (HRMRoutingProperty)pData;
 				
				//if(DEBUG){
					Logging.log(mQoSTestApp, "Received ProbeRoutingProperty: ");
					tProbeRoutingProperty.logAll(this);
				//}
 				//tProbeRoutingProperty.logAll(mQoSTestApp);
 				
 				/**
 				 * Count the number of connections with fulfilled QoS requirements
 				 */
 				boolean tQoSFulfilled = true;
 				if((tProbeRoutingProperty.getDesiredDelay() > 0) && (tProbeRoutingProperty.getDesiredDelay() < tProbeRoutingProperty.getRecordedDelay())){
 					tQoSFulfilled = false;
 				}
 				if((tProbeRoutingProperty.getDesiredDataRate() > 0) && (tProbeRoutingProperty.getDesiredDataRate() > tProbeRoutingProperty.getRecordedDataRate())){
 					tQoSFulfilled = false;
 				}
 				if(tQoSFulfilled){
 					synchronized (mConnectionsWithFulfilledQoS) {
 						mConnectionsWithFulfilledQoS.add(getConnection());
 					}
 				}
 
 				/**
 				 * Count the number of connections with a valid feedback
 				 */
 				synchronized (mConnectionsWithFeedback) {
 					mConnectionsWithFeedback.add(getConnection());
 				}
 				
 				tResult = true;
 			}else{
 				getLogger().warn(this, "Malformed received data from HRMController: " + pData);
 			}
 			
 			return tResult;
 		}
 	}
 
 }
