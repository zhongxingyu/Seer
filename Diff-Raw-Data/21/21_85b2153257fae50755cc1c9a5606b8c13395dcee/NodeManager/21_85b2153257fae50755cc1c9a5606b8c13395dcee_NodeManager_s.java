 package myhomeaudio.server.manager;
 
 import java.util.ArrayList;
 
 import myhomeaudio.server.http.NodeWorker;
 import myhomeaudio.server.node.Node;
 import myhomeaudio.server.node.NodeCommands;
 
 /**
  * Maintains a list of nodes and allows the rest of the server to send commands
  * to any of them.
  * 
  * @author cameron
  * 
  */
 public class NodeManager implements NodeCommands {
 
 	private static NodeManager instance = null;
 	private static int nodeCount = 0;
 	private ArrayList<Node> nodeList = new ArrayList<Node>();
 
 	protected NodeManager() {
 
 	}
 
 	/**
 	 * There is only one instance of NodeManager within program.
 	 * 
 	 * @return instance Creates a new instance of NodeManager if one does not
 	 *         exist or returns if one exists
 	 */
 	public static synchronized NodeManager getInstance() {
 		if (instance == null) {
 			instance = new NodeManager();
 		}
 		return instance;
 	}
 
 	// TODO add removeNode, checkNode to make sure no nodes have suddenly
 	// disconnected
 	/**
 	 * Add Node, places nodes within network into an arrayList
 	 * 
 	 * @param node
 	 *            Node to be added to the arrayList
 	 * @return True - Item added to list, False - Item already in list
 	 */
 	public synchronized boolean addNode(Node node) {
 		if (nodeList.contains(node)) {
 			return false;
 		}
 		nodeList.add(node);
 		nodeCount++;
 		return true;
 	}
 
 	/**
 	 * Sends commands from the server to the node
 	 * 
 	 * @param command
	 *            The command the server wants to send to the node\
 	 * @param ipAddress
 	 *            The IP address of the node to send the command to
 	 * @param data
 	 *            Necessary data the node would need to execute the command
 	 */
 	public synchronized void sendNodeCommand(int command, String ipAddress,
 			String data) {
 		NodeWorker worker = new NodeWorker();
 
 		worker.setRequestData(command, ipAddress, data);
 		worker.start();
 
 	}
 
 	/**
 	 * Returns the current number of nodes under NodeManager management
 	 * 
 	 * @return nodeCount Number of nodes
 	 */
 	public synchronized int getNodeCount() {
 		return nodeCount;
 	}
 
 	/**
 	 * Verifies that a node with the given name exists within the node manager
 	 * 
 	 * @param bluetoothName
 	 *            Name of the node
 	 * @return True if the node exists
 	 */
 	public boolean isValidNode(String bluetoothName) {
 		for (Node node : this.nodeList) {
 			if (node.getName().equals(bluetoothName)) {
 				return true;
 			}
 		}
 		return false;
 
 	}
 
 	/**
 	 * Get a Node object with the given IP address
 	 * 
 	 * @param ipAddress
 	 *            The IP address of the node to return.
 	 * @return The node with the corresponding IP. Returns null if no node with
 	 *         the corresponding IP address is found.
 	 */
 	public Node getNodeByIpAddress(String ipAddress) {
 		for (Node item : nodeList) {
 			if (item.getIpAddress().equals(ipAddress)) {
 				return item;
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Get aNode object with the given node name
 	 * 
 	 * @param name
 	 *            The name of the node to be searched for.
 	 * @return The node with the matching name. Returns null if not node is
 	 *         found.
 	 */
 	public Node getNodeByName(String name) {
 		// loops through nodeList loooking for node with matching name
 		for (Node item : nodeList) {
 			if (item.getName().equals(name)) {
 				return item;
 			}
 		}
 		return null;
 	}
 }
