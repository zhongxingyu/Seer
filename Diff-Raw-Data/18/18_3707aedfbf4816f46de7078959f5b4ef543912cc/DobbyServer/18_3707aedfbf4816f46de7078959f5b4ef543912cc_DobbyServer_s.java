 package com.dobby.app;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 
 import com.dobby.app.comm.BroadcastListener;
 import com.dobby.core.Request;
 import com.dobby.core.Session;
 import com.spartango.network.AsyncServerEvent;
 import com.spartango.network.AsyncServerListener;
 import com.spartango.network.AsyncServerSocket;
 
 public class DobbyServer implements AsyncServerListener {
 
 	private AsyncServerSocket server;
 	private Session serverSession;
 	private Map<String, BroadcastListener> listeners;
 
 	public DobbyServer(String documentName, int port) {
 		try {
 			serverSession = new Session("Server", documentName);
 			server = new AsyncServerSocket(port);
 			server.add(this);
 			listeners = new HashMap<String, BroadcastListener>();
 			server.start();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 	}
 
 	@Override
 	public void onServerClosed() {
 		// Close clients gracefully
 		for (BroadcastListener listener : listeners.values()) {
 			listener.onProviderClosed();
 		}
 	}
 
 	@Override
 	public void onNewClient(AsyncServerEvent e) {
 		System.out.println("Spawning new client for "
 				+ e.getClient().getInetAddress().toString());
 		try {
 			DobbyClient newClient = new DobbyClient(this, e.getClient(),
 					serverSession.getDocName());
 			System.out.println("New client: " + newClient.toString());
 		} catch (IOException e1) {
 			e1.printStackTrace();
 		}
 	}
 
 	@Override
 	public void onServerFailure(AsyncServerEvent e) {
 	}
 
 	public synchronized void registerClient(String userName,
 			BroadcastListener client) {
		System.out.println("Registering and syncing client "+userName);
 		listeners.put(userName, client);
 		Session newSession = serverSession.clone();
 		newSession.setUserName(userName);
 		client.syncState(newSession);
 	}
 
 	public synchronized void releaseClient(String userName) {
 		listeners.remove(userName);
 	}
 
 	public synchronized void broadcastRequest(Request r) {
 		// TODO parallelize sending broadcasts
 		for (String listener : listeners.keySet()) {
 			if (!r.getUser().equals(listener)) {
 				listeners.get(listener).onBroadcastReceived(r);
 			}
 		}
 	}
 
 	public boolean isOpen() {
 		return server.isAccepting();
 	}
 }
