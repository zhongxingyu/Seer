 package aic12.project3.service.loadBalancing;
 
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Queue;
 import java.util.UUID;
 import java.util.concurrent.locks.Lock;
 import java.util.concurrent.locks.ReentrantLock;
 
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 
 import aic12.project3.common.beans.SentimentProcessingRequest;
 import aic12.project3.common.beans.SentimentRequest;
 import aic12.project3.common.enums.NODE_STATUS;
 import aic12.project3.common.enums.REQUEST_QUEUE_STATE;
 import aic12.project3.service.nodeManagement.IdleNodeHandler;
 import aic12.project3.service.nodeManagement.Node;
 import aic12.project3.service.util.LoggerLevel;
 
 /**
  * Time based implementation of Load Balancer
  * Starts and Stops node according to using as much resources as possible
  * to complete requests as quick as possible
  * 1. Get amount of Tweets and store (async)
 2. Calculate Distribution:
 	- How many nodes are busy currently?
 		- How much longer will they be busy? 
 		- Whats in the backlog? (Amount of tweets to be analyzed in queue)? So how much longer will calculation have to run after current request to be released?
 	- How many nodes are idle?
 	- How many nodes are available to start?
 	- How many requests in the queue?
 3. 
 	a. More nodes available than needed:	
 		- distribute sub-requests to node(s) async
 	b. exact amount of nodes available as needed:
 		- distribute sub-requests to node(s) async
 	c. no nodes to start but needs waiting on finishing of other
 		- distribute sub-requests as possible and put others in request queue
 	d. nodes need to be started
 		- distribute sub-requests as possible and put others in request queue
 		- when node is ready continue putting them into request queue
 		- one thread for each node that polls until available
  * 
  * @author johannes
  *
  */
 public class LoadBalancerTime extends LoadBalancer {
 
 	private static LoadBalancerTime instance = new LoadBalancerTime();
 	private Queue<SentimentProcessingRequest> processQueue = new LinkedList<SentimentProcessingRequest>();
 	private int nodesToRunCurrently;
 	final Lock lock = new ReentrantLock();
 	@Autowired IHighLevelNodeManager highLvlNodeMan;
 	private String clazzName = "LoadBalancer";
 	private IBalancingAlgorithm balancingAlgorithm;
 	private Logger log = Logger.getLogger(LoadBalancerTime.class);
 	private int nodeIdleTimeout;
 	@Autowired private BalancingAlgorithmFactory balancingFactory;
 
 	private LoadBalancerTime(){
 	}
 
 	/**
 	 * Return the singleton LoadBalancer
 	 * @return
 	 */
 	public static LoadBalancerTime getInstance(){
 			return instance;			
 	}
 
 	/**
 	 * Init implementation
 	 */
 	@Override
 	protected void init(){
 		nodeIdleTimeout = Integer.parseInt(config.getProperty("nodeIdleTimeout"));
 		nodesToRunCurrently = Integer.parseInt(config.getProperty("minimumNodes"));
 
 		rqr.addObserver(this);
 		List<Node> runningNodes = lowLvlNodeMan.listRunningNodes();
 		
 		if (runningNodes != null){
 			// Stop all running nodes
 			for (Node node : runningNodes){
 				// Check if any Sentiment Nodes exist
 				if (node.getName().contains(config.getProperty("serverNameSentiment"))){
 					lowLvlNodeMan.stopNode(node.getId());	
 				}
 			}			
 		}
 
 		int minimumRunningNodes = Integer.parseInt(config.getProperty("minimumNodes"));
 		for (int i=0; i < minimumRunningNodes; i++){
 			highLvlNodeMan.startNode().addObserver(this);
 		}
 		
 		balancingAlgorithm = balancingFactory.getAlgorithm("default");
 
 		managementLogger.log(clazzName, LoggerLevel.INFO, "init done");
 	}
 
 	/**
 	 * Handle updates in RequestQueueReady
 	 */
 	@Override
 	protected void updateInQueue(String id) {
 		SentimentRequest request = rqr.getRequest(id);
 
 		if (request != null){
 			managementLogger.log(clazzName, LoggerLevel.INFO, "QueueUpdate: " + id + " is " + request.getState().toString());
 
 			switch (request.getState()){
 			case READY_TO_PROCESS:
 				managementLogger.log(clazzName, LoggerLevel.INFO, "Time to split");
 				int parts = balancingAlgorithm.calculatePartsCountForRequest(request);
 				RequestSplitter.splitRequest(request, parts);
 				break;
 
 			case SPLIT:
 				managementLogger.log(clazzName, LoggerLevel.INFO, "request was split");
 				// fill processQueue
 				processQueue.addAll(request.getSubRequestsNotProcessed());
 
 				// update needed nodes
 				int desiredNodeCount = balancingAlgorithm.calculateNodeCountOnNewRequest();
 				highLvlNodeMan.runDesiredNumberOfNodes(desiredNodeCount, this);
 				
 				// distribute work to already running nodes
 				synchronized (highLvlNodeMan) {
 					Node nodeForWork = highLvlNodeMan.getMostAvailableNode();
 					while(nodeForWork != null) {
 						startWorkOrStartIdleHandling_internal(nodeForWork);
 						nodeForWork = highLvlNodeMan.getMostAvailableNode();
 					}
 				}
 				break;
 			
 			default:
 				break;
 			}
 		}
 	}
 	
 	/**
 	 * Send request to node if one is available
 	 * @return true if work was sent, false if node still stays idle
 	 */
 	private boolean pollAndSend(Node n) {
 		managementLogger.log(clazzName, LoggerLevel.INFO, "pollAndSend; Node: " + n.getIp() + " Size of processQueue: " + processQueue.size());
 
 		// get work
 		SentimentProcessingRequest req = processQueue.poll();
 		if (req != null){ // polling was successful, new work available
 			synchronized(highLvlNodeMan){
 				highLvlNodeMan.sendRequestToNode(n, req);
 				return true;
 			}
 		} else {
 			return false;
 		}
 	}
 
 	/**
 	 * Accepts the processed requests and calls combiner
 	 */
 	@Override
 	public void acceptProcessingRequest(SentimentProcessingRequest req) {
 
 		log.debug("SentimentProcessingRequest with ID " + req.getId() + " received");
 		SentimentRequest parent = rqr.getRequest(req.getParentID());
 		parent.getSubRequestsNotProcessed().remove(req);
 		parent.getSubRequestsProcessed().add(req);
 		managementLogger.log(clazzName, LoggerLevel.INFO, "SentimentProcessingRequest received; SubRequests processed: " + parent.getSubRequestsProcessed().size() + " not processed: " + parent.getSubRequestsNotProcessed().size());
 
 		if(RequestSplitter.areAllPartsProcessed(parent)) {
 			managementLogger.log(clazzName, LoggerLevel.INFO, "Combination of parts started for " + parent.getCompanyName());
 			RequestSplitter.combineParts(parent);
 			// TODO notify GUI? set status of SentimentRequest?
 		}
 		managementLogger.log(clazzName, LoggerLevel.INFO, "Change node status to idle");
 		highLvlNodeMan.setNodeIdle(req);
 	}
 
 	@Override
 	protected void updateInNode(Node node) {
 		if(node.getStatus() == NODE_STATUS.IDLE) {
 			// TODO decide if we should stop this node or give it new load
 			
 			startWorkOrStartIdleHandling_internal(node);
 		}
 	}
 
 	private void startWorkOrStartIdleHandling_internal(Node node) {
 		// look for new work
 		if(pollAndSend(node)) {
 			log.info("sending new work to node " + node.getId());
 		} else {
 			IdleNodeHandler.startIdleNodeHandling(node, nodeIdleTimeout, highLvlNodeMan);
 		}
 	}
 
 	@Override
 	public IBalancingAlgorithm getBalancingAlgorithm() {
 		return balancingAlgorithm;
 	}
 
 	@Override
 	public void setBalancingAlgorithm(IBalancingAlgorithm alg) {
 		this.balancingAlgorithm.stopUsage();
 		this.balancingAlgorithm = alg;
 		this.balancingAlgorithm.startUsage();
 	}
 }
