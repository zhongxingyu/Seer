 package edu.uc.cs.distsys.idetect;
 
 import java.net.UnknownHostException;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Random;
 import java.util.concurrent.Executors;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.ScheduledThreadPoolExecutor;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.locks.Lock;
 import java.util.concurrent.locks.ReentrantLock;
 
 import edu.uc.cs.distsys.LogHelper;
 import edu.uc.cs.distsys.Node;
 import edu.uc.cs.distsys.NodeState;
 import edu.uc.cs.distsys.comms.MessageListener;
 import edu.uc.cs.distsys.comms.NotifyThread;
 import edu.uc.cs.distsys.ilead.ElectionTracker;
 import edu.uc.cs.distsys.ilead.LeaderChangeListener;
 import edu.uc.cs.distsys.ilead.LeaderMain;
 import edu.uc.cs.distsys.ui.NodeStatusViewThread;
 
 public class DetectMain implements MessageListener<Heartbeat>, LeaderChangeListener, FailureListener {
 
 	private static final long HB_INIT_DELAY		    = 0;
 	private static final long FAIL_DETECT_PERIOD_MS = 1000;
 	public static final long  HB_PERIOD_MS			= 750;
 
 	private final ScheduledExecutorService scheduledExecutor;
 
 	private Lock heartbeatLock;
 	private HashMap<Integer, Node> nodes;
 	private List<Node> failedNodes;
 	private Thread detectorThread;
 	private LogHelper logger;
 	private NodeStatusViewThread statusViewThread;
 	private ElectionTracker tracker;
 	private Node myNode;
 	private HeartbeatThread hbThread;
 
 	public DetectMain(int nodeId, List<Integer> peers) {
 		this.logger = new LogHelper(nodeId, System.out, System.err, null);
 		this.nodes = new HashMap<Integer, Node>();
 		this.failedNodes = new LinkedList<Node>();
 		this.heartbeatLock = new ReentrantLock();
 		this.scheduledExecutor = new ScheduledThreadPoolExecutor(1);	//TODO
 		this.myNode = new Node(nodeId);
 		this.statusViewThread = new NodeStatusViewThread(this.myNode.getId());
		new Thread(statusViewThread).start();
 		
 		for (int peer : peers) {
 			this.nodes.put(peer, new Node(peer));
 		}
 	}
 
 	public void start() throws UnknownHostException {
		HeartbeatThread hbThread = new HeartbeatThread(this.myNode.getId(), failedNodes, heartbeatLock, logger); 
 		this.detectorThread = Executors.defaultThreadFactory().newThread(
 				new NotifyThread<Heartbeat>(this.myNode.getId(), hbThread.getCommsWrapper(), this, logger));
 		this.detectorThread.start();
 		this.scheduledExecutor.scheduleAtFixedRate(hbThread, HB_INIT_DELAY, HB_PERIOD_MS, TimeUnit.MILLISECONDS);
 		FailureDetectionThread fdt = new FailureDetectionThread(nodes, heartbeatLock, this);
 		this.scheduledExecutor.scheduleAtFixedRate(fdt, HB_INIT_DELAY, FAIL_DETECT_PERIOD_MS, TimeUnit.MILLISECONDS);
 		
 		List<LeaderChangeListener> listeners = new LinkedList<LeaderChangeListener>();
 		listeners.add(this);
 		listeners.add(hbThread);
 		this.tracker = new LeaderMain(this.myNode.getId(), listeners, logger);
 		this.tracker.start();
 		this.tracker.startNewElection();
 	}
 	
 	public void stop() {
		this.statusViewThread.getViewFrame().dispose();
		hbThread.getCommsWrapper().close();
 		this.scheduledExecutor.shutdownNow();
 		try {
 			Thread.sleep(HB_PERIOD_MS);
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 		this.detectorThread.interrupt();
 		this.tracker.stop();
 
 		this.logger.log("Detector shutting down");
 	}
 
 	@Override
 	public void notifyMessage(Heartbeat status) {
 		try {
 			this.heartbeatLock.lock();
 			if (!nodes.containsKey(status.getNodeId())) {
 				logger.log("Discovered new node - " + status.getNodeId());
 				Node n = new Node(status);
 				this.nodes.put(status.getNodeId(), n);
 				this.statusViewThread.addMonitoredNode(n);
 			} else {
 				logger.debug("Received heartbeat from node " + status.getNodeId());
 				if (this.nodes.get(status.getNodeId()).updateStatus(status)) {
 					// Go through all reported failed nodes and update local state if necessary
 					for (Node failNode : status.getFailedNodes()) {
 						this.verifyFailedNode(failNode);
 					}
 				} else {
 					logger.error("Warning - Received out-of-order heartbeat from node " + status.getNodeId());
 				}
 			}
 			this.statusViewThread.updateUI();
 		} finally {
 			this.heartbeatLock.unlock();
 		}
 	}
 	
 	@Override
 	public void onFailedNode(Node failed) {
 		try {
 			this.heartbeatLock.lock();
 			this.failedNodes.add(failed);
 		} finally {
 			this.heartbeatLock.unlock();
 		}
 		if (failed.getId() == myNode.getLeaderId())
 			this.tracker.onLeaderFailed();
 		this.statusViewThread.updateUI();
 	}
 	
 	@Override
 	public void onNewLeader(int leaderId) {
 		this.logger.log("New Leader: " + leaderId);
 		if (this.myNode.getId() == leaderId) {
 			// update our UI to say we're the current user
 			this.statusViewThread.setUIMessage("Currently The Leader");
 		} else {
 			this.statusViewThread.setUIMessage("Leader = " + leaderId);
 		}
 		// update who we believe the leader is
 		this.myNode.setLeaderId(leaderId);
 	}
 	
 	@Override
 	public void onLeaderFailed() {
 		this.logger.log("Leader " + myNode.getLeaderId() + " has failed!");
 		myNode.setLeaderId(0);
 	}
 	
 	public int getLeaderId() {
 		return this.myNode.getLeaderId();
 	}
 	
 	public int getGroupId() {
 		return this.myNode.getGroupId();
 	}
 	
 	public int getId() {
 		return this.myNode.getId();
 	}
 	
 	/***
 	 * @return Number of known group members including self
 	 */
 	public int getNumGroupNodes() {
 		return this.nodes.size() + 1;
 	}
 	
 	private void verifyFailedNode(Node node) {
 		if (node.getId() == this.myNode.getId())
 			return;
 		try {
 			this.heartbeatLock.lock();
 			if (!nodes.containsKey(node.getId())) {
 				logger.log("Discovered new node (offline) - " + node.getId());
 				this.nodes.put(node.getId(), Node.createFailedNode(node.getId(), node.getState(), node.getSeqHighWaterMark()));
 			} else {
 				Node localNode = nodes.get(node.getId());
 				if (! localNode.isOffline() && localNode.getSeqHighWaterMark() <= node.getSeqHighWaterMark()) {
 					//update our node
 					localNode.updateState(node);
 					// If it was the leader, we need to elect a new one
 					if (localNode.getId() == myNode.getLeaderId()) {
 						this.tracker.onLeaderFailed();
 					}
 				} else if (! localNode.isOffline() && ! node.getState().equals(NodeState.SUSPECT)) {
 					//discard out-of-date info
 					//DEBUG
 					logger.debug("Reported failed node is actually online (id=" + node.getId() + ")");
 				}
 			}
 			this.statusViewThread.updateUI();
 		} finally {
 			this.heartbeatLock.unlock();
 		}
 	}
 	
 	public static void main(String[] args) {
 		int node = 0;
 		int group = 0;
 		if (args.length < 1) {
 			//System.err.println("Usage: " + args[0] + "<port#> [peer#1] ... [peer#N]");
 			
 			// DEBUGGING
 			//port = new Random(System.currentTimeMillis()).nextInt(1000) + 1024;
 			node = new Random(System.currentTimeMillis()).nextInt(1000);
 		} else {
 			if (args.length >= 1) {
 				// first arg is node id
 				node = Integer.parseInt(args[0]);
 			}
 			if (args.length >= 2) {
 				// second arg is group id
 				group = Integer.parseInt(args[1]);
 			}
 		}
 		
 		List<Integer> peers = new LinkedList<Integer>();
 		for (int i = 1; i < args.length; i++) {
 			peers.add(Integer.parseInt(args[i]));
 		}
 				
 		try {
 			final DetectMain detector = new DetectMain(node, peers);
 			Runtime.getRuntime().addShutdownHook(new Thread() {
 				@Override
 				public void run() {
 					detector.stop();
 				}
 			});
 			detector.start();
 		} catch (UnknownHostException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 }
