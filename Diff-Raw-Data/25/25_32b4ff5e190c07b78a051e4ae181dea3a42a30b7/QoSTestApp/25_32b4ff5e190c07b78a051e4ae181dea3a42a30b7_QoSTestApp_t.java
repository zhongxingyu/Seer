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
 
 import de.tuilmenau.ics.fog.application.ThreadApplication;
 import de.tuilmenau.ics.fog.facade.Connection;
 import de.tuilmenau.ics.fog.facade.Name;
 import de.tuilmenau.ics.fog.facade.NetworkException;
 import de.tuilmenau.ics.fog.packets.InvisibleMarker;
 import de.tuilmenau.ics.fog.routing.naming.HierarchicalNameMappingService;
 import de.tuilmenau.ics.fog.routing.naming.NameMappingEntry;
 import de.tuilmenau.ics.fog.routing.naming.NameMappingService;
 import de.tuilmenau.ics.fog.routing.naming.hierarchical.HRMID;
 import de.tuilmenau.ics.fog.topology.Node;
 import de.tuilmenau.ics.fog.ui.Logging;
 import de.tuilmenau.ics.fog.ui.Marker;
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
 	Name mDestinationNodeName = null;
 	
 	/**
 	 * Stores the last destination HRMID
 	 */
 	HRMID mDestinationHRMID = null;
 	
 	/**
 	 * Stores the established connections
 	 */
 	LinkedList<Connection> mConnections = new LinkedList<Connection>();
 	
 	
 	boolean mQoSTestNeeded = true;
 	boolean mQoSTestRunning = false;
 	
 	enum Operation{INC_CONN, DEC_CONN};
 	LinkedList<Operation> mOperations = new LinkedList<Operation>();
 	
 	/**
 	 * Constructor
 	 * 
 	 * @param pLocalNode the local node where this app. instance is running
 	 * @param pDestinationNodeNameStr the name of the destination node as string
 	 */
 	public QoSTestApp(Node pLocalNode, String pDestinationNodeNameStr)
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
 
 		/**
 		 * get the name of the central FN
 		 */ 
 		mDestinationNodeName = new SimpleName(Node.NAMESPACE_HOST, pDestinationNodeNameStr);
 	}
 
 	/**
 	 * Determines the HRMIDs of the destination node
 	 * 
 	 * @return the list of HRMIDs
 	 */
 	private LinkedList<HRMID> getDestinationHRMIDs()
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
 					
 					Logging.log(this, "Found in the NMS the HRMID " + tTargetNodeHRMID.toString() + " for node " + mDestinationNodeName);
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
 	 * EVENT: increase connections
 	 */
 	public synchronized void eventIncreaseConnections()
 	{
 		Logging.log(this, "EVENT: increase connections (currently: " + countConnections() + ")");
 		synchronized (mOperations) {
 			mOperations.add(Operation.INC_CONN);
 		}
 		notify();
 	}
 	
 	/**
 	 * EVENT: descrease connections
 	 */
 	public synchronized void eventDecreaseConnections()
 	{
 		Logging.log(this, "EVENT: decrease connections (currently: " + countConnections() + ")");
 		synchronized (mOperations) {
 			mOperations.add(Operation.DEC_CONN);
 		}
 		notify();
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
 		Logging.log("Creating marker with coloring (" + tRed + ", " + tGreen + ", " + tBlue + ")");
 
 		java.awt.Color tMarkerColor = new java.awt.Color(tRed, tGreen, tBlue);
 		String tMarkerText = "Marker " +new Random().nextInt();
 		InvisibleMarker.Operation tMarkerOperation = InvisibleMarker.Operation.ADD;
 		
 		Marker tMarker = new Marker(tMarkerText, tMarkerColor);
 		InvisibleMarker tMarkerPacketPayload = new InvisibleMarker(tMarker, tMarkerOperation);
 		//Logging.log(this, "Sending: " + tMarkerPacketPayload);
 
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
 		Logging.log(this, "Increasing connections (currently: " + countConnections() + ")");
 		
 		LinkedList<HRMID> tDestinationHRMIDs = getDestinationHRMIDs();
 		
 		HRMID tDestinationHRMID = tDestinationHRMIDs.getFirst();
 		mDestinationHRMID = tDestinationHRMID;
 				
 		if(!tDestinationHRMIDs.isEmpty()){
 			/**
 			 * Connect to the destination node
 			 */
 			Connection tConnection = ProbeRouting.createProbeRoutingConnection(mNode, tDestinationHRMID, 53 /* ms */, 1000 /* kbit/s */, false);
 			
 			/**
 			 * Check if connect request was successful
 			 */
 			if(tConnection != null){
 				Logging.log(this, "        ..found valid connection to " + tDestinationHRMID);
 				
 				synchronized (mConnections) {
 					mConnections.add(tConnection);
 				}
 				
 				/**
 				 * Send some test data
 				 */
 				for(int i = 0; i < 3; i++){
 					try {
 						//Logging.log(this, "      ..sending test data " + i);
 						tConnection.write("TEST DATA " + Integer.toString(i));
 					} catch (NetworkException tExc) {
 						Logging.err(this, "Couldn't send test data", tExc);
 					}
 				}
 				
 				/**
 				 * Send connection marker
 				 */
 				sendMarker(tConnection);
 			}	
 		}
 	}
 
 	/**
 	 * Removes the last connection
 	 */
 	private void decConnections()
 	{
 		Logging.log(this, "Decreasing connections (currently: " + countConnections() + ")");
 
 		/**
 		 * get the last connection
 		 */
 		Connection tConnection = null;
 		synchronized (mConnections) {
 			if(countConnections() > 0){
 				tConnection = mConnections.removeLast();
 			}
 		}
 		
 		/**
 		 * Disconnect by closing the connection
 		 */
 		if(tConnection != null){
 			tConnection.close();
 		}
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
 
 		//Logging.log(this, "Connections: " + tResult);
 		
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
 		/**
 		 * START
 		 */
 		Logging.log(this, "Main loop started");		
 		mQoSTestRunning = true;
 
 		/**
 		 * MAIN LOOP
 		 */
 		while(mQoSTestNeeded){
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
 					Logging.log(this, "Processing operation: " + tNextOperation);
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
		Connection tConnection = null;
		do{
			/**
			 * get the last connection
			 */
			tConnection = null;
			synchronized (mConnections) {
				if(countConnections() > 0){
					tConnection = mConnections.removeLast();
				}
			}
			
			/**
			 * Disconnect by closing the connection
			 */
			if(tConnection != null){
				tConnection.close();
			}
		}while(tConnection != null);

 		/**
 		 * END
 		 */
 		Logging.log(this, "Main loop finished");
 		mQoSTestRunning = false;
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
 		mQoSTestNeeded = false;
 		
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
 		return mQoSTestRunning;
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
 }
