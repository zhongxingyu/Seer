 /**
  * @author Federico Cabassi
  */
 
 package risiko.net;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.zoolu.tools.Log;
 
 import com.google.gson.Gson;
 
 import risiko.net.data.AttackPhase;
 import risiko.net.data.PlayerColor;
 import risiko.net.data.RisikoData;
 import risiko.net.data.SignalType;
 import risiko.net.data.TerritoryInfo;
 import risiko.net.data.sendable.AttackData;
 import risiko.net.data.sendable.PlayerInfo;
 import risiko.net.data.sendable.Signal;
 import risiko.net.data.sendable.TerritoriesLayout;
 import risiko.net.data.sendable.Winner;
 import risiko.net.gson.JSONMessage;
 import risiko.net.script.IRules;
 
 import groovy.lang.GroovyClassLoader;
 import it.unipr.ce.dsg.s2p.org.json.JSONException;
 import it.unipr.ce.dsg.s2p.org.json.JSONObject;
 import it.unipr.ce.dsg.s2p.peer.NeighborPeerDescriptor;
 import it.unipr.ce.dsg.s2p.peer.Peer;
 import it.unipr.ce.dsg.s2p.peer.PeerDescriptor;
 import it.unipr.ce.dsg.s2p.sip.Address;
 
 /**
  * When the server reaches the minimum number of players the game starts.
  * 
  * @author Federico Cabassi
  * 
  */
 public class Server extends Peer {
 
 	// // UTILITIES
 	private Log m_log;
 	private Gson m_jsonParser;
 	private GroovyClassLoader m_scriptLoader;
 
 	// // SCRIPTING
 	private IRules m_rules;
 
 	// // SYNCHRONIZATION
 	volatile private int m_barrierCount = 0;
 	volatile private boolean m_synchronizing = false;
 	volatile private List<Address> m_synchronizedClients;
 
 	// // GAME LOGIC
 	// Game state
 	private int m_playerNumber;
 	private TerritoriesLayout m_territories;
 	private Map<NeighborPeerDescriptor, PlayerInfo> m_players;
 	// Turn
 	private PlayerInfo m_turnOwner;
 	private int m_turnCounter;
 
 	volatile private ServerState m_state;
 
 	public Server(String pathConfig, String key) {
 		super(pathConfig, key);
 
 		// Parser
 		m_jsonParser = new Gson();
 
 		// Synchronization
 		m_synchronizedClients = new ArrayList<Address>();
 
 		// Load script
 		m_scriptLoader = new GroovyClassLoader();
 		Object script = null;
 		try {
 			Class clazz = m_scriptLoader.parseClass(new File(RisikoData.SCRIPT_NAME));
 			script = clazz.newInstance();
 		} catch (Exception e) {
 			new RuntimeException();
 			e.printStackTrace();
 		}
 		m_rules = (IRules) script;
 
 		// Create the log file
 		if (this.nodeConfig.log_path != null) {
 			m_log = new Log(this.nodeConfig.log_path + "" + this.peerDescriptor.getName() + ".log", Log.LEVEL_MEDIUM);
 		}
 		m_log.print("creating log file.");
 
 		// Initialize game logic
 		m_players = new HashMap<NeighborPeerDescriptor, PlayerInfo>();
 
 		m_territories = new TerritoriesLayout();
 		for (int i = 0; i < RisikoData.mapColumns * RisikoData.mapRows; ++i) {
 			// 1 unit per territory at least
 			m_territories.put(new TerritoryInfo(i, 1, PlayerColor.NONE));
 		}
 	}
 
 	@Override
 	protected void onDeliveryMsgFailure(String arg0, Address arg1, String arg2) {
 	}
 
 	@Override
 	protected void onDeliveryMsgSuccess(String arg0, Address arg1, String arg2) {
 	}
 
 	private void printLogInformation(JSONObject msg, Address sender) {
 		try {
 			m_log.println("**!** " + this.peerDescriptor.getName() + " has received JSON message type '"
 					+ msg.get("type").toString() + "' form: " + sender.toString());
 			System.out.println("**!** " + this.peerDescriptor.getName() + " has received JSON message type '"
 					+ msg.get("type").toString() + "' form: " + sender.toString());
 			m_log.println(msg.toString());
 		} catch (JSONException e) {
 			new RuntimeException();
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	protected void onReceivedJSONMsg(JSONObject msg, Address sender) {
 		super.onReceivedJSONMsg(msg, sender);
 
 		printLogInformation(msg, sender);
 		String params, type;
 		try {
 			params = msg.getJSONObject("payload").getJSONObject("params").toString();
 			type = msg.get("type").toString();
 		} catch (JSONException e) {
 			throw new RuntimeException(e);
 		}
 
 		if (type.equals(Signal.SIGNAL_MSG)) {
 
 			Signal signal = m_jsonParser.fromJson(params, Signal.class);
 
 			if (signal.getSignalType() == SignalType.CONNECTION) {
 				// If game is started refuse client connection
 				if (m_state != ServerState.GAME_NOT_START) {
 					String response = m_jsonParser.toJson(new JSONMessage(new Signal(SignalType.CONNECTION_REFUSED,
 							peerDescriptor)));
 					sendJSON(sender, response);
 				} else {
 					PeerDescriptor peerDesc = signal.getPeerDescriptor();
 					peerList.put(peerDesc.getKey(), new NeighborPeerDescriptor(peerDesc));
 					m_log.println("Peer " + peerDesc.getName() + " added to available peer list.");
 
 					String response = m_jsonParser.toJson(new JSONMessage(new Signal(SignalType.CONNECTION_ACCEPTED,
 							peerDescriptor)));
 					sendJSON(sender, response);
 					// Increment the number of players
 					m_playerNumber++;
 
 					if (peerList.size() >= m_rules.getRequiredPlayerNumber()) {
 						m_log.println("Minimum clients number reached.");
 					}
 				}
 			} else if (signal.getSignalType() == SignalType.DISCONNECTION) {
 				PeerDescriptor peerDesc = signal.getPeerDescriptor();
 				peerList.remove(peerDesc.getKey());
 				// Decrement the number of players
 				m_playerNumber--;
 				if (m_state != ServerState.GAME_NOT_START) {
 					String response = m_jsonParser.toJson(new JSONMessage(new Signal(SignalType.END_GAME,
 							peerDescriptor)));
 					broadcastMessage(response);
 					setState(ServerState.END);
 				}
 			} else if (signal.getSignalType() == SignalType.ACK) {
 				if (m_synchronizing && !isClientSynchronized(new Address(sender))) {
 					signal(new Address(sender));
 				}
 			} else if (signal.getSignalType() == SignalType.ATTACK_PHASE_ENDED) {
 				if (m_state == ServerState.ATTACK)
 					setState(ServerState.TURN_ASSIGNAMENT);
 			}
 		}
 
 		if (type.equals(TerritoriesLayout.TERRITORIES_LAYOUT_MSG) && m_state == ServerState.REINFORCEMENT) {
 			if (!isClientSynchronized(new Address(sender))) {
 				TerritoriesLayout layout = m_jsonParser.fromJson(params, TerritoriesLayout.class);
 
 				PlayerInfo player = getPlayerFromAddress(sender);
 
 				m_log.println("Updating territories of player: " + player.getColor());
 				if (m_rules.checkTerritoriesLayout(m_territories, layout, player)) {
 					updateTerritories(layout, player);
 					signal(new Address(sender));
 				}
 			} else {
 				m_log.println("Client " + sender + " has already sent territory_layout message.");
 			}
 		}
 
 		if (type.equals(AttackData.ATTACK_DATA_MSG)) {
 			AttackData attack = m_jsonParser.fromJson(params, AttackData.class);
 			// Attack
			if (m_rules.isValidAttack(attack, m_territories, m_territories.get(attack.getAttackerID()).getOwner()) 
					&& attack.getPhase() == AttackPhase.ATTACK) {
 				if (m_territories.get(attack.getAttackerID()).getOwner() == getPlayerFromAddress(sender).getColor()) {
 					m_log.println("Attack data received.");
 					// Resend the message at all the other players
 					broadcastMessage(msg.toString());
 					setState(ServerState.ATTACK_STARTED);
 				}
 			}
 			// Defence
 			else if (m_rules.isValidDefence(attack, m_territories) && attack.getPhase() == AttackPhase.DEFENCE
 					&& m_state == ServerState.ATTACK_STARTED) {
 				if (m_territories.get(attack.getAttackedID()).getOwner() == getPlayerFromAddress(sender).getColor()) {
 					m_log.println("Defence data received.");
 
 					int attDest = m_rules.attackerUnitsDestroyed(attack);
 					int defDest = m_rules.attackedUnitsDestroyed(attack);
 					int attUnits = m_territories.get(attack.getAttackerID()).getUnitNumber();
 					int defUnits = m_territories.get(attack.getAttackedID()).getUnitNumber();
 
 					m_log.println("Attacker unit destroied: " + attDest);
 					m_log.println("Defencer unit destroied: " + defDest);
 
 					if (defUnits - defDest == 0) {
 						// Attacker wins
 						TerritoryInfo attackedTerr = m_territories.get(attack.getAttackedID());
 						TerritoryInfo attackerTerr = m_territories.get(attack.getAttackerID());
 						PlayerColor attacker = m_territories.get(attack.getAttackerID()).getOwner();
 
 						// Set the new owner and put one unit in the new
 						// territory
 						attackedTerr.setOwner(attacker);
 						attackedTerr.setUnitNumber(1);
 						attackerTerr.setUnitNumber(attackerTerr.getUnitNumber() - 1);
 					} else {
 						m_log.println("Updated territories.");
 						m_territories.get(attack.getAttackerID()).setUnitNumber(attUnits - attDest);
 						m_territories.get(attack.getAttackedID()).setUnitNumber(defUnits - defDest);
 					}
 					// Resend the message at all the other players
 					broadcastMessage(msg.toString());
 					m_log.println("Wait for a syncronization.");
 					m_state = ServerState.ATTACK;
 				}
 			}
 			// If not valid ignore it
 		}
 
 	}
 
 	private PlayerInfo getPlayerFromAddress(Address address) {
 		// Get player from address
 		PlayerInfo player = new PlayerInfo();
 		Iterator<NeighborPeerDescriptor> iter = m_players.keySet().iterator();
 		while (iter.hasNext()) {
 			NeighborPeerDescriptor client = iter.next();
 			if (client.getAddress().equals(address.getURL())) {
 				player = m_players.get(client);
 			}
 		}
 		return player;
 	}
 
 	private void broadcastMessage(String msg) {
 		Iterator<String> iter = this.peerList.keySet().iterator();
 		while (iter.hasNext()) {
 			String key = (String) iter.next();
 			NeighborPeerDescriptor neighborPD = this.peerList.get(key);
 			sendJSON(new Address(neighborPD.getAddress()), msg);
 		}
 	}
 
 	private void sendJSON(Address toAddress, String msg) {
 		sendMessage(toAddress, getAddress(), msg, "application/json");
 	}
 
 	private void barrier() {
 		System.out.println("Synchronizing ...");
 		m_synchronizing = true;
 		while (peerList.size() != m_barrierCount && m_state != ServerState.END) {
 			// Do nothing
 		}
 		m_synchronizing = false;
 		m_log.println("Synchronization occurred.");
 		resetClientSynchronization();
 		m_barrierCount = 0;
 	}
 
 	/**
 	 * Check if a client is already synchronized.
 	 * 
 	 * @param clientAddress
 	 * @return True if client is synchronized, false otherwise.
 	 */
 	private boolean isClientSynchronized(Address clientAddress) {
 		if (m_synchronizedClients.contains(clientAddress))
 			return true;
 		return false;
 	}
 
 	/**
 	 * Add a client into the synchronized clients list.
 	 * 
 	 * @param clientAddress
 	 */
 	private void signal(Address clientAddress) {
 		m_log.println("Client " + clientAddress.toString() + " is synchronized.");
 		m_synchronizedClients.add(clientAddress);
 		m_barrierCount++;
 	}
 
 	/**
 	 * Clear the content of the synchronized clients list.
 	 */
 	private void resetClientSynchronization() {
 		m_synchronizedClients.clear();
 	}
 
 	/**
 	 * Update the layout of territories for only one player. Note: This
 	 * functions assumes that layout is a valid representation of player
 	 * territories.
 	 * 
 	 * @param layout
 	 * @param info
 	 */
 	private void updateTerritories(TerritoriesLayout layout, PlayerInfo info) {
 		TerritoriesLayout terrs = layout.getSubset(info.getColor());
 		Iterator<Integer> iter = terrs.keySet().iterator();
 		while (iter.hasNext()) {
 			TerritoryInfo ter = terrs.get(iter.next());
 			m_territories.put(ter);
 		}
 
 		m_log.println("New territories configuration: " + m_jsonParser.toJson(new JSONMessage(m_territories)));
 
 	}
 
 	/**
 	 * Assign a color at the connected clients, and send at each one a message
 	 */
 	private void assignColorToClients() {
 		Iterator<String> iter = peerList.keySet().iterator();
 		for (PlayerColor color : PlayerColor.values()) {
 			if (!iter.hasNext())
 				return; // Iterated over all players
 			if (color == PlayerColor.NONE)
 				continue;
 
 			PlayerInfo info = new PlayerInfo(color, m_rules.getInitUnits(m_playerNumber));
 			NeighborPeerDescriptor peer = peerList.get((String) iter.next());
 			m_players.put(peer, info);
 
 			String msg = m_jsonParser.toJson(new JSONMessage(info));
 			sendJSON(new Address(peer.getAddress()), msg);
 
 			m_log.println("Sended color " + info.getColor() + " to client " + peer.getName());
 		}
 	}
 
 	/**
 	 * Assign at each client a subset of territories.
 	 */
 	private void assignTerritoriesToClients() {
 		// Generate a list of number and assign that number to the players
 		int size = RisikoData.mapColumns * RisikoData.mapRows;
 		List<Integer> random = new ArrayList<Integer>();
 		for (int i = 0; i < size; ++i) {
 			random.add(new Integer(i));
 		}
 		// Randomize the list
 		Collections.shuffle(random);
 
 		int territoryForPlayer = size / m_playerNumber;
 		int remainder = size % m_playerNumber;
 
 		m_log.println("Generating the territories layout:");
 
 		Iterator<NeighborPeerDescriptor> peer = m_players.keySet().iterator();
 		while (peer.hasNext()) {
 			PlayerColor owner = m_players.get(peer.next()).getColor();
 
 			for (int j = 0; j < territoryForPlayer; ++j) {
 				int territoryId = random.get(0);
 				m_territories.get(territoryId).setOwner(owner);
 				random.remove(0);
 				m_log.println("Assigned territory: " + territoryId + " at player " + owner);
 			}
 
 			if (remainder > 0) {
 				int territoryId = random.get(0);
 				m_territories.get(territoryId).setOwner(owner);
 				random.remove(0);
 				remainder--;
 				m_log.println("Assigned territory: " + territoryId + " at player " + owner);
 			}
 		}
 
 		// Send the new configuration at all the clients
 		sendTerritoriesToClients();
 	}
 
 	/**
 	 * Send the current territories layout to clients.
 	 */
 	private void sendTerritoriesToClients() {
 		String msg = m_jsonParser.toJson(new JSONMessage(m_territories));
 		broadcastMessage(msg);
 	}
 
 	/**
 	 * Check if the game can start. The game can start if there are at least
 	 * getRequiredClientNumber (into the script file) players.
 	 * 
 	 * @return True if game can start
 	 */
 	private boolean gameCanStart() {
 		if (m_state != ServerState.GAME_NOT_START)
 			return false;
 		if (peerList.size() >= m_rules.getRequiredPlayerNumber()) {
 			m_log.println("The game can start");
 			return true;
 		} else
 			return false;
 	}
 
 	/**
 	 * Signal at each player that the game is started.
 	 */
 	private void startGame() {
 		String msg = m_jsonParser.toJson(new JSONMessage<Signal>(new Signal(SignalType.START_GAME, peerDescriptor)));
 		broadcastMessage(msg);
 	}
 
 	/**
 	 * Signal at each player how is the turn owner for ths turn.
 	 * 
 	 * @param player
 	 *            Turn owner.
 	 */
 	private void assignTurn(PlayerInfo player) {
 		m_log.println("Turn assigned at player: " + player.getColor());
 		m_turnOwner = player;
 
 		m_turnOwner.incrementTotalUnit(m_rules.getReinforcementUnits(m_turnCounter, m_territories));
 		broadcastMessage(m_jsonParser.toJson(new JSONMessage(m_turnOwner)));
 	}
 
 	/**
 	 * This function is blocking during the attack phase. At the end of the
 	 * attack phase the function return.
 	 */
 	private void attack() {
 
 		if (m_state == ServerState.ATTACK_STARTED) {
 			while (m_state == ServerState.ATTACK_STARTED) {
 				// Wait
 			}
 			barrier();
 
 			m_log.println("Sending new territories configuration.");
 			sendTerritoriesToClients();
 			barrier();
 			m_log.println("Attack ended!!");
 		}
 	}
 
 	private void terminateGame(PlayerColor winner) {
 		String msg = m_jsonParser.toJson(new JSONMessage<Winner>(new Winner(winner)));
 		broadcastMessage(msg);
 	}
 
 	private void setState(ServerState newState) {
 		if (ServerState.END == m_state)
 			return;
 		m_log.println("State changed from " + m_state + " to " + newState + ".");
 		System.out.println("State changed from " + m_state + " to " + newState + ".");
 		m_state = newState;
 	}
 
 	public void terminate() {
 		setState(ServerState.END);
 		peerList.clear();
 		this.halt();
 	}
 
 	/**
 	 * Enter in the server loop. This function is blocking for all the game
 	 * length, the function return only if the game is over.
 	 */
 	public void run() {
 		setState(ServerState.GAME_NOT_START);
 
 		// Initialization
 		while (!gameCanStart()) {
 		} // Loop until game can start
 
 		setState(ServerState.CONFIGURATION);
 		m_log.println("Game is started");
 		startGame();
 
 		m_log.println("Assigning IDs to clients");
 		assignColorToClients();
 		barrier();
 
 		m_log.println("Assign territories to clients");
 		assignTerritoriesToClients();
 		barrier();
 
 		// Turn loop
 		while (m_state != ServerState.END) {
 			// Assign the turn at all the player in order.
 			Iterator<NeighborPeerDescriptor> players = m_players.keySet().iterator();
 			while (players.hasNext()) {
 
 				System.out.println("Prova");
 				PlayerColor winner = m_rules.getWinner(m_territories);
 				if (winner != PlayerColor.NONE) {
 					terminateGame(winner);
 					return;
 				}
 
 				m_turnCounter++;
 				PlayerInfo player = m_players.get(players.next());
 
 				setState(ServerState.TURN_ASSIGNAMENT);
 				// Assign turn
 				if (m_state != ServerState.END)
 					assignTurn(player);
 				barrier();
 				m_log.println("Turn assigned.");
 
 				setState(ServerState.REINFORCEMENT);
 				// Wait for unit disposal
 				barrier();
 				if (m_state != ServerState.END)
 					sendTerritoriesToClients();
 				m_log.println("New territories configuration sended to clients.");
 
 				setState(ServerState.ATTACK);
 				// m_attackPhaseEnded = false;
 				while ((m_state == ServerState.ATTACK || m_state == ServerState.ATTACK_STARTED)
 						&& !(m_state == ServerState.END)) {
 					attack();
 				}
 				m_log.println("Attack phase finished.");
 
 			}
 		}
 
 		terminate();
 	}
 
 }
