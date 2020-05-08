 package myhomeaudio.server.database.object;
 
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.Date;
 
 import org.apache.commons.codec.digest.DigestUtils;
 import org.json.simple.JSONObject;
 
 import myhomeaudio.server.client.Client;
 import myhomeaudio.server.locations.layout.NodeSignalBoundary;
 import myhomeaudio.server.node.Node;
 
 public class DatabaseClient extends DatabaseObject<Client> {
 
 	private String sessionId;
 	private Node closestNode;
 
 	private int loggedUserId;
 
 	private ArrayList<NodeSignalBoundary> nodeSignatures;
 
 	public DatabaseClient(int id, Client client) {
 		super(id, new Client(client));
 		this.closestNode = null;
 		this.loggedUserId = -1; // Set logged out
 		this.nodeSignatures = null; // Starts off as null
 	}
 
 	public DatabaseClient(DatabaseClient dbClient) {
 		super(dbClient.getId(), new Client(dbClient.getMacAddress(), dbClient.getIpAddress(),
 				dbClient.getBluetoothName()));
 		this.sessionId = dbClient.getSessionId();
 		this.closestNode = dbClient.getClosestNode();
 		this.loggedUserId = dbClient.getLoggedInUserId();
 		this.nodeSignatures = dbClient.getNodeSignatures();
 	}
 
 	public DatabaseClient(int id, String macAddress, String ipAddress, String bluetoothName,
 			int userId) {
 		super(id, new Client(macAddress, ipAddress, bluetoothName));
 		this.loggedUserId = userId;
 	}
 
 	public String getSessionId() {
 		return this.sessionId;
 	}
 
 	public int getLoggedInUserId() {
 		return loggedUserId;
 	}
 
 	/**
 	 * Logs the DatabaseClient in with a specific user.
 	 * 
 	 * @param userId
 	 *            The id of the user to login
 	 * @return The session id.
 	 */
 	public String login(int userId) {
 		loggedUserId = userId;
 		sessionId = generateSessionId();
 		return sessionId;
 	}
 
 	/**
 	 * Logs the DatabaseClient out.
 	 * 
 	 * @return The user id that was logged out.
 	 */
 	public int logout() {
 		int result = loggedUserId;
 		loggedUserId = -1;
 		sessionId = null;
 		return result;
 	}
 	
 	public boolean isConfigured(){
 		return nodeSignatures != null;
 	}
 
 	public String getIpAddress() {
 		return this.object.getIpAddress();
 	}
 
 	public String getMacAddress() {
 		return this.object.getMacAddress();
 	}
 
 	public String getBluetoothName() {
 		return this.object.getBluetoothName();
 	}
 
 	public Node getClosestNode() {
 		return closestNode;
 	}
 
 	public void updateLocation(Node node) {
 		closestNode = new Node(node);
 	}
 
 	public ArrayList<NodeSignalBoundary> getNodeSignatures() {
 		if(nodeSignatures == null){
 			return null;
 		}
 		return new ArrayList<NodeSignalBoundary>(nodeSignatures);
 	}
 
 	public void setNodeSignatures(ArrayList<NodeSignalBoundary> nodeSignatures) {
 		this.nodeSignatures = new ArrayList<NodeSignalBoundary>(nodeSignatures);
 	}
 
 	/**
 	 * Creates a session id that will be unique to a specific client.
 	 * <p>
 	 * It uses the SHA-512 hash function on certain fields of the Client. It
 	 * also uses the current timestamp.
 	 * 
 	 * @return The unique session id.
 	 */
 	private String generateSessionId() {
 		return DigestUtils.sha512Hex(this.object.getMacAddress() + this.object.getBluetoothName()
 				+ (new Timestamp(new Date().getTime())).toString());
 	}
 
 	public boolean equals(Object obj) {
 		if (obj == null) {
 			return false;
 		} else if (obj instanceof Client) {
 			if (((Client) obj).getBluetoothName().equals(this.object.getBluetoothName())
 					&& ((Client) obj).getMacAddress().equals(this.object.getMacAddress())
 					&& ((Client) obj).getIpAddress().equals(this.object.getIpAddress())) {
 				return true;
 			}
 		} else if (obj instanceof DatabaseClient) {
 			if (((DatabaseClient) obj).getBluetoothName().equals(this.object.getBluetoothName())
 					&& ((DatabaseClient) obj).getMacAddress().equals(this.object.getMacAddress())
 					&& ((DatabaseClient) obj).getIpAddress().equals(this.object.getIpAddress())) {
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	public JSONObject getJSON(){
 		JSONObject object = new JSONObject();
 		object.put("id", loggedUserId);
		object.put("session", sessionId != null ? sessionId.subSequence(0, 7) : null);
 		object.put("closedNode", closestNode != null ? closestNode.getName() : null);
 		object.put("isConfigured",nodeSignatures != null);
 		return object;
 	}
 }
