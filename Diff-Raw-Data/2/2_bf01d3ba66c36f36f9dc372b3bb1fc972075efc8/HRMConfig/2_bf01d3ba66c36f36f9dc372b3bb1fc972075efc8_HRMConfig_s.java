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
 
 public class HRMConfig
 {
 	public class DebugOutput
 	{
 		/** 
 		 * Show debug outputs about observable/observer construct, which is used to inform GUIs about HRMController internal changes.
 		 */
 		public static final boolean GUI_SHOW_NOTIFICATIONS = false; 
 		
 		/**
 		 * Avoid duplicated HRMIDs in GUI
 		 */
 		public static final boolean GUI_AVOID_HRMID_DUPLICATES = true;
 
 		/** 
 		 * Show debug outputs about HRMID updates for nodes.
 		 */
 		public static final boolean GUI_HRMID_UPDATES = false;
 
 		/**
 		 * Show relative addresses in the GUI? (e.g., "0.0.1")
 		 */
 		public static final boolean GUI_SHOW_RELATIVE_ADDRESSES = false;
 
 		/**
 		 * Show debug outputs about the timing of report/share phases of each existing coordinator.
 		 */
 		public static final boolean GUI_SHOW_TIMING_ROUTE_DISTRIBUTION = false;
 
 		/**
 		 * Shows debug outputs for each received packet of a channel.
 		 */
 		public static final boolean SHOW_RECEIVED_CHANNEL_PACKETS = true;
 
 		/**
 		 * Shows debug outputs for each received RoutingInformation packet.
 		 */
 		public static final boolean SHOW_SHARE_PHASE = false;
 
 		/**
 		 * Shows debug outputs about the routing process 
 		 */
 		public static final boolean GUI_SHOW_ROUTING = false;
 
 		/**
 		 * Show debug outputs about node/link detection
 		 */
 		public static final boolean GUI_SHOW_TOPOLOGY_DETECTION = true;
 
 		/**
 		 * Shows debug outputs about multiplex packets 
 		 */
 		public static final boolean GUI_SHOW_MULTIPLEX_PACKETS = false;
 
 		/**
 		 * Shows general debug outputs about signaling messages
 		 */
 		public static final boolean GUI_SHOW_SIGNALING = true;
 
 		/**
 		 * Shows detailed debug outputs about Bully related signaling messages
 		 */
 		public static final boolean GUI_SHOW_SIGNALING_BULLY = true;
 
 		/**
 		 * Shows detailed debug outputs about HRMID signaling messages
 		 */
 		public static final boolean GUI_SHOW_SIGNALING_ADDRESSING = false;
 
 		/**
 		 * Shows detailed debug outputs about HRMViewer steps
 		 */
 		public static final boolean GUI_SHOW_VIEWER_STEPS = false;
 
 		/**
 		 * Shows coordinators in the ARG viewer
 		 * HINT: clusters HAVE TO BE STORED in the ARG, otherwise routing isn't possible
 		 */
 		public static final boolean GUI_SHOW_COORDINATORS_IN_ARG = false;
 
 		/**
 		 * Defines the time period between two updates of the node specific HRM display.
 		 */
 		public static final double GUI_NODE_DISPLAY_UPDATE_INTERVAL = 1.0;
 
 		/**
 		 * Defines if the hierarchy creation should start once the entire simulation was created. 
 		 */
		public static final boolean BLOCK_HIERARCHY_UNTIL_END_OF_SIMULATION_CREATION = true;
 	}
 	
 	public class Addressing
 	{
 		/**
 		 * Specifies whether the address are assigned automatically,
 		 * otherwise it has to be triggered step by step via the GUI.
 		 */
 		public static final boolean ASSIGN_AUTOMATICALLY = true;
 		
 		/**
 		 * Defines the address which is used for cluster broadcasts
 		 */
 		public static final long BROADCAST_ADDRESS = 0;
 	}
 	
 	public class Hierarchy
 	{
 		/**
 		 * amount of hierarchical levels in the simulation
 		 */
 		public static final int HEIGHT = 3; // TV
 
 		/**
 		 * this limits the maximum amount of nodes inside one cluster and defined the space which is used for selecting a hierarchy level
 		 */
 		public static final int USED_BITS_PER_LEVEL = 8; //TV
 
 		/**
 		 * The same like START_AUTOMATICALLY but restricted to base hierarchy level
 		 */
 		public static final boolean START_AUTOMATICALLY_BASE_LEVEL = true; 
 
 		/**
 		 * This specifies whether the hierarchy build process is continued automatically.
 		 * Otherwise, it is done step by step by the help of GUI and user inputs.
 		 */
 		public static final boolean CONTINUE_AUTOMATICALLY = true;
 		
 		/**
 		 * Defines if signaling (e.g., broadcasts) also includes the local host.
 		 * In this case, signaling also sends packets in a loop back to the sender.
 		 * This causes ADDITIONAL LOOPBACK SIGNALING.
 		 */
 		public static final boolean SIGNALING_INCLUDES_LOCALHOST = false;
 
 		/**
 		 * Defines if a coordinator may join an existing local superior cluster.
 		 */
 		public static final boolean COORDINATORS_MAY_JOIN_EXISTING_SUPERIOR_CLUSTERS = true;
 
 		/**
 		 * Defines if connection should remain open or be automatically closed if the last inferior comm. channel was closed
 		 */
 		public static final boolean AUTO_CLEANUP_FOR_CONNECTIONS = true;
 	}
 	
 	/**
 	 * Configuration parameters for the routing process and routing service
 	 */
 	public class Routing
 	{
 		/**
 		 * Maximum radius that is allowed during expansion phase 
 		 */
 		public static final int EXPANSION_RADIUS = 4; //TV
 		
 		/**
 		 * Should each HRS instance try to avoid duplicates in its internal routing tables?
 		 * In this case, also updates of routing table entries are made if the new route has better QoS values than the old one.
 		 */
 		public static final boolean AVOID_DUPLICATES_IN_ROUTING_TABLES = true;
 
 		/**
 		 * Defines the hop costs for a route to a direct neighbor. 
 		 */
 		public static final int HOP_COSTS_TO_A_DIRECT_NEIGHBOR = 1;
 
 		/**
 		 * Defines the time between two triggers for the HRMController/node specific "share phase"
 		 * The higher in the hierarchy a coordinator is, the higher is the multiplier for this value.
 		 */
 		public static final double GRANULARITY_SHARE_PHASE = 2.0; // in seconds
 		
 		/**
 		 * Should the packets of the "share phase" be send periodically?
 		 * If a distributed simulation (span a network over multiple physical nodes) is used, this value has to be set to "true". 
 		 */
 		public static final boolean PERIODIC_SHARE_PHASES = false;
 
 		/**
 		 * Define if the HRM based route should be recorded in a ProbeRoutingProperty if the connection  request uses this property.
 		 */		
 		public static final boolean RECORD_ROUTE_FOR_PROBES = true; 
 	}
 
 	public class Debugging
 	{
 		/**
 		 * (De-)activates the usage of HRMIDs when using toString() for clusters.
 		 */
 		public static final boolean PRINT_HRMIDS_AS_CLUSTER_IDS = false; //TV
 	}
 	
 	/**
 	 * Configuration for the election process
 	 */
 	public class Election //TV
 	{
 		/**
 		 * Default priority for election process. This value is used when no value is explicitly defined for a node.
 		 */
 		public static final long DEFAULT_BULLY_PRIORITY = 1; //TV
 		
 		/**
 		 * (De-)activate sending of BullyAlive messages in order to detect dead cluster members.
 		 */
 		public static final boolean SEND_BULLY_ALIVES = true;
 	}
 }
