 package com.ehalferty.mccoords;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.DataOutputStream;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.util.Hashtable;
 
 public class CoordsServer {
 
 	// Constants
 	private static final String CONFIG_FILENAME = "mccoordsserver.cfg";
 	
 	// Defaults
 	private static final int DEFAULT_PORT = 1338;
 	private static final String DEFAULT_PASSWORD = "PA55W0RD";
 	
 	private ServerSocket ss;
 	protected static Hashtable<String, Player> players = new Hashtable<String, Player>();
 	protected static Hashtable<Socket, DataOutputStream> outputStreams = new Hashtable<Socket, DataOutputStream>();
 
 	public CoordsServer(int port, String password) throws IOException {
 		try {
 			ss = new ServerSocket(port);
 		} catch (IOException e) {
 			System.out.println("Could not listen on port " + port);
 			e.printStackTrace();
 		}
 		System.out.println("Listening on " + ss);
 		while (true) {
 			Socket s = ss.accept();
 			System.out.println("Connection from " + s);
 			new ServerThread(this, s, password);
 		}
 	}
 	
	public static void main(String[] args) {
 		int port;
 		String password;
 		
 		if (args.length == 2) {
 			port = Integer.parseInt(args[0]);
 			password = args[1];
 		} else {
 			port = DEFAULT_PORT;
 			password = DEFAULT_PASSWORD;
 		}
 		
 		System.out.println("Running on port " + port + " with password " + password);
		try {
			new CoordsServer(port, password);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
 	}
 
 	public static void update(String name, int x, int y, int z, String addr) {
 		System.out.println("Adding coords for player " + name);
 		players.put(name, new Player(name, addr, x, y, z));
 	}
 }
