 package de.rallye.control;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.net.ServerSocket;
 import java.net.Socket;
 
 import org.apache.logging.log4j.LogManager;
 import org.apache.logging.log4j.Logger;
 
 import de.rallye.resource.DataHandler;
 
 /**
  * @author Felix Hbner
  * @version 1.0
  * 
  */
 public class GameConsole {
 	Logger logger = LogManager.getLogger(GameConsole.class.getName());
 	private DataHandler data = null;
 
 	private ServerSocket socket = null;
 
 	/**
 	 *  create a tcp-console on localhost.
 	 */
 	public GameConsole(DataHandler data) {
 		logger.entry();
 		this.data = data;
 
 		try {
 			socket = new ServerSocket(this.data.getConsolePort());
 		} catch (IOException e) {
 			logger.catching(e);
 		}
 		logger.exit();
 	}
 
 	/**
 	 * wait for next connection
 	 * @return return true if the server should be closed, otherwise false
 	 * @author Felix Hbner
 	 */
 	public boolean accept() {
 		logger.entry();
 		Socket client = null;
 		BufferedReader input = null;
 		PrintWriter output = null;
 		String inputLine;
 
 		try {
 			client = socket.accept();
 			
			logger.trace("New client to address: "+client.getLocalAddress().getHostAddress()+"");
 			if (!client.getLocalAddress().getHostAddress().equals("127.0.0.1")) {
 				client.close();
 				logger.warn("client not allowed! close connection. Bye Bye!");
 				return false;
 			}
 			
 			output = new PrintWriter(client.getOutputStream(), true);
 			input = new BufferedReader(new InputStreamReader(client.getInputStream()));
 
 			output.println("RallyeServ Telnet Server. Send ? for help.");
 			logger.info("Client logged into Server Console from: "+client.getInetAddress()+":"+client.getPort());
 
 			while ((inputLine = input.readLine()) != null) {
 				if (inputLine.equals("?")) {
 					output.println("Available Commands: quit,stop.");
 				} else if (inputLine.equals("quit")) {
 					logger.info("Client Disconnected.");
 					output.println("Bye.");
 					break;
 				} else if (inputLine.equals("stop")) {
 					logger.info("Stopping Server. Bye.");
 					output.println("Stopping Server. Bye.");
 					return logger.exit(true);
 				} else {
 					output.println("Unknown Command. Send ? for help.");
 				}
 			}
 			input.close();
 			output.close();
 			client.close();
 		} catch (IOException e) {
 			logger.catching(e);
 		}
 
 		return logger.exit(false);
 	}
 	
 	/**
 	 * try to close the Game Console Socket
 	 * 
 	 * @author Felix Hbner
 	 */
 	public void close() {
 		logger.entry();
 		try {
 			socket.close();
 		} catch (IOException e) {
 			logger.catching(e);
 		}
 		logger.exit();
 	}
 
 }
