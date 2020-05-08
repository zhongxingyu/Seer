 package games.distetris.domain;
 
 import java.io.IOException;
 import java.net.InetAddress;
 import java.net.NetworkInterface;
 import java.util.Enumeration;
 import java.util.Vector;
 
 import android.net.DhcpInfo;
 import android.net.wifi.WifiManager;
 import android.os.Handler;
 import android.util.Log;
 
 public class CtrlNet {
 	
 	public static Integer PORT = 17375;
 	public Integer portServer = 17375;
 	
 	private static CtrlNet INSTANCE = null;
 
 	private Vector<Player> players;
 	private Vector<Vector<Integer>> teamPlayer;
 	private Integer pointerTeamPlayer = 0;
 	
 	private Integer numTeams;
 	
 	private TCPServer threadTCPServer;
 	private TCPConnection threadTCPClient;
 	private UDPServer threadUDPServer;
 
 	private WifiManager wifiManager;
 
 	/*
 	 * 
 	 * 
 	 * 
 	 * CONTROLLER
 	 * 
 	 * 
 	 * 
 	 */
 
 	private CtrlNet() {
 		this.players = new Vector<Player>();
 		this.teamPlayer = new Vector<Vector<Integer>>();
 	}
 	
 	public static CtrlNet getInstance() {
 		if (INSTANCE == null) {
 			INSTANCE = new CtrlNet();
 		}
 		return INSTANCE;
 	}
 
 	/*
 	 * 
 	 * 
 	 * 
 	 * SERVER TCP
 	 * 
 	 * 
 	 * 
 	 */
 
 	public void serverTCPStart(int numTeams, int numTurns) throws Exception {
 
 		// TODO: Revisar! Sergio's code
 		this.numTeams = numTeams;
 		for (int i = 0; i<(numTeams); i++) {
 			Vector<Integer> seq = new Vector<Integer>();
 			teamPlayer.add(seq);
 		}
 		
 		// Clear previous connections (if any)
 		serverTCPStop();
 		serverTCPDisconnectClients();
 
 		this.players = new Vector<Player>();
 
 		// Creating server
 		this.threadTCPServer = new TCPServer(players, numTeams, numTurns);
 		this.threadTCPServer.start();
 
 		L.d("thread started");
 
 		// Waiting for the server to start
 		while (!this.threadTCPServer.isAlive()) {
 			;
 		}
 		L.d("thread alive");
 		while (!this.threadTCPServer.isListening()) {
 			;
 		}
 		L.d("thread listening");
 
 		// Connecting like a normal client
 		serverTCPConnect("127.0.0.1", this.portServer);
 
 		L.d("connected");
 	}
 
 	public void serverTCPStop() {
 		L.d("TCPServer starting close");
 
 		if (this.threadTCPServer != null && this.threadTCPServer.isAlive()) {
 			this.threadTCPServer.close();
 			while (this.threadTCPServer.isAlive()) {
 				;
 			}
 		}
 		this.threadTCPServer = null;
 		L.d("TCPServer Closed");
 	}
 
 	/**
 	 * Connects to the specified server.
 	 * 
 	 * @param ip
 	 *            The IP of the server
 	 * @param port
 	 *            The port of the server
 	 * @return The player id and the team id assigned by the server as a string
 	 *         like 7|3
 	 * @throws Exception
 	 */
 	public String serverTCPConnect(String ip, int port) throws Exception {
 
 		// Disconnect from previous server (just in case)
 		serverTCPDisconnect();
 
 		this.threadTCPClient = new TCPConnection(ip, port);
 		this.threadTCPClient.setName("TCP client " + CtrlDomain.getInstance().getPlayerName());
 		// The first thing we send is the player name
 		this.threadTCPClient.out(CtrlDomain.getInstance().getPlayerName());
 		// The first thing we receive is the player id
 		String player_assigned_info = this.threadTCPClient.in();
 		this.threadTCPClient.start();
 
 		return player_assigned_info;
 	}
 
 	public void serverTCPDisconnect() {
 		L.d("Disconnecting TCP");
 		if (this.threadTCPClient != null && this.threadTCPClient.isAlive()) {
 			this.threadTCPClient.close();
 			while (this.threadTCPClient.isAlive()) {
 				;
 			}
 		}
 		this.threadTCPClient = null;
 	}
 
 	public void serverTCPDisconnectClients() {
 
 		L.d("Starting disconnection");
 		// Close all the remaining connections
 		for (Player p : players) {
 			L.d("inside for");
 			p.close();
 		}
 		
 		L.d("Closed all sockets, clearing...");
 
 		players.clear();
 
 		L.d("Disconnection ended");
 	}
 
 	/*
 	 * 
 	 * 
 	 * 
 	 * SERVER UDP
 	 * 
 	 * 
 	 * 
 	 */
 
 	public void serverUDPStart() {
 		// If a previous server is already running, stop it
 		serverUDPStop();
 
 		this.threadUDPServer = new UDPServer(UDPServer.MODE_SERVER, null);
 		this.threadUDPServer.start();
 	}
 
 	public void serverUDPFind(Handler handler) {
 		// If a previous server is already running, stop it
 		serverUDPStop();
 
 		this.threadUDPServer = new UDPServer(UDPServer.MODE_CLIENT, handler);
 		this.threadUDPServer.start();
 		L.d("Its running!");
 		this.threadUDPServer.sendBroadcast("PING");
 		L.d("Sent PING");
 	}
 
 	public void serverUDPStop() {
 		if (this.threadUDPServer != null && this.threadUDPServer.isAlive()) {
 			this.threadUDPServer.close();
 			while (this.threadUDPServer.isAlive()) {
 				;
 			}
 		}
 		this.threadUDPServer = null;
 		L.d("UPDServer Closed");
 	}
 
 	/*
 	 * 
 	 * 
 	 * 
 	 * TCP COMMUNICATION / INFORMATION
 	 * 
 	 * 
 	 * 
 	 */
 
 	public void sendSignal(String string) throws Exception {
 		L.d("sending to server " + string);
 		threadTCPClient.out(string);
 	}
 	
 	public void sendSignal(Integer client, String string) throws Exception {
 		L.d("sending to player " + client + " " + string);
 		players.get(client).out(string);
 	}
 
 	public void sendSignals(String string) {
 		TCPServerSend threadTCPServerSend = new TCPServerSend(players, string);
 		threadTCPServerSend.start();
 	}
 
 	public void sendSignals(Vector<String> vector_string) {
 		TCPServerSend threadTCPServerSend = new TCPServerSend(players, vector_string);
 		threadTCPServerSend.start();
 	}
 
 	public void sendSignalsStartGame() {
 		TCPServerSend threadTCPServerSend = new TCPServerSend(players);
 		threadTCPServerSend.start();
 	}
 
 	public Vector<String> serverTCPGetConnectedPlayersName() {
 		Vector<String> result = new Vector<String>(players.size());
 
 		for (int i = 0; i < players.size(); i++) {
 			result.add(players.get(i).getName());
 		}
 
 		return result;
 	}
 
 	public Vector<Integer> serverTCPGetConnectedPlayersTeam() {
 		Vector<Integer> result = new Vector<Integer>(players.size());
 
 		for (int i = 0; i < players.size(); i++) {
 			result.add(players.get(i).getTeam());
 		}
 
 		return result;
 	}
 
 	public Integer serverTCPGetConnectedPlayersNum() {
 		return this.players.size();
 	}
 
 	public String serverTCPGetConnectedPlayer(int id) {
 		String result = players.get(id).getName();
 
 		return result;
 	}
 
 	public int serverTCPGetNumTeams() {
 		Vector<Integer> result = new Vector<Integer>();
 
 		for (int i = 0; i < players.size(); i++) {
 			if (!result.contains(players.get(i).getTeam())) {
 				result.add(players.get(i).getTeam());
 			}
 		}
 
 		return result.size();
 	}
 
 	public Vector<Integer> serverTCPGetPlayersTeam(Integer paramId) {
 
 		Integer teamId = serverTCPGetConnectedPlayersTeam().get(paramId);
 
 		Vector<Integer> result = new Vector<Integer>();
 
 		for (int i = 0; i < players.size(); i++) {
 			if (players.get(i).getTeam().equals(teamId)) {
 				result.add(players.get(i).getNumPlayer());
 			}
 		}
 
 		return result;
 	}
 
 	public int serverTCPGetPosFromId(Integer playerId) {
 		Integer result = null;
 
 		for (int i = 0; i < players.size(); i++) {
 			if (players.get(i).getNumPlayer().equals(playerId)) {
 				result = i;
 			}
 		}
 
 		return result;
 	}
 
 	public void sendUpdatedBoardServer() throws Exception {
 		Board b = CtrlGame.getInstance().getBoardToSend();
 		sendSignal("UPDATEDBOARD " + CtrlDomain.getInstance().serialize(b));
 	}
 
 	public void sendUpdatedBoardClients(Board b) {
 		sendSignals("UPDATEBOARD " + CtrlDomain.getInstance().serialize(b));
 	}
 
 	public void sendTurns(Integer serverTurnPointer) {
 		TCPServerSend threadTCPServerSend = new TCPServerSend(players, "UPDATEMYTURN 0");
 		threadTCPServerSend.start();
 		
 		// TODO: proper fix
 		try {
 			while (threadTCPServerSend.isAlive()) {
 			}
			Thread.sleep(500);
 		} catch (InterruptedException e) {
 		}
 
 		try {
 			sendSignal(serverTurnPointer, "UPDATEMYTURN " + CtrlDomain.getInstance().getServerNumTurns());
 		} catch (Exception e) {
 			CtrlDomain.getInstance().disconnectionDetected(players.get(serverTurnPointer).getConnection());
 		}
 	}
 
 	public void sendTurnFinished() throws Exception {
 		Board b = CtrlGame.getInstance().getBoardToSend();
 		sendSignal("TURNFINISHED " + CtrlDomain.getInstance().serialize(b));
 	}
 
 	public void sendShutdown() {
 		sendSignals("SHUTDOWN");
 	}
 
 	/**
 	 * ONLY call this function when a closed socket is found (exception),
 	 * because it will only remove the player from the game, it WON'T close the
 	 * connection.
 	 */
 	public void removePlayer(TCPConnection c) {
 
 		for (Player p : players) {
 			if (p.getConnection().equals(c)) {
 				this.players.remove(p);
 			}
 		}
 	}
 
 	/*
 	 * 
 	 * 
 	 * 
 	 * OTHER
 	 * 
 	 * 
 	 * 
 	 */
 
 	public void registerPlayer(int posPlayer, int chosenTeam) {
 		Integer pos = new Integer(posPlayer);
 		(teamPlayer.elementAt(chosenTeam)).add(pos);
 	}
 
 
 	public void nextPlayer() {
 		do {
 			pointerTeamPlayer++;
 			if (pointerTeamPlayer>=numTeams) {
 				pointerTeamPlayer = 0;
 			}
 		} while (teamPlayer.get(pointerTeamPlayer).isEmpty());
 		
 		Integer first = teamPlayer.get(pointerTeamPlayer).remove(0);
 		teamPlayer.get(pointerTeamPlayer).add(first);
 		
 		Integer firstPlayerPos = teamPlayer.get(pointerTeamPlayer).get(0);
 		try {
 			players.elementAt(firstPlayerPos).out("CONTINUE ");
 		} catch (Exception e) {
 			// TODO: finish game or try next player
 			e.printStackTrace();
 		}
 	}
 
 	/*
 	 * 
 	 * 
 	 * 
 	 * NETWORK UTILITIES
 	 * 
 	 * 
 	 * 
 	 */
 
 	public InetAddress getBroadcastAddress() throws IOException {
 		DhcpInfo dhcp = wifiManager.getDhcpInfo();
 
 		if (dhcp == null) {
 			Log.d("NET", "Could not get dhcp info");
 			return null;
 		}
 
 		int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
 		byte[] quads = new byte[4];
 		for (int k = 0; k < 4; k++)
 			quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);
 		return InetAddress.getByAddress(quads);
 	}
 
 	public InetAddress getLocalAddress() throws IOException {
 
 		for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
 			NetworkInterface intf = en.nextElement();
 			for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
 				InetAddress inetAddress = enumIpAddr.nextElement();
 				if (!inetAddress.isLoopbackAddress()) {
 					return inetAddress;
 				}
 			}
 		}
 
 		return null;
 	}
 
 	public void setWifiManager(WifiManager systemService) {
 		L.d("Wifi set");
 		this.wifiManager = systemService;
 	}
 
 
 }
