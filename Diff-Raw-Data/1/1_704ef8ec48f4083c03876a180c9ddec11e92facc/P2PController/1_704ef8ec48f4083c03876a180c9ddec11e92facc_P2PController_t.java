 package tr2.server.sync.controller;
 
 import java.io.IOException;
 import java.util.HashMap;
 
 import org.json.simple.JSONObject;
 import org.json.simple.parser.JSONParser;
 import org.json.simple.parser.ParseException;
 
 import tr2.server.common.entity.User;
 import tr2.server.common.multicast.Multicast;
 import tr2.server.common.multicast.MulticastController;
 import tr2.server.common.series.protocol.Messages;
 import tr2.server.common.tcp.ConnectionsManager;
 import tr2.server.common.tcp.TCPController;
 import tr2.server.common.util.JSONHelper;
 import tr2.server.common.util.NetworkConstants;
 import tr2.server.http.UserDB;
 import tr2.server.interval.data.Data;
 import tr2.server.sync.data.ServerData;
 
 public class P2PController implements MulticastController, TCPController,
 TimerController {
 
 	private ServerData serverData;
 
 	private Data data;
 
 	private Multicast multicast;
 
 	private ConnectionsManager p2p;
 
 	private String label = "[P2P CONTROLLER]";
 
 	private static final int timerWaitConnection = 0;
 	private static final int timerSendUpdates = 1;
 
 	public P2PController(Data data, int serversPort) throws IOException {
 		this.data = data;
 		p2p = new ConnectionsManager(this, serversPort);
 		serverData = new ServerData();
 	}
 
 	public void startMulticast() {
 		multicast = new Multicast(this,
 				NetworkConstants.CLIENT_MULTICAST_ADDRESS,
 				NetworkConstants.CLIENT_MULTICAST_PORT, "[MULTICAST MANAGER]");
 	}
 
 	public void start() {
 		System.out.println(label + " Initializing server");
 		System.out.println(label + " Searching for manager...");
 
 		serverData.setPassive();
 
 		startMulticast();
 
 		new Timer(this, NetworkConstants.PERIODIC_TIME * 5, false,
 				timerWaitConnection);
 	}
 
 	private void setActive() {
 		serverData.setActive();
 
 		startMulticast();
 
 		new Timer(this, NetworkConstants.SYNC_TIME, true, timerSendUpdates);
 
 		multicast.startSpeaker(NetworkConstants.HELLO,
 				NetworkConstants.PERIODIC_TIME);
 	}
 
 	private void connectAndAddServer(String address) {
 		if (serverData.addServerInfo(address)) {
 			// tries to connect to server
 			if (p2p.requestConnection(address)) {
 				if (p2p.getNumberOfConnections() == 1 && !serverData.isActive()) {
 					// it is the manager
 					serverData.setServerActive(address);
 				}
 			} else {
 				// if the connection was unsuccessful
 				serverData.removeServerInfo(address);
 			}
 		}
 	}
 
 	private class WriterThread implements Runnable {		
 		public void run() {
 			System.out.println(label + " Starting to write to buffer...");
 			// TODO send users
 			sendUsers();
 			sendCalculatedIntervals();
 			sendPendingIntervals();
 		}
 	}
 
 	public void notifyTimeIsOver(int type) throws IOException {
 		if (type == timerWaitConnection) {
 			// when time is over
 			// if no connection has been made
 			if (p2p.getNumberOfConnections() == 0) {
 				// delegates himself the "manager"
 				setActive();
 				System.out.println(label + " I am the new manager");
 			} else {
 				System.out.println(label + " There's a manager");
 			}
 		} else if (type == timerSendUpdates) {
 			if (p2p.getNumberOfConnections() > 0) {
 				System.out.println(label + " Creating Thread to send updates to servers");
 				Thread thread = new Thread(new WriterThread());
 				thread.start();
 			}
 		}
 	}
 
 	public void notifyMessageReceived(String message, String address) {
 		connectAndAddServer(address);
 	}
 
 	public void sendServersInfoUpdate() {
 		String message = serverData.serversInfoToString();
 		try {
 			p2p.sendToAllConnections(NetworkConstants.SERVER_UPDATE_PREFIX
 					+ message);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void sendCalculatedIntervals() {
		
 		String message = data.intervalsToString(Data.TYPE_INTERVALS);
 		try {
 			p2p.sendToAllConnections(NetworkConstants.INTERVALS_UPDATE_PREFIX
 					+ message);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public void sendPendingIntervals() {
 		String message = data.intervalsToString(Data.TYPE_PENDING_INTERVALS);
 		try {
 			p2p.sendToAllConnections(NetworkConstants.PENDING_INTERVALS_UPDATE_PREFIX + message);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void sendUsers() {
 		if (serverData.isActive()) {
 			// TODO
 			System.out.println(label + " Sending Users...");
 			// pega data do servidor http
 			HashMap<String, User> users = UserDB.getUsers();
 			HashMap<String, String> usersJSON = new HashMap<String, String>();
 	
 			for (String key : users.keySet()) {
 				User u = users.get(key);
 				usersJSON.put(key, u.toJSON());
 			}
 			
 			JSONObject obj = new JSONObject(usersJSON);
 			String json = obj.toJSONString();
 			try {
 				p2p.sendToAllConnections(NetworkConstants.USERS_UPDATE_PREFIX + json);
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	public void notifyDisconnected(String address) {
 		// disconnected after being connected
 		// the disconnected server can be the manager
 		if (!serverData.removeServerInfo(address)) {
 			System.out.println(label + " Manager Dropped");
 			System.out.println(label + " Initializing Recovery Routine...");
 			try {
 				Thread.sleep((long) (Math.random() * (NetworkConstants.TCP_TIMEOUT * 20)));
 				if (serverData.getActiveIndex() == -1) {
 					// delegates this server the manager
 					setActive();
 					p2p.sendToAllConnections(NetworkConstants.MANAGER_STATEMENT);
 				}
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 
 		}
 	}
 
 	// connection made notification
 	// adds it to data and sends update to all connected
 	public void notifyConnected(String address) {
 		// is connected
 		serverData.addServerInfo(address);
 		if (serverData.isActive()) {
 			sendUsers();
 			sendPendingIntervals();
 			sendCalculatedIntervals();
 			sendServersInfoUpdate();
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	public void notifyMessageReceived(String message, String localAddress,
 			String address) {
 		
 		if (message.startsWith(NetworkConstants.PENDING_INTERVALS_UPDATE_PREFIX)) {
 			message = message.replace(NetworkConstants.PENDING_INTERVALS_UPDATE_PREFIX, "");
 			
 			data.stringToIntervals(message, Data.TYPE_PENDING_INTERVALS);
 			
 		} else if (message.startsWith(NetworkConstants.USERS_UPDATE_PREFIX)) {
 			message = message.replace(NetworkConstants.USERS_UPDATE_PREFIX, "");
 
 			System.out.println(label + " Receiving Users...");
 			JSONParser parser = new JSONParser();
 			HashMap<String, User> users = null;
 			try {
 				HashMap<String, String> usersJSON = (HashMap<String, String>) parser.parse(message);
 				users = new HashMap<String, User>();
 				for(String key : usersJSON.keySet()) {
 					String userJSON = usersJSON.get(key);
 					User u = JSONHelper.fromJSON(userJSON, User.class);
 					users.put(key, u);
 				}
 			} catch (ParseException e) {
 				e.printStackTrace();
 			}
 			
 			// TODO
 			// invoke static method to manipulate https server list
 			if (users != null)
 				UserDB.setUsers(users);
 
 		} else if (message.startsWith(NetworkConstants.INTERVALS_UPDATE_PREFIX)) {
 			message = message.replace(NetworkConstants.INTERVALS_UPDATE_PREFIX,
 					"");
 			data.stringToIntervals(message, Data.TYPE_INTERVALS);
 
 		} else if (message.startsWith(NetworkConstants.SERVER_UPDATE_PREFIX)) {
 			String servers[];
 			// it's a serversInfo update message
 			message = message
 					.replace(NetworkConstants.SERVER_UPDATE_PREFIX, ""); // erases
 			// "header"
 
 			servers = message.split(Messages.SEPARATOR);
 
 			for (int i = 0; i < servers.length; i++) {
 				if (!servers[i].equals(localAddress)) {
 					connectAndAddServer(servers[i]);
 				}
 			}
 		}
 		// not used...
 		// else if (message.startsWith(NetworkConstants.MANAGER_PREFIX)) {
 		// if (message.equals(NetworkConstants.MANAGER_REQUEST)) {
 		// if (serverData.isActive()) {
 		// // send manager_response
 		// }
 		// } else if (message.equals(NetworkConstants.MANAGER_RESPONSE)) {
 		// // puts sender as manager
 		// } else if (message.equals(NetworkConstants.MANAGER_STATEMENT)) {
 		// // puts sender as manager
 		// }
 		// }
 	}
 }
