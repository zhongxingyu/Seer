 package net;
 
 import java.io.*;
 import java.net.*;
 
 import net.threads.*;
 
 import state.*;
 
 public class Server {
 	public static final int PORT = 24042;
 	
 	FactoryState state;
 	ServerSocket ss;
 	
 	public Server() {		
 		try {
 			ss = new ServerSocket(Server.PORT);
 		} catch (IOException e) {
 			e.printStackTrace();
 			System.exit(1);
 		}
 		
 		state = new FactoryState();
 	}
 	
 	public void accept() {
 		Socket s = null;
 		ObjectInputStream is = null;
 		ObjectOutputStream os = null;
 		ManagerType manName = null;
 		
 		try {
 			s = ss.accept();
 			os = new ObjectOutputStream(s.getOutputStream());
 			is = new ObjectInputStream(s.getInputStream());
 		} catch (IOException e) {
 			e.printStackTrace();
 			System.exit(1);
 		}
 		
 		System.out.println("Found client, attempting to connect...");
 		
 		try {
 			manName = (ManagerType)is.readObject();
 			os.writeObject(manName);
 		} catch (IOException e) {
 			e.printStackTrace();
 			System.exit(1);
 		} catch (ClassNotFoundException e) {
 			e.printStackTrace();
 		}
 		
 		switch(manName) {
 		case Factory:
			new FPMThread(s, os, is, state).start();
 			break;
 		case Gantry:
			new GantryThread(s, os, is, state).start();
 			break;
 		case Kit:
			new KitThread(s, os, is, state).start();
 			break;
 		case Kit_Assembly:
			new KAMThread(s, os, is, state).start();
 			break;
 		case Lane:
			new LaneThread(s, os, is, state).start();
 			break;
 		case Part:
			new PartThread(s, os, is, state).start();
 			break;
 		}
 		
 		System.out.println(manName + " has connected.");
 	}
 	
 	public static void main(String[] args) {
 		Server s = new Server();
 		
 		System.out.println("Server started successfully.");
 		System.out.println("Now listening on port " + Server.PORT);
 		
 		while(true)
 			s.accept();
 	}
 
 }
