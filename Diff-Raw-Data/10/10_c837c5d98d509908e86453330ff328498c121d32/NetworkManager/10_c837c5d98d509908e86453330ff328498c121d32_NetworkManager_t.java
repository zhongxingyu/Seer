 package game;
 
 import java.io.BufferedReader;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.Socket;
 import java.util.ArrayList;
 import java.util.concurrent.CopyOnWriteArrayList;
 
 import level.Loader;
 import level.Point;
 import network.Input;
 import network.Server;
 import entities.Bomb;
 import entities.Player;
 import enums.Gamemode;
 import enums.NetworkInputType;
 
 public class NetworkManager extends Thread {
 
 	private Server server;
 	private Socket socket;
 	private BufferedReader inStream;
 	private DataOutputStream outStream;
 	private CopyOnWriteArrayList<Input> out_queue, in_queue;
 	public int playerID;
 	public CopyOnWriteArrayList<NetworkPlayerKeys> networkplayer;
 
 	public NetworkManager(Server server) {
 		this.server = server;
 		this.out_queue = new CopyOnWriteArrayList<Input>();
 		this.in_queue = new CopyOnWriteArrayList<Input>();
 		this.networkplayer = new CopyOnWriteArrayList<NetworkPlayerKeys>();
 	}
 
 	public boolean connect() {
 		try {
 			this.socket = new Socket(this.server.host, this.server.port);
 		} catch (IOException e) {
 			Debug.log(Debug.ERROR, "Can't connect to gameserver");
 			return false;
 		}
 		try {
 			this.inStream = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
 			this.outStream = new DataOutputStream(this.socket.getOutputStream());
 		} catch (IOException e) {
 			Debug.log(Debug.ERROR, "Can't get input/output stream");
 		}
 		return true;
 	}
 
 	@Override
 	public void run() {
 		Game.gamemode = Gamemode.NETWORK;
 		while (true) {
 			if (this.out_queue.isEmpty() == false) {
 				this.sendCommand();
 			}
 
 			try {
 				String command = this.inStream.readLine();
 				Input in = null;
 				if (command.startsWith("input:")) {
 					in = new Input();
 					command = command.replace("input:", "").replace(";", "");
 					String[] parts = command.split(",");
 					in.playerID = Integer.valueOf(parts[0]);
 					in.type = NetworkInputType.valueOf(parts[1]);
 					if ((in.type == NetworkInputType.PLAYER) || (in.type == NetworkInputType.BOMB)) {
 						in.x = Integer.valueOf(parts[2]);
 						in.y = Integer.valueOf(parts[3]);
 					}
 
 					if (in.type == NetworkInputType.BOMB) {
 						Game.entities.add(new Bomb(in.x, in.y, in.playerID));
 						Debug.log(Debug.VERBOSE, "Bomb received");
 					} else if (in.type == NetworkInputType.PLAYER) {
 						Player p = (Player) Game.players.get(in.playerID);
 						p.setPosition(in.x, in.y);
 						Debug.log(Debug.VERBOSE, "Got new player position");
 					}
 				} else if (command.startsWith("me:")) {
 					this.playerID = Integer.valueOf(command.replace("me:", "").replace(";", ""));
 					Debug.log(Debug.VERBOSE, "PlayerID: " + this.playerID);
 				} else if (command.startsWith("m:")) {
 					String mapname = command.replace("m:", "").replace(";", "");
 					// Game.key_settings = new ArrayList<KeySettings>();
 					Game.getInstance().init(mapname);
 					ArrayList<Point> spawns = new Loader().getSpawnPoints(mapname);
					for (int i = 0; i < spawns.size(); i++) {
 						Point po = spawns.get(i);
 						if (i == this.playerID) {
 							/*
 							 * KeySettings s1 = new KeySettings(); s1.bomb =
 							 * Game.keys.bomb; s1.left = Game.keys.left;
 							 * s1.right = Game.keys.right; s1.up = Game.keys.up;
 							 * s1.down = Game.keys.down;
 							 * Game.key_settings.add(s1);
 							 */
 
 							Player p = new Player(po.x * Game.BLOCK_SIZE, po.y * Game.BLOCK_SIZE);
 							KeySettings keys = Game.getKeySettings(0);
 							p.setKeys(keys);
 							Game.players.add(p);
 						} else {
 							NetworkPlayerKeys keys = new NetworkPlayerKeys(i);
 							Player p = new Player(po.x * Game.BLOCK_SIZE, po.y * Game.BLOCK_SIZE);
 							p.setKeys(keys);
 							Game.players.add(p);
 						}
 					}
 				}
 
 				if (in != null) {
 					this.in_queue.add(in);
 				}
 
 				try {
 					Thread.sleep(20);
 				} catch (InterruptedException e) {
 					// TODO Auto-generated catch block
 					// e.printStackTrace();
 				}
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				// e.printStackTrace();
 			} catch (Exception e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 	}
 
 	private void sendCommand() {
 		for (Input in : this.out_queue) {
 			try {
 				this.outStream.write(("input:" + this.playerID + "," + in.type + "," + in.x + "," + in.y + ";\n")
 						.getBytes());
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 
 	}
 
 	public void send(Input in) {
 		this.out_queue.add(in);
 	}
 }
