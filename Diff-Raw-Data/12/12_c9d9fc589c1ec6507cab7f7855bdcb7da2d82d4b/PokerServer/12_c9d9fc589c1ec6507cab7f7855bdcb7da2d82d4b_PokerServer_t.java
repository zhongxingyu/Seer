 /*
  *	A lot of this code is taken from the wePoker, written by the Ambientalk team 
  */
 
 package be.infogroep.justpoker;
 
 import java.io.IOException;
 import java.util.Iterator;
 import java.util.UUID;
 import java.util.concurrent.ConcurrentSkipListMap;
 
 import android.util.Log;
 import be.infogroep.justpoker.GameElements.Card;
 import be.infogroep.justpoker.GameElements.Deck;
 import be.infogroep.justpoker.messages.ReceiveCardsMessage;
 import be.infogroep.justpoker.messages.RegisterMessage;
 
 import com.esotericsoftware.kryo.Kryo;
 import com.esotericsoftware.kryonet.Connection;
 import com.esotericsoftware.kryonet.FrameworkMessage.KeepAlive;
 import com.esotericsoftware.kryonet.Listener;
 import com.esotericsoftware.kryonet.Server;
 
 import edu.vub.at.commlib.CommLib;
 import edu.vub.at.commlib.UUIDSerializer;
 
 public class PokerServer {
 	// private static final String TAG = "PokerServer";
 	public static final String BROADCAST_ACTION = "be.infogroep.justpoker.pokerserver.displayevent";
 	private static PokerServer SingletonPokerServer;
 
 	int nextClientID = 0;
 	private ConcurrentSkipListMap<Integer, PokerPlayer> connections = new ConcurrentSkipListMap<Integer, PokerPlayer>();
 	private volatile Thread serverThread;
 	private Server server;
 	private Deck deck;
 	// private final Handler handler = new Handler();
 	// Intent intent;
 	private ServerTableActivity gui;
 	private String ipAddress;
 
 	public PokerServer() {}
 
 	public PokerServer(ServerTableActivity serverTableActivity, String ip) {
 		this.gui = serverTableActivity;
 		this.ipAddress = ip;
 		deck = new Deck();
 		deck.shuffle();
 	}
 
 	public static PokerServer getInstance() {
 		if (SingletonPokerServer == null) {
 			SingletonPokerServer = new PokerServer();
 		}
 		return SingletonPokerServer;
 	}
 
 	public static PokerServer getInstance(
 			ServerTableActivity serverTableActivity, String ipAddress) {
 		// TODO Auto-generated method stub
 		if (SingletonPokerServer == null) {
 			SingletonPokerServer = new PokerServer(serverTableActivity,
 					ipAddress);
 		}
 		return SingletonPokerServer;
 	}
 
 	Runnable serverR = new Runnable() {
 		public void run() {
 			try {
 				Log.d("justPoker - Server", "Creating server");
 				server = new Server();
 				Kryo k = server.getKryo();
 				k.setRegistrationRequired(false); // false is the default
 				k.register(UUID.class, new UUIDSerializer());
 				// k.register(Message.class);
 				// k.register(RegisterMessage.class);
 				server.bind(CommLib.SERVER_PORT);
 				server.start();
 				final Runnable test = this;
 				server.addListener(new Listener() {
 					@Override
 					public void connected(Connection c) {
 						super.connected(c);
 						Log.d("justPoker - Server",
 								"Client connected: " + c.getRemoteAddressTCP());
 						addClient(c);
 					}
 
 					@Override
 					public void received(Connection c, Object msg) {
 						super.received(c, msg);
 						Log.d("justPoker - Server", "Message received " + msg);
 						Log.d("justPoker - server", "the gui is: " + gui);
 						if (!(msg instanceof KeepAlive)) {
 							messageParser(c, msg, test);
 						}
 					}
 
 					@Override
 					public void disconnected(Connection c) {
 						super.disconnected(c);
 						Log.d("justPoker - Server", "Client disconnected: " + c);
 						removeClient(c);
 					}
 				});
 			} catch (IOException e) {
 				Log.e("justPoker - Server", "Server thread crashed", e);
 			}
 		};
 	};
 
 	public synchronized void start() {
 		Log.d("justPoker - Server", "Starting server thread...");
 		if (serverThread == null) {
 			serverThread = new Thread(serverR);
 			serverThread.start();
 			// new Thread(gameLoop).start();
 			// SingletonPokerServer = this;
 		}
 
 	}
 
 	public synchronized void stop() {
 		if (serverThread != null) {
 			// Thread tmpThread = serverThread;
 			server.stop();
 			serverThread.interrupt();
 			serverThread = null;
 			// tmpThread.interrupt();
 			Log.d("justPoker - Server", "Stoped server thread");
 		}
 	}
 
 	public void addClient(Connection c) {
 		Log.d("justPoker - Server", "Adding client " + c.getRemoteAddressTCP());
 		connections.put(nextClientID, new PokerPlayer(nextClientID,c));
 		RegisterMessage m = new RegisterMessage(nextClientID);
 		c.sendTCP(m);
 		nextClientID++;
 	}
 
 	public void registerClient(Connection c, String nickname, int avatar,
 			int money) {
 		for (Integer i : connections.keySet()) {
 			if (connections.get(i).getConnection() == c) {
 				// gameLoop.addPlayer(c, i, nickname, avatar, money);
 				return;
 			}
 		}
 	}
 
 	public void removeClient(Connection c) {
 		// Log.d("wePoker - Server", "Client removed: " + c);
 		for (Integer i : connections.keySet()) {
 			if (connections.get(i).getConnection() == c) {
 				// gameLoop.removePlayer(i);
 				connections.remove(i);
 				return;
 			}
 		}
 	}
 
 	private void messageParser(Connection c, Object msg, Runnable r) {
 		// DisplayLoggingInfo(msg);
 		// handler.postDelayed(test, 2000);
 		if (msg instanceof RegisterMessage) {
 			RegisterMessage rm = (RegisterMessage) msg;
 			connections.get(rm.getClient_id());
 			gui.displayLogginInfo(rm.getName() + " connected");
 			// gui.displayLogginInfo("someone connected");
 		}
 		// handler.post(r);
 	}
 
 	public void dealCards() {
		for (Iterator<PokerPlayer> iterator = connections.values().iterator(); iterator
 				.hasNext();) {
			PokerPlayer player = iterator.next();
			Connection c = player.getConnection();
 			if (c.isConnected()) {
 				Log.d("justPoker - server", "Dealing cards to " + c.toString());
 				Card card1 = deck.drawFromDeck();
 				Card card2 = deck.drawFromDeck();
 				c.sendTCP(new ReceiveCardsMessage(card1, card2));
 				gui.displayLogginInfo("Dealt cards to " + c.toString());
 			}
 		}
 	}
 
 	public void startGame() {
 		// DisplayLoggingInfo("Starting a game!!!!");
 		Log.d("justPoker - server", "starting game");
		for (Iterator<PokerPlayer> iterator = connections.values().iterator(); iterator
 				.hasNext();) {
			PokerPlayer player = iterator.next();
 			Connection c = player.getConnection();
 			if (c.isConnected()) {
 				Log.d("justPoker - server", "sending to " + c.toString());
 				c.sendTCP("Starting the game!");
 				dealCards();
 			}
 		}
 	}
 }
