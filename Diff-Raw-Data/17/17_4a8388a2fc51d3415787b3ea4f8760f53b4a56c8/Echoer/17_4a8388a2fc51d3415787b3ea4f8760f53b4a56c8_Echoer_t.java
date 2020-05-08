 package edu.buffalo;
 
 import java.util.Scanner;
 
 import edu.buffalo.util.Client;
 import edu.buffalo.util.Connection;
 import edu.buffalo.util.Listener;
 import edu.buffalo.util.Server;
 import edu.buffalo.util.ServerConnections;
 
 public class Echoer {
 
 	public static String INFO = "info";
 	public static String SEND = "send ";
 	public static String SENDTO = "sendto ";
 	public static String CONNECT = "connect";
 	public static String DISCONNECT = "disconnect";
 	public static String SHOW = "show";
 	public static String UNKNOWN_COMMAND = "unknown command";
 
 	/**
 	 * Main entry point for args
 	 */
 	public static void main(String[] args) {
 		int tcp = 8989; // Default value
 		// int udp;
 
 		String[] inputs = args;
 		if (inputs.length == 2) {
 			// System.out.println("tcp port:" + inputs[0]);
 			// System.out.println("udp port:" + inputs[1]);
 			ServerConnections.TCP_PORT = Integer.valueOf(inputs[0]);
 			ServerConnections.UDP_PORT = Integer.valueOf(inputs[1]);
 
 			// start the server
 			startServer(ServerConnections.TCP_PORT);
 
 			// listen for commands
 			System.out.println("------------------------------------------");
 			System.out.println("shell window starting. enter 'quit' to exit");
 			System.out.println("------------------------------------------");
 			startShell();
 
 			// System.out.println(inputs[2]);
 		} else {
 			System.out.println(inputs.length + " Arguments not valid");
 		}
 
 		System.out.println("System quit!!");
 
 	}
 
 	/**
 	 * start server thread
 	 * 
 	 * @param tcp
 	 */
 	public static void startServer(int tcp) {
 		System.out.println("---Starting server at port:" + tcp + "----");
 		try {
 			Server server = new Server(tcp);
 			Thread thread = new Thread(server);
 			thread.start();
 			System.out.println("---Started server at port:" + tcp + "----");
 		} catch (Exception ex) {
 			ex.printStackTrace(); // error creating server hence quit
 			System.exit(1);
 		}
 	}
 
 	/**
 	 * shell command window
 	 */
 	public static void startShell() {
 		Listener listen = new Listener();
 		System.out.print(" >>");
 		Scanner in = new Scanner(System.in);
 		String line = null;
 		String response = null;
 		Client client = null;
 
 		while ((line = in.nextLine()).length() > 0) {
 			try{
 			client = null; // for garbage collection if previous have been
 							// allocated
 
 			if ("quit".equalsIgnoreCase(line)) {
 				break;
 			}
 
 			String[] params = line.split(" ");
 
 			if (INFO.equalsIgnoreCase(line)) {
 				response = info();
 			} else if (line.startsWith(SENDTO)) {
 				response = sendTo(params);
 			} else if (line.startsWith(SEND)) {
 				response = send(params);
 			} else if (line.startsWith(CONNECT)) {
 				response = connect(params);
 
 			} else if (line.startsWith(DISCONNECT)) {
 				response = disconnect(params);
 			} else if (SHOW.equalsIgnoreCase(line)) {
 				response = show();
 			} else {
 				response = listen.doWork(line);
 			}
 
 			System.out.println("" + response);
 
 			// take in next input
 			System.out.print(" >>");
 			in = new Scanner(System.in);
 			}catch(Exception ex){
 				System.out.println("Some Exception!!");
 			}
 		}
		
		System.exit(1);
 	}
 
 	/**
 	 * returns info command output
 	 * 
 	 * @return
 	 */
 	public static String info() {
 		StringBuffer info = new StringBuffer();
 		info.append("IP Address     | Host Name     | UDP Port    | TCP Port\n");
 		info.append("-------------------------------------------------------\n");
 		info.append(ServerConnections.IP_ADDRESS + "  |  "
 				+ ServerConnections.HOST_NAME + "  |  "
 				+ ServerConnections.UDP_PORT + "  |  "
 				+ ServerConnections.TCP_PORT);
 
 		return info.toString();
 	}
 
 	/**
 	 * show command response
 	 * 
 	 * @return
 	 */
 	public static String show() {
 		StringBuffer details = new StringBuffer();
 		details.append("Conn. ID    |      IP      |   hostname     | local port     | remote");
 		details.append("-----------------------------------------------------------------------");
 		for (Connection connection : ServerConnections.activeConnection) {
 			details.append(connection.connectionId + " | " + connection.ip
 					+ " | " + connection.hostname + " | "
 					+ connection.localPort + " | " + connection.remotePort
 					+ "\n");
 		}
 
 		return details.toString();
 	}
 
 	/**
 	 * 
 	 * @param params
 	 * @return
 	 */
 	public static String send(String[] params) {
 		if (params.length < 3) {
 			return UNKNOWN_COMMAND;
 		}
 
 		int connectionId = Integer.valueOf(params[1]); // followed by send
 		StringBuffer message = new StringBuffer();
 		for (int i = 1; i < params.length; i++) {
 			message.append(params[i] + " ");
 		}
 
 		// this connection must be active
 		// iterate through all and find the connection with that id
 		for (Connection connection : ServerConnections.activeConnection) {
 			if (connection.connectionId == connectionId) {
 				return connection.handler.send(message.toString()); // this
 																	// should
 																	// take care
 			}
 		}
 
 		return UNKNOWN_COMMAND + " connection not active/found";
 	}
 
 	/**
 	 * 
 	 * @param params
 	 * @return
 	 */
 	public static String disconnect(String[] params) {
 		if (params.length == 2) {
 			int connectionId = Integer.valueOf(params[1]); // followed by
 															// disconnect
 			Connection connectToDelete = null;
 			for (Connection connection : ServerConnections.activeConnection) {
 				if (connection.connectionId == connectionId) {
 					connection.handler.disconnect(); // this should take
 															// care
 					//remove from active list
 					connectToDelete = connection;
 					break;
 				}
 			}
 			
 			if(connectToDelete!=null){
 				ServerConnections.activeConnection.remove(connectToDelete);
 				//connectToDelete = null;
 				return "Disconnected from " + connectToDelete.ip;
 			}
 
 		}
 
 		return UNKNOWN_COMMAND;
 	}
 
 	/**
 	 * connect command
 	 * @param params
 	 * @return
 	 */
 	public static String connect(String[] params) {
 		if (params.length == 3) {
 			String ip = params[1];
 			int port = Integer.valueOf(params[2]);
 			Client client = new Client();
 			return client.connect(ip, port);
 		}
 
 		return UNKNOWN_COMMAND;
 	}
 	
 	public static String sendTo(String[] params){
 		if (params.length < 4) {
 			return UNKNOWN_COMMAND;
 		}
 
 		String ip = params[1]; 
 		int port = Integer.valueOf(params[2]);
 		
 		StringBuffer message = new StringBuffer();
 		for (int i = 2; i < params.length; i++) {
 			message.append(params[i] + " ");
 		}
 
 		Client client = new Client();
 		client.sendUdp(ip, port, message.toString());
 
 		return "sent UDP message";
 	}
 
 }
