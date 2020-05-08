 package webarchive.server;
 
 import webarchive.connection.Connection;
 import webarchive.connection.NetworkModule;
 import webarchive.handler.Handlers;
 import webarchive.init.ConfigHandler;
 import webarchive.transfer.Header;
 import webarchive.transfer.Message;
 
 import java.io.IOException;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.net.SocketException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 public class Server implements Runnable, NetworkModule {
 
 	private static Server sv = null;
 	private int listenPort;
 	private ServerSocket svSock;
 	private final List<Connection> cList;
 	private final List<Connection> observers;
 	private Boolean running = false;
 	private Thread thread;
 
 	private Handlers collection;
 	
 	public Handlers getCollection() {
 		return collection;
 	}
 
 	public Thread getThread() {
 		return thread;
 	}
 
 	private Server(Handlers col) {
 		this.collection = col;
 		this.listenPort = new Integer(((ConfigHandler) collection.get(
 			ConfigHandler.class)).getValue("webarchive.server.port"));
 		this.cList = new ArrayList<>();
 		this.observers = new ArrayList<>();
 
 	}
 
 	public static Server getInstance() {
 		return sv;
 
 	}
 	
 	public static void init(Handlers col) {
 		sv = new Server(col);
 	}
 
 	public boolean start() {
 		if(checkRunning())
 			return false;
 		thread = new Thread(sv);
 		thread.start();
 		return checkRunning();
 	}
 
 	public boolean stop() {
			if(!checkRunning())
 				return false;
 			try {
 				System.out.println("closing svSock");
 				svSock.close();
 			} catch (IOException e) {
 				e.printStackTrace();
 
 			}
 		return true;
 	}
 
 	@Override
 	public void run() {
 		if (initRunning())
 			return;
 		accept();
 		finalizeRunning();
 	}
 
 	private void setRunning(boolean running) {
 		this.running = running;
 	}
 	private synchronized boolean checkRunning() {
 		if (isRunning()) {
 			return true;
 		}
 		return false;
 	}
 	private synchronized boolean initRunning() {
 		if (isRunning()) {
 			return true;
 		}
 		setRunning(true);
 		return false;
 	}
 	private synchronized void finalizeRunning() {
 		setRunning(false);
 	}
 
 	private boolean isRunning() {
 		return running;
 	}
 
 	private void disconnectClients() {
 		System.out.println("disconnecting Clients " + cList.size());
 		synchronized (cList) {
 			for (Connection c : cList) {
 				try {
 					c.getSocket().close();
 				} catch (IOException ex) {
 					Logger.getLogger(Server.class.getName()).
 						log(Level.SEVERE, null, ex);
 				}
 			}
 			cList.clear();
 		}
 		System.out.println("Clients disconnected");
 	}
 
 	private void accept() {
 		try {
 			this.svSock = new ServerSocket(this.listenPort);
 		} catch (IOException ex) {
 			Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
 		}
 		System.out.println("creating svSocket");
 
 		while (true) {
 			Socket sock = null;
 
 			try {
 				System.out.println("awaiting incomming connection");
 				sock = svSock.accept();
 				System.out.println("client connected!");
 			} catch (SocketException e) {
 				Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null,
 						e);
 				disconnectClients();
 				break;
 
 			} catch (IOException ex) {
 				Logger.getLogger(Server.class.getName()).log(Level.INFO, null,
 					ex);
 				continue;
 			}
 			new Thread(new ClientManager(this,sock)).start();
 		}
 	}
 
 	synchronized void addNewConnection(Connection c) {
 			cList.add(c);
 	}
 
 
 	@Override
 	public void removeConnection(Connection c) {
 		synchronized (cList) {
 			cList.remove(c);
 		}
 		synchronized (observers) {
 			observers.remove(c);
 		}
 	}
 
 	public Connection[] getObserverArray() {
 		Connection[] cons=null;
 			
 			synchronized (observers) {
 				Message ping = new Message(Header.PING);
 				ping.setBroadCast();
 				for(Connection c : observers) {
 					try {
 						c.send(ping);
 					} catch (Exception e) {
 						observers.remove(c);
 						cList.remove(c);
 						try {
 							c.getSocket().close();
 						} catch (IOException e1) {
 						}
 						Logger.getLogger(Server.class.getName()).log(Level.INFO,"Client "+c+" was not reachable and has been removed!");
 					}
 				}
 				cons = new Connection[observers.size()];
 				cons = observers.toArray(cons);
 			}
 		return cons;
 	}
 	public List<Connection> getObservers() {
 		return observers;
 	}
 }
