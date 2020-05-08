 package edu.uc.cs.distsys.idetect;
 
 import java.io.IOException;
 import java.net.UnknownHostException;
 import java.util.List;
 import java.util.concurrent.locks.Lock;
 
 import edu.uc.cs.distsys.Logger;
 import edu.uc.cs.distsys.Node;
 import edu.uc.cs.distsys.comms.CommsWrapper;
 import edu.uc.cs.distsys.comms.MulticastWrapper;
 
 final class HeartbeatThread implements Runnable {
 	
 	private static final int HEARTBEAT_PORT = 5000;
 	
 	private final int nodeId;
 	
 	private Logger logger;
 	private Lock nodeLock;
 	private List<Node> failedNodes;
 	private CommsWrapper<Heartbeat> heartbeatSender;
 	
 	private int nextSeqNum;
 	
 	public HeartbeatThread(int nodeId, List<Node> failedNodes, Lock nodeLock, Logger logger) throws UnknownHostException {
 		this.nodeId = nodeId;
 		this.failedNodes = failedNodes;
 		this.nodeLock = nodeLock;
 		this.logger = logger;
		this.heartbeatSender = new MulticastWrapper<Heartbeat>(HEARTBEAT_PORT, nodeId, new Heartbeat.HeartbeatFactory(), logger);
 		this.nextSeqNum = 0;
 	}
 	
 	@Override
 	public void run() {
 		long curTime = System.currentTimeMillis();
 		try {
 			nodeLock.lock();
 			if (failedNodes.size() > 0) {
 				String msg = "Sending notification of " + failedNodes.size() + " failed nodes: {";
 				for (Node n : failedNodes)
 					msg += n.getId() + ", ";
 				msg += "}";
 				logger.log(msg);
 			} else {
 				//DEBUG:
 				logger.debug("Sending heartbeat with " + 
 						failedNodes.size() + " failed nodes");
 				//DEBUG
 			}
 			heartbeatSender.send(new Heartbeat(nodeId, nextSeqNum++, curTime, failedNodes));
 			failedNodes.clear();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			logger.error("ERROR: " + e);
 			//e.printStackTrace();
 		} finally {
 			nodeLock.unlock();
 		}
 	}
 	
 	public CommsWrapper<Heartbeat> getCommsWrapper() {
 		return heartbeatSender;
 	}
 }
