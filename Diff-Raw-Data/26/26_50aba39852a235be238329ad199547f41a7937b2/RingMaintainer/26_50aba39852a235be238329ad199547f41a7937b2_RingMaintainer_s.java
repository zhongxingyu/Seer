 package decentchat.internal;
 
 import java.rmi.RemoteException;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.apache.log4j.Logger;
 
 import decentchat.internal.remotes.Node;
 import decentchat.internal.remotes.NodeImpl;
 
 public class RingMaintainer extends Thread {
 	
 	private static final int SLEEP_TIME = 1000;
 
 	static Logger logger = Logger.getLogger(RingMaintainer.class);
 	
 	private NodeImpl node;
 	private int last_fixed_finger;
 	private boolean stillRunning;
 
 	public static int MAX_SUCCESSOR_COUNT = 10;
 	public static int MAX_FINGER_COUNT = 31;
 	
 	public void abort() {
 		stillRunning = false;
 	}
 	
 	public RingMaintainer(NodeImpl node) {
 		this.node = node;
 		last_fixed_finger = -1;
 		stillRunning = true;
 	}
 
 	public void stabilize() {
 		NodeKey hash = node.getKey().inc(1);
 	    Node x = null;
 	    Node succ = null;
 	    NodeKey succKey = null;
	    NodeKey xKey = null;
 		try {
 			succ = node.getSuccessor();
 			succKey = succ.getKey();
 			x = succ.getPredecessor();
 		} catch (RemoteException e) {
 			switchToNextSuccessor(node);
 			return;
 		}
 		try {
			xKey = x.getKey();
 		} catch (RemoteException e) {
 			if (succ.equals(node)) {
 				node.setPredecessor(null);
 			}
 		}
	    if (x != null &&
	    		// we're either pointing to ourselves
	    		((succKey.compareTo(node.getKey()) == 0 && xKey.compareTo(node.getKey()) != 0)
	    		// or there is a node between us and node.getSuccessor()
	            || (xKey.isWithin(hash, succKey)))) {
	        node.setSuccessor(x);
	        logger.debug("New successor is " + x);
	        node.getSuccessors().add(0, x);
	    }
 		try {
 		    node.getSuccessor().notify(node);
 		} catch (RemoteException e) {
 			logger.debug("Stabilize did not work. Waiting for next run.");
 		}
 	}
 	
 	public void fixFingers() {
 	    try {
 			last_fixed_finger += 1;
 		    if (last_fixed_finger >= RingMaintainer.MAX_FINGER_COUNT) {
 		        last_fixed_finger = 0;
 		    }
 		    NodeKey hash = node.getKey().inc((long) Math.pow(2, last_fixed_finger));
 		    if (node.getFingers().size() <= last_fixed_finger) {
 		    	node.getFingers().add(node.findSuccessor(hash));
 		    } else {
 		    	node.getFingers().set(last_fixed_finger, node.findSuccessor(hash));
 		    }
 		} catch (RemoteException e) {
 			logger.debug("Fix fingers failed, waiting for next run");
 		}
 	}
 	
 	public void fixSuccessors() {
 	    List<Node> succs = new LinkedList<Node>();
 		try {
 		    succs.add(node.getSuccessor());
 			for (Node n: node.getSuccessor().getSuccessors()) {
 				succs.add(n);
 			}
 		    while (succs.size() > RingMaintainer.MAX_SUCCESSOR_COUNT) {
 		    	succs.remove(succs.size()-1);
 		    }
 		    node.setSuccessors(succs);
 		} catch (RemoteException e) {
 			switchToNextSuccessor(node);
 		}
 	}
 
 	@Override
 	public void run() {
 		logger.debug("maintenance loop started");
 		while (true) {
 			stabilize();
 			if (!stillRunning) return;
 			fixSuccessors();
 			if (!stillRunning) return;
 			fixFingers();
 			if (!stillRunning) return;
 			try {
 				Thread.sleep(SLEEP_TIME);
 			} catch (InterruptedException e) {
 			}
 			if (!stillRunning) return;
 		}
 	}
 	
 	private void switchToNextSuccessor(NodeImpl node) {
 		List<Node> nodes = node.getSuccessors();
 		if(nodes.size() > 1) {
 			logger.debug("Switched to next successor, old one was broken.");
 			node.setSuccessor(node.getSuccessors().get(1));
 			node.removeSuccessorFromList(nodes.get(0));
 		} else {
 			logger.debug("Using myself as successor, old one was broken.");
 			node.setSuccessor(node);
 			nodes.clear();
 			node.setSuccessors(nodes);
 		}
 	}
 }
