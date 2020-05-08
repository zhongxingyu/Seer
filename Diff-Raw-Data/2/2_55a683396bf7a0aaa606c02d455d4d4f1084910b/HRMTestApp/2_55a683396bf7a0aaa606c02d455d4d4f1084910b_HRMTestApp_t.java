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
 
 import java.text.DecimalFormat;
 import java.util.LinkedList;
 import java.util.Random;
 import java.util.HashMap;
 
 import de.tuilmenau.ics.fog.application.ThreadApplication;
 import de.tuilmenau.ics.fog.bus.Bus;
 import de.tuilmenau.ics.fog.routing.hierarchical.HRMController;
 import de.tuilmenau.ics.fog.routing.naming.HierarchicalNameMappingService;
 import de.tuilmenau.ics.fog.routing.naming.NameMappingService;
 import de.tuilmenau.ics.fog.topology.AutonomousSystem;
 import de.tuilmenau.ics.fog.topology.ILowerLayer;
 import de.tuilmenau.ics.fog.topology.Node;
 import de.tuilmenau.ics.fog.ui.Logging;
 import de.tuilmenau.ics.fog.ui.Statistic;
 		
 /**
  * This class is responsible for creating QoS-probe connections and also for their destruction.
  */
 public class HRMTestApp extends ThreadApplication
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
 	 * Stores if the QoSTestApp is still needed or is already exit  
 	 */
 	private boolean mHRMTestAppNeeded = true;
 	
 	/**
 	 * Stores if the QoSTestApp is running
 	 */
 	private boolean mHRMTestAppRunning = false;
 
 	/**
 	 * Defines how many connections we want to have per test turn
 	 */
 	private final int NUMBER_NODE_COMBINATIONS = 50;
 	private final int NUMBER_SUB_CONNECTIONS = 1;
 	private final int NUMBER_MEASUREMENT_TURNS = 100;
 	private final int MAX_DATA_RATE_STD_CONNECTION = 1000;
 	private final int MAX_DATA_RATE_REF_CONNECTION = 1000;
 	private final int MAX_REF_CONNECTIONS = 200;
 	
 	private HashMap<Node, QoSTestApp> mQoSTestApps = new HashMap<Node, QoSTestApp>();
 	private LinkedList<Node> mSource = new LinkedList<Node>();
 	private LinkedList<Node> mDestination = new LinkedList<Node>();
 	private LinkedList<Integer> mDataRates = new LinkedList<Integer>();
 	private Node mRefSource = null;
 	private Node mRefDestination = null;
 	private int mRefBEConnections = 0;
 	private int mRefHRMConnections = 0;
 	private Random mRandom = new Random(System.currentTimeMillis());
 	private AutonomousSystem mLocalNodeAS = null;
 	private LinkedList<Node> mGlobalNodeList = new LinkedList<Node>();
 	private LinkedList<Bus> mGlobalBusList = new LinkedList<Bus>();
 	private int mCntNodes = 0;
 	private Statistic mStatistic = null;
 	private int mCntBuss = 0;
 	private boolean GLOBAL_ERROR = false;
 	private int mTurn = 1;
 			
 	/**
 	 * Constructor
 	 * 
 	 * @param pLocalNode the local node where this app. instance is running
 	 */
 	public HRMTestApp(Node pLocalNode)
 	{
 		super(pLocalNode, null);
 		mNode = pLocalNode;
 		mLocalNodeAS = mNode.getAS();
 		
 		Logging.log(this, "########################################");
 		Logging.log(this, "##### HRM test app started #############");
 		Logging.log(this, "########################################");
 		
 		/**
 		 * Get a reference to the naming-service
 		 */
 		try {
 			mNMS = HierarchicalNameMappingService.getGlobalNameMappingService(mNode.getAS().getSimulation());
 		} catch (RuntimeException tExc) {
 			mNMS = HierarchicalNameMappingService.createGlobalNameMappingService(mNode.getAS().getSimulation());
 		}
 
 		/**
 		 * determine all nodes from the simulation
 		 */
 		for(AutonomousSystem tAS : mLocalNodeAS.getSimulation().getAllAS()) {
 			for(Node tNode : tAS.getNodelist().values()) {
 				Logging.warn(this, "Found node: " + tNode.getName());
 				mGlobalNodeList.add(tNode);
 			}
 		}
 		
 		mCntNodes = mGlobalNodeList.size();
 		
 		/**
 		 * determine all busses from the simulation
 		 */
 		for(AutonomousSystem tAS : mLocalNodeAS.getSimulation().getAllAS()) {
 			for(ILowerLayer tLL : tAS.getBuslist().values()) {
 				if(tLL instanceof Bus){
 					Bus tBus = (Bus)tLL;
 					Logging.warn(this, "Found bus: " + tBus.getName());
 					mGlobalBusList.add(tBus);
 				}else{
 					Logging.err(this, "Found unsupported LL: " + tLL);
 				}
 			}
 		}
 		
 		mCntBuss = mGlobalBusList.size();
 		
 		/**
 		 * Create all QoS test APPs
 		 */
 		for (Node tNode: mGlobalNodeList){
 			QoSTestApp tQoSTestApp = new QoSTestApp(tNode);
 			tQoSTestApp.start();			
 			mQoSTestApps.put(tNode, tQoSTestApp);
 		}
 	}
 	
 	/**
 	 * Resets all connections from all QoSTestApp instances
 	 */
 	private void resetAllConnetions()
 	{
 		for(QoSTestApp tQoSTestApp: mQoSTestApps.values()){
 			while(tQoSTestApp.countConnections() > 0){
 				int tBefore = tQoSTestApp.countConnections();
 				tQoSTestApp.eventDecreaseConnections();
				while((tQoSTestApp.countConnections() > 0) && (tQoSTestApp.countConnections() == tBefore)){
 					try {
 						Logging.log(this, "Waiting for end of " + tBefore + ". QoSTestApp connection of: " + tQoSTestApp);
 						Thread.sleep(30);
 						if(!tQoSTestApp.isRunning()){
 							Logging.err(this, "QoSTestApp isn't running anymore: " + tQoSTestApp);
 							break;
 						}
 						tQoSTestApp.eventDecreaseConnections();
 					} catch (InterruptedException tExc) {
 					}
 				}
 			}
 		}
 		
 		try {
 			Thread.sleep(2000);
 		} catch (InterruptedException tExc) {
 		}
 		
 		boolean tFoundRemainingReservation = false;
 		do{
 			tFoundRemainingReservation = false;
 			for(int i = 0; i < mCntBuss; i++){
 				Bus tBus = mGlobalBusList.get(i);
 				if(tBus.getUtilization() > 0){
 					tFoundRemainingReservation = true;
 					Logging.warn(this, "### Bus " + tBus + " has still some reservations");
 					try {
 						Thread.sleep(500);
 					} catch (InterruptedException tExc) {
 					}
 					break;
 				}
 			}
 		}while(tFoundRemainingReservation);
 			
 		for(int i = 0; i < mCntBuss; i++){
 			Bus tBus = mGlobalBusList.get(i);
 			if(tBus.getUtilization() > 0){
 				Logging.err(this, "###############################");
 				Logging.err(this, "### Bus " + tBus + " has still some reservations");
 				Logging.err(this, "###############################");
 				GLOBAL_ERROR = true;
 			}
 		}
 
 	}
 	
 	/**
 	 * Counts all connections with fulfilled QoS requirements
 	 * 
 	 * @return the number of QoS connections
 	 */
 	private int countConnectionsWithFulfilledQoS()
 	{
 		int tResult = 0;
 		
 		for(QoSTestApp tQoSTestApp: mQoSTestApps.values()){
 			tResult += tQoSTestApp.countConnectionsWithFulfilledQoS();
 		}
 		
 		return tResult;
 	}
 
 	/**
 	 * Creates the connections 
 	 */
 	private void createConnections()
 	{
 		for(int i = 0; (i < NUMBER_NODE_COMBINATIONS) && (mHRMTestAppNeeded); i++){
 			/**
 			 * Get source/destination node
 			 */
 			Node tSourceNode = mSource.get(i);
 			Node tDestinationNode = mDestination.get(i);
 			int tDataRate = mDataRates.get(i);
 			
 			/**
 			 * Create connection
 			 */
 			QoSTestApp tQoSTestApp = mQoSTestApps.get(tSourceNode);
 			tQoSTestApp.setDestination(tDestinationNode.getName());
 			for(int j = 0; j < NUMBER_SUB_CONNECTIONS; j++){
 				int tBefore = tQoSTestApp.countConnections();
 				tQoSTestApp.setDefaultDataRate(tDataRate);
 				tQoSTestApp.eventIncreaseConnections();
 				while(tQoSTestApp.countConnections() == tBefore){
 					if(!tQoSTestApp.isRunning()){
 						Logging.err(this, "QoSTestApp isn't running anymore: " + tQoSTestApp);
 						break;
 					}
 
 					try {
 						Logging.log(this, "Waiting for a start of " + (tBefore + 1) + ". QoSTestApp connection of: " + tQoSTestApp);
 						Thread.sleep(30);
 					} catch (InterruptedException tExc) {
 					}
 				}
 
 				if(!HRMController.ENFORCE_BE_ROUTING){
 					Logging.warn(this, mTurn + "/" + NUMBER_MEASUREMENT_TURNS + " - created HRM connection " + (1 + i * NUMBER_SUB_CONNECTIONS + j) + ", succesful QoS: " + tQoSTestApp.countConnectionsWithFulfilledQoS() + "/" + tQoSTestApp.countConnections());
 				}else{
 					Logging.warn(this, mTurn + "/" + NUMBER_MEASUREMENT_TURNS + " - created BE connection " + (1 + i * NUMBER_SUB_CONNECTIONS + j) + ", succesful QoS: " + tQoSTestApp.countConnectionsWithFulfilledQoS() + "/" + tQoSTestApp.countConnections());
 				}
 
 				/**
 				 * Wait some time
 				 */
 				try {
 					if(!HRMController.ENFORCE_BE_ROUTING){
 						Thread.sleep((long) 10);//(2000 * HRMConfig.Routing.REPORT_SHARE_PHASE_TIME_BASE * HRMConfig.Hierarchy.HEIGHT));
 					}else{
 						Thread.sleep((long) 10);//(2000 * HRMConfig.Routing.REPORT_SHARE_PHASE_TIME_BASE * HRMConfig.Hierarchy.HEIGHT));
 					}
 				} catch (InterruptedException tExc) {
 				}
 			}
 		}
 
 		/**
 		 * Reference connections
 		 */
 		if(mRefSource != null){
 			QoSTestApp tQoSTestApp = mQoSTestApps.get(mRefSource);
 			tQoSTestApp.setDestination(mRefDestination.getName());
 			tQoSTestApp.setDefaultDataRate(MAX_DATA_RATE_REF_CONNECTION);
 			int tConnsBefore = tQoSTestApp.countConnectionsWithFulfilledQoS();
 			for(int j = 0; j < MAX_REF_CONNECTIONS; j++){
 				int tBefore = tQoSTestApp.countConnections();
 				tQoSTestApp.eventIncreaseConnections();
 				while(tQoSTestApp.countConnections() == tBefore){
 					try {
 						Logging.log(this, "Waiting for a start of " + (tBefore + 1) + ". QoSTestApp REF connection of: " + tQoSTestApp);
 						Thread.sleep(50);
 					} catch (InterruptedException tExc) {
 					}
 				}
 
 				if(!HRMController.ENFORCE_BE_ROUTING){
 					Logging.warn(this, mTurn + "/" + NUMBER_MEASUREMENT_TURNS + " - created HRM REFERENCE connection " + (1 + j) + ", succesful QoS: " + tQoSTestApp.countConnectionsWithFulfilledQoS() + "/" + tQoSTestApp.countConnections());
 				}else{
 					Logging.warn(this, mTurn + "/" + NUMBER_MEASUREMENT_TURNS + " - created BE REFERENCE connection " + (1 + j) + ", succesful QoS: " + tQoSTestApp.countConnectionsWithFulfilledQoS() + "/" + tQoSTestApp.countConnections());
 				}
 
 				/**
 				 * Wait some time
 				 */
 				try {
 					if(!HRMController.ENFORCE_BE_ROUTING){
 						Thread.sleep((long) 50);//(2000 * HRMConfig.Routing.REPORT_SHARE_PHASE_TIME_BASE * HRMConfig.Hierarchy.HEIGHT));
 					}else{
 						Thread.sleep((long) 50);//(2000 * HRMConfig.Routing.REPORT_SHARE_PHASE_TIME_BASE * HRMConfig.Hierarchy.HEIGHT));
 					}
 				} catch (InterruptedException tExc) {
 				}
 			}
 
 			try {
 				Thread.sleep((long) 500);//(2000 * HRMConfig.Routing.REPORT_SHARE_PHASE_TIME_BASE * HRMConfig.Hierarchy.HEIGHT));
 			} catch (InterruptedException tExc) {
 			}
 
 			
 			if(!HRMController.ENFORCE_BE_ROUTING){
 				mRefHRMConnections = tQoSTestApp.countConnectionsWithFulfilledQoS() - tConnsBefore;
 			}else{
 				mRefBEConnections = tQoSTestApp.countConnectionsWithFulfilledQoS() - tConnsBefore;
 			}
 		}
 
 		
 		/**
 		 * Wait some time
 		 */
 		try {
 			Thread.sleep((long) 1000);
 		} catch (InterruptedException tExc) {
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
 		Logging.warn(this, "Main loop started");		
 		mHRMTestAppRunning = true;
 
 		try {
 			Thread.sleep(10000);
 		} catch (InterruptedException tExc) {
 		}
 
 		/**
 		 * MAIN LOOP
 		 */
 		while((mHRMTestAppNeeded) & (!GLOBAL_ERROR)){
 			
 			/**
 			 * Create connection scenario
 			 */
 			mSource.clear();
 			mDestination.clear();
 			mDataRates.clear();
 			int tSourceNodeNumber = 0;
 			int tDestinationNodeNumber = 0;
 			for(int i = 0; i < NUMBER_NODE_COMBINATIONS; i++){
 				tSourceNodeNumber = (int)(mRandom.nextFloat() * mCntNodes);
 				Logging.warn(this, "Selected source node: " + tSourceNodeNumber);
 				tDestinationNodeNumber = (int)(mRandom.nextFloat() * mCntNodes);
 				Logging.warn(this, "Selected destination node: " + tDestinationNodeNumber);
 				while(tSourceNodeNumber == tDestinationNodeNumber){
 					tDestinationNodeNumber = (int)(mRandom.nextFloat() * mCntNodes);
 					Logging.warn(this, "Selected destination node: " + tDestinationNodeNumber);
 				}
 				mSource.add(mGlobalNodeList.get(tSourceNodeNumber));
 				mDestination.add(mGlobalNodeList.get(tDestinationNodeNumber));
 				mDataRates.add((int)(mRandom.nextFloat() * MAX_DATA_RATE_STD_CONNECTION));
 			}
 			tSourceNodeNumber = (int)(mRandom.nextFloat() * mCntNodes);
 			tDestinationNodeNumber = (int)(mRandom.nextFloat() * mCntNodes);
 			Logging.warn(this, "Selected REF destination node: " + tDestinationNodeNumber);
 			while(tSourceNodeNumber == tDestinationNodeNumber){
 				tDestinationNodeNumber = (int)(mRandom.nextFloat() * mCntNodes);
 				Logging.warn(this, "Selected REF destination node: " + tDestinationNodeNumber);
 			}
 			mRefSource = mGlobalNodeList.get(tSourceNodeNumber);
 			mRefDestination = mGlobalNodeList.get(tDestinationNodeNumber);
 			
 			/**
 			 * Create the QoS connections
 			 */
 			Logging.warn(this, "########## Creating QoS connections now...");
 			HRMController.ENFORCE_BE_ROUTING = false;
 			createConnections();
 
 			/**
 			 * Wait some time
 			 */
 			Logging.warn(this, "########## Waiting for QoS connections now...");
 			try {
 				Thread.sleep(1000);
 			} catch (InterruptedException tExc) {
 			}
 
 			/**
 			 * Create statistics for HRM
 			 */
 			int tHRMConnectionsWithFulfilledQoS = countConnectionsWithFulfilledQoS();
 			LinkedList<Double> tStoredUtilBasedOnHRM = new LinkedList<Double>();
 			for(int i = 0; i < mCntBuss; i++){
 				Bus tBus = mGlobalBusList.get(i);
 				Logging.warn(this, tBus + " (HRM)=> " + tBus.getUtilization());
 				tStoredUtilBasedOnHRM.add(tBus.getUtilization());
 			}
 			
 			/**
 			 * Destroy all QoS connections
 			 */
 			Logging.warn(this, "########## Destroying QoS connections now...");
 			resetAllConnetions();
 			
 			
 			if(!GLOBAL_ERROR){
 				/**
 				 * Create the BE connections
 				 */
 				Logging.warn(this, "########## Creating BE connections now...");
 				HRMController.ENFORCE_BE_ROUTING = true;
 				createConnections();
 	
 				/**
 				 * Wait some time
 				 */
 				Logging.warn(this, "########## Waiting for BE connections now...");
 				try {
 					Thread.sleep(1000);
 				} catch (InterruptedException tExc) {
 				}
 	
 				/**
 				 * Create statistics for BE
 				 */
 				int tBEConnectionsWithFulfilledQoS = countConnectionsWithFulfilledQoS();
 				LinkedList<Double> tStoredUtilBasedOnBE = new LinkedList<Double>();
 				for(int i = 0; i < mCntBuss; i++){
 					Bus tBus = mGlobalBusList.get(i);
 					Logging.warn(this, tBus + " (BE)=> " + tBus.getUtilization());
 					tStoredUtilBasedOnBE.add(tBus.getUtilization());
 				}
 	
 				/**
 				 * Write statistics to file
 				 */
 				Logging.warn(this, "   ..HRM connections with fulfilled QoS: " + tHRMConnectionsWithFulfilledQoS);
 				Logging.warn(this, "   ..BE connections with fulfilled QoS: " + tBEConnectionsWithFulfilledQoS);
 				Logging.warn(this, "   ..HRM REF connections with fulfilled QoS: " + mRefHRMConnections);
 				Logging.warn(this, "   ..BE REF connections with fulfilled QoS: " + mRefBEConnections);
 				if(mTurn == 1){
 					try {
 						mStatistic = Statistic.getInstance(mLocalNodeAS.getSimulation(), HRMTestApp.class, ";", false);
 					} catch (Exception tExc) {
 						Logging.err(this, "Can not write statistic log file", tExc);
 					}
 	
 					if(mStatistic != null){
 						Runtime.getRuntime().addShutdownHook(new Thread() {
 							@Override
 							public void run()
 							{
 								Logging.getInstance().warn(this, "Closing HRMController statistics log file");
 								mStatistic.close();
 							}
 						});
 					}
 	
 					LinkedList<String> tTableHeader = new LinkedList<String>();
 					tTableHeader.add("Turn");
 					tTableHeader.add("Good HRM routing");
 					tTableHeader.add("Good BE routing");
 					tTableHeader.add("-");
 					tTableHeader.add("Ref source");
 					tTableHeader.add("Ref destination");
 					tTableHeader.add("Good HRM Ref routing");
 					tTableHeader.add("Good BE Ref routing");
 					tTableHeader.add("-");
 					tTableHeader.add("BusCounter");
 					for(int i = 0; i < mCntBuss; i++){
 						tTableHeader.add("HRM_" + mGlobalBusList.get(i).getName());
 					}
 					for(int i = 0; i < mCntBuss; i++){
 						tTableHeader.add("BE_" + mGlobalBusList.get(i).getName());
 					}
 					if(mStatistic != null){
 						mStatistic.log(tTableHeader);
 					}
 				}
 				LinkedList<String> tTableRow = new LinkedList<String>();
 				tTableRow.add(Integer.toString(mTurn));
 				tTableRow.add(Integer.toString(tHRMConnectionsWithFulfilledQoS));
 				tTableRow.add(Integer.toString(tBEConnectionsWithFulfilledQoS));
 				tTableRow.add("-");
 				tTableRow.add(mRefSource.toString());
 				tTableRow.add(mRefDestination.toString());
 				tTableRow.add(Integer.toString(mRefHRMConnections));
 				tTableRow.add(Integer.toString(mRefBEConnections));
 				tTableRow.add("-");
 				tTableRow.add(Integer.toString(mCntBuss));
 				for(int i = 0; i < mCntBuss; i++){
 					DecimalFormat tFormat = new DecimalFormat("0.#");
 					String tUtilizationStr = tFormat.format(tStoredUtilBasedOnHRM.get(i));
 					tTableRow.add(tUtilizationStr);
 				}			
 				for(int i = 0; i < mCntBuss; i++){
 					DecimalFormat tFormat = new DecimalFormat("0.#");
 					String tUtilizationStr = tFormat.format(tStoredUtilBasedOnBE.get(i));
 					tTableRow.add(tUtilizationStr);
 				}
 				if(mStatistic != null){
 					mStatistic.log(tTableRow);
 					Logging.log(this, ">>>>>>>>>> Writing statistics to file..");
 					mStatistic.flush();
 				}
 			}
 
 			/**
 			 * Destroy all QoS connections
 			 */
 			Logging.warn(this, "########## Destroying BE connections now...");
 			resetAllConnetions();
 
 			mTurn++;
 			
 			if(mTurn > NUMBER_MEASUREMENT_TURNS){
 				Logging.warn(this, "########## END OF MEASUREMENTS #########");
 				break;
 			}
 			
 			if(GLOBAL_ERROR){
 				Logging.err(this, "########## GLOBAL ERROR - END OF MEASUREMENTS #########");
 				break;
 			}
 		}//while
 
 		/**
 		 * END
 		 */
 		Logging.log(this, "Main loop finished");
 		mHRMTestAppRunning = false;
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
 		mHRMTestAppNeeded = false;
 		
 		// wakeup
 		if(isRunning()){
 			notifyAll();
 		}
 
 		for(QoSTestApp tQoSTestApp: mQoSTestApps.values()){
 			Logging.log(this, "Closing QoSTestApp: " + tQoSTestApp);
 			tQoSTestApp.exit();
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
 		return mHRMTestAppRunning;
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
