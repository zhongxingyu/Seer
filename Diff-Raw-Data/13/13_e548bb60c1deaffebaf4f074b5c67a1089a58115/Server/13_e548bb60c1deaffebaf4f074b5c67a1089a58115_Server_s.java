 package server;
 
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.net.BindException;
 import java.net.InetAddress;
 import java.net.ServerSocket;
 import java.net.Socket;
import java.util.Vector;
 
 import messages.ChatMessage;
 import messages.Message;
 import messages.MoveMessage;
 import messages.WinMessage;
 import universal.Parameters;
 
 /**
  * 4002-219 BreakThrough Server This is the server for the MyClient class/GUI
  * 
  * Channel 4 News Team: Rick Skanron David Aman Rohan Reen Quang Nguyen Tim Wong
  */
 
 public class Server implements Parameters {
 	/**
 	 * Thread class that represents a connected client and handles all
 	 * server-client communications
 	 */
 	public class ServerThread extends Thread {
 		private String clientID; // ID of client
 		private int clientsConnected;
 
 		private String gameID; // ID of game that client is in
 		private ObjectInputStream ois;
 
 		private ObjectOutputStream oos;
 
 		/**
 		 * Parameterized constructor
 		 * 
 		 * @param clientChatSocket
 		 */
 		public ServerThread(Socket clientChatSocket) {
 			clientsConnected = connectedClients.size();
 
 			try {
 				oos = new ObjectOutputStream(clientChatSocket.getOutputStream());
 				ois = new ObjectInputStream(clientChatSocket.getInputStream());
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 
 		/**
 		 * Getter for clientID
 		 * 
 		 * @return clientID
 		 */
 		public String getClientID() {
 			return clientID;
 		}
 
 		/**
 		 * Returns object output stream of client
 		 * 
 		 * @return oos
 		 */
 		public ObjectOutputStream getClientObjectOutStream() {
 			return oos;
 		}
 
 		/**
 		 * Returns gameID of game that client is in
 		 * 
 		 * @return gameID
 		 */
 		public String getGameID() {
 			return gameID;
 		}
 
 		/**
 		 * Run method of thread
 		 */
 		@Override
 		public void run() {
 			try {
 				boolean sentInitialMessage = false;
 
 				while (true) {
 					// make sure to send initial package
 					if (!sentInitialMessage) {
 						// tell client what their ID is, which is the next
 						// number in count
 
 						if (connectedClients.size() % 2 == 1) {
 							clientID = playerCount + "A";
 						} else {
 							clientID = playerCount + "B";
 						}
 
 						oos.writeObject(new Message(clientID));
 						oos.flush();
 						oos.writeObject(new ChatMessage(clientID, "Server",
 								" Welcome to the Chat!"));
 						oos.flush();
 						sentInitialMessage = true;
 					}
 
 					// check if # of connected clients known by client equals
 					// the current # of connected clients
 					if (clientsConnected != connectedClients.size()) {
 						clientsConnected = connectedClients.size();
 						serverGUI.appendJTASouth(clientID
 								+ " has connected! \n");
 						refreshClientsList();
 					}
 
 					// Receive message from client
 					Message message = (Message) ois.readObject();
 					System.out.println("Read Object");
 
 					if (message instanceof ChatMessage) {
 						System.out.println("ChatMessage received");
 						ChatMessage cMess = (ChatMessage) message;
 						serverGUI.appendJTASouth(message.toString() + "\n");
 
 						// Broadcast the message to all connected clients
 						for (ServerThread client : connectedClients) {
 							// get output stream for the selected client in
							// vector
 							ObjectOutputStream tempOut = client
 									.getClientObjectOutStream();
 
 							tempOut.writeObject(cMess); // to client
 							tempOut.flush();
 						}
 					} else if (message instanceof MoveMessage) {
 						MoveMessage mMess = (MoveMessage) message;
 						System.out.println("It is a Move object");
 						for (Game g : games) {
 							if (g.getGameID().equals(this.getGameID())) {
 								System.out.println(clientID);
 								g.validateMove(mMess, clientID);
 								break;
 							}
 						}
 					} else {
 						WinMessage wMess = (WinMessage) message;
 
 						for (int i = 0; i <= playerCount; i++) {
 							ObjectOutputStream oOut = connectedClients.get(i)
 									.getClientObjectOutStream();
 
 							oOut.writeObject(wMess); // send win object to
 							// client
 							oOut.flush();
 						}
 					}
 				}// end while
 			} catch (IOException e) {
 				System.out
 						.println("Inside catch, connection may have been lost");
 				connectedClients.remove(this);
 				serverGUI.appendJTASouth(clientID + " has disconnected! \n");
 				for (ServerThread client : connectedClients) {
					// get output stream for the selected client in vector
 					ObjectOutputStream tempOut = client
 							.getClientObjectOutStream();
 					try {
 						tempOut.writeObject(new ChatMessage(clientID, "Server",
 								clientID + " has disconnected! \n"));
 						tempOut.flush();
 					} catch (IOException e1) {
 						e1.printStackTrace();
 					} // to client
 					for (Game g : games) {
 						if (this.getGameID() == g.getGameID()) {
 							try {
 								tempOut.writeObject(new ChatMessage(clientID,
 										"Server", "Your game has ended."));
 								tempOut.flush();
 							} catch (IOException e1) {
 								e1.printStackTrace();
 							}
 							games.remove(g);
 							refreshGamesList();
 							break;
 						}
 					}
 				}// end for loop
 				refreshClientsList();
 			}
 
 			catch (ClassNotFoundException cnf) {
 				cnf.printStackTrace();
 			}
 
 		} // end run
 
 		/**
 		 * Sets gameID when game is initiated
 		 * 
 		 * @param gameID
 		 */
 		public void setGameID(String gameID) {
 			this.gameID = gameID;
 		}
 
 	} // end class ThreadServer
 
 	/**
 	 * Main method for the MyMultiThreadedServer class
 	 * 
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		new Server();
 	}
 
 	private ServerSocket chatS = null;
	private Vector<ServerThread> connectedClients = new Vector<ServerThread>();
	private Vector<Game> games = new Vector<Game>();
 
 	int numGames = 0;
 
 	private int playerCount = 0;
 
 	// GUI components
 	private ServerGUI serverGUI;
 
 	/**
 	 * Default constructor
 	 */
 	public Server() {
 		// create GUI
 		serverGUI = new ServerGUI();
 
 		try {
 			chatS = new ServerSocket(chatPort);
 			System.out.println("getLocalHost: " + InetAddress.getLocalHost());
 			System.out.println("getByName:    "
 					+ InetAddress.getByName("localhost"));
 
 			while (true)// run until server is shut down
 			{
 
 				Socket clientChatSocket = chatS.accept(); // wait for 1st client
 				ServerThread ths1 = new ServerThread(clientChatSocket);
 				connectedClients.add(ths1);
 				ths1.start();
 
 				if (connectedClients.size() % 2 == 1) {
 					playerCount++;
 				}
 
 				clientChatSocket = chatS.accept(); // wait for 2nd client to
 				ServerThread ths2 = new ServerThread(clientChatSocket);
 				connectedClients.add(ths2);
 				ths2.start();
 
 				if (connectedClients.size() % 2 == 1) {
 					playerCount++;
 				}
 
 				// Sleep 5ms to allow ThreadServer to instantiate and
 				// Game to recognize second player
 				try {
 					Thread.sleep(5);
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 				numGames++;
 				Game game = new Game(this, ths1, ths2);
 				games.add(game);
 
 				System.out.println("Game has started");
 				refreshGamesList();
 
 			} // end while
 		} catch (BindException be) {
 			System.out
 					.println("Server already running on this computer, stopping.");
 			System.exit(1);
 		} catch (IOException ioe) {
 			System.out.println("IO Error");
 			ioe.printStackTrace();
 		}
 
 	} // end main
 
 	/**
 	 * Refreshes list of connected clients in server GUI whenever a client
 	 * (dis)connects
 	 */
 
 	public synchronized void refreshClientsList() {
 		serverGUI.getJtaWest().setText("");
 		serverGUI.appendJTAWest(connectedClients.size()
 				+ " client(s) connected! \n");
 		if (connectedClients.size() >= 1) {
 			for (int x = 0; x < connectedClients.size(); x++) {
 				serverGUI.appendJTAWest("Client ID: "
 						+ connectedClients.get(x).getClientID() + "\n");
 			}
 		}
 	}
 
 	/**
 	 * Refreshes list of games in server GUI whenever a game starts/ends
 	 */
 	public synchronized void refreshGamesList() {
 		serverGUI.getJtaCenter().setText("");
 		serverGUI.appendJTACenter(games.size() + " games running! \n");
 		if (games.size() >= 1) {
 			for (int x = 0; x < games.size(); x++) {
 				serverGUI.appendJTACenter("Game ID: "
 						+ games.get(x).getGameID() + "\n");
 			}
 		}
 	}
 
 } // end MyMultiThreadedServer
