 package myhomeaudio.server.manager;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Iterator;
 
 import org.apache.commons.codec.digest.DigestUtils;
 import org.json.simple.JSONArray;
 
 import myhomeaudio.server.client.Client;
 import myhomeaudio.server.database.Database;
 import myhomeaudio.server.database.object.DatabaseNode;
 import myhomeaudio.server.http.NodeWorker;
 import myhomeaudio.server.http.StatusCode;
 import myhomeaudio.server.node.Node;
 import myhomeaudio.server.node.NodeCommands;
 
 /**
  * Maintains a list of nodes and allows the rest of the server to send commands
  * to any of them.
  * 
  * @author cameron
  * 
  */
 public class NodeManager implements NodeCommands, StatusCode {
 
 	private static NodeManager instance = null;
 	
 	private ArrayList<DatabaseNode> nodeList;
 	private Database db;
 
 	protected NodeManager() {
 		System.out.println("*** Starting NodeManager...");
 		this.db = Database.getInstance();
 		this.nodeList = new ArrayList<DatabaseNode>();
 		
 		if (!checkNodesTable() || !updateNodesFromDB()) {
 			System.exit(1);
 		}
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
 	
 	private boolean checkNodesTable() {
 		boolean result = false;
 		
 		// Make sure the table exists, create it if it doesn't
 		this.db.lock();
 		Connection conn = this.db.getConnection();
 		try {
 			Statement statement = conn.createStatement();
 			statement.executeUpdate("CREATE TABLE IF NOT EXISTS "
 					+ "nodes (id INTEGER PRIMARY KEY AUTOINCREMENT, "
 					+ "name TEXT, ipaddress TEXT, bluetoothAddress TEXT);");
 			result = true;
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		this.db.unlock();
 		
 		return result;
 	}
 	
 	private boolean updateNodesFromDB() {
 		boolean result = true;
 		
 		this.db.lock();
 		Connection conn = this.db.getConnection();
 		try {
 			Statement statement = conn.createStatement();
 			
 			// Create each DatabaseNode object using rows from the nodes table
 			ResultSet nodeResults = statement.executeQuery("SELECT * FROM nodes;");
 			while (nodeResults.next()) {
 				DatabaseNode dbNode = new DatabaseNode(
 						nodeResults.getInt("id"),
 						nodeResults.getString("name"),
 						nodeResults.getString("ipaddress"),
						nodeResults.getString("bluetoothaddress"));
 				// Populate the nodeList
 				this.nodeList.add(dbNode);
 			}
 			result = true;
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		this.db.unlock();
 		
 		return result;
 	}
 	
 	/**
 	 * Add Node, places nodes within network into an arrayList
 	 * 
 	 * @param node
 	 *            Node to be added to the arrayList
 	 * @return True - Item added to list, False - Item already in list
 	 */
 	public int addNode(Node node) {
 		int result = STATUS_FAILED;
 		
 		// TODO: Check for duplicates
 		
 		int newId = -1;
 		
 		// Add new node to the database
 		this.db.lock();
 		Connection conn = this.db.getConnection();
 		try {
			PreparedStatement pstatement = conn.prepareStatement("INSERT INTO nodes (name, ipaddress, bluetoothaddress) VALUES (?, ?, ?);");
 			pstatement.setString(1, node.getName());
 			pstatement.setString(2, node.getIpAddress());
 			pstatement.setString(3, node.getBluetoothAddress());
 			pstatement.executeUpdate();
 			
 			// We want the id of the new node, so get it back
 			PreparedStatement statement = conn.prepareStatement("SELECT id FROM nodes " + 
					"WHERE name = ? AND ipaddress = ? AND bluetoothaddress = ?;");
 			ResultSet resultSet = statement.executeQuery();
 			newId = resultSet.getInt("id");
 			
 			// Add the new node to the nodeList with their id
 			this.nodeList.add(new DatabaseNode(newId, node));
 			
 			result = STATUS_OK;		
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		this.db.unlock();
 		
 		return result;
 	}
 	
 	public int removeNode(Node node) {
 		int result = STATUS_FAILED;
 		
 		if (node != null) {
 			DatabaseNode dbNode = getMatchingNode(node);
 			if (dbNode != null) {
 				this.db.lock();
 				Connection conn = this.db.getConnection();
 				try {
 					PreparedStatement pstatement = conn.prepareStatement("DELETE FROM nodes "
 							+ "WHERE id = ?");
 					pstatement.setInt(1, dbNode.getId());
 					pstatement.executeUpdate();
 					
 					nodeList.remove(dbNode);
 					
 					result = STATUS_OK;
 				} catch (SQLException e) {
 					e.printStackTrace();
 				}
 				this.db.unlock();
 			}
 		}
 		return result;
 	}
 	
 	/**
 	 * Gets the DatabaseNode object that resides in the nodeList which
 	 * corresponds with the Node object being given.
 	 * 
 	 * @param node
 	 * @return The corresponding DatabaseNode, or null if not found.
 	 */
 	private DatabaseNode getMatchingNode(Node node) {
 		for (Iterator<DatabaseNode> i  = this.nodeList.iterator(); i.hasNext();) {
 			DatabaseNode nextNode = i.next();
 			if (nextNode.equals(node)) {
 				return nextNode;
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Sends commands from the server to the node
 	 * 
 	 * @param command
 	 *            The command the server wants to send to the node
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
 
 	/*
 	/**
 	 * Returns the current number of nodes under NodeManager management
 	 * 
 	 * @return nodeCount Number of nodes
 	 * 
 	public synchronized int getNodeCount() {
 		return nodeCount;
 	}
 	*/
 
 	/**
 	 * Verifies that a node with the given name exists within the node manager
 	 * 
 	 * @param bluetoothName
 	 *            Name of the node
 	 * @return True if the node exists
 	 */
 	public boolean isValidNode(String bluetoothName) {
 		for (DatabaseNode node : this.nodeList) {
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
 	public DatabaseNode getNodeByIpAddress(String ipAddress) {
 		for (DatabaseNode nextNode : nodeList) {
 			if (nextNode.getIpAddress().equals(ipAddress)) {
 				return new DatabaseNode(nextNode);
 			}
 		}
 		return null;
 	}
 	
 	/**
 	 * Get a Node object with the given node name
 	 * 
 	 * @param name
 	 *            The name of the node to be searched for.
 	 * @return The node with the matching name. Returns null if the node is
 	 *         not found.
 	 */
 	public DatabaseNode getNodeByName(String name) {
 		// loops through nodeList looking for node with matching name
 		for (DatabaseNode nextNode : nodeList) {
 			if (nextNode.getName().equals(name)) {
 				return new DatabaseNode(nextNode);
 			}
 		}
 		return null;
 	}
 	
 	
 	/**
 	 * Get a Node object with the given node id
 	 * 
 	 * @param nodeId
 	 *            The id of the node to be searched for.
 	 * @return The node with the matching id. Returns null if the node is not found.
 	 */
 	public DatabaseNode getNodeById(int nodeId) {
 		// loops through nodeList looking for node with matching name
 		for (DatabaseNode nextNode : nodeList) {
 			if (nextNode.getId() == nodeId) {
 				return new DatabaseNode(nextNode);
 			}
 		}
 		return null;
 	}
 	
 	
 	public ArrayList<DatabaseNode> getList() {
 		return new ArrayList<DatabaseNode>(nodeList);
 	}
 	
 	public JSONArray getJSONArray() {
 		JSONArray nodeArray = new JSONArray();
 		for (DatabaseNode n : nodeList) {
 			nodeArray.add(n);
 		}
 		return nodeArray;
 	}
 }
