 package com.cn.client;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.io.PrintWriter;
 import java.net.InetAddress;
 import java.net.Socket;
 
 
 import org.apache.log4j.Logger;
 import org.apache.log4j.PropertyConfigurator;
 
 import com.cn.constants.ClientConstants;
 import com.cn.constants.Constants;
 import com.cn.constants.ProtocolConstants;
 import com.cn.constants.ServerConstants;
 import com.cn.protocol.Protocol;
 
 public class Client {
 
 	//World server interaction
 	protected String serverName;
 	protected int serverPort;
 	protected Socket clientSocket = null;
 	protected BufferedInputStream bis;
 	protected BufferedOutputStream bos;
 	protected BufferedReader sockBufReader;
 	protected PrintWriter sockPrintWriter;
 	
 	//Auth server interaction
 	Socket authServerSocket = null;
 	BufferedReader authSockBufReader = null;
 	PrintWriter authSockPrintWriter = null;
 	BufferedInputStream authBis;
 	BufferedOutputStream authBos;
 
 	protected UserInput userInput = null;
 	
 	private String username = null;
 	private String charName = null;
 	private boolean isLoggedIn = false;
 
 	protected String myURL = null;
 	protected String cmd = "";
 
 	Logger logger = Logger.getLogger(ClientConstants.LOGGER);
 
 	/**
 	 * Default constructor.  Sets the URL, and gets the user input.
 	 */
 	public Client() {
 		try { myURL = InetAddress.getLocalHost().getHostAddress();}
 		catch(Exception e) { e.printStackTrace(); System.exit(1); }
 	}
 
 	/**
 	 * Method to run the Client as the cmd line interface.
 	 */
 	public void runClient() {
 		connectToAuthServer();
 		userInput = new UserInput();
 		try {
 			while(true) {
 				String cmd = userInput.getUserInput();
 				String[] input = cmd.split(" ");
 				if(input[0].equals(ClientConstants.HELP)) {
 					System.out.println(ClientConstants.HELP_FORMATTED);
 				}
 				else if(input[0].equals(ClientConstants.CONNECT)) {
 					System.out.println(ClientConstants.CONNECT_ATTEMPT);
 					if(input.length != 3) {
 						System.out.println(ClientConstants.CONNECT_ATTEMPT_MALFORMED);
 					}
 					else {
 						if (connectToServer(input[1], Integer.valueOf(input[2])) == 0) {
 							System.out.println(ClientConstants.CONNECT_SUCCESS);
 						}
 					}
 				}
 				else if(input[0].equals(ClientConstants.DISCONNECT)) {
 					disconnectFromServer();
 					System.out.println(ClientConstants.DISCONNECT_SUCCESS);
 				}
 				else if(input[0].equals(ClientConstants.ATTACK)) {
 					doATTACK(input);
 				}
 				else if(input[0].equals(ClientConstants.GET_MONSTER)) {
 					doGETMONSTERS(input);
 				}
 				else if(input[0].equals(Constants.HEAL)) {
 					doHEAL(input);
 				}
 				else if(input[0].equals(Constants.REST)) {
 					doREST(input);
 				}
 				else if(input[0].equals(Constants.LOOT)) {
 					doLOOT(input);
 				}
 				else if(input[0].equals(Constants.LOGIN)) {
 					doLOGIN(input);
 				}
 				else if(input[0].equals(Constants.SAVE)) {
 					doSAVE(input);
 				}
 				else if(input[0].equals(Constants.CREATE_ACC)) {
 					doCREATEACC(input);
 				}
 			}
 		} catch(Exception e) {
 			disconnectFromServer();
 			disconnectFromAuthServer();
 			System.out.println(ClientConstants.DISCONNECT_SUCCESS);
 		}
 	}
 
 
 	/**
 	 * Method that connects to the server Given a serverName (ip/url)
 	 * and serverPort.  Returns 0 on success.
 	 */
 	protected int connectToServer(String serverName, int serverPort) { 
 		int retVal = 0;
 		this.serverName = serverName;
 		this.serverPort = serverPort;
 
 		try {
 			clientSocket = new Socket(serverName, serverPort);
 			Thread t = new Thread(new ClientShutdown());
 			Runtime.getRuntime().addShutdownHook(t);
 		} catch (Exception e) {
 			clientSocket = null;
 			retVal = -1;
 		}
 		try {
 			bis = new BufferedInputStream(clientSocket.getInputStream());
 			bos = new BufferedOutputStream(clientSocket.getOutputStream());
 			sockPrintWriter = new PrintWriter(new OutputStreamWriter(bos), true);
 			sockBufReader = new BufferedReader(new InputStreamReader(bis));
 		} catch (Exception e) {
 			clientSocket = null;
 			retVal = -1;
 		}
 		return retVal;
 	}
 	
 	/**
 	 * Connects to the authentication server.
 	 */
 	protected void connectToAuthServer() {
 		try {
 			authServerSocket = new Socket(ServerConstants.HOSTNAME, 8888);
 			Thread t = new Thread(new AuthClientShutdown());
 			Runtime.getRuntime().addShutdownHook(t);
 		} catch (Exception e) {
 			authServerSocket = null;
 		}
 		try {
 			authBis = new BufferedInputStream(authServerSocket.getInputStream());
 			authBos = new BufferedOutputStream(authServerSocket.getOutputStream());
 			authSockPrintWriter = new PrintWriter(new OutputStreamWriter(authBos), true);
 			authSockBufReader = new BufferedReader(new InputStreamReader(authBis));
 		} catch (IOException e) {
 			System.out.println(e);  //temp until we import log4j jar.
 			System.exit(1);
 		} 
 	}
 
 
 
 	/**
 	 * Destroy socket to disconnect from the server.
 	 * Sets the value of socket to null after calling close()
 	 */
 	protected void disconnectFromServer() {
 		try {
 			if (clientSocket != null) {
 				clientSocket.close();
 				clientSocket = null;
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	/**
 	 * Destroy socket to disconnect from the auth server.
 	 * Sets the value of socket to null after calling close()
 	 */
 	protected void disconnectFromAuthServer() {
 		try {
 			if (authServerSocket != null) {
 				authServerSocket.close();
 				authServerSocket = null;
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Shutdown handler for the client
 	 */
 	public class ClientShutdown extends Thread {
 		public void run() {
 			try {
 				if (clientSocket != null) {
 					System.out.println("Closing client...");
 					clientSocket.close();
 				}
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	/**
 	 * Shutdown handler for the client
 	 */
 	public class AuthClientShutdown extends Thread {
 		public void run() {
 			try {
 				if (authServerSocket != null) {
 					System.out.println("Closing client...");
 					authServerSocket.close();
 				}
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	/**
 	 * Main method that is called when you launch the 
 	 * run_client.bat or run_server.sh
 	 * 
 	 * @param args not needed for now.
 	 */
 	public static void main(String[] args) {
 		PropertyConfigurator.configure(Constants.LOGGER_PROPERTIES);
 		Client client = new Client();
 		client.runClient();
 	}
 
 	public void doATTACK(String[] args) {
 		if(args.length != 3) {
 			System.out.println(ClientConstants.INVALID_INPUT);
 		}
 		logger.debug("charName: " + charName);
 		String response = sendToServerAndGetResponse(Protocol.attackRequest(Integer.valueOf(args[1]), Double.valueOf(args[2]), charName));
 		String cmd = Protocol.getRequestCmdSimple(response);
 		String[] responseArgs = Protocol.getRequestArgsSimple(response);
 		if(cmd.equals(Constants.SUCCESS)) {
 			System.out.println("You did " + responseArgs[1] + " damage to the monster.");
 		}
 		else if(cmd.equals(Constants.NOT_ENOUGH_ENERGY)) {
 			System.out.println("You do not have enough energy to attack.");
 		}
 		else if(cmd.equals(Constants.MONSTER_WAS_KILLED)) {
 			System.out.println("You did " + responseArgs[1] + " damage to the monster and the monster was killed!");
 			System.out.println(ClientConstants.LOOT_IS_POSSIBLE);
 		}
 		else if(cmd.equals(Constants.MONSTER_DOES_NOT_EXIST)) {
 			System.out.println("You nub, this monster does not exist!!");
 		}
 		else if(cmd.equals(Constants.DEATH)) {
 			System.out.println("This monster is already dead.");
 		}
 		else {
 			//should not get here ever
 			System.out.println(ClientConstants.GENERAL_FAILURE);
 		}
 	}
 
 	public void doHEAL(String[] args) {
 		String response = sendToServerAndGetResponse(Protocol.attackRequest(Integer.valueOf(args[1]), Double.valueOf(args[2]), username));
 		if(Protocol.getRequestCmdSimple(response).equals(ProtocolConstants.SUCCESS)) {
 			System.out.println(ClientConstants.HEAL_COMPLETE);
 		}
 	}
 
 	public void doLOOT(String[] args) {
 		if(args.length != 2) {
 			System.out.println(ClientConstants.INVALID_INPUT);
 		}
 		sendToServerAndGetResponse(Protocol.convertListToProtocol(args));
 //		if() {
 //			
 //		}
 //		else {
 //			System.out.println("Failed to loot monster.");
 //		}
 	}
 
 	public void doREST(String[] args) {
 		System.out.println(ClientConstants.NOT_IMPLEMENTED);
 	}
 
 	public void doGETMONSTERS(String[] args) {
 		String response = sendToServerAndGetResponse(Protocol.getMonstersRequest());
 		System.out.println(response);
 	}
 
 	public void doLOGIN(String[] input) {
 		if(isLoggedIn) {
 			System.out.println(ClientConstants.ALREADY_LOGGED_IN);
 			return;
 		}
 		if(input.length != 3) {
 			System.out.println(ClientConstants.INVALID_INPUT);
 			return;
 		}
 		
 		String response = sendToAuthServerAndGetResponse(Protocol.convertListToProtocol(input));
 		if(response.equals(ProtocolConstants.NO_CHARS_CREATED)) {
 			System.out.println(ClientConstants.NO_CHARS_CREATED);
 			return;
 		}
 		if(response.equals(ProtocolConstants.USER_NOT_FOUND)) {
 			System.out.println(ClientConstants.USER_NOT_FOUND);
 			return;
 		}
 		System.out.println(ClientConstants.LOGIN_CHARACTERS);
 		System.out.println(response);
 		username = input[1];
 		
 		String character = userInput.getUserInput();
 		charName = character;
 		String stats = sendToAuthServerAndGetResponse(Protocol.createSimpleRequest(character));
 		
 		connectToServer(ServerConstants.HOSTNAME, ServerConstants.PORT);
 		response = sendToServerAndGetResponse(Protocol.createLoginWithCharName(stats));
 		if(response.equals(ProtocolConstants.SUCCESS)) {
 			isLoggedIn = true;
 			System.out.println(ClientConstants.LOGIN_SUCCESS);
 		}
 	}
 	
 	private void doSAVE(String[] input) {
 		if(!isLoggedIn) {
 			System.out.println(ClientConstants.NOT_LOGGED_IN);
 			return;
 		}
		if(input.length == 2) {
			String response = sendToServerAndGetResponse(Protocol.createSaveRequest(input[1]));
 			
 			String toForward = ProtocolConstants.SAVE + Protocol.createSimpleRequest(username) + response;
 			response = sendToAuthServerAndGetResponse(toForward);
 		}
 		else {
 			System.out.println(ClientConstants.INVALID_INPUT);
 		}
 	}
 	
 	private void doCREATEACC(String[] input) {
 		logger.trace("Entering the doCREATEACC method.");
 		if(input.length != 4) {
 			System.out.println(ClientConstants.INVALID_INPUT);
 		}
 		if(!input[2].equals(input[3])) {
 			System.out.println(ClientConstants.PASSWORDS_DONT_MATCH);
 		}
 		String response = sendToAuthServerAndGetResponse(Protocol.convertListToProtocol(input));
 		if(response.equals(ProtocolConstants.ACCOUNT_ALREADY_IN_USE)) {
 			System.out.println(ClientConstants.ACCOUNT_ALREADY_EXISTS);
 		}
 		if(response.equals(ProtocolConstants.SUCCESS)) {
 			System.out.println(ClientConstants.ACCOUNT_CREATED_SUCCESSFULLY);
 		}
 		logger.trace("Exiting the doCREATEACC method.");
 	}
 
 	public String sendToServerAndGetResponse(String message) {
 		try {
 			sockPrintWriter.println(message);
 			String response = sockBufReader.readLine();
 			return response;
 		}catch (Exception e) {
 			e.printStackTrace();
 			return "";
 		}	
 	}
 	
 	public String sendToAuthServerAndGetResponse(String message) {
 		try {
 			authSockPrintWriter.println(message);
 			String response = authSockBufReader.readLine();
 			return response;
 		}catch (Exception e) {
 			e.printStackTrace();
 			return "";
 		}	
 	}
 
 }
