 package Net;
 
 import java.io.BufferedReader;
 import java.io.EOFException;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.net.InetAddress;
 import java.net.Socket;
 import java.net.SocketException;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.StringTokenizer;
 
 import Client.Enemy;
 import Client.GamePanel;
 import Client.PlayerMP;
 import Net.packets.Packet00Login;
 import Net.packets.Packet01Disconnect;
 import Net.packets.Packet02Move;
 import Net.packets.Packet03Map;
 import Net.packets.Packet04Enemy;
 import Net.packets.Packet06GetC;
 import Net.packets.Packet07Finish;
 
 
 /***************************************
  * Clienten Klasse fr den Multiplayer *
  ***************************************/
 public class Client implements Runnable {
 	public List<Client> clients;
 	private Socket socket;
 	private ObjectOutputStream output;
 	private ObjectInputStream input;
 	private boolean isServer;
 	static GamePanel game;
 	static int[][] leveldata;
 	public String userName;
 	public static boolean tick = false;
 	
 	
 	
 	private List<Client> copyClients() {
 		synchronized (clients) {
 			return new ArrayList<>(clients);
 		}
 	}
 
 	private void send(Object msg) {
 		try {
 			getOutput().writeObject(msg);
 			getOutput().flush();
 		} catch (Exception e) {
 			try {
 				getSocket().close();
 			} catch (Exception e2) {
 			}
 		}
 	}
 
 	private void broadcast(Object msg, boolean self) {
 		for (Client client : copyClients()) {
 			if (client != this || self)
 				try {
 					client.getOutput().writeObject(msg);
 					client.getOutput().flush();
 				} catch (Exception e) {
 					try {
 						client.getSocket().close();
 					} catch (IOException e1) {
 					}
 				}
 		}
 	}
 
 	protected void onRecv(Object message) throws Exception {
 		if (isServer) {
 			onServerRecv(copyClients(), message);
 		} else {
 			onClientRecv(message);
 		}
 	}
 
 	protected void onClientClosed() {
 
 	}
 
 	protected void onServerClientClosed() {
 		Packet01Disconnect o = new Packet01Disconnect(userName);
 
 		System.out.println("SERVER < [" + socket.getInetAddress() + ":"
 				+ socket.getPort() + "] "
 				+ ((Packet01Disconnect) o).getUsername() + " has left...");
 
 		Server.player.getAndDecrement();
 
 		Server.connectedPlayers.remove(getPlayerMPIndex(o.getUsername()));
 		broadcast(o, false);
 	}
 
 	protected void onServerRecv(List<Client> clients, Object o)
 			throws Exception {
 
 		if (o instanceof Packet00Login) {
 			System.out.println("[" + socket.getInetAddress() + ":"
 					+ socket.getPort() + "] "
 					+ ((Packet00Login) o).getUsername() + " has connected...");
 			getCoor(o);
 			System.out.println(((Packet00Login) o).getX() + ","
 					+ ((Packet00Login) o).getY());
 			PlayerMP player2 = new PlayerMP(game.pl2,
 					((Packet00Login) o).getX(), ((Packet00Login) o).getY(),
 					((Packet00Login) o).getUsername(), socket.getInetAddress(),
 					socket.getPort(), 100, game, ((Packet00Login)o).getId());
 
 			addConnection(player2, o);
 			//addEnemys();
 
 		} else if (o instanceof Packet02Move) {
 			this.handleMove(((Packet02Move) o), true);
 		} else if (o instanceof Packet03Map) {
 			sendLevel(true);
 		} else if(o instanceof Packet06GetC){
 			getCoor2(o);
 			broadcast(o, true);
 		}
 		
 	}
 
 	protected void onClientRecv(Object o) throws Exception {
 
 		if (o instanceof Packet00Login) {
 
 			if (!((Packet00Login) o).getUsername().equals(userName)) {
 				handleLogin((Packet00Login) o, socket.getInetAddress(),
 						socket.getPort());
 			}
 			handleinit((Packet00Login) o);
 			System.out.println("LOGIN erhalten");
 		} else if (o instanceof Packet01Disconnect) {
 			System.out.println("CLIENT < [" + socket.getInetAddress() + ":"
 					+ socket.getLocalPort() + "] "
 					+ ((Packet01Disconnect) o).getUsername()
 					+ " has left the world...");
 			game.removePlayerMP(((Packet01Disconnect) o).getUsername());
 		} else if (o instanceof Packet02Move) {
 			handleMove((Packet02Move) o, false);
 		} else if (o instanceof Packet03Map) {
 			game.leveldata = ((Packet03Map) o).getLevel();
 			if(!((Packet03Map)o).getBool()){
 				game.Clear(true);
 				Packet06GetC p = new Packet06GetC(game.player1.x, game.player1.y, game.player1.id, game.player1.getUsername());
 				send(p);
 				game.Draw();
 			}
 		} else if (o instanceof Packet04Enemy){
 			handleEnemy((Packet04Enemy)o);
 		} else if(o instanceof Packet06GetC){
 			Packet02Move p = new Packet02Move(((Packet06GetC)o).getUsername(), ((Packet06GetC)o).getX(), ((Packet06GetC)o).getY(), 1);
 			handleMove(p, false);
 		}else if(o instanceof Packet07Finish){
 			game.finish(((Packet07Finish)o).getUsername());
 		}
 
 	}
 
 	@Override
 	public void run() {
 		try {
 			while (true) {
 				Object object = input.readObject();
 				onRecv(object);
 			}
 		} catch (SocketException | EOFException e) {
 		} catch (Exception e) {
 			e.printStackTrace();
 		} finally {
 			if (clients != null) {
 				synchronized (clients) {
 					clients.remove(this);
 				}
 			}
 			if (isServer)
 				onServerClientClosed();
 			else
 				onClientClosed();
 		}
 	}
 
 	public ObjectOutputStream getOutput() {
 		return output;
 	}
 
 	public boolean isServer() {
 		return isServer;
 	}
 
 	public Socket getSocket() {
 		return socket;
 	}
 
 	@SuppressWarnings("static-access")
 	public Client(InetAddress addr, int port, GamePanel p)
 			throws UnknownHostException, IOException {
 		this(new Socket(addr, port), false);
 		this.game = p;
 		new Thread(this).start();
 	}
 
 	Client(Socket socket, boolean isServer) throws IOException {
 		this.socket = socket;
 		this.isServer = isServer;
 		socket.setTcpNoDelay(true);
 		output = new ObjectOutputStream(socket.getOutputStream());
 		input = new ObjectInputStream(socket.getInputStream());
 	}
 
 	// SERVER TEIL
 	
 	private void getCoor(Object o) {
 		for (int row = 0; row < leveldata.length; row++) {
 			for (int col = 0; col < leveldata[row].length; col++) {
 				if (Server.player.get() == 1) {
 					if (leveldata[row][col] == 2) {
 						((Packet00Login) o).setX(col * 16);
 						((Packet00Login) o).setY(row * 16);
 						((Packet00Login)o).setID(Server.player.get());
 					}
 				} else if (Server.player.get() == 2) {
 					if (leveldata[row][col] == 22) {
 						((Packet00Login) o).setX(col * 16);
 						((Packet00Login) o).setY(row * 16);
 						((Packet00Login)o).setID(Server.player.get());
 					}
 				}
 			}
 		}
 	}
 	
 	private void getCoor2(Object o){
 		for (int row = 0; row < leveldata.length; row++) {
 			for (int col = 0; col < leveldata[row].length; col++) {
 				if (((Packet06GetC)o).getId()==1) {
 					if (leveldata[row][col] == 2) {
 						((Packet06GetC) o).setX(col * 16);
 						((Packet06GetC) o).setY((row-1) * 16);
 					}
 				} else if (((Packet06GetC)o).getId()==2) {
 					if (leveldata[row][col] == 22) {
 						((Packet06GetC) o).setX(col * 16);
 						((Packet06GetC) o).setY((row-1) * 16);
 					}
 				}
 			}
 		}
 	}
 
 	/*private void addEnemys(){		
 		for(int index = 0;index < Server.spawnedEnemys.size(); index++){
 			Enemy e = Server.spawnedEnemys.get(index);
 			Packet04Enemy o = new Packet04Enemy(e.getX(), e.getY(), e.getType(), e.getID(), e.getStrength(), e.getWeakness());
 			send(o);
 		}
 	}*/
 
 	/*private void syncEnemy(){
 		if(Server.connectedPlayers.size() !=0){
 			for(Enemy e : Server.spawnedEnemys){
 				Packet05EnemyMove em = new Packet05EnemyMove(e.getX(), e.getY(), e.getID());
 				send(em);
 			}
 		}
 	}*/
 	
 	
 	private void addConnection(PlayerMP player, Object o) {
 		boolean alreadyConnected = false;
 		send(o);
 	
 		for (PlayerMP p : Server.connectedPlayers) {
 			if (player.getUsername().equalsIgnoreCase(p.getUsername())) {
 				if (p.ipAddress == null) {
 					p.ipAddress = player.ipAddress;
 				}
 				if (p.port == -1) {
 					p.port = player.port;
 				}
 				alreadyConnected = true;
 
 			} else {
 				userName = player.getUsername();
 
 				// relay to the current connected player that there is a new
 				// player
 				broadcast(o, false);
 				// relay to the new player that the currently connect player
 				// exists
 				o = new Packet00Login(p.getUsername(), p.x, p.y, 0);
 				send(o);
 			}
 		}
 		if (!alreadyConnected) {
 			Server.connectedPlayers.add(player);
 		}
 	}
 
 	private PlayerMP getPlayerMP(String username) {
 		for (PlayerMP player : Server.connectedPlayers) {
 			if (player.getUsername().equals(username)) {
 				return player;
 			}
 		}
 		return null;
 	}
 
 	private int getPlayerMPIndex(String username) {
 		int index = 0;
 		for (PlayerMP player : Server.connectedPlayers) {
 			if (player.getUsername().equals(username)) {
 				break;
 			}
 			index++;
 		}
 		return index;
 	}
 
 	@SuppressWarnings("static-access")
 	private void handleMove(Packet02Move packet, boolean isServer) {
 		if (isServer) {
 			if (getPlayerMP(packet.getUsername()) != null) {
 				int index = getPlayerMPIndex(packet.getUsername());
 				PlayerMP player = Server.connectedPlayers.get(index);
 				player.setMovingDir(packet.getMovingDir());
 				int movingDir = packet.getMovingDir();
 				int moveY = (int) (packet.y);
 				int moveX = (int) (packet.x) / 16;
 				moveY = (moveY + 16) / 16;
 
 				if (movingDir == 1) {
 					int oldY = moveY;
 
 					if (leveldata[moveY - 1][moveX] == 1
 							|| leveldata[moveY - 1][moveX] == 11
 							|| leveldata[moveY - 1][moveX] == 16) {
 						moveY = oldY;
 					} else if (leveldata[moveY - 1][moveX] == 4
 							|| leveldata[moveY - 1][moveX] == 30
 							|| leveldata[moveY - 1][moveX] == 31
 							|| leveldata[moveY - 1][moveX] == 99) {
 						moveY = oldY;
 					} else if (leveldata[moveY - 1][moveX] == 9 && Server.spawnedEnemys.size() != 0 || (leveldata[moveY - 1][moveX] == 92 && Server.spawnedEnemys.size() != 0)) {
 						moveY = oldY;
 					} else
 						moveY--;
 				} else if (movingDir == 3) {
 					int oldY = moveY;
 
 					if (leveldata[moveY + 1][moveX] == 1
 							|| leveldata[moveY + 1][moveX] == 11
 							|| leveldata[moveY + 1][moveX] == 16) {
 						moveY = oldY;
 					} else if (leveldata[moveY + 1][moveX] == 4
 							|| leveldata[moveY + 1][moveX] == 30
 							|| leveldata[moveY + 1][moveX] == 31
 							|| leveldata[moveY + 1][moveX] == 99) {
 						moveY = oldY;
 					} else if (leveldata[moveY + 1][moveX] == 9 && Server.spawnedEnemys.size() != 0 || (leveldata[moveY + 1][moveX] == 92 && Server.spawnedEnemys.size() != 0)) {
 						moveY = oldY;
 					} else
 						moveY++;
 				} else if (movingDir == 2) {
 
 					int oldX = moveX;
 
 					if (leveldata[moveY][moveX - 1] == 1
 							|| leveldata[moveY][moveX - 1] == 11
 							|| leveldata[moveY][moveX - 1] == 16) {
 						moveX = oldX;
 					} else if (leveldata[moveY][moveX - 1] == 4
 							|| leveldata[moveY][moveX - 1] == 30
 							|| leveldata[moveY][moveX - 1] == 31
 							|| leveldata[moveY][moveX - 1] == 99) {
 						moveX = oldX;
 					} else if (leveldata[moveY][moveX - 1] == 9 && Server.spawnedEnemys.size() != 0 ||(leveldata[moveY][moveX - 1] == 9 && Server.spawnedEnemys.size() != 0)) {
 						moveX = oldX;
 					} else
 						moveX--;
 				} else if (movingDir == 4) {
 
 					int oldX = moveX;
 
 					if (leveldata[moveY][moveX + 1] == 1
 							|| leveldata[moveY][moveX + 1] == 11
 							|| leveldata[moveY][moveX + 1] == 16) {
 						moveX = oldX;
 					} else if (leveldata[moveY][moveX + 1] == 4
 							|| leveldata[moveY][moveX + 1] == 30
 							|| leveldata[moveY][moveX + 1] == 31
 							|| leveldata[moveY][moveX + 1] == 99) {
 						moveX = oldX;
 					} else if (leveldata[moveY][moveX + 1] == 9 && Server.spawnedEnemys.size() != 0 || (leveldata[moveY][moveX - 1] == 92 && Server.spawnedEnemys.size() != 0)) {
 						moveX = oldX;
 					} else
 						moveX++;
 				}
 
 				player.x = packet.x = moveX * 16;
 				player.y = packet.y = (moveY * 16) - 16;
 				for(Enemy e : Server.spawnedEnemys){
 					e.getCoor(moveX*16, moveY*16);
 				}
 				broadcast(packet, true);
 			}
 		} else {
 			game.movePlayer(packet.getUsername(), packet.getX(), packet.getY(),
 					packet.getMovingDir());
 		}
 	}
 
 	public void sendLevel(boolean init) {
 		if(init){
 			Packet03Map p = new Packet03Map(leveldata, init);
 			send(p);
 		}else
 		{
 			Packet03Map p = new Packet03Map(leveldata, "Test", init);
 			send(p);
 		}
 		
 	}
 
 	
 	/******************************************
 	 * Funktion zum auslesen der Leveldateien *
 	 ******************************************/
 	@SuppressWarnings("resource")
 	public static void read() {
 		
 		try {
 			String sTemp;
 			String rest = null;
 			int i, j;
 			leveldata = new int[15][15];
 			switch (ServerLogic.lvl.get()) {
 			case 1:
 				rest = "lvl1.level";
 				break;
 			case 2:
 				rest = "lvl2.level";
 				break;
 			case 3:
 				rest = "lvl3.level";
 				break;
 			case 4:
 				rest = "lvl4.level";
 				break;
 			case 5:
 				rest = "lvl5.level";
 				break;
 			case 6:
 				rest = "lvl6.level";
 				break;
 			case 7:
 				rest = "lvl7.level";
 				break;
 			case 8:
 				rest = "lvl8.level";
 				break;
 			case 9:
 				rest = "lvl9.level";
 				break;
 			}
 			leveldata = new int[15][15];
 			BufferedReader oReader = new BufferedReader(new InputStreamReader(
 					new FileInputStream(new File("res/lvl/MP/"+rest)))); // Zeile
 																				// fr
 			// Zeile
 			// einlesen
 
 			i = 0;
 
 			while ((sTemp = oReader.readLine()) != null) {
 
 				// Zeile in Einzelteile zerlegen (wir trennen durch ;
 
 				java.util.StringTokenizer stWerte = new StringTokenizer(sTemp,
 						";");
 
 				j = 0;
 
 				// Nun eintragen in den Array. Es wird nicht berprft, ob
 				// die Grenzen berschritten werden!
 
 				while (stWerte.hasMoreTokens()) {
 
 					leveldata[i][j] = Integer.parseInt(stWerte.nextToken());
 					j++;
 				}
 				i++;
 			}
 		} catch (FileNotFoundException e) {
 
 			e.printStackTrace(); // Fehler ausdrucken
 
 		} catch (IOException e) {
 
 			e.printStackTrace(); // Fehler ausdrucken
 
 		}
 	}
 	
 	/***********************************************
 	 * Server ruft beim Starten die Funktion auf um*
 	 * Gegner zu erstellen						   *
 	 ***********************************************/
 	public static void SpawnEnemy() {	
 		for (int row = 0; row < leveldata.length; row++) {
 			for (int col = 0; col < leveldata[row].length; col++) {
 				if (leveldata[row][col] == 3) {
 					int rn = (int)(Math.random()*5);
 					int rn2 = (int)(Math.random()*999);
 					if(rn >= 0 && rn <2){
 						Enemy ene = new Enemy(GamePanel.enemy, 16 * col,	16 * row, 100, game, 1, rn2, "melee", "arcane");
 						Server.spawnedEnemys.add(ene);
 					}else if(rn >=2 && rn<=4){
 						Enemy sli = new Enemy(GamePanel.sl, 16 * col, 16 * row, 100, game, 2, rn2, "poison", "melee");
 						Server.spawnedEnemys.add(sli);
 					}
 					
 				}
 
 			}
 		}
 		if( Server.spawnedEnemys.size() == 0){
 			System.out.println("Keine Gegner gespawnt");
 		}
 	}
 	
 	
 
 	// CLIENTEN TEIL
 	private void handleLogin(Packet00Login packet, InetAddress address, int port) {
 		System.out.println("[" + address.getHostAddress() + ":" + port + "] "
 				+ packet.getUsername() + " has joined the game...");
 		PlayerMP player2 = new PlayerMP(game.pl2,
 				((Packet00Login) packet).getX(),
 				((Packet00Login) packet).getY() - 16, packet.getUsername(),
 				address, port, 100, game, ((Packet00Login) packet).getId());
 		game.addPlayerMP(player2);
 	}
 
 	private void handleinit(Object o) {
 		game.setStart(((Packet00Login) o).getUsername(),
 				((Packet00Login) o).getX(), ((Packet00Login) o).getY() - 16, ((Packet00Login)o).getId());
 	}
 	
 	@SuppressWarnings("static-access")
 	private void handleEnemy(Packet04Enemy o){
 		if(o.getType()==1){
 			Enemy ghost = new Enemy(game.enemy, o.getX(), o.getY(),100, game, true, o.getID(), o.getStrength(), o.getWeakness());
 			game.addEnemy(ghost);
 		}else if(o.getType()==2){
 			Enemy slime = new Enemy(game.sl, o.getX(), o.getY(),100, game, true, o.getID(), o.getStrength(), o.getWeakness());
 			game.addEnemy(slime);
 		}
 	}
 
 
 }
