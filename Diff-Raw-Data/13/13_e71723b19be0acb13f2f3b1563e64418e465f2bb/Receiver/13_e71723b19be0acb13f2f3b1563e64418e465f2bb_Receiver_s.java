 package implimentations;
 
 import gui.Interface;
 import interfaces.IMessage;
 import interfaces.IReceiver;
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.net.URLConnection;
 import java.rmi.RMISecurityManager;
 import java.rmi.RemoteException;
 import java.rmi.registry.LocateRegistry;
 import java.rmi.registry.Registry;
 import java.rmi.server.RemoteServer;
 import java.rmi.server.UnicastRemoteObject;
 import java.security.InvalidParameterException;
 import java.security.NoSuchAlgorithmException;
 import java.security.PrivateKey;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 public class Receiver implements IReceiver {
 	private static final int CACHE_SIZE = 100;
 	private static final int PORT = 1099;
 	private static String peerListUrl = "http://vierware.com/test/peerlist.txt";
 	
 	private static Interface ui;
 	private static ArrayList<IReceiver> peerList;
 	private static ArrayList<String> peerHostNames;
 	private static ArrayList<Integer> cache;
 	private static MessageHandler messageHandler;
 	private static RsaKeyPair rkp;
 	private static IReceiver stub;
 	private static Receiver instance = null;
 	
 	public static ArrayList<String> getPeerList(String url) {
 		ArrayList<String> peers = new ArrayList<String>();
 		try {
 			URLConnection conn = (new URL(url)).openConnection();
 
 			BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
 			String inputLine;
 			
 			while ((inputLine = in.readLine()) != null) 
 				peers.add(inputLine);
 			in.close();
 		} catch (Exception e) {
 			return peers;
 		}
 		
 		return peers;
 	}
 	
 	public static Receiver startServer(Interface gui, String[] args) {
 		if (instance != null)
 			return instance;
 		
 		// Remember the user interface for updating later //
 		ui = gui;
 		
 		
 		System.out.println("DEBUG: Creating Server.");
 		
 		// Get the RSA Keys for messaging //
 		rkp = RsaKeyHandler.getKeys();
 		instance = new Receiver(new MessageHandler(rkp.getPrivate()));
 		
 		try {
 			
 			// Create a stub of the server //
 			stub = (IReceiver) UnicastRemoteObject.exportObject(instance, PORT);
 			
 			// Get the RMI registry
 			Registry registry = null;
 			try {
 				registry = LocateRegistry.createRegistry(PORT);
 			} catch (RemoteException e) {
 				registry = LocateRegistry.getRegistry();
 			}
 			
 			// Host the stub on the registry, on the specified port
 			registry.bind("Receiver", stub);
 			System.out.println("DEBUG: Server Bound.");
 			
 			// Connect to peers //
 			System.out.println("DEBUG: Connecting to Clients.");
 			System.setSecurityManager(new RMISecurityManager());
 			if (args.length > 0) {
 				ArrayList<String> peers = new ArrayList<String>();
 				for (String s : args) {
 					peers.add(s);
 				}
 				instance.connectTo(peers);
 			} else {
 				instance.connectTo(getPeerList(peerListUrl));	// Get list of newline-seperated IP's of other peers
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return instance;				
 	}
 	
	public void sendMessage(String from, String to, String message) {
		IMessage m = MessageBroker.assembleMessage(rkp.getPublic(), to, from, message);
 		try {
 			stub.propagate(m);
 		} catch (RemoteException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public Receiver(MessageHandler mh) {
 		if (mh == null)
 			throw new InvalidParameterException("Message Handler cannot be null.");
 		
 		peerList = new ArrayList<IReceiver>();
 		peerHostNames = new ArrayList<String>();
 		cache = new ArrayList<Integer>();
 		messageHandler = mh;
 	}
 
 	@Override
 	public void propagate(IMessage message) throws RemoteException {
 		try {
 			// Check if message is in cache and has already been propagated before.
 			if (cache.contains(message.getIdentifier())) {
 				System.out.println("DEBUG: Message has come back again - discarding.");
 				return;
 			}
 			
 			// Message is new, add it to the cache
 			cache.add(message.getIdentifier());
 			
 			// Make sure cache size is limited
 			while (cache.size() > CACHE_SIZE)
 				cache.remove(0);
 			
 			// Propagate message to all connected hosts
 			String rhost = RemoteServer.getClientHost();
 			for (int i = 0; i < peerList.size(); i++) {
 				// Except the host that sent this to us.
 				String hostname = peerHostNames.get(i);
 				if (rhost.equals(hostname)) continue;
 				Thread t = new Thread(new Propogate(peerList.get(i), message), hostname);
 				t.start();
 			}
 			
 			// Decrypt the message - it might be ours!
 			System.out.println("DEBUG: Just received a message; Decrypting.");
 			if (messageHandler.decrypt(message)) {
 				// Message is to us //
 				ui.newMessage(message);
 			}
 		} catch (Exception e) {
 			System.out.println("DEBUG: Decryption Failed: " + e.getMessage());
 		}
 	}
 	
 	public boolean connectTo(String host) {
 		try {
 			Registry registry = LocateRegistry.getRegistry(host);
 			IReceiver node = (IReceiver) registry.lookup("Receiver");
 			peerList.add(node);
 			peerHostNames.add(host);
 			System.out.println("DEBUG: Node at " + host + " now part of peer list.");
 			node.connect();
 			return true;
 		} catch (Exception e) {
 			System.out.println("DEBUG: Failed to connect to node at " + host);
 			return false;
 		}
 	}
 	
 	public boolean connectTo(ArrayList<String> peers) {
 		boolean allPassed = true;
 		for (String peer : peers) {
 			if (!connectTo(peer)) 
 				allPassed = false;
 		}
 		
 		return allPassed;
 	}
 
 	public void addNode(IReceiver stub) {
 		if (stub == null) return;
 		peerList.add(stub);
 	}
 
 	@Override
 	public boolean connect() throws RemoteException {
 		try {
 			String client = RemoteServer.getClientHost();
 			if (peerHostNames.contains(client)) return false;
 			Registry registry = LocateRegistry.getRegistry(client);
 			IReceiver node = (IReceiver) registry.lookup("Receiver");
 			peerList.add(node);
 			peerHostNames.add(client);
 			System.out.println("Node at " + client + " now part of peer list.");
 			return true;
 		} catch (Exception e) {
 			return false;
 		}
 		
 	}
 }
