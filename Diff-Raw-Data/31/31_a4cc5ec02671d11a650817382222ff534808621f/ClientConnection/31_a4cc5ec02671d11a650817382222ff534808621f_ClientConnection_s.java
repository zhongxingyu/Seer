 package battlechallenge.server;
 
 import java.net.Socket;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import battlechallenge.ActionResult;
 import battlechallenge.CommunicationConstants;
 import battlechallenge.ShipAction;
 import battlechallenge.maps.BattleMap;
 import battlechallenge.network.ConnectionLostException;
 import battlechallenge.network.NetworkSocket;
 import battlechallenge.ship.Ship;
import battlechallenge.structures.Structure;
 
 /**
  * The Class ClientConnection.
  */
 public class ClientConnection {
 
 	
 	
 	/** The Socket conn. */
 	private NetworkSocket socket;
 	
 	/**
 	 * Instantiates a new client connection.
 	 *
 	 * @param conn the connection
 	 * @param id the player ID
 	 */
 	public ClientConnection(Socket conn, int id) {
 		if (conn == null)
 			throw new IllegalArgumentException("Socket cannot be null");
 		socket = new NetworkSocket(conn);
 		setupHandshake();
 		System.out.println("Client connection confirmed:" + id);
 	}
 	
 	/**
 	 * 
 	 * @return True if the socket is open
 	 */
 	public boolean isOpen() {
 		return socket.isOpen();
 	}
 	
 	/**
 	 * If the game is over, write the result to the player
 	 * @param result The String result of the game to write to the player
 	 */
 	public void endGame(String result) {
 		try {
 			socket.writeObject(result);
 			Thread.sleep(CommunicationConstants.SOCKET_WAIT_TIME);
 		} catch (ConnectionLostException e) {
 			/* ignore exceptions */
 		} catch (InterruptedException e) {
 			/* ignore exceptions */
 		}
 		kill();
 	}
 	
 	/**
 	 * Kill the client connection
 	 */
 	public void kill() {
 		socket.kill();
 	}
 	
 	/**
 	 * Setup handshake.
 	 *
 	 * @return true, if successful
 	 * @throws ConnectionLostException the connection lost exception
 	 */
 	public boolean setupHandshake() {
 		try {
 			socket.writeObject(CommunicationConstants.REQUEST_HANDSHAKE);
 			socket.writeObject(CommunicationConstants.SERVER_VERSION);
 			// FIXME: make this a timed blocking call
 			String client_version = (String)socket.readObject(1000);
 			if (client_version != null && CommunicationConstants.SUPPORTED_CLIENTS.contains(client_version)) {
 				return true;
 			} else {
 				// TODO: handle unsupported client version. Drop bad player and add another
 				return false;
 			}
 		} catch (ClassCastException e) {
 			// Someone is trying to break our server or, our code is really broken.
 			// TODO: handle invalid input
 			e.printStackTrace();
 		} catch (ConnectionLostException e) {
 			// TODO: bad client connection. Drop bad player and add another
 			e.printStackTrace();
 		}
 		return false;
 	}
 	
 	/**
 	 * Sets the player credentials.
 	 * @param id The id that identifies a player
 	 * @param height the height of the board
 	 * @param width the width of the board
 	 * @return the name of the client if successful, otherwise null
 	 * @throws ConnectionLostException the connection was lost throw an exception
 	 */
 	public String setCredentials(int id, int height, int width) {
 		try {
 			socket.writeObject(CommunicationConstants.REQUEST_CREDENTIALS);
 			socket.writeInt(id);
 			socket.writeInt(width);
 			socket.writeInt(height);
 			String client_name = (String)socket.readObject(1000);
 			return client_name;
 		} catch (ClassCastException e) {
 			// Someone is trying to break our server or, our code is really broken.
 			// TODO: handle invalid input
 			e.printStackTrace();
 		} catch (ConnectionLostException e) {
 			// TODO: bad client connection. Drop bad player and add another
 		}
 		return null;
 	}
 
 	/**
 	 * Tells the client to expect turn parameters then passes 
 	 * the current list of ships and actionResult list to the client
 	 * @param ships the current list of ships as of last turn
 	 * @param actionResults list of actionResults from last turn
 	 * @return true, if successful
 	 * @throws ConnectionLostException a lost connection exception
 	 */
 	public boolean requestTurn(Map<Integer, List<Ship>> ships, Map<Integer, List<ActionResult>> actionResults, BattleMap map) {
 		try {
 			// send command
 			socket.writeObject(CommunicationConstants.REQUEST_DO_TURN);
 			// send current players boats
 			socket.writeObject(ships);
 			// send action results hash table
 			socket.writeObject(actionResults);
 			// send list of structures
 			socket.writeObject(map);
 			return true;
 		} catch (ConnectionLostException e) {
 			System.err.println("Socket Exception: Client disconnected. Disqualifying player and ending game.");
 		}
 		return false;
 	}
 	
 	/**
 	 * Gets the coordinates passed by the client at the end of a turn
 	 *
 	 * @return the list of coordinates returned by the ClientPlayer's doTurn method
 	 * @throws ConnectionLostException the connection lost exception
 	 */
 	public List<ShipAction> getTurn() throws ConnectionLostException {
 		try {
 			/*
 			 * check to see if something is arrived in time. Otherwise, assume
 			 * no input. 
 			 */
 			@SuppressWarnings("unchecked")
 			List<ShipAction> temp = (List<ShipAction>)socket.readObject(false);
 			// check that list is not null
 			if (temp == null)
 				return new LinkedList<ShipAction>();
 			// validate that no ships in returned list are null
 			for (ShipAction c : temp) {
 				if (c == null)
 					return new LinkedList<ShipAction>();
 			}
 			return temp;
 			
 		} catch (ClassCastException e) {
 			// Someone is trying to break our server or, our code is really broken.
 			// TODO: handle invalid input
 		} catch (ConnectionLostException e) {			
 			// TODO: handle invalid input
 		}
 		return new LinkedList<ShipAction>();
 	}
 }
