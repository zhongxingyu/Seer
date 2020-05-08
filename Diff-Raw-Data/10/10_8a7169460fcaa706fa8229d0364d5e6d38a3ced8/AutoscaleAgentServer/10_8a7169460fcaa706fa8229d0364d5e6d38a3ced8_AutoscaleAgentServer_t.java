 package no.uio.master.autoscale.agent.service;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.List;
 import java.util.Map.Entry;
 import java.util.concurrent.Executors;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.TimeUnit;
 
 import no.uio.master.autoscale.agent.config.Config;
 import no.uio.master.autoscale.agent.config.YamlReader;
 import no.uio.master.autoscale.agent.host.CassandraNodeCmd;
 import no.uio.master.autoscale.agent.host.NodeCmd;
 import no.uio.master.autoscale.message.AgentMessage;
 import no.uio.master.autoscale.message.enumerator.AgentMessageType;
 import no.uio.master.autoscale.message.enumerator.AgentStatus;
 import no.uio.master.autoscale.net.Communicator;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.common.base.Strings;
 
 public class AutoscaleAgentServer implements Runnable {
 	private static Logger LOG = LoggerFactory.getLogger(AutoscaleAgentServer.class);
 	private static Communicator communicator;
 
 	private static AutoscaleAgentDaemon daemon = null;
 	private static ScheduledExecutorService executor;
 
 	private NodeCmd nodeCmd;
 
 	public AutoscaleAgentServer() throws IOException {
 		try {
 			YamlReader.loadYaml();
 		} catch (FileNotFoundException e) {
 			LOG.warn("Failed to load yaml ",e);
 		}
 		
 		nodeCmd = new CassandraNodeCmd(Config.node_address, Config.node_port);
 		communicator = new Communicator(Config.slave_input_port, Config.slave_output_port);
 	}
 
 	@Override
 	public void run() {
 		AgentMessage msg = (AgentMessage) communicator.readMessage();
 		try {
 			if(null != msg) {
 				performAction(msg);
 			}
 		} catch (Exception e) {
 			LOG.error("Failed while performing action ", e);
 		}
 
 	}
 
 	/**
 	 * Perform action, based upon SlaveMessage.type
 	 * 
 	 * @param msg
 	 * @throws IOException
 	 * @throws InterruptedException
 	 */
 	public void performAction(AgentMessage msg) throws IOException, InterruptedException {
 		LOG.debug("Performing action upon message: {}",msg);
 		switch (msg.getType()) {
 		case STARTUP_NODE:
 			nodeCmd.startupNode();
 			initAgent();
 		case UPDATE:
 			updateConfig(msg);
 			break;
 
 		case STOP_AGENT:
 			stopAgent();
 			break;
 
 		case START_AGENT:
 			initAgent();
			updateConfig(msg);
 			break;
 
 		case SHUTDOWN_NODE:
 			nodeCmd.shutdownNode(nodeCmd.getProcessId());
 			break;
 			
 		case STATUS:
 			if(!msg.getMap().isEmpty()) {
 				Entry<String, Object> entry = msg.getMap().entrySet().iterator().next();
 				getStatus((AgentStatus)entry.getValue());
 			}
 			break;
 
 		default:
 			LOG.error("Slave message-type didn't match any predefined types!");
 			break;
 		}
 
 	}
 	
 	private void getStatus(AgentStatus status) {
 		Communicator communicator = new Communicator(Config.slave_input_port, Config.slave_output_port);
 		AgentMessage msg = new AgentMessage(AgentMessageType.STATUS);
 		switch (status) {
 		case LIVE_NODES:
 			List<String> liveNodes = nodeCmd.getActiveNodes();
 			msg.put(AgentStatus.LIVE_NODES.toString(), liveNodes);
 			break;
 			
 		default:
 			LOG.debug("Missing implementation for status {}",status.toString());
 			break;
 		}
 		
 		LOG.debug("Sending status message {}",msg);
 		communicator.sendMessage(Config.master_host, msg);
 		communicator = null;
 	}
 
 	/**
 	 * Update local Configurations with configurations received from master
 	 * 
 	 * @param msg
 	 */
 	private void updateConfig(AgentMessage msg) {
		LOG.info("Update configurations");
 
 		if (msg.getMap().containsKey("intervall_timer")) {
 			Config.intervall_timer = (Integer) msg.getMap().get("intervall_timer");
 		}
 
 		if (msg.getMap().containsKey("threshold_breach_limit")) {
 			Config.threshold_breach_limit = (Integer) msg.getMap().get("threshold_breach_limit");
 		}
 
 		if (msg.getMap().containsKey("min_memory_usage")) {
 			Config.min_memory_usage = (Double) msg.getMap().get("min_memory_usage");
 		}
 
 		if (msg.getMap().containsKey("max_memory_usage")) {
 			Config.max_memory_usage = (Double) msg.getMap().get("max_memory_usage");
 		}
 
 		if (msg.getMap().containsKey("min_free_disk_space")) {
 			Config.min_free_disk_space = (Long) msg.getMap().get("min_free_disk_space");
 		}
 
 		if (msg.getMap().containsKey("max_free_disk_space")) {
 			Config.max_free_disk_space = (Long) msg.getMap().get("max_free_disk_space");
 		}
 
 		Config.master_host = (String) msg.getSenderHost();
 		LOG.info("Connected with master host: {}", (String)msg.getSenderHost());
 	}
 
 	/**
 	 * Initialize / Re-initialize daemon
 	 */
 	private void initAgent() {
		LOG.info("Initialize agent");
 		if (null != daemon) {
 			executor.shutdownNow();
 		}
 		daemon = new AutoscaleAgentDaemon();
 		executor = Executors.newSingleThreadScheduledExecutor();
 		executor.scheduleAtFixedRate(daemon, 0, Config.intervall_timer, TimeUnit.SECONDS);
 	}
 
 	/**
 	 * Shutdown currently running daemon (NOT the server)
 	 */
 	private void stopAgent() {
		LOG.info("Stop agent");
 		executor.shutdown();
 	}
 }
