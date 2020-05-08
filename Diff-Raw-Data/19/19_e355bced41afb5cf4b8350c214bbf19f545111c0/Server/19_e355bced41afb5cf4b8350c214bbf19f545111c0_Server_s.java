 package com.ankurdave.boggle;
 import java.io.IOException;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.util.ArrayList;
 import java.util.Collections;
 /**
  * Server component of DistBoggle. Manages BoggleClients.
  * @author ankur
  */
 public class Server {
 	private static final int DEFAULT_POP_CAP = 20;
 	private static final int POP_CAP_RANGE = 0;
 	private int curClientID = 0;
 	private Dictionary dict;
 	private Board highest;
 	private ServerSocket socket;
 	private long startTime;
 	private ArrayList<ServerThread> threads = new ArrayList<ServerThread>();
 	public Server(int port) {
 		// create the socket
 		socket = null;
 		try {
 			socket = new ServerSocket(port);
 		}
 		catch (IOException e) {
 			System.err.println("Could not listen on port " + port);
 			System.exit(1);
 		}
 		// create the dictionary
 		dict = new Dictionary();
 		dict.buildDictionary("words.txt");
 	}
 	public synchronized void addMigrant(Board migrant, ServerThread caller) {
 		Collections.sort(threads);
 		for (ServerThread c : threads) {
 			if ((c.getMigrant() == null || c.getMigrant().getScore() < migrant
 			        .getScore())
 			        && caller != c) {
 				c.setMigrant(migrant);
 				break;
 			}
 		}
 	}
 	public synchronized Dictionary getDictionary() {
 		return dict;
 	}
 	public synchronized int getPopCapForClient(int clientID) {
 		Collections.sort(threads);
 		if (threads.size() == 1) { return DEFAULT_POP_CAP; }
 		for (int i = 0; i < threads.size(); i++) {
 			if (threads.get(i).getId() == clientID) { return (DEFAULT_POP_CAP + POP_CAP_RANGE / 2)
 			        - i * (POP_CAP_RANGE / (threads.size() - 1)); }
 		}
 		return DEFAULT_POP_CAP;
 	}
 	/**
      * Starts listening for clients. Starts a new thread for each client. Never
      * returns.
      */
 	public void listen() {
 		try {
 			while (true) {
 				Socket s = socket.accept();
 				ServerThread serverThread = new ServerThread(this, s,
 				        curClientID++);
 				threads.add(serverThread);
 				serverThread.start();
 				// start the timer
 				if (startTime == 0) { // if not already started
 					startTime = System.currentTimeMillis();
 				}
 			}
 		}
 		catch (IOException e) {
 			System.err.println("Error while listening: " + e);
 			System.exit(1);
 		}
 	}
 	public synchronized void setHighest(Board b) {
 		if (highest == null || b.getScore() > highest.getScore()) {
 			highest = b;
 			if (highest.getScore() >= 3500) {
 				reset();
 			}
 		}
 	}
 	public synchronized void removeThread(ServerThread t) {
 		threads.remove(t);
 	}
 	private void reset() {
 		System.out.println(highest + " "
 		        + Long.toString(System.currentTimeMillis() - startTime));
 		// reset state
 		highest = null;
 		for (ServerThread t : threads) {
 			t.reset();
 		}
 		startTime = System.currentTimeMillis(); // restart timer
 	}
 }
