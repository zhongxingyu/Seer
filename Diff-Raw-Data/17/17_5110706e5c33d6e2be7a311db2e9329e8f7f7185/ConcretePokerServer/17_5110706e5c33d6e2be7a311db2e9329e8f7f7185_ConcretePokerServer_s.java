 package edu.vub.at.nfcpoker;
 
 import java.io.IOException;
 import java.util.TreeMap;
 
 import com.esotericsoftware.kryo.Kryo;
 import com.esotericsoftware.kryonet.Connection;
 import com.esotericsoftware.kryonet.Listener;
 import com.esotericsoftware.kryonet.Server;
 
 import android.app.Activity;
 import android.util.Log;
 import edu.vub.at.commlib.CommLib;
 import edu.vub.at.commlib.CommLibConnectionInfo;
 import edu.vub.at.nfcpoker.comm.Message;
 import edu.vub.at.nfcpoker.comm.PokerServer;
 import edu.vub.at.nfcpoker.comm.Message.ReceiveHoleCardsMessage;
 import edu.vub.at.nfcpoker.comm.Message.ReceivePublicCards;
 import edu.vub.at.nfcpoker.comm.Message.StateChangeMessage;
 
 public class ConcretePokerServer extends PokerServer  {
 	
 	Runnable exporterR = new Runnable() {	
 		@Override
 		public void run() {
 			Log.d("PokerServer", "Starting export");
 			String address = "192.168.1.106";
 			String port = "" + CommLib.SERVER_PORT;
 			CommLibConnectionInfo clci = new CommLibConnectionInfo(PokerServer.class.getCanonicalName(), new String[] {address, port});
 			try {
 				CommLib.export(clci);
 			} catch (IOException e) {
 				Log.e("PokerServer", "Exporter thread crashed", e);
 			}
 		}
 	};
 	
 	Runnable serverR = new Runnable() {
 		public void run() {
 			try {
 				Log.d("PokerServer", "Starting serverR");
 				Server s = new Server();
 				Kryo k = s.getKryo();
 				k.setRegistrationRequired(false);
 				s.bind(CommLib.SERVER_PORT);
 				s.start();
 				s.addListener(new Listener() {
 					@Override
 					public void connected(Connection c) {
 						super.connected(c);
 						Log.d("PokerServer", "Client connected: " + c.getRemoteAddressTCP());
 						gameLoop.addClient(c);
 					}
 					public void disconnected(Connection c) {
 						super.disconnected(c);
 						Log.d("PokerServer", "Client disconnected: " + c.getRemoteAddressTCP());
 						gameLoop.removeClient(c);
 					}
 				});
 			} catch (IOException e) {
 				Log.e("PokerServer", "Server thread crashed", e);
 			}
 		};
 	};
 	
 	int nextClientID = 0;
 	public enum GameState {
 		STOPPED, WAITING_FOR_PLAYERS, PREFLOP, FLOP, TURN, RIVER, END_OF_ROUND;
 	};
 	
 
 
 	public ConcretePokerServer(Activity gui) {
 	}
 	
 	public void start() {		
 		new Thread(serverR).start();
 		new Thread(exporterR).start();
 	}
 	
 	private GameLoop gameLoop = new GameLoop();
 	
 	class GameLoop implements Runnable {
 		
 		public GameLoop() {
 			gameState = GameState.STOPPED;
 		}
 
 		// todo: what if client disconnects before next round?
 		public void removeClient(Connection c) {
 			Log.d("PokerServer", "Client disconnected: " + c);
 			synchronized(this) {
 				for (Integer i : clientsInGame.keySet()) {
 					if (clientsInGame.get(i) == c) {
 						clientsInGame.remove(i);
 					}
 				}
 			}
 		}
 
 		public TreeMap<Integer, Connection> newClients = new TreeMap<Integer, Connection>();
 		public TreeMap<Integer, Connection> clientsInGame = new TreeMap<Integer, Connection>();
 		public GameState gameState;
 			
 		public void run() {
 			gameState = GameState.PREFLOP;
 			while (true) {
 				synchronized(this) {
 					clientsInGame.putAll(gameLoop.newClients);
 					newClients.clear();
 					for (Integer i : clientsInGame.keySet()) {
 						if (clientsInGame.get(i) == null)
 							clientsInGame.remove(i);
 					}
 					if (clientsInGame.size() < 2) {
 						try {
 							Log.d("PokerServer", "# of clients < 2, changing state to stopped");
 							broadcast(new StateChangeMessage(GameState.WAITING_FOR_PLAYERS));
 							this.wait();
 						} catch (InterruptedException e) {
 							Log.wtf("PokerServer", "Thread was interrupted");
 						}
 					}
 					//todo what if clientsInGame.size drops below two?
 				}
 				
 				Deck deck = new Deck();
				Card[] commonCards = deck.drawCards(5);
 				
 				// hole cards
 				newState(GameState.PREFLOP);
 				TreeMap<Integer, Card[]> holeCards = new TreeMap<Integer, Card[]>();
 				for (Integer clientNum : clientsInGame.navigableKeySet()) {
 					Card preflop[] = deck.drawCards(2);
 					holeCards.put(clientNum, preflop);
 					Connection c = clientsInGame.get(clientNum);
 					c.sendTCP(new ReceiveHoleCardsMessage(preflop[0], preflop[1]));
 				}
 					
 				
 				// flop cards
 				Card[] flop = deck.drawCards(3);
 				broadcast(new ReceivePublicCards(flop));
 				newState(GameState.FLOP);
 
 				// turn cards
 				Card[] turn = deck.drawCards(1);
 				broadcast(new ReceivePublicCards(turn));
 				newState(GameState.TURN);
 
 				// river cards
 				Card[] river = deck.drawCards(1);
				broadcast(new ReceivePublicCards(turn));
 				newState(GameState.RIVER);
 
 				// results
 				newState(GameState.END_OF_ROUND);
 
 				
 				try {
 					Thread.sleep(10000);
 				} catch (InterruptedException e) {
 					Log.wtf("PokerServer", "Thread.sleep was interrupted", e);
 				}
 			}
 		}
 
 		private void newState(GameState newState) {
 			gameState = newState;
 			broadcast(new StateChangeMessage(newState));
 		}
 		
 		private void broadcast(Message m) {
 			for (Connection c : clientsInGame.values())
 				if (c != null)
 					c.sendTCP(m);
 		}
 
 		public void addClient(Connection c) {
 			Log.d("PokerServer", "Adding client " + c.getRemoteAddressTCP());
 			synchronized(this) {
 				newClients.put(nextClientID++, c);
 			}
 			if (newClients.size() >= 2)
 				if (gameState == GameState.STOPPED) {
 					Log.d("PokerServer", "Two or more clients connected, game can start");
 					new Thread(this).start();
 			} else if (gameState == GameState.WAITING_FOR_PLAYERS) {
 				synchronized(this) { this.notifyAll(); }
 			}
 		}
 	};
 
 }
