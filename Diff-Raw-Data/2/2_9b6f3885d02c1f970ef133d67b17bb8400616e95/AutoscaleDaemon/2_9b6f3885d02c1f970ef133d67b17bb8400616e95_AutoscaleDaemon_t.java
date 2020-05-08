 package no.uio.master.autoscale.service;
 
 import java.util.Set;
 import java.util.concurrent.Executors;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.TimeUnit;
 
 import no.uio.master.autoscale.config.Config;
 import no.uio.master.autoscale.host.CassandraHost;
 import no.uio.master.autoscale.host.CassandraHostManager;
 import no.uio.master.autoscale.host.Host;
 import no.uio.master.autoscale.host.HostManager;
 import no.uio.master.autoscale.message.AgentMessage;
 import no.uio.master.autoscale.message.enumerator.AgentMessageType;
 import no.uio.master.autoscale.net.Communicator;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * The runnable instance of Autoscale, for external usage.
  * 
  * @author toraba
  * 
  */
 public class AutoscaleDaemon implements Runnable {
 	private static Logger LOG = LoggerFactory.getLogger(AutoscaleDaemon.class);
 	private static HostManager hostManager;
 	private static Set<Host> agents;
 
 	private static AgentListener agentListener;
 	private static Communicator communicator;
	private static Scaler scaler;
 
 	private static int hostUpdater = 0;
 	private final int UPDATE_NODELIST_COUNTER = 5;
 
 	/**
 	 * Initialize autoscaler
 	 * 
 	 * @param clusterName
 	 * @param host
 	 */
 	public AutoscaleDaemon() {
 		init();
 		LOG.debug("Daemon started");
 	}
 
 	private void init() {
 		hostManager = new CassandraHostManager();
 		initializeAgents(hostManager.getActiveHosts());
 		initalizeListener();
 		initializeScaler(agentListener);
 	}
 
 	@Override
 	public void run() {
 		LOG.debug("Daemon running...");
 		agentListener.listenForMessage();
 
 		// Update hosts every n-th run
 		// frequency depends on message-retrieval
 		if(hostUpdater < UPDATE_NODELIST_COUNTER) {
 			hostUpdater++;
 		} else {
 			checkForNewHosts();
 			hostUpdater = 0;
 		}
 	}
 
 	protected void checkForNewHosts() {
 			// Update slaves-list
 			Set<Host> oldSlaves = agents;
 			agents = hostManager.getActiveHosts();
 			Set<Host> initSlaves = agents;
 			initSlaves.removeAll(oldSlaves);
 
 			// Initialize any new slaves
 			if (!initSlaves.isEmpty()) {
 				initializeAgents(initSlaves);
 			}
 	}
 
 	/**
 	 * Send initialization-message to all agents.<br>
 	 * Node should already be running.
 	 * 
 	 * @param nodes
 	 *            to initialize
 	 */
 	protected void initializeAgents(Set<Host> nodes) {
 
 		// Construct agent-message
 		AgentMessage agentMsg = new AgentMessage(AgentMessageType.START_AGENT);
 
 		// Send message to all slaves
 		for (Host host : nodes) {
 			communicator = new Communicator(Config.master_input_port, Config.master_output_port);
 			communicator.sendMessage(host.getHost(), agentMsg);
 			communicator = null;
 		}
 	}
 
 	/**
 	 * Initialize the scaler, which will run alongside the autoscaler.<br>
 	 * The scaler will collect received breach-messages from the AgentListener
 	 * at a given interval, and perform analysis upon messages, and eventually
 	 * perform scaling of the cluster, at the desired locations.
 	 * 
 	 * @param listener
 	 */
 	protected void initializeScaler(AgentListener listener) {
 		scaler = new SimpleCassandraScaler(listener, hostManager);
 		ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
 		executor.scheduleAtFixedRate(scaler, 0, Config.intervall_timer_scaler, TimeUnit.SECONDS);
 	}
 	
 	protected void initalizeListener() {
 		agentListener = new AgentListener();
 		ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
 		executor.scheduleAtFixedRate(agentListener, 0, 1, TimeUnit.SECONDS);
 	}
 
 }
