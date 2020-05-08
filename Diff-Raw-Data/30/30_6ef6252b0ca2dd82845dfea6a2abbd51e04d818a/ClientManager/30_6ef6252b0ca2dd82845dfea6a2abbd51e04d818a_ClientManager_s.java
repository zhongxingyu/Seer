 package myhomeaudio.server.manager;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import myhomeaudio.server.client.Client;
 import myhomeaudio.server.database.Database;
 import myhomeaudio.server.database.object.DatabaseClient;
 import myhomeaudio.server.http.StatusCode;
 import myhomeaudio.server.locations.Triangulation;
 import myhomeaudio.server.locations.layout.DeviceObject;
 import myhomeaudio.server.locations.layout.NodeSignalBoundary;
 import myhomeaudio.server.node.Node;
 
 /**
  * Stores and maintains all of the clients on the server. This object maintains
  * the clients table in the database.
  * 
  * @author Cameron
  * 
  */
 public class ClientManager implements StatusCode {
 
 	private static ClientManager instance = null;
 
 	private ArrayList<DatabaseClient> clientList;
 	private Database db;
 
 	protected ClientManager() {
 		System.out.println("*** Starting ClientManager...");
 		this.db = Database.getInstance();
 		this.clientList = new ArrayList<DatabaseClient>();
 
 		if (!checkClientsTable() || !updateClientsFromDB()) {
 			System.exit(1); // Exit if there's a problem with the database
 		}
 	}
 
 	/**
 	 * Retrieve an instance of the ClientManager.
 	 * 
 	 * @return The ClientManager.
 	 */
 	public static synchronized ClientManager getInstance() {
 		if (instance == null) {
 			instance = new ClientManager();
 		}
 		return instance;
 	}
 
 	private boolean checkClientsTable() {
 		boolean result = false;
 
 		// Make sure table exists, create it if it doesn't
 		this.db.lock();
 		Connection conn = this.db.getConnection();
 		try {
 			Statement statement = conn.createStatement();
 			statement.executeUpdate("CREATE TABLE IF NOT EXISTS "
 					+ "clients (id INTEGER PRIMARY KEY AUTOINCREMENT, "
 					+ "macaddress TEXT UNIQUE, " + "ipaddress TEXT, "
 					+ "bluetoothname TEXT UNIQUE, " + "userid INTEGER);");
 			result = true;
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		this.db.unlock();
 		return result;
 	}
 
 	private boolean updateClientsFromDB() {
 		boolean result = false;
 
 		this.db.lock();
 		Connection conn = this.db.getConnection();
 		try {
 			Statement statement = conn.createStatement();
 			
 			// Create each DatabaseClient object using records for the clients table
 			ResultSet clientResults = statement.executeQuery("SELECT * FROM clients;");
 			while (clientResults.next()) {
 				DatabaseClient dbClient = new DatabaseClient(clientResults.getInt("id"),
 						clientResults.getString("macaddress"),
 						clientResults.getString("ipaddress"),
 						clientResults.getString("bluetoothname"),
 						clientResults.getInt("userid"));
 				this.clientList.add(dbClient);
 			}
 			result = true;
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		this.db.unlock();
 		
 		return result;
 	}
 	
 	/**
 	 * Adds a client to the server.
 	 * 
 	 * @param client
 	 *            The Client object representing the client to add.
 	 * @return Status code
 	 */
 	public int addClient(Client client) {
 		int result = STATUS_FAILED;
 
 		int newId = -1;
 		this.db.lock();
 		Connection conn = this.db.getConnection();
 		try {
 			PreparedStatement pstatement = conn
 					.prepareStatement("INSERT INTO clients (macaddress, ipaddress, bluetoothname) "
 							+ "VALUES (?, ?, ?);");
 			pstatement.setString(1, client.getMacAddress());
 			pstatement.setString(2, client.getIpAddress());
 			pstatement.setString(3, client.getBluetoothName());
 			pstatement.executeUpdate();
 
 			// We want the id of the new client, so get it back
 			PreparedStatement statement = conn.prepareStatement("SELECT id FROM clients "
 					+ "WHERE macaddress = ? AND ipaddress = ? AND bluetoothname = ? LIMIT 1;");
 			statement.setString(1, client.getMacAddress());
 			statement.setString(2, client.getIpAddress());
 			statement.setString(3, client.getBluetoothName());
 			ResultSet resultSet = statement.executeQuery();
 			newId = resultSet.getInt("id");
 
 			// Add the new client to the clientList with their id
 			this.clientList.add(new DatabaseClient(newId, client));
 
 			result = STATUS_OK;
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		this.db.unlock();
 
 		return result;
 	}
 
 	/**
 	 * Removes a client from the server.
 	 * 
 	 * @param sessionId
 	 *            Corresponding session id for the client to remove.
 	 * @return Whether the removal succeeded.
 	 */
 	public int removeClient(int clientId) {
 		int result = STATUS_FAILED;
 
 		DatabaseClient dbClient = getClient(clientId);
 
 		if (dbClient != null) {
 			this.db.lock();
 			Connection conn = this.db.getConnection();
 			try {
 				// Remove the client from the database
 				PreparedStatement pstatement = conn.prepareStatement("DELETE FROM clients "
 						+ "WHERE id = ?");
 				pstatement.setInt(1, clientId);
 				pstatement.executeUpdate();
 				
 				// Remove the client from the user list
 				clientList.remove(dbClient);
 				
 				result = STATUS_OK;
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 			this.db.unlock();
 		}
 
 		return result;
 	}
 
 	/**
 	 * Gets the DatabaseClient object associated with the given client id.
 	 * 
 	 * @param id
 	 * @return Returns a DatabaseClient object, or null if the id doesn't match
 	 *         any existing client.
 	 */
 	private synchronized DatabaseClient getClient(int id) {
 		for (Iterator<DatabaseClient> i = this.clientList.iterator(); i.hasNext();) {
 			DatabaseClient nextClient = i.next();
 			if (nextClient.getId() == id) {
 				return nextClient;
 			}
 		}
 		return null;
 	}
 
 	private synchronized DatabaseClient getClientBySession(String sessionId) {
		for (Iterator<DatabaseClient> i = this.clientList.iterator(); i.hasNext();) {
			DatabaseClient nextClient = i.next();
			if (nextClient.getSessionId().equals(sessionId)) {
				return nextClient;
 			}
 		}
 		return null;
 	}
 	
 
 	/**
 	 * Gets the DatabaseClient object associated with the given session id.
 	 * 
 	 * @param sessionId
 	 *            Session id for a client assigned by the ClientManager.
 	 * @return Returns a DatabaseClient object, or null if the session id
 	 *         doesn't match any existing client.
 	 */
 	public synchronized DatabaseClient getClient(String sessionId) {
 		if (sessionId != null) {
 			DatabaseClient databaseClient = getClientBySession(sessionId);
 			if (databaseClient != null) {
 				return new DatabaseClient(databaseClient);
 			}
 		}
 		return null;
 	}
 	public DatabaseClient getClient(Client client) {
 		for (Iterator<DatabaseClient> i = this.clientList.iterator(); i.hasNext();) {
 			DatabaseClient nextClient = i.next();
 			if (nextClient.equals(client)) {
 				return nextClient;
 			}
 		}
 		return null;
 	}
 
 	public boolean isValidClient(String sessionId) {
 		if (getClient(sessionId) != null)
 			return true;
 		return false;
 	}
 	
 	/**
 	 * Logs a user into a client.
 	 * 
 	 * @param clientId The id for the client to log into.
 	 * @param userId The id for the user logging in.
 	 * @return The session id for the client, or null if the clientId was invalid.
 	 */
 	public String loginClient(int clientId, int userId) {
 		DatabaseClient dbClient = getClient(clientId);
 		
 		if (dbClient != null) {
 			String sessionId = dbClient.login(userId);
 			updateClientToDB(dbClient);
 			return sessionId;
 		} else {
 			return null;
 		}
 		
 	}
 	
 	/**
 	 * Logs a user into a client.
 	 * 
 	 * @param client The Client to log into.
 	 * @param userId The id for the user logging in.
 	 * @return The session id for the client, or null if the client was invalid.
 	 */
 	public String loginClient(Client client, int userId) {
 		if (client != null) {
 			DatabaseClient dbClient = getClient(client);
 			
 			if (dbClient == null) {
 				// Client doesn't exist yet, go ahead and add it
 				if (addClient(client) == STATUS_OK) {
 					dbClient = getClient(client);
 				} else {
 					return null;
 				}
 			}
 			return loginClient(dbClient.getId(), userId);
 		}
 		return null;
 	}
 	
 	/**
 	 * Logs out a session for a client
 	 * 
 	 * @param sessionId The id for the session to end.
 	 * @return The id for the user that was logged out, or -1 if the sessionId was invalid.
 	 */
 	public int logoutClient(String sessionId) {
 		DatabaseClient dbClient = getClient(sessionId);
 		if (dbClient != null) {
 			int userId = dbClient.logout();
 			updateClientToDB(dbClient);
 			return userId;
 		} else {
 			return -1;
 		}
 	}
 	
 	/**
 	 * Changes the client initialization configuration, which includes the node
 	 * signatures for each node.
 	 * 
 	 * @param sessionId
 	 *            The session given to the client we want to set the
 	 *            initialization for.
 	 * @param nodeSignatures
 	 *            The list of NodeSignalBoundaries, which describe what ranges
 	 *            are seen around the area for each node.
 	 * @return Whether the operation completed successfully.
 	 */
 	public boolean changeClientInitialization(String sessionId,
 			ArrayList<NodeSignalBoundary> nodeSignatures) {
 		DatabaseClient databaseClient = getClientBySession(sessionId);
 		databaseClient.setNodeSignatures(nodeSignatures);
 
 		return updateClientToDB(databaseClient);
 	}
 
 	/**
 	 * Updates the location of the client.
 	 * 
 	 * @param sessionId
 	 *            The session given to the client we want to set the location
 	 *            for.
 	 * @param devices
 	 *            A DeviceObject list that provides the id and signal strength
 	 *            for any nodes seen.
 	 * @return Whether the operation completed successfully.
 	 */
 	public synchronized boolean updateClientLocation(String sessionId,
 			ArrayList<DeviceObject> devices) {
 		DatabaseClient databaseClient = getClientBySession(sessionId);
 
 		Node node = Triangulation.findLocation(databaseClient.getNodeSignatures(), devices);
 		if(node != null && databaseClient.getClosestNode() != null){
 			if(!node.equals(databaseClient.getClosestNode())){
 				//client location changed - update client location and streaming
 			}
 		}else if(node != null){
 			//client discovered - update
 		}else if(databaseClient.getClosestNode() != null){
 			//client lost - update
 		}
 		return updateClientToDB(databaseClient);
 	}
 
 	/**
 	 * Updates the client in the database.
 	 * 
 	 * @param dbClient
 	 *            The DatabaseClient to use for updating.
 	 * @return Whether the operation succeeded.
 	 */
 	private boolean updateClientToDB(DatabaseClient dbClient) {
 		boolean result = false;
 
 		this.db.lock();
 		Connection conn = this.db.getConnection();
 		try {
 			PreparedStatement pstatement = conn.prepareStatement("UPDATE clients SET "
 					+ "macaddress = ?, " + "ipaddress = ?, " + "bluetoothname = ?, "
 					+ "userid = ? " + "WHERE id = ?;");
 			pstatement.setString(1, dbClient.getMacAddress());
 			pstatement.setString(2, dbClient.getIpAddress());
 			pstatement.setString(3, dbClient.getBluetoothName());
 			pstatement.setInt(4, dbClient.getLoggedInUserId());
 			pstatement.setInt(5, dbClient.getId());
 			pstatement.executeUpdate();
 
 			result = true;
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		this.db.unlock();
 		return result;
 	}
 }
