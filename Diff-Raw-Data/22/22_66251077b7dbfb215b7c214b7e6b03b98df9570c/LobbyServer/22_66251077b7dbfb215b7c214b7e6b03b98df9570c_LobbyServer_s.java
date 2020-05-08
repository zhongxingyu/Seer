 package riskyspace.network;
 
 import java.io.EOFException;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.net.InetAddress;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.net.SocketException;
 import java.net.SocketTimeoutException;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.List;
 
 import riskyspace.data.SavedGame;
 
 
 public class LobbyServer {
 	
 	public static final String START_GAME = "start_game";
 
 	public static final String DISCONNECT = "disconnect";
 	
 	private int maxNumberOfPlayers;
 	private ServerSocket ss = null;
 
 	private SavedGame game;
 	
 	private List<ConnectionHandler> connections = new ArrayList<ConnectionHandler>();
 	
 	private int port;
 	private String ip;
 
 	private boolean started = false;
 
 	public LobbyServer(int maxNumberOfPlayers) {
 		this.maxNumberOfPlayers = maxNumberOfPlayers;
 		this.port = 6012;
 		try {
 			ss = new ServerSocket(port);
 		} catch (IOException e) {
 			e.printStackTrace();
 			System.exit(1);
 		}
 		try {
 			ss.setSoTimeout(1000);
 		} catch (SocketException e) {
 			e.printStackTrace();
 		}
 		new AcceptThread();
 		try {
 			ip = InetAddress.getLocalHost().getHostAddress();
 			System.out.println("Server started with IP: " + ip + ":" + port);
 		} catch (UnknownHostException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public LobbyServer(SavedGame game) {
 		this.maxNumberOfPlayers = game.getPlayers().size();
 		this.port = 6012;
 		try {
 			ss = new ServerSocket(port);
 		} catch (IOException e) {
 			e.printStackTrace();
 			System.exit(1);
 		}
 		try {
 			ss.setSoTimeout(1000);
 		} catch (SocketException e) {
 			e.printStackTrace();
 		}
 		new AcceptThread();
 		try {
 			ip = InetAddress.getLocalHost().getHostAddress();
 			System.out.println("Server started with IP: " + ip + ":" + port);
 		} catch (UnknownHostException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public int getMaxNumberOfPlayers() {
 		return maxNumberOfPlayers;
 	}
 
 	public String getIP() {
 		return ip;
 	}
 
 	public int getPort() {
 		return port;
 	}
 	
 	public boolean start() {
 		if(connections.size() == maxNumberOfPlayers){
 			started = true;
 			String[] ips = new String[maxNumberOfPlayers];
 			for (int i = 0; i < ips.length; i++) {
 				ips[i] = connections.get(i).socket.getInetAddress().getHostAddress();
 			}
 			if (game != null) {
 				new GameServer(ips, game);
 			} else {
 				new GameServer(maxNumberOfPlayers, ips);
 			}
 			for (ConnectionHandler ch : connections) {
 				try {
 					ch.output.writeObject("connect_to_game");
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 		return started;
 	}
 	
 	private class AcceptThread implements Runnable {
 		Thread t = null;
 		public AcceptThread() {
 			t = new Thread(this);
 			t.start();
 		}
 
 		@Override
 		public void run() {
 			Socket cs = null;
 			while (!started && !ss.isClosed()) {
 				if (connections.size() < maxNumberOfPlayers) {
 					try {
 						cs = ss.accept();
 						connections.add(new ConnectionHandler(cs));
 						for (ConnectionHandler ch : connections) {
 							ch.output.writeObject("players=" + (connections.size()));
 						}
 					} catch (SocketTimeoutException e) {
 					} catch (SocketException e) {
 						System.err.println("failed connect");
 					} catch (IOException e) {
 						e.printStackTrace();
 						System.exit(1);
 					}
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Close this server
 	 */
 	public void close() {
 		try {
 			int times = connections.size();
 			for (int i = 0; i < times; i++) {
 				connections.get(0).disconnect();
 			}
 			ss.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	private class ConnectionHandler implements Runnable {
 		private Socket socket;
 		private ObjectInputStream input = null;
 		private ObjectOutputStream output = null;
 		
 		private boolean host = false;
 		
 		public ConnectionHandler(Socket socket) throws IOException {
 			this.socket = socket;
 			this.output = new ObjectOutputStream(socket.getOutputStream());
 			this.input = new ObjectInputStream(socket.getInputStream());
 			
 			if (getIP().equals(socket.getInetAddress().getHostAddress())){
 				host = true;
 			}
 			
 			output.writeObject("max_players=" + maxNumberOfPlayers);
 			output.writeObject("game_mode=" + "Annihilation");
 			output.writeObject("is_host=" + host);
 			
 			Thread t = new Thread(this);
 			t.start();
 		}
 
 		@Override
 		public void run() {
 			while (!started) {
 				if(!socket.isConnected()){
 					disconnect();
 					break;
 				}
 				try {
 					Object o = input.readObject();
 					if (host && o instanceof String) {
 						if (START_GAME.equals(o) && connections.size() == maxNumberOfPlayers) {
 							start();
 						}
 					}else if (o instanceof String){
 						if (DISCONNECT.equals(o)) {
 							disconnect();
 							for (ConnectionHandler ch : connections) {
 								ch.output.writeObject("players=" + (connections.size()));
 							}
 						}
 					}
 				} catch (SocketException e){
 					disconnect();
 					break;
 				} catch (EOFException e) {
 					disconnect();
 					break;
 				} catch (IOException e) {
 					e.printStackTrace();
 				} catch (ClassNotFoundException e) {
 					/*
 					 * Server got an nonserializable object, nothing to do here.
 					 */
 				}
 			}
 		}
 
 		private void disconnect() {
 			try {
 				socket.close();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 			connections.remove(this);
 		}
 	}
 }
