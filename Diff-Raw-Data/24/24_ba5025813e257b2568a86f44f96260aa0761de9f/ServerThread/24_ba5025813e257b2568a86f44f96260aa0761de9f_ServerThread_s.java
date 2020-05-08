 package net;
 
 import java.io.*;
 import java.net.*;
 import java.util.HashMap;
 import java.util.concurrent.ConcurrentHashMap;
 
 import state.FactoryState;
 
abstract public class ServerThread implements Runnable {	
 	Socket sock;
 	ObjectOutputStream os;
 	ObjectInputStream is;
 	
 	protected FactoryState state;
 	
 	/*
 	 * The server has to know before creating the ServerThread what type of
 	 * client it is. That means it has to create the i/o streams and wait for
 	 * the client to send its name so it can match it to the appropriate
 	 * handler thread.
 	 */
 	public ServerThread(Socket s, ObjectOutputStream o, ObjectInputStream i, FactoryState st) {
 		sock = s;
 		os = o;
 		is = i;
 		state = st;
 	}
 	
 	//this must be implemented by the subclass. If you're implementing the
 	//subclass, do your work here.
 	abstract public void loop();
 	
 	@SuppressWarnings("unchecked")
 	public ConcurrentHashMap<String, Object> receiveAndSend(HashMap<String, Object> input) {
 		ConcurrentHashMap<String, Object> result = null;
 		
 		//Wait for a response from the client...
 		try {
 			result = (ConcurrentHashMap<String, Object>)is.readObject();
 		} catch (IOException e) {
 			System.out.println("Client error.");
 			System.exit(1);
 		} catch (ClassNotFoundException e) {
 			e.printStackTrace();
 		}
 		
 		//then send the input map. *IMPORTANT*: YOU ARE RESPONSIBLE FOR ADDING
 		//"drawings" to the input HashMap. If you don't, the client will crash.
 		try {
 			os.writeObject((Object)input);
 			os.reset();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 		//For example, now you should be able to call result.get("drawings")
 		//which will return an ArrayList of Drawings.
 		return result;
 	}
 	
 	public void run() {		
 		while(true) {
 			loop();
 		}
 	}
 }
