 package riskyspace.network;
 
 import java.io.EOFException;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.net.ConnectException;
 import java.net.Socket;
 import java.net.SocketException;
 import java.net.UnknownHostException;
 import java.util.Observable;
 
 /**
  * 
  * @author Daniel Augurell
  *
  */
 public class LobbyClient extends Observable {
 
 	private ObjectInputStream input = null;
 	private ObjectOutputStream output = null;
 	private Socket socket = null;
 	
 	public static final String IS_HOST = "is_host=";
 	public static final String MAX_PLAYERS = "max_players=";
 	public static final String CURRENT_PLAYER = "players=";
 	public static final String GAME_MODE = "game_mode=";
 	public static final String CONNECT_TO_GAME = "connect_to_game";
 	
 	public boolean connectToLobby(String hostIP) {
 		int tries = 0;
 		while (socket == null) {
 			if (tries == 3) {
 				System.err.println("Couldn't Connect");
 				return false;
 			}
 			System.out.println("Connecting. Test #" + (tries+1));
 			/*
 			 * Loop until Connected
 			 */
 			connectToHost(hostIP, 6012);
 			tries++;
 		}
 		new ServerListener();
 		return true;
 	}
 
 	public void startGame() {
 		try {
 			output.writeObject(LobbyServer.START_GAME);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	private void connectToHost(final String hostIP, final int hostPort) {
 		Runnable r = new Runnable() {
 			@Override
 			public void run() {
 				try {
 					socket = new Socket(hostIP, hostPort);
 					input = new ObjectInputStream(socket.getInputStream());
 					output = new ObjectOutputStream(socket.getOutputStream());
 				} catch (UnknownHostException e) {
 					System.err.println("Dont know about host: " + hostIP);
 					socket = null;
 				} catch (ConnectException e) {
 					System.err.println("Connection refused");
 					socket = null;
 				} catch (IOException e) {
 					e.printStackTrace();
 					socket = null;
 				}
 			}
 		};
 		Thread connectThread = new Thread(r);
 		connectThread.start();
 		int timeOut = 1000;
 		long startTime = System.currentTimeMillis();
 		boolean success = false;
 		while (System.currentTimeMillis() - startTime < timeOut) {
 			if (socket != null) {
 				success = true;
 				break;
 			}
 		}
 		if (!success) {
 			connectThread.interrupt();
 		}
 	}
 	
 	/**
 	 * 
 	 * @author Alexander Hederstaf
 	 *
 	 * Listens to events from the server and updates the 
 	 * View accordingly.
 	 */
 	private class ServerListener implements Runnable {
 		
 		public ServerListener() {
 			Thread t = new Thread(this);
 			t.start();
 		}
 		
 		@Override
 		public void run() {
 			boolean started = false;
 			boolean stopped = false;
 			while (!started && !stopped) {
 				try {
 					Object o = null;
 					if (input != null) {
 						o = input.readObject();
 					}
 					String input = null;
 					if (o instanceof String) {
 						input = (String) o;
 					}
 					if (input != null) {
 						if (input.contains(CONNECT_TO_GAME)) {
 							// Dispose window
 							setChanged();
 							notifyObservers("dispose");
 							// Create GameClient
 							String ip = socket.getInetAddress().getHostAddress();
 							new GameClient(ip, 6013);
 							// Set boolean to cancel this thread
 							started = true;
 						} else {
 							setChanged();
 							notifyObservers(input);
 						}
 					}
 				} catch (SocketException e) {
 					stopped = true;
 				} catch (EOFException e) {
 					stopped = true;
 				} catch (IOException e) {
 					e.printStackTrace();
 				} catch (ClassNotFoundException e) {
 					/*
 					 * Got an nonserializable object, nothing to do here.
 					 */
 				}
 			}
 		}
 	}
 
 	public void close() {
		if(socket != null){
			try {
				output.writeObject(LobbyServer.DISCONNECT);
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
 		}
 	}
 }
