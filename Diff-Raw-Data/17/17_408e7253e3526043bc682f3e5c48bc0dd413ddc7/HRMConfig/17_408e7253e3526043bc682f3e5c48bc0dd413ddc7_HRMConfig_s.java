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
 		public static final boolean GUI_SHOW_RELATIVE_ADDRESSES = true;
 
 		/**
 		 * Show debug outputs about the timing of report/share phases of each existing coordinator.
 		 */
		public static final boolean GUI_SHOW_TIMING_ROUTE_DISTRIBUTION = true;
 
 		/**
 		 * Shows debug outputs for each received packet of a channel.
 		 */
 		public static final boolean SHOW_RECEIVED_CHANNEL_PACKETS = true;
 
 		/**
 		 * Shows debug outputs for each received RoutingInformation packet.
 		 */
 		public static final boolean SHOW_RECEIVED_ROUTING_INFO = false;
 
 		/**
 		 * Defines the time period between two updates of the node specific HRM display.
 		 */
 		public static final double GUI_NODE_DISPLAY_UPDATE_INTERVAL = 1.0;
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
 		 * specifies whether the hierarchy is created automatically,
 		 * otherwise it is done step by step
 		 */
 		public static final boolean BUILD_AUTOMATICALLY = false; //TV
 		
 		/**
 		 * Identifies the base level of the hierarchy.
 		 */
 		public static final int BASE_LEVEL = 0;		
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
 		 * Disables/enables the region limitation feature which can be used to implement an obstructive/restrictive routing based on the HRM infrastructure.
 		 */
 		public static final boolean ENABLE_REGION_LIMITATION = false; //TV
 
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
