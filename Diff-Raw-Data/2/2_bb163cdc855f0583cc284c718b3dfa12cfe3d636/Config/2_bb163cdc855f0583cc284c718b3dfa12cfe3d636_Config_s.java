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
 package de.tuilmenau.ics.fog;
 
 import java.util.Locale;
 
 import de.tuilmenau.ics.fog.Config.Simulator.SimulatorMode;
 import de.tuilmenau.ics.fog.transfer.manager.Controller.RerouteMethod;
 import de.tuilmenau.ics.fog.ui.Logging.Level;
 
 
 public class Config
 {
 	private static Config sSingletonConfig = null;
 	synchronized public static Config getConfig()
 	{
 		if(sSingletonConfig == null) {
 			sSingletonConfig = new Config();
 		}
 		return sSingletonConfig;
 	}
 	
 	/**
 	 * Language for output of the simulator. In special,
 	 * it is used for formating numbers for log files of 
 	 * the data stream.
 	 */
 	public static final Locale LANGUAGE = Locale.GERMANY;
 	
 	public static final String PROPERTY_WORKER_NAME = "worker.name";
 		
 	/**
 	 * Default timeout for processes in seconds.
 	 */
 	public static final double PROCESS_STD_TIMEOUT_SEC = 30;
 	
 	/**
 	 * Indicates if some more extended GUI controls and behavior are enabled. 
 	 * Otherwise the simulator behaves in a more secured way for demo sessions.
 	 */
 	public static final boolean DEVELOPER_VERSION = true;
 	
 	/**
 	 * Configuration parameter for the simulator itself and not for
 	 * the simulated things inside a simulation.
 	 */
 	public static class Simulator
 	{
 		public enum SimulatorMode {
 			STEP_SIM,      // Executes events in manual step mode (command to execute next event is 'time')
 			REAL_TIME_SIM, // Simulator is executing the events in a time-based manner related to real-time
 			FAST_SIM,      // Simulation runs in batch mode and executes the events as fast a possible; reduced GUI output
 			EMULATION      // Same as REAL_TIME_SIM, but with interaction with real environment
 		};
 		
 		/**
 		 * Mode of the simulator. Descriptions of the modes are given for the
 		 * type {@link SimulatorMode}.
 		 */
 		public static final SimulatorMode MODE = SimulatorMode.REAL_TIME_SIM;
 		
 		/**
 		 * Time steps in time based mode if no events are processed.
 		 * The duration is important for other processed not working
 		 * with the event queue (e.g. Worker and packet delivery).
 		 */
 		public static final int REAL_TIME_GRANULARITY_MSEC = 5;
 		public static final double REAL_TIME_GRANULARITY_SEC = (double)REAL_TIME_GRANULARITY_MSEC / 1000.0d;
 		
 		/**
 		 * Indicates if the OSGi container (e.g. Equinox) should be terminated
 		 * after last simulation was terminated. Termination is needed, if another
 		 * script is waiting for the termination. Esp. it is needed for restarting
 		 * with different parameters.
 		 */
 		public static final boolean EXIT_OSGI_CONTAINER_AFTER_LAST_SIMULATON = true;		
 	}
 
 	public class Logging
 	{
 		public Level LOG_LEVEL = Level.TRACE;
 		
 		/**
 		 * Indicates if Logging should print all messages to std out
 		 * in addition to all registered loggers. This output is helpful
 		 * for debugging the simulator (running e.g. as command line
 		 * application) without the need to register a special logger
 		 * for std out. In special this enables the output of messages
 		 * to std out on start-up before such a logger can be registered.   
 		 */
 		public static final boolean LOG_ALWAYS_TO_STD_OUT = true;
 		
 		/**
 		 * Put date and time in front of log message?
 		 * 0 - disabled
 		 * 1 - with time
 		 * 2 - with time and date
 		 */
		public static final int LOG_WITH_DATE_AND_TIME = 2;
 		
 		/**
 		 * Enable logging via Eclipse in full mode. If disabled, not all
 		 * consoles are created or turned on.
 		 */
 		public static final boolean ECLIPSE_CONSOLE_LOG_FULL = false;
 
 		/**
 		 * Enables the logging of individual packets at several
 		 * measurement points in the simulation. 
 		 */
 		public static final boolean PACKET_LOGGER_ENABLED = true;
 		
 		/**
 		 * Defines after how many seconds packets are removed from
 		 * packet loggers. Negative values indicate that the packet
 		 * should not be deleted due to timing reasons. Than the
 		 * oldest packets are removed if the buffer is full.
 		 */
 		public static final double PACKET_LOGGER_HISTORY_SEC = 20;
 		
 		/**
 		 * Enables statistic files, which are logging all packets
 		 * transfered in the network.
 		 */
 		public static final boolean WRITE_PACKET_STATISTIC = true;
 		
 		/**
 		 * Requests packets to be authenticated - currently only used for registration of passed nodes,
 		 * relocate configuration variable if additional authentication mechanisms appear
 		 */
 		public static final boolean AUTHENTICATE_PACKETS = true;
 		
 		/**
 		 * Enables the creation of statistics of created gates, connections, and route
 		 * length per node. Values will be written to Datastream.
 		 */
 		public static final boolean CREATE_NODE_STATISTIC = false;
 
 		/**
 		 * Enables the transfer service to log all its routing service
 		 * requests in a statistic file.
 		 */
 		public static final boolean LOG_ROUTE_REQUEST_RESULTS = false;
 	}
 	/**
 	 * File statistical output will be written to.
 	 * It has to be defined here and not in logging because this option could be modified by simulator.
 	 */
 	public static String STATISTIC_FILE = "";
 
 	public Logging logging = new Logging();
 	
 	
 	public class Scenario
 	{
 		/**
 		 * Enables partial routing services for AS while importing
 		 * scenarios from files.
 		 */
 		public boolean ENABLE_PARTIAL_RS_IN_IMPORT = false;
 		
 		/**
 		 * Default value data rate for links in a scenario in
 		 * kbits per seconds. A negative value indicates infinite
 		 * data rate.
 		 */
 		public int DEFAULT_DATA_RATE_KBIT = -1;
 		public double DEFAULT_DATA_RATE_VARIANCE = 0.0d;
 		
 		/**
 		 * Default value for link delay in a scenario in
 		 * milliseconds. Only non-negative values are allowed. 
 		 */
 		public int DEFAULT_DELAY_MSEC = 0;
 		
 		/**
 		 * Switches between constant bus delay and variable delay
 		 * caused by bandwidth and packet size. 
 		 */
 		public boolean DEFAULT_DELAY_CONSTANT = true;
 
 		/**
 		 * Default value for loss probability of a packet transfered
 		 * via a link in %. Allowed are value between [0, 100].
 		 */
 		public int DEFAULT_PACKET_LOSS_PROP = 0;
 		
 		/**
 		 * Default value for bit error rate of a packet transfered
 		 * via a link in %. Allowed are value between [0, 100].
 		 */
 		public int DEFAULT_BIT_ERROR_PROP = 0;
 		
 		/**
 		 * Name of the node configurator, which is configuring the
 		 * routing service in each node of a simulation.
 		 * If it is not set, the default routing service is used.
 		 */
 		public String ROUTING_CONFIGURATOR = null;
 		
 		/**
 		 * Name of the node configurator, which is configuring the
 		 * applications in each node of a simulation.
 		 * If it is not set, no applications are started.
 		 */
 		public String APPLICATION_CONFIGURATOR = null;
 	}
 	public Scenario Scenario = new Scenario();
 	
 	/**
 	 * Config parameters for the routing process and transfer service
 	 */
 	public static class Transfer
 	{
 		/**
 		 * Global IDs for packets are basically for debugging purposes.
 		 * The flag enables the system to look for a global PacketIDManager
 		 * via JINI. For emulations, the feature should be turned off.
 		 */
 		public static final boolean ENABLE_GLOBAL_PACKET_NUMBERS = (Simulator.MODE != SimulatorMode.EMULATION);
 		
 		/**
 		 * Indicates if the check-timer in {@link de.tuilmenau.ics.fog.transfer.manager.Process} should run
 		 * all the time or just until the process in in operational mode. Default for networks with errors and
 		 * changing topology is {@code true}. For stable scenarios the simulation time can be reduced by setting
 		 * it to {@code false}. Timeouts during the starting period of a process are detected in both cases.
 		 */
 		public static final boolean PROCESS_CHECK_CONTINOUSLY_IN_OPERATING_MODE = true;
 		
 		/**
 		 * Enables packet loss, if link is overloaded.
 		 * The maximum seconds ahead of real time define the level
 		 * of the overload are.
 		 */
 		public static final boolean PACKET_LOSS_AT_LINK_OVERLOAD = false;
 		public static final double MAX_AHEAD_OF_TIME_SEC = 1.0d;
 		
 		/**
 		 * Indicates if verbose debug outputs should be written to console, 
 		 * otherwise no message will occur for a processed IP packet.
 		 */
 		public static final boolean DEBUG_INTEROP_PACKETS = false;
 
 		/**
 		 * Indicates if verbose debug outputs should be written to console, 
 		 * otherwise no message will occur for a processed packet.
 		 */
 		public static final boolean DEBUG_PACKETS = false;
 		
 		public static final double GATE_STD_TIMEOUT_SEC = 30;
 		
 		/**
 		 * Timeout for gates before they enter idle.
 		 */
 		public static final double GATE_UNUSED_TIMEOUT_SEC = Config.Transfer.GATE_STD_TIMEOUT_SEC;
 
 		/**
 		 * Amount of none acknowledged packets can be sent.  
 		 */
 		public static final int ACKNOWLEDGEMENT_WINDOW = 100;
 	}
 
 	/**
 	 * Config parameters for the routing process and routing service
 	 */
 	public class Routing
 	{
 		/**
 		 * During the route calculation of a partial route, the routing service
 		 * might be able to insert multiple intermediate names of forwarding
 		 * nodes in the route. If turned on, the routing service will add all
 		 * the intermediate "anchor points" in the route. The result is some
 		 * kind of loose-source routing. If turned off, only the next name/address
 		 * of an intermediate forwarding node is inserted in the route. The latter
 		 * is the default behaviour of FoG.
 		 */
 		public static final boolean MORE_THAN_ONE_INTERMEDIATE_ADDRESS_IN_ROUTE = false;
 		
 		/**
 		 * If enabled, not all forwarding nodes are named. That reduces the number of
 		 * forwarding nodes a routing service has to handle. However, it complicates
 		 * debugging since such forwarding nodes have the same name in the log.
 		 */
 		public static final boolean REDUCE_NUMBER_FNS = false;
 		
 		/**
 		 * Enables an additional hierarchy level for the simulated routing service (partial routing service).
 		 * If enabled, each FoG node has its own partial routing service instance, which is reporting
 		 * to the routing service of an autonomous system. If disabled, the FoG nodes report directly to
 		 * the higher level routing service instance (e.g. the autonomous system). 
 		 */
 		public static final boolean ENABLE_NODE_RS_HIERARCHY_LEVEL = false;
 		
 		/**
 		 * Indicates if a detector of a failure is aware of the root cause of the failure.
 		 * If, for example, a real node is broken, a detector might not be able to distinguish
 		 * between a link or node failure. To simulate such situations, the flag can be set to
 		 * false.
 		 */
 		public static final boolean ERROR_TYPE_VISIBLE = true;
 		
 		/**
 		 * Reroute method for simulation. If the {@link Config.Routing.REROUTE_USE_HORIZONTAL_GATES} flag
 		 * is not set, the reroute method is just performed for the first packet using a broken link or
 		 * node. Subsequent packets will be dropped.
 		 */
 		public RerouteMethod REROUTE = RerouteMethod.LOCAL;
 		
 		/**
 		 * Enables the creation of horizontal gates for rerouting. They store the
 		 * alternative/backup route for relaying packets "around" an failed element.
 		 */
 		public static final boolean REROUTE_USE_HORIZONTAL_GATES = true;
 		
 		/**
 		 * Activates for rerouting the automatic instantiation of a VideoTranscoding gate in case 
 		 * the property "VariableMediaQuality" is found in the description of the rerouting gate.
 		 */
 		public static final boolean REROUTE_USE_AUTO_VIDEO_TRANSCODER = false;
 	}
 	public Routing routing = new Routing();
 	
 	
 	/**
 	 * Config parameters for the connection related activities.
 	 */
 	public class Connection
 	{
 		/**
 		 * Indicates if the first forwarding node of an outgoing connection
 		 * (ClientFN) is analyzing the data send by the application. If so,
 		 * it searches for an object of type string equaling the
 		 * UPDATE_ROUTE_COMMAND. If such a command is found, the forwarding
 		 * node triggers a route update procedure for its socket.
 		 * This mechanism is used for testing the update procedure manually
 		 * by sending such a command with a console application.     
 		 */
 		public static final boolean ENABLE_UPDATE_ROUTE_BY_COMMAND = true;
 		public static final String UPDATE_ROUTE_COMMAND = "UPDATE ROUTE";
 		
 		/**
 		 * <b>If {@code true} the mapping language will be used to
 		 * establish connections.</b>
 		 * <br/><br/>
 		 * <b>If {@code false} the code from the DA Martin Bengsch will
 		 * be used to establish connections.</b>
 		 */
 		public static final boolean USE_REQU_MAPPING_LANGUAGE = false;
 		
 		/**
 		 * Indicates whether initial request sender initialy only creates an
 		 * {@link de.tuilmenau.ics.fog.facade.Binding} but socket-path
 		 * will not be created before a handshake arrives to be sure effort is
 		 * worth it.
 		 * 
 		 * <br/><br/><b>This field will only be consulted if
 		 * {@link #USE_REQUIREMENTS_IN_DESCRIPTION} is {@code true}.</b>
 		 */
 		public static final boolean LAZY_INITIATOR = false;
 		
 		/**
 		 * Indicates whether request receiver initialy only creates an
 		 * {@link ISOcket} but socket-path will not be created before next
 		 * handshake arrives to be sure effort is worth it.
 		 * 
 		 * <br/><br/><b>This field will only be consulted if
 		 * {@link #USE_REQUIREMENTS_IN_DESCRIPTION} is {@code true}.</b>
 		 */
 		public static final boolean LAZY_REQUEST_RECEIVER = false;
 		
 		/* ********************************************************************
 		 *                                                                    *
 		 * Combination of LAZY_INITIATOR (LI) and LAZY_REQUEST_RECEIVER (LRR) *
 		 * at initiating system (I) and LAZY_REQUEST_RECEIVER (LRR) at        *
 		 * responding system (R) influences number of handshakes:             *
 		 *                                                                    *
 		 * +-----------++-------++-------------+-------------+--------------+ *
 		 * | initiator || resp- || # Please-   |   Open-     | # Handshakes | *
 		 * |  system   || onder ||   Open-     | Connection- | establishing | *
 		 * |           || sys.  || Connection  |   Response  |  connection  | *
 		 * +-----+-----++-------++------+------+-----+-------+--------------+ *
 		 * | LI  | LRR ||  LRR  || I->R | I<-R |  #  | Dir.  | total    Var.| *
 		 * +-----+-----++-------++------+------+-----+-------+---------+----+ *
 		 * |     |     ||       ||   1         |  1    I<-R  |   2     | A  | *
 		 * |     |     ||   x   ||   1  +  1   |  1    I->R  |   3     | B  | *
 		 * |     |  x  ||       ||   1         |  1    I<-R  |   2     | C  | *
 		 * |     |  x  ||   x   ||   2  +  1   |  1    I<-R  |   4     | D  | *
 		 * |  x  |     ||       ||   1  +  1   |  1    I->R  |   3     | E  | *
 		 * |  x  |     ||   x   ||   2  +  1   |  1    I<-R  |   4     | F  | *
 		 * |  x  |  x  ||       ||   1  +  1   |  1    I->R  |   3     | G  | *
 		 * |  x  |  x  ||   x   ||   2  +  1   |  1    I<-R  |   4     | H  | *
 		 * +-----+-----++-------++-------------+-------------+---------+----+ *
 		 *                                                                    *
 		 **********************************************************************/
 		
 		/**
 		 * Indicates whether path-creation-algorithm should try to situate
 		 * new local gates parallel to their existing local partner gates.
 		 * If {@code true} and in case of demanded new gate with unspecified
 		 * target FN but known existent local partner gate, the algorithm tries
 		 * to find out and use partner gates origin FN as target FN for the new
 		 * gate. In all other cases to create gates without specified target FN
 		 * an arbitrary (new) FN will be used as target.
 		 *
 		 * <br/><br/><b>This field will only be consulted if
 		 * {@link #USE_REQUIREMENTS_IN_DESCRIPTION} is {@code true}.</b>
 		 */
 		public static final boolean PARALLELIZE_PARTNER_GATES = true;
 		
 		/**
 		 * Indicates whether new connections arranged with a local service
 		 * should start and run through central multiplexer-node of the host
 		 * instead of passing the services {@link Binding}.
 		 * 
 		 * <br/><br/><b>This field will only be consulted if
 		 * {@link #USE_REQUIREMENTS_IN_DESCRIPTION} is {@code true}.</b>
 		 */
 		public static final boolean SERVER_REDIRECT_TO_MULTIPLEXER = false;
 		
 		/**
 		 * Indicates whether established connection should be observed to detect
 		 * long time idle state without receiving any packets or
 		 * keep-alive-messages from remote peer.
 		 * <br/>If receive-idle-time exceeds {@link IDLE_TIMEOUT_SEC}
 		 * -> Terminate connection.
 		 * 
 		 * <br/><br/><b>This field will only be consulted if
 		 * {@link #USE_REQUIREMENTS_IN_DESCRIPTION} is {@code true}.</b>
 		 */
 		public static final boolean TERMINATE_WHEN_IDLE = false;
 		
 		/**
 		 * Indicates whether established connection should be observed to detect
 		 * long time idle state without sending a packet or keep-alive-message
 		 * to remote peer.
 		 * <br/>If send-idle-time exceeds one third of
 		 * {@link IDLE_TIMEOUT_SEC}
 		 * -> Send a keep-alive-message to peer to prevent his termination.
 		 * 
 		 * <br/><br/><b>This field will only be consulted if
 		 * {@link #USE_REQUIREMENTS_IN_DESCRIPTION} is {@code true}.</b>
 		 */
 		public static final boolean SEND_KEEP_ALIVE_MESSAGES_WHEN_IDLE = false;
 		
 		/**
 		 * Indicates if re use is activated (USE_REQU_MAPPING_LANGUAGE = true!). 
 		 */
 		public static final boolean RE_USE_ACTIVATED = false;
 		
 		/**
 		 * Indicates if Non Functional Requirements should be considered! 
 		 */
 		public static final boolean OPTIMISATION_CRITERIONS_ACTIVATED = false;
 		
 		/**
 		 * Do not use intermediate description of gates but use requirement of applications
 		 */
 		public static final boolean DONT_USE_INTERMEDIATE_DESCRIPTION = true;
 	}
 }
