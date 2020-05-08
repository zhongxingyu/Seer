 package network;
 
 import java.io.IOException;
 import java.net.InetAddress;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.net.UnknownHostException;
 import java.util.concurrent.CopyOnWriteArrayList;
 
 import level.Loader;
 import enums.GameServerStatus;
 import game.Debug;
 
 public class GameServer implements Runnable {
 
 	public final static int GAME_PORT = 4242;
 	private String map;
 	private int required_player;
 	private CopyOnWriteArrayList<ClientOnServer> connected_players;
 	private ServerSocket welcomeSocket;
 	private GameServerStatus status;
 	private CopyOnWriteArrayList<Input> queue;
 
 	public GameServer(String map) throws IOException {
 		this.map = map;
 		this.required_player = new Loader().parseForMultiplayer(map);
 		this.connected_players = new CopyOnWriteArrayList<ClientOnServer>();
 		this.welcomeSocket = new ServerSocket(GameServer.GAME_PORT);
 		this.status = GameServerStatus.WAITING;
 		this.queue = new CopyOnWriteArrayList<Input>();
 	}
 
 	@Override
 	public void run() {
 		Debug.log(Debug.DEBUG, "Game Server starting");
 		try {
 			Debug.log(Debug.DEBUG, "Server IP: " + InetAddress.getLocalHost().getHostAddress());
 		} catch (UnknownHostException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 		Debug.log(Debug.DEBUG, "waiting for clients");
 		while (true) {
 			try {
 				if (this.connected_players.size() < this.required_player) {
 					Socket connectionSocket = this.welcomeSocket.accept();
 					ClientOnServer c = new ClientOnServer(connectionSocket, this.connected_players.size(), this.queue);
 					c.start();
 					this.connected_players.add(c);
 					Debug.log(Debug.DEBUG, "New Client connected (" + connectionSocket.getInetAddress() + ")");
 				}
 
 				if ((this.connected_players.size() == this.required_player)
 						&& (this.status == GameServerStatus.WAITING)) {
 					this.initGame();
 				}
 
 				if (this.status == GameServerStatus.RUNNING) {
 					this.sendInputs();
 				}
 
 				try {
 					Thread.sleep(20);
 				} catch (InterruptedException e) {
 					// TODO Auto-generated catch block
 					// e.printStackTrace();
 				}
 
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	private void initGame() {
 		this.status = GameServerStatus.STARTING;
 		Debug.log(Debug.DEBUG, "Match starting");
 
 		for (ClientOnServer c : this.connected_players) {
 			try {
 				c.sendMap(this.map);
 			} catch (IOException e) {
 				Debug.log(Debug.ERROR, "Can't send Data to player, maybe disconnected");
 			}
 		}
 		this.status = GameServerStatus.RUNNING;
 	}
 
 	public void sendInputs() {
 		if (this.queue.isEmpty() == false) {
 			for (Input in : this.queue) {
 				for (ClientOnServer c : this.connected_players) {
 					if (in.playerID != c.playernumber) {
 						try {
 							c.sendInput(in);
 						} catch (IOException e) {
 							Debug.log(Debug.ERROR, "Can't send Data to player, maybe disconnected");
 						}
 					}
 				}
 			}
			this.queue.clear();
 		}
 	}
 }
