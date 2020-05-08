 package aic12.project3.service.loadBalancing;
 
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
 import java.util.Map;
 import java.util.Observer;
 
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 
 import aic12.project3.common.beans.SentimentProcessingRequest;
 import aic12.project3.common.config.ServersConfig;
 import aic12.project3.common.enums.NODE_STATUS;
 import aic12.project3.service.nodeManagement.ILowLevelNodeManager;
 import aic12.project3.service.nodeManagement.IdleNodeHandler;
 import aic12.project3.service.nodeManagement.Node;
 import aic12.project3.service.util.LoggerLevel;
 import aic12.project3.service.util.FifoWithAverageCalculation;
 import aic12.project3.service.util.ManagementConfig;
 import aic12.project3.service.util.ManagementLogger;
 
 
 public class HighLevelNodeManagerImpl implements IHighLevelNodeManager {
 
 	private Map<String, Node> nodes = Collections.synchronizedMap(new HashMap<String,Node> ()); // nodeID <-> Node
 	private Map<String, Node> processRequest_nodes = Collections.synchronizedMap(new HashMap<String, Node> ()); // requestID <-> Node
 	@Autowired ILowLevelNodeManager lowLvlNodeMan;
 	@Autowired private ManagementConfig config;
 	@Autowired private ManagementLogger managementLogger;
 	@Autowired private ServersConfig serversConfig;
 	private FifoWithAverageCalculation startupTimes;
 	private int maxNodeCount;
 	private int minNodeCount;
 	
 	private String clazz = this.getClass().getName();
	private List<String> nodesToBeStopped = Collections.synchronizedList(new LinkedList<String>());
 	
 	public void init() {
 		int size = Integer.parseInt(config.getProperty("nodeStartupTimesCache"));
 		startupTimes = new FifoWithAverageCalculation(size);
 		maxNodeCount = Integer.parseInt(config.getProperty("amountOfSentimentNodes"));
 		minNodeCount = Integer.parseInt(config.getProperty("minimumNodes"));
 	}
 	
 	@Override
 	public Map<String, Node> getNodes() {
 		return processRequest_nodes;
 	}
 
 	@Override
 	public void stopNode(String id) {
 		managementLogger.log(clazz, LoggerLevel.INFO, "stopNode");
 		if(nodes.size() > minNodeCount) {
 			stopNode_internal(nodes.get(id));
 		} else {
 			managementLogger.log(clazz, LoggerLevel.INFO, "minimum nodes reached, not stopping");
 		}
 	}
 
 	private void stopNode_internal(Node node) {
 		managementLogger.log(clazz, LoggerLevel.INFO, "stopNode_internal" + node.getIp());
 		node.setStatus(NODE_STATUS.STOPPED);
		nodes.remove(node.getId());
		nodesToBeStopped.remove(node.getId());
 		lowLvlNodeMan.stopNode(node.getId());
 		managementLogger.log(clazz, LoggerLevel.INFO, "stopNode_internal finished!" + node.getIp());
 	}
 	
 	private void stopNodeSchedule(Node node) {
 		managementLogger.log(clazz, LoggerLevel.INFO, "stopNodeSchedule " + node.getIp());
		nodesToBeStopped.add(node.getId());
 		node.setStatus(NODE_STATUS.SCHEDULED_FOR_STOP);
 	}
 	
 	@Override
 	public Node getNode(String id) {
 		return nodes.get(id);
 	}
 
 	@Override
 	public Node getMostAvailableNode(){
 		Collection<Node> nodeVals = nodes.values();
 		synchronized (nodes) {
 			for(Node node : nodeVals) {
 				if (node.getStatus() == NODE_STATUS.IDLE){
 					return node;
 				}
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Start a node implementation
 	 */
 	@Override
 	public synchronized Node startNode() {
 		//Check if resources to start new node are available
 		if (nodes.size() >= maxNodeCount){
 			// If not available return null
 			return null;
 		} else {
 			/*
 			 * Create new Node from available image
 			 */
 			Node n = lowLvlNodeMan.startNode(config.getProperty("serverNameSentiment"), config.getProperty("sentimentImageId"), config.getProperty("serverFlavor"));
 			n.setName(config.getProperty("serverNameSentiment"));
 			/*
 			 * Set Node Status to Starting
 			 */
 			n.setStatus(NODE_STATUS.STARTING);
 			nodes.put(n.getId(), n);
 
 			/*
 			 * Start waiter for node to become available
 			 */
 			new NodeAlivePollingThread(n, config, lowLvlNodeMan, this, managementLogger).start();
 
 			return n;
 		}
 
 	}
 
 	@Override
 	public void sendRequestToNode(Node node, SentimentProcessingRequest request) {
 		node.setStatus(NODE_STATUS.BUSY);
 		IdleNodeHandler.updateLastVisit(node);
 		new RequestSenderThread(node, request, serversConfig, managementLogger).start();
 
 		// Also save assignment
 		processRequest_nodes.put(request.getId(), node);
 	}
 
 	@Override
 	public int getRunningNodesCount() {
 		return nodes.size();
 	}
 
 	@Override
 	public void setNodeIdle(SentimentProcessingRequest request) {
 		Node node = this.processRequest_nodes.get(request.getId());
 		managementLogger.log(clazz, LoggerLevel.INFO, "setNodeIdle " + node.getIp());
 		
 		synchronized (node) {
 			if(node.getStatus() == NODE_STATUS.SCHEDULED_FOR_STOP) {
 				managementLogger.log(clazz, LoggerLevel.INFO, "node was scheduled for stop");
 				stopNode_internal(node);
 				return;
 			}
 			
 			if(node.getStatus() == NODE_STATUS.STOPPED) {
 				return; // don't set stopped nodes to idle (synchronization)
 			}
 			node.setStatus(NODE_STATUS.IDLE);
 			IdleNodeHandler.updateLastVisit(node);
 			
 			processRequest_nodes.remove(request.getId());
 		}
 	}
 
 	@Override
 	public synchronized void runDesiredNumberOfNodes(int desiredNodeCount, Observer observer) {
 		if(desiredNodeCount > maxNodeCount) {
 			desiredNodeCount = maxNodeCount;
 		}
 		
 		int diff = desiredNodeCount - this.getRunningNodesCount();
 		if (diff > 0){
 			for (int i = 0; i < diff; i++) {
 				Node startedNode = this.startNode();
 				if (startedNode == null) {
 					break;
 				} else {
 					startedNode.addObserver(observer);
 				}
 			}
 		} else if (diff < 0) {
			for(int i = diff + nodesToBeStopped.size(); i < 0; i++) {
 				scheduleAnyNodeForStopping_internal();
 			}
 		}
 	}
 	
 	private void scheduleAnyNodeForStopping_internal() {
 		if(nodes.size() <= minNodeCount) {
 			System.out.println("not scheduling for stopping, minNodeCount reached");
 			return;
 		}
 		
 		synchronized (nodes) {
 			Iterator<Node> it = nodes.values().iterator();
 			while(it.hasNext()) {
 				Node nodeToStop = it.next();
 				if(nodeIsRunning_internal(nodeToStop)) {
 					stopNodeSchedule(nodeToStop);
 					System.out.println("node " + nodeToStop.getIp() + " has been scheduled for being stopped");
 					return;
 				}
 			}
 			System.out.println("warning: scheduleAnyNodeForStopping_internal called and no node available");
 		}
 	}
 
 	private boolean nodeIsRunning_internal(Node node) {
 		return node.getStatus() == NODE_STATUS.IDLE || node.getStatus() == NODE_STATUS.BUSY;
 		
 	}
 
 	@Override
 	public int getNodeStartupTime() {
 		if(startupTimes.isEmpty()) {
 			return Integer.parseInt(config.getProperty("timeToStartup"));
 		}
 		return startupTimes.calculateAverage();
 	}
 
 	@Override
 	public void addNodeStartupTime(long timeToStartup) {
 		startupTimes.add(timeToStartup);
 	}
 
 	@Override
 	public int getNodeShutdownTime() {
 		return getNodeStartupTime(); // we can't track shutdown times
 	}
 
 	@Override
 	public int getMaxNodeCount() {
 		return maxNodeCount;
 	}
 
 	@Override
 	public int getMinNodeCount() {
 		return minNodeCount;
 	}
 }
