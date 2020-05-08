 package tracker.core;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import tracker.api.TrackerAPI;
 import core.protocols.p2p.Node;
 import core.protocols.transport.IRequestHandler;
 import core.protocols.transport.ITransport;
 import core.protocols.transport.http.HttpImpl;
 import core.protocols.transport.socket.request.RequestHandler;
 import core.protocols.transport.socket.server.SocketImpl;
 
 /**
  * Implementation of the tracker
  * 
  * @author laurent.vanni@sophia.inria.fr - logNet team 2010 - INRIA
  *         Sophia-Antipolis - France
  * 
  */
 public class Tracker implements TrackerAPI, IRequestHandler {
 
 	/** peerSet<networkID, List<Node>> */
 	private Map<String, List<Node>> peerSet;
 	/** Transport layer */
 	private ITransport socketTransport;
 	private ITransport httpTransport;
 	/** The list of the available invitation from a network to an other */
 	private List<Invitation> invitations;
 	/** The tracker singleton */
	private static Tracker tracker = new Tracker();
 	/** The status of the tracker */
 	public static boolean started = false;
 
 	/**
 	 * Default constructor
 	 */
 	private Tracker() {
 		// TRANSPORT LAYER
 		// socket
 		socketTransport = new SocketImpl(TRACKER_PORT, 10, RequestHandler.class
 				.getName(), 10, 1, 50, this);
 		((SocketImpl) socketTransport).launchServer();
 		// http
 		httpTransport = new HttpImpl();
 
 		// TRACKER INIT
 		peerSet = new HashMap<String, List<Node>>();
 		invitations = new ArrayList<Invitation>();
 	}
 
 	/**
 	 * Get the tracker instance
 	 * 
 	 * @return Tracker, the singleton instance
 	 */
 	public static Tracker getTracker() {
 		started = true;
 		if (tracker != null) {
 			return tracker;
 		} else {
 			return tracker = new Tracker();
 		}
 	}
 
 	/**
 	 * Allow to add a node to the peerSet
 	 * 
 	 * @param networkID
 	 * @param node
 	 */
 	public void putNode(String networkID, Node node) {
 		List<Node> nodes = null;
 		if ((nodes = peerSet.get(networkID)) != null) {
 			nodes.add(node);
 		} else {
 			nodes = new ArrayList<Node>();
 			nodes.add(node);
 			peerSet.put(networkID, nodes);
 		}
 	}
 
 	/**
 	 * Return the an entry point of an Network
 	 * 
 	 * @param networkID
 	 * @return Node.toString()
 	 */
 	public String getJoinEntry(String networkID) {
 		List<Node> nodes = peerSet.get(networkID);
 		return nodes != null && nodes.size() != 0 ? nodes.get(0).toString()
 				: "null";
 	}
 
 	/**
 	 * Remove the node from the tracker peerSet
 	 * 
 	 * @param networkID
 	 * @param node
 	 */
 	public void removeNode(String networkID, String node) {
 		List<Node> nodes = peerSet.get(networkID);
 		nodes.remove(node);
 		if (nodes.size() == 0) {
 			peerSet.remove(networkID);
 		}
 	}
 
 	/**
 	 * Add an invitation for a network
 	 * 
 	 * @param networkID
 	 * @param accessPass
 	 */
 	public synchronized void addInvitation(String networkID, String accessPass) {
 		invitations.add(new Invitation(networkID, accessPass));
 	}
 
 	/**
 	 * Remove an invitation for a network
 	 * 
 	 * @param networkID
 	 * @param accessPass
 	 */
 	public synchronized void removeInvitation(String networkID,
 			String accessPass) {
 		Invitation i = new Invitation(networkID, accessPass);
 		invitations.remove(i);
 	}
 
 	/**
 	 * @param code
 	 *            , the request code to handle
 	 */
 	public String handleRequest(String code) {
 		String[] args = code.split(",");
 		String result = "";
 		int f = Integer.parseInt(args[0]);
 		switch (f) {
 		case ADDNODE:
 			putNode(args[1], new Node(args[2], Integer.parseInt(args[3]),
 					Integer.parseInt(args[4])));
 			break;
 		case GETCONNECTION:
 			result = getJoinEntry(args[1]);
 			break;
 		case REMOVENODE:
 			Node n = new Node(args[2], Integer.parseInt(args[3]), Integer
 					.parseInt(args[4]));
 			peerSet.get(args[1]).remove(n);
 			break;
 		case JOIN:
 			result = "null";
 			Invitation toRemove = null;
 			for (Invitation i : invitations) {
 				if (i.getAccessPass().equals(args[1])) {
 					result = i.getNetworkID() + ","
 							+ getJoinEntry(i.getNetworkID());
 					toRemove = i;
 					break;
 				}
 			}
 			if (toRemove != null)
 				invitations.remove(toRemove);
 			break;
 		default:
 			break;
 		}
 		return result;
 	}
 
 	/**
 	 * 
 	 */
 	public void kill() {
 		started = false;
 		peerSet.clear();
 		invitations.clear();
 		socketTransport.stopServer();
 		tracker = null;
 	}
 
 	/**
 	 * @return List<Invitation>, the list of the invitations known
 	 */
 	public List<Invitation> getInvitations() {
 		return invitations;
 	}
 
 	/**
 	 * @return String, The port number used by the tracker to listen
 	 */
 	public String getPort() {
 		int port = socketTransport.getPort();
 		return port == 0 ? "8080" : port + "";
 	}
 
 	/**
 	 * @return Map<String, List<Node>>, The peerSet managed by the tracker
 	 */
 	public Map<String, List<Node>> getPeerSet() {
 		return peerSet;
 	}
 
 	/**
 	 * @return int, The number of registredPeer
 	 */
 	public int getPeerNumber() {
 		int cpt = 0;
 		for (String key : tracker.getPeerSet().keySet()) {
 			if (!key.equals("synapse")) {
 				cpt += peerSet.get(key).size();
 			}
 		}
 		return cpt;
 	}
 }
