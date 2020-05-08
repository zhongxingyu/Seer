 package risiko.net;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.zoolu.tools.Log;
 
 import com.google.gson.Gson;
 
 import risiko.net.configuration.ClientConfiguration;
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
 import it.unipr.ce.dsg.s2p.peer.Peer;
 import it.unipr.ce.dsg.s2p.sip.Address;
 
 public class Client extends Peer {
 
 	// / UTILITIES
 	private Log m_log;
 	private ClientConfiguration m_config;
 	private Gson m_jsonParser;
 	private GroovyClassLoader m_scriptLoader;
 
 	// / SCRIPTING
 	private IRules m_rules;
 
 	// / GAME LOGIC
 	private boolean m_gameStarted = false;
 	private boolean m_connected = false;
 	private PlayerInfo m_clientInfo;
 	private TerritoriesLayout m_territories;
 	private boolean m_initialized = false;
 	private PlayerColor m_turnOwner;
 	private AttackData m_currentAttack;
 
 	// / State management
 
 	volatile private ClientState m_state;
 	volatile private boolean m_stateChanged;
 
 	public Client(String pathConfig, String key) {
 		super(pathConfig, key);
 		// Configuration file
 		m_config = new ClientConfiguration(pathConfig);
 
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
 			m_log = new Log(this.nodeConfig.log_path + "info_" + this.peerDescriptor.getName() + ".log",
 					Log.LEVEL_MEDIUM);
 		}
 		m_log.println("creating log file.");
 		m_territories = new TerritoriesLayout();
 		m_jsonParser = new Gson();
 
 		// Set startup state
 		setState(ClientState.WAIT_FOR_CONFIGURATION);
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
 			throw new RuntimeException();
 		}
 
 		// Parsing messages without content
 		if (type.equals(Signal.SIGNAL_MSG)) {
 			Signal signal = m_jsonParser.fromJson(params, Signal.class);
 
 			if (signal.getSignalType() == SignalType.START_GAME) {
 				m_gameStarted = true;
 			} else if (signal.getSignalType() == SignalType.DISCONNECTION) {
 				m_log.println("Disconnecting from the server.");
 				setState(ClientState.GAME_DISCONNECTION);
 				m_connected = false;
 			} else if (signal.getSignalType() == SignalType.CONNECTION_ACCEPTED) {
 				m_log.println("Connection accepted from the server");
 				m_connected = true;
 			} else if (signal.getSignalType() == SignalType.CONNECTION_REFUSED) {
 				m_log.println("Connection refused from the server");
 				setState(ClientState.GAME_DISCONNECTION);
 				m_connected = false;
 				setState(ClientState.CONNECTION_REFUSED);
 			} else if (signal.getSignalType() == SignalType.END_GAME) {
 				m_log.println("Server terminated the game.");
 				setState(ClientState.GAME_DISCONNECTION);
 				m_connected = false;
 			}
 		}
 
 		// If message is used during the initialization phase specifies
 		// the available number of unit at the beginning and the player
 		// color.
 		else if (type.equals(PlayerInfo.PLAYER_COLOR_MSG)) {
 			PlayerInfo player = m_jsonParser.fromJson(params, PlayerInfo.class);
 
 			if (m_initialized == false) {
 				m_clientInfo = player;
 				m_log.println("Assigned color to client is: " + m_clientInfo.getColor() + ". Available unit are: "
 						+ m_clientInfo.getTotalUnits());
 				setState(ClientState.CONFIGURED);
 				m_initialized = true;
 			} else {
 				m_turnOwner = player.getColor();
 				if (isClientTurn()) {
 					m_log.println("It's my turn ...");
 					m_clientInfo.setTotalUnit(player.getTotalUnits());
 				} else {
 					m_log.println("It's player " + m_turnOwner + " turn.");
 				}
 				setState(ClientState.REINFORCEMENT);
 			}
 		}
 
 		else if (type.equals(TerritoriesLayout.TERRITORIES_LAYOUT_MSG)) {
 			m_territories = m_jsonParser.fromJson(params, TerritoriesLayout.class);
 
 			if (m_state == ClientState.CONFIGURED) {
 				setState(ClientState.TURN_ASSIGNED);
 			} else if (m_state == ClientState.UNITS_POSITIONED) {
 				setState(ClientState.END_REINFORCEMENT);
 			} else if (m_state == ClientState.AFTER_ATTACK) {
 				setState(ClientState.NEW_DISPOSITION);
 			}
 		}
 
 		else if (type.equals(AttackData.ATTACK_DATA_MSG)) {
 
 			if (m_state == ClientState.END_REINFORCEMENT || m_state == ClientState.ATTACK_PHASE
 					|| m_state == ClientState.NEW_DISPOSITION) {
 				AttackData attack = m_jsonParser.fromJson(params, AttackData.class);
 				TerritoryInfo attackedTer = m_territories.get(attack.getAttackedID());
 				TerritoryInfo attackerTer = m_territories.get(attack.getAttackerID());
 				m_currentAttack = attack;
 
 				if (attack.getPhase() == AttackPhase.ATTACK) {
 					setState(ClientState.ATTACK_PHASE);
 				}
 
 				// If client is under attack send the defence
 				if (attackedTer.getOwner() == getColor() && attack.getPhase() == AttackPhase.ATTACK) {
 					m_log.println("I'm under attack!!");
 					List<Integer> defence = new ArrayList<Integer>();
 					int n = (attackedTer.getUnitNumber() >= attack.getAttackingUnits() ? attack.getAttackingUnits()
 							: attackedTer.getUnitNumber());
 					for (int i = 0; i < n; ++i)
 						defence.add(m_rules.getDiceValue());
 					attack.setDefenceValues(defence);
 					String response = m_jsonParser.toJson(new JSONMessage(attack));
 					sendJSON(new Address(m_config.server_address), response);
 				}
 
 				if (attack.getPhase() == AttackPhase.DEFENCE) {
 					setState(ClientState.AFTER_ATTACK);
 				}
 			}
 		} else if (type.equals(Winner.WINNER_MSG)) {
 			Winner winner = m_jsonParser.fromJson(params, Winner.class);
 			if (winner.getWinner() == getColor())
 				setState(ClientState.WIN);
 			else
 				setState(ClientState.LOSE);
 		}
 
 	}
 
 	private void sendJSON(Address toAddress, String msg) {
 		sendMessage(toAddress, getAddress(), msg, "application/json");
 	}
 
 	@Override
 	protected void onDeliveryMsgFailure(String arg0, Address arg1, String arg2) {
 	}
 
 	@Override
 	protected void onDeliveryMsgSuccess(String arg0, Address arg1, String arg2) {
 	}
 
 	/**
 	 * Connect the client with the server.
 	 */
 	public void connect() {
 		String msg = m_jsonParser.toJson(new JSONMessage(new Signal(SignalType.CONNECTION, peerDescriptor)));
 		sendJSON(new Address(m_config.server_address), msg);
 	}
 
 	/**
 	 * Disconnect the client from the server.
 	 */
 	public void disconnect() {
 		String msg = m_jsonParser.toJson(new JSONMessage(new Signal(SignalType.DISCONNECTION, peerDescriptor)));
 		sendJSON(new Address(m_config.server_address), msg);
 		m_connected = false;
 		setState(ClientState.GAME_DISCONNECTION);
 	}
 
 	/**
 	 * Set new state of the client, the state must be changed with this function!
 	 * @param newState
 	 */
 	private void setState(ClientState newState) {
 		m_log.println("State changed from " + m_state + " to " + newState + ".");
 		m_state = newState;
 		m_stateChanged = true;
 	}
 
 	/**
 	 * Check if the client state is changed.
 	 * @return True if the state is changed.
 	 */
 	public boolean isStateChanged() {
 		return m_stateChanged;
 	}
 
 	/**
 	 * Synchronize the client with the server, sending an ACK message.
 	 */
 	public void synchronize() {
 		String msg = m_jsonParser.toJson(new JSONMessage(new Signal(SignalType.ACK, peerDescriptor)));
 		sendJSON(new Address(m_config.server_address), msg);
 	}
 
 	public boolean isConnected() {
 		return m_connected;
 	}
 
 	// STATE MANAGEMENT
 
 	public ClientState getState() {
 		m_stateChanged = false;
 		return m_state;
 	}
 
 	// LOGIC
 
 	public boolean isGameStarted() {
 		return m_gameStarted;
 	}
 
 	public int getAvailableUnits() {
 		return m_clientInfo.getTotalUnits();
 	}
 
 	public int getUsedUnits() {
 		return m_territories.getPlayerUnits(m_clientInfo.getColor());
 	}
 
 	public PlayerColor getColor() {
 		return m_clientInfo.getColor();
 	}
 
 	public PlayerColor getTurnOwner() {
 		return m_turnOwner;
 	}
 
 	public boolean isClientTurn() {
 		return (m_turnOwner == m_clientInfo.getColor());
 	}
 
 	public void finishAttackPhase() {
 		String msg = m_jsonParser.toJson(new JSONMessage(new Signal(SignalType.ATTACK_PHASE_ENDED, peerDescriptor)));
 		sendJSON(new Address(m_config.server_address), msg);
 	}
 
 	public TerritoriesLayout getTerritoriesLayout() {
 		return m_territories;
 	}
 
 	/**
 	 * Send the new territories configuration to the server.
 	 */
 	public void sendNewTerritoriesConfiguration() {
 		String msg = m_jsonParser.toJson(new JSONMessage(m_territories));
 		sendJSON(new Address(m_config.server_address), msg);
 		setState(ClientState.UNITS_POSITIONED);
 	}
 
 	public void initialize(){
 		m_territories = new TerritoriesLayout();
 		m_gameStarted = false;
 		m_connected = false;
 		m_initialized = false;
 		m_turnOwner = null;
 		m_currentAttack = null;
 		setState(ClientState.WAIT_FOR_CONFIGURATION);
 	}
 	/**
 	 * Get the latest attack data available.
 	 */
 	public AttackData getAttackData() {
 		return m_currentAttack;
 	}
 
 	/**
 	 * Send to the server an attack message.
 	 * @param fromID
 	 * @param toID
 	 * @param units
 	 * @return True is the attack is valid, false otherwise.
 	 */
 	public boolean attack(int fromID, int toID, int units) {
 		List<Integer> attacks = new ArrayList<Integer>();
 		for (int i = 0; i < units; ++i) {
 			attacks.add(m_rules.getDiceValue());
 		}
 		AttackData attack = new AttackData(fromID, toID, attacks);
		if (m_rules.isValidAttack(attack, m_territories)) {
 			String msg = m_jsonParser.toJson(new JSONMessage(attack));
 			sendJSON(new Address(m_config.server_address), msg);
 			return true;
 		}
 		return false;
 	}
 }
